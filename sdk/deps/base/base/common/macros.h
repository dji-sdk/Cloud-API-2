// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <functional>
#include <utility>

namespace drc_sdk {

#define DISALLOW_COPY_AND_ASSIGN(TypeName) \
    TypeName(const TypeName&&) = delete; \
    TypeName(const TypeName&) = delete; \
    void operator=(const TypeName&&) = delete; \
    void operator=(const TypeName&) = delete;

#if __WIN32__
#define PATH_SPLITTER '\\'
#else
#define PATH_SPLITTER '/'
#endif

#undef UNIQUE_PARAM_NAME
#undef UNIQUE_PARAM_NAME_
#undef UNIQUE_PARAM_NAME__
#define UNIQUE_PARAM_NAME__(post) var__##post
#define UNIQUE_PARAM_NAME_(post) UNIQUE_PARAM_NAME__(post)
#define UNIQUE_PARAM_NAME UNIQUE_PARAM_NAME_(__LINE__)

#ifdef SYNCHRONIZED
#undef SYNCHRONIZED
#endif
#define SYNCHRONIZED(mtx) std::lock_guard<decltype(mtx)> UNIQUE_PARAM_NAME(mtx)

template<typename T> using SP = std::shared_ptr<T>;

#ifdef MK_SP
#undef MK_SP
#endif
#define MK_SP std::make_shared

template<typename T> using UP = std::unique_ptr<T>;

#ifdef MK_UP
#undef MK_UP
#endif
#define MK_UP std::make_unique

using RunnableFunc = std::function<void()>;
}  // namespace drc_sdk

#ifdef __WIN32__
#ifdef UNICODE
typedef std::wstring TSTRING;
#else
typedef std::string TSTRING;
#endif
#else
typedef std::string TSTRING;
typedef char TCHAR;
#define TEXT(data) data
#define _tfopen fopen
#endif
