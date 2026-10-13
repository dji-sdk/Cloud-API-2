// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** AI 告警配置：告警方式与间隔 */
public class AiAlertAlarmInfo {
    /** 告警方式：1=声音 / 2=图像 / 3=其他 */
    private List<Integer> alarmMethod = new ArrayList<>();
    /** 告警间隔，单位秒 */
    private int alarmInterval;

    public AiAlertAlarmInfo() {}

    /** 告警方式：1=声音 / 2=图像 / 3=其他 */
    public List<Integer> getAlarmMethod() { return alarmMethod; }
    /** @param alarmMethod 告警方式：1=声音 / 2=图像 / 3=其他 */
    public void setAlarmMethod(List<Integer> v) { this.alarmMethod = v != null ? v : new ArrayList<>(); }

    /** 告警间隔，单位秒 */
    public int getAlarmInterval() { return alarmInterval; }
    /** @param alarmInterval 告警间隔，单位秒 */
    public void setAlarmInterval(int v) { this.alarmInterval = v; }
}
