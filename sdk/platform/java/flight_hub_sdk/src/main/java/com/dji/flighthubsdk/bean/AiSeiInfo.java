// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * AI SEI 信息。
 * <p>{@link #getTargets()} 为不可变列表，元素为 {@link AiDetectionTarget}。</p>
 * <p>每个 target 携带框来源字段：
 * {@link AiDetectionTarget#getModelIndex()}（协议 model_index）、
 * {@link AiDetectionTarget#getFrameSource()}（协议 frame_source）、
 * {@link AiDetectionTarget#getModelInfoType()}（协议 model_info_type），
 * 用于多模型/多镜头场景下区分框来源。</p>
 * <p>来源字段不在 AiSeiInfo 级别，因为一帧可能包含多个 group，
 * 每个 group 的来源可能不同。</p>
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

    /**
     * AI 检测目标列表；每个元素的来源字段见
     * {@link AiDetectionTarget#getModelIndex()}（协议 model_index）、
     * {@link AiDetectionTarget#getFrameSource()}（协议 frame_source）、
     * {@link AiDetectionTarget#getModelInfoType()}（协议 model_info_type）。
     */
    public List<AiDetectionTarget> getTargets() {
        return targets;
    }

    /** @param targets AI 检测目标列表；元素来源字段见 AiDetectionTarget 的 modelIndex/frameSource/modelInfoType */
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
        return Objects.equals(targets, aiSeiInfo.targets);
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
