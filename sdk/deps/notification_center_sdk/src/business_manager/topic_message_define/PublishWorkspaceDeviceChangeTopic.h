// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class DeviceChangeMsg {
    public:
        std::string change_type; // "add" or "reduce"
        std::vector<std::string> change_devices;

        AIGC_JSON_HELPER(change_type, change_devices);

        explicit operator DeviceChange() const {
            DeviceChange device_change{};
            device_change.change_type = change_type;
            device_change.change_devices = change_devices;
            return device_change;
        }
    };

    class WorkspaceDeviceChangeMsg {
    public:
        std::vector<DeviceChangeMsg> device_changes;

        AIGC_JSON_HELPER(device_changes);

        explicit operator WorkspaceDeviceChange() const {
            WorkspaceDeviceChange workspace_device_change{};
            for (const auto & device_change : device_changes) {
                workspace_device_change.device_changes.emplace_back(static_cast<DeviceChange>(device_change));
            }
            return workspace_device_change;
        }
    };

    class PublishWorkspaceDeviceChangeTopic : public PublishTopicWrapper {
    public:
        WorkspaceDeviceChangeMsg msg;

        [[nodiscard]] WorkspaceDeviceChange getPodClass() const {
            return static_cast<WorkspaceDeviceChange>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishWorkspaceDeviceChangeTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishWorkspaceDeviceChangeTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
