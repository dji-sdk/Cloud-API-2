// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.Map;

import java.util.Objects;
/** 获取航线详情返回结果 */
public final class GetWaylineDetailResult {

    private String waylineUuid;
    private String md5;
    private String waylineName;
    private String kmzObjectKey;
    private String url;
    private String suffix;
    private long deletedAt;
    private long updatedAt;
    private String kmzWithoutResUrl;
    private String targetAreaFileUrl;
    private String businessFileUrl;
    private WaylineDetailKmzData kmzData;
    private WaylineDetailExtData extData;
    private Map<String, String> editorWarnings;
    private Map<String, String> executeWarnings;
    public GetWaylineDetailResult() {
    }

/**
     * 构造 GetWaylineDetailResult。
     *
     * @param waylineUuid 航线 UUID
     * @param md5 MD5 校验值
     * @param waylineName 航线名称
     * @param kmzObjectKey KMZ 对象存储 Key
     * @param url 下载 URL
     * @param suffix 文件后缀
     * @param deletedAt 删除时间
     * @param updatedAt 更新时间
     * @param kmzWithoutResUrl 不含资源的 KMZ 下载 URL
     * @param targetAreaFileUrl 目标区域文件 URL
     * @param businessFileUrl 业务文件 URL
     * @param kmzData KMZ 解析数据
     * @param extData 扩展数据
     * @param editorWarnings 编辑器告警列表
     * @param executeWarnings 执行告警列表
     */
    public GetWaylineDetailResult(
            String waylineUuid,
            String md5,
            String waylineName,
            String kmzObjectKey,
            String url,
            String suffix,
            long deletedAt,
            long updatedAt,
            String kmzWithoutResUrl,
            String targetAreaFileUrl,
            String businessFileUrl,
            WaylineDetailKmzData kmzData,
            WaylineDetailExtData extData,
            Map<String, String> editorWarnings,
            Map<String, String> executeWarnings) {
        this.waylineUuid = waylineUuid != null ? waylineUuid : "";
        this.md5 = md5 != null ? md5 : "";
        this.waylineName = waylineName != null ? waylineName : "";
        this.kmzObjectKey = kmzObjectKey != null ? kmzObjectKey : "";
        this.url = url != null ? url : "";
        this.suffix = suffix != null ? suffix : "";
        this.deletedAt = deletedAt;
        this.updatedAt = updatedAt;
        this.kmzWithoutResUrl = kmzWithoutResUrl != null ? kmzWithoutResUrl : "";
        this.targetAreaFileUrl = targetAreaFileUrl != null ? targetAreaFileUrl : "";
        this.businessFileUrl = businessFileUrl != null ? businessFileUrl : "";
        this.kmzData = kmzData;
        this.extData = extData;
        this.editorWarnings = editorWarnings != null ? editorWarnings : Collections.emptyMap();
        this.executeWarnings = executeWarnings != null ? executeWarnings : Collections.emptyMap();
    }

    /** 航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }
    /** MD5 校验值 */
    public String getMd5() { return md5; }
    /** 航线名称 */
    public String getWaylineName() { return waylineName; }
    /** KMZ 对象存储 Key */
    public String getKmzObjectKey() { return kmzObjectKey; }
    /** 下载 URL */
    public String getUrl() { return url; }
    /** 文件后缀 */
    public String getSuffix() { return suffix; }
    /** 删除时间 */
    public long getDeletedAt() { return deletedAt; }
    /** 更新时间 */
    public long getUpdatedAt() { return updatedAt; }
    /** 不含资源的 KMZ 下载 URL */
    public String getKmzWithoutResUrl() { return kmzWithoutResUrl; }
    /** 目标区域文件 URL */
    public String getTargetAreaFileUrl() { return targetAreaFileUrl; }
    /** 业务文件 URL */
    public String getBusinessFileUrl() { return businessFileUrl; }
    /** KMZ 解析数据 */
    public WaylineDetailKmzData getKmzData() { return kmzData; }
    /** 扩展数据 */
    public WaylineDetailExtData getExtData() { return extData; }
    /** 编辑态告警 */
    public Map<String, String> getEditorWarnings() { return editorWarnings; }
    /** 执行态告警 */
    public Map<String, String> getExecuteWarnings() { return executeWarnings; }

    /** 航线 UUID */
    public void setWaylineUuid(String waylineUuid) {
        this.waylineUuid = waylineUuid;
    }

    /** MD5 校验值 */
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /** 航线名称 */
    public void setWaylineName(String waylineName) {
        this.waylineName = waylineName;
    }

    /** KMZ 对象存储 Key */
    public void setKmzObjectKey(String kmzObjectKey) {
        this.kmzObjectKey = kmzObjectKey;
    }

    /** 下载 URL */
    public void setUrl(String url) {
        this.url = url;
    }

    /** 文件后缀 */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /** 删除时间 */
    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }

    /** 更新时间 */
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** 不含资源的 KMZ 下载 URL */
    public void setKmzWithoutResUrl(String kmzWithoutResUrl) {
        this.kmzWithoutResUrl = kmzWithoutResUrl;
    }

    /** 目标区域文件 URL */
    public void setTargetAreaFileUrl(String targetAreaFileUrl) {
        this.targetAreaFileUrl = targetAreaFileUrl;
    }

    /** 业务文件 URL */
    public void setBusinessFileUrl(String businessFileUrl) {
        this.businessFileUrl = businessFileUrl;
    }

    /** KMZ 解析数据 */
    public void setKmzData(WaylineDetailKmzData kmzData) {
        this.kmzData = kmzData;
    }

    /** 扩展数据 */
    public void setExtData(WaylineDetailExtData extData) {
        this.extData = extData;
    }

    /** 编辑态告警 */
    public void setEditorWarnings(Map<String, String> editorWarnings) {
        this.editorWarnings = editorWarnings;
    }

    /** 执行态告警 */
    public void setExecuteWarnings(Map<String, String> executeWarnings) {
        this.executeWarnings = executeWarnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetWaylineDetailResult that = (GetWaylineDetailResult) o;
        return Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(md5, that.md5)
                && Objects.equals(waylineName, that.waylineName)
                && Objects.equals(kmzObjectKey, that.kmzObjectKey)
                && Objects.equals(url, that.url)
                && Objects.equals(suffix, that.suffix)
                && deletedAt == that.deletedAt
                && updatedAt == that.updatedAt
                && Objects.equals(kmzWithoutResUrl, that.kmzWithoutResUrl)
                && Objects.equals(targetAreaFileUrl, that.targetAreaFileUrl)
                && Objects.equals(businessFileUrl, that.businessFileUrl)
                && Objects.equals(kmzData, that.kmzData)
                && Objects.equals(extData, that.extData)
                && Objects.equals(editorWarnings, that.editorWarnings)
                && Objects.equals(executeWarnings, that.executeWarnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                waylineUuid,
                md5,
                waylineName,
                kmzObjectKey,
                url,
                suffix,
                deletedAt,
                updatedAt,
                kmzWithoutResUrl,
                targetAreaFileUrl,
                businessFileUrl,
                kmzData,
                extData,
                editorWarnings,
                executeWarnings);
    }

    @Override
    public String toString() {
        return "GetWaylineDetailResult{"
                + "waylineUuid='" + waylineUuid + '\''
                + ", md5='" + md5 + '\''
                + ", waylineName='" + waylineName + '\''
                + ", kmzObjectKey='" + kmzObjectKey + '\''
                + ", url='" + url + '\''
                + ", suffix='" + suffix + '\''
                + ", deletedAt=" + deletedAt
                + ", updatedAt=" + updatedAt
                + ", kmzWithoutResUrl='" + kmzWithoutResUrl + '\''
                + ", targetAreaFileUrl='" + targetAreaFileUrl + '\''
                + ", businessFileUrl='" + businessFileUrl + '\''
                + ", kmzData=" + kmzData
                + ", extData=" + extData
                + ", editorWarnings=" + editorWarnings
                + ", executeWarnings=" + executeWarnings
                + '}';
    }
}
