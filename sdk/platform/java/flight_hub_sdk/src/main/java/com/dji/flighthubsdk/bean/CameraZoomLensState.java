// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 变焦镜头状态。
 */
public final class CameraZoomLensState {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int zoomCalibrateFarthestFocusValue;
    private int zoomCalibrateNearestFocusValue;
    /** 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`） */
    private int zoomExposureMode;
    /** 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`） */
    private int zoomExposureValue;
    private double zoomFactor;
    /** 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`） */
    private int zoomFocusMode;
    /** 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`） */
    private int zoomFocusState;
    private int zoomFocusValue;
    /** 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`） */
    private int zoomIso;
    private int zoomMaxFocusValue;
    private int zoomMinFocusValue;
    /** 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`） */
    private int zoomShutterSpeed;
    /** 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`） */
    private int zoomApertureValue;
    /** 变焦镜头 FOV 信息（画幅/焦距） */
    private CameraLensFovInfo zoomFovInfo;
    public CameraZoomLensState() {
    }

/**
     * 构造 CameraZoomLensState。
     *
     * @param payloadIndex 负载索引
     * @param zoomCalibrateFarthestFocusValue 变焦镜头标定最远对焦值
     * @param zoomCalibrateNearestFocusValue 变焦镜头标定最近对焦值
     * @param zoomExposureMode 变焦镜头曝光模式
     * @param zoomExposureValue 变焦镜头曝光补偿值
     * @param zoomFactor 变焦倍数
     * @param zoomFocusMode 变焦镜头对焦模式
     * @param zoomFocusState 变焦镜头对焦状态
     * @param zoomFocusValue 变焦镜头对焦值
     * @param zoomIso 变焦镜头 ISO
     * @param zoomMaxFocusValue 变焦镜头最大对焦值
     * @param zoomMinFocusValue 变焦镜头最小对焦值
     * @param zoomShutterSpeed 变焦镜头快门速度
     * @param zoomApertureValue 变焦镜头光圈值
     */
    public CameraZoomLensState(
            String payloadIndex,
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
            int zoomShutterSpeed,
            int zoomApertureValue) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
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
        this.zoomApertureValue = zoomApertureValue;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 变焦镜头标定最远对焦值 */
    public int getZoomCalibrateFarthestFocusValue() {
        return zoomCalibrateFarthestFocusValue;
    }

    /** 变焦镜头标定最远对焦值 */
    public void setZoomCalibrateFarthestFocusValue(int zoomCalibrateFarthestFocusValue) {
        this.zoomCalibrateFarthestFocusValue = zoomCalibrateFarthestFocusValue;
    }

    /** 变焦镜头标定最近对焦值 */
    public int getZoomCalibrateNearestFocusValue() {
        return zoomCalibrateNearestFocusValue;
    }

    /** 变焦镜头标定最近对焦值 */
    public void setZoomCalibrateNearestFocusValue(int zoomCalibrateNearestFocusValue) {
        this.zoomCalibrateNearestFocusValue = zoomCalibrateNearestFocusValue;
    }

    /** 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`） */
    public int getZoomExposureMode() {
        return zoomExposureMode;
    }

    /** @param zoomExposureMode 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`） */
    public void setZoomExposureMode(int zoomExposureMode) {
        this.zoomExposureMode = zoomExposureMode;
    }

    /** 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`） */
    public int getZoomExposureValue() {
        return zoomExposureValue;
    }

    /** @param zoomExposureValue 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`） */
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

    /** 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`） */
    public int getZoomFocusMode() {
        return zoomFocusMode;
    }

    /** @param zoomFocusMode 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`） */
    public void setZoomFocusMode(int zoomFocusMode) {
        this.zoomFocusMode = zoomFocusMode;
    }

    /** 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`） */
    public int getZoomFocusState() {
        return zoomFocusState;
    }

    /** @param zoomFocusState 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`） */
    public void setZoomFocusState(int zoomFocusState) {
        this.zoomFocusState = zoomFocusState;
    }

    /** 变焦镜头对焦值 */
    public int getZoomFocusValue() {
        return zoomFocusValue;
    }

    /** 变焦镜头对焦值 */
    public void setZoomFocusValue(int zoomFocusValue) {
        this.zoomFocusValue = zoomFocusValue;
    }

    /** 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`） */
    public int getZoomIso() {
        return zoomIso;
    }

    /** @param zoomIso 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`） */
    public void setZoomIso(int zoomIso) {
        this.zoomIso = zoomIso;
    }

    /** 变焦镜头最大对焦值 */
    public int getZoomMaxFocusValue() {
        return zoomMaxFocusValue;
    }

    /** 变焦镜头最大对焦值 */
    public void setZoomMaxFocusValue(int zoomMaxFocusValue) {
        this.zoomMaxFocusValue = zoomMaxFocusValue;
    }

    /** 变焦镜头最小对焦值 */
    public int getZoomMinFocusValue() {
        return zoomMinFocusValue;
    }

    /** 变焦镜头最小对焦值 */
    public void setZoomMinFocusValue(int zoomMinFocusValue) {
        this.zoomMinFocusValue = zoomMinFocusValue;
    }

    /** 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`） */
    public int getZoomShutterSpeed() {
        return zoomShutterSpeed;
    }

    /** @param zoomShutterSpeed 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`） */
    public void setZoomShutterSpeed(int zoomShutterSpeed) {
        this.zoomShutterSpeed = zoomShutterSpeed;
    }

    /** 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`） */
    public int getZoomApertureValue() {
        return zoomApertureValue;
    }

    /** @param zoomApertureValue 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`） */
    public void setZoomApertureValue(int zoomApertureValue) {
        this.zoomApertureValue = zoomApertureValue;
    }

    /** 变焦镜头 FOV 信息（画幅/焦距） */
    public CameraLensFovInfo getZoomFovInfo() { return zoomFovInfo; }
    /** @param zoomFovInfo 变焦镜头 FOV 信息（画幅/焦距） */
    public void setZoomFovInfo(CameraLensFovInfo v) { this.zoomFovInfo = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraZoomLensState that = (CameraZoomLensState) o;
        return zoomCalibrateFarthestFocusValue == that.zoomCalibrateFarthestFocusValue
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
                && zoomApertureValue == that.zoomApertureValue
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(zoomFovInfo, that.zoomFovInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                payloadIndex,
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
                zoomShutterSpeed,
                zoomApertureValue,
                zoomFovInfo);
    }

    @Override
    public String toString() {
        return "CameraZoomLensState{"
                + "payloadIndex='" + payloadIndex + '\''
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
                + ", zoomApertureValue=" + zoomApertureValue
                + ", zoomFovInfo=" + zoomFovInfo
                + '}';
    }
}
