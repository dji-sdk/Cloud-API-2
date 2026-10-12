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

import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.event.DockOsdData;
import com.dji.fh.kernel.spi.event.DockOsdUpdateEvent;
import com.dji.fh.kernel.spi.event.KernelReadyEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * 真 SDK 启动序列编排。
 *
 * <p>{@code @EventListener(ApplicationReadyEvent.class)} 触发 5 步启动：
 * <ol>
 *   <li>{@code sdkInvoker.initialize(buildConfig())} 同步 init；errorCode != SUCCESS → 降级 return。</li>
 *   <li>注册 workspace 级 listen → 记录 {@code workspaceListenId}。</li>
 *   <li>调 {@code getDeviceList} + {@link CountDownLatch} 桥接拿初始设备清单（带超时）。</li>
 *   <li>按快照 {@code Device.isOnline()} 分支：在线设备注册 5 个 per-device listen
 *       （drone-online / hms / location / battery / attitude）并手动 publish {@link DeviceOnlineEvent}
 *       （SDK 不为已在线设备触发 ws callback）；离线设备只 publish {@link DeviceOfflineEvent}，不注册 listen。</li>
 *   <li>publish {@link KernelReadyEvent}{@code (\"real-sdk\", Mode.SDK, sdkVersion)}。</li>
 * </ol>
 *
 * <p>{@code @PreDestroy} 反向 cancel 所有 listen + SDK shutdown，每步 try-catch 隔离故障,
 * 单点失败不阻断其他清理（与 {@code MockSdkAdapter.unsubscribe()} 模板一致）。
 *
 * @author DJI DevKit Team
 */
public class RealSdkStarter {

    private static final Logger LOG = LoggerFactory.getLogger(RealSdkStarter.class);

    /** 标识 listen 类型，用于 perDeviceListenIds map */
    enum ListenKind {
        DRONE_ONLINE, HMS, LOCATION, BATTERY, ATTITUDE, DOCK_LOCATION, DOCK_ONLINE
    }

    /** 设备清单单项（从 SDK DeviceTopo.Device 提取的 A3b 需要的字段） */
    public record DeviceInfo(String sn, String deviceClass, String modelKey,
                              String deviceName, String nickname, boolean online) {
    }

    /** 启动时读到的初始快照即为离线的设备，下线事件 reason 取值 */
    private static final String OFFLINE_REASON_INITIAL_SNAPSHOT = "initial-snapshot-offline";

    private final SdkInvoker sdkInvoker;
    private final RealSdkAdapter adapter;
    private final OsdAggregator osdAggregator;
    private final RealSdkProperties props;
    private final SdkEventBus bus;

    /** workspace 级 listenId（&lt; 0 表示未注册或注册失败） */
    private volatile int workspaceListenId = -1;

    /** 每设备 listenId：sn → kind → id */
    private final Map<String, Map<ListenKind, Integer>> perDeviceListenIds = new ConcurrentHashMap<>();

    /** 重入防护：正在注册 listen 的 sn 集合（防止 SDK 同步回调 → Spring 事件 → 再次进入注册流程） */
    private final Set<String> currentlyRegistering = ConcurrentHashMap.newKeySet();

    /** dock listen 延迟重试调度器 */
    private final ScheduledExecutorService retryScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "dock-listen-retry");
                t.setDaemon(true);
                return t;
            });

    public RealSdkStarter(SdkInvoker sdkInvoker,
                          RealSdkAdapter adapter,
                          OsdAggregator osdAggregator,
                          RealSdkProperties props,
                          SdkEventBus bus) {
        this.sdkInvoker = sdkInvoker;
        this.adapter = adapter;
        this.osdAggregator = osdAggregator;
        this.props = props;
        this.bus = bus;
    }

    /**
     * 启动序列入口。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        // (0) 预加载 JNI 库（SDK demo 要求 setHeaders 在 initialize 之前，而 setHeaders 需要 JNI）
        try {
            sdkInvoker.loadJniLibrary();
            LOG.info("JNI 库预加载完成");
        } catch (Exception ex) {
            LOG.error("JNI 库预加载失败：{}，KernelReadyEvent 不发布", ex.getMessage());
            return;
        }

        // (1) 设置鉴权 headers（必须在 initialize 之前，SDK 初始化时会读取已设置的 headers）
        configureAuthHeaders();

        // (2) initialize
        FlightHubSdkErrorCode code = sdkInvoker.initialize(buildConfig());
        if (code != FlightHubSdkErrorCode.SUCCESS) {
            LOG.error("真 SDK initialize 失败：errorCode={}，KernelReadyEvent 不发布，app 照起", code);
            return;
        }

        LOG.info("真 SDK initialize 成功，版本={}", sdkInvoker.getVersion());

        // (2) workspace listen
        try {
            int wsId = sdkInvoker.listenWorkspaceDeviceChanged(
                    data -> safeCallback("workspace-device-change", "workspace",
                            () -> adapter.onWorkspaceDeviceChange(data)));
            if (wsId >= 0) {
                workspaceListenId = wsId;
                LOG.info("workspace listen 注册成功，listenId={}", wsId);
            } else {
                LOG.warn("workspace listen 注册返回负值 ({})，将跳过设备变更监听", wsId);
            }
        } catch (Exception ex) {
            LOG.warn("workspace listen 注册抛异常：{}", ex.getMessage());
        }

        // (3) initial device list
        List<DeviceInfo> devices = pullInitialDeviceList();

        // (4) per-device listen（仅在线设备）+ 手动补 DeviceOnlineEvent/DeviceOfflineEvent
        for (DeviceInfo device : devices) {
            DeviceType type = resolveDeviceType(device.deviceClass(), device.modelKey());
            if (device.online()) {
                registerListensForDevice(device.sn(), type);
                String displayName = (device.nickname() != null && !device.nickname().isBlank())
                        ? device.nickname() : device.deviceName();
                bus.publish(new DeviceOnlineEvent(
                        RealSdkAdapter.SOURCE,
                        device.sn(),
                        type,
                        device.modelKey(),
                        displayName));
            } else {
                bus.publish(new DeviceOfflineEvent(
                        RealSdkAdapter.SOURCE,
                        device.sn(),
                        type,
                        OFFLINE_REASON_INITIAL_SNAPSHOT));
            }
        }
        LOG.info("初始设备 per-device listen 注册完毕，count={}", devices.size());

        // (5) kernel ready
        bus.publish(new KernelReadyEvent(
                RealSdkAdapter.SOURCE,
                KernelReadyEvent.Mode.SDK,
                sdkInvoker.getVersion()));

        // (6) 周期调和：定时拉设备列表刷新存活信号（SDK listen 是事件驱动，不提供周期心跳）
        schedulePeriodicReconciliation();
    }

    // ---------- A3c 动态 listen ----------

    /**
     * 动态 listen：workspace callback 产生 DeviceOnlineEvent 后，如果该 sn 尚未注册
     * per-device listen，自动注册（补 A3b v0.1 gap #1）。
     *
     * <p>仅处理 real-sdk 自己发的事件，避免 mock profile 下误触发。
     */
    @EventListener
    public void onNewDeviceOnline(DeviceOnlineEvent event) {
        if (!RealSdkAdapter.SOURCE.equals(event.source())) {
            return;
        }
        String sn = event.deviceSn();
        if (perDeviceListenIds.containsKey(sn)) {
            return;
        }
        registerListensForDevice(sn, event.deviceType());
        LOG.info("动态注册 per-device listen：sn={}, type={}", sn, event.deviceType());
    }

    /**
     * 动态 unlisten：workspace callback 产生 DeviceOfflineEvent 后，
     * cancel 该 sn 的 per-device listen + 从 map 移除。
     */
    @EventListener
    public void onDeviceOffline(DeviceOfflineEvent event) {
        if (!RealSdkAdapter.SOURCE.equals(event.source())) {
            return;
        }
        String sn = event.deviceSn();
        Map<ListenKind, Integer> ids = perDeviceListenIds.remove(sn);
        if (ids == null) {
            return;
        }
        for (Map.Entry<ListenKind, Integer> entry : ids.entrySet()) {
            try {
                cancelByKind(sn, entry.getKey(), entry.getValue());
            } catch (Exception ex) {
                LOG.warn("动态 cancel {} listen 失败（sn={}）：{}", entry.getKey(), sn, ex.getMessage());
            }
        }
        LOG.info("动态 cancel per-device listen：sn={}", sn);
    }

    /**
     * 关闭序列：反向 cancel listen + SDK shutdown。每步 try-catch 隔离故障。
     */
    @PreDestroy
    public void shutdown() {
        LOG.info("RealSdkStarter 关闭中：开始反向 cancel listen");

        // 停止重试调度器，防止 shutdown 后仍有延迟任务执行
        retryScheduler.shutdownNow();

        // per-device listen
        for (Map.Entry<String, Map<ListenKind, Integer>> entry : perDeviceListenIds.entrySet()) {
            String sn = entry.getKey();
            for (Map.Entry<ListenKind, Integer> kindEntry : entry.getValue().entrySet()) {
                try {
                    cancelByKind(sn, kindEntry.getKey(), kindEntry.getValue());
                } catch (Exception ex) {
                    LOG.warn("cancel {} listen 失败（sn={}, id={}）：{}",
                            kindEntry.getKey(), sn, kindEntry.getValue(), ex.getMessage());
                }
            }
        }
        perDeviceListenIds.clear();

        // workspace listen
        if (workspaceListenId >= 0) {
            try {
                sdkInvoker.cancelListenWorkspaceDeviceChanged(workspaceListenId);
            } catch (Exception ex) {
                LOG.warn("cancel workspace listen 失败：{}", ex.getMessage());
            }
            workspaceListenId = -1;
        }

        // SDK shutdown
        try {
            FlightHubSdkErrorCode code = sdkInvoker.shutdown();
            if (code != FlightHubSdkErrorCode.SUCCESS) {
                LOG.warn("SDK shutdown 返回非 SUCCESS：{}", code);
            }
        } catch (Exception ex) {
            LOG.warn("SDK shutdown 抛异常：{}", ex.getMessage());
        }

        LOG.info("RealSdkStarter 关闭完成");
    }

    /**
     * 为单设备注册 per-device listen。
     *
     * <p>DRONE → 5 个 listen（drone-online / hms / location / battery / attitude）+ OSD 3 source 接到 aggregator。
     * <br>DOCK → 1 个 listen（dock-location）→ 发布 {@link DockOsdUpdateEvent}。
     *
     * <p>所有 listen callback 都是单参数（无 FlightHubError），订阅成功 / 失败信息在 listen_id 上。
     */
    private void registerListensForDevice(String sn, DeviceType deviceType) {
        if (!currentlyRegistering.add(sn)) {
            return;
        }
        try {
            Map<ListenKind, Integer> ids = new ConcurrentHashMap<>();

            // 先占位再注册，防止 SDK 同步回调触发 onNewDeviceOnline 重入
            perDeviceListenIds.put(sn, ids);

            if (deviceType == DeviceType.DOCK) {
                registerDockListens(sn, ids);
            } else {
                registerDroneListens(sn, ids);
                if (ids.isEmpty()) {
                    perDeviceListenIds.remove(sn);
                }
            }
        } catch (Exception ex) {
            LOG.warn("设备 {} per-device listen 注册失败：{}", sn, ex.getMessage());
        } finally {
            currentlyRegistering.remove(sn);
        }
    }

    /**
     * 注册 DOCK 级 listen：订阅机场 GPS 位置。
     *
     * <p>若订阅返回负值（SDK 通知 WebSocket 尚未就绪，错误码 -4），
     * 则延迟重试直到成功或达到最大重试次数。
     */
    private void registerDockListens(String sn, Map<ListenKind, Integer> ids) {
        int locId = attemptDockLocationListen(sn);
        if (locId >= 0) {
            ids.put(ListenKind.DOCK_LOCATION, locId);
        } else {
            scheduleDockListenRetry(sn, 1);
        }

        int onlineId = trackedListen(sn, ListenKind.DOCK_ONLINE,
                () -> sdkInvoker.listenDockOnlineStatus(sn,
                        data -> safeCallback("dock-online-status", sn,
                                () -> adapter.onDockOnlineStatus(sn, data))));
        if (onlineId >= 0) {
            ids.put(ListenKind.DOCK_ONLINE, onlineId);
        }
    }

    /**
     * 尝试注册 dock location listen。
     */
    private int attemptDockLocationListen(String sn) {
        return trackedListen(sn, ListenKind.DOCK_LOCATION,
                () -> sdkInvoker.listenDockLocation(sn, location ->
                        safeCallback("dock-location", sn, () -> {
                            Map<String, Object> attrs = Map.of(
                                    "lat", location.getLatitude(),
                                    "latitude", location.getLatitude(),
                                    "lng", location.getLongitude(),
                                    "longitude", location.getLongitude(),
                                    "height", location.getHeight(),
                                    "heading", location.getHeading());
                            bus.publish(new DockOsdUpdateEvent(
                                    RealSdkAdapter.SOURCE, sn, new DockOsdData(attrs)));
                        })));
    }

    /**
     * 延迟重试 dock location listen 注册。
     *
     * <p>SDK 通知 WebSocket 连接需要时间（生产环境约 16 秒），初始注册可能因通知中心
     * 未就绪而失败（错误码 -4）。本方法在延迟后重试，直到成功或达到最大次数。
     */
    private void scheduleDockListenRetry(String sn, int attempt) {
        if (attempt > props.dockListenMaxRetries()) {
            LOG.error("设备 {} 的 DOCK_LOCATION listen 在 {} 次重试后仍失败，放弃", sn, attempt - 1);
            return;
        }
        long delaySec = props.dockListenRetryDelay().getSeconds();
        LOG.info("设备 {} 的 DOCK_LOCATION listen 注册失败，将在 {}s 后第 {} 次重试",
                sn, delaySec, attempt);
        retryScheduler.schedule(() -> {
            try {
                if (!perDeviceListenIds.containsKey(sn)) {
                    LOG.info("设备 {} 已离线，取消 DOCK_LOCATION listen 重试", sn);
                    return;
                }
                int locId = attemptDockLocationListen(sn);
                if (locId >= 0) {
                    perDeviceListenIds.computeIfAbsent(sn, k -> new ConcurrentHashMap<>())
                            .put(ListenKind.DOCK_LOCATION, locId);
                    LOG.info("设备 {} 的 DOCK_LOCATION listen 第 {} 次重试成功，listenId={}",
                            sn, attempt, locId);
                } else {
                    scheduleDockListenRetry(sn, attempt + 1);
                }
            } catch (Exception ex) {
                LOG.warn("设备 {} 的 DOCK_LOCATION listen 重试异常：{}", sn, ex.getMessage());
                scheduleDockListenRetry(sn, attempt + 1);
            }
        }, delaySec, TimeUnit.SECONDS);
    }

    /**
     * 注册 DRONE 级 listen：5 个 Aircraft 回调 + OSD 3 source 接到 aggregator。
     */
    private void registerDroneListens(String sn, Map<ListenKind, Integer> ids) {
        int droneOnlineId = trackedListen(sn, ListenKind.DRONE_ONLINE,
                () -> sdkInvoker.listenDroneOnlineStatus(sn,
                        data -> safeCallback("drone-online-status", sn,
                                () -> adapter.onDroneOnlineStatus(sn, DeviceType.DRONE, data))));
        if (droneOnlineId >= 0) {
            ids.put(ListenKind.DRONE_ONLINE, droneOnlineId);
        }

        int hmsId = trackedListen(sn, ListenKind.HMS,
                () -> sdkInvoker.listenAircraftHMS(sn,
                        data -> safeCallback("hms", sn,
                                () -> adapter.onAircraftHMS(sn, data))));
        if (hmsId >= 0) {
            ids.put(ListenKind.HMS, hmsId);
        }

        int locId = trackedListen(sn, ListenKind.LOCATION,
                () -> sdkInvoker.listenAircraftLocation(sn,
                        loc -> safeCallback("location", sn,
                                () -> osdAggregator.updateLocation(sn, loc))));
        if (locId >= 0) {
            ids.put(ListenKind.LOCATION, locId);
        }

        int battId = trackedListen(sn, ListenKind.BATTERY,
                () -> sdkInvoker.listenAircraftBatteryInfo(sn,
                        batt -> safeCallback("battery", sn,
                                () -> osdAggregator.updateBattery(sn, batt))));
        if (battId >= 0) {
            ids.put(ListenKind.BATTERY, battId);
        }

        int attId = trackedListen(sn, ListenKind.ATTITUDE,
                () -> sdkInvoker.listenAircraftAttitude(sn,
                        att -> safeCallback("attitude", sn,
                                () -> osdAggregator.updateAttitude(sn, att))));
        if (attId >= 0) {
            ids.put(ListenKind.ATTITUDE, attId);
        }
    }

    /**
     * 调 listen API，记 WARN 若返回负值。
     */
    private int trackedListen(String sn, ListenKind kind, IntSupplier listenCall) {
        int id = listenCall.getAsInt();
        if (id < 0) {
            LOG.warn("设备 {} 的 {} listen 注册返回负值 ({})，跳过", sn, kind, id);
        }
        return id;
    }

    /**
     * SDK callback 安全包装：吞掉所有异常，防止 Java 异常穿透 SWIG director 导致 C++ abort。
     *
     * <p>SDK listen callback 通过 SWIG director（JNI → C++ → Java）调用。
     * 若 Java 侧抛出任何异常，SWIG 生成的 director 代码无法处理，
     * 会触发 {@code Swig::DirectorException} → C++ {@code abort()} → JVM SIGSEGV。
     */
    private void safeCallback(String callbackName, String sn, Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            LOG.error("{} callback 异常（sn={}），已吞掉防止 SWIG DirectorException", callbackName, sn, ex);
        }
    }

    /**
     * 按 ListenKind 调对应的 cancel API。
     */
    private void cancelByKind(String sn, ListenKind kind, int id) {
        switch (kind) {
            case DRONE_ONLINE -> sdkInvoker.cancelListenDroneOnlineStatus(sn, id);
            case HMS -> sdkInvoker.cancelListenAircraftHMS(sn, id);
            case LOCATION -> sdkInvoker.cancelListenAircraftLocation(sn, id);
            case BATTERY -> sdkInvoker.cancelListenAircraftBatteryInfo(sn, id);
            case ATTITUDE -> sdkInvoker.cancelListenAircraftAttitude(sn, id);
            case DOCK_LOCATION -> sdkInvoker.cancelListenDockLocation(sn, id);
            case DOCK_ONLINE -> sdkInvoker.cancelListenDockOnlineStatus(sn, id);
            default -> throw new IllegalStateException("未知的 ListenKind: " + kind);
        }
    }

    /**
     * 启动周期性设备状态调和。
     *
     * <p>SDK 的 {@code listenDroneOnlineStatus} / {@code listenDockOnlineStatus} 是事件驱动的
     * （仅在在线状态变化时回调），不提供周期性心跳信号。{@code DeviceStatusService} 的存活超时
     * 兜底机制需要周期性的 {@link DeviceOnlineEvent} 来刷新 {@code lastSeenAt}。
     *
     * <p>本方法启动一个定时任务：每 {@code reconcileInterval} 调一次 {@code getDeviceList}，
     * 为 SDK 仍判定在线的设备发布 {@link DeviceOnlineEvent}，充当心跳替代。
     * 同时补偿启动时 workspace listen 注册失败的情况。
     */
    private void schedulePeriodicReconciliation() {
        long intervalSec = props.reconcileInterval().getSeconds();
        if (intervalSec <= 0) {
            LOG.info("设备状态周期调和已关闭（reconcile-interval=0）");
            return;
        }
        retryScheduler.scheduleAtFixedRate(() -> {
            try {
                reconcileDeviceStatus();
            } catch (Exception ex) {
                LOG.warn("设备状态周期调和异常：{}", ex.getMessage());
            }
        }, intervalSec, intervalSec, TimeUnit.SECONDS);
        LOG.info("设备状态周期调和已启动，间隔={}s", intervalSec);
    }

    /**
     * 执行一轮设备状态调和。
     *
     * <p>1. 若 workspace listen 注册失败（{@code workspaceListenId < 0}），重试一次。
     * <br>2. 拉 SDK 设备列表，为在线设备发布 {@link DeviceOnlineEvent} 刷新 {@code lastSeenAt}。
     */
    private void reconcileDeviceStatus() {
        retryWorkspaceListenIfNeeded();

        List<DeviceInfo> devices = pullInitialDeviceList();
        int refreshed = 0;
        for (DeviceInfo device : devices) {
            if (device.online()) {
                DeviceType type = resolveDeviceType(device.deviceClass(), device.modelKey());
                String displayName = (device.nickname() != null && !device.nickname().isBlank())
                        ? device.nickname() : device.deviceName();
                bus.publish(new DeviceOnlineEvent(
                        RealSdkAdapter.SOURCE, device.sn(), type,
                        device.modelKey(), displayName));
                refreshed++;
            }
        }
        LOG.info("设备状态调和完成：{}/{} 台在线设备已刷新 lastSeenAt", refreshed, devices.size());
    }

    /**
     * 若 workspace listen 尚未成功注册，重试一次。
     */
    private void retryWorkspaceListenIfNeeded() {
        if (workspaceListenId >= 0) {
            return;
        }
        try {
            int wsId = sdkInvoker.listenWorkspaceDeviceChanged(
                    data -> safeCallback("workspace-device-change", "workspace",
                            () -> adapter.onWorkspaceDeviceChange(data)));
            if (wsId >= 0) {
                workspaceListenId = wsId;
                LOG.info("workspace listen 重试成功，listenId={}", wsId);
            } else {
                LOG.debug("workspace listen 重试返回负值 ({})", wsId);
            }
        } catch (Exception ex) {
            LOG.debug("workspace listen 重试异常：{}", ex.getMessage());
        }
    }

    /**
     * 拉初始设备清单。
     *
     * <p>SDK {@code Workspace.getDeviceList} 是 action 形式 callback（两参数 List+Error）,
     * 用 {@link CountDownLatch} 桥接同步 + {@code deviceListTimeout} 超时；超时返回空 list 降级。
     */
    protected List<DeviceInfo> pullInitialDeviceList() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<DeviceInfo>> ref = new AtomicReference<>(Collections.emptyList());

        try {
            sdkInvoker.getDeviceList((deviceList, error) -> {
                try {
                    if (error != null && !error.isSuccess()) {
                        LOG.warn("getDeviceList 错误：errorCode={}", error.getErrorCode());
                        return;
                    }
                    if (deviceList == null) {
                        return;
                    }
                    List<DeviceInfo> infos = new ArrayList<>(deviceList.size() * 2);
                    for (DeviceTopo topo : deviceList) {
                        Device d = topo.getDevice();
                        if (d == null) {
                            continue;
                        }
                        LOG.info("DeviceTopo 主设备：sn={}, class={}, model={}, name={}",
                                d.getDeviceSn(), d.getDeviceClass(), d.getModelKey(), d.getDeviceName());
                        infos.add(new DeviceInfo(
                                d.getDeviceSn(),
                                d.getDeviceClass(),
                                d.getModelKey(),
                                d.getDeviceName(),
                                d.getNickname(),
                                d.isOnline()));

                        List<Device> subDevices = topo.getSubDevices();
                        if (subDevices != null) {
                            for (Device sub : subDevices) {
                                if (sub == null) {
                                    continue;
                                }
                                LOG.info("DeviceTopo 子设备：sn={}, class={}, model={}, name={}",
                                        sub.getDeviceSn(), sub.getDeviceClass(),
                                        sub.getModelKey(), sub.getDeviceName());
                                infos.add(new DeviceInfo(
                                        sub.getDeviceSn(),
                                        sub.getDeviceClass(),
                                        sub.getModelKey(),
                                        sub.getDeviceName(),
                                        sub.getNickname(),
                                        sub.isOnline()));
                            }
                        }
                    }
                    ref.set(infos);
                } finally {
                    latch.countDown();
                }
            });
        } catch (Exception ex) {
            LOG.warn("getDeviceList 调用抛异常：{}", ex.getMessage());
            return Collections.emptyList();
        }

        try {
            boolean ok = latch.await(props.deviceListTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!ok) {
                LOG.warn("getDeviceList 超时 ({} ms)，跳过初始 per-device listen 注册",
                        props.deviceListTimeout().toMillis());
                return Collections.emptyList();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.warn("getDeviceList 等待被中断");
            return Collections.emptyList();
        }

        return ref.get();
    }

    /**
     * 在 initialize 之前设置 WebSocket + HTTP 鉴权 headers。
     *
     * <p>SDK demo 正确顺序：{@code loadLibrary()} → {@code setHeaders()} → {@code initialize()}。
     * initialize 内部初始化 HTTP/WebSocket client 时会读取已设置的 headers，
     * 若在 initialize 之后设置，GET 请求可正常携带 headers，但 FORM POST（如 importKmz）不会携带。
     *
     * <p>前提：{@code loadJniLibrary()} 已成功执行。
     */
    private void configureAuthHeaders() {
        if (props.httpAuthHeaderKey() != null && !props.httpAuthHeaderKey().isBlank()) {
            sdkInvoker.setWebSocketCustomAuthHeaders(
                    props.httpAuthHeaderKey(), props.httpAuthHeaderValue());
            LOG.info("SDK WebSocket 鉴权 header 已设置（auth_keys 模式）：auth_keys={}",
                    props.httpAuthHeaderKey());

            sdkInvoker.setHttpHeaders(props.httpAuthHeaderKey(), props.httpAuthHeaderValue());
            LOG.info("SDK HTTP 鉴权 header 已设置：{}={}", props.httpAuthHeaderKey(), "***");
        } else if (props.token() != null && !props.token().isBlank()) {
            sdkInvoker.setWebSocketAuthHeaders(props.token());
            LOG.info("SDK WebSocket 鉴权 header 已设置（Bearer 模式）");
        } else {
            LOG.warn("SDK 鉴权均未配置（token 和 httpAuthHeaderKey 均为空），通知订阅和 HTTP 请求可能失败");
        }
    }

    /**
     * 从 RealSdkProperties 构造 FlightHubSdkConfig。
     *
     * <p>SDK 2.0.0 FlightHubSdkConfig 字段（javap 实查）：address / port /
     * notificationServerAddress / logLevel / logToConsole / showVersionInfo / logFilePath /
     * logMaxFileSize / logMaxFiles。
     */
    private FlightHubSdkConfig buildConfig() {
        FlightHubSdkConfig config = new FlightHubSdkConfig();
        config.setAddress(props.address());
        config.setPort(props.port());
        config.setNotificationServerAddress(props.notificationServerAddress());
        config.setLogLevel(props.logLevel());
        return config;
    }

    /**
     * SDK Device.deviceClass 字符串映射到 SPI {@link DeviceType}。
     *
     * <p>SDK 返回的 deviceClass 可能值：
     * <ul>
     *   <li>"dock" / "DOCK" / "airport" / "AIRPORT" → {@link DeviceType#DOCK}</li>
     *   <li>"base_station" → O4 地面站遥控器，无 DockOsdModule，归类为 DRONE</li>
     *   <li>以 "1-" 开头的 modelKey（domain=1，gateway/dock） → {@link DeviceType#DOCK}</li>
     *   <li>空串 / null / 其他 → {@link DeviceType#DRONE}</li>
     * </ul>
     */
    private DeviceType mapType(String sdkDeviceClass) {
        if (sdkDeviceClass == null || sdkDeviceClass.isBlank()) {
            return DeviceType.DRONE;
        }
        return switch (sdkDeviceClass.toUpperCase()) {
            case "DOCK", "AIRPORT" -> DeviceType.DOCK;
            default -> DeviceType.DRONE;
        };
    }

    /**
     * 通过 modelKey 推断设备类型。
     *
     * <p>DJI 设备 modelKey 格式 "{domain}-{type}-{subType}"：
     * <ul>
     *   <li>domain 0 = sub-device（飞行器挂载在 dock 下）</li>
     *   <li>domain 1 = gateway（机场 / 遥控器）</li>
     *   <li>domain 2 = payload（负载 / 相机）</li>
     *   <li>domain 3 = standalone aircraft</li>
     * </ul>
     */
    private DeviceType inferTypeFromModelKey(String modelKey) {
        if (modelKey == null || modelKey.isBlank()) {
            return DeviceType.DRONE;
        }
        String[] parts = modelKey.split("-");
        if (parts.length >= 1) {
            try {
                int domain = Integer.parseInt(parts[0]);
                if (domain == 1) {
                    return DeviceType.DOCK;
                }
            } catch (NumberFormatException ignored) {
                // modelKey 格式无法解析，降级为 DRONE
            }
        }
        return DeviceType.DRONE;
    }

    /**
     * 组合策略解析设备类型：优先用 deviceClass，fallback 用 modelKey domain。
     */
    private DeviceType resolveDeviceType(String deviceClass, String modelKey) {
        DeviceType fromClass = mapType(deviceClass);
        if (fromClass == DeviceType.DOCK) {
            return DeviceType.DOCK;
        }
        return inferTypeFromModelKey(modelKey);
    }

    // ---------- 测试可见 ----------

    /**
     * 仅测试用：暴露 workspace listenId 便于断言。
     * 生产代码不应调本方法（package-private 故意限制可见性）。
     */
    int workspaceListenId() {
        return workspaceListenId;
    }

    /**
     * 仅测试用：暴露 per-device listenIds map 便于断言注册结果。
     * 生产代码不应调本方法。返回 ConcurrentHashMap 实例本身（非 unmodifiable view），
     * 测试可读但请勿写入污染生产状态。
     */
    Map<String, Map<ListenKind, Integer>> perDeviceListenIds() {
        return perDeviceListenIds;
    }
}
