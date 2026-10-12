// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>

#define WeakDummy(__TARGET__) \
    std::weak_ptr<drc_sdk::common::Dummy> __TARGET__(this->dummy_ptr_)

#define WeakDummyReturn(__TARGET__) \
    std::shared_ptr<drc_sdk::common::Dummy> strong_dummy_prt = __TARGET__.lock(); \
    if (!strong_dummy_prt) return;

namespace drc_sdk {
namespace common {

class Dummy {
public:
    Dummy() = default;
    ~Dummy() = default;
};

class AsyncCaptureProtect {
public:
    AsyncCaptureProtect() {
        dummy_ptr_ = std::make_shared<Dummy>();
    }
    virtual ~AsyncCaptureProtect() {
        dummy_ptr_ = nullptr;
    }
    std::shared_ptr<Dummy> dummy_ptr_;
};

} // namespace common
} // namespace drc_sdk
