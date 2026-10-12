# add_license.cmake — SWIG 生成后给 .java / .cxx / .h 补版权头
# 由 CMakeLists.txt POST_BUILD 以 cmake -P 调用；依赖 -DSWIG_OUTDIR=...

if(NOT DEFINED SWIG_OUTDIR OR SWIG_OUTDIR STREQUAL "")
    message(FATAL_ERROR "add_license.cmake requires -DSWIG_OUTDIR=...")
endif()

set(HEADER
"// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0
")

set(count 0)

# SWIG_OUTDIR：真正参与编译的 generated（.java / wrap.cxx / wrap.h）
# 另扫同目录旁的 platform/java/swig/*_wrap.*（仓内仍跟踪的旧路径副本）
set(_license_globs
    "${SWIG_OUTDIR}/*.java"
    "${SWIG_OUTDIR}/*wrap.cxx"
    "${SWIG_OUTDIR}/*wrap.h"
)
get_filename_component(_swig_dir "${CMAKE_SCRIPT_MODE_FILE}" DIRECTORY)
list(APPEND _license_globs
    "${_swig_dir}/*_wrap.cxx"
    "${_swig_dir}/*_wrap.h"
)

file(GLOB _license_files ${_license_globs})
list(REMOVE_DUPLICATES _license_files)

foreach(file ${_license_files})
    file(READ "${file}" content)
    string(FIND "${content}" "SPDX-License-Identifier" pos)
    if(NOT pos EQUAL -1)
        continue()
    endif()
    file(WRITE "${file}" "${HEADER}\n${content}")
    math(EXPR count "${count} + 1")
endforeach()

message(STATUS "License headers added to ${count} SWIG-generated files")
