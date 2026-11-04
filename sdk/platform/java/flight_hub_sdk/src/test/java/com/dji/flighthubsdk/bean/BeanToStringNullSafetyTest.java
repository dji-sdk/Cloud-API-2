// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 回归：默认构造 / set(null) 后 toString 不得抛 NPE。
 */
public class BeanToStringNullSafetyTest {

    @Test
    public void aircraftBatteryInfoDefaultCtorToStringDoesNotThrow() {
        AircraftBatteryInfo info = new AircraftBatteryInfo();
        assertTrue(info.toString().contains("batteries.size=0"));
        assertNotNull(info.getBatteries());
        assertEquals(info, new AircraftBatteryInfo());
        info.hashCode();
    }

    @Test
    public void aircraftBatteryInfoNullSetterToStringDoesNotThrow() {
        AircraftBatteryInfo info = new AircraftBatteryInfo();
        info.setBatteries(null);
        assertNotNull(info.getBatteries());
        assertTrue(info.toString().contains("batteries.size=0"));
    }

    @Test
    public void aircraftMaintainStatusInfoDefaultCtorToStringDoesNotThrow() {
        AircraftMaintainStatusInfo info = new AircraftMaintainStatusInfo();
        assertTrue(info.toString().contains("count=0"));
        assertNotNull(info.getMaintainStatuses());
        assertEquals(info, new AircraftMaintainStatusInfo());
        info.hashCode();
    }

    @Test
    public void aircraftMaintainStatusInfoNullSetterToStringDoesNotThrow() {
        AircraftMaintainStatusInfo info = new AircraftMaintainStatusInfo();
        info.setMaintainStatuses(null);
        assertNotNull(info.getMaintainStatuses());
        assertTrue(info.toString().contains("count=0"));
    }

    @Test
    public void paramCtorNullListNormalizedToEmptyList() {
        assertNotNull(new AircraftBatteryInfo(0, 0, 0, 0, null).getBatteries());
        assertNotNull(new AircraftMaintainStatusInfo(null).getMaintainStatuses());
    }
}
