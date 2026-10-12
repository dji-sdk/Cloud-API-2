// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IDockControlModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.value.DockCameraPosition;
import com.dji.flighthubsdk.value.DockCoverMode;

/**
 * 机场控制模块接口，提供舱内/舱外摄像头切换、空调/补光灯/充电/舱盖控制及状态订阅。
 */
public class DockControlModule {

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

    private final IDockControlModule dockControlModule;
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> dockSetAirConditionerModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> dockSetSupplementLightStateCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> dockSetChargeStateCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> dockSetCoverStateCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> dockChangeCameraPositionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int actionCallbackIdCounter = 0;

    public DockControlModule(IDockControlModule dockControlModule) {
        this.dockControlModule = dockControlModule;
    }

    private synchronized int generateActionCallbackId() {
        return actionCallbackIdCounter++;
    }

    /**
     * 切换机场舱内/舱外摄像头
     * @param liveInfo 直播信息
     * @param cameraPosition 摄像头位置（枚举 DockCameraPosition，见 value.h）
     * @param gatewaySn 网关设备序列号
     * @param callback 结果回调
     */
    public void changeDockCameraPosition(LiveInfo liveInfo, DockCameraPosition cameraPosition, String gatewaySn, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.LiveInfo nativeLiveInfo = null;
        try {
            nativeLiveInfo = FlightHubSdkConvert.toNativeLiveInfo(liveInfo);
            if (nativeLiveInfo == null) {
                Logger.error("DockControlModule", "changeDockCameraPosition: liveInfo is null");
                if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
                return;
            }
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        dockChangeCameraPositionCallbackRef.remove(id);
                    }
                }
            };
            synchronized (dockChangeCameraPositionCallbackRef) {
                dockChangeCameraPositionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.dockControlModule.changeDockCameraPositionWrapper(nativeLiveInfo, cameraPosition.getValue(), gatewaySn != null ? gatewaySn : "", swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DockControlModule", "changeDockCameraPosition error: " + e.getMessage(), e);
            if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
        } finally {
            if (nativeLiveInfo != null) {
                nativeLiveInfo.delete();
            }
        }
    }

    /**
     * 订阅机场空调工作模式
     * @param callback 空调状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_air_conditioner_status_info
     */
    public int listenDockAirConditionerStatusInfo(FlightHubCallback.DockAirConditionerStatusInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockAirConditionerStatusInfoCallback swigCallback = new com.dji.flighthubsdk.generated.DockAirConditionerStatusInfoCallback() {
                @Override
                public void onDockAirConditionerStatusInfo(com.dji.flighthubsdk.generated.DockAirConditionerStatusInfo status_info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockAirConditionerStatusInfo(status_info));
                    }
                }
            };
            int listenId = this.dockControlModule.listenDockAirConditionerStatusInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockControlModule", "listenDockAirConditionerStatusInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockAirConditionerStatusInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅机场空调工作模式
     */
    public void cancelListenDockAirConditionerStatusInfo(int listenId) {
        try {
            this.dockControlModule.cancelListenDockAirConditionerStatusInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockControlModule", "cancelListenDockAirConditionerStatusInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅机场补光灯状态
     * @param callback 补光灯状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_supplement_light_state
     */
    public int listenDockSupplementLightState(FlightHubCallback.DockSupplementLightStateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockSupplementLightStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockSupplementLightStateCallback() {
                @Override
                public void onDockSupplementLightState(com.dji.flighthubsdk.generated.DockSupplementLightState light_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockSupplementLightState(light_state));
                    }
                }
            };
            int listenId = this.dockControlModule.listenDockSupplementLightStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockControlModule", "listenDockSupplementLightState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockSupplementLightState(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅机场补光灯状态
     */
    public void cancelListenDockSupplementLightState(int listenId) {
        try {
            this.dockControlModule.cancelListenDockSupplementLightStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockControlModule", "cancelListenDockSupplementLightState error: " + e.getMessage(), e);
        }
    }

    /**
     * 设置机场补光灯开关
     * @param state true=开启 false=关闭
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /device/api/v1/service/supplement-light-{open|close}
     */
    public void setDockSupplementLightState(boolean state, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        dockSetSupplementLightStateCallbackRef.remove(id);
                    }
                }
            };
            synchronized (dockSetSupplementLightStateCallbackRef) {
                dockSetSupplementLightStateCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.dockControlModule.setDockSupplementLightStateWrapper(state, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DockControlModule", "setDockSupplementLightState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
            }
        }
    }

    /**
     * 设置机场充电开关
     * @param state true=开启充电 false=关闭充电
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /device/api/v1/service/charge-{open|close}
     */
    public void setDockChargeState(boolean state, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        dockSetChargeStateCallbackRef.remove(id);
                    }
                }
            };
            synchronized (dockSetChargeStateCallbackRef) {
                dockSetChargeStateCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.dockControlModule.setDockChargeStateWrapper(state, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DockControlModule", "setDockChargeState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
            }
        }
    }

    /**
     * 设置机场舱盖开关
     * @param mode 舱盖操作模式
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /device/api/v1/service/cover-{open|close}
     */
    public void setDockCoverState(DockCoverMode mode, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        dockSetCoverStateCallbackRef.remove(id);
                    }
                }
            };
            synchronized (dockSetCoverStateCallbackRef) {
                dockSetCoverStateCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                int nativeMode = mode != null ? mode.getValue() : DockCoverMode.UNKNOWN.getValue();
                this.dockControlModule.setDockCoverStateWrapper(nativeMode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DockControlModule", "setDockCoverState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
            }
        }
    }

    /**
     * 设置机场空调模式
     * @param mode 空调模式。枚举 DockAirConditionerState（value.h）：0=空闲, 1=制冷, 2=制热, 3=除湿
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /device/api/v1/service/air-conditioner
     */
    public void setDockAirConditionerMode(int mode, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        dockSetAirConditionerModeCallbackRef.remove(id);
                    }
                }
            };
            synchronized (dockSetAirConditionerModeCallbackRef) {
                dockSetAirConditionerModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.dockControlModule.setDockAirConditionerModeWrapper(mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DockControlModule", "setDockAirConditionerMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
            }
        }
    }
}
