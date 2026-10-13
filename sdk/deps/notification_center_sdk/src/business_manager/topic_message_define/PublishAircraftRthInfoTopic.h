// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 返航信息(返航模式, 返航高度);
    // AircraftRthInfo;
    class AircraftRthInfoMsg {
    public:
        int rth_mode = 0;         // int, 返航高度模式设置值, {0: "智能高度", 1: "设定高度"};
        int rth_altitude = 0;     // int, 返航高度(单位: m), 参考值: [20, 1500];


        AIGC_JSON_HELPER(rth_mode, rth_altitude);

        explicit operator AircraftRthInfo() const {
            AircraftRthInfo res{};

            res.rth_mode = rth_mode;
            res.rth_altitude = rth_altitude;

            return res;
        }
    };


    // topic definition;
    class PublishAircraftRthInfoTopic: public PublishTopicWrapper {
    public:
        AircraftRthInfoMsg msg;

        [[nodiscard]] AircraftRthInfo getPodClass() const {
            return static_cast<AircraftRthInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftRthInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftRthInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

