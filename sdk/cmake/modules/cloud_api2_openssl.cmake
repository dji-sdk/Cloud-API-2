# macOS: 系统 / Homebrew OpenSSL。Linux: 仓内 1.1.1 动态库。

if(CLOUD_API2_OS STREQUAL "macosx")
    find_package(OpenSSL REQUIRED)
    add_library(cloud_api2_openssl INTERFACE)
    target_link_libraries(cloud_api2_openssl INTERFACE OpenSSL::SSL OpenSSL::Crypto)
    get_filename_component(_ssl_libdir "${OPENSSL_SSL_LIBRARY}" DIRECTORY)
    if(_ssl_libdir)
        target_link_directories(cloud_api2_openssl INTERFACE "${_ssl_libdir}")
    endif()
    message(STATUS "openssl: system ${OPENSSL_VERSION} (${OPENSSL_SSL_LIBRARY})")
elseif(CLOUD_API2_OS STREQUAL "linux")
    set(_ssl_dir "${CLOUD_API2_THIRD_PARTY}/openssl_1_1_1")
    set(_ssl_lib "${_ssl_dir}/lib/${CLOUD_API2_PLAT}")
    cloud_api2_require_file("${_ssl_lib}/libssl.so.1.1")
    cloud_api2_require_file("${_ssl_lib}/libcrypto.so.1.1")

    add_library(cloud_api2_ssl SHARED IMPORTED GLOBAL)
    set_target_properties(cloud_api2_ssl PROPERTIES
        IMPORTED_LOCATION "${_ssl_lib}/libssl.so.1.1"
        INTERFACE_INCLUDE_DIRECTORIES "${_ssl_dir}/include"
    )
    add_library(cloud_api2_crypto SHARED IMPORTED GLOBAL)
    set_target_properties(cloud_api2_crypto PROPERTIES
        IMPORTED_LOCATION "${_ssl_lib}/libcrypto.so.1.1"
        INTERFACE_INCLUDE_DIRECTORIES "${_ssl_dir}/include"
    )
    add_library(cloud_api2_openssl INTERFACE)
    target_link_libraries(cloud_api2_openssl INTERFACE cloud_api2_ssl cloud_api2_crypto)
    target_include_directories(cloud_api2_openssl INTERFACE "${_ssl_dir}/include")
    message(STATUS "openssl: bundled 1.1.1 ${_ssl_lib}")
else()
    message(FATAL_ERROR "openssl: no recipe for ${CLOUD_API2_PLAT}")
endif()

target_compile_definitions(cloud_api2_openssl INTERFACE ENABLE_OPENSSL)
cloud_api2_alias(cloud_api2::openssl cloud_api2_openssl)
