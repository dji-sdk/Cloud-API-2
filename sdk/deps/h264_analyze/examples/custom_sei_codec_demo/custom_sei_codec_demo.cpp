// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <iostream>
#include <vector>
#include <memory>
#include <fstream>
#include <cassert>
#include "../../lib/custom_sei_codec/custom_sei_codec.h"
#include "../../lib/custom_sei_codec/custom_sei_data_define.h"

// 数据验证函数
bool compareCustomData(const std::shared_ptr<CustomSeiCustomData>& original, 
                      const std::shared_ptr<CustomSeiCustomData>& decoded) {
    if (!original || !decoded) return false;
    return original->data == decoded->data;
}

bool compareObjectDetectionBox(const std::shared_ptr<CustomSeiObjectDetectionBox>& original, 
                              const std::shared_ptr<CustomSeiObjectDetectionBox>& decoded) {
    if (!original || !decoded) return false;
    return original->id == decoded->id &&
           original->type == decoded->type &&
           original->state == decoded->state &&
           original->x == decoded->x &&
           original->y == decoded->y &&
           original->width == decoded->width &&
           original->height == decoded->height &&
           original->confidence == decoded->confidence;
}

bool compareObjectDetectionPolygon(const std::shared_ptr<CustomSeiObjectDetectionPolygon>& original, 
                                  const std::shared_ptr<CustomSeiObjectDetectionPolygon>& decoded) {
    if (!original || !decoded) return false;
    if (original->id != decoded->id ||
        original->type != decoded->type ||
        original->state != decoded->state ||
        original->confidence != decoded->confidence ||
        original->point_count != decoded->point_count ||
        original->points.size() != decoded->points.size()) {
        return false;
    }
    
    for (size_t i = 0; i < original->points.size(); i++) {
        if (original->points[i].x != decoded->points[i].x ||
            original->points[i].y != decoded->points[i].y) {
            return false;
        }
    }
    return true;
}

bool compareTextOverlay(const std::shared_ptr<CustomSeiTextOverlay>& original, 
                       const std::shared_ptr<CustomSeiTextOverlay>& decoded) {
    if (!original || !decoded) return false;
    return original->id == decoded->id &&
           original->x == decoded->x &&
           original->y == decoded->y &&
           original->font_size == decoded->font_size &&
           original->color_r == decoded->color_r &&
           original->color_g == decoded->color_g &&
           original->color_b == decoded->color_b &&
           original->alpha == decoded->alpha &&
           original->text == decoded->text;
}

bool compareHeaders(const CustomSEIHeader& original, const CustomSEIHeader& decoded) {
    return original.version == decoded.version &&
           original.reserved == decoded.reserved &&
           // 跳过 data_size 比较，因为它在编码过程中会被自动计算
           // original.data_size == decoded.data_size &&
           original.timestamp == decoded.timestamp;
}

void printValidationResults(const std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& original_data,
                           const std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& decoded_data,
                           const CustomSEIHeader& original_header,
                           const CustomSEIHeader& decoded_header) {
    std::cout << "\n=== 数据验证结果 ===" << std::endl;
    
    // 验证协议头部
    bool header_match = compareHeaders(original_header, decoded_header);
    std::cout << "协议头部验证: " << (header_match ? "✓ 通过" : "✗ 失败") << std::endl;
    if (!header_match) {
        std::cout << "  原始: version=" << (int)original_header.version 
                  << ", reserved=" << (int)original_header.reserved
                  << ", data_size=" << original_header.data_size
                  << ", timestamp=" << original_header.timestamp << std::endl;
        std::cout << "  解码: version=" << (int)decoded_header.version 
                  << ", reserved=" << (int)decoded_header.reserved
                  << ", data_size=" << decoded_header.data_size
                  << ", timestamp=" << decoded_header.timestamp << std::endl;
    }
    
    // 验证SEI数据项数量
    bool count_match = (original_data.size() == decoded_data.size());
    std::cout << "数据项数量验证: " << (count_match ? "✓ 通过" : "✗ 失败") 
              << " (原始: " << original_data.size() << ", 解码: " << decoded_data.size() << ")" << std::endl;
    
    if (!count_match) {
        std::cout << "数据项数量不匹配，无法进行详细验证" << std::endl;
        return;
    }
    
    // 验证每个数据项
    int passed_count = 0;
    for (size_t i = 0; i < original_data.size(); i++) {
        const auto& original_item = original_data[i];
        const auto& decoded_item = decoded_data[i];
        
        bool item_match = false;
        std::string item_type_name;
        
        switch (original_item->tlv_type) {
            case CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA: {
                auto orig_custom = std::dynamic_pointer_cast<CustomSeiCustomData>(original_item);
                auto dec_custom = std::dynamic_pointer_cast<CustomSeiCustomData>(decoded_item);
                item_match = compareCustomData(orig_custom, dec_custom);
                item_type_name = "自定义数据";
                break;
            }
            case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX: {
                auto orig_box = std::dynamic_pointer_cast<CustomSeiObjectDetectionBox>(original_item);
                auto dec_box = std::dynamic_pointer_cast<CustomSeiObjectDetectionBox>(decoded_item);
                item_match = compareObjectDetectionBox(orig_box, dec_box);
                item_type_name = "目标检测框";
                break;
            }
            case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON: {
                auto orig_polygon = std::dynamic_pointer_cast<CustomSeiObjectDetectionPolygon>(original_item);
                auto dec_polygon = std::dynamic_pointer_cast<CustomSeiObjectDetectionPolygon>(decoded_item);
                item_match = compareObjectDetectionPolygon(orig_polygon, dec_polygon);
                item_type_name = "多边形检测";
                break;
            }
            case CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY: {
                auto orig_text = std::dynamic_pointer_cast<CustomSeiTextOverlay>(original_item);
                auto dec_text = std::dynamic_pointer_cast<CustomSeiTextOverlay>(decoded_item);
                item_match = compareTextOverlay(orig_text, dec_text);
                item_type_name = "文本叠加";
                break;
            }
            default:
                item_match = false;
                item_type_name = "未知类型";
                break;
        }
        
        std::cout << "数据项 " << (i + 1) << " (" << item_type_name << "): " 
                  << (item_match ? "✓ 通过" : "✗ 失败") << std::endl;
        
        if (item_match) {
            passed_count++;
        } else {
            // 显示不匹配的详细信息
            std::cout << "  类型匹配: " << (original_item->tlv_type == decoded_item->tlv_type ? "是" : "否") << std::endl;
            std::cout << "  长度匹配: " << (original_item->tlv_length == decoded_item->tlv_length ? "是" : "否") << std::endl;
        }
    }
    
    // 总结
    std::cout << "\n验证总结:" << std::endl;
    std::cout << "  协议头部: " << (header_match ? "✓" : "✗") << std::endl;
    std::cout << "  数据项数量: " << (count_match ? "✓" : "✗") << std::endl;
    std::cout << "  数据项内容: " << passed_count << "/" << original_data.size() << " 通过" << std::endl;
    
    bool all_passed = header_match && count_match && (passed_count == original_data.size());
    std::cout << "  总体结果: " << (all_passed ? "✓ 全部验证通过" : "✗ 存在不匹配项") << std::endl;
}

int main(int argc, char* argv[]) {
    std::cout << "=== 自定义SEI编解码器完整演示 ===" << std::endl;
    const std::string work_file = (argc > 1) ? argv[1] : "custom_sei_encoded.h264";
    
    // ==================== 编码阶段 ====================
    std::cout << "\n=== 编码阶段 ===" << std::endl;
    
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
    
    // 保存原始数据用于后续验证
    CustomSEIHeader original_header = header;
    std::vector<std::shared_ptr<CustomSeiTLVDataBase>> original_sei_data;

    // 添加自定义数据（包含需要防竞争字节的序列）
    std::vector<uint8_t> custom_data = {0x01, 0x02, 0x03};
    // 预期结果: 00 00 03 01
    auto custom1 = std::make_shared<CustomSeiCustomData>(custom_data);
    sei_data.push_back(custom1);
    // 保存原始数据用于验证
    original_sei_data.push_back(std::make_shared<CustomSeiCustomData>(custom_data));

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
    // 保存原始数据用于验证
    original_sei_data.push_back(std::make_shared<CustomSeiObjectDetectionBox>(
        1, 1, 1, 1, 1, 1, 1, 1));
    
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
    // 保存原始数据用于验证
    original_sei_data.push_back(std::make_shared<CustomSeiObjectDetectionPolygon>(
        2, 2, 2, 2, 4, polygon_points));

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
    // 保存原始数据用于验证
    original_sei_data.push_back(std::make_shared<CustomSeiTextOverlay>(
        3, 3, 3, 3, 3, 3, 3, 3, "BBBBB"));
    
    // 4. 执行编码
    std::vector<uint8_t> output_frame;
    bool encode_success = CustomSeiCodec::encode(h264_frame, header, sei_data, output_frame);
    
    if (encode_success) {
        std::cout << "编码成功！" << std::endl;
        std::cout << "原始帧大小: " << h264_frame.size() << " 字节" << std::endl;
        std::cout << "输出帧大小: " << output_frame.size() << " 字节" << std::endl;
        std::cout << "SEI数据增加: " << (output_frame.size() - h264_frame.size()) << " 字节" << std::endl;
        std::cout << "sei_data size: " << sei_data.size() << " 字节" << std::endl;
        
        // 保存编码后的数据到文件
        std::string output_filename = work_file;
        std::ofstream output_file(output_filename, std::ios::binary);
        if (output_file.is_open()) {
            output_file.write(reinterpret_cast<const char*>(output_frame.data()), output_frame.size());
            output_file.close();
            std::cout << "\n编码数据已保存到文件: " << output_filename << std::endl;
        } else {
            std::cout << "无法创建输出文件: " << output_filename << std::endl;
            return 1;
        }
        
        // 打印十六进制数据（前64字节）
        std::cout << "\n输出帧前64字节 (十六进制):" << std::endl;
        size_t print_size = std::min(output_frame.size(), size_t(64));
        for (size_t i = 0; i < print_size; i++) {
            printf("%02X ", output_frame[i]);
            if ((i + 1) % 16 == 0) std::cout << std::endl;
        }
        std::cout << std::endl;
        
    } else {
        std::cout << "编码失败！" << std::endl;
        return 1;
    }
    
    // ==================== 解码阶段 ====================
    std::cout << "\n=== 解码阶段 ===" << std::endl;
    
    // 1. 读取之前编码的H.264文件
    std::string input_filename = work_file;
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
    CustomSEIHeader decoded_header;
    std::vector<std::shared_ptr<CustomSeiTLVDataBase>> decoded_sei_data;
    
    bool decode_success = CustomSeiCodec::decode(encoded_frame, decoded_header, decoded_sei_data);
    
    if (decode_success) {
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
        
        // 执行数据验证
        printValidationResults(original_sei_data, decoded_sei_data, original_header, decoded_header);
        
    } else {
        std::cout << "解码失败！" << std::endl;
        return 1;
    }
    
    std::cout << "\n=== 演示完成 ===" << std::endl;
    return 0;
}
