// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 相机工作状态。
 * <p>{@link #getCameraMode()} 为与协议一致的整型模式码，可与 {@link com.dji.flighthubsdk.value.CameraMode#fromValue(int)} 对照。</p>
 */
public final class CameraState {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`） */
    private int cameraMode;
    private int recordTime;
    private boolean recordingState;
    private int remainRecordDuration;
    private boolean photoState;
    private int remainPhotoNum;
    private double intervalPhotoInterval;
    // ── B4 扩展（drc_camera_state_push.camera_state 全量） ──
    /** 通用视频分辨率 */
    private int videoResolution;
    /** 联动变焦状态 */
    private int linkageZoomState;
    /** 照片尺寸 */
    private int photoSize;
    /** 广角视频分辨率 */
    private int videoResolutionWide;
    /** 变焦视频分辨率 */
    private int videoResolutionZoom;
    /** 广角照片尺寸 */
    private int photoSizeWide;
    /** 变焦照片尺寸 */
    private int photoSizeZoom;
    /** 照片格式 */
    private int photoFormat;
    /** 温度超出范围有效性 */
    private boolean thermalTempExceedRangeValid;
    /** 红外超分使能 */
    private boolean thermalSuperResolutionEnable;
    /** 等温线使能 */
    private int isothermEnable;
    /** 等温线 ROI 渲染使能 */
    private int isothermRoiBasedRenderingEnable;
    /** 等温线颜色渲染类型 */
    private int isothermColorRenderingType;
    /** 等温线下阈值 */
    private int isothermLowerThreshold;
    /** 等温线上阈值 */
    private int isothermUpperThreshold;
    public CameraState() {
    }

/**
     * 构造 CameraState。
     *
     * @param payloadIndex 负载索引
     * @param cameraMode 相机模式
     * @param recordTime 录制时长(秒)
     * @param recordingState 是否正在录制
     * @param remainRecordDuration 剩余可录制时长
     * @param photoState 是否正在拍照
     * @param remainPhotoNum 剩余可拍照数量
     * @param intervalPhotoInterval 定时拍照间隔
     */
    public CameraState(
            String payloadIndex,
            int cameraMode,
            int recordTime,
            boolean recordingState,
            int remainRecordDuration,
            boolean photoState,
            int remainPhotoNum,
            double intervalPhotoInterval) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.cameraMode = cameraMode;
        this.recordTime = recordTime;
        this.recordingState = recordingState;
        this.remainRecordDuration = remainRecordDuration;
        this.photoState = photoState;
        this.remainPhotoNum = remainPhotoNum;
        this.intervalPhotoInterval = intervalPhotoInterval;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`） */
    public int getCameraMode() {
        return cameraMode;
    }

    /** @param cameraMode 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`） */
    public void setCameraMode(int cameraMode) {
        this.cameraMode = cameraMode;
    }

    /** 录制时长(秒) */
    public int getRecordTime() {
        return recordTime;
    }

    /** 录制时长(秒) */
    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    /** 是否正在录制 */
    public boolean isRecordingState() {
        return recordingState;
    }

    /** 是否正在录制 */
    public void setRecordingState(boolean recordingState) {
        this.recordingState = recordingState;
    }

    /** 剩余可录制时长 */
    public int getRemainRecordDuration() {
        return remainRecordDuration;
    }

    /** 剩余可录制时长 */
    public void setRemainRecordDuration(int remainRecordDuration) {
        this.remainRecordDuration = remainRecordDuration;
    }

    /** 是否正在拍照 */
    public boolean isPhotoState() {
        return photoState;
    }

    /** 是否正在拍照 */
    public void setPhotoState(boolean photoState) {
        this.photoState = photoState;
    }

    /** 剩余可拍照数量 */
    public int getRemainPhotoNum() {
        return remainPhotoNum;
    }

    /** 剩余可拍照数量 */
    public void setRemainPhotoNum(int remainPhotoNum) {
        this.remainPhotoNum = remainPhotoNum;
    }

    /** 定时拍照间隔 */
    public double getIntervalPhotoInterval() {
        return intervalPhotoInterval;
    }

    /** 定时拍照间隔 */
    public void setIntervalPhotoInterval(double intervalPhotoInterval) {
        this.intervalPhotoInterval = intervalPhotoInterval;
    }

    // ── B4 扩展 getter/setter ──
    /** 通用视频分辨率 */
    public int getVideoResolution() { return videoResolution; }
    /** @param videoResolution 通用视频分辨率 */
    public void setVideoResolution(int v) { this.videoResolution = v; }
    /** 联动变焦状态 */
    public int getLinkageZoomState() { return linkageZoomState; }
    /** @param linkageZoomState 联动变焦状态 */
    public void setLinkageZoomState(int v) { this.linkageZoomState = v; }
    /** 照片尺寸 */
    public int getPhotoSize() { return photoSize; }
    /** @param photoSize 照片尺寸 */
    public void setPhotoSize(int v) { this.photoSize = v; }
    /** 广角视频分辨率 */
    public int getVideoResolutionWide() { return videoResolutionWide; }
    /** @param videoResolutionWide 广角视频分辨率 */
    public void setVideoResolutionWide(int v) { this.videoResolutionWide = v; }
    /** 变焦视频分辨率 */
    public int getVideoResolutionZoom() { return videoResolutionZoom; }
    /** @param videoResolutionZoom 变焦视频分辨率 */
    public void setVideoResolutionZoom(int v) { this.videoResolutionZoom = v; }
    /** 广角照片尺寸 */
    public int getPhotoSizeWide() { return photoSizeWide; }
    /** @param photoSizeWide 广角照片尺寸 */
    public void setPhotoSizeWide(int v) { this.photoSizeWide = v; }
    /** 变焦照片尺寸 */
    public int getPhotoSizeZoom() { return photoSizeZoom; }
    /** @param photoSizeZoom 变焦照片尺寸 */
    public void setPhotoSizeZoom(int v) { this.photoSizeZoom = v; }
    /** 照片格式 */
    public int getPhotoFormat() { return photoFormat; }
    /** @param photoFormat 照片格式 */
    public void setPhotoFormat(int v) { this.photoFormat = v; }
    /** 温度超出范围有效性 */
    public boolean isThermalTempExceedRangeValid() { return thermalTempExceedRangeValid; }
    /** @param thermalTempExceedRangeValid 温度超出范围有效性 */
    public void setThermalTempExceedRangeValid(boolean v) { this.thermalTempExceedRangeValid = v; }
    /** 红外超分使能 */
    public boolean isThermalSuperResolutionEnable() { return thermalSuperResolutionEnable; }
    /** @param thermalSuperResolutionEnable 红外超分使能 */
    public void setThermalSuperResolutionEnable(boolean v) { this.thermalSuperResolutionEnable = v; }
    /** 等温线使能 */
    public int getIsothermEnable() { return isothermEnable; }
    /** @param isothermEnable 等温线使能 */
    public void setIsothermEnable(int v) { this.isothermEnable = v; }
    /** 等温线 ROI 渲染使能 */
    public int getIsothermRoiBasedRenderingEnable() { return isothermRoiBasedRenderingEnable; }
    /** @param isothermRoiBasedRenderingEnable 等温线 ROI 渲染使能 */
    public void setIsothermRoiBasedRenderingEnable(int v) { this.isothermRoiBasedRenderingEnable = v; }
    /** 等温线颜色渲染类型 */
    public int getIsothermColorRenderingType() { return isothermColorRenderingType; }
    /** @param isothermColorRenderingType 等温线颜色渲染类型 */
    public void setIsothermColorRenderingType(int v) { this.isothermColorRenderingType = v; }
    /** 等温线下阈值 */
    public int getIsothermLowerThreshold() { return isothermLowerThreshold; }
    /** @param isothermLowerThreshold 等温线下阈值 */
    public void setIsothermLowerThreshold(int v) { this.isothermLowerThreshold = v; }
    /** 等温线上阈值 */
    public int getIsothermUpperThreshold() { return isothermUpperThreshold; }
    /** @param isothermUpperThreshold 等温线上阈值 */
    public void setIsothermUpperThreshold(int v) { this.isothermUpperThreshold = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraState that = (CameraState) o;
        return cameraMode == that.cameraMode
                && recordTime == that.recordTime
                && recordingState == that.recordingState
                && remainRecordDuration == that.remainRecordDuration
                && photoState == that.photoState
                && remainPhotoNum == that.remainPhotoNum
                && Double.compare(that.intervalPhotoInterval, intervalPhotoInterval) == 0
                && videoResolution == that.videoResolution
                && linkageZoomState == that.linkageZoomState
                && photoSize == that.photoSize
                && videoResolutionWide == that.videoResolutionWide
                && videoResolutionZoom == that.videoResolutionZoom
                && photoSizeWide == that.photoSizeWide
                && photoSizeZoom == that.photoSizeZoom
                && photoFormat == that.photoFormat
                && thermalTempExceedRangeValid == that.thermalTempExceedRangeValid
                && thermalSuperResolutionEnable == that.thermalSuperResolutionEnable
                && isothermEnable == that.isothermEnable
                && isothermRoiBasedRenderingEnable == that.isothermRoiBasedRenderingEnable
                && isothermColorRenderingType == that.isothermColorRenderingType
                && isothermLowerThreshold == that.isothermLowerThreshold
                && isothermUpperThreshold == that.isothermUpperThreshold
                && Objects.equals(payloadIndex, that.payloadIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                payloadIndex, cameraMode, recordTime, recordingState, remainRecordDuration,
                photoState, remainPhotoNum, intervalPhotoInterval,
                videoResolution, linkageZoomState, photoSize, videoResolutionWide,
                videoResolutionZoom, photoSizeWide, photoSizeZoom, photoFormat,
                thermalTempExceedRangeValid, thermalSuperResolutionEnable,
                isothermEnable, isothermRoiBasedRenderingEnable, isothermColorRenderingType,
                isothermLowerThreshold, isothermUpperThreshold);
    }

    @Override
    public String toString() {
        return "CameraState{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", cameraMode=" + cameraMode
                + ", recordTime=" + recordTime
                + ", recordingState=" + recordingState
                + ", remainRecordDuration=" + remainRecordDuration
                + ", photoState=" + photoState
                + ", remainPhotoNum=" + remainPhotoNum
                + ", intervalPhotoInterval=" + intervalPhotoInterval
                + ", videoResolution=" + videoResolution
                + ", linkageZoomState=" + linkageZoomState
                + ", photoSize=" + photoSize
                + ", videoResolutionWide=" + videoResolutionWide
                + ", videoResolutionZoom=" + videoResolutionZoom
                + ", photoSizeWide=" + photoSizeWide
                + ", photoSizeZoom=" + photoSizeZoom
                + ", photoFormat=" + photoFormat
                + ", thermalTempExceedRangeValid=" + thermalTempExceedRangeValid
                + ", thermalSuperResolutionEnable=" + thermalSuperResolutionEnable
                + ", isothermEnable=" + isothermEnable
                + ", isothermRoiBasedRenderingEnable=" + isothermRoiBasedRenderingEnable
                + ", isothermColorRenderingType=" + isothermColorRenderingType
                + ", isothermLowerThreshold=" + isothermLowerThreshold
                + ", isothermUpperThreshold=" + isothermUpperThreshold
                + '}';
    }
}
