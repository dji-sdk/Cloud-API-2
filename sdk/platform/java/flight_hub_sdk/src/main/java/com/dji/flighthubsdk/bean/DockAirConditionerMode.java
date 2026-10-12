// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 机场空调工作模式（air_conditioner_mode）。EA340 消息推送补全（Phase D）。 */
public final class DockAirConditionerMode {

    private long airConditionerMode;

    public DockAirConditionerMode() {
    }

    /**
     * 构造 DockAirConditionerMode。
     *
     * @param airConditionerMode 空调工作模式
     */
    public DockAirConditionerMode(long airConditionerMode) {
        this.airConditionerMode = airConditionerMode;
    }

    /** 空调工作模式 */
    public long getAirConditionerMode() {
        return airConditionerMode;
    }

    /** 空调工作模式 */
    public void setAirConditionerMode(long airConditionerMode) {
        this.airConditionerMode = airConditionerMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockAirConditionerMode that = (DockAirConditionerMode) o;
        return airConditionerMode == that.airConditionerMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(airConditionerMode);
    }

    @Override
    public String toString() {
        return "DockAirConditionerMode{airConditionerMode=" + airConditionerMode + '}';
    }
}
