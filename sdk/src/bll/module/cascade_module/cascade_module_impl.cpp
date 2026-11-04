// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "cascade_module_impl.h"

#include <cstddef>
#include <cstdint>
#include <limits>
#include <string>
#include <base/common/logger.h>

#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include "session/session_bm.h"

namespace flight_hub_sdk {

namespace {

constexpr std::size_t kMaxNotifyBodyBytes = 1u * 1024u * 1024u;  // v1.1 §9 1MB 上限

// v1.1 §通用约定：int64 走 protojson JSON 字符串编码；空串或非法值容错返回 0。
int64_t parseInt64OrZero(const std::string& s) {
    if (s.empty()) return 0;
    try {
        return std::stoll(s);
    } catch (const std::exception&) {
        LOG_WARN("[CascadeModuleImpl] parseInt64OrZero: invalid int64 str='%s'", s.c_str());
        return 0;
    }
}

FlightHubError sdkErr(FlightHubSdkErrorCode c, const std::string& msg = "") {
    FlightHubError e = FlightHubError::from_sdk(c);
    e.server_message = msg;
    return e;
}

bool validPageSize(int32_t page, int32_t size) {
    return page >= 1 && size > 0 && size <= 200;
}

// v1.1 §5/§7 total 走 int64→JSON 字符串；对外 CascadePage.total 是 int32。
// 上限 clamp + 负数保护，避免整数截断产生负值分页元数据。
int32_t clampToInt32(int64_t v) {
    if (v > std::numeric_limits<int32_t>::max()) {
        LOG_WARN("[CascadeModuleImpl] total=%lld > INT32_MAX, clamping",
                 static_cast<long long>(v));
        return std::numeric_limits<int32_t>::max();
    }
    if (v < 0) return 0;
    return static_cast<int32_t>(v);
}

CascadeNodeInfo toNodeInfo(const CascadeNodeRef& src) {
    CascadeNodeInfo n;
    n.node_id    = src.node_id;
    n.name       = src.name;
    n.code       = src.code;
    n.parent_id  = src.parent_id;
    n.level      = src.level;
    n.enabled    = src.enabled;
    n.online     = src.online;
    n.address    = src.address;
    n.networked  = src.networked;
    n.net_status = src.net_status;
    return n;
}

}  // namespace

CascadeModuleImpl::CascadeModuleImpl(int session_id) : session_id_(session_id) {
    auto bm = ResolveBusinessManager(session_id_);
    LOG_INFO("[CascadeModuleImpl] constructor");

    // v1.1 §事件推送：订阅本节点 notify 服务的 topic_cascade_node_status topic。
    // device_sn 留空（全局级联事件，非单设备维度），freq=OnChanged 按变推送。
    WeakDummy(weak_ptr);
    if (!bm) {
        LOG_ERROR("[CascadeModuleImpl] BusinessManager null, skip topic_cascade_node_status subscribe");
        return;
    }
    node_status_listen_id_ = bm->ListenCascadeNodeStatus(
        [this, weak_ptr](const notification_center::CascadeNodeStatusEvent& msg) -> void {
            WeakDummyReturn(weak_ptr);
            // notification_center 侧 pod 已是 snake_case 且 wire camelCase 反序列化在 Msg 层完成。
            CascadeNodeStatusEvent event;
            event.child_node_status_list.reserve(msg.child_node_status_list.size());
            for (const auto& c : msg.child_node_status_list) {
                CascadeChildNodeStatus cs;
                cs.node_id = c.node_id;
                cs.status  = c.status;
                event.child_node_status_list.push_back(std::move(cs));
            }
            event.parent_node_status.node_id = msg.parent_node_status.node_id;
            event.parent_node_status.status  = msg.parent_node_status.status;
            event.changed_at = msg.changed_at;
            LOG_DEBUG("[CascadeModuleImpl] topic_cascade_node_status: parent=%s changed_at=%lld children=%zu",
                     event.parent_node_status.node_id.c_str(),
                     static_cast<long long>(event.changed_at),
                     event.child_node_status_list.size());
            node_status_callbacks_.notifyCallback(event);
        },
        [weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[CascadeModuleImpl] ListenCascadeNodeStatus error: %d", error_code);
            }
        },
        "", notification_center::NotificationFrequency_OnChanged);
    if (node_status_listen_id_ < static_cast<int64_t>(notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)) {
        LOG_ERROR("[CascadeModuleImpl] ListenCascadeNodeStatus failed: %lld",
                 static_cast<long long>(node_status_listen_id_));
    }

    // v1.1 §跨节点通道消息：订阅 topic_cascade_notify，payload 任意字节透传。
    notify_payload_listen_id_ = bm->ListenCascadeNotify(
        [this, weak_ptr](const notification_center::CascadeNotifyEvent& msg) -> void {
            WeakDummyReturn(weak_ptr);
            CascadeNotifyEvent event;
            event.payload = msg.payload;
            LOG_DEBUG("[CascadeModuleImpl] topic_cascade_notify: payload_len=%zu",
                     event.payload.size());
            notify_payload_callbacks_.notifyCallback(event);
        },
        [weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[CascadeModuleImpl] ListenCascadeNotify error: %d", error_code);
            }
        },
        "", notification_center::NotificationFrequency_OnChanged);
    if (notify_payload_listen_id_ < static_cast<int64_t>(notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)) {
        LOG_ERROR("[CascadeModuleImpl] ListenCascadeNotify failed: %lld",
                 static_cast<long long>(notify_payload_listen_id_));
    }
}

CascadeModuleImpl::~CascadeModuleImpl() {
    auto bm = ResolveBusinessManager(session_id_);
    LOG_INFO("[CascadeModuleImpl] destructor");
    // 只在拿到有效订阅句柄时才 CancelObserve；订阅失败时 listen_id 会是负错误码
    // （如 -3 NotConnected），不能拿它当合法 subscribe id 反注册。
    if (node_status_listen_id_ >= 0) {
        if (bm) {
            bm->CancelObserve(node_status_listen_id_);
        }
        node_status_listen_id_ = -1;
    }
    if (notify_payload_listen_id_ >= 0) {
        if (bm) {
            bm->CancelObserve(notify_payload_listen_id_);
        }
        notify_payload_listen_id_ = -1;
    }
}

// §1 POST /cascade/api/provision
void CascadeModuleImpl::addChildNode(const AddChildNodeRequest& req,
    std::function<void(FlightHubError, const AddChildNodeResult&)> callback) {
    if (!callback) return;
    if (req.source_addr.empty() || req.child_addr.empty() || req.name.empty()) {
        LOG_WARN("[CascadeModuleImpl] addChildNode: empty source_addr/child_addr/name");
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM,
                        "source_addr/child_addr/name must be non-empty"), {});
        return;
    }
    CascadeProvisionRequest http_req;
    http_req.source_addr = req.source_addr;
    http_req.child_addr  = req.child_addr;
    http_req.name        = req.name;
    http_req.code        = req.code;
    http_req.token       = req.token;
    LOG_INFO("[CascadeModuleImpl] addChildNode: name=%s code=%s child_addr=%s",
             req.name.c_str(), req.code.c_str(), req.child_addr.c_str());
    WeakDummy(weak_ptr);
    cascade_provision(http_req,
        [this, weak_ptr, callback](const CascadeProvisionResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                AddChildNodeResult out;
                out.node_id     = r.node_id;
                out.grant_id    = r.grant_id;
                out.parent_id   = r.parent_id;
                out.executed_by = r.executed_by;
                out.enabled     = r.enabled;
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §2 POST /cascade/api/nodes/enable
void CascadeModuleImpl::enableChildNodes(const std::vector<std::string>& node_ids,
    std::function<void(FlightHubError, const CascadeBatchOpResult&)> callback) {
    if (!callback) return;
    if (node_ids.empty()) {
        LOG_WARN("[CascadeModuleImpl] enableChildNodes: empty node_ids");
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM, "node_ids must be non-empty"), {});
        return;
    }
    CascadeNodeIdsRequest http_req;
    http_req.node_id = node_ids;
    LOG_INFO("[CascadeModuleImpl] enableChildNodes: count=%zu", node_ids.size());
    WeakDummy(weak_ptr);
    cascade_enable_nodes(http_req,
        [this, weak_ptr, callback](const CascadeBatchOpResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                CascadeBatchOpResult out;
                out.ok_count   = r.ok_count;
                out.fail_count = r.fail_count;
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §3 POST /cascade/api/nodes/disable
void CascadeModuleImpl::disableChildNodes(const std::vector<std::string>& node_ids,
    std::function<void(FlightHubError, const CascadeBatchOpResult&)> callback) {
    if (!callback) return;
    if (node_ids.empty()) {
        LOG_WARN("[CascadeModuleImpl] disableChildNodes: empty node_ids");
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM, "node_ids must be non-empty"), {});
        return;
    }
    CascadeNodeIdsRequest http_req;
    http_req.node_id = node_ids;
    LOG_INFO("[CascadeModuleImpl] disableChildNodes: count=%zu", node_ids.size());
    WeakDummy(weak_ptr);
    cascade_disable_nodes(http_req,
        [this, weak_ptr, callback](const CascadeBatchOpResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                CascadeBatchOpResult out;
                out.ok_count   = r.ok_count;
                out.fail_count = r.fail_count;
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §4 DELETE /cascade/api/node/{nodeID}
void CascadeModuleImpl::deleteChildNode(const std::string& node_id,
    std::function<void(FlightHubError)> callback) {
    if (!callback) return;
    if (node_id.empty()) {
        LOG_WARN("[CascadeModuleImpl] deleteChildNode: empty node_id");
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_CASCADE_EMPTY_NODE_ID,
                        "node_id must be non-empty"));
        return;
    }
    LOG_INFO("[CascadeModuleImpl] deleteChildNode: node=%s", node_id.c_str());
    WeakDummy(weak_ptr);
    cascade_delete_node(node_id,
        [this, weak_ptr, callback](FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                if (callback) callback(e);
            });
        }, session_id_);
}

// §5 GET /cascade/api/children
void CascadeModuleImpl::getReachableChildren(const CascadeChildrenQuery& query,
    std::function<void(FlightHubError, const CascadeChildNodeListResult&)> callback) {
    if (!callback) return;
    // v1.0 §5：page 与 size 必须同时传才分页；都 <=0 表示不分页（返回全部）。
    // partial 组合（如 page=5, size=0）不合法——只传页号不传页大小没有确定语义，
    // 直接拒绝，避免 SDK/http/后端三方对「单参数分页」理解不一致产生脏数据。
    if (query.page < 0 || query.size < 0) {
        LOG_WARN("[CascadeModuleImpl] getReachableChildren: invalid page=%d size=%d",
                 query.page, query.size);
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM,
                        "page/size must be >= 0"), {});
        return;
    }
    const bool only_page = query.page > 0 && query.size <= 0;
    const bool only_size = query.size > 0 && query.page <= 0;
    if (only_page || only_size) {
        LOG_WARN("[CascadeModuleImpl] getReachableChildren: partial pagination page=%d size=%d",
                 query.page, query.size);
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM,
                        "page and size must be both > 0 or both <= 0"), {});
        return;
    }
    if (query.size > 200) {
        LOG_WARN("[CascadeModuleImpl] getReachableChildren: size=%d > 200", query.size);
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM, "size must be <= 200"), {});
        return;
    }
    LOG_INFO("[CascadeModuleImpl] getReachableChildren: node=%s page=%d size=%d name=%s status=%s",
             query.node_id.c_str(), query.page, query.size,
             query.name.c_str(), query.status.c_str());
    WeakDummy(weak_ptr);
    cascade_get_children(query.node_id, query.page, query.size, query.name, query.status,
        [this, weak_ptr, callback](const CascadeNodeListResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                CascadeChildNodeListResult out;
                out.list.reserve(r.list.size());
                for (const auto& it : r.list) out.list.push_back(toNodeInfo(it));
                out.page.total = clampToInt32(parseInt64OrZero(r.total));
                out.page.page  = r.page;
                out.page.size  = r.size;
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §6 GET /cascade/api/parent
void CascadeModuleImpl::getParentNode(
    std::function<void(FlightHubError, const CascadeParentNodeInfo&)> callback) {
    if (!callback) return;
    LOG_INFO("[CascadeModuleImpl] getParentNode");
    WeakDummy(weak_ptr);
    cascade_get_parent(
        [this, weak_ptr, callback](const CascadeParentResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                CascadeParentNodeInfo out;
                out.node_id          = r.node_id;
                out.name             = r.name;
                out.online           = r.online;
                out.depth            = r.depth;
                out.enabled          = r.enabled;
                out.heartbeat_period = parseInt64OrZero(r.heartbeat_period);
                out.net_status       = r.net_status;
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §7 POST /cascade/api/parent/respond
void CascadeModuleImpl::respondToParent(const CascadeParentRespondRequest& req,
    std::function<void(FlightHubError)> callback) {
    if (!callback) return;
    if (req.node_id.empty() || req.parent_id.empty()) {
        LOG_WARN("[CascadeModuleImpl] respondToParent: empty node_id/parent_id");
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM,
                        "node_id/parent_id must be non-empty"));
        return;
    }
    if (req.status != "accept" && req.status != "reject") {
        LOG_WARN("[CascadeModuleImpl] respondToParent: invalid status=%s", req.status.c_str());
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM,
                        "status must be accept or reject"));
        return;
    }
    LOG_INFO("[CascadeModuleImpl] respondToParent: parent=%s status=%s",
             req.parent_id.c_str(), req.status.c_str());
    CascadeParentRespondWireRequest http_req;
    http_req.node_id   = req.node_id;
    http_req.parent_id = req.parent_id;
    http_req.status    = req.status;
    WeakDummy(weak_ptr);
    cascade_parent_respond(http_req,
        [this, weak_ptr, callback](FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                if (callback) callback(e);
            });
        }, session_id_);
}

// §8 GET /cascade/api/nodes/index
void CascadeModuleImpl::getNodeIndex(int32_t page, int32_t size,
    std::function<void(FlightHubError, const CascadeChildNodeListResult&)> callback) {
    if (!callback) return;
    if (!validPageSize(page, size)) {
        LOG_WARN("[CascadeModuleImpl] getNodeIndex: invalid page=%d size=%d", page, size);
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_INVALID_PARAM, "page>=1, 0<size<=200"), {});
        return;
    }
    LOG_INFO("[CascadeModuleImpl] getNodeIndex: page=%d size=%d", page, size);
    WeakDummy(weak_ptr);
    cascade_get_node_index(page, size,
        [this, weak_ptr, callback](const CascadeNodeListResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                CascadeChildNodeListResult out;
                out.list.reserve(r.list.size());
                for (const auto& it : r.list) out.list.push_back(toNodeInfo(it));
                out.page.total = clampToInt32(parseInt64OrZero(r.total));
                out.page.page  = r.page;
                out.page.size  = r.size;
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §9 GET /cascade/api/connectivity
void CascadeModuleImpl::getConnectivity(
    std::function<void(FlightHubError, const CascadeConnectivityResult&)> callback) {
    if (!callback) return;
    LOG_INFO("[CascadeModuleImpl] getConnectivity");
    WeakDummy(weak_ptr);
    cascade_get_connectivity(
        [this, weak_ptr, callback](const CascadeConnectivityResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                CascadeConnectivityResult out;
                out.items.reserve(r.items.size());
                for (const auto& it : r.items) {
                    CascadeConnectivityItem item;
                    item.node_id   = it.node_id;
                    item.name      = it.name;
                    item.reachable = it.reachable;
                    item.rtt       = parseInt64OrZero(it.rtt);
                    out.items.push_back(std::move(item));
                }
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §10 GET /cascade/api/parent/connectivity
void CascadeModuleImpl::getParentConnectivity(
    std::function<void(FlightHubError, const CascadeParentConnectivity&)> callback) {
    if (!callback) return;
    LOG_INFO("[CascadeModuleImpl] getParentConnectivity");
    WeakDummy(weak_ptr);
    cascade_get_parent_connectivity(
        [this, weak_ptr, callback](const CascadeParentConnectivityResponse& r, FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, r, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                CascadeParentConnectivity out;
                out.node_id   = r.node_id;
                out.name      = r.name;
                out.reachable = r.reachable;
                out.rtt       = parseInt64OrZero(r.rtt);
                if (callback) callback(e, out);
            });
        }, session_id_);
}

// §11 POST /cascade/api/notify/{dstNode}
// v1.1 变更：topic 固定为 `topic_cascade_notify`，由 cascade-svc 侧决定；
// SDK 只负责 dst_node + body 透传。
void CascadeModuleImpl::postToNode(const std::string& dst_node_id,
    const std::string& body,
    std::function<void(FlightHubError)> callback) {
    if (!callback) return;
    if (dst_node_id.empty()) {
        LOG_WARN("[CascadeModuleImpl] postToNode: empty dst_node_id");
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_CASCADE_EMPTY_NODE_ID,
                        "dst_node_id must be non-empty"));
        return;
    }
    // v1.1 §9: body 长度 ≤ 1MB 合法；> 1MB 拒绝。
    if (body.size() > kMaxNotifyBodyBytes) {
        LOG_WARN("[CascadeModuleImpl] postToNode: body too large size=%zu limit=%zu",
                 body.size(), kMaxNotifyBodyBytes);
        callback(sdkErr(FlightHubSdkErrorCode::ERROR_CASCADE_BODY_TOO_LARGE, "body must be <= 1MB"));
        return;
    }
    LOG_INFO("[CascadeModuleImpl] postToNode: dst=%s body_len=%zu",
             dst_node_id.c_str(), body.size());
    WeakDummy(weak_ptr);
    cascade_notify(dst_node_id, body,
        [this, weak_ptr, callback](FlightHubError e) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([e, callback, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                if (callback) callback(e);
            });
        }, session_id_);
}

// §事件推送 cascade.node.status 订阅入口
int CascadeModuleImpl::listenNodeStatusChanged(
    std::function<void(const CascadeNodeStatusEvent& event)> callback) {
    return node_status_callbacks_.addCallback(std::move(callback));
}

void CascadeModuleImpl::cancelListenNodeStatusChanged(int listen_id) {
    node_status_callbacks_.removeCallback(listen_id);
}

// §跨节点通道消息 topic_cascade_notify 订阅入口
int CascadeModuleImpl::listenNotifyPayload(
    std::function<void(const CascadeNotifyEvent& event)> callback) {
    return notify_payload_callbacks_.addCallback(std::move(callback));
}

void CascadeModuleImpl::cancelListenNotifyPayload(int listen_id) {
    notify_payload_callbacks_.removeCallback(listen_id);
}

}  // namespace flight_hub_sdk
