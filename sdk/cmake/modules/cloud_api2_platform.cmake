# OS + ARCH 平台键。三方预编译路径只走 ${CLOUD_API2_OS}/${CLOUD_API2_ARCH}，禁止写死本机字面量。

if(NOT DEFINED CLOUD_API2_ROOT)
    message(FATAL_ERROR "CLOUD_API2_ROOT must be set before including cloud_api2_platform.cmake")
endif()

set(CLOUD_API2_THIRD_PARTY "${CLOUD_API2_ROOT}/third_party")
set(CLOUD_API2_DEPS "${CLOUD_API2_ROOT}/deps")

if(NOT CLOUD_API2_OS)
    if(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
        set(CLOUD_API2_OS macosx)
    elseif(CMAKE_SYSTEM_NAME STREQUAL "Linux")
        set(CLOUD_API2_OS linux)
    else()
        message(FATAL_ERROR "Unsupported CMAKE_SYSTEM_NAME=${CMAKE_SYSTEM_NAME}")
    endif()
endif()

if(NOT CLOUD_API2_ARCH)
    string(TOLOWER "${CMAKE_SYSTEM_PROCESSOR}" _proc)
    if(_proc MATCHES "arm64|aarch64")
        if(CLOUD_API2_OS STREQUAL "linux")
            set(CLOUD_API2_ARCH aarch64)
        else()
            set(CLOUD_API2_ARCH arm64)
        endif()
    elseif(_proc MATCHES "x86_64|amd64")
        set(CLOUD_API2_ARCH x86_64)
    else()
        message(FATAL_ERROR "Unsupported CMAKE_SYSTEM_PROCESSOR=${CMAKE_SYSTEM_PROCESSOR}")
    endif()
endif()

set(CLOUD_API2_PLAT "${CLOUD_API2_OS}/${CLOUD_API2_ARCH}")
set(CLOUD_API2_JAVA_PLAT "${CLOUD_API2_OS}-${CLOUD_API2_ARCH}")

if(NOT ((CLOUD_API2_OS STREQUAL "linux" AND CLOUD_API2_ARCH STREQUAL "x86_64")
        OR (CLOUD_API2_OS STREQUAL "macosx" AND CLOUD_API2_ARCH STREQUAL "arm64")))
    message(FATAL_ERROR
        "Unsupported platform ${CLOUD_API2_PLAT}. Buildable keys: "
        "linux/x86_64 (official delivery), macosx/arm64 (local dev only)")
endif()

message(STATUS "cloud_api2 platform: ${CLOUD_API2_PLAT}")

function(cloud_api2_require_file path)
    if(NOT EXISTS "${path}")
        message(FATAL_ERROR
            "Missing prebuilt for ${CLOUD_API2_PLAT}:\n  ${path}\n"
            "Add the OS/ARCH assets or pick a supported CMake preset.")
    endif()
endfunction()

function(cloud_api2_alias alias target)
    if(NOT TARGET ${alias})
        add_library(${alias} ALIAS ${target})
    endif()
endfunction()
