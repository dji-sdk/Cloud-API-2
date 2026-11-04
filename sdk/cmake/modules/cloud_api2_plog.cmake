# Copyright (c) 2026 SZ DJI Technology Co., Ltd.
# SPDX-License-Identifier: Apache-2.0

add_library(cloud_api2_plog INTERFACE)
target_include_directories(cloud_api2_plog INTERFACE
    "${CLOUD_API2_THIRD_PARTY}/plog/plog/include"
)
cloud_api2_alias(cloud_api2::plog cloud_api2_plog)
