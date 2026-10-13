// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class WaylineBreakPointMsg {
    public:
        int index = 0;
        int state = 0;
        double progress = 0.0;
        int wayline_id = 0;
        int break_reason = 0;
        double latitude = 0.0;
        double longitude = 0.0;
        double height = 0.0;
        double attitude_head = 0.0;

        AIGC_JSON_HELPER(index, state, progress, wayline_id, break_reason,
            latitude, longitude, height, attitude_head);

        explicit operator WaylineBreakPoint() const {
            WaylineBreakPoint bp{};
            bp.index = index;
            bp.state = state;
            bp.progress = progress;
            bp.wayline_id = wayline_id;
            bp.break_reason = break_reason;
            bp.latitude = latitude;
            bp.longitude = longitude;
            bp.height = height;
            bp.attitude_head = attitude_head;
            return bp;
        }
    };

    class WaylineObstacleMsg {
    public:
        int id = 0;
        int type = 0;
        int64_t timestamp = 0;
        double latitude = 0.0;
        double longitude = 0.0;
        double height = 0.0;
        int wayline_id = 0;
        int waypoint_index = 0;

        AIGC_JSON_HELPER(id, type, timestamp, latitude, longitude, height,
            wayline_id, waypoint_index);

        explicit operator WaylineObstacle() const {
            WaylineObstacle obs{};
            obs.id = id;
            obs.type = type;
            obs.timestamp = timestamp;
            obs.latitude = latitude;
            obs.longitude = longitude;
            obs.height = height;
            obs.wayline_id = wayline_id;
            obs.waypoint_index = waypoint_index;
            return obs;
        }
    };

    class WaylineTaskDataMsg {
    public:
        std::string name;
        std::string uuid;
        int task_type = 0;
        int status = 0;
        std::string sn;
        std::string landing_dock_sn;
        std::string wayline_uuid;
        int wayline_precision_type = 0;
        double wayline_duration = 0.0;
        std::vector<int> wayline_segment_indexes;
        int total_waypoints = 0;
        int current_waypoint_index = 0;
        std::string bid;
        std::string begin_at;
        std::string end_at;
        std::string run_at;
        std::string completed_at;
        int out_of_control_action_in_flight = 0;
        int rth_altitude = 0;
        int rth_mode = 0;
        int min_battery_capacity = 0;
        std::vector<std::string> tags;
        std::string cloud_to_cloud_id;
        WaylineBreakPointMsg break_point;
        bool is_break_point_resume = false;
        int media_upload_status = 0;
        bool fast_reconstruction = false;
        int device_type = 0;
        std::vector<WaylineObstacleMsg> obstacles;
        std::string created_at;
        std::string updated_at;
        std::string deleted_at;

        AIGC_JSON_HELPER(name, uuid, task_type, status, sn, landing_dock_sn,
            wayline_uuid, wayline_precision_type, wayline_duration, wayline_segment_indexes,
            total_waypoints, current_waypoint_index, bid, begin_at, end_at, run_at,
            completed_at, out_of_control_action_in_flight, rth_altitude, rth_mode,
            min_battery_capacity, tags, cloud_to_cloud_id, break_point,
            is_break_point_resume, media_upload_status, fast_reconstruction, device_type,
            obstacles, created_at, updated_at, deleted_at);

        explicit operator WaylineTaskData() const {
            WaylineTaskData data{};
            data.name = name;
            data.uuid = uuid;
            data.task_type = task_type;
            data.status = status;
            data.sn = sn;
            data.landing_dock_sn = landing_dock_sn;
            data.wayline_uuid = wayline_uuid;
            data.wayline_precision_type = wayline_precision_type;
            data.wayline_duration = wayline_duration;
            data.wayline_segment_indexes = wayline_segment_indexes;
            data.total_waypoints = total_waypoints;
            data.current_waypoint_index = current_waypoint_index;
            data.bid = bid;
            data.begin_at = begin_at;
            data.end_at = end_at;
            data.run_at = run_at;
            data.completed_at = completed_at;
            data.out_of_control_action_in_flight = out_of_control_action_in_flight;
            data.rth_altitude = rth_altitude;
            data.rth_mode = rth_mode;
            data.min_battery_capacity = min_battery_capacity;
            data.tags = tags;
            data.cloud_to_cloud_id = cloud_to_cloud_id;
            data.break_point = static_cast<WaylineBreakPoint>(break_point);
            data.is_break_point_resume = is_break_point_resume;
            data.media_upload_status = media_upload_status;
            data.fast_reconstruction = fast_reconstruction;
            data.device_type = device_type;
            for (const auto &obs : obstacles) {
                data.obstacles.push_back(static_cast<WaylineObstacle>(obs));
            }
            data.created_at = created_at;
            data.updated_at = updated_at;
            data.deleted_at = deleted_at;
            return data;
        }
    };

    class WaylineTaskEventMsg {
    public:
        std::string event_id;
        std::string action;
        std::string uuid;
        int64_t timestamp = 0;
        WaylineTaskDataMsg task_data;

        AIGC_JSON_HELPER(event_id, action, uuid, timestamp, task_data);

        explicit operator WaylineTaskEvent() const {
            WaylineTaskEvent event{};
            event.event_id = event_id;
            event.action = action;
            event.uuid = uuid;
            event.timestamp = timestamp;
            event.task_data = static_cast<WaylineTaskData>(task_data);
            return event;
        }
    };

    class PublishWaylineTaskEventTopic : public PublishTopicWrapper {
    public:
        WaylineTaskEventMsg msg;

        [[nodiscard]] WaylineTaskEvent getPodClass() const {
            return static_cast<WaylineTaskEvent>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishWaylineTaskEventTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishWaylineTaskEventTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
