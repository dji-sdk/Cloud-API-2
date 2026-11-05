// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
// 降落任务状态: 设备主动推送, 无需回包
// 北向: thing/product/{gateway_sn}/drc/up   method: landing_mission_state
//
// ============================ 拼写说明: cover_precent ============================
// "cover_precent" 不是笔误, 而是协议设计如此 —— 云端下发的 JSON key 原文即为
// precent(而非 percent)。此处是全链路唯一与线上报文直接绑定的地方:
// AIGC_JSON_HELPER 按 C++ 成员名匹配 JSON key, 且本仓库未使用
// AIGC_JSON_HELPER_RENAME, 因此成员名必须与协议逐字一致。
// 对外 API(flight_hub_sdk::DrcLandingMissionState)已更正为 cover_percent,
// 转换点在 FlightCtrlMgr::onReceiveLandingMissionState。本层保持 precent 不动。
// ==============================================================================
struct DrcLandingMissionStateNorthboundMsg {
    // 降落状态枚举:
    //   0 = 初始空闲
    //   1 = 等待参数同步
    //   2 = 航向对齐
    //   3 = 精准降落(有水平源)
    //   4 = 无源降落(无水平源, 继续下降)
    //   5 = 悬停微调(有水平源)
    //   6 = 纯悬停(无水平源)
    //   7 = 备降(有源)
    //   8 = 备降(无源)
    //   9 = 备降(用户 RC 接管)
    int landing_state = 0;     // 降落状态, 取值见上方枚举
    int pause_reason = 0;      // 悬停原因
    int cover_precent = 0;     // 舱盖开合百分比, 未开到 100 说明开仓失败。拼写见上方说明: 协议原文即 precent, 勿改
    int cover_error_code = 0;  // 开仓错误码, 0 表示无异常

    AIGC_JSON_HELPER(landing_state, pause_reason, cover_precent, cover_error_code)
};
///------ 用户自定义数据结构 end ------

class DrcLandingMissionStateNorthbound final : public MessageV2 {
public:
    DrcLandingMissionStateNorthbound();
    explicit DrcLandingMissionStateNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcLandingMissionStateNorthboundMsg msg;
};

}
