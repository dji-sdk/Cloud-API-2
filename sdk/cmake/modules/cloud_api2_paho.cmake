set(_paho_root "${CLOUD_API2_THIRD_PARTY}/paho_mqtt_c")
if(CLOUD_API2_OS STREQUAL "macosx")
    set(_paho_lib "${_paho_root}/lib/${CLOUD_API2_PLAT}/libpaho-mqtt3as.1.dylib")
elseif(CLOUD_API2_OS STREQUAL "linux")
    set(_paho_lib "${_paho_root}/lib/${CLOUD_API2_PLAT}/libpaho-mqtt3as.so.1")
else()
    message(FATAL_ERROR "paho: no recipe for ${CLOUD_API2_PLAT}")
endif()

cloud_api2_require_file("${_paho_lib}")

add_library(cloud_api2_paho SHARED IMPORTED GLOBAL)
set_target_properties(cloud_api2_paho PROPERTIES
    IMPORTED_LOCATION "${_paho_lib}"
    INTERFACE_INCLUDE_DIRECTORIES "${_paho_root}/include"
)
target_compile_definitions(cloud_api2_paho INTERFACE BUILD_WITH_PAHO_MQTT)
cloud_api2_alias(cloud_api2::paho cloud_api2_paho)
