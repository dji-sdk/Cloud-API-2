// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 遥控器失控动作。 */
public final class AircraftRcLostAction {

    /** int, {0: 悬停, 1: 着陆(降落), 2: 返航}; */
    private int rcLostAction;
    public AircraftRcLostAction() {
    }

/**
     * 构造 AircraftRcLostAction。
     *
     * @param rcLostAction 遥控器失联动作
     */
    public AircraftRcLostAction(int rcLostAction) {
        this.rcLostAction = rcLostAction;
    }

    /** int, {0: 悬停, 1: 着陆(降落), 2: 返航}; */
    public int getRcLostAction() {
        return rcLostAction;
    }

    /** @param rcLostAction int, {0: 悬停, 1: 着陆(降落), 2: 返航}; */
    public void setRcLostAction(int rcLostAction) {
        this.rcLostAction = rcLostAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftRcLostAction that = (AircraftRcLostAction) o;
        return rcLostAction == that.rcLostAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rcLostAction);
    }

    @Override
    public String toString() {
        return "AircraftRcLostAction{rcLostAction=" + rcLostAction + '}';
    }
}
