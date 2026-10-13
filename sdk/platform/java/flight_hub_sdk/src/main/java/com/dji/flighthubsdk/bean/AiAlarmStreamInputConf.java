// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** AI 告警码流配置 */
public class AiAlarmStreamInputConf {
    /** 码流编号 */
    private int streamSource = 0;
    /** 模型编号 */
    private int modelIdx = 0;
    /** 告警算法类型: 1 DJI可见光人车船, 2 DJI红外人车船, 3 DJI红外温度识别, 5 三方红外AI, 255 三方算法 */
    private int algorithmType = 0;
    /** 告警间隔(秒) */
    private int alarmInterval = 0;
    /** 告警目标数组 */
    private List<AiAlarmTarget> alarmTargets = new ArrayList<>();

    public AiAlarmStreamInputConf() {
    }

    /** 码流编号 */
    public int getStreamSource() { return streamSource; }
    /** @param streamSource 码流编号 */
    public void setStreamSource(int streamSource) { this.streamSource = streamSource; }
    /** 模型编号 */
    public int getModelIdx() { return modelIdx; }
    /** @param modelIdx 模型编号 */
    public void setModelIdx(int modelIdx) { this.modelIdx = modelIdx; }
    /** 告警算法类型: 1 DJI可见光人车船, 2 DJI红外人车船, 3 DJI红外温度识别, 5 三方红外AI, 255 三方算法 */
    public int getAlgorithmType() { return algorithmType; }
    /** @param algorithmType 告警算法类型: 1 DJI可见光人车船, 2 DJI红外人车船, 3 DJI红外温度识别, 5 三方红外AI, 255 三方算法 */
    public void setAlgorithmType(int algorithmType) { this.algorithmType = algorithmType; }
    /** 告警间隔(秒) */
    public int getAlarmInterval() { return alarmInterval; }
    /** @param alarmInterval 告警间隔(秒) */
    public void setAlarmInterval(int alarmInterval) { this.alarmInterval = alarmInterval; }
    /** 告警目标数组 */
    public List<AiAlarmTarget> getAlarmTargets() { return alarmTargets; }
    /** @param alarmTargets 告警目标数组 */
    public void setAlarmTargets(List<AiAlarmTarget> alarmTargets) { this.alarmTargets = alarmTargets != null ? alarmTargets : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiAlarmStreamInputConf that = (AiAlarmStreamInputConf) o;
        return streamSource == that.streamSource
                && modelIdx == that.modelIdx
                && algorithmType == that.algorithmType
                && alarmInterval == that.alarmInterval
                && Objects.equals(alarmTargets, that.alarmTargets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamSource, modelIdx, algorithmType, alarmInterval, alarmTargets);
    }
}
