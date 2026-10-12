// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Terra Build 重建任务 Job 信息 */
public final class TerraBuildJobInfo {
    private int type;
    private String uuid;
    private int jobStatus;
    private int percent;
    private int code;
    public TerraBuildJobInfo() {
    }

/**
     * 构造 TerraBuildJobInfo。
     *
     * @param type 几何类型。枚举："Circle"=圆形, "Polygon"=多边形, "Polyline"=折线
     * @param uuid 任务 UUID
     * @param jobStatus 作业状态
     * @param percent 全景拍照进度百分比
     * @param code 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时
     */
    public TerraBuildJobInfo(int type, String uuid, int jobStatus, int percent, int code) {
        this.type = type;
        this.uuid = uuid != null ? uuid : "";
        this.jobStatus = jobStatus;
        this.percent = percent;
        this.code = code;
    }

    /** 几何类型。枚举："Circle"=圆形, "Polygon"=多边形, "Polyline"=折线 */
    public int getType() { return type; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 作业状态 */
    public int getJobStatus() { return jobStatus; }
    /** 全景拍照进度百分比 */
    public int getPercent() { return percent; }
    /** 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时 */
    public int getCode() { return code; }

    /** 几何类型。枚举："Circle"=圆形, "Polygon"=多边形, "Polyline"=折线 */
    public void setType(int type) {
        this.type = type;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 作业状态 */
    public void setJobStatus(int jobStatus) {
        this.jobStatus = jobStatus;
    }

    /** 全景拍照进度百分比 */
    public void setPercent(int percent) {
        this.percent = percent;
    }

    /** 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时 */
    public void setCode(int code) {
        this.code = code;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildJobInfo that = (TerraBuildJobInfo) o;
        return type == that.type && jobStatus == that.jobStatus && percent == that.percent && code == that.code
                && Objects.equals(uuid, that.uuid);
    }
    @Override public int hashCode() { return Objects.hash(type, uuid, jobStatus, percent, code); }

    @Override
    public String toString() {
        return "TerraBuildJobInfo{"
                + "type=" + type
                + ", uuid='" + uuid + '\''
                + ", jobStatus=" + jobStatus
                + ", percent=" + percent
                + ", code=" + code
                + '}';
    }
}
