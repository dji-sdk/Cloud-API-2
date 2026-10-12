// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * 多载荷云台姿态。
 */
public final class AircraftPayloadsGimbalAttitude {

    private List<AircraftPayloadGimbalAttitude> payloadsGimbalAttitude;
    public AircraftPayloadsGimbalAttitude() {
    }

/**
     * 构造 AircraftPayloadsGimbalAttitude。
     *
     * @param payloadsGimbalAttitude 载荷云台姿态列表
     */
    public AircraftPayloadsGimbalAttitude(List<AircraftPayloadGimbalAttitude> payloadsGimbalAttitude) {
        this.payloadsGimbalAttitude =
                payloadsGimbalAttitude != null ? payloadsGimbalAttitude : Collections.emptyList();
    }

    /** 载荷云台姿态列表 */
    public List<AircraftPayloadGimbalAttitude> getPayloadsGimbalAttitude() {
        return payloadsGimbalAttitude;
    }

    /** 载荷云台姿态列表 */
    public void setPayloadsGimbalAttitude(List<AircraftPayloadGimbalAttitude> payloadsGimbalAttitude) {
        this.payloadsGimbalAttitude = payloadsGimbalAttitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftPayloadsGimbalAttitude that = (AircraftPayloadsGimbalAttitude) o;
        return Objects.equals(payloadsGimbalAttitude, that.payloadsGimbalAttitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadsGimbalAttitude);
    }

    @Override
    public String toString() {
        return "AircraftPayloadsGimbalAttitude{payloadsGimbalAttitude=" + payloadsGimbalAttitude + '}';
    }
}
