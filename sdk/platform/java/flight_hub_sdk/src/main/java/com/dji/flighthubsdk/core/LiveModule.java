// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.ILiveModule;
import com.dji.flighthubsdk.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 直播模块接口，提供直播流的启停、视频帧订阅、SEI 数据解析、画质设置等功能。
 */
public class LiveModule {
    
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
    
    private ILiveModule liveModule;
    private String deviceSn; // 设备序列号
    
    // 用于异步执行回调的线程池，避免阻塞 C++ 线程
    // 在 start() 时初始化，在 stop() 时关闭
    private volatile ExecutorService callbackExecutor;
    
    /**
     * 构造直播模块
     * @param liveModule 底层直播模块接口
     * @param deviceSn 设备序列号
     */
    public LiveModule(ILiveModule liveModule, String deviceSn) {
        this.liveModule = liveModule;
        this.deviceSn = deviceSn;
        this.callbackExecutor = null;
    }
    
    /**
     * 初始化回调线程池（如果尚未初始化）
     */
    private synchronized ExecutorService getOrCreateCallbackExecutor() {
        if (callbackExecutor == null || callbackExecutor.isShutdown()) {
            // 创建线程池用于异步执行回调，使用有界队列防止内存溢出
            // 使用固定大小的线程池，线程数根据 CPU 核心数动态调整
            int threadPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
            callbackExecutor = Executors.newFixedThreadPool(threadPoolSize, new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "LiveModule-Callback-" + deviceSn + "-" + threadNumber.getAndIncrement());
                    t.setDaemon(true); // 设置为守护线程
                    return t;
                }
            });
            
            Logger.info("LiveModule", "[" + deviceSn + "] Created callback executor with " + threadPoolSize + " threads");
        }
        return callbackExecutor;
    }

    // Action Callback
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startWithURLCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> stopWithURLCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setLiveQualityCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }
    
    /**
     * 开始直播拉流
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-live/api/v2/stream/start（获取 RTC 频道信息后通过 RTC SDK 拉流）
     */
    public void start(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            Logger.info("LiveModule", "[" + deviceSn + "] Starting live stream, callback: " + (callback != null ? "set" : "null"));

            // 初始化回调线程池
            getOrCreateCallbackExecutor();

            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (err.getError_code() == FlightHubSdkErrorCode.SUCCESS) {
                            Logger.info("LiveModule", "[" + deviceSn + "] Live stream started successfully");
                        } else {
                            Logger.warn("LiveModule", "[" + deviceSn + "] Live stream start failed, error code: " + err.getError_code());
                        }
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] Start callback error: " + e.getMessage(), e);
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
                this.liveModule.startWrapper(swigCallback);
            }

        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] start error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 获取直播拉流地址（不自动拉流）。
     *
     * 与 {@link #start} 不同，本方法只调用后端接口获取拉流地址，不进行 RTC 通道建立和自动拉流。
     * 用户可自行使用返回的 url 进行 WHEP/RTC 播放。
     *
     * @attention 由于 SDK 内部未建立 RTC 通道，调用本方法后以下接口将不起作用：
     *            {@link #stop}、{@link #subscribeLiveStream}、以及 SEI 订阅相关接口。
     *
     * @param callback 结果回调，成功时返回拉流地址信息
     * @implNote 链路: HTTP | POST /pf-live/api/v2/stream/start
     */
    public void startWithURL(FlightHubCallback.LiveStreamStartCallback callback) {
        try {
            Logger.info("LiveModule", "[" + deviceSn + "] startWithURL, callback: " + (callback != null ? "set" : "null"));

            getOrCreateCallbackExecutor();

            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.LiveStreamStartCallback swigCallback = new com.dji.flighthubsdk.generated.LiveStreamStartCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.LiveStreamStartResult result) {
                    try {
                        if (err.getError_code() == FlightHubSdkErrorCode.SUCCESS) {
                            Logger.info("LiveModule", "[" + deviceSn + "] startWithURL success, url_type: " + result.getUrl_type());
                        } else {
                            Logger.warn("LiveModule", "[" + deviceSn + "] startWithURL failed, error code: " + err.getError_code());
                        }
                        if (callback != null) {
                            com.dji.flighthubsdk.bean.FlightHubError beanErr = new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(),
                                (int) err.getDevice_code(), err.getServer_message());
                            com.dji.flighthubsdk.bean.LiveStreamStartResult beanResult = null;
                            if (err.getError_code() == FlightHubSdkErrorCode.SUCCESS) {
                                beanResult = new com.dji.flighthubsdk.bean.LiveStreamStartResult(
                                    result.getExpire_ts(), result.getUrl_type(), result.getUrl(),
                                    result.getPublish_uid(), result.getProvider_url());
                            }
                            callback.onResult(beanErr, beanResult);
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] startWithURL callback error: " + e.getMessage(), e);
                    } finally {
                        startWithURLCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (startWithURLCallbackRef) {
                startWithURLCallbackRef.put(id, container);
                this.liveModule.startWithURLWrapper(swigCallback);
            }

        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] startWithURL error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
            }
        }
    }

    /**
     * 停止直播拉流
     * @implNote 链路: SDK 内部 | 断开 RTC 频道连接
     */
    public void stop() {
        try {
            Logger.info("LiveModule", "[" + deviceSn + "] Stopping live stream");
            this.liveModule.stop();
            Logger.info("LiveModule", "[" + deviceSn + "] Live stream stopped successfully");
            
            // 关闭回调线程池
            shutdownCallbackExecutor();
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] stop error: " + e.getMessage(), e);
        }
    }

    /**
     * 停止设备推流（后端停流）。
     *
     * 与 {@link #stop} 不同，本方法调用后端接口通知服务端停止设备推流，
     * 适用于 {@link #startWithURL} 场景下用户自行停止推流。
     *
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-live/api/v1/stream/stop
     */
    public void stopWithURL(FlightHubCallback.ResultCallbackWithError callback) {
        try {
            Logger.info("LiveModule", "[" + deviceSn + "] stopWithURL, callback: " + (callback != null ? "set" : "null"));

            getOrCreateCallbackExecutor();

            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (err.getError_code() == FlightHubSdkErrorCode.SUCCESS) {
                            Logger.info("LiveModule", "[" + deviceSn + "] stopWithURL success");
                        } else {
                            Logger.warn("LiveModule", "[" + deviceSn + "] stopWithURL failed, error code: " + err.getError_code());
                        }
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] stopWithURL callback error: " + e.getMessage(), e);
                    } finally {
                        stopWithURLCallbackRef.remove(id);
                    }
                }
            };

            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (stopWithURLCallbackRef) {
                stopWithURLCallbackRef.put(id, container);
                this.liveModule.stopWithURLWrapper(swigCallback);
            }

        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] stopWithURL error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }
    
    /**
     * 关闭回调线程池
     */
    private synchronized void shutdownCallbackExecutor() {
        if (callbackExecutor != null && !callbackExecutor.isShutdown()) {
            try {
                Logger.info("LiveModule", "[" + deviceSn + "] Shutting down callback executor");
                callbackExecutor.shutdown();
                // 等待一段时间让正在执行的任务完成
                if (!callbackExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    Logger.warn("LiveModule", "[" + deviceSn + "] Callback executor did not terminate gracefully, forcing shutdown");
                    callbackExecutor.shutdownNow();
                }
                Logger.info("LiveModule", "[" + deviceSn + "] Callback executor shutdown completed");
            } catch (Exception e) {
                Logger.error("LiveModule", "[" + deviceSn + "] Error shutting down callback executor: " + e.getMessage(), e);
                if (callbackExecutor != null) {
                    callbackExecutor.shutdownNow();
                }
            }
        }
    }

    /**
     * 订阅直播视频帧数据
     * @param callback 帧数据回调，每帧触发一次
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | RTC SDK 视频帧回调
     */
    public int subscribeLiveStream(FlightHubCallback.LiveStreamCallback callback) {
        try {
            Logger.info("LiveModule", "[" + deviceSn + "] Subscribing to live stream, callback: " + (callback != null ? "set" : "null"));
            com.dji.flighthubsdk.generated.LiveStreamCallback swigLiveStreamCallback = new com.dji.flighthubsdk.generated.LiveStreamCallback() {
                private int frameCount = 0;
                private long totalBytes = 0;
                
                @Override
                public void onLiveStream(com.dji.flighthubsdk.generated.ByteVector data, com.dji.flighthubsdk.generated.FrameInfo frame_info) {
                    try {
                        // 添加空值检查和同步保护
                        if (data == null || frame_info == null) {
                            Logger.warn("LiveModule", "[" + deviceSn + "] Null data or frame_info received");
                            return;
                        }
                        
                        int dataSize = data.size();
                        
                        if (dataSize <= 0) {
                            Logger.warn("LiveModule", "[" + deviceSn + "] Invalid data size: " + dataSize);
                            return;
                        }
                        
                        // 添加大小限制，防止内存溢出
                        if (dataSize > 10 * 1024 * 1024) { // 10MB limit
                            Logger.warn("LiveModule", "[" + deviceSn + "] Data size too large: " + dataSize);
                            return;
                        }
                        
                        // 统计信息：只在关键帧时记录，减少日志量
                        frameCount++;
                        totalBytes += dataSize;
                        boolean isKeyFrame = frame_info != null && frame_info.getIs_key_frame();
                        
                        if (isKeyFrame) {
                            Logger.info("LiveModule", "[" + deviceSn + "] Received video frame #" + frameCount + 
                                ", size: " + dataSize + " bytes, is_key_frame: true, total_bytes: " + totalBytes);
                        }
                        
                        // 将SWIG的ByteVector转换为Java byte[]
                        byte[] byteArray = new byte[dataSize];
                        for (int i = 0; i < dataSize; i++) {
                            Short value = data.get(i);
                            if (value != null) {
                                byteArray[i] = value.byteValue();
                            }
                        }

                        // 在进异步队列前拷贝帧信息，用户回调不持有 SWIG FrameInfo
                        final com.dji.flighthubsdk.bean.FrameInfo frameBean = FlightHubSdkConvert.toFrameInfo(frame_info);
                        
                        // 记录 C++ 线程信息
                        Thread nativeThread = Thread.currentThread();
                        final String nativeThreadName = nativeThread.getName();
                        final long nativeThreadId = nativeThread.getId();
                        
                        // 异步调用Java回调，避免阻塞 C++ 线程
                        if (callback != null) {
                            // 记录回调开始时间
                            final long callbackStartTime = System.currentTimeMillis();
                            final int currentFrameCount = frameCount;
                            final boolean currentIsKeyFrame = frameBean.isKeyFrame();
                            final int currentDataSize = dataSize;
                            
                            // 提交到线程池异步执行
                            getOrCreateCallbackExecutor().submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        // 记录回调执行线程信息
                                        Thread callbackThread = Thread.currentThread();
                                        String callbackThreadName = callbackThread.getName();
                                        long callbackThreadId = callbackThread.getId();
                                        
                                        if (currentIsKeyFrame) {
                                            Logger.info("LiveModule", "[" + deviceSn + "] Calling Java callback for frame #" + currentFrameCount + 
                                                       ", size: " + currentDataSize + ", from native thread: " + nativeThreadName + " (ID: " + nativeThreadId + 
                                                       "), callback thread: " + callbackThreadName + " (ID: " + callbackThreadId + ")");
                                        }
                                        
                                        // 执行用户回调
                                        callback.onResult(byteArray, frameBean);
                                        
                                        // 计算并记录回调耗时
                                        long callbackDuration = System.currentTimeMillis() - callbackStartTime;
                                        if (callbackDuration > 100) { // 如果耗时超过 100ms，记录警告日志
                                            Logger.warn("LiveModule", "[" + deviceSn + "] Java callback took " + callbackDuration + "ms for frame #" + currentFrameCount + 
                                                       ", size: " + currentDataSize + ", this may block C++ thread if called synchronously");
                                        } else if (currentIsKeyFrame && callbackDuration > 10) { // 关键帧且耗时超过 10ms 也记录
                                            Logger.info("LiveModule", "[" + deviceSn + "] Java callback took " + callbackDuration + "ms for frame #" + currentFrameCount);
                                        }
                                    } catch (Exception e) {
                                        Logger.error("LiveModule", "[" + deviceSn + "] Error in async callback execution: " + e.getMessage(), e);
                                    }
                                }
                            });
                        } else {
                            if (isKeyFrame) {
                                Logger.warn("LiveModule", "[" + deviceSn + "] Java callback is null, data not delivered to user, frame #" + frameCount + ", size: " + dataSize);
                            }
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] Live stream callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.liveModule.subscribeLiveStreamWrapper(swigLiveStreamCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigLiveStreamCallback.swigReleaseOwnership();
            }
            Logger.info("LiveModule", "[" + deviceSn + "] Subscribed to live stream successfully, listenId: " + listenId);
            return listenId;
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] subscribeLiveStream error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅直播视频帧数据
     * @param listenId 订阅时返回的监听 ID
     * @implNote 链路: SDK 内部 | 移除帧数据回调
     */
    public void unSubscribeLiveStream(int listenId) {
        try {
            this.liveModule.unSubscribeLiveStream(listenId);
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] unSubscribeLiveStream error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 AI 检测 SEI 信息（目标检测框）
     * @param callback AI SEI 信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 AI 检测数据
     */
    public int subscribeAiSeiInfo(FlightHubCallback.AiSeiInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AiSeiInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AiSeiInfoCallback() {
                @Override
                public void onAiSeiInfo(com.dji.flighthubsdk.generated.AiSeiInfo ai_sei_info) {
                    try {
                        if (callback != null) {
                            // 立即拷贝 targets（含多元素），用户不持有 AiDetectionTargetVector / SWIG 元素代理
                            callback.onResult(FlightHubSdkConvert.toAiSeiInfo(ai_sei_info));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] AI SEI info callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.liveModule.subscribeAiSeiInfoWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] subscribeAiSeiInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 AI 检测 SEI 信息
     * @param listenId 订阅时返回的监听 ID
     */
    public void unSubscribeAiSeiInfo(int listenId) {
        try {
            this.liveModule.unSubscribeAiSeiInfo(listenId);
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] unSubscribeAiSeiInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 AR SEI 信息（相机姿态与位置）
     * @param callback AR SEI 信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 AR 数据
     */
    public int subscribeArSeiInfo(FlightHubCallback.ArSeiInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ArSeiInfoCallback swigCallback = new com.dji.flighthubsdk.generated.ArSeiInfoCallback() {
                @Override
                public void onArSeiInfo(com.dji.flighthubsdk.generated.ArSeiInfo ar_sei_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toArSeiInfo(ar_sei_info));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] AR SEI info callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.liveModule.subscribeArSeiInfoWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] subscribeArSeiInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 AR SEI 信息
     * @param listenId 订阅时返回的监听 ID
     */
    public void unSubscribeArSeiInfo(int listenId) {
        try {
            this.liveModule.unSubscribeArSeiInfo(listenId);
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] unSubscribeArSeiInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 FOV SEI 信息（画幅尺寸与焦距，见 FovSeiInfo）
     * @param callback FOV SEI 信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 FOV 数据
     */
    public int subscribeFovSeiInfo(FlightHubCallback.FovSeiInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.FovSeiInfoCallback swigCallback = new com.dji.flighthubsdk.generated.FovSeiInfoCallback() {
                @Override
                public void onFovSeiInfo(com.dji.flighthubsdk.generated.FovSeiInfo fov_sei_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFovSeiInfo(fov_sei_info));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] FOV SEI info callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.liveModule.subscribeFovSeiInfoWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] subscribeFovSeiInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 FOV SEI 信息
     * @param listenId 订阅时返回的监听 ID
     */
    public void unSubscribeFovSeiInfo(int listenId) {
        try {
            this.liveModule.unSubscribeFovSeiInfo(listenId);
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] unSubscribeFovSeiInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅智能辅助 SEI 信息（自动朝向等）
     * @param callback 智能辅助 SEI 信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 Assistant 数据
     */
    public int subscribeAssistantSeiInfo(FlightHubCallback.AssistantSeiInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AssistantSeiInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AssistantSeiInfoCallback() {
                @Override
                public void onAssistantSeiInfo(com.dji.flighthubsdk.generated.AssistantSeiInfo assistant_sei_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAssistantSeiInfo(assistant_sei_info));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] Assistant SEI info callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.liveModule.subscribeAssistantSeiInfoWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] subscribeAssistantSeiInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅智能辅助 SEI 信息
     * @param listenId 订阅时返回的监听 ID
     */
    public void unSubscribeAssistantSeiInfo(int listenId) {
        try {
            this.liveModule.unSubscribeAssistantSeiInfo(listenId);
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] unSubscribeAssistantSeiInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅 EIS（电子防抖）SEI 信息（裁剪区域坐标）
     * @param callback EIS SEI 信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | 从直播帧 SEI NAL 中解析 EIS 数据
     */
    public int subscribeEisSeiInfo(FlightHubCallback.EisSeiInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.EisSeiInfoCallback swigCallback = new com.dji.flighthubsdk.generated.EisSeiInfoCallback() {
                @Override
                public void onEisSeiInfo(com.dji.flighthubsdk.generated.EisSeiInfo eis_sei_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toEisSeiInfo(eis_sei_info));
                        }
                    } catch (Exception e) {
                        Logger.error("LiveModule", "[" + deviceSn + "] EIS SEI info callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            // 调用SWIG生成的JNI方法
            int listenId = this.liveModule.subscribeEisSeiInfoWrapper(swigCallback);
            if (listenId >= 0) {
                // 成功后由 C++ 端 shared_ptr 持有回调，释放 Java 端所有权
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] subscribeEisSeiInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅 EIS SEI 信息
     * @param listenId 订阅时返回的监听 ID
     */
    public void unSubscribeEisSeiInfo(int listenId) {
        try {
            this.liveModule.unSubscribeEisSeiInfo(listenId);
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] unSubscribeEisSeiInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 设置直播画质
     * @param params 画质设置参数（设备 SN、摄像头、视频流标识、画质等级）
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /pf-live/api/v1/stream/set-quality
     */
    public void setLiveQuality(com.dji.flighthubsdk.bean.LiveQualitySettingParams params, FlightHubCallback.ResultCallbackWithError callback) {
        if (liveModule == null) {
            Logger.error("LiveModule", "[" + deviceSn + "] setLiveQuality: liveModule is null");
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
            return;
        }
        com.dji.flighthubsdk.generated.LiveQualitySettingParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeLiveQualitySettingParams(params);

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
                        Logger.error("LiveModule", "[" + deviceSn + "] setLiveQuality callback error: " + e.getMessage(), e);
                    } finally {
                        setLiveQualityCallbackRef.remove(id);
                    }
                }
            };
            synchronized (setLiveQualityCallbackRef) {
                setLiveQualityCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.liveModule.setLiveQualityWrapper(nativeParams, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("LiveModule", "[" + deviceSn + "] setLiveQuality error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) {
                nativeParams.delete();
            }
        }
    }
}
