// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * AI 检测目标。
 * <p>AI SEI v4 框来源字段：
 * {@link #getModelIndex()}（协议 model_index）、
 * {@link #getFrameSource()}（协议 frame_source）、
 * {@link #getModelInfoType()}（协议 model_info_type）。
 * 由 SDK 从 SEI group 的 model info 回填；v4 以下或 model_info_type != 1 时为 -1/-1/0。</p>
 */
public final class AiDetectionTarget {

    /** 目标 ID */
    private int targetId;
    /** 目标类型（AI SEI）：官方模型 2=人, 3=车, 4=船, 34=移动目标；YOLOE 人 35~43、轿车 44~52、独立类型 53~73（公交/船/消防车/牛/羊 等） */
    private int targetType;
    /** 目标跟踪状态（AI SEI）：0=无效状态, 1=稳定跟随(正常跟踪), 2=视觉 beacon 融合, 3=辅助观测跟随(触发源丢失), 4=低可信度, 5=丢失(维持预测), 6=丢失, 7=丢失后重找回 */
    private int targetState;
    /** 目标中心 x 坐标 */
    private int targetCx;
    /** 目标中心 y 坐标 */
    private int targetCy;
    /** 目标宽度 */
    private int targetW;
    /** 目标高度 */
    private int targetH;
    /** 目标距离 */
    private long targetDistance;
    /** AI 模型索引（协议字段 model_index）：0=官方模型，1=三方模型，-1=未知 */
    private int modelIndex = -1;
    /** 图源/镜头来源（协议字段 frame_source，固件协议原始值）；-1=未知 */
    private int frameSource = -1;
    /** v4 model info 类型（协议字段 model_info_type）：0=无，1=base info（含 model_index + frame_source） */
    private int modelInfoType = 0;
    public AiDetectionTarget() {
    }

/**
     * 构造 AiDetectionTarget。
     *
     * @param targetId 目标ID
     * @param targetType 目标类型（AI SEI）：官方模型 2=人, 3=车, 4=船, 34=移动目标；YOLOE 人 35~43、轿车 44~52、独立类型 53~73（公交/船/消防车/牛/羊 等）
     * @param targetState 目标跟踪状态（AI SEI）：0=无效状态, 1=稳定跟随(正常跟踪), 2=视觉 beacon 融合, 3=辅助观测跟随(触发源丢失), 4=低可信度, 5=丢失(维持预测), 6=丢失, 7=丢失后重找回
     * @param targetCx 目标中心X坐标
     * @param targetCy 目标中心Y坐标
     * @param targetW 目标宽度
     * @param targetH 目标高度
     * @param targetDistance 目标距离
     */
    public AiDetectionTarget(
            int targetId,
            int targetType,
            int targetState,
            int targetCx,
            int targetCy,
            int targetW,
            int targetH,
            long targetDistance) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.targetState = targetState;
        this.targetCx = targetCx;
        this.targetCy = targetCy;
        this.targetW = targetW;
        this.targetH = targetH;
        this.targetDistance = targetDistance;
    }

    /** 目标 ID */
    public int getTargetId() {
        return targetId;
    }

    /** @param targetId 目标 ID */
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    /** 目标类型（AI SEI）：官方模型 2=人, 3=车, 4=船, 34=移动目标；YOLOE 人 35~43、轿车 44~52、独立类型 53~73（公交/船/消防车/牛/羊 等） */
    public int getTargetType() {
        return targetType;
    }

    /** 目标类型（AI SEI）：官方模型 2=人, 3=车, 4=船, 34=移动目标；YOLOE 人 35~43、轿车 44~52、独立类型 53~73（公交/船/消防车/牛/羊 等） */
    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    /** 目标跟踪状态（AI SEI）：0=无效状态, 1=稳定跟随(正常跟踪), 2=视觉 beacon 融合, 3=辅助观测跟随(触发源丢失), 4=低可信度, 5=丢失(维持预测), 6=丢失, 7=丢失后重找回 */
    public int getTargetState() {
        return targetState;
    }

    /** 目标跟踪状态（AI SEI）：0=无效状态, 1=稳定跟随(正常跟踪), 2=视觉 beacon 融合, 3=辅助观测跟随(触发源丢失), 4=低可信度, 5=丢失(维持预测), 6=丢失, 7=丢失后重找回 */
    public void setTargetState(int targetState) {
        this.targetState = targetState;
    }

    /** 目标中心 x 坐标 */
    public int getTargetCx() {
        return targetCx;
    }

    /** @param targetCx 目标中心 x 坐标 */
    public void setTargetCx(int targetCx) {
        this.targetCx = targetCx;
    }

    /** 目标中心 y 坐标 */
    public int getTargetCy() {
        return targetCy;
    }

    /** @param targetCy 目标中心 y 坐标 */
    public void setTargetCy(int targetCy) {
        this.targetCy = targetCy;
    }

    /** 目标宽度 */
    public int getTargetW() {
        return targetW;
    }

    /** 目标宽度 */
    public void setTargetW(int targetW) {
        this.targetW = targetW;
    }

    /** 目标高度 */
    public int getTargetH() {
        return targetH;
    }

    /** 目标高度 */
    public void setTargetH(int targetH) {
        this.targetH = targetH;
    }

    /** 目标距离 */
    public long getTargetDistance() {
        return targetDistance;
    }

    /** 目标距离 */
    public void setTargetDistance(long targetDistance) {
        this.targetDistance = targetDistance;
    }

    /** AI 模型索引（协议字段 model_index）：0=官方模型，1=三方模型，-1=未知 */
    public int getModelIndex() {
        return modelIndex;
    }

    /** @param modelIndex AI 模型索引（协议字段 model_index）：0=官方模型，1=三方模型，-1=未知 */
    public void setModelIndex(int modelIndex) {
        this.modelIndex = modelIndex;
    }

    /** 图源/镜头来源（协议字段 frame_source，固件协议原始值）；-1=未知 */
    public int getFrameSource() {
        return frameSource;
    }

    /** @param frameSource 图源/镜头来源（协议字段 frame_source，固件协议原始值）；-1=未知 */
    public void setFrameSource(int frameSource) {
        this.frameSource = frameSource;
    }

    /** v4 model info 类型（协议字段 model_info_type）：0=无，1=base info（含 model_index + frame_source） */
    public int getModelInfoType() {
        return modelInfoType;
    }

    /** @param modelInfoType v4 model info 类型（协议字段 model_info_type）：0=无，1=base info（含 model_index + frame_source） */
    public void setModelInfoType(int modelInfoType) {
        this.modelInfoType = modelInfoType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiDetectionTarget that = (AiDetectionTarget) o;
        return targetId == that.targetId
                && targetType == that.targetType
                && targetState == that.targetState
                && targetCx == that.targetCx
                && targetCy == that.targetCy
                && targetW == that.targetW
                && targetH == that.targetH
                && targetDistance == that.targetDistance
                && modelIndex == that.modelIndex
                && frameSource == that.frameSource
                && modelInfoType == that.modelInfoType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                targetId,
                targetType,
                targetState,
                targetCx,
                targetCy,
                targetW,
                targetH,
                targetDistance,
                modelIndex,
                frameSource,
                modelInfoType);
    }

    @Override
    public String toString() {
        return "AiDetectionTarget{"
                + "targetId=" + targetId
                + ", targetType=" + targetType
                + ", targetState=" + targetState
                + ", targetCx=" + targetCx
                + ", targetCy=" + targetCy
                + ", targetW=" + targetW
                + ", targetH=" + targetH
                + ", targetDistance=" + targetDistance
                + ", modelIndex=" + modelIndex
                + ", frameSource=" + frameSource
                + ", modelInfoType=" + modelInfoType
                + '}';
    }
}
