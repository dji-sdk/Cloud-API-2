// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 安装应用进度扩展结果 */
public final class InstallAppProgressExtResult {
    private String sn;
    public InstallAppProgressExtResult() {
    }

/**
     * 构造 InstallAppProgressExtResult。
     *
     * @param sn 设备序列号
     */
    public InstallAppProgressExtResult(String sn) {
        this.sn = sn != null ? sn : "";
    }

    /** 设备序列号 */
    public String getSn() { return sn; }

    /** 设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallAppProgressExtResult that = (InstallAppProgressExtResult) o;
        return Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sn);
    }

    @Override
    public String toString() {
        return "InstallAppProgressExtResult{sn='" + sn + '\'' + '}';
    }
}
