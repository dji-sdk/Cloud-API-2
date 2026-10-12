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

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ElicitationRegistry 单元测试。
 * 覆盖 register/complete 关联唤醒、超时、无匹配响应、一次性移除等路径。
 *
 * @author DJI FlightHub SDK Team
 */
class ElicitationRegistryTest {

    private static final String REQ_ID = "fh-elicit-1";

    @Test
    void await_shouldReturnResult_whenCompletedInAnotherThread() throws Exception {
        ElicitationRegistry registry = new ElicitationRegistry();
        CompletableFuture<ElicitResult> future = registry.register(REQ_ID);

        // 另一线程模拟 reader 收到 client 响应后唤醒
        Thread completer = new Thread(() -> {
            boolean hit = registry.complete(REQ_ID, ElicitResult.accept(null));
            assertTrue(hit);
        });
        completer.start();
        completer.join();

        ElicitResult result = registry.await(REQ_ID, future, 2_000L);
        assertEquals(ElicitResult.Action.ACCEPT, result.action());
    }

    @Test
    void await_shouldReturnTimeout_whenNoResponse() {
        ElicitationRegistry registry = new ElicitationRegistry();
        CompletableFuture<ElicitResult> future = registry.register(REQ_ID);

        ElicitResult result = registry.await(REQ_ID, future, 50L);

        assertEquals(ElicitResult.Action.TIMEOUT, result.action());
    }

    @Test
    void await_shouldRemovePending_afterCompletion() {
        ElicitationRegistry registry = new ElicitationRegistry();
        CompletableFuture<ElicitResult> future = registry.register(REQ_ID);
        assertTrue(registry.isPending(REQ_ID));

        registry.await(REQ_ID, future, 50L);

        assertFalse(registry.isPending(REQ_ID));
    }

    @Test
    void complete_shouldReturnFalse_whenIdUnknown() {
        ElicitationRegistry registry = new ElicitationRegistry();

        boolean hit = registry.complete("no-such-id", ElicitResult.accept(null));

        assertFalse(hit);
    }

    @Test
    void isPending_shouldReflectRegistration() {
        ElicitationRegistry registry = new ElicitationRegistry();
        assertFalse(registry.isPending(REQ_ID));

        registry.register(REQ_ID);
        assertTrue(registry.isPending(REQ_ID));
    }
}
