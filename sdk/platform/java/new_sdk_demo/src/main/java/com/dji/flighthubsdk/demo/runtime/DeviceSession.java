// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.Workspace;

/**
 * 一次 demo 运行的设备上下文：选定的设备拓扑 + drone/dock/workspace 句柄 + 订阅登记处。
 *
 * <p>由 {@link #of} 工厂从已选 topo 解析 drone/机场(dock)/workspace 句柄;
 * {@link #close()} 统一释放所有注册到 {@link #registry()} 的订阅。
 *
 * <p>设备句柄不是散落的 static 字段, 而是随会话对象生命周期管理;
 * 所有监听都应注册进本会话的 registry, 会话关闭即全部退订。
 */
public final class DeviceSession implements AutoCloseable {

    private static final String TAG = "DeviceSession";

    /** 所属 workspace 句柄(drone/dock 实例、订阅均在其下)。 */
    private final Workspace workspace;
    /** 本次会话选定的设备拓扑(drone/dock 序列号由此解析而来)。 */
    private final DeviceTopo topo;
    /** 选定 drone 的序列号; 拓扑中无 drone 时为 null/空(见 {@link #hasDrone()})。 */
    private final String droneSn;
    /** 选定 dock 的序列号; 拓扑中无 dock 时为 null/空(见 {@link #hasDock()})。 */
    private final String dockSn;
    /** 按所选 drone 的 modelKey 从 device_common_configs 匹配到的负载索引(负载操控用)。 */
    private final String targetPayloadIndex;
    /** 本会话订阅登记处：所有 listen 注册于此, {@link #close()} 时统一退订。 */
    private final SubscriptionRegistry registry = new SubscriptionRegistry();

    private Drone drone;   // 惰性(仅在 synchronized 的 drone() 内读写)
    private Dock dock;     // 惰性(仅在 synchronized 的 dock() 内读写)

    /** 私有构造：仅由工厂方法 {@link #of} 在解析好各句柄后调用。 */
    private DeviceSession(Workspace workspace, DeviceTopo topo, String droneSn, String dockSn, String targetPayloadIndex) {
        this.workspace = workspace;
        this.topo = topo;
        this.droneSn = droneSn;
        this.dockSn = dockSn;
        this.targetPayloadIndex = targetPayloadIndex;
    }

    /**
     * 由已选定的拓扑直接构造会话(不再拉设备列表)。
     *
     * <p>由入口统一调用(交互菜单 / 非交互单跑)：设备已在启动时选好, 每次运行 demo 用同一 topo 新建一个会话
     * (含独立 registry), 跑完 {@link #close()} 只退订本次订阅、不影响 SDK 生命周期。
     *
     * <p>构造时按所选 drone 的 modelKey 从 {@code device_common_configs} 匹配 target_payload_index,
     * 供后续负载操控使用(查不到回退 {@link DemoParams#DEFAULT_PAYLOAD_INDEX})。
     */
    public static DeviceSession of(Workspace ws, DeviceTopo topo, DemoParams params) {
        // 直接读已选 topo 的字段：飞机=子设备、机场=主设备(见 Devices.drone/dock), 无需按 SN 反查。
        Device drone = Devices.drone(topo);
        Device dock = Devices.dock(topo);

        String droneSn = drone != null ? drone.getDeviceSn() : null;
        String dockSn = dock != null ? dock.getDeviceSn() : null;
        String droneModelKey = drone != null ? drone.getModelKey() : null;
        String dronePayloadIndex = params.payloadIndexForModel(droneModelKey);

        DemoLog.info(
            TAG,
            "选定设备: drone=" + droneSn + " dock=" + dockSn
            + " droneModelKey=" + droneModelKey + " targetPayloadIndex=" + dronePayloadIndex
        );

        return new DeviceSession(ws, topo, droneSn, dockSn, dronePayloadIndex);
    }

    public Workspace workspace() {
        return workspace;
    }

    public DeviceTopo topo() {
        return topo;
    }

    public String droneSn() {
        return droneSn;
    }

    public String dockSn() {
        return dockSn;
    }

    /** 按所选 drone 的 modelKey 匹配到的负载索引(负载操控用)。 */
    public String targetPayloadIndex() {
        return targetPayloadIndex;
    }

    public boolean hasDrone() {
        return droneSn != null && !droneSn.isEmpty();
    }

    public boolean hasDock() {
        return dockSn != null && !dockSn.isEmpty();
    }

    /**
     * 拓扑快照中 drone 是否在线。<b>快照语义</b>：这是「选设备时刻」的状态, 非实时——
     * 设备状态其后变化不会反映到这里;若设备刚上线, 需回主菜单用 r 重选设备刷新拓扑。
     * 用途：离线设备不可能有直播源/可控链路, 调用方可据此直接跳过, 免等超时。
     */
    public boolean isDroneOnline() {
        Device d = Devices.drone(topo);
        return d != null && d.isOnline();
    }

    /** 拓扑快照中 dock 是否在线(选设备时刻的状态, 非实时;语义同 {@link #isDroneOnline()})。 */
    public boolean isDockOnline() {
        Device d = Devices.dock(topo);
        return d != null && d.isOnline();
    }

    /**
     * 惰性获取 Drone 实例。
     *
     * <p>synchronized：首次初始化可能被主线程与 SDK 回调线程并发触发, 无同步时两个线程都会看到
     * null 并各自解析(后写覆盖先写), 且写入对其它线程无可见性保证; 加锁保证只解析一次且结果可见
     * (字段仅在本方法内读写)。
     */
    public synchronized Drone drone() {
        if (!hasDrone()) {
            throw new FlightHubException("当前设备不含 drone");
        }
        if (drone == null) {
            drone = FlightHubSDK.getDroneInstance(droneSn);
            if (drone == null) {
                throw new FlightHubException("获取 Drone 实例失败: " + droneSn
                    + "(请确认已先获取 Workspace 且设备在线)");
            }
        }

        return drone;
    }

    /** 惰性获取 Dock 实例(线程安全语义同 {@link #drone()})。 */
    public synchronized Dock dock() {
        if (!hasDock()) {
            throw new FlightHubException("当前设备不含 dock");
        }
        if (dock == null) {
            dock = FlightHubSDK.getDockInstance(dockSn);
            if (dock == null) {
                throw new FlightHubException("获取 Dock 实例失败: " + dockSn);
            }
        }

        return dock;
    }

    /** 本会话的订阅登记处：所有 listen 都注册于此, 会话关闭时统一退订。 */
    public SubscriptionRegistry registry() {
        return registry;
    }

    @Override
    public void close() {
        registry.close();
    }
}
