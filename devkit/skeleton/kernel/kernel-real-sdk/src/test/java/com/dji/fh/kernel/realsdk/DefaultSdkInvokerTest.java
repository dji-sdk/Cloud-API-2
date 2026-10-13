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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.dji.flighthubsdk.FlightHubCallback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * DefaultSdkInvoker 契约级测试。
 *
 * <p><strong>默认 CI 跳过</strong>：本测试调用 {@code FlightHubSDK} 静态方法，会触发 SDK JNI 模块
 * 的类加载（flighthubsdkmoduleJNI），需要 native lib 可达。CI 环境通常不满足，靠
 * {@code FH_SKELETON_REAL_SDK_E2E=true} env var 启用。
 *
 * <p>本测试仅验：
 * <ol>
 *   <li>未 initialize 时 listen* 抛 IllegalStateException + 合理错误信息；</li>
 *   <li>cancel* 在实例为 null 时不抛异常（容错路径）。</li>
 * </ol>
 *
 * <p>真 SDK initialize 后的 listen/cancel 行为见 {@code RealSdkE2EIntegrationTest}。
 * 业务组件（Adapter / Aggregator / Starter）的单测通过 mock {@link SdkInvoker} interface 完成，
 * 不依赖本测试 — 详见各 *Test.java。
 *
 * @author DJI DevKit Team
 */
@EnabledIfEnvironmentVariable(named = "FH_SKELETON_REAL_SDK_E2E", matches = "true")
class DefaultSdkInvokerTest {

    private final DefaultSdkInvoker invoker = new DefaultSdkInvoker();

    @Test
    void listenWorkspaceDeviceChanged_shouldThrowIllegalState_whenSdkNotInitialized() {
        FlightHubCallback.WorkspaceDeviceChangeCallback cb =
                mock(FlightHubCallback.WorkspaceDeviceChangeCallback.class);

        assertThatThrownBy(() -> invoker.listenWorkspaceDeviceChanged(cb))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Workspace 实例不可用");
    }

    @Test
    void getDeviceList_shouldThrowIllegalState_whenSdkNotInitialized() {
        FlightHubCallback.DeviceListCallback cb = mock(FlightHubCallback.DeviceListCallback.class);

        assertThatThrownBy(() -> invoker.getDeviceList(cb))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Workspace 实例不可用");
    }

    @Test
    void listenAircraftLocation_shouldThrowIllegalState_whenDroneNotFound() {
        FlightHubCallback.AircraftLocationCallback cb =
                mock(FlightHubCallback.AircraftLocationCallback.class);

        assertThatThrownBy(() -> invoker.listenAircraftLocation("NONEXIST-SN", cb))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Drone 实例为 null")
                .hasMessageContaining("NONEXIST-SN");
    }

    @Test
    void listenAircraftHMS_shouldThrowIllegalState_whenDroneNotFound() {
        FlightHubCallback.AircraftHmsCallback cb =
                mock(FlightHubCallback.AircraftHmsCallback.class);

        assertThatThrownBy(() -> invoker.listenAircraftHMS("NONEXIST-SN", cb))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Drone 实例为 null");
    }

    @Test
    void cancelListenAircraftLocation_shouldNotThrow_whenDroneInstanceNull() {
        // cancel 走静默 null guard：实例 null 时不抛，best-effort 清理
        invoker.cancelListenAircraftLocation("NONEXIST-SN", 42);
        // 无断言 —— 仅验证不抛异常
    }

    @Test
    void cancelListenWorkspaceDeviceChanged_shouldNotThrow_whenWorkspaceNull() {
        invoker.cancelListenWorkspaceDeviceChanged(42);
        // 无断言 —— 仅验证不抛异常
    }
}
