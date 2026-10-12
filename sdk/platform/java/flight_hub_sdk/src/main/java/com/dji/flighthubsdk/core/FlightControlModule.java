// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.generated.IFlightControlModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 飞行控制模块接口，提供紧急停桨/降落、返航、摇杆控制及相关能力订阅。
 */
public class FlightControlModule {

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> emergencyStopActionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> emergencyLandingActionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> forceLandingActionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> returnHomeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> returnHomeCancelCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IFlightControlModule flightControlModule;

    public FlightControlModule(IFlightControlModule flightControlModule) {
        this.flightControlModule = flightControlModule;
    }

    /**
     * 紧急停桨
     * @implNote 链路: DRC | method: drone_emergency_stop
     */
    public void drcEmergencyStop(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
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
                        Logger.error("FlightControlModule", "Emergency stop callback error: " + e.getMessage(), e);
                    } finally {
                        emergencyStopActionCallbackRef.remove(id);
                    }
                }
            };

            synchronized (emergencyStopActionCallbackRef) {
                emergencyStopActionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.flightControlModule.drcEmergencyStopWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "drcEmergencyStop error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 紧急降落
     * @implNote 链路: DRC | method: drc_emergency_landing
     */
    public void drcEmergencyLanding(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
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
                        Logger.error("FlightControlModule", "Emergency landing callback error: " + e.getMessage(), e);
                    } finally {
                        emergencyLandingActionCallbackRef.remove(id);
                    }
                }
            };

            synchronized (emergencyLandingActionCallbackRef) {
                emergencyLandingActionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.flightControlModule.drcEmergencyLandingWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "drcEmergencyLanding error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 强制降落
     * @implNote 链路: DRC | method: drc_force_landing
     */
    public void drcForceLanding(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
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
                        Logger.error("FlightControlModule", "Force landing callback error: " + e.getMessage(), e);
                    } finally {
                        forceLandingActionCallbackRef.remove(id);
                    }
                }
            };

            synchronized (forceLandingActionCallbackRef) {
                forceLandingActionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.flightControlModule.drcForceLandingWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "drcForceLanding error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 返航
     * @implNote 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd (method: return_home)
     */
    public void returnHome(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("FlightControlModule", "Return home callback error: " + e.getMessage(), e);
                    } finally {
                        returnHomeCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (returnHomeCallbackRef) {
                returnHomeCallbackRef.put(id, container);
                this.flightControlModule.returnHomeWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "returnHome error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 取消返航
     * @implNote 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd (method: return_home_cancel)
     */
    public void returnHomeCancel(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("FlightControlModule", "Return home cancel callback error: " + e.getMessage(), e);
                    } finally {
                        returnHomeCancelCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (returnHomeCancelCallbackRef) {
                returnHomeCancelCallbackRef.put(id, container);
                this.flightControlModule.returnHomeCancelWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "returnHomeCancel error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 发送摇杆控制量（DRC 虚拟摇杆）
     * @param data 摇杆控制数据；各轴中性 1024，建议操控范围 364–1684
     * 需先 connectDrcLink 并持有飞行控制权
     * @implNote 链路: DRC | method: stick_control (高频 Southbound；无 Reply)
     */
    public void drcSendStickControl(StickControlData data) {
        com.dji.flighthubsdk.generated.StickControlData nativeData = null;
        try {
            nativeData = FlightHubSdkConvert.toNativeStickControlData(data);
            this.flightControlModule.drcSendStickControlWrapper(nativeData);
            Logger.info("FlightControlModule", "Send stick control data sent successfully");
        } catch (Exception e) {
            Logger.error("FlightControlModule", "drcSendStickControl error: " + e.getMessage(), e);
        } finally {
            if (nativeData != null) {
                nativeData.delete();
            }
        }
    }

    // ==================== 新增 DRC 飞行控制方法 ====================

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> flightActionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private void invokeDrcFlightAction(FlightHubCallback.ResultCallbackWithError callback,
                                        java.util.function.Consumer<com.dji.flighthubsdk.generated.ResultCallbackWithError> nativeCall) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCb =
                new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                        try {
                            if (callback != null) {
                                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                    err.getError_code(), (int) err.getHttp_code(),
                                    (int) err.getServer_code(), (int) err.getDevice_code(),
                                    err.getServer_message()));
                            }
                        } catch (Exception e) {
                            Logger.error("FlightControlModule", "drc action callback error: " + e.getMessage(), e);
                        } finally {
                            flightActionCallbackRef.remove(id);
                        }
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (flightActionCallbackRef) {
                flightActionCallbackRef.put(id, container);
                nativeCall.accept(swigCb);
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "drc action error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ── Send methods ──

    /**
     * 悬停设置。仅支持大疆机场 4（DJI Dock 4）搭载机型，其他机型不支持。
     * @param duration 悬停时长（毫秒）。取值范围 [0, 1800000]；0 表示取消悬停保活定时器，超出范围将被截断到 [0, 1800000] 且不报错
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_drone_hover_set
     */
    public void drcSetDroneHover(int duration, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcFlightAction(callback, cb -> this.flightControlModule.drcSetDroneHoverWrapper(duration, cb));
    }

    /**
     * 指哪打哪参数设置
     * @implNote 链路: DRC | method: drc_follow_mouse_parameter_set
     */
    public void drcSetFollowMouseParameter(int speed, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcFlightAction(callback, cb -> this.flightControlModule.drcSetFollowMouseParameterWrapper(speed, cb));
    }

    /**
     * 夜航灯设置
     * @implNote 链路: DRC | method: drc_night_lights_state_set
     */
    public void drcSetNightLightsState(int nightLightsState, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcFlightAction(callback, cb -> this.flightControlModule.drcSetNightLightsStateWrapper(nightLightsState, cb));
    }

    /**
     * 隐身模式设置
     * @implNote 链路: DRC | method: drc_stealth_state_set
     */
    public void drcSetStealthState(int stealthState, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcFlightAction(callback, cb -> this.flightControlModule.drcSetStealthStateWrapper(stealthState, cb));
    }

    /**
     * 开伞
     * @implNote 链路: DRC | method: drc_open_parachute
     */
    public void drcOpenParachute(int force, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcFlightAction(callback, cb -> this.flightControlModule.drcOpenParachuteWrapper(force, cb));
    }

    /**
     * 爆闪灯设置
     * @param params 爆闪灯设置参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_flash_led_set
     */
    public void drcSetFlashLed(com.dji.flighthubsdk.bean.FlashLedParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.FlashLedParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeFlashLedParams(params);
            final com.dji.flighthubsdk.generated.FlashLedParams np = nativeParams;
            invokeDrcFlightAction(callback, cb -> this.flightControlModule.drcSetFlashLedWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("FlightControlModule", "drcSetFlashLed error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 相机 LookAt
     * @param params 相机 LookAt 参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_look_at
     */
    public void drcCameraLookAt(com.dji.flighthubsdk.bean.CameraLookAtParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraLookAtParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraLookAtParams(params);
            final com.dji.flighthubsdk.generated.CameraLookAtParams np = nativeParams;
            invokeDrcFlightAction(callback, cb -> this.flightControlModule.drcCameraLookAtWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("FlightControlModule", "drcCameraLookAt error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    // ── Observe: OSD ──

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> osdInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 订阅OSD信息推送
     * @implNote 链路: DRC Northbound | method: osd_info_push
     */
    public int listenDrcOsdInfo(FlightHubCallback.DrcOsdInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DrcOsdInfoCallback swigCb =
                new com.dji.flighthubsdk.generated.DrcOsdInfoCallback() {
                    @Override
                    public void onDrcOsdInfo(com.dji.flighthubsdk.generated.DrcOsdInfo info) {
                        if (callback != null) callback.onResult(info);
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (osdInfoCallbackRef) {
                osdInfoCallbackRef.put(-1, container);
                int listenId = this.flightControlModule.listenDrcOsdInfoWrapper(swigCb);
                osdInfoCallbackRef.remove(-1);
                if (listenId >= 0) {
                    osdInfoCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "listenDrcOsdInfo error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** 取消订阅OSD信息推送 */
    public void cancelListenDrcOsdInfo(int listenId) {
        try {
            this.flightControlModule.cancelListenDrcOsdInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlightControlModule", "cancelListenDrcOsdInfo error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = osdInfoCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }

    // ── Observe: Batteries ──

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> batteriesInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 订阅电池信息推送
     * @implNote 链路: DRC Northbound | method: drc_batteries_info_push
     */
    public int listenDrcBatteriesInfo(FlightHubCallback.DrcBatteriesInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DrcBatteriesInfoCallback swigCb =
                new com.dji.flighthubsdk.generated.DrcBatteriesInfoCallback() {
                    @Override
                    public void onDrcBatteriesInfo(com.dji.flighthubsdk.generated.DrcBatteriesInfo info) {
                        if (callback != null) callback.onResult(info);
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (batteriesInfoCallbackRef) {
                batteriesInfoCallbackRef.put(-1, container);
                int listenId = this.flightControlModule.listenDrcBatteriesInfoWrapper(swigCb);
                batteriesInfoCallbackRef.remove(-1);
                if (listenId >= 0) {
                    batteriesInfoCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "listenDrcBatteriesInfo error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** 取消订阅电池信息推送 */
    public void cancelListenDrcBatteriesInfo(int listenId) {
        try {
            this.flightControlModule.cancelListenDrcBatteriesInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlightControlModule", "cancelListenDrcBatteriesInfo error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = batteriesInfoCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }

    // ── Observe: Parachute ──

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> parachuteInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 订阅降落伞信息推送
     * @implNote 链路: DRC Northbound | method: drc_parachute_info_push
     */
    public int listenDrcParachuteInfo(FlightHubCallback.DrcParachuteInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DrcParachuteInfoCallback swigCb =
                new com.dji.flighthubsdk.generated.DrcParachuteInfoCallback() {
                    @Override
                    public void onDrcParachuteInfo(com.dji.flighthubsdk.generated.DrcParachuteInfo info) {
                        if (callback != null) callback.onResult(info);
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (parachuteInfoCallbackRef) {
                parachuteInfoCallbackRef.put(-1, container);
                int listenId = this.flightControlModule.listenDrcParachuteInfoWrapper(swigCb);
                parachuteInfoCallbackRef.remove(-1);
                if (listenId >= 0) {
                    parachuteInfoCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("FlightControlModule", "listenDrcParachuteInfo error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** 取消订阅降落伞信息推送 */
    public void cancelListenDrcParachuteInfo(int listenId) {
        try {
            this.flightControlModule.cancelListenDrcParachuteInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlightControlModule", "cancelListenDrcParachuteInfo error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = parachuteInfoCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }

    // ── Observe: FlashLed ──

    /**
     * 订阅爆闪灯状态推送
     * @param callback 状态回调
     * @return 监听 ID，失败返回 -1
     * @implNote 链路: DRC Northbound | method: drc_drone_state_push (field: flash_led)
     */
    public int listenDrcFlashLed(FlightHubCallback.FlashLedStateInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.FlashLedStateInfoCallback swigCb =
                new com.dji.flighthubsdk.generated.FlashLedStateInfoCallback() {
                    @Override
                    public void onFlashLedStateInfo(com.dji.flighthubsdk.generated.FlashLedStateInfo info) {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlashLedStateInfo(info));
                        }
                    }
                };

            int listenId = this.flightControlModule.listenDrcFlashLedWrapper(swigCb);
            if (listenId >= 0) {
                swigCb.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("FlightControlModule", "listenDrcFlashLed error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** 取消订阅爆闪灯状态推送 */
    public void cancelListenDrcFlashLed(int listenId) {
        try {
            this.flightControlModule.cancelListenDrcFlashLedWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlightControlModule", "cancelListenDrcFlashLed error: " + e.getMessage(), e);
        }
    }
}
