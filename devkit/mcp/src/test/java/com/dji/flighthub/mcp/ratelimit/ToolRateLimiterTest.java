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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ToolRateLimiter 单元测试 — 覆盖分层配额、键隔离、容量保护与 fail-open 策略。
 *
 * <p>时间通过注入的 {@link LongSupplier} 手动推进，不使用 {@code Thread.sleep}，
 * 保证测试离线可运行且稳定。
 */
class ToolRateLimiterTest {

    /** L2 飞行工具默认配额 6/min，对应最小放行间隔 10000ms */
    private static final long L2_INTERVAL_MS = 10_000L;

    private static final String TOOL_FLIGHT = "fh_takeoff";
    private static final String TOOL_READ = "fh_device_list";
    private static final String TOOL_CRITICAL = "fh_emergency_stop";
    private static final String SESSION_A = "sess-a";
    private static final String SESSION_B = "sess-b";

    private AtomicLong now;
    private FlightHubProperties.RateLimit config;

    @BeforeEach
    void setUp() {
        now = new AtomicLong(1_000L);
        config = new FlightHubProperties.RateLimit();
    }

    private ToolRateLimiter createLimiter() {
        FlightHubProperties props = new FlightHubProperties();
        props.setRateLimit(config);
        return new ToolRateLimiter(props, now::get);
    }

    @Test
    void tryAcquire_shouldAllow_whenFirstCall() {
        ToolRateLimiter limiter = createLimiter();

        ToolRateLimiter.Decision decision = limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of());

        assertTrue(decision.allowed());
        assertEquals(0L, decision.retryAfterMs());
        assertEquals(config.getFlightRpm(), decision.limitRpm());
    }

    @Test
    void tryAcquire_shouldReject_whenWithinInterval() {
        ToolRateLimiter limiter = createLimiter();
        limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of());

        // 同一毫秒再次调用：应被拒绝，且建议等待时间不超过一个间隔
        ToolRateLimiter.Decision decision = limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of());

        assertFalse(decision.allowed());
        assertTrue(decision.retryAfterMs() > 0L);
        assertTrue(decision.retryAfterMs() <= L2_INTERVAL_MS,
                "retry_after_ms 应落在 (0, " + L2_INTERVAL_MS + "]，实际 " + decision.retryAfterMs());
        assertEquals(config.getFlightRpm(), decision.limitRpm());
    }

    @Test
    void tryAcquire_shouldAllow_whenIntervalElapsed() {
        ToolRateLimiter limiter = createLimiter();
        limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of());

        // 未满间隔仍拒绝
        now.addAndGet(L2_INTERVAL_MS - 1);
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());

        // 刚好满间隔应放行
        now.addAndGet(1);
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldNeverReject_whenLevelIsL3() {
        ToolRateLimiter limiter = createLimiter();

        // L3 默认 criticalRpm=0，表示不限流；紧急停止必须永远可执行
        for (int i = 0; i < 1000; i++) {
            assertTrue(limiter.tryAcquire(SESSION_A, TOOL_CRITICAL, SecurityLevel.CRITICAL, Map.of()).allowed(),
                    "第 " + (i + 1) + " 次 L3 调用不应被限流");
        }
    }

    @Test
    void tryAcquire_shouldIsolateBuckets_whenSessionDiffers() {
        ToolRateLimiter limiter = createLimiter();
        limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of());

        // 另一个 session 有独立配额，不受影响
        assertTrue(limiter.tryAcquire(SESSION_B, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
        // 原 session 仍然被限
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldFallbackToToolNameKey_whenStdio() {
        ToolRateLimiter limiter = createLimiter();

        // stdio 模式无 sessionId：退化为仅按 toolName 计数，两次调用共享同一个桶
        assertTrue(limiter.tryAcquire(null, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
        assertFalse(limiter.tryAcquire(null, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());

        // 空字符串与 null 等价
        assertFalse(limiter.tryAcquire("   ", TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldFailOpen_whenMaxKeysExceeded() {
        config.setMaxKeys(1);
        ToolRateLimiter limiter = createLimiter();

        // 第一个 key 占满容量
        limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of());

        // 新 key 超限：放行且不建桶，因此连续两次都不会被限流
        assertTrue(limiter.tryAcquire(SESSION_B, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
        assertTrue(limiter.tryAcquire(SESSION_B, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed(),
                "键空间耗尽时应 fail-open，不得因限流器自身容量阻断调用");
    }

    @Test
    void tryAcquire_shouldRespectPerToolOverride_whenConfigured() {
        config.setPerToolRpm(Map.of(TOOL_FLIGHT, 60));
        ToolRateLimiter limiter = createLimiter();

        // 覆盖值 60/min → 间隔 1000ms，优先于 FLIGHT 档位的 6/min
        ToolRateLimiter.Decision first = limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of());
        assertEquals(60, first.limitRpm());

        now.addAndGet(999);
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());

        now.addAndGet(1);
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldAllowUnlimited_whenRpmIsZero() {
        config.setPerToolRpm(Map.of(TOOL_READ, 0));
        ToolRateLimiter limiter = createLimiter();

        for (int i = 0; i < 100; i++) {
            ToolRateLimiter.Decision decision = limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of());
            assertTrue(decision.allowed());
            assertEquals(0, decision.limitRpm());
        }
    }

    @Test
    void tryAcquire_shouldApplyTierQuota_whenLevelIsReadOnly() {
        ToolRateLimiter limiter = createLimiter();

        // L0 默认 300/min → 间隔 200ms
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of()).allowed());
        now.addAndGet(199);
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of()).allowed());
        now.addAndGet(1);
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldAllowAll_whenDisabled() {
        config.setEnabled(false);
        ToolRateLimiter limiter = createLimiter();

        // 关闭后即使同一毫秒连续调用也全部放行
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
        }
    }

    @Test
    void tryAcquire_shouldApplyReadOnlyTier_whenLevelIsNull() {
        ToolRateLimiter limiter = createLimiter();

        // level == null 按 L0 处理：200ms 间隔，而不是放行或最严档位
        ToolRateLimiter.Decision first = limiter.tryAcquire(SESSION_A, TOOL_READ, null, Map.of());
        assertTrue(first.allowed());
        assertEquals(config.getReadOnlyRpm(), first.limitRpm());

        now.addAndGet(199);
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_READ, null, Map.of()).allowed());
        now.addAndGet(1);
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_READ, null, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldPreferPerToolOverride_whenLevelIsCritical() {
        // 覆盖优先级高于档位：L3 默认不限流，但显式配置后必须生效
        config.setPerToolRpm(Map.of(TOOL_CRITICAL, 60));
        ToolRateLimiter limiter = createLimiter();

        ToolRateLimiter.Decision first = limiter.tryAcquire(SESSION_A, TOOL_CRITICAL, SecurityLevel.CRITICAL, Map.of());
        assertTrue(first.allowed());
        assertEquals(60, first.limitRpm());

        now.addAndGet(999);
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_CRITICAL, SecurityLevel.CRITICAL, Map.of()).allowed());
        now.addAndGet(1);
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_CRITICAL, SecurityLevel.CRITICAL, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldStillLimit_whenRpmExceedsMillisecondPrecision() {
        // 120000/min 的间隔算出来是 0.5ms，整数除法会得到 0；
        // 若不钳位，第二次调用会被误判成「不限流」而放行
        config.setPerToolRpm(Map.of(TOOL_READ, 120_000));
        ToolRateLimiter limiter = createLimiter();

        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of()).allowed());
        // 同一毫秒内仍应被拒绝（精度上限 1ms ≈ 60000/min）
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of()).allowed(),
                "rpm 超过 60000 时不得静默退化为不限流");

        now.addAndGet(1);
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of()).allowed());
    }

    @Test
    void tryAcquire_shouldIsolateBuckets_whenDeviceDiffers() {
        ToolRateLimiter limiter = createLimiter();
        Map<String, Object> droneA = Map.of("drone_sn", "SN-A");
        Map<String, Object> droneB = Map.of("drone_sn", "SN-B");

        // 同一 session 下的两台飞机互不挤占配额
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, droneA).allowed());
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, droneB).allowed());
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, droneA).allowed());
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, droneB).allowed());
    }

    @Test
    void tryAcquire_shouldShareBucket_whenDeviceParamNameDiffersButValueSame() {
        ToolRateLimiter limiter = createLimiter();

        // drone_sn / dock_sn / device_sn 只是参数名不同，同一台设备应共用配额
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT,
                Map.of("drone_sn", "DEV-1")).allowed());
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT,
                Map.of("dock_sn", "DEV-1")).allowed());
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT,
                Map.of("device_sn", "DEV-1")).allowed());
    }

    @Test
    void tryAcquire_shouldFallBackToSessionKey_whenNoDeviceInArgs() {
        ToolRateLimiter limiter = createLimiter();

        // 只读查询类工具没有设备参数，保持存量键形状：同一 session 共用一个桶
        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, null).allowed());
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY, Map.of()).allowed());
        // 空白的设备号等价于无设备标识
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_READ, SecurityLevel.READ_ONLY,
                Map.of("drone_sn", "  ")).allowed());
    }

    @Test
    void tryAcquire_shouldUseTierQuotaAndOwnBucket_whenActionIsExempt() {
        // fh_return_home 的 per-tool 覆盖是 2 rpm；cancel 是中止操作，不能被启动配额挡住
        config.setPerToolRpm(Map.of(TOOL_FLIGHT, 2));
        ToolRateLimiter limiter = createLimiter();
        Map<String, Object> start = Map.of("action", "start");
        Map<String, Object> cancel = Map.of("action", "cancel");

        assertEquals(2, limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, start).limitRpm());

        // 独立桶：start 刚放行也不影响紧随其后的 cancel，且配额回落到档位值
        ToolRateLimiter.Decision firstCancel =
                limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, cancel);
        assertTrue(firstCancel.allowed(), "中止操作不应被同工具的启动配额拦截");
        assertEquals(config.getFlightRpm(), firstCancel.limitRpm(), "中止操作只走档位配额");

        // 但仍受档位配额约束，不是不限流后门
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, cancel).allowed());
        // 启动桶同样未被 cancel 消耗
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, start).allowed());
    }

    @Test
    void tryAcquire_shouldKeepPerToolOverride_whenActionIsNotExempt() {
        config.setPerToolRpm(Map.of(TOOL_FLIGHT, 2));
        ToolRateLimiter limiter = createLimiter();

        // 非豁免 action（如 fh_flyto 的 update）继续使用 per-tool 覆盖
        ToolRateLimiter.Decision decision = limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT,
                Map.of("action", "update"));
        assertEquals(2, decision.limitRpm());
    }

    @Test
    void tryAcquire_shouldFallBackToDefault_whenMaxKeysIsNotPositive() {
        // maxKeys <= 0 会让容量判断恒成立、限流整体失效，必须回落默认值而不是静默放开
        config.setMaxKeys(0);
        ToolRateLimiter limiter = createLimiter();

        assertTrue(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed());
        assertFalse(limiter.tryAcquire(SESSION_A, TOOL_FLIGHT, SecurityLevel.FLIGHT, Map.of()).allowed(),
                "max-keys 非法时应回落默认值并保持限流生效");
    }

    @Test
    void bucketTryAcquire_shouldGrantExactlyOnce_whenConcurrent() throws InterruptedException {
        int threads = 16;
        ToolRateLimiter.Bucket bucket = new ToolRateLimiter.Bucket(L2_INTERVAL_MS);
        long fixedNow = now.get();
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger granted = new AtomicInteger(0);
        List<Thread> workers = new ArrayList<>(threads);

        for (int i = 0; i < threads; i++) {
            Thread worker = new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (bucket.tryAcquire(fixedNow) == 0L) {
                    granted.incrementAndGet();
                }
            });
            workers.add(worker);
            worker.start();
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS), "工作线程未能全部就绪");
        start.countDown();
        for (Thread worker : workers) {
            worker.join(5_000L);
        }

        // 同一时刻的并发调用只能有一个拿到许可，其余必须被拒绝
        assertEquals(1, granted.get());
    }

    @Test
    void bucketIsExpired_shouldReturnTrue_whenIdleTwiceInterval() {
        ToolRateLimiter.Bucket bucket = new ToolRateLimiter.Bucket(L2_INTERVAL_MS);
        long start = now.get();
        bucket.tryAcquire(start);

        assertFalse(bucket.isExpired(start + L2_INTERVAL_MS * 2), "刚好两倍间隔时不应判定过期");
        assertTrue(bucket.isExpired(start + L2_INTERVAL_MS * 2 + 1));
    }

    @Test
    void bucketIsExpired_shouldReturnFalse_whenNeverGranted() {
        ToolRateLimiter.Bucket bucket = new ToolRateLimiter.Bucket(L2_INTERVAL_MS);

        // 未放行过的桶没有时间基准，不参与清理
        assertFalse(bucket.isExpired(Long.MAX_VALUE));
    }
}
