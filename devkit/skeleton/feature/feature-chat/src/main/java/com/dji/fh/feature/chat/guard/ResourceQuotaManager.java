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

package com.dji.fh.feature.chat.guard;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 按用户维度的资源配额管理器 — 调用频率 + 并发流数 + 日 token 额度。
 *
 * <p>全内存实现（ConcurrentHashMap），无 Redis 依赖。
 * 复用 MCP Server {@code RateLimitWebFilter} 的 WindowCounter 模式。
 *
 * <p>线程安全：使用 AtomicInteger/AtomicLong + volatile 保证原子性。
 *
 * @author DJI DevKit Team
 */
public class ResourceQuotaManager {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceQuotaManager.class);

    /** 默认 OOM 保护：最多追踪的用户数。 */
    private static final int MAX_USER_ENTRIES = 10000;

    /** 每 N 次操作触发一次过期清理。 */
    private static final int CLEANUP_INTERVAL = 64;

    private final ConcurrentHashMap<String, UserQuotaState> userStates = new ConcurrentHashMap<>();
    private final AtomicLong operationCount = new AtomicLong(0);

    private final int maxCallsPerWindow;
    private final long windowMs;
    private final int maxConcurrentStreams;
    private final long maxDailyTokens;

    /**
     * 构造配额管理器。
     *
     * @param maxCallsPerWindow    窗口内最大调用次数
     * @param windowMs             滑动窗口时长（毫秒）
     * @param maxConcurrentStreams  单用户最大并发流数
     * @param maxDailyTokens       单用户日 token 额度（0 表示不限制）
     */
    public ResourceQuotaManager(int maxCallsPerWindow, long windowMs,
                                int maxConcurrentStreams, long maxDailyTokens) {
        this.maxCallsPerWindow = maxCallsPerWindow;
        this.windowMs = windowMs;
        this.maxConcurrentStreams = maxConcurrentStreams;
        this.maxDailyTokens = maxDailyTokens;
    }

    /**
     * 请求前检查：调用频率限制。
     *
     * <p>并发检查已移至 {@link #tryStreamStart(String)}，通过 CAS 保证原子性。
     *
     * @param principalId 用户标识
     * @return 检查结果
     */
    public GuardResult tryAcquire(String principalId) {
        if (principalId == null || principalId.isBlank()) {
            return GuardResult.pass();
        }

        periodicCleanup();

        // OOM 保护：未知用户且配额表已满时拒绝
        if (!userStates.containsKey(principalId) && userStates.size() >= MAX_USER_ENTRIES) {
            LOG.warn("ResourceQuotaManager：用户配额表已满（{}），拒绝新用户 userId={}",
                    MAX_USER_ENTRIES, principalId);
            return GuardResult.blocked(
                    GuardResult.CODE_RATE_LIMITED,
                    "系统繁忙，请稍后重试");
        }

        UserQuotaState state = getOrCreate(principalId);

        // 频率检查（窗口内调用次数）
        long now = System.currentTimeMillis();
        synchronized (state) {
            if (now - state.windowStart > windowMs) {
                state.callCount.set(0);
                state.windowStart = now;
            }
            int currentCalls = state.callCount.incrementAndGet();
            if (currentCalls > maxCallsPerWindow) {
                LOG.warn("ResourceQuotaManager：频率超限 userId={} calls={}/{}",
                        principalId, currentCalls, maxCallsPerWindow);
                return GuardResult.blocked(
                        GuardResult.CODE_RATE_LIMITED,
                        String.format("调用频率已达上限（%d 次/小时），请稍后重试", maxCallsPerWindow));
            }
        }

        return GuardResult.pass();
    }

    /**
     * 检查日 token 额度。
     *
     * @param principalId 用户标识
     * @return 检查结果
     */
    public GuardResult checkTokenBudget(String principalId) {
        if (maxDailyTokens <= 0 || principalId == null || principalId.isBlank()) {
            return GuardResult.pass();
        }
        UserQuotaState state = userStates.get(principalId);
        if (state == null) {
            return GuardResult.pass();
        }

        resetDayIfNeeded(state);
        long used = state.dailyTokens.get();
        if (used >= maxDailyTokens) {
            LOG.warn("ResourceQuotaManager：日 token 超限 userId={} used={}/{}",
                    principalId, used, maxDailyTokens);
            return GuardResult.blocked(
                    GuardResult.CODE_TOKEN_BUDGET_EXCEEDED,
                    String.format("今日 token 用量已达上限（%d），请明日再试", maxDailyTokens));
        }
        return GuardResult.pass();
    }

    /**
     * 尝试开始一个新流 — 原子地检查并递增并发计数（CAS 模式）。
     *
     * <p>调用成功（{@code allowed=true}）后，必须在 finally 中调用 {@link #streamEnded(String, long)}。
     *
     * @param principalId 用户标识
     * @return 检查结果：允许时已完成递增，拒绝时未修改计数
     */
    public GuardResult tryStreamStart(String principalId) {
        if (principalId == null || principalId.isBlank()) {
            return GuardResult.pass();
        }
        UserQuotaState state = getOrCreate(principalId);
        while (true) {
            int current = state.concurrentStreams.get();
            if (current >= maxConcurrentStreams) {
                LOG.warn("ResourceQuotaManager：并发超限 userId={} concurrent={}/{}",
                        principalId, current, maxConcurrentStreams);
                return GuardResult.blocked(
                        GuardResult.CODE_CONCURRENT_LIMITED,
                        String.format("并发请求数已达上限（%d），请等待当前对话完成后重试",
                                maxConcurrentStreams));
            }
            if (state.concurrentStreams.compareAndSet(current, current + 1)) {
                return GuardResult.pass();
            }
        }
    }

    /**
     * 流结束时调用，递减并发计数并累加 token 消耗。
     *
     * @param principalId 用户标识
     * @param tokensUsed  本次消耗的 token 数（估算值）
     */
    public void streamEnded(String principalId, long tokensUsed) {
        if (principalId == null || principalId.isBlank()) {
            return;
        }
        UserQuotaState state = userStates.get(principalId);
        if (state != null) {
            state.concurrentStreams.decrementAndGet();
            if (tokensUsed > 0) {
                resetDayIfNeeded(state);
                state.dailyTokens.addAndGet(tokensUsed);
            }
        }
    }

    private UserQuotaState getOrCreate(String principalId) {
        return userStates.computeIfAbsent(principalId, k -> new UserQuotaState());
    }

    private void periodicCleanup() {
        if (operationCount.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            long now = System.currentTimeMillis();
            long dayMs = 24 * 60 * 60 * 1000L;
            userStates.entrySet().removeIf(entry -> {
                UserQuotaState state = entry.getValue();
                return state.concurrentStreams.get() == 0
                        && (now - state.windowStart > windowMs)
                        && (now - state.dayStart > dayMs);
            });
        }
    }

    private void resetDayIfNeeded(UserQuotaState state) {
        long now = System.currentTimeMillis();
        long dayMs = 24 * 60 * 60 * 1000L;
        if (now - state.dayStart > dayMs) {
            synchronized (state) {
                if (now - state.dayStart > dayMs) {
                    state.dailyTokens.set(0);
                    state.dayStart = now;
                }
            }
        }
    }

    /**
     * 单用户配额状态。
     */
    private static class UserQuotaState {
        final AtomicInteger callCount = new AtomicInteger(0);
        final AtomicInteger concurrentStreams = new AtomicInteger(0);
        final AtomicLong dailyTokens = new AtomicLong(0);
        volatile long windowStart = System.currentTimeMillis();
        volatile long dayStart = System.currentTimeMillis();
    }
}
