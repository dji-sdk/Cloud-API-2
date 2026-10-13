// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** AI 主动告警开启参数 */
public class AiAlarmStartParams {
    /** 手飞多码流告警配置数组 */
    private List<AiAlarmStreamInputConf> streamInputConfs = new ArrayList<>();

    public AiAlarmStartParams() {
    }

    /** 手飞多码流告警配置数组 */
    public List<AiAlarmStreamInputConf> getStreamInputConfs() { return streamInputConfs; }
    /** @param streamInputConfs 手飞多码流告警配置数组 */
    public void setStreamInputConfs(List<AiAlarmStreamInputConf> streamInputConfs) {
        this.streamInputConfs = streamInputConfs != null ? streamInputConfs : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AiAlarmStartParams that = (AiAlarmStartParams) o;
        return Objects.equals(streamInputConfs, that.streamInputConfs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streamInputConfs);
    }
}
