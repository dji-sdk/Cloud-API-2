# aws-cpp-sdk-s3（预编译）

| | |
|--|--|
| 上游 | https://github.com/aws/aws-sdk-cpp |
| 版本 | **1.11.642**（见各平台 `include/aws/core/VersionConfig.h`） |
| 许可证 | Apache-2.0，见 [LICENSE](./LICENSE) |
| 类型 | 预编译静态（仓内**不**带 `src/` 源码树） |
| SDK 目标 | `cloud_api2::aws_s3` |
| 官方平台 | **`lib/linux/x86_64/`** |

FH 只用 S3 Put。Linux 额外链 `libs2n.a`；`libaws-c-mqtt.a` 是 CRT 闭包带出来的，业务不用 MQTT。

上游 1.11.642 顶层没有 `NOTICE` 文件。

## 仓内布局

```text
lib/<os>/<arch>/
  include/          # AWS / CRT 头（按平台，不要合成一份）
  libaws-*.a        # 静态库（Linux 另有 libs2n.a）
bin/build_aws_s3.sh
src/                # gitignore
```

不要再加 `lib/android/`：官方只保证 Linux x86_64。

## 如何重编

脚本**不会**自己 clone。先把源码放到本目录旁，再跑构建：

```bash
cd third_party/aws-cpp-sdk-s3
git clone --branch 1.11.642 --recurse-submodules \
  https://github.com/aws/aws-sdk-cpp.git src
./bin/build_aws_s3.sh --build
```

产物写回当前机对应的 `lib/<os>/<arch>/`（头仍在该槽的 `include/`，因为 Linux 有 s2n、`config.h` 按平台生成）。脚本还能编其它 os/arch 组合，那些槽**不进仓**。

依赖：mac 用 Homebrew OpenSSL；Linux 用仓内 `third_party/openssl_1_1_1`；两边都用仓内 `libcurl.a`。

`src/` 编完即可删，不要提交。

## 如何替换

换 `lib/linux/x86_64/` 的头 + `.a`，保持 `cloud_api2::aws_s3`。CMake 里的 `AWS_SDK_VERSION_*` 与 `VersionConfig.h` 对齐。
