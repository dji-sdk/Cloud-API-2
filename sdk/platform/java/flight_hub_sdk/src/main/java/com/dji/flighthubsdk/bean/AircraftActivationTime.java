// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 飞行器激活时间 Unix 戳。 */
public final class AircraftActivationTime {

    /** 飞行器激活时间(unix时间戳); */
    private long activationTime;
    public AircraftActivationTime() {
    }

/**
     * 构造 AircraftActivationTime。
     *
     * @param activationTime 飞行器激活时间(unix时间戳)
     */
    public AircraftActivationTime(long activationTime) {
        this.activationTime = activationTime;
    }

    /** 飞行器激活时间(unix时间戳); */
    public long getActivationTime() {
        return activationTime;
    }

    /** @param activationTime 飞行器激活时间(unix时间戳); */
    public void setActivationTime(long activationTime) {
        this.activationTime = activationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftActivationTime that = (AircraftActivationTime) o;
        return activationTime == that.activationTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(activationTime);
    }

    @Override
    public String toString() {
        return "AircraftActivationTime{activationTime=" + activationTime + '}';
    }
}
