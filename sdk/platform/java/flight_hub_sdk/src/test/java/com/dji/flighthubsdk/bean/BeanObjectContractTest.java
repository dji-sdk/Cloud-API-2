// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BeanObjectContractTest {

    @Test
    public void defaultConstructedBeansHaveSafeObjectContract() throws Exception {
        assertObjectContract(new AircraftBatteryInfo());
        assertObjectContract(new AircraftMaintainStatusInfo());
        assertObjectContract(new AiSeiInfo());
        assertObjectContract(new EisSeiInfo());
    }

    @Test
    public void nullFieldsHaveSafeObjectContract() throws Exception {
        AircraftBatteryInfo batteryInfo = new AircraftBatteryInfo();
        batteryInfo.setBatteries(null);
        assertObjectContract(batteryInfo);

        AircraftMaintainStatusInfo maintainStatusInfo = new AircraftMaintainStatusInfo();
        maintainStatusInfo.setMaintainStatuses(null);
        assertObjectContract(maintainStatusInfo);

        AiSeiInfo aiSeiInfo = new AiSeiInfo();
        aiSeiInfo.setTargets(null);
        assertObjectContract(aiSeiInfo);

        EisSeiInfo eisSeiInfo = new EisSeiInfo();
        eisSeiInfo.setLeftTop(null);
        eisSeiInfo.setRightTop(null);
        eisSeiInfo.setRightBottom(null);
        eisSeiInfo.setLeftBottom(null);
        assertObjectContract(eisSeiInfo);
    }

    private static void assertObjectContract(Object bean) throws Exception {
        bean.toString();
        bean.hashCode();
        assertTrue("reflexive equals failed: " + bean.getClass().getName(), bean.equals(bean));

        Object other = bean.getClass().getDeclaredConstructor().newInstance();
        bean.equals(other);
        other.equals(bean);
        bean.equals(null);
        bean.equals("not-a-bean");
    }
}
