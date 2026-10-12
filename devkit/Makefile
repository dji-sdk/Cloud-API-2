.PHONY: build release privatization-build-wibu-60000010 privatization-build-wibu-6002150 \
       skeleton-privatization-build-wibu-60000010 skeleton-privatization-build-wibu-6002150

# 父子 pipeline ID 对齐：子 pipeline 的 CI_PIPELINE_ID 与父 pipeline 不同，
# 部署侧只能看到父 ID，因此镜像 tag 优先使用根 CI 显式传入的 PARENT_PIPELINE_ID。
IMAGE_TAG ?= $(or $(PARENT_PIPELINE_ID),$(CI_PIPELINE_ID))

# 私有化 Harbor 仓库地址（内部域名抽离为变量，避免硬编码入库；外部 fork 可用
# 私有化交付环境自己的 Harbor 地址覆盖，如 make ... HARBOR_INNER=<registry>）
# 默认取 CI 注入的 CI_REGISTRY_INNER_HARBOR（BUILD-02 脱敏后不再硬编码内部域名）；
# 外部 fork 未设置该变量时为空，私有化构建目标由各 target 的 guard 检查直接失败。
HARBOR_INNER ?= $(CI_REGISTRY_INNER_HARBOR)
HARBOR_PROJECT ?= es310/paas-sdk-devkit
HARBOR_PRIVATE_PROJECT ?=

# ──────────────────────────────────────────
# MCP Server 私有化构建
# ──────────────────────────────────────────
# MCP_TARGET 为构建产物名，沿用 flighthub-*（与 Maven artifactId、
# Spring 服务名保持一致，避免牵动 pom.xml / application.yml）。仅对外推送的
# Harbor 镜像名规范化为 pf-devkit-mcp。
MCP_TARGET = flighthub-mcp
MCP_BUILD_STAGE_TAG = pf-devkit-mcp-build:${CI_PIPELINE_ID}
MCP_BUILD_STAGE_CONTAINER = mcp-${CI_PIPELINE_ID}
BUILD_PACKAGE = build
MCP_IMAGE_URL = $(HARBOR_INNER)/$(HARBOR_PROJECT)/pf-devkit-mcp:$(IMAGE_TAG)
MCP_IMAGE_URL_s = $(HARBOR_INNER)/$(HARBOR_PROJECT)/pf-devkit-mcp:$(IMAGE_TAG)_s
MCP_IMAGE_LATEST = $(HARBOR_INNER)/$(HARBOR_PROJECT)/pf-devkit-mcp:latest

# ──────────────────────────────────────────
# Skeleton BFF 私有化构建
# ──────────────────────────────────────────
# SKE_TARGET 为构建产物名，沿用 fh-skeleton-app（与 Maven
# artifactId、Spring 服务名保持一致）。仅对外推送的 Harbor 镜像名规范化为
# pf-devkit-bff。
SKE_TARGET = fh-skeleton-app
SKE_BUILD_STAGE_TAG = pf-devkit-bff-build:${CI_PIPELINE_ID}
SKE_BUILD_STAGE_CONTAINER = skeleton-${CI_PIPELINE_ID}
SKE_IMAGE_URL = $(HARBOR_INNER)/$(HARBOR_PROJECT)/pf-devkit-bff:$(IMAGE_TAG)
SKE_IMAGE_URL_s = $(HARBOR_INNER)/$(HARBOR_PROJECT)/pf-devkit-bff:$(IMAGE_TAG)_s
SKE_IMAGE_LATEST = $(HARBOR_INNER)/$(HARBOR_PROJECT)/pf-devkit-bff:latest

build:
	cd mcp && mvn clean package -DskipTests -B

# 源码发行包（安全审计 F-01/F-07/I-05 整改回移）
# 基于 git archive 导出已跟踪文件，排除本地产物并生成 SHA256SUMS。
release:
	./scripts/release-package.sh dist

# ════════════════════════════════════════════
# MCP — 内部测试（不加壳，测试母锁）
# ════════════════════════════════════════════
privatization-build-wibu-60000010:
	@# 私有化内部构建：将 Dockerfile.private.* 中的 HARBOR_PLACEHOLDER 替换为内部 Harbor
	@#（CI 变量 CI_REGISTRY_INNER_HARBOR）。公共 fork 未设置该变量时保持占位符，
	@# 该目标不可用（私有化交付仅内部构建）。
	@if [ -z "$(CI_REGISTRY_INNER_HARBOR)" ]; then echo "⛔ CI_REGISTRY_INNER_HARBOR 未设置——私有化构建仅限内部 CI"; exit 1; fi
	@if [ -z "$(HARBOR_PRIVATE_PROJECT)" ]; then echo "⛔ HARBOR_PRIVATE_PROJECT 未设置——私有化构建需指定基础镜像 Harbor 项目路径"; exit 1; fi
	@sed -i.bak 's|HARBOR_PLACEHOLDER/private/build|$(CI_REGISTRY_INNER_HARBOR)/$(HARBOR_PRIVATE_PROJECT)|g' mcp/Dockerfile.private.build mcp/Dockerfile.private.runtime && rm -f mcp/Dockerfile.private.*.bak
	docker rm -v -f ${MCP_BUILD_STAGE_CONTAINER} || true
	rm -rf ${BUILD_PACKAGE}
	docker build \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f mcp/Dockerfile.private.build . -t ${MCP_BUILD_STAGE_TAG}
	docker create --name ${MCP_BUILD_STAGE_CONTAINER} ${MCP_BUILD_STAGE_TAG}
	mkdir -p ${BUILD_PACKAGE}
	docker cp ${MCP_BUILD_STAGE_CONTAINER}:/build/target/flighthub-mcp.jar ${BUILD_PACKAGE}/
	cp ./${BUILD_PACKAGE}/${MCP_TARGET}.jar ./${BUILD_PACKAGE}/${MCP_TARGET}.jar.ax
	docker build \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f mcp/Dockerfile.private.runtime . -t ${MCP_IMAGE_URL}
	docker push ${MCP_IMAGE_URL}
	@if [ "${CI_COMMIT_BRANCH}" = "master" ]; then \
		docker tag ${MCP_IMAGE_URL} ${MCP_IMAGE_LATEST}; \
		docker push ${MCP_IMAGE_LATEST}; \
	fi
	docker rm -v -f ${MCP_BUILD_STAGE_CONTAINER}
	rm -rf ${BUILD_PACKAGE}

# ════════════════════════════════════════════
# MCP — 正式加壳（AxProtector）
# ════════════════════════════════════════════
privatization-build-wibu-6002150:
	@# 见 privatization-build-wibu-60000010：HARBOR_PLACEHOLDER 替换为内部 Harbor
	@if [ -z "$(CI_REGISTRY_INNER_HARBOR)" ]; then echo "⛔ CI_REGISTRY_INNER_HARBOR 未设置——私有化构建仅限内部 CI"; exit 1; fi
	@if [ -z "$(HARBOR_PRIVATE_PROJECT)" ]; then echo "⛔ HARBOR_PRIVATE_PROJECT 未设置——私有化构建需指定基础镜像 Harbor 项目路径"; exit 1; fi
	@sed -i.bak 's|HARBOR_PLACEHOLDER/private/build|$(CI_REGISTRY_INNER_HARBOR)/$(HARBOR_PRIVATE_PROJECT)|g' mcp/Dockerfile.private.build mcp/Dockerfile.private.runtime && rm -f mcp/Dockerfile.private.*.bak
	docker rm -v -f ${MCP_BUILD_STAGE_CONTAINER} || true
	rm -rf ${BUILD_PACKAGE}
	docker build \
		--pull \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f mcp/Dockerfile.private.build . -t ${MCP_BUILD_STAGE_TAG}
	docker create --name ${MCP_BUILD_STAGE_CONTAINER} ${MCP_BUILD_STAGE_TAG}
	mkdir -p ${BUILD_PACKAGE}
	docker cp ${MCP_BUILD_STAGE_CONTAINER}:/build/target/flighthub-mcp.jar ${BUILD_PACKAGE}/
	java -jar /usr/share/AxProtector/AxProtector.jar -kcm -f${WIBU_FIRM_CODE} -p${WIBU_PRODUCT_CODE} -cf0 -d:6.20 -fw:3.00 -cav -cas100 -wu0 -we0 -eac -eec -eusc1 -emc -car30,3 -cag17 -v -o:"./${BUILD_PACKAGE}/${MCP_TARGET}.jar.ax" "./${BUILD_PACKAGE}/${MCP_TARGET}.jar"
	docker build \
		--pull \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f mcp/Dockerfile.private.runtime . -t ${MCP_IMAGE_URL_s}
	docker push ${MCP_IMAGE_URL_s}
	@if [ "${CI_COMMIT_BRANCH}" = "master" ]; then \
		docker tag ${MCP_IMAGE_URL_s} ${MCP_IMAGE_LATEST}; \
		docker push ${MCP_IMAGE_LATEST}; \
	fi
	docker rm -v -f ${MCP_BUILD_STAGE_CONTAINER}
	rm -rf ${BUILD_PACKAGE}

# ════════════════════════════════════════════
# Skeleton — 内部测试（不加壳，测试母锁）
# ════════════════════════════════════════════
skeleton-privatization-build-wibu-60000010:
	@# 见 privatization-build-wibu-60000010：HARBOR_PLACEHOLDER 替换为内部 Harbor
	@if [ -z "$(CI_REGISTRY_INNER_HARBOR)" ]; then echo "⛔ CI_REGISTRY_INNER_HARBOR 未设置——私有化构建仅限内部 CI"; exit 1; fi
	@if [ -z "$(HARBOR_PRIVATE_PROJECT)" ]; then echo "⛔ HARBOR_PRIVATE_PROJECT 未设置——私有化构建需指定基础镜像 Harbor 项目路径"; exit 1; fi
	@sed -i.bak 's|HARBOR_PLACEHOLDER/private/build|$(CI_REGISTRY_INNER_HARBOR)/$(HARBOR_PRIVATE_PROJECT)|g' skeleton/Dockerfile.private.build skeleton/Dockerfile.private.runtime && rm -f skeleton/Dockerfile.private.*.bak
	docker rm -v -f ${SKE_BUILD_STAGE_CONTAINER} || true
	rm -rf ${BUILD_PACKAGE}
	docker build \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f skeleton/Dockerfile.private.build . -t ${SKE_BUILD_STAGE_TAG}
	docker create --name ${SKE_BUILD_STAGE_CONTAINER} ${SKE_BUILD_STAGE_TAG}
	mkdir -p ${BUILD_PACKAGE}/skeleton
	docker cp ${SKE_BUILD_STAGE_CONTAINER}:/build/fh-skeleton-app.jar ${BUILD_PACKAGE}/skeleton/
	docker cp ${SKE_BUILD_STAGE_CONTAINER}:/opt/sdk-libs ${BUILD_PACKAGE}/skeleton/sdk-libs
	cp ./${BUILD_PACKAGE}/skeleton/${SKE_TARGET}.jar ./${BUILD_PACKAGE}/skeleton/${SKE_TARGET}.jar.ax
	docker build \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f skeleton/Dockerfile.private.runtime . -t ${SKE_IMAGE_URL}
	docker push ${SKE_IMAGE_URL}
	@if [ "${CI_COMMIT_BRANCH}" = "master" ]; then \
		docker tag ${SKE_IMAGE_URL} ${SKE_IMAGE_LATEST}; \
		docker push ${SKE_IMAGE_LATEST}; \
	fi
	docker rm -v -f ${SKE_BUILD_STAGE_CONTAINER}
	rm -rf ${BUILD_PACKAGE}

# ════════════════════════════════════════════
# Skeleton — 正式加壳（AxProtector）
# ════════════════════════════════════════════
skeleton-privatization-build-wibu-6002150:
	@# 见 privatization-build-wibu-60000010：HARBOR_PLACEHOLDER 替换为内部 Harbor
	@if [ -z "$(CI_REGISTRY_INNER_HARBOR)" ]; then echo "⛔ CI_REGISTRY_INNER_HARBOR 未设置——私有化构建仅限内部 CI"; exit 1; fi
	@if [ -z "$(HARBOR_PRIVATE_PROJECT)" ]; then echo "⛔ HARBOR_PRIVATE_PROJECT 未设置——私有化构建需指定基础镜像 Harbor 项目路径"; exit 1; fi
	@sed -i.bak 's|HARBOR_PLACEHOLDER/private/build|$(CI_REGISTRY_INNER_HARBOR)/$(HARBOR_PRIVATE_PROJECT)|g' skeleton/Dockerfile.private.build skeleton/Dockerfile.private.runtime && rm -f skeleton/Dockerfile.private.*.bak
	docker rm -v -f ${SKE_BUILD_STAGE_CONTAINER} || true
	rm -rf ${BUILD_PACKAGE}
	docker build \
		--pull \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f skeleton/Dockerfile.private.build . -t ${SKE_BUILD_STAGE_TAG}
	docker create --name ${SKE_BUILD_STAGE_CONTAINER} ${SKE_BUILD_STAGE_TAG}
	mkdir -p ${BUILD_PACKAGE}/skeleton
	docker cp ${SKE_BUILD_STAGE_CONTAINER}:/build/fh-skeleton-app.jar ${BUILD_PACKAGE}/skeleton/
	docker cp ${SKE_BUILD_STAGE_CONTAINER}:/opt/sdk-libs ${BUILD_PACKAGE}/skeleton/sdk-libs
	java -jar /usr/share/AxProtector/AxProtector.jar -kcm -f${WIBU_FIRM_CODE} -p${WIBU_PRODUCT_CODE} -cf0 -d:6.20 -fw:3.00 -cav -cas100 -wu0 -we0 -eac -eec -eusc1 -emc -car30,3 -cag17 -v -o:"./${BUILD_PACKAGE}/skeleton/${SKE_TARGET}.jar.ax" "./${BUILD_PACKAGE}/skeleton/${SKE_TARGET}.jar"
	docker build \
		--pull \
		--network=host \
		--build-arg http_proxy=${http_proxy} \
		--build-arg https_proxy=${https_proxy} \
		--build-arg no_proxy=${no_proxy} \
		-f skeleton/Dockerfile.private.runtime . -t ${SKE_IMAGE_URL_s}
	docker push ${SKE_IMAGE_URL_s}
	@if [ "${CI_COMMIT_BRANCH}" = "master" ]; then \
		docker tag ${SKE_IMAGE_URL_s} ${SKE_IMAGE_LATEST}; \
		docker push ${SKE_IMAGE_LATEST}; \
	fi
	docker rm -v -f ${SKE_BUILD_STAGE_CONTAINER}
	rm -rf ${BUILD_PACKAGE}
