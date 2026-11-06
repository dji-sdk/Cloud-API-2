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

package com.dji.flighthub.mcp.session;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 会话运行时状态。
 * 每个 session 绑定到一个 workspace 和一个 SDK 实例。
 *
 * @author DJI FlightHub SDK Team
 */
public class SessionContext {

    private final String sessionId;
    private final String workspaceUuid;
    private final String workspaceName;
    private final Instant createdAt;
    private final int maxSecurityLevel;

    /**
     * 会话所属用户标识（JWT 模式下从 token sub claim 提取，stdio 模式为 null）
     */
    private final String userId;

    /**
     * 当前正在控制的设备 SN（null 表示无控制权）
     */
    private volatile String activeControlDeviceSn;

    /**
     * 当前 DRC 链路绑定的设备 SN（null 表示无 DRC 连接）
     */
    private volatile String activeDrcDeviceSn;

    /**
     * 当前持有负载控制权的设备 SN（null 表示无负载控制权）
     */
    private volatile String activePayloadDeviceSn;

    /**
     * 当前持有负载控制权的 payload 索引（如 '81-0-0'）
     */
    private volatile String activePayloadIndex;

    /**
     * 活跃的飞行任务 ID（null 表示无进行中的飞行任务）
     */
    private volatile String activeFlightId;

    /**
     * 活跃的指点飞行 ID（null 表示无指点飞行）
     */
    private volatile String activeFlyToId;

    /**
     * 活跃的航线任务 UUID（null 表示无进行中的航线任务）
     */
    private volatile String activeWaylineTaskUuid;

    /**
     * 当前飞行阶段，默认为 IDLE
     */
    private volatile FlightPhase flightPhase = FlightPhase.IDLE;

    /**
     * 控制权获取时间（null 表示当前无控制权）
     */
    private volatile Instant controlAcquiredAt;

    /**
     * DRC 连接建立时间（null 表示当前无 DRC 连接）
     */
    private volatile Instant drcConnectedAt;

    /**
     * 被用户显式调用 fh_drc_disconnect 主动断开 DRC 链路的设备 SN 集合。
     *
     * <p>用于区分两种「当前无 DRC 链路」的情形：
     * <ul>
     *   <li>从未连接过 — 允许 ensureDrcLink() 静默自愈（见 device-control.md）</li>
     *   <li>用户明确断开 — 不得静默重连，必须由用户显式调用 fh_drc_connect 恢复</li>
     * </ul>
     * 调用 {@link #setActiveDrcDevice(String)} 成功建链时会从集合中移除对应 SN。
     */
    private final Set<String> explicitlyDisconnectedDrcDevices = ConcurrentHashMap.newKeySet();

    /**
     * 活跃的订阅 ID 集合
     */
    private final Set<Integer> activeListenIds = ConcurrentHashMap.newKeySet();

    /**
     * 带取消动作的订阅注册表。
     * key=订阅 key, value=注册信息（标签 + 取消动作）。
     */
    private final ConcurrentHashMap<String, ListenRegistration> listenRegistry =
            new ConcurrentHashMap<>();

    /**
     * 最后活跃时间（用于超时检测）
     */
    private volatile Instant lastActivityAt;

    /**
     * 是否处于 grace period（断连保护中，等待客户端重连）
     */
    private volatile boolean inGracePeriod = false;

    /**
     * 进入 grace period 的时间
     */
    private volatile Instant gracePeriodStartedAt;

    public SessionContext(String sessionId, String workspaceUuid, String workspaceName, int maxSecurityLevel) {
        this(sessionId, workspaceUuid, workspaceName, maxSecurityLevel, null);
    }

    public SessionContext(String sessionId, String workspaceUuid, String workspaceName,
                          int maxSecurityLevel, String userId) {
        this.sessionId = sessionId;
        this.workspaceUuid = workspaceUuid;
        this.workspaceName = workspaceName;
        this.maxSecurityLevel = maxSecurityLevel;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }

    // ==================== Activity Tracking ====================

    /**
     * 刷新最后活跃时间。
     */
    public void touch() {
        this.lastActivityAt = Instant.now();
    }

    /**
     * 判断会话是否已超时。
     *
     * @param timeoutSeconds 超时阈值（秒）
     * @return true 表示已超时
     */
    public boolean isExpired(int timeoutSeconds) {
        return Instant.now().isAfter(lastActivityAt.plusSeconds(timeoutSeconds));
    }

    // ==================== Grace Period ====================

    /**
     * 进入 grace period（断连保护期）。
     * 客户端 SSE 连接断开后调用，保留 session 状态等待重连。
     */
    public void enterGracePeriod() {
        this.inGracePeriod = true;
        this.gracePeriodStartedAt = Instant.now();
    }

    /**
     * 退出 grace period（客户端成功重连）。
     * 恢复正常活跃状态，刷新最后活跃时间。
     */
    public void exitGracePeriod() {
        this.inGracePeriod = false;
        this.gracePeriodStartedAt = null;
        touch();
    }

    /**
     * 是否处于 grace period。
     *
     * @return true 表示正在等待客户端重连
     */
    public boolean isInGracePeriod() {
        return inGracePeriod;
    }

    /**
     * 获取进入 grace period 的时间。
     *
     * @return grace period 开始时间，未进入时返回 null
     */
    public Instant getGracePeriodStartedAt() {
        return gracePeriodStartedAt;
    }

    /**
     * 判断 grace period 是否已过期。
     *
     * @param gracePeriodSeconds grace period 超时阈值（秒）
     * @return true 表示 grace period 已超时，应释放 session 资源
     */
    public boolean isGracePeriodExpired(int gracePeriodSeconds) {
        return inGracePeriod && gracePeriodStartedAt != null
                && Instant.now().isAfter(gracePeriodStartedAt.plusSeconds(gracePeriodSeconds));
    }

    // ==================== Control Authority ====================

    public boolean hasControlAuthority() {
        return activeControlDeviceSn != null;
    }

    public boolean hasControlOf(String deviceSn) {
        return deviceSn != null && deviceSn.equals(activeControlDeviceSn);
    }

    public void setActiveControlDevice(String sn) {
        this.activeControlDeviceSn = sn;
        this.controlAcquiredAt = (sn != null) ? Instant.now() : null;
    }

    public void clearControlDevice() {
        this.activeControlDeviceSn = null;
        this.controlAcquiredAt = null;
    }

    // ==================== DRC Link ====================

    public boolean hasDrcLink() {
        return activeDrcDeviceSn != null;
    }

    public boolean hasDrcLinkTo(String deviceSn) {
        return deviceSn != null && deviceSn.equals(activeDrcDeviceSn);
    }

    public void setActiveDrcDevice(String sn) {
        this.activeDrcDeviceSn = sn;
        this.drcConnectedAt = (sn != null) ? Instant.now() : null;
        if (sn != null) {
            // 建链成功即视为用户已恢复链路，解除「显式断开」标记
            this.explicitlyDisconnectedDrcDevices.remove(sn);
        }
    }

    public void clearDrcDevice() {
        this.activeDrcDeviceSn = null;
        this.drcConnectedAt = null;
    }

    /**
     * 返回指定设备是否被用户显式断开了 DRC 链路。
     *
     * @param deviceSn 设备 SN
     * @return true 表示用户主动断开且尚未重新建链
     */
    public boolean isDrcExplicitlyDisconnected(String deviceSn) {
        return deviceSn != null && explicitlyDisconnectedDrcDevices.contains(deviceSn);
    }

    /**
     * 记录用户显式断开指定设备的 DRC 链路，并清理本地链路状态。
     * 自动重连（{@code reconnectDrc}）与会话释放不走此方法，因此不会被误标记。
     *
     * <p>该方法是幂等的：即便本地已无该设备的链路（例如链路已在 SDK 侧掉线），
     * 仍然要留下「用户显式断开」标记。否则用户显式调用 {@code fh_drc_disconnect}
     * 后，后续飞行指令会被 {@code ensureDrcLink} 静默重新建链，覆盖用户的断开意图。
     * 内部清理路径（{@link #clearDrcDevice()}）不走此方法，因此不会误标记。
     *
     * @param deviceSn 设备 SN
     */
    public void markDrcExplicitlyDisconnected(String deviceSn) {
        if (deviceSn == null) {
            return;
        }
        if (hasDrcLinkTo(deviceSn)) {
            this.activeDrcDeviceSn = null;
            this.drcConnectedAt = null;
        }
        this.explicitlyDisconnectedDrcDevices.add(deviceSn);
    }

    // ==================== Payload Control Authority ====================

    public boolean hasPayloadAuthority() {
        return activePayloadDeviceSn != null;
    }

    public boolean hasPayloadAuthorityOf(String deviceSn, String payloadIndex) {
        return deviceSn != null && deviceSn.equals(activePayloadDeviceSn)
                && payloadIndex != null && payloadIndex.equals(this.activePayloadIndex);
    }

    public void setActivePayloadDevice(String sn, String payloadIndex) {
        this.activePayloadDeviceSn = sn;
        this.activePayloadIndex = payloadIndex;
    }

    public void clearPayloadDevice() {
        this.activePayloadDeviceSn = null;
        this.activePayloadIndex = null;
    }

    public String getActivePayloadDeviceSn() {
        return activePayloadDeviceSn;
    }

    public String getActivePayloadIndex() {
        return activePayloadIndex;
    }

    // ==================== Listen IDs ====================

    public void addListenId(int id) {
        activeListenIds.add(id);
    }

    public void removeListenId(int id) {
        activeListenIds.remove(id);
    }

    public Set<Integer> getActiveListenIds() {
        return Collections.unmodifiableSet(activeListenIds);
    }

    /**
     * 订阅注册信息。
     *
     * @param label        可读标签（如 "ai_data:DRONE-001"）
     * @param cancelAction 取消动作（SDK cancel 方法引用）
     */
    public record ListenRegistration(String label, Runnable cancelAction) {
    }

    /**
     * 注册一个带取消动作的订阅。
     * key 由调用方保证在 session 内唯一（SDK 的 listenId 不保证跨模块唯一，不能直接作 key）。
     * 同一 key 已存在时直接覆盖，调用方需先 cancelListen 旧订阅。
     *
     * @param key          订阅 key（如 "DRONE-001::ai_data"）
     * @param label        可读标签
     * @param cancelAction 取消时执行的动作
     */
    public void registerListen(String key, String label, Runnable cancelAction) {
        listenRegistry.put(key, new ListenRegistration(label, cancelAction));
    }

    /**
     * 取消并移除一个订阅：先从注册表移除，再执行其取消动作。
     *
     * @param key 订阅 key
     * @return 该 key 已注册并已执行取消动作返回 true；未注册返回 false
     */
    public boolean cancelListen(String key) {
        ListenRegistration registration = listenRegistry.remove(key);
        if (registration == null) {
            return false;
        }
        registration.cancelAction().run();
        return true;
    }

    /**
     * 获取所有已注册订阅（不可变视图）。
     *
     * @return 订阅 key → 注册信息的映射
     */
    public Map<String, ListenRegistration> getListenRegistry() {
        return Collections.unmodifiableMap(listenRegistry);
    }

    /**
     * 当前活跃订阅总数（旧版 listenId 集合 + 带取消动作的注册表）。
     *
     * @return 订阅数量
     */
    public int getActiveListenCount() {
        return activeListenIds.size() + listenRegistry.size();
    }

    // ==================== Getters ====================

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getWorkspaceUuid() {
        return workspaceUuid;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getMaxSecurityLevel() {
        return maxSecurityLevel;
    }

    public String getActiveControlDeviceSn() {
        return activeControlDeviceSn;
    }

    public String getActiveDrcDeviceSn() {
        return activeDrcDeviceSn;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    // ==================== Flight State ====================

    public FlightPhase getFlightPhase() {
        return flightPhase;
    }

    public void setFlightPhase(FlightPhase phase) {
        this.flightPhase = phase;
    }

    public String getActiveFlightId() {
        return activeFlightId;
    }

    public void setActiveFlightId(String id) {
        this.activeFlightId = id;
    }

    public String getActiveFlyToId() {
        return activeFlyToId;
    }

    public void setActiveFlyToId(String id) {
        this.activeFlyToId = id;
    }

    public String getActiveWaylineTaskUuid() {
        return activeWaylineTaskUuid;
    }

    public void setActiveWaylineTaskUuid(String uuid) {
        this.activeWaylineTaskUuid = uuid;
        touch();
    }

    public Instant getControlAcquiredAt() {
        return controlAcquiredAt;
    }

    public void setControlAcquiredAt(Instant t) {
        this.controlAcquiredAt = t;
    }

    public Instant getDrcConnectedAt() {
        return drcConnectedAt;
    }

    public void setDrcConnectedAt(Instant t) {
        this.drcConnectedAt = t;
    }
}
