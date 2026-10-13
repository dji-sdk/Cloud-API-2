// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** AI Box 获取应用列表进度查询参数 */
public class AIBoxFetchAppListProgressParam {
    private String deviceSn = "";
    private String boxSn = "";
    private String bid = "";

    public AIBoxFetchAppListProgressParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }

    /** AI Box序列号 */
    public String getBoxSn() { return boxSn; }
    /** @param boxSn AI Box序列号 */
    public void setBoxSn(String boxSn) { this.boxSn = boxSn != null ? boxSn : ""; }

    /** 业务标识 */
    public String getBid() { return bid; }
    /** @param bid 业务标识 */
    public void setBid(String bid) { this.bid = bid != null ? bid : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxFetchAppListProgressParam that = (AIBoxFetchAppListProgressParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(boxSn, that.boxSn)
                && Objects.equals(bid, that.bid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, boxSn, bid);
    }

    @Override
    public String toString() {
        return "AIBoxFetchAppListProgressParam{deviceSn='" + deviceSn + '\'' + ", boxSn='" + boxSn + '\'' + ", bid='" + bid + '\'' + '}';
    }
}
