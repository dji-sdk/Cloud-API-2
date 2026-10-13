// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include "defines.h"
#include <vector>
#include <string>


namespace message_engine {

class ChannelInfo {
public:
    /// For volc
    ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& app_id, const std::string& channel_name, const std::string& user_id, const std::string& log_path);
    /// For drtc
    ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& app_id, const std::string& channel_name, const std::string& room_id, const std::string& client_id, const std::string& config_path);
    /// For agora
    ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& channel_name, const std::string& user_id, const std::string& log_path);
    /// For Mrtc
    ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& channel_name, const std::string& protocol_name, const std::string& url, std::string serverIP, std::string serverPort, std::string serverID, std::string agentID, std::string agentPassword, std::string localPort, std::string channel);
    /// For WebRtc
    ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& channel_name, const std::string& url);

    ChannelInfo();
    ~ChannelInfo() = default;
    bool operator==(const ChannelInfo& info) const {
        return type_ == info.type_ &&
               provider_type_ == info.provider_type_ &&
               app_id_ == info.app_id_ &&
               channel_name_ == info.channel_name_ &&
               user_id_ == info.user_id_ &&
               room_id_ == info.room_id_ &&
               payload_index_ == info.payload_index_ &&
               country_code_ == info.country_code_ &&
               log_path_ == info.log_path_ &&
               client_id_ == info.client_id_ &&
               url_ == info.url_ &&
               config_path_ == info.config_path_ &&
               stream_tag_ == info.stream_tag_ &&
               drtc_stream_id_ == info.drtc_stream_id_;
    }
    ChannelInfo& operator=(const ChannelInfo& info) {
        if (&info == this) {
            return *this;
        }
        type_ = info.type_;
        provider_type_ = info.provider_type_;
        app_id_ = info.app_id_;
        channel_name_ = info.channel_name_;
        room_id_ = info.room_id_;
        user_id_ = info.user_id_;
        client_id_ = info.client_id_;
        payload_index_ = info.payload_index_;
        log_path_ = info.log_path_;
        country_code_ = info.country_code_;
        url_ = info.url_;
        config_path_ = info.config_path_;
        stream_tag_ = info.stream_tag_;
        drtc_stream_id_ = info.drtc_stream_id_;
        return *this;
    }
public:
    ChannelType type_;
    ChannelProviderType provider_type_;
    std::string app_id_;
    std::string channel_name_;  // Volc/Agora:channel_name_ 与 room_id_相同; Drtc: channel_name_ 独立
    std::string room_id_;
    std::string user_id_;       // For Volc/Agora
    std::string client_id_;     // Only for Drtc
    std::string log_path_;      // For Volc/Agora

    std::string config_path_;    // Only for Drtc

    std::string payload_index_;  // live channel 特有, 用于区分不同 payload
    std::string country_code_;   // agora 特有, 用于区分不同 国家码

    std::string protocol_name_; // mrtc 特有，用于区分不同直播协议

    std::string url_;      // webrtc/mrtc 特有, 用于区分不同 直播 url

    std::string stream_tag_;  // 流标识标签，格式: device_sn_payload_index_video_id，用于日志过滤和定位

    std::string drtc_stream_id_;  // DRTC: URL 中的 stream_id，用于 Subscribe/收帧过滤；RtcChannelManager key 用 channel_name_(stream_tag)

    // GB28181 参数
    std::string serverIP;
    std::string serverPort;
    std::string serverID;
    std::string agentID;
    std::string agentPassword;
    std::string localPort;
    std::string channel;
};

class IChannel {
public:
    virtual ~IChannel() = default;
public:
    virtual ChannelInfo GetChannelInfo() const = 0;
    virtual std::string GetToken() const = 0;
    virtual ChannelStatus GetChannelStatus() const = 0;
    virtual std::vector<std::string> GetUsersList() const = 0;
    virtual ChannelLifeCycleInfo GetChannelLifeCycleInfo() const = 0;
public:
    virtual void SetUsersListUpdateCallback(std::function<void(std::vector<std::string>users_list)>&& callback) = 0;
    virtual void SetTokenPrivilegeWillExpireCallback(std::function<void()>&& callback) = 0;
    virtual void SetLicenseWillExpireWarningCallback(std::function<void(const int &left_days)>&& callback) = 0;
    virtual void SetGlobalErrorCallback(std::function<void(const std::string& error, const std::string& message)>&& callback) = 0;

public:
    virtual bool Join(const JoinChannelCallback& callback) = 0;
    virtual bool Leave() = 0;
    virtual bool RenewToken(const std::string &token) = 0;
};

} // namespace message_engine

