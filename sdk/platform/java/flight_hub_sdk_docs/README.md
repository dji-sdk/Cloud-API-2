# flight_hub_sdk_docs

`flight_hub_sdk_docs` 是 Flight Hub Java SDK 的独立文档模块。

- 不复制 `flight_hub_sdk` 源码
- 直接引用 `../flight_hub_sdk/src/main/java`
- 生成 Javadoc / Dokka HTML / Dokka GFM(Markdown)
- **对外文档**：`javadoc` 与 Dokka 任务已排除 `com.dji.flighthubsdk.generated`、`com.dji.flighthubsdk.internal.convert`、`com.dji.flighthubsdk.util`（详见 `doc/.../3_FHSDK_Java_Javadoc进一步优化.md` 优化点一）；错误码说明见 `com.dji.flighthubsdk.value.FlightHubSdkErrorCode`。

## JDK 要求（文档构建）

**请使用 JDK 17 跑 Gradle 生成文档**（`javadoc` 随 Gradle 所用 JVM 变化；用 JDK 11 等版本生成的 Javadoc 搜索/脚本与 17 不一致，易出现右上角搜索无法跳转等问题）。

打文档前建议确认 **`JAVA_HOME` 与 `java -version` 一致且均为 17**：

```bash
echo "$JAVA_HOME"
java -version
"$JAVA_HOME/bin/java" -version   # 两者应同为 17
```

若使用 **jenv**：需先把本机 JDK 17 加入 jenv（`jenv add <JDK 17 安装路径>`，如 `jenv add /usr/lib/jvm/java-17-openjdk`），再在 `platform/java` 下 `jenv local <版本名>` 或 `jenv shell <版本名>`；否则可能出现 **`PATH` 已是 17、`JAVA_HOME` 仍指向 11**，Gradle 仍用旧 JDK 打文档的情况。切换 JDK 后可执行 **`./gradlew --stop`** 再跑任务，避免 Daemon 占用旧 JVM。

## 常用命令

在 `dji_flight_hub_sdk/platform/java` 目录下执行：

```bash
# 对外文档（不含 @implNote 链路信息）
./gradlew :flight_hub_sdk_docs:javadoc        # Javadoc HTML
./gradlew :flight_hub_sdk_docs:dokkaHtml      # Dokka HTML
./gradlew :flight_hub_sdk_docs:dokkaGfm       # Dokka GFM Markdown
./gradlew :flight_hub_sdk_docs:docsAll        # 以上三者一起生成

# 对内文档（含 @implNote 链路信息：HTTP path / WebSocket topic / DRC method）
./gradlew :flight_hub_sdk_docs:javadocInternal   # 对内 Javadoc HTML
./gradlew :flight_hub_sdk_docs:docsInternal      # 对内全套文档
```

## 对外 vs 对内文档

| 区别 | 对外（`javadoc` / `docsAll`） | 对内（`javadocInternal` / `docsInternal`） |
|------|------------------------------|------------------------------------------|
| `@implNote` | 不渲染（用户看不到） | 渲染为 "Implementation Note:" |
| 排除包 | generated / internal.convert / util | 同左 |
| 用途 | 交付给客户 | 研发内部对齐链路类型、后端接口路径 |

`@implNote` 格式约定见 `doc/.../4_FHSDK_CPP注释开发SOP.md`。

## 产物目录

- Javadoc（对外）: `flight_hub_sdk_docs/build/docs/javadoc/`
- Javadoc（对内）: `flight_hub_sdk_docs/build/docs/javadoc-internal/`
- Dokka HTML: `flight_hub_sdk_docs/build/docs/dokka-html/`
- Dokka GFM: `flight_hub_sdk_docs/build/docs/dokka-gfm/`
