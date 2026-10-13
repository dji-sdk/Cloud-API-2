// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <notification_center_sdk_api/Message.h>
#include "topic_engine/TopicMessageWrapper.h"

namespace notification_center {
    // "topic_reconstruction_status_changed_event"
    // 重建任务状态变更通知

    /// JobInfo — 单个任务信息
    class ReconJobInfoMsg {
    public:
        int type = 0;           // 任务类型（14=2D, 15=3D, 24=3DGS, 25=Lidar, 12=SfM, 23=ZIP 等）
        std::string uuid;       // 异步平台实例 ID
        int job_status = 0;     // 任务状态（1=申请中, 3=执行中, 6=成功, 7=失败, 8=停止中）
        int percent = 0;        // 进度百分比（0-100）
        int code = 0;           // 错误码（omitempty）

        AIGC_JSON_HELPER(type, uuid, job_status, percent, code);

        explicit operator ReconJobInfo() const {
            ReconJobInfo res{};
            res.type       = type;
            res.uuid       = uuid;
            res.job_status = job_status;
            res.percent    = percent;
            res.code       = code;
            return res;
        }
    };

    /// ReconstructionStatusChanged — 重建任务状态变更消息体
    class ReconstructionStatusChangedMsg {
    public:
        std::string workspace_id;           // 项目 UUID
        uint64_t id = 0;                    // 文件 ID
        std::string name;                   // 文件名
        int state = 0;                      // 模型状态码
        std::string uuid;                   // 文件 UUID
        int file_type = 0;                  // 文件类型
        int sub_file_type = 0;              // 子文件类型（格式标识）
        int code = 0;                       // 错误码，仅失败时填充
        int64_t size = 0;                   // 文件大小（bytes）
        ReconJobInfoMsg job_info;           // 当前触发的任务信息
        std::vector<ReconJobInfoMsg> job_infos; // 所有关联任务列表
        uint32_t fast_reconstruction_id = 0;   // 快速重建关联 ID

        AIGC_JSON_HELPER(workspace_id, id, name, state, uuid, file_type, sub_file_type,
                         code, size, job_info, job_infos, fast_reconstruction_id);

        explicit operator ReconstructionStatusChanged() const {
            ReconstructionStatusChanged res{};
            res.workspace_id         = workspace_id;
            res.id                   = id;
            res.name                 = name;
            res.state                = state;
            res.uuid                 = uuid;
            res.file_type            = file_type;
            res.sub_file_type        = sub_file_type;
            res.code                 = code;
            res.size                 = size;
            res.job_info             = static_cast<ReconJobInfo>(job_info);
            res.fast_reconstruction_id = fast_reconstruction_id;
            for (const auto& j : job_infos) {
                res.job_infos.push_back(static_cast<ReconJobInfo>(j));
            }
            return res;
        }
    };

    /// Topic 包装类
    class PublishReconstructionStatusChangedTopic : public PublishTopicWrapper {
    public:
        ReconstructionStatusChangedMsg msg;

        [[nodiscard]] ReconstructionStatusChanged getPodClass() const {
            return static_cast<ReconstructionStatusChanged>(msg);
        }

        bool isValid() override {
            return is_valid_;
        }

        explicit PublishReconstructionStatusChangedTopic(const std::shared_ptr<PublishTopicWrapper>& publish_msg) {
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
                LOG_ERROR("PublishReconstructionStatusChangedTopic ctor error, err: %s, json: %s",
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
