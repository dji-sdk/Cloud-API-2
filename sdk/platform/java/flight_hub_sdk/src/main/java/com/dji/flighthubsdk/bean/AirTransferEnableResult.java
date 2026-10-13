// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 空中回传设置回调结果 */
public final class AirTransferEnableResult {
    private int resultCode;
    private String deviceSn;
    private boolean airTransferEnable;
    public AirTransferEnableResult() {
    }

/**
     * 构造 AirTransferEnableResult。
     *
     * @param resultCode 设备侧返回码，0 表示设置成功
     * @param deviceSn 设备序列号
     * @param airTransferEnable 设备确认后的空中回传开关状态
     */
    public AirTransferEnableResult(int resultCode, String deviceSn, boolean airTransferEnable) {
        this.resultCode = resultCode;
        this.deviceSn = deviceSn != null ? deviceSn : "";
        this.airTransferEnable = airTransferEnable;
    }

    /** 设备侧返回码，0 表示设置成功 */
    public int getResultCode() { return resultCode; }
    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** 设备确认后的空中回传开关状态 */
    public boolean isAirTransferEnable() { return airTransferEnable; }

    /** 设备侧返回码，0 表示设置成功 */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    /** 设备序列号 */
    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    /** 设备确认后的空中回传开关状态 */
    public void setAirTransferEnable(boolean airTransferEnable) {
        this.airTransferEnable = airTransferEnable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AirTransferEnableResult that = (AirTransferEnableResult) o;
        return resultCode == that.resultCode
                && Objects.equals(deviceSn, that.deviceSn)
                && airTransferEnable == that.airTransferEnable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultCode, deviceSn, airTransferEnable);
    }

    @Override
    public String toString() {
        return "AirTransferEnableResult{resultCode=" + resultCode + ", deviceSn='" + deviceSn + '\'' + ", airTransferEnable=" + airTransferEnable + '}';
    }
}
