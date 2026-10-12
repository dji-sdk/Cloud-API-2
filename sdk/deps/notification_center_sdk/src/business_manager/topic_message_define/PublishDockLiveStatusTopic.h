// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 直播状态项;
    class LiveStatusItemMsg {
    public:
        std::string video_id;
        std::string video_type;
        int64_t video_quality = 0;
        int64_t status = 0;
        int64_t error_status = 0;

        AIGC_JSON_HELPER(video_id, video_type, video_quality, status, error_status);

        explicit operator LiveStatusItem() const {
            LiveStatusItem obj{};
            obj.video_id = video_id;
            obj.video_type = video_type;
            obj.video_quality = video_quality;
            obj.status = status;
            obj.error_status = error_status;
            return obj;
        }
    };

    // 机场直播状态(topic_dock_live_status);
    class DockLiveStatusMsg {
    public:
        std::vector<LiveStatusItemMsg> live_status;

        AIGC_JSON_HELPER(live_status);

        explicit operator DockLiveStatus() const {
            DockLiveStatus obj{};
            for (const auto &item : live_status) {
                obj.live_status.push_back(static_cast<LiveStatusItem>(item));
            }
            return obj;
        }
    };

    class PublishDockLiveStatusTopic : public PublishTopicWrapper {
    public:
        DockLiveStatusMsg msg;

        [[nodiscard]] DockLiveStatus getPodClass() const {
            return static_cast<DockLiveStatus>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockLiveStatusTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockLiveStatusTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
