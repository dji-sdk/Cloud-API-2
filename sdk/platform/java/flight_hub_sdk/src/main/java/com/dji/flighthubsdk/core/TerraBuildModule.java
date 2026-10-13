// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * Terra 重建模块接口，提供三维/二维/全景重建任务管理、模型文件管理、质量报告等功能。
 */
public class TerraBuildModule {

    private static final String TAG = "TerraBuildModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getReconstructionSettingsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getQualityReportCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getModelFileListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getModelFileUrlCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> retryReconstructionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopReconstructionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getSfmResultCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createReconstructionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deleteModelFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> batchDeleteModelFilesCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createPanoramaCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getReconstructionImagesCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenReconstructionStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    // 新增接口 callback ref
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> uploadModelCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> renameModelCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> showOnMapCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenUnzipProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenModelStatusChangedCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenFileShowOnMapCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenMediaFileHideOnMapCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> listenMediaFileShowOnMapCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getModelDetailCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setLasWktCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private ITerraBuildModule terraBuildModule;

    /**
     * 构造 Terra 建图模块
     * @param terraBuildModule 底层 Terra 建图模块接口
     */
    public TerraBuildModule(ITerraBuildModule terraBuildModule) {
        this.terraBuildModule = terraBuildModule;
        Logger.info(TAG, "TerraBuildModule instance created");
    }

    /**
     * 获取重建设置
     * @param fileUuid 文件 UUID
     * @param callback 结果回调
     * @implNote 链路: HTTP | GET /pf-terra/api/v1/models/reconstruction/settings/{file_uuid}
     */
    public void getReconstructionSettings(String fileUuid, FlightHubCallback.TerraBuildGetReconstructionSettingsCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildReconstructionSettingsCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildReconstructionSettingsCallback() {
                        @Override
                        public void onGetReconstructionSettings(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildSettingsResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildSettingsResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getReconstructionSettings] callback error: " + e.getMessage(), e);
                            } finally {
                                getReconstructionSettingsCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getReconstructionSettingsCallbackRef) {
                getReconstructionSettingsCallbackRef.put(id, container);
                this.terraBuildModule.getReconstructionSettingsWrapper(fileUuid != null ? fileUuid : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getReconstructionSettings] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 获取质量报告
     * @param fileUuid 文件 UUID
     * @param language 语言（如 zh / en）
     * @param measureUnitSystem 度量单位制：{@code metric}（公制，默认）/ {@code imperial}（英制）
     * @param callback 结果回调
     * @implNote 链路: HTTP | GET /pf-terra/api/v1/models/reconstruction/{file_uuid}/quality-report
     */
    public void getQualityReport(String fileUuid, String language, String measureUnitSystem, FlightHubCallback.TerraBuildGetQualityReportCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildQualityReportCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildQualityReportCallback() {
                        @Override
                        public void onGetQualityReport(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildQualityReportResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildQualityReportResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getQualityReport] callback error: " + e.getMessage(), e);
                            } finally {
                                getQualityReportCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getQualityReportCallbackRef) {
                getQualityReportCallbackRef.put(id, container);
                this.terraBuildModule.getQualityReportWrapper(
                        fileUuid != null ? fileUuid : "",
                        language != null ? language : "",
                        measureUnitSystem != null ? measureUnitSystem : "",
                        swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getQualityReport] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 获取模型文件列表（分页）
     * @param page 页码
     * @param pageSize 每页数量
     * @param callback 结果回调
     * @implNote 链路: HTTP | GET /pf-terra/api/v1/models/files
     */
    public void getModelFileList(int page, int pageSize, FlightHubCallback.TerraBuildGetModelFileListCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildModelFileListCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildModelFileListCallback() {
                        @Override
                        public void onGetModelFileList(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildModelFileListResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildModelFileListResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getModelFileList] callback error: " + e.getMessage(), e);
                            } finally {
                                getModelFileListCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getModelFileListCallbackRef) {
                getModelFileListCallbackRef.put(id, container);
                this.terraBuildModule.getModelFileListWrapper(page, pageSize, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getModelFileList] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 获取模型文件签名 URL
     * @param fileId 文件 ID
     * @param type 文件类型。枚举：1=原始文件(默认), 2=转换后 ZIP
     * @param callback 结果回调
     * @implNote 链路: HTTP | GET /pf-terra/api/v1/models/files/{file_id}/url
     */
    public void getModelFileUrl(int fileId, int type, FlightHubCallback.TerraBuildGetModelFileUrlCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildModelFileUrlCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildModelFileUrlCallback() {
                        @Override
                        public void onGetModelFileUrl(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildModelFileUrlResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildModelFileUrlResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getModelFileUrl] callback error: " + e.getMessage(), e);
                            } finally {
                                getModelFileUrlCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getModelFileUrlCallbackRef) {
                getModelFileUrlCallbackRef.put(id, container);
                this.terraBuildModule.getModelFileUrlWrapper(fileId, type, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getModelFileUrl] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 重试重建任务
     * @param fileUuid 文件 UUID
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-terra/api/v1/models/reconstruction/retry
     */
    public void retryReconstruction(String fileUuid, FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallback swigCallback =
                    new com.dji.flighthubsdk.generated.ResultCallback() {
                        @Override
                        public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[retryReconstruction] callback error: " + e.getMessage(), e);
                            } finally {
                                retryReconstructionCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (retryReconstructionCallbackRef) {
                retryReconstructionCallbackRef.put(id, container);
                this.terraBuildModule.retryReconstructionWrapper(fileUuid != null ? fileUuid : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[retryReconstruction] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED)); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 停止重建任务
     * @param fileUuid 文件 UUID
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-terra/api/v1/models/reconstruction/stop
     */
    public void stopReconstruction(String fileUuid, FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallback swigCallback =
                    new com.dji.flighthubsdk.generated.ResultCallback() {
                        @Override
                        public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[stopReconstruction] callback error: " + e.getMessage(), e);
                            } finally {
                                stopReconstructionCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (stopReconstructionCallbackRef) {
                stopReconstructionCallbackRef.put(id, container);
                this.terraBuildModule.stopReconstructionWrapper(fileUuid != null ? fileUuid : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[stopReconstruction] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED)); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 获取空三结果
     * @param fileUuid 文件 UUID
     * @param callback 结果回调
     * @implNote 链路: HTTP | GET /pf-terra/api/v1/models/reconstruction/{file_uuid}/sfm-result
     */
    public void getSfmResult(String fileUuid, FlightHubCallback.TerraBuildGetSfmResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildSfmResultCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildSfmResultCallback() {
                        @Override
                        public void onGetSfmResult(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildSfmResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildSfmResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getSfmResult] callback error: " + e.getMessage(), e);
                            } finally {
                                getSfmResultCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getSfmResultCallbackRef) {
                getSfmResultCallbackRef.put(id, container);
                this.terraBuildModule.getSfmResultWrapper(fileUuid != null ? fileUuid : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getSfmResult] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 创建重建任务
     * @param params 重建参数
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-terra/api/v1/models/reconstruction
     */
    public void createReconstruction(com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams params, FlightHubCallback.TerraBuildCreateReconstructionCallback callback) {
        com.dji.flighthubsdk.generated.CreateReconstructionJavaParams nativeCreateReconstructionParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildCreateReconstructionCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildCreateReconstructionCallback() {
                        @Override
                        public void onCreateReconstruction(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildCreateReconstructionResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildCreateReconstructionResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[createReconstruction] callback error: " + e.getMessage(), e);
                            } finally {
                                createReconstructionCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeCreateReconstructionParams = FlightHubSdkConvert.toNativeCreateReconstructionParams(params);
            synchronized (createReconstructionCallbackRef) {
                createReconstructionCallbackRef.put(id, container);
                this.terraBuildModule.createReconstructionWithParamsWrapper(nativeCreateReconstructionParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createReconstruction] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeCreateReconstructionParams != null) {
                nativeCreateReconstructionParams.delete();
            }
        }
    }

    /**
     * 创建重建任务
     * @param jp 重建参数
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-terra/api/v1/models/reconstruction
     */
    public void createReconstruction(com.dji.flighthubsdk.generated.CreateReconstructionJavaParams jp,
            FlightHubCallback.TerraBuildCreateReconstructionCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildCreateReconstructionCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildCreateReconstructionCallback() {
                        @Override
                        public void onCreateReconstruction(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildCreateReconstructionResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildCreateReconstructionResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[createReconstruction] callback error: " + e.getMessage(), e);
                            } finally {
                                createReconstructionCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (createReconstructionCallbackRef) {
                createReconstructionCallbackRef.put(id, container);
                this.terraBuildModule.createReconstructionWithParamsWrapper(jp, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createReconstruction] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 删除单个模型文件
     * @param fileId 文件 ID
     * @param callback 结果回调
     * @implNote 链路: HTTP | DELETE /pf-terra/api/v1/models/reconstruction/files/{file_id}
     */
    public void deleteModelFile(int fileId, FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallback swigCallback =
                    new com.dji.flighthubsdk.generated.ResultCallback() {
                        @Override
                        public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[deleteModelFile] callback error: " + e.getMessage(), e);
                            } finally {
                                deleteModelFileCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (deleteModelFileCallbackRef) {
                deleteModelFileCallbackRef.put(id, container);
                this.terraBuildModule.deleteModelFileWrapper(fileId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteModelFile] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED)); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 批量删除模型文件
     * @param fileIds 文件 ID 列表
     * @param callback 结果回调
     * @implNote 链路: HTTP | DELETE /pf-terra/api/v1/models/reconstruction/files
     */
    public void batchDeleteModelFiles(java.util.List<Integer> fileIds, FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.IntVector nativeFileIds = new com.dji.flighthubsdk.generated.IntVector();
            if (fileIds != null) for (int fid : fileIds) nativeFileIds.add(fid);
            com.dji.flighthubsdk.generated.ResultCallback swigCallback =
                    new com.dji.flighthubsdk.generated.ResultCallback() {
                        @Override
                        public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[batchDeleteModelFiles] callback error: " + e.getMessage(), e);
                            } finally {
                                batchDeleteModelFilesCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (batchDeleteModelFilesCallbackRef) {
                batchDeleteModelFilesCallbackRef.put(id, container);
                this.terraBuildModule.batchDeleteModelFilesWrapper(nativeFileIds, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[batchDeleteModelFiles] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED)); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 创建全景拼接任务
     * @param name 名称
     * @param folderUuids 文件夹 UUID 列表
     * @param wkt WKT 几何
     * @param isShownOnMap 是否在地图上显示
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-terra/api/v1/models/panorama
     */
    public void createPanorama(String name, java.util.List<String> folderUuids, String wkt, boolean isShownOnMap, FlightHubCallback.TerraBuildCreatePanoramaCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.StringVector nativeFolderUuids = new com.dji.flighthubsdk.generated.StringVector();
            if (folderUuids != null) for (String s : folderUuids) nativeFolderUuids.add(s);
            com.dji.flighthubsdk.generated.TerraBuildCreatePanoramaCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildCreatePanoramaCallback() {
                        @Override
                        public void onCreatePanorama(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildCreatePanoramaResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildCreatePanoramaResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[createPanorama] callback error: " + e.getMessage(), e);
                            } finally {
                                createPanoramaCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (createPanoramaCallbackRef) {
                createPanoramaCallbackRef.put(id, container);
                this.terraBuildModule.createPanoramaWrapper(
                        name != null ? name : "",
                        nativeFolderUuids,
                        wkt != null ? wkt : "",
                        isShownOnMap,
                        swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createPanorama] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 获取重建图像列表
     * @param fileUuid 文件 UUID
     * @param callback 结果回调
     * @implNote 链路: HTTP | GET /pf-terra/api/v1/images/reconstruction/{file_uuid}
     */
    public void getReconstructionImages(String fileUuid, FlightHubCallback.TerraBuildGetReconstructionImagesCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraBuildReconstructionImagesCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildReconstructionImagesCallback() {
                        @Override
                        public void onGetReconstructionImages(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraBuildReconstructionImagesResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraBuildReconstructionImagesResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getReconstructionImages] callback error: " + e.getMessage(), e);
                            } finally {
                                getReconstructionImagesCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getReconstructionImagesCallbackRef) {
                getReconstructionImagesCallbackRef.put(id, container);
                this.terraBuildModule.getReconstructionImagesWrapper(fileUuid != null ? fileUuid : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getReconstructionImages] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 订阅重建任务状态变更推送
     * @param callback 状态变更回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_reconstruction_status_changed_event
     */
    public int listenReconstructionStatusChanged(FlightHubCallback.TerraBuildReconstructionStatusChangedCallback callback) {
        try {
            com.dji.flighthubsdk.generated.TerraBuildReconstructionStatusChangedCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraBuildReconstructionStatusChangedCallback() {
                        @Override
                        public void onReconstructionStatusChanged(com.dji.flighthubsdk.generated.TerraBuildReconstructionStatusChanged msg) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toTerraBuildReconstructionStatusChanged(msg));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[listenReconstructionStatusChanged] callback error: " + e.getMessage(), e);
                            }
                        }
                    };
            int listenId = this.terraBuildModule.listenReconstructionStatusChangedWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            Logger.info(TAG, "listenReconstructionStatusChanged listenId: " + listenId);
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenReconstructionStatusChanged] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅重建任务状态变更推送
     * @param listenId 监听 ID
     */
    public void cancelListenReconstructionStatusChanged(int listenId) {
        try {
            this.terraBuildModule.cancelListenReconstructionStatusChangedWrapper(listenId);
            Logger.info(TAG, "cancelListenReconstructionStatusChanged listenId: " + listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenReconstructionStatusChanged] error: " + e.getMessage(), e);
        }
    }

    /**
     * 模型文件上传回调
     * @param params 上传回调参数（objectKey/name 必填）
     * @param callback 结果回调（返回 id/uuid/name）
     * @implNote 链路: HTTP | POST /pf-terra/api/v1/models/files/upload-callback
     */
    public void uploadModelCallback(com.dji.flighthubsdk.bean.TerraUploadCallbackParams params,
            FlightHubCallback.TerraUploadCallbackCallback callback) {
        com.dji.flighthubsdk.generated.TerraUploadCallbackJavaParams nativeParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraUploadCallbackCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraUploadCallbackCallback() {
                        @Override
                        public void onTerraUploadCallback(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraUploadCallbackResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraUploadCallbackResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[uploadModelCallback] callback error: " + e.getMessage(), e);
                            } finally {
                                uploadModelCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeParams = FlightHubSdkConvert.toNativeTerraUploadCallbackParams(params);
            synchronized (uploadModelCallbackRef) {
                uploadModelCallbackRef.put(id, container);
                this.terraBuildModule.uploadModelCallbackWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[uploadModelCallback] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 模型重命名
     * @param fileId 文件 ID
     * @param params 重命名参数（name 必填）
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /model/api/v1/files/{id}
     */
    public void renameModel(int fileId, com.dji.flighthubsdk.bean.TerraRenameModelParams params,
            FlightHubCallback.TerraRenameModelCallback callback) {
        com.dji.flighthubsdk.generated.TerraRenameModelJavaParams nativeParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallback swigCallback =
                    new com.dji.flighthubsdk.generated.ResultCallback() {
                        @Override
                        public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[renameModel] callback error: " + e.getMessage(), e);
                            } finally {
                                renameModelCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeParams = FlightHubSdkConvert.toNativeTerraRenameModelParams(params);
            synchronized (renameModelCallbackRef) {
                renameModelCallbackRef.put(id, container);
                this.terraBuildModule.renameModelWrapper(fileId, nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[renameModel] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED)); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 批量加载/取消加载模型到地图
     * @param params 加载参数（ids 与 selectFilter 二选一，showOnMap 必填）
     * @param callback 结果回调（返回实际操作的文件 ID 列表）
     * @implNote 链路: HTTP | PUT /model/api/v1/files
     */
    public void showModelOnMap(com.dji.flighthubsdk.bean.TerraShowOnMapParams params,
            FlightHubCallback.TerraShowOnMapCallback callback) {
        com.dji.flighthubsdk.generated.TerraShowOnMapJavaParams nativeParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraShowOnMapCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraShowOnMapCallback() {
                        @Override
                        public void onTerraShowOnMap(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraShowOnMapResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraShowOnMapResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[showModelOnMap] callback error: " + e.getMessage(), e);
                            } finally {
                                showOnMapCallbackRef.remove(id);
                            }
                        }
                    };
            // nativeParams 先分配，再创建 container，确保 toNative 抛出时 swigCallback 不会泄漏
            nativeParams = FlightHubSdkConvert.toNativeTerraShowOnMapParams(params);
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (showOnMapCallbackRef) {
                showOnMapCallbackRef.put(id, container);
                this.terraBuildModule.showModelOnMapWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[showModelOnMap] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 订阅模型解压进度推送
     * @param callback 解压进度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_reconstruction_unzip_progress
     */
    public int listenTerraUnzipProgress(FlightHubCallback.TerraUnzipProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.TerraUnzipProgressCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraUnzipProgressCallback() {
                        @Override
                        public void onTerraUnzipProgress(com.dji.flighthubsdk.generated.TerraUnzipProgressMsg msg) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toTerraUnzipProgressMsg(msg));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[listenTerraUnzipProgress] callback error: " + e.getMessage(), e);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (listenUnzipProgressCallbackRef) {
                listenUnzipProgressCallbackRef.put(-1, container);
                int listenId = this.terraBuildModule.listenTerraUnzipProgressWrapper(swigCallback);
                listenUnzipProgressCallbackRef.remove(-1);
                if (listenId >= 0) {
                    listenUnzipProgressCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                Logger.info(TAG, "listenTerraUnzipProgress listenId: " + listenId);
                return listenId;
            }
        } catch (Exception e) {
            Logger.error(TAG, "[listenTerraUnzipProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅模型解压进度推送
     * @param listenId 监听 ID
     */
    public void cancelListenTerraUnzipProgress(int listenId) {
        try {
            this.terraBuildModule.cancelListenTerraUnzipProgressWrapper(listenId);
            Logger.info(TAG, "cancelListenTerraUnzipProgress listenId: " + listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenTerraUnzipProgress] error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = listenUnzipProgressCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }

    /**
     * 订阅模型状态变更推送
     * @param callback 状态变更回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_model_status_changed
     */
    public int listenTerraModelStatusChanged(FlightHubCallback.TerraModelStatusChangedCallback callback) {
        try {
            com.dji.flighthubsdk.generated.TerraModelStatusChangedCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraModelStatusChangedCallback() {
                        @Override
                        public void onTerraModelStatusChanged(com.dji.flighthubsdk.generated.TerraModelStatusChangedMsg msg) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toTerraModelStatusChangedMsg(msg));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[listenTerraModelStatusChanged] callback error: " + e.getMessage(), e);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (listenModelStatusChangedCallbackRef) {
                listenModelStatusChangedCallbackRef.put(-1, container);
                int listenId = this.terraBuildModule.listenTerraModelStatusChangedWrapper(swigCallback);
                listenModelStatusChangedCallbackRef.remove(-1);
                if (listenId >= 0) {
                    listenModelStatusChangedCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                Logger.info(TAG, "listenTerraModelStatusChanged listenId: " + listenId);
                return listenId;
            }
        } catch (Exception e) {
            Logger.error(TAG, "[listenTerraModelStatusChanged] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅模型状态变更推送
     * @param listenId 监听 ID
     */
    public void cancelListenTerraModelStatusChanged(int listenId) {
        try {
            this.terraBuildModule.cancelListenTerraModelStatusChangedWrapper(listenId);
            Logger.info(TAG, "cancelListenTerraModelStatusChanged listenId: " + listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenTerraModelStatusChanged] error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = listenModelStatusChangedCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }

    /**
     * 订阅文件展示地图状态推送
     * @param callback 展示状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_model_file_show_on_map
     */
    public int listenTerraFileShowOnMap(FlightHubCallback.TerraFileShowOnMapCallback callback) {
        try {
            com.dji.flighthubsdk.generated.TerraFileShowOnMapCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraFileShowOnMapCallback() {
                        @Override
                        public void onTerraFileShowOnMap(com.dji.flighthubsdk.generated.TerraFileShowOnMapMsg msg) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toTerraFileShowOnMapMsg(msg));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[listenTerraFileShowOnMap] callback error: " + e.getMessage(), e);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (listenFileShowOnMapCallbackRef) {
                listenFileShowOnMapCallbackRef.put(-1, container);
                int listenId = this.terraBuildModule.listenTerraFileShowOnMapWrapper(swigCallback);
                listenFileShowOnMapCallbackRef.remove(-1);
                if (listenId >= 0) {
                    listenFileShowOnMapCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                Logger.info(TAG, "listenTerraFileShowOnMap listenId: " + listenId);
                return listenId;
            }
        } catch (Exception e) {
            Logger.error(TAG, "[listenTerraFileShowOnMap] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅文件展示地图状态推送
     * @param listenId 监听 ID
     */
    public void cancelListenTerraFileShowOnMap(int listenId) {
        try {
            this.terraBuildModule.cancelListenTerraFileShowOnMapWrapper(listenId);
            Logger.info(TAG, "cancelListenTerraFileShowOnMap listenId: " + listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenTerraFileShowOnMap] error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = listenFileShowOnMapCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }

    /**
     * 获取模型文件详情
     * @param fileId 文件 ID（字符串）
     * @param callback 结果回调（返回模型详情）
     * @implNote 链路: HTTP | GET /pf-terra/api/v1/models/files/{file_id}
     */
    public void getModelDetail(int fileId, FlightHubCallback.TerraGetModelDetailCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraGetModelDetailCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraGetModelDetailCallback() {
                        @Override
                        public void onGetModelDetail(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.TerraModelDetailJavaResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toTerraModelDetailResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getModelDetail] callback error: " + e.getMessage(), e);
                            } finally {
                                getModelDetailCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getModelDetailCallbackRef) {
                getModelDetailCallbackRef.put(id, container);
                this.terraBuildModule.getModelDetailWrapper(fileId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getModelDetail] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }

    /**
     * 设置 LAS 模型坐标系并触发格式转换
     * @param params 参数（fileUuid/wkt 必填）
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-terra/api/v1/models/converters
     */
    public void setLasWkt(com.dji.flighthubsdk.bean.TerraSetLasWktParams params,
            FlightHubCallback.TerraSetLasWktCallback callback) {
        com.dji.flighthubsdk.generated.TerraSetLasWktJavaParams nativeParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TerraSetLasWktCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraSetLasWktCallback() {
                        @Override
                        public void onSetLasWkt(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[setLasWkt] callback error: " + e.getMessage(), e);
                            } finally {
                                setLasWktCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeParams = FlightHubSdkConvert.toNativeTerraSetLasWktParams(params);
            synchronized (setLasWktCallbackRef) {
                setLasWktCallbackRef.put(id, container);
                this.terraBuildModule.setLasWktWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[setLasWkt] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED)); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 订阅模型隐藏地图推送
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_media_file_hide_on_map
     */
    public int listenTerraMediaFileHideOnMap(FlightHubCallback.TerraMediaFileHideOnMapCallback callback) {
        try {
            com.dji.flighthubsdk.generated.TerraMediaFileHideOnMapCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraMediaFileHideOnMapCallback() {
                        @Override
                        public void onTerraMediaFileHideOnMap(com.dji.flighthubsdk.generated.TerraMediaFileHideOnMapMsg msg) {
                            try {
                                if (callback != null) callback.onResult(FlightHubSdkConvert.toTerraMediaFileHideOnMapMsg(msg));
                            } catch (Exception e) {
                                Logger.error(TAG, "[listenTerraMediaFileHideOnMap] callback error: " + e.getMessage(), e);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (listenMediaFileHideOnMapCallbackRef) {
                listenMediaFileHideOnMapCallbackRef.put(-1, container);
                int listenId = this.terraBuildModule.listenTerraMediaFileHideOnMapWrapper(swigCallback);
                listenMediaFileHideOnMapCallbackRef.remove(-1);
                if (listenId >= 0) { listenMediaFileHideOnMapCallbackRef.put(listenId, container); } else { container.release(); }
                Logger.info(TAG, "listenTerraMediaFileHideOnMap listenId: " + listenId);
                return listenId;
            }
        } catch (Exception e) { Logger.error(TAG, "[listenTerraMediaFileHideOnMap] error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅模型隐藏地图推送 */
    public void cancelListenTerraMediaFileHideOnMap(int listenId) {
        try { this.terraBuildModule.cancelListenTerraMediaFileHideOnMapWrapper(listenId); } catch (Exception e) { Logger.error(TAG, "[cancelListenTerraMediaFileHideOnMap] error: " + e.getMessage(), e); }
        CallbackRefContainer r = listenMediaFileHideOnMapCallbackRef.remove(listenId);
        if (r != null) r.release();
    }

    /**
     * 订阅模型显示地图推送
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_media_file_show_on_map
     */
    public int listenTerraMediaFileShowOnMap(FlightHubCallback.TerraMediaFileShowOnMapCallback callback) {
        try {
            com.dji.flighthubsdk.generated.TerraMediaFileShowOnMapCallback swigCallback =
                    new com.dji.flighthubsdk.generated.TerraMediaFileShowOnMapCallback() {
                        @Override
                        public void onTerraMediaFileShowOnMap(com.dji.flighthubsdk.generated.TerraMediaFileShowOnMapMsg msg) {
                            try {
                                if (callback != null) callback.onResult(FlightHubSdkConvert.toTerraMediaFileShowOnMapMsg(msg));
                            } catch (Exception e) {
                                Logger.error(TAG, "[listenTerraMediaFileShowOnMap] callback error: " + e.getMessage(), e);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (listenMediaFileShowOnMapCallbackRef) {
                listenMediaFileShowOnMapCallbackRef.put(-1, container);
                int listenId = this.terraBuildModule.listenTerraMediaFileShowOnMapWrapper(swigCallback);
                listenMediaFileShowOnMapCallbackRef.remove(-1);
                if (listenId >= 0) { listenMediaFileShowOnMapCallbackRef.put(listenId, container); } else { container.release(); }
                Logger.info(TAG, "listenTerraMediaFileShowOnMap listenId: " + listenId);
                return listenId;
            }
        } catch (Exception e) { Logger.error(TAG, "[listenTerraMediaFileShowOnMap] error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅模型显示地图推送 */
    public void cancelListenTerraMediaFileShowOnMap(int listenId) {
        try { this.terraBuildModule.cancelListenTerraMediaFileShowOnMapWrapper(listenId); } catch (Exception e) { Logger.error(TAG, "[cancelListenTerraMediaFileShowOnMap] error: " + e.getMessage(), e); }
        CallbackRefContainer r = listenMediaFileShowOnMapCallbackRef.remove(listenId);
        if (r != null) r.release();
    }
}
