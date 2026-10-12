# AIGCJson 测试可执行文件（每个 .cpp 一个 main，独立可执行）
set(AIGCJSON_TESTS
  1base
  2rename
  3stl
  4nested
  5inheritance
  6defaultValue
  8numeric
)

# optional 测试（需启用 AIGCJSON_FEATURE_OPTIONAL）
if(AIGCJSON_FEATURE_OPTIONAL)
  list(APPEND AIGCJSON_TESTS 7optional 9all_optional)
endif()

foreach(name IN LISTS AIGCJSON_TESTS)
  add_executable(${name} ${name}.cpp)
  target_link_libraries(${name} PRIVATE AIGCJson)
  add_test(NAME ${name} COMMAND ${name})
endforeach()

# 根目录的 test.cpp 作为综合示例（可选编译）
if(EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/../test.cpp")
  add_executable(test_main ../test.cpp)
  target_link_libraries(test_main PRIVATE AIGCJson)
  add_test(NAME test_main COMMAND test_main)
endif()
