// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** AI Box 导出文件请求参数 */
public class AIBoxExportFileParam {
    private String deviceSn = "";
    private String boxSn = "";
    private String appId = "";
    private int exportType;

    public AIBoxExportFileParam() {
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

    /** 导出类型。枚举：日志/应用数据 */
    public int getExportType() { return exportType; }
    /** @param exportType 导出类型。枚举：日志/应用数据 */
    public void setExportType(int exportType) { this.exportType = exportType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxExportFileParam that = (AIBoxExportFileParam) o;
        return Objects.equals(deviceSn, that.deviceSn)
                && Objects.equals(boxSn, that.boxSn)
                && Objects.equals(appId, that.appId)
                && exportType == that.exportType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceSn, boxSn, appId, exportType);
    }

    @Override
    public String toString() {
        return "AIBoxExportFileParam{"
                + "deviceSn='" + deviceSn + '\''
                + ", boxSn='" + boxSn + '\''
                + ", appId='" + appId + '\''
                + ", exportType=" + exportType
                + '}';
    }
}
