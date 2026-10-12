# H264Parser Library CMakeLists.txt
add_library(h264_parser_lib STATIC h264_parser.cpp)

# 设置包含目录
target_include_directories(h264_parser_lib PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

# 设置C++标准
target_compile_features(h264_parser_lib PUBLIC cxx_std_17)

# WebAssembly 编译支持
if(EMSCRIPTEN)
    target_compile_options(h264_parser_lib PRIVATE
        "-pthread"
        "-matomics"
        "-mbulk-memory"
    )
endif()
