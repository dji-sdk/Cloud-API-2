// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** 获取算法列表结果 */
public class GetAIInsideModelsResult {
    /** 组织下算法列表，按 signed_utc 降序排列 */
    private List<AIInsideModelItem> models = new ArrayList<>();

    public GetAIInsideModelsResult() {}

    /** 组织下算法列表，按 signed_utc 降序排列 */
    public List<AIInsideModelItem> getModels() { return models; }

    /** @param models 组织下算法列表，按 signed_utc 降序排列 */
    public void setModels(List<AIInsideModelItem> v) { this.models = v != null ? v : new ArrayList<>(); }
}
