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

package com.dji.flighthub.mcp.elicit;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ElicitResult 单元测试。
 * 重点覆盖 fromResponse 的安全默认：无法识别的 action 一律降级为 CANCEL，绝不放行。
 *
 * @author DJI FlightHub SDK Team
 */
class ElicitResultTest {

    @Test
    void fromResponse_shouldReturnAccept_whenActionAccept() {
        ElicitResult result = ElicitResult.fromResponse(
                Map.of("action", "accept", "content", Map.of("lat", 30.0)));

        assertEquals(ElicitResult.Action.ACCEPT, result.action());
        assertTrue(result.isAccepted());
        assertEquals(30.0, result.content().get("lat"));
    }

    @Test
    void fromResponse_shouldReturnAcceptWithEmptyContent_whenContentMissing() {
        ElicitResult result = ElicitResult.fromResponse(Map.of("action", "accept"));

        assertEquals(ElicitResult.Action.ACCEPT, result.action());
        assertTrue(result.content().isEmpty());
    }

    @Test
    void fromResponse_shouldReturnDecline_whenActionDecline() {
        ElicitResult result = ElicitResult.fromResponse(Map.of("action", "decline"));

        assertEquals(ElicitResult.Action.DECLINE, result.action());
        assertFalse(result.isAccepted());
    }

    @Test
    void fromResponse_shouldReturnCancel_whenActionCancel() {
        ElicitResult result = ElicitResult.fromResponse(Map.of("action", "cancel"));

        assertEquals(ElicitResult.Action.CANCEL, result.action());
    }

    @Test
    void fromResponse_shouldReturnCancel_whenActionUnknown() {
        // 安全红线：未知 action 绝不放行
        ElicitResult result = ElicitResult.fromResponse(Map.of("action", "explode"));

        assertEquals(ElicitResult.Action.CANCEL, result.action());
        assertFalse(result.isAccepted());
    }

    @Test
    void fromResponse_shouldReturnCancel_whenResultNull() {
        ElicitResult result = ElicitResult.fromResponse(null);

        assertEquals(ElicitResult.Action.CANCEL, result.action());
    }

    @Test
    void fromResponse_shouldReturnCancel_whenActionMissing() {
        ElicitResult result = ElicitResult.fromResponse(Map.of("content", Map.of()));

        assertEquals(ElicitResult.Action.CANCEL, result.action());
    }

    @Test
    void fromResponse_shouldBeCaseInsensitive_whenActionUpperCase() {
        ElicitResult result = ElicitResult.fromResponse(Map.of("action", "ACCEPT"));

        assertEquals(ElicitResult.Action.ACCEPT, result.action());
    }

    @Test
    void factoryMethods_shouldProduceExpectedActions() {
        assertEquals(ElicitResult.Action.ACCEPT, ElicitResult.accept(null).action());
        assertEquals(ElicitResult.Action.DECLINE, ElicitResult.decline().action());
        assertEquals(ElicitResult.Action.CANCEL, ElicitResult.cancel().action());
        assertEquals(ElicitResult.Action.TIMEOUT, ElicitResult.timeout().action());
        assertNotNull(ElicitResult.accept(null).content());
    }
}
