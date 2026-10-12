// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * AI Box 模块接口，提供盒子信息查询、应用安装/操作、文件导入导出等功能。
 */
public class AIBoxModule {

    private static final String TAG = "AIBoxModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> fetchAIBoxInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> fetchFileDownloadUrlCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> fetchAppListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> fetchAppListProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> exportFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> exportFileProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> uploadAIBoxFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> installAppCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> installAppProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> operateAppCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IAIBoxModule aiBoxModule;

    public AIBoxModule(IAIBoxModule module) {
        this.aiBoxModule = module;
    }

    /**
     * 查询 AI Box 信息
     * @param deviceSn 设备序列号
     * @param callback 回调，返回 AI Box 信息列表与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/ai-box-info
     */
    public void fetchAIBoxInfo(String deviceSn, FlightHubCallback.FetchAIBoxInfoCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FetchAIBoxInfoCallback swigCallback =
                    new com.dji.flighthubsdk.generated.FetchAIBoxInfoCallback() {
                @Override
                public void onFetchAIBoxInfo(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.AIBoxInfoListResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toAIBoxInfoListResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "fetchAIBoxInfo callback error: " + e.getMessage(), e);
                    } finally {
                        fetchAIBoxInfoCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (fetchAIBoxInfoCallbackRef) {
                fetchAIBoxInfoCallbackRef.put(id, container);
                aiBoxModule.fetchAIBoxInfoWrapper(deviceSn != null ? deviceSn : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "fetchAIBoxInfo error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toAIBoxInfoListResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 获取文件下载链接
     * @param param 请求参数
     * @param callback 回调，返回下载链接列表与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/file-download-url
     */
    public void fetchFileDownloadUrl(com.dji.flighthubsdk.bean.FetchFileDownloadUrlParam param,
            FlightHubCallback.FetchFileDownloadUrlCallback callback) {
        com.dji.flighthubsdk.generated.FetchFileDownloadUrlParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeFetchFileDownloadUrlParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FetchFileDownloadUrlCallback swigCallback =
                    new com.dji.flighthubsdk.generated.FetchFileDownloadUrlCallback() {
                @Override
                public void onFetchFileDownloadUrl(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.FileDownloadUrlListResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFileDownloadUrlListResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "fetchFileDownloadUrl callback error: " + e.getMessage(), e);
                    } finally {
                        fetchFileDownloadUrlCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (fetchFileDownloadUrlCallbackRef) {
                fetchFileDownloadUrlCallbackRef.put(id, container);
                aiBoxModule.fetchFileDownloadUrlWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "fetchFileDownloadUrl error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toFileDownloadUrlListResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 获取 AI Box 应用列表
     * @param param 请求参数
     * @param callback 回调，返回应用列表与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/app-list
     */
    public void fetchAppList(com.dji.flighthubsdk.bean.AIBoxFetchAppListParam param,
            FlightHubCallback.FetchAppListCallback callback) {
        com.dji.flighthubsdk.generated.FetchAppListParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeFetchAppListParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FetchAppListCallback swigCallback =
                    new com.dji.flighthubsdk.generated.FetchAppListCallback() {
                @Override
                public void onFetchAppList(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.FetchAppListResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFetchAppListResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "fetchAppList callback error: " + e.getMessage(), e);
                    } finally {
                        fetchAppListCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (fetchAppListCallbackRef) {
                fetchAppListCallbackRef.put(id, container);
                aiBoxModule.fetchAppListWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "fetchAppList error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toFetchAppListResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 查询获取应用列表进度
     * @param param 请求参数
     * @param callback 回调，返回进度信息与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/app-list-progress
     */
    public void fetchAppListProgress(com.dji.flighthubsdk.bean.AIBoxFetchAppListProgressParam param,
            FlightHubCallback.FetchAppListProgressCallback callback) {
        com.dji.flighthubsdk.generated.FetchAppListProgressParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeFetchAppListProgressParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FetchAppListProgressCallback swigCallback =
                    new com.dji.flighthubsdk.generated.FetchAppListProgressCallback() {
                @Override
                public void onFetchAppListProgress(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.FetchAppListProgressResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFetchAppListProgressResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "fetchAppListProgress callback error: " + e.getMessage(), e);
                    } finally {
                        fetchAppListProgressCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (fetchAppListProgressCallbackRef) {
                fetchAppListProgressCallbackRef.put(id, container);
                aiBoxModule.fetchAppListProgressWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "fetchAppListProgress error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toFetchAppListProgressResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 导出 AI Box 文件
     * @param param 导出参数
     * @param callback 回调，返回导出结果与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/export-file
     */
    public void exportFile(com.dji.flighthubsdk.bean.AIBoxExportFileParam param,
            FlightHubCallback.ExportFileCallback callback) {
        com.dji.flighthubsdk.generated.ExportFileParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeExportFileParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ExportFileCallback swigCallback =
                    new com.dji.flighthubsdk.generated.ExportFileCallback() {
                @Override
                public void onExportFile(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.ExportFileResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toExportFileResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "exportFile callback error: " + e.getMessage(), e);
                    } finally {
                        exportFileCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (exportFileCallbackRef) {
                exportFileCallbackRef.put(id, container);
                aiBoxModule.exportFileWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "exportFile error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toExportFileResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 查询导出文件进度
     * @param param 请求参数
     * @param callback 回调，返回导出进度与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/export-file-progress
     */
    public void exportFileProgress(com.dji.flighthubsdk.bean.AIBoxExportFileProgressParam param,
            FlightHubCallback.ExportFileProgressCallback callback) {
        com.dji.flighthubsdk.generated.ExportFileProgressParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeExportFileProgressParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ExportFileProgressCallback swigCallback =
                    new com.dji.flighthubsdk.generated.ExportFileProgressCallback() {
                @Override
                public void onExportFileProgress(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.ExportFileProgressResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toExportFileProgressResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "exportFileProgress callback error: " + e.getMessage(), e);
                    } finally {
                        exportFileProgressCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (exportFileProgressCallbackRef) {
                exportFileProgressCallbackRef.put(id, container);
                aiBoxModule.exportFileProgressWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "exportFileProgress error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toExportFileProgressResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 上传 AI Box 文件
     * @param filePath 本地文件路径
     * @param deviceSn 设备序列号
     * @param boxSn AI Box 序列号
     * @param callback 回调，返回上传结果与错误信息
     * @implNote 链路: SDK 内部 | 获取 STS 凭证后上传 AI Box 文件
     */
    public void uploadAIBoxFile(String filePath, String deviceSn, String boxSn, FlightHubCallback.UploadAIBoxFileCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UploadAIBoxFileCallback swigCallback =
                    new com.dji.flighthubsdk.generated.UploadAIBoxFileCallback() {
                @Override
                public void onUploadAIBoxFile(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.UploadFileResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toUploadFileResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "uploadAIBoxFile callback error: " + e.getMessage(), e);
                    } finally {
                        uploadAIBoxFileCallbackRef.remove(id);
                    }
                }
            };
            com.dji.flighthubsdk.generated.UploadFileParam param = new com.dji.flighthubsdk.generated.UploadFileParam();
            param.setFile_path(filePath != null ? filePath : "");
            param.setDevice_sn(deviceSn != null ? deviceSn : "");
            param.setBox_sn(boxSn != null ? boxSn : "");
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (uploadAIBoxFileCallbackRef) {
                uploadAIBoxFileCallbackRef.put(id, container);
                aiBoxModule.uploadAIBoxFileWrapper(param, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "uploadAIBoxFile error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toUploadFileResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 安装 AI Box 应用
     * @param param 安装参数
     * @param callback 回调，返回安装结果与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/install-app
     */
    public void installApp(com.dji.flighthubsdk.bean.AIBoxInstallAppParam param,
            FlightHubCallback.InstallAppCallback callback) {
        com.dji.flighthubsdk.generated.InstallAppParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeInstallAppParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.InstallAppCallback swigCallback =
                    new com.dji.flighthubsdk.generated.InstallAppCallback() {
                @Override
                public void onInstallApp(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.InstallAppResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toInstallAppResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "installApp callback error: " + e.getMessage(), e);
                    } finally {
                        installAppCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (installAppCallbackRef) {
                installAppCallbackRef.put(id, container);
                aiBoxModule.installAppWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "installApp error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toInstallAppResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 查询安装应用进度
     * @param param 请求参数
     * @param callback 回调，返回安装进度与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/install-app-progress
     */
    public void installAppProgress(com.dji.flighthubsdk.bean.AIBoxInstallAppProgressParam param,
            FlightHubCallback.InstallAppProgressCallback callback) {
        com.dji.flighthubsdk.generated.InstallAppProgressParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeInstallAppProgressParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.InstallAppProgressCallback swigCallback =
                    new com.dji.flighthubsdk.generated.InstallAppProgressCallback() {
                @Override
                public void onInstallAppProgress(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.InstallAppProgressResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toInstallAppProgressResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "installAppProgress callback error: " + e.getMessage(), e);
                    } finally {
                        installAppProgressCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (installAppProgressCallbackRef) {
                installAppProgressCallbackRef.put(id, container);
                aiBoxModule.installAppProgressWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "installAppProgress error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toInstallAppProgressResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 操作 AI Box 应用（启动/停止等）
     * @param param 操作参数
     * @param callback 回调，返回操作结果与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/aibox/operate-app
     */
    public void operateApp(com.dji.flighthubsdk.bean.AIBoxOperateAppParam param,
            FlightHubCallback.OperateAppCallback callback) {
        com.dji.flighthubsdk.generated.OperateAppParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeOperateAppParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.OperateAppCallback swigCallback =
                    new com.dji.flighthubsdk.generated.OperateAppCallback() {
                @Override
                public void onOperateApp(com.dji.flighthubsdk.generated.FlightHubError err,
                        com.dji.flighthubsdk.generated.OperateAppResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toOperateAppResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "operateApp callback error: " + e.getMessage(), e);
                    } finally {
                        operateAppCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (operateAppCallbackRef) {
                operateAppCallbackRef.put(id, container);
                aiBoxModule.operateAppWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "operateApp error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toOperateAppResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }
}
