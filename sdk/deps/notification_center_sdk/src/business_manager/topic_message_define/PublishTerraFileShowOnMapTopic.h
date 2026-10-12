// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_model_file_show_on_map"
    // 文件展示地图状态变更推送

    /// TerraFileShowOnMapItemMsg — 展示地图文件项消息体
    class TerraFileShowOnMapItemMsg {
    public:
        long long id = 0;       // File 记录 ID
        std::string name;       // 文件名
        int file_type = 0;      // 文件类型
        int sub_file_type = 0;  // 子文件类型
        std::string preview_url; // 预览图签名 URL

        AIGC_JSON_HELPER(id, name, file_type, sub_file_type, preview_url);

        explicit operator TerraFileShowOnMapItem() const {
            TerraFileShowOnMapItem res{};
            res.id           = id;
            res.name         = name;
            res.file_type    = file_type;
            res.sub_file_type = sub_file_type;
            res.preview_url  = preview_url;
            return res;
        }
    };

    /// TerraFileShowOnMapMsg — 文件展示地图状态消息体
    class TerraFileShowOnMapMsg {
    public:
        std::vector<TerraFileShowOnMapItemMsg> list;

        AIGC_JSON_HELPER(list);

        explicit operator TerraFileShowOnMap() const {
            TerraFileShowOnMap res{};
            for (const auto& item : list) {
                res.list.push_back(static_cast<TerraFileShowOnMapItem>(item));
            }
            return res;
        }
    };

    /// Topic 包装类
    class PublishTerraFileShowOnMapTopic : public PublishTopicWrapper {
    public:
        TerraFileShowOnMapMsg msg;

        [[nodiscard]] TerraFileShowOnMap getPodClass() const {
            return static_cast<TerraFileShowOnMap>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishTerraFileShowOnMapTopic(const std::shared_ptr<PublishTopicWrapper>& publish_msg) {
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
                LOG_ERROR("PublishTerraFileShowOnMapTopic ctor error, err: %s, json: %s",
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
