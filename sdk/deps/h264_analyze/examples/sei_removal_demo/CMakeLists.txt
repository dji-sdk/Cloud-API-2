# SEI Removal Demo CMakeLists.txt
add_executable(sei_removal_demo sei_removal_demo.cpp)

# 链接H264Parser库
target_link_libraries(sei_removal_demo h264_parser_lib)

# 设置包含目录
target_include_directories(sei_removal_demo PRIVATE ${CMAKE_SOURCE_DIR}/lib)
