// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Demo 连接配置：服务器地址 / 端口 / 通知地址 / SDK 日志配置 / gateway 鉴权头。
 *
 * <p>来源：config.json(不入库, 见 .gitignore)。鉴权值支持环境变量覆盖,
 * 避免把明文 token/uuid 提交到仓库。
 *
 * <ul>
 *   <li>{@code auth.auth_key_values} 为「单键值对象」组成的数组, 支持同时注入多个 gateway 鉴权头; </li>
 *   <li>数组中任一项 value 为空时, 回退读取环境变量 {@code FLIGHTHUB_AUTH_VALUE}。</li>
 * </ul>
 */
public final class DemoConfig {

    public static final String ENV_AUTH_VALUE = "FLIGHTHUB_AUTH_VALUE";

    /** 单个 gateway 鉴权头(key/value), 对应 {@code auth.auth_key_values} 数组中的一项。 */
    public static final class AuthHeader {
        private final String key;
        private final String value;

        AuthHeader(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key() { return key; }
        public String value() { return value; }
    }

    private final String address;
    private final String port;
    private final String notificationServer;
    private final boolean verifySsl;
    private final String sslCaPath;
    private final String drtcConfigPath;
    private final String logLevel;
    private final boolean logToConsole;
    private final String logFilePath;
    private final long logMaxFileSize;
    private final int logMaxFiles;
    private final List<AuthHeader> authHeaders;

    private DemoConfig(
        String address, String port, String notificationServer,
        boolean verifySsl, String sslCaPath, String drtcConfigPath,
        String logLevel, boolean logToConsole, String logFilePath,
        long logMaxFileSize, int logMaxFiles,
        List<AuthHeader> authHeaders) {
        // 私有构造：仅由 load(...) 解析 config.json 后调用, 逐字段赋值给不可变成员。
        this.address = address;
        this.port = port;
        this.notificationServer = notificationServer;
        this.verifySsl = verifySsl;
        this.sslCaPath = sslCaPath;
        this.drtcConfigPath = drtcConfigPath;
        this.logLevel = logLevel;
        this.logToConsole = logToConsole;
        this.logFilePath = logFilePath;
        this.logMaxFileSize = logMaxFileSize;
        this.logMaxFiles = logMaxFiles;
        this.authHeaders = authHeaders;
    }

    /** 从 config.json 文件加载。 */
    public static DemoConfig load(String path) {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            throw new FlightHubException(
                "配置文件不存在: " + p.toAbsolutePath()
                    + " (可复制 config.json.example 为 config.json)"
            );
        }
        String text;
        try {
            text = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FlightHubException("读取配置文件失败: " + p, e);
        }

        Map<String, Object> root = Json.parseObject(text);

        Node n = new Node(root);
        Node server = n.child("server");
        Node sdk = n.child("sdk");
        Node auth = n.child("auth");

        List<AuthHeader> authHeaders = parseAuthHeaders(auth);

        return new DemoConfig(
                server.str("address", ""),
                server.str("port", ""),
                server.str("notification_server", ""),
                server.bool("verify_ssl", false),
                server.str("ssl_ca_path", ""),
                server.str("drtc_config_path", ""),
                sdk.str("log_level", "info"),
                sdk.bool("log_to_console", true),
                sdk.str("log_file_path", "flight_hub_sdk_demo.log"),
                sdk.longVal("log_max_file_size", 4194304L),
                (int) sdk.longVal("log_max_files", 10L),
                authHeaders
        );
    }

    /**
     * 解析 {@code auth.auth_key_values}：形如 {@code [{"key1": "value1"}, {"key2": "value2"}]} 的
     * 单键值对象数组, 逐项转换为 {@link AuthHeader}。任一项 value 为空则回退环境变量
     * {@value #ENV_AUTH_VALUE}(便于把真实鉴权值留空提交, 运行时再注入);
     * key/value 两者皆非空才纳入结果, 避免注入空鉴权头。
     */
    @SuppressWarnings("unchecked")
    private static List<AuthHeader> parseAuthHeaders(Node auth) {
        List<Object> rawList = auth.list("auth_key_values");
        List<AuthHeader> result = new ArrayList<AuthHeader>();
        String env = System.getenv(ENV_AUTH_VALUE);
        boolean envUsed = false;
        for (Object item : rawList) {
            if (!(item instanceof Map)) {
                continue;
            }
            for (Map.Entry<String, Object> e : ((Map<String, Object>) item).entrySet()) {
                String key = e.getKey();
                String value = e.getValue() == null ? "" : String.valueOf(e.getValue());
                if (value.isEmpty() && env != null && !env.isEmpty()) {
                    value = env;
                    envUsed = true;
                }
                if (key != null && !key.isEmpty() && !value.isEmpty()) {
                    result.add(new AuthHeader(key, value));
                }
            }
        }
        if (envUsed) {
            DemoLog.info("DemoConfig", "鉴权值取自环境变量 " + ENV_AUTH_VALUE);
        }
        return Collections.unmodifiableList(result);
    }

    public String address() { return address; }
    public String port() { return port; }
    public String notificationServer() { return notificationServer; }

    /**
     * WSS/HTTPS 证书验证开关, 对应 {@code server.verify_ssl}。
     * 默认关闭(与 DRTC 一致, 便于内网自签名环境开箱即用); 需要校验时显式置 true,
     * 并配合 {@link #sslCaPath()} 指向私有 CA(PEM)。
     */
    public boolean verifySsl() { return verifySsl; }

    /**
     * 私有 CA 证书文件路径(PEM 格式), 对应 {@code server.ssl_ca_path}。
     * 为空时使用系统 CA 信任库; 私有化自签名部署应指向自有 CA 文件。
     */
    public String sslCaPath() { return sslCaPath; }

    /**
     * DRTC Native SDK 配置文件路径(drtc_config_node.json), 对应 {@code server.drtc_config_path}。
     * 仅 Linux BUILD_WITH_DRTC_USER 场景需要; 为空时飞机 DRTC 拉流 Init 会失败。
     */
    public String drtcConfigPath() { return drtcConfigPath; }

    public String logLevel() { return logLevel; }
    public boolean logToConsole() { return logToConsole; }
    public String logFilePath() { return logFilePath; }
    public long logMaxFileSize() { return logMaxFileSize; }
    public int logMaxFiles() { return logMaxFiles; }
    /** 已解析且有效(key/value 均非空)的鉴权头列表, 对应 {@code auth.auth_key_values}。 */
    public List<AuthHeader> authHeaders() { return authHeaders; }

    public boolean hasAuth() {
        return !authHeaders.isEmpty();
    }
}
