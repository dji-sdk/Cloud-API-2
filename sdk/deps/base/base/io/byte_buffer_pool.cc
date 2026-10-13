// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "byte_buffer_pool.h"
#include <list>

namespace drc_sdk {

static constexpr int kDefaultBufferSize = 1024;

class ByteBufferPool::Impl {
public:
    Impl(){
        for (int i = 0; i < kMaxPoolCount; ++i) {
            auto buffer = new ByteBuffer;
            buffer->Reserve(kDefaultBufferSize);
            pool_.emplace_back(buffer);
        }
    };
    /// 申请一个ByteBuffer对象，使用完毕需要用Release来返还
    ByteBuffer* Acquire();
    /// 申请一个智能指针的ByteBuffer，不需要管理生命周期，内部会自动调用Release
    std::shared_ptr<ByteBuffer> AcquireShared();
    /// 返还一个buffer对象
    void Release(ByteBuffer* buffer);

private:
    std::list<ByteBuffer*> pool_{};
    std::mutex mutex_;
};

ByteBufferPool* ByteBufferPool::Instance() {
    static auto inst = new ByteBufferPool();
    return inst;
}
ByteBufferPool::ByteBufferPool() {
    impl_ = new ByteBufferPool::Impl();
}
ByteBufferPool::~ByteBufferPool() {
    delete impl_;
    impl_ = nullptr;
}
ByteBuffer* ByteBufferPool::Acquire() {
    return impl_->Acquire();
}
std::shared_ptr<ByteBuffer> ByteBufferPool::AcquireShared() {
    return impl_->AcquireShared();
}
void ByteBufferPool::Release(ByteBuffer* buffer) {
    impl_->Release(buffer);
}

ByteBuffer* ByteBufferPool::Impl::Acquire() {
    SYNCHRONIZED(mutex_);
    if (pool_.empty()) {
        auto buffer = new ByteBuffer;
        buffer->Reserve(kDefaultBufferSize);
        return buffer;
    }
    auto buffer = pool_.front();
    pool_.pop_front();
    buffer->Reserve(kDefaultBufferSize);
    buffer->Clear();
    return buffer;
}

void ByteBufferPool::Impl::Release(ByteBuffer* buffer) {
    SYNCHRONIZED(mutex_);
    if (pool_.size() > kMaxPoolCount) {
        delete buffer;
        return;
    }
    pool_.emplace_back(buffer);
}

std::shared_ptr<ByteBuffer> ByteBufferPool::Impl::AcquireShared() {
    return {Acquire(), [this] (ByteBuffer* buffer) {
        Release(buffer);
    }};
}
}  // namespace drc_sdk

