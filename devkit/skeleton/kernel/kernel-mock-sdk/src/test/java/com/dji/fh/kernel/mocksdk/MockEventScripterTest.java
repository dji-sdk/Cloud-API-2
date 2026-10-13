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

import com.dji.fh.kernel.spi.event.DeviceType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class MockEventScripterTest {

    @Test
    void play_shouldFireScriptedEvents_inOrder() throws InterruptedException {
        MockFlightHubSdk sdk = new MockFlightHubSdk();
        MockDeviceRegistry registry = new MockDeviceRegistry();
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        registry.register("DOCK-1", DeviceType.DOCK, "dock");
        MockEventTrigger trigger = new MockEventTrigger(sdk, registry);

        AtomicInteger online = new AtomicInteger();
        sdk.listenDeviceOnline((data, err) -> online.incrementAndGet());

        MockSdkProperties.ScriptEvent ev1 = scriptEvent(50, "device-online", "drone", null);
        MockSdkProperties.ScriptEvent ev2 = scriptEvent(100, "device-online", "dock", null);

        MockSdkProperties props = new MockSdkProperties();
        props.getDevice().setSn("DRONE-1");
        props.getDock().setSn("DOCK-1");
        props.getScript().setEvents(List.of(ev1, ev2));

        MockEventScripter scripter = new MockEventScripter(trigger, props);
        scripter.play();

        await().atMost(2, TimeUnit.SECONDS).until(() -> online.get() == 2);
        scripter.shutdown();
    }

    @Test
    void play_shouldNoOp_whenScriptDisabled() throws InterruptedException {
        MockFlightHubSdk sdk = new MockFlightHubSdk();
        MockDeviceRegistry registry = new MockDeviceRegistry();
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        MockEventTrigger trigger = new MockEventTrigger(sdk, registry);

        AtomicInteger online = new AtomicInteger();
        sdk.listenDeviceOnline((data, err) -> online.incrementAndGet());

        MockSdkProperties props = new MockSdkProperties();
        props.getScript().setEnabled(false);
        props.getScript().setEvents(List.of(scriptEvent(10, "device-online", "drone", null)));

        MockEventScripter scripter = new MockEventScripter(trigger, props);
        scripter.play();

        Thread.sleep(100);
        assertThat(online.get()).isZero();
        scripter.shutdown();
    }

    @Test
    void play_shouldNoOpButLogHint_whenEventsEmpty() throws InterruptedException {
        MockFlightHubSdk sdk = new MockFlightHubSdk();
        MockDeviceRegistry registry = new MockDeviceRegistry();
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        MockEventTrigger trigger = new MockEventTrigger(sdk, registry);

        AtomicInteger online = new AtomicInteger();
        sdk.listenDeviceOnline((data, err) -> online.incrementAndGet());

        MockSdkProperties props = new MockSdkProperties();
        // script enabled，但 events list 空
        assertThat(props.getScript().isEnabled()).isTrue();
        assertThat(props.getScript().getEvents()).isEmpty();

        MockEventScripter scripter = new MockEventScripter(trigger, props);
        scripter.play();

        Thread.sleep(100);
        assertThat(online.get()).isZero();
        scripter.shutdown();
    }

    @Test
    void play_shouldTolerateUnknownType_withoutThrowingOrTriggering() throws InterruptedException {
        MockFlightHubSdk sdk = new MockFlightHubSdk();
        MockDeviceRegistry registry = new MockDeviceRegistry();
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        MockEventTrigger trigger = new MockEventTrigger(sdk, registry);

        // 监听所有 5 种 callback 类型，任何被触发都会被计数到
        AtomicInteger anyCallback = new AtomicInteger();
        sdk.listenDeviceOnline((data, err) -> anyCallback.incrementAndGet());
        sdk.listenDeviceOffline((data, err) -> anyCallback.incrementAndGet());
        sdk.listenFlightTaskProgress((data, err) -> anyCallback.incrementAndGet());
        sdk.listenHmsAlert((data, err) -> anyCallback.incrementAndGet());
        sdk.listenOsd("DRONE-1", (data, err) -> anyCallback.incrementAndGet());

        MockSdkProperties props = new MockSdkProperties();
        props.getDevice().setSn("DRONE-1");
        props.getScript().setEvents(List.of(
                scriptEvent(20, "device-onlin", "drone", null),  // typo
                scriptEvent(40, "totally-bogus-type", "drone", null)
        ));

        MockEventScripter scripter = new MockEventScripter(trigger, props);
        // 不抛
        scripter.play();

        Thread.sleep(150);
        // 没触发任何 sdk callback
        assertThat(anyCallback.get()).isZero();
        scripter.shutdown();
    }

    private static MockSdkProperties.ScriptEvent scriptEvent(long at, String type, String device, String status) {
        MockSdkProperties.ScriptEvent ev = new MockSdkProperties.ScriptEvent();
        ev.setAtMillis(at);
        ev.setType(type);
        ev.setDevice(device);
        ev.setStatus(status);
        return ev;
    }
}
