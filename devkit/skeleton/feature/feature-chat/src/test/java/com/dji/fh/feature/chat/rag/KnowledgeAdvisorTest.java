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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KnowledgeAdvisor 单元测试。
 *
 * <p>使用存根 VectorStore 验证 RAG 检索注入逻辑：
 * <ul>
 *   <li>检索到相关文档时，system prompt 应包含"参考资料"section 和文档内容</li>
 *   <li>检索结果为空时，system prompt 保持不变，原样透传请求</li>
 *   <li>用户消息为空时，跳过检索，原样透传请求</li>
 * </ul>
 * </p>
 *
 * @author DJI DevKit Team
 */
class KnowledgeAdvisorTest {

    // -------------------------------------------------------------------------
    // 测试替身：可配置返回文档的 VectorStore 存根
    // -------------------------------------------------------------------------

    /**
     * 可编程 VectorStore 存根，用于控制 similaritySearch 的返回值。
     */
    static class StubVectorStore implements VectorStore {

        /** 预设的相似度搜索结果。 */
        private List<Document> searchResults = List.of();

        /** 记录最后一次收到的 SearchRequest，用于断言。 */
        private SearchRequest lastSearchRequest;

        /**
         * 设置下次 similaritySearch 调用的返回结果。
         *
         * @param results 待返回的文档列表
         */
        void setSearchResults(List<Document> results) {
            this.searchResults = results;
        }

        /**
         * 返回最后一次 similaritySearch 收到的请求。
         *
         * @return SearchRequest 对象
         */
        SearchRequest getLastSearchRequest() {
            return lastSearchRequest;
        }

        @Override
        public void add(List<Document> documents) {
            // 测试不需要
        }

        @Override
        public void delete(List<String> idList) {
            // 测试不需要
        }

        @Override
        public void delete(Filter.Expression filterExpression) {
            // 测试不需要
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            this.lastSearchRequest = request;
            return new ArrayList<>(searchResults);
        }
    }

    // -------------------------------------------------------------------------
    // 测试准备
    // -------------------------------------------------------------------------

    private static final int TEST_TOP_K = 3;
    private static final double TEST_SIMILARITY_THRESHOLD = 0.7;

    private StubVectorStore stubVectorStore;
    private KnowledgeAdvisor advisor;

    @BeforeEach
    void setUp() {
        stubVectorStore = new StubVectorStore();
        advisor = new KnowledgeAdvisor(stubVectorStore, TEST_TOP_K, TEST_SIMILARITY_THRESHOLD);
    }

    // -------------------------------------------------------------------------
    // 辅助方法
    // -------------------------------------------------------------------------

    /**
     * 构造含有指定 system message 和 user message 的 ChatClientRequest。
     *
     * @param systemText system prompt 文本
     * @param userText   用户消息文本
     * @return ChatClientRequest 实例
     */
    private ChatClientRequest buildRequest(String systemText, String userText) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemText),
                new UserMessage(userText)
        ));
        return ChatClientRequest.builder()
                .prompt(prompt)
                .context(Map.of())
                .build();
    }

    /**
     * 构造只含 user message（无 system message）的 ChatClientRequest。
     *
     * @param userText 用户消息文本
     * @return ChatClientRequest 实例
     */
    private ChatClientRequest buildRequestWithoutSystem(String userText) {
        Prompt prompt = new Prompt(List.of(new UserMessage(userText)));
        return ChatClientRequest.builder()
                .prompt(prompt)
                .context(Map.of())
                .build();
    }

    // -------------------------------------------------------------------------
    // 测试用例
    // -------------------------------------------------------------------------

    /**
     * 当 VectorStore 返回相关文档时，before() 应将文档内容注入到 system prompt 中，
     * 并在 system prompt 中包含"参考资料"标头和文档文本。
     */
    @Test
    void before_shouldInjectReferenceContext_whenRelevantDocumentsFound() {
        // 准备：设置两个相关文档
        Document doc1 = new Document("FlightHub SDK 提供 listenAircraftLocation 接口用于实时位置订阅");
        Document doc2 = new Document("调用 acquireFlightControlAuthority 需要先完成鉴权");
        stubVectorStore.setSearchResults(List.of(doc1, doc2));

        ChatClientRequest request = buildRequest("你是 DJI FlightHub 助手", "如何获取无人机位置？");

        // 执行
        ChatClientRequest enrichedRequest = advisor.before(request, null);

        // 断言：system prompt 应包含"参考资料"section
        String systemText = enrichedRequest.prompt().getSystemMessage().getText();
        assertThat(systemText)
                .as("system prompt 应包含参考资料标头")
                .contains(KnowledgeAdvisor.REFERENCE_SECTION_HEADER.strip());
        assertThat(systemText)
                .as("system prompt 应包含第一个文档的内容")
                .contains("listenAircraftLocation");
        assertThat(systemText)
                .as("system prompt 应包含第二个文档的内容")
                .contains("acquireFlightControlAuthority");
    }

    /**
     * 当 VectorStore 返回相关文档时，SearchRequest 应携带正确的 topK 和相似度阈值。
     */
    @Test
    void before_shouldPassCorrectSearchParameters_whenQuerying() {
        Document doc = new Document("测试文档");
        stubVectorStore.setSearchResults(List.of(doc));

        ChatClientRequest request = buildRequest("系统提示", "SDK 如何初始化？");
        advisor.before(request, null);

        SearchRequest capturedRequest = stubVectorStore.getLastSearchRequest();
        assertThat(capturedRequest).isNotNull();
        assertThat(capturedRequest.getTopK())
                .as("topK 应与构造函数参数一致")
                .isEqualTo(TEST_TOP_K);
        assertThat(capturedRequest.getSimilarityThreshold())
                .as("similarityThreshold 应与构造函数参数一致")
                .isEqualTo(TEST_SIMILARITY_THRESHOLD);
        assertThat(capturedRequest.getQuery())
                .as("检索 query 应为用户消息文本")
                .isEqualTo("SDK 如何初始化？");
    }

    /**
     * 当 VectorStore 返回空列表时，before() 应原样返回请求，不修改 system prompt。
     */
    @Test
    void before_shouldReturnUnchangedRequest_whenNoDocumentsFound() {
        // 准备：VectorStore 返回空结果
        stubVectorStore.setSearchResults(List.of());

        String originalSystemText = "你是 DJI FlightHub 助手，请专注回答无人机相关问题";
        ChatClientRequest request = buildRequest(originalSystemText, "今天天气怎么样？");

        // 执行
        ChatClientRequest resultRequest = advisor.before(request, null);

        // 断言：system prompt 保持不变
        String systemText = resultRequest.prompt().getSystemMessage().getText();
        assertThat(systemText)
                .as("无相关文档时，system prompt 不应被修改")
                .isEqualTo(originalSystemText);
        assertThat(systemText)
                .as("system prompt 不应包含参考资料标头")
                .doesNotContain("参考资料");
    }

    /**
     * 当请求中 user message 为空时，before() 应跳过检索，原样透传请求。
     */
    @Test
    void before_shouldSkipRetrieval_whenUserMessageIsBlank() {
        ChatClientRequest request = buildRequest("系统提示", "   ");

        ChatClientRequest resultRequest = advisor.before(request, null);

        // 断言：未调用 VectorStore
        assertThat(stubVectorStore.getLastSearchRequest())
                .as("用户消息为空时不应调用 VectorStore")
                .isNull();
        // 断言：system prompt 保持不变
        assertThat(resultRequest.prompt().getSystemMessage().getText())
                .isEqualTo("系统提示");
    }

    /**
     * after() 方法应原样返回响应，不做任何修改。
     */
    @Test
    void after_shouldReturnResponseUnchanged() {
        // after() 接收 null response 不抛异常，原样返回
        ChatClientResponse nullResponse = null;
        ChatClientResponse result = advisor.after(nullResponse, null);
        assertThat(result).isNull();
    }

    /**
     * getName() 应返回固定的 advisor 名称。
     */
    @Test
    void getName_shouldReturnAdvisorName() {
        assertThat(advisor.getName()).isEqualTo("KnowledgeAdvisor");
    }

    /**
     * extractUserQuery() 在 prompt 不含 user message 时应返回空字符串（Spring AI 行为）。
     * 空字符串会被 before() 方法的 isBlank() 检查拦截，跳过检索。
     */
    @Test
    void extractUserQuery_shouldReturnNull_whenNoUserMessage() {
        // 构造一个只有 system message 的 prompt
        Prompt promptWithOnlySystem = new Prompt(List.of(new SystemMessage("仅有系统消息")));
        ChatClientRequest request = ChatClientRequest.builder()
                .prompt(promptWithOnlySystem)
                .context(Map.of())
                .build();

        String query = advisor.extractUserQuery(request);

        // Spring AI Prompt.getUserMessage() 在无 user message 时返回空文本的 UserMessage，
        // 而非 null，故 extractUserQuery 返回空字符串
        assertThat(query)
                .as("无 user message 时，extractUserQuery 应返回空字符串（由 isBlank() 拦截）")
                .isNotNull()
                .isBlank();
    }

    /**
     * 验证"参考资料"section 追加在原始 system prompt 之后，而不是替换它。
     */
    @Test
    void before_shouldAppendReferenceAfterOriginalSystemPrompt() {
        String originalSystemText = "原始系统提示，不应被删除";
        Document doc = new Document("SDK 接口说明文档内容");
        stubVectorStore.setSearchResults(List.of(doc));

        ChatClientRequest request = buildRequest(originalSystemText, "如何使用 SDK？");
        ChatClientRequest enrichedRequest = advisor.before(request, null);

        String systemText = enrichedRequest.prompt().getSystemMessage().getText();
        // 原始 system prompt 应保留
        assertThat(systemText).contains(originalSystemText);
        // 参考资料应追加在后
        assertThat(systemText.indexOf(originalSystemText))
                .as("原始 system prompt 应出现在参考资料之前")
                .isLessThan(systemText.indexOf("参考资料"));
    }
}
