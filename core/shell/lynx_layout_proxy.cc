// Copyright 2025 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
#include "lynx_layout_proxy.h"

#include <memory>
#include <utility>

#include "lynx/core/renderer/ui_wrapper/layout/layout_context.h"

namespace lynx {
namespace shell {

void LynxLayoutProxy::DispatchTaskToLynxLayout(base::closure task) {
  actor_->Act([task = std::move(task)](auto& layout) mutable { task(); });
}

void LynxLayoutProxy::TriggerLayout() {
  actor_->Act([](auto& layout) { layout->Layout(); });
}

}  // namespace shell
}  // namespace lynx
