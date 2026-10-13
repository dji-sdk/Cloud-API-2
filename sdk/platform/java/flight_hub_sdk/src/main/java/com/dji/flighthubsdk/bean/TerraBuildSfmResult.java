// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** Terra Build SFM 结果 */
public final class TerraBuildSfmResult {
    private String sfmJsonUrl;
    private String sfmReportJsonUrl;
    private String gcpOutJsonUrl;
    private List<String> reProjectionErrorPicUrlList;
    public TerraBuildSfmResult() {
    }

/**
     * 构造 TerraBuildSfmResult。
     *
     * @param sfmJsonUrl SFM JSON URL
     * @param sfmReportJsonUrl Sfm报告JsonURL
     * @param gcpOutJsonUrl 控制点输出 JSON URL
     * @param reProjectionErrorPicUrlList ReProjection错误PicURL列表
     */
    public TerraBuildSfmResult(
            String sfmJsonUrl,
            String sfmReportJsonUrl,
            String gcpOutJsonUrl,
            List<String> reProjectionErrorPicUrlList) {
        this.sfmJsonUrl = sfmJsonUrl != null ? sfmJsonUrl : "";
        this.sfmReportJsonUrl = sfmReportJsonUrl != null ? sfmReportJsonUrl : "";
        this.gcpOutJsonUrl = gcpOutJsonUrl != null ? gcpOutJsonUrl : "";
        this.reProjectionErrorPicUrlList = reProjectionErrorPicUrlList != null ? reProjectionErrorPicUrlList : Collections.emptyList();
    }

    /** SFM JSON URL */
    public String getSfmJsonUrl() { return sfmJsonUrl; }
    /** Sfm报告JsonURL */
    public String getSfmReportJsonUrl() { return sfmReportJsonUrl; }
    /** 控制点输出 JSON URL */
    public String getGcpOutJsonUrl() { return gcpOutJsonUrl; }
    /** ReProjection错误PicURL列表 */
    public List<String> getReProjectionErrorPicUrlList() { return reProjectionErrorPicUrlList; }

    /** SFM JSON URL */
    public void setSfmJsonUrl(String sfmJsonUrl) {
        this.sfmJsonUrl = sfmJsonUrl;
    }

    /** Sfm报告JsonURL */
    public void setSfmReportJsonUrl(String sfmReportJsonUrl) {
        this.sfmReportJsonUrl = sfmReportJsonUrl;
    }

    /** 控制点输出 JSON URL */
    public void setGcpOutJsonUrl(String gcpOutJsonUrl) {
        this.gcpOutJsonUrl = gcpOutJsonUrl;
    }

    /** ReProjection错误PicURL列表 */
    public void setReProjectionErrorPicUrlList(List<String> reProjectionErrorPicUrlList) {
        this.reProjectionErrorPicUrlList = reProjectionErrorPicUrlList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraBuildSfmResult that = (TerraBuildSfmResult) o;
        return Objects.equals(sfmJsonUrl, that.sfmJsonUrl)
                && Objects.equals(sfmReportJsonUrl, that.sfmReportJsonUrl)
                && Objects.equals(gcpOutJsonUrl, that.gcpOutJsonUrl)
                && Objects.equals(reProjectionErrorPicUrlList, that.reProjectionErrorPicUrlList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sfmJsonUrl, sfmReportJsonUrl, gcpOutJsonUrl, reProjectionErrorPicUrlList);
    }

    @Override
    public String toString() {
        return "TerraBuildSfmResult{"
                + "sfmJsonUrl='" + sfmJsonUrl + '\''
                + ", sfmReportJsonUrl='" + sfmReportJsonUrl + '\''
                + ", gcpOutJsonUrl='" + gcpOutJsonUrl + '\''
                + ", reProjectionErrorPicUrlList=" + reProjectionErrorPicUrlList
                + '}';
    }
}
