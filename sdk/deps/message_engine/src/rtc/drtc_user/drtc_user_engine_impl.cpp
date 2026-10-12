// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drtc_user_engine_impl.h"
#include "drtc_user_channel_define.h"

#include <base/common/logger.h>
#include <base/io/byte_buffer_pool.h>

#include <cstring>
#include <fstream>
#include <functional>
#include <sstream>

#include <json11.hpp>

using namespace message_engine;

#define DRTC_USER_ERROR_NOT_INIT (-1)
#define DRTC_USER_ERROR_SDK_NULL (-2)

// DRTC SDK log callback
static void drtc_user_log_function(devicesdk_log_level_t level, const char* trace, int len) {
    std::string log_str(trace, len);
    switch (level) {
        case DEVICESDK_LOG_LEVEL_FATAL:
        case DEVICESDK_LOG_LEVEL_ERROR:
            LOG_ERROR("[DrtcUserLog] %s", log_str.c_str());
            break;
        case DEVICESDK_LOG_LEVEL_WARNING:
            LOG_WARN("[DrtcUserLog] %s", log_str.c_str());
            break;
        case DEVICESDK_LOG_LEVEL_INFO:
        case DEVICESDK_LOG_LEVEL_ALL:
            LOG_INFO("[DrtcUserLog] %s", log_str.c_str());
            break;
        case DEVICESDK_LOG_LEVEL_DEBUG:
        case DEVICESDK_LOG_LEVEL_TRACE:
            LOG_DEBUG("[DrtcUserLog] %s", log_str.c_str());
            break;
        default:
            break;
    }
}

int DrtcUserEngineImpl::GenerateListenerId() {
    static std::atomic<int> id(0);
    return ++id;
}

DrtcUserEngineImpl::DrtcUserEngineImpl(const std::string &config_path,
                                       const std::shared_ptr<drc_sdk::Executor> &executor)
    : config_path_(config_path), executor_(executor) {
    LOG_INFO("[DrtcUserEngineImpl] construct, config_path: %s", config_path_.c_str());
}

DrtcUserEngineImpl::~DrtcUserEngineImpl() {
    LOG_INFO("[DrtcUserEngineImpl] destruct");
    DeInit();
}

void DrtcUserEngineImpl::SetProviderUrl(const std::string &provider_url) {
    provider_url_ = provider_url;
}

bool DrtcUserEngineImpl::ParseProviderUrl(const std::string &url, std::string &host, int &port,
                                           std::string &path) {
    if (url.empty()) {
        return false;
    }
    const bool is_https = url.rfind("https://", 0) == 0;
    const bool is_http = url.rfind("http://", 0) == 0;
    if (!is_https && !is_http) {
        return false;
    }
    const size_t scheme_end = url.find("://") + 3;
    const size_t path_start = url.find('/', scheme_end);
    const std::string host_port = path_start == std::string::npos
                                      ? url.substr(scheme_end)
                                      : url.substr(scheme_end, path_start - scheme_end);
    path = path_start == std::string::npos ? "/mediaserver/arbitration" : url.substr(path_start);
    const size_t colon = host_port.find(':');
    if (colon != std::string::npos) {
        host = host_port.substr(0, colon);
        port = std::stoi(host_port.substr(colon + 1));
    } else {
        host = host_port;
        port = is_https ? 443 : 80;
    }
    return !host.empty();
}

std::string DrtcUserEngineImpl::ResolveConfigPath(const std::string &base_path,
                                                   const std::string &provider_url) {
    if (provider_url.empty() || base_path.empty()) {
        return base_path;
    }

    std::string host;
    int port = 443;
    std::string path;
    if (!ParseProviderUrl(provider_url, host, port, path)) {
        LOG_WARN("[DrtcUserEngineImpl] invalid provider_url, use base config: %s", provider_url.c_str());
        return base_path;
    }

    std::ifstream ifs(base_path);
    if (!ifs.is_open()) {
        LOG_WARN("[DrtcUserEngineImpl] cannot open base config: %s", base_path.c_str());
        return base_path;
    }
    std::stringstream ss;
    ss << ifs.rdbuf();
    std::string err;
    const auto json = json11::Json::parse(ss.str(), err);
    if (!err.empty() || !json.is_object()) {
        LOG_WARN("[DrtcUserEngineImpl] parse base config failed: %s", err.c_str());
        return base_path;
    }

    auto root = json.object_items();
    auto arbitration = root["arbitration"].object_items();
    auto https = arbitration["https"].object_items();
    https["host"] = host;
    https["port"] = port;
    https["path"] = path;
    arbitration["https"] = https;
    root["arbitration"] = arbitration;

    const std::string temp_path = "/tmp/fhsdk_drtc_node_" +
                                  std::to_string(std::hash<std::string>{}(base_path + provider_url)) + ".json";
    std::ofstream ofs(temp_path);
    if (!ofs.is_open()) {
        LOG_WARN("[DrtcUserEngineImpl] cannot write temp config: %s", temp_path.c_str());
        return base_path;
    }
    ofs << json11::Json(root).dump();
    LOG_INFO("[DrtcUserEngineImpl] provider_url applied, temp config: %s (host=%s, port=%d, path=%s)",
             temp_path.c_str(), host.c_str(), port, path.c_str());
    return temp_path;
}

bool DrtcUserEngineImpl::Init(const std::string &client_id) {
    LOG_INFO("[DrtcUserEngineImpl] Init client_id: %s", client_id.c_str());
    std::lock_guard<std::mutex> lock(init_mutex_);
    if (is_inited_) {
        LOG_INFO("[DrtcUserEngineImpl] already inited");
        return true;
    }

    const std::string config_to_use = ResolveConfigPath(config_path_, provider_url_);
    drtc_sdk_ = drtc::DrtcDeviceSdkFactory::getInstance(config_to_use.c_str());
    if (drtc_sdk_ == nullptr) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcDeviceSdkFactory::getInstance returned nullptr");
        return false;
    }

    client_id_ = client_id;
    memset(&self_client_, 0, sizeof(DrtcClient));
    strncpy(self_client_.client_id, client_id.c_str(), CLIENT_ID_LENGTH - 1);
    self_client_.device_type = DRTC_USER_DEVICE_TYPE_NODE;

    auto ret = drtc_sdk_->DrtcInit(this, drtc_user_log_function, DEVICESDK_LOG_LEVEL_INFO, self_client_);
    if (ret != DEVICESDK_SUCCESS) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcInit failed, code = %d", ret);
        drtc_sdk_ = nullptr;
        return false;
    }

    is_inited_ = true;
    LOG_INFO("[DrtcUserEngineImpl] Init success");
    return true;
}

bool DrtcUserEngineImpl::DeInit() {
    LOG_INFO("[DrtcUserEngineImpl] DeInit");
    std::lock_guard<std::mutex> lock(init_mutex_);
    if (!is_inited_) {
        return true;
    }
    if (drtc_sdk_ == nullptr) {
        LOG_ERROR("[DrtcUserEngineImpl] drtc_sdk_ is nullptr");
        return false;
    }

    // Leave room if still joined
    if (join_state_ == DrtcUserJoinRoomState::JOINED) {
        DrtcLeaveRoomReq req{};
        strncpy(req.room_id, room_id_.c_str(), ROOM_ID_LENGTH - 1);
        drtc_sdk_->DrtcLeaveRoom(req);
    }

    auto ret = drtc_sdk_->DrtcDeinit();
    if (ret != DEVICESDK_SUCCESS) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcDeinit failed, code = %d", ret);
    }

    is_inited_ = false;
    join_state_ = DrtcUserJoinRoomState::IDLE;
    drtc_sdk_ = nullptr;
    return true;
}

bool DrtcUserEngineImpl::GetIsInited() const {
    return is_inited_;
}

int DrtcUserEngineImpl::StartDrtcJoinRoom() {
    DrtcJoinRoomReq req{};
    strncpy(req.room_id, room_id_.c_str(), ROOM_ID_LENGTH - 1);
    strncpy(req.app_id, app_id_.c_str(), APP_ID_LENGTH - 1);
    strncpy(req.token, token_.c_str(), TOKEN_LENGTH - 1);

    join_state_ = DrtcUserJoinRoomState::JOINING;
    LOG_INFO("[DrtcUserEngineImpl] DrtcJoinRoom real room_id: %s, app_id: %s",
             room_id_.c_str(), app_id_.c_str());
    auto ret = drtc_sdk_->DrtcJoinRoom(req);
    if (ret != DEVICESDK_SUCCESS) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcJoinRoom failed, code = %d", ret);
        join_state_ = DrtcUserJoinRoomState::IDLE;
    }
    return ret;
}

void DrtcUserEngineImpl::NotifyJoinFailed(int error_code) {
    join_state_ = DrtcUserJoinRoomState::IDLE;
    DrtcUserJoinRoomCallback callback;
    {
        std::lock_guard<std::mutex> lock(join_room_callback_mutex_);
        callback = std::move(join_room_callback_);
        join_room_callback_ = nullptr;
    }
    if (callback) {
        DrtcJoinRoomResp empty{};
        empty.code = static_cast<drtc_response_code_t>(error_code);
        callback(room_id_.empty() ? reference_sn_ : room_id_, false, error_code, empty);
    }
}

int DrtcUserEngineImpl::JoinRoom(const std::string &app_id, const std::string &room_id,
                                  const std::string &token, const std::string &provider_url,
                                  const DrtcUserJoinRoomCallback &callback) {
    LOG_INFO("[DrtcUserEngineImpl] JoinRoom paas_room_id(reference_sn): %s, app_id: %s",
             room_id.c_str(), app_id.c_str());
    (void)provider_url;
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        LOG_ERROR("[DrtcUserEngineImpl] JoinRoom failed: not inited");
        return DRTC_USER_ERROR_NOT_INIT;
    }

    if (join_state_ != DrtcUserJoinRoomState::IDLE) {
        LOG_WARN("[DrtcUserEngineImpl] JoinRoom: already joining/joined, state=%d",
                 static_cast<int>(join_state_.load()));
        return DRTC_USER_ERROR_NOT_INIT;
    }

    app_id_ = app_id;
    reference_sn_ = room_id;
    room_id_.clear();
    token_ = token;

    {
        std::lock_guard<std::mutex> lock(join_room_callback_mutex_);
        join_room_callback_ = callback;
    }

    // PaaS stream/start room_id is device SN; arbitration requires the internal id.
    // Join is dispatched off the SDK exchange callback (re-entrancy hygiene).
    DrtcExchangeTokenReq ex{};
    strncpy(ex.app_id, app_id_.c_str(), APP_ID_LENGTH - 1);
    strncpy(ex.client_id, client_id_.c_str(), CLIENT_ID_LENGTH - 1);
    strncpy(ex.token, token_.c_str(), TOKEN_LENGTH - 1);
    strncpy(ex.reference_sn, reference_sn_.c_str(), DEVICE_SN_LENGTH - 1);
    strncpy(ex.device_type, "node", DEVICE_TYPE_LENGTH - 1);

    join_state_ = DrtcUserJoinRoomState::EXCHANGING;
    LOG_INFO("[DrtcUserEngineImpl] DrtcExchangeToken reference_sn: %s, client_id: %s",
             reference_sn_.c_str(), client_id_.c_str());
    auto ret = drtc_sdk_->DrtcExchangeToken(ex);
    if (ret != DEVICESDK_SUCCESS) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcExchangeToken call failed, code = %d", ret);
        join_state_ = DrtcUserJoinRoomState::IDLE;
        {
            std::lock_guard<std::mutex> lock(join_room_callback_mutex_);
            join_room_callback_ = nullptr;
        }
        return ret;
    }
    return 0;
}

int DrtcUserEngineImpl::LeaveRoom() {
    LOG_INFO("[DrtcUserEngineImpl] LeaveRoom room_id: %s, state=%d",
             room_id_.c_str(), static_cast<int>(join_state_.load()));
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }
    if (join_state_ == DrtcUserJoinRoomState::EXCHANGING ||
        join_state_ == DrtcUserJoinRoomState::JOINING) {
        join_state_ = DrtcUserJoinRoomState::IDLE;
        std::lock_guard<std::mutex> lock(join_room_callback_mutex_);
        join_room_callback_ = nullptr;
        LOG_WARN("[DrtcUserEngineImpl] LeaveRoom: abort in-flight exchange/join");
        return 0;
    }
    if (join_state_ != DrtcUserJoinRoomState::JOINED) {
        LOG_WARN("[DrtcUserEngineImpl] LeaveRoom: not joined");
        return 0;
    }

    DrtcLeaveRoomReq req{};
    strncpy(req.room_id, room_id_.c_str(), ROOM_ID_LENGTH - 1);

    auto ret = drtc_sdk_->DrtcLeaveRoom(req);
    if (ret != DEVICESDK_SUCCESS) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcLeaveRoom failed, code = %d", ret);
    }
    join_state_ = DrtcUserJoinRoomState::IDLE;
    return ret;
}

int DrtcUserEngineImpl::Publish(const DrtcStream &stream, const DrtcUserPublishCallback &callback) {
    LOG_INFO("[DrtcUserEngineImpl] Publish stream_id: %s, type: %d", stream.stream_id, stream.stream_type);
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }

    DrtcPublishReq req{};
    req.timeout_ms = DRTC_USER_DEFAULT_TIMEOUT_MS;
    strncpy(req.room_id, room_id_.c_str(), ROOM_ID_LENGTH - 1);
    req.stream = stream;

    // request_id 为 DRTC_OUT：由 SDK 分配，必须用回写值注册回调（不能用本地自增 id）
    int request_id = 0;
    auto ret = drtc_sdk_->DrtcPublish(req, request_id);
    if (ret != DEVICESDK_SUCCESS) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcPublish failed, code = %d", ret);
        return ret;
    }
    {
        std::lock_guard<std::mutex> lock(publish_callbacks_mutex_);
        publish_callbacks_[request_id] = callback;
    }
    LOG_INFO("[DrtcUserEngineImpl] DrtcPublish accepted, request_id: %d", request_id);
    return ret;
}

int DrtcUserEngineImpl::Unpublish(const DrtcStream &stream) {
    LOG_INFO("[DrtcUserEngineImpl] Unpublish stream_id: %s", stream.stream_id);
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }

    DrtcUnpublishReq req{};
    req.timeout_ms = DRTC_USER_DEFAULT_TIMEOUT_MS;
    strncpy(req.room_id, room_id_.c_str(), ROOM_ID_LENGTH - 1);
    req.stream = stream;

    int request_id = 0;
    auto ret = drtc_sdk_->DrtcUnpublish(req, request_id);
    return ret;
}

int DrtcUserEngineImpl::Subscribe(const DrtcClient &producer_client, const DrtcStream &producer_stream,
                                   const DrtcUserSubscribeCallback &callback) {
    LOG_INFO("[DrtcUserEngineImpl] Subscribe producer: %s, stream: %s, type: %d",
             producer_client.client_id, producer_stream.stream_id, producer_stream.stream_type);
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }

    DrtcSubscribeReq req{};
    req.timeout_ms = DRTC_USER_SUBSCRIBE_TIMEOUT_MS;
    strncpy(req.room_id, room_id_.c_str(), ROOM_ID_LENGTH - 1);
    req.producer_client = producer_client;
    req.producer_stream = producer_stream;

    // request_id 为 DRTC_OUT：由 SDK 分配，必须用回写值注册回调（不能用本地自增 id）
    // 否则应答里的 request_id 对不上 map → Subscribe VIDEO success / LiveModule start 回调丢失
    int request_id = 0;
    auto ret = drtc_sdk_->DrtcSubscribe(req, request_id);
    if (ret != DEVICESDK_SUCCESS) {
        LOG_ERROR("[DrtcUserEngineImpl] DrtcSubscribe failed, code = %d", ret);
        return ret;
    }
    {
        std::lock_guard<std::mutex> lock(subscribe_callbacks_mutex_);
        subscribe_callbacks_[request_id] = callback;
    }
    LOG_INFO("[DrtcUserEngineImpl] DrtcSubscribe accepted, request_id: %d", request_id);
    return ret;
}

int DrtcUserEngineImpl::Unsubscribe(const DrtcClient &producer_client, const DrtcStream &producer_stream) {
    LOG_INFO("[DrtcUserEngineImpl] Unsubscribe producer: %s, stream: %s",
             producer_client.client_id, producer_stream.stream_id);
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }

    DrtcUnsubscribeReq req{};
    req.timeout_ms = DRTC_USER_DEFAULT_TIMEOUT_MS;
    strncpy(req.room_id, room_id_.c_str(), ROOM_ID_LENGTH - 1);
    req.producer_client = producer_client;
    req.producer_stream = producer_stream;

    int request_id = 0;
    auto ret = drtc_sdk_->DrtcUnsubscribe(req, request_id);
    return ret;
}

int DrtcUserEngineImpl::SendData(const DrtcStream &stream, const std::shared_ptr<drc_sdk::ByteBuffer> &data) {
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }

    DeviceSdkData sdk_data{};
    sdk_data.vir_addr = reinterpret_cast<uint8_t*>(data->data());
    sdk_data.length = data->size();
    sdk_data.timeout_ms = DRTC_USER_DEFAULT_TIMEOUT_MS;

    auto ret = drtc_sdk_->DrtcSendData(stream, sdk_data);
    return ret;
}

int DrtcUserEngineImpl::UpdateToken(const std::string &token) {
    LOG_INFO("[DrtcUserEngineImpl] UpdateToken");
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }
    token_ = token;
    int request_id = 0;
    auto ret = drtc_sdk_->DrtcUpdateToken(token.c_str(), request_id);
    return ret;
}

int DrtcUserEngineImpl::RequestVideoKeyFrame(const DrtcClient &producer_client,
                                              const DrtcStream &producer_stream) {
    if (!is_inited_ || drtc_sdk_ == nullptr) {
        return DRTC_USER_ERROR_NOT_INIT;
    }
    auto ret = drtc_sdk_->DrtcRequestVideoKeyFrame(producer_client, producer_stream);
    return ret;
}

// Listener management
int DrtcUserEngineImpl::AddMessageReceiver(const std::shared_ptr<DrtcUserEngineMessageReceiver> &receiver) {
    auto id = GenerateListenerId();
    std::lock_guard<std::mutex> lock(message_receivers_mutex_);
    message_receivers_[id] = receiver;
    LOG_INFO("[DrtcUserEngineImpl] AddMessageReceiver id: %d", id);
    return id;
}

int DrtcUserEngineImpl::AddLiveStreamReceiver(const std::shared_ptr<DrtcUserEngineLiveStreamReceiver> &receiver) {
    auto id = GenerateListenerId();
    std::lock_guard<std::mutex> lock(live_stream_receivers_mutex_);
    live_stream_receivers_[id] = receiver;
    LOG_INFO("[DrtcUserEngineImpl] AddLiveStreamReceiver id: %d", id);
    return id;
}

int DrtcUserEngineImpl::AddEventsListener(const std::shared_ptr<DrtcUserEngineEventsListener> &listener) {
    auto id = GenerateListenerId();
    std::lock_guard<std::mutex> lock(events_listeners_mutex_);
    events_listeners_[id] = listener;
    LOG_INFO("[DrtcUserEngineImpl] AddEventsListener id: %d", id);
    return id;
}

bool DrtcUserEngineImpl::RemoveMessageReceiver(int listen_id) {
    std::lock_guard<std::mutex> lock(message_receivers_mutex_);
    if (message_receivers_.contains(listen_id)) {
        message_receivers_.erase(listen_id);
        return true;
    }
    return false;
}

bool DrtcUserEngineImpl::RemoveLiveStreamReceiver(int listen_id) {
    std::lock_guard<std::mutex> lock(live_stream_receivers_mutex_);
    if (live_stream_receivers_.contains(listen_id)) {
        live_stream_receivers_.erase(listen_id);
        return true;
    }
    return false;
}

bool DrtcUserEngineImpl::RemoveEventsListener(int listen_id) {
    std::lock_guard<std::mutex> lock(events_listeners_mutex_);
    if (events_listeners_.contains(listen_id)) {
        events_listeners_.erase(listen_id);
        return true;
    }
    return false;
}

std::vector<DrtcUserPublishedStreamInfo> DrtcUserEngineImpl::GetPublishedStreams() const {
    std::lock_guard<std::mutex> lock(published_streams_mutex_);
    return published_streams_;
}

std::string DrtcUserEngineImpl::GetRoomId() const {
    return room_id_;
}

DrtcUserJoinRoomState DrtcUserEngineImpl::GetJoinState() const {
    return join_state_;
}

// ======================== DrtcDeviceSdk::Listener Callbacks ========================

void DrtcUserEngineImpl::DrtcOnJoinRoomResponse(const DrtcJoinRoomResp &out) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnJoinRoomResponse code: %d, client_num: %d",
             out.code, out.client_num);

    WeakDummy(p);
    executor_->Dispatch([this, p, out]() {
        WeakDummyReturn(p);

        bool is_succeed = (out.code == RESPONSE_OK);
        if (is_succeed) {
            join_state_ = DrtcUserJoinRoomState::JOINED;
            // Cache published streams
            {
                std::lock_guard<std::mutex> lock(published_streams_mutex_);
                published_streams_.clear();
                for (int i = 0; i < out.client_num && i < MAX_ROOM_CLIENT_NUM; i++) {
                    DrtcUserPublishedStreamInfo info;
                    info.client = out.published_stream[i].client;
                    for (int j = 0; j < out.published_stream[i].stream_num && j < MAX_CLIENT_STREAM_NUM; j++) {
                        info.streams.push_back(out.published_stream[i].stream_list[j]);
                    }
                    if (!info.streams.empty()) {
                        published_streams_.push_back(info);
                    }
                }
            }
        } else {
            join_state_ = DrtcUserJoinRoomState::IDLE;
        }

        DrtcUserJoinRoomCallback callback;
        {
            std::lock_guard<std::mutex> lock(join_room_callback_mutex_);
            callback = std::move(join_room_callback_);
            join_room_callback_ = nullptr;
        }
        if (callback) {
            callback(room_id_, is_succeed, out.code, out);
        }
    });
}

void DrtcUserEngineImpl::DrtcOnLeaveRoomResponse(const DrtcLeaveRoomResp &out) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnLeaveRoomResponse code: %d", out.code);
    join_state_ = DrtcUserJoinRoomState::IDLE;
}

void DrtcUserEngineImpl::DrtcOnJoinRoomNotify(const DrtcJoinRoomNotify &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnJoinRoomNotify client: %s, device_type: %d",
             notify.client.client_id, notify.client.device_type);

    WeakDummy(p);
    executor_->Dispatch([this, p, notify]() {
        WeakDummyReturn(p);
        decltype(events_listeners_) listeners_tmp;
        {
            std::lock_guard<std::mutex> lock(events_listeners_mutex_);
            listeners_tmp = events_listeners_;
        }
        for (const auto &[id, weak_listener] : listeners_tmp) {
            auto listener = weak_listener.lock();
            if (listener) {
                listener->OnDrtcUserJoinRoomNotify(notify.client);
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnLeaveRoomNotify(const DrtcLeaveRoomNotify &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnLeaveRoomNotify client: %s, reason: %d",
             notify.client.client_id, notify.reason);

    WeakDummy(p);
    executor_->Dispatch([this, p, notify]() {
        WeakDummyReturn(p);
        decltype(events_listeners_) listeners_tmp;
        {
            std::lock_guard<std::mutex> lock(events_listeners_mutex_);
            listeners_tmp = events_listeners_;
        }
        for (const auto &[id, weak_listener] : listeners_tmp) {
            auto listener = weak_listener.lock();
            if (listener) {
                listener->OnDrtcUserLeaveRoomNotify(notify.client, notify.reason);
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnPublishStreamNotify(const DrtcPublishStreamNotify &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnPublishStreamNotify client: %s, stream: %s, type: %d",
             notify.client.client_id, notify.stream.stream_id, notify.stream.stream_type);

    // Update published streams cache
    {
        std::lock_guard<std::mutex> lock(published_streams_mutex_);
        bool found = false;
        for (auto &info : published_streams_) {
            if (std::string(info.client.client_id) == std::string(notify.client.client_id)) {
                PublishedStream ps;
                ps.stream = notify.stream;
                strncpy(ps.appendix, notify.appendix, APPENDIX_LENGTH - 1);
                info.streams.push_back(ps);
                found = true;
                break;
            }
        }
        if (!found) {
            DrtcUserPublishedStreamInfo new_info;
            new_info.client = notify.client;
            PublishedStream ps;
            ps.stream = notify.stream;
            strncpy(ps.appendix, notify.appendix, APPENDIX_LENGTH - 1);
            new_info.streams.push_back(ps);
            published_streams_.push_back(new_info);
        }
    }

    std::string appendix_str(notify.appendix);

    WeakDummy(p);
    executor_->Dispatch([this, p, notify, appendix_str]() {
        WeakDummyReturn(p);
        decltype(live_stream_receivers_) receivers_tmp;
        {
            std::lock_guard<std::mutex> lock(live_stream_receivers_mutex_);
            receivers_tmp = live_stream_receivers_;
        }
        for (const auto &[id, weak_receiver] : receivers_tmp) {
            auto receiver = weak_receiver.lock();
            if (receiver) {
                receiver->OnDrtcUserPublishStreamNotify(notify.client, notify.stream, appendix_str);
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnUnpublishStreamNotify(const DrtcUnpublishStreamNotify &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnUnpublishStreamNotify client: %s, stream: %s",
             notify.client.client_id, notify.stream.stream_id);

    // Update published streams cache
    {
        std::lock_guard<std::mutex> lock(published_streams_mutex_);
        for (auto &info : published_streams_) {
            if (std::string(info.client.client_id) == std::string(notify.client.client_id)) {
                auto it = std::remove_if(info.streams.begin(), info.streams.end(),
                    [&notify](const PublishedStream &ps) {
                        return std::string(ps.stream.stream_id) == std::string(notify.stream.stream_id);
                    });
                info.streams.erase(it, info.streams.end());
                break;
            }
        }
    }

    WeakDummy(p);
    executor_->Dispatch([this, p, notify]() {
        WeakDummyReturn(p);
        decltype(live_stream_receivers_) receivers_tmp;
        {
            std::lock_guard<std::mutex> lock(live_stream_receivers_mutex_);
            receivers_tmp = live_stream_receivers_;
        }
        for (const auto &[id, weak_receiver] : receivers_tmp) {
            auto receiver = weak_receiver.lock();
            if (receiver) {
                receiver->OnDrtcUserUnpublishStreamNotify(notify.client, notify.stream);
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnTokenWillExpireNotify(const DrtcTokenWillExpireNotify &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnTokenWillExpireNotify expires_in: %d", notify.expires_in_seconds);

    WeakDummy(p);
    executor_->Dispatch([this, p, notify]() {
        WeakDummyReturn(p);
        decltype(events_listeners_) listeners_tmp;
        {
            std::lock_guard<std::mutex> lock(events_listeners_mutex_);
            listeners_tmp = events_listeners_;
        }
        for (const auto &[id, weak_listener] : listeners_tmp) {
            auto listener = weak_listener.lock();
            if (listener) {
                listener->OnDrtcUserTokenWillExpire(notify.expires_in_seconds);
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnTokenDidExpireNotify(const DrtcTokenDidExpireNotify &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnTokenDidExpireNotify");

    WeakDummy(p);
    executor_->Dispatch([this, p]() {
        WeakDummyReturn(p);
        decltype(events_listeners_) listeners_tmp;
        {
            std::lock_guard<std::mutex> lock(events_listeners_mutex_);
            listeners_tmp = events_listeners_;
        }
        for (const auto &[id, weak_listener] : listeners_tmp) {
            auto listener = weak_listener.lock();
            if (listener) {
                listener->OnDrtcUserTokenDidExpire();
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnPublishResponse(const DrtcPublishResp &out, int request_id) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnPublishResponse code: %d, stream: %s, request_id: %d",
             out.code, out.stream.stream_id, request_id);

    WeakDummy(p);
    executor_->Dispatch([this, p, out, request_id]() {
        WeakDummyReturn(p);
        DrtcUserPublishCallback callback;
        {
            std::lock_guard<std::mutex> lock(publish_callbacks_mutex_);
            if (publish_callbacks_.contains(request_id)) {
                callback = std::move(publish_callbacks_[request_id]);
                publish_callbacks_.erase(request_id);
            }
        }
        if (callback) {
            callback(out.code == RESPONSE_OK, out.stream, out.code);
        } else {
            LOG_WARN("[DrtcUserEngineImpl] DrtcOnPublishResponse: no callback for request_id=%d "
                     "(register must use SDK-assigned id)", request_id);
        }
    });
}

void DrtcUserEngineImpl::DrtcOnUnpublishResponse(const DrtcUnpublishResp &out, int request_id) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnUnpublishResponse code: %d, stream: %s",
             out.code, out.stream.stream_id);
}

void DrtcUserEngineImpl::DrtcOnSubscribeResponse(const DrtcSubscribeResp &out, int request_id) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnSubscribeResponse code: %d, producer: %s, stream: %s, request_id: %d",
             out.code, out.producer_client.client_id, out.producer_stream.stream_id, request_id);

    WeakDummy(p);
    executor_->Dispatch([this, p, out, request_id]() {
        WeakDummyReturn(p);
        DrtcUserSubscribeCallback callback;
        {
            std::lock_guard<std::mutex> lock(subscribe_callbacks_mutex_);
            if (subscribe_callbacks_.contains(request_id)) {
                callback = std::move(subscribe_callbacks_[request_id]);
                subscribe_callbacks_.erase(request_id);
            }
        }
        if (callback) {
            callback(out.code == RESPONSE_OK, out);
        } else {
            LOG_WARN("[DrtcUserEngineImpl] DrtcOnSubscribeResponse: no callback for request_id=%d "
                     "(register must use SDK-assigned id)", request_id);
        }
    });
}

void DrtcUserEngineImpl::DrtcOnUnsubscribeResponse(const DrtcUnsubscribeResp &out, int request_id) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnUnsubscribeResponse code: %d", out.code);
}

void DrtcUserEngineImpl::DrtcOnUpdateTokenResponse(const DrtcUpdateTokenResp &out, int request_id) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnUpdateTokenResponse code: %d", out.code);
}

void DrtcUserEngineImpl::DrtcOnData(const DrtcClient &peer_client, const DrtcStream &stream, DeviceSdkData &data) {
    // SDK callback thread: copy payload then dispatch to executor
    auto buffer = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
    buffer->Assign(data.vir_addr, data.length);
    const auto stream_copy = stream;
    const std::string peer_client_id(peer_client.client_id);

    WeakDummy(p);
    executor_->Dispatch([this, p, peer_client_id, stream_copy, buffer]() {
        WeakDummyReturn(p);
        if (stream_copy.stream_type == DRTC_STREAM_VIDEO) {
            decltype(live_stream_receivers_) receivers_tmp;
            {
                std::lock_guard<std::mutex> lock(live_stream_receivers_mutex_);
                receivers_tmp = live_stream_receivers_;
            }
            for (const auto &[id, weak_receiver] : receivers_tmp) {
                auto receiver = weak_receiver.lock();
                if (receiver) {
                    receiver->OnDrtcUserVideoDataReceived(peer_client_id, stream_copy, buffer);
                }
            }
        } else if (stream_copy.stream_type == DRTC_STREAM_COMMAND ||
                   stream_copy.stream_type == DRTC_STREAM_CONTROL) {
            decltype(message_receivers_) receivers_tmp;
            {
                std::lock_guard<std::mutex> lock(message_receivers_mutex_);
                receivers_tmp = message_receivers_;
            }
            for (const auto &[id, weak_receiver] : receivers_tmp) {
                auto receiver = weak_receiver.lock();
                if (receiver) {
                    receiver->OnDrtcUserMessageReceived(peer_client_id, stream_copy, buffer);
                }
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnDataEvent(const DrtcClient &client, const DrtcStream &stream, DrtcDataEvent &event) {
    LOG_DEBUG("[DrtcUserEngineImpl] DrtcOnDataEvent type: %d", event.event_type);
}

void DrtcUserEngineImpl::DrtcOnMessageReceived(const DrtcClient &peer_client, DeviceSdkMessage &message) {
    LOG_DEBUG("[DrtcUserEngineImpl] DrtcOnMessageReceived from: %s, len: %d",
              peer_client.client_id, message.length);
}

void DrtcUserEngineImpl::DrtcOnUserMessage(const DrtcClient &peer_client, const DeviceSdkMessage &message, uint8_t transmission_type) {
    LOG_DEBUG("[DrtcUserEngineImpl] DrtcOnUserMessage from: %s, len: %d",
              peer_client.client_id, message.length);
}

void DrtcUserEngineImpl::DrtcOnDataBitrateChanged(uint32_t target_kbps) {
    LOG_DEBUG("[DrtcUserEngineImpl] DrtcOnDataBitrateChanged: %u kbps", target_kbps);
}

void DrtcUserEngineImpl::DrtcOnGlobalError(drtc_devicesdk_error_code_t code, const char *msg) {
    std::string msg_str = msg ? msg : "";
    LOG_ERROR("[DrtcUserEngineImpl] DrtcOnGlobalError code: %d, msg: %s", code, msg_str.c_str());

    WeakDummy(p);
    executor_->Dispatch([this, p, code, msg_str]() {
        WeakDummyReturn(p);
        decltype(events_listeners_) listeners_tmp;
        {
            std::lock_guard<std::mutex> lock(events_listeners_mutex_);
            listeners_tmp = events_listeners_;
        }
        for (const auto &[id, weak_listener] : listeners_tmp) {
            auto listener = weak_listener.lock();
            if (listener) {
                listener->OnDrtcUserGlobalError(code, msg_str);
            }
        }
    });
}

void DrtcUserEngineImpl::DrtcOnStats(const char *msg, uint32_t len) {
    // Stats logging - typically verbose, use debug level
}

void DrtcUserEngineImpl::DrtcOnClientKickoutNotify(const DrtcClientKickoutNotify &notify) {
    LOG_WARN("[DrtcUserEngineImpl] DrtcOnClientKickoutNotify client: %s, reason: %d",
             notify.client.client_id, notify.reason);
    join_state_ = DrtcUserJoinRoomState::IDLE;
}

void DrtcUserEngineImpl::DrtcOnPeerStateChangeNotify(const DrtcClientStateChangeNotify &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnPeerStateChangeNotify client: %s, event: %d",
             notify.client.client_id, notify.event);
}

void DrtcUserEngineImpl::DrtcOnLocalStateChangeNotify(const drtc_client_state_event_t &notify) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnLocalStateChangeNotify event: %d", notify);
}

void DrtcUserEngineImpl::DrtcOnExchangeTokenResponse(const DrtcExchangeTokenResp &out) {
    LOG_INFO("[DrtcUserEngineImpl] DrtcOnExchangeTokenResponse code: %d, room_id: %s, msg: %s",
             out.code, out.room_id, out.message);

    WeakDummy(p);
    executor_->Dispatch([this, p, out]() {
        WeakDummyReturn(p);
        if (join_state_ != DrtcUserJoinRoomState::EXCHANGING) {
            LOG_WARN("[DrtcUserEngineImpl] ignore ExchangeToken callback, state=%d",
                     static_cast<int>(join_state_.load()));
            return;
        }
        if (out.code != RESPONSE_OK || out.room_id[0] == '\0') {
            LOG_ERROR("[DrtcUserEngineImpl] ExchangeToken failed, code: %d, msg: %s (no SN Join fallback)",
                      out.code, out.message);
            NotifyJoinFailed(out.code != 0 ? out.code : DRTC_USER_ERROR_NOT_INIT);
            return;
        }

        room_id_ = out.room_id;
        if (out.token[0] != '\0') {
            token_ = out.token;
        }
        LOG_INFO("[DrtcUserEngineImpl] ExchangeToken success: %s -> %s",
                 reference_sn_.c_str(), room_id_.c_str());

        auto ret = StartDrtcJoinRoom();
        if (ret != 0) {
            NotifyJoinFailed(ret);
        }
    });
}

void DrtcUserEngineImpl::DrtcOnNetworkQualityNotify(const DrtcNetworkQualityNotify &notify) {
    LOG_DEBUG("[DrtcUserEngineImpl] DrtcOnNetworkQualityNotify uplink: %d, downlink: %d",
              notify.uplink_quality, notify.downlink_quality);
}

void DrtcUserEngineImpl::DrtcOnClientBandwidthNotify(const DrtcClient &peer_client, uint32_t bw_kbps) {
    LOG_DEBUG("[DrtcUserEngineImpl] DrtcOnClientBandwidthNotify client: %s, bw: %u kbps",
              peer_client.client_id, bw_kbps);
}
