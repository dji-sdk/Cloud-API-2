// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.util.Logger;

/**
 * 一键起飞模块接口，提供起飞指令、能力可用性订阅及指令任务事件监听。
 */
public class TakeoffModule {

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> takeoffCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final ITakeoffModule takeoffModule;

    public TakeoffModule(ITakeoffModule takeoffModule) {
        this.takeoffModule = takeoffModule;
    }

    /**
     * 一键起飞
     * @param params 起飞参数
     * @param callback 结果回调
     */
    public void takeoff(com.dji.flighthubsdk.bean.TakeoffParams params, FlightHubCallback.TakeoffCallback callback) {
        com.dji.flighthubsdk.generated.TakeoffParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeTakeoffParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.TakeoffResultCallback swigCallback = new com.dji.flighthubsdk.generated.TakeoffResultCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.TakeoffResult result) {
                    try {
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.TakeoffResult beanResult = new com.dji.flighthubsdk.bean.TakeoffResult(
                                result != null ? result.getFlight_id() : "",
                                result != null ? result.getFly_to_id() : "");
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), beanResult);
                        }
                    } catch (Exception e) {
                        Logger.error("TakeoffModule", "Takeoff callback error: " + e.getMessage(), e);
                    } finally {
                        takeoffCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (takeoffCallbackRef) {
                takeoffCallbackRef.put(id, container);
                this.takeoffModule.takeoffWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("TakeoffModule", "takeoff error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), new com.dji.flighthubsdk.bean.TakeoffResult("", ""));
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 订阅指令任务事件
     * @param callback 指令任务事件回调（CommandTaskEvent::action：command_task_created/command_task_updated；CommandTaskData::status：0-待执行，1-执行中，2-已完成，3-失败，4-已取消）
     * @return 监听 ID，用于取消订阅
     */
    public int listenCommandTaskEvent(FlightHubCallback.CommandTaskEventCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CommandTaskEventCallback swigCallback =
                new com.dji.flighthubsdk.generated.CommandTaskEventCallback() {
                    @Override
                    public void onCommandTaskEvent(com.dji.flighthubsdk.generated.CommandTaskEvent event) {
                        try {
                            if (callback != null) { callback.onResult(FlightHubSdkConvert.toCommandTaskEvent(event)); }
                        } catch (Exception e) { Logger.error("TakeoffModule", "CommandTaskEvent callback error: " + e.getMessage(), e); }
                    }
                };
            int listenId = this.takeoffModule.listenCommandTaskEventWrapper(swigCallback);
            if (listenId >= 0) { swigCallback.swigReleaseOwnership(); }
            return listenId;
        } catch (Exception e) { Logger.error("TakeoffModule", "listenCommandTaskEvent error: " + e.getMessage(), e); }
        return -1;
    }

    /**
     * 取消订阅指令任务事件
     * @param listenId 监听 ID
     */
    public void cancelListenCommandTaskEvent(int listenId) {
        try { this.takeoffModule.cancelListenCommandTaskEventWrapper(listenId); } catch (Exception e) { Logger.error("TakeoffModule", "cancelListenCommandTaskEvent error: " + e.getMessage(), e); }
    }

    // ===================== EA340 消息推送补全（Phase D） =====================

    /**
     * 订阅指点飞行模式设置值(commander_flight_mode)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_commander_flight_mode
     */
    public int listenDroneCommanderFlightMode(FlightHubCallback.DroneCommanderFlightModeCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneCommanderFlightModeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneCommanderFlightModeCallback() {
                        @Override
                        public void onDroneCommanderFlightMode(com.dji.flighthubsdk.generated.DroneCommanderFlightMode msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneCommanderFlightMode(msg));
                            }
                        }
                    };
            int listenId = this.takeoffModule.listenDroneCommanderFlightModeWrapper(swigCallback);
            if (listenId >= 0) { swigCallback.swigReleaseOwnership(); }
            return listenId;
        } catch (Exception e) { Logger.error("TakeoffModule", "listenDroneCommanderFlightMode error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅指点飞行模式设置值 */
    public void cancelListenDroneCommanderFlightMode(int listenId) {
        try { this.takeoffModule.cancelListenDroneCommanderFlightModeWrapper(listenId); } catch (Exception e) { Logger.error("TakeoffModule", "cancelListenDroneCommanderFlightMode error: " + e.getMessage(), e); }
    }

    /**
     * 订阅指点飞行模式当前值(current_commander_flight_mode)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_current_commander_flight_mode
     */
    public int listenDroneCurrentCommanderFlightMode(FlightHubCallback.DroneCurrentCommanderFlightModeCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneCurrentCommanderFlightModeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneCurrentCommanderFlightModeCallback() {
                        @Override
                        public void onDroneCurrentCommanderFlightMode(com.dji.flighthubsdk.generated.DroneCurrentCommanderFlightMode msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneCurrentCommanderFlightMode(msg));
                            }
                        }
                    };
            int listenId = this.takeoffModule.listenDroneCurrentCommanderFlightModeWrapper(swigCallback);
            if (listenId >= 0) { swigCallback.swigReleaseOwnership(); }
            return listenId;
        } catch (Exception e) { Logger.error("TakeoffModule", "listenDroneCurrentCommanderFlightMode error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅指点飞行模式当前值 */
    public void cancelListenDroneCurrentCommanderFlightMode(int listenId) {
        try { this.takeoffModule.cancelListenDroneCurrentCommanderFlightModeWrapper(listenId); } catch (Exception e) { Logger.error("TakeoffModule", "cancelListenDroneCurrentCommanderFlightMode error: " + e.getMessage(), e); }
    }

    /**
     * 订阅指点飞行失控动作(commander_mode_lost_action)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_commander_mode_lost_action
     */
    public int listenDroneCommanderModeLostAction(FlightHubCallback.DroneCommanderModeLostActionCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneCommanderModeLostActionCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneCommanderModeLostActionCallback() {
                        @Override
                        public void onDroneCommanderModeLostAction(com.dji.flighthubsdk.generated.DroneCommanderModeLostAction msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneCommanderModeLostAction(msg));
                            }
                        }
                    };
            int listenId = this.takeoffModule.listenDroneCommanderModeLostActionWrapper(swigCallback);
            if (listenId >= 0) { swigCallback.swigReleaseOwnership(); }
            return listenId;
        } catch (Exception e) { Logger.error("TakeoffModule", "listenDroneCommanderModeLostAction error: " + e.getMessage(), e); }
        return -1;
    }

    /** 取消订阅指点飞行失控动作 */
    public void cancelListenDroneCommanderModeLostAction(int listenId) {
        try { this.takeoffModule.cancelListenDroneCommanderModeLostActionWrapper(listenId); } catch (Exception e) { Logger.error("TakeoffModule", "cancelListenDroneCommanderModeLostAction error: " + e.getMessage(), e); }
    }

}
