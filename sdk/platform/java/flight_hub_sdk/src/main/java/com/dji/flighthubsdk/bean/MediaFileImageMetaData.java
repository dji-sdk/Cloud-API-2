// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 媒体文件图片元数据。 */
public final class MediaFileImageMetaData {
    private String photoCreateTime;
    private double flightYaw;
    private double focalLength;
    private double gimbalPitch;
    private double gimbalRoll;
    private double gimbalYaw;
    private int height;
    private int width;
    private double zoomFactor;
    private double digitalZoomRatio;
    /** 35mm 胶片等效焦距，示例值：50; */
    private double lengthIn35mmFilm;
    /** 图片类型, { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera"}, 空字符串表示无效; */
    private String imageType;
    /** 载荷类型, 示例值: "1-98-0"; */
    private String payloadType;
    /** 动作唯一标识符(UUID); */
    private String actionUuid;
    /** 照片拍摄位置信息; */
    private PositionData photoPosition;
    /** 飞机位置信息; */
    private PositionData shootPosition;
    public MediaFileImageMetaData() {
    }

/**
     * 构造 MediaFileImageMetaData。
     *
     * @param photoCreateTime 拍照创建时间
     * @param flightYaw 飞行偏航角
     * @param focalLength 焦距
     * @param gimbalPitch 云台俯仰角
     * @param gimbalRoll 云台横滚角
     * @param gimbalYaw 云台偏航角
     * @param height 图像高度
     * @param width 图像宽度
     * @param zoomFactor 变焦倍率
     * @param digitalZoomRatio 数字变焦比例
     * @param lengthIn35mmFilm 等效35mm胶片焦距
     * @param imageType 图像类型。枚举："WideCamera"=广角, "ZoomCamera"=变焦, "InfraredCamera"=红外, "PanoramaCamera"=全景；空字符串表示无效
     * @param payloadType 负载类型
     * @param actionUuid 动作UUID
     * @param photoPosition 拍照位置
     * @param shootPosition 拍摄位置
     */
    public MediaFileImageMetaData(
            String photoCreateTime,
            double flightYaw,
            double focalLength,
            double gimbalPitch,
            double gimbalRoll,
            double gimbalYaw,
            int height,
            int width,
            double zoomFactor,
            double digitalZoomRatio,
            double lengthIn35mmFilm,
            String imageType,
            String payloadType,
            String actionUuid,
            PositionData photoPosition,
            PositionData shootPosition) {
        this.photoCreateTime = photoCreateTime != null ? photoCreateTime : "";
        this.flightYaw = flightYaw;
        this.focalLength = focalLength;
        this.gimbalPitch = gimbalPitch;
        this.gimbalRoll = gimbalRoll;
        this.gimbalYaw = gimbalYaw;
        this.height = height;
        this.width = width;
        this.zoomFactor = zoomFactor;
        this.digitalZoomRatio = digitalZoomRatio;
        this.lengthIn35mmFilm = lengthIn35mmFilm;
        this.imageType = imageType != null ? imageType : "";
        this.payloadType = payloadType != null ? payloadType : "";
        this.actionUuid = actionUuid != null ? actionUuid : "";
        this.photoPosition = photoPosition;
        this.shootPosition = shootPosition;
    }

    /** 拍照创建时间 */
    public String getPhotoCreateTime() {
        return photoCreateTime;
    }

    /** 拍照创建时间 */
    public void setPhotoCreateTime(String photoCreateTime) {
        this.photoCreateTime = photoCreateTime;
    }

    /** 飞行偏航角 */
    public double getFlightYaw() {
        return flightYaw;
    }

    /** 飞行偏航角 */
    public void setFlightYaw(double flightYaw) {
        this.flightYaw = flightYaw;
    }

    /** 焦距 */
    public double getFocalLength() {
        return focalLength;
    }

    /** 焦距 */
    public void setFocalLength(double focalLength) {
        this.focalLength = focalLength;
    }

    /** 云台俯仰角 */
    public double getGimbalPitch() {
        return gimbalPitch;
    }

    /** 云台俯仰角 */
    public void setGimbalPitch(double gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    /** 云台横滚角 */
    public double getGimbalRoll() {
        return gimbalRoll;
    }

    /** 云台横滚角 */
    public void setGimbalRoll(double gimbalRoll) {
        this.gimbalRoll = gimbalRoll;
    }

    /** 云台偏航角 */
    public double getGimbalYaw() {
        return gimbalYaw;
    }

    /** 云台偏航角 */
    public void setGimbalYaw(double gimbalYaw) {
        this.gimbalYaw = gimbalYaw;
    }

    /** 图像高度 */
    public int getHeight() {
        return height;
    }

    /** 图像高度 */
    public void setHeight(int height) {
        this.height = height;
    }

    /** 图像宽度 */
    public int getWidth() {
        return width;
    }

    /** 图像宽度 */
    public void setWidth(int width) {
        this.width = width;
    }

    /** 变焦倍率 */
    public double getZoomFactor() {
        return zoomFactor;
    }

    /** 变焦倍率 */
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    /** 数字变焦比例 */
    public double getDigitalZoomRatio() {
        return digitalZoomRatio;
    }

    /** 数字变焦比例 */
    public void setDigitalZoomRatio(double digitalZoomRatio) {
        this.digitalZoomRatio = digitalZoomRatio;
    }

    /** 35mm 胶片等效焦距，示例值：50; */
    public double getLengthIn35mmFilm() {
        return lengthIn35mmFilm;
    }

    /** @param lengthIn35mmFilm 35mm 胶片等效焦距，示例值：50; */
    public void setLengthIn35mmFilm(double lengthIn35mmFilm) {
        this.lengthIn35mmFilm = lengthIn35mmFilm;
    }

    /** 图片类型, { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera"}, 空字符串表示无效; */
    public String getImageType() {
        return imageType;
    }

    /** @param imageType 图片类型, { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera"}, 空字符串表示无效; */
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /** 载荷类型, 示例值: "1-98-0"; */
    public String getPayloadType() {
        return payloadType;
    }

    /** @param payloadType 载荷类型, 示例值: "1-98-0"; */
    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    /** 动作唯一标识符(UUID); */
    public String getActionUuid() {
        return actionUuid;
    }

    /** @param actionUuid 动作唯一标识符(UUID); */
    public void setActionUuid(String actionUuid) {
        this.actionUuid = actionUuid;
    }

    /** 照片拍摄位置信息; */
    public PositionData getPhotoPosition() {
        return photoPosition;
    }

    /** @param photoPosition 照片拍摄位置信息; */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFileImageMetaData that = (MediaFileImageMetaData) o;
        return Objects.equals(photoCreateTime, that.photoCreateTime)
                && Double.compare(that.flightYaw, flightYaw) == 0
                && Double.compare(that.focalLength, focalLength) == 0
                && Double.compare(that.gimbalPitch, gimbalPitch) == 0
                && Double.compare(that.gimbalRoll, gimbalRoll) == 0
                && Double.compare(that.gimbalYaw, gimbalYaw) == 0
                && height == that.height
                && width == that.width
                && Double.compare(that.zoomFactor, zoomFactor) == 0
                && Double.compare(that.digitalZoomRatio, digitalZoomRatio) == 0
                && Double.compare(that.lengthIn35mmFilm, lengthIn35mmFilm) == 0
                && Objects.equals(imageType, that.imageType)
                && Objects.equals(payloadType, that.payloadType)
                && Objects.equals(actionUuid, that.actionUuid)
                && Objects.equals(photoPosition, that.photoPosition)
                && Objects.equals(shootPosition, that.shootPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                photoCreateTime,
                flightYaw,
                focalLength,
                gimbalPitch,
                gimbalRoll,
                gimbalYaw,
                height,
                width,
                zoomFactor,
                digitalZoomRatio,
                lengthIn35mmFilm,
                imageType,
                payloadType,
                actionUuid,
                photoPosition,
                shootPosition);
    }

    @Override
    public String toString() {
        return "MediaFileImageMetaData{"
                + "photoCreateTime='" + photoCreateTime + '\''
                + ", flightYaw=" + flightYaw
                + ", focalLength=" + focalLength
                + ", gimbalPitch=" + gimbalPitch
                + ", gimbalRoll=" + gimbalRoll
                + ", gimbalYaw=" + gimbalYaw
                + ", height=" + height
                + ", width=" + width
                + ", zoomFactor=" + zoomFactor
                + ", digitalZoomRatio=" + digitalZoomRatio
                + ", lengthIn35mmFilm=" + lengthIn35mmFilm
                + ", imageType='" + imageType + '\''
                + ", payloadType='" + payloadType + '\''
                + ", actionUuid='" + actionUuid + '\''
                + ", photoPosition=" + photoPosition
                + ", shootPosition=" + shootPosition
                + '}';
    }
}
