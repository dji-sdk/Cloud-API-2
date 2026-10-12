// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// PSDK 控制模块接口，提供 PSDK 设备管理、喊话器/探照灯/抛投器等负载控制。
class IPsdkControlModule {
public:
    virtual ~IPsdkControlModule() = default;

    /// @brief 订阅 PSDK 设备列表
    /// @param callback PSDK 设备列表回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_psdk_ui_resource (聚合去重后按 PSDK 索引透传)
    /// @endinternal
    virtual int listenDrcPsdkDeviceList(const std::function<void(const PsdkDeviceList& psdk_device_list)>& callback) = 0;

    /// @brief 取消订阅 PSDK 设备列表
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcPsdkDeviceList(int listen_id) = 0;

    /// @brief 订阅 PSDK 状态摘要
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback PSDK 状态摘要回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_psdk_state_info (field: 通用 PSDK 状态摘要)
    /// @endinternal
    virtual int listenDrcPsdkStateSummaryInfo(int psdk_index, const std::function<void(PsdkStateSummaryInfo psdk_device_list)>& callback) = 0;

    /// @brief 取消订阅 PSDK 状态摘要
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcPsdkStateSummaryInfo(int psdk_index, int listen_id) = 0;

    /// @brief 订阅 PSDK 喊话器状态
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback PSDK 喊话器状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_psdk_state_info (field: 喊话器子集)
    /// @endinternal
    virtual int listenDrcPsdkStateSpeakerInfo(int psdk_index, const std::function<void(PsdkStateSpeakerInfo speaker_info)>& callback) = 0;

    /// @brief 取消订阅 PSDK 喊话器状态
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcPsdkStateSpeakerInfo(int psdk_index, int listen_id) = 0;

    /// @brief 订阅 PSDK 探照灯状态
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback PSDK 探照灯状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_psdk_state_info (field: 探照灯子集)
    /// @endinternal
    virtual int listenDrcPsdkStateSearchlightInfo(int psdk_index, const std::function<void(PsdkStateSearchlightInfo searchlight_info)>& callback) = 0;

    /// @brief 取消订阅 PSDK 探照灯状态
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcPsdkStateSearchlightInfo(int psdk_index, int listen_id) = 0;

    /// @brief 设置探照灯模式
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param mode 探照灯模式
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_light_mode_set
    /// @endinternal
    virtual void DrcSearchlightModeSet(int psdk_index, int mode, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置探照灯亮度
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param brightness 亮度值
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_light_brightness_set
    /// @endinternal
    virtual void DrcSearchlightBrightnessSet(int psdk_index, int brightness, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 探照灯校准
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_light_calibration
    /// @endinternal
    virtual void DrcSearchlightCalibration(int psdk_index, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 探照灯微调设置
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param position 微调位置
    /// @param value 微调值
    /// @param saved 是否保存
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_light_fine_tuning_set
    /// @endinternal
    virtual void DrcSearchlightFineTuningSet(int psdk_index, int position, double value, bool saved, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置探照灯广域模式
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param wide_field_mode 广域模式
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_light_wide_field_mode_set
    /// @endinternal
    virtual void DrcSearchlightWideFieldModeSet(int psdk_index, int wide_field_mode, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置探照灯云台控制
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param light_gimbal_control 云台控制值
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_light_light_gimbal_control_set
    /// @endinternal
    virtual void DrcSearchlightLightGimbalControlSet(int psdk_index, int light_gimbal_control, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置探照灯类型
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param type 探照灯类型
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_light_type_set
    /// @endinternal
    virtual void DrcSearchlightTypeSet(int psdk_index, int type, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 订阅 PSDK 喊话器播放进度
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback PSDK 喊话器播放进度回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_speaker_play_progress
    /// @endinternal
    virtual int listenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, const std::function<void(PsdkSpeakerPlayProgressInfo play_progress_info)>& callback) = 0;

    /// @brief 取消订阅 PSDK 喊话器播放进度
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcPsdkSpeakerPlayProgressInfo(int psdk_index, int listen_id) = 0;

    /// @brief 订阅 PSDK 浮窗文本
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback PSDK 浮窗文本回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_psdk_floating_window_text
    /// @endinternal
    virtual int listenDrcPsdkFloatingWindowTextInfo(int psdk_index, const std::function<void(PsdkFloatingWindowTextInfo floating_window_text_info)>& callback) = 0;

    /// @brief 取消订阅 PSDK 浮窗文本
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcPsdkFloatingWindowTextInfo(int psdk_index, int listen_id) = 0;

    /// @brief 开始音频喊话
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param audio_play_info 音频播放信息
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_audio_play_start
    /// @endinternal
    virtual void DrcSpeakerAudioPlayStart(int psdk_index, const PsdkSpeakerAudioPlayInfo& audio_play_info, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 开始 TTS 喊话
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param tts_play_info TTS 播放信息
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_tts_play_start
    /// @endinternal
    virtual void DrcSpeakerTTSPlayStart(int psdk_index, const PsdkSpeakerTTSPlayInfo& tts_play_info, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 开始实时喊话
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_real_time_start
    /// @endinternal
    virtual void DrcSpeakerRealTimeStart(int psdk_index, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 停止实时喊话
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_real_time_stop
    /// @endinternal
    virtual void DrcSpeakerRealTimeStop(int psdk_index, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 重播喊话
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_replay
    /// @endinternal
    virtual void DrcSpeakerReplay(int psdk_index, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 停止喊话播放
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_play_stop
    /// @endinternal
    virtual void DrcSpeakerPlayStop(int psdk_index, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置喊话器播放音量
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param volume 音量值
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_play_volume_set
    /// @endinternal
    virtual void DrcSpeakerPlayVolumeSet(int psdk_index, int volume, const std::function<void(FlightHubError err)> &callback) = 0;

    /// @brief 设置喊话器播放模式
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param play_mode 播放模式
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_play_mode_set
    /// @endinternal
    virtual void DrcSpeakerPlayModeSet(int psdk_index, int play_mode, const std::function<void(FlightHubError err)> &callback) = 0;

    /// @brief 设置 TTS 配置
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param tts_config TTS 配置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_speaker_tts_set
    /// @endinternal
    virtual void DrcSpeakerTTSSet(int psdk_index, const PsdkSpeakerTTSconfig &tts_config, const std::function<void(FlightHubError err)> &callback) = 0;

    /// @brief 设置 Widget 值
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param index Widget 索引
    /// @param value Widget 值
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_psdk_widget_value_set
    /// @endinternal
    virtual void DrcWidgetValueSet(int psdk_index, int index, int value, const std::function<void(FlightHubError err)> &callback) = 0;

    /// @brief 设置 Widget 输入框文本
    /// @param psdk_index PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
    /// @param value 输入框文本内容
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_psdk_input_box_text_set
    /// @endinternal
    virtual void DrcWidgetInputBoxTextSet(int psdk_index, const std::string& value, const std::function<void(FlightHubError err)> &callback) = 0;

    /// @brief 订阅 PSDK 喊话器音频播放进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_psdk_speaker_audio_play_start_progress
    /// @endinternal
    virtual int listenPsdkSpeakerAudioPlayStartProgress(
        const std::function<void(const PsdkSpeakerPlayStartProgress& msg)>& callback) = 0;

    /// @brief 取消订阅 PSDK 喊话器音频播放进度
    /// @param listen_id 监听 ID
    virtual void cancelListenPsdkSpeakerAudioPlayStartProgress(int listen_id) = 0;

    /// @brief 订阅 PSDK 喊话器 TTS 播放进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_psdk_speaker_tts_play_start_progress
    /// @endinternal
    virtual int listenPsdkSpeakerTtsPlayStartProgress(
        const std::function<void(const PsdkSpeakerPlayStartProgress& msg)>& callback) = 0;

    /// @brief 取消订阅 PSDK 喊话器 TTS 播放进度
    /// @param listen_id 监听 ID
    virtual void cancelListenPsdkSpeakerTtsPlayStartProgress(int listen_id) = 0;

    /// @brief 订阅 PSDK UI 资源
    /// @param callback PSDK UI 资源回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_psdk_ui_resource
    /// @endinternal
    virtual int listenDronePsdkUiResource(
        const std::function<void(const DronePsdkUiResource& msg)>& callback) = 0;

    /// @brief 取消订阅 PSDK UI 资源
    /// @param listen_id 监听 ID
    virtual void cancelListenDronePsdkUiResource(int listen_id) = 0;

};

} // namespace flight_hub_sdk
