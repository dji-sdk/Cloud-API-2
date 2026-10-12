// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 飞机: 限制类信息状态;
    // AircraftLimitsInfo;
    class AircraftLimitsInfoMsg {
    public:
        int height_limit = 0;             // 相对起飞点的飞行限制高度,单位(m);
        int is_near_height_limit = 0;     // 是否接近设定的限制高度, {0: 未达到设定的限制高度, 1: 接近设定的限制高度};
        int is_near_area_limit = 0;       // 是否接近限飞区,{0: 未达到限飞区, 1:1: 接近限飞区};
        int distance_limit_state = 0;     // 限远状态,{0: 未设置, 1: 有设置};
        int distance_limit = 0;           // 限远距离;
        int is_near_distance_limit = 0;   // 是否接近限远距离;


        AIGC_JSON_HELPER(height_limit, is_near_height_limit, is_near_area_limit, distance_limit_state, distance_limit, is_near_distance_limit);

        explicit operator AircraftLimitsInfo() const {
            AircraftLimitsInfo limits_info{};

            limits_info.height_limit = height_limit;
            limits_info.is_near_height_limit = is_near_height_limit;
            limits_info.is_near_area_limit = is_near_area_limit;
            limits_info.distance_limit_state = distance_limit_state;
            limits_info.distance_limit = distance_limit;
            limits_info.is_near_distance_limit = is_near_distance_limit;

            return limits_info;
        }
    };


    // topic definition;
    class PublishAircraftLimitsInfoTopic: public PublishTopicWrapper {
    public:
        AircraftLimitsInfoMsg msg;

        [[nodiscard]] AircraftLimitsInfo getPodClass() const {
            return static_cast<AircraftLimitsInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftLimitsInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftLimitsInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

