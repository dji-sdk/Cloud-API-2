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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP Tool 参数声明注解。
 * 标注在 AbstractMcpTool 子类的字段上，声明该参数的 JSON Schema 属性。
 *
 * 字段类型映射：
 *   String → "string"
 *   Integer/int → "integer"
 *   Long/long → "integer"
 *   Double/double/Float/float → "number"
 *   Boolean/boolean → "boolean"
 *
 * 示例：
 * {@code
 * @McpParam(description = "Target latitude", required = true, range = {-90, 90})
 * private Double latitude;
 * }
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpParam {
    /** 参数名称（默认取字段名） */
    String name() default "";

    /** 参数描述（JSON Schema description） */
    String description();

    /** 是否必填 */
    boolean required() default false;

    /** 枚举值列表（用于生成 enum 约束） */
    String[] enumValues() default {};

    /** 数值范围 [min, max]（仅对数值类型有效） */
    double[] range() default {};

    /** 默认值（字符串表示，运行时按字段类型转换） */
    String defaultValue() default "";

    /**
     * 条件必填表达式。
     * 格式："fieldName in [val1, val2]"
     * 含义：当 fieldName 的值在给定列表中时，本参数变为必填。
     */
    String requiredWhen() default "";

    /** 类型覆盖（默认自动推断） */
    String type() default "";
}
