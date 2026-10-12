# Flight Hub SDK Java Demo

这是一个FlightHubSdk的Java Demo程序，展示了如何使用Flight Hub SDK的主要功能。Demo提供了完整的交互式和非交互式运行模式，支持所有核心SDK功能。

## 🚀 功能特性

### 📱 **交互式命令行界面**
- 设备列表获取和选择
- 交互式菜单系统
- 用户输入验证

### 🚁 **无人机操作**
- **OSD数据监听**: 电池、姿态、位置、模式、风速
- **飞行控制**: FlyTo点飞行、停止飞行
- **相机控制**: 拍照、相机状态监听
- **控制权管理**: 飞行控制权、负载控制权获取/释放/监听

### 🏗️ **机场操作**
- 机场实例获取
- 机场状态监控

### 📹 **直播流功能**
- 机库和无人机直播流获取
- 实时码流数据dump到文件
- SEI信息订阅（AI、AR、FOV、Assistant）
- 帧计数和字节统计

### ⚙️ **系统功能**
- SDK初始化和配置管理
- JNI库自动加载
- 优雅退出和资源清理
- 详细的日志输出
- 异常处理和错误恢复

## 📁 项目结构

```
flight_hub_sdk_demo/
├── src/main/java/com/dji/flighthubsdk/demo/
│   ├── FlightHubSDKDemo.java      # 主演示程序
│   └── ConfigManager.java         # 配置管理器
├── config.json                    # JSON配置文件
├── build.sh                       # 构建脚本
└── README.md                      # 说明文档
```

## 🛠️ 快速开始

### 1. 环境要求

- **Java**: Java 1.8 或更高版本
- **操作系统**: Linux (x86_64)
- **网络**: 能够访问Flight Hub服务器

### 2. 配置文件

创建 `config.json` 配置文件：

```json
{
  "server": {
    "address": "http://your-server-address",
    "port": "your-port",
    "notification_server": "ws://your-server-address:port/openapi/v0.9/notification/ws/v1/notifications",
    "verify_ssl": true,
    "ssl_ca_path": "/etc/ssl/certs/your_xxx.pem"
  },
  "sdk": {
    "log_level": "info",
    "log_to_console": true,
    "log_file_path": "flight_hub_sdk.log",
    "drtc_config_path": "drtc_config_node.json"
  },
  "app": {
    "encoding": "UTF-8",
    "working_dir": "."
  },
  "rtmp": {
    "server_url": "rtmp://your-server/live/",
    "stream_key": "livestream?secret=your-secret",
    "frame_rate": 30,
    "bitrate": 4000000,
    "width": 1920,
    "height": 1080,
    "enabled": false
  },
  "auth": {
    "header_key": "YOUR_AUTH_KEY",
    "header_value": "YOUR_AUTH_VALUE"
  }
}
```

`auth.header_value` 也可留空，改由环境变量 `FLIGHTHUB_AUTH_VALUE` 注入（避免把明文 token 写进配置文件）。可参考仓库内 `config_empty.json`。

### 🔒 安全约束（生产环境必看）

- `config.json` 中的 `server.verify_ssl` 默认 `true`（安全默认，校验服务器证书链与主机名/IP）；私有化自签名部署须将 `server.ssl_ca_path` 指向私有 CA 证书文件（PEM 格式，为空时使用系统 CA 信任库）；受控测试环境可显式置 `false`（**会产生 WARN 日志，存在中间人攻击风险，严禁用于生产**）。
- `drtc_config_node.json` 中的 `verify_ssl` 默认 `true`（安全默认）；私有化自签名部署须配合 `ssl_ca_path` 指向私有 CA 证书文件；受控测试环境可显式置 `false`（**仅限受控联调环境**）。
- `config.json` 中的 `sdk.log_level` 生产建议 `info`；`debug` 仅用于问题排查阶段临时开启，会输出敏感请求头 / URL / 设备属性明文。
- Demo 中所有 `YOUR_DEVICE_SN` / `YOUR_AUTH_VALUE` / `YOUR_OBJECT_KEY` 等占位符必须替换为你自己的真实值再运行；不要把真实值 commit 到公开仓库（`config.json` / `config_*.json` 已在 `.gitignore`）。
- Demo 中的坐标、SN 等参数示例仅供代码结构参考，**合法性校验（如坐标不能为 `(0,0)`、SN 归属校验等）必须由业务应用层实现**——SDK 不做业务层安全校验。

### 3. 配置文件设置

创建 `config.properties` 文件指定SDK JAR路径：
```properties
# SDK JAR包路径
sdk.jar.path=../dist/flight-hub-sdk.jar
```

### 4. 编译和运行

#### 使用Gradle（推荐）
```bash
# 编译项目
./gradlew build

# 运行Demo
./gradlew run

# 创建可执行JAR
./gradlew executableJar
```

#### 使用构建脚本
```bash
# 编译项目
./build.sh

# 编译/直接运行Demo（无法通过命令行交互测试）
./build.sh -r

# 编译Demo为可执行文件
./build.sh -e

# 清理构建文件
./build.sh -c
```

## 🎮 使用指南

### 交互式模式使用

1. **启动程序**
   ```bash
   ./build.sh -e
   cd ./build/libs
   # build 已自动拷贝 config*.json 与 drtc_config_node*.json；无需再手工 cp
   java -jar flight-hub-sdk-demo-*-executable.jar config_204.json
   # 或默认 config.json：
   # java -jar flight-hub-sdk-demo-*-executable.jar
   ```
> 注: 默认 `drtc_config_node.json` 为**占位空模板**（host=`xx.xx.xx.xx`，与 `config_empty.json` 同风格，与其它 config 同级），发包可用。真实环境请改 host，或用 `config_204.json`（指向 `drtc_config_node_204.json`）。也可设环境变量 `FHSDK_DRTC_CONFIG`。

2. **程序启动流程**
   - 自动加载配置文件
   - 初始化SDK
   - 获取设备列表
   - 选择设备（如果多个设备）
   - 显示主菜单

3. **主菜单功能选择**
   ```
   [1] 拉取码流
   [2] 拍照
   [3] 退出
   [4] 监听飞机 OSD
   [5] Flyto
   [6] Flyto Stop
   [7] 监听相机状态
   [8] 获取飞行控制权
   [9] 释放飞行控制权
   [10] 监听飞行控制权状态
   [11] 获取负载控制权
   [12] 释放负载控制权
   [13] 监听负载控制权状态
   ```

4. **功能测试步骤**
   - 输入数字选择功能（1-13）
   - 按Enter键执行功能
   - 观察日志输出
   - 按Enter键返回主菜单

### 部分功能测试详解

#### 选取设备
- **设备选择**: 终端上将自动拉取设备列表，输入对应数字选择对应设备进行测试

#### 1. 拉取码流测试
- **选择**: 输入 `1` 并按Enter
- **功能**: 获取机库和无人机直播流
- **输出**: 实时码流数据保存到 `stream_data/` 目录
- **文件**: `dji_stream_dock_*.h264`, `dji_stream_drone_*.h264`
- **停止**: 按Enter键停止

#### 2. 拍照测试
- **选择**: 输入 `2` 并按Enter
- **功能**: 执行无人机拍照操作
- **输出**: 拍照成功/失败状态
- **停止**: 按Enter键返回

#### 3. 监听飞机OSD测试
- **选择**: 输入 `4` 并按Enter
- **功能**: 监听无人机状态数据
- **输出**: 电池、姿态、位置、模式、风速信息
- **停止**: 按Enter键返回

#### 4. FlyTo飞行测试
- **选择**: 输入 `5` 并按Enter
- **功能**: 执行点到点飞行任务
- **输出**: 飞行任务启动状态和进度
- **停止**: 按Enter键返回

#### 5. 控制权测试
- **获取控制权**: 选择 `8` 或 `11`
- **释放控制权**: 选择 `9` 或 `12`
- **监听状态**: 选择 `10` 或 `13`
- **输出**: 控制权状态变化

## 🔧 运行模式

### 交互式模式
- 显示菜单，等待用户选择
- 支持所有功能的手动操作
- 实时用户交互
- 适合功能测试和调试

## 🛡️ 错误处理

### 常见错误及解决方法

1. **SDK初始化失败**
   ```bash
   # 检查配置文件
   cat config.json
   
   # 验证网络连接
   ping your-server-address
   ```

2. **设备连接失败**
   - 检查设备序列号是否正确
   - 确认设备在线状态
   - 验证网络连接

### 调试方法

1. **启用详细日志**

   问题排查阶段可将 `config.json` 中 `sdk.log_level` 临时改为 `debug`，输出更详细的请求头 / URL / 设备属性；排查结束务必恢复为 `info`（详见 §「🔒 安全约束」）。