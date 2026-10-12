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

import com.dji.flighthub.mcp.tools.AbstractMcpTool;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON Schema 生成器。
 * 根据 @McpTool 和 @McpParam 注解自动生成 MCP 协议要求的 tool definition。
 *
 * <p>结果通过 ConcurrentHashMap 缓存（Tool class → schema），避免重复反射。
 *
 * @author DJI FlightHub SDK Team
 */
public final class SchemaGenerator {

    /** Tool definition 缓存（class → immutable definition Map） */
    private static final Map<Class<?>, Map<String, Object>> CACHE = new ConcurrentHashMap<>();

    private SchemaGenerator() {
        // 工具类，禁止实例化
    }

    /**
     * 为指定 Tool 类生成完整的 tool definition（含 name, description, inputSchema）。
     * 结果被缓存，后续调用返回同一实例。
     *
     * @param toolClass 标注了 @McpTool 的 AbstractMcpTool 子类
     * @return 完整的 tool definition Map
     * @throws IllegalArgumentException 如果类上没有 @McpTool 注解
     */
    public static Map<String, Object> generate(Class<? extends AbstractMcpTool> toolClass) {
        return CACHE.computeIfAbsent(toolClass, SchemaGenerator::doGenerate);
    }

    /**
     * 内部生成逻辑：读取 @McpTool 注解并组装 definition。
     *
     * @param toolClass 工具类
     * @return 不可变的 tool definition Map
     */
    private static Map<String, Object> doGenerate(Class<?> toolClass) {
        McpTool meta = toolClass.getAnnotation(McpTool.class);
        if (meta == null) {
            throw new IllegalArgumentException(
                    toolClass.getSimpleName() + " is not annotated with @McpTool");
        }

        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("name", meta.name());
        definition.put("description", meta.description());
        definition.put("inputSchema", buildInputSchema(toolClass));

        return Collections.unmodifiableMap(definition);
    }

    /**
     * 构建 JSON Schema 的 inputSchema 部分。
     * 遍历类继承链中所有 @McpParam 字段，生成 properties + required 列表。
     * 自动追加 dry_run 参数。
     *
     * @param toolClass 工具类
     * @return inputSchema Map
     */
    private static Map<String, Object> buildInputSchema(Class<?> toolClass) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> requiredList = new ArrayList<>();

        // 收集所有 @McpParam 字段（含父类）
        List<Field> fields = collectAnnotatedFields(toolClass);
        for (Field field : fields) {
            McpParam param = field.getAnnotation(McpParam.class);
            String paramName = param.name().isEmpty() ? field.getName() : param.name();

            Map<String, Object> prop = buildPropertySchema(field, param);
            properties.put(paramName, prop);

            if (param.required()) {
                requiredList.add(paramName);
            }
        }

        // 自动追加 dry_run 参数（所有 Tool 通用）
        properties.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and show what would happen without executing"
        ));

        schema.put("properties", properties);
        if (!requiredList.isEmpty()) {
            schema.put("required", requiredList);
        }
        return schema;
    }

    /**
     * 为单个 @McpParam 字段生成 property schema。
     *
     * @param field  字段反射对象
     * @param param  @McpParam 注解实例
     * @return property schema Map
     */
    private static Map<String, Object> buildPropertySchema(Field field, McpParam param) {
        Map<String, Object> prop = new LinkedHashMap<>();

        // 类型推断：优先使用注解中的 type 覆盖，否则按字段类型推断
        String jsonType = param.type().isEmpty() ? inferType(field.getType()) : param.type();
        prop.put("type", jsonType);
        prop.put("description", param.description());

        // 枚举值约束
        if (param.enumValues().length > 0) {
            prop.put("enum", List.of(param.enumValues()));
        }

        // 默认值（按字段类型转换）
        if (!param.defaultValue().isEmpty()) {
            prop.put("default", convertDefaultForSchema(param.defaultValue(), field.getType()));
        }

        // 数值范围约束（输出为 JSON Schema 的 minimum/maximum，供 LLM 感知合法范围）
        if (param.range().length == 2) {
            prop.put("minimum", param.range()[0]);
            prop.put("maximum", param.range()[1]);
        }

        return prop;
    }

    /**
     * Java 类型 → JSON Schema type 推断。
     *
     * @param type Java 字段类型
     * @return JSON Schema type 字符串
     */
    private static String inferType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }
        if (type == Integer.class || type == int.class
                || type == Long.class || type == long.class) {
            return "integer";
        }
        if (type == Double.class || type == double.class
                || type == Float.class || type == float.class) {
            return "number";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        }
        return "string";
    }

    /**
     * 将默认值字符串转为 Schema 中的对应类型（Integer/Double/Boolean/String）。
     *
     * @param defaultValue 字符串形式的默认值
     * @param type         字段 Java 类型
     * @return 转换后的默认值对象
     */
    private static Object convertDefaultForSchema(String defaultValue, Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(defaultValue);
        }
        if (type == Long.class || type == long.class) {
            return Long.parseLong(defaultValue);
        }
        if (type == Double.class || type == double.class) {
            return Double.parseDouble(defaultValue);
        }
        if (type == Float.class || type == float.class) {
            return Float.parseFloat(defaultValue);
        }
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(defaultValue);
        }
        return defaultValue;
    }

    /**
     * 收集类继承链中所有 @McpParam 注解的字段（从父类到子类顺序）。
     * Package-private 以允许 ParamBinder（Task 4）复用此方法。
     *
     * @param clazz 工具类
     * @return 按继承顺序排列的 @McpParam 字段列表
     */
    static List<Field> collectAnnotatedFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        // 从顶层父类向下收集，确保字段顺序稳定
        collectFieldsRecursive(clazz, result);
        return result;
    }

    /**
     * 递归收集字段（先递归父类，再收集当前类的字段）。
     *
     * @param clazz  当前类
     * @param result 结果列表（累积）
     */
    private static void collectFieldsRecursive(Class<?> clazz, List<Field> result) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        collectFieldsRecursive(clazz.getSuperclass(), result);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(McpParam.class)) {
                field.setAccessible(true);
                result.add(field);
            }
        }
    }
}
