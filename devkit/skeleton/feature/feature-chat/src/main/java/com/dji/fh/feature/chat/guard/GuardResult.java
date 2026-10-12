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

/**
 * 安全检查结果。
 *
 * @param allowed      是否放行
 * @param errorCode    拦截时的错误码（放行时为 null）
 * @param errorMessage 拦截时的用户可见消息（放行时为 null）
 * @author DJI DevKit Team
 */
public record GuardResult(boolean allowed, String errorCode, String errorMessage) {

    /** 错误码：Prompt 注入检测命中。 */
    public static final String CODE_INJECTION_DETECTED = "injection_detected";

    /** 错误码：输入内容审核拦截。 */
    public static final String CODE_CONTENT_BLOCKED = "content_blocked";

    /** 错误码：调用频率超限。 */
    public static final String CODE_RATE_LIMITED = "rate_limited";

    /** 错误码：并发流数超限。 */
    public static final String CODE_CONCURRENT_LIMITED = "concurrent_limited";

    /** 错误码：日 token 额度超限。 */
    public static final String CODE_TOKEN_BUDGET_EXCEEDED = "token_budget_exceeded";

    /**
     * 放行结果。
     *
     * @return 允许通过的 GuardResult
     */
    public static GuardResult pass() {
        return new GuardResult(true, null, null);
    }

    /**
     * 拦截结果。
     *
     * @param code    错误码
     * @param message 用户可见消息
     * @return 拦截的 GuardResult
     */
    public static GuardResult blocked(String code, String message) {
        return new GuardResult(false, code, message);
    }
}
