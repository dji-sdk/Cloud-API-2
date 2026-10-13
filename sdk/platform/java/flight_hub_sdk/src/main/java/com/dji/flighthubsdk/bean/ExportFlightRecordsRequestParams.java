// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
/**
 * 导出飞行记录请求参数。
 * <p>必填字段：contentType, fileType, fileName, flightTaskIds；缺少任一必填字段将返回错误。</p>
 */
public class ExportFlightRecordsRequestParams {
    /** [必填] 导出类型。枚举："summary"=摘要, "details"=详情 */
    private String contentType = "";
    /** [必填] 文件类型, 比如 ["Excel"] */
    private List<String> fileType = new ArrayList<>();
    /** [必填] 文件名 */
    private String fileName = "";
    /** [必填] 飞行任务 id 列表 */
    private List<String> flightTaskIds = new ArrayList<>();
    /** 指标列表 */
    private List<String> metrics = new ArrayList<>();
    /** 时区, 比如 "Asia/Shanghai" */
    private String timeZone = "";
    /** 语言, 比如 "zh" */
    private String locale = "";

    public ExportFlightRecordsRequestParams() {
    }


    /** [必填] 导出类型。枚举："summary"=摘要, "details"=详情 */
    public String getContentType() { return contentType; }
    /** @param contentType [必填] 导出类型。枚举："summary"=摘要, "details"=详情 */
    public void setContentType(String contentType) { this.contentType = contentType != null ? contentType : ""; }
    /** [必填] 文件类型, 比如 ["Excel"] */
    public List<String> getFileType() { return fileType; }
    /** @param fileType [必填] 文件类型, 比如 ["Excel"] */
    public void setFileType(List<String> fileType) { this.fileType = fileType != null ? fileType : new ArrayList<>(); }
    /** [必填] 文件名 */
    public String getFileName() { return fileName; }
    /** @param fileName [必填] 文件名 */
    public void setFileName(String fileName) { this.fileName = fileName != null ? fileName : ""; }
    /** [必填] 飞行任务 id 列表 */
    public List<String> getFlightTaskIds() { return flightTaskIds; }
    /** @param flightTaskIds [必填] 飞行任务 id 列表 */
    public void setFlightTaskIds(List<String> flightTaskIds) { this.flightTaskIds = flightTaskIds != null ? flightTaskIds : new ArrayList<>(); }
    /** 指标列表 */
    public List<String> getMetrics() { return metrics; }
    /** @param metrics 指标列表 */
    public void setMetrics(List<String> metrics) { this.metrics = metrics != null ? metrics : new ArrayList<>(); }
    /** 时区, 比如 "Asia/Shanghai" */
    public String getTimeZone() { return timeZone; }
    /** @param timeZone 时区, 比如 "Asia/Shanghai" */
    public void setTimeZone(String timeZone) { this.timeZone = timeZone != null ? timeZone : ""; }
    /** 语言, 比如 "zh" */
    public String getLocale() { return locale; }
    /** @param locale 语言, 比如 "zh" */
    public void setLocale(String locale) { this.locale = locale != null ? locale : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExportFlightRecordsRequestParams that = (ExportFlightRecordsRequestParams) o;
        return Objects.equals(contentType, that.contentType)
                && Objects.equals(fileType, that.fileType)
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(flightTaskIds, that.flightTaskIds)
                && Objects.equals(metrics, that.metrics)
                && Objects.equals(timeZone, that.timeZone)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                contentType,
                fileType,
                fileName,
                flightTaskIds,
                metrics,
                timeZone,
                locale);
    }

    @Override
    public String toString() {
        return "ExportFlightRecordsRequestParams{"
                + "contentType='" + contentType + '\''
                + ", fileType=" + fileType
                + ", fileName='" + fileName + '\''
                + ", flightTaskIds=" + flightTaskIds
                + ", metrics=" + metrics
                + ", timeZone='" + timeZone + '\''
                + ", locale='" + locale + '\''
                + '}';
    }
}
