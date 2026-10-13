// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/**
 * 模型状态变更 WS 推送消息
 * topic: topic_model_status_changed
 * state: 1-解压中 2-成功 3-失败 5-转换中 6-转换成功 7-转换失败
 */
public final class TerraModelStatusChangedMsg {

    /** 关联的 File 记录 ID */
    private long fileId;
    /** 关联的 File 记录 UUID */
    private String uuid;
    /** 文件名 */
    private String name;
    /** 模型新状态 */
    private int state;
    /** 文件类型 */
    private int fileType;
    /** 子文件类型 */
    private int subFileType;
    /** 错误码（0 表示无错误） */
    private int errorCode;

    public TerraModelStatusChangedMsg() {
    }

    public TerraModelStatusChangedMsg(long fileId, String uuid, String name,
            int state, int fileType, int subFileType, int errorCode) {
        this.fileId = fileId;
        this.uuid = uuid != null ? uuid : "";
        this.name = name != null ? name : "";
        this.state = state;
        this.fileType = fileType;
        this.subFileType = subFileType;
        this.errorCode = errorCode;
    }

    public long getFileId() { return fileId; }
    public void setFileId(long fileId) { this.fileId = fileId; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getState() { return state; }
    public void setState(int state) { this.state = state; }

    public int getFileType() { return fileType; }
    public void setFileType(int fileType) { this.fileType = fileType; }

    public int getSubFileType() { return subFileType; }
    public void setSubFileType(int subFileType) { this.subFileType = subFileType; }

    public int getErrorCode() { return errorCode; }
    public void setErrorCode(int errorCode) { this.errorCode = errorCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraModelStatusChangedMsg that = (TerraModelStatusChangedMsg) o;
        return fileId == that.fileId && state == that.state && fileType == that.fileType
                && subFileType == that.subFileType && errorCode == that.errorCode
                && Objects.equals(uuid, that.uuid) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() { return Objects.hash(fileId, uuid, name, state, fileType, subFileType, errorCode); }

    @Override
    public String toString() {
        return "TerraModelStatusChangedMsg{fileId=" + fileId + ", uuid='" + uuid
                + "', name='" + name + "', state=" + state + ", fileType=" + fileType
                + ", subFileType=" + subFileType + ", errorCode=" + errorCode + '}';
    }
}
