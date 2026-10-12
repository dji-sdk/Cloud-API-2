// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IControlAuthModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * 控制权模块接口，提供飞行/负载控制权的抢夺、释放及状态订阅。
 */
public class ControlAuthModule {

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> acquireFlightControlAuthorityCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> releaseFlightControlAuthorityCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> acquirePayloadControlAuthorityCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> releasePayloadControlAuthorityCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IControlAuthModule controlAuthModule;

    public ControlAuthModule(IControlAuthModule controlAuthModule) {
        this.controlAuthModule = controlAuthModule;
    }

    /**
     * 抢夺飞行控制权
     */
    public void acquireFlightControlAuthority(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
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
                        Logger.error("ControlAuthModule", "Flight control authority callback error: " + e.getMessage(), e);
                    } finally {
                        acquireFlightControlAuthorityCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (acquireFlightControlAuthorityCallbackRef) {
                acquireFlightControlAuthorityCallbackRef.put(id, container);
                this.controlAuthModule.acquireFlightControlAuthorityWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "acquireFlightControlAuthority error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 释放飞行控制权
     */
    public void releaseFlightControlAuthority(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
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
                        Logger.error("ControlAuthModule", "Flight control authority callback error: " + e.getMessage(), e);
                    } finally {
                        releaseFlightControlAuthorityCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (releaseFlightControlAuthorityCallbackRef) {
                releaseFlightControlAuthorityCallbackRef.put(id, container);
                this.controlAuthModule.releaseFlightControlAuthorityWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "releaseFlightControlAuthority error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅飞行控制权状态
     * @return 监听 ID，用于取消订阅
     */
    public int listenFlightControlAuthority(FlightHubCallback.FlightControlAuthorityCallback callback) {
        try {
            com.dji.flighthubsdk.generated.FlightControlAuthorityCallback swigCallback = new com.dji.flighthubsdk.generated.FlightControlAuthorityCallback() {
                @Override
                public void onFlightControlAuthority(boolean state) {
                    try {
                        if (callback != null) {
                            callback.onResult(state);
                        }
                    } catch (Exception e) {
                        Logger.error("ControlAuthModule", "Flight control authority callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.controlAuthModule.listenFlightControlAuthorityWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "listenFlightControlAuthority error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(false);
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞行控制权状态
     * @param listenId 监听 ID
     */
    public void cancelListenFlightControlAuthority(int listenId) {
        try {
            this.controlAuthModule.cancelListenFlightControlAuthority(listenId);
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "cancelListenFlightControlAuthority error: " + e.getMessage(), e);
        }
    }

    /**
     * 抢夺负载控制权
     * @param payloadIndex 负载索引
     */
    public void acquirePayloadControlAuthority(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
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
                        Logger.error("ControlAuthModule", "Payload control authority callback error: " + e.getMessage(), e);
                    } finally {
                        acquirePayloadControlAuthorityCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (acquirePayloadControlAuthorityCallbackRef) {
                acquirePayloadControlAuthorityCallbackRef.put(id, container);
                this.controlAuthModule.acquirePayloadControlAuthorityWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "acquirePayloadControlAuthority error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 释放负载控制权
     * @param payloadIndex 负载索引
     */
    public void releasePayloadControlAuthority(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
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
                        Logger.error("ControlAuthModule", "Payload control authority callback error: " + e.getMessage(), e);
                    } finally {
                        releasePayloadControlAuthorityCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (releasePayloadControlAuthorityCallbackRef) {
                releasePayloadControlAuthorityCallbackRef.put(id, container);
                this.controlAuthModule.releasePayloadControlAuthorityWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "releasePayloadControlAuthority error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅负载控制权状态
     * @param payloadIndex 负载索引
     * @return 监听 ID，用于取消订阅
     */
    public int listenPayloadControlAuthority(String payloadIndex, FlightHubCallback.PayloadControlAuthorityCallback callback) {
        try {
            com.dji.flighthubsdk.generated.PayloadControlAuthorityCallback swigCallback = new com.dji.flighthubsdk.generated.PayloadControlAuthorityCallback() {
                @Override
                public void onPayloadControlAuthority(boolean state) {
                    try {
                        if (callback != null) {
                            callback.onResult(state);
                        }
                    } catch (Exception e) {
                        Logger.error("ControlAuthModule", "Payload control authority callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.controlAuthModule.listenPayloadControlAuthorityWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "listenPayloadControlAuthority error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(false);
            }
        }
        return -1;
    }

    /**
     * 取消订阅负载控制权状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenPayloadControlAuthority(String payloadIndex, int listenId) {
        try {
            this.controlAuthModule.cancelListenPayloadControlAuthority(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("ControlAuthModule", "cancelListenPayloadControlAuthority error: " + e.getMessage(), e);
        }
    }
}
