// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设置剩余电源For返航结果。 */
public final class SetRemainingPowerForRthResult {
    private String message;
    public SetRemainingPowerForRthResult() {
    }

/**
     * 构造 SetRemainingPowerForRthResult。
     *
     * @param message 消息
     */
    public SetRemainingPowerForRthResult(String message) {
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
        SetRemainingPowerForRthResult that = (SetRemainingPowerForRthResult) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "SetRemainingPowerForRthResult{message='" + message + '\'' + '}';
    }
}
