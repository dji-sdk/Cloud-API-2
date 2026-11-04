// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class WaypointMsg {
    public:
        double latitude = 0.0;
        double longitude = 0.0;
        double height = 0.0;

        AIGC_JSON_HELPER(latitude, longitude, height);

        explicit operator Waypoint() const {
            Waypoint wp{};
            wp.latitude = latitude;
            wp.longitude = longitude;
            wp.height = height;
            return wp;
        }
    };

    class MultiDockHomeInfoMsg {
    public:
        std::string sn;
        int plan_status = 0;
        int estimated_battery_consumption = 0;
        double home_distance = 0.0;

        AIGC_JSON_HELPER(sn, plan_status, estimated_battery_consumption, home_distance);

        explicit operator MultiDockHomeInfo() const {
            MultiDockHomeInfo info{};
            info.sn = sn;
            info.plan_status = plan_status;
            info.estimated_battery_consumption = estimated_battery_consumption;
            info.home_distance = home_distance;
            return info;
        }
    };

    class ReturnHomeInfoMsg {
    public:
        int64_t timestamp = 0;
        std::vector<WaypointMsg> planned_path_points;
        int last_point_type = 0;
        std::string home_dock_sn;
        // 设备在普通(非蛙跳)任务中可能下发 null / 缺省, 蛙跳任务下发数组; 用 optional 兼容 null
        std::optional<std::vector<MultiDockHomeInfoMsg>> multi_dock_home_info;

        AIGC_JSON_HELPER(timestamp, planned_path_points, last_point_type, home_dock_sn, multi_dock_home_info);

        explicit operator ReturnHomeInfo() const {
            ReturnHomeInfo info{};
            info.timestamp = timestamp;
            for (const auto &wp : planned_path_points) {
                info.planned_path_points.push_back(static_cast<Waypoint>(wp));
            }
            info.last_point_type = last_point_type;
            info.home_dock_sn = home_dock_sn;
            if (multi_dock_home_info.has_value()) {
                for (const auto &dock_info : *multi_dock_home_info) {
                    info.multi_dock_home_info.push_back(static_cast<MultiDockHomeInfo>(dock_info));
                }
            }
            return info;
        }
    };

    class ReturnHomeInfoEventMsg {
    public:
        std::string flight_id;
        std::string sn;
        ReturnHomeInfoMsg info;

        AIGC_JSON_HELPER(flight_id, sn, info);

        explicit operator ReturnHomeInfoEvent() const {
            ReturnHomeInfoEvent event{};
            event.flight_id = flight_id;
            event.sn = sn;
            event.info = static_cast<ReturnHomeInfo>(info);
            return event;
        }
    };

    class PublishReturnHomeInfoEventTopic : public PublishTopicWrapper {
    public:
        ReturnHomeInfoEventMsg msg;

        [[nodiscard]] ReturnHomeInfoEvent getPodClass() const {
            return static_cast<ReturnHomeInfoEvent>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishReturnHomeInfoEventTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishReturnHomeInfoEventTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
