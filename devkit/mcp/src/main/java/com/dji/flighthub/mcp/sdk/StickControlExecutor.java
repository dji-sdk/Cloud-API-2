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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 摇杆控制执行器 — 将方向+时长语义转为 50ms 间隔的 StickControlData 发送。
 * <p>
 * 安全设计：
 * <ul>
 *   <li>duration 上限 5000ms</li>
 *   <li>执行完毕自动发送中位值停止</li>
 *   <li>执行期间阻塞调用方</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class StickControlExecutor {

    private static final Logger log = LoggerFactory.getLogger(StickControlExecutor.class);

    /** 摇杆指令发送间隔（毫秒） */
    public static final int INTERVAL_MS = 50;

    /** 摇杆中位值（停止/悬停） */
    public static final int STICK_CENTER = 1024;

    /** 摇杆最小值 */
    public static final int STICK_MIN = 364;

    /** 摇杆最大值 */
    public static final int STICK_MAX = 1684;

    /** 单次指令最大持续时间（毫秒） */
    public static final int MAX_DURATION_MS = 5000;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "stick-control");
                t.setDaemon(true);
                return t;
            });

    /**
     * 执行受限方向移动（阻塞调用）。
     *
     * @param stickSender  发送函数（真实模式调 drone.sendStickControl，mock 模式为空操作）
     * @param direction    移动方向
     * @param speedPercent 速度百分比 10~100
     * @param durationMs   持续时间 100~5000ms
     * @return 实际发送的 tick 次数
     */
    public int execute(StickSender stickSender, StickDirection direction,
                       int speedPercent, int durationMs) {
        int clampedSpeed = Math.min(Math.max(speedPercent, 10), 100);
        int clampedDuration = clampDuration(durationMs);
        int[] stickData = buildStickData(direction, clampedSpeed);

        int ticks = runTicks(stickSender, stickData, clampedDuration);

        log.debug("Stick move completed: direction={}, speed={}%, duration={}ms, ticks={}",
                direction, clampedSpeed, clampedDuration, ticks);
        return ticks;
    }

    /**
     * 执行原始轴摇杆控制（AI 直接指定 roll/pitch/throttle/yaw/gimbalPitch 绝对值）。
     * 比 {@link #execute} 更底层——不做方向语义转换，仅做范围裁剪与安全停止。
     *
     * @param stickSender 发送函数
     * @param roll        横滚通道绝对值，自动裁剪到 [{@link #STICK_MIN}, {@link #STICK_MAX}]
     * @param pitch       俯仰通道绝对值，范围同上
     * @param throttle    油门通道绝对值，范围同上
     * @param yaw         偏航通道绝对值，范围同上
     * @param gimbalPitch 云台俯仰通道绝对值，范围同上
     * @param durationMs  持续时间 100~5000ms
     * @return 实际发送的 tick 次数
     */
    public int executeRaw(StickSender stickSender, int roll, int pitch, int throttle,
                          int yaw, int gimbalPitch, int durationMs) {
        int clampedDuration = clampDuration(durationMs);
        int[] stickData = {
                clampStick(roll), clampStick(pitch), clampStick(throttle),
                clampStick(yaw), clampStick(gimbalPitch)
        };

        int ticks = runTicks(stickSender, stickData, clampedDuration);

        log.debug("Raw stick control completed: roll={}, pitch={}, throttle={}, yaw={}, gimbal={}, duration={}ms, ticks={}",
                stickData[0], stickData[1], stickData[2], stickData[3], stickData[4], clampedDuration, ticks);
        return ticks;
    }

    /**
     * 裁剪持续时间到安全范围。
     *
     * @param durationMs 原始持续时间
     * @return 裁剪到 [100, {@link #MAX_DURATION_MS}] 的持续时间
     */
    private static int clampDuration(int durationMs) {
        return Math.min(Math.max(durationMs, 100), MAX_DURATION_MS);
    }

    /**
     * 按固定间隔发送同一帧摇杆数据，执行完毕或超时后自动发送中位值停止。
     *
     * @param stickSender 发送函数
     * @param stickData   长度为 5 的 [roll, pitch, throttle, yaw, gimbal] 数组
     * @param durationMs  持续时间，调用方需已通过 {@link #clampDuration} 裁剪
     * @return 实际发送的 tick 次数
     */
    private int runTicks(StickSender stickSender, int[] stickData, int durationMs) {
        int clampedDuration = clampDuration(durationMs);
        int totalTicks = clampedDuration / INTERVAL_MS;
        int[] tickCount = {0};

        CountDownLatch latch = new CountDownLatch(totalTicks);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                stickSender.send(stickData[0], stickData[1], stickData[2], stickData[3], stickData[4]);
                tickCount[0]++;
                latch.countDown();
            } catch (Exception e) {
                log.error("Stick control send failed", e);
                while (latch.getCount() > 0) {
                    latch.countDown();
                }
            }
        }, 0, INTERVAL_MS, TimeUnit.MILLISECONDS);

        try {
            boolean completed = latch.await(clampedDuration + 500, TimeUnit.MILLISECONDS);
            if (!completed) {
                log.warn("Stick control timed out after {}ms", clampedDuration);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            future.cancel(false);
            // 发送中位值停止
            stickSender.send(STICK_CENTER, STICK_CENTER, STICK_CENTER, STICK_CENTER, STICK_CENTER);
        }

        return tickCount[0];
    }

    /**
     * 将摇杆通道值裁剪到合法范围 [{@link #STICK_MIN}, {@link #STICK_MAX}]。
     *
     * @param value 原始值
     * @return 裁剪后的值
     */
    private static int clampStick(int value) {
        return Math.min(Math.max(value, STICK_MIN), STICK_MAX);
    }

    /**
     * 将方向和速度百分比转为 [roll, pitch, throttle, yaw, gimbalPitch] 数组。
     *
     * @param direction    移动方向
     * @param speedPercent 速度百分比 10~100
     * @return 长度为 5 的 int 数组：[roll, pitch, throttle, yaw, gimbal]
     */
    public int[] buildStickData(StickDirection direction, int speedPercent) {
        int offset = (int) ((STICK_MAX - STICK_CENTER) * speedPercent / 100.0);
        int roll = STICK_CENTER;
        int pitch = STICK_CENTER;
        int throttle = STICK_CENTER;
        int yaw = STICK_CENTER;
        int gimbal = STICK_CENTER;

        switch (direction) {
            case FORWARD -> pitch = STICK_CENTER + offset;
            case BACKWARD -> pitch = STICK_CENTER - offset;
            case RIGHT -> roll = STICK_CENTER + offset;
            case LEFT -> roll = STICK_CENTER - offset;
            case UP -> throttle = STICK_CENTER + offset;
            case DOWN -> throttle = STICK_CENTER - offset;
            case ROTATE_CW -> yaw = STICK_CENTER + offset;
            case ROTATE_CCW -> yaw = STICK_CENTER - offset;
            default -> throw new IllegalStateException("Unexpected direction: " + direction);
        }
        return new int[]{roll, pitch, throttle, yaw, gimbal};
    }

    /**
     * 摇杆数据发送接口 — 用于解耦真实 SDK 调用与测试 mock。
     */
    @FunctionalInterface
    public interface StickSender {
        /**
         * 发送一帧摇杆控制数据。
         *
         * @param roll        横滚通道值 364~1684
         * @param pitch       俯仰通道值 364~1684
         * @param throttle    油门通道值 364~1684
         * @param yaw         偏航通道值 364~1684
         * @param gimbalPitch 云台俯仰通道值 364~1684
         */
        void send(int roll, int pitch, int throttle, int yaw, int gimbalPitch);
    }
}
