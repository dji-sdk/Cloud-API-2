// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct SingleLogFile {
    int boot_index = 0;
    double size = 0.0;

    AIGC_JSON_HELPER(boot_index, size)
};

struct SingleDeviceLogFile {
    std::string device_sn;
    std::string module;
    std::string object_key;
    std::vector<SingleLogFile> list;

    AIGC_JSON_HELPER(device_sn, module, object_key, list)
};

struct DeviceLogFiles {
    std::vector<SingleDeviceLogFile> files;

    AIGC_JSON_HELPER(files)
};

struct CredentialInfo {
    std::string access_key_id;
    std::string access_key_secret;
    std::string security_token;
    int expire = 0;
    DeviceLogFiles params;

    AIGC_JSON_HELPER(access_key_id, access_key_secret, security_token, expire, params)
};

struct ServicesFileuploadStartSouthboundMsg {
    std::string bucket;
    std::string endpoint;
    std::string provider;
    std::string region;
    CredentialInfo credentials;

    AIGC_JSON_HELPER(bucket, endpoint, provider, region, credentials)
};
///------ 用户自定义数据结构 end ------

class ServicesFileuploadStartSouthbound final : public MessageV1 {
public:
    ServicesFileuploadStartSouthbound();
    explicit ServicesFileuploadStartSouthbound(const ServicesFileuploadStartSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesFileuploadStartSouthboundMsg msg;
};

}