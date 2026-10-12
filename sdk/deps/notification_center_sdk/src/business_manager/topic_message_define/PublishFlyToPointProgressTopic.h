// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class FlyToPointProgressMsg {
    public:
        std::string bid;
        std::string tid;
        uint64_t timestamp = 0;
        std::string fly_to_id;
        std::string flight_id;
        int task_seq = 0;
        std::string sn;
        std::string status;
        int result = 0;
        int waypoint_index = 0;

        AIGC_JSON_HELPER(bid, tid, timestamp, fly_to_id, flight_id, task_seq, sn, status, result, waypoint_index);

        explicit operator FlyToPointProgress() const {
            FlyToPointProgress fly_to_point_progress{};
            fly_to_point_progress.bid = bid;
            fly_to_point_progress.tid = tid;
            fly_to_point_progress.timestamp = timestamp;
            fly_to_point_progress.fly_to_id = fly_to_id;
            fly_to_point_progress.flight_id = flight_id;
            fly_to_point_progress.task_seq = task_seq;
            fly_to_point_progress.sn = sn;
            fly_to_point_progress.status = status;
            fly_to_point_progress.result = result;
            fly_to_point_progress.waypoint_index = waypoint_index;
            return fly_to_point_progress;
        }
    };

    class PublishFlyToPointProgressTopic : public PublishTopicWrapper {
    public:
        FlyToPointProgressMsg msg;

        [[nodiscard]] FlyToPointProgress getPodClass() const {
            return static_cast<FlyToPointProgress>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishFlyToPointProgressTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishFlyToPointProgressTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
