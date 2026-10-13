// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 进离场任务状态;
    // AircraftVisualScoutStatus;
    class AircraftVisualScoutStatusMsg {
    public:
        std::string task_id;
        int visual_scout_stage;
        uint64_t scout_wayline_generate_remain_time;


        AIGC_JSON_HELPER(task_id, visual_scout_stage, scout_wayline_generate_remain_time);

        explicit operator AircraftVisualScoutStatus() const {
            AircraftVisualScoutStatus res;

            res.task_id = task_id;
            res.visual_scout_stage = visual_scout_stage;
            res.scout_wayline_generate_remain_time = scout_wayline_generate_remain_time;

            return res;
        }
    };




    // topic 定义;
    class PublishAircraftVisualScoutStatusTopic: public PublishTopicWrapper {
    public:
        AircraftVisualScoutStatusMsg msg;

        [[nodiscard]] AircraftVisualScoutStatus getPodClass() const {
            return static_cast<AircraftVisualScoutStatus>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftVisualScoutStatusTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftVisualScoutStatusTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

