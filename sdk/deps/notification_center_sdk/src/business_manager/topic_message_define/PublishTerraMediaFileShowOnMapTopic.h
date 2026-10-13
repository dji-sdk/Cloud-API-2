// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_media_file_show_on_map"
    // 模型显示地图推送（BatchShowOnMap 触发，来自 file-storage）

    /// 地理坐标
    class TerraMediaFileShowOnMapGeoMsg {
    public:
        double lat = 0.0;
        double lng = 0.0;
        double abs_alt = 0.0;
        double rel_alt = 0.0;
        AIGC_JSON_HELPER(lat, lng, abs_alt, rel_alt);
    };

    /// 单个上图模型文件消息体
    class TerraMediaFileShowOnMapItemMsg {
    public:
        std::string name;
        int id = 0;
        TerraMediaFileShowOnMapGeoMsg shoot_position;
        TerraMediaFileShowOnMapGeoMsg photo_position;
        std::string preview_url;
        int file_type = 0;
        int sub_file_type = 0;

        AIGC_JSON_HELPER(name, id, shoot_position, photo_position, preview_url, file_type, sub_file_type);

        explicit operator TerraMediaFileShowOnMapItem() const {
            TerraMediaFileShowOnMapItem res{};
            res.name           = name;
            res.id             = id;
            res.shoot_lat      = shoot_position.lat;
            res.shoot_lng      = shoot_position.lng;
            res.shoot_abs_alt  = shoot_position.abs_alt;
            res.shoot_rel_alt  = shoot_position.rel_alt;
            res.photo_lat      = photo_position.lat;
            res.photo_lng      = photo_position.lng;
            res.photo_abs_alt  = photo_position.abs_alt;
            res.photo_rel_alt  = photo_position.rel_alt;
            res.preview_url    = preview_url;
            res.file_type      = file_type;
            res.sub_file_type  = sub_file_type;
            return res;
        }
    };

    /// 显示地图消息体
    class TerraMediaFileShowOnMapMsgWrapper {
    public:
        std::vector<TerraMediaFileShowOnMapItemMsg> list;
        AIGC_JSON_HELPER(list);

        explicit operator TerraMediaFileShowOnMap() const {
            TerraMediaFileShowOnMap res{};
            for (const auto& item : list) {
                res.list.push_back(static_cast<TerraMediaFileShowOnMapItem>(item));
            }
            return res;
        }
    };

    /// Topic 包装类
    class PublishTerraMediaFileShowOnMapTopic : public PublishTopicWrapper {
    public:
        TerraMediaFileShowOnMapMsgWrapper msg;

        [[nodiscard]] TerraMediaFileShowOnMap getPodClass() const {
            return static_cast<TerraMediaFileShowOnMap>(msg);
        }

        bool isValid() override { return is_valid_; }

        explicit PublishTerraMediaFileShowOnMapTopic(const std::shared_ptr<PublishTopicWrapper>& publish_msg) {
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
                LOG_ERROR("PublishTerraMediaFileShowOnMapTopic ctor error, err: %s, json: %s",
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
