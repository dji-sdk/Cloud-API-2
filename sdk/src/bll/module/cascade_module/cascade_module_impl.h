// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/cascade_module.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"
#include <memory>

namespace flight_hub_sdk {

class CascadeModuleImpl : public ICascadeModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    CascadeModuleImpl(int session_id);
    ~CascadeModuleImpl() override;

    void addChildNode(const AddChildNodeRequest& req,
        std::function<void(FlightHubError err, const AddChildNodeResult& result)> callback) override;

    void enableChildNodes(const std::vector<std::string>& node_ids,
        std::function<void(FlightHubError err, const CascadeBatchOpResult& result)> callback) override;

    void disableChildNodes(const std::vector<std::string>& node_ids,
        std::function<void(FlightHubError err, const CascadeBatchOpResult& result)> callback) override;

    void deleteChildNode(const std::string& node_id,
        std::function<void(FlightHubError err)> callback) override;

    void getReachableChildren(const CascadeChildrenQuery& query,
        std::function<void(FlightHubError err, const CascadeChildNodeListResult& result)> callback) override;

    void getParentNode(
        std::function<void(FlightHubError err, const CascadeParentNodeInfo& info)> callback) override;

    void respondToParent(const CascadeParentRespondRequest& req,
        std::function<void(FlightHubError err)> callback) override;

    void getNodeIndex(int32_t page, int32_t size,
        std::function<void(FlightHubError err, const CascadeChildNodeListResult& result)> callback) override;

    void getConnectivity(
        std::function<void(FlightHubError err, const CascadeConnectivityResult& result)> callback) override;

    void getParentConnectivity(
        std::function<void(FlightHubError err, const CascadeParentConnectivity& result)> callback) override;

    void postToNode(const std::string& dst_node_id, const std::string& body,
        std::function<void(FlightHubError err)> callback) override;

    int listenNodeStatusChanged(
        std::function<void(const CascadeNodeStatusEvent& event)> callback) override;
    void cancelListenNodeStatusChanged(int listen_id) override;

    int listenNotifyPayload(
        std::function<void(const CascadeNotifyEvent& event)> callback) override;
    void cancelListenNotifyPayload(int listen_id) override;

private:
    int session_id_;
    // v1.1 §事件推送：topic_cascade_node_status 多订阅者扇出。
    // UseCache=false —— 事件体是全量快照但 changedAt/节点集合可能高频抖动，
    // 缓存命中无意义且会向新订阅者补发一帧历史快照，与「实时状态」语义冲突。
    CallbackContainer<CascadeNodeStatusEvent, false> node_status_callbacks_;
    int64_t node_status_listen_id_ = -1;

    // v1.1 §跨节点通道消息：topic_cascade_notify 多订阅者扇出。
    // UseCache=false —— payload 是一次性透传消息，历史帧对新订阅者无意义。
    CallbackContainer<CascadeNotifyEvent, false> notify_payload_callbacks_;
    int64_t notify_payload_listen_id_ = -1;
};

}  // namespace flight_hub_sdk
