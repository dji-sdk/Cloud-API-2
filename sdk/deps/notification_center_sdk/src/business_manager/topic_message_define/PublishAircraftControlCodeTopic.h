// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class AircraftControlCodeMsg {
    public:
        int control_mode = 0; // "0": "航线控制-执行态", "1": "指令控制-手动飞行", "2": "指令控制-自动飞行", "3": "航线控制-中断态"

        AIGC_JSON_HELPER(control_mode);

        explicit operator AircraftControlCode() const {
            AircraftControlCode aircraft_control_code{};
            aircraft_control_code.control_mode = control_mode;
            return aircraft_control_code;
        }
    };

    class PublishAircraftControlCodeTopic : public PublishTopicWrapper {
    public:
        AircraftControlCodeMsg msg;

        [[nodiscard]] AircraftControlCode getPodClass() const {
            return static_cast<AircraftControlCode>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftControlCodeTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftControlCodeTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
