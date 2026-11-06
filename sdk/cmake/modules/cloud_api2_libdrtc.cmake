# Copyright (c) 2026 SZ DJI Technology Co., Ltd.
# SPDX-License-Identifier: Apache-2.0

# DRTC user：Linux x86_64 / aarch64 有预编译（v0.28+，glibc >= 2.34）。
# Ubuntu 22.04+ / 205 联调：裸机或 fhsdk-dev 容器。
# 麒麟 V10 宿主 glibc 2.28：不走裸机，官方高 glibc 容器整栈运行。

if(CLOUD_API2_OS STREQUAL "linux" AND (CLOUD_API2_ARCH STREQUAL "x86_64" OR CLOUD_API2_ARCH STREQUAL "aarch64"))
    set(_drtc "${CLOUD_API2_THIRD_PARTY}/libdrtc/lib/${CLOUD_API2_PLAT}")
    cloud_api2_require_file("${_drtc}/libdrtc_device_sdk.so")
    cloud_api2_require_file("${_drtc}/libdrpc.so")

    add_library(cloud_api2_drtc_sdk SHARED IMPORTED GLOBAL)
    set_target_properties(cloud_api2_drtc_sdk PROPERTIES
        IMPORTED_LOCATION "${_drtc}/libdrtc_device_sdk.so"
        INTERFACE_INCLUDE_DIRECTORIES "${CLOUD_API2_THIRD_PARTY}/libdrtc/include"
    )
    add_library(cloud_api2_drpc SHARED IMPORTED GLOBAL)
    set_target_properties(cloud_api2_drpc PROPERTIES
        IMPORTED_LOCATION "${_drtc}/libdrpc.so"
    )
    add_library(cloud_api2_libdrtc INTERFACE)
    target_link_libraries(cloud_api2_libdrtc INTERFACE cloud_api2_drtc_sdk cloud_api2_drpc)
    target_compile_definitions(cloud_api2_libdrtc INTERFACE BUILD_WITH_DRTC_USER)
    cloud_api2_alias(cloud_api2::libdrtc cloud_api2_libdrtc)
    message(STATUS "libdrtc: ${CLOUD_API2_PLAT}")
else()
    message(STATUS "libdrtc: skip (no prebuilt for ${CLOUD_API2_PLAT})")
endif()
