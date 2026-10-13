// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 模型文件解压进度 WS 推送消息
 * topic: topic_reconstruction_unzip_progress
 */
public final class TerraUnzipProgressMsg {

    /** 关联的 File 记录 ID */
    private long fileId;
    /** 关联的 File 记录 UUID */
    private String uuid;
    /** 文件名 */
    private String name;
    /** 解压进度百分比 [0, 100] */
    private double percent;

    public TerraUnzipProgressMsg() {
    }

    public TerraUnzipProgressMsg(long fileId, String uuid, String name, double percent) {
        this.fileId = fileId;
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
        this.percent = percent;
    }

    public long getFileId() { return fileId; }
    public void setFileId(long fileId) { this.fileId = fileId; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPercent() { return percent; }
    public void setPercent(double percent) { this.percent = percent; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraUnzipProgressMsg that = (TerraUnzipProgressMsg) o;
        return fileId == that.fileId && Double.compare(that.percent, percent) == 0
                && Objects.equals(uuid, that.uuid) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() { return Objects.hash(fileId, uuid, name, percent); }

    @Override
    public String toString() {
        return "TerraUnzipProgressMsg{fileId=" + fileId + ", uuid='" + uuid
                + "', name='" + name + "', percent=" + percent + '}';
    }
}
