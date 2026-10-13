find_package(Java REQUIRED COMPONENTS Development)
find_package(JNI REQUIRED)
find_package(SWIG REQUIRED)
include(UseSWIG)

set(CMAKE_SKIP_BUILD_RPATH FALSE)
set(CMAKE_BUILD_WITH_INSTALL_RPATH TRUE)
set(CMAKE_INSTALL_RPATH_USE_LINK_PATH FALSE)
set(CMAKE_BUILD_RPATH_USE_ORIGIN TRUE)
set(CMAKE_INSTALL_RPATH "$ORIGIN")
set(CMAKE_BUILD_RPATH "$ORIGIN")

set_property(SOURCE "${CMAKE_CURRENT_SOURCE_DIR}/swig/flight_hub_sdk.i" PROPERTY CPLUSPLUS ON)
set_property(SOURCE "${CMAKE_CURRENT_SOURCE_DIR}/swig/flight_hub_sdk.i" PROPERTY SWIG_MODULE_NAME flighthubsdkmodule)

set(CMAKE_SWIG_FLAGS "-c++" "-package" "com.dji.flighthubsdk.generated")
set(CMAKE_SWIG_OUTDIR "${CMAKE_CURRENT_SOURCE_DIR}/flight_hub_sdk/src/main/java/com/dji/flighthubsdk/generated")
file(MAKE_DIRECTORY "${CMAKE_SWIG_OUTDIR}")

swig_add_library(flighthubsdk_jni
    TYPE SHARED
    LANGUAGE java
    SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/swig/flight_hub_sdk.i"
)

target_compile_definitions(flighthubsdk_jni PRIVATE SWIG_JAVA_DETACH_ON_THREAD_END)
set_property(TARGET flighthubsdk_jni PROPERTY SWIG_USE_TARGET_INCLUDE_DIRECTORIES ON)

set_target_properties(flighthubsdk_jni PROPERTIES
    OUTPUT_NAME flighthubsdk_jni
    BUILD_RPATH_USE_ORIGIN TRUE
    INSTALL_RPATH "$ORIGIN"
    BUILD_RPATH "$ORIGIN"
    INSTALL_RPATH_USE_LINK_PATH FALSE
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}"
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}"
)

target_include_directories(flighthubsdk_jni PRIVATE
    ${JNI_INCLUDE_DIRS}
    "${CMAKE_CURRENT_SOURCE_DIR}/swig"
)

target_link_libraries(flighthubsdk_jni
    PRIVATE
        ${JNI_LIBRARIES}
        cloud_api2::flight_hub_sdk
)

if(APPLE)
    set_target_properties(flighthubsdk_jni PROPERTIES SUFFIX ".dylib")
    target_link_libraries(flighthubsdk_jni PRIVATE "-framework CoreFoundation")
endif()

message(STATUS "JNI: ${JNI_INCLUDE_DIRS}")
message(STATUS "SWIG Java out: ${CMAKE_SWIG_OUTDIR}")

# SWIG 生成的 Java 文件不含版权头，构建后自动补 Apache-2.0 声明
add_custom_command(TARGET flighthubsdk_jni POST_BUILD
    COMMAND ${CMAKE_COMMAND}
        -DSWIG_OUTDIR=${CMAKE_SWIG_OUTDIR}
        -P ${CMAKE_CURRENT_SOURCE_DIR}/swig/add_license.cmake
    COMMENT "Adding Apache-2.0 license headers to SWIG-generated Java files"
)
