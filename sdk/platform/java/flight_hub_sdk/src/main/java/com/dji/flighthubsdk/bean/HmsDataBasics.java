// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** HMS 基础字段。 */
public final class HmsDataBasics {

    /** int, 告警等级, {"0":"通知","1":"提醒","2":"警告"}; */
    private int level;
    /** int, 事件模块, {"0":"飞行任务","1":"设备管理","2":"媒体","3":"hms"}; */
    private int moduleId;
    /** string, 告警码, {无效值: 空值}; */
    private String code;
    /** string, 设备类型, 上云API格式:{domain-type-subtype},可以根据产品支持查询, {无效值: 空值}; */
    private String deviceType;
    /** string, such as "drone_nest"; */
    private String domainType;
    /** int, 是否飞行, {"0":"在地上", "1":"在天上"}; */
    private int inTheSky;
    /** int, 是否为及时性的, {"0":"否", "1":"是"}; */
    private int imminent;
    /** HmsArgs, 告警参数信息; */
    private HmsArgs args;
    /** string; */
    private String statusKey;
    /** string; */
    private String jointKey;
    /** int, 每个设备上报事件里hms的唯一id; */
    private int id;
    /** int, 每个设备上报事件里hms的唯一父id; */
    private int parentId;
    public HmsDataBasics() {
    }

/**
     * 构造 HmsDataBasics。
     *
     * @param level 告警级别
     * @param moduleId 模块 ID
     * @param code 告警码
     * @param deviceType 设备类型
     * @param domainType 域类型
     * @param inTheSky 是否在飞行中
     * @param imminent 是否紧急
     * @param args HMS 参数
     * @param statusKey 状态键
     * @param jointKey 联合键
     * @param id ID
     * @param parentId 父级 ID
     */
    public HmsDataBasics(
            int level,
            int moduleId,
            String code,
            String deviceType,
            String domainType,
            int inTheSky,
            int imminent,
            HmsArgs args,
            String statusKey,
            String jointKey,
            int id,
            int parentId) {
        this.level = level;
        this.moduleId = moduleId;
        this.code = code != null ? code : "";
        this.deviceType = deviceType != null ? deviceType : "";
        this.domainType = domainType != null ? domainType : "";
        this.inTheSky = inTheSky;
        this.imminent = imminent;
        this.args = args != null ? args : new HmsArgs(0, 0, "", "", 0, 0, 0L, 0);
        this.statusKey = statusKey != null ? statusKey : "";
        this.jointKey = jointKey != null ? jointKey : "";
        this.id = id;
        this.parentId = parentId;
    }

    /** int, 告警等级, {"0":"通知","1":"提醒","2":"警告"}; */
    public int getLevel() { return level; }
    /** int, 事件模块, {"0":"飞行任务","1":"设备管理","2":"媒体","3":"hms"}; */
    public int getModuleId() { return moduleId; }
    /** string, 告警码, {无效值: 空值}; */
    public String getCode() { return code; }
    /** string, 设备类型, 上云API格式:{domain-type-subtype},可以根据产品支持查询, {无效值: 空值}; */
    public String getDeviceType() { return deviceType; }
    /** string, such as "drone_nest"; */
    public String getDomainType() { return domainType; }
    /** int, 是否飞行, {"0":"在地上", "1":"在天上"}; */
    public int getInTheSky() { return inTheSky; }
    /** int, 是否为及时性的, {"0":"否", "1":"是"}; */
    public int getImminent() { return imminent; }
    /** HmsArgs, 告警参数信息; */
    public HmsArgs getArgs() { return args; }
    /** string; */
    public String getStatusKey() { return statusKey; }
    /** string; */
    public String getJointKey() { return jointKey; }
    /** int, 每个设备上报事件里hms的唯一id; */
    public int getId() { return id; }
    /** int, 每个设备上报事件里hms的唯一父id; */
    public int getParentId() { return parentId; }

    /** @param level int, 告警等级, {"0":"通知","1":"提醒","2":"警告"}; */
    public void setLevel(int level) {
        this.level = level;
    }

    /** @param moduleId int, 事件模块, {"0":"飞行任务","1":"设备管理","2":"媒体","3":"hms"}; */
    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    /** @param code string, 告警码, {无效值: 空值}; */
    public void setCode(String code) {
        this.code = code;
    }

    /** @param deviceType string, 设备类型, 上云API格式:{domain-type-subtype},可以根据产品支持查询, {无效值: 空值}; */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /** @param domainType string, such as "drone_nest"; */
    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    /** @param inTheSky int, 是否飞行, {"0":"在地上", "1":"在天上"}; */
    public void setInTheSky(int inTheSky) {
        this.inTheSky = inTheSky;
    }

    /** @param imminent int, 是否为及时性的, {"0":"否", "1":"是"}; */
    public void setImminent(int imminent) {
        this.imminent = imminent;
    }

    /** @param args HmsArgs, 告警参数信息; */
    public void setArgs(HmsArgs args) {
        this.args = args;
    }

    /** @param statusKey string; */
    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
    }

    /** @param jointKey string; */
    public void setJointKey(String jointKey) {
        this.jointKey = jointKey;
    }

    /** @param id int, 每个设备上报事件里hms的唯一id; */
    public void setId(int id) {
        this.id = id;
    }

    /** @param parentId int, 每个设备上报事件里hms的唯一父id; */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HmsDataBasics that = (HmsDataBasics) o;
        return level == that.level
                && moduleId == that.moduleId
                && inTheSky == that.inTheSky
                && imminent == that.imminent
                && id == that.id
                && parentId == that.parentId
                && Objects.equals(code, that.code)
                && Objects.equals(deviceType, that.deviceType)
                && Objects.equals(domainType, that.domainType)
                && Objects.equals(args, that.args)
                && Objects.equals(statusKey, that.statusKey)
                && Objects.equals(jointKey, that.jointKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                level,
                moduleId,
                code,
                deviceType,
                domainType,
                inTheSky,
                imminent,
                args,
                statusKey,
                jointKey,
                id,
                parentId);
    }

    @Override
    public String toString() {
        return "HmsDataBasics{"
                + "level=" + level
                + ", moduleId=" + moduleId
                + ", code='" + code + '\''
                + ", deviceType='" + deviceType + '\''
                + ", domainType='" + domainType + '\''
                + ", inTheSky=" + inTheSky
                + ", imminent=" + imminent
                + ", args=" + args
                + ", statusKey='" + statusKey + '\''
                + ", jointKey='" + jointKey + '\''
                + ", id=" + id
                + ", parentId=" + parentId
                + '}';
    }
}
