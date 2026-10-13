cmake_minimum_required(VERSION 3.10)
project(mqtt_demo)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra")

# 相对本 demo：../.. = paho_mqtt_c；../../.. = third_party
get_filename_component(PAHO_MQTT_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../.." ABSOLUTE)
get_filename_component(THIRD_PARTY_ROOT "${PAHO_MQTT_ROOT_DIR}/.." ABSOLUTE)
set(PAHO_MQTT_INCLUDE_DIR "${PAHO_MQTT_ROOT_DIR}/include")

if(APPLE)
    set(PAHO_MQTT_LIB_DIR "${PAHO_MQTT_ROOT_DIR}/lib/macosx/arm64")
    set(PAHO_SHLIB "${PAHO_MQTT_LIB_DIR}/libpaho-mqtt3as.1.dylib")
    find_package(OpenSSL REQUIRED)
    set(_ssl_libs OpenSSL::SSL OpenSSL::Crypto)
    # paho dylib 的 OpenSSL 是 @rpath/libssl.3.dylib，需把 Homebrew openssl 目录放进 rpath
    get_filename_component(_openssl_lib_dir "${OPENSSL_SSL_LIBRARY}" DIRECTORY)
    set(_rpath "${PAHO_MQTT_LIB_DIR};${_openssl_lib_dir}")
else()
    set(PAHO_MQTT_LIB_DIR "${PAHO_MQTT_ROOT_DIR}/lib/linux/x86_64")
    set(PAHO_SHLIB "${PAHO_MQTT_LIB_DIR}/libpaho-mqtt3as.so.1")
    set(OPENSSL_ROOT "${THIRD_PARTY_ROOT}/openssl_1_1_1")
    set(OPENSSL_INCLUDE_DIR "${OPENSSL_ROOT}/include")
    set(OPENSSL_SSL_LIBRARY "${OPENSSL_ROOT}/lib/linux/x86_64/libssl.so.1.1")
    set(OPENSSL_CRYPTO_LIBRARY "${OPENSSL_ROOT}/lib/linux/x86_64/libcrypto.so.1.1")
    if(NOT EXISTS "${OPENSSL_SSL_LIBRARY}" OR NOT EXISTS "${OPENSSL_CRYPTO_LIBRARY}")
        message(FATAL_ERROR "仓内 OpenSSL 1.1.1 不存在: ${OPENSSL_ROOT}/lib/linux/x86_64/")
    endif()
    set(_ssl_libs "${OPENSSL_SSL_LIBRARY}" "${OPENSSL_CRYPTO_LIBRARY}")
    set(_rpath "${PAHO_MQTT_LIB_DIR};${OPENSSL_ROOT}/lib/linux/x86_64")
endif()

if(NOT EXISTS "${PAHO_MQTT_INCLUDE_DIR}/MQTTAsync.h")
    message(FATAL_ERROR "未找到 paho 头: ${PAHO_MQTT_INCLUDE_DIR}/MQTTAsync.h")
endif()
if(NOT EXISTS "${PAHO_SHLIB}")
    message(FATAL_ERROR "未找到 paho 动态库: ${PAHO_SHLIB}")
endif()

add_executable(mqtt_demo mqtt_demo.cpp)
target_include_directories(mqtt_demo PRIVATE "${PAHO_MQTT_INCLUDE_DIR}")
if(NOT APPLE)
    target_include_directories(mqtt_demo PRIVATE "${OPENSSL_INCLUDE_DIR}")
endif()
target_link_libraries(mqtt_demo PRIVATE "${PAHO_SHLIB}" ${_ssl_libs} pthread dl)
set_target_properties(mqtt_demo PROPERTIES
    BUILD_RPATH "${_rpath}"
    INSTALL_RPATH "${PAHO_MQTT_LIB_DIR}"
)

message(STATUS "PAHO_MQTT_ROOT_DIR: ${PAHO_MQTT_ROOT_DIR}")
message(STATUS "PAHO_MQTT_LIB_DIR: ${PAHO_MQTT_LIB_DIR}")
message(STATUS "PAHO_SHLIB: ${PAHO_SHLIB}")
