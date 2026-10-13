cmake_minimum_required(VERSION 3.15)
project(blocking_detector LANGUAGES CXX)

add_library(blocking_detector STATIC src/blocking_detector.cpp)
set_target_properties(blocking_detector PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    OUTPUT_NAME blocking_detector
)
target_include_directories(blocking_detector PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/src")
target_compile_options(blocking_detector PRIVATE -funwind-tables -fexceptions)
target_compile_features(blocking_detector PUBLIC cxx_std_17)

find_package(Threads REQUIRED)
target_link_libraries(blocking_detector PUBLIC Threads::Threads ${CMAKE_DL_LIBS})

option(BUILD_BLOCKING_DETECTOR_DEMO "Build the blocking detector sample" OFF)
if(BUILD_BLOCKING_DETECTOR_DEMO)
    add_executable(blocking_detector_demo sample/demo.cpp)
    target_link_libraries(blocking_detector_demo PRIVATE blocking_detector)
endif()
