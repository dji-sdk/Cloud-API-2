// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 三方云(API1/API2) 配置（CloudEnvTargetConfig.type == 1 或 3 时使用）。 */
public class CloudEnvThirdCloudConfig {
    private String bindCode = "";
    private String orgId = "";
    private String mqttAddr = "";
    private String mqttPassword = "";
    private String mqttUsername = "";

    public CloudEnvThirdCloudConfig() {
    }

    /** 绑定码 */
    public String getBindCode() {
        return bindCode;
    }

    /** 绑定码 */
    public void setBindCode(String bindCode) {
        this.bindCode = bindCode;
    }

    /** 组织 ID */
    public String getOrgId() {
        return orgId;
    }

    /** 组织 ID */
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    /** MQTT 服务地址 */
    public String getMqttAddr() {
        return mqttAddr;
    }

    /** MQTT 服务地址 */
    public void setMqttAddr(String mqttAddr) {
        this.mqttAddr = mqttAddr;
    }

    /** MQTT 密码 */
    public String getMqttPassword() {
        return mqttPassword;
    }

    /** MQTT 密码 */
    public void setMqttPassword(String mqttPassword) {
        this.mqttPassword = mqttPassword;
    }

    /** MQTT 用户名 */
    public String getMqttUsername() {
        return mqttUsername;
    }

    /** MQTT 用户名 */
    public void setMqttUsername(String mqttUsername) {
        this.mqttUsername = mqttUsername;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CloudEnvThirdCloudConfig that = (CloudEnvThirdCloudConfig) o;
        return Objects.equals(bindCode, that.bindCode)
                && Objects.equals(orgId, that.orgId)
                && Objects.equals(mqttAddr, that.mqttAddr)
                && Objects.equals(mqttPassword, that.mqttPassword)
                && Objects.equals(mqttUsername, that.mqttUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bindCode, orgId, mqttAddr, mqttPassword, mqttUsername);
    }

    @Override
    public String toString() {
        return "CloudEnvThirdCloudConfig{bindCode='" + bindCode + '\'' + ", orgId='" + orgId + '\''
                + ", mqttAddr='" + mqttAddr + '\'' + ", mqttPassword='***'" + ", mqttUsername='" + mqttUsername + '\'' + '}';
    }
}
