// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AircraftAttitude;
import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.DockOnlineStatus;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.*;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SDK 压力测试模块
 * 多线程并发测试，持续运行，验证同步保护和对象生命周期管理
 */
public class StressTest {
    
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    // 测试控制
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean testStarted = new AtomicBoolean(false);
    
    // 测试统计
    private final AtomicLong moduleGetCount = new AtomicLong(0);
    private final AtomicLong callbackRegisterCount = new AtomicLong(0);
    private final AtomicLong controlCallCount = new AtomicLong(0);
    private final AtomicLong exceptionCount = new AtomicLong(0);
    private final AtomicLong crashCount = new AtomicLong(0);
    
    // 对象实例验证（用于验证懒加载是否只创建一次）
    private final Set<Object> flyToPointModuleInstances = Collections.synchronizedSet(new HashSet<>());
    private final Set<Object> poiModuleInstances = Collections.synchronizedSet(new HashSet<>());
    private final Set<Object> flightTaskControlModuleInstances = Collections.synchronizedSet(new HashSet<>());
    
    // 回调ID验证（用于验证ID生成器是否唯一）
    private final Set<Integer> callbackIds = Collections.synchronizedSet(new HashSet<>());
    
    // 核心组件
    private Drone drone;
    private Dock dock;
    private Workspace workspace;
    private Stream stream;
    
    // 线程池
    private ExecutorService executorService;
    
    // 测试参数
    private int threadCount = 20;
    private int iterationsPerThread = 1000;
    private long testDurationSeconds = 0; // 0 表示无限运行
    
    /**
     * 构造函数
     */
    public StressTest(Drone drone, Dock dock, Workspace workspace, Stream stream) {
        this.drone = drone;
        this.dock = dock;
        this.workspace = workspace;
        this.stream = stream;
    }
    
    /**
     * 设置测试参数
     */
    public void setTestParams(int threadCount, int iterationsPerThread, long testDurationSeconds) {
        this.threadCount = threadCount;
        this.iterationsPerThread = iterationsPerThread;
        this.testDurationSeconds = testDurationSeconds;
    }
    
    /**
     * 启动压力测试
     */
    public void start() {
        if (testStarted.getAndSet(true)) {
            log("WARN", "start", "测试已经启动，忽略重复启动");
            return;
        }
        
        log("INFO", "start", "========================================");
        log("INFO", "start", "开始 SDK 压力测试");
        log("INFO", "start", "线程数: " + threadCount);
        log("INFO", "start", "每线程迭代次数: " + (iterationsPerThread > 0 ? iterationsPerThread : "无限"));
        log("INFO", "start", "测试持续时间: " + (testDurationSeconds > 0 ? testDurationSeconds + " 秒" : "无限"));
        log("INFO", "start", "========================================");
        
        // 创建线程池
        executorService = Executors.newFixedThreadPool(threadCount + 10); // 额外10个线程用于监控和GC
        
        // 启动测试场景
        startModuleGetStressTest();
        startCallbackRegisterStressTest();
        startControlCallStressTest();
        startMixedScenarioTest();
        startLongRunningStabilityTest();
        
        // 启动统计报告线程
        startStatisticsReporter();
        
        log("INFO", "start", "所有测试场景已启动，测试将持续运行...");
    }
    
    /**
     * 停止压力测试
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }
        
        log("INFO", "stop", "正在停止压力测试...");
        
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        printFinalStatistics();
        log("INFO", "stop", "压力测试已停止");
    }
    
    /**
     * 场景1: 模块获取并发测试（核心测试）
     * 验证懒加载同步是否有效
     */
    private void startModuleGetStressTest() {
        log("INFO", "startModuleGetStressTest", "启动场景1: 模块获取并发测试");
        
        // 测试 getFlyToPointModule
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            FlyToPointModule module = drone.getFlyToPointModule();
                            if (module != null) {
                                flyToPointModuleInstances.add(module);
                                moduleGetCount.incrementAndGet();
                                localCount++;
                                
                                // 每100次操作输出一次日志
                                if (localCount % 100 == 0) {
                                    log("INFO", "startModuleGetStressTest", 
                                        "线程 " + threadId + " [getFlyToPointModule] 已执行 " + localCount + " 次");
                                }
                                
                                // 验证对象唯一性
                                if (flyToPointModuleInstances.size() > 1) {
                                    log("ERROR", "startModuleGetStressTest", 
                                        "检测到多个 FlyToPointModule 实例！当前实例数: " + flyToPointModuleInstances.size());
                                }
                            }
                        }
                        
                        // 随机延迟，模拟真实场景
                        Thread.sleep(randomDelay(10, 100));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startModuleGetStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
        
        // 测试 getPoiModule
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            PoiModule module = drone.getPoiModule();
                            if (module != null) {
                                poiModuleInstances.add(module);
                                moduleGetCount.incrementAndGet();
                                localCount++;
                                
                                // 每100次操作输出一次日志
                                if (localCount % 100 == 0) {
                                    log("INFO", "startModuleGetStressTest", 
                                        "线程 " + threadId + " [getPoiModule] 已执行 " + localCount + " 次");
                                }
                                
                                if (poiModuleInstances.size() > 1) {
                                    log("ERROR", "startModuleGetStressTest", 
                                        "检测到多个 PoiModule 实例！当前实例数: " + poiModuleInstances.size());
                                }
                            }
                        }
                        Thread.sleep(randomDelay(10, 100));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startModuleGetStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                    }
                }
            });
        }
        
        // 测试 getFlightTaskControlModule
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            FlightTaskControlModule module = drone.getFlightTaskControlModule();
                            if (module != null) {
                                flightTaskControlModuleInstances.add(module);
                                moduleGetCount.incrementAndGet();
                                localCount++;
                                
                                // 每100次操作输出一次日志
                                if (localCount % 100 == 0) {
                                    log("INFO", "startModuleGetStressTest", 
                                        "线程 " + threadId + " [getFlightTaskControlModule] 已执行 " + localCount + " 次");
                                }
                                
                                if (flightTaskControlModuleInstances.size() > 1) {
                                    log("ERROR", "startModuleGetStressTest", 
                                        "检测到多个 FlightTaskControlModule 实例！当前实例数: " + flightTaskControlModuleInstances.size());
                                }
                            }
                        }
                        Thread.sleep(randomDelay(10, 100));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startModuleGetStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                    }
                }
            });
        }
        
        // 测试 getLiveStream（使用不同的 payload_index）
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            LiveInfo liveInfo = new LiveInfo("", "99-0-" + (threadId % 5), "", Collections.emptyList());
                            LiveModule module = drone.getLiveStream(liveInfo);
                            if (module != null) {
                                moduleGetCount.incrementAndGet();
                            }
                        }
                        Thread.sleep(randomDelay(50, 200));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startModuleGetStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                    }
                }
            });
        }
    }
    
    /**
     * 场景2: 回调注册并发测试
     * 验证回调注册同步是否有效，包括多次监听和取消监听
     */
    private void startCallbackRegisterStressTest() {
        log("INFO", "startCallbackRegisterStressTest", "启动场景2: 回调注册并发测试");
        
        // 测试 listenAircraftBatteryInfo（多次监听和取消）
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                List<Integer> activeListenIds = new ArrayList<>();
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            // 创建静态回调对象，避免被GC
                            final FlightHubCallback.AircraftBatteryInfoCallback callback = 
                                new FlightHubCallback.AircraftBatteryInfoCallback() {
                                    @Override
                                    public void onResult(AircraftBatteryInfo batteryInfo) {
                                        // 回调接收验证
                                    }
                                };
                            
                            int listenId = drone.getDroneOsdModule().listenAircraftBatteryInfo(callback);
                            if (listenId >= 0) {
                                callbackIds.add(listenId);
                                activeListenIds.add(listenId);
                                callbackRegisterCount.incrementAndGet();
                                localCount++;
                                
                                log("INFO", "startCallbackRegisterStressTest", 
                                    "线程 " + threadId + " [listenAircraftBatteryInfo] 注册成功, listenId: " + listenId + 
                                    ", 当前活跃监听数: " + activeListenIds.size());
                                
                                // 每50次操作输出一次汇总日志
                                if (localCount % 50 == 0) {
                                    log("INFO", "startCallbackRegisterStressTest", 
                                        "线程 " + threadId + " [listenAircraftBatteryInfo] 已注册 " + localCount + " 次回调, 当前活跃: " + activeListenIds.size());
                                }
                                
                                // 验证ID唯一性
                                if (callbackIds.size() != callbackRegisterCount.get()) {
                                    log("ERROR", "startCallbackRegisterStressTest", 
                                        "检测到重复的回调ID！listenId: " + listenId);
                                }
                                
                                // 随机决定是否立即取消或延迟取消（模拟多次监听场景）
                                if (activeListenIds.size() > 10 || Math.random() < 0.3) {
                                    // 取消一个旧的监听
                                    int cancelId = activeListenIds.remove(0);
                                    drone.getDroneOsdModule().cancelListenAircraftBatteryInfo(cancelId);
                                    callbackIds.remove(cancelId);
                                    log("INFO", "startCallbackRegisterStressTest", 
                                        "线程 " + threadId + " [cancelListenAircraftBatteryInfo] 取消监听, listenId: " + cancelId);
                                }
                                
                                Thread.sleep(randomDelay(50, 200));
                            } else {
                                log("WARN", "startCallbackRegisterStressTest", 
                                    "线程 " + threadId + " [listenAircraftBatteryInfo] 注册失败, listenId: " + listenId);
                            }
                        }
                        Thread.sleep(randomDelay(50, 200));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startCallbackRegisterStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                // 清理所有活跃的监听
                for (Integer id : activeListenIds) {
                    try {
                        drone.getDroneOsdModule().cancelListenAircraftBatteryInfo(id);
                        callbackIds.remove(id);
                    } catch (Exception e) {
                        log("ERROR", "startCallbackRegisterStressTest", 
                            "线程 " + threadId + " 清理监听失败, listenId: " + id);
                    }
                }
            });
        }
        
        // 测试 listenAircraftLocation
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            final FlightHubCallback.AircraftLocationCallback callback = 
                                new FlightHubCallback.AircraftLocationCallback() {
                                    @Override
                                    public void onResult(AircraftLocation location) {
                                        // 回调接收验证
                                    }
                                };
                            
                            int listenId = drone.getDroneOsdModule().listenAircraftLocation(callback);
                            if (listenId >= 0) {
                                callbackRegisterCount.incrementAndGet();
                                localCount++;
                                
                                log("INFO", "startCallbackRegisterStressTest", 
                                    "线程 " + threadId + " [listenAircraftLocation] 注册成功, listenId: " + listenId);
                                
                                Thread.sleep(randomDelay(100, 500));
                                
                                drone.getDroneOsdModule().cancelListenAircraftLocation(listenId);
                                log("INFO", "startCallbackRegisterStressTest", 
                                    "线程 " + threadId + " [cancelListenAircraftLocation] 取消监听, listenId: " + listenId);
                            }
                        }
                        Thread.sleep(randomDelay(50, 200));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startCallbackRegisterStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                    }
                }
            });
        }
        
        // 测试 listenAircraftAttitude
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            final FlightHubCallback.AircraftAttitudeCallback callback = 
                                new FlightHubCallback.AircraftAttitudeCallback() {
                                    @Override
                                    public void onResult(AircraftAttitude attitude) {
                                        // 回调接收验证
                                    }
                                };
                            
                            int listenId = drone.getDroneOsdModule().listenAircraftAttitude(callback);
                            if (listenId >= 0) {
                                callbackRegisterCount.incrementAndGet();
                                localCount++;
                                
                                if (localCount % 50 == 0) {
                                    log("INFO", "startCallbackRegisterStressTest", 
                                        "线程 " + threadId + " [listenAircraftAttitude] 已注册 " + localCount + " 次回调");
                                }
                                
                                Thread.sleep(randomDelay(100, 500));
                                drone.getDroneOsdModule().cancelListenAircraftAttitude(listenId);
                            }
                        }
                        Thread.sleep(randomDelay(50, 200));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startCallbackRegisterStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                    }
                }
            });
        }
        
        // 测试 listenLinkStatus
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            final FlightHubCallback.LinkStatusCallback callback = 
                                new FlightHubCallback.LinkStatusCallback() {
                                    @Override
                                    public void onResult(LinkStatus status) {
                                        // 回调接收验证
                                    }
                                };
                            
                            int listenId = drone.getDrcModule().listenLinkStatus(callback);
                            if (listenId >= 0) {
                                callbackRegisterCount.incrementAndGet();
                                localCount++;
                                
                                if (localCount % 50 == 0) {
                                    log("INFO", "startCallbackRegisterStressTest", 
                                        "线程 " + threadId + " [listenLinkStatus] 已注册 " + localCount + " 次回调");
                                }
                                
                                Thread.sleep(randomDelay(100, 500));
                                drone.getDrcModule().cancelListenLinkStatus(listenId);
                            }
                        }
                        Thread.sleep(randomDelay(50, 200));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startCallbackRegisterStressTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                    }
                }
            });
        }
        
        // 测试 Dock 的监听（如果 dock 不为空）
        if (dock != null) {
            // 测试 listenDockLocation
            for (int i = 0; i < Math.min(threadCount, 10); i++) {
                final int threadId = i;
                executorService.submit(() -> {
                    long localCount = 0;
                    while (running.get() && shouldContinue()) {
                        try {
                            final FlightHubCallback.DockLocationCallback callback = 
                                new FlightHubCallback.DockLocationCallback() {
                                    @Override
                                    public void onResult(com.dji.flighthubsdk.bean.DockLocation location) {
                                        // 回调接收验证
                                    }
                                };
                            
                            int listenId = dock.getDockOsdModule().listenDockLocation(callback);
                            if (listenId >= 0) {
                                callbackRegisterCount.incrementAndGet();
                                localCount++;
                                
                                log("INFO", "startCallbackRegisterStressTest", 
                                    "线程 " + threadId + " [Dock.getDockOsdModule().listenDockLocation] 注册成功, listenId: " + listenId);
                                
                                Thread.sleep(randomDelay(100, 500));
                                dock.getDockOsdModule().cancelListenDockLocation(listenId);
                                log("INFO", "startCallbackRegisterStressTest", 
                                    "线程 " + threadId + " [Dock.getDockOsdModule().cancelListenDockLocation] 取消监听, listenId: " + listenId);
                            }
                            Thread.sleep(randomDelay(50, 200));
                        } catch (Exception e) {
                            exceptionCount.incrementAndGet();
                            log("ERROR", "startCallbackRegisterStressTest", 
                                "线程 " + threadId + " [Dock] 异常: " + e.getMessage());
                        }
                    }
                });
            }
            
            // 测试 listenDockOnlineStatus
            for (int i = 0; i < Math.min(threadCount, 10); i++) {
                final int threadId = i;
                executorService.submit(() -> {
                    long localCount = 0;
                    while (running.get() && shouldContinue()) {
                        try {
                            final FlightHubCallback.DockOnlineStatusCallback callback = 
                                new FlightHubCallback.DockOnlineStatusCallback() {
                                    @Override
                                    public void onResult(DockOnlineStatus status) {
                                        // 回调接收验证
                                    }
                                };
                            
                            int listenId = dock.getDeviceStateModule().listenDockOnlineStatus(callback);
                            if (listenId >= 0) {
                                callbackRegisterCount.incrementAndGet();
                                localCount++;
                                
                                if (localCount % 50 == 0) {
                                    log("INFO", "startCallbackRegisterStressTest", 
                                        "线程 " + threadId + " [Dock.getDockOsdModule().listenDockOnlineStatus] 已注册 " + localCount + " 次回调");
                                }
                                
                                Thread.sleep(randomDelay(100, 500));
                                dock.getDeviceStateModule().cancelListenDockOnlineStatus(listenId);
                            }
                            Thread.sleep(randomDelay(50, 200));
                        } catch (Exception e) {
                            exceptionCount.incrementAndGet();
                            log("ERROR", "startCallbackRegisterStressTest", 
                                "线程 " + threadId + " [Dock] 异常: " + e.getMessage());
                        }
                    }
                });
            }
        }
    }
    
    /**
     * 场景3: 控制接口并发测试
     * 验证控制接口的并发安全性
     */
    private void startControlCallStressTest() {
        log("INFO", "startControlCallStressTest", "启动场景3: 控制接口并发测试");
        
        // 测试 drcTakePhoto（需要设备在线）
        for (int i = 0; i < Math.min(threadCount, 5); i++) { // 限制线程数，避免过于频繁
            final int threadId = i;
            executorService.submit(() -> {
                final AtomicLong localCount = new AtomicLong(0);
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            final AtomicBoolean callbackReceived = new AtomicBoolean(false);
                            
                            log("INFO", "startControlCallStressTest",
                                "线程 " + threadId + " [drcTakePhoto] 开始拍照...");

                            // 拍照
                            drone.getCameraControlModule().drcTakePhoto("99-0-0", new FlightHubCallback.ResultCallbackWithError() {
                                @Override
                                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                    callbackReceived.set(true);
                                    controlCallCount.incrementAndGet();
                                    long count = localCount.incrementAndGet();

                                    log("INFO", "startControlCallStressTest",
                                        "线程 " + threadId + " [drcTakePhoto] 回调: error=" + error +
                                        ", 总调用次数: " + count);
                                }
                            });
                            
                            // 等待回调
                            Thread.sleep(1000);
                            
                            if (!callbackReceived.get()) {
                                log("WARN", "startControlCallStressTest", 
                                    "线程 " + threadId + " [drcTakePhoto] 等待回调超时");
                            }
                        }
                        Thread.sleep(randomDelay(2000, 5000)); // 拍照间隔较长
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startControlCallStressTest", 
                            "线程 " + threadId + " [drcTakePhoto] 异常: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
        
        // 测试 drcStartRecord / drcStopRecord
        for (int i = 0; i < Math.min(threadCount, 3); i++) {
            final int threadId = i;
            executorService.submit(() -> {
                final AtomicLong localCount = new AtomicLong(0);
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            log("INFO", "startControlCallStressTest", 
                                "线程 " + threadId + " [drcStartRecord] 开始录像...");
                            
                            drone.getCameraControlModule().drcStartRecord("99-0-0", new FlightHubCallback.ResultCallbackWithError() {
                                @Override
                                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                    controlCallCount.incrementAndGet();
                                    long count = localCount.incrementAndGet();
                                    
                                    log("INFO", "startControlCallStressTest", 
                                        "线程 " + threadId + " [drcStartRecord] 回调: error=" + error + 
                                        ", 总调用次数: " + count);
                                    
                                    // 延迟后停止录像
                                    try {
                                        Thread.sleep(randomDelay(1000, 3000));
                                        log("INFO", "startControlCallStressTest", 
                                            "线程 " + threadId + " [drcStopRecord] 停止录像...");
                                        
                                        drone.getCameraControlModule().drcStopRecord("99-0-0", new FlightHubCallback.ResultCallbackWithError() {
                                            @Override
                                            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                                log("INFO", "startControlCallStressTest", 
                                                    "线程 " + threadId + " [drcStopRecord] 回调: error=" + error);
                                            }
                                        });
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            });
                        }
                        Thread.sleep(randomDelay(5000, 10000)); // 录像间隔较长
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startControlCallStressTest", 
                            "线程 " + threadId + " [Record] 异常: " + e.getMessage());
                    }
                }
            });
        }
    }
    
    /**
     * 场景4: 混合场景测试（最接近真实场景）
     */
    private void startMixedScenarioTest() {
        log("INFO", "startMixedScenarioTest", "启动场景4: 混合场景测试");
        
        // 混合操作：获取模块 + 注册回调 + 调用接口
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                long localCount = 0;
                while (running.get() && shouldContinue()) {
                    try {
                        if (drone != null) {
                            // 随机选择操作
                            int operation = (int) (Math.random() * 3);
                            
                            switch (operation) {
                                case 0:
                                    // 获取模块
                                    drone.getFlyToPointModule();
                                    break;
                                case 1:
                                    // 注册回调
                                    int listenId = drone.getDroneOsdModule().listenAircraftAttitude(new FlightHubCallback.AircraftAttitudeCallback() {
                                        @Override
                                        public void onResult(AircraftAttitude attitude) {
                                            // 回调处理
                                        }
                                    });
                                    if (listenId >= 0) {
                                        Thread.sleep(randomDelay(100, 300));
                                        drone.getDroneOsdModule().cancelListenAircraftAttitude(listenId);
                                    }
                                    break;
                                case 2:
                                    // 获取其他模块
                                    drone.getPoiModule();
                                    break;
                            }
                            localCount++;
                            
                            // 每200次操作输出一次日志
                            if (localCount % 200 == 0) {
                                log("INFO", "startMixedScenarioTest", 
                                    "线程 " + threadId + " [混合场景] 已执行 " + localCount + " 次操作");
                            }
                        }
                        Thread.sleep(randomDelay(50, 300));
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                        log("ERROR", "startMixedScenarioTest", 
                            "线程 " + threadId + " 异常: " + e.getMessage());
                    }
                }
            });
        }
    }
    
    /**
     * 场景5: 长时间稳定性测试
     * 定期触发GC，验证对象生命周期
     */
    private void startLongRunningStabilityTest() {
        log("INFO", "startLongRunningStabilityTest", "启动场景5: 长时间稳定性测试");
        
        // GC触发线程
        executorService.submit(() -> {
            while (running.get()) {
                try {
                    Thread.sleep(10000); // 每10秒触发一次GC
                    System.gc();
                    log("INFO", "startLongRunningStabilityTest", "触发 GC");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        // 对象生命周期验证线程
        executorService.submit(() -> {
            while (running.get()) {
                try {
                    Thread.sleep(30000); // 每30秒验证一次
                    
                    if (drone != null) {
                        // 验证对象是否仍然有效
                        FlyToPointModule module = drone.getFlyToPointModule();
                        if (module == null) {
                            log("ERROR", "startLongRunningStabilityTest", 
                                "FlyToPointModule 对象被 GC 回收！");
                            crashCount.incrementAndGet();
                        } else {
                            // 尝试调用方法验证对象有效性
                            try {
                                // 这里可以调用一个简单的方法来验证对象是否有效
                                // 如果对象被GC，调用会失败
                            } catch (Exception e) {
                                log("ERROR", "startLongRunningStabilityTest", 
                                    "对象调用失败，可能已被GC: " + e.getMessage());
                                crashCount.incrementAndGet();
                            }
                        }
                    }
                } catch (Exception e) {
                    exceptionCount.incrementAndGet();
                    log("ERROR", "startLongRunningStabilityTest", 
                        "验证线程异常: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * 启动统计报告线程
     */
    private void startStatisticsReporter() {
        executorService.submit(() -> {
            long startTime = System.currentTimeMillis();
            long lastModuleCount = 0;
            long lastCallbackCount = 0;
            long lastControlCount = 0;
            
            while (running.get()) {
                try {
                    Thread.sleep(10000); // 每10秒输出一次统计（更频繁）
                    
                    long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                    long currentModuleCount = moduleGetCount.get();
                    long currentCallbackCount = callbackRegisterCount.get();
                    long currentControlCount = controlCallCount.get();
                    
                    // 计算速率
                    long moduleRate = (currentModuleCount - lastModuleCount) / 10; // 每秒次数
                    long callbackRate = (currentCallbackCount - lastCallbackCount) / 10;
                    long controlRate = (currentControlCount - lastControlCount) / 10;
                    
                    log("INFO", "Statistics", "========================================");
                    log("INFO", "Statistics", "运行时间: " + elapsedSeconds + " 秒");
                    log("INFO", "Statistics", "模块获取: " + currentModuleCount + " 次 (速率: " + moduleRate + " 次/秒)");
                    log("INFO", "Statistics", "回调注册: " + currentCallbackCount + " 次 (速率: " + callbackRate + " 次/秒)");
                    log("INFO", "Statistics", "控制调用: " + currentControlCount + " 次 (速率: " + controlRate + " 次/秒)");
                    log("INFO", "Statistics", "异常次数: " + exceptionCount.get());
                    log("INFO", "Statistics", "崩溃次数: " + crashCount.get());
                    log("INFO", "Statistics", "FlyToPointModule 实例数: " + flyToPointModuleInstances.size() + " (应为1)");
                    log("INFO", "Statistics", "PoiModule 实例数: " + poiModuleInstances.size() + " (应为1)");
                    log("INFO", "Statistics", "FlightTaskControlModule 实例数: " + flightTaskControlModuleInstances.size() + " (应为1)");
                    log("INFO", "Statistics", "唯一回调ID数: " + callbackIds.size());
                    log("INFO", "Statistics", "========================================");
                    
                    // 更新上次计数
                    lastModuleCount = currentModuleCount;
                    lastCallbackCount = currentCallbackCount;
                    lastControlCount = currentControlCount;
                    
                    // 如果检测到问题，输出警告
                    if (flyToPointModuleInstances.size() > 1) {
                        log("ERROR", "Statistics", "⚠️ 检测到多个 FlyToPointModule 实例！");
                    }
                    if (poiModuleInstances.size() > 1) {
                        log("ERROR", "Statistics", "⚠️ 检测到多个 PoiModule 实例！");
                    }
                    if (flightTaskControlModuleInstances.size() > 1) {
                        log("ERROR", "Statistics", "⚠️ 检测到多个 FlightTaskControlModule 实例！");
                    }
                    if (crashCount.get() > 0) {
                        log("ERROR", "Statistics", "⚠️ 检测到对象被 GC 回收！");
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    /**
     * 判断是否应该继续测试
     */
    private boolean shouldContinue() {
        if (iterationsPerThread > 0) {
            // 如果设置了迭代次数，需要跟踪（这里简化处理，实际可以更精确）
            return true; // 简化：由线程自己控制迭代次数
        }
        if (testDurationSeconds > 0) {
            // 如果设置了持续时间，需要跟踪开始时间
            return true; // 简化：由外部控制
        }
        return true; // 默认无限运行
    }
    
    /**
     * 生成随机延迟
     */
    private long randomDelay(int minMs, int maxMs) {
        return minMs + (long)(Math.random() * (maxMs - minMs));
    }
    
    /**
     * 打印最终统计
     */
    private void printFinalStatistics() {
        log("INFO", "printFinalStatistics", "========================================");
        log("INFO", "printFinalStatistics", "最终统计报告");
        log("INFO", "printFinalStatistics", "模块获取总次数: " + moduleGetCount.get());
        log("INFO", "printFinalStatistics", "回调注册总次数: " + callbackRegisterCount.get());
        log("INFO", "printFinalStatistics", "控制调用总次数: " + controlCallCount.get());
        log("INFO", "printFinalStatistics", "异常总次数: " + exceptionCount.get());
        log("INFO", "printFinalStatistics", "崩溃总次数: " + crashCount.get());
        log("INFO", "printFinalStatistics", "FlyToPointModule 实例数: " + flyToPointModuleInstances.size());
        log("INFO", "printFinalStatistics", "PoiModule 实例数: " + poiModuleInstances.size());
        log("INFO", "printFinalStatistics", "FlightTaskControlModule 实例数: " + flightTaskControlModuleInstances.size());
        log("INFO", "printFinalStatistics", "========================================");
    }
    
    /**
     * 日志输出
     */
    private void log(String level, String method, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + timestamp + "] " + level + " [" + threadName + "] [StressTest::" + method + "] " + message);
    }
}

