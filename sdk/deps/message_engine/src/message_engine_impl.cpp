// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_engine_impl.h"
#include "rtc/rtc_channel_manager_wrapper.h"
#include "rtc/rtc_channel_manager.h"
#include "mqtt/mqtt_client_manager.h"
#include "mqtt/mqtt_client_wrapper/mqtt_client_manager_wrapper.h"
#include "cloud_message_manager.h"
#include "transport/transport_manager.h"

using namespace message_engine;

MessageEngineImpl::MessageEngineImpl(const std::shared_ptr<::drc_sdk::Executor> &working_executor) : working_executor_(working_executor) {
    transport_manager_ = std::make_shared<TransportManager>(working_executor_);
}

MessageEngineImpl::~MessageEngineImpl() {
}

bool MessageEngineImpl::initialize() {
    initialize_ = true;
    return true;
}

bool MessageEngineImpl::setRtcChannelManager(const std::shared_ptr<IRtcChannelManagerBridge>& channel_manager_bridge) {
    rtc_channel_manager_ = std::make_shared<RtcChannelManagerWrapper>(
        channel_manager_bridge, transport_manager_, working_executor_);
    return true;
}

bool MessageEngineImpl::setMqttClientManager(const std::shared_ptr<IMqttClientManagerBridge>& mqtt_client_manager_bridge) {
    if (!mqtt_client_manager_bridge) {
        return false;
    }
    mqtt_client_manager_ = std::make_shared<MqttClientManagerWrapper>(
        working_executor_,
        transport_manager_,
        mqtt_client_manager_bridge);
    return true;
}

std::shared_ptr<ICloudMessageManager> MessageEngineImpl::getCloudMessageManager() {
    if (!cloud_message_manager_) {
        cloud_message_manager_ = std::make_shared<CloudMessageManager>(transport_manager_);
    }
    return cloud_message_manager_;
}

std::shared_ptr<IRtcChannelManager> MessageEngineImpl::getRtcChannelManager() {
    if (!rtc_channel_manager_) {
        rtc_channel_manager_ = std::make_shared<RtcChannelManager>(working_executor_, transport_manager_);
    }
    return rtc_channel_manager_;
}

std::shared_ptr<IMqttClientManager> MessageEngineImpl::getMqttClientManager() {
    if (!mqtt_client_manager_) {
        mqtt_client_manager_ = std::make_shared<MqttClientManager>(working_executor_, transport_manager_);
    }
    return mqtt_client_manager_;
}

std::shared_ptr<ITransportManager> MessageEngineImpl::getTransportManager() {
    if (!transport_manager_) {
        transport_manager_ = std::make_shared<TransportManager>(working_executor_);
    }
    return transport_manager_;
}

void MessageEngineImpl::destroy() {
    initialize_ = false;
    rtc_channel_manager_.reset();
    mqtt_client_manager_.reset();
    cloud_message_manager_.reset();
    transport_manager_.reset();
}
