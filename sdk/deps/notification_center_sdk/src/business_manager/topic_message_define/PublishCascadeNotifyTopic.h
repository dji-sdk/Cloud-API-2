// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // wire(服务端实际): message_data = postToNode 的 body 原样，如 {"case":"x","ts":1}
    // wire(早期约定):   {"payload":"<bytes, JSON-string 或 base64>"}，两种均兼容，见 ExtractPayload。
    // v1.1 §跨节点通道消息：SDK 不解析 payload，与 postToNode 的 body 语义对称。
    class CascadeNotifyEventMsg {
    public:
        std::string payload;

        AIGC_JSON_HELPER(payload);

        explicit operator CascadeNotifyEvent() const {
            CascadeNotifyEvent event{};
            event.payload = payload;
            return event;
        }
    };

    class PublishCascadeNotifyTopic : public PublishTopicWrapper {
    public:
        CascadeNotifyEventMsg msg;

        [[nodiscard]] CascadeNotifyEvent getPodClass() const {
            return static_cast<CascadeNotifyEvent>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishCascadeNotifyTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
            message_topic = publish_msg->message_topic;
            message_data = publish_msg->message_data;
            need_replay = publish_msg->need_replay;
            message_id = publish_msg->message_id;
            message_type = publish_msg->message_type;
            timestamp = publish_msg->timestamp;
            version = publish_msg->version;
            device_sn = publish_msg->device_sn;
            subscribe_id = publish_msg->subscribe_id;

            // 兼容两种 wire（SDK 不解析业务 payload，与 postToNode 的 body 语义对称）：
            //  1) 服务端实际行为：message_data 即 postToNode 的 body 原样（任意 JSON）-> payload = 原始 message_data
            //  2) 早期约定：{"payload":"<string>"}（仅此一个字符串字段）-> payload = 该字符串
            // 旧实现只认 2)，服务端按 1) 下发时 payload 恒为空串。
            msg.payload = ExtractPayload(publish_msg->message_data);
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
        static std::string ExtractPayload(const std::string &message_data) {
            rapidjson::Document doc;
            doc.Parse(message_data.c_str());
            if (!doc.HasParseError() && doc.IsObject() && doc.MemberCount() == 1 &&
                doc.HasMember("payload") && doc["payload"].IsString()) {
                return std::string(doc["payload"].GetString(), doc["payload"].GetStringLength());
            }
            return message_data;
        }

        bool is_valid_ = true;
    };
}