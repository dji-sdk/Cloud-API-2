// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "i_channel.h"

// Audio Data Channel的抽象类，抹平自研 RTC SDK 和 三方SDK（例如火山）的接口区别
// 可以比较方便的通过工厂方法来创建对应的实例
namespace message_engine {

class IAudioDataChannel : public IChannel {
public:
    virtual ~IAudioDataChannel() = default;

    virtual void SetAudioDataCallback(std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)>&& callback) = 0;
};
} // namespace message_engine
