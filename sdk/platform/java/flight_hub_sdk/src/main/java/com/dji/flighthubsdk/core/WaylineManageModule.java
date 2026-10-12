// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.BatchDownloadWaylinesResult;
import com.dji.flighthubsdk.generated.DuplicateWaylineResult;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.GetWaylineDetailResult;
import com.dji.flighthubsdk.generated.GetWaylineListResult;
import com.dji.flighthubsdk.generated.IWaylineManageModule;
import com.dji.flighthubsdk.generated.StringVector;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 航线管理模块接口，提供航线增删改查、批量下载及导入事件监听等功能。
 */
public class WaylineManageModule {

    private static final String TAG = "WaylineManageModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getWaylineListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getWaylineDetailCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> batchDeleteWaylinesCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateWaylineCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> submitWaylineInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> batchDownloadWaylinesCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> duplicateWaylineCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IWaylineManageModule waylineManageModule;

    public WaylineManageModule(IWaylineManageModule waylineManageModule) {
        this.waylineManageModule = waylineManageModule;
    }

    /**
     * 获取航线列表
     * @param param 查询参数
     * @param callback 回调，返回航线列表与错误信息
     * @implNote 链路: HTTP | GET /pf-wayline/api/v1/list
     */
    public void getWaylineList(com.dji.flighthubsdk.bean.GetWaylineListParam param, FlightHubCallback.GetWaylineListCallback callback) {
        com.dji.flighthubsdk.generated.GetWaylineListParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeGetWaylineListParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetWaylineListCallback swigCallback = new com.dji.flighthubsdk.generated.GetWaylineListCallback() {
                @Override
                public void onGetWaylineList(com.dji.flighthubsdk.generated.FlightHubError err, GetWaylineListResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetWaylineListResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getWaylineList] callback error: " + e.getMessage(), e);
                    } finally {
                        getWaylineListCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getWaylineListCallbackRef) {
                getWaylineListCallbackRef.put(id, container);
                waylineManageModule.getWaylineListWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getWaylineList] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toGetWaylineListResult(null));
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 获取航线详情
     * @param waylineUuid 航线 UUID
     * @param callback 回调，返回航线详情与错误信息
     * @implNote 链路: HTTP | GET /pf-wayline/api/v1/details
     */
    public void getWaylineDetail(String waylineUuid, FlightHubCallback.GetWaylineDetailCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetWaylineDetailCallback swigCallback = new com.dji.flighthubsdk.generated.GetWaylineDetailCallback() {
                @Override
                public void onGetWaylineDetail(com.dji.flighthubsdk.generated.FlightHubError err, GetWaylineDetailResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetWaylineDetailResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getWaylineDetail] callback error: " + e.getMessage(), e);
                    } finally {
                        getWaylineDetailCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getWaylineDetailCallbackRef) {
                getWaylineDetailCallbackRef.put(id, container);
                waylineManageModule.getWaylineDetailWrapper(waylineUuid, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getWaylineDetail] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toGetWaylineDetailResult(null));
            }
        }
    }

    /**
     * 批量删除航线
     * @param waylineUuids 航线 UUID 列表
     * @param callback 回调，返回删除结果与错误信息
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/batch-delete
     */
    public void batchDeleteWaylines(java.util.List<String> waylineUuids, FlightHubCallback.BatchDeleteWaylinesCallback callback) {
        StringVector nativeWaylineUuids = null;
        try {
            nativeWaylineUuids = FlightHubSdkConvert.toNativeStringVector(waylineUuids);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.BatchDeleteWaylinesCallback swigCallback = new com.dji.flighthubsdk.generated.BatchDeleteWaylinesCallback() {
                @Override
                public void onBatchDeleteWaylines(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.BatchDeleteWaylinesResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toBatchDeleteWaylinesResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[batchDeleteWaylines] callback error: " + e.getMessage(), e);
                    } finally {
                        batchDeleteWaylinesCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (batchDeleteWaylinesCallbackRef) {
                batchDeleteWaylinesCallbackRef.put(id, container);
                waylineManageModule.batchDeleteWaylinesWrapper(nativeWaylineUuids, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[batchDeleteWaylines] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        new com.dji.flighthubsdk.bean.BatchDeleteWaylinesResult(-1, ""));
            }
        } finally {
            if (nativeWaylineUuids != null) {
                nativeWaylineUuids.delete();
            }
        }
    }

    /**
     * 更新航线
     * @param waylineUuid 航线 UUID
     * @param waylineName 新航线名称
     * @param callback 回调，返回更新结果与错误信息
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/update
     */
    public void updateWayline(String waylineUuid, String waylineName, FlightHubCallback.UpdateWaylineCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UpdateWaylineCallback swigCallback = new com.dji.flighthubsdk.generated.UpdateWaylineCallback() {
                @Override
                public void onUpdateWayline(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.UpdateWaylineResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toUpdateWaylineResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[updateWayline] callback error: " + e.getMessage(), e);
                    } finally {
                        updateWaylineCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (updateWaylineCallbackRef) {
                updateWaylineCallbackRef.put(id, container);
                waylineManageModule.updateWaylineWrapper(waylineUuid, waylineName, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[updateWayline] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toUpdateWaylineResult(null));
            }
        }
    }

    /**
     * 提交航线文件信息
     * @param param 提交参数
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/import-callback
     */
    public void submitWaylineInfo(com.dji.flighthubsdk.bean.SubmitWaylineInfoParam param, FlightHubCallback.SubmitWaylineInfoCallback callback) {
        com.dji.flighthubsdk.generated.SubmitWaylineInfoParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeSubmitWaylineInfoParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SubmitWaylineInfoCallback swigCallback = new com.dji.flighthubsdk.generated.SubmitWaylineInfoCallback() {
                @Override
                public void onSubmitWaylineInfo(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[submitWaylineInfo] callback error: " + e.getMessage(), e);
                    } finally {
                        submitWaylineInfoCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (submitWaylineInfoCallbackRef) {
                submitWaylineInfoCallbackRef.put(id, container);
                waylineManageModule.submitWaylineInfoWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[submitWaylineInfo] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 批量下载航线文件
     * @param param 下载参数
     * @param callback 回调，返回下载结果与错误信息
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/batch-download
     */
    public void batchDownloadWaylines(com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam param, FlightHubCallback.BatchDownloadWaylinesCallback callback) {
        com.dji.flighthubsdk.generated.BatchDownloadWaylinesParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeBatchDownloadWaylinesParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.BatchDownloadWaylinesCallback swigCallback = new com.dji.flighthubsdk.generated.BatchDownloadWaylinesCallback() {
                @Override
                public void onBatchDownloadWaylines(com.dji.flighthubsdk.generated.FlightHubError err, BatchDownloadWaylinesResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toBatchDownloadWaylinesItemResultList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[batchDownloadWaylines] callback error: " + e.getMessage(), e);
                    } finally {
                        batchDownloadWaylinesCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (batchDownloadWaylinesCallbackRef) {
                batchDownloadWaylinesCallbackRef.put(id, container);
                waylineManageModule.batchDownloadWaylinesWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[batchDownloadWaylines] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 创建副本航线
     * @param param 参数
     * @param callback 回调，返回副本航线结果与错误信息
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/duplicate
     */
    public void duplicateWayline(com.dji.flighthubsdk.bean.DuplicateWaylineParam param, FlightHubCallback.DuplicateWaylineCallback callback) {
        com.dji.flighthubsdk.generated.DuplicateWaylineParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeDuplicateWaylineParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DuplicateWaylineCallback swigCallback = new com.dji.flighthubsdk.generated.DuplicateWaylineCallback() {
                @Override
                public void onDuplicateWayline(com.dji.flighthubsdk.generated.FlightHubError err, DuplicateWaylineResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDuplicateWaylineResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[duplicateWayline] callback error: " + e.getMessage(), e);
                    } finally {
                        duplicateWaylineCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (duplicateWaylineCallbackRef) {
                duplicateWaylineCallbackRef.put(id, container);
                waylineManageModule.duplicateWaylineWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[duplicateWayline] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toDuplicateWaylineResult(null));
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 监听航线导入事件
     * @param callback 航线导入事件回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_import_wayline_event
     */
    public int listenImportWaylineEvent(FlightHubCallback.ImportWaylineEventCallback callback) {
        try {
            com.dji.flighthubsdk.generated.ImportWaylineEventCallback swigCallback = new com.dji.flighthubsdk.generated.ImportWaylineEventCallback() {
                @Override
                public void onImportWaylineEvent(com.dji.flighthubsdk.generated.ImportWaylineEventResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toImportWaylineEventResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[listenImportWaylineEvent] callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = waylineManageModule.listenImportWaylineEventWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenImportWaylineEvent] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅航线导入事件
     * @param listenId 监听 ID
     */
    public void cancelListenImportWaylineEvent(int listenId) {
        try {
            waylineManageModule.cancelListenImportWaylineEventWrapper(listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenImportWaylineEvent] error: " + e.getMessage(), e);
        }
    }

    // ==================== 新增：航线复制 / 航线拼接 ====================

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> copyWaylineCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> concatWaylineCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getWaylineEditorStsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 航线复制（去组织化，不需要 workspace_id）
     * @param param    复制参数（原始航线 UUID + 目标航线名称）
     * @param callback 回调，返回复制结果与错误信息
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/copy
     */
    public void copyWayline(com.dji.flighthubsdk.bean.CopyWaylineParam param, FlightHubCallback.CopyWaylineCallback callback) {
        com.dji.flighthubsdk.generated.CopyWaylineParam nativeParam = null;
        try {
            nativeParam = new com.dji.flighthubsdk.generated.CopyWaylineParam();
            nativeParam.setOriginal_wayline_uuid(param.getOriginalWaylineUuid());
            nativeParam.setTarget_wayline_name(param.getTargetWaylineName());

            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CopyWaylineCallback swigCallback = new com.dji.flighthubsdk.generated.CopyWaylineCallback() {
                @Override
                public void onCopyWayline(com.dji.flighthubsdk.generated.FlightHubError err,
                                          com.dji.flighthubsdk.generated.CopyWaylineResult result) {
                    try {
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.CopyWaylineResult beanResult = new com.dji.flighthubsdk.bean.CopyWaylineResult();
                            if (result != null) {
                                beanResult.setWaylineUuid(result.getWayline_uuid());
                                beanResult.setWaylineName(result.getWayline_name());
                            }
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), beanResult);
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[copyWayline] callback error: " + e.getMessage(), e);
                    } finally {
                        copyWaylineCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (copyWaylineCallbackRef) {
                copyWaylineCallbackRef.put(id, container);
                waylineManageModule.copyWaylineWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[copyWayline] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        new com.dji.flighthubsdk.bean.CopyWaylineResult());
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 航线拼接（去组织化，不需要 workspace_id）
     * @param param    拼接参数（目标名称 + kmzObjectKey + 子航线UUID列表）
     * @param callback 回调，返回拼接结果与错误信息
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/concat
     */
    public void concatWayline(com.dji.flighthubsdk.bean.ConcatWaylineParam param, FlightHubCallback.ConcatWaylineCallback callback) {
        com.dji.flighthubsdk.generated.ConcatWaylineParam nativeParam = null;
        try {
            nativeParam = new com.dji.flighthubsdk.generated.ConcatWaylineParam();
            nativeParam.setTarget_wayline_name(param.getTargetWaylineName());
            nativeParam.setKmz_object_key(param.getKmzObjectKey());
            // sub_wayline_uuids: List<String> → generated.StringVector
            com.dji.flighthubsdk.generated.StringVector nativeUuids = new com.dji.flighthubsdk.generated.StringVector();
            for (String uuid : param.getSubWaylineUuids()) {
                nativeUuids.add(uuid);
            }
            nativeParam.setSub_wayline_uuids(nativeUuids);
            // editor_warnings: Map<String,String> → generated.StringStringUnorderedMap
            com.dji.flighthubsdk.generated.StringStringUnorderedMap nativeWarnings = new com.dji.flighthubsdk.generated.StringStringUnorderedMap();
            if (param.getEditorWarnings() != null) {
                for (java.util.Map.Entry<String, String> entry : param.getEditorWarnings().entrySet()) {
                    nativeWarnings.put(entry.getKey(), entry.getValue());
                }
            }
            nativeParam.setEditor_warnings(nativeWarnings);

            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ConcatWaylineCallback swigCallback = new com.dji.flighthubsdk.generated.ConcatWaylineCallback() {
                @Override
                public void onConcatWayline(com.dji.flighthubsdk.generated.FlightHubError err,
                                            com.dji.flighthubsdk.generated.ConcatWaylineResult result) {
                    try {
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.ConcatWaylineResult beanResult = new com.dji.flighthubsdk.bean.ConcatWaylineResult();
                            if (result != null) {
                                beanResult.setWaylineUuid(result.getWayline_uuid());
                                // 映射 kmzData（复用已有转换方法）
                                beanResult.setKmzData(FlightHubSdkConvert.toWaylineDetailKmzData(result.getKmz_data()));
                                // 映射 extData
                                com.dji.flighthubsdk.generated.ConcatWaylineExtData nativeExtData = result.getExt_data();
                                if (nativeExtData != null) {
                                    com.dji.flighthubsdk.bean.WaylineDetailExtData beanExtData = new com.dji.flighthubsdk.bean.WaylineDetailExtData();
                                    beanExtData.setKmzWithoutResObjectKey(nativeExtData.getKmz_without_res_object_key());
                                    beanExtData.setKmzWithoutResMd5(nativeExtData.getKmz_without_res_md5());
                                    beanExtData.setTargetAreaFileObjectKey(nativeExtData.getTarget_area_file_object_key());
                                    beanExtData.setBusinessFileObjectKey(nativeExtData.getBusiness_file_object_key());
                                    // model_file_object_keys
                                    com.dji.flighthubsdk.generated.StringVector nativeModelKeys = nativeExtData.getModel_file_object_keys();
                                    if (nativeModelKeys != null) {
                                        java.util.List<String> modelKeys = new java.util.ArrayList<>();
                                        for (int i = 0; i < nativeModelKeys.size(); i++) {
                                            modelKeys.add(nativeModelKeys.get(i));
                                        }
                                        beanExtData.setModelFileObjectKeys(modelKeys);
                                    }
                                    // elevation_file_object_keys
                                    com.dji.flighthubsdk.generated.StringVector nativeElevationKeys = nativeExtData.getElevation_file_object_keys();
                                    if (nativeElevationKeys != null) {
                                        java.util.List<String> elevationKeys = new java.util.ArrayList<>();
                                        for (int i = 0; i < nativeElevationKeys.size(); i++) {
                                            elevationKeys.add(nativeElevationKeys.get(i));
                                        }
                                        beanExtData.setElevationFileObjectKeys(elevationKeys);
                                    }
                                    // content_length (SWIG 将 long long 映射为 Java long)
                                    beanExtData.setContentLength(nativeExtData.getContent_length());
                                    beanResult.setExtData(beanExtData);
                                }
                            }
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), beanResult);
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[concatWayline] callback error: " + e.getMessage(), e);
                    } finally {
                        concatWaylineCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (concatWaylineCallbackRef) {
                concatWaylineCallbackRef.put(id, container);
                waylineManageModule.concatWaylineWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[concatWayline] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        new com.dji.flighthubsdk.bean.ConcatWaylineResult());
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    // ==================== 新增：上传 KMZ 文件并导入航线（走 C++ 层 multipart） ====================

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> importKmzCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 上传一个或多个 .kmz 航线文件，系统自动上传到对象存储并触发导入。
     * @param filePaths 本地 KMZ 文件路径列表（仅接受 .kmz 后缀）
     * @param callback  结果回调（含每个文件的导入结果）
     * @implNote 链路: HTTP | POST /pf-wayline/api/v1/import-kmz (multipart/form-data via C++ libcurl)
     */
    public void importKmz(java.util.List<String> filePaths, FlightHubCallback.ImportKmzCallback callback) {
        com.dji.flighthubsdk.generated.StringVector nativeFilePaths = null;
        try {
            nativeFilePaths = new com.dji.flighthubsdk.generated.StringVector();
            for (String path : filePaths) {
                nativeFilePaths.add(path);
            }

            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ImportKmzCallback swigCallback = new com.dji.flighthubsdk.generated.ImportKmzCallback() {
                @Override
                public void onImportKmz(com.dji.flighthubsdk.generated.FlightHubError err,
                                        com.dji.flighthubsdk.generated.ImportKmzResult result) {
                    try {
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.ImportKmzResult beanResult = new com.dji.flighthubsdk.bean.ImportKmzResult();
                            if (result != null) {
                                beanResult.setTotal((int) result.getTotal());
                                // list 映射（需 SWIG 已生成 ImportKmzItemVector）
                                com.dji.flighthubsdk.generated.ImportKmzItemVector nativeList = result.getList();
                                if (nativeList != null) {
                                    java.util.List<com.dji.flighthubsdk.bean.ImportKmzItem> beanList = new java.util.ArrayList<>();
                                    for (int i = 0; i < nativeList.size(); i++) {
                                        com.dji.flighthubsdk.generated.ImportKmzItem nativeItem = nativeList.get(i);
                                        com.dji.flighthubsdk.bean.ImportKmzItem beanItem = new com.dji.flighthubsdk.bean.ImportKmzItem();
                                        beanItem.setKmzObjectKey(nativeItem.getKmz_object_key());
                                        beanItem.setWaylineName(nativeItem.getWayline_name());
                                        beanItem.setAsyncRequestId(nativeItem.getAsync_request_id());
                                        beanList.add(beanItem);
                                    }
                                    beanResult.setList(beanList);
                                }
                            }
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), beanResult);
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[importKmz] callback error: " + e.getMessage(), e);
                    } finally {
                        importKmzCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (importKmzCallbackRef) {
                importKmzCallbackRef.put(id, container);
                waylineManageModule.importKmzWrapper(nativeFilePaths, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[importKmz] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        new com.dji.flighthubsdk.bean.ImportKmzResult());
            }
        } finally {
            if (nativeFilePaths != null) {
                nativeFilePaths.delete();
            }
        }
    }

    /**
     * 获取航线编辑器 OSS STS 临时上传凭证
     * @param params   请求参数（objectKeyPrefix 必填）
     * @param callback 结果回调，成功时返回 STS 凭证信息
     */
    public void getWaylineEditorSts(com.dji.flighthubsdk.bean.GetWaylineEditorStsParams params,
            FlightHubCallback.GetWaylineEditorStsCallback callback) {
        com.dji.flighthubsdk.generated.GetWaylineEditorStsParams nativeParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetWaylineEditorStsCallback swigCallback =
                    new com.dji.flighthubsdk.generated.GetWaylineEditorStsCallback() {
                        @Override
                        public void onGetWaylineEditorSts(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.GetWaylineEditorStsResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toGetWaylineEditorStsResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getWaylineEditorSts] callback error: " + e.getMessage(), e);
                            } finally {
                                getWaylineEditorStsCallbackRef.remove(id);
                            }
                        }
                    };
            nativeParams = new com.dji.flighthubsdk.generated.GetWaylineEditorStsParams();
            nativeParams.setObject_key_prefix(params != null && params.getObjectKeyPrefix() != null ? params.getObjectKeyPrefix() : "");
            nativeParams.setExpire(params != null ? params.getExpire() : 3600);
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getWaylineEditorStsCallbackRef) {
                getWaylineEditorStsCallbackRef.put(id, container);
                this.waylineManageModule.getWaylineEditorStsWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getWaylineEditorSts] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
}
