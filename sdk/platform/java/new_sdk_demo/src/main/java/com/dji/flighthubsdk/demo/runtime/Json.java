// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极简零依赖 JSON 解析器(递归下降)。
 *
 * <p>为什么手写：SDK jar 未内置任何 JSON 库, 且本项目约束「零第三方依赖」(不引入 Jackson/Gson)。
 * 高版本 JDK(≥15) 移除了 Nashorn, 故不能用脚本引擎的 JSON.parse。
 *
 * <p>支持标准 JSON：对象 / 数组 / 字符串(含 \\uXXXX 与常见转义)/ 数字 / true/false/null。
 * 返回类型：{@link Map}&lt;String,Object&gt; / {@link List}&lt;Object&gt; / String / Double / Long / Boolean / null。
 * 对象保序(LinkedHashMap)。仅用于读取本地配置文件, 不追求极致性能。
 */
public final class Json {

    /** 递归嵌套上限：超过即抛 FlightHubException(可被上层 catch(Exception) 接住), 避免深层嵌套配置触发不可捕获的 StackOverflowError。 */
    private static final int MAX_DEPTH = 512;

    private final String s;
    private int i;
    /** 当前递归深度(对象/数组每下探一层 +1), 用于封顶防栈溢出。 */
    private int depth;

    private Json(String src) {
        this.s = src;
    }

    /** 解析一段 JSON 文本为对象树。 */
    public static Object parse(String text) {
        if (text == null) {
            throw new FlightHubException("JSON 文本为 null");
        }
        // 剥掉可能存在的 UTF-8 BOM(U+FEFF), 否则会被当成非法首字符导致解析失败。
        if (!text.isEmpty() && text.charAt(0) == '﻿') {
            text = text.substring(1);
        }
        Json p = new Json(text);
        p.ws();
        Object v = p.value();
        p.ws();
        if (p.i < p.s.length()) {
            throw p.err("JSON 结尾存在多余字符");
        }
        return v;
    }

    /** 解析并断言为对象。 */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object v = parse(text);
        if (!(v instanceof Map)) {
            throw new FlightHubException("期望 JSON 对象, 实际为 " + typeName(v));
        }
        return (Map<String, Object>) v;
    }

    private Object value() {
        // value() 是唯一的递归汇聚点(object/array 的每个子值都经此下探), 在此封顶递归深度。
        if (++depth > MAX_DEPTH) {
            throw err("JSON 嵌套层级过深(>" + MAX_DEPTH + "), 疑似畸形配置");
        }
        try {
            char c = peek();
            switch (c) {
                case '{': return object();
                case '[': return array();
                case '"': return string();
                case 't': case 'f': return bool();
                case 'n': return nullLiteral();
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return number();
                    }
                    throw err("非法字符 '" + c + "'");
            }
        } finally {
            depth--;
        }
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        expect('{');
        ws();
        if (peek() == '}') { i++; return m; }
        while (true) {
            ws();
            if (peek() != '"') throw err("对象 key 必须是字符串");
            String key = string();
            ws();
            expect(':');
            ws();
            m.put(key, value());
            ws();
            char c = next();
            if (c == '}') break;
            if (c != ',') throw err("对象内期望 ',' 或 '}'");
        }
        return m;
    }

    private List<Object> array() {
        List<Object> list = new ArrayList<Object>();
        expect('[');
        ws();
        if (peek() == ']') { i++; return list; }
        while (true) {
            ws();
            list.add(value());
            ws();
            char c = next();
            if (c == ']') break;
            if (c != ',') throw err("数组内期望 ',' 或 ']'");
        }
        return list;
    }

    private String string() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (i >= s.length()) throw err("字符串未闭合");
            char c = s.charAt(i++);
            if (c == '"') break;
            if (c == '\\') {
                if (i >= s.length()) throw err("转义未完成");
                char e = s.charAt(i++);
                switch (e) {
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'u':
                        if (i + 4 > s.length()) throw err("\\u 转义不完整");
                        sb.append(hex4());
                        break;
                    default: throw err("非法转义 \\" + e);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 读取 4 位十六进制并返回对应字符; 非 hex(含 '+'/'-' 符号)时抛异常——{@code Integer.parseInt(...,16)} 会容忍符号, 故手工校验。 */
    private char hex4() {
        int cp = 0;
        for (int k = 0; k < 4; k++) {
            char h = s.charAt(i++);
            int d;
            if (h >= '0' && h <= '9') d = h - '0';
            else if (h >= 'a' && h <= 'f') d = h - 'a' + 10;
            else if (h >= 'A' && h <= 'F') d = h - 'A' + 10;
            else throw err("非法 \\u 转义(非十六进制字符 '" + h + "')");
            cp = (cp << 4) | d;
        }
        return (char) cp;
    }

    private Object number() {
        int start = i;
        if (peek() == '-') i++;
        while (i < s.length() && isNumChar(s.charAt(i))) i++;
        String num = s.substring(start, i);
        try {
            if (num.indexOf('.') >= 0 || num.indexOf('e') >= 0 || num.indexOf('E') >= 0) {
                return Double.valueOf(num);
            }
            try {
                return Long.valueOf(num);
            } catch (NumberFormatException overflow) {
                return Double.valueOf(num);   // 整数超出 long 范围时退回 double
            }
        } catch (NumberFormatException ex) {
            throw err("非法数字: " + num);       // 统一包成 FlightHubException, 不漏出裸 NumberFormatException
        }
    }

    private Boolean bool() {
        if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        throw err("非法布尔字面量");
    }

    private Object nullLiteral() {
        if (s.startsWith("null", i)) { i += 4; return null; }
        throw err("非法 null 字面量");
    }

    // ---- 辅助 ----

    private void ws() {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') { i++; } else { break; }
        }
    }

    private char peek() {
        if (i >= s.length()) throw err("意外的输入结尾");
        return s.charAt(i);
    }

    private char next() {
        if (i >= s.length()) throw err("意外的输入结尾");
        return s.charAt(i++);
    }

    private void expect(char c) {
        char actual = next();
        if (actual != c) throw err("期望 '" + c + "' 但得到 '" + actual + "'");
    }

    private static boolean isNumChar(char c) {
        return (c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+' || c == 'e' || c == 'E';
    }

    private FlightHubException err(String msg) {
        return new FlightHubException("JSON 解析错误 @" + i + ": " + msg);
    }

    private static String typeName(Object v) {
        return v == null ? "null" : v.getClass().getSimpleName();
    }
}
