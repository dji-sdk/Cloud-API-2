// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 设置夜航灯状态结果。 */
public final class SetNightLightsStateResult {
    private String message;
    public SetNightLightsStateResult() {
    }

/**
     * 构造 SetNightLightsStateResult。
     *
     * @param message 消息
     */
    public SetNightLightsStateResult(String message) {
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
        SetNightLightsStateResult that = (SetNightLightsStateResult) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "SetNightLightsStateResult{message='" + message + '\'' + '}';
    }
}
