// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** AI Box 信息列表结果 */
public final class AIBoxInfoListResult {
    private List<AIBoxInfoResult> aiBoxes;
    public AIBoxInfoListResult() {
    }

/**
     * 构造 AIBoxInfoListResult。
     *
     * @param aiBoxes AI Box列表
     */
    public AIBoxInfoListResult(List<AIBoxInfoResult> aiBoxes) {
        this.aiBoxes = aiBoxes != null ? aiBoxes : Collections.emptyList();
    }

    /** AI Box列表 */
    public List<AIBoxInfoResult> getAiBoxes() { return aiBoxes; }

    /** AI Box列表 */
    public void setAiBoxes(List<AIBoxInfoResult> aiBoxes) {
        this.aiBoxes = aiBoxes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AIBoxInfoListResult that = (AIBoxInfoListResult) o;
        return Objects.equals(aiBoxes, that.aiBoxes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aiBoxes);
    }

    @Override
    public String toString() {
        return "AIBoxInfoListResult{aiBoxes=" + aiBoxes + '}';
    }
}
