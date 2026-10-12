# DRTC user 仅 Linux x86_64 有预编译；其它平台不创建目标。

if(CLOUD_API2_OS STREQUAL "linux" AND CLOUD_API2_ARCH STREQUAL "x86_64")
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
