// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "stream_impl.h"

#include <memory>
#include <utility>

#include "module/custom_sei_module/custom_sei_module_impl.h"
#include "module/pull_stream_module/pull_stream_module_impl.h"
#include "common/executors/executors.h"
#include <base/common/logger.h>
#include "message_engine_factory.h"

namespace flight_hub_sdk {

StreamImpl::StreamImpl(
    const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge,
    const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge)
    : rtc_channel_manager_bridge_(rtc_channel_manager_bridge),
      mqtt_client_manager_bridge_(mqtt_client_manager_bridge) {
    custom_sei_module_ = std::make_shared<CustomSEIModuleImpl>();

    /// init message engine
    message_engine_ = message_engine::MessageEngineFactory::createMessageEngine(drc_sdk::common::Executors::Main());
    if (!message_engine_) {
        LOG_ERROR("StreamImpl: createMessageEngine failed");
        LOG_ERROR("StreamImpl: 程序将调用abort()终止，这可能导致服务退出");
        LOG_ERROR("StreamImpl: 请检查日志文件以获取更多信息");
        abort();
    }
    if (rtc_channel_manager_bridge_) {
        message_engine_->setRtcChannelManager(rtc_channel_manager_bridge_);
    }
    if (mqtt_client_manager_bridge_) {
        message_engine_->setMqttClientManager(mqtt_client_manager_bridge_);
    }
    message_engine_->initialize();
}

std::shared_ptr<ICustomSEIModule> StreamImpl::getCustomSEIModule() {
    return custom_sei_module_;
}

std::shared_ptr<IPullStreamModule> StreamImpl::getPullStreamModule(const std::string& stream_id) {
    if (stream_id.empty()) {
        LOG_ERROR("StreamImpl: stream_id不能为空");
        return nullptr;
    }
    
    if (!rtc_channel_manager_bridge_) {
        // 说明不需要使用注入的rtc_channel_manager_bridge_，使用默认的即可
    }
    
    std::lock_guard<std::mutex> lock(pull_stream_modules_mutex_);
    
    // 检查是否已存在该stream_id的模块
    if (const auto it = pull_stream_modules_.find(stream_id); it != pull_stream_modules_.end()) {
        return it->second;
    }
    
    // 创建新的PullStreamModule
    auto rtc_channel_manager = message_engine_->getRtcChannelManager();
    if (!rtc_channel_manager) {
        LOG_ERROR("StreamImpl: 无法获取RTC通道管理器");
        return nullptr;
    }
    
    auto pull_stream_module = std::make_shared<PullStreamModuleImpl>(rtc_channel_manager);
    pull_stream_modules_[stream_id] = pull_stream_module;
    
    LOG_INFO("StreamImpl: 为stream_id '%s' 创建了新的PullStreamModule", stream_id.c_str());
    return pull_stream_module;
}

} // namespace flight_hub_sdk
