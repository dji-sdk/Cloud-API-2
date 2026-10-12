// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "element_module_impl.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include "BusinessManager.h"
#include <AIGCJson.hpp>

namespace flight_hub_sdk {

// 从 JSON 字符串中提取指定 key 对应的数组子串（含括号匹配）
static std::string extractJsonArray(const std::string& json, const std::string& key) {
    std::string search = "\"" + key + "\":";
    auto pos = json.find(search);
    if (pos == std::string::npos) {
        // 也尝试 key 后有空格的情况
        search = "\"" + key + "\" :";
        pos = json.find(search);
        if (pos == std::string::npos) return "";
    }
    pos += search.size();
    // 跳过空白
    while (pos < json.size() && (json[pos] == ' ' || json[pos] == '\t' || json[pos] == '\n' || json[pos] == '\r')) pos++;
    if (pos >= json.size() || json[pos] != '[') return "";
    // 匹配括号
    int depth = 0;
    size_t start = pos;
    for (size_t i = pos; i < json.size(); i++) {
        if (json[i] == '[') depth++;
        else if (json[i] == ']') { depth--; if (depth == 0) return json.substr(start, i - start + 1); }
    }
    return "";
}

// 从 coordinates raw JSON 中提取所有坐标点（兼容 Point/LineString/Polygon 多态嵌套）
static std::vector<ElementGeoPoint> parseCoordinatesJson(const std::string& json_str) {
    std::vector<ElementGeoPoint> result;
    if (json_str.empty()) return result;
    // 提取所有浮点数，按坐标维度分组
    std::vector<double> numbers;
    size_t i = 0;
    while (i < json_str.size()) {
        if (json_str[i] == '-' || json_str[i] == '.' || (json_str[i] >= '0' && json_str[i] <= '9')) {
            size_t start = i;
            if (json_str[i] == '-') i++;
            while (i < json_str.size() && (json_str[i] == '.' || (json_str[i] >= '0' && json_str[i] <= '9') || json_str[i] == 'e' || json_str[i] == 'E' || json_str[i] == '+' || (json_str[i] == '-' && i > start && (json_str[i-1] == 'e' || json_str[i-1] == 'E')))) {
                i++;
            }
            try { numbers.push_back(std::stod(json_str.substr(start, i - start))); } catch (...) {}
        } else {
            i++;
        }
    }
    // 按 3 个一组或 2 个一组提取坐标
    int stride = (numbers.size() >= 3 && numbers.size() % 3 == 0) ? 3 : 2;
    for (size_t j = 0; j + stride - 1 < numbers.size(); j += stride) {
        ElementGeoPoint p;
        p.lng = numbers[j];
        p.lat = numbers[j + 1];
        p.alt = (stride >= 3) ? numbers[j + 2] : 0.0;
        result.push_back(p);
    }
    return result;
}

// 用于 AIGC 解析 resource JSON 的辅助结构体
struct ResourceContentParseHelper {
    std::string type;
    ElementPropertiesResp properties;
    struct {
        std::string type;
        double radius = 0.0;
        std::string coordinates;   // raw JSON 多态
        std::string bread_crumbs;  // raw JSON 多态
        AIGC_JSON_HELPER(type, radius, coordinates, bread_crumbs)
    } geometry;
    ElementMediaResourcesResp media_resources;
    AIGC_JSON_HELPER(type, properties, geometry, media_resources)
};

struct ResourceParseHelper {
    ResourceContentParseHelper content;
    int type = 0;
    std::string user_name;
    std::string user_id;
    std::string remark;
    AIGC_JSON_HELPER(content, type, user_name, user_id, remark)
};

// resource JSON string → ElementResourceData
static ElementResourceData convertResourceData(const std::string& resource_json) {
    ElementResourceData d;
    if (resource_json.empty()) return d;
    ResourceParseHelper h;
    std::string err;
    aigc::JsonHelper::JsonToObject(h, resource_json, {}, &err);
    // coordinates/bread_crumbs 是多态数组，AIGC 无法解析到 string，报错是预期行为，忽略
    d.type      = h.type;
    d.user_name = h.user_name;
    d.user_id   = h.user_id;
    d.remark    = h.remark;
    d.content.type = h.content.type;
    d.content.properties.color           = h.content.properties.color;
    d.content.properties.clamp_to_ground = h.content.properties.clamp_to_ground;
    d.content.properties.is3d            = h.content.properties.is3d;
    d.content.properties.is_transparent  = h.content.properties.is_transparent;
    d.content.properties.font_size       = h.content.properties.font_size;
    d.content.properties.icon            = h.content.properties.icon;
    d.content.geometry.type   = h.content.geometry.type;
    d.content.geometry.radius = h.content.geometry.radius;
    // coordinates/bread_crumbs 为 raw JSON 透传
    d.content.geometry.coordinates  = extractJsonArray(resource_json, "coordinates");
    d.content.geometry.bread_crumbs = extractJsonArray(resource_json, "bread_crumbs");
    for (const auto& img : h.content.media_resources.images) {
        ElementMediaImage mi;
        mi.object_key      = img.object_key;
        mi.media_file_id   = img.media_file_id;
        mi.media_file_uuid = img.media_file_uuid;
        d.content.media_resources.images.push_back(mi);
    }
    return d;
}

// ── HTTP 接口 ──────────────────────────────────────────────────────────────

void ElementModuleImpl::createElement(
    const CreateElementParams& params,
    std::function<void(FlightHubError err, const CreateElementResult& result)> callback) {
    WeakDummy(weak_ptr);
    CreateElementRequest request;
    request.id             = params.id;
    request.source         = params.source;
    // ElementResourceContent → ElementResourceContentReq 逐字段转换
    {
        const auto& c = params.resource.content;
        ElementResourceContentReq req_content;
        req_content.type = c.type;
        req_content.properties.color            = c.properties.color;
        req_content.properties.clamp_to_ground  = c.properties.clamp_to_ground;
        req_content.properties.is3d             = c.properties.is3d;
        req_content.properties.is_transparent   = c.properties.is_transparent;
        req_content.properties.font_size        = c.properties.font_size;
        req_content.properties.icon             = c.properties.icon;
        req_content.geometry.type   = c.geometry.type;
        req_content.geometry.radius = c.geometry.radius;
        req_content.geometry.coordinates  = c.geometry.coordinates;
        req_content.geometry.bread_crumbs = c.geometry.bread_crumbs;
        for (const auto& img : c.media_resources.images) {
            ElementMediaImageReq img_req;
            img_req.object_key       = img.object_key;
            img_req.media_file_id    = img.media_file_id;
            img_req.media_file_uuid  = img.media_file_uuid;
            req_content.media_resources.images.push_back(img_req);
        }
        request.resource.content = req_content;
    }
    request.resource.type    = params.resource.type;
    request.resource.remark  = params.resource.remark;
    request.name           = params.name;
    request.element_source = params.element_source;
    request.desc           = params.desc;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() mutable {
        WeakDummyReturn(weak_ptr);
        element_create(request,
            [this, weak_ptr, callback](const ElementCreateResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 创建要素; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_ELEMENT_CREATE, err, "[ElementModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, CreateElementResult{});
                        return;
                    }
                    CreateElementResult result;
                    result.id = data.id;
                    if (callback) callback(err, result);
                });
            });
    });
}

void ElementModuleImpl::updateElement(
    const UpdateElementParams& params,
    std::function<void(FlightHubError err, const UpdateElementResult& result)> callback) {
    WeakDummy(weak_ptr);
    UpdateElementRequest request;
    request.name                  = params.name;
    if (params.content.has_value()) {
        const auto& c = params.content.value();
        ElementResourceContentReq req_content;
        req_content.type = c.type;
        req_content.properties.color            = c.properties.color;
        req_content.properties.clamp_to_ground  = c.properties.clamp_to_ground;
        req_content.properties.is3d             = c.properties.is3d;
        req_content.properties.is_transparent   = c.properties.is_transparent;
        req_content.properties.font_size        = c.properties.font_size;
        req_content.properties.icon             = c.properties.icon;
        req_content.geometry.type   = c.geometry.type;
        req_content.geometry.radius = c.geometry.radius;
        req_content.geometry.coordinates  = c.geometry.coordinates;
        req_content.geometry.bread_crumbs = c.geometry.bread_crumbs;
        for (const auto& img : c.media_resources.images) {
            ElementMediaImageReq img_req;
            img_req.object_key       = img.object_key;
            img_req.media_file_id    = img.media_file_id;
            img_req.media_file_uuid  = img.media_file_uuid;
            req_content.media_resources.images.push_back(img_req);
        }
        request.content = req_content;
    }
    request.display               = params.display;
    request.remark                = params.remark;
    request.status                = params.status;
    request.elevation_load_status = params.elevation_load_status;
    request.user_status           = params.user_status;
    request.target_layer_id       = params.target_layer_id;
    for (const auto& o : params.orders) {
        ElementResourceOrderItemReq item;
        item.id    = o.id;
        item.order = o.order;
        request.orders.push_back(item);
    }
    const std::string element_id = params.id;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, element_id, request, callback]() mutable {
        WeakDummyReturn(weak_ptr);
        element_update(element_id, request,
            [this, weak_ptr, callback](const ElementUpdateResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 更新要素属性或移动要素到目标图层; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_ELEMENT_UPDATE, err, "[ElementModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, UpdateElementResult{});
                        return;
                    }
                    UpdateElementResult result;
                    result.id = data.id;
                    if (callback) callback(err, result);
                });
            });
    });
}

void ElementModuleImpl::listElementHierarchy(
    const ListElementHierarchyParams& params,
    std::function<void(FlightHubError err, const ListElementHierarchyResult& result)> callback) {
    WeakDummy(weak_ptr);
    ListElementHierarchyRequest request;
    request.group_id       = params.group_id;
    request.pid            = params.pid;
    request.is_distributed = params.is_distributed;
    request.type           = params.type;
    request.element_uuid   = params.element_uuid;
    request.exclude_copy   = params.exclude_copy;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        element_list_hierarchy(
            request,
            [this, weak_ptr, callback](const ElementListHierarchyResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 查询元素层级列表（目录树 JSON 透传）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_ELEMENT_LIST_HIERARCHY, err, "[ElementModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, ListElementHierarchyResult{});
                        return;
                    }
                    ListElementHierarchyResult result;
                    for (const auto& d : data.data) {
                        ElementGroupInfo g;
                        g.id             = d.id;
                        g.name           = d.name;
                        g.order          = d.order;
                        g.is_distributed = d.is_distributed;
                        g.type           = d.type;
                        g.is_lock        = d.is_lock;
                        g.tri_state      = d.tri_state;
                        g.create_time    = d.create_time;
                        g.pid            = d.pid;
                        for (const auto& e : d.elements) {
                            ElementItemInfo item;
                            item.id                    = e.id;
                            item.name                  = e.name;
                            item.desc                  = e.desc;
                            item.source                = e.source;
                            item.order                 = e.order;
                            item.status                = e.status;
                            item.display               = e.display;
                            item.resource              = convertResourceData(e.resource);
                            item.update_time           = e.update_time;
                            item.create_time           = e.create_time;
                            item.elevation_load_status = e.elevation_load_status;
                            item.notify                = e.notify;
                            g.elements.push_back(item);
                        }
                        for (const auto& c : d.children) {
                            ElementGroupInfo child;
                            child.id             = c.id;
                            child.name           = c.name;
                            child.order          = c.order;
                            child.is_distributed = c.is_distributed;
                            child.type           = c.type;
                            child.is_lock        = c.is_lock;
                            child.tri_state      = c.tri_state;
                            child.create_time    = c.create_time;
                            child.pid            = c.pid;
                            for (const auto& e : c.elements) {
                                ElementItemInfo item;
                                item.id                    = e.id;
                                item.name                  = e.name;
                                item.desc                  = e.desc;
                                item.source                = e.source;
                                item.order                 = e.order;
                                item.status                = e.status;
                                item.display               = e.display;
                                item.resource              = convertResourceData(e.resource);
                                item.update_time           = e.update_time;
                                item.create_time           = e.create_time;
                                item.elevation_load_status = e.elevation_load_status;
                                item.notify                = e.notify;
                                child.elements.push_back(item);
                            }
                            g.children.push_back(child);
                        }
                        result.list.push_back(g);
                    }
                    if (callback) callback(err, result);
                });
            });
    });
}

void ElementModuleImpl::setElementVisibility(
    const SetElementVisibilityParams& params,
    std::function<void(FlightHubError err, const SetElementVisibilityResult& result)> callback) {
    WeakDummy(weak_ptr);
    SetElementVisibilityRequest request;
    request.display = params.display;
    const std::string element_id = params.id;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, element_id, request, callback]() mutable {
        WeakDummyReturn(weak_ptr);
        element_set_visibility(element_id, request,
            [this, weak_ptr, callback](const ElementSetVisibilityResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 设置要素显示/隐藏状态（PaaS 专用简化接口）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_ELEMENT_SET_VISIBILITY, err, "[ElementModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, SetElementVisibilityResult{});
                        return;
                    }
                    SetElementVisibilityResult result;
                    result.id = data.id;
                    if (callback) callback(err, result);
                });
            });
    });
}

void ElementModuleImpl::deleteElement(
    const DeleteElementParams& params,
    std::function<void(FlightHubError err, const DeleteElementResult& result)> callback) {
    WeakDummy(weak_ptr);
    const std::string element_id    = params.id;
    const bool from_batch           = params.from_batch_delete;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, element_id, from_batch, callback]() {
        WeakDummyReturn(weak_ptr);
        element_delete(element_id, from_batch,
            [this, weak_ptr, callback](const ElementDeleteResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data]() {
                    // 埋点: 删除要素（仅限基础几何类型：点/线/面/圆）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_ELEMENT_DELETE, err, "[ElementModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        if (callback) callback(err, DeleteElementResult{});
                        return;
                    }
                    DeleteElementResult result;
                    result.id = data.id;
                    if (callback) callback(err, result);
                });
            });
    });
}

// ── WS 订阅接口 ────────────────────────────────────────────────────────────

int ElementModuleImpl::listenElementCreate(
    const std::function<void(const ElementCreateMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenElementCreate(
            [weak_ptr, callback](const notification_center::ElementCreate& nc_msg) {
                WeakDummyReturn(weak_ptr);
                ElementCreateMsg msg;
                msg.id       = nc_msg.id;
                msg.group_id = nc_msg.group_id;
                msg.name     = nc_msg.name;
                msg.display  = nc_msg.display;
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

void ElementModuleImpl::cancelListenElementCreate(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

int ElementModuleImpl::listenElementUpdate(
    const std::function<void(const ElementUpdateMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenElementUpdate(
            [weak_ptr, callback](const notification_center::ElementUpdate& nc_msg) {
                WeakDummyReturn(weak_ptr);
                ElementUpdateMsg msg;
                msg.id       = nc_msg.id;
                msg.group_id = nc_msg.group_id;
                msg.name     = nc_msg.name;
                msg.display  = nc_msg.display;
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

void ElementModuleImpl::cancelListenElementUpdate(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

int ElementModuleImpl::listenElementDelete(
    const std::function<void(const ElementDeleteMsg& msg)>& callback) {
    WeakDummy(weak_ptr);
    return NotificationCenter::getInstance()->getBusinessManager()
        ->ListenElementDelete(
            [weak_ptr, callback](const notification_center::ElementDelete& nc_msg) {
                WeakDummyReturn(weak_ptr);
                ElementDeleteMsg msg;
                msg.id       = nc_msg.id;
                msg.group_id = nc_msg.group_id;
                if (callback) callback(msg);
            },
            [](const notification_center::NotificationCenterErrorCode&) {},
            "",
            notification_center::NotificationFrequency_OnChanged);
}

void ElementModuleImpl::cancelListenElementDelete(int listen_id) {
    if (listen_id >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
    }
}

}  // namespace flight_hub_sdk
