# H264 SEI 移除工具

这是一个用于从H264码流中移除SEI（Supplemental Enhancement Information）帧的工具，基于H264Parser库实现。

## 功能特性

- 完全移除H264码流中的所有SEI帧
- 保留所有视频帧（I帧、P帧、B帧）
- 保持H264码流的完整性和可播放性
- 提供详细的处理统计信息
- 支持大文件处理
- 验证处理结果的正确性

## 使用方法

```bash
# 基本用法
./sei_removal_demo <输入H264文件> <输出H264文件>

# 示例
./sei_removal_demo input.h264 output_no_sei.h264
```

## 构建说明

```bash
# 在项目根目录下
mkdir build
cd build
cmake ..
make sei_removal_demo

# 或者构建所有示例
make build_all_examples
```

## 输出示例

```
=== H264 SEI 移除工具 ===
输入文件: input.h264
输出文件: output_no_sei.h264

=== 分析原始文件 ===
文件大小: 8320770 字节
找到 1597 个SEI帧，其中 1597 个DJI SEI帧

原始文件统计:
  SEI帧数量: 1597
  视频帧数量: 1597
  SEI总大小: 175672 字节

=== 移除SEI帧 ===
输入文件大小: 8320770 字节
移除SEI帧 #1 偏移: 0x00000000 大小: 133 字节
移除SEI帧 #2 偏移: 0x0000008d 大小: 109 字节
...
移除SEI帧 #1597 偏移: 0x01372ae6 大小: 109 字节

=== SEI移除完成 ===
移除的SEI帧数量: 1597
移除的总大小: 175672 字节
输出文件: output_no_sei.h264

=== 验证处理结果 ===
处理后文件统计:
  SEI帧数量: 0
  视频帧数量: 1597
✓ 成功：所有SEI帧已被移除
✓ 成功：视频帧数量保持不变

=== 功能分析 ===
通用H264处理功能 (可抽象到库中):
  - NALU查找和解析 (find_next_nalu, get_nalu_type)
  - SEI帧识别和解析 (get_sei_type)
  - 关键帧检测 (is_key_frame)
  - 防竞争字节处理 (remove_emulation_prevention_bytes)
  - H264码流基本操作 (读取、写入、NALU遍历)

业务逻辑功能 (特定应用场景):
  - DJI SEI载荷解析 (processSei)
  - 特定SEI类型过滤 (EXT_SEI_TYPE_1, EXT_SEI_TYPE_2)
  - 视频帧统计分析 (帧大小、类型统计)
  - 用户自定义SEI处理逻辑
```

## 技术实现

### 核心算法

1. **NALU遍历**: 使用`find_next_nalu`函数遍历H264码流中的所有NALU
2. **类型识别**: 使用`get_nalu_type`函数识别NALU类型
3. **SEI过滤**: 识别并跳过所有SEI类型的NALU（类型6）
4. **数据复制**: 将非SEI的NALU数据写入输出文件

### 关键函数

```cpp
// 移除SEI并保存到文件
bool removeSEIAndSave(const std::string& input_filename, const std::string& output_filename);
```

### 处理流程

1. 读取输入H264文件到内存
2. 遍历所有NALU单元
3. 识别SEI类型的NALU并跳过
4. 将非SEI的NALU写入输出文件
5. 验证处理结果

## 应用场景

- **调试H264播放器**: 移除SEI帧来测试播放器对纯视频数据的处理
- **码流分析**: 分析SEI帧对文件大小的影响
- **兼容性测试**: 测试不支持SEI的播放器或设备
- **带宽优化**: 移除不必要的SEI数据来减少传输带宽

## 注意事项

1. **文件完整性**: 处理后的文件仍然是有效的H264码流
2. **播放兼容性**: 移除SEI后可能影响某些依赖SEI信息的播放器
3. **元数据丢失**: SEI中包含的元数据信息会完全丢失
4. **文件大小**: 移除SEI后文件大小会显著减小

## 文件结构

```
sei_removal_demo/
├── CMakeLists.txt          # CMake构建配置
├── README.md               # 说明文档
└── sei_removal_demo.cpp    # 主程序源码
```

## 依赖关系

- H264Parser库 (`lib/h264_parser/`)
- C++11标准库
- CMake 3.10+

## 错误处理

程序会检查以下错误情况：
- 输入文件无法打开
- 输出文件无法创建
- 文件解析失败
- 内存分配失败

所有错误都会输出详细的错误信息并返回相应的错误码。
