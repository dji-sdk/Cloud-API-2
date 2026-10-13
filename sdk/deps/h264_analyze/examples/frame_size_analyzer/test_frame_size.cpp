// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "h264_parser.h"
#include <iostream>

int main(int argc, char* argv[]) {
    if (argc != 2) {
        std::cout << "用法: " << argv[0] << " <h264文件路径>" << std::endl;
        return 1;
    }
    
    std::string filename = argv[1];
    H264Parser parser;
    
    std::cout << "开始解析H264文件: " << filename << std::endl;
    
    if (parser.parseH264Stream(filename)) {
        std::cout << "解析成功！" << std::endl;
        
        // 打印SEI帧信息
        parser.printSEIFrames();
        
        // 打印视频帧信息
        parser.printVideoFrames();
        
        // 获取帧数据用于进一步分析
        const auto& video_frames = parser.getVideoFrames();
        const auto& sei_frames = parser.getSEIFrames();
        
        std::cout << "\n=== 总结 ===" << std::endl;
        std::cout << "找到 " << video_frames.size() << " 个视频帧" << std::endl;
        std::cout << "找到 " << sei_frames.size() << " 个SEI帧" << std::endl;
        
    } else {
        std::cout << "解析失败！" << std::endl;
        return 1;
    }
    
    return 0;
}
