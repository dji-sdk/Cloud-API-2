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

package com.dji.fh.kernel.realsdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.flighthubsdk.bean.AircraftAttitude;
import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * OsdAggregator 单元测试。
 *
 * <p>SDK bean（AircraftLocation / AircraftBatteryInfo / AircraftAttitude）都是 final class,
 * 测试用公共无参构造器 + setter 构造真实对象。
 *
 * @author DJI DevKit Team
 */
class OsdAggregatorTest {

    private static final Instant T0 = Instant.parse("2026-06-29T10:00:00Z");

    private final SdkEventBus bus = mock(SdkEventBus.class);
    private final RealSdkProperties props = new RealSdkProperties(
            "t", "h", "p", "wss://x", "info",
            Duration.ofSeconds(10), 1000L, Duration.ofSeconds(5),
            Duration.ofSeconds(20), 3, null, null, null);

    @Test
    void tick_shouldPublishOsdUpdateEvent_whenLocationAndBatteryUpdated() {
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);
        agg.updateLocation("SN-1", stubLocation(40.0, 120.0, 100.0, 50.0));
        agg.updateBattery("SN-1", stubBattery(85, 30, 15, 600));

        agg.tick();

        verify(bus).publish(argThat((OsdUpdateEvent e) ->
                "SN-1".equals(e.deviceSn())
                && "real-sdk".equals(e.source())
                && Double.valueOf(40.0).equals(e.osd().attributes().get("lat"))
                && Integer.valueOf(85).equals(e.osd().attributes().get("battery_percent"))));
    }

    @Test
    void tick_shouldSkipStaleSnapshot_whenLastUpdateBeyondTtl() {
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);
        agg.updateLocation("SN-OLD", stubLocation(40.0, 120.0, 100.0, 50.0));

        // 时钟前进 6 秒，超过 osdSnapshotTtl=5s
        Clock later = Clock.fixed(T0.plusSeconds(6), ZoneOffset.UTC);
        ReflectionTestUtils.setField(agg, "clock", later);

        agg.tick();

        verifyNoInteractions(bus);
    }

    @Test
    void tick_shouldPublishMultipleEvents_whenMultipleDevicesPresent() {
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);
        agg.updateLocation("SN-1", stubLocation(40.0, 120.0, 100.0, 50.0));
        agg.updateLocation("SN-2", stubLocation(41.0, 121.0, 200.0, 60.0));

        agg.tick();

        verify(bus, times(2)).publish(any(OsdUpdateEvent.class));
    }

    @Test
    void tick_shouldIsolateFailures_whenOneDevicePublishFails() {
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);
        agg.updateLocation("SN-1", stubLocation(40.0, 120.0, 100.0, 50.0));
        agg.updateLocation("SN-2", stubLocation(41.0, 121.0, 200.0, 60.0));
        doThrow(new RuntimeException("listener bug"))
                .when(bus).publish(argThat((OsdUpdateEvent e) -> "SN-1".equals(e.deviceSn())));

        agg.tick();  // 不应抛出

        // 两个 sn 都尝试发了（SN-1 异常被吞，SN-2 继续）
        verify(bus, times(2)).publish(any(OsdUpdateEvent.class));
    }

    @Test
    void updateLocation_shouldOverwriteExistingValue_whenSameSn() {
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);
        agg.updateLocation("SN-1", stubLocation(40.0, 120.0, 100.0, 50.0));
        agg.updateLocation("SN-1", stubLocation(50.0, 130.0, 200.0, 60.0));

        agg.tick();

        verify(bus).publish(argThat((OsdUpdateEvent e) ->
                Double.valueOf(50.0).equals(e.osd().attributes().get("lat"))));
    }

    @Test
    void clear_shouldRemoveAllSnapshots() {
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);
        agg.updateLocation("SN-1", stubLocation(40.0, 120.0, 100.0, 50.0));
        agg.clear();

        agg.tick();

        verifyNoInteractions(bus);
    }

    @Test
    void tick_shouldEmitOnlyAvailableFields_whenPartialUpdate() {
        // 只更新 location，不更新 battery / attitude
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);
        agg.updateLocation("SN-1", stubLocation(40.0, 120.0, 100.0, 50.0));

        agg.tick();

        verify(bus).publish(argThat((OsdUpdateEvent e) -> {
            assertThat(e.osd().attributes()).containsKeys("lat", "lng", "height", "elevation");
            assertThat(e.osd().attributes()).doesNotContainKeys(
                    "battery_percent", "head", "pitch", "roll");
            assertThat(e.osd().attributes()).containsKey("timestamp");
            return true;
        }));
    }

    @Test
    void updateLocation_shouldSilentlyIgnoreNullInputs() {
        OsdAggregator agg = new OsdAggregator(bus, Clock.fixed(T0, ZoneOffset.UTC), props);

        agg.updateLocation(null, stubLocation(40.0, 120.0, 100.0, 50.0));
        agg.updateLocation("SN-1", null);

        agg.tick();

        verifyNoInteractions(bus);
    }

    // ---------- 测试辅助 ----------

    private static AircraftLocation stubLocation(double lat, double lng, double height, double elevation) {
        AircraftLocation loc = new AircraftLocation();
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        loc.setHeight(height);
        loc.setElevation(elevation);
        return loc;
    }

    private static AircraftBatteryInfo stubBattery(int pct, int returnHome, int landing, int remainSec) {
        AircraftBatteryInfo batt = new AircraftBatteryInfo();
        batt.setCapacityPercent(pct);
        batt.setReturnHomePower(returnHome);
        batt.setLandingPower(landing);
        batt.setRemainFlightTime(remainSec);
        return batt;
    }
}
