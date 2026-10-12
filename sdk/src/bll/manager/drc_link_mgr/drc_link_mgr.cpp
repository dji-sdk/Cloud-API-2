// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_link_mgr.h"
#include "api/flight_hub_sdk.h"
#include "common/util/async_capture_protect.h"
#include "message_engine_factory.h"
#include <common/executors/executors.h>
#include <base/common/logger.h>
#include "http_interface/http_interface.h"
#include "i_mqtt_client_manager.h"
#include "drc_link_define.h"
#include "drc_topic_util.h"

namespace flight_hub_sdk {

DrcLinkMgr::DrcLinkMgr(std::string sn, std::string gateway_sn, std::shared_ptr<message_engine::IMessageEngine> message_engine, int drone_instance_id) : sn_(sn), gateway_sn_(gateway_sn), message_engine_(message_engine) {
    instance_id_ = drone_instance_id;
    log_tag_ = sn_ + "#" + std::to_string(instance_id_);
    LOG_INFO("[DrcLinkMgr] created, instance_id: %d, sn: %s, gateway_sn: %s", instance_id_, sn_.c_str(), gateway_sn_.c_str());
    initDelayInfoListening();
    initCountDownInfoListening();
}

DrcLinkMgr::~DrcLinkMgr() {
    LOG_INFO("[DrcLinkMgr] destroyed, instance_id: %d, sn: %s, gateway_sn: %s", instance_id_, sn_.c_str(), gateway_sn_.c_str());
    stopHeartBeatTimer();
    drc_link_state_callbacks_.clear();
    delay_info_callbacks_.clear();
    count_down_info_callbacks_.clear();
    if (delay_info_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(delay_info_listen_id_);
        delay_info_listen_id_ = -1;
    }
    if (count_down_info_listen_id_ != -1) {
        message_engine_->getCloudMessageManager()->CancelObserveMessage(count_down_info_listen_id_);
        count_down_info_listen_id_ = -1;
    }
}

void DrcLinkMgr::initialize() {
}

void DrcLinkMgr::connectDrcLink(const std::function<void(FlightHubSdkErrorCode err_code)> callback) {
    user_disconnected_ = false;  // 用户主动建链，清除主动断链标志
    OpenDataChannelRequest request;
    request.drone_sn = sn_;
    request.gateway_sn = gateway_sn_;
    request.provider = "";
    WeakDummy(p);
    open_data_channel(request, log_tag_, [this, callback, p](const OpenDataChannelResponse& open_data_channel_response, FlightHubError err) {
        WeakDummyReturn(p);

        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            // 埋点: 连接 DRC 链路(开通数据通道请求失败, 未建链); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_CONNECT_LINK, err, "[DrcLinkMgr]");

            callback(err.error_code);
            return;
        }
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: provider  %s", log_tag_.c_str(), open_data_channel_response.data_channel.provider.c_str());

        // 建链后续按 provider 分流到 MQTT/RTC/DRTC, 投递点散落在多个私有函数里; 故在此把 callback 包一层,
        // 任何一条路径回投递前先埋点, 保证每次建链结果恰好上报一次且都在 callback 之前。
        auto report_callback = [callback](FlightHubSdkErrorCode err_code) -> void {
            auto connect_err = FlightHubError::from_sdk(err_code);

            // 埋点: 连接 DRC 链路; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_CONNECT_LINK, connect_err, "[DrcLinkMgr]");

            callback(err_code);
        };

        drc_sdk::common::Executors::Main()->Post([this, open_data_channel_response, report_callback, p]() {
            WeakDummyReturn(p);
            onRecvOpenDataChannelResponse(open_data_channel_response, report_callback);
        });
    });
}

void DrcLinkMgr::onRecvOpenDataChannelResponse(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback) {
    if (open_data_channel_response.data_channel.provider == "mqtt_broker") {
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.addr.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: client_id: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.client_id.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: default_frequency: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.default_frequency.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: method_frequency: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.method_frequency.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: expire_time: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.expire_time.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: up_topic: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.up_topic.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: down_topic: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.down_topic.c_str());
        // 设置mqtt_link的method路由表
        message_engine_->getTransportManager()->AddMethodRouterMap(method_router_map_v2_message_mqtt_link);
        onRecvDataChannelResponseWithMqtt(open_data_channel_response, callback);
    } else if (open_data_channel_response.data_channel.provider == "volc_rtc") {
#ifdef BUILD_WITH_VOLC_USER
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: room_id: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.room_id.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: user_id: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.user_id.c_str());
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse: expire_time: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.expire_time.c_str());
        message_engine_->getTransportManager()->AddMethodRouterMap(method_router_map_v2_message_rtc_link);
        onRecvDataChannelResponseWithRTC(open_data_channel_response, callback);
#else
        // 本构建未编 Volc native。服务端若仍返回 provider=volc_rtc，直接失败，
        // 避免走进不存在的实现。请把该设备改到 webrtc / drtc。
        LOG_ERROR("[DrcLinkMgr][%s] provider=volc_rtc is not supported in this build "
                  "(BUILD_WITH_VOLC_USER=OFF); DRC session will fail. Use WebRTC/DRTC instead.",
                  log_tag_.c_str());
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
#endif
    } else if (open_data_channel_response.data_channel.provider == "drtc_rtc" ||
               open_data_channel_response.data_channel.provider == "drtc") {
        LOG_INFO("[DrcLinkMgr][%s] OpenDataChannelResponse DRTC: room_id: %s, client_id: %s", log_tag_.c_str(), open_data_channel_response.data_channel.config.room_id.c_str(), open_data_channel_response.data_channel.config.client_id.c_str());
        message_engine_->getTransportManager()->AddMethodRouterMap(method_router_map_v2_message_rtc_link);
        onRecvDataChannelResponseWithDRTC(open_data_channel_response, callback);
    } else {
        LOG_ERROR("[DrcLinkMgr][%s] Unsupported provider: %s", log_tag_.c_str(), open_data_channel_response.data_channel.provider.c_str());
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
    }
}

void DrcLinkMgr::onRecvDataChannelResponseWithDRTC(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback) {
    auto rtc_client_manager = message_engine_->getRtcChannelManager();
    if (!rtc_client_manager) {
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
        return;
    }
    const auto& config = open_data_channel_response.data_channel.config;
    message_engine::ChannelInfo channel_info;
    channel_info.type_ = message_engine::ChannelType::RealTime;
    channel_info.provider_type_ = message_engine::ChannelProviderType::DRTC;
    channel_info.app_id_ = config.app_id;
    channel_info.channel_name_ = config.room_id;
    channel_info.room_id_ = config.room_id;
    channel_info.client_id_ = config.client_id.empty() ? config.user_id : config.client_id;
    channel_info.url_ = config.provider_url;
    channel_info.log_path_ = "";
    auto sdk_config = FlightHubSdk::getConfig();
    channel_info.config_path_ = sdk_config.drtc_config_path;
    WeakDummy(p);
    rtc_client_manager->AddChannel(channel_info, config.token,
                [channel_info, this, callback, p](const bool& is_succeed, const int& elapsed_time_ms, const int& error_code, const std::string& message) {
                WeakDummyReturn(p);
                LOG_INFO("[DrcLinkMgr][%s] onRecvDataChannelResponseWithDRTC JoinChannel channel_name %s, succeed %d, elapsed_time_ms %d, error_code %d, message %s", log_tag_.c_str(), channel_info.channel_name_.c_str(),
                             is_succeed, elapsed_time_ms, error_code, message.c_str());
                drc_sdk::common::Executors::Main()->Post([this, is_succeed, p, callback]() {
                    WeakDummyReturn(p);
                    callback(is_succeed ? FlightHubSdkErrorCode::SUCCESS : FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
                    onDrcLinkStateChanged(is_succeed);
                });
            }, message_engine::RTC_BASE_TRANSPORT_ID);
}

void DrcLinkMgr::onRecvDataChannelResponseWithRTC(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback) {
    auto rtc_client_manager = message_engine_->getRtcChannelManager();
    if (!rtc_client_manager) {
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
        return;
    }
    // 添加 火山rtc 通道
    message_engine::ChannelInfo channel_info;
    channel_info.type_ = message_engine::ChannelType::RealTime;
    channel_info.provider_type_ = message_engine::ChannelProviderType::VolcEngine;
    channel_info.app_id_ = open_data_channel_response.data_channel.config.app_id;
    channel_info.channel_name_ = open_data_channel_response.data_channel.config.room_id;
    channel_info.user_id_ = open_data_channel_response.data_channel.config.user_id;
    channel_info.log_path_ = "";
    WeakDummy(p);
    rtc_client_manager->AddChannel(channel_info, open_data_channel_response.data_channel.config.token,
                [channel_info, this, callback, p](const bool& is_succeed, const int& elapsed_time_ms, const int& error_code, const std::string& message) {
                WeakDummyReturn(p);
                LOG_INFO("[DrcLinkMgr][%s] onRecvDataChannelResponseWithRTC JoinChannel channel_name %s, succeed %d, elapsed_time_ms %d, error_code %d, message %s", log_tag_.c_str(), channel_info.channel_name_.c_str(),
                             is_succeed, elapsed_time_ms, error_code, message.c_str());
                drc_sdk::common::Executors::Main()->Post([this, is_succeed, p, callback]() {
                    WeakDummyReturn(p);
                    callback(is_succeed ? FlightHubSdkErrorCode::SUCCESS : FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
                    onDrcLinkStateChanged(is_succeed);
                });
            }, message_engine::RTC_BASE_TRANSPORT_ID);
}

void DrcLinkMgr::onRecvDataChannelResponseWithMqtt(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback) {
    auto mqtt_client_manager = message_engine_->getMqttClientManager();
    if (!mqtt_client_manager) {
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
        return;
    }

    const auto topic_info = ResolveDrcMqttTopics(open_data_channel_response.data_channel.config, gateway_sn_);
    if (!topic_info.ok) {
        LOG_ERROR("[DrcLinkMgr][%s] ResolveDrcMqttTopics failed, up=%s down=%s",
                  log_tag_.c_str(),
                  open_data_channel_response.data_channel.config.up_topic.c_str(),
                  open_data_channel_response.data_channel.config.down_topic.c_str());
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
        return;
    }
    LOG_INFO("[DrcLinkMgr][%s] drc mqtt topics: up=%s down=%s topic_sn=%s from_backend=%d",
             log_tag_.c_str(), topic_info.up_topic.c_str(), topic_info.down_topic.c_str(),
             topic_info.topic_sn.c_str(), topic_info.from_backend_topics ? 1 : 0);

    // Update mqtt client connect params if existed
    if (mqtt_client_manager->IsMqttClientExisted(message_engine::PAHO_MQTT_DRC_TRANSPORT_ID)) {
        // update mqtt client connect params
        auto mqtt_client = mqtt_client_manager->GetMqttClient(message_engine::PAHO_MQTT_DRC_TRANSPORT_ID).lock();
        if (!mqtt_client) {
            LOG_ERROR("[DrcLinkMgr][%s] onRecvDataChannelResponseWithMqtt, mqtt_client is null", log_tag_.c_str());
            callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
            return;
        }
        mqtt_client->ConnectParamsConfig(
            open_data_channel_response.data_channel.config.addr, 
            open_data_channel_response.data_channel.config.username, 
            open_data_channel_response.data_channel.config.password, 
            open_data_channel_response.data_channel.config.client_id, 
            topic_info.topic_sn
        );
        // 添加连接状态回调，确保能收到连接状态变化通知
        WeakDummy(p);
        mqtt_client->AddConnectStateCallback([this, p](const message_engine::MQTT_CONNECT_STATUS& state) {
            WeakDummyReturn(p);
            drc_sdk::common::Executors::Main()->Post([this, state, p]() {
                WeakDummyReturn(p);
                onDrcLinkStateChanged(state);
            });
        });
        callback(FlightHubSdkErrorCode::SUCCESS);
        return;
    }

    // Add mqtt client
    message_engine::AddMqttClientParams params;
    params.topic_method_map = topic_method_map_for_mqtt_drc;
    params.topics_pub = publish_topics_for_mqtt_drc;
    params.topics_sub = subscribe_topics_for_mqtt_drc;
    if (mqtt_client_manager->AddMqttClient(message_engine::PAHO_MQTT_DRC_TRANSPORT_ID, params) != message_engine::ME_NO_ERROR) {
        LOG_ERROR("[DrcLinkMgr][%s] onRecvDataChannelResponseWithMqtt, AddMqttClient failed", log_tag_.c_str());
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
        return;
    }

    // Get mqtt client
    auto mqtt_client = mqtt_client_manager->GetMqttClient(message_engine::PAHO_MQTT_DRC_TRANSPORT_ID).lock();
    if (!mqtt_client) {
        LOG_ERROR("[DrcLinkMgr][%s] onRecvDataChannelResponseWithMqtt, mqtt_client is null", log_tag_.c_str());
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
        return;
    }

    // Set connect params
    mqtt_client->ConnectParamsConfig(
        open_data_channel_response.data_channel.config.addr, 
        open_data_channel_response.data_channel.config.username, 
        open_data_channel_response.data_channel.config.password, 
        open_data_channel_response.data_channel.config.client_id, 
        topic_info.topic_sn
    );
    
    // Add connect state callback
    WeakDummy(p);
    mqtt_client->AddConnectStateCallback([this, callback, p](const message_engine::MQTT_CONNECT_STATUS& state) {
        WeakDummyReturn(p);
        drc_sdk::common::Executors::Main()->Post([this, state, p]() {
            WeakDummyReturn(p);
            onDrcLinkStateChanged(state);
        });
    });
    // Set up mqtt client
    if (mqtt_client->SetUp() != message_engine::ME_NO_ERROR) {
        LOG_ERROR("[DrcLinkMgr][%s] onRecvDataChannelResponseWithMqtt, SetUp failed", log_tag_.c_str());
        callback(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
        return;
    }

    callback(FlightHubSdkErrorCode::SUCCESS);
}

void DrcLinkMgr::startHeartBeatTimer() {
    LOG_INFO("[DrcLinkMgr][%s] startHeartBeatTimer", log_tag_.c_str());
    last_heart_beat_timestamp_ = std::chrono::steady_clock::now();
    WeakDummy(p);
    heart_beat_timer_.Start(drc_sdk::common::Executors::Main(), [this, p]() {
        auto msg = std::make_shared<message_engine::DrcHeartBeatSouthbound>();
        int64_t send_ts = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
        msg->msg.ts = send_ts;
        WeakDummyReturn(p);
        auto error_code = message_engine_->getCloudMessageManager()->SendDrcHeartBeatSouthbound(msg, [this, p, send_ts](int error_code, const std::shared_ptr<message_engine::DrcReplyHeartBeatNorthbound>& reply_msg) {
            WeakDummyReturn(p);
            LOG_DEBUG("[DrcLinkMgr][%s] SendDrcHeartBeatSouthbound, error_code: %d", log_tag_.c_str(), error_code);
            if (error_code != message_engine::ME_NO_ERROR) {
                LOG_ERROR("[DrcLinkMgr][%s] SendDrcHeartBeatSouthbound failed, error_code: %d", log_tag_.c_str(), error_code);
                if (std::chrono::steady_clock::now() - last_heart_beat_timestamp_ > std::chrono::seconds(MAX_HEART_BEAT_INTERVAL_S)) {
                    LOG_ERROR("[DrcLinkMgr][%s] connection lost for %d seconds, disconnect", log_tag_.c_str(), MAX_HEART_BEAT_INTERVAL_S);
                    disconnectDrcLink([this, p](FlightHubSdkErrorCode err_code) {
                        WeakDummyReturn(p);
                        if (err_code == FlightHubSdkErrorCode::SUCCESS) {
                            stopHeartBeatTimer();
                            updateDrcLinkState(DroneLinkState::DroneLinkState_DISCONNECTED);
                            scheduleReconnect();
                        } else {
                            LOG_ERROR("[DrcLinkMgr][%s] disconnectDrcLink, err_code: %d", log_tag_.c_str(), err_code);
                        }
                    });
                }
                return;
            }
            last_heart_beat_timestamp_ = std::chrono::steady_clock::now();
            // 阶段二：计算链路延迟 (当前时间 - 发送时间) / 2
            int64_t now_ms = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
            drc_reply_data_.heart_beat = (now_ms - send_ts) / 2;
            LOG_DEBUG("[DrcLinkMgr][%s] heart_beat delay: %lld ms", log_tag_.c_str(), static_cast<long long>(drc_reply_data_.heart_beat));
        });
        if (error_code != message_engine::ME_NO_ERROR) {
            LOG_ERROR("[DrcLinkMgr][%s] SendDrcHeartBeatSouthbound failed, error_code: %d", log_tag_.c_str(), error_code);
            // 同步返回失败时，检查时间间隔，如果超过阈值则断开连接
            if (std::chrono::steady_clock::now() - last_heart_beat_timestamp_ > std::chrono::seconds(MAX_HEART_BEAT_INTERVAL_S)) {
                LOG_ERROR("[DrcLinkMgr][%s] connection lost for %d seconds, disconnect", log_tag_.c_str(), MAX_HEART_BEAT_INTERVAL_S);
                WeakDummy(p);
                disconnectDrcLink([this, p](FlightHubSdkErrorCode err_code) {
                    WeakDummyReturn(p);
                    if (err_code == FlightHubSdkErrorCode::SUCCESS) {
                        stopHeartBeatTimer();
                        updateDrcLinkState(DroneLinkState::DroneLinkState_DISCONNECTED);
                        scheduleReconnect();
                    } else {
                        LOG_ERROR("[DrcLinkMgr][%s] disconnectDrcLink, err_code: %d", log_tag_.c_str(), err_code);
                    }
                });
            }
        }
    }, std::chrono::seconds(HEART_BEAT_INTERVAL_S), true);
}

void DrcLinkMgr::stopHeartBeatTimer() {
    heart_beat_timer_.Cancel();
}

void DrcLinkMgr::scheduleReconnect() {
    if (user_disconnected_) {
        LOG_INFO("[DrcLinkMgr] scheduleReconnect, user disconnected, skip");
        return;
    }
    if (reconnect_pending_) {
        LOG_INFO("[DrcLinkMgr] scheduleReconnect, already pending, skip");
        return;
    }
    reconnect_pending_ = true;
    LOG_INFO("[DrcLinkMgr] scheduleReconnect, will retry in %d seconds", RECONNECT_DELAY_S);
    WeakDummy(p);
    drc_sdk::common::Executors::Main()->PostDelay([this, p]() {
        WeakDummyReturn(p);
        reconnect_pending_ = false;
        if (user_disconnected_) {
            LOG_INFO("[DrcLinkMgr] scheduleReconnect, user disconnected during delay, skip");
            return;
        }
        if (drc_link_state_ == DroneLinkState::DroneLinkState_CONNECTED || drc_link_state_ == DroneLinkState::DroneLinkState_CONNECTING) {
            LOG_INFO("[DrcLinkMgr] scheduleReconnect, already connected/connecting, skip");
            return;
        }
        LOG_INFO("[DrcLinkMgr] scheduleReconnect, retrying now");
        updateDrcLinkState(DroneLinkState::DroneLinkState_CONNECTING);
        connectDrcLink([this, p](FlightHubSdkErrorCode err_code) {
            WeakDummyReturn(p);
            LOG_INFO("[DrcLinkMgr][%s] scheduleReconnect connectDrcLink, err_code: %d", log_tag_.c_str(), err_code);
            if (err_code != FlightHubSdkErrorCode::SUCCESS) {
                updateDrcLinkState(DroneLinkState::DroneLinkState_DISCONNECTED);
                scheduleReconnect();
            }
        });
    }, std::chrono::seconds(RECONNECT_DELAY_S));
}


void DrcLinkMgr::sendDrcStateInitializeMsg() {
    LOG_INFO("[DrcLinkMgr][%s] sendDrcStateInitializeMsg", log_tag_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcInitialStateSubscribeSouthbound>();
    WeakDummy(p);
    auto error_code = message_engine_->getCloudMessageManager()->SendDrcDrcInitialStateSubscribeSouthbound(msg, [this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcInitialStateSubscribeNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[DrcLinkMgr][%s] SendDrcDrcInitialStateSubscribeSouthbound, error_code: %d", log_tag_.c_str(), error_code);
        if (error_code != message_engine::ME_NO_ERROR) {
            drc_sdk::common::Executors::Main()->PostDelay([this, p]() {
                WeakDummyReturn(p);
                if (drc_link_state_ != DroneLinkState::DroneLinkState_CONNECTED) {
                    LOG_INFO("[DrcLinkMgr][%s] sendDrcStateInitializeMsg retry stopped, state: %d", log_tag_.c_str(), drc_link_state_);
                    return;
                }
                sendDrcStateInitializeMsg();
            }, std::chrono::milliseconds(500));
        }
    });
    if (error_code != message_engine::ME_NO_ERROR) {
        LOG_ERROR("[DrcLinkMgr][%s] SendDrcDrcInitialStateSubscribeSouthbound failed, error_code: %d", log_tag_.c_str(), error_code);
        drc_sdk::common::Executors::Main()->PostDelay([this, p]() {
            WeakDummyReturn(p);
            if (drc_link_state_ != DroneLinkState::DroneLinkState_CONNECTED) {
                LOG_INFO("[DrcLinkMgr][%s] sendDrcStateInitializeMsg retry stopped, state: %d", log_tag_.c_str(), drc_link_state_);
                return;
            }
            sendDrcStateInitializeMsg();
        }, std::chrono::milliseconds(500));
    }
}

void DrcLinkMgr::disconnectDrcLink(const std::function<void(FlightHubSdkErrorCode err_code)> callback) {
    auto mqtt_client_manager = message_engine_->getMqttClientManager();
    if (!mqtt_client_manager) {
        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 断开 DRC 链路(MQTT 客户端管理器不可用, 未执行断链); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_DISCONNECT_LINK, state_err, "[DrcLinkMgr]");

        callback(state_err.error_code);
        return;
    }
    mqtt_client_manager->RemoveMqttClient(message_engine::PAHO_MQTT_DRC_TRANSPORT_ID);
    // 立即停止心跳并更新状态，不依赖 MQTT 异步断连回调（否则心跳会多跑 MAX_HEART_BEAT_INTERVAL_S 秒）
    stopHeartBeatTimer();
    updateDrcLinkState(DroneLinkState::DroneLinkState_DISCONNECTED);

    auto success_err = FlightHubError::success();

    // 埋点: 断开 DRC 链路; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
    report_beacon_event_helper(kBeaconReportEvent_DRC_DISCONNECT_LINK, success_err, "[DrcLinkMgr]");

    callback(success_err.error_code);
}

int DrcLinkMgr::listenDrcLinkState(const std::function<void(const DroneLinkState& state)> callback) {
    return drc_link_state_callbacks_.addCallback(callback);
}

void DrcLinkMgr::cancelListenDrcLinkState(int listen_id) {
    drc_link_state_callbacks_.removeCallback(listen_id);
}

void DrcLinkMgr::onDrcLinkStateChanged(message_engine::MQTT_CONNECT_STATUS state) {
    switch (state) {
        case message_engine::MQTT_CONNECT_STATUS::CONNECTED:
            updateDrcLinkState(DroneLinkState::DroneLinkState_CONNECTED);
            // 连接真正建立后，启动心跳和发送初始化消息
            startHeartBeatTimer();
            sendDrcStateInitializeMsg();
            break;
        case message_engine::MQTT_CONNECT_STATUS::DISCONNECTED:
            // 断开连接时，停止心跳
            stopHeartBeatTimer();
            updateDrcLinkState(DroneLinkState::DroneLinkState_DISCONNECTED);
            scheduleReconnect();
            break;
        case message_engine::MQTT_CONNECT_STATUS::PREPARED:
            updateDrcLinkState(DroneLinkState::DroneLinkState_CONNECTING);
            break;
        case message_engine::MQTT_CONNECT_STATUS::IDLE:
            // IDLE 状态时，停止心跳
            stopHeartBeatTimer();
            updateDrcLinkState(DroneLinkState::DroneLinkState_IDLE);
            break;
        default:
            break;
    }
}

void DrcLinkMgr::onDrcLinkStateChanged(bool is_rtc_connected) {
    if (is_rtc_connected) {
        updateDrcLinkState(DroneLinkState::DroneLinkState_CONNECTED);
        // RTC 连接成功后，与 MQTT 路径一致：启动心跳和发送初始状态订阅
        startHeartBeatTimer();
        sendDrcStateInitializeMsg();
    } else {
        stopHeartBeatTimer();
        updateDrcLinkState(DroneLinkState::DroneLinkState_DISCONNECTED);
    }
}

void DrcLinkMgr::updateDrcLinkState(DroneLinkState state) {
    LOG_INFO("[DrcLinkMgr][%s] updateDrcLinkState, state: %d", log_tag_.c_str(), state);
    drc_link_state_ = state;
    drc_link_state_callbacks_.notifyCallbackIfChanged(state);
}

// ── Send methods ──

void DrcLinkMgr::drcRtcQualityTest(const RtcQualityTestParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[DrcLinkMgr][%s] drcRtcQualityTest, index: %d, length: %d, is_test_end: %d", log_tag_.c_str(), params.index, params.length, params.is_test_end);
    auto msg = std::make_shared<message_engine::DrcDrcRtcQualityTestSouthbound>();
    msg->msg.index = params.index;
    msg->msg.length = params.length;
    msg->msg.data = params.data;
    msg->msg.diff_time = params.diff_time;
    msg->msg.timestamp = params.timestamp;
    msg->msg.is_need_ack = params.is_need_ack;
    msg->msg.is_test_end = params.is_test_end;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcRtcQualityTestSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcRtcQualityTestNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[DrcLinkMgr][%s] drcRtcQualityTest callback, error_code: %d, result: %d", log_tag_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            auto drc_err = FlightHubError::from_drc(error_code);

            // 埋点: RTC 网络质量测试; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_RTC_QUALITY_TEST, drc_err, "[DrcLinkMgr]");

            callback(drc_err);
        } else if (!msg || msg->msg.result != 0) {
            auto reply_err = FlightHubError::from_drc(0, msg ? msg->msg.result : -1);

            // 埋点: RTC 网络质量测试; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_RTC_QUALITY_TEST, reply_err, "[DrcLinkMgr]");

            callback(reply_err);
        } else {
            auto success_err = FlightHubError::success();

            // 埋点: RTC 网络质量测试; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DRC_RTC_QUALITY_TEST, success_err, "[DrcLinkMgr]");

            callback(success_err);
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_drc(err);

        // 埋点: RTC 网络质量测试(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DRC_RTC_QUALITY_TEST, link_err, "[DrcLinkMgr]");

        callback(link_err);
    }
}

// ── Observe methods (Northbound pushes) ──

void DrcLinkMgr::initDelayInfoListening() {
    WeakDummy(p);
    delay_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDelayInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDelayInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            onReceiveDelayInfo(msg);
        }
    );
    if (delay_info_listen_id_ < 0) {
        LOG_ERROR("[DrcLinkMgr][%s] ObserveDrcDelayInfoPushNorthbound failed, delay_info_listen_id_ %d", log_tag_.c_str(), delay_info_listen_id_);
    }
    LOG_INFO("[DrcLinkMgr][%s] ObserveDrcDelayInfoPushNorthbound, delay_info_listen_id_ %d", log_tag_.c_str(), delay_info_listen_id_);
}

void DrcLinkMgr::onReceiveDelayInfo(const std::shared_ptr<message_engine::DrcDelayInfoPushNorthbound>& msg) {
    if (!msg) return;
    DrcDelayInfo info;
    info.stick_delay = msg->msg.stick_delay;
    info.sdr_cmd_delay = msg->msg.sdr_cmd_delay;
    delay_info_callbacks_.notifyCallbackIfChanged(info);
}

int DrcLinkMgr::listenDrcDelayInfo(const std::function<void(const DrcDelayInfo& info)>& callback) {
    auto listen_id = delay_info_callbacks_.addCallback(callback);
    LOG_INFO("[DrcLinkMgr][%s] listenDrcDelayInfo listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void DrcLinkMgr::cancelListenDrcDelayInfo(int listen_id) {
    LOG_INFO("[DrcLinkMgr][%s] cancelListenDrcDelayInfo listen_id: %d", log_tag_.c_str(), listen_id);
    delay_info_callbacks_.removeCallback(listen_id);
}

void DrcLinkMgr::initCountDownInfoListening() {
    WeakDummy(p);
    count_down_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcCountDownInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcCountDownInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            onReceiveCountDownInfo(msg);
        }
    );
    if (count_down_info_listen_id_ < 0) {
        LOG_ERROR("[DrcLinkMgr][%s] ObserveDrcDrcCountDownInfoPushNorthbound failed, count_down_info_listen_id_ %d", log_tag_.c_str(), count_down_info_listen_id_);
    }
    LOG_INFO("[DrcLinkMgr][%s] ObserveDrcDrcCountDownInfoPushNorthbound, count_down_info_listen_id_ %d", log_tag_.c_str(), count_down_info_listen_id_);
}

void DrcLinkMgr::onReceiveCountDownInfo(const std::shared_ptr<message_engine::DrcDrcCountDownInfoPushNorthbound>& msg) {
    if (!msg) return;
    DrcCountDownInfo info;
    info.count_down_type = msg->msg.count_down_type;
    info.count_down_time = msg->msg.count_down_time;
    info.can_cancel = msg->msg.can_cancel;
    count_down_info_callbacks_.notifyCallbackIfChanged(info);
}

int DrcLinkMgr::listenDrcCountDownInfo(const std::function<void(const DrcCountDownInfo& info)>& callback) {
    auto listen_id = count_down_info_callbacks_.addCallback(callback);
    LOG_INFO("[DrcLinkMgr][%s] listenDrcCountDownInfo listen_id: %d", log_tag_.c_str(), listen_id);
    return listen_id;
}

void DrcLinkMgr::cancelListenDrcCountDownInfo(int listen_id) {
    LOG_INFO("[DrcLinkMgr][%s] cancelListenDrcCountDownInfo listen_id: %d", log_tag_.c_str(), listen_id);
    count_down_info_callbacks_.removeCallback(listen_id);
}

} // namespace flight_hub_sdk