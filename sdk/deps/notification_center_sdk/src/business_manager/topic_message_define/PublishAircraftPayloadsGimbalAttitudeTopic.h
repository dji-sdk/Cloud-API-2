// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class AircraftPayloadGimbalAttitudeMsg {
    public:
        double gimbal_pitch = 0.0;
        double gimbal_roll = 0.0;
        double gimbal_yaw = 0.0;

        AIGC_JSON_HELPER(gimbal_pitch, gimbal_roll, gimbal_yaw);

        explicit operator AircraftPayloadGimbalAttitude() const {
            AircraftPayloadGimbalAttitude aircraft_payload_gimbal_attitude{};
            aircraft_payload_gimbal_attitude.gimbal_pitch = gimbal_pitch;
            aircraft_payload_gimbal_attitude.gimbal_roll = gimbal_roll;
            aircraft_payload_gimbal_attitude.gimbal_yaw = gimbal_yaw;
            return aircraft_payload_gimbal_attitude;
        }
    };

    class AircraftPayloadsGimbalAttitudeMsg {
    public:
        std::unordered_map<std::string, AircraftPayloadGimbalAttitudeMsg> payloads_gimbal_attitude;

        AIGC_JSON_HELPER(payloads_gimbal_attitude);

        explicit operator AircraftPayloadsGimbalAttitude() const {
            AircraftPayloadsGimbalAttitude aircraft_payloads_gimbal_attitude{};
            for (const auto & payload : payloads_gimbal_attitude) {
                aircraft_payloads_gimbal_attitude.payloads_gimbal_attitude[payload.first] = 
                    static_cast<AircraftPayloadGimbalAttitude>(payload.second);
            }
            return aircraft_payloads_gimbal_attitude;
        }
    };

    class PublishAircraftPayloadsGimbalAttitudeTopic : public PublishTopicWrapper {
    public:
        AircraftPayloadsGimbalAttitudeMsg msg;

        [[nodiscard]] AircraftPayloadsGimbalAttitude getPodClass() const {
            return static_cast<AircraftPayloadsGimbalAttitude>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftPayloadsGimbalAttitudeTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftPayloadsGimbalAttitudeTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
