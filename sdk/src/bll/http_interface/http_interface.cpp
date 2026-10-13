// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "http_interface.h"
#include "api/flight_hub_sdk.h"
#include "common/network/network.h"
#include <base/common/logger.h>
#include <common/util/guid.h>
#include "api/message.h"
#include "response_message.h"
#include "request_message.h"
#include <common/executors/executors.h>
#include "api/value.h"
#include "common.h"
#include <chrono>
#include <set>
#include <sstream>
#include <string>
#include <common/log_safe.h>
#include "http_runtime.h"

namespace flight_hub_sdk {

// 请求/响应体内容日志统一经 log_safe()(共享实现见
// deps/base/common/log_safe.h): 内容类日志降 DEBUG, 敏感命中整条不打印
using drc_sdk::common::log_safe;

// 用于在函数预检阶段（lambda 之前）调用 callback 时提供默认 http_code = 0
// 在 lambda 内部，lambda 参数 http_code 会遮蔽此值
static constexpr int http_code = 0;

// 首次发布固件时后端同步拆包并回传 minio，HTTP 超时 90s（HYAPP-180947）
static constexpr uint32_t kFirmwareReleaseHttpTimeoutMs = 90000;

void get_device_list_by_filter(const std::string& gateway_model_key, int page_index, int page_size, uint32_t timeout_ms, const std::function<void(const DeviceTopoListResponse&, FlightHubError err)> callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kDeviceTopoListAPI;

    char param_buffer[128] = {'\0'};
    if (gateway_model_key.empty()) {
        snprintf(param_buffer, sizeof(param_buffer), "page_num=%d&page_size=%d", page_index, page_size);
    } else {
        snprintf(param_buffer, sizeof(param_buffer), "model_key=%s&page_num=%d&page_size=%d", gateway_model_key.c_str(), page_index, page_size);
    }


    std::string complete_api = api + "?" + param_buffer;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("[DeviceListManager] get_device_list_by_filter(), { url: %s, uuid: %s, page_index: %d, page_size: %d, timeout_ms: %u ms }",
        url.c_str(), uuid.str().c_str(), page_index, page_size, timeout_ms
    );

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("[DeviceListManager] get_device_list_by_filter(), { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    break;
                }

                DeviceTopoListResponse device_topo_list_response;
                std::string err;
                if (!parse_common_response_data(common_response, device_topo_list_response, &err, true)) {
                    LOG_ERROR("[DeviceListManager] get_device_list_by_filter(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    break;
                }
                callback(device_topo_list_response, FlightHubError::success());
                return;
            } while (0);

            // indicate error;
            callback({}, make_http_error(http_code, response));
            return;
        },
        timeout_ms
    );
}

// 切线程
// page=1&page_size=500&device_model_class=base_station
// page=1&page_size=500&device_model_class=airport
void get_device_topo(const std::string& device_sn, const std::function<void(const DeviceTopoResponse&, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDevicePropertiesAPI);
    url += "?device_sn=" + device_sn;
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_device_topo(properties): url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, device_sn](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            LOG_DEBUG("get_device_topo: raw HTTP response (size: %zu): %s", response.size(), log_safe(response).c_str());

            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_device_topo: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }

            DeviceTopoResponse device_topo_response;
            std::string err;
            if (!parse_common_response_data(common_response, device_topo_response, &err, true)) {
                LOG_ERROR("get_device_topo: JsonToObject error: %s", err.c_str());
                break;
            }

            if (!device_topo_response.list.empty()) {
                const auto& item = device_topo_response.list[0];
                LOG_INFO("get_device_topo: sn=%s, online=%d, live_capacity device_list.size=%zu",
                         item.sn.c_str(), item.online_status,
                         item.properties.live_capacity.value.device_list.size());
            }

            callback(device_topo_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    });
}

void start_live_stream(const LiveInfo& live_info, const std::function<void(const LiveStreamStartResponse&, FlightHubError err)> callback, const std::string& gateway_sn) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveStreamStartAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    LiveStreamStartRequest request;
    request.sn = live_info.device_sn;
    request.camera = live_info.payload_index;
    request.video = live_info.video_id;
    if (!gateway_sn.empty()) {
        request.gateway_sn = gateway_sn;
    }

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("StartLiveStream: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("StartLiveStream: url %s, uuid %s, sn %s, camera %s, video %s, body %s", 
        url.c_str(), uuid.str().c_str(), live_info.device_sn.c_str(), 
        live_info.payload_index.c_str(), live_info.video_id.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback, live_info](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            // 检查网络错误
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("StartLiveStream: network error: %d, sn: %s, camera: %s, response size: %zu", 
                    static_cast<int>(err_code), live_info.device_sn.c_str(), live_info.payload_index.c_str(), response.size());
                if (!response.empty()) {
                    LOG_ERROR("StartLiveStream: raw response: %s", log_safe(response).c_str());
                }
                callback({}, make_http_error(http_code, response));
                return;
            }
            
            // 打印原始 HTTP 响应
            LOG_DEBUG("StartLiveStream: raw HTTP response (size: %zu): %s", response.size(), log_safe(response).c_str());
            
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("StartLiveStream: code: %d, message: %s, sn: %s, camera: %s", 
                    common_response.code, common_response.message.c_str(), 
                    live_info.device_sn.c_str(), live_info.payload_index.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            
            LiveStreamStartResponse live_stream_start_response;
            std::string err;
            if (!parse_common_response_data(common_response, live_stream_start_response, &err, true)) {
                LOG_ERROR("StartLiveStream: JsonToObject error: %s, data: %s, sn: %s, camera: %s", 
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", 
                    live_info.device_sn.c_str(), live_info.payload_index.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            
            LOG_INFO("StartLiveStream: success, sn: %s, camera: %s, video: %s", 
                live_info.device_sn.c_str(), live_info.payload_index.c_str(), live_info.video_id.c_str());
            callback(live_stream_start_response, FlightHubError::success());
    });
}

void stop_live_stream(const LiveInfo& live_info, const std::function<void(FlightHubError err)> callback, const std::string& gateway_sn) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveStreamStopAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    LiveStreamStartRequest request;
    request.sn = live_info.device_sn;
    request.camera = live_info.payload_index;
    request.video = live_info.video_id;
    if (!gateway_sn.empty()) {
        request.gateway_sn = gateway_sn;
    }

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("StopLiveStream: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("StopLiveStream: url %s, uuid %s, sn %s, camera %s, video %s, body %s",
        url.c_str(), uuid.str().c_str(), live_info.device_sn.c_str(),
        live_info.payload_index.c_str(), live_info.video_id.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, live_info](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("StopLiveStream: network error: %d, sn: %s, camera: %s, response size: %zu",
                    static_cast<int>(err_code), live_info.device_sn.c_str(), live_info.payload_index.c_str(), response.size());
                if (!response.empty()) {
                    LOG_ERROR("StopLiveStream: raw response: %s", log_safe(response).c_str());
                }
                callback(make_http_error(http_code, response));
                return;
            }

            LOG_DEBUG("StopLiveStream: raw HTTP response (size: %zu): %s", response.size(), log_safe(response).c_str());

            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("StopLiveStream: code: %d, message: %s, sn: %s, camera: %s",
                    common_response.code, common_response.message.c_str(),
                    live_info.device_sn.c_str(), live_info.payload_index.c_str());
                callback(make_http_error(http_code, response));
                return;
            }

            LOG_INFO("StopLiveStream: success, sn: %s, camera: %s, video: %s",
                live_info.device_sn.c_str(), live_info.payload_index.c_str(), live_info.video_id.c_str());
            callback(FlightHubError::success());
    });
}

void update_live_stream_state(const LiveInfo& live_info, int state, const std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveStreamStateUpdateAPI);
    auto uuid = xg::newGuid();
    
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    LiveStreamStateUpdateRequest request;
    request.sn = live_info.device_sn;
    request.camera = live_info.payload_index;
    request.video = live_info.video_id;
    request.uid = "0";
    request.state = state;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("UpdateLiveStreamState: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("UpdateLiveStreamState:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback, live_info](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("UpdateLiveStreamState: network error: %d, sn: %s", static_cast<int>(err_code), live_info.device_sn.c_str());
            callback(make_http_error(http_code, response));
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("UpdateLiveStreamState: code: %d, message: %s", common_response.code, common_response.message.c_str());
            callback(make_http_error(http_code, response));
            return;
        }
        callback(FlightHubError::success());
    });
}

void update_live_stream_token(const LiveInfo& live_info, const std::string& user_id, const std::string& provider, const std::function<void(FlightHubError err, const std::string& token)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveStreamStateUpdateTokenAPI);
    auto uuid = xg::newGuid();
 
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    LiveStreamStateUpdateTokenRequest request;
    LiveStreamStateUpdateTokenItem item;
    item.sn = live_info.device_sn;
    item.camera = live_info.payload_index;
    item.user_id = user_id;  // 使用传入的 user_id，而不是硬编码
    request.list.push_back(item);

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("UpdateLiveStreamToken: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED), "");
        return;
    }
    LOG_DEBUG("UpdateLiveStreamToken:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback, live_info, user_id, provider](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            // 日志不落任何响应数据, 仅记录 size
            LOG_INFO("UpdateLiveStreamToken: 收到后端响应, response size: %zu", response.size());
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("UpdateLiveStreamToken: network error: %d", static_cast<int>(err_code));
                callback(make_http_error(http_code, response), "");
                return;
            }
            
            auto common_response = convert_response_struct(response);
            LOG_INFO("UpdateLiveStreamToken: 解析 common_response, code: %d, message: %s, data size: %zu", 
                     common_response.code, common_response.message.c_str(), common_response.data.has_value());
            
            if (common_response.code != 0) {
                // 不落响应数据, 仅保留 code/message
                LOG_ERROR("UpdateLiveStreamToken: code: %d, message: %s (response size: %zu)",
                        common_response.code, common_response.message.c_str(), response.size());
                callback(make_http_error(http_code, response), "");
                return;
            }
            
            // 解析响应中的 token
            // 响应格式: {"code": 0, "message": "OK", "data": {"list": [{"provider": "...", "app_id": "...", "room_id": "...", "user_id": "...", "token": "..."}]}}
            std::string new_token = "";
            if (common_response.data.has_value()) {
                // 不打印具体内容
                LOG_DEBUG("UpdateLiveStreamToken: 开始解析 data 字段, data size: %zu",
                          common_response.data.has_value() ? (*common_response.data).size() : 0);
                
                // 解析 data 字段（包含 list 字段的对象）
                UpdateLiveTokenListResponse token_list_response;
                std::string parse_err;
                parse_common_response_data(common_response, token_list_response, &parse_err, false);
                
                if (parse_err.empty() && !token_list_response.list.empty()) {
                    const auto& token_list = token_list_response.list;
                    LOG_INFO("UpdateLiveStreamToken: 成功解析 token_list, 数量: %zu, 期望的 user_id: %s", 
                             token_list.size(), user_id.c_str());
                    
                    // 打印所有 token 项的信息，方便调试
                    for (size_t i = 0; i < token_list.size(); ++i) {
                        const auto& item = token_list[i];
                        LOG_INFO("UpdateLiveStreamToken: token_list[%zu]: provider=%s, app_id=%s, room_id=%s, user_id=%s, token_length=%zu", 
                                 i, item.provider.c_str(), item.app_id.c_str(), item.room_id.c_str(), 
                                 item.user_id.c_str(), item.token.size());
                    }
                    
                    // 查找匹配的 user_id 和 provider（如果提供了 provider）
                    for (const auto& item : token_list) {
                        // 首先匹配 user_id
                        if (item.user_id != user_id) {
                            continue;
                        }
                        // 如果提供了 provider，还需要匹配 provider
                        if (!provider.empty() && item.provider != provider) {
                            LOG_DEBUG("UpdateLiveStreamToken: user_id 匹配但 provider 不匹配, user_id: %s, 期望 provider: %s, 实际 provider: %s", 
                                     user_id.c_str(), provider.c_str(), item.provider.c_str());
                            continue;
                        }
                        new_token = item.token;
                        LOG_INFO("UpdateLiveStreamToken: 成功找到匹配的 token, user_id: %s, provider: %s, token_length: %zu", 
                                 user_id.c_str(), item.provider.c_str(), new_token.size());
                        break;
                    }
                    if (new_token.empty()) {
                        LOG_WARN("UpdateLiveStreamToken: 未找到匹配的 token, user_id: %s, provider: %s, 可用的 token 列表:", 
                                user_id.c_str(), provider.empty() ? "(任意)" : provider.c_str());
                        for (const auto& item : token_list) {
                            LOG_WARN("UpdateLiveStreamToken:   - provider: %s, user_id: %s", item.provider.c_str(), item.user_id.c_str());
                        }
                    }
                } else {
                    // 不落响应数据, 仅保留错误摘要
                    LOG_ERROR("UpdateLiveStreamToken: 解析响应 data 失败, parse_err: %s (data size: %zu, response size: %zu)",
                              parse_err.c_str(),
                              common_response.data.has_value() ? (*common_response.data).size() : 0,
                              response.size());
                }
            } else {
                LOG_WARN("UpdateLiveStreamToken: 响应 data 为空, response size: %zu", response.size());
            }
            
            if (new_token.empty()) {
                // 不落响应数据, 仅保留摘要
                LOG_ERROR("UpdateLiveStreamToken: 未能获取到有效的 token, response size: %zu", response.size());
                callback(make_http_error(http_code, response), "");
                return;
            }
            
            LOG_INFO("UpdateLiveStreamToken: 成功获取 token, token_length: %zu", new_token.size());
            callback(FlightHubError::success(), new_token);
    });
}

void change_dock_camera_position(const LiveInfo& live_info, int camera_position, const std::string& gateway_sn, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveStreamChangeCameraAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    ChangeDockCameraPositionRequest request;
    request.sn = live_info.device_sn;
    request.camera = live_info.payload_index;
    request.video = live_info.video_id;
    request.gateway_sn = gateway_sn;
    request.camera_position = camera_position;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("ChangeDockCameraPosition: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_INFO("ChangeDockCameraPosition: url %s, sn %s, camera %s, video %s, camera_position %d", 
        url.c_str(), live_info.device_sn.c_str(), live_info.payload_index.c_str(), live_info.video_id.c_str(), camera_position);
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, live_info, camera_position](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("ChangeDockCameraPosition: network error: %d, sn: %s", static_cast<int>(err_code), live_info.device_sn.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("ChangeDockCameraPosition: code: %d, message: %s, sn: %s", 
                    common_response.code, common_response.message.c_str(), live_info.device_sn.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            LOG_INFO("ChangeDockCameraPosition: success, sn: %s, camera_position: %d", live_info.device_sn.c_str(), camera_position);
            callback(FlightHubError::success());
        });
}

void start_live_record(const LiveInfo& live_info, const std::string& gateway_sn, const std::string& time_zone, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveRecordStartAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();
    StartLiveRecordRequest request;
    request.sn = live_info.device_sn;
    request.camera = live_info.payload_index;
    request.video = live_info.video_id;
    if (!gateway_sn.empty()) {
        request.gateway_sn = gateway_sn;
    }
    if (!time_zone.empty()) {
        request.time_zone = time_zone;
    }
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("StartLiveRecord: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("StartLiveRecord: url %s, sn %s, body %s", url.c_str(), live_info.device_sn.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, live_info](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("StartLiveRecord: network error: %d, sn: %s", static_cast<int>(err_code), live_info.device_sn.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("StartLiveRecord: code: %d, message: %s, sn: %s", common_response.code, common_response.message.c_str(), live_info.device_sn.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            LOG_INFO("StartLiveRecord: success, sn: %s", live_info.device_sn.c_str());
            callback(FlightHubError::success());
        });
}

void stop_live_record(const LiveInfo& live_info, const std::string& gateway_sn, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveRecordStopAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();
    StartLiveRecordRequest request;
    request.sn = live_info.device_sn;
    request.camera = live_info.payload_index;
    request.video = live_info.video_id;
    if (!gateway_sn.empty()) {
        request.gateway_sn = gateway_sn;
    }
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("StopLiveRecord: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("StopLiveRecord: url %s, sn %s, body %s", url.c_str(), live_info.device_sn.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, live_info](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("StopLiveRecord: network error: %d, sn: %s", static_cast<int>(err_code), live_info.device_sn.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("StopLiveRecord: code: %d, message: %s, sn: %s", common_response.code, common_response.message.c_str(), live_info.device_sn.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            LOG_INFO("StopLiveRecord: success, sn: %s", live_info.device_sn.c_str());
            callback(FlightHubError::success());
        });
}

void get_device_live_records(const std::string& device_sn, const std::function<void(const std::vector<LiveRecordItem>& records, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kLiveRecordDeviceAPI).replace(kLiveRecordDeviceAPI.find("{sn}"), 5, device_sn);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    LOG_INFO("GetDeviceLiveRecords: url %s, sn %s", url.c_str(), device_sn.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, device_sn](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("GetDeviceLiveRecords: network error: %d, sn: %s", static_cast<int>(err_code), device_sn.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GetDeviceLiveRecords: code: %d, message: %s, sn: %s", common_response.code, common_response.message.c_str(), device_sn.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            GetDeviceLiveRecordsResponse resp;
            std::string err;
            if (!parse_common_response_data(common_response, resp, &err, true)) {
                LOG_ERROR("GetDeviceLiveRecords: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                callback({}, make_http_error(http_code, response));
                return;
            }
            std::vector<LiveRecordItem> records;
            for (const auto& r : resp.list) {
                LiveRecordItem item;
                item.bid = r.bid;
                item.sn = r.sn;
                item.state = r.state;
                item.camera = r.camera;
                item.created_by = r.created_by;
                item.duration = r.duration;
                records.push_back(item);
            }
            LOG_INFO("GetDeviceLiveRecords: success, sn: %s, count: %zu", device_sn.c_str(), records.size());
            callback(records, FlightHubError::success());
        }, 10000);
}

void post_audience_count(const std::vector<DeviceAudienceCountRequestItem>& device_list, const AudienceCountGroup* group, const std::function<void(const std::vector<DeviceAudienceCountItem>& result, FlightHubError err)>& callback) {
    if (device_list.empty()) {
        LOG_ERROR("PostAudienceCount: device_list empty");
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAudienceCountAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    AudienceCountRequest req;
    for (const auto& item : device_list) {
        AudienceCountRequestItem ri;
        ri.sn = item.sn;
        req.device_list.push_back(ri);
    }
    if (group) {
        req.group.level_1_group = group->level_1_group;
        req.group.level_2_group = group->level_2_group;
    }

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(req, body, &err);
    if (!err.empty()) {
        LOG_ERROR("PostAudienceCount: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_INFO("PostAudienceCount: url %s, device_list size: %zu", url.c_str(), device_list.size());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("PostAudienceCount: network error: %d", static_cast<int>(err_code));
                callback({}, make_http_error(http_code, response));
                return;
            }
            LOG_DEBUG("PostAudienceCount: raw response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("PostAudienceCount: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            AudienceCountResponse resp;
            std::string parse_err;
            if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
                LOG_ERROR("PostAudienceCount: JsonToObject error: %s, data: %s", parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                callback({}, make_http_error(http_code, response));
                return;
            }
            std::vector<DeviceAudienceCountItem> result;
            for (const auto& r : resp.device_list) {
                DeviceAudienceCountItem item;
                item.sn = r.sn;
                item.count = r.count;
                result.push_back(item);
            }
            LOG_INFO("PostAudienceCount: success, result size: %zu", result.size());
            callback(result, FlightHubError::success());
        }, 10000);
}

// ---------- 航线任务 HTTP 实现 -----------
static std::string flight_task_api_with_uuid(const std::string& api_template, const std::string& task_uuid) {
    std::string api = api_template;
    const std::string ph = "{task_uuid}";
    size_t pos = api.find(ph);
    if (pos != std::string::npos) api.replace(pos, ph.size(), task_uuid);
    return api;
}

static CreateFlightTaskBreakPointRequest to_create_flight_task_break_point(const WaylineBreakPoint& bp) {
    CreateFlightTaskBreakPointRequest out;
    out.index = bp.index;
    out.state = bp.state;
    out.progress = bp.progress;
    out.wayline_id = bp.wayline_id;
    out.break_reason = bp.break_reason;
    out.latitude = bp.latitude;
    out.longitude = bp.longitude;
    out.height = bp.height;
    out.attitude_head = bp.attitude_head;
    return out;
}

static std::string build_flight_task_list_query(const FlightTaskListParams& params) {
    std::ostringstream q;
    q << "page=" << params.page << "&page_size=" << params.page_size;
    for (const auto& s : params.sn) q << "&sn=" << s;
    if (!params.begin_at.empty()) q << "&begin_at=" << params.begin_at;
    if (!params.end_at.empty()) q << "&end_at=" << params.end_at;
    if (!params.name.empty()) q << "&name=" << params.name;
    for (int st : params.status) q << "&status=" << st;
    if (params.task_type != 0) q << "&task_type=" << params.task_type;
    if (params.media_upload_type != 0) q << "&media_upload_type=" << params.media_upload_type;
    return q.str();
}

void get_flight_task_list(const FlightTaskListParams& params, const std::function<void(FlightHubError err, const FlightTaskListResult& result)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string query = build_flight_task_list_query(params);
    std::string api = kFlightTaskListAPI + "?" + query;
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = xg::newGuid().str();
    HttpGetInnerWithStatus(url, headers, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("GetFlightTaskList: network error %d", static_cast<int>(err_code));

            auto network_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_LIST, network_err, "[get_flight_task_list]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("GetFlightTaskList: code %d, message %s", common_response.code, common_response.message.c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_LIST, server_err, "[get_flight_task_list]");

            callback(server_err, {});
            return;
        }
        FlightTaskListDataResponse resp;
        std::string parse_err;
        if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
            LOG_ERROR("GetFlightTaskList: parse error %s", parse_err.c_str());

            auto data_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_LIST, data_err, "[get_flight_task_list]");

            callback(data_err, {});
            return;
        }
        FlightTaskListResult result;
        result.pagination.page = resp.pagination.page;
        result.pagination.page_size = resp.pagination.page_size;
        result.pagination.total = resp.pagination.total;
        for (const auto& r : resp.list) {
            TaskDetail d;
            d.uuid = r.uuid; d.name = r.name; d.task_type = r.task_type; d.status = r.status; d.sn = r.sn;
            d.landing_dock_sn = r.landing_dock_sn; d.wayline_uuid = r.wayline_uuid; d.begin_at = r.begin_at; d.end_at = r.end_at;
            d.out_of_control_action_in_flight = r.out_of_control_action_in_flight; d.rth_altitude = r.rth_altitude; d.rth_mode = r.rth_mode;
            d.min_battery_capacity = r.min_battery_capacity; d.wayline_precision_type = r.wayline_precision_type; d.wayline_segment_indexes = r.wayline_segment_indexes;
            d.current_waypoint_index = r.current_waypoint_index; d.total_waypoints = r.total_waypoints; d.time_zone = r.time_zone; d.device_type = r.device_type;
            d.bid = r.bid;
            d.break_point.index = r.break_point.index;
            d.break_point.state = r.break_point.state;
            d.break_point.progress = r.break_point.progress;
            d.break_point.wayline_id = r.break_point.wayline_id;
            d.break_point.break_reason = r.break_point.break_reason;
            d.break_point.latitude = r.break_point.latitude;
            d.break_point.longitude = r.break_point.longitude;
            d.break_point.height = r.break_point.height;
            d.break_point.attitude_head = r.break_point.attitude_head;
            d.completed_at = r.completed_at;
            d.created_at = r.created_at;
            d.deleted_at = r.deleted_at;
            d.is_break_point_resume = r.is_break_point_resume;
            d.media_upload_status = r.media_upload_status;
            for (const auto& obs : r.obstacles) {
                WaylineObstacle o;
                o.id = obs.id; o.type = obs.type; o.timestamp = obs.timestamp;
                o.latitude = obs.latitude; o.longitude = obs.longitude; o.height = obs.height;
                o.wayline_id = obs.wayline_id; o.waypoint_index = obs.waypoint_index;
                d.obstacles.push_back(o);
            }
            d.run_at = r.run_at;
            d.tags = r.tags;
            d.updated_at = r.updated_at;
            d.wayline_duration = r.wayline_duration;
            result.list.push_back(d);
        }

        // 埋点: 查询飞行任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_LIST, FlightHubError::success(), "[get_flight_task_list]");

        callback(FlightHubError::success(), result);
    }, 10000);
}

static std::string build_command_task_list_query(const GetCommandTaskListParams& params) {
    std::ostringstream q;
    q << "page=" << params.page << "&page_size=" << params.page_size;
    if (!params.sn.empty()) q << "&sn=" << params.sn;
    if (!params.drone_sn.empty()) q << "&drone_sn=" << params.drone_sn;
    if (params.begin_at >= 0) q << "&begin_at=" << params.begin_at;
    if (params.end_at >= 0) q << "&end_at=" << params.end_at;
    for (int st : params.status) q << "&status=" << st;
    return q.str();
}

void get_command_task_list(const GetCommandTaskListParams& params, const std::function<void(FlightHubError err, const GetCommandTaskListResults& results)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string query = build_command_task_list_query(params);
    std::string api = kCommandTaskListAPI + "?" + query;
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = xg::newGuid().str();
    HttpGetInnerWithStatus(url, headers, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("GetCommandTaskList: network error %d", static_cast<int>(err_code));

            auto network_err = make_http_error(http_code, response);

            // 埋点: 查询指令任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_COMMAND_TASK_GET_LIST, network_err, "[get_command_task_list]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("GetCommandTaskList: code %d, message %s", common_response.code, common_response.message.c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 查询指令任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_COMMAND_TASK_GET_LIST, server_err, "[get_command_task_list]");

            callback(server_err, {});
            return;
        }
        GetCommandTaskListDataResponse resp;
        std::string parse_err;
        if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
            LOG_ERROR("GetCommandTaskList: parse error %s", parse_err.c_str());

            auto data_err = make_http_error(http_code, response);

            // 埋点: 查询指令任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_COMMAND_TASK_GET_LIST, data_err, "[get_command_task_list]");

            callback(data_err, {});
            return;
        }
        GetCommandTaskListResults results;
        results.pagination.page = resp.pagination.page;
        results.pagination.page_size = resp.pagination.page_size;
        results.pagination.total = resp.pagination.total;
        for (const auto& r : resp.list) {
            CommandTaskListItemData item;
            item.uuid = r.uuid;
            item.sn = r.sn;
            item.drone_sn = r.drone_sn;
            item.status = r.status;
            item.err_code = r.err_code;
            item.takeoff_mode = r.takeoff_mode;
            item.security_takeoff_height = r.security_takeoff_height;
            item.max_speed = r.max_speed;
            item.rth_altitude = r.rth_altitude;
            item.created_at = r.created_at;
            item.user_id = r.user_id;
            results.list.push_back(item);
        }

        // 埋点: 查询指令任务列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_COMMAND_TASK_GET_LIST, FlightHubError::success(), "[get_command_task_list]");

        callback(FlightHubError::success(), results);
    }, 10000);
}

void get_flight_task_track(const GetFlightTaskTrackParams& params, const std::function<void(FlightHubError err, const GetFlightTaskTrackResults& results)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kFlightTaskTrackAPI + "?task_uuid=" + params.task_uuid;
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = xg::newGuid().str();
    HttpGetInnerWithStatus(url, headers, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("GetFlightTaskTrack: network error %d", static_cast<int>(err_code));

            auto network_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务轨迹; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_TRACK, network_err, "[get_flight_task_track]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("GetFlightTaskTrack: code %d, message %s", common_response.code, common_response.message.c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务轨迹; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_TRACK, server_err, "[get_flight_task_track]");

            callback(server_err, {});
            return;
        }
        GetFlightTaskTrackDataResponse resp;
        std::string parse_err;
        if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
            LOG_ERROR("GetFlightTaskTrack: parse error %s", parse_err.c_str());

            auto data_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务轨迹; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_TRACK, data_err, "[get_flight_task_track]");

            callback(data_err, {});
            return;
        }
        GetFlightTaskTrackResults results;
        results.uuid = resp.uuid;
        results.run_at = resp.run_at;
        results.completed_at = resp.completed_at;
        results.name = resp.name;
        results.status = resp.status;
        results.wayline_uuid = resp.wayline_uuid;
        results.track.track_id = resp.track.track_id;
        results.track.drone_sn = resp.track.drone_sn;
        results.track.flight_distance = resp.track.flight_distance;
        results.track.flight_duration = resp.track.flight_duration;
        for (const auto& pt : resp.track.points) {
            FlightTrackPoint point;
            point.timestamp = pt.timestamp;
            point.latitude = pt.latitude;
            point.longitude = pt.longitude;
            point.height = pt.height;
            results.track.points.push_back(point);
        }

        // 埋点: 查询飞行任务轨迹; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_TRACK, FlightHubError::success(), "[get_flight_task_track]");

        callback(FlightHubError::success(), results);
    }, 10000);
}

void create_flight_task(const CreateTaskReq& req, const std::function<void(FlightHubError err, const CreateTaskResp& result)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFlightTaskCreateAPI);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    CreateFlightTaskRequest body;
    body.name = req.name;
    body.sn = req.sn;
    body.task_type = req.task_type;
    body.time_zone = req.time_zone;
    body.wayline_uuid = req.wayline_uuid;
    body.out_of_control_action_in_flight = req.out_of_control_action_in_flight;
    body.rth_altitude = req.rth_altitude;
    if (req.rth_mode != -1) body.rth_mode = req.rth_mode;
    if (req.begin_at != -1) body.begin_at = req.begin_at;
    if (req.end_at != -1) body.end_at = req.end_at;
    if (req.device_type != -1) body.device_type = req.device_type;
    if (!req.landing_dock_sn.empty()) body.landing_dock_sn = req.landing_dock_sn;
    if (req.min_battery_capacity != -1) body.min_battery_capacity = req.min_battery_capacity;
    if (req.wayline_precision_type != -1) body.wayline_precision_type = req.wayline_precision_type;
    if (!req.wayline_segment_indexes.empty()) body.wayline_segment_indexes = req.wayline_segment_indexes;
    if (req.wayline_duration != -1) body.wayline_duration = req.wayline_duration;
    if (!req.uuid.empty()) body.uuid = req.uuid;
    if (!req.tags.empty()) body.tags = req.tags;
    if (req.is_break_point_resume) {
        body.is_break_point_resume = true;
        body.break_point = to_create_flight_task_break_point(req.break_point);
    }
    if (req.signal_type == 1) body.signal_type = req.signal_type;
    if (!req.resumable_status.empty()) body.resumable_status = req.resumable_status;
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("CreateFlightTask: ObjectToJson error %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 创建飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_CREATE, serialize_err, "[create_flight_task]");

        callback(serialize_err, {});
        return;
    }
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("CreateFlightTask: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 创建飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_CREATE, network_err, "[create_flight_task]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("CreateFlightTask: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 创建飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_CREATE, server_err, "[create_flight_task]");

            callback(server_err, {});
            return;
        }
        CreateFlightTaskResponse resp;
        std::string parse_err;
        if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
            LOG_ERROR("CreateFlightTask: JsonToObject error: %s, data: %s, response: %s",
                      parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str());

            auto data_err = make_http_error(http_code, response);

            // 埋点: 创建飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_CREATE, data_err, "[create_flight_task]");

            callback(data_err, {});
            return;
        }
        CreateTaskResp result;
        result.uuid = resp.uuid;
        result.wayline_validity_check_codes = resp.wayline_validity_check_codes;

        // 埋点: 创建飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_CREATE, FlightHubError::success(), "[create_flight_task]");

        callback(FlightHubError::success(), result);
    }, 10000);
}

void get_flight_task_detail(const std::string& task_uuid, const std::function<void(FlightHubError err, const TaskDetail& result)>& callback) {
    if (task_uuid.empty()) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 查询飞行任务详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DETAIL, param_err, "[get_flight_task_detail]");

        callback(param_err, {});
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string api = flight_task_api_with_uuid(kFlightTaskDetailAPI, task_uuid);
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = xg::newGuid().str();
    HttpGetInnerWithStatus(url, headers, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("GetFlightTaskDetail: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DETAIL, network_err, "[get_flight_task_detail]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("GetFlightTaskDetail: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DETAIL, server_err, "[get_flight_task_detail]");

            callback(server_err, {});
            return;
        }
        TaskDetailResponse r;
        std::string parse_err;
        if (!parse_common_response_data(common_response, r, &parse_err, true)) {
            LOG_ERROR("GetFlightTaskDetail: JsonToObject error: %s, data: %s, response: %s",
                      parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str());

            auto data_err = make_http_error(http_code, response);

            // 埋点: 查询飞行任务详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DETAIL, data_err, "[get_flight_task_detail]");

            callback(data_err, {});
            return;
        }
        TaskDetail d;
        d.uuid = r.uuid; d.name = r.name; d.task_type = r.task_type; d.status = r.status; d.sn = r.sn;
        d.landing_dock_sn = r.landing_dock_sn; d.wayline_uuid = r.wayline_uuid;
        d.begin_at = r.begin_at; d.end_at = r.end_at;
        d.out_of_control_action_in_flight = r.out_of_control_action_in_flight; d.rth_altitude = r.rth_altitude; d.rth_mode = r.rth_mode;
        d.min_battery_capacity = r.min_battery_capacity; d.wayline_precision_type = r.wayline_precision_type; d.wayline_segment_indexes = r.wayline_segment_indexes;
        d.current_waypoint_index = r.current_waypoint_index; d.total_waypoints = r.total_waypoints; d.time_zone = r.time_zone; d.device_type = r.device_type;
        d.bid = r.bid;
        d.break_point.index = r.break_point.index;
        d.break_point.state = r.break_point.state;
        d.break_point.progress = r.break_point.progress;
        d.break_point.wayline_id = r.break_point.wayline_id;
        d.break_point.break_reason = r.break_point.break_reason;
        d.break_point.latitude = r.break_point.latitude;
        d.break_point.longitude = r.break_point.longitude;
        d.break_point.height = r.break_point.height;
        d.break_point.attitude_head = r.break_point.attitude_head;
        d.completed_at = r.completed_at;
        d.created_at = r.created_at;
        d.deleted_at = r.deleted_at;
        d.is_break_point_resume = r.is_break_point_resume;
        d.media_upload_status = r.media_upload_status;
        for (const auto& obs : r.obstacles) {
            WaylineObstacle o;
            o.id = obs.id; o.type = obs.type; o.timestamp = obs.timestamp;
            o.latitude = obs.latitude; o.longitude = obs.longitude; o.height = obs.height;
            o.wayline_id = obs.wayline_id; o.waypoint_index = obs.waypoint_index;
            d.obstacles.push_back(o);
        }
        d.run_at = r.run_at;
        d.tags = r.tags;
        d.updated_at = r.updated_at;
        d.wayline_duration = r.wayline_duration;

        // 埋点: 查询飞行任务详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DETAIL, FlightHubError::success(), "[get_flight_task_detail]");

        callback(FlightHubError::success(), d);
    }, 10000);
}

void edit_flight_task(const std::string& task_uuid, const EditTaskReq& req, const std::function<void(FlightHubError err, const CreateTaskResp& result)>& callback) {
    if (task_uuid.empty()) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 编辑飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_EDIT, param_err, "[edit_flight_task]");

        callback(param_err, {});
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string api = flight_task_api_with_uuid(kFlightTaskEditAPI, task_uuid);
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    CreateFlightTaskRequest body;
    body.name = req.name;
    body.sn = req.sn;
    body.wayline_uuid = req.wayline_uuid;
    if (req.task_type != -1) body.task_type = req.task_type;
    if (!req.time_zone.empty()) body.time_zone = req.time_zone;
    if (req.out_of_control_action_in_flight != -1) body.out_of_control_action_in_flight = req.out_of_control_action_in_flight;
    if (req.rth_altitude != -1) body.rth_altitude = req.rth_altitude;
    if (req.rth_mode != -1) body.rth_mode = req.rth_mode;
    if (req.begin_at != -1) body.begin_at = req.begin_at;
    if (req.end_at != -1) body.end_at = req.end_at;
    if (!req.landing_dock_sn.empty()) body.landing_dock_sn = req.landing_dock_sn;
    if (!req.uuid.empty()) body.uuid = req.uuid;
    if (!req.wayline_segment_indexes.empty()) body.wayline_segment_indexes = req.wayline_segment_indexes;
    if (req.wayline_precision_type != -1) body.wayline_precision_type = req.wayline_precision_type;
    if (req.min_battery_capacity != -1) body.min_battery_capacity = req.min_battery_capacity;
    if (req.is_break_point_resume) {
        body.is_break_point_resume = true;
        body.break_point = to_create_flight_task_break_point(req.break_point);
    }
    if (!req.tags.empty()) body.tags = req.tags;
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("EditFlightTask: ObjectToJson error: %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 编辑飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_EDIT, serialize_err, "[edit_flight_task]");

        callback(serialize_err, {});
        return;
    }
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("EditFlightTask: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 编辑飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_EDIT, network_err, "[edit_flight_task]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("EditFlightTask: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 编辑飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_EDIT, server_err, "[edit_flight_task]");

            callback(server_err, {});
            return;
        }
        CreateFlightTaskResponse resp;
        std::string parse_err;
        if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
            LOG_ERROR("EditFlightTask: JsonToObject error: %s, data: %s, response: %s",
                      parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str());
        }
        CreateTaskResp result;
        if (parse_err.empty()) {
            result.uuid = resp.uuid;
            result.wayline_validity_check_codes = resp.wayline_validity_check_codes;
        }
        auto final_err = parse_err.empty() ? FlightHubError::success() : make_http_error(http_code, response);

        // 埋点: 编辑飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_EDIT, final_err, "[edit_flight_task]");

        callback(final_err, result);
    }, 10000);
}

void delete_flight_task(const std::string& task_uuid, const std::function<void(FlightHubError err)>& callback) {
    if (task_uuid.empty()) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 删除飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_DELETE, param_err, "[delete_flight_task]");

        callback(param_err);
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string api = flight_task_api_with_uuid(kFlightTaskDeleteAPI, task_uuid);
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = xg::newGuid().str();
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "", [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("DeleteFlightTask: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 删除飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_DELETE, network_err, "[delete_flight_task]");

            callback(network_err);
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("DeleteFlightTask: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());
        }

        auto final_err = common_response.code == 0 ? FlightHubError::success() : make_http_error(http_code, response);

        // 埋点: 删除飞行任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_DELETE, final_err, "[delete_flight_task]");

        callback(final_err);
    }, 10000);
}

void update_flight_task_status(const std::string& task_uuid, int status, const std::function<void(FlightHubError err)>& callback) {
    if (task_uuid.empty()) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_UPDATE_STATUS, param_err, "[update_flight_task_status]");

        callback(param_err);
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string api = flight_task_api_with_uuid(kFlightTaskStatusAPI, task_uuid);
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    UpdateFlightTaskStatusRequest body;
    body.task_uuid = task_uuid;
    body.status = status;
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("UpdateFlightTaskStatus: ObjectToJson error: %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_UPDATE_STATUS, serialize_err, "[update_flight_task_status]");

        callback(serialize_err);
        return;
    }
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("UpdateFlightTaskStatus: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_UPDATE_STATUS, network_err, "[update_flight_task_status]");

            callback(network_err);
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("UpdateFlightTaskStatus: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());
        }

        auto final_err = common_response.code == 0 ? FlightHubError::success() : make_http_error(http_code, response);

        // 埋点: 更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_UPDATE_STATUS, final_err, "[update_flight_task_status]");

        callback(final_err);
    }, 10000);
}

void batch_update_flight_task_status(const BatchUpdateStatusReq& req, const std::function<void(FlightHubError err)>& callback) {
    if (req.task_uuids.empty()) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 批量更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_BATCH_UPDATE_STATUS, param_err, "[batch_update_flight_task_status]");

        callback(param_err);
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFlightTaskBatchStatusAPI);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    BatchUpdateFlightTaskStatusRequest body;
    body.task_uuids = req.task_uuids;
    body.status = req.status;
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("BatchUpdateFlightTaskStatus: ObjectToJson error: %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 批量更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_BATCH_UPDATE_STATUS, serialize_err, "[batch_update_flight_task_status]");

        callback(serialize_err);
        return;
    }
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("BatchUpdateFlightTaskStatus: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 批量更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_BATCH_UPDATE_STATUS, network_err, "[batch_update_flight_task_status]");

            callback(network_err);
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("BatchUpdateFlightTaskStatus: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());
        }

        auto final_err = common_response.code == 0 ? FlightHubError::success() : make_http_error(http_code, response);

        // 埋点: 批量更新飞行任务状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_BATCH_UPDATE_STATUS, final_err, "[batch_update_flight_task_status]");

        callback(final_err);
    }, 10000);
}

void get_device_recommendation(const DeviceRecommendationReq& req, const std::function<void(FlightHubError err, const DeviceRecommendationResp& result)>& callback) {
    if (req.sn.empty()) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 查询推荐设备; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_RECOMMENDATION, param_err, "[get_device_recommendation]");

        callback(param_err, {});
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFlightTaskDeviceRecommendationAPI);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    DeviceRecommendationRequest body;
    body.sn = req.sn;
    body.device_model_keys = req.device_model_keys;
    body.strategy_id = req.strategy_id;
    body.prj_uuid = req.prj_uuid;
    body.wayline_uuid = req.wayline_uuid;
    body.wayline_height_type = req.wayline_height_type;
    body.fly_to_position = {req.fly_to_position.height, req.fly_to_position.latitude, req.fly_to_position.longitude};
    body.wayline_start_position = {req.wayline_start_position.height, req.wayline_start_position.latitude, req.wayline_start_position.longitude};
    body.wayline_end_position = {req.wayline_end_position.height, req.wayline_end_position.latitude, req.wayline_end_position.longitude};
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("GetDeviceRecommendation: ObjectToJson error: %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 查询推荐设备; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_RECOMMENDATION, serialize_err, "[get_device_recommendation]");

        callback(serialize_err, {});
        return;
    }
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("GetDeviceRecommendation: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 查询推荐设备; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_RECOMMENDATION, network_err, "[get_device_recommendation]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("GetDeviceRecommendation: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 查询推荐设备; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_RECOMMENDATION, server_err, "[get_device_recommendation]");

            callback(server_err, {});
            return;
        }
        DeviceRecommendationResponse device_recommendation_response;
        std::string err;
        if (!parse_common_response_data(common_response, device_recommendation_response, &err, true)) {
            LOG_ERROR("GetDeviceRecommendation: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");

            auto data_err = make_http_error(http_code, response);

            // 埋点: 查询推荐设备; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_RECOMMENDATION, data_err, "[get_device_recommendation]");

            callback(data_err, {});
            return;
        }
        DeviceRecommendationResp result;
        for (const auto& item : device_recommendation_response.devices) {
            RecommendedDevice device;
            device.drone_sn = item.drone_sn;
            device.gateway_sn = item.gateway_sn;
            device.distance = item.distance;
            device.matched = item.matched;
            result.devices.push_back(device);
        }

        // 埋点: 查询推荐设备; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_RECOMMENDATION, FlightHubError::success(), "[get_device_recommendation]");

        callback(FlightHubError::success(), result);
    }, 10000);
}

void get_device_tcas(const std::vector<std::string>& center_sn, const std::function<void(FlightHubError err, const GetTCAInfosResp& result)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kFlightTaskTcasAPI;
    if (!center_sn.empty()) {
        std::ostringstream q;
        for (size_t i = 0; i < center_sn.size(); ++i) q << (i ? "&" : "?") << "center_sn=" << center_sn[i];
        api += q.str();
    }
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = xg::newGuid().str();
    HttpGetInnerWithStatus(url, headers, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("GetDeviceTcas: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 查询设备 TCAS 信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_TCAS, network_err, "[get_device_tcas]");

            callback(network_err, {});
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("GetDeviceTcas: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());

            auto server_err = make_http_error(http_code, response);

            // 埋点: 查询设备 TCAS 信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_TCAS, server_err, "[get_device_tcas]");

            callback(server_err, {});
            return;
        }
        GetTCAInfosDataResponse resp;
        std::string parse_err;
        bool parse_ok = parse_common_response_data(common_response, resp, &parse_err, true);
        GetTCAInfosResp result;
        if (!parse_ok) {
            LOG_ERROR("GetDeviceTcas: JsonToObject error: %s, data: %s, response: %s",
                      parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str());
        }
        if (parse_err.empty()) {
            for (const auto& t : resp.tcas) {
                TCAInfo info;
                info.center_sn = t.center_sn;
                info.data_json = t.data_json;
                result.tcas.push_back(info);
            }
        }

        auto final_err = parse_err.empty() ? FlightHubError::success() : make_http_error(http_code, response);

        // 埋点: 查询设备 TCAS 信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_DEVICE_TCAS, final_err, "[get_device_tcas]");

        callback(final_err, result);
    }, 10000);
}

void update_device_tcas(const std::vector<std::string>& center_sn, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFlightTaskTcasAPI);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    UpdateTcasRequest body;
    body.center_sn = center_sn;
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("UpdateDeviceTcas: ObjectToJson error: %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 更新设备 TCAS 信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_UPDATE_DEVICE_TCAS, serialize_err, "[update_device_tcas]");

        callback(serialize_err);
        return;
    }
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str, [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("UpdateDeviceTcas: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

            auto network_err = make_http_error(http_code, response);

            // 埋点: 更新设备 TCAS 信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_UPDATE_DEVICE_TCAS, network_err, "[update_device_tcas]");

            callback(network_err);
            return;
        }
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("UpdateDeviceTcas: code %d, message: %s, response: %s",
                      common_response.code, common_response.message.c_str(), log_safe(response).c_str());
        }

        auto final_err = common_response.code == 0 ? FlightHubError::success() : make_http_error(http_code, response);

        // 埋点: 更新设备 TCAS 信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_UPDATE_DEVICE_TCAS, final_err, "[update_device_tcas]");

        callback(final_err);
    }, 10000);
}

void start_roaming(const StartRoamingParams& params, const std::function<void(FlightHubError err)>& callback) {
    if (params.drone_sn.empty() || params.flight_id.empty() ||
        params.source_task_type < 1 || params.source_task_type > 2) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 开始漫游; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_START_ROAMING, param_err, "[start_roaming]");

        callback(param_err);
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFlightTaskStartRoamingAPI);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    StartRoamingRequest body;
    body.drone_sn = params.drone_sn;
    body.flight_id = params.flight_id;
    body.source_task_type = params.source_task_type;
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("StartRoaming: ObjectToJson error: %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 开始漫游; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_START_ROAMING, serialize_err, "[start_roaming]");

        callback(serialize_err);
        return;
    }
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("StartRoaming: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

                auto network_err = make_http_error(http_code, response);

                // 埋点: 开始漫游; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_START_ROAMING, network_err, "[start_roaming]");

                callback(network_err);
                return;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("StartRoaming: code %d, message: %s, response: %s",
                          common_response.code, common_response.message.c_str(), log_safe(response).c_str());

                auto server_err = make_http_error(http_code, response);

                // 埋点: 开始漫游; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_START_ROAMING, server_err, "[start_roaming]");

                callback(server_err);
                return;
            }

            // 埋点: 开始漫游; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_START_ROAMING, FlightHubError::success(), "[start_roaming]");

            callback(FlightHubError::success());
        }, 10000);
}

void set_roaming_station_sn(const SetRoamingStationSnParams& params,
                            const std::function<void(FlightHubError err, const SetRoamingStationSnResult& result)>& callback) {
    if (params.drone_sn.empty()) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 设置漫游基站; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_ROAMING_BASE_STATIONS, param_err, "[set_roaming_station_sn]");

        callback(param_err, {});
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFlightTaskRoamingBaseStationsAPI);
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = xg::newGuid().str();
    SetRoamingStationSnRequest body;
    body.drone_sn = params.drone_sn;
    body.station_sns = params.station_sns;
    std::string body_str;
    std::string err;
    aigc::JsonHelper::ObjectToJson(body, body_str, &err);
    if (!err.empty()) {
        LOG_ERROR("SetRoamingStationSn: ObjectToJson error: %s", err.c_str());

        auto serialize_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 设置漫游基站; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_ROAMING_BASE_STATIONS, serialize_err, "[set_roaming_station_sn]");

        callback(serialize_err, {});
        return;
    }
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body_str,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("SetRoamingStationSn: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

                auto network_err = make_http_error(http_code, response);

                // 埋点: 设置漫游基站; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_ROAMING_BASE_STATIONS, network_err, "[set_roaming_station_sn]");

                callback(network_err, {});
                return;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("SetRoamingStationSn: code %d, message: %s, response: %s",
                          common_response.code, common_response.message.c_str(), log_safe(response).c_str());

                auto server_err = make_http_error(http_code, response);

                // 埋点: 设置漫游基站; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_ROAMING_BASE_STATIONS, server_err, "[set_roaming_station_sn]");

                callback(server_err, {});
                return;
            }
            SetRoamingStationSnResponse resp;
            std::string parse_err;
            if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
                LOG_ERROR("SetRoamingStationSn: JsonToObject error: %s, data: %s",
                          parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");

                auto data_err = make_http_error(http_code, response);

                // 埋点: 设置漫游基站; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_ROAMING_BASE_STATIONS, data_err, "[set_roaming_station_sn]");

                callback(data_err, {});
                return;
            }
            SetRoamingStationSnResult result;
            for (const auto& item : resp.results) {
                RoamingStationSnResult r;
                r.station_sn = item.station_sn;
                r.success = item.success;
                r.fail_reason = static_cast<RoamingStationFailReason>(item.fail_reason);
                result.results.push_back(r);
            }

            // 埋点: 设置漫游基站; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_ROAMING_BASE_STATIONS, FlightHubError::success(), "[set_roaming_station_sn]");

            callback(FlightHubError::success(), result);
        }, 10000);
}

void get_in_flight_task_topos(const std::vector<std::string>& drone_sns,
                              const std::function<void(FlightHubError err, const GetInFlightTaskToposResult& result)>& callback) {
    if (drone_sns.empty() || drone_sns.size() > 10) {
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 查询在飞任务拓扑; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_IN_FLIGHT_TASK_TOPOS, param_err, "[get_in_flight_task_topos]");

        callback(param_err, {});
        return;
    }
    auto config = FlightHubSdk::getConfig();
    std::string api = kFlightTaskInFlightTaskToposAPI;
    std::ostringstream q;
    for (size_t i = 0; i < drone_sns.size(); ++i) {
        q << (i ? "&" : "?") << "drone_sns=" << drone_sns[i];
    }
    api += q.str();
    std::string url = generate_url(config.address, config.port, api);
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = xg::newGuid().str();
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("GetInFlightTaskTopos: network error %d, response: %s", static_cast<int>(err_code), log_safe(response).c_str());

                auto network_err = make_http_error(http_code, response);

                // 埋点: 查询在飞任务拓扑; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_IN_FLIGHT_TASK_TOPOS, network_err, "[get_in_flight_task_topos]");

                callback(network_err, {});
                return;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GetInFlightTaskTopos: code %d, message: %s, response: %s",
                          common_response.code, common_response.message.c_str(), log_safe(response).c_str());

                auto server_err = make_http_error(http_code, response);

                // 埋点: 查询在飞任务拓扑; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_IN_FLIGHT_TASK_TOPOS, server_err, "[get_in_flight_task_topos]");

                callback(server_err, {});
                return;
            }
            GetInFlightTaskToposResponse resp;
            std::string parse_err;
            if (!parse_common_response_data(common_response, resp, &parse_err, true)) {
                LOG_ERROR("GetInFlightTaskTopos: JsonToObject error: %s, data: %s",
                          parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");

                auto data_err = make_http_error(http_code, response);

                // 埋点: 查询在飞任务拓扑; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_IN_FLIGHT_TASK_TOPOS, data_err, "[get_in_flight_task_topos]");

                callback(data_err, {});
                return;
            }
            GetInFlightTaskToposResult result;
            for (const auto& item : resp.list) {
                InFlightTaskTopoItem topo;
                topo.drone_sn = item.drone_sn;
                topo.flight_id = item.flight_id;
                topo.takeoff_sn = item.takeoff_sn;
                result.list.push_back(topo);
            }

            // 埋点: 查询在飞任务拓扑; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_GET_IN_FLIGHT_TASK_TOPOS, FlightHubError::success(), "[get_in_flight_task_topos]");

            callback(FlightHubError::success(), result);
        }, 10000);
}

void open_data_channel(OpenDataChannelRequest request, const std::string& sn_tag, const std::function<void(const OpenDataChannelResponse&, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    // 新接口路径 /paas/api/v1/rt-channels/{sn}，sn 走 URL 路径参数；使用 gateway_sn 替换
    // （传统机场场景 gateway_sn = dock_sn；网联机型 gateway_sn = drone_sn，由 DroneImpl::getGatewaySn() 决定）
    std::string api_with_sn = std::string(kOpenDataChannelAPI);
    auto sn_placeholder = api_with_sn.find("{sn}");
    if (sn_placeholder != std::string::npos) {
        api_with_sn.replace(sn_placeholder, 4, request.gateway_sn);
    }
    std::string url = generate_url(config.address, config.port, api_with_sn);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("OpenDataChannel [%s]: ObjectToJson error: %s", sn_tag.c_str(), err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("OpenDataChannel [%s]: url %s, uuid %s, body %s", sn_tag.c_str(), url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, sn_tag, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("OpenDataChannel [%s]: network error: %d, uuid: %s, response: %s",
                          sn_tag.c_str(), static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            LOG_DEBUG("OpenDataChannel [%s]: HTTP response uuid: %s, raw: %s", sn_tag.c_str(), uuid_str.c_str(), log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("OpenDataChannel [%s]: code: %d, message: %s, uuid: %s", sn_tag.c_str(), common_response.code, common_response.message.c_str(), uuid_str.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            OpenDataChannelResponse open_data_channel_response;
            std::string err;
            if (!parse_common_response_data(common_response, open_data_channel_response, &err, true)) {
                LOG_ERROR("OpenDataChannel [%s]: JsonToObject error: %s, %s, uuid: %s", sn_tag.c_str(), err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", uuid_str.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            callback(open_data_channel_response, FlightHubError::success());
        });
}

void fly_to_point(FlyToPointRequest request, const std::function<void(const FlyToPointResponse& response, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFlyToPointAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("FlyToPoint: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("FlyToPoint:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("FlyToPoint: code: %d, message: %s, \n http response: %s",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                callback({}, make_http_error(http_code, response));
                return;
            }
            FlyToPointResponse fly_to_point_response;
            std::string err;
            if (!parse_common_response_data(common_response, fly_to_point_response, &err, true)) {
                LOG_ERROR("FlyToPoint: JsonToObject error: %s, %s, \n http response: %s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                callback({}, make_http_error(http_code, response));
                return;
            }
            callback(fly_to_point_response, FlightHubError::success());
        });
}

void poi_enter(POIEnterRequest request, const std::function<void(const POITaskResponse& response, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kPOIEnterAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("POIEnter: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("POIEnter: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("POIEnter response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("POIEnter: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            POITaskResponse poi_enter_response;
            std::string err;
            if (!parse_common_response_data(common_response, poi_enter_response, &err, true)) {
                LOG_ERROR("POIEnter: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                callback({}, make_http_error(http_code, response));
                return;
            }
            callback(poi_enter_response, FlightHubError::success());
        });
}

void acquire_ctrl_authority(AcquireCtrlAuthorityRequest request, const std::function<void(const AcquireCtrlAuthorityResponse& response, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kControlAuthorityAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("AcquireCtrlAuthority: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("AcquireCtrlAuthority:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("AcquireCtrlAuthority: code: %d, message: %s, http response: %s",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                callback({}, make_http_error(http_code, response));
                return;
            }
            AcquireCtrlAuthorityResponse acquire_ctrl_authority_response;
            std::string err;
            if (!parse_common_response_data(common_response, acquire_ctrl_authority_response, &err, true)) {
                LOG_ERROR("AcquireCtrlAuthority: JsonToObject error: %s, %s, http response: %s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                callback({}, make_http_error(http_code, response));
                return;
            }
            callback(acquire_ctrl_authority_response, FlightHubError::success());
        });
}

void release_ctrl_authority(ReleaseCtrlAuthorityRequest request, const std::function<void(const ReleaseCtrlAuthorityResponse& response, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kControlAuthorityAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("ReleaseCtrlAuthority: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("ReleaseCtrlAuthority:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("ReleaseCtrlAuthority: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            ReleaseCtrlAuthorityResponse release_ctrl_authority_response;
            std::string err;
            if (!parse_common_response_data(common_response, release_ctrl_authority_response, &err, true)) {
                LOG_ERROR("ReleaseCtrlAuthority: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                callback({}, make_http_error(http_code, response));
                return;
            }
            callback(release_ctrl_authority_response, FlightHubError::success());
        });
}

/// 带 FlightHubError 的非实时命令模板
template<typename RequestType, typename ResponseType>
void execute_non_realtime_cmd(
    RequestType request,
    const std::string& operation_name,
    const std::string& sn,
    const std::function<void(const ResponseType& response, FlightHubError err)>& callback) {

    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kNonRealtimeCmdAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("execute_non_realtime_cmd(FlightHubError) [%s] %s: ObjectToJson error: %s", sn.c_str(), operation_name.c_str(), err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("execute_non_realtime_cmd(FlightHubError) [%s] %s: url %s, uuid %s, body %s",
             sn.c_str(), operation_name.c_str(), url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, operation_name, uuid_str = uuid.str(), sn](
            drc_sdk::common::NetworkError net_err, int http_code,
            const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
            if (net_err != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("execute_non_realtime_cmd(FlightHubError) [%s] %s: network error: %d, http_code: %d, uuid: %s",
                          sn.c_str(), operation_name.c_str(), static_cast<int>(net_err), http_code, uuid_str.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }
            ResponseType cmd_response;
            std::string aigc_json_err;
            aigc::JsonHelper::JsonToObject(cmd_response, response, {}, &aigc_json_err);
            if (!aigc_json_err.empty()) {
                LOG_ERROR("execute_non_realtime_cmd(FlightHubError) [%s] %s: JsonToObject error: %s, %s, uuid: %s",
                          sn.c_str(), operation_name.c_str(), aigc_json_err.c_str(), log_safe(response).c_str(), uuid_str.c_str());
                callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
                return;
            }
            if (cmd_response.code != 0) {
                LOG_ERROR("execute_non_realtime_cmd(FlightHubError) [%s] %s: server error: %d, %s, uuid: %s",
                          sn.c_str(), operation_name.c_str(), cmd_response.code, cmd_response.message.c_str(), uuid_str.c_str());
                callback(cmd_response, FlightHubError::from_server(cmd_response.code, cmd_response.message));
                return;
            }
            callback(cmd_response, FlightHubError::success());
        });
}

void flight_task_pause(const FlightTaskPauseRequest& request,
    const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback) {
    execute_non_realtime_cmd<FlightTaskPauseRequest, NonRealtimeCmdResponse>(request, "flight_task_pause", request.sn, callback);
}

void flight_task_resume(const FlightTaskResumeRequest& request,
    const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback) {
    execute_non_realtime_cmd<FlightTaskResumeRequest, NonRealtimeCmdResponse>(request, "flight_task_resume", request.sn, callback);
}

void return_home(const ReturnHomeRequest& request,
    const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback) {
    execute_non_realtime_cmd<ReturnHomeRequest, NonRealtimeCmdResponse>(request, "return_home", request.sn, callback);
}

void return_home_cancel(const ReturnHomeCancelRequest& request,
    const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback) {
    execute_non_realtime_cmd<ReturnHomeCancelRequest, NonRealtimeCmdResponse>(request, "return_home_cancel", request.sn, callback);
}

void look_at_target(const LookAtTargetRequest& request,
    const std::function<void(const NonRealtimeCmdResponse& response, FlightHubError err)>& callback) {
    execute_non_realtime_cmd<LookAtTargetRequest, NonRealtimeCmdResponse>(request, "look_at_target", request.sn, callback);
}

void get_psdk_speaker_resource_list(const std::function<void(const PsdkSpeakerResourceListResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kPsdkSpeakerResourceListAPI);

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("GetPsdkSpeakerResourceList: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GetPsdkSpeakerResourceList: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            PsdkSpeakerResourceListResponse psdk_speaker_resource_list_response;
            std::string err;
            if (!parse_common_response_data(common_response, psdk_speaker_resource_list_response, &err, true)) {
                LOG_ERROR("GetPsdkSpeakerResourceList: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(psdk_speaker_resource_list_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void create_psdk_speaker_resource(CreateSpeakerResourceRequest request, const std::function<void(const CreateSpeakerResourceResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kCreatePsdkSpeakerResourceAPI);

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("CreatePsdkSpeakerResource: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("CreatePsdkSpeakerResource: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("CreatePsdkSpeakerResource: response %s", log_safe(response).c_str());
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("CreatePsdkSpeakerResource: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            CreateSpeakerResourceResponse create_speaker_resource_response;
            std::string err;
            if (!parse_common_response_data(common_response, create_speaker_resource_response, &err, true)) {
                LOG_ERROR("CreatePsdkSpeakerResource: JsonToObject error: %s, %s", 
                          err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(create_speaker_resource_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void edit_psdk_speaker_resource(const std::string& resource_id, EditSpeakerResourceRequest request, const std::function<void(const EditSpeakerResourceResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kEditPsdkSpeakerResourceAPI);
    api.replace(api.find("{id}"), 4, resource_id);
    
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("EditPsdkSpeakerResource: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("EditPsdkSpeakerResource: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("EditPsdkSpeakerResource: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            EditSpeakerResourceResponse edit_speaker_resource_response;
            std::string err;
            if (!parse_common_response_data(common_response, edit_speaker_resource_response, &err, true)) {
                LOG_ERROR("EditPsdkSpeakerResource: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(edit_speaker_resource_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void delete_psdk_speaker_resource(int resource_id, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kDeletePsdkSpeakerResourceAPI);
    api.replace(api.find("{id}"), 4, std::to_string(resource_id));
    
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("DeletePsdkSpeakerResource: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "", 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("DeletePsdkSpeakerResource: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (false);
        callback(make_http_error(http_code, response));
    }, 15000);
}

void get_flash_led_config_list(const std::function<void(const FlashLedConfigListHttpResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kFlashLedConfigListAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);

    LOG_INFO("GetFlashLedConfigList: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        FlashLedConfigListHttpResponse out;
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("GetFlashLedConfigList: network error: %d", static_cast<int>(err_code));
                break;
            }
            // 网关未挂路由时常返回纯文本（如 "not find path,please add it."）
            if (!response.empty() && response.front() != '{' && response.front() != '[') {
                LOG_ERROR("GetFlashLedConfigList: non-json response http=%d body=%s", http_code, log_safe(response).c_str());
                break;
            }
            // data 是数组，不能走 CommonResponse（optional<string> data）
            FlashLedConfigListRawResponse raw;
            std::string err;
            if (!aigc::JsonHelper::JsonToObject(raw, response, {}, &err)) {
                LOG_ERROR("GetFlashLedConfigList: JsonToObject error: %s, response: %s", err.c_str(), log_safe(response).c_str());
                break;
            }
            if (raw.code != 0) {
                LOG_ERROR("GetFlashLedConfigList: code: %d, message: %s", raw.code, raw.message.c_str());
                break;
            }
            out.configs = std::move(raw.data);
            callback(out, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void create_flash_led_config(CreateFlashLedConfigRequest request, const std::function<void(const CreateFlashLedConfigResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kCreateFlashLedConfigAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("CreateFlashLedConfig: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("CreateFlashLedConfig: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        CreateFlashLedConfigResponse out;
        dispatch_common_json_response("CreateFlashLedConfig", err_code, http_code, response, out, true, callback);
    }, 15000);
}

void update_flash_led_config(const std::string& config_id, UpdateFlashLedConfigRequest request, const std::function<void(const UpdateFlashLedConfigResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kUpdateFlashLedConfigAPI);
    api.replace(api.find("{id}"), 4, config_id);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("UpdateFlashLedConfig: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("UpdateFlashLedConfig: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        UpdateFlashLedConfigResponse out;
        dispatch_common_json_response("UpdateFlashLedConfig", err_code, http_code, response, out, true, callback);
    }, 15000);
}

void delete_flash_led_config(const std::string& config_id, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kDeleteFlashLedConfigAPI);
    api.replace(api.find("{id}"), 4, config_id);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);

    LOG_INFO("DeleteFlashLedConfig: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("DeleteFlashLedConfig: network error: %d", static_cast<int>(err_code));
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("DeleteFlashLedConfig: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (false);
        callback(make_http_error(http_code, response));
    }, 15000);
}

void get_sts(const std::function<void(const STSInfo& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kGetSTSAPI);

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("GetSTS: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GetSTS: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            STSInfoResponse get_sts_response;
            std::string err;
            if (!parse_common_response_data(common_response, get_sts_response, &err, true)) {
                LOG_ERROR("GetSTS: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }

            STSInfo sts_info{};
            sts_info.object_key_prefix = get_sts_response.object_key_prefix;
            sts_info.bucket = get_sts_response.bucket;
            sts_info.endpoint = get_sts_response.endpoint;
            sts_info.provider = get_sts_response.provider;
            sts_info.region = get_sts_response.region;
            sts_info.credentials.access_key_id = get_sts_response.credentials.access_key_id;
            sts_info.credentials.access_key_secret = get_sts_response.credentials.access_key_secret;
            sts_info.credentials.expire = get_sts_response.credentials.expire;
            sts_info.credentials.security_token = get_sts_response.credentials.security_token;
            sts_info.credentials.platform = get_sts_response.credentials.platform;

            callback(sts_info, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void get_psdk_widget_resources(PsdkWidgetResourceRequest request, const std::function<void(const PsdkWidgetResourceListResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kGetPsdkWidgetResourceAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("GetPsdkWidgetResources: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("GetPsdkWidgetResources: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GetPsdkWidgetResources: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            PsdkWidgetResourceListResponse psdk_widget_resource_list_response;
            std::string err;
            if (!parse_common_response_data(common_response, psdk_widget_resource_list_response, &err, true)) {
                LOG_ERROR("GetPsdkWidgetResources: JsonToObject error: %s, %s", 
                          err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(psdk_widget_resource_list_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void drone_takeoff(DroneTakeoffRequest request, const std::function<void(const DroneTakeoffResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDroneTakeoffAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("DroneTakeoff: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("DroneTakeoff: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("DroneTakeoff: code: %d, message: %s, response:\n%s",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }
            DroneTakeoffResponse drone_takeoff_response;
            std::string err;
            if (!parse_common_response_data(common_response, drone_takeoff_response, &err, true)) {
                LOG_ERROR("DroneTakeoff: JsonToObject error: %s, %s, response:\n%s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }
            callback(drone_takeoff_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void generate_try_flight_wayline(const std::string& project_id, GenerateTryFlightWaylineRequest request, const std::function<void(const GenerateTryFlightWaylineResponseData& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kGenerateTryFlightWaylineAPI).replace(kGenerateTryFlightWaylineAPI.find("{project_id}"), 12, project_id);

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("GenerateTryFlightWayline: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("GenerateTryFlightWayline: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GenerateTryFlightWayline: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            GenerateTryFlightWaylineResponseData wayline_response;
            std::string parse_err;
            if (!parse_common_response_data(common_response, wayline_response, &parse_err, true)) {
                LOG_ERROR("GenerateTryFlightWayline: JsonToObject error: %s, %s", parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(wayline_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 10000);
}

void create_test_flight_task(const std::string& project_id, CreateTestFlightTaskRequest request, const std::function<void(const CreateTestFlightTaskResponseData& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kCreateTestFlightTaskAPI);
    size_t pos = api.find("{project_id}");
    if (pos != std::string::npos) {
        api.replace(pos, 12, project_id);
    }
    LOG_INFO("CreateTestFlightTask: api after replace: %s", api.c_str());

    std::string url = generate_url(config.address, config.port, api);
    LOG_INFO("CreateTestFlightTask: final url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("CreateTestFlightTask: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("CreateTestFlightTask: url %s, uuid %s, body length: %zu, body: %s", url.c_str(), uuid.str().c_str(), body.length(), log_safe(body).c_str());
    LOG_INFO("CreateTestFlightTask: headers: x-auth-token=%s, x-request-id=%s, content-type=%s",
             headers.find("x-auth-token") != headers.end() ? "set" : "NOT_SET",
             headers.find("x-request-id") != headers.end() ? headers.at("x-request-id").c_str() : "NOT_SET",
             headers.find("content-type") != headers.end() ? headers.at("content-type").c_str() : "NOT_SET");
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_INFO("CreateTestFlightTask: response received, err_code: %d, response length: %zu", (int)err_code, response.length());
        LOG_DEBUG("CreateTestFlightTask: response body: %s", response.empty() ? "(empty)" : log_safe(response).c_str());
        if (!resp_headers.empty()) {
            LOG_INFO("CreateTestFlightTask: response headers count: %zu", resp_headers.size());
            for (const auto& header : resp_headers) {
                LOG_DEBUG("CreateTestFlightTask: response header: %s = %s", header.first.c_str(), log_safe(header.second).c_str());
            }
        }
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("CreateTestFlightTask: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            CreateTestFlightTaskResponseData task_response;
            std::string parse_err;
            if (!parse_common_response_data(common_response, task_response, &parse_err, true)) {
                LOG_ERROR("CreateTestFlightTask: JsonToObject error: %s, %s", parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(task_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}


/// 飞机星历下载;
void drone_download_ephemeris(DroneDownloadEphemerisRequest request, const std::function<void(FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kDownloadEphemerisAPI);

    std::string url = generate_url(config.address, config.port, api);
    LOG_INFO("drone_download_ephemeris(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("drone_download_ephemeris(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }


    LOG_DEBUG("drone_download_ephemeris(): { url:%s, x-request-id(uuid):%s, body length: %zu, body: %s", url.c_str(), uuid.str().c_str(), body.length(), log_safe(body).c_str());

    // Send HTTP request;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("drone_download_ephemeris(): response received, { err_code: %d, response length: %zu, response: %s }",
            (int)err_code, response.length(), response.empty() ? "(empty)" : log_safe(response).c_str()
        );

        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("drone_download_ephemeris(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
                break;
            }

            LOG_INFO("drone_download_ephemeris(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
            callback(FlightHubError::success());

            // Must return here, otherwise the callback will be called twice.
            return;
        } while (0);

        // Indicate failure;
        callback(make_http_error(http_code, response));
    }, 15000);
}


/// drone_in_out_wayline_detect  (开始/暂停)进离场任务
void drone_in_out_wayline_detect(DroneInOutWaylineDetectRequest request, const std::function<void(FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kDetectTaskAPI);

    std::string url = generate_url(config.address, config.port, api);
    LOG_INFO("drone_in_out_wayline_detect(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("drone_in_out_wayline_detect(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }


    LOG_DEBUG("drone_in_out_wayline_detect(): { url:%s, x-request-id(uuid):%s, body length: %zu, body: %s", url.c_str(), uuid.str().c_str(), body.length(), log_safe(body).c_str());

    // Send HTTP request;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("drone_in_out_wayline_detect(): response received, { err_code: %d, response length: %zu, response: %s }",
            (int)err_code, response.length(), response.empty() ? "(empty)" : log_safe(response).c_str()
        );

        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("drone_in_out_wayline_detect(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
                break;
            }

            LOG_INFO("drone_in_out_wayline_detect(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
            callback(FlightHubError::success());

            // Must return here, otherwise the callback will be called twice.
            return;
        } while (0);

        // Indicate failure;
        callback(make_http_error(http_code, response));
    }, 15000);
}


/// drone_in_out_wayline_flight_setting  进离场航线飞行设置;
void drone_in_out_wayline_flight_setting(DroneInOutWaylineFlightSettingRequest request, const std::function<void(FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kFlightSettingAPI);

    std::string url = generate_url(config.address, config.port, api);
    LOG_INFO("drone_in_out_wayline_flight_setting(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("drone_in_out_wayline_flight_setting(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }


    LOG_DEBUG("drone_in_out_wayline_flight_setting(): { url:%s, x-request-id(uuid):%s, body length: %zu, body: %s", url.c_str(), uuid.str().c_str(), body.length(), log_safe(body).c_str());

    // Send HTTP request;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("drone_in_out_wayline_flight_setting(): response received, { err_code: %d, response length: %zu, response: %s }",
            (int)err_code, response.length(), response.empty() ? "(empty)" : log_safe(response).c_str()
        );

        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("drone_in_out_wayline_flight_setting(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
                break;
            }

            LOG_INFO("drone_in_out_wayline_flight_setting(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
            callback(FlightHubError::success());

            // Must return here, otherwise the callback will be called twice.
            return;
        } while (0);

        // Indicate failure;
        callback(make_http_error(http_code, response));
    }, 15000);
}

/// get_scout_flight_setting  获取进离场航线飞行设置 (GET);
void get_scout_flight_setting(const std::string& sn, const std::function<void(const InOutWaylineFlightSettingParams& params, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kFlightSettingAPI) + "?sn=" + sn;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            InOutWaylineFlightSettingParams params{};
            auto error_code = FlightHubError::success();
            do {
                if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                    LOG_ERROR("get_scout_flight_setting(): Network error: %d", (int)err_code);
                    error_code = make_http_error(http_code, response);
                    break;
                }
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_scout_flight_setting(): Response error code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
                
                // 当 code=0 时，即使 data 为 null 也是成功的
                LOG_INFO("get_scout_flight_setting(): Success, code: %d, message: %s", common_response.code, common_response.message.c_str());
                
                DroneInOutWaylineFlightSettingRequest tmp;
                std::string err;
                if (!parse_common_response_data(common_response, tmp, &err, false)) {
                    LOG_ERROR("get_scout_flight_setting(): JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
                params.min_return_home_height = tmp.min_return_home_height;
                params.lost_contact_action = tmp.lost_contact_action;
                params.max_height_limit = tmp.max_height_limit;
                params.max_distance_limit = tmp.max_distance_limit;
            } while (0);
            if (callback) {
                callback(params, error_code);
            }
        }
    );
}

/// set_live_quality 直播画质设置;
void set_live_quality(LiveQualitySettingRequest request, const std::function<void(FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kLiveSetQualityAPI);

    std::string url = generate_url(config.address, config.port, api);
    LOG_INFO("set_live_quality(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("set_live_quality(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }


    LOG_DEBUG("set_live_quality(): { url:%s, x-request-id(uuid):%s, body length: %zu, body: %s", url.c_str(), uuid.str().c_str(), body.length(), log_safe(body).c_str());

    // Send HTTP request;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("set_live_quality(): response received, { err_code: %d, response length: %zu, response: %s }",
            (int)err_code, response.length(), response.empty() ? "(empty)" : log_safe(response).c_str()
        );

        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("set_live_quality(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
                break;
            }

            LOG_INFO("set_live_quality(): { code: %d, message: %s }", common_response.code, common_response.message.c_str());
            callback(FlightHubError::success());

            // Must return here, otherwise the callback will be called twice.
            return;
        } while (0);

        // Indicate failure;
        callback(make_http_error(http_code, response));
    }, 15000);
}


void poi_exit(POIExitRequest request, const std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kPOIExitAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("POIExit: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("POIExit: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("POIExit response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("POIExit: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            callback(FlightHubError::success());
        });
}

void poi_speed_update(POISpeedUpdateRequest request, const std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kPOISpeedUpdateAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("POISpeedUpdate: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("POISpeedUpdate: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("POISpeedUpdate response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("POISpeedUpdate: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            callback(FlightHubError::success());
        });
}

void get_poi_task(GetPOITaskRequest request, const std::function<void(const POITaskResponse& response, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kGetPOITaskAPI);

    url += "?device_sn=" + request.device_sn;

    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("GetPOITask: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("GetPOITask response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);

            //  检查 common_response.code 是否为 0;
            if (common_response.code != 0) {
                LOG_ERROR("GetPOITask: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }

            // 默认构造返回值;
            POITaskResponse poi_task_response{};

            std::string err;
            if (!parse_common_response_data(common_response, poi_task_response, &err, false)) {
                LOG_ERROR("GetPOITask: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                callback({}, make_http_error(http_code, response));
                return;
            }

            // callback 返回;
            callback(poi_task_response, FlightHubError::success());
        });
}

void get_media_file_list(const std::string& project_id, GetMediaFileListRequest request, const std::function<void(const MediaFileListResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kMediaFileListAPI).replace(kMediaFileListAPI.find("{project_id}"), 12, project_id);
    std::string query = "parent_id=" + std::to_string(request.parent_id) + 
                        "&page=" + std::to_string(request.page) + 
                        "&size=" + std::to_string(request.size) +
                        "&get_sub_file=" + (request.get_sub_file ? "true" : "false");
    if (!request.sort_by.empty()) {
        query += "&sort_by=" + request.sort_by;
    }
    std::string url = generate_url(config.address, config.port, api + "?" + query);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("GetMediaFileList: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GetMediaFileList: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            MediaFileListResponse media_file_list_response;
            std::string err;
            if (!parse_common_response_data(common_response, media_file_list_response, &err, true)) {
                LOG_ERROR("GetMediaFileList: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(media_file_list_response, FlightHubError::success());
            return;
        } while (false);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void set_dock_supplement_light_state(const std::string& device_sn, bool state, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = state ? kDockSupplementLightOpenAPI : kDockSupplementLightCloseAPI;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    DockSupplementLightRequest request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("SetDockSupplementLightState: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("SetDockSupplementLightState: url %s, uuid %s, body %s, state %s", 
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str(), state ? "open" : "close");
    
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, state](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("SetDockSupplementLightState response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("SetDockSupplementLightState: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            callback(FlightHubError::success());
        });
}

void set_dock_charge_state(const std::string& device_sn, bool state, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = state ? kDockChargeOpenAPI : kDockChargeCloseAPI;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    DockSupplementLightRequest request;  // 复用这个请求结构
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("SetDockChargeState: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("SetDockChargeState: url %s, uuid %s, body %s, state %s", 
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str(), state ? "open" : "close");
    
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, state](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("SetDockChargeState response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("SetDockChargeState: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            callback(FlightHubError::success());
        });
}

void set_dock_cover_state(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api;
    const int open = static_cast<int>(DockCoverMode::DockCoverMode_OPEN);
    const int close = static_cast<int>(DockCoverMode::DockCoverMode_CLOSE);
    const int force_close = static_cast<int>(DockCoverMode::DockCoverMode_FORCE_CLOSE);
    if (mode == open) {
        api = kDockCoverOpenAPI;
    } else if (mode == close || mode == force_close) {
        api = kDockCoverCloseAPI;  // 关闭和强制关闭都使用同一个接口
    } else {
        LOG_ERROR("SetDockCoverState: Invalid mode: %d", mode);
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM));
        return;
    }
    
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    
    if (mode == open) {
        // 开启舱盖只需要 device_sn
        DockSupplementLightRequest request;  // 复用简单请求结构
        request.device_sn = device_sn;
        aigc::JsonHelper::ObjectToJson(request, body, &err);
    } else {
        // 关闭舱盖需要 device_sn 和 mode
        DockCoverRequest request;
        request.device_sn = device_sn;
        request.data.mode = mode;
        aigc::JsonHelper::ObjectToJson(request, body, &err);
    }
    if (!err.empty()) {
        LOG_ERROR("SetDockCoverState: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("SetDockCoverState: url %s, uuid %s, body %s, mode %d", 
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str(), static_cast<int>(mode));
    
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, mode](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("SetDockCoverState response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("SetDockCoverState: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            callback(FlightHubError::success());
        });
}

void set_dock_air_conditioner_mode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDockAirConditionerAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    DockAirConditionerRequest request;
    request.device_sn = device_sn;
    request.data.mode = mode;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("SetDockAirConditionerMode: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("SetDockAirConditionerMode: url %s, uuid %s, body %s, mode %d", 
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str(), mode);
    
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, mode](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("SetDockAirConditionerMode response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("SetDockAirConditionerMode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            callback(FlightHubError::success());
        });
}

void post_open_debug_mode(const std::string& device_sn, const std::function<void(const OpenDebugModeResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kOpenDebugModeAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_open_debug_mode: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_open_debug_mode:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_open_debug_mode] http response: %s", log_safe(response).c_str());
        OpenDebugModeResponse open_debug_mode_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_open_debug_mode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                open_debug_mode_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, open_debug_mode_response, &err, false)) {
                LOG_ERROR("post_open_debug_mode: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                open_debug_mode_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(open_debug_mode_response, error_code);
        }
    });
}

void post_set_distance_limit(const std::string& device_sn, int distance_limit_state, int distance_limit_value, const std::function<void(const SetDistanceLimitResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSetDistanceLimitAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SetDistanceLimitRequest request;
    request.device_sn = device_sn;
    request.data.distance_limit_state = distance_limit_state;
    request.data.distance_limit_value = distance_limit_value;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_set_distance_limit: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_set_distance_limit: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_set_distance_limit] http response: %s", log_safe(response).c_str());
        SetDistanceLimitResponse set_distance_limit_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_set_distance_limit: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            set_distance_limit_response.status = common_response.message;
        } while (false);
        if (callback) {
            callback(set_distance_limit_response, error_code);
        }
    });
}

void post_set_height_limit(const std::string& device_sn, int height_limit, const std::function<void(const SetHeightLimitResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSetHeightLimitAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    SetHeightLimitRequest request;
    request.device_sn = device_sn;
    request.data.height_limit = height_limit;
    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_set_height_limit: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_set_height_limit: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_set_height_limit] http response: %s", log_safe(response).c_str());
        SetHeightLimitResponse set_height_limit_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_set_height_limit: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            set_height_limit_response.status = common_response.message;
        } while (false);
        if (callback) {
            callback(set_height_limit_response, error_code);
        }
    });
}

void post_set_obstacle_avoidance(const std::string& device_sn, int obstacle_avoidance_state, const std::function<void(const SetObstacleAvoidanceResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSetObstacleAvoidanceAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    SetObstacleAvoidanceRequest request;
    request.device_sn = device_sn;
    request.data.obstacle_avoidance_state = obstacle_avoidance_state;
    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_set_obstacle_avoidance: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_set_obstacle_avoidance: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_set_obstacle_avoidance] http response: %s", log_safe(response).c_str());
        SetObstacleAvoidanceResponse set_obstacle_avoidance_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_set_obstacle_avoidance: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            set_obstacle_avoidance_response.status = common_response.message;
        } while (false);
        if (callback) {
            callback(set_obstacle_avoidance_response, error_code);
        }
    });
}

void post_set_remaining_power_for_rth(const std::string& device_sn, int remaining_power_for_rth, const std::function<void(const SetRemainingPowerForRthResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSetRemainingPowerForRthAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    SetRemainingPowerForRthRequest request;
    request.device_sn = device_sn;
    request.data.remaining_power_for_rth = remaining_power_for_rth;
    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_set_remaining_power_for_rth: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_set_remaining_power_for_rth: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_set_remaining_power_for_rth] http response: %s", log_safe(response).c_str());
        SetRemainingPowerForRthResponse set_remaining_power_for_rth_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_set_remaining_power_for_rth: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            set_remaining_power_for_rth_response.status = common_response.message;
        } while (false);
        if (callback) {
            callback(set_remaining_power_for_rth_response, error_code);
        }
    });
}

void post_set_night_lights_state(const std::string& device_sn, int night_lights_state, const std::function<void(const SetNightLightsStateResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSetNightLightsStateAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    SetNightLightsStateRequest request;
    request.device_sn = device_sn;
    request.data.night_lights_state = night_lights_state;
    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_set_night_lights_state: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_set_night_lights_state: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_set_night_lights_state] http response: %s", log_safe(response).c_str());
        SetNightLightsStateResponse set_night_lights_state_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_set_night_lights_state: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            set_night_lights_state_response.status = common_response.message;
        } while (false);
        if (callback) {
            callback(set_night_lights_state_response, error_code);
        }
    });
}

void post_switch_alarm_state(const std::string& device_sn, int alarm_state, const std::function<void(const AlarmStateSwitchResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAlarmStateSwitchAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SwitchAlarmLightStateRequest request;
    request.device_sn = device_sn;
    request.data.alarm_state = alarm_state;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_switch_alarm_state: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_switch_alarm_state: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_switch_alarm_state] http response: %s", log_safe(response).c_str());
        AlarmStateSwitchResponse alarm_state_switch_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_switch_alarm_state: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                alarm_state_switch_response.status = common_response.message;
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, alarm_state_switch_response, &err, false)) {
                LOG_ERROR("post_switch_alarm_state: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                alarm_state_switch_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(alarm_state_switch_response, error_code);
        }
    });
}

void post_esim_operator_switch(const std::string& device_sn, const std::string& imei, int operator_type, const std::string& device_type, const std::function<void(const EsimOperatorSwitchResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kEsimOperatorSwitchAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    EsimOperatorSwitchRequest request;
    request.device_sn = device_sn;
    request.data.imei = imei;
    request.data.operator_ = operator_type;
    request.data.device_type = device_type;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_esim_operator_switch: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_esim_operator_switch: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_esim_operator_switch] http response: %s", log_safe(response).c_str());
        EsimOperatorSwitchResponse esim_operator_switch_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_esim_operator_switch: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                esim_operator_switch_response.status = common_response.message;
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, esim_operator_switch_response, &err, false)) {
                LOG_ERROR("post_esim_operator_switch: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                esim_operator_switch_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(esim_operator_switch_response, error_code);
        }
    });
}

void post_sim_slot_switch(const std::string& device_sn, const std::string& imei, int sim_slot, const std::string& device_type, const std::function<void(const SimSlotSwitchResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSimSlotSwitchAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SimSlotSwitchRequest request;
    request.device_sn = device_sn;
    request.data.imei = imei;
    request.data.sim_slot = sim_slot;
    request.data.device_type = device_type;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_sim_slot_switch: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_sim_slot_switch: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_sim_slot_switch] http response: %s", log_safe(response).c_str());
        SimSlotSwitchResponse sim_slot_switch_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_sim_slot_switch: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                sim_slot_switch_response.status = common_response.message;
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, sim_slot_switch_response, &err, false)) {
                LOG_ERROR("post_sim_slot_switch: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                sim_slot_switch_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(sim_slot_switch_response, error_code);
        }
    });
}

void post_cloud_environment_switch(CloudEnvironmentSwitchRequest& request, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCloudEnvironmentSwitchAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_cloud_environment_switch: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_cloud_environment_switch: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_cloud_environment_switch: response %s", log_safe(response).c_str());
        auto error_code = FlightHubError::success();
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("post_cloud_environment_switch: network error: %d", (int)err_code);
                error_code = make_http_error(http_code, response);
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_cloud_environment_switch: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(error_code);
        }
    });
}

void post_cloud_environment_switch_callback(CloudEnvironmentSwitchCallbackRequest& request, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCloudEnvironmentSwitchCallbackAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_cloud_environment_switch_callback: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_cloud_environment_switch_callback: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_cloud_environment_switch_callback: response %s", log_safe(response).c_str());
        auto error_code = FlightHubError::success();
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("post_cloud_environment_switch_callback: network error: %d", (int)err_code);
                error_code = make_http_error(http_code, response);
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_cloud_environment_switch_callback: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(error_code);
        }
    });
}

void post_set_battery_store_mode(const std::string& device_sn, int battery_store_mode, const std::function<void(const SetBatteryStoreModeResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kBatteryStoreModeSwitchAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SetBatteryStoreModeRequest request;
    request.device_sn = device_sn;
    request.data.battery_store_mode = battery_store_mode;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_set_battery_store_mode: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_set_battery_store_mode: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_set_battery_store_mode] http response: %s", log_safe(response).c_str());
        SetBatteryStoreModeResponse set_battery_store_mode_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_set_battery_store_mode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                set_battery_store_mode_response.status = common_response.message;
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, set_battery_store_mode_response, &err, false)) {
                LOG_ERROR("post_set_battery_store_mode: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                set_battery_store_mode_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(set_battery_store_mode_response, error_code);
        }
    });
}

void put_manual_battery_swap(const std::string& device_sn, int slot_id, const std::function<void(const ManualBatterySwapResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kManualBatterySwapAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    ManualBatterySwapRequest request;
    request.device_sn = device_sn;
    request.data.slot_id = slot_id;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("put_manual_battery_swap: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("put_manual_battery_swap: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[put_manual_battery_swap] http response: %s", log_safe(response).c_str());
        ManualBatterySwapResponse manual_battery_swap_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("put_manual_battery_swap: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                manual_battery_swap_response.status = common_response.message;
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, manual_battery_swap_response, &err, false)) {
                LOG_ERROR("put_manual_battery_swap: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                manual_battery_swap_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(manual_battery_swap_response, error_code);
        }
    });
}

void get_manual_battery_swap(const std::string& device_sn, const std::function<void(const ManualBatterySwapProgressResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    // 注意：接口约定 query 键为 sn（非 device_sn）
    std::string api = std::string(kManualBatterySwapAPI) + "?sn=" + device_sn;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_manual_battery_swap: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        LOG_DEBUG("[get_manual_battery_swap] http response: %s", log_safe(response).c_str());
        ManualBatterySwapProgressResponse progress_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_manual_battery_swap: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                progress_response.status = common_response.message;
                break;
            }
            // 透传整段 data 到 data_json（供上层业务解析）
            if (common_response.data.has_value()) {
                progress_response.data_json = *common_response.data;
            }
            std::string err;
            // Response 中已声明 bid/status；若 data 结构不匹配 aigc 也不影响 data_json 保留
            parse_common_response_data(common_response, progress_response, &err, true);
        } while (false);
        if (callback) {
            callback(progress_response, error_code);
        }
    });
}

void post_dongle_power_switch(const std::string& device_sn, int power_state, const std::function<void(const DonglePowerSwitchResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDonglePowerSwitchAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    DonglePowerSwitchRequest request;
    request.device_sn = device_sn;
    request.data.action = power_state;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_dongle_power_switch: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_dongle_power_switch: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_dongle_power_switch] http response: %s", log_safe(response).c_str());
        DonglePowerSwitchResponse dongle_power_switch_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_dongle_power_switch: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                dongle_power_switch_response.status = common_response.message;
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, dongle_power_switch_response, &err, false)) {
                LOG_ERROR("post_dongle_power_switch: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                dongle_power_switch_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(dongle_power_switch_response, error_code);
        }
    });
}

void post_low_power_switch(const std::string& device_sn, int low_power_mode, const std::function<void(const LowPowerSwitchResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLowPowerSwitchAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LowPowerSwitchRequest request;
    request.device_sn = device_sn;
    request.data.low_power_mode = low_power_mode;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_low_power_switch: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_low_power_switch: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_low_power_switch] http response: %s", log_safe(response).c_str());
        LowPowerSwitchResponse low_power_switch_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_low_power_switch: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                low_power_switch_response.status = common_response.message;
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, low_power_switch_response, &err, false)) {
                LOG_ERROR("post_low_power_switch: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                low_power_switch_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(low_power_switch_response, error_code);
        }
    });
}

void get_esim_operator_switch(const std::string& device_sn, const std::function<void(const EsimOperatorSwitchProgressResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    // 注意：接口约定 query 键为 device_sn
    std::string api = std::string(kEsimOperatorSwitchAPI) + "?device_sn=" + device_sn;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_esim_operator_switch: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        LOG_DEBUG("[get_esim_operator_switch] http response: %s", log_safe(response).c_str());
        EsimOperatorSwitchProgressResponse progress_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_esim_operator_switch: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                progress_response.status = common_response.message;
                break;
            }
            if (common_response.data.has_value()) {
                progress_response.data_json = *common_response.data;
            }
            std::string err;
            parse_common_response_data(common_response, progress_response, &err, true);
        } while (false);
        if (callback) {
            callback(progress_response, error_code);
        }
    });
}

void post_close_debug_mode(const std::string& device_sn, const std::function<void(const CloseDebugModeResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCloseDebugModeAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_close_debug_mode: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_close_debug_mode:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_close_debug_mode] http response: %s", log_safe(response).c_str());
        CloseDebugModeResponse close_debug_mode_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_close_debug_mode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                close_debug_mode_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, close_debug_mode_response, &err, false)) {
                LOG_ERROR("post_close_debug_mode: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                close_debug_mode_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(close_debug_mode_response, error_code);
        }
    });
}

void post_open_dock_cover(const std::string& device_sn, const std::function<void(const OpenDockCoverResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kOpenDockCoverAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_open_dock_cover: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_open_dock_cover:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_open_dock_cover] http response: %s", log_safe(response).c_str());
        OpenDockCoverResponse open_dock_cover_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_open_dock_cover: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                open_dock_cover_response.status = common_response.message;
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, open_dock_cover_response, &err, false)) {
                LOG_ERROR("post_open_dock_cover: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                open_dock_cover_response.status = common_response.message;
                break;
            }
            
        } while (false);
        if (callback) {
            callback(open_dock_cover_response, error_code);
        }
    });
}

void get_open_dock_cover_progress(const std::string& device_sn, const std::function<void(const OpenDockCoverProgressResponse&, FlightHubError err)> callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kOpenDockCoverAPI;
    
    char param_buffer[128] = {'\0'};
    snprintf(param_buffer, sizeof(param_buffer), "device_sn=%s", device_sn.c_str());

    std::string complete_api = api + "?" + param_buffer;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("[get_open_dock_cover_progress] http response: %s", log_safe(response).c_str());
            OpenDockCoverProgressResponse open_dock_cover_progress_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_device_list_by_filter(), { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    open_dock_cover_progress_response.status = common_response.message;
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, open_dock_cover_progress_response, &err, false)) {
                    LOG_ERROR("[get_open_dock_cover_progress] get_device_list_by_filter(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    open_dock_cover_progress_response.status = common_response.message;
                    break;
                }
                if (!common_response.data.has_value()) {
                    open_dock_cover_progress_response.status = "no test data";
                }
            } while (0);

            if (callback) {
                callback(open_dock_cover_progress_response, error_code);
            }
        }
    );
}

void post_close_dock_cover(const std::string& device_sn, const std::function<void(const CloseDockCoverResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCloseDockCoverAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_close_dock_cover: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_close_dock_cover:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_close_dock_cover] http response: %s", log_safe(response).c_str());
        CloseDockCoverResponse close_dock_cover_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_close_dock_cover: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                close_dock_cover_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, close_dock_cover_response, &err, false)) {
                LOG_ERROR("post_close_dock_cover: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                close_dock_cover_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(close_dock_cover_response, error_code);
        }
    });
}

void get_close_dock_cover_progress(const std::string& device_sn, const std::function<void(const CloseDockCoverProgressResponse&, FlightHubError err)> callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kCloseDockCoverAPI;
    
    char param_buffer[128] = {'\0'};
    snprintf(param_buffer, sizeof(param_buffer), "device_sn=%s", device_sn.c_str());

    std::string complete_api = api + "?" + param_buffer;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("[get_close_dock_cover_progress] http response: %s", log_safe(response).c_str());
            CloseDockCoverProgressResponse close_dock_cover_progress_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_close_dock_cover_progress, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    close_dock_cover_progress_response.status = common_response.message;
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, close_dock_cover_progress_response, &err, false)) {
                    LOG_ERROR("get_close_dock_cover_progress, JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    close_dock_cover_progress_response.status = common_response.message;
                    break;
                }
                if (!common_response.data.has_value()) {
                    close_dock_cover_progress_response.status = "no test data";
                }
            } while (0);

            if (callback) {
                callback(close_dock_cover_progress_response, error_code);
            }
            return;
        }
    );
}

void post_device_reboot(const std::string& device_sn, const std::function<void(const DeviceRebootResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDeviceRebootAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_device_reboot: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_device_reboot:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_device_reboot] http response: %s", log_safe(response).c_str());
        DeviceRebootResponse device_reboot_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_device_reboot: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                device_reboot_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, device_reboot_response, &err, false)) {
                LOG_ERROR("post_device_reboot: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                device_reboot_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(device_reboot_response, error_code);
        }
    });
}

void get_device_reboot_progress(const std::string& device_sn, const std::function<void(const DeviceRebootProgressResponse&, FlightHubError err)> callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kDeviceRebootAPI;
    
    char param_buffer[128] = {'\0'};
    snprintf(param_buffer, sizeof(param_buffer), "device_sn=%s", device_sn.c_str());

    std::string complete_api = api + "?" + param_buffer;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("[get_device_reboot_progress] http response: %s", log_safe(response).c_str());
            DeviceRebootProgressResponse device_reboot_progress_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_close_dock_cover_progress, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    device_reboot_progress_response.status = common_response.message;
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, device_reboot_progress_response, &err, false)) {
                    LOG_ERROR("get_close_dock_cover_progress, JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    device_reboot_progress_response.status = common_response.message;
                    break;
                }
                if (!common_response.data.has_value()) {
                    device_reboot_progress_response.status = "no test data";
                }
            } while (0);
            if (callback) {
                callback(device_reboot_progress_response, error_code);
            }
        }
    );
}

void post_device_format(const std::string& device_sn, const std::function<void(const DeviceFormatResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDeviceFormatAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_device_format: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_device_format:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_device_format] http response: %s", log_safe(response).c_str());
        DeviceFormatResponse device_format_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_device_format: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                device_format_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, device_format_response, &err, false)) {
                LOG_ERROR("post_device_format: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                device_format_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(device_format_response, error_code);
        }
    });
}

void get_device_format_progress(const std::string& device_sn, const std::function<void(const DeviceFormatProgressResponse&, FlightHubError err)> callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kDeviceFormatAPI;
    
    char param_buffer[128] = {'\0'};
    snprintf(param_buffer, sizeof(param_buffer), "device_sn=%s", device_sn.c_str());

    std::string complete_api = api + "?" + param_buffer;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("[get_device_format_progress] http response: %s", log_safe(response).c_str());
            DeviceFormatProgressResponse device_format_progress_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_device_reboot_progress, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    device_format_progress_response.status = common_response.message;
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, device_format_progress_response, &err, false)) {
                    LOG_ERROR("get_device_reboot_progress, JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    device_format_progress_response.status = common_response.message;
                    break;
                }
                if (!common_response.data.has_value()) {
                    device_format_progress_response.status = "no test data";
                }
            } while (0);
            if (callback) {
                callback(device_format_progress_response, error_code);
            }
        }
    );
}

void post_drone_open(const std::string& device_sn, const std::function<void(const DroneOpenResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDroneOpenAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_drone_open: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_drone_open:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_drone_open] http response: %s", log_safe(response).c_str());
        DroneOpenResponse drone_open_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_drone_open: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                drone_open_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, drone_open_response, &err, false)) {
                LOG_ERROR("post_drone_open: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                drone_open_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(drone_open_response, error_code);
        }
    });
}

void post_drone_close(const std::string& device_sn, const std::function<void(const DroneCloseResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDroneCloseAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_drone_close: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_drone_close:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_drone_close] http response: %s", log_safe(response).c_str());
        DroneCloseResponse drone_close_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_drone_close: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                drone_close_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, drone_close_response, &err, false)) {
                LOG_ERROR("post_drone_close: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                drone_close_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(drone_close_response, error_code);
        }
    });
}

void post_open_charge(const std::string& device_sn, const std::function<void(const OpenChargeResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kOpenChargeAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_open_charge: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_open_charge:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_open_charge] http response: %s", log_safe(response).c_str());
        OpenChargeResponse open_charge_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_open_charge: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                open_charge_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, open_charge_response, &err, false)) {
                LOG_ERROR("post_open_charge: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                open_charge_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(open_charge_response, error_code);
        }
    });
}

void post_close_charge(const std::string& device_sn, const std::function<void(const CloseChargeResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCloseChargeAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_open_close: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_open_close:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_open_close] http response: %s", log_safe(response).c_str());
        CloseChargeResponse close_charge_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_open_close: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                close_charge_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, close_charge_response, &err, false)) {
                LOG_ERROR("post_open_close: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                close_charge_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(close_charge_response, error_code);
        }
    });
}

void get_dock_status(const std::vector<std::string>& device_sns, const std::function<void(const DockStatusResponse& data, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kDeviceStateAPI;
    // 按照device_sns拼接参数
    std::string params= "";
    for (size_t i = 0; i < device_sns.size(); ++i) {
        if (i == device_sns.size() - 1) {
            params += ("device_sns=" + device_sns[i] );
        } else {
            params += ("device_sns=" + device_sns[i] + "&");
        }
    }

    std::string complete_api = api + "?" + params;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("[get_dock_status] http response: %s", log_safe(response).c_str());
            DockStatusResponse dock_status_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                LOG_DEBUG(" get_dock_status, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                if (common_response.code != 0) {
                    LOG_ERROR(" get_dock_status, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, dock_status_response, &err, true)) {
                    LOG_ERROR("get_dock_status, JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (0);
            if (callback) {
                callback(dock_status_response, error_code);
            }
        }
    );
}

void get_drone_status(const std::vector<std::string>& device_sns, const std::function<void(const DroneStatusResponse& data, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kDeviceStateAPI;
    // 按照device_sns拼接参数
    std::string params= "";
    for (size_t i = 0; i < device_sns.size(); ++i) {
        if (i == device_sns.size() - 1) {
            params += ("device_sns=" + device_sns[i] );
        } else {
            params += ("device_sns=" + device_sns[i] + "&");
        }
    }

    std::string complete_api = api + "?" + params;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("[get_drone_status] http response: %s", log_safe(response).c_str());
            DroneStatusResponse drone_status_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                LOG_ERROR(" get_dock_status, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                if (common_response.code != 0) {
                    LOG_ERROR(" get_dock_status, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, drone_status_response, &err, true)) {
                    LOG_ERROR("get_dock_status, JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (0);
            if (callback) {
                callback(drone_status_response, error_code);
            }
        }
    );
}

void post_dock_silent_mode(const std::string& device_sn, const int silent_mode, const std::function<void(const DockSilentModeResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDockSilentModePI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    DockSilentModeRequest request;
    request.device_sn = device_sn;
    request.data.silent_mode = silent_mode;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_dock_silent_mode: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_dock_silent_mode:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_dock_silent_mode] http response: %s", log_safe(response).c_str());
        DockSilentModeResponse dock_silent_mode_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_dock_silent_mode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                dock_silent_mode_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(dock_silent_mode_response, error_code);
        }
    });
}

void post_dock_air_conditioner_mode(const std::string& device_sn, const int mode, const std::function<void(const DockAirConditionerResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDockAirConditionerAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    DockAirConditionerRequest request;
    request.device_sn = device_sn;
    request.data.mode = mode;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_dock_air_conditioner_mode: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_dock_air_conditioner_mode:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_dock_air_conditioner_mode] http response: %s", log_safe(response).c_str());
        DockAirConditionerResponse dock_air_conditioner_mode_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_dock_air_conditioner_mode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                dock_air_conditioner_mode_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(dock_air_conditioner_mode_response, error_code);
        }
    });
}

void post_rtk_calibration_by_network(NetworkRtkCalibrationRequest request, const std::function<void(const RtkCalibrationResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRtkCalibrationAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_rtk_calibration_by_network: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_rtk_calibration_by_network:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_rtk_calibration_by_network: response %s", log_safe(response).c_str());
        RtkCalibrationResponse rtk_calibration_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_rtk_calibration_by_network: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                rtk_calibration_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, rtk_calibration_response, &err, false)) {
                LOG_ERROR("post_rtk_calibration_by_network: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                rtk_calibration_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(rtk_calibration_response, error_code);
        }
    });
}

void post_rtk_calibration_by_manual(ManualRtkCalibrationRequest request, const std::function<void(const RtkCalibrationResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRtkCalibrationAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_rtk_calibration_by_manual: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_rtk_calibration_by_manual:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_rtk_calibration_by_manual: response %s", log_safe(response).c_str());
        RtkCalibrationResponse rtk_calibration_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_rtk_calibration_by_manual: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                rtk_calibration_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, rtk_calibration_response, &err, false)) {
                LOG_ERROR("post_rtk_calibration_by_manual: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                rtk_calibration_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(rtk_calibration_response, error_code);
        }
    });
}

void post_drone_format(const std::string& device_sn, const std::function<void(const DroneFormatResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDroneFormatAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_drone_format: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_drone_format:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        DroneFormatResponse drone_format_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_drone_format: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                drone_format_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, drone_format_response, &err, false)) {
                LOG_ERROR("post_drone_format: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                drone_format_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(drone_format_response, error_code);
        }
    });
}

void post_drone_sdr_work_mode(const std::string& device_sn, const int work_mode, const std::function<void(const DroneSdrWorkModeResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDroneSdrWorkModeAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    DroneSdrWorkModeRequest request;
    request.device_sn = device_sn;
    request.data.link_workmode = work_mode;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_drone_sdr_work_mode: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_drone_sdr_work_mode:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        DroneSdrWorkModeResponse drone_sdr_work_mode_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_drone_sdr_work_mode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                drone_sdr_work_mode_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, drone_sdr_work_mode_response, &err, false)) {
                LOG_ERROR("post_drone_format: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                drone_sdr_work_mode_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(drone_sdr_work_mode_response, error_code);
        }
    });
}

void post_start_network_speed_test(const std::string& device_sn, const std::function<void(const StartNetworkSpeedTestResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kStartNetworkSpeedTestAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_start_network_speed_test: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_start_network_speed_test:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        StartNetworkSpeedTestResponse start_network_speed_test_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_start_network_speed_test: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                start_network_speed_test_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, start_network_speed_test_response, &err, false)) {
                LOG_ERROR("post_start_network_speed_test: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                start_network_speed_test_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(start_network_speed_test_response, error_code);
        }
    });
}

void put_stop_network_speed_test(const std::string& device_sn, const std::function<void(const StopNetworkSpeedTestResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kStopNetworkSpeedTestAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("put_stop_network_speed_test: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("put_stop_network_speed_test:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("put_stop_network_speed_test: response: %s", log_safe(response).c_str());
        StopNetworkSpeedTestResponse stop_network_speed_test_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("put_stop_network_speed_test: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                stop_network_speed_test_response.status = common_response.message;
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, stop_network_speed_test_response, &err, false)) {
                LOG_ERROR("put_stop_network_speed_test: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                stop_network_speed_test_response.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(stop_network_speed_test_response, error_code);
        }
    });
}

void get_network_speed_test_progress(const std::string& device_sn, const std::function<void(const NetworkSpeedTestProgressResponse& data, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kNetworkSpeedTestProgressAPI;
    
    char param_buffer[128] = {'\0'};
    snprintf(param_buffer, sizeof(param_buffer), "device_sn=%s", device_sn.c_str());

    std::string complete_api = api + "?" + param_buffer;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_network_speed_test_progress: response: %s", log_safe(response).c_str());
            NetworkSpeedTestProgressResponse network_speed_test_progress_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_network_speed_test_progress, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    network_speed_test_progress_response.status = common_response.message;
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, network_speed_test_progress_response, &err, false)) {
                    LOG_ERROR("get_network_speed_test_progress, JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    network_speed_test_progress_response.status = common_response.message;
                    break;
                }
                if (!common_response.data.has_value()) {
                    network_speed_test_progress_response.status = "no test data";
                }
            } while (0);
            if (callback) {
                callback(network_speed_test_progress_response, error_code);
            }
        }
    );
}

void get_device_log_list(const std::string& device_sn, std::vector<int> module_list, const std::function<void(const DeviceLogListResponse& data, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kDeviceLogListAPI;

    std::string params = "device_sn=" + device_sn;
    params += "&";
    for (size_t i = 0; i < module_list.size(); ++i) {
        if (i == module_list.size() - 1) {
            params += ("module_list=" + std::to_string(module_list[i]));
        } else {
            params += ("module_list=" + std::to_string(module_list[i]) + "&");
        }
    }

    std::string complete_api = api + "?" + params;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_device_log_list: response: %s", log_safe(response).c_str());
            DeviceLogListResponse device_log_list_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_device_log_list, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, device_log_list_response, &err, true)) {
                    LOG_ERROR("get_device_log_list, JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (0);
            if (callback) {
                callback(device_log_list_response, error_code);
            }
        }
    , 60000);
}

void post_create_device_log_report(CreateDeviceLogReportRequest& request, const std::function<void(const CreateDeviceLogReportResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kCreateDeviceLogReportAPI;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_create_device_log_report: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_create_device_log_report:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_create_device_log_report: response: %s", log_safe(response).c_str());
        CreateDeviceLogReportResponse create_device_log_report_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_create_device_log_report: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, create_device_log_report_response, &err, true)) {
                LOG_ERROR("post_create_device_log_report: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(create_device_log_report_response, error_code);
        }
    });
}

void get_log_report_list(const std::string& device_sn, int page_index, int page_size, const std::function<void(const LogReportListResponse& data, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kLogReportListAPI;

    std::string params = "device_sn=" + device_sn;
    params += ("&page=" + std::to_string(page_index));
    params += ("&page_size=" + std::to_string(page_size));

    std::string complete_api = api + "?" + params;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("[DeviceListManager] get_log_report_list, { url: %s, uuid: %s, page_index: %d, page_size: %d}",
        url.c_str(), uuid.str().c_str(), page_index, page_size
    );

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_log_report_list: response: %s", log_safe(response).c_str());
            LogReportListResponse log_report_list_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_log_report_list, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, log_report_list_response, &err, true)) {
                    LOG_ERROR("get_log_report_list, JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (0);
            if (callback) {
                callback(log_report_list_response, error_code);
            }
        }
    );
}

void post_log_report_operation(const std::string& operation, const std::string& report_id, const std::function<void(FlightHubError err)>& callback) {
    if (operation != "cancel" && operation != "delete" && operation != "read") {
        LOG_ERROR("post_log_report_operation: invalid param : %s", operation.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM));
        return;
    }
    std::string api = kLogReportOperationAPI;
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    LogReportOperationRequest request;
    request.report_cmd_method = operation;
    request.report_uuid = report_id;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_log_report_operation: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_log_report_operation:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_log_report_operation: response: %s", log_safe(response).c_str());
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_log_report_operation: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(error_code);
        }
    });
}

void post_log_report_append_sts(const std::function<void(const LogReportAppendStsResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kLogReportAppendStsAPI;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    LogReportAppendStsRequest request;
    request.method = "airport_bug_report";

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_log_report_append_sts: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_log_report_append_sts:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_log_report_append_sts: response: %s", log_safe(response).c_str());
        LogReportAppendStsResponse log_report_append_sts_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_log_report_append_sts: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, log_report_append_sts_response, &err, true)) {
                LOG_ERROR("post_log_report_append_sts: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(log_report_append_sts_response, error_code);
        }
    });
}

void post_history_hms(HistoryHmsDataRequest& request, const std::function<void(const HistoryHmsDataResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kQueryHistoryHmsAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("get_history_hms: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("get_history_hms:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("get_history_hms: response: %s", log_safe(response).c_str());
        HistoryHmsDataResponse history_hms_data_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_history_hms: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            
            std::string err;
            if (!parse_common_response_data(common_response, history_hms_data_response, &err, true)) {
                LOG_ERROR("get_history_hms: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(history_hms_data_response, error_code);
        }
    });
}

void post_unbind_device(UnBindDeviceRequest& request, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kUnBindDeviceAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_unbind_device: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_unbind_device:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_unbind_device: response %s", log_safe(response).c_str());
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_unbind_device: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(error_code);
        }
    });
}

void get_device_detail(const std::string& device_sn, const std::function<void(const DeviceDetailResponse& data, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string api = kGetDeviceDetailAPI;

    std::string params = "device_sn=" + device_sn;

    std::string complete_api = api + "?" + params;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_device_detail, { url: %s, uuid: %s}", url.c_str(), uuid.str().c_str());

    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            DeviceDetailResponse device_detail_response;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR(" get_device_detail, { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, device_detail_response, &err, true)) {
                    LOG_ERROR("get_device_detail, JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (0);
            if (callback) {
                callback(device_detail_response, error_code);
            }
        }
    );
}

void put_modify_device_name(ModifyDeviceNameRequest& request, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kModifyDeviceNameAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("put_modify_device_name: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("put_modify_device_name:  url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("put_modify_device_name: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(error_code);
        }
    });
}

void get_latest_firmware_release_note(const std::vector<std::string>& device_model_keys, const std::string& language, std::function<void(const FirmwareReleaseNoteListResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string params;
    for (const auto& key : device_model_keys) {
        if (!params.empty()) {
            params += "&";
        }
        params += "device_model_key=" + key;
    }
    if (!language.empty()) {
        if (!params.empty()) {
            params += "&";
        }
        params += "lang=" + language;
    }

    std::string complete_api = std::string(kGetLatestFirmwareReleaseNoteAPI) + "?" + params;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_latest_firmware_release_note: url %s, uuid %s", url.c_str(), uuid.str().c_str());

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_latest_firmware_release_note: response %s", log_safe(response).c_str());
            FirmwareReleaseNoteListResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_latest_firmware_release_note: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("get_latest_firmware_release_note: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void post_invoke_firmware_upgrade(FirmwareUpgradeRequest& request, std::function<void(const FirmwareUpgradeResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kInvokeFirmwareUpgradeAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_invoke_firmware_upgrade: ObjectToJson error: %s", err.c_str());
        if (callback) {
            callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }
    LOG_DEBUG("post_invoke_firmware_upgrade: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("post_invoke_firmware_upgrade: response %s", log_safe(response).c_str());
            FirmwareUpgradeResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("post_invoke_firmware_upgrade: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("post_invoke_firmware_upgrade: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
                if (result.device_code != 0) {
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void get_query_firmware_upgrade_progress(const std::string& device_sn, std::function<void(const FirmwareUpgradeProgressResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string complete_api = std::string(kQueryFirmwareUpgradeProgressAPI) + "?device_sn=" + device_sn;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_query_firmware_upgrade_progress: url %s, uuid %s", url.c_str(), uuid.str().c_str());

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            FirmwareUpgradeProgressResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_query_firmware_upgrade_progress: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("get_query_firmware_upgrade_progress: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void post_firmware_upgrade_sts(std::function<void(const FirmwareUpgradeStsResponse& sts, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kFirmwareUpgradeStsAPI;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("post_firmware_upgrade_sts: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            FirmwareUpgradeStsResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("post_firmware_upgrade_sts: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("post_firmware_upgrade_sts: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void put_release_firmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string api = std::string(kUpdateFirmwareReleaseStatusAPI);
    api.replace(api.find("{id}"), 4, file_id);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body = R"({"status":"released"})";

    LOG_DEBUG("put_release_firmware: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("put_release_firmware: response %s", log_safe(response).c_str());
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("put_release_firmware: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(error_code);
            }
        },
        kFirmwareReleaseHttpTimeoutMs
    );
}

void put_unrelease_firmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string api = std::string(kUpdateFirmwareReleaseStatusAPI);
    api.replace(api.find("{id}"), 4, file_id);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body = R"({"status":"unreleased"})";

    LOG_DEBUG("put_unrelease_firmware: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("put_unrelease_firmware: response %s", log_safe(response).c_str());
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("put_unrelease_firmware: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(error_code);
            }
        }
    );
}

void delete_delete_firmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string api = std::string(kUpdateFirmwareReleaseStatusAPI);
    api.replace(api.find("{id}"), 4, file_id);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("delete_delete_firmware: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("delete_delete_firmware: response %s", log_safe(response).c_str());
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("delete_delete_firmware: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(error_code);
            }
        }
    );
}

void get_query_firmware_list(int page, int page_size, std::function<void(const FirmwareListResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    char param_buffer[64] = {'\0'};
    snprintf(param_buffer, sizeof(param_buffer), "?page=%d&page_size=%d", page, page_size);
    std::string api = std::string(kQueryFirmwareListAPI);
    std::string complete_api = api + param_buffer;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_query_firmware_list: url %s, uuid %s, page %d, page_size %d",
             url.c_str(), uuid.str().c_str(), page, page_size);

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_query_firmware_list: response %s", log_safe(response).c_str());
            FirmwareListResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_query_firmware_list: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("get_query_firmware_list: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void post_query_firmware_compatibility_config(std::function<void(const FirmwareCompatibilityConfigResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string api = std::string(kGetFirmwareCompatibilityAPI);
    api.replace(api.find("{userType}"), 10, "SDK");
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("post_query_firmware_compatibility_config: url %s, uuid %s", url.c_str(), uuid.str().c_str());

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("post_query_firmware_compatibility_config: response %s", log_safe(response).c_str());
            FirmwareCompatibilityConfigResponse result;
            auto error_code = FlightHubError::success();
            do {
                std::string err;
                aigc::JsonHelper::JsonToObject(result, response, {}, &err);
                if (!err.empty()) {
                    LOG_ERROR("post_query_firmware_compatibility_config: JsonToObject error: %s, response: %s", err.c_str(), log_safe(response).c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void get_check_firmware_status(const std::vector<std::string>& device_sns, const std::vector<std::string>& status, std::function<void(const FirmwareStatusListResponse& response, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string url = generate_url(config.address, config.port, kCheckFirmwareStatusAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CheckFirmwareStatusRequest request;
    request.device_sns = device_sns;
    request.status = status;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("get_check_firmware_status: ObjectToJson error: %s", err.c_str());
        if (callback) {
            callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }

    LOG_DEBUG("get_check_firmware_status: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_check_firmware_status: response %s", log_safe(response).c_str());
            FirmwareStatusListResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_check_firmware_status: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("get_check_firmware_status: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void post_submit_firmware_info(FirmwareInfoRequest& request, std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kSubmitFirmwareInfoAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_submit_firmware_info: ObjectToJson error: %s", err.c_str());
        if (callback) {
            callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }

    LOG_DEBUG("post_submit_firmware_info: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("post_submit_firmware_info: response %s", log_safe(response).c_str());
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("post_submit_firmware_info: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(error_code);
            }
        }
    );
}

void get_unlock_license_list(QueryDeviceUnlockLicenseListRequest& request, std::function<void(const QueryDeviceUnlockLicenseListResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string complete_api = std::string(kGetUnlockLicenseListAPI)
        + "?device_sn=" + request.device_sn
        + "&device_model_domain=" + std::to_string(request.device_model_domain);
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_unlock_license_list: url %s, uuid %s, device_sn %s, device_model_domain %d",
             url.c_str(), uuid.str().c_str(), request.device_sn.c_str(), request.device_model_domain);

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_unlock_license_list: response %s", log_safe(response).c_str());
            QueryDeviceUnlockLicenseListResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_unlock_license_list: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("get_unlock_license_list: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void post_switch_unlock_license(SwitchDeviceUnlockLicenseRequest& request, std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string url = generate_url(config.address, config.port, kSwitchUnlockLicenseAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_switch_unlock_license: ObjectToJson error: %s", err.c_str());
        if (callback) {
            callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }

    LOG_DEBUG("post_switch_unlock_license: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("post_switch_unlock_license: response %s", log_safe(response).c_str());
            auto error_code = FlightHubError::success();
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_switch_unlock_license: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            if (callback) {
                callback(error_code);
            }
        }
    );
}

void post_check_unlock_license(CheckUnlockLicenseRequest& request, std::function<void(const CheckUnlockLicenseResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string api = std::string(kCheckUnlockLicenseAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_check_unlock_license: ObjectToJson error: %s", err.c_str());
        if (callback) {
            callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }

    LOG_DEBUG("post_check_unlock_license: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("post_check_unlock_license: response %s", log_safe(response).c_str());
            CheckUnlockLicenseResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("post_check_unlock_license: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("post_check_unlock_license: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void post_import_unlock_license(ImportUnlockLicenseRequest& request, std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string api = std::string(kImportUnlockLicenseAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_import_unlock_license: ObjectToJson error: %s", err.c_str());
        if (callback) {
            callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }

    LOG_DEBUG("post_import_unlock_license: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("post_import_unlock_license: response %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_import_unlock_license: code: %d, message: %s", common_response.code, common_response.message.c_str());
                if (callback) {
                    callback(make_http_error(http_code, response));
                }
                return;
            }
            if (callback) {
                callback(FlightHubError::success());
            }
        }
    );
}

void post_import_unlock_license_sts(std::function<void(const GetImportUnlockLicenseStsResponse& data, FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string url = generate_url(config.address, config.port, kGetImportUnlockLicenseStsAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_import_unlock_license_sts: url %s, uuid %s", url.c_str(), uuid.str().c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_import_unlock_license_sts: response %s", log_safe(response).c_str());
            GetImportUnlockLicenseStsResponse result;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_import_unlock_license_sts: code: %d, message: %s", common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }

                std::string err;
                if (!parse_common_response_data(common_response, result, &err, true)) {
                    LOG_ERROR("get_import_unlock_license_sts: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                    error_code = make_http_error(http_code, response);
                    break;
                }
            } while (false);
            if (callback) {
                callback(result, error_code);
            }
        }
    );
}

void post_clear_device_unlock_license(const std::string& device_sn, std::function<void(FlightHubError err)> callback) {
    auto config = FlightHubSdk::getConfig();

    std::string url = generate_url(config.address, config.port, kClearDeviceUnlockLicenseAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_clear_device_unlock_license: ObjectToJson error: %s", err.c_str());
        if (callback) {
            callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }

    LOG_DEBUG("post_clear_device_unlock_license: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("post_clear_device_unlock_license: response %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_clear_device_unlock_license: code: %d, message: %s", common_response.code, common_response.message.c_str());
                if (callback) {
                    callback(make_http_error(http_code, response));
                }
                return;
            }
            if (callback) {
                callback(FlightHubError::success());
            }
        }
    );
}

void get_wayline_list(const GetWaylineListParam& param, const std::function<void(const GetWaylineListResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string params;
    params += "page=" + std::to_string(param.page);
    params += "&page_size=" + std::to_string(param.page_size);
    params += "&start_timestamp=" + std::to_string(param.start_timestamp);
    params += "&end_timestamp=" + std::to_string(param.end_timestamp);
    for (const auto& v : param.drone_types) { params += "&drone_types=" + v; }
    for (const auto& v : param.wayline_uuids) { params += "&wayline_uuids=" + v; }
    for (const auto& v : param.payload_types) { params += "&payload_types=" + v; }
    for (const auto& v : param.wayline_types) { params += "&wayline_types=" + v; }
    std::string url = generate_url(config.address, config.port, kGetWaylineListAPI + "?" + params);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    LOG_INFO("get_wayline_list: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("get_wayline_list: response %s", log_safe(response).c_str());
        GetWaylineListResponse get_wayline_list_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_wayline_list: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                if (common_response.code == 217012) {
                    error_code = FlightHubError::from_sdk(FlightHubSdkErrorCode::WAYLINE_ID_NOT_FOUND);
                }
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, get_wayline_list_response, &err, true)) {
                LOG_ERROR("get_wayline_list: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(get_wayline_list_response, error_code);
        }
    });
}

void get_wayline_detail(const std::string& wayline_uuid, const std::function<void(const GetWaylineDetailResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string params = "wayline_uuid=" + wayline_uuid;
    std::string url = generate_url(config.address, config.port, kGetWaylineDetailAPI + "?" + params);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    LOG_INFO("get_wayline_detail: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("get_wayline_detail: response %s", log_safe(response).c_str());
        GetWaylineDetailResponse get_wayline_detail_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_wayline_detail: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            std::string err;
            if (!parse_common_response_data(common_response, get_wayline_detail_response, &err, true)) {
                LOG_ERROR("get_wayline_detail: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(get_wayline_detail_response, error_code);
        }
    });
}

void post_batch_delete_waylines(const std::vector<std::string>& wayline_uuids, const std::function<void(const BatchDeleteWaylinesResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kBatchDeleteWaylinesAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    BatchDeleteWaylinesRequest request;
    request.wayline_uuids = wayline_uuids;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_batch_delete_waylines: ObjectToJson error: %s", err.c_str());
        BatchDeleteWaylinesResponse fail_resp;
        fail_resp.server_code = -1;
        fail_resp.server_message = err;
        callback(fail_resp, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_batch_delete_waylines: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_batch_delete_waylines: response %s", log_safe(response).c_str());
        BatchDeleteWaylinesResponse batch_delete_waylines_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            batch_delete_waylines_response.server_code = common_response.code;
            batch_delete_waylines_response.server_message = common_response.message;
            batch_delete_waylines_response.status = common_response.message;
            if (common_response.code != 0) {
                LOG_ERROR("post_batch_delete_waylines: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
        } while (false);
        if (callback) {
            callback(batch_delete_waylines_response, error_code);
        }
    });
}

void post_update_wayline(const std::string& wayline_uuid, const std::string& wayline_name, const std::function<void(const UpdateWaylineResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kUpdateWaylineAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    UpdateWaylineRequest request;
    request.wayline_uuid = wayline_uuid;
    request.wayline_name = wayline_name;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_update_wayline: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_update_wayline: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_update_wayline: response %s", log_safe(response).c_str());
        UpdateWaylineResponse update_wayline_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_update_wayline: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            update_wayline_response.status = common_response.message;
        } while (false);
        if (callback) {
            callback(update_wayline_response, error_code);
        }
    });
}

void post_import_wayline_callback(const SubmitWaylineInfoParam& param, const std::function<void(const SubmitWaylineInfoResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSubmitWaylineInfoAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    SubmitWaylineInfoRequest request;
    request.kmz_object_key = param.kmz_object_key;
    request.wayline_name = param.wayline_name;
    request.async_request_id = param.async_request_id;
    request.timestamp = param.timestamp;
    request.return_paas_data = param.return_paas_data;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_import_wayline_callback: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_import_wayline_callback: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_import_wayline_callback: response %s", log_safe(response).c_str());
        SubmitWaylineInfoResponse import_wayline_callback_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_import_wayline_callback: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            import_wayline_callback_response.status = common_response.message;
        } while (false);
        if (callback) {
            callback(import_wayline_callback_response, error_code);
        }
    });
}

void post_batch_download_waylines(const BatchDownloadWaylinesParam& param, const std::function<void(const BatchDownloadWaylinesResponse& data, FlightHubError err)>& callback) {    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kBatchDownloadWaylinesAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    BatchDownloadWaylinesRequest request;
    request.wayline_uuids = param.wayline_uuids;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_batch_download_waylines: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_batch_download_waylines: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_batch_download_waylines: response %s", log_safe(response).c_str());
        BatchDownloadWaylinesResponse batch_download_waylines_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_batch_download_waylines: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            std::string json_err;
            if (!parse_common_response_data(common_response, batch_download_waylines_response, &json_err, true)) {
                LOG_ERROR("post_batch_download_waylines: JsonToObject error: %s, data: %s", json_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
            }
        } while (false);
        if (callback) {
            callback(batch_download_waylines_response, error_code);
        }
    });
}

void post_duplicate_wayline(const DuplicateWaylineParam& param, const std::function<void(const DuplicateWaylineResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDuplicateWaylineAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    DuplicateWaylineRequest request;
    request.original_wayline_uuid = param.original_wayline_uuid;
    request.target_wayline_name = param.target_wayline_name;
    request.target_object_key_prefix = param.target_object_key_prefix;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_duplicate_wayline: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_duplicate_wayline: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_duplicate_wayline: response %s", log_safe(response).c_str());
        DuplicateWaylineResponse duplicate_wayline_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_duplicate_wayline: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            std::string json_err;
            if (!parse_common_response_data(common_response, duplicate_wayline_response, &json_err, true)) {
                LOG_ERROR("post_duplicate_wayline: JsonToObject error: %s, data: %s", json_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
            }
        } while (false);
        if (callback) {
            callback(duplicate_wayline_response, error_code);
        }
    });
}

// =========================================================================
// AI Inside 机载算法管理 HTTP 函数实现（PAAS 纯血，路径不含 /organizations/:uuid）
// =========================================================================

void get_ai_inside_models(
    const std::function<void(const GetAIInsideModelsResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kGetAIInsideModelsAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_ai_inside_models: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_ai_inside_models: response %s", log_safe(response).c_str());
            GetAIInsideModelsResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_ai_inside_models: code: %d, message: %s",
                              common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_ai_inside_models: JsonToObject error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void get_ai_inside_model_devices(
    const GetAIInsideModelDevicesParam& param,
    const std::function<void(const GetAIInsideModelDevicesResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    // 将 {signed_id} 占位符替换为实际 signed_id，与项目其他接口保持一致
    std::string path = std::string(kGetAIInsideModelDevicesAPI);
    const std::string ph = "{signed_id}";
    size_t pos = path.find(ph);
    if (pos != std::string::npos) path.replace(pos, ph.size(), param.signed_id);
    path += "?page=" + std::to_string(param.page);
    path += "&page_size=" + std::to_string(param.page_size);
    for (const auto& sn : param.drone_sn) {
        path += "&drone_sn=" + sn;
    }
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_ai_inside_model_devices: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
            LOG_DEBUG("get_ai_inside_model_devices: response %s", log_safe(response).c_str());
            GetAIInsideModelDevicesResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_ai_inside_model_devices: code: %d, message: %s",
                              common_response.code, common_response.message.c_str());
                    error_code = make_http_error(http_code, response);
                    break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_ai_inside_model_devices: JsonToObject error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void get_ai_inside_model_devices_summary(
    const GetAIInsideModelDevicesSummaryParam& param,
    const std::function<void(const GetAIInsideModelDevicesSummaryResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string path = std::string(kGetAIInsideModelDevicesSummaryAPI);
    const std::string ph = "{signed_id}";
    size_t pos = path.find(ph);
    if (pos != std::string::npos) path.replace(pos, ph.size(), param.signed_id);
    path += "?operation_type=" + param.operation_type;
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_ai_inside_model_devices_summary: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_ai_inside_model_devices_summary: response %s", log_safe(response).c_str());
            GetAIInsideModelDevicesSummaryResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) { error_code = make_http_error(http_code, response); }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void post_operate_ai_inside_model(
    const std::string& signed_id,
    OperateAIInsideModelRequest& request,
    const std::function<void(const OperateAIInsideModelResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string path = std::string(kOperateAIInsideModelAPI);
    const std::string ph = "{signed_id}";
    size_t pos = path.find(ph);
    if (pos != std::string::npos) path.replace(pos, ph.size(), signed_id);
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    std::string body, err_msg;
    aigc::JsonHelper::ObjectToJson(request, body, &err_msg);
    if (!err_msg.empty()) {
        LOG_ERROR("post_operate_ai_inside_model: ObjectToJson error: %s", err_msg.c_str());
        if (callback) { callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)); }
        return;
    }
    LOG_DEBUG("post_operate_ai_inside_model: url %s, body %s", url.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("post_operate_ai_inside_model: response %s", log_safe(response).c_str());
            OperateAIInsideModelResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) { error_code = make_http_error(http_code, response); }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void post_create_ai_inside_algorithm(
    CreateAIInsideAlgorithmRequest& request,
    const std::function<void(const CreateAIInsideAlgorithmResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCreateAIInsideAlgorithmAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    std::string body, err_msg;
    aigc::JsonHelper::ObjectToJson(request, body, &err_msg);
    if (!err_msg.empty()) {
        if (callback) { callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)); }
        return;
    }
    LOG_INFO("post_create_ai_inside_algorithm: url %s", url.c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("post_create_ai_inside_algorithm: response %s", log_safe(response).c_str());
            CreateAIInsideAlgorithmResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) { error_code = make_http_error(http_code, response); }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void post_file_upload_ai_inside_algorithm(
    FileUploadAIInsideAlgorithmRequest& request,
    const std::function<void(const FileUploadAIInsideAlgorithmResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kFileUploadAIInsideAlgorithmAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    std::string body, err_msg;
    aigc::JsonHelper::ObjectToJson(request, body, &err_msg);
    if (!err_msg.empty()) {
        if (callback) { callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)); }
        return;
    }
    LOG_INFO("post_file_upload_ai_inside_algorithm: url %s", url.c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("post_file_upload_ai_inside_algorithm: response %s", log_safe(response).c_str());
            FileUploadAIInsideAlgorithmResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) { error_code = make_http_error(http_code, response); }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void delete_ai_inside_algorithm(
    DeleteAIInsideAlgorithmRequest& request,
    const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kDeleteAIInsideAlgorithmAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    // delete_request 底层未自动根据 post_type 设置 Content-Type，需手动指定
    headers["Content-Type"] = "application/json";
    ensure_gateway_auth_headers_if_needed(url, headers);
    std::string body, err_msg;
    aigc::JsonHelper::ObjectToJson(request, body, &err_msg);
    if (!err_msg.empty()) {
        if (callback) { callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)); }
        return;
    }
    LOG_DEBUG("delete_ai_inside_algorithm: url %s, body %s", url.c_str(), log_safe(body).c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("delete_ai_inside_algorithm: response %s", log_safe(response).c_str());
            auto error_code = FlightHubError::success();
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("delete_ai_inside_algorithm: code: %d, message: %s",
                          common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            if (callback) { callback(error_code); }
        });
}

void post_copy_wayline(const CopyWaylineParam& param, const std::function<void(const CopyWaylineResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCopyWaylineAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    CopyWaylineRequest request;
    request.original_wayline_uuid = param.original_wayline_uuid;
    request.target_wayline_name = param.target_wayline_name;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_copy_wayline: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_copy_wayline: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_copy_wayline: response %s", log_safe(response).c_str());
        CopyWaylineResponse copy_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_copy_wayline: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            std::string json_err;
            if (!parse_common_response_data(common_response, copy_response, &json_err, true)) {
                LOG_ERROR("post_copy_wayline: JsonToObject error: %s, data: %s", json_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
            }
        } while (false);
        if (callback) {
            callback(copy_response, error_code);
        }
    });
}

void post_concat_wayline(const ConcatWaylineParam& param, const std::function<void(const ConcatWaylineResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kConcatWaylineAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ConcatWaylineRequest request;
    request.target_wayline_name = param.target_wayline_name;
    request.kmz_object_key = param.kmz_object_key;
    request.sub_wayline_uuids = param.sub_wayline_uuids;
    request.editor_warnings = param.editor_warnings;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_concat_wayline: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_concat_wayline: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_concat_wayline: response %s", log_safe(response).c_str());
        ConcatWaylineResponse concat_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_concat_wayline: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }
            std::string json_err;
            if (!parse_common_response_data(common_response, concat_response, &json_err, true)) {
                LOG_ERROR("post_concat_wayline: JsonToObject error: %s, data: %s", json_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
            }
        } while (false);
        if (callback) {
            callback(concat_response, error_code);
        }
    });
}

void post_import_kmz(const std::vector<std::string>& file_paths, const std::function<void(const ImportKmzResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kImportKmzAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    // 注意：不设置 Content-Type，让 libcurl multipart 自动生成 boundary

    LOG_INFO("post_import_kmz: url %s, uuid %s, file_count %zu", url.c_str(), uuid.str().c_str(), file_paths.size());

    drc_sdk::common::Network::Instance().FormPost(url, headers, file_paths,
        [callback](const drc_sdk::common::NetworkError err_code, const std::string& response,
                   const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("post_import_kmz: response %s", log_safe(response).c_str());
        ImportKmzResponse import_response;
        auto error_code = FlightHubError::success();
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("post_import_kmz: network error");
                error_code = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_import_kmz: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(0, response);
                break;
            }
            std::string json_err;
            if (!parse_common_response_data(common_response, import_response, &json_err, true)) {
                LOG_ERROR("post_import_kmz: JsonToObject error: %s, data: %s", json_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(0, response);
            }
        } while (false);
        if (callback) {
            callback(import_response, error_code);
        }
    });
}


// 获取设备报修配置;
void obtain_maintenance_orders_repair_config(ObtainRepairConfigRequest request, const std::function<void(const ObtainRepairConfigResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kMaintenanceOrdersRepairConfigAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("obtain_maintenance_orders_repair_config(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("obtain_maintenance_orders_repair_config(): { url: %s, uuid: %s, body: %s }",
        url.c_str(), uuid.str().c_str(), log_safe(body).c_str()
    );

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("obtain_maintenance_orders_repair_config(): { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str());
                break;
            }

            ObtainRepairConfigResponse http_resp;
            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("obtain_maintenance_orders_repair_config(): JsonToObject error: %s, data: %s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : ""
                );
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        callback({}, make_http_error(http_code, response));
    });
}


// 创建报修订单;
void create_maintenance_orders(CreateMaintenanceOrdersRequest request, const std::function<void(const CreateMaintenanceOrdersResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kMaintenanceOrdersAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("create_maintenance_orders(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("create_maintenance_orders(): { url: %s, uuid: %s, body: %s }",
        url.c_str(), uuid.str().c_str(), log_safe(body).c_str()
    );

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("create_maintenance_orders(): { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str());
                break;
            }

            CreateMaintenanceOrdersResponse http_resp;
            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("create_maintenance_orders(): JsonToObject error: %s, data: %s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        callback({}, make_http_error(http_code, response));
    });
}


// 查询单个报修订单详情;
void get_maintenance_order_detail(const std::string &order_number,
    const std::function<void(const MaintenanceOrderDetailResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();

    std::string api = std::string(kMaintenanceOrderDetailAPI);
    auto pos = api.find("{orderNumber}");
    if (pos != std::string::npos) {
        api.replace(pos, std::string("{orderNumber}").size(), order_number);
    }

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_maintenance_order_detail(): { url: %s, uuid: %s, order_number: %s }",
        url.c_str(), uuid.str().c_str(), order_number.c_str()
    );

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_maintenance_order_detail(): { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str());
                break;
            }

            MaintenanceOrderDetailResponse http_resp;
            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("get_maintenance_order_detail(): JsonToObject error: %s, data: %s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        callback({}, make_http_error(http_code, response));
    });
}



// 分页查询报修订单列表;
// GET /device/api/v1/manager/maintenance-orders?
// page=1&page_size=100&order_number=xxxx&status=2&initiated_from=0&device_sn=SNSN&creator_id=1,2,3,4&
// q=KKK&created_at_start=1111111&created_at_end=2222222&sort_column=order_number&sort_type=asc
void get_maintenance_orders_list(const GetMaintenanceOrdersListRequest &params,
    const std::function<void(const MaintenanceOrdersListResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();

    std::string param_str;
    if (params.page >= 1 && params.page_size >= 1) {
        param_str += ("page=" + std::to_string(params.page) + "&");
        param_str += ("page_size=" + std::to_string(params.page_size) + "&");
    }
    if (!params.order_number.empty()) {
        param_str += ("order_number=" + params.order_number + "&");
    }
    if (!params.status.empty()) {
        param_str += ("status=" + params.status + "&");
    }
    if (!params.initiated_from.empty()) {
        param_str += ("initiated_from=" + params.initiated_from + "&");
    }
    if (!params.device_sn.empty()) {
        param_str += ("device_sn=" + params.device_sn + "&");
    }

    if (!params.creator_ids.empty()) {
        // creator_id=1,2,3,4&
        std::string temp = "creator_id=";
        for (const auto &id : params.creator_ids) {
            temp += (id + ",");
        }

        if (temp.back() == ',') {
            temp.pop_back();
        }

        temp += "&";
        param_str += temp;
    }


    if (!params.key_word.empty()) {
        param_str += ("q=" + params.key_word + "&");
    }
    if (!params.created_at_start.empty()) {
        param_str += ("created_at_start=" + params.created_at_start + "&");
    }
    if (!params.created_at_end.empty()) {
        param_str += ("created_at_end=" + params.created_at_end + "&");
    }
    if (!params.sort_column.empty()) {
        param_str += ("sort_column=" + params.sort_column + "&");
    }
    if (!params.sort_type.empty()) {
        param_str += ("sort_type=" + params.sort_type + "&");
    }

    // 去尾;
    if (!param_str.empty() && (param_str.back() == '&')) {
        param_str.pop_back();
    }

    std::string complete_api = std::string(kMaintenanceOrdersAPI);
    if (!param_str.empty()) {
        complete_api += ("?" + param_str);
    }

    // 拼装 API;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_maintenance_orders_list(): { url: %s, uuid: %s }",
        url.c_str(), uuid.str().c_str()
    );

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_maintenance_orders_list(): { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str());
                break;
            }

            MaintenanceOrdersListResponse http_resp;
            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("get_maintenance_orders_list(): JsonToObject error: %s, data: %s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        callback({}, make_http_error(http_code, response));
    });
}



/// Safety Services;
// signal-maps/search, 信号地图查询;
void search_signal_maps(SearchSignalMapsRequest request, const std::function<void(const SignalMapsResponse& response, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string url = generate_url(config.address, config.port, kSearchSignalMapsAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_DEBUG("search_signal_maps(), { url: %s, uuid: %s, request param: {%s} }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("search_signal_maps(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }


    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("search_signal_maps(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("search_signal_maps(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // 反序列化信号地图响应;
            SignalMapsResponse signal_maps_response;

            std::string err;
            if (!parse_common_response_data(common_response, signal_maps_response, &err, true)) {
                LOG_ERROR("search_signal_maps(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            callback(signal_maps_response, FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback({}, make_http_error(http_code, response));
        return;
    });
}


// [自定义飞行区];
// 自定义飞行区: 创建多种自定义类型的飞行区;
void create_customized_flight_areas(
    CreateCustomizedFlightAreasRequest request,
    const std::function<void(const CreateFlightAreasResponse& response, FlightHubError err)>& callback) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string url = generate_url(config.address, config.port, kCreateFlightAreas);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    // Log: 打印参数信息;
    LOG_DEBUG("create_customized_flight_areas(), { url: %s, uuid: %s, request param: {%s} }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("create_customized_flight_areas(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("create_customized_flight_areas(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("create_customized_flight_areas(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // 反序列化创建飞行区响应;
            CreateFlightAreasResponse http_resp;

            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("create_customized_flight_areas(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback({}, make_http_error(http_code, response));
        return;
    });
}


// [自定义飞行区];
// 自定义飞行区: 获取飞行区列表;
void get_customized_flight_areas(
    const std::string &name,             // 飞行区名称, 可选参数, 空值表示不使用该项过滤条件;
    const std::string &type,             // 飞行区类型, 可选参数, 空值表示不使用该项过滤条件, {"dfence": 自定义作业区; "nfz": 自定义限飞区; "noland": 自定义禁降区};
    const std::string &status,           // 飞行区状态, 可选参数, 空值表示不使用该项过滤条件, {"disable": 已禁用; "enable": 已启用};
    const std::vector<std::string> &ids, // 飞行区id列表, 可选参数, 空值表示不使用该项过滤条件, 示例: ["48a1cd0b-346e-4a47-a1b6-0c335cbc2487"];
    int page_index,                      // 页码, 页下标(从1开始), 可选参数，传负数(表示不使用分页条件, 比如-1)则查询所有飞行区;
    int page_size,                       // 页大小(比如 100), 可选参数，传负数(表示不使用分页条件, 比如-1)则查询所有飞行区(page_index 和 page_size 一起使用);
    const std::function<void(const GetFlightAreasResponse& response, FlightHubError err)>& callback
) {
    // url 示例;
    // GET /safety/api/v1/flight-areas?name=example&type=dfence&status=disable&ids=<uuid>&page=1&page_size=100
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    std::string param_str;
    if (!name.empty()) {
        param_str += ("name=" + name + "&");
    }

    if (!type.empty()) {
        param_str += ("type=" + type + "&");
    }

    if (!status.empty()) {
        param_str += ("status=" + status + "&");
    }

    if (!ids.empty()) {
        for (const auto &id : ids) {
            param_str += ("ids=" + id + "&");
        }
    }

    // 此处不宜做严格参数验证;
    bool use_pagenation = (page_index >= 0 && page_size >= 0);
    if (use_pagenation) {
        param_str += ("page=" + std::to_string(page_index) + "&");
        param_str += ("page_size=" + std::to_string(page_size) + "&");
    }

    // todo: 此处可继续添加查询过滤参数;

    // 最后一步: 拼接 url;
    if (!param_str.empty() && (param_str.back() == '&')) {
        // 去掉最后一个 '&';
        param_str.pop_back();
    }

    // 默认 api;
    std::string complete_api = kGetFlightAreas;
    // 拼装 url;
    if (!param_str.empty()) {
        complete_api += ("?" + param_str);
    }


    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
 

    // Log: 打印参数信息;
    LOG_INFO("get_flight_areas(), { url: %s, uuid: %s }",
        url.c_str(),
        uuid.str().c_str()
    );


    // GET 方法;
    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            do {
                // 检查网络层错误(超时、连接失败等);
                if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                    LOG_ERROR("get_flight_areas(), network error: %d, http_code: %d",
                        static_cast<int>(err_code), http_code);
                    break;
                }

                // 解析通用响应结构, 检查业务错误码;
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_flight_areas(), { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    break;
                }

                // 反序列化飞行区列表响应;
                GetFlightAreasResponse http_response_data;
                std::string err;
                if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                    LOG_ERROR("get_flight_areas(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    break;
                }

                callback(http_response_data, FlightHubError::success());
                return;
            } while (0);

            // 上述任一检查失败, 统一回调错误;
            callback({}, make_http_error(http_code, response));
            return;
        }
    );
}


// [自定义飞行区];
// 自定义飞行区: 更新飞行区;
void update_customized_flight_areas(
    const std::string &flight_area_id,    // 请求的 path 参数;
    UpdateCustomizedFlightAreasRequest request,
    const std::function<void(const UpdateFlightAreasResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string api = std::string(kUpdateFlightAreas);
    api.replace(api.find(kFlightAreaID), kFlightAreaID.size(), flight_area_id);

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("update_customized_flight_areas(): ObjectToJson error: %s, request params {%s}",
            err.c_str(), flight_hub_sdk::log_safe(to_string(request)).c_str()
        );

        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("update_customized_flight_areas(): url: %s, uuid: \"%s\", body: %s, request params {%s}",
        url.c_str(),
        uuid.str().c_str(),
        log_safe(body).c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );


    // PUT 方法;
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("update_customized_flight_areas(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("update_customized_flight_areas(): code: %d, message: %s, response: %s",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // 反序列化更新飞行区响应;
            UpdateFlightAreasResponse http_response_data;
            std::string err;
            if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                LOG_ERROR("update_customized_flight_areas(): JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }
            callback(http_response_data, FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback({}, make_http_error(http_code, response));
        return;
    }, 15000);
}


// [自定义飞行区];
// 自定义飞行区: 删除飞行区;
void delete_customized_flight_areas(
    const std::string &flight_area_id,    // 请求的 path 参数;
    const std::function<void(FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string api = std::string(kDeleteFlightAreas);
    api.replace(api.find(kFlightAreaID), kFlightAreaID.size(), flight_area_id);


    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();


    // Log: 打印参数信息;
    LOG_INFO("delete_customized_flight_areas(): url: %s, uuid: \"%s\" ",
        url.c_str(),
        uuid.str().c_str()
    );


    // DELETE 方法;
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "", 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("delete_customized_flight_areas(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("delete_customized_flight_areas(): code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }

            callback(FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback(make_http_error(http_code, response));
        return;
    }, 15000);
}


// [自定义飞行区];
// 同步自定义飞行区;
void sync_customized_flight_areas(
    const std::vector<std::string> &device_sns,
    const std::function<void(const SyncFlightAreasResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kSyncFlightAreas);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    // 拼装 common request 参数;
    CommonRequestWithDeviceSNs request;
    request.devices = device_sns;

    // Log: 打印参数信息;
    LOG_DEBUG("sync_customized_flight_areas(), { url: %s, uuid: %s, request param: {%s} }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );


    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("sync_customized_flight_areas(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("sync_customized_flight_areas(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            SyncFlightAreasResponse http_response_data;

            std::string err;
            if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                LOG_ERROR("sync_customized_flight_areas(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            // indicate success;
            callback(http_response_data, FlightHubError::success());
            return;
        } while (0);


        // indicate error;
        callback({}, make_http_error(http_code, response));
        return;
    });
}



// [自定义飞行区];
// 获取设备同步状态;
void get_flight_areas_sync_status(
    const std::vector<std::string> &device_sns,
    const std::function<void(const GetAreasSyncStatusResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kFlightAreasSyncStatus);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    // 拼装 common request 参数;
    CommonRequestWithDeviceSNs request;
    request.devices = device_sns;


    LOG_DEBUG("get_flight_areas_sync_status(), { url: %s, uuid: %s, request param: {%s} }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("get_flight_areas_sync_status(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("get_flight_areas_sync_status(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_flight_areas_sync_status(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // 反序列化设备同步状态响应;
            GetAreasSyncStatusResponse http_resp;

            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("get_flight_areas_sync_status(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback({}, make_http_error(http_code, response));
        return;
    });
}


// [地形障碍物];
// 查询离线地图模型信息;
void get_offline_maps_model_info(
    const std::function<void(const GetOfflineMapsModelInfoResponse& response, FlightHubError err)>& callback
) {
    // url 示例;
    // GET /safety/api/v1/offline-maps

    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 默认 api;
    std::string complete_api = kGetOfflineMaps;
    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    // 打印参数信息;
    LOG_INFO("get_offline_maps_model_info(), { url: %s, uuid(x-request-id): %s }",
        url.c_str(),
        uuid.str().c_str()
    );

    // GET 方法;
    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            do {
                // 检查网络层错误(超时、连接失败等);
                if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                    LOG_ERROR("get_offline_maps_model_info(), network error: %d, http_code: %d",
                        static_cast<int>(err_code), http_code);
                    break;
                }

                // 解析通用响应结构, 检查业务错误码;
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_offline_maps_model_info(), { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    break;
                }

                // 反序列化离线地图模型信息响应;
                GetOfflineMapsModelInfoResponse http_response_data;
                std::string err;
                if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                    LOG_ERROR("get_offline_maps_model_info(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    break;
                }

                callback(http_response_data, FlightHubError::success());
                return;
            } while (0);

            // indicate error;
            callback({}, make_http_error(http_code, response));
            return;
        }
    );
}


// [地形障碍物];
// 创建/更新离线地图模型;
void update_offline_maps_model_info(
    UpdateOfflineMapsRequest request,
    const std::function<void(FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string api = std::string(kUpdateOfflineMaps);

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();


    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("update_offline_maps_model_info(): ObjectToJson error: %s, request params {%s}",
            err.c_str(), flight_hub_sdk::log_safe(to_string(request)).c_str()
        );

        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("update_offline_maps_model_info(): url: %s, uuid: \"%s\", body: %s, request params {%s}",
        url.c_str(),
        uuid.str().c_str(),
        log_safe(body).c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );


    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("update_offline_maps_model_info(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("update_offline_maps_model_info(): code: %d, message: %s, response: %s",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            callback(FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback(make_http_error(http_code, response));
        return;
    });
}


// [地形障碍物];
// 取消离线地图生成;
void cancel_offline_maps_model_info(
    const std::function<void(FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string api = std::string(kCancelOfflineMaps);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();


    // 打印参数信息;
    LOG_INFO("cancel_offline_maps_model_info(): { url: %s, uuid: \"%s\" }",
        url.c_str(),
        uuid.str().c_str()
    );


    // DELETE 方法;
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "", 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("cancel_offline_maps_model_info(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("cancel_offline_maps_model_info(): code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }

            callback(FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback(make_http_error(http_code, response));
        return;
    }, 15000);
}


// [地形障碍物];
// 修改离线地图(开启/关闭);
void set_offline_maps(
    SetOfflineMapsRequest request,
    const std::function<void(FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();
    std::string api = kModifyOfflineMaps;
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();


    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("set_offline_maps(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    
    // 打印参数信息;
    LOG_DEBUG("set_offline_maps(): url: %s, uuid: %s, body: %s", 
        url.c_str(),
        uuid.str().c_str(),
        log_safe(body).c_str()
    );

    // PUT 方法;
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("set_offline_maps(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            LOG_DEBUG("set_offline_maps() response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("set_offline_maps(): code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }

            callback(FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback(make_http_error(http_code, response));
        return;
    });
}


// [地形障碍物];
// 离线地图同步;
void sync_offline_maps(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN;
    const std::function<void(const OfflineMapsSyncResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kSyncOfflineMaps);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    // 拼装 common request 参数;
    CommonRequestWithDeviceSNs request;
    request.devices = device_sns;

    LOG_DEBUG("sync_offline_maps(), { url: %s, uuid: %s, request param: {%s} }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );


    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("sync_offline_maps(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("sync_offline_maps(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("sync_offline_maps(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // 反序列化离线地图同步响应;
            OfflineMapsSyncResponse http_response_data;

            std::string err;
            if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                LOG_ERROR("sync_offline_maps(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            callback(http_response_data, FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback({}, make_http_error(http_code, response));
        return;
    }, 60000);
}


// [地形障碍物];
// 获取离线地图同步状态;
void get_offline_maps_sync_status(
    const std::vector<std::string> &device_sns,  // 待同步设备的SN;
    const std::function<void(const GetOfflineMapsSyncStatusResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kSyncOfflineMapsStatus);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();


    // 拼装 common request 参数;
    CommonRequestWithDeviceSNs request;
    request.devices = device_sns;

    // 打印参数信息;
    LOG_DEBUG("get_offline_maps_sync_status(), { url: %s, uuid: %s, request param: {%s} }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("get_offline_maps_sync_status(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            // 检查网络层错误(超时、连接失败等);
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("get_offline_maps_sync_status(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            // 解析通用响应结构, 检查业务错误码;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_offline_maps_sync_status(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // 反序列化离线地图同步状态响应;
            GetOfflineMapsSyncStatusResponse http_resp;

            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("get_offline_maps_sync_status(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        // 上述任一检查失败, 统一回调错误;
        callback({}, make_http_error(http_code, response));
        return;
    });
}


/// Media Service
// [媒体服务]
// 获取媒体文件列表;
void get_media_files_list(
    const GetMediaFilesListParams &request_params,
    const std::function<void(const GetMediaFilesListResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kGetMediaFileListAPI);
    
    std::string query_params;
    // flight_id, [可选参数] 飞行航线ID;
    if (!request_params.flight_id.empty()) {
        query_params += ("flight_id=" + request_params.flight_id);
        query_params += ("&");
    }

    // [必须参数] page-index & page-size;
    query_params += ("start_index=" + std::to_string(request_params.start_index) + "&");
    query_params += ("page_size=" + std::to_string(request_params.page_size) + "&");

    // [可选参数] file_types, 文件枚举类型(2图片、3视频), ["2", "3"];
    if (!request_params.file_types.empty()) {
        for (const auto &file_type : request_params.file_types) {
            query_params += ("file_types=" + file_type + "&");
        }
    }

    // device_sn,  [可选参数] 设备sn;
    if (!request_params.device_sn.empty()) {
        query_params += ("device_sn=" + request_params.device_sn + "&");
    }

    // [可选参数] start_create_time & end_create_time;
    // [可选参数] 开始时间, 时间戳: 秒;
    if (request_params.start_create_time >= 0) {
        query_params += ("start_create_time=" + std::to_string(request_params.start_create_time) + "&");
    }

    // [可选参数] 结束时间, 时间戳: 秒;
    if (request_params.end_create_time >= 0) {
        query_params += ("end_create_time=" + std::to_string(request_params.end_create_time) + "&");
    }

    // 最后的 query 参数不要带上 "&";
    if (!query_params.empty() && (query_params.back() == '&')) {
        query_params.pop_back();
    }

    // 组装 Query url;
    if (!query_params.empty()) {
        url += ("?" + query_params);
    }

    // generate uuid(x-request-id);
    auto uuid = xg::newGuid();

    // 填充 HTTP headers;
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_media_files_list(), { url: %s, (x-request-id)uuid: %s }",
        url.c_str(),
        uuid.str().c_str()
    );

    // GET 方法;
    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_media_files_list(), { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    break;
                }

                // 解析并回调结果数据;
                GetMediaFilesListResponse http_response_data;
                std::string err;
                if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                    LOG_ERROR("get_media_files_list(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    break;
                }

                // indicate success;
                callback(http_response_data, FlightHubError::success());
                return;
            } while (0);

            // indicate error;
            callback({}, make_http_error(http_code, response));
            return;
        }
    );
}

// [媒体服务]
// 获取媒体文件下载地址;
void get_media_files_download_url(
    long long file_id,    // 文件 id;
    const std::function<void(const GetMediaFilesDownloadUrlResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // GET /pf-media/api/v1/media-files/url?file_id=1

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kGetMediaFileUrlAPI);
    
    std::string query_params;
    // file_id;
    query_params += ("file_id=" + std::to_string(file_id));

    // 组装 Query url;
    url += ("?" + query_params);

    // generate uuid(x-request-id);
    auto uuid = xg::newGuid();

    // 填充 HTTP headers;
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_media_files_download_url(), { url: %s, (x-request-id)uuid: %s }",
        url.c_str(),
        uuid.str().c_str()
    );

    // GET 方法;
    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_media_files_download_url(), { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    break;
                }

                GetMediaFilesDownloadUrlResponse http_response_data;
                std::string err;
                if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                    LOG_ERROR("get_media_files_download_url(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    break;
                }

                // indicate success;
                callback(http_response_data, FlightHubError::success());
                return;
            } while (0);

            // indicate error;
            callback({}, make_http_error(http_code, response));
            return;
        }
    );
}


// [媒体服务]
// 获取媒体文件下载地址;
void get_media_files_download_url(
    const std::vector<std::string> & object_keys,
    const std::function<void(const GetMediaFilesDownloadUrlListResponse& response, FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kGetMediaFileUrlAPI);

    GetMediaFilesDownloadUrlListRequest request;
    request.object_keys = object_keys;

    // generate uuid(x-request-id);
    auto uuid = xg::newGuid();

    // 填充 HTTP headers;
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_media_files_download_url(), { url: %s, (x-request-id)uuid: %s }",
        url.c_str(),
        uuid.str().c_str()
    );

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("get_media_files_download_url(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("get_media_files_download_url(), network error: %d, http_code: %d",
                    static_cast<int>(err_code), http_code);
                break;
            }

            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_media_files_download_url(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            GetMediaFilesDownloadUrlListResponse http_response_data;
            std::string err;
            if (!parse_common_response_data(common_response, http_response_data, &err, false)) {
                LOG_ERROR("get_media_files_download_url(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            // indicate success;
            callback(http_response_data, FlightHubError::success());
            return;
        } while (0);

        // indicate error;
        callback({}, make_http_error(http_code, response));
        return;
    });
}


// [媒体服务]
// 批量删除媒体文件;
void delete_media_files(
    const std::vector<long long> &file_ids,    // 文件 id 列表;
    const std::function<void(FlightHubError err)>& callback
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kDeleteMediaFileAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    // 拼装 common request 参数;
    CommonMediaServiceRequestWithFileIDs request;
    request.file_ids.reserve(file_ids.size());

    for (auto file_id : file_ids) {
        request.file_ids.emplace_back(static_cast<int64_t>(file_id));
    }

    LOG_DEBUG("delete_media_files(), { url: %s, uuid: %s, request param: {%s} }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request)).c_str()
    );


    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("delete_media_files(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("delete_media_files(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // indicate success;
            callback(FlightHubError::success());
            return;
        } while (0);

        // indicate error;
        callback(make_http_error(http_code, response));
        return;
    });
}


// [ATC 空管服务]
// 导出飞行记录;
void export_flight_records(
    const std::string &user_id,                // [必选参数] 用户 id, 比如 "1547828723498192896";
    const std::string &project_id,             // [可选参数] 比如 "f111d837-7740-470b-9a44-e52fba516085", 默认传空字符串;
    ExportFlightRecordsRequest request,        // [必选参数] 请求 body 参数;
    const std::function<void(const ExportFlightRecordsResponse& response, FlightHubError err)>& callback // [必选参数] 回调函数;
) {
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kExportFlightRecordsAPI);

    // generate uuid(x-request-id);
    auto uuid = xg::newGuid();

    // 填充 HTTP headers;
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["x-user-id"] = user_id;

    // 可选参数;
    if (!project_id.empty()) {
        // 填充 project_id;
        headers["x-project-id"] = project_id;
    }

    // 序列化请求 body;
    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("export_flight_records(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("export_flight_records(), { url: %s, (x-request-id)uuid: %s, user_id: \"%s\", project_id: \"%s\", body: %s }",
        url.c_str(),
        uuid.str().c_str(),
        user_id.c_str(),
        project_id.c_str(),
        log_safe(body).c_str()
    );

    // POST 方法;
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("export_flight_records(), { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                );
                break;
            }

            // parse response;
            ExportFlightRecordsResponse http_response_data;
            std::string err;
            if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                LOG_ERROR("export_flight_records(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                );
                break;
            }

            // indicate success;
            callback(http_response_data, FlightHubError::success());
            return;
        } while (0);

        // indicate error;
        callback({}, make_http_error(http_code, response));
        return;
    });
}


// [ATC 空管服务]
// 查询导出进度;
void get_export_flight_records_progress(
    const std::string &project_id,                    // [可选参数] 比如 "f111d837-7740-470b-9a44-e52fba516085", 默认传空字符串;
    const GetExportFlightRecordsProgressParams &request_params,
    const std::function<void(const GetExportFlightRecordsProgressResponse& response, FlightHubError err)>& callback
) {
    // GET /atc/api/v1/flight-tasks/records/export?content_type=detail&status=0&sort=ASC&page=1&page_size=50&export_id=<id>
    // Get configs;
    auto config = FlightHubSdk::getConfig();

    // 拼装基础 api;
    std::string url = generate_url(config.address, config.port, kGetExportFlightRecordsProgressAPI);
    
    // 组装 Query params;
    std::string query_params;
    // content_type;
    // 导出类型(详细信息:"detail", 概要信息: "summary", 空字符串空则查询全部[不传该字段])
    if (!request_params.content_type.empty()) {
        query_params += ("content_type=" + request_params.content_type) + "&";
    }

    // status;
    if (request_params.status >= 0) {
        query_params += ("status=" + std::to_string(request_params.status)) + "&";
    }

    // sort;
    if (!request_params.sort.empty()) {
        query_params += ("sort=" + request_params.sort) + "&";
    }
    
    // page;
    query_params += ("page=" + std::to_string(request_params.page)) + "&";

    // page_size;
    query_params += ("page_size=" + std::to_string(request_params.page_size)) + "&";
    
    // export_id;
    //  指定查询某一次导出记录, 空字符串表示不指定;
    if (!request_params.export_id.empty()) {
        query_params += ("export_id=" + request_params.export_id) + "&";
    }


    // 去除最后一个 '&';
    if (!query_params.empty() && (query_params.back() == '&')) {
        query_params.pop_back();
    }

    if (!query_params.empty()) {
        // 组装 Query url;
        url += ("?" + query_params);
    }

    // generate uuid(x-request-id);
    auto uuid = xg::newGuid();

    // 填充 HTTP headers;
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["x-project-id"] = project_id;

    LOG_DEBUG("get_export_flight_records_progress(), { url: %s, (x-request-id)uuid: %s, query_params: { %s } }",
        url.c_str(),
        uuid.str().c_str(),
        flight_hub_sdk::log_safe(to_string(request_params)).c_str()
    );

    // GET 方法;
    HttpGetInnerWithStatus(url, headers, 
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    LOG_ERROR("get_export_flight_records_progress(), { code: %d, message: %s, http_response: %s }",
                        common_response.code, common_response.message.c_str(), log_safe(response).c_str()
                    );
                    break;
                }

                // parse response;
                GetExportFlightRecordsProgressResponse http_response_data;
                std::string err;
                if (!parse_common_response_data(common_response, http_response_data, &err, true)) {
                    LOG_ERROR("get_export_flight_records_progress(), JsonToObject() { error: %s, data: %s, http_response: %s }",
                        err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "", log_safe(response).c_str()
                    );
                    break;
                }

                // indicate success;
                callback(http_response_data, FlightHubError::success());
                return;
            } while (0);

            // indicate error;
            callback({}, make_http_error(http_code, response));
            return;
        }
    );
}

void set_flight_smart_plan_mode(
    FlightSmartPlanModeRequest request,
    const std::function<void(const FlightSmartPlanModeApiResponse& response, FlightHubError err)>& callback
)
{
    // Get configs;
    auto config = FlightHubSdk::getConfig();
    auto api = std::string(kFlightSafeConfigAPI) + "?sn=" + request.sn;

    std::string url = generate_url(config.address, config.port, api);
    LOG_INFO("set_flight_smart_plan_mode(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request.settings, body, &err);   // Body 平铺：只序列化 settings，sn 走 Query
    if (!err.empty()) {
        LOG_ERROR("set_flight_smart_plan_mode(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("set_flight_smart_plan_mode(): { url:%s, x-request-id(uuid):%s, body length: %zu, body: %s", 
             url.c_str(), uuid.str().c_str(), body.length(), log_safe(body).c_str());

    // Send HTTP PUT request;
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("set_flight_smart_plan_mode(): response received, { err_code: %d, response length: %zu, response: %s }",
            (int)err_code, response.length(), response.empty() ? "(empty)" : log_safe(response).c_str()
        );

        do {
            // 先解析外层通用 CommonResponse;
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("set_flight_smart_plan_mode(): { code: %d, message: %s }", 
                         common_response.code, common_response.message.c_str());
                break;
            }

            // 再将 data 字段反序列化为 FlightSafeConfigResponse;
            // 新接口在 PUT 成功时可能返回 "data":null（无返回体），此时跳过解析，返回默认 result
            FlightSmartPlanModeResponse flight_safe_data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, flight_safe_data, &parse_err, false)) {
                LOG_ERROR("set_flight_smart_plan_mode(): JsonToObject error: %s, data: %s",
                             parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }

            LOG_INFO("set_flight_smart_plan_mode(): success, { code: %d, message: %s }", 
                     common_response.code, common_response.message.c_str());

            FlightSmartPlanModeApiResponse api_response;
            api_response.code = common_response.code;
            api_response.message = common_response.message;
            api_response.data = flight_safe_data;

            callback(api_response, FlightHubError::success());

            // Must return here, otherwise the callback will be called twice.
            return;
        } while (0);

        // Indicate failure;
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void set_flight_out_of_control_action(
    FlightOutOfControlActionRequest request,
    const std::function<void(const FlightOutOfControlActionApiResponse& response, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, std::string(kFlightSafeConfigAPI) + "?sn=" + request.sn);
    LOG_INFO("set_flight_out_of_control_action(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request.settings, body, &err);   // Body 平铺：只序列化 settings，sn 走 Query
    if (!err.empty()) {
        LOG_ERROR("set_flight_out_of_control_action(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("set_flight_out_of_control_action(): { url:%s, x-request-id:%s, body: %s }",
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("set_flight_out_of_control_action(): response: %s", response.empty() ? "(empty)" : log_safe(response).c_str());
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("set_flight_out_of_control_action(): code:%d, msg:%s",
                         common_response.code, common_response.message.c_str());
                break;
            }
            // 新接口 PUT 成功时可能返回 "data":null，此时跳过解析，返回默认 result
            FlightOutOfControlActionResponse data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, false)) {
                LOG_ERROR("set_flight_out_of_control_action(): JsonToObject error: %s", parse_err.c_str());
                break;
            }
            FlightOutOfControlActionApiResponse api_response;
            api_response.code = common_response.code;
            api_response.message = common_response.message;
            api_response.data = data;
            callback(api_response, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void set_flight_task_height(
    FlightCommanderHeightRequest request,
    const std::function<void(const FlightTaskHeightApiResponse& response, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, std::string(kFlightSafeConfigAPI) + "?sn=" + request.sn);
    LOG_INFO("set_flight_task_height(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request.settings, body, &err);   // Body 平铺：只序列化 settings，sn 走 Query
    if (!err.empty()) {
        LOG_ERROR("set_flight_task_height(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("set_flight_task_height(): { url:%s, x-request-id:%s, body: %s }",
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("set_flight_task_height(): response: %s", response.empty() ? "(empty)" : log_safe(response).c_str());
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("set_flight_task_height(): code:%d, msg:%s",
                         common_response.code, common_response.message.c_str());
                break;
            }
            // 新接口 PUT 成功时可能返回 "data":null，此时跳过解析，返回默认 result
            FlightTaskHeightResponse data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, false)) {
                LOG_ERROR("set_flight_task_height(): JsonToObject error: %s", parse_err.c_str());
                break;
            }
            FlightTaskHeightApiResponse api_response;
            api_response.code = common_response.code;
            api_response.message = common_response.message;
            api_response.data = data;
            callback(api_response, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void set_rth_altitude(
    FlightRthAltitudeRequest request,
    const std::function<void(const FlightRthAltitudeApiResponse& response, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, std::string(kFlightSafeConfigAPI) + "?sn=" + request.sn);
    LOG_INFO("set_rth_altitude(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request.settings, body, &err);   // Body 平铺：只序列化 settings，sn 走 Query
    if (!err.empty()) {
        LOG_ERROR("set_rth_altitude(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("set_rth_altitude(): { url:%s, x-request-id:%s, body: %s }",
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("set_rth_altitude(): response: %s", response.empty() ? "(empty)" : log_safe(response).c_str());
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("set_rth_altitude(): code:%d, msg:%s",
                         common_response.code, common_response.message.c_str());
                break;
            }
            // 新接口 PUT 成功时可能返回 "data":null，此时跳过解析，返回默认 result
            FlightRthAltitudeResponse data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, false)) {
                LOG_ERROR("set_rth_altitude(): JsonToObject error: %s", parse_err.c_str());
                break;
            }
            FlightRthAltitudeApiResponse api_response;
            api_response.code = common_response.code;
            api_response.message = common_response.message;
            api_response.data = data;
            callback(api_response, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

void set_air_transfer_enable(
    AirTransferEnableRequest request,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, std::string(kAirTransferEnableAPI));
    LOG_INFO("set_air_transfer_enable(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("set_air_transfer_enable(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("set_air_transfer_enable(): { url:%s, x-request-id:%s, body: %s }",
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("set_air_transfer_enable(): response: %s", response.empty() ? "(empty)" : log_safe(response).c_str());
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("set_air_transfer_enable(): code:%d, msg:%s",
                         common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (0);
        callback(make_http_error(http_code, response));
    }, 15000);
}

void update_fly_to_points(
    UpdateFlyToPointsRequest request,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, std::string(kUpdateFlyToPointsAPI));
    LOG_INFO("update_fly_to_points(): request url: %s", url.c_str());
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("update_fly_to_points(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("update_fly_to_points(): { url:%s, x-request-id:%s, body: %s }",
             url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("update_fly_to_points(): response: %s", response.empty() ? "(empty)" : log_safe(response).c_str());
        auto common_response = convert_response_struct(response);
        if (common_response.code != 0) {
            LOG_ERROR("update_fly_to_points(): code:%d, msg:%s",
                     common_response.code, common_response.message.c_str());
            callback(make_http_error(http_code, response));
            return;
        }
        // data 为 null，code=0 即成功
        LOG_INFO("update_fly_to_points(): success");
        callback(FlightHubError::success());
    }, 15000);
}

// === Terra Build GET 接口实现 ===

// 1. 获取重建设置
void get_reconstruction_settings(
    const std::string& file_uuid,
    const std::function<void(const ReconstructionSettingsResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildReconstructionSettingsAPI);
    api.replace(api.find("{file_uuid}"), 11, file_uuid);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("get_reconstruction_settings(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_reconstruction_settings(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            ReconstructionSettingsResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("get_reconstruction_settings(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 2. 获取质量报告
void get_quality_report(
    const std::string& file_uuid,
    const GetQualityReportRequest& request,
    const std::function<void(const QualityReportResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildQualityReportAPI);
    api.replace(api.find("{file_uuid}"), 11, file_uuid);
    std::string query = "language=" + request.language +
                        "&measure_unit_system=" + request.measure_unit_system;
    std::string url = generate_url(config.address, config.port, api + "?" + query);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("get_quality_report(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_quality_report(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            QualityReportResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("get_quality_report(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 3. 获取模型文件列表
void get_model_file_list(
    const GetModelFileListRequest& request,
    const std::function<void(const ModelFileListResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildModelFileListAPI);
    std::string query = "page=" + std::to_string(request.page) +
                        "&page_size=" + std::to_string(request.page_size);
    std::string url = generate_url(config.address, config.port, api + "?" + query);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("get_model_file_list(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_model_file_list(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            ModelFileListResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("get_model_file_list(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 4. 获取模型文件签名 URL
void get_model_file_url(
    int file_id,
    const GetModelFileUrlRequest& request,
    const std::function<void(const ModelFileUrlResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildModelFileUrlAPI);
    api.replace(api.find("{file_id}"), 9, std::to_string(file_id));
    std::string query = "type=" + std::to_string(request.type);
    std::string url = generate_url(config.address, config.port, api + "?" + query);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("get_model_file_url(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_model_file_url(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            ModelFileUrlResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("get_model_file_url(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// === Terra Build POST/DELETE/GET 扩展实现 ===

// 5. 创建重建任务
void create_reconstruction(
    const std::string& body,
    const std::function<void(const CreateReconstructionResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildCreateReconstructionAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_DEBUG("create_reconstruction(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("create_reconstruction(): network error %d", static_cast<int>(err_code));
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("create_reconstruction(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            CreateReconstructionResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("create_reconstruction(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 6. 重试重建任务
void retry_reconstruction(
    const RetryReconstructionRequest& request,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildRetryReconstructionAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;
    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("retry_reconstruction(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("retry_reconstruction(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("retry_reconstruction(): network error %d", static_cast<int>(err_code));
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("retry_reconstruction(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (0);
        callback(make_http_error(http_code, response));
    }, 15000);
}

// 7. 停止重建任务
void stop_reconstruction(
    const StopReconstructionRequest& request,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildStopReconstructionAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;
    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("stop_reconstruction(): ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("stop_reconstruction(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("stop_reconstruction(): network error %d", static_cast<int>(err_code));
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("stop_reconstruction(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (0);
        callback(make_http_error(http_code, response));
    }, 15000);
}

// 8. 获取空三结果
void get_sfm_result(
    const std::string& file_uuid,
    const std::function<void(const SfmResultResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildSfmResultAPI);
    api.replace(api.find("{file_uuid}"), 11, file_uuid);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("get_sfm_result(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_sfm_result(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            SfmResultResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("get_sfm_result(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 9. 删除单个模型文件
void delete_model_file(
    int file_id,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildDeleteModelFileAPI);
    api.replace(api.find("{file_id}"), 9, std::to_string(file_id));
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("delete_model_file(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("delete_model_file(): network error %d", static_cast<int>(err_code));
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("delete_model_file(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (0);
        callback(make_http_error(http_code, response));
    }, 15000);
}

// 10. 批量删除模型文件
void batch_delete_model_files(
    const DeleteModelFilesRequest& request,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildBatchDeleteModelFilesAPI);

    std::string ids_str;
    for (size_t i = 0; i < request.file_ids.size(); ++i) {
        if (i > 0) ids_str += ",";
        ids_str += std::to_string(request.file_ids[i]);
    }
    std::string query = "ids=" + ids_str;
    std::string url = generate_url(config.address, config.port, api + "?" + query);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("batch_delete_model_files(): url: %s, uuid: %s, ids: %s", url.c_str(), uuid.str().c_str(), ids_str.c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("batch_delete_model_files(): network error %d", static_cast<int>(err_code));
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("batch_delete_model_files(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (0);
        callback(make_http_error(http_code, response));
    }, 15000);
}

// 11. 创建全景拼接任务
void create_panorama(
    const CreatePanoramaRequest& request,
    const std::function<void(const CreatePanoramaResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildCreatePanoramaAPI);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;
    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("create_panorama(): ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("create_panorama(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("create_panorama(): network error %d", static_cast<int>(err_code));
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("create_panorama(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            CreatePanoramaResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("create_panorama(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 12. 获取重建图像列表
void get_reconstruction_images(
    const std::string& file_uuid,
    const std::function<void(const ReconstructionImagesResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraBuildReconstructionImagesAPI);
    api.replace(api.find("{file_uuid}"), 11, file_uuid);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("get_reconstruction_images(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_reconstruction_images(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            ReconstructionImagesResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("get_reconstruction_images(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 获取媒体上传 STS 凭证（POST /pf-media/inner/v1/media-files/sts）
void get_media_upload_sts(
    const GetMediaUploadStsRequest& request,
    const std::function<void(const GetMediaUploadStsResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kMediaUploadStsAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;
    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("get_media_upload_sts(): ObjectToJson error: %s", err.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("get_media_upload_sts(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_media_upload_sts(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            GetMediaUploadStsResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("get_media_upload_sts(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 媒体上传完成回调（POST /pf-media/api/v1/upload-callback）
void media_upload_callback(
    const MediaUploadCallbackRequest& request,
    const std::function<void(const MediaUploadCallbackResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kMediaUploadCallbackAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;
    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("media_upload_callback(): ObjectToJson error: %s", err.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("media_upload_callback(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("media_upload_callback(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            MediaUploadCallbackResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("media_upload_callback(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}


// 设备定损: 下发定损指令;
void post_self_diagnosis(CreateSelfDiagnosisRequest request,
    const std::function<void(FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("post_self_diagnosis: callback is null");
        return;
    }

    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSelfDiagnosticAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_self_diagnosis: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // Send HTTP request;
    LOG_DEBUG("post_self_diagnosis: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("post_self_diagnosis: network error: %d, http_code: %d", static_cast<int>(err_code), http_code);
            callback(make_http_error(http_code, response));
            return;
        }
        LOG_DEBUG("post_self_diagnosis: response %s", log_safe(response).c_str());
        auto err_info = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_self_diagnosis: code: %d, message: %s", common_response.code, common_response.message.c_str());
                err_info = make_http_error(http_code, response);
                break;
            }
        } while (0);

        callback(err_info);
    });
}


// 定损操作(取消 / 删除);
void post_self_diagnosis_operation(SelfDiagnosisOperationRequest request,
    const std::function<void(FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("post_self_diagnosis_operation: callback is null");
        return;
    }

    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kSelfDiagnosticOperationAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_self_diagnosis_operation: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("post_self_diagnosis_operation: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("post_self_diagnosis_operation: network error: %d, http_code: %d", static_cast<int>(err_code), http_code);
            callback(make_http_error(http_code, response));
            return;
        }
        LOG_DEBUG("post_self_diagnosis_operation: response %s", log_safe(response).c_str());
        auto err_info = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_self_diagnosis_operation: code: %d, message: %s", common_response.code, common_response.message.c_str());
                err_info = make_http_error(http_code, response);
                break;
            }
        } while (0);
        callback(err_info);
    });
}


// 设备定损: 查询单条定损报告详情;
void get_self_diagnosis_report(const std::string &report_uuid,
    const std::function<void(const GetSelfDiagnosisReportResponse& response, FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("get_self_diagnosis_report: callback is null");
        return;
    }

    auto config = FlightHubSdk::getConfig();
    std::string api = kSelfDiagnosticReportAPI;
    auto pos = api.find("{report_uuid}");
    if (pos == std::string::npos) {
        LOG_ERROR("get_self_diagnosis_report: API template missing {report_uuid}");
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    api.replace(pos, 13, report_uuid);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_self_diagnosis_report: url %s, uuid %s", url.c_str(), uuid.str().c_str());

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("get_self_diagnosis_report: network error: %d, http_code: %d", static_cast<int>(err_code), http_code);
            callback({}, make_http_error(http_code, response));
            return;
        }
        GetSelfDiagnosisReportResponse report_response;
        auto error_info = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_self_diagnosis_report: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_info = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, report_response, &err, true)) {
                LOG_ERROR("get_self_diagnosis_report: JsonToObject error: %s, data: %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_info = make_http_error(http_code, response);
                break;
            }
        } while (0);
        callback(report_response, error_info);
    });
}


// 设备定损: 查询定损报告列表;
// GET /device/api/v1/manager/self-diagnostic?
// page=1&page_size=20&
// status=ok&status=in_progress&status=timeout&
// device_sn=SNSN&
// report_time_begin=1000000&report_time_end=8000000&part=1&origin=1&query=ZZZ&report_user=1111&report_user=2222&report_user=3333&
// sort_by=created_at&sort_type=desc
void get_self_diagnosis_report_lists(const GetSelfDiagnosisReportListParams &params,
    const std::function<void(const GetSelfDiagnosisReportListResponse& response, FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("get_self_diagnosis_report_lists: callback is null");
        return;
    }

    auto config = FlightHubSdk::getConfig();

    std::string param_str;
    // page=1&page_size=20&;
    if (params.page >= 1 && params.page_size >= 1) {
        param_str += ("page=" + std::to_string(params.page) + "&");
        param_str += ("page_size=" + std::to_string(params.page_size) + "&");
    }

    if (!params.device_sn.empty()) {
        param_str += ("device_sn=" + params.device_sn + "&");
    }
    if (!params.status.empty()) {
        std::string temp;
        for (const auto &s : params.status) {
            temp += ("status=" + s + "&");
        }

        param_str += temp;
    }
    if (!params.report_time_begin.empty()) {
        param_str += ("report_time_begin=" + params.report_time_begin + "&");
    }
    if (!params.report_time_end.empty()) {
        param_str += ("report_time_end=" + params.report_time_end + "&");
    }

    param_str += ("part=" + std::to_string(params.part) + "&");
    param_str += ("origin=" + std::to_string(params.origin) + "&");

    if (!params.query.empty()) {
        param_str += ("query=" + params.query + "&");
    }
    if (!params.report_user.empty()) {
        std::string temp;
        for (const auto &u : params.report_user) {
            temp += ("report_user=" + u + "&");
        }

        param_str += temp;
    }
    if (!params.sort_by.empty()) {
        param_str += ("sort_by=" + params.sort_by + "&");
    }
    if (!params.sort_type.empty()) {
        param_str += ("sort_type=" + params.sort_type + "&");
    }

    // 去尾;
    if (!param_str.empty() && (param_str.back() == '&')) {
        param_str.pop_back();
    }

    std::string complete_api = std::string(kSelfDiagnosticReportListAPI);
    if (!param_str.empty()) {
        complete_api += ("?" + param_str);
    }

    std::string url = generate_url(config.address, config.port, complete_api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_self_diagnosis_report_lists(): { url: %s, uuid: %s }", url.c_str(), uuid.str().c_str());

    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("get_self_diagnosis_report_lists: network error: %d, http_code: %d", static_cast<int>(err_code), http_code);
            callback({}, make_http_error(http_code, response));
            return;
        }
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_self_diagnosis_report_lists(): { code: %d, message: %s, http_response: %s }",
                    common_response.code, common_response.message.c_str(), log_safe(response).c_str());
                break;
            }

            GetSelfDiagnosisReportListResponse http_resp;
            std::string err;
            if (!parse_common_response_data(common_response, http_resp, &err, true)) {
                LOG_ERROR("get_self_diagnosis_report_lists(): JsonToObject error: %s, data: %s",
                    err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }

            callback(http_resp, FlightHubError::success());
            return;
        } while (0);

        callback({}, make_http_error(http_code, response));
    });
}

// ============================================================
// Geo Caging 接口实现
// ============================================================

void post_set_geo_caging_enable(const std::string& device_sn, int geo_caging_switch, const std::vector<GeoCagingInfo>& geo_caging_infos, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kGeoCagingEnableAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    // 组装 state 数组：每个围栏信息一个 {desired:{geo_caging_info:{...}}}，最后追加开关 {desired:{geo_caging_switch:{state:N}}}
    std::vector<GeoCagingStateInfoItem> info_items;
    for (const auto& info : geo_caging_infos) {
        GeoCagingStateInfoItem item;
        item.desired.geo_caging_info = info;
        info_items.push_back(item);
    }

    // 将围栏 items 序列化为 JSON 数组字符串
    std::string info_items_json, err_str;
    aigc::JsonHelper::ObjectToJson(info_items, info_items_json, &err_str);
    if (!err_str.empty()) {
        LOG_ERROR("post_set_geo_caging_enable: info items ObjectToJson error: %s", err_str.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // 序列化开关 item
    GeoCagingStateSwitchItem switch_item;
    switch_item.desired.geo_caging_switch.state = geo_caging_switch;
    std::string switch_item_json;
    aigc::JsonHelper::ObjectToJson(switch_item, switch_item_json, &err_str);
    if (!err_str.empty()) {
        LOG_ERROR("post_set_geo_caging_enable: switch item ObjectToJson error: %s", err_str.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    // 手动拼接 body: {"device_sn":"...","state":[...info_items..., switch_item]}
    // 去掉 info_items_json 的尾部 ']'，追加 switch_item，再闭合 ']'
    std::string state_array;
    if (info_items.empty()) {
        state_array = "[" + switch_item_json + "]";
    } else {
        // info_items_json 形如 "[{...},{...}]"，去掉末尾 ']'
        state_array = info_items_json.substr(0, info_items_json.size() - 1) + "," + switch_item_json + "]";
    }

    std::string body = "{\"device_sn\":\"" + device_sn + "\",\"state\":" + state_array + "}";

    LOG_DEBUG("post_set_geo_caging_enable: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_set_geo_caging_enable] http response: %s", log_safe(response).c_str());
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_set_geo_caging_enable: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (false);
        callback(make_http_error(http_code, response));
    });
}

// ============================================================
// 中继站调试接口实现
// ============================================================

void post_relay_broadcast_hide(const std::string& device_sn, bool is_broadcast_hide, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRelayBroadcastHideAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SetRelayBroadcastHideRequest request;
    request.device_sn = device_sn;
    request.is_broadcast_hide = is_broadcast_hide;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_relay_broadcast_hide: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_relay_broadcast_hide: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_relay_broadcast_hide] http response: %s", log_safe(response).c_str());
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_relay_broadcast_hide: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (false);
        callback(make_http_error(http_code, response));
    });
}

void post_relay_ssid(const std::string& device_sn, const std::string& device_name, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRelaySSIDAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SetRelaySSIDRequest request;
    request.device_sn = device_sn;
    request.device_name = device_name;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_relay_ssid: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_relay_ssid: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_relay_ssid] http response: %s", log_safe(response).c_str());
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_relay_ssid: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (false);
        callback(make_http_error(http_code, response));
    });
}

void post_relay_password(const std::string& device_sn, const std::string& passwd, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRelayPasswordAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SetRelayPasswordRequest request;
    request.device_sn = device_sn;
    request.passwd = passwd;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_relay_password: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    // P1-02: 密码类接口, 请求体含明文密码, 日志不落 body, 仅记录 url/uuid
    LOG_INFO("post_relay_password: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        // P1-02: 密码类接口响应脱敏, 仅记录解析后的 code/message, 不落原始响应体
        auto common_response = (err_code == drc_sdk::common::NetworkError::SUCCESS)
                                   ? convert_response_struct(response)
                                   : CommonResponse();
        LOG_INFO("[post_relay_password] http response: http_code=%d, code=%d, message: %s",
                 http_code, common_response.code, common_response.message.c_str());
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            if (common_response.code != 0) {
                LOG_ERROR("post_relay_password: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (false);
        callback(make_http_error(http_code, response));
    });
}

void post_relay_search_mode(const std::string& device_sn, int relay_search_mode, const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRelaySearchModeAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SetRelaySearchModeRequest request;
    request.device_sn = device_sn;
    request.relay_search_mode = relay_search_mode;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_relay_search_mode: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_relay_search_mode: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_relay_search_mode] http response: %s", log_safe(response).c_str());
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_relay_search_mode: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (false);
        callback(make_http_error(http_code, response));
    });
}

// 有 data 响应的 3 个接口：callback 带 Response 结构体
void post_relay_occupied_status(const std::string& device_sn, int occupied_status, const std::function<void(const SetRelayOccupiedStatusResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRelayOccupiedStatusAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    SetRelayOccupiedStatusRequest request;
    request.device_sn = device_sn;
    request.occupied_status = occupied_status;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_relay_occupied_status: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_relay_occupied_status: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_relay_occupied_status] http response: %s", log_safe(response).c_str());
        SetRelayOccupiedStatusResponse resp;
        auto error_code = FlightHubError::success();
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_relay_occupied_status: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                resp.status = common_response.message;
                break;
            }
            std::string parse_err;
            if (!parse_common_response_data(common_response, resp, &parse_err, false)) {
                LOG_ERROR("post_relay_occupied_status: JsonToObject error: %s, %s", parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                resp.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(resp, error_code);
        }
    });
}

void post_relay_reboot(const std::string& device_sn, const std::function<void(const RelayRebootResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRelayRebootAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    RelayRebootRequest request;
    request.device_sn = device_sn;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_relay_reboot: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_relay_reboot: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_relay_reboot] http response: %s", log_safe(response).c_str());
        RelayRebootResponse resp;
        auto error_code = FlightHubError::success();
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_relay_reboot: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                resp.status = common_response.message;
                break;
            }
            std::string parse_err;
            if (!parse_common_response_data(common_response, resp, &parse_err, false)) {
                LOG_ERROR("post_relay_reboot: JsonToObject error: %s, %s", parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                resp.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(resp, error_code);
        }
    });
}

void post_relay_disconnect(const std::string& device_sn, const std::function<void(const RelayDisconnectResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kRelayDisconnectAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    RelayDisconnectRequest request;
    request.device_sn = device_sn;

    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("post_relay_disconnect: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("post_relay_disconnect: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[post_relay_disconnect] http response: %s", log_safe(response).c_str());
        RelayDisconnectResponse resp;
        auto error_code = FlightHubError::success();
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_relay_disconnect: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                resp.status = common_response.message;
                break;
            }
            std::string parse_err;
            if (!parse_common_response_data(common_response, resp, &parse_err, false)) {
                LOG_ERROR("post_relay_disconnect: JsonToObject error: %s, %s", parse_err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                resp.status = common_response.message;
                break;
            }
        } while (false);
        if (callback) {
            callback(resp, error_code);
        }
    });
}

/// AI Box: 获取 AI Box 信息;
void fetch_ai_box_info(const std::string& device_sn, const std::function<void(const AIBoxInfoListResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxInfoAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    CommonRequestWithDeviceSn request;
    request.device_sn = device_sn;

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("fetch_ai_box_info: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("fetch_ai_box_info: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[fetch_ai_box_info] http response: %s", log_safe(response).c_str());
        AIBoxInfoListResponse ai_box_info_response;
        auto error_info = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("fetch_ai_box_info: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_info = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, ai_box_info_response, &err, true)) {
                LOG_ERROR("fetch_ai_box_info: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_info = make_http_error(http_code, response);
                break;
            }
        } while (0);
        if (callback) {
            callback(ai_box_info_response, error_info);
        }
    });
}


/// AI Box: 获取文件下载 URL;
void fetch_file_download_url(FetchFileDownloadUrlRequest request, const std::function<void(const FileDownloadUrlListResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxFileDownloadURLAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("fetch_file_download_url: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("fetch_file_download_url: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[fetch_file_download_url] http response: %s", log_safe(response).c_str());
        FileDownloadUrlListResponse file_download_url_response;
        auto error_info = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("fetch_file_download_url: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_info = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, file_download_url_response, &err, true)) {
                LOG_ERROR("fetch_file_download_url: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_info = make_http_error(http_code, response);
                break;
            }
        } while (0);
        if (callback) {
            callback(file_download_url_response, error_info);
        }
    });
}


///  AI Box: 获取应用列表;
void fetch_app_list(FetchAppListRequest request, const std::function<void(const FetchAppListResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxAppListAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("fetch_app_list: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("fetch_app_list: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[fetch_app_list] http response: %s", log_safe(response).c_str());
        FetchAppListResponse app_list_response;
        auto error_info = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("fetch_app_list: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_info = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, app_list_response, &err, true)) {
                LOG_ERROR("fetch_app_list: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_info = make_http_error(http_code, response);
                break;
            }
        } while (0);
        if (callback) {
            callback(app_list_response, error_info);
        }
    });
}

void fetch_app_list_progress(FetchAppListProgressRequest request, const std::function<void(const FetchAppListProgressResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxAppListProgressAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("fetch_app_list_progress: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("fetch_app_list_progress: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[fetch_app_list_progress] http response: %s", log_safe(response).c_str());
        FetchAppListProgressResponse app_list_progress_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("fetch_app_list_progress: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, app_list_progress_response, &err, true)) {
                LOG_ERROR("fetch_app_list_progress: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(app_list_progress_response, error_code);
        }
    });
}

void export_file(ExportFileRequest request, const std::function<void(const ExportFileResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxExportFileAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("export_file: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("export_file: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[export_file] http response: %s", log_safe(response).c_str());
        ExportFileResponse export_file_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("export_file: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, export_file_response, &err, false)) {
                LOG_ERROR("export_file: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(export_file_response, error_code);
        }
    });
}

void export_file_progress(ExportFileProgressRequest request, const std::function<void(const ExportFileProgressResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxExportFileProgressAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("export_file_progress: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("export_file_progress: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[export_file_progress] http response: %s", log_safe(response).c_str());
        ExportFileProgressResponse export_progress_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("export_file_progress: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, export_progress_response, &err, true)) {
                LOG_ERROR("export_file_progress: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(export_progress_response, error_code);
        }
    });
}

void install_app(InstallAppRequest request, const std::function<void(const InstallAppResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxInstallAppAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("install_app: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("install_app: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[install_app] http response: %s", log_safe(response).c_str());
        InstallAppResponse install_app_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("install_app: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, install_app_response, &err, false)) {
                LOG_ERROR("install_app: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(install_app_response, error_code);
        }
    });
}

void install_app_progress(InstallAppProgressRequest request, const std::function<void(const InstallAppProgressResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxInstallAppProgressAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("install_app_progress: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("install_app_progress: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[install_app_progress] http response: %s", log_safe(response).c_str());
        InstallAppProgressResponse install_progress_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("install_app_progress: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, install_progress_response, &err, true)) {
                LOG_ERROR("install_app_progress: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(install_progress_response, error_code);
        }
    });
}

void operate_app(OperateAppRequest request, const std::function<void(const OperateAppResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAIBoxOperateAppAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("operate_app: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    LOG_DEBUG("operate_app: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        LOG_DEBUG("[operate_app] http response: %s", log_safe(response).c_str());
        OperateAppResponse operate_app_response;
        auto error_code = FlightHubError::success();
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("operate_app: code: %d, message: %s", common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
                break;
            }

            std::string err;
            if (!parse_common_response_data(common_response, operate_app_response, &err, false)) {
                LOG_ERROR("operate_app: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                error_code = make_http_error(http_code, response);
                break;
            }
        } while (false);
        if (callback) {
            callback(operate_app_response, error_code);
        }
    });
}


// GET /service/aibox/upload-app-sts?device_sn=<device_sn>&box_sn=<box_sn>
void get_upload_app_sts(const std::string &device_sn, const std::string &box_sn, const std::function<void(const GetUploadStsResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kAIBoxUploadAppStsAPI;

    std::string param_str;
    if (!device_sn.empty() && !box_sn.empty()) {
        param_str += std::string("device_sn=") + device_sn + "&";
        param_str += std::string("box_sn=") + box_sn + "&";
    }

    // 去尾 &;
    if (!param_str.empty() && (param_str.back() == '&')) {
        param_str.pop_back();
    }

    if (!param_str.empty()) {
        api += ("?" + param_str);
    }

    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("get_upload_app_sts: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response, const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
        do {
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("get_upload_app_sts: code: %d, message: %s", common_response.code, common_response.message.c_str());
                break;
            }

            GetUploadStsResponse sts_response;
            std::string err;
            if (!parse_common_response_data(common_response, sts_response, &err, true)) {
                LOG_ERROR("get_upload_app_sts: JsonToObject error: %s, %s", err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                break;
            }

            callback(sts_response, FlightHubError::success());
            return;
        } while (0);

        callback({}, make_http_error(http_code, response));
    });
}

void create_in_flight_wayline_task(
    CreateInFlightWaylineTaskRequest request,
    const std::function<void(const CreateInFlightWaylineTaskResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kCreateInFlightWaylineTaskAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("CreateInFlightWaylineTask: ObjectToJson error: %s", err.c_str());
        callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("CreateInFlightWaylineTask: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("CreateInFlightWaylineTask response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("CreateInFlightWaylineTask: code: %d, message: %s", common_response.code,
                          common_response.message.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }

            CreateInFlightWaylineTaskResponse result{};
            std::string parse_err;
            if (!parse_common_response_data(common_response, result, &parse_err, true)) {
                    LOG_ERROR("CreateInFlightWaylineTask: JsonToObject error: %s, %s", parse_err.c_str(),
                              common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                callback({}, make_http_error(http_code, response));
                return;
            }
            callback(result, FlightHubError::success());
        });
}

void control_in_flight_wayline_task(
    ControlInFlightWaylineTaskRequest request,
    const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kControlInFlightWaylineTaskAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();

    std::string body;
    std::string err;
    aigc::JsonHelper::ObjectToJson(request, body, &err);
    if (!err.empty()) {
        LOG_ERROR("ControlInFlightWaylineTask: ObjectToJson error: %s", err.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("ControlInFlightWaylineTask: url %s, uuid %s, body %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("ControlInFlightWaylineTask response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("ControlInFlightWaylineTask: code: %d, message: %s", common_response.code,
                          common_response.message.c_str());
                callback(make_http_error(http_code, response));
                return;
            }
            callback(FlightHubError::success());
        });
}

void get_active_in_flight_wayline_task(
    const std::string& drone_sn,
    const std::function<void(const GetActiveInFlightWaylineTaskResponse& response, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kGetActiveInFlightWaylineTaskAPI);
    url += (url.find('?') == std::string::npos ? "?" : "&");
    url += "drone_sn=" + drone_sn;
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("GetActiveInFlightWaylineTask: url %s, uuid %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError err_code, int http_code, const std::string& response,
                   const std::vector<std::string>&, const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("GetActiveInFlightWaylineTask response: %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("GetActiveInFlightWaylineTask: code: %d, message: %s", common_response.code,
                          common_response.message.c_str());
                callback({}, make_http_error(http_code, response));
                return;
            }

            GetActiveInFlightWaylineTaskResponse result{};
            std::string parse_err;
            if (!parse_common_response_data(common_response, result, &parse_err, false)) {
                    LOG_ERROR("GetActiveInFlightWaylineTask: JsonToObject error: %s, %s", parse_err.c_str(),
                              common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
                callback({}, make_http_error(http_code, response));
                return;
            }
            callback(result, FlightHubError::success());
        });
}
// === Terra 模型管理新增接口实现 ===
// 模型文件上传回调（POST /pf-terra/api/v1/models/files/upload-callback）
void terra_upload_callback(
    const TerraUploadCallbackRequest& request,
    const std::function<void(const TerraUploadCallbackResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kTerraUploadCallbackAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;

    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("terra_upload_callback(): ObjectToJson error: %s", err.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("terra_upload_callback(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("%s: network error: %d, uuid: %s, response: %s",
                          __func__, static_cast<int>(err_code), uuid_str.c_str(), log_safe(response).c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("terra_upload_callback(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            TerraUploadCallbackResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("terra_upload_callback(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 模型重命名（PUT /model/api/v1/files/{id}）
void terra_rename_model(
    int file_id,
    const TerraRenameModelRequest& request,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraRenameModelAPI);
    api.replace(api.find("{id}"), 4, std::to_string(file_id));
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;

    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("terra_rename_model(): ObjectToJson error: %s", err.c_str());
        if (callback) callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("terra_rename_model(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("terra_rename_model(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("terra_rename_model(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (0);
        callback(make_http_error(http_code, response));
    }, 15000);
}

// 批量加载/取消加载模型到地图（PUT /model/api/v1/files）
void terra_show_on_map(
    const TerraShowOnMapRequest& request,
    const std::function<void(const TerraShowOnMapResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kTerraShowOnMapAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    std::string body;
    std::string err;

    auto request_copy = request;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("terra_show_on_map(): ObjectToJson error: %s", err.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("terra_show_on_map(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("terra_show_on_map(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("terra_show_on_map(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            TerraShowOnMapResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("terra_show_on_map(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 获取模型文件详情（GET /pf-terra/api/v1/models/files/{file_id}）
void terra_get_model_detail(
    int file_id,
    const std::function<void(const TerraModelDetailResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kTerraGetModelDetailAPI);
    api.replace(api.find("{file_id}"), 9, std::to_string(file_id));
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("terra_get_model_detail(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("terra_get_model_detail(): network error %d, uuid: %s",
                          static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("terra_get_model_detail(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            TerraModelDetailResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_response, data, &parse_err, true)) {
                LOG_ERROR("terra_get_model_detail(): JsonToObject error: %s, uuid:%s",
                          parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 设置 LAS 模型坐标系并触发格式转换（POST /pf-terra/api/v1/models/converters）
void terra_set_las_wkt(
    const std::string& file_uuid,
    const std::string& wkt,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kTerraSetLasWktAPI);
    auto uuid_req = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid_req.str();
    headers["content-type"]  = "application/json";

    TerraSetLasWktRequest request;
    request.file_uuid = file_uuid;
    request.wkt       = wkt;
    std::string body;
    std::string err_str;
    aigc::JsonHelper::ObjectToJson(request, body, &err_str);
    if (!err_str.empty()) {
        LOG_ERROR("terra_set_las_wkt(): ObjectToJson error: %s", err_str.c_str());
        if (callback) callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_INFO("terra_set_las_wkt(): url: %s, uuid: %s, file_uuid: %s",
             url.c_str(), uuid_req.str().c_str(), file_uuid.c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid_req.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("terra_set_las_wkt(): network error %d, uuid: %s",
                          static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("terra_set_las_wkt(): code:%d, msg:%s, uuid:%s",
                         common_response.code, common_response.message.c_str(), uuid_str.c_str());
                break;
            }
            callback(FlightHubError::success());
            return;
        } while (0);
        callback(make_http_error(http_code, response));
    }, 15000);
}

// ============================================================
// 元素管理（ElementService）HTTP 接口实现
// ============================================================

// 将 AIGC 序列化后的 JSON 中 "key":"[...]" 恢复为 "key":[...]（unquote raw JSON 数组字段）
// 若字段值为空字符串，则替换为空数组 []
static void unquoteJsonArrayField(std::string& json, const std::string& key) {
    std::string pattern = "\"" + key + "\":\"";
    size_t pos = 0;
    while ((pos = json.find(pattern, pos)) != std::string::npos) {
        size_t val_start = pos + pattern.size();
        // 找到结尾引号（需匹配转义）
        size_t val_end = val_start;
        while (val_end < json.size()) {
            if (json[val_end] == '"' && (val_end == 0 || json[val_end - 1] != '\\')) break;
            val_end++;
        }
        if (val_end >= json.size()) break;
        // 提取引号内的内容
        std::string value = json.substr(val_start, val_end - val_start);
        if (value.empty()) {
            // 空字符串 → 替换为空数组 []
            // 替换区域: 从开引号(val_start-1) 到闭引号(val_end)，共 val_end - val_start + 2 字符
            json.replace(val_start - 1, val_end - val_start + 2, "[]");
            pos = val_start + 1;
        } else if (value.front() == '[') {
            // 被引号包裹的数组 → 去掉引号
            json.erase(val_end, 1);           // 删闭引号
            json.erase(val_start - 1, 1);     // 删开引号
            pos = val_start;
        } else {
            // 非数组内容，跳过
            pos = val_end + 1;
        }
    }
}

// 创建要素（POST /pf-map/api/v1/elements/create）
void element_create(
    const CreateElementRequest& request,
    const std::function<void(const ElementCreateResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kElementCreateAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"]  = "application/json";

    auto request_copy = request;
    std::string body, err_str;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err_str);
    if (!err_str.empty()) {
        LOG_ERROR("element_create(): ObjectToJson error: %s", err_str.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    // coordinates/bread_crumbs 为 raw JSON 透传，AIGC 序列化后被引号包裹，需 unquote
    unquoteJsonArrayField(body, "coordinates");
    unquoteJsonArrayField(body, "bread_crumbs");

    LOG_DEBUG("element_create(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("element_create(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("element_create(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            ElementCreateResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("element_create(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 更新要素（PUT /pf-map/api/v1/elements/update/{id}）
void element_update(
    const std::string& element_id,
    const UpdateElementRequest& request,
    const std::function<void(const ElementUpdateResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kElementUpdateAPI);
    api.replace(api.find("{id}"), 4, element_id);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"]  = "application/json";

    auto request_copy = request;
    std::string body, err_str;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err_str);
    if (!err_str.empty()) {
        LOG_ERROR("element_update(): ObjectToJson error: %s", err_str.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }
    // coordinates/bread_crumbs 为 raw JSON 透传，AIGC 序列化后被引号包裹，需 unquote
    unquoteJsonArrayField(body, "coordinates");
    unquoteJsonArrayField(body, "bread_crumbs");

    LOG_INFO("element_update(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("element_update(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("element_update(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            ElementUpdateResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("element_update(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 查询元素层级列表（GET /pf-map/api/v1/elements/lists）
void element_list_hierarchy(
    const ListElementHierarchyRequest& request,
    const std::function<void(const ElementListHierarchyResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    auto uuid = xg::newGuid();

    // 构建查询参数，随 URL 传入 generate_url（与现有 GET 接口范式一致）
    std::string query;
    auto append = [&](const std::string& k, const std::string& v) {
        query += (query.empty() ? "?" : "&") + k + "=" + v;
    };
    if (!request.group_id.empty()) append("group_id", request.group_id);
    if (!request.pid.empty())      append("pid", request.pid);
    if (request.is_distributed.has_value()) append("is_distributed", request.is_distributed.value() ? "true" : "false");
    if (request.type.has_value())  append("type", std::to_string(request.type.value()));
    if (request.element_uuid.has_value() && !request.element_uuid.value().empty()) append("element_uuid", request.element_uuid.value());
    if (request.exclude_copy)      append("exclude_copy", "true");
    std::string url = generate_url(config.address, config.port, kElementListHierarchyAPI + query);

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"]  = "application/json";

    LOG_INFO("element_list_hierarchy(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("element_list_hierarchy(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("element_list_hierarchy(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            ElementListHierarchyResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("element_list_hierarchy(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 设置要素显示状态（PUT /pf-map/api/v1/elements/{id}）
void element_set_visibility(
    const std::string& element_id,
    const SetElementVisibilityRequest& request,
    const std::function<void(const ElementSetVisibilityResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kElementSetVisibilityAPI);
    api.replace(api.find("{id}"), 4, element_id);
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"]  = "application/json";

    auto request_copy = request;
    std::string body, err_str;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err_str);
    if (!err_str.empty()) {
        LOG_ERROR("element_set_visibility(): ObjectToJson error: %s", err_str.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_INFO("element_set_visibility(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("element_set_visibility(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("element_set_visibility(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            ElementSetVisibilityResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("element_set_visibility(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 删除要素（DELETE /pf-map/api/v1/elements/delete/{id}）
void element_delete(
    const std::string& element_id,
    bool from_batch_delete,
    const std::function<void(const ElementDeleteResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kElementDeleteAPI);
    api.replace(api.find("{id}"), 4, element_id);
    // from_batch_delete 作为查询参数传递
    if (from_batch_delete) api += "?from_batch_delete=true";
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();

    LOG_INFO("element_delete(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("element_delete(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("element_delete(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            ElementDeleteResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("element_delete(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 上传地理标注媒体资源（POST /pf-media/api/v1/geo-media-file）
void upload_geo_media_file(
    const UploadGeoMediaFileRequest& request,
    const std::function<void(const UploadGeoMediaFileResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kUploadGeoMediaFileAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    auto request_copy = request;
    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("upload_geo_media_file(): ObjectToJson error: %s", err.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("upload_geo_media_file(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("upload_geo_media_file(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("upload_geo_media_file(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            UploadGeoMediaFileResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("upload_geo_media_file(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 根据文件 ID 获取媒体文件详情（GET /pf-media/api/v1/media-files/{file_id}）
void get_media_file_detail_by_id(
    int file_id,
    const std::function<void(const GetMediaFileDetailByIdResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string api = std::string(kGetMediaFileDetailAPI);
    api.replace(api.find("{file_id}"), 9, std::to_string(file_id));
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    LOG_INFO("get_media_file_detail_by_id(): url: %s, uuid: %s", url.c_str(), uuid.str().c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("get_media_file_detail_by_id(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("get_media_file_detail_by_id(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            GetMediaFileDetailByIdResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("get_media_file_detail_by_id(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

// 获取航线编辑器 STS 凭证（POST /gateway/pf-wayline/editor/v1/sts）
void get_wayline_editor_sts(
    const GetWaylineEditorStsRequest& request,
    const std::function<void(const GetWaylineEditorStsResponseData& data, FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kGetWaylineEditorStsAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    auto request_copy = request;
    std::string body, err;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err);
    if (!err.empty()) {
        LOG_ERROR("get_wayline_editor_sts(): ObjectToJson error: %s", err.c_str());
        if (callback) callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        return;
    }

    LOG_DEBUG("get_wayline_editor_sts(): url: %s, uuid: %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
                   const std::string& response, const std::vector<std::string>& certs,
                   const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("get_wayline_editor_sts(): network error %d, uuid: %s", static_cast<int>(err_code), uuid_str.c_str());
                break;
            }
            auto common_resp = convert_response_struct(response);
            if (common_resp.code != 0) {
                LOG_ERROR("get_wayline_editor_sts(): code:%d, msg:%s, uuid:%s", common_resp.code, common_resp.message.c_str(), uuid_str.c_str());
                break;
            }
            GetWaylineEditorStsResponseData data;
            std::string parse_err;
            if (!parse_common_response_data(common_resp, data, &parse_err, true)) {
                LOG_ERROR("get_wayline_editor_sts(): JsonToObject error: %s, uuid:%s", parse_err.c_str(), uuid_str.c_str());
                break;
            }
            callback(data, FlightHubError::success());
            return;
        } while (0);
        callback({}, make_http_error(http_code, response));
    }, 15000);
}

}

// =========================================================================
// PaaS ai-alert 告警配置与查询 HTTP 函数实现（/drc/paas/v2/ai-alert）
// =========================================================================

namespace flight_hub_sdk {

void post_ai_alert_device_setting(
    PostAiAlertDeviceSettingRequest& request,
    const std::function<void(const PostAiAlertDeviceSettingResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAiAlertDeviceSettingAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    std::string body, err_msg;
    aigc::JsonHelper::ObjectToJson(request, body, &err_msg);
    if (!err_msg.empty()) {
        LOG_ERROR("post_ai_alert_device_setting: ObjectToJson error: %s", err_msg.c_str());
        if (callback) { callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)); }
        return;
    }
    LOG_DEBUG("post_ai_alert_device_setting: url %s, body %s", url.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("post_ai_alert_device_setting: response %s", log_safe(response).c_str());
            PostAiAlertDeviceSettingResponse rsp;
            auto error_code = FlightHubError::success();
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_ai_alert_device_setting: code: %d, message: %s",
                          common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            if (callback) { callback(rsp, error_code); }
        });
}

void get_ai_alert_device_setting(
    const GetAiAlertDeviceSettingParam& param,
    const std::function<void(const GetAiAlertDeviceSettingResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string path = kAiAlertDeviceSettingAPI + "?drone_sn=" + param.drone_sn;
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_ai_alert_device_setting: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_ai_alert_device_setting: response %s", log_safe(response).c_str());
            GetAiAlertDeviceSettingResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_ai_alert_device_setting: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void post_ai_alert_algorithm_setting(
    PostAiAlertAlgorithmSettingRequest& request,
    const std::function<void(const PostAiAlertAlgorithmSettingResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kAiAlertAlgorithmSettingAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    std::string body, err_msg;
    aigc::JsonHelper::ObjectToJson(request, body, &err_msg);
    if (!err_msg.empty()) {
        LOG_ERROR("post_ai_alert_algorithm_setting: ObjectToJson error: %s", err_msg.c_str());
        if (callback) { callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)); }
        return;
    }
    LOG_DEBUG("post_ai_alert_algorithm_setting: url %s, body %s", url.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("post_ai_alert_algorithm_setting: response %s", log_safe(response).c_str());
            PostAiAlertAlgorithmSettingResponse rsp;
            auto error_code = FlightHubError::success();
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("post_ai_alert_algorithm_setting: code: %d, message: %s",
                          common_response.code, common_response.message.c_str());
                error_code = make_http_error(http_code, response);
            }
            if (callback) { callback(rsp, error_code); }
        });
}

void get_ai_alert_algorithm_setting(
    const GetAiAlertAlgorithmSettingParam& param,
    const std::function<void(const GetAiAlertAlgorithmSettingResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string path = kAiAlertAlgorithmSettingAPI
        + "?drone_sn=" + param.drone_sn
        + "&algorithm_type=" + std::to_string(param.algorithm_type);
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_ai_alert_algorithm_setting: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_ai_alert_algorithm_setting: response %s", log_safe(response).c_str());
            GetAiAlertAlgorithmSettingResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_ai_alert_algorithm_setting: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void get_ai_alert_flights(
    const GetAiAlertFlightsParam& param,
    const std::function<void(const GetAiAlertFlightsResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string path = kAiAlertFlightsAPI + "?drone_sn=" + param.drone_sn;
    if (param.begin_at > 0) path += "&begin_at=" + std::to_string(param.begin_at);
    if (param.end_at > 0)   path += "&end_at=" + std::to_string(param.end_at);
    for (const auto& src : param.algorithm_sources) {
        path += "&algorithm_sources[]=" + std::to_string(src);
    }
    path += "&page=" + std::to_string(param.page);
    path += "&page_size=" + std::to_string(param.page_size);
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_ai_alert_flights: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_ai_alert_flights: response %s", log_safe(response).c_str());
            GetAiAlertFlightsResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_ai_alert_flights: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void get_ai_alert_records(
    const GetAiAlertRecordsParam& param,
    const std::function<void(const GetAiAlertRecordsResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string path = kAiAlertRecordsAPI + "?";
    bool first = true;
    auto append = [&](const std::string& kv) {
        if (!first) path += "&";
        path += kv;
        first = false;
    };
    for (const auto& id : param.flight_id)  { append("flight_id=" + id); }
    for (const auto& sn : param.drone_sn)   { append("drone_sn=" + sn); }
    for (const auto& t  : param.target_type){ append("target_type=" + std::to_string(t)); }
    for (const auto& src: param.algorithm_sources){ append("algorithm_sources=" + std::to_string(src)); }
    if (param.begin_at > 0) append("begin_at=" + std::to_string(param.begin_at));
    if (param.end_at > 0)   append("end_at=" + std::to_string(param.end_at));
    append("page=" + std::to_string(param.page));
    append("page_size=" + std::to_string(param.page_size));
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_ai_alert_records: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_ai_alert_records: response %s", log_safe(response).c_str());
            GetAiAlertRecordsResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_ai_alert_records: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

// ===================== Live Sync HTTP Functions Begin =====================

void post_live_sync_create(
    const LiveSyncCreateRequest& request,
    const std::function<void(const LiveSyncCreateResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveSyncBaseAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    std::string body, err_msg;
    LiveSyncCreateRequest req_copy = request;
    aigc::JsonHelper::ObjectToJson(req_copy, body, &err_msg);
    if (!err_msg.empty()) {
        LOG_ERROR("post_live_sync_create: ObjectToJson error: %s", err_msg.c_str());
        if (callback) { callback({}, FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)); }
        return;
    }
    LOG_DEBUG("post_live_sync_create: url %s, body %s", url.c_str(), log_safe(body).c_str());
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("post_live_sync_create: response %s", log_safe(response).c_str());
            LiveSyncCreateResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("post_live_sync_create: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void delete_live_sync(
    const std::string& id,
    const std::function<void(FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kLiveSyncByIdAPI;
    const std::string ph = "{id}";
    auto pos = api.find(ph);
    if (pos != std::string::npos) { api.replace(pos, ph.size(), id); }
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("delete_live_sync: url %s", url.c_str());
    HttpDeleteInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, "",
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("delete_live_sync: response %s", log_safe(response).c_str());
            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("delete_live_sync: code: %d, message: %s",
                          common_response.code, common_response.message.c_str());
                if (callback) { callback(make_http_error(http_code, response)); }
                return;
            }
            if (callback) { callback(FlightHubError::success()); }
        });
}

void put_live_sync_update(
    const std::string& id,
    const std::string& body,
    const std::function<void(const LiveSyncUpdateResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string api = kLiveSyncByIdAPI;
    const std::string ph = "{id}";
    auto pos = api.find(ph);
    if (pos != std::string::npos) { api.replace(pos, ph.size(), id); }
    std::string url = generate_url(config.address, config.port, api);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["content-type"] = "application/json";
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_DEBUG("put_live_sync_update: url %s, body %s", url.c_str(), log_safe(body).c_str());
    HttpPutInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("put_live_sync_update: response %s", log_safe(response).c_str());
            LiveSyncUpdateResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("put_live_sync_update: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void get_live_sync_list(
    const LiveSyncListParams& params,
    const std::function<void(const LiveSyncListResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string path = kLiveSyncListAPI;
    path += "?page=" + std::to_string(params.page);
    path += "&size=" + std::to_string(params.size);
    if (params.set_sn)     { path += "&sn=" + params.sn; }
    if (params.set_schema) { path += "&schema=" + params.schema; }
    if (params.set_enable) { path += std::string("&enable=") + (params.enable ? "true" : "false"); }
    std::string url = generate_url(config.address, config.port, path);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_live_sync_list: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_live_sync_list: response %s", log_safe(response).c_str());
            LiveSyncListResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_live_sync_list: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void get_live_sync_devices_v2(
    const std::function<void(const LiveSyncDevicesV2Response& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveSyncDevicesV2API);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_live_sync_devices_v2: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_live_sync_devices_v2: response %s", log_safe(response).c_str());
            LiveSyncDevicesV2Response rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_live_sync_devices_v2: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

void get_live_sync_summary(
    const std::function<void(const LiveSyncSummaryResponse& data, FlightHubError err)>& callback) {
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kLiveSyncSummaryAPI);
    auto uuid = xg::newGuid();
    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    ensure_gateway_auth_headers_if_needed(url, headers);
    LOG_INFO("get_live_sync_summary: url %s", url.c_str());
    HttpGetInnerWithStatus(url, headers,
        [callback](const drc_sdk::common::NetworkError, int http_code,
                   const std::string& response, const std::vector<std::string>&,
                   const drc_sdk::common::HttpHeaders&) {
            LOG_DEBUG("get_live_sync_summary: response %s", log_safe(response).c_str());
            LiveSyncSummaryResponse rsp;
            auto error_code = FlightHubError::success();
            do {
                auto common_response = convert_response_struct(response);
                if (common_response.code != 0) {
                    error_code = make_http_error(http_code, response); break;
                }
                std::string json_err;
                if (!parse_common_response_data(common_response, rsp, &json_err, true)) {
                    LOG_ERROR("get_live_sync_summary: parse error: %s", json_err.c_str());
                    error_code = make_http_error(http_code, response);
                }
            } while (false);
            if (callback) { callback(rsp, error_code); }
        });
}

// ===================== Live Sync HTTP Functions End =====================

// ============================================================
// 数据埋点（BeaconService）HTTP 接口实现
// ============================================================

// 上报埋点事件(POST /pf-beacon/ingest/inner/v1/report, 平台内部接口)
void report_beacon_event(
    const BeaconReportRequest& request,
    const std::function<void(FlightHubError err)>& callback)
{
    auto config = FlightHubSdk::getConfig();
    std::string url = generate_url(config.address, config.port, kBeaconReportAPI);
    auto uuid = xg::newGuid();

    drc_sdk::common::HttpHeaders headers;
    headers["x-request-id"] = uuid.str();
    headers["content-type"] = "application/json";

    auto request_copy = request;
    if (request_copy.timestamp <= 0) {
        // 调用方漏填时兜底为当前时间，避免上报出 1970 的脏数据
        request_copy.timestamp = std::chrono::duration_cast<std::chrono::seconds>(
            std::chrono::system_clock::now().time_since_epoch()).count();
    }
    std::string body, err_str;
    aigc::JsonHelper::ObjectToJson(request_copy, body, &err_str);
    if (!err_str.empty()) {
        LOG_ERROR("report_beacon_event(): ObjectToJson error: %s", err_str.c_str());
        if (callback) {
            callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED));
        }
        return;
    }

    LOG_DEBUG("report_beacon_event(): url: %s, uuid(request-id): %s, body: %s", url.c_str(), uuid.str().c_str(), log_safe(body).c_str());

    // 把埋点专用超时降到 2000–3000 ms。埋点是可丢的旁路遥测，不需要和业务请求同等的重试耐心。
    HttpPostInnerWithStatus(url, headers, drc_sdk::common::HttpPostType::JSON_DATA, body,
        [callback, uuid_str = uuid.str()](const drc_sdk::common::NetworkError err_code, int http_code,
            const std::string& response, const std::vector<std::string>& certs,
            const drc_sdk::common::HttpHeaders& resp_headers) {
        do {
            if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
                LOG_ERROR("report_beacon_event(): network error %d, uuid(request-id): %s",
                    static_cast<int>(err_code),
                    uuid_str.c_str()
                );
                break;
            }

            auto common_response = convert_response_struct(response);
            if (common_response.code != 0) {
                LOG_ERROR("report_beacon_event(): code:%d, msg:%s, uuid(request-id):%s",
                    common_response.code,
                    common_response.message.c_str(),
                    uuid_str.c_str()
                );
                break;
            }
            if (callback) {
                callback(FlightHubError::success());
            }
            return;
        } while (0);

        if (callback) {
            callback(make_http_error(http_code, response));
        }
    }, 3000);
}


// 埋点上报便捷封装(填好 type/timestamp/payload 后转调 report_beacon_event)
void report_beacon_event_helper(
    std::string_view event, const FlightHubError& err, std::string_view log_tag)
{
    BeaconReportRequest report;
    // type: "sdk_api";
    report.type = kBeaconReportType_SDK_API;
    report.event = event;
    // Unix 秒级时间戳(目标事件完成后上报时刻, 已经讨论过暂不关注事件时刻, 忽略其影响);
    report.timestamp = std::chrono::duration_cast<std::chrono::seconds>(std::chrono::system_clock::now().time_since_epoch()).count();
    // payload 填充;
    report.payload = {
        // 业务结果(err 的全部字段); payload 值统一为字符串, 数值字段转字符串上报;
        {"error_code", std::to_string(static_cast<int>(err.error_code))},  // SDK 客户端错误码, 0 表示无 SDK 层错误;
        {"http_code", std::to_string(err.http_code)},                           // HTTP 状态码, 用于 HTTP 请求场景;
        {"server_code", std::to_string(err.server_code)},                       // 服务端业务错误码, 0 表示成功;
        {"device_code", std::to_string(err.device_code)},                       // 设备端返回码, 0 表示成功;
        {"server_message", err.server_message},                                 // 服务端错误描述;
    };

    // log_tag 为空时不加前缀, 非空时补一个空格与正文分隔;
    std::string log_prefix = log_tag.empty() ? std::string() : (std::string(log_tag) + " ");
    // 上报失败不影响主流程, 故不关心结果, 仅打印 log 观察;
    report_beacon_event(report, [event = std::string(event), log_prefix](FlightHubError report_err) -> void {
        LOG_DEBUG("%sreport_beacon_event() callback, event: %s, err: %s",
            log_prefix.c_str(), event.c_str(), report_err.to_string().c_str()
        );
    });
}

}  // namespace flight_hub_sdk


