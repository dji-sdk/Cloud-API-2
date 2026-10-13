// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FirmwareCompatibilityConfigResult;
import com.dji.flighthubsdk.generated.FirmwareListResult;
import com.dji.flighthubsdk.generated.FirmwareReleaseNoteListResult;
import com.dji.flighthubsdk.generated.FirmwareStatusListResult;
import com.dji.flighthubsdk.generated.FirmwareUpgradeProgressResult;
import com.dji.flighthubsdk.generated.FirmwareUpgradeResult;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IFirmwareManageModule;
import com.dji.flighthubsdk.generated.StringVector;
import com.dji.flighthubsdk.generated.UploadFileResults;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 固件管理模块接口，提供固件查询、升级、上传、发布及兼容性检查等功能。
 */
public class FirmwareManageModule {

    private static final String TAG = "FirmwareManageModule";

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getLatestFirmwareReleaseNoteCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> invokeFirmwareUpgradeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> queryFirmwareUpgradeProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> uploadFirmwareFileCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> releaseFirmwareCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> unReleaseFirmwareCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deleteFirmwareCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> queryFirmwareListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> queryFirmwareCompatibilityConfigCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> checkFirmwareStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> submitFirmwareInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private final IFirmwareManageModule firmwareManageModule;

    public FirmwareManageModule(IFirmwareManageModule firmwareManageModule) {
        this.firmwareManageModule = firmwareManageModule;
    }

    /**
     * 获取最新固件发布说明
     * @param deviceModelKeys 设备型号 key 列表
     * @param language 语言
     * @param callback 回调，返回固件发布说明列表与错误信息
     * @implNote 链路: HTTP | GET /device/api/v1/firmware-release-notes/latest
     */
    public void getLatestFirmwareReleaseNote(java.util.List<String> deviceModelKeys, String language, FlightHubCallback.GetLatestFirmwareReleaseNoteCallback callback) {
        StringVector nativeDeviceModelKeys = null;
        try {
            nativeDeviceModelKeys = FlightHubSdkConvert.toNativeStringVector(deviceModelKeys);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetLatestFirmwareReleaseNoteCallback swigCallback = new com.dji.flighthubsdk.generated.GetLatestFirmwareReleaseNoteCallback() {
                @Override
                public void onGetLatestFirmwareReleaseNote(com.dji.flighthubsdk.generated.FlightHubError err, FirmwareReleaseNoteListResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFirmwareReleaseNoteResultList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[getLatestFirmwareReleaseNote] callback error: " + e.getMessage(), e);
                    } finally {
                        getLatestFirmwareReleaseNoteCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (getLatestFirmwareReleaseNoteCallbackRef) {
                getLatestFirmwareReleaseNoteCallbackRef.put(id, container);
                firmwareManageModule.getLatestFirmwareReleaseNoteWrapper(nativeDeviceModelKeys, language, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getLatestFirmwareReleaseNote] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeDeviceModelKeys != null) {
                nativeDeviceModelKeys.delete();
            }
        }
    }

    /**
     * 触发固件升级
     * @param params 升级参数
     * @param callback 回调，返回升级结果与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/manager/ota/upgrade
     */
    public void invokeFirmwareUpgrade(com.dji.flighthubsdk.bean.FirmwareUpgradeParam params, FlightHubCallback.InvokeFirmwareUpgradeCallback callback) {
        com.dji.flighthubsdk.generated.FirmwareUpgradeParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeFirmwareUpgradeParam(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.InvokeFirmwareUpgradeCallback swigCallback = new com.dji.flighthubsdk.generated.InvokeFirmwareUpgradeCallback() {
                @Override
                public void onInvokeFirmwareUpgrade(com.dji.flighthubsdk.generated.FlightHubError err, FirmwareUpgradeResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFirmwareUpgradeResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[invokeFirmwareUpgrade] callback error: " + e.getMessage(), e);
                    } finally {
                        invokeFirmwareUpgradeCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (invokeFirmwareUpgradeCallbackRef) {
                invokeFirmwareUpgradeCallbackRef.put(id, container);
                firmwareManageModule.invokeFirmwareUpgradeWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[invokeFirmwareUpgrade] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toFirmwareUpgradeResult(null));
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
     * 查询固件升级进度
     * @param deviceSn 设备序列号
     * @param callback 回调，返回升级进度与错误信息
     * @implNote 链路: HTTP | GET /device/api/v1/manager/ota/progress
     */
    public void queryFirmwareUpgradeProgress(String deviceSn, FlightHubCallback.QueryFirmwareUpgradeProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.QueryFirmwareUpgradeProgressCallback swigCallback = new com.dji.flighthubsdk.generated.QueryFirmwareUpgradeProgressCallback() {
                @Override
                public void onQueryFirmwareUpgradeProgress(com.dji.flighthubsdk.generated.FlightHubError err, FirmwareUpgradeProgressResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFirmwareUpgradeProgressItemList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[queryFirmwareUpgradeProgress] callback error: " + e.getMessage(), e);
                    } finally {
                        queryFirmwareUpgradeProgressCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (queryFirmwareUpgradeProgressCallbackRef) {
                queryFirmwareUpgradeProgressCallbackRef.put(id, container);
                firmwareManageModule.queryFirmwareUpgradeProgressWrapper(deviceSn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[queryFirmwareUpgradeProgress] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 上传固件文件
     * @param filePath 本地文件路径
     * @param callback 回调，返回上传结果与错误信息
     * @implNote 链路: SDK 内部 | 获取 STS 凭证后上传固件文件
     */
    public void uploadFirmwareFile(String filePath, FlightHubCallback.UploadFirmwareFileCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UploadFirmwareFileCallback swigCallback = new com.dji.flighthubsdk.generated.UploadFirmwareFileCallback() {
                @Override
                public void onUploadFirmwareFile(com.dji.flighthubsdk.generated.FlightHubError err, UploadFileResults results) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toUploadFileResult(results));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[uploadFirmwareFile] callback error: " + e.getMessage(), e);
                    } finally {
                        uploadFirmwareFileCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (uploadFirmwareFileCallbackRef) {
                uploadFirmwareFileCallbackRef.put(id, container);
                firmwareManageModule.uploadFirmwareFileWrapper(filePath, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[uploadFirmwareFile] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                            FlightHubSdkConvert.toUploadFileResult(null));
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 发布固件
     * @param fileId 固件文件 ID
     * @param callback 回调，返回错误信息
     * @implNote 首次发布时后端会同步拆包并上传小包，接口可能耗时较长；SDK HTTP 超时为 90s
     * @implNote 链路: HTTP | PUT /device/api/v1/manager/firmwares/{id}
     */
    public void releaseFirmware(String fileId, FlightHubCallback.ReleaseFirmwareCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ReleaseFirmwareCallback swigCallback = new com.dji.flighthubsdk.generated.ReleaseFirmwareCallback() {
                @Override
                public void onReleaseFirmware(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[releaseFirmware] callback error: " + e.getMessage(), e);
                    } finally {
                        releaseFirmwareCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (releaseFirmwareCallbackRef) {
                releaseFirmwareCallbackRef.put(id, container);
                firmwareManageModule.releaseFirmwareWrapper(fileId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[releaseFirmware] error: " + e.getMessage(), e);
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
     * 取消发布固件
     * @param fileId 固件文件 ID
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | PUT /device/api/v1/manager/firmwares/{id}
     */
    public void unReleaseFirmware(String fileId, FlightHubCallback.UnReleaseFirmwareCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UnReleaseFirmwareCallback swigCallback = new com.dji.flighthubsdk.generated.UnReleaseFirmwareCallback() {
                @Override
                public void onUnReleaseFirmware(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[unReleaseFirmware] callback error: " + e.getMessage(), e);
                    } finally {
                        unReleaseFirmwareCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (unReleaseFirmwareCallbackRef) {
                unReleaseFirmwareCallbackRef.put(id, container);
                firmwareManageModule.unReleaseFirmwareWrapper(fileId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[unReleaseFirmware] error: " + e.getMessage(), e);
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
     * 删除固件
     * @param fileId 固件文件 ID
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | DELETE /device/api/v1/manager/firmwares/{id}
     */
    public void deleteFirmware(String fileId, FlightHubCallback.DeleteFirmwareCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DeleteFirmwareCallback swigCallback = new com.dji.flighthubsdk.generated.DeleteFirmwareCallback() {
                @Override
                public void onDeleteFirmware(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[deleteFirmware] callback error: " + e.getMessage(), e);
                    } finally {
                        deleteFirmwareCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (deleteFirmwareCallbackRef) {
                deleteFirmwareCallbackRef.put(id, container);
                firmwareManageModule.deleteFirmwareWrapper(fileId, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteFirmware] error: " + e.getMessage(), e);
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
     * 查询固件列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param callback 回调，返回固件列表与错误信息
     * @implNote 链路: HTTP | GET /device/api/v1/manager/firmwares
     */
    public void queryFirmwareList(int page, int pageSize, FlightHubCallback.QueryFirmwareListCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.QueryFirmwareListCallback swigCallback = new com.dji.flighthubsdk.generated.QueryFirmwareListCallback() {
                @Override
                public void onQueryFirmwareList(com.dji.flighthubsdk.generated.FlightHubError err, FirmwareListResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFirmwareItemResultList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[queryFirmwareList] callback error: " + e.getMessage(), e);
                    } finally {
                        queryFirmwareListCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (queryFirmwareListCallbackRef) {
                queryFirmwareListCallbackRef.put(id, container);
                firmwareManageModule.queryFirmwareListWrapper(page, pageSize, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[queryFirmwareList] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 查询固件兼容性配置
     * @param callback 回调，返回兼容性配置与错误信息
     * @implNote 链路: HTTP | GET /device/api/v1/firmware_compatible/config
     */
    public void queryFirmwareCompatibilityConfig(FlightHubCallback.QueryFirmwareCompatibilityConfigCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.QueryFirmwareCompatibilityConfigCallback swigCallback = new com.dji.flighthubsdk.generated.QueryFirmwareCompatibilityConfigCallback() {
                @Override
                public void onQueryFirmwareCompatibilityConfig(com.dji.flighthubsdk.generated.FlightHubError err, FirmwareCompatibilityConfigResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFirmwareCompatibilityConfigItemResultList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[queryFirmwareCompatibilityConfig] callback error: " + e.getMessage(), e);
                    } finally {
                        queryFirmwareCompatibilityConfigCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (queryFirmwareCompatibilityConfigCallbackRef) {
                queryFirmwareCompatibilityConfigCallbackRef.put(id, container);
                firmwareManageModule.queryFirmwareCompatibilityConfigWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[queryFirmwareCompatibilityConfig] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * 检查固件状态
     * @param deviceSns 设备序列号列表
     * @param status 状态列表
     * @param callback 回调，返回固件状态列表与错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/manager/ota/infos
     */
    public void checkFirmwareStatus(java.util.List<String> deviceSns, java.util.List<String> status, FlightHubCallback.CheckFirmwareStatusCallback callback) {
        StringVector nativeDeviceSns = null;
        StringVector nativeStatus = null;
        try {
            nativeDeviceSns = FlightHubSdkConvert.toNativeStringVector(deviceSns);
            nativeStatus = FlightHubSdkConvert.toNativeStringVector(status);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CheckFirmwareStatusCallback swigCallback = new com.dji.flighthubsdk.generated.CheckFirmwareStatusCallback() {
                @Override
                public void onCheckFirmwareStatus(com.dji.flighthubsdk.generated.FlightHubError err, FirmwareStatusListResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFirmwareStatusItemList(result));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[checkFirmwareStatus] callback error: " + e.getMessage(), e);
                    } finally {
                        checkFirmwareStatusCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (checkFirmwareStatusCallbackRef) {
                checkFirmwareStatusCallbackRef.put(id, container);
                firmwareManageModule.checkFirmwareStatusWrapper(nativeDeviceSns, nativeStatus, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[checkFirmwareStatus] error: " + e.getMessage(), e);
            if (callback != null) {
                try {
                    callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), java.util.Collections.emptyList());
                } catch (Exception ex) {
                    Logger.error(TAG, "error callback threw: " + ex.getMessage(), ex);
                }
            }
        } finally {
            if (nativeDeviceSns != null) {
                nativeDeviceSns.delete();
            }
            if (nativeStatus != null) {
                nativeStatus.delete();
            }
        }
    }

    /**
     * 提交固件信息
     * @param firmwareInfo 固件信息参数
     * @param callback 回调，返回错误信息
     * @implNote 链路: HTTP | POST /device/api/v1/manager/firmwares
     */
    public void submitFirmwareInfo(com.dji.flighthubsdk.bean.FirmwareInfoParam firmwareInfo, FlightHubCallback.SubmitFirmwareInfoCallback callback) {
        com.dji.flighthubsdk.generated.FirmwareInfoParam nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeFirmwareInfoParam(firmwareInfo);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SubmitFirmwareInfoCallback swigCallback = new com.dji.flighthubsdk.generated.SubmitFirmwareInfoCallback() {
                @Override
                public void onSubmitFirmwareInfo(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error(TAG, "[submitFirmwareInfo] callback error: " + e.getMessage(), e);
                    } finally {
                        submitFirmwareInfoCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (submitFirmwareInfoCallbackRef) {
                submitFirmwareInfoCallbackRef.put(id, container);
                firmwareManageModule.submitFirmwareInfoWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[submitFirmwareInfo] error: " + e.getMessage(), e);
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

    // ==================== 固件升级进度监听 ====================

    /**
     * 订阅设备固件升级进度
     * @param deviceSn 设备序列号
     * @param callback 升级进度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_device_ota_progress
     */
    public int listenDeviceOtaProgress(String deviceSn, FlightHubCallback.DeviceOtaProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DeviceOtaProgressCallback swigCallback =
                new com.dji.flighthubsdk.generated.DeviceOtaProgressCallback() {
                    @Override
                    public void onDeviceOtaProgress(com.dji.flighthubsdk.generated.DeviceOtaProgress msg) {
                        try {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDeviceOtaProgress(msg));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[listenDeviceOtaProgress] callback error: " + e.getMessage(), e);
                        }
                    }
                };
            int listenId = this.firmwareManageModule.listenDeviceOtaProgressWrapper(deviceSn, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error(TAG, "[listenDeviceOtaProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅设备固件升级进度
     * @param listenId 监听 ID
     */
    public void cancelListenDeviceOtaProgress(int listenId) {
        try {
            this.firmwareManageModule.cancelListenDeviceOtaProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error(TAG, "[cancelListenDeviceOtaProgress] error: " + e.getMessage(), e);
        }
    }
}
