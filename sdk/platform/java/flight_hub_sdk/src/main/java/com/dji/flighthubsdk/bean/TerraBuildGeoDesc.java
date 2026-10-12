// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.List;
import java.util.Objects;

/** 地理坐标描述参数 */
public final class TerraBuildGeoDesc {
    private String csType;
    private String geoCs;
    private String geoCsWkt;
    private String geoidFileName;
    private List<Double> offset;
    private String overrideVerticalCs;

    public TerraBuildGeoDesc() {
    }

    /**
     * 构造 TerraBuildGeoDesc。
     *
     * @param csType 坐标系类型，如 "GEO_CS"
     * @param geoCs 地理坐标系编码，如 "EPSG:32649"
     * @param geoCsWkt 坐标系 WKT 字符串
     * @param geoidFileName 大地水准面文件名
     * @param offset 坐标偏移量
     * @param overrideVerticalCs 覆盖垂直坐标系，如 "EPSG:5737"
     */
    public TerraBuildGeoDesc(String csType, String geoCs, String geoCsWkt,
                             String geoidFileName, List<Double> offset, String overrideVerticalCs) {
        this.csType = csType != null ? csType : "";
        this.geoCs = geoCs != null ? geoCs : "";
        this.geoCsWkt = geoCsWkt != null ? geoCsWkt : "";
        this.geoidFileName = geoidFileName != null ? geoidFileName : "";
        this.offset = offset;
        this.overrideVerticalCs = overrideVerticalCs != null ? overrideVerticalCs : "";
    }

    /** 坐标系类型，如 "GEO_CS" */
    public String getCsType() { return csType; }
    /** 坐标系类型，如 "GEO_CS" */
    public void setCsType(String csType) { this.csType = csType; }

    /** 地理坐标系编码，如 "EPSG:32649" */
    public String getGeoCs() { return geoCs; }
    /** 地理坐标系编码，如 "EPSG:32649" */
    public void setGeoCs(String geoCs) { this.geoCs = geoCs; }

    /** 坐标系 WKT 字符串 */
    public String getGeoCsWkt() { return geoCsWkt; }
    /** 坐标系 WKT 字符串 */
    public void setGeoCsWkt(String geoCsWkt) { this.geoCsWkt = geoCsWkt; }

    /** 大地水准面文件名 */
    public String getGeoidFileName() { return geoidFileName; }
    /** 大地水准面文件名 */
    public void setGeoidFileName(String geoidFileName) { this.geoidFileName = geoidFileName; }

    /** 坐标偏移量 */
    public List<Double> getOffset() { return offset; }
    /** 坐标偏移量 */
    public void setOffset(List<Double> offset) { this.offset = offset; }

    /** 覆盖垂直坐标系，如 "EPSG:5737" */
    public String getOverrideVerticalCs() { return overrideVerticalCs; }
    /** 覆盖垂直坐标系，如 "EPSG:5737" */
    public void setOverrideVerticalCs(String overrideVerticalCs) { this.overrideVerticalCs = overrideVerticalCs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildGeoDesc that = (TerraBuildGeoDesc) o;
        return Objects.equals(csType, that.csType)
                && Objects.equals(geoCs, that.geoCs)
                && Objects.equals(geoCsWkt, that.geoCsWkt)
                && Objects.equals(geoidFileName, that.geoidFileName)
                && Objects.equals(offset, that.offset)
                && Objects.equals(overrideVerticalCs, that.overrideVerticalCs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(csType, geoCs, geoCsWkt, geoidFileName, offset, overrideVerticalCs);
    }

    @Override
    public String toString() {
        return "TerraBuildGeoDesc{"
                + "csType='" + csType + '\''
                + ", geoCs='" + geoCs + '\''
                + ", geoCsWkt='" + geoCsWkt + '\''
                + ", geoidFileName='" + geoidFileName + '\''
                + ", offset=" + offset
                + ", overrideVerticalCs='" + overrideVerticalCs + '\''
                + '}';
    }
}
