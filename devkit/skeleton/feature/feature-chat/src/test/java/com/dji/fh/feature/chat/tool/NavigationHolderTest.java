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

package com.dji.fh.feature.chat.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link NavigationHolder} 单元测试。
 *
 * @author DJI DevKit Team
 */
class NavigationHolderTest {

    private static final String REQUEST_ID = "nav-test-001";

    @BeforeEach
    void setUp() {
        ToolCallContext.CURRENT_REQUEST_ID.set(REQUEST_ID);
    }

    @AfterEach
    void tearDown() {
        NavigationHolder.clear(REQUEST_ID);
        ToolCallContext.CURRENT_REQUEST_ID.remove();
    }

    @Test
    void take_shouldReturnNull_whenNothingOffered() {
        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    @Test
    void take_shouldReturnPayload_afterOffer() {
        Map<String, String> query = Map.of("drone_sn", "DRONE-001");
        NavigationHolder.offer("/cockpit", query);

        NavigationHolder.Payload payload = NavigationHolder.take(REQUEST_ID);
        assertThat(payload).isNotNull();
        assertThat(payload.getPath()).isEqualTo("/cockpit");
        assertThat(payload.getQuery()).containsEntry("drone_sn", "DRONE-001");
    }

    @Test
    void take_shouldClearAfterFirstCall() {
        NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-001"));

        NavigationHolder.take(REQUEST_ID);
        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    @Test
    void offer_shouldOverwritePreviousPayload() {
        NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-001"));
        NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-002"));

        NavigationHolder.Payload payload = NavigationHolder.take(REQUEST_ID);
        assertThat(payload).isNotNull();
        assertThat(payload.getQuery()).containsEntry("drone_sn", "DRONE-002");
    }

    @Test
    void clear_shouldRemovePayload() {
        NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-001"));
        NavigationHolder.clear(REQUEST_ID);

        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    @Test
    void offer_shouldDoNothing_whenNoRequestIdBound() {
        ToolCallContext.CURRENT_REQUEST_ID.remove();

        NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-001"));

        assertThat(NavigationHolder.take(REQUEST_ID)).isNull();
    }

    @Test
    void concurrentRequests_shouldIsolateNavigation() throws InterruptedException {
        String reqA = "req-A";
        String reqB = "req-B";
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(2);

        Thread threadA = new Thread(() -> {
            ToolCallContext.CURRENT_REQUEST_ID.set(reqA);
            NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-A"));
            ToolCallContext.CURRENT_REQUEST_ID.remove();
            latch.countDown();
        });

        Thread threadB = new Thread(() -> {
            ToolCallContext.CURRENT_REQUEST_ID.set(reqB);
            NavigationHolder.offer("/cockpit", Map.of("drone_sn", "DRONE-B"));
            ToolCallContext.CURRENT_REQUEST_ID.remove();
            latch.countDown();
        });

        threadA.start();
        threadB.start();
        assertThat(latch.await(2, java.util.concurrent.TimeUnit.SECONDS)).isTrue();

        NavigationHolder.Payload payloadA = NavigationHolder.take(reqA);
        NavigationHolder.Payload payloadB = NavigationHolder.take(reqB);
        assertThat(payloadA).isNotNull();
        assertThat(payloadA.getQuery()).containsEntry("drone_sn", "DRONE-A");
        assertThat(payloadB).isNotNull();
        assertThat(payloadB.getQuery()).containsEntry("drone_sn", "DRONE-B");
    }
}
