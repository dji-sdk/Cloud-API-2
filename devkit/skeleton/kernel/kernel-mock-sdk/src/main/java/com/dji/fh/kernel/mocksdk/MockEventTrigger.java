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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Mock 事件触发的内部 API。
 *
 * <p>{@link MockEventScripter}（启动脚本回放）和 {@link MockRestController}（REST 手动触发）共享此 API，
 * 保证两条触发路径产生完全一致的 SDK callback → KernelEvent 行为。
 *
 * <p>每个 {@code fire*} 方法做两件事：
 * <ol>
 *   <li>更新 {@link MockDeviceRegistry} 中设备状态（如 markOnline）</li>
 *   <li>调 {@link MockFlightHubSdk} 对应 fire 方法，触发已注册 callback</li>
 * </ol>
 *
 * @author DJI DevKit Team
 */
public class MockEventTrigger {

    private final MockFlightHubSdk sdk;
    private final MockDeviceRegistry registry;

    /**
     * @param sdk      Mock SDK
     * @param registry 设备注册表
     */
    public MockEventTrigger(MockFlightHubSdk sdk, MockDeviceRegistry registry) {
        this.sdk = Objects.requireNonNull(sdk, "sdk 不能为空");
        this.registry = Objects.requireNonNull(registry, "registry 不能为空");
    }

    /**
     * 触发设备上线。设备必须先 register。
     *
     * @param sn 设备 SN
     * @throws IllegalArgumentException 当 sn 未注册
     */
    public void fireDeviceOnline(String sn) {
        Objects.requireNonNull(sn, "sn 不能为空");
        MockDeviceRegistry.Device device = registry.find(sn)
                .orElseThrow(() -> new IllegalArgumentException("device not registered: sn=" + sn));
        registry.markOnline(sn);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("model", device.model());
        attrs.put("name", device.model());
        attrs.put("type", device.type().name());
        sdk.fireDeviceOnline(sn, attrs);
    }

    /**
     * 触发设备下线。设备必须先 register。
     *
     * @param sn     设备 SN
     * @param reason 下线原因（free-form：normal / timeout / error）
     * @throws IllegalArgumentException 当 sn 未注册
     */
    public void fireDeviceOffline(String sn, String reason) {
        Objects.requireNonNull(sn, "sn 不能为空");
        registry.find(sn)
                .orElseThrow(() -> new IllegalArgumentException("device not registered: sn=" + sn));
        registry.markOffline(sn);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("reason", reason == null ? "normal" : reason);
        sdk.fireDeviceOffline(sn, attrs);
    }

    /**
     * 触发自定义 OSD 推送（手动触发版本，不走 {@link MockOsdScheduler} 周期）。
     *
     * @param sn   设备 SN
     * @param data OSD key-value
     */
    public void fireOsd(String sn, Map<String, Object> data) {
        Objects.requireNonNull(sn, "sn 不能为空");
        Objects.requireNonNull(data, "data 不能为空");
        sdk.fireOsd(sn, data);
    }

    /**
     * 触发飞行任务进度事件。
     *
     * @param sn       设备 SN
     * @param taskId   任务 ID
     * @param status   任务状态（PENDING / RUNNING / COMPLETED / FAILED / CANCELLED）
     * @param progress 进度（0~100）
     */
    public void fireFlightTaskProgress(String sn, String taskId, String status, int progress) {
        Objects.requireNonNull(sn, "sn 不能为空");
        Objects.requireNonNull(taskId, "taskId 不能为空");
        Objects.requireNonNull(status, "status 不能为空");
        sdk.fireFlightTaskProgress(sn, taskId, status, progress);
    }

    /**
     * 触发 HMS 告警。
     *
     * @param sn        设备 SN
     * @param alertCode 告警码
     * @param level     告警级别（INFO / WARNING / ERROR / CRITICAL）
     * @param message   告警消息
     */
    public void fireHmsAlert(String sn, String alertCode, String level, String message) {
        Objects.requireNonNull(sn, "sn 不能为空");
        Objects.requireNonNull(alertCode, "alertCode 不能为空");
        Objects.requireNonNull(level, "level 不能为空");
        sdk.fireHmsAlert(sn, alertCode, level, message == null ? "" : message);
    }

    /**
     * 用于内部测试 / 适配器需要查 device 元信息。
     *
     * @param sn 设备 SN
     * @return 设备快照
     */
    public Optional<MockDeviceRegistry.Device> findDevice(String sn) {
        return registry.find(sn);
    }

    /**
     * @return 已注册 drone（按 type 找第一个在线设备）
     */
    public Optional<MockDeviceRegistry.Device> findDrone() {
        return registry.findAllOnline().stream()
                .filter(d -> d.type() == DeviceType.DRONE)
                .findFirst();
    }
}
