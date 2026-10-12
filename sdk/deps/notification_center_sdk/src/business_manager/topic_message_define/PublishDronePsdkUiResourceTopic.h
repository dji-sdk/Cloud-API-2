// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // PSDK UI 资源项;
    class PsdkUiResourceItemMsg {
    public:
        int64_t psdk_index = 0;
        int64_t psdk_ready = 0;
        std::string object_key;

        AIGC_JSON_HELPER(psdk_index, psdk_ready, object_key);

        explicit operator PsdkUiResourceItem() const {
            PsdkUiResourceItem obj{};
            obj.psdk_index = psdk_index;
            obj.psdk_ready = psdk_ready;
            obj.object_key = object_key;
            return obj;
        }
    };

    // PSDK UI资源(topic_drone_psdk_ui_resource);
    class DronePsdkUiResourceMsg {
    public:
        std::vector<PsdkUiResourceItemMsg> psdk_ui_resource;

        AIGC_JSON_HELPER(psdk_ui_resource);

        explicit operator DronePsdkUiResource() const {
            DronePsdkUiResource obj{};
            for (const auto &item : psdk_ui_resource) {
                obj.psdk_ui_resource.push_back(static_cast<PsdkUiResourceItem>(item));
            }
            return obj;
        }
    };

    class PublishDronePsdkUiResourceTopic : public PublishTopicWrapper {
    public:
        DronePsdkUiResourceMsg msg;

        [[nodiscard]] DronePsdkUiResource getPodClass() const {
            return static_cast<DronePsdkUiResource>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDronePsdkUiResourceTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDronePsdkUiResourceTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
