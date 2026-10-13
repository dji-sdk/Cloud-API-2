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

package com.dji.flighthub.mcp.tools.safety;

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthubsdk.core.Workspace;

import java.lang.reflect.Field;

/**
 * 安全服务工具测试公共辅助类。
 * 提供 SdkService mock 构造方法，供所有 FlightArea*Test 共用。
 *
 * <p>实现说明：Workspace 类含 JNI native 方法，无法通过正常构造函数实例化。
 * 这里使用 sun.misc.Unsafe.allocateInstance() 分配一个未初始化的实例作为占位，
 * 仅用于使 {@code sdkService.getWorkspace() != null} 校验通过。
 *
 * @author DJI FlightHub SDK Team
 */
final class SafetyTestHelper {

    private SafetyTestHelper() {
        // 工具类禁止实例化
    }

    /**
     * 创建带 workspace 的 mock SdkService（isMockMode=true，workspace 非 null）。
     *
     * @return 可用于安全服务工具测试的 SdkService 实例
     */
    static SdkService createSdkServiceWithWorkspace() {
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        try {
            Field workspaceField = SdkService.class.getDeclaredField("workspace");
            workspaceField.setAccessible(true);
            sun.misc.Unsafe unsafe = getUnsafe();
            Workspace mockWorkspace = (Workspace) unsafe.allocateInstance(Workspace.class);
            workspaceField.set(sdkService, mockWorkspace);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set workspace field for test", e);
        }
        return sdkService;
    }

    /**
     * 创建非 Mock 模式且 workspace 非 null 的 SdkService，用于验证 SDK API 移除路径。
     *
     * @return isMockMode=false、workspace 非 null 的 SdkService 实例
     */
    static SdkService createNonMockSdkServiceWithWorkspace() {
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }
        };
        try {
            Field workspaceField = SdkService.class.getDeclaredField("workspace");
            workspaceField.setAccessible(true);
            sun.misc.Unsafe unsafe = getUnsafe();
            Workspace mockWorkspace = (Workspace) unsafe.allocateInstance(Workspace.class);
            workspaceField.set(sdkService, mockWorkspace);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set workspace field for test", e);
        }
        return sdkService;
    }

    /**
     * 获取 sun.misc.Unsafe 实例。
     *
     * @return Unsafe 实例
     * @throws Exception 反射访问失败时抛出
     */
    private static sun.misc.Unsafe getUnsafe() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return (sun.misc.Unsafe) unsafeField.get(null);
    }
}
