// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IDeviceManageModule;
import com.dji.flighthubsdk.generated.StringVector;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 设备管理模块接口，提供设备列表、详情、改名、解绑及工作空间设备变更监听等功能。
 */
public class DeviceManageModule {

    private static final String TAG = "DeviceManageModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deviceListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getDeviceDetailInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> modifyDeviceNameCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> unbindDeviceCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> dockDeviceStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> droneDeviceStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IDeviceManageModule deviceManageModule;

    public DeviceManageModule(IDeviceManageModule deviceManageModule) {
        this.deviceManageModule = deviceManageModule;
    }

    /**
     * 获取设备列表（含拓扑关系）
     * @param callback 回调，返回设备拓扑列表与错误信息
     * @implNote 链路: HTTP | GET /device/api/v1/manager/device/topologies
     */
    public void getDeviceList(FlightHubCallback.DeviceListCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DeviceListCallback swigCallback = new com.dji.flighthubsdk.generated.DeviceListCallback() {
                @Override
                public void onDeviceList(com.dji.flighthubsdk.generated.DeviceTopoVector devices, com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toDeviceTopoList(devices), FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getDeviceList] callback error: " + e.getMessage(), e);
                    } finally {
                        deviceListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (deviceListCallbackRef) {
                deviceListCallbackRef.put(id, container);
                deviceManageModule.getDeviceListWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getDeviceList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new java.util.ArrayList<>(),
                            new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 获取设备详情
     * @param device_sn 设备序列号
     * @param callback 回调，返回设备详情与错误信息
     * @implNote 链路: HTTP | GET /device/api/v1/manager/device/detail
     */
    public void getDeviceDetailInfo(String device_sn, FlightHubCallback.GetDeviceDetailInfoCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetDeviceDetailInfoCallback swigCallback = new com.dji.flighthubsdk.generated.GetDeviceDetailInfoCallback() {
                @Override
                public void onGetDeviceDetailInfo(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.DeviceDetailInfo device_detail) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceDetailInfo(device_detail));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getDeviceDetailInfo] callback error: " + e.getMessage(), e);
                    } finally {
                        getDeviceDetailInfoCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getDeviceDetailInfoCallbackRef) {
                getDeviceDetailInfoCallbackRef.put(id, container);
                deviceManageModule.getDeviceDetailInfoWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getDeviceDetailInfo] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toDeviceDetailInfo(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 修改设备名称
     * @param device_list 修改参数
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | PUT /device/api/v1/manager/device/info
     */
    public void modifyDeviceName(com.dji.flighthubsdk.bean.ModifyDeviceNameParam device_list, FlightHubCallback.ModifyDeviceNameCallback callback) {
        com.dji.flighthubsdk.generated.ModifyDeviceNameParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeModifyDeviceNameParam(device_list);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ModifyDeviceNameCallback swigCallback = new com.dji.flighthubsdk.generated.ModifyDeviceNameCallback() {
                @Override
                public void onModifyDeviceName(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[modifyDeviceName] callback error: " + e.getMessage(), e);
                    } finally {
                        modifyDeviceNameCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (modifyDeviceNameCallbackRef) {
                modifyDeviceNameCallbackRef.put(id, container);
                deviceManageModule.modifyDeviceNameWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[modifyDeviceName] error: " + e.getMessage(), e);
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
     * 解绑设备
     * @param device_sns 设备序列号列表
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/manager/unbind-devices
     */
    public void unbindDevice(java.util.List<String> device_sns, FlightHubCallback.ResultCallbackWithError callback) {
        StringVector nativeDeviceSns = null;
        try {
            nativeDeviceSns = FlightHubSdkConvert.toNativeStringVector(device_sns);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                    err.getError_code(),
                                    (int) err.getHttp_code(),
                                    (int) err.getServer_code(),
                                    (int) err.getDevice_code(),
                                    err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[unbindDevice] callback error: " + e.getMessage(), e);
                    } finally {
                        unbindDeviceCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (unbindDeviceCallbackRef) {
                unbindDeviceCallbackRef.put(id, container);
                deviceManageModule.unbindDeviceWrapper(nativeDeviceSns, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[unbindDevice] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
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
     * 监听工作空间设备变更
     * @param callback 设备变更事件回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_workspace_device_change
     */
    public int listenWorkspaceDeviceChanged(FlightHubCallback.WorkspaceDeviceChangeCallback callback) {
        try {
            com.dji.flighthubsdk.generated.WorkspaceDeviceChangeCallback swigCallback = new com.dji.flighthubsdk.generated.WorkspaceDeviceChangeCallback() {
                @Override
                public void onWorkspaceDeviceChange(com.dji.flighthubsdk.generated.WorkspaceDeviceChange change) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toWorkspaceDeviceChange(change));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[listenWorkspaceDeviceChanged] callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = deviceManageModule.listenWorkspaceDeviceChangedWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenWorkspaceDeviceChanged] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toWorkspaceDeviceChange(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅工作空间设备变更
     * @param listenId 监听 ID
     */
    public void cancelListenWorkspaceDeviceChanged(int listenId) {
        try {
            deviceManageModule.cancelListenWorkspaceDeviceChangedWrapper(listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenWorkspaceDeviceChanged] error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅设备拓扑变更（工作空间内全部网关）
     * @param callback 拓扑变更事件回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_device_topo_change
     */
    public int listenDeviceTopoChanged(FlightHubCallback.DeviceTopoChangeCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DeviceTopoChangeCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DeviceTopoChangeCallback() {
                        @Override
                        public void onDeviceTopoChange(com.dji.flighthubsdk.generated.DeviceTopoChange change) {
                            try {
                                if (callback != null) {
                                    callback.onResult(FlightHubSdkConvert.toDeviceTopoChange(change));
                                }
                            } catch (Exception e) {
                                Logger.error(TAG, "[listenDeviceTopoChanged] callback error: " + e.getMessage(), e);
                            }
                        }
                    };

            int listenId = deviceManageModule.listenDeviceTopoChangedWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenDeviceTopoChanged] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDeviceTopoChange(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅设备拓扑变更
     * @param listenId 监听 ID
     */
    public void cancelListenDeviceTopoChanged(int listenId) {
        try {
            deviceManageModule.cancelListenDeviceTopoChangedWrapper(listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenDeviceTopoChanged] error: " + e.getMessage(), e);
        }
    }

    /**
     * 获取机场设备状态
     * @param params 设备序列号列表
     * @param callback 回调，返回机场设备状态列表与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/manager/device-properties
     */
    public void getDockDeviceStatus(java.util.List<String> params, FlightHubCallback.DockDeviceStatusListCallback callback) {
        StringVector nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeStringVector(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DockDeviceStatusListCallback swigCallback = new com.dji.flighthubsdk.generated.DockDeviceStatusListCallback() {
                @Override
                public void onDockDeviceStatusList(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.DockDeviceStatusResultList result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDockStatusItemResultList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getDockDeviceStatus] callback error: " + e.getMessage(), e);
                    } finally {
                        dockDeviceStatusCallbackRef.remove(id);
                    }
                }
            };

            synchronized (dockDeviceStatusCallbackRef) {
                dockDeviceStatusCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                deviceManageModule.getDockDeviceStatusWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getDockDeviceStatus] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParams != null) {
                nativeParams.delete();
            }
        }
    }

    /**
     * 获取飞机设备状态
     * @param params 设备序列号列表
     * @param callback 回调，返回飞机设备状态列表与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/manager/device-properties
     */
    public void getDroneDeviceStatus(java.util.List<String> params, FlightHubCallback.DroneDeviceStatusListCallback callback) {
        StringVector nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeStringVector(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DroneDeviceStatusListCallback swigCallback = new com.dji.flighthubsdk.generated.DroneDeviceStatusListCallback() {
                @Override
                public void onDroneDeviceStatusList(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.DroneDeviceStatusResultList result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDroneStatusItemResultList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getDroneDeviceStatus] callback error: " + e.getMessage(), e);
                    } finally {
                        droneDeviceStatusCallbackRef.remove(id);
                    }
                }
            };

            synchronized (droneDeviceStatusCallbackRef) {
                droneDeviceStatusCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                deviceManageModule.getDroneDeviceStatusWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getDroneDeviceStatus] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeParams != null) {
                nativeParams.delete();
            }
        }
    }
}
