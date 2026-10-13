// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IFlightSettingsModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 飞行设置模块接口，提供智能规划、失控动作、任务/返航高度及空中回传等参数配置。
 */
public class FlightSettingsModule {

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

    private final IFlightSettingsModule flightSettingsModule;
    private final String deviceSn;

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setFlightSmartPlanModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setOutOfControlActionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setFlightTaskHeightCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRthAltitudeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setAirTransferEnableCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public FlightSettingsModule(IFlightSettingsModule flightSettingsModule, String deviceSn) {
        this.flightSettingsModule = flightSettingsModule;
        this.deviceSn = deviceSn;
    }

    /**
     * 设置智能规划模式
     * @param mode 规划模式
     */
    public void setFlightSmartPlanMode(int mode, FlightHubCallback.SetFlightSmartPlanModeCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetFlightSmartPlanModeCallback swigCallback = new com.dji.flighthubsdk.generated.SetFlightSmartPlanModeCallback() {
                @Override
                public void onSetFlightSmartPlanMode(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.FlightSmartPlanModeResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFlightSmartPlanModeResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("FlightSettingsModule", "setFlightSmartPlanMode callback error: " + e.getMessage(), e);
                    } finally {
                        setFlightSmartPlanModeCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setFlightSmartPlanModeCallbackRef) {
                setFlightSmartPlanModeCallbackRef.put(id, container);
                this.flightSettingsModule.setFlightSmartPlanModeWrapper(deviceSn, mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightSettingsModule", "setFlightSmartPlanMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toFlightSmartPlanModeResult(null));
            }
        }
    }

    /**
     * 设置失控动作
     * @param action 失控动作
     */
    public void setOutOfControlAction(String action, FlightHubCallback.SetOutOfControlActionCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetOutOfControlActionCallback swigCallback = new com.dji.flighthubsdk.generated.SetOutOfControlActionCallback() {
                @Override
                public void onSetOutOfControlAction(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.FlightOutOfControlActionResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFlightOutOfControlActionResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("FlightSettingsModule", "setOutOfControlAction callback error: " + e.getMessage(), e);
                    } finally {
                        setOutOfControlActionCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setOutOfControlActionCallbackRef) {
                setOutOfControlActionCallbackRef.put(id, container);
                this.flightSettingsModule.setOutOfControlActionWrapper(deviceSn, action, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightSettingsModule", "setOutOfControlAction error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toFlightOutOfControlActionResult(null));
            }
        }
    }

    /**
     * 设置飞行任务高度
     * @param height 任务高度
     */
    public void setFlightTaskHeight(int height, FlightHubCallback.SetFlightTaskHeightCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetFlightTaskHeightCallback swigCallback = new com.dji.flighthubsdk.generated.SetFlightTaskHeightCallback() {
                @Override
                public void onSetFlightTaskHeight(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.FlightCommanderHeightResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFlightCommanderHeightResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("FlightSettingsModule", "setFlightTaskHeight callback error: " + e.getMessage(), e);
                    } finally {
                        setFlightTaskHeightCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setFlightTaskHeightCallbackRef) {
                setFlightTaskHeightCallbackRef.put(id, container);
                this.flightSettingsModule.setFlightTaskHeightWrapper(deviceSn, height, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightSettingsModule", "setFlightTaskHeight error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toFlightCommanderHeightResult(null));
            }
        }
    }

    /**
     * 设置返航高度
     * @param altitude 返航高度
     */
    public void setRthAltitude(int altitude, FlightHubCallback.SetRthAltitudeCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetRthAltitudeCallback swigCallback = new com.dji.flighthubsdk.generated.SetRthAltitudeCallback() {
                @Override
                public void onSetRthAltitude(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.FlightRthAltitudeResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFlightRthAltitudeResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("FlightSettingsModule", "setRthAltitude callback error: " + e.getMessage(), e);
                    } finally {
                        setRthAltitudeCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setRthAltitudeCallbackRef) {
                setRthAltitudeCallbackRef.put(id, container);
                this.flightSettingsModule.setRthAltitudeWrapper(deviceSn, altitude, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightSettingsModule", "setRthAltitude error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toFlightRthAltitudeResult(null));
            }
        }
    }

    /**
     * 设置空中回传开关
     * @param enabled 是否开启
     */
    public void setAirTransferEnable(boolean enabled, FlightHubCallback.SetAirTransferEnableCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetAirTransferEnableCallback swigCallback = new com.dji.flighthubsdk.generated.SetAirTransferEnableCallback() {
                @Override
                public void onSetAirTransferEnable(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("FlightSettingsModule", "setAirTransferEnable callback error: " + e.getMessage(), e);
                    } finally {
                        setAirTransferEnableCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setAirTransferEnableCallbackRef) {
                setAirTransferEnableCallbackRef.put(id, container);
                this.flightSettingsModule.setAirTransferEnableWrapper(deviceSn, enabled, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlightSettingsModule", "setAirTransferEnable error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }
}
