// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct StorageConfigGetOutputCredentials {
    std::string access_key_id;
    std::string access_key_secret;
    std::string expire;
    std::string platform;
    std::string security_token;

    AIGC_JSON_HELPER(access_key_id, access_key_secret, expire, platform, security_token)
};

struct StorageConfigGetOutputExtCloudToCloudConfig {
    std::string bucket;
    std::string cloud_to_cloud_id;
    StorageConfigGetOutputCredentials credentials;
    std::string endpoint;
    std::string object_key_prefix;
    std::string provider;
    std::string region;

    AIGC_JSON_HELPER(bucket, cloud_to_cloud_id, credentials, endpoint, object_key_prefix, provider, region)
};

struct StorageConfigGetOutputExt {
    std::vector<StorageConfigGetOutputExtCloudToCloudConfig> cloud_to_cloud_configs;

    AIGC_JSON_HELPER(cloud_to_cloud_configs)
};

struct StorageConfigGetOutput {
    std::string bucket;
    StorageConfigGetOutputCredentials credentials;
    std::string endpoint;
    std::string object_key_prefix;
    std::string provider;
    std::string region;
    StorageConfigGetOutputExt ext;

    AIGC_JSON_HELPER(bucket, credentials, endpoint, object_key_prefix, provider, region, ext)
};

struct RequestsReplyStorageConfigGetSouthboundMsg {
    StorageConfigGetOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyStorageConfigGetSouthbound final : public MessageV1 {
public:
    RequestsReplyStorageConfigGetSouthbound();
    explicit RequestsReplyStorageConfigGetSouthbound(const RequestsReplyStorageConfigGetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyStorageConfigGetSouthboundMsg msg;
};

}