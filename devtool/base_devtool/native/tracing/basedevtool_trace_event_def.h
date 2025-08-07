// Copyright 2025 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.

#ifndef DEVTOOL_BASE_DEVTOOL_NATIVE_TRACING_BASEDEVTOOL_TRACE_EVENT_DEF_H_
#define DEVTOOL_BASE_DEVTOOL_NATIVE_TRACING_BASEDEVTOOL_TRACE_EVENT_DEF_H_

#include <string>

#include "base/trace/native/trace_event.h"
#include "core/base/lynx_trace_categories.h"

#if ENABLE_TRACE_PERFETTO || ENABLE_TRACE_SYSTRACE

inline constexpr const char* const DISPATCH_MESSAGE_ASYNC =
    "DispatchMessageAsync";

#define TRACE_EVENT_PROCESS_BEGIN(m)                                       \
  rapidjson::Document json_mes;                                            \
  if (!ParseStrToJson(json_mes, mes)) {                                    \
    return;                                                                \
  }                                                                        \
  std::string message_name = json_mes.GetObject()[kKeyMethod].GetString(); \
  int id = json_mes.GetObject()[kKeyId].GetInt();                          \
  std::string event_name = "DispatchMessage." + message_name;              \
  TRACE_EVENT_BEGIN(LYNX_TRACE_CATEGORY_DEVTOOL, event_name, "id", id);

#define TRACE_EVENT_PROCESS_END TRACE_EVENT_END(LYNX_TRACE_CATEGORY_DEVTOOL);

#else  // #if ENABLE_TRACE_PERFETTO || ENABLE_TRACE_SYSTRACE

#define TRACE_EVENT_PROCESS_BEGIN(m)
#define TRACE_EVENT_PROCESS_END

#endif

#endif  // DEVTOOL_BASE_DEVTOOL_NATIVE_TRACING_BASEDEVTOOL_TRACE_EVENT_DEF_H_
