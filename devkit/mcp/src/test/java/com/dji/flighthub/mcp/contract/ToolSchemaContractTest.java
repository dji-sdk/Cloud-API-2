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

import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B 层：全部已注册 Tool 的 {@code inputSchema} 结构契约（B1–B8）。
 *
 * <p>遍历的是 {@code McpToolRegistry} 的**实际注册集**，不硬编码源文件数量。</p>
 *
 * @author DJI DevKit Team
 */
class ToolSchemaContractTest {

    private static final String FIELD_NAME = "name";

    private static final String FIELD_DESCRIPTION = "description";

    private static final String FIELD_INPUT_SCHEMA = "inputSchema";

    private static final String FIELD_TYPE = "type";

    private static final String FIELD_PROPERTIES = "properties";

    private static final String FIELD_DRY_RUN = "dry_run";

    private static final String JSON_TYPE_OBJECT = "object";

    private static final String JSON_TYPE_BOOLEAN = "boolean";

    /** Tool 名格式：小写字母开头，仅含小写字母、数字、下划线。 */
    private static final Pattern TOOL_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    private static final Set<String> VALID_TYPES = Set.of(
            "object", "array", "string", "number", "integer", "boolean", "null");

    private static McpToolRegistry registry;

    private static List<Map<String, Object>> definitions;

    private static ObjectMapper objectMapper;

    private static McpSchemaValidator validator;

    @BeforeAll
    static void setUpAll() {
        ContractFixtures.Fixture fixture = ContractFixtures.create();
        registry = fixture.registry();
        definitions = registry.getToolDefinitions();
        objectMapper = fixture.objectMapper();
        validator = new McpSchemaValidator();
        System.out.println("[B] 实际注册数 = " + definitions.size());
    }

    @Test
    void b1_inputSchema_shouldSatisfyDraft202012AndProjectRules() {
        List<String> failures = new ArrayList<>();
        for (Map<String, Object> definition : definitions) {
            JsonNode schema = inputSchemaOf(definition);
            for (McpSchemaValidator.Violation violation : validator.validateSchema(schema)) {
                failures.add(definition.get(FIELD_NAME) + " -> " + violation);
            }
        }
        assertTrue(failures.isEmpty(), "inputSchema 违规:\n" + String.join("\n", failures));
    }

    @Test
    void b2_inputSchema_shouldDeclareObjectType() {
        for (Map<String, Object> definition : definitions) {
            JsonNode type = inputSchemaOf(definition).get(FIELD_TYPE);
            assertNotNull(type, definition.get(FIELD_NAME) + ": inputSchema 缺少 type");
            assertEquals(JSON_TYPE_OBJECT, type.asText(),
                    definition.get(FIELD_NAME) + ": inputSchema.type 必须是 object");
        }
    }

    @Test
    void b3_required_shouldReferenceDeclaredProperties() {
        List<String> failures = new ArrayList<>();
        // McpSchemaValidator.validateSchema（b1 已调用）也会报告 required 引用缺失的属性；
        // 这里刻意保留，因为逐属性遍历能给出更清晰的失败信息。
        for (Map<String, Object> definition : definitions) {
            for (String missing : validator.missingRequiredProperties(inputSchemaOf(definition))) {
                failures.add(definition.get(FIELD_NAME) + " -> required 引用了未声明的属性: " + missing);
            }
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void b4_names_shouldMatchPattern() {
        // Tool 名唯一性已由 ContractFixtures.registerAll 以 fail-fast 方式强制，本测试只负责命名格式
        for (Map<String, Object> definition : definitions) {
            Object rawName = definition.get(FIELD_NAME);
            // assertTrue 的参数不参与流作用域，取不到 pattern variable，故用 assertInstanceOf 直接拿到强类型值
            String name = assertInstanceOf(String.class, rawName, "tool.name 必须是字符串");
            assertTrue(TOOL_NAME_PATTERN.matcher(name).matches(),
                    "tool.name 必须匹配 " + TOOL_NAME_PATTERN.pattern() + "，实际: " + name);
        }
    }

    @Test
    void b5_description_shouldNotBeBlank() {
        for (Map<String, Object> definition : definitions) {
            Object description = definition.get(FIELD_DESCRIPTION);
            assertTrue(description instanceof String text && !text.isBlank(),
                    definition.get(FIELD_NAME) + ": description 必须非空");
        }
    }

    @Test
    void b6_dryRun_shouldBeBoolean_whenDeclared() {
        // 防止所有 Tool 都不再声明 dry_run 导致本测试空跑通过
        int inspected = 0;
        for (Map<String, Object> definition : definitions) {
            JsonNode dryRun = inputSchemaOf(definition).path(FIELD_PROPERTIES).path(FIELD_DRY_RUN);
            if (dryRun.isMissingNode()) {
                continue;
            }
            inspected++;
            assertEquals(JSON_TYPE_BOOLEAN, dryRun.path(FIELD_TYPE).asText(),
                    definition.get(FIELD_NAME) + ": dry_run 必须是 boolean");
        }
        System.out.println("[B] dry_run 校验数 = " + inspected);
        assertTrue(inspected > 0, "没有任何 Tool 声明 dry_run，B6 实际未校验任何 schema");
    }

    @Test
    void b7_propertyTypes_shouldBeValidJsonSchemaTypes() {
        List<String> failures = new ArrayList<>();
        for (Map<String, Object> definition : definitions) {
            JsonNode properties = inputSchemaOf(definition).path(FIELD_PROPERTIES);
            if (!properties.isObject()) {
                continue;
            }
            properties.fields().forEachRemaining(entry -> {
                JsonNode type = entry.getValue().get(FIELD_TYPE);
                if (type == null) {
                    return;
                }
                if (type.isArray()) {
                    for (JsonNode item : type) {
                        if (!VALID_TYPES.contains(item.asText())) {
                            failures.add(definition.get(FIELD_NAME) + "." + entry.getKey()
                                    + " -> 非法类型: " + item.asText());
                        }
                    }
                    return;
                }
                if (!VALID_TYPES.contains(type.asText())) {
                    failures.add(definition.get(FIELD_NAME) + "." + entry.getKey()
                            + " -> 非法类型: " + type.asText());
                }
            });
        }
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void b8_registryCount_shouldMatchSnapshot() {
        int registered = definitions.size();
        assertEquals(ContractFixtures.EXPECTED_TOOL_COUNT, registered,
                "实际注册数 = " + registered
                        + "；若新增/删除了 Tool，请同步更新 ContractFixtures.buildTools 与 EXPECTED_TOOL_COUNT");
        // 当前 definitions 与 registry.getToolCount() 同源，这条断言恒真；保留它作为前瞻性防护：
        // 若将来 test 层改成从别处取 definitions，此处才会真正起到交叉校验作用
        assertEquals(definitions.size(), registry.getToolCount(),
                "tools/list 快照数量必须与注册表计数一致（当前两者同源，此断言为前瞻性防护，暂恒真）");
    }

    private static JsonNode inputSchemaOf(Map<String, Object> definition) {
        Object schema = definition.get(FIELD_INPUT_SCHEMA);
        assertNotNull(schema, definition.get(FIELD_NAME) + ": 缺少 inputSchema");
        return objectMapper.valueToTree(schema);
    }
}
