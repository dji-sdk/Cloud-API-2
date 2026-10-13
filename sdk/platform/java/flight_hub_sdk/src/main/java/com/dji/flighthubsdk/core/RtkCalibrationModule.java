// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IRtkCalibrationModule;
import com.dji.flighthubsdk.generated.RtkCalibrationCallback;
import com.dji.flighthubsdk.generated.SubscribeCalibrationProgressCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * RTK 标定模块，提供网络/手动标定与进度订阅。
 */
public class RtkCalibrationModule {

    private class CallbackRefContainer {
        private Object callback_jni_ref;
        private Object callback_java_ref;

        CallbackRefContainer(Object callback_jni_ref, Object callback_java_ref) {
            this.callback_jni_ref = callback_jni_ref;
            this.callback_java_ref = callback_java_ref;
        }

        void release() {
            if (callback_jni_ref != null) {
                try {
                    callback_jni_ref.getClass().getMethod("delete").invoke(callback_jni_ref);
                } catch (Exception ignored) {
                }
                callback_jni_ref = null;
            }
        }
    }

    private final IRtkCalibrationModule rtkCalibrationModule;
    private int id_counter = 0;

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> calibrationCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();

    public RtkCalibrationModule(IRtkCalibrationModule rtkCalibrationModule) {
        this.rtkCalibrationModule = rtkCalibrationModule;
    }

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    /**
     * RTK 标定（网络模式）
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/rtk-calibration
     */
    public void calibrationByNetwork(com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams params,
                                     FlightHubCallback.RtkCalibrationCallback callback) {
        try {
            int id = generateActionCallbackId();
            RtkCalibrationCallback swigCallback = new RtkCalibrationCallback() {
                @Override
                public void onRtkCalibration(com.dji.flighthubsdk.generated.FlightHubError err,
                                             com.dji.flighthubsdk.generated.RtkCalibrationResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toRtkCalibrationResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("RtkCalibrationModule", "[calibrationByNetwork] callback error: " + e.getMessage(), e);
                    } finally {
                        calibrationCallbackRef.remove(id);
                    }
                }
            };
            synchronized (calibrationCallbackRef) {
                calibrationCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                com.dji.flighthubsdk.generated.NetworkRtkCalibrationParams nativeParams =
                        FlightHubSdkConvert.toNativeNetworkRtkCalibrationParams(params);
                try {
                    rtkCalibrationModule.calibrationByNetworkWrapper(nativeParams, swigCallback);
                } finally {
                    if (nativeParams != null) {
                        nativeParams.delete();
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("RtkCalibrationModule", "[calibrationByNetwork] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toRtkCalibrationResult(null));
            }
        }
    }

    /**
     * RTK 标定（手动模式）
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/rtk-calibration
     */
    public void calibrationByManual(com.dji.flighthubsdk.bean.ManulRtkCalibrationParams params,
                                    FlightHubCallback.RtkCalibrationCallback callback) {
        try {
            int id = generateActionCallbackId();
            RtkCalibrationCallback swigCallback = new RtkCalibrationCallback() {
                @Override
                public void onRtkCalibration(com.dji.flighthubsdk.generated.FlightHubError err,
                                             com.dji.flighthubsdk.generated.RtkCalibrationResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toRtkCalibrationResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("RtkCalibrationModule", "[calibrationByManual] callback error: " + e.getMessage(), e);
                    } finally {
                        calibrationCallbackRef.remove(id);
                    }
                }
            };
            synchronized (calibrationCallbackRef) {
                calibrationCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                com.dji.flighthubsdk.generated.ManulRtkCalibrationParams nativeParams =
                        FlightHubSdkConvert.toNativeManulRtkCalibrationParams(params);
                try {
                    rtkCalibrationModule.calibrationByManualWrapper(nativeParams, swigCallback);
                } finally {
                    if (nativeParams != null) {
                        nativeParams.delete();
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("RtkCalibrationModule", "[calibrationByManual] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toRtkCalibrationResult(null));
            }
        }
    }

    /**
     * 订阅 RTK 标定进度
     * @implNote 链路: WebSocket | topic: topic_device_rtk_calibration
     */
    public int subscribeCalibrationProgress(FlightHubCallback.SubscribeCalibrationProgressCallback callback) {
        try {
            SubscribeCalibrationProgressCallback swigCallback = new SubscribeCalibrationProgressCallback() {
                @Override
                public void onSubscribeCalibrationProgress(com.dji.flighthubsdk.generated.RtkCalibrationResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toRtkCalibrationResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("RtkCalibrationModule", "[subscribeCalibrationProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = rtkCalibrationModule.subscribeCalibrationProgressWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("RtkCalibrationModule", "[subscribeCalibrationProgress] error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅 RTK 标定进度
     */
    public void unsubscribeCalibrationProgress(int listenId) {
        try {
            rtkCalibrationModule.unsubscribeCalibrationProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("RtkCalibrationModule", "unsubscribeCalibrationProgress error: " + e.getMessage(), e);
        }
    }
}
