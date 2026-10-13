// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.util.Logger;

/**
 * 航线任务控制模块接口，提供航线任务的暂停/恢复、控制权状态订阅等功能。
 */
public class FlightTaskControlModule {
    
    private IFlightTaskControlModule module;
    
    // 回调引用容器,防止回调被GC
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
    
    // 回调引用存储
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> pauseCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> resumeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    
    private int id_counter = 0;
    private synchronized int generateCallbackId() {
        return id_counter++;
    }

    /**
     * 构造飞行任务控制模块
     * @param module 底层飞行任务控制模块接口
     */
    public FlightTaskControlModule(IFlightTaskControlModule module) {
        this.module = module;
    }

    /**
     * 暂停当前航线任务
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd（method: flight_task_pause）
     */
    public void pause(FlightHubCallback.ResultCallbackWithError callback) {
        final int callbackId = generateCallbackId();
        try {
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
                        Logger.error("FlightTaskControlModule", "Pause callback error: " + e.getMessage(), e);
                    } finally {
                        pauseCallbackRef.remove(callbackId);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (pauseCallbackRef) {
                pauseCallbackRef.put(callbackId, container);
                this.module.pauseWrapper(swigCallback);
            }
        } catch (Exception e) {
            pauseCallbackRef.remove(callbackId);
            Logger.error("FlightTaskControlModule", "pause error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 恢复当前航线任务
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd（method: flight_task_resume）
     */
    public void resume(FlightHubCallback.ResultCallbackWithError callback) {
        final int callbackId = generateCallbackId();
        try {
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
                        Logger.error("FlightTaskControlModule", "Resume callback error: " + e.getMessage(), e);
                    } finally {
                        resumeCallbackRef.remove(callbackId);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (resumeCallbackRef) {
                resumeCallbackRef.put(callbackId, container);
                this.module.resumeWrapper(swigCallback);
            }
        } catch (Exception e) {
            resumeCallbackRef.remove(callbackId);
            Logger.error("FlightTaskControlModule", "resume error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅航线任务事件
     * @return 监听 ID，用于取消订阅
     */
    public int listenWaylineTaskEvent(FlightHubCallback.WaylineTaskEventCallback callback) {
        try {
            com.dji.flighthubsdk.generated.WaylineTaskEventCallback swigCallback =
                new com.dji.flighthubsdk.generated.WaylineTaskEventCallback() {
                    @Override
                    public void onWaylineTaskEvent(com.dji.flighthubsdk.generated.WaylineTaskEvent event) {
                        try {
                            if (callback != null) {
                                callback.onResult(com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert.toWaylineTaskEvent(event));
                            }
                        } catch (Exception e) {
                            Logger.error("FlightTaskControlModule", "WaylineTaskEvent callback error: " + e.getMessage(), e);
                        }
                    }
                };
            int listenId = this.module.listenWaylineTaskEventWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("FlightTaskControlModule", "listenWaylineTaskEvent error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅航线任务事件
     * @param listenId 监听 ID
     */
    public void cancelListenWaylineTaskEvent(int listenId) {
        try {
            this.module.cancelListenWaylineTaskEventWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlightTaskControlModule", "cancelListenWaylineTaskEvent error: " + e.getMessage(), e);
        }
    }

    /**
     * 获取底层SWIG模块实例
     * @return SWIG模块实例
     */
    public IFlightTaskControlModule getModule() {
        return this.module;
    }
}
