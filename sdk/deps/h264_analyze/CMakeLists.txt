cmake_minimum_required(VERSION 3.10)
project(H264Analyzer)

# 设置C++标准
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 设置编译选项
if(CMAKE_CXX_COMPILER_ID STREQUAL "GNU" OR CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -O2")
endif()

# 设置输出目录
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

# 添加H264Parser库
add_subdirectory(lib/h264_parser)
add_subdirectory(lib/custom_sei_codec)

# 选项：是否构建示例程序（默认关闭）
option(BUILD_H264_ANALYZE_EXAMPLES "Build H264 analyzer example programs" OFF)

# 添加示例程序（仅在启用时构建）
if(BUILD_H264_ANALYZE_EXAMPLES)
    add_subdirectory(examples/sei_parser_demo)
    add_subdirectory(examples/frame_size_analyzer)
    add_subdirectory(examples/sei_removal_demo)
    add_subdirectory(examples/custom_sei_codec_demo)
    add_subdirectory(examples/frame_spliter_demo)
    add_subdirectory(examples/emulation_prevention_bytes)

    # 可选：添加一个构建所有示例的目标
    add_custom_target(build_all_examples
        DEPENDS
        sei_parser_demo
        frame_size_analyzer
        sei_removal_demo
        custom_sei_codec_demo
        frame_spliter_demo
        emulation_prevention_bytes_operator
        
        COMMENT "Building all example programs"
    )
endif()