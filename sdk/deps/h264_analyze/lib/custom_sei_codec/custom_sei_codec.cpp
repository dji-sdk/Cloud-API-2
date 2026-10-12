// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "custom_sei_codec.h"
#include "custom_sei_data_define.h"
#include <cstdio>
#include <memory>

// H.264 NALU类型定义
#define SEI_NALU_TYPE 0x06
#define CUSTOM_SEI_PAYLOAD_TYPE 0x200  // 0xff + 0xff + 0x02

static bool matchNaluStartCode(const std::vector<uint8_t>& data, size_t i, size_t& start_code_len) {
    if (i + 3 < data.size() &&
        data[i] == 0x00 && data[i + 1] == 0x00 &&
        data[i + 2] == 0x00 && data[i + 3] == 0x01) {
        start_code_len = 4;
        return true;
    }
    if (i + 2 < data.size() &&
        data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x01) {
        start_code_len = 3;
        return true;
    }
    return false;
}

static bool isCustomSeiPayloadType(const std::vector<uint8_t>& data, size_t offset) {
    return offset + 3 <= data.size() &&
           data[offset] == 0xFF && data[offset + 1] == 0xFF && data[offset + 2] == 0x02;
}

// 序列化TLV数据。长度对不上则失败，避免静默丢字段。
static bool serializeTLVData(const std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& custom_sei_data,
                             std::vector<uint8_t>& tlv_data) {
    tlv_data.clear();

    for (const auto& tlv_item : custom_sei_data) {
        if (!tlv_item) continue;

        if (tlv_item->tlv_length != tlv_item->tlv_data.size()) {
            printf("错误：TLV数据长度不匹配！Type=%d, 声明的长度=%d, 实际数据长度=%zu\n",
                   static_cast<int>(tlv_item->tlv_type),
                   tlv_item->tlv_length,
                   tlv_item->tlv_data.size());
            return false;
        }

        // TLV头部：Type(1字节) + Length(2字节，小端)
        writeToBuffer(tlv_data, static_cast<uint8_t>(tlv_item->tlv_type));
        writeToBuffer(tlv_data, tlv_item->tlv_length);
        tlv_data.insert(tlv_data.end(), tlv_item->tlv_data.begin(), tlv_item->tlv_data.end());
    }

    return true;
}

// 序列化协议头部
static std::vector<uint8_t> serializeProtocolHeader(const CustomSEIHeader& header) {
    std::vector<uint8_t> header_data;
    
    // 使用工具函数存储
    writeToBuffer(header_data, header.version);
    writeToBuffer(header_data, header.reserved);
    writeToBuffer(header_data, header.data_size);
    writeToBuffer(header_data, header.timestamp);
    
    return header_data;
}

// 查找H.264帧中的第一个NALU（4字节起始码优先，避免 00 00 00 01 被拆成 00 00 01）
static bool findFirstNALU(const std::vector<uint8_t>& frame_data, size_t& nalu_offset) {
    if (frame_data.size() < 4) {
        return false;
    }
    for (size_t i = 0; i + 2 < frame_data.size(); ++i) {
        size_t start_code_len = 0;
        if (!matchNaluStartCode(frame_data, i, start_code_len)) {
            continue;
        }
        if (i + start_code_len >= frame_data.size()) {
            return false;
        }
        nalu_offset = i + start_code_len;
        return true;
    }
    return false;
}

// 添加防竞争字节处理
static std::vector<uint8_t> addEmulationPreventionBytes(const std::vector<uint8_t>& data) {
    std::vector<uint8_t> result;
    result.reserve(data.size() + data.size() / 4);

    size_t consecutive_zero_count = 0;

    for (size_t i = 0; i < data.size(); i++) {
        uint8_t current_byte = data[i];

        if (consecutive_zero_count >= 2 &&
            (current_byte == 0x00 || current_byte == 0x01 || current_byte == 0x02 || current_byte == 0x03)) {
            result.push_back(0x03);
            consecutive_zero_count = 0;
        }

        result.push_back(current_byte);

        if (current_byte == 0x00) {
            consecutive_zero_count++;
        } else {
            consecutive_zero_count = 0;
        }
    }
    
    return result;
}

// 创建SEI NALU
static std::vector<uint8_t> createSEINALU(const std::vector<uint8_t>& sei_payload) {
    std::vector<uint8_t> sei_nalu;
    
    // NALU起始码
    sei_nalu.push_back(0x00);
    sei_nalu.push_back(0x00);
    sei_nalu.push_back(0x00);
    sei_nalu.push_back(0x01);
    
    // NALU头部：F(1) + NRI(2) + Type(5) = 0x06 (SEI)
    sei_nalu.push_back(SEI_NALU_TYPE);
    
    // SEI Payload Type: 0x200 (0xff + 0xff + 0x02)
    sei_nalu.push_back(0xFF);
    sei_nalu.push_back(0xFF);
    sei_nalu.push_back(0x02);
    
    // SEI Payload Length (使用H.264标准的长度编码)
    size_t payload_length = sei_payload.size();
    while (payload_length >= 0xFF) {
        sei_nalu.push_back(0xFF);
        payload_length -= 0xFF;
    }
    sei_nalu.push_back(static_cast<uint8_t>(payload_length));
    
    // 添加SEI Payload数据（已经包含防竞争字节）
    sei_nalu.insert(sei_nalu.end(), sei_payload.begin(), sei_payload.end());
    
    // 添加rbsp_trailing_bits (0x80)
    sei_nalu.push_back(0x80);
    
    return sei_nalu;
}

bool CustomSeiCodec::encode(const std::vector<uint8_t>& frame_data, 
                           const CustomSEIHeader& custom_sei_header,
                           const std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& custom_sei_data,
                           std::vector<uint8_t>& output_frame_data) {
    // 1. 序列化TLV数据
    std::vector<uint8_t> tlv_data;
    if (!serializeTLVData(custom_sei_data, tlv_data)) {
        return false;
    }
    if (tlv_data.size() > 0xFFFF) {
        return false;
    }

    // 2. 序列化协议头部（使用原始TLV数据大小）
    CustomSEIHeader header = custom_sei_header;
    header.data_size = static_cast<uint16_t>(tlv_data.size());
    std::vector<uint8_t> header_data = serializeProtocolHeader(header);
    
    // 3. 组合完整的SEI载荷
    std::vector<uint8_t> sei_payload;
    sei_payload.insert(sei_payload.end(), header_data.begin(), header_data.end());
    sei_payload.insert(sei_payload.end(), tlv_data.begin(), tlv_data.end());
    
    // 4. 统一添加防竞争字节
    std::vector<uint8_t> sei_payload_with_epb = addEmulationPreventionBytes(sei_payload);
    
    // 5. 创建SEI NALU（使用添加防竞争字节后的数据）
    std::vector<uint8_t> sei_nalu = createSEINALU(sei_payload_with_epb);
    
    // 6. 查找原始帧中的第一个NALU位置
    size_t first_nalu_offset = 0;
    if (!findFirstNALU(frame_data, first_nalu_offset)) {
        return false; // 未找到有效的NALU
    }
    
    // 7. 构建输出帧：起始码 + SEI NALU + 原始帧数据
    output_frame_data.clear();
    
    // 添加起始码
    output_frame_data.push_back(0x00);
    output_frame_data.push_back(0x00);
    output_frame_data.push_back(0x00);
    output_frame_data.push_back(0x01);
    
    // 添加SEI NALU（跳过起始码部分）
    output_frame_data.insert(output_frame_data.end(), 
                            sei_nalu.begin() + 4, sei_nalu.end());
    
    // 添加原始帧数据
    output_frame_data.insert(output_frame_data.end(), 
                            frame_data.begin(), frame_data.end());
    
    return true;
}

// 移除防竞争字节处理
static std::vector<uint8_t> removeEmulationPreventionBytes(const std::vector<uint8_t>& data) {
    std::vector<uint8_t> result;
    result.reserve(data.size());
    
    for (size_t i = 0; i < data.size(); i++) {
        if (i >= 2 && (i + 1) < data.size() &&
            data[i] == 0x03 &&
            data[i - 1] == 0x00 &&
            data[i - 2] == 0x00 &&
            (data[i + 1] == 0x00 || data[i + 1] == 0x01 || data[i + 1] == 0x02 || data[i + 1] == 0x03)) {
            continue;
        }
        result.push_back(data[i]);
    }
    
    return result;
}

// 查找并提取自定义 SEI NALU（payload type 0x200）
static bool findSEINALU(const std::vector<uint8_t>& frame_data, std::vector<uint8_t>& sei_nalu) {
    if (frame_data.size() < 4) {
        return false;
    }

    size_t i = 0;
    while (i + 2 < frame_data.size()) {
        size_t start_code_len = 0;
        if (!matchNaluStartCode(frame_data, i, start_code_len)) {
            ++i;
            continue;
        }

        const size_t nalu_hdr = i + start_code_len;
        if (nalu_hdr >= frame_data.size()) {
            break;
        }
        if ((frame_data[nalu_hdr] & 0x1F) != SEI_NALU_TYPE) {
            i = nalu_hdr + 1;
            continue;
        }

        size_t offset = nalu_hdr + 1;
        if (!isCustomSeiPayloadType(frame_data, offset)) {
            i = nalu_hdr + 1;
            continue;
        }
        offset += 3;

        size_t payload_length = 0;
        while (offset < frame_data.size() && frame_data[offset] == 0xFF) {
            payload_length += 0xFF;
            ++offset;
        }
        if (offset >= frame_data.size()) {
            return false;
        }
        payload_length += frame_data[offset];
        ++offset;

        const size_t end = offset + payload_length + 1; // +1 for rbsp_trailing_bits
        if (end > frame_data.size()) {
            i = nalu_hdr + 1;
            continue;
        }

        sei_nalu.assign(frame_data.begin() + i, frame_data.begin() + end);
        return true;
    }
    return false;
}

// 解析SEI NALU
static bool parseSEINALU(const std::vector<uint8_t>& sei_nalu, std::vector<uint8_t>& sei_payload) {
    if (sei_nalu.size() < 8) return false;
    
    size_t offset = 0;
    size_t start_code_len = 0;
    if (!matchNaluStartCode(sei_nalu, 0, start_code_len)) {
        return false;
    }
    offset = start_code_len;
    
    // 检查NALU类型
    if (offset >= sei_nalu.size() || (sei_nalu[offset] & 0x1F) != SEI_NALU_TYPE) {
        return false;
    }
    offset++;
    
    // 检查SEI Payload Type
    if (!isCustomSeiPayloadType(sei_nalu, offset)) {
        return false;
    }
    offset += 3;
    
    // 解析SEI Payload Length
    size_t payload_length = 0;
    while (offset < sei_nalu.size() && sei_nalu[offset] == 0xFF) {
        payload_length += 0xFF;
        offset++;
    }
    if (offset >= sei_nalu.size()) return false;
    payload_length += sei_nalu[offset];
    offset++;
    
    // 提取SEI Payload（不包含rbsp_trailing_bits）
    if (offset + payload_length > sei_nalu.size()) return false;
    sei_payload.assign(sei_nalu.begin() + offset, sei_nalu.begin() + offset + payload_length);
    
    // 跳过rbsp_trailing_bits (0x80)
    if (offset + payload_length < sei_nalu.size() && sei_nalu[offset + payload_length] == 0x80) {
        // rbsp_trailing_bits存在，这是正常的
    }
    
    return true;
}

// 解析协议头部
static bool parseProtocolHeader(const std::vector<uint8_t>& data, CustomSEIHeader& header) {
    // CustomSEIHeader结构体大小：version(1B) + reserved(1B) + data_size(2B) + timestamp(8B) = 12字节
    if (data.size() < 12) return false;
    
    uint32_t offset = 0;
    
    // 使用工具函数读取
    header.version = readFromBuffer<uint8_t>(data, offset);
    header.reserved = readFromBuffer<uint8_t>(data, offset);
    header.data_size = readFromBuffer<uint16_t>(data, offset);
    header.timestamp = readFromBuffer<uint64_t>(data, offset);
    
    return true;
}

// 解析TLV数据
static bool parseTLVData(const std::vector<uint8_t>& tlv_data, 
                        std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& output_sei_data) {
    uint32_t offset = 0;
    
    while (offset < tlv_data.size()) {
        if (offset + 3 > tlv_data.size()) break; // 至少需要Type + Length
        
        // 解析Type和Length - 使用工具函数
        auto type = static_cast<CustomSeiDataType>(readFromBuffer<uint8_t>(tlv_data, offset));
        auto length = readFromBuffer<uint16_t>(tlv_data, offset);
        
        // 检查数据长度
        if (offset + length > tlv_data.size()) break;
        
        // 创建TLV数据基类
        CustomSeiTLVDataBase tlv_base;
        tlv_base.tlv_type = type;
        tlv_base.tlv_length = length;
        tlv_base.tlv_data.assign(tlv_data.begin() + offset, tlv_data.begin() + offset + length);
        
        // 根据类型创建具体的TLV数据对象
        std::shared_ptr<CustomSeiTLVDataBase> tlv_item;
        switch (type) {
            case CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA:
                tlv_item = std::make_shared<CustomSeiCustomData>(tlv_base);
                break;
            case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX:
                tlv_item = std::make_shared<CustomSeiObjectDetectionBox>(tlv_base);
                break;
            case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON:
                tlv_item = std::make_shared<CustomSeiObjectDetectionPolygon>(tlv_base);
                break;
            case CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY:
                tlv_item = std::make_shared<CustomSeiTextOverlay>(tlv_base);
                break;
            default:
                // 未知类型，创建基类对象
                tlv_item = std::make_shared<CustomSeiTLVDataBase>(tlv_base);
                break;
        }
        
        output_sei_data.push_back(tlv_item);
        offset += length;
    }
    
    return !output_sei_data.empty();
}

bool CustomSeiCodec::decode(const std::vector<uint8_t>& frame_data, 
                           CustomSEIHeader& output_header, 
                           std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& output_sei_data) {
    output_sei_data.clear();
    
    // 1. 查找并提取SEI NALU
    std::vector<uint8_t> sei_nalu;
    if (!findSEINALU(frame_data, sei_nalu)) {
        return false; // 未找到SEI NALU
    }
    
    // 2. 解析SEI NALU，提取SEI Payload
    std::vector<uint8_t> sei_payload;
    if (!parseSEINALU(sei_nalu, sei_payload)) {
        return false; // 解析SEI NALU失败
    }
    
    // 3. 统一移除防竞争字节处理
    std::vector<uint8_t> sei_payload_without_epb = removeEmulationPreventionBytes(sei_payload);
    
    // 4. 解析协议头部
    if (!parseProtocolHeader(sei_payload_without_epb, output_header)) {
        return false; // 解析协议头部失败
    }
    
    // 5. 提取TLV数据
    // 跳过协议头部(12字节)后提取TLV数据
    if (sei_payload_without_epb.size() < 12 + output_header.data_size) {
        return false; // 数据长度不足
    }
    
    std::vector<uint8_t> tlv_data(sei_payload_without_epb.begin() + 12, 
                                 sei_payload_without_epb.begin() + 12 + output_header.data_size);
    
    // 6. 解析TLV数据
    if (!parseTLVData(tlv_data, output_sei_data)) {
        return false; // 解析TLV数据失败
    }
    
    return true;
}
