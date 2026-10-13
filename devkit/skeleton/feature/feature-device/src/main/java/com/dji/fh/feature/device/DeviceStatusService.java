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

package com.dji.fh.feature.device;

import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.event.DockOsdUpdateEvent;
import com.dji.fh.kernel.spi.event.HmsAlertEvent;
import com.dji.fh.kernel.spi.event.OsdData;
import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * 设备状态管理核心 service。
 *
 * <p>消费 5 种 KernelEvent，维护内存设备表：
 * <ul>
 *   <li>{@link DeviceOnlineEvent} → 建立 / 刷新 entry，同时刷新存活时间</li>
 *   <li>{@link DeviceOfflineEvent} → 标离线（entry 不存在时也会建立）</li>
 *   <li>{@link OsdUpdateEvent} → 更新飞行器 OSD 快照，同时刷新存活时间</li>
 *   <li>{@link DockOsdUpdateEvent} → 更新机场 OSD 快照（转为 OsdData 存入 lastOsd），同时刷新存活时间</li>
 *   <li>{@link HmsAlertEvent} → 追加 HMS 告警，同时刷新存活时间</li>
 * </ul>
 *
 * <p><strong>存活超时兜底</strong>：事件驱动状态区分「显式上下线」与「心跳」——
 * {@link #onDeviceOnline} / {@link #onDeviceOffline} 改写 {@code online} 布尔量，
 * 而 OSD / HMS 只刷新 {@code lastSeenAt}（不碰 {@code online}）。读取时
 * （{@link #toDeviceState}）取二者合取：{@code online && 心跳未超时}。
 * 这样既能让迟到的 OSD 把设备从"超时离线"状态救回（避免抖动），
 * 也能在既无下线事件、平台 device 表又未回写时收敛为离线。
 * 超时时长由 {@code fh.skeleton.feature.device.liveness-timeout} 控制，设为 0 可关闭。
 *
 * <p>OSD / HMS 如果对应 sn 的 entry 不存在 → 静默丢弃（不为"幽灵设备"建 entry）。
 * 下线事件是例外：必须建 entry，否则「设备已下线但 SDK 快照仍为 online」无法被纠正。
 *
 * <p>ISV 可通过 {@code @ConditionalOnMissingBean} 替换本 service 的默认实现。
 *
 * @author DJI DevKit Team
 */
public class DeviceStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceStatusService.class);

    /** 设备列表缓存 TTL，避免每次 REST 请求都触发 SDK 同步查询 */
    private static final Duration DEVICE_LIST_CACHE_TTL = Duration.ofSeconds(5);

    private final ConcurrentHashMap<String, MutableDeviceEntry> devices = new ConcurrentHashMap<>();
    private final DeviceProperties props;
    private final DeviceQueryService queryService;
    private final Clock clock;

    /** 设备列表缓存：短 TTL 合并高频请求 */
    private volatile List<DeviceSnapshot> cachedSnapshots = List.of();
    private volatile Instant cacheExpiry = Instant.EPOCH;

    public DeviceStatusService(DeviceProperties props, DeviceQueryService queryService, Clock clock) {
        this.props = props;
        this.queryService = queryService;
        this.clock = clock;
    }

    // ---------- 事件消费 ----------

    /**
     * 设备上线：建立或刷新 entry。
     */
    @EventListener
    public void onDeviceOnline(DeviceOnlineEvent event) {
        MutableDeviceEntry entry = devices.computeIfAbsent(event.deviceSn(),
                sn -> new MutableDeviceEntry(props.hmsMaxPerDevice()));
        entry.applyOnline(event.deviceType(), event.deviceModel(), event.deviceName(), clock.instant());
        LOG.debug("设备上线：sn={}, type={}", event.deviceSn(), event.deviceType());
    }

    /**
     * 设备下线：标 offline，不删 entry（last known state 保留）。
     *
     * <p><strong>entry 不存在时必须补建</strong>：SDK 的设备列表查询（{@code getDeviceList()}）
     * 读的是平台 device 表，该表在设备离线后可能长期不回写，快照里的 {@code online} 会一直为
     * {@code true}。若此处因为"没有 entry"而丢弃下线事件，就再也没有任何信息能把该设备纠正为
     * 离线 —— 现场现象是机场 {@code lastOsd} 为空却返回 {@code online: true}。
     * 补建的 entry 只有 {@code online=false}，不含任何 OSD/HMS 数据。
     */
    @EventListener
    public void onDeviceOffline(DeviceOfflineEvent event) {
        MutableDeviceEntry entry = devices.computeIfAbsent(event.deviceSn(),
                sn -> new MutableDeviceEntry(props.hmsMaxPerDevice()));
        entry.applyOffline();
        LOG.debug("设备下线：sn={}, reason={}", event.deviceSn(), event.reason());
    }

    /**
     * OSD 更新：刷新 lastOsd 与存活时间。entry 不存在时静默丢弃。
     */
    @EventListener
    public void onOsdUpdate(OsdUpdateEvent event) {
        MutableDeviceEntry entry = devices.get(event.deviceSn());
        if (entry != null) {
            entry.applyOsd(event.osd(), clock.instant());
        }
    }

    /**
     * 机场 OSD 更新：将 DockOsdData 转为 OsdData 存入同一 lastOsd 字段。
     *
     * <p>DockOsdData 和 OsdData 内部都是 {@code Map<String, Object>}，结构相同。
     * 复用 lastOsd 字段使 DashboardService 无需修改即可读取 dock 坐标。
     */
    @EventListener
    public void onDockOsdUpdate(DockOsdUpdateEvent event) {
        MutableDeviceEntry entry = devices.get(event.deviceSn());
        if (entry != null) {
            OsdData osd = new OsdData(event.dockOsd().attributes());
            entry.applyOsd(osd, clock.instant());
        }
    }

    /**
     * HMS 告警：追加到 bounded deque，同时刷新存活时间。entry 不存在时静默丢弃。
     */
    @EventListener
    public void onHmsAlert(HmsAlertEvent event) {
        MutableDeviceEntry entry = devices.get(event.deviceSn());
        if (entry != null) {
            Instant now = clock.instant();
            entry.appendHms(new HmsAlert(
                    event.alertCode(),
                    event.level(),
                    event.message(),
                    now), now);
        }
    }

    // ---------- 查询方法 ----------

    /**
     * 全量设备列表：通过带缓存的 SDK 查询获取设备，叠加内存中的 OSD/HMS 缓存数据。
     *
     * <p><strong>层级去重</strong>：{@link DeviceQueryService#listDevices()} 返回的列表是扁平 + 嵌套
     * 并存的（见该接口 Javadoc）—— 挂载在机场下的飞机既出现在机场的 {@code subDevices()} 中，
     * 又作为独立顶层元素被追加一次。对外暴露的列表按「机场 + 挂载飞机」层级去重：
     * 凡是已成为<b>其他设备</b>子设备的 sn，都不再作为顶层元素返回（其状态仍可通过所属机场的
     * {@code childDevices} 读到）。同时按 sn 兜底去重，与 {@link #findBySn} 的行为对齐。
     *
     * <p>子设备集合中排除自身的自引用：畸形拓扑（设备把自己列为子设备）时若一并剔除，
     * 该设备会从接口中彻底消失，比重复返回更糟。
     *
     * @return 当前所有设备的状态列表（顶层按 sn 唯一）
     */
    public List<DeviceState> listAll() {
        List<DeviceSnapshot> snapshots = getCachedSnapshots();
        Set<String> mountedSns = snapshots.stream()
                .flatMap(snapshot -> snapshot.subDevices().stream()
                        .filter(child -> !snapshot.sn().equals(child.sn()))
                        .map(DeviceSnapshot::sn))
                .collect(Collectors.toSet());
        Map<String, DeviceSnapshot> topLevel = new LinkedHashMap<>();
        for (DeviceSnapshot snapshot : snapshots) {
            if (mountedSns.contains(snapshot.sn())) {
                continue;
            }
            topLevel.putIfAbsent(snapshot.sn(), snapshot);
        }
        return topLevel.values().stream()
                .map(this::toDeviceState)
                .toList();
    }

    /**
     * 按 sn 查单设备：优先从缓存的 SDK 查询结果中匹配，命中失败则 fallback 到内存缓存。
     *
     * @param sn 设备序列号
     * @return 设备状态，不存在时返回 empty
     */
    public Optional<DeviceState> findBySn(String sn) {
        // 从带 TTL 的缓存中查，不额外触发 SDK 调用
        Map<String, DeviceSnapshot> snapshotMap = getCachedSnapshots().stream()
                .collect(Collectors.toMap(DeviceSnapshot::sn, Function.identity(), (a, b) -> a));
        DeviceSnapshot match = snapshotMap.get(sn);
        if (match != null) {
            return Optional.of(toDeviceState(match));
        }
        // fallback：如果 SDK 查询失败但内存中有缓存
        MutableDeviceEntry entry = devices.get(sn);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry.snapshot(sn, isAlive(entry, clock.instant())));
    }

    /**
     * 判断事件缓存条目是否仍"存活"：距今最近的存活信号是否在 {@link DeviceProperties#livenessTimeout()} 内。
     *
     * <p>存活信号来源见类 Javadoc —— {@link DeviceOnlineEvent} 与 OSD / HMS 事件都会刷新
     * {@link MutableDeviceEntry#lastSeenAt}。超时时长设为 0（或负）表示关闭兜底，恒返回
     * {@code true}，语义退回「纯事件驱动」。
     *
     * <p>从未收到任何存活信号的条目（如由 {@link #onDeviceOffline} 补建的）恒为 {@code false}。
     *
     * @param entry 事件缓存条目
     * @param now   当前时刻
     * @return 心跳未超时返回 true
     */
    private boolean isAlive(MutableDeviceEntry entry, Instant now) {
        Duration timeout = props.livenessTimeout();
        // Duration.isPositive() 是 Java 18+ API，本工程基线为 Java 17
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            return true;
        }
        Instant lastSeenAt = entry.lastSeenAt;
        return lastSeenAt != null && !lastSeenAt.plus(timeout).isBefore(now);
    }

    /**
     * 获取带 TTL 缓存的设备快照列表。
     *
     * <p>缓存有效期内直接返回上次结果，过期后重新查询 SDK。
     * 使用 volatile + 时间戳实现轻量级缓存，避免引入额外依赖。
     *
     * @return 设备快照列表
     */
    private List<DeviceSnapshot> getCachedSnapshots() {
        Instant now = clock.instant();
        if (now.isBefore(cacheExpiry)) {
            return cachedSnapshots;
        }
        // 缓存过期，重新查询（多线程下可能重复查询，但 SDK 回调本身线程安全，可接受）
        List<DeviceSnapshot> fresh = queryService.listDevices();
        cachedSnapshots = fresh;
        cacheExpiry = now.plus(DEVICE_LIST_CACHE_TTL);
        return fresh;
    }

    /**
     * SDK 快照转 DeviceState，叠加内存中的 OSD/HMS 数据，递归映射子设备。
     *
     * <p>在线状态优先级：事件驱动缓存 &gt; SDK 查询结果。
     * SDK 的 {@code getDeviceList()} 返回的 online 字段可能滞后（平台 device 表未及时回写子设备下线事件），
     * 而 {@link DeviceOnlineEvent}/{@link DeviceOfflineEvent} 为实时推送，更具权威性。
     * 当内存中有事件缓存时，以缓存状态为准。
     *
     * <p>判定由「显式上下线」与「心跳存活」两个独立信号合取得出：
     * <ol>
     *   <li>{@link MutableDeviceEntry#applyOnline} / {@code applyOffline} 写 {@code online} ——
     *       表达平台推送的上下线意图；</li>
     *   <li>OSD / HMS 事件刷新 {@code lastSeenAt} —— 表达设备仍在产生数据的客观证据；</li>
     *   <li>{@link #isAlive} 判断心跳是否在 {@code livenessTimeout} 内。</li>
     * </ol>
     * 二者缺一即判离线。这样即使平台从未推下线事件（离线机场可能正是如此），
     * 只要心跳停了，也能在超时后自动收敛为离线。
     *
     * <p><strong>缓存自愈</strong>：事件驱动状态一旦被写成 offline，理论上只能靠后续
     * {@link DeviceOnlineEvent} 纠正——但 workspace 设备变更监听在通知中心 WS 未就绪时可能
     * 注册失败或收到迟到的错误事件（现场已复现：容器重启后 workspace listen 无重试，
     * 若此时误收一次 offline 变更，缓存会永久卡死在 offline，即使设备从未真正下线）。
     * {@link MutableDeviceEntry#reconcileOnline} 用 SDK 快照兜底：当 {@code cached.online=false}
     * 但快照连续 {@code reconcileConfirmWindow} 时长都判定为 online 时，纠正缓存为 online，
     * 视为一次迟到的上线事件。
     */
    private DeviceState toDeviceState(DeviceSnapshot snapshot) {
        String sn = snapshot.sn();
        MutableDeviceEntry cached = devices.get(sn);

        DeviceType type = resolveDeviceType(snapshot.deviceClass());
        String displayName = resolveDisplayName(snapshot);

        List<DeviceState> children = snapshot.subDevices().stream()
                .map(this::toDeviceState)
                .toList();

        // 事件驱动的在线状态优先于 SDK 查询结果，叠加存活超时兜底与缓存自愈
        Instant now = clock.instant();
        boolean online;
        if (cached != null) {
            boolean reconciledOnline = cached.reconcileOnline(sn, snapshot.online(), props.reconcileConfirmWindow(), now);
            online = reconciledOnline && isAlive(cached, now);
        } else {
            online = snapshot.online();
        }

        if (cached != null) {
            return new DeviceState(
                    sn,
                    type,
                    snapshot.modelKey(),
                    displayName,
                    online,
                    cached.lastOsd,
                    cached.lastOsdAt,
                    List.copyOf(cached.recentHmsSnapshot()),
                    // onlineSince 描述的是"本次在线起始时刻"，判离线时不应再对外暴露
                    online ? cached.onlineSince : null,
                    children);
        }

        return new DeviceState(
                sn,
                type,
                snapshot.modelKey(),
                displayName,
                online,
                null,
                null,
                List.of(),
                null,
                children);
    }

    /**
     * 解析对外展示的设备名称。
     *
     * <p>取 SDK 设备表返回的「用户自定义名称」（{@code nickname}，平台侧通过
     * {@code modifyDeviceName} 写入）优先，回退到设备出厂名 {@code deviceName}，
     * 两者都为空时兜底返回 sn —— 保证 API 的 {@code name} 字段永远非空，
     * 前端无需各自实现空值回退。
     *
     * @param snapshot SDK 设备快照
     * @return 非空的展示名称
     */
    private String resolveDisplayName(DeviceSnapshot snapshot) {
        if (snapshot.nickname() != null && !snapshot.nickname().isBlank()) {
            return snapshot.nickname();
        }
        if (snapshot.deviceName() != null && !snapshot.deviceName().isBlank()) {
            return snapshot.deviceName();
        }
        return snapshot.sn();
    }

    /**
     * 从 deviceClass 字符串解析设备类型。
     */
    private DeviceType resolveDeviceType(String deviceClass) {
        if (deviceClass == null || deviceClass.isBlank()) {
            return DeviceType.DRONE;
        }
        return switch (deviceClass.toUpperCase()) {
            case "DOCK", "AIRPORT", "BASE_STATION" -> DeviceType.DOCK;
            default -> DeviceType.DRONE;
        };
    }

    // ---------- 内部 mutable entry ----------

    /**
     * 内部 mutable 状态，不对外暴露。
     * volatile 字段保证 read 可见性；synchronized 写保多字段 ordering。
     */
    static class MutableDeviceEntry {

        private final int hmsMax;

        volatile DeviceType deviceType;
        volatile String model;
        volatile String name;

        /** 平台推送的显式在线意图，仅由 {@link #applyOnline} / {@link #applyOffline} 改写 */
        volatile boolean online;

        /** 距今最近一次存活信号时刻，由上线事件与 OSD / HMS 事件共同刷新 */
        volatile Instant lastSeenAt;

        volatile OsdData lastOsd;
        volatile Instant lastOsdAt;
        volatile Instant onlineSince;

        /** {@code online=false} 但 SDK 快照持续判定为 online 的起始时刻，用于缓存自愈计时 */
        volatile Instant mismatchSince;

        private final ArrayDeque<HmsAlert> recentHms = new ArrayDeque<>();

        MutableDeviceEntry(int hmsMax) {
            this.hmsMax = hmsMax;
        }

        synchronized void applyOnline(DeviceType type, String model, String name, Instant now) {
            this.deviceType = type;
            this.model = model;
            this.name = name;
            if (!this.online) {
                this.online = true;
                this.onlineSince = now;
            }
            this.lastSeenAt = now;
            this.mismatchSince = null;
        }

        /**
         * 标离线。刻意<b>不</b>清空 {@code lastSeenAt}：让后续迟到的 OSD 能通过存活判定
         * 自证在线（见 {@link #applyOsd}）。
         */
        synchronized void applyOffline() {
            this.online = false;
            this.onlineSince = null;
            this.mismatchSince = null;
        }

        /**
         * 用 SDK 实时快照的 online 判定纠正长期卡在 offline 的缓存（缓存自愈）。
         *
         * <p>只有 {@code online=false} 且快照<b>连续</b>判定为 online 达到 {@code window} 时长后
         * 才纠正，单次快照的抖动或短暂重连不会误纠正。纠正后视为一次迟到的上线事件：
         * {@code online=true}，并刷新 {@code onlineSince} / {@code lastSeenAt}，
         * 使随后的存活超时判定（{@link DeviceStatusService#isAlive}）立即通过。
         *
         * @param sn             设备 SN，仅用于日志
         * @param snapshotOnline 本次 SDK 快照的 online 判定
         * @param window         纠正确认窗口
         * @param now            当前时刻
         * @return 纠正后的 online 取值
         */
        synchronized boolean reconcileOnline(String sn, boolean snapshotOnline, Duration window, Instant now) {
            if (online) {
                mismatchSince = null;
                return true;
            }
            if (!snapshotOnline) {
                mismatchSince = null;
                return false;
            }
            if (mismatchSince == null) {
                mismatchSince = now;
                return false;
            }
            if (mismatchSince.plus(window).isAfter(now)) {
                return false;
            }
            LOG.warn("设备 {} 在线状态缓存自愈：事件缓存判定 offline 但 SDK 快照持续 {} 以上判定为 online，纠正为 online",
                    sn, window);
            online = true;
            onlineSince = now;
            lastSeenAt = now;
            mismatchSince = null;
            return true;
        }

        /**
         * 刷新 OSD 快照与存活时刻。刻意<b>不</b>触碰 {@code online}：
         * 显式上下线意图与心跳存活是两个独立信号，在读取时合取（见 {@link #toDeviceState}）。
         * 这样迟到的 OSD 能把"心跳超时"的设备救回，却不会推翻平台明确推送的下线事件。
         */
        synchronized void applyOsd(OsdData osd, Instant now) {
            this.lastOsd = osd;
            this.lastOsdAt = now;
            this.lastSeenAt = now;
        }

        synchronized void appendHms(HmsAlert alert, Instant now) {
            if (recentHms.size() >= hmsMax) {
                recentHms.pollFirst();
            }
            recentHms.addLast(alert);
            this.lastSeenAt = now;
        }

        /**
         * 获取 HMS 告警快照（线程安全）。
         */
        synchronized List<HmsAlert> recentHmsSnapshot() {
            return List.copyOf(recentHms);
        }

        DeviceState snapshot(String sn, boolean alive) {
            List<HmsAlert> hmsCopy;
            synchronized (this) {
                hmsCopy = List.copyOf(recentHms);
            }
            boolean onlineNow = online && alive;
            // name 只有 applyOnline 会写入：仅由下线事件补建的 entry 从未收到过名称。
            // 与 toDeviceState 的 resolveDisplayName 保持一致，兜底返回 sn，保证对外 name 非空。
            String displayName = name != null && !name.isBlank() ? name : sn;
            return new DeviceState(
                    sn,
                    deviceType,
                    model,
                    displayName,
                    onlineNow,
                    lastOsd,
                    lastOsdAt,
                    hmsCopy,
                    onlineNow ? onlineSince : null,
                    List.of());
        }
    }
}
