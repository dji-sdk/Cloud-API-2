// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <string>
#include "i_channel_status_callback_bridge.h"
#include "i_channel_data_callback_bridge.h"
#include "channel_info_bridge.h"

namespace message_engine {

/**
 * @brief 实时通道桥接接口
 * 
 * 用于跨语言互操作的实时通道接口
 */
class IRealTimeChannelBridge {
public:
    virtual ~IRealTimeChannelBridge() = default;

    // 回调设置
    virtual bool SetChannelStatusCallback(const std::shared_ptr<IChannelStatusCallbackBridge>& callback) = 0;

    // 通道控制
    virtual bool Join(const ChannelInfoBridge& channel_info, std::function<void(bool is_success)> callback) = 0;
    virtual bool Leave() = 0;
    virtual bool RenewToken(const std::string& token) = 0;

    // 消息发送
    virtual bool SendMessage(const std::string& target_user_id,  const std::vector<uint8_t>& message, bool is_reliable) = 0;

    // 消息接收回调
    virtual bool SetReceiveMessageCallback(const std::shared_ptr<IChannelDataCallbackBridge>& callback) = 0;
};

} // namespace message_engine 