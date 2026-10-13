// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "h264_parser.h"
#include <iostream>
#include <string>

int main(int argc, char* argv[]) {
    std::cout << "=== H264 SEI帧解析器 Demo ===" << std::endl;

    if (argc < 2) {
        std::cerr << "用法: " << argv[0] << " <h264文件路径>" << std::endl;
        std::cerr << "样例: " << argv[0] << " ../../video/dji_stream_drone_20250916_111046.h264" << std::endl;
        return 1;
    }
    std::string h264_file = argv[1];
    
    std::cout << "正在解析文件: " << h264_file << std::endl;
    
    H264Parser parser;
    
    if (!parser.parseH264Stream(h264_file)) {
        std::cerr << "解析文件失败!" << std::endl;
        return 1;
    }
    
    // 打印SEI帧信息
    parser.printSEIFrames();
    
    // 获取SEI帧列表
    const auto& sei_frames = parser.getSEIFrames();
    
    if (sei_frames.empty()) {
        std::cout << "\n未找到任何SEI帧。" << std::endl;
    } else {
        std::cout << "\n=== 总结 ===" << std::endl;
        std::cout << "成功解析了 " << sei_frames.size() << " 个SEI帧" << std::endl;
        
        // 计算总大小
        uint32_t total_size = 0;
        for (const auto& sei : sei_frames) {
            total_size += sei.size;
        }
        std::cout << "SEI帧总大小: " << total_size << " 字节" << std::endl;
    }
    
    return 0;
}
