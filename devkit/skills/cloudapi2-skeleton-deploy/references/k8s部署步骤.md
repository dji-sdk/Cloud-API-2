# Kubernetes 部署步骤

> 来源：`skeleton/docs/部署指南.md` §5。AI 按此推进。
> 适用：生产级高可用、多副本、弹性伸缩。Manifest 位于 `skeleton/deploy/k8s/`（9 个）。

## 步骤清单

### 1. 准备镜像推送私有仓库

```bash
docker build --platform linux/amd64 -f skeleton/docker/Dockerfile -t <REGISTRY>/paas-sdk-devkit/skeleton:<TAG> .
docker build -f skeleton/web/Dockerfile -t <REGISTRY>/paas-sdk-devkit/skeleton-ui:<TAG> skeleton/web/
docker push <REGISTRY>/paas-sdk-devkit/skeleton:<TAG>
docker push <REGISTRY>/paas-sdk-devkit/skeleton-ui:<TAG>
```

> 确保集群能拉取私有仓库镜像（imagePullSecrets 或节点 docker login）。

### 2. 编辑 Secret 与 ConfigMap（AI 逐项检查清单）

```bash
cd skeleton/deploy/k8s
# ① 01-mysql-secret.yaml：MYSQL_ROOT_PASSWORD / MYSQL_PASSWORD（强随机）
# ② 04-backend-secret.yaml：FH_JWT_SECRET / FH_DB_PASSWORD / FH_SKELETON_SDK_TOKEN 必填；FH_CHAT_MCP_AUTH_TOKEN 可选
# ③ 03-backend-configmap.yaml：FH_SKELETON_SDK_ADDRESS / FH_SKELETON_SDK_NOTIFICATION_SERVER / FH_CHAT_MCP_URL 必填
# ④ 06-frontend-configmap.yaml：VITE_FH2_ADDRESS / VITE_FH2_WSS_URL 必填
# ⑤ 05/07 部署：把 <REGISTRY>/paas-sdk-devkit/skeleton:<TAG> 替换为实际镜像
```

> AI 核对 snippets/k8s-checklist.yaml 逐项确认。

### 3. 应用

```bash
kubectl apply -f 00-namespace.yaml
kubectl apply -f 01-mysql-secret.yaml
kubectl apply -f 02-mysql-sts.yaml
kubectl apply -f 03-backend-configmap.yaml
kubectl apply -f 04-backend-secret.yaml
kubectl apply -f 05-backend-deploy.yaml
kubectl apply -f 06-frontend-configmap.yaml
kubectl apply -f 07-frontend-deploy.yaml
kubectl apply -f 08-ingress.yaml
```

### 4. 验证

```bash
kubectl -n fh-skeleton get pods,svc,ingress
kubectl -n fh-skeleton get sts mysql        # READY 1/1
kubectl -n fh-skeleton port-forward svc/skeleton-backend 8080:8080 &
curl -f http://127.0.0.1:8080/pf-devkit/ai-plugin/skeleton/actuator/health
```

### 5. TLS 与域名（可选）

编辑 `08-ingress.yaml`：取消 `tls:` 注释并填充 `tls-secret`（或改 ingress-nginx 自动签发），`host` 换实际域名。`nginx` 为示例 IngressClass。

### 6. 升级 / 回滚

```bash
kubectl -n fh-skeleton set image deployment/skeleton-backend skeleton=<REGISTRY>/paas-sdk-devkit/skeleton:<NEW_TAG>
kubectl -n fh-skeleton set image deployment/skeleton-frontend nginx=<REGISTRY>/paas-sdk-devkit/skeleton-ui:<NEW_TAG>
kubectl -n fh-skeleton rollout status deployment/skeleton-backend

kubectl -n fh-skeleton rollout undo deployment/skeleton-backend
kubectl -n fh-skeleton rollout undo deployment/skeleton-frontend
```

## AI 推进要点

- Secret/ConfigMap 编辑是 AI 与用户协作重点：AI 生成完整替换值，用户 vi 编辑
- 应用顺序不可乱（namespace → secret → sts → config → deploy → ingress）
- 报错 → 加载 references/部署排障.md（kubectl logs 定位）
