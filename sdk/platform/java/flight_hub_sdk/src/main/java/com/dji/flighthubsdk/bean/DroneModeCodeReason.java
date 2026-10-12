// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 飞行器进入当前状态原因（mode_code_reason）。EA340 消息推送补全（Phase D）。 */
public final class DroneModeCodeReason {

    private long modeCodeReason;

    public DroneModeCodeReason() {
    }

    /**
     * 构造 DroneModeCodeReason。
     *
     * @param modeCodeReason 进入当前状态原因
     */
    public DroneModeCodeReason(long modeCodeReason) {
        this.modeCodeReason = modeCodeReason;
    }

    /** 进入当前状态原因 */
    public long getModeCodeReason() {
        return modeCodeReason;
    }

    /** 进入当前状态原因 */
    public void setModeCodeReason(long modeCodeReason) {
        this.modeCodeReason = modeCodeReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneModeCodeReason that = (DroneModeCodeReason) o;
        return modeCodeReason == that.modeCodeReason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modeCodeReason);
    }

    @Override
    public String toString() {
        return "DroneModeCodeReason{modeCodeReason=" + modeCodeReason + '}';
    }
}
