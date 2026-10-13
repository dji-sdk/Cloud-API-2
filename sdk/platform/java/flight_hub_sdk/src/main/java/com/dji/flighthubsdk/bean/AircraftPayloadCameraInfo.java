// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 单路负载相机 OSD。
 * <p>{@link #getCameraMode()} 为与协议一致的整型模式码，可与
 * {@link com.dji.flighthubsdk.value.CameraMode#fromValue(int)} 对照。</p>
 */
public final class AircraftPayloadCameraInfo {

    /** "payload_index": "81-0-0"; */
    private String payloadIndex;
    /** 相机模式, {0: 拍照, 1: 录像, 2: 低光智能, 3: 全景拍照, -1: 不支持的模式}; */
    private int cameraMode;
    private int irMeteringMode;
    private CameraIrMeteringPoint irMeteringPoint;
    private double irZoomFactor;
    private CameraLiveviewWorldRegion liveviewWorldRegion;
    private int photoState;
    private List<String> photoStorageSettings;
    /** 视频录制时长, 单位：秒(s); */
    private int recordTime;
    private int recordingState;
    /** 剩余拍照张数; */
    private int remainPhotoNum;
    private int remainRecordDuration;
    private boolean screenSplitEnable;
    private int wideCalibrateFarthestFocusValue;
    private int wideCalibrateNearestFocusValue;
    private int wideExposureMode;
    private int wideExposureValue;
    private int wideFocusMode;
    private int wideFocusState;
    private int wideFocusValue;
    private int wideIso;
    private int wideMaxFocusValue;
    private int wideMinFocusValue;
    private int wideShutterSpeed;
    private int zoomCalibrateFarthestFocusValue;
    private int zoomCalibrateNearestFocusValue;
    private int zoomExposureMode;
    private int zoomExposureValue;
    private double zoomFactor;
    private int zoomFocusMode;
    private int zoomFocusState;
    private int zoomFocusValue;
    private int zoomIso;
    private int zoomMaxFocusValue;
    private int zoomMinFocusValue;
    private int zoomShutterSpeed;
    public AircraftPayloadCameraInfo() {
    }

/**
     * 构造 AircraftPayloadCameraInfo。
     */
    public AircraftPayloadCameraInfo(
            String payloadIndex,
            int cameraMode,
            int irMeteringMode,
            CameraIrMeteringPoint irMeteringPoint,
            double irZoomFactor,
            CameraLiveviewWorldRegion liveviewWorldRegion,
            int photoState,
            List<String> photoStorageSettings,
            int recordTime,
            int recordingState,
            int remainPhotoNum,
            int remainRecordDuration,
            boolean screenSplitEnable,
            int wideCalibrateFarthestFocusValue,
            int wideCalibrateNearestFocusValue,
            int wideExposureMode,
            int wideExposureValue,
            int wideFocusMode,
            int wideFocusState,
            int wideFocusValue,
            int wideIso,
            int wideMaxFocusValue,
            int wideMinFocusValue,
            int wideShutterSpeed,
            int zoomCalibrateFarthestFocusValue,
            int zoomCalibrateNearestFocusValue,
            int zoomExposureMode,
            int zoomExposureValue,
            double zoomFactor,
            int zoomFocusMode,
            int zoomFocusState,
            int zoomFocusValue,
            int zoomIso,
            int zoomMaxFocusValue,
            int zoomMinFocusValue,
            int zoomShutterSpeed) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.cameraMode = cameraMode;
        this.irMeteringMode = irMeteringMode;
        this.irMeteringPoint = irMeteringPoint;
        this.irZoomFactor = irZoomFactor;
        this.liveviewWorldRegion = liveviewWorldRegion;
        this.photoState = photoState;
        this.photoStorageSettings = photoStorageSettings != null ? photoStorageSettings : Collections.emptyList();
        this.recordTime = recordTime;
        this.recordingState = recordingState;
        this.remainPhotoNum = remainPhotoNum;
        this.remainRecordDuration = remainRecordDuration;
        this.screenSplitEnable = screenSplitEnable;
        this.wideCalibrateFarthestFocusValue = wideCalibrateFarthestFocusValue;
        this.wideCalibrateNearestFocusValue = wideCalibrateNearestFocusValue;
        this.wideExposureMode = wideExposureMode;
        this.wideExposureValue = wideExposureValue;
        this.wideFocusMode = wideFocusMode;
        this.wideFocusState = wideFocusState;
        this.wideFocusValue = wideFocusValue;
        this.wideIso = wideIso;
        this.wideMaxFocusValue = wideMaxFocusValue;
        this.wideMinFocusValue = wideMinFocusValue;
        this.wideShutterSpeed = wideShutterSpeed;
        this.zoomCalibrateFarthestFocusValue = zoomCalibrateFarthestFocusValue;
        this.zoomCalibrateNearestFocusValue = zoomCalibrateNearestFocusValue;
        this.zoomExposureMode = zoomExposureMode;
        this.zoomExposureValue = zoomExposureValue;
        this.zoomFactor = zoomFactor;
        this.zoomFocusMode = zoomFocusMode;
        this.zoomFocusState = zoomFocusState;
        this.zoomFocusValue = zoomFocusValue;
        this.zoomIso = zoomIso;
        this.zoomMaxFocusValue = zoomMaxFocusValue;
        this.zoomMinFocusValue = zoomMinFocusValue;
        this.zoomShutterSpeed = zoomShutterSpeed;
    }

    /** "payload_index": "81-0-0"; */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex "payload_index": "81-0-0"; */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 相机模式, {0: 拍照, 1: 录像, 2: 低光智能, 3: 全景拍照, -1: 不支持的模式}; */
    public int getCameraMode() {
        return cameraMode;
    }

    /** @param cameraMode 相机模式, {0: 拍照, 1: 录像, 2: 低光智能, 3: 全景拍照, -1: 不支持的模式}; */
    public void setCameraMode(int cameraMode) {
        this.cameraMode = cameraMode;
    }

    /** 红外测温模式 */
    public int getIrMeteringMode() {
        return irMeteringMode;
    }

    /** 红外测温模式 */
    public void setIrMeteringMode(int irMeteringMode) {
        this.irMeteringMode = irMeteringMode;
    }

    /** 红外测温点 */
    public CameraIrMeteringPoint getIrMeteringPoint() {
        return irMeteringPoint;
    }

    /** 红外测温点 */
    public void setIrMeteringPoint(CameraIrMeteringPoint irMeteringPoint) {
        this.irMeteringPoint = irMeteringPoint;
    }

    /** 红外变焦倍数 */
    public double getIrZoomFactor() {
        return irZoomFactor;
    }

    /** 红外变焦倍数 */
    public void setIrZoomFactor(double irZoomFactor) {
        this.irZoomFactor = irZoomFactor;
    }

    /** 实时视图世界区域 */
    public CameraLiveviewWorldRegion getLiveviewWorldRegion() {
        return liveviewWorldRegion;
    }

    /** 实时视图世界区域 */
    public void setLiveviewWorldRegion(CameraLiveviewWorldRegion liveviewWorldRegion) {
        this.liveviewWorldRegion = liveviewWorldRegion;
    }

    /** 拍照状态 */
    public int getPhotoState() {
        return photoState;
    }

    /** 拍照状态 */
    public void setPhotoState(int photoState) {
        this.photoState = photoState;
    }

    /** 照片存储设置 */
    public List<String> getPhotoStorageSettings() {
        return photoStorageSettings;
    }

    /** 照片存储设置 */
    public void setPhotoStorageSettings(List<String> photoStorageSettings) {
        this.photoStorageSettings = photoStorageSettings;
    }

    /** 视频录制时长, 单位：秒(s); */
    public int getRecordTime() {
        return recordTime;
    }

    /** @param recordTime 视频录制时长, 单位：秒(s); */
    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    /** 录制状态 */
    public int getRecordingState() {
        return recordingState;
    }

    /** 录制状态 */
    public void setRecordingState(int recordingState) {
        this.recordingState = recordingState;
    }

    /** 剩余拍照张数; */
    public int getRemainPhotoNum() {
        return remainPhotoNum;
    }

    /** @param remainPhotoNum 剩余拍照张数; */
    public void setRemainPhotoNum(int remainPhotoNum) {
        this.remainPhotoNum = remainPhotoNum;
    }

    /** 剩余录制时长 */
    public int getRemainRecordDuration() {
        return remainRecordDuration;
    }

    /** 剩余录制时长 */
    public void setRemainRecordDuration(int remainRecordDuration) {
        this.remainRecordDuration = remainRecordDuration;
    }

    /** 画面分屏启用 */
    public boolean isScreenSplitEnable() {
        return screenSplitEnable;
    }

    /** 画面分屏启用 */
    public void setScreenSplitEnable(boolean screenSplitEnable) {
        this.screenSplitEnable = screenSplitEnable;
    }

    /** 广角最远对焦校准值 */
    public int getWideCalibrateFarthestFocusValue() {
        return wideCalibrateFarthestFocusValue;
    }

    /** 广角最远对焦校准值 */
    public void setWideCalibrateFarthestFocusValue(int wideCalibrateFarthestFocusValue) {
        this.wideCalibrateFarthestFocusValue = wideCalibrateFarthestFocusValue;
    }

    /** 广角最近对焦校准值 */
    public int getWideCalibrateNearestFocusValue() {
        return wideCalibrateNearestFocusValue;
    }

    /** 广角最近对焦校准值 */
    public void setWideCalibrateNearestFocusValue(int wideCalibrateNearestFocusValue) {
        this.wideCalibrateNearestFocusValue = wideCalibrateNearestFocusValue;
    }

    /** 广角曝光模式 */
    public int getWideExposureMode() {
        return wideExposureMode;
    }

    /** 广角曝光模式 */
    public void setWideExposureMode(int wideExposureMode) {
        this.wideExposureMode = wideExposureMode;
    }

    /** 广角曝光补偿 */
    public int getWideExposureValue() {
        return wideExposureValue;
    }

    /** 广角曝光补偿 */
    public void setWideExposureValue(int wideExposureValue) {
        this.wideExposureValue = wideExposureValue;
    }

    /** 广角对焦模式 */
    public int getWideFocusMode() {
        return wideFocusMode;
    }

    /** 广角对焦模式 */
    public void setWideFocusMode(int wideFocusMode) {
        this.wideFocusMode = wideFocusMode;
    }

    /** 广角对焦状态 */
    public int getWideFocusState() {
        return wideFocusState;
    }

    /** 广角对焦状态 */
    public void setWideFocusState(int wideFocusState) {
        this.wideFocusState = wideFocusState;
    }

    /** 广角对焦值 */
    public int getWideFocusValue() {
        return wideFocusValue;
    }

    /** 广角对焦值 */
    public void setWideFocusValue(int wideFocusValue) {
        this.wideFocusValue = wideFocusValue;
    }

    /** 广角ISO */
    public int getWideIso() {
        return wideIso;
    }

    /** 广角ISO */
    public void setWideIso(int wideIso) {
        this.wideIso = wideIso;
    }

    /** 广角最大对焦值 */
    public int getWideMaxFocusValue() {
        return wideMaxFocusValue;
    }

    /** 广角最大对焦值 */
    public void setWideMaxFocusValue(int wideMaxFocusValue) {
        this.wideMaxFocusValue = wideMaxFocusValue;
    }

    /** 广角最小对焦值 */
    public int getWideMinFocusValue() {
        return wideMinFocusValue;
    }

    /** 广角最小对焦值 */
    public void setWideMinFocusValue(int wideMinFocusValue) {
        this.wideMinFocusValue = wideMinFocusValue;
    }

    /** 广角快门速度 */
    public int getWideShutterSpeed() {
        return wideShutterSpeed;
    }

    /** 广角快门速度 */
    public void setWideShutterSpeed(int wideShutterSpeed) {
        this.wideShutterSpeed = wideShutterSpeed;
    }

    /** 变焦最远对焦校准值 */
    public int getZoomCalibrateFarthestFocusValue() {
        return zoomCalibrateFarthestFocusValue;
    }

    /** 变焦最远对焦校准值 */
    public void setZoomCalibrateFarthestFocusValue(int zoomCalibrateFarthestFocusValue) {
        this.zoomCalibrateFarthestFocusValue = zoomCalibrateFarthestFocusValue;
    }

    /** 变焦最近对焦校准值 */
    public int getZoomCalibrateNearestFocusValue() {
        return zoomCalibrateNearestFocusValue;
    }

    /** 变焦最近对焦校准值 */
    public void setZoomCalibrateNearestFocusValue(int zoomCalibrateNearestFocusValue) {
        this.zoomCalibrateNearestFocusValue = zoomCalibrateNearestFocusValue;
    }

    /** 变焦曝光模式 */
    public int getZoomExposureMode() {
        return zoomExposureMode;
    }

    /** 变焦曝光模式 */
    public void setZoomExposureMode(int zoomExposureMode) {
        this.zoomExposureMode = zoomExposureMode;
    }

    /** 变焦曝光补偿 */
    public int getZoomExposureValue() {
        return zoomExposureValue;
    }

    /** 变焦曝光补偿 */
    public void setZoomExposureValue(int zoomExposureValue) {
        this.zoomExposureValue = zoomExposureValue;
    }

    /** 变焦倍数 */
    public double getZoomFactor() {
        return zoomFactor;
    }

    /** 变焦倍数 */
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    /** 变焦对焦模式 */
    public int getZoomFocusMode() {
        return zoomFocusMode;
    }

    /** 变焦对焦模式 */
    public void setZoomFocusMode(int zoomFocusMode) {
        this.zoomFocusMode = zoomFocusMode;
    }

    /** 变焦对焦状态 */
    public int getZoomFocusState() {
        return zoomFocusState;
    }

    /** 变焦对焦状态 */
    public void setZoomFocusState(int zoomFocusState) {
        this.zoomFocusState = zoomFocusState;
    }

    /** 变焦对焦值 */
    public int getZoomFocusValue() {
        return zoomFocusValue;
    }

    /** 变焦对焦值 */
    public void setZoomFocusValue(int zoomFocusValue) {
        this.zoomFocusValue = zoomFocusValue;
    }

    /** 变焦ISO */
    public int getZoomIso() {
        return zoomIso;
    }

    /** 变焦ISO */
    public void setZoomIso(int zoomIso) {
        this.zoomIso = zoomIso;
    }

    /** 变焦最大对焦值 */
    public int getZoomMaxFocusValue() {
        return zoomMaxFocusValue;
    }

    /** 变焦最大对焦值 */
    public void setZoomMaxFocusValue(int zoomMaxFocusValue) {
        this.zoomMaxFocusValue = zoomMaxFocusValue;
    }

    /** 变焦最小对焦值 */
    public int getZoomMinFocusValue() {
        return zoomMinFocusValue;
    }

    /** 变焦最小对焦值 */
    public void setZoomMinFocusValue(int zoomMinFocusValue) {
        this.zoomMinFocusValue = zoomMinFocusValue;
    }

    /** 变焦快门速度 */
    public int getZoomShutterSpeed() {
        return zoomShutterSpeed;
    }

    /** 变焦快门速度 */
    public void setZoomShutterSpeed(int zoomShutterSpeed) {
        this.zoomShutterSpeed = zoomShutterSpeed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftPayloadCameraInfo that = (AircraftPayloadCameraInfo) o;
        return cameraMode == that.cameraMode
                && irMeteringMode == that.irMeteringMode
                && Double.compare(that.irZoomFactor, irZoomFactor) == 0
                && photoState == that.photoState
                && recordTime == that.recordTime
                && recordingState == that.recordingState
                && remainPhotoNum == that.remainPhotoNum
                && remainRecordDuration == that.remainRecordDuration
                && screenSplitEnable == that.screenSplitEnable
                && wideCalibrateFarthestFocusValue == that.wideCalibrateFarthestFocusValue
                && wideCalibrateNearestFocusValue == that.wideCalibrateNearestFocusValue
                && wideExposureMode == that.wideExposureMode
                && wideExposureValue == that.wideExposureValue
                && wideFocusMode == that.wideFocusMode
                && wideFocusState == that.wideFocusState
                && wideFocusValue == that.wideFocusValue
                && wideIso == that.wideIso
                && wideMaxFocusValue == that.wideMaxFocusValue
                && wideMinFocusValue == that.wideMinFocusValue
                && wideShutterSpeed == that.wideShutterSpeed
                && zoomCalibrateFarthestFocusValue == that.zoomCalibrateFarthestFocusValue
                && zoomCalibrateNearestFocusValue == that.zoomCalibrateNearestFocusValue
                && zoomExposureMode == that.zoomExposureMode
                && zoomExposureValue == that.zoomExposureValue
                && Double.compare(that.zoomFactor, zoomFactor) == 0
                && zoomFocusMode == that.zoomFocusMode
                && zoomFocusState == that.zoomFocusState
                && zoomFocusValue == that.zoomFocusValue
                && zoomIso == that.zoomIso
                && zoomMaxFocusValue == that.zoomMaxFocusValue
                && zoomMinFocusValue == that.zoomMinFocusValue
                && zoomShutterSpeed == that.zoomShutterSpeed
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(irMeteringPoint, that.irMeteringPoint)
                && Objects.equals(liveviewWorldRegion, that.liveviewWorldRegion)
                && Objects.equals(photoStorageSettings, that.photoStorageSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                payloadIndex,
                cameraMode,
                irMeteringMode,
                irMeteringPoint,
                irZoomFactor,
                liveviewWorldRegion,
                photoState,
                photoStorageSettings,
                recordTime,
                recordingState,
                remainPhotoNum,
                remainRecordDuration,
                screenSplitEnable,
                wideCalibrateFarthestFocusValue,
                wideCalibrateNearestFocusValue,
                wideExposureMode,
                wideExposureValue,
                wideFocusMode,
                wideFocusState,
                wideFocusValue,
                wideIso,
                wideMaxFocusValue,
                wideMinFocusValue,
                wideShutterSpeed,
                zoomCalibrateFarthestFocusValue,
                zoomCalibrateNearestFocusValue,
                zoomExposureMode,
                zoomExposureValue,
                zoomFactor,
                zoomFocusMode,
                zoomFocusState,
                zoomFocusValue,
                zoomIso,
                zoomMaxFocusValue,
                zoomMinFocusValue,
                zoomShutterSpeed);
    }

    @Override
    public String toString() {
        return "AircraftPayloadCameraInfo{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", cameraMode=" + cameraMode
                + ", irMeteringMode=" + irMeteringMode
                + ", irMeteringPoint=" + irMeteringPoint
                + ", irZoomFactor=" + irZoomFactor
                + ", liveviewWorldRegion=" + liveviewWorldRegion
                + ", photoState=" + photoState
                + ", photoStorageSettings=" + photoStorageSettings
                + ", recordTime=" + recordTime
                + ", recordingState=" + recordingState
                + ", remainPhotoNum=" + remainPhotoNum
                + ", remainRecordDuration=" + remainRecordDuration
                + ", screenSplitEnable=" + screenSplitEnable
                + ", wideCalibrateFarthestFocusValue=" + wideCalibrateFarthestFocusValue
                + ", wideCalibrateNearestFocusValue=" + wideCalibrateNearestFocusValue
                + ", wideExposureMode=" + wideExposureMode
                + ", wideExposureValue=" + wideExposureValue
                + ", wideFocusMode=" + wideFocusMode
                + ", wideFocusState=" + wideFocusState
                + ", wideFocusValue=" + wideFocusValue
                + ", wideIso=" + wideIso
                + ", wideMaxFocusValue=" + wideMaxFocusValue
                + ", wideMinFocusValue=" + wideMinFocusValue
                + ", wideShutterSpeed=" + wideShutterSpeed
                + ", zoomCalibrateFarthestFocusValue=" + zoomCalibrateFarthestFocusValue
                + ", zoomCalibrateNearestFocusValue=" + zoomCalibrateNearestFocusValue
                + ", zoomExposureMode=" + zoomExposureMode
                + ", zoomExposureValue=" + zoomExposureValue
                + ", zoomFactor=" + zoomFactor
                + ", zoomFocusMode=" + zoomFocusMode
                + ", zoomFocusState=" + zoomFocusState
                + ", zoomFocusValue=" + zoomFocusValue
                + ", zoomIso=" + zoomIso
                + ", zoomMaxFocusValue=" + zoomMaxFocusValue
                + ", zoomMinFocusValue=" + zoomMinFocusValue
                + ", zoomShutterSpeed=" + zoomShutterSpeed
                + '}';
    }
}
