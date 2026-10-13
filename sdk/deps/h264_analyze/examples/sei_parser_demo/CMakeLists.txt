# SEI Parser Demo CMakeLists.txt
add_executable(sei_parser_demo sei_parser_demo.cpp)

# 链接H264Parser库
target_link_libraries(sei_parser_demo h264_parser_lib)

# 设置包含目录
target_include_directories(sei_parser_demo PRIVATE ${CMAKE_SOURCE_DIR}/lib)
