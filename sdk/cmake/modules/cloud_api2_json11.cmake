# Dropbox json11：源码进仓，跟 SDK 一起编。

add_library(cloud_api2_json11 STATIC
    "${CLOUD_API2_THIRD_PARTY}/json11/src/json11.cpp"
)
set_target_properties(cloud_api2_json11 PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    OUTPUT_NAME json11
)
target_include_directories(cloud_api2_json11 PUBLIC
    "${CLOUD_API2_THIRD_PARTY}/json11/include"
)
target_compile_features(cloud_api2_json11 PUBLIC cxx_std_11)
cloud_api2_alias(cloud_api2::json11 cloud_api2_json11)
