# 自研基础库。include 根 = 本目录，保持 #include <base/...> / <common/...>。

file(GLOB_RECURSE BASE_SRC CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_SOURCE_DIR}/base/*.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/base/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/base/*.c"
)

set(COMMON_SRC
    "${CMAKE_CURRENT_SOURCE_DIR}/common/executors/executors.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/common/http_client/http_request_sync.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/common/network/network.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/common/network/convert.cc"
    "${CMAKE_CURRENT_SOURCE_DIR}/common/network/impl/network_libcurl.cc"
)

add_library(cloud_api2_base STATIC ${BASE_SRC} ${COMMON_SRC})
set_target_properties(cloud_api2_base PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    OUTPUT_NAME base
)
add_library(cloud_api2::base ALIAS cloud_api2_base)

target_include_directories(cloud_api2_base PUBLIC
    "${CMAKE_CURRENT_SOURCE_DIR}"
    "${CMAKE_CURRENT_SOURCE_DIR}/common/network"
)

find_package(Threads REQUIRED)
target_link_libraries(cloud_api2_base
    PUBLIC
        cloud_api2::json11
        cloud_api2::plog
        cloud_api2::libcurl
        cloud_api2::openssl
        cloud_api2::aws_s3
    PRIVATE
        Threads::Threads
)
