// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 媒体服务模块接口，提供媒体文件查询、删除、下载、上传凭证获取、上传回调等功能。
 */
public class MediaServiceModule {

    private static final String TAG = "MediaServiceModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getMediaFilesListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deleteMediaFilesCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getMediaFilesDownloadURLCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getMediaFilesDownloadURLListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getMediaUploadStsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> mediaUploadCallbackCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> uploadGeoMediaFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getMediaFileDetailByIdCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IMediaServiceModule mediaServiceModule;

    /**
     * 构造媒体服务模块
     * @param mediaServiceModule 底层媒体服务模块接口
     */
    public MediaServiceModule(IMediaServiceModule mediaServiceModule) {
        this.mediaServiceModule = mediaServiceModule;
        Logger.info(TAG, "MediaServiceModule instance created");
    }

    /**
     * 获取媒体文件列表
     * @param requestParams 查询参数（设备 SN、分页、文件类型等）
     * @param callback 结果回调，成功时返回媒体文件列表
     * @implNote 链路: HTTP | GET /pf-media/api/v1/media-files/list
     */
    public void getMediaFilesList(com.dji.flighthubsdk.bean.GetMediaFilesListParams requestParams, FlightHubCallback.GetMediaFilesListCallback callback) {
        com.dji.flighthubsdk.generated.GetMediaFilesListParams nativeGetMediaFilesListParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetMediaFilesListCallback swigCallback = new com.dji.flighthubsdk.generated.GetMediaFilesListCallback() {
                @Override
                public void onGetMediaFilesList(com.dji.flighthubsdk.generated.FlightHubError err, GetMediaFilesListResults results) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetMediaFilesListResults(results));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getMediaFilesList] callback error: " + e.getMessage(), e);
                    } finally {
                        getMediaFilesListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getMediaFilesListCallbackRef) {
                getMediaFilesListCallbackRef.put(id, container);

                nativeGetMediaFilesListParams = FlightHubSdkConvert.toNativeGetMediaFilesListParams(requestParams);
                this.mediaServiceModule.getMediaFilesListWrapper(nativeGetMediaFilesListParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getMediaFilesList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeGetMediaFilesListParams != null) {
                nativeGetMediaFilesListParams.delete();
            }
        }
    }

    /**
     * 获取媒体文件列表
     * @param flightId 航线/飞行任务 ID
     * @param startIndex 起始索引
     * @param pageSize 每页数量
     * @param fileTypes 文件类型列表
     * @param deviceSn 设备序列号
     * @param startCreateTime 创建开始时间
     * @param endCreateTime 创建结束时间
     * @param callback 结果回调，成功时返回媒体文件列表
     * @implNote 链路: HTTP | GET /pf-media/api/v1/media-files/list
     */
    public void getMediaFilesList(
            String flightId,
            int startIndex,
            int pageSize,
            java.util.List<String> fileTypes,
            String deviceSn,
            long startCreateTime,
            long endCreateTime,
            FlightHubCallback.GetMediaFilesListCallback callback) {
        com.dji.flighthubsdk.bean.GetMediaFilesListParams requestParams =
                new com.dji.flighthubsdk.bean.GetMediaFilesListParams();
        requestParams.setFlightId(flightId != null ? flightId : "");
        requestParams.setStartIndex(startIndex);
        requestParams.setPageSize(pageSize);
        requestParams.setFileTypes(fileTypes);
        requestParams.setDeviceSn(deviceSn != null ? deviceSn : "");
        requestParams.setStartCreateTime(startCreateTime);
        requestParams.setEndCreateTime(endCreateTime);
        getMediaFilesList(requestParams, callback);
    }

    /**
     * 批量删除媒体文件
     * @param fileIds 文件 id 列表
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-media/api/v1/media-files/batch-delete
     */
    public void deleteMediaFiles(java.util.List<Long> fileIds, FlightHubCallback.ResultCallbackWithError callback) {
        LongLongVector nativeFileIds = null;
        try {
            nativeFileIds = FlightHubSdkConvert.toNativeLongLongVector(fileIds);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[deleteMediaFiles] callback error: " + e.getMessage(), e);
                    } finally {
                        deleteMediaFilesCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (deleteMediaFilesCallbackRef) {
                deleteMediaFilesCallbackRef.put(id, container);
                this.mediaServiceModule.deleteMediaFilesWrapper(nativeFileIds, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteMediaFiles] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeFileIds != null) {
                nativeFileIds.delete();
            }
        }
    }

    /**
     * 获取媒体文件下载地址
     * @param fileId 文件 ID
     * @param callback 结果回调, 成功时返回下载地址 url
     * @implNote 链路: HTTP | POST /pf-media/api/v1/media-files/url
     */
    public void getMediaFilesDownloadURL(long fileId, FlightHubCallback.GetMediaFilesDownloadUrlCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetMediaFilesDownloadUrlCallback swigCallback = new com.dji.flighthubsdk.generated.GetMediaFilesDownloadUrlCallback() {
                @Override
                public void onGetMediaFilesDownloadUrl(com.dji.flighthubsdk.generated.FlightHubError err, GetMediaFilesDownloadUrlResults results) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetMediaFilesDownloadUrlResults(results));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getMediaFilesDownloadURL] callback error: " + e.getMessage(), e);
                    } finally {
                        getMediaFilesDownloadURLCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getMediaFilesDownloadURLCallbackRef) {
                getMediaFilesDownloadURLCallbackRef.put(id, container);
                this.mediaServiceModule.getMediaFilesDownloadURLWrapper(fileId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getMediaFilesDownloadURL] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 获取媒体文件下载地址
     * @param objectKeys 存储对象key列表
     * @param callback 结果回调, 成功时返回下载地址 url
     * @implNote 链路: HTTP | POST /pf-media/api/v1/media-files/url
     */
    public void getMediaFilesDownloadURL(java.util.List<String> objectKeys, FlightHubCallback.GetMediaFilesDownloadUrlListCallback callback) {
        com.dji.flighthubsdk.generated.StringVector nativeStringVector = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetMediaFilesDownloadUrlListCallback swigCallback = new com.dji.flighthubsdk.generated.GetMediaFilesDownloadUrlListCallback() {
                @Override
                public void onGetMediaFilesDownloadUrlList(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.GetMediaFilesDownloadUrlListResults results) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                FlightHubSdkConvert.toFlightHubError(err),
                                FlightHubSdkConvert.toGetMediaFilesDownloadUrlListResults(results)
                            );
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getMediaFilesDownloadURL(objectKeys)] callback error: " + e.getMessage(), e);
                    } finally {
                        getMediaFilesDownloadURLListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeStringVector = FlightHubSdkConvert.toNativeStringVector(objectKeys);
            synchronized (getMediaFilesDownloadURLListCallbackRef) {
                getMediaFilesDownloadURLListCallbackRef.put(id, container);
                this.mediaServiceModule.getMediaFilesDownloadURLListWrapper(nativeStringVector, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getMediaFilesDownloadURL(objectKeys)] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeStringVector != null) {
                nativeStringVector.delete();
            }
        }
    }

    /**
     * 监听设备媒体后处理完成通知
     * @param deviceSn 设备序列号
     * @param callback 消息回调
     * @return 监听 ID，用于取消监听
     * @implNote 链路: WebSocket | topic: topic_media_file_post_process
     */
    public int listenMediaFilePostProcessMessage(String deviceSn, FlightHubCallback.MediaFilePostProcessCallback callback) {
        try {
            com.dji.flighthubsdk.generated.MediaFilePostProcessCallback swigCallback = new com.dji.flighthubsdk.generated.MediaFilePostProcessCallback() {
                @Override
                public void onMediaFilePostProcess(MediaFilePostProcess msg) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toMediaFilePostProcess(msg));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[listenMediaFilePostProcessMessage] callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.mediaServiceModule.listenMediaFilePostProcessMessageWrapper(deviceSn, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            Logger.info(TAG, "listenMediaFilePostProcessMessage listenId: " + listenId);
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenMediaFilePostProcessMessage] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消监听设备媒体后处理完成通知
     * @param listenId 监听 ID
     */
    public void cancelListenMediaFilePostProcessMessage(int listenId) {
        try {
            this.mediaServiceModule.cancelListenMediaFilePostProcessMessageWrapper(listenId);
            Logger.info(TAG, "cancelListenMediaFilePostProcessMessage listenId: " + listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenMediaFilePostProcessMessage] error: " + e.getMessage(), e);
        }
    }

    /**
     * 监听设备媒体回传通知
     * @param deviceSn 设备序列号
     * @param callback 消息回调
     * @return 监听 ID，用于取消监听
     * @implNote 链路: WebSocket | topic: topic_media_file_device_upload
     */
    public int listenMediaFileDeviceUploadMessage(String deviceSn, FlightHubCallback.MediaFileDeviceUploadCallback callback) {
        try {
            com.dji.flighthubsdk.generated.MediaFileDeviceUploadCallback swigCallback = new com.dji.flighthubsdk.generated.MediaFileDeviceUploadCallback() {
                @Override
                public void onMediaFileDeviceUpload(MediaFileDeviceUpload msg) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toMediaFileDeviceUpload(msg));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[listenMediaFileDeviceUploadMessage] callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.mediaServiceModule.listenMediaFileDeviceUploadMessageWrapper(deviceSn, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            Logger.info(TAG, "listenMediaFileDeviceUploadMessage listenId: " + listenId);
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenMediaFileDeviceUploadMessage] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消监听设备媒体回传通知
     * @param listenId 监听 ID
     */
    public void cancelListenMediaFileDeviceUploadMessage(int listenId) {
        try {
            this.mediaServiceModule.cancelListenMediaFileDeviceUploadMessageWrapper(listenId);
            Logger.info(TAG, "cancelListenMediaFileDeviceUploadMessage listenId: " + listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenMediaFileDeviceUploadMessage] error: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 OSS STS 临时上传凭证
     * @param params 上传凭证请求参数
     * @param callback 结果回调，成功时返回 STS 凭证信息
     * @implNote 链路: HTTP | POST /pf-media/inner/v1/media-files/sts
     */
    public void getMediaUploadSts(com.dji.flighthubsdk.bean.GetMediaUploadStsParams params, FlightHubCallback.GetMediaUploadStsCallback callback) {
        com.dji.flighthubsdk.generated.GetMediaUploadStsParams nativeGetMediaUploadStsParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetMediaUploadStsCallback swigCallback = new com.dji.flighthubsdk.generated.GetMediaUploadStsCallback() {
                @Override
                public void onGetMediaUploadSts(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.GetMediaUploadStsResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetMediaUploadStsResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getMediaUploadSts] callback error: " + e.getMessage(), e);
                    } finally {
                        getMediaUploadStsCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeGetMediaUploadStsParams = FlightHubSdkConvert.toNativeGetMediaUploadStsParams(params);
            synchronized (getMediaUploadStsCallbackRef) {
                getMediaUploadStsCallbackRef.put(id, container);
                this.mediaServiceModule.getMediaUploadStsWrapper(nativeGetMediaUploadStsParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getMediaUploadSts] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeGetMediaUploadStsParams != null) {
                nativeGetMediaUploadStsParams.delete();
            }
        }
    }

    /**
     * 媒体文件上传完成后通知服务端
     * @param params 上传回调参数
     * @param callback 结果回调，成功时返回文件信息
     * @implNote 链路: HTTP | POST /pf-media/api/v1/upload-callback
     */
    public void mediaUploadCallback(com.dji.flighthubsdk.bean.MediaUploadCallbackParams params, FlightHubCallback.MediaUploadCallbackCallback callback) {
        com.dji.flighthubsdk.generated.MediaUploadCallbackParams nativeMediaUploadCallbackParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.MediaUploadCallbackCallback swigCallback = new com.dji.flighthubsdk.generated.MediaUploadCallbackCallback() {
                @Override
                public void onMediaUploadCallback(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.MediaUploadCallbackResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toMediaUploadCallbackResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[mediaUploadCallback] callback error: " + e.getMessage(), e);
                    } finally {
                        mediaUploadCallbackCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeMediaUploadCallbackParams = FlightHubSdkConvert.toNativeMediaUploadCallbackParams(params);
            synchronized (mediaUploadCallbackCallbackRef) {
                mediaUploadCallbackCallbackRef.put(id, container);
                this.mediaServiceModule.mediaUploadCallbackWrapper(nativeMediaUploadCallbackParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[mediaUploadCallback] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeMediaUploadCallbackParams != null) {
                nativeMediaUploadCallbackParams.delete();
            }
        }
    }

    /**
     * 上传地理标注媒体资源（图片备注上传回调）
     * @param params   上传参数（name/objectKey 必填）
     * @param callback 结果回调，成功时返回 file_id/file_uuid/media_file_id
     */
    public void uploadGeoMediaFile(com.dji.flighthubsdk.bean.UploadGeoMediaFileParams params,
            FlightHubCallback.UploadGeoMediaFileCallback callback) {
        com.dji.flighthubsdk.generated.UploadGeoMediaFileParams nativeParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UploadGeoMediaFileCallback swigCallback =
                    new com.dji.flighthubsdk.generated.UploadGeoMediaFileCallback() {
                        @Override
                        public void onUploadGeoMediaFile(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.UploadGeoMediaFileResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toUploadGeoMediaFileResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[uploadGeoMediaFile] callback error: " + e.getMessage(), e);
                            } finally {
                                uploadGeoMediaFileCallbackRef.remove(id);
                            }
                        }
                    };
            nativeParams = new com.dji.flighthubsdk.generated.UploadGeoMediaFileParams();
            nativeParams.setName(params != null && params.getName() != null ? params.getName() : "");
            nativeParams.setObject_key(params != null && params.getObjectKey() != null ? params.getObjectKey() : "");
            if (params != null && params.getFingerprint() != null) nativeParams.setFingerprint(params.getFingerprint());
            if (params != null) nativeParams.setMedia_source(params.getMediaSource());
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (uploadGeoMediaFileCallbackRef) {
                uploadGeoMediaFileCallbackRef.put(id, container);
                this.mediaServiceModule.uploadGeoMediaFileWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[uploadGeoMediaFile] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 根据文件 ID 获取媒体文件详情（轮询文件 URL）
     * @param fileId   文件 ID（files 表主键），必填
     * @param callback 结果回调，成功时返回 url/thumbnail_url 等信息
     */
    public void getMediaFileDetailById(int fileId,
            FlightHubCallback.GetMediaFileDetailByIdCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetMediaFileDetailByIdCallback swigCallback =
                    new com.dji.flighthubsdk.generated.GetMediaFileDetailByIdCallback() {
                        @Override
                        public void onGetMediaFileDetailById(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.GetMediaFileDetailByIdResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toGetMediaFileDetailByIdResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[getMediaFileDetailById] callback error: " + e.getMessage(), e);
                            } finally {
                                getMediaFileDetailByIdCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getMediaFileDetailByIdCallbackRef) {
                getMediaFileDetailByIdCallbackRef.put(id, container);
                this.mediaServiceModule.getMediaFileDetailByIdWrapper(fileId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getMediaFileDetailById] error: " + e.getMessage(), e);
            if (callback != null) try { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null); } catch (Exception ex) { Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex); }
        }
    }
}
