// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 媒体文件扩展信息（对齐后端 media.proto FileDetailExt） */
public final class MediaFileDetailExt {

    /** 图片宽度（像素） */
    private int width;
    /** 图片高度（像素） */
    private int height;
    /** 照片 GPS 位置 */
    private MediaFileGeoPosition photoPosition;
    /** 分辨率 */
    private String resolution;
    /** 拍摄者 */
    private String photographer;
    /** 任务名称 */
    private String taskName;
    /** 航线名称 */
    private String lineName;
    /** 航线 UUID */
    private String waylineUuid;
    /** 原始航线 UUID */
    private String originWaylineUuid;
    /** 航线动作 UUID */
    private String waylineActionUuid;
    /** 负载型号 Key */
    private String payloadModelKey;
    /** 飞行器型号 Key */
    private String droneModelKey;
    /** 是否原片 */
    private boolean isOriginal;
    /** 数码变焦倍数 */
    private double digitalZoomRatio;
    /** 35mm 等效焦距 */
    private double lengthIn35mmFilm;
    /** 云台横滚角 */
    private double gimbalRoll;
    /** 云台偏航角 */
    private double gimbalYaw;
    /** 云台偏航角（度） */
    private double gimbalYawDegree;
    /** 飞行任务 ID */
    private String flightId;
    /** 时长（秒） */
    private double duration;
    /** 拍摄位置 */
    private MediaFileGeoPosition shootPosition;
    /** 绝对高度（米） */
    private double absoluteAltitude;
    /** 相对高度（米） */
    private double relativeAltitude;
    /** 创建时间 */
    private String createdTime;
    /** 创建时间 */
    private String creationTime;
    /** 创建时间 */
    private String createTime;
    /** 编码器 */
    private String encoder;
    /** 格式长名称 */
    private String formatLongName;
    /** 格式名称 */
    private String formatName;
    /** 扩展大小（字符串） */
    private String extSize;
    /** 飞行偏航角 */
    private double flightYaw;
    /** 焦距 */
    private double focalLength;
    /** 云台俯仰角 */
    private double gimbalPitch;
    /** 图片类型 */
    private String imageType;

    public MediaFileDetailExt() {
        this.photoPosition = new MediaFileGeoPosition();
        this.shootPosition = new MediaFileGeoPosition();
    }

    /** 图片宽度（像素） */
    public int getWidth() { return width; }

    /** 图片宽度（像素） */
    public void setWidth(int width) { this.width = width; }

    /** 图片高度（像素） */
    public int getHeight() { return height; }

    /** 图片高度（像素） */
    public void setHeight(int height) { this.height = height; }

    /** 照片 GPS 位置 */
    public MediaFileGeoPosition getPhotoPosition() { return photoPosition; }

    /** 照片 GPS 位置 */
    public void setPhotoPosition(MediaFileGeoPosition photoPosition) {
        this.photoPosition = photoPosition != null ? photoPosition : new MediaFileGeoPosition();
    }

    /** 分辨率 */
    public String getResolution() { return resolution; }

    /** 分辨率 */
    public void setResolution(String resolution) { this.resolution = resolution; }

    /** 拍摄者 */
    public String getPhotographer() { return photographer; }

    /** 拍摄者 */
    public void setPhotographer(String photographer) { this.photographer = photographer; }

    /** 任务名称 */
    public String getTaskName() { return taskName; }

    /** 任务名称 */
    public void setTaskName(String taskName) { this.taskName = taskName; }

    /** 航线名称 */
    public String getLineName() { return lineName; }

    /** 航线名称 */
    public void setLineName(String lineName) { this.lineName = lineName; }

    /** 航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) { this.waylineUuid = waylineUuid; }

    /** 原始航线 UUID */
    public String getOriginWaylineUuid() { return originWaylineUuid; }

    /** 原始航线 UUID */
    public void setOriginWaylineUuid(String originWaylineUuid) { this.originWaylineUuid = originWaylineUuid; }

    /** 航线动作 UUID */
    public String getWaylineActionUuid() { return waylineActionUuid; }

    /** 航线动作 UUID */
    public void setWaylineActionUuid(String waylineActionUuid) { this.waylineActionUuid = waylineActionUuid; }

    /** 负载型号 Key */
    public String getPayloadModelKey() { return payloadModelKey; }

    /** 负载型号 Key */
    public void setPayloadModelKey(String payloadModelKey) { this.payloadModelKey = payloadModelKey; }

    /** 飞行器型号 Key */
    public String getDroneModelKey() { return droneModelKey; }

    /** 飞行器型号 Key */
    public void setDroneModelKey(String droneModelKey) { this.droneModelKey = droneModelKey; }

    /** 是否原片 */
    public boolean isOriginal() { return isOriginal; }

    /** 是否原片 */
    public void setOriginal(boolean original) { this.isOriginal = original; }

    /** 数码变焦倍数 */
    public double getDigitalZoomRatio() { return digitalZoomRatio; }

    /** 数码变焦倍数 */
    public void setDigitalZoomRatio(double digitalZoomRatio) { this.digitalZoomRatio = digitalZoomRatio; }

    /** 35mm 等效焦距 */
    public double getLengthIn35mmFilm() { return lengthIn35mmFilm; }

    /** 35mm 等效焦距 */
    public void setLengthIn35mmFilm(double lengthIn35mmFilm) { this.lengthIn35mmFilm = lengthIn35mmFilm; }

    /** 云台横滚角 */
    public double getGimbalRoll() { return gimbalRoll; }

    /** 云台横滚角 */
    public void setGimbalRoll(double gimbalRoll) { this.gimbalRoll = gimbalRoll; }

    /** 云台偏航角 */
    public double getGimbalYaw() { return gimbalYaw; }

    /** 云台偏航角 */
    public void setGimbalYaw(double gimbalYaw) { this.gimbalYaw = gimbalYaw; }

    /** 云台偏航角（度） */
    public double getGimbalYawDegree() { return gimbalYawDegree; }

    /** 云台偏航角（度） */
    public void setGimbalYawDegree(double gimbalYawDegree) { this.gimbalYawDegree = gimbalYawDegree; }

    /** 飞行任务 ID */
    public String getFlightId() { return flightId; }

    /** 飞行任务 ID */
    public void setFlightId(String flightId) { this.flightId = flightId; }

    /** 时长（秒） */
    public double getDuration() { return duration; }

    /** 时长（秒） */
    public void setDuration(double duration) { this.duration = duration; }

    /** 拍摄位置 */
    public MediaFileGeoPosition getShootPosition() { return shootPosition; }

    /** 拍摄位置 */
    public void setShootPosition(MediaFileGeoPosition shootPosition) {
        this.shootPosition = shootPosition != null ? shootPosition : new MediaFileGeoPosition();
    }

    /** 绝对高度（米） */
    public double getAbsoluteAltitude() { return absoluteAltitude; }

    /** 绝对高度（米） */
    public void setAbsoluteAltitude(double absoluteAltitude) { this.absoluteAltitude = absoluteAltitude; }

    /** 相对高度（米） */
    public double getRelativeAltitude() { return relativeAltitude; }

    /** 相对高度（米） */
    public void setRelativeAltitude(double relativeAltitude) { this.relativeAltitude = relativeAltitude; }

    /** 创建时间 */
    public String getCreatedTime() { return createdTime; }

    /** 创建时间 */
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    /** 创建时间 */
    public String getCreationTime() { return creationTime; }

    /** 创建时间 */
    public void setCreationTime(String creationTime) { this.creationTime = creationTime; }

    /** 创建时间 */
    public String getCreateTime() { return createTime; }

    /** 创建时间 */
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    /** 编码器 */
    public String getEncoder() { return encoder; }

    /** 编码器 */
    public void setEncoder(String encoder) { this.encoder = encoder; }

    /** 格式长名称 */
    public String getFormatLongName() { return formatLongName; }

    /** 格式长名称 */
    public void setFormatLongName(String formatLongName) { this.formatLongName = formatLongName; }

    /** 格式名称 */
    public String getFormatName() { return formatName; }

    /** 格式名称 */
    public void setFormatName(String formatName) { this.formatName = formatName; }

    /** 扩展大小（字符串） */
    public String getExtSize() { return extSize; }

    /** 扩展大小（字符串） */
    public void setExtSize(String extSize) { this.extSize = extSize; }

    /** 飞行偏航角 */
    public double getFlightYaw() { return flightYaw; }

    /** 飞行偏航角 */
    public void setFlightYaw(double flightYaw) { this.flightYaw = flightYaw; }

    /** 焦距 */
    public double getFocalLength() { return focalLength; }

    /** 焦距 */
    public void setFocalLength(double focalLength) { this.focalLength = focalLength; }

    /** 云台俯仰角 */
    public double getGimbalPitch() { return gimbalPitch; }

    /** 云台俯仰角 */
    public void setGimbalPitch(double gimbalPitch) { this.gimbalPitch = gimbalPitch; }

    /** 图片类型 */
    public String getImageType() { return imageType; }

    /** 图片类型 */
    public void setImageType(String imageType) { this.imageType = imageType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaFileDetailExt that = (MediaFileDetailExt) o;
        return width == that.width
                && height == that.height
                && isOriginal == that.isOriginal
                && Double.compare(that.digitalZoomRatio, digitalZoomRatio) == 0
                && Double.compare(that.lengthIn35mmFilm, lengthIn35mmFilm) == 0
                && Double.compare(that.gimbalRoll, gimbalRoll) == 0
                && Double.compare(that.gimbalYaw, gimbalYaw) == 0
                && Double.compare(that.gimbalYawDegree, gimbalYawDegree) == 0
                && Double.compare(that.duration, duration) == 0
                && Double.compare(that.absoluteAltitude, absoluteAltitude) == 0
                && Double.compare(that.relativeAltitude, relativeAltitude) == 0
                && Double.compare(that.flightYaw, flightYaw) == 0
                && Double.compare(that.focalLength, focalLength) == 0
                && Double.compare(that.gimbalPitch, gimbalPitch) == 0
                && Objects.equals(photoPosition, that.photoPosition)
                && Objects.equals(shootPosition, that.shootPosition)
                && Objects.equals(resolution, that.resolution)
                && Objects.equals(photographer, that.photographer)
                && Objects.equals(taskName, that.taskName)
                && Objects.equals(lineName, that.lineName)
                && Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(originWaylineUuid, that.originWaylineUuid)
                && Objects.equals(waylineActionUuid, that.waylineActionUuid)
                && Objects.equals(payloadModelKey, that.payloadModelKey)
                && Objects.equals(droneModelKey, that.droneModelKey)
                && Objects.equals(flightId, that.flightId)
                && Objects.equals(createdTime, that.createdTime)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(createTime, that.createTime)
                && Objects.equals(encoder, that.encoder)
                && Objects.equals(formatLongName, that.formatLongName)
                && Objects.equals(formatName, that.formatName)
                && Objects.equals(extSize, that.extSize)
                && Objects.equals(imageType, that.imageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, photoPosition, resolution, photographer, taskName, lineName,
                waylineUuid, originWaylineUuid, waylineActionUuid, payloadModelKey, droneModelKey,
                isOriginal, digitalZoomRatio, lengthIn35mmFilm, gimbalRoll, gimbalYaw, gimbalYawDegree,
                flightId, duration, shootPosition, absoluteAltitude, relativeAltitude, createdTime,
                creationTime, createTime, encoder, formatLongName, formatName, extSize, flightYaw,
                focalLength, gimbalPitch, imageType);
    }

    @Override
    public String toString() {
        return "MediaFileDetailExt{"
                + "width=" + width
                + ", height=" + height
                + ", photoPosition=" + photoPosition
                + ", resolution='" + resolution + '\''
                + ", photographer='" + photographer + '\''
                + ", taskName='" + taskName + '\''
                + ", lineName='" + lineName + '\''
                + ", waylineUuid='" + waylineUuid + '\''
                + ", originWaylineUuid='" + originWaylineUuid + '\''
                + ", waylineActionUuid='" + waylineActionUuid + '\''
                + ", payloadModelKey='" + payloadModelKey + '\''
                + ", droneModelKey='" + droneModelKey + '\''
                + ", isOriginal=" + isOriginal
                + ", digitalZoomRatio=" + digitalZoomRatio
                + ", lengthIn35mmFilm=" + lengthIn35mmFilm
                + ", gimbalRoll=" + gimbalRoll
                + ", gimbalYaw=" + gimbalYaw
                + ", gimbalYawDegree=" + gimbalYawDegree
                + ", flightId='" + flightId + '\''
                + ", duration=" + duration
                + ", shootPosition=" + shootPosition
                + ", absoluteAltitude=" + absoluteAltitude
                + ", relativeAltitude=" + relativeAltitude
                + ", createdTime='" + createdTime + '\''
                + ", creationTime='" + creationTime + '\''
                + ", createTime='" + createTime + '\''
                + ", encoder='" + encoder + '\''
                + ", formatLongName='" + formatLongName + '\''
                + ", formatName='" + formatName + '\''
                + ", extSize='" + extSize + '\''
                + ", flightYaw=" + flightYaw
                + ", focalLength=" + focalLength
                + ", gimbalPitch=" + gimbalPitch
                + ", imageType='" + imageType + '\''
                + '}';
    }
}
