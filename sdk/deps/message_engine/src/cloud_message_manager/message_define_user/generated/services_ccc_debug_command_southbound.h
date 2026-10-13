// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesCccDebugCommandUpdateCccParam {
    std::string zip_md5;

    AIGC_JSON_HELPER(zip_md5)
};

struct ServicesCccDebugCommandCccConfig {
    std::string env;

    AIGC_JSON_HELPER(env)
};

struct ServicesCccDebugCommandCccConfigParam {
    std::string version;
    ServicesCccDebugCommandCccConfig ccc_config;

    AIGC_JSON_HELPER(version, ccc_config)
};

struct ServicesCccDebugCommandMockSdrDisconnectParam {
    int disconnect_time = 0;

    AIGC_JSON_HELPER(disconnect_time)
};

struct ServicesCccDebugCommandSouthboundMsg {
    int type = 0;
    ServicesCccDebugCommandUpdateCccParam update_ccc;
    ServicesCccDebugCommandCccConfigParam modify_ccc_config;
    ServicesCccDebugCommandMockSdrDisconnectParam mock_sdr_disconnect;

    AIGC_JSON_HELPER(type, update_ccc, modify_ccc_config, mock_sdr_disconnect)
};
///------ 用户自定义数据结构 end ------

class ServicesCccDebugCommandSouthbound final : public MessageV1 {
public:
    ServicesCccDebugCommandSouthbound();
    explicit ServicesCccDebugCommandSouthbound(const ServicesCccDebugCommandSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesCccDebugCommandSouthboundMsg msg;
};

}