// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "custom_sei_module_impl.h"
#include <base/common/logger.h>
#include "custom_sei_data_define.h"
#include "custom_sei_codec.h"
#include <cstdint>
#include <memory>
#include <chrono>
#include <vector>

namespace flight_hub_sdk {

CustomSEIModuleImpl::CustomSEIModuleImpl() {
    LOG_INFO("CustomSEIModuleImpl initialized with H264 Analyze support");
}

bool CustomSEIModuleImpl::CustomSEIEncode(const std::vector<int>& frame_data,
                                         const std::vector<SeiItem>& custom_sei_data,
                                         std::vector<int>& output_frame_data) {
    // 空数据校验，防止 SIGSEGV
    if (frame_data.empty()) {
        LOG_ERROR("CustomSEIModule: encode failed, frame_data is empty");
        return false;
    }
    // 转换 SDK 数据到 h264_analyze 格式
    auto h264_data = convertToH264AnalyzeData(custom_sei_data);
    
    // 创建自定义 SEI 头部
    CustomSEIHeader header;
    header.version = 1;
    header.reserved = 0;
    // 获取毫秒级时间戳
    auto now = std::chrono::system_clock::now();
    auto duration = now.time_since_epoch();
    header.timestamp = static_cast<std::uint64_t>(std::chrono::duration_cast<std::chrono::milliseconds>(duration).count());

    // data_size 由 CustomSeiCodec::encode 按序列化后的 TLV 长度填写
    header.data_size = 0;

    // 转换 frame_data 到 uint8_t 格式
    std::vector<uint8_t> h264_frame_data;
    h264_frame_data.reserve(frame_data.size());
    for (const int val : frame_data) {
        h264_frame_data.push_back(static_cast<uint8_t>(val & 0xFF));
    }
    std::vector<uint8_t> h264_output_data;
    
    // 调用 h264_analyze 库的编码函数
    bool result = CustomSeiCodec::encode(h264_frame_data, header, h264_data, h264_output_data);

    if (result) {
        // 转换回 int 格式
        output_frame_data.clear();
        for (uint8_t val : h264_output_data) {
            output_frame_data.push_back(static_cast<int>(val));
        }
        LOG_DEBUG("Custom SEI encode successful, output size: %zu", output_frame_data.size());
    } else {
        LOG_ERROR("Custom SEI encode failed");
    }

    return result;
}

bool CustomSEIModuleImpl::CustomSEIDecode(const std::vector<int>& frame_data,
                                         SeiHeader& output_header,
                                         std::vector<SeiItem>& output_sei_data) {
    // 空数据校验，防止 SIGSEGV
    if (frame_data.empty()) {
        LOG_ERROR("CustomSEIModule: decode failed, frame_data is empty");
        return false;
    }
    // 转换 frame_data 到 uint8_t 格式
    std::vector<uint8_t> h264_frame_data;
    h264_frame_data.reserve(frame_data.size());
    for (const int val : frame_data) {
        h264_frame_data.push_back(static_cast<uint8_t>(val & 0xFF));
    }
    
    // 调用 h264_analyze 库的解码函数
    CustomSEIHeader h264_header;
    std::vector<std::shared_ptr<CustomSeiTLVDataBase>> h264_data;
    
    bool result = CustomSeiCodec::decode(h264_frame_data, h264_header, h264_data);

    if (result) {
        // 转换 h264_analyze 数据到 SDK 格式
        output_header = convertFromH264AnalyzeHeader(h264_header);
        output_sei_data = convertFromH264AnalyzeData(h264_data);
        
        LOG_DEBUG("Custom SEI decode successful, found %zu SEI data items", output_sei_data.size());
    } else {
        LOG_DEBUG("Custom SEI decode failed or no SEI data found");
    }

    return result;
}

// 数据转换函数实现
std::vector<std::shared_ptr<CustomSeiTLVDataBase>> CustomSEIModuleImpl::convertToH264AnalyzeData(const std::vector<SeiItem>& sdk_data) {
    std::vector<std::shared_ptr<CustomSeiTLVDataBase>> h264_data;
    
    for (const auto& item : sdk_data) {
        switch (static_cast<SeiDataType>(item.type)) {
            case SeiDataType::CUSTOM_DATA: {
                // 转换 int 数据到 uint8_t
                std::vector<uint8_t> h264_custom_data;
                h264_custom_data.reserve(item.custom_data.data.size());
                for (const int val : item.custom_data.data) {
                    h264_custom_data.push_back(static_cast<uint8_t>(val & 0xFF));
                }
                auto h264_item = std::make_shared<CustomSeiCustomData>(h264_custom_data);
                h264_data.push_back(h264_item);
                break;
            }
            case SeiDataType::DETECTION_BOX: {
                auto h264_item = std::make_shared<CustomSeiObjectDetectionBox>(
                    static_cast<uint16_t>(item.detection_box.id),
                    static_cast<uint8_t>(item.detection_box.type),
                    static_cast<uint8_t>(item.detection_box.state),
                    static_cast<uint16_t>(item.detection_box.x),
                    static_cast<uint16_t>(item.detection_box.y),
                    static_cast<uint16_t>(item.detection_box.width),
                    static_cast<uint16_t>(item.detection_box.height),
                    static_cast<uint8_t>(item.detection_box.confidence)
                );
                h264_data.push_back(h264_item);
                break;
            }
            case SeiDataType::DETECTION_POLYGON: {
                std::vector<Point2D> points;
                points.reserve(item.detection_polygon.points.size());
                for (const auto& p : item.detection_polygon.points) {
                    points.emplace_back(static_cast<uint16_t>(p.x), static_cast<uint16_t>(p.y));
                }
                auto h264_item = std::make_shared<CustomSeiObjectDetectionPolygon>(
                    static_cast<uint16_t>(item.detection_polygon.id),
                    static_cast<uint8_t>(item.detection_polygon.type),
                    static_cast<uint8_t>(item.detection_polygon.state),
                    static_cast<uint8_t>(item.detection_polygon.confidence),
                    static_cast<uint16_t>(item.detection_polygon.points.size()),
                    points
                );
                h264_data.push_back(h264_item);
                break;
            }
            case SeiDataType::TEXT_OVERLAY: {
                auto h264_item = std::make_shared<CustomSeiTextOverlay>(
                    static_cast<uint16_t>(item.text_overlay.id),
                    static_cast<uint16_t>(item.text_overlay.x),
                    static_cast<uint16_t>(item.text_overlay.y),
                    static_cast<uint8_t>(item.text_overlay.font_size),
                    static_cast<uint8_t>(item.text_overlay.color_r),
                    static_cast<uint8_t>(item.text_overlay.color_g),
                    static_cast<uint8_t>(item.text_overlay.color_b),
                    static_cast<uint8_t>(item.text_overlay.alpha),
                    item.text_overlay.text
                );
                h264_data.push_back(h264_item);
                break;
            }
        }
    }
    
    return h264_data;
}

std::vector<SeiItem> CustomSEIModuleImpl::convertFromH264AnalyzeData(const std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& h264_data) {
    std::vector<SeiItem> sdk_data;
    
    for (const auto& item : h264_data) {
        switch (item->tlv_type) {
            case CUSTOM_SEI_DATA_TYPE_CUSTOM_DATA: {
                auto custom_data = std::dynamic_pointer_cast<CustomSeiCustomData>(item);
                if (custom_data) {
                    // 转换 uint8_t 数据到 int
                    std::vector<int> sdk_custom_data;
                    for (uint8_t val : custom_data->data) {
                        sdk_custom_data.push_back(static_cast<int>(val));
                    }
                    sdk_data.emplace_back(SeiData{sdk_custom_data});
                }
                break;
            }
            case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_BOX: {
                auto box_data = std::dynamic_pointer_cast<CustomSeiObjectDetectionBox>(item);
                if (box_data) {
                    sdk_data.emplace_back(SeiDetectionBox{
                        static_cast<int>(box_data->id),
                        static_cast<int>(box_data->type),
                        static_cast<int>(box_data->state),
                        static_cast<int>(box_data->x),
                        static_cast<int>(box_data->y),
                        static_cast<int>(box_data->width),
                        static_cast<int>(box_data->height),
                        static_cast<int>(box_data->confidence)
                    });
                }
                break;
            }
            case CUSTOM_SEI_DATA_TYPE_OBJECT_DETECTION_POLYGON: {
                auto polygon_data = std::dynamic_pointer_cast<CustomSeiObjectDetectionPolygon>(item);
                if (polygon_data) {
                    std::vector<SeiPoint2D> points;
                    for (const auto& p : polygon_data->points) {
                        points.push_back({static_cast<int>(p.x), static_cast<int>(p.y)});
                    }
                    sdk_data.emplace_back(SeiDetectionPolygon{
                        static_cast<int>(polygon_data->id),
                        static_cast<int>(polygon_data->type),
                        static_cast<int>(polygon_data->state),
                        static_cast<int>(polygon_data->confidence),
                        points
                    });
                }
                break;
            }
            case CUSTOM_SEI_DATA_TYPE_TEXT_OVERLAY: {
                auto text_data = std::dynamic_pointer_cast<CustomSeiTextOverlay>(item);
                if (text_data) {
                    sdk_data.emplace_back(SeiTextOverlay{
                        static_cast<int>(text_data->id),
                        static_cast<int>(text_data->x),
                        static_cast<int>(text_data->y),
                        static_cast<int>(text_data->font_size),
                        static_cast<int>(text_data->color_r),
                        static_cast<int>(text_data->color_g),
                        static_cast<int>(text_data->color_b),
                        static_cast<int>(text_data->alpha),
                        text_data->text
                    });
                }
                break;
            }
            case CUSTOM_SEI_DATA_TYPE_UNKNOWN:
            default:
                // 未知类型，跳过
                break;
        }
    }
    
    return sdk_data;
}

CustomSEIHeader CustomSEIModuleImpl::convertToH264AnalyzeHeader(const SeiHeader& sdk_header) {
    CustomSEIHeader h264_header;
    h264_header.version = static_cast<uint8_t>(sdk_header.version);
    h264_header.reserved = static_cast<uint8_t>(sdk_header.reserved);
    h264_header.data_size = static_cast<uint16_t>(sdk_header.data_size);
    h264_header.timestamp = static_cast<uint64_t>(sdk_header.timestamp);
    return h264_header;
}

SeiHeader CustomSEIModuleImpl::convertFromH264AnalyzeHeader(const CustomSEIHeader& h264_header) {
    SeiHeader sdk_header;
    sdk_header.version = static_cast<int>(h264_header.version);
    sdk_header.reserved = static_cast<int>(h264_header.reserved);
    sdk_header.data_size = static_cast<int>(h264_header.data_size);
    sdk_header.timestamp = static_cast<long>(h264_header.timestamp);
    return sdk_header;
}

} // namespace flight_hub_sdk
