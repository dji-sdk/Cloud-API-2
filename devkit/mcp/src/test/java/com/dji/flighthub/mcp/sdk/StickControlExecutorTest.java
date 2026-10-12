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

package com.dji.flighthub.mcp.sdk;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StickControlExecutor 单元测试。
 */
class StickControlExecutorTest {

    private final StickControlExecutor executor = new StickControlExecutor();

    @Test
    void buildStickData_forward_shouldIncreasePitch() {
        int[] data = executor.buildStickData(StickDirection.FORWARD, 50);
        assertEquals(StickControlExecutor.STICK_CENTER, data[0]); // roll 不变
        assertTrue(data[1] > StickControlExecutor.STICK_CENTER);  // pitch 增大
        assertEquals(StickControlExecutor.STICK_CENTER, data[2]); // throttle 不变
    }

    @Test
    void buildStickData_left_shouldDecreaseRoll() {
        int[] data = executor.buildStickData(StickDirection.LEFT, 50);
        assertTrue(data[0] < StickControlExecutor.STICK_CENTER); // roll 减小
    }

    @Test
    void buildStickData_up_shouldIncreaseThrottle() {
        int[] data = executor.buildStickData(StickDirection.UP, 100);
        assertEquals(StickControlExecutor.STICK_MAX, data[2]); // throttle 到最大值
    }

    @Test
    void execute_shouldSendMultipleTicks() {
        AtomicInteger sendCount = new AtomicInteger(0);
        StickControlExecutor.StickSender mockSender = (r, p, t, y, g) -> sendCount.incrementAndGet();

        int ticks = executor.execute(mockSender, StickDirection.FORWARD, 30, 200);

        assertTrue(ticks >= 3); // 200ms / 50ms = 4 ticks，至少 3 次（允许轻微时序误差）
        assertTrue(sendCount.get() >= ticks + 1); // +1 为中位值停止帧
    }

    @Test
    void execute_shouldClampDuration() {
        AtomicInteger sendCount = new AtomicInteger(0);
        StickControlExecutor.StickSender mockSender = (r, p, t, y, g) -> sendCount.incrementAndGet();

        long start = System.currentTimeMillis();
        executor.execute(mockSender, StickDirection.UP, 50, 10000);
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 6000); // duration 应被截断到 ~5000ms
    }

    @Test
    void execute_shouldSendCenterOnCompletion() {
        int[][] lastSent = {null};
        StickControlExecutor.StickSender mockSender = (r, p, t, y, g) ->
                lastSent[0] = new int[]{r, p, t, y, g};

        executor.execute(mockSender, StickDirection.FORWARD, 50, 100);

        // 最后一帧必须是中位值停止
        assertNotNull(lastSent[0]);
        assertEquals(StickControlExecutor.STICK_CENTER, lastSent[0][0]);
        assertEquals(StickControlExecutor.STICK_CENTER, lastSent[0][1]);
        assertEquals(StickControlExecutor.STICK_CENTER, lastSent[0][2]);
    }
}
