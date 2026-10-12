// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * Workspace 设备详情中的型号信息。
 */
public final class ModelInfo {

    private String key;
    private String domain;
    private String type;
    private String subType;
    private String name;
    private String deviceClass;
    private String developCode;
    public ModelInfo() {
    }

/**
     * 构造 ModelInfo。
     *
     * @param key 型号Key
     * @param domain 领域
     * @param type 类型
     * @param subType 子类型
     * @param name 名称
     * @param deviceClass 设备类别
     * @param developCode 开发代码
     */
    public ModelInfo(
            String key,
            String domain,
            String type,
            String subType,
            String name,
            String deviceClass,
            String developCode) {
        this.key = key != null ? key : "";
        this.domain = domain != null ? domain : "";
        this.type = type != null ? type : "";
        this.subType = subType != null ? subType : "";
        this.name = name != null ? name : "";
        this.deviceClass = deviceClass != null ? deviceClass : "";
        this.developCode = developCode != null ? developCode : "";
    }

    /** 型号Key */
    public String getKey() {
        return key;
    }

    /** 型号Key */
    public void setKey(String key) {
        this.key = key;
    }

    /** 领域 */
    public String getDomain() {
        return domain;
    }

    /** 领域 */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /** 类型 */
    public String getType() {
        return type;
    }

    /** 类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** 子类型 */
    public String getSubType() {
        return subType;
    }

    /** 子类型 */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /** 名称 */
    public String getName() {
        return name;
    }

    /** 名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 设备类别 */
    public String getDeviceClass() {
        return deviceClass;
    }

    /** 设备类别 */
    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    /** 开发代码 */
    public String getDevelopCode() {
        return developCode;
    }

    /** 开发代码 */
    public void setDevelopCode(String developCode) {
        this.developCode = developCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelInfo that = (ModelInfo) o;
        return Objects.equals(key, that.key)
                && Objects.equals(domain, that.domain)
                && Objects.equals(type, that.type)
                && Objects.equals(subType, that.subType)
                && Objects.equals(name, that.name)
                && Objects.equals(deviceClass, that.deviceClass)
                && Objects.equals(developCode, that.developCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, domain, type, subType, name, deviceClass, developCode);
    }

    @Override
    public String toString() {
        return "ModelInfo{"
                + "key='" + key + '\''
                + ", domain='" + domain + '\''
                + ", type='" + type + '\''
                + ", subType='" + subType + '\''
                + ", name='" + name + '\''
                + ", deviceClass='" + deviceClass + '\''
                + ", developCode='" + developCode + '\''
                + '}';
    }
}
