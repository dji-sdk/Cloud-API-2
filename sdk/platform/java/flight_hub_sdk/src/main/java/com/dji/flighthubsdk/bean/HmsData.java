// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 飞机/机场 HMS 数据。
 */
public final class HmsData {

    private List<HmsDataItem> hmsDataItems;
    public HmsData() {
    }

/**
     * 构造 HmsData。
     *
     * @param hmsDataItems HMS 数据项列表
     */
    public HmsData(List<HmsDataItem> hmsDataItems) {
        this.hmsDataItems = hmsDataItems != null ? hmsDataItems : Collections.emptyList();
    }

    /** HMS 数据项列表 */
    public List<HmsDataItem> getHmsDataItems() {
        return hmsDataItems;
    }

    /** HMS 数据项列表 */
    public void setHmsDataItems(List<HmsDataItem> hmsDataItems) {
        this.hmsDataItems = hmsDataItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HmsData hmsData = (HmsData) o;
        return Objects.equals(hmsDataItems, hmsData.hmsDataItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hmsDataItems);
    }

    @Override
    public String toString() {
        return "HmsData{hmsDataItems=" + hmsDataItems + '}';
    }
}
