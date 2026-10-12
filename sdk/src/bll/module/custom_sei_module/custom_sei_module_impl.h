// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/custom_sei.h"
#include "common/util/async_capture_protect.h"
#include "custom_sei_data_define.h"
#include "message.h"

namespace flight_hub_sdk {

class CustomSEIModuleImpl : public ICustomSEIModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    CustomSEIModuleImpl();
    ~CustomSEIModuleImpl() override = default;

    bool CustomSEIEncode(const std::vector<int>& frame_data,
                        const std::vector<SeiItem>& custom_sei_data,
                        std::vector<int>& output_frame_data) override;

    bool CustomSEIDecode(const std::vector<int>& frame_data,
                        SeiHeader& output_header,
                        std::vector<SeiItem>& output_sei_data) override;

private:
    // 数据转换函数
    static std::vector<std::shared_ptr<CustomSeiTLVDataBase>> convertToH264AnalyzeData(const std::vector<SeiItem>& sdk_data);
    static std::vector<SeiItem> convertFromH264AnalyzeData(const std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& h264_data);
    static CustomSEIHeader convertToH264AnalyzeHeader(const SeiHeader& sdk_header);
    static SeiHeader convertFromH264AnalyzeHeader(const CustomSEIHeader& h264_header);
};

} // namespace flight_hub_sdk
