// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.util.Logger;

/**
 * POI（兴趣点环绕）模块接口
 */
public class PoiModule {

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
    
    
    // Action Callback
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> enterCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> exitCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateSpeedCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getTaskCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IPoiModule poiModule;

    /**
     * 构造兴趣点环绕模块
     * @param poiModule 底层 POI 模块接口
     */
    public PoiModule(IPoiModule poiModule) {
        this.poiModule = poiModule;
    }

    /**
     * 进入 POI 环绕模式
     * @param params 环绕参数（中心点坐标、环绕速度、环绕半径、负载索引）
     * @param callback 结果回调，成功时返回任务 UUID
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks/poi-enter
     */
    public void enter(com.dji.flighthubsdk.bean.POIEnterParams params, FlightHubCallback.PoiEnterCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PoiEnterCallback swigCallback = new com.dji.flighthubsdk.generated.PoiEnterCallback() {
                @Override
                public void onPoiEnter(com.dji.flighthubsdk.generated.FlightHubError err, String task_uuid) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), task_uuid);
                        }
                    } catch (Exception e) {
                        Logger.error("PoiModule", "Enter callback error: " + e.getMessage(), e);
                    } finally {
                        enterCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (enterCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                enterCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                com.dji.flighthubsdk.generated.POIEnterParams nativePOIEnterParams = null;

                try {

                    nativePOIEnterParams = FlightHubSdkConvert.toNativePOIEnterParams(params);

                    this.poiModule.enterWrapper(nativePOIEnterParams, swigCallback);

                } finally {

                    if (nativePOIEnterParams != null) {

                        nativePOIEnterParams.delete();

                    }

                }

            }
            
        } catch (Exception e) {
            Logger.error("PoiModule", "enter error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), "");
            }
        }
    }

    /**
     * 退出 POI 环绕模式
     * @param callback 结果回调
     * @implNote 链路: HTTP | DELETE /atc/api/v1/flight-tasks/poi-exit
     */
    public void exit(FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PoiExitCallback swigCallback = new com.dji.flighthubsdk.generated.PoiExitCallback() {
                @Override
                public void onPoiExit(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("PoiModule", "Exit callback error: " + e.getMessage(), e);
                    } finally {
                        exitCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (exitCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                exitCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.poiModule.exitWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PoiModule", "exit error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 更新 POI 环绕速度
     * @param speed 目标环绕速度，单位 m/s
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /atc/api/v1/flight-tasks/poi-speed
     */
    public void updateSpeed(double speed, FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PoiUpdateSpeedCallback swigCallback = new com.dji.flighthubsdk.generated.PoiUpdateSpeedCallback() {
                @Override
                public void onPoiUpdateSpeed(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("PoiModule", "UpdateSpeed callback error: " + e.getMessage(), e);
                    } finally {
                        updateSpeedCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (updateSpeedCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                updateSpeedCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.poiModule.updateSpeedWrapper(speed, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PoiModule", "updateSpeed error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 查询当前 POI 任务信息
     * @param callback 结果回调，成功时返回 POITaskInfo
     * @implNote 链路: HTTP | GET /atc/api/v1/flight-tasks/poi-task
     */
    public void getTask(FlightHubCallback.PoiGetTaskCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PoiGetTaskCallback swigCallback = new com.dji.flighthubsdk.generated.PoiGetTaskCallback() {
                @Override
                public void onPoiGetTask(com.dji.flighthubsdk.generated.FlightHubError err, POITaskInfo task) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toPOITaskInfo(task));
                        }
                    } catch (Exception e) {
                        Logger.error("PoiModule", "GetTask callback error: " + e.getMessage(), e);
                    } finally {
                        getTaskCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (getTaskCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                getTaskCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.poiModule.getTaskWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PoiModule", "getTask error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
            }
        }
    }

    /**
     * 订阅 POI 任务事件
     */
    public int listenPoiTaskEvent(FlightHubCallback.PoiTaskEventCallback callback) {
        try {
            com.dji.flighthubsdk.generated.PoiTaskEventCallback swigCallback =
                new com.dji.flighthubsdk.generated.PoiTaskEventCallback() {
                    @Override
                    public void onPoiTaskEvent(com.dji.flighthubsdk.generated.PoiTaskEvent event) {
                        try {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toPoiTaskEvent(event));
                            }
                        } catch (Exception e) {
                            Logger.error("PoiModule", "PoiTaskEvent callback error: " + e.getMessage(), e);
                        }
                    }
                };
            int listenId = this.poiModule.listenPoiTaskEventWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PoiModule", "listenPoiTaskEvent error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 POI 任务事件
     * @param listenId 监听 ID
     */
    public void cancelListenPoiTaskEvent(int listenId) {
        try {
            this.poiModule.cancelListenPoiTaskEventWrapper(listenId);
        } catch (Exception e) {
            Logger.error("PoiModule", "cancelListenPoiTaskEvent error: " + e.getMessage(), e);
        }
    }

    // ==================== POI 飞行进度监听 ====================

    /**
     * 订阅 POI 飞行进度
     * @param callback POI 飞行进度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_task_flight_poi_progress
     */
    public int listenFlightPoiProgress(FlightHubCallback.FlightPoiProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.FlightPoiProgressCallback swigCallback =
                new com.dji.flighthubsdk.generated.FlightPoiProgressCallback() {
                    @Override
                    public void onFlightPoiProgress(com.dji.flighthubsdk.generated.FlightPoiProgress msg) {
                        if (callback != null) {
                            callback.onResult(msg);
                        }
                    }
                };
            int listenId = this.poiModule.listenFlightPoiProgressWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PoiModule", "listenFlightPoiProgress error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 POI 飞行进度
     * @param listenId 监听 ID
     */
    public void cancelListenFlightPoiProgress(int listenId) {
        try {
            this.poiModule.cancelListenFlightPoiProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("PoiModule", "cancelListenFlightPoiProgress error: " + e.getMessage(), e);
        }
    }

}
