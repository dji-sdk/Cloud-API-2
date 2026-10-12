# cloudapi2-knowledge-ingest

知识库入库助手 — 将外部文档自动收录到 `knowledge/` 知识库。

## 功能

将用户提供的内容（文件/URL/文本/飞书文档/Confluence 页面）自动：
1. 获取并解析内容
2. 判断归属模块和分类
3. 格式化为 LLM 友好的标准结构
4. 写入 `knowledge/` 目录
5. 更新 `llms.txt` 索引
6. 重建 `llms-full.txt`
7. 提交到 Git

## 触发词

- `加到知识库`
- `更新知识库`
- `入库` / `ingest`
- `添加知识` / `知识库收录`

## 支持的输入源

| 来源 | 示例 |
|------|------|
| 本地文件 | `把 docs/新接口.md 加到知识库` |
| URL | `把 https://developer.dji.com/xxx 加到知识库` |
| 飞书文档 | `把飞书上这个文档入库 https://xxx.feishu.cn/docx/xxx` |
| Confluence | `把 Confluence 上 xxx 页面加到知识库` |
| 粘贴文本 | `把下面这段加到知识库：...` |

## 处理模式

| 模式 | 说明 | 触发方式 |
|------|------|---------|
| **重写**（默认） | AI 浓缩为 ≤2000 字 LLM 友好格式 | 默认行为 |
| **原样** | 仅加 frontmatter，内容不改 | 说"原样入库" |

## 使用示例

```
用户：把这个加到知识库 https://developer.dji.com/doc/wayline-spec

AI：📥 准备入库：
      标题：航线文件格式规范
      文件：knowledge/api-reference/wayline-file-spec.md
      标签：[wayline, kmz, file-format, specification]
      模式：重写（浓缩为 ≤2000 字 LLM 友好格式）
    确认？

用户：确认

AI：✅ 已入库并提交。
      文件：knowledge/api-reference/wayline-file-spec.md
      索引：llms.txt 已更新
      commit：a1b2c3d feat(knowledge): 收录 航线文件格式规范
```

## 知识库模块

入库时 AI 会自动判断内容归属以下模块之一：

| 模块 | 内容类型 |
|------|---------|
| `getting-started` | 初始化、环境搭建、快速入门 |
| `architecture` | 系统设计、架构决策 |
| `api-reference` | 接口、错误码、调用流程 |
| `scenarios` | 行业方案、业务场景 |
| `operations` | 部署、监控、故障恢复 |
| `migration` | 版本迁移、breaking changes |
| `faq` | 常见问题 |

## 依赖

- `knowledge/` 目录必须存在（由 knowledge base 初始化创建）
- `knowledge/llms.txt` 必须存在
- `scripts/build-llms-full.sh` 必须可执行
