// Copyright 2019 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
#include "base/include/value/table.h"

#include "base/include/log/logging.h"
#include "base/include/value/base_value.h"

namespace lynx {
namespace lepus {

bool Dictionary::Erase(const base::String& key) {
  if (IsConstLog()) {
    return false;
  }
  map_.erase(key);
  return true;
}

int32_t Dictionary::EraseKey(const base::String& key) {
  if (IsConstLog()) {
    return -1;
  }
  return static_cast<int32_t>(map_.erase(key));
}

Dictionary::ValueWrapper Dictionary::GetValue(const base::String& key) const {
  const auto* ptr = map_.find(key);
  if (ptr != nullptr) {
    return ValueWrapper(ptr);
  } else {
    static Value kNil;
    return ValueWrapper(&kNil);
  }
}

Dictionary::ValueWrapper Dictionary::GetValueOrUndefined(
    const base::String& key) const {
  const auto* ptr = map_.find(key);
  if (ptr != nullptr) {
    return ValueWrapper(ptr);
  } else {
    static Value kUndefined(Value::kCreateAsUndefinedTag);
    return ValueWrapper(&kUndefined);
  }
}

Dictionary::ValueWrapper Dictionary::GetValueOrNull(
    const base::String& key) const {
  return ValueWrapper(map_.find(key));
}

Dictionary::ValueWrapper Dictionary::GetValueOrInsert(const base::String& key) {
  if (IsConstLog()) {
    return ValueWrapper(nullptr);
  } else {
    return ValueWrapper(&map_[key]);
  }
}

Dictionary::ValueWrapper Dictionary::GetValueOrInsert(base::String&& key) {
  if (IsConstLog()) {
    return ValueWrapper(nullptr);
  } else {
    return ValueWrapper(&map_[std::move(key)]);
  }
}

void Dictionary::Dump() {
  LOGE("begin dump dict----------");
  auto it = begin();
  for (; it != end(); it++) {
    lepus::Value value = it->second;
    if (value.IsNumber()) {
      LOGE(it->first.str() << " : " << value.Number());
    }

    else if (value.IsString()) {
      LOGE(it->first.str() << " : " << value.StdString());
    } else if (value.IsTable()) {
      LOGE(it->first.str() << " : ===>");
      value.Table()->Dump();
    } else if (value.IsBool()) {
      LOGE(it->first.str() << " : "
                           << ((value.Bool() == true) ? "true" : "false"));
    } else if (value.IsArray()) {
      LOGE(it->first.str() << " : []");
    } else {
      LOGE(it->first.str() << " : type is " << value.Type());
    }
  }
  LOGE("end dump dict----------");
}

bool operator==(const Dictionary& left, const Dictionary& right) {
  if (left.size() != right.size()) {
    return false;
  }
  for (const auto& [key, value] : left) {
    auto it = right.find(key);
    if (it == right.end()) {
      return false;
    }
    if (value != it->second) {
      return false;
    }
  }
  return true;
}

}  // namespace lepus
}  // namespace lynx
