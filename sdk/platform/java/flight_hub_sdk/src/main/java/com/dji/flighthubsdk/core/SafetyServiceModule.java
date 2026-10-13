// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 安全服务模块接口，提供信号地图查询、自定义飞行区域管理、地形障碍物离线地图管理等功能。
 */
public class SafetyServiceModule {

    private static final String TAG = "SafetyServiceModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> searchSignalMapsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createCustomizedFlightAreasCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getCustomizedFlightAreasCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateCustomizedFlightAreasCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deleteCustomizedFlightAreasCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> syncCustomizedFlightAreasCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getFlightAreasSyncStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getOfflineMapsModelInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateOfflineMapsModelInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> cancelOfflineMapsModelInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setOfflineMapsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> syncOfflineMapsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getOfflineMapsSyncStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getUnlockLicenseListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> switchUnlockLicenseCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> checkUnlockLicenseCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> submitUnlockLicenseCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> clearDeviceUnlockLicenseCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> uploadUnlockLicenseFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private ISafetyServiceModule safetyServiceModule;

    /**
     * 构造安全服务模块
     * @param safetyServiceModule 底层安全服务模块接口
     */
    public SafetyServiceModule(ISafetyServiceModule safetyServiceModule) {
        this.safetyServiceModule = safetyServiceModule;
        Logger.info(TAG, "SafetyServiceModule instance created");
    }

    // ==================== 信号地图 ====================

    /**
     * 获取信号地图
     * @param deviceSns 设备 SN 列表
     * @param dataTypes 待查询的数据类型, 比如 ["4g", "sdr"]
     * @param callback 结果回调，成功时返回信号地图数据
     * @implNote 链路: HTTP | POST /safety/api/v1/signal-maps/search
     */
    public void searchSignalMaps(java.util.List<String> deviceSns, java.util.List<String> dataTypes, FlightHubCallback.SearchSignalMapsCallback callback) {
        StringVector nativeDeviceSns = null;
        StringVector nativeDataTypes = null;
        try {
            nativeDeviceSns = FlightHubSdkConvert.toNativeStringVector(deviceSns);
            nativeDataTypes = FlightHubSdkConvert.toNativeStringVector(dataTypes);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SearchSignalMapsCallback swigCallback = new com.dji.flighthubsdk.generated.SearchSignalMapsCallback() {
                @Override
                public void onSearchSignalMaps(com.dji.flighthubsdk.generated.FlightHubError err, SignalMapsResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSignalMapsResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[searchSignalMaps] callback error: " + e.getMessage(), e);
                    } finally {
                        searchSignalMapsCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (searchSignalMapsCallbackRef) {
                searchSignalMapsCallbackRef.put(id, container);
                this.safetyServiceModule.searchSignalMapsWrapper(nativeDeviceSns, nativeDataTypes, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[searchSignalMaps] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[searchSignalMaps] error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeDeviceSns != null) {
                nativeDeviceSns.delete();
            }
            if (nativeDataTypes != null) {
                nativeDataTypes.delete();
            }
        }
    }

    // ==================== 自定义飞行区域 ====================

    /**
     * 创建自定义飞行区域
     * @param requestParams 创建参数
     * @param callback 结果回调，成功时返回创建结果
     * @implNote 链路: HTTP | POST /safety/api/v2/flight-areas
     */
    public void createCustomizedFlightAreas(com.dji.flighthubsdk.bean.CreateCustomizedFlightAreasParams requestParams, FlightHubCallback.CreateCustomizedFlightAreasCallback callback) {
        com.dji.flighthubsdk.generated.CreateCustomizedFlightAreasParams nativeCreateCustomizedFlightAreasParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CreateCustomizedFlightAreasCallback swigCallback = new com.dji.flighthubsdk.generated.CreateCustomizedFlightAreasCallback() {
                @Override
                public void onCreateCustomizedFlightAreas(com.dji.flighthubsdk.generated.FlightHubError err, CreateFlightAreasResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCreateFlightAreasResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[createCustomizedFlightAreas] callback error: " + e.getMessage(), e);
                    } finally {
                        createCustomizedFlightAreasCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeCreateCustomizedFlightAreasParams = FlightHubSdkConvert.toNativeCreateCustomizedFlightAreasParams(requestParams);
            synchronized (createCustomizedFlightAreasCallbackRef) {
                createCustomizedFlightAreasCallbackRef.put(id, container);
                this.safetyServiceModule.createCustomizedFlightAreasWrapper(nativeCreateCustomizedFlightAreasParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createCustomizedFlightAreas] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[createCustomizedFlightAreas] error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeCreateCustomizedFlightAreasParams != null) {
                nativeCreateCustomizedFlightAreasParams.delete();
            }
        }
    }

    /**
     * 获取自定义飞行区域
     * @param requestParams 查询参数
     * @param callback 结果回调，成功时返回飞行区域列表
     * @implNote 链路: HTTP | GET /safety/api/v2/flight-areas
     */
    public void getCustomizedFlightAreas(com.dji.flighthubsdk.bean.GetCustomizedFlightAreasParams requestParams, FlightHubCallback.GetCustomizedFlightAreasCallback callback) {
        com.dji.flighthubsdk.generated.GetCustomizedFlightAreasParams nativeGetCustomizedFlightAreasParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetCustomizedFlightAreasCallback swigCallback = new com.dji.flighthubsdk.generated.GetCustomizedFlightAreasCallback() {
                @Override
                public void onGetCustomizedFlightAreas(com.dji.flighthubsdk.generated.FlightHubError err, GetFlightAreasResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetFlightAreasResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getCustomizedFlightAreas] callback error: " + e.getMessage(), e);
                    } finally {
                        getCustomizedFlightAreasCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeGetCustomizedFlightAreasParams = FlightHubSdkConvert.toNativeGetCustomizedFlightAreasParams(requestParams);
            synchronized (getCustomizedFlightAreasCallbackRef) {
                getCustomizedFlightAreasCallbackRef.put(id, container);
                this.safetyServiceModule.getCustomizedFlightAreasWrapper(nativeGetCustomizedFlightAreasParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getCustomizedFlightAreas] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "[getCustomizedFlightAreas] error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeGetCustomizedFlightAreasParams != null) {
                nativeGetCustomizedFlightAreasParams.delete();
            }
        }
    }

    /**
     * 获取自定义飞行区域
     * @param name 名称
     * @param type 类型
     * @param status 状态
     * @param ids ID 列表
     * @param pageIndex 页码
     * @param pageSize 每页数量
     * @param callback 结果回调，成功时返回飞行区域列表
     * @implNote 链路: HTTP | GET /safety/api/v2/flight-areas
     */
    public void getCustomizedFlightAreas(
            String name,
            String type,
            String status,
            java.util.List<String> ids,
            int pageIndex,
            int pageSize,
            FlightHubCallback.GetCustomizedFlightAreasCallback callback) {
        com.dji.flighthubsdk.bean.GetCustomizedFlightAreasParams requestParams = new com.dji.flighthubsdk.bean.GetCustomizedFlightAreasParams();
        requestParams.setName(name != null ? name : "");
        requestParams.setType(type != null ? type : "");
        requestParams.setStatus(status != null ? status : "");
        requestParams.setIds(ids);
        requestParams.setPageIndex(pageIndex);
        requestParams.setPageSize(pageSize);
        getCustomizedFlightAreas(requestParams, callback);
    }

    /**
     * 更新自定义飞行区域
     * @param requestParams 更新参数
     * @param callback 结果回调，成功时返回更新结果
     * @implNote 链路: HTTP | PUT /safety/api/v2/flight-areas/{flight-area-id}
     */
    public void updateCustomizedFlightAreas(com.dji.flighthubsdk.bean.UpdateCustomizedFlightAreasParams requestParams, FlightHubCallback.UpdateCustomizedFlightAreasCallback callback) {
        com.dji.flighthubsdk.generated.UpdateCustomizedFlightAreasParams nativeUpdateCustomizedFlightAreasParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UpdateCustomizedFlightAreasCallback swigCallback = new com.dji.flighthubsdk.generated.UpdateCustomizedFlightAreasCallback() {
                @Override
                public void onUpdateCustomizedFlightAreas(com.dji.flighthubsdk.generated.FlightHubError err, UpdateFlightAreasResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toUpdateFlightAreasResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[updateCustomizedFlightAreas] callback error: " + e.getMessage(), e);
                    } finally {
                        updateCustomizedFlightAreasCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeUpdateCustomizedFlightAreasParams = FlightHubSdkConvert.toNativeUpdateCustomizedFlightAreasParams(requestParams);
            synchronized (updateCustomizedFlightAreasCallbackRef) {
                updateCustomizedFlightAreasCallbackRef.put(id, container);
                this.safetyServiceModule.updateCustomizedFlightAreasWrapper(nativeUpdateCustomizedFlightAreasParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[updateCustomizedFlightAreas] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeUpdateCustomizedFlightAreasParams != null) {
                nativeUpdateCustomizedFlightAreasParams.delete();
            }
        }
    }

    /**
     * 删除自定义飞行区域
     * @param flightAreaId 飞行区域 ID
     * @param callback 结果回调
     * @implNote 链路: HTTP | DELETE /safety/api/v2/flight-areas/{flight-area-id}
     */
    public void deleteCustomizedFlightAreas(String flightAreaId, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[deleteCustomizedFlightAreas] callback error: " + e.getMessage(), e);
                    } finally {
                        deleteCustomizedFlightAreasCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (deleteCustomizedFlightAreasCallbackRef) {
                deleteCustomizedFlightAreasCallbackRef.put(id, container);
                this.safetyServiceModule.deleteCustomizedFlightAreasWrapper(flightAreaId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteCustomizedFlightAreas] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 同步自定义飞行区域
     * @param deviceSns 待同步设备的 SN，支持网关类型设备（机场1、2、3 以及地面站）
     * @param callback 结果回调，成功时返回同步结果
     * @implNote 链路: HTTP | POST /safety/api/v1/flight-areas/sync
     */
    public void syncCustomizedFlightAreas(java.util.List<String> deviceSns, FlightHubCallback.SyncCustomizedFlightAreasCallback callback) {
        StringVector nativeDeviceSns = null;
        try {
            nativeDeviceSns = FlightHubSdkConvert.toNativeStringVector(deviceSns);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SyncCustomizedFlightAreasCallback swigCallback = new com.dji.flighthubsdk.generated.SyncCustomizedFlightAreasCallback() {
                @Override
                public void onSyncCustomizedFlightAreas(com.dji.flighthubsdk.generated.FlightHubError err, SyncFlightAreasResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSyncFlightAreasResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[syncCustomizedFlightAreas] callback error: " + e.getMessage(), e);
                    } finally {
                        syncCustomizedFlightAreasCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (syncCustomizedFlightAreasCallbackRef) {
                syncCustomizedFlightAreasCallbackRef.put(id, container);
                this.safetyServiceModule.syncCustomizedFlightAreasWrapper(nativeDeviceSns, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[syncCustomizedFlightAreas] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeDeviceSns != null) {
                nativeDeviceSns.delete();
            }
        }
    }

    /**
     * 获取自定义飞行区域同步状态
     * @param deviceSns 待同步设备的 SN，支持网关类型设备（机场1、2、3 以及地面站）
     * @param callback 结果回调，成功时返回同步状态
     * @implNote 链路: HTTP | GET /safety/api/v1/flight-areas/device-status
     */
    public void getFlightAreasSyncStatus(java.util.List<String> deviceSns, FlightHubCallback.GetFlightAreasSyncStatusCallback callback) {
        StringVector nativeDeviceSns = null;
        try {
            nativeDeviceSns = FlightHubSdkConvert.toNativeStringVector(deviceSns);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetFlightAreasSyncStatusCallback swigCallback = new com.dji.flighthubsdk.generated.GetFlightAreasSyncStatusCallback() {
                @Override
                public void onGetFlightAreasSyncStatus(com.dji.flighthubsdk.generated.FlightHubError err, GetAreasSyncStatusResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetAreasSyncStatusResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getFlightAreasSyncStatus] callback error: " + e.getMessage(), e);
                    } finally {
                        getFlightAreasSyncStatusCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getFlightAreasSyncStatusCallbackRef) {
                getFlightAreasSyncStatusCallbackRef.put(id, container);
                this.safetyServiceModule.getFlightAreasSyncStatusWrapper(nativeDeviceSns, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getFlightAreasSyncStatus] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeDeviceSns != null) {
                nativeDeviceSns.delete();
            }
        }
    }

    // ==================== 地形障碍物 ====================

    /**
     * 查询离线地图模型信息
     * @param callback 结果回调，成功时返回离线地图模型信息
     * @implNote 链路: HTTP | GET /safety/api/v1/offline-maps
     */
    public void getOfflineMapsModelInfo(FlightHubCallback.GetOfflineMapsModelInfoCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetOfflineMapsModelInfoCallback swigCallback = new com.dji.flighthubsdk.generated.GetOfflineMapsModelInfoCallback() {
                @Override
                public void onGetOfflineMapsModelInfo(com.dji.flighthubsdk.generated.FlightHubError err, GetOfflineMapsModelInfoResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetOfflineMapsModelInfoResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getOfflineMapsModelInfo] callback error: " + e.getMessage(), e);
                    } finally {
                        getOfflineMapsModelInfoCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getOfflineMapsModelInfoCallbackRef) {
                getOfflineMapsModelInfoCallbackRef.put(id, container);
                this.safetyServiceModule.getOfflineMapsModelInfoWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getOfflineMapsModelInfo] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 创建/更新离线地图模型
     * @param requestParams 更新参数
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /safety/api/v1/offline-maps
     */
    public void updateOfflineMapsModelInfo(com.dji.flighthubsdk.bean.UpdateOfflineMapsParams requestParams, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.UpdateOfflineMapsParams nativeUpdateOfflineMapsParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[updateOfflineMapsModelInfo] callback error: " + e.getMessage(), e);
                    } finally {
                        updateOfflineMapsModelInfoCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeUpdateOfflineMapsParams = FlightHubSdkConvert.toNativeUpdateOfflineMapsParams(requestParams);
            synchronized (updateOfflineMapsModelInfoCallbackRef) {
                updateOfflineMapsModelInfoCallbackRef.put(id, container);
                this.safetyServiceModule.updateOfflineMapsModelInfoWrapper(nativeUpdateOfflineMapsParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[updateOfflineMapsModelInfo] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeUpdateOfflineMapsParams != null) {
                nativeUpdateOfflineMapsParams.delete();
            }
        }
    }

    /**
     * 取消离线地图生成
     * @param callback 结果回调
     * @implNote 链路: HTTP | DELETE /safety/api/v1/offline-maps
     */
    public void cancelOfflineMapsModelInfo(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[cancelOfflineMapsModelInfo] callback error: " + e.getMessage(), e);
                    } finally {
                        cancelOfflineMapsModelInfoCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (cancelOfflineMapsModelInfoCallbackRef) {
                cancelOfflineMapsModelInfoCallbackRef.put(id, container);
                this.safetyServiceModule.cancelOfflineMapsModelInfoWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[cancelOfflineMapsModelInfo] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 修改离线地图（开启/关闭）
     * @param requestParams 设置参数
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /safety/api/v1/offline-maps
     */
    public void setOfflineMaps(com.dji.flighthubsdk.bean.SetOfflineMapsParams requestParams, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetOfflineMapsParams nativeSetOfflineMapsParams = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[setOfflineMaps] callback error: " + e.getMessage(), e);
                    } finally {
                        setOfflineMapsCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            nativeSetOfflineMapsParams = FlightHubSdkConvert.toNativeSetOfflineMapsParams(requestParams);
            synchronized (setOfflineMapsCallbackRef) {
                setOfflineMapsCallbackRef.put(id, container);
                this.safetyServiceModule.setOfflineMapsWrapper(nativeSetOfflineMapsParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[setOfflineMaps] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeSetOfflineMapsParams != null) {
                nativeSetOfflineMapsParams.delete();
            }
        }
    }

    /**
     * 离线地图同步
     * @param deviceSns 待同步设备的 SN，支持网关类型设备（机场1、2、3 以及地面站）
     * @param callback 结果回调，成功时返回同步结果
     * @implNote 链路: HTTP | POST /safety/api/v1/offline-maps/sync
     */
    public void syncOfflineMaps(java.util.List<String> deviceSns, FlightHubCallback.SyncOfflineMapsCallback callback) {
        StringVector nativeDeviceSns = null;
        try {
            nativeDeviceSns = FlightHubSdkConvert.toNativeStringVector(deviceSns);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SyncOfflineMapsCallback swigCallback = new com.dji.flighthubsdk.generated.SyncOfflineMapsCallback() {
                @Override
                public void onSyncOfflineMaps(com.dji.flighthubsdk.generated.FlightHubError err, OfflineMapsSyncResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toOfflineMapsSyncResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[syncOfflineMaps] callback error: " + e.getMessage(), e);
                    } finally {
                        syncOfflineMapsCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (syncOfflineMapsCallbackRef) {
                syncOfflineMapsCallbackRef.put(id, container);
                this.safetyServiceModule.syncOfflineMapsWrapper(nativeDeviceSns, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[syncOfflineMaps] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeDeviceSns != null) {
                nativeDeviceSns.delete();
            }
        }
    }

    /**
     * 获取设备同步状态
     * @param deviceSns 待同步设备的 SN
     * @param callback 结果回调，成功时返回同步状态
     * @implNote 链路: HTTP | GET /safety/api/v1/offline-maps/device-status
     */
    public void getOfflineMapsSyncStatus(java.util.List<String> deviceSns, FlightHubCallback.GetOfflineMapsSyncStatusCallback callback) {
        StringVector nativeDeviceSns = null;
        try {
            nativeDeviceSns = FlightHubSdkConvert.toNativeStringVector(deviceSns);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetOfflineMapsSyncStatusCallback swigCallback = new com.dji.flighthubsdk.generated.GetOfflineMapsSyncStatusCallback() {
                @Override
                public void onGetOfflineMapsSyncStatus(com.dji.flighthubsdk.generated.FlightHubError err, GetOfflineMapsSyncStatusResults result) {
                    try {
                        if (callback != null) {
                            callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetOfflineMapsSyncStatusResults(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getOfflineMapsSyncStatus] callback error: " + e.getMessage(), e);
                    } finally {
                        getOfflineMapsSyncStatusCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getOfflineMapsSyncStatusCallbackRef) {
                getOfflineMapsSyncStatusCallbackRef.put(id, container);
                this.safetyServiceModule.getOfflineMapsSyncStatusWrapper(nativeDeviceSns, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getOfflineMapsSyncStatus] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeDeviceSns != null) {
                nativeDeviceSns.delete();
            }
        }
    }

    // ==================== Flysafe / 解锁证书 ====================

    /**
     * 获取解锁证书列表
     * @param param 查询参数
     * @param callback 回调，返回解锁证书列表与错误信息
     * @implNote 链路: HTTP | GET /device/api/v1/service/unlock-license/list
     */
    public void getUnlockLicenseList(com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListParam param, FlightHubCallback.GetUnlockLicenseListCallback callback) {
        com.dji.flighthubsdk.generated.QueryDeviceUnlockLicenseListParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeQueryDeviceUnlockLicenseListParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetUnlockLicenseListCallback swigCallback = new com.dji.flighthubsdk.generated.GetUnlockLicenseListCallback() {
                @Override
                public void onGetUnlockLicenseList(com.dji.flighthubsdk.generated.FlightHubError err, QueryDeviceUnlockLicenseListResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toQueryDeviceUnlockLicenseListResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getUnlockLicenseList] callback error: " + e.getMessage(), e);
                    } finally {
                        getUnlockLicenseListCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getUnlockLicenseListCallbackRef) {
                getUnlockLicenseListCallbackRef.put(id, container);
                safetyServiceModule.getUnlockLicenseListWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getUnlockLicenseList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toQueryDeviceUnlockLicenseListResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 切换解锁证书状态
     * @param param 切换参数
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/unlock-license/switch
     */
    public void switchUnlockLicense(com.dji.flighthubsdk.bean.SwitchUnlockLicenseParam param, FlightHubCallback.SwitchUnlockLicenseCallback callback) {
        com.dji.flighthubsdk.generated.SwitchUnlockLicenseParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeSwitchUnlockLicenseParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SwitchUnlockLicenseCallback swigCallback = new com.dji.flighthubsdk.generated.SwitchUnlockLicenseCallback() {
                @Override
                public void onSwitchUnlockLicense(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[switchUnlockLicense] callback error: " + e.getMessage(), e);
                    } finally {
                        switchUnlockLicenseCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (switchUnlockLicenseCallbackRef) {
                switchUnlockLicenseCallbackRef.put(id, container);
                safetyServiceModule.switchUnlockLicenseWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[switchUnlockLicense] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 检查解锁证书
     * @param param 检查参数
     * @param callback 回调，返回检查结果与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/unlock-license/check
     */
    public void checkUnlockLicense(com.dji.flighthubsdk.bean.CheckUnlockLicenseParam param, FlightHubCallback.CheckUnlockLicenseCallback callback) {
        com.dji.flighthubsdk.generated.CheckUnlockLicenseParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeCheckUnlockLicenseParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CheckUnlockLicenseCallback swigCallback = new com.dji.flighthubsdk.generated.CheckUnlockLicenseCallback() {
                @Override
                public void onCheckUnlockLicense(com.dji.flighthubsdk.generated.FlightHubError err, CheckUnlockLicenseResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCheckUnlockLicenseResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[checkUnlockLicense] callback error: " + e.getMessage(), e);
                    } finally {
                        checkUnlockLicenseCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (checkUnlockLicenseCallbackRef) {
                checkUnlockLicenseCallbackRef.put(id, container);
                safetyServiceModule.checkUnlockLicenseWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[checkUnlockLicense] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toCheckUnlockLicenseResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 提交解锁证书
     * @param param 提交参数
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/unlock-license/unlock
     */
    public void submitUnlockLicense(com.dji.flighthubsdk.bean.ImportUnlockLicenseParam param, FlightHubCallback.SubmitUnlockLicenseCallback callback) {
        com.dji.flighthubsdk.generated.ImportUnlockLicenseParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeImportUnlockLicenseParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SubmitUnlockLicenseCallback swigCallback = new com.dji.flighthubsdk.generated.SubmitUnlockLicenseCallback() {
                @Override
                public void onSubmitUnlockLicense(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[submitUnlockLicense] callback error: " + e.getMessage(), e);
                    } finally {
                        submitUnlockLicenseCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (submitUnlockLicenseCallbackRef) {
                submitUnlockLicenseCallbackRef.put(id, container);
                safetyServiceModule.submitUnlockLicenseWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[submitUnlockLicense] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    /**
     * 清除设备解锁证书
     * @param deviceSn 设备序列号
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/service/unlock-license/clear
     */
    public void clearDeviceUnlockLicense(String deviceSn, FlightHubCallback.ClearDeviceUnlockLicenseCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ClearDeviceUnlockLicenseCallback swigCallback = new com.dji.flighthubsdk.generated.ClearDeviceUnlockLicenseCallback() {
                @Override
                public void onClearDeviceUnlockLicense(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[clearDeviceUnlockLicense] callback error: " + e.getMessage(), e);
                    } finally {
                        clearDeviceUnlockLicenseCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (clearDeviceUnlockLicenseCallbackRef) {
                clearDeviceUnlockLicenseCallbackRef.put(id, container);
                safetyServiceModule.clearDeviceUnlockLicenseWrapper(deviceSn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[clearDeviceUnlockLicense] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ==================== 飞安数据库升级进度监听 ====================

    /**
     * 上传解锁证书文件
     * @param filePath 本地文件路径
     * @param callback 回调，返回上传结果与错误信息
     * @implNote 链路: SDK 内部 | 获取 STS 凭证后上传解锁证书文件
     */
    public void uploadUnlockLicenseFile(String filePath, FlightHubCallback.UploadUnlockLicenseFileCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UploadUnlockLicenseFileCallback swigCallback = new com.dji.flighthubsdk.generated.UploadUnlockLicenseFileCallback() {
                @Override
                public void onUploadUnlockLicenseFile(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.UploadUnlockLicenseFileResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toUploadUnlockLicenseFileResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[uploadUnlockLicenseFile] callback error: " + e.getMessage(), e);
                    } finally {
                        uploadUnlockLicenseFileCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (uploadUnlockLicenseFileCallbackRef) {
                uploadUnlockLicenseFileCallbackRef.put(id, container);
                safetyServiceModule.uploadUnlockLicenseFileWrapper(filePath, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[uploadUnlockLicenseFile] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toUploadUnlockLicenseFileResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    // ===================== EA340 消息推送补全（Phase D） =====================

    /**
     * 订阅飞行器离线地图开关(offline_map_enable)
     * @param droneSn 飞行器序列号
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_drone_offline_map_enable
     */
    public int listenDroneOfflineMapEnable(String droneSn, FlightHubCallback.DroneOfflineMapEnableCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneOfflineMapEnableCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneOfflineMapEnableCallback() {
                        @Override
                        public void onDroneOfflineMapEnable(com.dji.flighthubsdk.generated.DroneOfflineMapEnable msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneOfflineMapEnable(msg));
                            }
                        }
                    };
            int listenId = safetyServiceModule.listenDroneOfflineMapEnableWrapper(
                    droneSn != null ? droneSn : "", swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenDroneOfflineMapEnable] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅飞行器离线地图开关 */
    public void cancelListenDroneOfflineMapEnable(int listenId) {
        try {
            safetyServiceModule.cancelListenDroneOfflineMapEnableWrapper(listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenDroneOfflineMapEnable] error: " + e.getMessage(), e);
        }
    }

}
