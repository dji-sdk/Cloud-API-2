// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // 任务异常消息;
    // topic_task_visual_scout_abnormal_info;
    // AircraftVisualScoutAbnormalInfo;
    // 注意：JSON 协议使用 "error_code"，此处用 error_code 以正确反序列化，转换时赋给 API 的 err_code
    class AircraftVisualScoutAbnormalInfoMsg {
    public:
        std::string task_id;
        int error_code;  // 与 JSON 字段 "error_code" 一致，避免未初始化导致 err_code 出现垃圾值（如 1684627301）

        AIGC_JSON_HELPER(task_id, error_code);

        explicit operator AircraftVisualScoutAbnormalInfo() const {
            AircraftVisualScoutAbnormalInfo res;
            res.task_id = task_id;
            res.err_code = error_code;
            return res;
        }
    };




    // topic 定义;
    class PublishAircraftVisualScoutAbnormalInfoTopic: public PublishTopicWrapper {
    public:
        AircraftVisualScoutAbnormalInfoMsg msg;

        [[nodiscard]] AircraftVisualScoutAbnormalInfo getPodClass() const {
            return static_cast<AircraftVisualScoutAbnormalInfo>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishAircraftVisualScoutAbnormalInfoTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishAircraftVisualScoutAbnormalInfoTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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

