// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 机场作业次数。 */
public final class DockJobNumber {

    /** int, 作业次数; */
    private int jobNumber;
    public DockJobNumber() {
    }

/**
     * 构造 DockJobNumber。
     *
     * @param jobNumber 作业次数
     */
    public DockJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    /** int, 作业次数; */
    public int getJobNumber() {
        return jobNumber;
    }

    /** @param jobNumber int, 作业次数; */
    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockJobNumber that = (DockJobNumber) o;
        return jobNumber == that.jobNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobNumber);
    }

    @Override
    public String toString() {
        return "DockJobNumber{jobNumber=" + jobNumber + '}';
    }
}
