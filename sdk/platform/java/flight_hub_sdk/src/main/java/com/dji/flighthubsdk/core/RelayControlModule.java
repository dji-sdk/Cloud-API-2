// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 中继站控制模块，提供广播、SSID、密码、搜索模式、占用状态、重启与断开等能力。
 */
public class RelayControlModule {

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

    private final IRelayControlModule relayControlModule;
    private int id_counter = 0;

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRelayBroadcastHideCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRelaySSIDCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRelayPasswordCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRelaySearchModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRelayOccupiedStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> relayRebootCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> relayDisconnectCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    public RelayControlModule(IRelayControlModule relayControlModule) {
        this.relayControlModule = relayControlModule;
    }

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    /**
     * 设置中继站广播隐藏状态
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/relay-broadcast-hide
     */
    public void setRelayBroadcastHide(boolean is_broadcast_hide, FlightHubCallback.SetRelayBroadcastHideCallback callback) {
        try {
            int id = generateActionCallbackId();
            SetRelayBroadcastHideCallback swigCallback = new SetRelayBroadcastHideCallback() {
                @Override
                public void onSetRelayBroadcastHide(FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("RelayControlModule", "[setRelayBroadcastHide] callback error: " + e.getMessage(), e);
                    } finally {
                        setRelayBroadcastHideCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setRelayBroadcastHideCallbackRef) {
                setRelayBroadcastHideCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                relayControlModule.setRelayBroadcastHideWrapper(is_broadcast_hide, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("RelayControlModule", "[setRelayBroadcastHide] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置中继站 SSID
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/relay-ssid
     */
    public void setRelaySSID(String device_name, FlightHubCallback.SetRelaySSIDCallback callback) {
        try {
            int id = generateActionCallbackId();
            SetRelaySSIDCallback swigCallback = new SetRelaySSIDCallback() {
                @Override
                public void onSetRelaySSID(FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("RelayControlModule", "[setRelaySSID] callback error: " + e.getMessage(), e);
                    } finally {
                        setRelaySSIDCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setRelaySSIDCallbackRef) {
                setRelaySSIDCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                relayControlModule.setRelaySSIDWrapper(device_name, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("RelayControlModule", "[setRelaySSID] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置中继站密码
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/relay-password
     */
    public void setRelayPassword(String passwd, FlightHubCallback.SetRelayPasswordCallback callback) {
        try {
            int id = generateActionCallbackId();
            SetRelayPasswordCallback swigCallback = new SetRelayPasswordCallback() {
                @Override
                public void onSetRelayPassword(FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("RelayControlModule", "[setRelayPassword] callback error: " + e.getMessage(), e);
                    } finally {
                        setRelayPasswordCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setRelayPasswordCallbackRef) {
                setRelayPasswordCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                relayControlModule.setRelayPasswordWrapper(passwd, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("RelayControlModule", "[setRelayPassword] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置中继站搜索模式
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/relay-search-mode
     */
    public void setRelaySearchMode(int relay_search_mode, FlightHubCallback.SetRelaySearchModeCallback callback) {
        try {
            int id = generateActionCallbackId();
            SetRelaySearchModeCallback swigCallback = new SetRelaySearchModeCallback() {
                @Override
                public void onSetRelaySearchMode(FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("RelayControlModule", "[setRelaySearchMode] callback error: " + e.getMessage(), e);
                    } finally {
                        setRelaySearchModeCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setRelaySearchModeCallbackRef) {
                setRelaySearchModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                relayControlModule.setRelaySearchModeWrapper(relay_search_mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("RelayControlModule", "[setRelaySearchMode] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置中继站占用状态
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/relay-occupied-status
     */
    public void setRelayOccupiedStatus(int occupied_status, FlightHubCallback.SetRelayOccupiedStatusCallback callback) {
        try {
            int id = generateActionCallbackId();
            SetRelayOccupiedStatusCallback swigCallback = new SetRelayOccupiedStatusCallback() {
                @Override
                public void onSetRelayOccupiedStatus(FlightHubError err, SetRelayOccupiedStatusResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSetRelayOccupiedStatusResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("RelayControlModule", "[setRelayOccupiedStatus] callback error: " + e.getMessage(), e);
                    } finally {
                        setRelayOccupiedStatusCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setRelayOccupiedStatusCallbackRef) {
                setRelayOccupiedStatusCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                relayControlModule.setRelayOccupiedStatusWrapper(occupied_status, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("RelayControlModule", "[setRelayOccupiedStatus] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toSetRelayOccupiedStatusResult(null));
            }
        }
    }

    /**
     * 中继站重启
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/relay-reboot
     */
    public void relayReboot(FlightHubCallback.RelayRebootCallback callback) {
        try {
            int id = generateActionCallbackId();
            RelayRebootCallback swigCallback = new RelayRebootCallback() {
                @Override
                public void onRelayReboot(FlightHubError err, RelayRebootResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toRelayRebootResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("RelayControlModule", "[relayReboot] callback error: " + e.getMessage(), e);
                    } finally {
                        relayRebootCallbackRef.remove(id);
                    }
                }
            };
            synchronized (relayRebootCallbackRef) {
                relayRebootCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                relayControlModule.relayRebootWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("RelayControlModule", "[relayReboot] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toRelayRebootResult(null));
            }
        }
    }

    /**
     * 中继站断开连接
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/relay-disconnect
     */
    public void relayDisconnect(FlightHubCallback.RelayDisconnectCallback callback) {
        try {
            int id = generateActionCallbackId();
            RelayDisconnectCallback swigCallback = new RelayDisconnectCallback() {
                @Override
                public void onRelayDisconnect(FlightHubError err, RelayDisconnectResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toRelayDisconnectResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("RelayControlModule", "[relayDisconnect] callback error: " + e.getMessage(), e);
                    } finally {
                        relayDisconnectCallbackRef.remove(id);
                    }
                }
            };
            synchronized (relayDisconnectCallbackRef) {
                relayDisconnectCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                relayControlModule.relayDisconnectWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("RelayControlModule", "[relayDisconnect] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        FlightHubSdkConvert.toRelayDisconnectResult(null));
            }
        }
    }
}
