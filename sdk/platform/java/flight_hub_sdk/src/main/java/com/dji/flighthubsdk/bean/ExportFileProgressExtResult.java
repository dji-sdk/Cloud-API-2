// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 导出文件进度扩展结果 */
public final class ExportFileProgressExtResult {
    private String sn;
    private int exportType;
    private String appId;
    private String objectKey;
    public ExportFileProgressExtResult() {
    }

/**
     * 构造 ExportFileProgressExtResult。
     *
     * @param sn 设备序列号
     * @param exportType 导出类型
     * @param appId 应用 ID
     * @param objectKey 对象存储 Key
     */
    public ExportFileProgressExtResult(String sn, int exportType, String appId, String objectKey) {
        this.sn = sn != null ? sn : "";
        this.exportType = exportType;
        this.appId = appId != null ? appId : "";
        this.objectKey = objectKey != null ? objectKey : "";
    }

    /** 设备序列号 */
    public String getSn() { return sn; }
    /** 导出类型 */
    public int getExportType() { return exportType; }
    /** 应用 ID */
    public String getAppId() { return appId; }
    /** 对象存储 Key */
    public String getObjectKey() { return objectKey; }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 导出类型 */
    public void setExportType(int exportType) {
        this.exportType = exportType;
    }

    /** 应用 ID */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /** 对象存储 Key */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExportFileProgressExtResult that = (ExportFileProgressExtResult) o;
        return Objects.equals(sn, that.sn)
                && exportType == that.exportType
                && Objects.equals(appId, that.appId)
                && Objects.equals(objectKey, that.objectKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn, exportType, appId, objectKey);
    }

    @Override
    public String toString() {
        return "ExportFileProgressExtResult{"
                + "sn='" + sn + '\''
                + ", exportType=" + exportType
                + ", appId='" + appId + '\''
                + ", objectKey='" + objectKey + '\''
                + '}';
    }
}
