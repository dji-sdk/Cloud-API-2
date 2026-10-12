// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对 {@link Json} 解析结果(Map/List 树)的类型安全只读视图。
 *
 * <p>提供带默认值的取值方法, 避免到处写 {@code (Double) map.get(...)} 与空指针判断。
 * 缺失的 key 一律回退默认值并记一条 debug 日志, 让「配置项漏填」不至于直接崩。
 */
public final class Node {

    private static final Node EMPTY = new Node(Collections.<String, Object>emptyMap());

    private final Map<String, Object> map;

    @SuppressWarnings("unchecked")
    public Node(Object raw) {
        this.map = (raw instanceof Map) ? (Map<String, Object>) raw
                : Collections.<String, Object>emptyMap();
    }

    /** 取子对象; 不存在则返回空 Node(链式安全)。 */
    public Node child(String key) {
        Object v = map.get(key);
        return (v instanceof Map) ? new Node(v) : EMPTY;
    }

    public boolean has(String key) {
        return map.containsKey(key) && map.get(key) != null;
    }

    public String str(String key, String def) {
        Object v = map.get(key);
        if (v == null) {
            DemoLog.debug("Node", "缺少字符串配置 '" + key + "', 用默认值 " + def);
            return def;
        }
        return String.valueOf(v);
    }

    public double dbl(String key, double def) {
        Object v = map.get(key);
        if (v instanceof Number) { return ((Number) v).doubleValue(); }
        if (v instanceof String) {
            try { return Double.parseDouble((String) v); } catch (NumberFormatException ignore) {}
        }
        DemoLog.debug("Node", "缺少数值配置 '" + key + "', 用默认值 " + def);
        return def;
    }

    /**
     * 取必填数值配置：key 缺失、值为 null 或无法解析成数字时抛异常, 而非像 {@link #dbl} 那样
     * 静默回退默认值——用于飞行目标点这类必须有明确合法值的场景, 避免默认值掩盖一次真实的
     * 配置错误(如误填字符串/嵌套对象)。
     *
     * @throws FlightHubException key 缺失/类型不匹配/字符串无法解析成数字
     */
    public double requireDouble(String key) {
        Object v = map.get(key);
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        if (v instanceof String) {
            try {
                return Double.parseDouble((String) v);
            } catch (NumberFormatException e) {
                throw new FlightHubException("配置 '" + key + "' 不是合法数值: " + v);
            }
        }
        throw new FlightHubException("配置 '" + key + "' 缺失或类型错误(需要数值), 实际值: "
            + (v == null ? "null" : v));
    }

    public int intVal(String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number) { return ((Number) v).intValue(); }
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch (NumberFormatException ignore) {}
        }
        DemoLog.debug("Node", "缺少整数配置 '" + key + "', 用默认值 " + def);
        return def;
    }

    public long longVal(String key, long def) {
        Object v = map.get(key);
        if (v instanceof Number) { return ((Number) v).longValue(); }
        if (v instanceof String) {
            try { return Long.parseLong((String) v); } catch (NumberFormatException ignore) {}
        }
        DemoLog.debug("Node", "缺少长整数配置 '" + key + "', 用默认值 " + def);
        return def;
    }

    public boolean bool(String key, boolean def) {
        Object v = map.get(key);
        if (v instanceof Boolean) { return (Boolean) v; }
        if (v instanceof String) { return Boolean.parseBoolean((String) v); }
        DemoLog.debug("Node", "缺少布尔配置 '" + key + "', 用默认值 " + def);
        return def;
    }

    @SuppressWarnings("unchecked")
    public List<Object> list(String key) {
        Object v = map.get(key);
        if (v instanceof List) {
            return (List<Object>) v;
        }
        DemoLog.debug("Node", "缺少列表配置 '" + key + "', 用空列表");
        return Collections.emptyList();
    }
}
