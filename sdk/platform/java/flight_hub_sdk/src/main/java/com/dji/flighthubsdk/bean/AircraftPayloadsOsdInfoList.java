// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 负载 OSD 列表。
 * <p><b>注意：当前版本后端暂不支持推送，详见 {@link AircraftPayloadsOsdInfo} 类注释。</b></p>
 */
public final class AircraftPayloadsOsdInfoList {

    private List<AircraftPayloadsOsdInfo> payloadsInfoList;
    public AircraftPayloadsOsdInfoList() {
    }

/**
     * 构造 AircraftPayloadsOsdInfoList。
     *
     * @param payloadsInfoList 载荷 OSD 信息列表
     */
    public AircraftPayloadsOsdInfoList(List<AircraftPayloadsOsdInfo> payloadsInfoList) {
        this.payloadsInfoList = payloadsInfoList != null ? payloadsInfoList : Collections.emptyList();
    }

    /** 载荷 OSD 信息列表 */
    public List<AircraftPayloadsOsdInfo> getPayloadsInfoList() {
        return payloadsInfoList;
    }

    /** 载荷 OSD 信息列表 */
    public void setPayloadsInfoList(List<AircraftPayloadsOsdInfo> payloadsInfoList) {
        this.payloadsInfoList = payloadsInfoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftPayloadsOsdInfoList that = (AircraftPayloadsOsdInfoList) o;
        return Objects.equals(payloadsInfoList, that.payloadsInfoList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadsInfoList);
    }

    @Override
    public String toString() {
        return "AircraftPayloadsOsdInfoList{payloadsInfoList=" + payloadsInfoList + '}';
    }
}
