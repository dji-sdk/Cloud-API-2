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

package com.dji.flighthub.mcp.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionContext grace period 功能单元测试。
 * 验证断连保护期的状态转换、过期判定和重连恢复。
 */
class SessionContextGracePeriodTest {

    private SessionContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new SessionContext("sess_gp_test", "ws-uuid", "TestWorkspace", 3);
    }

    // ==================== 初始状态 ====================

    @Test
    void initialState_shouldNotBeInGracePeriod() {
        assertFalse(ctx.isInGracePeriod());
        assertNull(ctx.getGracePeriodStartedAt());
    }

    @Test
    void initialState_shouldNotBeGracePeriodExpired() {
        assertFalse(ctx.isGracePeriodExpired(60));
    }

    // ==================== enterGracePeriod ====================

    @Test
    void enterGracePeriod_shouldSetInGracePeriodTrue() {
        ctx.enterGracePeriod();

        assertTrue(ctx.isInGracePeriod());
        assertNotNull(ctx.getGracePeriodStartedAt());
    }

    @Test
    void enterGracePeriod_shouldNotBeExpiredImmediately() {
        ctx.enterGracePeriod();

        // 刚进入 grace period，60s 超时不会立即过期
        assertFalse(ctx.isGracePeriodExpired(60));
    }

    // ==================== exitGracePeriod ====================

    @Test
    void exitGracePeriod_shouldClearGraceState() {
        ctx.enterGracePeriod();
        ctx.exitGracePeriod();

        assertFalse(ctx.isInGracePeriod());
        assertNull(ctx.getGracePeriodStartedAt());
    }

    @Test
    void exitGracePeriod_shouldRefreshLastActivityAt() throws InterruptedException {
        var beforeEnter = ctx.getLastActivityAt();
        Thread.sleep(10);

        ctx.enterGracePeriod();
        Thread.sleep(10);

        ctx.exitGracePeriod();

        // exitGracePeriod 调用 touch()，lastActivityAt 应更新
        assertTrue(ctx.getLastActivityAt().isAfter(beforeEnter));
    }

    // ==================== isGracePeriodExpired ====================

    @Test
    void isGracePeriodExpired_shouldReturnFalse_whenNotInGracePeriod() {
        // 未进入 grace period，无论超时时间设多短都不应过期
        assertFalse(ctx.isGracePeriodExpired(0));
        assertFalse(ctx.isGracePeriodExpired(1));
    }

    @Test
    void isGracePeriodExpired_shouldReturnTrue_whenGracePeriodExceedsThreshold() throws InterruptedException {
        ctx.enterGracePeriod();

        // 等待超过超时阈值（使用极短的 threshold 模拟过期）
        Thread.sleep(20);

        // 0 秒超时 → 必然过期
        assertTrue(ctx.isGracePeriodExpired(0));
    }

    @Test
    void isGracePeriodExpired_shouldReturnFalse_whenWithinThreshold() {
        ctx.enterGracePeriod();

        // 使用足够长的 threshold，不应过期
        assertFalse(ctx.isGracePeriodExpired(3600));
    }

    // ==================== 状态转换幂等性 ====================

    @Test
    void enterGracePeriod_calledTwice_shouldUpdateStartTime() throws InterruptedException {
        ctx.enterGracePeriod();
        var firstStart = ctx.getGracePeriodStartedAt();

        Thread.sleep(10);
        ctx.enterGracePeriod();
        var secondStart = ctx.getGracePeriodStartedAt();

        // 第二次调用应更新开始时间
        assertTrue(secondStart.isAfter(firstStart));
        assertTrue(ctx.isInGracePeriod());
    }

    @Test
    void exitGracePeriod_calledWithoutEnter_shouldNotThrow() {
        // 未进入 grace period 时调用 exit 不应抛异常
        assertDoesNotThrow(() -> ctx.exitGracePeriod());
        assertFalse(ctx.isInGracePeriod());
    }

    // ==================== 完整生命周期 ====================

    @Test
    void fullLifecycle_enterThenExpireThenExit() throws InterruptedException {
        // 1. 正常活跃
        assertFalse(ctx.isInGracePeriod());

        // 2. 断连 → 进入 grace period
        ctx.enterGracePeriod();
        assertTrue(ctx.isInGracePeriod());
        assertFalse(ctx.isGracePeriodExpired(60));

        // 3. 模拟时间流逝（使用 0s 阈值模拟过期）
        Thread.sleep(10);
        assertTrue(ctx.isGracePeriodExpired(0));

        // 4. 重连恢复
        ctx.exitGracePeriod();
        assertFalse(ctx.isInGracePeriod());
        assertFalse(ctx.isGracePeriodExpired(0));
    }
}
