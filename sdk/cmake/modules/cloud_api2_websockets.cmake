set(_lws_root "${CLOUD_API2_THIRD_PARTY}/websockets")
if(CLOUD_API2_OS STREQUAL "macosx")
    set(_lws_lib "${_lws_root}/lib/${CLOUD_API2_PLAT}/libwebsockets.12.dylib")
elseif(CLOUD_API2_OS STREQUAL "linux")
    set(_lws_lib "${_lws_root}/lib/${CLOUD_API2_PLAT}/libwebsockets.so.12")
else()
    message(FATAL_ERROR "websockets: no recipe for ${CLOUD_API2_PLAT}")
endif()

cloud_api2_require_file("${_lws_lib}")

add_library(cloud_api2_websockets SHARED IMPORTED GLOBAL)
set_target_properties(cloud_api2_websockets PROPERTIES
    IMPORTED_LOCATION "${_lws_lib}"
    INTERFACE_INCLUDE_DIRECTORIES "${_lws_root}/include"
)
if(CLOUD_API2_OS STREQUAL "linux")
    find_package(Threads REQUIRED)
    target_link_libraries(cloud_api2_websockets INTERFACE Threads::Threads ${CMAKE_DL_LIBS})
endif()
target_link_libraries(cloud_api2_websockets INTERFACE cloud_api2::openssl)
target_compile_definitions(cloud_api2_websockets INTERFACE ENABLE_LIBWEBSOCKETS)
cloud_api2_alias(cloud_api2::websockets cloud_api2_websockets)
