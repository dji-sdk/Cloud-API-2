# 自定义SEI编解码器演示

这是一个演示如何使用自定义SEI编解码器将AI分析结果嵌入到H.264视频流中的示例程序。

## 功能特性

- **SEI编码**: 将目标检测框和文本叠加数据编码为SEI格式
- **H.264兼容**: 完全符合H.264 SEI标准
- **TLV结构**: 使用Type-Length-Value结构组织数据
- **网络字节序**: 确保跨平台兼容性
- **二进制协议**: 高效紧凑的数据格式

## 支持的数据类型

### 1. 目标检测框 (CustomSeiObjectDetectionBox)
- 目标ID (0-65535)
- 目标类型 (人/车/动物等)
- 目标状态 (出现/跟踪中/消失等)
- 坐标和尺寸 (归一化到0-10000)
- 置信度 (0-100)

### 2. 文本叠加 (CustomSeiTextOverlay)
- 文本ID
- 坐标位置 (归一化到0-10000)
- 字体大小 (1-255)
- RGB颜色和透明度
- UTF-8文本内容

### 3. 自定义数据 (CustomSeiCustomData)
- 任意二进制数据
- 完全由用户定义

## 构建说明

```bash
# 在项目根目录下
mkdir build
cd build
cmake ..
make custom_sei_codec_demo

# 或者构建所有示例
make build_all_examples
```

## 使用方法

```bash
# 运行演示程序
./build/bin/custom_sei_codec_demo
```

## 输出示例

```
=== 自定义SEI编解码器演示 ===
编码成功！
原始帧大小: 20 字节
输出帧大小: 74 字节
SEI数据增加: 54 字节

输出帧前32字节 (十六进制):
00 00 00 01 06 FF FF 02 2C 01 D2 02 96 49 24 00 
00 02 0D 00 00 01 01 02 03 E8 07 D0 01 F4 03 20 
```

## SEI协议结构

### 协议头部 (12字节)
```
┌─────────────┬─────────────┬──────────────┬─────────────┐
│ Version(1B) │ Reserved(1B)│ Data Size(2B)│Timestamp(8B)│
├─────────────┼─────────────┼──────────────┼─────────────┤
│    0x01     │    0x00     │   0x00FF     │0x1234567890 │
└─────────────┴─────────────┴──────────────┴─────────────┘
```

### TLV数据结构
```
┌─────────────┬─────────────┬─────────────┐
│ Type(1B)    │ Length(2B)  │ Value(NB)   │
├─────────────┼─────────────┼─────────────┤
│    0x02     │   0x000D    │ 目标框数据   │
└─────────────┴─────────────┴─────────────┘
```

### H.264 SEI NALU结构
```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│ 起始码(4B)  │ NALU头(1B)  │ PayloadType │ PayloadLen  │
├─────────────┼─────────────┼─────────────┼─────────────┤
│ 00 00 00 01 │    0x06     │ FF FF 02    │   变长编码   │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

## 代码示例

```cpp
#include "custom_sei_codec.h"

// 创建协议头部
CustomSEIHeader header;
header.version = 1;
header.timestamp = 1234567890;
header.data_size = 0; // 自动计算
header.reserved = 0;

// 创建目标检测数据
std::vector<std::shared_ptr<CustomSeiTLVDataBase>> sei_data;

// 添加目标检测框
auto box = std::make_shared<CustomSeiObjectDetectionBox>(
    1,      // id
    1,      // type (人)
    2,      // state (跟踪中)
    1000,   // x (归一化坐标)
    2000,   // y
    500,    // width
    800,    // height
    95      // confidence
);
sei_data.push_back(box);

// 添加文本叠加
auto text = std::make_shared<CustomSeiTextOverlay>(
    1,      // id
    1000,   // x
    1800,   // y
    16,     // font_size
    255,    // color_r
    0,      // color_g
    0,      // color_b
    255,    // alpha
    "Person" // text
);
sei_data.push_back(text);

// 执行编码
std::vector<uint8_t> output_frame;
bool success = CustomSeiCodec::encode(h264_frame, header, sei_data, output_frame);
```

## 技术特点

- **高效编码**: 目标检测框仅需13字节
- **类型安全**: 使用C++强类型系统
- **网络字节序**: 确保跨平台兼容性
- **H.264标准**: 完全符合H.264 SEI规范
- **可扩展性**: 支持添加新的数据类型

## 文件结构

```
custom_sei_codec_demo/
├── CMakeLists.txt              # CMake构建配置
├── README.md                   # 说明文档
└── custom_sei_codec_demo.cpp   # 主程序源码
```

## 依赖关系

- CustomSEICodec库 (`lib/custom_sei_codec/`)
- C++20标准库
- CMake 3.10+

## 应用场景

- **实时视频流**: 将AI分析结果嵌入到H.264流中
- **前端可视化**: 在播放器中显示目标检测框和文本
- **元数据传输**: 高效传输视频相关的元数据
- **多平台兼容**: 支持各种H.264播放器和设备

## 注意事项

1. **坐标归一化**: 所有坐标都归一化到0-10000范围
2. **网络字节序**: 多字节数据使用大端序
3. **内存管理**: 使用智能指针管理内存
4. **错误处理**: 编码失败时返回false
5. **数据完整性**: 确保TLV数据长度正确
