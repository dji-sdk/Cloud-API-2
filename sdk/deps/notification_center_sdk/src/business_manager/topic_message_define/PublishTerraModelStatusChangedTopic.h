// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_model_status_changed"
    // 模型状态变更推送

    /// TerraModelStatusChangedMsg — 模型状态变更消息体
    class TerraModelStatusChangedMsg {
    public:
        long long file_id = 0;  // 关联的 File 记录 ID
        std::string uuid;       // 关联的 File 记录 UUID
        std::string name;       // 文件名
        int state = 0;          // 模型新状态
        int file_type = 0;      // 文件类型
        int sub_file_type = 0;  // 子文件类型
        int error_code = 0;     // 错误码（0 表示无错误）

        AIGC_JSON_HELPER(file_id, uuid, name, state, file_type, sub_file_type, error_code);

        explicit operator TerraModelStatusChanged() const {
            TerraModelStatusChanged res{};
            res.file_id      = file_id;
            res.uuid         = uuid;
            res.name         = name;
            res.state        = state;
            res.file_type    = file_type;
            res.sub_file_type = sub_file_type;
            res.error_code   = error_code;
            return res;
        }
    };

    /// Topic 包装类
    class PublishTerraModelStatusChangedTopic : public PublishTopicWrapper {
    public:
        TerraModelStatusChangedMsg msg;

        [[nodiscard]] TerraModelStatusChanged getPodClass() const {
            return static_cast<TerraModelStatusChanged>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishTerraModelStatusChangedTopic(const std::shared_ptr<PublishTopicWrapper>& publish_msg) {
            message_topic = publish_msg->message_topic;
            message_data  = publish_msg->message_data;
            need_replay   = publish_msg->need_replay;
            message_id    = publish_msg->message_id;
            message_type  = publish_msg->message_type;
            timestamp     = publish_msg->timestamp;
            version       = publish_msg->version;
            device_sn     = publish_msg->device_sn;
            subscribe_id  = publish_msg->subscribe_id;

            std::string err;
            aigc::JsonHelper::JsonToObject(msg, publish_msg->message_data, {}, &err);
            if (!err.empty()) {
                LOG_ERROR("PublishTerraModelStatusChangedTopic ctor error, err: %s, json: %s",
                          err.c_str(), publish_msg->ToJsonString().c_str());
                is_valid_ = false;
            }
        }

        AIGC_JSON_HELPER(msg);
        AIGC_JSON_HELPER_BASE((PublishTopicWrapper*)this)

        std::string ToJsonString() override {
            std::string err, json_string;
            aigc::JsonHelper::ObjectToJson(*this, json_string, &err);
            return json_string;
        }

        void FromJsonString(const std::string& json) override {
            std::string err;
            aigc::JsonHelper::JsonToObject(*this, json, {}, &err);
        }

    private:
        bool is_valid_ = true;
    };

} // namespace notification_center
