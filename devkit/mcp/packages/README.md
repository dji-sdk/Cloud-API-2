# CodeMeter Runtime 包

此目录存放 Wibu CodeMeter Runtime 的安装包，用于私有化构建时打入运行时镜像。

> **注意**：`.deb` 文件因 Wibu 再分发条款限制，不随 git 仓库分发（已加入 `.gitignore`）。
> 请按下方说明手动获取并放置到正确目录。

## 所需文件

- `x86/codemeter-lite_8.10.6221.500_amd64.deb` — CodeMeter Lite Runtime (x86_64)

## 获取方式

从 Wibu Systems 官方下载或联系运维团队获取，版本需与 CI 镜像 `your-registry.example.com/private/build/wibu:v0.2` 中的 AxProtector 版本匹配。

CI 不走本地文件：私有化构建 job 的 `before_script` 会从本项目的 GitLab 通用包仓库拉取
（`${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/generic/codemeter/<版本>/<文件名>`），
用 CI 变量 `CODEMETER_READ_TOKEN` 鉴权 —— 这是一个 scope 仅 `read_package_registry`
的只读 Deploy Token，通过 `DEPLOY-TOKEN` 请求头传递。

> 为什么不用 `CI_JOB_TOKEN`：本 GitLab 实例（17.6.5-ee）拒绝 job token 访问包仓库，
> header 和 `?job_token=` 两种形式均返回 401，而同一 URL 用 Deploy Token 可正常下载。
> 项目侧的 job token inbound allowlist 已包含本项目自身，非配置问题。

升级版本时需两步：先把新 `.deb` 上传到包仓库，再同步修改两个 `.gitlab-ci.yml` 中的
`CODEMETER_DEB` / `CODEMETER_VERSION` 变量以及 `Dockerfile.private.runtime` 里的文件名。

```bash
curl --request PUT --header "PRIVATE-TOKEN: <your-token>" \
  --upload-file <新版本>.deb \
  "https://<gitlab-host>/api/v4/projects/<project-id>/packages/generic/codemeter/<新版本>/<新版本>.deb"
```

## 放置方式（仅本地构建需要）

```bash
mkdir -p mcp/packages/x86
cp /path/to/codemeter-lite_8.10.6221.500_amd64.deb mcp/packages/x86/
```
