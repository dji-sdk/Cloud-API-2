// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // eSIM 信息;
    class EsimInfoItemMsg {
    public:
        bool enabled = false;
        std::string iccid;
        int64_t telecom_operator = 0;

        AIGC_JSON_HELPER(enabled, iccid, telecom_operator);

        explicit operator EsimInfoItem() const {
            EsimInfoItem obj{};
            obj.enabled = enabled;
            obj.iccid = iccid;
            obj.telecom_operator = telecom_operator;
            return obj;
        }
    };

    // SIM 卡信息;
    class SimInfoItemMsg {
    public:
        std::string iccid;
        int64_t telecom_operator = 0;

        AIGC_JSON_HELPER(iccid, telecom_operator);

        explicit operator SimInfoItem() const {
            SimInfoItem obj{};
            obj.iccid = iccid;
            obj.telecom_operator = telecom_operator;
            return obj;
        }
    };

    // Dongle 信息项;
    class DongleInfoItemMsg {
    public:
        int64_t dongle_type = 0;
        std::string eid;
        int64_t esim_activate_state = 0;
        std::vector<EsimInfoItemMsg> esim_infos;
        std::string imei;
        int64_t sim_card_state = 0;
        int64_t sim_slot = 0;
        SimInfoItemMsg sim_info;
        int64_t telecom_operator = 0;

        AIGC_JSON_HELPER(dongle_type, eid, esim_activate_state, esim_infos, imei, sim_card_state, sim_slot, sim_info, telecom_operator);

        explicit operator DongleInfoItem() const {
            DongleInfoItem obj{};
            obj.dongle_type = dongle_type;
            obj.eid = eid;
            obj.esim_activate_state = esim_activate_state;
            for (const auto &item : esim_infos) {
                obj.esim_infos.push_back(static_cast<EsimInfoItem>(item));
            }
            obj.imei = imei;
            obj.sim_card_state = sim_card_state;
            obj.sim_slot = sim_slot;
            obj.sim_info = static_cast<SimInfoItem>(sim_info);
            obj.telecom_operator = telecom_operator;
            return obj;
        }
    };

    // 机场4G Dongle信息(topic_dock_dongle_infos);
    class DockDongleInfosMsg {
    public:
        std::vector<DongleInfoItemMsg> dongle_infos;

        AIGC_JSON_HELPER(dongle_infos);

        explicit operator DockDongleInfos() const {
            DockDongleInfos obj{};
            for (const auto &item : dongle_infos) {
                obj.dongle_infos.push_back(static_cast<DongleInfoItem>(item));
            }
            return obj;
        }
    };

    class PublishDockDongleInfosTopic : public PublishTopicWrapper {
    public:
        DockDongleInfosMsg msg;

        [[nodiscard]] DockDongleInfos getPodClass() const {
            return static_cast<DockDongleInfos>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockDongleInfosTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockDongleInfosTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
