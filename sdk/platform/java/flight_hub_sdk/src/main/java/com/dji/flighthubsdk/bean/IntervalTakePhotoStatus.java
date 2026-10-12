// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 定时拍照状态 */
public class IntervalTakePhotoStatus {
    private int countdownTime;

    public int getCountdownTime() { return countdownTime; }
    public void setCountdownTime(int countdownTime) { this.countdownTime = countdownTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalTakePhotoStatus that = (IntervalTakePhotoStatus) o;
        return countdownTime == that.countdownTime;
    }

    @Override
    public int hashCode() { return Objects.hash(countdownTime); }
}
