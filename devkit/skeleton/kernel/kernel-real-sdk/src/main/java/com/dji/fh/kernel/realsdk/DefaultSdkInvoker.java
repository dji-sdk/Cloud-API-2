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

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;

import java.util.List;

/**
 * {@link SdkInvoker} 默认实现：直接调 {@code FlightHubSDK} 静态方法 + Workspace / Drone 实例方法。
 *
 * <p>不持有任何实例状态（SDK 全局状态都在 {@code FlightHubSDK} 静态字段）。
 *
 * <p>{@code listenXxx} 系列：内部走 {@link #requireWorkspace()} / {@link #requireDrone(String)}
 * 实例非空校验，null 时抛 {@link IllegalStateException}，由 {@link RealSdkStarter} 捕获后跳过
 * 该设备 / workspace 的 listen 注册。
 *
 * <p>{@code cancelXxx} 系列：走静默 null guard 容错（实例 null 时直接 return，不抛），
 * 适配关闭路径上的 best-effort 清理。
 *
 * @author DJI DevKit Team
 */
public class DefaultSdkInvoker implements SdkInvoker {

    @Override
    public void loadJniLibrary() {
        JniLibraryLoader.loadLibrary();
    }

    @Override
    public FlightHubSdkErrorCode initialize(FlightHubSdkConfig config) {
        return FlightHubSDK.initialize(config);
    }

    @Override
    public FlightHubSdkErrorCode shutdown() {
        return FlightHubSDK.shutdown();
    }

    @Override
    public boolean isInitialized() {
        return FlightHubSDK.isInitialized();
    }

    @Override
    public String getVersion() {
        return FlightHubSDK.getVersion();
    }

    @Override
    public void setWebSocketAuthHeaders(String token) {
        FlightHubSDK.setWebSocketAuthHeaders(
                List.of(new HeaderKeyValue("Authorization", "Bearer " + token)));
    }

    @Override
    public void setWebSocketCustomAuthHeaders(String headerKey, String headerValue) {
        FlightHubSDK.setWebSocketAuthHeaders(List.of(
                new HeaderKeyValue("auth_keys", headerKey),
                new HeaderKeyValue(headerKey, headerValue)));
    }

    @Override
    public void setHttpHeaders(String headerKey, String headerValue) {
        FlightHubSDK.setHttpHeaders(List.of(
                new HeaderKeyValue("auth_keys", headerKey),
                new HeaderKeyValue(headerKey, headerValue)));
    }

    @Override
    public int listenWorkspaceDeviceChanged(FlightHubCallback.WorkspaceDeviceChangeCallback callback) {
        return requireWorkspace().getDeviceManageModule().listenWorkspaceDeviceChanged(callback);
    }

    @Override
    public void cancelListenWorkspaceDeviceChanged(int listenId) {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws != null) {
            ws.getDeviceManageModule().cancelListenWorkspaceDeviceChanged(listenId);
        }
    }

    @Override
    public void getDeviceList(FlightHubCallback.DeviceListCallback callback) {
        requireWorkspace().getDeviceManageModule().getDeviceList(callback);
    }

    @Override
    public int listenDroneOnlineStatus(String sn, FlightHubCallback.DroneOnlineStatusCallback callback) {
        return requireDrone(sn).getDeviceStateModule().listenDroneOnlineStatus(callback);
    }

    @Override
    public void cancelListenDroneOnlineStatus(String sn, int listenId) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone != null) {
            drone.getDeviceStateModule().cancelListenDroneOnlineStatus(listenId);
        }
    }

    @Override
    public int listenAircraftHMS(String sn, FlightHubCallback.AircraftHmsCallback callback) {
        return requireDrone(sn).getDeviceHMSModule().listenDeviceHMS(callback);
    }

    @Override
    public void cancelListenAircraftHMS(String sn, int listenId) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone != null) {
            drone.getDeviceHMSModule().cancelListenDeviceHMS(listenId);
        }
    }

    @Override
    public int listenAircraftLocation(String sn, FlightHubCallback.AircraftLocationCallback callback) {
        return requireDrone(sn).getDroneOsdModule().listenAircraftLocation(callback);
    }

    @Override
    public void cancelListenAircraftLocation(String sn, int listenId) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone != null) {
            drone.getDroneOsdModule().cancelListenAircraftLocation(listenId);
        }
    }

    @Override
    public int listenAircraftBatteryInfo(String sn, FlightHubCallback.AircraftBatteryInfoCallback callback) {
        return requireDrone(sn).getDroneOsdModule().listenAircraftBatteryInfo(callback);
    }

    @Override
    public void cancelListenAircraftBatteryInfo(String sn, int listenId) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone != null) {
            drone.getDroneOsdModule().cancelListenAircraftBatteryInfo(listenId);
        }
    }

    @Override
    public int listenAircraftAttitude(String sn, FlightHubCallback.AircraftAttitudeCallback callback) {
        return requireDrone(sn).getDroneOsdModule().listenAircraftAttitude(callback);
    }

    @Override
    public void cancelListenAircraftAttitude(String sn, int listenId) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone != null) {
            drone.getDroneOsdModule().cancelListenAircraftAttitude(listenId);
        }
    }

    @Override
    public int listenDockLocation(String sn, FlightHubCallback.DockLocationCallback callback) {
        return requireDock(sn).getDockOsdModule().listenDockLocation(callback);
    }

    @Override
    public void cancelListenDockLocation(String sn, int listenId) {
        Dock dock = FlightHubSDK.getDockInstance(sn);
        if (dock != null) {
            dock.getDockOsdModule().cancelListenDockLocation(listenId);
        }
    }

    @Override
    public int listenDockOnlineStatus(String sn, FlightHubCallback.DockOnlineStatusCallback callback) {
        return requireDock(sn).getDeviceStateModule().listenDockOnlineStatus(callback);
    }

    @Override
    public void cancelListenDockOnlineStatus(String sn, int listenId) {
        Dock dock = FlightHubSDK.getDockInstance(sn);
        if (dock != null) {
            dock.getDeviceStateModule().cancelListenDockOnlineStatus(listenId);
        }
    }

    /**
     * 获取 workspace 实例，null 时抛 {@link IllegalStateException}。
     */
    private Workspace requireWorkspace() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new IllegalStateException("Workspace 实例不可用，SDK 未 initialize");
        }
        return ws;
    }

    /**
     * 获取 drone 实例，null 时抛 {@link IllegalStateException}。
     */
    private Drone requireDrone(String sn) {
        Drone drone = FlightHubSDK.getDroneInstance(sn);
        if (drone == null) {
            throw new IllegalStateException("Drone 实例为 null：sn=" + sn);
        }
        return drone;
    }

    /**
     * 获取 dock 实例，null 时抛 {@link IllegalStateException}。
     */
    private Dock requireDock(String sn) {
        Dock dock = FlightHubSDK.getDockInstance(sn);
        if (dock == null) {
            throw new IllegalStateException("Dock 实例为 null：sn=" + sn);
        }
        return dock;
    }
}
