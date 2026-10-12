// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include "api/message.h"
#include "response_message.h"
#include "request_message.h"

namespace flight_hub_sdk {
    // 信号地图;
    std::string to_string(const SearchSignalMapsRequest &request);

    // 创建自定义飞行区域;
    std::string to_string(const CreateCustomizedFlightAreasRequest &request);

    // 更新自定义飞行区域;
    std::string to_string(const UpdateCustomizedFlightAreasRequest &request);

    // Common request with Device SNs;
    std::string to_string(const CommonRequestWithDeviceSNs &request);

    // 更新离线地图模型;
    std::string to_string(const UpdateOfflineMapsRequest &request);

    // Common media service request with file IDs;
    std::string to_string(const CommonMediaServiceRequestWithFileIDs &request);

    // 飞行记录: 查询导出进度;
    std::string to_string(const GetExportFlightRecordsProgressParams &request);
}