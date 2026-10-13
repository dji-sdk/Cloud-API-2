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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * RAG 文档初始化器。
 *
 * <p>在应用启动时，从 classpath 指定路径扫描 Markdown/文本文件，
 * 使用 {@link TokenTextSplitter} 分块后写入 {@link VectorStore}，
 * 为后续知识库检索提供基础数据。</p>
 *
 * <p>若指定路径不存在或扫描结果为空，记录警告日志并正常返回，不抛异常。</p>
 *
 * @author DJI DevKit Team
 */
public class DocumentInitializer {

    private static final Logger log = LoggerFactory.getLogger(DocumentInitializer.class);

    /** 文档扫描路径（支持 Ant 风格通配符，如 classpath:chat-docs/** /*.md）。 */
    private final String docsLocationPattern;

    /** 目标向量存储，文档块将被写入此处。 */
    private final VectorStore vectorStore;

    /** Classpath 资源扫描器。 */
    private final PathMatchingResourcePatternResolver resourceResolver;

    /** Token 文本分块器（使用默认参数：chunkSize=800, overlap=0）。 */
    private final TokenTextSplitter textSplitter;

    /**
     * 构造 DocumentInitializer。
     *
     * @param docsLocationPattern Ant 风格的 classpath 资源路径，例如 {@code classpath:chat-docs/**\/*.md}
     * @param vectorStore         目标向量存储实现
     */
    public DocumentInitializer(String docsLocationPattern, VectorStore vectorStore) {
        this.docsLocationPattern = docsLocationPattern;
        this.vectorStore = vectorStore;
        this.resourceResolver = new PathMatchingResourcePatternResolver();
        this.textSplitter = new TokenTextSplitter();
    }

    /**
     * 执行文档加载管道：扫描 → 读取 → 分块 → 入库。
     *
     * <p>幂等调用：重复调用会重复写入，调用方应保证只在启动时调用一次。</p>
     */
    public void initialize() {
        Resource[] resources = resolveResources();
        if (resources.length == 0) {
            log.warn("RAG 文档加载：路径 '{}' 下未找到任何文档，跳过初始化", docsLocationPattern);
            return;
        }

        List<Document> allDocuments = readDocuments(resources);
        if (allDocuments.isEmpty()) {
            log.warn("RAG 文档加载：所有资源读取后文档列表为空，跳过写入");
            return;
        }

        List<Document> chunks = textSplitter.apply(allDocuments);
        vectorStore.add(chunks);

        log.info("RAG 文档加载完成：读取 {} 个文件，生成 {} 个文档块", resources.length, chunks.size());
    }

    /**
     * 解析 classpath 资源，若路径无效或无匹配则返回空数组。
     *
     * @return 匹配的资源数组
     */
    private Resource[] resolveResources() {
        try {
            Resource[] resources = resourceResolver.getResources(docsLocationPattern);
            // 过滤掉目录资源（classpath 通配扫描可能返回目录）
            List<Resource> fileResources = new ArrayList<>();
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    fileResources.add(resource);
                }
            }
            return fileResources.toArray(new Resource[0]);
        } catch (IOException e) {
            log.warn("RAG 文档加载：扫描路径 '{}' 时发生 IO 异常，跳过初始化: {}",
                    docsLocationPattern, e.getMessage());
            return new Resource[0];
        }
    }

    /**
     * 逐个读取资源文件，汇总为文档列表。
     *
     * <p>单个文件读取失败时记录警告并跳过，不中断整体流程。</p>
     *
     * @param resources 待读取的资源数组
     * @return 所有成功读取的文档
     */
    private List<Document> readDocuments(Resource[] resources) {
        List<Document> documents = new ArrayList<>();
        for (Resource resource : resources) {
            try {
                TextReader reader = new TextReader(resource);
                List<Document> docs = reader.get();
                documents.addAll(docs);
                log.debug("RAG 文档加载：已读取 '{}', 生成 {} 个文档", resource.getDescription(), docs.size());
            } catch (Exception e) {
                log.warn("RAG 文档加载：读取资源 '{}' 失败，跳过: {}",
                        resource.getDescription(), e.getMessage());
            }
        }
        return documents;
    }
}
