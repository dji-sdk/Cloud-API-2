// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IFlightTaskMediaUploadModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * 飞行任务媒体上传模块，提供媒体优先上传（下行）与最高优先级上传监听（上行）能力。
 */
public class FlightTaskMediaUploadModule {

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

    // 下行 prioritize 回调引用容器，防止 GC 回收 JNI director
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> prioritizeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private final IFlightTaskMediaUploadModule module;
    private final String sn;
    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public FlightTaskMediaUploadModule(IFlightTaskMediaUploadModule module, String sn) {
        this.module = module;
        this.sn = sn;
    }

    // ==================== 飞行任务媒体优先上传（下行 services） ====================

    /**
     * 下发飞行任务媒体优先上传（下行，云端→dock）
     * @param flightId 飞行任务 ID
     * @param callback 结果回调，error 为 SUCCESS 表示设备接受（result==0）
     * @implNote 链路: MQTT services | method: upload_flighttask_media_prioritize
     */
    public void uploadFlighttaskMediaPrioritize(String flightId, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("FlightTaskMediaUploadModule", "[" + sn + "] prioritize callback error: " + e.getMessage(), e);
                    } finally {
                        prioritizeCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (prioritizeCallbackRef) {
                prioritizeCallbackRef.put(id, container);
                this.module.uploadFlighttaskMediaPrioritizeWrapper(flightId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightTaskMediaUploadModule", "[" + sn + "] uploadFlighttaskMediaPrioritize error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ==================== 飞行任务媒体最高优先级上传（上行 events 监听） ====================

    /**
     * 订阅飞行任务媒体最高优先级上传（上行，dock→云端）
     * @param callback 事件回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_highest_priority_upload_flighttask_media
     */
    public int listenHighestPriorityUploadFlighttaskMedia(FlightHubCallback.HighestPriorityUploadFlighttaskMediaCallback callback) {
        try {
            com.dji.flighthubsdk.generated.HighestPriorityUploadFlighttaskMediaCallback swigCallback =
                new com.dji.flighthubsdk.generated.HighestPriorityUploadFlighttaskMediaCallback() {
                    @Override
                    public void onHighestPriorityUploadFlighttaskMedia(com.dji.flighthubsdk.generated.HighestPriorityUploadFlighttaskMedia msg) {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toHighestPriorityUploadFlighttaskMedia(msg));
                        }
                    }
                };

            int listenId = this.module.listenHighestPriorityUploadFlighttaskMediaWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("FlightTaskMediaUploadModule", "[" + sn + "] listenHighestPriorityUploadFlighttaskMedia error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅飞行任务媒体最高优先级上传
     * @param listenId 监听 ID
     */
    public void cancelListenHighestPriorityUploadFlighttaskMedia(int listenId) {
        try {
            this.module.cancelListenHighestPriorityUploadFlighttaskMediaWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlightTaskMediaUploadModule", "[" + sn + "] cancelListenHighestPriorityUploadFlighttaskMedia error: " + e.getMessage(), e);
        }
    }
}