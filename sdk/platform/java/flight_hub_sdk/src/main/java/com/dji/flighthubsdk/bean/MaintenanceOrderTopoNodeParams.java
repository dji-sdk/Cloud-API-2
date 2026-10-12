// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 创建报修订单 - 设备拓扑节点参数 */
public class MaintenanceOrderTopoNodeParams {
    private String sn = "";
    private String deviceModelKey = "";
    private boolean onlineStatus;

    public MaintenanceOrderTopoNodeParams() {
    }


    /** 设备序列号 */
    public String getSn() {
        return sn;
    }

    /** @param sn 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn != null ? sn : "";
    }

    /** 设备型号Key */
    public String getDeviceModelKey() {
        return deviceModelKey;
    }

    /** @param deviceModelKey 设备型号Key */
    public void setDeviceModelKey(String deviceModelKey) {
        this.deviceModelKey = deviceModelKey != null ? deviceModelKey : "";
    }

    /** 在线状态 */
    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    /** @param onlineStatus 在线状态 */
    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaintenanceOrderTopoNodeParams that = (MaintenanceOrderTopoNodeParams) o;
        return Objects.equals(sn, that.sn)
                && Objects.equals(deviceModelKey, that.deviceModelKey)
                && onlineStatus == that.onlineStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, deviceModelKey, onlineStatus);
    }

    @Override
    public String toString() {
        return "MaintenanceOrderTopoNodeParams{sn='" + sn + '\'' + ", deviceModelKey='" + deviceModelKey + '\'' + ", onlineStatus=" + onlineStatus + '}';
    }
}
