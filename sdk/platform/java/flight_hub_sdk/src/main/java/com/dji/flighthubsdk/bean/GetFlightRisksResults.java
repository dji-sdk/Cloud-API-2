// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 查询飞行安全风险响应 */
public final class GetFlightRisksResults {
    /** 风险列表；任务未完成飞行时为空 */
    private List<FlightRiskItem> list = Collections.emptyList();

    public GetFlightRisksResults() {
    }

    /**
     * 构造 GetFlightRisksResults。
     *
     * @param list 风险列表；任务未完成飞行时为空
     */
    public GetFlightRisksResults(List<FlightRiskItem> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    /** 风险列表；任务未完成飞行时为空 */
    public List<FlightRiskItem> getList() { return list; }
    /** @param list 风险列表；任务未完成飞行时为空 */
    public void setList(List<FlightRiskItem> list) { this.list = list != null ? list : Collections.emptyList(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GetFlightRisksResults that = (GetFlightRisksResults) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return "GetFlightRisksResults{"
                + "list=" + list
                + '}';
    }
}
