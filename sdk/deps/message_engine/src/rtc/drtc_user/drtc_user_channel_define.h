// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>

namespace message_engine {

// Stream type constants (aligned with DrtcStreamType enum in drtc_device_sdk_define.h)
static constexpr uint8_t DRTC_USER_STREAM_TYPE_VIDEO = 1;    // DRTC_STREAM_VIDEO
static constexpr uint8_t DRTC_USER_STREAM_TYPE_COMMAND = 2;  // DRTC_STREAM_COMMAND
static constexpr uint8_t DRTC_USER_STREAM_TYPE_CONTROL = 3;  // DRTC_STREAM_CONTROL

// Stream tag strings for user-side (subscribe model)
static const std::string DRTC_USER_CMD_STREAM_TAG = "_cmd";
static const std::string DRTC_USER_CTRL_STREAM_TAG = "_ctrl";
static const std::string DRTC_USER_VIDEO_STREAM_TAG = "_video";

// Owner ID constants for engine manager reference counting
static const std::string DRTC_USER_OWNER_REALTIME = "realtime";
static const std::string DRTC_USER_OWNER_LIVESTREAM = "livestream";

// Timeout constants
static constexpr int32_t DRTC_USER_DEFAULT_TIMEOUT_MS = 5000;
static constexpr int32_t DRTC_USER_SUBSCRIBE_TIMEOUT_MS = 10000;

// DRTC device type for user side (WEB node)
static constexpr uint8_t DRTC_USER_DEVICE_TYPE_NODE = 6;  // DRTC_DEVICE_NODE

}  // namespace message_engine
