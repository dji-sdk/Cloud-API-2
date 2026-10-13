// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.ICameraControlModule;
import com.dji.flighthubsdk.bean.SetPhotoStorageSettingsParams;
import com.dji.flighthubsdk.bean.SetNightSceneModeParams;
import com.dji.flighthubsdk.bean.SetVideoStorageSettingsParams;
import com.dji.flighthubsdk.generated.SwitchLiveLensParams;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.util.Logger;

/**
 * 相机控制模块接口，提供镜头切换、拍照/录像、云台控制、相机参数设置等功能。
 */
public class CameraControlModule {

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setThermalGainModeActionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> switchLiveLensCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setPhotoStorageSettingsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setVideoStorageSettingsCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setNightSceneModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> resetGimbalCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> takePhotoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startRecordCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopRecordCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> zoomAtTargetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> switchCameraModeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> lookAtTargetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startPanoramaCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopPanoramaCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> smartPhotoTakePhotoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setLinkageZoomCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> sendCameraScreenDragCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setPhotoSizeCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setIntervalTakePhotoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> intervalTakePhotoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopTakePhotoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> cameraAimAtTargetCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();


    private final ICameraControlModule cameraControlModule;
    private final String sn;
    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public CameraControlModule(ICameraControlModule cameraControlModule, String sn) {
        this.cameraControlModule = cameraControlModule;
        this.sn = sn;
    }

    /**
     * 切换直播镜头
     * @param params 切换镜头参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_live_lens_change
     */
    public void drcSwitchLiveLens(com.dji.flighthubsdk.bean.SwitchLiveLensParams params, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("CameraControlModule", "[" + sn + "] Switch live lens callback error: " + e.getMessage(), e);
                    } finally {
                        switchLiveLensCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (switchLiveLensCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                switchLiveLensCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                com.dji.flighthubsdk.generated.SwitchLiveLensParams nativeSwitchLiveLensParams = null;

                try {

                    nativeSwitchLiveLensParams = FlightHubSdkConvert.toNativeSwitchLiveLensParams(params);

                    this.cameraControlModule.switchLiveLensWrapper(nativeSwitchLiveLensParams, swigCallback);

                } finally {

                    if (nativeSwitchLiveLensParams != null) {

                        nativeSwitchLiveLensParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSwitchLiveLens error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置照片存储参数
     * @param params 照片存储设置参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_photo_storage_set
     */
    public void drcSetPhotoStorageSettings(SetPhotoStorageSettingsParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetPhotoStorageSettingsParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetPhotoStorageSettingsParams(params);

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
                        Logger.error("CameraControlModule", "[" + sn + "] drcSetPhotoStorageSettings callback error: " + e.getMessage(), e);
                    } finally {
                        setPhotoStorageSettingsCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setPhotoStorageSettingsCallbackRef) {
                setPhotoStorageSettingsCallbackRef.put(id, container);
                this.cameraControlModule.setPhotoStorageSettingsWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetPhotoStorageSettings error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) {
                nativeParams.delete();
            }
        }
    }

    /**
     * 设置视频存储参数
     * @param params 视频存储设置参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_video_storage_set
     */
    public void drcSetVideoStorageSettings(SetVideoStorageSettingsParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetVideoStorageSettingsParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetVideoStorageSettingsParams(params);

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
                        Logger.error("CameraControlModule", "[" + sn + "] drcSetVideoStorageSettings callback error: " + e.getMessage(), e);
                    } finally {
                        setVideoStorageSettingsCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setVideoStorageSettingsCallbackRef) {
                setVideoStorageSettingsCallbackRef.put(id, container);
                this.cameraControlModule.setVideoStorageSettingsWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetVideoStorageSettings error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) {
                nativeParams.delete();
            }
        }
    }

    /**
     * 设置夜景模式
     * @param params 夜景模式参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_night_mode_set
     */
    public void drcSetNightSceneMode(SetNightSceneModeParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetNightSceneModeParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetNightSceneModeParams(params);

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
                        Logger.error("CameraControlModule", "[" + sn + "] drcSetNightSceneMode callback error: " + e.getMessage(), e);
                    } finally {
                        setNightSceneModeCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (setNightSceneModeCallbackRef) {
                setNightSceneModeCallbackRef.put(id, container);
                this.cameraControlModule.setNightSceneModeWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetNightSceneMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) {
                nativeParams.delete();
            }
        }
    }

    /**
     * 拍照
     * @param payloadIndex 负载索引
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_photo_take
     */
    public void drcTakePhoto(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("CameraControlModule", "[" + sn + "] Take photo callback error: " + e.getMessage(), e);
                    } finally {
                        takePhotoCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (takePhotoCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                takePhotoCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.cameraControlModule.takePhotoWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcTakePhoto error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 开始录像
     * @param payloadIndex 负载索引
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_recording_start
     */
    public void drcStartRecord(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("CameraControlModule", "[" + sn + "] Start record callback error: " + e.getMessage(), e);
                    } finally {
                        startRecordCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (startRecordCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                startRecordCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.cameraControlModule.startRecordWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcStartRecord error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 停止录像
     * @param payloadIndex 负载索引
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_recording_stop
     */
    public void drcStopRecord(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("CameraControlModule", "[" + sn + "] Stop record callback error: " + e.getMessage(), e);
                    } finally {
                        stopRecordCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (stopRecordCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                stopRecordCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.cameraControlModule.stopRecordWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcStopRecord error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 框选变焦
     * @param params 框选变焦参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_frame_zoom
     */
    public void drcZoomAtTarget(com.dji.flighthubsdk.bean.ZoomAtTargetParams params, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("CameraControlModule", "[" + sn + "] Zoom at target callback error: " + e.getMessage(), e);
                    } finally {
                        zoomAtTargetCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (zoomAtTargetCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                zoomAtTargetCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                com.dji.flighthubsdk.generated.ZoomAtTargetParams nativeZoomAtTargetParams = null;

                try {

                    nativeZoomAtTargetParams = FlightHubSdkConvert.toNativeZoomAtTargetParams(params);

                    this.cameraControlModule.zoomAtTargetWrapper(nativeZoomAtTargetParams, swigCallback);

                } finally {

                    if (nativeZoomAtTargetParams != null) {

                        nativeZoomAtTargetParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcZoomAtTarget error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置焦距
     * @param params 焦距参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_focal_length_set
     */
    public void drcSetFocalLength(com.dji.flighthubsdk.bean.CameraFocalLengthParams params, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("CameraControlModule", "[" + sn + "] drcSetFocalLength callback error: " + e.getMessage(), e);
                    } finally {
                        zoomAtTargetCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (zoomAtTargetCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                zoomAtTargetCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                com.dji.flighthubsdk.generated.CameraFocalLengthParams nativeCameraFocalLengthParams = null;

                try {

                    nativeCameraFocalLengthParams = FlightHubSdkConvert.toNativeCameraFocalLengthParams(params);

                    this.cameraControlModule.setFocalLengthWrapper(nativeCameraFocalLengthParams, swigCallback);

                } finally {

                    if (nativeCameraFocalLengthParams != null) {

                        nativeCameraFocalLengthParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetFocalLength error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 重置云台
     * @param payloadIndex 负载索引, 格式为 {type-subtype-gimbalindex}，例如 "81-0-0";
     * @param resetMode 云台重置模式, 参考上云 API, { 0: "回中", 1: "向下", 2: "偏航回中", 3: "俯仰向下" };
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_gimbal_reset
     */
    public void drcResetGimbal(String payloadIndex, int resetMode, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.GimbalResetParams nativeParams = new com.dji.flighthubsdk.generated.GimbalResetParams();
        nativeParams.setPayload_index(payloadIndex);
        nativeParams.setReset_mode(resetMode);
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
                        Logger.error("CameraControlModule", "[" + sn + "] drcResetGimbal callback error: " + e.getMessage(), e);
                    } finally {
                        resetGimbalCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (resetGimbalCallbackRef) {
                resetGimbalCallbackRef.put(id, container);
                this.cameraControlModule.resetGimbalWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcResetGimbal error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 看向目标
     * @param params 看向目标参数
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd (method: look_at_target)
     */
    public void lookAtTarget(com.dji.flighthubsdk.bean.LookAtTargetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError nativeErr) {
                    try {
                        if (callback != null) {
                            FlightHubError err = new FlightHubError(
                                    nativeErr.getError_code(),
                                    nativeErr.getHttp_code(),
                                    nativeErr.getServer_code(),
                                    nativeErr.getServer_message());
                            callback.onResult(err);
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] lookAtTarget callback error: " + e.getMessage(), e);
                    } finally {
                        lookAtTargetCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (lookAtTargetCallbackRef) {
                lookAtTargetCallbackRef.put(id, container);
                com.dji.flighthubsdk.generated.LookAtTargetParams nativeLookAtTargetParams = null;

                try {

                    nativeLookAtTargetParams = FlightHubSdkConvert.toNativeLookAtTargetParams(params);

                    this.cameraControlModule.lookAtTargetWrapper(nativeLookAtTargetParams, swigCallback);

                } finally {

                    if (nativeLookAtTargetParams != null) {

                        nativeLookAtTargetParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] lookAtTarget error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED, 0, 0, e.getMessage()));
            }
        }
    }

    /**
     * 开始全景拍摄
     * @param params 全景参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_photo_take (panorama mode)
     */
    public void drcStartPanorama(com.dji.flighthubsdk.bean.PanoramaParam params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.PanoramaParam nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativePanoramaParam(params);
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
                        Logger.error("CameraControlModule", "[" + sn + "] Start panorama callback error: " + e.getMessage(), e);
                    } finally {
                        startPanoramaCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (startPanoramaCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                startPanoramaCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.cameraControlModule.startPanoramaWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcStartPanorama error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) {
                nativeParams.delete();
            }
        }
    }

    /**
     * 停止全景拍摄
     * @param payloadIndex 负载索引
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_photo_stop
     */
    public void drcStopPanorama(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
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
                        Logger.error("CameraControlModule", "[" + sn + "] Stop panorama callback error: " + e.getMessage(), e);
                    } finally {
                        stopPanoramaCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (stopPanoramaCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                stopPanoramaCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                this.cameraControlModule.stopPanoramaWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcStopPanorama error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅全景拍摄状态
     * @param payloadIndex 负载索引
     * @param callback 全景状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | DRC 全景状态回调（drc_camera_photo_info_push filter: camera_mode = PANO_PHOTO）
     */
    public int listenDrcPanoramaStatus(String payloadIndex, FlightHubCallback.PanoramaStatusCallback callback) {
        try {
            com.dji.flighthubsdk.generated.PanoramaStatusCallback swigCallback = new com.dji.flighthubsdk.generated.PanoramaStatusCallback() {
                @Override
                public void onPanoramaStatus(com.dji.flighthubsdk.generated.PanoramaStatus panorama_status) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toPanoramaStatus(panorama_status));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Panorama status callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenPanoramaStatusWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcPanoramaStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toPanoramaStatus(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅全景拍摄状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcPanoramaStatus(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcPanoramaStatus(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcPanoramaStatus error: " + e.getMessage(), e);
        }
    }

    /**
     * 切换相机模式
     * @param params 切换相机模式参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_mode_switch
     */
    public void drcSwitchCameraMode(com.dji.flighthubsdk.bean.SwitchCameraModeParams params, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            // 调用SWIG生成的JNI方法
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
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
                        Logger.error("CameraControlModule", "[" + sn + "] Switch camera mode callback error: " + e.getMessage(), e);
                    } finally {
                        switchCameraModeCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (switchCameraModeCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                switchCameraModeCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                com.dji.flighthubsdk.generated.SwitchCameraModeParams nativeSwitchCameraModeParams = null;

                try {

                    nativeSwitchCameraModeParams = FlightHubSdkConvert.toNativeSwitchCameraModeParams(params);

                    this.cameraControlModule.switchCameraModeWrapper(nativeSwitchCameraModeParams, swigCallback);

                } finally {

                    if (nativeSwitchCameraModeParams != null) {

                        nativeSwitchCameraModeParams.delete();

                    }

                }

            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSwitchCameraMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅可用相机模式列表
     * @param payloadIndex 负载索引
     * @param callback 可用相机模式列表回调（用 std::vector<int> 替换 std::vector<CameraMode>，避免跨平台编译报错）
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | 硬编码可用相机模式列表（PHOTO / VIDEO / SMART_PHOTO / PANO_PHOTO 等），非 DRC push
     */
    public int listenDrcCameraModeAvailableRange(String payloadIndex, FlightHubCallback.CameraModeAvailableRangeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CameraModeAvailableRangeCallback swigCallback = new com.dji.flighthubsdk.generated.CameraModeAvailableRangeCallback() {
                @Override
                public void onCameraModeAvailableRange(com.dji.flighthubsdk.generated.IntVector cameraModes) {
                    try {
                        if (callback != null) {
                            // 将SWIG的IntVector转换为Java List<CameraMode>
                            java.util.List<com.dji.flighthubsdk.value.CameraMode> cameraModesList = new java.util.ArrayList<>();
                            Logger.debug("Drone", "[" + sn + "] Convert CameraModeVector to List<CameraMode>: " + cameraModesList.size());
                            for (int i = 0; i < cameraModes.size(); i++) {
                                cameraModesList.add(com.dji.flighthubsdk.value.CameraMode.fromValue(cameraModes.get(i)));
                            }
                            callback.onResult(cameraModesList);
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Camera mode available range callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenCameraModeAvailableRangeWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraModeAvailableRange error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new java.util.Vector<com.dji.flighthubsdk.value.CameraMode>());
            }
        }
        return -1;
    }

    /**
     * 取消订阅可用相机模式列表
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcCameraModeAvailableRange(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcCameraModeAvailableRange(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraModeAvailableRange error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅相机状态
     * @param payloadIndex 负载索引
     * @param callback 相机状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state)
     */
    public int listenDrcCameraState(String payloadIndex, FlightHubCallback.CameraStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CameraStateCallback swigCallback = new com.dji.flighthubsdk.generated.CameraStateCallback() {
                @Override
                public void onCameraState(com.dji.flighthubsdk.generated.CameraState camera_state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCameraState(camera_state));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Camera state callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenCameraStateWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toCameraState(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅相机状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcCameraState(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcCameraState(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraState error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅实时视图世界区域
     * @param payloadIndex 负载索引
     * @param callback 实时视图区域回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: liveview.liveview_world_region)
     */
    public int listenDrcLiveviewWorldRegion(String payloadIndex, FlightHubCallback.LiveviewWorldRegionCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.LiveviewWorldRegionCallback swigCallback = new com.dji.flighthubsdk.generated.LiveviewWorldRegionCallback() {
                @Override
                public void onLiveviewWorldRegion(com.dji.flighthubsdk.generated.LiveviewWorldRegion liveview_world_region) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toLiveviewWorldRegion(liveview_world_region));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Liveview world region callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenLiveviewWorldRegionWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcLiveviewWorldRegion error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toLiveviewWorldRegion(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅实时视图世界区域
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcLiveviewWorldRegion(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcLiveviewWorldRegion(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcLiveviewWorldRegion error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅广角镜头状态
     * @param payloadIndex 负载索引
     * @param callback 广角镜头状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: wide_lense, 含 wide_fov_info)
     */
    public int listenDrcCameraWideLensState(String payloadIndex, FlightHubCallback.CameraWideLensStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CameraWideLensStateCallback swigCallback = new com.dji.flighthubsdk.generated.CameraWideLensStateCallback() {
                @Override
                public void onCameraWideLensState(com.dji.flighthubsdk.generated.CameraWideLensState camera_wide_lens_state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCameraWideLensState(camera_wide_lens_state));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Camera wide lens state callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenCameraWideLensStateWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraWideLensState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toCameraWideLensState(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅广角镜头状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcCameraWideLensState(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcCameraWideLensState(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraWideLensState error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅变焦镜头状态
     * @param payloadIndex 负载索引
     * @param callback 变焦镜头状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: zoom_lense, 含 zoom_fov_info)
     */
    public int listenDrcCameraZoomLensState(String payloadIndex, FlightHubCallback.CameraZoomLensStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.CameraZoomLensStateCallback swigCallback = new com.dji.flighthubsdk.generated.CameraZoomLensStateCallback() {
                @Override
                public void onCameraZoomLensState(com.dji.flighthubsdk.generated.CameraZoomLensState camera_zoom_lens_state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCameraZoomLensState(camera_zoom_lens_state));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Camera zoom lens state callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenCameraZoomLensStateWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraZoomLensState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toCameraZoomLensState(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅变焦镜头状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcCameraZoomLensState(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcCameraZoomLensState(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraZoomLensState error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅测量目标数据
     * @param payloadIndex 负载索引
     * @param callback 测量目标回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: measure_target)
     */
    public int listenDrcCameraMeasureTarget(String payloadIndex, FlightHubCallback.CameraMeasureTargetCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraMeasureTargetCallback swigCallback = new com.dji.flighthubsdk.generated.CameraMeasureTargetCallback() {
                @Override
                public void onCameraMeasureTarget(com.dji.flighthubsdk.generated.CameraMeasureTarget camera_measure_target) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toCameraMeasureTarget(camera_measure_target));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Camera measure target callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenCameraMeasureTargetWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraMeasureTarget error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toCameraMeasureTarget(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅测量目标数据
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcCameraMeasureTarget(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcCameraMeasureTarget(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraMeasureTarget error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅夜景模式设置
     * @param payloadIndex 负载索引
     * @param callback 夜景模式设置回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.night_mode_settings)
     */
    public int listenDrcNightModeSettings(String payloadIndex, FlightHubCallback.NightModeSettingsCallback callback) {
        try {
            com.dji.flighthubsdk.generated.NightModeSettingsCallback swigCallback =
                    new com.dji.flighthubsdk.generated.NightModeSettingsCallback() {
                @Override
                public void onNightModeSettings(com.dji.flighthubsdk.generated.NightModeSettings settings) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toNightModeSettings(settings));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] night mode settings callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenNightModeSettingsWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcNightModeSettings error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅夜景模式设置
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcNightModeSettings(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcNightModeSettings(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcNightModeSettings error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅云台状态
     * @param payloadIndex 负载索引
     * @param callback 云台状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: gimbal_pitch / gimbal_roll / gimbal_yaw)
     */
    public int listenDrcGimbalState(String payloadIndex, FlightHubCallback.GimbalStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.GimbalStateCallback swigCallback = new com.dji.flighthubsdk.generated.GimbalStateCallback() {
                @Override
                public void onGimbalState(com.dji.flighthubsdk.generated.GimbalState gimbal_state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toGimbalState(gimbal_state));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] Gimbal state callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenGimbalStateWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcGimbalState error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toGimbalState(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅云台状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcGimbalState(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenDrcGimbalState(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcGimbalState error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅直播视频信息
     * @param payloadIndex 负载索引
     * @param callback 视频信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_live_video_info_push
     */
    public int listenDrcLiveVideoInfo(String payloadIndex, FlightHubCallback.LiveVideoInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.LiveVideoInfoCallback swigCallback = new com.dji.flighthubsdk.generated.LiveVideoInfoCallback() {
                @Override
                public void onLiveVideoInfo(com.dji.flighthubsdk.generated.LiveVideoInfo info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toLiveVideoInfo(info));
                    }
                }
            };

            int listenId = this.cameraControlModule.listenLiveVideoInfoWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcLiveVideoInfo error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅直播视频信息
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcLiveVideoInfo(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenLiveVideoInfoWrapper(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcLiveVideoInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅红外增益模式
     * @param payloadIndex 负载索引
     * @param callback 红外增益模式回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: ir_lense.thermal_gain_mode; listenDrcCameraIrLensState 的子集，兼容保留)
     */
    public int listenDrcThermalGainMode(String payloadIndex, FlightHubCallback.ThermalGainModeInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ThermalGainModeInfoCallback swigCallback = new com.dji.flighthubsdk.generated.ThermalGainModeInfoCallback() {
                @Override
                public void onThermalGainModeInfo(com.dji.flighthubsdk.generated.ThermalGainModeInfo info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toThermalGainModeInfo(info));
                    }
                }
            };

            int listenId = this.cameraControlModule.listenThermalGainModeWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcThermalGainMode error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅红外增益模式
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcThermalGainMode(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenThermalGainModeWrapper(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcThermalGainMode error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅红外镜头状态（DRC 上行 ir_lense 全量）
     * @param payloadIndex 负载索引
     * @param callback 红外镜头状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: ir_lense)
     */
    public int listenDrcCameraIrLensState(String payloadIndex, FlightHubCallback.CameraIrLensStateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraIrLensStateCallback swigCallback = new com.dji.flighthubsdk.generated.CameraIrLensStateCallback() {
                @Override
                public void onCameraIrLensState(com.dji.flighthubsdk.generated.CameraIrLensState state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toCameraIrLensState(state));
                    }
                }
            };
            int listenId = this.cameraControlModule.listenCameraIrLensStateWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraIrLensState error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅红外镜头状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcCameraIrLensState(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenCameraIrLensStateWrapper(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraIrLensState error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅红外测温信息（DRC 上行 ir_thermometry 全量）
     * @param payloadIndex 负载索引
     * @param callback 红外测温回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_camera_osd_info_push (field: ir_thermometry)
     */
    public int listenDrcCameraIrThermometry(String payloadIndex, FlightHubCallback.CameraIrThermometryInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraIrThermometryInfoCallback swigCallback = new com.dji.flighthubsdk.generated.CameraIrThermometryInfoCallback() {
                @Override
                public void onCameraIrThermometryInfo(com.dji.flighthubsdk.generated.CameraIrThermometryInfo info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toCameraIrThermometryInfo(info));
                    }
                }
            };
            int listenId = this.cameraControlModule.listenCameraIrThermometryWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraIrThermometry error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅红外测温信息
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcCameraIrThermometry(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenCameraIrThermometryWrapper(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraIrThermometry error: " + e.getMessage(), e);
        }
    }

    // ==================== B4 drc_camera_state_push 拆分 listen ====================

    /**
     * 订阅相机水印设置
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.camera_watermark_settings)
     */
    public int listenDrcCameraWatermarkSettings(String payloadIndex, FlightHubCallback.CameraWatermarkSettingsCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraWatermarkSettingsCallback swigCallback = new com.dji.flighthubsdk.generated.CameraWatermarkSettingsCallback() {
                @Override public void onCameraWatermarkSettings(com.dji.flighthubsdk.generated.CameraWatermarkSettings v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraWatermarkSettings(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraWatermarkSettingsWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraWatermarkSettings error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraWatermarkSettings(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraWatermarkSettingsWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraWatermarkSettings error: " + e.getMessage(), e); }
    }

    /**
     * 订阅相机存储信息
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.storage)
     */
    public int listenDrcCameraStorageInfo(String payloadIndex, FlightHubCallback.CameraStorageInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraStorageInfoCallback swigCallback = new com.dji.flighthubsdk.generated.CameraStorageInfoCallback() {
                @Override public void onCameraStorageInfo(com.dji.flighthubsdk.generated.CameraStorageInfo v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraStorageInfo(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraStorageInfoWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraStorageInfo error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraStorageInfo(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraStorageInfoWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraStorageInfo error: " + e.getMessage(), e); }
    }

    /**
     * 订阅红外画中画状态
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.ir_pip_state)
     */
    public int listenDrcCameraIrPipState(String payloadIndex, FlightHubCallback.CameraIrPipStateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraIrPipStateCallback swigCallback = new com.dji.flighthubsdk.generated.CameraIrPipStateCallback() {
                @Override public void onCameraIrPipState(com.dji.flighthubsdk.generated.CameraIrPipState v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraIrPipState(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraIrPipStateWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraIrPipState error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraIrPipState(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraIrPipStateWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraIrPipState error: " + e.getMessage(), e); }
    }

    /**
     * 订阅等温线渲染多边形
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_state.isotherm_rendering_polygon)
     */
    public int listenDrcCameraIsothermPolygon(String payloadIndex, FlightHubCallback.CameraIsothermPolygonCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraIsothermPolygonCallback swigCallback = new com.dji.flighthubsdk.generated.CameraIsothermPolygonCallback() {
                @Override public void onCameraIsothermPolygon(com.dji.flighthubsdk.generated.CameraIsothermPolygon v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraIsothermPolygon(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraIsothermPolygonWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraIsothermPolygon error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraIsothermPolygon(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraIsothermPolygonWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraIsothermPolygon error: " + e.getMessage(), e); }
    }

    /**
     * 订阅媒体存储列表
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: media_storage)
     */
    public int listenDrcCameraMediaStorage(String payloadIndex, FlightHubCallback.CameraMediaStorageCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraMediaStorageCallback swigCallback = new com.dji.flighthubsdk.generated.CameraMediaStorageCallback() {
                @Override public void onCameraMediaStorage(com.dji.flighthubsdk.generated.CameraMediaStorage v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraMediaStorage(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraMediaStorageWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraMediaStorage error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraMediaStorage(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraMediaStorageWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraMediaStorage error: " + e.getMessage(), e); }
    }

    /**
     * 订阅去雾算法信息
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: algorithm_dehaze_info)
     */
    public int listenDrcCameraAlgorithmDehazeInfo(String payloadIndex, FlightHubCallback.CameraAlgorithmDehazeInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraAlgorithmDehazeInfoCallback swigCallback = new com.dji.flighthubsdk.generated.CameraAlgorithmDehazeInfoCallback() {
                @Override public void onCameraAlgorithmDehazeInfo(com.dji.flighthubsdk.generated.CameraAlgorithmDehazeInfo v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraAlgorithmDehazeInfo(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraAlgorithmDehazeInfoWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraAlgorithmDehazeInfo error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraAlgorithmDehazeInfo(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraAlgorithmDehazeInfoWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraAlgorithmDehazeInfo error: " + e.getMessage(), e); }
    }

    /**
     * 订阅预录制信息
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: pre_recorded_info)
     */
    public int listenDrcCameraPreRecordedInfo(String payloadIndex, FlightHubCallback.CameraPreRecordedInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraPreRecordedInfoCallback swigCallback = new com.dji.flighthubsdk.generated.CameraPreRecordedInfoCallback() {
                @Override public void onCameraPreRecordedInfo(com.dji.flighthubsdk.generated.CameraPreRecordedInfo v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraPreRecordedInfo(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraPreRecordedInfoWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraPreRecordedInfo error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraPreRecordedInfo(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraPreRecordedInfoWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraPreRecordedInfo error: " + e.getMessage(), e); }
    }

    /**
     * 订阅相机能力集
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_capability)
     */
    public int listenDrcCameraCapability(String payloadIndex, FlightHubCallback.CameraCapabilityCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraCapabilityCallback swigCallback = new com.dji.flighthubsdk.generated.CameraCapabilityCallback() {
                @Override public void onCameraCapability(com.dji.flighthubsdk.generated.CameraCapability v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraCapability(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraCapabilityWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraCapability error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraCapability(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraCapabilityWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraCapability error: " + e.getMessage(), e); }
    }

    /**
     * 订阅相机可设置能力集
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_settable_capability)
     */
    public int listenDrcCameraSettableCapability(String payloadIndex, FlightHubCallback.CameraSettableCapabilityCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraSettableCapabilityCallback swigCallback = new com.dji.flighthubsdk.generated.CameraSettableCapabilityCallback() {
                @Override public void onCameraSettableCapability(com.dji.flighthubsdk.generated.CameraSettableCapability v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraSettableCapability(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraSettableCapabilityWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraSettableCapability error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraSettableCapability(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraSettableCapabilityWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraSettableCapability error: " + e.getMessage(), e); }
    }

    /**
     * 订阅相机可见能力集
     * @implNote 链路: DRC Northbound | method: drc_camera_state_push (field: camera_visible_capability)
     */
    public int listenDrcCameraVisibleCapability(String payloadIndex, FlightHubCallback.CameraVisibleCapabilityCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraVisibleCapabilityCallback swigCallback = new com.dji.flighthubsdk.generated.CameraVisibleCapabilityCallback() {
                @Override public void onCameraVisibleCapability(com.dji.flighthubsdk.generated.CameraVisibleCapability v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraVisibleCapability(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraVisibleCapabilityWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraVisibleCapability error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraVisibleCapability(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraVisibleCapabilityWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraVisibleCapability error: " + e.getMessage(), e); }
    }

    // ==================== B4.1 drc_camera_photo_info_push 全量透传 ====================

    /**
     * 订阅相机拍照上行信息（全量透传，含 result 与非全景/定时模式字段）
     * @implNote 链路: DRC Northbound | method: drc_camera_photo_info_push
     */
    public int listenDrcCameraPhotoInfo(String payloadIndex, FlightHubCallback.CameraPhotoInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.CameraPhotoInfoCallback swigCallback = new com.dji.flighthubsdk.generated.CameraPhotoInfoCallback() {
                @Override public void onCameraPhotoInfo(com.dji.flighthubsdk.generated.CameraPhotoInfo v) {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toCameraPhotoInfo(v));
                }
            };
            int listenId = this.cameraControlModule.listenCameraPhotoInfoWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) swigCallback.swigReleaseOwnership();
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcCameraPhotoInfo error: " + e.getMessage(), e);
            return -1;
        }
    }
    public void cancelListenDrcCameraPhotoInfo(String payloadIndex, int listenId) {
        try { this.cameraControlModule.cancelListenCameraPhotoInfoWrapper(payloadIndex, listenId); }
        catch (Exception e) { Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcCameraPhotoInfo error: " + e.getMessage(), e); }
    }


    /**
     * 智能低光拍照
     * @param payloadIndex 负载索引
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_photo_take (smart mode)
     */
    public void drcSmartPhotoTakePhoto(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] drcSmartPhotoTakePhoto callback error: " + e.getMessage(), e);
                    } finally {
                        smartPhotoTakePhotoCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (smartPhotoTakePhotoCallbackRef) {
                smartPhotoTakePhotoCallbackRef.put(id, container);
                this.cameraControlModule.smartPhotoTakePhotoWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSmartPhotoTakePhoto error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置联动变焦
     * @param params 联动变焦参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_linkage_zoom_set
     */
    public void drcSetLinkageZoom(com.dji.flighthubsdk.bean.LinkageZoomParams params, FlightHubCallback.ResultCallbackWithError callback) {
        invokeParamAction(params, callback, setLinkageZoomCallbackRef, "drcSetLinkageZoom",
            () -> FlightHubSdkConvert.toNativeLinkageZoomParams(params),
            (nativeParams, swigCallback) -> this.cameraControlModule.setLinkageZoomWrapper(nativeParams, swigCallback),
            com.dji.flighthubsdk.generated.LinkageZoomParams::delete);
    }

    /**
     * 画面拖动控制云台转动速度
     * @param params 云台拖拽参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_screen_drag
     */
    public void drcSendCameraScreenDrag(com.dji.flighthubsdk.bean.CameraScreenDragParams params, FlightHubCallback.ResultCallbackWithError callback) {
        invokeParamAction(params, callback, sendCameraScreenDragCallbackRef, "drcSendCameraScreenDrag",
            () -> FlightHubSdkConvert.toNativeCameraScreenDragParams(params),
            (nativeParams, swigCallback) -> this.cameraControlModule.sendCameraScreenDragWrapper(nativeParams, swigCallback),
            com.dji.flighthubsdk.generated.CameraScreenDragParams::delete);
    }

    /**
     * 设置照片分辨率
     * @param params 照片分辨率参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_photo_size_set
     */
    public void drcSetPhotoSize(com.dji.flighthubsdk.bean.PhotoSizeParam params, FlightHubCallback.ResultCallbackWithError callback) {
        invokeParamAction(params, callback, setPhotoSizeCallbackRef, "drcSetPhotoSize",
            () -> FlightHubSdkConvert.toNativePhotoSizeParam(params),
            (nativeParams, swigCallback) -> this.cameraControlModule.setPhotoSizeWrapper(nativeParams, swigCallback),
            com.dji.flighthubsdk.generated.PhotoSizeParam::delete);
    }

    /**
     * 设置定时拍参数
     * @param params 定时拍设置参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_interval_photo_set
     */
    public void drcSetIntervalTakePhoto(com.dji.flighthubsdk.bean.IntervalTakePhotoSetParam params, FlightHubCallback.ResultCallbackWithError callback) {
        invokeParamAction(params, callback, setIntervalTakePhotoCallbackRef, "drcSetIntervalTakePhoto",
            () -> FlightHubSdkConvert.toNativeIntervalTakePhotoSetParam(params),
            (nativeParams, swigCallback) -> this.cameraControlModule.setIntervalTakePhotoWrapper(nativeParams, swigCallback),
            com.dji.flighthubsdk.generated.IntervalTakePhotoSetParam::delete);
    }

    /**
     * 执行定时拍
     * @param params 定时拍参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_photo_take (interval mode)
     */
    public void drcIntervalTakePhoto(com.dji.flighthubsdk.bean.IntervalTakePhotoParam params, FlightHubCallback.ResultCallbackWithError callback) {
        invokeParamAction(params, callback, intervalTakePhotoCallbackRef, "drcIntervalTakePhoto",
            () -> FlightHubSdkConvert.toNativeIntervalTakePhotoParam(params),
            (nativeParams, swigCallback) -> this.cameraControlModule.intervalTakePhotoWrapper(nativeParams, swigCallback),
            com.dji.flighthubsdk.generated.IntervalTakePhotoParam::delete);
    }

    /**
     * 停止拍照
     * @param payloadIndex 负载索引
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_photo_stop
     */
    public void drcStopTakePhoto(String payloadIndex, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] drcStopTakePhoto callback error: " + e.getMessage(), e);
                    } finally {
                        stopTakePhotoCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (stopTakePhotoCallbackRef) {
                stopTakePhotoCallbackRef.put(id, container);
                this.cameraControlModule.stopTakePhotoWrapper(payloadIndex, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcStopTakePhoto error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 订阅定时拍状态
     * @param payloadIndex 负载索引
     * @param callback 定时拍状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | DRC 定时拍状态回调（drc_camera_photo_info_push filter: camera_mode = INTERVAL_PHOTO）
     */
    public int listenDrcIntervalTakePhotoStatus(String payloadIndex, FlightHubCallback.IntervalTakePhotoStatusCallback callback) {
        try {
            com.dji.flighthubsdk.generated.IntervalTakePhotoStatusCallback swigCallback = new com.dji.flighthubsdk.generated.IntervalTakePhotoStatusCallback() {
                @Override
                public void onIntervalTakePhotoStatus(com.dji.flighthubsdk.generated.IntervalTakePhotoStatus status) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toIntervalTakePhotoStatus(status));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] listenDrcIntervalTakePhotoStatus callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.cameraControlModule.listenIntervalTakePhotoStatusWrapper(payloadIndex, swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDrcIntervalTakePhotoStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.IntervalTakePhotoStatus());
            }
        }
        return -1;
    }

    /**
     * 取消订阅定时拍状态
     * @param payloadIndex 负载索引
     * @param listenId 监听 ID
     */
    public void cancelListenDrcIntervalTakePhotoStatus(String payloadIndex, int listenId) {
        try {
            this.cameraControlModule.cancelListenIntervalTakePhotoStatusWrapper(payloadIndex, listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDrcIntervalTakePhotoStatus error: " + e.getMessage(), e);
        }
    }

    /**
     * 双击画面瞄准目标
     * @param params 瞄准参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_camera_aim
     */
    public void drcCameraAimAtTarget(com.dji.flighthubsdk.bean.CameraAimParam params, FlightHubCallback.ResultCallbackWithError callback) {
        invokeParamAction(params, callback, cameraAimAtTargetCallbackRef, "drcCameraAimAtTarget",
            () -> FlightHubSdkConvert.toNativeCameraAimParam(params),
            (nativeParams, swigCallback) -> this.cameraControlModule.cameraAimAtTargetWrapper(nativeParams, swigCallback),
            com.dji.flighthubsdk.generated.CameraAimParam::delete);
    }

    @FunctionalInterface
    private interface NativeParamSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    private interface NativeParamInvoker<T> {
        void invoke(T nativeParams, com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback) throws Exception;
    }

    @FunctionalInterface
    private interface NativeParamDeleter<T> {
        void delete(T nativeParams);
    }

    private <T> void invokeParamAction(
            Object params,
            FlightHubCallback.ResultCallbackWithError callback,
            java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> callbackRef,
            String methodName,
            NativeParamSupplier<T> toNative,
            NativeParamInvoker<T> invoke,
            NativeParamDeleter<T> deleteNative) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error("CameraControlModule", "[" + sn + "] " + methodName + " callback error: " + e.getMessage(), e);
                    } finally {
                        callbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            T nativeParams = null;
            synchronized (callbackRef) {
                callbackRef.put(id, container);
                try {
                    nativeParams = toNative.get();
                    invoke.invoke(nativeParams, swigCallback);
                } finally {
                    if (nativeParams != null) {
                        deleteNative.delete(nativeParams);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] " + methodName + " error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 设置红外增益模式
     * @param params 红外增益模式参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_thermal_gain_mode_set
     */
    public void drcSetThermalGainMode(com.dji.flighthubsdk.bean.ThermalGainModeSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.ThermalGainModeSetParams nativeParam = null;
        try {
            nativeParam = FlightHubSdkConvert.toNativeThermalGainModeSetParams(params);
            final int id = generateActionCallbackId();
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
                        Logger.error("CameraControlModule", "[" + sn + "] drcSetThermalGainMode callback error: " + e.getMessage(), e);
                    } finally {
                        setThermalGainModeActionCallbackRef.remove(id);
                    }
                }
            };

            synchronized (setThermalGainModeActionCallbackRef) {
                setThermalGainModeActionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.cameraControlModule.setThermalGainModeWrapper(nativeParam, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetThermalGainMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParam != null) {
                nativeParam.delete();
            }
        }
    }

    // ==================== 新增 DRC 高级相机控制方法 ====================

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> advancedCameraCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    /** 通用一次性 DRC action 调用辅助 */
    private void invokeDrcAction(FlightHubCallback.ResultCallbackWithError callback,
                                  java.util.function.Consumer<com.dji.flighthubsdk.generated.ResultCallbackWithError> nativeCall) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCb =
                new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                        try {
                            if (callback != null) {
                                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                    err.getError_code(), (int) err.getHttp_code(),
                                    (int) err.getServer_code(), (int) err.getDevice_code(),
                                    err.getServer_message()));
                            }
                        } catch (Exception e) {
                            Logger.error("CameraControlModule", "drc action callback error: " + e.getMessage(), e);
                        } finally {
                            advancedCameraCallbackRef.remove(id);
                        }
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (advancedCameraCallbackRef) {
                advancedCameraCallbackRef.put(id, container);
                nativeCall.accept(swigCb);
            }
        } catch (Exception e) {
            Logger.error("CameraControlModule", "drc action error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ── Camera Advanced Params ──

    /**
     * 设置相机光圈值
     * @implNote 链路: DRC | method: drc_camera_aperture_value_set
     */
    public void drcSetCameraApertureValue(com.dji.flighthubsdk.bean.CameraApertureValueSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraApertureValueSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraApertureValueSetParams(params);
            final com.dji.flighthubsdk.generated.CameraApertureValueSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraApertureValueWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraApertureValue error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机曝光模式
     * @implNote 链路: DRC | method: drc_camera_exposure_mode_set
     */
    public void drcSetCameraExposureMode(com.dji.flighthubsdk.bean.CameraExposureModeSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraExposureModeSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraExposureModeSetParams(params);
            final com.dji.flighthubsdk.generated.CameraExposureModeSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraExposureModeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraExposureMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机曝光值
     * @implNote 链路: DRC | method: drc_camera_exposure_set
     */
    public void drcSetCameraExposureValue(com.dji.flighthubsdk.bean.CameraExposureSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraExposureSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraExposureSetParams(params);
            final com.dji.flighthubsdk.generated.CameraExposureSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraExposureValueWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraExposureValue error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机对焦模式
     * @implNote 链路: DRC | method: drc_camera_focus_mode_set
     */
    public void drcSetCameraFocusMode(com.dji.flighthubsdk.bean.CameraFocusModeSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraFocusModeSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraFocusModeSetParams(params);
            final com.dji.flighthubsdk.generated.CameraFocusModeSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraFocusModeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraFocusMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机对焦值
     * @implNote 链路: DRC | method: drc_camera_focus_value_set
     */
    public void drcSetCameraFocusValue(com.dji.flighthubsdk.bean.CameraFocusValueSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraFocusValueSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraFocusValueSetParams(params);
            final com.dji.flighthubsdk.generated.CameraFocusValueSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraFocusValueWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraFocusValue error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机ISO
     * @implNote 链路: DRC | method: drc_camera_iso_set
     */
    public void drcSetCameraIso(com.dji.flighthubsdk.bean.CameraIsoSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraIsoSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraIsoSetParams(params);
            final com.dji.flighthubsdk.generated.CameraIsoSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraIsoWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraIso error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机机械快门
     * @implNote 链路: DRC | method: drc_camera_mechanical_shutter_set
     */
    public void drcSetCameraMechanicalShutter(com.dji.flighthubsdk.bean.CameraMechanicalShutterSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraMechanicalShutterSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraMechanicalShutterSetParams(params);
            final com.dji.flighthubsdk.generated.CameraMechanicalShutterSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraMechanicalShutterWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraMechanicalShutter error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 相机点对焦
     * @implNote 链路: DRC | method: drc_camera_point_focus_action
     */
    public void drcCameraPointFocusAction(com.dji.flighthubsdk.bean.CameraPointFocusActionParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraPointFocusActionParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraPointFocusActionParams(params);
            final com.dji.flighthubsdk.generated.CameraPointFocusActionParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.cameraPointFocusActionWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcCameraPointFocusAction error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机快门速度
     * @implNote 链路: DRC | method: drc_camera_shutter_set
     */
    public void drcSetCameraShutter(com.dji.flighthubsdk.bean.CameraShutterSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraShutterSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraShutterSetParams(params);
            final com.dji.flighthubsdk.generated.CameraShutterSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraShutterWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraShutter error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机水印
     * @implNote 链路: DRC | method: drc_camera_watermark_set
     */
    public void drcSetCameraWatermark(com.dji.flighthubsdk.bean.CameraWatermarkParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraWatermarkParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraWatermarkParams(params);
            final com.dji.flighthubsdk.generated.CameraWatermarkParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraWatermarkWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraWatermark error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置相机畸变矫正
     * @implNote 链路: DRC | method: drc_camera_dewarping_set
     */
    public void drcSetCameraDewarping(com.dji.flighthubsdk.bean.CameraDewarpingSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraDewarpingSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraDewarpingSetParams(params);
            final com.dji.flighthubsdk.generated.CameraDewarpingSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraDewarpingWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraDewarping error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    // ── Screen/Gimbal Advanced ──

    /**
     * 设置相机分屏显示
     * @implNote 链路: DRC | method: drc_camera_screen_split
     */
    public void drcSetCameraScreenSplit(com.dji.flighthubsdk.bean.CameraScreenSplitParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraScreenSplitParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraScreenSplitParams(params);
            final com.dji.flighthubsdk.generated.CameraScreenSplitParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraScreenSplitWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraScreenSplit error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 除雾
     * @implNote 链路: DRC | method: drc_clean_fog
     */
    public void drcCleanFog(com.dji.flighthubsdk.bean.CleanFogParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CleanFogParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCleanFogParams(params);
            final com.dji.flighthubsdk.generated.CleanFogParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.cleanFogWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcCleanFog error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    // ── Thermal/IR ──

    /**
     * 设置红外调色板
     * @implNote 链路: DRC | method: drc_thermal_current_palette_set
     */
    public void drcSetThermalCurrentPalette(com.dji.flighthubsdk.bean.ThermalCurrentPaletteSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.ThermalCurrentPaletteSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeThermalCurrentPaletteSetParams(params);
            final com.dji.flighthubsdk.generated.ThermalCurrentPaletteSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setThermalCurrentPaletteWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetThermalCurrentPalette error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外超分辨率
     * @implNote 链路: DRC | method: drc_thermal_super_resolution_enable
     */
    public void drcSetThermalSuperResolutionEnable(com.dji.flighthubsdk.bean.ThermalSuperResolutionEnableParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.ThermalSuperResolutionEnableParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeThermalSuperResolutionEnableParams(params);
            final com.dji.flighthubsdk.generated.ThermalSuperResolutionEnableParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setThermalSuperResolutionEnableWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetThermalSuperResolutionEnable error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外测温模式
     * @implNote 链路: DRC | method: drc_ir_metering_mode_set
     */
    public void drcSetIrMeteringMode(com.dji.flighthubsdk.bean.IrMeteringModeSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.IrMeteringModeSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeIrMeteringModeSetParams(params);
            final com.dji.flighthubsdk.generated.IrMeteringModeSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIrMeteringModeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIrMeteringMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外测温点
     * @implNote 链路: DRC | method: drc_ir_metering_point_set
     */
    public void drcSetIrMeteringPoint(com.dji.flighthubsdk.bean.IrMeteringPointSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.IrMeteringPointSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeIrMeteringPointSetParams(params);
            final com.dji.flighthubsdk.generated.IrMeteringPointSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIrMeteringPointWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIrMeteringPoint error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外测温区域
     * @implNote 链路: DRC | method: drc_ir_metering_area_set
     */
    public void drcSetIrMeteringArea(com.dji.flighthubsdk.bean.IrMeteringAreaSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.IrMeteringAreaSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeIrMeteringAreaSetParams(params);
            final com.dji.flighthubsdk.generated.IrMeteringAreaSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIrMeteringAreaWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIrMeteringArea error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外显示模式
     * @implNote 链路: DRC | method: drc_set_ir_display_mode
     */
    public void drcSetIrDisplayMode(com.dji.flighthubsdk.bean.SetIrDisplayModeParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIrDisplayModeParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIrDisplayModeParams(params);
            final com.dji.flighthubsdk.generated.SetIrDisplayModeParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIrDisplayModeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIrDisplayMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外增强模式
     * @implNote 链路: DRC | method: drc_set_ir_enhance_mode
     */
    public void drcSetIrEnhanceMode(com.dji.flighthubsdk.bean.SetIrEnhanceModeParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIrEnhanceModeParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIrEnhanceModeParams(params);
            final com.dji.flighthubsdk.generated.SetIrEnhanceModeParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIrEnhanceModeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIrEnhanceMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外混合比例
     * @implNote 链路: DRC | method: drc_set_ir_mix_rate
     */
    public void drcSetIrMixRate(com.dji.flighthubsdk.bean.SetIrMixRateParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIrMixRateParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIrMixRateParams(params);
            final com.dji.flighthubsdk.generated.SetIrMixRateParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIrMixRateWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIrMixRate error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置等温线开关
     * @implNote 链路: DRC | method: drc_set_isotherm_enable
     */
    public void drcSetIsothermEnable(com.dji.flighthubsdk.bean.SetIsothermEnableParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIsothermEnableParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIsothermEnableParams(params);
            final com.dji.flighthubsdk.generated.SetIsothermEnableParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIsothermEnableWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIsothermEnable error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置等温线渲染类型
     * @implNote 链路: DRC | method: drc_set_isotherm_color_rendering_type
     */
    public void drcSetIsothermColorRenderingType(com.dji.flighthubsdk.bean.SetIsothermColorRenderingTypeParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIsothermColorRenderingTypeParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIsothermColorRenderingTypeParams(params);
            final com.dji.flighthubsdk.generated.SetIsothermColorRenderingTypeParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIsothermColorRenderingTypeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIsothermColorRenderingType error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置等温线温度范围
     * @implNote 链路: DRC | method: drc_set_isotherm_lower_upper_range
     */
    public void drcSetIsothermLowerUpperRange(com.dji.flighthubsdk.bean.SetIsothermLowerUpperRangeParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIsothermLowerUpperRangeParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIsothermLowerUpperRangeParams(params);
            final com.dji.flighthubsdk.generated.SetIsothermLowerUpperRangeParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIsothermLowerUpperRangeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIsothermLowerUpperRange error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置等温线渲染多边形
     * @implNote 链路: DRC | method: drc_set_isotherm_rendering_polygon
     */
    public void drcSetIsothermRenderingPolygon(com.dji.flighthubsdk.bean.SetIsothermRenderingPolygonParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIsothermRenderingPolygonParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIsothermRenderingPolygonParams(params);
            final com.dji.flighthubsdk.generated.SetIsothermRenderingPolygonParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIsothermRenderingPolygonWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIsothermRenderingPolygon error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置等温线ROI渲染使能
     * @implNote 链路: DRC | method: drc_set_isotherm_roi_based_rendering_enable
     */
    public void drcSetIsothermRoiBasedRenderingEnable(com.dji.flighthubsdk.bean.SetIsothermRoiBasedRenderingEnableParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.SetIsothermRoiBasedRenderingEnableParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeSetIsothermRoiBasedRenderingEnableParams(params);
            final com.dji.flighthubsdk.generated.SetIsothermRoiBasedRenderingEnableParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setIsothermRoiBasedRenderingEnableWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetIsothermRoiBasedRenderingEnable error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置红外补光灯
     * @implNote 链路: DRC | method: drc_infrared_fill_light_enable
     */
    public void drcSetInfraredFillLightEnable(com.dji.flighthubsdk.bean.InfraredFillLightEnableParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.InfraredFillLightEnableParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeInfraredFillLightEnableParams(params);
            final com.dji.flighthubsdk.generated.InfraredFillLightEnableParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setInfraredFillLightEnableWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetInfraredFillLightEnable error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置黑白夜视
     * @implNote 链路: DRC | method: drc_camera_night_vision_enable
     */
    public void drcSetCameraNightVisionEnable(com.dji.flighthubsdk.bean.CameraNightVisionEnableParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraNightVisionEnableParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraNightVisionEnableParams(params);
            final com.dji.flighthubsdk.generated.CameraNightVisionEnableParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraNightVisionEnableWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraNightVisionEnable error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 重置相机设置
     * @implNote 链路: DRC | method: drc_camera_settings_reset
     */
    public void drcResetCameraSettings(com.dji.flighthubsdk.bean.CameraSettingsResetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraSettingsResetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraSettingsResetParams(params);
            final com.dji.flighthubsdk.generated.CameraSettingsResetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.resetCameraSettingsWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcResetCameraSettings error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    // ── Other Camera DRC ──

    /**
     * 设置降噪级别
     * @implNote 链路: DRC | method: drc_camera_denoise_level_set
     */
    public void drcSetCameraDenoiseLevelSet(com.dji.flighthubsdk.bean.CameraDenoiseLevelSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraDenoiseLevelSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraDenoiseLevelSetParams(params);
            final com.dji.flighthubsdk.generated.CameraDenoiseLevelSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraDenoiseLevelSetWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraDenoiseLevelSet error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置视频分辨率
     * @implNote 链路: DRC | method: drc_video_resolution_set
     */
    public void drcSetVideoResolution(com.dji.flighthubsdk.bean.VideoResolutionSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.VideoResolutionSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeVideoResolutionSetParams(params);
            final com.dji.flighthubsdk.generated.VideoResolutionSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setVideoResolutionWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetVideoResolution error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置照片格式
     * @implNote 链路: DRC | method: drc_camera_photo_format_set
     */
    public void drcSetCameraPhotoFormat(com.dji.flighthubsdk.bean.CameraPhotoFormatSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.CameraPhotoFormatSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeCameraPhotoFormatSetParams(params);
            final com.dji.flighthubsdk.generated.CameraPhotoFormatSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setCameraPhotoFormatWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetCameraPhotoFormat error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置预录制状态
     * @implNote 链路: DRC | method: drc_pre_recorded_status_set
     */
    public void drcSetPreRecordedStatus(com.dji.flighthubsdk.bean.PreRecordedStatusSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.PreRecordedStatusSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativePreRecordedStatusSetParams(params);
            final com.dji.flighthubsdk.generated.PreRecordedStatusSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setPreRecordedStatusWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetPreRecordedStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置预录制时长
     * @implNote 链路: DRC | method: drc_pre_recorded_time_set
     */
    public void drcSetPreRecordedTime(com.dji.flighthubsdk.bean.PreRecordedTimeSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.PreRecordedTimeSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativePreRecordedTimeSetParams(params);
            final com.dji.flighthubsdk.generated.PreRecordedTimeSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setPreRecordedTimeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetPreRecordedTime error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置倒计时动作
     * @implNote 链路: DRC | method: drc_count_down_action_set
     */
    public void drcSetCountDownAction(int countDownType, int actionType, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAction(callback, cb -> this.cameraControlModule.setCountDownActionWrapper(countDownType, actionType, cb));
    }
    /**
     * 设置辅助画面模式
     * @implNote 链路: DRC | method: drc_assistant_video_mode_set
     */
    public void drcSetAssistantVideoMode(int mode, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAction(callback, cb -> this.cameraControlModule.setAssistantVideoModeWrapper(mode, cb));
    }
    /**
     * 设置辅助画面开关
     * @implNote 链路: DRC | method: drc_assistant_video_switch_set
     */
    public void drcSetAssistantVideoSwitch(int sw, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAction(callback, cb -> this.cameraControlModule.setAssistantVideoSwitchWrapper(sw, cb));
    }
    /**
     * 设置去雾模式
     * @implNote 链路: DRC | method: drc_algorithm_dehaze_mode_set
     */
    public void drcSetAlgorithmDehazeMode(com.dji.flighthubsdk.bean.AlgorithmDehazeModeSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.AlgorithmDehazeModeSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeAlgorithmDehazeModeSetParams(params);
            final com.dji.flighthubsdk.generated.AlgorithmDehazeModeSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setAlgorithmDehazeModeWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetAlgorithmDehazeMode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }
    /**
     * 设置去雾级别
     * @implNote 链路: DRC | method: drc_algorithm_dehaze_level_set
     */
    public void drcSetAlgorithmDehazeLevel(com.dji.flighthubsdk.bean.AlgorithmDehazeLevelSetParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.AlgorithmDehazeLevelSetParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeAlgorithmDehazeLevelSetParams(params);
            final com.dji.flighthubsdk.generated.AlgorithmDehazeLevelSetParams np = nativeParams;
            invokeDrcAction(callback, cb -> this.cameraControlModule.setAlgorithmDehazeLevelWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] drcSetAlgorithmDehazeLevel error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    // ===================== EA340 消息推送补全（Phase D） =====================

    /**
     * 订阅飞机相机水印设置(WebSocket topic_drone_camera_watermark_settings)
     * 注意：与 DRC 侧 {@code listenDrcCameraWatermarkSettings} 不同，不要混用。
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_drone_camera_watermark_settings
     */
    public int listenDroneCameraWatermarkSettings(FlightHubCallback.DroneCameraWatermarkSettingsPushCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneCameraWatermarkSettingsPushCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneCameraWatermarkSettingsPushCallback() {
                        @Override
                        public void onDroneCameraWatermarkSettingsPush(com.dji.flighthubsdk.generated.DroneCameraWatermarkSettingsPush msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneCameraWatermarkSettingsPush(msg));
                            }
                        }
                    };
            int listenId = this.cameraControlModule.listenDroneCameraWatermarkSettingsWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] listenDroneCameraWatermarkSettings error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅飞机相机水印设置(WebSocket) */
    public void cancelListenDroneCameraWatermarkSettings(int listenId) {
        try {
            this.cameraControlModule.cancelListenDroneCameraWatermarkSettingsWrapper(listenId);
        } catch (Exception e) {
            Logger.error("CameraControlModule", "[" + sn + "] cancelListenDroneCameraWatermarkSettings error: " + e.getMessage(), e);
        }
    }
}
