// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class PoiCenterPointMsg {
    public:
        double latitude = 0.0;
        double longitude = 0.0;
        double height = 0.0;

        AIGC_JSON_HELPER(latitude, longitude, height);

        explicit operator PoiCenterPoint() const {
            PoiCenterPoint pt{};
            pt.latitude = latitude;
            pt.longitude = longitude;
            pt.height = height;
            return pt;
        }
    };

    class PoiTaskDataMsg {
    public:
        std::string uuid;
        std::string sn;
        std::string device_sn;
        PoiCenterPointMsg poi_center_point;
        double speed = 0.0;
        double circle_radius = 0.0;
        std::string payload_index;
        int status = 0;
        int err_code = 0;

        AIGC_JSON_HELPER(uuid, sn, device_sn, poi_center_point, speed, circle_radius,
            payload_index, status, err_code);

        explicit operator PoiTaskData() const {
            PoiTaskData data{};
            data.uuid = uuid;
            data.sn = sn;
            data.device_sn = device_sn;
            data.poi_center_point = static_cast<PoiCenterPoint>(poi_center_point);
            data.speed = speed;
            data.circle_radius = circle_radius;
            data.payload_index = payload_index;
            data.status = status;
            data.err_code = err_code;
            return data;
        }
    };

    class PoiTaskEventMsg {
    public:
        std::string event_id;
        std::string action;
        std::string uuid;
        int64_t timestamp = 0;
        PoiTaskDataMsg task_data;

        AIGC_JSON_HELPER(event_id, action, uuid, timestamp, task_data);

        explicit operator PoiTaskEvent() const {
            PoiTaskEvent event{};
            event.event_id = event_id;
            event.action = action;
            event.uuid = uuid;
            event.timestamp = timestamp;
            event.task_data = static_cast<PoiTaskData>(task_data);
            return event;
        }
    };

    class PublishPoiTaskEventTopic : public PublishTopicWrapper {
    public:
        PoiTaskEventMsg msg;

        [[nodiscard]] PoiTaskEvent getPodClass() const {
            return static_cast<PoiTaskEvent>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishPoiTaskEventTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishPoiTaskEventTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
