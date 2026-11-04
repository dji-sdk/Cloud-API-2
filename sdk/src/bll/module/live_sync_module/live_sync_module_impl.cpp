// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "live_sync_module_impl.h"
#include <base/common/logger.h>
#include "base/utils/base64_codec.h"
#include <AIGCJson.hpp>
#include "http_interface/http_interface.h"
#include "common/executors/executors.h"

using namespace flight_hub_sdk;

namespace flight_hub_sdk {

LiveSyncModuleImpl::LiveSyncModuleImpl(int session_id) : session_id_(session_id) {
    LOG_INFO("LiveSyncModuleImpl: created");
}

static LiveSyncSchemaOption map_schema_option(const LiveSyncSchemaOptionHttp& h) {
    LiveSyncSchemaOption o;
    o.server_id       = h.server_id;
    o.server_ip       = h.server_ip;
    o.server_port     = h.server_port;
    o.device_password = h.device_password;
    o.local_port      = h.local_port;
    o.device_id       = h.device_id;
    o.local_channel   = h.local_channel;
    o.url             = h.url;
    o.provider        = h.provider;
    o.volc_user_id    = h.volc_user_id;
    o.rtsp_url        = h.rtsp_url;
    o.username        = h.username;
    o.password        = h.password;
    o.enable_ts       = h.enable_ts;
    return o;
}

static LiveSyncSchemaOptionHttp to_schema_option_http(const LiveSyncSchemaOption& o) {
    LiveSyncSchemaOptionHttp h;
    h.server_id       = o.server_id;
    h.server_ip       = o.server_ip;
    h.server_port     = o.server_port;
    h.device_password = o.device_password;
    h.local_port      = o.local_port;
    h.device_id       = o.device_id;
    h.local_channel   = o.local_channel;
    h.url             = o.url;
    h.provider        = o.provider;
    h.volc_user_id    = o.volc_user_id;
    h.rtsp_url        = o.rtsp_url;
    h.username        = o.username;
    h.password        = o.password;
    h.enable_ts       = o.enable_ts;
    return h;
}

static LiveSyncItem map_live_sync_item(const LiveSyncCreateResponse& r) {
    LiveSyncItem item;
    item.uuid             = r.uuid;
    item.status           = r.status;
    item.org_uuid         = r.org_uuid;
    item.sn               = r.sn;
    item.camera_index     = r.camera_index;
    item.name             = r.name;
    item.schema           = r.schema;
    item.schema_option    = map_schema_option(r.schema_option);
    item.device_name      = r.device_name;
    item.device_online    = r.device_online;
    item.device_model_key = r.device_model_key;
    item.is_airport       = r.is_airport;
    item.video_type       = r.video_type;
    item.video_quality    = r.video_quality;
    item.region           = r.region;
    item.enable           = r.enable;
    item.err_msg          = r.err_msg;
    item.device_type      = r.device_type;
    return item;
}

static LiveSyncItem map_live_sync_item(const LiveSyncUpdateResponse& r) {
    LiveSyncItem item;
    item.uuid             = r.uuid;
    item.status           = r.status;
    item.org_uuid         = r.org_uuid;
    item.sn               = r.sn;
    item.camera_index     = r.camera_index;
    item.name             = r.name;
    item.schema           = r.schema;
    item.schema_option    = map_schema_option(r.schema_option);
    item.device_name      = r.device_name;
    item.device_online    = r.device_online;
    item.device_model_key = r.device_model_key;
    item.is_airport       = r.is_airport;
    item.video_type       = r.video_type;
    item.video_quality    = r.video_quality;
    item.region           = r.region;
    item.enable           = r.enable;
    item.err_msg          = r.err_msg;
    item.device_type      = r.device_type;
    return item;
}

static LiveSyncItem map_live_sync_item(const LiveSyncItemHttp& r) {
    LiveSyncItem item;
    item.uuid             = r.uuid;
    item.status           = r.status;
    item.org_uuid         = r.org_uuid;
    item.sn               = r.sn;
    item.camera_index     = r.camera_index;
    item.name             = r.name;
    item.schema           = r.schema;
    item.schema_option    = map_schema_option(r.schema_option);
    item.device_name      = r.device_name;
    item.device_online    = r.device_online;
    item.device_model_key = r.device_model_key;
    item.is_airport       = r.is_airport;
    item.video_type       = r.video_type;
    item.video_quality    = r.video_quality;
    item.region           = r.region;
    item.enable           = r.enable;
    item.err_msg          = r.err_msg;
    item.device_type      = r.device_type;
    return item;
}

static LiveSyncDeviceV2 map_device_v2(const LiveSyncDeviceV2Http& h) {
    LiveSyncDeviceV2 d;
    d.sn               = h.sn;
    d.name             = h.name;
    d.device_model_key = h.device_model_key;
    d.device_type      = h.device_type;
    // camera_capacity 为 base64 字符串，解码后解析内层 JSON；
    // 字段缺省/空/解码或解析失败均视为无能力（空集合），不影响整条设备树解析。
    if (!h.camera_capacity.empty()) {
        std::string decoded = drc_sdk::base64_decode(h.camera_capacity);
        if (!decoded.empty()) {
            LiveSyncCameraCapacityPayloadHttp payload;
            std::string parse_err;
            if (aigc::JsonHelper::JsonToObject(payload, decoded, {}, &parse_err)) {
                for (const auto& cc : payload.camera_list) {
                    LiveSyncCameraCapacity cap;
                    cap.camera_index = cc.camera_index;
                    for (const auto& v : cc.video_list) {
                        LiveSyncVideoCapacity vc;
                        vc.video_type  = v.video_type;
                        vc.video_index = v.video_index;
                        cap.videos_list.push_back(vc);
                    }
                    d.camera_capacity.push_back(cap);
                }
            } else {
                LOG_WARN("map_device_v2: camera_capacity decode-json parse failed sn=%s, err=%s",
                         h.sn.c_str(), parse_err.c_str());
            }
        }
    }
    return d;
}

void LiveSyncModuleImpl::createLiveSync(
    const LiveSyncCreateParams& params,
    std::function<void(FlightHubError err, LiveSyncCreateResult result)> callback) {
    WeakDummy(weak_ptr);
    LiveSyncCreateRequest req;
    req.converter_name = params.converter_name;
    req.sn             = params.sn;
    req.camera_index   = params.camera_index;
    req.video_type     = params.video_type;
    req.video_quality  = params.video_quality;
    req.schema         = params.schema;
    req.schema_option  = to_schema_option_http(params.schema_option);
    post_live_sync_create(req,
        [this, weak_ptr, callback](const LiveSyncCreateResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 创建转码器; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_LIVE_SYNC_CREATE, err, session_id_, "[LiveSyncModuleImpl]");
                LiveSyncCreateResult result;
                result.item      = map_live_sync_item(rsp);
                result.limit     = rsp.limit;
                result.has_limit = (rsp.limit > 0);
                if (callback) { callback(err, result); }
            });
        }, session_id_);
}

void LiveSyncModuleImpl::deleteLiveSync(
    const std::string& id,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    delete_live_sync(id,
        [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 删除转码器; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_LIVE_SYNC_DELETE, err, session_id_, "[LiveSyncModuleImpl]");
                if (callback) { callback(err); }
            });
        }, session_id_);
}

void LiveSyncModuleImpl::updateLiveSync(
    const LiveSyncUpdateParams& params,
    std::function<void(FlightHubError err, LiveSyncItem result)> callback) {
    WeakDummy(weak_ptr);

    // 判断是否仅开关 enable（路径1：小 body）
    bool only_enable = params.set_enable
        && !params.set_schema && !params.set_schema_option
        && !params.set_converter_name && !params.set_sn
        && !params.set_camera_index && !params.set_video_type
        && !params.set_video_quality;

    std::string body;
    std::string json_err;
    if (only_enable) {
        LiveSyncEnableOnlyRequest req;
        req.enable = params.enable;
        aigc::JsonHelper::ObjectToJson(req, body, &json_err);
    } else {
        LiveSyncUpdateFullRequest req;
        if (params.set_enable)         req.enable         = params.enable;
        if (params.set_converter_name) req.converter_name = params.converter_name;
        if (params.set_sn)             req.sn             = params.sn;
        if (params.set_camera_index)   req.camera_index   = params.camera_index;
        if (params.set_video_type)     req.video_type     = params.video_type;
        if (params.set_video_quality)  req.video_quality  = params.video_quality;
        if (params.set_schema)         req.schema         = params.schema;
        if (params.set_schema_option)  req.schema_option  = to_schema_option_http(params.schema_option);
        aigc::JsonHelper::ObjectToJson(req, body, &json_err);
    }

    if (!json_err.empty()) {
        LOG_ERROR("LiveSyncModuleImpl::updateLiveSync: ObjectToJson error: %s", json_err.c_str());
        drc_sdk::common::Executors::Main()->Post([callback, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
            auto json_error = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);
            // 埋点: 更新直播同步任务(请求体序列化失败, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_LIVE_SYNC_UPDATE, json_error, session_id_, "[LiveSyncModuleImpl]");
            if (callback) { callback(json_error, {}); }
        });
        return;
    }

    put_live_sync_update(params.id, body,
        [this, weak_ptr, callback](const LiveSyncUpdateResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 更新转码器（启用/停用或全量更新）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_LIVE_SYNC_UPDATE, err, session_id_, "[LiveSyncModuleImpl]");
                LiveSyncItem result = map_live_sync_item(rsp);
                if (callback) { callback(err, result); }
            });
        }, session_id_);
}

void LiveSyncModuleImpl::getLiveSyncList(
    const LiveSyncListParams& params,
    std::function<void(FlightHubError err, LiveSyncListResult result)> callback) {
    WeakDummy(weak_ptr);
    get_live_sync_list(params,
        [this, weak_ptr, callback](const LiveSyncListResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 分页查询转码器列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_LIVE_SYNC_GET_LIST, err, session_id_, "[LiveSyncModuleImpl]");
                LiveSyncListResult result;
                result.page  = rsp.page;
                result.size  = rsp.size;
                result.total = rsp.total;
                for (const auto& it : rsp.list) {
                    result.list.push_back(map_live_sync_item(it));
                }
                if (callback) { callback(err, result); }
            });
        }, session_id_);
}

void LiveSyncModuleImpl::getLiveSyncDevicesV2(
    std::function<void(FlightHubError err, LiveSyncDevicesV2Result result)> callback) {
    WeakDummy(weak_ptr);
    get_live_sync_devices_v2(
        [this, weak_ptr, callback](const LiveSyncDevicesV2Response& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 获取可用设备树（V2，含基站/Dock）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_LIVE_SYNC_GET_DEVICES_V2, err, session_id_, "[LiveSyncModuleImpl]");
                LiveSyncDevicesV2Result result;
                result.total = rsp.total;
                for (const auto& item : rsp.list) {
                    LiveSyncDeviceItemV2 di;
                    di.gateway = map_device_v2(item.gateway);
                    for (const auto& h : item.hosts) {
                        di.hosts.push_back(map_device_v2(h));
                    }
                    result.list.push_back(di);
                }
                if (callback) { callback(err, result); }
            });
        }, session_id_);
}

void LiveSyncModuleImpl::getLiveSyncSummary(
    std::function<void(FlightHubError err, LiveSyncSummaryResult result)> callback) {
    WeakDummy(weak_ptr);
    get_live_sync_summary(
        [this, weak_ptr, callback](const LiveSyncSummaryResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 获取转码器总数与启用数汇总; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_LIVE_SYNC_GET_SUMMARY, err, session_id_, "[LiveSyncModuleImpl]");
                LiveSyncSummaryResult result;
                result.total      = rsp.total;
                result.enable_cnt = rsp.enable_cnt;
                if (callback) { callback(err, result); }
            });
        }, session_id_);
}

}  // namespace flight_hub_sdk
