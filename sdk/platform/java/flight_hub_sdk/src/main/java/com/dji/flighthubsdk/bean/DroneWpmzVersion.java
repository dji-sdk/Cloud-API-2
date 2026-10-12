// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 飞行器航线解析库版本（wpmz_version）。EA340 消息推送补全（Phase D）。 */
public final class DroneWpmzVersion {

    private String wpmzVersion;

    public DroneWpmzVersion() {
        this.wpmzVersion = "";
    }

    /**
     * 构造 DroneWpmzVersion。
     *
     * @param wpmzVersion 航线解析库版本
     */
    public DroneWpmzVersion(String wpmzVersion) {
        this.wpmzVersion = wpmzVersion != null ? wpmzVersion : "";
    }

    /** 航线解析库版本 */
    public String getWpmzVersion() {
        return wpmzVersion;
    }

    /** 航线解析库版本 */
    public void setWpmzVersion(String wpmzVersion) {
        this.wpmzVersion = wpmzVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneWpmzVersion that = (DroneWpmzVersion) o;
        return Objects.equals(wpmzVersion, that.wpmzVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wpmzVersion);
    }

    @Override
    public String toString() {
        return "DroneWpmzVersion{wpmzVersion='" + wpmzVersion + "'}";
    }
}
