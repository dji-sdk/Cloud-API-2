// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;

/**
 * 设备日志管理模块接口，提供日志文件查询、日志报告创建与管理、追加文件上传等功能。
 */
public class LogManagementModule {

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> queryDeviceLogListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createDeviceLogReportCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> logReportListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> logReportOperationCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> uploadLogAppendFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private ILogManagementModule logManagementModule;

    /**
     * 构造日志管理模块
     * @param logManagementModule 底层日志管理模块接口
     */
    public LogManagementModule(ILogManagementModule logManagementModule) {
        this.logManagementModule = logManagementModule;
    }

    /**
     * 查询设备日志文件列表
     * @param device_sn 设备序列号
     * @param module_list 模块过滤列表
     * @param callback 结果回调，成功时返回日志文件列表
     * @implNote 链路: HTTP | POST /device/api/v1/manager/reports/devices/logs
     */
    public void queryDeviceLogList(String device_sn, java.util.List<Integer> module_list, FlightHubCallback.QueryDeviceLogListCallback callback) {
        IntVector nativeModuleList = null;
        try {
            nativeModuleList = FlightHubSdkConvert.toNativeIntVector(module_list);
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.QueryDeviceLogListCallback swigCallback = new com.dji.flighthubsdk.generated.QueryDeviceLogListCallback() {
                @Override
                public void onQueryDeviceLogList(com.dji.flighthubsdk.generated.FlightHubError err, DeviceLogListResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceLogListResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("LogManagementModule", "[queryDeviceLogList] query device log list callback error: " + e.getMessage(), e);
                    } finally {
                        queryDeviceLogListCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (queryDeviceLogListCallbackRef) {
                queryDeviceLogListCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.logManagementModule.QueryDeviceLogListWrapper(device_sn, nativeModuleList, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LogManagementModule", "[queryDeviceLogList] query device log list error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
            }
        } finally {
            if (nativeModuleList != null) {
                nativeModuleList.delete();
            }
        }
    }

    /**
     * 创建设备日志报告
     * @param param 创建参数（报告信息、错误时间、设备拓扑、联系方式、日志列表等）
     * @param callback 结果回调，成功时返回报告 UUID 和工单号
     * @implNote 链路: HTTP | POST /device/api/v1/manager/reports
     */
    public void createDeviceLogReport(com.dji.flighthubsdk.bean.CreateDeviceLogReportRequestParam param, FlightHubCallback.CreateDeviceLogReportCallback callback) {
        com.dji.flighthubsdk.generated.CreateDeviceLogReportRequestParam nativeCreateDeviceLogReportRequestParam = null;
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CreateDeviceLogReportCallback swigCallback = new com.dji.flighthubsdk.generated.CreateDeviceLogReportCallback() {
                @Override
                public void onCreateDeviceLogReport(com.dji.flighthubsdk.generated.FlightHubError err, CreateDeviceLogReportResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCreateDeviceLogReportResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("LogManagementModule", "[createDeviceLogReport] create device log report callback error: " + e.getMessage(), e);
                    } finally {
                        createDeviceLogReportCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (createDeviceLogReportCallbackRef) {
                createDeviceLogReportCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));

                nativeCreateDeviceLogReportRequestParam = FlightHubSdkConvert.toNativeCreateDeviceLogReportRequestParam(param);

                this.logManagementModule.CreateDeviceLogReportWrapper(nativeCreateDeviceLogReportRequestParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LogManagementModule", "[createDeviceLogReport] create device log report error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
            }
        } finally {
            if (nativeCreateDeviceLogReportRequestParam != null) {
                nativeCreateDeviceLogReportRequestParam.delete();
            }
        }
    }

    /**
     * 查询日志报告列表（分页）
     * @param device_sn 设备序列号
     * @param page 页码
     * @param page_size 每页数量
     * @param callback 结果回调，成功时返回报告列表和分页信息
     * @implNote 链路: HTTP | GET /device/api/v1/manager/reports
     */
    public void logReportList(String device_sn, int page, int page_size, FlightHubCallback.LogReportListCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.LogReportListCallback swigCallback = new com.dji.flighthubsdk.generated.LogReportListCallback() {
                @Override
                public void onLogReportList(com.dji.flighthubsdk.generated.FlightHubError err, LogReportListResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toLogReportListResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("LogManagementModule", "[logReportList] log report list callback error: " + e.getMessage(), e);
                    } finally {
                        logReportListCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (logReportListCallbackRef) {
                logReportListCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.logManagementModule.LogReportListWrapper(device_sn, page, page_size, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LogManagementModule", "[logReportList] log report list error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
            }
        }
    }

    /**
     * 日志报告操作（取消、删除等）
     * @param operation 操作类型
     * @param report_id 报告 ID
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /device/api/v1/manager/reports/operation
     */
    public void logReportOperation(String operation, String report_id, FlightHubCallback.LogReportOperationCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.LogReportOperationCallback swigCallback = new com.dji.flighthubsdk.generated.LogReportOperationCallback() {
                @Override
                public void onLogReportOperation(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("LogManagementModule", "[logReportOperation] log report operation callback error: " + e.getMessage(), e);
                    } finally {
                        logReportOperationCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (logReportOperationCallbackRef) {
                logReportOperationCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.logManagementModule.LogReportOperationWrapper(operation, report_id, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LogManagementModule", "[logReportOperation] log report operation error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 上传追加文件到日志报告
     * @param file_path 本地文件路径
     * @param callback 结果回调
     * @implNote 链路: HTTP | 先获取 STS 临时凭证（/device/api/v1/manager/reports/sts），再上传至对象存储
     */
    public void uploadLogAppendFile(String file_path, FlightHubCallback.UploadLogAppendFileCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.UploadLogAppendFileCallback swigCallback = new com.dji.flighthubsdk.generated.UploadLogAppendFileCallback() {
                @Override
                public void onUploadLogAppendFile(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("LogManagementModule", "[uploadLogAppendFile] upload log append file callback error: " + e.getMessage(), e);
                    } finally {
                        uploadLogAppendFileCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (uploadLogAppendFileCallbackRef) {
                uploadLogAppendFileCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.logManagementModule.UploadLogAppendFileWrapper(file_path, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LogManagementModule", "[uploadLogAppendFile] upload log append file error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ==================== 设备文件上传进度监听 ====================

    /**
     * 订阅设备文件上传进度
     * @param deviceSn 设备序列号
     * @param callback 文件上传进度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_device_fileupload_progress
     */
    public int listenDeviceFileUploadProgress(String deviceSn, FlightHubCallback.DeviceFileUploadProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DeviceFileUploadProgressCallback swigCallback =
                new com.dji.flighthubsdk.generated.DeviceFileUploadProgressCallback() {
                    @Override
                    public void onDeviceFileUploadProgress(com.dji.flighthubsdk.generated.DeviceFileUploadProgress msg) {
                        if (callback != null) {
                            callback.onResult(msg);
                        }
                    }
                };
            int listenId = this.logManagementModule.listenDeviceFileUploadProgressWrapper(deviceSn, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("LogManagementModule", "[listenDeviceFileUploadProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅设备文件上传进度
     * @param listenId 监听 ID
     */
    public void cancelListenDeviceFileUploadProgress(int listenId) {
        try {
            this.logManagementModule.cancelListenDeviceFileUploadProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("LogManagementModule", "[cancelListenDeviceFileUploadProgress] error: " + e.getMessage(), e);
        }
    }
}
