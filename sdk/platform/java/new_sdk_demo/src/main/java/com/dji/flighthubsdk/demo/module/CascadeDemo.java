// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.AddChildNodeRequest;
import com.dji.flighthubsdk.bean.AddChildNodeResult;
import com.dji.flighthubsdk.bean.CascadeBatchOpResult;
import com.dji.flighthubsdk.bean.CascadeChildNodeListResult;
import com.dji.flighthubsdk.bean.CascadeChildrenQuery;
import com.dji.flighthubsdk.bean.CascadeConnectivityItem;
import com.dji.flighthubsdk.bean.CascadeConnectivityResult;
import com.dji.flighthubsdk.bean.CascadeNodeInfo;
import com.dji.flighthubsdk.bean.CascadeNodeStatusEvent;
import com.dji.flighthubsdk.bean.CascadeNotifyEvent;
import com.dji.flighthubsdk.bean.CascadeParentConnectivity;
import com.dji.flighthubsdk.bean.CascadeParentNodeInfo;
import com.dji.flighthubsdk.bean.CascadeParentRespondRequest;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.CascadeModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cascade（级联管理）Demo — 对齐 v1.1 §1~§11 共 11 个 REST 接口 + 2 个事件通道。
 *
 * <p><b>访问入口：</b>{@code FlightHubSDK.getCascade()}（workspace 级，无需选设备）。
 *
 * <p><b>参数来源：</b>{@code demo_params.json} 的 {@code cascade} 段，键名参见
 * <ul>
 *   <li>{@code provision} — addChildNode 请求体</li>
 *   <li>{@code enableNodes.nodeID} / {@code disableNodes.nodeID} — 批量启用/禁用节点 ID 列表</li>
 *   <li>{@code deleteNode.nodeID} — 删除子节点 ID</li>
 *   <li>{@code children} — getReachableChildren 查询条件（nodeID/page/size/name/status）</li>
 *   <li>{@code parentRespond} — respondToParent 请求体（nodeID/parentID/status）</li>
 *   <li>{@code nodeIndex.page/size} — getNodeIndex 分页</li>
 *   <li>{@code notify.dstNodeID/body} — postToNode 参数（v1.1 起 topic 固定，不由调用方传入）</li>
 * </ul>
 * 联调 mock：{@code scripts/tests/cascade_mock.py}（默认 :18081）。
 *
 * <p>字段命名沿用 v1.1 wire 侧惯例（{@code nodeID} 大写），便于与后端联调对照。
 */
public class CascadeDemo extends ModuleDemo {

    /** cascade 是 workspace 级模块，不需要选设备。 */
    @Override
    public boolean requiresDevice() {
        return false;
    }

    @Override
    public String name() {
        return "CascadeDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式 demo");
            return;
        }
        final CascadeModule module = FlightHubSDK.getCascade();
        if (module == null) {
            hint("FlightHubSDK.getCascade() 返回 null，SDK 未正确初始化");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(mainMenu());
            int choice = Utils.readIntInRange(input, "请选择(1-13, q 退出): ", 1, 13);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出 Cascade 菜单");
                // 兜底：退出前若仍订阅事件，主动取消，避免监听泄漏（本 demo 不走 registry 自动退订）。
                cancelNodeStatusListenIfAny();
                cancelNotifyPayloadListenIfAny();
                break;
            }
            try {
                switch (choice) {
                    case 1: runAddChildNode(module, actionTimeout); break;
                    case 2: runEnableChildNodes(module, actionTimeout); break;
                    case 3: runDisableChildNodes(module, actionTimeout); break;
                    case 4: runDeleteChildNode(module, actionTimeout); break;
                    case 5: runGetReachableChildren(module, actionTimeout); break;
                    case 6: runGetParentNode(module, actionTimeout); break;
                    case 7: runRespondToParent(module, actionTimeout); break;
                    case 8: runGetNodeIndex(module, actionTimeout); break;
                    case 9: runGetConnectivity(module, actionTimeout); break;
                    case 10: runGetParentConnectivity(module, actionTimeout); break;
                    case 11: runPostToNode(module, actionTimeout); break;
                    case 12: runListenNodeStatusChanged(module); break;
                    case 13: runListenNotifyPayload(module); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    private String mainMenu() {
        return "\n  ---- 变更 ----\n"
                + "  1. 新增子节点(配网)               (addChildNode          §1)\n"
                + "  2. 批量启用子节点                 (enableChildNodes      §2)\n"
                + "  3. 批量禁用子节点                 (disableChildNodes     §3)\n"
                + "  4. 删除子节点                     (deleteChildNode       §4)\n"
                + "  ---- 查询 ----\n"
                + "  5. 分页查询可达子节点             (getReachableChildren  §5)\n"
                + "  6. 查询父节点信息                 (getParentNode         §6)\n"
                + "  7. 响应上级组网请求               (respondToParent       §7)\n"
                + "  8. 分页查询组网节点索引           (getNodeIndex          §8)\n"
                + "  ---- 联通 ----\n"
                + "  9. 下级联通性检测                 (getConnectivity       §9)\n"
                + "  10. 上级联通性检测                (getParentConnectivity §10)\n"
                + "  ---- 消息 ----\n"
                + "  11. 向指定节点投递消息            (postToNode            §11)\n"
                + "  ---- 事件 ----\n"
                + "  12. 订阅/取消 节点状态变更        (listenNodeStatusChanged §事件推送)\n"
                + "  13. 订阅/取消 跨节点消息          (listenNotifyPayload     §事件推送)\n"
                + "  q. 退出\n";
    }

    // ==================== §1 addChildNode ====================

    private void runAddChildNode(CascadeModule module, long timeout) {
        Node cfg = cascade().child("provision");
        AddChildNodeRequest req = new AddChildNodeRequest(
                cfg.str("sourceAddr", ""),
                cfg.str("childAddr", ""),
                cfg.str("name", ""),
                cfg.str("code", ""),
                cfg.str("token", "")
        );
        console("   addChildNode: sourceAddr='" + req.getSourceAddr()
                + "' childAddr='" + req.getChildAddr()
                + "' name='" + req.getName() + "' code='" + req.getCode() + "'");
        final Await<FlightHubError> await = new Await<>(name() + ".addChildNode");
        final AtomicReference<AddChildNodeResult> ref = new AtomicReference<>();
        runOutcome("addChildNode", timeout, await, () ->
            module.addChildNode(req, new FlightHubCallback.CascadeAddChildNodeCallback() {
                @Override
                public void onResult(FlightHubError err, AddChildNodeResult result) {
                    ref.set(result);
                    await.set(err);
                }
            })
        );
        AddChildNodeResult r = ref.get();
        if (r != null) {
            console("   new nodeID='" + r.getNodeId() + "' grantID='" + r.getGrantId()
                    + "' parentID='" + r.getParentId() + "' executedBy='" + r.getExecutedBy()
                    + "' enabled=" + r.isEnabled());
        }
    }

    // ==================== §2 enableChildNodes ====================

    private void runEnableChildNodes(CascadeModule module, long timeout) {
        List<String> ids = readNodeIdList(cascade().child("enableNodes"));
        if (ids.isEmpty()) {
            hint("[跳过] cascade.enableNodes.nodeID 为空");
            return;
        }
        console("   enableChildNodes: nodeIDs=" + ids);
        final Await<FlightHubError> await = new Await<>(name() + ".enableChildNodes");
        final AtomicReference<CascadeBatchOpResult> ref = new AtomicReference<>();
        runOutcome("enableChildNodes", timeout, await, () ->
            module.enableChildNodes(ids, new FlightHubCallback.CascadeEnableChildNodesCallback() {
                @Override
                public void onResult(FlightHubError err, CascadeBatchOpResult result) {
                    ref.set(result);
                    await.set(err);
                }
            })
        );
        CascadeBatchOpResult r = ref.get();
        if (r != null) {
            console("   okCount=" + r.getOkCount() + " failCount=" + r.getFailCount());
        }
    }

    // ==================== §3 disableChildNodes ====================

    private void runDisableChildNodes(CascadeModule module, long timeout) {
        List<String> ids = readNodeIdList(cascade().child("disableNodes"));
        if (ids.isEmpty()) {
            hint("[跳过] cascade.disableNodes.nodeID 为空");
            return;
        }
        console("   disableChildNodes: nodeIDs=" + ids);
        final Await<FlightHubError> await = new Await<>(name() + ".disableChildNodes");
        final AtomicReference<CascadeBatchOpResult> ref = new AtomicReference<>();
        runOutcome("disableChildNodes", timeout, await, () ->
            module.disableChildNodes(ids, new FlightHubCallback.CascadeDisableChildNodesCallback() {
                @Override
                public void onResult(FlightHubError err, CascadeBatchOpResult result) {
                    ref.set(result);
                    await.set(err);
                }
            })
        );
        CascadeBatchOpResult r = ref.get();
        if (r != null) {
            console("   okCount=" + r.getOkCount() + " failCount=" + r.getFailCount());
        }
    }

    // ==================== §4 deleteChildNode ====================

    private void runDeleteChildNode(CascadeModule module, long timeout) {
        // 优先取 cascade.deleteNode.nodeID; 兜底可复用 cascade.provision.nodeID(测试同一节点回滚)
        String nodeId = cascade().child("deleteNode").str("nodeID", "");
        if (nodeId == null || nodeId.isEmpty()) {
            nodeId = cascade().child("provision").str("nodeID", "");
        }
        if (nodeId == null || nodeId.isEmpty()) {
            hint("[跳过] cascade.deleteNode.nodeID 未配置");
            return;
        }
        final String targetNodeId = nodeId;
        console("   deleteChildNode: nodeID='" + targetNodeId + "'");
        final Await<FlightHubError> await = new Await<>(name() + ".deleteChildNode");
        runOutcome("deleteChildNode", timeout, await, () ->
            module.deleteChildNode(targetNodeId, new FlightHubCallback.CascadeDeleteChildNodeCallback() {
                @Override
                public void onResult(FlightHubError err) {
                    await.set(err);
                }
            })
        );
    }

    // ==================== §5 getReachableChildren ====================

    private void runGetReachableChildren(CascadeModule module, long timeout) {
        Node cfg = cascade().child("children");
        CascadeChildrenQuery query = new CascadeChildrenQuery(
                cfg.str("nodeID", ""),
                cfg.intVal("page", 0),
                cfg.intVal("size", 0),
                cfg.str("name", ""),
                cfg.str("status", "")
        );
        console("   getReachableChildren: nodeID='" + query.getNodeId()
                + "' page=" + query.getPage() + " size=" + query.getSize()
                + " name='" + query.getName() + "' status='" + query.getStatus() + "'");
        final Await<FlightHubError> await = new Await<>(name() + ".getReachableChildren");
        final AtomicReference<CascadeChildNodeListResult> ref = new AtomicReference<>();
        runOutcome("getReachableChildren", timeout, await, () ->
            module.getReachableChildren(query, new FlightHubCallback.CascadeGetReachableChildrenCallback() {
                @Override
                public void onResult(FlightHubError err, CascadeChildNodeListResult result) {
                    ref.set(result);
                    await.set(err);
                }
            })
        );
        printChildList("getReachableChildren", ref.get());
    }

    // ==================== §6 getParentNode ====================

    private void runGetParentNode(CascadeModule module, long timeout) {
        console("   getParentNode");
        final Await<FlightHubError> await = new Await<>(name() + ".getParentNode");
        final AtomicReference<CascadeParentNodeInfo> ref = new AtomicReference<>();
        runOutcome("getParentNode", timeout, await, () ->
            module.getParentNode(new FlightHubCallback.CascadeGetParentNodeCallback() {
                @Override
                public void onResult(FlightHubError err, CascadeParentNodeInfo info) {
                    ref.set(info);
                    await.set(err);
                }
            })
        );
        CascadeParentNodeInfo p = ref.get();
        if (p != null) {
            console("   parent: nodeID='" + p.getNodeId() + "' name='" + p.getName()
                    + "' online=" + p.getOnline()
                    + " depth=" + p.getDepth() + " enabled=" + p.isEnabled()
                    + " heartbeatPeriod=" + p.getHeartbeatPeriod()
                    + " netStatus='" + p.getNetStatus() + "'");
        }
    }

    // ==================== §7 respondToParent ====================

    private void runRespondToParent(CascadeModule module, long timeout) {
        Node cfg = cascade().child("parentRespond");
        String nodeId = cfg.str("nodeID", "");
        String parentId = cfg.str("parentID", "");
        String status = cfg.str("status", "");
        if (nodeId.isEmpty() || parentId.isEmpty()) {
            hint("[跳过] cascade.parentRespond.nodeID / parentID 未配置");
            return;
        }
        if (!"accept".equals(status) && !"reject".equals(status)) {
            hint("[跳过] cascade.parentRespond.status 需为 accept 或 reject（当前='" + status + "'）");
            return;
        }
        CascadeParentRespondRequest req = new CascadeParentRespondRequest(nodeId, parentId, status);
        console("   respondToParent: nodeID='" + nodeId + "' parentID='" + parentId
                + "' status='" + status + "'");
        final Await<FlightHubError> await = new Await<>(name() + ".respondToParent");
        runOutcome("respondToParent", timeout, await, () ->
            module.respondToParent(req, new FlightHubCallback.CascadeRespondToParentCallback() {
                @Override
                public void onResult(FlightHubError err) {
                    await.set(err);
                }
            })
        );
    }

    // ==================== §8 getNodeIndex ====================

    private void runGetNodeIndex(CascadeModule module, long timeout) {
        Node cfg = cascade().child("nodeIndex");
        int page = cfg.intVal("page", 1);
        int size = cfg.intVal("size", 50);
        console("   getNodeIndex: page=" + page + " size=" + size);
        final Await<FlightHubError> await = new Await<>(name() + ".getNodeIndex");
        final AtomicReference<CascadeChildNodeListResult> ref = new AtomicReference<>();
        runOutcome("getNodeIndex", timeout, await, () ->
            module.getNodeIndex(page, size, new FlightHubCallback.CascadeGetNodeIndexCallback() {
                @Override
                public void onResult(FlightHubError err, CascadeChildNodeListResult result) {
                    ref.set(result);
                    await.set(err);
                }
            })
        );
        printChildList("getNodeIndex", ref.get());
    }

    // ==================== §11 postToNode ====================

    private void runPostToNode(CascadeModule module, long timeout) {
        Node cfg = cascade().child("notify");
        final String dstNodeId = cfg.str("dstNodeID", "");
        final String body = cfg.str("body", "");
        if (dstNodeId.isEmpty()) {
            hint("[跳过] cascade.notify.dstNodeID 未配置");
            return;
        }
        // v1.1：topic 固定为 topic_cascade_notify，由 cascade-svc 侧决定，不再由调用方传入。
        console("   postToNode: dst='" + dstNodeId
                + "' body_len=" + body.length());
        final Await<FlightHubError> await = new Await<>(name() + ".postToNode");
        runOutcome("postToNode", timeout, await, () ->
            module.postToNode(dstNodeId, body, new FlightHubCallback.CascadePostToNodeCallback() {
                @Override
                public void onResult(FlightHubError err) {
                    await.set(err);
                }
            })
        );
    }

    // ==================== §9 getConnectivity ====================

    private void runGetConnectivity(CascadeModule module, long timeout) {
        console("   getConnectivity");
        final Await<FlightHubError> await = new Await<>(name() + ".getConnectivity");
        final AtomicReference<CascadeConnectivityResult> ref = new AtomicReference<>();
        runOutcome("getConnectivity", timeout, await, () ->
            module.getConnectivity(new FlightHubCallback.CascadeGetConnectivityCallback() {
                @Override
                public void onResult(FlightHubError err, CascadeConnectivityResult result) {
                    ref.set(result);
                    await.set(err);
                }
            })
        );
        CascadeConnectivityResult r = ref.get();
        if (r != null && r.getItems() != null) {
            console("   items.size=" + r.getItems().size());
            for (CascadeConnectivityItem it : r.getItems()) {
                console("     nodeID='" + it.getNodeId() + "' name='" + it.getName()
                        + "' reachable=" + it.isReachable() + " rtt=" + it.getRtt());
            }
        }
    }

    // ==================== §10 getParentConnectivity ====================

    private void runGetParentConnectivity(CascadeModule module, long timeout) {
        console("   getParentConnectivity");
        final Await<FlightHubError> await = new Await<>(name() + ".getParentConnectivity");
        final AtomicReference<CascadeParentConnectivity> ref = new AtomicReference<>();
        runOutcome("getParentConnectivity", timeout, await, () ->
            module.getParentConnectivity(new FlightHubCallback.CascadeGetParentConnectivityCallback() {
                @Override
                public void onResult(FlightHubError err, CascadeParentConnectivity result) {
                    ref.set(result);
                    await.set(err);
                }
            })
        );
        CascadeParentConnectivity r = ref.get();
        if (r != null) {
            console("   parent: nodeID='" + r.getNodeId() + "' name='" + r.getName()
                    + "' reachable=" + r.isReachable() + " rtt=" + r.getRtt());
        }
    }

    // ==================== §事件推送 listenNodeStatusChanged ====================

    private void runListenNodeStatusChanged(CascadeModule module) {
        // 已订阅 → 取消；未订阅 → 订阅。toggle 式。
        if (nodeStatusListenId >= 0) {
            console("   取消订阅节点状态变更: listenId=" + nodeStatusListenId);
            try {
                module.cancelListenNodeStatusChanged(nodeStatusListenId);
                console("   结果 -> [成功] 已取消");
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
            } finally {
                nodeStatusListenId = -1;
            }
            return;
        }

        console("   订阅节点状态变更 (topic=" + CascadeNodeStatusEvent.TOPIC + ") ...");
        int listenId = module.listenNodeStatusChanged(
                new FlightHubCallback.CascadeNodeStatusEventCallback() {
            @Override
            public void onResult(CascadeNodeStatusEvent event) {
                // SDK 回调在 native 线程：只打印，不做重逻辑（遵循手册 §11.7）。
                if (event == null) {
                    console("   [推送] event=null");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("   [推送] changedAt=").append(event.getChangedAt());
                CascadeNodeStatusEvent.ParentStatus p = event.getParentNodeStatus();
                if (p != null) {
                    sb.append(" parent{nodeID='").append(p.getNodeId())
                      .append("', status='").append(p.getStatus()).append("'}");
                }
                if (event.getChildNodeStatusList() != null) {
                    sb.append(" children=").append(event.getChildNodeStatusList().size()).append(":");
                    for (CascadeNodeStatusEvent.ChildStatus c : event.getChildNodeStatusList()) {
                        sb.append("\n     child{nodeID='").append(c.getNodeId())
                          .append("', status='").append(c.getStatus()).append("'}");
                    }
                }
                console(sb.toString());
            }
        });
        if (listenId < 0) {
            console("   结果 -> [失败] listenId=" + listenId
                    + "（Notify 通道未连接或订阅失败，检查 notification_server 配置与连通性）");
            return;
        }
        nodeStatusListenId = listenId;
        console("   结果 -> [成功] 已订阅 listenId=" + listenId
                + "，等待推送...（再选 12 取消订阅）");
    }

    /** 退出菜单兜底：若仍订阅节点状态，主动取消，防泄漏。 */
    private void cancelNodeStatusListenIfAny() {
        if (nodeStatusListenId >= 0) {
            try {
                FlightHubSDK.getCascade().cancelListenNodeStatusChanged(nodeStatusListenId);
                DemoLog.info(name(), "退出兜底：已取消节点状态监听 listenId=" + nodeStatusListenId);
            } catch (Throwable t) {
                DemoLog.warn(name(), "退出兜底：取消节点状态监听异常: " + Utils.describe(t));
            } finally {
                nodeStatusListenId = -1;
            }
        }
    }

    // ==================== §事件推送 listenNotifyPayload ====================

    private void runListenNotifyPayload(CascadeModule module) {
        // 已订阅 → 取消；未订阅 → 订阅。toggle 式。
        if (notifyPayloadListenId >= 0) {
            console("   取消订阅跨节点消息: listenId=" + notifyPayloadListenId);
            try {
                module.cancelListenNotifyPayload(notifyPayloadListenId);
                console("   结果 -> [成功] 已取消");
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
            } finally {
                notifyPayloadListenId = -1;
            }
            return;
        }

        console("   订阅跨节点通道消息 (topic=" + CascadeNotifyEvent.TOPIC + ") ...");
        int listenId = module.listenNotifyPayload(
                new FlightHubCallback.CascadeNotifyEventCallback() {
            @Override
            public void onResult(CascadeNotifyEvent event) {
                // SDK 回调在 native 线程：只打印，不做重逻辑（遵循手册 §11.7）。
                if (event == null) {
                    console("   [推送] event=null");
                    return;
                }
                String payload = event.getPayload();
                console("   [推送] payload_len=" + payload.length()
                        + " head='" + (payload.length() > 80 ? payload.substring(0, 80) + "..." : payload) + "'");
            }
        });
        if (listenId < 0) {
            console("   结果 -> [失败] listenId=" + listenId
                    + "（Notify 通道未连接或订阅失败，检查 notification_server 配置与连通性）");
            return;
        }
        notifyPayloadListenId = listenId;
        console("   结果 -> [成功] 已订阅 listenId=" + listenId
                + "，等待推送...（再选 13 取消订阅）");
    }

    /** 退出菜单兜底：若仍订阅跨节点消息，主动取消，防泄漏。 */
    private void cancelNotifyPayloadListenIfAny() {
        if (notifyPayloadListenId >= 0) {
            try {
                FlightHubSDK.getCascade().cancelListenNotifyPayload(notifyPayloadListenId);
                DemoLog.info(name(), "退出兜底：已取消跨节点消息监听 listenId=" + notifyPayloadListenId);
            } catch (Throwable t) {
                DemoLog.warn(name(), "退出兜底：取消跨节点消息监听异常: " + Utils.describe(t));
            } finally {
                notifyPayloadListenId = -1;
            }
        }
    }

    // ==================== 辅助 ====================

    /** 节点状态监听句柄；-1 表示未订阅。本 demo 为 workspace 级（requiresDevice=false，无 session/registry），
     *  故监听句柄由本类字段持有 + 手动 cancel，不走 SubscriptionRegistry。 */
    private int nodeStatusListenId = -1;

    /** 跨节点消息监听句柄；-1 表示未订阅。同上，本 demo 手动管理。 */
    private int notifyPayloadListenId = -1;

    /** 读取 cascade 段。 */
    private Node cascade() {
        return params.node("cascade");
    }

    /** 从子节点读 nodeID 数组(键名 "nodeID", 与 wire 侧对齐)。 */
    private List<String> readNodeIdList(Node group) {
        List<Object> raw = group.list("nodeID");
        List<String> ids = new ArrayList<>();
        for (Object o : raw) {
            if (o != null) {
                String s = o.toString();
                if (!s.isEmpty()) ids.add(s);
            }
        }
        return ids;
    }

    private void printChildList(String label, CascadeChildNodeListResult result) {
        if (result == null) return;
        console("   " + label + ": total=" + result.getPage().getTotal()
                + " page=" + result.getPage().getPage() + " size=" + result.getPage().getSize()
                + " list.size=" + result.getList().size());
        for (CascadeNodeInfo n : result.getList()) {
            console("     nodeID='" + n.getNodeId() + "' name='" + n.getName()
                    + "' parentID='" + n.getParentId() + "' online=" + n.getOnline()
                    + " enabled=" + n.isEnabled()
                    + " address='" + n.getAddress() + "'"
                    + " networked=" + n.isNetworked()
                    + " netStatus='" + n.getNetStatus() + "'");
        }
    }

    protected void console(String line) { System.out.println(line); }

    protected void hint(String msg) { System.out.println("   >> " + msg); }
}
