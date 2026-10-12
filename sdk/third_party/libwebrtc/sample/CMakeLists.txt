cmake_minimum_required(VERSION 3.10)
project(whep_pull_project VERSION 1.0.0 LANGUAGES CXX)

# 只链仓内 include/ + lib/<本机>/libwebrtc.a + OpenSSL。
# HTTP 用系统 libcurl（仓内 curl 头是扁平 curl.h，不是 <curl/curl.h>）。

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_BUILD_TYPE Release)

set(WEBRTC_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/..")
set(SDK_ROOT "${WEBRTC_ROOT}/../..")

if(APPLE)
    set(WEBRTC_LIB "${WEBRTC_ROOT}/lib/macosx/arm64/libwebrtc.a")
    execute_process(
        COMMAND brew --prefix openssl@3
        OUTPUT_VARIABLE OPENSSL_ROOT
        OUTPUT_STRIP_TRAILING_WHITESPACE
        ERROR_QUIET
    )
    if(NOT OPENSSL_ROOT)
        set(OPENSSL_ROOT "/opt/homebrew/opt/openssl@3")
    endif()
    set(OPENSSL_INCLUDE_DIR "${OPENSSL_ROOT}/include")
    set(OPENSSL_LIB_DIR "${OPENSSL_ROOT}/lib")
    if(NOT EXISTS "${OPENSSL_INCLUDE_DIR}")
        message(FATAL_ERROR "OpenSSL 头不存在: ${OPENSSL_INCLUDE_DIR}\n请 brew install openssl@3")
    endif()
else()
    set(WEBRTC_LIB "${WEBRTC_ROOT}/lib/linux/x86_64/libwebrtc.a")
    set(OPENSSL_ROOT "${SDK_ROOT}/third_party/openssl_1_1_1")
    set(OPENSSL_INCLUDE_DIR "${OPENSSL_ROOT}/include")
    set(OPENSSL_LIB_DIR "${OPENSSL_ROOT}/lib/linux/x86_64")
    if(NOT EXISTS "${OPENSSL_INCLUDE_DIR}")
        message(FATAL_ERROR "OpenSSL 头不存在: ${OPENSSL_INCLUDE_DIR}")
    endif()
endif()

if(NOT EXISTS "${WEBRTC_LIB}")
    message(FATAL_ERROR "未找到 ${WEBRTC_LIB}")
endif()

find_package(CURL REQUIRED)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")

if(APPLE)
    add_compile_definitions(
        WEBRTC_MAC=1
        WEBRTC_POSIX=1
        WEBRTC_ARCH_ARM64=1
        WEBRTC_USE_H264=1
        WEBRTC_ENABLE_PROTOBUF=1
        WEBRTC_STRICT_FIELD_TRIALS=0
        WEBRTC_INCLUDE_INTERNAL_AUDIO_DEVICE=1
        RTC_ENABLE_VP9=1
        RTC_DAV1D_IN_INTERNAL_DECODER_FACTORY=1
        WEBRTC_HAVE_SCTP=1
        WEBRTC_ENABLE_LIBEVENT=1
        WEBRTC_LIBRARY_IMPL=1
        WEBRTC_NON_STATIC_TRACE_EVENT_HANDLERS=0
        ABSL_ALLOCATOR_NOTHROW=1
    )
else()
    add_compile_definitions(
        WEBRTC_LINUX=1
        WEBRTC_POSIX=1
        WEBRTC_ARCH_X86_64=1
        WEBRTC_USE_H264=1
        WEBRTC_ENABLE_PROTOBUF=1
        WEBRTC_STRICT_FIELD_TRIALS=0
        WEBRTC_INCLUDE_INTERNAL_AUDIO_DEVICE=1
        RTC_ENABLE_VP9=1
        RTC_DAV1D_IN_INTERNAL_DECODER_FACTORY=1
        WEBRTC_HAVE_SCTP=1
        WEBRTC_ENABLE_LIBEVENT=1
        WEBRTC_LIBRARY_IMPL=1
        WEBRTC_ENABLE_AVX2=1
        WEBRTC_NON_STATIC_TRACE_EVENT_HANDLERS=0
        ABSL_ALLOCATOR_NOTHROW=1
    )
endif()

add_executable(whep_pull_origin_stream whep_pull_origin_stream.cpp)

set(RTC_INC "${WEBRTC_ROOT}/include")
target_include_directories(whep_pull_origin_stream PRIVATE
    "${RTC_INC}"
    "${RTC_INC}/api"
    "${RTC_INC}/rtc_base"
    "${RTC_INC}/third_party/abseil-cpp"
    ${OPENSSL_INCLUDE_DIR}
    ${CURL_INCLUDE_DIRS}
)

add_library(webrtc STATIC IMPORTED)
set_target_properties(webrtc PROPERTIES IMPORTED_LOCATION "${WEBRTC_LIB}")

set(COMMON_LIBS webrtc pthread m ssl crypto ${CURL_LIBRARIES})

if(APPLE)
    find_library(COCOA_LIB Cocoa)
    find_library(FOUNDATION_LIB Foundation)
    find_library(COREFOUNDATION_LIB CoreFoundation)
    find_library(COREGRAPHICS_LIB CoreGraphics)
    find_library(COREVIDEO_LIB CoreVideo)
    find_library(AUDIOTOOLBOX_LIB AudioToolbox)
    find_library(AVFOUNDATION_LIB AVFoundation)
    find_library(SECURITY_LIB Security)
    find_library(SYSTEMCONFIGURATION_LIB SystemConfiguration)
    find_library(COREMEDIA_LIB CoreMedia)
    find_library(COREAUDIO_LIB CoreAudio)
    find_library(VIDEOTOOLBOX_LIB VideoToolbox)
    find_library(QUARTZCORE_LIB QuartzCore)
    find_library(IOKIT_LIB IOKit)
    target_link_libraries(whep_pull_origin_stream
        ${COMMON_LIBS}
        ${COCOA_LIB}
        ${FOUNDATION_LIB}
        ${COREFOUNDATION_LIB}
        ${COREGRAPHICS_LIB}
        ${COREVIDEO_LIB}
        ${AUDIOTOOLBOX_LIB}
        ${AVFOUNDATION_LIB}
        ${SECURITY_LIB}
        ${SYSTEMCONFIGURATION_LIB}
        ${COREMEDIA_LIB}
        ${COREAUDIO_LIB}
        ${VIDEOTOOLBOX_LIB}
        ${QUARTZCORE_LIB}
        ${IOKIT_LIB}
    )
else()
    target_link_libraries(whep_pull_origin_stream ${COMMON_LIBS} rt X11 asound dl jpeg)
endif()

target_link_directories(whep_pull_origin_stream PRIVATE ${OPENSSL_LIB_DIR})

message(STATUS "WEBRTC_LIB: ${WEBRTC_LIB}")
message(STATUS "OpenSSL: ${OPENSSL_INCLUDE_DIR}")
