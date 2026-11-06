# 安全策略 / Security Policy

[中文](#中文) · [English](#english)

---

## 中文

### 报告安全漏洞

**请不要通过公开 Issue、Discussion 或 Pull Request 报告安全漏洞。** 公开披露会让使用者在修复发布前暴露于风险之中。

请使用 GitHub 私密漏洞报告功能提交：

1. 打开本仓库的 **[Security](https://github.com/dji-sdk/Cloud-API-2/security)** 标签页
2. 点击 **Report a vulnerability**
3. 填写表单并提交

整个过程仅对您和仓库维护者可见。也可直接访问：
<https://github.com/dji-sdk/Cloud-API-2/security/advisories/new>

### 报告中请包含

为便于我们快速定位和复现，请尽量提供：

- **受影响的组件与版本**：SDK、前端组件、AI Plugin、设备模拟器、文档或示例代码，以及对应版本号
- **漏洞类型与影响**：例如凭据泄露、越权访问、远程代码执行等，以及攻击者可借此达成什么
- **复现步骤**：最小化的复现路径，必要时附上请求/响应、日志或截图
- **运行环境**：部署形态（私有化 / 本地调试）、操作系统、JDK 或浏览器版本
- **您认为的修复建议**（可选）

> 请勿在报告中附带真实的生产凭据、客户数据或设备序列号。如需说明，请做脱敏处理。

### 处理流程

| 阶段 | 说明 |
| --- | --- |
| 确认接收 | 我们会在收到报告后尽快回复确认 |
| 评估定级 | 复现问题并评估影响范围与严重程度 |
| 修复与验证 | 开发修复方案，必要时与您确认修复效果 |
| 发布与披露 | 随版本发布修复，并在合适时机公开安全公告 |

在修复发布前，我们希望与您协同保密。修复发布后，如您愿意，我们会在安全公告中致谢您的贡献。

### 适用范围

本策略适用于上云 API 2 开源仓库中发布的全部代码与文档，包括上云 API 2 SDK 源码、前端组件、AI Plugin 与 AI Coding 脚手架、设备模拟器、调试工具、开源 Demo，以及开发者套件（devkit：MCP Server、接入骨架 Skeleton、AI Skill 集合）。

DJI 硬件产品、大疆在线服务或其他 DJI 软件产品（含本仓库）的漏洞，也可以通过 [DJI 安全响应中心](https://security.dji.com/) 报告。

---

## English

### Reporting a Vulnerability

**Please do not report security vulnerabilities through public issues, discussions, or pull requests.** Public disclosure puts users at risk before a fix is available.

Please use GitHub's private vulnerability reporting:

1. Go to the **[Security](https://github.com/dji-sdk/Cloud-API-2/security)** tab of this repository
2. Click **Report a vulnerability**
3. Fill in and submit the form

The report is visible only to you and the maintainers. You can also go directly to:
<https://github.com/dji-sdk/Cloud-API-2/security/advisories/new>

### What to Include

To help us triage and reproduce quickly, please provide where possible:

- **Affected component and version**: SDK, frontend components, AI Plugin, device simulator, documentation, or sample code, with version numbers
- **Vulnerability type and impact**: e.g. credential exposure, privilege escalation, remote code execution — and what an attacker could achieve
- **Steps to reproduce**: a minimal reproduction path, with requests/responses, logs, or screenshots if relevant
- **Environment**: deployment type (on-premises / local development), OS, JDK or browser version
- **Suggested remediation** (optional)

> Please do not include real production credentials, customer data, or device serial numbers in your report. Redact them if they are needed for context.

### Process

| Stage | Description |
| --- | --- |
| Acknowledgement | We will confirm receipt of your report as soon as we can |
| Assessment | We reproduce the issue and assess its scope and severity |
| Fix and verification | We develop a fix and, where helpful, confirm it with you |
| Release and disclosure | The fix ships with a release, and we publish an advisory when appropriate |

We ask that you keep the report confidential until a fix is released. Once it is, we are happy to credit you in the advisory if you would like.

### Scope

This policy covers all code and documentation published in the Cloud API 2 open-source repository, including the Cloud API 2 SDK source, frontend components, AI Plugin and AI Coding scaffolding, device simulator, debugging tools, open-source demos, and the developer kit (devkit: MCP Server, integration skeleton, and AI Skill collection).

Vulnerabilities in DJI hardware products, DJI online services, or other DJI software — including this repository — can also be reported through the [DJI Security Response Center](https://security.dji.com/).
