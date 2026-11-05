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

package com.dji.flighthub.mcp.ratelimit;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.security.SecurityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * 工具级限流器 — 按 {@code (sessionId, toolName, deviceSn[, exemptAction])} 维度的均匀漏桶。
 *
 * <p>设计目标：阻断大模型逻辑死循环在一分钟内对同一高成本 Tool 的上千次调用。
 * 与 {@code RateLimitWebFilter}（SSE 模式按客户端 IP 的窗口限流）是两层独立防线：
 * 前者防单 Agent 行为失控，后者防外部接口被刷。
 *
 * <p>放行模型：<b>只拒绝不排队</b>。每个键只记录「最近一次放行时间」，
 * 两次放行之间的最小间隔为 {@code 60000 / rpm} 毫秒。超限时立即返回
 * {@code retry_after_ms}，不阻塞调用线程（否则死循环会演变成线程池耗尽）。
 *
 * <p>配额优先级：单工具覆盖 {@code per-tool-rpm} &gt; {@link SecurityLevel} 分层默认值。
 * {@code rpm <= 0} 视为不限流（不创建桶），因此 L3 紧急工具默认永远可执行。
 *
 * <p>计数键包含目标设备序列号（{@code drone_sn} / {@code dock_sn} / {@code device_sn} 取第一个非空值），
 * 避免同一 session 下多台设备共用配额互相挤占；参数中无设备标识的只读工具仍退化为
 * {@code sessionId|toolName}。
 *
 * <p><b>中止 / 查询类 action 的特殊规则</b>：{@code cancel}、{@code stop}、{@code progress}、
 * {@code quality} 跳过 {@code per-tool-rpm} 覆盖、只走 {@link SecurityLevel} 档位配额，
 * 并额外使用带 action 后缀的<b>独立桶</b>。这两个处理缺一不可：
 * 一是不让昂贵的启动配额误伤中止操作（{@code fh_return_home} 的 2 rpm 会把 cancel 拦下）；
 * 二是桶的最小间隔在创建时固化，若与 start 共用桶，先 start 后 cancel 仍按启动配额判定。
 * 独立桶后，同一工具+设备上的中止调用不受之前的启动调用影响，且仍受档位配额约束，
 * 不会成为不限流的后门。详见 {@link #resolveRpm(String, SecurityLevel, String)}。
 *
 * <p>失败策略：限流是安全增强而非可用性单点 —— 键空间耗尽或内部异常一律
 * <b>放行（fail-open）</b>并告警，绝不因限流器自身问题阻断工具调用。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class ToolRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(ToolRateLimiter.class);

    /** 每分钟毫秒数，用于把 rpm 换算成最小放行间隔 */
    private static final long MILLIS_PER_MINUTE = 60_000L;

    /** 清理周期（每 N 次请求顺带清理过期桶，与 RateLimitWebFilter 保持一致） */
    private static final int CLEANUP_INTERVAL = 256;

    /** 每毫秒纳秒数，用于把单调时钟折算成毫秒 */
    private static final long NANOS_PER_MILLI = 1_000_000L;

    /** 键分隔符，形如 {@code sessionId|toolName|deviceSn|action} */
    private static final char KEY_SEPARATOR = '|';

    /**
     * 限流精度上限：最小放行间隔按毫秒计时，故 rpm 超过 60000 时无法再细化，
     * 实际生效配额会被钳到 60000/min。
     */
    private static final int MAX_EFFECTIVE_RPM = (int) MILLIS_PER_MINUTE;

    /** {@code maxKeys} 配置非法时的兜底值，与 {@code RateLimit.maxKeys} 默认值保持一致 */
    private static final int DEFAULT_MAX_KEYS = 10_000;

    /** 容量告警最小间隔（毫秒），避免死循环满载时每次调用都刷一条 warn */
    private static final long CAPACITY_WARN_INTERVAL_MS = 60_000L;

    /** 动作参数名，仓库内所有 action 型工具统一使用该名称 */
    private static final String PARAM_ACTION = "action";

    /**
     * 设备序列号参数名，按优先级依次尝试。
     * 起飞类工具同时带 {@code drone_sn} 与 {@code dock_sn}，取第一个非空值即可定位「哪台设备」。
     */
    private static final List<String> DEVICE_SN_PARAMS = List.of("drone_sn", "dock_sn", "device_sn");

    /**
     * 中止 / 查询类 action：跳过 per-tool 覆盖，只走档位配额并使用独立桶。
     *
     * <p>覆盖仓库内相关工具的动作词表：返航 {@code cancel}、飞向 / 图传 / POI {@code stop}、
     * 飞向 {@code progress}、图传 {@code quality}。
     */
    private static final Set<String> EXEMPT_ACTIONS = Set.of("cancel", "stop", "progress", "quality");

    private final boolean enabled;
    private final int readOnlyRpm;
    private final int reversibleRpm;
    private final int flightRpm;
    private final int criticalRpm;
    private final Map<String, Integer> perToolRpm;
    private final int maxKeys;

    /** 可注入时钟，便于单测在不 sleep 的前提下推进时间 */
    private final LongSupplier clock;

    /** 键（sessionId|toolName|deviceSn[|action]）→ 漏桶 */
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /** 请求计数器，驱动周期性清理 */
    private final AtomicLong requestCount = new AtomicLong(0);

    /** 上次容量告警的毫秒时间，0 表示尚未告警 */
    private final AtomicLong lastCapacityWarnAt = new AtomicLong(0);

    /**
     * Spring 构造器 — 时钟使用单调毫秒时间。
     *
     * <p>必须用 {@link System#nanoTime()} 而非 {@link System#currentTimeMillis()}：
     * 后者的 NTP 回拨会让 {@code elapsed} 变成负数，导致桶被永久判定为「间隔未到」，
     * {@code retry_after_ms} 被放大到回拨的时长。本类只关心时间差，不关心绝对时刻。
     *
     * @param properties FlightHub 配置（读取 {@code flighthub.rate-limit.*}）
     */
    @Autowired
    public ToolRateLimiter(FlightHubProperties properties) {
        this(properties, () -> System.nanoTime() / NANOS_PER_MILLI);
    }

    /**
     * 测试专用构造器 — 允许注入可控时钟。
     *
     * @param properties FlightHub 配置
     * @param clock      毫秒时间源
     */
    public ToolRateLimiter(FlightHubProperties properties, LongSupplier clock) {
        FlightHubProperties.RateLimit config = properties.getRateLimit();
        this.enabled = config.isEnabled();
        this.readOnlyRpm = config.getReadOnlyRpm();
        this.reversibleRpm = config.getReversibleRpm();
        this.flightRpm = config.getFlightRpm();
        this.criticalRpm = config.getCriticalRpm();
        this.perToolRpm = config.getPerToolRpm() != null ? config.getPerToolRpm() : Map.of();
        this.maxKeys = resolveMaxKeys(config.getMaxKeys());
        this.clock = clock;

        log.info("ToolRateLimiter 启动: enabled={}, L0={}/min, L1={}/min, L2={}/min, L3={}/min, overrides={}, maxKeys={}",
                enabled, readOnlyRpm, reversibleRpm, flightRpm, criticalRpm, perToolRpm, maxKeys);

        warnIfRpmExceedsPrecision("read-only-rpm", readOnlyRpm);
        warnIfRpmExceedsPrecision("reversible-rpm", reversibleRpm);
        warnIfRpmExceedsPrecision("flight-rpm", flightRpm);
        warnIfRpmExceedsPrecision("critical-rpm", criticalRpm);
        perToolRpm.forEach((tool, rpm) -> warnIfRpmExceedsPrecision("per-tool-rpm[" + tool + "]", rpm));
    }

    /**
     * 校验并归一化 {@code max-keys}。
     *
     * <p>{@code maxKeys <= 0} 会让容量保护恒成立（{@code size() >= 0}），限流器将<b>整体失效</b>
     * 却仍在启动日志里显示「限流已启用」—— 这是配置错误而非功能取舍，必须纠正。
     * 这里回落默认值并告警，而不是抛异常让服务起不来：限流器本身不应成为启动故障点。
     *
     * @param configured 配置值
     * @return 生效的键空间上限，恒为正数
     */
    private static int resolveMaxKeys(int configured) {
        if (configured <= 0) {
            log.warn("max-keys 配置非法({}), 回落默认值 {} —— 非正值会让容量保护恒成立、限流整体失效",
                    configured, DEFAULT_MAX_KEYS);
            return DEFAULT_MAX_KEYS;
        }
        return configured;
    }

    /**
     * 毫秒精度下 rpm 超过 {@value #MAX_EFFECTIVE_RPM} 会被钳位，启动时提示配置者。
     *
     * @param source 配置来源（便于定位是哪一项写错了）
     * @param rpm    配置的每分钟配额
     */
    private static void warnIfRpmExceedsPrecision(String source, int rpm) {
        if (rpm > MAX_EFFECTIVE_RPM) {
            log.warn("限流配额超出毫秒精度上限, 实际按 {}/min 生效: {}={}",
                    MAX_EFFECTIVE_RPM, source, rpm);
        }
    }

    /**
     * 尝试为一次工具调用获取许可。
     *
     * @param sessionId 当前 session ID；stdio 模式可能为 null，此时退化为仅按 toolName 计数
     * @param toolName  工具名（如 "fh_takeoff"）
     * @param level     工具的安全等级，null 时按 L0 处理
     * @param args      本次调用参数，用于提取设备序列号与 action；null 视为无参
     * @return 放行决策，包含是否放行、需等待毫秒数与生效配额
     */
    public Decision tryAcquire(String sessionId, String toolName, SecurityLevel level,
                               Map<String, Object> args) {
        if (!enabled) {
            return Decision.allow(0);
        }

        try {
            String action = resolveExemptAction(args);
            int rpm = resolveRpm(toolName, level, action);
            long minIntervalMs = toIntervalMs(rpm);
            // rpm <= 0（含 L3 紧急工具）：不限流，不创建桶
            if (minIntervalMs <= 0L) {
                return Decision.allow(rpm);
            }

            long now = clock.getAsLong();
            cleanupIfNeeded(now);

            String key = buildKey(sessionId, toolName, resolveDeviceSn(args), action);
            Bucket bucket = buckets.get(key);
            if (bucket == null) {
                // 容量保护：超限后 fail-open，避免伪造 sessionId / 设备号撑爆内存
                if (buckets.size() >= maxKeys) {
                    warnCapacityExceeded(now, key);
                    return Decision.allow(rpm);
                }
                bucket = buckets.computeIfAbsent(key, k -> new Bucket(minIntervalMs));
            }

            long retryAfterMs = bucket.tryAcquire(now);
            if (retryAfterMs > 0L) {
                log.warn("工具调用被限流: key={}, limit={}/min, retry_after_ms={}",
                        key, rpm, retryAfterMs);
                return Decision.reject(retryAfterMs, rpm);
            }
            return Decision.allow(rpm);
        } catch (Exception e) {
            // fail-open：限流器自身异常不得阻断工具调用
            log.warn("限流检查异常, 本次放行: tool={}, error={}", toolName, e.getMessage());
            return Decision.allow(0);
        }
    }

    /**
     * 提取设备序列号。
     *
     * <p>仓库内存在三种参数名（{@code drone_sn} / {@code dock_sn} / {@code device_sn}），
     * 按 {@link #DEVICE_SN_PARAMS} 的顺序取第一个非空值；不含设备参数的只读工具返回 null，
     * 限流键退化为 {@code sessionId|toolName}。
     *
     * @param args 本次调用参数
     * @return 设备序列号，无设备标识时返回 null
     */
    private static String resolveDeviceSn(Map<String, Object> args) {
        if (args == null) {
            return null;
        }
        for (String param : DEVICE_SN_PARAMS) {
            Object value = args.get(param);
            if (value instanceof String sn && !sn.isBlank()) {
                return sn;
            }
        }
        return null;
    }

    /**
     * 判断本次调用是否是中止 / 查询类 action。
     *
     * @param args 本次调用参数
     * @return 命中 {@link #EXEMPT_ACTIONS} 的小写 action，否则返回 null
     */
    private static String resolveExemptAction(Map<String, Object> args) {
        if (args == null) {
            return null;
        }
        Object raw = args.get(PARAM_ACTION);
        if (!(raw instanceof String action)) {
            return null;
        }
        String normalized = action.trim().toLowerCase(Locale.ROOT);
        return EXEMPT_ACTIONS.contains(normalized) ? normalized : null;
    }

    /**
     * 解析工具生效的每分钟配额。
     *
     * @param toolName 工具名
     * @param level    安全等级
     * @param action   中止 / 查询类 action；非 null 时跳过 per-tool 覆盖
     * @return 每分钟允许调用次数，0 表示不限流
     */
    private int resolveRpm(String toolName, SecurityLevel level, String action) {
        if (action == null) {
            Integer override = perToolRpm.get(toolName);
            if (override != null) {
                return override;
            }
        }
        if (level == null) {
            return readOnlyRpm;
        }
        // 刻意不写 default：新增 SecurityLevel 常量时这里编译失败，强制显式决定档位。
        // 若保留 default 分支，新档位会静默落到 L0 的宽松配额上（fail-open）。
        return switch (level) {
            case READ_ONLY -> readOnlyRpm;
            case REVERSIBLE -> reversibleRpm;
            case FLIGHT -> flightRpm;
            case CRITICAL -> criticalRpm;
        };
    }

    /**
     * 把每分钟配额换算为最小放行间隔。
     *
     * @param rpm 每分钟调用次数
     * @return 最小间隔毫秒数，rpm &lt;= 0 时返回 0（不限流）；其余情况至少 1ms
     */
    private static long toIntervalMs(int rpm) {
        if (rpm <= 0) {
            return 0L;
        }
        // 整数除法：rpm > 60000 时商为 0，会被下游误判成「不限流」而静默放宽；
        // 这里钳到 1ms（等价于 60000 rpm 的精度上限），并由构造器统一告警。
        return Math.max(1L, MILLIS_PER_MINUTE / rpm);
    }

    /**
     * 构建限流键：{@code sessionId|toolName[|deviceSn][|action]}。
     *
     * <p>无 session 时省略 sessionId 段（stdio 模式），无设备标识时省略设备段，
     * 非中止 / 查询类 action 省略 action 段 —— 保证存量工具与既有测试的键形状不变。
     *
     * @param sessionId session ID，可为 null 或空
     * @param toolName  工具名
     * @param deviceSn  设备序列号，可为 null
     * @param action    中止 / 查询类 action，可为 null
     * @return 限流键
     */
    private static String buildKey(String sessionId, String toolName, String deviceSn, String action) {
        StringBuilder key = new StringBuilder(64);
        if (sessionId != null && !sessionId.isBlank()) {
            key.append(sessionId).append(KEY_SEPARATOR);
        }
        key.append(toolName);
        if (deviceSn != null) {
            key.append(KEY_SEPARATOR).append(deviceSn);
        }
        if (action != null) {
            key.append(KEY_SEPARATOR).append(action);
        }
        return key.toString();
    }

    /**
     * 容量告警限频：满载时每次调用都会走到这里，不节流会把日志刷爆。
     *
     * @param now 当前毫秒时间
     * @param key 本次未能建桶的键
     */
    private void warnCapacityExceeded(long now, String key) {
        long lastWarnAt = lastCapacityWarnAt.get();
        if (lastWarnAt != 0L && now - lastWarnAt < CAPACITY_WARN_INTERVAL_MS) {
            return;
        }
        if (lastCapacityWarnAt.compareAndSet(lastWarnAt, now)) {
            log.warn("限流桶已达上限({}), 本次及后续新键放行不做限流: key={}", maxKeys, key);
        }
    }

    /**
     * 每 {@value #CLEANUP_INTERVAL} 次请求清理一次长期空闲的桶，避免额外的定时线程。
     *
     * @param now 当前毫秒时间
     */
    private void cleanupIfNeeded(long now) {
        if (requestCount.incrementAndGet() % CLEANUP_INTERVAL != 0) {
            return;
        }
        buckets.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    /**
     * 单个限流键 {@code (session, tool, device, action)} 的漏桶状态。
     *
     * <p>采用「最近一次放行时间 + 最小间隔」实现均匀漏桶，无需保存队列或令牌，
     * 内存占用为 O(桶数)。临界区只有几个字段读写，使用 {@code synchronized}
     * 比 CAS 重试循环更直观。
     */
    static final class Bucket {

        /** 两次放行之间的最小间隔（毫秒），创建桶时由配额决定 */
        private final long minIntervalMs;

        /** 最近一次放行时间（毫秒） */
        private volatile long lastGrantedAt;

        /** 是否已放行过至少一次（新建桶首次调用直接放行） */
        private boolean granted;

        Bucket(long minIntervalMs) {
            this.minIntervalMs = minIntervalMs;
        }

        /**
         * 尝试获取放行资格。
         *
         * <p>新建的桶首次调用必然放行（否则需要在创建时等待一个间隔，
         * 对首次调用并不合理），此后按最小间隔均匀放行。
         *
         * @param now 当前毫秒时间
         * @return 0 表示放行；大于 0 表示被拒，值为建议等待的毫秒数
         */
        synchronized long tryAcquire(long now) {
            if (!granted) {
                granted = true;
                lastGrantedAt = now;
                return 0L;
            }
            long elapsed = now - lastGrantedAt;
            if (elapsed < minIntervalMs) {
                return minIntervalMs - elapsed;
            }
            lastGrantedAt = now;
            return 0L;
        }

        /**
         * 判断桶是否长期空闲可被回收（空闲超过两个放行间隔）。
         *
         * @param now 当前毫秒时间
         * @return true 表示可回收
         */
        boolean isExpired(long now) {
            return granted && now - lastGrantedAt > minIntervalMs * 2;
        }
    }

    /**
     * 单次限流决策结果。
     *
     * @param allowed      是否放行
     * @param retryAfterMs 被拒时建议等待的毫秒数，放行时为 0
     * @param limitRpm     本次调用生效的每分钟配额，0 表示不限流
     */
    public record Decision(boolean allowed, long retryAfterMs, int limitRpm) {

        /**
         * 构建放行决策。
         *
         * @param limitRpm 生效配额
         * @return 放行决策
         */
        public static Decision allow(int limitRpm) {
            return new Decision(true, 0L, limitRpm);
        }

        /**
         * 构建拒绝决策。
         *
         * @param retryAfterMs 建议等待毫秒数
         * @param limitRpm     生效配额
         * @return 拒绝决策
         */
        public static Decision reject(long retryAfterMs, int limitRpm) {
            return new Decision(false, retryAfterMs, limitRpm);
        }
    }
}
