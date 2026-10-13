// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/live_record_module.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include "message.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class LiveRecordModuleImpl : public ILiveRecordModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit LiveRecordModuleImpl(const LiveInfo& live_info);
    ~LiveRecordModuleImpl() override;

    void startLiveRecord(const std::string& time_zone, const std::function<void(FlightHubError err)>& callback) override;
    void stopLiveRecord(const std::function<void(FlightHubError err)>& callback) override;
    void getDeviceLiveRecords(const std::string& device_sn, const std::function<void(FlightHubError err, const std::vector<LiveRecordItem>& records)>& callback) override;
    int listenRecordState(const std::function<void(const LiveRecordStateInfo& info)>& callback) override;
    void cancelListenRecordState(int listen_id) override;

    void notifyRecordState(const LiveRecordStateInfo& info) override;

    /// 返回本模块绑定的 LiveInfo，用于 NC 全量推送时按 sn/camera/video 匹配后再通知
    const LiveInfo& getLiveInfo() const { return live_info_; }

private:
    LiveInfo live_info_;
    CallbackContainer<LiveRecordStateInfo, false> record_state_callbacks_;
};

}  // namespace flight_hub_sdk
