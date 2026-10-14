# fhsdk-demo-server（Docker / linux-amd64）

Demo 后端容器（**linux/amd64**），内含 Java 17、带 WHIP 的 FFmpeg、DRTC 配置。

默认端口口径：
- 后端：宿主 `18080` → 容器 `8080`
- Oryx：宿主 `12022` → 容器 `2022`

## 准备

`prepare.sh` 做两件事：**(1)** 把 `mvn package` 产出的 fat jar 拷为 `app.jar`；**(2)** 确保 `config/drtc_config_node.json` 存在（供 Dockerfile `COPY config/`）。  
DRTC 配置来源（二选一）：

1. 复制 `drtc_config_node.json.example` → `config/drtc_config_node.json` 并改 `YOUR_FH_SERVER_HOST`  
2. 或 `export SDK_DRTC_CONFIG=/path/from/fhsdk/demo/drtc_config_node.json` 后执行脚本

```bash
cp drtc_config_node.json.example config/drtc_config_node.json   # 首次
# 编辑 config/drtc_config_node.json
./prepare.sh
```

> 容器内 Java 进程以非 root 用户 `appuser` 运行。

## 启动

```bash
# 先起 Oryx（若未起）
cd ../oryx && docker compose up -d && cd -

docker compose up -d --build
```

宿主机端口：**18080** → 容器 8080。前端 Vite 代理到 `http://127.0.0.1:18080`。

`.env` 中与 Oryx 相关的默认模板：
- `ORYX_WHIP_URL_TEMPLATE=http://fhsdk-demo-oryx:2022/...`（容器间同网直连，避免 `host.docker.internal` UDP 超时）
- `ORYX_WHEP_URL_TEMPLATE=http://127.0.0.1:12022/...`（浏览器走宿主映射）

复制 `.env.example` 为 `.env` 后按环境改 DJI CloudAPI2 地址。

## 最小验收

1. `POST /api/v1/session/init` 成功，日志包含 `FHSDK initialized`。  
2. `POST /api/v1/live/start` 成功，日志包含 `FFmpeg stream config` 且持续出现 `live frame #...`。  
3. 前端打开后可正常播放 WHEP（`/oryx` 代理到 `12022`）。  
