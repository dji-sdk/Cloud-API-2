// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // 机场: 子设备信息;
    class DockSubDeviceInfoMsg {
    public:
        std::string device_sn;           // string, "子设备序列号(SN)";
        std::string device_model_key;    // string,"子设备枚举值", {domain-type-subtype};
        int device_online_status = 0;    // int,"机场停机坪上的飞行器开机状态", { 0: 关机, 1: 开机 };
        int device_paired = 0;           // int,"机场停机坪上的飞行器是否与机场对频", { 0: 未对频, 1: 已对频 };

        AIGC_JSON_HELPER(device_sn, device_model_key, device_online_status, device_paired);

        explicit operator DockSubDeviceInfo() const {
            DockSubDeviceInfo sub_device_info {};

            sub_device_info.device_sn = device_sn;
            sub_device_info.device_model_key = device_model_key;
            sub_device_info.device_online_status = device_online_status;
            sub_device_info.device_paired = device_paired;

            return sub_device_info;
        }
    };

    // 机场: 子设备信息列表;
    class DockSubDeviceInfoListMsg {
    public:
        /* 注意对象的名字和 json key对齐, 不能修改, 否则会解析失败; */
        std::vector<DockSubDeviceInfoMsg> sub_device_list;

        AIGC_JSON_HELPER(sub_device_list);

        explicit operator DockSubDeviceInfoList() const {
            DockSubDeviceInfoList sub_device_info_list {};

            for (const auto & sub_dev_info_msg: sub_device_list) {
                sub_device_info_list.sub_device_info_vec.emplace_back(static_cast<DockSubDeviceInfo>(sub_dev_info_msg));
            }

            return sub_device_info_list;
        }
    };


    class PublishDockSubDeviceTopic: public PublishTopicWrapper {
    public:
        DockSubDeviceInfoListMsg msg;

        [[nodiscard]] DockSubDeviceInfoList getPodClass() const {
            return static_cast<DockSubDeviceInfoList>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockSubDeviceTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockSubDeviceTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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
