// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct OfflineMapFileDownloadInfo {
    std::string name;
    std::string url;
    int size = 0;
    std::string checksum;

    AIGC_JSON_HELPER(name, url, size, checksum)
};

struct OfflineMapFileDownloadOutput {
    bool offline_map_enable = false;
    std::vector<OfflineMapFileDownloadInfo> files;

    AIGC_JSON_HELPER(offline_map_enable, files)
};

struct RequestsReplyOfflineMapGetSouthboundMsg {
    OfflineMapFileDownloadOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyOfflineMapGetSouthbound final : public MessageV1 {
public:
    RequestsReplyOfflineMapGetSouthbound();
    explicit RequestsReplyOfflineMapGetSouthbound(const RequestsReplyOfflineMapGetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyOfflineMapGetSouthboundMsg msg;
};

}