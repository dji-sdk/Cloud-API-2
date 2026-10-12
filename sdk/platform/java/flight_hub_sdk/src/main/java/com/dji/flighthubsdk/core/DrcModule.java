// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IDrcModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.value.DroneLinkState;

/**
 * DRC 模块接口，提供 DRC 链路连接/断开及链路状态订阅。
 */
public class DrcModule {

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

    private final IDrcModule drcModule;
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> linkStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> connectDrcLinkCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> disconnectDrcLinkCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public DrcModule(IDrcModule drcModule) {
        this.drcModule = drcModule;
    }

    /**
     * 订阅 DRC 链路状态
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC 链路管理 | 非单条 ME method（见方案 8_ §5.5 例外）
     */
    public int listenLinkStatus(FlightHubCallback.LinkStatusCallback callback) {
        try {
            com.dji.flighthubsdk.generated.LinkStatusCallback swigCallback = new com.dji.flighthubsdk.generated.LinkStatusCallback() {
                @Override
                public void onLinkStatus(com.dji.flighthubsdk.generated.LinkStatus link_status) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toLinkStatus(link_status));
                        }
                    } catch (Exception e) {
                        Logger.error("DrcModule", "listenLinkStatus callback error: " + e.getMessage(), e);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (linkStatusCallbackRef) {
                linkStatusCallbackRef.put(-1, container);
                int listenId = this.drcModule.listenLinkStatusWrapper(swigCallback);
                linkStatusCallbackRef.remove(-1);
                if (listenId >= 0) {
                    linkStatusCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("DrcModule", "listenLinkStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.LinkStatus(
                        DroneLinkState.UNKNOWN, DroneLinkState.UNKNOWN, DroneLinkState.UNKNOWN));
            }
        }
        return -1;
    }

    /**
     * 取消订阅 DRC 链路状态
     * @param listenId 监听 ID
     */
    public void cancelListenLinkStatus(int listenId) {
        try {
            this.drcModule.cancelListenLinkStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DrcModule", "cancelListenLinkStatus error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = linkStatusCallbackRef.remove(listenId);
        if (removed != null) {
            removed.release();
        }
    }

    /**
     * 连接 DRC 链路
     * @implNote 链路: DRC 链路管理 | 非单条 ME method（见方案 8_ §5.5 例外）
     */
    public void connectDrcLink(FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallback swigCallback = new com.dji.flighthubsdk.generated.ResultCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("DrcModule", "connectDrcLink callback error: " + e.getMessage(), e);
                    } finally {
                        connectDrcLinkCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (connectDrcLinkCallbackRef) {
                connectDrcLinkCallbackRef.put(id, container);
                this.drcModule.connectDrcLinkWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DrcModule", "connectDrcLink error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 断开 DRC 链路
     * @implNote 链路: DRC 链路管理 | 非单条 ME method（见方案 8_ §5.5 例外）
     */
    public void disconnectDrcLink(FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallback swigCallback = new com.dji.flighthubsdk.generated.ResultCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("DrcModule", "disconnectDrcLink callback error: " + e.getMessage(), e);
                    } finally {
                        disconnectDrcLinkCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (disconnectDrcLinkCallbackRef) {
                disconnectDrcLinkCallbackRef.put(id, container);
                this.drcModule.disconnectDrcLinkWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DrcModule", "disconnectDrcLink error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ==================== 新增 DRC 推送数据方法 ====================

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> rtcQualityTestCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> delayInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> countDownInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * RTC质量测试
     * @implNote 链路: DRC | method: drc_rtc_quality_test
     */
    public void drcRtcQualityTest(FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.RtcQualityTestParams params = null;
        try {
            // RtcQualityTestParams is built with defaults (empty test)
            params = new com.dji.flighthubsdk.generated.RtcQualityTestParams();
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCb =
                new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                        try {
                            if (callback != null) {
                                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                    err.getError_code(), (int) err.getHttp_code(),
                                    (int) err.getServer_code(), (int) err.getDevice_code(),
                                    err.getServer_message()));
                            }
                        } catch (Exception e) {
                            Logger.error("DrcModule", "drcRtcQualityTest callback error: " + e.getMessage(), e);
                        } finally {
                            rtcQualityTestCallbackRef.remove(id);
                        }
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (rtcQualityTestCallbackRef) {
                rtcQualityTestCallbackRef.put(id, container);
                this.drcModule.drcRtcQualityTestWrapper(params, swigCb);
            }
        } catch (Exception e) {
            Logger.error("DrcModule", "drcRtcQualityTest error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (params != null) {
                params.delete();
            }
        }
    }

    // ── Observe: Delay Info ──

    /**
     * 订阅DRC延时信息推送
     * @implNote 链路: DRC Northbound | method: delay_info_push
     */
    public int listenDrcDelayInfo(FlightHubCallback.DrcDelayInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DrcDelayInfoCallback swigCb =
                new com.dji.flighthubsdk.generated.DrcDelayInfoCallback() {
                    @Override
                    public void onDrcDelayInfo(com.dji.flighthubsdk.generated.DrcDelayInfo info) {
                        if (callback != null) callback.onResult(info);
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (delayInfoCallbackRef) {
                delayInfoCallbackRef.put(-1, container);
                int listenId = this.drcModule.listenDrcDelayInfoWrapper(swigCb);
                delayInfoCallbackRef.remove(-1);
                if (listenId >= 0) {
                    delayInfoCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("DrcModule", "listenDrcDelayInfo error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** 取消订阅DRC延时信息推送 */
    public void cancelListenDrcDelayInfo(int listenId) {
        try {
            this.drcModule.cancelListenDrcDelayInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DrcModule", "cancelListenDrcDelayInfo error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = delayInfoCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }

    // ── Observe: CountDown Info ──

    /**
     * 订阅倒计时信息推送
     * @implNote 链路: DRC Northbound | method: drc_count_down_info_push
     */
    public int listenDrcCountDownInfo(FlightHubCallback.DrcCountDownInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DrcCountDownInfoCallback swigCb =
                new com.dji.flighthubsdk.generated.DrcCountDownInfoCallback() {
                    @Override
                    public void onDrcCountDownInfo(com.dji.flighthubsdk.generated.DrcCountDownInfo info) {
                        if (callback != null) callback.onResult(info);
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (countDownInfoCallbackRef) {
                countDownInfoCallbackRef.put(-1, container);
                int listenId = this.drcModule.listenDrcCountDownInfoWrapper(swigCb);
                countDownInfoCallbackRef.remove(-1);
                if (listenId >= 0) {
                    countDownInfoCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("DrcModule", "listenDrcCountDownInfo error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** 取消订阅倒计时信息推送 */
    public void cancelListenDrcCountDownInfo(int listenId) {
        try {
            this.drcModule.cancelListenDrcCountDownInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DrcModule", "cancelListenDrcCountDownInfo error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = countDownInfoCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }
}
