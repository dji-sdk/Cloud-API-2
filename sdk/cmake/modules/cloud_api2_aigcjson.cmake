# 头文件库；不走上游 CMakeLists（避免默认开 test）。

add_library(cloud_api2_aigcjson INTERFACE)
target_include_directories(cloud_api2_aigcjson INTERFACE
    "${CLOUD_API2_THIRD_PARTY}/AIGCJson/include"
)
target_compile_definitions(cloud_api2_aigcjson INTERFACE AIGCJSON_FEATURE_OPTIONAL)
target_compile_features(cloud_api2_aigcjson INTERFACE cxx_std_17)
cloud_api2_alias(cloud_api2::aigcjson cloud_api2_aigcjson)
