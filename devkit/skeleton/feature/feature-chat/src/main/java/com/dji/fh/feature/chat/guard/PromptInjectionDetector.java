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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dji.fh.feature.chat.controller.ChatRequest;

/**
 * Prompt 注入检测器 — 基于正则规则扫描用户输入和历史消息中的注入模式。
 *
 * <p>内置 7 条覆盖主流攻击向量的规则，支持通过配置追加自定义正则。
 * 检测到注入模式时返回 {@link GuardResult#blocked}，否则返回 {@link GuardResult#pass()}。
 *
 * <p>安全加固：检测前自动剥离零宽字符和不可见 Unicode，防止绕过检测。
 *
 * <p>线程安全：所有 Pattern 在构造时编译并存储为不可变列表，无共享可变状态。
 *
 * @author DJI DevKit Team
 */
public class PromptInjectionDetector {

    private static final Logger LOG = LoggerFactory.getLogger(PromptInjectionDetector.class);

    /** 零宽/不可见字符正则 — 预处理时剥离，防止插入隐形字符绕过关键词匹配。 */
    private static final Pattern INVISIBLE_CHARS = Pattern.compile(
            "[\\u200B\\u200C\\u200D\\u200E\\u200F\\u2060\\u2061\\u2062\\u2063\\u2064"
            + "\\uFEFF\\u00AD\\u034F\\u061C\\u115F\\u1160\\u17B4\\u17B5"
            + "\\u180E\\u2000-\\u200A\\u202A-\\u202E\\u2066-\\u2069\\uFFF9-\\uFFFB]");

    /** 内置规则：忽略/无视/遗忘前文指令。 */
    private static final Pattern IGNORE_PREVIOUS = Pattern.compile(
            "(?i)(ignore|disregard|forget|忽略|无视|忘记|忽\\s*略|无\\s*视)\\s*(all\\s*)?"
            + "(previous|prior|above|之前的|以上的)?\\s*(instructions|prompts|rules|指令|规则|提示)",
            Pattern.UNICODE_CHARACTER_CLASS);

    /** 内置规则：试图获取系统提示词。 */
    private static final Pattern SYSTEM_PROMPT_LEAK = Pattern.compile(
            "(?i)(system\\s*prompt|系统提示词|系统指令|输出.*初始指令|repeat.*instructions|print.*prompt|show.*system|reveal.*prompt|display.*instructions)",
            Pattern.UNICODE_CHARACTER_CLASS);

    /** 内置规则：中文角色劫持（"你是一个X"，但不含"助手"/"客服"等合法描述）。 */
    private static final Pattern CN_ROLE_HIJACK = Pattern.compile(
            "(?i)(你现在是|你扮演|假装你是|从现在开始你是|请?忘记你是|你不再是)(?!.*(助手|客服|FlightHub))");

    /** 内置规则：英文角色劫持。 */
    private static final Pattern EN_ROLE_HIJACK = Pattern.compile(
            "(?i)(pretend|act|imagine|roleplay|suppose|behave)\\s+(you\\s+are|to\\s+be|as\\s+if|that\\s+you|like\\s+you)",
            Pattern.UNICODE_CHARACTER_CLASS);

    /** 内置规则：分隔符注入（尝试用代码块模拟 system/assistant 角色）。 */
    private static final Pattern DELIMITER_INJECTION = Pattern.compile(
            "(```|---{3,}|\\[SYSTEM\\]|\\[INST\\]|<\\|im_start\\|>)\\s*(system|assistant|prompt|指令|角色)",
            Pattern.CASE_INSENSITIVE);

    /** 内置规则：unicode/base64 编码绕过尝试。 */
    private static final Pattern ENCODING_BYPASS = Pattern.compile(
            "(\\\\u[0-9a-fA-F]{4}.*?(ignore|system|prompt|指令))|"
            + "(base64\\s*[.:：])|"
            + "(eval\\s*\\()|"
            + "(\\{\\{.*?(system|prompt).*?\\}\\})",
            Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);

    /** 内置规则：已知越狱模式名。 */
    private static final Pattern JAILBREAK_NAMES = Pattern.compile(
            "(?i)(jailbreak|DAN\\s*mode|do\\s+anything\\s+now|STAN|DUDE|evil\\s+mode|developer\\s+mode|grandma\\s+exploit|pliny)",
            Pattern.UNICODE_CHARACTER_CLASS);

    /** 所有规则名称（与 ALL_PATTERNS 一一对应，用于日志记录）。 */
    private static final List<String> RULE_NAMES = List.of(
            "ignore_previous", "system_prompt_leak", "cn_role_hijack",
            "en_role_hijack", "delimiter_injection", "encoding_bypass", "jailbreak_names"
    );

    /** 内置规则列表。 */
    private static final List<Pattern> DEFAULT_PATTERNS = List.of(
            IGNORE_PREVIOUS, SYSTEM_PROMPT_LEAK, CN_ROLE_HIJACK,
            EN_ROLE_HIJACK, DELIMITER_INJECTION, ENCODING_BYPASS, JAILBREAK_NAMES
    );

    private final List<Pattern> allPatterns;
    private final List<String> allRuleNames;

    /**
     * 构造 Prompt 注入检测器。
     *
     * @param extraPatterns 用户自定义的附加正则表达式列表（可为空）
     */
    public PromptInjectionDetector(List<String> extraPatterns) {
        List<Pattern> combined = new ArrayList<>(DEFAULT_PATTERNS);
        List<String> names = new ArrayList<>(RULE_NAMES);

        if (extraPatterns != null) {
            for (int i = 0; i < extraPatterns.size(); i++) {
                String raw = extraPatterns.get(i);
                try {
                    combined.add(Pattern.compile(raw, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS));
                    names.add("custom_" + i);
                } catch (Exception e) {
                    LOG.warn("PromptInjectionDetector：自定义规则编译失败，已跳过 pattern='{}' error={}",
                            raw, e.getMessage());
                }
            }
        }
        this.allPatterns = List.copyOf(combined);
        this.allRuleNames = List.copyOf(names);
    }

    /**
     * 检查单条消息是否包含注入模式。
     *
     * <p>检测前自动剥离零宽/不可见 Unicode 字符，防止绕过。
     *
     * @param message 待检查的文本
     * @return 检查结果
     */
    public GuardResult check(String message) {
        if (message == null || message.isBlank()) {
            return GuardResult.pass();
        }
        String sanitized = sanitize(message);
        for (int i = 0; i < allPatterns.size(); i++) {
            if (allPatterns.get(i).matcher(sanitized).find()) {
                String ruleName = allRuleNames.get(i);
                LOG.warn("PromptInjectionDetector：检测到注入模式 rule={} snippet='{}'",
                        ruleName, truncate(message, 100));
                return GuardResult.blocked(
                        GuardResult.CODE_INJECTION_DETECTED,
                        "检测到潜在的 Prompt 注入，请求已被拦截");
            }
        }
        return GuardResult.pass();
    }

    /**
     * 检查当前消息及所有历史消息。
     *
     * @param currentMessage 本轮用户消息
     * @param history        历史消息列表（可为 null）
     * @return 检查结果（任何一条消息命中即拦截）
     */
    public GuardResult checkAll(String currentMessage, List<ChatRequest.HistoryMessage> history) {
        GuardResult result = check(currentMessage);
        if (!result.allowed()) {
            return result;
        }

        if (history != null) {
            for (ChatRequest.HistoryMessage msg : history) {
                if (msg != null && msg.getContent() != null) {
                    result = check(msg.getContent());
                    if (!result.allowed()) {
                        return result;
                    }
                }
            }
        }
        return GuardResult.pass();
    }

    /**
     * 剥离零宽/不可见字符并折叠连续空白，防止通过隐形字符拆散关键词绕过检测。
     */
    private static String sanitize(String text) {
        String stripped = INVISIBLE_CHARS.matcher(text).replaceAll("");
        return stripped.replaceAll("[\\r\\n]+", " ");
    }

    private static String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...";
    }
}
