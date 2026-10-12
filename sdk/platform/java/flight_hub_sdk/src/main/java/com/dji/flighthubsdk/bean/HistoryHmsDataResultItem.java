// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 历史HMS数据结果条目。 */
public final class HistoryHmsDataResultItem {
    private String mongoId;
    /** 业务标识(bid) */
    private String bid;
    /** 事务 ID */
    private String tid;
    /** 告警级别。枚举：0=通知, 1=提醒, 2=警告 */
    private int level;
    /** 模块标识 */
    private int module;
    /** 告警消息 */
    private String message;
    /** 事件状态。枚举：1=live, 2=非 live */
    private int status;
    /** 告警码 */
    private String code;
    /** 设备数据创建时间 */
    private long deviceDataCreateTime;
    /** 设备数据更新时间 */
    private long deviceDataUpdateTime;
    /** 记录创建时间 */
    private long createTime;
    /** 记录更新时间 */
    private long updateTime;
    /** 告警参数详情 */
    private HistoryHmsDataResultArgs args;
    /** 设备类型 */
    private String deviceType;
    /** 领域类型 */
    private String domainType;
    /** 是否紧急 */
    private int imminent;
    /** 是否在飞行中 */
    private int inTheSky;
    /** 联合键 */
    private String jointKey;
    /** 状态键 */
    private String statusKey;
    /** 设备 SN */
    private String sn;
    public HistoryHmsDataResultItem() {
    }

/**
     * 构造 HistoryHmsDataResultItem。
     */
    public HistoryHmsDataResultItem(
            String mongoId,
            String bid,
            String tid,
            int level,
            int module,
            String message,
            int status,
            String code,
            long deviceDataCreateTime,
            long deviceDataUpdateTime,
            long createTime,
            long updateTime,
            HistoryHmsDataResultArgs args,
            String deviceType,
            String domainType,
            int imminent,
            int inTheSky,
            String jointKey,
            String statusKey,
            String sn) {
        this.mongoId = mongoId != null ? mongoId : "";
        this.bid = bid != null ? bid : "";
        this.tid = tid != null ? tid : "";
        this.level = level;
        this.module = module;
        this.message = message != null ? message : "";
        this.status = status;
        this.code = code != null ? code : "";
        this.deviceDataCreateTime = deviceDataCreateTime;
        this.deviceDataUpdateTime = deviceDataUpdateTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.args = args;
        this.deviceType = deviceType != null ? deviceType : "";
        this.domainType = domainType != null ? domainType : "";
        this.imminent = imminent;
        this.inTheSky = inTheSky;
        this.jointKey = jointKey != null ? jointKey : "";
        this.statusKey = statusKey != null ? statusKey : "";
        this.sn = sn != null ? sn : "";
    }

    /** MongoDB ID */
    public String getMongoId() { return mongoId; }
    /** 业务标识(bid) */
    public String getBid() { return bid; }
    /** 事务 ID */
    public String getTid() { return tid; }
    /** 告警级别。枚举：0=通知, 1=提醒, 2=警告 */
    public int getLevel() { return level; }
    /** 模块标识 */
    public int getModule() { return module; }
    /** 告警消息 */
    public String getMessage() { return message; }
    /** 事件状态。枚举：1=live, 2=非 live */
    public int getStatus() { return status; }
    /** 告警码 */
    public String getCode() { return code; }
    /** 设备数据创建时间 */
    public long getDeviceDataCreateTime() { return deviceDataCreateTime; }
    /** 设备数据更新时间 */
    public long getDeviceDataUpdateTime() { return deviceDataUpdateTime; }
    /** 记录创建时间 */
    public long getCreateTime() { return createTime; }
    /** 记录更新时间 */
    public long getUpdateTime() { return updateTime; }
    /** 告警参数详情 */
    public HistoryHmsDataResultArgs getArgs() { return args; }
    /** 设备类型 */
    public String getDeviceType() { return deviceType; }
    /** 领域类型 */
    public String getDomainType() { return domainType; }
    /** 是否紧急 */
    public int getImminent() { return imminent; }
    /** 是否在飞行中 */
    public int getInTheSky() { return inTheSky; }
    /** 联合键 */
    public String getJointKey() { return jointKey; }
    /** 状态键 */
    public String getStatusKey() { return statusKey; }
    /** 设备 SN */
    public String getSn() { return sn; }

    /** MongoDB ID */
    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    /** @param bid 业务标识(bid) */
    public void setBid(String bid) {
        this.bid = bid;
    }

    /** 事务 ID */
    public void setTid(String tid) {
        this.tid = tid;
    }

    /** 告警级别。枚举：0=通知, 1=提醒, 2=警告 */
    public void setLevel(int level) {
        this.level = level;
    }

    /** @param module 模块标识 */
    public void setModule(int module) {
        this.module = module;
    }

    /** @param message 告警消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 事件状态。枚举：1=live, 2=非 live */
    public void setStatus(int status) {
        this.status = status;
    }

    /** 告警码 */
    public void setCode(String code) {
        this.code = code;
    }

    /** 设备数据创建时间 */
    public void setDeviceDataCreateTime(long deviceDataCreateTime) {
        this.deviceDataCreateTime = deviceDataCreateTime;
    }

    /** 设备数据更新时间 */
    public void setDeviceDataUpdateTime(long deviceDataUpdateTime) {
        this.deviceDataUpdateTime = deviceDataUpdateTime;
    }

    /** @param createTime 记录创建时间 */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /** @param updateTime 记录更新时间 */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /** @param args 告警参数详情 */
    public void setArgs(HistoryHmsDataResultArgs args) {
        this.args = args;
    }

    /** 设备类型 */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /** @param domainType 领域类型 */
    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    /** 是否紧急 */
    public void setImminent(int imminent) {
        this.imminent = imminent;
    }

    /** 是否在飞行中 */
    public void setInTheSky(int inTheSky) {
        this.inTheSky = inTheSky;
    }

    /** 联合键 */
    public void setJointKey(String jointKey) {
        this.jointKey = jointKey;
    }

    /** 状态键 */
    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
    }

    /** @param sn 设备 SN */
    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HistoryHmsDataResultItem that = (HistoryHmsDataResultItem) o;
        return Objects.equals(mongoId, that.mongoId)
                && Objects.equals(bid, that.bid)
                && Objects.equals(tid, that.tid)
                && level == that.level
                && module == that.module
                && Objects.equals(message, that.message)
                && status == that.status
                && Objects.equals(code, that.code)
                && deviceDataCreateTime == that.deviceDataCreateTime
                && deviceDataUpdateTime == that.deviceDataUpdateTime
                && createTime == that.createTime
                && updateTime == that.updateTime
                && Objects.equals(args, that.args)
                && Objects.equals(deviceType, that.deviceType)
                && Objects.equals(domainType, that.domainType)
                && imminent == that.imminent
                && inTheSky == that.inTheSky
                && Objects.equals(jointKey, that.jointKey)
                && Objects.equals(statusKey, that.statusKey)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mongoId,
                bid,
                tid,
                level,
                module,
                message,
                status,
                code,
                deviceDataCreateTime,
                deviceDataUpdateTime,
                createTime,
                updateTime,
                args,
                deviceType,
                domainType,
                imminent,
                inTheSky,
                jointKey,
                statusKey,
                sn);
    }

    @Override
    public String toString() {
        return "HistoryHmsDataResultItem{"
                + "mongoId='" + mongoId + '\''
                + ", bid='" + bid + '\''
                + ", tid='" + tid + '\''
                + ", level=" + level
                + ", module=" + module
                + ", message='" + message + '\''
                + ", status=" + status
                + ", code='" + code + '\''
                + ", deviceDataCreateTime=" + deviceDataCreateTime
                + ", deviceDataUpdateTime=" + deviceDataUpdateTime
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + ", args=" + args
                + ", deviceType='" + deviceType + '\''
                + ", domainType='" + domainType + '\''
                + ", imminent=" + imminent
                + ", inTheSky=" + inTheSky
                + ", jointKey='" + jointKey + '\''
                + ", statusKey='" + statusKey + '\''
                + ", sn='" + sn + '\''
                + '}';
    }
}
