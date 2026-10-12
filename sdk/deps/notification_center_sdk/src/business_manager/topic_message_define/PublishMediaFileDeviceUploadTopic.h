// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_media_file_device_upload";
    // 设备媒体回传通知;
    // MediaFileDeviceUpload;
    class MediaFileDeviceUploadMsg {
    public:
        int64_t file_id = -1;                // 文件 id;
        std::string flight_id;               // 飞行任务 id;
        std::string device_sn;               // 设备序列号 SN;
        std::string object_key;              // 文件对象 key;
        int64_t object_size = 0;             // 文件大小(字节);
        int file_type = -1;                  // 文件类型，2：图片、3：视频;

        // register class members;
        AIGC_JSON_HELPER(file_id, flight_id, device_sn, object_key, object_size, file_type);

        // explicit type cast;
        explicit operator MediaFileDeviceUpload() const {
            MediaFileDeviceUpload res{};

            res.file_id = file_id;
            res.flight_id = flight_id;
            res.device_sn = device_sn;
            res.object_key = object_key;
            res.object_size = object_size;
            res.file_type = file_type;

            return res;
        }
    };


    // topic define;
    class PublishMediaFileDeviceUploadTopic: public PublishTopicWrapper {
    public:
        MediaFileDeviceUploadMsg msg;

        [[nodiscard]] MediaFileDeviceUpload getPodClass() const {
            return static_cast<MediaFileDeviceUpload>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishMediaFileDeviceUploadTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishMediaFileDeviceUploadTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

