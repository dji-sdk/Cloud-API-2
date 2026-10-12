// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 设备报修管理模块接口，提供报修配置查询、报修订单创建与查询等功能。
 */
public class MaintenanceManagementModule {

    private static final String TAG = "MaintenanceManagementModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getRepairConfigsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createMaintenanceOrderCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getMaintenanceOrderDetailCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getMaintenanceOrdersListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IMaintenanceManagementModule maintenanceManagementModule;

    /**
     * 构造维保管理模块
     * @param maintenanceManagementModule 底层维保管理模块接口
     */
    public MaintenanceManagementModule(IMaintenanceManagementModule maintenanceManagementModule) {
        this.maintenanceManagementModule = maintenanceManagementModule;
        Logger.info(TAG, "MaintenanceManagementModule instance created");
    }

    // ==================== 获取设备报修配置 ====================

    /**
     * 获取设备报修配置
     * @param params 请求参数
     * @param callback 结果回调，成功时返回报修配置信息
     * @implNote 链路: HTTP | GET /device/api/v1/manager/maintenance-orders/repair-config
     */
    public void getRepairConfigs(com.dji.flighthubsdk.bean.GetDeviceRepairConfigParams params, FlightHubCallback.GetRepairConfigsCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetRepairConfigsCallback swigCallback = new com.dji.flighthubsdk.generated.GetRepairConfigsCallback() {
                @Override
                public void onGetRepairConfigs(com.dji.flighthubsdk.generated.FlightHubError err, DeviceRepairConfigResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceRepairConfigResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getRepairConfigs] callback error: " + e.getMessage(), e);
                    } finally {
                        getRepairConfigsCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getRepairConfigsCallbackRef) {
                getRepairConfigsCallbackRef.put(id, container);
                com.dji.flighthubsdk.generated.GetDeviceRepairConfigParams nativeGetDeviceRepairConfigParams = null;

                try {

                    nativeGetDeviceRepairConfigParams = FlightHubSdkConvert.toNativeGetDeviceRepairConfigParams(params);

                    this.maintenanceManagementModule.getRepairConfigsWrapper(nativeGetDeviceRepairConfigParams, swigCallback);

                } finally {

                    if (nativeGetDeviceRepairConfigParams != null) {

                        nativeGetDeviceRepairConfigParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error(TAG, "[getRepairConfigs] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[getRepairConfigs] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ==================== 创建报修订单 ====================

    /**
     * 创建报修订单
     * @param params 创建报修订单的请求参数
     * @param callback 结果回调，成功时返回创建结果
     * @implNote 链路: HTTP | POST /device/api/v1/manager/maintenance-orders
     */
    public void createMaintenanceOrder(com.dji.flighthubsdk.bean.CreateMaintenanceOrderParams params, FlightHubCallback.CreateMaintenanceOrderCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CreateMaintenanceOrderCallback swigCallback = new com.dji.flighthubsdk.generated.CreateMaintenanceOrderCallback() {
                @Override
                public void onCreateMaintenanceOrder(com.dji.flighthubsdk.generated.FlightHubError err, CreateMaintenanceOrderResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCreateMaintenanceOrderResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[createMaintenanceOrder] callback error: " + e.getMessage(), e);
                    } finally {
                        createMaintenanceOrderCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (createMaintenanceOrderCallbackRef) {
                createMaintenanceOrderCallbackRef.put(id, container);
                com.dji.flighthubsdk.generated.CreateMaintenanceOrderParams nativeCreateMaintenanceOrderParams = null;

                try {

                    nativeCreateMaintenanceOrderParams = FlightHubSdkConvert.toNativeCreateMaintenanceOrderParams(params);

                    this.maintenanceManagementModule.createMaintenanceOrderWrapper(nativeCreateMaintenanceOrderParams, swigCallback);

                } finally {

                    if (nativeCreateMaintenanceOrderParams != null) {

                        nativeCreateMaintenanceOrderParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error(TAG, "[createMaintenanceOrder] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[createMaintenanceOrder] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ==================== 查询单个报修订单详情 ====================

    /**
     * 查询单个报修订单详情
     * @param orderNumber 报修订单编号
     * @param callback 结果回调，成功时返回订单详情
     * @implNote 链路: HTTP | GET /device/api/v1/manager/maintenance-orders/{orderNumber}
     */
    public void getMaintenanceOrderDetail(String orderNumber, FlightHubCallback.GetMaintenanceOrderDetailCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetMaintenanceOrderDetailCallback swigCallback = new com.dji.flighthubsdk.generated.GetMaintenanceOrderDetailCallback() {
                @Override
                public void onGetMaintenanceOrderDetail(com.dji.flighthubsdk.generated.FlightHubError err, MaintenanceOrderDetailResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toMaintenanceOrderDetailResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getMaintenanceOrderDetail] callback error: " + e.getMessage(), e);
                    } finally {
                        getMaintenanceOrderDetailCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getMaintenanceOrderDetailCallbackRef) {
                getMaintenanceOrderDetailCallbackRef.put(id, container);
                this.maintenanceManagementModule.getMaintenanceOrderDetailWrapper(orderNumber, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getMaintenanceOrderDetail] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[getMaintenanceOrderDetail] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ==================== 分页查询报修订单列表 ====================

    /**
     * 分页查询报修订单列表
     * @param params 查询参数（分页、筛选条件）
     * @param callback 结果回调，成功时返回订单列表和分页信息
     * @implNote 链路: HTTP | GET /device/api/v1/manager/maintenance-orders
     */
    public void getMaintenanceOrdersList(com.dji.flighthubsdk.bean.GetMaintenanceOrdersListParams params, FlightHubCallback.GetMaintenanceOrdersListCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetMaintenanceOrdersListCallback swigCallback = new com.dji.flighthubsdk.generated.GetMaintenanceOrdersListCallback() {
                @Override
                public void onGetMaintenanceOrdersList(com.dji.flighthubsdk.generated.FlightHubError err, MaintenanceOrdersListResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toMaintenanceOrdersListResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getMaintenanceOrdersList] callback error: " + e.getMessage(), e);
                    } finally {
                        getMaintenanceOrdersListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getMaintenanceOrdersListCallbackRef) {
                getMaintenanceOrdersListCallbackRef.put(id, container);
                com.dji.flighthubsdk.generated.GetMaintenanceOrdersListParams nativeGetMaintenanceOrdersListParams = null;

                try {

                    nativeGetMaintenanceOrdersListParams = FlightHubSdkConvert.toNativeGetMaintenanceOrdersListParams(params);

                    this.maintenanceManagementModule.getMaintenanceOrdersListWrapper(nativeGetMaintenanceOrdersListParams, swigCallback);

                } finally {

                    if (nativeGetMaintenanceOrdersListParams != null) {

                        nativeGetMaintenanceOrdersListParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error(TAG, "[getMaintenanceOrdersList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[getMaintenanceOrdersList] error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }
}
