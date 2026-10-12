# 通用错误模式

> 本文收录跨服务、跨场景的通用错误模式。特定领域错误见对应 reference 文件。

## 配置文件被 Docker bind mount 为空目录

容器读取 `/etc/configs/<service>.json`，但挂载的是空目录 → `panic: no json file found in /etc/configs` 循环重启。

**根因**：Docker compose bind mount 在源路径不存在时，自动创建为**空目录**而非报错。常见触发：
1. 升级部署时安装包 `config/` 目录缺少配置文件
2. 外接存储切换后 `setuptools install` 重建容器，config 路径指向空目录
3. 增量升级 `cp -r` 从旧包拷贝，旧包该路径已经是空目录

**排查**：
```bash
# 确认 bind mount 源路径类型（应为 file，若为 directory 即根因）
docker inspect <container> --format='{{json .Mounts}}' \
  | python3 -c "import sys,json; [print(m['Type'],m['Source'],'->',m['Destination']) for m in json.load(sys.stdin)]"
```

**修复**：从历史正确版本拷贝配置文件，替换空目录后 `docker compose up -d` 重建容器（`docker restart` 不更新 mount）。

## 跨容器错误透传（容易误判发起方）

**核心**：「A 容器日志报某 URL 失败」≠「A 自己请求了这个 URL」。A 可能只是 HTTP 调用了下游 B，B 内部失败后把错误体（含 B 的 URL）透传回 A，A 原样打日志。

**判别**：
- 错误形如 `{"error":"..."}` JSON 几乎一定是从下游透传
- 进 A 镜像 `grep -rn "<错误关键词>" /app /go/src` 确认是否真在 A 代码里

**典型场景**：
- `elevation-update-tool` 报 minio 错，实际是 `es-digital-elevation-data` 发的
- `skeleton-server` 报下游服务超时，实际是 `pf-notify-service` 连不上 redis

## Panic crash loop（Go 服务）

Go 服务启动 panic，每 ~60s 由 Docker restart policy 重启。根因在 panic 信息里。

**排查**：`docker logs --tail 50 <container>` 找 `panic:` 和 `goroutine` 堆栈。

## Connection refused 连锁崩溃

`dial tcp <ip>:<port>: connect: connection refused` → 目标容器 down 或未就绪。

**注意**：多个容器同时报 connection refused 时，先找**最先挂的那个**（通常是 mysql/redis/rabbitmq），它是根因，其他是连锁反应。

用 `docker ps -a --format '{{.Names}}\t{{.Status}}'` 按 uptime 排序，uptime 最短的是最后重启的（可能最先挂）。
