// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct FH2FileDownloadInfo {
    std::string name;
    std::string url;
    int size = 0;
    std::string checksum;

    AIGC_JSON_HELPER(name, url, size, checksum)
};

struct TileFileDownloadInfo {
    std::string name;
    std::string url;
    int size = 0;
    std::string checksum;
    std::string signature;

    AIGC_JSON_HELPER(name, url, size, checksum, signature)
};

struct OfflineMapFileV2DownloadOutput {
    bool offline_map_enable = false;
    std::vector<FH2FileDownloadInfo> fh2_files;
    std::vector<TileFileDownloadInfo> tile_files;

    AIGC_JSON_HELPER(offline_map_enable, fh2_files, tile_files)
};

struct RequestsReplyOfflineMapGetV2SouthboundMsg {
    OfflineMapFileV2DownloadOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyOfflineMapGetV2Southbound final : public MessageV1 {
public:
    RequestsReplyOfflineMapGetV2Southbound();
    explicit RequestsReplyOfflineMapGetV2Southbound(const RequestsReplyOfflineMapGetV2SouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyOfflineMapGetV2SouthboundMsg msg;
};

}