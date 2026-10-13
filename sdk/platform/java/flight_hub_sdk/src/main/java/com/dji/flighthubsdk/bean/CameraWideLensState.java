// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 广角镜头状态。
 */
public final class CameraWideLensState {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    private int wideCalibrateFarthestFocusValue;
    private int wideCalibrateNearestFocusValue;
    /** 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`） */
    private int wideExposureMode;
    /** 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`） */
    private int wideExposureValue;
    /** 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`） */
    private int wideFocusMode;
    /** 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`） */
    private int wideFocusState;
    private int wideFocusValue;
    /** 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`） */
    private int wideIso;
    private int wideMaxFocusValue;
    private int wideMinFocusValue;
    /** 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`） */
    private int wideShutterSpeed;
    private boolean wideDewarpingState;
    private boolean wideMechanicalShutterState;
    /** 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`） */
    private int wideApertureValue;
    /** 广角镜头 FOV 信息（画幅/焦距） */
    private CameraLensFovInfo wideFovInfo;
    public CameraWideLensState() {
    }

/**
     * 构造 CameraWideLensState。
     *
     * @param payloadIndex 负载索引
     * @param wideCalibrateFarthestFocusValue 广角镜头标定最远对焦值
     * @param wideCalibrateNearestFocusValue 广角镜头标定最近对焦值
     * @param wideExposureMode 广角镜头曝光模式
     * @param wideExposureValue 广角镜头曝光补偿值
     * @param wideFocusMode 广角镜头对焦模式
     * @param wideFocusState 广角镜头对焦状态
     * @param wideFocusValue 广角镜头对焦值
     * @param wideIso 广角镜头 ISO
     * @param wideMaxFocusValue 广角镜头最大对焦值
     * @param wideMinFocusValue 广角镜头最小对焦值
     * @param wideShutterSpeed 广角镜头快门速度
     * @param wideDewarpingState 广角镜头畸变矫正状态
     * @param wideMechanicalShutterState 广角镜头机械快门状态
     * @param wideApertureValue 广角镜头光圈值
     */
    public CameraWideLensState(
            String payloadIndex,
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
            boolean wideDewarpingState,
            boolean wideMechanicalShutterState,
            int wideApertureValue) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
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
        this.wideDewarpingState = wideDewarpingState;
        this.wideMechanicalShutterState = wideMechanicalShutterState;
        this.wideApertureValue = wideApertureValue;
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 广角镜头标定最远对焦值 */
    public int getWideCalibrateFarthestFocusValue() {
        return wideCalibrateFarthestFocusValue;
    }

    /** 广角镜头标定最远对焦值 */
    public void setWideCalibrateFarthestFocusValue(int wideCalibrateFarthestFocusValue) {
        this.wideCalibrateFarthestFocusValue = wideCalibrateFarthestFocusValue;
    }

    /** 广角镜头标定最近对焦值 */
    public int getWideCalibrateNearestFocusValue() {
        return wideCalibrateNearestFocusValue;
    }

    /** 广角镜头标定最近对焦值 */
    public void setWideCalibrateNearestFocusValue(int wideCalibrateNearestFocusValue) {
        this.wideCalibrateNearestFocusValue = wideCalibrateNearestFocusValue;
    }

    /** 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`） */
    public int getWideExposureMode() {
        return wideExposureMode;
    }

    /** @param wideExposureMode 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`） */
    public void setWideExposureMode(int wideExposureMode) {
        this.wideExposureMode = wideExposureMode;
    }

    /** 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`） */
    public int getWideExposureValue() {
        return wideExposureValue;
    }

    /** @param wideExposureValue 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`） */
    public void setWideExposureValue(int wideExposureValue) {
        this.wideExposureValue = wideExposureValue;
    }

    /** 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`） */
    public int getWideFocusMode() {
        return wideFocusMode;
    }

    /** @param wideFocusMode 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`） */
    public void setWideFocusMode(int wideFocusMode) {
        this.wideFocusMode = wideFocusMode;
    }

    /** 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`） */
    public int getWideFocusState() {
        return wideFocusState;
    }

    /** @param wideFocusState 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`） */
    public void setWideFocusState(int wideFocusState) {
        this.wideFocusState = wideFocusState;
    }

    /** 广角镜头对焦值 */
    public int getWideFocusValue() {
        return wideFocusValue;
    }

    /** 广角镜头对焦值 */
    public void setWideFocusValue(int wideFocusValue) {
        this.wideFocusValue = wideFocusValue;
    }

    /** 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`） */
    public int getWideIso() {
        return wideIso;
    }

    /** @param wideIso 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`） */
    public void setWideIso(int wideIso) {
        this.wideIso = wideIso;
    }

    /** 广角镜头最大对焦值 */
    public int getWideMaxFocusValue() {
        return wideMaxFocusValue;
    }

    /** 广角镜头最大对焦值 */
    public void setWideMaxFocusValue(int wideMaxFocusValue) {
        this.wideMaxFocusValue = wideMaxFocusValue;
    }

    /** 广角镜头最小对焦值 */
    public int getWideMinFocusValue() {
        return wideMinFocusValue;
    }

    /** 广角镜头最小对焦值 */
    public void setWideMinFocusValue(int wideMinFocusValue) {
        this.wideMinFocusValue = wideMinFocusValue;
    }

    /** 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`） */
    public int getWideShutterSpeed() {
        return wideShutterSpeed;
    }

    /** @param wideShutterSpeed 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`） */
    public void setWideShutterSpeed(int wideShutterSpeed) {
        this.wideShutterSpeed = wideShutterSpeed;
    }

    /** 广角镜头畸变矫正状态 */
    public boolean isWideDewarpingState() {
        return wideDewarpingState;
    }

    /** 广角镜头畸变矫正状态 */
    public void setWideDewarpingState(boolean wideDewarpingState) {
        this.wideDewarpingState = wideDewarpingState;
    }

    /** 广角镜头机械快门状态 */
    public boolean isWideMechanicalShutterState() {
        return wideMechanicalShutterState;
    }

    /** 广角镜头机械快门状态 */
    public void setWideMechanicalShutterState(boolean wideMechanicalShutterState) {
        this.wideMechanicalShutterState = wideMechanicalShutterState;
    }

    /** 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`） */
    public int getWideApertureValue() {
        return wideApertureValue;
    }

    /** @param wideApertureValue 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`） */
    public void setWideApertureValue(int wideApertureValue) {
        this.wideApertureValue = wideApertureValue;
    }

    /** 广角镜头 FOV 信息（画幅/焦距） */
    public CameraLensFovInfo getWideFovInfo() { return wideFovInfo; }
    /** @param wideFovInfo 广角镜头 FOV 信息（画幅/焦距） */
    public void setWideFovInfo(CameraLensFovInfo v) { this.wideFovInfo = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CameraWideLensState that = (CameraWideLensState) o;
        return wideCalibrateFarthestFocusValue == that.wideCalibrateFarthestFocusValue
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
                && wideDewarpingState == that.wideDewarpingState
                && wideMechanicalShutterState == that.wideMechanicalShutterState
                && wideApertureValue == that.wideApertureValue
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(wideFovInfo, that.wideFovInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                payloadIndex,
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
                wideDewarpingState,
                wideMechanicalShutterState,
                wideApertureValue,
                wideFovInfo);
    }

    @Override
    public String toString() {
        return "CameraWideLensState{"
                + "payloadIndex='" + payloadIndex + '\''
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
                + ", wideDewarpingState=" + wideDewarpingState
                + ", wideMechanicalShutterState=" + wideMechanicalShutterState
                + ", wideApertureValue=" + wideApertureValue
                + ", wideFovInfo=" + wideFovInfo
                + '}';
    }
}
