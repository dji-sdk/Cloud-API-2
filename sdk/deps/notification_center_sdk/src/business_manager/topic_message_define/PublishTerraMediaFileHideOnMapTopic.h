// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_media_file_hide_on_map"
    // 模型隐藏地图推送（Delete/BatchDelete 触发，来自 file-storage）

    /// TerraMediaFileHideOnMapMsg — 隐藏地图消息体
    class TerraMediaFileHideOnMapMsg {
    public:
        std::vector<int> ids;  // 被隐藏的模型文件 ID 列表（uint32）

        AIGC_JSON_HELPER(ids);

        explicit operator TerraMediaFileHideOnMap() const {
            TerraMediaFileHideOnMap res{};
            res.ids = ids;
            return res;
        }
    };

    /// Topic 包装类
    class PublishTerraMediaFileHideOnMapTopic : public PublishTopicWrapper {
    public:
        TerraMediaFileHideOnMapMsg msg;

        [[nodiscard]] TerraMediaFileHideOnMap getPodClass() const {
            return static_cast<TerraMediaFileHideOnMap>(msg);
        }

        bool isValid() override { return is_valid_; }

        explicit PublishTerraMediaFileHideOnMapTopic(const std::shared_ptr<PublishTopicWrapper>& publish_msg) {
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
                LOG_ERROR("PublishTerraMediaFileHideOnMapTopic ctor error, err: %s, json: %s",
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
