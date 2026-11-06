if(NOT CMAKE_SYSTEM_NAME STREQUAL "Linux" OR NOT CLOUD_API2_ARCH STREQUAL "x86_64")
    message(FATAL_ERROR "C# SDK delivery supports Linux x64 only")
endif()
find_package(SWIG 4.3 REQUIRED)
include(UseSWIG)
set(CMAKE_SWIG_OUTDIR "${CMAKE_BINARY_DIR}/generated")
file(MAKE_DIRECTORY "${CMAKE_SWIG_OUTDIR}")
set(_interface "${CMAKE_CURRENT_SOURCE_DIR}/swig/flight_hub_sdk.i")
set_source_files_properties("${_interface}" PROPERTIES CPLUSPLUS ON
    SWIG_MODULE_NAME flight_hub_sdk_native USE_TARGET_INCLUDE_DIRECTORIES TRUE)
set(CMAKE_SWIG_FLAGS -module flight_hub_sdk_native -namespace FlightHubSdk -dllimport flight_hub_sdk_native)
file(GLOB_RECURSE _api_deps CONFIGURE_DEPENDS "${CLOUD_API2_ROOT}/src/api/*.h")
set_property(SOURCE "${_interface}" PROPERTY DEPENDS
    "${CLOUD_API2_ROOT}/platform/java/swig/flight_hub_sdk.i" ${_api_deps})
swig_add_library(flight_hub_sdk_native TYPE SHARED LANGUAGE csharp SOURCES "${_interface}")
set_property(TARGET flight_hub_sdk_native PROPERTY SWIG_USE_TARGET_INCLUDE_DIRECTORIES TRUE)
target_link_libraries(flight_hub_sdk_native PRIVATE cloud_api2::flight_hub_sdk)
set_target_properties(flight_hub_sdk_native PROPERTIES
    PREFIX "lib"
    OUTPUT_NAME flight_hub_sdk_native
    LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/native"
    BUILD_WITH_INSTALL_RPATH TRUE INSTALL_RPATH "$ORIGIN"
    INSTALL_RPATH_USE_LINK_PATH FALSE)
target_link_options(flight_hub_sdk_native PRIVATE "-Wl,--no-undefined")

add_executable(cloud_api2_csharp_shutdown_smoke "${CMAKE_CURRENT_SOURCE_DIR}/smoke/shutdown_order.cpp")
target_link_libraries(cloud_api2_csharp_shutdown_smoke PRIVATE cloud_api2::base Threads::Threads)
set_target_properties(cloud_api2_csharp_shutdown_smoke PROPERTIES RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")
