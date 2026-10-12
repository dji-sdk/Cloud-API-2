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

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * RAG 知识库检索 Advisor。
 *
 * <p>在每次 LLM 调用前，从 {@link VectorStore} 检索与用户问题最相关的文档片段，
 * 将其作为"参考资料"注入到 system prompt 末尾，从而让 LLM 能够利用私有知识库
 * 生成更准确、更具体的回答。</p>
 *
 * <p>工作流程：
 * <ol>
 *   <li>从 {@link ChatClientRequest} 中提取用户最新消息作为检索查询</li>
 *   <li>使用 {@link VectorStore#similaritySearch(SearchRequest)} 检索 Top-K 相关文档</li>
 *   <li>若有匹配结果，将文档内容格式化后追加到 system prompt</li>
 *   <li>若无匹配结果，原样透传请求，不做任何修改</li>
 *   <li>调用链中的下一个 Advisor</li>
 * </ol>
 * </p>
 *
 * @author DJI DevKit Team
 */
public class KnowledgeAdvisor implements BaseAdvisor {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeAdvisor.class);

    /** Advisor 名称，用于日志和可观测性标识。 */
    private static final String ADVISOR_NAME = "KnowledgeAdvisor";

    /** 参考资料注入的 section 标头。 */
    static final String REFERENCE_SECTION_HEADER = "\n\n参考资料:\n";

    /** 参考资料分隔符，用于多个文档块之间。 */
    private static final String REFERENCE_SEPARATOR = "\n---\n";

    /** 默认 Advisor 执行顺序，在 memory advisor 之前执行。 */
    private static final int DEFAULT_ORDER = 0;

    /** 向量存储，提供文档相似度检索能力。 */
    private final VectorStore vectorStore;

    /** 检索返回的最大候选文档数。 */
    private final int topK;

    /** 相似度阈值，低于此值的结果将被过滤。 */
    private final double similarityThreshold;

    /**
     * 构造 KnowledgeAdvisor。
     *
     * @param vectorStore         向量存储实现
     * @param topK                检索返回的最大候选文档数
     * @param similarityThreshold 相似度阈值（0.0 ~ 1.0）
     */
    public KnowledgeAdvisor(VectorStore vectorStore, int topK, double similarityThreshold) {
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    /**
     * 返回 Advisor 的唯一名称。
     *
     * @return advisor 名称
     */
    @Override
    public String getName() {
        return ADVISOR_NAME;
    }

    /**
     * 返回 Advisor 的执行顺序。
     *
     * @return 顺序值（值越小越先执行）
     */
    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    /**
     * 请求前置处理：从 VectorStore 检索相关文档并注入 system prompt。
     *
     * <p>若检索结果为空，原样返回请求，不做任何修改。</p>
     *
     * @param request      当前 ChatClient 请求
     * @param advisorChain advisor 调用链上下文
     * @return 注入了参考资料的新请求（或原始请求）
     */
    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        String userQuery = extractUserQuery(request);
        if (userQuery == null || userQuery.isBlank()) {
            log.debug("KnowledgeAdvisor：用户问题为空，跳过 RAG 检索");
            return request;
        }

        List<Document> relevantDocs = retrieveRelevantDocuments(userQuery);
        if (relevantDocs.isEmpty()) {
            log.debug("KnowledgeAdvisor：未检索到相关文档（query='{}'），原样透传请求", userQuery);
            return request;
        }

        log.debug("KnowledgeAdvisor：检索到 {} 个相关文档，注入 system prompt", relevantDocs.size());
        return injectReferenceContext(request, relevantDocs);
    }

    /**
     * 响应后置处理：不做任何处理，原样透传响应。
     *
     * @param response     LLM 返回的响应
     * @param advisorChain advisor 调用链上下文
     * @return 原样响应
     */
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        return response;
    }

    /**
     * 从请求中提取用户最新消息文本，作为向量检索的查询词。
     *
     * @param request ChatClient 请求
     * @return 用户消息文本，若不存在则返回 null
     */
    String extractUserQuery(ChatClientRequest request) {
        if (request.prompt() == null || request.prompt().getUserMessage() == null) {
            return null;
        }
        return request.prompt().getUserMessage().getText();
    }

    /**
     * 在向量存储中检索与查询最相关的文档列表。
     *
     * @param query 检索查询词
     * @return 相关文档列表（按相似度排序），若无结果则返回空列表
     */
    List<Document> retrieveRelevantDocuments(String query) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();
            return vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.warn("KnowledgeAdvisor：向量检索失败，跳过 RAG 注入: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 将参考文档内容格式化后追加到 system prompt 末尾。
     *
     * @param request      原始请求
     * @param relevantDocs 检索到的相关文档列表
     * @return 注入了参考资料的新请求
     */
    private ChatClientRequest injectReferenceContext(ChatClientRequest request,
                                                     List<Document> relevantDocs) {
        String referenceContent = buildReferenceContent(relevantDocs);
        // 使用 Function 形式的 augmentSystemMessage，保留原始 system prompt 并追加参考资料
        return request.mutate()
                .prompt(request.prompt().augmentSystemMessage(
                        existing -> new SystemMessage(existing.getText() + referenceContent)
                ))
                .build();
    }

    /**
     * 将文档列表格式化为"参考资料"文本块。
     *
     * @param docs 文档列表
     * @return 格式化后的参考资料字符串
     */
    private String buildReferenceContent(List<Document> docs) {
        String docsText = docs.stream()
                .map(doc -> doc.getText() != null ? doc.getText() : "")
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(REFERENCE_SEPARATOR));
        return REFERENCE_SECTION_HEADER + docsText;
    }
}
