// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * HMS 单条（含主 basics 与子 basics 列表）。
 */
public final class HmsDataItem {

    private HmsDataBasics basics;
    private List<HmsDataBasics> subHmsData;
    public HmsDataItem() {
    }

/**
     * 构造 HmsDataItem。
     *
     * @param basics HMS 基础信息
     * @param subHmsData 子 HMS 数据列表
     */
    public HmsDataItem(HmsDataBasics basics, List<HmsDataBasics> subHmsData) {
        this.basics =
                basics != null
                        ? basics
                        : new HmsDataBasics(
                                0,
                                0,
                                "",
                                "",
                                "",
                                0,
                                0,
                                new HmsArgs(0, 0, "", "", 0, 0, 0L, 0),
                                "",
                                "",
                                0,
                                0);
        this.subHmsData = subHmsData != null ? subHmsData : Collections.emptyList();
    }

    /** HMS 基础信息 */
    public HmsDataBasics getBasics() { return basics; }
    /** 子 HMS 数据列表 */
    public List<HmsDataBasics> getSubHmsData() { return subHmsData; }

    /** HMS 基础信息 */
    public void setBasics(HmsDataBasics basics) {
        this.basics = basics;
    }

    /** 子 HMS 数据列表 */
    public void setSubHmsData(List<HmsDataBasics> subHmsData) {
        this.subHmsData = subHmsData;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HmsDataItem that = (HmsDataItem) o;
        return Objects.equals(basics, that.basics) && Objects.equals(subHmsData, that.subHmsData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basics, subHmsData);
    }

    @Override
    public String toString() {
        return "HmsDataItem{basics=" + basics + ", subHmsData=" + subHmsData + '}';
    }
}
