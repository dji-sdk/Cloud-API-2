// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * AI SEI 信息。 * <p>{@link #getTargets()} 为不可变列表，元素为 {@link AiDetectionTarget}。</p>
 */
public final class AiSeiInfo {

    /** AI 检测目标列表 */
    private List<AiDetectionTarget> targets;
    public AiSeiInfo() {
    }

/**
     * @param targets 检测目标列表，允许为 null（视为空列表）
     */
    public AiSeiInfo(List<AiDetectionTarget> targets) {
        this.targets = targets != null ? targets : Collections.emptyList();
    }

    /** AI 检测目标列表 */
    public List<AiDetectionTarget> getTargets() {
        return targets;
    }

    /** @param targets AI 检测目标列表 */
    public void setTargets(List<AiDetectionTarget> targets) {
        this.targets = targets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiSeiInfo aiSeiInfo = (AiSeiInfo) o;
        return targets.equals(aiSeiInfo.targets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targets);
    }

    @Override
    public String toString() {
        return "AiSeiInfo{targets=" + targets + '}';
    }
}
