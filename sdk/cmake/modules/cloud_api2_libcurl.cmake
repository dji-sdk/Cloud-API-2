set(_curl_root "${CLOUD_API2_THIRD_PARTY}/libcurl")
set(_curl_inc "${_curl_root}/include")
if(CLOUD_API2_OS STREQUAL "macosx")
    set(_curl_lib "${_curl_root}/lib/macosx/${CLOUD_API2_ARCH}/libcurl.a")
elseif(CLOUD_API2_OS STREQUAL "linux")
    set(_curl_lib "${_curl_root}/lib/linux/${CLOUD_API2_ARCH}/libcurl.a")
else()
    message(FATAL_ERROR "libcurl: no recipe for ${CLOUD_API2_PLAT}")
endif()

cloud_api2_require_file("${_curl_lib}")
if(NOT EXISTS "${_curl_inc}")
    message(FATAL_ERROR "libcurl headers missing: ${_curl_inc}")
endif()

add_library(cloud_api2_libcurl STATIC IMPORTED GLOBAL)
set_target_properties(cloud_api2_libcurl PROPERTIES
    IMPORTED_LOCATION "${_curl_lib}"
    INTERFACE_INCLUDE_DIRECTORIES "${_curl_inc}"
)
target_link_libraries(cloud_api2_libcurl INTERFACE cloud_api2::openssl)
cloud_api2_alias(cloud_api2::libcurl cloud_api2_libcurl)
