// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// DRC 模块接口，提供 DRC 链路连接/断开及链路状态订阅。
class IDrcModule {
public:
    virtual ~IDrcModule() = default;

    /// @brief 订阅 DRC 链路状态
    /// @return 监听 ID，用于取消订阅
    virtual int listenLinkStatus(const std::function<void(const LinkStatus& status)>& callback) = 0;
    /// @brief 取消订阅 DRC 链路状态
    /// @param listen_id 监听 ID
    virtual void cancelListenLinkStatus(int listen_id) = 0;

    /// @brief 连接 DRC 链路
    virtual void connectDrcLink(const std::function<void(FlightHubSdkErrorCode)>& callback) = 0;
    /// @brief 断开 DRC 链路
    virtual void disconnectDrcLink(const std::function<void(FlightHubSdkErrorCode)>& callback) = 0;

    // ══════════════════════════════════════════════
    // DRC Link Auxiliary
    // ══════════════════════════════════════════════

    /// @brief RTC 网络质量测试
    /// @param params 质量测试参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_rtc_quality_test
    /// @endinternal
    virtual void drcRtcQualityTest(const RtcQualityTestParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 订阅 DRC 链路延迟信息推送
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: delay_info_push
    /// @endinternal
    virtual int listenDrcDelayInfo(const std::function<void(const DrcDelayInfo&)>& callback) = 0;

    /// @brief 取消订阅 DRC 链路延迟信息推送
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcDelayInfo(int listen_id) = 0;


    /// @brief 订阅 DRC 倒计时信息推送
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_count_down_info_push
    /// @endinternal
    virtual int listenDrcCountDownInfo(const std::function<void(const DrcCountDownInfo&)>& callback) = 0;

    /// @brief 取消订阅 DRC 倒计时信息推送
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcCountDownInfo(int listen_id) = 0;
};

} // namespace flight_hub_sdk
