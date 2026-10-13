// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 飞机相机水印设置推送（WebSocket topic_drone_camera_watermark_settings）。EA340 消息推送补全（Phase D）。
 * 注意：与 DRC 侧现有 {@code CameraWatermarkSettings}（listenDrcCameraWatermarkSettings）字段不同
 * （缺少 payload_index / manufacturer_enable），不要混用。
 */
public final class DroneCameraWatermarkSettingsPush {

    private long globalEnable;
    private long droneTypeEnable;
    private long droneSnEnable;
    private long datetimeEnable;
    private long gpsEnable;
    private long userCustomStringEnable;
    private String userCustomString;
    private long layout;

    public DroneCameraWatermarkSettingsPush() {
        this.userCustomString = "";
    }

    /**
     * 构造 DroneCameraWatermarkSettingsPush。
     *
     * @param globalEnable 水印总开关
     * @param droneTypeEnable 机型水印开关
     * @param droneSnEnable 机身序列号水印开关
     * @param datetimeEnable 拍摄时间水印开关
     * @param gpsEnable GPS 位置水印开关
     * @param userCustomStringEnable 自定义文本水印开关
     * @param userCustomString 自定义文本内容
     * @param layout 水印布局
     */
    public DroneCameraWatermarkSettingsPush(
            long globalEnable,
            long droneTypeEnable,
            long droneSnEnable,
            long datetimeEnable,
            long gpsEnable,
            long userCustomStringEnable,
            String userCustomString,
            long layout) {
        this.globalEnable = globalEnable;
        this.droneTypeEnable = droneTypeEnable;
        this.droneSnEnable = droneSnEnable;
        this.datetimeEnable = datetimeEnable;
        this.gpsEnable = gpsEnable;
        this.userCustomStringEnable = userCustomStringEnable;
        this.userCustomString = userCustomString != null ? userCustomString : "";
        this.layout = layout;
    }

    /** 水印总开关 */
    public long getGlobalEnable() {
        return globalEnable;
    }

    /** 水印总开关 */
    public void setGlobalEnable(long globalEnable) {
        this.globalEnable = globalEnable;
    }

    /** 机型水印开关 */
    public long getDroneTypeEnable() {
        return droneTypeEnable;
    }

    /** 机型水印开关 */
    public void setDroneTypeEnable(long droneTypeEnable) {
        this.droneTypeEnable = droneTypeEnable;
    }

    /** 机身序列号水印开关 */
    public long getDroneSnEnable() {
        return droneSnEnable;
    }

    /** 机身序列号水印开关 */
    public void setDroneSnEnable(long droneSnEnable) {
        this.droneSnEnable = droneSnEnable;
    }

    /** 拍摄时间水印开关 */
    public long getDatetimeEnable() {
        return datetimeEnable;
    }

    /** 拍摄时间水印开关 */
    public void setDatetimeEnable(long datetimeEnable) {
        this.datetimeEnable = datetimeEnable;
    }

    /** GPS 位置水印开关 */
    public long getGpsEnable() {
        return gpsEnable;
    }

    /** GPS 位置水印开关 */
    public void setGpsEnable(long gpsEnable) {
        this.gpsEnable = gpsEnable;
    }

    /** 自定义文本水印开关 */
    public long getUserCustomStringEnable() {
        return userCustomStringEnable;
    }

    /** 自定义文本水印开关 */
    public void setUserCustomStringEnable(long userCustomStringEnable) {
        this.userCustomStringEnable = userCustomStringEnable;
    }

    /** 自定义文本内容 */
    public String getUserCustomString() {
        return userCustomString;
    }

    /** 自定义文本内容 */
    public void setUserCustomString(String userCustomString) {
        this.userCustomString = userCustomString;
    }

    /** 水印布局 */
    public long getLayout() {
        return layout;
    }

    /** 水印布局 */
    public void setLayout(long layout) {
        this.layout = layout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneCameraWatermarkSettingsPush that = (DroneCameraWatermarkSettingsPush) o;
        return globalEnable == that.globalEnable
                && droneTypeEnable == that.droneTypeEnable
                && droneSnEnable == that.droneSnEnable
                && datetimeEnable == that.datetimeEnable
                && gpsEnable == that.gpsEnable
                && userCustomStringEnable == that.userCustomStringEnable
                && layout == that.layout
                && Objects.equals(userCustomString, that.userCustomString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(globalEnable, droneTypeEnable, droneSnEnable, datetimeEnable, gpsEnable,
                userCustomStringEnable, userCustomString, layout);
    }

    @Override
    public String toString() {
        return "DroneCameraWatermarkSettingsPush{globalEnable=" + globalEnable
                + ", droneTypeEnable=" + droneTypeEnable
                + ", droneSnEnable=" + droneSnEnable
                + ", datetimeEnable=" + datetimeEnable
                + ", gpsEnable=" + gpsEnable
                + ", userCustomStringEnable=" + userCustomStringEnable
                + ", userCustomString='" + userCustomString + '\''
                + ", layout=" + layout + '}';
    }
}
