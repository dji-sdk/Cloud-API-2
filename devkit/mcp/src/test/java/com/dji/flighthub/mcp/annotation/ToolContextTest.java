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

import com.dji.flighthub.mcp.session.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ToolContextTest {

    @AfterEach
    void cleanup() {
        ToolContext.clear();
    }

    @Test
    void init_shouldSetCurrentContext() {
        Map<String, Object> args = Map.of("drone_sn", "DRONE-001", "height", 50.0);
        SessionContext session = new SessionContext("s1", "ws-uuid", "test-ws", 3);

        ToolContext.init(args, session);

        assertNotNull(ToolContext.current());
        assertEquals("s1", ToolContext.current().getSession().getSessionId());
    }

    @Test
    void current_shouldReturnNull_whenNotInitialized() {
        assertNull(ToolContext.current());
    }

    @Test
    void clear_shouldRemoveContext() {
        ToolContext.init(Map.of("key", "val"), null);
        assertNotNull(ToolContext.current());

        ToolContext.clear();
        assertNull(ToolContext.current());
    }

    @Test
    void getString_shouldReturnValue() {
        ToolContext.init(Map.of("name", "test"), null);
        assertEquals("test", ToolContext.current().getString("name"));
    }

    @Test
    void getString_shouldReturnFallback_whenMissing() {
        ToolContext.init(Map.of(), null);
        assertEquals("default", ToolContext.current().getString("missing", "default"));
    }

    @Test
    void getDouble_shouldReturnValue() {
        ToolContext.init(Map.of("lat", 22.5), null);
        assertEquals(22.5, ToolContext.current().getDouble("lat"));
    }

    @Test
    void getDouble_shouldReturnNull_whenMissing() {
        ToolContext.init(Map.of(), null);
        assertNull(ToolContext.current().getDouble("missing"));
    }

    @Test
    void getInt_shouldConvertFromDouble() {
        ToolContext.init(Map.of("speed", 15.0), null);
        assertEquals(15, ToolContext.current().getInt("speed"));
    }

    @Test
    void getBoolean_shouldReturnValue() {
        ToolContext.init(Map.of("confirm", true), null);
        assertTrue(ToolContext.current().getBoolean("confirm"));
    }

    @Test
    void getBoolean_shouldReturnFalse_whenMissing() {
        ToolContext.init(Map.of(), null);
        assertFalse(ToolContext.current().getBoolean("missing"));
    }

    @Test
    void threadIsolation_shouldNotLeakBetweenThreads() throws Exception {
        ToolContext.init(Map.of("thread", "main"), null);

        Thread other = new Thread(() -> {
            assertNull(ToolContext.current(), "Should not see main thread's context");
        });
        other.start();
        other.join();

        assertEquals("main", ToolContext.current().getString("thread"));
    }
}
