// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct SelfDiagnosticUploadConfigCredentials {
    std::string access_key_id;
    std::string access_key_secret;
    std::string expire;
    std::string platform;
    std::string security_token;

    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, platform, security_token)
};

struct SelfDiagnosticUploadConfig {
    std::string bucket;
    SelfDiagnosticUploadConfigCredentials credentials;
    std::string endpoint;
    std::string object_key;
    std::string provider;
    std::string region;

    AIGC_JSON_HELPER(bucket, credentials, endpoint, object_key, provider, region)
};

struct ServicesSelfDiagnosticSouthboundMsg {
    int type = 0;
    int part = 0;
    int index = 0;
    int motor_action = 0;
    bool upload_enable = false;
    SelfDiagnosticUploadConfig upload_config;

    AIGC_JSON_HELPER(type, part, index, motor_action, upload_enable, upload_config)
};
///------ 用户自定义数据结构 end ------

class ServicesSelfDiagnosticSouthbound final : public MessageV1 {
public:
    ServicesSelfDiagnosticSouthbound();
    explicit ServicesSelfDiagnosticSouthbound(const ServicesSelfDiagnosticSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesSelfDiagnosticSouthboundMsg msg;
};

}