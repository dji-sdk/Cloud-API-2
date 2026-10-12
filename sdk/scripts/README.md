# scripts/ —— Java SDK 打包构建入口

## 文件

| 文件 | 作用 |
|---|---|
| `build_java.sh` | Java SDK 全量打包入口：编 C++ JNI → 编 SDK jar → 编两个 demo → 收拢产物 → 打 zip |
| `make_source_package.sh` | 源码外包打包入口：**git 白名单**（只打已跟踪文件）+ 排除清单（测试录像、macOS 预编译产物）+ 版本/坐标/机密门禁 → 源码 tar.gz |

## 用法

### Java SDK（二进制包）

```bash
cd scripts

./build_java.sh -r              # 默认：按 CPU 核数满并行
./build_java.sh -r --jobs=8     # 限制并行度
FHSDK_JOBS=4 ./build_java.sh -r # 同上
./build_java.sh --help
```

### 源码包

```bash
cd scripts

./make_source_package.sh                               # 要求工作区干净（包内容 = commit，可追溯）
git add -A && git commit -m "release vX.Y.Z"           # 先提交
RELEASE_TAG=vX.Y.Z ./make_source_package.sh            # 发布侧指定 tag，强制与 VERSION 一致
FHSDK_ALLOW_DIRTY=1 ./make_source_package.sh           # 调试：跳过干净度检查
FHSDK_LIST_EXCLUDED=1 ./make_source_package.sh         # 只看排除清单命中哪些文件，不出包
```

排除清单在脚本内的 `PKG_EXCLUDE_RULES` 一处定义，拷贝过滤 / 干净度门禁 / 打包后回归门禁
/ 终端摘要共用它。干净度检查只看**入包**文件，被排除的路径（如本机重编的 macOS 库）
处于 modified 也不影响打包。

## 并行度

| 变量 / 参数 | 含义 |
|---|---|
| （默认） | `nproc`（Linux）或 `sysctl -n hw.ncpu`（macOS） |
| `--jobs=N` / `-j=N` / `FHSDK_JOBS` | 覆盖并行度 |
| `FHSDK_MAX_JOBS` | 旧名，与 `FHSDK_JOBS` 等价 |

开源常见做法：默认吃满核；机器内存吃紧时由用户自行降 `-j`，脚本不再默认 `nice` / 硬顶。

`build_java.sh` 解析一次并 `export FHSDK_JOBS`，下游沿用：

- `platform/java/bin/build.sh` —— `make -j$N`、gradle `--max-workers=$N`
- `flight_hub_sdk_demo/build.sh`、`new_sdk_demo/build.sh` —— 各自 `run_gradle`

## Gradle daemon

三处 `gradle.properties`（`platform/java/`、`flight_hub_sdk_demo/`、`new_sdk_demo/`）的
`org.gradle.jvmargs` 建议保持一致，便于复用 daemon。

`build_java.sh` 收尾默认 `gradlew --stop`；迭代开发想保留热 daemon 时设 `FHSDK_KEEP_DAEMON=1`。
