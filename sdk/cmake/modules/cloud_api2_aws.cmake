set(_aws_dir "${CLOUD_API2_THIRD_PARTY}/aws-cpp-sdk-s3/lib/${CLOUD_API2_PLAT}")
if(NOT EXISTS "${_aws_dir}")
    message(FATAL_ERROR "AWS SDK missing: ${_aws_dir}")
endif()

set(_aws_libs
    aws-cpp-sdk-s3
    aws-cpp-sdk-core
    aws-crt-cpp
    aws-c-s3
    aws-c-auth
    aws-c-http
    aws-c-io
    aws-c-cal
    aws-c-compression
    aws-c-event-stream
    aws-c-mqtt
    aws-c-sdkutils
    aws-checksums
    aws-c-common
)

add_library(cloud_api2_aws INTERFACE)
target_include_directories(cloud_api2_aws INTERFACE
    "${_aws_dir}/include"
    "${_aws_dir}/include/aws"
)

set(_aws_files)
foreach(_n ${_aws_libs})
    cloud_api2_require_file("${_aws_dir}/lib${_n}.a")
    list(APPEND _aws_files "${_aws_dir}/lib${_n}.a")
endforeach()

if(CLOUD_API2_OS STREQUAL "linux")
    cloud_api2_require_file("${_aws_dir}/libs2n.a")
    get_target_property(_curl_loc cloud_api2_libcurl IMPORTED_LOCATION)
    target_link_libraries(cloud_api2_aws INTERFACE
        -Wl,--start-group
        ${_aws_files}
        "${_aws_dir}/libs2n.a"
        "${_curl_loc}"
        -Wl,--end-group
        z
        pthread
        dl
    )
elseif(CLOUD_API2_OS STREQUAL "macosx")
    target_link_libraries(cloud_api2_aws INTERFACE
        ${_aws_files}
        z
        "-framework Network"
    )
endif()

target_compile_definitions(cloud_api2_aws INTERFACE
    USE_AWS_SDK
    AWS_SDK_VERSION_MAJOR=1
    AWS_SDK_VERSION_MINOR=11
    AWS_SDK_VERSION_PATCH=642
)
cloud_api2_alias(cloud_api2::aws_s3 cloud_api2_aws)
