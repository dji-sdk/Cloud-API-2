set(_rtc_root "${CLOUD_API2_THIRD_PARTY}/libwebrtc")
set(_rtc_inc "${_rtc_root}/include")
set(_rtc_lib "${_rtc_root}/lib/${CLOUD_API2_PLAT}/libwebrtc.a")
cloud_api2_require_file("${_rtc_lib}")

add_library(cloud_api2_libwebrtc_a STATIC IMPORTED GLOBAL)
set_target_properties(cloud_api2_libwebrtc_a PROPERTIES
    IMPORTED_LOCATION "${_rtc_lib}"
)

add_library(cloud_api2_libwebrtc INTERFACE)
target_link_libraries(cloud_api2_libwebrtc INTERFACE cloud_api2_libwebrtc_a)
target_include_directories(cloud_api2_libwebrtc INTERFACE
    "${_rtc_inc}"
    "${_rtc_inc}/rtc_base"
    "${_rtc_inc}/rtc_base/strings"
    "${_rtc_inc}/rtc_base/system"
    "${_rtc_inc}/system_wrappers/include"
    "${_rtc_inc}/third_party/abseil-cpp"
)
target_compile_definitions(cloud_api2_libwebrtc INTERFACE
    BUILD_WITH_LIBWEBRTC
    WEBRTC_POSIX=1
)
if(CLOUD_API2_OS STREQUAL "macosx")
    target_compile_definitions(cloud_api2_libwebrtc INTERFACE WEBRTC_MAC=1)
    target_link_libraries(cloud_api2_libwebrtc INTERFACE
        "-framework CoreFoundation"
        "-framework CoreGraphics"
        "-framework CoreVideo"
        "-framework AudioToolbox"
        "-framework AVFoundation"
        "-framework Foundation"
        "-framework AppKit"
        "-framework Security"
        "-framework SystemConfiguration"
        "-framework CoreMedia"
        "-framework CoreAudio"
        "-framework VideoToolbox"
        "-framework QuartzCore"
        "-framework IOKit"
        "-framework Carbon"
        "-framework Cocoa"
    )
endif()

cloud_api2_alias(cloud_api2::libwebrtc cloud_api2_libwebrtc)
