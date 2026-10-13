// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 爆闪灯设置参数 */
public class FlashLedParams {
    /** 开关状态 0:关闭 1:开启 */
    private int on = 0;
    /** pattern的uuid */
    private String patternUuid = "";
    /** pattern的名字 */
    private String patternName = "";
    /** 爆闪pattern信息数组 */
    private List<FlashLedPatternInfo> patterns = new ArrayList<>();

    public FlashLedParams() {
    }

    /** 开关状态 0:关闭 1:开启 */
    public int getOn() { return on; }
    /** @param on 开关状态 0:关闭 1:开启 */
    public void setOn(int on) { this.on = on; }
    /** pattern的uuid */
    public String getPatternUuid() { return patternUuid; }
    /** @param patternUuid pattern的uuid */
    public void setPatternUuid(String patternUuid) { this.patternUuid = patternUuid != null ? patternUuid : ""; }
    /** pattern的名字 */
    public String getPatternName() { return patternName; }
    /** @param patternName pattern的名字 */
    public void setPatternName(String patternName) { this.patternName = patternName != null ? patternName : ""; }
    /** 爆闪pattern信息数组 */
    public List<FlashLedPatternInfo> getPatterns() { return patterns; }
    /** @param patterns 爆闪pattern信息数组 */
    public void setPatterns(List<FlashLedPatternInfo> patterns) { this.patterns = patterns != null ? patterns : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlashLedParams that = (FlashLedParams) o;
        return on == that.on
                && Objects.equals(patternUuid, that.patternUuid)
                && Objects.equals(patternName, that.patternName)
                && Objects.equals(patterns, that.patterns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(on, patternUuid, patternName, patterns);
    }

    @Override
    public String toString() {
        return "FlashLedParams{on=" + on + ", patternUuid='" + patternUuid + '\'' + ", patternName='" + patternName + '\'' + ", patterns=" + patterns + '}';
    }
}
