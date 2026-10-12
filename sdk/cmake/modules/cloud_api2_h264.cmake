set(BUILD_H264_ANALYZE_EXAMPLES OFF CACHE BOOL "" FORCE)
add_subdirectory("${CLOUD_API2_DEPS}/h264_analyze" "${CMAKE_BINARY_DIR}/_h264_analyze")

set_target_properties(h264_parser_lib h264_custom_sei_codec_lib PROPERTIES
    POSITION_INDEPENDENT_CODE ON
)

add_library(cloud_api2_h264 INTERFACE)
target_link_libraries(cloud_api2_h264 INTERFACE h264_parser_lib h264_custom_sei_codec_lib)
target_compile_definitions(cloud_api2_h264 INTERFACE BUILD_WITH_H264_ANALYZE)
cloud_api2_alias(cloud_api2::h264_analyze cloud_api2_h264)
