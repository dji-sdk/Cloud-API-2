// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设置距离限制结果。 */
public final class SetDistanceLimitResult {
    private String message;
    public SetDistanceLimitResult() {
    }

/**
     * 构造 SetDistanceLimitResult。
     *
     * @param message 消息
     */
    public SetDistanceLimitResult(String message) {
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
        SetDistanceLimitResult that = (SetDistanceLimitResult) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "SetDistanceLimitResult{message='" + message + '\'' + '}';
    }
}
