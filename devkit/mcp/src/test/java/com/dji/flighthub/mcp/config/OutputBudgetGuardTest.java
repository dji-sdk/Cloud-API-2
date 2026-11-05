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
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OutputBudgetGuard 单元测试。
 *
 * <p>覆盖：未超预算原样返回、超预算截断、元信息注入位置、超长字符串截断、
 * 禁用开关、异常兜底与配置默认值。
 */
class OutputBudgetGuardTest {

    private static final String TOOL_NAME = "fh_test_tool";
    private static final String DATA = "data";
    private static final String SUCCESS = "success";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造守卫。
     *
     * @param enabled        是否启用
     * @param maxBytes       字节阈值
     * @param maxArrayItems  列表条数上限
     * @param maxStringChars 字符串长度上限
     * @return 守卫实例
     */
    private OutputBudgetGuard newGuard(boolean enabled, int maxBytes, int maxArrayItems, int maxStringChars) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getOutput().setEnabled(enabled);
        properties.getOutput().setMaxBytes(maxBytes);
        properties.getOutput().setMaxArrayItems(maxArrayItems);
        properties.getOutput().setMaxStringChars(maxStringChars);
        return new OutputBudgetGuard(objectMapper, properties);
    }

    /**
     * 构造一个带 data 子对象的成功响应，data.items 含 count 条记录。
     *
     * @param count 记录条数
     * @return 响应 Map
     */
    private Map<String, Object> buildResponse(int count) {
        List<Map<String, Object>> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", "item-" + i);
            item.put("name", "payload-of-item-" + i);
            items.add(item);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", count);
        data.put("items", items);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(SUCCESS, true);
        result.put(DATA, data);
        return result;
    }

    /**
     * 测量序列化后字节数。
     *
     * @param value 待测对象
     * @return 字节数
     */
    private long sizeOf(Object value) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(value).getBytes(StandardCharsets.UTF_8).length;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 读取 data 子对象。
     *
     * @param result 响应
     * @return data 子对象
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> dataOf(Map<String, Object> result) {
        return (Map<String, Object>) result.get(DATA);
    }

    // ==================== 默认配置 ====================

    @Test
    void constructor_shouldExposeDefaultLimits_whenPropertiesUntouched() {
        OutputBudgetGuard guard = new OutputBudgetGuard(objectMapper, new FlightHubProperties());

        assertTrue(guard.isEnabled());
        assertEquals(256 * 1024, guard.getMaxBytes());
        assertEquals(500, guard.getMaxArrayItems());
        assertEquals(8192, guard.getMaxStringChars());
    }

    // ==================== 未超预算 ====================

    @Test
    void apply_shouldReturnSameInstance_whenResultWithinBudget() {
        OutputBudgetGuard guard = newGuard(true, 1024 * 1024, 500, 8192);
        Map<String, Object> result = buildResponse(3);

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);

        assertSame(result, guarded);
        assertNull(dataOf(guarded).get(OutputBudgetGuard.KEY_TRUNCATED));
    }

    @Test
    void apply_shouldReturnNull_whenResultIsNull() {
        OutputBudgetGuard guard = newGuard(true, 1024, 10, 100);

        assertNull(guard.apply(TOOL_NAME, null));
    }

    @Test
    void apply_shouldReturnSameInstance_whenDisabled() {
        OutputBudgetGuard guard = newGuard(false, 64, 1, 16);
        Map<String, Object> result = buildResponse(50);

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);

        assertSame(result, guarded);
        assertNull(dataOf(guarded).get(OutputBudgetGuard.KEY_TRUNCATED));
    }

    // ==================== 超预算截断 ====================

    @Test
    void apply_shouldShrinkItems_whenResultExceedsBudget() {
        OutputBudgetGuard guard = newGuard(true, 2 * 1024, 20, 8192);
        Map<String, Object> result = buildResponse(200);
        assertTrue(sizeOf(result) > 2 * 1024, "前置条件：原始结果应超预算");

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) dataOf(guarded).get("items");
        assertTrue(items.size() < 200, "列表应被截断");
        assertTrue(sizeOf(guarded) <= 2 * 1024, "截断后应落入预算，实际=" + sizeOf(guarded));
    }

    @Test
    void apply_shouldInjectTruncationMetadataIntoData_whenDataIsMap() {
        OutputBudgetGuard guard = newGuard(true, 2 * 1024, 20, 8192);
        Map<String, Object> result = buildResponse(200);

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);
        Map<String, Object> data = dataOf(guarded);

        assertEquals(Boolean.TRUE, data.get(OutputBudgetGuard.KEY_TRUNCATED));
        @SuppressWarnings("unchecked")
        Map<String, Object> detail = (Map<String, Object>) data.get(OutputBudgetGuard.KEY_DETAIL);
        assertNotNull(detail);
        assertEquals(2 * 1024, detail.get("limit_bytes"));
        assertTrue((Long) detail.get("original_bytes") > 2 * 1024);
        assertNotNull(detail.get("truncated_arrays"));
        assertNotNull(detail.get("truncated_strings"));
        assertTrue(detail.get("hint").toString().contains("page"));
        // 顶层不应被污染
        assertNull(guarded.get(OutputBudgetGuard.KEY_TRUNCATED));
    }

    @Test
    void apply_shouldInjectTruncationMetadataAtTopLevel_whenNoDataMap() {
        OutputBudgetGuard guard = newGuard(true, 512, 10, 8192);
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            items.add(Map.of("id", i, "name", "payload-" + i));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(SUCCESS, true);
        result.put("items", items);

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);

        assertEquals(Boolean.TRUE, guarded.get(OutputBudgetGuard.KEY_TRUNCATED));
        assertNotNull(guarded.get(OutputBudgetGuard.KEY_DETAIL));
        assertTrue(sizeOf(guarded) <= 512, "截断后应落入预算，实际=" + sizeOf(guarded));
    }

    @Test
    void apply_shouldTruncateLongString_whenStringExceedsLimit() {
        OutputBudgetGuard guard = newGuard(true, 4096, 500, 256);
        // 单条超长字符串，条数截断无法解决，必须依赖字符串截断
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("log", "x".repeat(20_000));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(SUCCESS, true);
        result.put(DATA, data);

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);
        String log = (String) dataOf(guarded).get("log");

        assertNotNull(log);
        assertTrue(log.length() < 20_000, "字符串应被截断");
        assertTrue(log.endsWith("]"), "应带截断后缀");
        assertTrue(sizeOf(guarded) <= 4096, "截断后应落入预算，实际=" + sizeOf(guarded));
    }

    @Test
    void apply_shouldRecordTruncatedPaths_whenNestedArraysShrink() {
        OutputBudgetGuard guard = newGuard(true, 2 * 1024, 5, 8192);
        Map<String, Object> result = buildResponse(200);

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> detail = (Map<String, Object>) dataOf(guarded)
                .get(OutputBudgetGuard.KEY_DETAIL);
        @SuppressWarnings("unchecked")
        Map<String, Integer> truncatedArrays = (Map<String, Integer>) detail.get("truncated_arrays");

        assertTrue(truncatedArrays.containsKey("data.items"),
                "应记录被截断的列表路径，实际=" + truncatedArrays);
        assertTrue(truncatedArrays.get("data.items") > 0);
    }

    @Test
    void apply_shouldKeepAtLeastOneItem_whenBudgetExtremelySmall() {
        OutputBudgetGuard guard = newGuard(true, 64, 500, 8192);
        Map<String, Object> result = buildResponse(500);

        Map<String, Object> guarded = guard.apply(TOOL_NAME, result);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) dataOf(guarded).get("items");

        assertEquals(1, items.size(), "到达下限时至少保留 1 条，便于调用方感知结构");
        assertEquals(Boolean.TRUE, dataOf(guarded).get(OutputBudgetGuard.KEY_TRUNCATED));
    }

    @Test
    void apply_shouldNotMutateOriginalResult_whenTruncating() {
        OutputBudgetGuard guard = newGuard(true, 2 * 1024, 20, 8192);
        Map<String, Object> result = buildResponse(200);

        guard.apply(TOOL_NAME, result);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> originalItems = (List<Map<String, Object>>) dataOf(result).get("items");
        assertEquals(200, originalItems.size(), "原始结果不应被就地修改");
        assertNull(dataOf(result).get(OutputBudgetGuard.KEY_TRUNCATED));
    }

    // ==================== 异常兜底 ====================

    @Test
    void apply_shouldReturnOriginal_whenSerializationFails() {
        // 自引用结构会让 Jackson 抛出 StackOverflow/JsonMappingException 类异常
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(SUCCESS, true);
        result.put("self", result);

        OutputBudgetGuard guard = newGuard(true, 1, 10, 100);

        assertDoesNotThrow(() -> guard.apply(TOOL_NAME, result));
    }
}
