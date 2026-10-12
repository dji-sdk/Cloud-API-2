// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 飞机起飞进度事件消息
    class AircraftTakeoffProgressMsg {
    public:
        std::string bid;
        std::string tid;
        std::string flight_id;
        std::string track_id;
        std::string sn;
        std::string status;
        int result = 0;
        int waypoint_index = 0;
        std::string takeoff_mode;
        uint64_t timestamp = 0;

        AIGC_JSON_HELPER(bid, tid, flight_id, track_id, sn, status, result, waypoint_index, takeoff_mode, timestamp);

        explicit operator AircraftTakeoffProgress() const {
            AircraftTakeoffProgress event{};
            event.bid = bid;
            event.tid = tid;
            event.flight_id = flight_id;
            event.track_id = track_id;
            event.sn = sn;
            event.status = status;
            event.result = result;
            event.waypoint_index = waypoint_index;
            event.takeoff_mode = takeoff_mode;
            event.timestamp = timestamp;
            return event;
        }
    };

    class PublishAircraftTakeoffProgressTopic : public PublishTopicWrapper {
    public:
        AircraftTakeoffProgressMsg msg;

        [[nodiscard]] AircraftTakeoffProgress getPodClass() const {
            return static_cast<AircraftTakeoffProgress>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftTakeoffProgressTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftTakeoffProgressTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
