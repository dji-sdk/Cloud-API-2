// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 机载 AI 模块接口，提供 AI 识别开关/过滤/阈值配置、模型选择及 AI 数据订阅。
class IOnboardAiModule {
public:
    virtual ~IOnboardAiModule() = default;

    /// @brief 订阅机载 AI 数据
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_ai_info_push
    /// @endinternal
    virtual int listenAIDataInfo(const std::function<void(const AiDataInfo& state)>& callback) = 0;
    /// @brief 取消订阅机载 AI 数据
    /// @param listen_id 监听 ID
    virtual void cancelListenAIDataInfo(int listen_id) = 0;

    /// @brief 设置 AI 识别开关
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_identify_set
    /// @endinternal
    virtual void AIIdentifySet(const AiIdentifySetParam& param, const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 设置 AI 识别过滤；2.0 须填 index + light_type
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_identify_filter_set
    /// @endinternal
    virtual void AIIdentifyFilterSet(const AiIdentifyFilterSetParam& param, const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 设置 AI 识别分数阈值；2.0 须填 index + light_type
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_identify_score_set
    /// @endinternal
    virtual void AIIdentifyScoreSet(const AiIdentifyScoreSetParam& param, const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 设置 AI 识别分数模式；2.0 须填 index + light_type
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_identify_score_mode_set
    /// @endinternal
    virtual void AIIdentifyScoreModeSet(const AiIdentifyScoreModeSetParam& param, const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 重置 AI 识别分数；2.0 须填 index + light_type
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_identify_score_reset
    /// @endinternal
    virtual void AIIdentifyScoreReset(const AiIdentifyScoreResetParam& param, const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 选择 AI 模型
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_model_select
    /// @endinternal
    virtual void AIModelSelect(const AiModelSelectParam& param, const std::function<void(FlightHubError)>& callback) = 0;

    // ══════════════════════════════════════════════
    // AI Spotlight Zoom (Tracking) DRC
    // ══════════════════════════════════════════════

    /// @brief AI 跟随开关
    /// @param on 开关（0=关 1=开）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_spotlight_zoom_set
    /// @endinternal
    virtual void DrcAISpotlightZoomSet(int on, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief AI 框选目标跟随
    /// @param params 框选参数（目标中心坐标及宽高）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_spotlight_zoom_select
    /// @endinternal
    virtual void DrcAISpotlightZoomSelect(const AiSpotlightZoomSelectParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief AI 框选目标跟随确认
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_spotlight_zoom_confirm
    /// @endinternal
    virtual void DrcAISpotlightZoomConfirm(const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 停止 AI 目标跟随
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_spotlight_zoom_stop
    /// @endinternal
    virtual void DrcAISpotlightZoomStop(const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief AI 识别目标跟随（按目标索引）
    /// @param target_index 目标索引
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_spotlight_zoom_track
    /// @endinternal
    virtual void DrcAISpotlightZoomTrack(int target_index, const std::function<void(FlightHubError)>& callback) = 0;

    // ══════════════════════════════════════════════
    // AI Alarm DRC
    // ══════════════════════════════════════════════

    /// @brief 开启 AI 主动告警
    /// @param params 多码流告警配置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_alarm_start
    /// @endinternal
    virtual void DrcAIAlarmStart(const AiAlarmStartParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 关闭 AI 主动告警
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_alarm_stop
    /// @endinternal
    virtual void DrcAIAlarmStop(const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 配置 AI 模型
    /// @param params AI 模型配置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_ai_models_config
    /// @endinternal
    virtual void DrcAIModelsConfig(const AiModelsConfigParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 订阅 AI 主动告警状态推送
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_ai_alarm_state_push
    /// @endinternal
    virtual int listenDrcAIAlarmState(const std::function<void(const AiAlarmStateInfo&)>& callback) = 0;

    /// @brief 取消订阅 AI 主动告警状态推送
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcAIAlarmState(int listen_id) = 0;
};

} // namespace flight_hub_sdk
