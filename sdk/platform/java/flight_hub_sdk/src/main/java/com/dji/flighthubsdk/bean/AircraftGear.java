// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/**
 * 飞机档位信息。
 */
public final class AircraftGear {

    /** 档位,{0: A, 1: P, 2: NAV, 3: FPV, 4: FARM, 5: S, 6: F, 7: M, 8: G, 9: T}; */
    private int gear;
    public AircraftGear() {
    }

/**
     * 构造 AircraftGear。
     *
     * @param gear 档位。枚举：0=A, 1=P, 2=NAV, 3=FPV, 4=FARM, 5=S, 6=F, 7=M, 8=G, 9=T
     */
    public AircraftGear(int gear) {
        this.gear = gear;
    }

    /** 档位,{0: A, 1: P, 2: NAV, 3: FPV, 4: FARM, 5: S, 6: F, 7: M, 8: G, 9: T}; */
    public int getGear() {
        return gear;
    }

    /** @param gear 档位,{0: A, 1: P, 2: NAV, 3: FPV, 4: FARM, 5: S, 6: F, 7: M, 8: G, 9: T}; */
    public void setGear(int gear) {
        this.gear = gear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AircraftGear that = (AircraftGear) o;
        return gear == that.gear;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gear);
    }

    @Override
    public String toString() {
        return "AircraftGear{gear=" + gear + '}';
    }
}
