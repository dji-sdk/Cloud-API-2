// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.ICascadeModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 级联管理模块（司空 Mesh 级联，v1.1 文档：docs/superpowers/specs/级联接口文档-v1.1.md）。
 *
 * <p>对齐 v1.1 的 11 个 HTTP 接口：
 * <ol>
 *   <li>{@link #addChildNode} — POST /gateway/cascade/api/provision</li>
 *   <li>{@link #enableChildNodes} — POST /gateway/cascade/api/nodes/enable</li>
 *   <li>{@link #disableChildNodes} — POST /gateway/cascade/api/nodes/disable</li>
 *   <li>{@link #deleteChildNode} — DELETE /gateway/cascade/api/node/&#123;nodeID&#125;</li>
 *   <li>{@link #getReachableChildren} — GET /gateway/cascade/api/children</li>
 *   <li>{@link #getParentNode} — GET /gateway/cascade/api/parent</li>
 *   <li>{@link #respondToParent} — POST /gateway/cascade/api/parent/respond</li>
 *   <li>{@link #getNodeIndex} — GET /gateway/cascade/api/nodes/index</li>
 *   <li>{@link #getConnectivity} — GET /gateway/cascade/api/connectivity</li>
 *   <li>{@link #getParentConnectivity} — GET /gateway/cascade/api/parent/connectivity</li>
 *   <li>{@link #postToNode} — POST /gateway/cascade/api/notify/&#123;dstNode&#125;</li>
 * </ol>
 *
 * <p>另有两个事件通道：{@link #listenNodeStatusChanged} 订阅
 * {@code topic_cascade_node_status}（节点状态变更），{@link #listenNotifyPayload} 订阅
 * {@code topic_cascade_notify}（跨节点通道消息，与 postToNode 语义对称）。
 *
 * <p>访问入口：{@code com.dji.flighthubsdk.FlightHubSDK.getCascade()}（workspace 级单例）。
 * 每次异步调用生成的 SWIG 回调对象会以 {@code CallbackRefContainer} 强引用形式登记进
 * {@link ConcurrentHashMap}，避免 JVM 在 native 回调返回前将其回收（PITFALLS S1）。
 */
public class CascadeModule {

    private class CallbackRefContainer {
        private Object callback_jni_ref;
        private Object callback_java_ref;

        public CallbackRefContainer(Object callback_jni_ref, Object callback_java_ref) {
            this.callback_jni_ref = callback_jni_ref;
            this.callback_java_ref = callback_java_ref;
        }

        public void release() {
            if (callback_jni_ref != null) {
                try {
                    callback_jni_ref.getClass().getMethod("delete").invoke(callback_jni_ref);
                } catch (Exception ignored) {
                }
                callback_jni_ref = null;
            }
        }
    }

    private final ConcurrentHashMap<Integer, CallbackRefContainer> addChildNodeRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> enableChildNodesRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> disableChildNodesRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> deleteChildNodeRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getReachableChildrenRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getParentNodeRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> respondToParentRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getNodeIndexRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getConnectivityRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getParentConnectivityRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> postToNodeRef = new ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final ICascadeModule cascadeModule;

    /** 构造 CascadeModule。 */
    public CascadeModule(ICascadeModule cascadeModule) {
        this.cascadeModule = cascadeModule;
    }

    private static com.dji.flighthubsdk.bean.FlightHubError fallbackError() {
        return new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED);
    }

    /**
     * §1 新增子节点（配网 / provision）。
     *
     * @param req      配网参数：sourceAddr / childAddr / name / code / token
     * @param callback 结果回调（err + AddChildNodeResult：nodeID/grantID/parentID/executedBy/enabled）
     * @implNote 链路: HTTP | POST /gateway/cascade/api/provision
     */
    public void addChildNode(com.dji.flighthubsdk.bean.AddChildNodeRequest req,
                             FlightHubCallback.CascadeAddChildNodeCallback callback) {
        com.dji.flighthubsdk.generated.AddChildNodeRequest nativeReq = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeAddChildNodeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeAddChildNodeCallback() {
                @Override
                public void onCascadeAddChildNode(com.dji.flighthubsdk.generated.FlightHubError err,
                                                  com.dji.flighthubsdk.generated.AddChildNodeResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(result));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[addChildNode] callback error: " + e.getMessage(), e);
                    } finally {
                        addChildNodeRef.remove(id);
                    }
                }
            };
            nativeReq = FlightHubSdkConvert.toNative(req);
            synchronized (addChildNodeRef) {
                addChildNodeRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.addChildNodeWrapper(nativeReq, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[addChildNode] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.AddChildNodeResult) null));
            }
        } finally {
            if (nativeReq != null) {
                nativeReq.delete();
            }
        }
    }

    /**
     * §2 批量启用子节点。
     *
     * @param nodeIds  子节点 nodeID 列表
     * @param callback 结果回调（err + CascadeBatchOpResult：okCount/failCount）
     * @implNote 链路: HTTP | POST /gateway/cascade/api/nodes/enable
     */
    public void enableChildNodes(java.util.List<String> nodeIds,
                                 FlightHubCallback.CascadeEnableChildNodesCallback callback) {
        com.dji.flighthubsdk.generated.StringVector nativeIds = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeEnableChildNodesCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeEnableChildNodesCallback() {
                @Override
                public void onCascadeEnableChildNodes(com.dji.flighthubsdk.generated.FlightHubError err,
                                                      com.dji.flighthubsdk.generated.CascadeBatchOpResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(result));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[enableChildNodes] callback error: " + e.getMessage(), e);
                    } finally {
                        enableChildNodesRef.remove(id);
                    }
                }
            };
            nativeIds = FlightHubSdkConvert.toNodeIdVector(nodeIds);
            synchronized (enableChildNodesRef) {
                enableChildNodesRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.enableChildNodesWrapper(nativeIds, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[enableChildNodes] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.CascadeBatchOpResult) null));
            }
        } finally {
            if (nativeIds != null) {
                nativeIds.delete();
            }
        }
    }

    /**
     * §3 批量禁用子节点。
     *
     * @param nodeIds  子节点 nodeID 列表
     * @param callback 结果回调（err + CascadeBatchOpResult：okCount/failCount）
     * @implNote 链路: HTTP | POST /gateway/cascade/api/nodes/disable
     */
    public void disableChildNodes(java.util.List<String> nodeIds,
                                  FlightHubCallback.CascadeDisableChildNodesCallback callback) {
        com.dji.flighthubsdk.generated.StringVector nativeIds = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeDisableChildNodesCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeDisableChildNodesCallback() {
                @Override
                public void onCascadeDisableChildNodes(com.dji.flighthubsdk.generated.FlightHubError err,
                                                       com.dji.flighthubsdk.generated.CascadeBatchOpResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(result));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[disableChildNodes] callback error: " + e.getMessage(), e);
                    } finally {
                        disableChildNodesRef.remove(id);
                    }
                }
            };
            nativeIds = FlightHubSdkConvert.toNodeIdVector(nodeIds);
            synchronized (disableChildNodesRef) {
                disableChildNodesRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.disableChildNodesWrapper(nativeIds, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[disableChildNodes] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.CascadeBatchOpResult) null));
            }
        } finally {
            if (nativeIds != null) {
                nativeIds.delete();
            }
        }
    }

    /**
     * §4 删除子节点（deprovision）。
     *
     * @param nodeId   要删除的子节点 nodeID
     * @param callback 结果回调（err）
     * @implNote 链路: HTTP | DELETE /gateway/cascade/api/node/{nodeID}
     */
    public void deleteChildNode(String nodeId, FlightHubCallback.CascadeDeleteChildNodeCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeDeleteChildNodeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeDeleteChildNodeCallback() {
                @Override
                public void onCascadeDeleteChildNode(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[deleteChildNode] callback error: " + e.getMessage(), e);
                    } finally {
                        deleteChildNodeRef.remove(id);
                    }
                }
            };
            synchronized (deleteChildNodeRef) {
                deleteChildNodeRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.deleteChildNodeWrapper(nodeId == null ? "" : nodeId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[deleteChildNode] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError());
            }
        }
    }

    /**
     * §5 查询可达子节点列表（跨层级；权威子孙由 cascade-svc 计算）。
     *
     * @param query    查询条件：nodeID / page / size / name / status（分页可选）
     * @param callback 结果回调（err + CascadeChildNodeListResult：list + page）
     * @implNote 链路: HTTP | GET /gateway/cascade/api/children
     */
    public void getReachableChildren(com.dji.flighthubsdk.bean.CascadeChildrenQuery query,
                                     FlightHubCallback.CascadeGetReachableChildrenCallback callback) {
        com.dji.flighthubsdk.generated.CascadeChildrenQuery nativeQuery = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeGetReachableChildrenCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeGetReachableChildrenCallback() {
                @Override
                public void onCascadeGetReachableChildren(com.dji.flighthubsdk.generated.FlightHubError err,
                                                          com.dji.flighthubsdk.generated.CascadeChildNodeListResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(result));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[getReachableChildren] callback error: " + e.getMessage(), e);
                    } finally {
                        getReachableChildrenRef.remove(id);
                    }
                }
            };
            nativeQuery = FlightHubSdkConvert.toNative(query);
            if (nativeQuery == null) {
                nativeQuery = new com.dji.flighthubsdk.generated.CascadeChildrenQuery();
            }
            synchronized (getReachableChildrenRef) {
                getReachableChildrenRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.getReachableChildrenWrapper(nativeQuery, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[getReachableChildren] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.CascadeChildNodeListResult) null));
            }
        } finally {
            if (nativeQuery != null) {
                nativeQuery.delete();
            }
        }
    }

    /**
     * §6 获取父节点信息。
     *
     * @param callback 结果回调（err + CascadeParentNodeInfo：name/online/depth/enabled/heartbeatPeriod）
     * @implNote 链路: HTTP | GET /gateway/cascade/api/parent
     */
    public void getParentNode(FlightHubCallback.CascadeGetParentNodeCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeGetParentNodeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeGetParentNodeCallback() {
                @Override
                public void onCascadeGetParentNode(com.dji.flighthubsdk.generated.FlightHubError err,
                                                   com.dji.flighthubsdk.generated.CascadeParentNodeInfo info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(info));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[getParentNode] callback error: " + e.getMessage(), e);
                    } finally {
                        getParentNodeRef.remove(id);
                    }
                }
            };
            synchronized (getParentNodeRef) {
                getParentNodeRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.getParentNodeWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[getParentNode] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.CascadeParentNodeInfo) null));
            }
        }
    }

    /**
     * §7 响应上级平台组网请求（接受 / 拒绝）。
     *
     * <p>SDK 侧不做 status 校验（{@code "accept"} / {@code "reject"} 之外的取值），
     * 由 cascade-svc 返回 4001 参数错误。
     *
     * @param req      请求：nodeID / parentID / status
     * @param callback 结果回调（err；成功时后端返回 {@code {"ok": true}}）
     * @implNote 链路: HTTP | POST /gateway/cascade/api/parent/respond
     */
    public void respondToParent(com.dji.flighthubsdk.bean.CascadeParentRespondRequest req,
                                FlightHubCallback.CascadeRespondToParentCallback callback) {
        com.dji.flighthubsdk.generated.CascadeParentRespondRequest nativeReq = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeRespondToParentCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeRespondToParentCallback() {
                @Override
                public void onCascadeRespondToParent(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[respondToParent] callback error: " + e.getMessage(), e);
                    } finally {
                        respondToParentRef.remove(id);
                    }
                }
            };
            nativeReq = FlightHubSdkConvert.toNative(req);
            if (nativeReq == null) {
                nativeReq = new com.dji.flighthubsdk.generated.CascadeParentRespondRequest();
            }
            synchronized (respondToParentRef) {
                respondToParentRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.respondToParentWrapper(nativeReq, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[respondToParent] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError());
            }
        } finally {
            if (nativeReq != null) {
                nativeReq.delete();
            }
        }
    }

    /**
     * §8 查询组网节点索引（供 UI 展示；SDK 不做可见性裁剪）。
     *
     * @param page     页码，1-based
     * @param size     每页条数
     * @param callback 结果回调（err + CascadeChildNodeListResult：list + page）
     * @implNote 链路: HTTP | GET /gateway/cascade/api/nodes/index
     */
    public void getNodeIndex(int page, int size, FlightHubCallback.CascadeGetNodeIndexCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeGetNodeIndexCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeGetNodeIndexCallback() {
                @Override
                public void onCascadeGetNodeIndex(com.dji.flighthubsdk.generated.FlightHubError err,
                                                  com.dji.flighthubsdk.generated.CascadeChildNodeListResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(result));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[getNodeIndex] callback error: " + e.getMessage(), e);
                    } finally {
                        getNodeIndexRef.remove(id);
                    }
                }
            };
            synchronized (getNodeIndexRef) {
                getNodeIndexRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.getNodeIndexWrapper(page, size, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[getNodeIndex] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.CascadeChildNodeListResult) null));
            }
        }
    }

    /**
     * §9 联通性检测（v1.1 新增）。遍历本节点的下级平台，逐个 Mesh Route+Accessible 探活。
     *
     * @param callback 结果回调（err + CascadeConnectivityResult：items[nodeID/name/reachable/rtt]）
     * @implNote 链路: HTTP | GET /gateway/cascade/api/connectivity
     */
    public void getConnectivity(FlightHubCallback.CascadeGetConnectivityCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeGetConnectivityCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeGetConnectivityCallback() {
                @Override
                public void onCascadeGetConnectivity(com.dji.flighthubsdk.generated.FlightHubError err,
                                                     com.dji.flighthubsdk.generated.CascadeConnectivityResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(result));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[getConnectivity] callback error: " + e.getMessage(), e);
                    } finally {
                        getConnectivityRef.remove(id);
                    }
                }
            };
            synchronized (getConnectivityRef) {
                getConnectivityRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.getConnectivityWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[getConnectivity] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.CascadeConnectivityResult) null));
            }
        }
    }

    /**
     * §10 上级平台联通性检测（v1.1 新增）。查询 Mesh 获取上级平台信息再 Route+Accessible 探活；
     * 正常只有一个上级平台。
     *
     * @param callback 结果回调（err + CascadeParentConnectivity：nodeID/name/reachable/rtt；
     *                 无上级/总部时 nodeID 为空）
     * @implNote 链路: HTTP | GET /gateway/cascade/api/parent/connectivity
     */
    public void getParentConnectivity(FlightHubCallback.CascadeGetParentConnectivityCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadeGetParentConnectivityCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeGetParentConnectivityCallback() {
                @Override
                public void onCascadeGetParentConnectivity(com.dji.flighthubsdk.generated.FlightHubError err,
                                                           com.dji.flighthubsdk.generated.CascadeParentConnectivity result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.fromNative(result));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule",
                                "[getParentConnectivity] callback error: " + e.getMessage(), e);
                    } finally {
                        getParentConnectivityRef.remove(id);
                    }
                }
            };
            synchronized (getParentConnectivityRef) {
                getParentConnectivityRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.getParentConnectivityWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[getParentConnectivity] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError(), FlightHubSdkConvert.fromNative(
                        (com.dji.flighthubsdk.generated.CascadeParentConnectivity) null));
            }
        }
    }

    /**
     * §11 向指定子节点投递消息（notify）。
     *
     * <p>v1.1 变更：{@code topic} 固定为 {@code topic_cascade_notify}，由 cascade-svc 侧决定，
     * SDK 不再接受用户传入 topic；body 为任意字节透传（不解析、不重试），> 1MB 由后端拒。
     *
     * @param dstNodeId 目标节点 nodeID
     * @param body      业务字节 body（原样透传）
     * @param callback  结果回调（err）
     * @implNote 链路: HTTP | POST /gateway/cascade/api/notify/{dstNode}
     */
    public void postToNode(String dstNodeId, String body,
                           FlightHubCallback.CascadePostToNodeCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CascadePostToNodeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadePostToNodeCallback() {
                @Override
                public void onCascadePostToNode(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule", "[postToNode] callback error: " + e.getMessage(), e);
                    } finally {
                        postToNodeRef.remove(id);
                    }
                }
            };
            synchronized (postToNodeRef) {
                postToNodeRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cascadeModule.postToNodeWrapper(
                        dstNodeId == null ? "" : dstNodeId,
                        body == null ? "" : body,
                        swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CascadeModule", "[postToNode] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(fallbackError());
            }
        }
    }

    /**
     * 订阅级联节点状态变更事件（v1.1 事件推送 topic {@code topic_cascade_node_status}）。
     *
     * <p>事件体为全量快照，幂等自愈；状态派生优先级 {@code deleted > offline > disabled > normal}。
     * 支持多订阅者：每次调用返回独立 listenId，用 {@link #cancelListenNodeStatusChanged(int)} 注销。
     *
     * @param callback 事件回调
     * @return listenId（≥0 成功；&lt;0 失败，例如 notify 通道未连接）
     * @implNote 链路: Notify | topic topic_cascade_node_status
     */
    public int listenNodeStatusChanged(FlightHubCallback.CascadeNodeStatusEventCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CascadeNodeStatusEventCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeNodeStatusEventCallback() {
                @Override
                public void onCascadeNodeStatusEvent(
                        com.dji.flighthubsdk.generated.CascadeNodeStatusEvent event) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.fromNative(event));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule",
                                "[listenNodeStatusChanged] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.cascadeModule.listenNodeStatusChangedWrapper(swigCallback);
            // 多订阅者场景：director 生命周期随 listenId，需把所有权移交 native 侧
            // （C++ impl 用 shared_ptr 持有 callback 直至 cancelListen），否则 SWIG 默认
            // 会把 director 的 ownership 留在 Java，回调触发时易被 GC 回收。
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CascadeModule",
                    "[listenNodeStatusChanged] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅级联节点状态变更事件。重复取消安全（幂等）。
     *
     * @param listenId {@link #listenNodeStatusChanged} 返回的句柄
     */
    public void cancelListenNodeStatusChanged(int listenId) {
        try {
            this.cascadeModule.cancelListenNodeStatusChangedWrapper(listenId);
        } catch (Exception e) {
            Logger.error("CascadeModule",
                    "[cancelListenNodeStatusChanged] error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅跨节点通道消息事件（v1.1 事件推送 topic {@code topic_cascade_notify}）。
     *
     * <p>payload 为发送侧 {@link #postToNode} 时透传的原始 body（SDK 不解析）。
     * 支持多订阅者：每次调用返回独立 listenId，用 {@link #cancelListenNotifyPayload(int)} 注销。
     *
     * @param callback 事件回调
     * @return listenId（≥0 成功；&lt;0 失败，例如 notify 通道未连接）
     * @implNote 链路: Notify | topic topic_cascade_notify
     */
    public int listenNotifyPayload(FlightHubCallback.CascadeNotifyEventCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CascadeNotifyEventCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CascadeNotifyEventCallback() {
                @Override
                public void onCascadeNotifyEvent(
                        com.dji.flighthubsdk.generated.CascadeNotifyEvent event) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.fromNative(event));
                        }
                    } catch (Exception e) {
                        Logger.error("CascadeModule",
                                "[listenNotifyPayload] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.cascadeModule.listenNotifyPayloadWrapper(swigCallback);
            // 多订阅者场景：director 生命周期随 listenId，需把所有权移交 native 侧
            // （C++ impl 用 shared_ptr 持有 callback 直至 cancelListen），否则 SWIG 默认
            // 会把 director 的 ownership 留在 Java，回调触发时易被 GC 回收。
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CascadeModule",
                    "[listenNotifyPayload] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅跨节点通道消息事件。重复取消安全（幂等）。
     *
     * @param listenId {@link #listenNotifyPayload} 返回的句柄
     */
    public void cancelListenNotifyPayload(int listenId) {
        try {
            this.cascadeModule.cancelListenNotifyPayloadWrapper(listenId);
        } catch (Exception e) {
            Logger.error("CascadeModule",
                    "[cancelListenNotifyPayload] error: " + e.getMessage(), e);
        }
    }
}
