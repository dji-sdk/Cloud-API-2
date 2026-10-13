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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 伪 SDK 入口，模拟真 DJI FlightHub PaaS SDK 的 listen/cancel callback 范式。
 *
 * <p>真 SDK 约定（项目根 CLAUDE.md）：每个 {@code listenXxx(callback)} 返回 {@code listen_id ≥ 0}，
 * 调用方负责 {@code cancelListenXxx(id)} 释放；callback 签名 {@code (data, error)}，
 * error.error_code == 0 才有 data。本 Mock 严格遵守该范式，便于真 SDK adapter 复用模式。
 *
 * <p>实现要点：
 * <ul>
 *   <li>listen_id 用 {@link AtomicLong} 单调递增，永不复用</li>
 *   <li>callback 列表用 {@link CopyOnWriteArrayList}，dispatch 期间 listen/cancel 不影响本次派发</li>
 *   <li>callback 抛异常被吞 + log warn，不影响其他 listener</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public class MockFlightHubSdk {

    private static final Logger LOG = LoggerFactory.getLogger(MockFlightHubSdk.class);

    /**
     * 模拟 SDK 错误对象 —— error_code == 0 表示成功。
     *
     * @param errorCode SDK 错误码
     * @param message   人类可读描述
     */
    public record SdkError(int errorCode, String message) {
        /** 成功常量。 */
        public static final SdkError OK = new SdkError(0, "OK");
    }

    /**
     * 通用 callback 签名。
     *
     * @param <T> 数据类型
     */
    @FunctionalInterface
    public interface SdkCallback<T> {
        /**
         * 接收数据。
         *
         * @param data  事件数据
         * @param error 错误对象（{@link SdkError#OK} 表示成功）
         */
        void onData(T data, SdkError error);
    }

    private final AtomicLong nextListenId = new AtomicLong(1);

    private final ConcurrentMap<Long, SdkCallback<DeviceEvent>> deviceOnlineListeners = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, SdkCallback<DeviceEvent>> deviceOfflineListeners = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CopyOnWriteArrayList<OsdListenEntry>> osdListeners = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, SdkCallback<FlightTaskEvent>> flightTaskListeners = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, SdkCallback<HmsEvent>> hmsListeners = new ConcurrentHashMap<>();

    /**
     * 注册设备上线 listener。
     *
     * @param callback 上线回调
     * @return listen_id（>0）
     */
    public long listenDeviceOnline(SdkCallback<DeviceEvent> callback) {
        Objects.requireNonNull(callback, "callback 不能为空");
        long id = nextListenId.getAndIncrement();
        deviceOnlineListeners.put(id, callback);
        return id;
    }

    /**
     * 取消设备上线 listener。未知 id 静默忽略。
     *
     * @param listenId listen_id
     */
    public void cancelListenDeviceOnline(long listenId) {
        deviceOnlineListeners.remove(listenId);
    }

    /**
     * 触发设备上线事件，派发给所有 deviceOnline listeners。
     *
     * @param sn         设备 SN
     * @param attributes 设备属性（如 model / name）
     */
    public void fireDeviceOnline(String sn, Map<String, Object> attributes) {
        Objects.requireNonNull(sn, "sn 不能为空");
        DeviceEvent event = new DeviceEvent(sn, attributes);
        dispatch(deviceOnlineListeners.values(), event, "deviceOnline");
    }

    /**
     * 注册设备下线 listener。
     *
     * @param callback 下线回调
     * @return listen_id
     */
    public long listenDeviceOffline(SdkCallback<DeviceEvent> callback) {
        Objects.requireNonNull(callback, "callback 不能为空");
        long id = nextListenId.getAndIncrement();
        deviceOfflineListeners.put(id, callback);
        return id;
    }

    /**
     * 取消设备下线 listener。
     *
     * @param listenId listen_id
     */
    public void cancelListenDeviceOffline(long listenId) {
        deviceOfflineListeners.remove(listenId);
    }

    /**
     * 触发设备下线事件。
     *
     * @param sn         设备 SN
     * @param attributes 设备属性（如 reason）
     */
    public void fireDeviceOffline(String sn, Map<String, Object> attributes) {
        Objects.requireNonNull(sn, "sn 不能为空");
        DeviceEvent event = new DeviceEvent(sn, attributes);
        dispatch(deviceOfflineListeners.values(), event, "deviceOffline");
    }

    /**
     * 注册指定 sn 的 OSD listener。同一 sn 可有多个 listener。
     *
     * @param sn       设备 SN
     * @param callback OSD 回调
     * @return listen_id
     */
    public long listenOsd(String sn, SdkCallback<Map<String, Object>> callback) {
        Objects.requireNonNull(sn, "sn 不能为空");
        Objects.requireNonNull(callback, "callback 不能为空");
        long id = nextListenId.getAndIncrement();
        osdListeners.computeIfAbsent(sn, k -> new CopyOnWriteArrayList<>())
                .add(new OsdListenEntry(id, callback));
        return id;
    }

    /**
     * 取消 OSD listener。
     *
     * @param listenId listen_id
     */
    public void cancelListenOsd(long listenId) {
        for (CopyOnWriteArrayList<OsdListenEntry> list : osdListeners.values()) {
            list.removeIf(e -> e.id == listenId);
        }
    }

    /**
     * 触发指定 sn 的 OSD 推送。
     *
     * @param sn   设备 SN
     * @param data OSD 数据
     */
    public void fireOsd(String sn, Map<String, Object> data) {
        Objects.requireNonNull(sn, "sn 不能为空");
        CopyOnWriteArrayList<OsdListenEntry> list = osdListeners.get(sn);
        if (list == null) {
            return;
        }
        for (OsdListenEntry entry : list) {
            invokeSafely(() -> entry.callback.onData(data, SdkError.OK), "osd[" + sn + "]");
        }
    }

    /**
     * 注册飞行任务状态变化 listener。
     *
     * @param callback 任务进度回调
     * @return listen_id
     */
    public long listenFlightTaskProgress(SdkCallback<FlightTaskEvent> callback) {
        Objects.requireNonNull(callback, "callback 不能为空");
        long id = nextListenId.getAndIncrement();
        flightTaskListeners.put(id, callback);
        return id;
    }

    /**
     * 取消飞行任务 listener。
     *
     * @param listenId listen_id
     */
    public void cancelListenFlightTaskProgress(long listenId) {
        flightTaskListeners.remove(listenId);
    }

    /**
     * 触发飞行任务进度事件。
     *
     * @param sn       设备 SN
     * @param taskId   任务 ID
     * @param status   任务状态（PENDING / RUNNING / COMPLETED / FAILED / CANCELLED）
     * @param progress 进度百分比（0~100）
     */
    public void fireFlightTaskProgress(String sn, String taskId, String status, int progress) {
        Objects.requireNonNull(sn, "sn 不能为空");
        Objects.requireNonNull(taskId, "taskId 不能为空");
        Objects.requireNonNull(status, "status 不能为空");
        FlightTaskEvent event = new FlightTaskEvent(sn, taskId, status, progress);
        dispatch(flightTaskListeners.values(), event, "flightTaskProgress");
    }

    /**
     * 注册 HMS 告警 listener。
     *
     * @param callback HMS 告警回调
     * @return listen_id
     */
    public long listenHmsAlert(SdkCallback<HmsEvent> callback) {
        Objects.requireNonNull(callback, "callback 不能为空");
        long id = nextListenId.getAndIncrement();
        hmsListeners.put(id, callback);
        return id;
    }

    /**
     * 取消 HMS 告警 listener。
     *
     * @param listenId listen_id
     */
    public void cancelListenHmsAlert(long listenId) {
        hmsListeners.remove(listenId);
    }

    /**
     * 触发 HMS 告警事件。
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
        HmsEvent event = new HmsEvent(sn, alertCode, level, message);
        dispatch(hmsListeners.values(), event, "hmsAlert");
    }

    private <T> void dispatch(java.util.Collection<SdkCallback<T>> callbacks, T data, String kind) {
        for (SdkCallback<T> cb : callbacks) {
            invokeSafely(() -> cb.onData(data, SdkError.OK), kind);
        }
    }

    private void invokeSafely(Runnable r, String kind) {
        try {
            r.run();
        } catch (RuntimeException e) {
            LOG.warn("mock SDK callback {} 抛异常被吞: {}", kind, e.getMessage(), e);
        }
    }

    /**
     * 设备上下线事件载体。
     *
     * @param sn         设备 SN
     * @param attributes 自由 key-value（model / reason 等）
     */
    public record DeviceEvent(String sn, Map<String, Object> attributes) {
    }

    /**
     * 飞行任务事件载体。
     *
     * @param sn       设备 SN
     * @param taskId   任务 ID
     * @param status   任务状态
     * @param progress 进度百分比
     */
    public record FlightTaskEvent(String sn, String taskId, String status, int progress) {
    }

    /**
     * HMS 告警事件载体。
     *
     * @param sn        设备 SN
     * @param alertCode 告警码
     * @param level     告警级别
     * @param message   告警消息
     */
    public record HmsEvent(String sn, String alertCode, String level, String message) {
    }

    private record OsdListenEntry(long id, SdkCallback<Map<String, Object>> callback) {
    }
}
