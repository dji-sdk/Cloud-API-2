option(ENABLE_CCACHE "Enable ccache if installed" ON)
option(ASAN_ENABLE "AddressSanitizer (debug)" OFF)
option(FH_SDK_DEBUG_SYMBOLS "Debug symbols + frame pointers" ON)
option(ENABLE_JAVA "Build JNI / SWIG Java binding" ON)

if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release CACHE STRING "Debug or Release" FORCE)
endif()

if(ENABLE_CCACHE)
    find_program(CCACHE_PROGRAM ccache)
    if(CCACHE_PROGRAM)
        set(CMAKE_C_COMPILER_LAUNCHER "${CCACHE_PROGRAM}")
        set(CMAKE_CXX_COMPILER_LAUNCHER "${CCACHE_PROGRAM}")
        message(STATUS "ccache: ${CCACHE_PROGRAM}")
    else()
        message(STATUS "ccache not found; skip")
    endif()
endif()

if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang|AppleClang")
    add_compile_options(-Werror=return-type)
    # 把 CI / 本机源码绝对路径从 DWARF 里映射掉，避免 native 带上 /home/jenkins、/root/build。
    add_compile_options(
        "-ffile-prefix-map=/home/jenkins=."
        "-ffile-prefix-map=/root/build=."
        "-ffile-prefix-map=${CMAKE_SOURCE_DIR}=."
        "-ffile-prefix-map=${CLOUD_API2_ROOT}=."
        "-ffile-prefix-map=${CLOUD_API2_THIRD_PARTY}=./third_party"
    )
endif()

if(ASAN_ENABLE)
    add_compile_options(-fsanitize=address -fno-omit-frame-pointer)
    add_link_options(-fsanitize=address)
endif()

if(FH_SDK_DEBUG_SYMBOLS)
    if(MSVC)
        add_compile_options($<$<COMPILE_LANGUAGE:CXX>:/Zi>)
    elseif(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang|AppleClang")
        add_compile_options(-g -fno-omit-frame-pointer)
    endif()
endif()
