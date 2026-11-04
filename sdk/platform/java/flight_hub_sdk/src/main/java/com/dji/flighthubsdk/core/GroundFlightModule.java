// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.value.GroundLandingCommand;

/**
 * 舱外起降模块：无人机停放在机场<b>舱外</b>时的起飞与降落回舱。
 *
 * <p>与一键起飞（{@link TakeoffModule#takeoff}，从机场舱内起飞）不同，本模块面向
 * 「飞机已在舱外地面」的场景：起飞不经开舱盖流程；降落由机场自动开舱盖、飞机降落入舱、再关舱盖。
 * 降落过程状态可经 {@link FlightControlModule#listenDrcLandingMissionState} 订阅观察。
 *
 * <p><b>注意：</b>[舱外起飞降落机场] 该功能目前机场3(EA230)高版本固件支持, 如需此功能请升级固件版本.
 * 特别注意: 官方固件未承诺支持前, 本模块接口不作为正式接口使用。
 */
public class GroundFlightModule {

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> groundTakeoffCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> groundLandingCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IGroundFlightModule groundFlightModule;

    public GroundFlightModule(IGroundFlightModule groundFlightModule) {
        this.groundFlightModule = groundFlightModule;
    }

    /**
     * 舱外起飞
     * @param params 舱外起飞参数
     * @param callback 结果回调（GroundTakeoffResult：flightId）
     */
    public void groundTakeoff(com.dji.flighthubsdk.bean.GroundTakeoffParams params,
            FlightHubCallback.GroundTakeoffCallback callback) {
        com.dji.flighthubsdk.generated.GroundTakeoffParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeGroundTakeoffParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GroundTakeoffResultCallback swigCallback = new com.dji.flighthubsdk.generated.GroundTakeoffResultCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.GroundTakeoffResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGroundTakeoffResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("GroundFlightModule", "GroundTakeoff callback error: " + e.getMessage(), e);
                    } finally {
                        groundTakeoffCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (groundTakeoffCallbackRef) {
                groundTakeoffCallbackRef.put(id, container);
                this.groundFlightModule.groundTakeoffWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("GroundFlightModule", "groundTakeoff error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        new com.dji.flighthubsdk.bean.GroundTakeoffResult(""));
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 舱外降落
     * @param cmd 降落指令：{@link GroundLandingCommand#LAND_TO_DOCK} 降落至机场
     *            （机场自动开舱盖→无人机降落→关舱盖）；{@link GroundLandingCommand#CANCEL} 取消降落（悬停）
     * @param callback 结果回调
     */
    public void groundLanding(GroundLandingCommand cmd, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            final int cmdValue = cmd != null ? cmd.getValue() : GroundLandingCommand.LAND_TO_DOCK.getValue();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("GroundFlightModule", "GroundLanding callback error: " + e.getMessage(), e);
                    } finally {
                        groundLandingCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (groundLandingCallbackRef) {
                groundLandingCallbackRef.put(id, container);
                this.groundFlightModule.groundLandingWrapper(cmdValue, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("GroundFlightModule", "groundLanding error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }
}
