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
 * 内容审核结果。
 *
 * @param flagged        是否标记为违规
 * @param reason         违规原因描述（未违规时为 null）
 * @param matchedKeyword 命中的关键词（未违规时为 null）
 * @author DJI DevKit Team
 */
public record ModerationResult(boolean flagged, String reason, String matchedKeyword) {

    /**
     * 安全结果（未违规）。
     *
     * @return 安全的 ModerationResult
     */
    public static ModerationResult safe() {
        return new ModerationResult(false, null, null);
    }

    /**
     * 违规结果。
     *
     * @param reason  违规原因
     * @param keyword 命中的关键词
     * @return 违规的 ModerationResult
     */
    public static ModerationResult blocked(String reason, String keyword) {
        return new ModerationResult(true, reason, keyword);
    }
}
