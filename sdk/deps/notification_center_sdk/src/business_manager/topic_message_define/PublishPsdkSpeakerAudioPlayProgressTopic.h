// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class PsdkSpeakerProgressInfoMsg {
    public:
        int percent = 0;
        std::string step_key;

        AIGC_JSON_HELPER(percent, step_key);

        explicit operator PsdkSpeakerProgressInfo() const {
            PsdkSpeakerProgressInfo obj{};
            obj.percent = percent;
            obj.step_key = step_key;
            return obj;
        }
    };

    class PsdkSpeakerOutputInfoMsg {
    public:
        std::string md5;
        PsdkSpeakerProgressInfoMsg progress;
        int psdk_index = 0;
        std::string status;

        AIGC_JSON_HELPER(md5, progress, psdk_index, status);

        explicit operator PsdkSpeakerOutputInfo() const {
            PsdkSpeakerOutputInfo obj{};
            obj.md5 = md5;
            obj.progress = static_cast<PsdkSpeakerProgressInfo>(progress);
            obj.psdk_index = psdk_index;
            obj.status = status;
            return obj;
        }
    };

    class PsdkSpeakerPlayProgressMsg {
    public:
        std::string gateway;
        PsdkSpeakerOutputInfoMsg output;
        int result = 0;
        std::string sn;

        AIGC_JSON_HELPER(gateway, output, result, sn);

        explicit operator PsdkSpeakerPlayProgress() const {
            PsdkSpeakerPlayProgress obj{};
            obj.gateway = gateway;
            obj.output = static_cast<PsdkSpeakerOutputInfo>(output);
            obj.result = result;
            obj.sn = sn;
            return obj;
        }
    };

    class PublishPsdkSpeakerAudioPlayProgressTopic : public PublishTopicWrapper {
    public:
        PsdkSpeakerPlayProgressMsg msg;

        [[nodiscard]] PsdkSpeakerPlayProgress getPodClass() const {
            return static_cast<PsdkSpeakerPlayProgress>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishPsdkSpeakerAudioPlayProgressTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishPsdkSpeakerAudioPlayProgressTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
