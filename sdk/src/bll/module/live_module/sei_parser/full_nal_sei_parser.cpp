// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "full_nal_sei_parser.h"
#include "live_utils.h"
#include "base/common/logger.h"
#include <vector>

namespace flight_hub_sdk {

namespace {

constexpr uint8_t kExtSeiType = 0xF5;
constexpr uint8_t kLvstreamSeiTypeHigh = 0xFF;
constexpr uint8_t kLvstreamSeiTypeLow = 0x01;
constexpr uint32_t kNalStartCodeLen = 4;
constexpr uint8_t kH264SeiNalType = 0x06;

struct SeiLengthInfo {
    uint32_t sei_len = 0;
    uint32_t bytes_consumed = 0;
};

SeiLengthInfo ReadSeiLength(const uint8_t* data, uint32_t length, uint32_t start_index) {
    SeiLengthInfo info;
    uint32_t i = start_index;
    while (i < length && data[i] == 0xFF) {
        info.sei_len += 255;
        i++;
    }
    if (i < length) {
        info.sei_len += data[i];
        i++;
    }
    info.bytes_consumed = i - start_index;
    return info;
}

std::vector<uint32_t> FindNalUnitOffsets(const uint8_t* data, uint32_t length) {
    std::vector<uint32_t> offsets;
    if (length < 4) {
        return offsets;
    }
    for (uint32_t i = 0; i + 3 < length; ++i) {
        if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
            offsets.push_back(i);
        }
    }
    return offsets;
}

bool IsH264SeiNal(const uint8_t* nal_unit, uint32_t nal_length) {
    if (nal_length < kNalStartCodeLen + 1) {
        return false;
    }
    return (nal_unit[kNalStartCodeLen] & 0x1F) == kH264SeiNalType;
}

bool IsSeiNalUnit(const uint8_t* nal_unit, uint32_t nal_length) {
    if (nal_length < kNalStartCodeLen + 1) {
        return false;
    }
    const uint8_t b0 = nal_unit[kNalStartCodeLen];
    if ((b0 & 0x1F) == kH264SeiNalType) {
        return true;
    }
    const uint8_t h265_type = (b0 >> 1) & 0x3F;
    return h265_type == 39 || h265_type == 40;
}

uint32_t GetSeiTypeOffset(const uint8_t* nal_unit, uint32_t nal_length) {
    return kNalStartCodeLen + (IsH264SeiNal(nal_unit, nal_length) ? 1 : 2);
}

bool ExtractExtPayload(const uint8_t* nal_unit, uint32_t nal_length,
                       std::shared_ptr<drc_sdk::ByteBuffer>& payload) {
    if (!IsSeiNalUnit(nal_unit, nal_length)) {
        return false;
    }
    const uint32_t sei_type_offset = GetSeiTypeOffset(nal_unit, nal_length);
    if (sei_type_offset >= nal_length) {
        return false;
    }
    if (nal_unit[sei_type_offset] != kExtSeiType) {
        return false;
    }

    const uint32_t after_type = sei_type_offset + 1;
    const SeiLengthInfo len_info = ReadSeiLength(nal_unit, nal_length, after_type);
    const uint32_t payload_start = after_type + len_info.bytes_consumed;
    const uint32_t payload_end = payload_start + len_info.sei_len;
    if (payload_end > nal_length || len_info.sei_len == 0) {
        return false;
    }

    payload = remove_emulation_prevention_bytes(nal_unit + payload_start, len_info.sei_len);
    return payload != nullptr && payload->size() > 0;
}

bool NalHasFullNalSeiMarker(const uint8_t* nal_unit, uint32_t nal_length) {
    if (!IsSeiNalUnit(nal_unit, nal_length)) {
        return false;
    }
    const uint32_t type_off = GetSeiTypeOffset(nal_unit, nal_length);
    if (type_off >= nal_length) {
        return false;
    }
    const uint8_t t = nal_unit[type_off];
    if (t == kExtSeiType) {
        return true;
    }
    return t == kLvstreamSeiTypeHigh && type_off + 1 < nal_length &&
           nal_unit[type_off + 1] == kLvstreamSeiTypeLow;
}

}  // namespace

bool FullNalSeiParser::LooksLikeFullNalSei(const uint8_t* data, uint32_t length) {
    if (data == nullptr || length < 8) {
        return false;
    }
    const auto offsets = FindNalUnitOffsets(data, length);
    for (const uint32_t nal_start : offsets) {
        if (NalHasFullNalSeiMarker(data + nal_start, length - nal_start)) {
            return true;
        }
    }
    return false;
}

bool FullNalSeiParser::TryExtractTlvBundle(const uint8_t* data, uint32_t length,
                                           std::shared_ptr<drc_sdk::ByteBuffer>& tlv_buffer) {
    if (data == nullptr || length == 0) {
        return false;
    }

    const auto offsets = FindNalUnitOffsets(data, length);
    std::shared_ptr<drc_sdk::ByteBuffer> merged;
    bool found_ext = false;

    for (const uint32_t nal_start : offsets) {
        std::shared_ptr<drc_sdk::ByteBuffer> ext_payload;
        if (!ExtractExtPayload(data + nal_start, length - nal_start, ext_payload)) {
            continue;
        }
        found_ext = true;
        if (!merged) {
            merged = ext_payload;
        } else {
            merged->Write(ext_payload->data(), ext_payload->size());
        }
    }

    if (!found_ext || !merged || merged->size() == 0) {
        return false;
    }

    tlv_buffer = merged;
    return true;
}

}  // namespace flight_hub_sdk
