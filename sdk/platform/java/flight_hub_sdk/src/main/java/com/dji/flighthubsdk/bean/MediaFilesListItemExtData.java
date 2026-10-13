// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 媒体文件列表条目扩展数据。 */
public final class MediaFilesListItemExtData {
    /** 创建时间; */
    private String createTime;
    /** 飞行偏航角; */
    private double flightYaw;
    /** 焦距; */
    private double focalLength;
    /** 云台俯仰角; */
    private double gimbalPitch;
    /** 云台横滚角; */
    private double gimbalRoll;
    /** 云台偏航角; */
    private double gimbalYaw;
    /** 图片宽度; */
    private int width;
    /** 图片高度; */
    private int height;
    /** 图片类型; { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera" }, 空字符串表示无效; */
    private String imageType;
    /** 图片位置信息; */
    private PositionData photoPosition;
    /** 飞机位置信息; */
    private PositionData shootPosition;
    /** 焦距倍数, 服务端可能无返回值, 默认为 INT_MIN(比如 -2.147483648E9) 无效值; */
    private double zoomFactor;
    public MediaFilesListItemExtData() {
    }

/**
     * 构造 MediaFilesListItemExtData。
     *
     * @param createTime 创建时间
     * @param flightYaw 飞行偏航角
     * @param focalLength 焦距
     * @param gimbalPitch 云台俯仰角
     * @param gimbalRoll 云台横滚角
     * @param gimbalYaw 云台偏航角
     * @param width 图像宽度
     * @param height 图像高度
     * @param imageType 图像类型。枚举："WideCamera"=广角, "ZoomCamera"=变焦, "InfraredCamera"=红外, "PanoramaCamera"=全景；空字符串表示无效
     * @param photoPosition 拍照位置
     * @param shootPosition 拍摄位置
     * @param zoomFactor 变焦倍率
     */
    public MediaFilesListItemExtData(
            String createTime,
            double flightYaw,
            double focalLength,
            double gimbalPitch,
            double gimbalRoll,
            double gimbalYaw,
            int width,
            int height,
            String imageType,
            PositionData photoPosition,
            PositionData shootPosition,
            double zoomFactor) {
        this.createTime = createTime != null ? createTime : "";
        this.flightYaw = flightYaw;
        this.focalLength = focalLength;
        this.gimbalPitch = gimbalPitch;
        this.gimbalRoll = gimbalRoll;
        this.gimbalYaw = gimbalYaw;
        this.width = width;
        this.height = height;
        this.imageType = imageType != null ? imageType : "";
        this.photoPosition = photoPosition;
        this.shootPosition = shootPosition;
        this.zoomFactor = zoomFactor;
    }

    /** 创建时间; */
    public String getCreateTime() {
        return createTime;
    }

    /** @param createTime 创建时间; */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /** 飞行偏航角; */
    public double getFlightYaw() {
        return flightYaw;
    }

    /** @param flightYaw 飞行偏航角; */
    public void setFlightYaw(double flightYaw) {
        this.flightYaw = flightYaw;
    }

    /** 焦距; */
    public double getFocalLength() {
        return focalLength;
    }

    /** @param focalLength 焦距; */
    public void setFocalLength(double focalLength) {
        this.focalLength = focalLength;
    }

    /** 云台俯仰角; */
    public double getGimbalPitch() {
        return gimbalPitch;
    }

    /** @param gimbalPitch 云台俯仰角; */
    public void setGimbalPitch(double gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    /** 云台横滚角; */
    public double getGimbalRoll() {
        return gimbalRoll;
    }

    /** @param gimbalRoll 云台横滚角; */
    public void setGimbalRoll(double gimbalRoll) {
        this.gimbalRoll = gimbalRoll;
    }

    /** 云台偏航角; */
    public double getGimbalYaw() {
        return gimbalYaw;
    }

    /** @param gimbalYaw 云台偏航角; */
    public void setGimbalYaw(double gimbalYaw) {
        this.gimbalYaw = gimbalYaw;
    }

    /** 图片宽度; */
    public int getWidth() {
        return width;
    }

    /** @param width 图片宽度; */
    public void setWidth(int width) {
        this.width = width;
    }

    /** 图片高度; */
    public int getHeight() {
        return height;
    }

    /** @param height 图片高度; */
    public void setHeight(int height) {
        this.height = height;
    }

    /** 图片类型; { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera" }, 空字符串表示无效; */
    public String getImageType() {
        return imageType;
    }

    /** @param imageType 图片类型; { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera" }, 空字符串表示无效; */
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /** 图片位置信息; */
    public PositionData getPhotoPosition() {
        return photoPosition;
    }

    /** @param photoPosition 图片位置信息; */
    public void setPhotoPosition(PositionData photoPosition) {
        this.photoPosition = photoPosition;
    }

    /** 飞机位置信息; */
    public PositionData getShootPosition() {
        return shootPosition;
    }

    /** @param shootPosition 飞机位置信息; */
    public void setShootPosition(PositionData shootPosition) {
        this.shootPosition = shootPosition;
    }

    /** 焦距倍数, 服务端可能无返回值, 默认为 INT_MIN(比如 -2.147483648E9) 无效值; */
    public double getZoomFactor() {
        return zoomFactor;
    }

    /** @param zoomFactor 焦距倍数, 服务端可能无返回值, 默认为 INT_MIN(比如 -2.147483648E9) 无效值; */
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFilesListItemExtData that = (MediaFilesListItemExtData) o;
        return Objects.equals(createTime, that.createTime)
                && Double.compare(that.flightYaw, flightYaw) == 0
                && Double.compare(that.focalLength, focalLength) == 0
                && Double.compare(that.gimbalPitch, gimbalPitch) == 0
                && Double.compare(that.gimbalRoll, gimbalRoll) == 0
                && Double.compare(that.gimbalYaw, gimbalYaw) == 0
                && width == that.width
                && height == that.height
                && Objects.equals(imageType, that.imageType)
                && Objects.equals(photoPosition, that.photoPosition)
                && Objects.equals(shootPosition, that.shootPosition)
                && Double.compare(that.zoomFactor, zoomFactor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                createTime,
                flightYaw,
                focalLength,
                gimbalPitch,
                gimbalRoll,
                gimbalYaw,
                width,
                height,
                imageType,
                photoPosition,
                shootPosition,
                zoomFactor);
    }

    @Override
    public String toString() {
        return "MediaFilesListItemExtData{"
                + "createTime='" + createTime + '\''
                + ", flightYaw=" + flightYaw
                + ", focalLength=" + focalLength
                + ", gimbalPitch=" + gimbalPitch
                + ", gimbalRoll=" + gimbalRoll
                + ", gimbalYaw=" + gimbalYaw
                + ", width=" + width
                + ", height=" + height
                + ", imageType='" + imageType + '\''
                + ", photoPosition=" + photoPosition
                + ", shootPosition=" + shootPosition
                + ", zoomFactor=" + zoomFactor
                + '}';
    }
}
