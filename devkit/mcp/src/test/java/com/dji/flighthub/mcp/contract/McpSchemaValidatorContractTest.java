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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link McpSchemaValidator} 反向自检：确保它不是一个「永远返回空」的假校验器。
 *
 * @author DJI DevKit Team
 */
class McpSchemaValidatorContractTest {

    private static final String MUTATIONS_RESOURCE = "/contract/schema-mutations.json";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final McpSchemaValidator validator = new McpSchemaValidator();

    @Test
    void validateSchema_shouldReportExpectedKeyword_whenSchemaIsBroken() throws Exception {
        JsonNode mutations = loadMutations();
        assertTrue(mutations.isArray() && !mutations.isEmpty(), "mutation 夹具不能为空");

        for (JsonNode mutation : mutations) {
            String caseName = mutation.get("case").asText();
            List<McpSchemaValidator.Violation> violations = validator.validateSchema(mutation.get("schema"));
            String expectedKeyword = mutation.get("expectedKeyword").asText();
            String expectedLocation = mutation.get("expectedLocation").asText();
            assertTrue(violations.stream().anyMatch(v -> expectedKeyword.equals(v.keyword())
                            && expectedLocation.equals(v.instanceLocation())),
                    "用例 " + caseName + " 未报出 keyword=" + expectedKeyword
                            + " location=" + expectedLocation + "，实际: " + violations);
        }
    }

    @Test
    void validateSchema_shouldReportNothing_whenSchemaIsValid() {
        JsonNode schema = objectMapper.createObjectNode()
                .put("type", "object")
                .set("properties", objectMapper.createObjectNode()
                        .set("a", objectMapper.createObjectNode().put("type", "string")));

        assertTrue(validator.validateSchema(schema).isEmpty());
    }

    @Test
    void validateInstance_shouldReportRequiredKeyword_whenRequiredPropertyMissing() {
        JsonNode schema = objectMapper.createObjectNode()
                .put("type", "object")
                .set("properties", objectMapper.createObjectNode()
                        .set("a", objectMapper.createObjectNode().put("type", "string")));
        ((com.fasterxml.jackson.databind.node.ObjectNode) schema).putArray("required").add("a");

        List<McpSchemaValidator.Violation> violations = validator.validateInstance(schema,
                objectMapper.createObjectNode());

        assertFalse(violations.isEmpty(), "实例缺必填属性时必须报错");
        assertTrue(violations.stream().anyMatch(v -> "required".equals(v.keyword())));
    }

    private JsonNode loadMutations() throws Exception {
        try (InputStream input = getClass().getResourceAsStream(MUTATIONS_RESOURCE)) {
            assertNotNull(input, "找不到夹具 " + MUTATIONS_RESOURCE);
            return objectMapper.readTree(input);
        }
    }
}
