// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 航迹id(track_id);
    // AircraftTrackId;
    class AircraftTrackIdMsg {
    public:
        // string, 航迹id, such as "da247702-9636-4404-b98c-7e667efb4c4b";
        std::string track_id = "";


        AIGC_JSON_HELPER(track_id);

        explicit operator AircraftTrackId() const {
            AircraftTrackId res;

            res.track_id = track_id;

            return res;
        }
    };


    // topic 定义;
    class PublishAircraftTrackIdTopic: public PublishTopicWrapper {
    public:
        AircraftTrackIdMsg msg;

        [[nodiscard]] AircraftTrackId getPodClass() const {
            return static_cast<AircraftTrackId>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftTrackIdTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftTrackIdTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

