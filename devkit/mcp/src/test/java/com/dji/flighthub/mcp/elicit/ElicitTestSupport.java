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

package com.dji.flighthub.mcp.elicit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;

/**
 * Elicitation 测试公共支撑：提供伪 ClientBridge 工厂方法和 ThreadLocal 自动清理扩展。
 * 消除各 Tool 测试类中重复粘贴的 fakeBridge() + @AfterEach ClientBridgeHolder.clear()。
 *
 * <p>用法示例：
 * <pre>{@code
 * @ExtendWith(ElicitTestSupport.AutoClear.class)
 * class FhXxxToolTest {
 *     // setUp 中按需调用：
 *     ClientBridgeHolder.set(ElicitTestSupport.fakeBridge(ElicitResult.accept(null)));
 * }
 * }</pre>
 *
 * @author DJI FlightHub SDK Team
 */
public final class ElicitTestSupport {

    private ElicitTestSupport() {
        // 工具类禁止实例化
    }

    /**
     * 构造支持 elicitation 且返回固定结果的伪 bridge（常用场景：supportsElicitation=true）。
     *
     * @param result 预设的 elicitation 结果
     * @return 伪 ClientBridge
     */
    public static ClientBridge fakeBridge(ElicitResult result) {
        return fakeBridge(true, result);
    }

    /**
     * 构造伪 bridge，可自定义是否声明支持 elicitation。
     *
     * @param supportsElicitation 是否声明支持
     * @param result              预设的 elicitation 结果
     * @return 伪 ClientBridge
     */
    public static ClientBridge fakeBridge(boolean supportsElicitation, ElicitResult result) {
        return fakeBridge(supportsElicitation, false, result);
    }

    /**
     * 构造伪 bridge，可自定义是否声明支持 elicitation 和是否为远程传输。
     *
     * @param supportsElicitation 是否声明支持
     * @param remote              是否为远程传输（SSE）
     * @param result              预设的 elicitation 结果
     * @return 伪 ClientBridge
     */
    public static ClientBridge fakeBridge(boolean supportsElicitation, boolean remote, ElicitResult result) {
        return new ClientBridge() {
            @Override
            public boolean supportsElicitation() {
                return supportsElicitation;
            }

            @Override
            public boolean isRemote() {
                return remote;
            }

            @Override
            public ElicitResult elicit(String message, Map<String, Object> requestedSchema,
                                       long timeoutMillis) {
                return result;
            }
        };
    }

    /**
     * JUnit 5 扩展：每个测试方法执行后自动清理 ClientBridgeHolder ThreadLocal，
     * 防止跨测试泄漏。
     *
     * <p>使用方式：在测试类上标注 {@code @ExtendWith(ElicitTestSupport.AutoClear.class)}。
     */
    public static class AutoClear implements AfterEachCallback {
        @Override
        public void afterEach(ExtensionContext context) {
            ClientBridgeHolder.clear();
        }
    }
}
