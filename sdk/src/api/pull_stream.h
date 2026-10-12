// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <vector>
#include <cstdint>
#include <string>
#include "message.h"

namespace flight_hub_sdk {

/// 拉流模块接口，负责从指定地址拉取 WebRTC 流，不包含业务逻辑。
class IPullStreamModule {
public:
    virtual ~IPullStreamModule() = default;

    /// @brief 开始拉流
    /// @param stream_url 拉流地址（支持 WHEP 协议）
    /// @param callback 启动结果回调
    ///
    /// @internal
    /// 链路: SDK 内部 | 通过 RTC SDK 建立 WebRTC 连接并拉流
    /// @endinternal
    virtual void start(const std::string& stream_url,
                      const std::function<void(FlightHubSdkErrorCode result)>& callback) = 0;

    /// @brief 停止拉流
    ///
    /// @internal
    /// 链路: SDK 内部 | 断开 WebRTC 连接
    /// @endinternal
    virtual void stop() = 0;

    /// @brief 订阅流视频帧数据
    /// @param callback 帧数据回调，每帧触发一次
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | RTC SDK 视频帧回调
    /// @endinternal
    virtual int subscribeStream(const std::function<void(const std::vector<uint8_t>& data,
                                                        const FrameInfo& frame_info)>& callback) = 0;

    /// @brief 取消订阅流视频帧数据
    /// @param listen_id 订阅时返回的监听 ID
    virtual void unSubscribeStream(int listen_id) = 0;

    /// @brief 设置错误回调
    /// @param callback 错误回调（error_code + error_message）
    virtual void setErrorCallback(const std::function<void(int error_code,
                                                          const std::string& error_message)>& callback) = 0;

    /// @brief 查询是否正在拉流
    /// @return true 表示正在拉流
    virtual bool isStreaming() const = 0;

    /// @brief 获取当前流信息
    /// @return 流信息字符串
    virtual std::string getStreamInfo() const = 0;
};

} // namespace flight_hub_sdk
