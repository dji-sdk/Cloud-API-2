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

package com.dji.flighthub.mcp.tools;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 NextStep 机制的基类增强。
 *
 * @author DJI FlightHub SDK Team
 */
class AbstractMcpToolNextStepTest {

    @Test
    void successResultWithNextSteps_shouldIncludeNextStepsField() {
        List<Map<String, Object>> nextSteps = List.of(
                Map.of("tool", "fh_flight_task",
                       "action", "Create a flight task",
                       "params_hint", Map.of("action", "create", "wayline_uuid", "wl_123"))
        );

        Map<String, Object> result = AbstractMcpTool.successResultWithNextSteps(
                Map.of("wayline_uuid", "wl_123"), "Upload complete.", nextSteps);

        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("Upload complete.");
        assertThat(result.get("next_steps")).isEqualTo(nextSteps);
    }

    @Test
    void successResultWithNextSteps_shouldOmitNextStepsWhenEmpty() {
        Map<String, Object> result = AbstractMcpTool.successResultWithNextSteps(
                Map.of("key", "val"), "Done.", List.of());

        assertThat(result).doesNotContainKey("next_steps");
    }

    @Test
    void successResultWithNextSteps_shouldOmitNextStepsWhenNull() {
        Map<String, Object> result = AbstractMcpTool.successResultWithNextSteps(
                Map.of("key", "val"), "Done.", null);

        assertThat(result).doesNotContainKey("next_steps");
    }

    @Test
    void errorResultWithHint_shouldIncludeHintAndNextSteps() {
        List<Map<String, Object>> nextSteps = List.of(
                Map.of("tool", "fh_workspace", "params_hint", Map.of("action", "submit"), "when", "local file")
        );

        Map<String, Object> result = AbstractMcpTool.errorResultWithHint(
                "Missing wayline_uuid",
                "Upload a wayline first.",
                "Local KMZ -> fh_workspace(submit). Public URL -> fh_wayline_upload.",
                nextSteps);

        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error_message")).isEqualTo("Missing wayline_uuid");
        assertThat(result.get("suggestion")).isEqualTo("Upload a wayline first.");
        assertThat(result.get("hint")).isNotNull();
        assertThat(result.get("next_steps")).isEqualTo(nextSteps);
    }

    @Test
    void errorResultWithHint_shouldOmitHintWhenBlank() {
        Map<String, Object> result = AbstractMcpTool.errorResultWithHint(
                "Error", "Suggestion", "   ", null);

        assertThat(result).doesNotContainKey("hint");
        assertThat(result).doesNotContainKey("next_steps");
    }
}
