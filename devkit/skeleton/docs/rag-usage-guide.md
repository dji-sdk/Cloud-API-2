# RAG 知识库使用指南

> 适用版本：skeleton v0.1.0+  
> 前置条件：`fh.chat.enabled=true` + `fh.chat.rag.enabled=true`

## 概述

RAG（Retrieval-Augmented Generation）让 Chat Bot 在回答时自动检索你的私有知识库，将最相关的文档片段注入到 LLM 的上下文中，从而生成更准确、更贴合业务的回答。

**工作流程：**

```
用户提问 → 向量检索（VectorStore）→ Top-K 相关文档 → 注入 system prompt → LLM 生成回答
```

## 快速开始

### 1. 放入知识库文档

将你的 Markdown 文件放到 `feature-chat` 模块的资源目录下：

```
skeleton/feature/feature-chat/src/main/resources/chat-docs/
├── getting-started.md          ← 内置示例
├── flight-operations.md        ← 你的飞行操作手册
├── safety-guidelines.md        ← 安全规范
├── device-troubleshooting.md   ← 设备故障排查
└── subfolder/                  ← 支持子目录
    └── advanced-topics.md
```

**支持格式：** Markdown（`.md`）、纯文本（`.txt`）

**建议实践：**
- 每个文件聚焦一个主题（设备管理、飞行操作、告警处理等）
- 单文件建议 500~5000 字，过长会被自动分块
- 使用清晰的标题结构（`# / ## / ###`），有助于分块语义完整性
- FAQ 格式效果好：`Q: 问题\nA: 回答`

### 2. 启用配置

在 `application.yml` 中确保以下配置（默认已开启）：

```yaml
fh:
  chat:
    enabled: true           # 开启 Chat Bot
    rag:
      enabled: true         # 开启 RAG
      documents-path: classpath:chat-docs/   # 文档路径
      top-k: 5              # 每次检索返回的最大文档数
      similarity-threshold: 0.7   # 相似度阈值（0~1）
```

### 3. 配置 Embedding 模型

RAG 需要 Embedding 模型将文档和查询转为向量。当前支持 OpenAI 兼容的 embedding API：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com}   # 支持代理
      embedding:
        options:
          model: text-embedding-3-small   # 推荐，成本低精度够用
```

> **注意：** Embedding 模型和 Chat 模型可以来自不同 provider。Chat 用 Anthropic，Embedding 用 OpenAI 是常见组合。

### 4. 重启应用

```bash
# mock profile 同样需要 MySQL 8.0 与 FH_JWT_SECRET（详见 README「方式二：本地开发」）
export FH_SKELETON_PROFILE=mock
export FH_JWT_SECRET=$(openssl rand -hex 32)
export FH_DB_URL='jdbc:mysql://localhost:3306/fh_skeleton?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export FH_DB_USERNAME=root FH_DB_PASSWORD=your-mysql-password

mvn clean package -DskipTests
java -jar app/fh-skeleton-app/target/fh-skeleton-app-0.1.0.jar \
  --fh.chat.enabled=true
```

启动日志中会看到：
```
RAG 文档加载完成：读取 3 个文件，生成 12 个文档块
```

## 配置参数详解

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `fh.chat.rag.enabled` | `true` | 是否启用 RAG（需同时 `fh.chat.enabled=true`） |
| `fh.chat.rag.documents-path` | `classpath:chat-docs/` | 文档扫描路径，支持 Ant 通配符 |
| `fh.chat.rag.embedding-provider` | `openai` | Embedding 模型 provider |
| `fh.chat.rag.chunk-size` | `1000` | 文档分块大小（token 数） |
| `fh.chat.rag.chunk-overlap` | `200` | 分块重叠大小（token 数） |
| `fh.chat.rag.top-k` | `5` | 检索返回的最大候选文档数 |
| `fh.chat.rag.similarity-threshold` | `0.7` | 相似度阈值，低于此值的结果被过滤 |

### 路径配置示例

```yaml
# 默认：classpath 下的 chat-docs 目录
documents-path: classpath:chat-docs/

# 扫描所有子目录的 Markdown 文件（等效于默认行为，路径不含通配符时自动追加）
documents-path: classpath:chat-docs/**/*.md

# 外部文件系统路径（Docker 部署时挂载卷）
documents-path: file:/data/knowledge-base/
```

## 外部文档目录（推荐生产使用）

classpath 方式需要重新打包才能更新文档。生产环境建议使用外部目录：

```yaml
fh:
  chat:
    rag:
      documents-path: file:/data/fh-knowledge/
```

**Docker Compose 配置：**

```yaml
services:
  fh-skeleton:
    volumes:
      - ./knowledge-base:/data/fh-knowledge:ro
    environment:
      - FH_CHAT_RAG_DOCUMENTS_PATH=file:/data/fh-knowledge/
```

更新文档后重启应用即可生效（v1 暂不支持热加载）。

## 调优建议

### 检索效果不好？

| 现象 | 调整方式 |
|------|---------|
| 回答和文档无关 | 降低 `similarity-threshold`（如 0.5） |
| 回答遗漏了关键信息 | 增大 `top-k`（如 8~10） |
| 回答混入了不相关内容 | 提高 `similarity-threshold`（如 0.8） |
| 文档被截断导致语义不完整 | 增大 `chunk-size`（如 1500）+ 适当增大 `chunk-overlap` |

### 文档编写技巧

1. **明确的问答格式**：RAG 对 FAQ 风格文档效果最好
   ```markdown
   ## 设备离线处理
   Q: 设备显示离线怎么办？
   A: 依次检查：1) 网络连接 2) 机场供电 3) HMS 告警
   ```

2. **避免过度引用**：不要在文档中大段引用 SDK API 原文，用自然语言解释

3. **保持时效性**：定期更新文档，移除过时内容（向量库中的过时内容会干扰回答）

4. **分主题存放**：一个文件一个主题，避免一个大文件包含所有内容

## 禁用 RAG

如果不需要知识库增强（例如只需要设备操作能力），可以关闭：

```yaml
fh:
  chat:
    enabled: true
    rag:
      enabled: false    # 关闭 RAG，Chat Bot 仅使用 LLM 通用知识 + Tool
```

## 架构说明

```
┌──────────────────────────────────────────────────┐
│ ChatController (SSE Stream)                       │
├──────────────────────────────────────────────────┤
│ KnowledgeAdvisor (Spring AI Advisor)             │
│  ├─ 提取用户问题                                  │
│  ├─ 调用 VectorStore.similaritySearch()          │
│  └─ 将 Top-K 文档注入 system prompt 末尾         │
├──────────────────────────────────────────────────┤
│ SimpleVectorStore (内存向量库)                     │
│  └─ 应用启动时由 DocumentInitializer 填充        │
├──────────────────────────────────────────────────┤
│ DocumentInitializer                              │
│  ├─ 扫描 documents-path 下的文件                  │
│  ├─ TextReader 读取内容                           │
│  ├─ TokenTextSplitter 分块                       │
│  └─ 写入 VectorStore                            │
└──────────────────────────────────────────────────┘
```

## 已知限制（v0.1）

| 限制 | 说明 | 未来计划 |
|------|------|---------|
| 内存向量库 | 使用 SimpleVectorStore，重启后数据丢失需重建 | v0.2 支持 PgVector/Milvus |
| 启动时全量加载 | 文档多时启动变慢 | v0.2 增量加载 + 持久化 |
| 不支持热更新 | 更新文档需重启应用 | v0.2 文件 Watch + 增量索引 |
| 仅支持文本格式 | Markdown/TXT，不支持 PDF/Word | v0.2 扩展 DocumentReader |
| 固定分块策略 | TokenTextSplitter，无语义分块 | v0.2 按标题分块 |
