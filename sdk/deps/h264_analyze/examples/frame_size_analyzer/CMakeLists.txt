# Frame Size Analyzer CMakeLists.txt
add_executable(frame_size_analyzer test_frame_size.cpp)

# 链接H264Parser库
target_link_libraries(frame_size_analyzer h264_parser_lib)

# 设置包含目录
target_include_directories(frame_size_analyzer PRIVATE ${CMAKE_SOURCE_DIR}/lib)
