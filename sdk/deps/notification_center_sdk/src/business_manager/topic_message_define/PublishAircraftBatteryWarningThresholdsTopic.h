// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 电量告警信息(用户设置的阈值);
    // AircraftBatteryWarningThresholds;
    class AircraftBatteryWarningThresholdsMsg {
    public:
        int low_battery_warning_threshold = 0;            // int, 用户设置的电池低电量告警百分比, [0, 100];
        int serious_low_battery_warning_threshold = 0;    // int, 用户设置的电池严重低电量告警百分比, [0,100];

        AIGC_JSON_HELPER(low_battery_warning_threshold, serious_low_battery_warning_threshold);

        explicit operator AircraftBatteryWarningThresholds() const {
            AircraftBatteryWarningThresholds res{};
            
            res.low_battery_warning_threshold = low_battery_warning_threshold;
            res.serious_low_battery_warning_threshold = serious_low_battery_warning_threshold;

            return res;
        }
    };


    // topic define;
    class PublishAircraftBatteryWarningThresholdsTopic: public PublishTopicWrapper {
    public:
        AircraftBatteryWarningThresholdsMsg msg;

        [[nodiscard]] AircraftBatteryWarningThresholds getPodClass() const {
            return static_cast<AircraftBatteryWarningThresholds>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftBatteryWarningThresholdsTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftBatteryWarningThresholdsTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

