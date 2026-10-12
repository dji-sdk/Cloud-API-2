// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 飞机: 档位信息(gear);
    // AircraftGear;
    class AircraftGearMsg {
    public:
        // int, 0:"A", 1:"P", 2:"NAV", 3:"FPV", 4:"FARM", 5:"S", 6:"F", 7:"M", 8:"G", 9:"T";
        int gear = 0;


        AIGC_JSON_HELPER(gear);

        explicit operator AircraftGear() const {
            AircraftGear res;

            res.gear = gear;

            return res;
        }
    };


    // topic 定义;
    class PublishAircraftGearTopic: public PublishTopicWrapper {
    public:
        AircraftGearMsg msg;

        [[nodiscard]] AircraftGear getPodClass() const {
            return static_cast<AircraftGear>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftGearTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftGearTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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


