// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class FlightPoiProgressCenterPointMsg {
    public:
        double latitude = 0.0;
        double longitude = 0.0;
        double height = 0.0;

        AIGC_JSON_HELPER(latitude, longitude, height);

        explicit operator FlightPoiProgressCenterPoint() const {
            FlightPoiProgressCenterPoint obj{};
            obj.latitude = latitude;
            obj.longitude = longitude;
            obj.height = height;
            return obj;
        }
    };

    class FlightPoiProgressMsg {
    public:
        std::string uuid;
        std::string sn;
        std::string device_sn;
        FlightPoiProgressCenterPointMsg poi_center_point;
        double speed = 0.0;
        double circle_radius = 0.0;
        std::string payload_index;
        int status = 0;
        int err_code = 0;

        AIGC_JSON_HELPER(uuid, sn, device_sn, poi_center_point, speed, circle_radius, payload_index, status, err_code);

        explicit operator FlightPoiProgress() const {
            FlightPoiProgress obj{};
            obj.uuid = uuid;
            obj.sn = sn;
            obj.device_sn = device_sn;
            obj.poi_center_point = static_cast<FlightPoiProgressCenterPoint>(poi_center_point);
            obj.speed = speed;
            obj.circle_radius = circle_radius;
            obj.payload_index = payload_index;
            obj.status = status;
            obj.err_code = err_code;
            return obj;
        }
    };

    class PublishFlightPoiProgressTopic : public PublishTopicWrapper {
    public:
        FlightPoiProgressMsg msg;

        [[nodiscard]] FlightPoiProgress getPodClass() const {
            return static_cast<FlightPoiProgress>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishFlightPoiProgressTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishFlightPoiProgressTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
