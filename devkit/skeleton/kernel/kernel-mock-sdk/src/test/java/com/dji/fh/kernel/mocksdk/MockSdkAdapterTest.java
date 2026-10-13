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

package com.dji.fh.kernel.mocksdk;

import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.event.FlightTaskProgressEvent;
import com.dji.fh.kernel.spi.event.FlightTaskStatus;
import com.dji.fh.kernel.spi.event.HmsAlertEvent;
import com.dji.fh.kernel.spi.event.HmsAlertLevel;
import com.dji.fh.kernel.spi.event.KernelEvent;
import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MockSdkAdapterTest {

    private MockFlightHubSdk sdk;
    private MockDeviceRegistry registry;
    private SdkEventBus bus;
    private MockSdkAdapter adapter;

    @BeforeEach
    void setUp() {
        sdk = new MockFlightHubSdk();
        registry = new MockDeviceRegistry();
        bus = mock(SdkEventBus.class);
        adapter = new MockSdkAdapter(sdk, registry, bus);
        adapter.subscribe();
    }

    @Test
    void deviceOnline_shouldBecomeDeviceOnlineEvent_whenFired() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");

        sdk.fireDeviceOnline("SN-1", Map.of("name", "Drone-1"));

        ArgumentCaptor<KernelEvent> cap = ArgumentCaptor.forClass(KernelEvent.class);
        verify(bus).publish(cap.capture());
        assertThat(cap.getValue()).isInstanceOf(DeviceOnlineEvent.class);
        DeviceOnlineEvent ev = (DeviceOnlineEvent) cap.getValue();
        assertThat(ev.deviceSn()).isEqualTo("SN-1");
        assertThat(ev.deviceType()).isEqualTo(DeviceType.DRONE);
        assertThat(ev.deviceModel()).isEqualTo("Mock Drone");
        assertThat(ev.deviceName()).isEqualTo("Drone-1");
        assertThat(ev.source()).isEqualTo("mock-sdk");
    }

    @Test
    void deviceOffline_shouldBecomeDeviceOfflineEvent_whenFired() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");

        sdk.fireDeviceOffline("SN-1", Map.of("reason", "timeout"));

        ArgumentCaptor<KernelEvent> cap = ArgumentCaptor.forClass(KernelEvent.class);
        verify(bus).publish(cap.capture());
        assertThat(cap.getValue()).isInstanceOf(DeviceOfflineEvent.class);
        DeviceOfflineEvent ev = (DeviceOfflineEvent) cap.getValue();
        assertThat(ev.deviceSn()).isEqualTo("SN-1");
        assertThat(ev.reason()).isEqualTo("timeout");
    }

    @Test
    void osd_shouldBecomeOsdUpdateEvent_whenFired() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");
        registry.markOnline("SN-1");
        adapter.subscribeOsd("SN-1");

        sdk.fireOsd("SN-1", Map.of("battery_percent", 90));

        ArgumentCaptor<KernelEvent> cap = ArgumentCaptor.forClass(KernelEvent.class);
        verify(bus).publish(cap.capture());
        assertThat(cap.getValue()).isInstanceOf(OsdUpdateEvent.class);
        OsdUpdateEvent ev = (OsdUpdateEvent) cap.getValue();
        assertThat(ev.deviceSn()).isEqualTo("SN-1");
        assertThat(ev.osd().get("battery_percent")).contains(90);
    }

    @Test
    void flightTask_shouldBecomeFlightTaskProgressEvent_whenFired() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");

        sdk.fireFlightTaskProgress("SN-1", "task-001", "RUNNING", 42);

        ArgumentCaptor<KernelEvent> cap = ArgumentCaptor.forClass(KernelEvent.class);
        verify(bus).publish(cap.capture());
        FlightTaskProgressEvent ev = (FlightTaskProgressEvent) cap.getValue();
        assertThat(ev.deviceSn()).isEqualTo("SN-1");
        assertThat(ev.taskId()).isEqualTo("task-001");
        assertThat(ev.status()).isEqualTo(FlightTaskStatus.RUNNING);
        assertThat(ev.progress()).isEqualTo(42);
    }

    @Test
    void hms_shouldBecomeHmsAlertEvent_whenFired() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");

        sdk.fireHmsAlert("SN-1", "CODE-001", "WARNING", "battery low");

        ArgumentCaptor<KernelEvent> cap = ArgumentCaptor.forClass(KernelEvent.class);
        verify(bus).publish(cap.capture());
        HmsAlertEvent ev = (HmsAlertEvent) cap.getValue();
        assertThat(ev.deviceSn()).isEqualTo("SN-1");
        assertThat(ev.alertCode()).isEqualTo("CODE-001");
        assertThat(ev.level()).isEqualTo(HmsAlertLevel.WARNING);
        assertThat(ev.message()).isEqualTo("battery low");
    }

    @Test
    void unsubscribe_shouldStopAllCallbacks_whenInvoked() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");
        adapter.subscribeOsd("SN-1");

        adapter.unsubscribe();

        // unsubscribe 后 fire 不应再产生任何 publish
        sdk.fireDeviceOnline("SN-1", Map.of());
        sdk.fireDeviceOffline("SN-1", Map.of());
        sdk.fireFlightTaskProgress("SN-1", "task-1", "RUNNING", 50);
        sdk.fireHmsAlert("SN-1", "C", "WARNING", "");
        sdk.fireOsd("SN-1", Map.of("battery_percent", 80));

        org.mockito.Mockito.verifyNoInteractions(bus);
    }
}
