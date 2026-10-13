// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <atomic>
#include <mutex>


#include "base/common/macros.h"
#include "base/io/byte_buffer.h"

namespace drc_sdk {

static constexpr int kMaxPoolCount = 50;

/// ByteBuffer的池子，默认每个buffer的size为1024，max count为10，先默认非线程安全
class ByteBufferPool {
 public:
    static ByteBufferPool* Instance();

    /// 申请一个ByteBuffer对象，使用完毕需要用Release来返还
    ByteBuffer* Acquire();
    /// 申请一个智能指针的ByteBuffer，不需要管理生命周期，内部会自动调用Release
    std::shared_ptr<ByteBuffer> AcquireShared();
    /// 返还一个buffer对象
    void Release(ByteBuffer* buffer);

 private:
    explicit ByteBufferPool();
    ~ByteBufferPool();
    class Impl;
    Impl* impl_ = nullptr;
};


}  // namespace drc_sdk
