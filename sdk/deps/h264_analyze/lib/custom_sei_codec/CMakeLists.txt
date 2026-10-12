# CustomSEICodec Library CMakeLists.txt
add_library(h264_custom_sei_codec_lib STATIC custom_sei_codec.cpp)

# 设置包含目录
target_include_directories(h264_custom_sei_codec_lib PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

# 设置C++标准
target_compile_features(h264_custom_sei_codec_lib PUBLIC cxx_std_20)

# 设置位置无关代码编译选项，用于创建共享库
set_target_properties(h264_custom_sei_codec_lib PROPERTIES POSITION_INDEPENDENT_CODE ON)

# WebAssembly 编译支持
if(EMSCRIPTEN)
    target_compile_options(h264_custom_sei_codec_lib PRIVATE
        "-pthread"
        "-matomics"
        "-mbulk-memory"
    )
endif()
