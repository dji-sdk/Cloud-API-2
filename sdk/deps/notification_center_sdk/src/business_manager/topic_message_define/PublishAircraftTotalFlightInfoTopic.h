// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 累计飞行信息(total_flight_info);
    // AircraftTotalFlightInfo;
    class AircraftTotalFlightInfoMsg {
    public:
        double total_flight_time = 0.0;       // double, 总飞行时长(飞行器累计飞行航时);
        double total_flight_distance = 0.0;   // double, 总飞行距离;
        int total_flight_sorties = 0;         // int, 总飞行架次;


        AIGC_JSON_HELPER(total_flight_time, total_flight_distance, total_flight_sorties);

        explicit operator AircraftTotalFlightInfo() const {
            AircraftTotalFlightInfo res{};

            res.total_flight_time = total_flight_time;
            res.total_flight_distance = total_flight_distance;
            res.total_flight_sorties = total_flight_sorties;

            return res;
        }
    };


    // topic definition;
    class PublishAircraftTotalFlightInfoTopic: public PublishTopicWrapper {
    public:
        AircraftTotalFlightInfoMsg msg;

        [[nodiscard]] AircraftTotalFlightInfo getPodClass() const {
            return static_cast<AircraftTotalFlightInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftTotalFlightInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftTotalFlightInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

