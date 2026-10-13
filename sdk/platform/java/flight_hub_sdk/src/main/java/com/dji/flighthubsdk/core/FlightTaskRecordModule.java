// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 飞行任务记录模块接口，提供飞行记录导出和导出进度查询功能。
 */
public class FlightTaskRecordModule {

    private static final String TAG = "FlightTaskRecordModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> exportFlightRecordsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getExportFlightRecordsProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IFlightTaskRecordModule flightTaskRecordModule;

    /**
     * 构造飞行任务记录模块
     * @param flightTaskRecordModule 底层飞行任务记录模块接口
     */
    public FlightTaskRecordModule(IFlightTaskRecordModule flightTaskRecordModule) {
        this.flightTaskRecordModule = flightTaskRecordModule;
        Logger.info(TAG, "FlightTaskRecordModule instance created");
    }

    /**
     * 导出飞行记录
     * @param userId 用户 id, 比如 "15478287234981xxxxx"
     * @param params 导出飞行记录的请求参数。必填字段：contentType, fileType, fileName,
     *              flightTaskIds；缺少任一必填字段将返回错误。
     * @param callback 结果回调，成功时返回导出结果
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks/records/export
     */
    public void exportFlightRecords(String userId, com.dji.flighthubsdk.bean.ExportFlightRecordsRequestParams params, FlightHubCallback.ExportFlightRecordsCallback callback) {
        com.dji.flighthubsdk.generated.ExportFlightRecordsRequestParams nativeExportParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ExportFlightRecordsCallback swigCallback = new com.dji.flighthubsdk.generated.ExportFlightRecordsCallback() {
                @Override
                public void onExportFlightRecords(com.dji.flighthubsdk.generated.FlightHubError err, ExportFlightRecordsResults results) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toExportFlightRecordsResults(results));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[exportFlightRecords] callback error: " + e.getMessage(), e);
                    } finally {
                        exportFlightRecordsCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeExportParams = FlightHubSdkConvert.toNativeExportFlightRecordsRequestParams(params);
            synchronized (exportFlightRecordsCallbackRef) {
                exportFlightRecordsCallbackRef.put(id, container);
                this.flightTaskRecordModule.exportFlightRecordsWrapper(userId, nativeExportParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[exportFlightRecords] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeExportParams != null) {
                nativeExportParams.delete();
            }
        }
    }

    /**
     * 导出飞行记录（便捷重载，所有参数展开传入）
     * @param userId 用户 id, 比如 "15478287234981xxxxx"
     * @param contentType [必填] 导出类型，"summary"=摘要, "details"=详情
     * @param fileName [必填] 导出文件名
     * @param timeZone 时区, 如 "Asia/Shanghai"
     * @param locale 语言, 如 "zh"
     * @param fileType 文件类型列表, 如 ["Excel"]
     * @param flightTaskIds [必填] 飞行任务 ID 列表
     * @param metrics 指标列表
     * @param callback 结果回调，成功时返回导出结果
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks/records/export
     */
    public void exportFlightRecords(
            String userId,
            String contentType,
            String fileName,
            String timeZone,
            String locale,
            java.util.List<String> fileType,
            java.util.List<String> flightTaskIds,
            java.util.List<String> metrics,
            FlightHubCallback.ExportFlightRecordsCallback callback) {
        com.dji.flighthubsdk.bean.ExportFlightRecordsRequestParams params =
                new com.dji.flighthubsdk.bean.ExportFlightRecordsRequestParams();
        params.setContentType(contentType != null ? contentType : "");
        params.setFileName(fileName != null ? fileName : "");
        params.setTimeZone(timeZone != null ? timeZone : "");
        params.setLocale(locale != null ? locale : "");
        params.setFileType(fileType);
        params.setFlightTaskIds(flightTaskIds);
        params.setMetrics(metrics);
        exportFlightRecords(userId, params, callback);
    }

    /**
     * 查询导出进度
     * @param contentType 内容类型
     * @param status 状态
     * @param sort 排序
     * @param page 页码
     * @param pageSize 每页数量
     * @param exportId 导出 ID
     * @param callback 结果回调，成功时返回导出进度信息
     * @implNote 链路: HTTP | GET /atc/api/v1/flight-tasks/records/export
     */
    public void getExportFlightRecordsProgress(
            String contentType,
            int status,
            String sort,
            int page,
            int pageSize,
            String exportId,
            FlightHubCallback.GetExportFlightRecordsProgressCallback callback) {
        GetExportFlightRecordsProgressParams params = new GetExportFlightRecordsProgressParams();
        params.setContent_type(contentType != null ? contentType : "");
        params.setStatus(status);
        params.setSort(sort != null ? sort : "");
        params.setPage(page);
        params.setPage_size(pageSize);
        params.setExport_id(exportId != null ? exportId : "");
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetExportFlightRecordsProgressCallback swigCallback = new com.dji.flighthubsdk.generated.GetExportFlightRecordsProgressCallback() {
                @Override
                public void onGetExportFlightRecordsProgress(com.dji.flighthubsdk.generated.FlightHubError err, GetExportFlightRecordsProgressResults results) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetExportFlightRecordsProgressResults(results));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getExportFlightRecordsProgress] callback error: " + e.getMessage(), e);
                    } finally {
                        getExportFlightRecordsProgressCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getExportFlightRecordsProgressCallbackRef) {
                getExportFlightRecordsProgressCallbackRef.put(id, container);
                this.flightTaskRecordModule.getExportFlightRecordsProgressWrapper(params, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getExportFlightRecordsProgress] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

}
