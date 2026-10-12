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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SDK 时间戳统一转换工具。
 *
 * <p>将 SDK 各接口返回的多种时间格式统一为 Unix 秒（epoch second）。
 * 支持输入格式：null、Unix 秒字符串、Unix 秒 long、Unix 毫秒字符串、Unix 毫秒 long、
 * ISO 8601 字符串、Go time.Time 字符串（"2026-08-27 08:50:47.941669 +0800 CST"）、
 * 纯日期字符串（yyyy-MM-dd）。
 *
 * <p>语义约定：ISO 字符串与裸数字（秒/毫秒）回归到动态时区的系统本地时间；
 * 无时区信息的纯日期按系统时区零点归一化，保证与下游
 * {@code atZone(systemDefault()).toLocalDate()} 互逆。
 *
 * @author DJI DevKit Team
 */
public final class DateTimeUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtils.class);

    private static final long EPOCH_MILLIS_THRESHOLD = 1_000_000_000_000L;

    /** Go time.Time.String() 无时区缩写的输出，如 "2026-08-27 08:50:47.941669 +0800"。 */
    private static final Pattern GO_TIME_WITH_OFFSET = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d{1,9}))? "
                    + "([+-])(\\d{2})(\\d{2})$");

    /** Go time.Time.String() 带时区缩写的输出，如 "2026-08-27 08:50:47.941669 +0800 CST"。 */
    private static final Pattern GO_TIME_WITH_OFFSET_AND_ABBR = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d{1,9}))? "
                    + "([+-])(\\d{2})(\\d{2}) [A-Z]{1,5}$");

    /** Go time.Time.String() 无偏移输出（本地时间），如 "2026-08-27 08:50:47.941669" 或 "2026-08-27 08:50:47"。 */
    private static final Pattern GO_TIME_NO_OFFSET = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})(?:\\.(\\d{1,9}))?$");

    private DateTimeUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 将 SDK 返回的多种时间格式转换为 Unix 秒（epoch second）。
     *
     * 支持以下输入形式：
     * 1. null / 空字符串 → 返回 0L
     * 2. Unix 秒字符串（"1704067200"）
     * 3. Unix 秒 long（1704067200L）
     * 4. Unix 毫秒字符串（"1704067200000"）
     * 5. Unix 毫秒 long（1704067200000L）
     * 6. Go 格式字符串（"2024-01-01 08:00:00 +0800 MST"，由 Go time.Time.String() 产生）
     * 7. ISO 8601 字符串（"2024-01-01T00:00:00Z"、"2024-01-01T00:00:00+08:00"）
     * 8. 纯日期字符串（"2024-01-01"）
     *
     * <p>时区语义：带偏移输入按偏移精确还原到绝对时刻；无偏移输入统一按系统时区归一化，
     * 保证与下游 {@code atZone(systemDefault()).toLocalDate()} 互逆。
     *
     * @param raw 原始值（Object 可为 String、Long 或 null）
     * @return Unix 秒，若输入无效则返回 0L
     */
    public static long toEpochSeconds(Object raw) {
        if (raw == null) {
            return 0L;
        }

        if (raw instanceof Long) {
            long value = (Long) raw;
            // 按阈值判定是秒还是毫秒：>=10^12 视为毫秒，否则为秒
            if (value >= EPOCH_MILLIS_THRESHOLD) {
                return value / 1000;
            }
            return value;
        }

        if (raw instanceof String) {
            String str = ((String) raw).strip();
            if (str.isEmpty()) {
                return 0L;
            }

            // 尝试 1：解析为 long（可能是秒或毫秒字符串）
            try {
                long value = Long.parseLong(str);
                if (value >= EPOCH_MILLIS_THRESHOLD) {
                    return value / 1000;
                }
                return value;
            } catch (NumberFormatException ignored) {
                // 继续尝试其他格式
            }

            // 尝试 2：解析为 ISO 8601 字符串
            try {
                Instant instant = Instant.parse(str);
                return instant.getEpochSecond();
            } catch (DateTimeParseException ignored) {
                // 继续尝试其他格式
            }

            // 尝试 3：解析为 Go time.Time.String() 输出（Go 服务端生成的日期时间格式）
            Long goSeconds = tryParseGoTime(str);
            if (goSeconds != null) {
                return goSeconds;
            }

            // 尝试 4：解析为纯日期字符串（yyyy-MM-dd）。
            // epoch 语义与 ISO 解析、秒级裸数字相同：无时区信息时按系统时区零点
            // 归一化，保证与下游 atZone(systemDefault()).toLocalDate() 互逆。
            try {
                LocalDate date = LocalDate.parse(str);
                return date.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
            } catch (DateTimeParseException ignored) {
                // 格式均不匹配，返回 0
            }

            logger.debug("无法解析时间字符串: {}", str);
            return 0L;
        }

        logger.debug("不支持的时间类型: {}", raw.getClass().getName());
        return 0L;
    }

    /**
     * 解析 Go time.Time.String() 输出为 Unix 秒。
     *
     * <p>支持三种形态：带偏移 + 时区缩写（"… +0800 CST"）、仅带偏移（"… +0800"）、
     * 无偏移本地时间（"…"）。带偏移按偏移精确还原；无偏移按系统时区归一化，与纯日期语义一致。
     *
     * @param str 待解析的 Go 格式时间字符串
     * @return Unix 秒；格式不匹配返回 null
     */
    private static Long tryParseGoTime(String str) {
        Matcher withAbbr = GO_TIME_WITH_OFFSET_AND_ABBR.matcher(str);
        if (withAbbr.matches()) {
            return toEpochSecondsWithSign(withAbbr);
        }
        Matcher withOffset = GO_TIME_WITH_OFFSET.matcher(str);
        if (withOffset.matches()) {
            return toEpochSecondsWithSign(withOffset);
        }
        Matcher noOffset = GO_TIME_NO_OFFSET.matcher(str);
        if (noOffset.matches()) {
            return toEpochSecondsWithSystemZone(noOffset);
        }
        return null;
    }

    /**
     * 将带偏移的 Go 格式解析为绝对时刻的 Unix 秒。
     *
     * @param matcher 已匹配的偏移正则，组 1-7 为日期时间小数秒，组 8-10 为 ±HHMM
     * @return Unix 秒；非法值时返回 0
     */
    private static long toEpochSecondsWithSign(Matcher matcher) {
        try {
            LocalDateTime local = buildLocalDateTime(matcher);
            ZoneOffset offset = buildZoneOffset(matcher);
            return local.toInstant(offset).getEpochSecond();
        } catch (DateTimeException e) {
            return 0L;
        }
    }

    /**
     * 将无偏移的 Go 格式按系统时区归一化为 Unix 秒。
     *
     * @param matcher 已匹配的无偏移正则，组 1-7 为日期时间小数秒
     * @return Unix 秒；非法值时返回 0
     */
    private static long toEpochSecondsWithSystemZone(Matcher matcher) {
        try {
            LocalDateTime local = buildLocalDateTime(matcher);
            return local.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        } catch (DateTimeException e) {
            return 0L;
        }
    }

    /**
     * 从 Go 格式正则的组 1-7 构建本地日期时间。
     *
     * @param matcher 已匹配的 Go 格式正则
     * @return 本地日期时间
     */
    private static LocalDateTime buildLocalDateTime(Matcher matcher) {
        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        int hour = Integer.parseInt(matcher.group(4));
        int minute = Integer.parseInt(matcher.group(5));
        int second = Integer.parseInt(matcher.group(6));
        int nano = parseNanos(matcher.group(7));
        return LocalDateTime.of(
                LocalDate.of(year, month, day),
                LocalTime.of(hour, minute, second, nano));
    }

    /**
     * 从 Go 格式正则的组 8-10 构建时区偏移（±HHMM）。
     *
     * @param matcher 已匹配的 Go 格式正则
     * @return 时区偏移
     */
    private static ZoneOffset buildZoneOffset(Matcher matcher) {
        int sign = "+".equals(matcher.group(8)) ? 1 : -1;
        int hours = Integer.parseInt(matcher.group(9));
        int minutes = Integer.parseInt(matcher.group(10));
        return ZoneOffset.ofHoursMinutes(sign * hours, sign * minutes);
    }

    /**
     * 将 Go 格式的小数秒字符串解析为纳秒值（右补 0 到 9 位）。
     *
     * @param fraction 小数秒串（允许 null）
     * @return 纳秒值，无小数秒返回 0
     */
    private static int parseNanos(String fraction) {
        if (fraction == null || fraction.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(fraction.concat("000000000").substring(0, 9));
    }

    /**
     * 将 Unix 秒转换为 ISO 8601 字符串（UTC 时区，带 Z 后缀）。
     *
     * @param epochSeconds Unix 秒
     * @return ISO 8601 字符串（如 "2024-01-01T00:00:00Z"），0L 或负数返回 ""
     */
    public static String toIsoString(long epochSeconds) {
        if (epochSeconds <= 0) {
            return "";
        }

        try {
            Instant instant = Instant.ofEpochSecond(epochSeconds);
            return instant.toString();
        } catch (Exception e) {
            logger.debug("无法转换时间戳为 ISO 字符串: {}", epochSeconds, e);
            return "";
        }
    }
}
