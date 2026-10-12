// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;

/**
 * 航线拼接返回结果。
 * <p>包含拼接后的航线 UUID、完整的 KMZ 航线解析数据和扩展数据。</p>
 */
public final class ConcatWaylineResult {

    /** 拼接后的新航线 UUID */
    private String waylineUuid;
    /** KMZ 航线解析数据 */
    private WaylineDetailKmzData kmzData;
    /** 航线扩展数据 */
    private WaylineDetailExtData extData;

    public ConcatWaylineResult() {
    }

    /** 拼接后的新航线 UUID */
    public String getWaylineUuid() { return waylineUuid; }

    /** @param waylineUuid 拼接后的新航线 UUID */
    public void setWaylineUuid(String waylineUuid) { this.waylineUuid = waylineUuid; }

    /** KMZ 航线解析数据 */
    public WaylineDetailKmzData getKmzData() { return kmzData; }

    /** @param kmzData KMZ 航线解析数据 */
    public void setKmzData(WaylineDetailKmzData kmzData) { this.kmzData = kmzData; }

    /** 航线扩展数据 */
    public WaylineDetailExtData getExtData() { return extData; }

    /** @param extData 航线扩展数据 */
    public void setExtData(WaylineDetailExtData extData) { this.extData = extData; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcatWaylineResult that = (ConcatWaylineResult) o;
        return Objects.equals(waylineUuid, that.waylineUuid)
                && Objects.equals(kmzData, that.kmzData)
                && Objects.equals(extData, that.extData);
    }

    @Override
    public int hashCode() { return Objects.hash(waylineUuid, kmzData, extData); }

    @Override
    public String toString() {
        return "ConcatWaylineResult{waylineUuid='" + waylineUuid + '\''
                + ", kmzData=" + kmzData + ", extData=" + extData + '}';
    }
}
