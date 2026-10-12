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
     * 活跃的订阅 ID 集合
     */
    private final Set<Integer> activeListenIds = ConcurrentHashMap.newKeySet();

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
    }

    public void clearDrcDevice() {
        this.activeDrcDeviceSn = null;
        this.drcConnectedAt = null;
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
