// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // wire: {"nodeID":"GD","status":"enabled"}
    // v1.1 §事件推送：status 取值 enabled / disabled / deleted，不承载在线态。
    class CascadeChildNodeStatusMsg {
    public:
        std::string node_id;
        std::string status;

        AIGC_JSON_HELPER(node_id, status);
        AIGC_JSON_HELPER_RENAME("nodeID", "status");

        explicit operator CascadeChildNodeStatus() const {
            CascadeChildNodeStatus item{};
            item.node_id = node_id;
            item.status  = status;
            return item;
        }
    };

    class CascadeParentNodeStatusMsg {
    public:
        std::string node_id;
        std::string status;

        AIGC_JSON_HELPER(node_id, status);
        AIGC_JSON_HELPER_RENAME("nodeID", "status");

        explicit operator CascadeParentNodeStatus() const {
            CascadeParentNodeStatus item{};
            item.node_id = node_id;
            item.status  = status;
            return item;
        }
    };

    // wire: {"childNodeStatusList":[...],"parentNodeStatus":{...},"changedAt":1756600000}
    class CascadeNodeStatusEventMsg {
    public:
        std::vector<CascadeChildNodeStatusMsg> child_node_status_list;
        CascadeParentNodeStatusMsg parent_node_status;
        int64_t changed_at = 0;

        AIGC_JSON_HELPER(child_node_status_list, parent_node_status, changed_at);
        AIGC_JSON_HELPER_RENAME("childNodeStatusList", "parentNodeStatus", "changedAt");

        explicit operator CascadeNodeStatusEvent() const {
            CascadeNodeStatusEvent event{};
            event.child_node_status_list.reserve(child_node_status_list.size());
            for (const auto &item : child_node_status_list) {
                event.child_node_status_list.emplace_back(static_cast<CascadeChildNodeStatus>(item));
            }
            event.parent_node_status = static_cast<CascadeParentNodeStatus>(parent_node_status);
            event.changed_at = changed_at;
            return event;
        }
    };

    class PublishCascadeNodeStatusTopic : public PublishTopicWrapper {
    public:
        CascadeNodeStatusEventMsg msg;

        [[nodiscard]] CascadeNodeStatusEvent getPodClass() const {
            return static_cast<CascadeNodeStatusEvent>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishCascadeNodeStatusTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
            message_topic = publish_msg->message_topic;
            message_data = publish_msg->message_data;
            need_replay = publish_msg->need_replay;
            message_id = publish_msg->message_id;
            message_type = publish_msg->message_type;
            timestamp = publish_msg->timestamp;
            version = publish_msg->version;
            device_sn = publish_msg->device_sn;
            subscribe_id = publish_msg->subscribe_id;

            // 服务端对"无子节点 / 无父节点"的一侧下发 null（如子节点 211 收到的
            // {"childNodeStatusList":null,"parentNodeStatus":{...}}），aigc JsonToObject
            // 遇 null 判类型不符 -> 整条事件被判 invalid 丢弃。反序列化前先把 null 规范化为空值。
            const std::string data = NormalizeNullFields(publish_msg->message_data);
            std::string err;
            aigc::JsonHelper::JsonToObject(msg, data, {}, &err);
            if (!err.empty()) {
                LOG_ERROR("PublishCascadeNodeStatusTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
        // 把顶层 null 的 childNodeStatusList / parentNodeStatus 替换为 [] / {}；
        // 非法 JSON 或无需修改时原样返回，交由后续 JsonToObject 按原逻辑报错。
        static std::string NormalizeNullFields(const std::string &json) {
            rapidjson::Document doc;
            doc.Parse(json.c_str());
            if (doc.HasParseError() || !doc.IsObject()) {
                return json;
            }
            bool changed = false;
            if (doc.HasMember("childNodeStatusList") && doc["childNodeStatusList"].IsNull()) {
                doc["childNodeStatusList"].SetArray();
                changed = true;
            }
            if (doc.HasMember("parentNodeStatus") && doc["parentNodeStatus"].IsNull()) {
                doc["parentNodeStatus"].SetObject();
                changed = true;
            }
            if (!changed) {
                return json;
            }
            rapidjson::StringBuffer sb;
            rapidjson::Writer<rapidjson::StringBuffer> writer(sb);
            doc.Accept(writer);
            return sb.GetString();
        }

        bool is_valid_ = true;
    };
}