// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.INetworkTestModule;
import com.dji.flighthubsdk.generated.NetworkTestProgressResult;
import com.dji.flighthubsdk.generated.SubscribeNetworkTestProgressCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 网络测速模块，提供开始/停止/查询进度与进度订阅。
 */
public class NetworkTestModule {

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

    private final INetworkTestModule networkTestModule;
    private int id_counter = 0;

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startNetworkTestCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopNetworkTestCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getNetworkTestProgressCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();

    public NetworkTestModule(INetworkTestModule networkTestModule) {
        this.networkTestModule = networkTestModule;
    }

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    /**
     * 开始网络测速
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/network/speed-tests/start
     */
    public void startNetworkTest(FlightHubCallback.StartNetworkTestCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.StartNetworkTestCallback swigCallback =
                    new com.dji.flighthubsdk.generated.StartNetworkTestCallback() {
                        @Override
                        public void onStartNetworkTest(com.dji.flighthubsdk.generated.FlightHubError err,
                                                       NetworkTestProgressResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toNetworkTestProgressResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error("NetworkTestModule", "[startNetworkTest] callback error: " + e.getMessage(), e);
                            } finally {
                                startNetworkTestCallbackRef.remove(id);
                            }
                        }
                    };
            synchronized (startNetworkTestCallbackRef) {
                startNetworkTestCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                networkTestModule.startNetworkTestWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("NetworkTestModule", "[startNetworkTest] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toNetworkTestProgressResult(null));
            }
        }
    }

    /**
     * 停止网络测速
     * @implNote 链路: HTTP | PUT /device/api/v1/service/network/speed-tests
     */
    public void stopNetworkTest(FlightHubCallback.StopNetworkTestCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.StopNetworkTestCallback swigCallback =
                    new com.dji.flighthubsdk.generated.StopNetworkTestCallback() {
                        @Override
                        public void onStopNetworkTest(com.dji.flighthubsdk.generated.FlightHubError err) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                                }
                            } catch (Exception e) {
                                Logger.error("NetworkTestModule", "[stopNetworkTest] callback error: " + e.getMessage(), e);
                            } finally {
                                stopNetworkTestCallbackRef.remove(id);
                            }
                        }
                    };
            synchronized (stopNetworkTestCallbackRef) {
                stopNetworkTestCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                networkTestModule.stopNetworkTestWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("NetworkTestModule", "[stopNetworkTest] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 获取网络测速进度
     * @implNote 链路: HTTP | GET /device/api/v1/service/network/speed-tests
     */
    public void getNetworkTestProgress(FlightHubCallback.GetNetworkTestProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetNetworkTestProgressCallback swigCallback =
                    new com.dji.flighthubsdk.generated.GetNetworkTestProgressCallback() {
                        @Override
                        public void onGetNetworkTestProgress(com.dji.flighthubsdk.generated.FlightHubError err,
                                                             NetworkTestProgressResult result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toNetworkTestProgressResult(result));
                                }
                            } catch (Exception e) {
                                Logger.error("NetworkTestModule", "[getNetworkTestProgress] callback error: " + e.getMessage(), e);
                            } finally {
                                getNetworkTestProgressCallbackRef.remove(id);
                            }
                        }
                    };
            synchronized (getNetworkTestProgressCallbackRef) {
                getNetworkTestProgressCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                networkTestModule.getNetworkTestProgressWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("NetworkTestModule", "[getNetworkTestProgress] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toNetworkTestProgressResult(null));
            }
        }
    }

    /**
     * 订阅网络测速进度
     * @implNote 链路: WebSocket | topic: topic_device_network_speed_test_progress
     */
    public int subscribeNetworkTestProgress(FlightHubCallback.SubscribeNetworkTestProgressCallback callback) {
        try {
            SubscribeNetworkTestProgressCallback swigCallback = new SubscribeNetworkTestProgressCallback() {
                @Override
                public void onSubscribeNetworkTestProgress(NetworkTestProgressResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toNetworkTestProgressResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("NetworkTestModule", "[subscribeNetworkTestProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = networkTestModule.subscribeNetworkTestProgressWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("NetworkTestModule", "[subscribeNetworkTestProgress] error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅网络测速进度
     */
    public void unsubscribeNetworkTestProgress(int listenId) {
        try {
            networkTestModule.unsubscribeNetworkTestProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("NetworkTestModule", "unsubscribeNetworkTestProgress error: " + e.getMessage(), e);
        }
    }
}
