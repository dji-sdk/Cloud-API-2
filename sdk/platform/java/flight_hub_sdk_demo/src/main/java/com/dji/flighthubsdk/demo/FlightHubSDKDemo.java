// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.core.CustomSEIModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.LiveModule;
import com.dji.flighthubsdk.core.LiveRecordModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.BaseStation;
import com.dji.flighthubsdk.core.AIBoxModule;
import com.dji.flighthubsdk.core.FlightTaskControlModule;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.FlyToPointModule;
import com.dji.flighthubsdk.core.InFlightWaylineModule;
import com.dji.flighthubsdk.core.TerraBuildModule;
import com.dji.flighthubsdk.core.DeviceDebugModule;
import com.dji.flighthubsdk.core.RelayControlModule;
import com.dji.flighthubsdk.core.RtkCalibrationModule;
import com.dji.flighthubsdk.core.NetworkTestModule;
import com.dji.flighthubsdk.core.AIInsideManageModule;
import com.dji.flighthubsdk.core.AiAlertModule;
import com.dji.flighthubsdk.core.LiveSyncModule;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import com.dji.flighthubsdk.core.FlightTaskRecordModule;
import com.dji.flighthubsdk.core.MaintenanceManagementModule;
import com.dji.flighthubsdk.core.DiagnosisManagementModule;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.core.LiveModule;
import com.dji.flighthubsdk.core.PoiModule;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.core.FlashLedModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.OnboardAiModule;
import com.dji.flighthubsdk.generated.AiIdentifyFilterSetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreModeSetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreResetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreSetParam;
import com.dji.flighthubsdk.bean.FlashLedConfig;
import com.dji.flighthubsdk.bean.FlashLedConfigParams;
import com.dji.flighthubsdk.bean.FlashLedParams;
import com.dji.flighthubsdk.bean.FlashLedPatternInfo;
import com.dji.flighthubsdk.bean.FlashLedSegment;
import com.dji.flighthubsdk.bean.FlashLedStateInfo;
import com.dji.flighthubsdk.bean.AiSeiInfo;
import com.dji.flighthubsdk.bean.ActiveInFlightWaylineTaskInfo;
import com.dji.flighthubsdk.bean.AircraftActivationTime;
import com.dji.flighthubsdk.bean.AircraftAttitude;
import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftBatteryWarningThresholds;
import com.dji.flighthubsdk.bean.AircraftComplianceInfo;
import com.dji.flighthubsdk.bean.AircraftControlCode;
import com.dji.flighthubsdk.bean.AircraftCurrentRthMode;
import com.dji.flighthubsdk.bean.AircraftFirmwareStatus;
import com.dji.flighthubsdk.bean.AircraftHomeGeographicalInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.AircraftMaintainStatus;
import com.dji.flighthubsdk.bean.AircraftMaintainStatusInfo;
import com.dji.flighthubsdk.bean.AircraftNightLightsState;
import com.dji.flighthubsdk.bean.AircraftObstacleAvoidanceState;
import com.dji.flighthubsdk.bean.AircraftRcLostAction;
import com.dji.flighthubsdk.bean.AircraftRthInfo;
import com.dji.flighthubsdk.bean.AircraftSpeed;
import com.dji.flighthubsdk.bean.AircraftStorageInfo;
import com.dji.flighthubsdk.bean.AircraftTotalFlightInfo;
import com.dji.flighthubsdk.bean.AircraftTrackId;
import com.dji.flighthubsdk.bean.BatteryInfo;
import com.dji.flighthubsdk.bean.AircraftModeCode;
import com.dji.flighthubsdk.bean.AircraftControlSource;
import com.dji.flighthubsdk.bean.AircraftGear;
import com.dji.flighthubsdk.bean.AircraftHomeDistance;
import com.dji.flighthubsdk.bean.AircraftLimitsInfo;
import com.dji.flighthubsdk.bean.AircraftPositionState;
import com.dji.flighthubsdk.bean.AircraftWindSpeed;
import com.dji.flighthubsdk.bean.ArSeiInfo;
import com.dji.flighthubsdk.bean.AssistantSeiInfo;
import com.dji.flighthubsdk.value.CameraLensType;
import com.dji.flighthubsdk.value.IrGainMode;
import com.dji.flighthubsdk.value.InFlightWaylineControlAction;
import com.dji.flighthubsdk.bean.CameraMeasureTarget;
import com.dji.flighthubsdk.value.CameraMode;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.CameraWideLensState;
import com.dji.flighthubsdk.bean.CameraZoomLensState;
import com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskParams;
import com.dji.flighthubsdk.bean.CreatePsdkSpeakerResourceParams;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceChange;
import com.dji.flighthubsdk.bean.DeviceDetailInfo;
import com.dji.flighthubsdk.bean.ModelInfo;
import com.dji.flighthubsdk.bean.SubDeviceInfo;
import com.dji.flighthubsdk.bean.WorkspaceDeviceChange;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.DeviceTopoChange;
import com.dji.flighthubsdk.bean.DockAirConditionerStatusInfo;
import com.dji.flighthubsdk.bean.DockAlternateLandPoint;
import com.dji.flighthubsdk.bean.DockBackupBatteryInfo;
import com.dji.flighthubsdk.bean.DockCoverState;
import com.dji.flighthubsdk.bean.DockDroneBatteryMaintenanceInfo;
import com.dji.flighthubsdk.bean.DockDroneChargeState;
import com.dji.flighthubsdk.bean.DockDrcState;
import com.dji.flighthubsdk.bean.DockEmergencyStopState;
import com.dji.flighthubsdk.bean.DockFirmwareInfo;
import com.dji.flighthubsdk.bean.DockFirstPowerOnTime;
import com.dji.flighthubsdk.bean.DockJobNumber;
import com.dji.flighthubsdk.bean.DockLocation;
import com.dji.flighthubsdk.bean.DockMaintainStatus;
import com.dji.flighthubsdk.bean.DockMaintainStatusInfo;
import com.dji.flighthubsdk.bean.DockMediaFileDetail;
import com.dji.flighthubsdk.bean.DockMeteorologicalInfo;
import com.dji.flighthubsdk.bean.DockModeCode;
import com.dji.flighthubsdk.bean.DockNetworkState;
import com.dji.flighthubsdk.bean.DockPositionState;
import com.dji.flighthubsdk.bean.DockPowerState;
import com.dji.flighthubsdk.bean.DockPutterState;
import com.dji.flighthubsdk.bean.DockRelativeAlternateLandPoint;
import com.dji.flighthubsdk.bean.DockSelfConvergeCoordinate;
import com.dji.flighthubsdk.bean.DockSilentMode;
import com.dji.flighthubsdk.bean.DockStorageState;
import com.dji.flighthubsdk.bean.DockSubDeviceInfoList;
import com.dji.flighthubsdk.bean.DockSupplementLightState;
import com.dji.flighthubsdk.bean.DockUserExperienceImprovement;
import com.dji.flighthubsdk.bean.DockWirelessLink;
import com.dji.flighthubsdk.bean.DockAccTime;
import com.dji.flighthubsdk.bean.DockActivationTime;
import com.dji.flighthubsdk.bean.DockAlarmState;
import com.dji.flighthubsdk.bean.DockBatteryStoreMode;
import com.dji.flighthubsdk.bean.BatteryBasicInfo;
import com.dji.flighthubsdk.bean.DroneInDock;
import com.dji.flighthubsdk.bean.FlightTaskStepCode;
import com.dji.flighthubsdk.bean.DockSubDeviceInfo;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.EditPsdkSpeakerResourceParams;
import com.dji.flighthubsdk.bean.EisSeiInfo;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.bean.FlyToDroneSelfPlanPath;
import com.dji.flighthubsdk.bean.FlyToPointParams;
import com.dji.flighthubsdk.bean.FlyToPointProgress;
import com.dji.flighthubsdk.bean.FovSeiInfo;
import com.dji.flighthubsdk.bean.FrameInfo;
import com.dji.flighthubsdk.bean.GetPsdkWidgetResourceParams;
import com.dji.flighthubsdk.bean.GimbalState;
import com.dji.flighthubsdk.bean.AircraftPayloadCameraInfo;
import com.dji.flighthubsdk.bean.AircraftPayloadsCamerasList;
import com.dji.flighthubsdk.bean.AircraftPayloadGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadLiveviewWorldRegionEntry;
import com.dji.flighthubsdk.bean.AircraftPayloadsOsdInfo;
import com.dji.flighthubsdk.bean.AircraftPayloadsOsdInfoList;
import com.dji.flighthubsdk.bean.AircraftPayloadsCameraLiveviewWorldRegion;
import com.dji.flighthubsdk.bean.AircraftPayloadsGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadsList;
import com.dji.flighthubsdk.bean.CameraIrMeteringPoint;
import com.dji.flighthubsdk.bean.CameraLiveviewWorldRegion;
import com.dji.flighthubsdk.bean.HmsArgs;
import com.dji.flighthubsdk.bean.HmsDataBasics;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.HmsDataItem;
import com.dji.flighthubsdk.bean.InFlightWaylineProgress;
import com.dji.flighthubsdk.bean.SmartTrackPointInfo;
import com.dji.flighthubsdk.generated.IntVector;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveviewWorldRegion;
import com.dji.flighthubsdk.bean.PanoramaParam;
import com.dji.flighthubsdk.bean.PanoramaStatus;
import com.dji.flighthubsdk.bean.PsdkDeviceInfo;
import com.dji.flighthubsdk.bean.PsdkDeviceList;
import com.dji.flighthubsdk.bean.PsdkFloatingWindowTextInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerPlayProgressInfo;
import com.dji.flighthubsdk.bean.PsdkStateSearchlightInfo;
import com.dji.flighthubsdk.bean.PsdkStateSpeakerInfo;
import com.dji.flighthubsdk.bean.PsdkStateSummaryInfo;
import com.dji.flighthubsdk.value.PsdkSubType;
import com.dji.flighthubsdk.bean.PsdkSpeakerResource;
import com.dji.flighthubsdk.bean.PsdkSpeakerResourceList;
import com.dji.flighthubsdk.bean.PsdkWidgetResource;
import com.dji.flighthubsdk.bean.PsdkWidgetResourceList;
import com.dji.flighthubsdk.bean.PsdkSpeakerAudioPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSconfig;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.value.DockCameraPosition;
import com.dji.flighthubsdk.value.DockCoverMode;
import com.dji.flighthubsdk.value.MegaphonePlayMode;
import com.dji.flighthubsdk.value.MegaphoneTTSLanguageType;
import com.dji.flighthubsdk.value.MegaphoneTTSVoiceType;
import com.dji.flighthubsdk.value.NightSceneMode;
import com.dji.flighthubsdk.bean.STSCredential;
import com.dji.flighthubsdk.bean.STSInfo;
import com.dji.flighthubsdk.generated.SeiPoint2D;
import com.dji.flighthubsdk.bean.CameraFocalLengthParams;
import com.dji.flighthubsdk.bean.LookAtTargetParams;
import com.dji.flighthubsdk.bean.SwitchCameraModeParams;
import com.dji.flighthubsdk.bean.LiveQualitySettingParams;
import com.dji.flighthubsdk.bean.SwitchLiveLensParams;
import com.dji.flighthubsdk.bean.SetPhotoStorageSettingsParams;
import com.dji.flighthubsdk.bean.SetNightSceneModeParams;
import com.dji.flighthubsdk.bean.SetVideoStorageSettingsParams;
import com.dji.flighthubsdk.bean.ZoomAtTargetParams;
import com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams;
import com.dji.flighthubsdk.bean.ManulRtkCalibrationParams;



// 定义函数式接口
@FunctionalInterface
interface DataProcessor {
    void print(String prefix, HmsDataBasics data);
}


/**
 * Flight Hub SDK 演示程序。
 * <p>Phase 2 module 化后，业务 API 统一经 {@code getXxxModule()} 访问，例如：
 * {@code workspace.getMediaServiceModule().getMediaFilesList(...)}、
 * {@code drone.getFlightControlModule().returnHome(...)}、
 * {@code dock.getDockControlModule().setDockCoverState(...)}。
 * 对象层（Workspace/Dock/Drone）仅保留 module getter 与 Live 工厂方法。</p>
 */
public class FlightHubSDKDemo {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Scanner scanner = new Scanner(System.in);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final Random random = new Random();
    private static boolean isInteractiveMode = true;

    // 全局变量
    private static List<DeviceTopo> deviceList = new ArrayList<>();
    private static DeviceTopo selectedDevice = null;
    // 已选择拓扑的全部设备 SN（主设备在前，子设备按拓扑顺序追加）
    private static final List<String> selectedDeviceSns = new ArrayList<>();
    private static LiveModule dockLiveModule = null;
    private static LiveModule droneLiveModule = null;
    private static Dock dock = null;
    private static Drone drone = null;
    private static final Map<String, Dock> dockCache = new HashMap<>();
    private static final Map<String, Drone> droneCache = new HashMap<>();
    private static final Map<String, BaseStation> baseStationCache = new HashMap<>();
    private static Workspace workspace = null;
    private static java.util.List<LiveInfo> liveInfoList = new ArrayList<>();
    private static String targetPayloadIndex = "99-0-0"; // hardcode for test
    // listen id
    private static int payloadControlAuthorityListenId = -1;
    private static int flightControlAuthorityListenId = -1;
    private static int panoramaStatusListenId = -1;
    private static int dockLiveStreamListenId = -1;
    private static int droneLiveStreamListenId = -1;

    // 红外增益模式相关监听ID
    private static int liveVideoInfoListenId = -1;
    private static int thermalGainModeListenId = -1;

    // 飞行任务异常和设备变更相关监听ID
    private static int flightTaskExceptionListenId = -1;
    private static int workspaceDeviceChangeListenId = -1;
    private static int droneDeviceTopoChangeListenId = -1;
    private static int dockDeviceTopoChangeListenId = -1;

    // POI相关监听ID
    private static int aircraftPoiProgressListenId = -1;
    private static int inFlightWaylineListenId = -1;

    private static int nightModeSettingsListenId = -1;

    // 飞机在线状态和链路状态监听ID
    private static int droneOnlineStatusListenId = -1;
    private static int linkStatusListenId = -1;

    // LiveAvailableRange 监听ID
    private static int dockLiveAvailableRangeListenId = -1;
    private static int droneLiveAvailableRangeListenId = -1;

    // 文件输出流和相关变量
    private static FileOutputStream dockStreamOutput = null;
    private static FileOutputStream droneStreamOutput = null;
    private static String outputDirectory = "stream_data";
    private static long dockTotalBytes = 0;
    private static long droneTotalBytes = 0;
    private static long dockFrameCount = 0;
    private static long droneFrameCount = 0;

    // 异步文件写入相关
    private static ExecutorService fileWriteExecutor = null;
    private static final int MAX_QUEUE_SIZE = 100; // 最大队列大小
    private static final AtomicLong droppedFrames = new AtomicLong(0);

    // 推流（仅支持机库或者飞机其中一个）
    // private static FFmpegPipeStreamer dockStreamer = null;
    private static FFmpegPipeStreamer droneStreamer = null;
    private static Thread streamMonitorThread = null;

    // 推流测试模块
    private static DroneStreamingTest droneStreamingTest = null;

    // 压力测试模块
    private static StressTest stressTest = null;

    // 循环拉流相关
    private static final AtomicBoolean cyclicStreamRunning = new AtomicBoolean(false);
    private static Thread cyclicStreamThread = null;


    // 拉流压测相关
    private static final AtomicBoolean pullStreamStressTestRunning = new AtomicBoolean(false);
    private static java.util.List<com.dji.flighthubsdk.core.PullStreamModule> pullStreamModules = new java.util.ArrayList<>();
    private static java.util.List<Integer> pullStreamSubscribeIds = new java.util.ArrayList<>();
    private static java.io.FileOutputStream pullStreamStressTestOutput = null;

    // 循环拉流压测相关
    private static final AtomicBoolean cyclicPullStreamStressTestRunning = new AtomicBoolean(false);
    private static Thread cyclicPullStreamStressTestThread = null;

    // SDK 状态跟踪
    private static final AtomicBoolean sdkShutdown = new AtomicBoolean(false);
    private static java.util.List<com.dji.flighthubsdk.core.PullStreamModule> cyclicPullStreamModules = new java.util.ArrayList<>();
    private static java.util.List<Integer> cyclicPullStreamSubscribeIds = new java.util.ArrayList<>();
    // 每路流的统计信息：{frameCount, totalBytes}
    private static java.util.List<java.util.concurrent.atomic.AtomicInteger> cyclicStreamFrameCounts = new java.util.ArrayList<>();
    private static java.util.List<java.util.concurrent.atomic.AtomicLong> cyclicStreamTotalBytes = new java.util.ArrayList<>();

    /**
     * 将时间戳转换为可读的时间格式
     * @param timestamp 时间戳（可能是负数，需要无符号处理）
     * @return 格式化的时间字符串
     */
    private static String formatTimestamp(long timestamp) {
        try {
            // 使用Java 8+原生的无符号长整数处理方法
            long unsignedTimestamp = Long.parseUnsignedLong(Long.toUnsignedString(timestamp));
            
            // 如果时间戳是秒级的（10位），转换为毫秒级
            if (unsignedTimestamp < 1000000000000L) {
                unsignedTimestamp *= 1000;
            }
            
            // 转换为LocalDateTime
            LocalDateTime dateTime = LocalDateTime.ofEpochSecond(
                unsignedTimestamp / 1000, 
                (int) ((unsignedTimestamp % 1000) * 1_000_000), 
                java.time.ZoneOffset.UTC
            );
            
            // 格式化为可读字符串
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return "Invalid timestamp: " + timestamp + " (Error: " + e.getMessage() + ")";
        }
    }
    
    public static void main(String[] args) {
        log("INFO", "main", "=== DJI Flight Hub SDK Java 示例程序 ===");

        // 解析命令行参数
        String jsonConfigPath = null;
        boolean stressTestMode = false;
        boolean autoMonitorMode = false;

        // 解析参数
        for (int i = 0; i < args.length; i++) {
            if ("--stress-test".equals(args[i])) {
                stressTestMode = true;
            } else if ("--auto-monitor".equals(args[i])) {
                autoMonitorMode = true;
            } else if (!args[i].startsWith("--")) {
                // 非选项参数作为配置文件路径
                jsonConfigPath = args[i];
            }
        }
        
        if (jsonConfigPath == null) {
            jsonConfigPath = "config.json";
        }
        
        log("INFO", "main", "使用JSON配置文件路径: " + jsonConfigPath);
        if (stressTestMode) {
            log("INFO", "main", "运行模式: 非交互式循环拉流压测");
        }
        
        // 检测运行环境
        detectEnvironment();
        
        // 设置关闭钩子
        setupShutdownHook();
        
        try {
            // 初始化配置
            log("INFO", "main", "正在加载配置...");
            ConfigManager.initConfig(jsonConfigPath);
            
            // 打印配置信息
            ConfigManager.printConfig();
            
            // 初始化SDK
            if (!initializeSDK()) {
                log("ERROR", "main", "SDK初始化失败，程序退出");
                System.exit(1);
            }

            // 压测模式：跳过设备列表获取和选择，直接启动循环拉流压测
            if (stressTestMode) {
                log("INFO", "main", "压测模式：跳过设备列表获取，直接启动循环拉流压测");
                startCyclicPullStreamStressTestAuto();
                
                // 持续运行，直到被信号终止
                // 注意：主线程必须等待循环拉流压测线程完全停止，否则会导致 SDK 在压测还在运行时被关闭
                try {
                    while (cyclicPullStreamStressTestRunning.get()) {
                        Thread.sleep(1000);
                    }
                    // 等待循环拉流压测线程完全退出
                    if (cyclicPullStreamStressTestThread != null && cyclicPullStreamStressTestThread.isAlive()) {
                        log("INFO", "main", "等待循环拉流压测线程完全退出...");
                        try {
                            cyclicPullStreamStressTestThread.join(10000); // 最多等待10秒
                            if (cyclicPullStreamStressTestThread.isAlive()) {
                                log("WARN", "main", "警告: 循环拉流压测线程未在10秒内结束");
                            } else {
                                log("INFO", "main", "循环拉流压测线程已完全退出");
                            }
                        } catch (InterruptedException e) {
                            log("WARN", "main", "等待循环拉流压测线程退出被中断");
                        }
                    }
                } catch (InterruptedException e) {
                    log("INFO", "main", "主线程被中断，等待循环拉流压测线程停止...");
                    // 即使主线程被中断，也要等待循环拉流压测线程停止
                    if (cyclicPullStreamStressTestThread != null && cyclicPullStreamStressTestThread.isAlive()) {
                        try {
                            cyclicPullStreamStressTestThread.join(10000);
                        } catch (InterruptedException ie) {
                            log("WARN", "main", "等待循环拉流压测线程退出被中断");
                        }
                    }
                }
            } else if (autoMonitorMode) {
                // 全设备监听模式：遍历所有设备，自动注册监听，后台持续运行
                log("INFO", "main", "运行模式: --auto-monitor 全设备监听");
                startAutoMonitor();
            } else {
                // 正常模式：获取设备列表和选择设备
                // 获取设备列表
                if (!getDeviceList()) {
                    log("ERROR", "main", "获取设备列表失败(请检查是否有在线设备), 程序退出");
                    System.exit(1);
                }

                // 选择设备
                selectedDevice = selectDevice();
                if (selectedDevice == null) {
                    log("ERROR", "main", "未选择设备，程序退出");
                    System.exit(1);
                }
                
                Device mainDevice = selectedDevice.getDevice();
                log("INFO", "main", "已选择拓扑根: " + formatTopoDeviceLine(mainDevice, true)
                        + ", dockSn=" + getDockDeviceSn(selectedDevice)
                        + ", baseStationSn=" + getBaseStationDeviceSn(selectedDevice)
                        + ", droneSn=" + getSubDeviceSn(selectedDevice));

                // 收集当前拓扑的主设备 + 子设备 SN 列表
                selectedDeviceSns.clear();
                selectedDeviceSns.addAll(collectDeviceSns(selectedDevice));
                log("INFO", "main", "当前已选择的拓扑设备 SN 列表(" + selectedDeviceSns.size() + "): " + selectedDeviceSns);



                String subDeviceSn = getSubDeviceSn(selectedDevice);
                if (subDeviceSn != null) {
                    drone = getOrCreateDrone(subDeviceSn);
                    if (drone == null) {
                        log("WARN", "main", "获取无人机实例失败，飞机相关菜单可能不可用: " + subDeviceSn);
                    }
                } else {
                    log("WARN", "main", "当前拓扑没有无人机，飞机相关菜单将不可用");
                }

                // 根据飞机型号输入 target payload index
                System.out.println("请根据飞机型号输入 target payload index (例如 \"99-0-0\")，直接回车使用默认值 [" + targetPayloadIndex + "]: ");
                if (isInteractiveMode) {
                    String payloadInput = scanner.nextLine().trim();
                    if (!payloadInput.isEmpty()) {
                        targetPayloadIndex = payloadInput;
                    }
                }
                log("INFO", "main", "📸📸 当前 targetPayloadIndex: " + targetPayloadIndex);

                handleListenOnlineState();

                // 逐个查询设备详情并打印回调数据
                queryAndLogDeviceDetailInfos(selectedDeviceSns);

                // 主循环
                if (isInteractiveMode) {
                    mainLoop();
                } else {
                    // 非交互式环境下的自动演示
                    autoDemo();
                }
            }
            
            // 清理资源（主循环退出后）
            log("INFO", "main", "主循环已退出，开始清理资源...");
            
            // 取消所有监听
            if (drone != null) {
                if (droneOnlineStatusListenId != -1) {
                    drone.getDeviceStateModule().cancelListenDroneOnlineStatus(droneOnlineStatusListenId);
                    droneOnlineStatusListenId = -1;
                }
                if (linkStatusListenId != -1) {
                    drone.getDrcModule().cancelListenLinkStatus(linkStatusListenId);
                    linkStatusListenId = -1;
                }
                if (droneLiveAvailableRangeListenId != -1) {
                    drone.getLiveDeviceModule().cancelListenLiveAvailableRange(droneLiveAvailableRangeListenId);
                    droneLiveAvailableRangeListenId = -1;
                }
            }
            
            if (dock != null) {
                if (dockLiveAvailableRangeListenId != -1) {
                    dock.getLiveDeviceModule().cancelListenLiveAvailableRange(dockLiveAvailableRangeListenId);
                    dockLiveAvailableRangeListenId = -1;
                }
            }
            
            closeStreamOutputs();
            stopPullStreamStressTest();
            stopCyclicPullStreamStressTest();
            
            // 清空静态变量引用，确保 JNI 可以释放 C++ 对象
            // 必须在 shutdown 之前清空，这样 shutdown 时才能正确释放对象
            drone = null;
            dock = null;
            droneLiveModule = null;
            dockLiveModule = null;
            workspace = null;

            // 关闭SDK
            if (!sdkShutdown.get()) {
                FlightHubSdkErrorCode result = FlightHubSDK.shutdown();
                if (result == FlightHubSdkErrorCode.SUCCESS) {
                    log("INFO", "main", "SDK关闭成功");
                    sdkShutdown.set(true);
                } else {
                    log("ERROR", "main", "SDK关闭失败，错误代码: " + result);
                }
            } else {
                log("INFO", "main", "SDK已经关闭，跳过重复关闭");
            }

            log("INFO", "main", "程序正常退出");
            
            // 强制退出，确保所有线程都结束
            // 因为 SDK 内部可能还有非守护线程在运行
            System.exit(0);
            
        } catch (Exception e) {
            log("ERROR", "main", "程序启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 检测运行环境
     */
    private static void detectEnvironment() {
        try {
            // 检测是否在交互式终端中运行
            isInteractiveMode = System.console() != null && System.in.available() == 0;
            
            // 额外检查：如果System.in不可用，则认为是非交互式环境
            if (!isInteractiveMode) {
                try {
                    System.in.available();
                } catch (Exception e) {
                    isInteractiveMode = false;
                }
            }
            
            log("INFO", "detectEnvironment", "运行环境: " + (isInteractiveMode ? "交互式" : "非交互式"));
            
            if (!isInteractiveMode) {
                log("INFO", "detectEnvironment", "检测到非交互式环境，将使用自动演示模式");
            }
            
        } catch (Exception e) {
            log("WARN", "detectEnvironment", "环境检测失败，默认使用非交互式模式: " + e.getMessage());
            isInteractiveMode = false;
        }
    }
    
    /**
     * 设置关闭钩子
     */
    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log("INFO", "shutdownHook", "正在关闭程序...");
            running.set(false);
            
            try {
                // 取消所有监听
                if (drone != null) {
                    if (droneOnlineStatusListenId != -1) {
                        try {
                            drone.getDeviceStateModule().cancelListenDroneOnlineStatus(droneOnlineStatusListenId);
                        } catch (Exception e) {
                            log("WARN", "shutdownHook", "取消无人机在线状态监听失败: " + e.getMessage());
                        }
                    }
                    if (linkStatusListenId != -1) {
                        try {
                            drone.getDrcModule().cancelListenLinkStatus(linkStatusListenId);
                        } catch (Exception e) {
                            log("WARN", "shutdownHook", "取消链路状态监听失败: " + e.getMessage());
                        }
                    }
                    if (droneLiveAvailableRangeListenId != -1) {
                        try {
                            drone.getLiveDeviceModule().cancelListenLiveAvailableRange(droneLiveAvailableRangeListenId);
                        } catch (Exception e) {
                            log("WARN", "shutdownHook", "取消无人机直播范围监听失败: " + e.getMessage());
                        }
                    }
                }
                
                if (dock != null) {
                    if (dockLiveAvailableRangeListenId != -1) {
                        try {
                            dock.getLiveDeviceModule().cancelListenLiveAvailableRange(dockLiveAvailableRangeListenId);
                        } catch (Exception e) {
                            log("WARN", "shutdownHook", "取消机库直播范围监听失败: " + e.getMessage());
                        }
                    }
                }
                
                // 关闭文件输出流
                closeStreamOutputs();
                
                // 停止拉流压测
                stopPullStreamStressTest();
                
                // 停止循环拉流压测
                stopCyclicPullStreamStressTest();
                
                // 清空静态变量引用，确保 JNI 可以释放 C++ 对象
                // 必须在 shutdown 之前清空，这样 shutdown 时才能正确释放对象
                drone = null;
                dock = null;
                droneLiveModule = null;
                dockLiveModule = null;
                workspace = null;
                
                // 关闭SDK（如果还没有关闭）
                if (!sdkShutdown.get()) {
                    FlightHubSdkErrorCode result = FlightHubSDK.shutdown();
                    if (result == FlightHubSdkErrorCode.SUCCESS) {
                        log("INFO", "shutdownHook", "SDK关闭成功");
                        sdkShutdown.set(true);
                    } else {
                        log("ERROR", "shutdownHook", "SDK关闭失败，错误代码: " + result);
                        // 即使关闭失败，也标记为已关闭，避免重复尝试
                        if (result == FlightHubSdkErrorCode.ERROR_NOT_INITIALIZED) {
                            log("INFO", "shutdownHook", "SDK未初始化或已关闭，跳过");
                            sdkShutdown.set(true);
                        }
                    }
                } else {
                    log("INFO", "shutdownHook", "SDK已经关闭，跳过重复关闭");
                }
            } catch (Exception e) {
                log("ERROR", "shutdownHook", "SDK关闭异常: " + e.getMessage());
            }
            log("INFO", "shutdownHook", "程序退出完成");
        }));
    }
    
    /**
     * 主循环
     */
    private static void mainLoop() {
        while (running.get()) {
            try {
                showMainMenu();
                int choice = getUserChoice(0, 9999);  // 覆盖 121-124, 240-260, 300-381 等
                switch (choice) {
                    case 1:
                        handleLiveStream();
                        break;
                    case 2:
                        handleTakePhoto();
                        break;
                    case 3:
                        log("INFO", "mainLoop", "用户选择退出");
                        running.set(false);
                        break;
                    case 4:
                        handleDroneOsd();
                        break;
                    case 5:
                        handleFlyTo();
                        break;
                    case 6:
                        handleFlyToStop();
                        break;
                    case 7:
                        handleCameraState();
                        break;
                    case 8:
                        handleGetFlightControlAuthority();
                        break;
                    case 9:
                        handleReleaseFlightControlAuthority();
                        break;
                    case 10:
                        handleListenFlightControlAuthorityState();
                        break;
                    case 11:
                        handleGetPayloadControlAuthority();
                        break;
                    case 12:
                        handleReleasePayloadControlAuthority();
                        break;
                    case 13:
                        handleListenPayloadControlAuthorityState();
                        break;
                    case 14:
                        handleStopStream();
                        break;
                    case 15:
                        handleStartRecord();
                        break;
                    case 16:
                        handleStopRecord();
                        break;
                    case 17:
                        handleListenOnlineState();
                        break;
                    case 18:
                        handleZoomIn();
                        break;
                    case 19:
                        handleLookAtTarget();
                        break;
                    case 20:
                        handleStartPanorama();
                        break;
                    case 21:
                        handleStopPanorama();
                        break;
                    case 22:
                        handleSwitchCameraMode();
                        break;
                    case 23:
                        handleSwitchLiveLens();
                        break;
                    case 24: // 航线 - 暂停
                        handleFlightTaskPause();
                        break;
                    case 25: // 航线 - 恢复
                        handleFlightTaskResume();
                        break;
                    case 27: // 飞行 - 急停
                        handleFlightEmergencyStop();
                        break;
                    case 28: // 飞行 - WASD
                        handleFlightSticksControl();
                        break;
                    case 31: // 检查功能状态
                        checkFeatureStatus();
                        break;
                    case 32: // 飞行 - 返航
                        handleReturnHome();
                        break;
                    case 33: // 飞行 - 取消返航
                        handleReturnHomeCancel();
                        break;
                    case 34: // 飞行 - 一键起飞
                        handleTakeoff();
                        break;
                    case 35: // 飞行 - POI - 进度监听
                        handleListenAircraftPoiProgress();
                        break;
                    case 36: // 飞行 - POI - 进入POI模式
                        handlePOIEnter();
                        break;
                    case 37: // 飞行 - POI - 停止POI模式
                        handlePOIExit();
                        break;
                    case 38: // 飞行 - POI - 更新POI速度
                        handlePOIUpdateSpeed();
                        break;
                    case 39: // 飞行 - POI - 查询POI任务
                        handlePOIGetTask();
                        break;
                    case 40: // OSD - Drone
                        handleDroneOsd();
                        break;
                    case 41: // OSD - Dock
                        handleDockOsd();
                        break;
                    case 42: // OSD - Workspace
                        handleWorkspaceOsd();
                        break;
                    case 43: // EA340 消息推送补全 (WS)
                        handleEa340MessagePush();
                        break;
                    case 50: // PSDK - 状态监听
                        handlePsdkState();
                        break;
                    case 51: // PSDK - 喊话器 - 重新播放
                        handlePsdkSpeakerReplay();
                        break;
                    case 52: // PSDK - 喊话器 - 停止播放
                        handlePsdkSpeakerPlayStop();
                        break;
                    case 53: // PSDK - 喊话器 - 音量设置
                        handlePsdkSpeakerPlayVolumeSet();
                        break;
                    case 54: // PSDK - 喊话器 - TTS设置
                        handlePsdkSpeakerTTSSet();
                        break;
                    case 55: // PSDK - 喊话器 - 播放模式设置 - 循环播放
                        handlePsdkSpeakerPlayModeSet();
                        break;
                    case 56: // PSDK - 喊话器 - TTS播放
                        handlePsdkSpeakerTTSPlayStart();
                        break;
                    case 57: // PSDK - 喊话器 - 文件播放
                        handlePsdkSpeakerAudioPlayStart();
                        break;
                    case 58: // PSDK - 控件 - 发送文本框内容
                        handlePsdkWidgetInputBoxTextSet();
                        break;
                    case 59: // PSDK - 控件 - 设置 widget 值
                        handlePsdkWidgetValueSet();
                        break;
                    case 61: // PSDK - 喊话器 - 创建语音/文本资源
                        handleCreatePsdkSpeakerResource();
                        break;
                    case 62: // PSDK - 喊话器 - 修改语音/文本资源
                        handleEditPsdkSpeakerResource();
                        break;
                    case 63: // PSDK - 喊话器 - 删除语音/文本资源
                        handleDeletePsdkSpeakerResource();
                        break;
                    case 64: // PSDK - 喊话器 - 查询列表语音/文本资源
                        handleGetPsdkSpeakerResourceList();
                        break;
                    case 65: // PSDK - 喊话器 - 获取临时上传凭证
                        handleGetSTS();
                        break;
                    case 66: // PSDK - 获取PSDK控件资源包索引
                        handleGetPsdkWidgetResources();
                        break;
                    case 67: // PSDK - 喊话器 - 模拟上传音频文件
                        handleUploadSpeakerAudioFile();
                        break;
                    case 80: // 监听直播镜头信息
                        handleListenLiveVideoInfo();
                        break;
                    case 82: // 监听红外增益模式
                        handleListenThermalGainMode();
                        break;
                    case 84: // 设置红外增益模式
                        handleSetThermalGainMode();
                        break;
                    case 85: // 监听飞行任务异常
                        handleListenFlightTasksException();
                        break;
                    case 86: // 监听工作空间设备变更
                        handleListenWorkspaceDeviceChanged();
                        break;
                    case 87: // 监听设备拓扑变更（Drone）
                        handleListenDeviceTopoChangedDrone();
                        break;
                    case 88: // 监听设备拓扑变更（Dock）
                        handleListenDeviceTopoChangedDock();
                        break;
                    case 89: // 线程堵塞测试（getDeviceList 回调内 sleep）
                        handleThreadBlockingTest();
                        break;
                    case 90: // 自定义 SEI 测试
                        handleCustomSeiTest();
                        break;
                    case 91: // 自定义 SEI 编解码测试（无需设备）
                        handleCustomSeiCodecTest();
                        break;
                    case 92: // 无人机推流测试（基于状态自动控制）
                        handleDroneStreamingTest();
                        break;
                    case 93: // 拉流模块测试
                        handlePullStreamTest();
                        break;
                    case 94: // 飞机码流 + 写入自定义SEI + RTMP 推流
                        handleSeiRtmpStream();
                        break;
                    case 95: // [95] 负载控制 - 变焦
                        handleSetFocalLength();
                        break;
                    case 100: // 机场控制 - 补光灯 - 开启
                        handleDockSupplementLightOpen();
                        break;
                    case 101: // 机场控制 - 补光灯 - 关闭
                        handleDockSupplementLightClose();
                        break;
                    case 102: // 机场控制 - 充电 - 开启
                        handleDockChargeOpen();
                        break;
                    case 103: // 机场控制 - 充电 - 关闭
                        handleDockChargeClose();
                        break;
                    case 104: // 机场控制 - 舱盖 - 开启
                        handleDockCoverOpen();
                        break;
                    case 105: // 机场控制 - 舱盖 - 关闭
                        handleDockCoverClose();
                        break;
                    case 106: // 机场控制 - 舱盖 - 强制关闭
                        handleDockCoverForceClose();
                        break;
                    case 107: // 机场控制 - 空调模式
                        handleDockAirConditionerMode();
                        break;
                    case 108: // 设备调试
                        handleDeviceDebug();
                        break;
                    case 109: // DRC 链路 - 连接/断开循环测试
                        handleDrcLinkCycleTest();
                        break;
                    case 113: // DRC 链路 - 手动连接
                        handleDrcLinkConnect();
                        break;
                    case 114: // DRC 链路 - 手动断开
                        handleDrcLinkDisconnect();
                        break;
                    case 115: // DRC 操控 - 云台重置
                        handleResetGimbal();
                        break;
                    case 121: // 组织测试
                        handleWorkspace();
                        break;
                    case 122: // 日志测试
                        handleLogTest();
                        break;
                    case 123: // 固件管理测试
                        handleFirmwareManagementTest();
                        break;
                    case 124: // 限飞测试
                        handleFlySafeTest();
                        break;
                    case 125: // 航线管理测试
                        handleWaylineTest();
                        break;
                    case 126: // 机载算法管理测试（PAAS 纯血）
                        handleAIInsideTest();
                        break;
                    case 127: // HMS 测试
                        handleHmsTest();
                        break;
                    case 128: // AI 告警管理测试（PAAS 纯血）
                        handleAiAlertTest();
                        break;
                    case 129: // 直播转发测试（live-sync）
                        handleLiveSyncTest();
                        break;
                    case 223: // 空中下发航线测试
                        handleInFlightWaylineTest();
                        break;
                    case 200: // 压力测试模式
                        handleStressTest();
                        break;
                    case 201: // 循环拉流
                        handleCyclicLiveStream();
                        break;
                    case 202: // 停止循环拉流
                        handleStopCyclicLiveStream();
                        break;
                    case 203: // 拉流压测
                        handlePullStreamStressTest();
                        break;
                    case 204: // 循环拉流压测
                        handleCyclicPullStreamStressTest();
                        break;
                    case 205: // 停止循环拉流压测
                        handleStopCyclicPullStreamStressTest();
                        break;
                    case 240: // 机场控制 - 切换舱内/舱外摄像头
                        handleDockChangeCameraPosition();
                        break;
                    case 241: // 直播录制 - 开始录制
                        handleLiveRecordStart();
                        break;
                    case 242: // 直播录制 - 停止录制
                        handleLiveRecordStop();
                        break;
                    case 243: // 直播录制 - 查询设备录制记录
                        handleLiveRecordGetRecords();
                        break;
                    case 244: // 直播录制 - 订阅录制状态
                        handleLiveRecordListenState();
                        break;
                    case 245: // 直播录制 - 取消订阅录制状态
                        handleLiveRecordCancelListenState();
                        break;
                    case 246: // 获取在线观众数量
                        handleGetAudienceCount();
                        break;
                    case 247: // 设置直播画质（drone）
                        handleSetLiveQuality();
                        break;
                    case 248: // 设置直播画质（dock）
                        handleSetDockLiveQuality();
                        break;
                    case 250: // 航线任务 - 获取任务列表
                        handleGetFlightTaskList();
                        break;
                    case 251: // 航线任务 - 创建飞行计划
                        handleCreateFlightTask();
                        break;
                    case 252: // 航线任务 - 获取任务详情
                        handleGetFlightTaskDetail();
                        break;
                    case 253: // 航线任务 - 编辑飞行计划
                        handleEditFlightTask();
                        break;
                    case 254: // 航线任务 - 删除飞行计划
                        handleDeleteFlightTask();
                        break;
                    case 255: // 航线任务 - 漫游
                        handleRoaming();
                        break;
                    case 256: // 航线任务 - 更新任务状态
                        handleUpdateFlightTaskStatus();
                        break;
                    case 257: // 航线任务 - 批量更新任务状态
                        handleBatchUpdateFlightTaskStatus();
                        break;
                    case 258: // 航线任务 - 获取设备推荐
                        handleGetDeviceRecommendation();
                        break;
                    case 259: // 航线任务 - 获取设备TCA
                        handleGetDeviceTcas();
                        break;
                    case 260: // 航线任务 - 更新设备TCA
                        handleUpdateDeviceTcas();
                        break;
                    case 261: // 负载控制 - 设置拍照存储设置
                        handleSetPhotoStorageSettings();
                        break;
                    case 262: // 负载控制 - 设置录像存储设置
                        handleSetVideoStorageSettings();
                        break;
                    case 263: // 负载控制 - 设置夜景模式
                        handleSetNightSceneMode();
                        break;
                    case 264: // 负载控制 - 监听夜景模式状态
                        handleListenNightModeSettings();
                        break;
                    case 265: // 负载控制 - 取消监听夜景模式状态
                        handleCancelListenNightModeSettings();
                        break;
                    case 280: // PSDK 探照灯 - 工作模式设置
                        handlePsdkSearchlightModeSet();
                        break;
                    case 281: // PSDK 探照灯 - 亮度设置
                        handlePsdkSearchlightBrightnessSet();
                        break;
                    case 282: // PSDK 探照灯 - 云台自动校准
                        handlePsdkSearchlightCalibration();
                        break;
                    case 283: // PSDK 探照灯 - 云台微调
                        handlePsdkSearchlightFineTuningSet();
                        break;
                    case 284: // PSDK 探照灯 - 广视野照明开关
                        handlePsdkSearchlightWideFieldModeSet();
                        break;
                    case 285: // PSDK 探照灯 - 云台联动开关
                        handlePsdkSearchlightLightGimbalControlSet();
                        break;
                    case 286: // PSDK 探照灯 - 灯光类型
                        handlePsdkSearchlightTypeSet();
                        break;
                    case 270: // SDK信息 - 获取版本号和构建信息
                        handleGetSDKVersionInfo();
                        break;
                    case 291: // 直播测试 - 循环拉流/断流测试
                        handleCyclicLiveStreamTest();
                        break;
                    case 292: // 一键起飞任务 - 获取任务列表
                        handleGetCommandTaskList();
                        break;
                    case 293: // 飞行记录 - 查询飞行任务轨迹
                        handleGetFlightTaskTrack();
                        break;
                     // [300-339] for 媒体服务模块;
                     case 300:
                         // [媒体服务模块] 监听媒体服务状态 [设备媒体回传通知][设备媒体后处理完成通知]
                         handleMediaServiceListen();
                         break;
                     case 301:
                         handleMediaServiceBasicFunctionality();
                         break;
                     case 302: handleMediaUploadGetSts(); break;
                     case 303: handleMediaUploadCallback(); break;
                     case 304: handleUploadGeoMediaFile(); break;
                     case 305: handleGetMediaFileDetailById(); break;
                     case 306: handleGetWaylineEditorSts(); break;
                     case 307: handleDeleteGeoMediaFile(); break;
                     // [310-317] for 爆闪灯 flash_led / flash-configs;
                     case 310: handleFlashLedListen(); break;
                     case 311: handleFlashLedSetOnPreset(); break;
                     case 312: handleFlashLedSetOff(); break;
                     case 313: handleFlashLedConfigList(); break;
                     case 314: handleFlashLedConfigCreate(); break;
                     case 315: handleFlashLedConfigUpdate(); break;
                     case 316: handleFlashLedConfigDelete(); break;
                     case 317: handleFlashLedApplyTemplate(); break;
                     case 318: handleEa610BaseStationLiveTest(); break;
                     // [400-429] for Terra Build 重建模块;
                     case 400: handleTerraBuildGetModelFileList(); break;
                     case 401: handleTerraBuildGetModelFileUrl(); break;
                     case 402: handleTerraBuildGetReconstructionSettings(); break;
                     case 403: handleTerraBuildGetQualityReport(); break;
                     case 404: handleTerraBuildCreateReconstruction2D(); break;
                     case 405: handleTerraBuildCreateReconstruction3D(); break;
                     case 406: handleTerraBuildStopReconstruction(); break;
                     case 407: handleTerraBuildRetryReconstruction(); break;
                     case 409: handleTerraBuildGetReconstructionImages(); break;
                     case 410: handleTerraBuildDeleteModelFile(); break;
                     case 413: handleTerraBuildBatchDeleteModelFiles(); break;
                     case 411: handleTerraBuildCreatePanorama(); break;
                     case 412: handleTerraBuildListenReconstructionStatus(); break;
                     case 414: handleTerraUploadModelCallback(); break;
                     case 415: handleTerraRenameModel(); break;
                     case 416: handleTerraShowModelOnMap(); break;
                     case 6000: handleTerraHideModelOnMap(); break;
                     case 417: handleTerraListenUnzipProgress(); break;
                     case 418: handleTerraListenModelStatusChanged(); break;
                     case 419: handleTerraListenFileShowOnMap(); break;
                     case 4190: handleTerraListenMediaFileHideOnMap(); break;
                     case 4191: handleTerraListenMediaFileShowOnMap(); break;
                     case 491: handleTerraGetModelDetail(); break;
                     case 492: handleTerraSetLasWkt(); break;
                    // [340-379] for 安全服务模块;
                    case 340:
                        // [安全服务模块] - [获取信号地图]
                        handleSearchSignalMaps();
                        break;
                    case 341:
                        // [安全服务模块] - [创建自定义飞行区域]
                        handleCreateCustomizedFlightAreas();
                        break;
                    case 342:
                        // [安全服务模块] - [获取自定义飞行区域]
                        handleGetCustomizedFlightAreas();
                        break;
                    case 343:
                        // [安全服务模块] - [更新自定义飞行区域]
                        handleUpdateCustomizedFlightAreas();
                        break;
                    case 344:
                        // [安全服务模块] - [删除自定义飞行区域]
                        handleDeleteCustomizedFlightAreas();
                        break;
                    case 345:
                        // [安全服务模块] - [同步自定义飞行区域]
                        handleSyncCustomizedFlightAreas();
                        break;
                    case 346:
                        // [安全服务模块] - [获取自定义飞行区域同步状态]
                        handleGetFlightAreasSyncStatus();
                        break;
                    case 350:
                        // [安全服务模块][地形障碍物] - [查询离线地图模型信息]
                        handleGetOfflineMapsModelInfo();
                        break;
                    case 351:
                        // [安全服务模块][地形障碍物] - [创建/更新离线地图模型]
                        handleUpdateOfflineMapsModelInfo();
                        break;
                    case 352:
                        // [安全服务模块][地形障碍物] - [取消离线地图生成]
                        handleCancelOfflineMapsModelInfo();
                        break;
                    case 353:
                        // [安全服务模块][地形障碍物] - [修改离线地图开关]
                        handleSetOfflineMaps();
                        break;
                    case 354:
                        // [安全服务模块][地形障碍物] - [同步离线地图至设备]
                        handleSyncOfflineMaps();
                        break;
                    case 355:
                        // [安全服务模块][地形障碍物] - [获取设备离线地图同步状态]
                        handleGetOfflineMapsSyncStatus();
                        break;
                    // [380-400] for ATC 空管服务:飞行任务记录模块;
                    case 380:
                        // [飞行任务记录模块] - [导出飞行记录]
                        handleExportFlightRecords();
                        break;
                    case 381:
                        // [飞行任务记录模块] - [查询导出进度]
                        handleGetExportFlightRecordsProgress();
                        break;
                    // [390-399] for 飞行安全配置;
                    case 390:
                        handleSetFlightSmartPlanMode();
                        break;
                    case 391:
                        handleSetOutOfControlAction();
                        break;
                    case 392:
                        handleSetFlightTaskHeight();
                        break;
                    case 393:
                        handleSetRthAltitude();
                        break;
                    case 394:
                        handleSetAirTransferEnable();
                        break;
                    case 395:
                        handleUpdateTargetPoints();
                        break;
                    // [420-429] for 报修管理模块;
                    case 420: handleMaintenanceGetRepairConfigs(); break;
                    case 421: handleMaintenanceCreateOrder(); break;
                    case 422: handleMaintenanceGetOrderDetail(); break;
                    case 423: handleMaintenanceGetOrdersList(); break;
                    // [430-439] for 定损管理模块(DiagnosisManagementModule);
                    case 430: handleDiagnosisCreate(); break;
                    case 431: handleDiagnosisOperate(); break;
                    case 432: handleDiagnosisGetReport(); break;
                    case 433: handleDiagnosisGetReportList(); break;
                    // [440-449] for AI Box 管理模块;
                    case 440: handleAIBoxFetchInfo(); break;
                    case 441: handleAIBoxFetchFileDownloadUrl(); break;
                    case 442: handleAIBoxFetchAppList(); break;
                    case 443: handleAIBoxFetchAppListProgress(); break;
                    case 444: handleAIBoxExportFile(); break;
                    case 445: handleAIBoxExportFileProgress(); break;
                    case 446: handleAIBoxUploadFile(); break;
                    case 447: handleAIBoxInstallApp(); break;
                    case 448: handleAIBoxInstallAppProgress(); break;
                    case 449: handleAIBoxOperateApp(); break;
                    case 450: handleListenTaskEvents(); break;
                    case 460: handleHyapp177995Repro(); break;
                    case 500: handleDrcCameraApertureSet(); break;
                    case 501: handleDrcCameraIsoSet(); break;
                    case 502: handleDrcCameraShutterSet(); break;
                    case 503: handleDrcCameraExposureModeSet(); break;
                    case 504: handleDrcCameraFocusModeSet(); break;
                    case 505: handleDrcCameraPointFocus(); break;
                    case 506: handleDrcCameraDewarpingSet(); break;
                    case 507: handleDrcCameraScreenSplit(); break;
                    case 508: handleDrcCleanFog(); break;
                    case 509: handleDrcThermalPaletteSet(); break;
                    case 510: handleDrcIrMeteringModeSet(); break;
                    case 511: handleDrcIsothermEnable(); break;
                    case 512: handleDrcAiSpotlightZoomSet(); break;
                    case 513: handleDrcAiSpotlightZoomSelect(); break;
                    case 514: handleDrcAiSpotlightZoomStop(); break;
                    case 521: handleAiIdentifyFilterSet(); break;
                    case 522: handleAiIdentifyScoreSet(); break;
                    case 523: handleAiIdentifyScoreModeSet(); break;
                    case 524: handleAiIdentifyScoreReset(); break;
                    case 525: handleListenAIDataInfo(); break;
                    case 526: handleAiModelsConfig(); break;
                    case 515: handleDrcDroneHoverSet(); break;
                    case 516: handleDrcNightLightsSet(); break;
                    case 517: handleDrcStealthStateSet(); break;
                    case 518: handleDrcListenPushData(); break;
                    case 519: handleDrcStopListenPushData(); break;
                     case 520: handleDrcRtcQualityTest(); break;
                     case 294: handleModuleCacheTest(); break;
                     // ── Element 模块 ───────────────────────────────────────
                     case 601: handleElementCreateElement(); break;
                     case 602: handleElementUpdateElement(); break;
                     case 603: handleElementListHierarchy(); break;
                     case 604: handleElementSetVisibility(); break;
                     case 605: handleElementDeleteElement(); break;
                     case 606: handleElementListenCreate(); break;
                     case 607: handleElementListenUpdate(); break;
                     case 608: handleElementListenDelete(); break;
                    default:
                        log("WARN", "mainLoop", "无效的选择: " + choice);
                        break;
                }
            } catch (Exception e) {
                log("ERROR", "mainLoop", "主循环异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 初始化SDK
     */
    private static boolean initializeSDK() {
        printSeparator("初始化SDK");
        
        try {
            // 加载JNI库
            log("INFO", "initializeSDK", "正在加载JNI库...");
            JniLibraryLoader.loadLibrary();
            log("INFO", "initializeSDK", "JNI库加载完成");

            // 获取配置信息
            ConfigManager.DemoConfig config = ConfigManager.getConfig();

            // 验证必要的配置
            if (config.server.address == null || config.server.address.trim().isEmpty()) {
                throw new RuntimeException("服务器地址未配置");
            }

            // 构建服务器URL
            String serverUrl = config.server.address;
            if (config.server.port != null && !config.server.port.trim().isEmpty()) {
                serverUrl += ":" + config.server.port;
            }
            log("INFO", "initializeSDK", "服务器URL: " + serverUrl);
            log("INFO", "initializeSDK", "日志级别: " + config.sdk.log_level);
            log("INFO", "initializeSDK", "终端日志: " + config.sdk.log_to_console);
            log("INFO", "initializeSDK", "日志文件路径: " + config.sdk.log_file_path);
            log("INFO", "initializeSDK", "日志文件最大大小: " + config.sdk.log_max_file_size + " bytes");
            log("INFO", "initializeSDK", "最大日志文件数量: " + config.sdk.log_max_files);
            
            // 创建SDK配置对象
            FlightHubSdkConfig sdkConfig = new FlightHubSdkConfig();
            sdkConfig.setLogLevel(config.sdk.log_level != null ? config.sdk.log_level : "info");
            sdkConfig.setLogToConsole(config.sdk.log_to_console);
            sdkConfig.setLogFilePath(config.sdk.log_file_path != null ? config.sdk.log_file_path : "flight_hub_sdk.log");
            sdkConfig.setLogMaxFileSize(config.sdk.log_max_file_size);
            sdkConfig.setLogMaxFiles(config.sdk.log_max_files);
            sdkConfig.setShowVersionInfo(true);
            sdkConfig.setAddress(config.server.address);
            sdkConfig.setPort(config.server.port != null ? config.server.port : "");
            sdkConfig.setNotificationServerAddress(config.server.notification_server);
            // TLS: 私有化自签名环境经 server.verify_ssl / server.ssl_ca_path 控制(默认 false, 生产环境应置 true + 私有 CA)
            sdkConfig.setVerifySsl(config.server.verify_ssl);
            sdkConfig.setSslCaPath(config.server.ssl_ca_path != null ? config.server.ssl_ca_path : "");

            // DRTC：环境变量 > config.json sdk.drtc_config_path > jar/CWD 旁 drtc_config_node.json
            String drtcConfigPath = System.getenv("FHSDK_DRTC_CONFIG");
            if (drtcConfigPath == null || drtcConfigPath.trim().isEmpty()) {
                drtcConfigPath = config.sdk.drtc_config_path;
            }
            String resolvedDrtc = ConfigManager.resolveExistingFile(drtcConfigPath);
            if (resolvedDrtc == null) {
                resolvedDrtc = ConfigManager.resolveExistingFile("drtc_config_node.json");
            }
            if (resolvedDrtc != null) {
                sdkConfig.setDrtcConfigPath(resolvedDrtc);
                log("INFO", "initializeSDK", "DRTC config: " + resolvedDrtc);
            } else {
                log("WARN", "initializeSDK",
                        "未找到 DRTC 配置；飞机拉流会 Init 失败。可设 FHSDK_DRTC_CONFIG、config.json sdk.drtc_config_path，"
                                + "或将 drtc_config_node.json 放在 jar 同级（build 会自动拷贝到 build/libs）");
            }

            // Gateway 鉴权头：来自 config.json auth.*，空则回退环境变量 FLIGHTHUB_AUTH_VALUE
            String authHeaderKey = ConfigManager.resolveAuthHeaderKey();
            String authHeaderValue = ConfigManager.resolveAuthHeaderValue();
            if (authHeaderValue.isEmpty()) {
                log("ERROR", "initializeSDK",
                        "未配置鉴权头：请在 config.json 填写 auth.header_value，或设置环境变量 "
                                + ConfigManager.ENV_AUTH_VALUE);
                return false;
            }
            List<HeaderKeyValue> gatewayAuthHeaders = new ArrayList<>();
            gatewayAuthHeaders.add(new HeaderKeyValue(authHeaderKey, authHeaderValue));

            FlightHubSdkErrorCode wsHeaderResult = FlightHubSDK.setWebSocketAuthHeaders(gatewayAuthHeaders);
            FlightHubSdkErrorCode httpHeaderResult = FlightHubSDK.setHttpHeaders(gatewayAuthHeaders);
            log("INFO", "initializeSDK", "设置WebSocket鉴权头结果: " + wsHeaderResult
                    + ", headers: " + authHeaderKey + "=***");
            log("INFO", "initializeSDK", "设置HTTP鉴权头结果: " + httpHeaderResult
                    + ", headers: " + authHeaderKey + "=***");
            if (wsHeaderResult != FlightHubSdkErrorCode.SUCCESS || httpHeaderResult != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "initializeSDK", "SDK鉴权头设置失败，取消初始化");
                return false;
            }
            
            // 初始化SDK
            FlightHubSdkErrorCode result = FlightHubSDK.initialize(sdkConfig);
            
            if (result != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "initializeSDK", "SDK初始化失败，错误代码: " + result);
                return false;
            }
            
            log("INFO", "initializeSDK", "SDK初始化成功！");
            
            // 设置日志级别
            if (config.sdk.log_level != null) {
                FlightHubSDK.setLogLevel(config.sdk.log_level);
                log("INFO", "initializeSDK", "日志级别设置为: " + config.sdk.log_level);
            }
            
            // 打印SDK版本信息
            String version = FlightHubSDK.getVersion();
            String buildInfo = FlightHubSDK.getBuildInfo();
            log("INFO", "initializeSDK", "SDK版本: " + version);
            log("INFO", "initializeSDK", "构建信息: " + buildInfo);
            
            return true;
            
        } catch (Exception e) {
            log("ERROR", "initializeSDK", "SDK初始化失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取并显示设备列表
     */
    private static boolean getDeviceList() {
        printSeparator("获取设备列表");
        
        final AtomicBoolean deviceListReady = new AtomicBoolean(false);
        final AtomicBoolean getDeviceSuccess = new AtomicBoolean(false);
        
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        
        workspace = FlightHubSDK.getWorkspaceInstance();
        workspace.getDeviceManageModule().getDeviceList(new FlightHubCallback.DeviceListCallback() {
            @Override
            public void onResult(java.util.List<DeviceTopo> devices, FlightHubError error) {
                if (error.isSuccess()) {
                    log("INFO", "getDeviceList", "获取设备列表成功，拓扑数量: " + devices.size());
                    
                    deviceList.clear();
                    int index = 1;

                    for (DeviceTopo deviceTopo : devices) {
                        Device mainDevice = deviceTopo.getDevice();
                        // 打印设备信息;
                        if (mainDevice != null) {
                            if (config != null) {
                                Device dockDevice = findDeviceByClass(deviceTopo, "dock", "airport");
                                if (dockDevice != null && dockDevice.isOnline()) {
                                    config.devices.dock_sns.add(dockDevice.getDeviceSn());
                                }
                            }

                            log("INFO", "getDeviceList", "mainDevice: " + "{"
                                + "deviceSn: " + "\"" + mainDevice.getDeviceSn() + "\"" + ", "
                                + "deviceClass: " + "\"" + mainDevice.getDeviceClass() + "\"" + ", "
                                + "deviceName: " + "\"" + mainDevice.getDeviceName() + "\"" + ", "
                                + "nickname: " + "\"" + mainDevice.getNickname() + "\"" + ", "
                                + "modelKey: " + "\"" + mainDevice.getModelKey() + "\"" + ", "
                                + "isOnline: " + mainDevice.isOnline()
                                + "}");

                            java.util.List<Device> subDevices = deviceTopo.getSubDevices();
                            if (subDevices != null && !subDevices.isEmpty()) {
                                for (Device subDevice : subDevices) {
                                    // TODO: may add drone sns for config;

                                    log("INFO", "getDeviceList", "  --> subDevice: " + "{"
                                        + "deviceSn: " + "\"" + subDevice.getDeviceSn() + "\"" + ", "
                                        + "deviceClass: " + "\"" + subDevice.getDeviceClass() + "\"" + ", "
                                        + "deviceName: " + "\"" + subDevice.getDeviceName() + "\"" + ", "
                                        + "nickname: " + "\"" + subDevice.getNickname() + "\"" + ", "
                                        + "modelKey: " + "\"" + subDevice.getModelKey() + "\"" + ", "
                                        + "isOnline: " + subDevice.isOnline()
                                        + "}");
                                }
                            }
                        }

                        if (mainDevice == null || !mainDevice.isOnline()) {
                            continue;
                        }
                        deviceList.add(deviceTopo);

                        log("INFO", "getDeviceList", "[" + index + "] "
                                + formatTopoDeviceLine(mainDevice, true));
                        
                        java.util.List<Device> subDevices = deviceTopo.getSubDevices();
                        if (subDevices != null && !subDevices.isEmpty()) {
                            for (Device subDevice : subDevices) {
                                log("INFO", "getDeviceList", "    -> "
                                        + formatTopoDeviceLine(subDevice, false));
                            }
                        }
                        log("INFO", "getDeviceList", "    [角色] dockSn=" + getDockDeviceSn(deviceTopo)
                                + " baseStationSn=" + getBaseStationDeviceSn(deviceTopo)
                                + " droneSn=" + getSubDeviceSn(deviceTopo));
                        
                        index++;
                    }

                    getDeviceSuccess.set(true);
                } else {
                    log("ERROR", "getDeviceList", "获取设备列表失败，错误: " + error);
                    getDeviceSuccess.set(false);
                }
                deviceListReady.set(true);
            }
        });
        
        // 等待回调完成
        while (!deviceListReady.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return getDeviceSuccess.get() && !deviceList.isEmpty();
    }
    
    /**
     * 选择拓扑
     */
    private static DeviceTopo selectDevice() {
        if (deviceList.isEmpty()) {
            log("ERROR", "selectDevice", "没有可用拓扑");
            return null;
        }
        
        if (deviceList.size() == 1) {
            DeviceTopo device = deviceList.get(0);
            log("INFO", "selectDevice", "自动选择唯一拓扑: " + formatTopoDeviceLine(device.getDevice(), true)
                    + ", dockSn=" + getDockDeviceSn(device));
            return device;
        }
        
        printSeparator("选择拓扑");
        System.out.println("可用拓扑:");
        for (int i = 0; i < deviceList.size(); i++) {
            DeviceTopo deviceTopo = deviceList.get(i);
            Device mainDevice = deviceTopo.getDevice();
            System.out.println("[" + (i + 1) + "] " + formatTopoDeviceLine(mainDevice, true));
            
            java.util.List<Device> subDevices = deviceTopo.getSubDevices();
            if (subDevices != null && !subDevices.isEmpty()) {
                for (Device subDevice : subDevices) {
                    System.out.println("    -> " + formatTopoDeviceLine(subDevice, false));
                }
            }
        }
        
        int choice = getUserChoice(1, deviceList.size());
        return deviceList.get(choice - 1);
    }
    
    /**
     * 自动演示模式（非交互式环境）
     */
    /**
     * 全设备监听模式（--auto-monitor）
     * 覆盖 BusinessManagerImpl.h 中全部 topic，机库+无人机全量订阅。
     * 用途：配合 ss_probe / tcpdump / ws_monitor 排查 WebSocket 稳定性问题。
     */
    private static void startAutoMonitor() {
        if (!getDeviceList()) {
            log("ERROR", "startAutoMonitor", "获取设备列表失败，退出");
            System.exit(1);
        }
        log("INFO", "startAutoMonitor", "共 " + deviceList.size() + " 个设备，开始全量注册监听...");

        // workspace 级别
        workspace.getDeviceManageModule().listenWorkspaceDeviceChanged(msg ->
            log("INFO", "workspace", "设备变更 changes=" + msg.getDeviceChanges()));

        for (DeviceTopo topo : deviceList) {
            final String dockSn = getMainDeviceSn(topo);

            // ---- 机库 ----
            Dock dockInst = getOrCreateDock(dockSn);
            Drone droneForDock = null;
            if (hasSubDevice(topo)) {
                droneForDock = getOrCreateDrone(getSubDeviceSn(topo));
            }
            if (dockInst != null) {
                setupDockListeners(dockSn, dockInst, droneForDock);
            } else {
                log("WARN", "startAutoMonitor", "getDockInstance 失败: " + dockSn);
            }

            // ---- 无人机 ----
            if (!hasSubDevice(topo)) continue;
            final String droneSn = getSubDeviceSn(topo);
            Drone droneInst = getOrCreateDrone(droneSn);
            if (droneInst != null) {
                setupDroneListeners(droneSn, droneInst);
            } else {
                log("WARN", "startAutoMonitor", "getDroneInstance 失败: " + droneSn);
            }
        }

        log("INFO", "startAutoMonitor", "所有监听已注册，后台运行中（Ctrl+C 退出）");
        try {
            while (true) {
                Thread.sleep(60_000);
                log("INFO", "heartbeat", "运行中...");
            }
        } catch (InterruptedException e) {
            log("INFO", "startAutoMonitor", "退出");
        }
    }

    /** 无人机全量 topic 监听（对应 BusinessManagerImpl.h 中 drone/task 相关接口） */
    private static void setupDroneListeners(String sn, Drone d) {
        // -- FHSDK 封装层 --
        d.getDeviceStateModule().listenDroneOnlineStatus(s -> log("INFO", sn, "在线状态=" + s.isOnline()));
        d.getDrcModule().listenLinkStatus(s -> log("INFO", sn, "链路状态=" + s.getCtrlLinkStatus()));

        // -- 飞机 OSD --
        d.getDroneOsdModule().listenAircraftLocation(m -> log("INFO", sn,
            String.format("位置 lat=%.2f lon=%.2f h=%.1fm", m.getLatitude(), m.getLongitude(), m.getHeight())));
        d.getDroneOsdModule().listenAircraftSpeed(m -> log("INFO", sn,
            String.format("速度 h=%.2f v=%.2f m/s", m.getHorizontalSpeed(), m.getVerticalSpeed())));
        d.getDroneOsdModule().listenAircraftAttitude(m -> log("INFO", sn,
            String.format("姿态 pitch=%.1f roll=%.1f yaw=%.1f", m.getAttitudePitch(), m.getAttitudeRoll(), m.getAttitudeHead())));
        d.getDroneOsdModule().listenAircraftBatteryInfo(m -> log("INFO", sn,
            "电量=" + m.getCapacityPercent() + "% 剩余飞行=" + m.getRemainFlightTime() + "s"));
        d.getDroneOsdModule().listenAircraftModeCode(m -> log("INFO", sn, "模式码=" + m.getModeCode()));
        d.getDroneOsdModule().listenAircraftWindSpeed(m -> log("INFO", sn,
            "风速=" + m.getWindSpeed() + " 风向=" + m.getWindDirection()));
        d.getDroneOsdModule().listenAircraftControlCode(m -> log("INFO", sn, "控制码=" + m.getControlMode()));
        d.getDroneOsdModule().listenAircraftGear(m -> log("INFO", sn, "起落架=" + m.getGear()));
        d.getDroneOsdModule().listenAircraftHomeDistance(m -> log("INFO", sn, "返航距离=" + m.getHomeDistance() + "m"));
        d.getDroneOsdModule().listenAircraftPositionState(m -> log("INFO", sn,
            "定位 fixed=" + m.getIsFixed() + " quality=" + m.getQuality()));
        d.getDroneOsdModule().listenAircraftControlSource(m -> log("INFO", sn, "控制源=" + m.getControlSource()));
        d.getDroneOsdModule().listenAircraftLimitsInfo(m -> log("INFO", sn, "限制信息 received"));
        d.getDroneOsdModule().listenAircraftBatteryWarningThresholds(m -> log("INFO", sn, "电池告警阈值 received"));
        d.getDroneOsdModule().listenAircraftFirmwareStatus(m -> log("INFO", sn, "固件状态 received"));
        d.getDroneOsdModule().listenAircraftTotalFlightInfo(m -> log("INFO", sn,
            "累计飞行 time=" + m.getTotalFlightTime() + "s dist=" + m.getTotalFlightDistance() + "m"));
        d.getDroneOsdModule().listenAircraftActivationTime(m -> log("INFO", sn, "激活时间=" + m.getActivationTime()));
        d.getDroneOsdModule().listenAircraftMaintainStatusInfo(m -> log("INFO", sn, "维护状态 received"));
        d.getDroneOsdModule().listenAircraftNightLightsState(m -> log("INFO", sn, "夜航灯=" + m.getNightLightsState()));
        d.getDroneOsdModule().listenAircraftObstacleAvoidanceState(m -> log("INFO", sn, "避障状态 received"));
        d.getDroneOsdModule().listenAircraftStorageInfo(m -> log("INFO", sn,
            "存储 total=" + m.getTotal() + " used=" + m.getUsed()));
        d.getDroneOsdModule().listenAircraftTrackId(m -> log("INFO", sn, "轨迹ID=" + m.getTrackId()));
        d.getDroneOsdModule().listenAircraftComplianceInfo(m -> log("INFO", sn, "合规信息 received"));
        d.getDroneOsdModule().listenAircraftRcLostAction(m -> log("INFO", sn, "失控动作=" + m.getRcLostAction()));
        d.getDroneOsdModule().listenAircraftRthInfo(m -> log("INFO", sn,
            "返航信息 mode=" + m.getRthMode() + " alt=" + m.getRthAltitude()));
        d.getDroneOsdModule().listenAircraftHomeGeographicalInfo(m -> log("INFO", sn, "Home点 received"));
        d.getDroneOsdModule().listenAircraftCurrentRthMode(m -> log("INFO", sn, "当前返航模式=" + m.getCurrentRthMode()));

        // -- Payload OSD --
        d.getDroneOsdModule().listenAircraftPayloadsList(m -> log("INFO", sn, "负载列表 received"));
        d.getDroneOsdModule().listenAircraftPayloadsCamerasList(m -> log("INFO", sn, "相机列表 received"));
        d.getDroneOsdModule().listenAircraftPayloadsGimbalAttitude(m -> log("INFO", sn, "云台姿态 received"));
        d.getDroneOsdModule().listenAircraftPayloadsCameraLiveviewWorldRegion(m -> log("INFO", sn, "相机视野 received"));
        d.getDroneOsdModule().listenAircraftPayloadsOsdInfo(m -> log("INFO", sn, "Payload OSD received"));
        d.getDeviceHMSModule().listenDeviceHMS(m -> log("INFO", sn, "HMS received"));

        // -- 控制权 --
        d.getDroneOsdModule().listenControlRightsCloudControlStatus(m -> log("INFO", sn,
            "云端控制权 controls=" + m.getControls()));
        d.getDroneOsdModule().listenControlRightsDeviceControlStatus(m -> log("INFO", sn, "设备控制权 received"));

        // -- 设备拓扑（Workspace 级）--
        FlightHubSDK.getWorkspaceInstance().getDeviceManageModule()
            .listenDeviceTopoChanged(m -> log("INFO", sn, "拓扑变更 received"));

        // -- 飞行任务 --
        d.getDroneOsdModule().listenFlightTasksException(m -> log("INFO", sn,
            "任务异常 exceptions=" + m.getExceptions()));
        d.getDroneOsdModule().listenFlightTasksMediaFileDisplayAvailable(m -> log("INFO", sn, "媒体展示 received"));

        log("INFO", "setupDroneListeners", "drone 全量监听注册完成: " + sn);
    }

    /** 机库全量 topic 监听（对应 BusinessManagerImpl.h 中 dock 相关接口） */
    private static void setupDockListeners(String sn, Dock d, Drone aircraft) {
        // -- 基础状态 --
        d.getDeviceStateModule().listenDockOnlineStatus(m -> log("INFO", sn, "机库在线=" + m.isOnline()));
        d.getDockOsdModule().listenDockLocation(m -> log("INFO", sn,
            String.format("机库位置 lat=%.2f lon=%.2f", m.getLatitude(), m.getLongitude())));
        d.getDockOsdModule().listenDroneInDock(m -> log("INFO", sn, "机库内有无人机=" + m.getDroneInDock()));
        d.getDockOsdModule().listenDockModeCode(m -> log("INFO", sn, "模式码=" + m.getModeCode()));
        d.getDockOsdModule().listenFlightTaskStepCode(m -> log("INFO", sn,
            "飞行任务步骤码=" + m.getFlighttaskStepCode()
                + " (0准备/1飞行/2恢复/3自定义飞行区/4地形障碍物/5空闲/256未知)"));
        d.getDockOsdModule().listenDockNetworkState(m -> log("INFO", sn,
            "网络 type=" + m.getType() + " quality=" + m.getQuality() + " rate=" + m.getRate()));

        // -- 环控 OSD --
        d.getDockControlModule().listenDockAirConditionerStatusInfo(m -> log("INFO", sn, "空调状态 received"));
        d.getDockControlModule().listenDockSupplementLightState(m -> log("INFO", sn,
            "补光灯=" + m.getSupplementLightState()));
        d.getDockOsdModule().listenDockCoverState(m -> log("INFO", sn, "舱盖=" + m.getCoverState()));
        d.getDockOsdModule().listenDockMeteorologicalInfo(m -> log("INFO", sn,
            String.format("气象 temp=%.1f rain=%d wind=%.1f",
                m.getTemperature(), m.getRainfall(), m.getWindSpeed())));

        // -- 电源 --
        d.getDockOsdModule().listenDockPowerState(m -> log("INFO", sn,
            "电源 supply=" + m.getElectricSupplyVoltage() + "mV"));
        d.getDockOsdModule().listenDockBackupBatteryInfo(m -> log("INFO", sn,
            "备用电池 switch=" + m.getSwitchState() + " volt=" + m.getVoltage() + "mV"));
        d.getDockOsdModule().listenDockDroneChargeState(m -> log("INFO", sn,
            "充电 status=" + m.getState() + " capacity=" + m.getCapacityPercent() + "%"));
        d.getDockOsdModule().listenDockDroneBatteryMaintenanceInfo(m -> log("INFO", sn, "电池维护 received"));
        d.getDockOsdModule().listenDockBatteryStoreMode(m -> log("INFO", sn, "存储模式=" + m.getBatteryStoreMode()));

        // -- 存储/位置 --
        d.getDockOsdModule().listenDockStorageState(m -> log("INFO", sn,
            "存储 total=" + m.getTotal() + " used=" + m.getUsed()));
        d.getDockOsdModule().listenDockPositionState(m -> log("INFO", sn,
            "定位 fixed=" + m.getIsFixed() + " quality=" + m.getQuality()));
        d.getDockOsdModule().listenDockAlternateLandPoint(m -> log("INFO", sn, "备降点 received"));
        d.getDockOsdModule().listenDockRelativeAlternateLandPoint(m -> log("INFO", sn, "相对备降点 received"));
        d.getDockOsdModule().listenDockSelfConvergeCoordinate(m -> log("INFO", sn, "自汇聚坐标 received"));

        // -- 机库运行信息 --
        d.getDockOsdModule().listenDockFirstPowerOnTime(m -> log("INFO", sn, "首次上电时间=" + m.getFirstPowerOn()));
        d.getDockOsdModule().listenDockAccumulatedRunningTime(m -> log("INFO", sn, "累计运行=" + m.getAccTime() + "s"));
        d.getDockOsdModule().listenDockActivationTime(m -> log("INFO", sn, "激活时间=" + m.getActivationTime()));
        d.getDockOsdModule().listenDockFirmwareInfo(m -> log("INFO", sn, "固件 received"));
        d.getDockOsdModule().listenDockMaintainStatusInfo(m -> log("INFO", sn, "机库维护状态 received"));
        d.getDockOsdModule().listenDockAlarmState(m -> log("INFO", sn, "告警 received"));
        d.getDockOsdModule().listenDockEmergencyStopState(m -> log("INFO", sn, "急停=" + m.getEmergencyStopState()));
        d.getDockOsdModule().listenDockSilentMode(m -> log("INFO", sn, "静默模式=" + m.getSilentMode()));
        d.getDockOsdModule().listenDockPutterState(m -> log("INFO", sn, "推杆=" + m.getPutterState()));
        d.getDockOsdModule().listenDockDrcState(m -> log("INFO", sn, "DRC state=" + m.getDrcState()));
        d.getDockOsdModule().listenDockUserExperienceImprovement(m -> log("INFO", sn, "用户体验改进 received"));
        d.getDockOsdModule().listenDockJobNumber(m -> log("INFO", sn, "任务编号=" + m.getJobNumber()));

        // -- 链路 --
        d.getDockOsdModule().listenDockWirelessLink(m -> log("INFO", sn, "无线链路 received"));
        d.getDockOsdModule().listenDockSubDevice(m -> log("INFO", sn, "子设备列表 received"));

        // -- 媒体 --
        d.getDockOsdModule().listenDockMediaFileDetail(m -> log("INFO", sn, "媒体文件详情 received"));

        // -- 飞行任务 --
        d.getDeviceHMSModule().listenDeviceHMS(m -> log("INFO", sn, "Dock HMS received"));

        // -- ATC 任务事件（已迁入 Drone module）--
        if (aircraft != null) {
            FlyToPointModule flyToModule = aircraft.getFlyToPointModule();
            if (flyToModule != null) {
                flyToModule.listenFlyToTaskEvent(event -> log("INFO", sn, "指点飞行任务事件: event_id=" + event.getEventId() + ", action=" + event.getAction() + ", uuid=" + event.getUuid()));
            }
            FlightTaskControlModule flightTaskControlModule = aircraft.getFlightTaskControlModule();
            if (flightTaskControlModule != null) {
                flightTaskControlModule.listenWaylineTaskEvent(event -> log("INFO", sn, "航线任务事件: event_id=" + event.getEventId() + ", action=" + event.getAction() + ", uuid=" + event.getUuid() + ", status=" + (event.getTaskData() != null ? event.getTaskData().getStatus() : "N/A")));
            }
            PoiModule poiModule = aircraft.getPoiModule();
            if (poiModule != null) {
                poiModule.listenPoiTaskEvent(event -> log("INFO", sn, "POI任务事件: event_id=" + event.getEventId() + ", action=" + event.getAction() + ", uuid=" + event.getUuid()));
            }
        }

        log("INFO", "setupDockListeners", "dock 全量监听注册完成: " + sn);
    }

    private static void autoDemo() {
        log("INFO", "autoDemo", "开始自动演示模式...");
        
        try {
            // 演示1: 监听飞机OSD（如果有无人机）
            if (hasSubDevice(selectedDevice)) {
                log("INFO", "autoDemo", "演示功能: 监听飞机OSD");
                handleDroneOsd();
            }
            
            // 演示2: 拉取码流
            log("INFO", "autoDemo", "演示功能: 拉取码流");
            handleLiveStream();
            
            log("INFO", "autoDemo", "自动演示完成");
            
        } catch (Exception e) {
            log("ERROR", "autoDemo", "自动演示过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 设置程序退出
        running.set(false);
    }
    
    /**
     * 显示主菜单
     */
    private static void showMainMenu() {
        printSeparator("主菜单");
        System.out.println("[1] 拉取码流");
        System.out.println("[2] 拍照");
        System.out.println("[3] 退出");
        System.out.println("[4] 监听飞机 OSD");
        System.out.println("[5] Flyto");
        System.out.println("[6] Flyto Stop");
        System.out.println("[7] 监听相机状态");
        System.out.println("[8] 获取飞行控制权");
        System.out.println("[9] 释放飞行控制权");
        System.out.println("[10] 监听飞行控制权状态");
        System.out.println("[11] 获取负载控制权");
        System.out.println("[12] 释放负载控制权");
        System.out.println("[13] 监听负载控制权状态");
        System.out.println("[14] 停止推流");
        System.out.println("[15] 开始录像");
        System.out.println("[16] 停止录像");
        System.out.println("[17] 监听飞机在线状态、链路状态");
        System.out.println("[18] 框选变焦");
        System.out.println("[19] LookAtTarget");
        System.out.println("[20] 开始全景拍照");
        System.out.println("[21] 停止全景拍照");
        System.out.println("[22] 切换相机模式");
        System.out.println("[23] 切换镜头");
        System.out.println("[24] 航线 - 暂停");
        System.out.println("[25] 航线 - 恢复");
        System.out.println("[27] 飞行 - 急停");
        System.out.println("[28] 飞行 - WASD");
        System.out.println("[31] 检查功能状态");
        //
        System.out.println("[32] 飞行 - 返航");
        System.out.println("[33] 飞行 - 取消返航");
        System.out.println("[34] 飞行 - 一键起飞");
        System.out.println("[35] 飞行 - POI - 进度监听");
        System.out.println("[36] 飞行 - POI - 进入POI模式");
        System.out.println("[37] 飞行 - POI - 停止POI模式");
        System.out.println("[38] 飞行 - POI - 更新POI速度");
        System.out.println("[39] 飞行 - POI - 查询POI任务");

        System.out.println("[40] OSD - Drone");
        System.out.println("[41] OSD - Dock");
        System.out.println("[42] OSD - Workspace");
        System.out.println("[43] EA340 消息推送补全 (WS listen，可多选/全选)");

        // 50-67 for PSDK
        System.out.println("[50] PSDK - 状态监听");
        System.out.println("[51] PSDK - 喊话器 - 重新播放");
        System.out.println("[52] PSDK - 喊话器 - 停止播放");
        System.out.println("[53] PSDK - 喊话器 - 音量设置");
        System.out.println("[54] PSDK - 喊话器 - TTS设置");
        System.out.println("[55] PSDK - 喊话器 - 播放模式设置 - 循环播放");
        System.out.println("[56] PSDK - 喊话器 - TTS播放");
        System.out.println("[57] PSDK - 喊话器 - 文件播放");
        System.out.println("[58] PSDK - 控件 - 发送文本框内容");
        System.out.println("[59] PSDK - 控件 - 设置 widget 值");
        System.out.println("[61] PSDK - 喊话器 - 创建语音/文本资源");
        System.out.println("[62] PSDK - 喊话器 - 修改语音/文本资源");
        System.out.println("[63] PSDK - 喊话器 - 删除语音/文本资源");
        System.out.println("[64] PSDK - 喊话器 - 查询列表语音/文本资源");
        System.out.println("[65] PSDK - 喊话器 - 获取临时上传凭证");
        System.out.println("[66] PSDK - 获取PSDK控件资源包索引");
        System.out.println("[67] PSDK - 喊话器 - 模拟上传音频文件");

        // 80-84 for 红外增益模式
        System.out.println("[80] 监听直播镜头信息");
        System.out.println("[82] 监听红外增益模式");
        System.out.println("[84] 设置红外增益模式");

        // 85-88 for 飞行任务异常和设备变更
        System.out.println("[85] 监听飞行任务异常");
        System.out.println("[86] 监听工作空间设备变更");
        System.out.println("[87] 监听设备拓扑变更（Drone）");
        System.out.println("[88] 监听设备拓扑变更（Dock）");
        System.out.println("[89] 线程堵塞测试（getDeviceList 回调内 sleep，验证 BlockingDetector）");

        // 90-94 for 自定义SEI测试
        System.out.println("[90] 自定义 SEI 测试");
        System.out.println("[91] 自定义 SEI 编解码测试（无需设备）");
        System.out.println("[92] 无人机推流测试（基于状态自动控制）");
        System.out.println("[93] 拉流模块测试");
        System.out.println("[94] 飞机码流 + 写入自定义SEI + RTMP 推流");
        
        System.out.println("[95] 负载控制 - 变焦");

        // 100-107 for 机场控制
        System.out.println("[100] 机场控制 - 补光灯 - 开启");
        System.out.println("[101] 机场控制 - 补光灯 - 关闭");
        System.out.println("[102] 机场控制 - 充电 - 开启");
        System.out.println("[103] 机场控制 - 充电 - 关闭");
        System.out.println("[104] 机场控制 - 舱盖 - 开启");
        System.out.println("[105] 机场控制 - 舱盖 - 关闭");
        System.out.println("[106] 机场控制 - 舱盖 - 强制关闭");
        System.out.println("[107] 机场控制 - 空调模式");
        System.out.println("[108] 设备调试");

        // 121-124 for 组织/日志/固件/限飞测试
        System.out.println("[121] 组织测试");
        System.out.println("[122] 日志测试");
        System.out.println("[123] 固件管理测试");
        System.out.println("[124] 限飞测试");
        System.out.println("[125] 航线管理测试");
        System.out.println("[126] 机载算法管理测试（PAAS 纯血）");
        System.out.println("[127] HMS 测试");
        System.out.println("[128] AI 告警管理测试（PAAS 纯血）");
        System.out.println("[129] 直播转发（live-sync）");
        System.out.println("[223] 空中下发航线测试");

        System.out.println("[108] DRC 链路 - 连接/断开循环测试");
        System.out.println("[113] DRC 链路 - 手动连接");
        System.out.println("[114] DRC 链路 - 手动断开");
        System.out.println("[115] DRC 操控 - 云台重置 (drcResetGimbal)");

        System.out.println("[200] 压力测试模式（多线程并发测试，持续运行）");
        
        System.out.println("[201] 循环拉流（飞机+机场，30s拉流/10s等待，循环往复）");
        System.out.println("[202] 停止循环拉流");
        System.out.println("[203] 拉流压测（多路拉流测试）");
        System.out.println("[204] 循环拉流压测（多路WHEP循环拉流，可配置时间）");
        System.out.println("[205] 停止循环拉流压测");

        System.out.println("[240] 机场控制 - 切换舱内/舱外摄像头");
        System.out.println("[241] 直播录制 - 开始录制");
        System.out.println("[242] 直播录制 - 停止录制");
        System.out.println("[243] 直播录制 - 查询设备录制记录");
        System.out.println("[244] 直播录制 - 订阅录制状态");
        System.out.println("[245] 直播录制 - 取消订阅录制状态");
        System.out.println("[246] 获取在线观众数量");
        System.out.println("[247] 设置直播画质（drone）");
        System.out.println("[248] 设置直播画质（dock）");
        System.out.println("[250] 航线任务 - 获取任务列表");
        System.out.println("[251] 航线任务 - 创建飞行计划");
        System.out.println("[252] 航线任务 - 获取任务详情");
        System.out.println("[253] 航线任务 - 编辑飞行计划");
        System.out.println("[254] 航线任务 - 删除飞行计划");
        System.out.println("[255] 航线任务 - 漫游（开始/基站白名单/拓扑/监听）");
        System.out.println("[256] 航线任务 - 更新任务状态");
        System.out.println("[257] 航线任务 - 批量更新任务状态");
        System.out.println("[258] 航线任务 - 获取设备推荐");
        System.out.println("[259] 航线任务 - 获取设备TCA");
        System.out.println("[260] 航线任务 - 更新设备TCA");
        System.out.println("[261] 负载控制 - 设置拍照存储设置");
        System.out.println("[262] 负载控制 - 设置录像存储设置");
        System.out.println("[263] 负载控制 - 设置夜景模式");
        System.out.println("[264] 负载控制 - 监听夜景模式状态");
        System.out.println("[265] 负载控制 - 取消监听夜景模式状态");

        System.out.println("[280] PSDK 探照灯 - 工作模式设置（0=关闭 1=开启 2=爆闪）");
        System.out.println("[281] PSDK 探照灯 - 亮度设置（0~100）");
        System.out.println("[282] PSDK 探照灯 - 云台自动校准");
        System.out.println("[283] PSDK 探照灯 - 云台微调（position: 0=左 1=右, value: -100~100）");
        System.out.println("[284] PSDK 探照灯 - 广视野照明开关（0=关 1=开）");
        System.out.println("[285] PSDK 探照灯 - 云台联动开关（0=关 1=开）");
        System.out.println("[286] PSDK 探照灯 - 灯光类型（0=近光 1=远光 2=全开）");

        System.out.println("[270] SDK信息 - 获取版本号和构建信息");

        System.out.println("[291] 直播测试 - 循环拉流/断流测试");
        System.out.println("[292] 一键起飞任务 - 获取任务列表");
        System.out.println("[293] 飞行记录 - 查询飞行任务轨迹");

        // 300-339 for 媒体服务模块;
        System.out.println("[300] 媒体服务模块 - 监听媒体服务状态 [设备媒体回传通知][设备媒体后处理完成通知]");
        System.out.println("[301] 媒体服务模块 - 下载地址/获取列表/删除 [获取媒体文件下载地址][获取媒体文件列表][批量删除媒体文件]");
        System.out.println("[302] 媒体服务模块 - 获取媒体上传 STS 凭证");
        System.out.println("[303] 媒体服务模块 - 媒体上传完成回调");
        System.out.println("[304] 媒体服务模块 - 图片备注上传回调");
        System.out.println("[305] 媒体服务模块 - 图片备注轮询文件 URL");
        System.out.println("[306] 航线管理模块 - 获取航线编辑器 STS 凭证");
        System.out.println("[307] 媒体服务模块 - 删除图片备注文件（media_file_id）");

        // 310-317 for 爆闪灯;
        System.out.println("[310] 爆闪灯 - 监听 flash_led 状态");
        System.out.println("[311] 爆闪灯 - 开启(匀速闪烁预设)");
        System.out.println("[312] 爆闪灯 - 关闭");
        System.out.println("[313] 爆闪灯 - 查询 flash-configs 列表");
        System.out.println("[314] 爆闪灯 - 创建 flash-config");
        System.out.println("[315] 爆闪灯 - 更新 flash-config");
        System.out.println("[316] 爆闪灯 - 删除 flash-config");
        System.out.println("[317] 爆闪灯 - 应用模板(convertToDevicePatterns + setFlashLed)");
        System.out.println("[318] EA610 基站三方外设直播 - 列路→开一路→切另一路");

        // 400-429 for Terra Build 重建模块;
        System.out.println("[400] Terra Build - 获取模型文件列表");
        System.out.println("[401] Terra Build - 获取模型文件签名 URL");
        System.out.println("[402] Terra Build - 获取重建设置");
        System.out.println("[403] Terra Build - 获取质量报告");
        System.out.println("[404] Terra Build - 创建重建任务（2D 可见光）");
        System.out.println("[405] Terra Build - 创建重建任务（3D 全开）");
        System.out.println("[406] Terra Build - 停止重建任务");
        System.out.println("[407] Terra Build - 重试重建任务");
        System.out.println("[409] Terra Build - 获取重建图像列表");
        System.out.println("[410] Terra Build - 删除模型文件");
        System.out.println("[413] Terra Build - 批量删除模型文件");
        System.out.println("[411] Terra Build - 创建全景拼接任务");
        System.out.println("[412] Terra Build - 监听重建状态变更推送（输入0退出）");
        System.out.println("[414] Terra Build - 模型上传回调");
        System.out.println("[415] Terra Build - 模型重命名");
        System.out.println("[416] Terra Build - 加载模型到地图");
        System.out.println("[4160] Terra Build - 取消加载模型到地图");
        System.out.println("[417] Terra Build - 监听模型解压进度推送（输入0退出）");
        System.out.println("[418] Terra Build - 监听模型状态变更推送（输入0退出）");
        System.out.println("[419]  Terra Build - 监听模型展示地图推送 topic_model_file_show_on_map（输入0退出）");
        System.out.println("[4190] Terra Build - 监听模型隐藏地图推送 topic_media_file_hide_on_map（输入0退出）");
        System.out.println("[4191] Terra Build - 监听模型显示地图推送 topic_media_file_show_on_map（输入0退出）");
        System.out.println("[491] Terra Build - 获取模型文件详情");
        System.out.println("[492] Terra Build - 设置LAS坐标系并触发格式转换");

        // 340-379 for 安全服务模块;
        System.out.println("[340] 安全服务模块 - [获取信号地图]");
        System.out.println("[341] 安全服务模块 - [创建自定义飞行区域]");
        System.out.println("[342] 安全服务模块 - [获取自定义飞行区域]");
        System.out.println("[343] 安全服务模块 - [更新自定义飞行区域]");
        System.out.println("[344] 安全服务模块 - [删除自定义飞行区域]");
        System.out.println("[345] 安全服务模块 - [同步自定义飞行区域]");
        System.out.println("[346] 安全服务模块 - [获取自定义飞行区域同步状态]");
        System.out.println("[350] 安全服务模块 - [地形障碍物] 查询离线地图模型信息");
        System.out.println("[351] 安全服务模块 - [地形障碍物] 创建/更新离线地图模型");
        System.out.println("[352] 安全服务模块 - [地形障碍物] 取消离线地图生成");
        System.out.println("[353] 安全服务模块 - [地形障碍物] 修改离线地图开关");
        System.out.println("[354] 安全服务模块 - [地形障碍物] 同步离线地图至设备");
        System.out.println("[355] 安全服务模块 - [地形障碍物] 获取设备离线地图同步状态");

        // 380-399 for ATC 空管服务--飞行任务记录模块;
        System.out.println("[380] 飞行任务记录模块 - [导出飞行记录]");
        System.out.println("[381] 飞行任务记录模块 - [查询导出进度]");

        // 390-399 for 飞行安全配置;
        System.out.println("[390] 飞行安全配置 - [设置智能规划模式]");
        System.out.println("[391] 飞行安全配置 - [设置失控动作]");
        System.out.println("[392] 飞行安全配置 - [设置飞行作业高度]");
        System.out.println("[393] 飞行安全配置 - [设置返航高度]");
        System.out.println("[394] 飞行安全配置 - [设置空中回传开关]");
        System.out.println("[395] 指点飞行 - [更新目标点]");

        // 420-429 for 报修管理模块;
        System.out.println("[420] 报修管理 - 获取设备报修配置");
        System.out.println("[421] 报修管理 - 创建报修订单");
        System.out.println("[422] 报修管理 - 查询报修订单详情");
        System.out.println("[423] 报修管理 - 查询报修订单列表");

        // 430-439 for 定损管理模块;
        System.out.println("[430] 定损管理 - 下发定损指令");
        System.out.println("[431] 定损管理 - 定损操作(取消/删除)");
        System.out.println("[432] 定损管理 - 查询单条定损报告详情");
        System.out.println("[433] 定损管理 - 查询定损报告列表");

        // 440-449 for AI Box 管理模块;
        System.out.println("[440] AI Box - 获取AI Box设备信息");
        System.out.println("[441] AI Box - 获取文件下载地址");
        System.out.println("[442] AI Box - 获取应用列表（异步下发）");
        System.out.println("[443] AI Box - 查询获取应用列表进度");
        System.out.println("[444] AI Box - 导出文件（异步下发）");
        System.out.println("[445] AI Box - 查询导出文件进度");
        System.out.println("[446] AI Box - 上传文件到AI Box");
        System.out.println("[447] AI Box - 安装应用（异步下发）");
        System.out.println("[448] AI Box - 查询安装应用进度");
        System.out.println("[449] AI Box - 操作应用（启动/停止/卸载）");

        System.out.println("[450] 任务事件监听 - [指令/指点飞行/航线]");

        System.out.println("[460] HYAPP-177995 复现 - 监听直播范围+离线即cancel(关机触发 SWIG director UAF)");

        // 500-520 for DRC 高级功能测试
        System.out.println("[500] DRC高级 - 相机光圈设置");
        System.out.println("[501] DRC高级 - 相机ISO设置");
        System.out.println("[502] DRC高级 - 相机快门设置");
        System.out.println("[503] DRC高级 - 相机曝光模式设置");
        System.out.println("[504] DRC高级 - 相机对焦模式设置");
        System.out.println("[505] DRC高级 - 相机点对焦");
        System.out.println("[506] DRC高级 - 相机畸变矫正");
        System.out.println("[507] DRC高级 - 相机分屏显示");
        System.out.println("[508] DRC高级 - 相机除雾");
        System.out.println("[509] DRC高级 - 红外调色板设置");
        System.out.println("[510] DRC高级 - 红外测温模式设置");
        System.out.println("[511] DRC高级 - 红外等温线开关");
        System.out.println("[512] DRC高级 - AI跟踪开启/关闭");
        System.out.println("[513] DRC高级 - AI跟踪选择目标");
        System.out.println("[514] DRC高级 - AI跟踪停止");
        System.out.println("[521] 机载AI 2.0 - 过滤设置 (index+light_type+filters)");
        System.out.println("[522] 机载AI 2.0 - 置信度设置 (index+light_type+score)");
        System.out.println("[523] 机载AI 2.0 - 置信度模式 (index+light_type+score_mode)");
        System.out.println("[524] 机载AI 2.0 - 置信度重置 (index+light_type)");
        System.out.println("[525] 机载AI 2.0 - 监听 AiDataInfo (含 selected_ai_models_status)");
        System.out.println("[526] 机载AI 2.0 - ModelsConfig 开/关识别");
        System.out.println("[515] DRC高级 - 悬停设置");
        System.out.println("[516] DRC高级 - 夜航灯设置");
        System.out.println("[517] DRC高级 - 隐身模式设置");
        System.out.println("[518] DRC高级 - 监听OSD/电池/延时推送");
        System.out.println("[519] DRC高级 - 停止监听OSD/电池/延时推送");
        System.out.println("[520] DRC高级 - RTC质量测试");
        System.out.println("── Element 模块 ─────────────────────────────────────────");
        System.out.println("[601] Element - 创建要素");
        System.out.println("[602] Element - 更新要素（重命名/显示）");
        System.out.println("[603] Element - 查询元素层级列表");
        System.out.println("[604] Element - 设置要素显示/隐藏");
        System.out.println("[605] Element - 删除要素");
        System.out.println("[606] Element - 监听要素创建推送（输入0退出）");
        System.out.println("[607] Element - 监听要素更新推送（输入0退出）");
        System.out.println("[608] Element - 监听要素删除推送（输入0退出）");
        System.out.println("[294] Module缓存测试 - getLiveStream/getLiveRecord 多实例验证");

        System.out.print("请选择功能 (1-999): ");
    }
    
    /**
     * 获取用户选择
     */
    private static int getUserChoice(int min, int max) {
        // 在非交互式环境中直接返回默认选择
        if (!isInteractiveMode) {
            log("INFO", "getUserChoice", "非交互式环境，使用默认选择: " + min);
            return min;
        }
        
        while (true) {
            try {
                // 检查是否有可用输入
                if (!scanner.hasNextLine()) {
                    log("ERROR", "getUserChoice", "无法读取用户输入");
                    // 切换到非交互式模式
                    isInteractiveMode = false;
                    log("INFO", "getUserChoice", "切换到非交互式模式，使用默认选择: " + min);
                    return min;
                }
                
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }
                
                int choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.print("请输入 " + min + " 到 " + max + " 之间的数字: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("请输入有效的数字: ");
            } catch (Exception e) {
                log("ERROR", "getUserChoice", "读取用户输入时发生异常: " + e.getMessage());
                // 切换到非交互式模式
                isInteractiveMode = false;
                log("INFO", "getUserChoice", "切换到非交互式模式，使用默认选择: " + min);
                return min;
            }
        }
    }
    
    /**
     * 生成带时间戳的文件名
     */
    private static String generateTimestampFilename(String prefix, String extension) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new java.util.Date());
        return prefix + "_" + timestamp + "." + extension;
    }
    
    /**
     * 等待用户按键
     */
    private static void waitForKeyPress() {
        if (!isInteractiveMode) {
            log("INFO", "waitForKeyPress", "非交互式环境，等待3秒后继续...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return;
        }
        
        System.out.println("按Enter键继续...");
        try {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            } else {
                log("INFO", "waitForKeyPress", "切换到非交互式模式");
                isInteractiveMode = false;
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            log("WARN", "waitForKeyPress", "等待用户输入时发生异常: " + e.getMessage());
            isInteractiveMode = false;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * 打印分隔符
     */
    private static void printSeparator(String title) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }
    
    // ==================== 设备辅助方法 ====================

    private static String formatDeviceLabel(Device device) {
        if (device == null) {
            return "unknown";
        }
        String deviceClass = device.getDeviceClass() != null ? device.getDeviceClass() : "";
        String name = device.getNickname() != null && !device.getNickname().isEmpty()
                ? device.getNickname()
                : (device.getDeviceName() != null && !device.getDeviceName().isEmpty()
                        ? device.getDeviceName()
                        : device.getDeviceSn());
        return deviceClass + " " + name;
    }

    /** 按 deviceClass 给出中文角色，避免把拓扑根一律当成机场。 */
    private static String describeDeviceRole(Device device, boolean isRoot) {
        if (device == null) {
            return isRoot ? "拓扑根" : "子设备";
        }
        String cls = device.getDeviceClass();
        if ("airport".equals(cls) || "dock".equals(cls)) {
            return isRoot ? "机场(拓扑根)" : "机场";
        }
        if ("base_station".equals(cls)) {
            return isRoot ? "地面站(拓扑根/网关)" : "地面站";
        }
        if ("drone".equals(cls)) {
            return "飞机";
        }
        if ("rc".equals(cls)) {
            return isRoot ? "遥控器(拓扑根/网关)" : "遥控器";
        }
        return isRoot ? "拓扑根" : "子设备";
    }

    private static String formatTopoDeviceLine(Device device, boolean isRoot) {
        if (device == null) {
            return "unknown";
        }
        return describeDeviceRole(device, isRoot) + " " + formatDeviceLabel(device)
                + " (SN: " + device.getDeviceSn() + ")";
    }

    private static Device findDeviceByClass(DeviceTopo deviceTopo, String... classes) {
        if (deviceTopo == null) {
            return null;
        }
        java.util.Set<String> classSet = new java.util.HashSet<>();
        for (String cls : classes) {
            classSet.add(cls);
        }

        Device mainDevice = deviceTopo.getDevice();
        if (mainDevice != null && classSet.contains(mainDevice.getDeviceClass())) {
            return mainDevice;
        }
        java.util.List<Device> subDevices = deviceTopo.getSubDevices();
        if (subDevices != null) {
            for (Device subDevice : subDevices) {
                if (subDevice != null && classSet.contains(subDevice.getDeviceClass())) {
                    return subDevice;
                }
            }
        }
        return null;
    }

    private static String getDockDeviceSn(DeviceTopo deviceTopo) {
        Device device = findDeviceByClass(deviceTopo, "dock", "airport");
        return device != null ? device.getDeviceSn() : null;
    }

    private static String getBaseStationDeviceSn(DeviceTopo deviceTopo) {
        Device device = findDeviceByClass(deviceTopo, "base_station");
        return device != null ? device.getDeviceSn() : null;
    }

    private static BaseStation getOrCreateBaseStation(String sn) {
        if (sn == null || sn.isEmpty()) {
            return null;
        }
        BaseStation cached = baseStationCache.get(sn);
        if (cached != null) {
            return cached;
        }
        BaseStation created = FlightHubSDK.getBaseStationInstance(sn);
        if (created != null) {
            baseStationCache.put(sn, created);
        }
        return created;
    }

    private static Dock getOrCreateDock(String sn) {
        if (sn == null || sn.isEmpty()) {
            return null;
        }
        Dock cached = dockCache.get(sn);
        if (cached != null) {
            return cached;
        }
        Dock created = FlightHubSDK.getDockInstance(sn);
        if (created != null) {
            dockCache.put(sn, created);
        }
        return created;
    }

    private static Drone getOrCreateDrone(String sn) {
        if (sn == null || sn.isEmpty()) {
            return null;
        }
        Drone cached = droneCache.get(sn);
        if (cached != null) {
            return cached;
        }
        Drone created = FlightHubSDK.getDroneInstance(sn);
        if (created != null) {
            droneCache.put(sn, created);
        }
        return created;
    }
    
    /**
     * 检查设备是否有子设备（无人机）
     */
    private static boolean hasSubDevice(DeviceTopo deviceTopo) {
        return findDeviceByClass(deviceTopo, "drone") != null;
    }
    
    /**
     * 获取子设备（无人机）的SN
     */
    private static String getSubDeviceSn(DeviceTopo deviceTopo) {
        Device droneDevice = findDeviceByClass(deviceTopo, "drone");
        return droneDevice != null ? droneDevice.getDeviceSn() : null;
    }

    /**
     * 收集拓扑中主设备 + 全部子设备的 SN。
     * 主设备在前，子设备按拓扑顺序追加；跳过 null/空 SN 并去重。
     */
    private static List<String> collectDeviceSns(DeviceTopo deviceTopo) {
        List<String> sns = new ArrayList<>();
        if (deviceTopo == null) {
            return sns;
        }

        Device mainDevice = deviceTopo.getDevice();
        if (mainDevice != null) {
            String mainSn = mainDevice.getDeviceSn();
            if (mainSn != null && !mainSn.isEmpty()) {
                sns.add(mainSn);
            }
        }

        java.util.List<Device> subDevices = deviceTopo.getSubDevices();
        if (subDevices != null) {
            for (Device subDevice : subDevices) {
                if (subDevice == null) {
                    continue;
                }
                String subSn = subDevice.getDeviceSn();
                if (subSn != null && !subSn.isEmpty() && !sns.contains(subSn)) {
                    sns.add(subSn);
                }
            }
        }

        return sns;
    }

    /**
     * 逐个查询 SN 列表对应的设备详情（DeviceManageModule.getDeviceDetailInfo）并打印回调数据。
     * 单个 SN 最多等待 10 秒；失败或超时只记录日志，不阻断后续 SN 的查询。
     */
    private static void queryAndLogDeviceDetailInfos(List<String> deviceSns) {
        if (deviceSns == null || deviceSns.isEmpty()) {
            log("WARN", "getDeviceDetailInfo", "SN 列表为空，跳过设备详情查询");
            return;
        }
        if (workspace == null) {
            workspace = FlightHubSDK.getWorkspaceInstance();
        }
        if (workspace == null) {
            log("ERROR", "getDeviceDetailInfo", "获取 Workspace 实例失败，跳过设备详情查询");
            return;
        }

        for (final String deviceSn : deviceSns) {
            log("INFO", "getDeviceDetailInfo", "正在查询设备详情: " + deviceSn);
            final CountDownLatch latch = new CountDownLatch(1);
            workspace.getDeviceManageModule().getDeviceDetailInfo(deviceSn, new FlightHubCallback.GetDeviceDetailInfoCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error, DeviceDetailInfo deviceDetail) {
                    try {
                        if (error == null || !error.isSuccess()) {
                            log("ERROR", "getDeviceDetailInfo", "查询设备详情失败 [" + deviceSn + "], error: " + error);
                            return;
                        }
                        log("INFO", "getDeviceDetailInfo", "查询设备详情成功 [" + deviceSn + "]:");
                        logDeviceDetailInfo("getDeviceDetailInfo", deviceDetail);
                    } finally {
                        latch.countDown();
                    }
                }
            });
            try {
                if (!latch.await(10, TimeUnit.SECONDS)) {
                    log("WARN", "getDeviceDetailInfo", "查询设备详情超时(10s): " + deviceSn);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("WARN", "getDeviceDetailInfo", "等待设备详情回调被中断，终止后续查询");
                return;
            }
        }
    }

    /**
     * 打印 getDeviceDetailInfo 回调返回的设备详情（含子设备）
     */
    private static void logDeviceDetailInfo(String tag, DeviceDetailInfo detail) {
        if (detail == null) {
            log("WARN", tag, "  设备详情为空");
            return;
        }

        log("INFO", tag, "  device_sn: " + detail.getDeviceSn()
                + ", nickname: " + detail.getNickname()
                + ", complete_machine_sn: " + detail.getCompleteMachineSn());
        log("INFO", tag, "  online: " + detail.isOnline()
                + ", device_add_time: " + detail.getDeviceAddTime()
                + ", firmware_version: " + detail.getDeviceFirmwareVersion()
                + ", firmware_status: " + detail.getDeviceFirmwareStatus()
                + ", working_status: " + detail.getDeviceWorkingStatus());
        log("INFO", tag, "  model_info: " + formatModelInfo(detail.getModelInfo()));

        List<SubDeviceInfo> subDevices = detail.getSubDevices();
        if (subDevices == null || subDevices.isEmpty()) {
            log("INFO", tag, "  sub_devices: 无");
            return;
        }
        log("INFO", tag, "  sub_devices(" + subDevices.size() + "):");
        for (SubDeviceInfo subDevice : subDevices) {
            if (subDevice == null) {
                continue;
            }
            log("INFO", tag, "    -> device_sn: " + subDevice.getDeviceSn()
                    + ", online: " + subDevice.isOnline()
                    + ", firmware_version: " + subDevice.getDeviceFirmwareVersion()
                    + ", firmware_status: " + subDevice.getDeviceFirmwareStatus()
                    + ", working_status: " + subDevice.getDeviceWorkingStatus()
                    + ", flysafe_database_version: " + subDevice.getFlysafeDatabaseVersion());
            log("INFO", tag, "       parent_sns: " + subDevice.getParentSns()
                    + ", model_info: " + formatModelInfo(subDevice.getModelInfo()));
        }
    }

    private static String formatModelInfo(ModelInfo modelInfo) {
        if (modelInfo == null) {
            return "null";
        }
        return "{key=" + modelInfo.getKey()
                + ", domain=" + modelInfo.getDomain()
                + ", type=" + modelInfo.getType()
                + ", sub_type=" + modelInfo.getSubType()
                + ", name=" + modelInfo.getName()
                + ", device_class=" + modelInfo.getDeviceClass()
                + ", develop_code=" + modelInfo.getDevelopCode() + "}";
    }

    /**
     * 优先返回机场 SN（dock/airport）；拓扑里没有机场时才退回拓扑根 SN。
     * 不要把它当「一定是机场」：EA340 无机场时这里仍是地面站。
     */
    private static String getMainDeviceSn(DeviceTopo deviceTopo) {
        String dockSn = getDockDeviceSn(deviceTopo);
        if (dockSn != null) {
            return dockSn;
        }
        Device mainDevice = deviceTopo.getDevice();
        return mainDevice != null ? mainDevice.getDeviceSn() : null;
    }
    
    // ==================== 功能处理方法 ====================
    
    /**
     * 处理直播流功能
     */
    private static void handleLiveStream() {
        printSeparator("拉取码流");
        
        log("INFO", "handleLiveStream", "正在监听机库的直播可用范围...");
        
        try {
            // 初始化文件输出
            initStreamOutputs();

            // RTMP 配置
            ConfigManager.DemoConfig cfg = ConfigManager.getConfig();
            final boolean rtmpEnabled = cfg.rtmp != null && cfg.rtmp.enabled &&
                    cfg.rtmp.server_url != null && !cfg.rtmp.server_url.isEmpty() &&
                    cfg.rtmp.stream_key != null && !cfg.rtmp.stream_key.isEmpty();
            final String rtmpUrl = rtmpEnabled ? (cfg.rtmp.server_url.endsWith("/") ?
                    (cfg.rtmp.server_url + cfg.rtmp.stream_key) : (cfg.rtmp.server_url + "/" + cfg.rtmp.stream_key)) : null;
            final int rtmpFps = cfg.rtmp != null ? Math.max(1, cfg.rtmp.frame_rate) : 30;
            
            // 获取拓扑中的机场/机库实例并监听直播范围。gateway 解析由 SDK 内部处理。
            String dockSn = getDockDeviceSn(selectedDevice);
            dock = getOrCreateDock(dockSn);
            if (dock != null) {
                dockLiveAvailableRangeListenId = dock.getLiveDeviceModule().listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                    @Override
                    public void onResult(java.util.List<LiveInfo> ranges) {
                        log("INFO", "handleLiveStream", "机库直播可用范围: " + ranges.size());
                        for (LiveInfo range : ranges) {
                            logLiveInfoDetails("handleLiveStream", range);
                        }
                        if (!ranges.isEmpty()) {
                            log("INFO", "handleLiveStream", "开始拉取机库码流...");
                            dockLiveModule = dock.getLiveStream(ranges.get(0));
                            dockLiveModule.start(new FlightHubCallback.ResultCallbackWithError() {
                                @Override
                                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                    if (error.isSuccess()) {
                                        log("INFO", "handleLiveStream", "机库直播流启动成功");
                                        // 启动机库 RTMP 推流
                                        // if (rtmpEnabled) {
                                        //     try {
                                        //         dockStreamer = new FFmpegPipeStreamer();
                                        //         dockStreamer.start(rtmpUrl, rtmpFps, cfg.rtmp.width, cfg.rtmp.height, cfg.rtmp.bitrate);
                                        //         log("INFO", "handleLiveStream", "机库 RTMP 推流已启动 -> " + rtmpUrl);
                                        //     } catch (Exception e) {
                                        //         log("ERROR", "handleLiveStream", "启动机库 RTMP 推流失败: " + e.getMessage());
                                        //     }
                                        // }
                                    } else {
                                        log("ERROR", "handleLiveStream", "机库直播流启动失败，错误码: " + error);
                                    }
                                }
                            });
                            
                            dockLiveStreamListenId = dockLiveModule.subscribeLiveStream(new FlightHubCallback.LiveStreamCallback() {
                                @Override
                                public void onResult(byte[] data, FrameInfo frameInfo) {
                                    dockFrameCount++;
                                    dockTotalBytes += data.length;
                                    if (dockFrameCount % 30 == 0) {
                                        log("INFO", "handleLiveStream", "收到机库直播流数据 #" + dockFrameCount + 
                                            ": " + data.length + " bytes (总计: " + dockTotalBytes + " bytes)");
                                    }
                                    
                                    writeDockStreamData(data);

                                    // 异步推送到 ffmpeg，避免阻塞回调
                                    // if (rtmpEnabled && dockStreamer != null) {
                                    //     dockStreamer.pushFrame(data);
                                    // }
                                }
                            });
                        }
                    }
                });
            } else {
                log("WARN", "handleLiveStream", "当前拓扑没有可拉机库/机场流的 Dock 对象");
            }
            
            // 如果有子设备（无人机），也监听无人机的直播范围
            if (hasSubDevice(selectedDevice)) {
                log("INFO", "handleLiveStream", "正在监听无人机的直播可用范围...");
                drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
                if (drone != null) {
                    droneLiveAvailableRangeListenId = drone.getLiveDeviceModule().listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                        @Override
                        public void onResult(java.util.List<LiveInfo> ranges) {
                            log("INFO", "handleLiveStream", "无人机直播可用范围: " + ranges.size());
                            if (!ranges.isEmpty()) {
                                log("INFO", "handleLiveStream", "开始拉取无人机码流...");
                                LiveInfo liveInfo = null;
                                for (LiveInfo range : ranges) {
                                    logLiveInfoDetails("handleLiveStream", range);
                                    if (targetPayloadIndex.equals(range.getPayloadIndex())) {
                                        liveInfo = range;
                                        break;
                                    }
                                }
                                if (liveInfo == null && !ranges.isEmpty()) {
                                    liveInfo = ranges.get(0);
                                }
                                
                                if (liveInfo != null) {
                                    droneLiveModule = drone.getLiveStream(liveInfo);
                                    droneLiveModule.start(new FlightHubCallback.ResultCallbackWithError() {
                                        @Override
                                        public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                            if (error.isSuccess()) {
                                                log("INFO", "handleLiveStream", "无人机直播流启动成功");
                                                // 启动飞机 RTMP 推流
                                                if (rtmpEnabled) {
                                                    try {
                                                        droneStreamer = new FFmpegPipeStreamer();
                                                        droneStreamer.start(rtmpUrl, rtmpFps, cfg.rtmp.width, cfg.rtmp.height, cfg.rtmp.bitrate);
                                                        log("INFO", "handleLiveStream", "飞机 RTMP 推流已启动 -> " + rtmpUrl);
                                                        
                                                        // 启动推流状态监控
                                                        startStreamMonitor();
                                                    } catch (Exception e) {
                                                        log("ERROR", "handleLiveStream", "启动飞机 RTMP 推流失败: " + e.getMessage());
                                                    }
                                                }
                                            } else {
                                                log("ERROR", "handleLiveStream", "无人机直播流启动失败，错误码: " + error);
                                            }
                                        }
                                    });
                                    
                                    droneLiveStreamListenId = droneLiveModule.subscribeLiveStream(new FlightHubCallback.LiveStreamCallback() {
                                        @Override
                                        public void onResult(byte[] data, FrameInfo frameInfo) {
                                            if(data == null) {
                                                log("ERROR", "handleLiveStream", "收到无人机直播流数据为空");
                                                return;
                                            }
                                            if(data.length == 0) {
                                                log("ERROR", "handleLiveStream", "收到无人机直播流数据长度为0");
                                                return;
                                            }
                                            droneFrameCount++;
                                            droneTotalBytes += data.length;
                                            if (droneFrameCount % 30 == 0) {
                                                log("INFO", "handleLiveStream", "收到无人机直播流数据 #" + droneFrameCount + 
                                                    ": " + data.length + " bytes (总计: " + droneTotalBytes + " bytes)");
                                            }
                                            
                                            writeDroneStreamData(data);

                                            // 异步推送到 ffmpeg，避免阻塞回调
                                            if (rtmpEnabled && droneStreamer != null) {
                                                droneStreamer.pushFrame(data);
                                            }
                                        }
                                    });
                                    
                                    // 订阅SEI信息
                                    droneLiveModule.subscribeAiSeiInfo(new FlightHubCallback.AiSeiInfoCallback() {
                                        @Override
                                        public void onResult(AiSeiInfo aiSeiInfo) {
                                            log("INFO", "handleLiveStream", "subscribeAiSeiInfo: " + aiSeiInfo.getTargets().size());
                                        }
                                    });
                                    
                                    droneLiveModule.subscribeArSeiInfo(new FlightHubCallback.ArSeiInfoCallback() {
                                        @Override
                                        public void onResult(ArSeiInfo arSeiInfo) {
                                            // log("INFO", "handleLiveStream", "subscribeArSeiInfo: " +
                                            //     arSeiInfo.getCamera_yaw() + ", " + arSeiInfo.getCamera_pitch() + ", " +
                                            //     arSeiInfo.getCamera_roll() + ", " + arSeiInfo.getLatitude() + ", " +
                                            //     arSeiInfo.getLongitude() + ", " + arSeiInfo.getAltitude());
                                        }
                                    });
                                    
                                    droneLiveModule.subscribeFovSeiInfo(new FlightHubCallback.FovSeiInfoCallback() {
                                        @Override
                                        public void onResult(FovSeiInfo fovSeiInfo) {
                                            // log("INFO", "handleLiveStream", "subscribeFovSeiInfo: " +
                                            //     fovSeiInfo.getFov_horizontal() + ", " + fovSeiInfo.getFov_vertical() + ", " +
                                            //     fovSeiInfo.getCur_actual_focal_length());
                                        }
                                    });
                                    
                                    droneLiveModule.subscribeAssistantSeiInfo(new FlightHubCallback.AssistantSeiInfoCallback() {
                                        @Override
                                        public void onResult(AssistantSeiInfo assistantSeiInfo) {
                                            // log("INFO", "handleLiveStream", "subscribeAssistantSeiInfo: " +
                                            //     assistantSeiInfo.getEnable() + ", " + assistantSeiInfo.getIs_auto_direction() + ", " +
                                            //     assistantSeiInfo.getDirection());
                                        }
                                    });
                                    
                                    droneLiveModule.subscribeEisSeiInfo(new FlightHubCallback.EisSeiInfoCallback() {
                                        @Override
                                        public void onResult(EisSeiInfo eisSeiInfo) {
                                            // log("INFO", "handleLiveStream", "subscribeEisSeiInfo: " +
                                            //     "version=" + eisSeiInfo.getVersion() + ", " +
                                            //     "left_top=(" + eisSeiInfo.getLeft_top().getX() + "," + eisSeiInfo.getLeft_top().getY() + "), " +
                                            //     "right_top=(" + eisSeiInfo.getRight_top().getX() + "," + eisSeiInfo.getRight_top().getY() + "), " +
                                            //     "right_bottom=(" + eisSeiInfo.getRight_bottom().getX() + "," + eisSeiInfo.getRight_bottom().getY() + "), " +
                                            //     "left_bottom=(" + eisSeiInfo.getLeft_bottom().getX() + "," + eisSeiInfo.getLeft_bottom().getY() + ")");
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
            
            log("INFO", "handleLiveStream", "直播流已启动，按Enter键停止...");
            waitForKeyPress();
            
        } catch (Exception e) {
            log("ERROR", "handleLiveStream", "直播流处理异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeStreamOutputs();
            // // 停止机库推流
            // if (dockStreamer != null) {
            //     dockStreamer.stop();
            //     dockStreamer = null;
            // }
            // 停止推流监控
            stopStreamMonitor();
            
            // 停止飞机推流
            if (droneStreamer != null) {
                droneStreamer.stop();
                droneStreamer = null;
            }
        }
    }

    private static void handleStopStream() {
        // 先取消订阅，再停止（顺序很重要）
        if (dockLiveStreamListenId != -1 && dockLiveModule != null) {
            try {
                log("INFO", "handleStopStream", "取消订阅机库直播流");
                dockLiveModule.unSubscribeLiveStream(dockLiveStreamListenId);
                log("INFO", "handleStopStream", "机库直播流已取消订阅");
            } catch (Exception e) {
                log("ERROR", "handleStopStream", "取消订阅机库直播流失败: " + e.getMessage());
            }
            dockLiveStreamListenId = -1;
        }
        
        if (dockLiveModule != null) {
            try {
                log("INFO", "handleStopStream", "停止机库推流");
                dockLiveModule.stop();
                log("INFO", "handleStopStream", "机库推流已停止");
            } catch (Exception e) {
                log("ERROR", "handleStopStream", "停止机库推流失败: " + e.getMessage());
            }
            dockLiveModule = null;
        }
        
        if (droneLiveStreamListenId != -1 && droneLiveModule != null) {
            try {
                log("INFO", "handleStopStream", "取消订阅无人机直播流");
                droneLiveModule.unSubscribeLiveStream(droneLiveStreamListenId);
                log("INFO", "handleStopStream", "无人机直播流已取消订阅");
            } catch (Exception e) {
                log("ERROR", "handleStopStream", "取消订阅无人机直播流失败: " + e.getMessage());
            }
            droneLiveStreamListenId = -1;
        }
        
        if (droneLiveModule != null) {
            try {
                log("INFO", "handleStopStream", "停止无人机推流");
                droneLiveModule.stop();
                log("INFO", "handleStopStream", "无人机推流已停止");
            } catch (Exception e) {
                log("ERROR", "handleStopStream", "停止无人机推流失败: " + e.getMessage());
            }
            droneLiveModule = null;
        }
    }

    /**
     * 随机生成 SEI 数据
     * @return 包含4种类型 SEI 数据的列表
     */
    private static java.util.List<com.dji.flighthubsdk.bean.SeiItem> generateRandomSeiData() {
        java.util.List<com.dji.flighthubsdk.bean.SeiItem> customSeiData = new java.util.ArrayList<>();
        Random random = new Random();

        // 1. 添加自定义数据（随机数值）
        java.util.List<Integer> data = new java.util.ArrayList<>();
        data.add(random.nextInt(256));
        data.add(random.nextInt(256));
        data.add(random.nextInt(256));
        customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofCustomData(new com.dji.flighthubsdk.bean.SeiData(data)));

        // 2. 添加目标检测框（随机坐标和尺寸）
        customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofDetectionBox(
                new com.dji.flighthubsdk.bean.SeiDetectionBox(1, 1, 1,
                        random.nextInt(10001), random.nextInt(10001),
                        random.nextInt(10001), random.nextInt(10001),
                        random.nextInt(101))));

        // 3. 添加多边形检测（随机坐标点）
        java.util.List<com.dji.flighthubsdk.bean.SeiPoint2D> points = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            points.add(new com.dji.flighthubsdk.bean.SeiPoint2D(random.nextInt(10001), random.nextInt(10001)));
        }
        customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofDetectionPolygon(
                new com.dji.flighthubsdk.bean.SeiDetectionPolygon(2, 2, 2, random.nextInt(101), points)));

        // 4. 添加文本叠加（固定前缀 + 随机文本）
        customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofTextOverlay(
                new com.dji.flighthubsdk.bean.SeiTextOverlay(3,
                        random.nextInt(10001), random.nextInt(10001),
                        random.nextInt(50) + 10,
                        random.nextInt(256), random.nextInt(256), random.nextInt(256),
                        random.nextInt(256),
                        "DJI_LIVE_" + random.nextInt(10000))));

        return customSeiData;
    }

    /**
     * 处理飞机码流 + 写入自定义SEI + RTMP 推流功能
     */
    private static void handleSeiRtmpStream() {
        printSeparator("飞机码流 + 写入自定义SEI + RTMP 推流");
        
        // 检查设备是否有子设备（无人机）
        if (selectedDevice == null || !hasSubDevice(selectedDevice)) {
            log("ERROR", "handleSeiRtmpStream", "该设备没有无人机，无法进行 SEI + RTMP 推流");
            return;
        }
        
        log("INFO", "handleSeiRtmpStream", "开始飞机码流 + 写入自定义SEI + RTMP 推流...");
        
        try {
            // 获取 CustomSEIModule 实例用于添加自定义 SEI
            com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
            final CustomSEIModule customSeiModule;
            if (stream != null) {
                customSeiModule = stream.getCustomSEIModule();
                if (customSeiModule != null) {
                    log("INFO", "handleSeiRtmpStream", "CustomSEIModule 实例获取成功");
                } else {
                    log("WARN", "handleSeiRtmpStream", "获取 CustomSEIModule 失败，将跳过自定义 SEI 处理");
                }
            } else {
                log("WARN", "handleSeiRtmpStream", "获取 Stream 实例失败，将跳过自定义 SEI 处理");
                customSeiModule = null;
            }
            
            // 初始化动态 SEI 数据（将在每 60 帧更新）
            final java.util.List<com.dji.flighthubsdk.bean.SeiItem> customSeiData = new java.util.ArrayList<>();
            if (customSeiModule != null) {
                // 生成初始 SEI 数据
                customSeiData.addAll(generateRandomSeiData());
                log("INFO", "handleSeiRtmpStream", "初始 SEI 数据生成完成，包含 " + customSeiData.size() + " 个 SEI 项");
            }

            // RTMP 配置
            ConfigManager.DemoConfig cfg = ConfigManager.getConfig();
            final boolean rtmpEnabled = cfg.rtmp != null && cfg.rtmp.enabled &&
                    cfg.rtmp.server_url != null && !cfg.rtmp.server_url.isEmpty() &&
                    cfg.rtmp.stream_key != null && !cfg.rtmp.stream_key.isEmpty();
            final String rtmpUrl = rtmpEnabled ? (cfg.rtmp.server_url.endsWith("/") ?
                    (cfg.rtmp.server_url + cfg.rtmp.stream_key) : (cfg.rtmp.server_url + "/" + cfg.rtmp.stream_key)) : null;
            final int rtmpFps = cfg.rtmp != null ? Math.max(1, cfg.rtmp.frame_rate) : 30;
            
            if (!rtmpEnabled) {
                log("ERROR", "handleSeiRtmpStream", "RTMP 配置未启用或配置不完整，无法进行推流");
                return;
            }
            
            // 获取无人机实例
            drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
            if (drone == null) {
                log("ERROR", "handleSeiRtmpStream", "获取无人机实例失败");
                return;
            }
            
            log("INFO", "handleSeiRtmpStream", "正在监听无人机的直播可用范围...");
            
            // 监听无人机的直播范围
            drone.getLiveDeviceModule().listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                @Override
                public void onResult(java.util.List<LiveInfo> ranges) {
                    log("INFO", "handleSeiRtmpStream", "无人机直播可用范围: " + ranges.size());
                    if (!ranges.isEmpty()) {
                        log("INFO", "handleSeiRtmpStream", "开始拉取无人机码流并添加自定义 SEI...");
                        LiveInfo liveInfo = null;
                        for (LiveInfo range : ranges) {
                            log("INFO", "handleSeiRtmpStream", "payload_index: " + range.getPayloadIndex());
                            if (targetPayloadIndex.equals(range.getPayloadIndex())) {
                                liveInfo = range;
                                break;
                            }
                        }
                        if (liveInfo == null && !ranges.isEmpty()) {
                            liveInfo = ranges.get(0);
                        }
                        
                        if (liveInfo != null) {
                            droneLiveModule = drone.getLiveStream(liveInfo);
                            droneLiveModule.start(new FlightHubCallback.ResultCallbackWithError() {
                                @Override
                                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                    if (error.isSuccess()) {
                                        log("INFO", "handleSeiRtmpStream", "无人机直播流启动成功");
                                        // 启动飞机 RTMP 推流
                                        try {
                                            droneStreamer = new FFmpegPipeStreamer();
                                            droneStreamer.start(rtmpUrl, rtmpFps, cfg.rtmp.width, cfg.rtmp.height, cfg.rtmp.bitrate);
                                            log("INFO", "handleSeiRtmpStream", "飞机 RTMP 推流已启动 -> " + rtmpUrl);
                                            
                                            // 启动推流状态监控
                                            startStreamMonitor();
                                        } catch (Exception e) {
                                            log("ERROR", "handleSeiRtmpStream", "启动飞机 RTMP 推流失败: " + e.getMessage());
                                        }
                                    } else {
                                        log("ERROR", "handleSeiRtmpStream", "无人机直播流启动失败，错误码: " + error);
                                    }
                                }
                            });
                            
                            droneLiveStreamListenId = droneLiveModule.subscribeLiveStream(new FlightHubCallback.LiveStreamCallback() {
                                private int frameCount = 0;
                                private long totalBytes = 0;
                                
                                @Override
                                public void onResult(byte[] data, FrameInfo frameInfo) {
                                    if(data == null) {
                                        log("ERROR", "handleSeiRtmpStream", "收到无人机直播流数据为空");
                                        return;
                                    }
                                    if(data.length == 0) {
                                        log("ERROR", "handleSeiRtmpStream", "收到无人机直播流数据长度为0");
                                        return;
                                    }
                                    frameCount++;
                                    totalBytes += data.length;
                                    if (frameCount % 30 == 0) {
                                        log("INFO", "handleSeiRtmpStream", "收到无人机直播流数据 #" + frameCount + 
                                            ": " + data.length + " bytes (总计: " + totalBytes + " bytes)");
                                    }
                                    
                                    // 每 60 帧（约 2 秒）更新一次 SEI 数据
                                    if (customSeiModule != null && frameCount % 60 == 0) {
                                        customSeiData.clear();
                                        customSeiData.addAll(generateRandomSeiData());
                                        log("INFO", "handleSeiRtmpStream", "第 " + frameCount + " 帧：SEI 数据已更新");
                                    }
                                    
                                    // 处理自定义 SEI 和 RTMP 推流
                                    byte[] streamData = data;
                                    if (rtmpEnabled && droneStreamer != null) {
                                        // 如果有自定义 SEI 模块，添加 SEI 信息
                                        if (customSeiModule != null && customSeiData != null && !customSeiData.isEmpty()) {
                                            try {
                                                // 将 byte[] 转换为 List<Integer>
                                                java.util.List<Integer> frameData = new java.util.ArrayList<>();
                                                for (byte b : data) {
                                                    frameData.add(b & 0xFF);
                                                }
                                                
                                                // 添加自定义 SEI
                                                java.util.List<Integer> encodedData = customSeiModule.customSEIEncode(frameData, customSeiData);
                                                
                                                if (encodedData != null && !encodedData.isEmpty()) {
                                                    // 将编码后的数据转换回 byte[]
                                                    streamData = new byte[encodedData.size()];
                                                    for (int i = 0; i < encodedData.size(); i++) {
                                                        streamData[i] = (byte) (encodedData.get(i) & 0xFF);
                                                    }
                                                    if (frameCount % 100 == 0) {
                                                        log("INFO", "handleSeiRtmpStream", "已添加自定义 SEI 到第 " + frameCount + " 帧");
                                                    }
                                                }
                                            } catch (Exception e) {
                                                log("ERROR", "handleSeiRtmpStream", "添加自定义 SEI 失败: " + e.getMessage());
                                                // 如果 SEI 处理失败，使用原始数据
                                                streamData = data;
                                            }
                                        }
                                        
                                        // 异步推送到 ffmpeg，避免阻塞回调
                                        droneStreamer.pushFrame(streamData);
                                    }
                                }
                            });
                        }
                    }
                }
            });

            log("INFO", "handleSeiRtmpStream", "SEI + RTMP 推流已启动，按Enter键停止...");
            waitForKeyPress();
        } catch (Exception e) {
            log("ERROR", "handleSeiRtmpStream", "SEI + RTMP 推流处理异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 停止推流监控
            stopStreamMonitor();

            // 停止飞机推流
            if (droneStreamer != null) {
                droneStreamer.stop();
                droneStreamer = null;
            }

            // 停止无人机直播流
            if (droneLiveModule != null) {
                droneLiveModule.stop();
                droneLiveModule = null;
            }

            if (droneLiveStreamListenId != -1) {
                droneLiveStreamListenId = -1;
            }
        }
    }

    /**
     * 处理拍照功能
     */
    private static void handleTakePhoto() {
        printSeparator("拍照功能");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleTakePhoto", "该设备没有无人机，无法拍照");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleTakePhoto", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleTakePhoto", "正在连接无人机...");

        log("INFO", "handleTakePhoto", "正在拍照...");
        drone.getCameraControlModule().drcTakePhoto(targetPayloadIndex, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (error.isSuccess()) {
                    log("INFO", "handleTakePhoto", "拍照成功！");
                } else {
                    log("ERROR", "handleTakePhoto", "拍照失败，错误码: " + error);
                }
            }
        });

        log("INFO", "handleTakePhoto", "等待拍照完成，按Enter键返回...");
        waitForKeyPress();
    }
    
    /**
     * 处理无人机OSD监听
     */
    private static void handleDroneOsd() {
        printSeparator("监听飞机 OSD");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleDroneOsd", "该设备没有无人机，无法监听 OSD");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleDroneOsd", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDroneOsd", "开始监听无人机OSD信息, droneSn=" + getSubDeviceSn(selectedDevice));

        drone.getDroneOsdModule().listenAircraftBatteryInfo(new FlightHubCallback.AircraftBatteryInfoCallback() {
            @Override
            public void onResult(AircraftBatteryInfo batteryInfo) {
                log("INFO", "listenAircraftBatteryInfo", "Received aircraft battery info message for device " + getSubDeviceSn(selectedDevice) + ":");
                log("INFO", "listenAircraftBatteryInfo", "  - Capacity Percent (电池组容量百分比): " + batteryInfo.getCapacityPercent() + "%");
                log("INFO", "listenAircraftBatteryInfo", "  - Landing Power (降落电量): " + batteryInfo.getLandingPower() + "%");
                log("INFO", "listenAircraftBatteryInfo", "  - Remain Flight Time (剩余飞行时间): " + batteryInfo.getRemainFlightTime() + "s");
                log("INFO", "listenAircraftBatteryInfo", "  - Return Home Power (返航电量): " + batteryInfo.getReturnHomePower() + "%");

                List<BatteryInfo> batteries = batteryInfo.getBatteries();
                if (batteries != null && !batteries.isEmpty()) {
                    log("INFO", "listenAircraftBatteryInfo", "  - batterie (电池详细信息):");
                    for (int i = 0; i < batteries.size(); i++) {
                        BatteryInfo bi = batteries.get(i);
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Capacity Percent (电量百分比): " + bi.getCapacityPercent());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Firmware version (固件版本号): " + bi.getFirmwareVersion());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "High voltage storage days (高电位存储天数): " + bi.getHighVoltageStorageDays());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Index (电池序号): " + bi.getIndex());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Loop times (循环次数): " + bi.getLoopTimes());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Sn (电池序列号): " + bi.getSn());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Type (类型): " + bi.getType());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Sub type (子类型): " + bi.getSubType());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Temperature (电池温度): " + bi.getTemperature());
                        log("INFO", "listenAircraftBatteryInfo", "    [" + i + "] " + "Voltage (电压mV): " + bi.getVoltage());
                    }
                } else {
                    log("INFO", "AircraftBatteryInfo", "  - Payloads List: 空列表或无数据");
                }
            }
        });

        drone.getDroneOsdModule().listenAircraftAttitude(new FlightHubCallback.AircraftAttitudeCallback() {
            @Override
            public void onResult(AircraftAttitude attitude) {
                log("INFO", "handleDroneOsd", "listenAircraftAttitude: " + 
                    attitude.getAttitudeHead() + ", " + attitude.getAttitudePitch() + ", " + attitude.getAttitudeRoll());
                log("INFO", "listenAircraftAttitude", " ");
            }
        });

        drone.getDroneOsdModule().listenAircraftLocation(new FlightHubCallback.AircraftLocationCallback() {
            @Override
            public void onResult(AircraftLocation location) {
                log("INFO", "handleDroneOsd", "listenAircraftLocation: " + 
                    location.getLatitude() + ", " + location.getLongitude() + ", " + location.getHeight());
            }
        });

        drone.getDroneOsdModule().listenAircraftModeCode(new FlightHubCallback.AircraftModeCodeCallback() {
            @Override
            public void onResult(AircraftModeCode modeCode) {
                log("INFO", "handleDroneOsd", "listenAircraftModeCode: " + modeCode.getModeCode());
            }
        });

        drone.getDroneOsdModule().listenAircraftWindSpeed(new FlightHubCallback.AircraftWindSpeedCallback() {
            @Override
            public void onResult(AircraftWindSpeed windSpeed) {
                log("INFO", "handleDroneOsd", "listenAircraftWindSpeed: " + windSpeed.getWindSpeed());
            }
        });

        // 飞机mode_code监听
        drone.getDroneOsdModule().listenAircraftControlCode(new FlightHubCallback.AircraftControlCodeCallback() {
            @Override
            public void onResult(AircraftControlCode controlCode) {
                log("INFO", "listenAircraftControlCode", "收到飞机控制码: " + controlCode.getControlMode());
                log("INFO", "listenAircraftControlCode", " ");
            }
        });


        // 飞机速度监听
        drone.getDroneOsdModule().listenAircraftSpeed(new FlightHubCallback.AircraftSpeedCallback() {
            @Override
            public void onResult(AircraftSpeed speed) {
                log("INFO", "listenAircraftSpeed", "收到飞机速度: horizontal=" + speed.getHorizontalSpeed() + ", vertical=" + speed.getVerticalSpeed());
                log("INFO", "listenAircraftSpeed", " ");
            }
        });


        // 荷相机实时视图世界区域监听
        drone.getDroneOsdModule().listenAircraftPayloadsCameraLiveviewWorldRegion(new FlightHubCallback.AircraftPayloadsCameraLiveviewWorldRegionCallback() {
            @Override
            public void onResult(AircraftPayloadsCameraLiveviewWorldRegion region) {
                int n = region.getPayloadsList().size();
                log("INFO", "listenAircraftPayloadsCameraLiveviewWorldRegion", "收到载荷相机实时视图世界区域, 条目数=" + n);
                for (AircraftPayloadLiveviewWorldRegionEntry e : region.getPayloadsList()) {
                    if (e.getRegion() != null) {
                        log("INFO", "listenAircraftPayloadsCameraLiveviewWorldRegion",
                                "  payload=" + e.getPayloadIndex() + " bottom=" + e.getRegion().getBottom());
                    }
                }
            }
        });


        // 载荷云台姿态监听
        drone.getDroneOsdModule().listenAircraftPayloadsGimbalAttitude(new FlightHubCallback.AircraftPayloadsGimbalAttitudeCallback() {
            @Override
            public void onResult(AircraftPayloadsGimbalAttitude attitude) {
                int n = attitude.getPayloadsGimbalAttitude().size();
                log("INFO", "listenAircraftPayloadsGimbalAttitude", "收到载荷云台姿态, 条目数=" + n);
                for (AircraftPayloadGimbalAttitude it : attitude.getPayloadsGimbalAttitude()) {
                    log("INFO", "listenAircraftPayloadsGimbalAttitude",
                            "  payload=" + it.getPayloadIndex() + " pitch=" + it.getGimbalPitch());
                }
            }
        });


        // 载荷列表监听
        drone.getDroneOsdModule().listenAircraftPayloadsList(new FlightHubCallback.AircraftPayloadsListCallback() {
            @Override
            public void onResult(AircraftPayloadsList payloadsList) {
                log("INFO", "listenAircraftPayloadsList", "收到载荷列表更新, size=" + payloadsList.getPayloadsList().size());
            }
        });


        // 飞机 HMS data 监听;
        drone.getDeviceHMSModule().listenDeviceHMS(new FlightHubCallback.DeviceHmsCallback() {
            @Override
            public void onResult(HmsData hmsData) {
                List<HmsDataItem> hmsDataItemVec = hmsData.getHmsDataItems();
                log("INFO", "listenAircraftHMS", "收到HMS数据, hmsData size: " + hmsDataItemVec.size());

                DataProcessor printHmsDataBasics = (prefix, hmsDataBasics) -> {
                    if (hmsDataBasics == null) {
                        log("INFO", "listenAircraftHMS", prefix + "    -- basics: null object.");
                    } else {
                        log("INFO", "listenAircraftHMS", prefix + "    ==> basics: ");
                        log("INFO", "listenAircraftHMS", prefix + "      --- level: " + hmsDataBasics.getLevel() + ", 告警等级, {0: 通知, 1: 提醒, 2: 警告}");
                        log("INFO", "listenAircraftHMS", prefix + "      --- module_id: " + hmsDataBasics.getModuleId() + ", 事件模块, {0: 飞行任务, 1: 设备管理, 2 : 媒体, 3: hms}");
                        log("INFO", "listenAircraftHMS", prefix + "      --- code: " + hmsDataBasics.getCode());
                        log("INFO", "listenAircraftHMS", prefix + "      --- device_type: " + hmsDataBasics.getDeviceType());
                        log("INFO", "listenAircraftHMS", prefix + "      --- domain_type: " + hmsDataBasics.getDomainType());
                        log("INFO", "listenAircraftHMS", prefix + "      --- in_the_sky: " + hmsDataBasics.getInTheSky());
                        log("INFO", "listenAircraftHMS", prefix + "      --- imminent: " + hmsDataBasics.getImminent() +", 是否为及时性的, {0: 否, 1: 是}");
                        log("INFO", "listenAircraftHMS", prefix + "      --- status_key: " + hmsDataBasics.getStatusKey());
                        log("INFO", "listenAircraftHMS", prefix + "      --- joint_key: " + hmsDataBasics.getJointKey());
                        log("INFO", "listenAircraftHMS", prefix + "      --- id: " + hmsDataBasics.getId() +", 每个设备上报事件里hms的唯一id");
                        log("INFO", "listenAircraftHMS", prefix + "      --- parent_id: " + hmsDataBasics.getParentId() +", 每个设备上报事件里hms的唯一父id");

                        HmsArgs hmsArgs = hmsDataBasics.getArgs();
                        log("INFO", "listenAircraftHMS", prefix + "      --- args: ");
                        log("INFO", "listenAircraftHMS", prefix + "          --> component_index: " + hmsArgs.getComponentIndex());
                        log("INFO", "listenAircraftHMS", prefix + "          --> sensor_index: " + hmsArgs.getSensorIndex());
                        log("INFO", "listenAircraftHMS", prefix + "          --> device_id: " + hmsArgs.getDeviceId());
                        log("INFO", "listenAircraftHMS", prefix + "          --> sender_host_id: " + hmsArgs.getSenderHostId());
                        log("INFO", "listenAircraftHMS", prefix + "          --> ignore: " + hmsArgs.getIgnore() +", 告警是否忽略, {0:不忽略, 1: 忽略}");
                        log("INFO", "listenAircraftHMS", prefix + "          --> alarm_info: " + hmsArgs.getAlarmInfo());
                        log("INFO", "listenAircraftHMS", prefix + "          --> create_timestamp: " + hmsArgs.getCreateTimestamp() + ", 告警产生的UTC时间(单位秒)");
                        log("INFO", "listenAircraftHMS", prefix + "          --> display_scene: " + hmsArgs.getDisplayScene() + ", 告警是否忽略, {0: 正式错误码, 1: 调试错误码, 只用于排障}");
                    }
                };

                log("INFO", "listenAircraftHMS()", "START -------------------------------------------------------------------------------------------------------------------------------");
                for (int i = 0; i < hmsDataItemVec.size(); i++) {
                    HmsDataItem hmsDataItem = hmsDataItemVec.get(i);
                    if (hmsDataItem == null) {
                        log("INFO", "listenAircraftHMS", "  - HMS data_item[ + " + i + "]: null object.");
                    } else {
                        log("INFO", "listenAircraftHMS", "  - HMS data_item[ + " + i + "]: ");
                        HmsDataBasics hmsDataBasics = hmsDataItem.getBasics();
                        printHmsDataBasics.print("", hmsDataBasics);

                        List<HmsDataBasics> subHmsDataList = hmsDataItem.getSubHmsData();
                        if (subHmsDataList == null || subHmsDataList.isEmpty()) {
                            log("INFO", "listenAircraftHMS", "    ==> sub_hms_data: 空或无数据.");
                        } else {
                            long size = subHmsDataList.size();
                            log("INFO", "listenAircraftHMS", "    ==> sub_hms_data size: " + size);
                            for (int j = 0; j < size; j++) {
                                printHmsDataBasics.print("    ", subHmsDataList.get(j));
                            }
                        }
                    }
                    log("INFO", "listenDockHMS", " ");
                }
                log("INFO", "listenAircraftHMS()", "END -------------------------------------------------------------------------------------------------------------------------------");
            }
        });


        // 监听: 飞机负载 OSD 信息列表({type-subtype-gimbalindex} + payloads);
        // AircraftPayloadsOsdInfoList
        drone.getDroneOsdModule().listenAircraftPayloadsOsdInfo(new FlightHubCallback.AircraftPayloadsOsdInfoListCallback() {
            @Override
            public void onResult(AircraftPayloadsOsdInfoList aircraftPayloadsOsdInfoList) {
                log("INFO", "listenAircraftPayloadsOsdInfo()", "START ------------------------------------------------------------------------------------------------------------------");
                if (aircraftPayloadsOsdInfoList == null) {
                    log("INFO", "listenAircraftPayloadsOsdInfo", "aircraftPayloadsOsdInfoList: null object.");
                } else {
                    log("INFO", "listenAircraftPayloadsOsdInfo", "aircraftPayloadsOsdInfoList: ");

                    List<AircraftPayloadsOsdInfo> payloads_info_list = aircraftPayloadsOsdInfoList.getPayloadsInfoList();
                    if (payloads_info_list == null || payloads_info_list.isEmpty()) {
                        log("INFO", "listenAircraftPayloadsOsdInfo", "  - payloads_info_list: 空或无数据.");
                    } else {
                        long size = payloads_info_list.size();
                        log("INFO", "listenAircraftPayloadsOsdInfo", "  - payloads_info_list size: " + size);
                        for (int i = 0; i < size; i++) {
                            AircraftPayloadsOsdInfo payload_info = payloads_info_list.get(i);
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> payload_index:" + payload_info.getPayloadIndex());
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> gimbal_pitch:" + payload_info.getGimbalPitch() + ", 云台俯仰轴角度, [-180, 180]度");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> gimbal_roll:" + payload_info.getGimbalRoll() + ", 云台横滚轴角度,[-180, 180]度");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> gimbal_yaw:" + payload_info.getGimbalYaw() + ", 云台朝向角度/云台偏航轴角度,[-180, 180]度");

                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> measure_target_altitude:" + payload_info.getMeasureTargetAltitude() + ", 激光测距目标海拔(单位: m)");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> measure_target_longitude:" + payload_info.getMeasureTargetLongitude() + ", 激光测距目标经度,[-180,180]度");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> measure_target_latitude:" + payload_info.getMeasureTargetLatitude() + ", 激光测距目标纬度,[-90, 90]度");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> measure_target_distance:" + payload_info.getMeasureTargetDistance() + ", 测距距离(单位: m)");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> measure_target_error_state:" + payload_info.getMeasureTargetErrorState() +
                                ", 测距状态, 0: NORMAL, 1: TOO_CLOSE, 2: TOO_FAR, 3: NO_SIGNAL");

                            List<SmartTrackPointInfo> smart_track_points = payload_info.getSmartTrackPoints();
                            if (smart_track_points == null || smart_track_points.isEmpty()) {
                                log("INFO", "listenAircraftPayloadsOsdInfo", "      --> smart_track_points: 空或无数据.");
                            } else {
                                long points_size = smart_track_points.size();
                                log("INFO", "listenAircraftPayloadsOsdInfo", "      --> smart_track_points size: " + points_size);
                                for (int j = 0; j < points_size; j++) {
                                    SmartTrackPointInfo smart_track_point = smart_track_points.get(j);
                                    log("INFO", "listenAircraftPayloadsOsdInfo", "          --> track_altitude:" + smart_track_point.getTrackAltitude() + ", 跟踪目标海拔高度(单位: m)");
                                    log("INFO", "listenAircraftPayloadsOsdInfo", "          --> track_latitude:" + smart_track_point.getTrackLatitude() + ", 跟踪目标纬度,[-90, 90]度");
                                    log("INFO", "listenAircraftPayloadsOsdInfo", "          --> track_longitude:" + smart_track_point.getTrackLongitude() + ", 跟踪目标经度,[-180, 180]度");
                                    log("INFO", "listenAircraftPayloadsOsdInfo", "          --> track_target_mode:" + smart_track_point.getTrackTargetMode() +
                                        ", track 模式, {1: 正常跟踪, 2: 正常跟踪，可信度低, 3: 目标是GPS点，或目标丢失但有预测}");

                                    log("INFO", "listenAircraftPayloadsOsdInfo", " ");
                                }
                            }
                            log("INFO", "listenAircraftPayloadsOsdInfo", " ");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> zoom_factor:" + payload_info.getZoomFactor());
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_current_palette_style:" + payload_info.getThermalCurrentPaletteStyle() + ",  调色盘样式");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_gain_mode:" + payload_info.getThermalGainMode() + ", 增益模式");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_global_temperature_min:" + payload_info.getThermalGlobalTemperatureMin() + ", 全局画面中测量的最低温度(摄氏度)");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_global_temperature_max:" + payload_info.getThermalGlobalTemperatureMax() + ", 全局画面中测量的最高温度(摄氏度)");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_isotherm_state:" + payload_info.getThermalIsothermState() + ", 是否开启等温线, {0: 关闭, 1: 开启}");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_isotherm_lower_limit:" + payload_info.getThermalIsothermLowerLimit() + ", 测温区间下限(摄氏度), 仅启用等温线功能后有效");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_isotherm_upper_limit:" + payload_info.getThermalIsothermUpperLimit() + ", 测温区间上限(摄氏度), 仅启用等温线功能后有效");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> thermal_supported_palette_styles:" + payload_info.getThermalSupportedPaletteStyles() +
                                ",  设备支持的调色盘样式集合, 不同设备所支持的样式能力有所不同."
                            );

                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> control_source:" + payload_info.getControlSource() + ", 负载控制权");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> firmware_version:" + payload_info.getFirmwareVersion() + ", 负载固件版本");
                            log("INFO", "listenAircraftPayloadsOsdInfo", "      --> sn:" + payload_info.getSn() + ", 负载sn");

                            log("INFO", "listenAircraftPayloadsOsdInfo", "");
                        }
                    }

                    log("INFO", "listenAircraftPayloadsOsdInfo", " ");
                }
                log("INFO", "listenAircraftPayloadsOsdInfo()", "END ------------------------------------------------------------------------------------------------------------------");
            }
        });


        // 监听飞机: 档位信息(gear);
        drone.getDroneOsdModule().listenAircraftGear(new FlightHubCallback.AircraftGearCallback() {
            @Override
            public void onResult(AircraftGear gearInfo) {
                log("INFO", "listenAircraftGear", "飞机: 档位信息(gear) {0: A, 1: P, 2: NAV, 3: FPV, 4: FARM, 5: S, 6: F, 7: M, 8: G, 9: T }");
                log("INFO", "listenAircraftGear", "gear: " + gearInfo.getGear() + ", 档位信息");
                log("INFO", "listenAircraftGear", " ");
            }
        });


        // 监听飞机: Home点距离(home_distance);
        // AircraftHomeDistance;
        drone.getDroneOsdModule().listenAircraftHomeDistance(new FlightHubCallback.AircraftHomeDistanceCallback() {
            @Override
            public void onResult(AircraftHomeDistance homeDistanceInfo) {
                log("INFO", "listenAircraftHomeDistance", "Home点距离(home_distance)");
                log("INFO", "listenAircraftHomeDistance", "home_distance: " + homeDistanceInfo.getHomeDistance() + ", Home点距离(单位: m)");
                log("INFO", "listenAircraftHomeDistance", " ");
            }
        });


        // 监听飞机: 搜星状态(position_state);
        // AircraftPositionState;
        drone.getDroneOsdModule().listenAircraftPositionState(new FlightHubCallback.AircraftPositionStateCallback() {
            @Override
            public void onResult(AircraftPositionState positionStateInfo) {
                log("INFO", "listenAircraftPositionState", "搜星状态(position_state)");
                log("INFO", "listenAircraftPositionState", "  --> is_fixed: " + positionStateInfo.getIsFixed() + ", 是否收敛,{0: 非开始, 1: 收敛中, 2: 收敛成功, 3: 收敛失败}");
                log("INFO", "listenAircraftPositionState", "  --> quality: " + positionStateInfo.getQuality() + ", 搜星档位,{1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档}");
                log("INFO", "listenAircraftPositionState", "  --> gps_number: " + positionStateInfo.getGpsNumber() + ", gps搜星数量");
                log("INFO", "listenAircraftPositionState", "  --> rtk_number: " + positionStateInfo.getRtkNumber() + ", rtk搜星数量");
                log("INFO", "listenAircraftPositionState", " ");
            }
        });


        // 飞机: 当前控制源(control_source);
        // AircraftControlSource;
        drone.getDroneOsdModule().listenAircraftControlSource(new FlightHubCallback.AircraftControlSourceCallback() {
            @Override
            public void onResult(AircraftControlSource controlSource) {
                log("INFO", "listenAircraftControlSource", "飞机: 当前控制源(control_source)");
                log("INFO", "listenAircraftControlSource", "control_source: " + controlSource.getControlSource() + ", 当前控制源");
                log("INFO", "listenAircraftControlSource", " ");
            }
        });


        // 飞机: 限制类信息状态;
        // AircraftLimitsInfo;
        drone.getDroneOsdModule().listenAircraftLimitsInfo(new FlightHubCallback.AircraftLimitsInfoCallback() {
            @Override
            public void onResult(AircraftLimitsInfo limitsInfo) {
                log("INFO", "listenAircraftLimitsInfo", "限制类信息状态");
                log("INFO", "listenAircraftLimitsInfo", "height_limit: " + limitsInfo.getHeightLimit() + ", 相对起飞点的飞行限制高度,单位(m)");
                log("INFO", "listenAircraftLimitsInfo", "is_near_height_limit: " + limitsInfo.getIsNearHeightLimit() + ", 是否接近设定的限制高度, {0: 未达到设定的限制高度, 1: 接近设定的限制高度}");
                log("INFO", "listenAircraftLimitsInfo", "is_near_area_limit: " + limitsInfo.getIsNearAreaLimit() + ", 是否接近限飞区,{0: 未达到限飞区, 1:1: 接近限飞区}");
                log("INFO", "listenAircraftLimitsInfo", "distance_limit_state: " + limitsInfo.getDistanceLimitState() + ", 限远状态,{0: 未设置, 1: 有设置}");
                log("INFO", "listenAircraftLimitsInfo", "distance_limit: " + limitsInfo.getDistanceLimit() + ", 限远距离");
                log("INFO", "listenAircraftLimitsInfo", "is_near_distance_limit: " + limitsInfo.getIsNearDistanceLimit() + ", 是否接近限远距离");
                log("INFO", "listenAircraftLimitsInfo", " ");
            }
        });


        // 电量告警信息(用户设置的阈值);
        // AircraftBatteryWarningThresholds;
        drone.getDroneOsdModule().listenAircraftBatteryWarningThresholds(new FlightHubCallback.AircraftBatteryWarningThresholdsCallback() { 
            @Override
            public void onResult(AircraftBatteryWarningThresholds thresholds) {
                log("INFO", "listenAircraftBatteryWarningThresholds", "电量告警信息(用户设置的阈值)");
                log("INFO", "listenAircraftBatteryWarningThresholds", "  --> low_battery_warning_threshold: " + thresholds.getLowBatteryWarningThreshold() +
                        ", 用户设置的电池低电量告警百分比, [0, 100]");
                log("INFO", "listenAircraftBatteryWarningThresholds", "  --> serious_low_battery_warning_threshold: " + thresholds.getSeriousLowBatteryWarningThreshold() +
                        ", 用户设置的电池严重低电量告警百分比, [0, 100]");

                log("INFO", "listenAircraftBatteryWarningThresholds", "  ");
            }
        });


        // 固件状态信息;
        // AircraftFirmwareStatus;
        drone.getDroneOsdModule().listenAircraftFirmwareStatus(new FlightHubCallback.AircraftFirmwareStatusCallback() {
            @Override
            public void onResult(AircraftFirmwareStatus firmwareStatus) {
                log("INFO", "listenAircraftFirmwareStatus", "固件状态信息");
                log("INFO", "listenAircraftFirmwareStatus", "  --> firmware_version: " + firmwareStatus.getFirmwareVersion() + ", 固件版本, such as \"00.01.0439\"");
                log("INFO", "listenAircraftFirmwareStatus", "  --> compatible_status: " + firmwareStatus.getCompatibleStatus() + ", 固件一致性, {0: 一致, 1: 不一致} ");
                log("INFO", "listenAircraftFirmwareStatus", "  --> firmware_upgrade_status: " + firmwareStatus.getFirmwareUpgradeStatus() + ", 固件升级状态, {0: 未升级, 1: 升级中}");
                log("INFO", "listenAircraftFirmwareStatus", " ");
            }
        });


        // 累计飞行信息(total_flight_info);
        // AircraftTotalFlightInfo;
        drone.getDroneOsdModule().listenAircraftTotalFlightInfo(new FlightHubCallback.AircraftTotalFlightInfoCallback() {
            @Override
            public void onResult(AircraftTotalFlightInfo totalFlightInfo) {
                log("INFO", "listenAircraftTotalFlightInfo", "累计飞行信息(total_flight_info)");
                log("INFO", "listenAircraftTotalFlightInfo", "  --> total_flight_time: " + totalFlightInfo.getTotalFlightTime() + ", 总飞行时长(飞行器累计飞行航时)");
                log("INFO", "listenAircraftTotalFlightInfo", "  --> total_flight_distance: " + totalFlightInfo.getTotalFlightDistance() + ", 总飞行距离");
                log("INFO", "listenAircraftTotalFlightInfo", "  --> total_flight_sorties: " + totalFlightInfo.getTotalFlightSorties() + ", 总飞行架次");
                log("INFO", "listenAircraftTotalFlightInfo", " ");
            }
        });


        // 飞行器激活时间(unix时间戳)(activation_time);
        // AircraftActivationTime;
        drone.getDroneOsdModule().listenAircraftActivationTime(new FlightHubCallback.AircraftActivationTimeCallback() {
            @Override
            public void onResult(AircraftActivationTime activationTime) {
                log("INFO", "listenAircraftActivationTime", "激活时间(unix时间戳)(activation_time)");
                log("INFO", "listenAircraftActivationTime", "  --> activation_time: " + activationTime.getActivationTime() + ", 激活时间(unix时间戳)");
                log("INFO", "listenAircraftActivationTime", " ");
            }
        });

        // 飞机：保养信息(maintain_status);
        // AircraftMaintainStatusInfo;
        drone.getDroneOsdModule().listenAircraftMaintainStatusInfo(new FlightHubCallback.AircraftMaintainStatusInfoCallback() {
            @Override
            public void onResult(AircraftMaintainStatusInfo maintainStatusInfo) {
                List<AircraftMaintainStatus> maintainStatuses = maintainStatusInfo.getMaintainStatuses();
                if (maintainStatuses == null || maintainStatuses.isEmpty()) {
                    log("INFO", "listenAircraftMaintainStatusInfo", "  --> maintainStatuses: 空列表或无数据");
                } else {
                    for (int i = 0; i < maintainStatuses.size(); i++) {
                        AircraftMaintainStatus maintainStatus = maintainStatuses.get(i);

                        log("INFO", "listenAircraftMaintainStatusInfo", "飞机：保养信息(maintain_status) maintainStatus: [" + i + "]");
                        log("INFO", "listenAircraftMaintainStatusInfo", "    --> state: " + maintainStatus.getState() +
                            ", 保养状态(0: 未保养, 1: 保养中)");
                        log("INFO", "listenAircraftMaintainStatusInfo", "    --> last_maintain_type: " + maintainStatus.getLastMaintainType() +
                            ", 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养)");
                        log("INFO", "listenAircraftMaintainStatusInfo", "    --> last_maintain_time: " + maintainStatus.getLastMaintainTime() +
                            ", 上一次保养时间, utc 秒时间戳(单位: 秒)");
                        log("INFO", "listenAircraftMaintainStatusInfo", "    --> last_maintain_flight_time: " + maintainStatus.getLastMaintainFlightTime() +
                            ", 上一次保养时飞行航时(单位: h)");
                        log("INFO", "listenAircraftMaintainStatusInfo", "    --> last_maintain_flight_sorties: " + maintainStatus.getLastMaintainFlightSorties() +
                            ", 上一次保养时飞行架次");
                    }
                }

                log("INFO", "listenAircraftMaintainStatusInfo", " ");
            }
        });


        // 飞行器夜航灯状态(night_lights_state);
        // AircraftNightLightsState;
        drone.getDroneOsdModule().listenAircraftNightLightsState(new FlightHubCallback.AircraftNightLightsStateCallback() {
            @Override
            public void onResult(AircraftNightLightsState nightLightsState) {
                log("INFO", "listenAircraftNightLightsState", "飞行器夜航灯状态(night_lights_state)");
                log("INFO", "listenAircraftNightLightsState", "  --> night_lights_state: " + nightLightsState.getNightLightsState() + ", 夜航灯状态(0: 关闭, 1: 开启)");
                log("INFO", "listenAircraftNightLightsState", " ");
            }
        });


        // 飞行器避障状态(obstacle_avoidance);
        // AircraftObstacleAvoidanceState;
        drone.getDroneOsdModule().listenAircraftObstacleAvoidanceState(new FlightHubCallback.AircraftObstacleAvoidanceStateCallback() {
            @Override
            public void onResult(AircraftObstacleAvoidanceState obstacleAvoidanceState) {
                log("INFO", "listenAircraftObstacleAvoidanceState", "飞行器避障状态(obstacle_avoidance)");
                log("INFO", "listenAircraftObstacleAvoidanceState", "  --> horizon: " + obstacleAvoidanceState.getHorizon() + ", 水平避障状态, {0:关闭, 1:开启} ");
                log("INFO", "listenAircraftObstacleAvoidanceState", "  --> upside: " + obstacleAvoidanceState.getUpside() + ", 上视避障状态, {0:关闭, 1:开启} ");
                log("INFO", "listenAircraftObstacleAvoidanceState", "  --> downside: " + obstacleAvoidanceState.getDownside() + ", 下视避障状态, {0:关闭, 1:开启} ");
                log("INFO", "listenAircraftObstacleAvoidanceState", " ");
            }
        });


        // 存储容量(storage);
        // AircraftStorageInfo;
        drone.getDroneOsdModule().listenAircraftStorageInfo(new FlightHubCallback.AircraftStorageInfoCallback() {
            @Override
            public void onResult(AircraftStorageInfo storageInfo) {
                log("INFO", "listenAircraftStorageInfo", "存储容量(storage)");
                log("INFO", "listenAircraftStorageInfo", "  --> total: " + storageInfo.getTotal() + ", 总容量 ");
                log("INFO", "listenAircraftStorageInfo", "  --> used: " + storageInfo.getUsed() + ",  已使用 ");
                log("INFO", "listenAircraftStorageInfo", " ");
            }
        });


        // 航迹id(track_id);
        // AircraftTrackId;
        drone.getDroneOsdModule().listenAircraftTrackId(new FlightHubCallback.AircraftTrackIdCallback() { 
            @Override
            public void onResult(AircraftTrackId id) {
                log("INFO", "listenAircraftTrackId", "航迹id(track_id)");
                log("INFO", "listenAircraftTrackId", "  --> track_id: " + "\"" + id.getTrackId()  + "\"");
                log("INFO", "listenAircraftTrackId", " ");
            }
        });


        // 合规相关信息(compliance information);
        // AircraftComplianceInfo;
        drone.getDroneOsdModule().listenAircraftComplianceInfo(new FlightHubCallback.AircraftComplianceInfoCallback() {
            @Override
            public void onResult(AircraftComplianceInfo complianceInfo) {
                log("INFO", "listenAircraftComplianceInfo", "合规相关信息(compliance information)");
                log("INFO", "listenAircraftComplianceInfo", "  --> rid_state: " + complianceInfo.getRidState() +
                    ", RID(Remote Identification)工作状态, {1: 正常, 0: 异常} ");
                log("INFO", "listenAircraftComplianceInfo", "  --> country: " + complianceInfo.getCountry() + ", 国家区域码");
                log("INFO", "listenAircraftComplianceInfo", " ");
            }
        });


        // 飞行器相机信息(cameras);
        // AircraftPayloadsCamerasList;
        drone.getDroneOsdModule().listenAircraftPayloadsCamerasList(new FlightHubCallback.AircraftPayloadsCamerasListCallback() {
            @Override
            public void onResult(AircraftPayloadsCamerasList camerasList) {
                log("INFO", "listenAircraftPayloadsCamerasList", "飞行器相机信息(cameras)");
                if (camerasList == null) {
                    log("INFO", "listenAircraftPayloadsCamerasList", "  --> camerasList is null");
                    return;
                }

                List<AircraftPayloadCameraInfo> cam_list = camerasList.getCamerasList();
                if (cam_list == null || cam_list.isEmpty()) {
                    log("INFO", "listenAircraftPayloadsCamerasList", "  --> camerasList.cameras_list 空或无数据");
                    return;
                }

                log("INFO", "listenAircraftPayloadsCamerasList", "  --> camerasList.cameras_list.size(): " + cam_list.size());
                for (int i = 0; i < cam_list.size(); i++) {
                    AircraftPayloadCameraInfo cam = cam_list.get(i);
                    if (cam == null) {
                        log("INFO", "listenAircraftPayloadsCamerasList", "  --> cameras_list[" + i + "] is null");
                        continue;
                    }

                    log("INFO", "listenAircraftPayloadsCamerasList", "  --> cameras_list[" + i + "]: ");
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> payload_index: " + cam.getPayloadIndex());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> camera_mode: " + cam.getCameraMode());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> ir_metering_mode: " + cam.getIrMeteringMode());

                    CameraIrMeteringPoint ir_metering_point = cam.getIrMeteringPoint();
                    if (ir_metering_point == null) {
                        log("INFO", "listenAircraftPayloadsCamerasList", "      --> ir_metering_point is null");
                    } else {
                        log("INFO", "listenAircraftPayloadsCamerasList", "      --> ir_metering_point: { " +
                            "temperature: " + ir_metering_point.getTemperature() + ", " +
                            "x: " + ir_metering_point.getX() + ", " +
                            "y: " + ir_metering_point.getY() + " }"
                        );
                    }

                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> ir_zoom_factor: " + cam.getIrZoomFactor());

                    CameraLiveviewWorldRegion liveview_world_region = cam.getLiveviewWorldRegion();
                    if (liveview_world_region == null) {
                        log("INFO", "listenAircraftPayloadsCamerasList", "      --> liveview_world_region is null");
                    } else {
                        log("INFO", "listenAircraftPayloadsCamerasList", "      --> liveview_world_region: { " +
                            "bottom: " + liveview_world_region.getBottom() + ", " +
                            "left: " + liveview_world_region.getLeft() + ", " +
                            "right: " + liveview_world_region.getRight() + ", " +
                            "top: " + liveview_world_region.getTop() + " }"
                        );
                    }

                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> photo_state: " + cam.getPhotoState());

                    List<String> photo_storage_settings = cam.getPhotoStorageSettings();
                    if (photo_storage_settings == null || photo_storage_settings.isEmpty()) {
                        log("INFO", "listenAircraftPayloadsCamerasList", "      --> photo_storage_settings 空或无数据");
                    } else {
                        log("INFO", "listenAircraftPayloadsCamerasList", "      --> photo_storage_settings.size(): " + photo_storage_settings.size());
                        for (int j = 0; j < photo_storage_settings.size(); j++) {
                            log("INFO", "listenAircraftPayloadsCamerasList", "        --> photo_storage_settings[" + j + "]: " + photo_storage_settings.get(j));
                        }
                    }

                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> record_time: " + cam.getRecordTime());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> recording_state: " + cam.getRecordingState());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> remain_photo_num: " + cam.getRemainPhotoNum());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> remain_record_duration: " + cam.getRemainRecordDuration());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> screen_split_enable: " + cam.isScreenSplitEnable());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_calibrate_farthest_focus_value: " + cam.getWideCalibrateFarthestFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_calibrate_nearest_focus_value: " + cam.getWideCalibrateNearestFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_exposure_mode: " + cam.getWideExposureMode());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_exposure_value: " + cam.getWideExposureValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_focus_mode: " + cam.getWideFocusMode());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_focus_state: " + cam.getWideFocusState());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_focus_value: " + cam.getWideFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_iso: " + cam.getWideIso());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_max_focus_value: " + cam.getWideMaxFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_min_focus_value: " + cam.getWideMinFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> wide_shutter_speed: " + cam.getWideShutterSpeed());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_calibrate_farthest_focus_value: " + cam.getZoomCalibrateFarthestFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_calibrate_nearest_focus_value: " + cam.getZoomCalibrateNearestFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_exposure_mode: " + cam.getZoomExposureMode());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_exposure_value: " + cam.getZoomExposureValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_factor: " + cam.getZoomFactor());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_focus_mode: " + cam.getZoomFocusMode());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_focus_state: " + cam.getZoomFocusState());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_focus_value: " + cam.getZoomFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_iso: " + cam.getZoomIso());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_max_focus_value: " + cam.getZoomMaxFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_min_focus_value: " + cam.getZoomMinFocusValue());
                    log("INFO", "listenAircraftPayloadsCamerasList", "      --> zoom_shutter_speed: " + cam.getZoomShutterSpeed());
                    log("INFO", "listenAircraftPayloadsCamerasList", " ");
                }
            }
        });


        // 遥控器失控动作(rc_lost_action);
        // AircraftRcLostAction;
        drone.getDroneOsdModule().listenAircraftRcLostAction(new FlightHubCallback.AircraftRcLostActionCallback() {
            @Override
            public void onResult(AircraftRcLostAction action) {
                log("INFO", "listenAircraftRcLostAction", "rc_lost_action: " + action.getRcLostAction());
                log("INFO", "listenAircraftRcLostAction", " ");
            }
        });


        // 返航信息(返航模式, 返航高度);
        // AircraftRthInfo;
        drone.getDroneOsdModule().listenAircraftRthInfo(new FlightHubCallback.AircraftRthInfoCallback() {
            @Override
            public void onResult(AircraftRthInfo rth_info) {
                log("INFO", "listenAircraftRthInfo", "rth_mode: " + rth_info.getRthMode() + ", 返航高度模式, {0: 智能高度, 1: 设定高度}");
                log("INFO", "listenAircraftRthInfo", "rth_altitude: " + rth_info.getRthAltitude() + ", 返航高度(单位: m), 参考值: [20, 1500]");
                log("INFO", "listenAircraftRthInfo", " ");
            }
        });


        // 控制权限云控制状态监听
        drone.getDroneOsdModule().listenControlRightsCloudControlStatus(new FlightHubCallback.ControlRightsCloudControlStatusCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.ControlRightsCloudControlStatus status) {
                log("INFO", "listenControlRightsCloudControlStatus", "云控制状态: sn=" + status.getSn() + ", reason=" + status.getReason());
                log("INFO", "listenControlRightsCloudControlStatus", " ");
            }
        });


        // 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
        // AircraftHomeGeographicalInfo;
        drone.getDroneOsdModule().listenAircraftHomeGeographicalInfo(new FlightHubCallback.AircraftHomeGeographicalInfoCallback() {
            @Override
            public void onResult(AircraftHomeGeographicalInfo info) {
                log("INFO", "listenAircraftHomeGeographicalInfo", "Home点地理坐标信息: ");
                log("INFO", "listenAircraftHomeGeographicalInfo", "    --> home_longitude: " + info.getHomeLongitude());
                log("INFO", "listenAircraftHomeGeographicalInfo", "    --> home_latitude: " + info.getHomeLatitude());
                log("INFO", "listenAircraftHomeGeographicalInfo", " ");
            }
        });


        // 返航高度模式当前值(current_rth_mode);
        // AircraftCurrentRthMode;
        drone.getDroneOsdModule().listenAircraftCurrentRthMode(new FlightHubCallback.AircraftCurrentRthModeCallback() { 
            @Override
            public void onResult(AircraftCurrentRthMode mode) {
                log("INFO", "listenAircraftCurrentRthMode", "当前返航模式: " + mode.getCurrentRthMode());
                log("INFO", "listenAircraftCurrentRthMode", " ");
            }
        });


        // 控制权限设备控制状态监听
        drone.getDroneOsdModule().listenControlRightsDeviceControlStatus(new FlightHubCallback.ControlRightsDeviceControlStatusCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.ControlRightsDeviceControlStatus status) {
                log("INFO", "listenControlRightsDeviceControlStatus", "设备控制状态: sn=" + status.getSn() + ", reason=" + status.getReason());
            }
        });
        
        // 飞行任务媒体文件显示可用监听
        drone.getDroneOsdModule().listenFlightTasksMediaFileDisplayAvailable(new FlightHubCallback.FlightTasksMediaFileDisplayAvailableCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightTasksMediaFileDisplayAvailable available) {
                log("INFO", "listenFlightTasksMediaFileDisplayAvailable", "飞行任务媒体文件显示可用: uuid=" + available.getUuid());
            }
        });
        
        log("INFO", "handleDroneOsd", "OSD监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    private static void handleDockOsd() {
        printSeparator("监听机场 OSD");

        String dockSn = getDockDeviceSn(selectedDevice);
        if (dockSn == null || dockSn.isEmpty()) {
            log("ERROR", "handleDockOsd", "当前拓扑没有机场(dock/airport)，无法监听机场 OSD");
            waitForKeyPress();
            return;
        }
        dock = getOrCreateDock(dockSn);
        if (dock == null) {
            log("ERROR", "handleDockOsd", "获取机场实例失败: " + dockSn);
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockOsd", "开始监听机场OSD信息, dockSn=" + dockSn);

        // 机场 HMS data 监听;
        dock.getDeviceHMSModule().listenDeviceHMS(new FlightHubCallback.DeviceHmsCallback() {
            @Override
            public void onResult(HmsData hmsData) {
                List<HmsDataItem> hmsDataItemVec = hmsData.getHmsDataItems();
                log("INFO", "listenDockHMS", "收到HMS数据, hmsData size: " + hmsDataItemVec.size());

                DataProcessor printHmsDataBasics = (prefix, hmsDataBasics) -> {
                    if (hmsDataBasics == null) {
                        log("INFO", "listenDockHMS", prefix + "    -- basics: null object.");
                    } else {
                        log("INFO", "listenDockHMS", prefix + "    -- basics: ");
                        log("INFO", "listenDockHMS", prefix + "      --- level: " + hmsDataBasics.getLevel() + ", 告警等级, {0: 通知, 1: 提醒, 2: 警告}");
                        log("INFO", "listenDockHMS", prefix + "      --- module_id: " + hmsDataBasics.getModuleId() + ", 事件模块, {0: 飞行任务, 1: 设备管理, 2 : 媒体, 3: hms}");
                        log("INFO", "listenDockHMS", prefix + "      --- code: " + hmsDataBasics.getCode());
                        log("INFO", "listenDockHMS", prefix + "      --- device_type: " + hmsDataBasics.getDeviceType());
                        log("INFO", "listenDockHMS", prefix + "      --- domain_type: " + hmsDataBasics.getDomainType());
                        log("INFO", "listenDockHMS", prefix + "      --- in_the_sky: " + hmsDataBasics.getInTheSky());
                        log("INFO", "listenDockHMS", prefix + "      --- imminent: " + hmsDataBasics.getImminent() +", 是否为及时性的, {0: 否, 1: 是}");
                        log("INFO", "listenDockHMS", prefix + "      --- status_key: " + hmsDataBasics.getStatusKey());
                        log("INFO", "listenDockHMS", prefix + "      --- joint_key: " + hmsDataBasics.getJointKey());
                        log("INFO", "listenDockHMS", prefix + "      --- id: " + hmsDataBasics.getId() +", 每个设备上报事件里hms的唯一id");
                        log("INFO", "listenDockHMS", prefix + "      --- parent_id: " + hmsDataBasics.getParentId() +", 每个设备上报事件里hms的唯一父id");

                        HmsArgs hmsArgs = hmsDataBasics.getArgs();
                        log("INFO", "listenDockHMS", prefix + "      --- args: ");
                        log("INFO", "listenDockHMS", prefix + "          --> component_index: " + hmsArgs.getComponentIndex());
                        log("INFO", "listenDockHMS", prefix + "          --> sensor_index: " + hmsArgs.getSensorIndex());
                        log("INFO", "listenDockHMS", prefix + "          --> device_id: " + hmsArgs.getDeviceId());
                        log("INFO", "listenDockHMS", prefix + "          --> sender_host_id: " + hmsArgs.getSenderHostId());
                        log("INFO", "listenDockHMS", prefix + "          --> ignore: " + hmsArgs.getIgnore() +", 告警是否忽略, {0:不忽略, 1: 忽略}");
                        log("INFO", "listenDockHMS", prefix + "          --> alarm_info: " + hmsArgs.getAlarmInfo());
                        log("INFO", "listenDockHMS", prefix + "          --> create_timestamp: " + hmsArgs.getCreateTimestamp() + ", 告警产生的UTC时间(单位秒)");
                        log("INFO", "listenDockHMS", prefix + "          --> display_scene: " + hmsArgs.getDisplayScene() + ", 告警是否忽略, {0: 正式错误码, 1: 调试错误码, 只用于排障}");
                    }
                };

                log("INFO", "listenDockHMS()", "START -------------------------------------------------------------------------------------------------------------------------------");
                for (int i = 0; i < hmsDataItemVec.size(); i++) {
                    HmsDataItem hmsDataItem = hmsDataItemVec.get(i);
                    if (hmsDataItem == null) {
                        log("INFO", "listenDockHMS", "  - HMS data_item[ + " + i + "]: null object.");
                    } else {
                        log("INFO", "listenDockHMS", "  - HMS data_item[ + " + i + "]: ");
                        HmsDataBasics hmsDataBasics = hmsDataItem.getBasics();
                        printHmsDataBasics.print("", hmsDataBasics);

                        List<HmsDataBasics> subHmsDataList = hmsDataItem.getSubHmsData();
                        if (subHmsDataList == null || subHmsDataList.isEmpty()) {
                            log("INFO", "listenDockHMS", "    ==> sub_hms_data: 空或无数据.");
                        } else {
                            long size = subHmsDataList.size();
                            log("INFO", "listenDockHMS", "    ==> sub_hms_data size: " + size);
                            for (int j = 0; j < size; j++) {
                                printHmsDataBasics.print("    ", subHmsDataList.get(j));
                            }
                        }
                    }
                    log("INFO", "listenDockHMS", " ");
                }

                log("INFO", "listenDockHMS()", "END -------------------------------------------------------------------------------------------------------------------------------");
            }
        });


        dock.getDockControlModule().listenDockAirConditionerStatusInfo(new FlightHubCallback.DockAirConditionerStatusInfoCallback() {
            @Override
            public void onResult(DockAirConditionerStatusInfo statusInfo) {
                log("INFO", "listenDockAirConditionerStatusInfo", "Received dock air conditioner status info message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockAirConditionerStatusInfo", "  - Air Conditioner State (空调工作状态): "
                        + statusInfo.getAirConditionerState()
                        + " (code=" + statusInfo.getAirConditionerState().getValue() + ")");
                log("INFO", "listenDockAirConditionerStatusInfo", "  - Switch Time (切换时间): " + statusInfo.getSwitchTime() + " s");
                log("INFO", "listenDockAirConditionerStatusInfo", " ");
            }
        });

        dock.getDockControlModule().listenDockSupplementLightState(new FlightHubCallback.DockSupplementLightStateCallback() {
            @Override
            public void onResult(DockSupplementLightState lightState) {
                log("INFO", "listenDockSupplementLightState", "Received dock supplement light state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockSupplementLightState", "  - Supplement Light State (补光灯状态): " + lightState.getSupplementLightState());
                String statusDesc = lightState.getSupplementLightState() == 1 ? "开启" : "关闭";
                log("INFO", "listenDockSupplementLightState", "  - Status Description: " + statusDesc);
                log("INFO", "listenDockSupplementLightState", " ");
            }
        });

        // 监听机场舱盖状态;
        dock.getDockOsdModule().listenDockCoverState(new FlightHubCallback.DockCoverStateCallback() {
            @Override
            public void onResult(DockCoverState coverState) {
                log("INFO", "listenDockCoverState", "Received dock cover state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockCoverState", "  - Cover State (舱盖状态): " + coverState.getCoverState());
                String statusDesc = coverState.getCoverState() == 1 ? "开启" : "关闭";
                log("INFO", "listenDockCoverState", "  - Status Description: " + statusDesc);
                log("INFO", "listenDockCoverState", " ");
            }
        });

        // 监听机场媒体文件数量(media_file_detail);
        dock.getDockOsdModule().listenDockMediaFileDetail(new FlightHubCallback.DockMediaFileDetailCallback() {
            @Override
            public void onResult(DockMediaFileDetail mediaFileDetail) {
                log("INFO", "listenDockMediaFileDetail", "Received dock media file detail message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockMediaFileDetail", "  - Media File detail, remain_upload(待上传数量): " + mediaFileDetail.getRemainUpload());
                log("INFO", "listenDockMediaFileDetail", " ");
            }
        });


        // 机场图传质量(wireless_link);
        dock.getDockOsdModule().listenDockWirelessLink(new FlightHubCallback.DockWirelessLinkCallback() { 
            @Override
            public void onResult(DockWirelessLink dockWirelessLink) {
                log("INFO", "listenDockWirelessLink", "Received dock wireless link message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockWirelessLink", "  - Wireless Link (机场图传质量): ");
                log("INFO", "listenDockWirelessLink", "    -- dongle_num , {0,1,2}, 飞机上 dongle 数量): " + dockWirelessLink.getDongleNum());
                log("INFO", "listenDockWirelessLink", "    -- link_state_4g (4g 链路连接状态, 0-断开, 1-连接): " + dockWirelessLink.getLinkState4g());
                log("INFO", "listenDockWirelessLink", "    -- sdr_link_state (sdr 链路连接状态, 0-断开, 1-连接): " + dockWirelessLink.getSdrLinkState());
                log("INFO", "listenDockWirelessLink", "    -- link_workmode (链路工作模式, 0-sdr only, 1-4G融合): " + dockWirelessLink.getLinkWorkmode());
                log("INFO", "listenDockWirelessLink", "    -- sdr_quality (标识sdr信号强弱, 0~5 一共六档): " + dockWirelessLink.getSdrQuality());
                log("INFO", "listenDockWirelessLink", "    -- quality_4g (标识总体4G信号强弱, 0~5 一共六档，310UI上使用此值, 对应v1协议的 lk_quality): " + dockWirelessLink.getQuality4g());
                log("INFO", "listenDockWirelessLink", "    -- uav_quality_4g (标识飞机端4G信号强弱,0~5 一共六档): " + dockWirelessLink.getUavQuality4g());
                log("INFO", "listenDockWirelessLink", "    -- gnd_quality_4g (标识地面端4G信号强弱,0~5 一共六档): " + dockWirelessLink.getGndQuality4g());
                log("INFO", "listenDockWirelessLink", "    -- sdr_freq_band (sdr 频段): " + dockWirelessLink.getSdrFreqBand());
                log("INFO", "listenDockWirelessLink", "    -- freq_band_4g (4g 频段): " + dockWirelessLink.getFreqBand4g());
                log("INFO", "listenDockWirelessLink", " ");
            }
        });


        // 机场作业次数(job_number);
        dock.getDockOsdModule().listenDockJobNumber(new FlightHubCallback.DockJobNumberCallback() { 
            @Override
            public void onResult(DockJobNumber dockJobNumber) {
                log("INFO", "listenDockJobNumber", "Received dock job number for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockJobNumber", "  - Job Number(机场作业次数): ");
                log("INFO", "listenDockJobNumber", "    -- job_number: " + dockJobNumber.getJobNumber());
                log("INFO", "listenDockJobNumber", " ");
            }
        });


        // 机场：DockPowerState;
        dock.getDockOsdModule().listenDockPowerState(new FlightHubCallback.DockPowerStateCallback() {
            @Override
            public void onResult(DockPowerState dockPowerState) {
                log("INFO", "listenDockPowerState", "Received dock power state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockPowerState", "  - electric_supply_voltage(市电电压, V): " + dockPowerState.getElectricSupplyVoltage());
                log("INFO", "listenDockPowerState", "  - working_voltage(工作电压, mV): " + dockPowerState.getWorkingVoltage());
                log("INFO", "listenDockPowerState", "  - working_current(工作电流, mA): " + dockPowerState.getWorkingCurrent());
                log("INFO", "listenDockPowerState", " ");
            }
        });


        // 监听机场：DockStorageState;
        dock.getDockOsdModule().listenDockStorageState(new FlightHubCallback.DockStorageStateCallback() {
            @Override
            public void onResult(DockStorageState dockStorageState) {
                log("INFO", "listenDockStorageState", "Received dock storage state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockStorageState", "  - total: " + dockStorageState.getTotal() + " KB.");
                log("INFO", "listenDockStorageState", "  - used: " + dockStorageState.getUsed() + " KB.");
                log("INFO", "listenDockStorageState", " ");
            }
        });


        // 监听机场：DockFirstPowerOnTime;
        dock.getDockOsdModule().listenDockFirstPowerOnTime(new FlightHubCallback.DockFirstPowerOnTimeCallback() {
            @Override
            public void onResult(DockFirstPowerOnTime firstPowerOnTime) {
                log("INFO", "listenDockFirstPowerOnTime", "Received dock first power on time message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockFirstPowerOnTime", "  - First Power On Time (机场首次上电时间): " + firstPowerOnTime.getFirstPowerOn());
                log("INFO", "listenDockFirstPowerOnTime", " ");
            }
        });


        // 监听机场：DockAccTime;
        dock.getDockOsdModule().listenDockAccumulatedRunningTime(new FlightHubCallback.DockAccTimeCallback() {
            @Override
            public void onResult(DockAccTime dockAccTime) {
                log("INFO", "listenDockAccumulatedRunningTime", "Received dock accumulated running time message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockAccumulatedRunningTime", "  - Accumulated Running Time (机场累计运行时间): " + dockAccTime.getAccTime());
                log("INFO", "listenDockAccumulatedRunningTime", " ");
            }
        });


        // 监听机场：DockAlternateLandPoint;
        dock.getDockOsdModule().listenDockAlternateLandPoint(new FlightHubCallback.DockAlternateLandPointCallback() {
            @Override
            public void onResult(DockAlternateLandPoint landPoint) {
                log("INFO", "listenDockAlternateLandPoint", "Received dock alternate land point message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockAlternateLandPoint", "  - is_configured(备降点是否设置, 0:未设置, 1:已设置): " + landPoint.getIsConfigured());
                log("INFO", "listenDockAlternateLandPoint", "  - longitude(经度): " + landPoint.getLongitude());
                log("INFO", "listenDockAlternateLandPoint", "  - latitude(纬度): " + landPoint.getLatitude());
                log("INFO", "listenDockAlternateLandPoint", "  - safe_land_height(安全高度(备降转移高)): " + landPoint.getSafeLandHeight());
                log("INFO", "listenDockAlternateLandPoint", "  - height(高度): " + landPoint.getHeight());
                log("INFO", "listenDockAlternateLandPoint", " ");
            }
        });


        // 监听机场：DockPositionState;
        dock.getDockOsdModule().listenDockPositionState(new FlightHubCallback.DockPositionStateCallback() {
            @Override
            public void onResult(DockPositionState dockPositionState) {
                log("INFO", "listenDockPositionState", "Received dock position state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockPositionState", "  -- is_calibration(是否标定, 0:未标定,1:已标定): " + dockPositionState.getIsCalibration());
                log("INFO", "listenDockPositionState", "  -- is_fixed(是否收敛, 0:未开始, 1:收敛中, 2:收敛成功, 3: 收敛失败): " + dockPositionState.getIsFixed());
                log("INFO", "listenDockPositionState", "  -- quality(搜星档位, 1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档): " + dockPositionState.getQuality());
                log("INFO", "listenDockPositionState", "  -- gps_number(GPS 搜星数量): " + dockPositionState.getGpsNumber());
                log("INFO", "listenDockPositionState", "  -- rtk_number(RTK 搜星数量): " + dockPositionState.getRtkNumber());
                log("INFO", "listenDockPositionState", " ");
            }
        });


        // 监听机场：DockActivationTime;
        dock.getDockOsdModule().listenDockActivationTime(new FlightHubCallback.DockActivationTimeCallback() {
            @Override
            public void onResult(DockActivationTime activationTime) {
                log("INFO", "listenDockActivationTime", "Received dock activation time message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockActivationTime", "  - Activation Time (机场激活时间): " + activationTime.getActivationTime() + " s");
                log("INFO", "listenDockActivationTime", " ");
            }
        });


        // 监听机场：电池(存储)模式(battery_store_mode);
        dock.getDockOsdModule().listenDockBatteryStoreMode(new FlightHubCallback.DockBatteryStoreModeCallback() {
            @Override
            public void onResult(DockBatteryStoreMode batteryStoreMode) {
                log("INFO", "listenDockBatteryStoreMode", "Received dock battery store mode message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockBatteryStoreMode", "  - Battery Store Mode (电池存储模式): " + batteryStoreMode.getBatteryStoreMode() +
                    ", {0: 默认无效值, 1: 电池计划存储策略, 2: 电池应急存储策略}");
                log("INFO", "listenDockBatteryStoreMode", " ");
            }
        });


        // 监听机场：机场声光报警状态(alarm_state);
        dock.getDockOsdModule().listenDockAlarmState(new FlightHubCallback.DockAlarmStateCallback() {
            @Override
            public void onResult(DockAlarmState alarmState) {
                log("INFO", "listenDockAlarmState", "Received dock alarm state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockAlarmState", "  - DockAlarmState(0: 声光报警关闭, 1: 声光报警开启): { alarm_state: " + alarmState.getAlarmState() + "}");
                log("INFO", "listenDockAlarmState", " ");
            }
        });


        // 飞行器电池保养信息(drone_battery_maintenance_info);
        // DockDroneBatteryMaintenanceInfo;
        dock.getDockOsdModule().listenDockDroneBatteryMaintenanceInfo(new FlightHubCallback.DockDroneBatteryMaintenanceInfoCallback() {
            @Override
            public void onResult(DockDroneBatteryMaintenanceInfo info) {
                log("INFO", "listenDockDroneBatteryMaintenanceInfo", "Received dock drone battery maintenance info message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockDroneBatteryMaintenanceInfo", "  - maintenance_state: " + info.getMaintenanceState() + ", 保养状态, { 0: 无需保养, 1: 待保养, 2: 正在保养 }");
                log("INFO", "listenDockDroneBatteryMaintenanceInfo", "  - maintenance_time_left: " + info.getMaintenanceTimeLeft() + " hour(s)");
                log("INFO", "listenDockDroneBatteryMaintenanceInfo", "  - heat_state: " + info.getHeatState() + ", 电池加热保温状态, {0: 电池未开启加热或保温, 1: 电池在加热中, 2: 电池在保温中}");

                java.util.List<BatteryBasicInfo> batteries = info.getBatteries();
                if (batteries == null || batteries.isEmpty()) {
                    log("INFO", "listenDockDroneBatteryMaintenanceInfo", "  - batteries: empty or null");
                } else {
                    log("INFO", "listenDockDroneBatteryMaintenanceInfo", "  - batteries: ");
                    log("INFO", "listenDockDroneBatteryMaintenanceInfo", "      - count: " + batteries.size() + " elements");

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < batteries.size(); i++) {
                        BatteryBasicInfo batteryBasicInfo = batteries.get(i);
                        sb.setLength(0);
                        log("INFO", "listenDockDroneBatteryMaintenanceInfo", "      - BatteryBasicInfo[" + i + "]: ");
                        sb.append("{ ");
                        sb.append("index: " + batteryBasicInfo.getIndex() + ", ");
                        sb.append("capacity_percent: " + batteryBasicInfo.getCapacityPercent() + "%, ");
                        sb.append("voltage: " + batteryBasicInfo.getVoltage() + ", ");
                        sb.append("temperature: " + batteryBasicInfo.getTemperature() + "°C (Celsius)");
                        sb.append(" }");
                        log("INFO", "listenDockDroneBatteryMaintenanceInfo", "          --> " + sb.toString());
                    }
                }

                log("INFO", "listenDockDroneBatteryMaintenanceInfo", " ");
            }
        });


        // 紧急停止按钮状态(emergency_stop_state);
        // DockEmergencyStopState;
        dock.getDockOsdModule().listenDockEmergencyStopState(new FlightHubCallback.DockEmergencyStopStateCallback() {
            @Override
            public void onResult(DockEmergencyStopState state) {
                log("INFO", "listenDockEmergencyStopState", "Received dock emergency stop state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockEmergencyStopState", "  - emergency_stop_state: " + state.getEmergencyStopState() + ", {紧急停止按钮状态, 0: 关闭, 1: 开启;}");
                log("INFO", "listenDockEmergencyStopState", " ");
            }
        });


        // 监听机场 Silent Mode;
        dock.getDockOsdModule().listenDockSilentMode(new FlightHubCallback.DockSilentModeCallback() {
            @Override
            public void onResult(DockSilentMode silentMode) {
                log("INFO", "listenDockSilentMode", "Received dock silent mode message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockSilentMode", "  - Silent Mode (静音模式): " + silentMode.getSilentMode() +
                    ", {0: 静音模式关闭, 1: 静音模式开启};");
                log("INFO", "listenDockSilentMode", " ");
            }
        });


        // 监听机场 User Experience Improvement;
        dock.getDockOsdModule().listenDockUserExperienceImprovement(new FlightHubCallback.DockUserExperienceImprovementCallback() {
            @Override
            public void onResult(DockUserExperienceImprovement userExperienceImprovement) {
                log("INFO", "listenDockUserExperienceImprovement", "Received dock user experience improvement message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockUserExperienceImprovement", "  - User Experience Improvement (用户体验改进): " + userExperienceImprovement.getUserExperienceImprovement() +
                    ", { 0:初始状态, 1:拒绝加入用户体验改善计划, 2:同意加入用户体验改善计划; }");
                log("INFO", "listenDockUserExperienceImprovement", " ");
            }
        });


        // 监听机场 Drc State;
        dock.getDockOsdModule().listenDockDrcState(new FlightHubCallback.DockDrcStateCallback() {
            @Override
            public void onResult(DockDrcState drcState) {
                log("INFO", "listenDockDrcState", "Received dock drc state message for device " + getDockDeviceSn(selectedDevice) + ": ");
                log("INFO", "listenDockDrcState", "  - Drc State (Drc状态): " + drcState.getDrcState() + ", { 0: 未连接, 1: 连接中, 2: 已连接 };");
                log("INFO", "listenDockDrcState", " ");
            }
        });


        // 监听机场 Putter State;
        dock.getDockOsdModule().listenDockPutterState(new FlightHubCallback.DockPutterStateCallback() {
            @Override
            public void onResult(DockPutterState putterState) {
                log("INFO", "listenDockPutterState", "Received dock putter state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockPutterState", "  - Putter State (Putter状态): " + putterState.getPutterState());
                log("INFO", "listenDockPutterState", " ");
            }
        });


        // 监听机场 Relative Alternate Land Point;
        dock.getDockOsdModule().listenDockRelativeAlternateLandPoint(new FlightHubCallback.DockRelativeAlternateLandPointCallback() {
            @Override
            public void onResult(DockRelativeAlternateLandPoint relativeAlternateLandPoint) {
                log("INFO", "listenDockRelativeAlternateLandPoint", "Received dock relative alternate land point message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockRelativeAlternateLandPoint", "  - Relative Alternate Land Point (相对自动降落点): ");
                log("INFO", "listenDockRelativeAlternateLandPoint", "      --> longitude: " + relativeAlternateLandPoint.getLongitude());
                log("INFO", "listenDockRelativeAlternateLandPoint", "      --> latitude: " + relativeAlternateLandPoint.getLatitude());
                log("INFO", "listenDockRelativeAlternateLandPoint", "      --> safe_land_height: " + relativeAlternateLandPoint.getSafeLandHeight());
                log("INFO", "listenDockRelativeAlternateLandPoint", "      --> status: " + relativeAlternateLandPoint.getStatus() +
                    ", { 0: 默认, 1: 机场未设置相对备降点, 2: 机场未进入基站模式, 3: 机场朝向未收敛 };");
 
                log("INFO", "listenDockRelativeAlternateLandPoint", " ");
            }
        });


        // 监听机场 自收敛坐标(self_converge_coordinate);
        dock.getDockOsdModule().listenDockSelfConvergeCoordinate(new FlightHubCallback.DockSelfConvergeCoordinateCallback() {
            @Override
            public void onResult(DockSelfConvergeCoordinate selfConvergeCoordinate) {
                log("INFO", "listenDockSelfConvergeCoordinate", "Received dock self converge coordinate message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockSelfConvergeCoordinate", "  - Self Converge Coordinate (自收敛坐标): ");
                log("INFO", "listenDockSelfConvergeCoordinate", "      --> height: " + selfConvergeCoordinate.getHeight());
                log("INFO", "listenDockSelfConvergeCoordinate", "      --> longitude: " + selfConvergeCoordinate.getLongitude());
                log("INFO", "listenDockSelfConvergeCoordinate", "      --> latitude: " + selfConvergeCoordinate.getLatitude());
                log("INFO", "listenDockSelfConvergeCoordinate", " ");
            }
        });


        // 机场固件信息;
        // DockFirmwareInfo
        dock.getDockOsdModule().listenDockFirmwareInfo(new FlightHubCallback.DockFirmwareInfoCallback() {
            @Override
            public void onResult(DockFirmwareInfo firmware_info) {
                log("INFO", "listenDockFirmwareInfo", "机场固件信息");
                log("INFO", "listenDockFirmwareInfo", "  --> firmware_version: " + firmware_info.getFirmwareVersion() + ", 固件版本, such as \"00.01.0439\"");
                log("INFO", "listenDockFirmwareInfo", "  --> compatible_status: " + firmware_info.getCompatibleStatus() + ", 固件一致性, {0: 一致, 1: 不一致} ");
                log("INFO", "listenDockFirmwareInfo", "  --> firmware_upgrade_status: " + firmware_info.getFirmwareUpgradeStatus() + ", 固件升级状态, {0: 未升级, 1: 升级中}");
                log("INFO", "listenDockFirmwareInfo", " ");
            }
        });


        // 监听机场 Mode Code;
        dock.getDockOsdModule().listenDockModeCode(new FlightHubCallback.DockModeCodeCallback() {
            @Override
            public void onResult(DockModeCode modeCode) {
                log("INFO", "listenDockModeCode", "Received dock mode code message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockModeCode", "  - Mode Code (机场模式): " + modeCode.getModeCode() +
                    ", {0: 空闲中, 1: 现场调试, 2: 远程调试, 3: 固件升级中, 4: 作业中, 5: 待标定;};");
                log("INFO", "listenDockModeCode", " ");
            }
        });


        // 监听飞行任务步骤码 (flighttask_step_code);
        dock.getDockOsdModule().listenFlightTaskStepCode(new FlightHubCallback.FlightTaskStepCodeCallback() {
            @Override
            public void onResult(FlightTaskStepCode stepCode) {
                log("INFO", "listenFlightTaskStepCode", "Received flight task step code for device "
                    + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenFlightTaskStepCode", "  - Flight Task Step Code (飞行任务步骤码): "
                    + stepCode.getFlighttaskStepCode()
                    + ", {0: 作业准备中, 1: 飞行作业中, 2: 作业后状态恢复, 3: 自定义飞行区更新中, 4: 地形障碍物更新中, 5: 任务空闲, 256: 未知状态};");
                log("INFO", "listenFlightTaskStepCode", " ");
            }
        });


        // 监听机场子设备信息列表(sub_device);
        dock.getDockOsdModule().listenDockSubDevice(new FlightHubCallback.DockSubDeviceInfoListCallback() {
            @Override
            public void onResult(DockSubDeviceInfoList dock_sub_device_info_list) {
                log("INFO", "listenDockSubDevice", "Received dock sub device info list message for device " + getDockDeviceSn(selectedDevice) + ":");
                if (dock_sub_device_info_list == null) {
                    log("INFO", "listenDockSubDevice", "  - Dock sub device info list: null object.");
                } else {
                    java.util.List<DockSubDeviceInfo> items = dock_sub_device_info_list.getSubDeviceInfos();
                    log("INFO", "listenDockSubDevice", "  - Dock sub device info list, size: " + items.size() + ".");
                    for (int i = 0; i < items.size(); i++) {
                        DockSubDeviceInfo subDeviceInfo = items.get(i);
                        log("INFO", "listenDockSubDevice", "  - Dock sub device info:");
                        log("INFO", "listenDockSubDevice", "    - Device SN (设备序列号): " + subDeviceInfo.getDeviceSn());
                        log("INFO", "listenDockSubDevice", "    - Device Model Key (设备型号): " + subDeviceInfo.getDeviceModelKey());
                        log("INFO", "listenDockSubDevice", "    - Device Online Status (设备在线状态): " + subDeviceInfo.getDeviceOnlineStatus());
                        log("INFO", "listenDockSubDevice", "    - Device Paired (设备是否配对): " + subDeviceInfo.getDevicePaired());
                    }
                }
                log("INFO", "listenDockSubDevice", " ");
            }
        });


        // 监听机场网络状态;
        dock.getDockOsdModule().listenDockNetworkState(new FlightHubCallback.DockNetworkStateCallback() {
            @Override
            public void onResult(DockNetworkState networkState) { 
                log("INFO", "listenDockNetworkState", "Received dock network state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockNetworkState", "  - Type (网络类型): " + networkState.getType() + ",  { 网络类型 1: 4G, 2: 以太网 }, 默认无效值: -1(无效值)");
                log("INFO", "listenDockNetworkState", "  - Quality (网络质量): " + networkState.getQuality() + ",  网络质量(当网络类型为4G时才具有网络质量信息)\", { 0: 差, 1: 中, 2: 好 }; 默认无效值: -1(无效值)");
                log("INFO", "listenDockNetworkState", "  - Rate (网络速率): " + networkState.getRate() + " KB/s");
                log("INFO", "listenDockNetworkState", " ");
            }
        });


        // 监听机场无人机充电状态;
        dock.getDockOsdModule().listenDockDroneChargeState(new FlightHubCallback.DockDroneChargeStateCallback() {
            @Override
            public void onResult(DockDroneChargeState chargeState) {
                log("INFO", "listenDockDroneChargeState", "Received dock drone charge state message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockDroneChargeState", "  - State (充电状态): " + chargeState.getState());
                String statusDesc = chargeState.getState() == 1 ? "充电中" : "未充电";
                log("INFO", "listenDockDroneChargeState", "  - Status Description: " + statusDesc);
                log("INFO", "listenDockDroneChargeState", "  - Capacity Percent (电量百分比): " + chargeState.getCapacityPercent() + "%");
                log("INFO", "listenDockDroneChargeState", " ");
            }
        });


        // 监听机场气象信息;
        dock.getDockOsdModule().listenDockMeteorologicalInfo(new FlightHubCallback.DockMeteorologicalInfoCallback() {
            @Override
            public void onResult(DockMeteorologicalInfo meteorologicalInfo) {
                log("INFO", "listenDockMeteorologicalInfo", "Received dock meteorological info message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockMeteorologicalInfo", "  - Temperature (温度): " + meteorologicalInfo.getTemperature() + "℃");
                log("INFO", "listenDockMeteorologicalInfo", "  - Environment Temperature (环境温度): " + meteorologicalInfo.getEnvironmentTemperature() + "℃");
                log("INFO", "listenDockMeteorologicalInfo", "  - Humidity (湿度): " + meteorologicalInfo.getHumidity() + "%");
                log("INFO", "listenDockMeteorologicalInfo", "  - Wind Speed (风速): " + meteorologicalInfo.getWindSpeed());
                log("INFO", "listenDockMeteorologicalInfo", "  - Rainfall (雨量): " + meteorologicalInfo.getRainfall());
                log("INFO", "listenDockMeteorologicalInfo", " ");
            }
        });


        // 监听机场备份电池信息;
        dock.getDockOsdModule().listenDockBackupBatteryInfo(new FlightHubCallback.DockBackupBatteryInfoCallback() {
            @Override
            public void onResult(DockBackupBatteryInfo backupBatteryInfo) {
                log("INFO", "listenDockBackupBatteryInfo", "Received dock backup battery info message for device " + getDockDeviceSn(selectedDevice) + ":");
                log("INFO", "listenDockBackupBatteryInfo", "  - Switch State (开关状态): " + backupBatteryInfo.getSwitchState());
                log("INFO", "listenDockBackupBatteryInfo", "  - Voltage (电压): " + backupBatteryInfo.getVoltage() + "mV");
                log("INFO", "listenDockBackupBatteryInfo", "  - Temperature (温度): " + backupBatteryInfo.getTemperature() + "℃");
                log("INFO", "listenDockBackupBatteryInfo", " ");
            }
        });


        // 监听机场维护状态信息;
        dock.getDockOsdModule().listenDockMaintainStatusInfo(new FlightHubCallback.DockMaintainStatusInfoCallback() {
            @Override
            public void onResult(DockMaintainStatusInfo maintainStatusInfo) {
                log("INFO", "listenDockMaintainStatusInfo", "Received dock maintain status info message for device " + getDockDeviceSn(selectedDevice) + ":");
                java.util.List<DockMaintainStatus> infos = maintainStatusInfo.getMaintainStatusInfos();
                log("INFO", "listenDockMaintainStatusInfo", "  - Maintain Status Info Count: " + infos.size());
                for (int i = 0; i < infos.size(); i++) {
                    DockMaintainStatus maintainStatus = infos.get(i);
                    log("INFO", "listenDockMaintainStatusInfo", "  - Maintain Status [" + i + "]:");
                    log("INFO", "listenDockMaintainStatusInfo", "    - State: " + maintainStatus.getState());
                    log("INFO", "listenDockMaintainStatusInfo", "    - Last Maintain Type: " + maintainStatus.getLastMaintainType());
                    log("INFO", "listenDockMaintainStatusInfo", "    - Last Maintain Time: " + maintainStatus.getLastMaintainTime());
                    log("INFO", "listenDockMaintainStatusInfo", "    - Last Maintain Work Sorties: " + maintainStatus.getLastMaintainWorkSorties());
                    log("INFO", "listenDockMaintainStatusInfo", " ");
                }
            }
        });


        // 机场在线状态监听
        dock.getDeviceStateModule().listenDockOnlineStatus(new FlightHubCallback.DockOnlineStatusCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.DockOnlineStatus status) {
                log("INFO", "listenDockOnlineStatus", "机场在线状态: is_online=" + status.isOnline());
                log("INFO", "listenDockOnlineStatus", " ");
            }
        });


        // 机场位置监听
        dock.getDockOsdModule().listenDockLocation(new FlightHubCallback.DockLocationCallback() {
            @Override
            public void onResult(DockLocation location) {
                log("INFO", "listenDockLocation", "收到机场位置: latitude=" + location.getLatitude() + ", longitude=" + location.getLongitude() + ", height=" + location.getHeight());
                log("INFO", "listenDockLocation", " ");
            }
        });


        // 机场中的无人机监听
        dock.getDockOsdModule().listenDroneInDock(new FlightHubCallback.DroneInDockCallback() {
            @Override
            public void onResult(DroneInDock droneInDock) {
                log("INFO", "listenDroneInDock", "无人机在机场内: drone_in_dock=" + droneInDock.getDroneInDock());
                log("INFO", "listenDroneInDock", " ");
            }
        });

        log("INFO", "handleDockOsd", "OSD监听已启动，按Enter键停止...");
        waitForKeyPress();
    }


    private static void handleWorkspaceOsd() {
        printSeparator("监听工作空间 OSD");
        
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleWorkspaceOsd", "获取工作空间实例失败");
            waitForKeyPress();
            return;
        }
        
        log("INFO", "handleWorkspaceOsd", "开始监听工作空间OSD信息...");
        
        // 工作空间设备变更监听
        workspace.getDeviceManageModule().listenWorkspaceDeviceChanged(new FlightHubCallback.WorkspaceDeviceChangeCallback() {
            @Override
            public void onResult(WorkspaceDeviceChange change) {
                log("INFO", "listenWorkspaceDeviceChanged", "收到工作空间设备变更信息:");
                List<DeviceChange> deviceChanges = change.getDeviceChanges();
                log("INFO", "listenWorkspaceDeviceChanged", "  device_changes count: " + deviceChanges.size());

                for (int i = 0; i < deviceChanges.size(); i++) {
                    DeviceChange item = deviceChanges.get(i);
                    List<String> changeDevices = item.getChangeDevices();
                    log("INFO", "listenWorkspaceDeviceChanged", "  [" + i + "] change_type: " + item.getChangeType()
                                     + ", change_devices count: " + changeDevices.size());

                    for (int j = 0; j < changeDevices.size(); j++) {
                        log("INFO", "listenWorkspaceDeviceChanged", "    [" + j + "] device: " + changeDevices.get(j));
                    }
                }
            }
        });
        
        log("INFO", "handleWorkspaceOsd", "OSD监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    /**
     * [43] EA340 消息推送补全：按 notify.proto 的 14 个 WS topic 订阅，便于联调观察推送。
     * 子菜单可选全部或单个 topic；Enter 停止并 cancel。
     */
    private static void handleEa340MessagePush() {
        printSeparator("EA340 消息推送补全 (WS)");

        System.out.println("选择要订阅的 topic（可多选，逗号分隔；0=全部）:");
        System.out.println("  0  全部 14 个");
        System.out.println("  1  topic_dock_live_status              (LiveDevice)");
        System.out.println("  2  topic_dock_live_capacity            (LiveDevice)");
        System.out.println("  3  topic_dock_dongle_infos             (DockOsd)");
        System.out.println("  4  topic_dock_air_conditioner_mode     (DockOsd)");
        System.out.println("  5  topic_drone_mode_code_reason        (DroneOsd)");
        System.out.println("  6  topic_drone_dongle_infos            (DroneOsd)");
        System.out.println("  7  topic_drone_flysafe_database_version(DroneOsd)");
        System.out.println("  8  topic_drone_wpmz_version            (DroneOsd)");
        System.out.println("  9  topic_drone_commander_flight_mode   (Takeoff)");
        System.out.println(" 10  topic_drone_current_commander_flight_mode (Takeoff)");
        System.out.println(" 11  topic_drone_commander_mode_lost_action (Takeoff)");
        System.out.println(" 12  topic_drone_offline_map_enable      (SafetyService, 需 drone_sn)");
        System.out.println(" 13  topic_drone_psdk_ui_resource        (PsdkControl)");
        System.out.println(" 14  topic_drone_camera_watermark_settings (CameraControl, Push 类型)");
        System.out.print("请输入(例: 0 或 1,3,14，q 取消): ");

        final java.util.Set<Integer> selected = new java.util.LinkedHashSet<Integer>();
        try {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || "q".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                log("INFO", "handleEa340MessagePush", "已取消");
                return;
            }
            if ("0".equals(line)) {
                for (int i = 1; i <= 14; i++) {
                    selected.add(i);
                }
            } else {
                String[] parts = line.split("[,\\s]+");
                for (String p : parts) {
                    if (p.isEmpty()) {
                        continue;
                    }
                    int n = Integer.parseInt(p);
                    if (n >= 1 && n <= 14) {
                        selected.add(n);
                    } else if (n == 0) {
                        selected.clear();
                        for (int i = 1; i <= 14; i++) {
                            selected.add(i);
                        }
                        break;
                    } else {
                        log("WARN", "handleEa340MessagePush", "忽略非法选项: " + n);
                    }
                }
            }
        } catch (Exception e) {
            log("ERROR", "handleEa340MessagePush", "解析选项失败: " + e.getMessage());
            waitForKeyPress();
            return;
        }
        if (selected.isEmpty()) {
            log("WARN", "handleEa340MessagePush", "未选择任何 topic");
            waitForKeyPress();
            return;
        }

        final boolean needDock = selected.contains(1) || selected.contains(2)
                || selected.contains(3) || selected.contains(4);
        final boolean needDrone = selected.contains(5) || selected.contains(6)
                || selected.contains(7) || selected.contains(8)
                || selected.contains(9) || selected.contains(10) || selected.contains(11)
                || selected.contains(12) || selected.contains(13) || selected.contains(14);

        if (needDock) {
            dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
            if (dock == null) {
                log("ERROR", "handleEa340MessagePush", "获取机场实例失败");
                waitForKeyPress();
                return;
            }
        }
        if (needDrone) {
            if (!hasSubDevice(selectedDevice)) {
                log("WARN", "handleEa340MessagePush", "当前拓扑无无人机，飞机侧 topic 将跳过");
            } else {
                drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
                if (drone == null) {
                    log("ERROR", "handleEa340MessagePush", "获取无人机实例失败");
                    waitForKeyPress();
                    return;
                }
            }
        }

        workspace = FlightHubSDK.getWorkspaceInstance();
        final String droneSn = hasSubDevice(selectedDevice) ? getSubDeviceSn(selectedDevice) : "";
        final java.util.List<Runnable> cancelActions = new ArrayList<Runnable>();

        if (selected.contains(1) && dock != null) {
            final int id = dock.getLiveDeviceModule().listenDockLiveStatus(msg ->
                    log("INFO", "EA340/live_status", String.valueOf(msg)));
            cancelActions.add(() -> dock.getLiveDeviceModule().cancelListenDockLiveStatus(id));
            log("INFO", "handleEa340MessagePush", "已订阅 live_status, listenId=" + id);
        }
        if (selected.contains(2) && dock != null) {
            final int id = dock.getLiveDeviceModule().listenDockLiveCapacity(msg ->
                    log("INFO", "EA340/live_capacity", String.valueOf(msg)));
            cancelActions.add(() -> dock.getLiveDeviceModule().cancelListenDockLiveCapacity(id));
            log("INFO", "handleEa340MessagePush", "已订阅 live_capacity, listenId=" + id);
        }
        if (selected.contains(3) && dock != null) {
            final int id = dock.getDockOsdModule().listenDockDongleInfos(msg ->
                    log("INFO", "EA340/dock_dongle", String.valueOf(msg)));
            cancelActions.add(() -> dock.getDockOsdModule().cancelListenDockDongleInfos(id));
            log("INFO", "handleEa340MessagePush", "已订阅 dock_dongle_infos, listenId=" + id);
        }
        if (selected.contains(4) && dock != null) {
            final int id = dock.getDockOsdModule().listenDockAirConditionerMode(msg ->
                    log("INFO", "EA340/ac_mode", String.valueOf(msg)));
            cancelActions.add(() -> dock.getDockOsdModule().cancelListenDockAirConditionerMode(id));
            log("INFO", "handleEa340MessagePush", "已订阅 air_conditioner_mode, listenId=" + id);
        }

        if (drone != null) {
            if (selected.contains(5)) {
                final int id = drone.getDroneOsdModule().listenDroneModeCodeReason(msg ->
                        log("INFO", "EA340/mode_code_reason", String.valueOf(msg)));
                cancelActions.add(() -> drone.getDroneOsdModule().cancelListenDroneModeCodeReason(id));
                log("INFO", "handleEa340MessagePush", "已订阅 mode_code_reason, listenId=" + id);
            }
            if (selected.contains(6)) {
                final int id = drone.getDroneOsdModule().listenDroneDongleInfos(msg ->
                        log("INFO", "EA340/drone_dongle", String.valueOf(msg)));
                cancelActions.add(() -> drone.getDroneOsdModule().cancelListenDroneDongleInfos(id));
                log("INFO", "handleEa340MessagePush", "已订阅 drone_dongle_infos, listenId=" + id);
            }
            if (selected.contains(7)) {
                final int id = drone.getDroneOsdModule().listenDroneFlysafeDatabaseVersion(msg ->
                        log("INFO", "EA340/flysafe_db", String.valueOf(msg)));
                cancelActions.add(() -> drone.getDroneOsdModule().cancelListenDroneFlysafeDatabaseVersion(id));
                log("INFO", "handleEa340MessagePush", "已订阅 flysafe_database_version, listenId=" + id);
            }
            if (selected.contains(8)) {
                final int id = drone.getDroneOsdModule().listenDroneWpmzVersion(msg ->
                        log("INFO", "EA340/wpmz", String.valueOf(msg)));
                cancelActions.add(() -> drone.getDroneOsdModule().cancelListenDroneWpmzVersion(id));
                log("INFO", "handleEa340MessagePush", "已订阅 wpmz_version, listenId=" + id);
            }
            if (selected.contains(9)) {
                final int id = drone.getTakeoffModule().listenDroneCommanderFlightMode(msg ->
                        log("INFO", "EA340/commander_mode", String.valueOf(msg)));
                cancelActions.add(() -> drone.getTakeoffModule().cancelListenDroneCommanderFlightMode(id));
                log("INFO", "handleEa340MessagePush", "已订阅 commander_flight_mode, listenId=" + id);
            }
            if (selected.contains(10)) {
                final int id = drone.getTakeoffModule().listenDroneCurrentCommanderFlightMode(msg ->
                        log("INFO", "EA340/current_commander", String.valueOf(msg)));
                cancelActions.add(() -> drone.getTakeoffModule().cancelListenDroneCurrentCommanderFlightMode(id));
                log("INFO", "handleEa340MessagePush", "已订阅 current_commander_flight_mode, listenId=" + id);
            }
            if (selected.contains(11)) {
                final int id = drone.getTakeoffModule().listenDroneCommanderModeLostAction(msg ->
                        log("INFO", "EA340/commander_lost", String.valueOf(msg)));
                cancelActions.add(() -> drone.getTakeoffModule().cancelListenDroneCommanderModeLostAction(id));
                log("INFO", "handleEa340MessagePush", "已订阅 commander_mode_lost_action, listenId=" + id);
            }
            if (selected.contains(12)) {
                if (workspace == null) {
                    log("WARN", "handleEa340MessagePush", "Workspace 为空，跳过 offline_map_enable");
                } else {
                    final int id = workspace.getSafetyServiceModule().listenDroneOfflineMapEnable(
                            droneSn, msg -> log("INFO", "EA340/offline_map", String.valueOf(msg)));
                    cancelActions.add(() -> workspace.getSafetyServiceModule().cancelListenDroneOfflineMapEnable(id));
                    log("INFO", "handleEa340MessagePush",
                            "已订阅 offline_map_enable (drone_sn=" + droneSn + "), listenId=" + id);
                }
            }
            if (selected.contains(13)) {
                final int id = drone.getPsdkControlModule().listenDronePsdkUiResource(msg ->
                        log("INFO", "EA340/psdk_ui", String.valueOf(msg)));
                cancelActions.add(() -> drone.getPsdkControlModule().cancelListenDronePsdkUiResource(id));
                log("INFO", "handleEa340MessagePush", "已订阅 psdk_ui_resource, listenId=" + id);
            }
            if (selected.contains(14)) {
                final int id = drone.getCameraControlModule().listenDroneCameraWatermarkSettings(msg ->
                        log("INFO", "EA340/watermark_push", String.valueOf(msg)));
                cancelActions.add(() -> drone.getCameraControlModule().cancelListenDroneCameraWatermarkSettings(id));
                log("INFO", "handleEa340MessagePush", "已订阅 camera_watermark_settings (Push), listenId=" + id);
            }
        }

        if (cancelActions.isEmpty()) {
            log("WARN", "handleEa340MessagePush", "没有任何订阅被建立（检查设备拓扑 / 选项）");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleEa340MessagePush",
                "已启动 " + cancelActions.size() + " 路监听，等待推送… 按 Enter 停止并 cancel");
        waitForKeyPress();
        for (Runnable cancel : cancelActions) {
            try {
                cancel.run();
            } catch (Exception e) {
                log("WARN", "handleEa340MessagePush", "cancel 异常: " + e.getMessage());
            }
        }
        log("INFO", "handleEa340MessagePush", "已全部 cancel");
    }

    /**
     * 处理飞行到指定点
     */
    private static void handleFlyTo() {
        printSeparator("Flyto");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleFlyTo", "该设备没有无人机，无法执行飞行任务");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleFlyTo", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        // 基准GPS坐标（天空之城附近）
        final double baseLatitude = 0.0;
        final double baseLongitude = 0.0;
        
        // 直接在基准坐标上随机加减小数值（±0.001度，约±100米）
        double randomLat = baseLatitude + (random.nextDouble() - 0.5) * 0.002;
        double randomLon = baseLongitude + (random.nextDouble() - 0.5) * 0.002;
        
        log("INFO", "handleFlyTo", "基准坐标: (" + baseLatitude + ", " + baseLongitude + ")");
        log("INFO", "handleFlyTo", "随机坐标: (" + randomLat + ", " + randomLon + ")");
        
        com.dji.flighthubsdk.bean.FlyToPointParams params = new com.dji.flighthubsdk.bean.FlyToPointParams();
        params.setLatitude(randomLat);
        params.setLongitude(randomLon);
        params.setHeight(180);
        params.setMaxSpeed(10);
        
        FlyToPointModule flyToModule = drone.getFlyToPointModule();
        flyToModule.start(params, new FlightHubCallback.FlyToPointStartCallback() {
            @Override
            public void onResult(FlightHubError error, String flyToId) {
                log("INFO", "handleFlyTo", "handleFlyTo error: " + error + ", fly_to_id: " + flyToId);
            }
        });
        
        flyToModule.subscribeFlyToDroneSelfPlanPath(new FlightHubCallback.FlyToDroneSelfPlanPathCallback() {
            @Override
            public void onResult(FlyToDroneSelfPlanPath planPath) {
                log("INFO", "handleFlyTo", "subscribeFlyToDroneSelfPlanPath, fly_to_id: " + planPath.getFlyToId() + ", path_points: " + planPath.getPlannedPathPoints().size());
                
                // 处理时间戳：从C++传入的是uint64，Java接收为long，需要无符号解析
                long rawTimestamp = planPath.getTimestamp();
                String unsignedTimestamp = Long.toUnsignedString(rawTimestamp); // 使用Java原生方法
                String readableTime = formatTimestamp(rawTimestamp);
                log("INFO", "handleFlyTo FlyToDroneSelfPlanPath", "  - Raw Timestamp: " + rawTimestamp);
                log("INFO", "handleFlyTo FlyToDroneSelfPlanPath", "  - Unsigned Timestamp: " + unsignedTimestamp);
                log("INFO", "handleFlyTo FlyToDroneSelfPlanPath", "  - Readable Time (UTC): " + readableTime);
            }
        });
        
        flyToModule.subscribeFlyToPointProgress(new FlightHubCallback.FlyToPointProgressCallback() {
            @Override
            public void onResult(FlyToPointProgress progress) {
                log("INFO", "handleFlyTo", "subscribeFlyToPointProgress, fly_to_id: " + progress.getFlyToId() + ", status: " + progress.getStatus() + ", waypoint_index: " + progress.getWaypointIndex());
                
                // 处理时间戳：从C++传入的是uint64，Java接收为long，需要无符号解析
                long rawTimestamp = progress.getTimestamp();
                String unsignedTimestamp = Long.toUnsignedString(rawTimestamp); // 使用Java原生方法
                String readableTime = formatTimestamp(rawTimestamp);
                log("INFO", "handleFlyTo FlyToPointProgress", "  - Raw Timestamp: " + rawTimestamp);
                log("INFO", "handleFlyTo FlyToPointProgress", "  - Unsigned Timestamp: " + unsignedTimestamp);
                log("INFO", "handleFlyTo FlyToPointProgress", "  - Readable Time (UTC): " + readableTime);
            }
        });
        
        log("INFO", "handleFlyTo", "飞行任务已启动，按Enter键返回...");
        waitForKeyPress();
    }
    
    /**
     * 处理停止飞行
     */
    private static void handleFlyToStop() {
        printSeparator("Flyto Stop");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleFlyToStop", "该设备没有无人机，无法停止飞行任务");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleFlyToStop", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        FlyToPointModule flyToModule = drone.getFlyToPointModule();
        flyToModule.drcStop(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleFlyToStop", "handleFlyToStop error: " + error);
            }
        });
        
        log("INFO", "handleFlyToStop", "停止飞行任务请求已发送，按Enter键返回...");
        waitForKeyPress();
    }
    
    /**
     * 处理相机状态监听
     */
    private static void handleCameraState() {
        printSeparator("监听相机状态");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleCameraState", "该设备没有无人机，无法监听相机状态");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleCameraState", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        log("INFO", "handleCameraState", "开始监听相机状态...");
        
        drone.getCameraControlModule().listenDrcCameraState(targetPayloadIndex, new FlightHubCallback.CameraStateCallback() {
            @Override
            public void onResult(CameraState cameraState) {
                log("INFO", "handleCameraState", "监听相机状态: " + cameraState.getPayloadIndex() +
                    ", cameraMode(swig)=" + cameraState.getCameraMode() +
                    ", " + cameraState.getRecordTime() + ", " + cameraState.isRecordingState() +
                    ", " + cameraState.getRemainRecordDuration() + ", " + cameraState.isPhotoState() +
                    ", " + cameraState.getRemainPhotoNum() + ", " + cameraState.getIntervalPhotoInterval());
            }
        });
        
        drone.getCameraControlModule().listenDrcLiveviewWorldRegion(targetPayloadIndex, new FlightHubCallback.LiveviewWorldRegionCallback() {
            @Override
            public void onResult(LiveviewWorldRegion worldRegion) {
                CameraLiveviewWorldRegion r = worldRegion.getWorldRegion();
                log("INFO", "handleCameraState", "监听直播范围: " + worldRegion.getPayloadIndex() +
                    ", bottom=" + r.getBottom() + ", left=" + r.getLeft() +
                    ", right=" + r.getRight() + ", top=" + r.getTop());
            }
        });
        
        drone.getCameraControlModule().listenDrcCameraWideLensState(targetPayloadIndex, new FlightHubCallback.CameraWideLensStateCallback() {
            @Override
            public void onResult(CameraWideLensState wideLensState) {
                log("INFO", "handleCameraState", "监听广角镜头状态: " + wideLensState.getPayloadIndex() +
                    ", wideFocusState(swig)=" + wideLensState.getWideFocusState());
            }
        });
        
        drone.getCameraControlModule().listenDrcCameraZoomLensState(targetPayloadIndex, new FlightHubCallback.CameraZoomLensStateCallback() {
            @Override
            public void onResult(CameraZoomLensState zoomLensState) {
                log("INFO", "handleCameraState", "监听变焦镜头状态: " + zoomLensState.getPayloadIndex() +
                    ", zoomFocusState(swig)=" + zoomLensState.getZoomFocusState());
            }
        });
        
        drone.getCameraControlModule().listenDrcCameraMeasureTarget(targetPayloadIndex, new FlightHubCallback.CameraMeasureTargetCallback() {
            @Override
            public void onResult(CameraMeasureTarget measureTarget) {
                log("INFO", "handleCameraState", "监听测量目标(measure_target): " + measureTarget.getPayloadIndex() +
                    ", " + measureTarget.getAltitude() + ", " + measureTarget.getDistance() +
                    ", " + measureTarget.getLatitude() + ", " + measureTarget.getLongitude() +
                    ", laserState(swig)=" + measureTarget.getLaserMeasureState());
            }
        });
        
        drone.getCameraControlModule().listenDrcGimbalState(targetPayloadIndex, new FlightHubCallback.GimbalStateCallback() {
            @Override
            public void onResult(GimbalState gimbalState) {
                log("INFO", "handleCameraState", "监听云台状态: " + gimbalState.getPayloadIndex() +
                    ", " + gimbalState.getYaw() + ", " + gimbalState.getPitch() + ", " + gimbalState.getRoll());
            }
        });
        
        log("INFO", "handleCameraState", "相机状态监听已启动，按Enter键停止...");
        waitForKeyPress();
    }
    
    /**
     * 获取飞行控制权
     */
    private static void handleGetFlightControlAuthority() {
        printSeparator("获取飞行控制权");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleGetFlightControlAuthority", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleGetFlightControlAuthority", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        ControlAuthModule controlAuth = drone.getControlAuthModule();
        if (controlAuth == null) {
            log("ERROR", "handleGetFlightControlAuthority", "获取控制权模块失败");
            waitForKeyPress();
            return;
        }

        controlAuth.acquireFlightControlAuthority(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleGetFlightControlAuthority", "获取飞行控制权 error: " + error);
            }
        });
        
        log("INFO", "handleGetFlightControlAuthority", "获取飞行控制权请求已发送，按Enter键返回...");
        waitForKeyPress();
    }
    
    /**
     * 释放飞行控制权
     */
    private static void handleReleaseFlightControlAuthority() {
        printSeparator("释放飞行控制权");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleReleaseFlightControlAuthority", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleReleaseFlightControlAuthority", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        ControlAuthModule controlAuth = drone.getControlAuthModule();
        if (controlAuth == null) {
            log("ERROR", "handleReleaseFlightControlAuthority", "获取控制权模块失败");
            waitForKeyPress();
            return;
        }

        controlAuth.releaseFlightControlAuthority(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleReleaseFlightControlAuthority", "释放飞行控制权 error: " + error);
            }
        });
        
        log("INFO", "handleReleaseFlightControlAuthority", "释放飞行控制权请求已发送，按Enter键返回...");
        waitForKeyPress();
    }
    
    /**
     * 监听飞行控制权状态
     */
    private static void handleListenFlightControlAuthorityState() {
        printSeparator("监听飞行控制权状态");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleListenFlightControlAuthorityState", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleListenFlightControlAuthorityState", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        ControlAuthModule controlAuth = drone.getControlAuthModule();
        if (controlAuth == null) {
            log("ERROR", "handleListenFlightControlAuthorityState", "获取控制权模块失败");
            waitForKeyPress();
            return;
        }

        controlAuth.listenFlightControlAuthority(new FlightHubCallback.FlightControlAuthorityCallback() {
            @Override
            public void onResult(boolean isGranted) {
                log("INFO", "handleListenFlightControlAuthorityState", "监听飞行控制权状态 is_granted: " + (isGranted ? "true" : "false"));
            }
        });
        
        log("INFO", "handleListenFlightControlAuthorityState", "飞行控制权状态监听已启动，按Enter键停止...");
        waitForKeyPress();
    }
    
    /**
     * 获取负载控制权
     */
    private static void handleGetPayloadControlAuthority() {
        printSeparator("获取负载控制权");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleGetPayloadControlAuthority", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleGetPayloadControlAuthority", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        ControlAuthModule controlAuth = drone.getControlAuthModule();
        if (controlAuth == null) {
            log("ERROR", "handleGetPayloadControlAuthority", "获取控制权模块失败");
            waitForKeyPress();
            return;
        }

        controlAuth.acquirePayloadControlAuthority(targetPayloadIndex, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleGetPayloadControlAuthority", "获取负载控制权 error: " + error);
            }
        });
        
        log("INFO", "handleGetPayloadControlAuthority", "获取负载控制权请求已发送，按Enter键返回...");
        waitForKeyPress();
    }
    
    /**
     * 释放负载控制权
     */
    private static void handleReleasePayloadControlAuthority() {
        printSeparator("释放负载控制权");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleReleasePayloadControlAuthority", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleReleasePayloadControlAuthority", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        ControlAuthModule controlAuth = drone.getControlAuthModule();
        if (controlAuth == null) {
            log("ERROR", "handleReleasePayloadControlAuthority", "获取控制权模块失败");
            waitForKeyPress();
            return;
        }

        controlAuth.releasePayloadControlAuthority(targetPayloadIndex, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleReleasePayloadControlAuthority", "释放负载控制权 error: " + error);
            }
        });
        
        log("INFO", "handleReleasePayloadControlAuthority", "释放负载控制权请求已发送，按Enter键返回...");
        waitForKeyPress();
    }
    
    /**
     * 监听负载控制权状态
     */
    private static void handleListenPayloadControlAuthorityState() {
        printSeparator("监听负载控制权状态");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleListenPayloadControlAuthorityState", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleListenPayloadControlAuthorityState", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        
        ControlAuthModule controlAuth = drone.getControlAuthModule();
        if (controlAuth == null) {
            log("ERROR", "handleListenPayloadControlAuthorityState", "获取控制权模块失败");
            waitForKeyPress();
            return;
        }

        controlAuth.listenPayloadControlAuthority(targetPayloadIndex, new FlightHubCallback.PayloadControlAuthorityCallback() {
            @Override
            public void onResult(boolean isGranted) {
                log("INFO", "handleListenPayloadControlAuthorityState", "监听负载控制权状态 is_granted: " + (isGranted ? "true" : "false"));
            }
        });
        
        log("INFO", "handleListenPayloadControlAuthorityState", "负载控制权状态监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    // ==================== 新增功能方法 ====================

    /**
     * 处理航线暂停功能
     */
    private static void handleFlightTaskPause() {
        printSeparator("航线暂停");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleFlightTaskPause", "该设备没有无人机，无法暂停航线");
            waitForKeyPress();
            return;
        }

        // 使用Drone包装类
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleFlightTaskPause", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleFlightTaskPause", "正在暂停航线...");

        // 获取航线控制模块并暂停
        FlightTaskControlModule flightTaskControlModule = drone.getFlightTaskControlModule();
        if (flightTaskControlModule != null) {
            flightTaskControlModule.pause(new FlightHubCallback.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                    if (error.isSuccess()) {
                        log("INFO", "handleFlightTaskPause", "航线暂停成功！");
                    } else {
                        log("ERROR", "handleFlightTaskPause", "航线暂停失败，错误: " + error);
                    }
                }
            });
            log("INFO", "handleFlightTaskPause", "航线暂停请求已发送");
        } else {
            log("ERROR", "handleFlightTaskPause", "获取航线控制模块失败");
        }

        log("INFO", "handleFlightTaskPause", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理航线恢复功能
     */
    private static void handleFlightTaskResume() {
        printSeparator("航线恢复");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleFlightTaskResume", "该设备没有无人机，无法恢复航线");
            waitForKeyPress();
            return;
        }

        // 使用Drone包装类
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleFlightTaskResume", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleFlightTaskResume", "正在恢复航线...");

        // 获取航线控制模块并恢复
        FlightTaskControlModule flightTaskControlModule = drone.getFlightTaskControlModule();
        if (flightTaskControlModule != null) {
            flightTaskControlModule.resume(new FlightHubCallback.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                    if (error.isSuccess()) {
                        log("INFO", "handleFlightTaskResume", "航线恢复成功！");
                    } else {
                        log("ERROR", "handleFlightTaskResume", "航线恢复失败，错误: " + error);
                    }
                }
            });
            log("INFO", "handleFlightTaskResume", "航线恢复请求已发送");
        } else {
            log("ERROR", "handleFlightTaskResume", "获取航线控制模块失败");
        }

        log("INFO", "handleFlightTaskResume", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理飞行急停功能
     */
    private static void handleFlightEmergencyStop() {
        printSeparator("飞行急停");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleFlightEmergencyStop", "该设备没有无人机，无法执行急停");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleFlightEmergencyStop", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleFlightEmergencyStop", "执行急停...");

        com.dji.flighthubsdk.core.FlightControlModule flightControl = drone.getFlightControlModule();
        if (flightControl == null) {
            log("ERROR", "handleFlightEmergencyStop", "获取飞控模块失败");
            waitForKeyPress();
            return;
        }

        flightControl.drcEmergencyStop(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (error.isSuccess()) {
                    log("SUCCESS", "handleFlightEmergencyStop", "急停执行成功");
                } else {
                    log("ERROR", "handleFlightEmergencyStop", "急停执行失败，错误代码: " + error);
                }
            }
        });

        log("INFO", "handleFlightEmergencyStop", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理飞行WASD控制功能
     */
    private static void handleFlightSticksControl() {
        printSeparator("飞行WASD控制");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleFlightSticksControl", "该设备没有无人机，无法执行摇杆控制");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleFlightSticksControl", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleFlightSticksControl", "开始执行摇杆控制测试...");

        try {
            // 创建摇杆控制数据，参考C++代码
            StickControlData stickData = new StickControlData();
            stickData.setRoll(2048);      // 右移
            stickData.setPitch(2048);     // 前进
            stickData.setThrottle(2048);  // 上升
            stickData.setYaw(2048);       // 右转
            stickData.setGimbalPitch(2048); // 云台俯仰

            // 发送控制数据，50Hz × 50次 × 20ms = 10秒
            int totalCommands = 50 * 10; // 500次命令
            int intervalMs = 20;          // 20ms间隔 (50Hz)

            com.dji.flighthubsdk.core.FlightControlModule flightControl = drone.getFlightControlModule();
            if (flightControl == null) {
                log("ERROR", "handleFlightSticksControl", "获取飞控模块失败");
                waitForKeyPress();
                return;
            }

            for (int i = 0; i < totalCommands; i++) {
                // 发送摇杆控制数据
                flightControl.drcSendStickControl(stickData);

                // 每100次命令显示一次进度
                if ((i + 1) % 100 == 0) {
                    int progress = ((i + 1) * 100) / totalCommands;
                    log("INFO", "handleFlightSticksControl", "进度: " + progress + "% (" + (i + 1) + "/" + totalCommands + ")");
                }

                // 控制间隔
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("WARN", "handleFlightSticksControl", "控制被中断");
                    break;
                }
            }

            log("SUCCESS", "handleFlightSticksControl", "摇杆控制测试完成！");
            log("INFO", "handleFlightSticksControl", "总计发送了 " + totalCommands + " 次控制命令");
            log("INFO", "handleFlightSticksControl", "控制持续时间: " + (totalCommands * intervalMs / 1000.0) + " 秒");

        } catch (Exception e) {
            log("ERROR", "handleFlightSticksControl", "摇杆控制执行失败: " + e.getMessage());
            e.printStackTrace();
        }

        log("INFO", "handleFlightSticksControl", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 检查功能状态
     */
    private static void checkFeatureStatus() {
        printSeparator("检查功能状态");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "checkFeatureStatus", "该设备没有无人机，无法检查功能状态");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "checkFeatureStatus", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "checkFeatureStatus", "正在检查功能状态...");

        ControlAuthModule controlAuth = drone.getControlAuthModule();
        if (controlAuth == null) {
            log("ERROR", "checkFeatureStatus", "获取控制权模块失败");
            waitForKeyPress();
            return;
        }

        // 检查飞行控制权
        controlAuth.listenFlightControlAuthority(new FlightHubCallback.FlightControlAuthorityCallback() {
            @Override
            public void onResult(boolean isGranted) {
                log("INFO", "checkFeatureStatus", "飞行控制权: " + (isGranted ? "已授予" : "未授予"));
            }
        });

        // 检查负载控制权
        controlAuth.listenPayloadControlAuthority(targetPayloadIndex, new FlightHubCallback.PayloadControlAuthorityCallback() {
            @Override
            public void onResult(boolean isGranted) {
                log("INFO", "checkFeatureStatus", "负载控制权: " + (isGranted ? "已授予" : "未授予"));
            }
        });

        com.dji.flighthubsdk.core.FlightControlModule flightControl = drone.getFlightControlModule();
        if (flightControl == null) {
            log("ERROR", "checkFeatureStatus", "获取飞控模块失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "checkFeatureStatus", "功能状态检查已启动，按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理飞行返航功能
     */
    private static void handleReturnHome() {
        printSeparator("飞行返航");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleReturnHome", "该设备没有无人机，无法执行返航");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleReturnHome", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleReturnHome", "执行返航...");

        com.dji.flighthubsdk.core.FlightControlModule flightControl = drone.getFlightControlModule();
        if (flightControl == null) {
            log("ERROR", "handleReturnHome", "获取飞控模块失败");
            waitForKeyPress();
            return;
        }

        flightControl.returnHome(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (error.isSuccess()) {
                    log("SUCCESS", "handleReturnHome", "返航执行成功");
                } else {
                    log("ERROR", "handleReturnHome", "返航执行失败，错误: " + error);
                }
            }
        });

        log("INFO", "handleReturnHome", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理取消返航功能
     */
    private static void handleReturnHomeCancel() {
        printSeparator("取消返航");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleReturnHomeCancel", "该设备没有无人机，无法执行取消返航");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleReturnHomeCancel", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleReturnHomeCancel", "执行取消返航...");

        com.dji.flighthubsdk.core.FlightControlModule flightControl = drone.getFlightControlModule();
        if (flightControl == null) {
            log("ERROR", "handleReturnHomeCancel", "获取飞控模块失败");
            waitForKeyPress();
            return;
        }

        flightControl.returnHomeCancel(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (error.isSuccess()) {
                    log("SUCCESS", "handleReturnHomeCancel", "取消返航执行成功");
                } else {
                    log("ERROR", "handleReturnHomeCancel", "取消返航执行失败，错误: " + error);
                }
            }
        });

        log("INFO", "handleReturnHomeCancel", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理一键起飞功能。
     * 参数对齐 QA HYAPP-189294 可用 curl（TakeoffWithFlyTo）；
     * 故意不 set commanderFlightHeight / commanderFlightMode（保持 -1），验证可选字段未设置时不写入请求体。
     */
    private static void handleTakeoff() {
        printSeparator("一键起飞");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleTakeoff", "该设备没有无人机，无法执行起飞");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleTakeoff", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleTakeoff", "执行一键起飞（对齐 CommandRequest / QA curl）...");

        // 必填 + 坐标 + rth_mode；可选 commander_* / cloud_to_cloud_id / need_verify 保持默认未设置（不写入请求体）
        com.dji.flighthubsdk.bean.TakeoffParams params = new com.dji.flighthubsdk.bean.TakeoffParams();
        params.setSecurityTakeoffHeight(100.0);
        params.setMaxSpeed(10);
        params.setOutOfControlAction(com.dji.flighthubsdk.value.OutOfControlAction.RETURN_HOME);
        params.setRthAltitude(100);
        params.setTargetHeight(120.0);
        params.setTargetLatitude(0.0);
        params.setTargetLongitude(0.0);
        params.setTakeoffMode(com.dji.flighthubsdk.value.TakeoffMode.TAKEOFF_WITH_FLY_TO);
        params.setRthMode(1);

        log("INFO", "handleTakeoff", "起飞参数（CommandRequest）:");
        log("INFO", "handleTakeoff", "  security_takeoff_height: " + params.getSecurityTakeoffHeight() + "m");
        log("INFO", "handleTakeoff", "  max_speed: " + params.getMaxSpeed() + "m/s");
        log("INFO", "handleTakeoff", "  out_of_control_action: " + params.getOutOfControlAction());
        log("INFO", "handleTakeoff", "  rth_altitude: " + params.getRthAltitude() + "m");
        log("INFO", "handleTakeoff", "  target_height: " + params.getTargetHeight() + "m");
        log("INFO", "handleTakeoff", "  target_lat/lon: " + params.getTargetLatitude() + ", " + params.getTargetLongitude());
        log("INFO", "handleTakeoff", "  takeoff_mode: " + params.getTakeoffMode());
        log("INFO", "handleTakeoff", "  rth_mode: " + params.getRthMode());
        log("INFO", "handleTakeoff", "  commander_flight_mode/height: " + params.getCommanderFlightMode()
                + " / " + params.getCommanderFlightHeight() + " (-1=未设置)");
        log("INFO", "handleTakeoff", "  cloud_to_cloud_id: '" + params.getCloudToCloudId() + "' (空=未设置)");
        log("INFO", "handleTakeoff", "  need_verify_flight_control: " + params.getNeedVerifyFlightControl()
                + " (-1=未设置)");

        drone.getTakeoffModule().takeoff(params, new FlightHubCallback.TakeoffCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error, com.dji.flighthubsdk.bean.TakeoffResult result) {
                if (error.isSuccess()) {
                    log("SUCCESS", "handleTakeoff", "起飞执行成功, flight_id=" + result.getFlightId() + ", fly_to_id=" + result.getFlyToId());
                } else {
                    log("ERROR", "handleTakeoff", "起飞执行失败，错误代码: " + error);
                }
            }
        });

        log("INFO", "handleTakeoff", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * [223] 空中下发航线测试（Confluence 794727833，分接口逐步验证）
     */
    private static void handleInFlightWaylineTest() {
        printSeparator("空中下发航线测试");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleInFlightWaylineTest", "空中下发航线需要机场+无人机拓扑（sub_device_sn 为空）");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleInFlightWaylineTest", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        InFlightWaylineModule module = drone.getInFlightWaylineModule();
        if (module == null) {
            log("ERROR", "handleInFlightWaylineTest", "getInFlightWaylineModule 失败");
            waitForKeyPress();
            return;
        }

        String gatewaySn = selectedDevice.getDevice().getDeviceSn();
        String droneSn = getSubDeviceSn(selectedDevice);
        log("INFO", "handleInFlightWaylineTest", "网关 SN: " + gatewaySn);
        log("INFO", "handleInFlightWaylineTest", "飞机 SN: " + droneSn + "（gateway_sn/drone_sn 由 Module 注入）");
        System.out.println(" 创建空中航线任务 [1]  POST /drc/api/v3/flight-tasks/in-flight-way-line-task");
        System.out.println(" 查询活跃任务 [2]        GET  /drc/api/v3/flight-tasks/in-flight");
        System.out.println(" 控制任务 [3]            PUT  /drc/api/v3/flight-wayline-tasks/commands");
        System.out.println(" 监听进度 [4]            WS   in_flight_wayline_task_notify");
        System.out.println(" 取消监听 [5]");
        System.out.print("请输入选择: ");

        int userChoice = 0;
        try {
            userChoice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            log("ERROR", "handleInFlightWaylineTest", "无效输入");
            waitForKeyPress();
            return;
        }

        switch (userChoice) {
            case 1: {
                CreateInFlightWaylineTaskParams params = new CreateInFlightWaylineTaskParams();
                params.setWaylineUuid("4cb0d615-4f3f-4c08-8620-0dd148ab103c");
                params.setWaylinePrecisionType(1);
                params.setRthMode(1);
                params.setOutOfControlAction(1);
                params.setExitWaylineWhenRcLost(1);
                params.setRthAltitude(60);
                params.setWaylineDuration(80.988884);
                params.setWaylineLength(1200.0);
                params.setTaskName("java_demo_in_flight_wayline");
                params.setFastReconstruction(Boolean.FALSE);

                log("INFO", "handleInFlightWaylineTest", "createInFlightWaylineTask 请求参数:");
                log("INFO", "handleInFlightWaylineTest", "  wayline_uuid: " + params.getWaylineUuid());
                log("INFO", "handleInFlightWaylineTest", "  rth_altitude: " + params.getRthAltitude());
                log("INFO", "handleInFlightWaylineTest", "  wayline_duration: " + params.getWaylineDuration());
                log("INFO", "handleInFlightWaylineTest", "  task_name: " + params.getTaskName());

                module.createInFlightWaylineTask(params, new FlightHubCallback.CreateInFlightWaylineTaskCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.CreateInFlightWaylineTaskResult result,
                                         com.dji.flighthubsdk.bean.FlightHubError err) {
                        if (err.isSuccess()) {
                            log("SUCCESS", "handleInFlightWaylineTest",
                                    "createInFlightWaylineTask 成功, flight_task_id=" + result.getFlightTaskId()
                                            + ", in_flight_wayline_task_id=" + result.getInFlightWaylineTaskId());
                        } else {
                            log("ERROR", "handleInFlightWaylineTest",
                                    "createInFlightWaylineTask 失败: " + err);
                        }
                    }
                });
                break;
            }
            case 2: {
                module.getActiveInFlightWaylineTask(new FlightHubCallback.GetActiveInFlightWaylineTaskCallback() {
                    @Override
                    public void onResult(ActiveInFlightWaylineTaskInfo info,
                                         com.dji.flighthubsdk.bean.FlightHubError err) {
                        if (err.isSuccess()) {
                            log("SUCCESS", "handleInFlightWaylineTest",
                                    "getActiveInFlightWaylineTask 成功, flight_id=" + info.getFlightId()
                                            + ", flight_type=" + info.getFlightType()
                                            + ", sn=" + info.getSn());
                            if (info.getInFlightTask() != null) {
                                com.dji.flighthubsdk.bean.InFlightWaylineTaskDetail task = info.getInFlightTask();
                                if (task.getUuid() == null || task.getUuid().isEmpty()) {
                                    log("INFO", "handleInFlightWaylineTest", "  in_flight_task: 无活跃空中航线任务");
                                } else {
                                    log("INFO", "handleInFlightWaylineTest",
                                            "  uuid=" + task.getUuid()
                                                    + ", status=" + task.getStatus()
                                                    + ", progress=" + task.getProgressPercent() + "%"
                                                    + ", waypoint=" + task.getWayPointIndex()
                                                    + ", wayline_id=" + task.getWaylineId()
                                                    + ", err_code=" + task.getErrCode()
                                                    + ", task_name=" + task.getTaskName());
                                }
                            }
                        } else {
                            log("ERROR", "handleInFlightWaylineTest",
                                    "getActiveInFlightWaylineTask 失败: " + err);
                        }
                    }
                });
                break;
            }
            case 3: {
                System.out.print("控制动作: 1=暂停 2=恢复 3=取消，请输入 action [1]: ");
                String line = scanner.nextLine().trim();
                int action = 1;
                if (!line.isEmpty()) {
                    try {
                        action = Integer.parseInt(line);
                    } catch (NumberFormatException ignored) {
                        action = 1;
                    }
                }

                InFlightWaylineControlAction controlAction;
                switch (action) {
                    case 2:
                        controlAction = InFlightWaylineControlAction.RESUME;
                        break;
                    case 3:
                        controlAction = InFlightWaylineControlAction.CANCEL;
                        break;
                    case 1:
                    default:
                        controlAction = InFlightWaylineControlAction.PAUSE;
                        break;
                }

                final int actionFinal = action;
                module.controlInFlightWaylineTask(controlAction, new FlightHubCallback.InFlightWaylineControlCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError err) {
                        if (err.isSuccess()) {
                            log("SUCCESS", "handleInFlightWaylineTest",
                                    "controlInFlightWaylineTask 成功, action=" + actionFinal);
                        } else {
                            log("ERROR", "handleInFlightWaylineTest",
                                    "controlInFlightWaylineTask 失败: " + err);
                        }
                    }
                });
                break;
            }
            case 4: {
                if (inFlightWaylineListenId != -1) {
                    log("WARN", "handleInFlightWaylineTest",
                            "已存在监听 listen_id=" + inFlightWaylineListenId + "，请先选择 [5] 取消监听");
                    break;
                }
                inFlightWaylineListenId = module.listenInFlightWaylineProgress(
                        new FlightHubCallback.InFlightWaylineProgressCallback() {
                            @Override
                            public void onResult(InFlightWaylineProgress msg) {
                                log("INFO", "handleInFlightWaylineTest",
                                        "listenInFlightWaylineProgress notify: id=" + msg.getInFlightWaylineId()
                                                + ", percent=" + msg.getPercent()
                                                + ", status=" + msg.getStatus()
                                                + ", result=" + msg.getResult()
                                                + ", waypoint=" + msg.getWayPointIndex()
                                                + ", gateway_sn=" + msg.getGatewaySn()
                                                + ", task_name=" + msg.getTaskName()
                                                + ", timestamp=" + msg.getTimestamp());
                            }
                        });
                log("INFO", "handleInFlightWaylineTest",
                        "listenInFlightWaylineProgress listen_id=" + inFlightWaylineListenId);
                log("INFO", "handleInFlightWaylineTest", "提示: 可先选择 [1] 创建任务，进度将通过 NC 推送");
                break;
            }
            case 5: {
                if (inFlightWaylineListenId == -1) {
                    log("INFO", "handleInFlightWaylineTest", "当前无活跃监听");
                } else {
                    module.cancelListenInFlightWaylineProgress(inFlightWaylineListenId);
                    log("INFO", "handleInFlightWaylineTest",
                            "cancelListenInFlightWaylineProgress listen_id=" + inFlightWaylineListenId);
                    inFlightWaylineListenId = -1;
                }
                break;
            }
            default:
                log("ERROR", "handleInFlightWaylineTest", "无效选择: " + userChoice);
                break;
        }

        waitForKeyPress();
    }

    // ==================== 文件输出相关方法 ====================
    
    /**
     * 初始化流数据输出
     */
    private static void initStreamOutputs() throws IOException {
        // 创建输出目录
        Files.createDirectories(Paths.get(outputDirectory));
        
        // 生成文件名（包含时间戳）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        
        String dockFileName = outputDirectory + "/dji_stream_dock_" + timestamp + ".h264";
        String droneFileName = outputDirectory + "/dji_stream_drone_" + timestamp + ".h264";
        
        // 创建文件输出流
        dockStreamOutput = new FileOutputStream(dockFileName);
        droneStreamOutput = new FileOutputStream(droneFileName);
        
        // 初始化异步文件写入线程池
        fileWriteExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "file-writer");
            t.setDaemon(true);
            return t;
        });
        
        log("INFO", "initStreamOutputs", "机库流数据将保存到: " + dockFileName);
        log("INFO", "initStreamOutputs", "无人机流数据将保存到: " + droneFileName);
        log("INFO", "initStreamOutputs", "异步文件写入已启用，最大队列大小: " + MAX_QUEUE_SIZE);
    }
    
    /**
     * 写入机库流数据（异步）
     */
    private static void writeDockStreamData(byte[] data) {
        if (dockStreamOutput != null && fileWriteExecutor != null) {
            // 检查队列大小，如果队列满了就丢包
            if (fileWriteExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) fileWriteExecutor;
                if (tpe.getQueue().size() >= MAX_QUEUE_SIZE) {
                    droppedFrames.incrementAndGet();
                    if (droppedFrames.get() % 100 == 0) {
                        log("WARN", "writeDockStreamData", "文件写入队列满，已丢弃 " + droppedFrames.get() + " 帧");
                    }
                    return;
                }
            }
            
            // 异步写入文件
            fileWriteExecutor.submit(() -> {
                try {
                    dockStreamOutput.write(data);
                    dockStreamOutput.flush();
                } catch (IOException e) {
                    log("ERROR", "writeDockStreamData", "写入机库流数据失败: " + e.getMessage());
                }
            });
        }
    }

    /**
     * 写入无人机流数据（异步）
     */
    private static void writeDroneStreamData(byte[] data) {
        if (droneStreamOutput != null && fileWriteExecutor != null) {
            // 检查队列大小，如果队列满了就丢包
            if (fileWriteExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) fileWriteExecutor;
                if (tpe.getQueue().size() >= MAX_QUEUE_SIZE) {
                    droppedFrames.incrementAndGet();
                    if (droppedFrames.get() % 100 == 0) {
                        log("WARN", "writeDroneStreamData", "文件写入队列满，已丢弃 " + droppedFrames.get() + " 帧");
                    }
                    return;
                }
            }
            
            // 异步写入文件
            fileWriteExecutor.submit(() -> {
                try {
                    droneStreamOutput.write(data);
                    droneStreamOutput.flush();
                } catch (IOException e) {
                    log("ERROR", "writeDroneStreamData", "写入无人机流数据失败: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * 关闭流数据输出
     */
    private static void closeStreamOutputs() {
        // 关闭异步文件写入线程池
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
            fileWriteExecutor = null;
        }
        
        if (dockStreamOutput != null) {
            try {
                dockStreamOutput.close();
                log("INFO", "closeStreamOutputs", String.format("机库流数据文件已关闭, 总计写入 %d 帧, %d 字节 (%.2f MB)", 
                    dockFrameCount, dockTotalBytes, dockTotalBytes / 1024.0 / 1024.0));
            } catch (IOException e) {
                log("ERROR", "closeStreamOutputs", "关闭机库流数据文件失败: " + e.getMessage());
            }
            dockStreamOutput = null;
        }
        
        if (droneStreamOutput != null) {
            try {
                droneStreamOutput.close();
                log("INFO", "closeStreamOutputs", String.format("无人机流数据文件已关闭, 总计写入 %d 帧, %d 字节 (%.2f MB)", 
                    droneFrameCount, droneTotalBytes, droneTotalBytes / 1024.0 / 1024.0));
            } catch (IOException e) {
                log("ERROR", "closeStreamOutputs", "关闭无人机流数据文件失败: " + e.getMessage());
            }
            droneStreamOutput = null;
        }
        
        // 打印丢包统计
        if (droppedFrames.get() > 0) {
            log("INFO", "closeStreamOutputs", "文件写入过程中共丢弃 " + droppedFrames.get() + " 帧");
        }
    }

    // ==================== 监听飞机在线状态、链路状态 ====================
    private static void handleListenOnlineState() {
        printSeparator("监听飞机在线状态、链路状态");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleListenOnlineState", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleListenOnlineState", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        droneOnlineStatusListenId = drone.getDeviceStateModule().listenDroneOnlineStatus(new FlightHubCallback.DroneOnlineStatusCallback() {
            @Override
            public void onResult(DroneOnlineStatus status) {
                log("INFO", "handleListenOnlineState", "飞机在线状态: " + status.isOnline());
            }
        });

        linkStatusListenId = drone.getDrcModule().listenLinkStatus(new FlightHubCallback.LinkStatusCallback() {
            @Override
            public void onResult(LinkStatus status) {
                log("INFO", "handleListenOnlineState", "飞机链路状态: " + status.getCtrlLinkStatus());
            }
        });

        log("INFO", "handleListenOnlineState", "飞机在线状态、链路状态监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    // ==================== [460] HYAPP-177995 复现 - SWIG director 异步回调 UAF ====================
    /**
     * HYAPP-177995 复现：监听直播可用范围 + 监听在线状态；离线即【立即】cancel 范围监听
     * （制造与"关机→范围变空派发"的竞态），在线即重新 listen。
     *
     * 操作：进入后反复对飞机【关机→等离线 / 开机→等在线】循环数十次，按 Enter 停止。
     * - 旧包（裸指针 wrapper）：若干轮后 JVM SIGSEGV(hs_err, pc=0/RIP=0)。
     * - 修复包（C++ shared_ptr 接管 director + Java swigReleaseOwnership）：长时间不崩，
     *   允许 cancel 后最后一帧在途回调静默失效（NPE 被 SWIG 吃掉）。
     *
     * 注：真机 race 命中靠时序，可能需多轮；机制层确定性验证见 ASAN 微复现 uaf_repro.cpp。
     */
    private static void handleHyapp177995Repro() {
        printSeparator("HYAPP-177995 复现 - 监听直播范围 + 离线即 cancel (SWIG director UAF)");

        if (selectedDevice == null || !hasSubDevice(selectedDevice)) {
            log("ERROR", "handleHyapp177995Repro", "该设备没有无人机，无法复现");
            waitForKeyPress();
            return;
        }

        final Drone d = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (d == null) {
            log("ERROR", "handleHyapp177995Repro", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        // 自包含的监听 id 与计数，避免与其它 handler 共享的静态字段相互干扰
        final Object listenLock = new Object();
        final int[] rangeId = { -1 };
        final int[] onlineId = { -1 };
        final java.util.concurrent.atomic.AtomicInteger onlineCycles = new java.util.concurrent.atomic.AtomicInteger();
        final java.util.concurrent.atomic.AtomicInteger rangeCallbacks = new java.util.concurrent.atomic.AtomicInteger();

        // 启动/重启 范围监听
        final Runnable startRange = () -> {
            synchronized (listenLock) {
                if (rangeId[0] >= 0) return; // 已在监听
                onlineCycles.incrementAndGet();
                rangeId[0] = d.getLiveDeviceModule().listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                    @Override
                    public void onResult(java.util.List<LiveInfo> list) {
                        rangeCallbacks.incrementAndGet();
                        log("INFO", "handleHyapp177995Repro", "[range] size=" + (list == null ? -1 : list.size()));
                    }
                });
                log("INFO", "handleHyapp177995Repro", "[listen] rangeListenId=" + rangeId[0]);
            }
        };

        // 离线瞬间【立即】cancel 范围监听 —— 与"范围变空派发"竞态，越零延迟越易命中
        final Runnable stopRange = () -> {
            synchronized (listenLock) {
                if (rangeId[0] < 0) return;
                int id = rangeId[0];
                rangeId[0] = -1;
                log("INFO", "handleHyapp177995Repro", "[cancel] rangeListenId=" + id);
                d.getLiveDeviceModule().cancelListenLiveAvailableRange(id); // ★ 离线即同步 cancel
            }
        };

        // 监听在线状态：离线→cancel，在线→relisten
        synchronized (listenLock) {
            onlineId[0] = d.getDeviceStateModule().listenDroneOnlineStatus(new FlightHubCallback.DroneOnlineStatusCallback() {
                @Override
                public void onResult(DroneOnlineStatus status) {
                    boolean online = status != null && status.isOnline();
                    log("INFO", "handleHyapp177995Repro", "[online] is_online=" + online);
                    if (online) {
                        startRange.run();
                    } else {
                        stopRange.run(); // ★ 离线即立即 cancel，制造竞态
                    }
                }
            });
        }

        // 启动时若已在线，先 listen 一次
        startRange.run();

        log("INFO", "handleHyapp177995Repro",
                "复现中：请反复对飞机【关机→等离线 / 开机→等在线】循环数十次。按 Enter 停止。");
        log("INFO", "handleHyapp177995Repro",
                "旧包预期：若干轮后 JVM 崩溃(hs_err, SIGSEGV pc=0)；修复包预期：长时间不崩。");
        waitForKeyPress();

        // 清理：取消两个监听
        synchronized (listenLock) {
            if (onlineId[0] >= 0) {
                d.getDeviceStateModule().cancelListenDroneOnlineStatus(onlineId[0]);
                onlineId[0] = -1;
            }
            if (rangeId[0] >= 0) {
                d.getLiveDeviceModule().cancelListenLiveAvailableRange(rangeId[0]);
                rangeId[0] = -1;
            }
        }
        log("INFO", "handleHyapp177995Repro",
                "已停止。online_cycles=" + onlineCycles.get() + " range_callbacks=" + rangeCallbacks.get());
    }


    // ==================== [95] 负载控制 - 变焦 ====================
    private static void handleSetFocalLength() {
        printSeparator("变焦");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetFocalLength", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetFocalLength", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        com.dji.flighthubsdk.bean.CameraFocalLengthParams params = new com.dji.flighthubsdk.bean.CameraFocalLengthParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setCameraType(CameraLensType.ZOOM);
        params.setZoomFactor(0);  // use 0 as default;

        // zoom factors;
        int[] numbers = {-2, -1, 0, 1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 60, 70, 80, 100, 120, 150, 180, 190, 198, 199, 200, 201, 202};
        StringBuilder sb = new StringBuilder();
        for (int number : numbers) {
            params.setZoomFactor(number);
            sb.setLength(0);
            sb.append("{")
                .append("CameraType: ").append(params.getCameraType()).append(", ")
                .append("PayloadIndex: ").append(params.getPayloadIndex()).append(", ")
                .append("ZoomFactor: ").append(params.getZoomFactor()).append("}");

            try {
                Thread.sleep(3000);  // 暂停 3 秒;
            } catch (InterruptedException e) {
                System.err.println("handleSetFocalLength() 线程被中断: " + e.getMessage());
            }

            log("INFO", "handleSetFocalLength", "执行 变焦测试, 变焦参数: " + sb.toString());
            drone.getCameraControlModule().drcSetFocalLength(params, new FlightHubCallback.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                    log("INFO", "handleSetFocalLength", "变焦结果: " + error);
                }
            });
        }

        log("INFO", "handleSetFocalLength", "变焦, 按Enter键停止 ...");
        waitForKeyPress();
    }

    // ==================== 框选变焦 ====================
    private static void handleZoomIn() {
        printSeparator("框选变焦");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleZoomIn", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleZoomIn", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        com.dji.flighthubsdk.bean.ZoomAtTargetParams params = new com.dji.flighthubsdk.bean.ZoomAtTargetParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setCameraLensType(CameraLensType.ZOOM);
        params.setX(0.5);
        params.setY(0.5);
        params.setWidth(0.05);
        params.setHeight(0.05);
        drone.getCameraControlModule().drcZoomAtTarget(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleZoomIn", "框选变焦结果: " + error);
            }
        });

        log("INFO", "handleZoomIn", "框选变焦已启动，按Enter键停止...");
        waitForKeyPress();
    }

    // ==================== LookAtTarget ====================
    private static void handleLookAtTarget() {
        printSeparator("LookAtTarget");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleLookAtTarget", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleLookAtTarget", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        com.dji.flighthubsdk.bean.LookAtTargetParams params = new com.dji.flighthubsdk.bean.LookAtTargetParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setLatitude(0.0);
        params.setLongitude(0.0);
        params.setHeight(200);

        drone.getCameraControlModule().lookAtTarget(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleLookAtTarget", "LookAtTarget结果: " + error);
            }
        });

        log("INFO", "handleLookAtTarget", "LookAtTarget已启动，按Enter键停止...");
        waitForKeyPress();
    }

    // ==================== 开始全景拍照 ====================
    private static void handleStartPanorama() {
        printSeparator("开始全景拍照");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleStartPanorama", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleStartPanorama", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        PanoramaParam params = new PanoramaParam();
        params.setPayloadIndex(targetPayloadIndex);
        drone.getCameraControlModule().drcStartPanorama(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleStartPanorama", "开始全景拍照结果: " + error);
            }
        });

        panoramaStatusListenId = drone.getCameraControlModule().listenDrcPanoramaStatus(targetPayloadIndex, new FlightHubCallback.PanoramaStatusCallback() {
            @Override
            public void onResult(PanoramaStatus status) {
                log("INFO", "handleStopPanorama", "全景拍照状态: percent=" + status.getPercent()
                        + ", currentStep(swig)=" + status.getCurrentStep()
                        + ", photoState(swig)=" + status.getPhotoState());
            }
        });
        log("INFO", "handleStartPanorama", "开始全景拍照已启动，按Enter键停止...");
        waitForKeyPress();
    }

    // ==================== 停止全景拍照 ====================
    private static void handleStopPanorama() {
        printSeparator("停止全景拍照");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleStopPanorama", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleStopPanorama", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        drone.getCameraControlModule().drcStopPanorama(targetPayloadIndex, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleStopPanorama", "停止全景拍照结果: " + error);
            }
        });

        if (panoramaStatusListenId != -1) {
            drone.getCameraControlModule().cancelListenDrcPanoramaStatus(targetPayloadIndex, panoramaStatusListenId);
        }
    }

    // ==================== 开始录像 ====================
    private static void handleStartRecord() {
        printSeparator("开始录像");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleStartRecord", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleStartRecord", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        drone.getCameraControlModule().drcStartRecord(targetPayloadIndex, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleStartRecord", "开始录像结果: " + error);
            }
        });
    }

    // ==================== 停止录像 ====================
    private static void handleStopRecord() {
        printSeparator("停止录像");

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleStopRecord", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        drone.getCameraControlModule().drcStopRecord(targetPayloadIndex, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleStopRecord", "停止录像结果: " + error);
            }
        });
    }

    private static void handleSwitchCameraMode() {
        printSeparator("切换相机模式");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSwitchCameraMode", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSwitchCameraMode", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        CameraMode cameraMode = null;
        log("INFO", "handleSwitchCameraMode", "请选择相机模式: ");
        log("INFO", "handleSwitchCameraMode", "0: 拍照");
        log("INFO", "handleSwitchCameraMode", "1: 录像");
        log("INFO", "handleSwitchCameraMode", "2: 智能低光拍照");
        log("INFO", "handleSwitchCameraMode", "3: 全景拍照");
        log("INFO", "handleSwitchCameraMode", "4: 定时拍照");
        int choice = getUserChoice(0, 4);
        switch (choice) {
            case 0:
                cameraMode = CameraMode.PHOTO;
                break;
            case 1:
                cameraMode = CameraMode.VIDEO;
                break;
            case 2:
                cameraMode = CameraMode.SMART_PHOTO;
                break;
            case 3:
                cameraMode = CameraMode.PANO_PHOTO;
                break;
            case 4:
                cameraMode = CameraMode.INTERVAL_PHOTO;
                break;
            default:
                log("ERROR", "handleSwitchCameraMode", "无效的相机模式");
                waitForKeyPress();
                return;
        }

        com.dji.flighthubsdk.bean.SwitchCameraModeParams params = new com.dji.flighthubsdk.bean.SwitchCameraModeParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setCameraMode(cameraMode);
        drone.getCameraControlModule().drcSwitchCameraMode(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleSwitchCameraMode", "切换相机模式结果: " + error);
            }
        });

        log("INFO", "handleSwitchCameraMode", "切换相机模式已启动，按Enter键停止...");
        waitForKeyPress();
    }

    private static void handleSwitchLiveLens() {
        printSeparator("切换镜头");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSwitchLiveLens", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSwitchLiveLens", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        CameraLensType cameraLensType = null;
        log("INFO", "handleSwitchCameraLens", "请选择相机模式: ");
        log("INFO", "handleSwitchCameraLens", "0: 广角");
        log("INFO", "handleSwitchCameraLens", "1: 变焦");
        log("INFO", "handleSwitchCameraLens", "2: 红外");
        int choice = getUserChoice(0, 2);
        switch (choice) {
            case 0:
                cameraLensType = CameraLensType.WIDE;
                break;
            case 1:
                cameraLensType = CameraLensType.ZOOM;
                break;
            case 2:
                cameraLensType = CameraLensType.IR;
                break;
            default:
                log("ERROR", "handleSwitchCameraLens", "无效的相机模式");
                waitForKeyPress();
                return;
        }

        com.dji.flighthubsdk.bean.SwitchLiveLensParams params = new com.dji.flighthubsdk.bean.SwitchLiveLensParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setLensType(cameraLensType);
        drone.getCameraControlModule().drcSwitchLiveLens(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleSwitchLiveLens", "切换镜头结果: " + error);
            }
        });

        log("INFO", "handleSwitchLiveLens", "切换镜头已启动，按Enter键停止...");
        waitForKeyPress();
    }

    private static void handleSetPhotoStorageSettings() {
        printSeparator("设置拍照存储设置");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetPhotoStorageSettings", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetPhotoStorageSettings", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handleSetPhotoStorageSettings", "请输入拍照存储设置(逗号分隔，如 current,vision,ir；可选: current/vision/ir/wide/zoom):");
        String line = scanner.nextLine();
        java.util.List<String> settings = new java.util.ArrayList<>();
        if (line != null && !line.trim().isEmpty()) {
            for (String item : line.split(",")) {
                String value = item.trim();
                if (!value.isEmpty()) {
                    settings.add(value);
                }
            }
        }
        SetPhotoStorageSettingsParams params = new SetPhotoStorageSettingsParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setPhotoStorageSettings(settings);
        drone.getCameraControlModule().drcSetPhotoStorageSettings(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleSetPhotoStorageSettings", "设置拍照存储设置结果: " + error);
            }
        });
        waitForKeyPress();
    }

    private static void handleSetVideoStorageSettings() {
        printSeparator("设置录像存储设置");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetVideoStorageSettings", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetVideoStorageSettings", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handleSetVideoStorageSettings", "请输入录像存储设置(逗号分隔，如 current,vision；可选: current/vision/ir/wide/zoom):");
        String line = scanner.nextLine();
        java.util.List<String> settings = new java.util.ArrayList<>();
        if (line != null && !line.trim().isEmpty()) {
            for (String item : line.split(",")) {
                String value = item.trim();
                if (!value.isEmpty()) {
                    settings.add(value);
                }
            }
        }
        SetVideoStorageSettingsParams params = new SetVideoStorageSettingsParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setVideoStorageSettings(settings);
        drone.getCameraControlModule().drcSetVideoStorageSettings(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleSetVideoStorageSettings", "设置录像存储设置结果: " + error);
            }
        });
        waitForKeyPress();
    }

    private static void handleSetNightSceneMode() {
        printSeparator("设置夜景模式");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetNightSceneMode", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetNightSceneMode", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handleSetNightSceneMode", "请输入模式: 0=关闭, 1=开启, 2=自动(默认2):");
        String line = scanner.nextLine();
        int mode = 2;
        try {
            if (line != null && !line.trim().isEmpty()) {
                mode = Integer.parseInt(line.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        SetNightSceneModeParams params = new SetNightSceneModeParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setMode(NightSceneMode.fromValue(mode));
        drone.getCameraControlModule().drcSetNightSceneMode(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleSetNightSceneMode", "设置夜景模式结果: " + error);
            }
        });
        waitForKeyPress();
    }

    private static void handleListenNightModeSettings() {
        printSeparator("监听夜景模式状态");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleListenNightModeSettings", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleListenNightModeSettings", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        nightModeSettingsListenId = drone.getCameraControlModule().listenDrcNightModeSettings(targetPayloadIndex,
                new FlightHubCallback.NightModeSettingsCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.NightModeSettings s) {
                        log("INFO", "nightModeSettings",
                                "payload=" + s.getPayloadIndex() + " mode=" + s.getNightMode()
                                        + " working=" + s.isWorking());
                    }
                });
        log("INFO", "handleListenNightModeSettings", "listenId=" + nightModeSettingsListenId);
        waitForKeyPress();
    }

    private static void handleCancelListenNightModeSettings() {
        printSeparator("取消监听夜景模式状态");
        if (!hasSubDevice(selectedDevice) || drone == null || nightModeSettingsListenId < 0) {
            log("WARN", "handleCancelListenNightModeSettings", "无效状态或未监听");
            waitForKeyPress();
            return;
        }
        drone.getCameraControlModule().cancelListenDrcNightModeSettings(targetPayloadIndex, nightModeSettingsListenId);
        nightModeSettingsListenId = -1;
        log("INFO", "handleCancelListenNightModeSettings", "已取消");
        waitForKeyPress();
    }

    private static void handleSetLiveQuality() {
        printSeparator("设置直播画质");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetLiveQuality", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetLiveQuality", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleSetLiveQuality", "请选择画质: 0=自适应, 1=标清, 4=超清 (默认4)");
        int choice = getUserChoice(0, 4);
        int videoQuality = 4;
        if (choice == 0) {
            videoQuality = 0;
        } else if (choice == 1) {
            videoQuality = 1;
        } else {
            videoQuality = 4;
        }

        LiveQualitySettingParams params = new LiveQualitySettingParams();
        params.setSn(getSubDeviceSn(selectedDevice));
        params.setCamera(targetPayloadIndex);
        params.setVideo("normal-0");
        params.setVideoQuality(videoQuality);

        // setLiveQuality 从 ILiveModule 调用，需先通过 getLiveStream 获取对应的 live_module
        if (droneLiveModule == null) {
            com.dji.flighthubsdk.bean.LiveInfo liveInfo = new com.dji.flighthubsdk.bean.LiveInfo(
                params.getSn(), params.getCamera(), params.getVideo(), java.util.Collections.emptyList());
            droneLiveModule = drone.getLiveStream(liveInfo);
        }
        if (droneLiveModule == null) {
            log("ERROR", "handleSetLiveQuality", "获取 LiveModule 失败");
            waitForKeyPress();
            return;
        }

        droneLiveModule.setLiveQuality(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleSetLiveQuality", "设置直播画质结果: " + error);
            }
        });

        log("INFO", "handleSetLiveQuality", "已发起设置直播画质请求");
        waitForKeyPress();
    }

    private static void handleSetDockLiveQuality() {
        printSeparator("设置机场直播画质（dock）");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleSetDockLiveQuality", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleSetDockLiveQuality", "请选择画质: 0=自适应, 1=标清, 4=超清 (默认4)");
        int choice = getUserChoice(0, 4);
        int videoQuality = 4;
        if (choice == 0) {
            videoQuality = 0;
        } else if (choice == 1) {
            videoQuality = 1;
        } else {
            videoQuality = 4;
        }

        LiveQualitySettingParams params = new LiveQualitySettingParams();
        params.setSn(getMainDeviceSn(selectedDevice));
        params.setCamera("165-0-7");   // 机场摄像头（舱外）默认 payload_index，可按实际修改
        params.setVideo("normal-0");
        params.setVideoQuality(videoQuality);

        // setLiveQuality 从 ILiveModule 调用，需先通过 getLiveStream 获取对应的 live_module
        if (dockLiveModule == null) {
            com.dji.flighthubsdk.bean.LiveInfo liveInfo = new com.dji.flighthubsdk.bean.LiveInfo(
                params.getSn(), params.getCamera(), params.getVideo(), java.util.Collections.emptyList());
            dockLiveModule = dock.getLiveStream(liveInfo);
        }
        if (dockLiveModule == null) {
            log("ERROR", "handleSetDockLiveQuality", "获取 LiveModule 失败");
            waitForKeyPress();
            return;
        }

        dockLiveModule.setLiveQuality(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleSetDockLiveQuality", "设置机场直播画质结果: " + error);
            }
        });

        log("INFO", "handleSetDockLiveQuality", "已发起设置机场直播画质请求");
        waitForKeyPress();
    }
    // ==================== PSDK相关方法 ====================

    /**
     * 处理PSDK状态监听功能
     */
    private static void handlePsdkState() {
        printSeparator("PSDK状态监听");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkState", "该设备没有无人机，无法监听PSDK状态");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkState", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkState", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkState", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkState", "没有找到PSDK设备");
                    return;
                }

                // 输出 PSDK 设备列表[简略信息];
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkState", "PSDK设备[" + i + "] PsdkDeviceInfo: " +
                        "{ psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey() + " }"
                    );
                }

                // 监听 PSDK 设备信息;
                // 注意: 这里不深入考虑 PSDK 设备类型, 统一监听 index;
                // 也可以作为 "错误传参"的一种测试参考;
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    int psdkIndex = psdkDeviceList.getDeviceInfos().get(i).getPsdkIndex();
                    log("INFO", "handlePsdkState", "使用PSDK设备索引: " + psdkIndex);

                    // 监听PSDK状态摘要信息
                    drone.getPsdkControlModule().listenDrcPsdkStateSummaryInfo(psdkIndex, new FlightHubCallback.PsdkStateSummaryInfoCallback() {
                        @Override
                        public void onResult(PsdkStateSummaryInfo summaryInfo) {
                            log("INFO", "handlePsdkState", "PSDK状态摘要信息(PsdkStateSummaryInfo): ");
                            log("INFO", "handlePsdkState", "  psdk_index: " + summaryInfo.getPsdkIndex());
                            log("INFO", "handlePsdkState", "  psdk_type: " + summaryInfo.getPsdkType());
                            log("INFO", "handlePsdkState", "  psdk_sub_type: " + summaryInfo.getPsdkSubType());
                            log("INFO", "handlePsdkState", "  psdk_name: " + summaryInfo.getPsdkName());
                            log("INFO", "handlePsdkState", "  psdk_sn: " + summaryInfo.getPsdkSn());
                            log("INFO", "handlePsdkState", "  psdk_version: " + summaryInfo.getPsdkVersion());
                            log("INFO", "handlePsdkState", "  psdk_lib_version: " + summaryInfo.getPsdkLibVersion());
                            log("INFO", "handlePsdkState", "  psdk_restrict_state: " + summaryInfo.getPsdkRestrictState());
                        }
                    });


                    // 监听PSDK喊话器状态信息
                    drone.getPsdkControlModule().listenDrcPsdkStateSpeakerInfo(psdkIndex, new FlightHubCallback.PsdkStateSpeakerInfoCallback() {
                        @Override
                        public void onResult(PsdkStateSpeakerInfo speakerInfo) {
                            log("INFO", "handlePsdkState", "PSDK喊话器状态信息:");
                            log("INFO", "handlePsdkState", "  work_mode: " + speakerInfo.getWorkMode());
                            log("INFO", "handlePsdkState", "  play_mode: " + speakerInfo.getPlayMode());
                            log("INFO", "handlePsdkState", "  system_state: " + speakerInfo.getSystemState());
                            log("INFO", "handlePsdkState", "  play_volume: " + speakerInfo.getPlayVolume());
                            log("INFO", "handlePsdkState", "  play_file_name: " + speakerInfo.getPlayFileName());
                            log("INFO", "handlePsdkState", "  play_file_md5: " + speakerInfo.getPlayFileMd5());
                            log("INFO", "handlePsdkState", "  tts_volume: " + speakerInfo.getTtsVolume());
                            log("INFO", "handlePsdkState", "  tts_type: " + speakerInfo.getTtsType());
                            log("INFO", "handlePsdkState", "  tts_language: " + speakerInfo.getTtsLanguage());
                            log("INFO", "handlePsdkState", "  tts_speed: " + speakerInfo.getTtsSpeed());
                        }
                    });


                    // 监听PSDK探照灯状态信息
                    drone.getPsdkControlModule().listenDrcPsdkStateSearchlightInfo(psdkIndex, new FlightHubCallback.PsdkStateSearchlightInfoCallback() {
                        @Override
                        public void onResult(PsdkStateSearchlightInfo searchlightInfo) {
                            log("INFO", "handlePsdkState", "PSDK探照灯状态信息:");
                            log("INFO", "handlePsdkState", "  work_mode: " + searchlightInfo.getWorkMode()
                                    + " (code=" + searchlightInfo.getWorkMode().getValue() + ")");
                            log("INFO", "handlePsdkState", "  brightness: " + searchlightInfo.getBrightness());
                            log("INFO", "handlePsdkState", "  calibration_status: " + searchlightInfo.getCalibrationStatus());
                            log("INFO", "handlePsdkState", "  calibration_progress: " + searchlightInfo.getCalibrationProgress());
                            log("INFO", "handlePsdkState", "  left_value: " + searchlightInfo.getLeftValue());
                            log("INFO", "handlePsdkState", "  right_value: " + searchlightInfo.getRightValue());
                        }
                    });


                    // 监听PSDK喊话器播放进度信息
                    drone.getPsdkControlModule().listenDrcPsdkSpeakerPlayProgressInfo(psdkIndex, new FlightHubCallback.PsdkSpeakerPlayProgressInfoCallback() {
                        @Override
                        public void onResult(PsdkSpeakerPlayProgressInfo playProgressInfo) {
                            log("INFO", "handlePsdkState", "PSDK喊话器播放进度信息:");
                            log("INFO", "handlePsdkState", "  psdk_index: " + playProgressInfo.getPsdkIndex());
                            log("INFO", "handlePsdkState", "  result: " + playProgressInfo.getResult());
                            log("INFO", "handlePsdkState", "  status: " + playProgressInfo.getStatus());
                            log("INFO", "handlePsdkState", "  step_key: " + playProgressInfo.getStepKey());
                            log("INFO", "handlePsdkState", "  percent: " + playProgressInfo.getPercent());
                            log("INFO", "handlePsdkState", "  md5: " + playProgressInfo.getMd5());
                        }
                    });


                    // 监听PSDK浮动窗口文本信息
                    drone.getPsdkControlModule().listenDrcPsdkFloatingWindowTextInfo(psdkIndex, new FlightHubCallback.PsdkFloatingWindowTextInfoCallback() {
                        @Override
                        public void onResult(PsdkFloatingWindowTextInfo floatingWindowTextInfo) {
                            log("INFO", "handlePsdkState", "PSDK浮动窗口文本信息:");
                            log("INFO", "handlePsdkState", "  psdk_index: " + floatingWindowTextInfo.getPsdkIndex());
                            log("INFO", "handlePsdkState", "  floating_window_text: " + floatingWindowTextInfo.getFloatingWindowText());
                        }
                    });
                }
            }
        });

        log("INFO", "handlePsdkState", "PSDK状态监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器重新播放功能
     */
    private static void handlePsdkSpeakerReplay() {
        printSeparator("PSDK喊话器重新播放");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSpeakerReplay", "该设备没有无人机，无法执行喊话器重新播放");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSpeakerReplay", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkSpeakerReplay", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkSpeakerReplay", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSpeakerReplay", "没有找到PSDK设备(PsdkDeviceList is empty)");
                    return;
                }


                // 监听 PSDK 设备信息;
                // 注意: 从 PSDK 设备列表中找到 目标设备[喊话器];
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkSpeakerReplay", "PSDK设备[" + i + "] PsdkDeviceInfo: " +
                        "{ psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey() + " }"
                    );

                    int psdkIndex = psdkDeviceList.getDeviceInfos().get(i).getPsdkIndex();
                    log("INFO", "handlePsdkSpeakerReplay", "当前使用PSDK设备索引: " + psdkIndex);

                    // 监听PSDK状态摘要信息
                    drone.getPsdkControlModule().listenDrcPsdkStateSummaryInfo(psdkIndex, new FlightHubCallback.PsdkStateSummaryInfoCallback() {
                        @Override
                        public void onResult(PsdkStateSummaryInfo summaryInfo) {
                            log("INFO", "handlePsdkSpeakerReplay", "PSDK状态摘要信息(PsdkStateSummaryInfo): ");
                            log("INFO", "handlePsdkSpeakerReplay", "{");
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_index: " + summaryInfo.getPsdkIndex());
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_type: " + summaryInfo.getPsdkType() +
                                ", PsdkType { UNOFFICIAL=4, 第三方设备; OFFICIAL=5, 官方设备; MANIFOLD3=8, 妙算 3; UNKNOWN=0xFFFF, Unknown }"
                            );
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_sub_type: " + summaryInfo.getPsdkSubType() +
                                ", PsdkSubType { SPEAKER=0, 喊话器 AS1; SEARCHLIGHT=1, 探照灯 AL1; UNKNOWN=0xFFFF, Unknown }"
                            );
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_name: " + summaryInfo.getPsdkName());
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_sn: " + summaryInfo.getPsdkSn());
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_version: " + summaryInfo.getPsdkVersion());
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_lib_version: " + summaryInfo.getPsdkLibVersion());
                            log("INFO", "handlePsdkSpeakerReplay", "    psdk_restrict_state: " + summaryInfo.getPsdkRestrictState());
                            log("INFO", "handlePsdkSpeakerReplay", "}");

                            // 筛选喊话器;
                            int index = summaryInfo.getPsdkIndex();
                            if (summaryInfo.getPsdkSubType() == PsdkSubType.SPEAKER) {
                                // 执行喊话器重新播放
                                drone.getPsdkControlModule().speakerReplay(index, new FlightHubCallback.ResultCallbackWithError() {
                                    @Override
                                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                        if (error.isSuccess()) {
                                            log("INFO", "handlePsdkSpeakerReplay", "喊话器重新播放成功");
                                        } else {
                                            log("ERROR", "handlePsdkSpeakerReplay", "喊话器重新播放失败，错误码: " + error);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        log("INFO", "handlePsdkSpeakerReplay", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器停止播放功能
     */
    private static void handlePsdkSpeakerPlayStop() {
        printSeparator("PSDK喊话器停止播放");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSpeakerPlayStop", "该设备没有无人机，无法执行喊话器停止播放");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSpeakerPlayStop", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkSpeakerPlayStop", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkSpeakerPlayStop", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkSpeakerPlayStop", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSpeakerPlayStop", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkSpeakerPlayStop", "使用PSDK设备索引: " + psdkIndex);

                // 执行喊话器停止播放
                drone.getPsdkControlModule().speakerPlayStop(psdkIndex, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkSpeakerPlayStop", "喊话器停止播放成功");
                        } else {
                            log("ERROR", "handlePsdkSpeakerPlayStop", "喊话器停止播放失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkSpeakerPlayStop", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器音量设置功能
     */
    private static void handlePsdkSpeakerPlayVolumeSet() {
        printSeparator("PSDK喊话器音量设置");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSpeakerPlayVolumeSet", "该设备没有无人机，无法执行喊话器音量设置");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSpeakerPlayVolumeSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkSpeakerPlayVolumeSet", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkSpeakerPlayVolumeSet", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkSpeakerPlayVolumeSet", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSpeakerPlayVolumeSet", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkSpeakerPlayVolumeSet", "使用PSDK设备索引: " + psdkIndex);
                log("WARN", "handlePsdkSpeakerPlayVolumeSet", "请保证当前的 index 对应负载配件是 [喊话器] !");

                // 执行喊话器音量设置（设置为1）
                drone.getPsdkControlModule().speakerPlayVolumeSet(psdkIndex, 1, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkSpeakerPlayVolumeSet", "喊话器音量设置成功");
                        } else {
                            log("ERROR", "handlePsdkSpeakerPlayVolumeSet", "喊话器音量设置失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkSpeakerPlayVolumeSet", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器TTS设置功能
     */
    private static void handlePsdkSpeakerTTSSet() {
        printSeparator("PSDK喊话器TTS设置");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSpeakerTTSSet", "该设备没有无人机，无法执行喊话器TTS设置");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSpeakerTTSSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkSpeakerTTSSet", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkSpeakerTTSSet", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkSpeakerTTSSet", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSpeakerTTSSet", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkSpeakerTTSSet", "使用PSDK设备索引: " + psdkIndex);

                // 创建TTS配置
                PsdkSpeakerTTSconfig ttsConfig = new PsdkSpeakerTTSconfig();
                ttsConfig.setVolume(50);
                ttsConfig.setType(MegaphoneTTSVoiceType.WOMAN);
                ttsConfig.setLanguage(MegaphoneTTSLanguageType.CHINESE);
                ttsConfig.setSpeed(50);

                // 执行喊话器TTS设置
                drone.getPsdkControlModule().speakerTTSSet(psdkIndex, ttsConfig, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkSpeakerTTSSet", "喊话器TTS设置成功");
                        } else {
                            log("ERROR", "handlePsdkSpeakerTTSSet", "喊话器TTS设置失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkSpeakerTTSSet", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器播放模式设置功能
     */
    private static void handlePsdkSpeakerPlayModeSet() {
        printSeparator("PSDK喊话器播放模式设置");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSpeakerPlayModeSet", "该设备没有无人机，无法执行喊话器播放模式设置");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSpeakerPlayModeSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkSpeakerPlayModeSet", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkSpeakerPlayModeSet", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkSpeakerPlayModeSet", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSpeakerPlayModeSet", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkSpeakerPlayModeSet", "使用PSDK设备索引: " + psdkIndex);

                // 执行喊话器播放模式设置（循环播放）
                drone.getPsdkControlModule().speakerPlayModeSet(psdkIndex, MegaphonePlayMode.LOOP_PLAYBACK, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkSpeakerPlayModeSet", "喊话器播放模式设置成功");
                        } else {
                            log("ERROR", "handlePsdkSpeakerPlayModeSet", "喊话器播放模式设置失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkSpeakerPlayModeSet", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器TTS播放功能
     */
    private static void handlePsdkSpeakerTTSPlayStart() {
        printSeparator("PSDK喊话器TTS播放");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSpeakerTTSPlayStart", "该设备没有无人机，无法执行喊话器TTS播放");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSpeakerTTSPlayStart", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkSpeakerTTSPlayStart", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkSpeakerTTSPlayStart", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkSpeakerTTSPlayStart", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSpeakerTTSPlayStart", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkSpeakerTTSPlayStart", "使用PSDK设备索引: " + psdkIndex);

                // 创建TTS播放信息
                PsdkSpeakerTTSPlayInfo ttsPlayInfo = new PsdkSpeakerTTSPlayInfo();
                ttsPlayInfo.setName("来自Java的TTS文本转语音测试");
                ttsPlayInfo.setText("这是一段测试文本，用于验证Java SDK的TTS功能。测试1，测试2，测试3，测试4，测试5，测试6，测试7，测试8，测试9，测试10。");
                ttsPlayInfo.setMd5("d81ba3d982c189e06d99f651a25eecf1");

                // 执行喊话器TTS播放
                drone.getPsdkControlModule().speakerTTSPlayStart(psdkIndex, ttsPlayInfo, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkSpeakerTTSPlayStart", "喊话器TTS播放成功");
                        } else {
                            log("ERROR", "handlePsdkSpeakerTTSPlayStart", "喊话器TTS播放失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkSpeakerTTSPlayStart", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器文件播放功能
     */
    private static void handlePsdkSpeakerAudioPlayStart() {
        printSeparator("PSDK喊话器文件播放");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSpeakerAudioPlayStart", "该设备没有无人机，无法执行喊话器文件播放");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSpeakerAudioPlayStart", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkSpeakerAudioPlayStart", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkSpeakerAudioPlayStart", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkSpeakerAudioPlayStart", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSpeakerAudioPlayStart", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkSpeakerAudioPlayStart", "使用PSDK设备索引: " + psdkIndex);

                // 创建音频播放信息
                PsdkSpeakerAudioPlayInfo audioPlayInfo = new PsdkSpeakerAudioPlayInfo();
                audioPlayInfo.setName("千千阙歌2");
                audioPlayInfo.setUrl("http://xx.xx.xx.xx/path/to/audio.pcm");
                audioPlayInfo.setMd5("PLACEHOLDER_MD5");
                audioPlayInfo.setFormat("pcm");

                // 执行喊话器文件播放
                drone.getPsdkControlModule().speakerAudioPlayStart(psdkIndex, audioPlayInfo, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkSpeakerAudioPlayStart", "喊话器文件播放成功");
                        } else {
                            log("ERROR", "handlePsdkSpeakerAudioPlayStart", "喊话器文件播放失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkSpeakerAudioPlayStart", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK控件发送文本框内容功能
     */
    private static void handlePsdkWidgetInputBoxTextSet() {
        printSeparator("PSDK控件发送文本框内容");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkWidgetInputBoxTextSet", "该设备没有无人机，无法执行控件发送文本框内容");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkWidgetInputBoxTextSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkWidgetInputBoxTextSet", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkWidgetInputBoxTextSet", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkWidgetInputBoxTextSet", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkWidgetInputBoxTextSet", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkWidgetInputBoxTextSet", "使用PSDK设备索引: " + psdkIndex);

                // 执行控件发送文本框内容
                drone.getPsdkControlModule().widgetInputBoxTextSet(psdkIndex, "this is a test text", new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkWidgetInputBoxTextSet", "控件发送文本框内容成功");
                        } else {
                            log("ERROR", "handlePsdkWidgetInputBoxTextSet", "控件发送文本框内容失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkWidgetInputBoxTextSet", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK控件设置widget值功能
     */
    private static void handlePsdkWidgetValueSet() {
        printSeparator("PSDK控件设置widget值");

        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkWidgetValueSet", "该设备没有无人机，无法执行控件设置widget值");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkWidgetValueSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handlePsdkWidgetValueSet", "开始监听PSDK设备列表...");

        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                log("INFO", "handlePsdkWidgetValueSet", "PSDK设备列表大小: " + psdkDeviceList.getDeviceInfos().size());
                
                for (int i = 0; i < psdkDeviceList.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo deviceInfo = psdkDeviceList.getDeviceInfos().get(i);
                    log("INFO", "handlePsdkWidgetValueSet", "PSDK设备[" + i + "] psdk_index: " + deviceInfo.getPsdkIndex() + 
                        ", object_key: " + deviceInfo.getObjectKey());
                }

                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkWidgetValueSet", "没有找到PSDK设备");
                    return;
                }

                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                log("INFO", "handlePsdkWidgetValueSet", "使用PSDK设备索引: " + psdkIndex);

                // 执行控件设置widget值
                int widgetIndex = 3;
                int widgetValue = 93;
                drone.getPsdkControlModule().widgetValueSet(psdkIndex, widgetIndex, widgetValue, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        if (error.isSuccess()) {
                            log("INFO", "handlePsdkWidgetValueSet", "控件设置widget值成功");
                        } else {
                            log("ERROR", "handlePsdkWidgetValueSet", "控件设置widget值失败，错误码: " + error);
                        }
                    }
                });
            }
        });

        log("INFO", "handlePsdkWidgetValueSet", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器创建语音/文本资源功能
     */
    private static void handleCreatePsdkSpeakerResource() {
        printSeparator("PSDK喊话器创建语音/文本资源");

        if (workspace == null) {
            log("ERROR", "handleCreatePsdkSpeakerResource", "获取工作区实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleCreatePsdkSpeakerResource", "开始创建PSDK喊话器资源...");

        // 创建Audio资源
        // CreatePsdkSpeakerResourceParams params = new CreatePsdkSpeakerResourceParams();
        // params.setName("千千阙歌2");
        // params.setFile_type("audio");
        // params.setObject_key("41906993-d508-44ef-b9e4-36e33479a5f2/3b6752a4-85ae-4aba-bbe9-db0148166a09/qqqg.mp3");
        // params.setDuration(300);
        // params.setContent("");
        // params.setCreate_scene("import");
        // params.setNeed_opus(true);

        // 创建Audio资源
        CreatePsdkSpeakerResourceParams params = new CreatePsdkSpeakerResourceParams();
        params.setName("java sdk TTS demo");
        params.setFileType("tts");
        params.setObjectKey("");
        params.setDuration(0);
        params.setContent("这是喊话器 TTS 测试文本, 用于验证喊话器 TTS 功能。");
        params.setCreateScene("import");
        params.setNeedOpus(false);
        params.setSize(1024000);
        params.setVisible(true);
        log("INFO", "handleCreatePsdkSpeakerResource", "size=" + params.getSize() + ", visible=" + params.isVisible());

        workspace.getPsdkResourceModule().createPsdkSpeakerResource(params, new FlightHubCallback.PsdkSpeakerResourceCallback() {
            @Override
            public void onResult(FlightHubError error, PsdkSpeakerResource resource) {
                if (error.isSuccess()) {
                    log("INFO", "handleCreatePsdkSpeakerResource", "创建PSDK喊话器资源成功:");
                    log("INFO", "handleCreatePsdkSpeakerResource", "  ID: " + resource.getId());
                    log("INFO", "handleCreatePsdkSpeakerResource", "  名称: " + resource.getName());
                    log("INFO", "handleCreatePsdkSpeakerResource", "  文件类型: " + resource.getFileType());
                    log("INFO", "handleCreatePsdkSpeakerResource", "  状态: " + resource.getState());
                    log("INFO", "handleCreatePsdkSpeakerResource", "  opus_object_key: " + resource.getOpusObjectKey());
                    log("INFO", "handleCreatePsdkSpeakerResource", "  opus_size: " + resource.getOpusSize());
                    log("INFO", "handleCreatePsdkSpeakerResource", "  opus_md5_sum: " + resource.getOpusMd5Sum());
                } else {
                    log("ERROR", "handleCreatePsdkSpeakerResource", "创建PSDK喊话器资源失败，错误: " + error);
                }
            }
        });

        log("INFO", "handleCreatePsdkSpeakerResource", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器修改语音/文本资源功能
     */
    private static void handleEditPsdkSpeakerResource() {
        printSeparator("PSDK喊话器修改语音/文本资源");

        if (workspace == null) {
            log("ERROR", "handleEditPsdkSpeakerResource", "获取工作区实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleEditPsdkSpeakerResource", "开始修改PSDK喊话器资源...");

        // 修改TTS文本资源（需要先有一个存在的资源ID）
        EditPsdkSpeakerResourceParams params = new EditPsdkSpeakerResourceParams();
        params.setId("2");  // 这里需要替换为实际存在的资源ID
        params.setName("修改后的TTS资源");
        params.setFileType("tts");
        params.setContent("这是修改后的测试文本，用于验证修改TTS资源功能。");
        params.setVisible(false);  // TTS资源不需要visible字段

        workspace.getPsdkResourceModule().editPsdkSpeakerResource(params, new FlightHubCallback.PsdkSpeakerResourceCallback() {
            @Override
            public void onResult(FlightHubError error, PsdkSpeakerResource resource) {
                if (error.isSuccess()) {
                    log("INFO", "handleEditPsdkSpeakerResource", "修改PSDK喊话器资源成功:");
                    log("INFO", "handleEditPsdkSpeakerResource", "  ID: " + resource.getId());
                    log("INFO", "handleEditPsdkSpeakerResource", "  名称: " + resource.getName());
                    log("INFO", "handleEditPsdkSpeakerResource", "  文件类型: " + resource.getFileType());
                    log("INFO", "handleEditPsdkSpeakerResource", "  状态: " + resource.getState());
                } else {
                    log("ERROR", "handleEditPsdkSpeakerResource", "修改PSDK喊话器资源失败，错误: " + error);
                }
            }
        });

        log("INFO", "handleEditPsdkSpeakerResource", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器删除语音/文本资源功能
     */
    private static void handleDeletePsdkSpeakerResource() {
        printSeparator("PSDK喊话器删除语音/文本资源");

        if (workspace == null) {
            log("ERROR", "handleDeletePsdkSpeakerResource", "获取工作区实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDeletePsdkSpeakerResource", "开始删除PSDK喊话器资源...");

        int resourceId = 2;  // 这里需要替换为实际存在的资源ID

        workspace.getPsdkResourceModule().deletePsdkSpeakerResource(resourceId, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (error.isSuccess()) {
                    log("INFO", "handleDeletePsdkSpeakerResource", "删除PSDK喊话器资源成功");
                } else {
                    log("ERROR", "handleDeletePsdkSpeakerResource", "删除PSDK喊话器资源失败，错误码: " + error);
                }
            }
        });

        log("INFO", "handleDeletePsdkSpeakerResource", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器查询列表语音/文本资源功能
     */
    private static void handleGetPsdkSpeakerResourceList() {
        printSeparator("PSDK喊话器查询列表语音/文本资源");

        if (workspace == null) {
            log("ERROR", "handleGetPsdkSpeakerResourceList", "获取工作区实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleGetPsdkSpeakerResourceList", "开始查询PSDK喊话器资源列表...");

        workspace.getPsdkResourceModule().getPsdkSpeakerResourceList(new FlightHubCallback.PsdkSpeakerResourceListCallback() {
            @Override
            public void onResult(FlightHubError error, PsdkSpeakerResourceList resourceList) {
                if (error.isSuccess()) {
                    log("INFO", "handleGetPsdkSpeakerResourceList", "查询PSDK喊话器资源列表成功，共 " + resourceList.getResources().size() + " 个资源:");
                    for (int i = 0; i < resourceList.getResources().size(); i++) {
                        PsdkSpeakerResource resource = resourceList.getResources().get(i);
                        log("INFO", "handleGetPsdkSpeakerResourceList", "  资源[" + i + "]:");
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    ID: " + resource.getId());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    名称: " + resource.getName());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    文件类型: " + resource.getFileType());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    状态: " + resource.getState());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    pcm_object_url: " + resource.getPcmObjectUrl());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    md5: " + resource.getPcmMd5Sum());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    opus_object_key: " + resource.getOpusObjectKey());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    opus_size: " + resource.getOpusSize());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    opus_md5_sum: " + resource.getOpusMd5Sum());
                        log("INFO", "handleGetPsdkSpeakerResourceList", "    创建时间: " + resource.getCreatedAt());
                    }
                } else {
                    log("ERROR", "handleGetPsdkSpeakerResourceList", "查询PSDK喊话器资源列表失败，错误: " + error);
                }
            }
        });

        log("INFO", "handleGetPsdkSpeakerResourceList", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器获取临时上传凭证功能
     */
    private static void handleGetSTS() {
        printSeparator("PSDK喊话器获取临时上传凭证");

        if (workspace == null) {
            log("ERROR", "handleGetSTS", "获取工作区实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleGetSTS", "开始获取临时上传凭证...");

        workspace.getPsdkResourceModule().getSTS(new FlightHubCallback.STSInfoCallback() {
            @Override
            public void onResult(FlightHubError error, STSInfo stsInfo) {
                if (error.isSuccess()) {
                    log("INFO", "handleGetSTS", "获取临时上传凭证成功:");
                    log("INFO", "handleGetSTS", "  对象前缀: " + stsInfo.getObjectKeyPrefix());
                    log("INFO", "handleGetSTS", "  存储桶: " + stsInfo.getBucket());
                    log("INFO", "handleGetSTS", "  端点: " + stsInfo.getEndpoint());
                    log("INFO", "handleGetSTS", "  提供商: " + stsInfo.getProvider());
                    log("INFO", "handleGetSTS", "  区域: " + stsInfo.getRegion());
                    log("INFO", "handleGetSTS", "  访问密钥ID: " + stsInfo.getCredentials().getAccessKeyId());
                    log("INFO", "handleGetSTS", "  访问密钥Secret: " + stsInfo.getCredentials().getAccessKeySecret());
                    log("INFO", "handleGetSTS", "  过期时间: " + stsInfo.getCredentials().getExpire());
                    log("INFO", "handleGetSTS", "  安全令牌: " + stsInfo.getCredentials().getSecurityToken());
                } else {
                    log("ERROR", "handleGetSTS", "获取临时上传凭证失败，错误: " + error);
                }
            }
        });

        log("INFO", "handleGetSTS", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK获取控件资源包索引功能
     */
    private static void handleGetPsdkWidgetResources() {
        printSeparator("PSDK获取控件资源包索引");

        if (workspace == null) {
            log("ERROR", "handleGetPsdkWidgetResources", "获取工作区实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleGetPsdkWidgetResources", "开始获取PSDK控件资源包索引...");

        GetPsdkWidgetResourceParams params = new GetPsdkWidgetResourceParams();
        // object_key 格式: <资源 UUID>/<设备 SN>_<yyyy_MM_dd_HH_mm_ss>_widget
        params.setObjectKeys(java.util.Collections.singletonList("YOUR_OBJECT_KEY"));

        workspace.getPsdkResourceModule().getPsdkWidgetResources(params, new FlightHubCallback.PsdkWidgetResourceListCallback() {
            @Override
            public void onResult(FlightHubError error, PsdkWidgetResourceList psdkWidgetResourceList) {
                if (error.isSuccess()) {
                    log("INFO", "handleGetPsdkWidgetResources", "获取PSDK控件资源包索引成功，共 " + psdkWidgetResourceList.getUiResourceUrls().size() + " 个PSDK控件资源:");
                    for (int i = 0; i < psdkWidgetResourceList.getUiResourceUrls().size(); i++) {
                        PsdkWidgetResource resource = psdkWidgetResourceList.getUiResourceUrls().get(i);
                        log("INFO", "handleGetPsdkWidgetResources", "  资源[" + i + "]:");
                        log("INFO", "handleGetPsdkWidgetResources", "    对象键: " + resource.getObjectKey());
                        log("INFO", "handleGetPsdkWidgetResources", "    URL: " + resource.getUrl());
                    }
                } else {
                    log("ERROR", "handleGetPsdkWidgetResources", "获取PSDK控件资源包索引失败，错误: " + error);
                }
            }
        });

        log("INFO", "handleGetPsdkWidgetResources", "按Enter键返回...");
        waitForKeyPress();
    }

    /**
     * 处理PSDK喊话器上传音频文件：先 getSTS，再 upload（默认 /tmp/qqqg.mp3）
     */
    private static void handleUploadSpeakerAudioFile() {
        printSeparator("PSDK喊话器上传音频文件");

        if (workspace == null) {
            log("ERROR", "handleUploadSpeakerAudioFile", "获取工作区实例失败");
            waitForKeyPress();
            return;
        }

        final String testFilePath = "/tmp/qqqg.mp3";
        java.io.File f = new java.io.File(testFilePath);
        if (!f.exists()) {
            log("ERROR", "handleUploadSpeakerAudioFile", "本地文件不存在: " + testFilePath + "（请先 cp 喊话器/qqqg.mp3）");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleUploadSpeakerAudioFile", "本地文件: " + testFilePath + " (" + f.length() + " bytes)");
        log("INFO", "handleUploadSpeakerAudioFile", "先 getSTS，再 upload...");

        workspace.getPsdkResourceModule().getSTS(new FlightHubCallback.STSInfoCallback() {
            @Override
            public void onResult(FlightHubError error, STSInfo stsInfo) {
                if (!error.isSuccess() || stsInfo == null) {
                    log("ERROR", "handleUploadSpeakerAudioFile", "getSTS 失败: " + error);
                    return;
                }
                log("INFO", "handleUploadSpeakerAudioFile", "getSTS 成功: bucket=" + stsInfo.getBucket()
                        + ", endpoint=" + stsInfo.getEndpoint()
                        + ", provider=" + stsInfo.getProvider()
                        + ", prefix=" + stsInfo.getObjectKeyPrefix());

                workspace.getPsdkResourceModule().uploadSpeakerAudioFile(testFilePath, stsInfo,
                        new FlightHubCallback.ResultCallbackWithError() {
                            @Override
                            public void onResult(com.dji.flighthubsdk.bean.FlightHubError upErr) {
                                if (upErr.isSuccess()) {
                                    String prefix = stsInfo.getObjectKeyPrefix();
                                    if (prefix == null) prefix = "";
                                    if (!prefix.isEmpty() && !prefix.endsWith("/")) prefix += "/";
                                    String objectKey = prefix + f.getName();
                                    log("SUCCESS", "handleUploadSpeakerAudioFile", "✅ 音频文件上传成功！ object_key=" + objectKey);
                                } else {
                                    log("ERROR", "handleUploadSpeakerAudioFile", "上传失败: " + upErr);
                                }
                            }
                        });
            }
        });

        log("INFO", "handleUploadSpeakerAudioFile", "按Enter键返回（请等待上传回调打印完成）...");
        waitForKeyPress();
    }

    /**
     * 日志输出
     */
    private static void log(String level, String method, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String threadId = String.format("0x%x", Thread.currentThread().getId());
        String logMessage = String.format("[%s] %s [%s:%s] [%s::%s] %s", 
            timestamp, level, threadId, "FlightHubSDKDemo", "FlightHubSDKDemo", method, message);
        System.out.println(logMessage);
    }

    // ==================== 红外增益模式相关Demo功能 ====================

    /**
     * 监听直播镜头信息
     */
    private static void handleListenLiveVideoInfo() {
        printSeparator("监听直播镜头信息");
        
        if (drone == null) {
            log("ERROR", "handleListenLiveVideoInfo", "❌ 无人机未初始化");
            return;
        }

        // 取消之前的监听
        if (liveVideoInfoListenId != -1) {
            drone.getCameraControlModule().cancelListenDrcLiveVideoInfo(targetPayloadIndex, liveVideoInfoListenId);
            liveVideoInfoListenId = -1;
        }

        log("INFO", "handleListenLiveVideoInfo", "开始监听直播镜头信息...");
        liveVideoInfoListenId = drone.getCameraControlModule().listenDrcLiveVideoInfo(targetPayloadIndex, new FlightHubCallback.LiveVideoInfoCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.LiveVideoInfo info) {
                log("INFO", "handleListenLiveVideoInfo", "📹 直播镜头信息更新:");
                log("INFO", "handleListenLiveVideoInfo", "  载荷索引: " + info.getPayloadIndex());
                log("INFO", "handleListenLiveVideoInfo", "  视频索引: " + info.getVideoIndex());
                log("INFO", "handleListenLiveVideoInfo", "  镜头类型: " + cameraLensTypeToStringFromSwig(info.getVideoTypeSwig()));
                log("INFO", "handleListenLiveVideoInfo", "  可切换镜头类型: " + switchableVideoTypesSwigToString(info.getSwitchableVideoTypeSwigValues()));
            }
        });

        if (liveVideoInfoListenId != -1) {
            log("INFO", "handleListenLiveVideoInfo", "✅ 监听直播镜头信息成功，监听ID: " + liveVideoInfoListenId);
        } else {
            log("ERROR", "handleListenLiveVideoInfo", "❌ 监听直播镜头信息失败");
        }

        waitForKeyPress();
    }

    /**
     * 监听红外增益模式
     */
    private static void handleListenThermalGainMode() {
        printSeparator("监听红外增益模式");
        
        if (drone == null) {
            log("ERROR", "handleListenThermalGainMode", "❌ 无人机未初始化");
            return;
        }

        // 取消之前的监听
        if (thermalGainModeListenId != -1) {
            drone.getCameraControlModule().cancelListenDrcThermalGainMode(targetPayloadIndex, thermalGainModeListenId);
            thermalGainModeListenId = -1;
        }

        log("INFO", "handleListenThermalGainMode", "开始监听红外增益模式...");
        
        thermalGainModeListenId = drone.getCameraControlModule().listenDrcThermalGainMode(targetPayloadIndex, new FlightHubCallback.ThermalGainModeInfoCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.ThermalGainModeInfo info) {
                log("INFO", "handleListenThermalGainMode", "🔥 红外增益模式更新:");
                log("INFO", "handleListenThermalGainMode", "  载荷索引: " + info.getPayloadIndex());
                log("INFO", "handleListenThermalGainMode", "  增益模式: " + irGainModeToStringFromSwig(info.getThermalGainModeSwig()));
            }
        });

        if (thermalGainModeListenId != -1) {
            log("INFO", "handleListenThermalGainMode", "✅ 监听红外增益模式成功，监听ID: " + thermalGainModeListenId);
        } else {
            log("ERROR", "handleListenThermalGainMode", "❌ 监听红外增益模式失败");
        }

        waitForKeyPress();
    }

    /**
     * 设置红外增益模式
     */
    private static void handleSetThermalGainMode() {
        printSeparator("设置红外增益模式");
        
        if (drone == null) {
            log("ERROR", "handleSetThermalGainMode", "❌ 无人机未初始化");
            return;
        }

        System.out.println("请选择红外增益模式:");
        System.out.println("  0: 自动");
        System.out.println("  1: 低增益");
        System.out.println("  2: 高增益");
        System.out.println("  3: 超清模式");
        System.out.print("请输入选择 (0-3): ");
        
        int choice = getUserChoice(0, 3);
        IrGainMode gainMode = IrGainMode.fromValue(choice);
        
        com.dji.flighthubsdk.bean.ThermalGainModeSetParams params = new com.dji.flighthubsdk.bean.ThermalGainModeSetParams();
        params.setPayloadIndex(targetPayloadIndex);
        params.setThermalGainMode(gainMode);
        
        log("INFO", "handleSetThermalGainMode", "正在设置红外增益模式为: " + irGainModeToString(gainMode) + " (" + choice + ")");
        log("INFO", "handleSetThermalGainMode", "注意：只有当前镜头类型为IR时才能设置红外增益模式");
        
        drone.getCameraControlModule().drcSetThermalGainMode(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (error.isSuccess()) {
                    log("INFO", "handleSetThermalGainMode", "✅ 红外增益模式设置成功！");
                } else {
                    log("ERROR", "handleSetThermalGainMode", "❌ 红外增益模式设置失败，错误码: " + error);
                    if (error.getErrorCode() == FlightHubSdkErrorCode.ERROR_INVALID_STATE) {
                        log("ERROR", "handleSetThermalGainMode", "   可能原因：当前镜头类型不是IR，请先切换到IR镜头");
                    }
                }
            }
        });
        
        log("INFO", "handleSetThermalGainMode", "等待设置完成，按Enter键返回...");
        waitForKeyPress();
    }

    // ==================== 辅助方法 ====================

    /**
     * 镜头类型枚举转字符串
     */
    private static String cameraLensTypeToString(CameraLensType lensType) {
        if (lensType == null) {
            return "未知镜头";
        }
        switch (lensType) {
            case WIDE:
                return "广角镜头";
            case ZOOM:
                return "变焦镜头";
            case IR:
                return "红外镜头";
            case THERMAL:
                return "热成像镜头";
            case NORMAL:
                return "普通镜头";
            case UNKNOWN:
            default:
                return "未知镜头";
        }
    }

    /**
     * 红外增益模式枚举转字符串
     */
    private static String irGainModeToString(IrGainMode gainMode) {
        if (gainMode == null) {
            return "未知模式";
        }
        switch (gainMode) {
            case Auto:
                return "自动";
            case LowGain:
                return "低增益";
            case HighGain:
                return "高增益";
            case UltraHD:
                return "超清模式";
            default:
                return "未知模式";
        }
    }

    private static String cameraLensTypeToStringFromSwig(int swig) {
        return cameraLensTypeToString(CameraLensType.fromValue(swig));
    }

    private static String switchableVideoTypesSwigToString(java.util.List<Integer> types) {
        if (types == null || types.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Integer v = types.get(i);
            sb.append(cameraLensTypeToStringFromSwig(v != null ? v : 0));
        }
        sb.append("]");
        return sb.toString();
    }

    private static void logLiveInfoDetails(String tag, LiveInfo range) {
        log("INFO", tag, "  payload_index: " + range.getPayloadIndex()
                + ", device_sn: " + range.getDeviceSn()
                + ", video_id: " + range.getVideoId());
        log("INFO", tag, "  switchable_lens_types(raw): " + range.getSwitchableLensTypeSwigValues());
        log("INFO", tag, "  switchable_lens_types: " + switchableVideoTypesSwigToString(range.getSwitchableLensTypeSwigValues()));
    }

    private static String irGainModeToStringFromSwig(int swig) {
        return irGainModeToString(IrGainMode.fromValue(swig));
    }

    // ==================== 飞行任务异常和设备变更相关处理函数 ====================

    /**
     * 监听飞行任务异常
     */
    private static void handleListenFlightTasksException() {
        if (selectedDevice == null) {
            System.out.println("请先选择设备");
            return;
        }

        if (!hasSubDevice(selectedDevice)) {
            System.out.println("该设备没有无人机，无法监听飞行任务异常");
            return;
        }

        String subDeviceSn = getSubDeviceSn(selectedDevice);
        Drone drone = getOrCreateDrone(subDeviceSn);
        if (drone == null) {
            System.out.println("获取无人机实例失败");
            return;
        }

        System.out.println("正在监听飞行任务异常...");
        flightTaskExceptionListenId = drone.getDroneOsdModule().listenFlightTasksException(new FlightHubCallback.FlightTaskExceptionCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightTaskException exception) {
                System.out.println("收到飞行任务异常信息:");
                System.out.println("  uuid: " + exception.getUuid());
                System.out.println("  name: " + exception.getName());
                System.out.println("  sn: " + exception.getSn());
                System.out.println("  task_type: " + exception.getTaskType());
                System.out.println("  status: " + exception.getStatus());
                System.out.println("  progress: " + exception.getProgress() + "%");
                System.out.println("  timestamp: " + exception.getTimestamp());
                java.util.List<com.dji.flighthubsdk.bean.FlightTaskExceptionDetail> exceptions = exception.getExceptions();
                System.out.println("  exceptions_count: " + exceptions.size());
                
                if (!exceptions.isEmpty()) {
                    System.out.println("  异常详情:");
                    for (int i = 0; i < exceptions.size(); i++) {
                        com.dji.flighthubsdk.bean.FlightTaskExceptionDetail detail = exceptions.get(i);
                        System.out.println("    [" + i + "] code: " + detail.getCode() 
                                         + ", message: " + detail.getMessage() 
                                         + ", task_id: " + detail.getTaskId() 
                                         + ", happen_at: " + detail.getHappenAt() 
                                         + ", sn: " + detail.getSn());
                    }
                }
                System.out.println("---");
            }
        });

        System.out.println("飞行任务异常监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    /**
     * [89] 线程堵塞测试：调用 getDeviceList，回调中 sleep，用于验证 BlockingDetector（仅 C++/Native 侧有检测器时生效）
     */
    private static void handleThreadBlockingTest() {
        if (workspace == null) {
            System.out.println("工作空间未初始化");
            return;
        }
        final int blockingSleepSeconds = 60;
        System.out.println("[89] 线程堵塞测试：getDeviceList 回调内 sleep " + blockingSleepSeconds + " 秒，用于验证 BlockingDetector");
        System.out.println("BlockingDetector 会在检测到堵塞时抓堆栈并写入 /tmp/blocking_detector/（仅 Native 侧）");
        workspace.getDeviceManageModule().getDeviceList(new FlightHubCallback.DeviceListCallback() {
            @Override
            public void onResult(java.util.List<DeviceTopo> devices, FlightHubError error) {
                System.out.println("[BlockingTest] 收到 getDeviceList 回调，即将 sleep " + blockingSleepSeconds + " 秒以模拟线程堵塞（Main 线程）...");
                try {
                    Thread.sleep(blockingSleepSeconds * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[BlockingTest] sleep 被中断");
                    return;
                }
                System.out.println("[BlockingTest] sleep 结束");
            }
        });
        System.out.println("getDeviceList 已调用，回调返回后会 sleep，按 Enter 键返回菜单。");
        waitForKeyPress();
    }

    /**
     * 监听工作空间设备变更
     */
    private static void handleListenWorkspaceDeviceChanged() {
        if (workspace == null) {
            System.out.println("工作空间未初始化");
            return;
        }

        System.out.println("正在监听工作空间设备变更...");
        workspaceDeviceChangeListenId = workspace.getDeviceManageModule().listenWorkspaceDeviceChanged(new FlightHubCallback.WorkspaceDeviceChangeCallback() {
            @Override
            public void onResult(WorkspaceDeviceChange change) {
                System.out.println("收到工作空间设备变更信息:");
                List<DeviceChange> deviceChanges = change.getDeviceChanges();
                System.out.println("  device_changes count: " + deviceChanges.size());

                for (int i = 0; i < deviceChanges.size(); i++) {
                    DeviceChange item = deviceChanges.get(i);
                    List<String> changeDevices = item.getChangeDevices();
                    System.out.println("  [" + i + "] change_type: " + item.getChangeType()
                                     + ", change_devices count: " + changeDevices.size());

                    for (int j = 0; j < changeDevices.size(); j++) {
                        System.out.println("    [" + j + "] device: " + changeDevices.get(j));
                    }
                }
                System.out.println("---");
            }
        });

        System.out.println("工作空间设备变更监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    /**
     * 监听设备拓扑变更（Workspace / DeviceManageModule）
     */
    private static void handleListenDeviceTopoChangedDrone() {
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            System.out.println("Workspace 未初始化");
            return;
        }

        System.out.println("正在监听设备拓扑变更（Workspace）...");
        droneDeviceTopoChangeListenId = workspace.getDeviceManageModule().listenDeviceTopoChanged(new FlightHubCallback.DeviceTopoChangeCallback() {
            @Override
            public void onResult(DeviceTopoChange change) {
                System.out.println("收到设备拓扑变更信息:");
                System.out.println("  gateway_sn: " + change.getGatewaySn());
                java.util.List<String> devices = change.getDevices();
                System.out.println("  devices count: " + devices.size());
                
                for (int i = 0; i < devices.size(); i++) {
                    System.out.println("  [" + i + "] device: " + devices.get(i));
                }
                System.out.println("---");
            }
        });

        System.out.println("设备拓扑变更监听已启动，按Enter键停止...");
        waitForKeyPress();
    }

    /**
     * 监听设备拓扑变更（Dock 菜单入口，同 Workspace API）
     */
    private static void handleListenDeviceTopoChangedDock() {
        handleListenDeviceTopoChangedDrone();
    }
    private static void handleCustomSeiTest() {
        log("INFO", "handleCustomSeiTest", "开始自定义 SEI 测试...");

        // 检查设备是否有子设备（无人机）
        if (selectedDevice == null || !hasSubDevice(selectedDevice)) {
            log("ERROR", "handleCustomSeiTest", "该设备没有无人机，无法进行自定义 SEI 测试");
            return;
        }

        // 获取无人机实例
        com.dji.flighthubsdk.core.Drone drone = FlightHubSDK.getDroneInstance(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleCustomSeiTest", "获取无人机实例失败");
            return;
        }

        // 获取 Stream 实例
        com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
        if (stream == null) {
            log("ERROR", "handleCustomSeiTest", "获取 Stream 实例失败");
            return;
        }

        CustomSEIModule customSeiModule = stream.getCustomSEIModule();
        if (customSeiModule == null) {
            log("ERROR", "handleCustomSeiTest", "获取 CustomSEI 模块失败");
            return;
        }

        log("INFO", "handleCustomSeiTest", "开始自定义 SEI 测试...");

        try {
            // 创建视频文件
            String filename = generateTimestampFilename("dji_stream_with_custom_sei", "h264");
            java.io.FileOutputStream outputFile = new java.io.FileOutputStream(filename);
            log("INFO", "handleCustomSeiTest", "创建输出文件: " + filename);

            // 创建自定义 SEI 数据（简单版本）
            java.util.List<com.dji.flighthubsdk.bean.SeiItem> customSeiData = new java.util.ArrayList<>();

            // 1. 添加自定义数据
            java.util.List<Integer> data = java.util.Arrays.asList(1, 2, 3);
            customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofCustomData(new com.dji.flighthubsdk.bean.SeiData(data)));
            log("INFO", "handleCustomSeiTest", "添加自定义数据: 01 02 03");

            // 2. 添加目标检测框（小数值）
            customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofDetectionBox(
                    new com.dji.flighthubsdk.bean.SeiDetectionBox(1, 1, 1, 1, 1, 1, 1, 1)));
            log("INFO", "handleCustomSeiTest", "添加目标检测框: ID=1, 类型=人, 位置=(1,1), 尺寸=1x1, 置信度=1%");

            // 3. 添加多边形检测（4个点）
            java.util.List<com.dji.flighthubsdk.bean.SeiPoint2D> points = java.util.Arrays.asList(
                    new com.dji.flighthubsdk.bean.SeiPoint2D(1, 2),
                    new com.dji.flighthubsdk.bean.SeiPoint2D(3, 4),
                    new com.dji.flighthubsdk.bean.SeiPoint2D(5, 6),
                    new com.dji.flighthubsdk.bean.SeiPoint2D(7, 8));
            customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofDetectionPolygon(
                    new com.dji.flighthubsdk.bean.SeiDetectionPolygon(2, 2, 2, 2, points)));
            log("INFO", "handleCustomSeiTest", "添加多边形检测: ID=2, 类型=车, 4个坐标点");

            // 4. 添加文本叠加（短文本）
            customSeiData.add(com.dji.flighthubsdk.bean.SeiItem.ofTextOverlay(
                    new com.dji.flighthubsdk.bean.SeiTextOverlay(3, 3, 3, 3, 3, 3, 3, 3, "BBBBB")));
            log("INFO", "handleCustomSeiTest", "添加文本叠加: ID=3, 位置=(3,3), 文本=\"BBBBB\"");

            log("INFO", "handleCustomSeiTest", "正在监听无人机的直播可用范围...");

            // 监听无人机的直播范围
            drone.getLiveDeviceModule().listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                @Override
                public void onResult(java.util.List<LiveInfo> ranges) {
                    log("INFO", "handleCustomSeiTest", "无人机直播可用范围: " + ranges.size());
                    if (!ranges.isEmpty()) {
                        log("INFO", "handleCustomSeiTest", "开始拉取无人机码流并添加自定义 SEI...");
                        LiveInfo liveInfo = null;
                        for (LiveInfo range : ranges) {
                            log("INFO", "handleCustomSeiTest", "payload_index: " + range.getPayloadIndex());
                            if (targetPayloadIndex.equals(range.getPayloadIndex())) {
                                liveInfo = range;
                                break;
                            }
                        }
                        
                        if (liveInfo != null) {
                            droneLiveModule = drone.getLiveStream(liveInfo);
                            droneLiveModule.start(new FlightHubCallback.ResultCallbackWithError() {
                                @Override
                                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                    if (error.isSuccess()) {
                                        log("INFO", "handleCustomSeiTest", "无人机直播流启动成功");
                                    } else {
                                        log("ERROR", "handleCustomSeiTest", "无人机直播流启动失败，错误码: " + error);
                                    }
                                }
                            });
                            
                            droneLiveStreamListenId = droneLiveModule.subscribeLiveStream(new FlightHubCallback.LiveStreamCallback() {
                                private int frameCount = 0;
                                private long totalBytes = 0;
                                
                                @Override
                                public void onResult(byte[] data, FrameInfo frameInfo) {
                                    frameCount++;
                                    totalBytes += data.length;
                                    
                                    if (frameCount % 30 == 0) {
                                        log("INFO", "handleCustomSeiTest", "收到无人机直播流数据 #" + frameCount + ": " + data.length + " bytes (总计: " + totalBytes + " bytes)");
                                    }
                                    
                                    // 将 byte[] 转换为 List<Integer>
                                    java.util.List<Integer> frameData = new java.util.ArrayList<>();
                                    for (byte b : data) {
                                        frameData.add(b & 0xFF);
                                    }
                                    
                                    // 添加自定义 SEI
                                    java.util.List<Integer> encodedData = customSeiModule.customSEIEncode(frameData, customSeiData);
                                    
                                    if (encodedData != null && !encodedData.isEmpty()) {
                                        // 将编码后的数据写入文件
                                        try {
                                            for (Integer val : encodedData) {
                                                outputFile.write(val & 0xFF);
                                            }
                                        } catch (java.io.IOException e) {
                                            log("ERROR", "handleCustomSeiTest", "写入文件失败: " + e.getMessage());
                                        }
                                    } else {
                                        // 如果编码失败，直接写入原始数据
                                        try {
                                            outputFile.write(data);
                                        } catch (java.io.IOException e) {
                                            log("ERROR", "handleCustomSeiTest", "写入文件失败: " + e.getMessage());
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
            log("INFO", "handleCustomSeiTest", "文件保存在: " + filename);

        } catch (Exception e) {
            log("ERROR", "handleCustomSeiTest", "测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        waitForKeyPress();
    }

    private static void handleCustomSeiCodecTest() {
        log("INFO", "handleCustomSeiCodecTest", "开始自定义 SEI 编解码完整演示（无需设备）...");

        // 获取 Stream 实例
        com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
        if (stream == null) {
            log("ERROR", "handleCustomSeiCodecTest", "获取 Stream 实例失败");
            return;
        }

        CustomSEIModule customSeiModule = stream.getCustomSEIModule();
        if (customSeiModule == null) {
            log("ERROR", "handleCustomSeiCodecTest", "获取 CustomSEI 模块失败");
            return;
        }

        try {
            // ==================== 编码阶段 ====================
            log("INFO", "handleCustomSeiCodecTest", "\n=== 编码阶段 ===");

            // 1. 创建示例H.264帧数据（与 C++ codec demo 相同）
            java.util.List<Integer> h264Frame = new java.util.ArrayList<>();
            h264Frame.add(0x00); h264Frame.add(0x00); h264Frame.add(0x00); h264Frame.add(0x01);  // 起始码
            h264Frame.add(0x65); h264Frame.add(0x88); h264Frame.add(0x84); h264Frame.add(0x00);  // NALU头部 (IDR帧)
            h264Frame.add(0x10); h264Frame.add(0x00); h264Frame.add(0x00); h264Frame.add(0x03);  // 示例数据
            h264Frame.add(0x00); h264Frame.add(0x00); h264Frame.add(0x03); h264Frame.add(0x00);
            h264Frame.add(0x00); h264Frame.add(0x03); h264Frame.add(0x00); h264Frame.add(0x00);

            // 2. 创建协议头部
            com.dji.flighthubsdk.bean.SeiHeader header = new com.dji.flighthubsdk.bean.SeiHeader(1, 0, 0, 0);

            // 3. 创建目标检测数据
            java.util.List<com.dji.flighthubsdk.bean.SeiItem> seiData = new java.util.ArrayList<>();

            // 保存原始数据用于后续验证
            com.dji.flighthubsdk.bean.SeiHeader originalHeader = new com.dji.flighthubsdk.bean.SeiHeader(
                    header.getVersion(), header.getReserved(), header.getDataSize(), header.getTimestamp());
            java.util.List<com.dji.flighthubsdk.bean.SeiItem> originalSeiData = new java.util.ArrayList<>();

            // 添加自定义数据（与 C++ codec demo 相同）
            com.dji.flighthubsdk.bean.SeiItem customDataItem = com.dji.flighthubsdk.bean.SeiItem.ofCustomData(
                    new com.dji.flighthubsdk.bean.SeiData(java.util.Arrays.asList(1, 2, 3)));
            seiData.add(customDataItem);
            originalSeiData.add(customDataItem);
            log("INFO", "handleCustomSeiCodecTest", "添加自定义数据: 01 02 03");

            // 添加目标检测框（与 C++ codec demo 相同）
            com.dji.flighthubsdk.bean.SeiItem boxSeiItem = com.dji.flighthubsdk.bean.SeiItem.ofDetectionBox(
                    new com.dji.flighthubsdk.bean.SeiDetectionBox(1, 1, 1, 1, 1, 1, 1, 1));
            seiData.add(boxSeiItem);
            originalSeiData.add(boxSeiItem);
            log("INFO", "handleCustomSeiCodecTest", "添加目标检测框: ID=1, 类型=人, 位置=(1,1), 尺寸=1x1, 置信度=1%");

            // 添加多边形检测框（与 C++ codec demo 相同）
            java.util.List<com.dji.flighthubsdk.bean.SeiPoint2D> polygonPoints = java.util.Arrays.asList(
                    new com.dji.flighthubsdk.bean.SeiPoint2D(1, 2),
                    new com.dji.flighthubsdk.bean.SeiPoint2D(3, 4),
                    new com.dji.flighthubsdk.bean.SeiPoint2D(5, 6),
                    new com.dji.flighthubsdk.bean.SeiPoint2D(7, 8));
            com.dji.flighthubsdk.bean.SeiItem polygonSeiItem = com.dji.flighthubsdk.bean.SeiItem.ofDetectionPolygon(
                    new com.dji.flighthubsdk.bean.SeiDetectionPolygon(2, 2, 2, 2, polygonPoints));
            seiData.add(polygonSeiItem);
            originalSeiData.add(polygonSeiItem);
            log("INFO", "handleCustomSeiCodecTest", "添加多边形检测: ID=2, 类型=车, 4个点");

            // 添加文本叠加（与 C++ codec demo 相同）
            com.dji.flighthubsdk.bean.SeiItem textSeiItem = com.dji.flighthubsdk.bean.SeiItem.ofTextOverlay(
                    new com.dji.flighthubsdk.bean.SeiTextOverlay(3, 3, 3, 3, 3, 3, 3, 3, "BBBBB"));
            seiData.add(textSeiItem);
            originalSeiData.add(textSeiItem);
            log("INFO", "handleCustomSeiCodecTest", "添加文本叠加: ID=3, 位置=(3,3), 文本=\"BBBBB\"");

            // 4. 执行编码
            log("INFO", "handleCustomSeiCodecTest", "\n开始编码...");
            java.util.List<Integer> outputFrame = customSeiModule.customSEIEncode(h264Frame, seiData);

            if (outputFrame != null && !outputFrame.isEmpty()) {
                log("INFO", "handleCustomSeiCodecTest", "编码成功！");
                log("INFO", "handleCustomSeiCodecTest", "原始帧大小: " + h264Frame.size() + " 字节");
                log("INFO", "handleCustomSeiCodecTest", "输出帧大小: " + outputFrame.size() + " 字节");

                // 打印编码后的数据（前32字节）
                log("INFO", "handleCustomSeiCodecTest", "编码后的数据（前32字节）:");
                StringBuilder hexStr = new StringBuilder();
                for (int i = 0; i < Math.min(32, outputFrame.size()); i++) {
                    hexStr.append(String.format("%02X ", outputFrame.get(i)));
                    if ((i + 1) % 16 == 0) {
                        log("INFO", "handleCustomSeiCodecTest", hexStr.toString());
                        hexStr = new StringBuilder();
                    }
                }
                if (hexStr.length() > 0) {
                    log("INFO", "handleCustomSeiCodecTest", hexStr.toString());
                }
                if (outputFrame.size() % 16 != 0) {
                    log("INFO", "handleCustomSeiCodecTest", "");
                }

                // ==================== 解码阶段 ====================
                log("INFO", "handleCustomSeiCodecTest", "\n=== 解码阶段 ===");

                // 2. 解码SEI数据
                log("INFO", "handleCustomSeiCodecTest", "开始解码...");
                CustomSEIModule.CustomSEIDecodeResult decodeResult = customSeiModule.customSEIDecode(outputFrame);

                if (decodeResult != null) {
                    log("INFO", "handleCustomSeiCodecTest", "解码成功！");
                    log("INFO", "handleCustomSeiCodecTest", "协议头部信息:");
                    log("INFO", "handleCustomSeiCodecTest", "  版本: " + decodeResult.getHeader().getVersion());
                    log("INFO", "handleCustomSeiCodecTest", "  保留字段: " + decodeResult.getHeader().getReserved());
                    log("INFO", "handleCustomSeiCodecTest", "  数据大小: " + decodeResult.getHeader().getDataSize());
                    log("INFO", "handleCustomSeiCodecTest", "  时间戳: " + decodeResult.getHeader().getTimestamp());
                    log("INFO", "handleCustomSeiCodecTest", "解码出的 SEI 数据项数量: " + decodeResult.getSeiData().size());

                    // 验证数据完整性
                    log("INFO", "handleCustomSeiCodecTest", "\n=== 数据验证结果 ===");

                    // 验证协议头部
                    boolean headerMatch = (originalHeader.getVersion() == decodeResult.getHeader().getVersion() &&
                            originalHeader.getReserved() == decodeResult.getHeader().getReserved()); // 不需要比较时间戳，SDK 会自动处理时间戳
                    log("INFO", "handleCustomSeiCodecTest", "协议头部验证: " + (headerMatch ? "通过" : "失败"));

                    // 验证SEI数据项数量
                    boolean countMatch = (originalSeiData.size() == decodeResult.getSeiData().size());
                    log("INFO", "handleCustomSeiCodecTest", "SEI数据项数量验证: " + (countMatch ? "通过" : "失败") +
                            " (原始: " + originalSeiData.size() + ", 解码: " + decodeResult.getSeiData().size() + ")");

                    if (headerMatch && countMatch) {
                        log("INFO", "handleCustomSeiCodecTest", "✅ 自定义 SEI 编解码测试完全成功！");
                    } else {
                        log("WARN", "handleCustomSeiCodecTest", "⚠️ 自定义 SEI 编解码测试部分失败");
                    }
                } else {
                    log("ERROR", "handleCustomSeiCodecTest", "解码失败");
                }
            } else {
                log("ERROR", "handleCustomSeiCodecTest", "编码失败");
            }

        } catch (Exception e) {
            log("ERROR", "handleCustomSeiCodecTest", "测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        log("INFO", "handleCustomSeiCodecTest", "自定义 SEI 编解码测试完成");
        System.out.println("\n=== 自定义 SEI 编解码测试完成 ===");
        System.out.println("按 Enter 键返回...");
        waitForKeyPress();
    }

    // ==================== 无人机推流测试 ====================
    /**
     * 处理无人机推流测试功能
     */
    private static void handleDroneStreamingTest() {
        printSeparator("无人机推流测试（基于状态自动控制）");
        
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleDroneStreamingTest", "该设备没有无人机");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleDroneStreamingTest", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }

        try {
            // 获取RTMP配置
            ConfigManager.DemoConfig cfg = ConfigManager.getConfig();
            final boolean rtmpEnabled = cfg.rtmp != null && cfg.rtmp.enabled &&
                    cfg.rtmp.server_url != null && !cfg.rtmp.server_url.isEmpty() &&
                    cfg.rtmp.stream_key != null && !cfg.rtmp.stream_key.isEmpty();
            
            if (!rtmpEnabled) {
                log("WARN", "handleDroneStreamingTest", "RTMP推流未配置，将只进行拉流测试");
                log("INFO", "handleDroneStreamingTest", "请在配置文件中启用RTMP推流");
            }
            
            final String rtmpUrl = rtmpEnabled ? (cfg.rtmp.server_url.endsWith("/") ?
                    (cfg.rtmp.server_url + cfg.rtmp.stream_key) : (cfg.rtmp.server_url + "/" + cfg.rtmp.stream_key)) : null;
            final int rtmpFps = cfg.rtmp != null ? Math.max(1, cfg.rtmp.frame_rate) : 30;
            final int rtmpWidth = cfg.rtmp != null ? cfg.rtmp.width : 1920;
            final int rtmpHeight = cfg.rtmp != null ? cfg.rtmp.height : 1080;
            final int rtmpBitrate = cfg.rtmp != null ? cfg.rtmp.bitrate : 2000000;
            
            // 创建推流测试实例
            droneStreamingTest = new DroneStreamingTest(drone, rtmpUrl, rtmpFps, rtmpWidth, rtmpHeight, rtmpBitrate);
            
            // 启动推流测试
            droneStreamingTest.start();
            
            log("INFO", "handleDroneStreamingTest", "推流测试已启动");
            log("INFO", "handleDroneStreamingTest", "该测试将根据飞机在线状态自动控制拉流和推流");
            log("INFO", "handleDroneStreamingTest", "飞机上线时自动开始拉流和推流，飞机离线时自动停止");
            log("INFO", "handleDroneStreamingTest", "按Enter键停止测试...");
            waitForKeyPress();
            
        } catch (Exception e) {
            log("ERROR", "handleDroneStreamingTest", "推流测试失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 停止推流测试
            if (droneStreamingTest != null) {
                droneStreamingTest.stop();
                droneStreamingTest = null;
            }
            log("INFO", "handleDroneStreamingTest", "推流测试已停止");
        }
    }

    /**
     * 启动推流状态监控
     */
    private static void startStreamMonitor() {
        if (streamMonitorThread != null && streamMonitorThread.isAlive()) {
            return; // 已经在运行
        }
        
        streamMonitorThread = new Thread(() -> {
            while (running.get() && droneStreamer != null) {
                try {
                    // 检查推流状态
                    if (droneStreamer.isRunning()) {
                        log("DEBUG", "StreamMonitor", "推流状态正常");
                    } else {
                        log("WARN", "StreamMonitor", "推流已停止");
                    }
                    Thread.sleep(10000); // 每10秒检查一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "stream-monitor");
        streamMonitorThread.setDaemon(true);
        streamMonitorThread.start();
        log("INFO", "startStreamMonitor", "推流状态监控已启动");
    }

    /**
     * 停止推流状态监控
     */
    private static void stopStreamMonitor() {
        if (streamMonitorThread != null) {
            streamMonitorThread.interrupt();
            try {
                streamMonitorThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            streamMonitorThread = null;
            log("INFO", "stopStreamMonitor", "推流状态监控已停止");
        }
    }

    /**
     * 拉流模块测试
     */
    private static void handlePullStreamTest() {
        printSeparator("拉流模块测试");
        
        // 获取Stream实例
        com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
        if (stream == null) {
            System.out.println("获取Stream失败");
            return;
        }
        
        // 获取PullStreamModule
        String streamId = "test_stream_001";
        com.dji.flighthubsdk.core.PullStreamModule pullStreamModule = stream.getPullStreamModule(streamId);
        if (pullStreamModule == null) {
            System.out.println("获取PullStreamModule失败，请检查RTC通道管理器是否正确注入");
            return;
        }
        
        System.out.println("拉流模块获取成功！stream_id: " + streamId);
        
        // 创建视频文件保存器
        String filename = generateTimestampFilename("dji_pull_stream", "h264");
        try {
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(filename);
            
            // 设置错误回调
            pullStreamModule.setErrorCallback(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamErrorCallback() {
                @Override
                public void onPullStreamError(int errorCode, String errorMessage) {
                    System.out.println("拉流错误: " + errorCode + " - " + errorMessage);
                }
            });
            
            // 订阅流数据
            int streamSubscribeId = pullStreamModule.subscribeStream(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamDataCallback() {
                private int frameCount = 0;
                private long totalBytes = 0;
                
                @Override
                public void onPullStreamData(byte[] data, com.dji.flighthubsdk.bean.FrameInfo frameInfo) {
                    frameCount++;
                    totalBytes += data.length;
                    
                    try {
                        // 保存数据到文件
                        outputStream.write(data);
                        outputStream.flush();
                        
                        // 每10帧打印一次信息
                        if (frameCount % 10 == 0) {
                            System.out.println("拉流数据 - 帧数: " + frameCount + 
                                             ", 总字节数: " + totalBytes + 
                                             ", 当前帧大小: " + data.length + 
                                             ", 关键帧: " + frameInfo.isKeyFrame());
                        }
                    } catch (Exception e) {
                        System.err.println("写入文件错误: " + e.getMessage());
                    }
                }
            });
            
            if (streamSubscribeId < 0) {
                System.out.println("订阅流数据失败");
                outputStream.close();
                return;
            }
            
            System.out.println("订阅流数据成功，订阅ID: " + streamSubscribeId);
            
            // 获取拉流URL
            System.out.print("请输入WHEP拉流URL (例如: https://example.com/whep/stream): ");
            String whepUrl = scanner.nextLine().trim();
            
            if (whepUrl.isEmpty()) {
                System.out.println("使用默认测试URL");
                whepUrl = "https://example.com/whep/test_stream";
            }
            
            System.out.println("开始拉流: " + whepUrl);
            
            // 开始拉流
            pullStreamModule.start(whepUrl, new com.dji.flighthubsdk.core.PullStreamModule.PullStreamStartCallback() {
                @Override
                public void onPullStreamStart(FlightHubError error) {
                    if (error.isSuccess()) {
                        System.out.println("拉流启动成功");
                    } else {
                        System.out.println("拉流启动失败: " + error);
                    }
                }
            });
            
            // 等待用户输入停止
            System.out.println("拉流已启动，按回车键停止拉流...");
            scanner.nextLine();
            
            // 停止拉流
            System.out.println("正在停止拉流...");
            pullStreamModule.stop();
            
            // 取消订阅
            pullStreamModule.unSubscribeStream(streamSubscribeId);
            
            // 关闭文件
            outputStream.close();
            
            System.out.println("拉流测试完成，文件已保存到: " + filename);
            System.out.println("流状态: " + (pullStreamModule.isStreaming() ? "正在拉流" : "已停止"));
            System.out.println("流信息: " + pullStreamModule.getStreamInfo());
            
        } catch (Exception e) {
            System.err.println("拉流测试错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理监听飞机POI进度
     */
    private static void handleListenAircraftPoiProgress() {
        printSeparator("飞行 - POI - 进度监听");

        if (drone == null) {
            System.out.println("错误: 无人机对象为空");
            return;
        }

        try {
            if (dock == null) {
                System.out.println("错误: 机场对象为空");
                return;
            }

            // 如果已经在监听，先取消
            if (aircraftPoiProgressListenId != -1) {
                drone.getPoiModule().cancelListenPoiTaskEvent(aircraftPoiProgressListenId);
                aircraftPoiProgressListenId = -1;
                System.out.println("已取消POI任务事件监听");
                return;
            }

            // POI 任务事件
            aircraftPoiProgressListenId = drone.getPoiModule().listenPoiTaskEvent(event -> {
                log("INFO", "listenPoiTaskEvent", "========== POI 任务事件 ==========");
                log("INFO", "listenPoiTaskEvent", "event_id: " + event.getEventId());
                log("INFO", "listenPoiTaskEvent", "action: " + event.getAction());
                log("INFO", "listenPoiTaskEvent", "uuid: " + event.getUuid());
                log("INFO", "listenPoiTaskEvent", "timestamp: " + event.getTimestamp());
                if (event.getTaskData() != null) {
                    com.dji.flighthubsdk.bean.PoiTaskData td = event.getTaskData();
                    log("INFO", "listenPoiTaskEvent", "task_data:");
                    log("INFO", "listenPoiTaskEvent", "  uuid: " + td.getUuid());
                    log("INFO", "listenPoiTaskEvent", "  sn: " + td.getSn());
                    log("INFO", "listenPoiTaskEvent", "  device_sn: " + td.getDeviceSn());
                    if (td.getPoiCenterPoint() != null) {
                        com.dji.flighthubsdk.bean.PoiCenterPoint cp = td.getPoiCenterPoint();
                        log("INFO", "listenPoiTaskEvent", "  poi_center_point: lat=" + cp.getLatitude() + ", lng=" + cp.getLongitude() + ", height=" + cp.getHeight());
                    }
                    log("INFO", "listenPoiTaskEvent", "  speed: " + td.getSpeed() + " m/s");
                    log("INFO", "listenPoiTaskEvent", "  circle_radius: " + td.getCircleRadius() + " m");
                    log("INFO", "listenPoiTaskEvent", "  payload_index: " + td.getPayloadIndex());
                    log("INFO", "listenPoiTaskEvent", "  status: " + td.getStatus());
                    log("INFO", "listenPoiTaskEvent", "  err_code: " + td.getErrCode());
                }
            });
            System.out.println("已开始监听POI任务事件, listenId=" + aircraftPoiProgressListenId);

        } catch (Exception e) {
            System.err.println("监听POI进度错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理进入POI模式
     */
    private static void handlePOIEnter() {
        printSeparator("飞行 - POI - 进入POI模式");

        if (drone == null) {
            System.out.println("错误: 无人机对象为空");
            return;
        }

        try {
            PoiModule poiModule = drone.getPoiModule();
            if (poiModule == null) {
                System.out.println("错误: POI模块为空");
                return;
            }

            // 获取用户输入
            System.out.print("请输入载荷索引 (默认: " + targetPayloadIndex + "): ");
            String payloadIndex = scanner.nextLine().trim();
            if (payloadIndex.isEmpty()) {
                payloadIndex = targetPayloadIndex;
            }

            System.out.print("请输入POI中心点纬度: ");
            double latitude = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("请输入POI中心点经度: ");
            double longitude = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("请输入POI中心点高度 (米): ");
            double height = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("请输入圆形半径 (米): ");
            double radius = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("请输入飞行速度 (m/s): ");
            double speed = Double.parseDouble(scanner.nextLine().trim());

            // 创建POI进入参数
            com.dji.flighthubsdk.bean.POIEnterParams params = new com.dji.flighthubsdk.bean.POIEnterParams();
            params.setPayloadIndex(payloadIndex);
            params.setPoiCenterPoint(new com.dji.flighthubsdk.bean.Waypoint(latitude, longitude, height));
            params.setCircleRadius(radius);
            params.setSpeed(speed);

            System.out.println("正在进入POI模式...");
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            poiModule.enter(params, new FlightHubCallback.PoiEnterCallback() {
                @Override
                public void onResult(FlightHubError error, String taskUuid) {
                    if (error.isSuccess()) {
                        System.out.println("进入POI模式成功，任务UUID: " + taskUuid);
                    } else {
                        System.out.println("进入POI模式失败: " + error);
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await(15, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitForKeyPress();

        } catch (Exception e) {
            System.err.println("进入POI模式错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理停止POI模式
     */
    private static void handlePOIExit() {
        printSeparator("飞行 - POI - 停止POI模式");

        if (drone == null) {
            System.out.println("错误: 无人机对象为空");
            return;
        }

        try {
            PoiModule poiModule = drone.getPoiModule();
            if (poiModule == null) {
                System.out.println("错误: POI模块为空");
                return;
            }

            System.out.println("正在停止POI模式...");
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            poiModule.exit(new FlightHubCallback.ResultCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    if (error.isSuccess()) {
                        System.out.println("停止POI模式成功");
                    } else {
                        System.out.println("停止POI模式失败: " + error);
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await(15, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitForKeyPress();

        } catch (Exception e) {
            System.err.println("停止POI模式错误: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 处理更新POI速度
     */
    private static void handlePOIUpdateSpeed() {
        printSeparator("飞行 - POI - 更新POI速度");

        if (drone == null) {
            System.out.println("错误: 无人机对象为空");
            return;
        }

        try {
            PoiModule poiModule = drone.getPoiModule();
            if (poiModule == null) {
                System.out.println("错误: POI模块为空");
                return;
            }

            System.out.print("请输入新的飞行速度 (m/s): ");
            double speed = Double.parseDouble(scanner.nextLine().trim());

            System.out.println("正在更新POI速度...");
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            poiModule.updateSpeed(speed, new FlightHubCallback.ResultCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    if (error.isSuccess()) {
                        System.out.println("更新POI速度成功");
                    } else {
                        System.out.println("更新POI速度失败: " + error);
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await(15, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitForKeyPress();

        } catch (Exception e) {
            System.err.println("更新POI速度错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理查询POI任务
     */
    private static void handlePOIGetTask() {
        printSeparator("飞行 - POI - 查询POI任务");

        if (drone == null) {
            System.out.println("错误: 无人机对象为空");
            return;
        }

        try {
            PoiModule poiModule = drone.getPoiModule();
            if (poiModule == null) {
                System.out.println("错误: POI模块为空");
                return;
            }

            System.out.println("正在查询POI任务...");
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            poiModule.getTask(new FlightHubCallback.PoiGetTaskCallback() {
                @Override
                public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.POITaskInfo task) {
                    if (error.isSuccess() && task != null) {
                        System.out.println("=== POI任务信息 ===");
                        System.out.println("任务UUID: " + task.getUuid());
                        System.out.println("设备SN: " + task.getDeviceSn());
                        System.out.println("飞行器SN: " + task.getSn());
                        System.out.println("状态: " + task.getTaskStatus() + " (int=" + task.getTaskStatus().getValue() + ")");
                        System.out.println("错误代码: " + task.getErrCode());
                        System.out.println("中心点: 纬度=" + task.getPoiCenterPoint().getLatitude()
                                + ", 经度=" + task.getPoiCenterPoint().getLongitude()
                                + ", 高度=" + task.getPoiCenterPoint().getHeight());
                        System.out.println("速度: " + task.getSpeed() + "m/s");
                        System.out.println("半径: " + task.getCircleRadius() + "m");
                        System.out.println("载荷索引: " + task.getPayloadIndex());
                        System.out.println("================");
                    } else {
                        System.out.println("查询POI任务失败: " + error);
                    }
                    latch.countDown();
                }
            });
            try {
                latch.await(15, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitForKeyPress();

        } catch (Exception e) {
            System.err.println("查询POI任务错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 机场控制相关方法 ====================

    /**
     * 处理机场补光灯开启
     */
    private static void handleDockSupplementLightOpen() {
        printSeparator("开启机场补光灯");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockSupplementLightOpen", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockSupplementLightOpen", "正在开启补光灯...");
        dock.getDockControlModule().setDockSupplementLightState(true, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockSupplementLightOpen", "补光灯已成功开启");
            } else {
                log("ERROR", "handleDockSupplementLightOpen", "开启补光灯失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * 处理机场补光灯关闭
     */
    private static void handleDockSupplementLightClose() {
        printSeparator("关闭机场补光灯");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockSupplementLightClose", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockSupplementLightClose", "正在关闭补光灯...");
        dock.getDockControlModule().setDockSupplementLightState(false, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockSupplementLightClose", "补光灯已成功关闭");
            } else {
                log("ERROR", "handleDockSupplementLightClose", "关闭补光灯失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * 处理机场充电开启
     */
    private static void handleDockChargeOpen() {
        printSeparator("开启机场充电");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockChargeOpen", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockChargeOpen", "正在开启充电...");
        dock.getDockControlModule().setDockChargeState(true, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockChargeOpen", "充电已成功开启");
            } else {
                log("ERROR", "handleDockChargeOpen", "开启充电失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * 处理机场充电关闭
     */
    private static void handleDockChargeClose() {
        printSeparator("关闭机场充电");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockChargeClose", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockChargeClose", "正在关闭充电...");
        dock.getDockControlModule().setDockChargeState(false, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockChargeClose", "充电已成功关闭");
            } else {
                log("ERROR", "handleDockChargeClose", "关闭充电失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * 处理机场舱盖开启
     */
    private static void handleDockCoverOpen() {
        printSeparator("开启机场舱盖");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockCoverOpen", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockCoverOpen", "正在开启舱盖...");
        dock.getDockControlModule().setDockCoverState(DockCoverMode.OPEN, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockCoverOpen", "舱盖已成功开启");
            } else {
                log("ERROR", "handleDockCoverOpen", "开启舱盖失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * 处理机场舱盖关闭
     */
    private static void handleDockCoverClose() {
        printSeparator("关闭机场舱盖");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockCoverClose", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockCoverClose", "正在关闭舱盖...");
        dock.getDockControlModule().setDockCoverState(DockCoverMode.CLOSE, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockCoverClose", "舱盖已成功关闭");
            } else {
                log("ERROR", "handleDockCoverClose", "关闭舱盖失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * 处理机场舱盖强制关闭
     */
    private static void handleDockCoverForceClose() {
        printSeparator("强制关闭机场舱盖");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockCoverForceClose", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDockCoverForceClose", "正在强制关闭舱盖...");
        dock.getDockControlModule().setDockCoverState(DockCoverMode.FORCE_CLOSE, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockCoverForceClose", "舱盖已成功强制关闭");
            } else {
                log("ERROR", "handleDockCoverForceClose", "强制关闭舱盖失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * 处理机场空调模式设置
     */
    private static void handleDockAirConditionerMode() {
        printSeparator("设置机场空调模式");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockAirConditionerMode", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        System.out.println("请输入空调模式 (0-9):");
        System.out.println("  0: 空闲模式");
        System.out.println("  1: 制冷模式");
        System.out.println("  2: 制热模式");
        System.out.println("  3: 除湿模式");
        System.out.println("  4: 制冷退出模式");
        System.out.println("  5: 制热退出模式");
        System.out.println("  6: 除湿退出模式");
        System.out.println("  7: 制冷准备模式");
        System.out.println("  8: 制热准备模式");
        System.out.println("  9: 除湿准备模式");
        System.out.print("请输入选择 (0-9): ");

        int mode = scanner.nextInt();

        log("INFO", "handleDockAirConditionerMode", "正在设置空调模式为: " + mode + "...");
        dock.getDockControlModule().setDockAirConditionerMode(mode, error -> {
            if (error.isSuccess()) {
                log("INFO", "handleDockAirConditionerMode", "空调模式设置成功");
            } else {
                log("ERROR", "handleDockAirConditionerMode", "空调模式设置失败, error: " + error);
            }
        });

        waitForKeyPress();
    }

    /**
     * DRC 操控 - 云台重置（drcResetGimbal）
     */
    private static void handleResetGimbal() {
        printSeparator("DRC 操控 - 云台重置 (drcResetGimbal)");
        if (selectedDevice == null || !hasSubDevice(selectedDevice)) {
            log("WARN", "handleResetGimbal", "该设备没有无人机，无法执行云台重置");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleResetGimbal", "获取 Drone 实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleResetGimbal", "提示: 请先通过 [113] 连接 DRC 链路，再执行本项测试。");
        log("INFO", "handleResetGimbal", "当前 payload_index=" + targetPayloadIndex + " (可在代码中修改 targetPayloadIndex)");
        System.out.println("请选择 reset_mode: 0=回中, 1=向下, 2=偏航回中, 3=俯仰向下 (直接回车默认 0):");
        int resetMode = 0;
        try {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                resetMode = Integer.parseInt(line);
            }
        } catch (NumberFormatException ignored) {
        }
        if (resetMode < 0 || resetMode > 3) {
            log("WARN", "handleResetGimbal", "无效 reset_mode，使用 0 (回中)");
            resetMode = 0;
        }

        final int mode = resetMode;
        log("INFO", "handleResetGimbal", "正在执行 drcResetGimbal, payload_index=" + targetPayloadIndex + ", reset_mode=" + mode);
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        drone.getCameraControlModule().drcResetGimbal(targetPayloadIndex, mode, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (error.isSuccess()) {
                    log("INFO", "handleResetGimbal", "drcResetGimbal 成功");
                } else {
                    log("ERROR", "handleResetGimbal", "drcResetGimbal 失败: " + error);
                }
                latch.countDown();
            }
        });
        try {
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log("INFO", "handleResetGimbal", "请观察云台是否已复位");
        waitForKeyPress();
    }

    /**
     * DRC 链路 - 手动连接
     */
    private static void handleDrcLinkConnect() {
        printSeparator("DRC 链路 - 手动连接");
        if (selectedDevice == null || !hasSubDevice(selectedDevice)) {
            log("WARN", "handleDrcLinkConnect", "该设备没有无人机，无法连接 DRC 链路");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleDrcLinkConnect", "获取 Drone 实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handleDrcLinkConnect", "正在连接 DRC 链路...");
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        drone.getDrcModule().connectDrcLink(error -> {
            log("INFO", "handleDrcLinkConnect", "connectDrcLink callback, error: " + error);
            latch.countDown();
        });
        try { latch.await(10, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        log("INFO", "handleDrcLinkConnect", "DRC 链路连接完成，可继续测试其他操控功能。断开请用 [114]");
        waitForKeyPress();
    }

    /**
     * DRC 链路 - 手动断开
     */
    private static void handleDrcLinkDisconnect() {
        printSeparator("DRC 链路 - 手动断开");
        if (selectedDevice == null || !hasSubDevice(selectedDevice)) {
            log("WARN", "handleDrcLinkDisconnect", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleDrcLinkDisconnect", "获取 Drone 实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handleDrcLinkDisconnect", "正在断开 DRC 链路...");
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        drone.getDrcModule().disconnectDrcLink(error -> {
            log("INFO", "handleDrcLinkDisconnect", "disconnectDrcLink callback, error: " + error);
            latch.countDown();
        });
        try { latch.await(10, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        log("INFO", "handleDrcLinkDisconnect", "DRC 链路已断开");
        waitForKeyPress();
    }

    /**
     * DRC 链路 - 连接/断开循环测试
     */
    private static void handleDrcLinkCycleTest() {
        printSeparator("DRC 链路 - 连接/断开循环测试");

        if (selectedDevice == null || !hasSubDevice(selectedDevice)) {
            log("WARN", "handleDrcLinkCycleTest", "该设备没有无人机，无法测试 DRC 链路");
            waitForKeyPress();
            return;
        }

        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (drone == null || dock == null) {
            log("ERROR", "handleDrcLinkCycleTest", "获取设备实例失败");
            waitForKeyPress();
            return;
        }

        final int CYCLE_COUNT = 3;
        final int CONNECTED_HOLD_S = 10;
        final int DISCONNECTED_HOLD_S = 8;

        log("INFO", "handleDrcLinkCycleTest",
                "测试参数: " + CYCLE_COUNT + " 轮, 连接保持 " + CONNECTED_HOLD_S +
                "s, 断开等待 " + DISCONNECTED_HOLD_S + "s");

        // 监听 DRC 状态
        dock.getDockOsdModule().listenDockDrcState(drcState ->
                log("INFO", "DRC State", drcState.getDrcState() +
                        "  (0=DISCONNECTED 1=CONNECTING 2=CONNECTED)"));

        for (int i = 1; i <= CYCLE_COUNT; i++) {
            final int round = i;
            log("INFO", "handleDrcLinkCycleTest", "=== 第 " + round + "/" + CYCLE_COUNT + " 轮 ===");

            // --- Connect ---
            log("INFO", "handleDrcLinkCycleTest", "[" + round + "] connectDrcLink...");
            final AtomicBoolean connectDone = new AtomicBoolean(false);
            drone.getDrcModule().connectDrcLink(error -> {
                log("INFO", "handleDrcLinkCycleTest",
                        "[" + round + "] connectDrcLink callback, error: " + error);
                connectDone.set(true);
            });
            int waitMs = 0;
            while (!connectDone.get() && waitMs < 5000) {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                waitMs += 100;
            }

            log("INFO", "handleDrcLinkCycleTest",
                    "[" + round + "] 等待 " + CONNECTED_HOLD_S + "s (观察 DRC 是否变为 CONNECTED)...");
            try { Thread.sleep(CONNECTED_HOLD_S * 1000L); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }

            // --- Disconnect ---
            log("INFO", "handleDrcLinkCycleTest", "[" + round + "] disconnectDrcLink...");
            final AtomicBoolean disconnectDone = new AtomicBoolean(false);
            drone.getDrcModule().disconnectDrcLink(error -> {
                log("INFO", "handleDrcLinkCycleTest",
                        "[" + round + "] disconnectDrcLink callback, error: " + error);
                disconnectDone.set(true);
            });
            waitMs = 0;
            while (!disconnectDone.get() && waitMs < 3000) {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                waitMs += 100;
            }

            if (i < CYCLE_COUNT) {
                log("INFO", "handleDrcLinkCycleTest",
                        "[" + round + "] 等待 " + DISCONNECTED_HOLD_S + "s 后进行下一轮...");
                try { Thread.sleep(DISCONNECTED_HOLD_S * 1000L); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }

        log("INFO", "handleDrcLinkCycleTest", "所有轮次完成");
        waitForKeyPress();
    }

    /**
     * 处理机场舱内/舱外摄像头切换（与 C++ sample [240] 一致）
     * {@code listenLiveAvailableRange} 回调已传出 {@code bean.LiveInfo} 值拷贝，可安全在回调外持有引用。
     */
    private static void handleDockChangeCameraPosition() {
        printSeparator("切换机场舱内/舱外摄像头");

        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleDockChangeCameraPosition", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        System.out.println("选择目标位置: [0] 舱内 [1] 舱外: ");
        int pos = 0;
        if (scanner.hasNextInt()) {
            pos = scanner.nextInt();
        }
        if (pos != 0 && pos != 1) {
            log("WARN", "handleDockChangeCameraPosition", "无效选择，已取消");
            waitForKeyPress();
            return;
        }
        final DockCameraPosition position = pos == 0 ? DockCameraPosition.INSIDE : DockCameraPosition.OUTSIDE;

        final java.util.concurrent.atomic.AtomicReference<LiveInfo> liveInfoCopyRef = new java.util.concurrent.atomic.AtomicReference<>();
        final java.util.concurrent.atomic.AtomicReference<String> logSnRef = new java.util.concurrent.atomic.AtomicReference<>();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        int listenId = dock.getLiveDeviceModule().listenLiveAvailableRange(ranges -> {
            if (ranges == null || ranges.isEmpty()) {
                latch.countDown();
                return;
            }
            LiveInfo info = ranges.get(0);
            liveInfoCopyRef.set(info);
            logSnRef.set(info.getDeviceSn() + " camera=" + info.getPayloadIndex() + " video=" + info.getVideoId());
            dock.getDockControlModule().changeDockCameraPosition(info, position, "", error -> {
                if (error.isSuccess()) {
                    log("INFO", "handleDockChangeCameraPosition", "切换成功: " + (position == DockCameraPosition.INSIDE ? "舱内" : "舱外"));
                } else {
                    log("ERROR", "handleDockChangeCameraPosition", "切换失败, error: " + error);
                }
            });
            latch.countDown();
        });
        try {
            if (!latch.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
                log("WARN", "handleDockChangeCameraPosition", "获取直播范围超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("ERROR", "handleDockChangeCameraPosition", "等待直播范围被中断: " + e.getMessage());
        }
        dock.getLiveDeviceModule().cancelListenLiveAvailableRange(listenId);

        if (liveInfoCopyRef.get() == null) {
            log("ERROR", "handleDockChangeCameraPosition", "无可用直播范围，请确认机场在线且支持舱内/舱外摄像头");
        } else {
            log("INFO", "handleDockChangeCameraPosition", "使用当前直播: sn=" + logSnRef.get());
        }

        waitForKeyPress();
    }

    /** 获取当前设备的 LiveRecordModule（通过 listenLiveAvailableRange 拿到 LiveInfo 后 getLiveRecord） */
    private static com.dji.flighthubsdk.core.LiveRecordModule getLiveRecordModuleFromDock() {
        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) return null;
        final java.util.concurrent.atomic.AtomicReference<com.dji.flighthubsdk.core.LiveRecordModule> moduleRef = new java.util.concurrent.atomic.AtomicReference<>();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        int listenId = dock.getLiveDeviceModule().listenLiveAvailableRange(ranges -> {
            if (ranges == null || ranges.isEmpty()) {
                latch.countDown();
                return;
            }
            com.dji.flighthubsdk.core.LiveRecordModule m = dock.getLiveRecord(ranges.get(0));
            moduleRef.set(m);
            latch.countDown();
        });
        try {
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        dock.getLiveDeviceModule().cancelListenLiveAvailableRange(listenId);
        return moduleRef.get();
    }

    private static void handleLiveRecordStart() {
        printSeparator("直播录制 - 开始录制");
        com.dji.flighthubsdk.core.LiveRecordModule module = getLiveRecordModuleFromDock();
        if (module == null) {
            log("ERROR", "handleLiveRecordStart", "获取 LiveRecord 模块或直播范围失败");
            waitForKeyPress();
            return;
        }
        /* @deprecated: 本接口暂未开放使用
        module.startLiveRecord("Asia/Shanghai", error -> {
            if (error.isSuccess()) log("INFO", "handleLiveRecordStart", "开始录制成功");
            else log("ERROR", "handleLiveRecordStart", "开始录制失败, error: " + error);
        });
        */
        waitForKeyPress();
    }

    private static void handleLiveRecordStop() {
        printSeparator("直播录制 - 停止录制");
        com.dji.flighthubsdk.core.LiveRecordModule module = getLiveRecordModuleFromDock();
        if (module == null) {
            log("ERROR", "handleLiveRecordStop", "获取 LiveRecord 模块或直播范围失败");
            waitForKeyPress();
            return;
        }
        /* @deprecated: 本接口暂未开放使用
        module.stopLiveRecord(error -> {
            if (error.isSuccess()) log("INFO", "handleLiveRecordStop", "停止录制成功");
            else log("ERROR", "handleLiveRecordStop", "停止录制失败, error: " + error);
        });
        */
        waitForKeyPress();
    }

    private static void handleLiveRecordGetRecords() {
        printSeparator("直播录制 - 查询设备录制记录");
        com.dji.flighthubsdk.core.LiveRecordModule module = getLiveRecordModuleFromDock();
        if (module == null) {
            log("ERROR", "handleLiveRecordGetRecords", "获取 LiveRecord 模块或直播范围失败");
            waitForKeyPress();
            return;
        }
        /* @deprecated: 本接口暂未开放使用
        String sn = getMainDeviceSn(selectedDevice);
        module.getDeviceLiveRecords(sn, (error, records) -> {
            if (!error.isSuccess()) {
                log("ERROR", "handleLiveRecordGetRecords", "查询录制记录失败, error: " + error);
                return;
            }
            log("INFO", "handleLiveRecordGetRecords", "录制记录数: " + (records != null ? records.size() : 0));
            if (records != null) {
                for (int i = 0; i < records.size(); i++) {
                    com.dji.flighthubsdk.bean.LiveRecordItem r = records.get(i);
                    System.out.println("  [" + i + "] bid=" + r.getBid()
                            + " sn=" + r.getSn()
                            + " camera=" + r.getCamera()
                            + " state=" + r.getState()
                            + " duration=" + r.getDuration()
                            + " created_by=" + r.getCreatedBy());
                }
            }
        });
        */
        waitForKeyPress();
    }

    /** 用于「取消订阅录制状态」：保存最近一次订阅的 listenId 与 module */
    private static int liveRecordListenId = -1;
    private static com.dji.flighthubsdk.core.LiveRecordModule liveRecordModuleForCancel = null;

    private static void handleLiveRecordListenState() {
        printSeparator("直播录制 - 订阅录制状态");
        com.dji.flighthubsdk.core.LiveRecordModule module = getLiveRecordModuleFromDock();
        if (module == null) {
            log("ERROR", "handleLiveRecordListenState", "获取 LiveRecord 模块或直播范围失败");
            waitForKeyPress();
            return;
        }
        /* @deprecated: 本接口暂未开放使用
        int listenId = module.listenRecordState(info -> {
            if (info != null)
                log("INFO", "LiveRecordState", "sn=" + info.getSn() + " camera=" + info.getCamera() + " video=" + info.getVideo() + " state=" + info.getState() + " duration=" + info.getDuration());
        });
        liveRecordListenId = listenId;
        liveRecordModuleForCancel = module;
        log("INFO", "handleLiveRecordListenState", "已订阅录制状态, listen_id=" + listenId + "。取消请选择 [245] 直播录制 - 取消订阅录制状态");
        */
        waitForKeyPress();
    }

    private static void handleLiveRecordCancelListenState() {
        printSeparator("直播录制 - 取消订阅录制状态");
        if (liveRecordListenId < 0 || liveRecordModuleForCancel == null) {
            log("WARN", "handleLiveRecordCancelListenState", "当前无有效订阅，请先选择 [244] 订阅录制状态");
            waitForKeyPress();
            return;
        }
        // @deprecated: 本接口暂未开放使用
        // liveRecordModuleForCancel.cancelListenRecordState(liveRecordListenId);
        // log("INFO", "handleLiveRecordCancelListenState", "已取消订阅, listen_id=" + liveRecordListenId);
        liveRecordListenId = -1;
        liveRecordModuleForCancel = null;
        waitForKeyPress();
    }

    private static void handleGetAudienceCount() {
        printSeparator("获取在线观众数量");
        if (selectedDevice == null) {
            log("ERROR", "handleGetAudienceCount", "未选择设备");
            waitForKeyPress();
            return;
        }
        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (dock == null) {
            log("ERROR", "handleGetAudienceCount", "获取机场实例失败");
            waitForKeyPress();
            return;
        }
        com.dji.flighthubsdk.bean.AudienceCountGroup group = new com.dji.flighthubsdk.bean.AudienceCountGroup();
        group.setLevel1Group("web");
        group.setLevel2Group("");
        dock.getLiveDeviceModule().getAudienceCount(group, new FlightHubCallback.GetAudienceCountCallback() {
            @Override
            public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.DeviceAudienceCountItem> items) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetAudienceCount", "getAudienceCount 失败, error: " + error);
                    return;
                }
                int n = items != null ? items.size() : 0;
                log("INFO", "handleGetAudienceCount", "在线观众数量 (共 " + n + " 台设备):");
                if (items != null) {
                    for (com.dji.flighthubsdk.bean.DeviceAudienceCountItem d : items) {
                        log("INFO", "handleGetAudienceCount", "  sn=" + d.getSn() + ", count=" + d.getCount());
                    }
                }
            }
        });
        waitForKeyPress();
    }

    private static void handleGetFlightTaskList() {
        printSeparator("航线任务 - 获取任务列表");
        if (selectedDevice == null) {
            log("ERROR", "handleGetFlightTaskList", "未选择设备");
            waitForKeyPress();
            return;
        }
        FlightTaskModule module = requireFlightTaskModule("handleGetFlightTaskList");
        if (module == null) { waitForKeyPress(); return; }
        com.dji.flighthubsdk.bean.FlightTaskListParams params = new com.dji.flighthubsdk.bean.FlightTaskListParams();
        params.setPage(1);
        params.setPageSize(10);
        params.setTaskType(1);
        // params.setSn(java.util.Collections.singletonList(getMainDeviceSn(selectedDevice)));
        params.setSn(java.util.Collections.singletonList(""));
        module.getFlightTaskList(params, new FlightHubCallback.GetFlightTaskListCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlightTaskListResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetFlightTaskList", "getFlightTaskList 失败, error: " + error);
                    return;
                }
                if (result != null && result.getList() != null) {
                    log("INFO", "handleGetFlightTaskList", "任务数: " + result.getList().size() + ", total=" + (result.getPagination() != null ? result.getPagination().getTotal() : 0));
                    for (int i = 0; i < result.getList().size(); i++) {
                        com.dji.flighthubsdk.bean.TaskDetail t = result.getList().get(i);
                        log("INFO", "handleGetFlightTaskList", "  [" + (i+1) + "] uuid=" + t.getUuid() + ", name=" + t.getName() + ", status=" + t.getStatus()
                                + ", run_at=" + t.getRunAt() + ", completed_at=" + t.getCompletedAt());
                    }
                }
            }
        });
        waitForKeyPress();
    }

    private static void handleRoaming() {
        printSeparator("航线任务 - 漫游");
        if (selectedDevice == null) {
            log("ERROR", "handleRoaming", "未选择设备");
            waitForKeyPress();
            return;
        }
        FlightTaskModule module = requireFlightTaskModule("handleRoaming");
        if (module == null) { waitForKeyPress(); return; }

        System.out.println("[1] 开始漫游");
        System.out.println("[2] 设置漫游基站白名单");
        System.out.println("[3] 查询飞行中任务拓扑");
        System.out.println("[4] 监听任务拓扑变更（输入 0 取消）");
        System.out.print("请输入选择: ");
        int choice = getUserChoice(1, 4);
        final String droneSn = getSubDeviceSn(selectedDevice);
        if (droneSn == null || droneSn.isEmpty()) {
            log("ERROR", "handleRoaming", "当前拓扑没有飞机 SN，无法执行漫游");
            waitForKeyPress();
            return;
        }
        switch (choice) {
            case 1: {
                System.out.print("请输入 flight_id: ");
                String flightId = scanner.nextLine().trim();
                System.out.print("请输入 source_task_type（1=航线，2=一键起飞，默认 1）: ");
                String typeInput = scanner.nextLine().trim();
                int sourceTaskType = typeInput.isEmpty() ? 1 : Integer.parseInt(typeInput);
                if (flightId.isEmpty() || (sourceTaskType != 1 && sourceTaskType != 2)) {
                    log("ERROR", "handleRoaming", "flight_id 不能为空，source_task_type 仅支持 1 或 2");
                    break;
                }
                com.dji.flighthubsdk.bean.StartRoamingParams params =
                        new com.dji.flighthubsdk.bean.StartRoamingParams();
                params.setDroneSn(droneSn);
                params.setFlightId(flightId);
                params.setSourceTaskType(sourceTaskType);
                module.startRoaming(params, error ->
                        log(error.isSuccess() ? "INFO" : "ERROR", "handleRoaming",
                                "startRoaming: " + error));
                break;
            }
            case 2: {
                System.out.print("请输入 station_sns（逗号分隔，留空清空白名单）: ");
                String input = scanner.nextLine().trim();
                java.util.List<String> stationSns = new java.util.ArrayList<>();
                if (!input.isEmpty()) {
                    for (String stationSn : input.split(",")) {
                        if (!stationSn.trim().isEmpty()) stationSns.add(stationSn.trim());
                    }
                }
                com.dji.flighthubsdk.bean.SetRoamingStationSnParams params =
                        new com.dji.flighthubsdk.bean.SetRoamingStationSnParams();
                params.setDroneSn(droneSn);
                params.setStationSns(stationSns);
                module.setRoamingStationSn(params, (error, result) -> {
                    log(error.isSuccess() ? "INFO" : "ERROR", "handleRoaming",
                            "setRoamingStationSn: " + error);
                    if (result != null) {
                        for (com.dji.flighthubsdk.bean.RoamingStationSnResult item : result.getResults()) {
                            log("INFO", "handleRoaming", "  station_sn=" + item.getStationSn()
                                    + ", success=" + item.isSuccess()
                                    + ", fail_reason=" + item.getFailReason());
                        }
                    }
                });
                break;
            }
            case 3:
                module.getInFlightTaskTopos(java.util.Collections.singletonList(droneSn), (error, result) -> {
                    log(error.isSuccess() ? "INFO" : "ERROR", "handleRoaming",
                            "getInFlightTaskTopos: " + error);
                    if (result != null) {
                        for (com.dji.flighthubsdk.bean.InFlightTaskTopoItem item : result.getList()) {
                            log("INFO", "handleRoaming", "  drone_sn=" + item.getDroneSn()
                                    + ", flight_id=" + item.getFlightId()
                                    + ", takeoff_sn=" + item.getTakeoffSn());
                        }
                    }
                });
                break;
            case 4: {
                int listenId = module.listenTaskTopoChanged(event -> log("INFO", "handleRoaming",
                        "[推送] drone_sn=" + event.getDroneSn() + ", flight_id=" + event.getFlightId()
                                + ", takeoff_sn=" + event.getTakeoffSn()));
                if (listenId < 0) {
                    log("ERROR", "handleRoaming", "任务拓扑监听注册失败");
                    break;
                }
                log("INFO", "handleRoaming", "监听已注册 listen_id=" + listenId + "，输入 0 取消");
                while (running.get() && getUserChoice(0, 999) != 0) {
                    // 等待用户取消，推送在后台回调中输出。
                }
                module.cancelListenTaskTopoChanged(listenId);
                log("INFO", "handleRoaming", "已取消监听 listen_id=" + listenId);
                break;
            }
            default:
                break;
        }
        waitForKeyPress();
    }

    private static void handleCreateFlightTask() {
        printSeparator("航线任务 - 创建飞行计划");
        if (selectedDevice == null) {
            log("ERROR", "handleCreateFlightTask", "未选择设备");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入任务名称 (直接回车用默认 demo_task): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "demo_task";
        System.out.print("请输入 wayline_uuid (直接回车用默认): ");
        String waylineUuid = scanner.nextLine().trim();
        if (waylineUuid.isEmpty()) waylineUuid = "default-wayline-uuid";

        FlightTaskModule module = requireFlightTaskModule("handleCreateFlightTask");
        if (module == null) { waitForKeyPress(); return; }

        com.dji.flighthubsdk.bean.CreateTaskReq req = new com.dji.flighthubsdk.bean.CreateTaskReq();
        req.setName(name);
        req.setSn(getMainDeviceSn(selectedDevice));
        req.setWaylineUuid(waylineUuid);
        req.setLandingDockSn("");
        req.setTaskType(2);
        req.setBeginAt(1800000000L);
        req.setEndAt(1801000000L);
        req.setDeviceType(0);
        req.setTimeZone("Asia/Shanghai");
        req.setMinBatteryCapacity(30);
        req.setRthAltitude(80);
        req.setRthMode(1);
        req.setOutOfControlActionInFlight(1);
        List<Integer> segments = new ArrayList<>();
        segments.add(0);
        req.setWaylineSegmentIndexes(segments);
        req.setWaylinePrecisionType(1);
        req.setWaylineDuration(600);
        req.setBreakPointResume(false);
        List<String> tags = new ArrayList<>();
        tags.add("inspection");
        req.setTags(tags);
        module.createFlightTask(req, new FlightHubCallback.CreateFlightTaskCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CreateTaskResp result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleCreateFlightTask", "createFlightTask 失败, error: " + error);
                    return;
                }
                log("INFO", "handleCreateFlightTask", "创建成功, uuid: " + (result != null ? result.getUuid() : ""));
            }
        });
        waitForKeyPress();
    }

    private static void handleGetFlightTaskDetail() {
        printSeparator("航线任务 - 获取任务详情");
        System.out.print("请输入 task_uuid: ");
        String taskUuid = scanner.nextLine().trim();
        if (taskUuid.isEmpty()) { log("ERROR", "handleGetFlightTaskDetail", "task_uuid 不能为空"); waitForKeyPress(); return; }

        FlightTaskModule module = requireFlightTaskModule("handleGetFlightTaskDetail");
        if (module == null) { waitForKeyPress(); return; }

        module.getFlightTaskDetail(taskUuid, new FlightHubCallback.GetFlightTaskDetailCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.TaskDetail result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetFlightTaskDetail", "getFlightTaskDetail 失败, error: " + error);
                    return;
                }
                if (result != null) {
                    log("INFO", "handleGetFlightTaskDetail", "任务详情: uuid=" + result.getUuid() + ", name=" + result.getName() + ", status=" + result.getStatus() + ", sn=" + result.getSn());
                    log("INFO", "handleGetFlightTaskDetail", "  begin_at=" + result.getBeginAt()
                            + ", end_at=" + result.getEndAt()
                            + ", bid=" + result.getBid()
                            + ", created_at=" + result.getCreatedAt()
                            + ", completed_at=" + result.getCompletedAt()
                            + ", run_at=" + result.getRunAt()
                            + ", wayline_duration=" + result.getWaylineDuration()
                            + ", media_upload_status=" + result.getMediaUploadStatus()
                            + ", is_break_point_resume=" + result.isBreakPointResume()
                            + ", tags=" + result.getTags());
                }
            }
        });
        waitForKeyPress();
    }

    private static void handleEditFlightTask() {
        printSeparator("航线任务 - 编辑飞行计划");
        System.out.print("请输入 task_uuid: ");
        String taskUuid = scanner.nextLine().trim();
        if (taskUuid.isEmpty()) { log("ERROR", "handleEditFlightTask", "task_uuid 不能为空"); waitForKeyPress(); return; }
        System.out.print("请输入新任务名称 (直接回车跳过): ");
        String name = scanner.nextLine().trim();

        if (selectedDevice == null) { log("ERROR", "handleEditFlightTask", "未选择设备"); waitForKeyPress(); return; }
        FlightTaskModule module = requireFlightTaskModule("handleEditFlightTask");
        if (module == null) { waitForKeyPress(); return; }

        com.dji.flighthubsdk.bean.EditTaskReq req = new com.dji.flighthubsdk.bean.EditTaskReq();
        req.setUuid(taskUuid);
        if (!name.isEmpty()) req.setName(name);
        req.setSn(getMainDeviceSn(selectedDevice));
        module.editFlightTask(taskUuid, req, new FlightHubCallback.CreateFlightTaskCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CreateTaskResp result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleEditFlightTask", "editFlightTask 失败, error: " + error);
                    return;
                }
                log("INFO", "handleEditFlightTask", "编辑成功, uuid: " + (result != null ? result.getUuid() : ""));
            }
        });
        waitForKeyPress();
    }

    private static void handleDeleteFlightTask() {
        printSeparator("航线任务 - 删除飞行计划");
        System.out.print("请输入 task_uuid: ");
        String taskUuid = scanner.nextLine().trim();
        if (taskUuid.isEmpty()) { log("ERROR", "handleDeleteFlightTask", "task_uuid 不能为空"); waitForKeyPress(); return; }

        FlightTaskModule module = requireFlightTaskModule("handleDeleteFlightTask");
        if (module == null) { waitForKeyPress(); return; }

        module.deleteFlightTask(taskUuid, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleDeleteFlightTask", "deleteFlightTask 失败, error: " + error);
                    return;
                }
                log("INFO", "handleDeleteFlightTask", "删除成功");
            }
        });
        waitForKeyPress();
    }

    private static void handleUpdateFlightTaskStatus() {
        printSeparator("航线任务 - 更新任务状态");
        System.out.print("请输入 task_uuid: ");
        String taskUuid = scanner.nextLine().trim();
        if (taskUuid.isEmpty()) { log("ERROR", "handleUpdateFlightTaskStatus", "task_uuid 不能为空"); waitForKeyPress(); return; }
        System.out.print("请输入 status (0-10, 默认6挂起): ");
        String statusStr = scanner.nextLine().trim();
        int status = statusStr.isEmpty() ? 6 : Integer.parseInt(statusStr);

        FlightTaskModule module = requireFlightTaskModule("handleUpdateFlightTaskStatus");
        if (module == null) { waitForKeyPress(); return; }

        module.updateFlightTaskStatus(taskUuid, status, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleUpdateFlightTaskStatus", "updateFlightTaskStatus 失败, error: " + error);
                    return;
                }
                log("INFO", "handleUpdateFlightTaskStatus", "状态更新成功");
            }
        });
        waitForKeyPress();
    }

    private static void handleBatchUpdateFlightTaskStatus() {
        printSeparator("航线任务 - 批量更新任务状态");
        System.out.print("请输入 task_uuid 列表，逗号分隔: ");
        String line = scanner.nextLine().trim();
        System.out.print("请输入 status (0-10, 默认6): ");
        String statusStr = scanner.nextLine().trim();
        int status = statusStr.isEmpty() ? 6 : Integer.parseInt(statusStr);
        if (line.isEmpty()) { log("ERROR", "handleBatchUpdateFlightTaskStatus", "task_uuid 列表不能为空"); waitForKeyPress(); return; }

        String[] parts = line.split(",");
        com.dji.flighthubsdk.bean.BatchUpdateStatusReq req = new com.dji.flighthubsdk.bean.BatchUpdateStatusReq();
        java.util.List<String> taskUuids = new java.util.ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) taskUuids.add(t);
        }
        if (taskUuids.size() == 0) { log("ERROR", "handleBatchUpdateFlightTaskStatus", "无有效 task_uuid"); waitForKeyPress(); return; }

        FlightTaskModule module = requireFlightTaskModule("handleBatchUpdateFlightTaskStatus");
        if (module == null) { waitForKeyPress(); return; }

        req.setTaskUuids(taskUuids);
        req.setStatus(status);
        module.batchUpdateFlightTaskStatus(req, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleBatchUpdateFlightTaskStatus", "batchUpdateFlightTaskStatus 失败, error: " + error);
                    return;
                }
                log("INFO", "handleBatchUpdateFlightTaskStatus", "批量状态更新成功");
            }
        });
        waitForKeyPress();
    }

    private static void handleUpdateDeviceTcas() {
        printSeparator("航线任务 - 更新设备TCA");
        if (selectedDevice == null) {
            log("ERROR", "handleUpdateDeviceTcas", "未选择设备");
            waitForKeyPress();
            return;
        }
        FlightTaskModule module = requireFlightTaskModule("handleUpdateDeviceTcas");
        if (module == null) { waitForKeyPress(); return; }
        java.util.List<String> centerSn = java.util.Collections.singletonList(getMainDeviceSn(selectedDevice));
        module.updateDeviceTcas(centerSn, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleUpdateDeviceTcas", "updateDeviceTcas 失败, error: " + error);
                    return;
                }
                log("INFO", "handleUpdateDeviceTcas", "更新设备TCA成功");
            }
        });
        waitForKeyPress();
    }

    private static void handleGetSDKVersionInfo() {
        printSeparator("SDK信息 - 获取版本号和构建信息");
        String version = FlightHubSDK.getVersion();
        String buildInfo = FlightHubSDK.getBuildInfo();
        log("INFO", "handleGetSDKVersionInfo", "getVersion() 返回: [" + version + "]");
        log("INFO", "handleGetSDKVersionInfo", "getBuildInfo() 返回: [" + buildInfo + "]");
        if (version == null || version.isEmpty()) {
            log("WARN", "handleGetSDKVersionInfo", "版本号为空，请检查构建配置！");
        }
        if (buildInfo == null || buildInfo.isEmpty()) {
            log("WARN", "handleGetSDKVersionInfo", "构建信息为空，请检查构建配置！");
        }
        waitForKeyPress();
    }

    private static void handleGetDeviceRecommendation() {
        printSeparator("航线任务 - 获取设备推荐");
        if (selectedDevice == null) {
            log("ERROR", "handleGetDeviceRecommendation", "未选择设备");
            waitForKeyPress();
            return;
        }
        FlightTaskModule module = requireFlightTaskModule("handleGetDeviceRecommendation");
        if (module == null) { waitForKeyPress(); return; }
        com.dji.flighthubsdk.bean.DeviceRecommendationReq req = new com.dji.flighthubsdk.bean.DeviceRecommendationReq();
        req.setSn(java.util.Collections.singletonList(getMainDeviceSn(selectedDevice)));
        req.setStrategyId(1);
        module.getDeviceRecommendation(req, new FlightHubCallback.GetDeviceRecommendationCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceRecommendationResp result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetDeviceRecommendation", "getDeviceRecommendation 失败, error: " + error);
                    return;
                }
                if (result != null && result.getDevices() != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("设备推荐结果: ").append(result.getDevices().size()).append(" 个设备\n");
                    for (com.dji.flighthubsdk.bean.RecommendedDevice d : result.getDevices()) {
                        sb.append("  drone_sn=").append(d.getDroneSn())
                          .append(", gateway_sn=").append(d.getGatewaySn())
                          .append(", distance=").append(d.getDistance())
                          .append(", matched=").append(d.isMatched())
                          .append("\n");
                    }
                    log("INFO", "handleGetDeviceRecommendation", sb.toString());
                } else {
                    log("INFO", "handleGetDeviceRecommendation", "设备推荐结果为空");
                }
            }
        });
        waitForKeyPress();
    }

    private static void handleGetDeviceTcas() {
        printSeparator("航线任务 - 获取设备TCA");
        if (selectedDevice == null) {
            log("ERROR", "handleGetDeviceTcas", "未选择设备");
            waitForKeyPress();
            return;
        }
        FlightTaskModule module = requireFlightTaskModule("handleGetDeviceTcas");
        if (module == null) { waitForKeyPress(); return; }
        java.util.List<String> centerSn = java.util.Collections.singletonList(getMainDeviceSn(selectedDevice));
        module.getDeviceTcas(centerSn, new FlightHubCallback.GetDeviceTcasCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetTCAInfosResp result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetDeviceTcas", "getDeviceTcas 失败, error: " + error);
                    return;
                }
                if (result != null && result.getTcas() != null) {
                    log("INFO", "handleGetDeviceTcas", "TCA 数量: " + result.getTcas().size());
                    for (int i = 0; i < result.getTcas().size(); i++) {
                        log("INFO", "handleGetDeviceTcas", "  center_sn=" + result.getTcas().get(i).getCenterSn());
                    }
                }
            }
        });
        waitForKeyPress();
    }

    /**
     * 处理压力测试
     */
    private static void handleStressTest() {
        printSeparator("压力测试模式");
        
        // 检查设备
        if (selectedDevice == null) {
            log("ERROR", "handleStressTest", "未选择设备，无法进行压力测试");
            waitForKeyPress();
            return;
        }
        
        // 获取设备实例
        if (hasSubDevice(selectedDevice)) {
            drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
            if (drone == null) {
                log("ERROR", "handleStressTest", "获取无人机实例失败");
                waitForKeyPress();
                return;
            }
        }
        
        dock = getOrCreateDock(getMainDeviceSn(selectedDevice));

        workspace = FlightHubSDK.getWorkspaceInstance();
        com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
        
        // 配置测试参数
        System.out.println("压力测试配置：");
        System.out.println("  测试将持续运行，直到程序被终止");
        System.out.println("  如果出现崩溃或异常，说明存在 bug");
        System.out.println();
        System.out.print("请输入线程数 (默认 20): ");
        String threadCountStr = scanner.nextLine().trim();
        int threadCount = threadCountStr.isEmpty() ? 20 : Integer.parseInt(threadCountStr);
        
        System.out.print("请输入每线程迭代次数 (0=无限, 默认 0): ");
        String iterationsStr = scanner.nextLine().trim();
        int iterations = iterationsStr.isEmpty() ? 0 : Integer.parseInt(iterationsStr);
        
        System.out.print("请输入测试持续时间秒数 (0=无限, 默认 0): ");
        String durationStr = scanner.nextLine().trim();
        long duration = durationStr.isEmpty() ? 0 : Long.parseLong(durationStr);
        
        log("INFO", "handleStressTest", "启动压力测试...");
        log("INFO", "handleStressTest", "线程数: " + threadCount);
        log("INFO", "handleStressTest", "每线程迭代次数: " + (iterations > 0 ? iterations : "无限"));
        log("INFO", "handleStressTest", "测试持续时间: " + (duration > 0 ? duration + " 秒" : "无限"));
        log("INFO", "handleStressTest", "测试将持续运行，按 Enter 键停止");
        
        // 创建并启动压力测试
        stressTest = new StressTest(drone, dock, workspace, stream);
        stressTest.setTestParams(threadCount, iterations, duration);
        stressTest.start();
        
        // 注册关闭钩子，确保测试能正确停止
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (stressTest != null) {
                stressTest.stop();
            }
        }));
        
        // 等待用户按 Enter 键停止测试
        System.out.println();
        System.out.println("压力测试已启动，正在运行...");
        System.out.println("按 Enter 键停止测试");
        scanner.nextLine();
        
        // 停止测试
        if (stressTest != null) {
            stressTest.stop();
            stressTest = null;
        }
        
        log("INFO", "handleStressTest", "压力测试已停止");
        waitForKeyPress();
    }
    
    /**
     * 处理循环拉流（简化版）
     * 拉取飞机和机场码流，30s拉流/10s等待，循环往复
     */
    private static void handleCyclicLiveStream() {
        printSeparator("循环拉流");
        
        // 检查设备
        if (selectedDevice == null) {
            log("ERROR", "handleCyclicLiveStream", "未选择设备，无法进行循环拉流");
            waitForKeyPress();
            return;
        }
        
        // 检查是否已经在运行
        if (cyclicStreamRunning.get()) {
            log("WARN", "handleCyclicLiveStream", "循环拉流已经在运行中");
            waitForKeyPress();
            return;
        }
        
        log("INFO", "handleCyclicLiveStream", "启动循环拉流...");
        log("INFO", "handleCyclicLiveStream", "模式: 拉流30秒 -> 等待10秒 -> 循环往复");
        log("INFO", "handleCyclicLiveStream", "使用选项 [202] 可以停止循环拉流");
        
        // 设置运行标志
        cyclicStreamRunning.set(true);
        
        // 创建并启动循环拉流线程
        cyclicStreamThread = new Thread(() -> {
            int cycleCount = 0;
            
            while (cyclicStreamRunning.get()) {
                cycleCount++;
                log("INFO", "handleCyclicLiveStream", "========== 开始第 " + cycleCount + " 轮拉流 ==========");
                
                try {
                    // 先确保上一轮的模块已经完全停止
                    if (dockLiveModule != null || droneLiveModule != null) {
                        log("INFO", "handleCyclicLiveStream", "清理上一轮的模块...");
                        // 先取消订阅
                        if (dockLiveStreamListenId != -1 && dockLiveModule != null) {
                            try {
                                dockLiveModule.unSubscribeLiveStream(dockLiveStreamListenId);
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "清理时取消订阅机库直播流失败: " + e.getMessage());
                            }
                            dockLiveStreamListenId = -1;
                        }
                        if (droneLiveStreamListenId != -1 && droneLiveModule != null) {
                            try {
                                droneLiveModule.unSubscribeLiveStream(droneLiveStreamListenId);
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "清理时取消订阅无人机直播流失败: " + e.getMessage());
                            }
                            droneLiveStreamListenId = -1;
                        }
                        // 等待一下确保取消订阅完成
                        Thread.sleep(500);
                        // 然后停止
                        if (dockLiveModule != null) {
                            try {
                                dockLiveModule.stop();
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "清理时停止机库推流失败: " + e.getMessage());
                            }
                            dockLiveModule = null;
                        }
                        if (droneLiveModule != null) {
                            try {
                                droneLiveModule.stop();
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "清理时停止无人机推流失败: " + e.getMessage());
                            }
                            droneLiveModule = null;
                        }
                        // 再等待一下确保停止完成
                        Thread.sleep(500);
                    }
                    
                    // 先关闭之前的文件输出（如果有）
                    closeStreamOutputs();
                    
                    // 重置计数器
                    dockFrameCount = 0;
                    droneFrameCount = 0;
                    dockTotalBytes = 0;
                    droneTotalBytes = 0;
                    
                    // 初始化新的文件输出（每次都会生成新的带时间戳的文件名）
                    initStreamOutputs();
                    
                    // 获取机库实例并启动拉流
                    dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
                    if (dock != null) {
                        dock.getLiveDeviceModule().listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                            @Override
                            public void onResult(java.util.List<LiveInfo> ranges) {
                                if (!ranges.isEmpty() && cyclicStreamRunning.get()) {
                                    dockLiveModule = dock.getLiveStream(ranges.get(0));
                                    dockLiveModule.start(new FlightHubCallback.ResultCallbackWithError() {
                                        @Override
                                        public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                            if (error.isSuccess()) {
                                                log("INFO", "handleCyclicLiveStream", "机库直播流启动成功");
                                            } else {
                                                log("ERROR", "handleCyclicLiveStream", "机库直播流启动失败，错误码: " + error);
                                            }
                                        }
                                    });
                                    
                                    dockLiveStreamListenId = dockLiveModule.subscribeLiveStream(new FlightHubCallback.LiveStreamCallback() {
                                        @Override
                                        public void onResult(byte[] data, FrameInfo frameInfo) {
                                            dockFrameCount++;
                                            dockTotalBytes += data.length;
                                            if (dockFrameCount % 30 == 0) {
                                                log("INFO", "handleCyclicLiveStream", "收到机库直播流数据 #" + dockFrameCount);
                                            }
                                            writeDockStreamData(data);
                                        }
                                    });
                                }
                            }
                        });
                    }
                    
                    // 如果有子设备（无人机），也启动拉流
                    if (hasSubDevice(selectedDevice) && cyclicStreamRunning.get()) {
                        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
                        if (drone != null) {
                            drone.getLiveDeviceModule().listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
                                @Override
                                public void onResult(java.util.List<LiveInfo> ranges) {
                                    if (!ranges.isEmpty() && cyclicStreamRunning.get()) {
                                        LiveInfo liveInfo = null;
                                        for (LiveInfo range : ranges) {
                                            if (targetPayloadIndex.equals(range.getPayloadIndex())) {
                                                liveInfo = range;
                                                break;
                                            }
                                        }
                                        if (liveInfo == null && !ranges.isEmpty()) {
                                            liveInfo = ranges.get(0);
                                        }
                                        
                                        if (liveInfo != null) {
                                            droneLiveModule = drone.getLiveStream(liveInfo);
                                            droneLiveModule.start(new FlightHubCallback.ResultCallbackWithError() {
                                                @Override
                                                public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                                                    if (error.isSuccess()) {
                                                        log("INFO", "handleCyclicLiveStream", "无人机直播流启动成功");
                                                    } else {
                                                        log("ERROR", "handleCyclicLiveStream", "无人机直播流启动失败，错误码: " + error);
                                                    }
                                                }
                                            });
                                            
                                            droneLiveStreamListenId = droneLiveModule.subscribeLiveStream(new FlightHubCallback.LiveStreamCallback() {
                                                @Override
                                                public void onResult(byte[] data, FrameInfo frameInfo) {
                                                    if (data != null && data.length > 0) {
                                                        droneFrameCount++;
                                                        droneTotalBytes += data.length;
                                                        if (droneFrameCount % 30 == 0) {
                                                            log("INFO", "handleCyclicLiveStream", "收到无人机直播流数据 #" + droneFrameCount);
                                                        }
                                                        writeDroneStreamData(data);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                    }
                    
                    // 等待一小段时间，确保模块已经创建
                    Thread.sleep(2000);
                    
                    // 等待30秒（拉流时间）
                    log("INFO", "handleCyclicLiveStream", "拉流中... (30秒)");
                    for (int i = 0; i < 30 && cyclicStreamRunning.get(); i++) {
                        Thread.sleep(1000);
                    }
                    
                    // 停止拉流
                    if (cyclicStreamRunning.get()) {
                        log("INFO", "handleCyclicLiveStream", "停止拉流...");
                        // 先取消订阅，再停止（顺序很重要，避免崩溃）
                        if (dockLiveStreamListenId != -1 && dockLiveModule != null) {
                            try {
                                dockLiveModule.unSubscribeLiveStream(dockLiveStreamListenId);
                                dockLiveStreamListenId = -1;
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "取消订阅机库直播流失败: " + e.getMessage());
                            }
                        }
                        if (droneLiveStreamListenId != -1 && droneLiveModule != null) {
                            try {
                                droneLiveModule.unSubscribeLiveStream(droneLiveStreamListenId);
                                droneLiveStreamListenId = -1;
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "取消订阅无人机直播流失败: " + e.getMessage());
                            }
                        }
                        
                        // 等待一小段时间，确保取消订阅完成
                        Thread.sleep(500);
                        
                        // 然后停止模块
                        if (dockLiveModule != null) {
                            try {
                                dockLiveModule.stop();
                                dockLiveModule = null;
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "停止机库推流失败: " + e.getMessage());
                                dockLiveModule = null;
                            }
                        }
                        if (droneLiveModule != null) {
                            try {
                                droneLiveModule.stop();
                                droneLiveModule = null;
                            } catch (Exception e) {
                                log("ERROR", "handleCyclicLiveStream", "停止无人机推流失败: " + e.getMessage());
                                droneLiveModule = null;
                            }
                        }
                        
                        // 关闭文件输出（会记录保存的文件信息）
                        closeStreamOutputs();
                        log("INFO", "handleCyclicLiveStream", "第 " + cycleCount + " 轮拉流文件已保存");
                        log("INFO", "handleCyclicLiveStream", "========== 第 " + cycleCount + " 轮拉流完成 ==========");
                    }
                    
                    // 等待10秒（间隔时间）
                    if (cyclicStreamRunning.get()) {
                        log("INFO", "handleCyclicLiveStream", "等待中... (10秒)");
                        for (int i = 0; i < 10 && cyclicStreamRunning.get(); i++) {
                            Thread.sleep(1000);
                        }
                    }
                    
                } catch (InterruptedException e) {
                    log("INFO", "handleCyclicLiveStream", "循环拉流线程被中断");
                    break;
                } catch (Exception e) {
                    log("ERROR", "handleCyclicLiveStream", "循环拉流异常: " + e.getMessage());
                    e.printStackTrace();
                    // 发生异常时等待一下再继续
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
            
            // 清理资源
            log("INFO", "handleCyclicLiveStream", "循环拉流已停止，清理资源...");
            log("INFO", "handleCyclicLiveStream", "========== 循环拉流统计 ==========");
            log("INFO", "handleCyclicLiveStream", "总共完成了 " + cycleCount + " 轮拉流");
            log("INFO", "handleCyclicLiveStream", "====================================");
            handleStopStream();
            closeStreamOutputs();
            log("INFO", "handleCyclicLiveStream", "循环拉流线程退出");
        });
        
        cyclicStreamThread.setDaemon(true);
        cyclicStreamThread.start();
        
        log("INFO", "handleCyclicLiveStream", "循环拉流已启动（后台运行）");
        waitForKeyPress();
    }
    

    /**
     * 拉流压测
     */
    private static void handlePullStreamStressTest() {
        printSeparator("拉流压测");
        
        if (pullStreamStressTestRunning.get()) {
            System.out.println("拉流压测已在运行中，请先停止当前测试");
            return;
        }
        
        try {
        
        // 获取WHEP地址
        System.out.print("请输入WHEP拉流URL: ");
        String whepUrl = scanner.nextLine().trim();
        if (whepUrl.isEmpty()) {
            System.out.println("WHEP地址不能为空");
            return;
        }
        
        // 获取拉流路数
        System.out.print("请输入拉流路数: ");
        String input = scanner.nextLine().trim();
        int streamCount;
        try {
            streamCount = Integer.parseInt(input);
            if (streamCount < 1) {
                System.out.println("拉流路数必须是正整数");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("无效的拉流路数，请输入正整数");
            return;
        }
        
        System.out.println("开始拉流压测:");
        System.out.println("  WHEP地址: " + whepUrl);
        System.out.println("  拉流路数: " + streamCount);
        
        // 创建文件输出流（只保存最后一路）
        String filename = generateTimestampFilename("dji_pull_stream_stress", "h264");
        try {
            pullStreamStressTestOutput = new java.io.FileOutputStream(filename);
        } catch (Exception e) {
            System.err.println("创建文件失败: " + e.getMessage());
            return;
        }
        
        // 清空列表
        pullStreamModules.clear();
        pullStreamSubscribeIds.clear();
        
        // 设置运行标志
        pullStreamStressTestRunning.set(true);
        
        // 获取Stream实例（在循环外获取，提高效率）
        com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
        if (stream == null) {
            System.out.println("获取Stream失败");
            pullStreamStressTestRunning.set(false);
            return;
        }
        
        // 创建多个拉流模块
        final int finalStreamCount = streamCount;
        System.out.println("开始创建 " + streamCount + " 路拉流模块...");
        for (int i = 1; i <= streamCount; i++) {
            final int streamIndex = i;
            final String streamId = "stress_test_stream_" + streamIndex;
            
            System.out.println("[流" + streamIndex + "] 正在创建PullStreamModule，streamId: " + streamId);
            
            // 获取PullStreamModule
            com.dji.flighthubsdk.core.PullStreamModule pullStreamModule = stream.getPullStreamModule(streamId);
            if (pullStreamModule == null) {
                System.out.println("[流" + streamIndex + "] 获取PullStreamModule失败");
                continue;
            }
            
            System.out.println("[流" + streamIndex + "] PullStreamModule获取成功");
            pullStreamModules.add(pullStreamModule);
            
            // 设置错误回调
            pullStreamModule.setErrorCallback(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamErrorCallback() {
                @Override
                public void onPullStreamError(int errorCode, String errorMessage) {
                    System.out.println("[流" + streamIndex + "] 拉流错误: " + errorCode + " - " + errorMessage);
                }
            });
            
            // 订阅流数据
            final boolean isLastStream = (streamIndex == finalStreamCount);
            int subscribeId = pullStreamModule.subscribeStream(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamDataCallback() {
                private int frameCount = 0;
                private long totalBytes = 0;
                
                @Override
                public void onPullStreamData(byte[] data, com.dji.flighthubsdk.bean.FrameInfo frameInfo) {
                    frameCount++;
                    totalBytes += data.length;
                    
                    // 只保存最后一路的数据
                    if (isLastStream && pullStreamStressTestOutput != null) {
                        try {
                            pullStreamStressTestOutput.write(data);
                            pullStreamStressTestOutput.flush();
                        } catch (Exception e) {
                            System.err.println("[流" + streamIndex + "] 写入文件错误: " + e.getMessage());
                        }
                    }
                    
                    // 每10帧打印一次信息（区分不同路）
                    if (frameCount % 10 == 0) {
                        System.out.println("[流" + streamIndex + "] 帧数: " + frameCount + 
                                         ", 总字节数: " + totalBytes + 
                                         ", 当前帧大小: " + data.length + 
                                         ", 关键帧: " + frameInfo.isKeyFrame());
                    }
                }
            });
            
            if (subscribeId < 0) {
                System.out.println("[流" + streamIndex + "] 订阅流数据失败");
                pullStreamModules.remove(pullStreamModules.size() - 1); // 移除刚添加的模块
                continue;
            }
            
            pullStreamSubscribeIds.add(subscribeId);
            System.out.println("[流" + streamIndex + "] 订阅流数据成功，订阅ID: " + subscribeId);
            
            // 开始拉流
            pullStreamModule.start(whepUrl, new com.dji.flighthubsdk.core.PullStreamModule.PullStreamStartCallback() {
                @Override
                public void onPullStreamStart(FlightHubError error) {
                    if (error.isSuccess()) {
                        System.out.println("[流" + streamIndex + "] 拉流启动成功");
                    } else {
                        System.out.println("[流" + streamIndex + "] 拉流启动失败: " + error);
                    }
                }
            });
        }
        
        System.out.println("拉流压测已启动，共创建 " + pullStreamModules.size() + " 路拉流");
        System.out.println("按回车键停止压测...");
        scanner.nextLine();
        
        // 停止所有拉流
        stopPullStreamStressTest();
        } catch (Exception e) {
            System.err.println("拉流压测异常: " + e.getMessage());
            e.printStackTrace();
            pullStreamStressTestRunning.set(false);
            stopPullStreamStressTest();
        }
    }
    
    /**
     * 停止拉流压测
     */
    private static void stopPullStreamStressTest() {
        if (!pullStreamStressTestRunning.get()) {
            return;
        }
        
        System.out.println("正在停止拉流压测...");
        pullStreamStressTestRunning.set(false);
        
        // 停止所有拉流模块
        int size = Math.min(pullStreamModules.size(), pullStreamSubscribeIds.size());
        for (int i = 0; i < size; i++) {
            com.dji.flighthubsdk.core.PullStreamModule module = pullStreamModules.get(i);
            int subscribeId = pullStreamSubscribeIds.get(i);
            
            try {
                module.stop();
                module.unSubscribeStream(subscribeId);
                System.out.println("[流" + (i + 1) + "] 已停止");
            } catch (Exception e) {
                System.err.println("[流" + (i + 1) + "] 停止失败: " + e.getMessage());
            }
        }
        
        // 关闭文件输出流
        if (pullStreamStressTestOutput != null) {
            try {
                pullStreamStressTestOutput.close();
                System.out.println("文件已保存");
            } catch (Exception e) {
                System.err.println("关闭文件失败: " + e.getMessage());
            }
            pullStreamStressTestOutput = null;
        }
        
        // 清空列表
        pullStreamModules.clear();
        pullStreamSubscribeIds.clear();
        
        System.out.println("拉流压测已停止");
    }


    /**
     * 停止循环拉流
     */
    private static void handleStopCyclicLiveStream() {
        printSeparator("停止循环拉流");
        
        if (!cyclicStreamRunning.get()) {
            log("WARN", "handleStopCyclicLiveStream", "循环拉流未在运行");
            waitForKeyPress();
            return;
        }
        
        log("INFO", "handleStopCyclicLiveStream", "正在停止循环拉流...");
        cyclicStreamRunning.set(false);
        
        // 等待线程结束
        if (cyclicStreamThread != null && cyclicStreamThread.isAlive()) {
            try {
                cyclicStreamThread.join(5000); // 最多等待5秒
                if (cyclicStreamThread.isAlive()) {
                    log("WARN", "handleStopCyclicLiveStream", "循环拉流线程未在5秒内结束，可能仍在运行");
                } else {
                    log("INFO", "handleStopCyclicLiveStream", "循环拉流已成功停止");
                }
            } catch (InterruptedException e) {
                log("ERROR", "handleStopCyclicLiveStream", "等待线程结束被中断: " + e.getMessage());
            }
        }
        
        cyclicStreamThread = null;
        waitForKeyPress();
    }

    /**
     * 循环拉流压测
     * 支持多路 WHEP 拉流，循环往复进行测试
     */
    private static void handleCyclicPullStreamStressTest() {
        printSeparator("循环拉流压测");
        
        if (cyclicPullStreamStressTestRunning.get()) {
            System.out.println("循环拉流压测已在运行中，请先停止当前测试");
            waitForKeyPress();
            return;
        }
        
        try {
            // 获取WHEP地址
            System.out.print("请输入WHEP拉流URL: ");
            String whepUrl = scanner.nextLine().trim();
            if (whepUrl.isEmpty()) {
                System.out.println("WHEP地址不能为空");
                waitForKeyPress();
                return;
            }
            
            // 获取拉流路数
            System.out.print("请输入拉流路数: ");
            String input = scanner.nextLine().trim();
            int streamCount;
            try {
                streamCount = Integer.parseInt(input);
                if (streamCount < 1) {
                    System.out.println("拉流路数必须是正整数");
                    waitForKeyPress();
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("无效的拉流路数，请输入正整数");
                waitForKeyPress();
                return;
            }
            
            // 获取拉流时间
            System.out.print("请输入拉流时间（秒）: ");
            input = scanner.nextLine().trim();
            int pullDurationSec;
            try {
                pullDurationSec = Integer.parseInt(input);
                if (pullDurationSec < 1) {
                    System.out.println("拉流时间必须是正整数");
                    waitForKeyPress();
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("无效的拉流时间，请输入正整数");
                waitForKeyPress();
                return;
            }
            
            // 获取停止等待时间
            System.out.print("请输入停止等待时间（秒）: ");
            input = scanner.nextLine().trim();
            int stopDurationSec;
            try {
                stopDurationSec = Integer.parseInt(input);
                if (stopDurationSec < 0) {
                    System.out.println("停止等待时间必须是非负整数");
                    waitForKeyPress();
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("无效的停止等待时间，请输入非负整数");
                waitForKeyPress();
                return;
            }
            
            System.out.println();
            System.out.println("========================================");
            System.out.println("测试配置:");
            System.out.println("  WHEP地址: " + whepUrl);
            System.out.println("  拉流路数: " + streamCount);
            System.out.println("  拉流时间: " + pullDurationSec + " 秒");
            System.out.println("  停止等待时间: " + stopDurationSec + " 秒");
            System.out.println("========================================");
            System.out.println();
            System.out.println("按 Ctrl+C 或使用选项 [205] 停止测试");
            System.out.println();
            
            // 设置运行标志
            cyclicPullStreamStressTestRunning.set(true);
            
            // 创建并启动循环拉流线程
            cyclicPullStreamStressTestThread = new Thread(() -> {
                int cycleCount = 0;
                
                try {
                    while (cyclicPullStreamStressTestRunning.get()) {
                        cycleCount++;
                        System.out.println();
                        System.out.println("========================================");
                        System.out.println("========== 开始第 " + cycleCount + " 轮拉流 ==========");
                        System.out.println("时间: " + java.time.LocalDateTime.now().format(timeFormatter));
                        System.out.println("========================================");
                        
                        // 清理上一轮的资源
                        stopAllCyclicPullStreams();
                        
                        // 等待一小段时间，确保资源清理完成
                        Thread.sleep(500);
                        
                        // 创建新的拉流模块
                        com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
                        if (stream == null) {
                            System.err.println("获取Stream失败，停止循环拉流压测");
                            break;
                        }
                        
                        // 清空列表和统计信息
                        cyclicPullStreamModules.clear();
                        cyclicPullStreamSubscribeIds.clear();
                        cyclicStreamFrameCounts.clear();
                        cyclicStreamTotalBytes.clear();
                        
                        // 创建多个拉流模块
                        System.out.println("并行启动所有拉流...");
                        java.util.List<java.util.concurrent.CountDownLatch> startLatches = new java.util.ArrayList<>();
                        java.util.List<java.util.concurrent.atomic.AtomicInteger> startSuccessCounts = new java.util.ArrayList<>();
                        
                        for (int i = 1; i <= streamCount; i++) {
                            final int streamIndex = i;
                            final String streamId = "cyclic_stress_test_stream_" + streamIndex + "_" + System.currentTimeMillis();
                            
                            // 初始化统计信息
                            cyclicStreamFrameCounts.add(new java.util.concurrent.atomic.AtomicInteger(0));
                            cyclicStreamTotalBytes.add(new java.util.concurrent.atomic.AtomicLong(0));
                            
                            // 获取PullStreamModule
                            com.dji.flighthubsdk.core.PullStreamModule pullStreamModule = stream.getPullStreamModule(streamId);
                            if (pullStreamModule == null) {
                                System.err.println("[流" + streamIndex + "] 获取PullStreamModule失败");
                                continue;
                            }
                            
                            cyclicPullStreamModules.add(pullStreamModule);
                            
                            // 设置错误回调
                            pullStreamModule.setErrorCallback(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamErrorCallback() {
                                @Override
                                public void onPullStreamError(int errorCode, String errorMessage) {
                                    System.out.println("[流" + streamIndex + "] 拉流错误: " + errorCode + " - " + errorMessage);
                                }
                            });
                            
                            // 订阅流数据
                            final int streamIndexForCallback = streamIndex - 1; // 用于访问统计信息数组
                            int subscribeId = pullStreamModule.subscribeStream(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamDataCallback() {
                                @Override
                                public void onPullStreamData(byte[] data, com.dji.flighthubsdk.bean.FrameInfo frameInfo) {
                                    if (streamIndexForCallback < cyclicStreamFrameCounts.size() && 
                                        streamIndexForCallback < cyclicStreamTotalBytes.size()) {
                                        cyclicStreamFrameCounts.get(streamIndexForCallback).incrementAndGet();
                                        cyclicStreamTotalBytes.get(streamIndexForCallback).addAndGet(data.length);
                                    }
                                }
                            });
                            
                            if (subscribeId < 0) {
                                System.err.println("[流" + streamIndex + "] 订阅流数据失败");
                                cyclicPullStreamModules.remove(cyclicPullStreamModules.size() - 1);
                                cyclicStreamFrameCounts.remove(cyclicStreamFrameCounts.size() - 1);
                                cyclicStreamTotalBytes.remove(cyclicStreamTotalBytes.size() - 1);
                                continue;
                            }
                            
                            cyclicPullStreamSubscribeIds.add(subscribeId);
                            
                            // 创建 CountDownLatch 用于同步启动
                            java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
                            java.util.concurrent.atomic.AtomicInteger startSuccessCount = new java.util.concurrent.atomic.AtomicInteger(0);
                            startLatches.add(startLatch);
                            startSuccessCounts.add(startSuccessCount);
                            
                            // 异步启动拉流
                            final java.util.concurrent.CountDownLatch finalStartLatch = startLatch;
                            final java.util.concurrent.atomic.AtomicInteger finalStartSuccessCount = startSuccessCount;
                            new Thread(() -> {
                                try {
                                    pullStreamModule.start(whepUrl, new com.dji.flighthubsdk.core.PullStreamModule.PullStreamStartCallback() {
                                        @Override
                                        public void onPullStreamStart(FlightHubError error) {
                                            if (error.isSuccess()) {
                                                System.out.println("[流" + streamIndex + "] 拉流启动成功");
                                                finalStartSuccessCount.incrementAndGet();
                                            } else {
                                                System.err.println("[流" + streamIndex + "] 拉流启动失败: " + error);
                                            }
                                            finalStartLatch.countDown();
                                        }
                                    });
                                } catch (Exception e) {
                                    System.err.println("[流" + streamIndex + "] 启动拉流异常: " + e.getMessage());
                                    finalStartLatch.countDown();
                                }
                            }).start();
                        }
                        
                        // 等待所有启动操作完成
                        for (java.util.concurrent.CountDownLatch latch : startLatches) {
                            try {
                                latch.await();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        
                        // 统计成功启动的路数
                        int totalStartSuccess = 0;
                        for (java.util.concurrent.atomic.AtomicInteger count : startSuccessCounts) {
                            totalStartSuccess += count.get();
                        }
                        
                        System.out.println("成功启动 " + totalStartSuccess + "/" + streamCount + " 路拉流");
                        
                        // 等待拉流时间
                        System.out.println("拉流中... (" + pullDurationSec + " 秒)");
                        for (int i = 0; i < pullDurationSec && cyclicPullStreamStressTestRunning.get(); i++) {
                            Thread.sleep(1000);
                            if ((i + 1) % 10 == 0) {
                                System.out.println("  已拉流 " + (i + 1) + "/" + pullDurationSec + " 秒");
                            }
                        }
                        
                        if (!cyclicPullStreamStressTestRunning.get()) {
                            break;
                        }
                        
                        // 等待一小段时间，确保所有回调都已完成
                        Thread.sleep(100);
                        
                        // 读取统计信息
                        System.out.println();
                        System.out.println("第 " + cycleCount + " 轮拉流统计:");
                        int totalFrames = 0;
                        long totalBytes = 0;
                        java.util.List<java.util.Map<String, Long>> stats = new java.util.ArrayList<>();
                        
                        for (int i = 0; i < cyclicPullStreamModules.size(); i++) {
                            int frames = cyclicStreamFrameCounts.get(i).get();
                            long bytes = cyclicStreamTotalBytes.get(i).get();
                            stats.add(new java.util.HashMap<String, Long>() {{
                                put("frames", (long) frames);
                                put("bytes", bytes);
                            }});
                            totalFrames += frames;
                            totalBytes += bytes;
                        }
                        
                        // 打印统计信息
                        for (int i = 0; i < stats.size(); i++) {
                            long frames = stats.get(i).get("frames");
                            long bytes = stats.get(i).get("bytes");
                            System.out.print("  [流" + (i + 1) + "] 帧数: " + frames + ", 总字节数: " + bytes);
                            if (frames == 0) {
                                System.out.print(" (警告: 未收到数据)");
                            }
                            System.out.println();
                        }
                        
                        System.out.println("  总计: 帧数 " + totalFrames + ", 总字节数 " + totalBytes);
                        System.out.println("========== 第 " + cycleCount + " 轮拉流完成 ==========");
                        
                        // 停止所有拉流
                        System.out.println("并行停止所有拉流...");
                        stopAllCyclicPullStreams();
                        
                        // 等待停止时间
                        if (stopDurationSec > 0 && cyclicPullStreamStressTestRunning.get()) {
                            System.out.println("等待中... (" + stopDurationSec + " 秒)");
                            for (int i = 0; i < stopDurationSec && cyclicPullStreamStressTestRunning.get(); i++) {
                                Thread.sleep(1000);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("循环拉流压测线程被中断");
                } catch (Exception e) {
                    System.err.println("循环拉流压测异常: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // 清理资源
                    stopAllCyclicPullStreams();
                    cyclicPullStreamStressTestRunning.set(false);
                    
                    System.out.println();
                    System.out.println("========== 循环拉流压测统计 ==========");
                    System.out.println("总共完成了 " + cycleCount + " 轮拉流");
                    System.out.println("====================================");
                    System.out.println("循环拉流压测已停止");
                }
            });
            
            cyclicPullStreamStressTestThread.start();
            System.out.println("循环拉流压测已启动（后台运行）");
            waitForKeyPress();
            
        } catch (Exception e) {
            System.err.println("循环拉流压测异常: " + e.getMessage());
            e.printStackTrace();
            cyclicPullStreamStressTestRunning.set(false);
            waitForKeyPress();
        }
    }
    
    /**
     * 停止所有循环拉流（内部方法）
     */
    private static void stopAllCyclicPullStreams() {
        int size = Math.min(cyclicPullStreamModules.size(), cyclicPullStreamSubscribeIds.size());
        for (int i = 0; i < size; i++) {
            com.dji.flighthubsdk.core.PullStreamModule module = cyclicPullStreamModules.get(i);
            int subscribeId = cyclicPullStreamSubscribeIds.get(i);
            
            try {
                module.stop();
                module.unSubscribeStream(subscribeId);
            } catch (Exception e) {
                // 忽略停止错误，继续停止其他流
            }
        }
        
        cyclicPullStreamModules.clear();
        cyclicPullStreamSubscribeIds.clear();
        cyclicStreamFrameCounts.clear();
        cyclicStreamTotalBytes.clear();
    }
    
    /**
     * 停止循环拉流压测
     */
    private static void handleStopCyclicPullStreamStressTest() {
        printSeparator("停止循环拉流压测");
        
        if (!cyclicPullStreamStressTestRunning.get()) {
            System.out.println("循环拉流压测未在运行");
            waitForKeyPress();
            return;
        }
        
        System.out.println("正在停止循环拉流压测...");
        stopCyclicPullStreamStressTest();
        waitForKeyPress();
    }
    
    /**
     * 停止循环拉流压测（非交互式，无等待按键）
     */
    private static void stopCyclicPullStreamStressTest() {
        if (!cyclicPullStreamStressTestRunning.get()) {
            return;
        }
        
        log("INFO", "stopCyclicPullStreamStressTest", "正在停止循环拉流压测...");
        cyclicPullStreamStressTestRunning.set(false);
        
        // 等待线程结束
        if (cyclicPullStreamStressTestThread != null && cyclicPullStreamStressTestThread.isAlive()) {
            try {
                cyclicPullStreamStressTestThread.join(10000); // 最多等待10秒
                if (cyclicPullStreamStressTestThread.isAlive()) {
                    log("WARN", "stopCyclicPullStreamStressTest", "警告: 循环拉流压测线程未在10秒内结束，可能仍在运行");
                } else {
                    log("INFO", "stopCyclicPullStreamStressTest", "循环拉流压测已成功停止");
                }
            } catch (InterruptedException e) {
                log("ERROR", "stopCyclicPullStreamStressTest", "等待线程结束被中断: " + e.getMessage());
            }
        }
        
        cyclicPullStreamStressTestThread = null;
    }
    
    /**
     * 自动启动循环拉流压测（非交互式，使用固定参数）
     * 参数固定为：
     * - WHEP地址: http://xx.xx.xx.xx/rtc/v1/whep/?app=live&stream=livestream
     * - 拉流路数: 120路
     * - 拉流时间: 50秒
     * - 停止等待时间: 10秒
     */
    private static void startCyclicPullStreamStressTestAuto() {
        // 固定参数
        final String whepUrl = "http://xx.xx.xx.xx/rtc/v1/whep/?app=live&stream=livestream";
        final int streamCount = 120;
        final int pullDurationSec = 50;
        final int stopDurationSec = 10;
        
        log("INFO", "startCyclicPullStreamStressTestAuto", "========================================");
        log("INFO", "startCyclicPullStreamStressTestAuto", "自动启动循环拉流压测");
        log("INFO", "startCyclicPullStreamStressTestAuto", "测试配置:");
        log("INFO", "startCyclicPullStreamStressTestAuto", "  WHEP地址: " + whepUrl);
        log("INFO", "startCyclicPullStreamStressTestAuto", "  拉流路数: " + streamCount);
        log("INFO", "startCyclicPullStreamStressTestAuto", "  拉流时间: " + pullDurationSec + " 秒");
        log("INFO", "startCyclicPullStreamStressTestAuto", "  停止等待时间: " + stopDurationSec + " 秒");
        log("INFO", "startCyclicPullStreamStressTestAuto", "========================================");
        
        if (cyclicPullStreamStressTestRunning.get()) {
            log("WARN", "startCyclicPullStreamStressTestAuto", "循环拉流压测已在运行中");
            return;
        }
        
        // 设置运行标志
        cyclicPullStreamStressTestRunning.set(true);
        
        // 创建并启动循环拉流线程
        cyclicPullStreamStressTestThread = new Thread(() -> {
            int cycleCount = 0;
            
            try {
                while (cyclicPullStreamStressTestRunning.get()) {
                    cycleCount++;
                    log("INFO", "cyclicPullStreamStressTest", "========================================");
                    log("INFO", "cyclicPullStreamStressTest", "========== 开始第 " + cycleCount + " 轮拉流 ==========");
                    log("INFO", "cyclicPullStreamStressTest", "时间: " + java.time.LocalDateTime.now().format(timeFormatter));
                    log("INFO", "cyclicPullStreamStressTest", "========================================");
                    
                    // 清理上一轮的资源
                    stopAllCyclicPullStreams();
                    
                    // 等待一小段时间，确保资源清理完成
                    Thread.sleep(500);
                    
                    // 创建新的拉流模块
                    com.dji.flighthubsdk.core.Stream stream = FlightHubSDK.getStream();
                    if (stream == null) {
                        log("ERROR", "cyclicPullStreamStressTest", "获取Stream失败，停止循环拉流压测");
                        break;
                    }
                    
                    // 清空列表和统计信息
                    cyclicPullStreamModules.clear();
                    cyclicPullStreamSubscribeIds.clear();
                    cyclicStreamFrameCounts.clear();
                    cyclicStreamTotalBytes.clear();
                    
                    // 创建多个拉流模块
                    log("INFO", "cyclicPullStreamStressTest", "并行启动所有拉流...");
                    java.util.List<java.util.concurrent.CountDownLatch> startLatches = new java.util.ArrayList<>();
                    java.util.List<java.util.concurrent.atomic.AtomicInteger> startSuccessCounts = new java.util.ArrayList<>();
                    
                    for (int i = 1; i <= streamCount; i++) {
                        final int streamIndex = i;
                        final String streamId = "cyclic_stress_test_stream_" + streamIndex + "_" + System.currentTimeMillis();
                        
                        // 初始化统计信息
                        cyclicStreamFrameCounts.add(new java.util.concurrent.atomic.AtomicInteger(0));
                        cyclicStreamTotalBytes.add(new java.util.concurrent.atomic.AtomicLong(0));
                        
                        // 获取PullStreamModule
                        com.dji.flighthubsdk.core.PullStreamModule pullStreamModule = stream.getPullStreamModule(streamId);
                        if (pullStreamModule == null) {
                            log("ERROR", "cyclicPullStreamStressTest", "[流" + streamIndex + "] 获取PullStreamModule失败");
                            continue;
                        }
                        
                        cyclicPullStreamModules.add(pullStreamModule);
                        
                        // 设置错误回调
                        pullStreamModule.setErrorCallback(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamErrorCallback() {
                            @Override
                            public void onPullStreamError(int errorCode, String errorMessage) {
                                log("ERROR", "cyclicPullStreamStressTest", "[流" + streamIndex + "] 拉流错误: " + errorCode + " - " + errorMessage);
                            }
                        });
                        
                        // 订阅流数据
                        final int streamIndexForCallback = streamIndex - 1; // 用于访问统计信息数组
                        int subscribeId = pullStreamModule.subscribeStream(new com.dji.flighthubsdk.core.PullStreamModule.PullStreamDataCallback() {
                            @Override
                            public void onPullStreamData(byte[] data, com.dji.flighthubsdk.bean.FrameInfo frameInfo) {
                                if (streamIndexForCallback < cyclicStreamFrameCounts.size() && 
                                    streamIndexForCallback < cyclicStreamTotalBytes.size()) {
                                    cyclicStreamFrameCounts.get(streamIndexForCallback).incrementAndGet();
                                    cyclicStreamTotalBytes.get(streamIndexForCallback).addAndGet(data.length);
                                }
                            }
                        });
                        
                        if (subscribeId < 0) {
                            log("ERROR", "cyclicPullStreamStressTest", "[流" + streamIndex + "] 订阅流数据失败");
                            cyclicPullStreamModules.remove(cyclicPullStreamModules.size() - 1);
                            cyclicStreamFrameCounts.remove(cyclicStreamFrameCounts.size() - 1);
                            cyclicStreamTotalBytes.remove(cyclicStreamTotalBytes.size() - 1);
                            continue;
                        }
                        
                        cyclicPullStreamSubscribeIds.add(subscribeId);
                        
                        // 创建 CountDownLatch 用于同步启动
                        java.util.concurrent.CountDownLatch startLatch = new java.util.concurrent.CountDownLatch(1);
                        java.util.concurrent.atomic.AtomicInteger startSuccessCount = new java.util.concurrent.atomic.AtomicInteger(0);
                        startLatches.add(startLatch);
                        startSuccessCounts.add(startSuccessCount);
                        
                        // 异步启动拉流
                        final java.util.concurrent.CountDownLatch finalStartLatch = startLatch;
                        final java.util.concurrent.atomic.AtomicInteger finalStartSuccessCount = startSuccessCount;
                        new Thread(() -> {
                            try {
                                pullStreamModule.start(whepUrl, new com.dji.flighthubsdk.core.PullStreamModule.PullStreamStartCallback() {
                                    @Override
                                    public void onPullStreamStart(FlightHubError error) {
                                        if (error.isSuccess()) {
                                            if (streamIndex <= 5 || streamIndex % 20 == 0) {
                                                // 只打印前5路和每20路的启动信息，避免日志过多
                                                log("INFO", "cyclicPullStreamStressTest", "[流" + streamIndex + "] 拉流启动成功");
                                            }
                                            finalStartSuccessCount.incrementAndGet();
                                        } else {
                                            log("ERROR", "cyclicPullStreamStressTest", "[流" + streamIndex + "] 拉流启动失败: " + error);
                                        }
                                        finalStartLatch.countDown();
                                    }
                                });
                            } catch (Exception e) {
                                log("ERROR", "cyclicPullStreamStressTest", "[流" + streamIndex + "] 启动拉流异常: " + e.getMessage());
                                finalStartLatch.countDown();
                            }
                        }).start();
                    }
                    
                    // 等待所有启动操作完成
                    for (java.util.concurrent.CountDownLatch latch : startLatches) {
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    
                    // 统计成功启动的路数
                    int totalStartSuccess = 0;
                    for (java.util.concurrent.atomic.AtomicInteger count : startSuccessCounts) {
                        totalStartSuccess += count.get();
                    }
                    
                    log("INFO", "cyclicPullStreamStressTest", "成功启动 " + totalStartSuccess + "/" + streamCount + " 路拉流");
                    
                    // 等待拉流时间
                    log("INFO", "cyclicPullStreamStressTest", "拉流中... (" + pullDurationSec + " 秒)");
                    for (int i = 0; i < pullDurationSec && cyclicPullStreamStressTestRunning.get(); i++) {
                        Thread.sleep(1000);
                        if ((i + 1) % 10 == 0) {
                            log("INFO", "cyclicPullStreamStressTest", "  已拉流 " + (i + 1) + "/" + pullDurationSec + " 秒");
                        }
                    }
                    
                    if (!cyclicPullStreamStressTestRunning.get()) {
                        break;
                    }
                    
                    // 等待一小段时间，确保所有回调都已完成
                    Thread.sleep(100);
                    
                    // 读取统计信息
                    log("INFO", "cyclicPullStreamStressTest", "");
                    log("INFO", "cyclicPullStreamStressTest", "第 " + cycleCount + " 轮拉流统计:");
                    int totalFrames = 0;
                    long totalBytes = 0;
                    
                    // 只打印汇总信息，不打印每路详情（避免日志过多）
                    for (int i = 0; i < cyclicPullStreamModules.size(); i++) {
                        int frames = cyclicStreamFrameCounts.get(i).get();
                        long bytes = cyclicStreamTotalBytes.get(i).get();
                        totalFrames += frames;
                        totalBytes += bytes;
                    }
                    
                    // 计算平均值
                    int avgFrames = cyclicPullStreamModules.size() > 0 ? totalFrames / cyclicPullStreamModules.size() : 0;
                    long avgBytes = cyclicPullStreamModules.size() > 0 ? totalBytes / cyclicPullStreamModules.size() : 0;
                    
                    log("INFO", "cyclicPullStreamStressTest", "  总路数: " + cyclicPullStreamModules.size());
                    log("INFO", "cyclicPullStreamStressTest", "  总计: 帧数 " + totalFrames + ", 总字节数 " + totalBytes);
                    log("INFO", "cyclicPullStreamStressTest", "  平均: 每路帧数 " + avgFrames + ", 每路字节数 " + avgBytes);
                    log("INFO", "cyclicPullStreamStressTest", "========== 第 " + cycleCount + " 轮拉流完成 ==========");
                    
                    // 停止所有拉流
                    log("INFO", "cyclicPullStreamStressTest", "并行停止所有拉流...");
                    stopAllCyclicPullStreams();
                    
                    // 等待停止时间
                    if (stopDurationSec > 0 && cyclicPullStreamStressTestRunning.get()) {
                        log("INFO", "cyclicPullStreamStressTest", "等待中... (" + stopDurationSec + " 秒)");
                        for (int i = 0; i < stopDurationSec && cyclicPullStreamStressTestRunning.get(); i++) {
                            Thread.sleep(1000);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log("INFO", "cyclicPullStreamStressTest", "循环拉流压测线程被中断");
            } catch (Exception e) {
                log("ERROR", "cyclicPullStreamStressTest", "循环拉流压测异常: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // 清理资源
                stopAllCyclicPullStreams();
                cyclicPullStreamStressTestRunning.set(false);
                
                log("INFO", "cyclicPullStreamStressTest", "");
                log("INFO", "cyclicPullStreamStressTest", "========== 循环拉流压测统计 ==========");
                log("INFO", "cyclicPullStreamStressTest", "总共完成了 " + cycleCount + " 轮拉流");
                log("INFO", "cyclicPullStreamStressTest", "====================================");
                log("INFO", "cyclicPullStreamStressTest", "循环拉流压测已停止");
            }
        });
        
        cyclicPullStreamStressTestThread.start();
        log("INFO", "startCyclicPullStreamStressTestAuto", "循环拉流压测已启动（后台运行）");
    }

    private static RelayControlModule getRelayControlModule(String sn) {
        com.dji.flighthubsdk.core.Relay relay = FlightHubSDK.getRelayInstance(sn);
        if (relay == null) {
            log("ERROR", "getRelayControlModule", "relay not found, sn=" + sn);
            return null;
        }
        RelayControlModule module = relay.getRelayControlModule();
        if (module == null) {
            log("ERROR", "getRelayControlModule", "relay control module not found, sn=" + sn);
        }
        return module;
    }

    private static RtkCalibrationModule getRtkCalibrationModuleForDock(String sn) {
        Dock dock = getOrCreateDock(sn);
        if (dock == null) {
            log("ERROR", "getRtkCalibrationModuleForDock", "dock not found, sn=" + sn);
            return null;
        }
        RtkCalibrationModule module = dock.getRtkCalibrationModule();
        if (module == null) {
            log("ERROR", "getRtkCalibrationModuleForDock", "rtk calibration module not found, sn=" + sn);
        }
        return module;
    }

    private static RtkCalibrationModule getRtkCalibrationModuleForRelay(String sn) {
        com.dji.flighthubsdk.core.Relay relay = FlightHubSDK.getRelayInstance(sn);
        if (relay == null) {
            log("ERROR", "getRtkCalibrationModuleForRelay", "relay not found, sn=" + sn);
            return null;
        }
        RtkCalibrationModule module = relay.getRtkCalibrationModule();
        if (module == null) {
            log("ERROR", "getRtkCalibrationModuleForRelay", "rtk calibration module not found, sn=" + sn);
        }
        return module;
    }

    private static void handleDeviceDebug() {
        printSeparator("设备调试模式");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        final String device_sn = "PLACEHOLDER_DOCK_SN";
        Dock dock = FlightHubSDK.getDockInstance(device_sn);
        if (dock == null) {
            log("ERROR", "handleDeviceDebug", "获取dock失败, sn=" + device_sn);
            return;
        }
        DeviceDebugModule device_debug_module = dock.getDeviceDebugModule();
        System.out.println(" 开启设备调试模块 [1]");
        System.out.println(" 关闭设备调试模块 [2]");
        System.out.println(" 打开仓盖 [3]");
        System.out.println(" 关闭仓盖 [4]");
        System.out.println(" 设备重启 [5]");
        System.out.println(" 设备格式化 [6]");
        System.out.println(" 打开飞机 [7]");
        System.out.println(" 关闭飞机 [8]");
        System.out.println(" 打开充电 [9]");
        System.out.println(" 关闭充电 [10]");
        System.out.println(" 查询开盖进度 [11]");
        System.out.println(" 查询关盖进度 [12]");
        System.out.println(" 查询重启进度 [13]");
        System.out.println(" 机场空调模式设置 [14]");
        System.out.println(" 机场静音模式设置 [15]");
        System.out.println(" 开始网络测速 [16]");
        System.out.println(" 停止网络测速 [17]");
        System.out.println(" 查询网络测速进度 [18]");
        System.out.println(" 机场网络RTK校准 [19]");
        System.out.println(" 机场手动RTK校准 [20]");
        System.out.println(" 中继网络RTK校准 [21]");
        System.out.println(" 中继手动RTK校准 [22]");
        System.out.println(" 设置限距 [23]");
        System.out.println(" 设置限高 [24]");
        System.out.println(" 设置避障 [25]");
        System.out.println(" 设置返航保留电量 [26]");
        System.out.println(" 设置夜航灯 [27]");
        System.out.println(" 切换声光报警开关 [28]");
        System.out.println(" eSIM运营商切换 [29]");
        System.out.println(" SIM卡槽切换 [30]");
         System.out.println(" 设置电池存储模式 [31]");
         System.out.println("--- 中继站调试接口 ---");
         System.out.println(" 设置中继广播隐藏 [32]");
         System.out.println(" 设置中继名称(SSID) [33]");
         System.out.println(" 设置中继密码 [34]");
         System.out.println(" 设置图传搜索模式 [35]");
         System.out.println(" 设置中继占用状态 [36]");
         System.out.println(" 中继重启 [37]");
         System.out.println(" 中继断开连接 [38]");
         System.out.println(" 查询飞机状态 [39]");
         System.out.println(" 查询机场状态 [40]");
         System.out.println("--- EA340 KOC3 FO 新增 ---");
         System.out.println(" 发起手动换电 [42]");
         System.out.println(" 查询手动换电进度 [43]");
         System.out.println(" Dongle 电源开关 [44]");
         System.out.println(" 机场低功耗开关 [45]");
         System.out.println(" 查询 eSIM 运营商切换进度 [46]");
         System.out.print(" 请输入选择: ");
        Scanner scanner = new Scanner(System.in);
        int userChoice = scanner.nextInt();

        switch (userChoice) {
            case 1: // 开启调试模式
                device_debug_module.openDebugMode("PLACEHOLDER_DOCK_SN", new FlightHubCallback.OpenDebugModeCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDebugModeResult result) {
                        System.out.println("openDebugMode result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 2: // 关闭调试模式
                device_debug_module.closeDebugMode("PLACEHOLDER_DOCK_SN", new FlightHubCallback.CloseDebugModeCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDebugModeResult result) {
                        System.out.println("closeDebugMode result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 3: // 打开仓盖
                device_debug_module.openDockCover("PLACEHOLDER_DOCK_SN", new FlightHubCallback.OpenDockCoverCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDockCoverResult result) {
                        System.out.println("openDockCover result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 4: // 关闭仓盖
                device_debug_module.closeDockCover("PLACEHOLDER_DOCK_SN", new FlightHubCallback.CloseDockCoverCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDockCoverResult result) {
                        System.out.println("closeDockCover result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 5: // 设备重启
                device_debug_module.deviceReboot("PLACEHOLDER_DOCK_SN", new FlightHubCallback.DeviceRebootCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceRebootResult result) {
                        System.out.println("deviceReboot result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 6: // 设备格式化
                device_debug_module.deviceFormat("PLACEHOLDER_DOCK_SN", new FlightHubCallback.DeviceFormatCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceFormatResult result) {
                        System.out.println("deviceFormat result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 7: // 打开飞机
                device_debug_module.openDrone("PLACEHOLDER_DOCK_SN", new FlightHubCallback.OpenDroneCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDroneResult result) {
                        System.out.println("openDrone result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 8: // 关闭飞机
                device_debug_module.closeDrone("PLACEHOLDER_DOCK_SN", new FlightHubCallback.CloseDroneCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDroneResult result) {
                        System.out.println("closeDrone result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 9: // 打开充电
                device_debug_module.openCharge("PLACEHOLDER_DOCK_SN", new FlightHubCallback.OpenChargeCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenChargeResult result) {
                        System.out.println("openCharge result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 10: // 关闭充电
                device_debug_module.closeCharge("PLACEHOLDER_DOCK_SN", new FlightHubCallback.CloseChargeCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseChargeResult result) {
                        System.out.println("closeCharge result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 11: // 查询开盖进度
                device_debug_module.getOpenDockCoverProgress("PLACEHOLDER_DOCK_SN", new FlightHubCallback.OpenDockCoverProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OpenDockCoverProgressResult result) {
                        System.out.println("getOpenDockCoverProgress result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 12: // 查询关盖进度
                device_debug_module.getCloseDockCoverProgress("PLACEHOLDER_DOCK_SN", new FlightHubCallback.CloseDockCoverProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CloseDockCoverProgressResult result) {
                        System.out.println("getCloseDockCoverProgress result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 13: // 查询重启进度
                device_debug_module.getDeviceRebootProgress("PLACEHOLDER_DOCK_SN", new FlightHubCallback.DeviceRebootProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceRebootProgressResult result) {
                        System.out.println("getDeviceRebootProgress result: " + error + ", " + result.getMessage());
                    }
                });
                break;
            case 14: // 机场空调模式设置
                device_debug_module.setDockAirConditionerMode("PLACEHOLDER_DOCK_SN", 1, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        System.out.println("setDockAirConditionerMode result: " + error);
                    }
                });
            break;
            case 15: // 机场静音模式设置
                device_debug_module.setDockSilentMode("PLACEHOLDER_DOCK_SN", 1, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        System.out.println("setDockAirConditionerMode result: " + error);
                    }
                });
                break;
            case 16: { // 开始网络测速
                if (dock == null) {
                    break;
                }
                NetworkTestModule netModule = dock.getNetworkTestModule();
                if (netModule == null) {
                    break;
                }
                netModule.startNetworkTest((error, result) -> {
                    System.out.println("startNetworkTest result: " + error + ", " + result.getMessage());
                });
                break;
            }
            case 17: { // 停止网络测速
                if (dock == null) {
                    break;
                }
                NetworkTestModule netModule = dock.getNetworkTestModule();
                if (netModule == null) {
                    break;
                }
                netModule.stopNetworkTest(error -> {
                    System.out.println("stopNetworkTest result: " + error);
                });
                break;
            }
            case 18: { // 查询网络测速进度
                if (dock == null) {
                    break;
                }
                NetworkTestModule netModule = dock.getNetworkTestModule();
                if (netModule == null) {
                    break;
                }
                netModule.getNetworkTestProgress((error, result) -> {
                    System.out.println("getNetworkTestProgress result: " + error + ", " + result.getMessage());
                });
                break;
            }
            case 19: { // 机场网络RTK校准
                RtkCalibrationModule rtkModule = getRtkCalibrationModuleForDock("PLACEHOLDER_DOCK_SN");
                if (rtkModule == null) {
                    break;
                }
                com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams params = new com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams();
                params.setHost("xx.xx.xx.xx");
                params.setPort(8002);
                params.setAccount("PLACEHOLDER_RTK_ACCOUNT");
                params.setPassword("PLACEHOLDER_RTK_PASSWORD");
                params.setMountPoint("PLACEHOLDER_MOUNT_POINT");
                rtkModule.calibrationByNetwork(params, (error, result) -> {
                    System.out.println("calibrationByNetwork result: " + error + ", " + result.getMessage());
                });
                break;
            }
            case 20: { // 机场手动RTK校准
                RtkCalibrationModule rtkModule = getRtkCalibrationModuleForDock("PLACEHOLDER_DOCK_SN");
                if (rtkModule == null) {
                    break;
                }
                com.dji.flighthubsdk.bean.ManulRtkCalibrationParams params = new com.dji.flighthubsdk.bean.ManulRtkCalibrationParams();
                params.setLongitude(0.0);
                params.setLatitude(0.0);
                params.setHeight(100);
                rtkModule.calibrationByManual(params, (error, result) -> {
                    System.out.println("calibrationByManual result: " + error + ", " + result.getMessage());
                });
                break;
            }
            case 21: { // 中继网络RTK校准
                RtkCalibrationModule rtkModule = getRtkCalibrationModuleForRelay("PLACEHOLDER_DOCK_SN");
                if (rtkModule == null) {
                    break;
                }
                com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams params = new com.dji.flighthubsdk.bean.NetworkRtkCalibrationParams();
                params.setHost("xx.xx.xx.xx");
                params.setPort(8002);
                params.setAccount("PLACEHOLDER_RTK_ACCOUNT");
                params.setPassword("PLACEHOLDER_RTK_PASSWORD");
                params.setMountPoint("PLACEHOLDER_MOUNT_POINT");
                params.setVersion(3);
                rtkModule.calibrationByNetwork(params, (error, result) -> {
                    System.out.println("calibrationByNetwork result: " + error + ", " + result.getMessage());
                });
                break;
            }
            case 22: { // 中继手动RTK校准
                RtkCalibrationModule rtkModule = getRtkCalibrationModuleForRelay("PLACEHOLDER_DOCK_SN");
                if (rtkModule == null) {
                    break;
                }
                com.dji.flighthubsdk.bean.ManulRtkCalibrationParams params = new com.dji.flighthubsdk.bean.ManulRtkCalibrationParams();
                params.setLongitude(0.0);
                params.setLatitude(0.0);
                params.setHeight(100);
                rtkModule.calibrationByManual(params, (error, result) -> {
                    System.out.println("calibrationByManual result: " + error + ", " + result.getMessage());
                });
                break;
            }
            case 23: // 设置限距
                device_debug_module.setDistanceLimit("PLACEHOLDER_DOCK_SN", 0, 0, (error, result) -> {
                    System.out.println("setDistanceLimit result: " + error + ", " + result.getMessage());
                });
                break;
            case 24: // 设置限高
                device_debug_module.setHeightLimit("PLACEHOLDER_DOCK_SN", 487, (error, result) -> {
                    System.out.println("setHeightLimit result: " + error + ", " + result.getMessage());
                });
                break;
            case 25: // 设置避障
                device_debug_module.setObstacleAvoidance("PLACEHOLDER_DOCK_SN", 1, (error, result) -> {
                    System.out.println("setObstacleAvoidance result: " + error + ", " + result.getMessage());
                });
                break;
            case 26: // 设置返航保留电量
                device_debug_module.setRemainingPowerForRth("PLACEHOLDER_DOCK_SN", 20, (error, result) -> {
                    System.out.println("setRemainingPowerForRth result: " + error + ", " + result.getMessage());
                });
                break;
            case 27: // 设置夜航灯
                device_debug_module.setNightLightsState("PLACEHOLDER_DOCK_SN", 0, (error, result) -> {
                    System.out.println("setNightLightsState result: " + error + ", " + result.getMessage());
                });
                break;
            case 28: // 切换声光报警开关
                device_debug_module.switchAlarmLightState("PLACEHOLDER_DOCK_SN", 1, (error, result) -> {
                    System.out.println("switchAlarmLightState result: " + error + ", " + result.getMessage());
                });
                break;
            case 29: // eSIM运营商切换
                device_debug_module.esimOperatorSwitch("PLACEHOLDER_DOCK_SN", "PLACEHOLDER_IMEI", 2, "dock", (error, result) -> {
                    System.out.println("esimOperatorSwitch result: " + error + ", " + result.getMessage() + ", bid: " + result.getBid());
                });
                break;
            case 30: // SIM卡槽切换
                device_debug_module.simSlotSwitch("PLACEHOLDER_DOCK_SN", "PLACEHOLDER_IMEI", 2, "dock", (error, result) -> {
                    System.out.println("simSlotSwitch result: " + error + ", " + result.getMessage() + ", bid: " + result.getBid());
                });
                break;
            case 31: // 设置电池存储模式
                device_debug_module.setBatteryStoreMode("PLACEHOLDER_DOCK_SN", 1, (error, result) -> {
                    System.out.println("setBatteryStoreMode result: " + error + ", " + result.getMessage() + ", bid: " + result.getBid());
                });
                break;
            // ---- 中继站调试接口（IRelayControlModule）----
            case 32: { // 设置中继广播隐藏（false=显示广播）
                RelayControlModule relayControl = getRelayControlModule("YOUR_DEVICE_SN");
                if (relayControl != null) {
                    relayControl.setRelayBroadcastHide(false, error -> {
                        System.out.println("setRelayBroadcastHide error: " + error);
                    });
                }
                break;
            }
            case 33: { // 设置中继名称（SSID）
                RelayControlModule relayControl = getRelayControlModule("YOUR_DEVICE_SN");
                if (relayControl != null) {
                    relayControl.setRelaySSID("Relay_01", error -> {
                        System.out.println("setRelaySSID error: " + error);
                    });
                }
                break;
            }
            case 34: { // 设置中继密码
                RelayControlModule relayControl = getRelayControlModule("YOUR_DEVICE_SN");
                if (relayControl != null) {
                    relayControl.setRelayPassword("12345678", error -> {
                        System.out.println("setRelayPassword error: " + error);
                    });
                }
                break;
            }
            case 35: { // 设置图传搜索模式（1=开启）
                RelayControlModule relayControl = getRelayControlModule("YOUR_DEVICE_SN");
                if (relayControl != null) {
                    relayControl.setRelaySearchMode(1, error -> {
                        System.out.println("setRelaySearchMode error: " + error);
                    });
                }
                break;
            }
            case 36: { // 设置中继占用状态（1=允许抢占）
                RelayControlModule relayControl = getRelayControlModule("YOUR_DEVICE_SN");
                if (relayControl != null) {
                    relayControl.setRelayOccupiedStatus(1, (error, result) -> {
                        System.out.println("setRelayOccupiedStatus error: " + error + ", bid: " + result.getBid() + ", status: " + result.getStatus());
                    });
                }
                break;
            }
            case 37: { // 中继重启
                RelayControlModule relayControl = getRelayControlModule("YOUR_DEVICE_SN");
                if (relayControl != null) {
                    relayControl.relayReboot((error, result) -> {
                        System.out.println("relayReboot error: " + error + ", bid: " + result.getBid() + ", status: " + result.getStatus());
                    });
                }
                break;
            }
            case 38: { // 中继断开连接
                RelayControlModule relayControl = getRelayControlModule("YOUR_DEVICE_SN");
                if (relayControl != null) {
                    relayControl.relayDisconnect((error, result) -> {
                        System.out.println("relayDisconnect error: " + error + ", bid: " + result.getBid() + ", status: " + result.getStatus());
                    });
                }
                break;
            }
            case 39: { // 设置 Geo Caging 开关
                java.util.List<com.dji.flighthubsdk.bean.GeoCagingInfoParam> infos = new java.util.ArrayList<>();
                com.dji.flighthubsdk.bean.GeoCagingInfoParam info1 = new com.dji.flighthubsdk.bean.GeoCagingInfoParam();
                info1.setFgLongitude(0.0);
                info1.setFgLatitude(0.0);
                info1.setFgRadius(100);
                info1.setFgHeight(35);
                info1.setCvLongitude(0.0);
                info1.setCvLatitude(0.0);
                info1.setCvRadius(200);
                info1.setCvHeight(70);
                com.dji.flighthubsdk.bean.GeoCagingInfoParam info2 = new com.dji.flighthubsdk.bean.GeoCagingInfoParam();
                info2.setFgLongitude(0.0);
                info2.setFgLatitude(0.0);
                info2.setFgRadius(101);
                info2.setFgHeight(36);
                info2.setCvLongitude(0.0);
                info2.setCvLatitude(0.0);
                info2.setCvRadius(205);
                info2.setCvHeight(75);
                infos.add(info1);
                infos.add(info2);
                device_debug_module.setGeoCagingEnable("YOUR_DEVICE_SN", 0, infos, error -> {
                    System.out.println("setGeoCagingEnable error: " + error);
                });
                break;
            }
            case 40: // 查询飞机状态
                java.util.List<String> drone_params = java.util.Collections.singletonList(getSubDeviceSn(selectedDevice));
                workspace.getDeviceManageModule().getDroneDeviceStatus(drone_params, new FlightHubCallback.DroneDeviceStatusListCallback() {
                    @Override
                    public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.DroneStatusItemResult> items) {
                        int n = items != null ? items.size() : 0;
                        System.out.println("getDroneDeviceStatus result: " + error + ", size = " + n);
                    }
                });
                break;
            case 41: // 查询机场状态
                java.util.List<String> dock_params = java.util.Collections.singletonList(getMainDeviceSn(selectedDevice));
                workspace.getDeviceManageModule().getDockDeviceStatus(dock_params, new FlightHubCallback.DockDeviceStatusListCallback() {
                    @Override
                    public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.DockStatusItemResult> items) {
                        int n = items != null ? items.size() : 0;
                        System.out.println("getDockDeviceStatus result: " + error + ", size = " + n);
                        if (items == null) {
                            return;
                        }
                        for (com.dji.flighthubsdk.bean.DockStatusItemResult it : items) {
                            System.out.println("getDockDeviceStatus result: " + error + ", sn = " + it.getSn());
                            System.out.println("getDockDeviceStatus result: " + error + ", online = " + it.isOnline());
                            System.out.println("getDockDeviceStatus result: " + error + ", updatedTime = " + it.getUpdatedTime());
                            com.dji.flighthubsdk.bean.DeviceStatusModel deviceModel = it.getDeviceModel();
                            if (deviceModel != null) {
                                System.out.println("getDockDeviceStatus result: " + error + ", deviceClass = " + deviceModel.getDeviceClass());
                                System.out.println("getDockDeviceStatus result: " + error + ", key = " + deviceModel.getKey());
                                System.out.println("getDockDeviceStatus result: " + error + ", name = " + deviceModel.getName());
                            }
                        }
                    }
                 });
                 break;
             case 42: { // 发起手动换电
                 System.out.print(" slot_id: ");
                 int slotId = Integer.parseInt(scanner.next());
                 device_debug_module.startManualBatterySwap(getMainDeviceSn(selectedDevice), slotId,
                     new FlightHubCallback.ManualBatterySwapCallback() {
                         @Override
                         public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ManualBatterySwapResult result) {
                             System.out.println("startManualBatterySwap result: " + error + ", bid=" + result.getBid() + ", message=" + result.getMessage());
                         }
                     });
                 break;
             }
             case 43: // 查询手动换电进度
                 device_debug_module.getManualBatterySwapProgress(getMainDeviceSn(selectedDevice),
                     new FlightHubCallback.ManualBatterySwapProgressCallback() {
                         @Override
                         public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ManualBatterySwapProgressResult result) {
                             System.out.println("getManualBatterySwapProgress result: " + error + ", bid=" + result.getBid()
                                     + ", message=" + result.getMessage() + ", data=" + result.getDataJson());
                         }
                     });
                 break;
              case 44: { // Dongle 电源开关（需用无人机 SN）
                  System.out.print(" power_state (0=off / 1=on): ");
                  int powerState = Integer.parseInt(scanner.next());
                  String dongleSn = getSubDeviceSn(selectedDevice);
                  System.out.println(" dongle sn: " + dongleSn);
                  device_debug_module.setDonglePowerSwitch(dongleSn, powerState,
                     new FlightHubCallback.DonglePowerSwitchCallback() {
                         @Override
                         public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DonglePowerSwitchResult result) {
                             System.out.println("setDonglePowerSwitch result: " + error + ", bid=" + result.getBid() + ", message=" + result.getMessage());
                         }
                     });
                 break;
             }
             case 45: { // 机场低功耗开关
                 System.out.print(" low_power_mode (0=off / 1=on): ");
                 int mode = Integer.parseInt(scanner.next());
                 device_debug_module.setLowPowerSwitch(getMainDeviceSn(selectedDevice), mode,
                     new FlightHubCallback.LowPowerSwitchCallback() {
                         @Override
                         public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LowPowerSwitchResult result) {
                             System.out.println("setLowPowerSwitch result: " + error + ", bid=" + result.getBid() + ", message=" + result.getMessage());
                         }
                     });
                 break;
             }
             case 46: // 查询 eSIM 运营商切换进度
                 device_debug_module.getEsimOperatorSwitchProgress(getMainDeviceSn(selectedDevice),
                     new FlightHubCallback.EsimOperatorSwitchProgressCallback() {
                         @Override
                         public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.EsimOperatorSwitchProgressResult result) {
                             System.out.println("getEsimOperatorSwitchProgress result: " + error + ", bid=" + result.getBid()
                                     + ", message=" + result.getMessage() + ", data=" + result.getDataJson());
                         }
                     });
                 break;
              default:
                  System.out.println("无效输入");
          }
         waitForKeyPress();
     }

    private static void handleWorkspace() {
        log("INFO", "handleWorkspace", "组织测试");
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleWorkspace", "获取workspace失败");
            return;
        }
        java.util.List<String> sns = java.util.Collections.singletonList("YOUR_DEVICE_SN");
        workspace.getDeviceManageModule().unbindDevice(sns, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleWorkspace", "unbindDevice error: " + error);
            }
        });
        waitForKeyPress();
    }

    private static void handleLogTest() {
        log("INFO", "handleLogTest", "日志测试");
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleLogTest", "获取workspace失败");
            return;
        }
        com.dji.flighthubsdk.core.LogManagementModule logModule = workspace.getLogManagementModule();
        // 223 环境可用机场 SN（与司空前端一致）；勿用写死无效 SN，否则 MQTT fileupload_list 会卡住无回包
        final String logTestDeviceSn = "PLACEHOLDER_LOG_TEST_SN";
        System.out.println(" 拉取设备日志列表 [1]");
        System.out.println(" 拉取日志报告列表 [2]");
        System.out.println(" 读取日志操作 [3]");
        System.out.println(" 删除日志 [4]");
        System.out.println(" 取消上传日志 [5]");
        System.out.println(" 上传日志附件 [6]");
        System.out.println(" 创建异常反馈 [7]");
        System.out.print(" 请输入选择: ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1: {
                // 0=飞机 3=机场 6=中继 9=机库（对齐司空前端 module_list）
                java.util.List<Integer> moduleList = java.util.Arrays.asList(0, 3, 6, 9);
                log("INFO", "handleLogTest", "queryDeviceLogList sn=" + logTestDeviceSn + ", module_list=" + moduleList);
                logModule.queryDeviceLogList(logTestDeviceSn, moduleList, new FlightHubCallback.QueryDeviceLogListCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DeviceLogListResult result) {
                        int n = result != null && result.getFiles() != null ? result.getFiles().size() : 0;
                        log("INFO", "handleLogTest", "queryDeviceLogList files size: " + n + ", error: " + error);
                        if (result != null && result.getFiles() != null) {
                            for (com.dji.flighthubsdk.bean.DeviceLogListResultFileItem file : result.getFiles()) {
                                int inner = file.getList() != null ? file.getList().size() : 0;
                                log("INFO", "handleLogTest",
                                        "  file sn=" + file.getDeviceSn()
                                                + ", module=" + file.getModule()
                                                + ", result=" + file.getResult()
                                                + ", logs=" + inner);
                            }
                        }
                    }
                });
                break;
            }
            case 2: {
                logModule.logReportList(logTestDeviceSn, 1, 20, new FlightHubCallback.LogReportListCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.LogReportListResult result) {
                        int sz = result != null && result.getList() != null ? result.getList().size() : 0;
                        log("INFO", "handleLogTest", "logReportList list size: " + sz + ", error: " + error);
                        if (result != null && result.getList() != null) {
                            for (int i = 0; i < result.getList().size(); i++) {
                                log("INFO", "handleLogTest", "  report_id: " + result.getList().get(i).getReportUuid());
                            }
                        }
                    }
                });
                break;
            }
            case 3: {
                logModule.logReportOperation("read", "0b381d37-541e-4577-8e26-a124d639e0bd", new FlightHubCallback.LogReportOperationCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleLogTest", "logReportOperation read error: " + error);
                    }
                });
                break;
            }
            case 4: {
                logModule.logReportOperation("delete", "0b381d37-541e-4577-8e26-a124d639e0bd", new FlightHubCallback.LogReportOperationCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleLogTest", "logReportOperation delete error: " + error);
                    }
                });
                break;
            }
            case 5: {
                logModule.logReportOperation("cancel", "0b381d37-541e-4577-8e26-a124d639e0bd", new FlightHubCallback.LogReportOperationCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleLogTest", "logReportOperation cancel error: " + error);
                    }
                });
                break;
            }
            case 6: {
                logModule.uploadLogAppendFile("/path/to/log/file.zip", new FlightHubCallback.UploadLogAppendFileCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleLogTest", "uploadLogAppendFile error: " + error);
                    }
                });
                break;
            }
            case 7: {
                // 创建异常反馈（CreateDeviceLogReport）
                com.dji.flighthubsdk.bean.CreateDeviceLogReportRequestParam param =
                        new com.dji.flighthubsdk.bean.CreateDeviceLogReportRequestParam();

                // 故障描述（JSON格式）
                param.setReportInformation("{\"text\":\"ed\",\"error_codes\":[\"1\"]}");

                // 故障发生时间（秒级时间戳）
                param.setErrorStartTime(1770195274L);

                // 联系信息
                com.dji.flighthubsdk.bean.DeviceLogReportContactInformationParam contact =
                        new com.dji.flighthubsdk.bean.DeviceLogReportContactInformationParam();
                contact.setEmail("");
                contact.setPhone("11");
                param.setReportContactInformation(contact);

                // 设备拓扑 — 机场（parent）；SN 与 queryDeviceLogList 测试一致
                com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam dock =
                        new com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam();
                dock.setSn(logTestDeviceSn);
                dock.setDeviceModelKey("3-3-0");

                // 设备拓扑 — 无人机（host）；需与机场当前子机一致，boot 信息请按 [1] 回包改
                com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam drone =
                        new com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoItemParam();
                drone.setSn("PLACEHOLDER_DRONE_SN");
                drone.setDeviceModelKey("0-100-1");

                com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoParam topo =
                        new com.dji.flighthubsdk.bean.DeviceLogReportDeviceTopoParam();
                topo.setParents(java.util.Arrays.asList(dock));
                topo.setHosts(java.util.Arrays.asList(drone));
                param.setDeviceTopo(topo);

                // 机场日志列表
                com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam dockLogItem =
                        new com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam();
                dockLogItem.setBootIndex(20257);
                dockLogItem.setStartTime(1770194580L);
                dockLogItem.setEndTime(1770195298L);
                dockLogItem.setSize(2378476398L);
                dockLogItem.setMd5("");

                com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam dockLogListItem =
                        new com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam();
                dockLogListItem.setDeviceSn(logTestDeviceSn);
                dockLogListItem.setModule("3");
                dockLogListItem.setList(java.util.Arrays.asList(dockLogItem));

                // 无人机日志列表
                com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam droneLogItem =
                        new com.dji.flighthubsdk.bean.DeviceLogReportLogItemParam();
                droneLogItem.setBootIndex(982);
                droneLogItem.setStartTime(1770195106L);
                droneLogItem.setEndTime(1770195297L);
                droneLogItem.setSize(617572353L);
                droneLogItem.setMd5("");

                com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam droneLogListItem =
                        new com.dji.flighthubsdk.bean.DeviceLogReportLogListItemParam();
                droneLogListItem.setDeviceSn("PLACEHOLDER_DRONE_SN");
                droneLogListItem.setModule("0");
                droneLogListItem.setList(java.util.Arrays.asList(droneLogItem));

                param.setLogList(java.util.Arrays.asList(dockLogListItem, droneLogListItem));

                // domains
                com.dji.flighthubsdk.bean.DeviceLogReportDomainItemParam domain =
                        new com.dji.flighthubsdk.bean.DeviceLogReportDomainItemParam();
                domain.setKey(1770194965412L);
                domain.setValue("1");
                param.setDomains(java.util.Arrays.asList(domain));

                // appendix_files 为空
                param.setAppendixFiles(new java.util.ArrayList<>());

                // 是否崩溃
                param.setCrashed(false);

                logModule.createDeviceLogReport(param, new FlightHubCallback.CreateDeviceLogReportCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CreateDeviceLogReportResult result) {
                        if (error.isSuccess()) {
                            log("INFO", "handleLogTest", "createDeviceLogReport 成功!");
                            log("INFO", "handleLogTest", "  track_number: " + result.getTrackNumber());
                            log("INFO", "handleLogTest", "  report_uuid:  " + result.getReportUuid());
                            if (result.getErrorLogList() != null && !result.getErrorLogList().isEmpty()) {
                                log("INFO", "handleLogTest", "  error_log_list: " + result.getErrorLogList());
                            }
                            if (result.getCreateDeviceJobData() != null) {
                                log("INFO", "handleLogTest", "  job bid:    " + result.getCreateDeviceJobData().getBid());
                                log("INFO", "handleLogTest", "  job sn:     " + result.getCreateDeviceJobData().getSn());
                                log("INFO", "handleLogTest", "  job status: " + result.getCreateDeviceJobData().getStatus());
                                log("INFO", "handleLogTest", "  job result: " + result.getCreateDeviceJobData().getResult());
                            }
                        } else {
                            log("ERROR", "handleLogTest", "createDeviceLogReport 失败, error: " + error);
                        }
                    }
                });
                break;
            }
            default:
                System.out.println("无效输入");
        }
        waitForKeyPress();
    }

    private static void handleFirmwareManagementTest() {
        log("INFO", "handleFirmwareManagementTest", "固件管理测试");
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleFirmwareManagementTest", "获取workspace失败");
            return;
        }
        System.out.println(" 获取最新固件RN [1]");
        System.out.println(" 固件升级 [2]");
        System.out.println(" 查询固件升级进度 [3]");
        System.out.println(" 上传固件 [4]");
        System.out.println(" 发布固件 [5]");
        System.out.println(" 下架固件 [6]");
        System.out.println(" 删除固件 [7]");
        System.out.println(" 查询固件列表 [8]");
        System.out.println(" 查询固件兼容性情况 [9]");
        System.out.println(" 检查固件状态 [10]");
        System.out.println(" 提交固件信息 [11]");
        System.out.print(" 请输入选择: ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1: {
                java.util.List<String> deviceModelKeys = java.util.Arrays.asList("3-3-0", "8-135-0");
                workspace.getFirmwareManageModule().getLatestFirmwareReleaseNote(deviceModelKeys, "zh_cn", new FlightHubCallback.GetLatestFirmwareReleaseNoteCallback() {
                    @Override
                    public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.FirmwareReleaseNoteResult> notes) {
                        int n = notes != null ? notes.size() : 0;
                        log("INFO", "handleFirmwareManagementTest", "getLatestFirmwareReleaseNote result size: " + n + ", error: " + error);
                    }
                });
                break;
            }
            case 2: {
                com.dji.flighthubsdk.bean.FirmwareUpgradeParam upgradeParam = new com.dji.flighthubsdk.bean.FirmwareUpgradeParam();
                workspace.getFirmwareManageModule().invokeFirmwareUpgrade(upgradeParam, new FlightHubCallback.InvokeFirmwareUpgradeCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FirmwareUpgradeResult result) {
                        log("INFO", "handleFirmwareManagementTest", "invokeFirmwareUpgrade result: " + result.getDeviceCode() + ", error: " + error);
                    }
                });
                break;
            }
            case 3: {
                workspace.getFirmwareManageModule().queryFirmwareUpgradeProgress("PLACEHOLDER_DOCK_SN", new FlightHubCallback.QueryFirmwareUpgradeProgressCallback() {
                    @Override
                    public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.FirmwareUpgradeProgressItem> items) {
                        int n = items != null ? items.size() : 0;
                        log("INFO", "handleFirmwareManagementTest", "queryFirmwareUpgradeProgress result size: " + n + ", error: " + error);
                    }
                });
                break;
            }
            case 4: {
                workspace.getFirmwareManageModule().uploadFirmwareFile("/path/to/firmware.zip", new FlightHubCallback.UploadFirmwareFileCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.UploadFileResult result) {
                        log("INFO", "handleFirmwareManagementTest", "uploadFirmwareFile error: " + error + ", result: " + result);
                    }
                });
                break;
            }
            case 5: {
                workspace.getFirmwareManageModule().releaseFirmware("file_id", new FlightHubCallback.ReleaseFirmwareCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleFirmwareManagementTest", "releaseFirmware error: " + error);
                    }
                });
                break;
            }
            case 6: {
                workspace.getFirmwareManageModule().unReleaseFirmware("file_id", new FlightHubCallback.UnReleaseFirmwareCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleFirmwareManagementTest", "unReleaseFirmware error: " + error);
                    }
                });
                break;
            }
            case 7: {
                workspace.getFirmwareManageModule().deleteFirmware("file_id", new FlightHubCallback.DeleteFirmwareCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleFirmwareManagementTest", "deleteFirmware error: " + error);
                    }
                });
                break;
            }
            case 8: {
                workspace.getFirmwareManageModule().queryFirmwareList(1, 20, new FlightHubCallback.QueryFirmwareListCallback() {
                    @Override
                    public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.FirmwareItemResult> items) {
                        int n = items != null ? items.size() : 0;
                        log("INFO", "handleFirmwareManagementTest", "queryFirmwareList result size: " + n + ", error: " + error);
                    }
                });
                break;
            }
            case 9: {
                workspace.getFirmwareManageModule().queryFirmwareCompatibilityConfig(new FlightHubCallback.QueryFirmwareCompatibilityConfigCallback() {
                    @Override
                    public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.FirmwareCompatibilityConfigItemResult> items) {
                        int n = items != null ? items.size() : 0;
                        log("INFO", "handleFirmwareManagementTest", "queryFirmwareCompatibilityConfig data size: " + n + ", error: " + error);
                    }
                });
                break;
            }
            case 10: {
                java.util.List<String> deviceSns = java.util.Collections.singletonList("PLACEHOLDER_DOCK_SN");
                java.util.List<String> status = java.util.Collections.singletonList("in_progress");
                workspace.getFirmwareManageModule().checkFirmwareStatus(deviceSns, status, new FlightHubCallback.CheckFirmwareStatusCallback() {
                    @Override
                    public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.FirmwareStatusItem> items) {
                        int n = items != null ? items.size() : 0;
                        log("INFO", "handleFirmwareManagementTest", "checkFirmwareStatus result size: " + n + ", error: " + error);
                    }
                });
                break;
            }
            case 11: {
                com.dji.flighthubsdk.bean.FirmwareInfoParam firmwareInfoParam = new com.dji.flighthubsdk.bean.FirmwareInfoParam();
                firmwareInfoParam.setDeviceModelKey("3-3-0");
                firmwareInfoParam.setVersion("v01.00.0000");
                firmwareInfoParam.setDesc("demo firmware info");
                firmwareInfoParam.setObjectKey("firmware/demo/path.zip");
                firmwareInfoParam.setMd5("dummy-md5");
                workspace.getFirmwareManageModule().submitFirmwareInfo(firmwareInfoParam, new FlightHubCallback.SubmitFirmwareInfoCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleFirmwareManagementTest", "submitFirmwareInfo error: " + error);
                    }
                });
                break;
            }
            default:
                System.out.println("无效输入");
        }
        waitForKeyPress();
    }

    private static void handleFlySafeTest() {
        log("INFO", "handleFlySafeTest", "限飞测试");
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleFlySafeTest", "获取workspace失败");
            return;
        }
        com.dji.flighthubsdk.core.SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleFlySafeTest", "获取safetyServiceModule失败");
            return;
        }
        System.out.println(" 获取解锁证书列表 [3]");
        System.out.println(" 切换解锁证书状态 [4]");
        System.out.println(" 检查解锁证书 [5]");
        System.out.println(" 提交解锁证书 [6]");
        System.out.println(" 清除设备解锁证书 [7]");
        System.out.println(" 上传解锁证书文件 [8]");
        System.out.print(" 请输入选择: ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 3: {
                com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListParam queryParam = new com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListParam();
                queryParam.setDeviceSn("PLACEHOLDER_DOCK_SN");
                queryParam.setDeviceModelDomain(0);
                queryParam.setLicenseType("");
                queryParam.setLicenseId("");
                queryParam.setDetail(true);
                log("INFO", "handleFlySafeTest", "getUnlockLicenseList: device_sn=" + queryParam.getDeviceSn()
                        + ", license_type=" + queryParam.getLicenseType()
                        + ", license_id=" + queryParam.getLicenseId()
                        + ", detail=" + queryParam.isDetail());
                safetyServiceModule.getUnlockLicenseList(queryParam, new FlightHubCallback.GetUnlockLicenseListCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListResult result) {
                        log("INFO", "handleFlySafeTest", "getUnlockLicenseList result: " + result.getResult() + ", error: " + error);
                    }
                });
                break;
            }
            case 4: {
                com.dji.flighthubsdk.bean.SwitchUnlockLicenseParam switchParam = new com.dji.flighthubsdk.bean.SwitchUnlockLicenseParam();
                switchParam.setDeviceSn("PLACEHOLDER_DOCK_SN");
                switchParam.setLicenseId(1);
                switchParam.setEnable(false);
                safetyServiceModule.switchUnlockLicense(switchParam, new FlightHubCallback.SwitchUnlockLicenseCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleFlySafeTest", "switchUnlockLicense error: " + error);
                    }
                });
                break;
            }
            case 5: {
                com.dji.flighthubsdk.bean.CheckUnlockLicenseParam checkParam = new com.dji.flighthubsdk.bean.CheckUnlockLicenseParam();
                checkParam.setDeviceSn("PLACEHOLDER_DOCK_SN");
                checkParam.setFileKey("file_key");
                safetyServiceModule.checkUnlockLicense(checkParam, new FlightHubCallback.CheckUnlockLicenseCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CheckUnlockLicenseResult result) {
                        log("INFO", "handleFlySafeTest", "checkUnlockLicense error: " + error);
                    }
                });
                break;
            }
            case 6: {
                com.dji.flighthubsdk.bean.ImportUnlockLicenseParam importParam = new com.dji.flighthubsdk.bean.ImportUnlockLicenseParam();
                importParam.setDeviceSn("PLACEHOLDER_DOCK_SN");
                importParam.setCertUrl("http://example.com/cert");
                safetyServiceModule.submitUnlockLicense(importParam, new FlightHubCallback.SubmitUnlockLicenseCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleFlySafeTest", "submitUnlockLicense error: " + error);
                    }
                });
                break;
            }
            case 7: {
                safetyServiceModule.clearDeviceUnlockLicense("PLACEHOLDER_DOCK_SN", new FlightHubCallback.ClearDeviceUnlockLicenseCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        log("INFO", "handleFlySafeTest", "clearDeviceUnlockLicense error: " + error);
                    }
                });
                break;
            }
            case 8: {
                safetyServiceModule.uploadUnlockLicenseFile("/path/to/license.zip", new FlightHubCallback.UploadUnlockLicenseFileCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.UploadUnlockLicenseFileResult result) {
                        log("INFO", "handleFlySafeTest", "uploadUnlockLicenseFile error: " + error);
                    }
                });
                break;
            }
            default:
                System.out.println("无效输入");
        }
        waitForKeyPress();
    }

    private static void handleWaylineTest() {
        log("INFO", "handleWaylineTest", "航线管理测试");
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleWaylineTest", "获取workspace失败");
            return;
        }
         System.out.println(" 获取航线列表 [1]");
         System.out.println(" 获取航线详情 [2]");
         System.out.println(" 批量删除航线 [3]");
         System.out.println(" 更新航线名称 [4]");
         System.out.println(" 提交航线文件 [5]");
         System.out.println(" 批量下载航线 [6]");
         System.out.println(" 创建副本航线 [7]");
         System.out.print(" 请输入选择: ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1: {
                com.dji.flighthubsdk.bean.GetWaylineListParam waylineListParam = new com.dji.flighthubsdk.bean.GetWaylineListParam();
                waylineListParam.setPage(0);
                waylineListParam.setPageSize(20);
                waylineListParam.setStartTimestamp(0);
                waylineListParam.setEndTimestamp(System.currentTimeMillis());
                workspace.getWaylineManageModule().getWaylineList(waylineListParam, new FlightHubCallback.GetWaylineListCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetWaylineListResult result) {
                        com.dji.flighthubsdk.bean.WaylineListPaginationResult p = result != null ? result.getPagination() : null;
                        log("INFO", "handleWaylineTest", "getWaylineList error: " + error
                                + ", total: " + (p != null ? p.getTotal() : 0)
                                + ", page: " + (p != null ? p.getPage() : 0)
                                + ", page_size: " + (p != null ? p.getPageSize() : 0));
                        java.util.List<com.dji.flighthubsdk.bean.WaylineListItemResult> list =
                                result != null ? result.getItems() : null;
                        if (list != null) {
                            for (int i = 0; i < list.size(); i++) {
                                com.dji.flighthubsdk.bean.WaylineListItemResult item = list.get(i);
                                log("INFO", "handleWaylineTest", "  [" + i + "] wayline_uuid: " + item.getWaylineUuid()
                                        + ", wayline_name: " + item.getWaylineName()
                                        + ", drone_type: " + item.getDroneType()
                                        + ", size: " + item.getSize()
                                        + ", updated_at: " + item.getUpdatedAt()
                                        + ", with_megaphone: " + item.isWithMegaphone()
                                        + ", with_search_light: " + item.isWithSearchLight()
                                        + ", is_ai_wayline: " + item.isAiWayline());
                            }
                        }
                    }
                });
                break;
            }
            case 2: {
                 workspace.getWaylineManageModule().getWaylineDetail("0fee6feb-ea9f-46d0-b482-291c0a2246e9", new FlightHubCallback.GetWaylineDetailCallback() {
                     @Override
                     public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetWaylineDetailResult result) {
                         com.dji.flighthubsdk.bean.WaylineDetailKmzData kmz =
                                 result != null ? result.getKmzData() : null;
                         log("INFO", "handleWaylineTest", "getWaylineDetail error: " + error
                                 + ", wayline_uuid: " + (result != null ? result.getWaylineUuid() : "")
                                 + ", wayline_name: " + (result != null ? result.getWaylineName() : "")
                                 + ", url: " + (result != null ? result.getUrl() : "")
                                 + ", editor_warnings: " + (result != null ? result.getEditorWarnings() : "{}")
                                 + ", execute_warnings: " + (result != null ? result.getExecuteWarnings() : "{}")
                                 + ", duration: " + (kmz != null ? kmz.getDuration() : 0)
                                 + ", distance: " + (kmz != null ? kmz.getDistance() : 0));
                     }
                 });
                 break;
             }
             case 3: {
                java.util.List<String> uuids = java.util.Collections.singletonList("0fee6feb-ea9f-46d0-b482-291c0a2246e9");
                 workspace.getWaylineManageModule().batchDeleteWaylines(uuids, new FlightHubCallback.BatchDeleteWaylinesCallback() {
                     @Override
                     public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.BatchDeleteWaylinesResult result) {
                        log("INFO", "handleWaylineTest", "batchDeleteWaylines error: " + error);
                     }
                 });
                 break;
             }
             case 4: {
                 workspace.getWaylineManageModule().updateWayline("0fee6feb-ea9f-46d0-b482-291c0a2246e9", "new_wayline_name", new FlightHubCallback.UpdateWaylineCallback() {
                     @Override
                     public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.UpdateWaylineResult result) {
                         log("INFO", "handleWaylineTest", "updateWayline error: " + error + ", status: " + (result != null ? result.getStatus() : "null"));
                     }
                 });
                 break;
             }
             case 5: {
                 com.dji.flighthubsdk.bean.SubmitWaylineInfoParam submitParam = new com.dji.flighthubsdk.bean.SubmitWaylineInfoParam();
                 submitParam.setKmzObjectKey("5a6f9d4b-2a38-4b4b-86f9-3a678da0bf4a/935446f0-146b-4f6c-bb2b-61f6d42c4588/9185e5ab-d948-43ee-8855-e7eec17fcfa8/新建航点航线(25).kmz");
                 submitParam.setWaylineName("paas_import_test_0209");
                 submitParam.setAsyncRequestId("24a97e9e-c03e-4d92-b24a-a53a65add777");
                 submitParam.setTimestamp(System.currentTimeMillis());
                 java.util.Map<String, String> transparent = new java.util.HashMap<>();
                 transparent.put("test_key", "test_value");
                 submitParam.setTransparent(transparent);
                 log("INFO", "handleWaylineTest", "submitWaylineInfo transparent.size=" + submitParam.getTransparent().size());
                 workspace.getWaylineManageModule().submitWaylineInfo(submitParam, new FlightHubCallback.SubmitWaylineInfoCallback() {
                     @Override
                     public void onResult(FlightHubError error) {
                         log("INFO", "handleWaylineTest", "submitWaylineInfo error: " + error);
                     }
                 });
                 break;
             }
             case 6: {
                 com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam downloadParam = new com.dji.flighthubsdk.bean.BatchDownloadWaylinesParam();
                 downloadParam.setWaylineUuids(java.util.Collections.singletonList("0fee6feb-ea9f-46d0-b482-291c0a2246e9"));
                 workspace.getWaylineManageModule().batchDownloadWaylines(downloadParam, new FlightHubCallback.BatchDownloadWaylinesCallback() {
                     @Override
                     public void onResult(FlightHubError error, java.util.List<com.dji.flighthubsdk.bean.BatchDownloadWaylinesItemResult> items) {
                         log("INFO", "handleWaylineTest", "batchDownloadWaylines error: " + error);
                         if (items != null) {
                             for (int i = 0; i < items.size(); i++) {
                                 com.dji.flighthubsdk.bean.BatchDownloadWaylinesItemResult item = items.get(i);
                                 log("INFO", "handleWaylineTest", "  [" + i + "] wayline_uuid: " + item.getWaylineUuid()
                                         + ", wayline_name: " + item.getWaylineName()
                                         + ", url: " + item.getUrl()
                                         + ", size: " + item.getSize());
                             }
                         }
                     }
                 });
                 break;
             }
             case 7: {
                 com.dji.flighthubsdk.bean.DuplicateWaylineParam duplicateParam = new com.dji.flighthubsdk.bean.DuplicateWaylineParam();
                 duplicateParam.setOriginalWaylineUuid("aaf178c4-05fd-4c24-b6e4-89d6db9c1579");
                 duplicateParam.setTargetWaylineName("duplicate_test");
                 duplicateParam.setTargetObjectKeyPrefix("5a6f9d4b-2a38-4b4b-86f9-3a678da0bf4a/935446f0-146b-4f6c-bb2b-61f6d42c4588/9c799087-e830-4a87-b976-4f16ebce20c5");
                 workspace.getWaylineManageModule().duplicateWayline(duplicateParam, new FlightHubCallback.DuplicateWaylineCallback() {
                     @Override
                     public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.DuplicateWaylineResult result) {
                         log("INFO", "handleWaylineTest", "duplicateWayline error: " + error);
                         if (result != null) {
                             log("INFO", "handleWaylineTest", "  wayline_uuid: " + result.getWaylineUuid()
                                     + ", wayline_name: " + result.getWaylineName());
                         }
                     }
                 });
                 break;
             }
             case 8: {
                 // 航线复制（去组织化）
                 com.dji.flighthubsdk.bean.CopyWaylineParam copyParam = new com.dji.flighthubsdk.bean.CopyWaylineParam();
                 copyParam.setOriginalWaylineUuid("4bb01fa5-00c5-472c-a672-d329dcc9b836");
                 copyParam.setTargetWaylineName("测试duplicate");
                 log("INFO", "handleWaylineTest", "copyWayline param: " + copyParam);
                 workspace.getWaylineManageModule().copyWayline(copyParam, new FlightHubCallback.CopyWaylineCallback() {
                     @Override
                     public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CopyWaylineResult result) {
                         log("INFO", "handleWaylineTest", "copyWayline error: " + error);
                         if (result != null) {
                             log("INFO", "handleWaylineTest", "  wayline_uuid: " + result.getWaylineUuid()
                                     + ", wayline_name: " + result.getWaylineName());
                         }
                     }
                 });
                 break;
             }
             case 9: {
                 // 航线拼接（去组织化）
                 com.dji.flighthubsdk.bean.ConcatWaylineParam concatParam = new com.dji.flighthubsdk.bean.ConcatWaylineParam();
                 concatParam.setTargetWaylineName("测试duplicate+223-惠州航点航线-M3TD");
                 concatParam.setKmzObjectKey("pass_tests/测试duplicate.kmz");
                 java.util.List<String> subUuids = new java.util.ArrayList<>();
                 subUuids.add("4bb01fa5-00c5-472c-a672-d329dcc9b836");
                 subUuids.add("6e180f35-0280-40ab-80ab-d681348a65f9");
                 concatParam.setSubWaylineUuids(subUuids);
                 log("INFO", "handleWaylineTest", "concatWayline param: " + concatParam);
                 workspace.getWaylineManageModule().concatWayline(concatParam, new FlightHubCallback.ConcatWaylineCallback() {
                     @Override
                     public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ConcatWaylineResult result) {
                         log("INFO", "handleWaylineTest", "concatWayline error: " + error);
                         if (result != null) {
                             log("INFO", "handleWaylineTest", "  wayline_uuid: " + result.getWaylineUuid());
                             // 打印 kmzData
                             com.dji.flighthubsdk.bean.WaylineDetailKmzData kmzData = result.getKmzData();
                             if (kmzData != null) {
                                 log("INFO", "handleWaylineTest", "  === kmzData ===");
                                 log("INFO", "handleWaylineTest", "    wayline_type: " + kmzData.getWaylineType());
                                 log("INFO", "handleWaylineTest", "    wayline_point_count: " + kmzData.getWaylinePointCount());
                                 log("INFO", "handleWaylineTest", "    duration: " + kmzData.getDuration());
                                 log("INFO", "handleWaylineTest", "    distance: " + kmzData.getDistance());
                                 log("INFO", "handleWaylineTest", "    survey_area: " + kmzData.getSurveyArea());
                                 log("INFO", "handleWaylineTest", "    start_latitude: " + kmzData.getStartLatitude());
                                 log("INFO", "handleWaylineTest", "    start_longitude: " + kmzData.getStartLongitude());
                                 log("INFO", "handleWaylineTest", "    start_altitude: " + kmzData.getStartAltitude());
                                 log("INFO", "handleWaylineTest", "    drone_type: " + kmzData.getDroneType());
                                 log("INFO", "handleWaylineTest", "    is_ai_wayline: " + kmzData.isAiWayline());
                                 log("INFO", "handleWaylineTest", "    is_with_megaphone: " + kmzData.isWithMegaphone());
                                 log("INFO", "handleWaylineTest", "    is_with_search_light: " + kmzData.isWithSearchLight());
                                 log("INFO", "handleWaylineTest", "    is_surface_fly_on: " + kmzData.isSurfaceFlyOn());
                                 log("INFO", "handleWaylineTest", "    estimated_photo_count: " + kmzData.getEstimatedPhotoCount());
                                 log("INFO", "handleWaylineTest", "    payload_types: " + kmzData.getPayloadTypes());
                             }
                             // 打印 extData
                             com.dji.flighthubsdk.bean.WaylineDetailExtData extData = result.getExtData();
                             if (extData != null) {
                                 log("INFO", "handleWaylineTest", "  === extData ===");
                                 log("INFO", "handleWaylineTest", "    kmz_without_res_object_key: " + extData.getKmzWithoutResObjectKey());
                                 log("INFO", "handleWaylineTest", "    kmz_without_res_md5: " + extData.getKmzWithoutResMd5());
                                 log("INFO", "handleWaylineTest", "    target_area_file_object_key: " + extData.getTargetAreaFileObjectKey());
                                 log("INFO", "handleWaylineTest", "    business_file_object_key: " + extData.getBusinessFileObjectKey());
                                 log("INFO", "handleWaylineTest", "    model_file_object_keys: " + extData.getModelFileObjectKeys());
                                 log("INFO", "handleWaylineTest", "    elevation_file_object_keys: " + extData.getElevationFileObjectKeys());
                                 log("INFO", "handleWaylineTest", "    content_length: " + extData.getContentLength());
                             }
                         }
                     }
                 });
                 break;
             }
             case 10: {
                 // 上传 KMZ 文件并导入航线（走 C++ multipart 通道）
                 java.util.List<String> kmzFiles = new java.util.ArrayList<>();
                 kmzFiles.add("/path/to/test.kmz");  // 换成本机实际 .kmz 路径
                 log("INFO", "handleWaylineTest", "importKmz files: " + kmzFiles);
                 workspace.getWaylineManageModule().importKmz(kmzFiles, new FlightHubCallback.ImportKmzCallback() {
                     @Override
                     public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.ImportKmzResult result) {
                         log("INFO", "handleWaylineTest", "importKmz error: " + error);
                         if (result != null) {
                             log("INFO", "handleWaylineTest", "  total: " + result.getTotal());
                             for (com.dji.flighthubsdk.bean.ImportKmzItem item : result.getList()) {
                                 log("INFO", "handleWaylineTest", "  item: " + item);
                             }
                         }
                     }
                 });
                 break;
             }
              default:
                  System.out.println("无效输入");
              }
         }


    /**
     * [媒体服务模块] 监听媒体服务状态 [设备媒体回传通知][设备媒体后处理完成通知]
     */
    private static void handleMediaServiceListen() {
        printSeparator("[媒体服务模块] 监听媒体服务状态");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleMediaServiceListen", "获取workspace失败");
            return;
        }

        // 获取 [媒体服务模块]
        MediaServiceModule mediaServiceModule = workspace.getMediaServiceModule();
        if (mediaServiceModule == null) {
            log("ERROR", "handleMediaServiceListen", "获取 mediaServiceModule 失败(mediaServiceModule is null)");
            return;
        }

        // 监听 [设备媒体后处理完成通知]
        Device mainDevice = selectedDevice.getDevice();
        if (mainDevice == null) {
            log("ERROR", "handleMediaServiceListen", "获取 mainDevice 失败(mainDevice is null)");
            return;
        }

        String mainDeviceSn = mainDevice.getDeviceSn();
        if (mainDeviceSn == null) {
            log("ERROR", "handleMediaServiceListen", "获取 mainDeviceSn 失败(mainDeviceSn is null)");
            mainDeviceSn = "";
        }

        log("INFO", "main", "已选择设备 mainDevice SN: " + mainDeviceSn);

        String subDeviceSn = getSubDeviceSn(selectedDevice);
        if (subDeviceSn == null) {
                log("ERROR", "main", "没有关联的无人机设备，请选择其他有机库的设备");
                subDeviceSn = "";
        }

        int listenMediaFilePostProcessID = mediaServiceModule.listenMediaFilePostProcessMessage(
            subDeviceSn,
            new FlightHubCallback.MediaFilePostProcessCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.bean.MediaFilePostProcess msg) {
                    log("INFO", "handleMediaServiceListen", "listenMediaFilePostProcessMessage() onResult file_id=" + (msg != null ? msg.getFileId() : -1));
                    if (msg != null) {
                        log("INFO", "handleMediaServiceListen", "  device_sn: \"" + msg.getDeviceSn() + "\"");
                        log("INFO", "handleMediaServiceListen", "  flight_id: \"" + msg.getFlightId() + "\"");
                        log("INFO", "handleMediaServiceListen", "  object_key: \"" + msg.getObjectKey() + "\"");
                        log("INFO", "handleMediaServiceListen", "  object_size: " + msg.getObjectSize());
                        log("INFO", "handleMediaServiceListen", "  file_type: " + msg.getFileType() + ", 文件类型 {2: 图片, 3: 视频 }");
                        log("INFO", "handleMediaServiceListen", "  preview_object_key: \"" + msg.getPreviewObjectKey() + "\"");
                    }
                }
            }
        );

        log("INFO", "listenMediaFilePostProcessMessage", "listenMediaFilePostProcessMessage() 已调用, " +
            "subDeviceSn: " + "\"" + subDeviceSn + "\"" + ", " +
            "listenMediaFilePostProcessID = " + listenMediaFilePostProcessID + ", sleep some seconds."
        );

        // Sleep for 3 seconds;
        try {
            Thread.sleep(3000);  // 暂停 3 秒;
        } catch (InterruptedException e) {
            System.err.println("handleMediaServiceListen() 线程被中断: " + e.getMessage());
        }

        // 取消监听 [设备媒体后处理完成通知]
        mediaServiceModule.cancelListenMediaFilePostProcessMessage(listenMediaFilePostProcessID);
        listenMediaFilePostProcessID = -1;
        log("INFO", "cancelListenMediaFilePostProcessMessage", "cancelListenMediaFilePostProcessMessage() 已调用, now listenMediaFilePostProcessID = "
            + listenMediaFilePostProcessID + ", sleep some seconds."
        );

        // Sleep for 2 seconds;
        try {
            Thread.sleep(2000);  // 暂停 2 秒;
        } catch (InterruptedException e) {
            System.err.println("handleMediaServiceListen() 线程被中断: " + e.getMessage());
        }

        // 监听并打印回调;
        listenMediaFilePostProcessID = mediaServiceModule.listenMediaFilePostProcessMessage(
            mainDeviceSn,
            new FlightHubCallback.MediaFilePostProcessCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.MediaFilePostProcess msg) {
                log("INFO", "listenMediaFilePostProcessMessage", "listenMediaFilePostProcessMessage() onResult: ");
                log("INFO", "listenMediaFilePostProcessMessage", "file_id: " + msg.getFileId());
                log("INFO", "listenMediaFilePostProcessMessage", "device_sn: " + "\"" + msg.getDeviceSn() + "\"");
                log("INFO", "listenMediaFilePostProcessMessage", "flight_id: " + "\"" + msg.getFlightId() + "\"");
                log("INFO", "listenMediaFilePostProcessMessage", "object_key: " + "\"" + msg.getObjectKey() + "\"");
                log("INFO", "listenMediaFilePostProcessMessage", "object_size: " + msg.getObjectSize());
                log("INFO", "listenMediaFilePostProcessMessage", "file_type: " + msg.getFileType() + ", 文件类型 {2: 图片, 3: 视频 }");  // 文件类型，2：图片、3：视频;
                log("INFO", "listenMediaFilePostProcessMessage", "preview_object_key: " + "\"" + msg.getPreviewObjectKey() + "\"");

                // 打印所有信息;
                com.dji.flighthubsdk.bean.MediaFileImageMetaData imageMetaData = msg.getImageMetadata();
                if (imageMetaData == null) {
                    log("ERROR", "listenMediaFilePostProcessMessage", "imageMetaData is null");
                } else {
                    log("INFO", "listenMediaFilePostProcessMessage", "imageMetaData: ");
                    log("INFO", "listenMediaFilePostProcessMessage", "  photo_create_time: " + "\"" + imageMetaData.getPhotoCreateTime() + "\"");
                    log("INFO", "listenMediaFilePostProcessMessage", "  flight_yaw: " + imageMetaData.getFlightYaw());
                    log("INFO", "listenMediaFilePostProcessMessage", "  focal_length: " + imageMetaData.getFocalLength());
                    log("INFO", "listenMediaFilePostProcessMessage", "  gimbal_pitch: " + imageMetaData.getGimbalPitch());
                    log("INFO", "listenMediaFilePostProcessMessage", "  gimbal_roll: " + imageMetaData.getGimbalRoll());
                    log("INFO", "listenMediaFilePostProcessMessage", "  gimbal_yaw: " + imageMetaData.getGimbalYaw());
                    log("INFO", "listenMediaFilePostProcessMessage", "  height: " + imageMetaData.getHeight());
                    log("INFO", "listenMediaFilePostProcessMessage", "  width: " + imageMetaData.getWidth());
                    log("INFO", "listenMediaFilePostProcessMessage", "  zoom_factor: " + imageMetaData.getZoomFactor());
                    log("INFO", "listenMediaFilePostProcessMessage", "  digital_zoom_ratio: " + imageMetaData.getDigitalZoomRatio());
                    log("INFO", "listenMediaFilePostProcessMessage", "  length_in_35mm_film: " + imageMetaData.getLengthIn35mmFilm());
                    // 图片类型; {广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera"};
                    log("INFO", "listenMediaFilePostProcessMessage", "  image_type: " + "\"" + imageMetaData.getImageType() + "\"");
                    log("INFO", "listenMediaFilePostProcessMessage", "  payload_type: " + "\"" + imageMetaData.getPayloadType() + "\"");
                    log("INFO", "listenMediaFilePostProcessMessage", "  action_uuid: " + "\"" + imageMetaData.getActionUuid() + "\"");

                    log("INFO", "listenMediaFilePostProcessMessage", "  photo_position: ");
                    com.dji.flighthubsdk.bean.PositionData photoPosition = imageMetaData.getPhotoPosition();
                    if (photoPosition == null) {
                        log("ERROR", "listenMediaFilePostProcessMessage", "    photoPosition is null");
                    } else {
                        log("INFO", "listenMediaFilePostProcessMessage", "    lat: " + photoPosition.getLat());
                        log("INFO", "listenMediaFilePostProcessMessage", "    lng: " + photoPosition.getLng());
                        log("INFO", "listenMediaFilePostProcessMessage", "    abs_alt: " + photoPosition.getAbsAlt());
                        log("INFO", "listenMediaFilePostProcessMessage", "    rel_alt: " + photoPosition.getRelAlt());
                    }
                    
                    log("INFO", "listenMediaFilePostProcessMessage", "  shoot_position: ");
                    com.dji.flighthubsdk.bean.PositionData shootPosition = imageMetaData.getShootPosition();
                    if (shootPosition == null) {
                        log("ERROR", "listenMediaFilePostProcessMessage", "    shootPosition is null");
                    } else {
                        log("INFO", "listenMediaFilePostProcessMessage", "    lat: " + shootPosition.getLat());
                        log("INFO", "listenMediaFilePostProcessMessage", "    lng: " + shootPosition.getLng());
                        log("INFO", "listenMediaFilePostProcessMessage", "    abs_alt: " + shootPosition.getAbsAlt());
                        log("INFO", "listenMediaFilePostProcessMessage", "    rel_alt: " + shootPosition.getRelAlt());
                    }
                }

                com.dji.flighthubsdk.bean.MediaFileVideoMetaData videoMetaData = msg.getVideoMetadata();
                if (videoMetaData == null) {
                    log("ERROR", "listenMediaFilePostProcessMessage", "videoMetaData is null");
                } else {
                    log("INFO", "listenMediaFilePostProcessMessage", "videoMetaData: ");
                    log("INFO", "listenMediaFilePostProcessMessage", "  duration: " + videoMetaData.getDuration());
                    log("INFO", "listenMediaFilePostProcessMessage", "  encoder: " + "\"" + videoMetaData.getEncoder() + "\"");
                    log("INFO", "listenMediaFilePostProcessMessage", "  format_long_name: " + "\"" + videoMetaData.getFormatLongName() + "\"");
                    log("INFO", "listenMediaFilePostProcessMessage", "  format_name: " + "\""  + videoMetaData.getFormatName() + "\"");
                    log("INFO", "listenMediaFilePostProcessMessage", "  height: " + videoMetaData.getHeight());
                    log("INFO", "listenMediaFilePostProcessMessage", "  width: " + videoMetaData.getWidth());
                }


                // 收到后处理完成通知后，通过 object_key 获取下载地址;
                if (msg != null && msg.getObjectKey() != null && !msg.getObjectKey().isEmpty()) {
                    java.util.List<String> objectKeys = new java.util.ArrayList<>();
                    objectKeys.add(msg.getObjectKey());
                    mediaServiceModule.getMediaFilesDownloadURL(objectKeys, new FlightHubCallback.GetMediaFilesDownloadUrlListCallback() {
                        @Override
                        public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlListResults results) {
                            if (!error.isSuccess()) {
                                log("ERROR", "listenMediaFilePostProcessMessage", "getMediaFilesDownloadURL(objectKeys) 失败, error: " + error);
                                return;
                            }
                            if (results == null || results.getList().isEmpty()) {
                                log("WARN", "listenMediaFilePostProcessMessage", "getMediaFilesDownloadURL(objectKeys) 返回结果为空");
                                return;
                            }
                            for (int i = 0; i < results.getList().size(); i++) {
                                com.dji.flighthubsdk.bean.MediaFileDownloadInfo info = results.getList().get(i);
                                log("INFO", "listenMediaFilePostProcessMessage", "  download url[" + i + "]: object_key=" + info.getObjectKey() + ", url=" + info.getUrl());
                            }
                        }
                    });
                }

            }
        });

        // 再次监听;
        log("INFO", "listenMediaFilePostProcessMessage", "再次监听, listenMediaFilePostProcessMessage() 已调用, listenMediaFilePostProcessID = " + listenMediaFilePostProcessID);


        // listenMediaFileDeviceUploadMessage
        int listenMediaFileDeviceUploadID = mediaServiceModule.listenMediaFileDeviceUploadMessage(
            mainDeviceSn,
            new FlightHubCallback.MediaFileDeviceUploadCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.MediaFileDeviceUpload msg) {
                log("INFO", "handleMediaFileDeviceUploadMessage", "msg file_id=" + (msg != null ? msg.getFileId() : -1));
            }
        });


        log("INFO", "listenMediaFileDeviceUploadMessage", "listenMediaFileDeviceUploadMessage()已调用, " +
            "mainDeviceSn: " + mainDeviceSn + ", " +
            "listenMediaFileDeviceUploadID = " + listenMediaFileDeviceUploadID + ", sleep some seconds."
        );
        // Sleep for 3 seconds;
        try {
            Thread.sleep(3000);  // 暂停 3 秒;
        } catch (InterruptedException e) {
            System.err.println("handleMediaServiceListen() 线程被中断: " + e.getMessage());
        }

        // 取消监听 [设备媒体后处理完成通知]
        mediaServiceModule.cancelListenMediaFileDeviceUploadMessage(listenMediaFileDeviceUploadID);
        listenMediaFileDeviceUploadID = -1;
        log("INFO", "cancelListenMediaFileDeviceUploadMessage", "cancelListenMediaFileDeviceUploadMessage() 已调用, now listenMediaFileDeviceUploadID = "
            + listenMediaFileDeviceUploadID + ", sleep some seconds."
        );

        // Sleep for 2 seconds;
        try {
            Thread.sleep(2000);  // 暂停 2 秒;
        } catch (InterruptedException e) {
            System.err.println("handleMediaServiceListen() 线程被中断: " + e.getMessage());
        }


        // listenMediaFileDeviceUploadMessage
        listenMediaFileDeviceUploadID = mediaServiceModule.listenMediaFileDeviceUploadMessage(
            mainDeviceSn,
            new FlightHubCallback.MediaFileDeviceUploadCallback() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.MediaFileDeviceUpload msg) {
                log("INFO", "handleMediaFileDeviceUploadMessage", "onResult: ");
                if (msg == null) {
                    log("ERROR", "handleMediaFileDeviceUploadMessage", "msg is null");
                } else {
                    log("INFO", "handleMediaFileDeviceUploadMessage", "msg: ");
                    log("INFO", "handleMediaFileDeviceUploadMessage", "  ==>file_id: " + msg.getFileId());
                    log("INFO", "handleMediaFileDeviceUploadMessage", "  ==>flight_id: " + "\"" + msg.getFlightId() + "\"");
                    log("INFO", "handleMediaFileDeviceUploadMessage", "  ==>device_sn: " + "\"" + msg.getDeviceSn() + "\"");
                    log("INFO", "handleMediaFileDeviceUploadMessage", "  ==>object_key: " + "\"" + msg.getObjectKey() + "\"");
                    log("INFO", "handleMediaFileDeviceUploadMessage", "  ==>object_size: " + msg.getObjectSize());
                    log("INFO", "handleMediaFileDeviceUploadMessage", "  ==>file_type: " + msg.getFileType() + ", 注释: 图片(2), 视频(3)");  // 文件类型，2：图片、3：视频;
                }
            }
        });

        waitForKeyPress();
    }


    /**
     * [媒体服务模块] 媒体服务基本功能 [获取媒体文件下载地址][获取媒体文件列表][批量删除媒体文件]
     */
    private static void handleMediaServiceBasicFunctionality() {
        printSeparator("[媒体服务模块] 媒体服务基本功能 [获取媒体文件下载地址][获取媒体文件列表][批量删除媒体文件]");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleMediaServiceBasicFunctionality", "获取workspace失败");
            return;
        }

        // 获取 [媒体服务模块]
        MediaServiceModule mediaServiceModule = workspace.getMediaServiceModule();
        if (mediaServiceModule == null) {
            log("ERROR", "handleMediaServiceBasicFunctionality", "获取 mediaServiceModule 失败(mediaServiceModule is null)");
            return;
        }

        // 尽可能演示更多的 file id;
        // 无效id + 有效id;
        // 可以从 getMediaFilesList() 获取到;
        long fileIDs[] = { -2, -1, 0, 1, 5, 10, 37, 42, 47, 48, 49 };
        for (long fileId : fileIDs) {
            mediaServiceModule.getMediaFilesDownloadURL(fileId, new FlightHubCallback.GetMediaFilesDownloadUrlCallback() {
                @Override
                public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults results) {
                    if (!error.isSuccess()) {
                        log("ERROR", "handleMediaServiceBasicFunctionality", "getMediaFilesDownloadURL() 失败, error: " + error);
                        return;
                    }

                    if (results == null) {
                        log("ERROR", "handleMediaServiceBasicFunctionality", "getMediaFilesDownloadURL() 获取结果为空");
                        return;
                    } else {
                        log("INFO", "handleMediaServiceBasicFunctionality", "getMediaFilesDownloadURL() 成功, results:");
                        log("INFO", "handleMediaServiceBasicFunctionality", "  url: " + results.getUrl());
                    }
                }
            });

            // Sleep for 1.5 seconds;
            log("INFO", "handleMediaServiceBasicFunctionality", "sleep for some seconds.");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                System.err.println("handleMediaServiceBasicFunctionality() 线程被中断: " + e.getMessage());
            }
        }

        // 演示接口调用: 批量获取媒体文件下载地址 (通过 object_keys);
        java.util.List<String> objectKeys = new java.util.ArrayList<>();
        objectKeys.add("sample-object-key-1");
        objectKeys.add("sample-object-key-2");
        objectKeys.add("sample-object-key-3");
        mediaServiceModule.getMediaFilesDownloadURL(objectKeys, new FlightHubCallback.GetMediaFilesDownloadUrlListCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlListResults results) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleMediaServiceBasicFunctionality", "getMediaFilesDownloadURL(objectKeys) 失败, error: " + error);
                    return;
                }

                if (results == null) {
                    log("ERROR", "handleMediaServiceBasicFunctionality", "getMediaFilesDownloadURL(objectKeys) 获取结果为空");
                    return;
                }

                log("INFO", "handleMediaServiceBasicFunctionality", "getMediaFilesDownloadURL(objectKeys) 成功, results.list.size() = " + results.getList().size());
                for (int i = 0; i < results.getList().size(); i++) {
                    com.dji.flighthubsdk.bean.MediaFileDownloadInfo info = results.getList().get(i);
                    if (info == null) {
                        log("ERROR", "handleMediaServiceBasicFunctionality", "  list[" + i + "] is null");
                        continue;
                    }
                    log("INFO", "handleMediaServiceBasicFunctionality", "  list[" + i + "]:");
                    log("INFO", "handleMediaServiceBasicFunctionality", "    object_key: " + info.getObjectKey());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    url: " + info.getUrl());
                }
            }
        });

        // Sleep for 3 seconds;
        log("INFO", "handleMediaServiceBasicFunctionality", "sleep for some seconds after batch download URL call.");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.err.println("handleMediaServiceBasicFunctionality() 线程被中断: " + e.getMessage());
        }

        // Sleep for 5 seconds;
        log("INFO", "handleMediaServiceBasicFunctionality", "sleep for some seconds.");
        try {
            Thread.sleep(5000);  // 暂停 5 秒;
        } catch (InterruptedException e) {
            System.err.println("handleMediaServiceBasicFunctionality() 线程被中断: " + e.getMessage());
        }

        // 演示接口调用: 批量删除媒体文件;
        // 测试过复数 id 会失败，符合预期;
        long fileIDsToDelete[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        java.util.List<Long> fileIdsToDeleteList = new java.util.ArrayList<>(fileIDsToDelete.length);
        for (long id : fileIDsToDelete) {
            fileIdsToDeleteList.add(id);
        }
        mediaServiceModule.deleteMediaFiles(fileIdsToDeleteList, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleMediaServiceBasicFunctionality", "deleteMediaFiles() 批量删除媒体文件失败, error = " + error);
                } else {
                    log("INFO", "handleMediaServiceBasicFunctionality", "deleteMediaFiles() 批量删除媒体文件成功");
                }
            }
        });

        // 回调对象;
        FlightHubCallback.GetMediaFilesListCallback callback = new FlightHubCallback.GetMediaFilesListCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetMediaFilesListResults results) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleMediaServiceBasicFunctionality", "getMediaFilesList() 获取媒体文件列表失败, error: " + error);
                    return;
                }

                if (results == null) {
                    log("ERROR", "handleMediaServiceBasicFunctionality", "getMediaFilesList() 获取媒体文件列表失败, results is null");
                }

                log("INFO", "handleMediaServiceBasicFunctionality", "getMediaFilesList() 获取媒体文件列表成功, results:");
                // 页数据大小;
                log("INFO", "handleMediaServiceBasicFunctionality", "page_size: " + results.getPageSize());
                // 下一页的起始索引, 如果是最后一页, 则返回-1(表示获取完毕);
                log("INFO", "handleMediaServiceBasicFunctionality", "next_index: " + results.getNextIndex());
                // 总记录数;
                log("INFO", "handleMediaServiceBasicFunctionality", "total: " + results.getTotal());

                java.util.List<com.dji.flighthubsdk.bean.MediaFilesListItemInfoData> mediaFilesListItemInfoDataVector = results.getList();
                if (mediaFilesListItemInfoDataVector == null) {
                    log("ERROR", "handleMediaServiceBasicFunctionality", "getMediaFilesList() 获取媒体文件列表失败, results.list is null");
                    return;
                }

                log("INFO", "handleMediaServiceBasicFunctionality", "getMediaFilesList() 获取媒体文件列表成功, results.list.size() = "
                    + mediaFilesListItemInfoDataVector.size()
                );

                // 数据列表;
                log("INFO", "handleMediaServiceBasicFunctionality", "getMediaFilesList() 获取媒体文件列表成功, results.list:");
                int i = 0;
                for (com.dji.flighthubsdk.bean.MediaFilesListItemInfoData itemInfoData : mediaFilesListItemInfoDataVector) {
                    log("INFO", "handleMediaServiceBasicFunctionality",  "  itemInfoData[" + i + "]: ");
                    if (itemInfoData == null) {
                        log("ERROR", "handleMediaServiceBasicFunctionality", "    getMediaFilesList() 获取媒体文件列表失败, results.list[" + i + "] is null");
                        ++i;
                        continue;
                    }

                    log("INFO", "handleMediaServiceBasicFunctionality", "    file_id: " + itemInfoData.getFileId());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    flight_id: " + "\"" + itemInfoData.getFlightId() + "\"");
                    log("INFO", "handleMediaServiceBasicFunctionality", "    wayline_action_uuid: " + "\"" + itemInfoData.getWaylineActionUuid() + "\"");
                    log("INFO", "handleMediaServiceBasicFunctionality", "    size: " + itemInfoData.getSize());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    object_key: " + "\"" + itemInfoData.getObjectKey() + "\"");
                    log("INFO", "handleMediaServiceBasicFunctionality", "    file_name: " + itemInfoData.getFileName());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    suffix: " + "\"" + itemInfoData.getSuffix() + "\"");
                    log("INFO", "handleMediaServiceBasicFunctionality", "    device_sn: " + itemInfoData.getDeviceSn());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    payload_type: " + itemInfoData.getPayloadType());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    type: " + itemInfoData.getType());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    created_at: " + itemInfoData.getCreatedAt());
                    log("INFO", "handleMediaServiceBasicFunctionality", "    file_path: " + "\"" + itemInfoData.getFilePath() + "\"");

                    // 扩展字段; 扩展数据;
                    com.dji.flighthubsdk.bean.MediaFilesListItemExtData ext = itemInfoData.getExt();
                    if (ext == null) {
                        log("ERROR", "handleMediaServiceBasicFunctionality", "    results.list[" + i + "].ext is null");
                    } else {
                        log("INFO", "handleMediaServiceBasicFunctionality", "    results.list[" + i + "].ext:");
                        log("INFO", "handleMediaServiceBasicFunctionality", "      create_time: " + ext.getCreateTime());
                        log("INFO", "handleMediaServiceBasicFunctionality", "      flight_yaw: " + ext.getFlightYaw());
                        log("INFO", "handleMediaServiceBasicFunctionality", "      focal_length: " + ext.getFocalLength());
                        log("INFO", "handleMediaServiceBasicFunctionality", "      gimbal_pitch: " + ext.getGimbalPitch());
                        log("INFO", "handleMediaServiceBasicFunctionality", "      gimbal_roll: " + ext.getGimbalRoll());
                        log("INFO", "handleMediaServiceBasicFunctionality", "      gimbal_yaw: " + ext.getGimbalYaw());
                        log("INFO", "handleMediaServiceBasicFunctionality", "      width: " + ext.getWidth());
                        log("INFO", "handleMediaServiceBasicFunctionality", "      height: " + ext.getHeight());
                        // 图片类型; {广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera"}
                        log("INFO", "handleMediaServiceBasicFunctionality", "      image_type: " + "\"" + ext.getImageType() + "\"");
                        // 焦距倍数, 服务端可能无返回值, 默认为 INT_MIN 无效值;
                        log("INFO", "handleMediaServiceBasicFunctionality", "      zoom_factor: " + ext.getZoomFactor());

                        // 图片位置信息;
                        com.dji.flighthubsdk.bean.PositionData photoPosition = ext.getPhotoPosition();
                        if (photoPosition == null) {
                            log("ERROR", "handleMediaServiceBasicFunctionality", "      photoPosition is null");
                        } else {
                            log("INFO", "handleMediaServiceBasicFunctionality", "      photoPosition:");
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => lat: " + photoPosition.getLat());
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => lng: " + photoPosition.getLng());
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => abs_alt: " + photoPosition.getAbsAlt());
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => rel_alt: " + photoPosition.getRelAlt());
                        }

                        // 飞机位置信息;
                        com.dji.flighthubsdk.bean.PositionData shootPosition = ext.getShootPosition();
                        if (shootPosition == null) {
                            log("ERROR", "handleMediaServiceBasicFunctionality", "      shootPosition is null");
                        } else {
                            log("INFO", "handleMediaServiceBasicFunctionality", "      shootPosition:");
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => lat: " + shootPosition.getLat());
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => lng: " + shootPosition.getLng());
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => abs_alt: " + shootPosition.getAbsAlt());
                            log("INFO", "handleMediaServiceBasicFunctionality", "        => rel_alt: " + shootPosition.getRelAlt());
                        }
                    }
                    // 递增索引;
                    ++i;
                }
            }
        };

        // 请求 获取 媒体文件列表;
        mediaServiceModule.getMediaFilesList(
            "",  // [可选参数] 飞行航线ID, 不指定则传空字符串;
            0,   // [必须参数] 下一页请求的起始 index;
            100, // [必须参数] 每页大小;
            java.util.Arrays.asList("2", "3"),  // file_types, 文件枚举类型(2图片、3视频)
            "",   // device_sn, 不指定则传空字符串;
            -1L,  // 开始时间(秒), -1 表示不传该参数;
            -1L,  // 结束时间(秒), -1 表示不传该参数;
            callback);

        waitForKeyPress();
    }
    
    /**
     * 安全服务模块: 获取信号地图;
     */
    private static void handleSearchSignalMaps() {
        printSeparator("[安全服务模块] 获取信号地图");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleSearchSignalMaps", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleSearchSignalMaps", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 设备序列号列表;
        java.util.List<String> deviceSns = java.util.Collections.singletonList("YOUR_DEVICE_SN");
        java.util.List<String> dataTypes = java.util.Arrays.asList("4g", "sdr");

        // 回调对象
        FlightHubCallback.SearchSignalMapsCallback callback = new FlightHubCallback.SearchSignalMapsCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SignalMapsResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleSearchSignalMaps", "searchSignalMaps 失败, error: " + error);
                    return;
                }
                log("INFO", "handleSearchSignalMaps", "searchSignalMaps 成功");
                if (result == null) {
                    log("ERROR", "handleSearchSignalMaps", "result is null");
                    return;
                }
                java.util.List<com.dji.flighthubsdk.bean.DeviceSignalItem> lists = result.getLists();
                if (lists == null) {
                    log("INFO", "handleSearchSignalMaps", "lists is null");
                    return;
                }
                log("INFO", "handleSearchSignalMaps", "设备信号地图数量: " + lists.size());
                for (int i = 0; i < lists.size(); i++) {
                    com.dji.flighthubsdk.bean.DeviceSignalItem deviceItem = lists.get(i);
                    log("INFO", "handleSearchSignalMaps", "  [" + i + "] device_sn: " + deviceItem.getDeviceSn());
                    java.util.List<com.dji.flighthubsdk.bean.GridSignalItem> grids = deviceItem.getGrids();
                    if (grids == null) {
                        log("INFO", "handleSearchSignalMaps", "    grids is null");
                        continue;
                    }
                    log("INFO", "handleSearchSignalMaps", "    网格数量: " + grids.size());
                    for (int j = 0; j < grids.size(); j++) {
                        com.dji.flighthubsdk.bean.GridSignalItem grid = grids.get(j);
                        log("INFO", "handleSearchSignalMaps", "    grid[" + j + "]:");
                        log("INFO", "handleSearchSignalMaps", "      => h3_index: " + "\"" + grid.getH3Index() + "\"");
                        log("INFO", "handleSearchSignalMaps", "      => sdr_avg: " + grid.getSdrAvg());
                        log("INFO", "handleSearchSignalMaps", "      => sdr_max: " + grid.getSdrMax());
                        log("INFO", "handleSearchSignalMaps", "      => sdr_min: " + grid.getSdrMin());
                        log("INFO", "handleSearchSignalMaps", "      => signal_4g_avg: " + grid.getSignal4gAvg());
                        log("INFO", "handleSearchSignalMaps", "      => signal_4g_max: " + grid.getSignal4gMax());
                        log("INFO", "handleSearchSignalMaps", "      => signal_4g_min: " + grid.getSignal4gMin());
                    }
                }
            }
        };

        // 请求 获取 信号地图
        safetyServiceModule.searchSignalMaps(deviceSns, dataTypes, callback);

        waitForKeyPress();
    }


    /**
     * 安全服务模块: 创建自定义飞行区域;
     */
    private static void handleCreateCustomizedFlightAreas() {
        printSeparator("[安全服务模块] 创建自定义飞行区域");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleCreateCustomizedFlightAreas", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleCreateCustomizedFlightAreas", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建 content.properties
        com.dji.flighthubsdk.bean.ContentsPropertyData properties =
                new com.dji.flighthubsdk.bean.ContentsPropertyData("#FF0000", 2, false);

        // 构建 content.geometry (Circle: 圆形区域)
        java.util.List<Double> center = java.util.Arrays.asList(0.0, 0.0); // lng, lat
        com.dji.flighthubsdk.bean.ContentsGeometryData geometry =
                new com.dji.flighthubsdk.bean.ContentsGeometryData(
                        "Circle", java.util.Collections.emptyList(), center, 500.0);

        // 构建 content
        com.dji.flighthubsdk.bean.FlightAreasContentData content =
                new com.dji.flighthubsdk.bean.FlightAreasContentData("Feature", properties, geometry);

        // 构建请求参数
        com.dji.flighthubsdk.bean.CreateCustomizedFlightAreasParams requestParams =
                new com.dji.flighthubsdk.bean.CreateCustomizedFlightAreasParams();
        requestParams.setName("圆形限飞区");
        requestParams.setType("nfz");  // {"nfz" 限飞区; "dfence" 作业区; "noland" 禁降区}
        requestParams.setContent(content);
        requestParams.setCreatedBy("demo_user");

        // 回调对象
        FlightHubCallback.CreateCustomizedFlightAreasCallback callback = new FlightHubCallback.CreateCustomizedFlightAreasCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.CreateFlightAreasResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleCreateCustomizedFlightAreas", "createCustomizedFlightAreas 失败, error: " + error);
                    return;
                }
                log("INFO", "handleCreateCustomizedFlightAreas", "createCustomizedFlightAreas 成功");
                if (result == null) {
                    log("ERROR", "handleCreateCustomizedFlightAreas", "result is null");
                    return;
                }
                log("INFO", "handleCreateCustomizedFlightAreas", "  => id:   " + result.getId());
                log("INFO", "handleCreateCustomizedFlightAreas", "  => name: " + result.getName());
            }
        };

        // 请求 创建 自定义飞行区域
        safetyServiceModule.createCustomizedFlightAreas(requestParams, callback);

        waitForKeyPress();
    }


    /**
     * 安全服务模块: 获取自定义飞行区域;
     */
    private static void handleGetCustomizedFlightAreas() {
        printSeparator("[安全服务模块] 获取自定义飞行区域");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetCustomizedFlightAreas", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleGetCustomizedFlightAreas", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 回调对象
        FlightHubCallback.GetCustomizedFlightAreasCallback callback = new FlightHubCallback.GetCustomizedFlightAreasCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetFlightAreasResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetCustomizedFlightAreas", "getCustomizedFlightAreas 失败, error: " + error);
                    return;
                }
                log("INFO", "handleGetCustomizedFlightAreas", "getCustomizedFlightAreas 成功");
                if (result == null) {
                    log("ERROR", "handleGetCustomizedFlightAreas", "result is null");
                    return;
                }
                // 打印分页信息
                com.dji.flighthubsdk.bean.FlightAreaPagination pagination = result.getPagination();
                if (pagination != null) {
                    log("INFO", "handleGetCustomizedFlightAreas", "分页信息:");
                    log("INFO", "handleGetCustomizedFlightAreas", "  => page:      " + pagination.getPage());
                    log("INFO", "handleGetCustomizedFlightAreas", "  => page_size: " + pagination.getPageSize());
                    log("INFO", "handleGetCustomizedFlightAreas", "  => total:     " + pagination.getTotal());
                }
                // 打印飞行区列表
                java.util.List<com.dji.flighthubsdk.bean.FlightAreaInfoItem> list = result.getList();
                if (list == null) {
                    log("INFO", "handleGetCustomizedFlightAreas", "list is null");
                    return;
                }
                log("INFO", "handleGetCustomizedFlightAreas", "飞行区域数量: " + list.size());
                for (int i = 0; i < list.size(); i++) {
                    com.dji.flighthubsdk.bean.FlightAreaInfoItem item = list.get(i);
                    log("INFO", "handleGetCustomizedFlightAreas", "  [" + i + "]:");
                    log("INFO", "handleGetCustomizedFlightAreas", "    => id:           " + item.getId());
                    log("INFO", "handleGetCustomizedFlightAreas", "    => name:          " + item.getName());
                    log("INFO", "handleGetCustomizedFlightAreas", "    => status:        " + item.getStatus());
                    log("INFO", "handleGetCustomizedFlightAreas", "    => type:          " + item.getType());
                    log("INFO", "handleGetCustomizedFlightAreas", "    => created_time:  " + item.getCreatedTime());
                    log("INFO", "handleGetCustomizedFlightAreas", "    => created_by:    " + item.getCreatedBy());
                    log("INFO", "handleGetCustomizedFlightAreas", "    => updated_time:  " + item.getUpdatedTime());
                    log("INFO", "handleGetCustomizedFlightAreas", "    => updated_by:    " + item.getUpdatedBy());
                }
            }
        };

        // 请求 获取 自定义飞行区域
        safetyServiceModule.getCustomizedFlightAreas(
            "",  // 不按名称过滤
            "",  // 不按类型过滤 (或指定 "nfz"/"dfence"/"noland")
            "",  // 不按状态过滤
            java.util.Collections.emptyList(),  // 不按ID过滤
            1,   // 第1页
            50,  // 每页50条
            callback);

        waitForKeyPress();
    }


    /**
     * 安全服务模块: 更新自定义飞行区域;
     */
    private static void handleUpdateCustomizedFlightAreas() {
        printSeparator("[安全服务模块] 更新自定义飞行区域");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleUpdateCustomizedFlightAreas", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleUpdateCustomizedFlightAreas", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建 content.properties
        com.dji.flighthubsdk.bean.ContentsPropertyData properties =
                new com.dji.flighthubsdk.bean.ContentsPropertyData("#00FF00", 5, false);

        // 构建 content.geometry (Circle: 圆形区域)
        java.util.List<Double> center = java.util.Arrays.asList(0.0, 0.0); // lng, lat
        com.dji.flighthubsdk.bean.ContentsGeometryData geometry =
                new com.dji.flighthubsdk.bean.ContentsGeometryData(
                        "Circle", java.util.Collections.emptyList(), center, 300.0);

        // 构建 content
        com.dji.flighthubsdk.bean.FlightAreasContentData content =
                new com.dji.flighthubsdk.bean.FlightAreasContentData("Feature", properties, geometry);

        // 构建请求参数 (flight_area_id 替换为实际飞行区ID)
        com.dji.flighthubsdk.bean.UpdateCustomizedFlightAreasParams requestParams =
                new com.dji.flighthubsdk.bean.UpdateCustomizedFlightAreasParams();
        // TODO: 替换为实际飞行区ID;
        requestParams.setFlightAreaId("your-flight-area-id");
        requestParams.setName("更新后的圆形限飞区");
        requestParams.setContent(content);
        // TODO: 替换为实际用户ID;
        requestParams.setUpdateBy("demo_user");

        // 回调对象
        FlightHubCallback.UpdateCustomizedFlightAreasCallback callback = new FlightHubCallback.UpdateCustomizedFlightAreasCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.UpdateFlightAreasResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleUpdateCustomizedFlightAreas", "updateCustomizedFlightAreas 失败, error: " + error);
                    return;
                }
                log("INFO", "handleUpdateCustomizedFlightAreas", "updateCustomizedFlightAreas 成功");
                if (result == null) {
                    log("ERROR", "handleUpdateCustomizedFlightAreas", "result is null");
                    return;
                }
                log("INFO", "handleUpdateCustomizedFlightAreas", "  => id: " + result.getId());
                com.dji.flighthubsdk.bean.UpdatedAreaData updatedArea = result.getUpdatedArea();
                if (updatedArea != null) {
                    log("INFO", "handleUpdateCustomizedFlightAreas", "  updated_area:");
                    log("INFO", "handleUpdateCustomizedFlightAreas", "    => id:     " + updatedArea.getId());
                    log("INFO", "handleUpdateCustomizedFlightAreas", "    => name:   " + updatedArea.getName());
                    log("INFO", "handleUpdateCustomizedFlightAreas", "    => status: " + updatedArea.getStatus());
                    log("INFO", "handleUpdateCustomizedFlightAreas", "    => type:   " + updatedArea.getType());
                }
            }
        };

        // 请求 更新 自定义飞行区域
        safetyServiceModule.updateCustomizedFlightAreas(requestParams, callback);

        waitForKeyPress();
    }


    /**
     * 安全服务模块: 删除自定义飞行区域;
     */
    private static void handleDeleteCustomizedFlightAreas() {
        printSeparator("[安全服务模块] 删除自定义飞行区域");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleDeleteCustomizedFlightAreas", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleDeleteCustomizedFlightAreas", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // TODO: 替换为实际飞行区ID
        String flightAreaId = "your-flight-area-id";
        log("INFO", "handleDeleteCustomizedFlightAreas", "正在删除飞行区域, id: " + flightAreaId);

        // 请求 删除 自定义飞行区域
        safetyServiceModule.deleteCustomizedFlightAreas(flightAreaId, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleDeleteCustomizedFlightAreas", "deleteCustomizedFlightAreas 失败, error: " + error);
                    return;
                }
                log("INFO", "handleDeleteCustomizedFlightAreas", "deleteCustomizedFlightAreas 成功");
            }
        });

        waitForKeyPress();
    }


    /**
     * 安全服务模块: 同步自定义飞行区域;
     */
    private static void handleSyncCustomizedFlightAreas() {
        printSeparator("[安全服务模块] 同步自定义飞行区域");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleSyncCustomizedFlightAreas", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleSyncCustomizedFlightAreas", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建设备SN列表 (机场设备);
        // TODO: 替换为实际机场设备SN列表;
        java.util.List<String> deviceSns = new java.util.ArrayList<>(config.devices.dock_sns);

        // 回调对象
        FlightHubCallback.SyncCustomizedFlightAreasCallback callback = new FlightHubCallback.SyncCustomizedFlightAreasCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.SyncFlightAreasResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleSyncCustomizedFlightAreas", "syncCustomizedFlightAreas 失败, error: " + error);
                    return;
                }
                log("INFO", "handleSyncCustomizedFlightAreas", "syncCustomizedFlightAreas 成功");
                if (result == null) {
                    log("ERROR", "handleSyncCustomizedFlightAreas", "result is null");
                    return;
                }
                java.util.List<com.dji.flighthubsdk.bean.DeviceSyncResults> results = result.getResults();
                if (results == null) {
                    log("INFO", "handleSyncCustomizedFlightAreas", "results is null");
                    return;
                }
                log("INFO", "handleSyncCustomizedFlightAreas", "同步结果数量: " + results.size());
                for (int i = 0; i < results.size(); i++) {
                    com.dji.flighthubsdk.bean.DeviceSyncResults item = results.get(i);
                    log("INFO", "handleSyncCustomizedFlightAreas", "  [" + i + "]:");
                    log("INFO", "handleSyncCustomizedFlightAreas", "    => device_sn: " + item.getDeviceSn());
                    log("INFO", "handleSyncCustomizedFlightAreas", "    => code:       " + item.getCode());
                }
            }
        };

        // 请求 同步 自定义飞行区域
        safetyServiceModule.syncCustomizedFlightAreas(deviceSns, callback);

        waitForKeyPress();
    }


    /**
     * 安全服务模块: 获取自定义飞行区域同步状态;
     */
    private static void handleGetFlightAreasSyncStatus() {
        printSeparator("[安全服务模块] 获取自定义飞行区域同步状态");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetFlightAreasSyncStatus", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleGetFlightAreasSyncStatus", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建设备SN列表 (机场设备)
        java.util.List<String> deviceSns = new java.util.ArrayList<>(config.devices.dock_sns);

        // 回调对象
        FlightHubCallback.GetFlightAreasSyncStatusCallback callback = new FlightHubCallback.GetFlightAreasSyncStatusCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetAreasSyncStatusResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetFlightAreasSyncStatus", "getFlightAreasSyncStatus 失败, error: " + error);
                    return;
                }
                log("INFO", "handleGetFlightAreasSyncStatus", "getFlightAreasSyncStatus 成功");
                if (result == null) {
                    log("ERROR", "handleGetFlightAreasSyncStatus", "result is null");
                    return;
                }
                java.util.List<com.dji.flighthubsdk.bean.DeviceSyncStatusResults> list = result.getList();
                if (list == null) {
                    log("INFO", "handleGetFlightAreasSyncStatus", "list is null");
                    return;
                }
                log("INFO", "handleGetFlightAreasSyncStatus", "设备同步状态数量: " + list.size());
                for (int i = 0; i < list.size(); i++) {
                    com.dji.flighthubsdk.bean.DeviceSyncStatusResults item = list.get(i);
                    log("INFO", "handleGetFlightAreasSyncStatus", "  [" + i + "]:");
                    log("INFO", "handleGetFlightAreasSyncStatus", "    => sn:     " + item.getSn());
                    log("INFO", "handleGetFlightAreasSyncStatus", "    => status: " + item.getStatus());
                    // reason 失败原因码枚举：0=无错误(成功), 1=解析云端返回的文件信息失败, 2=获取飞行器端文件信息失败,
                    // 3=从云端下载文件失败, 4=链路翻转失败, 5=传输文件失败, 6=disable失败, 7=自定义飞行区删除失败,
                    // 8=飞行器端加载作业区域数据失败, 9=enable失败, 10=机场增强图传无法关闭，作业区域数据同步失败,
                    // 11=飞行器开机失败，无法同步作业区域数据, 12=checksum校验失败, 13=同步异常超时,
                    // 255=未知错误(机场侧快速起飞，等不急司空返回结果，主动上报255)
                    log("INFO", "handleGetFlightAreasSyncStatus", "    => reason: " + item.getReason());
                }
            }
        };

        // 请求 获取 自定义飞行区域同步状态
        safetyServiceModule.getFlightAreasSyncStatus(deviceSns, callback);

        waitForKeyPress();
    }


    // ==================== 地形障碍物 ====================

    /**
     * 安全服务模块 [地形障碍物]: 查询离线地图模型信息;
     */
    private static void handleGetOfflineMapsModelInfo() {
        printSeparator("[安全服务模块][地形障碍物] 查询离线地图模型信息");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetOfflineMapsModelInfo", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleGetOfflineMapsModelInfo", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 请求 查询离线地图模型信息
        safetyServiceModule.getOfflineMapsModelInfo(new FlightHubCallback.GetOfflineMapsModelInfoCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetOfflineMapsModelInfoResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetOfflineMapsModelInfo", "getOfflineMapsModelInfo 失败, error: " + error);
                    return;
                }
                log("INFO", "handleGetOfflineMapsModelInfo", "getOfflineMapsModelInfo 成功");
                if (result == null) {
                    log("ERROR", "handleGetOfflineMapsModelInfo", "result is null");
                    return;
                }
                log("INFO", "handleGetOfflineMapsModelInfo", "  => disable: " + result.isDisable());

                com.dji.flighthubsdk.bean.OfflineMapsModelData offlineMap = result.getOfflineMap();
                if (offlineMap == null) {
                    log("INFO", "handleGetOfflineMapsModelInfo", "  offline_map is null");
                    return;
                }
                log("INFO", "handleGetOfflineMapsModelInfo", "  offline_map:");
                log("INFO", "handleGetOfflineMapsModelInfo", "    => id:           " + offlineMap.getId());
                log("INFO", "handleGetOfflineMapsModelInfo", "    => status:        " + offlineMap.getStatus());
                log("INFO", "handleGetOfflineMapsModelInfo", "    => percent:       " + offlineMap.getPercent());
                log("INFO", "handleGetOfflineMapsModelInfo", "    => result:        " + offlineMap.getResult());
                log("INFO", "handleGetOfflineMapsModelInfo", "    => updated_time:  " + offlineMap.getUpdatedTime());

                java.util.List<com.dji.flighthubsdk.bean.OfflineMapsModelInfoData> models = offlineMap.getModels();
                if (models == null) {
                    log("INFO", "handleGetOfflineMapsModelInfo", "    models is null");
                    return;
                }
                log("INFO", "handleGetOfflineMapsModelInfo", "    models 数量: " + models.size());
                for (int i = 0; i < models.size(); i++) {
                    log("INFO", "handleGetOfflineMapsModelInfo", "      [" + i + "] uuid: " + models.get(i).getUuid());
                }
            }
        });

        waitForKeyPress();
    }


    /**
     * 安全服务模块 [地形障碍物]: 创建/更新离线地图模型;
     */
    private static void handleUpdateOfflineMapsModelInfo() {
        printSeparator("[安全服务模块][地形障碍物] 创建/更新离线地图模型");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleUpdateOfflineMapsModelInfo", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleUpdateOfflineMapsModelInfo", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建 3D 模型条目
        com.dji.flighthubsdk.bean.ModelItemInfoData model3dItem = new com.dji.flighthubsdk.bean.ModelItemInfoData();
        model3dItem.setUuid("your-model-uuid");          // TODO: 替换为实际模型 UUID
        model3dItem.setObjectKey("path/to/model.osgb"); // TODO: 替换为实际对象存储 key
        model3dItem.setFileType("osgb");

        java.util.List<com.dji.flighthubsdk.bean.ModelItemInfoData> model3dItems = new java.util.ArrayList<>();
        model3dItems.add(model3dItem);

        // 构建请求参数 (2D 模型列表留空)
        com.dji.flighthubsdk.bean.UpdateOfflineMapsParams requestParams = new com.dji.flighthubsdk.bean.UpdateOfflineMapsParams();
        requestParams.setModel3dItems(model3dItems);
        requestParams.setModel2dItems(new java.util.ArrayList<>());

        // 请求 创建/更新 离线地图模型
        safetyServiceModule.updateOfflineMapsModelInfo(requestParams, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleUpdateOfflineMapsModelInfo", "updateOfflineMapsModelInfo 失败, error: " + error);
                    return;
                }
                log("INFO", "handleUpdateOfflineMapsModelInfo", "updateOfflineMapsModelInfo 成功");
            }
        });

        waitForKeyPress();
    }


    /**
     * 安全服务模块 [地形障碍物]: 取消离线地图生成;
     */
    private static void handleCancelOfflineMapsModelInfo() {
        printSeparator("[安全服务模块][地形障碍物] 取消离线地图生成");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleCancelOfflineMapsModelInfo", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleCancelOfflineMapsModelInfo", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 请求 取消 离线地图生成
        safetyServiceModule.cancelOfflineMapsModelInfo(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleCancelOfflineMapsModelInfo", "cancelOfflineMapsModelInfo 失败, error: " + error);
                    return;
                }
                log("INFO", "handleCancelOfflineMapsModelInfo", "cancelOfflineMapsModelInfo 成功");
            }
        });

        waitForKeyPress();
    }


    /**
     * 安全服务模块 [地形障碍物]: 修改离线地图开关;
     */
    private static void handleSetOfflineMaps() {
        printSeparator("[安全服务模块][地形障碍物] 修改离线地图开关");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleSetOfflineMaps", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleSetOfflineMaps", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建请求参数: 开启离线地图
        com.dji.flighthubsdk.bean.SetOfflineMapsParams requestParams = new com.dji.flighthubsdk.bean.SetOfflineMapsParams();
        requestParams.setOfflineMapEnable(true);

        // 请求 修改 离线地图开关
        safetyServiceModule.setOfflineMaps(requestParams, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleSetOfflineMaps", "setOfflineMaps 失败, error: " + error);
                    return;
                }
                log("INFO", "handleSetOfflineMaps", "setOfflineMaps 成功, offline_map_enable: " + requestParams.isOfflineMapEnable());
            }
        });

        waitForKeyPress();
    }


    /**
     * 安全服务模块 [地形障碍物]: 同步离线地图至设备;
     */
    private static void handleSyncOfflineMaps() {
        printSeparator("[安全服务模块][地形障碍物] 同步离线地图至设备");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleSyncOfflineMaps", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleSyncOfflineMaps", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建设备SN列表 (机场设备)
        java.util.List<String> deviceSns = new java.util.ArrayList<>(config.devices.dock_sns);

        // 请求 同步 离线地图至设备
        safetyServiceModule.syncOfflineMaps(deviceSns, new FlightHubCallback.SyncOfflineMapsCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.OfflineMapsSyncResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleSyncOfflineMaps", "syncOfflineMaps 失败, error: " + error);
                    return;
                }
                log("INFO", "handleSyncOfflineMaps", "syncOfflineMaps 成功");
                if (result == null) {
                    log("ERROR", "handleSyncOfflineMaps", "result is null");
                    return;
                }
                java.util.List<com.dji.flighthubsdk.bean.DeviceSyncResults> results = result.getResults();
                if (results == null) {
                    log("INFO", "handleSyncOfflineMaps", "results is null");
                    return;
                }
                log("INFO", "handleSyncOfflineMaps", "同步结果数量: " + results.size());
                for (int i = 0; i < results.size(); i++) {
                    com.dji.flighthubsdk.bean.DeviceSyncResults item = results.get(i);
                    log("INFO", "handleSyncOfflineMaps", "  [" + i + "]:");
                    log("INFO", "handleSyncOfflineMaps", "    => device_sn: " + item.getDeviceSn());
                    log("INFO", "handleSyncOfflineMaps", "    => code:       " + item.getCode());
                }
            }
        });

        waitForKeyPress();
    }


    /**
     * 安全服务模块 [地形障碍物]: 获取设备离线地图同步状态;
     */
    private static void handleGetOfflineMapsSyncStatus() {
        printSeparator("[安全服务模块][地形障碍物] 获取设备离线地图同步状态");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetOfflineMapsSyncStatus", "获取workspace失败");
            return;
        }

        // 获取 [安全服务模块]
        SafetyServiceModule safetyServiceModule = workspace.getSafetyServiceModule();
        if (safetyServiceModule == null) {
            log("ERROR", "handleGetOfflineMapsSyncStatus", "获取 safetyServiceModule 失败(safetyServiceModule is null)");
            return;
        }

        // 构建设备SN列表 (机场设备)
        java.util.List<String> deviceSns = new java.util.ArrayList<>(config.devices.dock_sns);

        // 请求 获取 设备离线地图同步状态
        safetyServiceModule.getOfflineMapsSyncStatus(deviceSns, new FlightHubCallback.GetOfflineMapsSyncStatusCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetOfflineMapsSyncStatusResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetOfflineMapsSyncStatus", "getOfflineMapsSyncStatus 失败, error: " + error);
                    return;
                }
                log("INFO", "handleGetOfflineMapsSyncStatus", "getOfflineMapsSyncStatus 成功");
                if (result == null) {
                    log("ERROR", "handleGetOfflineMapsSyncStatus", "result is null");
                    return;
                }
                java.util.List<com.dji.flighthubsdk.bean.DeviceSyncStatusResults> list = result.getList();
                if (list == null) {
                    log("INFO", "handleGetOfflineMapsSyncStatus", "list is null");
                    return;
                }
                log("INFO", "handleGetOfflineMapsSyncStatus", "设备同步状态数量: " + list.size());
                for (int i = 0; i < list.size(); i++) {
                    com.dji.flighthubsdk.bean.DeviceSyncStatusResults item = list.get(i);
                    log("INFO", "handleGetOfflineMapsSyncStatus", "  [" + i + "]:");
                    log("INFO", "handleGetOfflineMapsSyncStatus", "    => sn:     " + item.getSn());
                    log("INFO", "handleGetOfflineMapsSyncStatus", "    => status: " + item.getStatus());
                    // reason 失败原因码枚举：0=无错误(成功), 1=解析云端返回的文件信息失败, 2=获取飞行器端文件信息失败,
                    // 3=从云端下载文件失败, 4=链路翻转失败, 5=传输文件失败, 6=disable失败, 7=自定义飞行区删除失败,
                    // 8=飞行器端加载作业区域数据失败, 9=enable失败, 10=机场增强图传无法关闭，作业区域数据同步失败,
                    // 11=飞行器开机失败，无法同步作业区域数据, 12=checksum校验失败, 13=同步异常超时,
                    // 255=未知错误(机场侧快速起飞，等不急司空返回结果，主动上报255)
                    log("INFO", "handleGetOfflineMapsSyncStatus", "    => reason: " + item.getReason());
                }
            }
        });

        waitForKeyPress();
    }

    // ==================== 飞行任务记录模块 ====================

    /**
     * 飞行任务记录模块: 导出飞行记录;
     */
    private static void handleExportFlightRecords() {
        printSeparator("[飞行任务记录模块] 导出飞行记录");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleExportFlightRecords", "获取workspace失败");
            return;
        }

        // 获取 [飞行任务记录模块]
        FlightTaskRecordModule flightTaskRecordModule = workspace.getFlightTaskRecordModule();
        if (flightTaskRecordModule == null) {
            log("ERROR", "handleExportFlightRecords", "获取 flightTaskRecordModule 失败(flightTaskRecordModule is null)");
            return;
        }

        // 请求 导出 飞行记录
        flightTaskRecordModule.exportFlightRecords(
            "demo_user_id",
            "details",              // "summary" 或 "details"
            "flight_records_export",
            "Asia/Shanghai",
            "zh",
            java.util.Collections.singletonList("Excel"),
            java.util.Collections.emptyList(),  // 飞行任务ID列表 (空列表表示导出所有)
            java.util.Collections.emptyList(),  // 导出指标列表 (空列表使用默认指标)
            new FlightHubCallback.ExportFlightRecordsCallback() {
                @Override
                public void onResult(FlightHubError error,
                        com.dji.flighthubsdk.bean.ExportFlightRecordsResults results) {
                    if (!error.isSuccess()) {
                        log("ERROR", "handleExportFlightRecords", "exportFlightRecords 失败, error: " + error);
                        return;
                    }
                    log("INFO", "handleExportFlightRecords", "exportFlightRecords 成功");
                    if (results == null) {
                        log("ERROR", "handleExportFlightRecords", "results is null");
                        return;
                    }
                    log("INFO", "handleExportFlightRecords", "  => export_id: " + results.getExportId());
                    log("INFO", "handleExportFlightRecords", "  提示: 使用此 export_id 可通过 [381] 查询导出进度");
                }
            });

        waitForKeyPress();
    }


    /**
     * 飞行任务记录模块: 查询导出进度;
     */
    private static void handleGetExportFlightRecordsProgress() {
        printSeparator("[飞行任务记录模块] 查询导出进度");

        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetExportFlightRecordsProgress", "获取workspace失败");
            return;
        }

        // 获取 [飞行任务记录模块]
        FlightTaskRecordModule flightTaskRecordModule = workspace.getFlightTaskRecordModule();
        if (flightTaskRecordModule == null) {
            log("ERROR", "handleGetExportFlightRecordsProgress", "获取 flightTaskRecordModule 失败(flightTaskRecordModule is null)");
            return;
        }

        // 构建请求参数
        // 请求 查询 导出进度
        flightTaskRecordModule.getExportFlightRecordsProgress(
            "",   // 不按类型过滤; "summary"/"details" 可筛选特定类型
            -1,   // -1: 查询所有状态; 0: 导出中; 1: 完成; 2: 失败; 3: 已取消
            "",   // "" 默认按时间降序; "ASC" 升序
            1,    // 第 1 页
            50,   // 每页 50 条
            "",   // 不指定特定任务; 填入 export_id 可查单条
            new FlightHubCallback.GetExportFlightRecordsProgressCallback() {
                @Override
                public void onResult(FlightHubError error,
                        com.dji.flighthubsdk.bean.GetExportFlightRecordsProgressResults results) {
                    if (!error.isSuccess()) {
                        log("ERROR", "handleGetExportFlightRecordsProgress", "getExportFlightRecordsProgress 失败, error: " + error);
                        return;
                    }
                    log("INFO", "handleGetExportFlightRecordsProgress", "getExportFlightRecordsProgress 成功");
                    if (results == null) {
                        log("ERROR", "handleGetExportFlightRecordsProgress", "results is null");
                        return;
                    }
                    // 打印分页信息
                    com.dji.flighthubsdk.bean.CommonPaginationData pagination = results.getPagination();
                    if (pagination != null) {
                        log("INFO", "handleGetExportFlightRecordsProgress", "分页信息:");
                        log("INFO", "handleGetExportFlightRecordsProgress", "  => page:      " + pagination.getPage());
                        log("INFO", "handleGetExportFlightRecordsProgress", "  => page_size: " + pagination.getPageSize());
                        log("INFO", "handleGetExportFlightRecordsProgress", "  => total:     " + pagination.getTotal());
                    }
                    // 打印导出进度列表
                    java.util.List<com.dji.flighthubsdk.bean.FlightRecordExportProgressItemData> list = results.getList();
                    if (list == null) {
                        log("INFO", "handleGetExportFlightRecordsProgress", "list is null");
                        return;
                    }
                    log("INFO", "handleGetExportFlightRecordsProgress", "导出记录数量: " + list.size());
                    for (int i = 0; i < list.size(); i++) {
                        com.dji.flighthubsdk.bean.FlightRecordExportProgressItemData item = list.get(i);
                        log("INFO", "handleGetExportFlightRecordsProgress", "  [" + i + "]:");
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => id:                 " + item.getId());
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => export_id:          " + item.getExportId());
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => content_type:       " + item.getContentType());
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => status:             " + item.getStatus()
                            + " (0:导出中 1:完成 2:失败 3:已取消)");
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => progress:           " + item.getProgress() + "%");
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => file_name:          " + item.getFileName());
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => created_at:         " + item.getCreatedAt());
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => user_id:            " + item.getUserId());
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => download_url:       " + item.getDownloadUrl());
                        log("INFO", "handleGetExportFlightRecordsProgress", "    => failed_reason_code: " + item.getFailedReasonCode());
                    }
                }
            });

        waitForKeyPress();
    }

    // [280] PSDK 探照灯 - 工作模式设置（0=关闭 1=开启 2=爆闪）
    private static void handlePsdkSearchlightModeSet() {
        printSeparator("PSDK 探照灯 - 工作模式设置");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSearchlightModeSet", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSearchlightModeSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handlePsdkSearchlightModeSet", "请输入模式 0=关闭 1=开启 2=爆闪(默认1):");
        String line = scanner.nextLine();
        int mode = 1;
        try {
            if (line != null && !line.trim().isEmpty()) mode = Integer.parseInt(line.trim());
        } catch (NumberFormatException ignored) {}
        final int finalMode = mode;
        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSearchlightModeSet", "没有找到PSDK设备");
                    return;
                }
                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                drone.getPsdkControlModule().searchlightModeSet(psdkIndex, finalMode, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handlePsdkSearchlightModeSet", "DrcSearchlightModeSet 结果: " + error);
                    }
                });
            }
        });
        waitForKeyPress();
    }

    // [281] PSDK 探照灯 - 亮度设置（0~100）
    private static void handlePsdkSearchlightBrightnessSet() {
        printSeparator("PSDK 探照灯 - 亮度设置");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSearchlightBrightnessSet", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSearchlightBrightnessSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handlePsdkSearchlightBrightnessSet", "请输入亮度 0~100(默认50):");
        String line = scanner.nextLine();
        int brightness = 50;
        try {
            if (line != null && !line.trim().isEmpty()) brightness = Integer.parseInt(line.trim());
        } catch (NumberFormatException ignored) {}
        final int finalBrightness = brightness;
        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSearchlightBrightnessSet", "没有找到PSDK设备");
                    return;
                }
                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                drone.getPsdkControlModule().searchlightBrightnessSet(psdkIndex, finalBrightness, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handlePsdkSearchlightBrightnessSet", "DrcSearchlightBrightnessSet 结果: " + error);
                    }
                });
            }
        });
        waitForKeyPress();
    }

    // [282] PSDK 探照灯 - 云台自动校准
    private static void handlePsdkSearchlightCalibration() {
        printSeparator("PSDK 探照灯 - 云台自动校准");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSearchlightCalibration", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSearchlightCalibration", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSearchlightCalibration", "没有找到PSDK设备");
                    return;
                }
                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                drone.getPsdkControlModule().searchlightCalibration(psdkIndex, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handlePsdkSearchlightCalibration", "DrcSearchlightCalibration 结果: " + error);
                    }
                });
            }
        });
        waitForKeyPress();
    }

    // [283] PSDK 探照灯 - 云台微调（position: 0=左 1=右, value: -100~100）
    private static void handlePsdkSearchlightFineTuningSet() {
        printSeparator("PSDK 探照灯 - 云台微调");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSearchlightFineTuningSet", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSearchlightFineTuningSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handlePsdkSearchlightFineTuningSet", "请输入 position(0=左 1=右, 默认0):");
        String posLine = scanner.nextLine();
        int position = 0;
        try {
            if (posLine != null && !posLine.trim().isEmpty()) position = Integer.parseInt(posLine.trim());
        } catch (NumberFormatException ignored) {}
        log("INFO", "handlePsdkSearchlightFineTuningSet", "请输入 value(-100~100, 默认0.0):");
        String valLine = scanner.nextLine();
        double value = 0.0;
        try {
            if (valLine != null && !valLine.trim().isEmpty()) value = Double.parseDouble(valLine.trim());
        } catch (NumberFormatException ignored) {}
        log("INFO", "handlePsdkSearchlightFineTuningSet", "是否保存 saved(0=否 1=是, 默认1):");
        String savedLine = scanner.nextLine();
        boolean saved = true;
        try {
            if (savedLine != null && !savedLine.trim().isEmpty()) saved = Integer.parseInt(savedLine.trim()) != 0;
        } catch (NumberFormatException ignored) {}
        final int finalPosition = position;
        final double finalValue = value;
        final boolean finalSaved = saved;
        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSearchlightFineTuningSet", "没有找到PSDK设备");
                    return;
                }
                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                drone.getPsdkControlModule().searchlightFineTuningSet(psdkIndex, finalPosition, finalValue, finalSaved, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handlePsdkSearchlightFineTuningSet", "DrcSearchlightFineTuningSet 结果: " + error);
                    }
                });
            }
        });
        waitForKeyPress();
    }

    // [284] PSDK 探照灯 - 广视野照明开关（0=关 1=开）
    private static void handlePsdkSearchlightWideFieldModeSet() {
        printSeparator("PSDK 探照灯 - 广视野照明开关");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSearchlightWideFieldModeSet", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSearchlightWideFieldModeSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handlePsdkSearchlightWideFieldModeSet", "请输入 wide_field_mode(0=关 1=开, 默认1):");
        String line = scanner.nextLine();
        int wideFieldMode = 1;
        try {
            if (line != null && !line.trim().isEmpty()) wideFieldMode = Integer.parseInt(line.trim());
        } catch (NumberFormatException ignored) {}
        final int finalMode = wideFieldMode;
        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSearchlightWideFieldModeSet", "没有找到PSDK设备");
                    return;
                }
                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                drone.getPsdkControlModule().searchlightWideFieldModeSet(psdkIndex, finalMode, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handlePsdkSearchlightWideFieldModeSet", "DrcSearchlightWideFieldModeSet 结果: " + error);
                    }
                });
            }
        });
        waitForKeyPress();
    }

    // [285] PSDK 探照灯 - 云台联动开关（0=关 1=开）
    private static void handlePsdkSearchlightLightGimbalControlSet() {
        printSeparator("PSDK 探照灯 - 云台联动开关");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSearchlightLightGimbalControlSet", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSearchlightLightGimbalControlSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handlePsdkSearchlightLightGimbalControlSet", "请输入 light_gimbal_control(0=关 1=开, 默认1):");
        String line = scanner.nextLine();
        int ctrl = 1;
        try {
            if (line != null && !line.trim().isEmpty()) ctrl = Integer.parseInt(line.trim());
        } catch (NumberFormatException ignored) {}
        final int finalCtrl = ctrl;
        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSearchlightLightGimbalControlSet", "没有找到PSDK设备");
                    return;
                }
                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                drone.getPsdkControlModule().searchlightLightGimbalControlSet(psdkIndex, finalCtrl, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handlePsdkSearchlightLightGimbalControlSet", "DrcSearchlightLightGimbalControlSet 结果: " + error);
                    }
                });
            }
        });
        waitForKeyPress();
    }

    // [286] PSDK 探照灯 - 灯光类型（0=近光 1=远光 2=全开）
    private static void handlePsdkSearchlightTypeSet() {
        printSeparator("PSDK 探照灯 - 灯光类型设置");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handlePsdkSearchlightTypeSet", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handlePsdkSearchlightTypeSet", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        log("INFO", "handlePsdkSearchlightTypeSet", "请输入 type(0=近光 1=远光 2=全开, 默认2):");
        String line = scanner.nextLine();
        int type = 2;
        try {
            if (line != null && !line.trim().isEmpty()) type = Integer.parseInt(line.trim());
        } catch (NumberFormatException ignored) {}
        final int finalType = type;
        drone.getPsdkControlModule().listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList psdkDeviceList) {
                if (psdkDeviceList.getDeviceInfos().isEmpty()) {
                    log("WARN", "handlePsdkSearchlightTypeSet", "没有找到PSDK设备");
                    return;
                }
                int psdkIndex = psdkDeviceList.getDeviceInfos().get(0).getPsdkIndex();
                drone.getPsdkControlModule().searchlightTypeSet(psdkIndex, finalType, new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handlePsdkSearchlightTypeSet", "DrcSearchlightTypeSet 结果: " + error);
                    }
                });
            }
        });
        waitForKeyPress();
    }

    // ==================== 飞行安全配置 ====================

    private static void handleSetFlightSmartPlanMode() {
        printSeparator("设置智能规划模式");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetFlightSmartPlanMode", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetFlightSmartPlanMode", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入智能规划模式 (0=启用, 1=关闭): ");
        int mode = Integer.parseInt(scanner.nextLine().trim());
        log("INFO", "handleSetFlightSmartPlanMode", "设置智能规划模式: " + mode);
        drone.getFlightSettingsModule().setFlightSmartPlanMode(mode, new FlightHubCallback.SetFlightSmartPlanModeCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlightSmartPlanModeResult result) {
                log("INFO", "handleSetFlightSmartPlanMode", "结果: " + error
                        + ", mode=" + result.getSmartPlanningMode()
                        + ", sync=" + result.getDeviceShadowSync().getStatus());
            }
        });
        waitForKeyPress();
    }

    private static void handleSetOutOfControlAction() {
        printSeparator("设置失控动作");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetOutOfControlAction", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetOutOfControlAction", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入失控动作 (Hover / ReturnHome / Continue): ");
        String action = scanner.nextLine().trim();
        log("INFO", "handleSetOutOfControlAction", "设置失控动作: " + action);
        drone.getFlightSettingsModule().setOutOfControlAction(action, new FlightHubCallback.SetOutOfControlActionCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlightOutOfControlActionResult result) {
                log("INFO", "handleSetOutOfControlAction", "结果: " + error
                        + ", action=" + result.getOutOfControlAction()
                        + ", sync=" + result.getDeviceShadowSync().getStatus());
            }
        });
        waitForKeyPress();
    }

    private static void handleSetFlightTaskHeight() {
        printSeparator("设置飞行作业高度");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetFlightTaskHeight", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetFlightTaskHeight", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入飞行作业高度 (米, 0-3000): ");
        int height = Integer.parseInt(scanner.nextLine().trim());
        log("INFO", "handleSetFlightTaskHeight", "设置飞行作业高度: " + height + "m");
        drone.getFlightSettingsModule().setFlightTaskHeight(height, new FlightHubCallback.SetFlightTaskHeightCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlightCommanderHeightResult result) {
                log("INFO", "handleSetFlightTaskHeight", "结果: " + error
                        + ", height=" + result.getCommanderFlightHeight()
                        + ", sync=" + result.getDeviceShadowSync().getStatus());
            }
        });
        waitForKeyPress();
    }

    private static void handleSetRthAltitude() {
        printSeparator("设置返航高度");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleSetRthAltitude", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleSetRthAltitude", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入返航高度 (米, 20-1500): ");
        int altitude = Integer.parseInt(scanner.nextLine().trim());
        log("INFO", "handleSetRthAltitude", "设置返航高度: " + altitude + "m");
        drone.getFlightSettingsModule().setRthAltitude(altitude, new FlightHubCallback.SetRthAltitudeCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.FlightRthAltitudeResult result) {
                log("INFO", "handleSetRthAltitude", "结果: " + error
                        + ", altitude=" + result.getRthAltitude()
                        + ", sync=" + result.getDeviceShadowSync().getStatus());
            }
        });
        waitForKeyPress();
    }

    private static void handleSetAirTransferEnable() {
        printSeparator("设置空中回传开关");
        if (selectedDevice == null) {
            log("WARN", "handleSetAirTransferEnable", "未选择设备");
            waitForKeyPress();
            return;
        }
        String subDeviceSn = getSubDeviceSn(selectedDevice);
        drone = getOrCreateDrone(subDeviceSn);  
        if (drone == null) {
            log("ERROR", "handleSetAirTransferEnable", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        System.out.print("照片空中回传 (1=开启, 0=关闭): ");
        boolean enabled = "1".equals(scanner.nextLine().trim());
        log("INFO", "handleSetAirTransferEnable", "设置空中回传: " + (enabled ? "开启" : "关闭") + ", droneSn=" + subDeviceSn);
        drone.getFlightSettingsModule().setAirTransferEnable(enabled, error -> {
            log("INFO", "handleSetAirTransferEnable", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleUpdateTargetPoints() {
        printSeparator("更新指点飞行目标点");
        if (!hasSubDevice(selectedDevice)) {
            log("WARN", "handleUpdateTargetPoints", "该设备没有无人机");
            waitForKeyPress();
            return;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", "handleUpdateTargetPoints", "获取无人机实例失败");
            waitForKeyPress();
            return;
        }
        FlyToPointModule flyToModule = drone.getFlyToPointModule();
        if (flyToModule == null) {
            log("ERROR", "handleUpdateTargetPoints", "获取FlyToPointModule失败");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入 fly_to_id: ");
        String flyToId = scanner.nextLine().trim();
        System.out.print("请输入起始点纬度: ");
        double startLat = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入起始点经度: ");
        double startLng = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入起始点高度(米): ");
        double startH = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入目标点纬度: ");
        double targetLat = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入目标点经度: ");
        double targetLng = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入目标点高度(米): ");
        double targetH = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入最大速度(m/s): ");
        int maxSpeed = Integer.parseInt(scanner.nextLine().trim());

        com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams params = new com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams();
        params.setFlyToId(flyToId);
        params.setMaxSpeed(maxSpeed);
        com.dji.flighthubsdk.bean.FlyToTargetPoint startPoint = new com.dji.flighthubsdk.bean.FlyToTargetPoint();
        startPoint.setLatitude(startLat);
        startPoint.setLongitude(startLng);
        startPoint.setHeight(startH);
        params.setStartPoint(startPoint);
        com.dji.flighthubsdk.bean.FlyToTargetPoint targetPoint = new com.dji.flighthubsdk.bean.FlyToTargetPoint();
        targetPoint.setLatitude(targetLat);
        targetPoint.setLongitude(targetLng);
        targetPoint.setHeight(targetH);
        params.setPoints(java.util.Arrays.asList(targetPoint));

        log("INFO", "handleUpdateTargetPoints", "更新目标点, flyToId=" + flyToId + ", maxSpeed=" + maxSpeed);
        flyToModule.updateTargetPoints(params, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                log("INFO", "handleUpdateTargetPoints", "结果: " + error);
            }
        });
        waitForKeyPress();
    }

    // ============================================================
    // ===================== Module 访问辅助方法 ====================
    // ============================================================

    private static FlightTaskModule requireFlightTaskModule(String tag) {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            log("ERROR", tag, "获取工作空间失败");
            return null;
        }
        FlightTaskModule module = ws.getFlightTaskModule();
        if (module == null) {
            log("ERROR", tag, "获取航线任务模块失败");
        }
        return module;
    }

    private static AIBoxModule requireAIBoxModule(String tag) {
        if (selectedDevice == null) {
            log("ERROR", tag, "未选择设备");
            return null;
        }
        drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        if (drone == null) {
            log("ERROR", tag, "获取 drone 失败");
            return null;
        }
        AIBoxModule module = drone.getAIBoxModule();
        if (module == null) {
            log("ERROR", tag, "获取 AIBoxModule 失败");
        }
        return module;
    }

    private static TerraBuildModule getTerraBuildModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            log("ERROR", "Terra", "获取 workspace 失败");
            return null;
        }
        TerraBuildModule module = ws.getTerraBuildModule();
        if (module == null) {
            log("ERROR", "Terra", "获取 TerraBuildModule 失败");
        }
        return module;
    }

    // ============================================================
    // ===================== Terra Build 测试方法 ==================
    // ============================================================

    // [400] 获取模型文件列表
    private static void handleTerraBuildGetModelFileList() {
        printSeparator("[Terra Build] 获取模型文件列表");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getModelFileList(1, 20, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "getModelFileList 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "total=" + result.getTotal());
                java.util.List<com.dji.flighthubsdk.bean.TerraBuildModelFileItem> list = result.getList();
                if (list != null) for (com.dji.flighthubsdk.bean.TerraBuildModelFileItem item : list) {
                    log("INFO", "Terra", "  id=" + item.getId() + " uuid=" + item.getUuid()
                            + " name=" + item.getName() + " state=" + item.getState());
                    String zip = item.getZipUrl();
                    if (zip != null && !zip.isEmpty())
                        log("INFO", "Terra", "  zip_url=" + zip.substring(0, Math.min(60, zip.length())) + "...");
                }
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [401] 获取模型文件签名 URL
    private static void handleTerraBuildGetModelFileUrl() {
        printSeparator("[Terra Build] 获取模型文件签名 URL");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        int fileId = (int) ConfigManager.getConfig().getTerraBuildFileId();
        log("INFO", "Terra", "file_id=" + fileId);
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getModelFileUrl(fileId, 1, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS)
                log("ERROR", "Terra", "getModelFileUrl 失败: " + err.toString());
            else log("INFO", "Terra", "url=" + result.getUrl());
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [402] 获取重建设置
    private static void handleTerraBuildGetReconstructionSettings() {
        printSeparator("[Terra Build] 获取重建设置");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        String fileUuid = ConfigManager.getConfig().getTerraBuildFileUuid();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getReconstructionSettings(fileUuid, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "getReconstructionSettings 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "state=" + result.getState()
                        + " media_input_object_key=" + result.getMediaInputObjectKey());
                com.dji.flighthubsdk.bean.TerraBuildSettingsDetail s = result.getReconstructionSettings();
                if (s != null) log("INFO", "Terra", "  name=" + s.getName()
                        + " quality_level=" + s.getQualityLevel() + " wkt=" + s.getWkt());
                java.util.List<com.dji.flighthubsdk.bean.TerraBuildFolderInfo> folders = result.getFolderList();
                if (folders != null) for (com.dji.flighthubsdk.bean.TerraBuildFolderInfo f : folders) {
                    log("INFO", "Terra", "  folder: id=" + f.getFolderId()
                            + " uuid=" + f.getFolderUuid() + " name=" + f.getFolderName());
                }
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [403] 获取质量报告
    private static void handleTerraBuildGetQualityReport() {
        printSeparator("[Terra Build] 获取质量报告");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        String fileUuid = ConfigManager.getConfig().getTerraBuildFileUuid();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getQualityReport(fileUuid, "zh", "metric", (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS)
                log("ERROR", "Terra", "getQualityReport 失败: " + err.toString());
            else log("INFO", "Terra", "quality_report_url=" + result.getQualityReportUrl());
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [404] 创建重建任务 — 2D 可见光（使用 params 对象方式）
    private static void handleTerraBuildCreateReconstruction2D() {
        printSeparator("[Terra Build] 创建重建任务（2D 可见光，bean 对象方式）");
        TerraBuildModule module = getTerraBuildModule();
        if (module == null) return;
        String folderUuid = ConfigManager.getConfig().getTerraBuildFolderUuid();

        // 构建顶层参数（使用 bean）
        com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams params =
                new com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams();
        params.setName("Java-2D重建测试");
        params.setReconstructionType(java.util.Arrays.asList(2));
        params.setFolderUuids(java.util.Arrays.asList(folderUuid));
        params.setQualityLevel(2);
        params.setQualityLevelSet(true);

        // 构建 Parameter2D
        com.dji.flighthubsdk.bean.TerraBuildParameter2D p2d =
                new com.dji.flighthubsdk.bean.TerraBuildParameter2D();
        p2d.setSfmMethod("GLOBAL");
        p2d.setSfmFeatureQuantityLevel(2);
        p2d.setSfmFeatureQuantityLevelSet(true);
        p2d.setUseImagePosition(true);
        p2d.setUseImagePositionSet(true);
        p2d.setGenerateBlocksExchangeXml(true);
        p2d.setGenerateBlocksExchangeXmlSet(true);

        com.dji.flighthubsdk.bean.TerraBuildGeoDesc blkGeo =
                new com.dji.flighthubsdk.bean.TerraBuildGeoDesc("GEO_CS", "EPSG:32649", "", "", null, "");
        p2d.setBlocksExchangeXmlGeoDesc(blkGeo);

        com.dji.flighthubsdk.bean.TerraBuildGeoDesc outGeo =
                new com.dji.flighthubsdk.bean.TerraBuildGeoDesc("GEO_CS", "EPSG:32649", "", "", null, "");
        p2d.setOutputGeoDesc(outGeo);

        p2d.setGenerateMaps2dTiff(true);       p2d.setGenerateMaps2dTiffSet(true);
        p2d.setGenerateMaps2dTiffCog(true);    p2d.setGenerateMaps2dTiffCogSet(true);
        p2d.setResolutionType2d(0);            p2d.setResolutionType2dSet(true);
        p2d.setQualityLevel(2);                p2d.setQualityLevelSet(true);
        p2d.setResolutionGsd2d(0.05);          p2d.setResolutionGsd2dSet(true);
        p2d.setDomTileSize(5000);              p2d.setDomTileSizeSet(true);
        p2d.setMapMode(1);                     p2d.setMapModeSet(true);
        p2d.setUseSmartFillHole(true);         p2d.setUseSmartFillHoleSet(true);

        params.setParameter2d(p2d);

        log("INFO", "Terra", "使用 bean 对象创建 2D 重建，folderUuid=" + folderUuid);
        createReconstructionWithBeanAndWait(module, params);
    }

    // [405] 创建重建任务 — 3D（验证可选参数，使用 bean 对象方式）
    private static void handleTerraBuildCreateReconstruction3D() {
        printSeparator("[Terra Build] 创建重建任务（3D 带可选参数，bean 对象方式）");
        TerraBuildModule module = getTerraBuildModule();
        if (module == null) return;
        String folderUuid = ConfigManager.getConfig().getTerraBuildFolderUuid();

        com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams params =
                new com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams();
        params.setName("Java-3D重建测试");
        params.setReconstructionType(java.util.Arrays.asList(3));
        params.setFolderUuids(java.util.Arrays.asList(folderUuid));
        params.setQualityLevel(1);                 params.setQualityLevelSet(true);
        params.setSimplifiedFactor(0.8);           params.setSimplifiedFactorSet(true);
        params.setWkt("EPSG:32649");
        params.setGenerateModelFormats(java.util.Arrays.asList(1, 2));

        com.dji.flighthubsdk.bean.TerraBuildParameter3D p3d =
                new com.dji.flighthubsdk.bean.TerraBuildParameter3D();
        p3d.setSfmMethod("GLOBAL");
        p3d.setSfmFeatureQuantityLevel(1);   p3d.setSfmFeatureQuantityLevelSet(true);
        p3d.setUseImagePosition(true);       p3d.setUseImagePositionSet(true);
        p3d.setOutputMesh(true);             p3d.setOutputMeshSet(true);
        p3d.setQualityLevel(1);              p3d.setQualityLevelSet(true);
        p3d.setGenerateB3dm(true);           p3d.setGenerateB3dmSet(true);
        p3d.setGenerateOsgb(true);           p3d.setGenerateOsgbSet(true);

        com.dji.flighthubsdk.bean.TerraBuildGeoDesc outGeo =
                new com.dji.flighthubsdk.bean.TerraBuildGeoDesc("GEO_CS", "EPSG:32649", "", "", null, "");
        outGeo.setOverrideVerticalCs("EPSG:5737");
        p3d.setOutputGeoDesc(outGeo);

        params.setParameter3d(p3d);

        log("INFO", "Terra", "使用 bean 对象创建 3D 重建，folderUuid=" + folderUuid);
        createReconstructionWithBeanAndWait(module, params);
    }

    private static void createReconstructionWithBeanAndWait(
            com.dji.flighthubsdk.core.TerraBuildModule module,
            com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams params) {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.createReconstruction(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS)
                log("ERROR", "Terra", "createReconstruction 失败: " + err.toString());
            else log("INFO", "Terra", "成功! id=" + result.getId()
                    + " uuid=" + result.getUuid() + " job_uuid=" + result.getJobUuid());
            latch.countDown();
        });
        try { latch.await(30, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [406] 停止重建任务
    private static void handleTerraBuildStopReconstruction() {
        printSeparator("[Terra Build] 停止重建任务");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        String fileUuid = ConfigManager.getConfig().getTerraBuildFileUuid();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.stopReconstruction(fileUuid, err -> {
            log("INFO", "Terra", "stopReconstruction: " + err.toString()); latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [407] 重试重建任务
    private static void handleTerraBuildRetryReconstruction() {
        printSeparator("[Terra Build] 重试重建任务");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        String fileUuid = ConfigManager.getConfig().getTerraBuildFileUuid();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.retryReconstruction(fileUuid, err -> {
            log("INFO", "Terra", "retryReconstruction: " + err.toString()); latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [409] 获取重建图像列表
    private static void handleTerraBuildGetReconstructionImages() {
        printSeparator("[Terra Build] 获取重建图像列表");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        String fileUuid = ConfigManager.getConfig().getTerraBuildFileUuid();
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getReconstructionImages(fileUuid, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS)
                log("ERROR", "Terra", "getReconstructionImages 失败: " + err.toString());
            else {
                log("INFO", "Terra", "total=" + result.getTotal());
                for (com.dji.flighthubsdk.bean.TerraBuildReconstructionImageItem img : result.getList()) {
                    log("INFO", "Terra", "  image_id=" + img.getImageId() + " file_name=" + img.getFileName());
                }
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [410] 删除模型文件
    private static void handleTerraBuildDeleteModelFile() {
        printSeparator("[Terra Build] 删除模型文件");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        int fileId = (int) ConfigManager.getConfig().getTerraBuildFileId();
        log("INFO", "Terra", "file_id=" + fileId);
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.deleteModelFile(fileId, err -> {
            log("INFO", "Terra", "deleteModelFile: " + err.toString()); latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [413] 批量删除模型文件
    private static void handleTerraBuildBatchDeleteModelFiles() {
        printSeparator("[Terra Build] 批量删除模型文件");
        TerraBuildModule module = getTerraBuildModule();
        if (module == null) return;
        int fileId = (int) ConfigManager.getConfig().getTerraBuildFileId();
        java.util.List<Integer> fileIds = java.util.Collections.singletonList(fileId);
        log("INFO", "Terra", "批量删除 file_ids=[" + fileId + "]");
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.batchDeleteModelFiles(fileIds, err -> {
            log("INFO", "Terra", "batchDeleteModelFiles: " + err.toString());
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [411] 创建全景拼接任务
    private static void handleTerraBuildCreatePanorama() {
        printSeparator("[Terra Build] 创建全景拼接任务");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        String folderUuid = ConfigManager.getConfig().getTerraBuildFolderUuid();
        java.util.List<String> uuids = java.util.Collections.singletonList(folderUuid);
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.createPanorama("Java全景拼接测试", uuids, "EPSG:32649", false, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS)
                log("ERROR", "Terra", "createPanorama 失败: " + err.toString());
            else log("INFO", "Terra", "成功! id=" + result.getId()
                    + " uuid=" + result.getUuid() + " job_uuid=" + result.getJobUuid());
            latch.countDown();
        });
        try { latch.await(30, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [412] 监听重建状态变更推送（持续，输入0退出）
    private static void handleTerraBuildListenReconstructionStatus() {
        printSeparator("[Terra Build] 监听重建状态变更推送");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        log("INFO", "Terra", "开始监听，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenReconstructionStatusChanged(msg -> {
            log("INFO", "Terra", "[推送] 重建状态变更: uuid=" + msg.getUuid()
                    + " state=" + msg.getState() + " name=" + msg.getName());
            com.dji.flighthubsdk.bean.TerraBuildJobInfo ji = msg.getJobInfo();
            if (ji != null) log("INFO", "Terra", "  job_info: type=" + ji.getType()
                    + " status=" + ji.getJobStatus() + " percent=" + ji.getPercent() + "%");
            java.util.List<com.dji.flighthubsdk.bean.TerraBuildJobInfo> jis = msg.getJobInfos();
            if (jis != null && !jis.isEmpty()) {
                for (int i = 0; i < jis.size(); i++) {
                    com.dji.flighthubsdk.bean.TerraBuildJobInfo j = jis.get(i);
                    log("INFO", "Terra", "  job_infos[" + i + "]: type=" + j.getType()
                            + " status=" + j.getJobStatus() + " percent=" + j.getPercent() + "%");
                }
            }
        });
        log("INFO", "Terra", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) {
            int input = getUserChoice(0, 999);
            if (input == 0) break;
        }
        if (listenIdHolder[0] >= 0) {
            module.cancelListenReconstructionStatusChanged(listenIdHolder[0]);
            log("INFO", "Terra", "已取消监听 listen_id=" + listenIdHolder[0]);
        }
        waitForKeyPress();
    }

    // [414] 模型上传回调
    private static void handleTerraUploadModelCallback() {
        printSeparator("[Terra Build] 模型上传回调");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;

        // 示例：从配置读取 object_key（需提前直传 OSS 后填入）
        String objectKey = ConfigManager.getConfig().getTerraObjectKey();
        String fileName  = ConfigManager.getConfig().getTerraModelFileName();
        log("INFO", "Terra", "object_key=" + objectKey + "  name=" + fileName);

        com.dji.flighthubsdk.bean.TerraUploadCallbackParams params =
                new com.dji.flighthubsdk.bean.TerraUploadCallbackParams(objectKey, fileName);
        // 可选: 上传完成后自动加载到地图
        params.setShowOnMap(true);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.uploadModelCallback(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "uploadModelCallback 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "uploadModelCallback 成功! id=" + result.getId()
                        + " uuid=" + result.getUuid() + " name=" + result.getName());
            }
            latch.countDown();
        });
        try { latch.await(30, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [415] 模型重命名
    private static void handleTerraRenameModel() {
        printSeparator("[Terra Build] 模型重命名");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;

        int fileId = (int) ConfigManager.getConfig().getTerraBuildFileId();
        log("INFO", "Terra", "file_id=" + fileId);
        System.out.print("请输入新文件名: ");
        String inputName = System.console() != null ? System.console().readLine() : "renamed_model";
        final String newName = (inputName == null || inputName.trim().isEmpty()) ? "renamed_model" : inputName.trim();

        com.dji.flighthubsdk.bean.TerraRenameModelParams params =
                new com.dji.flighthubsdk.bean.TerraRenameModelParams(newName);
        // 可选: 设置等高模式
        // params.setInUniformHeight(true);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.renameModel(fileId, params, (err) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "renameModel 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "renameModel 成功! file_id=" + fileId + " new_name=" + newName);
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [416] 加载模型到地图
    private static void handleTerraShowModelOnMap() {
        printSeparator("[Terra Build] 加载模型到地图");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;

        long fileId = ConfigManager.getConfig().getTerraBuildFileId();
        log("INFO", "Terra", "file_id=" + fileId + "  操作: 加载到地图");

        // TerraShowOnMapParams.ids 为 List<Integer>；file ID 在实际业务中不会超过 int 范围，此处明确转换
        int fileIdInt = (int) fileId;
        com.dji.flighthubsdk.bean.TerraShowOnMapParams params =
                new com.dji.flighthubsdk.bean.TerraShowOnMapParams(
                        java.util.Collections.singletonList(fileIdInt), true);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.showModelOnMap(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "showModelOnMap 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "showModelOnMap 成功! operated_ids=" + result.getIds());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [4160] 取消加载模型到地图
    private static void handleTerraHideModelOnMap() {
        printSeparator("[Terra Build] 取消加载模型到地图");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;

        long fileId = ConfigManager.getConfig().getTerraBuildFileId();
        log("INFO", "Terra", "file_id=" + fileId + "  操作: 取消加载");

        int fileIdInt = (int) fileId;
        com.dji.flighthubsdk.bean.TerraShowOnMapParams params =
                new com.dji.flighthubsdk.bean.TerraShowOnMapParams(
                        java.util.Collections.singletonList(fileIdInt), false);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.showModelOnMap(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "hideModelOnMap 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "hideModelOnMap 成功! operated_ids=" + result.getIds());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [417] 监听模型解压进度推送
    private static void handleTerraListenUnzipProgress() {
        printSeparator("[Terra Build] 监听模型解压进度推送");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        log("INFO", "Terra", "开始监听解压进度，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenTerraUnzipProgress(msg -> {
            log("INFO", "Terra", "[推送] 解压进度: file_id=" + msg.getFileId()
                    + " name=" + msg.getName() + " percent=" + msg.getPercent() + "%");
        });
        log("INFO", "Terra", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) {
            int input = getUserChoice(0, 999);
            if (input == 0) break;
        }
        if (listenIdHolder[0] >= 0) {
            module.cancelListenTerraUnzipProgress(listenIdHolder[0]);
            log("INFO", "Terra", "已取消监听 listen_id=" + listenIdHolder[0]);
        }
        waitForKeyPress();
    }

    // [418] 监听模型状态变更推送
    private static void handleTerraListenModelStatusChanged() {
        printSeparator("[Terra Build] 监听模型状态变更推送");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        log("INFO", "Terra", "开始监听模型状态变更，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenTerraModelStatusChanged(msg -> {
            log("INFO", "Terra", "[推送] 模型状态变更: file_id=" + msg.getFileId()
                    + " uuid=" + msg.getUuid() + " name=" + msg.getName()
                    + " state=" + msg.getState()
                    + " fileType=" + msg.getFileType() + " subFileType=" + msg.getSubFileType()
                    + (msg.getErrorCode() != 0 ? " errorCode=" + msg.getErrorCode() : ""));
        });
        log("INFO", "Terra", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) {
            int input = getUserChoice(0, 999);
            if (input == 0) break;
        }
        if (listenIdHolder[0] >= 0) {
            module.cancelListenTerraModelStatusChanged(listenIdHolder[0]);
            log("INFO", "Terra", "已取消监听 listen_id=" + listenIdHolder[0]);
        }
        waitForKeyPress();
    }

    // [419] 监听文件展示地图推送
    private static void handleTerraListenFileShowOnMap() {
        printSeparator("[Terra Build] 监听模型展示地图推送 (topic_model_file_show_on_map)");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        log("INFO", "Terra", "开始监听，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenTerraFileShowOnMap(msg -> {
            log("INFO", "Terra", "[推送] topic_model_file_show_on_map  count=" + msg.getList().size());
            for (com.dji.flighthubsdk.bean.TerraFileShowOnMapItem item : msg.getList()) {
                log("INFO", "Terra", "  ├─ id="          + item.getId()
                        + "  name="        + item.getName()
                        + "  fileType="    + item.getFileType()
                        + "  subFileType=" + item.getSubFileType()
                        + "  previewUrl="  + (item.getPreviewUrl() != null && !item.getPreviewUrl().isEmpty()
                                             ? item.getPreviewUrl().substring(0, Math.min(60, item.getPreviewUrl().length())) + "..."
                                             : "(empty)"));
            }
        });
        log("INFO", "Terra", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) {
            int input = getUserChoice(0, 999);
            if (input == 0) break;
        }
        if (listenIdHolder[0] >= 0) {
            module.cancelListenTerraFileShowOnMap(listenIdHolder[0]);
            log("INFO", "Terra", "已取消监听 listen_id=" + listenIdHolder[0]);
        }
        waitForKeyPress();
    }

    // [4190] 监听模型隐藏地图推送 topic_media_file_hide_on_map
    private static void handleTerraListenMediaFileHideOnMap() {
        printSeparator("[Terra Build] 监听模型隐藏地图推送 (topic_media_file_hide_on_map)");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        log("INFO", "Terra", "开始监听，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenTerraMediaFileHideOnMap(msg -> {
            log("INFO", "Terra", "[推送] topic_media_file_hide_on_map  ids=" + msg.getIds()
                    + "  count=" + msg.getIds().size());
        });
        log("INFO", "Terra", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) { if (getUserChoice(0, 999) == 0) break; }
        if (listenIdHolder[0] >= 0) {
            module.cancelListenTerraMediaFileHideOnMap(listenIdHolder[0]);
            log("INFO", "Terra", "已取消监听 listen_id=" + listenIdHolder[0]);
        }
        waitForKeyPress();
    }

    // [4191] 监听模型显示地图推送 topic_media_file_show_on_map
    private static void handleTerraListenMediaFileShowOnMap() {
        printSeparator("[Terra Build] 监听模型显示地图推送 (topic_media_file_show_on_map)");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;
        log("INFO", "Terra", "开始监听，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenTerraMediaFileShowOnMap(msg -> {
            log("INFO", "Terra", "[推送] topic_media_file_show_on_map  count=" + msg.getList().size());
            for (com.dji.flighthubsdk.bean.TerraMediaFileShowOnMapItem item : msg.getList()) {
                log("INFO", "Terra", "  ├─ id="          + item.getId()
                        + "  name="        + item.getName()
                        + "  fileType="    + item.getFileType()
                        + "  subFileType=" + item.getSubFileType());
                log("INFO", "Terra", "  │  previewUrl="  + (item.getPreviewUrl() != null && !item.getPreviewUrl().isEmpty()
                                                            ? item.getPreviewUrl().substring(0, Math.min(60, item.getPreviewUrl().length())) + "..."
                                                            : "(empty)"));
                log("INFO", "Terra", "  │  shootPos: lat=" + item.getShootLat()
                        + "  lng="     + item.getShootLng()
                        + "  absAlt="  + item.getShootAbsAlt()
                        + "  relAlt="  + item.getShootRelAlt());
                log("INFO", "Terra", "  │  photoPos: lat=" + item.getPhotoLat()
                        + "  lng="     + item.getPhotoLng()
                        + "  absAlt="  + item.getPhotoAbsAlt()
                        + "  relAlt="  + item.getPhotoRelAlt());
            }
        });
        log("INFO", "Terra", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) { if (getUserChoice(0, 999) == 0) break; }
        if (listenIdHolder[0] >= 0) {
            module.cancelListenTerraMediaFileShowOnMap(listenIdHolder[0]);
            log("INFO", "Terra", "已取消监听 listen_id=" + listenIdHolder[0]);
        }
        waitForKeyPress();
    }
    private static void handleTerraGetModelDetail() {
        printSeparator("[Terra Build] 获取模型文件详情");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;

        int fileId = (int) ConfigManager.getConfig().getTerraBuildFileId();
        log("INFO", "Terra", "file_id=" + fileId);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getModelDetail(fileId, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "getModelDetail 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "getModelDetail 成功!");
                log("INFO", "Terra", "  id=" + result.getId() + "  uuid=" + result.getUuid());
                log("INFO", "Terra", "  name=" + result.getName());
                log("INFO", "Terra", "  fileType=" + result.getFileType() + "  subFileType=" + result.getSubFileType());
                log("INFO", "Terra", "  state=" + result.getState() + "  showOnMap=" + result.isShowOnMap());
                log("INFO", "Terra", "  size=" + result.getSize() + " bytes");
                if (result.getSelectedWkt() != null && !result.getSelectedWkt().isEmpty())
                    log("INFO", "Terra", "  selectedWkt=" + result.getSelectedWkt());
                if (result.getUrl() != null && !result.getUrl().isEmpty())
                    log("INFO", "Terra", "  url=" + result.getUrl().substring(0, Math.min(80, result.getUrl().length())) + "...");
                if (result.getIndexFileKey() != null && !result.getIndexFileKey().isEmpty())
                    log("INFO", "Terra", "  indexFileKey=" + result.getIndexFileKey());
                if (result.getLatitude() != 0.0 || result.getLongitude() != 0.0)
                    log("INFO", "Terra", "  center=(" + result.getLatitude() + ", " + result.getLongitude() + ")  alt=" + result.getAltitude());
                if (result.getMaxLevel() != 0)
                    log("INFO", "Terra", "  tileLevel=[" + result.getMinLevel() + ", " + result.getMaxLevel() + "]");
                if (!result.getFileTags().isEmpty())
                    log("INFO", "Terra", "  fileTags=" + result.getFileTags());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [421] 设置 LAS 坐标系并触发格式转换
    private static void handleTerraSetLasWkt() {
        printSeparator("[Terra Build] 设置LAS坐标系并触发格式转换");
        TerraBuildModule module = getTerraBuildModule(); if (module == null) return;

        String fileUuid = ConfigManager.getConfig().getTerraBuildFileUuid();
        log("INFO", "Terra", "file_uuid=" + fileUuid);
        System.out.print("请输入坐标系（如 EPSG:4326 或 WKT 字符串）: ");
        String rawInput = System.console() != null ? System.console().readLine() : "EPSG:4326";
        final String wkt = (rawInput == null || rawInput.trim().isEmpty()) ? "EPSG:4326" : rawInput.trim();
        log("INFO", "Terra", "wkt=" + wkt);

        com.dji.flighthubsdk.bean.TerraSetLasWktParams params =
                new com.dji.flighthubsdk.bean.TerraSetLasWktParams(fileUuid, wkt);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.setLasWkt(params, (err) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Terra", "setLasWkt 失败: " + err.toString());
            } else {
                log("INFO", "Terra", "setLasWkt 成功! 格式转换已触发 file_uuid=" + fileUuid + " wkt=" + wkt);
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    private static com.dji.flighthubsdk.core.MediaServiceModule getMediaServiceModule() {
        com.dji.flighthubsdk.core.Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) { log("ERROR", "Media", "获取 workspace 失败"); return null; }
        com.dji.flighthubsdk.core.MediaServiceModule m = ws.getMediaServiceModule();
        if (m == null) log("ERROR", "Media", "获取 MediaServiceModule 失败");
        return m;
    }

    // [302] 获取媒体上传 STS 凭证
    private static void handleMediaUploadGetSts() {
        printSeparator("[媒体服务] 获取媒体上传 STS 凭证");
        com.dji.flighthubsdk.core.MediaServiceModule module = getMediaServiceModule();
        if (module == null) return;

        String folderUuid = ConfigManager.getConfig().getTerraBuildFolderUuid();
        com.dji.flighthubsdk.bean.GetMediaUploadStsParams params =
                new com.dji.flighthubsdk.bean.GetMediaUploadStsParams(
                        "project/" + folderUuid + "/media/", 3600, "", 0);
        log("INFO", "Media", "file_path=" + params.getFilePath());

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getMediaUploadSts(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Media", "getMediaUploadSts 失败: " + err.toString());
            } else {
                log("INFO", "Media", "getMediaUploadSts 成功!");
                log("INFO", "Media", "  bucket=" + result.getBucket());
                log("INFO", "Media", "  object_key_prefix=" + result.getObjectKeyPrefix());
                log("INFO", "Media", "  endpoint=" + result.getEndpoint());
                log("INFO", "Media", "  provider=" + result.getProvider());
                log("INFO", "Media", "  region=" + result.getRegion());
                com.dji.flighthubsdk.bean.MediaUploadStsCredentials cred = result.getCredentials();
                if (cred != null) {
                    log("INFO", "Media", "  credentials.access_key_id=" + cred.getAccessKeyId());
                    log("INFO", "Media", "  credentials.expire=" + cred.getExpire());
                    String token = cred.getSecurityToken();
                    if (token != null && !token.isEmpty())
                        log("INFO", "Media", "  credentials.security_token=" + token.substring(0, Math.min(20, token.length())) + "...");
                }
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [303] 媒体上传完成回调
    private static void handleMediaUploadCallback() {
        printSeparator("[媒体服务] 媒体上传完成回调");
        com.dji.flighthubsdk.core.MediaServiceModule module = getMediaServiceModule();
        if (module == null) return;

        String folderUuid = ConfigManager.getConfig().getTerraBuildFolderUuid();
        com.dji.flighthubsdk.bean.MediaUploadCallbackFileExt ext =
                new com.dji.flighthubsdk.bean.MediaUploadCallbackFileExt(
                        "", "", folderUuid, "", "", true, "", "", "", 0, 0);
        com.dji.flighthubsdk.bean.MediaUploadCallbackFile file =
                new com.dji.flighthubsdk.bean.MediaUploadCallbackFile(
                        "690d4a85-1363-4c22-9a6f-a2abf6108b2e/a951dde1-4ded-4b07-b37b-a20a5fe4e6cd/DJI_202602271902_052_a951dde1-4ded-4b07-b37b-a20a5fe4e6cd/DJI_20260227190345_0006_V.jpeg",
                        "", "DJI_20260227190345_0006_V.jpeg", 0, "", ext, null, "", "web");
        com.dji.flighthubsdk.bean.MediaUploadCallbackParams params =
                new com.dji.flighthubsdk.bean.MediaUploadCallbackParams(
                        "", 0, 0, file, 1, 1, 0, "file_upload_callback", "1.0");

        log("INFO", "Media", "object_key=" + file.getObjectKey());

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.mediaUploadCallback(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Media", "mediaUploadCallback 失败: " + err.toString());
            } else {
                log("INFO", "Media", "mediaUploadCallback 成功!");
                log("INFO", "Media", "  id=" + result.getId());
                log("INFO", "Media", "  uuid=" + result.getUuid());
                log("INFO", "Media", "  media_id=" + result.getMediaId());
                log("INFO", "Media", "  object_key=" + result.getObjectKey());
                log("INFO", "Media", "  filename=" + result.getFilename());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [304] 图片备注上传回调
    private static void handleUploadGeoMediaFile() {
        printSeparator("[Media] 图片备注上传回调");
        MediaServiceModule module = getMediaServiceModule(); if (module == null) return;

        // 先通过 STS 直传 OSS，再调此接口注册文件
        String objectKey = ConfigManager.getConfig().getMediaObjectKey();
        String fileName  = "E5997759-B0F2-D64B-A95E-EFA40FB14D16.png";
        log("INFO", "Media", "object_key=" + objectKey + "  name=" + fileName);

        com.dji.flighthubsdk.bean.UploadGeoMediaFileParams params =
                new com.dji.flighthubsdk.bean.UploadGeoMediaFileParams(fileName, objectKey);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.uploadGeoMediaFile(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Media", "图片备注上传失败: " + err.toString());
            } else {
                log("INFO", "Media", "图片备注上传成功!");
                log("INFO", "Media", "  file_id=" + result.getFileId()
                        + "  file_uuid=" + result.getFileUuid()
                        + "  media_file_id=" + result.getMediaFileId());
                log("INFO", "Media", "  → 可用 [305] 轮询 file_id=" + result.getFileId() + " 的下载 URL");
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [305] 图片备注轮询文件 URL
    private static void handleGetMediaFileDetailById() {
        printSeparator("[Media] 图片备注轮询文件 URL");
        MediaServiceModule module = getMediaServiceModule(); if (module == null) return;

        int fileId = (int) ConfigManager.getConfig().getMediaFileId();
        log("INFO", "Media", "轮询 file_id=" + fileId
                + "（通过 [304] 上传回调返回的 file_id 填入 config.mediaFileId）");

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.getMediaFileDetailById(fileId, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Media", "获取文件详情失败: " + err.toString());
            } else {
                log("INFO", "Media", "获取文件详情成功!");
                log("INFO", "Media", "  name=" + result.getName()
                        + "  suffix=" + result.getSuffix()
                        + "  size=" + result.getSize() + " bytes");
                log("INFO", "Media", "  createAt=" + result.getCreateAt());
                log("INFO", "Media", "  url=" + (result.getUrl() != null && !result.getUrl().isEmpty()
                        ? result.getUrl().substring(0, Math.min(80, result.getUrl().length())) + "..."
                        : "(empty — 文件可能仍在处理中，稍后重试)"));
                if (result.getThumbnailUrl() != null && !result.getThumbnailUrl().isEmpty()) {
                    log("INFO", "Media", "  thumbnailUrl=" + result.getThumbnailUrl().substring(0, Math.min(80, result.getThumbnailUrl().length())) + "...");
                }
                log("INFO", "Media", "  ext: width=" + result.getExtWidth()
                        + "  height=" + result.getExtHeight());
                if (result.getPhotoPosition() != null) {
                    log("INFO", "Media", "  photoPos: lat=" + result.getPhotoPosition().getLat()
                            + "  lng=" + result.getPhotoPosition().getLng()
                            + "  absAlt=" + result.getPhotoPosition().getAbsAlt());
                }
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [306] 获取航线编辑器 STS 凭证
    private static void handleGetWaylineEditorSts() {
        printSeparator("[Wayline] 获取航线编辑器 STS 凭证");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetWaylineEditorSts", "获取 workspace 失败");
            waitForKeyPress();
            return;
        }

        com.dji.flighthubsdk.bean.GetWaylineEditorStsParams params =
                new com.dji.flighthubsdk.bean.GetWaylineEditorStsParams("wayline/editor/");
        params.setExpire(3600);
        log("INFO", "Wayline", "objectKeyPrefix=" + params.getObjectKeyPrefix()
                + "  expire=" + params.getExpire());

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        workspace.getWaylineManageModule().getWaylineEditorSts(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Wayline", "获取 STS 失败: " + err.toString());
            } else {
                log("INFO", "Wayline", "获取 STS 成功!");
                log("INFO", "Wayline", "  bucket=" + result.getBucket());
                log("INFO", "Wayline", "  endpoint=" + result.getEndpoint());
                log("INFO", "Wayline", "  objectKeyPrefix=" + result.getObjectKeyPrefix());
                log("INFO", "Wayline", "  provider=" + result.getProvider());
                log("INFO", "Wayline", "  region=" + result.getRegion());
                if (result.getCredentials() != null) {
                    log("INFO", "Wayline", "  credentials.expire=" + result.getCredentials().getExpire()
                            + "  platform=" + result.getCredentials().getPlatform());
                    log("INFO", "Wayline", "  credentials.accessKeyId=" + result.getCredentials().getAccessKeyId());
                }
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [307] 删除图片备注文件（通过 [304] 上传回调返回的 media_file_id）
    private static void handleDeleteGeoMediaFile() {
        printSeparator("[Media] 删除图片备注文件");
        com.dji.flighthubsdk.core.MediaServiceModule module = getMediaServiceModule();
        if (module == null) return;

        // 使用 [304] 上传回调返回的 media_file_id
        long mediaFileId = ConfigManager.getConfig().getMediaFileId();
        log("INFO", "Media", "待删除 media_file_id=" + mediaFileId
                + "（通过 [304] 获取，填入 config.mediaFileId）");

        java.util.List<Long> ids = new java.util.ArrayList<>();
        ids.add(mediaFileId);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.deleteMediaFiles(ids, new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "Media", "删除图片备注文件失败: " + error.toString());
                } else {
                    log("INFO", "Media", "删除图片备注文件成功! media_file_id=" + mediaFileId);
                }
                latch.countDown();
            }
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // ==================== 报修管理模块（MaintenanceManagement） ====================

    /**
     * [420] 报修管理 - 获取设备报修配置
     */
    private static void handleMaintenanceGetRepairConfigs() {
        printSeparator("[报修管理] 获取设备报修配置");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleMaintenanceGetRepairConfigs", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        MaintenanceManagementModule module = workspace.getMaintenanceManagementModule();
        if (module == null) {
            log("ERROR", "handleMaintenanceGetRepairConfigs", "获取 MaintenanceManagementModule 失败");
            waitForKeyPress();
            return;
        }

        String deviceSn = getMainDeviceSn(selectedDevice);
        log("INFO", "handleMaintenanceGetRepairConfigs", "查询设备报修配置, device_sn: " + deviceSn);

        com.dji.flighthubsdk.bean.GetDeviceRepairConfigParams params = new com.dji.flighthubsdk.bean.GetDeviceRepairConfigParams();
        java.util.List<String> sns = java.util.Arrays.asList(deviceSn);
        params.setDeviceSns(sns);

        module.getRepairConfigs(params, new FlightHubCallback.GetRepairConfigsCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.DeviceRepairConfigResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleMaintenanceGetRepairConfigs", "getRepairConfigs 失败, error: " + error);
                    return;
                }
                log("INFO", "handleMaintenanceGetRepairConfigs", "getRepairConfigs 成功");
                if (result == null || result.getData() == null) {
                    log("INFO", "handleMaintenanceGetRepairConfigs", "结果为空");
                    return;
                }
                java.util.List<com.dji.flighthubsdk.bean.DeviceRepairConfig> configs = result.getData();
                log("INFO", "handleMaintenanceGetRepairConfigs", "设备报修配置数量: " + configs.size());
                for (int i = 0; i < configs.size(); i++) {
                    com.dji.flighthubsdk.bean.DeviceRepairConfig config = configs.get(i);
                    log("INFO", "handleMaintenanceGetRepairConfigs", "  [" + i + "]:");
                    log("INFO", "handleMaintenanceGetRepairConfigs", "    => device_sn: " + config.getDeviceSn());
                    log("INFO", "handleMaintenanceGetRepairConfigs", "    => support_repair: " + config.isSupportRepair());
                    log("INFO", "handleMaintenanceGetRepairConfigs", "    => allowed_source_types: " + config.getAllowedSourceTypes());
                    com.dji.flighthubsdk.bean.RepairGuidance guidance = config.getRepairGuidance();
                    if (guidance != null) {
                        log("INFO", "handleMaintenanceGetRepairConfigs", "    => repair_guidance:");
                        log("INFO", "handleMaintenanceGetRepairConfigs", "        redirect_url: " + guidance.getRedirectUrl());
                        log("INFO", "handleMaintenanceGetRepairConfigs", "        description: " + guidance.getDescription());
                    }
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [421] 报修管理 - 创建报修订单
     */
    private static void handleMaintenanceCreateOrder() {
        printSeparator("[报修管理] 创建报修订单");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleMaintenanceCreateOrder", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        MaintenanceManagementModule module = workspace.getMaintenanceManagementModule();
        if (module == null) {
            log("ERROR", "handleMaintenanceCreateOrder", "获取 MaintenanceManagementModule 失败");
            waitForKeyPress();
            return;
        }

        String deviceSn = getMainDeviceSn(selectedDevice);
        log("INFO", "handleMaintenanceCreateOrder", "创建报修订单, device_sn: " + deviceSn);

        com.dji.flighthubsdk.bean.CreateMaintenanceOrderParams params = new com.dji.flighthubsdk.bean.CreateMaintenanceOrderParams();
        params.setFaultOccurrenceTime("2026-05-15T10:32:56Z");
        params.setFaultDescription("测试");
        params.setContactPerson("15573777377");
        params.setContactPhone("15573777377");
        params.setExpectedServiceTime("2026-05-16T01:00:00Z");

        com.dji.flighthubsdk.bean.MaintenanceOrderContactAddressParams address = new com.dji.flighthubsdk.bean.MaintenanceOrderContactAddressParams();
        address.setCountry("中国");
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetail("测试地址");
        params.setContactAddress(address);

        java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderDeviceItemParams> deviceList = new java.util.ArrayList<>();
        com.dji.flighthubsdk.bean.MaintenanceOrderDeviceItemParams deviceItem = new com.dji.flighthubsdk.bean.MaintenanceOrderDeviceItemParams();
        deviceItem.setDeviceSn("YOUR_DEVICE_SN");
        deviceItem.setDeviceModelKey("3-3-0");
        deviceItem.setModule("0");
        deviceItem.setNameplateSn("YOUR_DEVICE_SN");
        deviceItem.setSourceType(3);
        deviceList.add(deviceItem);
        params.setDeviceList(deviceList);

        com.dji.flighthubsdk.bean.MaintenanceOrderDeviceTopoParams deviceTopo = new com.dji.flighthubsdk.bean.MaintenanceOrderDeviceTopoParams();
        java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams> hosts = new java.util.ArrayList<>();
        com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams hostNode = new com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams();
        hostNode.setDeviceModelKey("0-100-1");
        hostNode.setOnlineStatus(false);
        hostNode.setSn("PLACEHOLDER_HOST_SN");
        hosts.add(hostNode);
        deviceTopo.setHosts(hosts);
        java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams> parents = new java.util.ArrayList<>();
        com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams parentNode = new com.dji.flighthubsdk.bean.MaintenanceOrderTopoNodeParams();
        parentNode.setDeviceModelKey("3-3-0");
        parentNode.setOnlineStatus(false);
        parentNode.setSn("YOUR_DEVICE_SN");
        parents.add(parentNode);
        deviceTopo.setParents(parents);
        params.setDeviceTopo(deviceTopo);

        params.setAttachments(new java.util.ArrayList<>());
        params.setLogList(new java.util.ArrayList<>());

        module.createMaintenanceOrder(params, new FlightHubCallback.CreateMaintenanceOrderCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.CreateMaintenanceOrderResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleMaintenanceCreateOrder", "createMaintenanceOrder 失败, error: " + error);
                    return;
                }
                log("INFO", "handleMaintenanceCreateOrder", "createMaintenanceOrder 成功");
                if (result == null) {
                    log("INFO", "handleMaintenanceCreateOrder", "结果为空");
                    return;
                }
                log("INFO", "handleMaintenanceCreateOrder", "  success: " + result.isSuccess());
                java.util.List<com.dji.flighthubsdk.bean.GeneratedOrderInfo> orders = result.getGeneratedOrders();
                if (orders != null) {
                    log("INFO", "handleMaintenanceCreateOrder", "  生成订单数量: " + orders.size());
                    for (int i = 0; i < orders.size(); i++) {
                        com.dji.flighthubsdk.bean.GeneratedOrderInfo order = orders.get(i);
                        log("INFO", "handleMaintenanceCreateOrder", "  [" + i + "]:");
                        log("INFO", "handleMaintenanceCreateOrder", "    => order_number: " + order.getOrderNumber());
                        log("INFO", "handleMaintenanceCreateOrder", "    => status: " + order.getStatus());
                        log("INFO", "handleMaintenanceCreateOrder", "    => ruiyun_order_number: " + order.getRuiyunOrderNumber());
                    }
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [422] 报修管理 - 查询报修订单详情
     */
    private static void handleMaintenanceGetOrderDetail() {
        printSeparator("[报修管理] 查询报修订单详情");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleMaintenanceGetOrderDetail", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        MaintenanceManagementModule module = workspace.getMaintenanceManagementModule();
        if (module == null) {
            log("ERROR", "handleMaintenanceGetOrderDetail", "获取 MaintenanceManagementModule 失败");
            waitForKeyPress();
            return;
        }

        System.out.print("请输入 order_number: ");
        String orderNumber = scanner.nextLine().trim();
        if (orderNumber.isEmpty()) {
            orderNumber = "demo-order-001";
            log("INFO", "handleMaintenanceGetOrderDetail", "使用默认 order_number: " + orderNumber);
        }

        log("INFO", "handleMaintenanceGetOrderDetail", "查询报修订单详情, order_number: " + orderNumber);

        module.getMaintenanceOrderDetail(orderNumber, new FlightHubCallback.GetMaintenanceOrderDetailCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.MaintenanceOrderDetailResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleMaintenanceGetOrderDetail", "getMaintenanceOrderDetail 失败, error: " + error);
                    return;
                }
                log("INFO", "handleMaintenanceGetOrderDetail", "getMaintenanceOrderDetail 成功");
                if (result == null) {
                    log("INFO", "handleMaintenanceGetOrderDetail", "结果为空");
                    return;
                }
                log("INFO", "handleMaintenanceGetOrderDetail", "订单详情:");
                log("INFO", "handleMaintenanceGetOrderDetail", "  => order_number: " + result.getOrderNumber());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => creator_name: " + result.getCreatorName());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => status: " + result.getStatus() + " (" + result.getStatusText() + ")");
                log("INFO", "handleMaintenanceGetOrderDetail", "  => source_type: " + result.getSourceType() + " (" + result.getSourceTypeText() + ")");
                log("INFO", "handleMaintenanceGetOrderDetail", "  => fault_description: " + result.getFaultDescription());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => fault_occurrence_time: " + result.getFaultOccurrenceTime());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => expected_service_time: " + result.getExpectedServiceTime());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => contact_person: " + result.getContactPerson());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => contact_phone: " + result.getContactPhone());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => contact_email: " + result.getContactEmail());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => device_sn: " + result.getDeviceSn());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => device_model_key: " + result.getDeviceModelKey());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => module: " + result.getModule());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => nameplate_sn: " + result.getNameplateSn());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => created_at: " + result.getCreatedAt());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => updated_at: " + result.getUpdatedAt());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => ruiyun_order_number: " + result.getRuiyunOrderNumber());
                log("INFO", "handleMaintenanceGetOrderDetail", "  => ruiyun_sync_status: " + result.getRuiyunSyncStatus());

                com.dji.flighthubsdk.bean.MaintenanceOrderContactAddress addr = result.getContactAddressFull();
                if (addr != null) {
                    log("INFO", "handleMaintenanceGetOrderDetail", "  => 联系地址: " + addr.getCountry() + " " +
                            addr.getProvince() + " " + addr.getCity() + " " + addr.getDistrict() + " " + addr.getDetail());
                }

                java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderAttachment> attachments = result.getAttachments();
                if (attachments != null && !attachments.isEmpty()) {
                    log("INFO", "handleMaintenanceGetOrderDetail", "  附件数量: " + attachments.size());
                    for (int i = 0; i < attachments.size(); i++) {
                        com.dji.flighthubsdk.bean.MaintenanceOrderAttachment att = attachments.get(i);
                        log("INFO", "handleMaintenanceGetOrderDetail", "    [" + i + "] file_name=" + att.getFileName() +
                                ", file_size=" + att.getFileSize() + ", object_key=" + att.getObjectKey());
                    }
                }

                java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrderStatusHistoryItem> history = result.getStatusHistory();
                if (history != null && !history.isEmpty()) {
                    log("INFO", "handleMaintenanceGetOrderDetail", "  状态历史 (" + history.size() + " 条):");
                    for (int i = 0; i < history.size(); i++) {
                        com.dji.flighthubsdk.bean.MaintenanceOrderStatusHistoryItem h = history.get(i);
                        log("INFO", "handleMaintenanceGetOrderDetail", "    [" + i + "] " + h.getFromStatus() + " -> " +
                                h.getToStatus() + ", time=" + h.getChangeTime() + ", operator=" + h.getOperatorName());
                    }
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [423] 报修管理 - 查询报修订单列表
     */
    private static void handleMaintenanceGetOrdersList() {
        printSeparator("[报修管理] 查询报修订单列表");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleMaintenanceGetOrdersList", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        MaintenanceManagementModule module = workspace.getMaintenanceManagementModule();
        if (module == null) {
            log("ERROR", "handleMaintenanceGetOrdersList", "获取 MaintenanceManagementModule 失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleMaintenanceGetOrdersList", "查询报修订单列表...");

        com.dji.flighthubsdk.bean.GetMaintenanceOrdersListParams params =
                new com.dji.flighthubsdk.bean.GetMaintenanceOrdersListParams();
        params.setPage(1);
        params.setPageSize(20);

        module.getMaintenanceOrdersList(params, new FlightHubCallback.GetMaintenanceOrdersListCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.MaintenanceOrdersListResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleMaintenanceGetOrdersList", "getMaintenanceOrdersList 失败, error: " + error);
                    return;
                }
                log("INFO", "handleMaintenanceGetOrdersList", "getMaintenanceOrdersList 成功");
                if (result == null) {
                    log("INFO", "handleMaintenanceGetOrdersList", "结果为空");
                    return;
                }
                com.dji.flighthubsdk.bean.CommonPaginationData pagination = result.getPagination();
                if (pagination != null) {
                    log("INFO", "handleMaintenanceGetOrdersList", "分页信息: page=" + pagination.getPage() +
                            ", page_size=" + pagination.getPageSize() + ", total=" + pagination.getTotal());
                }
                java.util.List<com.dji.flighthubsdk.bean.MaintenanceOrdersListItem> list = result.getList();
                if (list == null || list.isEmpty()) {
                    log("INFO", "handleMaintenanceGetOrdersList", "订单列表为空");
                    return;
                }
                log("INFO", "handleMaintenanceGetOrdersList", "订单数量: " + list.size());
                for (int i = 0; i < list.size(); i++) {
                    com.dji.flighthubsdk.bean.MaintenanceOrdersListItem item = list.get(i);
                    log("INFO", "handleMaintenanceGetOrdersList", "  [" + i + "]:");
                    log("INFO", "handleMaintenanceGetOrdersList", "    => order_number: " + item.getOrderNumber());
                    log("INFO", "handleMaintenanceGetOrdersList", "    => status: " + item.getStatus() + " (" + item.getStatusText() + ")");
                    log("INFO", "handleMaintenanceGetOrdersList", "    => device_sn: " + item.getDeviceSn());
                    log("INFO", "handleMaintenanceGetOrdersList", "    => device_model_name: " + item.getDeviceModelName());
                    log("INFO", "handleMaintenanceGetOrdersList", "    => fault_description: " + item.getFaultDescription());
                    log("INFO", "handleMaintenanceGetOrdersList", "    => creator_name: " + item.getCreatorName());
                    log("INFO", "handleMaintenanceGetOrdersList", "    => created_at: " + item.getCreatedAt());
                }
            }
        });

        waitForKeyPress();
    }

    // ==================== 定损管理模块（DiagnosisManagement） ====================

    /**
     * [430] 定损管理 - 下发定损指令
     */
    private static void handleDiagnosisCreate() {
        printSeparator("[定损管理] 下发定损指令");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleDiagnosisCreate", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        DiagnosisManagementModule module = workspace.getDiagnosisManagementModule();
        if (module == null) {
            log("ERROR", "handleDiagnosisCreate", "获取 DiagnosisManagementModule 失败");
            waitForKeyPress();
            return;
        }

        String deviceSn = getMainDeviceSn(selectedDevice);
        log("INFO", "handleDiagnosisCreate", "下发定损指令, device_sn: " + deviceSn);

        com.dji.flighthubsdk.bean.CreateSelfDiagnosisParams params =
                new com.dji.flighthubsdk.bean.CreateSelfDiagnosisParams();
        params.setDeviceSn(deviceSn);
        params.setType(0);
        params.setPart(0);
        params.setIndex(0);
        params.setMotorAction(0);

        module.createSelfDiagnosis(params, new FlightHubCallback.CreateSelfDiagnosisCallback() {
            @Override
            public void onResult(FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleDiagnosisCreate", "createSelfDiagnosis 失败, error: " + error);
                } else {
                    log("INFO", "handleDiagnosisCreate", "createSelfDiagnosis 成功");
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [431] 定损管理 - 定损操作(取消/删除)
     */
    private static void handleDiagnosisOperate() {
        printSeparator("[定损管理] 定损操作(取消/删除)");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleDiagnosisOperate", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        DiagnosisManagementModule module = workspace.getDiagnosisManagementModule();
        if (module == null) {
            log("ERROR", "handleDiagnosisOperate", "获取 DiagnosisManagementModule 失败");
            waitForKeyPress();
            return;
        }

        System.out.print("请输入 report_uuid: ");
        String reportUuid = scanner.nextLine().trim();
        if (reportUuid.isEmpty()) {
            reportUuid = "demo-report-uuid";
            log("INFO", "handleDiagnosisOperate", "使用默认 report_uuid: " + reportUuid);
        }

        System.out.println("请选择操作: [1] 取消  [2] 删除");
        int choice = getUserChoice(1, 2);
        String cmdMethod = (choice == 1) ? "cancel" : "delete";

        log("INFO", "handleDiagnosisOperate", "执行定损操作: report_uuid=" + reportUuid + ", cmd=" + cmdMethod);

        com.dji.flighthubsdk.bean.OperateSelfDiagnosisParams params =
                new com.dji.flighthubsdk.bean.OperateSelfDiagnosisParams();
        params.setReportUuid(reportUuid);
        params.setReportCmdMethod(cmdMethod);

        module.operateSelfDiagnosis(params, new FlightHubCallback.OperateSelfDiagnosisCallback() {
            @Override
            public void onResult(FlightHubError error) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleDiagnosisOperate", "operateSelfDiagnosis 失败, error: " + error);
                } else {
                    log("INFO", "handleDiagnosisOperate", "operateSelfDiagnosis 成功");
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [432] 定损管理 - 查询单条定损报告详情
     */
    private static void handleDiagnosisGetReport() {
        printSeparator("[定损管理] 查询单条定损报告详情");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleDiagnosisGetReport", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        DiagnosisManagementModule module = workspace.getDiagnosisManagementModule();
        if (module == null) {
            log("ERROR", "handleDiagnosisGetReport", "获取 DiagnosisManagementModule 失败");
            waitForKeyPress();
            return;
        }

        System.out.print("请输入 report_uuid: ");
        String reportUuid = scanner.nextLine().trim();
        if (reportUuid.isEmpty()) {
            reportUuid = "demo-report-uuid";
            log("INFO", "handleDiagnosisGetReport", "使用默认 report_uuid: " + reportUuid);
        }

        log("INFO", "handleDiagnosisGetReport", "查询定损报告详情, report_uuid: " + reportUuid);

        module.getSelfDiagnosisReport(reportUuid, new FlightHubCallback.GetSelfDiagnosisReportCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleDiagnosisGetReport", "getSelfDiagnosisReport 失败, error: " + error);
                    return;
                }
                log("INFO", "handleDiagnosisGetReport", "getSelfDiagnosisReport 成功");
                if (result == null) {
                    log("INFO", "handleDiagnosisGetReport", "结果为空");
                    return;
                }
                log("INFO", "handleDiagnosisGetReport", "定损报告详情:");
                log("INFO", "handleDiagnosisGetReport", "  => report_uuid: " + result.getReportUuid());
                log("INFO", "handleDiagnosisGetReport", "  => device_sn: " + result.getDeviceSn());
                log("INFO", "handleDiagnosisGetReport", "  => device_model_key: " + result.getDeviceModelKey());
                log("INFO", "handleDiagnosisGetReport", "  => status: " + result.getStatus());
                log("INFO", "handleDiagnosisGetReport", "  => result_code: " + result.getResultCode());
                log("INFO", "handleDiagnosisGetReport", "  => part: " + result.getPart());
                log("INFO", "handleDiagnosisGetReport", "  => origin: " + result.getOrigin());
                log("INFO", "handleDiagnosisGetReport", "  => bid: " + result.getBid());
                log("INFO", "handleDiagnosisGetReport", "  => complete_machine_sn: " + result.getCompleteMachineSn());
                log("INFO", "handleDiagnosisGetReport", "  => create_time: " + result.getCreateTime());
                log("INFO", "handleDiagnosisGetReport", "  => qr_code_content: " + result.getQrCodeContent());
                log("INFO", "handleDiagnosisGetReport", "  => qr_guide_text: " + result.getQrGuideText());
            }
        });

        waitForKeyPress();
    }

    /**
     * [433] 定损管理 - 查询定损报告列表
     */
    private static void handleDiagnosisGetReportList() {
        printSeparator("[定损管理] 查询定损报告列表");

        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleDiagnosisGetReportList", "获取workspace失败");
            waitForKeyPress();
            return;
        }

        DiagnosisManagementModule module = workspace.getDiagnosisManagementModule();
        if (module == null) {
            log("ERROR", "handleDiagnosisGetReportList", "获取 DiagnosisManagementModule 失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleDiagnosisGetReportList", "查询定损报告列表...");

        com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListParams params =
                new com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListParams();
        params.setPage(1);
        params.setPageSize(20);

        module.getSelfDiagnosisReportList(params, new FlightHubCallback.GetSelfDiagnosisReportListCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.GetSelfDiagnosisReportListResults result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleDiagnosisGetReportList", "getSelfDiagnosisReportList 失败, error: " + error);
                    return;
                }
                log("INFO", "handleDiagnosisGetReportList", "getSelfDiagnosisReportList 成功");
                if (result == null) {
                    log("INFO", "handleDiagnosisGetReportList", "结果为空");
                    return;
                }
                com.dji.flighthubsdk.bean.CommonPaginationData pagination = result.getPagination();
                if (pagination != null) {
                    log("INFO", "handleDiagnosisGetReportList", "分页信息: page=" + pagination.getPage() +
                            ", page_size=" + pagination.getPageSize() + ", total=" + pagination.getTotal());
                }
                java.util.List<com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults> list = result.getList();
                if (list == null || list.isEmpty()) {
                    log("INFO", "handleDiagnosisGetReportList", "定损报告列表为空");
                    return;
                }
                log("INFO", "handleDiagnosisGetReportList", "定损报告数量: " + list.size());
                for (int i = 0; i < list.size(); i++) {
                    com.dji.flighthubsdk.bean.GetSelfDiagnosisReportResults item = list.get(i);
                    log("INFO", "handleDiagnosisGetReportList", "  [" + i + "]:");
                    log("INFO", "handleDiagnosisGetReportList", "    => report_uuid: " + item.getReportUuid());
                    log("INFO", "handleDiagnosisGetReportList", "    => device_sn: " + item.getDeviceSn());
                    log("INFO", "handleDiagnosisGetReportList", "    => status: " + item.getStatus());
                    log("INFO", "handleDiagnosisGetReportList", "    => result_code: " + item.getResultCode());
                    log("INFO", "handleDiagnosisGetReportList", "    => part: " + item.getPart());
                    log("INFO", "handleDiagnosisGetReportList", "    => create_time: " + item.getCreateTime());
                }
            }
        });

        waitForKeyPress();
    }

    // ==================== AI Box 管理模块 ====================

    /**
     * [440] AI Box - 获取AI Box设备信息
     */
    private static void handleAIBoxFetchInfo() {
        printSeparator("[AI Box] 获取AI Box设备信息");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxFetchInfo");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);
        log("INFO", "handleAIBoxFetchInfo", "查询AI Box信息, device_sn: " + deviceSn);

        aiBoxModule.fetchAIBoxInfo(deviceSn, new FlightHubCallback.FetchAIBoxInfoCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.AIBoxInfoListResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxFetchInfo", "fetchAIBoxInfo 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxFetchInfo", "fetchAIBoxInfo 成功");
                if (result == null || result.getAiBoxes() == null) {
                    log("INFO", "handleAIBoxFetchInfo", "结果为空");
                    return;
                }
                java.util.List<com.dji.flighthubsdk.bean.AIBoxInfoResult> boxes = result.getAiBoxes();
                log("INFO", "handleAIBoxFetchInfo", "AI Box 数量: " + boxes.size());
                for (int i = 0; i < boxes.size(); i++) {
                    com.dji.flighthubsdk.bean.AIBoxInfoResult box = boxes.get(i);
                    log("INFO", "handleAIBoxFetchInfo", "  [" + i + "]:");
                    log("INFO", "handleAIBoxFetchInfo", "    => index: " + box.getIndex());
                    log("INFO", "handleAIBoxFetchInfo", "    => type: " + box.getType());
                    log("INFO", "handleAIBoxFetchInfo", "    => sn: " + box.getSn());
                    log("INFO", "handleAIBoxFetchInfo", "    => firmware_version: " + box.getFirmwareVersion());
                    log("INFO", "handleAIBoxFetchInfo", "    => storage_total: " + box.getStorageTotal());
                    log("INFO", "handleAIBoxFetchInfo", "    => storage_used: " + box.getStorageUsed());
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [441] AI Box - 获取文件下载地址
     */
    private static void handleAIBoxFetchFileDownloadUrl() {
        printSeparator("[AI Box] 获取文件下载地址");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxFetchFileDownloadUrl");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn (直接回车使用默认): ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入 object_key (直接回车使用默认): ");
        String objectKey = scanner.nextLine().trim();
        if (objectKey.isEmpty()) objectKey = "demo/path/file.zip";

        log("INFO", "handleAIBoxFetchFileDownloadUrl", "获取文件下载地址, device_sn=" + deviceSn + ", box_sn=" + boxSn);

        com.dji.flighthubsdk.bean.FetchFileDownloadUrlParam param = new com.dji.flighthubsdk.bean.FetchFileDownloadUrlParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setObjectKeys(java.util.Collections.singletonList(objectKey));

        aiBoxModule.fetchFileDownloadUrl(param, new FlightHubCallback.FetchFileDownloadUrlCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.FileDownloadUrlListResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxFetchFileDownloadUrl", "fetchFileDownloadUrl 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxFetchFileDownloadUrl", "fetchFileDownloadUrl 成功");
                if (result == null || result.getList() == null) {
                    log("INFO", "handleAIBoxFetchFileDownloadUrl", "结果为空");
                    return;
                }
                java.util.List<String> urls = result.getList();
                log("INFO", "handleAIBoxFetchFileDownloadUrl", "下载地址数量: " + urls.size());
                for (int i = 0; i < urls.size(); i++) {
                    log("INFO", "handleAIBoxFetchFileDownloadUrl", "  [" + i + "] url: " + urls.get(i));
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [442] AI Box - 获取应用列表（异步下发）
     */
    private static void handleAIBoxFetchAppList() {
        printSeparator("[AI Box] 获取应用列表（异步下发）");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxFetchAppList");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn (直接回车使用默认): ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        log("INFO", "handleAIBoxFetchAppList", "获取应用列表, device_sn=" + deviceSn + ", box_sn=" + boxSn);

        com.dji.flighthubsdk.bean.AIBoxFetchAppListParam param = new com.dji.flighthubsdk.bean.AIBoxFetchAppListParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);

        aiBoxModule.fetchAppList(param, new FlightHubCallback.FetchAppListCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.FetchAppListResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxFetchAppList", "fetchAppList 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxFetchAppList", "fetchAppList 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxFetchAppList", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxFetchAppList", "  => bid: " + result.getBid());
                log("INFO", "handleAIBoxFetchAppList", "  => status: " + result.getStatus());
                log("INFO", "handleAIBoxFetchAppList", "  提示: 使用此 bid 通过 [443] 查询进度");
            }
        });

        waitForKeyPress();
    }

    /**
     * [443] AI Box - 查询获取应用列表进度
     */
    private static void handleAIBoxFetchAppListProgress() {
        printSeparator("[AI Box] 查询获取应用列表进度");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxFetchAppListProgress");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn: ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入 bid (从 [442] 获取): ");
        String bid = scanner.nextLine().trim();
        if (bid.isEmpty()) bid = "demo-bid";

        log("INFO", "handleAIBoxFetchAppListProgress", "查询应用列表进度, bid=" + bid);

        com.dji.flighthubsdk.bean.AIBoxFetchAppListProgressParam param = new com.dji.flighthubsdk.bean.AIBoxFetchAppListProgressParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setBid(bid);

        aiBoxModule.fetchAppListProgress(param, new FlightHubCallback.FetchAppListProgressCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.FetchAppListProgressResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxFetchAppListProgress", "fetchAppListProgress 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxFetchAppListProgress", "fetchAppListProgress 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxFetchAppListProgress", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxFetchAppListProgress", "  => bid: " + result.getBid());
                log("INFO", "handleAIBoxFetchAppListProgress", "  => status: " + result.getStatus());
                log("INFO", "handleAIBoxFetchAppListProgress", "  => percent: " + result.getPercent() + "%");
                log("INFO", "handleAIBoxFetchAppListProgress", "  => device_code: " + result.getDeviceCode());

                com.dji.flighthubsdk.bean.FetchAppListProgressExtResult ext = result.getExt();
                if (ext != null) {
                    log("INFO", "handleAIBoxFetchAppListProgress", "  => ext.sn: " + ext.getSn());
                    log("INFO", "handleAIBoxFetchAppListProgress", "  => ext.error_reason: " + ext.getErrorReason());
                    java.util.List<com.dji.flighthubsdk.bean.AIBoxAppInfoResult> apps = ext.getAiBoxAppInfo();
                    if (apps != null && !apps.isEmpty()) {
                        log("INFO", "handleAIBoxFetchAppListProgress", "  应用列表 (" + apps.size() + " 个):");
                        for (int i = 0; i < apps.size(); i++) {
                            com.dji.flighthubsdk.bean.AIBoxAppInfoResult app = apps.get(i);
                            log("INFO", "handleAIBoxFetchAppListProgress", "    [" + i + "] id=" + app.getId() +
                                    ", name=" + app.getName() + ", version=" + app.getVersion() +
                                    ", type=" + app.getType() + ", appSize=" + app.getAppSize() +
                                    ", dataSize=" + app.getDataSize());
                            log("INFO", "handleAIBoxFetchAppListProgress", "         publisher=" + app.getPublisher() +
                                    ", description=" + app.getDescription());
                        }
                    }
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [444] AI Box - 导出文件（异步下发）
     */
    private static void handleAIBoxExportFile() {
        printSeparator("[AI Box] 导出文件（异步下发）");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxExportFile");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn: ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入 app_id: ");
        String appId = scanner.nextLine().trim();
        if (appId.isEmpty()) appId = "demo-app-id";

        System.out.print("请输入 export_type (0=数据, 1=日志, 默认0): ");
        String typeStr = scanner.nextLine().trim();
        int exportType = typeStr.isEmpty() ? 0 : Integer.parseInt(typeStr);

        log("INFO", "handleAIBoxExportFile", "导出文件, device_sn=" + deviceSn + ", app_id=" + appId + ", export_type=" + exportType);

        com.dji.flighthubsdk.bean.AIBoxExportFileParam param = new com.dji.flighthubsdk.bean.AIBoxExportFileParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setAppId(appId);
        param.setExportType(exportType);

        aiBoxModule.exportFile(param, new FlightHubCallback.ExportFileCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.ExportFileResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxExportFile", "exportFile 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxExportFile", "exportFile 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxExportFile", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxExportFile", "  => bid: " + result.getBid());
                log("INFO", "handleAIBoxExportFile", "  => status: " + result.getStatus());
                log("INFO", "handleAIBoxExportFile", "  提示: 使用此 bid 通过 [445] 查询导出进度");
            }
        });

        waitForKeyPress();
    }

    /**
     * [445] AI Box - 查询导出文件进度
     */
    private static void handleAIBoxExportFileProgress() {
        printSeparator("[AI Box] 查询导出文件进度");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxExportFileProgress");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn: ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入 bid (从 [444] 获取): ");
        String bid = scanner.nextLine().trim();
        if (bid.isEmpty()) bid = "demo-bid";

        log("INFO", "handleAIBoxExportFileProgress", "查询导出进度, bid=" + bid);

        com.dji.flighthubsdk.bean.AIBoxExportFileProgressParam param = new com.dji.flighthubsdk.bean.AIBoxExportFileProgressParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setBid(bid);

        aiBoxModule.exportFileProgress(param, new FlightHubCallback.ExportFileProgressCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.ExportFileProgressResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxExportFileProgress", "exportFileProgress 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxExportFileProgress", "exportFileProgress 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxExportFileProgress", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxExportFileProgress", "  => bid: " + result.getBid());
                log("INFO", "handleAIBoxExportFileProgress", "  => status: " + result.getStatus());
                log("INFO", "handleAIBoxExportFileProgress", "  => percent: " + result.getPercent() + "%");
                log("INFO", "handleAIBoxExportFileProgress", "  => device_code: " + result.getDeviceCode());

                com.dji.flighthubsdk.bean.ExportFileProgressExtResult ext = result.getExt();
                if (ext != null) {
                    log("INFO", "handleAIBoxExportFileProgress", "  => ext.sn: " + ext.getSn());
                    log("INFO", "handleAIBoxExportFileProgress", "  => ext.export_type: " + ext.getExportType());
                    log("INFO", "handleAIBoxExportFileProgress", "  => ext.app_id: " + ext.getAppId());
                    log("INFO", "handleAIBoxExportFileProgress", "  => ext.object_key: " + ext.getObjectKey());
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [446] AI Box - 上传文件到AI Box
     */
    private static void handleAIBoxUploadFile() {
        printSeparator("[AI Box] 上传文件到AI Box");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxUploadFile");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn: ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入要上传的文件路径: ");
        String filePath = scanner.nextLine().trim();
        if (filePath.isEmpty()) {
            filePath = "/tmp/demo_upload.zip";
            log("INFO", "handleAIBoxUploadFile", "使用默认文件路径: " + filePath);
        }

        log("INFO", "handleAIBoxUploadFile", "上传文件, device_sn=" + deviceSn + ", box_sn=" + boxSn + ", file_path=" + filePath);

        aiBoxModule.uploadAIBoxFile(filePath, deviceSn, boxSn, new FlightHubCallback.UploadAIBoxFileCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.UploadFileResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxUploadFile", "uploadAIBoxFile 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxUploadFile", "uploadAIBoxFile 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxUploadFile", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxUploadFile", "  => object_url: " + result.getObjectUrl());
                log("INFO", "handleAIBoxUploadFile", "  => etag: " + result.getEtag());
                log("INFO", "handleAIBoxUploadFile", "  => object_key: " + result.getObjectKey());
                log("INFO", "handleAIBoxUploadFile", "  提示: 使用 object_key 通过 [447] 安装应用");
            }
        });

        waitForKeyPress();
    }

    /**
     * [447] AI Box - 安装应用（异步下发）
     */
    private static void handleAIBoxInstallApp() {
        printSeparator("[AI Box] 安装应用（异步下发）");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxInstallApp");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn: ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入 object_key (从 [446] 上传获取): ");
        String objectKey = scanner.nextLine().trim();
        if (objectKey.isEmpty()) objectKey = "demo/path/app.apk";

        log("INFO", "handleAIBoxInstallApp", "安装应用, device_sn=" + deviceSn + ", object_key=" + objectKey);

        com.dji.flighthubsdk.bean.AIBoxInstallAppParam param = new com.dji.flighthubsdk.bean.AIBoxInstallAppParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setObjectKey(objectKey);

        aiBoxModule.installApp(param, new FlightHubCallback.InstallAppCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.InstallAppResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxInstallApp", "installApp 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxInstallApp", "installApp 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxInstallApp", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxInstallApp", "  => bid: " + result.getBid());
                log("INFO", "handleAIBoxInstallApp", "  => status: " + result.getStatus());
                log("INFO", "handleAIBoxInstallApp", "  提示: 使用此 bid 通过 [448] 查询安装进度");
            }
        });

        waitForKeyPress();
    }

    /**
     * [448] AI Box - 查询安装应用进度
     */
    private static void handleAIBoxInstallAppProgress() {
        printSeparator("[AI Box] 查询安装应用进度");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxInstallAppProgress");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn: ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入 bid (从 [447] 获取): ");
        String bid = scanner.nextLine().trim();
        if (bid.isEmpty()) bid = "demo-bid";

        log("INFO", "handleAIBoxInstallAppProgress", "查询安装进度, bid=" + bid);

        com.dji.flighthubsdk.bean.AIBoxInstallAppProgressParam param = new com.dji.flighthubsdk.bean.AIBoxInstallAppProgressParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setBid(bid);

        aiBoxModule.installAppProgress(param, new FlightHubCallback.InstallAppProgressCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.InstallAppProgressResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxInstallAppProgress", "installAppProgress 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxInstallAppProgress", "installAppProgress 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxInstallAppProgress", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxInstallAppProgress", "  => bid: " + result.getBid());
                log("INFO", "handleAIBoxInstallAppProgress", "  => status: " + result.getStatus());
                log("INFO", "handleAIBoxInstallAppProgress", "  => percent: " + result.getPercent() + "%");
                log("INFO", "handleAIBoxInstallAppProgress", "  => device_code: " + result.getDeviceCode());

                com.dji.flighthubsdk.bean.InstallAppProgressExtResult ext = result.getExt();
                if (ext != null) {
                    log("INFO", "handleAIBoxInstallAppProgress", "  => ext.sn: " + ext.getSn());
                }
            }
        });

        waitForKeyPress();
    }

    /**
     * [449] AI Box - 操作应用（启动/停止/卸载）
     */
    private static void handleAIBoxOperateApp() {
        printSeparator("[AI Box] 操作应用（启动/停止/卸载）");

        AIBoxModule aiBoxModule = requireAIBoxModule("handleAIBoxOperateApp");
        if (aiBoxModule == null) { waitForKeyPress(); return; }

        String deviceSn = getMainDeviceSn(selectedDevice);

        System.out.print("请输入 box_sn: ");
        String boxSn = scanner.nextLine().trim();
        if (boxSn.isEmpty()) boxSn = "demo-box-sn";

        System.out.print("请输入 app_id: ");
        String appId = scanner.nextLine().trim();
        if (appId.isEmpty()) appId = "demo-app-id";

        System.out.println("请选择操作: [1] 启动(start)  [2] 停止(stop)  [3] 卸载(uninstall)");
        int choice = getUserChoice(1, 3);
        String action;
        switch (choice) {
            case 1: action = "start"; break;
            case 2: action = "stop"; break;
            case 3: action = "uninstall"; break;
            default: action = "start"; break;
        }

        log("INFO", "handleAIBoxOperateApp", "操作应用, device_sn=" + deviceSn + ", app_id=" + appId + ", action=" + action);

        com.dji.flighthubsdk.bean.AIBoxOperateAppParam param = new com.dji.flighthubsdk.bean.AIBoxOperateAppParam();
        param.setDeviceSn(deviceSn);
        param.setBoxSn(boxSn);
        param.setAppId(appId);
        param.setAction(action);

        aiBoxModule.operateApp(param, new FlightHubCallback.OperateAppCallback() {
            @Override
            public void onResult(FlightHubError error,
                                 com.dji.flighthubsdk.bean.OperateAppResult result) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleAIBoxOperateApp", "operateApp 失败, error: " + error);
                    return;
                }
                log("INFO", "handleAIBoxOperateApp", "operateApp 成功");
                if (result == null) {
                    log("INFO", "handleAIBoxOperateApp", "结果为空");
                    return;
                }
                log("INFO", "handleAIBoxOperateApp", "  => bid: " + result.getBid());
                log("INFO", "handleAIBoxOperateApp", "  => status: " + result.getStatus());
            }
        });

        waitForKeyPress();
    }

    private static void handleListenTaskEvents() {
        printSeparator("监听任务事件（指令/指点飞行/航线）");
        if (dock == null) {
            dock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        }
        if (dock == null) {
            log("ERROR", "handleListenTaskEvents", "获取机场实例失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleListenTaskEvents", "开始监听任务事件...");

        // 指令任务事件（已迁移到 TakeoffModule）
        if (drone == null) {
            drone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        }
        if (drone != null && drone.getTakeoffModule() != null) {
            drone.getTakeoffModule().listenCommandTaskEvent(event -> {
                log("INFO", "listenCommandTaskEvent", "========== 指令任务事件 ==========");
                log("INFO", "listenCommandTaskEvent", "event_id: " + event.getEventId());
                log("INFO", "listenCommandTaskEvent", "action: " + event.getAction());
                log("INFO", "listenCommandTaskEvent", "uuid: " + event.getUuid());
                log("INFO", "listenCommandTaskEvent", "timestamp: " + event.getTimestamp());
                if (event.getTaskData() != null) {
                    com.dji.flighthubsdk.bean.CommandTaskData td = event.getTaskData();
                    log("INFO", "listenCommandTaskEvent", "task_data:");
                    log("INFO", "listenCommandTaskEvent", "  id: " + td.getId());
                    log("INFO", "listenCommandTaskEvent", "  created_at: " + td.getCreatedAt());
                    log("INFO", "listenCommandTaskEvent", "  uuid: " + td.getUuid());
                    log("INFO", "listenCommandTaskEvent", "  prj_uuid: " + td.getPrjUuid());
                    log("INFO", "listenCommandTaskEvent", "  sn: " + td.getSn());
                    log("INFO", "listenCommandTaskEvent", "  drone_sn: " + td.getDroneSn());
                    log("INFO", "listenCommandTaskEvent", "  track_id: " + td.getTrackId());
                    log("INFO", "listenCommandTaskEvent", "  user_id: " + td.getUserId());
                    log("INFO", "listenCommandTaskEvent", "  status: " + td.getStatus());
                    log("INFO", "listenCommandTaskEvent", "  err_code: " + td.getErrCode());
                    log("INFO", "listenCommandTaskEvent", "  takeoff_mode: " + td.getTakeoffMode());
                    log("INFO", "listenCommandTaskEvent", "  security_takeoff_height: " + td.getSecurityTakeoffHeight());
                    log("INFO", "listenCommandTaskEvent", "  max_speed: " + td.getMaxSpeed());
                    log("INFO", "listenCommandTaskEvent", "  out_of_control_action: " + td.getOutOfControlAction());
                    log("INFO", "listenCommandTaskEvent", "  rth_altitude: " + td.getRthAltitude());
                    log("INFO", "listenCommandTaskEvent", "  commander_flight_mode: " + td.getCommanderFlightMode());
                    log("INFO", "listenCommandTaskEvent", "  commander_flight_height: " + td.getCommanderFlightHeight());
                    log("INFO", "listenCommandTaskEvent", "  rth_mode: " + td.getRthMode());
                    log("INFO", "listenCommandTaskEvent", "  cloud_to_cloud_id: " + td.getCloudToCloudId());
                }
            });
        }

        if (drone == null) {
            log("ERROR", "handleListenTaskEvents", "获取无人机实例失败，ATC 任务事件需通过 Drone module 订阅");
            waitForKeyPress();
            return;
        }

        FlyToPointModule flyToModule = drone.getFlyToPointModule();
        if (flyToModule != null) {
            flyToModule.listenFlyToTaskEvent(event -> {
                log("INFO", "listenFlyToTaskEvent", "========== 指点飞行任务事件 ==========");
                log("INFO", "listenFlyToTaskEvent", "event_id: " + event.getEventId());
                log("INFO", "listenFlyToTaskEvent", "action: " + event.getAction());
                log("INFO", "listenFlyToTaskEvent", "uuid: " + event.getUuid());
                log("INFO", "listenFlyToTaskEvent", "timestamp: " + event.getTimestamp());
                if (event.getTaskData() != null) {
                    com.dji.flighthubsdk.bean.FlyToTaskData td = event.getTaskData();
                    log("INFO", "listenFlyToTaskEvent", "task_data:");
                    log("INFO", "listenFlyToTaskEvent", "  id: " + td.getId());
                    log("INFO", "listenFlyToTaskEvent", "  uuid: " + td.getUuid());
                    log("INFO", "listenFlyToTaskEvent", "  prj_uuid: " + td.getPrjUuid());
                    log("INFO", "listenFlyToTaskEvent", "  sn: " + td.getSn());
                    log("INFO", "listenFlyToTaskEvent", "  drone_sn: " + td.getDroneSn());
                    log("INFO", "listenFlyToTaskEvent", "  user_id: " + td.getUserId());
                    log("INFO", "listenFlyToTaskEvent", "  seq_no: " + td.getSeqNo());
                    log("INFO", "listenFlyToTaskEvent", "  flight_task_uuid: " + td.getFlightTaskUuid());
                    log("INFO", "listenFlyToTaskEvent", "  flight_task_type: " + td.getFlightTaskType());
                    log("INFO", "listenFlyToTaskEvent", "  status: " + td.getStatus());
                    log("INFO", "listenFlyToTaskEvent", "  err_code: " + td.getErrCode());
                    log("INFO", "listenFlyToTaskEvent", "  way_point_index: " + td.getWayPointIndex());
                    if (td.getWayPoints() != null) {
                        log("INFO", "listenFlyToTaskEvent", "  way_points: (" + td.getWayPoints().size() + " points)");
                        for (int i = 0; i < td.getWayPoints().size(); i++) {
                            com.dji.flighthubsdk.bean.FlyToWayPoint wp = td.getWayPoints().get(i);
                            log("INFO", "listenFlyToTaskEvent", "    [" + i + "] lat=" + wp.getLatitude() + ", lng=" + wp.getLongitude() + ", height=" + wp.getHeight());
                        }
                    }
                    if (td.getPlannedPathPoints() != null && !td.getPlannedPathPoints().isEmpty()) {
                        log("INFO", "listenFlyToTaskEvent", "  planned_path_points: (" + td.getPlannedPathPoints().size() + " points)");
                    }
                    log("INFO", "listenFlyToTaskEvent", "  remaining_distance: " + td.getRemainingDistance() + " m");
                    log("INFO", "listenFlyToTaskEvent", "  remaining_time: " + td.getRemainingTime() + " s");
                }
            });
        }

        FlightTaskControlModule flightTaskControlModule = drone.getFlightTaskControlModule();
        if (flightTaskControlModule != null) {
            flightTaskControlModule.listenWaylineTaskEvent(event -> {
                log("INFO", "listenWaylineTaskEvent", "========== 航线任务事件 ==========");
                log("INFO", "listenWaylineTaskEvent", "event_id: " + event.getEventId());
                log("INFO", "listenWaylineTaskEvent", "action: " + event.getAction());
                log("INFO", "listenWaylineTaskEvent", "uuid: " + event.getUuid());
                log("INFO", "listenWaylineTaskEvent", "timestamp: " + event.getTimestamp());
                if (event.getTaskData() != null) {
                    com.dji.flighthubsdk.bean.WaylineTaskData td = event.getTaskData();
                    log("INFO", "listenWaylineTaskEvent", "task_data:");
                    log("INFO", "listenWaylineTaskEvent", "  name: " + td.getName());
                    log("INFO", "listenWaylineTaskEvent", "  uuid: " + td.getUuid());
                    log("INFO", "listenWaylineTaskEvent", "  task_type: " + td.getTaskType());
                    log("INFO", "listenWaylineTaskEvent", "  status: " + td.getStatus());
                    log("INFO", "listenWaylineTaskEvent", "  sn: " + td.getSn());
                    log("INFO", "listenWaylineTaskEvent", "  landing_dock_sn: " + td.getLandingDockSn());
                    log("INFO", "listenWaylineTaskEvent", "  wayline_uuid: " + td.getWaylineUuid());
                    log("INFO", "listenWaylineTaskEvent", "  wayline_precision_type: " + td.getWaylinePrecisionType());
                    log("INFO", "listenWaylineTaskEvent", "  wayline_duration: " + td.getWaylineDuration() + " s");
                    log("INFO", "listenWaylineTaskEvent", "  total_waypoints: " + td.getTotalWaypoints());
                    log("INFO", "listenWaylineTaskEvent", "  current_waypoint_index: " + td.getCurrentWaypointIndex());
                    log("INFO", "listenWaylineTaskEvent", "  bid: " + td.getBid());
                    log("INFO", "listenWaylineTaskEvent", "  begin_at: " + td.getBeginAt());
                    log("INFO", "listenWaylineTaskEvent", "  end_at: " + td.getEndAt());
                    log("INFO", "listenWaylineTaskEvent", "  run_at: " + td.getRunAt());
                    log("INFO", "listenWaylineTaskEvent", "  completed_at: " + td.getCompletedAt());
                    log("INFO", "listenWaylineTaskEvent", "  out_of_control_action_in_flight: " + td.getOutOfControlActionInFlight());
                    log("INFO", "listenWaylineTaskEvent", "  rth_altitude: " + td.getRthAltitude());
                    log("INFO", "listenWaylineTaskEvent", "  rth_mode: " + td.getRthMode());
                    log("INFO", "listenWaylineTaskEvent", "  min_battery_capacity: " + td.getMinBatteryCapacity());
                    log("INFO", "listenWaylineTaskEvent", "  cloud_to_cloud_id: " + td.getCloudToCloudId());
                    if (td.getBreakPoint() != null) {
                        com.dji.flighthubsdk.bean.WaylineBreakPoint bp = td.getBreakPoint();
                        log("INFO", "listenWaylineTaskEvent", "  break_point:");
                        log("INFO", "listenWaylineTaskEvent", "    index: " + bp.getIndex());
                        log("INFO", "listenWaylineTaskEvent", "    state: " + bp.getState());
                        log("INFO", "listenWaylineTaskEvent", "    progress: " + bp.getProgress());
                        log("INFO", "listenWaylineTaskEvent", "    wayline_id: " + bp.getWaylineId());
                        log("INFO", "listenWaylineTaskEvent", "    break_reason: " + bp.getBreakReason());
                        log("INFO", "listenWaylineTaskEvent", "    lat=" + bp.getLatitude() + ", lng=" + bp.getLongitude() + ", height=" + bp.getHeight());
                        log("INFO", "listenWaylineTaskEvent", "    attitude_head: " + bp.getAttitudeHead());
                    }
                    log("INFO", "listenWaylineTaskEvent", "  is_break_point_resume: " + td.isBreakPointResume());
                    log("INFO", "listenWaylineTaskEvent", "  media_upload_status: " + td.getMediaUploadStatus());
                    log("INFO", "listenWaylineTaskEvent", "  fast_reconstruction: " + td.isFastReconstruction());
                    log("INFO", "listenWaylineTaskEvent", "  device_type: " + td.getDeviceType());
                    if (td.getObstacles() != null && !td.getObstacles().isEmpty()) {
                        log("INFO", "listenWaylineTaskEvent", "  obstacles: (" + td.getObstacles().size() + " items)");
                        for (int i = 0; i < td.getObstacles().size(); i++) {
                            com.dji.flighthubsdk.bean.WaylineObstacle obs = td.getObstacles().get(i);
                            log("INFO", "listenWaylineTaskEvent", "    [" + i + "] id=" + obs.getId() + ", type=" + obs.getType() + ", lat=" + obs.getLatitude() + ", lng=" + obs.getLongitude() + ", height=" + obs.getHeight());
                        }
                    }
                    log("INFO", "listenWaylineTaskEvent", "  created_at: " + td.getCreatedAt());
                    log("INFO", "listenWaylineTaskEvent", "  updated_at: " + td.getUpdatedAt());
                }
            });
        }

        log("INFO", "handleListenTaskEvents", "已注册任务事件监听（指令/指点飞行/航线），等待事件推送...");
        waitForKeyPress();
    }

    // [291] 循环拉流/断流测试
    private static void handleCyclicLiveStreamTest() {
        log("INFO", "cyclicLiveTest", "=== 循环拉流/断流测试 ===");

        System.out.print("请输入拉流持续时间（秒，默认5）: ");
        String pullInput = scanner.nextLine().trim();
        int pullDuration = pullInput.isEmpty() ? 5 : Integer.parseInt(pullInput);

        System.out.print("请输入断流后等待时间（秒，默认3）: ");
        String waitInput = scanner.nextLine().trim();
        int waitDuration = waitInput.isEmpty() ? 3 : Integer.parseInt(waitInput);

        System.out.print("请输入测试次数（默认3）: ");
        String countInput = scanner.nextLine().trim();
        int testCount = countInput.isEmpty() ? 3 : Integer.parseInt(countInput);

        log("INFO", "cyclicLiveTest", String.format("参数: 拉流 %d 秒, 断流等待 %d 秒, 测试 %d 次", pullDuration, waitDuration, testCount));

        Dock testDock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        if (testDock == null) {
            log("ERROR", "cyclicLiveTest", "获取 Dock 实例失败");
            return;
        }

        CountDownLatch rangeLatch = new CountDownLatch(1);
        final List<LiveInfo>[] availableRanges = new List[]{null};
        testDock.getLiveDeviceModule().listenLiveAvailableRange(ranges -> {
            if (ranges != null && !ranges.isEmpty()) {
                availableRanges[0] = ranges;
                rangeLatch.countDown();
            }
        });

        try {
            log("INFO", "cyclicLiveTest", "等待获取直播可用范围...");
            if (!rangeLatch.await(10, java.util.concurrent.TimeUnit.SECONDS)) {
                log("ERROR", "cyclicLiveTest", "获取直播可用范围超时");
                return;
            }
        } catch (InterruptedException e) {
            log("ERROR", "cyclicLiveTest", "等待中断");
            return;
        }

        log("INFO", "cyclicLiveTest", "直播可用范围: " + availableRanges[0].size() + " 个");

        int successCount = 0;
        int failCount = 0;

        for (int i = 1; i <= testCount; i++) {
            final int round = i;
            log("INFO", "cyclicLiveTest", String.format("========== 第 %d/%d 次循环 ==========", round, testCount));

            LiveModule module = testDock.getLiveStream(availableRanges[0].get(0));
            if (module == null) {
                log("ERROR", "cyclicLiveTest", "第 " + round + " 次: getLiveStream 返回 null");
                failCount++;
                continue;
            }

            // start
            CountDownLatch startLatch = new CountDownLatch(1);
            final boolean[] startSuccess = {false};
            log("INFO", "cyclicLiveTest", "第 " + round + " 次: 调用 start()...");
            module.start(err -> {
                startSuccess[0] = (err.getErrorCode() == com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.SUCCESS);
                log("INFO", "cyclicLiveTest", "第 " + round + " 次: start 回调, err_code=" + err.getErrorCode());
                startLatch.countDown();
            });

            try {
                startLatch.await(15, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log("ERROR", "cyclicLiveTest", "第 " + round + " 次: start 等待中断");
            }

            if (!startSuccess[0]) {
                log("ERROR", "cyclicLiveTest", "第 " + round + " 次: start 失败");
                failCount++;
                continue;
            }

            // subscribe
            final java.util.concurrent.atomic.AtomicInteger frameCount = new java.util.concurrent.atomic.AtomicInteger(0);
            int listenId = module.subscribeLiveStream((data, frameInfo) -> {
                int count = frameCount.incrementAndGet();
                if (count % 30 == 0) {
                    log("INFO", "cyclicLiveTest", "第 " + round + " 次: 收到视频帧 #" + count + ", 大小: " + data.length);
                }
            });
            log("INFO", "cyclicLiveTest", "第 " + round + " 次: subscribeLiveStream, listenId=" + listenId);

            // 等待拉流
            log("INFO", "cyclicLiveTest", "第 " + round + " 次: 拉流中，等待 " + pullDuration + " 秒...");
            try { Thread.sleep(pullDuration * 1000L); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            log("INFO", "cyclicLiveTest", "第 " + round + " 次: 共收到 " + frameCount.get() + " 帧");

            // unsubscribe
            module.unSubscribeLiveStream(listenId);
            log("INFO", "cyclicLiveTest", "第 " + round + " 次: unSubscribeLiveStream 完成");

            // stop
            log("INFO", "cyclicLiveTest", "第 " + round + " 次: 调用 stop()...");
            module.stop();
            log("INFO", "cyclicLiveTest", "第 " + round + " 次: stop() 返回");

            successCount++;

            if (round < testCount) {
                log("INFO", "cyclicLiveTest", "第 " + round + " 次: 断流等待 " + waitDuration + " 秒...");
                try { Thread.sleep(waitDuration * 1000L); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }

        log("INFO", "cyclicLiveTest", String.format("========== 测试完成: 成功 %d 次, 失败 %d 次 ==========", successCount, failCount));
    }

    /**
     * [126] 机载算法管理测试（PAAS 纯血场景）
     * 覆盖 7 个接口：获取算法列表、获取绑定设备、获取可操作设备统计、批量操作、
     *               创建上传凭证、处理上传文件、删除算法
     */
    private static void handleAIInsideTest() {
        log("INFO", "handleAIInsideTest", "机载算法管理测试（PAAS 纯血）");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleAIInsideTest", "获取 workspace 失败");
            return;
        }
        AIInsideManageModule aiModule = workspace.getAIInsideManageModule();
        if (aiModule == null) {
            log("ERROR", "handleAIInsideTest", "获取 AIInsideManageModule 失败");
            return;
        }

        System.out.println(" [1] 获取算法列表 (getAIInsideModels)");
        System.out.println(" [2] 获取算法绑定设备详情 (getAIInsideModelDevices)");
        System.out.println(" [3] 获取可操作设备数量统计 (getAIInsideModelDevicesSummary)");
        System.out.println(" [4] 批量下发装载指令 load (operateAIInsideModel)");
        System.out.println(" [5] 批量下发卸载指令 unload (operateAIInsideModel)");
        System.out.println(" [6] 创建算法上传凭证 (createAIInsideAlgorithm)");
        System.out.println(" [7] 处理已上传文件 (fileUploadAIInsideAlgorithm)");
         System.out.println(" [8] 删除算法 (deleteAIInsideAlgorithm)");
         System.out.println(" [9] 一站式上传算法文件 (uploadAlgorithmFile)");
        System.out.print(" 请输入选择: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // 消费换行

        switch (choice) {
            case 1: {
                // 获取算法列表
                log("INFO", "handleAIInsideTest", "调用 getAIInsideModels...");
                aiModule.getAIInsideModels(new FlightHubCallback.GetAIInsideModelsCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                         com.dji.flighthubsdk.bean.GetAIInsideModelsResult result) {
                        log("INFO", "handleAIInsideTest", "getAIInsideModels error: " + error);
                        if (result == null || result.getModels() == null) return;
                        log("INFO", "handleAIInsideTest", "算法总数: " + result.getModels().size());
                        for (int i = 0; i < result.getModels().size(); i++) {
                            com.dji.flighthubsdk.bean.AIInsideModelItem m = result.getModels().get(i);
                            log("INFO", "handleAIInsideTest",
                                    "  [" + i + "] signed_id: " + m.getSignedId()
                                    + ", model_name: " + m.getModelName()
                                    + ", signed_version: " + m.getSignedVersion()
                                    + ", sn_list.size: " + (m.getSnList() != null ? m.getSnList().size() : 0)
                                    + ", image_source: " + m.getImageSource()
                                    + ", expired_utc: " + m.getExpiredUtc());
                        }
                    }
                });
                break;
            }
            case 2: {
                // 获取算法绑定设备详情（分页）
                System.out.print(" 请输入 signed_id (算法签名ID): ");
                String signedId = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.GetAIInsideModelDevicesParam param =
                        new com.dji.flighthubsdk.bean.GetAIInsideModelDevicesParam();
                param.setSignedId(signedId.isEmpty() ? "algo_signed_001" : signedId);
                param.setPage(1);
                param.setPageSize(20);
                log("INFO", "handleAIInsideTest", "调用 getAIInsideModelDevices, signed_id=" + param.getSignedId());
                aiModule.getAIInsideModelDevices(param, new FlightHubCallback.GetAIInsideModelDevicesCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                         com.dji.flighthubsdk.bean.GetAIInsideModelDevicesResult result) {
                        log("INFO", "handleAIInsideTest",
                                "getAIInsideModelDevices error: " + error
                                + ", total: " + (result != null ? result.getTotal() : 0)
                                + ", page: " + (result != null ? result.getPage() : 0));
                        if (result == null || result.getList() == null) return;
                        for (int i = 0; i < result.getList().size(); i++) {
                            com.dji.flighthubsdk.bean.AIInsideDeviceItem dev = result.getList().get(i);
                            com.dji.flighthubsdk.bean.AIInsideAssociateDrone drone = dev.getAssociateDroneDeviceInfo();
                            log("INFO", "handleAIInsideTest",
                                    "  [" + i + "] dock_sn: " + dev.getDeviceSn()
                                    + ", online: " + dev.isDeviceOnlineStatus()
                                    + ", operate_status: " + (dev.getOperateAiModel() != null ? dev.getOperateAiModel().getStatus() : "")
                                    + ", operate_progress: " + (dev.getOperateAiModel() != null && dev.getOperateAiModel().getProgress() != null
                                            ? dev.getOperateAiModel().getProgress().getPercent() : 0)
                                    + ", drone_sn: " + (drone != null ? drone.getDeviceSn() : "")
                                    + ", ai_model_status: " + (drone != null && drone.getAiModelInfo() != null ? drone.getAiModelInfo().getStatus() : ""));
                        }
                    }
                });
                break;
            }
            case 3: {
                // 获取可操作设备数量统计
                System.out.print(" 请输入 signed_id (算法签名ID): ");
                String signedId = scanner.nextLine().trim();
                System.out.print(" 请输入 operation_type (load/update，勿填 0): ");
                String opType = scanner.nextLine().trim();
                if ("0".equals(opType)) {
                    log("WARN", "handleAIInsideTest",
                            "operation_type 不能为 0，Forge 要求 load|update，已改用 load");
                    opType = "load";
                }
                com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryParam summaryParam =
                        new com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryParam();
                summaryParam.setSignedId(signedId.isEmpty() ? "algo_signed_001" : signedId);
                summaryParam.setOperationType(opType.isEmpty() ? "load" : opType);
                log("INFO", "handleAIInsideTest",
                        "调用 getAIInsideModelDevicesSummary, signed_id=" + summaryParam.getSignedId()
                        + ", operation_type=" + summaryParam.getOperationType());
                aiModule.getAIInsideModelDevicesSummary(summaryParam, new FlightHubCallback.GetAIInsideModelDevicesSummaryCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                         com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryResult result) {
                        log("INFO", "handleAIInsideTest",
                                "getAIInsideModelDevicesSummary error: " + error
                                + ", total_devices: " + (result != null ? result.getTotalDevices() : 0)
                                + ", operable_devices: " + (result != null ? result.getOperableDevices() : 0));
                    }
                });
                break;
            }
            case 4: {
                // 批量下发装载指令 load
                System.out.print(" 请输入 signed_id (算法签名ID): ");
                String signedId = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.OperateAIInsideModelParam opParam =
                        new com.dji.flighthubsdk.bean.OperateAIInsideModelParam();
                opParam.setSignedId(signedId.isEmpty() ? "algo_signed_001" : signedId);
                opParam.setOperationType("load");
                // 指定 EA230，避免空列表全量下发
                opParam.setDroneSNList(java.util.Collections.singletonList("PLACEHOLDER_DRONE_SN_2"));
                log("INFO", "handleAIInsideTest",
                        "调用 operateAIInsideModel load, signed_id=" + opParam.getSignedId()
                        + ", drone_sn_list=[PLACEHOLDER_DRONE_SN_2]");
                aiModule.operateAIInsideModel(opParam, new FlightHubCallback.OperateAIInsideModelCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                         com.dji.flighthubsdk.bean.OperateAIInsideModelResult result) {
                        log("INFO", "handleAIInsideTest", "operateAIInsideModel(load) error: " + error);
                        if (result == null || result.getResults() == null) return;
                        for (com.dji.flighthubsdk.bean.AIInsideOperateDeviceResult r : result.getResults()) {
                            log("INFO", "handleAIInsideTest",
                                    "  drone_sn: " + r.getDroneSn() + ", code: " + r.getCode());
                        }
                    }
                });
                break;
            }
            case 5: {
                 // 批量下发卸载指令 unload
                 System.out.print(" 请输入 signed_id (算法签名ID): ");
                 String signedId = scanner.nextLine().trim();
                 com.dji.flighthubsdk.bean.OperateAIInsideModelParam opParam =
                         new com.dji.flighthubsdk.bean.OperateAIInsideModelParam();
                 opParam.setSignedId(signedId.isEmpty() ? "algo_signed_001" : signedId);
                 opParam.setOperationType("unload");
                 opParam.setDroneSNList(java.util.Collections.singletonList("PLACEHOLDER_DRONE_SN_2"));
                 log("INFO", "handleAIInsideTest",
                         "调用 operateAIInsideModel unload, signed_id=" + opParam.getSignedId());
                aiModule.operateAIInsideModel(opParam, new FlightHubCallback.OperateAIInsideModelCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                         com.dji.flighthubsdk.bean.OperateAIInsideModelResult result) {
                        log("INFO", "handleAIInsideTest", "operateAIInsideModel(unload) error: " + error);
                        if (result == null || result.getResults() == null) return;
                        for (com.dji.flighthubsdk.bean.AIInsideOperateDeviceResult r : result.getResults()) {
                            log("INFO", "handleAIInsideTest",
                                    "  drone_sn: " + r.getDroneSn() + ", code: " + r.getCode());
                        }
                    }
                });
                break;
            }
            case 6: {
                // 创建算法上传凭证
                System.out.print(" 请输入上传文件名 (如 fire_detection_v2.zip): ");
                String fileName = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmParam createParam =
                        new com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmParam(
                                fileName.isEmpty() ? "test_algorithm.zip" : fileName);
                log("INFO", "handleAIInsideTest",
                        "调用 createAIInsideAlgorithm, name=" + createParam.getName());
                aiModule.createAIInsideAlgorithm(createParam, new FlightHubCallback.CreateAIInsideAlgorithmCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                         com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmResult result) {
                        log("INFO", "handleAIInsideTest", "createAIInsideAlgorithm error: " + error);
                        if (result == null) return;
                        log("INFO", "handleAIInsideTest",
                                "  provider: " + result.getProvider()
                                + ", bucket: " + result.getBucket()
                                + ", region: " + result.getRegion()
                                + ", endpoint: " + result.getEndpoint()
                                + ", file_store_dir: " + result.getFileStoreDir());
                        if (result.getCredentials() != null) {
                            log("INFO", "handleAIInsideTest",
                                    "  credentials.access_key_id: " + result.getCredentials().getAccessKeyId()
                                    + ", expire: " + result.getCredentials().getExpire());
                        }
                    }
                });
                break;
            }
            case 7: {
                // 处理已上传文件
                System.out.print(" 请输入 file_key (MinIO 对象 Key): ");
                String fileKey = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmParam uploadParam =
                        new com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmParam(
                                fileKey.isEmpty() ? "ai_inside/algorithm/test-uuid/test_algorithm.zip" : fileKey);
                log("INFO", "handleAIInsideTest",
                        "调用 fileUploadAIInsideAlgorithm, file_key=" + uploadParam.getFileKey());
                aiModule.fileUploadAIInsideAlgorithm(uploadParam, new FlightHubCallback.FileUploadAIInsideAlgorithmCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                         com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmResult result) {
                        log("INFO", "handleAIInsideTest", "fileUploadAIInsideAlgorithm error: " + error);
                        com.dji.flighthubsdk.bean.AIInsideAlgorithmDetail detail =
                                result != null ? result.getDetail() : null;
                        if (detail == null) return;
                        log("INFO", "handleAIInsideTest",
                                "  signed_id: " + detail.getSignedId()
                                + ", signed_name: " + detail.getSignedName()
                                + ", signed_version: " + detail.getSignedVersion()
                                + ", model_name: " + detail.getModelName()
                                + ", model_type: " + detail.getModelType()
                                + ", file_key: " + detail.getFileKey());
                        if (detail.getLicense() != null) {
                            log("INFO", "handleAIInsideTest",
                                    "  license.sn_list.size: " + detail.getLicense().getSnList().size()
                                    + ", expired_utc: " + detail.getLicense().getExpiredUtc());
                        }
                    }
                });
                break;
            }
            case 8: {
                // 删除算法
                System.out.print(" 请输入 signed_id (算法签名ID): ");
                String signedId = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.DeleteAIInsideAlgorithmParam deleteParam =
                        new com.dji.flighthubsdk.bean.DeleteAIInsideAlgorithmParam(
                                signedId.isEmpty() ? "algo_signed_001" : signedId);
                log("INFO", "handleAIInsideTest",
                        "调用 deleteAIInsideAlgorithm, signed_id=" + deleteParam.getSignedId());
                aiModule.deleteAIInsideAlgorithm(deleteParam, new FlightHubCallback.DeleteAIInsideAlgorithmCallback() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.bean.FlightHubError error) {
                        log("INFO", "handleAIInsideTest",
                                "deleteAIInsideAlgorithm error: " + error
                                + (error != null && error.getErrorCode() != null
                                   && error.getErrorCode().toString().equals("SUCCESS") ? " ✓ 删除成功" : ""));
                    }
                });
                 break;
             }
             case 9: {
                 // 一站式上传算法文件（获取STS凭证→上传→通知后端入库）
                 System.out.print(" 请输入本地 ZIP 文件路径: ");
                 String filePath = scanner.nextLine().trim();
                 com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileParam uploadFileParam =
                         new com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileParam(
                                 filePath.isEmpty() ? "/tmp/test_algorithm.zip" : filePath);
                 log("INFO", "handleAIInsideTest",
                         "调用 uploadAlgorithmFile, file_path=" + uploadFileParam.getFilePath());
                 aiModule.uploadAlgorithmFile(uploadFileParam, new FlightHubCallback.UploadAlgorithmFileCallback() {
                     @Override
                     public void onResult(com.dji.flighthubsdk.bean.FlightHubError error,
                                          com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileResult result) {
                         log("INFO", "handleAIInsideTest", "uploadAlgorithmFile error: " + error);
                         if (result == null) return;
                         log("INFO", "handleAIInsideTest",
                                 "  object_key: " + result.getObjectKey()
                                 + ", object_url: " + result.getObjectUrl()
                                 + ", etag: " + result.getEtag());
                         com.dji.flighthubsdk.bean.AIInsideAlgorithmDetail detail = result.getDetail();
                         if (detail != null) {
                             log("INFO", "handleAIInsideTest",
                                     "  detail.signed_id: " + detail.getSignedId()
                                     + ", version: " + detail.getSignedVersion()
                                     + ", model_name: " + detail.getModelName()
                                     + ", file_key: " + detail.getFileKey());
                             if (detail.getLicense() != null) {
                                 log("INFO", "handleAIInsideTest",
                                         "  license.sn_list.size: " + detail.getLicense().getSnList().size()
                                         + ", expired_utc: " + detail.getLicense().getExpiredUtc());
                             }
                         }
                     }
                 });
                 break;
             }
             default:
                 System.out.println("无效输入");
         }
    }
    
    // [292] 一键起飞任务 - 获取任务列表
    private static void handleGetCommandTaskList() {
        log("INFO", "handleGetCommandTaskList", "=== 一键起飞任务 - 获取任务列表 ===");
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetCommandTaskList", "获取工作空间失败");
            waitForKeyPress();
            return;
        }
        com.dji.flighthubsdk.core.FlightTaskModule module = workspace.getFlightTaskModule();
        if (module == null) {
            log("ERROR", "handleGetCommandTaskList", "获取航线任务模块失败");
            waitForKeyPress();
            return;
        }

        com.dji.flighthubsdk.bean.GetCommandTaskListParams params =
            new com.dji.flighthubsdk.bean.GetCommandTaskListParams();
        params.setPage(1);
        params.setPageSize(10);
        if (selectedDevice != null) {
            String commandTaskSn = getMainDeviceSn(selectedDevice);
            if (commandTaskSn != null && !commandTaskSn.isEmpty()) {
                params.setSn(commandTaskSn);
            }
        }

        log("INFO", "handleGetCommandTaskList", "请求参数: page=" + params.getPage()
            + " pageSize=" + params.getPageSize()
            + " sn=" + (params.getSn().isEmpty() ? "(未选设备)" : params.getSn()));

        module.getCommandTaskList(params, new FlightHubCallback.GetCommandTaskListCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetCommandTaskListResults results) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetCommandTaskList", "getCommandTaskList 失败, error: " + error);
                    return;
                }
                if (results == null || results.getList() == null) {
                    log("WARN", "handleGetCommandTaskList", "结果为空");
                    return;
                }
                com.dji.flighthubsdk.bean.PaginationInfo pagination = results.getPagination();
                log("INFO", "handleGetCommandTaskList", "一键起飞任务列表 (本页 " + results.getList().size()
                    + " 条, total=" + (pagination != null ? pagination.getTotal() : 0)
                    + ", page=" + (pagination != null ? pagination.getPage() : 0)
                    + ", page_size=" + (pagination != null ? pagination.getPageSize() : 0) + ")");

                for (int i = 0; i < results.getList().size(); i++) {
                    com.dji.flighthubsdk.bean.CommandTaskListItemData t = results.getList().get(i);
                    log("INFO", "handleGetCommandTaskList", "  [" + (i + 1) + "]"
                        + " uuid=" + t.getUuid()
                        + ", sn=" + t.getSn()
                        + ", drone_sn=" + t.getDroneSn()
                        + ", status=" + t.getStatus()
                        + ", err_code=" + t.getErrCode()
                        + ", takeoff_mode=" + t.getTakeoffMode()
                        + ", security_takeoff_height=" + t.getSecurityTakeoffHeight()
                        + ", max_speed=" + t.getMaxSpeed()
                        + ", rth_altitude=" + t.getRthAltitude()
                        + ", created_at=" + t.getCreatedAt()
                        + ", user_id=" + t.getUserId());
                }
            }
        });
        waitForKeyPress();
    }

    // [293] 飞行记录 - 查询飞行任务轨迹
    private static void handleGetFlightTaskTrack() {
        log("INFO", "handleGetFlightTaskTrack", "=== 飞行记录 - 查询飞行任务轨迹 ===");
        Workspace workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleGetFlightTaskTrack", "获取工作空间失败");
            waitForKeyPress();
            return;
        }
        com.dji.flighthubsdk.core.FlightTaskModule module = workspace.getFlightTaskModule();
        if (module == null) {
            log("ERROR", "handleGetFlightTaskTrack", "获取航线任务模块失败");
            waitForKeyPress();
            return;
        }

        System.out.print("请输入任务UUID (task_uuid): ");
        String taskUuid = scanner.nextLine().trim();
        if (taskUuid.isEmpty()) {
            log("ERROR", "handleGetFlightTaskTrack", "task_uuid 不能为空");
            waitForKeyPress();
            return;
        }

        com.dji.flighthubsdk.bean.GetFlightTaskTrackParams params =
            new com.dji.flighthubsdk.bean.GetFlightTaskTrackParams();
        params.setTaskUuid(taskUuid);

        log("INFO", "handleGetFlightTaskTrack", "请求参数: taskUuid=" + params.getTaskUuid());

        module.getFlightTaskTrack(params, new FlightHubCallback.GetFlightTaskTrackCallback() {
            @Override
            public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.GetFlightTaskTrackResults results) {
                if (!error.isSuccess()) {
                    log("ERROR", "handleGetFlightTaskTrack", "getFlightTaskTrack 失败, error: " + error);
                    return;
                }
                if (results == null) {
                    log("WARN", "handleGetFlightTaskTrack", "结果为空");
                    return;
                }
                log("INFO", "handleGetFlightTaskTrack", "飞行任务轨迹查询结果:");
                log("INFO", "handleGetFlightTaskTrack", "  uuid=" + results.getUuid());
                log("INFO", "handleGetFlightTaskTrack", "  name=" + results.getName());
                log("INFO", "handleGetFlightTaskTrack", "  status=" + results.getStatus());
                log("INFO", "handleGetFlightTaskTrack", "  runAt=" + results.getRunAt());
                log("INFO", "handleGetFlightTaskTrack", "  completedAt=" + results.getCompletedAt());
                log("INFO", "handleGetFlightTaskTrack", "  waylineUuid=" + results.getWaylineUuid());

                com.dji.flighthubsdk.bean.FlightTrackInfo track = results.getTrack();
                if (track != null) {
                    log("INFO", "handleGetFlightTaskTrack", "  track.trackId=" + track.getTrackId());
                    log("INFO", "handleGetFlightTaskTrack", "  track.droneSn=" + track.getDroneSn());
                    log("INFO", "handleGetFlightTaskTrack", "  track.flightDistance=" + track.getFlightDistance() + " 米");
                    log("INFO", "handleGetFlightTaskTrack", "  track.flightDuration=" + track.getFlightDuration() + " 秒");
                    java.util.List<com.dji.flighthubsdk.bean.FlightTrackPoint> points = track.getPoints();
                    log("INFO", "handleGetFlightTaskTrack", "  track.points.size()=" + (points != null ? points.size() : 0));
                    if (points != null) {
                        int showCount = Math.min(points.size(), 5);
                        for (int i = 0; i < showCount; i++) {
                            com.dji.flighthubsdk.bean.FlightTrackPoint p = points.get(i);
                            log("INFO", "handleGetFlightTaskTrack", "    [" + i + "] timestamp=" + p.getTimestamp()
                                + ", lat=" + p.getLatitude()
                                + ", lng=" + p.getLongitude()
                                + ", height=" + p.getHeight());
                        }
                        if (points.size() > 5) {
                            log("INFO", "handleGetFlightTaskTrack", "    ... (共 " + points.size() + " 个轨迹点)");
                        }
                    }
                } else {
                    log("INFO", "handleGetFlightTaskTrack", "  track=null (无轨迹信息)");
                }
            }
        });
        waitForKeyPress();
    }

    // ==================== 爆闪灯 flash_led / flash-configs ====================

    private static final int FLASH_LED_UNIT_MS = 20;
    private static int flashLedListenId = -1;

    /** 司空匀速闪烁预设：color=4 黄, on=100ms off=100ms → period=5 */
    private static List<FlashLedPatternInfo> buildUniformFlashPatterns() {
        FlashLedSegment seg = new FlashLedSegment();
        seg.setColor(4);
        seg.setCount(1);
        seg.setOn(100);
        seg.setOff(100);
        seg.setInterval(0);
        return convertToDevicePatterns(Collections.singletonList(seg));
    }

    /**
     * 将爆闪灯片段转换为设备 patterns（对齐司空 convertToDevicePatterns，UNIT_MS=20）
     */
    private static List<FlashLedPatternInfo> convertToDevicePatterns(List<FlashLedSegment> segments) {
        List<FlashLedPatternInfo> patterns = new ArrayList<>();
        if (segments == null) {
            return patterns;
        }
        for (FlashLedSegment segment : segments) {
            int onPeriod = Math.max(1, Math.round((float) segment.getOn() / FLASH_LED_UNIT_MS));
            int offPeriod = Math.round((float) segment.getOff() / FLASH_LED_UNIT_MS);
            int intervalPeriod = Math.round((float) segment.getInterval() / FLASH_LED_UNIT_MS);
            for (int i = 0; i < segment.getCount(); i++) {
                boolean isLastLoop = (i == segment.getCount() - 1);
                FlashLedPatternInfo onPat = new FlashLedPatternInfo();
                onPat.setColor(segment.getColor());
                onPat.setPeriod(onPeriod);
                onPat.setBrightness(100);
                patterns.add(onPat);
                if (!isLastLoop) {
                    if (offPeriod > 0) {
                        FlashLedPatternInfo offPat = new FlashLedPatternInfo();
                        offPat.setColor(0);
                        offPat.setPeriod(offPeriod);
                        offPat.setBrightness(0);
                        patterns.add(offPat);
                    }
                } else {
                    int totalBlackPeriod = offPeriod + intervalPeriod;
                    if (totalBlackPeriod > 0) {
                        FlashLedPatternInfo offPat = new FlashLedPatternInfo();
                        offPat.setColor(0);
                        offPat.setPeriod(totalBlackPeriod);
                        offPat.setBrightness(0);
                        patterns.add(offPat);
                    }
                }
            }
        }
        return patterns;
    }

    // [318] EA610 基站三方外设直播：列路 → 开一路落盘 → 停旧 → 开另一路
    private static void handleEa610BaseStationLiveTest() {
        printSeparator("EA610 基站三方外设直播（列路→开一路→切另一路）");

        String bsSn = getBaseStationDeviceSn(selectedDevice);
        if (bsSn == null || bsSn.isEmpty()) {
            log("ERROR", "handleEa610BaseStationLiveTest",
                    "当前拓扑无 base_station，请选择含 EA610/O4 地面站的拓扑");
            waitForKeyPress();
            return;
        }

        BaseStation baseStation = getOrCreateBaseStation(bsSn);
        if (baseStation == null) {
            log("ERROR", "handleEa610BaseStationLiveTest", "getBaseStationInstance(" + bsSn + ") 失败");
            waitForKeyPress();
            return;
        }

        log("INFO", "handleEa610BaseStationLiveTest",
                "[318] 基站 SN=" + bsSn + "（开播 sn=基站 SN，不是摄像头 SN）");

        System.out.print("请输入每路落盘时长（秒，默认10）: ");
        int recordSeconds = 10;
        try {
            String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
            if (line.isEmpty() && scanner.hasNextLine()) {
                line = scanner.nextLine().trim();
            }
            if (!line.isEmpty()) {
                recordSeconds = Integer.parseInt(line);
            }
        } catch (Exception ignored) {
            recordSeconds = 10;
        }
        if (recordSeconds < 1) {
            recordSeconds = 1;
        }

        if (baseStation.getLiveDeviceModule() == null) {
            log("ERROR", "handleEa610BaseStationLiveTest", "getLiveDeviceModule 返回 null");
            waitForKeyPress();
            return;
        }

        final CountDownLatch rangeLatch = new CountDownLatch(1);
        final AtomicReference<List<LiveInfo>> rangesRef = new AtomicReference<>();
        final AtomicBoolean rangeReceived = new AtomicBoolean(false);
        int rangeListenId = baseStation.getLiveDeviceModule().listenLiveAvailableRange(ranges -> {
            if (!rangeReceived.compareAndSet(false, true)) {
                return;
            }
            rangesRef.set(ranges != null ? new ArrayList<>(ranges) : new ArrayList<>());
            rangeLatch.countDown();
        });

        log("INFO", "handleEa610BaseStationLiveTest", "等待 listenLiveAvailableRange...");
        try {
            if (!rangeLatch.await(15, TimeUnit.SECONDS)) {
                log("ERROR", "handleEa610BaseStationLiveTest", "获取直播可用范围超时");
                baseStation.getLiveDeviceModule().cancelListenLiveAvailableRange(rangeListenId);
                waitForKeyPress();
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            baseStation.getLiveDeviceModule().cancelListenLiveAvailableRange(rangeListenId);
            waitForKeyPress();
            return;
        }
        baseStation.getLiveDeviceModule().cancelListenLiveAvailableRange(rangeListenId);

        List<LiveInfo> allRanges = rangesRef.get() != null ? rangesRef.get() : new ArrayList<>();
        List<LiveInfo> bsRanges = new ArrayList<>();
        for (LiveInfo info : allRanges) {
            if (info != null && bsSn.equals(info.getDeviceSn())) {
                bsRanges.add(info);
            }
        }
        log("INFO", "handleEa610BaseStationLiveTest",
                "基站 live 可用范围: " + bsRanges.size() + "（全量 " + allRanges.size() + "）");
        for (int i = 0; i < bsRanges.size(); i++) {
            LiveInfo info = bsRanges.get(i);
            log("INFO", "handleEa610BaseStationLiveTest",
                    "[" + i + "] payload=" + info.getPayloadIndex() + " video=" + info.getVideoId());
        }

        if (bsRanges.isEmpty()) {
            log("ERROR", "handleEa610BaseStationLiveTest",
                    "基站无可用直播源（期望 166-0-0 / 166-0-1 等）");
            waitForKeyPress();
            return;
        }

        try {
            Files.createDirectories(Paths.get(outputDirectory));
        } catch (IOException e) {
            log("WARN", "handleEa610BaseStationLiveTest", "创建输出目录失败: " + e.getMessage());
        }

        int streamCount = Math.min(bsRanges.size(), 2);
        for (int i = 0; i < streamCount; i++) {
            LiveInfo liveInfo = bsRanges.get(i);
            String label = "cam[" + i + "] " + liveInfo.getPayloadIndex() + "/" + liveInfo.getVideoId();
            log("INFO", "handleEa610BaseStationLiveTest", "---------- 开播 " + label + " ----------");

            LiveModule liveModule = baseStation.getLiveStream(liveInfo);
            if (liveModule == null) {
                log("ERROR", "handleEa610BaseStationLiveTest", label + ": getLiveStream 失败");
                continue;
            }

            CountDownLatch startLatch = new CountDownLatch(1);
            AtomicBoolean startOk = new AtomicBoolean(false);
            liveModule.start(error -> {
                if (error != null && error.isSuccess()) {
                    startOk.set(true);
                    log("INFO", "handleEa610BaseStationLiveTest", label + ": 直播流启动成功");
                } else {
                    log("ERROR", "handleEa610BaseStationLiveTest",
                            label + ": 直播流启动失败, error=" + error);
                }
                startLatch.countDown();
            });
            try {
                if (!startLatch.await(30, TimeUnit.SECONDS) || !startOk.get()) {
                    log("ERROR", "handleEa610BaseStationLiveTest", label + ": start 超时或失败");
                    liveModule.stop();
                    continue;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                liveModule.stop();
                break;
            }

            String filename = generateTimestampFilename(
                    "dji_stream_bs_" + sanitizeFilename(liveInfo.getPayloadIndex()) + "_"
                            + sanitizeFilename(liveInfo.getVideoId()),
                    "h264");
            String filepath = outputDirectory + "/" + filename;
            final AtomicLong frameCount = new AtomicLong(0);
            FileOutputStream fos = null;
            int streamListenId = -1;
            try {
                fos = new FileOutputStream(filepath);
                final FileOutputStream out = fos;
                streamListenId = liveModule.subscribeLiveStream((data, frameInfo) -> {
                    long count = frameCount.incrementAndGet();
                    if (count % 30 == 0) {
                        log("INFO", "handleEa610BaseStationLiveTest",
                                label + ": 收到视频帧 #" + count + ", 大小: " + data.length);
                    }
                    try {
                        if (data != null && data.length > 0) {
                            out.write(data);
                        }
                    } catch (IOException e) {
                        // 不在回调中抛异常
                    }
                });

                log("INFO", "handleEa610BaseStationLiveTest",
                        label + ": 落盘中 " + recordSeconds + " 秒... file=" + filepath);
                Thread.sleep(recordSeconds * 1000L);
            } catch (Exception e) {
                log("ERROR", "handleEa610BaseStationLiveTest", label + ": 落盘异常: " + e.getMessage());
            } finally {
                if (streamListenId >= 0) {
                    liveModule.unSubscribeLiveStream(streamListenId);
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ignored) {
                    }
                }
                liveModule.stop();
                log("INFO", "handleEa610BaseStationLiveTest",
                        label + ": 停流完成, frames=" + frameCount.get() + ", file=" + filepath);
            }
        }

        log("INFO", "handleEa610BaseStationLiveTest", "========== [318] 完成 ==========");
        waitForKeyPress();
    }

    private static String sanitizeFilename(String value) {
        if (value == null) {
            return "unknown";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // [310] 监听 flash_led 状态
    private static void handleFlashLedListen() {
        printSeparator("爆闪灯 - 监听 flash_led 状态");
        if (drone == null) {
            log("ERROR", "handleFlashLedListen", "drone未初始化");
            waitForKeyPress();
            return;
        }
        FlightControlModule fc = drone.getFlightControlModule();
        if (fc == null) {
            log("ERROR", "handleFlashLedListen", "FlightControlModule null");
            waitForKeyPress();
            return;
        }
        if (flashLedListenId >= 0) {
            fc.cancelListenDrcFlashLed(flashLedListenId);
            log("INFO", "handleFlashLedListen", "已取消旧监听 id=" + flashLedListenId);
            flashLedListenId = -1;
        }
        flashLedListenId = fc.listenDrcFlashLed(info -> {
            log("INFO", "FLASH_LED", "on=" + info.getOn()
                    + " uuid=" + info.getPatternUuid()
                    + " name=" + info.getPatternName()
                    + " patterns=" + info.getPatterns());
        });
        log("INFO", "handleFlashLedListen", "监听已启动 id=" + flashLedListenId + "，按Enter取消...");
        waitForKeyPress();
        if (flashLedListenId >= 0) {
            fc.cancelListenDrcFlashLed(flashLedListenId);
            log("INFO", "handleFlashLedListen", "已取消监听 id=" + flashLedListenId);
            flashLedListenId = -1;
        }
    }

    // [311] 开启匀速闪烁预设
    private static void handleFlashLedSetOnPreset() {
        printSeparator("爆闪灯 - 开启(匀速闪烁预设)");
        if (drone == null) {
            log("ERROR", "handleFlashLedSetOnPreset", "drone未初始化");
            waitForKeyPress();
            return;
        }
        FlightControlModule fc = drone.getFlightControlModule();
        if (fc == null) {
            log("ERROR", "handleFlashLedSetOnPreset", "FlightControlModule null");
            waitForKeyPress();
            return;
        }
        FlashLedParams params = new FlashLedParams();
        params.setOn(1);
        params.setPatternUuid("preset-1");
        params.setPatternName("匀速闪烁");
        params.setPatterns(buildUniformFlashPatterns());
        log("INFO", "handleFlashLedSetOnPreset", "下发: " + params);
        fc.drcSetFlashLed(params, error -> log("INFO", "handleFlashLedSetOnPreset", "结果: " + error));
        waitForKeyPress();
    }

    // [312] 关闭爆闪灯
    private static void handleFlashLedSetOff() {
        printSeparator("爆闪灯 - 关闭");
        if (drone == null) {
            log("ERROR", "handleFlashLedSetOff", "drone未初始化");
            waitForKeyPress();
            return;
        }
        FlightControlModule fc = drone.getFlightControlModule();
        if (fc == null) {
            log("ERROR", "handleFlashLedSetOff", "FlightControlModule null");
            waitForKeyPress();
            return;
        }
        FlashLedParams params = new FlashLedParams();
        params.setOn(0);
        params.setPatternUuid("preset-1");
        params.setPatternName("匀速闪烁");
        params.setPatterns(buildUniformFlashPatterns());
        fc.drcSetFlashLed(params, error -> log("INFO", "handleFlashLedSetOff", "结果: " + error));
        waitForKeyPress();
    }

    // [313] 查询 flash-configs 列表
    private static void handleFlashLedConfigList() {
        printSeparator("爆闪灯 - 查询 flash-configs 列表");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleFlashLedConfigList", "workspace未初始化");
            waitForKeyPress();
            return;
        }
        FlashLedModule module = workspace.getFlashLedModule();
        if (module == null) {
            log("ERROR", "handleFlashLedConfigList", "FlashLedModule null");
            waitForKeyPress();
            return;
        }
        module.getFlashLedConfigList((error, list) -> {
            if (error == null || !error.isSuccess()) {
                log("ERROR", "handleFlashLedConfigList", "失败: " + error);
                return;
            }
            List<FlashLedConfig> configs = list != null ? list.getConfigs() : Collections.emptyList();
            log("INFO", "handleFlashLedConfigList", "共 " + configs.size() + " 个模板:");
            for (int i = 0; i < configs.size(); i++) {
                FlashLedConfig c = configs.get(i);
                log("INFO", "handleFlashLedConfigList",
                        "  [" + i + "] id=" + c.getId() + " name=" + c.getName()
                                + " segments=" + c.getFlashSegments());
            }
        });
        waitForKeyPress();
    }

    // [314] 创建 flash-config
    private static void handleFlashLedConfigCreate() {
        printSeparator("爆闪灯 - 创建 flash-config");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleFlashLedConfigCreate", "workspace未初始化");
            waitForKeyPress();
            return;
        }
        FlashLedModule module = workspace.getFlashLedModule();
        if (module == null) {
            log("ERROR", "handleFlashLedConfigCreate", "FlashLedModule null");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入配置名称(默认 java-demo-匀速闪烁): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "java-demo-匀速闪烁";
        }
        FlashLedSegment seg = new FlashLedSegment();
        seg.setColor(4);
        seg.setCount(1);
        seg.setOn(100);
        seg.setOff(100);
        // 204 服务端 Interval binding:"required"，0 会被当成空；HTTP 侧至少传 1（对齐 cpp Demo）
        seg.setInterval(1);
        FlashLedConfigParams params = new FlashLedConfigParams();
        params.setName(name);
        params.setFlashSegments(Collections.singletonList(seg));
        module.createFlashLedConfig(params, (error, config) -> {
            if (error == null || !error.isSuccess()) {
                log("ERROR", "handleFlashLedConfigCreate", "失败: " + error);
            } else {
                log("INFO", "handleFlashLedConfigCreate", "创建成功: " + config);
            }
        });
        waitForKeyPress();
    }

    // [315] 更新 flash-config
    private static void handleFlashLedConfigUpdate() {
        printSeparator("爆闪灯 - 更新 flash-config");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleFlashLedConfigUpdate", "workspace未初始化");
            waitForKeyPress();
            return;
        }
        FlashLedModule module = workspace.getFlashLedModule();
        if (module == null) {
            log("ERROR", "handleFlashLedConfigUpdate", "FlashLedModule null");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入 config_id（LIST 返回的 uuid，不要填 name）: ");
        String configId = scanner.nextLine().trim();
        System.out.print("请输入新名称: ");
        String name = scanner.nextLine().trim();
        FlashLedSegment seg = new FlashLedSegment();
        seg.setColor(1);
        seg.setCount(2);
        seg.setOn(60);
        seg.setOff(40);
        seg.setInterval(1);
        FlashLedConfigParams params = new FlashLedConfigParams();
        params.setName(name.isEmpty() ? "java-demo-updated" : name);
        params.setFlashSegments(Collections.singletonList(seg));
        module.updateFlashLedConfig(configId, params, (error, config) -> {
            if (error == null || !error.isSuccess()) {
                log("ERROR", "handleFlashLedConfigUpdate", "失败: " + error);
            } else {
                log("INFO", "handleFlashLedConfigUpdate", "更新成功: " + config);
            }
        });
        waitForKeyPress();
    }

    // [316] 删除 flash-config
    private static void handleFlashLedConfigDelete() {
        printSeparator("爆闪灯 - 删除 flash-config");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleFlashLedConfigDelete", "workspace未初始化");
            waitForKeyPress();
            return;
        }
        FlashLedModule module = workspace.getFlashLedModule();
        if (module == null) {
            log("ERROR", "handleFlashLedConfigDelete", "FlashLedModule null");
            waitForKeyPress();
            return;
        }
        System.out.print("请输入 config_id（LIST 返回的 uuid，不要填 name）: ");
        String configId = scanner.nextLine().trim();
        module.deleteFlashLedConfig(configId, error -> {
            if (error == null || !error.isSuccess()) {
                log("ERROR", "handleFlashLedConfigDelete", "失败: " + error);
            } else {
                log("INFO", "handleFlashLedConfigDelete", "删除成功");
            }
        });
        waitForKeyPress();
    }

    // [317] 应用模板：list → 选中 → convertToDevicePatterns → setFlashLed
    private static void handleFlashLedApplyTemplate() {
        printSeparator("爆闪灯 - 应用模板");
        if (drone == null) {
            log("ERROR", "handleFlashLedApplyTemplate", "drone未初始化");
            waitForKeyPress();
            return;
        }
        FlightControlModule fc = drone.getFlightControlModule();
        if (fc == null) {
            log("ERROR", "handleFlashLedApplyTemplate", "FlightControlModule null");
            waitForKeyPress();
            return;
        }
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleFlashLedApplyTemplate", "workspace未初始化");
            waitForKeyPress();
            return;
        }
        FlashLedModule module = workspace.getFlashLedModule();
        if (module == null) {
            log("ERROR", "handleFlashLedApplyTemplate", "FlashLedModule null");
            waitForKeyPress();
            return;
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final List<FlashLedConfig> configsHolder = new ArrayList<>();
        final AtomicReference<com.dji.flighthubsdk.bean.FlightHubError> errHolder = new AtomicReference<>();
        module.getFlashLedConfigList((error, list) -> {
            errHolder.set(error);
            if (list != null && list.getConfigs() != null) {
                configsHolder.addAll(list.getConfigs());
            }
            latch.countDown();
        });
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("ERROR", "handleFlashLedApplyTemplate", "等待列表超时/中断");
            waitForKeyPress();
            return;
        }
        if (errHolder.get() == null || !errHolder.get().isSuccess()) {
            log("ERROR", "handleFlashLedApplyTemplate", "列表失败: " + errHolder.get());
            waitForKeyPress();
            return;
        }
        if (configsHolder.isEmpty()) {
            log("WARN", "handleFlashLedApplyTemplate", "无模板，可先用[314]创建");
            waitForKeyPress();
            return;
        }
        for (int i = 0; i < configsHolder.size(); i++) {
            FlashLedConfig c = configsHolder.get(i);
            log("INFO", "handleFlashLedApplyTemplate",
                    "  [" + i + "] id=" + c.getId() + " name=" + c.getName());
        }
        System.out.print("请输入要应用的模板序号: ");
        int idx;
        try {
            idx = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            log("ERROR", "handleFlashLedApplyTemplate", "序号无效");
            waitForKeyPress();
            return;
        }
        if (idx < 0 || idx >= configsHolder.size()) {
            log("ERROR", "handleFlashLedApplyTemplate", "序号越界");
            waitForKeyPress();
            return;
        }
        FlashLedConfig selected = configsHolder.get(idx);
        List<FlashLedPatternInfo> patterns = convertToDevicePatterns(selected.getFlashSegments());
        FlashLedParams params = new FlashLedParams();
        params.setOn(1);
        params.setPatternUuid(selected.getId());
        params.setPatternName(selected.getName());
        params.setPatterns(patterns);
        log("INFO", "handleFlashLedApplyTemplate", "下发: " + params);
        fc.drcSetFlashLed(params, error -> log("INFO", "handleFlashLedApplyTemplate", "结果: " + error));
        waitForKeyPress();
    }

    // ==================== DRC 高级功能测试 ====================

    private static int drcOsdListenId = -1;
    private static int drcBatteryListenId = -1;
    private static int drcDelayListenId = -1;

    private static void handleDrcCameraApertureSet() {
        log("INFO", "handleDrcCameraApertureSet", "=== DRC - 相机光圈设置 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraApertureSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraApertureSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index (如 39-0-7): ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入镜头类型 (0=广角 1=变焦 2=红外): ");
        int lensType = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("请输入光圈值 (如 280=F2.8, 400=F4.0): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraApertureValueSetParams params = new com.dji.flighthubsdk.bean.CameraApertureValueSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(lensType);
        params.setApertureValue(value);
        camera.drcSetCameraApertureValue(params, error -> {
            log("INFO", "handleDrcCameraApertureSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCameraIsoSet() {
        log("INFO", "handleDrcCameraIsoSet", "=== DRC - 相机ISO设置 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraIsoSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraIsoSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入镜头类型: ");
        int lensType = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("请输入ISO值 (如 100, 200, 400, 800): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraIsoSetParams params = new com.dji.flighthubsdk.bean.CameraIsoSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(lensType);
        params.setIsoValue(value);
        camera.drcSetCameraIso(params, error -> {
            log("INFO", "handleDrcCameraIsoSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCameraShutterSet() {
        log("INFO", "handleDrcCameraShutterSet", "=== DRC - 相机快门设置 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraShutterSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraShutterSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入镜头类型: ");
        int lensType = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("请输入快门值 (如 500=1/500s): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraShutterSetParams params = new com.dji.flighthubsdk.bean.CameraShutterSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(lensType);
        params.setShutterValue(value);
        camera.drcSetCameraShutter(params, error -> {
            log("INFO", "handleDrcCameraShutterSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCameraExposureModeSet() {
        log("INFO", "handleDrcCameraExposureModeSet", "=== DRC - 相机曝光模式设置 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraExposureModeSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraExposureModeSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入镜头类型: ");
        int lensType = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("请输入曝光模式 (0=自动 1=快门优先 2=光圈优先 3=手动): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraExposureModeSetParams params = new com.dji.flighthubsdk.bean.CameraExposureModeSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(lensType);
        params.setExposureMode(value);
        camera.drcSetCameraExposureMode(params, error -> {
            log("INFO", "handleDrcCameraExposureModeSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCameraFocusModeSet() {
        log("INFO", "handleDrcCameraFocusModeSet", "=== DRC - 相机对焦模式设置 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraFocusModeSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraFocusModeSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入镜头类型: ");
        int lensType = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("请输入对焦模式 (0=MF 1=AFC 2=AFS): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraFocusModeSetParams params = new com.dji.flighthubsdk.bean.CameraFocusModeSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(lensType);
        params.setFocusMode(value);
        camera.drcSetCameraFocusMode(params, error -> {
            log("INFO", "handleDrcCameraFocusModeSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCameraPointFocus() {
        log("INFO", "handleDrcCameraPointFocus", "=== DRC - 相机点对焦 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraPointFocus", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraPointFocus", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入镜头类型: ");
        int lensType = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("请输入对焦点 x (0.0~1.0): ");
        double x = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入对焦点 y (0.0~1.0): ");
        double y = Double.parseDouble(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraPointFocusActionParams params = new com.dji.flighthubsdk.bean.CameraPointFocusActionParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(lensType);
        params.setX(x);
        params.setY(y);
        camera.drcCameraPointFocusAction(params, error -> {
            log("INFO", "handleDrcCameraPointFocus", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCameraDewarpingSet() {
        log("INFO", "handleDrcCameraDewarpingSet", "=== DRC - 相机畸变矫正 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraDewarpingSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraDewarpingSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入镜头类型: ");
        int lensType = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("请输入畸变矫正状态 (0=关 1=开): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraDewarpingSetParams params = new com.dji.flighthubsdk.bean.CameraDewarpingSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(lensType);
        params.setDewarpingState(value);
        camera.drcSetCameraDewarping(params, error -> {
            log("INFO", "handleDrcCameraDewarpingSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCameraScreenSplit() {
        log("INFO", "handleDrcCameraScreenSplit", "=== DRC - 相机分屏显示 ===");
        if (drone == null) { log("ERROR", "handleDrcCameraScreenSplit", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCameraScreenSplit", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入开关 (1=开 0=关): ");
        boolean enable = "1".equals(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.CameraScreenSplitParams params = new com.dji.flighthubsdk.bean.CameraScreenSplitParams();
        params.setPayloadIndex(payloadIndex);
        params.setEnable(enable);
        camera.drcSetCameraScreenSplit(params, error -> {
            log("INFO", "handleDrcCameraScreenSplit", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcCleanFog() {
        log("INFO", "handleDrcCleanFog", "=== DRC - 相机除雾 ===");
        if (drone == null) { log("ERROR", "handleDrcCleanFog", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcCleanFog", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        com.dji.flighthubsdk.bean.CleanFogParams params = new com.dji.flighthubsdk.bean.CleanFogParams();
        params.setPayloadIndex(payloadIndex);
        camera.drcCleanFog(params, error -> {
            log("INFO", "handleDrcCleanFog", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcThermalPaletteSet() {
        log("INFO", "handleDrcThermalPaletteSet", "=== DRC - 红外调色板设置 ===");
        if (drone == null) { log("ERROR", "handleDrcThermalPaletteSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcThermalPaletteSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入调色板样式 (0=白热 1=黑热 ...): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.ThermalCurrentPaletteSetParams params = new com.dji.flighthubsdk.bean.ThermalCurrentPaletteSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setPaletteStyle(value);
        camera.drcSetThermalCurrentPalette(params, error -> {
            log("INFO", "handleDrcThermalPaletteSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcIrMeteringModeSet() {
        log("INFO", "handleDrcIrMeteringModeSet", "=== DRC - 红外测温模式设置 ===");
        if (drone == null) { log("ERROR", "handleDrcIrMeteringModeSet", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcIrMeteringModeSet", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入测温模式 (0=关闭 1=点测温 2=区域测温): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.IrMeteringModeSetParams params = new com.dji.flighthubsdk.bean.IrMeteringModeSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setMode(value);
        camera.drcSetIrMeteringMode(params, error -> {
            log("INFO", "handleDrcIrMeteringModeSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcIsothermEnable() {
        log("INFO", "handleDrcIsothermEnable", "=== DRC - 红外等温线开关 ===");
        if (drone == null) { log("ERROR", "handleDrcIsothermEnable", "drone未初始化"); waitForKeyPress(); return; }
        CameraControlModule camera = drone.getCameraControlModule();
        if (camera == null) { log("ERROR", "handleDrcIsothermEnable", "CameraControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入payload_index: ");
        String payloadIndex = scanner.nextLine().trim();
        System.out.print("请输入开关 (0=关 1=开): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.SetIsothermEnableParams params = new com.dji.flighthubsdk.bean.SetIsothermEnableParams();
        params.setPayloadIndex(payloadIndex);
        params.setEnable(value);
        camera.drcSetIsothermEnable(params, error -> {
            log("INFO", "handleDrcIsothermEnable", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcAiSpotlightZoomSet() {
        log("INFO", "handleDrcAiSpotlightZoomSet", "=== DRC - AI跟踪开启/关闭 ===");
        if (drone == null) { log("ERROR", "handleDrcAiSpotlightZoomSet", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleDrcAiSpotlightZoomSet", "OnboardAiModule null"); waitForKeyPress(); return; }
        System.out.print("请输入 (1=开启 0=关闭): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        aiModule.DrcAISpotlightZoomSet(value, error -> {
            log("INFO", "handleDrcAiSpotlightZoomSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcAiSpotlightZoomSelect() {
        log("INFO", "handleDrcAiSpotlightZoomSelect", "=== DRC - AI跟踪选择目标 ===");
        if (drone == null) { log("ERROR", "handleDrcAiSpotlightZoomSelect", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleDrcAiSpotlightZoomSelect", "OnboardAiModule null"); waitForKeyPress(); return; }
        System.out.print("请输入 center_x (0.0~1.0): ");
        double cx = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入 center_y (0.0~1.0): ");
        double cy = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入 width (0.0~1.0): ");
        double w = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("请输入 height (0.0~1.0): ");
        double h = Double.parseDouble(scanner.nextLine().trim());
        com.dji.flighthubsdk.bean.AiSpotlightZoomSelectParams params = new com.dji.flighthubsdk.bean.AiSpotlightZoomSelectParams();
        params.setCenterX(cx);
        params.setCenterY(cy);
        params.setWidth(w);
        params.setHeight(h);
        aiModule.DrcAISpotlightZoomSelect(params, error -> {
            log("INFO", "handleDrcAiSpotlightZoomSelect", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcAiSpotlightZoomStop() {
        log("INFO", "handleDrcAiSpotlightZoomStop", "=== DRC - AI跟踪停止 ===");
        if (drone == null) { log("ERROR", "handleDrcAiSpotlightZoomStop", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleDrcAiSpotlightZoomStop", "OnboardAiModule null"); waitForKeyPress(); return; }
        aiModule.DrcAISpotlightZoomStop(error -> {
            log("INFO", "handleDrcAiSpotlightZoomStop", "结果: " + error);
        });
        waitForKeyPress();
    }

    /** 读取 AI 2.0 公共入参：index + light_type */
    private static int[] readAiIndexLightType() {
        System.out.print("模型 index (官方0, 三方>=128, 默认0): ");
        String idxStr = scanner.nextLine().trim();
        int index = idxStr.isEmpty() ? 0 : Integer.parseInt(idxStr);
        System.out.print("light_type (0无效/1广角/2变焦/3红外, 默认1): ");
        String ltStr = scanner.nextLine().trim();
        int lightType = ltStr.isEmpty() ? 1 : Integer.parseInt(ltStr);
        return new int[]{index, lightType};
    }

    private static void handleAiIdentifyFilterSet() {
        log("INFO", "handleAiIdentifyFilterSet", "=== 机载AI 2.0 过滤设置 ===");
        if (drone == null) { log("ERROR", "handleAiIdentifyFilterSet", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleAiIdentifyFilterSet", "OnboardAiModule null"); waitForKeyPress(); return; }
        try {
            int[] il = readAiIndexLightType();
            System.out.print("filters (逗号分隔, 如 0,1): ");
            String fStr = scanner.nextLine().trim();
            com.dji.flighthubsdk.generated.IntVector filters = new com.dji.flighthubsdk.generated.IntVector();
            if (!fStr.isEmpty()) {
                for (String p : fStr.split(",")) {
                    filters.add(Integer.parseInt(p.trim()));
                }
            }
            AiIdentifyFilterSetParam param = new AiIdentifyFilterSetParam();
            param.setIndex(il[0]);
            param.setLight_type(il[1]);
            param.setFilters(filters);
            log("INFO", "handleAiIdentifyFilterSet", "index=" + il[0] + " light_type=" + il[1] + " filters=" + fStr);
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            aiModule.AIIdentifyFilterSet(param, error -> {
                log("INFO", "handleAiIdentifyFilterSet", "结果: " + error);
                latch.countDown();
            });
            try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log("ERROR", "handleAiIdentifyFilterSet", e.getMessage());
        }
        waitForKeyPress();
    }

    private static void handleAiIdentifyScoreSet() {
        log("INFO", "handleAiIdentifyScoreSet", "=== 机载AI 2.0 置信度设置 ===");
        if (drone == null) { log("ERROR", "handleAiIdentifyScoreSet", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleAiIdentifyScoreSet", "OnboardAiModule null"); waitForKeyPress(); return; }
        try {
            int[] il = readAiIndexLightType();
            System.out.print("score (0-100): ");
            int score = Integer.parseInt(scanner.nextLine().trim());
            AiIdentifyScoreSetParam param = new AiIdentifyScoreSetParam();
            param.setIndex(il[0]);
            param.setLight_type(il[1]);
            param.setScore(score);
            log("INFO", "handleAiIdentifyScoreSet", "index=" + il[0] + " light_type=" + il[1] + " score=" + score);
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            aiModule.AIIdentifyScoreSet(param, error -> {
                log("INFO", "handleAiIdentifyScoreSet", "结果: " + error);
                latch.countDown();
            });
            try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log("ERROR", "handleAiIdentifyScoreSet", e.getMessage());
        }
        waitForKeyPress();
    }

    private static void handleAiIdentifyScoreModeSet() {
        log("INFO", "handleAiIdentifyScoreModeSet", "=== 机载AI 2.0 置信度模式 ===");
        if (drone == null) { log("ERROR", "handleAiIdentifyScoreModeSet", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleAiIdentifyScoreModeSet", "OnboardAiModule null"); waitForKeyPress(); return; }
        try {
            int[] il = readAiIndexLightType();
            System.out.print("score_mode (0无效/1计数/2搜救/3自定义): ");
            int mode = Integer.parseInt(scanner.nextLine().trim());
            AiIdentifyScoreModeSetParam param = new AiIdentifyScoreModeSetParam();
            param.setIndex(il[0]);
            param.setLight_type(il[1]);
            param.setScore_mode(mode);
            log("INFO", "handleAiIdentifyScoreModeSet", "index=" + il[0] + " light_type=" + il[1] + " score_mode=" + mode);
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            aiModule.AIIdentifyScoreModeSet(param, error -> {
                log("INFO", "handleAiIdentifyScoreModeSet", "结果: " + error);
                latch.countDown();
            });
            try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log("ERROR", "handleAiIdentifyScoreModeSet", e.getMessage());
        }
        waitForKeyPress();
    }

    private static void handleAiIdentifyScoreReset() {
        log("INFO", "handleAiIdentifyScoreReset", "=== 机载AI 2.0 置信度重置 ===");
        if (drone == null) { log("ERROR", "handleAiIdentifyScoreReset", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleAiIdentifyScoreReset", "OnboardAiModule null"); waitForKeyPress(); return; }
        try {
            int[] il = readAiIndexLightType();
            AiIdentifyScoreResetParam param = new AiIdentifyScoreResetParam();
            param.setIndex(il[0]);
            param.setLight_type(il[1]);
            log("INFO", "handleAiIdentifyScoreReset", "index=" + il[0] + " light_type=" + il[1]);
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            aiModule.AIIdentifyScoreReset(param, error -> {
                log("INFO", "handleAiIdentifyScoreReset", "结果: " + error);
                latch.countDown();
            });
            try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log("ERROR", "handleAiIdentifyScoreReset", e.getMessage());
        }
        waitForKeyPress();
    }

    private static int aiDataInfoListenId = -1;

    private static void handleListenAIDataInfo() {
        log("INFO", "handleListenAIDataInfo", "=== 机载AI 2.0 - 监听 AiDataInfo ===");
        if (drone == null) { log("ERROR", "handleListenAIDataInfo", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleListenAIDataInfo", "OnboardAiModule null"); waitForKeyPress(); return; }
        if (aiDataInfoListenId >= 0) {
            aiModule.cancelListenAIDataInfo(aiDataInfoListenId);
            log("INFO", "handleListenAIDataInfo", "已取消旧监听 id=" + aiDataInfoListenId);
            aiDataInfoListenId = -1;
        }
        aiDataInfoListenId = aiModule.listenAIDataInfo(info -> {
            if (info == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("identify_on=").append(info.getIdentify_on());
            com.dji.flighthubsdk.generated.AiModelsStatusItemVector statusVec = info.getSelected_ai_models_status();
            if (statusVec != null) {
                sb.append(" status_cnt=").append(statusVec.size());
                for (int i = 0; i < statusVec.size(); i++) {
                    com.dji.flighthubsdk.generated.AiModelsStatusItem s = statusVec.get(i);
                    if (s == null) continue;
                    sb.append(" [").append(i).append("]idx=").append(s.getIndex())
                      .append(" lt=").append(s.getLight_type())
                      .append(" score=").append(s.getScore())
                      .append(" mode=").append(s.getScore_mode());
                }
            }
            com.dji.flighthubsdk.generated.AiModelsConfigSetVector cfgVec = info.getAi_models_config();
            if (cfgVec != null) {
                sb.append(" config_cnt=").append(cfgVec.size());
            }
            log("INFO", "handleListenAIDataInfo", sb.toString());
        });
        log("INFO", "handleListenAIDataInfo", "监听已启动 id=" + aiDataInfoListenId + "（再按525可取消）");
        waitForKeyPress();
    }

    private static void handleAiModelsConfig() {
        log("INFO", "handleAiModelsConfig", "=== 机载AI 2.0 ModelsConfig 开/关 ===");
        if (drone == null) { log("ERROR", "handleAiModelsConfig", "drone未初始化"); waitForKeyPress(); return; }
        OnboardAiModule aiModule = drone.getOnboardAiModule();
        if (aiModule == null) { log("ERROR", "handleAiModelsConfig", "OnboardAiModule null"); waitForKeyPress(); return; }
        try {
            System.out.print("开识别? (1开/0关, 默认1): ");
            String onStr = scanner.nextLine().trim();
            boolean open = onStr.isEmpty() || "1".equals(onStr);
            System.out.print("stream_type (1广角/2变焦/3红外, 默认1): ");
            String stStr = scanner.nextLine().trim();
            int streamType = stStr.isEmpty() ? 1 : Integer.parseInt(stStr);
            com.dji.flighthubsdk.bean.AiModelsConfigParams params = new com.dji.flighthubsdk.bean.AiModelsConfigParams();
            if (open) {
                com.dji.flighthubsdk.bean.AiModelsConfigSet set = new com.dji.flighthubsdk.bean.AiModelsConfigSet();
                set.setStreamType(streamType);
                java.util.List<Integer> models = new java.util.ArrayList<>();
                models.add(0);
                set.setModels(models);
                java.util.List<com.dji.flighthubsdk.bean.AiModelsConfigSet> list = new java.util.ArrayList<>();
                list.add(set);
                params.setAiModelsConfig(list);
            }
            log("INFO", "handleAiModelsConfig", "open=" + open + " stream_type=" + streamType + " models=[0]");
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            aiModule.DrcAIModelsConfig(params, error -> {
                log("INFO", "handleAiModelsConfig", "结果: " + error);
                latch.countDown();
            });
            try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log("ERROR", "handleAiModelsConfig", e.getMessage());
        }
        waitForKeyPress();
    }

    private static void handleDrcDroneHoverSet() {
        log("INFO", "handleDrcDroneHoverSet", "=== DRC - 悬停设置 ===");
        if (drone == null) { log("ERROR", "handleDrcDroneHoverSet", "drone未初始化"); waitForKeyPress(); return; }
        FlightControlModule fc = drone.getFlightControlModule();
        if (fc == null) { log("ERROR", "handleDrcDroneHoverSet", "FlightControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入悬停时长(毫秒, 0=取消保活, 最大1800000): ");
        int duration = Integer.parseInt(scanner.nextLine().trim());
        fc.drcSetDroneHover(duration, error -> {
            log("INFO", "handleDrcDroneHoverSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcNightLightsSet() {
        log("INFO", "handleDrcNightLightsSet", "=== DRC - 夜航灯设置 ===");
        if (drone == null) { log("ERROR", "handleDrcNightLightsSet", "drone未初始化"); waitForKeyPress(); return; }
        FlightControlModule fc = drone.getFlightControlModule();
        if (fc == null) { log("ERROR", "handleDrcNightLightsSet", "FlightControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入夜航灯状态 (0=关 1=开): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        fc.drcSetNightLightsState(value, error -> {
            log("INFO", "handleDrcNightLightsSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcStealthStateSet() {
        log("INFO", "handleDrcStealthStateSet", "=== DRC - 隐身模式设置 ===");
        if (drone == null) { log("ERROR", "handleDrcStealthStateSet", "drone未初始化"); waitForKeyPress(); return; }
        FlightControlModule fc = drone.getFlightControlModule();
        if (fc == null) { log("ERROR", "handleDrcStealthStateSet", "FlightControlModule null"); waitForKeyPress(); return; }
        System.out.print("请输入隐身模式状态 (0=关 1=开): ");
        int value = Integer.parseInt(scanner.nextLine().trim());
        fc.drcSetStealthState(value, error -> {
            log("INFO", "handleDrcStealthStateSet", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleDrcListenPushData() {
        log("INFO", "handleDrcListenPushData", "=== DRC - 监听OSD/电池/延时推送 ===");
        if (drone == null) { log("ERROR", "handleDrcListenPushData", "drone未初始化"); waitForKeyPress(); return; }
        FlightControlModule fc = drone.getFlightControlModule();
        DrcModule drcMod = drone.getDrcModule();

        if (fc != null) {
            drcOsdListenId = fc.listenDrcOsdInfo(info -> {
                log("INFO", "DRC_PUSH", String.format("OSD: lat=%.2f lng=%.2f alt=%.1f hSpd=%.1f",
                    info.getLatitude(), info.getLongitude(), info.getHeight(), info.getHorizontal_speed()));
            });
            log("INFO", "handleDrcListenPushData", "OSD监听ID=" + drcOsdListenId);

            drcBatteryListenId = fc.listenDrcBatteriesInfo(info -> {
                log("INFO", "DRC_PUSH", String.format("Battery: %d%% remain=%ds",
                    info.getCapacity_percent(), info.getRemain_flight_time()));
            });
            log("INFO", "handleDrcListenPushData", "电池监听ID=" + drcBatteryListenId);
        }

        if (drcMod != null) {
            drcDelayListenId = drcMod.listenDrcDelayInfo(info -> {
                log("INFO", "DRC_PUSH", String.format("Delay: stick=%dms sdr_cmd=%dms",
                    info.getStick_delay(), info.getSdr_cmd_delay()));
            });
            log("INFO", "handleDrcListenPushData", "延时监听ID=" + drcDelayListenId);
        }

        log("INFO", "handleDrcListenPushData", "推送监听已注册。使用[519]停止。");
        waitForKeyPress();
    }

    private static void handleDrcStopListenPushData() {
        log("INFO", "handleDrcStopListenPushData", "=== DRC - 停止监听推送 ===");
        if (drone == null) { log("ERROR", "handleDrcStopListenPushData", "drone未初始化"); waitForKeyPress(); return; }
        FlightControlModule fc = drone.getFlightControlModule();
        DrcModule drcMod = drone.getDrcModule();

        if (fc != null && drcOsdListenId >= 0) {
            fc.cancelListenDrcOsdInfo(drcOsdListenId);
            log("INFO", "handleDrcStopListenPushData", "已取消OSD监听");
            drcOsdListenId = -1;
        }
        if (fc != null && drcBatteryListenId >= 0) {
            fc.cancelListenDrcBatteriesInfo(drcBatteryListenId);
            log("INFO", "handleDrcStopListenPushData", "已取消电池监听");
            drcBatteryListenId = -1;
        }
        if (drcMod != null && drcDelayListenId >= 0) {
            drcMod.cancelListenDrcDelayInfo(drcDelayListenId);
            log("INFO", "handleDrcStopListenPushData", "已取消延时监听");
            drcDelayListenId = -1;
        }
        waitForKeyPress();
    }

    private static void handleDrcRtcQualityTest() {
        log("INFO", "handleDrcRtcQualityTest", "=== DRC - RTC质量测试 ===");
        if (drone == null) { log("ERROR", "handleDrcRtcQualityTest", "drone未初始化"); waitForKeyPress(); return; }
        DrcModule drcMod = drone.getDrcModule();
        if (drcMod == null) { log("ERROR", "handleDrcRtcQualityTest", "DrcModule null"); waitForKeyPress(); return; }
        drcMod.drcRtcQualityTest(error -> {
            log("INFO", "handleDrcRtcQualityTest", "结果: " + error);
        });
        waitForKeyPress();
    }

    private static void handleModuleCacheTest() {
        printSeparator("Module缓存测试 - getLiveStream/getLiveRecord 多实例验证");

        com.dji.flighthubsdk.core.Dock testDock = null;
        com.dji.flighthubsdk.core.Drone testDrone = null;
        if (!selectedDevice.getSubDevices().isEmpty()) {
            testDrone = getOrCreateDrone(getSubDeviceSn(selectedDevice));
        } else {
            testDock = getOrCreateDock(getMainDeviceSn(selectedDevice));
        }
        if (testDock == null && testDrone == null) {
            log("ERROR", "handleModuleCacheTest", "获取设备实例失败");
            waitForKeyPress();
            return;
        }

        int pass = 0, fail = 0;

        LiveInfo baseInfo = new LiveInfo();
        baseInfo.setDeviceSn(testDrone != null ? getSubDeviceSn(selectedDevice) : getMainDeviceSn(selectedDevice));

        // === getLiveStream 测试 ===
        log("INFO", "handleModuleCacheTest", "--- getLiveStream 测试 ---");
        baseInfo.setPayloadIndex("98-0-0");
        baseInfo.setVideoId("normal-0");
        LiveModule s1 = testDrone != null ? testDrone.getLiveStream(baseInfo) : testDock.getLiveStream(baseInfo);
        LiveModule s2 = testDrone != null ? testDrone.getLiveStream(baseInfo) : testDock.getLiveStream(baseInfo);
        if (s1 != null && s1 == s2) { log("INFO", "handleModuleCacheTest", "  [PASS] 相同 payload_index 返回同一实例"); pass++; }
        else { log("ERROR", "handleModuleCacheTest", "  [FAIL] 相同 payload_index 返回同一实例"); fail++; }

        baseInfo.setPayloadIndex("99-0-0");
        LiveModule s3 = testDrone != null ? testDrone.getLiveStream(baseInfo) : testDock.getLiveStream(baseInfo);
        if (s1 != null && s3 != null && s1 != s3) { log("INFO", "handleModuleCacheTest", "  [PASS] 不同 payload_index 返回不同实例"); pass++; }
        else { log("ERROR", "handleModuleCacheTest", "  [FAIL] 不同 payload_index 返回不同实例"); fail++; }

        // === getLiveRecord 测试 ===
        log("INFO", "handleModuleCacheTest", "--- getLiveRecord 测试 ---");
        baseInfo.setPayloadIndex("98-0-0");
        baseInfo.setVideoId("normal-0");
        LiveRecordModule r1 = testDrone != null ? testDrone.getLiveRecord(baseInfo) : testDock.getLiveRecord(baseInfo);
        LiveRecordModule r2 = testDrone != null ? testDrone.getLiveRecord(baseInfo) : testDock.getLiveRecord(baseInfo);
        if (r1 != null && r1 == r2) { log("INFO", "handleModuleCacheTest", "  [PASS] 相同 payload_index 返回同一实例"); pass++; }
        else { log("ERROR", "handleModuleCacheTest", "  [FAIL] 相同 payload_index 返回同一实例"); fail++; }

        baseInfo.setPayloadIndex("99-0-0");
        LiveRecordModule r3 = testDrone != null ? testDrone.getLiveRecord(baseInfo) : testDock.getLiveRecord(baseInfo);
        if (r1 != null && r3 != null && r1 != r3) { log("INFO", "handleModuleCacheTest", "  [PASS] 不同 payload_index 返回不同实例"); pass++; }
        else { log("ERROR", "handleModuleCacheTest", "  [FAIL] 不同 payload_index 返回不同实例"); fail++; }

        log("INFO", "handleModuleCacheTest", "结果: " + (pass + fail) + " 测试, " + pass + " 通过, " + fail + " 失败");
        waitForKeyPress();
    }

    /**
     * HMS 测试 - 查询历史 HMS 告警数据
     */
    private static void handleHmsTest() {
        log("INFO", "handleHmsTest", "HMS 测试");
        ConfigManager.DemoConfig config = ConfigManager.getConfig();
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleHmsTest", "获取workspace失败");
            return;
        }
        com.dji.flighthubsdk.core.HMSModule hmsModule = workspace.getHMSModule();
        if (hmsModule == null) {
            log("ERROR", "handleHmsTest", "获取 HMSModule 失败");
            return;
        }
        System.out.println(" 查询历史 HMS 数据 [1]");
        System.out.print(" 请输入选择: ");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1: {
                java.util.List<String> snList = new java.util.ArrayList<>(config.devices.dock_sns);
                com.dji.flighthubsdk.bean.HistoryHmsDataParam param = new com.dji.flighthubsdk.bean.HistoryHmsDataParam();
                param.setSnList(snList);
                param.setCreateTimeBegin(1L);
                param.setCreateTimeEnd(System.currentTimeMillis());
                param.setPageSize(20);
                param.setPageNum(1);
                hmsModule.queryHistoryHMSData(param, new FlightHubCallback.QueryHistoryHMSDataCallback() {
                    @Override
                    public void onResult(FlightHubError error, com.dji.flighthubsdk.bean.HistoryHmsDataResult result) {
                        int listSize = (result != null && result.getList() != null) ? result.getList().size() : 0;
                        int total = (result != null && result.getPagination() != null) ? result.getPagination().getTotal() : 0;
                        log("INFO", "handleHmsTest", "queryHistoryHMSData error: " + error
                                + ", list size: " + listSize
                                + ", total: " + total);
                        if (result != null && result.getList() != null) {
                            for (com.dji.flighthubsdk.bean.HistoryHmsDataResultItem item : result.getList()) {
                                log("INFO", "handleHmsTest", "  mongo_id: " + item.getMongoId()
                                        + ", sn: " + item.getSn()
                                        + ", code: " + item.getCode()
                                        + ", level: " + item.getLevel()
                                        + ", message: " + item.getMessage()
                                        + ", status: " + item.getStatus());
                            }
                        }
                    }
                });
                break;
            }
            default:
                System.out.println("无效输入");
        }
    }

    // ─── Element 模块 Helper ─────────────────────────────────────────────────

    private static com.dji.flighthubsdk.core.ElementModule getElementModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) { log("ERROR", "Element", "获取 workspace 失败"); return null; }
        com.dji.flighthubsdk.core.ElementModule m = ws.getElementModule();
        if (m == null) log("ERROR", "Element", "获取 ElementModule 失败");
        return m;
    }

    // [601] 创建要素
    private static void handleElementCreateElement() {
        printSeparator("[Element] 创建要素");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;

        // 构建创建要素参数（bean 方式）
        com.dji.flighthubsdk.bean.ElementResourceContent content =
                new com.dji.flighthubsdk.bean.ElementResourceContent();
        content.setType("Feature");
        com.dji.flighthubsdk.bean.ElementGeometry geometry =
                new com.dji.flighthubsdk.bean.ElementGeometry();
        geometry.setType("Point");
        geometry.setCoordinates("[0.0,0.0]");
        content.setGeometry(geometry);

        com.dji.flighthubsdk.bean.ElementCreateParams params =
                new com.dji.flighthubsdk.bean.ElementCreateParams(
                        content,
                        0,          // type=0：点要素
                        "Demo点要素");
        log("INFO", "Element", "创建点要素 content=" + params.getResourceContent());

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.createElement(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Element", "创建要素失败: " + err.toString());
            } else {
                log("INFO", "Element", "创建要素成功! id=" + result.getId());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [602] 更新要素（重命名）
    private static void handleElementUpdateElement() {
        printSeparator("[Element] 更新要素");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;
        System.out.print("请输入要素 UUID: ");
        String rawId = System.console() != null ? System.console().readLine() : "test-element-uuid";
        final String elementId = (rawId == null || rawId.trim().isEmpty()) ? "test-element-uuid" : rawId.trim();
        log("INFO", "Element", "更新要素 id=" + elementId);

        // 构建更新参数（bean 方式，演示多个可选字段）
        com.dji.flighthubsdk.bean.ElementUpdateParams params =
                new com.dji.flighthubsdk.bean.ElementUpdateParams(elementId);
        params.setName("更新后名称");
        params.setDisplay(1);              // 设为显示
        params.setRemark("Demo备注信息");
        params.setStatus(1);               // 元素状态

        // 更新几何内容（将点移动到新坐标）
        com.dji.flighthubsdk.bean.ElementResourceContent content =
                new com.dji.flighthubsdk.bean.ElementResourceContent();
        content.setType("Feature");
        com.dji.flighthubsdk.bean.ElementGeometry geometry =
                new com.dji.flighthubsdk.bean.ElementGeometry();
        geometry.setType("Point");
        geometry.setCoordinates("[0.0,0.0]");
        content.setGeometry(geometry);
        com.dji.flighthubsdk.bean.ElementProperties properties =
                new com.dji.flighthubsdk.bean.ElementProperties();
        properties.setColor("#FF0000");
        properties.setClampToGround(true);
        properties.setIs3d(false);
        properties.setTransparent(false);
        properties.setFontSize(14);
        properties.setIcon(1);
        content.setProperties(properties);
        params.setContent(content);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.updateElement(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Element", "更新要素失败: " + err.toString());
            } else {
                log("INFO", "Element", "更新要素成功! id=" + result.getId());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [603] 查询元素层级列表
    private static void handleElementListHierarchy() {
        printSeparator("[Element] 查询元素层级列表");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;
        log("INFO", "Element", "查询所有图层（group_id 为空）");

        // 构建查询参数（bean 方式）
        com.dji.flighthubsdk.bean.ElementListHierarchyParams params =
                new com.dji.flighthubsdk.bean.ElementListHierarchyParams();
        // 不设置 groupId/pid，查询 workspace 下全部根目录图层

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.listElementHierarchy(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Element", "查询失败: " + err.toString());
            } else {
                log("INFO", "Element", "查询成功! 图层数=" + result.getList().size());
                for (com.dji.flighthubsdk.bean.ElementGroupOutput group : result.getList()) {
                    printGroupOutput(group, 0);
                }
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    private static void printGroupOutput(com.dji.flighthubsdk.bean.ElementGroupOutput group, int depth) {
        // Java 8 无 String.repeat；用 StringBuilder 拼缩进
        StringBuilder indentSb = new StringBuilder(depth * 2);
        for (int i = 0; i < depth; i++) {
            indentSb.append("  ");
        }
        String indent = indentSb.toString();
        log("INFO", "Element", indent + "├─[图层] id=" + group.getId()
                + "  name=" + group.getName()
                + "  type=" + group.getType()
                + "  triState=" + group.getTriState()
                + "  locked=" + group.isLock()
                + "  distributed=" + group.isDistributed()
                + "  pid=" + group.getParentId()
                + "  createTime=" + group.getCreateTime()
                + "  elements=" + group.getElements().size()
                + "  children=" + group.getChildren().size());
        for (com.dji.flighthubsdk.bean.ElementItem item : group.getElements()) {
            com.dji.flighthubsdk.bean.ElementResourceData res = item.getResource();
            String resInfo = "";
            if (res != null && res.getContent() != null && res.getContent().getGeometry() != null) {
                // coordinates 为 raw JSON，格式由 geoType 决定：
                //   Point:      [lng, lat, alt]              例: [114.06,22.55,12.6]
                //   LineString: [[lng,lat,alt],...]          例: [[114.03,22.53],[114.04,22.53]]
                //   Polygon:    [[[lng,lat,alt],...]]        例: [[[114.22,30.55,0],[114.22,30.55,0]]]
                //   圆(type=7): Point + radius              例: coordinates=[114.06,22.53,0], radius=825.88
                resInfo = "  geoType=" + res.getContent().getGeometry().getType()
                        + "  coordinates=" + res.getContent().getGeometry().getCoordinates()
                        + "  radius=" + res.getContent().getGeometry().getRadius()
                        + "  color=" + (res.getContent().getProperties() != null ? res.getContent().getProperties().getColor() : "")
                        + "  resType=" + res.getType()
                        + "  remark=" + res.getRemark();
            }
            log("INFO", "Element", indent + "  │ [要素] id=" + item.getId()
                    + "  name=" + item.getName()
                    + "  source=" + item.getSource()
                    + "  display=" + item.getDisplay()
                    + "  status=" + item.getStatus()
                    + "  order=" + item.getOrder()
                    + "  elevationLoad=" + item.getElevationLoadStatus()
                    + "  notify=" + item.isNotify()
                    + "  updateTime=" + item.getUpdateTime()
                    + "  createTime=" + item.getCreateTime()
                    + resInfo);
        }
        for (com.dji.flighthubsdk.bean.ElementGroupOutput child : group.getChildren()) {
            printGroupOutput(child, depth + 1);
        }
    }

    // [604] 设置要素显示/隐藏
    private static void handleElementSetVisibility() {
        printSeparator("[Element] 设置要素显示/隐藏");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;
        System.out.print("请输入要素 UUID: ");
        String rawId = System.console() != null ? System.console().readLine() : "test-element-uuid";
        final String elementId = (rawId == null || rawId.trim().isEmpty()) ? "test-element-uuid" : rawId.trim();
        log("INFO", "Element", "设置要素显示 id=" + elementId + " display=1（显示）");

        // 构建可见性参数（bean 方式）
        com.dji.flighthubsdk.bean.ElementSetVisibilityParams params =
                new com.dji.flighthubsdk.bean.ElementSetVisibilityParams(elementId, 1);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.setElementVisibility(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Element", "操作失败: " + err.toString());
            } else {
                log("INFO", "Element", "操作成功! id=" + result.getId());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [605] 删除要素
    private static void handleElementDeleteElement() {
        printSeparator("[Element] 删除要素");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;
        System.out.print("请输入要素 UUID: ");
        String rawId = System.console() != null ? System.console().readLine() : "test-element-uuid";
        final String elementId = (rawId == null || rawId.trim().isEmpty()) ? "test-element-uuid" : rawId.trim();
        log("INFO", "Element", "删除要素 id=" + elementId);

        // 构建删除参数（bean 方式）
        com.dji.flighthubsdk.bean.ElementDeleteParams params =
                new com.dji.flighthubsdk.bean.ElementDeleteParams(elementId);

        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        module.deleteElement(params, (err, result) -> {
            if (err.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                log("ERROR", "Element", "删除失败: " + err.toString());
            } else {
                log("INFO", "Element", "删除成功! id=" + result.getId());
            }
            latch.countDown();
        });
        try { latch.await(15, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        waitForKeyPress();
    }

    // [606] 监听要素创建推送
    private static void handleElementListenCreate() {
        printSeparator("[Element] 监听要素创建推送 (topic_element_create)");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;
        log("INFO", "Element", "开始监听，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenElementCreate(msg -> {
            log("INFO", "Element", "[推送] 要素创建: id=" + msg.getId()
                    + "  groupId=" + msg.getGroupId()
                    + "  name=" + msg.getName()
                    + "  display=" + msg.getDisplay());
        });
        log("INFO", "Element", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) { if (getUserChoice(0, 999) == 0) break; }
        if (listenIdHolder[0] >= 0) { module.cancelListenElementCreate(listenIdHolder[0]); log("INFO", "Element", "已取消 listen_id=" + listenIdHolder[0]); }
        waitForKeyPress();
    }

    // [607] 监听要素更新推送
    private static void handleElementListenUpdate() {
        printSeparator("[Element] 监听要素更新推送 (topic_element_update)");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;
        log("INFO", "Element", "开始监听，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenElementUpdate(msg -> {
            log("INFO", "Element", "[推送] 要素更新: id=" + msg.getId()
                    + "  groupId=" + msg.getGroupId()
                    + "  name=" + msg.getName()
                    + "  display=" + msg.getDisplay());
        });
        log("INFO", "Element", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) { if (getUserChoice(0, 999) == 0) break; }
        if (listenIdHolder[0] >= 0) { module.cancelListenElementUpdate(listenIdHolder[0]); log("INFO", "Element", "已取消 listen_id=" + listenIdHolder[0]); }
        waitForKeyPress();
    }

    // [608] 监听要素删除推送
    private static void handleElementListenDelete() {
        printSeparator("[Element] 监听要素删除推送 (topic_element_delete)");
        com.dji.flighthubsdk.core.ElementModule module = getElementModule(); if (module == null) return;
        log("INFO", "Element", "开始监听，输入 0 取消");
        int[] listenIdHolder = {-1};
        listenIdHolder[0] = module.listenElementDelete(msg -> {
            log("INFO", "Element", "[推送] 要素删除: id=" + msg.getId()
                    + "  groupId=" + msg.getGroupId());
        });
        log("INFO", "Element", "监听已注册 listen_id=" + listenIdHolder[0]);
        while (running.get()) { if (getUserChoice(0, 999) == 0) break; }
        if (listenIdHolder[0] >= 0) { module.cancelListenElementDelete(listenIdHolder[0]); log("INFO", "Element", "已取消 listen_id=" + listenIdHolder[0]); }
        waitForKeyPress();
    }

    /**
     * [128] AI 告警管理测试（PAAS 纯血场景）
     * 覆盖 6 个接口：写入/读取设备级告警配置、写入/读取算法阈值配置、查询告警架次、查询告警明细
     */
    private static void handleAiAlertTest() {
        log("INFO", "handleAiAlertTest", "AI 告警管理测试（PAAS 纯血）");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleAiAlertTest", "获取 workspace 失败");
            return;
        }
        AiAlertModule aiAlertModule = workspace.getAIAlertModule();
        if (aiAlertModule == null) {
            log("ERROR", "handleAiAlertTest", "获取 AiAlertModule 失败");
            return;
        }

        System.out.println(" [1] 写入设备级告警配置 (postDeviceSetting)");
        System.out.println(" [2] 读取设备级告警配置 (getDeviceSetting)");
        System.out.println(" [3] 写入算法级阈值配置 (postAlgorithmSetting)");
        System.out.println(" [4] 读取算法级阈值配置 (getAlgorithmSetting)");
        System.out.println(" [5] 查询告警架次统计 (getAiAlertFlights)");
        System.out.println(" [6] 查询告警明细记录 (getAiAlertRecords)");
        System.out.print(" 请输入选择: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1: {
                System.out.print(" 请输入 drone_sn: ");
                String droneSn = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingParam param =
                        new com.dji.flighthubsdk.bean.PostAiAlertDeviceSettingParam();
                param.setDroneSn(droneSn);
                param.setEnableTargetAlert(true);
                com.dji.flighthubsdk.bean.AiAlertAlarmInfo alarmInfo = new com.dji.flighthubsdk.bean.AiAlertAlarmInfo();
                alarmInfo.setAlarmInterval(5);
                param.setAlarmInfo(alarmInfo);
                log("INFO", "handleAiAlertTest", "调用 postDeviceSetting, drone_sn=" + droneSn);
                aiAlertModule.postDeviceSetting(param, (error, result) ->
                        log("INFO", "handleAiAlertTest", "postDeviceSetting error: " + error));
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 2: {
                System.out.print(" 请输入 drone_sn: ");
                String droneSn = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingParam param =
                        new com.dji.flighthubsdk.bean.GetAiAlertDeviceSettingParam();
                param.setDroneSn(droneSn);
                log("INFO", "handleAiAlertTest", "调用 getDeviceSetting, drone_sn=" + droneSn);
                aiAlertModule.getDeviceSetting(param, (error, result) -> {
                    log("INFO", "handleAiAlertTest", "getDeviceSetting error: " + error);
                    if (result != null) {
                        log("INFO", "handleAiAlertTest", "enable_target_alert=" + result.isEnableTargetAlert()
                                + ", alarm_interval=" + (result.getAlarmInfo() != null ? result.getAlarmInfo().getAlarmInterval() : -1));
                    }
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 3: {
                System.out.print(" 请输入 drone_sn: ");
                String droneSn = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingParam param =
                        new com.dji.flighthubsdk.bean.PostAiAlertAlgorithmSettingParam();
                param.setDroneSn(droneSn);
                param.setAlgorithmType(0);
                param.setAlgorithmName("default");
                log("INFO", "handleAiAlertTest", "调用 postAlgorithmSetting, drone_sn=" + droneSn);
                aiAlertModule.postAlgorithmSetting(param, (error, result) ->
                        log("INFO", "handleAiAlertTest", "postAlgorithmSetting error: " + error));
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 4: {
                System.out.print(" 请输入 drone_sn: ");
                String droneSn = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingParam param =
                        new com.dji.flighthubsdk.bean.GetAiAlertAlgorithmSettingParam();
                param.setDroneSn(droneSn);
                log("INFO", "handleAiAlertTest", "调用 getAlgorithmSetting, drone_sn=" + droneSn);
                aiAlertModule.getAlgorithmSetting(param, (error, result) -> {
                    log("INFO", "handleAiAlertTest", "getAlgorithmSetting error: " + error);
                    if (result != null) {
                        log("INFO", "handleAiAlertTest", "algorithm_type=" + result.getAlgorithmType()
                                + ", algorithm_name=" + result.getAlgorithmName()
                                + ", alarm_targets.size=" + (result.getAlarmTargets() != null ? result.getAlarmTargets().size() : 0));
                    }
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 5: {
                System.out.print(" 请输入 drone_sn: ");
                String droneSn = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.GetAiAlertFlightsParam param =
                        new com.dji.flighthubsdk.bean.GetAiAlertFlightsParam();
                param.setDroneSn(droneSn);
                log("INFO", "handleAiAlertTest", "调用 getAiAlertFlights, drone_sn=" + droneSn);
                aiAlertModule.getAiAlertFlights(param, (error, result) -> {
                    log("INFO", "handleAiAlertTest", "getAiAlertFlights error: " + error);
                    if (result != null) {
                        log("INFO", "handleAiAlertTest", "total=" + result.getTotal()
                                + ", page=" + result.getPage()
                                + ", data.size=" + (result.getData() != null ? result.getData().size() : 0));
                        if (result.getData() != null) {
                            for (com.dji.flighthubsdk.bean.AiAlertFlightItem item : result.getData()) {
                                log("INFO", "handleAiAlertTest", "  flight_id=" + item.getFlightId()
                                        + ", count=" + item.getCount()
                                        + ", task=" + item.getFlightTaskName());
                            }
                        }
                    }
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 6: {
                System.out.print(" 请输入 flight_id（逗号分隔）: ");
                String flightIds = scanner.nextLine().trim();
                com.dji.flighthubsdk.bean.GetAiAlertRecordsParam param =
                        new com.dji.flighthubsdk.bean.GetAiAlertRecordsParam();
                java.util.List<String> ids = new java.util.ArrayList<>();
                for (String id : flightIds.split(",")) {
                    String trimmed = id.trim();
                    if (!trimmed.isEmpty()) ids.add(trimmed);
                }
                param.setFlightId(ids);
                log("INFO", "handleAiAlertTest", "调用 getAiAlertRecords, flight_ids=" + ids);
                aiAlertModule.getAiAlertRecords(param, (error, result) -> {
                    log("INFO", "handleAiAlertTest", "getAiAlertRecords error: " + error);
                    if (result != null) {
                        log("INFO", "handleAiAlertTest", "total=" + result.getTotal()
                                + ", data.keys=" + (result.getData() != null ? result.getData().keySet() : "null"));
                    }
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            default:
                System.out.println("无效输入");
        }
    }

    /**
     * [129] 直播转发（live-sync）测试
     * 联调环境：xx.xx.xx.xx；sn 待后端确认 §3，Demo 先填飞机 SN
     */
    private static void handleLiveSyncTest() {
        log("INFO", "handleLiveSyncTest", "直播转发测试开始");
        workspace = FlightHubSDK.getWorkspaceInstance();
        if (workspace == null) {
            log("ERROR", "handleLiveSyncTest", "获取 workspace 失败");
            return;
        }
        LiveSyncModule module = workspace.getLiveSyncModule();
        if (module == null) {
            log("ERROR", "handleLiveSyncTest", "获取 LiveSyncModule 失败");
            return;
        }

        System.out.println("=== [129] 直播转发（live-sync）===");
        System.out.println(" [1] getLiveSyncDevicesV2");
        System.out.println(" [2] createLiveSync (RTMP：手输 sn / camera_index / url)");
        System.out.println(" [3] getLiveSyncList");
        System.out.println(" [4] updateLiveSync enable on/off");
        System.out.println(" [5] getLiveSyncSummary");
        System.out.println(" [6] deleteLiveSync");
        System.out.print("请选择: ");
        int sub = Integer.parseInt(scanner.nextLine().trim());

        switch (sub) {
            case 1: {
                module.getLiveSyncDevicesV2((err, result) -> {
                    log("INFO", "getLiveSyncDevicesV2",
                        "err=" + err.getErrorCode() + " total=" + result.getTotal()
                        + " list.size=" + result.getList().size());
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 2: {
                System.out.print("sn（飞机 SN，待后端确认 §3）: ");
                String sn = scanner.nextLine().trim();
                System.out.print("camera_index（如 0-0-0）: ");
                String camIdx = scanner.nextLine().trim();
                System.out.print("RTMP url: ");
                String rtmpUrl = scanner.nextLine().trim();

                com.dji.flighthubsdk.bean.LiveSyncCreateParams params =
                        new com.dji.flighthubsdk.bean.LiveSyncCreateParams();
                params.setConverterName("demo-converter-" + System.currentTimeMillis());
                params.setSn(sn);
                params.setCameraIndex(camIdx);
                params.setSchema("rtmp");
                com.dji.flighthubsdk.bean.LiveSyncSchemaOption opt =
                        new com.dji.flighthubsdk.bean.LiveSyncSchemaOption();
                opt.setUrl(rtmpUrl);
                params.setSchemaOption(opt);

                module.createLiveSync(params, (err, result) -> {
                    log("INFO", "createLiveSync",
                        "err=" + err.getErrorCode() + " uuid=" + result.getItem().getUuid()
                        + " limit=" + result.getLimit());
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 3: {
                com.dji.flighthubsdk.bean.LiveSyncListParams params =
                        new com.dji.flighthubsdk.bean.LiveSyncListParams();
                params.setPage(1);
                params.setSize(20);
                module.getLiveSyncList(params, (err, result) -> {
                    log("INFO", "getLiveSyncList",
                        "err=" + err.getErrorCode() + " total=" + result.getTotal()
                        + " list.size=" + result.getList().size());
                    result.getList().forEach(item ->
                        log("INFO", "getLiveSyncList",
                            "  uuid=" + item.getUuid() + " status=" + item.getStatus()
                            + " enable=" + item.isEnable() + " schema=" + item.getSchema()));
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 4: {
                System.out.print("converter uuid: ");
                String uuid = scanner.nextLine().trim();
                System.out.print("enable (true/false): ");
                boolean enable = Boolean.parseBoolean(scanner.nextLine().trim());

                com.dji.flighthubsdk.bean.LiveSyncUpdateParams params =
                        new com.dji.flighthubsdk.bean.LiveSyncUpdateParams();
                params.setId(uuid);
                params.setSetEnable(true);
                params.setEnable(enable);

                module.updateLiveSync(params, (err, result) -> {
                    log("INFO", "updateLiveSync",
                        "err=" + err.getErrorCode() + " uuid=" + result.getUuid()
                        + " enable=" + result.isEnable());
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 5: {
                module.getLiveSyncSummary((err, result) -> {
                    log("INFO", "getLiveSyncSummary",
                        "err=" + err.getErrorCode() + " total=" + result.getTotal()
                        + " enableCnt=" + result.getEnableCnt());
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            case 6: {
                System.out.print("converter uuid to delete: ");
                String uuid = scanner.nextLine().trim();
                module.deleteLiveSync(uuid, (err) -> {
                    log("INFO", "deleteLiveSync", "err=" + err.getErrorCode());
                });
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                break;
            }
            default:
                System.out.println("无效输入");
        }
    }
}
