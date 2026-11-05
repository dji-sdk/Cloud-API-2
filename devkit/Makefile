.PHONY: build release

build:
	cd mcp && mvn clean package -DskipTests -B

# 源码发行包（安全审计 F-01/F-07/I-05 整改回移）
# 基于 git archive 导出已跟踪文件，排除本地产物并生成 SHA256SUMS。
release:
	./scripts/release-package.sh dist

# 内部 CI 专用 target（多架构私有化构建、AxProtector 加壳等）拆分到
# ci/internal-build.mk，开源仓库同步时排除该文件；文件不存在时静默跳过，
# 不影响 build/release 这两个公开 target。
-include ci/internal-build.mk
