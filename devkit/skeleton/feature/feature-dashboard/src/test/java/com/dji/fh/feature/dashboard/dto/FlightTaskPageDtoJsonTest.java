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

package com.dji.fh.feature.dashboard.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 飞行任务列表 DTO 的 JSON 字段名契约测试。
 *
 * <p>前端按 camelCase 直接消费该结构，需锁定 record 组件名原样输出，
 * 尤其是 boolean 组件 {@code isConflicted} 是否保留 is 前缀
 * （Jackson 对 {@code isXxx} 命名存在派生改名风险）。
 *
 * @author DJI DevKit Team
 */
class FlightTaskPageDtoJsonTest {

    @Test
    void serialize_shouldKeepComponentNames_whenListSerialized() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FlightTaskListItemDto item = new FlightTaskListItemDto(
                "test-uuid", "测试任务", "SN001", "CREATED", 2, "wl-001",
                1700000000L, 1200.0, 30, true, true);
        FlightTaskPageDto page = new FlightTaskPageDto(List.of(item), 1, false);

        JsonNode node = mapper.readTree(mapper.writeValueAsString(page));

        assertThat(node.has("tasks")).isTrue();
        assertThat(node.has("totalCount")).isTrue();
        assertThat(node.has("hasMore")).isTrue();

        JsonNode itemNode = node.get("tasks").get(0);
        assertThat(itemNode.has("taskUuid")).isTrue();
        assertThat(itemNode.has("name")).isTrue();
        assertThat(itemNode.has("sn")).isTrue();
        assertThat(itemNode.has("status")).isTrue();
        assertThat(itemNode.has("taskType")).isTrue();
        assertThat(itemNode.has("waylineUuid")).isTrue();
        assertThat(itemNode.has("createdAt")).isTrue();
        assertThat(itemNode.has("distanceMeters")).isTrue();
        assertThat(itemNode.has("minBatteryCapacity")).isTrue();
        assertThat(itemNode.get("minBatteryCapacity").asInt()).isEqualTo(30);
        assertThat(itemNode.has("isConflicted")).isTrue();
        assertThat(itemNode.get("isConflicted").asBoolean()).isTrue();
        assertThat(itemNode.has("isBreakPointResume")).isTrue();
        assertThat(itemNode.get("isBreakPointResume").asBoolean()).isTrue();
    }
}
