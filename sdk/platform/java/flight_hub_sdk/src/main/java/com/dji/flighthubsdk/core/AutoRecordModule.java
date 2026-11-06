// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IAutoRecordModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 自动录制配置模块（项目级配置，PAAS 路径无 project_id，服务端硬编码默认值）。
 * 对齐 drc.proto PaasAutoRecordService：无设备上下文，与 {@link FlashLedModule} 同构。
 */
public class AutoRecordModule {

    private static final String TAG = "AutoRecordModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getAutoRecordConfigCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> saveAutoRecordConfigCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IAutoRecordModule autoRecordModule;

    public AutoRecordModule(IAutoRecordModule autoRecordModule) {
        this.autoRecordModule = autoRecordModule;
    }

    /**
     * 获取自动录制配置
     * @param callback 回调，返回配置与错误信息；配置不存在时返回全零值配置（非错误）
     * @implNote 链路: HTTP | GET /gateway/drc/api/v2/auto_record/configs
     */
    public void getAutoRecordConfig(FlightHubCallback.AutoRecordConfigCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.AutoRecordConfigCallback swigCallback = new com.dji.flighthubsdk.generated.AutoRecordConfigCallback() {
                @Override
                public void onAutoRecordConfig(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.AutoRecordConfig config) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toAutoRecordConfig(config));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getAutoRecordConfig] callback error: " + e.getMessage(), e);
                    } finally {
                        getAutoRecordConfigCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getAutoRecordConfigCallbackRef) {
                getAutoRecordConfigCallbackRef.put(id, container);
                autoRecordModule.getAutoRecordConfigWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getAutoRecordConfig] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toAutoRecordConfig((com.dji.flighthubsdk.generated.AutoRecordConfig) null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 保存自动录制配置
     * @param params 自动清理时长 + 各类型禁用开关 + 录制目标列表
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | POST /gateway/drc/api/v2/auto_record/configs
     */
    public void saveAutoRecordConfig(com.dji.flighthubsdk.bean.SaveAutoRecordConfigParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SaveAutoRecordConfigParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeSaveAutoRecordConfigParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[saveAutoRecordConfig] callback error: " + e.getMessage(), e);
                    } finally {
                        saveAutoRecordConfigCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (saveAutoRecordConfigCallbackRef) {
                saveAutoRecordConfigCallbackRef.put(id, container);
                autoRecordModule.saveAutoRecordConfigWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[saveAutoRecordConfig] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
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
