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

package com.dji.fh.kernel.impl.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * JpaAppendLogAdapter 集成测试。
 *
 * @author DJI DevKit Team
 */
@DataJpaTest
@EntityScan(basePackageClasses = JpaAppendLogAdapterTest.LogEntry.class)
@Import(JpaAppendLogAdapterTest.TestConfig.class)
class JpaAppendLogAdapterTest {

    @Autowired
    private LogEntryStore store;

    @jakarta.persistence.PersistenceContext
    private EntityManager em;

    @Test
    void append_shouldPersist_whenSingleEntry() {
        store.append(new LogEntry("hello", Instant.now()));

        assertThat(store.count()).isEqualTo(1L);
    }

    @Test
    void readSince_shouldReturnAllAtOrAfter_whenStartGiven() {
        Instant t0 = Instant.parse("2026-06-26T00:00:00Z");
        store.append(new LogEntry("a", t0));
        store.append(new LogEntry("b", t0.plus(1, ChronoUnit.MINUTES)));
        store.append(new LogEntry("c", t0.plus(2, ChronoUnit.MINUTES)));

        try (Stream<LogEntry> s = store.readSince(t0.plus(1, ChronoUnit.MINUTES))) {
            List<String> messages = s.map(LogEntry::getMessage).toList();
            assertThat(messages).containsExactly("b", "c");
        }
    }

    @Test
    void readBetween_shouldReturnRangeHalfOpen_whenBothBoundsGiven() {
        Instant t0 = Instant.parse("2026-06-26T00:00:00Z");
        store.append(new LogEntry("a", t0));
        store.append(new LogEntry("b", t0.plus(1, ChronoUnit.MINUTES)));
        store.append(new LogEntry("c", t0.plus(2, ChronoUnit.MINUTES)));
        store.append(new LogEntry("d", t0.plus(3, ChronoUnit.MINUTES)));

        try (Stream<LogEntry> s = store.readBetween(
                t0.plus(1, ChronoUnit.MINUTES),
                t0.plus(3, ChronoUnit.MINUTES))) {
            List<String> messages = s.map(LogEntry::getMessage).toList();
            // 半开区间 [from, to) → 含 b、c，不含 d
            assertThat(messages).containsExactly("b", "c");
        }
    }

    @Test
    void readBetween_shouldRejectInvertedRange() {
        Instant t0 = Instant.parse("2026-06-26T00:00:00Z");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> store.readBetween(
                        t0.plus(1, ChronoUnit.MINUTES), t0));
    }

    @Test
    void count_shouldStartAtZero_whenEmpty() {
        assertThat(store.count()).isZero();
    }

    @Test
    void constructor_shouldRejectIllegalTimestampFieldName() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JpaAppendLogAdapter<LogEntry>(
                        em, LogEntry.class, "createdAt; DROP TABLE x") {})
                .withMessageContaining("合法 Java 标识符");
    }

    @Test
    void constructor_shouldRejectBlankTimestampFieldName() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JpaAppendLogAdapter<LogEntry>(
                        em, LogEntry.class, "   ") {})
                .withMessageContaining("不能为空白");
    }

    /**
     * 测试用日志实体。
     */
    @Entity(name = "LogEntry")
    public static class LogEntry {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String message;

        private Instant createdAt;

        public LogEntry() {
        }

        public LogEntry(String message, Instant createdAt) {
            this.message = message;
            this.createdAt = createdAt;
        }

        public String getMessage() {
            return message;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }
    }

    /**
     * LogEntry 的 AppendLog 实现。
     */
    public static class LogEntryStore extends JpaAppendLogAdapter<LogEntry> {
        public LogEntryStore(EntityManager em) {
            super(em, LogEntry.class, "createdAt");
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        LogEntryStore logEntryStore(EntityManager em) {
            return new LogEntryStore(em);
        }
    }
}
