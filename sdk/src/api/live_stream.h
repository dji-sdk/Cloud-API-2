// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <vector>
#include <cstdint>
#include "message.h"

namespace flight_hub_sdk {

/// 直播模块接口，提供直播流的启停、视频帧订阅、SEI 数据解析、画质设置等功能。
class ILiveModule {
public:
    virtual ~ILiveModule() = default;

    /// @brief 开始直播拉流
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-live/api/v2/stream/start（获取 RTC 频道信息后通过 RTC SDK 拉流）
    /// @endinternal
    virtual void start(const std::function<void(FlightHubError result)>& callback) = 0;

    /// @brief 获取直播拉流地址（不自动拉流）
    ///
    /// 与 start() 不同，本方法只调用后端接口获取拉流地址（LiveStreamStartResult），
    /// 不进行 RTC 通道建立和自动拉流。用户可自行使用返回的 url 进行 WHEP/RTC 播放。
    ///
    /// @attention 由于 SDK 内部未建立 RTC 通道，调用本方法后以下接口将不起作用：
    ///            stop()、subscribeLiveStream()、以及 SEI 订阅相关接口。
    ///
    /// @param callback 结果回调，成功时返回 LiveStreamStartResult
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-live/api/v2/stream/start
    /// @endinternal
    virtual void startWithURL(const std::function<void(FlightHubError err, const LiveStreamStartResult& result)>& callback) = 0;

    /// @brief 停止直播拉流
    ///
    /// @internal
    /// 链路: SDK 内部 | 断开 RTC 频道连接
    /// @endinternal
    virtual void stop() = 0;

    /// @brief 停止设备推流（后端停流）
    ///
    /// 与 stop() 不同，本方法调用后端接口通知服务端停止设备推流，
    /// 适用于 startWithURL 场景下用户自行停止推流。
    ///
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-live/api/v1/stream/stop
    /// @endinternal
    virtual void stopWithURL(const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 订阅直播视频帧数据
    /// @param callback 帧数据回调，每帧触发一次
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | RTC SDK 视频帧回调
    /// @endinternal
    virtual int subscribeLiveStream(const std::function<void(const std::vector<uint8_t>& data, const FrameInfo& frame_info)>& callback) = 0;

    /// @brief 取消订阅直播视频帧数据
    /// @param listen_id 订阅时返回的监听 ID
    ///
    /// @internal
    /// 链路: SDK 内部 | 移除帧数据回调
    /// @endinternal
    virtual void unSubscribeLiveStream(int listen_id) = 0;

    /// @brief 订阅 AI 检测 SEI 信息（目标检测框）
    /// @param callback AI SEI 信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 AI 检测数据
    /// @endinternal
    virtual int subscribeAiSeiInfo(const std::function<void(const AiSeiInfo& ai_sei_info)>& callback) = 0;

    /// @brief 取消订阅 AI 检测 SEI 信息
    /// @param listen_id 订阅时返回的监听 ID
    virtual void unSubscribeAiSeiInfo(int listen_id) = 0;

    /// @brief 订阅 AR SEI 信息（相机姿态与位置）
    /// @param callback AR SEI 信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 AR 数据
    /// @endinternal
    virtual int subscribeArSeiInfo(const std::function<void(const ArSeiInfo& ar_sei_info)>& callback) = 0;

    /// @brief 取消订阅 AR SEI 信息
    /// @param listen_id 订阅时返回的监听 ID
    virtual void unSubscribeArSeiInfo(int listen_id) = 0;

    /// @brief 订阅 FOV SEI 信息（画幅尺寸与焦距，见 FovSeiInfo）
    /// @param callback FOV SEI 信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 FOV 数据
    /// @endinternal
    virtual int subscribeFovSeiInfo(const std::function<void(const FovSeiInfo& fov_sei_info)>& callback) = 0;

    /// @brief 取消订阅 FOV SEI 信息
    /// @param listen_id 订阅时返回的监听 ID
    virtual void unSubscribeFovSeiInfo(int listen_id) = 0;

    /// @brief 订阅智能辅助 SEI 信息（自动朝向等）
    /// @param callback 智能辅助 SEI 信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 Assistant 数据
    /// @endinternal
    virtual int subscribeAssistantSeiInfo(const std::function<void(const AssistantSeiInfo& assistant_sei_info)>& callback) = 0;

    /// @brief 取消订阅智能辅助 SEI 信息
    /// @param listen_id 订阅时返回的监听 ID
    virtual void unSubscribeAssistantSeiInfo(int listen_id) = 0;

    /// @brief 订阅 EIS（电子防抖）SEI 信息（裁剪区域坐标）
    /// @param callback EIS SEI 信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 EIS 数据
    /// @endinternal
    virtual int subscribeEisSeiInfo(const std::function<void(const EisSeiInfo& eis_sei_info)>& callback) = 0;

    /// @brief 取消订阅 EIS SEI 信息
    /// @param listen_id 订阅时返回的监听 ID
    virtual void unSubscribeEisSeiInfo(int listen_id) = 0;

    /// @brief 设置直播画质
    /// @param params 画质设置参数（设备 SN、摄像头、视频流标识、画质等级）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-live/api/v1/stream/set-quality
    /// @endinternal
    virtual void setLiveQuality(const LiveQualitySettingParams& params, const std::function<void(FlightHubError err)>& callback) = 0;
};

} // namespace flight_hub_sdk