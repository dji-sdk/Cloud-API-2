// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {

    class DeviceFileUploadFileProgressMsg {
    public:
        int percent = 0;
        int current_step = 0;
        std::string status;
        int total_step = 0;
        int64_t upload_rate = 0;

        AIGC_JSON_HELPER(percent, current_step, status, total_step, upload_rate);

        explicit operator DeviceFileUploadFileProgress() const {
            DeviceFileUploadFileProgress obj{};
            obj.percent = percent;
            obj.current_step = current_step;
            obj.status = status;
            obj.total_step = total_step;
            obj.upload_rate = upload_rate;
            return obj;
        }
    };

    class DeviceFileUploadFileInfoMsg {
    public:
        std::string device_sn;
        std::string key;
        std::string module;
        DeviceFileUploadFileProgressMsg progress;
        int64_t size = 0;

        AIGC_JSON_HELPER(device_sn, key, module, progress, size);

        explicit operator DeviceFileUploadFileInfo() const {
            DeviceFileUploadFileInfo obj{};
            obj.device_sn = device_sn;
            obj.key = key;
            obj.module = module;
            obj.progress = static_cast<DeviceFileUploadFileProgress>(progress);
            obj.size = size;
            return obj;
        }
    };

    class DeviceFileUploadProgressMsg {
    public:
        std::string bid;
        int64_t timestamp = 0;
        std::string status;
        int result = 0;
        std::vector<DeviceFileUploadFileInfoMsg> files;

        AIGC_JSON_HELPER(bid, timestamp, status, result, files);

        explicit operator DeviceFileUploadProgress() const {
            DeviceFileUploadProgress obj{};
            obj.bid = bid;
            obj.timestamp = timestamp;
            obj.status = status;
            obj.result = result;
            for (const auto& f : files) {
                obj.files.push_back(static_cast<DeviceFileUploadFileInfo>(f));
            }
            return obj;
        }
    };

    class PublishDeviceFileUploadProgressTopic : public PublishTopicWrapper {
    public:
        DeviceFileUploadProgressMsg msg;

        [[nodiscard]] DeviceFileUploadProgress getPodClass() const {
            return static_cast<DeviceFileUploadProgress>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDeviceFileUploadProgressTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDeviceFileUploadProgressTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
