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
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;

/**
 * 包装 {@code FlightHubSDK} 静态调用 + {@code Workspace} / {@code Drone} 实例方法的薄抽象。
 *
 * <p>设计目的：让 {@link RealSdkAdapter} / {@link OsdAggregator} / {@link RealSdkStarter}
 * 等业务组件可通过 Mockito mock 此 interface 进行单元测试，无需 native lib + 真实网络。
 *
 * <p>仅暴露 A3b 实际用到的 11 个方法（同步 init / shutdown / 元方法 + workspace 级 listen +
 * getDeviceList + 5 个 drone 级 listen）。不复刻 SDK 完整 50+ listenXxx API。
 *
 * <p>需要其他 SDK 能力（如设备相机、wayline 管理）的 ISV 应直接调 {@code FlightHubSDK}
 * 静态方法，不必扩展本 interface。
 *
 * <p>SDK callback 约定（javap 实查后确认）：
 * <ul>
 *   <li>listen 类 callback **单参数**（无 FlightHubError）：订阅成功 / 失败信息在
 *       listenXxx() 返回的 int listen_id 上（&lt; 0 表示订阅失败），订阅成功后 callback 只推数据；</li>
 *   <li>{@code DeviceListCallback}（action 类）带 {@code (List<DeviceTopo>, FlightHubError)}。</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public interface SdkInvoker {

    // ---------- SDK 生命周期 ----------

    /**
     * 同步调 {@code FlightHubSDK.initialize(config)}。
     *
     * @param config SDK 配置
     * @return 错误码，{@link FlightHubSdkErrorCode#SUCCESS} 表示初始化成功
     */
    FlightHubSdkErrorCode initialize(FlightHubSdkConfig config);

    /** 调 {@code FlightHubSDK.shutdown()}。 */
    FlightHubSdkErrorCode shutdown();

    /** {@code FlightHubSDK.isInitialized()}。 */
    boolean isInitialized();

    /** {@code FlightHubSDK.getVersion()}。 */
    String getVersion();

    /**
     * 预加载 JNI 本机库。
     *
     * <p>SDK demo 要求在 {@code setHttpHeaders} / {@code setWebSocketAuthHeaders} 之前
     * 显式加载 JNI 库，否则这些方法会因 "JNI library not loaded" 返回
     * {@link FlightHubSdkErrorCode#ERROR_NOT_INITIALIZED}。
     *
     * <p>mock 模式无需 JNI，默认空实现。
     */
    default void loadJniLibrary() {
        // mock 场景不需要
    }

    /**
     * 设置 WebSocket 认证 headers（SDK 2.0.0 认证方式）。
     *
     * @param token 访问令牌
     */
    void setWebSocketAuthHeaders(String token);

    /**
     * 设置 WebSocket 认证 headers（auth_keys 模式，适用于 FH2 网关鉴权）。
     *
     * <p>FH2 网关要求 WebSocket 升级请求同时携带 {@code auth_keys: <headerKey>}
     * 和 {@code <headerKey>: <headerValue>} 两个 header，否则返回 401。
     *
     * @param headerKey   鉴权 header 名称（如 {@code user_id}）
     * @param headerValue 鉴权 header 值
     */
    default void setWebSocketCustomAuthHeaders(String headerKey, String headerValue) {
        // 默认空实现，mock 场景不需要
    }

    /**
     * 设置 SDK HTTP 请求鉴权 headers（平台网关鉴权）。
     *
     * @param headerKey   header 名称（如 {@code user_id}）
     * @param headerValue header 值（如 {@code your-auth-value}，占位示例需按平台实际配置）
     */
    default void setHttpHeaders(String headerKey, String headerValue) {
        // 默认空实现，mock 场景不需要
    }

    // ---------- workspace 级 listen ----------

    /** 工作空间设备进出 listen → callback。返回 {@code listenId}（&lt; 0 表示失败）。 */
    int listenWorkspaceDeviceChanged(FlightHubCallback.WorkspaceDeviceChangeCallback callback);

    /** 取消工作空间设备 listen。 */
    void cancelListenWorkspaceDeviceChanged(int listenId);

    /** 拉初始设备清单（callback 形式，调用方需用 CountDownLatch 桥接同步）。 */
    void getDeviceList(FlightHubCallback.DeviceListCallback callback);

    // ---------- drone 级 listen（按 sn）----------

    /** drone 在线状态 listen。 */
    int listenDroneOnlineStatus(String sn, FlightHubCallback.DroneOnlineStatusCallback callback);

    void cancelListenDroneOnlineStatus(String sn, int listenId);

    /** drone HMS 告警 listen。 */
    int listenAircraftHMS(String sn, FlightHubCallback.AircraftHmsCallback callback);

    void cancelListenAircraftHMS(String sn, int listenId);

    /** drone 位置 listen（OSD location 子项）。 */
    int listenAircraftLocation(String sn, FlightHubCallback.AircraftLocationCallback callback);

    void cancelListenAircraftLocation(String sn, int listenId);

    /** drone 电池 listen（OSD battery 子项）。 */
    int listenAircraftBatteryInfo(String sn, FlightHubCallback.AircraftBatteryInfoCallback callback);

    void cancelListenAircraftBatteryInfo(String sn, int listenId);

    /** drone 姿态 listen（OSD attitude 子项）。 */
    int listenAircraftAttitude(String sn, FlightHubCallback.AircraftAttitudeCallback callback);

    void cancelListenAircraftAttitude(String sn, int listenId);

    // ---------- dock 级 listen（按 sn）----------

    /**
     * 订阅机场 GPS 位置（来自 {@code DockOsdModule.listenDockLocation}）。
     *
     * <p>回调为单参数 {@link FlightHubCallback.DockLocationCallback}（无 FlightHubError），
     * 订阅成功/失败看返回的 listenId（&lt; 0 表示失败）。
     *
     * @param sn       机场 SN
     * @param callback 位置回调
     * @return listenId（&ge; 0 成功，&lt; 0 失败）
     */
    default int listenDockLocation(String sn, FlightHubCallback.DockLocationCallback callback) {
        throw new UnsupportedOperationException("listenDockLocation 未实现");
    }

    /**
     * 取消机场位置订阅。
     *
     * @param sn       机场 SN
     * @param listenId 订阅 ID
     */
    default void cancelListenDockLocation(String sn, int listenId) {
        throw new UnsupportedOperationException("cancelListenDockLocation 未实现");
    }

    /**
     * 订阅机场在线状态心跳（来自 {@code DeviceStateModule.listenDockOnlineStatus}）。
     *
     * <p>SDK 周期性推送机场在线状态，可用于刷新存活超时计时器。
     *
     * @param sn       机场 SN
     * @param callback 在线状态回调
     * @return listenId（&ge; 0 成功，&lt; 0 失败）
     */
    default int listenDockOnlineStatus(String sn, FlightHubCallback.DockOnlineStatusCallback callback) {
        throw new UnsupportedOperationException("listenDockOnlineStatus 未实现");
    }

    /**
     * 取消机场在线状态订阅。
     *
     * @param sn       机场 SN
     * @param listenId 订阅 ID
     */
    default void cancelListenDockOnlineStatus(String sn, int listenId) {
        throw new UnsupportedOperationException("cancelListenDockOnlineStatus 未实现");
    }
}
