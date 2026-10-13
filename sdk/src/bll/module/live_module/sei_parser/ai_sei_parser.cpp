// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ai_sei_parser.h"
#include "base/common/logger.h"

namespace flight_hub_sdk {

namespace {

// AI SEI target_type / group 常量，对齐公开直播 SEI 布局。
// Parser 只按区间做保留/归并；细分含义原样留在 AiDetectionTarget.target_type，
// 由业务侧按下表映射 icon/文案。
//
// 官方模型：
//   2 人 / 3 车 / 4 船 / 34 移动目标
// YOLOE 人（35–43，计数归并为 person）：
//   35 不限色 / 36 黑灰 / 37 白 / 38 蓝 / 39 红粉 / 40 黄橙 / 41 绿迷彩 / 42 棕褐 / 43 紫
// YOLOE 轿车（44–52，计数归并为 car）：
//   44 不限色 / 45 黑灰 / 46 白 / 47 蓝 / 48 红粉 / 49 黄橙 / 50 绿迷彩 / 51 棕褐 / 52 紫
// YOLOE 独立类型（53–73，除 54 船外独立展示）：
//   53 公交 / 54 船 / 55 搅拌车 / 56 渣土车 / 57 叉车 / 58 挖掘机 / 59 压路机
//   60 起重机 / 61 货车 / 62 油罐车 / 63 警车 / 64 救护车 / 65 消防车
//   66 自行车 / 67 摩托车 / 68 河道漂浮物 / 69 烟 / 70 火 / 71 牛 / 72 马 / 73 羊
// SEI group：
//   10 = 2D 框+距离；12 = 按类型计数；v4 modelInfo type=1 后跟 model_index + frame_source

constexpr uint8_t kAiTypePerson = 2;
constexpr uint8_t kAiTypeCar = 3;
constexpr uint8_t kAiTypeBoat = 4;
constexpr uint8_t kAiTypeMoving = 34;
constexpr uint8_t kYoloePersonMin = 35;
constexpr uint8_t kYoloePersonMax = 43;
constexpr uint8_t kYoloeSedanMin = 44;
constexpr uint8_t kYoloeSedanMax = 52;
constexpr uint8_t kYoloeBus = 53;
constexpr uint8_t kYoloeBoat = 54;
constexpr uint8_t kYoloeSheep = 73;
constexpr uint8_t kGroupTwoDBox = 10;
constexpr uint8_t kGroupTypeCount = 12;
constexpr uint8_t kModelInfoBase = 1;

bool IsYoloePersonType(uint8_t type) {
    return type >= kYoloePersonMin && type <= kYoloePersonMax;
}

bool IsYoloeCarType(uint8_t type) {
    return type >= kYoloeSedanMin && type <= kYoloeSedanMax;
}

bool IsYoloeIndependentType(uint8_t type) {
    return type >= kYoloeBus && type <= kYoloeSheep && type != kYoloeBoat;
}

bool ShouldKeepTarget(uint8_t type) {
    return type == kAiTypePerson || type == kAiTypeCar || type == kAiTypeBoat ||
           type == kAiTypeMoving || IsYoloePersonType(type) || IsYoloeCarType(type) ||
           type == kYoloeBoat || IsYoloeIndependentType(type);
}

bool SkipV4ModelInfo(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer, uint32_t& offset) {
    if (offset >= buffer->size()) {
        return false;
    }
    const uint8_t model_info_type = buffer->data()[offset++];
    if (model_info_type == kModelInfoBase) {
        if (offset + 2 > buffer->size()) {
            return false;
        }
        offset += 2;  // model_index + frame_source
    }
    return true;
}

}  // namespace

AiSeiParser::AiSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer)
    : sei_buffer_(sei_buffer) {}

AiSeiParser::~AiSeiParser() {}

bool AiSeiParser::parse() {
    if (!sei_buffer_) {
        LOG_ERROR("sei_buffer_ is null");
        return false;
    }

    if (sei_buffer_->size() < 10) {
        LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
        return false;
    }
    uint32_t offset = 0;
    version_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    time_stamp_ = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
    offset += 4;

    frame_type_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    if (frame_type_ == 1) {
        if (sei_buffer_->size() < 22) {
            LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
            return false;
        }
        offset += 12;
    }

    track_id_ = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    reserved_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    obj_group_count_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    for (int i = 0; i < obj_group_count_; i++) {
        if (offset + 2 > sei_buffer_->size()) {
            LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
            return false;
        }

        auto group_type = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
        offset += 1;

        auto group_count = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
        offset += 1;

        if (group_type == kGroupTypeCount) {
            if (offset + 3 * group_count > sei_buffer_->size()) {
                LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
                return false;
            }
            for (int j = 0; j < group_count; j++) {
                TargetCount target_count;
                target_count.type = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
                offset += 1;

                target_count.count = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
                offset += 2;

                target_counts_.push_back(target_count);
            }

            if (version_ == 4 && !SkipV4ModelInfo(sei_buffer_, offset)) {
                LOG_ERROR("sei_buffer_ size is too small to parse v4 model info");
                return false;
            }
            continue;
        }

        if (group_type == kGroupTwoDBox) { // DJI_TM_OBJ_GROUP_OBJ_2D_BOX_WITH_DISTANCE
            if (offset + 16 * group_count > sei_buffer_->size()) {
                LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
                return false;
            }

            for (int j = 0; j < group_count; j++) {
                Target target;
                target.target_id = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
                offset += 2;

                target.target_type = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
                offset += 1;

                target.target_state = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
                offset += 1;

                target.target_cx = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
                offset += 2;

                target.target_cy = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
                offset += 2;

                target.target_w = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
                offset += 2;

                target.target_h = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
                offset += 2;

                target.target_distance = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
                offset += 4;

                if (ShouldKeepTarget(target.target_type)) {
                    targets_.push_back(target);
                }
            }

            if (version_ == 4 && !SkipV4ModelInfo(sei_buffer_, offset)) {
                LOG_ERROR("sei_buffer_ size is too small to parse v4 model info");
                return false;
            }
            continue;
        }

        LOG_WARN("AiSeiParser: unknown group_type=%d, stop parsing", group_type);
        break;
    }
    return true;
}

AiSeiInfo AiSeiParser::get_ai_sei_info() const {
    AiSeiInfo ai_sei_info;
    for (auto& target : targets_) {
        AiDetectionTarget ai_detection_target;
        ai_detection_target.target_id = target.target_id;
        ai_detection_target.target_type = target.target_type;
        ai_detection_target.target_state = target.target_state;
        ai_detection_target.target_cx = target.target_cx;
        ai_detection_target.target_cy = target.target_cy;
        ai_detection_target.target_w = target.target_w;
        ai_detection_target.target_h = target.target_h;
        ai_detection_target.target_distance = target.target_distance;
        ai_sei_info.targets.push_back(ai_detection_target);
    }
    return ai_sei_info;
}

}