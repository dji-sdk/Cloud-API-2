# 回放页媒体（照片 / 视频）打不开 —— 排查清单

> **根因几乎总是同一类：媒体**取流地址**指向对象存储的内网端口 `:30802`，客户网段不可达。**
> 存储本身是健康的（预签名 URL 直接请求能正常返回字节），**坏的只有「从客户网络到 `:30802` 的可达性」**。
> 这类问题**不在 ISV 的应用代码里** —— 回放页由 FH2 托管的 `FlyDataReplay` UMD 自行取流，ISV 没有改写钩子。

适用于 RCA **Stage 2 Locate**。症状命中即优先于分层排查（SKILL.md Stage 2 步骤 3）。

## 症状分流（先分流，再排查）

| 现象 | 归属 | 去哪 |
|------|------|------|
| **媒体列表 / 缩略图正常**，点开照片全黑、视频进度条停在 `0:00`、下载无反应 | 取流地址不可达 | **本清单** |
| 缩略图也是空的、列表本身加载不出来 | 列表接口 / 鉴权问题 | 按 `error-codes.yaml` 走错误码路径，**不是本清单** |
| 直播黑屏、设备离线 `212015` | 设备接入 / DRTC | `live-stream-device-troubleshooting.md` |

**关键分流线**：同一台 FH2 上，媒体**列表**走网关 `:30812`（客户网段通），媒体**取流**走对象存储 `:30802`（客户网段不通）。所以「列表能出来、点开没反应」这个组合本身就是最强的定位信号 —— 它把故障锁定在取流这一跳，而不是鉴权、不是存储、不是前端组件。

## 判定命令（按顺序跑，两条即可定案）

### 1. 证明存储是健康的（在能访问到平台的机器上跑）

拿到媒体详情接口返回的那个绝对 URL（形如 `http://<主机>:30802/<桶>/<对象>?X-Amz-*`），**原样**请求前 1KB：

```bash
curl -sS -o /dev/null -D - \
  -H 'Range: bytes=0-1023' \
  '<媒体详情接口返回的完整 URL>'
```

**期望**：`HTTP/1.1 206 Partial Content` + `Content-Type: video/mp4`（或图片 MIME）+ `Accept-Ranges: bytes` + `Content-Range: bytes 0-1023/<总长>`，响应体开头是 `ftypisom` 之类的容器魔数。

看到 `206` 就说明**对象存在、预签名有效、Range 语义正常、存储服务健康** —— 排除存储侧一切嫌疑。若这里是 4xx/5xx，那才是存储或签名过期问题，转别的排查路径。

### 2. 证明客户网段到 `:30802` 不可达

在**客户网络里**（就是浏览器所在那台机器 / 那个网段）跑：

```bash
# 主机与端口取自上面那个 URL
nc -vz <对象存储主机> 30802
```

**期望（故障态）**：连接超时 / `Connection refused` / 域名解析失败（`NXDOMAIN`）。

同时对照浏览器 DevTools → Network：出错的那条请求应该是 `(failed)` / `net::ERR_CONNECTION_TIMED_OUT` / `ERR_NAME_NOT_RESOLVED`，**而不是** 4xx、**而不是** CORS 报错。

> 两条合起来就是定案证据：**存储说「我有」，网络说「够不着」。**

## 为什么 ISV 侧修不了

回放页的媒体面板由 `FlyDataReplay` 渲染，这个 UMD 脚本由 FH2 前端主机在 `/paasjs-demo/` 下托管，**不在 ISV 仓库里**。它只吃 `api.baseUrl` + `api.auth` 两个配置，然后：

- 自己拼 `{api.baseUrl}/pf-media/api/v1/media-files/{file_id}` 拿详情
- **把详情里的 `url` 字段原样赋给 `<video src>` / `<img src>`**
- 缩略图同理走 `preview_url` / `thumbnail_url`

即：详情接口返回什么地址，浏览器就直连什么地址。**没有地址改写钩子，也没有可注入的 HttpClient**。所以只要平台返回的是 `:30802` 的绝对地址，ISV 无论怎么写前端代码都改不掉这一跳。

## 平台侧应做的事（提单时把这段一起给平台）

**媒体详情接口返回的 `url` 应使用客户可达的对外地址**，二选一：

1. 对象存储对外暴露一个客户网段可达的地址（域名 / 端口映射 / 反向代理），详情接口返回该地址；或
2. 媒体取流统一经 FH2 网关 `:30812` 反代到对象存储，详情接口返回网关地址。

改造完成前，任何依赖「浏览器直连详情接口返回的 `url`」的功能（回放页媒体面板是其中之一）在客户网段都不可用。

## 附：devkit 已闭环的部分

`skeleton/web` 的**媒体管理页**（`/media`，非回放页）不直连 `:30802`：预览 / 播放 / 下载统一走本服务的代理端点 `/api/v1/media/{fileId}/content`，由 `feature-media` 回源对象存储并**透传 `Range`**（回 `206 Partial Content`），因此 `<video>` 仍可拖动进度条。

排查时如果客户用的是 skeleton 媒体页却说打不开，先确认代理端点是否真的生效：

```bash
curl -sS -o /dev/null -D - -H 'Range: bytes=0-1023' \
  '<客户前端地址>/pf-devkit/ai-plugin/skeleton/api/v1/media/<fileId>/content'
```

期望同样是 `206` + `Accept-Ranges: bytes`。返回 5xx 说明代理端点自身有问题（后端日志），与本文的 `:30802` 可达性问题是两回事。

## 相关

- 约束定义：`skills/cloudapi2-frontend-component/constraints.yaml` 的 `media-url-must-not-be-presigned-direct`
- 前端组件 FAQ（媒体面板开关 `playerBarBtn.media` 默认 `false`）：`skills/cloudapi2-frontend-component/references/troubleshooting.md` 第五节
- 组件接口细节：`skills/cloudapi2-frontend-component/references/fly-data-replay.md`
- 直播类黑屏：`live-stream-device-troubleshooting.md`
