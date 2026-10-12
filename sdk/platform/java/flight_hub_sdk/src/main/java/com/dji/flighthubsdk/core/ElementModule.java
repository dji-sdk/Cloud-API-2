// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.*;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 元素管理模块（ElementService）
 * 提供要素 CRUD、显示状态管理、以及 WS 订阅接口。
 */
public final class ElementModule {

    private static final String TAG = "ElementModule";

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

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() { return id_counter++; }

    private final IElementModule elementModule;

    // ── callback ref maps ──────────────────────────────────────────────────
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createCallbackRef         = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateCallbackRef         = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listCallbackRef           = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setVisibilityCallbackRef  = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deleteCallbackRef         = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenCreateCallbackRef   = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenUpdateCallbackRef   = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenDeleteCallbackRef   = new java.util.concurrent.ConcurrentHashMap<>();

    public ElementModule(IElementModule module) {
        this.elementModule = module;
        Logger.info(TAG, "ElementModule instance created");
    }

    // ── HTTP 接口 ────────────────────────────────────────────────────────────

    /**
     * 创建要素
     * @param params   创建参数（resourceType 必填，resourceContent 需填充几何数据）
     * @param callback 结果回调
     */
    public void createElement(
            com.dji.flighthubsdk.bean.ElementCreateParams params,
            FlightHubCallback.ElementCreateElementCallback callback) {
        com.dji.flighthubsdk.generated.CreateElementParams nativeParams = null;
        try {
            int cbId = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ElementCreateElementCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementCreateElementCallback() {
                        @Override
                        public void onCreateElement(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.CreateElementResult r) {
                            try {
                                if (callback != null) callback.onResult(
                                        FlightHubSdkConvert.toFlightHubError(err),
                                        FlightHubSdkConvert.toElementCreateResult(r));
                            } catch (Exception e) {
                                Logger.error(TAG, "[createElement] cb error: " + e.getMessage(), e);
                            } finally { createCallbackRef.remove(cbId); }
                        }
                    };
            nativeParams = FlightHubSdkConvert.toNativeCreateElementParams(params);
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (createCallbackRef) {
                createCallbackRef.put(cbId, container);
                elementModule.createElementWrapper(nativeParams, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createElement] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 更新要素属性
     * @param params   更新参数（id 必填，其余字段可选）
     * @param callback 结果回调
     */
    public void updateElement(
            com.dji.flighthubsdk.bean.ElementUpdateParams params,
            FlightHubCallback.ElementUpdateElementCallback callback) {
        com.dji.flighthubsdk.generated.ElementUpdateJavaParams nativeParams = null;
        try {
            int cbId = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ElementUpdateElementCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementUpdateElementCallback() {
                        @Override
                        public void onUpdateElement(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.UpdateElementResult r) {
                            try {
                                if (callback != null) callback.onResult(
                                        FlightHubSdkConvert.toFlightHubError(err),
                                        FlightHubSdkConvert.toElementUpdateResult(r));
                            } catch (Exception e) {
                                Logger.error(TAG, "[updateElement] cb error: " + e.getMessage(), e);
                            } finally { updateCallbackRef.remove(cbId); }
                        }
                    };
            nativeParams = FlightHubSdkConvert.toNativeElementUpdateParams(params);
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (updateCallbackRef) {
                updateCallbackRef.put(cbId, container);
                elementModule.updateElementWrapper(nativeParams, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[updateElement] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 查询元素层级列表（目录树）
     * @param params   查询参数
     * @param callback 结果回调
     */
    public void listElementHierarchy(
            com.dji.flighthubsdk.bean.ElementListHierarchyParams params,
            FlightHubCallback.ElementListHierarchyCallback callback) {
        com.dji.flighthubsdk.generated.ListElementHierarchyJavaParams nativeParams = null;
        try {
            int cbId = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ElementListHierarchyCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementListHierarchyCallback() {
                        @Override
                        public void onListElementHierarchy(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.ListElementHierarchyResult r) {
                            try {
                                if (callback != null) callback.onResult(
                                        FlightHubSdkConvert.toFlightHubError(err),
                                        FlightHubSdkConvert.toElementListHierarchyResult(r));
                            } catch (Exception e) {
                                Logger.error(TAG, "[listElementHierarchy] cb error: " + e.getMessage(), e);
                            } finally { listCallbackRef.remove(cbId); }
                        }
                    };
            nativeParams = FlightHubSdkConvert.toNativeListElementHierarchyParams(params);
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (listCallbackRef) {
                listCallbackRef.put(cbId, container);
                elementModule.listElementHierarchyWrapper(nativeParams, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[listElementHierarchy] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 设置要素显示/隐藏状态
     * @param params   参数（id + display 必填）
     * @param callback 结果回调
     */
    public void setElementVisibility(
            com.dji.flighthubsdk.bean.ElementSetVisibilityParams params,
            FlightHubCallback.ElementSetVisibilityCallback callback) {
        com.dji.flighthubsdk.generated.SetElementVisibilityParams nativeParams = null;
        try {
            int cbId = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ElementSetVisibilityCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementSetVisibilityCallback() {
                        @Override
                        public void onSetElementVisibility(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.SetElementVisibilityResult r) {
                            try {
                                if (callback != null) callback.onResult(
                                        FlightHubSdkConvert.toFlightHubError(err),
                                        FlightHubSdkConvert.toElementSetVisibilityResult(r));
                            } catch (Exception e) {
                                Logger.error(TAG, "[setElementVisibility] cb error: " + e.getMessage(), e);
                            } finally { setVisibilityCallbackRef.remove(cbId); }
                        }
                    };
            nativeParams = FlightHubSdkConvert.toNativeSetElementVisibilityParams(params);
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (setVisibilityCallbackRef) {
                setVisibilityCallbackRef.put(cbId, container);
                elementModule.setElementVisibilityWrapper(nativeParams, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[setElementVisibility] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 删除要素（仅限基础几何类型：点/线/面/圆）
     * @param params   参数（id 必填）
     * @param callback 结果回调
     */
    public void deleteElement(
            com.dji.flighthubsdk.bean.ElementDeleteParams params,
            FlightHubCallback.ElementDeleteElementCallback callback) {
        com.dji.flighthubsdk.generated.DeleteElementParams nativeParams = null;
        try {
            int cbId = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ElementDeleteElementCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementDeleteElementCallback() {
                        @Override
                        public void onDeleteElement(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.DeleteElementResult r) {
                            try {
                                if (callback != null) callback.onResult(
                                        FlightHubSdkConvert.toFlightHubError(err),
                                        FlightHubSdkConvert.toElementDeleteResult(r));
                            } catch (Exception e) {
                                Logger.error(TAG, "[deleteElement] cb error: " + e.getMessage(), e);
                            } finally { deleteCallbackRef.remove(cbId); }
                        }
                    };
            nativeParams = FlightHubSdkConvert.toNativeDeleteElementParams(params);
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (deleteCallbackRef) {
                deleteCallbackRef.put(cbId, container);
                elementModule.deleteElementWrapper(nativeParams, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteElement] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    // ── WS 订阅接口 ────────────────────────────────────────────────────────────

    /**
     * 订阅要素创建事件
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_element_create
     */
    public int listenElementCreate(FlightHubCallback.ElementCreateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.ElementCreateCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementCreateCallback() {
                        @Override
                        public void onElementCreate(com.dji.flighthubsdk.generated.ElementCreateMsg msg) {
                            try {
                                if (callback != null) callback.onResult(FlightHubSdkConvert.toElementCreateMsg(msg));
                            } catch (Exception e) { Logger.error(TAG, "[listenElementCreate] cb error: " + e.getMessage(), e); }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (listenCreateCallbackRef) {
                listenCreateCallbackRef.put(-1, container);
                int listenId = elementModule.listenElementCreateWrapper(swigCb);
                listenCreateCallbackRef.remove(-1);
                if (listenId >= 0) { listenCreateCallbackRef.put(listenId, container); } else { container.release(); }
                Logger.info(TAG, "listenElementCreate listenId=" + listenId);
                return listenId;
            }
        } catch (Exception e) { Logger.error(TAG, "[listenElementCreate] error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅要素创建事件 */
    public void cancelListenElementCreate(int listenId) {
        try { elementModule.cancelListenElementCreateWrapper(listenId); } catch (Exception e) { Logger.error(TAG, "[cancelListenElementCreate] error: " + e.getMessage(), e); }
        CallbackRefContainer r = listenCreateCallbackRef.remove(listenId); if (r != null) r.release();
    }

    /**
     * 订阅要素更新事件
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_element_update
     */
    public int listenElementUpdate(FlightHubCallback.ElementUpdateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.ElementUpdateCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementUpdateCallback() {
                        @Override
                        public void onElementUpdate(com.dji.flighthubsdk.generated.ElementUpdateMsg msg) {
                            try {
                                if (callback != null) callback.onResult(FlightHubSdkConvert.toElementUpdateMsg(msg));
                            } catch (Exception e) { Logger.error(TAG, "[listenElementUpdate] cb error: " + e.getMessage(), e); }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (listenUpdateCallbackRef) {
                listenUpdateCallbackRef.put(-1, container);
                int listenId = elementModule.listenElementUpdateWrapper(swigCb);
                listenUpdateCallbackRef.remove(-1);
                if (listenId >= 0) { listenUpdateCallbackRef.put(listenId, container); } else { container.release(); }
                Logger.info(TAG, "listenElementUpdate listenId=" + listenId);
                return listenId;
            }
        } catch (Exception e) { Logger.error(TAG, "[listenElementUpdate] error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅要素更新事件 */
    public void cancelListenElementUpdate(int listenId) {
        try { elementModule.cancelListenElementUpdateWrapper(listenId); } catch (Exception e) { Logger.error(TAG, "[cancelListenElementUpdate] error: " + e.getMessage(), e); }
        CallbackRefContainer r = listenUpdateCallbackRef.remove(listenId); if (r != null) r.release();
    }

    /**
     * 订阅要素删除事件
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_element_delete
     */
    public int listenElementDelete(FlightHubCallback.ElementDeleteCallback callback) {
        try {
            com.dji.flighthubsdk.generated.ElementDeleteCallback swigCb =
                    new com.dji.flighthubsdk.generated.ElementDeleteCallback() {
                        @Override
                        public void onElementDelete(com.dji.flighthubsdk.generated.ElementDeleteMsg msg) {
                            try {
                                if (callback != null) callback.onResult(FlightHubSdkConvert.toElementDeleteMsg(msg));
                            } catch (Exception e) { Logger.error(TAG, "[listenElementDelete] cb error: " + e.getMessage(), e); }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (listenDeleteCallbackRef) {
                listenDeleteCallbackRef.put(-1, container);
                int listenId = elementModule.listenElementDeleteWrapper(swigCb);
                listenDeleteCallbackRef.remove(-1);
                if (listenId >= 0) { listenDeleteCallbackRef.put(listenId, container); } else { container.release(); }
                Logger.info(TAG, "listenElementDelete listenId=" + listenId);
                return listenId;
            }
        } catch (Exception e) { Logger.error(TAG, "[listenElementDelete] error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅要素删除事件 */
    public void cancelListenElementDelete(int listenId) {
        try { elementModule.cancelListenElementDeleteWrapper(listenId); } catch (Exception e) { Logger.error(TAG, "[cancelListenElementDelete] error: " + e.getMessage(), e); }
        CallbackRefContainer r = listenDeleteCallbackRef.remove(listenId); if (r != null) r.release();
    }
}
