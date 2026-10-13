// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 飞行任务异常明细。 */
public final class FlightTaskExceptionDetail {

    private int code;
    private String message;
    private int taskId;
    /** 时间字符串，如 "2025-09-17T04:01:00.972477409Z" */
    private String happenAt;
    private String sn;
    public FlightTaskExceptionDetail() {
    }

/**
     * 构造 FlightTaskExceptionDetail。
     *
     * @param code 错误码
     * @param message 错误消息
     * @param taskId 任务 ID
     * @param happenAt 发生时间
     * @param sn 设备序列号
     */
    public FlightTaskExceptionDetail(int code, String message, int taskId, String happenAt, String sn) {
        this.code = code;
        this.message = message != null ? message : "";
        this.taskId = taskId;
        this.happenAt = happenAt != null ? happenAt : "";
        this.sn = sn != null ? sn : "";
    }

    /** 错误码 */
    public int getCode() { return code; }
    /** 错误消息 */
    public String getMessage() { return message; }
    /** 任务 ID */
    public int getTaskId() { return taskId; }
    /** 时间字符串，如 "2025-09-17T04:01:00.972477409Z" */
    public String getHappenAt() { return happenAt; }
    /** 设备序列号 */
    public String getSn() { return sn; }

    /** 错误码 */
    public void setCode(int code) {
        this.code = code;
    }

    /** 错误消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 任务 ID */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /** @param happenAt 时间字符串，如 "2025-09-17T04:01:00.972477409Z" */
    public void setHappenAt(String happenAt) {
        this.happenAt = happenAt;
    }

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
        FlightTaskExceptionDetail that = (FlightTaskExceptionDetail) o;
        return code == that.code
                && Objects.equals(message, that.message)
                && taskId == that.taskId
                && Objects.equals(happenAt, that.happenAt)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                code,
                message,
                taskId,
                happenAt,
                sn);
    }

    @Override
    public String toString() {
        return "FlightTaskExceptionDetail{"
                + "code=" + code
                + ", message='" + message + '\''
                + ", taskId=" + taskId
                + ", happenAt='" + happenAt + '\''
                + ", sn='" + sn + '\''
                + '}';
    }
}
