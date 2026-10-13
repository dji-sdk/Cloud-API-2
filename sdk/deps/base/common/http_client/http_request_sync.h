// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <functional>
#include <map>
#include <memory>
#include <unordered_map>
#include "base/io/byte_buffer.h"
#include "curl.h"

namespace drc_sdk {
namespace common {
namespace network {

// ptr points to the delivered data, and the size of that data is nmemb; size is always 1.
using WriteFunction = std::function<size_t(char *ptr, size_t size, size_t nmemb)>;

using RequestHeader = std::unordered_map<std::string, std::string>;

class HttpSyncRequest  {
public:
    HttpSyncRequest();
    ~HttpSyncRequest();
    HttpSyncRequest &operator=(const HttpSyncRequest &) = delete;
    HttpSyncRequest(const HttpSyncRequest &) = delete;

    void SetURL(const std::string &url);
    void SetHeader(const RequestHeader &header);
    void SetSslVerify(bool enable, const std::string& ca_path = "");
    void OpenDebugLog();

    // 设置全局 TLS 证书验证配置(进程级, 与 Network 单例语义一致)。
    // 每个 HttpSyncRequest 构造时自动应用 —— 网络层 9 处实例化点及未来新增点零遗漏。
    // verify_ssl=true: 校验服务器证书链与主机名; ca_path 为空用系统 CA, 非空用指定 PEM 文件
    // verify_ssl=false: 显式逃生舱(不安全, 仅限受控测试环境, 产生 WARN 日志)
    static void SetGlobalTlsConfig(bool verify_ssl, const std::string &ca_path = "");

    // |callback| called when data received.
    void OnDataWrite(WriteFunction callback);

    CURLcode Get(uint32_t timeout_ms = 10000);
    CURLcode Post(const std::string &data, uint32_t timeout_ms = 10000);
    CURLcode PostMultipartFiles(const std::vector<std::string> &file_paths, const std::string &field_name = "files", uint32_t timeout_ms = 60000);
    CURLcode Put(const std::string &data, uint32_t timeout_ms = 10000);
    CURLcode Delete(const std::string &data, uint32_t timeout_ms = 10000);
    long StatusCode();
    void Cancel();
    
    // 获取响应数据
    std::string GetResponseData();

public:
    size_t OnCurlWrite(char *ptr, size_t size, size_t nmemb);

private:
    WriteFunction write_function_;
    ByteBuffer response_buffer_;

    CURL *easy_ = nullptr;
    std::string url_;
    curl_slist *header_list_ = nullptr;
    std::string data_str_;
    long status_code_ = 0;
};

}  // namespace network
}  // namespace common
}  // namespace drc_sdk
