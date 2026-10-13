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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数绑定引擎：将 args Map 中的值绑定回 args（带类型转换和默认值），
 * 并执行必填校验、范围校验、条件必填校验。
 *
 * <p>不直接写入 Tool 实例字段（避免 singleton bean 的线程安全问题），
 * 而是将处理后的值写回 args Map，由 ToolContext 持有。
 *
 * @author DJI FlightHub SDK Team
 */
public final class ParamBinder {

    /** requiredWhen 表达式格式："fieldName in [val1, val2, ...]" */
    private static final Pattern REQUIRED_WHEN_PATTERN =
            Pattern.compile("^(\\w+)\\s+in\\s+\\[([^]]+)]$");

    private ParamBinder() {
        // 工具类，禁止实例化
    }

    /**
     * 绑定并校验参数。成功返回 null，失败返回错误响应 Map。
     *
     * <p>处理流程（对每个 @McpParam 字段）：
     * <ol>
     *   <li>读取 args 中的原始值</li>
     *   <li>缺失时填充默认值</li>
     *   <li>必填校验（含条件必填）</li>
     *   <li>类型转换（String → Number/Boolean）</li>
     *   <li>范围校验</li>
     *   <li>转换后的值写回 args</li>
     * </ol>
     *
     * @param toolClass Tool 类（从中读取 @McpParam 字段注解）
     * @param args      输入参数 Map（会被原地修改：填充默认值、类型转换）
     * @return null=绑定成功; 非 null=校验错误响应
     */
    public static Map<String, Object> bind(Class<? extends AbstractMcpTool> toolClass,
                                           Map<String, Object> args) {
        List<Field> fields = SchemaGenerator.collectAnnotatedFields(toolClass);

        for (Field field : fields) {
            McpParam param = field.getAnnotation(McpParam.class);
            String paramName = param.name().isEmpty() ? field.getName() : param.name();

            Object rawValue = args != null ? args.get(paramName) : null;

            // 默认值填充
            if (rawValue == null && !param.defaultValue().isEmpty()) {
                rawValue = convertDefault(param.defaultValue(), field.getType());
                if (args != null) {
                    args.put(paramName, rawValue);
                }
            }

            // 必填校验（null 和空白字符串均视为缺失，与 Legacy getStringArg 行为一致）
            boolean isEffectivelyEmpty = rawValue == null
                    || (rawValue instanceof String s && s.isBlank());
            if (isEffectivelyEmpty && param.required()) {
                return errorResult(
                        "Missing required parameter: " + paramName,
                        "Provide " + paramName + ". " + param.description());
            }

            // 条件必填校验
            if (isEffectivelyEmpty && !param.requiredWhen().isEmpty()) {
                if (evaluateCondition(param.requiredWhen(), args)) {
                    return errorResult(
                            "Missing parameter: " + paramName,
                            paramName + " is required when " + param.requiredWhen());
                }
            }

            // 类型转换 + 范围校验
            if (rawValue != null) {
                Object converted = convertValue(rawValue, field.getType());
                if (converted != null && args != null) {
                    args.put(paramName, converted);
                }

                Map<String, Object> rangeError = validateRange(
                        converted != null ? converted : rawValue, param, paramName);
                if (rangeError != null) {
                    return rangeError;
                }
            }
        }
        return null;
    }

    /**
     * 将字符串形式的默认值转为目标 Java 类型。
     * 转换失败时抛出 IllegalStateException，提供开发者友好的错误信息（配置错误，应在开发期发现）。
     *
     * @param defaultValue 字符串默认值
     * @param targetType   目标字段类型
     * @return 转换后的值对象
     * @throws IllegalStateException 默认值格式与字段类型不匹配时
     */
    private static Object convertDefault(String defaultValue, Class<?> targetType) {
        if (targetType == String.class) {
            return defaultValue;
        }
        try {
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(defaultValue);
            }
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(defaultValue);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(defaultValue);
            }
            if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(defaultValue);
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "@McpParam defaultValue \"" + defaultValue
                            + "\" cannot be parsed as " + targetType.getSimpleName()
                            + ". Fix the annotation configuration.", e);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(defaultValue);
        }
        return defaultValue;
    }

    /**
     * 将运行时传入的值转换为字段声明的目标类型。
     * 仅在类型不匹配时转换，返回 null 表示无需转换（值已是目标类型）。
     *
     * @param value      原始值
     * @param targetType 目标字段类型
     * @return 转换后的值；已匹配时返回 null（表示不需要写回）
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return null;
        }
        if (value instanceof String s && !s.isBlank()) {
            return convertDefault(s.trim(), targetType);
        }
        if (value instanceof Number n) {
            if (targetType == Double.class || targetType == double.class) {
                return n.doubleValue();
            }
            if (targetType == Integer.class || targetType == int.class) {
                return n.intValue();
            }
            if (targetType == Long.class || targetType == long.class) {
                return n.longValue();
            }
        }
        return null;
    }

    /**
     * 校验数值参数是否在 @McpParam#range 指定范围内。
     *
     * @param value     待校验的值（类型转换后）
     * @param param     @McpParam 注解
     * @param paramName 参数名（用于错误信息）
     * @return null=校验通过; 非 null=范围错误响应
     */
    private static Map<String, Object> validateRange(Object value, McpParam param, String paramName) {
        if (param.range().length != 2) {
            return null;
        }
        double min = param.range()[0];
        double max = param.range()[1];

        if (value instanceof Number n) {
            double numericValue = n.doubleValue();
            if (numericValue < min || numericValue > max) {
                return errorResult(
                        "Parameter out of range: " + paramName + "=" + numericValue,
                        paramName + " must be between " + min + " and " + max);
            }
        }
        return null;
    }

    /**
     * 求值 requiredWhen 条件表达式。
     * 支持格式："fieldName in [val1, val2, ...]"
     *
     * @param expression requiredWhen 表达式字符串
     * @param args       当前参数 Map
     * @return true 表示条件成立（即本参数变为必填）
     */
    private static boolean evaluateCondition(String expression, Map<String, Object> args) {
        if (args == null) {
            return false;
        }
        Matcher matcher = REQUIRED_WHEN_PATTERN.matcher(expression.trim());
        if (!matcher.matches()) {
            return false;
        }

        String fieldName = matcher.group(1);
        String valuesStr = matcher.group(2);

        Object fieldValue = args.get(fieldName);
        if (fieldValue == null) {
            return false;
        }

        String fieldStr = fieldValue.toString().trim();
        for (String v : valuesStr.split(",")) {
            if (v.trim().equals(fieldStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建统一格式的错误响应 Map。
     *
     * @param error      错误信息
     * @param suggestion 建议操作
     * @return 含 success/error_message/suggestion 的 Map
     */
    private static Map<String, Object> errorResult(String error, String suggestion) {
        Map<String, Object> result = new LinkedHashMap<>(3);
        result.put("success", false);
        result.put("error_message", error);
        result.put("suggestion", suggestion);
        return result;
    }
}
