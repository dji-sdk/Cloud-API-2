// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>

namespace notification_center {
    // aligned with drc_sdk::LogLevel
    enum class SdkLogLevel {
        NONE_LEVEL = 0,
        DEBUG_LEVEL = 1,
        INFO_LEVEL = 2,
        WARNING_LEVEL = 3,
        ERROR_LEVEL = 4,
        FATAL_LEVEL = 5,
    };

    class SdkInitializeInfo {
    public:
        // 日志配置
        std::string log_path;
        SdkLogLevel log_level = SdkLogLevel::NONE_LEVEL;
        bool log_to_console = true;                 // 是否打印日志到终端
        size_t log_max_file_size = 4 * 1024 * 1024; // 单个日志文件最大大小（默认4MB）
        int log_max_files = 1000;                   // 最大日志文件数量（默认1000个）

        // TLS 证书验证配置（WSS 通道），安全默认开启
        // verify_ssl=true 时校验服务器证书链与主机名/IP：
        //   - ssl_ca_path 为空：使用系统 CA 信任库（公网/正规 CA 证书部署）
        //   - ssl_ca_path 非空：使用指定 PEM 格式私有 CA 文件（私有化自签名部署推荐）
        // verify_ssl=false 跳过全部验证（会产生 WARN 日志，仅限受控测试环境）
        bool verify_ssl = true;
        std::string ssl_ca_path;
    };

    // 客户端错误码
    enum NotificationCenterErrorCode : int64_t {
        // 大于0，就是无错误，为ListenID
        NotificationCenterErrorCode_NoError = 0,
        NotificationCenterErrorCode_InvalidParameter = -1, // 参数错误
        NotificationCenterErrorCode_NotConnected = -2, // 链路未建立
        NotificationCenterErrorCode_SendError = -3, // TopicManager 错误
        NotificationCenterErrorCode_SubscribeError = -4, // 向服务器订阅失败
    };

    // 通知频率枚举
    enum NotificationFrequency {
        NotificationFrequency_Any = 0, // 服务端自定决定推送频率
        NotificationFrequency_OnChanged = 1, // 按变推送
        NotificationFrequency_Push_1s = 1000, // 定频 1s 推送 1 次
        NotificationFrequency_Push_2s = 2000, // 定频 2s 推送 1 次
        NotificationFrequency_Push_3s = 3000, // 定频 3s 推送 1 次
        NotificationFrequency_Push_4s = 4000, // 定频 4s 推送 1 次
        NotificationFrequency_Push_5s = 5000, // 定频 5s 推送 1 次
        NotificationFrequency_Push_10s = 10000, // 定频 10s 推送 1 次
        NotificationFrequency_Push_20s = 20000, // 定频 20s 推送 1 次
        NotificationFrequency_Push_30s = 30000, // 定频 30s 推送 1 次
    };
}
