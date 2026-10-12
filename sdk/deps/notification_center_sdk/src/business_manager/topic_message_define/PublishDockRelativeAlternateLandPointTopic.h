// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 相对备降点(relative_alternate_land_point);
    // DockRelativeAlternateLandPoint;
    class DockRelativeAlternateLandPointMsg {
    public:
        double longitude = 0.0;          // 经度;
        double latitude = 0.0;           // 纬度;
        double safe_land_height = 0.0;   // 安全高度(备降转移高);
        int status = 0;                  // { 0: 正常, 1: 机场未设置相对备降点, 2: 机场未进入基站模式, 3: 机场朝向未收敛 };

        AIGC_JSON_HELPER(longitude, latitude, safe_land_height, status);

        explicit operator DockRelativeAlternateLandPoint() const {
            DockRelativeAlternateLandPoint res{};

            res.longitude = longitude;
            res.latitude = latitude;
            res.safe_land_height = safe_land_height;
            res.status = status;

            return res;
        }
    };

    // topic 定义;
    class PublishDockRelativeAlternateLandPointTopic: public PublishTopicWrapper {
    public:
        DockRelativeAlternateLandPointMsg msg;

        [[nodiscard]] DockRelativeAlternateLandPoint getPodClass() const {
            return static_cast<DockRelativeAlternateLandPoint>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockRelativeAlternateLandPointTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockRelativeAlternateLandPointTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
