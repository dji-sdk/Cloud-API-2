// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <functional>
#include <memory>
#include <string>
#include <vector>

#include "message.h"

namespace flight_hub_sdk {

// ---------- 分页 ----------

/// 分页元数据（对齐 api.md 通用约定 `{list,total,page,size}`）
struct CascadePage {
    int32_t total = 0;
    int32_t page  = 1;
    int32_t size  = 50;
};

// ---------- 节点信息 ----------

/// 节点引用（对齐 v1.1 §NodeRef / §ChildNodeRef）
///
/// v1.1 两个列表接口的节点对象差异字段：
/// - §5 下级列表（/children）出参带 `networked`（是否已组网），不带 `net_status`；
/// - §8 组网列表（/nodes/index）出参带 `net_status`（accept/reject/空），不带 `networked`。
/// SDK 复用同一 POD，未由后端下发的差异字段保持默认值（false / 空）。
struct CascadeNodeInfo {
    std::string node_id;     ///< 16 位 MDC
    std::string name;        ///< 节点名称
    std::string code;        ///< 路径段
    std::string parent_id;   ///< 父节点 ID
    int32_t     level = 0;   ///< 层级（总部=1，最大 5）
    bool        enabled = false; ///< 链路启用态
    std::string online;      ///< v1.1 在线态字符串：`online` / `offline`
    std::string address;     ///< 组网地址（v1.1 §NodeRef，字段名 address）
    bool        networked = false; ///< §5 下级列表差异字段：是否已组网（默认 false）
    std::string net_status;       ///< §8 组网列表差异字段：`accept` / `reject` / 空（无记录）
};

struct CascadeChildNodeListResult {
    std::vector<CascadeNodeInfo> list;
    CascadePage                  page;
};

// ---------- 父节点 ----------

struct CascadeParentNodeInfo {
    std::string node_id;                   ///< v1.1 §6：父平台节点 ID（无父时为空）
    std::string name;
    std::string online;                    ///< v1.1 在线态字符串：`online` / `offline`
    int32_t     depth = 1;                 ///< 总部=1
    bool        enabled = false;
    int64_t     heartbeat_period = 0;
    std::string net_status;                ///< v1.1 §6：组网状态 `accept` / `reject` / 空（无记录）
};

// ---------- 批量操作 / 查询过滤 ----------

/// v1.0 §2/§3 批量启用/禁用响应
struct CascadeBatchOpResult {
    int ok_count = 0;
    int fail_count = 0;
};

/// v1.0 §5 下级平台列表过滤
struct CascadeChildrenQuery {
    std::string node_id;  ///< 空则查本节点
    int32_t     page = 0; ///< 与 size 同时 >0 才分页；同时 <=0 表示不分页（返回全部）
    int32_t     size = 0; ///< 与 page 同时 >0 才分页；同时 <=0 表示不分页；上限 200
    std::string name;     ///< 关键词模糊
    std::string status;   ///< v1.1 §5：取值 "online" / "offline" / ""（空 = 不过滤）
};

// ---------- 配网 ----------

/// v1.1 §1 配网请求
struct AddChildNodeRequest {
    std::string source_addr;  ///< 必填：源平台地址（本节点 IP:port）
    std::string child_addr;   ///< 必填：目标平台地址（子平台 IP:port 或 tls://host:port）
    std::string name;         ///< 必填：平台名称
    std::string code;         ///< 可选：路径段，空则后端生成 n-<uuid6>
    std::string token;        ///< 可选：保留字段，当前忽略（认证走 mTLS）
};

/// v1.1 §1 配网响应
struct AddChildNodeResult {
    std::string node_id;
    std::string grant_id;     ///< 后端 int64→JSON 字符串编码
    std::string parent_id;
    std::string executed_by;
    bool        enabled = false; ///< v1.1 §1：配网后默认禁用，需前端显式启用
};

// ---------- 联通性检测 (v1.1 §9/§10) ----------

/// v1.1 §9 下级联通性检测单条结果
struct CascadeConnectivityItem {
    std::string node_id;
    std::string name;
    bool        reachable = false;
    int64_t     rtt = 0;   ///< 往返时延（毫秒），不可达时 0
};

struct CascadeConnectivityResult {
    std::vector<CascadeConnectivityItem> items;
};

/// v1.1 §10 上级平台联通性检测结果（正常情况下只有一个上级平台）
struct CascadeParentConnectivity {
    std::string node_id;   ///< 上级平台节点 ID（无上级/总部时为空）
    std::string name;
    bool        reachable = false;
    int64_t     rtt = 0;   ///< 往返时延（毫秒），不可达时 0
};

/// v1.1 §7 响应上级平台组网请求
struct CascadeParentRespondRequest {
    std::string node_id;   ///< 本节点 nodeID
    std::string parent_id; ///< 上级节点 nodeID
    std::string status;    ///< `accept`（接受）/ `reject`（拒绝）
};

// ---------- 事件推送（v1.1 §事件推送） ----------

/// v1.1 事件推送 topic：`topic_cascade_node_status`
///
/// 事件体（v1.1）：
/// ```json
/// {
///   "childNodeStatusList": [
///     {"nodeID": "GD", "status": "enabled"},
///     {"nodeID": "SZ", "status": "disabled"},
///     {"nodeID": "BJ", "status": "deleted"}
///   ],
///   "parentNodeStatus": {"nodeID": "HQ", "status": "enabled"},
///   "changedAt": 1756600000
/// }
/// ```
///
/// status 取值（v1.1）：`enabled` / `disabled` / `deleted`，仅承载操作结果，
/// 不承载在线态（在线态读 REST 接口）。推的是全量快照，幂等自愈。
constexpr const char* kCascadeNodeStatusTopic = "topic_cascade_node_status";

/// v1.1 事件体中单个子节点状态项。
struct CascadeChildNodeStatus {
    std::string node_id;   ///< v1.1 wire: `nodeID`
    std::string status;    ///< v1.1：`enabled` / `disabled` / `deleted`
};

/// v1.1 事件体中父节点状态。
struct CascadeParentNodeStatus {
    std::string node_id;   ///< v1.1 wire: `nodeID`
    std::string status;    ///< v1.1：`enabled` / `disabled` / `deleted`
};

/// v1.1 `topic_cascade_node_status` 事件体聚合。
struct CascadeNodeStatusEvent {
    std::vector<CascadeChildNodeStatus> child_node_status_list;
    CascadeParentNodeStatus              parent_node_status;
    int64_t                              changed_at = 0;  ///< Unix 秒
};

/// v1.1 事件推送 topic：`topic_cascade_notify`
///
/// 对端节点通过 `POST /cascade/api/notify/{dstNode}` 发过来的原始 payload；
/// SDK 不解析（任意字节透传，与 postToNode 的 body 语义对称）。
constexpr const char* kCascadeNotifyTopic = "topic_cascade_notify";

/// v1.1 `topic_cascade_notify` 事件体。
struct CascadeNotifyEvent {
    std::string payload;   ///< 发送侧 postToNode 时透传的原始 body（SDK 不解析）
};

// ---------- 接口 ----------

/// 司空 Mesh 级联模块。全局单例，通过 FlightHubSdk::getCascadeModule() 获取。
/// 契约对齐：docs/superpowers/specs/级联接口文档-v1.1.md（v1.1，11 个接口）
class ICascadeModule {
public:
    virtual ~ICascadeModule() = default;

    /// @brief 配网（新增子平台）
    /// @internal 链路: HTTP | POST /cascade/api/provision @endinternal
    virtual void addChildNode(
        const AddChildNodeRequest& req,
        std::function<void(FlightHubError err, const AddChildNodeResult& result)> callback) = 0;

    /// @brief 批量启用子节点
    /// @internal 链路: HTTP | POST /cascade/api/nodes/enable @endinternal
    virtual void enableChildNodes(
        const std::vector<std::string>& node_ids,
        std::function<void(FlightHubError err, const CascadeBatchOpResult& result)> callback) = 0;

    /// @brief 批量禁用子节点
    /// @internal 链路: HTTP | POST /cascade/api/nodes/disable @endinternal
    virtual void disableChildNodes(
        const std::vector<std::string>& node_ids,
        std::function<void(FlightHubError err, const CascadeBatchOpResult& result)> callback) = 0;

    /// @brief 删除子平台
    /// @internal 链路: HTTP | DELETE /cascade/api/node/{nodeID} @endinternal
    virtual void deleteChildNode(
        const std::string& node_id,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 下级平台列表（分页可选 + name/status 过滤）
    /// @internal 链路: HTTP | GET /cascade/api/children @endinternal
    virtual void getReachableChildren(
        const CascadeChildrenQuery& query,
        std::function<void(FlightHubError err, const CascadeChildNodeListResult& result)> callback) = 0;

    /// @brief 上级平台信息
    /// @internal 链路: HTTP | GET /cascade/api/parent @endinternal
    virtual void getParentNode(
        std::function<void(FlightHubError err, const CascadeParentNodeInfo& info)> callback) = 0;

    /// @brief 响应上级平台组网请求（接受 / 拒绝）
    /// @param req 包含 nodeID/parentID/status(accept|reject)
    /// @internal 链路: HTTP | POST /cascade/api/parent/respond @endinternal
    virtual void respondToParent(
        const CascadeParentRespondRequest& req,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 组网列表（分页；扁平返回本节点+全部方向）
    /// @internal 链路: HTTP | GET /cascade/api/nodes/index @endinternal
    virtual void getNodeIndex(
        int32_t page, int32_t size,
        std::function<void(FlightHubError err, const CascadeChildNodeListResult& result)> callback) = 0;

    /// @brief 联通性检测（v1.1 §9）
    /// 遍历本节点的下级平台，逐个走 Mesh Route+Accessible 探活。
    /// @internal 链路: HTTP | GET /cascade/api/connectivity @endinternal
    virtual void getConnectivity(
        std::function<void(FlightHubError err, const CascadeConnectivityResult& result)> callback) = 0;

    /// @brief 上级平台联通性检测（v1.1 §10）
    /// 查询 Mesh 获取上级平台信息再 Route+Accessible 探活；正常只有一个上级平台。
    /// @internal 链路: HTTP | GET /cascade/api/parent/connectivity @endinternal
    virtual void getParentConnectivity(
        std::function<void(FlightHubError err, const CascadeParentConnectivity& result)> callback) = 0;

    /// @brief 跨节点透传发送（body 原样字节，不解析、不重试；> 1MB 直接失败）
    /// @param dst_node_id 目标节点 nodeID
    /// @param body 原样字节透传；长度 ≤ 1MB（v1.1 §11）
    /// @internal 链路: HTTP | POST /cascade/api/notify/{dstNode} @endinternal
    /// @note v1.1 变更：topic 固定为 `topic_cascade_notify`，由 cascade-svc 侧决定，
    ///       不再由调用方传入。
    virtual void postToNode(
        const std::string& dst_node_id,
        const std::string& body,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 订阅级联节点状态变更事件（topic `cascade.node.status`）
    /// @param callback 事件回调；每次推送为全量快照，幂等自愈
    /// @return 订阅句柄，用于 cancelListenNodeStatusChanged；<0 表示订阅失败
    /// @see cancelListenNodeStatusChanged
    /// @internal 链路: Notify | topic cascade.node.status @endinternal
    virtual int listenNodeStatusChanged(
        std::function<void(const CascadeNodeStatusEvent& event)> callback) = 0;

    /// @brief 取消订阅级联节点状态变更事件
    /// @param listen_id listenNodeStatusChanged 返回的句柄
    virtual void cancelListenNodeStatusChanged(int listen_id) = 0;

    /// @brief 订阅跨节点通道消息事件（topic `topic_cascade_notify`）
    /// @param callback 事件回调；payload 是发送侧 postToNode 时透传的原始 body（SDK 不解析）
    /// @return 订阅句柄，用于 cancelListenNotifyPayload；<0 表示订阅失败
    /// @see cancelListenNotifyPayload
    /// @internal 链路: Notify | topic topic_cascade_notify @endinternal
    virtual int listenNotifyPayload(
        std::function<void(const CascadeNotifyEvent& event)> callback) = 0;

    /// @brief 取消订阅跨节点通道消息事件
    /// @param listen_id listenNotifyPayload 返回的句柄
    virtual void cancelListenNotifyPayload(int listen_id) = 0;
};

}  // namespace flight_hub_sdk
