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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import com.dji.fh.feature.chat.controller.ChatRequest;

/**
 * 安全防护链编排器 — 按顺序执行 quota → injection → moderation，fail-fast。
 *
 * <p>任何一个组件检查失败即立即返回拦截结果，后续组件不再执行。
 * 各组件为 nullable：对应功能未启用时直接跳过。
 *
 * @author DJI DevKit Team
 */
public class ChatGuardChain {

    private static final Logger LOG = LoggerFactory.getLogger(ChatGuardChain.class);

    @Nullable
    private final ResourceQuotaManager quotaManager;

    @Nullable
    private final PromptInjectionDetector injectionDetector;

    @Nullable
    private final ContentModerator contentModerator;

    private final boolean outputCheckEnabled;

    /**
     * 构造防护链。
     *
     * @param quotaManager       配额管理器（可为 null，表示 quota 功能关闭）
     * @param injectionDetector  注入检测器（可为 null，表示注入检测关闭）
     * @param contentModerator   内容审核器（可为 null，表示内容审核关闭）
     * @param outputCheckEnabled 是否启用输出侧审核
     */
    public ChatGuardChain(@Nullable ResourceQuotaManager quotaManager,
                          @Nullable PromptInjectionDetector injectionDetector,
                          @Nullable ContentModerator contentModerator,
                          boolean outputCheckEnabled) {
        this.quotaManager = quotaManager;
        this.injectionDetector = injectionDetector;
        this.contentModerator = contentModerator;
        this.outputCheckEnabled = outputCheckEnabled;
    }

    /**
     * 配额前置检查（在 SseEmitter 创建之前调用）。
     *
     * <p>频率限制和 token 预算属于资源层面的拒绝，应在请求进入流式通道之前返回 HTTP 429。
     *
     * @param principalId 用户标识
     * @return 检查结果
     */
    public GuardResult quotaPreCheck(String principalId) {
        if (quotaManager == null) {
            return GuardResult.pass();
        }

        GuardResult result = quotaManager.tryAcquire(principalId);
        if (!result.allowed()) {
            return result;
        }
        return quotaManager.checkTokenBudget(principalId);
    }

    /**
     * 内容前置检查（在 SseEmitter 创建之后、LLM 调用之前）。
     *
     * <p>执行顺序：Prompt 注入检测 → 输入内容审核。
     *
     * @param principalId 用户标识（用于日志追踪）
     * @param request     聊天请求
     * @return 检查结果
     */
    public GuardResult preCheck(String principalId, ChatRequest request) {
        // 1. Prompt 注入检测
        if (injectionDetector != null) {
            GuardResult result = injectionDetector.checkAll(
                    request.getMessage(), request.getHistory());
            if (!result.allowed()) {
                LOG.info("ChatGuardChain：preCheck 拦截 userId={} stage=injection code={}",
                        principalId, result.errorCode());
                return result;
            }
        }

        // 2. 输入内容审核
        if (contentModerator != null) {
            ModerationResult modResult = contentModerator.moderateInput(request.getMessage());
            if (modResult.flagged()) {
                LOG.info("ChatGuardChain：preCheck 拦截 userId={} stage=moderation keyword={}",
                        principalId, modResult.matchedKeyword());
                return GuardResult.blocked(
                        GuardResult.CODE_CONTENT_BLOCKED,
                        modResult.reason());
            }
        }

        return GuardResult.pass();
    }

    /**
     * 输出后置检查（LLM 流完成后，对完整回复执行内容审核）。
     *
     * <p>流式场景中内容已发送给客户端，此检查结果仅用于追加 warning 事件。
     *
     * @param fullResponse LLM 完整回复文本
     * @return 审核结果（null 表示无需检查或通过）
     */
    @Nullable
    public ModerationResult postCheck(String fullResponse) {
        if (!outputCheckEnabled || contentModerator == null) {
            return null;
        }
        if (fullResponse == null || fullResponse.isBlank()) {
            return null;
        }
        ModerationResult result = contentModerator.moderateOutput(fullResponse);
        if (result.flagged()) {
            LOG.info("ChatGuardChain：postCheck 输出审核命中 keyword={}", result.matchedKeyword());
            return result;
        }
        return null;
    }

    /**
     * 流开始时通知 — 原子地检查并递增并发计数。
     *
     * <p>返回 {@code allowed=true} 表示并发已递增，调用方必须在 finally 中调用
     * {@link #onStreamEnd(String, long)} 来递减。
     * 返回 {@code allowed=false} 表示并发超限，计数未修改，不得调用 onStreamEnd。
     *
     * @param principalId 用户标识
     * @return 检查结果
     */
    public GuardResult onStreamStart(String principalId) {
        if (quotaManager != null) {
            return quotaManager.tryStreamStart(principalId);
        }
        return GuardResult.pass();
    }

    /**
     * 流结束时通知（递减并发计数，累加 token 消耗）。
     *
     * @param principalId 用户标识
     * @param tokensUsed  本次消耗的 token 数
     */
    public void onStreamEnd(String principalId, long tokensUsed) {
        if (quotaManager != null) {
            quotaManager.streamEnded(principalId, tokensUsed);
        }
    }
}
