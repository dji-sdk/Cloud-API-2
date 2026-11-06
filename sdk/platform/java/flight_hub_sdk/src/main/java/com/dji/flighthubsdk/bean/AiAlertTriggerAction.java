// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI 告警触发动作 */
public class AiAlertTriggerAction {
    /** 触发动作类型 */
    private int action;
    /** 动作持续时间（秒） */
    private int duration;

    public AiAlertTriggerAction() {}

    /** 触发动作类型 */
    public int getAction() { return action; }
    /** @param action 触发动作类型 */
    public void setAction(int v) { this.action = v; }

    /** 动作持续时间（秒） */
    public int getDuration() { return duration; }
    /** @param duration 动作持续时间（秒） */
    public void setDuration(int v) { this.duration = v; }
}
