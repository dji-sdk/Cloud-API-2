// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 设备调试模块接口，提供机场/飞行器的开关机、舱盖、充电、格式化、重启、网络测试、RTK 标定、中继站调试等功能。
 */
public class DeviceDebugModule {

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
    
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> openDebugModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> closeDebugModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> openDockCoverCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> openDockCoverProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> closeDockCoverCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> closeDockCoverProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deviceRebootCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deviceRebootProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deviceFormatCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deviceFormatProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> openDroneCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> closeDroneCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> openChargeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> closeChargeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setDockAirConditionerModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setDockSilentModeModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setDistanceLimitCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setHeightLimitCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setObstacleAvoidanceCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRemainingPowerForRthCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setNightLightsStateCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> switchAlarmLightStateCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> esimOperatorSwitchCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> simSlotSwitchCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setBatteryStoreModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> manualBatterySwapCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> manualBatterySwapProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> donglePowerSwitchCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> lowPowerSwitchCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> esimOperatorSwitchProgressCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setGeoCagingEnableCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> droneFormatCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> droneSdrWorkModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> cloudEnvironmentSwitchCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> cloudEnvironmentSwitchCallbackCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IDeviceDebugModule deviceDebugModule;

    /**
     * 构造设备调试模块
     * @param deviceDebugModule 底层设备调试模块接口
     */
    public DeviceDebugModule(IDeviceDebugModule deviceDebugModule) {
        this.deviceDebugModule = deviceDebugModule;
    }

    /**
     * 打开调试模式
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和打开调试模式结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/debug-mode-open
     */
    public void openDebugMode(String device_sn, FlightHubCallback.OpenDebugModeCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.OpenDebugModeCallback swigCallback = new com.dji.flighthubsdk.generated.OpenDebugModeCallback() {
                @Override
                public void onOpenDebugMode(com.dji.flighthubsdk.generated.FlightHubError err, OpenDebugModeResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toOpenDebugModeResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[openDebugMode] open debug mode callback error: " + e.getMessage(), e);
                    } finally {
                        openDebugModeCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (openDebugModeCallbackRef) {
                openDebugModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.openDebugModeWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[openDebugMode] open debug mode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toOpenDebugModeResult(null));
            }
        }
    }

    /**
     * 关闭调试模式
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和关闭调试模式结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/debug-mode-close
     */
    public void closeDebugMode(String device_sn, FlightHubCallback.CloseDebugModeCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CloseDebugModeCallback swigCallback = new com.dji.flighthubsdk.generated.CloseDebugModeCallback() {
                @Override
                public void onCloseDebugMode(com.dji.flighthubsdk.generated.FlightHubError err, CloseDebugModeResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCloseDebugModeResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[closeDebugMode] close debug mode callback error: " + e.getMessage(), e);
                    } finally {
                        closeDebugModeCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (closeDebugModeCallbackRef) {
                closeDebugModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.closeDebugModeWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[closeDebugMode] close debug mode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toCloseDebugModeResult(null));
            }
        }
    }
    
    /**
     * 打开机场舱盖
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和打开舱盖结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/cover-open
     */
    public void openDockCover(String device_sn, FlightHubCallback.OpenDockCoverCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.OpenDockCoverCallback swigCallback = new com.dji.flighthubsdk.generated.OpenDockCoverCallback() {
                @Override
                public void onOpenDockCover(com.dji.flighthubsdk.generated.FlightHubError err, OpenDockCoverResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toOpenDockCoverResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[openDockCover] open dock cover callback error: " + e.getMessage(), e);
                    } finally {
                        openDockCoverCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (openDockCoverCallbackRef) {
                openDockCoverCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.openDockCoverWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[openDockCover] open dock cover error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toOpenDockCoverResult(null));
            }
        }
    }

    /**
     * 获取打开机场舱盖进度
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和打开舱盖进度结果
     * @implNote 链路: HTTP | HTTP GET /device/api/v1/service/cover-open
     */
    public void getOpenDockCoverProgress(String device_sn, FlightHubCallback.OpenDockCoverProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.OpenDockCoverProgressCallback swigCallback = new com.dji.flighthubsdk.generated.OpenDockCoverProgressCallback() {
                @Override
                public void onOpenDockCoverProgress(com.dji.flighthubsdk.generated.FlightHubError err, OpenDockCoverProgressResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toOpenDockCoverProgressResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[openDockCoverProgress] open dock cover progress callback error: " + e.getMessage(), e);
                    } finally {
                        openDockCoverProgressCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (openDockCoverProgressCallbackRef) {
                openDockCoverProgressCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.openDockCoverProgressWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[openDockCoverProgress] open dock cover progress error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toOpenDockCoverProgressResult(null));
            }
        }
    }

    /**
     * 关闭机场舱盖
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和关闭舱盖结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/cover-close
     */
    public void closeDockCover(String device_sn, FlightHubCallback.CloseDockCoverCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CloseDockCoverCallback swigCallback = new com.dji.flighthubsdk.generated.CloseDockCoverCallback() {
                @Override
                public void onCloseDockCover(com.dji.flighthubsdk.generated.FlightHubError err, CloseDockCoverResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCloseDockCoverResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[closeDockCover] close dock cover callback error: " + e.getMessage(), e);
                    } finally {
                        closeDockCoverCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (closeDockCoverCallbackRef) {
                closeDockCoverCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.closeDockCoverWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[closeDockCover] close dock cover error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toCloseDockCoverResult(null));
            }
        }
    }

    /**
     * 获取关闭机场舱盖进度
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和关闭舱盖进度结果
     * @implNote 链路: HTTP | HTTP GET /device/api/v1/service/cover-close
     */
    public void getCloseDockCoverProgress(String device_sn, FlightHubCallback.CloseDockCoverProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CloseDockCoverProgressCallback swigCallback = new com.dji.flighthubsdk.generated.CloseDockCoverProgressCallback() {
                @Override
                public void onCloseDockCoverProgress(com.dji.flighthubsdk.generated.FlightHubError err, CloseDockCoverProgressResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCloseDockCoverProgressResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[onCloseDockCoverProgress] close dock cover progress callback error: " + e.getMessage(), e);
                    } finally {
                        closeDockCoverProgressCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (closeDockCoverProgressCallbackRef) {
                closeDockCoverProgressCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.closeDockCoverProgressWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[closeDockCoverProgress] close dock cover progress error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toCloseDockCoverProgressResult(null));
            }
        }
    }

    /**
     * 设备重启
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和设备重启结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/device-reboot
     */
    public void deviceReboot(String device_sn, FlightHubCallback.DeviceRebootCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DeviceRebootCallback swigCallback = new com.dji.flighthubsdk.generated.DeviceRebootCallback() {
                @Override
                public void onDeviceReboot(com.dji.flighthubsdk.generated.FlightHubError err, DeviceRebootResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceRebootResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[deviceReboot] device reboot callback error: " + e.getMessage(), e);
                    } finally {
                        deviceRebootCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (deviceRebootCallbackRef) {
                deviceRebootCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.deviceRebootWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[deviceReboot] device reboot error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toDeviceRebootResult(null));
            }
        }
    }

    /**
     * 获取设备重启进度
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和设备重启进度结果
     * @implNote 链路: HTTP | HTTP GET /device/api/v1/service/device-reboot
     */
    public void getDeviceRebootProgress(String device_sn, FlightHubCallback.DeviceRebootProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DeviceRebootProgressCallback swigCallback = new com.dji.flighthubsdk.generated.DeviceRebootProgressCallback() {
                @Override
                public void onDeviceRebootProgress(com.dji.flighthubsdk.generated.FlightHubError err, DeviceRebootProgressResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceRebootProgressResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[getDeviceRebootProgress] device reboot progress callback error: " + e.getMessage(), e);
                    } finally {
                        deviceRebootProgressCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (deviceRebootProgressCallbackRef) {
                deviceRebootProgressCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.deviceRebootProgressWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[getDeviceRebootProgress] device reboot progress error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toDeviceRebootProgressResult(null));
            }
        }
    }

    /**
     * 设备格式化
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和设备格式化结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/device-format
     */
    public void deviceFormat(String device_sn, FlightHubCallback.DeviceFormatCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DeviceFormatCallback swigCallback = new com.dji.flighthubsdk.generated.DeviceFormatCallback() {
                @Override
                public void onDeviceFormat(com.dji.flighthubsdk.generated.FlightHubError err, DeviceFormatResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceFormatResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[deviceFormat] device format callback error: " + e.getMessage(), e);
                    } finally {
                        deviceFormatCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (deviceFormatCallbackRef) {
                deviceFormatCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.deviceFormatWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[deviceFormat] device format error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toDeviceFormatResult(null));
            }
        }
    }

    /**
     * 获取设备格式化进度
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和设备格式化进度结果
     * @implNote 链路: HTTP | HTTP GET /device/api/v1/service/device-format
     */
    public void getDeviceFormatProgress(String device_sn, FlightHubCallback.DeviceFormatProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DeviceFormatProgressCallback swigCallback = new com.dji.flighthubsdk.generated.DeviceFormatProgressCallback() {
                @Override
                public void onDeviceFormatProgress(com.dji.flighthubsdk.generated.FlightHubError err, DeviceFormatProgressResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceFormatProgressResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[deviceFormat] device format progress callback error: " + e.getMessage(), e);
                    } finally {
                        deviceFormatProgressCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (deviceFormatProgressCallbackRef) {
                deviceFormatProgressCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.deviceFormatProgressWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[getDeviceFormatProgress] device format progress error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toDeviceFormatProgressResult(null));
            }
        }
    }

    /**
     * 飞行器开机
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和飞行器开机结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/drone-open
     */
    public void openDrone(String device_sn, FlightHubCallback.OpenDroneCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.OpenDroneCallback swigCallback = new com.dji.flighthubsdk.generated.OpenDroneCallback() {
                @Override
                public void onOpenDrone(com.dji.flighthubsdk.generated.FlightHubError err, OpenDroneResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toOpenDroneResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[openDrone] open drone callback error: " + e.getMessage(), e);
                    } finally {
                        openDroneCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (openDroneCallbackRef) {
                openDroneCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.openDroneWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[openDrone] open drone error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toOpenDroneResult(null));
            }
        }
    }

    /**
     * 飞行器关机
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和飞行器关机结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/drone-close
     */
    public void closeDrone(String device_sn, FlightHubCallback.CloseDroneCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CloseDroneCallback swigCallback = new com.dji.flighthubsdk.generated.CloseDroneCallback() {
                @Override
                public void onCloseDrone(com.dji.flighthubsdk.generated.FlightHubError err, CloseDroneResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCloseDroneResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[closeDrone] close drone callback error: " + e.getMessage(), e);
                    } finally {
                        closeDroneCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (closeDroneCallbackRef) {
                closeDroneCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.closeDroneWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[closeDrone] close drone error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toCloseDroneResult(null));
            }
        }
    }

    /**
     * 打开充电
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和打开充电结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/charge-open
     */
    public void openCharge(String device_sn, FlightHubCallback.OpenChargeCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.OpenChargeCallback swigCallback = new com.dji.flighthubsdk.generated.OpenChargeCallback() {
                @Override
                public void onOpenCharge(com.dji.flighthubsdk.generated.FlightHubError err, OpenChargeResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toOpenChargeResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[openCharge] open charge callback error: " + e.getMessage(), e);
                    } finally {
                        openChargeCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (openChargeCallbackRef) {
                openChargeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.openChargeWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[openCharge] open charge error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toOpenChargeResult(null));
            }
        }
    }

    /**
     * 关闭充电
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和关闭充电结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/charge-close
     */
    public void closeCharge(String device_sn, FlightHubCallback.CloseChargeCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CloseChargeCallback swigCallback = new com.dji.flighthubsdk.generated.CloseChargeCallback() {
                @Override
                public void onCloseCharge(com.dji.flighthubsdk.generated.FlightHubError err, CloseChargeResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCloseChargeResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[closeCharge] close charge callback error: " + e.getMessage(), e);
                    } finally {
                        closeChargeCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (closeChargeCallbackRef) {
                closeChargeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.closeChargeWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[closeCharge] close charge error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toCloseChargeResult(null));
            }
        }
    }

    /**
     * 设置机场空调模式
     * @param device_sn 设备序列号
     * @param mode 空调模式。枚举 DockAirConditionerState（value.h）：0=空闲, 1=制冷, 2=制热, 3=除湿
     * @param callback 结果回调，返回错误信息
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/air-conditioners
     */
    public void setDockAirConditionerMode(String device_sn, int mode, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setDockAirConditionerMode] set dock air conditioner mode callback error: " + e.getMessage(), e);
                    } finally {
                        setDockAirConditionerModeCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (setDockAirConditionerModeCallbackRef) {
                setDockAirConditionerModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setDockAirConditionerModeWrapper(device_sn, mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setDockAirConditionerMode] set dock air conditioner mode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置机场静音模式
     * @param device_sn 设备序列号
     * @param mode 静音模式
     * @param callback 结果回调，返回错误信息
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/silent-mode
     */
    public void setDockSilentMode(String device_sn, int mode, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setDockSilentMode] set dock silent mode callback error: " + e.getMessage(), e);
                    } finally {
                        setDockSilentModeModeCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (setDockSilentModeModeCallbackRef) {
                setDockSilentModeModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setDockSilentModeWrapper(device_sn, mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setDockSilentMode] set dock silent mode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置限远距离
     * @param device_sn 设备序列号
     * @param distance_limit_state 限远状态
     * @param distance_limit_value 限远距离值
     * @param callback 结果回调，返回错误信息和设置限远结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/distance-limit
     */
    public void setDistanceLimit(String device_sn, int distance_limit_state, int distance_limit_value, FlightHubCallback.SetDistanceLimitCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetDistanceLimitCallback swigCallback = new com.dji.flighthubsdk.generated.SetDistanceLimitCallback() {
                @Override
                public void onSetDistanceLimit(com.dji.flighthubsdk.generated.FlightHubError err, SetDistanceLimitResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSetDistanceLimitResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setDistanceLimit] set distance limit callback error: " + e.getMessage(), e);
                    } finally {
                        setDistanceLimitCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setDistanceLimitCallbackRef) {
                setDistanceLimitCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setDistanceLimitWrapper(device_sn, distance_limit_state, distance_limit_value, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setDistanceLimit] set distance limit error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSetDistanceLimitResult(null));
            }
        }
    }

    /**
     * 设置限高
     * @param device_sn 设备序列号
     * @param height_limit 限高值
     * @param callback 结果回调，返回错误信息和设置限高结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/height-limit
     */
    public void setHeightLimit(String device_sn, int height_limit, FlightHubCallback.SetHeightLimitCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetHeightLimitCallback swigCallback = new com.dji.flighthubsdk.generated.SetHeightLimitCallback() {
                @Override
                public void onSetHeightLimit(com.dji.flighthubsdk.generated.FlightHubError err, SetHeightLimitResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSetHeightLimitResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setHeightLimit] set height limit callback error: " + e.getMessage(), e);
                    } finally {
                        setHeightLimitCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setHeightLimitCallbackRef) {
                setHeightLimitCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setHeightLimitWrapper(device_sn, height_limit, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setHeightLimit] set height limit error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSetHeightLimitResult(null));
            }
        }
    }

    /**
     * 设置避障状态
     * @param device_sn 设备序列号
     * @param obstacle_avoidance_state 避障状态
     * @param callback 结果回调，返回错误信息和设置避障结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/obstacle-avoidance
     */
    public void setObstacleAvoidance(String device_sn, int obstacle_avoidance_state, FlightHubCallback.SetObstacleAvoidanceCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetObstacleAvoidanceCallback swigCallback = new com.dji.flighthubsdk.generated.SetObstacleAvoidanceCallback() {
                @Override
                public void onSetObstacleAvoidance(com.dji.flighthubsdk.generated.FlightHubError err, SetObstacleAvoidanceResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSetObstacleAvoidanceResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setObstacleAvoidance] set obstacle avoidance callback error: " + e.getMessage(), e);
                    } finally {
                        setObstacleAvoidanceCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setObstacleAvoidanceCallbackRef) {
                setObstacleAvoidanceCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setObstacleAvoidanceWrapper(device_sn, obstacle_avoidance_state, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setObstacleAvoidance] set obstacle avoidance error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSetObstacleAvoidanceResult(null));
            }
        }
    }

    /**
     * 设置返航剩余电量
     * @param device_sn 设备序列号
     * @param remaining_power_for_rth 返航剩余电量百分比
     * @param callback 结果回调，返回错误信息和设置返航电量结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/remaining-power-for-rth
     */
    public void setRemainingPowerForRth(String device_sn, int remaining_power_for_rth, FlightHubCallback.SetRemainingPowerForRthCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetRemainingPowerForRthCallback swigCallback = new com.dji.flighthubsdk.generated.SetRemainingPowerForRthCallback() {
                @Override
                public void onSetRemainingPowerForRth(com.dji.flighthubsdk.generated.FlightHubError err, SetRemainingPowerForRthResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSetRemainingPowerForRthResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setRemainingPowerForRth] set remaining power for rth callback error: " + e.getMessage(), e);
                    } finally {
                        setRemainingPowerForRthCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setRemainingPowerForRthCallbackRef) {
                setRemainingPowerForRthCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setRemainingPowerForRthWrapper(device_sn, remaining_power_for_rth, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setRemainingPowerForRth] set remaining power for rth error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSetRemainingPowerForRthResult(null));
            }
        }
    }

    /**
     * 设置夜航灯状态
     * @param device_sn 设备序列号
     * @param night_lights_state 夜航灯状态
     * @param callback 结果回调，返回错误信息和设置夜航灯结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/night-lights-state
     */
    public void setNightLightsState(String device_sn, int night_lights_state, FlightHubCallback.SetNightLightsStateCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetNightLightsStateCallback swigCallback = new com.dji.flighthubsdk.generated.SetNightLightsStateCallback() {
                @Override
                public void onSetNightLightsState(com.dji.flighthubsdk.generated.FlightHubError err, SetNightLightsStateResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSetNightLightsStateResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setNightLightsState] set night lights state callback error: " + e.getMessage(), e);
                    } finally {
                        setNightLightsStateCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setNightLightsStateCallbackRef) {
                setNightLightsStateCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setNightLightsStateWrapper(device_sn, night_lights_state, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setNightLightsState] set night lights state error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSetNightLightsStateResult(null));
            }
        }
    }

    /**
     * 切换报警灯状态
     * @param device_sn 设备序列号
     * @param alarm_state 报警灯状态
     * @param callback 结果回调，返回错误信息和切换报警灯结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/alarm-state-switch
     */
    public void switchAlarmLightState(String device_sn, int alarm_state, FlightHubCallback.SwitchAlarmLightStateCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SwitchAlarmStateCallback swigCallback = new com.dji.flighthubsdk.generated.SwitchAlarmStateCallback() {
                @Override
                public void onSwitchAlarmState(com.dji.flighthubsdk.generated.FlightHubError err, SwitchAlarmStateResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSwitchAlarmStateResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[switchAlarmLightState] switch alarm light state callback error: " + e.getMessage(), e);
                    } finally {
                        switchAlarmLightStateCallbackRef.remove(id);
                    }
                }
            };
            synchronized (switchAlarmLightStateCallbackRef) {
                switchAlarmLightStateCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.switchAlarmLightStateWrapper(device_sn, alarm_state, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[switchAlarmLightState] switch alarm light state error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSwitchAlarmStateResult(null));
            }
        }
    }

    /**
     * eSIM 运营商切换
     * @param device_sn 设备序列号
     * @param imei 设备 IMEI
     * @param operator_type 运营商类型
     * @param device_type 设备类型
     * @param callback 结果回调，返回错误信息和 eSIM 运营商切换结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/esim-operator-switch
     */
    public void esimOperatorSwitch(String device_sn, String imei, int operator_type, String device_type, FlightHubCallback.EsimOperatorSwitchCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.EsimOperatorSwitchCallback swigCallback = new com.dji.flighthubsdk.generated.EsimOperatorSwitchCallback() {
                @Override
                public void onEsimOperatorSwitch(com.dji.flighthubsdk.generated.FlightHubError err, EsimOperatorSwitchResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toEsimOperatorSwitchResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[esimOperatorSwitch] esim operator switch callback error: " + e.getMessage(), e);
                    } finally {
                        esimOperatorSwitchCallbackRef.remove(id);
                    }
                }
            };
            synchronized (esimOperatorSwitchCallbackRef) {
                esimOperatorSwitchCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.esimOperatorSwitchWrapper(device_sn, imei, operator_type, device_type, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[esimOperatorSwitch] esim operator switch error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toEsimOperatorSwitchResult(null));
            }
        }
    }

    /**
     * SIM 卡槽切换
     * @param device_sn 设备序列号
     * @param imei 设备 IMEI
     * @param sim_slot SIM 卡槽
     * @param device_type 设备类型
     * @param callback 结果回调，返回错误信息和 SIM 卡槽切换结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/sim-slot-switch
     */
    public void simSlotSwitch(String device_sn, String imei, int sim_slot, String device_type, FlightHubCallback.SimSlotSwitchCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SimSlotSwitchCallback swigCallback = new com.dji.flighthubsdk.generated.SimSlotSwitchCallback() {
                @Override
                public void onSimSlotSwitch(com.dji.flighthubsdk.generated.FlightHubError err, SimSlotSwitchResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSimSlotSwitchResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[simSlotSwitch] sim slot switch callback error: " + e.getMessage(), e);
                    } finally {
                        simSlotSwitchCallbackRef.remove(id);
                    }
                }
            };
            synchronized (simSlotSwitchCallbackRef) {
                simSlotSwitchCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.simSlotSwitchWrapper(device_sn, imei, sim_slot, device_type, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[simSlotSwitch] sim slot switch error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSimSlotSwitchResult(null));
            }
        }
    }

    /**
     * 设置电池存储模式
     * @param device_sn 设备序列号
     * @param battery_store_mode 电池存储模式
     * @param callback 结果回调，返回错误信息和设置电池存储模式结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/battery-store-mode-switch
     */
    public void setBatteryStoreMode(String device_sn, int battery_store_mode, FlightHubCallback.SetBatteryStoreModeCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetBatteryStoreModeCallback swigCallback = new com.dji.flighthubsdk.generated.SetBatteryStoreModeCallback() {
                @Override
                public void onSetBatteryStoreMode(com.dji.flighthubsdk.generated.FlightHubError err, SetBatteryStoreModeResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toSetBatteryStoreModeResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setBatteryStoreMode] set battery store mode callback error: " + e.getMessage(), e);
                    } finally {
                        setBatteryStoreModeCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setBatteryStoreModeCallbackRef) {
                setBatteryStoreModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setBatteryStoreModeWrapper(device_sn, battery_store_mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setBatteryStoreMode] set battery store mode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toSetBatteryStoreModeResult(null));
            }
        }
    }


    /**
     * 发起手动换电
     * @param device_sn 设备序列号（机场 SN）
     * @param slot_id 目标电池卡槽 ID
     * @param callback 结果回调，返回错误信息和换电发起结果
     * @implNote 链路: HTTP | HTTP PUT /device/api/v1/service/manual-battery-swap
     */
    public void startManualBatterySwap(String device_sn, int slot_id, FlightHubCallback.ManualBatterySwapCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ManualBatterySwapCallback swigCallback = new com.dji.flighthubsdk.generated.ManualBatterySwapCallback() {
                @Override
                public void onManualBatterySwap(com.dji.flighthubsdk.generated.FlightHubError err, ManualBatterySwapResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toManualBatterySwapResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[startManualBatterySwap] callback error: " + e.getMessage(), e);
                    } finally {
                        manualBatterySwapCallbackRef.remove(id);
                    }
                }
            };
            synchronized (manualBatterySwapCallbackRef) {
                manualBatterySwapCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.startManualBatterySwapWrapper(device_sn, slot_id, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[startManualBatterySwap] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toManualBatterySwapResult(null));
            }
        }
    }


    /**
     * 查询手动换电进度
     * @param device_sn 设备序列号（机场 SN，query 键为 sn）
     * @param callback 结果回调，返回错误信息和换电进度结果
     * @implNote 链路: HTTP | HTTP GET /device/api/v1/service/manual-battery-swap?sn={device_sn}
     */
    public void getManualBatterySwapProgress(String device_sn, FlightHubCallback.ManualBatterySwapProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ManualBatterySwapProgressCallback swigCallback = new com.dji.flighthubsdk.generated.ManualBatterySwapProgressCallback() {
                @Override
                public void onManualBatterySwapProgress(com.dji.flighthubsdk.generated.FlightHubError err, ManualBatterySwapProgressResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toManualBatterySwapProgressResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[getManualBatterySwapProgress] callback error: " + e.getMessage(), e);
                    } finally {
                        manualBatterySwapProgressCallbackRef.remove(id);
                    }
                }
            };
            synchronized (manualBatterySwapProgressCallbackRef) {
                manualBatterySwapProgressCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.getManualBatterySwapProgressWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[getManualBatterySwapProgress] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toManualBatterySwapProgressResult(null));
            }
        }
    }


    /**
     * Dongle 电源开关
     * @param device_sn 设备序列号（飞行器 SN，非机场 SN；飞机需已安装 Dongle）
     * @param power_state 电源状态，0=关，1=开
     *                    注意：底层 JSON 字段名为 {@code action}（服务端 API 要求），
     *                    原始请求体为 {@code {"device_sn":"...","data":{"action":0/1}}}
     * @param callback 结果回调，返回错误信息和 Dongle 电源开关结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/dongle-power-switch
     */
    public void setDonglePowerSwitch(String device_sn, int power_state, FlightHubCallback.DonglePowerSwitchCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DonglePowerSwitchCallback swigCallback = new com.dji.flighthubsdk.generated.DonglePowerSwitchCallback() {
                @Override
                public void onDonglePowerSwitch(com.dji.flighthubsdk.generated.FlightHubError err, DonglePowerSwitchResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDonglePowerSwitchResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setDonglePowerSwitch] callback error: " + e.getMessage(), e);
                    } finally {
                        donglePowerSwitchCallbackRef.remove(id);
                    }
                }
            };
            synchronized (donglePowerSwitchCallbackRef) {
                donglePowerSwitchCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setDonglePowerSwitchWrapper(device_sn, power_state, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setDonglePowerSwitch] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toDonglePowerSwitchResult(null));
            }
        }
    }


    /**
     * 机场低功耗开关
     * @param device_sn 设备序列号
     * @param low_power_mode 低功耗模式，0=关，1=开
     * @param callback 结果回调，返回错误信息和低功耗开关结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/low-power-switch
     */
    public void setLowPowerSwitch(String device_sn, int low_power_mode, FlightHubCallback.LowPowerSwitchCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.LowPowerSwitchCallback swigCallback = new com.dji.flighthubsdk.generated.LowPowerSwitchCallback() {
                @Override
                public void onLowPowerSwitch(com.dji.flighthubsdk.generated.FlightHubError err, LowPowerSwitchResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toLowPowerSwitchResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[setLowPowerSwitch] callback error: " + e.getMessage(), e);
                    } finally {
                        lowPowerSwitchCallbackRef.remove(id);
                    }
                }
            };
            synchronized (lowPowerSwitchCallbackRef) {
                lowPowerSwitchCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setLowPowerSwitchWrapper(device_sn, low_power_mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setLowPowerSwitch] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toLowPowerSwitchResult(null));
            }
        }
    }


    /**
     * 查询 eSIM 运营商切换进度
     * @param device_sn 设备序列号（query 键为 sn）
     * @param callback 结果回调，返回错误信息和切换进度
     * @implNote 链路: HTTP | HTTP GET /device/api/v1/service/esim-operator-switch?sn={device_sn}
     */
    public void getEsimOperatorSwitchProgress(String device_sn, FlightHubCallback.EsimOperatorSwitchProgressCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.EsimOperatorSwitchProgressCallback swigCallback = new com.dji.flighthubsdk.generated.EsimOperatorSwitchProgressCallback() {
                @Override
                public void onEsimOperatorSwitchProgress(com.dji.flighthubsdk.generated.FlightHubError err, EsimOperatorSwitchProgressResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toEsimOperatorSwitchProgressResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[getEsimOperatorSwitchProgress] callback error: " + e.getMessage(), e);
                    } finally {
                        esimOperatorSwitchProgressCallbackRef.remove(id);
                    }
                }
            };
            synchronized (esimOperatorSwitchProgressCallbackRef) {
                esimOperatorSwitchProgressCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.getEsimOperatorSwitchProgressWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[getEsimOperatorSwitchProgress] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toEsimOperatorSwitchProgressResult(null));
            }
        }
    }


    /**
     * 设置 Geo Caging（地理围栏）开关及围栏参数
     * @param device_sn 设备序列号
     * @param geoCagingSwitch Geo Caging 开关状态，1=开启，0=关闭
     * @param geoCagingInfos 围栏信息列表，每个元素包含飞行围栏和自定义围栏参数
     * @param callback 结果回调，返回错误信息
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/geo-caging-enable
     */
    public void setGeoCagingEnable(String device_sn, int geoCagingSwitch, java.util.List<com.dji.flighthubsdk.bean.GeoCagingInfoParam> geoCagingInfos, FlightHubCallback.SetGeoCagingEnableCallback callback) {
        com.dji.flighthubsdk.generated.GeoCagingInfoParamVector nativeInfos = null;
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.SetGeoCagingEnableCallback swigCallback = new com.dji.flighthubsdk.generated.SetGeoCagingEnableCallback() {
                @Override
                public void onSetGeoCagingEnable(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) { callback.onResult(FlightHubSdkConvert.toFlightHubError(err)); }
                    } catch (Exception e) { Logger.error("DeviceDebugModule", "[setGeoCagingEnable] callback error: " + e.getMessage(), e);
                    } finally {
                        setGeoCagingEnableCallbackRef.remove(id);
                    }
                }
            };
            nativeInfos = FlightHubSdkConvert.toNativeGeoCagingInfoParamVector(geoCagingInfos);
            synchronized (setGeoCagingEnableCallbackRef) {
                setGeoCagingEnableCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.setGeoCagingEnableWrapper(device_sn, geoCagingSwitch, nativeInfos, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[setGeoCagingEnable] error: " + e.getMessage(), e);
            if (callback != null) { callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED)); }
        } finally {
            if (nativeInfos != null) {
                nativeInfos.delete();
            }
        }
    }

    /**
     * 订阅机场舱盖打开进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回打开舱盖结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_dock_cover_open
     */
    public int subscribeDockCoverOpenProgress(String device_sn, FlightHubCallback.SubscribeDockCoverOpenProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDockCoverOpenProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDockCoverOpenProgressCallback() {
                @Override
                public void onSubscribeDockCoverOpenProgress(OpenDockCoverResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toOpenDockCoverResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDockCoverOpenProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDockCoverOpenProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDockCoverOpenProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅机场舱盖打开进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDockCoverOpenProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDockCoverOpenProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDockCoverOpenProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅机场舱盖关闭进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回关闭舱盖结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_dock_cover_close
     */
    public int subscribeDockCoverCloseProgress(String device_sn, FlightHubCallback.SubscribeDockCoverCloseProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDockCoverCloseProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDockCoverCloseProgressCallback() {
                @Override
                public void onSubscribeDockCoverCloseProgress(CloseDockCoverResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCloseDockCoverResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDockCoverCloseProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDockCoverCloseProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDockCoverCloseProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅机场舱盖关闭进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDockCoverCloseProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDockCoverCloseProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDockCoverCloseProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅设备重启进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回设备重启结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_device_reboot
     */
    public int subscribeDeviceRebootProgress(String device_sn, FlightHubCallback.SubscribeDeviceRebootProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDeviceRebootProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDeviceRebootProgressCallback() {
                @Override
                public void onSubscribeDeviceRebootProgress(DeviceRebootResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toDeviceRebootResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDeviceRebootProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDeviceRebootProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDeviceRebootProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅设备重启进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDeviceRebootProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDeviceRebootProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDeviceRebootProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅设备格式化进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回设备格式化结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_device_format
     */
    public int subscribeDeviceFormatProgress(String device_sn, FlightHubCallback.SubscribeDeviceFormatProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDeviceFormatProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDeviceFormatProgressCallback() {
                @Override
                public void onSubscribeDeviceFormatProgress(DeviceFormatResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toDeviceFormatResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDeviceFormatProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDeviceFormatProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDeviceFormatProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅设备格式化进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDeviceFormatProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDeviceFormatProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDeviceFormatProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞行器开机进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回飞行器开机结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_open
     */
    public int subscribeDroneOpenProgress(String device_sn, FlightHubCallback.SubscribeDroneOpenProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDroneOpenProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDroneOpenProgressCallback() {
                @Override
                public void onSubscribeDroneOpenProgress(OpenDroneResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toOpenDroneResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDroneOpenProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDroneOpenProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDroneOpenProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅飞行器开机进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDroneOpenProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDroneOpenProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDroneOpenProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞行器关机进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回飞行器关机结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_close
     */
    public int subscribeDroneCloseProgress(String device_sn, FlightHubCallback.SubscribeDroneCloseProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDroneCloseProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDroneCloseProgressCallback() {
                @Override
                public void onSubscribeDroneCloseProgress(CloseDroneResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCloseDroneResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDroneCloseProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDroneCloseProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDroneCloseProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅飞行器关机进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDroneCloseProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDroneCloseProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDroneCloseProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞行器格式化进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回飞行器格式化结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_format
     */
    public int subscribeDroneFormatProgress(String device_sn, FlightHubCallback.SubscribeDroneFormatProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDroneFormatProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDroneFormatProgressCallback() {
                @Override
                public void onSubscribeDroneFormatProgress(DroneFormatResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toDroneFormatResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDroneFormatProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDroneFormatProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDroneFormatProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅飞行器格式化进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDroneFormatProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDroneFormatProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDroneFormatProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅打开充电进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回打开充电结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_charge_open
     */
    public int subscribeDroneChargeOpenProgress(String device_sn, FlightHubCallback.SubscribeDroneChargeOpenProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDroneChargeOpenProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDroneChargeOpenProgressCallback() {
                @Override
                public void onSubscribeDroneChargeOpenProgress(OpenChargeResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toOpenChargeResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDroneChargeOpenProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDroneChargeOpenProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDroneChargeOpenProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅打开充电进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDroneChargeOpenProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDroneChargeOpenProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDroneChargeOpenProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅关闭充电进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回关闭充电结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_charge_close
     */
    public int subscribeDroneChargeCloseProgress(String device_sn, FlightHubCallback.SubscribeDroneChargeCloseProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDroneChargeCloseProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDroneChargeCloseProgressCallback() {
                @Override
                public void onSubscribeDroneChargeCloseProgress(CloseChargeResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCloseChargeResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDroneChargeCloseProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDroneChargeCloseProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDroneChargeCloseProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅关闭充电进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDroneChargeCloseProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDroneChargeCloseProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDroneChargeCloseProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 eSIM 运营商切换进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回 eSIM 运营商切换结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_device_esim_operator_switch
     */
    public int subscribeDeviceEsimOperatorSwitchProgress(String device_sn, FlightHubCallback.SubscribeDeviceEsimOperatorSwitchProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDeviceEsimOperatorSwitchProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDeviceEsimOperatorSwitchProgressCallback() {
                @Override
                public void onSubscribeDeviceEsimOperatorSwitchProgress(EsimOperatorSwitchResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toEsimOperatorSwitchResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDeviceEsimOperatorSwitchProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDeviceEsimOperatorSwitchProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDeviceEsimOperatorSwitchProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 eSIM 运营商切换进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDeviceEsimOperatorSwitchProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDeviceEsimOperatorSwitchProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDeviceEsimOperatorSwitchProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 SIM 卡槽切换进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回 SIM 卡槽切换结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_device_sim_slot_switch
     */
    public int subscribeDeviceSimSlotSwitchProgress(String device_sn, FlightHubCallback.SubscribeDeviceSimSlotSwitchProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.SubscribeDeviceSimSlotSwitchProgressCallback swigCallback = new com.dji.flighthubsdk.generated.SubscribeDeviceSimSlotSwitchProgressCallback() {
                @Override
                public void onSubscribeDeviceSimSlotSwitchProgress(SimSlotSwitchResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toSimSlotSwitchResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[subscribeDeviceSimSlotSwitchProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.subscribeDeviceSimSlotSwitchProgressWrapper(device_sn, swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[subscribeDeviceSimSlotSwitchProgress] error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 SIM 卡槽切换进度
     * @param listenId 监听 ID
     */
    public void unSubscribeDeviceSimSlotSwitchProgress(int listenId) {
        try {
            this.deviceDebugModule.unsubscribeDeviceSimSlotSwitchProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "unSubscribeDeviceSimSlotSwitchProgress error: " + e.getMessage(), e);
        }
    }

    /**
     * 飞行器格式化（过程操作）
     * @param device_sn 设备序列号
     * @param callback 结果回调，返回错误信息和飞行器格式化结果
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/drone-format
     */
    public void droneFormat(String device_sn, FlightHubCallback.DroneFormatCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DroneFormatCallback swigCallback = new com.dji.flighthubsdk.generated.DroneFormatCallback() {
                @Override
                public void onDroneFormat(com.dji.flighthubsdk.generated.FlightHubError err, DroneFormatResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDroneFormatResult(result));
                        }
                    
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[droneFormat] drone format callback error: " + e.getMessage(), e);
                    } finally {
                        droneFormatCallbackRef.remove(id);
                    }
                }
            };
            synchronized (droneFormatCallbackRef) {
                droneFormatCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.droneFormatWrapper(device_sn, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[droneFormat] drone format error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), FlightHubSdkConvert.toDroneFormatResult(null));
            }
        }
    }

    /**
     * 切换飞行器 SDR 工作模式（即时操作）
     * @param device_sn 设备序列号
     * @param mode SDR 工作模式
     * @param callback 结果回调，返回错误信息
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/sdr-workmode-switch
     */
    public void droneSdrWorkMode(String device_sn, int mode, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("DeviceDebugModule", "[droneSdrWorkMode] drone sdr work mode callback error: " + e.getMessage(), e);
                    } finally {
                        droneSdrWorkModeCallbackRef.remove(id);
                    }
                }
            };
            synchronized (droneSdrWorkModeCallbackRef) {
                droneSdrWorkModeCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.droneSdrWorkModeWrapper(device_sn, mode, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[droneSdrWorkMode] drone sdr work mode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 云环境切换下发通知
     * @param param 设备列表 + 目标云环境配置
     * @param callback 结果回调，返回错误信息
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/cloud-environment-switch
     */
    public void cloudEnvironmentSwitch(com.dji.flighthubsdk.bean.CloudEnvironmentSwitchParam param, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[cloudEnvironmentSwitch] callback error: " + e.getMessage(), e);
                    } finally {
                        cloudEnvironmentSwitchCallbackRef.remove(id);
                    }
                }
            };
            synchronized (cloudEnvironmentSwitchCallbackRef) {
                cloudEnvironmentSwitchCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.cloudEnvironmentSwitchWrapper(FlightHubSdkConvert.toNativeCloudEnvironmentSwitchParam(param), swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[cloudEnvironmentSwitch] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 云环境切换回调通知
     * @param param 切换结果、鉴权信息、设备列表 + 目标云环境配置
     * @param callback 结果回调，返回错误信息
     * @implNote 链路: HTTP | HTTP POST /device/api/v1/service/cloud_environment_switch/callback
     */
    public void cloudEnvironmentSwitchCallback(com.dji.flighthubsdk.bean.CloudEnvironmentSwitchCallbackParam param, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[cloudEnvironmentSwitchCallback] callback error: " + e.getMessage(), e);
                    } finally {
                        cloudEnvironmentSwitchCallbackCallbackRef.remove(id);
                    }
                }
            };
            synchronized (cloudEnvironmentSwitchCallbackCallbackRef) {
                cloudEnvironmentSwitchCallbackCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deviceDebugModule.cloudEnvironmentSwitchCallbackWrapper(FlightHubSdkConvert.toNativeCloudEnvironmentSwitchCallbackParam(param), swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[cloudEnvironmentSwitchCallback] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅云环境切换进度
     * @param device_sn 设备序列号
     * @param callback 进度回调，返回云环境切换结果
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_cloud_environment_switch
     */
    public int listenDeviceCloudEnvironmentSwitchProgress(String device_sn, FlightHubCallback.DeviceCloudEnvironmentSwitchProgressCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DeviceCloudEnvironmentSwitchProgressCallback swigCallback = new com.dji.flighthubsdk.generated.DeviceCloudEnvironmentSwitchProgressCallback() {
                @Override
                public void onDeviceCloudEnvironmentSwitchProgress(com.dji.flighthubsdk.generated.CloudEnvironmentSwitchResult result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCloudEnvironmentSwitchResult(result));
                        }
                    } catch (Exception e) {
                        Logger.error("DeviceDebugModule", "[listenDeviceCloudEnvironmentSwitchProgress] callback error: " + e.getMessage(), e);
                    }
                }
            };
            int listenId = this.deviceDebugModule.listenDeviceCloudEnvironmentSwitchProgressWrapper(device_sn, swigCallback);
            return listenId;
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "[listenDeviceCloudEnvironmentSwitchProgress] error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅云环境切换进度
     * @param listenId 监听 ID
     */
    public void cancelListenDeviceCloudEnvironmentSwitchProgress(int listenId) {
        try {
            this.deviceDebugModule.cancelListenDeviceCloudEnvironmentSwitchProgressWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeviceDebugModule", "cancelListenDeviceCloudEnvironmentSwitchProgress error: " + e.getMessage(), e);
        }
    }
}