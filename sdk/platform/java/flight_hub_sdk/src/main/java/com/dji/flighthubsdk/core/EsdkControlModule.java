// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IEsdkControlModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * ESDK 控制模块接口，提供 ESDK（dock 扩展口）自定义数据透传能力。
 */
public class EsdkControlModule {

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

    // 下行 send 回调引用容器，防止 GC 回收 JNI director
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> sendCustomDataToEsdkCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private final IEsdkControlModule esdkControlModule;
    private final String sn;
    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public EsdkControlModule(IEsdkControlModule esdkControlModule, String sn) {
        this.esdkControlModule = esdkControlModule;
        this.sn = sn;
    }

    // ==================== ESDK 自定义数据透传（上行） ====================

    /**
     * 订阅 ESDK 自定义数据透传（上行，ESDK→云端）
     * @param callback 自定义数据回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_custom_data_transmission_from_esdk
     */
    public int listenEsdkCustomDataTransmissionFromEsdk(FlightHubCallback.EsdkCustomDataTransmissionFromEsdkCallback callback) {
        try {
            com.dji.flighthubsdk.generated.EsdkCustomDataTransmissionFromEsdkCallback swigCallback =
                new com.dji.flighthubsdk.generated.EsdkCustomDataTransmissionFromEsdkCallback() {
                    @Override
                    public void onEsdkCustomDataTransmissionFromEsdk(com.dji.flighthubsdk.generated.EsdkCustomDataTransmission msg) {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toEsdkCustomDataTransmission(msg));
                        }
                    }
                };

            int listenId = this.esdkControlModule.listenEsdkCustomDataTransmissionFromEsdkWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("EsdkControlModule", "[" + sn + "] listenEsdkCustomDataTransmissionFromEsdk error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 ESDK 自定义数据透传
     * @param listenId 监听 ID
     */
    public void cancelListenEsdkCustomDataTransmissionFromEsdk(int listenId) {
        try {
            this.esdkControlModule.cancelListenEsdkCustomDataTransmissionFromEsdkWrapper(listenId);
        } catch (Exception e) {
            Logger.error("EsdkControlModule", "[" + sn + "] cancelListenEsdkCustomDataTransmissionFromEsdk error: " + e.getMessage(), e);
        }
    }

    // ==================== ESDK 自定义数据透传（下行） ====================

    /**
     * 下发 ESDK 自定义数据透传（下行，云端→ESDK）
     * @param value 数据内容（长度 < 256）
     * @param callback 结果回调，error 为 SUCCESS 表示设备接受（result==0）
     * @implNote 链路: MQTT services | method: custom_data_transmission_to_esdk
     */
    public void sendEsdkCustomDataTransmissionToEsdk(String value, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("EsdkControlModule", "[" + sn + "] send custom data to esdk callback error: " + e.getMessage(), e);
                    } finally {
                        sendCustomDataToEsdkCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (sendCustomDataToEsdkCallbackRef) {
                sendCustomDataToEsdkCallbackRef.put(id, container);
                this.esdkControlModule.sendEsdkCustomDataTransmissionToEsdkWrapper(value, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("EsdkControlModule", "[" + sn + "] sendEsdkCustomDataTransmissionToEsdk error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

}
