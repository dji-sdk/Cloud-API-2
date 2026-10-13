// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场激活时间。 */
public final class DockActivationTime {

    /** int, 机场激活时间(unix时间戳 s); */
    private int activationTime;
    public DockActivationTime() {
    }

/**
     * 构造 DockActivationTime。
     *
     * @param activationTime 激活时间
     */
    public DockActivationTime(int activationTime) {
        this.activationTime = activationTime;
    }

    /** int, 机场激活时间(unix时间戳 s); */
    public int getActivationTime() {
        return activationTime;
    }

    /** @param activationTime int, 机场激活时间(unix时间戳 s); */
    public void setActivationTime(int activationTime) {
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
        DockActivationTime that = (DockActivationTime) o;
        return activationTime == that.activationTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(activationTime);
    }

    @Override
    public String toString() {
        return "DockActivationTime{activationTime=" + activationTime + '}';
    }
}
