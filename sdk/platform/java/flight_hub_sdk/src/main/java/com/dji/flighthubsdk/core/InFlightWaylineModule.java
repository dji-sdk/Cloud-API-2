// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IInFlightWaylineModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.value.InFlightWaylineControlAction;

/**
 * 空中下发航线模块：HTTP 创建/控制/查询 + WS 进度推送。
 */
public class InFlightWaylineModule {

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

    private final IInFlightWaylineModule module;
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> controlCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getActiveCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public InFlightWaylineModule(IInFlightWaylineModule module) {
        this.module = module;
    }

    /**
     * 创建空中航线任务（gateway_sn / drone_sn 由 Module 自动注入）
     * @param params 任务参数
     * @param callback 结果回调
     */
    public void createInFlightWaylineTask(
            com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskParams params,
            FlightHubCallback.CreateInFlightWaylineTaskCallback callback) {
        com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeCreateInFlightWaylineTaskParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskCallback swigCallback =
                    new com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskCallback() {
                        @Override
                        public void onResult(
                                com.dji.flighthubsdk.generated.CreateInFlightWaylineTaskResult result,
                                com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(
                                            FlightHubSdkConvert.toCreateInFlightWaylineTaskResult(result),
                                            FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error("InFlightWaylineModule", "createInFlightWaylineTask callback error: "
                                        + e.getMessage(), e);
                            } finally {
                                createCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (createCallbackRef) {
                createCallbackRef.put(id, container);
                module.createInFlightWaylineTaskWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("InFlightWaylineModule", "createInFlightWaylineTask error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult(),
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 控制空中航线任务（暂停 / 恢复 / 取消）
     * @param action 控制动作
     * @param callback 结果回调
     */
    public void controlInFlightWaylineTask(
            InFlightWaylineControlAction action,
            FlightHubCallback.InFlightWaylineControlCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback =
                    new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                        @Override
                        public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error("InFlightWaylineModule", "controlInFlightWaylineTask callback error: "
                                        + e.getMessage(), e);
                            } finally {
                                controlCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (controlCallbackRef) {
                controlCallbackRef.put(id, container);
                module.controlInFlightWaylineTaskWrapper(
                        FlightHubSdkConvert.toNativeInFlightWaylineControlAction(action), swigCallback);
            }
        } catch (Exception e) {
            Logger.error("InFlightWaylineModule", "controlInFlightWaylineTask error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                        FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 查询当前设备活跃中的空中航线任务
     * @param callback 结果回调
     */
    public void getActiveInFlightWaylineTask(FlightHubCallback.GetActiveInFlightWaylineTaskCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetActiveInFlightWaylineTaskCallback swigCallback =
                    new com.dji.flighthubsdk.generated.GetActiveInFlightWaylineTaskCallback() {
                        @Override
                        public void onResult(
                                com.dji.flighthubsdk.generated.ActiveInFlightWaylineTaskInfo info,
                                com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(
                                            FlightHubSdkConvert.toActiveInFlightWaylineTaskInfo(info),
                                            FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error("InFlightWaylineModule", "getActiveInFlightWaylineTask callback error: "
                                        + e.getMessage(), e);
                            } finally {
                                getActiveCallbackRef.remove(id);
                            }
                        }
                    };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getActiveCallbackRef) {
                getActiveCallbackRef.put(id, container);
                module.getActiveInFlightWaylineTaskWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("InFlightWaylineModule", "getActiveInFlightWaylineTask error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo(),
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅空中航线任务进度推送
     * @return 监听 ID，用于 cancelListenInFlightWaylineProgress
     */
    public int listenInFlightWaylineProgress(FlightHubCallback.InFlightWaylineProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.InFlightWaylineProgressCallback swigCallback =
                    new com.dji.flighthubsdk.generated.InFlightWaylineProgressCallback() {
                        @Override
                        public void onInFlightWaylineProgress(
                                com.dji.flighthubsdk.generated.InFlightWaylineProgress progress) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toInFlightWaylineProgress(progress));
                                }
                            } catch (Exception e) {
                                Logger.error("InFlightWaylineModule", "listenInFlightWaylineProgress callback error: "
                                        + e.getMessage(), e);
                            }
                        }
                    };
            int listenId = module.listenInFlightWaylineProgressWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("InFlightWaylineModule", "listenInFlightWaylineProgress error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅空中航线任务进度
     * @param listenId 监听 ID
     */
    public void cancelListenInFlightWaylineProgress(int listenId) {
        try {
            module.cancelListenInFlightWaylineProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("InFlightWaylineModule", "cancelListenInFlightWaylineProgress error: " + e.getMessage(), e);
        }
    }
}
