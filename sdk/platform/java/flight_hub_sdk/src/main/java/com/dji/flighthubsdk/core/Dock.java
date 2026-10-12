// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.IDock;
import com.dji.flighthubsdk.generated.IRtkCalibrationModule;
import com.dji.flighthubsdk.generated.IDeviceStateModule;
import com.dji.flighthubsdk.generated.IDeviceHMSModule;
import com.dji.flighthubsdk.generated.ILiveDeviceModule;
import com.dji.flighthubsdk.generated.INetworkTestModule;
import com.dji.flighthubsdk.generated.IDeviceDebugModule;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.util.Logger;

/**
 * 机场模块接口，提供直播、录制、设备状态监听、设备控制、进离场航线等功能。
 * <p>通过 {@code FlightHubSDK.getDockInstance()} 获取实例。</p>
 */
public class Dock {

    private IDock dock;
    private String sn; // 设备序列号
    private RtkCalibrationModule rtkCalibrationModule;
    private NetworkTestModule networkTestModule;
    private DeviceDebugModule deviceDebugModule;
    private DeviceStateModule deviceStateModule;
    private DeviceHMSModule deviceHmsModule;
    private LiveDeviceModule liveDeviceModule;
    private DeployModule deployModule;
    private DockOsdModule dockOsdModule;
    private DockControlModule dockControlModule;

    // Live Module Container
    private final java.util.concurrent.ConcurrentHashMap<String, LiveModule> liveModuleRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, LiveRecordModule> liveRecordModuleRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 构造机场实例
     * @param dock 底层机场接口
     * @param sn 设备序列号
     */
    public Dock(IDock dock, String sn) {
        this.dock = dock;
        this.sn = sn;
        Logger.info("Dock", "[" + sn + "] Dock instance created");
    }

    /**
     * 获取直播模块（按 LiveInfo 区分不同摄像头/视频流）
     * @param params 直播信息（SN、摄像头、视频流标识）
     * @return 直播模块实例
     */
    public synchronized LiveModule getLiveStream(LiveInfo params) {
        if (params == null) {
            Logger.warn("Dock", "getLiveStream: params is null");
            return null;
        }
        com.dji.flighthubsdk.generated.LiveInfo nativeInfo = null;
        try {
            nativeInfo = FlightHubSdkConvert.toNativeLiveInfo(params);
            if (liveModuleRef.containsKey(params.getPayloadIndex())) {
                return liveModuleRef.get(params.getPayloadIndex());
            }
            LiveModule liveModule = new LiveModule(this.dock.getLiveStream(nativeInfo), this.sn);
            if (liveModule != null) {
                Logger.info("Dock", "[" + sn + "] GetLiveStream: live module created, payload_index: " + params.getPayloadIndex());
                liveModuleRef.put(params.getPayloadIndex(), liveModule);
                return liveModule;
            }
            Logger.warn("Dock", "GetLiveStream error: live module not found");
            return null;
        } finally {
            if (nativeInfo != null) {
                nativeInfo.delete();
            }
        }
    }

    /**
     * 获取录制模块（按 LiveInfo 区分不同摄像头/视频流）
     * @param params 直播信息
     * @return 录制模块实例
     */
    public synchronized LiveRecordModule getLiveRecord(LiveInfo params) {
        if (params == null) {
            Logger.warn("Dock", "getLiveRecord: params is null");
            return null;
        }
        com.dji.flighthubsdk.generated.LiveInfo nativeInfo = null;
        try {
            nativeInfo = FlightHubSdkConvert.toNativeLiveInfo(params);
            if (liveRecordModuleRef.containsKey(params.getPayloadIndex())) {
                return liveRecordModuleRef.get(params.getPayloadIndex());
            }
            com.dji.flighthubsdk.generated.ILiveRecordModule raw = this.dock.getLiveRecord(nativeInfo);
            if (raw == null) {
                Logger.warn("Dock", "getLiveRecord: module is null, payload_index: " + params.getPayloadIndex());
                return null;
            }
            LiveRecordModule liveRecordModule = new LiveRecordModule(raw, this.sn);
            liveRecordModuleRef.put(params.getPayloadIndex(), liveRecordModule);
            Logger.info("Dock", "[" + sn + "] getLiveRecord: live record module created, payload_index: " + params.getPayloadIndex());
            return liveRecordModule;
        } finally {
            if (nativeInfo != null) {
                nativeInfo.delete();
            }
        }
    }

    /**
     * 获取设备级直播发现模块（可用直播源订阅）
     */
    public synchronized LiveDeviceModule getLiveDeviceModule() {
        if (liveDeviceModule == null) {
            ILiveDeviceModule module = dock.getLiveDeviceModule();
            if (module != null) {
                liveDeviceModule = new LiveDeviceModule(module, "Dock[" + sn + "]");
            }
        }
        return liveDeviceModule;
    }

    /**
     * 获取部署/踏勘模块
     */
    public synchronized DeployModule getDeployModule() {
        if (deployModule == null) {
            com.dji.flighthubsdk.generated.IDeployModule module = dock.getDeployModule();
            if (module != null) {
                deployModule = new DeployModule(module);
            }
        }
        return deployModule;
    }

    /**
     * 获取机场控制模块（环控：空调/补光灯/充电/舱盖等）
     */
    public synchronized DockControlModule getDockControlModule() {
        if (dockControlModule == null) {
            com.dji.flighthubsdk.generated.IDockControlModule module = dock.getDockControlModule();
            if (module != null) {
                dockControlModule = new DockControlModule(module);
            }
        }
        return dockControlModule;
    }
    /**
     * 获取机场 OSD 数据订阅模块
     */
    public synchronized DockOsdModule getDockOsdModule() {
        if (dockOsdModule != null) {
            return dockOsdModule;
        }
        try {
            dockOsdModule = new DockOsdModule(this.dock.getDockOsdModule(), sn);
            Logger.info("Dock", "[" + sn + "] GetDockOsdModule: DockOsdModule created");
            return dockOsdModule;
        } catch (Exception e) {
            Logger.error("Dock", "[" + sn + "] GetDockOsdModule error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取 RTK 标定模块
     */
    public synchronized RtkCalibrationModule getRtkCalibrationModule() {
        if (rtkCalibrationModule == null) {
            IRtkCalibrationModule module = dock.getRtkCalibrationModule();
            if (module != null) {
                rtkCalibrationModule = new RtkCalibrationModule(module);
            }
        }
        return rtkCalibrationModule;
    }

    /**
     * 获取网络测速模块
     */
    public synchronized NetworkTestModule getNetworkTestModule() {
        if (networkTestModule == null) {
            INetworkTestModule module = dock.getNetworkTestModule();
            if (module != null) {
                networkTestModule = new NetworkTestModule(module);
            }
        }
        return networkTestModule;
    }

    /**
     * 获取设备调试模块
     */
    public synchronized DeviceDebugModule getDeviceDebugModule() {
        if (deviceDebugModule == null) {
            IDeviceDebugModule module = dock.getDeviceDebugModule();
            if (module != null) {
                deviceDebugModule = new DeviceDebugModule(module);
            }
        }
        return deviceDebugModule;
    }

    /**
     * 获取设备状态模块（拓扑变更、在线状态）
     */
    public synchronized DeviceStateModule getDeviceStateModule() {
        if (deviceStateModule == null) {
            IDeviceStateModule module = dock.getDeviceStateModule();
            if (module != null) {
                deviceStateModule = new DeviceStateModule(module, "Dock[" + sn + "]");
            }
        }
        return deviceStateModule;
    }

    /**
     * 获取设备 HMS 实时推送模块
     */
    public synchronized DeviceHMSModule getDeviceHMSModule() {
        if (deviceHmsModule == null) {
            IDeviceHMSModule module = dock.getDeviceHMSModule();
            if (module != null) {
                deviceHmsModule = new DeviceHMSModule(module, "Dock[" + sn + "]");
            }
        }
        return deviceHmsModule;
    }
}
