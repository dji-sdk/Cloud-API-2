// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    class ElementCreateMsgWrapper {
    public:
        std::string id; std::string group_id; std::string name; int display=0;
        AIGC_JSON_HELPER(id, group_id, name, display);
        explicit operator ElementCreate() const {
            ElementCreate r; r.id=id; r.group_id=group_id; r.name=name; r.display=display; return r;
        }
    };
    class PublishElementCreateTopic : public PublishTopicWrapper {
    public:
        ElementCreateMsgWrapper msg;
        [[nodiscard]] ElementCreate getPodClass() const { return static_cast<ElementCreate>(msg); }
        bool isValid() override { return is_valid_; }
        explicit PublishElementCreateTopic(const std::shared_ptr<PublishTopicWrapper>& p) {
            message_topic=p->message_topic; message_data=p->message_data; need_replay=p->need_replay;
            message_id=p->message_id; message_type=p->message_type; timestamp=p->timestamp;
            version=p->version; device_sn=p->device_sn; subscribe_id=p->subscribe_id;
            std::string err; aigc::JsonHelper::JsonToObject(msg, p->message_data, {}, &err);
            if (!err.empty()) { LOG_ERROR("PublishElementCreateTopic ctor error, err: %s, json: %s", err.c_str(), p->ToJsonString().c_str()); is_valid_=false; }
        }
        AIGC_JSON_HELPER(msg); AIGC_JSON_HELPER_BASE((PublishTopicWrapper*)this)
        std::string ToJsonString() override { std::string e,s; aigc::JsonHelper::ObjectToJson(*this,s,&e); return s; }
        void FromJsonString(const std::string& j) override { std::string e; aigc::JsonHelper::JsonToObject(*this,j,{},&e); }
    private: bool is_valid_=true;
    };
}
