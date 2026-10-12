// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** AI Box 获取应用列表请求参数 */
public class AIBoxFetchAppListParam {
    private String deviceSn = "";
    private String boxSn = "";

    public AIBoxFetchAppListParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }

    /** AI Box序列号 */
    public String getBoxSn() { return boxSn; }
    /** @param boxSn AI Box序列号 */
    public void setBoxSn(String boxSn) { this.boxSn = boxSn != null ? boxSn : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxFetchAppListParam that = (AIBoxFetchAppListParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(boxSn, that.boxSn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, boxSn);
    }

    @Override
    public String toString() {
        return "AIBoxFetchAppListParam{deviceSn='" + deviceSn + '\'' + ", boxSn='" + boxSn + '\'' + '}';
    }
}
