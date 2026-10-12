// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 备降点(alternate_land_point);
    class DockAlternateLandPointMsg {
    public:
        int is_configured = 0;          // 备降点是否设置, {0:未设置, 1:已设置};
        double longitude = 0.0;         // 经度;
        double latitude = 0.0;          // 纬度;
        double safe_land_height = 0.0;  // 安全高度(备降转移高);
        double height = 0.0;            // 高度;

        AIGC_JSON_HELPER(is_configured, longitude, latitude, safe_land_height, height);

        explicit operator DockAlternateLandPoint() const {
            DockAlternateLandPoint dock_alternate_land_point{};

            dock_alternate_land_point.is_configured = is_configured;
            dock_alternate_land_point.longitude = longitude;
            dock_alternate_land_point.latitude = latitude;
            dock_alternate_land_point.safe_land_height = safe_land_height;
            dock_alternate_land_point.height = height;


            return dock_alternate_land_point;
        }
    };

    class PublishDockAlternateLandPointTopic: public PublishTopicWrapper {
    public:
        DockAlternateLandPointMsg msg;

        [[nodiscard]] DockAlternateLandPoint getPodClass() const {
            return static_cast<DockAlternateLandPoint>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockAlternateLandPointTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockAlternateLandPointTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
