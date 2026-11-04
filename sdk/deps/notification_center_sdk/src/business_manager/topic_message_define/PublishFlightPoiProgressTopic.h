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
        // 字段名/类型严格对齐设备 topic_task_flight_poi_progress 实际推送：
        // {device_sn, drone_sn, center_point{lat,lon,height}, circle_radius, circle_speed,
        //  max_circle_speed, reason, status(string), timestamp, execute_duration, payload_index}
        std::string device_sn;
        std::string drone_sn;
        FlightPoiProgressCenterPointMsg center_point;
        double circle_radius = 0.0;
        double circle_speed = 0.0;
        double max_circle_speed = 0.0;
        int reason = 0;
        std::string status;            // 设备推送为字符串，如 "in_progress" / "ok"
        long timestamp = 0;
        int execute_duration = 0;
        std::string payload_index;

        AIGC_JSON_HELPER(device_sn, drone_sn, center_point, circle_radius, circle_speed,
                         max_circle_speed, reason, status, timestamp, execute_duration, payload_index);

        explicit operator FlightPoiProgress() const {
            FlightPoiProgress obj{};
            obj.device_sn = device_sn;
            obj.drone_sn = drone_sn;
            obj.poi_center_point = static_cast<FlightPoiProgressCenterPoint>(center_point);
            obj.circle_radius = circle_radius;
            obj.circle_speed = circle_speed;
            obj.max_circle_speed = max_circle_speed;
            obj.speed = circle_speed;   // 兼容旧字段 speed
            obj.reason = reason;
            obj.status = status;
            obj.timestamp = timestamp;
            obj.execute_duration = execute_duration;
            obj.payload_index = payload_index;
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
