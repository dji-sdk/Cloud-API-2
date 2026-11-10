# CodeMeter Runtime 包

此目录存放 Wibu CodeMeter Runtime 的安装包，用于私有化构建时打入运行时镜像。

> **注意**：`.deb` 文件因 Wibu 再分发条款限制，不随 git 仓库分发（已加入 `.gitignore`）。
> 请按下方说明手动获取并放置到正确目录。

## 所需文件

- `x86/codemeter-lite_8.10.6221.500_amd64.deb` — CodeMeter Lite Runtime (x86_64)

## 获取方式

从 Wibu Systems 官方下载或联系运维团队获取，版本需与 CI 构建镜像中的 AxProtector 版本匹配。

私有化运行时镜像构建时会读取 `x86/` 下该文件，因此构建前必须已按下方「放置方式」准备到位，
否则镜像构建阶段会因找不到文件而失败。`.deb` 不随仓库分发，请自行保管并在每次构建前放置。

升级版本时，需同步修改构建配置中引用的 `.deb` 文件名，保持与实际放置的文件名一致。

## 放置方式（仅本地构建需要）

```bash
mkdir -p mcp/packages/x86
cp /path/to/codemeter-lite_8.10.6221.500_amd64.deb mcp/packages/x86/
```
