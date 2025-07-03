// Copyright 2024 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.

#include "devtool/lynx_devtool/js_debug/lepus/inspector_lepus_debugger_impl.h"

#include "core/services/recorder/recorder_controller.h"
#include "devtool/lynx_devtool/config/devtool_config.h"
#include "devtool/lynx_devtool/recorder/test_bench_utils.h"
#include "third_party/httplib/httplib.h"

namespace lynx {
namespace devtool {

namespace {

void SplitUrl(const std::string &url, std::string &host, std::string &path) {
  size_t protocol_end = url.find("://");
  size_t pos = 0;
  if (protocol_end != std::string::npos) {
    pos = protocol_end + 3;
  } else {
    size_t single_slash_pos = url.find(":/");
    if (single_slash_pos != std::string::npos) {
      pos = single_slash_pos + 2;
    }
  }

  size_t path_start = url.find('/', pos);
  if (path_start != std::string::npos) {
    host = url.substr(pos, path_start - pos);
    path = url.substr(path_start);
  } else {
    host = url.substr(pos);
    path = "/";
  }
}

constexpr int timeout_sec = 5;
constexpr char kDownloadThread[] = "MTS_DebugInfo_Download";

}  // namespace

InspectorLepusDebuggerImpl::InspectorLepusDebuggerImpl(
    const std::shared_ptr<LynxDevToolMediator> &devtool_mediator)
    : JavaScriptDebuggerNG(devtool_mediator) {}

const std::shared_ptr<InspectorLepusObserverImpl> &
InspectorLepusDebuggerImpl::GetInspectorLepusObserver() {
  if (observer_ == nullptr) {
    observer_ = std::make_shared<InspectorLepusObserverImpl>(
        std::static_pointer_cast<InspectorLepusDebuggerImpl>(
            shared_from_this()));
  }
  return observer_;
}

void InspectorLepusDebuggerImpl::SetRecordID(int64_t record_id) {
  record_id_ = record_id;
}

void InspectorLepusDebuggerImpl::DecodeDebugInfo(const std::string &debug_info,
                                                 std::string &result) {
  std::string decode_debug_info = TestBenchDecode(debug_info);
  std::vector<uint8_t> compressed_data(decode_debug_info.begin(),
                                       decode_debug_info.end());
  std::vector<uint8_t> decompressed_data = TestBenchDecompress(compressed_data);
  result.assign(decompressed_data.begin(), decompressed_data.end());
}

std::string InspectorLepusDebuggerImpl::GetDebugInfo(const std::string &url) {
  LOGI("lepus debug: get debug info, url: " << url);
  auto sp = devtool_platform_facade_wp_.lock();
  CHECK_NULL_AND_LOG_RETURN_VALUE(
      sp, "lepus debug: devtool_platform_facade_ is null", "");

  std::string debug_info = sp->GetDebugInfoByUrl(url);
  if (debug_info == DevToolStatus::NO_DEBUG_INFO_FOUND_BY_URL) {
    std::string host, path;
    SplitUrl(url, host, path);
    if (host.empty()) {
      LOGE("lepus debug: Failed to download debug-info.json! Empty host!");
      return "";
    }

    httplib::Client client(host);
    client.set_max_timeout(timeout_sec * 1000);
    client.set_connection_timeout(timeout_sec);
    client.set_read_timeout(timeout_sec);

    // Since httplib calls `CFRunLoopRunInMode()` during downloading (see
    // `getaddrinfo_with_timeout()`), which can cause subsequent tasks to be
    // executed prematurely and lead to unexpected behavior or errors.
    // Therefore, we dispatch the download task to a separate thread and use a
    // future to block the current thread while waiting for the result.
    std::promise<httplib::Result> promise;
    std::future<httplib::Result> future = promise.get_future();
    const auto &task_runner = GetDownloadTaskRunner();
    fml::TaskRunner::RunNowOrPostTask(task_runner,
                                      [client = std::move(client), path,
                                       promise = std::move(promise)]() mutable {
                                        auto res = client.Get(path.c_str());
                                        promise.set_value(std::move(res));
                                      });

    if (future.wait_for(std::chrono::seconds(timeout_sec)) !=
        std::future_status::ready) {
      LOGE("lepus debug: Failed to download debug-info.json! Timeout!");
      return "";
    }

    auto res = future.get();
    if (res == nullptr) {
      LOGE("lepus debug: Failed to download debug-info.json! Null response!");
      return "";
    }

    if (res->status == 200) {
      LOGI("lepus debug: Successfully downloaded debug-info.json!");
      debug_info = res->body;
    } else {
      LOGE("lepus debug: Failed to download debug-info.json! status: "
           << res->status << ", reason: " << res->reason);
      return "";
    }
  } else {
    DecodeDebugInfo(debug_info, debug_info);
  }
  if (record_id_ != 0) {
    tasm::recorder::RecorderController::RecordDebugInfo(record_id_, url,
                                                        debug_info);
  }
  return debug_info;
}

void InspectorLepusDebuggerImpl::SetDebugInfoUrl(const std::string &url,
                                                 const std::string &file_name) {
  file_name_to_debug_info_url_[file_name] = url;
}

std::string InspectorLepusDebuggerImpl::GetDebugInfoUrl(
    const std::string &file_name) {
  auto it = file_name_to_debug_info_url_.find(file_name);
  if (it != file_name_to_debug_info_url_.end()) {
    return it->second;
  }
  return "";
}

void InspectorLepusDebuggerImpl::OnInspectorInited(
    const std::string &vm_type, const std::string &name,
    const std::shared_ptr<devtool::InspectorClientNG> &client) {
  std::unique_lock<std::mutex> lock(mutex_);
  auto it = delegates_.find(name);
  if (it == delegates_.end()) {
    auto delegate =
        InspectorClientDelegateProvider::GetInstance()->GetDelegate(vm_type);
    delegate->InsertDebugger(
        std::static_pointer_cast<InspectorLepusDebuggerImpl>(
            shared_from_this()),
        true);
    delegate->SetTargetId(name);
    it = (delegates_.emplace(name, delegate)).first;
  }
  // InspectorClientNG will be destroyed and recreated after reloading, so we
  // need to reset the pointer.
  auto delegate = it->second;
  delegate->SetInspectorClient(client);
  client->SetInspectorClientDelegate(delegate);

  if (tasm::LynxEnv::GetInstance().IsDevToolConnected()) {
    delegate->OnTargetCreated();
    delegate->DispatchInitMessage(kDefaultViewID, false);
  }
}

void InspectorLepusDebuggerImpl::OnContextDestroyed(const std::string &name) {
  std::unique_lock<std::mutex> lock(mutex_);
  auto it = delegates_.find(name);
  if (it != delegates_.end()) {
    it->second->OnTargetDestroyed();
  }
}

void InspectorLepusDebuggerImpl::PrepareForScriptEval(const std::string &name) {
  if (tasm::LynxEnv::GetInstance().IsDevToolConnected()) {
    std::unique_lock<std::mutex> lock(mutex_);
    auto it = delegates_.find(name);
    if (it != delegates_.end()) {
      it->second->SetStopAtEntry(DevToolConfig::ShouldStopAtEntry(true),
                                 kDefaultViewID);
    }
  }
}

// Undefine the 'DispatchMessage' macro to prevent it from being replaced by
// 'DispatchMessageW' on Windows.
#undef DispatchMessage
void InspectorLepusDebuggerImpl::DispatchMessage(
    const std::string &message, const std::string &session_id) {
  std::unique_lock<std::mutex> lock(mutex_);
  auto it = delegates_.find(session_id);
  if (it != delegates_.end()) {
    it->second->DispatchMessageAsync(message, kDefaultViewID);
  }
}

void InspectorLepusDebuggerImpl::RunOnTargetThread(base::closure &&closure,
                                                   bool run_now) {
  auto sp = devtool_mediator_wp_.lock();
  CHECK_NULL_AND_LOG_RETURN(sp, "lepus debug: devtool_mediator_ is null");
  sp->RunOnTASMThread(std::move(closure), run_now);
}

void InspectorLepusDebuggerImpl::UpdateTarget() {
  std::unique_lock<std::mutex> lock(mutex_);
  for (const auto &delegate : delegates_) {
    delegate.second->OnTargetCreated();
  }
}

const fml::RefPtr<fml::TaskRunner> &
InspectorLepusDebuggerImpl::GetDownloadTaskRunner() {
  static base::NoDestructor<fml::Thread> thread(fml::Thread::ThreadConfig(
      kDownloadThread, fml::Thread::ThreadPriority::NORMAL));
  return (*thread).GetTaskRunner();
}

}  // namespace devtool
}  // namespace lynx
