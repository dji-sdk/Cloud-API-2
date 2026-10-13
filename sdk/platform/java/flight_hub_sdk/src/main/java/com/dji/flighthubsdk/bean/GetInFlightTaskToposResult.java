// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 飞行中任务拓扑查询响应。 */
public class GetInFlightTaskToposResult {
    private List<InFlightTaskTopoItem> list = new ArrayList<>();

    public GetInFlightTaskToposResult() {}
    public GetInFlightTaskToposResult(List<InFlightTaskTopoItem> list) { setList(list); }
    public List<InFlightTaskTopoItem> getList() { return list; }
    public void setList(List<InFlightTaskTopoItem> list) {
        this.list = list != null ? list : new ArrayList<InFlightTaskTopoItem>();
    }
}
