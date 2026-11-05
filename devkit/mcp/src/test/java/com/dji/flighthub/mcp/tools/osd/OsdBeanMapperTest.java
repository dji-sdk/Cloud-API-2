/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.flighthub.mcp.tools.osd;

import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftWindSpeed;
import com.dji.flighthubsdk.bean.BatteryInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * OsdBeanMapper 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class OsdBeanMapperTest {

    @Test
    void toEvent_shouldMapBeanGettersToSnakeCaseKeys() {
        Map<String, Object> event = OsdBeanMapper.toEvent(new AircraftWindSpeed(270, 4.5));

        assertEquals(270, event.get("wind_direction"));
        assertEquals(4.5, event.get("wind_speed"));
        assertFalse(event.containsKey("class"));
    }

    @Test
    void toEvent_shouldMapNestedBeansInsideLists() {
        BatteryInfo battery = new BatteryInfo();
        battery.setCapacityPercent(88);
        AircraftBatteryInfo info = new AircraftBatteryInfo();
        info.setCapacityPercent(90);
        info.setBatteries(List.of(battery));

        Map<String, Object> event = OsdBeanMapper.toEvent(info);

        assertEquals(90, event.get("capacity_percent"));
        List<?> batteries = assertInstanceOf(List.class, event.get("batteries"));
        Map<?, ?> first = assertInstanceOf(Map.class, batteries.get(0));
        assertEquals(88, first.get("capacity_percent"));
    }

    @Test
    void toEvent_shouldWrapNonObjectPayload() {
        assertEquals(Map.of("value", 5), OsdBeanMapper.toEvent(5));
        assertEquals(Map.of("value", List.of("a", "b")), OsdBeanMapper.toEvent(List.of("a", "b")));
        Map<String, Object> nullEvent = OsdBeanMapper.toEvent(null);
        assertTrue(nullEvent.containsKey("value"));
        assertNull(nullEvent.get("value"));
    }

    @Test
    void map_shouldPassThroughSimpleTypes() {
        assertEquals("s", OsdBeanMapper.map("s", 0));
        assertEquals(true, OsdBeanMapper.map(true, 0));
        assertEquals(1.5, OsdBeanMapper.map(1.5, 0));
        assertEquals("c", OsdBeanMapper.map('c', 0));
        assertEquals("seq", OsdBeanMapper.map(new StringBuilder("seq"), 0));
    }

    @Test
    void map_shouldRenderEnumByName() {
        assertEquals("SECONDS", OsdBeanMapper.map(TimeUnit.SECONDS, 0));
    }

    @Test
    void map_shouldConvertMapsAndArrays() {
        Object mapped = OsdBeanMapper.map(Map.of(1, new int[]{1, 2}), 0);

        Map<?, ?> map = assertInstanceOf(Map.class, mapped);
        assertEquals(List.of(1, 2), map.get("1"));
    }

    @Test
    void map_shouldFallBackToToString_forUnknownTypes() {
        Date date = new Date(0);
        assertEquals(date.toString(), OsdBeanMapper.map(date, 0));
    }

    @Test
    void map_shouldStopAtMaxDepth() {
        Object nested = "leaf";
        for (int i = 0; i < OsdBeanMapper.MAX_DEPTH + 2; i++) {
            List<Object> wrapper = new ArrayList<>();
            wrapper.add(nested);
            nested = wrapper;
        }

        Object mapped = OsdBeanMapper.map(nested, 0);

        Object cursor = mapped;
        int levels = 0;
        while (cursor instanceof List<?> list) {
            cursor = list.get(0);
            levels++;
        }
        assertEquals(OsdBeanMapper.MAX_DEPTH, levels);
        assertInstanceOf(String.class, cursor);
    }

    @Test
    void toEvent_shouldReportNullForGettersThatThrow() {
        AircraftWindSpeed broken = mock(AircraftWindSpeed.class);
        when(broken.getWindSpeed()).thenThrow(new IllegalStateException("boom"));
        when(broken.getWindDirection()).thenReturn(10);

        Map<String, Object> event = OsdBeanMapper.toEvent(broken);

        assertEquals(10, event.get("wind_direction"));
        assertTrue(event.containsKey("wind_speed"));
        assertNull(event.get("wind_speed"));
    }
}
