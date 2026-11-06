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

package com.dji.flighthub.mcp.transport;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ToolCallParams} 单测：只钉住「name 与 arguments 的形状」，不涉及业务语义。
 *
 * @author DJI DevKit Team
 */
class ToolCallParamsTest {

    @Test
    void parse_shouldReturnNameAndArguments_whenParamsAreValid() {
        Map<String, Object> arguments = Map.of("drone_sn", "SN-1");

        ToolCallParams parsed = ToolCallParams.parse(Map.of("name", "fh_takeoff", "arguments", arguments));

        assertEquals("fh_takeoff", parsed.toolName());
        assertSame(arguments, parsed.arguments());
    }

    @Test
    void parse_shouldUseEmptyArguments_whenArgumentsMissing() {
        ToolCallParams parsed = ToolCallParams.parse(Map.of("name", "fh_ops"));

        assertTrue(parsed.arguments().isEmpty());
    }

    @Test
    void parse_shouldUseEmptyArguments_whenArgumentsIsNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "fh_ops");
        params.put("arguments", null);

        assertTrue(ToolCallParams.parse(params).arguments().isEmpty());
    }

    @Test
    void parse_shouldThrow_whenNameMissing() {
        InvalidParamsException error = assertThrows(InvalidParamsException.class,
                () -> ToolCallParams.parse(Map.of()));
        assertEquals("Missing required parameter: name", error.getMessage());
    }

    @Test
    void parse_shouldThrow_whenParamsIsNull() {
        assertThrows(InvalidParamsException.class, () -> ToolCallParams.parse(null));
    }

    @Test
    void parse_shouldThrow_whenNameIsNotString() {
        assertThrows(InvalidParamsException.class,
                () -> ToolCallParams.parse(Map.of("name", 42)));
    }

    @Test
    void parse_shouldThrow_whenNameIsBlank() {
        assertThrows(InvalidParamsException.class,
                () -> ToolCallParams.parse(Map.of("name", "   ")));
    }

    @Test
    void parse_shouldThrow_whenArgumentsIsNotObject() {
        assertThrows(InvalidParamsException.class,
                () -> ToolCallParams.parse(Map.of("name", "fh_ops", "arguments", "not-an-object")));
        assertThrows(InvalidParamsException.class,
                () -> ToolCallParams.parse(Map.of("name", "fh_ops", "arguments", List.of("x"))));
        assertThrows(InvalidParamsException.class,
                () -> ToolCallParams.parse(Map.of("name", "fh_ops", "arguments", 5)));
    }
}
