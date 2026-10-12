// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "live_module_impl.h"
#include "common/util/async_capture_protect.h"
#include "common/util/url_query.h"
#include "i_rtc_channel_manager.h"
#include "i_live_stream_channel.h"
#include <base/common/logger.h>
#include "live_utils.h"
#include "sei_parser/ai_sei_parser.h"
#include "sei_parser/ar_sei_parser.h"
#include "sei_parser/camera_fov_sei_parser.h"
#include "sei_parser/assistant_sei_parser.h"
#include "sei_parser/eis_sei_parser.h"
#include "sei_parser/end_to_end_delay_sei_parser.h"
#include "sei_parser/json_formatted_sei_parser.h"
#include "sei_parser/full_nal_sei_parser.h"
#include "common/executors/executors.h"
#include "api/flight_hub_sdk.h"
#include <cstdlib>
#include <filesystem>

using namespace flight_hub_sdk;

LiveModuleImpl::LiveModuleImpl(const LiveInfo& live_info_, std::shared_ptr<message_engine::IRtcChannelManager> rtc_channel_manager, const std::string& gateway_sn)
 : live_info_(live_info_), gateway_sn_(gateway_sn), rtc_channel_manager_(rtc_channel_manager) {
    LOG_INFO("LiveModuleImpl: LiveModuleImpl, live_info_: %s, %s, %s, gateway_sn: %s", live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str(), gateway_sn_.c_str());
 }

LiveModuleImpl::~LiveModuleImpl() {
    // 优先清空所有回调容器，防止析构后异步回调触发导致访问已释放的对象
    // 这是为了解决SWIG生成的lambda捕获裸指针，在Java对象GC后导致的悬垂指针问题
    live_stream_callback_container_.clear();
    ai_sei_info_callback_container_.clear();
    ar_sei_info_callback_container_.clear();
    fov_sei_info_callback_container_.clear();
    assistant_sei_info_callback_container_.clear();
    eis_sei_info_callback_container_.clear();
    
    // 防止双重删除，只有在未停止且channel_name_不为空时才删除
    if (!is_stopped_.load() && !channel_info_.channel_name_.empty() && rtc_channel_manager_) {
        rtc_channel_manager_->RemoveChannel(channel_info_.channel_name_);
        channel_info_.channel_name_.clear(); // 清空channel_name_防止重复删除
        LOG_INFO("LiveModuleImpl: ~LiveModuleImpl cleanup completed");
    } else {
        LOG_DEBUG("LiveModuleImpl: ~LiveModuleImpl skipped cleanup (already stopped or empty channel)");
    }
}

void LiveModuleImpl::start(const std::function<void(FlightHubError result)>& callback) {
    LOG_INFO("LiveModuleImpl: start, sn: %s, camera: %s, video: %s", 
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str());
    is_stopped_.store(false);
    if (!rtc_channel_manager_) {
        
        LOG_ERROR("LiveModuleImpl: start error, rtc_channel_manager_ is null, sn: %s, camera: %s",
            live_info_.device_sn.c_str(), live_info_.payload_index.c_str());

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, param_err, "[LiveModuleImpl]");

        callback(param_err);
        return;
    }
    WeakDummy(p);
    start_live_stream(live_info_, [callback, this, p](const LiveStreamStartResponse& response, FlightHubError err) {
        WeakDummyReturn(p);
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("LiveModuleImpl: start error: %d, sn: %s, camera: %s, video: %s",
                (int)err.error_code, live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str());

            // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, err, "[LiveModuleImpl]");

            callback(err);
            return;
        }
        if (response.publish_uid.empty() || response.url.empty() || response.url_type.empty()) {
            LOG_ERROR("LiveModuleImpl: start response is invalid, sn: %s, camera: %s, video: %s, publish_uid: %s, url: %s, url_type: %s",
                live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str(),
                response.publish_uid.c_str(), response.url.c_str(), response.url_type.c_str());

            auto resp_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

            // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, resp_err, "[LiveModuleImpl]");

            callback(resp_err);
            return;
        }
        channel_info_.channel_name_ = live_info_.device_sn + "_" + live_info_.payload_index;
        channel_info_.type_ = message_engine::ChannelType::LiveStream;
        channel_info_.provider_type_ = getChannelProviderType(response);
        channel_info_.url_ = response.url;

        // url_type 为 volc / 非 whep 的 agora，且本构建未编对应 native RTC 时，
        // getChannelProviderType 返回 UNKNOWN。这里直接失败，避免 AddChannel(UNKNOWN)
        // 落到 WebRTC 兜底、错误偏晚。其它未知 url_type 仍交给下方 AddChannel。
        if (channel_info_.provider_type_ == message_engine::ChannelProviderType::UNKNOWN
            && (response.url_type == "volc" || response.url_type == "agora")) {
            LOG_ERROR("LiveModuleImpl: url_type=%s not supported in this build (Volc/Agora native removed). sn: %s, camera: %s",
                     response.url_type.c_str(), live_info_.device_sn.c_str(), live_info_.payload_index.c_str());

            auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

            // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, provider_err, "[LiveModuleImpl]");

            callback(provider_err);
            return;
        }

        // 对于火山引擎，需要从 URL 中解析 app_id、user_id 和 token
        std::string volc_token = "";
        std::string agora_token = "";
        std::string drtc_token_parsed = "";
        if (channel_info_.provider_type_ == message_engine::ChannelProviderType::VolcEngine) {
#ifdef BUILD_WITH_VOLC_USER
            parseVolcUrlParams(response.url, channel_info_.app_id_, channel_info_.user_id_, volc_token);
            channel_info_.room_id_ = channel_info_.channel_name_;

            // 从 SDK 配置中获取日志路径，并转换为目录路径
            // 火山引擎的 log_path 需要是目录路径，而不是文件路径
            auto sdk_config = FlightHubSdk::getConfig();
            std::string log_file_path = sdk_config.log_file_path;
            if (!log_file_path.empty()) {
                // 从文件路径中提取目录路径
                std::filesystem::path log_path(log_file_path);
                std::string log_dir = log_path.parent_path().string();
                // 如果目录为空（即文件在当前目录），使用 "." 表示当前目录
                if (log_dir.empty()) {
                    log_dir = ".";
                }
                channel_info_.log_path_ = log_dir;
                LOG_INFO("LiveModuleImpl: set volc log_path to directory: %s (from log_file_path: %s)",
                         channel_info_.log_path_.c_str(), log_file_path.c_str());
            } else {
                // 如果 SDK 没有配置日志路径，使用当前目录
                channel_info_.log_path_ = ".";
                LOG_INFO("LiveModuleImpl: volc log_path not configured, using current directory");
            }

            LOG_INFO("LiveModuleImpl: parsed volc params, app_id: %s, user_id: %s, room_id: %s, token_len: %zu, log_path: %s",
                     channel_info_.app_id_.c_str(), channel_info_.user_id_.c_str(), channel_info_.room_id_.c_str(),
                     volc_token.size(), channel_info_.log_path_.c_str());
#else
            // 本构建未编 Volc native；正常已被 getChannelProviderType 拦住，这里再失败一次。
            LOG_ERROR("LiveModuleImpl: provider VolcEngine requested but BUILD_WITH_VOLC_USER=OFF; SDK does not support Volc in this build. sn: %s, camera: %s",
                     live_info_.device_sn.c_str(), live_info_.payload_index.c_str());

            auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

            // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, provider_err, "[LiveModuleImpl]");

            callback(provider_err);
            return;
#endif
        } else if (channel_info_.provider_type_ == message_engine::ChannelProviderType::Agora) {
#ifdef BUILD_WITH_AGORA_USER
            // 对于声网，需要从 URL 中解析 app_id、channel_name、user_id 和 token
            std::string agora_channel_name = "";
            parseAgoraUrlParams(response.url, channel_info_.app_id_, agora_channel_name, channel_info_.user_id_, agora_token);

            // 如果解析到了channel_name，使用解析到的值；否则使用默认的channel_name
            if (!agora_channel_name.empty()) {
                channel_info_.channel_name_ = agora_channel_name;
            }
            // 对于声网，channel_name_ 和 room_id_ 相同
            channel_info_.room_id_ = channel_info_.channel_name_;

            // 从 SDK 配置中获取日志路径，并转换为目录路径
            // 声网的 log_path 需要是目录路径，而不是文件路径
            auto sdk_config = FlightHubSdk::getConfig();
            std::string log_file_path = sdk_config.log_file_path;
            if (!log_file_path.empty()) {
                // 从文件路径中提取目录路径
                std::filesystem::path log_path(log_file_path);
                std::string log_dir = log_path.parent_path().string();
                // 如果目录为空（即文件在当前目录），使用 "." 表示当前目录
                if (log_dir.empty()) {
                    log_dir = ".";
                }
                channel_info_.log_path_ = log_dir;
                LOG_INFO("LiveModuleImpl: set agora log_path to directory: %s (from log_file_path: %s)",
                         channel_info_.log_path_.c_str(), log_file_path.c_str());
            } else {
                // 如果 SDK 没有配置日志路径，使用当前目录
                channel_info_.log_path_ = ".";
                LOG_INFO("LiveModuleImpl: agora log_path not configured, using current directory");
            }

            LOG_INFO("LiveModuleImpl: parsed agora params, app_id: %s, channel_name: %s, user_id: %s, room_id: %s, token_len: %zu, log_path: %s",
                     channel_info_.app_id_.c_str(), channel_info_.channel_name_.c_str(), channel_info_.user_id_.c_str(), channel_info_.room_id_.c_str(),
                     agora_token.size(), channel_info_.log_path_.c_str());
#else
            // 本构建未编 Agora native（whep 已在 getChannelProviderType 改走 WebRTC，到不了这里）。
            LOG_ERROR("LiveModuleImpl: provider Agora (non-whep) requested but BUILD_WITH_AGORA_USER=OFF. sn: %s, camera: %s",
                     live_info_.device_sn.c_str(), live_info_.payload_index.c_str());

            auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

            // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, provider_err, "[LiveModuleImpl]");

            callback(provider_err);
            return;
#endif
        } else if (channel_info_.provider_type_ == message_engine::ChannelProviderType::DRTC) {
            // DRTC: 从 URL query 解析 room_id、stream_id、app_id、client_id、token、provider_url
            std::string provider_url_parsed;
            parseDrtcUrlParams(response.url, channel_info_.app_id_, channel_info_.room_id_,
                              channel_info_.channel_name_, channel_info_.client_id_, drtc_token_parsed,
                              provider_url_parsed);
            channel_info_.url_ = !response.provider_url.empty() ? response.provider_url : provider_url_parsed;

            // 对齐 Web：RtcChannelManager key 用 stream_tag；DRTC Subscribe 用 URL 中的 stream_id
            channel_info_.drtc_stream_id_ = channel_info_.channel_name_;
            channel_info_.stream_tag_ = live_info_.device_sn + "_" + live_info_.payload_index + "_" + live_info_.video_id;
            channel_info_.channel_name_ = channel_info_.stream_tag_;

            // config_path 从 SDK 全局配置获取
            auto sdk_config = FlightHubSdk::getConfig();
            channel_info_.config_path_ = sdk_config.drtc_config_path;

            LOG_INFO("LiveModuleImpl: parsed drtc params, app_id: %s, room_id: %s, drtc_stream_id: %s, rtc_channel_key: %s, client_id: %s, provider_url: %s, config_path: %s",
                     channel_info_.app_id_.c_str(), channel_info_.room_id_.c_str(),
                     channel_info_.drtc_stream_id_.c_str(), channel_info_.channel_name_.c_str(),
                     channel_info_.client_id_.c_str(),
                     channel_info_.url_.c_str(), channel_info_.config_path_.c_str());
        }

        // 构建流标识标签，格式: device_sn_payload_index_video_id，用于日志过滤和定位
        channel_info_.stream_tag_ = live_info_.device_sn + "_" + live_info_.payload_index + "_" + live_info_.video_id;
        
        // 检查channel是否已存在，如果存在则记录警告
        bool channel_exists = rtc_channel_manager_->QueryChannelExists(channel_info_.channel_name_);
        if (channel_exists) {
            LOG_WARN("LiveModuleImpl: Channel %s already exists. If multiple LiveModuleImpl instances share the same channel_name, only the last one will receive data. sn: %s, camera: %s", 
                channel_info_.channel_name_.c_str(), live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
        }
        
        LOG_INFO("LiveModuleImpl: Adding channel, sn: %s, camera: %s, video: %s, channel_name: %s, url: %s, provider_type: %d, channel_exists: %s", 
            live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str(),
            channel_info_.channel_name_.c_str(), channel_info_.url_.c_str(), static_cast<int>(channel_info_.provider_type_),
            (channel_exists ? "是" : "否"));
        // 根据provider_type选择对应的token
        std::string token;
        if (channel_info_.provider_type_ == message_engine::ChannelProviderType::Agora) {
            token = agora_token;
        } else if (channel_info_.provider_type_ == message_engine::ChannelProviderType::DRTC) {
            // DRTC token 已在上面 parseDrtcUrlParams 中解析到 drtc_token_parsed
            token = drtc_token_parsed;
        } else {
            token = volc_token;
        }
        auto ret = rtc_channel_manager_->AddChannel(channel_info_, token, 
            [callback, this, p](const bool &is_succeed, const int &elapsed_time_ms, const int &error_code, const std::string &message) {
            WeakDummyReturn(p);
            LOG_INFO("LiveModuleImpl: start callback, is_succeed: %d, elapsed_time_ms: %d, error_code: %d, message: %s, sn: %s, camera: %s", 
                is_succeed, elapsed_time_ms, error_code, message.c_str(), live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
            if (is_succeed) {
                LOG_INFO("LiveModuleImpl: start success, sn: %s, camera: %s, video: %s",
                    live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str());

                // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, FlightHubError::success(), "[LiveModuleImpl]");

                callback(FlightHubError::success());
                subscribeLiveStreamInternal();
            } else {
                LOG_ERROR("LiveModuleImpl: AddChannel failed, error_code: %d, message: %s, sn: %s, camera: %s",
                    error_code, message.c_str(), live_info_.device_sn.c_str(), live_info_.payload_index.c_str());

                auto channel_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INIT_FAILED);

                // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, channel_err, "[LiveModuleImpl]");

                callback(channel_err);
            }
            // 更新直播状态
            update_live_stream_state(live_info_, is_succeed ? 2 : 1, [callback, this, p](FlightHubError err) {
                WeakDummyReturn(p);
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("LiveModuleImpl: update_live_stream_state error: %d, sn: %s, camera: %s",
                        (int)err.error_code, live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
                }
            });
        }, live_info_.video_id);
        if (!ret) {
            LOG_ERROR("LiveModuleImpl: AddChannel call failed, sn: %s, camera: %s, video: %s",
                live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str());

            auto add_channel_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

            // 埋点: 开始直播; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_START_LIVE_STREAM, add_channel_err, "[LiveModuleImpl]");

            callback(add_channel_err);
            return;
        }
    }, gateway_sn_);
}

void LiveModuleImpl::startWithURL(const std::function<void(FlightHubError err, const LiveStreamStartResult& result)>& callback) {
    LOG_INFO("LiveModuleImpl: startWithURL, sn: %s, camera: %s, video: %s",
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str());
    WeakDummy(p);
    start_live_stream(live_info_, [callback, p](const LiveStreamStartResponse& response, FlightHubError err) {
        WeakDummyReturn(p);
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, LiveStreamStartResult{});
            return;
        }
        LiveStreamStartResult result;
        result.expire_ts = response.expire_ts;
        result.url_type = response.url_type;
        result.url = response.url;
        result.publish_uid = response.publish_uid;
        result.provider_url = response.provider_url;
        callback(FlightHubError::success(), result);
    }, gateway_sn_);
}

void LiveModuleImpl::stop() {
    LOG_INFO("LiveModuleImpl: stop, sn: %s, camera: %s, channel_name: %s", 
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), channel_info_.channel_name_.c_str());
    
    // 使用原子操作防止重复停止
    bool expected = false;
    if (!is_stopped_.compare_exchange_strong(expected, true)) {
        LOG_WARN("LiveModuleImpl: stop already called, ignoring duplicate call, sn: %s, camera: %s", 
            live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
        return;
    }
    
    if (!rtc_channel_manager_) {
        LOG_ERROR("LiveModuleImpl: stop error, rtc_channel_manager_ is null, sn: %s, camera: %s", 
            live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
        return;
    }
    
    // 防止重复停止
    if (channel_info_.channel_name_.empty()) {
        LOG_WARN("LiveModuleImpl: stop already called, channel_name_ is empty, sn: %s, camera: %s", 
            live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
        return;
    }
    
    rtc_channel_manager_->RemoveChannel(channel_info_.channel_name_);
    channel_info_.channel_name_.clear(); // 清空channel_name_防止重复删除
    LOG_INFO("LiveModuleImpl: stop success, sn: %s, camera: %s", 
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
}

void LiveModuleImpl::stopWithURL(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("LiveModuleImpl: stopWithURL, sn: %s, camera: %s, video: %s",
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), live_info_.video_id.c_str());
    WeakDummy(p);
    stop_live_stream(live_info_, [callback, p](FlightHubError err) {
        WeakDummyReturn(p);
        callback(err);
    }, gateway_sn_);
}

int LiveModuleImpl::subscribeLiveStream(const std::function<void(const std::vector<uint8_t>& data, const FrameInfo& frame_info)>& callback) {
    return live_stream_callback_container_.addCallback(callback);
}

void LiveModuleImpl::unSubscribeLiveStream(int listen_id) {
    live_stream_callback_container_.removeCallback(listen_id);
}

int LiveModuleImpl::subscribeAiSeiInfo(const std::function<void(const AiSeiInfo& ai_sei_info)>& callback) {
    return ai_sei_info_callback_container_.addCallback(callback);
}

void LiveModuleImpl::unSubscribeAiSeiInfo(int listen_id) {
    ai_sei_info_callback_container_.removeCallback(listen_id);
}

int LiveModuleImpl::subscribeArSeiInfo(const std::function<void(const ArSeiInfo& ar_sei_info)>& callback) {
    return ar_sei_info_callback_container_.addCallback(callback);
}

void LiveModuleImpl::unSubscribeArSeiInfo(int listen_id) {
    ar_sei_info_callback_container_.removeCallback(listen_id);
}

int LiveModuleImpl::subscribeFovSeiInfo(const std::function<void(const FovSeiInfo& fov_sei_info)>& callback) {
    return fov_sei_info_callback_container_.addCallback(callback);
}

void LiveModuleImpl::unSubscribeFovSeiInfo(int listen_id) {
    fov_sei_info_callback_container_.removeCallback(listen_id);
}

int LiveModuleImpl::subscribeAssistantSeiInfo(const std::function<void(const AssistantSeiInfo& assistant_sei_info)>& callback) {
    return assistant_sei_info_callback_container_.addCallback(callback);
}

void LiveModuleImpl::unSubscribeAssistantSeiInfo(int listen_id) {
    assistant_sei_info_callback_container_.removeCallback(listen_id);
}

int LiveModuleImpl::subscribeEisSeiInfo(const std::function<void(const EisSeiInfo& eis_sei_info)>& callback) {
    return eis_sei_info_callback_container_.addCallback(callback);
}

void LiveModuleImpl::unSubscribeEisSeiInfo(int listen_id) {
    eis_sei_info_callback_container_.removeCallback(listen_id);
}

void LiveModuleImpl::processLiveStreamData(const std::shared_ptr<drc_sdk::ByteBuffer>& data, bool is_i_frame) {
    // 添加数据验证，避免空指针或无效数据
    if (!data || data->size() == 0) {
        LOG_WARN("LiveModuleImpl: processLiveStreamData - invalid data");
        return;
    }
    
    std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer;
    if (isNeedProcessSei()) {
        const uint8_t* frame_data = reinterpret_cast<const uint8_t*>(data->data());
        const uint32_t frame_size = static_cast<uint32_t>(data->size());
        bool sei_extracted = false;
        if (FullNalSeiParser::LooksLikeFullNalSei(frame_data, frame_size)) {
            sei_extracted = FullNalSeiParser::TryExtractTlvBundle(frame_data, frame_size, sei_buffer);
        }
        if (!sei_extracted) {
            sei_extracted = get_sei_buffer(frame_data, frame_size, sei_buffer);
        }
        if (sei_extracted) {
            WeakDummy(p);
            drc_sdk::common::Executors::Main()->Post([sei_buffer, this, p]() {
                WeakDummyReturn(p);
                processSei(sei_buffer);
            });
        }
    }
    
    FrameInfo frame_info;
    frame_info.is_key_frame = is_key_frame((uint8_t*)data->data(), data->size());
    
    // 增加诊断日志：只在关键帧时记录，减少日志量
    if (frame_info.is_key_frame) {
        LOG_INFO("LiveModuleImpl: processLiveStreamData - 处理视频数据, 大小: %zu, 关键帧: 是", 
            data->size());
    }
    
    // 安全地创建vector，避免内存问题
    // 检查数据大小是否合理，避免过大的内存分配
    if (data->size() > 0 && data->size() < 100 * 1024 * 1024) {  // 限制在100MB以内
        std::vector<uint8_t> frame_data(data->data(), data->data() + data->size());
        live_stream_callback_container_.notifyCallback(frame_data, frame_info);
        // 增加诊断日志：只在关键帧时记录，减少日志量
        if (frame_info.is_key_frame) {
            LOG_INFO("LiveModuleImpl: processLiveStreamData - 已通知所有callback, 大小: %zu, 关键帧: 是", data->size());
        }
    } else {
        LOG_ERROR("LiveModuleImpl: processLiveStreamData - data size out of range: %zu", data->size());
    }
}

bool LiveModuleImpl::isNeedProcessSei() {
    if (live_info_.payload_index == "39-0-7") { // FPV
        return false;
    }
    if (live_info_.payload_index == "165-0-7") { // Dock Live
        return false;
    }
    return true;
}

bool LiveModuleImpl::isJsonFormattedSei() const {
    // Matrice 3D/3TD (EA220)
    if (live_info_.payload_index == "80-0-0" || live_info_.payload_index == "81-0-0") return true;
    // Mavic 3E/3T/3TA (WM265)
    if (live_info_.payload_index == "66-0-0" || live_info_.payload_index == "67-0-0" || live_info_.payload_index == "129-0-0") return true;
    return false;
}

void LiveModuleImpl::processSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    if (isJsonFormattedSei()) {
        processJsonFormattedSei(data);
        return;
    }
    int length = data->size();
    for (int i = 0; i < length;) {
        int payload_type = (uint8_t)data->data()[i];
        if (i + 3 >= length) {
            LOG_ERROR("LiveModuleImpl: processSei - insufficient data for payload_length");
            break;
        }
        uint16_t payload_length = static_cast<uint16_t>(static_cast<uint8_t>(data->data()[i + 3]) << 8) | (static_cast<uint8_t>(data->data()[i + 2]));
        uint32_t data_start_index = i + 4;
        uint32_t data_end_index = data_start_index + payload_length;
        if (data_end_index > length) {
            LOG_ERROR("LiveModuleImpl: processSei - insufficient data for payload_length, payload_type: %d, data_end_index: %d, length: %d, payload_length: %d", payload_type, data_end_index, length, payload_length);
            break;
        }
        auto payload_data = std::make_shared<drc_sdk::ByteBuffer>(data->data() + data_start_index, payload_length);
        if (payload_type == static_cast<int>(SeiType::AI_SEI)) {
            processAISei(payload_data);
        } else if (payload_type == static_cast<int>(SeiType::AR_SEI)) {
            processARSei(payload_data);
        } else if (payload_type == static_cast<int>(SeiType::FOV_SEI)) {
            processFovSei(payload_data);
        } else if (payload_type == static_cast<int>(SeiType::ASSISTANT_SEI)) {
            processAssistantSei(payload_data);
        } else if (payload_type == static_cast<int>(SeiType::EIS_SEI)) {
            processEisSei(payload_data);
        } else if (payload_type == static_cast<int>(SeiType::END_TO_END_DELAY_SEI)) {
            processEndToEndDelaySei(payload_data);
        }
        i = data_end_index;
    }
}

void LiveModuleImpl::processAISei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    AiSeiParser ai_sei_parser(data);
    if (!ai_sei_parser.parse()) {
        LOG_ERROR("LiveModuleImpl: processAISei error");
        return;
    }
    auto ai_sei_info = ai_sei_parser.get_ai_sei_info();
    
    // for (auto& target : ai_sei_info.targets) {
    //     LOG_DEBUG("LiveModuleImpl: processAISei success, target: %d, %d, %d, %d, %d, %d, %d", target.target_id, target.target_type, target.target_state, target.target_cx, target.target_cy, target.target_w, target.target_h);
    // }
    ai_sei_info_callback_container_.notifyCallback(ai_sei_info);
}

void LiveModuleImpl::processARSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    ArSeiParser ar_sei_parser(data);
    if (!ar_sei_parser.parse()) {
        LOG_ERROR("LiveModuleImpl: processARSei error");
        return;
    }
    auto ar_sei_info = ar_sei_parser.get_ar_sei_info();
    // LOG_DEBUG("LiveModuleImpl: processARSei success, ar_sei_info: %lf, %lf, %lf, %d, %d, %d, %d, %d, %d", ar_sei_info.camera_yaw, ar_sei_info.camera_pitch, ar_sei_info.camera_roll, ar_sei_info.camera_pos_x, ar_sei_info.camera_pos_y, ar_sei_info.camera_pos_z, ar_sei_info.latitude, ar_sei_info.longitude, ar_sei_info.altitude);
    ar_sei_info_callback_container_.notifyCallback(ar_sei_info);
}

void LiveModuleImpl::processFovSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    CameraFovSeiParser camera_fov_sei_parser(data);
    if (!camera_fov_sei_parser.parse()) {
        LOG_ERROR("LiveModuleImpl: processFovSei error");
        return;
    }
    auto camera_fov_sei_info = camera_fov_sei_parser.get_camera_fov_sei_info();
    // LOG_DEBUG("LiveModuleImpl: processFovSei success, camera_fov_sei_info: %d, %d, %d", camera_fov_sei_info.fov_horizontal, camera_fov_sei_info.fov_vertical, camera_fov_sei_info.cur_actual_focal_length);
    fov_sei_info_callback_container_.notifyCallback(camera_fov_sei_info);
}

void LiveModuleImpl::processAssistantSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    AssistantSeiParser assistant_sei_parser(data);
    if (!assistant_sei_parser.parse()) {
        LOG_ERROR("LiveModuleImpl: processAssistantSei error");
        return;
    }
    auto assistant_sei_info = assistant_sei_parser.get_assistant_sei_info();
    // LOG_DEBUG("LiveModuleImpl: processAssistantSei success, assistant_sei_info: %d, %d, %d, %d", assistant_sei_info.enable, assistant_sei_info.is_auto_direction, assistant_sei_info.direction);
    assistant_sei_info_callback_container_.notifyCallback(assistant_sei_info);
}

void LiveModuleImpl::processEisSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    EisSeiParser eis_sei_parser(data);
    if (!eis_sei_parser.parse()) {
        LOG_ERROR("LiveModuleImpl: processEisSei error");
        return;
    }
    auto eis_sei_info = eis_sei_parser.get_eis_sei_info();
    
    // LOG_DEBUG("LiveModuleImpl: processEisSei success, eis_sei_info: version=%d, left_top=(%d,%d), right_top=(%d,%d), right_bottom=(%d,%d), left_bottom=(%d,%d)", 
    //     eis_sei_info.version, eis_sei_info.left_top.x, eis_sei_info.left_top.y, eis_sei_info.right_top.x, eis_sei_info.right_top.y, 
    //     eis_sei_info.right_bottom.x, eis_sei_info.right_bottom.y, eis_sei_info.left_bottom.x, eis_sei_info.left_bottom.y);
    eis_sei_info_callback_container_.notifyCallback(eis_sei_info);
}

void LiveModuleImpl::processEndToEndDelaySei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    EndToEndDelaySeiParser end_to_end_delay_sei_parser(data);
    if (!end_to_end_delay_sei_parser.parse()) {
        LOG_ERROR("LiveModuleImpl: processEndToEndDelaySei error");
        return;
    }
}
void LiveModuleImpl::processJsonFormattedSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data) {
    std::string sei_buffer(data->data(), data->size());
    JsonFormattedSeiParser json_formatted_sei_parser(sei_buffer);
    if (!json_formatted_sei_parser.parse()) {
        LOG_ERROR("LiveModuleImpl: processJsonFormattedSei error");
        return;
    }
    if (json_formatted_sei_parser.is_fov_sei_info_valid()) {
        auto fov_sei_info = json_formatted_sei_parser.get_camera_fov_sei_info();
        fov_sei_info_callback_container_.notifyCallback(fov_sei_info);
        // LOG_DEBUG("LiveModuleImpl: processJsonFormattedSei success, fov_sei_info: %d, %d, %d", fov_sei_info.fov_horizontal, fov_sei_info.fov_vertical, fov_sei_info.cur_actual_focal_length);
    }
    if (json_formatted_sei_parser.is_ar_sei_info_valid()) {
        auto ar_sei_info = json_formatted_sei_parser.get_ar_sei_info();
        ar_sei_info_callback_container_.notifyCallback(ar_sei_info);
        // LOG_DEBUG("LiveModuleImpl: processJsonFormattedSei success, ar_sei_info: %d, %d, %d, %d, %d, %d, %d, %d, %d", ar_sei_info.camera_yaw, ar_sei_info.camera_pitch, ar_sei_info.camera_roll, ar_sei_info.camera_pos_x, ar_sei_info.camera_pos_y, ar_sei_info.camera_pos_z, ar_sei_info.latitude, ar_sei_info.longitude, ar_sei_info.altitude);
    }

}

void LiveModuleImpl::subscribeLiveStreamInternal() {
    LOG_INFO("LiveModuleImpl: subscribeLiveStreamInternal, sn: %s, camera: %s, channel_name: %s", 
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), channel_info_.channel_name_.c_str());
    auto live_stream_channel = rtc_channel_manager_->GetChannel(channel_info_.channel_name_);
    auto live_stream_channel_ptr = live_stream_channel.lock();
    if (!live_stream_channel_ptr) {
        LOG_ERROR("LiveModuleImpl: subscribeLiveStreamInternal error, live_stream_channel is null, sn: %s, camera: %s, channel_name: %s", 
            live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), channel_info_.channel_name_.c_str());
        return;
    }
    WeakDummy(p);
    
    // 设置视频数据回调
    // 注意：如果多个LiveModuleImpl实例共享同一个channel，后面的SetReceiveVideoDataCallback会覆盖前面的
    // 这会导致只有最后一个LiveModuleImpl能接收到数据
    LOG_INFO("LiveModuleImpl: subscribeLiveStreamInternal - 设置SetReceiveVideoDataCallback, sn: %s, camera: %s, channel_name: %s", 
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), channel_info_.channel_name_.c_str());
    std::dynamic_pointer_cast<message_engine::ILiveStreamChannel>(live_stream_channel_ptr)->SetReceiveVideoDataCallback(
        [this, p](const std::shared_ptr<drc_sdk::ByteBuffer>& data, bool is_key_frame) {
            WeakDummyReturn(p);
            if (data == nullptr) {
                LOG_WARN("[LiveModuleImpl] subscribeLiveStreamInternal error: data is null");
                return;
            }
            if (data->Empty()) {
                LOG_WARN("[LiveModuleImpl] subscribeLiveStreamInternal error: data is Empty");
                return;
            }
            // 增加诊断日志：只在关键帧时记录，减少日志量
            if (is_key_frame) {
                LOG_INFO("[LiveModuleImpl] subscribeLiveStreamInternal - 收到视频数据回调, 大小: %zu, 关键帧: 是", data->size());
            }
            processLiveStreamData(data, is_key_frame);
        }
    );
    LOG_INFO("LiveModuleImpl: subscribeLiveStreamInternal - SetReceiveVideoDataCallback设置完成, sn: %s, camera: %s", 
        live_info_.device_sn.c_str(), live_info_.payload_index.c_str());
    
    // 设置全局错误回调 - 订阅WebRTC错误
    live_stream_channel_ptr->SetGlobalErrorCallback([this, p](const std::string& error, const std::string& message) {
        WeakDummyReturn(p);
        LOG_ERROR("[LiveModuleImpl] WebRTC错误: %s - %s", error.c_str(), message.c_str());
        handleWebRTCError(error, message);
    });
    
    // 设置Token过期回调
    live_stream_channel_ptr->SetTokenPrivilegeWillExpireCallback([this, p]() {
        WeakDummyReturn(p);
        LOG_WARN("[LiveModuleImpl] Token即将过期，需要续期");
        handleTokenExpiration();
    });
}

void LiveModuleImpl::handleWebRTCError(const std::string& error, const std::string& message) {
    LOG_ERROR("[LiveModuleImpl] 处理WebRTC错误: %s - %s", error.c_str(), message.c_str());
    
    // 解析错误码
    int error_code = 0;
    if (error.find("WebRTC_ErrorCode_") == 0) {
        const char* error_code_str = error.c_str() + 17; // 跳过 "WebRTC_ErrorCode_" 前缀
        char* end_ptr = nullptr;
        error_code = std::strtol(error_code_str, &end_ptr, 10);
        if (end_ptr == error_code_str || *end_ptr != '\0') {
            LOG_ERROR("[LiveModuleImpl] 解析错误码失败，原始字符串: %s", error.c_str());
            error_code = 0;
        }
    }
    
    // 根据错误码进行不同处理
    switch (error_code) {
        case 1001: // PeerConnectionFactory创建失败
        case 1002: // PeerConnection创建失败
        case 1003: // 视频接收器添加失败
            LOG_ERROR("[LiveModuleImpl] WebRTC初始化失败，错误码: %d", error_code);
            // 可以在这里实现重新初始化逻辑
            break;
        case 1004: // WHEP请求失败
            LOG_ERROR("[LiveModuleImpl] WHEP请求失败，可能是服务器问题");
            // 可以在这里实现重试逻辑
            break;
        case 2001: // ICE连接失败
            LOG_ERROR("[LiveModuleImpl] ICE连接失败，可能需要检查网络连接");
            // 可以在这里实现重连逻辑
            break;
        case 2002: // ICE连接断开
            LOG_WARN("[LiveModuleImpl] ICE连接断开，尝试重连");
            // 可以在这里实现重连逻辑
            break;
        case 3001: // 数据流超时
            LOG_ERROR("[LiveModuleImpl] 数据流超时，超过30秒未收到数据");
            // 可以在这里实现重连或通知用户
            break;
        default:
            LOG_ERROR("[LiveModuleImpl] WebRTC错误，错误码: %d, 消息: %s", error_code, message.c_str());
            break;
    }
}

void LiveModuleImpl::handleTokenExpiration() {
    LOG_WARN("[LiveModuleImpl] Token即将过期，开始自动续期, device_sn: %s, camera: %s, user_id: %s, channel_name: %s", 
             live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), 
             channel_info_.user_id_.c_str(), channel_info_.channel_name_.c_str());
    
    // 1. 调用后端 API 获取新 token
    // 根据 provider_type 确定 provider 字符串
    // 注意：后端返回的 provider 值：火山引擎为 "volc"，声网为 "agora_rtc"
    std::string provider_str = "";
    if (channel_info_.provider_type_ == message_engine::ChannelProviderType::VolcEngine) {
        provider_str = "volc";
    } else if (channel_info_.provider_type_ == message_engine::ChannelProviderType::Agora) {
        provider_str = "agora_rtc";  // 后端返回的 provider 值是 "agora_rtc"，不是 "agora"
    }
    
    update_live_stream_token(live_info_, channel_info_.user_id_, provider_str,
        [this](FlightHubError err, const std::string& new_token) {
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("[LiveModuleImpl] 获取新 token 失败: %d, device_sn: %s, camera: %s, user_id: %s",
                          (int)err.error_code, live_info_.device_sn.c_str(), live_info_.payload_index.c_str(),
                          channel_info_.user_id_.c_str());
                return;
            }
            
            if (new_token.empty()) {
                LOG_ERROR("[LiveModuleImpl] 获取的新 token 为空, device_sn: %s, camera: %s, user_id: %s", 
                          live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), 
                          channel_info_.user_id_.c_str());
                return;
            }
            
            LOG_INFO("[LiveModuleImpl] 成功获取新 token, token_length: %zu, device_sn: %s, camera: %s, user_id: %s", 
                     new_token.size(), live_info_.device_sn.c_str(), live_info_.payload_index.c_str(), 
                     channel_info_.user_id_.c_str());
            
            // 2. 若 token 带 %XX，做 URL 解码（不把 + 当空格，token 里可能有字面 +）
            std::string decoded_token = new_token;
            // 检查 token 是否包含 URL 编码字符（如 %2B, %2F, %3D 等）
            if (new_token.find('%') != std::string::npos) {
                LOG_INFO("[LiveModuleImpl] 检测到 token 包含 URL 编码字符，开始解码, token_length: %zu", new_token.size());
                // 简单的 URL 解码实现（只解码 %XX 格式，不处理 + 号，因为 token 中可能包含 +）
                std::string result;
                for (size_t i = 0; i < new_token.length(); ++i) {
                    if (new_token[i] == '%' && i + 2 < new_token.length()) {
                        int value = 0;
                        if (std::sscanf(new_token.substr(i + 1, 2).c_str(), "%x", &value) == 1) {
                            result += static_cast<char>(value);
                            i += 2;
                        } else {
                            result += new_token[i];
                        }
                    } else {
                        result += new_token[i];
                    }
                }
                decoded_token = result;
                LOG_INFO("[LiveModuleImpl] Token URL 解码完成, 原始长度: %zu, 解码后长度: %zu", 
                         new_token.size(), decoded_token.size());
            } else {
                LOG_DEBUG("[LiveModuleImpl] Token 不包含 URL 编码字符，直接使用");
            }
            
            // 3. 更新到 SDK
            auto live_stream_channel = rtc_channel_manager_->GetChannel(channel_info_.channel_name_);
            auto live_stream_channel_ptr = live_stream_channel.lock();
            if (!live_stream_channel_ptr) {
                LOG_ERROR("[LiveModuleImpl] live_stream_channel 为空，无法更新 token, channel_name: %s", 
                          channel_info_.channel_name_.c_str());
                return;
            }
            
            LOG_INFO("[LiveModuleImpl] 开始更新 token 到 SDK, channel_name: %s, token_length: %zu", 
                     channel_info_.channel_name_.c_str(), decoded_token.size());
            bool ret = live_stream_channel_ptr->RenewToken(decoded_token);
            if (ret) {
                LOG_INFO("[LiveModuleImpl] Token 更新成功, channel_name: %s", channel_info_.channel_name_.c_str());
            } else {
                LOG_ERROR("[LiveModuleImpl] Token 更新失败, channel_name: %s", channel_info_.channel_name_.c_str());
            }
        });
}

message_engine::ChannelProviderType LiveModuleImpl::getChannelProviderType(const LiveStreamStartResponse& response) {
    if (response.url_type == "agora") {
        // Agora whep URL 走内置 WebRTC，与 Agora native 无关。非 whep 的 agora 才需要 native，本构建默认没有。
        if (response.url.find("whep") != std::string::npos) {
            return message_engine::ChannelProviderType::WebRTC;
        }
#ifndef BUILD_WITH_AGORA_USER
        LOG_ERROR("LiveModuleImpl: url_type=agora (non-whep) but Agora RTC not enabled in this build");
        return message_engine::ChannelProviderType::UNKNOWN;
#else
        return message_engine::ChannelProviderType::Agora;
#endif
    } else if (response.url_type == "volc") {
#ifndef BUILD_WITH_VOLC_USER
        LOG_ERROR("LiveModuleImpl: url_type=volc but Volc RTC not enabled in this build");
        return message_engine::ChannelProviderType::UNKNOWN;
#else
        return message_engine::ChannelProviderType::VolcEngine;
#endif
    } else if (response.url_type == "drtc") {
        return message_engine::ChannelProviderType::DRTC;
    }
    // TODO: 其他类型
    return message_engine::ChannelProviderType::UNKNOWN;
}

void LiveModuleImpl::parseVolcUrlParams(const std::string& url, std::string& app_id, std::string& user_id, std::string& token) {
    const auto parameters = cloud_api2::parse_query(url);
    app_id = cloud_api2::query_param(parameters, "app_id");
    user_id = cloud_api2::query_param(parameters, "user_id");
    token = cloud_api2::query_param(parameters, "token");
}

void LiveModuleImpl::parseAgoraUrlParams(const std::string& url, std::string& app_id, std::string& channel_name, std::string& user_id, std::string& token) {
    // 声网 URL 参数：app_id, channel (对应 channel_name), uid (对应 user_id), token
    const auto parameters = cloud_api2::parse_query(url);
    app_id = cloud_api2::query_param(parameters, "app_id");
    channel_name = cloud_api2::query_param(parameters, "channel");
    user_id = cloud_api2::query_param(parameters, "uid");
    token = cloud_api2::query_param(parameters, "token");

    LOG_INFO("LiveModuleImpl: parseAgoraUrlParams, app_id: %s, channel: %s, uid: %s, token: %s",
             app_id.c_str(), channel_name.c_str(), user_id.c_str(), token.empty() ? "empty" : "set");
}

void LiveModuleImpl::parseDrtcUrlParams(const std::string& url, std::string& app_id, std::string& room_id,
                                         std::string& channel_name, std::string& client_id,
                                         std::string& token, std::string& provider_url) {
    // 解析 DRTC URL query 参数，对齐 Web parseDRTCUrl（decodeURIComponent）
    // URL 格式: app_id=xxx&room_id=xxx&channel_name=xxx&client_id=xxx&token=xxx&provider_url=xxx
    auto urlDecodePercent = [](const std::string& input) -> std::string {
        if (input.find('%') == std::string::npos) {
            return input;
        }
        // 只解 %XX；不把 + 当成空格（token 里可能含字面 +）
        std::string result;
        result.reserve(input.size());
        for (size_t i = 0; i < input.length(); ++i) {
            if (input[i] == '%' && i + 2 < input.length()) {
                int value = 0;
                if (std::sscanf(input.substr(i + 1, 2).c_str(), "%x", &value) == 1) {
                    result += static_cast<char>(value);
                    i += 2;
                } else {
                    result += input[i];
                }
            } else {
                result += input[i];
            }
        }
        return result;
    };

    auto getParam = [&url, &urlDecodePercent](const std::string& key) -> std::string {
        std::string search = key + "=";
        size_t pos = url.find(search);
        if (pos == std::string::npos) return "";
        pos += search.length();
        size_t end = url.find('&', pos);
        if (end == std::string::npos) end = url.length();
        return urlDecodePercent(url.substr(pos, end - pos));
    };

    app_id = getParam("app_id");
    if (app_id.empty()) app_id = getParam("appId");

    room_id = getParam("room_id");
    if (room_id.empty()) room_id = getParam("roomId");

    channel_name = getParam("channel_name");
    if (channel_name.empty()) channel_name = getParam("stream_id");

    client_id = getParam("client_id");
    if (client_id.empty()) client_id = getParam("clientId");

    token = getParam("token");

    provider_url = getParam("provider_url");
    if (provider_url.empty()) provider_url = getParam("providerUrl");

    LOG_INFO("LiveModuleImpl: parseDrtcUrlParams, app_id: %s, room_id: %s, channel_name: %s, client_id: %s, provider_url: %s, token_len: %zu",
             app_id.c_str(), room_id.c_str(), channel_name.c_str(), client_id.c_str(),
             provider_url.empty() ? "(empty)" : "(present)", token.size());
}

void LiveModuleImpl::setLiveQuality(const LiveQualitySettingParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("LiveModuleImpl: setLiveQuality(), sn: %s, camera: %s, video: %s, gateway_sn: %s, video_quality: %d",
             params.sn.c_str(), params.camera.c_str(), params.video.c_str(), gateway_sn_.c_str(), params.video_quality);

    LiveQualitySettingRequest request;
    request.sn            = params.sn;
    request.camera        = params.camera;
    request.video         = params.video;
    request.gateway_sn    = gateway_sn_;
    request.video_quality = params.video_quality;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()->void {
        WeakDummyReturn(weak_ptr);
        set_live_quality(request, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]()->void {
                LOG_INFO("LiveModuleImpl: setLiveQuality(), err_code = %d", (int)err.error_code);

                // 埋点: 设置直播画质; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_SET_LIVE_QUALITY, err, "[LiveModuleImpl]");

                if (callback) callback(err);
            });
        });
    });
}