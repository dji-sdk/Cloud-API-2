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

package com.dji.fh.feature.chat.tool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 导航信号持有者 — 以 requestId 为 key 实现请求级隔离。
 *
 * <p>并发请求各自持有独立的导航信号，避免 User A 的起飞操作将 User B 导航到错误的驾驶舱。
 * requestId 通过 {@link ToolCallContext#currentRequestId()} 获取，
 * 依赖 Reactor Context Propagation 跨线程传播。
 *
 * <p>使用流程：
 * <ol>
 *   <li>McpProxyChatTool 在特定工具成功时调用 {@link #offer(String, Map)} 写入信号</li>
 *   <li>ChatController 在流结束后调用 {@link #take(String)} 检查是否有导航信号</li>
 *   <li>{@code take()} 返回信号后自动清理对应 key</li>
 * </ol>
 *
 * @author DJI DevKit Team
 */
public final class NavigationHolder {

    private static final ConcurrentMap<String, Payload> HOLDER = new ConcurrentHashMap<>();

    private NavigationHolder() { }

    /**
     * 写入导航信号（基于当前线程的 requestId 隔离）。
     *
     * @param path  前端路由路径，如 {@code /cockpit}
     * @param query 路由查询参数，如 {@code {drone: sn}}
     */
    public static void offer(String path, Map<String, String> query) {
        String requestId = ToolCallContext.currentRequestId();
        if (requestId == null) {
            return;
        }
        HOLDER.put(requestId, new Payload(path, Collections.unmodifiableMap(new HashMap<>(query))));
    }

    /**
     * 取走并清除指定请求的导航信号。
     *
     * @param requestId 请求唯一标识
     * @return 若存在导航信号则返回 {@link Payload}，否则返回 {@code null}
     */
    public static Payload take(String requestId) {
        if (requestId == null) {
            return null;
        }
        return HOLDER.remove(requestId);
    }

    /**
     * 清除指定请求的导航信号（用于异常路径兜底清理）。
     *
     * @param requestId 请求唯一标识
     */
    public static void clear(String requestId) {
        if (requestId != null) {
            HOLDER.remove(requestId);
        }
    }

    /**
     * 导航信号载体，持有目标路径和查询参数。
     */
    public static final class Payload {

        /** 前端路由路径。 */
        private final String path;

        /** 路由查询参数（不可变）。 */
        private final Map<String, String> query;

        /**
         * @param path  路由路径
         * @param query 查询参数
         */
        public Payload(String path, Map<String, String> query) {
            this.path = path;
            this.query = query;
        }

        /**
         * @return 路由路径
         */
        public String getPath() {
            return path;
        }

        /**
         * @return 查询参数（不可变视图）
         */
        public Map<String, String> getQuery() {
            return query;
        }
    }
}
