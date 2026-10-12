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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * {@link DefaultContentModerator} 单元测试。
 *
 * @author DJI DevKit Team
 */
class DefaultContentModeratorTest {

    // -------------------------------------------------------------------------
    // 输入审核
    // -------------------------------------------------------------------------

    @Test
    void moderateInput_shouldBlock_whenKeywordMatches() {
        ContentModerator moderator = new DefaultContentModerator(List.of("违禁词", "敏感内容"));
        ModerationResult result = moderator.moderateInput("这是一条含有违禁词的消息");
        assertThat(result.flagged()).isTrue();
        assertThat(result.matchedKeyword()).isEqualTo("违禁词");
    }

    @Test
    void moderateInput_shouldPass_whenNoKeywordMatches() {
        ContentModerator moderator = new DefaultContentModerator(List.of("违禁词"));
        ModerationResult result = moderator.moderateInput("这是一条正常的查询消息");
        assertThat(result.flagged()).isFalse();
        assertThat(result.reason()).isNull();
    }

    @Test
    void moderateInput_shouldBeCaseInsensitive() {
        ContentModerator moderator = new DefaultContentModerator(List.of("forbidden"));
        ModerationResult result = moderator.moderateInput("This is FORBIDDEN content");
        assertThat(result.flagged()).isTrue();
    }

    @Test
    void moderateInput_shouldSupportRegex() {
        ContentModerator moderator = new DefaultContentModerator(List.of("暴力.*行为"));
        ModerationResult result = moderator.moderateInput("请描述暴力犯罪行为");
        assertThat(result.flagged()).isTrue();
    }

    // -------------------------------------------------------------------------
    // 输出审核
    // -------------------------------------------------------------------------

    @Test
    void moderateOutput_shouldBlock_whenKeywordInOutput() {
        ContentModerator moderator = new DefaultContentModerator(List.of("内部机密"));
        ModerationResult result = moderator.moderateOutput("这份报告包含内部机密信息");
        assertThat(result.flagged()).isTrue();
        assertThat(result.reason()).contains("输出");
    }

    @Test
    void moderateOutput_shouldPass_whenOutputIsClean() {
        ContentModerator moderator = new DefaultContentModerator(List.of("内部机密"));
        ModerationResult result = moderator.moderateOutput("设备列表：drone-001 在线");
        assertThat(result.flagged()).isFalse();
    }

    // -------------------------------------------------------------------------
    // 边界条件
    // -------------------------------------------------------------------------

    @Test
    void moderate_shouldPass_whenKeywordListIsEmpty() {
        ContentModerator moderator = new DefaultContentModerator(List.of());
        assertThat(moderator.moderateInput("anything").flagged()).isFalse();
        assertThat(moderator.moderateOutput("anything").flagged()).isFalse();
    }

    @Test
    void moderate_shouldPass_whenKeywordListIsNull() {
        ContentModerator moderator = new DefaultContentModerator(null);
        assertThat(moderator.moderateInput("anything").flagged()).isFalse();
    }

    @Test
    void moderate_shouldPass_whenTextIsNullOrBlank() {
        ContentModerator moderator = new DefaultContentModerator(List.of("keyword"));
        assertThat(moderator.moderateInput(null).flagged()).isFalse();
        assertThat(moderator.moderateInput("").flagged()).isFalse();
        assertThat(moderator.moderateInput("   ").flagged()).isFalse();
    }

    @Test
    void constructor_shouldSkipInvalidRegex() {
        ContentModerator moderator = new DefaultContentModerator(List.of("[invalid(", "有效词"));
        ModerationResult result = moderator.moderateInput("包含有效词");
        assertThat(result.flagged()).isTrue();
    }
}
