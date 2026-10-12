// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/**
 * SDK 初始化配置。
 *
 * <p>调用 {@link com.dji.flighthubsdk.FlightHubSDK#initialize(FlightHubSdkConfig)} 时传入。</p>
 */
public class FlightHubSdkConfig {

    /** 日志级别，可选值：debug / info / warn / error */
    private String logLevel = "info";
    /** 是否将 SDK 日志输出到标准输出/标准错误，便于在控制台或容器日志中查看 */
    private boolean logToConsole = true;
    /** 是否在初始化时打印 SDK 版本与构建信息 */
    private boolean showVersionInfo = true;
    /** SDK 日志文件路径，默认 flight_hub_sdk.log；可为文件名或目录，SDK 滚动日志时可能追加时间戳 */
    private String logFilePath = "flight_hub_sdk.log";
    /** 单个日志文件最大字节数，默认 4MB；为 0 时由 SDK 使用内置默认值 */
    private long logMaxFileSize = 4L * 1024 * 1024;
    /** 保留的日志文件个数上限，默认 1000；为 0 时由 SDK 使用内置默认值 */
    private int logMaxFiles = 1000;
    /** PaaS 服务地址（含协议），例如 https://your-paas-host */
    private String address = "";
    /** FlightHub 服务端口 */
    private String port = "";
    /** 消息通知 WebSocket 服务地址（完整 URL，含鉴权参数时需自行拼接） */
    private String notificationServerAddress = "";
    /** DRTC Native SDK 配置文件路径（drtc_config_node.json，文件路径非目录）；仅 Linux BUILD_WITH_DRTC_USER 场景需要，为空时飞机 DRTC 拉流 Init 会失败 */
    private String drtcConfigPath = "";
    /** WSS/HTTPS 证书验证开关（安全默认开启）；校验服务器证书链与主机名/IP；私有化自签名部署须配 sslCaPath 指向 CA 文件；显式置 false 会产生 WARN 日志，仅限受控测试环境 */
    private boolean verifySsl = true;
    /** 私有 CA 证书文件路径（PEM 格式）；为空时使用系统 CA 信任库，私有化自签名部署应指向自有 CA 文件 */
    private String sslCaPath = "";

    public FlightHubSdkConfig() {
    }


    /** 日志级别，可选值：debug / info / warn / error */
    public String getLogLevel() { return logLevel; }

    /** @param logLevel 日志级别，可选值：debug / info / warn / error */
    public void setLogLevel(String logLevel) { this.logLevel = logLevel != null ? logLevel : ""; }

    /** 是否将 SDK 日志输出到标准输出/标准错误，便于在控制台或容器日志中查看 */
    public boolean isLogToConsole() { return logToConsole; }

    /** @param logToConsole 是否将 SDK 日志输出到标准输出/标准错误，便于在控制台或容器日志中查看 */
    public void setLogToConsole(boolean logToConsole) { this.logToConsole = logToConsole; }

    /** 是否在初始化时打印 SDK 版本与构建信息 */
    public boolean isShowVersionInfo() { return showVersionInfo; }

    /** @param showVersionInfo 是否在初始化时打印 SDK 版本与构建信息 */
    public void setShowVersionInfo(boolean showVersionInfo) { this.showVersionInfo = showVersionInfo; }

    /** SDK 日志文件路径，默认 flight_hub_sdk.log；可为文件名或目录，SDK 滚动日志时可能追加时间戳 */
    public String getLogFilePath() { return logFilePath; }

    /** @param logFilePath SDK 日志文件路径，默认 flight_hub_sdk.log；可为文件名或目录，SDK 滚动日志时可能追加时间戳 */
    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath != null && !logFilePath.isEmpty() ? logFilePath : "flight_hub_sdk.log";
    }

    /** 单个日志文件最大字节数，默认 4MB；为 0 时由 SDK 使用内置默认值 */
    public long getLogMaxFileSize() { return logMaxFileSize; }

    /** @param logMaxFileSize 单个日志文件最大字节数，默认 4MB；为 0 时由 SDK 使用内置默认值 */
    public void setLogMaxFileSize(long logMaxFileSize) { this.logMaxFileSize = logMaxFileSize; }

    /** 保留的日志文件个数上限，默认 1000；为 0 时由 SDK 使用内置默认值 */
    public int getLogMaxFiles() { return logMaxFiles; }

    /** @param logMaxFiles 保留的日志文件个数上限，默认 1000；为 0 时由 SDK 使用内置默认值 */
    public void setLogMaxFiles(int logMaxFiles) { this.logMaxFiles = logMaxFiles; }

    /** PaaS 服务地址（含协议），例如 https://your-paas-host */
    public String getAddress() { return address; }

    /** @param address PaaS 服务地址（含协议），例如 https://your-paas-host */
    public void setAddress(String address) { this.address = address != null ? address : ""; }

    /** FlightHub 服务端口 */
    public String getPort() { return port; }

    /** @param port FlightHub 服务端口 */
    public void setPort(String port) { this.port = port != null ? port : ""; }

    /** 消息通知 WebSocket 服务地址（完整 URL，含鉴权参数时需自行拼接） */
    public String getNotificationServerAddress() { return notificationServerAddress; }

    /** @param notificationServerAddress 消息通知 WebSocket 服务地址（完整 URL，含鉴权参数时需自行拼接） */
    public void setNotificationServerAddress(String notificationServerAddress) {
        this.notificationServerAddress = notificationServerAddress != null ? notificationServerAddress : "";
    }

    /** DRTC Native SDK 配置文件路径（drtc_config_node.json，文件路径非目录）；仅 Linux BUILD_WITH_DRTC_USER 场景需要，为空时飞机 DRTC 拉流 Init 会失败 */
    public String getDrtcConfigPath() { return drtcConfigPath; }

    /** @param drtcConfigPath DRTC Native SDK 配置文件路径（drtc_config_node.json，文件路径非目录）；仅 Linux BUILD_WITH_DRTC_USER 场景需要，为空时飞机 DRTC 拉流 Init 会失败 */
    public void setDrtcConfigPath(String drtcConfigPath) {
        this.drtcConfigPath = drtcConfigPath != null ? drtcConfigPath : "";
    }

    /** WSS/HTTPS 证书验证开关（安全默认开启）；校验服务器证书链与主机名/IP；私有化自签名部署须配 sslCaPath 指向 CA 文件；显式置 false 会产生 WARN 日志，仅限受控测试环境 */
    public boolean isVerifySsl() { return verifySsl; }

    /** @param verifySsl WSS/HTTPS 证书验证开关（安全默认开启）；校验服务器证书链与主机名/IP；私有化自签名部署须配 sslCaPath 指向 CA 文件；显式置 false 会产生 WARN 日志，仅限受控测试环境 */
    public void setVerifySsl(boolean verifySsl) { this.verifySsl = verifySsl; }

    /** 私有 CA 证书文件路径（PEM 格式）；为空时使用系统 CA 信任库，私有化自签名部署应指向自有 CA 文件 */
    public String getSslCaPath() { return sslCaPath; }

    /** @param sslCaPath 私有 CA 证书文件路径（PEM 格式）；为空时使用系统 CA 信任库，私有化自签名部署应指向自有 CA 文件 */
    public void setSslCaPath(String sslCaPath) {
        this.sslCaPath = sslCaPath != null ? sslCaPath : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlightHubSdkConfig that = (FlightHubSdkConfig) o;
        return Objects.equals(logLevel, that.logLevel)
                && logToConsole == that.logToConsole
                && showVersionInfo == that.showVersionInfo
                && Objects.equals(logFilePath, that.logFilePath)
                && logMaxFileSize == that.logMaxFileSize
                && logMaxFiles == that.logMaxFiles
                && Objects.equals(address, that.address)
                && Objects.equals(port, that.port)
                && Objects.equals(notificationServerAddress, that.notificationServerAddress)
                && Objects.equals(drtcConfigPath, that.drtcConfigPath)
                && verifySsl == that.verifySsl
                && Objects.equals(sslCaPath, that.sslCaPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                logLevel,
                logToConsole,
                showVersionInfo,
                logFilePath,
                logMaxFileSize,
                logMaxFiles,
                address,
                port,
                notificationServerAddress,
                drtcConfigPath,
                verifySsl,
                sslCaPath);
    }

    @Override
    public String toString() {
        return "FlightHubSdkConfig{"
                + "logLevel='" + logLevel + '\''
                + ", logToConsole=" + logToConsole
                + ", showVersionInfo=" + showVersionInfo
                + ", logFilePath='" + logFilePath + '\''
                + ", logMaxFileSize=" + logMaxFileSize
                + ", logMaxFiles=" + logMaxFiles
                + ", address='" + address + '\''
                + ", port='" + port + '\''
                + ", notificationServerAddress='" + notificationServerAddress + '\''
                + ", drtcConfigPath='" + drtcConfigPath + '\''
                + ", verifySsl=" + verifySsl
                + ", sslCaPath='" + sslCaPath + '\''
                + '}';
    }
}
