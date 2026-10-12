# 示例默认不编；单独调试时用 -DBUILD_BLOCKING_DETECTOR_DEMO=ON。
set(BUILD_BLOCKING_DETECTOR_DEMO OFF CACHE BOOL "" FORCE)
add_subdirectory(
    "${CLOUD_API2_DEPS}/thread_blocking_detector"
    "${CMAKE_BINARY_DIR}/_blocking_detector"
)
cloud_api2_alias(cloud_api2::blocking_detector blocking_detector)
