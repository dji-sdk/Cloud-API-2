// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class ControlRightsCloudControlStatusItemMsg {
    public:
        std::string type; // flight/payload, 飞机还是负载
        std::string key; // 如果 type 是 flight, 则 key 固定为flight, 如果是 payload, 则是 payload_81-0-0 这样的格式(带上了payload_index)
        std::string user_id; // 用户 id
        bool is_locked = false;
        double version = 0;

        AIGC_JSON_HELPER(type, key, user_id, is_locked, version);

        explicit operator ControlRightsCloudControlStatusItem() const {
            ControlRightsCloudControlStatusItem control_rights_cloud_control_status_item{};
            control_rights_cloud_control_status_item.type = type;
            control_rights_cloud_control_status_item.key = key;
            control_rights_cloud_control_status_item.user_id = user_id;
            control_rights_cloud_control_status_item.is_locked = is_locked;
            control_rights_cloud_control_status_item.version = version;
            return control_rights_cloud_control_status_item;
        }
    };

    class ControlRightsCloudControlStatusMsg {
    public:
        std::string sn;
        std::string reason;
        std::vector<ControlRightsCloudControlStatusItemMsg> controls;

        AIGC_JSON_HELPER(sn, reason, controls);

        explicit operator ControlRightsCloudControlStatus() const {
            ControlRightsCloudControlStatus control_rights_cloud_control_status{};
            control_rights_cloud_control_status.sn = sn;
            control_rights_cloud_control_status.reason = reason;
            for (const auto & control : controls) {
                control_rights_cloud_control_status.controls.emplace_back(
                    static_cast<ControlRightsCloudControlStatusItem>(control));
            }
            return control_rights_cloud_control_status;
        }
    };

    class PublishControlRightsCloudControlStatusTopic : public PublishTopicWrapper {
    public:
        ControlRightsCloudControlStatusMsg msg;

        [[nodiscard]] ControlRightsCloudControlStatus getPodClass() const {
            return static_cast<ControlRightsCloudControlStatus>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishControlRightsCloudControlStatusTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishControlRightsCloudControlStatusTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
