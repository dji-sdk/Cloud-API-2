// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

// 读取dji_stream_drone_20250916_111046_no_sei.h264或者dji_stream_drone_20250916_111046.h264
// 使用H264Parser, 把 H264 码流拆分成帧(把每个 NALU 都拆出来)
// 拆出来的 NALU 可以都保存到文件中, 理论上拆出来的数据回写到文件, 和原来的文件是一样的

#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <iomanip>
#include <map>
#include <algorithm>
#include "../../lib/h264_parser/h264_parser.h"

int main(int argc, char* argv[]) {
    if (argc != 3) {
        std::cout << "用法: " << argv[0] << " <输入H264文件> <输出H264文件>" << std::endl;
        std::cout << "样例: " << argv[0]
                  << " ../../video/dji_stream_drone_20250916_111046.h264 reconstructed.h264" << std::endl;
        return 1;
    }
    std::string input_file = argv[1];
    std::string output_file = argv[2];
    
    std::cout << "=== H264帧拆分演示 ===" << std::endl;
    std::cout << "输入文件: " << input_file << std::endl;
    
    // 创建H264Parser实例
    H264Parser parser;
    
    // 拆分所有NALU
    std::cout << "\n=== NALU拆分测试 ===" << std::endl;
    std::vector<NALU> all_nalus = parser.splitNALUsFromFile(input_file);
    std::cout << "总共拆分出 " << all_nalus.size() << " 个NALU" << std::endl;
    
    // 统计NALU类型分布
    std::map<uint8_t, int> nalu_type_count;
    for (const auto& nalu : all_nalus) {
        nalu_type_count[nalu.nalu_type]++;
    }
    
    std::cout << "\nNALU类型分布:" << std::endl;
    for (const auto& pair : nalu_type_count) {
        std::string type_name;
        switch (pair.first) {
            case 0x01: type_name = "P帧"; break;
            case 0x02: type_name = "B帧"; break;
            case 0x05: type_name = "IDR帧"; break;
            case 0x06: type_name = "SEI"; break;
            case 0x07: type_name = "SPS"; break;
            case 0x08: type_name = "PPS"; break;
            case 0x09: type_name = "AUD"; break;
            default: type_name = "其他"; break;
        }
        std::cout << "  类型 " << (int)pair.first << " (" << type_name << "): " << pair.second << " 个" << std::endl;
    }
    
    // 统计信息
    int i_frame_count = 0;
    int p_frame_count = 0;
    uint32_t total_size = 0;
    
    // 从NALU中统计视频帧信息
    for (const auto& nalu : all_nalus) {
        if (nalu.nalu_type >= 0x01 && nalu.nalu_type <= 0x05) {
            bool is_key = H264Parser::is_key_frame(nalu.data.data() + 4, nalu.data.size() - 4);
            char frame_type = H264Parser::getFrameType(nalu.nalu_type, is_key);
            
            if (frame_type == 'I') {
                i_frame_count++;
            } else if (frame_type == 'P') {
                p_frame_count++;
            }
            total_size += nalu.size;
        }
    }
    
    // 创建重构文件（使用完整NALU）
    std::ofstream reconstructed_file(output_file, std::ios::binary);
    if (!reconstructed_file.is_open()) {
        std::cout << "无法创建重构文件: " << output_file << std::endl;
        return -1;
    }
    
    // 使用完整NALU重构文件
    std::cout << "\n=== 使用完整NALU重构文件 ===" << std::endl;
    uint32_t total_nalu_size = 0;
    for (size_t i = 0; i < all_nalus.size(); ++i) {
        const NALU& nalu = all_nalus[i];
        reconstructed_file.write(reinterpret_cast<const char*>(nalu.data.data()), nalu.data.size());
        total_nalu_size += nalu.size;
        
        // 检查前几个和最后几个NALU
        if (i < 3 || i >= all_nalus.size() - 3) {
            std::cout << "NALU #" << (i + 1) << " 偏移: 0x" << std::hex << nalu.offset 
                      << " 大小: " << std::dec << nalu.size 
                      << " 类型: " << (int)nalu.nalu_type << std::endl;
        }
    }
    
    // 打印前几个和后几个NALU的详细信息
    std::cout << "\n=== NALU详细信息 ===" << std::endl;
    for (size_t i = 0; i < std::min(size_t(5), all_nalus.size()); ++i) {
        const NALU& nalu = all_nalus[i];
        std::cout << "NALU #" << (i + 1) << ":" << std::endl;
        std::cout << "  偏移: 0x" << std::hex << std::setfill('0') << std::setw(8) << nalu.offset << std::dec << std::endl;
        std::cout << "  大小: " << nalu.size << " 字节" << std::endl;
        std::cout << "  类型: " << (int)nalu.nalu_type << " (0x" << std::hex << (int)nalu.nalu_type << ")" << std::dec << std::endl;
    }
    
    if (all_nalus.size() > 10) {
        std::cout << "... (省略中间 " << (all_nalus.size() - 10) << " 个NALU) ..." << std::endl;
        
        for (size_t i = all_nalus.size() - 5; i < all_nalus.size(); ++i) {
            const NALU& nalu = all_nalus[i];
            std::cout << "NALU #" << (i + 1) << ":" << std::endl;
            std::cout << "  偏移: 0x" << std::hex << std::setfill('0') << std::setw(8) << nalu.offset << std::dec << std::endl;
            std::cout << "  大小: " << nalu.size << " 字节" << std::endl;
            std::cout << "  类型: " << (int)nalu.nalu_type << " (0x" << std::hex << (int)nalu.nalu_type << ")" << std::dec << std::endl;
        }
    }
    
    reconstructed_file.close();
    
    // 打印统计信息
    std::cout << "\n=== 统计信息 ===" << std::endl;
    std::cout << "I帧数量: " << i_frame_count << " 个" << std::endl;
    std::cout << "P帧数量: " << p_frame_count << " 个" << std::endl;
    std::cout << "总大小: " << total_size << " 字节" << std::endl;
    
    if (i_frame_count > 0) {
        std::cout << "I帧平均大小: " << (total_size / (i_frame_count + p_frame_count)) << " 字节" << std::endl;
    }
    
    // 获取原始文件大小
    std::ifstream original_file(input_file, std::ios::binary);
    original_file.seekg(0, std::ios::end);
    size_t original_file_size = original_file.tellg();
    original_file.close();
    
    std::cout << "\n=== 重构文件信息 ===" << std::endl;
    std::cout << "重构文件已保存: " << output_file << std::endl;
    std::cout << "总NALU大小: " << total_nalu_size << " 字节" << std::endl;
    std::cout << "原始文件大小: " << original_file_size << " 字节" << std::endl;
    std::cout << "差异: " << (original_file_size - total_nalu_size) << " 字节" << std::endl;
    
    // 检查最后一个NALU
    if (!all_nalus.empty()) {
        const NALU& last_nalu = all_nalus.back();
        std::cout << "\n最后一个NALU信息:" << std::endl;
        std::cout << "  偏移: 0x" << std::hex << last_nalu.offset << std::dec << std::endl;
        std::cout << "  大小: " << last_nalu.size << " 字节" << std::endl;
        std::cout << "  类型: " << (int)last_nalu.nalu_type << std::endl;
        std::cout << "  结束位置: 0x" << std::hex << (last_nalu.offset + last_nalu.size) << std::dec << std::endl;
    }
    
    std::cout << "理论上重构文件应该与原始文件相同" << std::endl;
    
    return 0;
}
