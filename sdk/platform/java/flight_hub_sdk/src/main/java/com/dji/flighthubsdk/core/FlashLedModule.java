// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IFlashLedModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 爆闪灯模板库模块接口，提供 flash-configs CRUD。控灯请走 {@link FlightControlModule#drcSetFlashLed}。
 * PAAS 路径无 project_id（对齐 drc.proto DrcDeviceControlService）。
 */
public class FlashLedModule {

    private static final String TAG = "FlashLedModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> flashLedConfigListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createFlashLedConfigCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateFlashLedConfigCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deleteFlashLedConfigCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IFlashLedModule flashLedModule;

    public FlashLedModule(IFlashLedModule flashLedModule) {
        this.flashLedModule = flashLedModule;
    }

    /**
     * 获取爆闪灯模板列表
     * @param callback 回调，返回模板列表与错误信息
     * @implNote 链路: HTTP | GET /gateway/drc/api/v2/flash-configs
     */
    public void getFlashLedConfigList(FlightHubCallback.FlashLedConfigListCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FlashLedConfigListCallback swigCallback = new com.dji.flighthubsdk.generated.FlashLedConfigListCallback() {
                @Override
                public void onFlashLedConfigList(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.FlashLedConfigList list) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toFlashLedConfigList(list));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getFlashLedConfigList] callback error: " + e.getMessage(), e);
                    } finally {
                        flashLedConfigListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (flashLedConfigListCallbackRef) {
                flashLedConfigListCallbackRef.put(id, container);
                flashLedModule.getFlashLedConfigListWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getFlashLedConfigList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toFlashLedConfigList((com.dji.flighthubsdk.generated.FlashLedConfigList) null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 创建爆闪灯模板
     * @param params 名称 + flash_segments
     * @param callback 回调，返回创建后的配置与错误信息
     * @implNote 链路: HTTP | POST /gateway/drc/api/v2/flash-configs
     */
    public void createFlashLedConfig(com.dji.flighthubsdk.bean.FlashLedConfigParams params, FlightHubCallback.FlashLedConfigCallback callback) {
        com.dji.flighthubsdk.generated.FlashLedConfigParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeFlashLedConfigParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FlashLedConfigCallback swigCallback = new com.dji.flighthubsdk.generated.FlashLedConfigCallback() {
                @Override
                public void onFlashLedConfig(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.FlashLedConfig config) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toFlashLedConfig(config));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[createFlashLedConfig] callback error: " + e.getMessage(), e);
                    } finally {
                        createFlashLedConfigCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (createFlashLedConfigCallbackRef) {
                createFlashLedConfigCallbackRef.put(id, container);
                flashLedModule.createFlashLedConfigWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createFlashLedConfig] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toFlashLedConfig(null));
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
     * 更新爆闪灯模板
     * @param configId 模板 UUID
     * @param params 名称 + flash_segments
     * @param callback 回调，返回更新后的配置与错误信息
     * @implNote 链路: HTTP | PUT /gateway/drc/api/v2/flash-configs/{id}
     */
    public void updateFlashLedConfig(String configId, com.dji.flighthubsdk.bean.FlashLedConfigParams params, FlightHubCallback.FlashLedConfigCallback callback) {
        com.dji.flighthubsdk.generated.FlashLedConfigParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeFlashLedConfigParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FlashLedConfigCallback swigCallback = new com.dji.flighthubsdk.generated.FlashLedConfigCallback() {
                @Override
                public void onFlashLedConfig(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.FlashLedConfig config) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toFlashLedConfig(config));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[updateFlashLedConfig] callback error: " + e.getMessage(), e);
                    } finally {
                        updateFlashLedConfigCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (updateFlashLedConfigCallbackRef) {
                updateFlashLedConfigCallbackRef.put(id, container);
                flashLedModule.updateFlashLedConfigWrapper(configId, nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[updateFlashLedConfig] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toFlashLedConfig(null));
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
     * 删除爆闪灯模板
     * @param configId 模板 UUID
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | DELETE /gateway/drc/api/v2/flash-configs/{id}
     */
    public void deleteFlashLedConfig(String configId, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error(TAG, "[deleteFlashLedConfig] callback error: " + e.getMessage(), e);
                    } finally {
                        deleteFlashLedConfigCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (deleteFlashLedConfigCallbackRef) {
                deleteFlashLedConfigCallbackRef.put(id, container);
                flashLedModule.deleteFlashLedConfigWrapper(configId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteFlashLedConfig] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }
}
