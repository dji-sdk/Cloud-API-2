// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include <cstdint>
#include <memory>

namespace flight_hub_sdk {

/**
 * EA340 等机型：DRTC 透传完整 H265 Annex-B NAL，业务 SEI 在 ext(0xF5) / lvstream(0xFF01) 内。
 * 对齐 Web live_stream_sei_parser.ts FULL_NAL 路径。
 */
class FullNalSeiParser {
public:
    /** 是否像完整 NAL SEI（含 SEI NAL + 0xF5 或 0xFF01） */
    static bool LooksLikeFullNalSei(const uint8_t* data, uint32_t length);

    /**
     * 从完整 NAL 帧提取 TLV bundle（去 EPB 后的 ext payload 拼接）。
     * 成功时 tlv_buffer 可直接交给 LiveModuleImpl::processSei。
     */
    static bool TryExtractTlvBundle(const uint8_t* data, uint32_t length,
                                    std::shared_ptr<drc_sdk::ByteBuffer>& tlv_buffer);
};

}  // namespace flight_hub_sdk
