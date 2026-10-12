file(GLOB_RECURSE NC_SRC CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_SOURCE_DIR}/src/sdk_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/business_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/topic_engine/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/websocket/*.cpp"
)

add_library(cloud_api2_notification_center STATIC ${NC_SRC})
set_target_properties(cloud_api2_notification_center PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    OUTPUT_NAME NotificationCenterSDK
)
add_library(cloud_api2::notification_center ALIAS cloud_api2_notification_center)

target_include_directories(cloud_api2_notification_center
    PUBLIC
        "${CMAKE_CURRENT_SOURCE_DIR}/src/notification_center_sdk_api"
    PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}/src"
)

find_package(Threads REQUIRED)
target_link_libraries(cloud_api2_notification_center
    PUBLIC
        cloud_api2::base
        cloud_api2::plog
    PRIVATE
        cloud_api2::websockets
        cloud_api2::openssl
        cloud_api2::aigcjson
        Threads::Threads
)
