// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 切换告警状态结果。 */
public final class SwitchAlarmStateResult {
    private String message;
    public SwitchAlarmStateResult() {
    }

/**
     * 构造 SwitchAlarmStateResult。
     *
     * @param message 消息
     */
    public SwitchAlarmStateResult(String message) {
        this.message = message != null ? message : "";
    }

    /** 消息 */
    public String getMessage() { return message; }

    /** 消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SwitchAlarmStateResult that = (SwitchAlarmStateResult) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "SwitchAlarmStateResult{message='" + message + '\'' + '}';
    }
}
