// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include "base/io/byte_buffer_pool.h"
#include "base/common/logger.h"
#include <cstdint>

namespace flight_hub_sdk {

#define SEI_NALU_TYPE 0x06
#define EXT_SEI_TYPE_1 0xf5  // DJI SEI type: 245
#define EXT_SEI_TYPE_2 0x66  // DJI SEI type: 102
#define EXT_SEI_TYPE_VOLC 0x64  // VolcEngine SEI type: 100
#define EXT_SEI_TYPE_AGORA 0x65  // Agora SEI type: 101

inline bool is_key_frame(const uint8_t *data, const uint32_t& length) {
    // 添加空指针检查
    if (data == nullptr) {
        LOG_WARN("is_key_frame: data is null");
        return false;
    }
    
    // 添加长度检查
    if (length < 4) {
        LOG_WARN("is_key_frame: length too small: %u", length);
        return false;
    }
    
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

inline bool find_next_nalu(const uint8_t *data, const uint32_t& length, uint32_t& nalu_offset) {
    // 添加空指针检查
    if (data == nullptr) {
        LOG_WARN("find_next_nalu: data is null");
        return false;
    }
    
    // 添加长度检查
    if (length < 4) {
        LOG_WARN("find_next_nalu: length too small: %u", length);
        return false;
    }
    
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

inline bool get_nalu_type(const uint8_t *data, const uint32_t& length, uint32_t& nalu_type) {
    /*
        | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |
        |---|---|---|---|---|---|---|---|
        | F | R | R | T | T | T | T | T |
        F: forbidden_zero_bit
        R: ref_idc
        T: type
        
        常见NALU类型:
        0: 未定义
        1: 非IDR图像的Slice
        2: 数据分区A
        3: 数据分区B
        4: 数据分区C
        5: IDR图像的Slice
        6: SEI (补充增强信息)
        7: SPS (序列参数集)
        8: PPS (图像参数集)
        9: 访问单元分隔符
    */
    // 添加空指针检查
    if (data == nullptr) {
        LOG_WARN("get_nalu_type: data is null");
        return false;
    }
    
    if (length < 1) {
        LOG_WARN("get_nalu_type: length too small: %u", length);
        return false;
    }
    nalu_type = data[0] & 0x1F; // 0x1F: 0001 1111, 取低5位
    return true;
}

inline bool get_sei_type(const uint8_t *data, const uint32_t& length, uint32_t& sei_type) {
    // 添加空指针检查
    if (data == nullptr) {
        LOG_WARN("get_sei_type: data is null");
        return false;
    }
    
    if (length < 2) {
        LOG_WARN("get_sei_type: length too small: %u", length);
        return false;
    }
    // data[0] is nalu_type, data[1] is sei_type (payload type in H264)
    sei_type = data[1];
    return true;
}

inline bool get_ext_sei_offset(const uint8_t *data, const uint32_t& length, uint32_t& sei_offset) {
    // 添加空指针检查 - 防止网络断连导致的空指针传递
    if (data == nullptr) {
        LOG_WARN("get_ext_sei_offset: data is null, possibly due to network issues");
        return false;
    }
    
    // 添加长度检查 - 防止空数据传递
    if (length == 0) {
        LOG_WARN("get_ext_sei_offset: length is 0, possibly due to network issues");
        return false;
    }
    
    do {
        // DJI 的SEI都在包头，所以只需要解析第一个
        uint32_t nalu_offset = 0;
        if (!find_next_nalu(data, length, nalu_offset)) {
            LOG_ERROR("find_next_nalu failed");
            break;
        }
        if (nalu_offset >= length) {
            LOG_ERROR("nalu_offset out of range");
            break;
        }
        uint32_t nalu_type = 0;
        if (!get_nalu_type(data + nalu_offset, length - nalu_offset, nalu_type)) {
            LOG_ERROR("get_nalu_type failed");
            break;
        }
        if (nalu_type != SEI_NALU_TYPE) {
            break;
        }
        uint32_t sei_type = 0;
        if (!get_sei_type(data + nalu_offset, length - nalu_offset, sei_type)) {
            LOG_ERROR("get_sei_type failed");
            break;
        }
        if (sei_type == EXT_SEI_TYPE_1 || sei_type == EXT_SEI_TYPE_2 || sei_type == EXT_SEI_TYPE_VOLC || sei_type == EXT_SEI_TYPE_AGORA) {
            // skip nalu_type and sei_type(payload type)
            sei_offset = nalu_offset + 2;
            return true;
        }
    } while (false);
    return false;
}

inline std::shared_ptr<drc_sdk::ByteBuffer> remove_emulation_prevention_bytes(const uint8_t *data, const uint32_t& length) {
    // 添加空指针检查
    if (data == nullptr) {
        LOG_WARN("remove_emulation_prevention_bytes: data is null");
        return nullptr;
    }
    
    // Annex B 规范: 若出现 0x00 0x00 0x03 字节序列，需要把 0x03 删除
    std::shared_ptr<drc_sdk::ByteBuffer> result = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
    for (int i = 0; i < length; i++) {
        // 添加边界检查，防止数组越界
        if (data[i] == 3 && 
            i >= 2 &&  // 确保有足够的前置字节
            data[i - 1] == 0 && 
            data[i - 2] == 0 && 
            i + 1 < length &&  // 确保有足够的后置字节
            (data[i + 1] == 0 || data[i + 1] == 1 || data[i + 1] == 2 || data[i + 1] == 3)) {
            continue;
        }
        result->Write(data[i]);
    }
    return result;
}

// 设备端已经将非业务SEI移除，所以只会存在一个SEI NALU单元
inline bool get_sei_buffer(const uint8_t *data, const uint32_t& length, std::shared_ptr<drc_sdk::ByteBuffer>& sei_buffer) {
    // 添加空指针检查
    if (data == nullptr) {
        LOG_WARN("get_sei_buffer: data is null, possibly due to network issues");
        return false;
    }
    
    // 添加长度检查
    if (length == 0) {
        LOG_WARN("get_sei_buffer: length is 0, possibly due to network issues");
        return false;
    }
    
    uint32_t sei_offset = 0;
    if (!get_ext_sei_offset(data, length, sei_offset)) {
        return false;
    }
    
    // 检查sei_offset是否超出范围
    if (sei_offset >= length) {
        LOG_ERROR("get_sei_buffer: sei_offset out of range, sei_offset: %u, length: %u", sei_offset, length);
        return false;
    }
    
    uint32_t sei_raw_data_length = data[sei_offset];
    // 连续 0xFF 累加 + 最后一个 < 0xFF 的值，表示 sei_len， 符合 H264 规范
    // 0xFF 0xFF 0x03 -> sei_len = 255 + 255 + 3 = 513
    while (data[sei_offset] == 0xFF) {
        sei_offset++;
        if (sei_offset >= length) {
            LOG_ERROR("get_sei_buffer: incomplete length encoding, sei_offset: %u, length: %u", sei_offset, length);
            return false;
        }
        sei_raw_data_length += data[sei_offset];
    }
    if (sei_raw_data_length == 0) {
        LOG_ERROR("get_sei_buffer_length failed, sei_offset: %d, length: %d", sei_offset, length);
        return false;
    }
    sei_offset++;
    
    // 检查数据是否足够
    if (sei_offset + sei_raw_data_length > length) {
        LOG_ERROR("get_sei_buffer: insufficient data for payload, sei_offset: %u, payload_length: %u, total_length: %u", 
                  sei_offset, sei_raw_data_length, length);
        return false;
    }
    
    sei_buffer = remove_emulation_prevention_bytes(data + sei_offset, sei_raw_data_length);
    return true;
}

} // namespace flight_hub_sdk