// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对 {@link Json} 解析结果 (Map/List 树)的类型安全只读视图。
 *
 * <p>提供带默认值的取值方法, 避免到处写 {@code (Double) map.get(...)} 与空指针判断。
 * 取不到值时一律回退默认值而不抛异常 (设计意图: 配置残缺不阻断运行), 但<b>回退原因分两级记录</b>:
 *
 * <table border="1">
 *   <caption>回退日志分级</caption>
 *   <tr><th>情形</th><th>级别</th><th>理由</th></tr>
 *   <tr><td>key 不存在 / 值为 JSON {@code null}</td><td>DEBUG</td>
 *       <td>"没配就用默认值"是本类的正常工作方式, 每个 demo 都有几十个可选项没配</td></tr>
 *   <tr><td>key 存在但<b>类型不对</b>(如 {@code "max_speed": "10 m/s"})</td><td><b>WARN</b></td>
 *       <td>用户<b>明确配了</b>却没生效——这是一次真实的配置错误, 必须能在日志里一眼看到,
 *           而不是和几十条"没配"混在同一级里说"缺少配置"(用户会以为自己没写)</td></tr>
 * </table>
 *
 * <p>必填参数请用 {@link #requireDouble} 这类 {@code requireXxx} 严格访问器: 取不到直接抛异常,
 * 避免默认值掩盖一次真实的配置错误。
 */
public final class Node {

    private static final Node EMPTY = new Node(Collections.<String, Object>emptyMap());

    /** 类型错误日志里回显原值的最大长度; 超出截断, 防止一整个数组/对象刷屏。 */
    private static final int VALUE_ECHO_LIMIT = 80;

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

    public String str(String key, String def) {
        Object v = map.get(key);
        // 标量 (字符串/数值/布尔)都可以安全地当字符串用 (如 port 写成 30812 或 "30812" 都行);
        // 但 Map/List 用 String.valueOf 会拼出 "{a=1}" 这种垃圾值, 属类型错误。
        if (v instanceof String || v instanceof Number || v instanceof Boolean) {
            return String.valueOf(v);
        }
        return fallback(key, v, "字符串", def);
    }

    public double dbl(String key, double def) {
        Object v = map.get(key);
        if (v instanceof Number) { return ((Number) v).doubleValue(); }
        if (v instanceof String) {
            try { return Double.parseDouble(((String) v).trim()); } catch (NumberFormatException ignore) {}
        }
        return fallback(key, v, "数值", def);
    }

    /**
     * 取必填数值配置: key 缺失、值为 null 或无法解析成数字时抛异常, 而非像 {@link #dbl} 那样
     * 静默回退默认值——用于飞行目标点这类必须有明确合法值的场景, 避免默认值掩盖一次真实的
     * 配置错误 (如误填字符串/嵌套对象)。
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
                return Double.parseDouble(((String) v).trim());
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
            try { return Integer.parseInt(((String) v).trim()); } catch (NumberFormatException ignore) {}
        }
        return fallback(key, v, "整数", def);
    }

    public long longVal(String key, long def) {
        Object v = map.get(key);
        if (v instanceof Number) { return ((Number) v).longValue(); }
        if (v instanceof String) {
            try { return Long.parseLong(((String) v).trim()); } catch (NumberFormatException ignore) {}
        }
        return fallback(key, v, "长整数", def);
    }

    /**
     * 取布尔配置。
     *
     * <p><b>字符串只认忽略大小写的 {@code "true"}/{@code "false"}</b>, 其余一律按类型错误回退默认值。
     * 原实现直接用 {@code Boolean.parseBoolean}, 而它对<b>任何</b>非 {@code "true"} 的字符串都返回
     * {@code false}——于是 {@code "1"}/{@code "yes"}/{@code "on"} 乃至拼错的 {@code "ture"} 全都
     * 静默变成"关闭", 既不是用户想要的 {@code true}, 也不是本方法承诺的 {@code def}, 且无任何日志。
     * 现在这些值会走 WARN 分支明确告知"写法不认识, 请写 true/false", 并如实回退 {@code def}。
     */
    public boolean bool(String key, boolean def) {
        Object v = map.get(key);
        if (v instanceof Boolean) { return (Boolean) v; }
        if (v instanceof String) {
            String s = ((String) v).trim();
            if ("true".equalsIgnoreCase(s)) { return true; }
            if ("false".equalsIgnoreCase(s)) { return false; }
        }
        return fallback(key, v, "布尔(true/false)", def);
    }

    /**
     * 取列表配置; 命中与未命中<b>都</b>返回不可修改视图。
     *
     * <p>命中时原先直接返回底层解析树里的那个 {@code List} 活引用: 调用方一个 {@code add}/{@code sort}
     * 就会改掉整棵配置树, 之后别处再读同一个 key 拿到的已是被改过的值; 而未命中返回的
     * {@code emptyList()} 是不可变的——同一个方法两条路给出两种可变性, 是否会炸取决于配置里
     * 恰好有没有这个 key。现统一包 {@code unmodifiableList}, 违规修改在<b>两条路上</b>都立即
     * 抛 {@code UnsupportedOperationException}, 而不是安静地污染配置。
     */
    @SuppressWarnings("unchecked")
    public List<Object> list(String key) {
        Object v = map.get(key);
        if (v instanceof List) {
            return Collections.unmodifiableList((List<Object>) v);
        }
        return fallback(key, v, "列表", Collections.<Object>emptyList());
    }

    /**
     * 统一的回退日志 + 返回默认值。区分「没配」与「配错了」两种情形 (见类注释的分级表)。
     *
     * @param key  配置键名
     * @param raw  {@code map.get(key)} 的原值; {@code null} 表示 key 不存在或值为 JSON null
     * @param kind 期望类型的中文名, 用于文案
     * @param def  回退的默认值, 原样返回
     */
    private <T> T fallback(String key, Object raw, String kind, T def) {
        if (raw != null) {
            // 键存在但类型不对: 用户明确配了却没生效, 属真实配置错误 → WARN, 并回显实际类型与值,
            // 让人不必翻源码就知道该怎么改 (如 "需要数值, 实际 String=\"10 m/s\"")。
            DemoLog.warn("Node", "配置 '" + key + "' 类型错误(需要" + kind + ", 实际 "
                + raw.getClass().getSimpleName() + "=" + echo(raw) + "), 改用默认值 " + def);
        } else if (map.containsKey(key)) {
            // 显式写了 null: 与"压根没写"分开说, 否则用户对着自己写下的那行 null 看到"缺少配置"会困惑。
            DemoLog.debug("Node", "配置 '" + key + "' 的值为 null, 用默认值 " + def);
        } else {
            DemoLog.debug("Node", "缺少" + kind + "配置 '" + key + "', 用默认值 " + def);
        }
        return def;
    }

    /** 日志里回显原值: 字符串加引号 (区分 {@code 10} 与 {@code "10"}), 过长则截断。 */
    private static String echo(Object raw) {
        String s = (raw instanceof String) ? "\"" + raw + "\"" : String.valueOf(raw);
        return s.length() <= VALUE_ECHO_LIMIT ? s : s.substring(0, VALUE_ECHO_LIMIT) + "...(已截断)";
    }
}
