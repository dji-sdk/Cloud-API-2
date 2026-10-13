// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class FlyToWayPointMsg {
    public:
        double latitude = 0.0;
        double longitude = 0.0;
        double height = 0.0;

        AIGC_JSON_HELPER(latitude, longitude, height);

        explicit operator FlyToWayPoint() const {
            FlyToWayPoint wp{};
            wp.latitude = latitude;
            wp.longitude = longitude;
            wp.height = height;
            return wp;
        }
    };

    class FlyToTaskDataMsg {
    public:
        int id = 0;
        std::string uuid;
        std::string prj_uuid;
        std::string sn;
        std::string drone_sn;
        std::string user_id;
        int seq_no = 0;
        std::string flight_task_uuid;
        int flight_task_type = 0;
        int status = 0;
        int err_code = 0;
        int way_point_index = 0;
        std::vector<FlyToWayPointMsg> way_points;
        std::vector<FlyToWayPointMsg> planned_path_points;
        double remaining_distance = 0.0;
        double remaining_time = 0.0;

        AIGC_JSON_HELPER(id, uuid, prj_uuid, sn, drone_sn, user_id, seq_no,
            flight_task_uuid, flight_task_type, status, err_code, way_point_index,
            way_points, planned_path_points, remaining_distance, remaining_time);

        explicit operator FlyToTaskData() const {
            FlyToTaskData data{};
            data.id = id;
            data.uuid = uuid;
            data.prj_uuid = prj_uuid;
            data.sn = sn;
            data.drone_sn = drone_sn;
            data.user_id = user_id;
            data.seq_no = seq_no;
            data.flight_task_uuid = flight_task_uuid;
            data.flight_task_type = flight_task_type;
            data.status = status;
            data.err_code = err_code;
            data.way_point_index = way_point_index;
            for (const auto &wp : way_points) {
                data.way_points.push_back(static_cast<FlyToWayPoint>(wp));
            }
            for (const auto &pp : planned_path_points) {
                data.planned_path_points.push_back(static_cast<FlyToWayPoint>(pp));
            }
            data.remaining_distance = remaining_distance;
            data.remaining_time = remaining_time;
            return data;
        }
    };

    class FlyToTaskEventMsg {
    public:
        std::string event_id;
        std::string action;
        std::string uuid;
        int64_t timestamp = 0;
        FlyToTaskDataMsg task_data;

        AIGC_JSON_HELPER(event_id, action, uuid, timestamp, task_data);

        explicit operator FlyToTaskEvent() const {
            FlyToTaskEvent event{};
            event.event_id = event_id;
            event.action = action;
            event.uuid = uuid;
            event.timestamp = timestamp;
            event.task_data = static_cast<FlyToTaskData>(task_data);
            return event;
        }
    };

    class PublishFlyToTaskEventTopic : public PublishTopicWrapper {
    public:
        FlyToTaskEventMsg msg;

        [[nodiscard]] FlyToTaskEvent getPodClass() const {
            return static_cast<FlyToTaskEvent>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishFlyToTaskEventTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishFlyToTaskEventTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
