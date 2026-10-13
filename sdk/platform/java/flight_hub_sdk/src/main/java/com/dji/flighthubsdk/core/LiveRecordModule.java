// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.ILiveRecordModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * 直播录制模块接口，提供直播流的录制启停、录制记录查询、录制状态订阅等功能。
 */
public class LiveRecordModule {

    private class CallbackRefContainer {
        private Object callbackJniRef;
        private Object callbackJavaRef;

        CallbackRefContainer(Object callbackJniRef, Object callbackJavaRef) {
            this.callbackJniRef = callbackJniRef;
            this.callbackJavaRef = callbackJavaRef;
        }

        public void release() {
            if (callbackJniRef != null) {
                try {
                    callbackJniRef.getClass().getMethod("delete").invoke(callbackJniRef);
                } catch (Exception ignored) {
                }
                callbackJniRef = null;
            }
        }
    }

    private final ILiveRecordModule module;
    private final String deviceSn;

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getRecordsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int actionIdCounter = 0;
    private synchronized int nextActionId() {
        return actionIdCounter++;
    }

    /**
     * 构造直播录制模块
     * @param module 底层直播录制模块接口
     * @param deviceSn 设备序列号
     */
    public LiveRecordModule(ILiveRecordModule module, String deviceSn) {
        this.module = module;
        this.deviceSn = deviceSn;
    }

    /** @deprecated: 注意，本接口暂未开放使用 */
    /**
     * 开始录制当前直播流
     * @param timeZone 时区（可选），合法取值如：America/Sao_Paulo, Africa/Johannesburg, Asia/Shanghai
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-live/api/v1/record/start
     */
    public void startLiveRecord(String timeZone, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            String tz = (timeZone != null) ? timeZone : "";
            final int id = nextActionId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                    } catch (Exception e) {
                        Logger.error("LiveRecordModule", "startLiveRecord callback error: " + e.getMessage(), e);
                    } finally {
                        startCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (startCallbackRef) {
                startCallbackRef.put(id, container);
                module.startLiveRecordWrapper(tz, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LiveRecordModule", "startLiveRecord error: " + e.getMessage(), e);
            if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
        }
    }

    /** @deprecated: 注意，本接口暂未开放使用 */
    /**
     * 停止录制当前直播流
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-live/api/v1/record/stop
     */
    public void stopLiveRecord(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = nextActionId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                    } catch (Exception e) {
                        Logger.error("LiveRecordModule", "stopLiveRecord callback error: " + e.getMessage(), e);
                    } finally {
                        stopCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (stopCallbackRef) {
                stopCallbackRef.put(id, container);
                module.stopLiveRecordWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LiveRecordModule", "stopLiveRecord error: " + e.getMessage(), e);
            if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
        }
    }

    /** @deprecated: 注意，本接口暂未开放使用 */
    /**
     * 按设备 SN 查询录制记录列表
     * @param deviceSn 设备序列号
     * @param callback 结果回调，成功时返回录制记录列表
     * @implNote 链路: HTTP | GET /pf-live/api/v1/record/device/{sn}
     */
    public void getDeviceLiveRecords(String deviceSn, FlightHubCallback.GetDeviceLiveRecordsCallback callback) {
        try {
            final int id = nextActionId();
            com.dji.flighthubsdk.generated.GetDeviceLiveRecordsCallback swigCallback = new com.dji.flighthubsdk.generated.GetDeviceLiveRecordsCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.LiveRecordItemVector records) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toLiveRecordItemList(records));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveRecordModule", "getDeviceLiveRecords callback error: " + e.getMessage(), e);
                    } finally {
                        getRecordsCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getRecordsCallbackRef) {
                getRecordsCallbackRef.put(id, container);
                module.getDeviceLiveRecordsWrapper(deviceSn != null ? deviceSn : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LiveRecordModule", "getDeviceLiveRecords error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN), java.util.Collections.emptyList());
            }
        }
    }

    /** @deprecated: 注意，本接口暂未开放使用 */
    /**
     * 订阅录制状态推送
     * @param callback 录制状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_live_record_info
     */
    public int listenRecordState(FlightHubCallback.LiveRecordStateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.LiveRecordStateCallback swigCallback = new com.dji.flighthubsdk.generated.LiveRecordStateCallback() {
                @Override
                public void onRecordState(com.dji.flighthubsdk.generated.LiveRecordStateInfo info) {
                    try {
                        if (callback != null) {
                            callback.onRecordState(FlightHubSdkConvert.toLiveRecordStateInfo(info));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveRecordModule", "listenRecordState callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = module.listenRecordStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("LiveRecordModule", "listenRecordState error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** @deprecated: 注意，本接口暂未开放使用 */
    /**
     * 取消订阅录制状态
     * @param listenId 订阅时返回的监听 ID
     */
    public void cancelListenRecordState(int listenId) {
        try {
            module.cancelListenRecordState(listenId);
        } catch (Exception e) {
            Logger.error("LiveRecordModule", "cancelListenRecordState error: " + e.getMessage(), e);
        }
    }
}
