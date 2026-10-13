// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"


namespace notification_center {
    // Dock HMS 参数信息;
    class DockHmsArgsMsg {
    public:
        int component_index = -1;        // int, 文案变量, 默认无效值(-1);
        int sensor_index = -1;           // int, 文案变量, 默认无效值(-1);
        std::string device_id;           // string, 告警产生的设备ID, 无效值: 空值;
        std::string sender_host_id;      // string, 告警产生的模块ID, 无效值: 空值;
        int ignore = 0;                  // int, 告警是否忽略, {0:不忽略, 1: 忽略};
        int alarm_info = 0;              // int, 模块自定义的告警信息;
        uint64_t create_timestamp = 0;   // uint64_t, 故障产生的UTC时间(单位秒); 

        int display_scene = 0;           // int, 0: 正式错误码, 1: 调试错误码, 只用于排障;

        AIGC_JSON_HELPER(component_index, sensor_index, device_id, sender_host_id, ignore, alarm_info, create_timestamp, display_scene);

        explicit operator HmsArgs() const {
            HmsArgs hms_args{};

            hms_args.component_index = component_index;
            hms_args.sensor_index = sensor_index;
            hms_args.device_id = device_id;
            hms_args.sender_host_id = sender_host_id;
            hms_args.ignore = ignore;
            hms_args.alarm_info = alarm_info;
            hms_args.create_timestamp = create_timestamp;
            hms_args.display_scene = display_scene;

            return hms_args;
        }
    };


    // Dock HMS 基础告警信息;
    class DockHmsDataBasicsMsg {
    public:
        int level = 0;                      // int, 告警等级, {"0":"通知","1":"提醒","2":"警告"};
        int module_id = -1;                 // int, 事件模块, {"0":"飞行任务","1":"设备管理","2":"媒体","3":"hms"};
        std::string code;                   // string, 告警码, {无效值: 空值};
        std::string device_type;            // string, 设备类型, 上云API格式:{domain-type-subtype},可以根据产品支持查询, {无效值: 空值};
        std::string domain_type;            // string;
        int in_the_sky = 0;                 // int, 是否飞行, {"0":"在地上", "1":"在天上"};
        int imminent = 0;                   // int, 是否为及时性的, {"0":"否", "1":"是"};
        DockHmsArgsMsg args;                // HmsArgs, 告警参数信息;
        std::string status_key;             // string;
        std::string joint_key;              // string;
        int id = 0;                         // int, 每个设备上报事件里hms的唯一id;
        int parent_id = 0;                  // int, 每个设备上报事件里hms的唯一父id;

        AIGC_JSON_HELPER(level, module_id, code, device_type, domain_type, in_the_sky, imminent, args, status_key, joint_key, id, parent_id);


        explicit operator HmsDataBasics() const {
            HmsDataBasics hms_data_basics{};
            
            hms_data_basics.level = level;
            hms_data_basics.module_id = module_id;
            hms_data_basics.code = code;
            hms_data_basics.device_type = device_type;
            hms_data_basics.domain_type = domain_type;
            hms_data_basics.in_the_sky = in_the_sky;
            hms_data_basics.imminent = imminent;
            hms_data_basics.args = static_cast<HmsArgs>(args);
            hms_data_basics.status_key = status_key;
            hms_data_basics.joint_key = joint_key;
            hms_data_basics.id = id;
            hms_data_basics.parent_id = parent_id;

            return hms_data_basics;
        }
    };


    // Dock HMS 告警信息;
    class DockHmsDataItemMsg {
    public:
        DockHmsDataBasicsMsg               main_hms_data;  // json key;
        std::vector<DockHmsDataBasicsMsg>  sub_hms_list;   // json key;


        AIGC_JSON_HELPER(main_hms_data, sub_hms_list);

        explicit operator HmsDataItem() const {
            HmsDataItem hms_data_item{};
            hms_data_item.basics = static_cast<HmsDataBasics>(main_hms_data);

            for (const auto &sub_hms : sub_hms_list) {
                hms_data_item.sub_hms_data.emplace_back(static_cast<HmsDataBasics>(sub_hms));
            }

            return hms_data_item;
        }
    };

    // Dock HMS 告警信息列表;
    class DockHmsDataMsg {
    public:
        std::vector<DockHmsDataItemMsg>  hms_data_list;  // json key;

        AIGC_JSON_HELPER(hms_data_list);

        explicit operator HmsData() const {
            HmsData hms_data{};

            for (const auto &data_item_msg : hms_data_list) {
                hms_data.hms_data_items.emplace_back(static_cast<HmsDataItem>(data_item_msg));
            }

            return hms_data;
        }
    };


    class PublishDockHmsTopic: public PublishTopicWrapper {
    public:
        DockHmsDataMsg msg;

        [[nodiscard]] HmsData getPodClass() const {
            return static_cast<HmsData>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishDockHmsTopic(const std::shared_ptr<PublishTopicWrapper> &publish_msg) {
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
                LOG_ERROR("PublishDockHmsTopic ctor error, err: %s, json: %s", err.c_str(), publish_msg->ToJsonString().c_str());
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