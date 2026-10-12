// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.util.Logger;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;

/**
 * 设备诊断（定损）管理模块接口，提供定损指令下发、操作、报告查询等功能。
 */
public class DiagnosisManagementModule {

    private static final String TAG = "DiagnosisManagementModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createSelfDiagnosisCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> operateSelfDiagnosisCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getSelfDiagnosisReportCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getSelfDiagnosisReportListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IDiagnosisManagementModule diagnosisManagementModule;

    /**
     * 构造诊断管理模块
     * @param diagnosisManagementModule 底层诊断管理模块接口
     */
    public DiagnosisManagementModule(IDiagnosisManagementModule diagnosisManagementModule) {
        this.diagnosisManagementModule = diagnosisManagementModule;
        Logger.info(TAG, "DiagnosisManagementModule instance created");
    }

    // ==================== 下发定损指令 ====================

    /**
     * [注意] 本接口暂未开放使用;
     * 下发设备自检定损指令
     * @param params 定损参数（设备拓扑等）
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /device/api/v1/service/self-diagnostic
     */
    public void createSelfDiagnosis(com.dji.flighthubsdk.bean.CreateSelfDiagnosisParams params, FlightHubCallback.CreateSelfDiagnosisCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CreateSelfDiagnosisCallback swigCallback = new com.dji.flighthubsdk.generated.CreateSelfDiagnosisCallback() {
                @Override
                public void onCreateSelfDiagnosis(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[createSelfDiagnosis] callback error: " + e.getMessage(), e);
                    } finally {
                        createSelfDiagnosisCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            createSelfDiagnosisCallbackRef.put(id, container);
            try {
                com.dji.flighthubsdk.generated.CreateSelfDiagnosisParams nativeCreateSelfDiagnosisParams = null;

                try {

                    nativeCreateSelfDiagnosisParams = FlightHubSdkConvert.toNativeCreateSelfDiagnosisParams(params);

                    this.diagnosisManagementModule.createSelfDiagnosisWrapper(nativeCreateSelfDiagnosisParams, swigCallback);

                } finally {

                    if (nativeCreateSelfDiagnosisParams != null) {

                        nativeCreateSelfDiagnosisParams.delete();

                    }

                }

            } catch (Exception e) {
                createSelfDiagnosisCallbackRef.remove(id);
                throw e;
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createSelfDiagnosis] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "[createSelfDiagnosis] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ==================== 定损操作(取消/删除) ====================

    /**
     * [注意] 本接口暂未开放使用;
     * 定损操作（取消/删除）
     * @param params 操作参数（报告 UUID、操作类型）
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /device/api/v1/manager/self-diagnostic/operation
     */
    public void operateSelfDiagnosis(com.dji.flighthubsdk.bean.OperateSelfDiagnosisParams params, FlightHubCallback.OperateSelfDiagnosisCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.OperateSelfDiagnosisCallback swigCallback = new com.dji.flighthubsdk.generated.OperateSelfDiagnosisCallback() {
                @Override
                public void onOperateSelfDiagnosis(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[operateSelfDiagnosis] callback error: " + e.getMessage(), e);
                    } finally {
                        operateSelfDiagnosisCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            operateSelfDiagnosisCallbackRef.put(id, container);
            try {
                com.dji.flighthubsdk.generated.OperateSelfDiagnosisParams nativeOperateSelfDiagnosisParams = null;

                try {

                    nativeOperateSelfDiagnosisParams = FlightHubSdkConvert.toNativeOperateSelfDiagnosisParams(params);

                    this.diagnosisManagementModule.operateSelfDiagnosisWrapper(nativeOperateSelfDiagnosisParams, swigCallback);

                } finally {

                    if (nativeOperateSelfDiagnosisParams != null) {

                        nativeOperateSelfDiagnosisParams.delete();

                    }

                }

            } catch (Exception e) {
                operateSelfDiagnosisCallbackRef.remove(id);
                throw e;
            }
        } catch (Exception e) {
            Logger.error(TAG, "[operateSelfDiagnosis] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "[operateSelfDiagnosis] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ==================== 查询单条定损报告详情 ====================

    /**
     * [注意] 本接口暂未开放使用;
     * 查询单条定损报告详情
     * @param reportUuid 报告 UUID
     * @param callback 结果回调，成功时返回报告详情
     * @implNote 链路: HTTP | GET /device/api/v1/manager/self-diagnostic/{report_uuid}
     */
    public void getSelfDiagnosisReport(String reportUuid, FlightHubCallback.GetSelfDiagnosisReportCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetSelfDiagnosisReportCallback swigCallback = new com.dji.flighthubsdk.generated.GetSelfDiagnosisReportCallback() {
                @Override
                public void onGetSelfDiagnosisReport(com.dji.flighthubsdk.generated.FlightHubError err, GetSelfDiagnosisReportResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetSelfDiagnosisReportResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getSelfDiagnosisReport] callback error: " + e.getMessage(), e);
                    } finally {
                        getSelfDiagnosisReportCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            getSelfDiagnosisReportCallbackRef.put(id, container);
            try {
                this.diagnosisManagementModule.getSelfDiagnosisReportWrapper(reportUuid, swigCallback);
            } catch (Exception e) {
                getSelfDiagnosisReportCallbackRef.remove(id);
                throw e;
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getSelfDiagnosisReport] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[getSelfDiagnosisReport] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ==================== 查询定损报告列表 ====================

    /**
     * [注意] 本接口暂未开放使用;
     * 查询定损报告列表
     * @param params 查询参数（分页、设备 SN 等）
     * @param callback 结果回调，成功时返回报告列表
     * @implNote 链路: HTTP | GET /device/api/v1/manager/self-diagnostic
     */
    public void getSelfDiagnosisReportList(com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListParams params, FlightHubCallback.GetSelfDiagnosisReportListCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetSelfDiagnosisReportListCallback swigCallback = new com.dji.flighthubsdk.generated.GetSelfDiagnosisReportListCallback() {
                @Override
                public void onGetSelfDiagnosisReportList(com.dji.flighthubsdk.generated.FlightHubError err, GetSelfDiagnosisReportListResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetSelfDiagnosisReportListResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getSelfDiagnosisReportList] callback error: " + e.getMessage(), e);
                    } finally {
                        getSelfDiagnosisReportListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            getSelfDiagnosisReportListCallbackRef.put(id, container);
            try {
                com.dji.flighthubsdk.generated.GetSelfDiagnosisReportListParams nativeGetSelfDiagnosisReportListParams = null;

                try {

                    nativeGetSelfDiagnosisReportListParams = FlightHubSdkConvert.toNativeGetSelfDiagnosisReportListParams(params);

                    this.diagnosisManagementModule.getSelfDiagnosisReportListWrapper(nativeGetSelfDiagnosisReportListParams, swigCallback);

                } finally {

                    if (nativeGetSelfDiagnosisReportListParams != null) {

                        nativeGetSelfDiagnosisReportListParams.delete();

                    }

                }

            } catch (Exception e) {
                getSelfDiagnosisReportListCallbackRef.remove(id);
                throw e;
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getSelfDiagnosisReportList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[getSelfDiagnosisReportList] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }
}
