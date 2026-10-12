// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <type_traits>
#include <utility>
#include <vector>
#include <string>

// 按小端序读写，避免本机端序和未对齐解引用
template<typename T>
T readFromBuffer(const std::vector<uint8_t>& buffer, uint32_t& offset) {
    static_assert(std::is_integral<T>::value && std::is_unsigned<T>::value,
                  "readFromBuffer only supports unsigned integers");
    T value = 0;
    if (offset + sizeof(T) > buffer.size()) {
        offset = static_cast<uint32_t>(buffer.size());
        return 0;
    }
    for (size_t i = 0; i < sizeof(T); ++i) {
        value |= static_cast<T>(buffer[offset + i]) << (8 * i);
    }
    offset += sizeof(T);
    return value;
}

template<typename T>
void writeToBuffer(std::vector<uint8_t>& buffer, T value) {
    static_assert(std::is_integral<T>::value && std::is_unsigned<T>::value,
                  "writeToBuffer only supports unsigned integers");
    for (size_t i = 0; i < sizeof(T); ++i) {
        buffer.push_back(static_cast<uint8_t>((value >> (8 * i)) & 0xFF));
    }
}

template<>
inline uint8_t readFromBuffer<uint8_t>(const std::vector<uint8_t>& buffer, uint32_t& offset) {
    if (offset >= buffer.size()) {
        return 0;
    }
    return buffer[offset++];
}

template<>
inline void writeToBuffer<uint8_t>(std::vector<uint8_t>& buffer, uint8_t value) {
    buffer.push_back(value);
}

struct CustomSEIHeader {
    uint8_t version = 1; // 版本号
    uint8_t reserved = 0; // 保留字段
    uint16_t data_size = 0; // 自定义SEI数据大小
    uint64_t timestamp = 0; // 毫秒时间戳
};

enum CustomSeiDataType {
    CUSTOM_SEI_DATA_TYPE_UNKNOWN = 0x00,             // 保留字段
    CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA = 0x01,         // 自定义数据
    CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX = 0x02,    // 目标检测数据（水平框）
    CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON = 0x03, // 目标检测数据（多边形框）
    CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY = 0x04,        // 文本叠加数据
};

// TLV 数据公共基类
struct CustomSeiTLVDataBase {
    CustomSeiTLVDataBase() = default;
    
    CustomSeiTLVDataBase(const CustomSeiTLVDataBase& tlv_data_base) {
        this->tlv_type = tlv_data_base.tlv_type;
        this->tlv_length = tlv_data_base.tlv_length;
        this->tlv_data = tlv_data_base.tlv_data;
    }
    
    virtual ~CustomSeiTLVDataBase() = default;
    
    CustomSeiDataType tlv_type = CUSTOM_SEI_DATA_TYPE_UNKNOWN;
    uint16_t tlv_length = 0;
    std::vector<uint8_t> tlv_data;
};

// CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA
struct CustomSeiCustomData : CustomSeiTLVDataBase {
    explicit CustomSeiCustomData(const CustomSeiTLVDataBase& tlv_data_base) : CustomSeiTLVDataBase(tlv_data_base) {
        if (tlv_type != CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA) {
            return;
        }
        if (tlv_length != tlv_data.size()) {
            return;
        }
        // 从TLV数据解析业务字段
        data = tlv_data;
    }
    
    // 构造函数：从业务字段创建
    explicit CustomSeiCustomData(const std::vector<uint8_t>& custom_data)
        : CustomSeiTLVDataBase(), data(custom_data) {
        tlv_type = CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA;
        serializeToTLV();
    }
    
    // 序列化到TLV格式
    void serializeToTLV() {
        tlv_data.clear();
        // 直接复制自定义数据
        tlv_data.insert(tlv_data.end(), data.begin(), data.end());
        tlv_type = CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA;  // 确保设置正确的type
        tlv_length = static_cast<uint16_t>(tlv_data.size());  // 在设置type之后计算长度
    }
    
    std::vector<uint8_t> data; // 自定义数据内容
};

// CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX
struct CustomSeiObjectDetectionBox : CustomSeiTLVDataBase {
    explicit CustomSeiObjectDetectionBox(const CustomSeiTLVDataBase& tlv_data_base) : CustomSeiTLVDataBase(tlv_data_base) {
        if (tlv_type != CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX) {
            return;
        }
        if (tlv_length != tlv_data.size()) {
            return;
        }
        // 从TLV数据解析业务字段 - 使用工具函数
        if (tlv_data.size() >= 13) {
            uint32_t offset = 0;
            id = readFromBuffer<uint16_t>(tlv_data, offset);
            x = readFromBuffer<uint16_t>(tlv_data, offset);
            y = readFromBuffer<uint16_t>(tlv_data, offset);
            width = readFromBuffer<uint16_t>(tlv_data, offset);
            height = readFromBuffer<uint16_t>(tlv_data, offset);
            confidence = readFromBuffer<uint8_t>(tlv_data, offset);
            type = readFromBuffer<uint8_t>(tlv_data, offset);
            state = readFromBuffer<uint8_t>(tlv_data, offset);
        }
    }
    
    // 构造函数：从业务字段创建
    CustomSeiObjectDetectionBox(uint16_t id, uint8_t type, uint8_t state, 
                               uint16_t x, uint16_t y, uint16_t width, uint16_t height, 
                               uint8_t confidence) 
        : CustomSeiTLVDataBase(), id(id), x(x), y(y), width(width), height(height), confidence(confidence), type(type), state(state) {
        tlv_type = CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX;
        serializeToTLV();
    }
    
    // 序列化到TLV格式
    void serializeToTLV() {
        tlv_data.clear();
        
        // 使用工具函数存储
        writeToBuffer(tlv_data, id);
        writeToBuffer(tlv_data, x);
        writeToBuffer(tlv_data, y);
        writeToBuffer(tlv_data, width);
        writeToBuffer(tlv_data, height);
        writeToBuffer(tlv_data, confidence);
        writeToBuffer(tlv_data, type);
        writeToBuffer(tlv_data, state);
        
        tlv_length = static_cast<uint16_t>(tlv_data.size());
    }
    
    uint16_t id = 0; // 目标ID (0-65535)
    uint16_t x = 0;
    uint16_t y = 0; // 左上角坐标 (归一化到0-10000)
    uint16_t width = 0;
    uint16_t height = 0; // 宽高 (归一化到0-10000)
    uint8_t confidence = 0; // 置信度 (0-100)
    uint8_t type = 0; // 目标类型
    uint8_t state = 0; // 目标状态
};

// 2D坐标点结构
struct Point2D {
    uint16_t x = 0; // X坐标 (归一化到0-10000)
    uint16_t y = 0; // Y坐标 (归一化到0-10000)
    
    Point2D() = default;
    Point2D(uint16_t x, uint16_t y) : x(x), y(y) {}
};

// CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON
struct CustomSeiObjectDetectionPolygon : CustomSeiTLVDataBase {
    explicit CustomSeiObjectDetectionPolygon(const CustomSeiTLVDataBase& tlv_data_base) : CustomSeiTLVDataBase(tlv_data_base) {
        if (tlv_type != CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON) {
            return;
        }
        if (tlv_length != tlv_data.size()) {
            return;
        }
        // 从TLV数据解析业务字段 - 使用工具函数
        if (tlv_data.size() >= 7) { // 至少需要 id(2) + type(1) + state(1) + confidence(1) + point_count(2)
            uint32_t offset = 0;
            id = readFromBuffer<uint16_t>(tlv_data, offset);
            type = readFromBuffer<uint8_t>(tlv_data, offset);
            state = readFromBuffer<uint8_t>(tlv_data, offset);
            confidence = readFromBuffer<uint8_t>(tlv_data, offset);
            point_count = readFromBuffer<uint16_t>(tlv_data, offset);
            
            // 解析坐标点
            points.clear();
            for (uint16_t i = 0; i < point_count && offset + 4 <= tlv_data.size(); i++) {
                auto x = readFromBuffer<uint16_t>(tlv_data, offset);
                auto y = readFromBuffer<uint16_t>(tlv_data, offset);
                points.emplace_back(x, y);
            }
        }
    }
    
    // 构造函数：从业务字段创建
    CustomSeiObjectDetectionPolygon(uint16_t id, uint8_t type, uint8_t state, 
                                   uint8_t confidence, uint16_t point_count, const std::vector<Point2D>& polygon_points) 
        : CustomSeiTLVDataBase(), id(id), type(type), state(state), confidence(confidence), 
          point_count(point_count), points(polygon_points) {
        tlv_type = CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON;
        serializeToTLV();
    }
    
    // 序列化到TLV格式
    void serializeToTLV() {
        tlv_data.clear();
        
        // 使用工具函数存储
        writeToBuffer(tlv_data, id);
        writeToBuffer(tlv_data, type);
        writeToBuffer(tlv_data, state);
        writeToBuffer(tlv_data, confidence);
        writeToBuffer(tlv_data, point_count); // 坐标点数量
        
        // 存储所有坐标点
        for (const auto& point : points) {
            writeToBuffer(tlv_data, point.x);
            writeToBuffer(tlv_data, point.y);
        }
        
        tlv_length = static_cast<uint16_t>(tlv_data.size());
    }
    
    uint16_t id = 0; // 目标ID (0-65535)
    uint8_t type = 0; // 目标类型
    uint8_t state = 0; // 目标状态
    uint8_t confidence = 0; // 置信度 (0-100)
    uint16_t point_count = 0; // 坐标点数量
    std::vector<Point2D> points; // 多边形坐标点列表
};

// CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY
struct CustomSeiTextOverlay : CustomSeiTLVDataBase {
    explicit CustomSeiTextOverlay(const CustomSeiTLVDataBase& tlv_data_base) : CustomSeiTLVDataBase(tlv_data_base) {
        if (tlv_type != CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY) {
            return;
        }
        if (tlv_length != tlv_data.size()) {
            return;
        }
        // 从TLV数据解析业务字段 - 使用工具函数
        if (tlv_data.size() >= 11) {
            uint32_t offset = 0;
            id = readFromBuffer<uint16_t>(tlv_data, offset);
            x = readFromBuffer<uint16_t>(tlv_data, offset);
            y = readFromBuffer<uint16_t>(tlv_data, offset);
            font_size = readFromBuffer<uint8_t>(tlv_data, offset);
            color_r = readFromBuffer<uint8_t>(tlv_data, offset);
            color_g = readFromBuffer<uint8_t>(tlv_data, offset);
            color_b = readFromBuffer<uint8_t>(tlv_data, offset);
            alpha = readFromBuffer<uint8_t>(tlv_data, offset);
            
            // 解析文本内容
            if (tlv_data.size() > offset) {
                text.assign(tlv_data.begin() + offset, tlv_data.end());
            }
        }
    }
    
    // 构造函数：从业务字段创建
    CustomSeiTextOverlay(uint16_t id, uint16_t x, uint16_t y, uint8_t font_size,
                        uint8_t color_r, uint8_t color_g, uint8_t color_b, uint8_t alpha,
                        std::string text)
        : CustomSeiTLVDataBase(), id(id), x(x), y(y), font_size(font_size), color_r(color_r), color_g(color_g), 
          color_b(color_b), alpha(alpha), text(std::move(text)) {
        tlv_type = CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY;
        serializeToTLV();
    }
    
    // 序列化到TLV格式
    void serializeToTLV() {
        tlv_data.clear();
        
        // 使用工具函数存储
        writeToBuffer(tlv_data, id);
        writeToBuffer(tlv_data, x);
        writeToBuffer(tlv_data, y);
        writeToBuffer(tlv_data, font_size);
        writeToBuffer(tlv_data, color_r);
        writeToBuffer(tlv_data, color_g);
        writeToBuffer(tlv_data, color_b);
        writeToBuffer(tlv_data, alpha);
        
        // 添加文本内容
        tlv_data.insert(tlv_data.end(), text.begin(), text.end());
        tlv_length = static_cast<uint16_t>(tlv_data.size());
    }
    
    uint16_t id = 0; // 文本ID
    uint16_t x = 0; // 坐标 (归一化到0-10000)
    uint16_t y = 0; // 坐标 (归一化到0-10000)
    uint8_t font_size = 0; // 字体大小 (1-255)
    uint8_t color_r = 0; // RGB颜色 R
    uint8_t color_g = 0; // RGB颜色 G
    uint8_t color_b = 0; // RGB颜色 B
    uint8_t alpha = 0; // 透明度 (0-255)
    std::string text; // UTF-8文本内容
};
