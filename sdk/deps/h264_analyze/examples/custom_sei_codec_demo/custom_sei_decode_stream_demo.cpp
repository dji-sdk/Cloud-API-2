// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <iostream>
#include <vector>
#include <memory>
#include <fstream>
#include <map>
#include <iomanip>
#include "../../lib/h264_parser/h264_parser.h"
#include "../../lib/custom_sei_codec/custom_sei_codec.h"
#include "../../lib/custom_sei_codec/custom_sei_data_define.h"

static bool isCustomSeiNalu(const std::vector<uint8_t>& data) {
    size_t offset = 0;
    if (data.size() >= 4 && data[0] == 0x00 && data[1] == 0x00 &&
        data[2] == 0x00 && data[3] == 0x01) {
        offset = 4;
    } else if (data.size() >= 3 && data[0] == 0x00 && data[1] == 0x00 && data[2] == 0x01) {
        offset = 3;
    } else {
        return false;
    }
    if (offset >= data.size() || (data[offset] & 0x1F) != 0x06) {
        return false;
    }
    ++offset;
    return offset + 3 <= data.size() &&
           data[offset] == 0xFF && data[offset + 1] == 0xFF && data[offset + 2] == 0x02;
}

int main(int argc, char* argv[]) {
    std::cout << "=== 自定义SEI流解码演示 ===" << std::endl;

    if (argc != 2) {
        std::cout << "用法: " << argv[0] << " <输入H264文件>" << std::endl;
        std::cout << "样例: " << argv[0]
                  << " ../../video/dji_stream_drone_20250916_111046_with_custom_sei.h264" << std::endl;
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
    std::vector<uint8_t> file_data(file_size);
    input_file.read(reinterpret_cast<char*>(file_data.data()), file_size);
    input_file.close();
    
    std::cout << "文件大小: " << file_size << " 字节" << std::endl;
    
    // 2. 使用H264Parser解析文件
    H264Parser parser;
    std::vector<NALU> all_nalus = parser.splitNALUs(file_data);
    
    if (all_nalus.empty()) {
        std::cout << "错误：无法解析H264文件或文件为空" << std::endl;
        return 1;
    }
    
    std::cout << "成功解析出 " << all_nalus.size() << " 个NALU" << std::endl;
    
    // 调试：打印前几个NALU的详细信息
    std::cout << "\n=== 前10个NALU详细信息 ===" << std::endl;
    for (size_t i = 0; i < std::min(all_nalus.size(), (size_t)10); i++) {
        const NALU& nalu = all_nalus[i];
        std::cout << "NALU #" << (i+1) << " 偏移: 0x" << std::hex << nalu.offset 
                  << std::dec << " 大小: " << nalu.size << " 类型: " << (int)nalu.nalu_type << std::endl;
        
        // 打印NALU的前32字节数据
        std::cout << "  数据: ";
        for (size_t k = 0; k < std::min(nalu.data.size(), (size_t)32); k++) {
            printf("%02X ", nalu.data[k]);
        }
        std::cout << std::endl;
    }
    
    // 3. 统计NALU类型分布
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
    
    // 4. 查找并解码所有SEI NALU
    int sei_count = 0;
    int custom_sei_count = 0;
    int decoded_sei_count = 0;
    
    for (size_t i = 0; i < all_nalus.size(); ++i) {
        const NALU& nalu = all_nalus[i];
        
        // 检查是否为SEI NALU
        if (nalu.nalu_type == 0x06) {
            sei_count++;
            if (!isCustomSeiNalu(nalu.data)) {
                continue;
            }
            custom_sei_count++;
            std::cout << "\n=== 处理自定义SEI NALU #" << custom_sei_count << " ===" << std::endl;
            std::cout << "NALU大小: " << nalu.size << " 字节" << std::endl;
            std::cout << "NALU偏移: 0x" << std::hex << nalu.offset << std::dec << std::endl;
            
            // 调用封装的解码函数（不做手动解析）
            CustomSEIHeader decoded_header;
            std::vector<std::shared_ptr<CustomSeiTLVDataBase>> decoded_sei_data;
            bool success = CustomSeiCodec::decode(nalu.data, decoded_header, decoded_sei_data);
            
            if (success) {
                decoded_sei_count++;
                std::cout << "解码成功！" << std::endl;
                std::cout << "协议头部信息:" << std::endl;
                std::cout << "  版本: " << (int)decoded_header.version << std::endl;
                std::cout << "  保留字段: " << (int)decoded_header.reserved << std::endl;
                std::cout << "  数据大小: " << decoded_header.data_size << " 字节" << std::endl;
                std::cout << "  时间戳: " << decoded_header.timestamp << std::endl;
                
                std::cout << "解码出的SEI数据项数量: " << decoded_sei_data.size() << std::endl;
                
                for (size_t j = 0; j < decoded_sei_data.size(); j++) {
                    const auto& item = decoded_sei_data[j];
                    std::cout << "SEI数据项 " << (j + 1) << ":" << std::endl;
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
                                for (size_t k = 0; k < polygon_data->points.size(); k++) {
                                    std::cout << "(" << polygon_data->points[k].x << ", " << polygon_data->points[k].y << ")";
                                    if (k < polygon_data->points.size() - 1) std::cout << ", ";
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
                
                // 打印SEI NALU的十六进制数据用于调试
                std::cout << "SEI NALU数据 (前64字节):" << std::endl;
                size_t print_size = std::min(nalu.data.size(), size_t(64));
                for (size_t k = 0; k < print_size; k++) {
                    printf("%02X ", nalu.data[k]);
                    if ((k + 1) % 16 == 0) std::cout << std::endl;
                }
                if (nalu.data.size() % 16 != 0) std::cout << std::endl;
            }
        }
    }
    
    // 5. 输出统计信息
    std::cout << "\n=== 处理完成 ===" << std::endl;
    std::cout << "找到的SEI NALU数量: " << sei_count << std::endl;
    std::cout << "其中自定义SEI (payload type 0x200): " << custom_sei_count << std::endl;
    std::cout << "成功解码的自定义SEI数量: " << decoded_sei_count << std::endl;
    std::cout << "解码成功率: " << (custom_sei_count > 0 ? (100.0 * decoded_sei_count / custom_sei_count) : 0) << "%" << std::endl;
    
    return 0;
}
