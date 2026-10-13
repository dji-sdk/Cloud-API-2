// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcParachuteInfoPushNorthboundMsg {
    std::string parachute_firmware_version;
    int parachute_is_fired = 0;
    int parachute_on_fire_status = 0;
    int parachute_type = 0;
    std::string parachute_sn;
    int parachute_is_abnormal = 0;

    AIGC_JSON_HELPER(parachute_firmware_version, parachute_is_fired, parachute_on_fire_status, parachute_type, parachute_sn, parachute_is_abnormal)
};
///------ 用户自定义数据结构 end ------

class DrcDrcParachuteInfoPushNorthbound final : public MessageV2 {
public:
    DrcDrcParachuteInfoPushNorthbound();
    explicit DrcDrcParachuteInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcParachuteInfoPushNorthboundMsg msg;
};

}