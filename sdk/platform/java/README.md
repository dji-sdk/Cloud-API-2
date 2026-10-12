# Flight Hub SDK - Java Platform

这是Flight Hub SDK的Java平台实现，基于SWIG技术将C++ API封装为Java接口，为Java开发者提供便捷的无人机和机场管理功能。

## 项目结构

```
dji_flight_hub_sdk/platform/java/
├── bin/                          # 编译脚本
│   └── build.sh                  # 主构建脚本
├── lib/                          # JNI原生库（按平台分类）
│   ├── linux-x86_64/            # Linux x86_64
│   └── README.md                # 库目录说明
├── swig/                         # SWIG接口定义
│   └── flight_hub_sdk.i         # SWIG接口文件
├── flight_hub_sdk/               # Java SDK封装
│   ├── src/main/java/           # Java源码
│   └── build.gradle             # SDK构建配置
├── flight_hub_sdk_demo/          # 演示项目
│   ├── src/main/java/           # Demo源码
│   ├── src/main/resources/      # 配置文件
│   ├── build.gradle             # Demo构建配置
│   └── README.md                # Demo说明
├── flight_hub_sdk_docs/          # 文档生成模块（Javadoc + Dokka）
│   ├── build.gradle             # 文档构建配置
│   └── README.md                # 文档模块说明
├── dist/                         # 打包产物
├── build.gradle                  # 根项目构建配置
├── settings.gradle               # Gradle项目设置
├── gradle.properties             # 版本和配置信息
├── CMakeLists.txt                # CMake构建配置
└── README.md                     # 本文档
```

## 快速开始

### 1. 环境要求

- **操作系统**: Linux x86_64（官方支持平台，见仓根 [`README.md`](../../README.md)）
- **Java**: JDK 1.8 或以上版本
- **CMake**: 3.16 或以上版本
- **SWIG**: 4.0 或以上版本
- **C++编译器**: 支持C++17的编译器
- **Gradle**: 8.5 或以上版本（使用Gradle Wrapper）

### 2. 构建SDK

```bash
# 进入Java平台目录
cd dji_flight_hub_sdk/platform/java

# 给构建脚本添加执行权限
chmod +x bin/build.sh

# 构建所有组件（推荐）
./bin/build.sh --all

# 或者分步构建
./bin/build.sh --swig-only     # 只生成SWIG包装代码
./bin/build.sh --cpp-only      # 只构建C++库
./bin/build.sh --java-only     # 只构建Java SDK
```

### 3. 运行演示

```bash
# 使用Gradle运行演示
./gradlew :flight_hub_sdk_demo:run

# 或构建JAR后运行
./gradlew build
java -jar flight_hub_sdk_demo/build/libs/flight-hub-sdk-demo-executable.jar
```

## 使用示例

### 基本用法

```java
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.model.*;

// 1. 创建配置
FlightHubSdkConfig config = new FlightHubSdkConfig();
config.setAddress("your-server.com");
config.setPort("8080");

// 2. 初始化SDK
FlightHubSdkErrorCode result = FlightHubSDK.initialize(config);
if (result == FlightHubSdkErrorCode.SUCCESS) {
    System.out.println("SDK 初始化成功");
}

// 3. 注入 gateway 鉴权头（HTTP + WebSocket）
List<HeaderKeyValue> authHeaders = new ArrayList<>();
authHeaders.add(new HeaderKeyValue("x-your-auth-key", "your-auth-value"));
FlightHubSDK.setHttpHeaders(authHeaders);
FlightHubSDK.setWebSocketAuthHeaders(authHeaders);

// 4. 获取 Workspace（全局单例），再拿 DeviceManageModule 拉设备列表
Workspace workspace = FlightHubSDK.getWorkspaceInstance();
workspace.getDeviceManageModule().getDeviceList((devices, errorCode) -> {
    if (errorCode == FlightHubSdkErrorCode.SUCCESS) {
        System.out.println("设备数量: " + devices.size());
        for (DeviceTopo device : devices) {
            System.out.println("设备: " + device);
        }
    }
});

// 5. 获取无人机实例
IDrone drone = FlightHubSDK.getDroneInstance("drone_sn");
if (drone != null) {
    // 执行无人机操作
}

// 6. 关闭SDK
FlightHubSDK.shutdown();
```

### 错误处理

```java
FlightHubSdkErrorCode result = FlightHubSDK.initialize(config);
switch (result) {
    case SUCCESS:
        System.out.println("初始化成功");
        break;
    case ERROR_ALREADY_INITIALIZED:
        System.out.println("SDK已经初始化");
        break;
    case ERROR_INIT_FAILED:
        System.out.println("初始化失败");
        break;
    default:
        System.out.println("未知错误: " + result);
}
```

## API文档

本工程采用独立文档模块 `flight_hub_sdk_docs` 生成文档，不复制 SDK 源码。

### 文档生成命令

在 `dji_flight_hub_sdk/platform/java` 目录执行：

```bash
# 一次生成全部文档
./gradlew :flight_hub_sdk_docs:docsAll

# 分别生成
./gradlew :flight_hub_sdk_docs:javadoc
./gradlew :flight_hub_sdk_docs:dokkaHtml
./gradlew :flight_hub_sdk_docs:dokkaGfm
```

### 文档输出目录

- Javadoc HTML: `flight_hub_sdk_docs/build/docs/javadoc/`
- Dokka HTML: `flight_hub_sdk_docs/build/docs/dokka-html/`
- Dokka Markdown(GFM): `flight_hub_sdk_docs/build/docs/dokka-gfm/`

### 主要类

- **`FlightHubSDK`**: SDK主入口类，提供静态方法
- **`FlightHubSdkConfig`**: SDK配置类
- **`FlightHubSdkErrorCode`**: 错误代码枚举
- **`IDrone`**: 无人机接口（由SWIG生成）
- **`IDock`**: 机场接口（由SWIG生成）
- **`DeviceTopo`**: 设备拓扑结构

### 核心方法

```java
// SDK生命周期管理
FlightHubSdkErrorCode initialize(FlightHubSdkConfig config)
FlightHubSdkErrorCode initialize()
FlightHubSdkErrorCode shutdown()
boolean isInitialized()

// 配置和信息
String getVersion()
String getBuildInfo()
FlightHubSdkErrorCode setLogLevel(String level)
FlightHubSdkConfig getConfig()

// 设备管理
void getDeviceList(String bindingUuid, DeviceListCallback callback)
IDrone getDroneInstance(String sn)
IDock getDockInstance(String sn)
```

## 构建选项

构建脚本 `bin/build.sh` 支持以下选项：

```bash
# 基本选项
./bin/build.sh --help          # 显示帮助信息
./bin/build.sh --clean         # 清理构建目录
./bin/build.sh --debug         # 构建Debug版本
./bin/build.sh --release       # 构建Release版本
./bin/build.sh --all           # 构建所有版本

# 分步构建
./bin/build.sh --swig-only     # 只生成SWIG包装代码
./bin/build.sh --cpp-only      # 只构建C++库
./bin/build.sh --java-only     # 只构建Java SDK
```

## 开发指南

### 添加新的API

1. 在 `dji_flight_hub_sdk/src/api/` 目录下的头文件中定义C++ API
2. 更新 `swig/flight_hub_sdk.i` 文件，添加新的接口包装
3. 在 `flight_hub_sdk/src/main/java/` 中添加相应的Java封装类
4. 重新构建SDK

### 修改SWIG配置

编辑 `swig/flight_hub_sdk.i` 文件：

- 添加新的头文件包含
- 定义回调接口
- 配置类型映射
- 添加辅助方法

### 自定义构建

编辑 `CMakeLists.txt` 文件来自定义C++构建配置：

- 添加编译选项
- 修改包含路径
- 配置链接库

## 故障排除

### JNI库加载失败

**问题**: `java.lang.UnsatisfiedLinkError`

**解决方案**:
1. 确保已正确构建JNI库
2. 检查库文件在正确的平台目录中
3. 设置 `java.library.path` 系统属性

### SWIG生成失败

**问题**: SWIG编译错误

**解决方案**:
1. 检查SWIG版本（需要4.0+）
2. 验证头文件路径正确
3. 检查SWIG接口文件语法

### Gradle构建失败

**问题**: Gradle任务执行失败

**解决方案**:
1. 确保JDK版本正确（1.8+）
2. 检查网络连接（下载依赖）
3. 清理构建缓存：`./gradlew clean`

### 运行时错误

**问题**: SDK初始化失败

**解决方案**:
1. 检查服务器连接配置
2. 验证token有效性
3. 查看日志输出获取详细错误信息

## 版本信息

- **SDK版本**: 3.1.0
- **Java版本**: 1.8+
- **SWIG版本**: 4.3.1
- **CMake版本**: 3.16+
- **Gradle版本**: 8.5

## 许可证

请参考项目根目录的许可证文件。

## 支持

如有问题或建议，请：

1. 查看本文档和Demo代码
2. 检查构建日志和错误信息
3. 联系DJI技术支持团队

---

**注意**: 某些功能需要连接到实际的Flight Hub服务器才能正常工作。在开发和测试阶段，可以使用模拟数据进行功能验证。