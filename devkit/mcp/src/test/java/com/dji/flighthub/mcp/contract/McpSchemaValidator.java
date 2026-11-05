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

package com.dji.flighthub.mcp.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Tool {@code inputSchema} 校验器：Draft 2020-12 meta-schema + 项目自身约定。
 *
 * <p>项目约定（meta-schema 不覆盖）：{@code required} 里的每个字段都必须出现在
 * {@code properties} 中，否则视为违规并产出 {@code keyword = "required"} 的合成条目。</p>
 *
 * <p>断言只基于 {@link Violation#keyword()} 与 {@link Violation#instanceLocation()}，
 * 不比对 message——{@code json-schema-validator} 的 message 是中文本地化的，随 locale 变化。</p>
 *
 * @author DJI DevKit Team
 */
public final class McpSchemaValidator {

    /** Draft 2020-12 meta-schema 的规范 URI */
    private static final String META_SCHEMA_URI = "https://json-schema.org/draft/2020-12/schema";

    private static final String FIELD_PROPERTIES = "properties";

    private static final String FIELD_REQUIRED = "required";

    /** 项目规则产出的合成 keyword */
    private static final String KEYWORD_REQUIRED = "required";

    /** 项目规则产出的合成位置 */
    private static final String LOCATION_REQUIRED = "/required";

    private final SchemaRegistry registry;

    private final Schema metaSchema;

    /**
     * 构造函数。
     */
    public McpSchemaValidator() {
        this.registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        this.metaSchema = registry.getSchema(SchemaLocation.of(META_SCHEMA_URI));
    }

    /**
     * 校验 schema 本身：meta-schema + 项目约定。
     *
     * @param schema 待校验的 inputSchema
     * @return 违规列表，空表示通过
     */
    public List<Violation> validateSchema(JsonNode schema) {
        List<Violation> violations = new ArrayList<>(validateMetaSchema(schema));
        for (String missing : missingRequiredProperties(schema)) {
            violations.add(new Violation(KEYWORD_REQUIRED, LOCATION_REQUIRED,
                    "required 里的属性 " + missing + " 未在 properties 中声明"));
        }
        return violations;
    }

    /**
     * 只做 Draft 2020-12 meta-schema 校验。
     *
     * @param schema 待校验的 schema
     * @return 违规列表
     */
    public List<Violation> validateMetaSchema(JsonNode schema) {
        List<Violation> violations = new ArrayList<>();
        for (com.networknt.schema.Error error : metaSchema.validate(schema)) {
            violations.add(new Violation(error.getKeyword(), String.valueOf(error.getInstanceLocation()),
                    error.getMessage()));
        }
        return violations;
    }

    /**
     * 找出 {@code required} 中引用了未声明属性的字段名。
     *
     * <p>不做去重：{@code required} 中每个未声明的条目都会各自产出一个结果，
     * 调用方通过字段名进行比对。</p>
     *
     * @param schema 待检查的 schema
     * @return 未声明的字段名（按出现顺序，可能重复）
     */
    public List<String> missingRequiredProperties(JsonNode schema) {
        Set<String> declared = new LinkedHashSet<>();
        JsonNode properties = schema.get(FIELD_PROPERTIES);
        if (properties != null && properties.isObject()) {
            properties.fieldNames().forEachRemaining(declared::add);
        }
        List<String> missing = new ArrayList<>();
        JsonNode required = schema.get(FIELD_REQUIRED);
        if (required == null || !required.isArray()) {
            return missing;
        }
        for (JsonNode item : required) {
            if (item.isTextual() && !declared.contains(item.asText())) {
                missing.add(item.asText());
            }
        }
        return missing;
    }

    /**
     * 用 schema 校验一份实例数据。
     *
     * @param schema   业务 schema
     * @param instance 实例数据
     * @return 违规列表
     */
    public List<Violation> validateInstance(JsonNode schema, JsonNode instance) {
        List<Violation> violations = new ArrayList<>();
        for (com.networknt.schema.Error error : registry.getSchema(schema).validate(instance)) {
            violations.add(new Violation(error.getKeyword(), String.valueOf(error.getInstanceLocation()),
                    error.getMessage()));
        }
        return violations;
    }

    /**
     * 一条 schema 违规。
     *
     * @param keyword          JSON Schema keyword
     * @param instanceLocation 实例路径（缺必填属性时为空串）
     * @param message          面向人的描述（可能被本地化，不要用于断言）
     */
    public record Violation(String keyword, String instanceLocation, String message) {

        @Override
        public String toString() {
            return keyword + "@" + instanceLocation + " (" + message + ")";
        }
    }
}
