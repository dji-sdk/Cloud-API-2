// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 网络 RTK 标定参数 */
public class NetworkRtkCalibrationParams {
    /** 主机地址 */
    private String host = "";
    /** 端口 */
    private int port;
    /** 账号。需 Base64 编码 */
    private String account = "";
    /** 密码。需 Base64 编码 */
    private String password = "";
    /** 挂载点 */
    private String mountPoint = "";
    /** 版本 */
    private int version;

    public NetworkRtkCalibrationParams() {
    }


    /** 主机地址 */
    public String getHost() { return host; }
    /** @param host 主机地址 */
    public void setHost(String host) { this.host = host != null ? host : ""; }
    /** 端口 */
    public int getPort() { return port; }
    /** @param port 端口 */
    public void setPort(int port) { this.port = port; }
    /** 账号。需 Base64 编码 */
    public String getAccount() { return account; }
    /** @param account 账号。需 Base64 编码 */
    public void setAccount(String account) { this.account = account != null ? account : ""; }
    /** 密码。需 Base64 编码 */
    public String getPassword() { return password; }
    /** @param password 密码。需 Base64 编码 */
    public void setPassword(String password) { this.password = password != null ? password : ""; }
    /** 挂载点 */
    public String getMountPoint() { return mountPoint; }
    /** @param mountPoint 挂载点 */
    public void setMountPoint(String mountPoint) { this.mountPoint = mountPoint != null ? mountPoint : ""; }
    /** 版本 */
    public int getVersion() { return version; }
    /** @param version 版本 */
    public void setVersion(int version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkRtkCalibrationParams that = (NetworkRtkCalibrationParams) o;
        return Objects.equals(host, that.host)
                && port == that.port
                && Objects.equals(account, that.account)
                && Objects.equals(password, that.password)
                && Objects.equals(mountPoint, that.mountPoint)
                && version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                host,
                port,
                account,
                password,
                mountPoint,
                version);
    }

    @Override
    public String toString() {
        return "NetworkRtkCalibrationParams{"
                + "host='" + host + '\''
                + ", port=" + port
                + ", account='" + account + '\''
                + ", password='" + password + '\''
                + ", mountPoint='" + mountPoint + '\''
                + ", version=" + version
                + '}';
    }
}
