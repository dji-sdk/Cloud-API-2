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

/**
 * 通过 ThreadLocal 承载当前 tool 调用的 {@link ClientBridge}。
 *
 * <p>设计动机：transport 在工作线程上执行 tool，执行前将本次调用的 bridge 绑定到
 * ThreadLocal，tool 基类按需读取。这样避免了给全部 21 个 tool 的构造函数注入 bridge
 * （会波及所有 tool 及其单元测试）。单元测试直接调用 {@code execute()} 时未绑定 bridge，
 * {@link #get()} 返回 null，tool 自动回退到本地 confirm 校验 —— 存量测试无需改动。
 *
 * @author DJI FlightHub SDK Team
 */
public final class ClientBridgeHolder {

    private static final ThreadLocal<ClientBridge> CURRENT = new ThreadLocal<>();

    private ClientBridgeHolder() {
        // 工具类禁止实例化
    }

    /**
     * 绑定当前线程的 bridge（transport 在执行 tool 前调用）。
     *
     * @param bridge 当前调用的 client bridge
     */
    public static void set(ClientBridge bridge) {
        CURRENT.set(bridge);
    }

    /**
     * 获取当前线程绑定的 bridge。
     *
     * @return bridge，未绑定时返回 null
     */
    public static ClientBridge get() {
        return CURRENT.get();
    }

    /**
     * 清除当前线程绑定（transport 在 tool 执行完成后必须调用，防止线程复用泄漏）。
     */
    public static void clear() {
        CURRENT.remove();
    }
}
