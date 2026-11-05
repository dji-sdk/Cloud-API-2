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

package com.dji.flighthub.mcp.tools.ai;

import com.dji.flighthub.mcp.session.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AiSubscriptionBuffer 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class AiSubscriptionBufferTest {

    private AiSubscriptionBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new AiSubscriptionBuffer();
    }

    @Test
    void append_shouldStoreAndRetrieve_whenWithinCapacity() {
        buffer.append("DRONE-001", "ai_data", Map.of("identify_on", 1));
        buffer.append("DRONE-001", "ai_data", Map.of("identify_on", 0));

        AiSubscriptionBuffer.ReadResult result = buffer.read("DRONE-001", "ai_data", 0, 50);

        assertEquals(2, result.events().size());
        assertEquals(1, result.events().get(0).get("identify_on"));
        assertEquals(0, result.events().get(1).get("identify_on"));
        assertEquals(0, result.dropped());
        assertTrue((long) result.events().get(0).get("_seq") < (long) result.events().get(1).get("_seq"));
    }

    @Test
    void read_shouldReturnEmpty_whenNoBucket() {
        AiSubscriptionBuffer.ReadResult result = buffer.read("DRONE-999", "ai_data", 0, 50);

        assertTrue(result.events().isEmpty());
        assertEquals(0, result.dropped());
    }

    @Test
    void read_shouldFilterBySinceSeq_whenPartialRead() {
        buffer.append("DRONE-001", "ai_data", Map.of("v", 1));
        buffer.append("DRONE-001", "ai_data", Map.of("v", 2));
        buffer.append("DRONE-001", "ai_data", Map.of("v", 3));

        AiSubscriptionBuffer.ReadResult first = buffer.read("DRONE-001", "ai_data", 0, 50);
        long lastSeq = (long) first.events().get(1).get("_seq");

        AiSubscriptionBuffer.ReadResult second = buffer.read("DRONE-001", "ai_data", lastSeq, 50);

        assertEquals(1, second.events().size());
        assertEquals(3, second.events().get(0).get("v"));
    }

    @Test
    void append_shouldDropOldest_whenOverCapacity() {
        AiSubscriptionBuffer smallBuffer = new AiSubscriptionBuffer(3);
        smallBuffer.append("D", "k", Map.of("v", 1));
        smallBuffer.append("D", "k", Map.of("v", 2));
        smallBuffer.append("D", "k", Map.of("v", 3));
        smallBuffer.append("D", "k", Map.of("v", 4));

        AiSubscriptionBuffer.ReadResult result = smallBuffer.read("D", "k", 0, 50);

        assertEquals(3, result.events().size());
        assertEquals(2, result.events().get(0).get("v"));
        assertEquals(1, result.dropped());
    }

    @Test
    void read_shouldLimitResults_whenLimitLessThanAvailable() {
        buffer.append("D", "k", Map.of("v", 1));
        buffer.append("D", "k", Map.of("v", 2));
        buffer.append("D", "k", Map.of("v", 3));

        AiSubscriptionBuffer.ReadResult result = buffer.read("D", "k", 0, 2);

        assertEquals(2, result.events().size());
        assertEquals(1, result.events().get(0).get("v"));
        assertEquals(2, result.events().get(1).get("v"));
    }

    @Test
    void remove_shouldClearBucket() {
        buffer.append("D", "k", Map.of("v", 1));
        buffer.remove("D", "k");

        AiSubscriptionBuffer.ReadResult result = buffer.read("D", "k", 0, 50);
        assertTrue(result.events().isEmpty());
    }

    @Test
    void separateBuckets_shouldNotInterfere() {
        buffer.append("D1", "ai_data", Map.of("src", "d1"));
        buffer.append("D1", "alarm", Map.of("src", "alarm"));
        buffer.append("D2", "ai_data", Map.of("src", "d2"));

        assertEquals(1, buffer.read("D1", "ai_data", 0, 50).events().size());
        assertEquals(1, buffer.read("D1", "alarm", 0, 50).events().size());
        assertEquals(1, buffer.read("D2", "ai_data", 0, 50).events().size());
    }

    @Test
    void append_shouldBeThreadSafe_whenConcurrentWrites() throws Exception {
        int writerCount = 4;
        int writesPerThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(writerCount);
        CountDownLatch latch = new CountDownLatch(writerCount);

        for (int w = 0; w < writerCount; w++) {
            int writerId = w;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < writesPerThread; i++) {
                        buffer.append("D", "k", Map.of("w", writerId, "i", i));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        pool.shutdown();

        AiSubscriptionBuffer.ReadResult result = buffer.read("D", "k", 0, 500);
        assertEquals(writerCount * writesPerThread, result.dropped() + result.events().size());
    }

    @Test
    void read_shouldReportTruncated_whenSinceSeqPrecedesEvictedEntries() {
        AiSubscriptionBuffer small = new AiSubscriptionBuffer(2);
        small.append("S", "k", Map.of("v", 1));
        small.append("S", "k", Map.of("v", 2));
        small.append("S", "k", Map.of("v", 3));

        assertTrue(small.read("S", "k", 0, 10).truncated(), "event 1 was evicted after since_seq=0");
        long lastSeq = (long) small.read("S", "k", 0, 10).events().get(1).get("_seq");
        assertFalse(small.read("S", "k", lastSeq - 1, 10).truncated(), "caller already saw the evicted event");
    }

    @Test
    void read_shouldNotReportTruncated_whenNothingEvicted() {
        buffer.append("S", "k", Map.of("v", 1));
        assertFalse(buffer.read("S", "k", 0, 10).truncated());
    }

    @Test
    void read_shouldReturnReadOnlyEntries() {
        buffer.append("S", "k", Map.of("v", 1));
        AiSubscriptionBuffer.ReadResult result = buffer.read("S", "k", 0, 10);

        assertThrows(UnsupportedOperationException.class, () -> result.events().get(0).put("x", 1));
        assertThrows(UnsupportedOperationException.class, () -> result.events().add(Map.of()));
    }

    @Test
    void scope_shouldDifferBetweenSessions_soBucketsDoNotLeak() {
        SessionContext a = new SessionContext("sess_a", "u", "w", 3);
        SessionContext b = new SessionContext("sess_b", "u", "w", 3);
        buffer.append(AiSubscriptionBuffer.scope(a, "SN"), "ai_data", Map.of("v", 1));

        assertEquals(1, buffer.read(AiSubscriptionBuffer.scope(a, "SN"), "ai_data", 0, 10).events().size());
        assertTrue(buffer.read(AiSubscriptionBuffer.scope(b, "SN"), "ai_data", 0, 10).events().isEmpty());
    }
}
