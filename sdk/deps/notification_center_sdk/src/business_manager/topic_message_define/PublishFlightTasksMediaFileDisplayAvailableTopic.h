// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class FlightTasksMediaFileDisplayAvailableMsg {
    public:
        std::string uuid;
        int idx_parent_id = -1;
        int file_type = -1;
        int sub_file_type = -1;
        int media_file_id = -1;
        std::string flight_id;
        std::string sn;
        int media_source = -1;
        std::string url;

        AIGC_JSON_HELPER(uuid, idx_parent_id, file_type, sub_file_type, media_file_id, flight_id, sn, media_source, url);

        explicit operator FlightTasksMediaFileDisplayAvailable() const {
            FlightTasksMediaFileDisplayAvailable flight_tasks_media_file_display_available{};
            flight_tasks_media_file_display_available.uuid = uuid;
            flight_tasks_media_file_display_available.idx_parent_id = idx_parent_id;
            flight_tasks_media_file_display_available.file_type = file_type;
            flight_tasks_media_file_display_available.sub_file_type = sub_file_type;
            flight_tasks_media_file_display_available.media_file_id = media_file_id;
            flight_tasks_media_file_display_available.flight_id = flight_id;
            flight_tasks_media_file_display_available.sn = sn;
            flight_tasks_media_file_display_available.media_source = media_source;
            flight_tasks_media_file_display_available.url = url;
            return flight_tasks_media_file_display_available;
        }
    };

    class PublishFlightTasksMediaFileDisplayAvailableTopic : public PublishTopicWrapper {
    public:
        FlightTasksMediaFileDisplayAvailableMsg msg;

        [[nodiscard]] FlightTasksMediaFileDisplayAvailable getPodClass() const {
            return static_cast<FlightTasksMediaFileDisplayAvailable>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishFlightTasksMediaFileDisplayAvailableTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishFlightTasksMediaFileDisplayAvailableTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
