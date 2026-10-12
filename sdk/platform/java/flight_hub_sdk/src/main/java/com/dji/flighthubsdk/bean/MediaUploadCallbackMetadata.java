// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 媒体上传回调通知中的文件元数据。
 */
public final class MediaUploadCallbackMetadata {
    private double absoluteAltitude;
    private double relativeAltitude;
    private double gimbalYawDegree;
    private String createdTime;
    private MediaUploadCallbackShootPosition shootPosition;
    public MediaUploadCallbackMetadata() {
    }

/**
     * 构造 MediaUploadCallbackMetadata。
     *
     * @param absoluteAltitude 绝对高度（海拔）
     * @param relativeAltitude 相对高度
     * @param gimbalYawDegree 云台偏航角（度）
     * @param createdTime 创建时间
     * @param shootPosition 拍摄位置
     */
    public MediaUploadCallbackMetadata(
            double absoluteAltitude,
            double relativeAltitude,
            double gimbalYawDegree,
            String createdTime,
            MediaUploadCallbackShootPosition shootPosition) {
        this.absoluteAltitude = absoluteAltitude;
        this.relativeAltitude = relativeAltitude;
        this.gimbalYawDegree = gimbalYawDegree;
        this.createdTime = createdTime != null ? createdTime : "";
        this.shootPosition = shootPosition;
    }

    /** 绝对高度（海拔） */
    public double getAbsoluteAltitude() { return absoluteAltitude; }

    /** 相对高度 */
    public double getRelativeAltitude() { return relativeAltitude; }

    /** 云台偏航角（度） */
    public double getGimbalYawDegree() { return gimbalYawDegree; }

    /** 创建时间 */
    public String getCreatedTime() { return createdTime; }

    /** 拍摄位置 */
    public MediaUploadCallbackShootPosition getShootPosition() { return shootPosition; }

    /** 绝对高度（海拔） */
    public void setAbsoluteAltitude(double absoluteAltitude) {
        this.absoluteAltitude = absoluteAltitude;
    }

    /** 相对高度 */
    public void setRelativeAltitude(double relativeAltitude) {
        this.relativeAltitude = relativeAltitude;
    }

    /** 云台偏航角（度） */
    public void setGimbalYawDegree(double gimbalYawDegree) {
        this.gimbalYawDegree = gimbalYawDegree;
    }

    /** 创建时间 */
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    /** 拍摄位置 */
    public void setShootPosition(MediaUploadCallbackShootPosition shootPosition) {
        this.shootPosition = shootPosition;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaUploadCallbackMetadata that = (MediaUploadCallbackMetadata) o;
        return Double.compare(that.absoluteAltitude, absoluteAltitude) == 0
                && Double.compare(that.relativeAltitude, relativeAltitude) == 0
                && Double.compare(that.gimbalYawDegree, gimbalYawDegree) == 0
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(shootPosition, that.shootPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absoluteAltitude, relativeAltitude, gimbalYawDegree, createdTime, shootPosition);
    }

    @Override
    public String toString() {
        return "MediaUploadCallbackMetadata{"
                + "absoluteAltitude=" + absoluteAltitude
                + ", relativeAltitude=" + relativeAltitude
                + ", gimbalYawDegree=" + gimbalYawDegree
                + ", createdTime='" + createdTime + '\''
                + ", shootPosition=" + shootPosition
                + '}';
    }
}
