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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认内容审核实现 — 基于关键词正则匹配。
 *
 * <p>将配置的关键词列表编译为大小写不敏感的正则表达式，
 * 对输入和输出文本执行相同的匹配逻辑。
 *
 * <p>线程安全：编译后的 Pattern 不可变，无共享可变状态。
 *
 * @author DJI DevKit Team
 */
public class DefaultContentModerator implements ContentModerator {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultContentModerator.class);

    private final List<KeywordEntry> keywords;

    /**
     * 构造默认内容审核器。
     *
     * @param keywordList 敏感词列表（支持正则语法，大小写不敏感）
     */
    public DefaultContentModerator(List<String> keywordList) {
        if (keywordList == null || keywordList.isEmpty()) {
            this.keywords = List.of();
        } else {
            this.keywords = keywordList.stream()
                    .filter(k -> k != null && !k.isBlank())
                    .map(k -> {
                        try {
                            Pattern pattern = Pattern.compile(k,
                                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
                            return new KeywordEntry(k, pattern);
                        } catch (Exception e) {
                            LOG.warn("DefaultContentModerator：关键词编译失败，已跳过 keyword='{}' error={}",
                                    k, e.getMessage());
                            return null;
                        }
                    })
                    .filter(entry -> entry != null)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    @Override
    public ModerationResult moderateInput(String text) {
        return doModerate(text, "输入");
    }

    @Override
    public ModerationResult moderateOutput(String text) {
        return doModerate(text, "输出");
    }

    private ModerationResult doModerate(String text, String direction) {
        if (text == null || text.isBlank() || keywords.isEmpty()) {
            return ModerationResult.safe();
        }
        for (KeywordEntry entry : keywords) {
            if (entry.pattern.matcher(text).find()) {
                LOG.warn("DefaultContentModerator：{}内容命中敏感词 keyword='{}'", direction, entry.raw);
                return ModerationResult.blocked(
                        direction + "内容包含敏感信息，已被拦截",
                        entry.raw);
            }
        }
        return ModerationResult.safe();
    }

    private record KeywordEntry(String raw, Pattern pattern) {
    }
}
