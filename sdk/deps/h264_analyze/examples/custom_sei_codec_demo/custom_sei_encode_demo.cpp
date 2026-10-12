// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <iostream>
#include <vector>
#include <memory>
#include <fstream>
#include "../../lib/custom_sei_codec/custom_sei_codec.h"

int main(int argc, char* argv[]) {
    std::cout << "=== 自定义SEI编解码器演示 ===" << std::endl;
    const std::string output_filename = (argc > 1) ? argv[1] : "custom_sei_encoded.h264";
    
    // 1. 创建示例H.264帧数据（简化版，仅包含起始码和NALU头部）
    std::vector<uint8_t> h264_frame = {
        0x00, 0x00, 0x00, 0x01,  // 起始码
        0x65, 0x88, 0x84, 0x00,  // NALU头部 (IDR帧)
        0x10, 0x00, 0x00, 0x03,  // 示例数据
        0x00, 0x00, 0x03, 0x00,
        0x00, 0x03, 0x00, 0x00
    };
    
    // 2. 创建协议头部
    CustomSEIHeader header;
    header.version = 1;
    header.reserved = 0;
    header.data_size = 0; // 将在encode中自动计算
    header.timestamp = 0; // 示例时间戳

    // 3. 创建目标检测数据
    std::vector<std::shared_ptr<CustomSeiTLVDataBase>> sei_data;


    // 添加自定义数据（包含需要防竞争字节的序列）
    std::vector<uint8_t> custom_data = {0x01, 0x02, 0x03};
    // 预期结果: 00 00 03 01
    auto custom1 = std::make_shared<CustomSeiCustomData>(custom_data);
    sei_data.push_back(custom1);

    // 添加目标检测框
    auto box1 = std::make_shared<CustomSeiObjectDetectionBox>(
        1,      // id
        1,      // type (人)
        1,      // state (跟踪中)
        1,      // x (归一化坐标)
        1,      // y
        1,      // width
        1,      // height
        1       // confidence
    );
    sei_data.push_back(box1);
    
    // 添加多边形检测框
    std::vector<Point2D> polygon_points = {
        {1, 2},  // 第一个点
        {3, 4},  // 第二个点
        {5, 6},  // 第三个点
        {7, 8}   // 第四个点
    };
    auto polygon1 = std::make_shared<CustomSeiObjectDetectionPolygon>(
        2,      // id
        2,      // type (车)
        2,      // state (跟踪中)
        2,      // confidence
        4,      // point_count (坐标点数量)
        polygon_points
    );
    sei_data.push_back(polygon1);

    // 添加文本叠加
    auto text1 = std::make_shared<CustomSeiTextOverlay>(
        3,      // id
        3,      // x
        3,      // y
        3,      // font_size
        3,      // color_r
        3,      // color_g
        3,      // color_b
        3,      // alpha
        "BBBBB" // text
    );
    sei_data.push_back(text1);
    
    // 4. 执行编码
    std::vector<uint8_t> output_frame;
    bool success = CustomSeiCodec::encode(h264_frame, header, sei_data, output_frame);
    
    if (success) {
        std::cout << "编码成功！" << std::endl;
        std::cout << "原始帧大小: " << h264_frame.size() << " 字节" << std::endl;
        std::cout << "输出帧大小: " << output_frame.size() << " 字节" << std::endl;
        std::cout << "SEI数据增加: " << (output_frame.size() - h264_frame.size()) << " 字节" << std::endl;
        std::cout << "sei_data size: " << sei_data.size() << " 字节" << std::endl;
        
        // 保存编码后的数据到文件
        std::ofstream output_file(output_filename, std::ios::binary);
        if (output_file.is_open()) {
            output_file.write(reinterpret_cast<const char*>(output_frame.data()), output_frame.size());
            output_file.close();
            std::cout << "\n编码数据已保存到文件: " << output_filename << std::endl;
        } else {
            std::cout << "无法创建输出文件: " << output_filename << std::endl;
        }
        
        // 打印十六进制数据
        size_t print_size = std::min(output_frame.size(), size_t(256));
        std::cout << "\n输出帧前" << print_size << "字节 (十六进制):" << std::endl;
        for (size_t i = 0; i < print_size; i++) {
            printf("%02X ", output_frame[i]);
            if ((i + 1) % 16 == 0) std::cout << std::endl;
        }
        std::cout << std::endl;
        
    } else {
        std::cout << "编码失败！" << std::endl;
        return 1;
    }
    
    return 0;
}
