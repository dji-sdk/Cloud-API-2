# CodeMeter Runtime 包

此目录存放 Wibu CodeMeter Runtime 的安装包，用于私有化构建时打入运行时镜像。

> **注意**：`.deb` 文件因 Wibu 再分发条款限制，不随 git 仓库分发（已加入 `.gitignore`）。
> 请按下方说明手动获取并放置到正确目录。再分发条款见 `x86/NOTICE`。

## 所需文件

- `x86/codemeter-lite_8.10.6221.500_amd64.deb` — CodeMeter Lite Runtime (x86_64)

## 获取方式

从 Wibu Systems 官方下载或联系运维团队获取，版本需与 CI 镜像 `your-registry.example.com/private/build/wibu:v0.2` 中的 AxProtector 版本匹配。

## 放置方式

```bash
cp /path/to/codemeter-lite_8.10.6221.500_amd64.deb skeleton/packages/x86/
```
