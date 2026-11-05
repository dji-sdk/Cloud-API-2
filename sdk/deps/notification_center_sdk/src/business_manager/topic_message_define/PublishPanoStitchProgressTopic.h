// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_pano_stitch_progress";
    // 3X 超清全景拼接进度推送（PaaS 通道，经云端通知服务下发）;

    class PanoStitchProgressMsg {
    public:
        /* 注意对象的名字和 json key 对齐, 不能修改, 否则会解析失败; */
        std::string workspace_id;
        int64_t     file_id = -1;
        std::string name;
        int pano_stitch_state    = 0;
        int pano_stitch_progress = 0;
        int pano_error_code      = 0;

        AIGC_JSON_HELPER(workspace_id, file_id, name,
                         pano_stitch_state, pano_stitch_progress, pano_error_code);

        explicit operator PanoStitchProgress() const {
            PanoStitchProgress res{};
            res.workspace_id         = workspace_id;
            res.file_id              = file_id;
            res.name                 = name;
            res.pano_stitch_state    = pano_stitch_state;
            res.pano_stitch_progress = pano_stitch_progress;
            res.pano_error_code      = pano_error_code;
            return res;
        }
    };


    // topic define;
    class PublishPanoStitchProgressTopic : public PublishTopicWrapper {
    public:
        PanoStitchProgressMsg msg;

        [[nodiscard]] PanoStitchProgress getPodClass() const {
            return static_cast<PanoStitchProgress>(msg);
        }

        bool isValid() override { return is_valid_; }

        explicit PublishPanoStitchProgressTopic(const std::shared_ptr<PublishTopicWrapper>& publish_msg) {
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
                LOG_ERROR("PublishPanoStitchProgressTopic ctor error, err: %s, json: %s",
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
}
