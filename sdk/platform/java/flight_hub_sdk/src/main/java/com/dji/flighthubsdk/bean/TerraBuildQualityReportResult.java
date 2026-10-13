// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Terra Build 质量报告返回结果 */
public final class TerraBuildQualityReportResult {
    private String qualityReportUrl;
    public TerraBuildQualityReportResult() {
    }

/**
     * 构造 TerraBuildQualityReportResult。
     *
     * @param qualityReportUrl Quality报告URL
     */
    public TerraBuildQualityReportResult(String qualityReportUrl) {
        this.qualityReportUrl = qualityReportUrl != null ? qualityReportUrl : "";
    }

    /** Quality报告URL */
    public String getQualityReportUrl() { return qualityReportUrl; }

    /** Quality报告URL */
    public void setQualityReportUrl(String qualityReportUrl) {
        this.qualityReportUrl = qualityReportUrl;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(qualityReportUrl, ((TerraBuildQualityReportResult) o).qualityReportUrl);
    }
    @Override public int hashCode() { return Objects.hash(qualityReportUrl); }

    @Override
    public String toString() {
        return "TerraBuildQualityReportResult{qualityReportUrl='" + qualityReportUrl + '\'' + '}';
    }
}
