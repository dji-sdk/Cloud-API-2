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

package com.dji.fh.feature.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService.FlightTaskDetailDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * FlightTaskDetailDto JSON 序列化契约测试。
 *
 * <p>前端编辑预填依赖该 DTO 的字段名，需锁定 record 组件名原样输出，
 * 尤其是 boolean 组件 {@code isBreakPointResume} 是否保留 is 前缀
 * （Jackson 对 {@code isXxx} 命名存在派生改名风险）。
 *
 * @author DJI DevKit Team
 */
class FlightTaskDetailDtoJsonTest {

    @Test
    void serialize_shouldKeepComponentNames_whenRecordSerialized() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FlightTaskDetailDto dto = new FlightTaskDetailDto(
                "test-uuid", "测试任务", "SN001", "测试机场",
                "CREATED", 2, "wl-001", "测试航线",
                1700000000L, 1700003600L,
                100.0, 0, 1, 1, 0, false);

        JsonNode node = mapper.readTree(mapper.writeValueAsString(dto));

        assertThat(node.has("taskUuid")).isTrue();
        assertThat(node.has("name")).isTrue();
        assertThat(node.has("sn")).isTrue();
        // deviceName / waylineName 是编辑回显的关键字段，后端反查补齐后必须原样输出
        assertThat(node.has("deviceName")).isTrue();
        assertThat(node.get("deviceName").asText()).isEqualTo("测试机场");
        assertThat(node.has("status")).isTrue();
        assertThat(node.has("taskType")).isTrue();
        assertThat(node.has("waylineUuid")).isTrue();
        assertThat(node.has("waylineName")).isTrue();
        assertThat(node.get("waylineName").asText()).isEqualTo("测试航线");
        assertThat(node.has("beginAt")).isTrue();
        assertThat(node.has("endAt")).isTrue();
        assertThat(node.has("rthAltitude")).isTrue();
        assertThat(node.has("rthMode")).isTrue();
        assertThat(node.has("outOfControlActionInFlight")).isTrue();
        assertThat(node.has("waylinePrecisionType")).isTrue();
        assertThat(node.has("minBatteryCapacity")).isTrue();
        assertThat(node.has("isBreakPointResume")).isTrue();
    }
}
