// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <string>
#include <functional>
#include "i_channel_status_callback_bridge.h"
#include "i_channel_data_callback_bridge.h"
#include "channel_info_bridge.h"

namespace message_engine {

/**
 * @brief 直播流通道桥接接口
 * 
 * 用于跨语言互操作的直播流通道接口
 */
class ILiveStreamChannelBridge {
public:
    virtual ~ILiveStreamChannelBridge() = default;
    // 回调设置
    virtual bool SetChannelStatusCallback(const std::shared_ptr<IChannelStatusCallbackBridge>& callback) = 0;
    virtual bool SetReceiveVideoDataCallback(const std::shared_ptr<IChannelDataCallbackBridge>& callback) = 0;

    // 通道控制
    virtual bool Join(const ChannelInfoBridge& channel_info, std::function<void(bool is_success)> callback) = 0;
    virtual bool Leave() = 0;
    virtual bool RenewToken(const std::string& token) = 0;
};

} // namespace message_engine 