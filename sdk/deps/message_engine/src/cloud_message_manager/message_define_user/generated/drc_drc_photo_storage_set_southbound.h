// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcPhotoStorageSetSouthboundMsg {
    std::string payload_index;
    std::vector<std::string> photo_storage_settings;

    AIGC_JSON_HELPER(payload_index, photo_storage_settings)
};
///------ 用户自定义数据结构 end ------

class DrcDrcPhotoStorageSetSouthbound final : public MessageV2 {
public:
    DrcDrcPhotoStorageSetSouthbound();
    explicit DrcDrcPhotoStorageSetSouthbound(const DrcDrcPhotoStorageSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcPhotoStorageSetSouthboundMsg msg;
};

}