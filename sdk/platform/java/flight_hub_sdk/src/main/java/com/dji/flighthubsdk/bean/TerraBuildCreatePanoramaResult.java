// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** Terra Build 创建全景图的返回结果 */
public final class TerraBuildCreatePanoramaResult {
    private int id;
    private String uuid;
    private String jobUuid;
    public TerraBuildCreatePanoramaResult() {
    }

/**
     * 构造 TerraBuildCreatePanoramaResult。
     *
     * @param id ID
     * @param uuid 任务 UUID
     * @param jobUuid 作业UUID
     */
    public TerraBuildCreatePanoramaResult(int id, String uuid, String jobUuid) {
        this.id = id;
        this.uuid = uuid != null ? uuid : "";
        this.jobUuid = jobUuid != null ? jobUuid : "";
    }

    /** ID */
    public int getId() { return id; }
    /** 任务 UUID */
    public String getUuid() { return uuid; }
    /** 作业UUID */
    public String getJobUuid() { return jobUuid; }

    /** ID */
    public void setId(int id) {
        this.id = id;
    }

    /** 任务 UUID */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /** 作业UUID */
    public void setJobUuid(String jobUuid) {
        this.jobUuid = jobUuid;
    }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildCreatePanoramaResult that = (TerraBuildCreatePanoramaResult) o;
        return id == that.id && Objects.equals(uuid, that.uuid) && Objects.equals(jobUuid, that.jobUuid);
    }
    @Override public int hashCode() { return Objects.hash(id, uuid, jobUuid); }

    @Override
    public String toString() {
        return "TerraBuildCreatePanoramaResult{id=" + id + ", uuid='" + uuid + '\'' + ", jobUuid='" + jobUuid + '\'' + '}';
    }
}
