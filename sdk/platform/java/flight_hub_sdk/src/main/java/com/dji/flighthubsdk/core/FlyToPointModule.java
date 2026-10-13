// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 指点飞行模块接口
 */
public class FlyToPointModule {

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
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateTargetPointsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IFlyToPointModule flyToPointModule;

    /**
     * 构造指点飞行模块
     * @param flyToPointModule 底层指点飞行模块接口
     */
    public FlyToPointModule(IFlyToPointModule flyToPointModule) {
        this.flyToPointModule = flyToPointModule;
    }

    /**
     * 开始指点飞行
     * @param params 目标点坐标与最大速度
     * @param callback 结果回调，成功时返回 fly_to_id
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks/fly-to-points
     */
    public void start(com.dji.flighthubsdk.bean.FlyToPointParams params, FlightHubCallback.FlyToPointStartCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.FlyToPointStartCallback swigCallback = new com.dji.flighthubsdk.generated.FlyToPointStartCallback() {
                @Override
                public void onFlyToPointStart(com.dji.flighthubsdk.generated.FlightHubError err, String fly_to_id) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), fly_to_id);
                        }
                    } catch (Exception e) {
                        Logger.error("FlyToPointModule", "Start callback error: " + e.getMessage(), e);
                    } finally {
                        startCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (startCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                startCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                com.dji.flighthubsdk.generated.FlyToPointParams nativeFlyToPointParams = null;

                try {

                    nativeFlyToPointParams = FlightHubSdkConvert.toNativeFlyToPointParams(params);

                    this.flyToPointModule.startWrapper(nativeFlyToPointParams, swigCallback);

                } finally {

                    if (nativeFlyToPointParams != null) {

                        nativeFlyToPointParams.delete();

                    }

                }

            }
            
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "start error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), "");
            }
        }

    }

    /**
     * 停止指点飞行（紧急停止）
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drone_emergency_stop
     */
    public void drcStop(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error("FlyToPointModule", "Stop callback error: " + e.getMessage(), e);
                    } finally {
                        stopCallbackRef.remove(id);
                    }
                }
            };

            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);

            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (stopCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                stopCallbackRef.put(id, container);

                // 调用SWIG生成的JNI方法
                this.flyToPointModule.drcStopWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "drcStop error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }
    
    /**
     * 订阅飞行器自主规划路径。
     *
     * @deprecated 已废弃。消息 topic {@code topic_drone_self_plan_path} 已替换为
     *             {@code topic_atc_flyto_task_event}。请改用 {@link #listenFlyToTaskEvent}
     *             监听指点飞行任务事件（路径见 {@link com.dji.flighthubsdk.bean.FlyToTaskData#getPlannedPathPoints()}）；
     *             取消订阅使用 {@link #cancelListenFlyToTaskEvent}。
     * @param callback 路径回调，每次路径更新时触发
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_self_plan_path
     */
    @Deprecated
    public int subscribeFlyToDroneSelfPlanPath(FlightHubCallback.FlyToDroneSelfPlanPathCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.FlyToDroneSelfPlanPathCallback swigCallback = new com.dji.flighthubsdk.generated.FlyToDroneSelfPlanPathCallback() {
                @Override
                public void onFlyToDroneSelfPlanPath(FlyToDroneSelfPlanPath fly_to_drone_self_plan_path) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlyToDroneSelfPlanPath(fly_to_drone_self_plan_path));
                        }
                    } catch (Exception e) {
                        Logger.error("FlyToPointModule", "subscribeFlyToDroneSelfPlanPath callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.flyToPointModule.subscribeFlyToDroneSelfPlanPathWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "subscribeFlyToDroneSelfPlanPath error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(null);
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞行器自主规划路径。
     *
     * @deprecated 已废弃。请改用 {@link #cancelListenFlyToTaskEvent}。
     * @param listenId {@link #subscribeFlyToDroneSelfPlanPath} 返回的监听 ID
     * @implNote 链路: SDK 内部 | 取消 NotificationCenter 监听
     */
    @Deprecated
    public void unSubscribeFlyToDroneSelfPlanPath(int listenId) {
        try {
            this.flyToPointModule.unsubscribeFlyToDroneSelfPlanPathWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "unSubscribeFlyToDroneSelfPlanPath error: " + e.getMessage(), e);
        }
    }
    
    /**
     * 订阅指点飞行进度。
     *
     * @deprecated 已废弃。消息 topic {@code topic_drone_flyto_point_progress} 已替换为
     *             {@code topic_atc_flyto_task_event}。请改用 {@link #listenFlyToTaskEvent}
     *             监听指点飞行任务事件（进度见 {@link com.dji.flighthubsdk.bean.FlyToTaskData} 的
     *             {@code status}、{@code wayPointIndex}、{@code remainingDistance}、{@code remainingTime}）；
     *             取消订阅使用 {@link #cancelListenFlyToTaskEvent}。
     * @param callback 进度回调，每次状态变化时触发
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_flyto_point_progress
     */
    @Deprecated
    public int subscribeFlyToPointProgress(FlightHubCallback.FlyToPointProgressCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.FlyToPointProgressCallback swigCallback = new com.dji.flighthubsdk.generated.FlyToPointProgressCallback() {
                @Override
                public void onFlyToPointProgress(FlyToPointProgress fly_to_point_progress) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlyToPointProgress(fly_to_point_progress));
                        }
                    } catch (Exception e) {
                        Logger.error("FlyToPointModule", "subscribeFlyToPointProgress callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.flyToPointModule.subscribeFlyToPointProgressWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "subscribeFlyToPointProgress error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toFlyToPointProgress(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅指点飞行进度。
     *
     * @deprecated 已废弃。请改用 {@link #cancelListenFlyToTaskEvent}。
     * @param listenId {@link #subscribeFlyToPointProgress} 返回的监听 ID
     * @implNote 链路: SDK 内部 | 取消 NotificationCenter 监听
     */
    @Deprecated
    public void unSubscribeFlyToPointProgress(int listenId) {
        try {
            this.flyToPointModule.unsubscribeFlyToPointProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "unSubscribeFlyToPointProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 更新指点飞行目标点
     * @param params 更新参数（fly_to_id、起始点、目标点数组、最大速度）
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /atc/api/v1/flight-tasks/fly-to-points
     */
    public void updateTargetPoints(com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.FlyToUpdateTargetPointsParams nativeFlyToUpdateTargetPointsParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error("FlyToPointModule", "updateTargetPoints callback error: " + e.getMessage(), e);
                    } finally {
                        updateTargetPointsCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeFlyToUpdateTargetPointsParams = FlightHubSdkConvert.toNativeFlyToUpdateTargetPointsParams(params);
            synchronized (updateTargetPointsCallbackRef) {
                updateTargetPointsCallbackRef.put(id, container);
                this.flyToPointModule.updateTargetPointsWrapper(nativeFlyToUpdateTargetPointsParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "updateTargetPoints error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeFlyToUpdateTargetPointsParams != null) {
                nativeFlyToUpdateTargetPointsParams.delete();
            }
        }
    }

    /**
     * 订阅指点飞行任务事件（创建与更新），含路径规划与执行进度。
     *
     * <p>替代已废弃的 {@link #subscribeFlyToPointProgress} 与
     * {@link #subscribeFlyToDroneSelfPlanPath}。事件数据见
     * {@link com.dji.flighthubsdk.bean.FlyToTaskEvent#getTaskData()}：路径为
     * {@code plannedPathPoints}；进度为 {@code status}、{@code wayPointIndex}、
     * {@code remainingDistance}、{@code remainingTime}。</p>
     *
     * @param callback 任务事件回调
     * @return 监听 ID，用于 {@link #cancelListenFlyToTaskEvent(int)} 取消监听
     * @implNote 链路: WebSocket | topic: topic_atc_flyto_task_event
     */
    public int listenFlyToTaskEvent(FlightHubCallback.FlyToTaskEventCallback callback) {
        try {
            com.dji.flighthubsdk.generated.FlyToTaskEventCallback swigCallback =
                new com.dji.flighthubsdk.generated.FlyToTaskEventCallback() {
                    @Override
                    public void onFlyToTaskEvent(com.dji.flighthubsdk.generated.FlyToTaskEvent event) {
                        try {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toFlyToTaskEvent(event));
                            }
                        } catch (Exception e) {
                            Logger.error("FlyToPointModule", "FlyToTaskEvent callback error: " + e.getMessage(), e);
                        }
                    }
                };
            int listenId = this.flyToPointModule.listenFlyToTaskEventWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "listenFlyToTaskEvent error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅指点飞行任务事件。
     *
     * @param listenId {@link #listenFlyToTaskEvent} 返回的监听 ID
     * @implNote 链路: SDK 内部 | 取消 NotificationCenter 监听
     */
    public void cancelListenFlyToTaskEvent(int listenId) {
        try {
            this.flyToPointModule.cancelListenFlyToTaskEventWrapper(listenId);
        } catch (Exception e) {
            Logger.error("FlyToPointModule", "cancelListenFlyToTaskEvent error: " + e.getMessage(), e);
        }
    }
}