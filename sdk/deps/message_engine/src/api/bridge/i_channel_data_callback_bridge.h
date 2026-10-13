// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"

namespace message_engine {

class IChannelDataCallbackBridge {
public:
    virtual ~IChannelDataCallbackBridge() = default;

    virtual void OnReceiveBinaryMessage(const std::string& target_user_id, const std::vector<uint8_t>& message) = 0;
    virtual void OnReceiveStringMessage(const std::string& target_user_id, const std::string& message) = 0;
    virtual void OnReceiveVideoData(const std::vector<uint8_t>& video_data, bool is_key_frame) = 0;
};

}