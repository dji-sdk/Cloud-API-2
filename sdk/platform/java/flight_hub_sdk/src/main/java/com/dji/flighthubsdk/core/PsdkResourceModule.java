// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.STSInfo;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IPsdkResourceModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * PSDK 资源模块接口，提供喊话器资源、控件资源及音频上传等功能。
 */
public class PsdkResourceModule {

    private static final String TAG = "PsdkResourceModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> psdkSpeakerResourceListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createPsdkSpeakerResourceCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> editPsdkSpeakerResourceCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deletePsdkSpeakerResourceCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stsInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> uploadSpeakerAudioFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> psdkWidgetResourceListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IPsdkResourceModule psdkResourceModule;

    public PsdkResourceModule(IPsdkResourceModule psdkResourceModule) {
        this.psdkResourceModule = psdkResourceModule;
    }

    /**
     * 获取 PSDK 喊话器资源列表
     * @param callback 回调，返回喊话器资源列表与错误信息
     * @implNote 链路: HTTP | GET /pf-media/api/v2/speaker/resources
     */
    public void getPsdkSpeakerResourceList(FlightHubCallback.PsdkSpeakerResourceListCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.PsdkSpeakerResourceListCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkSpeakerResourceListCallback() {
                @Override
                public void onPsdkSpeakerResourceList(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.PsdkSpeakerResourceList psdk_speaker_resource_list) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toPsdkSpeakerResourceList(psdk_speaker_resource_list));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getPsdkSpeakerResourceList] callback error: " + e.getMessage(), e);
                    } finally {
                        psdkSpeakerResourceListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (psdkSpeakerResourceListCallbackRef) {
                psdkSpeakerResourceListCallbackRef.put(id, container);
                psdkResourceModule.getPsdkSpeakerResourceListWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getPsdkSpeakerResourceList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toPsdkSpeakerResourceList((com.dji.flighthubsdk.generated.PsdkSpeakerResourceList) null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 创建 PSDK 喊话器资源
     * @param params 创建参数
     * @param callback 回调，返回创建后的资源与错误信息
     * @implNote 链路: HTTP | POST /pf-media/api/v2/speaker/resources
     */
    public void createPsdkSpeakerResource(com.dji.flighthubsdk.bean.CreatePsdkSpeakerResourceParams params, FlightHubCallback.PsdkSpeakerResourceCallback callback) {
        com.dji.flighthubsdk.generated.CreatePsdkSpeakerResourceParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeCreatePsdkSpeakerResourceParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.PsdkSpeakerResourceCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkSpeakerResourceCallback() {
                @Override
                public void onPsdkSpeakerResource(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.PsdkSpeakerResource psdk_speaker_resource) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toPsdkSpeakerResource(psdk_speaker_resource));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[createPsdkSpeakerResource] callback error: " + e.getMessage(), e);
                    } finally {
                        createPsdkSpeakerResourceCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (createPsdkSpeakerResourceCallbackRef) {
                createPsdkSpeakerResourceCallbackRef.put(id, container);
                psdkResourceModule.createPsdkSpeakerResourceWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createPsdkSpeakerResource] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toPsdkSpeakerResource(null));
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
     * 编辑 PSDK 喊话器资源
     * @param params 编辑参数
     * @param callback 回调，返回更新后的资源与错误信息
     * @implNote 链路: HTTP | PUT /pf-media/api/v2/speaker/resources/{id}
     */
    public void editPsdkSpeakerResource(com.dji.flighthubsdk.bean.EditPsdkSpeakerResourceParams params, FlightHubCallback.PsdkSpeakerResourceCallback callback) {
        com.dji.flighthubsdk.generated.EditPsdkSpeakerResourceParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeEditPsdkSpeakerResourceParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.PsdkSpeakerResourceCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkSpeakerResourceCallback() {
                @Override
                public void onPsdkSpeakerResource(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.PsdkSpeakerResource psdk_speaker_resource) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toPsdkSpeakerResource(psdk_speaker_resource));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[editPsdkSpeakerResource] callback error: " + e.getMessage(), e);
                    } finally {
                        editPsdkSpeakerResourceCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (editPsdkSpeakerResourceCallbackRef) {
                editPsdkSpeakerResourceCallbackRef.put(id, container);
                psdkResourceModule.editPsdkSpeakerResourceWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[editPsdkSpeakerResource] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toPsdkSpeakerResource(null));
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
     * 删除 PSDK 喊话器资源
     * @param resourceId 资源 ID
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | DELETE /pf-media/api/v2/speaker/resources/{id}
     */
    public void deletePsdkSpeakerResource(int resourceId, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                    err.getError_code(),
                                    (int) err.getHttp_code(),
                                    (int) err.getServer_code(),
                                    (int) err.getDevice_code(),
                                    err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[deletePsdkSpeakerResource] callback error: " + e.getMessage(), e);
                    } finally {
                        deletePsdkSpeakerResourceCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (deletePsdkSpeakerResourceCallbackRef) {
                deletePsdkSpeakerResourceCallbackRef.put(id, container);
                psdkResourceModule.deletePsdkSpeakerResourceWrapper(resourceId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deletePsdkSpeakerResource] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 获取 STS 临时凭证
     * @param callback 回调，返回 STS 凭证信息与错误信息
     * @implNote 链路: HTTP | GET /pf-media/api/v2/speaker/sts
     */
    public void getSTS(FlightHubCallback.STSInfoCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.STSInfoCallback swigCallback = new com.dji.flighthubsdk.generated.STSInfoCallback() {
                @Override
                public void onSTSInfo(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.STSInfo sts_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSTSInfo(sts_info));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getSTS] callback error: " + e.getMessage(), e);
                    } finally {
                        stsInfoCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (stsInfoCallbackRef) {
                stsInfoCallbackRef.put(id, container);
                psdkResourceModule.getSTSWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getSTS] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toSTSInfo(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 上传喊话器音频文件
     * @param filePath 本地文件路径
     * @param stsInfo STS 凭证信息
     * @param callback 回调，返回错误信息
     * @implNote 链路: SDK 内部 | 获取 STS 凭证后上传至对象存储
     */
    public void uploadSpeakerAudioFile(String filePath, STSInfo stsInfo, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.STSInfo nativeSts = null;
        try {
            int id = generateActionCallbackId();
            nativeSts = FlightHubSdkConvert.toNativeSTSInfo(stsInfo);
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                    err.getError_code(),
                                    (int) err.getHttp_code(),
                                    (int) err.getServer_code(),
                                    (int) err.getDevice_code(),
                                    err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[uploadSpeakerAudioFile] callback error: " + e.getMessage(), e);
                    } finally {
                        uploadSpeakerAudioFileCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (uploadSpeakerAudioFileCallbackRef) {
                uploadSpeakerAudioFileCallbackRef.put(id, container);
                psdkResourceModule.uploadSpeakerAudioFileWrapper(filePath, nativeSts, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[uploadSpeakerAudioFile] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeSts != null) {
                nativeSts.delete();
            }
        }
    }

    /**
     * 批量获取 PSDK 控件资源
     * @param params 获取参数
     * @param callback 回调，返回控件资源列表与错误信息
     * @implNote 链路: HTTP | POST /control/api/v1/manual/psdk/ui-resources/batch-get
     */
    public void getPsdkWidgetResources(com.dji.flighthubsdk.bean.GetPsdkWidgetResourceParams params, FlightHubCallback.PsdkWidgetResourceListCallback callback) {
        com.dji.flighthubsdk.generated.GetPsdkWidgetResourceParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeGetPsdkWidgetResourceParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.PsdkWidgetResourceListCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkWidgetResourceListCallback() {
                @Override
                public void onPsdkWidgetResourceList(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.PsdkWidgetResourceList psdk_widget_resource_list) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toPsdkWidgetResourceList(psdk_widget_resource_list));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getPsdkWidgetResources] callback error: " + e.getMessage(), e);
                    } finally {
                        psdkWidgetResourceListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (psdkWidgetResourceListCallbackRef) {
                psdkWidgetResourceListCallbackRef.put(id, container);
                psdkResourceModule.getPsdkWidgetResourcesWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getPsdkWidgetResources] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toPsdkWidgetResourceList((com.dji.flighthubsdk.generated.PsdkWidgetResourceList) null));
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
