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

package com.dji.fh.feature.media.spi.sdk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SdkMediaService} 时间解析逻辑单元测试。
 *
 * <p>通过反射访问私有解析方法，验证前端 yyyy-MM-dd 与 ISO 8601 输入的兼容性，
 * 不依赖 SDK 原生库。
 *
 * @author DJI DevKit Team
 */
@DisplayName("SdkMediaService 时间解析")
class SdkMediaServiceTimeParseTest {

    /** 一天的秒数 */
    private static final long SECONDS_PER_DAY = 86400L;

    /** 被测探针实例：解析方法不依赖构造参数，传 null 即可 */
    private final SdkMediaService probe = new SdkMediaService(null);

    private long parseStart(String time) throws Exception {
        var method = SdkMediaService.class.getDeclaredMethod("parseStartTimeToEpochSeconds", String.class);
        method.setAccessible(true);
        return (Long) method.invoke(probe, time);
    }

    private long parseEnd(String time) throws Exception {
        var method = SdkMediaService.class.getDeclaredMethod("parseEndTimeToEpochSeconds", String.class);
        method.setAccessible(true);
        return (Long) method.invoke(probe, time);
    }

    @Test
    @DisplayName("起始时间输入为 null 或空时返回 0（不限制）")
    void parseStart_shouldReturn0_whenNullOrBlank() throws Exception {
        assertEquals(0L, parseStart(null));
        assertEquals(0L, parseStart(""));
        assertEquals(0L, parseStart("   "));
    }

    @Test
    @DisplayName("结束时间输入为 null 或空时返回 0（不限制）")
    void parseEnd_shouldReturn0_whenNullOrBlank() throws Exception {
        assertEquals(0L, parseEnd(null));
        assertEquals(0L, parseEnd(""));
        assertEquals(0L, parseEnd("   "));
    }

    @Test
    @DisplayName("起始时间为纯日期时归一化到当天零点（系统时区）")
    void parseStart_shouldReturnStartOfDay_whenPlainDate() throws Exception {
        long epoch = parseStart("2026-09-02");
        LocalDate reversed = Instant.ofEpochSecond(epoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        assertEquals(LocalDate.of(2026, 9, 2), reversed);
        assertEquals(0, Instant.ofEpochSecond(epoch).atZone(ZoneId.systemDefault())
                .toLocalTime().toSecondOfDay());
    }

    @Test
    @DisplayName("结束时间为纯日期时归一化到当天 23:59:59（闭区间）")
    void parseEnd_shouldReturnEndOfDay_whenPlainDate() throws Exception {
        long startEpoch = parseStart("2026-09-04");
        long endEpoch = parseEnd("2026-09-04");
        // 与同日零点相差一天减一秒，即 23:59:59
        assertEquals(startEpoch + SECONDS_PER_DAY - 1, endEpoch);
        String hhmmss = Instant.ofEpochSecond(endEpoch)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        assertEquals("23:59:59", hhmmss);
    }

    @Test
    @DisplayName("结束时间为完整 ISO 8601 时原样解析，不再追加当天末尾")
    void parseEnd_shouldParseAsIs_whenIsoString() {
        long expected = Instant.parse("2026-09-04T12:30:00Z").getEpochSecond();
        long actual = assertDoesNotThrow(() -> parseEnd("2026-09-04T12:30:00Z"));
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("起止时间为 ISO 8601 带时区偏移时正确解析")
    void parseBoth_shouldParseIsoWithOffset() throws Exception {
        long expected = Instant.parse("2026-09-04T04:00:00Z").getEpochSecond();
        assertEquals(expected, parseStart("2026-09-04T12:00:00+08:00"));
        assertEquals(expected, parseEnd("2026-09-04T12:00:00+08:00"));
    }

    @Test
    @DisplayName("非法输入时返回 0（不限制），不抛出异常")
    void parseBoth_shouldReturn0_whenInvalidInput() {
        assertTrue(assertDoesNotThrow(() -> parseStart("invalid")) == 0L);
        assertTrue(assertDoesNotThrow(() -> parseEnd("invalid")) == 0L);
        assertTrue(assertDoesNotThrow(() -> parseStart("2024-13-01")) == 0L);
        assertTrue(assertDoesNotThrow(() -> parseEnd("2024-13-01")) == 0L);
    }

    @Test
    @DisplayName("日期区间语义：start=首日零点、end=末日 23:59:59（复现用户 0902~0904 场景）")
    void parseRange_shouldCoverWholeDays_whenPlainDates() throws Exception {
        long start = parseStart("2026-09-02");
        long end = parseEnd("2026-09-04");
        assertTrue(end > start);
        assertEquals(LocalDate.of(2026, 9, 2), Instant.ofEpochSecond(start)
                .atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(LocalDate.of(2026, 9, 4), Instant.ofEpochSecond(end)
                .atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(SECONDS_PER_DAY * 3 - 1, end - start);
    }
}
