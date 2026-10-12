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

package com.dji.fh.feature.chat.rag;

import com.dji.fh.feature.chat.ChatProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * RAG（检索增强生成）AutoConfiguration。
 *
 * <p>条件：{@code fh.chat.enabled=true} 且 {@code fh.chat.rag.enabled=true}（两者均需显式配置，
 * 未配置时不启用）。
 * 在 {@code ChatAutoConfiguration} 之前加载（因为 ChatAutoConfiguration 依赖 VectorStore bean）。
 *
 * <p>注册以下 bean：
 * <ul>
 *   <li>{@link VectorStore} — 基于 SimpleVectorStore 的内存向量存储</li>
 *   <li>{@link DocumentInitializer} — 文档加载和向量化初始化器</li>
 *   <li>{@link KnowledgeAdvisor} — RAG 知识库检索 Advisor</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "fh.chat", name = {"enabled", "rag.enabled"}, havingValue = "true")
public class RagAutoConfiguration {

    /**
     * 注册基于 SimpleVectorStore 的内存向量存储。
     *
     * <p>SimpleVectorStore 是 Spring AI 提供的纯内存实现，适合 v1 原型阶段。
     * 生产环境可替换为 PgVector、Milvus 等外部向量数据库。
     *
     * @param embeddingModel 向量化模型，由对应 provider 的 starter 自动配置
     * @return VectorStore 实例
     */
    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore chatVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 注册文档初始化器。
     *
     * @param properties  Chat 配置属性，用于读取 documentsPath 配置
     * @param vectorStore 目标向量存储，文档块将被写入此处
     * @return DocumentInitializer 实例
     */
    @Bean
    @ConditionalOnMissingBean(DocumentInitializer.class)
    public DocumentInitializer documentInitializer(ChatProperties properties, VectorStore vectorStore) {
        String docsPath = properties.getRag().getDocumentsPath();
        // 若路径不含通配符，自动追加 ** / * .md 以扫描子目录下所有 Markdown 文件
        if (!docsPath.contains("*")) {
            docsPath = docsPath.endsWith("/") ? docsPath + "**/*.md" : docsPath + "/**/*.md";
        }
        return new DocumentInitializer(docsPath, vectorStore);
    }

    /**
     * 注册 KnowledgeAdvisor。
     *
     * @param vectorStore 向量存储，提供相似度检索能力
     * @param properties  Chat 配置属性，用于读取 topK 和 similarityThreshold
     * @return KnowledgeAdvisor 实例
     */
    @Bean
    @ConditionalOnMissingBean(KnowledgeAdvisor.class)
    public KnowledgeAdvisor knowledgeAdvisor(VectorStore vectorStore, ChatProperties properties) {
        ChatProperties.Rag rag = properties.getRag();
        return new KnowledgeAdvisor(vectorStore, rag.getTopK(), rag.getSimilarityThreshold());
    }
}
