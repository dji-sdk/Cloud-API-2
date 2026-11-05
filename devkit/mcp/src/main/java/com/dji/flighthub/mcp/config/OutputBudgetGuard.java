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

package com.dji.flighthub.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tool 输出体积预算守卫。
 *
 * <p>底层 SDK 可能返回超大负载（十万行日志、超大设备列表等），直接透传给 LLM 会耗尽
 * token 并放大内存占用。本类在 {@link McpToolRegistry} 返回结果前统一兜底：
 * <ol>
 *   <li>按传输层同样的 pretty-print 序列化方式测量字节数</li>
 *   <li>未超阈值：原样返回，零侵入</li>
 *   <li>超阈值：截断超长列表与超长字符串，并注入 {@code output_truncated}
 *       / {@code output_truncation} 元信息，提示调用方改用分页或缩小过滤条件</li>
 * </ol>
 *
 * <p>截断采用"收缩预算"循环：每轮未达标即把列表条数、字符串长度预算减半，
 * 直到达标或到达下限，循环轮数因此有上界，不会出现死循环。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class OutputBudgetGuard {

    private static final Logger log = LoggerFactory.getLogger(OutputBudgetGuard.class);

    /** 截断标记键：结果是否被截断 */
    public static final String KEY_TRUNCATED = "output_truncated";

    /** 截断详情键：原始字节数、被截断的列表/字符串路径等 */
    public static final String KEY_DETAIL = "output_truncation";

    private static final String FIELD_LIMIT_BYTES = "limit_bytes";
    private static final String FIELD_ORIGINAL_BYTES = "original_bytes";
    private static final String FIELD_TRUNCATED_ARRAYS = "truncated_arrays";
    private static final String FIELD_TRUNCATED_STRINGS = "truncated_strings";
    private static final String FIELD_HINT = "hint";
    private static final String HINT_TEXT =
            "Result exceeded the output budget. Narrow the query (page/page_size, time range or "
                    + "device filter) to retrieve the remaining data.";

    /** 单轮最多收缩的轮数（列表/字符串预算每次减半，留给极端嵌套结构） */
    private static final int MAX_SHRINK_ROUNDS = 24;

    /** 列表条数下限：至少保留 1 条，便于调用方判断数据结构 */
    private static final int MIN_ARRAY_ITEMS = 1;

    /** 字符串长度下限：至少保留 256 字符 */
    private static final int MIN_STRING_CHARS = 256;

    /** 收缩系数：每轮预算减半 */
    private static final int SHRINK_DIVISOR = 2;

    /** 递归深度上限：防止异常嵌套结构导致栈溢出 */
    private static final int MAX_DEPTH = 64;

    /** 测量失败标记（无法序列化的结构，交由传输层处理） */
    private static final long UNMEASURABLE = -1L;

    /** 截断字符串后缀模板 */
    private static final String TRUNCATED_SUFFIX_FORMAT = "…[truncated %d chars]";

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final int maxBytes;
    private final int maxArrayItems;
    private final int maxStringChars;

    /**
     * 构造输出预算守卫。
     *
     * @param objectMapper Spring 容器中与传输层共用的 ObjectMapper
     * @param properties   FlightHub 配置（读取 {@code flighthub.output.*}）
     */
    public OutputBudgetGuard(ObjectMapper objectMapper, FlightHubProperties properties) {
        this.objectMapper = objectMapper;
        FlightHubProperties.Output output = properties.getOutput();
        this.enabled = output.isEnabled();
        this.maxBytes = output.getMaxBytes();
        this.maxArrayItems = output.getMaxArrayItems();
        this.maxStringChars = output.getMaxStringChars();
    }

    /**
     * 对 tool 执行结果施加输出预算。
     *
     * <p>任何内部异常都会被吞掉并返回原结果，保证兜底逻辑不会中断业务。
     *
     * @param toolName tool 名称（仅用于日志）
     * @param result   tool 原始执行结果
     * @return 原始结果或截断后的结果
     */
    public Map<String, Object> apply(String toolName, Map<String, Object> result) {
        if (!enabled || result == null) {
            return result;
        }

        long originalBytes = measure(result);
        if (originalBytes < 0 || originalBytes <= maxBytes) {
            return result;
        }

        try {
            Map<String, Object> guarded = shrinkUntilWithinBudget(result, originalBytes);
            log.warn("Tool 输出超预算已截断: tool={}, original={}B, returned={}B, limit={}B",
                    toolName, originalBytes, measure(guarded), maxBytes);
            return guarded;
        } catch (Exception e) {
            log.error("输出预算处理异常，返回原始结果: tool={}, error={}", toolName, e.getMessage(), e);
            return result;
        }
    }

    /**
     * 逐轮收缩列表/字符串预算，直到序列化体积落入阈值。
     *
     * @param result       原始结果
     * @param originalBytes 原始序列化字节数
     * @return 带截断元信息的结果
     */
    private Map<String, Object> shrinkUntilWithinBudget(Map<String, Object> result, long originalBytes) {
        int arrayLimit = maxArrayItems;
        int stringLimit = maxStringChars;
        Map<String, Object> candidate = result;

        for (int round = 0; round < MAX_SHRINK_ROUNDS; round++) {
            Map<String, Integer> truncatedArrays = new TreeMap<>();
            List<String> truncatedStrings = new ArrayList<>();
            Map<String, Object> shrunk = asMap(shrink(result, "", 0, arrayLimit, stringLimit,
                    truncatedArrays, truncatedStrings));
            attachMetadata(shrunk, originalBytes, truncatedArrays, truncatedStrings);
            candidate = shrunk;

            long currentBytes = measure(candidate);
            if (currentBytes >= 0 && currentBytes <= maxBytes) {
                return candidate;
            }
            if (arrayLimit <= MIN_ARRAY_ITEMS && stringLimit <= MIN_STRING_CHARS) {
                // 已到下限仍超预算：返回最小可用结果，交由上层感知
                log.warn("输出预算收缩已达下限仍超阈值: returned={}B, limit={}B", currentBytes, maxBytes);
                return candidate;
            }
            arrayLimit = Math.max(MIN_ARRAY_ITEMS, arrayLimit / SHRINK_DIVISOR);
            stringLimit = Math.max(MIN_STRING_CHARS, stringLimit / SHRINK_DIVISOR);
        }
        return candidate;
    }

    /**
     * 递归截断节点：列表按条数上限保留前缀，字符串按长度上限截断。
     *
     * @param node             当前节点
     * @param path             当前路径（用于告知调用方哪里被截断）
     * @param depth            当前递归深度，超过 {@link #MAX_DEPTH} 时保留原节点
     * @param arrayLimit       单列表保留条数上限
     * @param stringLimit      单字符串保留字符数上限
     * @param truncatedArrays  输出参数：路径 → 被省略的元素个数
     * @param truncatedStrings 输出参数：被截断的字符串路径
     * @return 截断后的节点
     */
    private Object shrink(Object node, String path, int depth, int arrayLimit, int stringLimit,
                          Map<String, Integer> truncatedArrays, List<String> truncatedStrings) {
        if (depth > MAX_DEPTH) {
            return node;
        }
        if (node instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                copy.put(key, shrink(entry.getValue(), childPath(path, key), depth + 1,
                        arrayLimit, stringLimit, truncatedArrays, truncatedStrings));
            }
            return copy;
        }
        if (node instanceof Collection<?> collection) {
            int kept = 0;
            List<Object> copy = new ArrayList<>(Math.min(collection.size(), arrayLimit));
            for (Object item : collection) {
                if (kept >= arrayLimit) {
                    truncatedArrays.put(path, collection.size() - arrayLimit);
                    break;
                }
                copy.add(shrink(item, path + "[" + kept + "]", depth + 1, arrayLimit, stringLimit,
                        truncatedArrays, truncatedStrings));
                kept++;
            }
            return copy;
        }
        if (node instanceof String text && text.length() > stringLimit) {
            truncatedStrings.add(path);
            int omitted = text.length() - stringLimit;
            return text.substring(0, stringLimit) + String.format(TRUNCATED_SUFFIX_FORMAT, omitted);
        }
        return node;
    }

    /**
     * 注入截断元信息。优先写入 {@code data} 子对象，保持 tool 响应结构一致；
     * 无 {@code data} 对象时写入顶层。
     *
     * @param result           截断后的结果
     * @param originalBytes    原始字节数
     * @param truncatedArrays  被截断的列表
     * @param truncatedStrings 被截断的字符串
     */
    private void attachMetadata(Map<String, Object> result, long originalBytes,
                                Map<String, Integer> truncatedArrays, List<String> truncatedStrings) {
        Map<String, Object> detail = new LinkedHashMap<>(5);
        detail.put(FIELD_LIMIT_BYTES, maxBytes);
        detail.put(FIELD_ORIGINAL_BYTES, originalBytes);
        detail.put(FIELD_TRUNCATED_ARRAYS, new LinkedHashMap<>(truncatedArrays));
        detail.put(FIELD_TRUNCATED_STRINGS, new ArrayList<>(truncatedStrings));
        detail.put(FIELD_HINT, HINT_TEXT);

        Object data = result.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mutableData = (Map<String, Object>) dataMap;
            mutableData.put(KEY_TRUNCATED, true);
            mutableData.put(KEY_DETAIL, detail);
        } else {
            result.put(KEY_TRUNCATED, true);
            result.put(KEY_DETAIL, detail);
        }
    }

    /**
     * 测量结果序列化后的字节数（使用传输层同样的 pretty-print 方式）。
     * 序列化失败（如自引用结构）时返回 {@link #UNMEASURABLE}，由调用方跳过预算处理。
     *
     * @param value 待测量对象
     * @return 近似字节数；无法序列化时返回 -1
     */
    private long measure(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value)
                    .getBytes(StandardCharsets.UTF_8).length;
        } catch (Exception e) {
            log.debug("输出体积测量失败，跳过预算处理: {}", e.getMessage());
            return UNMEASURABLE;
        }
    }

    /**
     * 将截断节点转回 Map（顶层必为 Map）。
     *
     * @param node 截断后的节点
     * @return Map 结果；异常时返回空 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object node) {
        return node instanceof Map<?, ?> map ? (Map<String, Object>) map : new LinkedHashMap<>();
    }

    /**
     * 拼接子路径。
     *
     * @param parent 父路径
     * @param key    子键
     * @return 点分路径
     */
    private String childPath(String parent, String key) {
        return parent.isEmpty() ? key : parent + "." + key;
    }

    /**
     * 暴露是否启用（供测试/监控使用）。
     *
     * @return true 表示启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 暴露字节阈值（供测试/监控使用）。
     *
     * @return 单次响应最大字节数
     */
    public int getMaxBytes() {
        return maxBytes;
    }

    /**
     * 暴露字符串长度上限（供测试使用）。
     *
     * @return 单字符串字符数上限
     */
    public int getMaxStringChars() {
        return maxStringChars;
    }

    /**
     * 暴露列表条数上限（供测试使用）。
     *
     * @return 单列表条数上限
     */
    public int getMaxArrayItems() {
        return maxArrayItems;
    }
}
