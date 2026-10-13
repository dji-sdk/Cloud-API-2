// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.DroneModeCode;

import java.util.Objects;
/**
 * 飞机模式码推送。
 */
public final class AircraftModeCode {

    /** 取值语义同 `flight_hub_sdk::DroneModeCode`（见 `value.h`） */
    private DroneModeCode modeCode;
    public AircraftModeCode() {
    }

/**
     * 构造 AircraftModeCode。
     *
     * @param modeCode 模式代码
     */
    public AircraftModeCode(DroneModeCode modeCode) {
        this.modeCode = modeCode != null ? modeCode : DroneModeCode.UNKNOWN;
    }

    /** 取值语义同 `flight_hub_sdk::DroneModeCode`（见 `value.h`） */
    public DroneModeCode getModeCode() {
        return modeCode;
    }

    /** @param modeCode 取值语义同 `flight_hub_sdk::DroneModeCode`（见 `value.h`） */
    public void setModeCode(DroneModeCode modeCode) {
        this.modeCode = modeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftModeCode that = (AircraftModeCode) o;
        return modeCode == that.modeCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modeCode);
    }

    @Override
    public String toString() {
        return "AircraftModeCode{modeCode=" + modeCode + '}';
    }
}
