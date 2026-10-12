// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <iostream>
#include <string>
#include "../lib/h264_parser/h264_parser.h"

int main(int argc, char* argv[]) {
    if (argc != 3) {
        std::cout << "用法: " << argv[0] << " <输入H264文件> <输出H264文件>" << std::endl;
        std::cout << "示例: " << argv[0] << " input.h264 output_no_sei.h264" << std::endl;
        return 1;
    }
    
    std::string input_file = argv[1];
    std::string output_file = argv[2];
    
    std::cout << "=== H264 SEI 移除工具 ===" << std::endl;
    std::cout << "输入文件: " << input_file << std::endl;
    std::cout << "输出文件: " << output_file << std::endl;
    std::cout << std::endl;
    
    // 创建H264解析器
    H264Parser parser;
    
    // 首先分析原始文件中的SEI帧
    std::cout << "=== 分析原始文件 ===" << std::endl;
    if (!parser.parseH264Stream(input_file)) {
        std::cout << "解析输入文件失败!" << std::endl;
        return 1;
    }
    
    // 显示SEI帧统计信息
    const auto& sei_frames = parser.getSEIFrames();
    const auto& video_frames = parser.getVideoFrames();
    
    std::cout << "原始文件统计:" << std::endl;
    std::cout << "  SEI帧数量: " << sei_frames.size() << std::endl;
    std::cout << "  视频帧数量: " << video_frames.size() << std::endl;
    
    if (!sei_frames.empty()) {
        uint32_t total_sei_size = 0;
        for (const auto& frame : sei_frames) {
            total_sei_size += frame.size;
        }
        std::cout << "  SEI总大小: " << total_sei_size << " 字节" << std::endl;
    }
    
    std::cout << std::endl;
    
    // 移除SEI并保存到新文件
    std::cout << "=== 移除SEI帧 ===" << std::endl;
    if (!parser.removeSEIAndSave(input_file, output_file)) {
        std::cout << "移除SEI失败!" << std::endl;
        return 1;
    }
    
    std::cout << std::endl;
    
    // 分析处理后的文件
    std::cout << "=== 验证处理结果 ===" << std::endl;
    H264Parser new_parser;
    if (!new_parser.parseH264Stream(output_file)) {
        std::cout << "解析输出文件失败!" << std::endl;
        return 1;
    }
    
    const auto& new_sei_frames = new_parser.getSEIFrames();
    const auto& new_video_frames = new_parser.getVideoFrames();
    
    std::cout << "处理后文件统计:" << std::endl;
    std::cout << "  SEI帧数量: " << new_sei_frames.size() << std::endl;
    std::cout << "  视频帧数量: " << new_video_frames.size() << std::endl;
    
    // 验证结果
    if (new_sei_frames.empty()) {
        std::cout << "✓ 成功：所有SEI帧已被移除" << std::endl;
    } else {
        std::cout << "✗ 警告：仍有 " << new_sei_frames.size() << " 个SEI帧未被移除" << std::endl;
    }
    
    if (video_frames.size() == new_video_frames.size()) {
        std::cout << "✓ 成功：视频帧数量保持不变" << std::endl;
    } else {
        std::cout << "✗ 警告：视频帧数量发生变化 (原始: " << video_frames.size() 
                  << ", 处理后: " << new_video_frames.size() << ")" << std::endl;
    }
    
    return 0;
}
