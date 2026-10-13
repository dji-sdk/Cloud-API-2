// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <cstdint>
#include <string>
#include <AIGCJson.hpp>
#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

#include "Common.h"

#include "base/common/logger.h"
#include "base_utils/utils/cpp_uuid.h"

namespace notification_center {
    class TopicMessageWrapper {
    public:
        virtual ~TopicMessageWrapper() = default;

        virtual bool isValid() {
            if (message_type.empty()) {
                LOG_ERROR("TopicMessageWrapper isValid: message_type.empty()");
                return false;
            }
            if (message_type != "subscribe" &&
                message_type != "unsubscribe" &&
                message_type != "publish" &&
                message_type != "ack" &&
                message_type != "ping" &&
                message_type != "pong") {
                LOG_ERROR("TopicMessageWrapper isValid: message_type unknown: %s", message_type.c_str());
                return false;
            }
            if (message_id.empty()) {
                LOG_ERROR("TopicMessageWrapper isValid: message_id.empty()");
                return false;
            }
            return true;
        }

        std::string message_type; // subscribe/unsubscribe/publish/ack/ping/pong
        std::string message_id = generate_uuid_v4(); // uuid, 用于消息匹配
        uint64_t timestamp = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
        bool need_replay{false};
        std::string version = "1";

        AIGC_JSON_HELPER(message_type, message_id, timestamp, need_replay, version);

        virtual std::string ToJsonString() {
            std::string err, json_string;
            aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
            return json_string;
        }

        virtual void FromJsonString(const std::string &json) {
            std::string err;
            aigc::JsonHelper::JsonToObject(*this, json, {}, &err);
        }
    };


    class SubscribeTopicMessageData {
    public:
        std::string device_sn;
        std::string topic;
        NotificationFrequency notify_strategy = NotificationFrequency_Any;
        std::string subscribe_id;

        AIGC_JSON_HELPER(device_sn, topic, subscribe_id, notify_strategy);
    };

    class SubscribeTopicWrapper final : public TopicMessageWrapper {
    public:

        SubscribeTopicMessageData message_data;

        AIGC_JSON_HELPER(message_data);
        AIGC_JSON_HELPER_BASE((TopicMessageWrapper *)this)

        explicit SubscribeTopicWrapper(const std::string &sn, const std::string &topic, const NotificationFrequency freq, const std::string& subscribe_id) {
            message_type = "subscribe";
            need_replay = true;
            message_data.device_sn = sn;
            message_data.topic = topic;
            message_data.notify_strategy = freq;
            message_data.subscribe_id = subscribe_id;
        }

        bool isValid() override {
            if (TopicMessageWrapper::isValid() == false) {
                return false;
            }
            return true;
        }

        std::string ToJsonString() override {
            std::string err, json_string;
            aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
            return json_string;
        }

        void FromJsonString(const std::string &json) override {
            std::string err;
            aigc::JsonHelper::JsonToObject(*this, json, {}, &err);
        }
    };

    class SubscribeTopicReplyWrapper final : public TopicMessageWrapper {
    public:
        class SubscribeTopicReplyMessageData {
        public:
            int code = 0;
            std::string message;
            std::string subscribe_id;

            AIGC_JSON_HELPER(code, message, subscribe_id);
        };

        SubscribeTopicReplyMessageData message_data;

        AIGC_JSON_HELPER(message_data);
        AIGC_JSON_HELPER_BASE((TopicMessageWrapper *)this)

        std::string ToJsonString() override {
            std::string err, json_string;
            aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
            return json_string;
        }

        void FromJsonString(const std::string &json) override {
            std::string err;
            aigc::JsonHelper::JsonToObject(*this, json, {}, &err);
        }
    };

    class UnSubscribeTopicWrapper : public TopicMessageWrapper {
    public:

        SubscribeTopicMessageData message_data;

        AIGC_JSON_HELPER(message_data);
        AIGC_JSON_HELPER_BASE((TopicMessageWrapper *)this)

        explicit UnSubscribeTopicWrapper(const std::string &sn, const std::string &topic, const NotificationFrequency freq, const std::string& subscribe_id) {
            message_type = "unsubscribe";
            need_replay = true;
            message_data.device_sn = sn;
            message_data.topic = topic;
            message_data.notify_strategy = freq;
            message_data.subscribe_id = subscribe_id;
        }

        bool isValid() override {
            if (TopicMessageWrapper::isValid() == false) {
                return false;
            }
            return true;
        }

        std::string ToJsonString() override {
            std::string err, json_string;
            aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
            return json_string;
        }

        void FromJsonString(const std::string &json) override {
            std::string err;
            aigc::JsonHelper::JsonToObject(*this, json, {}, &err);
        }
    };

    class PublishTopicWrapper : public TopicMessageWrapper {
    public:
        bool isValid() override {
            if (TopicMessageWrapper::isValid() == false) {
                LOG_ERROR("PublishTopicWrapper isValid: TopicMessageWrapper::isValid() == false");
                return false;
            }
            if (message_topic.empty()) {
                LOG_ERROR("PublishTopicWrapper isValid: message_topic.empty()");
                return false;
            }
            return true;
        }

        std::string device_sn;
        std::string message_topic;
        std::string subscribe_id;

        std::string message_data; // 这个保存原始的数据，不进行 json 操作, 交给用户进行解析

        AIGC_JSON_HELPER(device_sn, message_topic, subscribe_id, message_data);

        AIGC_JSON_HELPER_BASE((TopicMessageWrapper *)this)

        std::string ToJsonString() override {
            std::string err, json_string;
            aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
            return json_string;
        }

        void FromJsonString(const std::string &json) override {
            std::string err;
            aigc::JsonHelper::JsonToObject(*this, json, {}, &err);

            // 使用rapidjson直接从JSON字符串中提取message_data字段
            rapidjson::Document doc;
            doc.Parse(json.c_str());

            if (message_data.empty() == false) {
                return;
            }

            if (!doc.HasParseError() && doc.HasMember("message_data")) {
                const rapidjson::Value &message_data_value = doc["message_data"];
                if (message_data_value.IsString()) {
                    message_data = message_data_value.GetString();
                } else {
                    // 如果不是字符串，将其转换为JSON字符串
                    rapidjson::StringBuffer buffer;
                    rapidjson::Writer<rapidjson::StringBuffer> writer(buffer);
                    message_data_value.Accept(writer);
                    message_data = buffer.GetString();
                }
            }
            LOG_WARN("PublishTopicWrapper FromJsonString message_data: %s", message_data.c_str());
        }
    };

    class PublishAckTopicWrapper final : public TopicMessageWrapper {
    public:
        explicit PublishAckTopicWrapper() {
            message_type = "ack";
        }

        bool isValid() override {
            if (TopicMessageWrapper::isValid() == false) {
                LOG_ERROR("PublishAckTopicWrapper isValid: TopicMessageWrapper::isValid() == false");
                return false;
            }
            return true;
        }

        std::string ToJsonString() override {
            return TopicMessageWrapper::ToJsonString();
        }

        void FromJsonString(const std::string &json) override {
            TopicMessageWrapper::FromJsonString(json);
        }
    };

    class PingTopicWrapper final : public TopicMessageWrapper {
    public:
        explicit PingTopicWrapper() {
            message_type = "ping";
            need_replay = true; // ping 消息需要回复
        }
    };

    class PongTopicWrapper final : public TopicMessageWrapper {
    public:
        // 基于 ping 消息构造 pong 消息
        explicit PongTopicWrapper(const std::shared_ptr<PingTopicWrapper> &ping) {
            message_type = "pong";
            message_id = ping->message_id;
            timestamp = ping->timestamp; // 填 ping 包的时间戳，ping 包发送方可以计算 RTT
            need_replay = false; // pong 消息不需要回复
            version = ping->version;
        }

        explicit PongTopicWrapper(const std::string &json) {
            TopicMessageWrapper::FromJsonString(json);
        }
    };
}
