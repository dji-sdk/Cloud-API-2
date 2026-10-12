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
import com.dji.fh.kernel.spi.event.OsdData;
import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 把 {@link MockFlightHubSdk} 的 5 种 callback 转成 Kernel SPI 的 KernelEvent，
 * 通过 {@link SdkEventBus#publish} 发到 Spring 事件总线。
 *
 * <p>所有发出的 KernelEvent 的 {@code source} 都是 {@code "mock-sdk"}，便于 ISV 区分。
 *
 * <p>使用方式：
 * <ol>
 *   <li>Spring 构造完成后调 {@link #subscribe()} 一次性注册全部全局 listener</li>
 *   <li>设备 register 完成后调 {@link #subscribeOsd(String)} 给该 sn 加 OSD listener</li>
 *   <li>Spring shutdown 时通过 {@code @Bean(destroyMethod="unsubscribe")} 自动调
 *       {@link #unsubscribe()} 释放所有 listen_id（与真 SDK 的 listen/cancel 契约对齐）</li>
 * </ol>
 *
 * <p><b>真 SDK adapter 参考</b>：本类作为后续真 SDK adapter plan 的模板，
 * listen_id 列表 + 按 kind 调 cancel 的模式必须保留。
 *
 * @author DJI DevKit Team
 */
public class MockSdkAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(MockSdkAdapter.class);

    /**
     * 发出的所有 KernelEvent 的 source 标记。
     */
    public static final String SOURCE = "mock-sdk";

    private final MockFlightHubSdk sdk;
    private final MockDeviceRegistry registry;
    private final SdkEventBus bus;

    /**
     * 已注册的非 OSD listen_id（按 kind 标记，用于按类型调对应 cancel 方法）。
     */
    private final List<TrackedListenId> globalListenIds = new CopyOnWriteArrayList<>();

    /**
     * 已注册的 OSD listen_id（所有 OSD listener 共用同一个 cancel 方法）。
     */
    private final List<Long> osdListenIds = new CopyOnWriteArrayList<>();

    /**
     * @param sdk      Mock SDK
     * @param registry 设备注册表（用于 callback 时查 deviceType / model）
     * @param bus      Kernel 事件总线
     */
    public MockSdkAdapter(MockFlightHubSdk sdk, MockDeviceRegistry registry, SdkEventBus bus) {
        this.sdk = Objects.requireNonNull(sdk, "sdk 不能为空");
        this.registry = Objects.requireNonNull(registry, "registry 不能为空");
        this.bus = Objects.requireNonNull(bus, "bus 不能为空");
    }

    /**
     * 注册全局 listener（设备上下线 / 飞行任务 / HMS）。OSD 按 sn 维度，要单独调 {@link #subscribeOsd}。
     */
    public void subscribe() {
        globalListenIds.add(new TrackedListenId(ListenKind.DEVICE_ONLINE,
                sdk.listenDeviceOnline((data, err) -> onDeviceOnline(data))));
        globalListenIds.add(new TrackedListenId(ListenKind.DEVICE_OFFLINE,
                sdk.listenDeviceOffline((data, err) -> onDeviceOffline(data))));
        globalListenIds.add(new TrackedListenId(ListenKind.FLIGHT_TASK,
                sdk.listenFlightTaskProgress((data, err) -> onFlightTask(data))));
        globalListenIds.add(new TrackedListenId(ListenKind.HMS,
                sdk.listenHmsAlert((data, err) -> onHms(data))));
    }

    /**
     * 给指定 sn 注册 OSD listener。
     *
     * @param sn 设备 SN
     */
    public void subscribeOsd(String sn) {
        Objects.requireNonNull(sn, "sn 不能为空");
        osdListenIds.add(sdk.listenOsd(sn, (data, err) -> onOsd(sn, data)));
    }

    /**
     * 释放全部已注册 listen_id。由 Spring 在 bean destroy 时调用，
     * 防止测试场景（@DirtiesContext context 重启）累积老 listener。
     */
    public void unsubscribe() {
        LOG.info("MockSdkAdapter 释放 {} 个全局 listener + {} 个 OSD listener",
                globalListenIds.size(), osdListenIds.size());
        for (TrackedListenId tracked : globalListenIds) {
            cancelByKind(tracked.kind(), tracked.id());
        }
        globalListenIds.clear();
        for (Long id : osdListenIds) {
            sdk.cancelListenOsd(id);
        }
        osdListenIds.clear();
    }

    private void cancelByKind(ListenKind kind, long id) {
        switch (kind) {
            case DEVICE_ONLINE -> sdk.cancelListenDeviceOnline(id);
            case DEVICE_OFFLINE -> sdk.cancelListenDeviceOffline(id);
            case FLIGHT_TASK -> sdk.cancelListenFlightTaskProgress(id);
            case HMS -> sdk.cancelListenHmsAlert(id);
            default -> throw new IllegalStateException("未知的 ListenKind: " + kind);
        }
    }

    private void onDeviceOnline(MockFlightHubSdk.DeviceEvent data) {
        MockDeviceRegistry.Device device = registry.find(data.sn()).orElse(null);
        DeviceType type = device != null ? device.type() : DeviceType.DRONE;
        String model = device != null ? device.model() : String.valueOf(data.attributes().get("model"));
        String name = String.valueOf(data.attributes().getOrDefault("name", model));
        bus.publish(new DeviceOnlineEvent(SOURCE, data.sn(), type, model, name));
    }

    private void onDeviceOffline(MockFlightHubSdk.DeviceEvent data) {
        MockDeviceRegistry.Device device = registry.find(data.sn()).orElse(null);
        DeviceType type = device != null ? device.type() : DeviceType.DRONE;
        String reason = String.valueOf(data.attributes().getOrDefault("reason", "normal"));
        bus.publish(new DeviceOfflineEvent(SOURCE, data.sn(), type, reason));
    }

    private void onOsd(String sn, Map<String, Object> data) {
        bus.publish(new OsdUpdateEvent(SOURCE, sn, new OsdData(data)));
    }

    private void onFlightTask(MockFlightHubSdk.FlightTaskEvent data) {
        FlightTaskStatus status = FlightTaskStatus.valueOf(data.status());
        bus.publish(new FlightTaskProgressEvent(SOURCE, data.sn(), data.taskId(), status, data.progress()));
    }

    private void onHms(MockFlightHubSdk.HmsEvent data) {
        HmsAlertLevel level = HmsAlertLevel.valueOf(data.level());
        bus.publish(new HmsAlertEvent(SOURCE, data.sn(), data.alertCode(), level, data.message()));
    }

    /**
     * listen_id 按 callback 类型分类。
     */
    private enum ListenKind {
        DEVICE_ONLINE, DEVICE_OFFLINE, FLIGHT_TASK, HMS
    }

    /**
     * listen_id 元组。
     *
     * @param kind callback 类型
     * @param id   listen_id
     */
    private record TrackedListenId(ListenKind kind, long id) {
    }
}
