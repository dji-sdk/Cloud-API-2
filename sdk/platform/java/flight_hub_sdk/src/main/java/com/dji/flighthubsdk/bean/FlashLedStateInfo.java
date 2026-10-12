// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** DRC 北向 flash_led 状态（与 FlashLedParams 字段对齐，供 listenDrcFlashLed） */
public class FlashLedStateInfo {
    private int on = 0;
    private String patternUuid = "";
    private String patternName = "";
    private List<FlashLedPatternInfo> patterns = new ArrayList<>();

    public FlashLedStateInfo() {
    }

    /** 开关状态：0=关闭 1=开启 */
    public int getOn() { return on; }
    /** @param on 开关状态：0=关闭 1=开启 */
    public void setOn(int on) { this.on = on; }
    /** pattern 的 UUID */
    public String getPatternUuid() { return patternUuid; }
    /** @param patternUuid pattern 的 UUID */
    public void setPatternUuid(String patternUuid) { this.patternUuid = patternUuid != null ? patternUuid : ""; }
    /** pattern 的名字 */
    public String getPatternName() { return patternName; }
    /** @param patternName pattern 的名字 */
    public void setPatternName(String patternName) { this.patternName = patternName != null ? patternName : ""; }
    /** 爆闪 pattern 信息数组 */
    public List<FlashLedPatternInfo> getPatterns() { return patterns; }
    /** @param patterns 爆闪 pattern 信息数组 */
    public void setPatterns(List<FlashLedPatternInfo> patterns) {
        this.patterns = patterns != null ? patterns : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FlashLedStateInfo that = (FlashLedStateInfo) o;
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
        return "FlashLedStateInfo{on=" + on + ", patternUuid='" + patternUuid + '\''
                + ", patternName='" + patternName + '\'' + ", patterns=" + patterns + '}';
    }
}
