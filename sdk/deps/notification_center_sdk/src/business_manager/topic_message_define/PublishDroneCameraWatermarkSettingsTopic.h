// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 飞机相机水印设置(topic_drone_camera_watermark_settings);
    // 注意: 与 DRC CameraWatermarkSettingsMsg 不同 (无 payload_index / manufacturer_enable), 不要复用
    class DroneCameraWatermarkSettingsPushMsg {
    public:
        int64_t global_enable = 0;
        int64_t drone_type_enable = 0;
        int64_t drone_sn_enable = 0;
        int64_t datetime_enable = 0;
        int64_t gps_enable = 0;
        int64_t user_custom_string_enable = 0;
        std::string user_custom_string;
        int64_t layout = 0;

        AIGC_JSON_HELPER(global_enable, drone_type_enable, drone_sn_enable, datetime_enable, gps_enable, user_custom_string_enable, user_custom_string, layout);

        explicit operator DroneCameraWatermarkSettingsPush() const {
            DroneCameraWatermarkSettingsPush obj{};
            obj.global_enable = global_enable;
            obj.drone_type_enable = drone_type_enable;
            obj.drone_sn_enable = drone_sn_enable;
            obj.datetime_enable = datetime_enable;
            obj.gps_enable = gps_enable;
            obj.user_custom_string_enable = user_custom_string_enable;
            obj.user_custom_string = user_custom_string;
            obj.layout = layout;
            return obj;
        }
    };

    class PublishDroneCameraWatermarkSettingsTopic : public PublishTopicWrapper {
    public:
        DroneCameraWatermarkSettingsPushMsg msg;

        [[nodiscard]] DroneCameraWatermarkSettingsPush getPodClass() const {
            return static_cast<DroneCameraWatermarkSettingsPush>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDroneCameraWatermarkSettingsTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDroneCameraWatermarkSettingsTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
