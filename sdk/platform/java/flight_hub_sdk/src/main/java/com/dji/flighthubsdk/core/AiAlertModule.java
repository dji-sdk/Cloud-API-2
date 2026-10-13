// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** AI 告警管理模块（PAAS 纯血场景）。负责设备告警配置、算法阈值设置、告警架次查询和告警明细查询。 */
public class AiAlertModule {

    private static final String TAG = "AiAlertModule";

    private class CallbackRefContainer {
        private final Object callback_jni_ref;
        private final Object callback_java_ref;

        public CallbackRefContainer(Object jni, Object java) {
            this.callback_jni_ref = jni;
            this.callback_java_ref = java;
        }
    }

    private final ConcurrentHashMap<Integer, CallbackRefContainer> postDeviceSettingRef    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getDeviceSettingRef     = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> postAlgorithmSettingRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getAlgorithmSettingRef  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getFlightsRef           = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getRecordsRef           = new ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() { return id_counter++; }

    private void removeActionCallbackRef(ConcurrentHashMap<Integer, CallbackRefContainer> map, int id) {
        map.remove(id);
    }

    private final com.dji.flighthubsdk.generated.IAIAlertModule module;

    public AiAlertModule(com.dji.flighthubsdk.generated.IAIAlertModule module) {
        this.module = module;
    }

    @SuppressWarnings("unchecked")
    public void destroy() {
        List<ConcurrentHashMap<Integer, CallbackRefContainer>> allMaps = Arrays.asList(
                postDeviceSettingRef, getDeviceSettingRef, postAlgorithmSettingRef,
                getAlgorithmSettingRef, getFlightsRef, getRecordsRef);
        for (ConcurrentHashMap<Integer, CallbackRefContainer> map : allMaps) {
            map.clear();
        }
    }

    /**
     * 写入设备级告警配置。
     *
     * @implNote POST /drc/paas/v2/ai-alert/device-setting
     * @param param    请求参数（drone_sn、enable_target_alert、alarm_info）
     * @param callback 结果回调，在主线程触发
     */
    public void postDeviceSetting(
            com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingParam param,
            FlightHubCallback.PostAiAlertDeviceSettingCallback callback) {
        com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingParam np = null;
        try {
            np = FlightHubSdkConvert.toNativePostAiAlertDeviceSettingParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingCallback swigCb =
                new com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingCallback() {
                    @Override
                    public void onPostAiAlertDeviceSetting(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.PostAiAlertDeviceSettingResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toPostAiAlertDeviceSettingResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[postDeviceSetting] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(postDeviceSettingRef, id);
                        }
                    }
                };
            synchronized (postDeviceSettingRef) {
                postDeviceSettingRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.postDeviceSettingWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[postDeviceSetting] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toPostAiAlertDeviceSettingResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 读取设备级告警配置。
     *
     * @implNote GET /drc/paas/v2/ai-alert/device-setting
     * @param param    请求参数（drone_sn）
     * @param callback 结果回调，在主线程触发
     */
    public void getDeviceSetting(
            com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingParam param,
            FlightHubCallback.GetAiAlertDeviceSettingCallback callback) {
        com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeGetAiAlertDeviceSettingParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingCallback swigCb =
                new com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingCallback() {
                    @Override
                    public void onGetAiAlertDeviceSetting(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.GetAiAlertDeviceSettingResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetAiAlertDeviceSettingResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getDeviceSetting] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(getDeviceSettingRef, id);
                        }
                    }
                };
            synchronized (getDeviceSettingRef) {
                getDeviceSettingRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getDeviceSettingWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getDeviceSetting] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toGetAiAlertDeviceSettingResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 写入算法级阈值配置（Upsert）。
     *
     * @implNote POST /drc/paas/v2/ai-alert/algorithm-setting
     * @param param    请求参数（drone_sn、algorithm_type、algorithm_name、alarm_targets）
     * @param callback 结果回调，在主线程触发
     */
    public void postAlgorithmSetting(
            com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingParam param,
            FlightHubCallback.PostAiAlertAlgorithmSettingCallback callback) {
        com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingParam np = null;
        try {
            np = FlightHubSdkConvert.toNativePostAiAlertAlgorithmSettingParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingCallback swigCb =
                new com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingCallback() {
                    @Override
                    public void onPostAiAlertAlgorithmSetting(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.PostAiAlertAlgorithmSettingResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toPostAiAlertAlgorithmSettingResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[postAlgorithmSetting] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(postAlgorithmSettingRef, id);
                        }
                    }
                };
            synchronized (postAlgorithmSettingRef) {
                postAlgorithmSettingRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.postAlgorithmSettingWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[postAlgorithmSetting] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toPostAiAlertAlgorithmSettingResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 读取算法级阈值配置。
     *
     * @implNote GET /drc/paas/v2/ai-alert/algorithm-setting
     * @param param    请求参数（drone_sn、algorithm_type）
     * @param callback 结果回调，在主线程触发
     */
    public void getAlgorithmSetting(
            com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingParam param,
            FlightHubCallback.GetAiAlertAlgorithmSettingCallback callback) {
        com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeGetAiAlertAlgorithmSettingParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingCallback swigCb =
                new com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingCallback() {
                    @Override
                    public void onGetAiAlertAlgorithmSetting(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.GetAiAlertAlgorithmSettingResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetAiAlertAlgorithmSettingResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getAlgorithmSetting] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(getAlgorithmSettingRef, id);
                        }
                    }
                };
            synchronized (getAlgorithmSettingRef) {
                getAlgorithmSettingRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getAlgorithmSettingWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getAlgorithmSetting] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toGetAiAlertAlgorithmSettingResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 查询告警架次统计（分页）。
     *
     * @implNote GET /drc/paas/v2/ai-alert/flights
     * @param param    请求参数（drone_sn、begin_at、end_at、algorithm_sources、page、page_size）
     * @param callback 结果回调，在主线程触发
     */
    public void getAiAlertFlights(
            com.dji.flighthubsdk.bean.GetAiAlertFlightsParam param,
            FlightHubCallback.GetAiAlertFlightsCallback callback) {
        com.dji.flighthubsdk.generated.GetAiAlertFlightsParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeGetAiAlertFlightsParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetAiAlertFlightsCallback swigCb =
                new com.dji.flighthubsdk.generated.GetAiAlertFlightsCallback() {
                    @Override
                    public void onGetAiAlertFlights(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.GetAiAlertFlightsResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetAiAlertFlightsResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getAiAlertFlights] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(getFlightsRef, id);
                        }
                    }
                };
            synchronized (getFlightsRef) {
                getFlightsRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getAiAlertFlightsWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getAiAlertFlights] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toGetAiAlertFlightsResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 查询告警明细记录（按 flight_id 分组，分页）。
     *
     * @implNote GET /drc/paas/v2/ai-alert/records
     * @param param    请求参数（flight_id 列表、drone_sn 过滤、target_type、algorithm_sources、时间范围、分页）
     * @param callback 结果回调，在主线程触发
     */
    public void getAiAlertRecords(
            com.dji.flighthubsdk.bean.GetAiAlertRecordsParam param,
            FlightHubCallback.GetAiAlertRecordsCallback callback) {
        com.dji.flighthubsdk.generated.GetAiAlertRecordsParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeGetAiAlertRecordsParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetAiAlertRecordsCallback swigCb =
                new com.dji.flighthubsdk.generated.GetAiAlertRecordsCallback() {
                    @Override
                    public void onGetAiAlertRecords(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.GetAiAlertRecordsResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetAiAlertRecordsResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getAiAlertRecords] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(getRecordsRef, id);
                        }
                    }
                };
            synchronized (getRecordsRef) {
                getRecordsRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getAiAlertRecordsWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getAiAlertRecords] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toGetAiAlertRecordsResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }
}
