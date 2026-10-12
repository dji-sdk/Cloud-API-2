// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IPsdkControlModule;
import com.dji.flighthubsdk.bean.PsdkSpeakerAudioPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSconfig;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.value.MegaphonePlayMode;

/**
 * PSDK 控制模块接口，提供 PSDK 设备管理、喊话器/探照灯/抛投器等负载控制。
 */
public class PsdkControlModule {

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

    // PSDK喊话器控制相关回调引用容器
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerAudioPlayStartCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerTTSPlayStartCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerReplayCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerPlayStopCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    // speakerRealTimeStart/Stop 已注释关闭（Java SDK 无 RTC/音频采集能力），下面两个字段一并注释
    // private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerRealTimeStartCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    // private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerRealTimeStopCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerPlayVolumeSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerPlayModeSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> speakerTTSSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> widgetValueSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> widgetInputBoxTextSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private final IPsdkControlModule psdkControlModule;
    private final String sn;
    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public PsdkControlModule(IPsdkControlModule psdkControlModule, String sn) {
        this.psdkControlModule = psdkControlModule;
        this.sn = sn;
    }

    // ==================== PSDK相关接口实现 ====================

    /**
     * 订阅 PSDK 设备列表
     * @param callback PSDK 设备列表回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_psdk_ui_resource (聚合去重后按 PSDK 索引透传)
     */
    public int listenDrcPsdkDeviceList(FlightHubCallback.PsdkDeviceListCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PsdkDeviceListCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkDeviceListCallback() {
                @Override
                public void onPsdkDeviceList(com.dji.flighthubsdk.generated.PsdkDeviceList psdk_device_list) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toPsdkDeviceList(psdk_device_list));
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] PSDK device list callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.psdkControlModule.listenPsdkDeviceListWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenDrcPsdkDeviceList error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toPsdkDeviceList(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 设备列表
     * @param listenId 监听 ID
     */
    public void cancelListenDrcPsdkDeviceList(int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkDeviceListWrapper(listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenDrcPsdkDeviceList error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 PSDK 状态摘要
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback PSDK 状态摘要回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_psdk_state_info (field: 通用 PSDK 状态摘要)
     */
    public int listenDrcPsdkStateSummaryInfo(int psdkIndex, FlightHubCallback.PsdkStateSummaryInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PsdkStateSummaryInfoCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkStateSummaryInfoCallback() {
                @Override
                public void onPsdkStateSummaryInfo(com.dji.flighthubsdk.generated.PsdkStateSummaryInfo psdk_state_summary_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toPsdkStateSummaryInfo(psdk_state_summary_info));
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] PSDK state summary info callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.psdkControlModule.listenPsdkStateSummaryInfoWrapper(psdkIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenDrcPsdkStateSummaryInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toPsdkStateSummaryInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 状态摘要
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param listenId 监听 ID
     */
    public void cancelListenDrcPsdkStateSummaryInfo(int psdkIndex, int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkStateSummaryInfoWrapper(psdkIndex, listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenDrcPsdkStateSummaryInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 PSDK 喊话器状态
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback PSDK 喊话器状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_psdk_state_info (field: 喊话器子集)
     */
    public int listenDrcPsdkStateSpeakerInfo(int psdkIndex, FlightHubCallback.PsdkStateSpeakerInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PsdkStateSpeakerInfoCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkStateSpeakerInfoCallback() {
                @Override
                public void onPsdkStateSpeakerInfo(com.dji.flighthubsdk.generated.PsdkStateSpeakerInfo psdk_state_speaker_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toPsdkStateSpeakerInfo(psdk_state_speaker_info));
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] PSDK state speaker info callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.psdkControlModule.listenPsdkStateSpeakerInfoWrapper(psdkIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenDrcPsdkStateSpeakerInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toPsdkStateSpeakerInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 喊话器状态
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param listenId 监听 ID
     */
    public void cancelListenDrcPsdkStateSpeakerInfo(int psdkIndex, int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkStateSpeakerInfoWrapper(psdkIndex, listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenDrcPsdkStateSpeakerInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 PSDK 探照灯状态
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback PSDK 探照灯状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_psdk_state_info (field: 探照灯子集)
     */
    public int listenDrcPsdkStateSearchlightInfo(int psdkIndex, FlightHubCallback.PsdkStateSearchlightInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PsdkStateSearchlightInfoCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkStateSearchlightInfoCallback() {
                @Override
                public void onPsdkStateSearchlightInfo(com.dji.flighthubsdk.generated.PsdkStateSearchlightInfo psdk_state_searchlight_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toPsdkStateSearchlightInfo(psdk_state_searchlight_info));
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] PSDK state searchlight info callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.psdkControlModule.listenPsdkStateSearchlightInfoWrapper(psdkIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenDrcPsdkStateSearchlightInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toPsdkStateSearchlightInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 探照灯状态
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param listenId 监听 ID
     */
    public void cancelListenDrcPsdkStateSearchlightInfo(int psdkIndex, int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkStateSearchlightInfoWrapper(psdkIndex, listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenDrcPsdkStateSearchlightInfo error: " + e.getMessage(), e);
        }
    }

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchlightModeSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchlightBrightnessSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchlightCalibrationCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchlightFineTuningSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchlightWideFieldModeSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchlightLightGimbalControlSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchlightTypeSetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 设置探照灯模式
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param mode 探照灯模式
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_light_mode_set
     */
    public void searchlightModeSet(int psdkIndex, int mode, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("PsdkControlModule", "[" + sn + "] Searchlight mode set callback error: " + e.getMessage(), e);
                    } finally {
                        searchlightModeSetCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchlightModeSetCallbackRef) {
                searchlightModeSetCallbackRef.put(id, container);
                this.psdkControlModule.searchlightModeSetWrapper(psdkIndex, mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] searchlightModeSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置探照灯亮度
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param brightness 亮度值
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_light_brightness_set
     */
    public void searchlightBrightnessSet(int psdkIndex, int brightness, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("PsdkControlModule", "[" + sn + "] Searchlight brightness set callback error: " + e.getMessage(), e);
                    } finally {
                        searchlightBrightnessSetCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchlightBrightnessSetCallbackRef) {
                searchlightBrightnessSetCallbackRef.put(id, container);
                this.psdkControlModule.searchlightBrightnessSetWrapper(psdkIndex, brightness, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] searchlightBrightnessSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 探照灯校准
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_light_calibration
     */
    public void searchlightCalibration(int psdkIndex, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("PsdkControlModule", "[" + sn + "] Searchlight calibration callback error: " + e.getMessage(), e);
                    } finally {
                        searchlightCalibrationCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchlightCalibrationCallbackRef) {
                searchlightCalibrationCallbackRef.put(id, container);
                this.psdkControlModule.searchlightCalibrationWrapper(psdkIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] searchlightCalibration error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 探照灯微调设置
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param position 微调位置
     * @param value 微调值
     * @param saved 是否保存
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_light_fine_tuning_set
     */
    public void searchlightFineTuningSet(int psdkIndex, int position, double value, boolean saved, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("PsdkControlModule", "[" + sn + "] Searchlight fine tuning set callback error: " + e.getMessage(), e);
                    } finally {
                        searchlightFineTuningSetCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchlightFineTuningSetCallbackRef) {
                searchlightFineTuningSetCallbackRef.put(id, container);
                this.psdkControlModule.searchlightFineTuningSetWrapper(psdkIndex, position, value, saved, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] searchlightFineTuningSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置探照灯广域模式
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param wideFieldMode 广域模式
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_light_wide_field_mode_set
     */
    public void searchlightWideFieldModeSet(int psdkIndex, int wideFieldMode, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("PsdkControlModule", "[" + sn + "] Searchlight wide field mode set callback error: " + e.getMessage(), e);
                    } finally {
                        searchlightWideFieldModeSetCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchlightWideFieldModeSetCallbackRef) {
                searchlightWideFieldModeSetCallbackRef.put(id, container);
                this.psdkControlModule.searchlightWideFieldModeSetWrapper(psdkIndex, wideFieldMode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] searchlightWideFieldModeSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置探照灯云台控制
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param lightGimbalControl 云台控制值
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_light_light_gimbal_control_set
     */
    public void searchlightLightGimbalControlSet(int psdkIndex, int lightGimbalControl, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("PsdkControlModule", "[" + sn + "] Searchlight gimbal control set callback error: " + e.getMessage(), e);
                    } finally {
                        searchlightLightGimbalControlSetCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchlightLightGimbalControlSetCallbackRef) {
                searchlightLightGimbalControlSetCallbackRef.put(id, container);
                this.psdkControlModule.searchlightLightGimbalControlSetWrapper(psdkIndex, lightGimbalControl, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] searchlightLightGimbalControlSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置探照灯类型
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param type 探照灯类型
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_light_type_set
     */
    public void searchlightTypeSet(int psdkIndex, int type, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("PsdkControlModule", "[" + sn + "] Searchlight type set callback error: " + e.getMessage(), e);
                    } finally {
                        searchlightTypeSetCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchlightTypeSetCallbackRef) {
                searchlightTypeSetCallbackRef.put(id, container);
                this.psdkControlModule.searchlightTypeSetWrapper(psdkIndex, type, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] searchlightTypeSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅 PSDK 喊话器播放进度
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback PSDK 喊话器播放进度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_speaker_play_progress
     */
    public int listenDrcPsdkSpeakerPlayProgressInfo(int psdkIndex, FlightHubCallback.PsdkSpeakerPlayProgressInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PsdkSpeakerPlayProgressInfoCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkSpeakerPlayProgressInfoCallback() {
                @Override
                public void onPsdkSpeakerPlayProgressInfo(com.dji.flighthubsdk.generated.PsdkSpeakerPlayProgressInfo psdk_speaker_play_progress_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toPsdkSpeakerPlayProgressInfo(psdk_speaker_play_progress_info));
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] PSDK speaker play progress info callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.psdkControlModule.listenPsdkSpeakerPlayProgressInfoWrapper(psdkIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenDrcPsdkSpeakerPlayProgressInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toPsdkSpeakerPlayProgressInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 喊话器播放进度
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param listenId 监听 ID
     */
    public void cancelListenDrcPsdkSpeakerPlayProgressInfo(int psdkIndex, int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkSpeakerPlayProgressInfoWrapper(psdkIndex, listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenDrcPsdkSpeakerPlayProgressInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 PSDK 浮窗文本
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback PSDK 浮窗文本回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_psdk_floating_window_text
     */
    public int listenDrcPsdkFloatingWindowTextInfo(int psdkIndex, FlightHubCallback.PsdkFloatingWindowTextInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.PsdkFloatingWindowTextInfoCallback swigCallback = new com.dji.flighthubsdk.generated.PsdkFloatingWindowTextInfoCallback() {
                @Override
                public void onPsdkFloatingWindowTextInfo(com.dji.flighthubsdk.generated.PsdkFloatingWindowTextInfo psdk_floating_window_text_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toPsdkFloatingWindowTextInfo(psdk_floating_window_text_info));
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] PSDK floating window text info callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.psdkControlModule.listenPsdkFloatingWindowTextInfoWrapper(psdkIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenDrcPsdkFloatingWindowTextInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toPsdkFloatingWindowTextInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 浮窗文本
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param listenId 监听 ID
     */
    public void cancelListenDrcPsdkFloatingWindowTextInfo(int psdkIndex, int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkFloatingWindowTextInfoWrapper(psdkIndex, listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenDrcPsdkFloatingWindowTextInfo error: " + e.getMessage(), e);
        }
    }

    // ==================== PSDK喊话器控制接口实现 ====================

    /**
     * 开始音频喊话
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param audioPlayInfo 音频播放信息
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_speaker_audio_play_start
     */
    public void speakerAudioPlayStart(int psdkIndex, PsdkSpeakerAudioPlayInfo audioPlayInfo, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.PsdkSpeakerAudioPlayInfo nativeInfo = null;
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Speaker audio play start callback error: " + e.getMessage(), e);
                    } finally {
                        speakerAudioPlayStartCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (speakerAudioPlayStartCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                speakerAudioPlayStartCallbackRef.put(id, container);
                nativeInfo = FlightHubSdkConvert.toNativePsdkSpeakerAudioPlayInfo(audioPlayInfo);
                this.psdkControlModule.speakerAudioPlayStartWrapper(psdkIndex, nativeInfo, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] speakerAudioPlayStart error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeInfo != null) {
                nativeInfo.delete();
            }
        }
    }

    /**
     * 开始 TTS 喊话
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param ttsPlayInfo TTS 播放信息
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_speaker_tts_play_start
     */
    public void speakerTTSPlayStart(int psdkIndex, PsdkSpeakerTTSPlayInfo ttsPlayInfo, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.PsdkSpeakerTTSPlayInfo nativeInfo = null;
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Speaker TTS play start callback error: " + e.getMessage(), e);
                    } finally {
                        speakerTTSPlayStartCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (speakerTTSPlayStartCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                speakerTTSPlayStartCallbackRef.put(id, container);
                nativeInfo = FlightHubSdkConvert.toNativePsdkSpeakerTTSPlayInfo(ttsPlayInfo);
                this.psdkControlModule.speakerTTSPlayStartWrapper(psdkIndex, nativeInfo, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] speakerTTSPlayStart error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeInfo != null) {
                nativeInfo.delete();
            }
        }
    }

    /**
     * 重播喊话
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_speaker_replay
     */
    public void speakerReplay(int psdkIndex, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Speaker replay callback error: " + e.getMessage(), e);
                    } finally {
                        speakerReplayCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (speakerReplayCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                speakerReplayCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.psdkControlModule.speakerReplayWrapper(psdkIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] speakerReplay error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 停止喊话播放
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_speaker_play_stop
     */
    public void speakerPlayStop(int psdkIndex, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Speaker play drcStop callback error: " + e.getMessage(), e);
                    } finally {
                        speakerPlayStopCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (speakerPlayStopCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                speakerPlayStopCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.psdkControlModule.speakerPlayStopWrapper(psdkIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] speakerPlayStop error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 喊话器实时喊话开始 —— <b>Java SDK 暂不开放</b>
     *
     * <p>本接口只是 DRC 信令入口，实际实时喊话完整链路还需要独立 RTC 音频房间：
     * <ol>
     *   <li>HTTP POST 分配 voice-channel 房间（Java SDK 未暴露该 HTTP 接口）</li>
     *   <li>浏览器 WebRTC / @volcengine/rtc 加入房间</li>
     *   <li>getUserMedia 采集麦克风推流（Java 无浏览器环境）</li>
     * </ol>
     * Java SDK 场景无 RTC 与音频采集能力，此接口无实际用途，已注释关闭。
     *
     * <p>Java SDK 场景请使用可用的替代方案：
     * <ul>
     *   <li>{@link #speakerTTSPlayStart} — 文字转语音（走 DRC 音频文件播放）</li>
     *   <li>{@link #speakerAudioPlayStart} — 上传音频文件后播放</li>
     * </ul>
     *
     * @implNote 底层 C++/Web/驾驶舱前端仍保留该接口，Java 层因缺配套能力隐藏。
     */
    // public void speakerRealTimeStart(int psdkIndex, FlightHubCallback.ResultCallbackWithError callback) {
    //     try {
    //         int id = generateActionCallbackId();
    //         com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
    //             @Override
    //             public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
    //                 try {
    //                     if (callback != null) {
    //                         com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
    //                             err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
    //                         callback.onResult(beanError);
    //                     }
    //                 } catch (Exception e) {
    //                     Logger.error("PsdkControlModule", "[" + sn + "] Speaker real time start callback error: " + e.getMessage(), e);
    //                 } finally {
    //                     speakerRealTimeStartCallbackRef.remove(id);
    //                 }
    //             }
    //         };
    //
    //         final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
    //
    //         synchronized (speakerRealTimeStartCallbackRef) {
    //             speakerRealTimeStartCallbackRef.put(id, container);
    //             this.psdkControlModule.speakerRealTimeStartWrapper(psdkIndex, swigCallback);
    //         }
    //     } catch (Exception e) {
    //         Logger.error("PsdkControlModule", "[" + sn + "] speakerRealTimeStart error: " + e.getMessage(), e);
    //         if (callback != null) {
    //             callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
    //         }
    //     }
    // }

    /**
     * 喊话器实时喊话停止 —— <b>Java SDK 暂不开放</b>
     *
     * <p>与 {@code speakerRealTimeStart} 成对使用。因 Java SDK 场景无 RTC 与音频采集能力，
     * 实时喊话完整链路走不通，本停止接口也随之关闭。详见 {@code speakerRealTimeStart} 说明。
     */
    // public void speakerRealTimeStop(int psdkIndex, FlightHubCallback.ResultCallbackWithError callback) {
    //     try {
    //         int id = generateActionCallbackId();
    //         com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
    //             @Override
    //             public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
    //                 try {
    //                     if (callback != null) {
    //                         com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
    //                             err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
    //                         callback.onResult(beanError);
    //                     }
    //                 } catch (Exception e) {
    //                     Logger.error("PsdkControlModule", "[" + sn + "] Speaker real time drcStop callback error: " + e.getMessage(), e);
    //                 } finally {
    //                     speakerRealTimeStopCallbackRef.remove(id);
    //                 }
    //             }
    //         };
    //
    //         final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
    //
    //         synchronized (speakerRealTimeStopCallbackRef) {
    //             speakerRealTimeStopCallbackRef.put(id, container);
    //             this.psdkControlModule.speakerRealTimeStopWrapper(psdkIndex, swigCallback);
    //         }
    //     } catch (Exception e) {
    //         Logger.error("PsdkControlModule", "[" + sn + "] speakerRealTimeStop error: " + e.getMessage(), e);
    //         if (callback != null) {
    //             callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
    //         }
    //     }
    // }

    /**
     * 设置喊话器播放音量
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param volume 音量值
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_speaker_play_volume_set
     */
    public void speakerPlayVolumeSet(int psdkIndex, int volume, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Speaker play volume set callback error: " + e.getMessage(), e);
                    } finally {
                        speakerPlayVolumeSetCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (speakerPlayVolumeSetCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                speakerPlayVolumeSetCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.psdkControlModule.speakerPlayVolumeSetWrapper(psdkIndex, volume, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] speakerPlayVolumeSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置喊话器播放模式
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param playMode 播放模式
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_speaker_play_mode_set
     */
    public void speakerPlayModeSet(int psdkIndex, MegaphonePlayMode playMode, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Speaker play mode set callback error: " + e.getMessage(), e);
                    } finally {
                        speakerPlayModeSetCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (speakerPlayModeSetCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                speakerPlayModeSetCallbackRef.put(id, container);
                
                int nativeMode = playMode != null ? playMode.getValue() : MegaphonePlayMode.UNKNOWN.getValue();
                this.psdkControlModule.speakerPlayModeSetWrapper(psdkIndex, nativeMode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] speakerPlayModeSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置 TTS 配置
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param ttsConfig TTS 配置参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_speaker_tts_set
     */
    public void speakerTTSSet(int psdkIndex, PsdkSpeakerTTSconfig ttsConfig, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.PsdkSpeakerTTSconfig nativeConfig = null;
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Speaker TTS set callback error: " + e.getMessage(), e);
                    } finally {
                        speakerTTSSetCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (speakerTTSSetCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                speakerTTSSetCallbackRef.put(id, container);
                nativeConfig = FlightHubSdkConvert.toNativePsdkSpeakerTTSconfig(ttsConfig);
                this.psdkControlModule.speakerTTSSetWrapper(psdkIndex, nativeConfig, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] speakerTTSSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeConfig != null) {
                nativeConfig.delete();
            }
        }
    }

    // ==================== PSDK控件接口实现 ====================

    /**
     * 设置 Widget 值
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param index Widget 索引
     * @param value Widget 值
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_psdk_widget_value_set
     */
    public void widgetValueSet(int psdkIndex, int index, int value, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Widget value set callback error: " + e.getMessage(), e);
                    } finally {
                        widgetValueSetCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (widgetValueSetCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                widgetValueSetCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.psdkControlModule.widgetValueSetWrapper(psdkIndex, index, value, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] widgetValueSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置 Widget 输入框文本
     * @param psdkIndex PSDK 索引（整型 PSDK 序号，从 listenDrcPsdkStateSummaryInfo 回调的 PsdkStateSummaryInfo.psdk_index 获取；区别于相机负载索引 std::string，格式 type-subtype-gimbalindex 如 "99-0-0"）
     * @param value 输入框文本内容
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_psdk_input_box_text_set
     */
    public void widgetInputBoxTextSet(int psdkIndex, String value, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanError = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message());
                            callback.onResult(beanError);
                        }
                    } catch (Exception e) {
                        Logger.error("PsdkControlModule", "[" + sn + "] Widget input box text set callback error: " + e.getMessage(), e);
                    } finally {
                        widgetInputBoxTextSetCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (widgetInputBoxTextSetCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                widgetInputBoxTextSetCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.psdkControlModule.widgetInputBoxTextSetWrapper(psdkIndex, value, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] widgetInputBoxTextSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ==================== PSDK喊话器播放开始进度监听 ====================

    /**
     * 订阅 PSDK 喊话器音频播放开始进度
     * @param callback 音频播放开始进度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_psdk_speaker_audio_play_start_progress
     */
    public int listenPsdkSpeakerAudioPlayStartProgress(FlightHubCallback.PsdkSpeakerAudioPlayStartProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.PsdkSpeakerAudioPlayStartProgressCallback swigCallback =
                new com.dji.flighthubsdk.generated.PsdkSpeakerAudioPlayStartProgressCallback() {
                    @Override
                    public void onPsdkSpeakerAudioPlayStartProgress(com.dji.flighthubsdk.generated.PsdkSpeakerPlayStartProgress msg) {
                        if (callback != null) {
                            callback.onResult(msg);
                        }
                    }
                };

            int listenId = this.psdkControlModule.listenPsdkSpeakerAudioPlayStartProgressWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenPsdkSpeakerAudioPlayStartProgress error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 喊话器音频播放开始进度
     * @param listenId 监听 ID
     */
    public void cancelListenPsdkSpeakerAudioPlayStartProgress(int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkSpeakerAudioPlayStartProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenPsdkSpeakerAudioPlayStartProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 PSDK 喊话器 TTS 播放开始进度
     * @param callback TTS 播放开始进度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_psdk_speaker_tts_play_start_progress
     */
    public int listenPsdkSpeakerTtsPlayStartProgress(FlightHubCallback.PsdkSpeakerTtsPlayStartProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.PsdkSpeakerTtsPlayStartProgressCallback swigCallback =
                new com.dji.flighthubsdk.generated.PsdkSpeakerTtsPlayStartProgressCallback() {
                    @Override
                    public void onPsdkSpeakerTtsPlayStartProgress(com.dji.flighthubsdk.generated.PsdkSpeakerPlayStartProgress msg) {
                        if (callback != null) {
                            callback.onResult(msg);
                        }
                    }
                };

            int listenId = this.psdkControlModule.listenPsdkSpeakerTtsPlayStartProgressWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenPsdkSpeakerTtsPlayStartProgress error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 PSDK 喊话器 TTS 播放开始进度
     * @param listenId 监听 ID
     */
    public void cancelListenPsdkSpeakerTtsPlayStartProgress(int listenId) {
        try {
            this.psdkControlModule.cancelListenPsdkSpeakerTtsPlayStartProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenPsdkSpeakerTtsPlayStartProgress error: " + e.getMessage(), e);
        }
    }

    // ===================== EA340 消息推送补全（Phase D） =====================

    /**
     * 订阅 PSDK UI 资源(psdk_ui_resource)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_drone_psdk_ui_resource
     */
    public int listenDronePsdkUiResource(FlightHubCallback.DronePsdkUiResourceCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DronePsdkUiResourceCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DronePsdkUiResourceCallback() {
                        @Override
                        public void onDronePsdkUiResource(com.dji.flighthubsdk.generated.DronePsdkUiResource msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDronePsdkUiResource(msg));
                            }
                        }
                    };
            int listenId = this.psdkControlModule.listenDronePsdkUiResourceWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] listenDronePsdkUiResource error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅 PSDK UI 资源 */
    public void cancelListenDronePsdkUiResource(int listenId) {
        try {
            this.psdkControlModule.cancelListenDronePsdkUiResourceWrapper(listenId);
        } catch (Exception e) {
            Logger.error("PsdkControlModule", "[" + sn + "] cancelListenDronePsdkUiResource error: " + e.getMessage(), e);
        }
    }

}
