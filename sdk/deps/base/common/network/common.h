// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <map>
#include <functional>
#include <string>
#include <cstdint>

namespace drc_sdk {
namespace common {

using HttpHeaders = std::map<std::string, std::string>;

enum class NetworkError : int {
    /// 网络请求成功
    SUCCESS = 0,

    /// 无网络支持（APP未联网 / 系统不支持 / ...）
    NO_NETWORK = 1,

    /// 请求不合法（URL不合法 / 请求参数不合法 / ...）
    BAD_REQUEST = 2,

    /// 网络不可达（DNS解析失败 / 路由ICMP不可达 / 请求超时 / ...）
    UNREACHABLE = 3,

    /// 网络请求失败（non-2xx返回码 / 响应体编码无效 / 压缩算法无效 / ...）
    FAILURE = 4,

    /// 请求被取消（下载模块使用）
    CANCELLED = 5,

    /// 发生未知错误
    UNKNOWN = 0xFFFF,
};

enum class HttpPostType : int {
    /// MIME -> application/json
    JSON_DATA = 0,

    /// MIME -> application/x-www-form-urlencoded
    X_WWW_FORM_DATA = 1,

    /// 未知类型
    UNKNOWN = 0xFFFF,
};

/// 返回二进制数据，不一定为UTF-8编码
using HttpCallback = std::function<void(NetworkError err_code, const std::string &response,
    const std::vector<std::string> &certs, const HttpHeaders &headers)>;

/// 带 HTTP 原始状态码的回调（重载版），http_code 为 0 表示 curl 层失败（无 HTTP 响应）
using HttpCallbackWithStatus = std::function<void(NetworkError err_code, int http_code,
    const std::string &response,
    const std::vector<std::string> &certs,
    const HttpHeaders &headers)>;

/// 下载进度回调 -> 文件体积 + 进度比例
using DownloadProcess = std::function<void(uint64_t size, float ratio)>;

/// 下载结果回调
using DownloadResult = std::function<void(NetworkError err_code)>;

/// 流请求数据回调
using StreamData = std::function<void(const std::string &data)>;

/// 流请求结果回调
using StreamResult = std::function<void(NetworkError err_code)>;

} // namespace common
} // namespace drc_sdk
