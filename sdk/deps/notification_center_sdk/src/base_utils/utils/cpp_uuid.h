// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <common/util/cpp_uuid.h>

namespace notification_center {

inline std::string generate_uuid_v4() {
    return cloud_api2::generate_uuid_v4();
}

}  // namespace notification_center
