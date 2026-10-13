# H264 Analyzer

一个用于分析 H264 视频流的 C++ 工具库，支持视频帧分析、DJI 扩展信息 SEI 解析，以及用户自定义 SEI 的编解码。

## 项目结构

```
h264_analyze/
├── lib/                          # 基础组件库
│   ├── h264_parser/             # H264Parser 核心库
│   │   ├── h264_parser.h        # 头文件
│   │   ├── h264_parser.cpp      # 实现文件
│   │   └── CMakeLists.txt       # 库的构建配置
│   └── custom_sei_codec/        # 自定义 SEI 编解码库
│       ├── custom_sei_codec.h
│       ├── custom_sei_codec.cpp
│       ├── custom_sei_data_define.h # 自定义 SEI 的 TLV 定义与工具
│       └── CMakeLists.txt
├── examples/                     # 示例程序（SDK 默认不编，-DBUILD_H264_ANALYZE_EXAMPLES=ON）
│   ├── sei_parser_demo/         # SEI 帧解析示例
│   ├── frame_size_analyzer/     # 帧大小分析示例
│   ├── sei_removal_demo/        # 从码流移除 SEI
│   ├── frame_spliter_demo/      # 按 NALU 拆帧再回写
│   ├── emulation_prevention_bytes/  # 防竞争字节演示
│   └── custom_sei_codec_demo/   # 自定义 SEI 编解码示例
├── video/                       # 样例码流（跑 examples 用）
│   ├── dji_stream_drone_20250916_111046.h264
│   ├── dji_stream_drone_20250916_111046_no_sei.h264
│   ├── dji_stream_drone_20250916_111046_with_custom_sei.h264
│   └── dji_stream_drone_20250916_111046_no_sei_with_custom_sei.h264
├── CMakeLists.txt              # 主构建配置
└── README.md                   # 项目说明
```

## 功能特性

### H264Parser库
- **SEI 帧解析**：提取和解析 H264 流中的 SEI（Supplemental Enhancement Information）帧
- **视频帧分析**：计算 I 帧、P 帧、B 帧的大小和统计信息
- **DJI 扩展 SEI 支持**：支持大疆无人机的扩展 SEI 数据格式
- **NALU 解析**：完整的 H264 NALU 单元解析功能

### Custom SEI Codec 库
- **用户自定义 SEI 编码**：将自定义 TLV 结构编码为 SEI NALU 并插入码流（Annex B）
- **用户自定义 SEI 解码**：从码流提取并解析自定义 SEI，返回协议头与 TLV 数据
- **统一防竞争字节处理**：对 Payload 自动添加/移除 emulation prevention bytes，符合标准

### 示例程序

#### 1. SEI Parser Demo (`sei_parser_demo`)
- 解析 H264 文件中的 SEI 帧
- 显示 SEI 载荷的详细信息
- 支持 DJI 特定的 SEI 类型

#### 2. Frame Size Analyzer (`frame_size_analyzer`)
- 分析视频帧大小
- 统计 I 帧、P 帧、B 帧的数量和大小
- 计算平均帧大小

#### 3. Custom SEI Codec Demos (`custom_sei_codec_demo` 系列)
- `custom_sei_codec_demo`：一体化演示（构造 TLV → 编码 → 解码校验）
- `custom_sei_encode_demo`：将自定义 TLV 编码为单个 SEI NALU
- `custom_sei_decode_demo`：对已经编码的单帧 SEI NALU 进行解码
- `custom_sei_encode_stream_demo`：向现有 H264 码流按帧插入 SEI（流式）
- `custom_sei_decode_stream_demo`：从 H264 码流逐个提取并解码 SEI（流式）

## 构建说明

### 环境要求
- CMake 3.10+
- C++17兼容的编译器（GCC、Clang、MSVC）

### 构建步骤

1. **创建构建目录**
```bash
mkdir build
cd build
```

2. **配置项目**
```bash
cmake -DBUILD_H264_ANALYZE_EXAMPLES=ON ..
```

Cloud API 2 SDK 编 JNI 时会关掉这个 option，不把示例编进产品库。单独玩本目录时打开即可。

3. **编译所有目标**
```bash
make
# 或者
cmake --build .
```

4. **编译特定目标**
```bash
# 只编译SEI解析器示例
make sei_parser_demo

# 只编译帧大小分析器
make frame_size_analyzer

# 编译所有示例
make build_all_examples

# 自定义 SEI 编解码相关
make custom_sei_codec_demo
make custom_sei_encode_demo
make custom_sei_decode_demo
make custom_sei_encode_stream_demo
make custom_sei_decode_stream_demo
```

## 使用方法

### 运行SEI解析器示例
```bash
./bin/sei_parser_demo [h264文件路径]
```

如果不提供文件路径，将使用默认的测试文件。

### 运行帧大小分析器
```bash
./bin/frame_size_analyzer <h264文件路径>
```

### 运行自定义 SEI 编解码示例

1) 一体化演示（构造 TLV → 编码 → 解码校验）
```bash
./bin/custom_sei_codec_demo
```

2) 单帧编码/解码
```bash
./bin/custom_sei_encode_demo
./bin/custom_sei_decode_demo
```

3) 流式插入/解析 SEI（针对 H264 Annex B 码流）
```bash
./bin/custom_sei_encode_stream_demo
./bin/custom_sei_decode_stream_demo
```

### 在您的项目中使用H264Parser库

1. **包含头文件**
```cpp
#include "h264_parser.h"
```

2. **链接库**
在您的CMakeLists.txt中添加：
```cmake
# 添加H264Parser库
add_subdirectory(path/to/h264_analyze/lib/h264_parser)

# 链接到您的目标
target_link_libraries(your_target h264_parser_lib)
target_include_directories(your_target PRIVATE path/to/h264_analyze/lib)
```

3. **使用示例**
```cpp
H264Parser parser;
if (parser.parseH264Stream("your_file.h264")) {
    // 获取SEI帧
    const auto& sei_frames = parser.getSEIFrames();
    
    // 获取视频帧
    const auto& video_frames = parser.getVideoFrames();
    
    // 打印信息
    parser.printSEIFrames();
    parser.printVideoFrames();
}
```

## API文档

### H264Parser类

#### 公共方法
- `bool parseH264Stream(const std::string& filename)` - 解析H264文件
- `const std::vector<SEIFrame>& getSEIFrames() const` - 获取SEI帧列表
- `const std::vector<VideoFrame>& getVideoFrames() const` - 获取视频帧列表
- `void printSEIFrames() const` - 打印SEI帧信息
- `void printVideoFrames() const` - 打印视频帧信息

#### 数据结构
- `SEIFrame` - SEI帧信息
- `VideoFrame` - 视频帧信息
- `SEISubPayload` - SEI子载荷信息

### CustomSeiCodec 类

主要接口（头文件 `lib/custom_sei_codec/custom_sei_codec.h`）：
- `encode(frame_data, header, tlv_list, output_frame)`：在给定帧前插入自定义 SEI NALU，返回新帧数据
- `decode(frame_or_nalu, out_header, out_tlv_list)`：从帧或包含起始码的 SEI NALU 中提取并解析自定义 SEI

数据结构定义见 `custom_sei_data_define.h`，包含：
- `CustomSEIHeader`（版本、保留、数据长度、时间戳）
- TLV 基类 `CustomSeiTLVDataBase` 与具体类型（自定义数据、目标检测框、多边形检测、文本叠加等）

## 技术细节

- 支持 H264 Annex B 格式
- 自动处理 emulation prevention bytes（防竞争字节）
- 支持 DJI 特定的 SEI 类型（0xf5, 0x66）以及用户自定义 SEI（TLV 扩展）
- 完整的 NALU 解析和帧类型识别