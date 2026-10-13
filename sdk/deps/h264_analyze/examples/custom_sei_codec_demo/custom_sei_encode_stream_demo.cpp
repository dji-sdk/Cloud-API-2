// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <memory>
#include "../../lib/h264_parser/h264_parser.h"
#include "../../lib/custom_sei_codec/custom_sei_codec.h"
#include "../../lib/custom_sei_codec/custom_sei_data_define.h"

int main(int argc, char* argv[]) {
    std::cout << "=== 自定义SEI流编码演示 ===" << std::endl;

    if (argc != 3) {
        std::cout << "用法: " << argv[0] << " <输入H264文件> <输出H264文件>" << std::endl;
        std::cout << "样例: " << argv[0]
                  << " ../../video/dji_stream_drone_20250916_111046.h264 with_custom_sei.h264" << std::endl;
        return 1;
    }
    std::string input_file = argv[1];
    std::string output_file = argv[2];
    
    std::cout << "输入文件: " << input_file << std::endl;
    std::cout << "输出文件: " << output_file << std::endl;
    
    // 2. 创建H264Parser实例并解析文件
    H264Parser parser;
    std::vector<NALU> all_nalus = parser.splitNALUsFromFile(input_file);
    
    if (all_nalus.empty()) {
        std::cout << "错误：无法解析H264文件或文件为空" << std::endl;
        return 1;
    }
    
    std::cout << "成功解析出 " << all_nalus.size() << " 个NALU" << std::endl;
    
    // 3. 统计I帧和P帧数量
    int i_frame_count = 0;
    int p_frame_count = 0;
    
    for (const auto& nalu : all_nalus) {
        if (nalu.nalu_type >= 0x01 && nalu.nalu_type <= 0x05) {
            bool is_key = H264Parser::is_key_frame(nalu.data.data() + 4, nalu.data.size() - 4);
            char frame_type = H264Parser::getFrameType(nalu.nalu_type, is_key);
            
            if (frame_type == 'I') {
                i_frame_count++;
            } else if (frame_type == 'P') {
                p_frame_count++;
            }
        }
    }
    
    std::cout << "I帧数量: " << i_frame_count << " 个" << std::endl;
    std::cout << "P帧数量: " << p_frame_count << " 个" << std::endl;
    
    // 4. 创建输出文件
    std::ofstream output_stream(output_file, std::ios::binary);
    if (!output_stream.is_open()) {
        std::cout << "错误：无法创建输出文件: " << output_file << std::endl;
        return 1;
    }
    
    // 5. 处理每个NALU
    int processed_i_frames = 0;
    int processed_p_frames = 0;
    for (size_t i = 0; i < all_nalus.size(); ++i) {
        const NALU& nalu = all_nalus[i];
        
        // 检查是否为视频帧（I帧或P帧）
        bool is_video_frame = false;
        char frame_type = 'U'; // Unknown
        
        if (nalu.nalu_type >= 0x01 && nalu.nalu_type <= 0x05) {
            bool is_key = H264Parser::is_key_frame(nalu.data.data() + 4, nalu.data.size() - 4);
            frame_type = H264Parser::getFrameType(nalu.nalu_type, is_key);
            is_video_frame = (frame_type == 'I' || frame_type == 'P');
        }
        
        // 如果是I帧或P帧，添加SEI信息
        if (is_video_frame) {
            std::cout << "处理 " << frame_type << " 帧 #" << (frame_type == 'I' ? ++processed_i_frames : ++processed_p_frames) << std::endl;
            
            // 创建SEI头部
            CustomSEIHeader sei_header;
            sei_header.version = 1;
            sei_header.reserved = 0;
            // 与 custom_sei_encode_demo.cpp 保持一致，使用固定时间戳 0
            sei_header.timestamp = 0;
            
            // 创建SEI数据（与 custom_sei_encode_demo.cpp 保持一致的简化固定示例）
            std::vector<std::shared_ptr<CustomSeiTLVDataBase>> sei_data;

            // 1) 自定义数据 {0x01,0x02,0x03}
            std::vector<uint8_t> custom_data = {0x01, 0x02, 0x03};
            auto custom_sei = std::make_shared<CustomSeiCustomData>(custom_data);
            sei_data.push_back(custom_sei);

            // 2) 目标检测框：id=1,type=1,state=1,x=1,y=1,width=1,height=1,confidence=1
            auto detection_box = std::make_shared<CustomSeiObjectDetectionBox>(
                1,  // id
                1,  // type
                1,  // state
                1,  // x
                1,  // y
                1,  // width
                1,  // height
                1   // confidence
            );
            sei_data.push_back(detection_box);

            // 3) 多边形检测：id=2,type=2,state=2,confidence=2, 4个点 {(1,2),(3,4),(5,6),(7,8)}
            std::vector<Point2D> polygon_points = { {1,2}, {3,4}, {5,6}, {7,8} };
            auto polygon1 = std::make_shared<CustomSeiObjectDetectionPolygon>(
                2,  // id
                2,  // type
                2,  // state
                2,  // confidence
                4,  // point_count
                polygon_points
            );
            sei_data.push_back(polygon1);

            // 4) 文本叠加：id=3,x=3,y=3,font=3,color(3,3,3),alpha=3,text="BBBBB"
            auto text_overlay = std::make_shared<CustomSeiTextOverlay>(
                3,      // id
                3,      // x
                3,      // y
                3,      // font_size
                3,      // color_r
                3,      // color_g
                3,      // color_b
                3,      // alpha
                std::string("BBBBB")
            );
            sei_data.push_back(text_overlay);
            
            // 使用CustomSeiCodec进行编码，它会自动处理SEI NALU的插入
            std::vector<uint8_t> output_frame;
            bool encode_success = CustomSeiCodec::encode(nalu.data, sei_header, sei_data, output_frame);
            
            if (encode_success) {
                // 写入包含SEI的完整帧数据
                output_stream.write(reinterpret_cast<const char*>(output_frame.data()), output_frame.size());
                
                std::cout << "  添加SEI信息，帧大小: " << output_frame.size() << " 字节" << std::endl;
                
                // 调试：打印前32字节的十六进制数据
                std::cout << "  帧数据前32字节: ";
                for (size_t k = 0; k < std::min(output_frame.size(), (size_t)32); k++) {
                    printf("%02X ", output_frame[k]);
                }
                std::cout << std::endl;
            } else {
                std::cout << "  错误：SEI编码失败，写入原始NALU" << std::endl;
                // 如果编码失败，至少写入原始NALU
                output_stream.write(reinterpret_cast<const char*>(nalu.data.data()), nalu.data.size());
            }
        } else {
            // 非视频帧，直接写入原始NALU
            output_stream.write(reinterpret_cast<const char*>(nalu.data.data()), nalu.data.size());
        }
    }
    
    output_stream.close();
    
    // 6. 输出统计信息
    std::cout << "\n=== 处理完成 ===" << std::endl;
    std::cout << "处理的I帧数量: " << processed_i_frames << std::endl;
    std::cout << "处理的P帧数量: " << processed_p_frames << std::endl;
    std::cout << "输出文件已保存: " << output_file << std::endl;
    
    // 7. 验证输出文件
    std::ifstream output_check(output_file, std::ios::binary);
    if (output_check.is_open()) {
        output_check.seekg(0, std::ios::end);
        size_t output_size = output_check.tellg();
        output_check.close();
        
        std::cout << "输出文件大小: " << output_size << " 字节" << std::endl;
    }
    
    std::cout << "自定义SEI流编码演示完成！" << std::endl;
    
    return 0;
}
