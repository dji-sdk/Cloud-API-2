// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <vector>
#include <string>

namespace flight_hub_sdk {
    /// 自定义 SEI（Supplemental Enhancement Information）处理模块，
    /// 用于在 H.264 视频帧中编码/解码自定义数据（检测框、多边形、文本叠加等）。
    class ICustomSEIModule {
    public:
        virtual ~ICustomSEIModule() = default;

        /// @brief 将自定义 SEI 数据编码到 H.264 帧中
        /// @param frame_data 原始 H.264 帧数据
        /// @param custom_sei_data 待编码的 SEI 数据项列表
        /// @param output_frame_data 编码后的帧数据（含 SEI）
        /// @return 编码是否成功
        ///
        /// @internal
        /// 链路: SDK 内部 | 在视频帧 NAL 单元前插入 SEI NAL
        /// @endinternal
        virtual bool CustomSEIEncode(const std::vector<int>& frame_data,
                                   const std::vector<SeiItem>& custom_sei_data,
                                   std::vector<int>& output_frame_data) = 0;

        /// @brief 从 H.264 帧中解码出自定义 SEI 数据
        /// @param frame_data H.264 帧数据（含 SEI）
        /// @param output_header 解码出的 SEI 头部信息
        /// @param output_sei_data 解码出的 SEI 数据项列表
        /// @return 解码是否成功
        ///
        /// @internal
        /// 链路: SDK 内部 | 解析帧中的 SEI NAL 单元
        /// @endinternal
        virtual bool CustomSEIDecode(const std::vector<int>& frame_data,
                                   SeiHeader& output_header,
                                   std::vector<SeiItem>& output_sei_data) = 0;
    };

}