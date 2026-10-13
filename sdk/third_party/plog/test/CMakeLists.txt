cmake_minimum_required(VERSION 3.20)
project(plog_test)

# 设置 C++ 标准
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 创建测试可执行文件
add_executable(plog_test main.cpp)

# 直接使用 plog 源码目录
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../plog ${CMAKE_CURRENT_BINARY_DIR}/plog)

# 链接 plog 库
target_link_libraries(plog_test plog)

# 设置包含目录
target_include_directories(plog_test PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/../plog/include
)
