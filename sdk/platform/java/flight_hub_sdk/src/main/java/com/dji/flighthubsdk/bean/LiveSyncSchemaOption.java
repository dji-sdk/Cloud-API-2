// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** 直播转发推流协议参数（RTMP / GB28181 / RTSP 字段同层扁平） */
public class LiveSyncSchemaOption {
    // ---- GB28181 ----
    /** SIP 服务器 ID */
    private String serverId = "";
    /** SIP 服务器 IP */
    private String serverIp = "";
    /** SIP 服务器端口 */
    private String serverPort = "";
    /** 设备接入密码 */
    private String devicePassword = "";
    /** 本地 SIP 端口 */
    private String localPort = "";
    /** 设备编码 ID */
    private String deviceId = "";
    /** 本地通道编码 */
    private String localChannel = "";
    // ---- RTMP ----
    /** RTMP 推流地址 */
    private String url = "";
    // ---- RTSP ----
    /** RTSP 服务商（如 volc） */
    private String provider = "";
    /** 火山引擎用户 ID */
    private String volcUserId = "";
    /** RTSP 推流地址 */
    private String rtspUrl = "";
    /** 鉴权用户名 */
    private String username = "";
    /** 鉴权密码 */
    private String password = "";
    /** 是否开启 TS 封装 */
    private boolean enableTs = false;

    public LiveSyncSchemaOption() {}

    public String getServerId() { return serverId; }
    public void setServerId(String v) { this.serverId = v != null ? v : ""; }

    public String getServerIp() { return serverIp; }
    public void setServerIp(String v) { this.serverIp = v != null ? v : ""; }

    public String getServerPort() { return serverPort; }
    public void setServerPort(String v) { this.serverPort = v != null ? v : ""; }

    public String getDevicePassword() { return devicePassword; }
    public void setDevicePassword(String v) { this.devicePassword = v != null ? v : ""; }

    public String getLocalPort() { return localPort; }
    public void setLocalPort(String v) { this.localPort = v != null ? v : ""; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String v) { this.deviceId = v != null ? v : ""; }

    public String getLocalChannel() { return localChannel; }
    public void setLocalChannel(String v) { this.localChannel = v != null ? v : ""; }

    public String getUrl() { return url; }
    public void setUrl(String v) { this.url = v != null ? v : ""; }

    public String getProvider() { return provider; }
    public void setProvider(String v) { this.provider = v != null ? v : ""; }

    public String getVolcUserId() { return volcUserId; }
    public void setVolcUserId(String v) { this.volcUserId = v != null ? v : ""; }

    public String getRtspUrl() { return rtspUrl; }
    public void setRtspUrl(String v) { this.rtspUrl = v != null ? v : ""; }

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v != null ? v : ""; }

    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v != null ? v : ""; }

    public boolean isEnableTs() { return enableTs; }
    public void setEnableTs(boolean v) { this.enableTs = v; }
}
