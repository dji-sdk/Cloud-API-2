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

package com.dji.flighthub.mcp.annotation;

import com.dji.flighthub.mcp.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * 线程安全的 MCP Tool 执行上下文。
 *
 * <p>基于 ThreadLocal 实现，每个请求线程持有独立的参数副本。
 * 生命周期：{@code init()} → 使用 → {@code clear()}（在 finally 块中调用）。
 *
 * <p>用法示例：
 * <pre>{@code
 *   try {
 *       ToolContext.init(args, session);
 *       ToolContext ctx = ToolContext.current();
 *       String sn = ctx.getString("drone_sn");
 *       Double lat = ctx.getDouble("latitude");
 *   } finally {
 *       ToolContext.clear();
 *   }
 * }</pre>
 *
 * @author DJI FlightHub SDK Team
 */
public final class ToolContext {

    private static final Logger log = LoggerFactory.getLogger(ToolContext.class);

    /** ThreadLocal 持有器，确保线程间隔离。 */
    private static final ThreadLocal<ToolContext> HOLDER = new ThreadLocal<>();

    /** 当前请求携带的参数映射，不可变。 */
    private final Map<String, Object> args;

    /** 当前请求绑定的会话上下文，可为 null（未认证场景）。 */
    private final SessionContext session;

    /**
     * 私有构造，通过 {@link #init} 工厂方法创建。
     *
     * @param args    请求参数映射，null 时转为空 Map
     * @param session 会话上下文，可为 null
     */
    private ToolContext(Map<String, Object> args, SessionContext session) {
        this.args = args != null ? args : Collections.emptyMap();
        this.session = session;
    }

    // ==================== 生命周期方法 ====================

    /**
     * 初始化当前线程的执行上下文。
     *
     * @param args    工具调用参数映射
     * @param session 关联的会话上下文，未认证时传 null
     */
    public static void init(Map<String, Object> args, SessionContext session) {
        HOLDER.set(new ToolContext(args, session));
    }

    /**
     * 获取当前线程的上下文。
     *
     * @return 当前线程绑定的 ToolContext，未初始化时返回 null
     */
    public static ToolContext current() {
        return HOLDER.get();
    }

    /**
     * 清除当前线程的上下文。
     *
     * <p><b>必须在 finally 块中调用</b>，避免线程池场景下的内存泄漏与状态污染。
     */
    public static void clear() {
        HOLDER.remove();
    }

    // ==================== 类型安全读取 ====================

    /**
     * 获取绑定的会话上下文。
     *
     * @return 会话上下文，可能为 null
     */
    public SessionContext getSession() {
        return session;
    }

    /**
     * 获取只读参数映射。
     *
     * @return 不可修改的参数映射视图
     */
    public Map<String, Object> getArgs() {
        return Collections.unmodifiableMap(args);
    }

    /**
     * 读取字符串参数。
     *
     * @param key 参数键
     * @return 字符串值，键不存在时返回 null
     */
    public String getString(String key) {
        Object val = args.get(key);
        if (val == null) {
            return null;
        }
        String s = val.toString();
        return s.isBlank() ? null : s;
    }

    /**
     * 读取字符串参数，键缺失时返回默认值。
     *
     * @param key      参数键
     * @param fallback 默认值
     * @return 字符串值或 fallback
     */
    public String getString(String key, String fallback) {
        String val = getString(key);
        return val != null ? val : fallback;
    }

    /**
     * 读取 Double 参数，支持 Number 和 String 类型自动转换。
     *
     * @param key 参数键
     * @return Double 值，键不存在或转换失败时返回 null
     */
    public Double getDouble(String key) {
        Object val = args.get(key);
        if (val instanceof Number n) {
            return n.doubleValue();
        }
        if (val instanceof String s && !s.isBlank()) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                log.debug("参数解析为 Double 失败: key={}, value={}", key, val, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 读取 Integer 参数，支持 Number 和 String 类型自动转换。
     *
     * @param key 参数键
     * @return Integer 值，键不存在或转换失败时返回 null
     */
    public Integer getInt(String key) {
        Object val = args.get(key);
        if (val instanceof Number n) {
            return n.intValue();
        }
        if (val instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                log.debug("参数解析为 Integer 失败: key={}, value={}", key, val, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 读取 Boolean 参数，支持 Boolean 和 String 类型自动转换。
     *
     * @param key 参数键
     * @return Boolean 值，键不存在时返回 false
     */
    public Boolean getBoolean(String key) {
        Object val = args.get(key);
        if (val instanceof Boolean b) {
            return b;
        }
        if (val instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return false;
    }
}
