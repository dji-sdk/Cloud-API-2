// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ModuleInfo {
    int boot_index = 0;
    double start_time = 0.0;
    double end_time = 0.0;
    int size = 0;

    AIGC_JSON_HELPER(boot_index, start_time, end_time, size)
};

struct ModuleInfoList {
    std::string device_sn;
    std::string module;
    int result = 0;
    std::vector<ModuleInfo> list;

    AIGC_JSON_HELPER(device_sn, module, result, list)
};

struct ServicesReplyFileuploadListNorthboundMsg {
    std::vector<ModuleInfoList> files;

    AIGC_JSON_HELPER(files)
};
///------ 用户自定义数据结构 end ------

class ServicesReplyFileuploadListNorthbound final : public MessageV1 {
public:
    ServicesReplyFileuploadListNorthbound();
    explicit ServicesReplyFileuploadListNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesReplyFileuploadListNorthboundMsg msg;
};

}