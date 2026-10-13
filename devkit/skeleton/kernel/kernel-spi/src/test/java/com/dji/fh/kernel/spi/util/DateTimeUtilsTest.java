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

package com.dji.fh.kernel.spi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DateTimeUtils} 单元测试。
 */
@DisplayName("DateTimeUtils 时间转换工具")
class DateTimeUtilsTest {

    @Test
    @DisplayName("toEpochSeconds 输入为 null 时返回 0")
    void toEpochSeconds_shouldReturn0_whenInputIsNull() {
        assertEquals(0L, DateTimeUtils.toEpochSeconds(null));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为空字符串时返回 0")
    void toEpochSeconds_shouldReturn0_whenInputIsBlankString() {
        assertEquals(0L, DateTimeUtils.toEpochSeconds(""));
        assertEquals(0L, DateTimeUtils.toEpochSeconds("   "));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Unix 秒字符串时正确解析")
    void toEpochSeconds_shouldReturnCorrectEpochSeconds_whenInputIsUnixSecondsString() {
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds("1704067200"));
        assertEquals(0L, DateTimeUtils.toEpochSeconds("0"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Unix 秒 long 时直接返回")
    void toEpochSeconds_shouldReturnSameValue_whenInputIsUnixSecondsLong() {
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds(1704067200L));
        assertEquals(0L, DateTimeUtils.toEpochSeconds(0L));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Unix 毫秒 long 时自动转换")
    void toEpochSeconds_shouldDivideByThousand_whenInputIsUnixMillisLong() {
        // 1704067200000 毫秒 = 1704067200 秒
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds(1704067200000L));
        // 恰好在阈值上的情况（10^12）
        assertEquals(1000000000L, DateTimeUtils.toEpochSeconds(1_000_000_000_000L));
        // 刚好超过阈值
        assertEquals(1000000001L, DateTimeUtils.toEpochSeconds(1_000_000_001_000L));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Unix 毫秒字符串时自动转换")
    void toEpochSeconds_shouldDivideByThousand_whenInputIsUnixMillisString() {
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds("1704067200000"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 ISO 8601 字符串时正确解析")
    void toEpochSeconds_shouldReturnEpochSeconds_whenInputIsIsoString() {
        // "2024-01-01T00:00:00Z" 的 Unix 秒数
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds("2024-01-01T00:00:00Z"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 ISO 8601 带时区偏移的字符串时正确解析")
    void toEpochSeconds_shouldReturnEpochSeconds_whenInputIsIsoStringWithOffset() {
        // "2024-01-01T08:00:00+08:00" 也对应 "2024-01-01T00:00:00Z"
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds("2024-01-01T08:00:00+08:00"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Go 格式字符串（带时区缩写）时正确解析")
    void toEpochSeconds_shouldReturnEpochSeconds_whenInputIsGoTimeStringWithAbbreviations() {
        // "2026-08-27T00:50:47Z"（即 08:50:47+08:00）参考基准
        long expected = Instant.parse("2026-08-27T00:50:47Z").getEpochSecond();
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2026-08-27 08:50:47 +0800 CST"));
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2026-08-27 08:50:47.000000 +0800 CST"));
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2026-08-27 08:50:47.941669 +0800 CST"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Go 格式字符串（仅带偏移，无时区缩写）时正确解析")
    void toEpochSeconds_shouldReturnEpochSeconds_whenInputIsGoTimeStringWithOffsetOnly() {
        long expected = Instant.parse("2024-01-01T00:00:00Z").getEpochSecond();
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2024-01-01 08:00:00 +0800"));
        // 负偏移：UTC 前一天 22:00-08:00（美西夏时区 -07:00）
        long expectedWest = Instant.parse("2023-12-31T23:00:00Z").getEpochSecond();
        assertEquals(expectedWest, DateTimeUtils.toEpochSeconds("2023-12-31 16:00:00 -0700"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Go 格式字符串（UTC 偏移）时正确解析")
    void toEpochSeconds_shouldReturnEpochSeconds_whenInputIsGoTimeStringWithUtcOffset() {
        long expected = Instant.parse("2024-01-01T00:00:00Z").getEpochSecond();
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2024-01-01 00:00:00 +0000 UTC"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Go 格式字符串（带 UTC 缩写）时正确解析")
    void toEpochSeconds_shouldReturnEpochSeconds_whenInputIsGoTimeStringWithUtcAbbreviation() {
        long expected = Instant.parse("2024-01-01T05:30:00Z").getEpochSecond();
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2024-01-01 11:00:00 +0530 IST"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为 Go 格式字符串（无偏移本地时间）时按系统时区归一化")
    void toEpochSeconds_shouldCombineWithSystemZone_whenInputIsGoTimeStringWithoutOffset() {
        // 无偏移输入：按系统时区解释为本地时间 → epoch
        long expected = LocalDateTime.of(2024, 1, 1, 8, 0, 0)
                .atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2024-01-01 08:00:00"));
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2024-01-01 08:00:00.000000"));
        // 无偏移 + 小数秒：nanos 精确归一化
        long expectedNanos = LocalDateTime.of(2024, 1, 1, 8, 0, 0, 941669000)
                .atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        assertEquals(expectedNanos, DateTimeUtils.toEpochSeconds("2024-01-01 08:00:00.941669"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为带偏移 Go 格式字符串时忽略时区缩写后缀")
    void toEpochSeconds_shouldIgnoreZoneAbbreviation_whenInputIsGoTimeStringWithAbbreviations() {
        // 同一绝对时刻：UTC、CST(+8) 与 IST(+5:30) 缩写不影响偏移解析
        long expectedUtc = Instant.parse("2024-01-01T02:30:00Z").getEpochSecond();
        assertEquals(expectedUtc, DateTimeUtils.toEpochSeconds("2024-01-01 10:30:00 +0800 CST"));
        assertEquals(expectedUtc, DateTimeUtils.toEpochSeconds("2024-01-01 08:00:00 +0530 IST"));

        // 偏移本身是决定时刻的唯一因素：相同偏移不同缩写 → 同一时刻（缩写字符集被正则限定为大写，见 regex）
        long expectedCst = Instant.parse("2024-01-01T02:30:00Z").getEpochSecond();
        assertEquals(expectedCst, DateTimeUtils.toEpochSeconds("2024-01-01 10:30:00 +0800 ACT"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为不符合 Go 格式的时间字符串时返回 0")
    void toEpochSeconds_shouldReturn0_whenInputIsGoTimeStringButInvalid() {
        assertEquals(0L, DateTimeUtils.toEpochSeconds("2024-13-01 08:00:00 +0800 CST")); // 无效月份
        assertEquals(0L, DateTimeUtils.toEpochSeconds("2024-01-01 25:00:00 +0800 CST")); // 无效小时
        assertEquals(0L, DateTimeUtils.toEpochSeconds("2024-01-01 08:00:00 +2580 CST")); // 无效时区偏移
        assertEquals(0L, DateTimeUtils.toEpochSeconds("2024-01-01T08:00:00"));           // ISO 大写的 T 会走 ISO 路径
    }

    @Test
    @DisplayName("toEpochSeconds 输入为纯日期字符串时按系统时区零点归一化（与下游 toLocalDate 互逆）")
    void toEpochSeconds_shouldReturnStartOfDay_whenInputIsPlainDateString() {
        // 系统时区零点 → 该时刻对应本地日期就是原日期（在任何时区下都成立）
        long epoch = DateTimeUtils.toEpochSeconds("2024-01-01");
        LocalDate reversed = Instant.ofEpochSecond(epoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        assertEquals(LocalDate.of(2024, 1, 1), reversed);
    }

    @Test
    @DisplayName("toEpochSeconds 输入为无效字符串时返回 0")
    void toEpochSeconds_shouldReturn0_whenInputIsInvalidString() {
        assertEquals(0L, DateTimeUtils.toEpochSeconds("invalid_string"));
        assertEquals(0L, DateTimeUtils.toEpochSeconds("2024-13-01")); // 无效日期
        assertEquals(0L, DateTimeUtils.toEpochSeconds("not-a-number"));
    }

    @Test
    @DisplayName("toEpochSeconds 输入含前后空白时自动 strip 后正常解析")
    void toEpochSeconds_shouldStripWhitespace_whenInputHasLeadingOrTrailingSpaces() {
        long expected = Instant.parse("2026-08-27T00:50:47Z").getEpochSecond();
        assertEquals(expected, DateTimeUtils.toEpochSeconds("  2026-08-27 08:50:47.941669 +0800 CST  "));
        assertEquals(expected, DateTimeUtils.toEpochSeconds("2026-08-27 08:50:47.941669 +0800 CST\n"));
        assertEquals(expected, DateTimeUtils.toEpochSeconds("\t2026-08-27 08:50:47.941669 +0800 CST\r\n"));
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds("  1704067200  "));
        assertEquals(1704067200L, DateTimeUtils.toEpochSeconds("  2024-01-01T00:00:00Z  "));
    }

    @Test
    @DisplayName("toEpochSeconds 输入为不支持的类型时返回 0")
    void toEpochSeconds_shouldReturn0_whenInputIsUnsupportedType() {
        assertEquals(0L, DateTimeUtils.toEpochSeconds(Double.valueOf(123.45)));
        assertEquals(0L, DateTimeUtils.toEpochSeconds(new Object()));
    }

    @Test
    @DisplayName("toIsoString 输入为有效的 Unix 秒时正确转换")
    void toIsoString_shouldReturnIsoString_whenInputIsValidEpochSeconds() {
        String result = DateTimeUtils.toIsoString(1704067200L);
        assertEquals("2024-01-01T00:00:00Z", result);
    }

    @Test
    @DisplayName("toIsoString 输入为 0 时返回空字符串")
    void toIsoString_shouldReturnEmpty_whenInputIs0() {
        assertEquals("", DateTimeUtils.toIsoString(0L));
    }

    @Test
    @DisplayName("toIsoString 输入为负数时返回空字符串")
    void toIsoString_shouldReturnEmpty_whenInputIsNegative() {
        assertEquals("", DateTimeUtils.toIsoString(-1L));
        assertEquals("", DateTimeUtils.toIsoString(-1704067200L));
    }

    @Test
    @DisplayName("toEpochSeconds 和 toIsoString 互为逆操作")
    void roundTrip_shouldMaintainValue_forValidUnixSeconds() {
        long originalEpochSeconds = 1704067200L;
        String isoString = DateTimeUtils.toIsoString(originalEpochSeconds);
        long parsedEpochSeconds = DateTimeUtils.toEpochSeconds(isoString);
        assertEquals(originalEpochSeconds, parsedEpochSeconds);
    }
}
