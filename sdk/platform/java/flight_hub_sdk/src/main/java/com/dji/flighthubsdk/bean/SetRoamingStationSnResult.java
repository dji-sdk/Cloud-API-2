// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 设置漫游基站白名单响应。 */
public class SetRoamingStationSnResult {
    private List<RoamingStationSnResult> results = new ArrayList<>();

    public SetRoamingStationSnResult() {}
    public SetRoamingStationSnResult(List<RoamingStationSnResult> results) {
        setResults(results);
    }
    public List<RoamingStationSnResult> getResults() { return results; }
    public void setResults(List<RoamingStationSnResult> results) {
        this.results = results != null ? results : new ArrayList<RoamingStationSnResult>();
    }
}
