# MQTT 与 RTC 消息引擎。物模型生成代码较多，用 GLOB 收集源文件。
# DRTC 仅在当前平台提供预编译库时编入。

file(GLOB_RECURSE _me_src CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp"
)
if(NOT TARGET cloud_api2::libdrtc)
    list(FILTER _me_src EXCLUDE REGEX "/rtc/drtc_user/")
endif()

add_library(cloud_api2_message_engine STATIC ${_me_src})
set_target_properties(cloud_api2_message_engine PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    OUTPUT_NAME message_engine
)
add_library(cloud_api2::message_engine ALIAS cloud_api2_message_engine)

# 预编译 libwebrtc 未包含的 rtc_base 源文件，说明见 third_party/libwebrtc/README。
target_sources(cloud_api2_message_engine PRIVATE
    "${CLOUD_API2_THIRD_PARTY}/libwebrtc/include/rtc_base/log_sinks.cc"
    "${CLOUD_API2_THIRD_PARTY}/libwebrtc/include/rtc_base/file_rotating_stream.cc"
    "${CLOUD_API2_THIRD_PARTY}/libwebrtc/include/rtc_base/strings/string_builder.cc"
    "${CLOUD_API2_THIRD_PARTY}/libwebrtc/include/rtc_base/system/file_wrapper.cc"
)

# PUBLIC 为对外头文件，PRIVATE 为实现内部的头文件搜索路径。
target_include_directories(cloud_api2_message_engine
    PUBLIC
        "${CMAKE_CURRENT_SOURCE_DIR}/src/api"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/message_wrapper"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/i_interface_user"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/message_define_user"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/message_define_user/custom"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/message_define_user/generated"
    PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}/src"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/transport"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/rtc"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/mqtt"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/mqtt/paho_mqtt"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/mqtt/mqtt_client_wrapper"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/message_decoder"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/message_scheduler"
        "${CMAKE_CURRENT_SOURCE_DIR}/src/cloud_message_manager/interface_user"
)

target_link_libraries(cloud_api2_message_engine
    PUBLIC
        cloud_api2::base
        cloud_api2::plog
    PRIVATE
        cloud_api2::aigcjson
        cloud_api2::paho
        cloud_api2::libwebrtc
)

if(TARGET cloud_api2::libdrtc)
    target_link_libraries(cloud_api2_message_engine PRIVATE cloud_api2::libdrtc)
endif()
