# libs — 私有 SDK JAR

本目录存放 DJI Cloud API 2 SDK 的私有 JAR。SDK **不在 Maven Central**，
需在开发机手动安装到本地仓库后构建（见下方命令）。

**SDK 按 CPU 架构分为两个独立的包**（amd64 / arm64），每个包只内含自己那一份
JNI 原生库，且两者 Maven 坐标完全相同（`com.dji.flighthubsdk:flight-hub-sdk:3.3.7`）。
因此**打包镜像时必须显式指定用哪个包**，见「按架构选择 SDK 包」。

## 文件清单

| 文件 | 版本 | 架构 | 内置主库 | SHA256 |
|------|------|------|----------|--------|
| `amd64/flight-hub-sdk-3.3.7.jar` | 3.3.7 | linux/amd64 | `lib/linux-x86_64/libflighthubsdk_jni.so`（51,016,344 B） | `44900f6a7fca585d87954d718915c80758d723edf6fed17ef9314631d05dea95` |
| `arm64/flight-hub-sdk-3.3.7.jar` | 3.3.7 | linux/arm64 | `lib/linux-aarch64/libflighthubsdk_jni.so`（48,360,800 B） | `a2aedf159f9312029f2f82fcf4b18c27afd3a8eb52c2da7762a3cbefcd11066a` |

> **SHA256 核验**：获取/拷贝 JAR 后应校验一致性，防止中间人替换或拷贝链路污染：
> ```bash
> shasum -a 256 libs/amd64/flight-hub-sdk-3.3.7.jar
> shasum -a 256 libs/arm64/flight-hub-sdk-3.3.7.jar
> # 应输出与上表一致的哈希
> ```

## 来源说明

- 渠道：DJI Cloud API 2 官方 SDK 发布包，同一版本号的两份独立构建：
  - `libs/amd64/` ← `flight_hub_sdk_v3.3.7_20261010`（x86_64 主库完整）
  - `libs/arm64/` ← `flight_hub_sdk_v3.3.7_20261013`（aarch64 主库完整，**无 x86_64 主库**）
- 平台：内置 JNI native 库，运行时 `JniLibraryLoader` 按 `os.arch` 查表加载 `/lib/linux-<arch>/`，
  **找不到时不回退**；macOS/Windows 使用 mock 模式，不加载 native。
- 两个包除原生库与少量 SDK 内部 API 外，Java 侧基本一致；已知的 API 差异见
  `findings.md`（`FlyToPointParams` 的目标点设置方法不同，代码里已用反射兼容两种包）。

## 按架构选择 SDK 包（构建镜像时必填）

所有 Dockerfile 统一通过构建参数 `SDK_ARCH` 选择包：

```dockerfile
ARG SDK_ARCH
COPY libs/${SDK_ARCH}/flight-hub-sdk-3.3.7.jar /tmp/flight-hub-sdk-3.3.7.jar
```

| 场景 | 传参 |
|------|------|
| 构建 amd64 镜像 / CI 的 lint、coverage、OWASP 检查 | `--build-arg SDK_ARCH=amd64` |
| 构建 arm64 镜像 | `--build-arg SDK_ARCH=arm64` |
| 私有化构建（`Dockerfile.private.build`） | 不传 = `amd64`（Dockerfile 内默认值，兼容既有调用方）；arm64 显式传 `arm64` |

**刻意不设默认值**：常规 Dockerfile 不写 `ARG SDK_ARCH=amd64`，漏传时 `COPY` 直接失败，
避免在 arm64 构建里悄悄塞进 amd64 的包、直到运行时才因 JNI 加载失败暴露。

> 例外：`mcp/Dockerfile.private.build` 保留了 `ARG SDK_ARCH=amd64` 默认值，因为它的既有
> 调用方都不传该参数。arm64 私有化构建走独立的构建 job，由 CI 显式传 `SDK_ARCH=arm64`。

## 安装到本地 Maven 仓库（开发机首次）

按开发机的 CPU 架构选包安装（macOS Apple Silicon 装 arm64 包只是为了让本地 native 探针可用；
纯 mock 调试装任一包均可）：

```bash
# x86_64 开发机 / 需要跑 amd64 原生库
mvn install:install-file \
  -Dfile=libs/amd64/flight-hub-sdk-3.3.7.jar \
  -DgroupId=com.dji.flighthubsdk \
  -DartifactId=flight-hub-sdk \
  -Dversion=3.3.7 \
  -Dpackaging=jar \
  --settings config/settings.xml

# arm64 开发机（如 Apple Silicon / 鲲鹏 / 飞腾）
mvn install:install-file \
  -Dfile=libs/arm64/flight-hub-sdk-3.3.7.jar \
  -DgroupId=com.dji.flighthubsdk \
  -DartifactId=flight-hub-sdk \
  -Dversion=3.3.7 \
  -Dpackaging=jar \
  --settings config/settings.xml
```

两个包坐标相同，`~/.m2` 里只能存在一个；切换架构时重跑上面对应的一条即可覆盖。

## 版本一致性提醒（LKG-05 / SUP-03）

升级 SDK 时请同步核对：

1. 文件名 / `pom.xml` 依赖版本 / MANIFEST `Implementation-Version` 三者一致；
2. 新 JAR 的 SHA256 更新到本表；
3. 提示 SDK 团队在构建脚本中解析 `Built-JDK` 模板变量。

**2026-09-28 核验记录（3.1.6 → 3.3.5）**：文件名/pom 版本/MANIFEST 三者一致（`Implementation-Version: v3.3.5`，dist 目录名 `flight_hub_sdk_v3.3.5_20260921`）；类级 diff 新增 54 个类（含 `UnlockLicense` 系列）、删除 14 个类（`FlightTasksMediaUploadFile*` 系列，已确认仓内无引用）；三方组件新增 aws-cpp-sdk-s3/libcurl/libwebrtc，见 `THIRD_PARTY_LICENSES.md`。

**2026-10-15 核验记录（3.3.7 仅 x86_64 → 3.3.7 +aarch64）**：文件名 `flight-hub-sdk-3.3.7.jar` 与 `pom.xml` 依赖版本均保持 `3.3.7` 不变，MANIFEST `Implementation-Version` 仍为 `v3.3.7`（dist 目录名由 `flight_hub_sdk_v3.3.7_20261010` 变为 `flight_hub_sdk_v3.3.7_20261013`）；本次是同版本号下的 native 库补充构建，新增 `lib/linux-aarch64/libflighthubsdk_jni.so`（48,360,800 B）。SHA256 已同步更新，见上方文件清单。

**2026-10-21 复检与归档（重要）**：此前记录中「`lib/linux-x86_64/`、`lib/macosx-arm64/` 内容不变，无类级 diff」**不成立**，实测两份发布的原生库差异如下：

| 项 | 20261010（现归档为 `libs/amd64/`） | 20261013（现归档为 `libs/arm64/`） |
|---|---|---|
| `lib/linux-x86_64/libflighthubsdk_jni.so` | 51,016,344 B | **缺失** |
| `lib/linux-aarch64/libflighthubsdk_jni.so` | 无 | 48,360,800 B |
| `lib/macosx-arm64/libflighthubsdk_jni.dylib` | 41,172,976 B（10-10 构建） | 43,260,000 B（10-13 构建） |
| `lib/linux-x86_64/` 依赖库 | 2026-10-10 构建 | 2026-10-09 构建（更旧、更小） |
| native 方法数（`flighthubsdkmoduleJNI`） | 14,979 | 16,002 |

结论：`20261013` 那份**只能用于 arm64**。在 linux/amd64 容器内实测 `JniLibraryLoader.loadLibrary()` 抛出
`java.io.FileNotFoundException: JNI library not found in resources: /lib/linux-x86_64/libflighthubsdk_jni.so`，
SDK 无法初始化；同一探针在 linux/arm64 下加载成功。SDK 团队随后提供了含 x86_64 主库完整内容的
`20261010` 构建，两份包分别按架构归档到 `libs/amd64/`、`libs/arm64/`。
**部署哪个架构的镜像，就用哪个架构的包构建，不要混用。**

## 已知风险 / 已知问题（安全审计留档，2026-09-28）

以下问题源于厂商私有 SDK JAR 内嵌的二进制/元数据，无法从本仓库层面修复，仅作留档记录：

1. **Linux 原生库 OpenSSL 版本 EOL**：当前 `flight-hub-sdk-3.3.7.jar` 的 Linux x86_64 native 库（`libflighthubsdk_jni.so` 等）内嵌 OpenSSL 1.1.1（已终止维护）。已用 `strings` 核验，原生库仍报告 `OpenSSL 1.1.1f`，因此**不能靠升级 SDK 版本解决**，只能等待厂商下一次重新编译原生库时切换到受支持的 OpenSSL 版本。`skeleton/docker/Dockerfile` 中的 `focal-updates` 源安装 `libssl1.1` 步骤是运行时必需依赖，不应移除。
2. **Native 二进制内嵌调试路径**：厂商二进制中检测到内嵌的构建期调试路径字符串（如 `/build/vendor/...`），属于厂商编译产物特征，无法从本仓库清除。
3. **`@vitest/mocker` 依赖 CVE（中危）**：已于 2026-09-29 回移开源仓第四轮整改（fix/backport-opensource-round45）——`skeleton/web` 升级 vitest 至 5.0.x 并适配 v5 mock 行为（constructor mock 改 function 表达式、URL stub 改 spyOn），`npm audit` 0 漏洞、225 个前端测试全部通过、vue-tsc 通过。

**2026-10-30 复审补充：macOS 原生库内嵌 Homebrew OpenSSL 构建路径（DK-P2-01）**

对 `libs/amd64/flight-hub-sdk-3.3.7.jar` 与 `libs/arm64/flight-hub-sdk-3.3.7.jar` 中的
`lib/macosx-arm64/` 原生库做了实测核验（`unzip -j` + `otool -L` + `strings -a`）：

| 检查项 | 实测结果 |
|---|---|
| `libcrypto.3.dylib` 报告的 OpenSSL 版本 | `OpenSSL 3.6.4 25 Aug 2026` |
| 内嵌 Homebrew Cellar 路径字符串 | 4 条，均为 `ENGINESDIR` / `MODULESDIR` 及同值纯字符串：`/opt/homebrew/Cellar/openssl@3/3.6.4/lib/engines-3`、`.../lib/ossl-modules` |
| `libcrypto` 动态依赖 | 仅 `@loader_path/libcrypto.3.dylib`（自引用）+ `/usr/lib/libSystem.B.dylib`，**无绝对 Homebrew 路径依赖** |
| `libssl.3.dylib` 动态依赖 | `@loader_path/libssl.3.dylib`、`@loader_path/libcrypto.3.dylib`、`/usr/lib/libSystem.B.dylib` |
| `libflighthubsdk_jni.dylib` 的 OpenSSL 依赖 | `@loader_path/libssl.3.dylib`、`@loader_path/libcrypto.3.dylib`（随包分发，相对路径） |
| JAR 内是否附带 `ossl-modules/` / `engines-3/` | **否** |

结论与处置：

- 这些路径是 Homebrew 版 OpenSSL 在**编译期固化**的默认 provider/engine 目录，属于厂商二进制
  的构建环境残留特征；因为动态链接全部走 `@loader_path`，未安装 Homebrew OpenSSL 的机器
  仍可从 JAR 内正常加载这三个 dylib，**不构成加载失败风险**。
- 残留字符串是**信息性披露**（暴露打包机目录结构），不是可利用漏洞；仓库层面无法、也不应该
  篡改厂商二进制（改 strings 会破坏代码签名/哈希，且触发 SHA256 核验失败）。
- macOS 上本套件只以 mock 模式运行（`JniLibraryLoader` 只查 `lib/linux-<arch>/`，找不到不回退；
  见上文「平台」说明），native 库不会被加载，因此该风险在 macOS 上无实际攻击面。
- 若需彻底消除，只能由 SDK 团队在构建时用 `crypto`/`ssl` 的静态或 rpath 化构建（或改用非 Homebrew
  来源的 OpenSSL），随下一次 JAR 发布闭环。**本仓不做二进制修改。**

> **F-03 闭环说明（2026-09-29）**：3.3.5 JAR 的 MANIFEST `Implementation-Version: v3.3.5` 已与文件名/pom 三者一致，开源外发审计 F-03（版本戳滞后）随 SDK 升级自动闭环。此前 3.1.6 JAR 的 v3.1.5 版本戳差异不再适用。
