// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class AircraftAttitudeMsg {
    public:
        double attitude_head = 0.0; // 机头朝向角度
        double attitude_pitch = 0.0; // 俯仰轴角度
        double attitude_roll = 0.0; // 横滚轴角度

        AIGC_JSON_HELPER(attitude_head, attitude_pitch, attitude_roll);

        explicit operator AircraftAttitude() const {
            AircraftAttitude aircraft_attitude{};
            aircraft_attitude.attitude_head = attitude_head;
            aircraft_attitude.attitude_pitch = attitude_pitch;
            aircraft_attitude.attitude_roll = attitude_roll;
            return aircraft_attitude;
        }
    };

    class PublishAircraftAttitudeTopic : public PublishTopicWrapper {
    public:
        AircraftAttitudeMsg msg;

        [[nodiscard]] AircraftAttitude getPodClass() const {
            return static_cast<AircraftAttitude>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftAttitudeTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftAttitudeTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
