# ── Stage 1: Vite 构建 ──────────────────────────────────
# 注意：构建上下文为 skeleton/（见 docker-compose.yml / .gitlab-ci.yml），
# 这样 docs/user-guide 使用说明才能被 COPY 进上下文，供 vite copy 插件写入产物。
FROM node:20-alpine AS builder
WORKDIR /app/web

# VITE_* 变量在构建期烘入产物（修改后需 --build 重建）
ARG VITE_FH2_ADDRESS
ARG VITE_FH2_PORT=30812
ARG VITE_FH2_PRJ_ID
ARG VITE_DEFAULT_THEME=dark
ARG VITE_PRIMARY_COLOR=#1a73e8
ARG VITE_PRIMARY_HOVER_COLOR=
ARG VITE_MOCK_ENABLED=false
ARG VITE_MAP_PROVIDER=esri
# 警告：VITE_BING_KEY / VITE_TIANDITU_KEY 不要通过 build-arg 传入真实 key，
# 否则 key 会被烙印到镜像层（docker history 可提取）。
# 请保持默认空值，改用运行时注入：deploy.env → docker-entrypoint.sh → config.js。
ARG VITE_BING_KEY=
ARG VITE_TIANDITU_KEY=

ENV VITE_FH2_ADDRESS=$VITE_FH2_ADDRESS \
    VITE_FH2_PORT=$VITE_FH2_PORT \
    VITE_FH2_PRJ_ID=$VITE_FH2_PRJ_ID \
    VITE_DEFAULT_THEME=$VITE_DEFAULT_THEME \
    VITE_PRIMARY_COLOR=$VITE_PRIMARY_COLOR \
    VITE_PRIMARY_HOVER_COLOR=$VITE_PRIMARY_HOVER_COLOR \
    VITE_MOCK_ENABLED=$VITE_MOCK_ENABLED \
    VITE_MAP_PROVIDER=$VITE_MAP_PROVIDER \
    VITE_BING_KEY=$VITE_BING_KEY \
    VITE_TIANDITU_KEY=$VITE_TIANDITU_KEY

COPY web/package*.json ./
RUN npm ci
# 拷贝前端源码 + 使用说明文档（docs/user-guide 在 /app/docs，vite copy 插件读取 ../docs）
COPY web/ .
COPY docs/ /app/docs/
RUN npm run build

# ── Stage 2: Nginx 服务（非特权镜像，PAAS-CI-04）────────
FROM nginxinc/nginx-unprivileged:alpine
COPY --from=builder --chown=nginx:nginx /app/web/dist /usr/share/nginx/html
COPY --chown=nginx:nginx web/nginx.conf /etc/nginx/conf.d/default.conf
COPY web/docker-entrypoint.sh /docker-entrypoint.sh
USER root
RUN chmod +x /docker-entrypoint.sh && chown nginx:nginx /docker-entrypoint.sh
USER nginx
EXPOSE 8080
ENTRYPOINT ["/docker-entrypoint.sh"]
