// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "logger_plog.h"
#include <cstdarg>
#include <cstring>
#include <ctime>
#include <iomanip>
#include <sstream>
namespace drc_sdk {

// 静态成员初始化
std::unique_ptr<plog::RollingFileAppender<plog::TxtFormatter>> PlogLogger::file_appender_;
std::unique_ptr<plog::ConsoleAppender<plog::TxtFormatter>> PlogLogger::console_appender_;
std::unique_ptr<plog::DynamicAppender> PlogLogger::dynamic_appender_;
bool PlogLogger::plog_initialized_ = false;
bool PlogLogger::console_enabled_ = true;


std::string PlogLogger::FormatMessage(const char *format, ...) {
    char *message = nullptr;
    va_list args;
    va_start(args, format);
    int log_length = vasprintf(&message, format, args);
    va_end(args);
    
    if (log_length > 0) {
        std::string result(message);
        free(message);
        return result;
    }
    return "";
}

// 将 drc_sdk::LogLevel 转换为 plog::Severity（InitializePlog / SetLogLevel 共用）
static plog::Severity ToPlogSeverity(int severity) {
    switch (severity) {
        case static_cast<int>(LogLevel::kNone):    return plog::none;
        case static_cast<int>(LogLevel::kDebug):   return plog::debug;
        case static_cast<int>(LogLevel::kInfo):    return plog::info;
        case static_cast<int>(LogLevel::kWarning): return plog::warning;
        case static_cast<int>(LogLevel::kError):   return plog::error;
        case static_cast<int>(LogLevel::kFatal):   return plog::fatal;
        default:                                   return plog::info; // 默认 info
    }
}

void PlogLogger::InitializePlog(int severity, const char *file, bool alsoConsole, size_t maxFileSize, int maxFiles) {
    if (plog_initialized_) {
        return;
    }

    console_enabled_ = alsoConsole;

    // 将 drc_sdk::LogLevel 转换为 plog::Severity
    plog::Severity plog_level = ToPlogSeverity(severity);
    
    // 创建动态 appender
    dynamic_appender_ = std::make_unique<plog::DynamicAppender>();
    
    // 添加文件 appender
    if (file && strlen(file) > 0) {
        // 生成带时间戳的文件名
        std::string filename_with_timestamp = file;
        auto now = std::time(nullptr);
        auto tm = *std::localtime(&now);
        
        std::ostringstream oss;
        oss << std::put_time(&tm, "%Y%m%d_%H%M%S");
        std::string timestamp = oss.str();
        
        // 如果文件名包含扩展名，在扩展名前插入时间戳
        size_t dot_pos = filename_with_timestamp.find_last_of('.');
        if (dot_pos != std::string::npos) {
            filename_with_timestamp = filename_with_timestamp.substr(0, dot_pos) + "_" + timestamp + filename_with_timestamp.substr(dot_pos);
        } else {
            filename_with_timestamp += "_" + timestamp;
        }
        
        // 使用配置的文件大小和文件数量（0表示不限制文件数量）
        file_appender_ = std::make_unique<plog::RollingFileAppender<plog::TxtFormatter>>(filename_with_timestamp.c_str(), maxFileSize, maxFiles);
        dynamic_appender_->addAppender(file_appender_.get());
    }
    
    // 添加控制台 appender
    if (alsoConsole) {
        console_appender_ = std::make_unique<plog::ConsoleAppender<plog::TxtFormatter>>();
        dynamic_appender_->addAppender(console_appender_.get());
    }
    
    // 初始化 plog
    plog::init(plog_level, dynamic_appender_.get());

    plog_initialized_ = true;
}

void PlogLogger::SetLogLevel(int severity) {
    // 运行时调整最大日志级别；需先 InitializePlog
    if (!plog_initialized_) {
        return;
    }
    if (plog::get() != nullptr) {
        plog::get()->setMaxSeverity(ToPlogSeverity(severity));
    }
}

void PlogLogger::SetConsoleOutput(bool enable) {
    if (!plog_initialized_ || !dynamic_appender_) {
        return;
    }
    
    if (enable && !console_enabled_) {
        // 启用控制台输出
        if (!console_appender_) {
            console_appender_ = std::make_unique<plog::ConsoleAppender<plog::TxtFormatter>>();
        }
        dynamic_appender_->addAppender(console_appender_.get());
        console_enabled_ = true;
    } else if (!enable && console_enabled_) {
        // 禁用控制台输出
        if (console_appender_) {
            dynamic_appender_->removeAppender(console_appender_.get());
        }
        console_enabled_ = false;
    }
}

} // namespace drc_sdk
