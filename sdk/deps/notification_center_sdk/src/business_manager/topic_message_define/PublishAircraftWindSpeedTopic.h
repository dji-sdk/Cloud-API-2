// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class AircraftWindSpeedMsg {
    public:
        int wind_direction = 0; // 当前风向 "enum_desc": "1:正北  2：东北  3:东  4:东南  5:南  6:西南  7:西   8:西北"
        double wind_speed = 0.0; // 风速估计，该风速是通过飞机姿态推测出的，有一定的误差，仅供参考，不能作为气象数据使用

        AIGC_JSON_HELPER(wind_direction, wind_speed);

        explicit operator AircraftWindSpeed() const {
            AircraftWindSpeed aircraft_wind_speed{};
            aircraft_wind_speed.wind_direction = wind_direction;
            aircraft_wind_speed.wind_speed = wind_speed;
            return aircraft_wind_speed;
        }
    };

    class PublishAircraftWindSpeedTopic : public PublishTopicWrapper {
    public:
        AircraftWindSpeedMsg msg;

        [[nodiscard]] AircraftWindSpeed getPodClass() const {
            return static_cast<AircraftWindSpeed>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftWindSpeedTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftWindSpeedTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
