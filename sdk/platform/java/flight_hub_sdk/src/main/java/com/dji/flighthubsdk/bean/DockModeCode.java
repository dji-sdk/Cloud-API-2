// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 机场模式码（{@code listenDockModeCode}）。
 *
 * <p>{@code mode_code} 取值：
 * <ul>
 *   <li>0 — 空闲中</li>
 *   <li>1 — 现场调试中</li>
 *   <li>2 — 远程调试中</li>
 *   <li>3 — 设备升级中</li>
 *   <li>4 — 设备作业中（细分见 {@code flighttask_step_code}）</li>
 *   <li>5 — 机场设备未标定</li>
 *   <li>6 — 节能状态中</li>
 *   <li>7 — 现场急停中</li>
 *   <li>8 — 换电中</li>
 *   <li>9 — 日志拉取中</li>
 * </ul>
 */
public final class DockModeCode {

    private int modeCode;

    public DockModeCode() {
    }

    /**
     * @param modeCode 模式码，语义见类注释
     */
    public DockModeCode(int modeCode) {
        this.modeCode = modeCode;
    }

    /** 模式码，语义见类注释 */
    public int getModeCode() {
        return modeCode;
    }

    /** 模式码，语义见类注释 */
    public void setModeCode(int modeCode) {
        this.modeCode = modeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockModeCode that = (DockModeCode) o;
        return modeCode == that.modeCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modeCode);
    }

    @Override
    public String toString() {
        return "DockModeCode{modeCode=" + modeCode + '}';
    }
}
