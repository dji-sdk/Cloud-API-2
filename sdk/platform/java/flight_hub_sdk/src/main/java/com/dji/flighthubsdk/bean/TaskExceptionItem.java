// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 任务异常项。 */
public final class TaskExceptionItem {
    private int code;
    private String message;
    private String sn;
    private long happenAt;

    public TaskExceptionItem() {
    }

    /**
     * 构造 TaskExceptionItem。
     *
     * @param code 设备/业务错误码
     * @param message 错误信息
     * @param sn 出错设备序列号
     * @param happenAt 发生时间，单位：秒
     */
    public TaskExceptionItem(int code, String message, String sn, long happenAt) {
        this.code = code;
        this.message = message != null ? message : "";
        this.sn = sn != null ? sn : "";
        this.happenAt = happenAt;
    }

    /** 设备/业务错误码 */
    public int getCode() { return code; }
    /** 错误信息 */
    public String getMessage() { return message; }
    /** 出错设备序列号 */
    public String getSn() { return sn; }
    /** 发生时间，单位：秒 */
    public long getHappenAt() { return happenAt; }

    /** 设备/业务错误码 */
    public void setCode(int code) {
        this.code = code;
    }

    /** 错误信息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 出错设备序列号 */
    public void setSn(String sn) {
        this.sn = sn;
    }

    /** 发生时间，单位：秒 */
    public void setHappenAt(long happenAt) {
        this.happenAt = happenAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskExceptionItem that = (TaskExceptionItem) o;
        return code == that.code && happenAt == that.happenAt
                && Objects.equals(message, that.message)
                && Objects.equals(sn, that.sn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, sn, happenAt);
    }

    @Override
    public String toString() {
        return "TaskExceptionItem{"
                + "code=" + code
                + ", message='" + message + '\''
                + ", sn='" + sn + '\''
                + ", happenAt=" + happenAt
                + '}';
    }
}
