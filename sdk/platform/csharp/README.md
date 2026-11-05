# flight_hub_sdk

.NET 8 SDK，运行平台为 Linux x64（glibc）。PackageId 和程序集名均为
`flight_hub_sdk`，命名空间为 `FlightHubSdk`。不需要 JDK、JNI 或 Java 服务。

## 构建

在 SDK 根目录或任意目录执行：

```bash
bash scripts/build_csharp.sh -r --jobs=8
```

构建机器需要 .NET SDK 8 或更新、CMake 3.20+、SWIG 4.3+、支持 C++20 的
GCC/Clang、Python 3、patchelf、binutils，以及原有 Java native 构建所需的
系统开发库（包括 zlib）。必须包含仓库的 Linux x64 三方预编译文件。
版本从根目录 `VERSION` 读取；改版本时先修改该文件。

输出目录：`build/csharp/dist/`。

- `flight_hub_sdk.<版本>.nupkg`：包含托管 DLL、原生桥接库、Java 同款六个动态依赖和许可文件。
- `flight_hub_sdk.dll`：单独的托管程序集；单独复制此文件不足以运行。
- `api-parity.json`：Java/C# 生成类型和业务桥接入口覆盖报告。

构建脚本会检查 ELF 架构、动态依赖，随后从实际 NuGet 包还原 smoke 工程，验证
原生版本调用、UTF-8 字符串、64 位数值及 C++ → C# director 回调。smoke 不连接业务环境。
测试使用全新的包缓存，`flight_hub_sdk` 固定从本次本地产物还原；若机器缺少
.NET targeting packs，只允许对应 Microsoft 框架包从官方 NuGet 源下载。

版本以仓库 `VERSION` 和构建时生成的 `dist/api-parity.json` 为准。
构建检查覆盖打包与互操作，设备、直播和飞控仍需在目标业务环境单独验证。

## 业务项目引用

```bash
dotnet add package flight_hub_sdk --version 3.4.0 --source /path/to/sdk/build/csharp/dist
```

```csharp
using FlightHubSdk;
using Sdk = FlightHubSdk.FlightHubSdk;

using var headers = new HeaderKeyValueVector();
using var header = new HeaderKeyValue();
header.key = "Authorization"; // 按实际网关约定填写名称和值
header.value = "Bearer ...";
headers.Add(header);
if (Sdk.setHttpHeaders(headers) != FlightHubSdkErrorCode.SUCCESS ||
    Sdk.setWebSocketAuthHeaders(headers) != FlightHubSdkErrorCode.SUCCESS)
    throw new InvalidOperationException("鉴权头设置失败");

using var config = new FlightHubSdkConfig();
config.address = "https://your-host";
config.port = "30812";
config.notification_server_address = "wss://your-host:30812/openapi/v0.9/notification/ws/v1/notifications";
if (Sdk.initialize(config) != FlightHubSdkErrorCode.SUCCESS)
    throw new InvalidOperationException("初始化失败");

// 在服务停止时：取消全部订阅，等待在途请求/回调结束，然后 shutdown。
// 最后释放回调对象和模块代理。
Sdk.shutdown();
```

## 与 Java 的能力对应

C# SWIG 入口直接复用 `platform/java/swig/flight_hub_sdk.i` 的全部模块、
shared_ptr、集合、回调及扩展桥接。Java 专用类型映射用 `SWIGJAVA` 条件保护，
Java 原有构建行为保留。原生实现与 Java 完全共用。

功能覆盖以 Java 的 generated 原生绑定层为基准；Java 手写 core/bean/util 的便利包装
没有逐行翻译。C# 直接使用 `FlightHubSdk`、`IWorkspace`、各 `I…Module` 和
`…Callback` 类型。已有桥接中的 `Java` 方法/参数后缀保留，便于逐一对应。
覆盖检查核对类型和业务桥接名称，不代表签名语义、回调时序和实机行为已经全部验证。

- C++ 字段映射为 C# 属性，保持原生字段名（如 `error_code`）。
- 集合使用 `Add`、`Count` 和索引器等 .NET 接口。
- Java 手写 value 枚举在 C# 直接生成原生枚举。
- `LiveSyncListParams` / `LiveSyncUpdateParams` 中 `set_sn` 等布尔字段在 C# 为
  `has_sn` 等，避免与属性 setter 冲突；含义不变。
- Linux LP64 的 `long` / `unsigned long` 映射为 C# `long` / `ulong`。

## 回调和资源生命周期

继承生成的 `…Callback`，override 对应 `on…` 方法，再传给对应 `…Java` 桥接方法。
服务必须强引用回调对象，直到一次性请求完成或订阅取消且在途回调结束；
不可在发起异步调用后立即 `Dispose`，也不可只依赖局部变量的词法作用域。
订阅回调建议存储为服务字段，记录 listenId，停止时调用对应 cancel 方法。

回调在原生线程执行，必须在 override 内捕获异常，不能让异常跨越原生边界。
回调参数可能是原生临时对象的借用代理；需要异步使用时，在回调返回前复制到自己的
C# DTO/数组。生成代理实现 IDisposable，按拥有关系及时释放。

NuGet 通过 `runtimes/linux-x64/native/` 交付原生库，每个库的 RPATH 都为 `$ORIGIN`。
生产容器需要兼容的 glibc/libstdc++ 等系统运行库。Alpine/musl、Windows、ARM64、
NativeAOT 和裁剪发布未纳入本次支持范围。公开发布前需自行核对三方闭源库的分发条件。
