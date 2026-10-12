// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <memory>
#include <cstdarg>

// 在包含 plog 头文件之前，先定义宏以避免冲突
#define PLOG_OMIT_LOG_DEFINES

#include <plog/Log.h>
#include <plog/Appenders/RollingFileAppender.h>
#include <plog/Appenders/ConsoleAppender.h>
#include <plog/Appenders/DynamicAppender.h>
#include <plog/Formatters/TxtFormatter.h>
#include <plog/Initializers/RollingFileInitializer.h>

namespace drc_sdk {
enum class LogLevel : int {
    kNone = 0,
    kDebug = 1,
    kInfo = 2,
    kWarning = 3,
    kError = 4,
    kFatal = 5,
};

class PlogLogger {
public:
    PlogLogger() = delete;
    PlogLogger(const PlogLogger&) = delete;
    void operator=(PlogLogger const &) = delete;

    static std::string FormatMessage(const char *format, ...);

    static void InitializePlog(int severity = 2, const char *file = nullptr, bool alsoConsole = true,
                              size_t maxFileSize = 10 * 1024 * 1024, int maxFiles = 0);

    // 运行时生效，需先 InitializePlog；severity 为 drc_sdk::LogLevel
    static void SetLogLevel(int severity);

    static void SetConsoleOutput(bool enable);

private:
    static std::unique_ptr<plog::RollingFileAppender<plog::TxtFormatter>> file_appender_;
    static std::unique_ptr<plog::ConsoleAppender<plog::TxtFormatter>> console_appender_;
    static std::unique_ptr<plog::DynamicAppender> dynamic_appender_;
    static bool plog_initialized_;
    static bool console_enabled_;
};

} // namespace drc_sdk
