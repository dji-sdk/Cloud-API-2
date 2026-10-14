# libs — 私有 SDK JAR

本目录存放 DJI CloudAPI2 SDK 的私有 JAR。SDK **不在 Maven Central**，
需在开发机手动安装到本地仓库后构建（见下方命令）。

## 文件清单

| 文件 | 版本 | SHA256 |
|------|------|--------|
| `flight-hub-sdk-3.3.7.jar` | 3.3.7 | `44900f6a7fca585d87954d718915c80758d723edf6fed17ef9314631d05dea95` |

> **SHA256 核验**：获取/拷贝 JAR 后应校验一致性，防止中间人替换或拷贝链路污染：
> ```bash
> shasum -a 256 flight-hub-sdk-3.3.7.jar   # 应输出与上表一致的哈希
> ```

## 来源说明

- 渠道：DJI CloudAPI2 官方 SDK 发布包（`flight_hub_sdk_v3.3.7_20261010`）。
- 平台：内置 JNI native 库，仅 Linux x86_64 运行时加载；macOS/Windows 使用 mock 模式，不加载 native。

## 安装到本地 Maven 仓库（开发机首次）

```bash
mvn install:install-file \
  -Dfile=libs/flight-hub-sdk-3.3.7.jar \
  -DgroupId=com.dji.flighthubsdk \
  -DartifactId=flight-hub-sdk \
  -Dversion=3.3.7 \
  -Dpackaging=jar \
  --settings config/settings.xml
```

## 版本一致性提醒（LKG-05 / SUP-03）

升级 SDK 时请同步核对：

1. 文件名 / `pom.xml` 依赖版本 / MANIFEST `Implementation-Version` 三者一致；
2. 新 JAR 的 SHA256 更新到本表；
3. 提示 SDK 团队在构建脚本中解析 `Built-JDK` 模板变量。

**2026-09-28 核验记录（3.1.6 → 3.3.5）**：文件名/pom 版本/MANIFEST 三者一致（`Implementation-Version: v3.3.5`，dist 目录名 `flight_hub_sdk_v3.3.5_20260921`）；类级 diff 新增 54 个类（含 `UnlockLicense` 系列）、删除 14 个类（`FlightTasksMediaUploadFile*` 系列，已确认仓内无引用）；三方组件新增 aws-cpp-sdk-s3/libcurl/libwebrtc，见 `THIRD_PARTY_LICENSES.md`。

## 已知风险 / 已知问题（安全审计留档，2026-09-28）

以下问题源于厂商私有 SDK JAR 内嵌的二进制/元数据，无法从本仓库层面修复，仅作留档记录：

1. **Linux 原生库 OpenSSL 版本 EOL**：当前 `flight-hub-sdk-3.3.7.jar` 的 Linux x86_64 native 库（`libflighthubsdk_jni.so` 等）内嵌 OpenSSL 1.1.1（已终止维护）。已用 `strings` 核验，原生库仍报告 `OpenSSL 1.1.1f`，因此**不能靠升级 SDK 版本解决**，只能等待厂商下一次重新编译原生库时切换到受支持的 OpenSSL 版本。`skeleton/docker/Dockerfile` 中的 `focal-updates` 源安装 `libssl1.1` 步骤是运行时必需依赖，不应移除。
2. **Native 二进制内嵌调试路径**：厂商二进制中检测到内嵌的构建期调试路径字符串（如 `/build/vendor/...`），属于厂商编译产物特征，无法从本仓库清除。
3. **`@vitest/mocker` 依赖 CVE（中危）**：已于 2026-09-29 回移开源仓第四轮整改（fix/backport-opensource-round45）——`skeleton/web` 升级 vitest 至 5.0.x 并适配 v5 mock 行为（constructor mock 改 function 表达式、URL stub 改 spyOn），`npm audit` 0 漏洞、225 个前端测试全部通过、vue-tsc 通过。

> **F-03 闭环说明（2026-09-29）**：3.3.5 JAR 的 MANIFEST `Implementation-Version: v3.3.5` 已与文件名/pom 三者一致，开源外发审计 F-03（版本戳滞后）随 SDK 升级自动闭环。此前 3.1.6 JAR 的 v3.1.5 版本戳差异不再适用。
