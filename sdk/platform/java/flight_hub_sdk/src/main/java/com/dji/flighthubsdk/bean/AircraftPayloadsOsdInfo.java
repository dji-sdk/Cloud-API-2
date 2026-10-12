// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 单路负载 OSD。
 * <p><b>注意：当前版本后端暂不支持 topic_drone_payloads_osd_info 推送，该监听接口无法收到回调。</b></p>
 * <p>替代方案：</p>
 * <ul>
 *   <li>云台姿态（gimbalPitch/Roll/Yaw）→ {@code drone.getDroneOsdModule().listenAircraftPayloadsGimbalAttitude(...)}</li>
 *   <li>激光测距（measureTarget*）→ {@code drone.getCameraControlModule().listenCameraMeasureTarget(payloadIndex, ...)}</li>
 *   <li>变焦倍率（zoomFactor）→ {@code drone.getDroneOsdModule().listenAircraftPayloadsCamerasList(...)}</li>
 *   <li>其他字段（smartTrack/thermal/controlSource/firmwareVersion/sn）当前无替代接口</li>
 * </ul>
 */
public final class AircraftPayloadsOsdInfo {

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    private String payloadIndex;
    /** 云台俯仰轴角度, [-180, 180]度; */
    private double gimbalPitch;
    /** 云台横滚轴角度,[-180, 180]度; */
    private double gimbalRoll;
    /** 云台朝向角度/云台偏航轴角度,[-180, 180]度; */
    private double gimbalYaw;
    /** 激光测距目标海拔(单位: m); */
    private double measureTargetAltitude;
    /** 激光测距目标经度,[-180,180]度; */
    private double measureTargetLongitude;
    /** 激光测距目标纬度,[-90, 90]度; */
    private double measureTargetLatitude;
    /** 测距距离(单位: m); */
    private double measureTargetDistance;
    /** 测距状态, 0: NORMAL, 1: TOO_CLOSE, 2: TOO_FAR, 3: NO_SIGNAL; */
    private double measureTargetErrorState;
    /** smart track 点信息; */
    private List<SmartTrackPointInfo> smartTrackPoints;
    /** 当前变焦倍率; */
    private double zoomFactor;
    /** 调色盘样式; */
    private int thermalCurrentPaletteStyle;
    /** 增益模式; */
    private int thermalGainMode;
    /** 全局画面中测量的最低温度(摄氏度); */
    private double thermalGlobalTemperatureMin;
    /** 全局画面中测量的最高温度(摄氏度); */
    private double thermalGlobalTemperatureMax;
    /** 是否开启等温线, {0: 关闭, 1: 开启}; */
    private int thermalIsothermState;
    /** 测温区间下限(摄氏度), 仅启用等温线功能后有效; */
    private int thermalIsothermLowerLimit;
    /** 测温区间上限(摄氏度), 仅启用等温线功能后有效; */
    private int thermalIsothermUpperLimit;
    /** 设备支持的调色盘样式集合, 不同设备所支持的样式能力有所不同", */
    private List<Integer> thermalSupportedPaletteStyles;
    /** 负载控制权; */
    private String controlSource;
    /** 负载固件版本; */
    private String firmwareVersion;
    /** 负载sn; */
    private String sn;
    public AircraftPayloadsOsdInfo() {
    }

/**
     * 构造 AircraftPayloadsOsdInfo。
     */
    public AircraftPayloadsOsdInfo(
            String payloadIndex,
            double gimbalPitch,
            double gimbalRoll,
            double gimbalYaw,
            double measureTargetAltitude,
            double measureTargetLongitude,
            double measureTargetLatitude,
            double measureTargetDistance,
            double measureTargetErrorState,
            List<SmartTrackPointInfo> smartTrackPoints,
            double zoomFactor,
            int thermalCurrentPaletteStyle,
            int thermalGainMode,
            double thermalGlobalTemperatureMin,
            double thermalGlobalTemperatureMax,
            int thermalIsothermState,
            int thermalIsothermLowerLimit,
            int thermalIsothermUpperLimit,
            List<Integer> thermalSupportedPaletteStyles,
            String controlSource,
            String firmwareVersion,
            String sn) {
        this.payloadIndex = payloadIndex != null ? payloadIndex : "";
        this.gimbalPitch = gimbalPitch;
        this.gimbalRoll = gimbalRoll;
        this.gimbalYaw = gimbalYaw;
        this.measureTargetAltitude = measureTargetAltitude;
        this.measureTargetLongitude = measureTargetLongitude;
        this.measureTargetLatitude = measureTargetLatitude;
        this.measureTargetDistance = measureTargetDistance;
        this.measureTargetErrorState = measureTargetErrorState;
        this.smartTrackPoints = smartTrackPoints != null ? smartTrackPoints : Collections.emptyList();
        this.zoomFactor = zoomFactor;
        this.thermalCurrentPaletteStyle = thermalCurrentPaletteStyle;
        this.thermalGainMode = thermalGainMode;
        this.thermalGlobalTemperatureMin = thermalGlobalTemperatureMin;
        this.thermalGlobalTemperatureMax = thermalGlobalTemperatureMax;
        this.thermalIsothermState = thermalIsothermState;
        this.thermalIsothermLowerLimit = thermalIsothermLowerLimit;
        this.thermalIsothermUpperLimit = thermalIsothermUpperLimit;
        this.thermalSupportedPaletteStyles =
                thermalSupportedPaletteStyles != null ? thermalSupportedPaletteStyles : Collections.emptyList();
        this.controlSource = controlSource != null ? controlSource : "";
        this.firmwareVersion = firmwareVersion != null ? firmwareVersion : "";
        this.sn = sn != null ? sn : "";
    }

    /** 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public String getPayloadIndex() {
        return payloadIndex;
    }

    /** @param payloadIndex 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0" */
    public void setPayloadIndex(String payloadIndex) {
        this.payloadIndex = payloadIndex;
    }

    /** 云台俯仰轴角度, [-180, 180]度; */
    public double getGimbalPitch() {
        return gimbalPitch;
    }

    /** @param gimbalPitch 云台俯仰轴角度, [-180, 180]度; */
    public void setGimbalPitch(double gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    /** 云台横滚轴角度,[-180, 180]度; */
    public double getGimbalRoll() {
        return gimbalRoll;
    }

    /** @param gimbalRoll 云台横滚轴角度,[-180, 180]度; */
    public void setGimbalRoll(double gimbalRoll) {
        this.gimbalRoll = gimbalRoll;
    }

    /** 云台朝向角度/云台偏航轴角度,[-180, 180]度; */
    public double getGimbalYaw() {
        return gimbalYaw;
    }

    /** @param gimbalYaw 云台朝向角度/云台偏航轴角度,[-180, 180]度; */
    public void setGimbalYaw(double gimbalYaw) {
        this.gimbalYaw = gimbalYaw;
    }

    /** 激光测距目标海拔(单位: m); */
    public double getMeasureTargetAltitude() {
        return measureTargetAltitude;
    }

    /** @param measureTargetAltitude 激光测距目标海拔(单位: m); */
    public void setMeasureTargetAltitude(double measureTargetAltitude) {
        this.measureTargetAltitude = measureTargetAltitude;
    }

    /** 激光测距目标经度,[-180,180]度; */
    public double getMeasureTargetLongitude() {
        return measureTargetLongitude;
    }

    /** @param measureTargetLongitude 激光测距目标经度,[-180,180]度; */
    public void setMeasureTargetLongitude(double measureTargetLongitude) {
        this.measureTargetLongitude = measureTargetLongitude;
    }

    /** 激光测距目标纬度,[-90, 90]度; */
    public double getMeasureTargetLatitude() {
        return measureTargetLatitude;
    }

    /** @param measureTargetLatitude 激光测距目标纬度,[-90, 90]度; */
    public void setMeasureTargetLatitude(double measureTargetLatitude) {
        this.measureTargetLatitude = measureTargetLatitude;
    }

    /** 测距距离(单位: m); */
    public double getMeasureTargetDistance() {
        return measureTargetDistance;
    }

    /** @param measureTargetDistance 测距距离(单位: m); */
    public void setMeasureTargetDistance(double measureTargetDistance) {
        this.measureTargetDistance = measureTargetDistance;
    }

    /** 测距状态, 0: NORMAL, 1: TOO_CLOSE, 2: TOO_FAR, 3: NO_SIGNAL; */
    public double getMeasureTargetErrorState() {
        return measureTargetErrorState;
    }

    /** @param measureTargetErrorState 测距状态, 0: NORMAL, 1: TOO_CLOSE, 2: TOO_FAR, 3: NO_SIGNAL; */
    public void setMeasureTargetErrorState(double measureTargetErrorState) {
        this.measureTargetErrorState = measureTargetErrorState;
    }

    /** smart track 点信息; */
    public List<SmartTrackPointInfo> getSmartTrackPoints() {
        return smartTrackPoints;
    }

    /** @param smartTrackPoints smart track 点信息; */
    public void setSmartTrackPoints(List<SmartTrackPointInfo> smartTrackPoints) {
        this.smartTrackPoints = smartTrackPoints;
    }

    /** 当前变焦倍率; */
    public double getZoomFactor() {
        return zoomFactor;
    }

    /** @param zoomFactor 当前变焦倍率; */
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    /** 调色盘样式; */
    public int getThermalCurrentPaletteStyle() {
        return thermalCurrentPaletteStyle;
    }

    /** @param thermalCurrentPaletteStyle 调色盘样式; */
    public void setThermalCurrentPaletteStyle(int thermalCurrentPaletteStyle) {
        this.thermalCurrentPaletteStyle = thermalCurrentPaletteStyle;
    }

    /** 增益模式; */
    public int getThermalGainMode() {
        return thermalGainMode;
    }

    /** @param thermalGainMode 增益模式; */
    public void setThermalGainMode(int thermalGainMode) {
        this.thermalGainMode = thermalGainMode;
    }

    /** 全局画面中测量的最低温度(摄氏度); */
    public double getThermalGlobalTemperatureMin() {
        return thermalGlobalTemperatureMin;
    }

    /** @param thermalGlobalTemperatureMin 全局画面中测量的最低温度(摄氏度); */
    public void setThermalGlobalTemperatureMin(double thermalGlobalTemperatureMin) {
        this.thermalGlobalTemperatureMin = thermalGlobalTemperatureMin;
    }

    /** 全局画面中测量的最高温度(摄氏度); */
    public double getThermalGlobalTemperatureMax() {
        return thermalGlobalTemperatureMax;
    }

    /** @param thermalGlobalTemperatureMax 全局画面中测量的最高温度(摄氏度); */
    public void setThermalGlobalTemperatureMax(double thermalGlobalTemperatureMax) {
        this.thermalGlobalTemperatureMax = thermalGlobalTemperatureMax;
    }

    /** 是否开启等温线, {0: 关闭, 1: 开启}; */
    public int getThermalIsothermState() {
        return thermalIsothermState;
    }

    /** @param thermalIsothermState 是否开启等温线, {0: 关闭, 1: 开启}; */
    public void setThermalIsothermState(int thermalIsothermState) {
        this.thermalIsothermState = thermalIsothermState;
    }

    /** 测温区间下限(摄氏度), 仅启用等温线功能后有效; */
    public int getThermalIsothermLowerLimit() {
        return thermalIsothermLowerLimit;
    }

    /** @param thermalIsothermLowerLimit 测温区间下限(摄氏度), 仅启用等温线功能后有效; */
    public void setThermalIsothermLowerLimit(int thermalIsothermLowerLimit) {
        this.thermalIsothermLowerLimit = thermalIsothermLowerLimit;
    }

    /** 测温区间上限(摄氏度), 仅启用等温线功能后有效; */
    public int getThermalIsothermUpperLimit() {
        return thermalIsothermUpperLimit;
    }

    /** @param thermalIsothermUpperLimit 测温区间上限(摄氏度), 仅启用等温线功能后有效; */
    public void setThermalIsothermUpperLimit(int thermalIsothermUpperLimit) {
        this.thermalIsothermUpperLimit = thermalIsothermUpperLimit;
    }

    /** 设备支持的调色盘样式集合, 不同设备所支持的样式能力有所不同", */
    public List<Integer> getThermalSupportedPaletteStyles() {
        return thermalSupportedPaletteStyles;
    }

    /** @param thermalSupportedPaletteStyles 设备支持的调色盘样式集合, 不同设备所支持的样式能力有所不同", */
    public void setThermalSupportedPaletteStyles(List<Integer> thermalSupportedPaletteStyles) {
        this.thermalSupportedPaletteStyles = thermalSupportedPaletteStyles;
    }

    /** 负载控制权; */
    public String getControlSource() {
        return controlSource;
    }

    /** @param controlSource 负载控制权; */
    public void setControlSource(String controlSource) {
        this.controlSource = controlSource;
    }

    /** 负载固件版本; */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /** @param firmwareVersion 负载固件版本; */
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    /** 负载sn; */
    public String getSn() {
        return sn;
    }

    /** @param sn 负载sn; */
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
        AircraftPayloadsOsdInfo that = (AircraftPayloadsOsdInfo) o;
        return Double.compare(that.gimbalPitch, gimbalPitch) == 0
                && Double.compare(that.gimbalRoll, gimbalRoll) == 0
                && Double.compare(that.gimbalYaw, gimbalYaw) == 0
                && Double.compare(that.measureTargetAltitude, measureTargetAltitude) == 0
                && Double.compare(that.measureTargetLongitude, measureTargetLongitude) == 0
                && Double.compare(that.measureTargetLatitude, measureTargetLatitude) == 0
                && Double.compare(that.measureTargetDistance, measureTargetDistance) == 0
                && Double.compare(that.measureTargetErrorState, measureTargetErrorState) == 0
                && Double.compare(that.zoomFactor, zoomFactor) == 0
                && thermalCurrentPaletteStyle == that.thermalCurrentPaletteStyle
                && thermalGainMode == that.thermalGainMode
                && Double.compare(that.thermalGlobalTemperatureMin, thermalGlobalTemperatureMin) == 0
                && Double.compare(that.thermalGlobalTemperatureMax, thermalGlobalTemperatureMax) == 0
                && thermalIsothermState == that.thermalIsothermState
                && thermalIsothermLowerLimit == that.thermalIsothermLowerLimit
                && thermalIsothermUpperLimit == that.thermalIsothermUpperLimit
                && Objects.equals(payloadIndex, that.payloadIndex)
                && Objects.equals(smartTrackPoints, that.smartTrackPoints)
                && Objects.equals(thermalSupportedPaletteStyles, that.thermalSupportedPaletteStyles)
                && Objects.equals(controlSource, that.controlSource)
                && Objects.equals(firmwareVersion, that.firmwareVersion)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                payloadIndex,
                gimbalPitch,
                gimbalRoll,
                gimbalYaw,
                measureTargetAltitude,
                measureTargetLongitude,
                measureTargetLatitude,
                measureTargetDistance,
                measureTargetErrorState,
                smartTrackPoints,
                zoomFactor,
                thermalCurrentPaletteStyle,
                thermalGainMode,
                thermalGlobalTemperatureMin,
                thermalGlobalTemperatureMax,
                thermalIsothermState,
                thermalIsothermLowerLimit,
                thermalIsothermUpperLimit,
                thermalSupportedPaletteStyles,
                controlSource,
                firmwareVersion,
                sn);
    }

    @Override
    public String toString() {
        return "AircraftPayloadsOsdInfo{"
                + "payloadIndex='" + payloadIndex + '\''
                + ", gimbalPitch=" + gimbalPitch
                + ", gimbalRoll=" + gimbalRoll
                + ", gimbalYaw=" + gimbalYaw
                + ", measureTargetAltitude=" + measureTargetAltitude
                + ", measureTargetLongitude=" + measureTargetLongitude
                + ", measureTargetLatitude=" + measureTargetLatitude
                + ", measureTargetDistance=" + measureTargetDistance
                + ", measureTargetErrorState=" + measureTargetErrorState
                + ", smartTrackPoints=" + smartTrackPoints
                + ", zoomFactor=" + zoomFactor
                + ", thermalCurrentPaletteStyle=" + thermalCurrentPaletteStyle
                + ", thermalGainMode=" + thermalGainMode
                + ", thermalGlobalTemperatureMin=" + thermalGlobalTemperatureMin
                + ", thermalGlobalTemperatureMax=" + thermalGlobalTemperatureMax
                + ", thermalIsothermState=" + thermalIsothermState
                + ", thermalIsothermLowerLimit=" + thermalIsothermLowerLimit
                + ", thermalIsothermUpperLimit=" + thermalIsothermUpperLimit
                + ", thermalSupportedPaletteStyles=" + thermalSupportedPaletteStyles
                + ", controlSource='" + controlSource + '\''
                + ", firmwareVersion='" + firmwareVersion + '\''
                + ", sn='" + sn + '\''
                + '}';
    }
}
