// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.bean.FrameInfo;
import com.dji.flighthubsdk.generated.IPullStreamModule;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.ByteVector;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拉流模块接口，负责从指定地址拉取 WebRTC 流，不包含业务逻辑。
 */
public class PullStreamModule {
    
    private IPullStreamModule pullStreamModule;
    
    // 回调引用容器，防止垃圾回收
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
    
    // 存储回调引用，防止被垃圾回收
    private ConcurrentHashMap<Integer, CallbackRefContainer> startCallbackRef = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, CallbackRefContainer> errorCallbackRef = new ConcurrentHashMap<>();
    
    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    /**
     * 构造拉流模块
     * @param pullStreamModule 底层拉流模块接口
     */
    public PullStreamModule(IPullStreamModule pullStreamModule) {
        this.pullStreamModule = pullStreamModule;
    }

    /**
     * 开始拉流
     * @param streamUrl 拉流地址（支持 WHEP 协议）
     * @param callback 启动结果回调
     * @implNote 链路: SDK 内部 | 通过 RTC SDK 建立 WebRTC 连接并拉流
     */
    public void start(String streamUrl, PullStreamStartCallback callback) {
        if (pullStreamModule == null) {
            Logger.warn("PullStreamModule", "PullStream module is null");
            if (callback != null) {
                callback.onPullStreamStart(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
            }
            return;
        }
        
        if (streamUrl == null || streamUrl.isEmpty()) {
            Logger.warn("PullStreamModule", "Stream URL is null or empty");
            if (callback != null) {
                callback.onPullStreamStart(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_INVALID_PARAM));
            }
            return;
        }
        
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.PullStreamStartCallback swigCallback = new com.dji.flighthubsdk.generated.PullStreamStartCallback() {
                @Override
                public void onPullStreamStart(com.dji.flighthubsdk.generated.FlightHubSdkErrorCode err_code) {
                    try {
                        if (callback != null) {
                            callback.onPullStreamStart(new com.dji.flighthubsdk.bean.FlightHubError(err_code));
                        }
                    } catch (Exception e) {
                        Logger.error("PullStreamModule", "Start callback error: " + e.getMessage(), e);
                    } finally {
                        startCallbackRef.remove(id);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护，防止多线程并发时id冲突
            synchronized (startCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用
                startCallbackRef.put(id, container);
                
                // 调用SWIG生成的JNI方法
                pullStreamModule.startWrapper(streamUrl, swigCallback);
            }
            Logger.info("PullStreamModule", "Start pull stream: " + streamUrl);
        } catch (Exception e) {
            Logger.error("PullStreamModule", "Start pull stream error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onPullStreamStart(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.UNKNOWN));
            }
        }
    }

    /**
     * 停止拉流
     * @implNote 链路: SDK 内部 | 断开 WebRTC 连接
     */
    public void stop() {
        if (pullStreamModule == null) {
            Logger.warn("PullStreamModule", "PullStream module is null");
            return;
        }
        
        try {
            pullStreamModule.stop();
            Logger.info("PullStreamModule", "Stop pull stream");
        } catch (Exception e) {
            Logger.error("PullStreamModule", "Stop pull stream error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅流视频帧数据
     * @param callback 帧数据回调，每帧触发一次
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | RTC SDK 视频帧回调
     */
    public int subscribeStream(PullStreamDataCallback callback) {
        if (pullStreamModule == null) {
            Logger.warn("PullStreamModule", "PullStream module is null");
            return -1;
        }
        
        if (callback == null) {
            Logger.warn("PullStreamModule", "Callback is null");
            return -1;
        }
        
        try {
            com.dji.flighthubsdk.generated.PullStreamDataCallback swigCallback = new com.dji.flighthubsdk.generated.PullStreamDataCallback() {
                @Override
                public void onPullStreamData(com.dji.flighthubsdk.generated.ByteVector data, com.dji.flighthubsdk.generated.FrameInfo frameInfo) {
                    try {
                        // 将ByteVector转换为byte[]
                        byte[] byteData = new byte[data.size()];
                        for (int i = 0; i < data.size(); i++) {
                            Short value = data.get(i);
                            if (value != null) {
                                byteData[i] = value.byteValue();
                            }
                        }
                        if (callback != null) {
                            callback.onPullStreamData(byteData, FlightHubSdkConvert.toFrameInfo(frameInfo));
                        }
                    } catch (Exception e) {
                        Logger.error("PullStreamModule", "Subscribe stream data callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int subscribeId = pullStreamModule.subscribeStreamWrapper(swigCallback);
            if (subscribeId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
                Logger.info("PullStreamModule", "Subscribe stream data, subscribe ID: " + subscribeId);
            }
            return subscribeId;
        } catch (Exception e) {
            Logger.error("PullStreamModule", "Subscribe stream data error: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 取消订阅流视频帧数据
     * @param listenId 订阅时返回的监听 ID
     */
    public void unSubscribeStream(int listenId) {
        if (pullStreamModule == null) {
            Logger.warn("PullStreamModule", "PullStream module is null");
            return;
        }
        
        try {
            pullStreamModule.unSubscribeStream(listenId);
            Logger.info("PullStreamModule", "Unsubscribe stream data, listen ID: " + listenId);
        } catch (Exception e) {
            Logger.error("PullStreamModule", "Unsubscribe stream data error: " + e.getMessage(), e);
        }
    }

    /**
     * 设置错误回调
     * @param callback 错误回调（error_code + error_message）
     */
    public void setErrorCallback(PullStreamErrorCallback callback) {
        if (pullStreamModule == null) {
            Logger.warn("PullStreamModule", "PullStream module is null");
            return;
        }
        
        try {
            com.dji.flighthubsdk.generated.PullStreamErrorCallback swigCallback = new com.dji.flighthubsdk.generated.PullStreamErrorCallback() {
                @Override
                public void onPullStreamError(int errorCode, String errorMessage) {
                    try {
                        if (callback != null) {
                            callback.onPullStreamError(errorCode, errorMessage);
                        }
                    } catch (Exception e) {
                        Logger.error("PullStreamModule", "Error callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 先创建并保存CallbackRefContainer，确保swigCallback对象有强引用，防止在传递给C++端时被GC
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            
            // 使用同步块保护
            synchronized (errorCallbackRef) {
                // 先保存到Map中，确保swigCallback有强引用（使用固定key 0，因为错误回调是全局的）
                errorCallbackRef.put(0, container);
                
                // 调用SWIG生成的JNI方法
                pullStreamModule.setErrorCallbackWrapper(swigCallback);
            }
            Logger.info("PullStreamModule", "Set error callback");
        } catch (Exception e) {
            Logger.error("PullStreamModule", "Set error callback error: " + e.getMessage(), e);
        }
    }

    /**
     * 查询是否正在拉流
     * @return true 表示正在拉流
     */
    public boolean isStreaming() {
        if (pullStreamModule == null) {
            Logger.warn("PullStreamModule", "PullStream module is null");
            return false;
        }
        
        try {
            return pullStreamModule.isStreaming();
        } catch (Exception e) {
            Logger.error("PullStreamModule", "Get streaming status error: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取当前流信息
     * @return 流信息字符串
     */
    public String getStreamInfo() {
        if (pullStreamModule == null) {
            Logger.warn("PullStreamModule", "PullStream module is null");
            return "";
        }
        
        try {
            return pullStreamModule.getStreamInfo();
        } catch (Exception e) {
            Logger.error("PullStreamModule", "Get stream info error: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * 拉流启动回调接口
     */
    public interface PullStreamStartCallback {
        /**
         * 拉流启动结果回调
         * @param error 错误信息；成功时 error code 为 0
         */
        void onPullStreamStart(com.dji.flighthubsdk.bean.FlightHubError error);
    }

    /**
     * 拉流数据回调接口
     */
    public interface PullStreamDataCallback {
        /**
         * 拉流视频帧数据回调，每帧触发一次
         * @param data 视频帧原始字节数据
         * @param frameInfo 帧元信息（编码格式、宽高、时间戳等）
         */
        void onPullStreamData(byte[] data, FrameInfo frameInfo);
    }

    /**
     * 拉流错误回调接口
     */
    public interface PullStreamErrorCallback {
        /**
         * 拉流过程中发生错误时回调
         * @param errorCode 错误码
         * @param errorMessage 错误描述信息
         */
        void onPullStreamError(int errorCode, String errorMessage);
    }
}
