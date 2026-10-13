// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.IDeviceStateModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 设备状态模块：在线状态订阅。
 */
public class DeviceStateModule {

    private final IDeviceStateModule deviceStateModule;
    private final String logTag;

    public DeviceStateModule(IDeviceStateModule deviceStateModule, String logTag) {
        this.deviceStateModule = deviceStateModule;
        this.logTag = logTag != null ? logTag : "DeviceStateModule";
    }

    /**
     * 订阅机场在线状态（Dock owner）
     * @implNote 链路: WebSocket | topic: topic_device_online_status
     */
    public int listenDockOnlineStatus(FlightHubCallback.DockOnlineStatusCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockOnlineStatusCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DockOnlineStatusCallback() {
                        @Override
                        public void onDockOnlineStatus(com.dji.flighthubsdk.generated.DockOnlineStatus status) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDockOnlineStatus(status));
                            }
                        }
                    };

            int listenId = deviceStateModule.listenDockOnlineStatusWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(logTag, "listenDockOnlineStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.DockOnlineStatus(false));
            }
        }
        return -1;
    }

    /**
     * 取消订阅机场在线状态
     * @param listenId 监听 ID
     */
    public void cancelListenDockOnlineStatus(int listenId) {
        try {
            deviceStateModule.cancelListenDockOnlineStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error(logTag, "cancelListenDockOnlineStatus error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机在线状态（Drone owner）
     * @implNote 链路: WebSocket | topic: topic_device_online_status
     */
    public int listenDroneOnlineStatus(FlightHubCallback.DroneOnlineStatusCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneOnlineStatusCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneOnlineStatusCallback() {
                        @Override
                        public void onDroneOnlineStatus(com.dji.flighthubsdk.generated.DroneOnlineStatus drone_online_status) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toDroneOnlineStatus(drone_online_status));
                                }
                            } catch (Exception e) {
                                Logger.error(logTag, "Drone online status callback error: " + e.getMessage(), e);
                            }
                        }
                    };

            int listenId = deviceStateModule.listenDroneOnlineStatusWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(logTag, "listenDroneOnlineStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.DroneOnlineStatus(false));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机在线状态
     * @param listenId 监听 ID
     */
    public void cancelListenDroneOnlineStatus(int listenId) {
        try {
            deviceStateModule.cancelListenDroneOnlineStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error(logTag, "cancelListenDroneOnlineStatus error: " + e.getMessage(), e);
        }
    }
}
