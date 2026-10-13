// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

// ---------------------------------------------------------------------------
// FH36-P2-02 修复: 请求/响应体日志安全化工具(共享实现)。
// 使用方: src/bll/http_interface/http_interface.cpp(C++ 主网络路径)、
//         deps/base/common/network/impl/network_emsdk.cc(WASM 路径)。
//
// 策略: 内容类日志(body/response/data/请求对象序列化)统一降 DEBUG 级;
// 打印前经 log_safe() 检测——内容含敏感字段(凭据类)则整个字符串不打印,
// 仅输出占位说明(fail-safe: 宁可整体隐藏也不泄露)。
//
// 性能: ① plog 宏在级别未启用时短路求值(IF_PLOG), 生产(info 级)下 DEBUG
// 调用不执行本函数、零开销; ② 实际执行时(DEBUG 排障/ERROR 路径)采用
// "小写化 + std::string::find" 组合——find 底层为 memchr 向量化搜索, 实测
// 远快于逐字节比较循环(3 倍+, 已实测否决该方案); ③ 小写化缓冲经
// thread_local 复用, 消除每次调用的堆分配(实测较每次分配方案快 3 倍);
// ④ 词表按命中概率排序, 命中即提前返回。
//
// 配套 CI 门禁: scripts/make_source_package.sh 的 FH36-P2-02 规则——
// 任意级别日志出现未包裹的内容实参即拒绝打包。
// ---------------------------------------------------------------------------

#include <cctype>
#include <string>

namespace drc_sdk {
namespace common {

// 敏感词表: 覆盖 SDK 全部凭据面(直播 token / STS access_key / relay 密码 /
// RTMP stream_key / 鉴权头 / 预签名 URL signature / Set-Cookie / 证书 license)。
// 命中任一即整条内容不打印; 词表为本仓可扩充的唯一清单, 新增凭据面须同步维护,
// 并同步更新 scripts/make_source_package.sh 门禁与单元测试。
inline bool contains_sensitive_field(const std::string &content) {
    static const char *kSensitiveWords[] = {
        "token", "password", "passwd", "secret", "credential",
        "access_key", "private_key", "stream_key", "authorization",
        "signature", "cookie", "license_key",
    };
    // thread_local 复用小写化缓冲: 消除热路径堆分配(容量按历史最大值保留,
    // 上界为单条响应大小; 每线程一份, 无锁; inline 函数内 static 跨翻译单元
    // 共享同一实体, 符合 ODR)
    static thread_local std::string lower;
    lower.assign(content);
    for (char &c : lower) {
        c = static_cast<char>(::tolower(static_cast<unsigned char>(c)));
    }
    for (const char *word : kSensitiveWords) {
        if (lower.find(word) != std::string::npos) {
            return true;  // 命中即提前返回(短路)
        }
    }
    return false;
}

// 内容安全化: 常见路径(非敏感)零拷贝返回原内容引用; 敏感路径(低频)返回
// thread_local 占位串(含 size, 便于排障判断响应规模)。返回引用无临时对象,
// 调用点 .c_str() 生命周期天然安全。
inline const std::string &log_safe(const std::string &content) {
    if (!contains_sensitive_field(content)) {
        return content;
    }
    static thread_local std::string hidden;
    hidden = "(敏感字段已隐藏, size: " + std::to_string(content.size()) + ")";
    return hidden;
}

}  // namespace common
}  // namespace drc_sdk
