// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "h264_parser.h"
#include <iostream>
#include <iomanip>
#include <fstream>

// 完全照抄FlightHubSDK的常量定义
#define SEI_NALU_TYPE 0x06
#define EXT_SEI_TYPE_1 0xf5  // DJI SEI type: 245
#define EXT_SEI_TYPE_2 0x66  // DJI SEI type: 102

// 完全照抄FlightHubSDK的SeiType枚举
enum class SeiType {
    ASSISTANT_SEI = 1,
    AR_SEI = 4,
    AI_SEI = 7,
    EXT_SEI = 9,
    FOV_SEI = 10,
    TIME_STAMP_SEI = 100,
    END_TO_END_DELAY_SEI = 155,
};

// 完全照抄FlightHubSDK的is_key_frame函数
bool H264Parser::is_key_frame(const uint8_t *data, const uint32_t& length) {
    for (int i = 0; i < length - 3; i++) {
        if (data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x01) {
            if ((data[i+3] & 0x1F) == 0x05) {
                return true;
            } else if ((data[i+3] & 0x1F) == 0x01) {
                return false;
            }
            continue;
        }
    }
    return false;
}

// 新增：获取帧类型
char H264Parser::getFrameType(uint8_t nalu_type, bool is_key_frame) {
    switch (nalu_type) {
        case 0x05: // IDR帧
            return 'I';
        case 0x01: // P帧
            return 'P';
        case 0x02: // B帧
            return 'B';
        default:
            return 'U'; // 未知类型
    }
}

// 完全照抄FlightHubSDK的find_next_nalu函数
bool find_next_nalu(const uint8_t *data, const uint32_t& length, uint32_t& nalu_offset) {
    // start code 00 00 01 or 00 00 00 01
    for (int i = 0; i < length - 4; i++) {
        if (data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x01) {
            nalu_offset = i + 3;
            return true;
        } else if (data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x00 && data[i+3] == 0x01) {
            nalu_offset = i + 4;
            return true;
        }
    }
    return false;
}

// 完全照抄FlightHubSDK的get_nalu_type函数
bool get_nalu_type(const uint8_t *data, const uint32_t& length, uint32_t& nalu_type) {
    if (length < 1) {
        return false;
    }
    nalu_type = data[0] & 0x1F; // 0x1F: 0001 1111, 取低5位
    return true;
}

// 修改后的get_sei_type函数，支持多字节SEI Payload Type
bool get_sei_type(const uint8_t *data, const uint32_t& length, uint32_t& sei_type) {
    if (length < 2) {
        return false;
    }
    
    // data[0] is nalu_type, data[1] is first byte of sei_type
    uint32_t offset = 1;
    sei_type = 0;
    
    // 按照H.264标准解析SEI Payload Type
    // 连续0xFF累加 + 最后一个<0xFF的值
    while (offset < length && data[offset] == 0xFF) {
        sei_type += 0xFF;
        offset++;
    }
    
    if (offset < length) {
        sei_type += data[offset];
        offset++;
    } else {
        return false; // 没有找到有效的SEI Payload Type结束字节
    }
    
    return true;
}

// 修改后的get_ext_sei_offset函数，支持多字节SEI Payload Type
bool get_ext_sei_offset(const uint8_t *data, const uint32_t& length, uint32_t& sei_offset) {
    do {
        // DJI 的SEI都在包头，所以只需要解析第一个
        uint32_t nalu_offset = 0;
        if (!find_next_nalu(data, length, nalu_offset)) {
            std::cout << "find_next_nalu failed" << std::endl;
            break;
        }
        if (nalu_offset >= length) {
            std::cout << "nalu_offset out of range" << std::endl;
            break;
        }
        uint32_t nalu_type = 0;
        if (!get_nalu_type(data + nalu_offset, length - nalu_offset, nalu_type)) {
            std::cout << "get_nalu_type failed" << std::endl;
            break;
        }
        if (nalu_type != SEI_NALU_TYPE) {
            break;
        }
        
        // 检查SEI Payload Type是否为我们的自定义类型 (0xFF 0xFF 0x02)
        if (nalu_offset + 3 < length && 
            data[nalu_offset + 1] == 0xFF && 
            data[nalu_offset + 2] == 0xFF && 
            data[nalu_offset + 3] == 0x02) {
            // 跳过nalu_type和3字节的SEI Payload Type
            sei_offset = nalu_offset + 4;
            return true;
        }
        
        // 检查传统的DJI SEI类型
        uint32_t sei_type = 0;
        if (get_sei_type(data + nalu_offset, length - nalu_offset, sei_type)) {
            if (sei_type == EXT_SEI_TYPE_1 || sei_type == EXT_SEI_TYPE_2) {
                // 计算SEI Payload Type的字节数
                uint32_t payload_type_bytes = 1;
                uint32_t offset = nalu_offset + 1;
                while (offset < length && data[offset] == 0xFF) {
                    payload_type_bytes++;
                    offset++;
                }
                if (offset < length) {
                    payload_type_bytes++;
                }
                
                // skip nalu_type and sei_type(payload type)
                sei_offset = nalu_offset + 1 + payload_type_bytes;
                return true;
            }
        }
    } while (false);
    return false;
}

// 完全照抄FlightHubSDK的remove_emulation_prevention_bytes函数
std::vector<uint8_t> remove_emulation_prevention_bytes(const uint8_t *data, const uint32_t& length) {
    // Annex B 规范: 若出现 0x00 0x00 0x03 字节序列，需要把 0x03 删除
    std::vector<uint8_t> result;
    for (int i = 0; i < length; i++) {
        if (data[i] == 3 && 
            i >= 2 &&
            data[i - 1] == 0 && 
            data[i - 2] == 0 && 
            i + 1 < length &&
            (data[i + 1] == 0 || data[i + 1] == 1 || data[i + 1] == 2 || data[i + 1] == 3)) {
            continue;
        }
        result.push_back(data[i]);
    }
    return result;
}

// 修改后的get_sei_buffer函数，支持多字节SEI Payload Type
bool get_sei_buffer(const uint8_t *data, const uint32_t& length, std::vector<uint8_t>& sei_buffer) {
    uint32_t sei_offset = 0;
    if (!get_ext_sei_offset(data, length, sei_offset)) {
        return false;
    }
    
    if (sei_offset >= length) {
        std::cout << "get_sei_buffer: sei_offset out of range" << std::endl;
        return false;
    }
    
    uint32_t sei_raw_data_length = data[sei_offset];
    // 连续 0xFF 累加 + 最后一个 < 0xFF 的值，表示 sei_len， 符合 H264 规范
    // 0xFF 0xFF 0x03 -> sei_len = 255 + 255 + 3 = 513
    while (data[sei_offset] == 0xFF) {
        sei_offset++;
        if (sei_offset >= length) {
            std::cout << "get_sei_buffer: incomplete length encoding" << std::endl;
            return false;
        }
        sei_raw_data_length += data[sei_offset];
    }
    if (sei_raw_data_length == 0) {
        std::cout << "get_sei_buffer_length failed, sei_offset: " << sei_offset << ", length: " << length << std::endl;
        return false;
    }
    sei_offset++;
    
    if (sei_offset + sei_raw_data_length > length) {
        std::cout << "get_sei_buffer: insufficient data for payload" << std::endl;
        return false;
    }
    
    sei_buffer = remove_emulation_prevention_bytes(data + sei_offset, sei_raw_data_length);
    return true;
}

// 完全照抄FlightHubSDK的processSei函数
void processSei(const std::vector<uint8_t>& data, SEIFrame& sei_frame) {
    int length = data.size();
    for (int i = 0; i < length;) {
        // 检查是否有足够的数据解析子载荷头部 (2字节类型 + 2字节长度)
        if (i + 3 >= length) {
            std::cout << "processSei - insufficient data for payload header" << std::endl;
            break;
        }
        // 解析子载荷类型 (2字节，小端序)
        uint16_t payload_type = (static_cast<uint8_t>(data[i + 1]) << 8) | static_cast<uint8_t>(data[i]);
        // 解析子载荷长度 (2字节，小端序)
        uint16_t payload_length = static_cast<uint16_t>(static_cast<uint8_t>(data[i + 3]) << 8) | (static_cast<uint8_t>(data[i + 2]));
        uint32_t data_start_index = i + 4;
        uint32_t data_end_index = data_start_index + payload_length;
        if (data_end_index > length) {
            std::cout << "processSei - insufficient data for payload_length, payload_type: " << payload_type 
                      << ", data_end_index: " << data_end_index << ", length: " << length 
                      << ", payload_length: " << payload_length << std::endl;
            break;
        }
        
        // 提取子载荷数据
        std::vector<uint8_t> sub_payload_data(data.begin() + data_start_index, 
                                             data.begin() + data_end_index);
        
        // 创建子载荷对象
        SEISubPayload sub_payload;
        sub_payload.payload_type = payload_type;
        sub_payload.payload_length = payload_length;
        sub_payload.payload_data = sub_payload_data;
        
        // 添加到SEI帧中
        sei_frame.sub_payloads.push_back(sub_payload);
        
        // 移动到下一个子载荷
        i = data_end_index;
    }
}

H264Parser::H264Parser() {
}

H264Parser::~H264Parser() {
}

// 新增：解析视频帧大小
void H264Parser::parseVideoFrames(const std::vector<uint8_t>& file_data, size_t file_size) {
    uint32_t offset = 0;
    
    while (offset < file_size) {
        // 查找下一个NALU
        uint32_t nalu_offset = 0;
        if (!find_next_nalu(file_data.data() + offset, file_size - offset, nalu_offset)) {
            break;
        }
        
        uint32_t current_nalu_offset = offset + nalu_offset;
        if (current_nalu_offset >= file_size) {
            break;
        }
        
        // 获取NALU类型
        uint32_t nalu_type = 0;
        if (!get_nalu_type(file_data.data() + current_nalu_offset, file_size - current_nalu_offset, nalu_type)) {
            offset = current_nalu_offset + 1;
            continue;
        }
        
        // 检查是否是视频帧的NALU (非SEI、SPS、PPS等)
        if (nalu_type >= 0x01 && nalu_type <= 0x05) {
            bool is_key = is_key_frame(file_data.data() + current_nalu_offset, file_size - current_nalu_offset);
            char frame_type = getFrameType(nalu_type, is_key);
            
            // 只处理I帧和P帧，B帧暂时跳过
            if (frame_type == 'I' || frame_type == 'P') {
                VideoFrame frame;
                frame.offset = current_nalu_offset - 4; // 包含起始码
                frame.frame_type = frame_type;
                frame.is_key_frame = is_key;
                frame.nalu_count = 1;
                frame.size = 0;
                
                // 计算当前NALU的大小
                uint32_t nalu_size = 0;
                uint32_t next_nalu_offset = 0;
                
                // 查找下一个NALU来确定当前NALU的大小
                if (find_next_nalu(file_data.data() + current_nalu_offset + 1, file_size - current_nalu_offset - 1, next_nalu_offset)) {
                    nalu_size = next_nalu_offset + 1; // +1 包含起始码
                } else {
                    // 如果是最后一个NALU，计算到文件末尾
                    nalu_size = file_size - current_nalu_offset;
                }
                
                frame.size = nalu_size;
                
                // 添加到结果中
                video_frames_.push_back(frame);
            }
        }
        
        // 移动到下一个NALU
        offset = current_nalu_offset + 1;
    }
}

bool H264Parser::parseH264Stream(const std::string& filename) {
    std::ifstream file(filename, std::ios::binary);
    if (!file.is_open()) {
        std::cout << "无法打开文件: " << filename << std::endl;
        return false;
    }
    
    // 读取整个文件
    file.seekg(0, std::ios::end);
    size_t file_size = file.tellg();
    file.seekg(0, std::ios::beg);
    
    std::vector<uint8_t> file_data(file_size);
    file.read(reinterpret_cast<char*>(file_data.data()), file_size);
    file.close();
    
    std::cout << "文件大小: " << file_size << " 字节" << std::endl;
    
    // 清空之前的结果
    sei_frames_.clear();
    video_frames_.clear();
    
    // 解析视频帧大小
    parseVideoFrames(file_data, file_size);
    
    // 使用FlightHubSDK的方法解析SEI
    uint32_t offset = 0;
    int sei_count = 0;
    int dji_sei_count = 0;
    
    while (offset < file_size) {
        // 查找下一个NALU
        uint32_t nalu_offset = 0;
        if (!find_next_nalu(file_data.data() + offset, file_size - offset, nalu_offset)) {
            break;
        }
        
        uint32_t current_nalu_offset = offset + nalu_offset;
        if (current_nalu_offset >= file_size) {
            break;
        }
        
        // 获取NALU类型
        uint32_t nalu_type = 0;
        if (!get_nalu_type(file_data.data() + current_nalu_offset, file_size - current_nalu_offset, nalu_type)) {
            offset = current_nalu_offset + 1;
            continue;
        }
        
        // 检查是否是SEI NALU
        if (nalu_type == SEI_NALU_TYPE) {
            sei_count++;
            
            // 获取SEI类型
            uint32_t sei_type = 0;
            if (get_sei_type(file_data.data() + current_nalu_offset, file_size - current_nalu_offset, sei_type)) {
                if (sei_type == EXT_SEI_TYPE_1 || sei_type == EXT_SEI_TYPE_2) {
                    dji_sei_count++;
                    
                    // 解析SEI载荷大小
                    uint32_t sei_payload_offset = current_nalu_offset + 2;
                    if (sei_payload_offset >= file_size) {
                        offset = current_nalu_offset + 1;
                        continue;
                    }
                    
                    uint32_t sei_payload_size = file_data[sei_payload_offset];
                    // 连续 0xFF 累加 + 最后一个 < 0xFF 的值，表示 sei_len， 符合 H264 规范
                    while (file_data[sei_payload_offset] == 0xFF) {
                        sei_payload_offset++;
                        sei_payload_size += file_data[sei_payload_offset];
                    }
                    sei_payload_offset++;
                    
                    // 检查载荷大小是否有效
                    if (sei_payload_offset + sei_payload_size > file_size) {
                        offset = current_nalu_offset + 1;
                        continue;
                    }
                    
                    // 获取SEI数据
                    std::vector<uint8_t> sei_data(file_data.begin() + sei_payload_offset, 
                                                 file_data.begin() + sei_payload_offset + sei_payload_size);
                    
                    // 去除防竞争字节
                    std::vector<uint8_t> clean_sei_data = remove_emulation_prevention_bytes(sei_data.data(), sei_payload_size);
                    
                    // 创建SEI帧对象
                    SEIFrame sei_frame;
                    sei_frame.offset = current_nalu_offset - 4; // 包含起始码
                    sei_frame.size = sei_payload_offset + sei_payload_size - (current_nalu_offset - 4);
                    sei_frame.sei_payload_type = sei_type;
                    
                    // 使用FlightHubSDK的processSei函数解析子载荷
                    processSei(clean_sei_data, sei_frame);
                    
                    // 添加到结果中
                    sei_frames_.push_back(sei_frame);
                }
            }
        }
        
        // 移动到下一个NALU
        offset = current_nalu_offset + 1;
    }
    
    std::cout << "找到 " << sei_count << " 个SEI帧，其中 " << dji_sei_count << " 个DJI SEI帧" << std::endl;
    
    return true;
}

const std::vector<SEIFrame>& H264Parser::getSEIFrames() const {
    return sei_frames_;
}

const std::vector<VideoFrame>& H264Parser::getVideoFrames() const {
    return video_frames_;
}

void H264Parser::printSEIFrames() const {
    std::cout << "\n=== SEI帧信息 ===" << std::endl;
    std::cout << "总共找到 " << sei_frames_.size() << " 个SEI帧" << std::endl;
    
    for (size_t i = 0; i < sei_frames_.size(); ++i) {
        const SEIFrame& frame = sei_frames_[i];
        std::cout << "\nSEI帧 #" << (i + 1) << ":" << std::endl;
        std::cout << "  偏移位置: 0x" << std::hex << std::setfill('0') << std::setw(8) << frame.offset << std::dec << std::endl;
        std::cout << "  帧大小: " << frame.size << " 字节" << std::endl;
        std::cout << "  H.264 SEI载荷类型: " << (int)frame.sei_payload_type << " (0x" << std::hex << (int)frame.sei_payload_type << ")" << std::dec << std::endl;
        std::cout << "  子载荷数量: " << frame.sub_payloads.size() << std::endl;
        
        for (size_t j = 0; j < frame.sub_payloads.size(); ++j) {
            const SEISubPayload& sub_payload = frame.sub_payloads[j];
            std::cout << "    子载荷 #" << (j + 1) << ":" << std::endl;
            std::cout << "      类型: " << (int)sub_payload.payload_type << " (0x" << std::hex << (int)sub_payload.payload_type << ")" << std::dec << std::endl;
            std::cout << "      长度: " << sub_payload.payload_length << " 字节" << std::endl;
            std::cout << "      实际数据大小: " << sub_payload.payload_data.size() << " 字节" << std::endl;
            
            // 打印完整数据
            std::cout << "      数据 (完整): ";
            for (size_t k = 0; k < sub_payload.payload_data.size(); ++k) {
                std::cout << std::hex << std::setfill('0') << std::setw(2) << (int)sub_payload.payload_data[k] << " ";
            }
            std::cout << std::dec << std::endl;
        }
    }
}

void H264Parser::printVideoFrames() const {
    std::cout << "\n=== 视频帧信息 ===" << std::endl;
    std::cout << "总共找到 " << video_frames_.size() << " 个视频帧" << std::endl;
    
    int i_frame_count = 0;
    int p_frame_count = 0;
    int b_frame_count = 0;
    uint32_t total_i_size = 0;
    uint32_t total_p_size = 0;
    uint32_t total_b_size = 0;
    
    for (size_t i = 0; i < video_frames_.size(); ++i) {
        const VideoFrame& frame = video_frames_[i];
        std::cout << "\n视频帧 #" << (i + 1) << ":" << std::endl;
        std::cout << "  偏移位置: 0x" << std::hex << std::setfill('0') << std::setw(8) << frame.offset << std::dec << std::endl;
        std::cout << "  帧大小: " << frame.size << " 字节" << std::endl;
        std::cout << "  帧类型: " << frame.frame_type << " 帧" << std::endl;
        std::cout << "  是否关键帧: " << (frame.is_key_frame ? "是" : "否") << std::endl;
        std::cout << "  NALU数量: " << frame.nalu_count << std::endl;
        
        // 统计信息
        switch (frame.frame_type) {
            case 'I':
                i_frame_count++;
                total_i_size += frame.size;
                break;
            case 'P':
                p_frame_count++;
                total_p_size += frame.size;
                break;
            case 'B':
                b_frame_count++;
                total_b_size += frame.size;
                break;
        }
    }
    
    // 打印统计信息
    std::cout << "\n=== 帧统计信息 ===" << std::endl;
    std::cout << "I帧数量: " << i_frame_count << " 个，总大小: " << total_i_size << " 字节" << std::endl;
    std::cout << "P帧数量: " << p_frame_count << " 个，总大小: " << total_p_size << " 字节" << std::endl;
    std::cout << "B帧数量: " << b_frame_count << " 个，总大小: " << total_b_size << " 字节" << std::endl;
    
    if (i_frame_count > 0) {
        std::cout << "I帧平均大小: " << (total_i_size / i_frame_count) << " 字节" << std::endl;
    }
    if (p_frame_count > 0) {
        std::cout << "P帧平均大小: " << (total_p_size / p_frame_count) << " 字节" << std::endl;
    }
    if (b_frame_count > 0) {
        std::cout << "B帧平均大小: " << (total_b_size / b_frame_count) << " 字节" << std::endl;
    }
}

bool H264Parser::removeSEIAndSave(const std::string& input_filename, const std::string& output_filename) {
    std::ifstream input_file(input_filename, std::ios::binary);
    if (!input_file.is_open()) {
        std::cout << "无法打开输入文件: " << input_filename << std::endl;
        return false;
    }
    
    // 读取整个文件
    input_file.seekg(0, std::ios::end);
    size_t file_size = input_file.tellg();
    input_file.seekg(0, std::ios::beg);
    
    std::vector<uint8_t> file_data(file_size);
    input_file.read(reinterpret_cast<char*>(file_data.data()), file_size);
    input_file.close();
    
    std::cout << "输入文件大小: " << file_size << " 字节" << std::endl;
    
    // 创建输出文件
    std::ofstream output_file(output_filename, std::ios::binary);
    if (!output_file.is_open()) {
        std::cout << "无法创建输出文件: " << output_filename << std::endl;
        return false;
    }
    
    uint32_t offset = 0;
    uint32_t sei_removed_count = 0;
    uint32_t total_removed_size = 0;
    
    while (offset < file_size) {
        // 查找下一个NALU起始码
        uint32_t nalu_offset = 0;
        if (!find_next_nalu(file_data.data() + offset, file_size - offset, nalu_offset)) {
            // 没有找到更多NALU，写入剩余数据
            output_file.write(reinterpret_cast<const char*>(file_data.data() + offset), file_size - offset);
            break;
        }
        
        uint32_t nalu_data_start = offset + nalu_offset; // NALU数据的开始位置（跳过起始码）
        if (nalu_data_start >= file_size) {
            // 写入剩余数据
            output_file.write(reinterpret_cast<const char*>(file_data.data() + offset), file_size - offset);
            break;
        }
        
        // 计算NALU起始码的位置
        uint32_t nalu_start = nalu_data_start - 4; // 起始码 0x00000001 的位置
        
        // 获取NALU类型
        uint32_t nalu_type = 0;
        if (!get_nalu_type(file_data.data() + nalu_data_start, file_size - nalu_data_start, nalu_type)) {
            // 无法识别NALU类型，写入从offset到nalu_start的数据
            output_file.write(reinterpret_cast<const char*>(file_data.data() + offset), nalu_start - offset);
            offset = nalu_data_start + 1;
            continue;
        }
        
        // 查找下一个NALU来确定当前NALU的结束位置
        uint32_t next_nalu_offset = 0;
        uint32_t nalu_end = file_size; // 默认到文件末尾
        
        if (find_next_nalu(file_data.data() + nalu_data_start, file_size - nalu_data_start, next_nalu_offset)) {
            nalu_end = nalu_data_start + next_nalu_offset - 4; // 下一个NALU起始码的位置
        }
        
        // 检查是否是SEI NALU
        if (nalu_type == SEI_NALU_TYPE) {
            sei_removed_count++;
            uint32_t sei_size = nalu_end - nalu_start;
            total_removed_size += sei_size;
            
            std::cout << "移除SEI帧 #" << sei_removed_count 
                      << " 偏移: 0x" << std::hex << nalu_start << std::dec
                      << " 大小: " << sei_size << " 字节" << std::endl;
            
            // 跳过这个SEI NALU，不写入输出文件
            offset = nalu_end;
        } else {
            // 不是SEI NALU，写入从offset到nalu_end的所有数据（包含当前NALU）
            output_file.write(reinterpret_cast<const char*>(file_data.data() + offset), nalu_end - offset);
            offset = nalu_end;
        }
    }
    
    output_file.close();
    
    std::cout << "\n=== SEI移除完成 ===" << std::endl;
    std::cout << "移除的SEI帧数量: " << sei_removed_count << std::endl;
    std::cout << "移除的总大小: " << total_removed_size << " 字节" << std::endl;
    std::cout << "输出文件: " << output_filename << std::endl;
    
    return true;
}

std::vector<NALU> H264Parser::splitNALUs(const std::vector<uint8_t>& h264_data) {
    std::vector<NALU> nalus;
    uint32_t offset = 0;
    size_t data_size = h264_data.size();
    
    while (offset < data_size) {
        // 查找下一个NALU
        uint32_t nalu_offset = 0;
        if (!find_next_nalu(h264_data.data() + offset, data_size - offset, nalu_offset)) {
            break;
        }
        
        uint32_t current_nalu_offset = offset + nalu_offset;
        if (current_nalu_offset >= data_size) {
            break;
        }
        
        // 获取NALU类型
        uint32_t nalu_type = 0;
        if (!get_nalu_type(h264_data.data() + current_nalu_offset, data_size - current_nalu_offset, nalu_type)) {
            offset = current_nalu_offset + 1;
            continue;
        }
        
        // 查找下一个NALU来确定当前NALU的大小
        uint32_t next_nalu_offset = 0;
        uint32_t nalu_end = data_size; // 默认到数据末尾
        
        // 特殊处理SEI NALU：根据SEI Payload Length来确定大小
        if (nalu_type == SEI_NALU_TYPE) {
            uint32_t sei_payload_offset = current_nalu_offset + 1; // 跳过NALU头部
            
            // 解析SEI Payload Type（可能是多字节）
            while (sei_payload_offset < data_size && h264_data[sei_payload_offset] == 0xFF) {
                sei_payload_offset++;
            }
            if (sei_payload_offset < data_size) {
                sei_payload_offset++; // 跳过最后一个Payload Type字节
            }
            
            // 解析SEI Payload Length
            if (sei_payload_offset < data_size) {
                uint32_t payload_length = 0;
                uint32_t length_offset = sei_payload_offset;
                
                while (length_offset < data_size && h264_data[length_offset] == 0xFF) {
                    payload_length += 0xFF;
                    length_offset++;
                }
                if (length_offset < data_size) {
                    payload_length += h264_data[length_offset];
                    length_offset++;
                    
                    // SEI NALU结束位置 = 长度字段后的位置 + payload数据长度 + rbsp_trailing_bits(1字节)
                    // 重要：这里以 buffer 起始为坐标，current_nalu_offset 指向 NAL 头（非起始码），
                    // 不要进行 "-4" 回跳到起始码。起始码(3/4字节)的处理在下方通过计算 start_code_length
                    // 并设置 nalu.offset/nalu.size 时统一处理，避免 SEI NALU 被错误截断。
                    nalu_end = length_offset + payload_length + 1;
                    
                    // 确保不超过数据范围
                    if (nalu_end > data_size) {
                        nalu_end = data_size;
                    }
                }
            }
        } else if (find_next_nalu(h264_data.data() + current_nalu_offset + 1, data_size - current_nalu_offset - 1, next_nalu_offset)) {
            nalu_end = current_nalu_offset + 1 + next_nalu_offset - 4; // 下一个NALU起始码的位置
        }
        // 如果没有找到下一个NALU，说明这是最后一个NALU，包含到文件末尾的所有数据
        
        // 确保NALU大小不会超出数据范围
        if (nalu_end > data_size) {
            nalu_end = data_size;
        }
        
        // 确保NALU结束位置不会小于开始位置
        if (nalu_end <= current_nalu_offset - 4) {
            nalu_end = data_size;
        }
        
        // 创建NALU对象
        NALU nalu;
        
        // 确定起始码的长度
        uint32_t start_code_length = 4; // 默认4字节起始码
        if (current_nalu_offset >= 4 && 
            h264_data[current_nalu_offset - 4] == 0x00 && 
            h264_data[current_nalu_offset - 3] == 0x00 && 
            h264_data[current_nalu_offset - 2] == 0x00 && 
            h264_data[current_nalu_offset - 1] == 0x01) {
            start_code_length = 4;
        } else if (current_nalu_offset >= 3 && 
                   h264_data[current_nalu_offset - 3] == 0x00 && 
                   h264_data[current_nalu_offset - 2] == 0x00 && 
                   h264_data[current_nalu_offset - 1] == 0x01) {
            start_code_length = 3;
        }
        
        nalu.offset = current_nalu_offset - start_code_length; // 包含起始码
        nalu.size = nalu_end - nalu.offset;
        nalu.nalu_type = nalu_type;
        
        // 复制NALU数据（包含起始码）
        nalu.data.assign(h264_data.begin() + nalu.offset, h264_data.begin() + nalu.offset + nalu.size);
        
        // 添加到结果中
        nalus.push_back(nalu);
        
        // 调试信息：检查NALU边界
        if (nalus.size() <= 3 || nalus.size() >= 1300) {
            std::cout << "NALU #" << nalus.size() << " 偏移: 0x" << std::hex << nalu.offset 
                      << " 大小: " << std::dec << nalu.size 
                      << " 类型: " << (int)nalu.nalu_type 
                      << " 结束: 0x" << std::hex << (nalu.offset + nalu.size) << std::dec << std::endl;
        }
        
        // 移动到下一个NALU
        offset = current_nalu_offset + 1;
    }
    
    return nalus;
}

std::vector<NALU> H264Parser::splitNALUsFromFile(const std::string& filename) {
    std::ifstream file(filename, std::ios::binary);
    if (!file.is_open()) {
        std::cout << "无法打开文件: " << filename << std::endl;
        return std::vector<NALU>();
    }
    
    // 读取整个文件
    file.seekg(0, std::ios::end);
    size_t file_size = file.tellg();
    file.seekg(0, std::ios::beg);
    
    std::vector<uint8_t> file_data(file_size);
    file.read(reinterpret_cast<char*>(file_data.data()), file_size);
    file.close();
    
    std::cout << "文件大小: " << file_size << " 字节" << std::endl;
    
    return splitNALUs(file_data);
}