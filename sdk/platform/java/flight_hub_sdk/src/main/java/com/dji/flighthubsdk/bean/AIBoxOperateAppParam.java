// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** AI Box 操作应用请求参数 */
public class AIBoxOperateAppParam {
    private String deviceSn = "";
    private String boxSn = "";
    private String appId = "";
    private String action = "";

    public AIBoxOperateAppParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }

    /** AI Box序列号 */
    public String getBoxSn() { return boxSn; }
    /** @param boxSn AI Box序列号 */
    public void setBoxSn(String boxSn) { this.boxSn = boxSn != null ? boxSn : ""; }

    /** 应用ID */
    public String getAppId() { return appId; }
    /** @param appId 应用ID */
    public void setAppId(String appId) { this.appId = appId != null ? appId : ""; }

    /** 操作动作。枚举："uninstall"=卸载, "start"=启动, "stop"=停止, "clear"=清除数据 */
    public String getAction() { return action; }
    /** @param action 操作动作。枚举："uninstall"=卸载, "start"=启动, "stop"=停止, "clear"=清除数据 */
    public void setAction(String action) { this.action = action != null ? action : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxOperateAppParam that = (AIBoxOperateAppParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(boxSn, that.boxSn)
                && Objects.equals(appId, that.appId)
                && Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, boxSn, appId, action);
    }

    @Override
    public String toString() {
        return "AIBoxOperateAppParam{"
                + "deviceSn='" + deviceSn + '\''
                + ", boxSn='" + boxSn + '\''
                + ", appId='" + appId + '\''
                + ", action='" + action + '\''
                + '}';
    }
}
