# Oryx（Demo 拉流服务）

与 `fhsdk-demo-server` **独立进程**。Java 通过 FFmpeg **WHIP**（WebRTC）推流到本服务，浏览器通过 **WHEP** 播放。

## 启动

```bash
docker compose up -d
```

## 首次初始化（必须）

Oryx 默认开启推流鉴权，未初始化时 WHIP 会被拒绝。

> ⚠️ **首次部署前必须设置管理密码**。脚本内置的 `CHANGE_ME_BEFORE_DEPLOY` 仅为占位符，请务必通过环境变量 `ORYX_PASSWORD` 传入您自己的强密码，或编辑脚本替换默认值。切勿使用占位符直接部署。

```bash
chmod +x init-oryx.sh
export ORYX_PASSWORD='<your-strong-password>'    # 首次部署必须设置
./init-oryx.sh
```

脚本会用该密码初始化 Oryx 管理后台并打印 **publish secret**。将 secret 写入 Java 配置：

```yaml
# fhsdk-demo-server/src/main/resources/application.yml
oryx:
  publish-secret: <脚本输出的 secret>
```

也可在浏览器打开 [http://127.0.0.1:12022/mgmt/](http://127.0.0.1:12022/mgmt/) → **System / Authentication** 查看或修改 secret。

## 端口

| 端口 | 用途 |
|------|------|
| 12022（宿主）→ 2022（容器） | Oryx Web / API / WHIP / WHEP 信令 |
| 8000/udp | WebRTC 媒体（WHIP 推流 + WHEP 播放） |

## 与 Java 服务配置对应

```yaml
oryx:
  whip-url-template: http://127.0.0.1:12022/rtc/v1/whip/?app=live&stream={streamKey}
  whep-url-template: http://127.0.0.1:12022/rtc/v1/whep/?app=live&stream={streamKey}
  publish-secret: <Oryx 推流密钥>
```

前端开发时 Vite 将 `/oryx` 代理到 `:12022`，避免 WHEP 跨域。

## 流名约定

- `streamKey` = `demo_{gatewaySn}`
- FFmpeg WHIP：`http://127.0.0.1:12022/rtc/v1/whip/?app=live&stream=demo_{gatewaySn}&secret={publish-secret}`

## 本地验证 WHIP 推流 + WHEP 播放

```bash
SECRET=<your-secret>
ffmpeg -re -f lavfi -i testsrc=size=640x480:rate=25 \
  -c:v libx264 -preset ultrafast -tune zerolatency -an -t 30 \
  -f whip "http://127.0.0.1:12022/rtc/v1/whip/?app=live&stream=demo_test&secret=$SECRET"
```

推流成功后，WHEP 播放 URL：

`http://127.0.0.1:12022/rtc/v1/whep/?app=live&stream=demo_test`

## 排错

| 现象 | 原因 / 处理 |
|------|-------------|
| WHIP 鉴权失败 | 未带 `secret=` 或 secret 错误；先运行 `init-oryx.sh` |
| `StreamBusy` | 同名流已在推，停止旧 ffmpeg 或换 stream 名 |
| WHEP 502 | 无有效 WHIP 推流，或 SDP 无效 |
| 浏览器 `Failed to fetch` | CORS；开发环境走 Vite `/oryx` 代理 |
| `Unknown format 'whip'` | 本机 FFmpeg 版本过低，需 **≥ 8.0**（WHIP muxer 自 8.0 "Huffman" 2025-08 进入主线） |
| 端口冲突 | 修改 `docker-compose.yml` 端口映射 |
