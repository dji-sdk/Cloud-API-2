// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** AI Box 安装应用请求参数 */
public class AIBoxInstallAppParam {
    private String deviceSn = "";
    private String boxSn = "";
    private String objectKey = "";

    public AIBoxInstallAppParam() {
    }


    /** 设备序列号 */
    public String getDeviceSn() { return deviceSn; }
    /** @param deviceSn 设备序列号 */
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn != null ? deviceSn : ""; }

    /** AI Box序列号 */
    public String getBoxSn() { return boxSn; }
    /** @param boxSn AI Box序列号 */
    public void setBoxSn(String boxSn) { this.boxSn = boxSn != null ? boxSn : ""; }

    /** 对象存储键 */
    public String getObjectKey() { return objectKey; }
    /** @param objectKey 对象存储键 */
    public void setObjectKey(String objectKey) { this.objectKey = objectKey != null ? objectKey : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxInstallAppParam that = (AIBoxInstallAppParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(boxSn, that.boxSn)
                && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, boxSn, objectKey);
    }

    @Override
    public String toString() {
        return "AIBoxInstallAppParam{deviceSn='" + deviceSn + '\'' + ", boxSn='" + boxSn + '\'' + ", objectKey='" + objectKey + '\'' + '}';
    }
}
