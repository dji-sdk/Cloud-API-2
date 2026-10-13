// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesPhotoStorageSetSouthboundMsg {
    std::string payload_index;
    std::vector<std::string> photo_storage_settings;

    AIGC_JSON_HELPER(payload_index, photo_storage_settings)
};
///------ 用户自定义数据结构 end ------

class ServicesPhotoStorageSetSouthbound final : public MessageV1 {
public:
    ServicesPhotoStorageSetSouthbound();
    explicit ServicesPhotoStorageSetSouthbound(const ServicesPhotoStorageSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesPhotoStorageSetSouthboundMsg msg;
};

}