# CMake toolchains

本目录放**交叉编译**用的 toolchain 文件。本机 native 构建不走这里。

## 目录

1. [现状](#现状)
2. [何时需要 toolchain](#何时需要-toolchain)
3. [如何新增](#如何新增)
4. [还要改哪些地方](#还要改哪些地方)

## 现状

官方交付平台只有 **linux/x86_64**；`macosx/arm64` 仅供本仓开发者本机编译，不对外支持。`CMakePresets.json` 里两者各有 release/debug，共四条 preset。这两套都是本机编译，不需要 `toolchainFile`，所以目录里暂时没有 `.cmake`。

平台键由 `cmake/modules/cloud_api2_platform.cmake` 校验：`CLOUD_API2_OS` / `CLOUD_API2_ARCH` 对不上白名单组合会直接失败。

## 何时需要 toolchain

只有编译机和目标机不是同一套 OS/ARCH 时才加文件，例如在 x86_64 Linux 上编 aarch64。本机编本机不要写 toolchain。

文件里只放交叉工具链：`CMAKE_SYSTEM_NAME`、`CMAKE_SYSTEM_PROCESSOR`、编译器、sysroot。产品开关（`CLOUD_API2_*`、功能宏）放 preset / 业务 CMake，不要写进 toolchain。

## 如何新增

1. 在本目录加 `<os>-<arch>.cmake`，例如 `linux-aarch64.cmake`：

```cmake
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR aarch64)
set(CMAKE_C_COMPILER aarch64-linux-gnu-gcc)
set(CMAKE_CXX_COMPILER aarch64-linux-gnu-g++)
# set(CMAKE_SYSROOT /path/to/sysroot)
```

2. 在 `CMakePresets.json` 加 configure preset，挂上 `toolchainFile`，并写死平台键：

```json
{
  "name": "linux-aarch64-release",
  "inherits": "base",
  "toolchainFile": "${sourceDir}/cmake/toolchains/linux-aarch64.cmake",
  "cacheVariables": {
    "CMAKE_BUILD_TYPE": "Release",
    "CLOUD_API2_OS": "linux",
    "CLOUD_API2_ARCH": "aarch64"
  }
}
```

同时补一条同名 `buildPresets`。

3. 用 `cmake --preset <name>` 配置。交叉编不通时，先查编译器/sysroot，再查三方预编译是否已按 `${CLOUD_API2_OS}/${CLOUD_API2_ARCH}` 就位。

## 还要改哪些地方

Toolchain 只解决「用哪套编译器」。要让该平台真正能编、能打包，还需要：

| 位置 | 做什么 |
|------|--------|
| `cmake/modules/cloud_api2_platform.cmake` | 把新的 OS/ARCH 组合加入平台白名单 |
| `third_party/<库>/<os>/<arch>/` | 放下该平台预编译库（CMake 按 `CLOUD_API2_PLAT` 找） |
| `platform/java/bin/build.sh`、`scripts/build_java.sh` | `get_platform`、preset 映射、拷贝三方进 jar |
| `JniLibraryLoader` | 依赖 so/dylib 文件名 |
| `platform/java/lib/<os>-<arch>/` | JNI 槽 |

先有预编译和平台键，再填 toolchain；空的 placeholder `.cmake` 不要提前进仓。
