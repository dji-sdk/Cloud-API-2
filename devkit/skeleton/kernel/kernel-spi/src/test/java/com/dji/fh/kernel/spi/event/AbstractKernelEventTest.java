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

package com.dji.fh.kernel.spi.event;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class AbstractKernelEventTest {

    private static final class TestEvent extends AbstractKernelEvent {
        TestEvent(String source) {
            super(source);
        }
    }

    @Test
    void eventId_shouldBeUnique_whenMultipleInstancesCreated() {
        TestEvent a = new TestEvent("test");
        TestEvent b = new TestEvent("test");

        assertThat(a.eventId()).isNotBlank();
        assertThat(a.eventId()).isNotEqualTo(b.eventId());
    }

    @Test
    void occurredAt_shouldBeNearNow_whenConstructed() {
        Instant before = Instant.now();
        TestEvent e = new TestEvent("test");
        Instant after = Instant.now();

        assertThat(e.occurredAt()).isBetween(before, after);
    }

    @Test
    void source_shouldReturnConstructorArgument_whenAccessed() {
        TestEvent e = new TestEvent("kernel-sdk");

        assertThat(e.source()).isEqualTo("kernel-sdk");
    }

    @Test
    void eventId_shouldBeValidUuid_whenAccessed() {
        TestEvent e = new TestEvent("test");

        assertThatCode(() -> UUID.fromString(e.eventId())).doesNotThrowAnyException();
    }

    @Test
    void constructor_shouldThrowNPE_whenSourceIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new TestEvent(null))
                .withMessageContaining("source");
    }
}
