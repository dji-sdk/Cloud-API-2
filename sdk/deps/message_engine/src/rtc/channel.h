// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/async/executor.h"
#include <functional>
#include "api/defines.h"
#include "api/i_channel.h"
#include <vector>
#include <string>
#include <mutex>

namespace message_engine {
/*
* channel 表征业务逻辑层的管道，例如一台飞机直播和信令会加入不同房间。
* 后续DRTC方案如果跟目前的抽象定义不一致，需要在下层进行抹平
*/
class RtcChannelManager;

class Channel : public IChannel {
public:
    friend RtcChannelManager;
    explicit Channel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    Channel() = default;
    virtual ~Channel() = default;

public:
    [[nodiscard]] ChannelInfo GetChannelInfo() const override;
    [[nodiscard]] std::string GetToken() const override;
    [[nodiscard]] ChannelStatus GetChannelStatus() const override;
    [[nodiscard]] std::vector<std::string> GetUsersList() const override;
    [[nodiscard]] ChannelLifeCycleInfo GetChannelLifeCycleInfo() const override;

public:
    void SetUsersListUpdateCallback(std::function<void(std::vector<std::string>users_list)>&& callback) override;
    void SetTokenPrivilegeWillExpireCallback(std::function<void()>&& callback) override;
    void SetLicenseWillExpireWarningCallback(std::function<void(const int &left_days)>&& callback) override;
    void SetGlobalErrorCallback(std::function<void(const std::string& error, const std::string& message)>&& callback) override;

protected:
    ChannelStatus status_ = ChannelStatus::OFFLINE;
    ChannelInfo channel_info_;
    ChannelLifeCycleInfo life_cycle_info_;
    std::string token_;
    std::vector<std::string> users_list_;
    std::shared_ptr<::drc_sdk::Executor> working_executor_;

    std::vector<std::function<void(std::vector<std::string>users_list)>> users_list_update_callbacks_;
    std::function<void()> token_privilege_will_expire_callback_;
    std::function<void(const int &left_days)> license_will_expire_warning_callback_;
    std::function<void(const std::string& error, const std::string& message)> global_error_callback_;

    std::mutex users_list_update_callbacks_mutex_;
};

} // namespace message_engine
