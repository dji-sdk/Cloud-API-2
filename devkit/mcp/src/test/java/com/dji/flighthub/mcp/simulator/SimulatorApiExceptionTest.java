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

package com.dji.flighthub.mcp.simulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SimulatorApiException} 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class SimulatorApiExceptionTest {

    @Test
    void constructor_shouldStoreHttpStatusAndMessage() {
        SimulatorApiException exception = new SimulatorApiException(404, "/simulator/api/v1/devices/SN001: not found");

        assertEquals(404, exception.getHttpStatus());
        assertEquals("/simulator/api/v1/devices/SN001: not found", exception.getMessage());
    }

    @Test
    void constructor_shouldStoreZeroHttpStatus_whenConnectionFailure() {
        SimulatorApiException exception = new SimulatorApiException(0, "connection failed");

        assertEquals(0, exception.getHttpStatus());
    }

    @Test
    void constructor_shouldStoreCause_whenCauseProvided() {
        Throwable cause = new RuntimeException("original error");

        SimulatorApiException exception = new SimulatorApiException(0, "connection failed", cause);

        assertEquals(cause, exception.getCause());
    }
}
