// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <iostream>
#include <vector>
#include <memory>
#include <fstream>
#include "../../lib/custom_sei_codec/custom_sei_codec.h"
#include "../../lib/custom_sei_codec/custom_sei_data_define.h"

int main(int argc, char* argv[]) {
    std::cout << "=== 自定义SEI解码器演示 ===" << std::endl;

    if (argc != 2) {
        std::cout << "用法: " << argv[0] << " <输入H264文件>" << std::endl;
        std::cout << "可先运行 custom_sei_encode_demo 生成 custom_sei_encoded.h264" << std::endl;
        return 1;
    }
    std::string input_filename = argv[1];
    std::cout << "=== 读取编码文件 ===" << std::endl;
    std::cout << "输入文件: " << input_filename << std::endl;
    
    // 读取文件
    std::ifstream input_file(input_filename, std::ios::binary);
    if (!input_file.is_open()) {
        std::cout << "无法打开文件: " << input_filename << std::endl;
        return 1;
    }
    
    // 获取文件大小
    input_file.seekg(0, std::ios::end);
    size_t file_size = input_file.tellg();
    input_file.seekg(0, std::ios::beg);
    
    // 读取文件内容
    std::vector<uint8_t> encoded_frame(file_size);
    input_file.read(reinterpret_cast<char*>(encoded_frame.data()), file_size);
    input_file.close();
    
    std::cout << "文件大小: " << file_size << " 字节" << std::endl;
    std::cout << "文件内容 (十六进制):" << std::endl;
    for (size_t i = 0; i < encoded_frame.size(); i++) {
        printf("%02X ", encoded_frame[i]);
        if ((i + 1) % 16 == 0) std::cout << std::endl;
    }
    if (encoded_frame.size() % 16 != 0) std::cout << std::endl;
    std::cout << std::endl;
    
    // 2. 解码SEI数据
    std::cout << "=== 解码阶段 ===" << std::endl;
    CustomSEIHeader decoded_header;
    std::vector<std::shared_ptr<CustomSeiTLVDataBase>> decoded_sei_data;
    
    bool success = CustomSeiCodec::decode(encoded_frame, decoded_header, decoded_sei_data);
    
    if (success) {
        std::cout << "解码成功！" << std::endl;
        std::cout << "协议头部信息:" << std::endl;
        std::cout << "  版本: " << (int)decoded_header.version << std::endl;
        std::cout << "  保留字段: " << (int)decoded_header.reserved << std::endl;
        std::cout << "  数据大小: " << decoded_header.data_size << " 字节" << std::endl;
        std::cout << "  时间戳: " << decoded_header.timestamp << std::endl;
        
        std::cout << "解码出的SEI数据项数量: " << decoded_sei_data.size() << std::endl;
        
        for (size_t i = 0; i < decoded_sei_data.size(); i++) {
            const auto& item = decoded_sei_data[i];
            std::cout << "SEI数据项 " << (i + 1) << ":" << std::endl;
            std::cout << "  类型: 0x" << std::hex << (int)item->tlv_type << std::dec;
            
            // 显示类型名称
            std::string type_name;
            switch (item->tlv_type) {
                case CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA:
                    type_name = "自定义数据";
                    break;
                case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX:
                    type_name = "目标检测框";
                    break;
                case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON:
                    type_name = "多边形检测";
                    break;
                case CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY:
                    type_name = "文本叠加";
                    break;
                default:
                    type_name = "未知类型";
                    break;
            }
            std::cout << " (" << type_name << ")" << std::endl;
            
            std::cout << "  长度: " << item->tlv_length << " 字节" << std::endl;
            std::cout << "  原始数据: ";
            for (auto b : item->tlv_data) {
                printf("%02X ", b);
            }
            std::cout << std::endl;
            
            // 根据类型进行详细解析和显示
            switch (item->tlv_type) {
                case CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA: {
                    auto custom_data = std::dynamic_pointer_cast<CustomSeiCustomData>(item);
                    if (custom_data) {
                        std::cout << "  自定义数据内容: ";
                        for (auto b : custom_data->data) {
                            printf("%02X ", b);
                        }
                        std::cout << std::endl;
                    }
                    break;
                }
                
                case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX: {
                    auto box_data = std::dynamic_pointer_cast<CustomSeiObjectDetectionBox>(item);
                    if (box_data) {
                        std::cout << "  目标检测框详情:" << std::endl;
                        std::cout << "    ID: " << box_data->id << std::endl;
                        std::cout << "    类型: " << (int)box_data->type << std::endl;
                        std::cout << "    状态: " << (int)box_data->state << std::endl;
                        std::cout << "    位置: (" << box_data->x << ", " << box_data->y << ")" << std::endl;
                        std::cout << "    尺寸: " << box_data->width << " x " << box_data->height << std::endl;
                        std::cout << "    置信度: " << (int)box_data->confidence << "%" << std::endl;
                    }
                    break;
                }
                
                case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON: {
                    auto polygon_data = std::dynamic_pointer_cast<CustomSeiObjectDetectionPolygon>(item);
                    if (polygon_data) {
                        std::cout << "  多边形检测详情:" << std::endl;
                        std::cout << "    ID: " << polygon_data->id << std::endl;
                        std::cout << "    类型: " << (int)polygon_data->type << std::endl;
                        std::cout << "    状态: " << (int)polygon_data->state << std::endl;
                        std::cout << "    置信度: " << (int)polygon_data->confidence << "%" << std::endl;
                        std::cout << "    坐标点数量: " << polygon_data->point_count << std::endl;
                        std::cout << "    坐标点: ";
                        for (size_t j = 0; j < polygon_data->points.size(); j++) {
                            std::cout << "(" << polygon_data->points[j].x << ", " << polygon_data->points[j].y << ")";
                            if (j < polygon_data->points.size() - 1) std::cout << ", ";
                        }
                        std::cout << std::endl;
                    }
                    break;
                }
                
                case CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY: {
                    auto text_data = std::dynamic_pointer_cast<CustomSeiTextOverlay>(item);
                    if (text_data) {
                        std::cout << "  文本叠加详情:" << std::endl;
                        std::cout << "    ID: " << text_data->id << std::endl;
                        std::cout << "    位置: (" << text_data->x << ", " << text_data->y << ")" << std::endl;
                        std::cout << "    字体大小: " << (int)text_data->font_size << std::endl;
                        std::cout << "    颜色: RGB(" << (int)text_data->color_r << ", " << (int)text_data->color_g << ", " << (int)text_data->color_b << ")" << std::endl;
                        std::cout << "    透明度: " << (int)text_data->alpha << std::endl;
                        std::cout << "    文本内容: \"" << text_data->text << "\"" << std::endl;
                    }
                    break;
                }
                
                default:
                    std::cout << "  未知数据类型，无法解析详细信息" << std::endl;
                    break;
            }
            std::cout << std::endl;
        }
    } else {
        std::cout << "解码失败！" << std::endl;
    }
    
    return 0;
}
