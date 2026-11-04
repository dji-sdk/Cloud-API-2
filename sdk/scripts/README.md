# scripts/ —— Java SDK 打包构建入口

## 文件

| 文件 | 作用 |
|---|---|
| `build_java.sh` | Java SDK 全量打包入口：编 C++ JNI → 编 SDK jar → 编两个 demo → 收拢产物 → 打 zip |

## 用法

### Java SDK（二进制包）

```bash
cd scripts

./build_java.sh -r -j=10        # Release，10 路并行（-j 必传）
./build_java.sh -d --jobs=4     # Debug，4 路并行
./build_java.sh -r -j=$(nproc)  # 吃满核
./build_java.sh --help          # 帮助不需要 -j
```

## 并行度

`--jobs=N` / `-j=N` 是**必传参数**，没有默认值：不传直接报错退出，不再按核数推算。
本机核数用 `nproc` 查（`lscpu` 看物理/逻辑核细分）。

| 参数 / 变量 | 含义 |
|---|---|
| `--jobs=N` / `-j=N` | **必传**。整数，下限 1；两者同时出现时后者生效 |
| `FHSDK_JOBS` / `FHSDK_MAX_JOBS` | **不再是输入**。由脚本按 `--jobs` 定版后 `export` 给下游 |

取值校验：非十进制整数（`abc` / `-3` / `1.5`）直接报错退出，不做猜测性纠正；
`0` 会带警告修正为 1（`make -j0`、`gradle --max-workers=0` 会直接失败）。

之所以做成必传：并行度取决于当时机器上还跑着什么（IDE、其他构建、容器配额），
脚本猜不准，猜错的代价是把机器压到失去响应。内存吃紧时自行降 `-j`，脚本不做默认 `nice` / 硬顶。

定版后 `export FHSDK_JOBS`（并同步 `FHSDK_MAX_JOBS` 兼容旧名），下游沿用同一值：

- `platform/java/bin/build.sh` —— `make -j$N`、gradle `--max-workers=$N`
- `flight_hub_sdk_demo/build.sh`、`new_sdk_demo/build.sh` —— 各自 `run_gradle`
- `third_party/*/bin/build_*.sh` —— 读 `FHSDK_MAX_JOBS`

这些子脚本单独直接调用时仍各自带兜底默认值，不受本次改动影响。

## Gradle daemon

三处 `gradle.properties`（`platform/java/`、`flight_hub_sdk_demo/`、`new_sdk_demo/`）的
`org.gradle.jvmargs` 建议保持一致，便于复用 daemon。

`build_java.sh` 收尾默认 `gradlew --stop`；迭代开发想保留热 daemon 时设 `FHSDK_KEEP_DAEMON=1`。
