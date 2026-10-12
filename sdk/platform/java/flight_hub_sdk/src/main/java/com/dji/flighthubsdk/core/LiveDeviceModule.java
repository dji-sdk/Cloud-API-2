// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.ILiveDeviceModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 设备级直播发现模块：订阅可用直播源列表（live_capacity）。
 */
public class LiveDeviceModule {

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

    private final ILiveDeviceModule liveDeviceModule;
    private final String logTag;
    private final java.util.concurrent.atomic.AtomicInteger actionCallbackIdCounter = new java.util.concurrent.atomic.AtomicInteger(0);
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getAudienceCountCallbackRef =
            new java.util.concurrent.ConcurrentHashMap<>();

    public LiveDeviceModule(ILiveDeviceModule liveDeviceModule, String logTag) {
        this.liveDeviceModule = liveDeviceModule;
        this.logTag = logTag != null ? logTag : "LiveDeviceModule";
    }

    /**
     * 监听直播能力（live_capacity）变化，回调可用直播源列表
     * @param callback 回调；解析后的 LiveInfo 列表相对上次变化时触发；注册时若有缓存则立即回调一次
     * @return 监听 ID
     * SDK 每 5 秒轮询 device topo/properties 读取 live_capacity（直播能力上报），解析为 LiveInfo 列表；
     * 非 WebSocket 实时推送，不监听已开播流状态。结果用于 getLiveStream/getLiveRecord。
     * AvailableRange 指由 live_capacity 解析的可用直播源（LiveInfo）列表，非地理范围。不依赖 DRC。
     * @implNote 链路: SDK 内部 | properties / live_capacity（经 get_device_topo）
     */
    public int listenLiveAvailableRange(FlightHubCallback.LiveAvailableRangeCallback callback) {
        try {
            com.dji.flighthubsdk.generated.LiveAvailableRangeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.LiveAvailableRangeCallback() {
                        @Override
                        public void onLiveAvailableRange(com.dji.flighthubsdk.generated.LiveInfoVector infos) {
                            try {
                                java.util.List<LiveInfo> liveInfos = FlightHubSdkConvert.toLiveInfoList(infos);
                                if (callback != null) {
                                    callback.onResult(liveInfos);
                                }
                            } catch (Exception e) {
                                Logger.error(logTag, "Live available range callback error: " + e.getMessage(), e);
                                if (callback != null) {
                                    callback.onResult(java.util.Collections.emptyList());
                                }
                            }
                        }
                    };

            int listenId = liveDeviceModule.listenLiveAvailableRangeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(logTag, "listenLiveAvailableRange error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(java.util.Collections.emptyList());
            }
        }
        return -1;
    }

    /**
     * 取消订阅可用直播流列表
     */
    public void cancelListenLiveAvailableRange(int listenId) {
        try {
            liveDeviceModule.cancelListenLiveAvailableRangeWrapper(listenId);
        } catch (Exception e) {
            Logger.error(logTag, "cancelListenLiveAvailableRange error: " + e.getMessage(), e);
        }
    }

    /**
     * 查询本设备在线观众数量，group 可选（传 nullptr 表示不按分组筛选）
     * @implNote 链路: HTTP | POST /pf-live/api/v1/stream/audience-count
     */
    public void getAudienceCount(com.dji.flighthubsdk.bean.AudienceCountGroup group, FlightHubCallback.GetAudienceCountCallback callback) {
        com.dji.flighthubsdk.generated.AudienceCountGroup nativeGroup = null;
        try {
            nativeGroup = FlightHubSdkConvert.toNativeAudienceCountGroup(group);
            int id = actionCallbackIdCounter.incrementAndGet();
            com.dji.flighthubsdk.generated.GetAudienceCountCallback swigCallback =
                    new com.dji.flighthubsdk.generated.GetAudienceCountCallback() {
                        @Override
                        public void onGetAudienceCount(com.dji.flighthubsdk.generated.FlightHubError err,
                                com.dji.flighthubsdk.generated.DeviceAudienceCountItemVector result) {
                            try {
                                if (callback != null) {
                                    callback.onResult(
                                            FlightHubSdkConvert.toFlightHubError(err),
                                            FlightHubSdkConvert.toDeviceAudienceCountItemList(result));
                                }
                            } catch (Exception e) {
                                Logger.error(logTag, "getAudienceCount callback error: " + e.getMessage(), e);
                            } finally {
                                getAudienceCountCallbackRef.remove(id);
                            }
                        }
                    };
            synchronized (getAudienceCountCallbackRef) {
                getAudienceCountCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                liveDeviceModule.getAudienceCountWrapper(nativeGroup, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(logTag, "getAudienceCount error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                        new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                        java.util.Collections.emptyList());
            }
        } finally {
            if (nativeGroup != null) {
                nativeGroup.delete();
            }
        }
    }

    // ===================== EA340 消息推送补全（Phase D） =====================

    /**
     * 订阅机场直播状态(live_status)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_live_status
     */
    public int listenDockLiveStatus(FlightHubCallback.DockLiveStatusCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockLiveStatusCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DockLiveStatusCallback() {
                        @Override
                        public void onDockLiveStatus(com.dji.flighthubsdk.generated.DockLiveStatus msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDockLiveStatus(msg));
                            }
                        }
                    };
            int listenId = liveDeviceModule.listenDockLiveStatusWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(logTag, "listenDockLiveStatus error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅机场直播状态 */
    public void cancelListenDockLiveStatus(int listenId) {
        try {
            liveDeviceModule.cancelListenDockLiveStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error(logTag, "cancelListenDockLiveStatus error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅机场直播能力(live_capacity)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_live_capacity
     */
    public int listenDockLiveCapacity(FlightHubCallback.DockLiveCapacityCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockLiveCapacityCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DockLiveCapacityCallback() {
                        @Override
                        public void onDockLiveCapacity(com.dji.flighthubsdk.generated.DockLiveCapacity msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDockLiveCapacity(msg));
                            }
                        }
                    };
            int listenId = liveDeviceModule.listenDockLiveCapacityWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(logTag, "listenDockLiveCapacity error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅机场直播能力 */
    public void cancelListenDockLiveCapacity(int listenId) {
        try {
            liveDeviceModule.cancelListenDockLiveCapacityWrapper(listenId);
        } catch (Exception e) {
            Logger.error(logTag, "cancelListenDockLiveCapacity error: " + e.getMessage(), e);
        }
    }
}
