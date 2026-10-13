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

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * DocumentInitializer 单元测试。
 *
 * <p>使用内存存根 VectorStore，验证文档加载管道的正确性：
 * <ul>
 *   <li>从 classpath:chat-docs/ 成功加载文档并入库</li>
 *   <li>指向不存在路径时不抛异常，优雅降级</li>
 * </ul>
 * </p>
 *
 * @author DJI DevKit Team
 */
class DocumentInitializerTest {

    // -------------------------------------------------------------------------
    // 测试替身：记录所有 add() 调用的 VectorStore 存根
    // -------------------------------------------------------------------------

    /**
     * 简单的 VectorStore 存根，只记录加入的文档，不做向量化。
     */
    static class CapturingVectorStore implements VectorStore {

        private final List<Document> capturedDocuments = new ArrayList<>();

        @Override
        public void add(List<Document> documents) {
            capturedDocuments.addAll(documents);
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
            return List.of();
        }

        /**
         * 返回所有被捕获的文档（不可修改副本）。
         *
         * @return 已入库文档列表
         */
        public List<Document> getCapturedDocuments() {
            return List.copyOf(capturedDocuments);
        }
    }

    // -------------------------------------------------------------------------
    // 测试用例
    // -------------------------------------------------------------------------

    /**
     * 从 classpath:chat-docs/ 加载文档后，VectorStore 应收到包含关键词的文档块。
     */
    @Test
    void initialize_shouldLoadDocumentsFromClasspath() {
        CapturingVectorStore vectorStore = new CapturingVectorStore();
        DocumentInitializer initializer = new DocumentInitializer(
                "classpath:chat-docs/**/*.md",
                vectorStore
        );

        initializer.initialize();

        List<Document> docs = vectorStore.getCapturedDocuments();
        assertThat(docs).isNotEmpty();

        // getting-started.md 包含 "FlightHub"，切分后至少一个 chunk 应含有该关键词
        boolean containsFlightHub = docs.stream()
                .anyMatch(doc -> doc.getText() != null && doc.getText().contains("FlightHub"));
        assertThat(containsFlightHub)
                .as("至少一个文档块应包含 'FlightHub' 关键词")
                .isTrue();
    }

    /**
     * 路径不存在时，initialize() 不应抛出异常（优雅降级，打印警告）。
     */
    @Test
    void initialize_emptyPath_shouldNotThrow() {
        CapturingVectorStore vectorStore = new CapturingVectorStore();
        DocumentInitializer initializer = new DocumentInitializer(
                "classpath:nonexistent-path/**/*.md",
                vectorStore
        );

        assertThatNoException().isThrownBy(initializer::initialize);
    }
}
