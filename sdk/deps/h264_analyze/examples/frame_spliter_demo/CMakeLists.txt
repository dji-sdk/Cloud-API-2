# Frame Splitter Demo CMakeLists.txt
cmake_minimum_required(VERSION 3.10)

# 创建frame_spliter_demo可执行文件
add_executable(frame_spliter_demo frame_spliter_demo.cpp)

# 链接H264Parser库
target_link_libraries(frame_spliter_demo h264_parser_lib)

# 设置包含目录
target_include_directories(frame_spliter_demo PRIVATE 
    ${CMAKE_SOURCE_DIR}/lib/h264_parser
)

# 设置输出目录
set_target_properties(frame_spliter_demo PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
)
