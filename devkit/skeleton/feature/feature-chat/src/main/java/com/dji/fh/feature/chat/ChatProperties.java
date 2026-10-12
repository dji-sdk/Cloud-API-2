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

package com.dji.fh.feature.chat;

import java.util.List;
import java.util.Set;

import com.dji.fh.feature.chat.provider.ProviderType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Chat Bot 配置属性，前缀 {@code fh.chat}。
 *
 * <p>通过 Spring Boot {@code @ConfigurationProperties} 机制绑定，
 * 可在 {@code application.yml} 中以 {@code fh.chat.*} 覆盖所有默认值。
 *
 * @author DJI DevKit Team
 */
@ConfigurationProperties(prefix = "fh.chat")
public class ChatProperties {

    /** 是否启用 Chat Bot 功能，默认关闭 */
    private boolean enabled = false;

    /** 默认 LLM provider 类型，默认使用 OpenAI 兼容协议接入（适配 GLM 等模型网关） */
    private ProviderType provider = ProviderType.OPENAI;

    /** 最多带入的历史对话轮数，默认 10 轮 */
    private int maxHistoryTurns = 10;

    /**
     * 单次回复最大 token 数，默认 8192。
     *
     * <p>GLM-5.2 等 reasoning 模型会先消耗 token 生成思维链（reasoning），再产出正式 content；
     * 若 max-tokens 过小，token 易被 reasoning 耗尽导致 content 为空、finish_reason=length 截断。
     * 故默认上调至 8192，可按需通过 {@code fh.chat.max-tokens} 覆盖。
     */
    private int maxTokens = 8192;

    /** system prompt 文件路径，默认从 classpath 加载 */
    private String systemPromptPath = "classpath:chat-system-prompt.txt";

    /**
     * 轻量模型，用于 L0 只读查询。
     * 默认使用 deepseek-v4-flash-private（tool calling 能力强），可通过 FH_CHAT_LIGHT_MODEL 环境变量覆盖。
     */
    private String lightModel = "deepseek-v4-flash-private";

    /** RAG 子配置 */
    private Rag rag = new Rag();

    /** 安全防护子配置 */
    private Guard guard = new Guard();

    /** 资源配额子配置 */
    private Quota quota = new Quota();

    /** tool 访问控制子配置 */
    private ToolAccess toolAccess = new ToolAccess();

    /**
     * 返回 tool 访问控制子配置。
     *
     * @return ToolAccess 配置对象
     */
    public ToolAccess getToolAccess() {
        return toolAccess;
    }

    /**
     * 设置 tool 访问控制子配置。
     *
     * @param toolAccess ToolAccess 配置对象
     */
    public void setToolAccess(ToolAccess toolAccess) {
        this.toolAccess = toolAccess;
    }

    /**
     * 返回是否启用 Chat Bot 功能。
     *
     * @return 启用标志
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 Chat Bot 功能。
     *
     * @param enabled 启用标志
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 返回当前配置的 LLM provider 类型。
     *
     * @return provider 类型
     */
    public ProviderType getProvider() {
        return provider;
    }

    /**
     * 设置 LLM provider 类型。
     *
     * @param provider provider 类型
     */
    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    /**
     * 返回最多带入的历史对话轮数。
     *
     * @return 历史轮数上限
     */
    public int getMaxHistoryTurns() {
        return maxHistoryTurns;
    }

    /**
     * 设置最多带入的历史对话轮数。
     *
     * @param maxHistoryTurns 历史轮数上限
     */
    public void setMaxHistoryTurns(int maxHistoryTurns) {
        this.maxHistoryTurns = maxHistoryTurns;
    }

    /**
     * 返回单次回复最大 token 数。
     *
     * @return 最大 token 数
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    /**
     * 设置单次回复最大 token 数。
     *
     * @param maxTokens 最大 token 数
     */
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     * 返回 system prompt 文件路径。
     *
     * @return 路径字符串（支持 classpath: 前缀）
     */
    public String getSystemPromptPath() {
        return systemPromptPath;
    }

    /**
     * 设置 system prompt 文件路径。
     *
     * @param systemPromptPath 路径字符串
     */
    public void setSystemPromptPath(String systemPromptPath) {
        this.systemPromptPath = systemPromptPath;
    }

    /**
     * 返回轻量模型名称（用于 L0 只读查询）。
     *
     * @return 模型 ID 字符串
     */
    public String getLightModel() {
        return lightModel;
    }

    /**
     * 设置轻量模型名称。
     *
     * @param lightModel 模型 ID 字符串
     */
    public void setLightModel(String lightModel) {
        this.lightModel = lightModel;
    }

    /**
     * 返回 RAG 子配置。
     *
     * @return RAG 配置对象
     */
    public Rag getRag() {
        return rag;
    }

    /**
     * 设置 RAG 子配置。
     *
     * @param rag RAG 配置对象
     */
    public void setRag(Rag rag) {
        this.rag = rag;
    }

    /**
     * 返回安全防护子配置。
     *
     * @return Guard 配置对象
     */
    public Guard getGuard() {
        return guard;
    }

    /**
     * 设置安全防护子配置。
     *
     * @param guard Guard 配置对象
     */
    public void setGuard(Guard guard) {
        this.guard = guard;
    }

    /**
     * 返回资源配额子配置。
     *
     * @return Quota 配置对象
     */
    public Quota getQuota() {
        return quota;
    }

    /**
     * 设置资源配额子配置。
     *
     * @param quota Quota 配置对象
     */
    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    /**
     * RAG（检索增强生成）子配置。
     *
     * <p>控制文档向量化、分块策略和检索参数。
     *
     * @author DJI DevKit Team
     */
    public static class Rag {

        /** 是否启用 RAG，默认开启 */
        private boolean enabled = true;

        /** 文档存储路径，默认从 classpath 读取 */
        private String documentsPath = "classpath:chat-docs/";

        /** 向量化使用的 embedding provider */
        private String embeddingProvider = "openai";

        /** 文档分块大小（token 数），默认 1000 */
        private int chunkSize = 1000;

        /** 分块重叠大小（token 数），默认 200 */
        private int chunkOverlap = 200;

        /** 检索返回的最大候选文档数，默认 5 */
        private int topK = 5;

        /** 相似度阈值，低于此值的结果将被过滤，默认 0.7 */
        private double similarityThreshold = 0.7;

        /**
         * 返回是否启用 RAG。
         *
         * @return 启用标志
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置是否启用 RAG。
         *
         * @param enabled 启用标志
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 返回文档存储路径。
         *
         * @return 文档路径
         */
        public String getDocumentsPath() {
            return documentsPath;
        }

        /**
         * 设置文档存储路径。
         *
         * @param documentsPath 文档路径
         */
        public void setDocumentsPath(String documentsPath) {
            this.documentsPath = documentsPath;
        }

        /**
         * 返回 embedding provider 名称。
         *
         * @return provider 名称
         */
        public String getEmbeddingProvider() {
            return embeddingProvider;
        }

        /**
         * 设置 embedding provider 名称。
         *
         * @param embeddingProvider provider 名称
         */
        public void setEmbeddingProvider(String embeddingProvider) {
            this.embeddingProvider = embeddingProvider;
        }

        /**
         * 返回文档分块大小。
         *
         * @return 分块大小（token 数）
         */
        public int getChunkSize() {
            return chunkSize;
        }

        /**
         * 设置文档分块大小。
         *
         * @param chunkSize 分块大小（token 数）
         */
        public void setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
        }

        /**
         * 返回分块重叠大小。
         *
         * @return 重叠大小（token 数）
         */
        public int getChunkOverlap() {
            return chunkOverlap;
        }

        /**
         * 设置分块重叠大小。
         *
         * @param chunkOverlap 重叠大小（token 数）
         */
        public void setChunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
        }

        /**
         * 返回检索返回的最大候选文档数。
         *
         * @return topK 值
         */
        public int getTopK() {
            return topK;
        }

        /**
         * 设置检索返回的最大候选文档数。
         *
         * @param topK topK 值
         */
        public void setTopK(int topK) {
            this.topK = topK;
        }

        /**
         * 返回相似度阈值。
         *
         * @return 阈值（0.0 ~ 1.0）
         */
        public double getSimilarityThreshold() {
            return similarityThreshold;
        }

        /**
         * 设置相似度阈值。
         *
         * @param similarityThreshold 阈值（0.0 ~ 1.0）
         */
        public void setSimilarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
        }
    }

    /**
     * 安全防护子配置，前缀 {@code fh.chat.guard}。
     *
     * <p>控制 Prompt 注入检测和内容审核的开关及参数。
     *
     * @author DJI DevKit Team
     */
    public static class Guard {

        /** 安全防护总开关，默认关闭 */
        private boolean enabled = false;

        /** Prompt 注入检测子配置 */
        private Injection injection = new Injection();

        /** 内容审核子配置 */
        private Moderation moderation = new Moderation();

        /**
         * 返回是否启用安全防护。
         *
         * @return 启用标志
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置是否启用安全防护。
         *
         * @param enabled 启用标志
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 返回 Prompt 注入检测子配置。
         *
         * @return Injection 配置对象
         */
        public Injection getInjection() {
            return injection;
        }

        /**
         * 设置 Prompt 注入检测子配置。
         *
         * @param injection Injection 配置对象
         */
        public void setInjection(Injection injection) {
            this.injection = injection;
        }

        /**
         * 返回内容审核子配置。
         *
         * @return Moderation 配置对象
         */
        public Moderation getModeration() {
            return moderation;
        }

        /**
         * 设置内容审核子配置。
         *
         * @param moderation Moderation 配置对象
         */
        public void setModeration(Moderation moderation) {
            this.moderation = moderation;
        }

        /**
         * Prompt 注入检测配置。
         */
        public static class Injection {

            /** 是否启用注入检测，默认开启（guard 总开关开时生效） */
            private boolean enabled = true;

            /** 用户自定义附加正则表达式列表 */
            private List<String> extraPatterns = List.of();

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public List<String> getExtraPatterns() {
                return extraPatterns;
            }

            public void setExtraPatterns(List<String> extraPatterns) {
                this.extraPatterns = extraPatterns;
            }
        }

        /**
         * 内容审核配置。
         */
        public static class Moderation {

            /** 是否启用内容审核，默认开启（guard 总开关开时生效） */
            private boolean enabled = true;

            /** 敏感词列表（支持正则语法） */
            private List<String> keywords = List.of();

            /** 是否启用输出侧审核，默认开启 */
            private boolean outputCheckEnabled = true;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public List<String> getKeywords() {
                return keywords;
            }

            public void setKeywords(List<String> keywords) {
                this.keywords = keywords;
            }

            public boolean isOutputCheckEnabled() {
                return outputCheckEnabled;
            }

            public void setOutputCheckEnabled(boolean outputCheckEnabled) {
                this.outputCheckEnabled = outputCheckEnabled;
            }
        }
    }

    /**
     * 资源配额子配置，前缀 {@code fh.chat.quota}。
     *
     * <p>控制 per-user 调用频率、并发流数和日 token 额度。
     *
     * @author DJI DevKit Team
     */
    public static class Quota {

        /** 配额总开关，默认关闭 */
        private boolean enabled = false;

        /** 每小时最大调用次数，默认 100 */
        private int maxCallsPerHour = 100;

        /** 单用户最大并发流数，默认 3 */
        private int maxConcurrentStreams = 3;

        /** 单用户日 token 额度，默认 500000（0 表示不限制） */
        private long maxDailyTokens = 500000L;

        /**
         * 返回是否启用配额。
         *
         * @return 启用标志
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置是否启用配额。
         *
         * @param enabled 启用标志
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 返回每小时最大调用次数。
         *
         * @return 调用次数上限
         */
        public int getMaxCallsPerHour() {
            return maxCallsPerHour;
        }

        /**
         * 设置每小时最大调用次数。
         *
         * @param maxCallsPerHour 调用次数上限
         */
        public void setMaxCallsPerHour(int maxCallsPerHour) {
            this.maxCallsPerHour = maxCallsPerHour;
        }

        /**
         * 返回单用户最大并发流数。
         *
         * @return 并发数上限
         */
        public int getMaxConcurrentStreams() {
            return maxConcurrentStreams;
        }

        /**
         * 设置单用户最大并发流数。
         *
         * @param maxConcurrentStreams 并发数上限
         */
        public void setMaxConcurrentStreams(int maxConcurrentStreams) {
            this.maxConcurrentStreams = maxConcurrentStreams;
        }

        /**
         * 返回单用户日 token 额度。
         *
         * @return 日 token 上限（0 表示不限制）
         */
        public long getMaxDailyTokens() {
            return maxDailyTokens;
        }

        /**
         * 设置单用户日 token 额度。
         *
         * @param maxDailyTokens 日 token 上限
         */
        public void setMaxDailyTokens(long maxDailyTokens) {
            this.maxDailyTokens = maxDailyTokens;
        }
    }

    /**
     * Tool 访问控制子配置，前缀 {@code fh.chat.tool-access}。
     *
     * <p><b>安全发布专项修复</b>：DEVICE_CONTROL（L1）级工具
     * 必须由<b>已认证角色</b>调用 — 未登录匿名及普通 user 角色默认禁止执行
     * 起飞/返航/航线等设备控制指令。
     *
     * @author DJI DevKit Team
     */
    public static class ToolAccess {

        /** 允许调用 DEVICE_CONTROL（L1）级工具的角色列表（默认仅 admin） */
        private Set<String> deviceControlRoles = Set.of("admin");

        /**
         * 返回允许调用 DEVICE_CONTROL 级工具的角色。
         *
         * @return 角色集合（不可变）
         */
        public Set<String> getDeviceControlRoles() {
            return deviceControlRoles;
        }

        /**
         * 设置允许调用 DEVICE_CONTROL 级工具的角色。
         *
         * @param deviceControlRoles 角色集合
         */
        public void setDeviceControlRoles(Set<String> deviceControlRoles) {
            this.deviceControlRoles = deviceControlRoles == null ? Set.of() : Set.copyOf(deviceControlRoles);
        }
    }
}
