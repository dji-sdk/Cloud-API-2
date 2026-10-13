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
import com.dji.fh.kernel.spi.event.HmsAlertEvent;
import com.dji.fh.kernel.spi.event.HmsAlertLevel;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.flighthubsdk.bean.DeviceChange;
import com.dji.flighthubsdk.bean.DockOnlineStatus;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.HmsDataBasics;
import com.dji.flighthubsdk.bean.HmsDataItem;
import com.dji.flighthubsdk.bean.WorkspaceDeviceChange;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SDK 原生 callback → KernelEvent 的纯转换器。
 *
 * <p>单一职责：仅做 callback data 到 KernelEvent 的字段映射 + publish。
 * 不负责 listen 注册（{@link RealSdkStarter} 做）、不负责 listen 生命周期跟踪、
 * 不持有 SDK 句柄。
 *
 * <p>SDK callback 约定（javap 实查后确认）：
 * <ul>
 *   <li>listen 类 callback <strong>单参数</strong>（无 FlightHubError）：订阅成功 / 失败
 *       信息在 {@code listenXxx()} 返回的 int listen_id 上（&lt; 0 表示订阅失败），
 *       订阅成功后 callback 只推数据；</li>
 *   <li>action 类 callback 才带 {@code (data, FlightHubError)}（A3b 唯一遇到的 action
 *       callback 是 {@code DeviceListCallback}，在 {@link RealSdkStarter} 内处理）。</li>
 * </ul>
 *
 * <p>OSD 相关的 3 个 listen（location / battery / attitude）的 callback 不在本类，
 * 而是由 {@link RealSdkStarter#registerListensForDevice} 直接接到 {@link OsdAggregator}。
 *
 * @author DJI DevKit Team
 */
public class RealSdkAdapter {

    /** 事件 source 字段值：标识来自真 SDK */
    public static final String SOURCE = "real-sdk";

    /** WorkspaceDeviceChange.changeType 上线值（具体值需 SDK doc 或运行时日志验证） */
    private static final String CHANGE_TYPE_ONLINE = "ONLINE";

    /** WorkspaceDeviceChange.changeType 下线值 */
    private static final String CHANGE_TYPE_OFFLINE = "OFFLINE";

    /** 设备下线 reason：通过 ws callback 触发的下线统一标记 */
    private static final String OFFLINE_REASON_WS_CHANGE = "ws-change";

    /** 设备下线 reason：per-device 心跳回调报告离线 */
    private static final String OFFLINE_REASON_HEARTBEAT = "heartbeat-offline";

    private static final Logger LOG = LoggerFactory.getLogger(RealSdkAdapter.class);

    private final SdkEventBus bus;

    public RealSdkAdapter(SdkEventBus bus) {
        this.bus = bus;
    }

    /**
     * 工作空间设备变更 callback（单参数，无 FlightHubError）。
     *
     * <p>data 内是 {@code List<DeviceChange>}，每个 {@code DeviceChange} 内是
     * {@code List<String> sn}。model / name / deviceType 在 workspace 级 callback
     * 不可得：上线事件 model / name 传 null，type 默认 DRONE；下线事件 type 默认 DRONE，
     * reason 标 "ws-change"。ISV 需要详情可调 {@code Workspace.getDeviceDetailInfo}。
     */
    public void onWorkspaceDeviceChange(WorkspaceDeviceChange data) {
        if (data == null || data.getDeviceChanges() == null) {
            return;
        }
        for (DeviceChange change : data.getDeviceChanges()) {
            String changeType = change.getChangeType();
            List<String> sns = change.getChangeDevices();
            if (sns == null) {
                continue;
            }
            for (String sn : sns) {
                if (CHANGE_TYPE_ONLINE.equalsIgnoreCase(changeType)) {
                    bus.publish(new DeviceOnlineEvent(SOURCE, sn, DeviceType.DRONE, null, null));
                } else if (CHANGE_TYPE_OFFLINE.equalsIgnoreCase(changeType)) {
                    bus.publish(new DeviceOfflineEvent(SOURCE, sn, DeviceType.DRONE,
                            OFFLINE_REASON_WS_CHANGE));
                } else {
                    // 升 WARN：生产环境立即可见。SDK 实际推送的 changeType 字面值需运行时验证，
                    // 期望 ONLINE / OFFLINE（{@link #CHANGE_TYPE_ONLINE} / {@link #CHANGE_TYPE_OFFLINE}）。
                    // 如观察到其他值（如 add / remove / online_status_change），请反馈以更新映射。
                    LOG.warn("未识别的 workspace changeType={}（sn={}），跳过 publish。"
                            + "期望 ONLINE/OFFLINE", changeType, sn);
                }
            }
        }
    }

    /**
     * drone 在线状态 callback（单参数）。
     *
     * <p>v0.1：workspace 级 callback 是 ONLINE/OFFLINE 的权威事件源，本 callback 仅记 DEBUG
     * 用于诊断 drone-specific 在线状态切换，不重复 publish KernelEvent。
     */
    public void onDroneOnlineStatus(String sn, DeviceType deviceType, DroneOnlineStatus data) {
        boolean online = data != null && data.isOnline();
        LOG.debug("drone-online-status callback (sn={}): online={}", sn, online);
        if (online) {
            bus.publish(new DeviceOnlineEvent(SOURCE, sn, deviceType, null, null));
        } else {
            bus.publish(new DeviceOfflineEvent(SOURCE, sn, deviceType, OFFLINE_REASON_HEARTBEAT));
        }
    }

    /**
     * dock 在线状态心跳 callback（单参数）。
     *
     * <p>SDK 周期性推送机场在线状态，作为 {@link DeviceOnlineEvent} / {@link DeviceOfflineEvent}
     * 的存活信号，刷新 DeviceStatusService 的 {@code lastSeenAt}，防止存活超时误判。
     */
    public void onDockOnlineStatus(String sn, DockOnlineStatus data) {
        boolean online = data != null && data.isOnline();
        LOG.debug("dock-online-status callback (sn={}): online={}", sn, online);
        if (online) {
            bus.publish(new DeviceOnlineEvent(SOURCE, sn, DeviceType.DOCK, null, null));
        } else {
            bus.publish(new DeviceOfflineEvent(SOURCE, sn, DeviceType.DOCK, OFFLINE_REASON_HEARTBEAT));
        }
    }

    /**
     * drone HMS 告警 callback（单参数）。
     *
     * <p>data 内是 {@code List<HmsDataItem>}，每个 item 的 basics 内含 code (String) /
     * level (int，数字编码) / statusKey / jointKey 等字段。
     *
     * <p>SDK HMS 告警的人类可读 message 需要二次查询翻译表（{@code Workspace.queryHmsTrans}），
     * v0.1 不做翻译，message 字段传 null。{@link HmsAlertEvent#alertCode()} 字段已含 code，
     * ISV 可根据 code 自行查询翻译。
     *
     * <p>level int 映射：1 / NOTICE → INFO；2 / WARN → WARNING；3 / ERROR → ERROR；
     * 4 / FATAL → CRITICAL；其他 → WARNING 兜底。
     *
     * <p>一次 callback 可能 publish 多个 {@link HmsAlertEvent}。
     */
    public void onAircraftHMS(String sn, HmsData data) {
        if (data == null || data.getHmsDataItems() == null) {
            return;
        }
        for (HmsDataItem item : data.getHmsDataItems()) {
            HmsDataBasics basics = item.getBasics();
            if (basics == null) {
                continue;
            }
            bus.publish(new HmsAlertEvent(
                    SOURCE,
                    sn,
                    basics.getCode(),
                    mapHmsLevel(basics.getLevel()),
                    null));
        }
    }

    /**
     * 把 SDK HMS 数字 level 映射到 SPI {@link HmsAlertLevel} 枚举。
     *
     * <p>SDK 实际 level 值的语义（推测 1=NOTICE, 2=WARN, 3=ERROR, 4=FATAL）需 SDK 文档确认；
     * 不在此范围的值兜底 WARNING。
     *
     * <p>DEBUG 日志打印 sdkLevel 原始值，让 ISV 运行时可反推 SDK 真实编码语义。
     */
    private HmsAlertLevel mapHmsLevel(int sdkLevel) {
        HmsAlertLevel mapped = switch (sdkLevel) {
            case 0, 1 -> HmsAlertLevel.INFO;
            case 2 -> HmsAlertLevel.WARNING;
            case 3 -> HmsAlertLevel.ERROR;
            case 4 -> HmsAlertLevel.CRITICAL;
            default -> HmsAlertLevel.WARNING;
        };
        if (LOG.isDebugEnabled()) {
            LOG.debug("HMS level 映射：sdkLevel={} → {}", sdkLevel, mapped);
        }
        return mapped;
    }
}
