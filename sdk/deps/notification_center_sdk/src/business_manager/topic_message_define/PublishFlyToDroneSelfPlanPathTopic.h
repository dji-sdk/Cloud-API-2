// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class FlyToDroneSelfPlanPathPointMsg {
    public:
        double latitude = 0.0;
        double longitude = 0.0;
        double height = 0.0;

        AIGC_JSON_HELPER(latitude, longitude, height);

        explicit operator FlyToDroneSelfPlanPathPoint() const {
            FlyToDroneSelfPlanPathPoint fly_to_drone_self_plan_path_point{};
            fly_to_drone_self_plan_path_point.latitude = latitude;
            fly_to_drone_self_plan_path_point.longitude = longitude;
            fly_to_drone_self_plan_path_point.height = height;
            return fly_to_drone_self_plan_path_point;
        }
    };

    class FlyToDroneSelfPlanPathMsg {
    public:
        std::string fly_to_id;
        std::string sn;
        uint64_t timestamp = 0;
        std::vector<FlyToDroneSelfPlanPathPointMsg> planned_path_points;
        double remaining_distance = 0;
        double remaining_time = 0;

        AIGC_JSON_HELPER(fly_to_id, sn, timestamp, planned_path_points, remaining_distance, remaining_time);

        explicit operator FlyToDroneSelfPlanPath() const {
            FlyToDroneSelfPlanPath fly_to_drone_self_plan_path{};
            fly_to_drone_self_plan_path.fly_to_id = fly_to_id;
            fly_to_drone_self_plan_path.sn = sn;
            fly_to_drone_self_plan_path.timestamp = timestamp;
            for (const auto & point : planned_path_points) {
                fly_to_drone_self_plan_path.planned_path_points.emplace_back(
                    static_cast<FlyToDroneSelfPlanPathPoint>(point));
            }
            fly_to_drone_self_plan_path.remaining_distance = remaining_distance;
            fly_to_drone_self_plan_path.remaining_time = remaining_time;
            return fly_to_drone_self_plan_path;
        }
    };

    class PublishFlyToDroneSelfPlanPathTopic : public PublishTopicWrapper {
    public:
        FlyToDroneSelfPlanPathMsg msg;

        [[nodiscard]] FlyToDroneSelfPlanPath getPodClass() const {
            return static_cast<FlyToDroneSelfPlanPath>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishFlyToDroneSelfPlanPathTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishFlyToDroneSelfPlanPathTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
