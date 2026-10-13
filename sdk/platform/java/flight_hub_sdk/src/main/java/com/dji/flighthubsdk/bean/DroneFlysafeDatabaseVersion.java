// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 飞行器限飞数据库版本（flysafe_database_version）。EA340 消息推送补全（Phase D）。 */
public final class DroneFlysafeDatabaseVersion {

    private String flysafeDatabaseVersion;

    public DroneFlysafeDatabaseVersion() {
        this.flysafeDatabaseVersion = "";
    }

    /**
     * 构造 DroneFlysafeDatabaseVersion。
     *
     * @param flysafeDatabaseVersion 限飞数据库版本
     */
    public DroneFlysafeDatabaseVersion(String flysafeDatabaseVersion) {
        this.flysafeDatabaseVersion = flysafeDatabaseVersion != null ? flysafeDatabaseVersion : "";
    }

    /** 限飞数据库版本 */
    public String getFlysafeDatabaseVersion() {
        return flysafeDatabaseVersion;
    }

    /** 限飞数据库版本 */
    public void setFlysafeDatabaseVersion(String flysafeDatabaseVersion) {
        this.flysafeDatabaseVersion = flysafeDatabaseVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DroneFlysafeDatabaseVersion that = (DroneFlysafeDatabaseVersion) o;
        return Objects.equals(flysafeDatabaseVersion, that.flysafeDatabaseVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flysafeDatabaseVersion);
    }

    @Override
    public String toString() {
        return "DroneFlysafeDatabaseVersion{flysafeDatabaseVersion='" + flysafeDatabaseVersion + "'}";
    }
}
