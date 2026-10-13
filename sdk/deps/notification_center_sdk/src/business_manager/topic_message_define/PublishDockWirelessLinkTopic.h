// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 机场图传质量(wireless_link);
    class DockWirelessLinkMsg {
    public:
        int dongle_num = 0;           // int: 0,1,2 飞机上 dongle 数量;
        int link_state_4g = 0;        // int: 4g 链路连接状态, 0-断开, 1-连接;
        int sdr_link_state = 0;       // int, "sdr链路状态", { 0: 断开, 1: 连接 };
        int link_workmode = 0;        // int, "链路工作模式", { 0: sdr only, 1: 4G融合 };
        int sdr_quality = 0;          // int: 标识sdr信号强弱, 0~5 一共六档;
        int quality_4g = 0;           // int: 标识总体4G信号强弱, 0~5 一共六档，310UI上使用此值, 对应v1协议的 lk_quality;
        int uav_quality_4g = 0;       // int: 标识飞机端4G信号强弱,0~5 一共六档;
        int gnd_quality_4g = 0;       // int: 标识地面端4G信号强弱,0~5 一共六档;
        float sdr_freq_band = 0;      // float: sdr 频段;
        float freq_band_4g = 0;       // float: 4g 频段;

        AIGC_JSON_HELPER(
            dongle_num, link_state_4g, sdr_link_state, link_workmode, sdr_quality,
            quality_4g, uav_quality_4g, gnd_quality_4g, sdr_freq_band, freq_band_4g
        );

        explicit operator DockWirelessLink() const {
            DockWirelessLink dock_wireless_link{};

            dock_wireless_link.dongle_num = dongle_num;
            dock_wireless_link.link_state_4g = link_state_4g;
            dock_wireless_link.sdr_link_state = sdr_link_state;
            dock_wireless_link.link_workmode = link_workmode;
            dock_wireless_link.sdr_quality = sdr_quality;
            dock_wireless_link.quality_4g = quality_4g;
            dock_wireless_link.uav_quality_4g = uav_quality_4g;
            dock_wireless_link.gnd_quality_4g = gnd_quality_4g;
            dock_wireless_link.sdr_freq_band = sdr_freq_band;
            dock_wireless_link.freq_band_4g = freq_band_4g;

            return dock_wireless_link;
        }
    };


    class PublishDockWirelessLinkTopic: public PublishTopicWrapper {
    public:
        DockWirelessLinkMsg msg;

        [[nodiscard]] DockWirelessLink getPodClass() const {
            return static_cast<DockWirelessLink>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockWirelessLinkTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
            message_topic = publish_msg->message_topic;
            message_data = publish_msg->message_data;
            need_replay = publish_msg->need_replay;
            message_id = publish_msg->message_id;
            message_type = publish_msg->message_type;
            timestamp = publish_msg->timestamp;
            version = publish_msg->version;
            device_sn = publish_msg->device_sn;
            subscribe_id = publish_msg->subscribe_id;

            std::string err;
            aigc::JsonHelper::JsonToObject(msg, publish_msg->message_data, {}, &err);
            if (!err.empty()) {
                LOG_ERROR("PublishDockWirelessLinkTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
                is_valid_ = false;
            }
        }

        AIGC_JSON_HELPER(msg);
        AIGC_JSON_HELPER_BASE((PublishTopicWrapper *)this)

        std::string ToJsonString() override {
            std::string err, json_string;
            aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
            return json_string;
        }

        void FromJsonString(const std::string &json) override {
            std::string err;
            aigc::JsonHelper::JsonToObject(*this, json, {}, &err);
        }

    private:
        bool is_valid_ = true;
    };
}
