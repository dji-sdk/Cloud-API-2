// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveModule;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.bean.FrameInfo;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.LiveInfo;

/**
 * 无人机推流测试模块
 * 基于飞机在线状态自动控制拉流和RTMP推流
 * 解决WebRTC WHEP协议热切换问题
 */
public class DroneStreamingTest {
    
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // 静态回调对象，避免被GC
    private static FlightHubCallback.DroneOnlineStatusCallback staticDroneOnlineStatusCallback;
    private static FlightHubCallback.LinkStatusCallback staticLinkStatusCallback;
    private static FlightHubCallback.LiveAvailableRangeCallback staticLiveAvailableRangeCallback;
    private static FlightHubCallback.ResultCallbackWithError staticResultCallback;
    private static FlightHubCallback.LiveStreamCallback staticLiveStreamCallback;
    
    // 核心组件
    private Drone drone;
    private LiveModule droneLiveModule;
    private FFmpegPipeStreamer droneStreamer;
    private Thread streamMonitorThread;
    
    // 状态控制
    private final AtomicBoolean isDroneOnline = new AtomicBoolean(false);
    private final AtomicBoolean isStreaming = new AtomicBoolean(false);
    private final AtomicBoolean isStopping = new AtomicBoolean(false);
    private final AtomicBoolean isRestarting = new AtomicBoolean(false);
    private final Object streamLock = new Object();
    
    // 流健康监控
    private final AtomicLong lastValidDataTime = new AtomicLong(0);
    private Thread streamHealthMonitorThread;
    private static final long STREAM_HEALTH_CHECK_INTERVAL = 1000; // 检查间隔：1秒
    private static final long STREAM_EMPTY_TIMEOUT = 60000; // 空数据超时阈值：60秒
    
    // 文件输出
    private FileOutputStream droneStreamOutput = null;
    private String outputDirectory = "stream_data";
    private long droneTotalBytes = 0;
    private long droneFrameCount = 0;
    
    // 异步文件写入
    private ExecutorService fileWriteExecutor = null;
    private static final int MAX_QUEUE_SIZE = 100;
    private final AtomicLong droppedFrames = new AtomicLong(0);
    
    // 推流配置
    private String targetPayloadIndex = "99-0-0";
    private String rtmpUrl;
    private int rtmpFps = 30;
    private int rtmpWidth = 1920;
    private int rtmpHeight = 1080;
    private int rtmpBitrate = 2000000;
    
    // 监听ID
    private int droneLiveStreamListenId = -1;
    
    /**
     * 构造函数
     * @param drone 无人机实例
     * @param rtmpUrl RTMP推流地址
     * @param rtmpFps 推流帧率
     * @param rtmpWidth 推流宽度
     * @param rtmpHeight 推流高度
     * @param rtmpBitrate 推流码率
     */
    public DroneStreamingTest(Drone drone, String rtmpUrl, int rtmpFps, int rtmpWidth, int rtmpHeight, int rtmpBitrate) {
        this.drone = drone;
        this.rtmpUrl = rtmpUrl;
        this.rtmpFps = rtmpFps;
        this.rtmpWidth = rtmpWidth;
        this.rtmpHeight = rtmpHeight;
        this.rtmpBitrate = rtmpBitrate;
    }
    
    /**
     * 启动推流测试
     */
    public void start() {
        log("INFO", "start", "启动无人机推流测试...");
        
        try {
            // 初始化文件输出
            initStreamOutputs();
            
            // 启动飞机状态监听
            startDroneStatusMonitoring();
            
            log("INFO", "start", "推流测试已启动，将根据飞机在线状态自动控制拉流和推流");
            
        } catch (Exception e) {
            log("ERROR", "start", "启动推流测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 停止推流测试
     */
    public void stop() {
        log("INFO", "stop", "停止无人机推流测试...");
        
        synchronized (streamLock) {
            isStreaming.set(false);
        }
        
        try {
            // 检查是否启用安全模式
            String safeMode = System.getProperty("safe.mode", "false");
            if ("true".equals(safeMode)) {
                log("INFO", "stop", "安全模式：跳过JNI清理，直接退出");
                closeStreamOutputs();
                System.exit(0);
                return;
            }
            
            // 停止飞机流
            stopDroneStreaming();
            
            // 等待一段时间确保所有资源都已释放
            Thread.sleep(200);
            
        } catch (Exception e) {
            log("ERROR", "stop", "停止推流过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭文件输出
            closeStreamOutputs();
            
            // 清理静态回调对象
            staticDroneOnlineStatusCallback = null;
            staticLinkStatusCallback = null;
            staticLiveAvailableRangeCallback = null;
            staticResultCallback = null;
            staticLiveStreamCallback = null;
            
            log("INFO", "stop", "推流测试已停止");
        }
    }
    
    /**
     * 启动飞机状态监听
     */
    private void startDroneStatusMonitoring() {
        if (drone == null) {
            log("ERROR", "startDroneStatusMonitoring", "无人机实例为空，无法启动状态监听");
            return;
        }

        log("INFO", "startDroneStatusMonitoring", "启动飞机状态监听...");

        // 创建并保存静态回调对象
        staticDroneOnlineStatusCallback = new FlightHubCallback.DroneOnlineStatusCallback() {
            @Override
            public void onResult(DroneOnlineStatus status) {
                boolean wasOnline = isDroneOnline.get();
                boolean isOnline = status.isOnline();
                isDroneOnline.set(isOnline);
                
                log("INFO", "startDroneStatusMonitoring", "飞机在线状态变化: " + wasOnline + " -> " + isOnline);
                
                synchronized (streamLock) {
                    if (isOnline && !isStreaming.get()) {
                        // 飞机上线且未在推流，启动拉流和推流
                        startDroneStreaming();
                    } else if (!isOnline && isStreaming.get()) {
                        // 飞机离线且正在推流，停止拉流和推流
                        stopDroneStreaming();
                    }
                }
            }
        };
        
        // 监听飞机在线状态
        drone.getDeviceStateModule().listenDroneOnlineStatus(staticDroneOnlineStatusCallback);

        // 创建并保存静态回调对象
        staticLinkStatusCallback = new FlightHubCallback.LinkStatusCallback() {
            @Override
            public void onResult(LinkStatus status) {
                log("INFO", "startDroneStatusMonitoring", "飞机链路状态: " + status.getCtrlLinkStatus());
            }
        };
        
        // 监听链路状态
        drone.getDrcModule().listenLinkStatus(staticLinkStatusCallback);
    }
    
    /**
     * 启动飞机拉流和推流
     */
    private void startDroneStreaming() {
        if (isStreaming.get()) {
            log("WARN", "startDroneStreaming", "流已经在运行，跳过启动");
            return;
        }

        log("INFO", "startDroneStreaming", "飞机上线，启动拉流和推流...");

        try {
            // 创建并保存静态回调对象
            staticLiveAvailableRangeCallback = new FlightHubCallback.LiveAvailableRangeCallback() {
                @Override
                public void onResult(java.util.List<LiveInfo> ranges) {
                    if (ranges == null || ranges.isEmpty()) {
                        log("ERROR", "startDroneStreaming", "获取直播信息失败");
                        return;
                    }

                    // 选择目标负载的直播信息
                    LiveInfo selectedLiveInfo = null;
                    for (LiveInfo range : ranges) {
                        if (targetPayloadIndex.equals(range.getPayloadIndex())) {
                            selectedLiveInfo = range;
                            break;
                        }
                    }
                    if (selectedLiveInfo == null && !ranges.isEmpty()) {
                        selectedLiveInfo = ranges.get(0);
                    }

                    if (selectedLiveInfo != null) {
                        final LiveInfo liveInfo = selectedLiveInfo;
                        log("INFO", "startDroneStreaming", "开始拉取无人机直播流，负载索引: " + liveInfo.getPayloadIndex());
                        
                        // 创建直播模块
                        droneLiveModule = drone.getLiveStream(liveInfo);
                        
                        // 创建并保存静态回调对象
                        staticResultCallback = new FlightHubCallback.ResultCallbackWithError() {
                            @Override
                            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                if (error.isSuccess()) {
                                    log("INFO", "startDroneStreaming", "无人机直播流启动成功");
                                    
                                    // 初始化有效数据时间戳
                                    lastValidDataTime.set(System.currentTimeMillis());
                                    
                                    // 启动RTMP推流
                                    startRTMPStreaming();
                                    
                                    // 订阅直播流数据
                                    subscribeDroneLiveStream();
                                    
                                    // 启动流健康监控
                                    startStreamHealthMonitor();
                                    
                                    synchronized (streamLock) {
                                        isStreaming.set(true);
                                    }
                                } else {
                                    log("ERROR", "startDroneStreaming", "无人机直播流启动失败: " + error);
                                }
                            }
                        };
                        
                        // 启动直播流
                        droneLiveModule.start(staticResultCallback);
                    }
                }
            };
            
            // 获取直播信息
            drone.getLiveDeviceModule().listenLiveAvailableRange(staticLiveAvailableRangeCallback);
        } catch (Exception e) {
            log("ERROR", "startDroneStreaming", "启动拉流失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 启动RTMP推流
     */
    private void startRTMPStreaming() {
        try {
            if (rtmpUrl != null && !rtmpUrl.isEmpty()) {
                droneStreamer = new FFmpegPipeStreamer();
                droneStreamer.start(rtmpUrl, rtmpFps, rtmpWidth, rtmpHeight, rtmpBitrate);
                log("INFO", "startRTMPStreaming", "飞机 RTMP 推流已启动 -> " + rtmpUrl);
                
                // 启动推流状态监控
                startStreamMonitor();
            } else {
                log("INFO", "startRTMPStreaming", "RTMP推流未配置");
            }
        } catch (Exception e) {
            log("ERROR", "startRTMPStreaming", "启动RTMP推流失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 订阅飞机直播流数据
     */
    private void subscribeDroneLiveStream() {
        if (droneLiveModule == null) {
            log("ERROR", "subscribeDroneLiveStream", "直播模块为空");
            return;
        }

        // 创建并保存静态回调对象
        staticLiveStreamCallback = new FlightHubCallback.LiveStreamCallback() {
            @Override
            public void onResult(byte[] data, FrameInfo frameInfo) {
                // 首先检查是否正在停止或已停止
                if (!isStreaming.get() || isStopping.get()) {
                    return;
                }
                
                // 检查数据是否为空
                boolean isEmpty = (data == null || data.length == 0);
                
                if (isEmpty) {
                    // 数据为空，记录日志但不更新时间戳
                    log("WARN", "subscribeDroneLiveStream", "收到无人机直播流数据为空或长度为0");
                    return;
                }
                
                // 收到有效数据，更新时间戳
                lastValidDataTime.set(System.currentTimeMillis());
                
                try {
                    droneFrameCount++;
                    droneTotalBytes += data.length;
                    if (droneFrameCount % 30 == 0) {
                        log("INFO", "subscribeDroneLiveStream", "收到无人机直播流数据 #" + droneFrameCount + 
                            ": " + data.length + " bytes (总计: " + droneTotalBytes + " bytes)");
                    }
                    
                    // 写入文件
                    writeDroneStreamData(data);

                    // 推送到FFmpeg
                    if (droneStreamer != null && droneStreamer.isRunning()) {
                        droneStreamer.pushFrame(data);
                    }
                } catch (Exception e) {
                    log("ERROR", "subscribeDroneLiveStream", "处理流数据时发生异常: " + e.getMessage());
                }
            }
        };
        
        droneLiveStreamListenId = droneLiveModule.subscribeLiveStream(staticLiveStreamCallback);
        
        log("INFO", "subscribeDroneLiveStream", "已订阅无人机直播流数据");
    }
    
    /**
     * 停止飞机拉流和推流
     */
    private void stopDroneStreaming() {
        // 使用同步块确保停止过程的原子性
        synchronized (streamLock) {
            if (!isStreaming.get()) {
                log("WARN", "stopDroneStreaming", "流未在运行，跳过停止");
                return;
            }

            // 防止重复停止
            if (isStopping.get()) {
                log("WARN", "stopDroneStreaming", "正在停止中，跳过重复停止");
                return;
            }

            isStopping.set(true);
            isStreaming.set(false);
        }
        
        log("INFO", "stopDroneStreaming", "飞机离线，停止拉流和推流...");

        try {
            // 1. 先停止RTMP推流（避免数据继续流入）
            if (droneStreamer != null) {
                try {
                    // 使用超时机制停止FFmpeg，避免死锁
                    Thread stopThread = new Thread(() -> {
                        try {
                            droneStreamer.stop();
                        } catch (Exception e) {
                            log("ERROR", "stopDroneStreaming", "FFmpeg停止线程异常: " + e.getMessage());
                        }
                    });
                    stopThread.setDaemon(true); // 设置为守护线程
                    stopThread.start();
                    
                    // 使用更短的超时时间，避免死锁
                    try {
                        stopThread.join(1000); // 最多等待1秒
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log("WARN", "stopDroneStreaming", "等待FFmpeg停止时被中断");
                    }
                    
                    if (stopThread.isAlive()) {
                        log("WARN", "stopDroneStreaming", "FFmpeg停止超时，强制中断");
                        stopThread.interrupt();
                        // 再等待一小段时间让线程清理
                        try {
                            stopThread.join(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    
                    log("INFO", "stopDroneStreaming", "RTMP推流已停止");
                } catch (Exception e) {
                    log("ERROR", "stopDroneStreaming", "停止RTMP推流时出错: " + e.getMessage());
                } finally {
                    droneStreamer = null;
                }
            }

            // 2. 停止推流监控
            stopStreamMonitor();

            // 2.5. 停止流健康监控
            stopStreamHealthMonitor();

            // 3. 取消直播流订阅
            if (droneLiveStreamListenId != -1 && droneLiveModule != null) {
                try {
                    droneLiveModule.unSubscribeLiveStream(droneLiveStreamListenId);
                    log("INFO", "stopDroneStreaming", "已取消订阅无人机直播流");
                    droneLiveStreamListenId = -1;
                    
                    // 等待足够长的时间确保JNI回调完全停止
                    Thread.sleep(500);
                } catch (Exception e) {
                    log("ERROR", "stopDroneStreaming", "取消订阅时出错: " + e.getMessage());
                    droneLiveStreamListenId = -1;
                }
            }

            // 4. 最后停止直播流（给WebRTC和JNI足够时间清理）
            if (droneLiveModule != null) {
                try {
                    // 增加延迟时间，确保所有回调和JNI调用完成
                    Thread.sleep(1000);
                    
                    // 使用超时机制停止直播流，避免死锁
                    Thread stopLiveThread = new Thread(() -> {
                        try {
                            droneLiveModule.stop();
                        } catch (Exception e) {
                            log("ERROR", "stopDroneStreaming", "停止直播流线程异常: " + e.getMessage());
                        }
                    });
                    stopLiveThread.setDaemon(true);
                    stopLiveThread.start();
                    
                    // 等待停止完成，但设置超时
                    try {
                        stopLiveThread.join(2000); // 最多等待2秒
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log("WARN", "stopDroneStreaming", "等待直播流停止时被中断");
                    }
                    
                    if (stopLiveThread.isAlive()) {
                        log("ERROR", "stopDroneStreaming", "直播流停止超时，JNI调用可能死锁，强制退出程序");
                        stopLiveThread.interrupt();
                        // 如果JNI调用死锁，直接退出程序
                        System.exit(1);
                    }
                    
                    log("INFO", "stopDroneStreaming", "无人机直播流已停止");
                    
                    // 再次等待，确保底层资源完全释放
                    Thread.sleep(500);
                    droneLiveModule = null;
                } catch (Exception e) {
                    log("ERROR", "stopDroneStreaming", "停止直播流时出错: " + e.getMessage());
                    droneLiveModule = null;
                }
            }

        } catch (Exception e) {
            log("ERROR", "stopDroneStreaming", "停止推流过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isStopping.set(false);
        }
    }
    
    /**
     * 初始化文件输出
     */
    private void initStreamOutputs() throws IOException {
        // 创建输出目录
        java.io.File outputDir = new java.io.File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // 创建文件输出流
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String droneFileName = outputDirectory + "/drone_stream_" + timestamp + ".h264";
        droneStreamOutput = new FileOutputStream(droneFileName);
        
        // 初始化异步文件写入
        fileWriteExecutor = Executors.newFixedThreadPool(2);
        
        log("INFO", "initStreamOutputs", "文件输出已初始化: " + droneFileName);
        log("INFO", "initStreamOutputs", "异步文件写入已启用，最大队列大小: " + MAX_QUEUE_SIZE);
    }
    
    /**
     * 写入无人机流数据
     */
    private void writeDroneStreamData(byte[] data) {
        // 检查是否正在停止或已停止
        if (!isStreaming.get() || isStopping.get() || droneStreamOutput == null || fileWriteExecutor == null) {
            return;
        }
        
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) fileWriteExecutor;
        if (tpe.isShutdown() || tpe.getQueue().size() >= MAX_QUEUE_SIZE) {
            droppedFrames.incrementAndGet();
            if (droppedFrames.get() % 100 == 0) {
                log("WARN", "writeDroneStreamData", "文件写入队列已满，已丢弃 " + droppedFrames.get() + " 帧");
            }
            return;
        }
        
        fileWriteExecutor.submit(() -> {
            try {
                // 在执行时再次检查状态
                if (isStreaming.get() && !isStopping.get() && droneStreamOutput != null) {
                    droneStreamOutput.write(data);
                    droneStreamOutput.flush();
                }
            } catch (IOException e) {
                if (!isStopping.get()) {
                    log("ERROR", "writeDroneStreamData", "写入无人机流数据失败: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 关闭文件输出
     */
    private void closeStreamOutputs() {
        if (fileWriteExecutor != null) {
            fileWriteExecutor.shutdown();
            try {
                if (!fileWriteExecutor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    fileWriteExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                fileWriteExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (droneStreamOutput != null) {
            try {
                droneStreamOutput.close();
            } catch (IOException e) {
                log("ERROR", "closeStreamOutputs", "关闭无人机流输出失败: " + e.getMessage());
            }
        }
        
        if (droppedFrames.get() > 0) {
            log("INFO", "closeStreamOutputs", "文件写入过程中共丢弃 " + droppedFrames.get() + " 帧");
        }
    }
    
    /**
     * 启动推流监控
     */
    private void startStreamMonitor() {
        if (streamMonitorThread != null && streamMonitorThread.isAlive()) {
            return;
        }
        
        streamMonitorThread = new Thread(() -> {
            while (isStreaming.get()) {
                try {
                    Thread.sleep(5000); // 每5秒检查一次
                    
                    if (droneStreamer != null) {
                        boolean isRunning = droneStreamer.isRunning();
                        log("INFO", "streamMonitor", "推流状态: " + (isRunning ? "运行中" : "已停止"));
                        
                        if (!isRunning) {
                            log("WARN", "streamMonitor", "检测到推流已停止，尝试重启...");
                            // 这里可以添加重启逻辑
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        streamMonitorThread.setDaemon(true);
        streamMonitorThread.start();
        log("INFO", "startStreamMonitor", "推流监控已启动");
    }
    
    /**
     * 停止推流监控
     */
    private void stopStreamMonitor() {
        if (streamMonitorThread != null) {
            streamMonitorThread.interrupt();
            try {
                streamMonitorThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            streamMonitorThread = null;
        }
    }
    
    /**
     * 启动流健康监控
     */
    private void startStreamHealthMonitor() {
        if (streamHealthMonitorThread != null && streamHealthMonitorThread.isAlive()) {
            return;
        }
        
        streamHealthMonitorThread = new Thread(() -> {
            while (isStreaming.get() && !isStopping.get()) {
                try {
                    Thread.sleep(STREAM_HEALTH_CHECK_INTERVAL);
                    
                    // 检查是否正在重启，避免重复触发
                    if (isRestarting.get()) {
                        continue;
                    }
                    
                    // 只有在飞机在线且正在推流时才检查
                    if (!isDroneOnline.get() || !isStreaming.get()) {
                        continue;
                    }
                    
                    long currentTime = System.currentTimeMillis();
                    long lastValidTime = lastValidDataTime.get();
                    long timeSinceLastValid = currentTime - lastValidTime;
                    
                    // 如果超过阈值没有收到有效数据，触发重启
                    if (timeSinceLastValid > STREAM_EMPTY_TIMEOUT) {
                        log("WARN", "startStreamHealthMonitor", 
                            "检测到流数据异常：连续 " + (timeSinceLastValid / 1000) + " 秒未收到有效数据，准备重启拉流");
                        
                        // 触发重启拉流
                        restartDroneStreaming();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log("ERROR", "startStreamHealthMonitor", "流健康监控异常: " + e.getMessage());
                }
            }
        });
        
        streamHealthMonitorThread.setDaemon(true);
        streamHealthMonitorThread.start();
        log("INFO", "startStreamHealthMonitor", "流健康监控已启动，超时阈值: " + (STREAM_EMPTY_TIMEOUT / 1000) + " 秒");
    }
    
    /**
     * 停止流健康监控
     */
    private void stopStreamHealthMonitor() {
        if (streamHealthMonitorThread != null) {
            streamHealthMonitorThread.interrupt();
            try {
                streamHealthMonitorThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            streamHealthMonitorThread = null;
        }
    }
    
    /**
     * 重启拉流（当检测到流异常时）
     */
    private void restartDroneStreaming() {
        // 防止重复重启
        if (!isRestarting.compareAndSet(false, true)) {
            log("WARN", "restartDroneStreaming", "正在重启中，跳过重复重启");
            return;
        }
        
        log("INFO", "restartDroneStreaming", "开始重启拉流...");
        
        try {
            // 先停止当前拉流（但不改变 isStreaming 状态，因为飞机还在线）
            synchronized (streamLock) {
                if (!isStreaming.get()) {
                    log("WARN", "restartDroneStreaming", "流未在运行，跳过重启");
                    isRestarting.set(false);
                    return;
                }
                
                // 设置停止标志，防止回调继续处理数据
                isStopping.set(true);
            }
            
            // 1. 停止RTMP推流
            if (droneStreamer != null) {
                try {
                    Thread stopThread = new Thread(() -> {
                        try {
                            droneStreamer.stop();
                        } catch (Exception e) {
                            log("ERROR", "restartDroneStreaming", "FFmpeg停止线程异常: " + e.getMessage());
                        }
                    });
                    stopThread.setDaemon(true);
                    stopThread.start();
                    stopThread.join(1000);
                    if (stopThread.isAlive()) {
                        stopThread.interrupt();
                        stopThread.join(100);
                    }
                } catch (Exception e) {
                    log("ERROR", "restartDroneStreaming", "停止RTMP推流时出错: " + e.getMessage());
                } finally {
                    droneStreamer = null;
                }
            }
            
            // 2. 停止流健康监控
            stopStreamHealthMonitor();
            
            // 3. 取消直播流订阅
            if (droneLiveStreamListenId != -1 && droneLiveModule != null) {
                try {
                    droneLiveModule.unSubscribeLiveStream(droneLiveStreamListenId);
                    droneLiveStreamListenId = -1;
                    Thread.sleep(500);
                } catch (Exception e) {
                    log("ERROR", "restartDroneStreaming", "取消订阅时出错: " + e.getMessage());
                    droneLiveStreamListenId = -1;
                }
            }
            
            // 4. 停止直播流
            if (droneLiveModule != null) {
                try {
                    Thread.sleep(1000);
                    Thread stopLiveThread = new Thread(() -> {
                        try {
                            droneLiveModule.stop();
                        } catch (Exception e) {
                            log("ERROR", "restartDroneStreaming", "停止直播流线程异常: " + e.getMessage());
                        }
                    });
                    stopLiveThread.setDaemon(true);
                    stopLiveThread.start();
                    stopLiveThread.join(2000);
                    if (stopLiveThread.isAlive()) {
                        log("WARN", "restartDroneStreaming", "直播流停止超时，继续重启流程");
                        stopLiveThread.interrupt();
                    }
                    Thread.sleep(500);
                    droneLiveModule = null;
                } catch (Exception e) {
                    log("ERROR", "restartDroneStreaming", "停止直播流时出错: " + e.getMessage());
                    droneLiveModule = null;
                }
            }
            
            // 5. 重置状态
            synchronized (streamLock) {
                isStopping.set(false);
                isStreaming.set(false); // 确保流状态为 false，以便重新启动
            }
            
            // 6. 等待一小段时间后重新启动
            Thread.sleep(1000);
            
            log("INFO", "restartDroneStreaming", "准备重新启动拉流...");
            
            // 7. 重新启动拉流
            synchronized (streamLock) {
                // 检查飞机是否仍然在线
                if (isDroneOnline.get() && !isStreaming.get()) {
                    // 调用启动方法
                    startDroneStreaming();
                } else if (!isDroneOnline.get()) {
                    log("WARN", "restartDroneStreaming", "飞机已离线，取消重启");
                } else {
                    log("WARN", "restartDroneStreaming", "流状态异常，取消重启");
                }
            }
            
        } catch (Exception e) {
            log("ERROR", "restartDroneStreaming", "重启拉流过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isRestarting.set(false);
        }
    }
    
    /**
     * 获取当前状态
     */
    public boolean isDroneOnline() {
        return isDroneOnline.get();
    }
    
    public boolean isStreaming() {
        return isStreaming.get();
    }
    
    /**
     * 日志输出
     */
    private void log(String level, String method, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + timestamp + "] " + level + " [" + threadName + "] [DroneStreamingTest::" + method + "] " + message);
    }
}
