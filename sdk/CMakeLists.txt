cmake_minimum_required(VERSION 3.20)

file(STRINGS "${CMAKE_CURRENT_LIST_DIR}/VERSION" _CLOUD_API2_VERSION LIMIT_COUNT 1)
string(STRIP "${_CLOUD_API2_VERSION}" _CLOUD_API2_VERSION)
if(NOT _CLOUD_API2_VERSION MATCHES "^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$")
    set(_CLOUD_API2_VERSION "${_CLOUD_API2_VERSION}.0")
endif()

project(cloud_api2 VERSION ${_CLOUD_API2_VERSION} LANGUAGES C CXX)

# BIN-02: 默认隐藏符号可见性, 收敛动态库导出面。
# 仓内唯一非静态目标为 flighthubsdk_jni(SHARED, 终端 JNI 库, 无跨 DSO 依赖),
# 其内部 C++ 符号来自静态链接的 cloud_api2_* 静态库, 仅对 JNI target 设
# -fvisibility=hidden 无法覆盖静态库目标, 必须全局生效。
# SWIG 生成的 Java_* 入口经 SWIGEXPORT/JNIEXPORT(含 visibility("default"))显式导出, 不受影响。
set(CMAKE_C_VISIBILITY_PRESET hidden)
set(CMAKE_CXX_VISIBILITY_PRESET hidden)
set(CMAKE_VISIBILITY_INLINES_HIDDEN ON)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(CLOUD_API2_ROOT "${CMAKE_CURRENT_LIST_DIR}")
list(APPEND CMAKE_MODULE_PATH "${CLOUD_API2_ROOT}/cmake/modules")

include(cloud_api2_platform)
include(cloud_api2_options)
include(cloud_api2_third_party)
include(cloud_api2_h264)
include(cloud_api2_blocking_detector)

add_subdirectory(deps/base)
add_subdirectory(deps/message_engine)
add_subdirectory(deps/notification_center_sdk)
add_subdirectory(src)

if(ENABLE_JAVA)
    add_subdirectory(platform/java)
endif()
