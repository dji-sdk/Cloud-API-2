file(STRINGS "${CLOUD_API2_ROOT}/VERSION" _ver LIMIT_COUNT 1)
string(STRIP "${_ver}" _ver)
string(REPLACE "." ";" _ver_list "${_ver}")
list(LENGTH _ver_list _ver_n)
list(GET _ver_list 0 FLIGHT_HUB_VERSION_MAJOR)
list(GET _ver_list 1 FLIGHT_HUB_VERSION_MINOR)
list(GET _ver_list 2 FLIGHT_HUB_VERSION_ALTER)
if(_ver_n GREATER 3)
    list(GET _ver_list 3 FLIGHT_HUB_VERSION_TWEAK)
else()
    set(FLIGHT_HUB_VERSION_TWEAK 0)
endif()

if(NOT DEFINED FH_SDK_BUILD_VERSION)
    set(FH_SDK_BUILD_VERSION -1)
endif()
set(FLIGHT_HUB_BUILD_VERSION ${FH_SDK_BUILD_VERSION})

string(TIMESTAMP FLIGHT_HUB_BUILD_DATE "%Y-%m-%d")
string(TIMESTAMP FLIGHT_HUB_BUILD_TIME "%H:%M:%S")
set(FLIGHT_HUB_BUILD_PLAT "${CMAKE_SYSTEM_NAME}")
set(FLIGHT_HUB_BUILD_ARCH "${CMAKE_SYSTEM_PROCESSOR}")
set(FLIGHT_HUB_BUILD_COMPILER "${CMAKE_CXX_COMPILER_ID} ${CMAKE_CXX_COMPILER_VERSION}")

set(FLIGHT_HUB_GIT_TAG "")
set(FLIGHT_HUB_GIT_BRANCH "")
set(FLIGHT_HUB_GIT_COMMIT_ID "")
find_package(Git QUIET)
if(GIT_FOUND)
    execute_process(COMMAND ${GIT_EXECUTABLE} describe --tags
        WORKING_DIRECTORY "${CLOUD_API2_ROOT}"
        OUTPUT_VARIABLE FLIGHT_HUB_GIT_TAG OUTPUT_STRIP_TRAILING_WHITESPACE ERROR_QUIET)
    execute_process(COMMAND ${GIT_EXECUTABLE} symbolic-ref --short -q HEAD
        WORKING_DIRECTORY "${CLOUD_API2_ROOT}"
        OUTPUT_VARIABLE FLIGHT_HUB_GIT_BRANCH OUTPUT_STRIP_TRAILING_WHITESPACE ERROR_QUIET)
    execute_process(COMMAND ${GIT_EXECUTABLE} log -1 --pretty=format:%H
        WORKING_DIRECTORY "${CLOUD_API2_ROOT}"
        OUTPUT_VARIABLE FLIGHT_HUB_GIT_COMMIT_ID OUTPUT_STRIP_TRAILING_WHITESPACE ERROR_QUIET)
endif()

configure_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/version.h.in"
    "${CMAKE_CURRENT_SOURCE_DIR}/version.h"
    @ONLY
)

file(GLOB_RECURSE FH_SDK_SRC CONFIGURE_DEPENDS
    "${CMAKE_CURRENT_SOURCE_DIR}/bll/*.cpp"
)

add_library(cloud_api2_flight_hub_sdk STATIC ${FH_SDK_SRC})
set_target_properties(cloud_api2_flight_hub_sdk PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    OUTPUT_NAME flight_hub_sdk
)
add_library(cloud_api2::flight_hub_sdk ALIAS cloud_api2_flight_hub_sdk)

file(GLOB _fh_priv_inc LIST_DIRECTORIES true
    "${CMAKE_CURRENT_SOURCE_DIR}/bll/*"
    "${CMAKE_CURRENT_SOURCE_DIR}/bll/module/*"
)
set(_fh_priv_dirs)
foreach(_p ${_fh_priv_inc})
    if(IS_DIRECTORY "${_p}")
        list(APPEND _fh_priv_dirs "${_p}")
    endif()
endforeach()

target_include_directories(cloud_api2_flight_hub_sdk
    PUBLIC
        "${CMAKE_CURRENT_SOURCE_DIR}"
        "${CMAKE_CURRENT_SOURCE_DIR}/api"
    PRIVATE
        "${CMAKE_CURRENT_SOURCE_DIR}/bll"
        "${CLOUD_API2_ROOT}/deps/notification_center_sdk/src"
        ${_fh_priv_dirs}
)

target_link_libraries(cloud_api2_flight_hub_sdk
    PUBLIC
        cloud_api2::base
        cloud_api2::message_engine
        cloud_api2::notification_center
    PRIVATE
        cloud_api2::aigcjson
        cloud_api2::h264_analyze
        cloud_api2::openssl
        cloud_api2::aws_s3
        cloud_api2::blocking_detector
        ${CMAKE_DL_LIBS}
)
