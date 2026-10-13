// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcUpDrcCameraPhotoInfoPushNorthboundMsgExt {
    int camera_mode = 0;

    AIGC_JSON_HELPER(camera_mode)
};

struct DrcUpDrcCameraPhotoInfoPushNorthboundMsgProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct DrcDrcCameraPhotoInfoPushNorthboundMsg {
    DrcUpDrcCameraPhotoInfoPushNorthboundMsgExt ext;
    DrcUpDrcCameraPhotoInfoPushNorthboundMsgProgress progress;
    int countdown_time = 0;
    int result = 0;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, countdown_time, result, status)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraPhotoInfoPushNorthbound final : public MessageV2 {
public:
    DrcDrcCameraPhotoInfoPushNorthbound();
    explicit DrcDrcCameraPhotoInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraPhotoInfoPushNorthboundMsg msg;
};

}