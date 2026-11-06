// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 批量操作结果（成功/失败计数）。 */
public final class CascadeBatchOpResult {
    private int okCount;
    private int failCount;

    public CascadeBatchOpResult() {
        this(0, 0);
    }

    /**
     * 构造 CascadeBatchOpResult。
     *
     * @param okCount   成功数
     * @param failCount 失败数
     */
    public CascadeBatchOpResult(int okCount, int failCount) {
        this.okCount = okCount;
        this.failCount = failCount;
    }

    /** 成功数 */
    public int getOkCount() { return okCount; }
    /** 成功数 */
    public void setOkCount(int okCount) { this.okCount = okCount; }

    /** 失败数 */
    public int getFailCount() { return failCount; }
    /** 失败数 */
    public void setFailCount(int failCount) { this.failCount = failCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CascadeBatchOpResult that = (CascadeBatchOpResult) o;
        return okCount == that.okCount && failCount == that.failCount;
    }

    @Override
    public int hashCode() { return Objects.hash(okCount, failCount); }

    @Override
    public String toString() {
        return "CascadeBatchOpResult{okCount=" + okCount + ", failCount=" + failCount + "}";
    }
}
