// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AircraftActivationTime;
import com.dji.flighthubsdk.bean.AircraftAttitude;
import com.dji.flighthubsdk.bean.AircraftBatteryInfo;
import com.dji.flighthubsdk.bean.AircraftBatteryWarningThresholds;
import com.dji.flighthubsdk.bean.AircraftComplianceInfo;
import com.dji.flighthubsdk.bean.AircraftControlCode;
import com.dji.flighthubsdk.bean.AircraftControlSource;
import com.dji.flighthubsdk.bean.AircraftCurrentRthMode;
import com.dji.flighthubsdk.bean.AircraftFirmwareStatus;
import com.dji.flighthubsdk.bean.AircraftGear;
import com.dji.flighthubsdk.bean.AircraftHomeDistance;
import com.dji.flighthubsdk.bean.AircraftHomeGeographicalInfo;
import com.dji.flighthubsdk.bean.AircraftLimitsInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.AircraftMaintainStatus;
import com.dji.flighthubsdk.bean.AircraftMaintainStatusInfo;
import com.dji.flighthubsdk.bean.AircraftModeCode;
import com.dji.flighthubsdk.bean.AircraftNightLightsState;
import com.dji.flighthubsdk.bean.AircraftObstacleAvoidanceState;
import com.dji.flighthubsdk.bean.AircraftPayloadCameraInfo;
import com.dji.flighthubsdk.bean.AircraftPayloadGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadLiveviewWorldRegionEntry;
import com.dji.flighthubsdk.bean.AircraftPayloadsCameraLiveviewWorldRegion;
import com.dji.flighthubsdk.bean.AircraftPayloadsCamerasList;
import com.dji.flighthubsdk.bean.AircraftPayloadsGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadsList;
import com.dji.flighthubsdk.bean.AircraftPayloadsOsdInfo;
import com.dji.flighthubsdk.bean.AircraftPayloadsOsdInfoList;
import com.dji.flighthubsdk.bean.AircraftPositionState;
import com.dji.flighthubsdk.bean.AircraftRcLostAction;
import com.dji.flighthubsdk.bean.AircraftRthInfo;
import com.dji.flighthubsdk.bean.AircraftSpeed;
import com.dji.flighthubsdk.bean.AircraftStorageInfo;
import com.dji.flighthubsdk.bean.AircraftTotalFlightInfo;
import com.dji.flighthubsdk.bean.AircraftTrackId;
import com.dji.flighthubsdk.bean.AircraftWindSpeed;
import com.dji.flighthubsdk.bean.BatteryBasicInfo;
import com.dji.flighthubsdk.bean.BatteryInfo;
import com.dji.flighthubsdk.bean.CameraLiveviewWorldRegion;
import com.dji.flighthubsdk.bean.CameraMeasureTarget;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.CameraWideLensState;
import com.dji.flighthubsdk.bean.CameraZoomLensState;
import com.dji.flighthubsdk.bean.ControlRightsCloudControlStatus;
import com.dji.flighthubsdk.bean.ControlRightsCloudControlStatusItem;
import com.dji.flighthubsdk.bean.ControlRightsDeviceControlStatus;
import com.dji.flighthubsdk.bean.ControlRightsDeviceControlStatusItem;
import com.dji.flighthubsdk.bean.DeviceChange;
import com.dji.flighthubsdk.bean.DeviceTopoChange;
import com.dji.flighthubsdk.bean.DockAccTime;
import com.dji.flighthubsdk.bean.DockActivationTime;
import com.dji.flighthubsdk.bean.DockAirConditionerStatusInfo;
import com.dji.flighthubsdk.bean.DockAlarmState;
import com.dji.flighthubsdk.bean.DockAlternateLandPoint;
import com.dji.flighthubsdk.bean.DockBackupBatteryInfo;
import com.dji.flighthubsdk.bean.DockBatteryStoreMode;
import com.dji.flighthubsdk.bean.DockCoverState;
import com.dji.flighthubsdk.bean.DockDrcState;
import com.dji.flighthubsdk.bean.DockDroneBatteryMaintenanceInfo;
import com.dji.flighthubsdk.bean.DockDroneChargeState;
import com.dji.flighthubsdk.bean.DockEmergencyStopState;
import com.dji.flighthubsdk.bean.DockFirmwareInfo;
import com.dji.flighthubsdk.bean.DockFirstPowerOnTime;
import com.dji.flighthubsdk.bean.DockJobNumber;
import com.dji.flighthubsdk.bean.FlightTaskStepCode;
import com.dji.flighthubsdk.bean.DockLocation;
import com.dji.flighthubsdk.bean.DockMaintainStatus;
import com.dji.flighthubsdk.bean.DockMaintainStatusInfo;
import com.dji.flighthubsdk.bean.DockMediaFileDetail;
import com.dji.flighthubsdk.bean.DockMeteorologicalInfo;
import com.dji.flighthubsdk.bean.DockModeCode;
import com.dji.flighthubsdk.bean.DockNetworkState;
import com.dji.flighthubsdk.bean.DockOnlineStatus;
import com.dji.flighthubsdk.bean.DockPositionState;
import com.dji.flighthubsdk.bean.DockPowerState;
import com.dji.flighthubsdk.bean.DockPutterState;
import com.dji.flighthubsdk.bean.DockRelativeAlternateLandPoint;
import com.dji.flighthubsdk.bean.DockSelfConvergeCoordinate;
import com.dji.flighthubsdk.bean.DockSilentMode;
import com.dji.flighthubsdk.bean.DockStorageState;
import com.dji.flighthubsdk.bean.DockSubDeviceInfo;
import com.dji.flighthubsdk.bean.DockSubDeviceInfoList;
import com.dji.flighthubsdk.bean.DockSupplementLightState;
import com.dji.flighthubsdk.bean.DockUserExperienceImprovement;
import com.dji.flighthubsdk.bean.DockWirelessLink;
import com.dji.flighthubsdk.bean.DroneInDock;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.FlightTasksMediaFileDisplayAvailable;
import com.dji.flighthubsdk.bean.GimbalState;
import com.dji.flighthubsdk.bean.HmsArgs;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.HmsDataBasics;
import com.dji.flighthubsdk.bean.HmsDataItem;
import com.dji.flighthubsdk.bean.LiveviewWorldRegion;
import com.dji.flighthubsdk.bean.SmartTrackPointInfo;
import com.dji.flighthubsdk.bean.WorkspaceDeviceChange;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.DeviceHMSModule;
import com.dji.flighthubsdk.core.DeviceManageModule;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.DockControlModule;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 设备状态 / OSD 模块 Demo: 订阅无人机 OSD 与在线状态推送, 机场 OSD, HMS 消息等。
 *
 * <p>覆盖无人机在线状态(DeviceStateModule)与无人机 OSD 模块(DroneOsdModule)下的多种 OSD 推送:
 * 负载 OSD 明细、电池、姿态、位置、模式码、风速、控制码、速度、负载相机实时画面世界区域、
 * 负载云台姿态、负载列表。为完整演示推送内容, 本 demo <b>每帧都打印</b>完整明细(不节流);
 * 订阅全部登记进 registry, 运行固定时长后自动退订。
 *
 * <p><b>设计意图 / 生产提醒</b>: OSD 多为高频推送, demo 逐帧打印仅为让使用者看清每一帧字段;
 * <b>实际业务代码不应逐帧打印此类高频 log</b>(会刷屏、拖慢、撑爆日志), 应改为按需节流采样、
 * 聚合统计, 或仅在关注字段发生变化时才落日志。
 *
 * <p><b>安全性</b>: SDK 回调在 native 线程触发且已被 SDK 内部 try/catch 包裹; 本 demo 仍对每个
 * 回调下发的对象与列表逐层判空(顶层对象、子列表、列表元素), 缺失只打印占位说明, 绝不裸抛 NPE。
 *
 * <p><b>枚举取值说明</b>: 本 demo 日志<b>只打印字段原始值与中文语义标签, 不内联枚举取值图例</b>
 * (如 {@code 0=关闭 1=开启} 之类)。原因: 枚举集合可能随固件/协议演进而增删, 把图例硬编码进日志
 * 极易与真实取值漂移, 反被误读为「bug」; 字段取值的权威含义请以 SDK bean 的 Javadoc / 上云协议为准。
 */
public class DeviceOsdDemo extends ModuleDemo {
    /** 默认观察时长(ms): 未输入分钟数 / 无交互输入时的回退值(2 分钟)。 */
    private static final long DEFAULT_OBSERVE_MS = 1000 * 60 * 2;
    /** 交互输入观察时长的下限(分钟)。 */
    private static final int MIN_OBSERVE_MINUTES = 1;
    /** 交互输入观察时长的上限(分钟): 防手误(如多打一位)导致 demo 卡死过久。 */
    private static final int MAX_OBSERVE_MINUTES = 120;

    @Override
    public String name() {
        return "DeviceOsdDemo";
    }

    @Override
    protected void run() throws Exception {
        Drone drone = null;
        if (session.hasDrone()) {
            try {
                drone = session.drone();
            } catch (Exception e) {
                DemoLog.warn(name(), "获取无人机实例失败, 跳过无人机相关订阅: " + Utils.describe(e));
            }
        } else {
            DemoLog.info(name(), "当前设备无无人机, 跳过无人机相关订阅。");
        }

        // OSD/State 监听项很多且会持续增加, 建立订阅后需观察若干分钟才能收全各路推送。
        // 故在订阅前先交互询问一次观察时长(直接回车用默认), 便于按需拉长/缩短观察窗口、逐项排查。
        final long observeMs = askObserveMinutesMs();

        // 控制台明示预计运行时长：DemoLog 只落日志文件, 这里直接给屏幕一句直观提示, 让人知道要等多久。
        System.out.println("\n>>> 订阅建立后将持续观察约 " + observeMs / 60000 + " 分钟(" + observeMs / 1000
            + "s), 期间持续打印 OSD/State 推送, 请耐心等待 ...");

        // 无人机 OSD 模块(DroneOsdModule)。
        DroneOsdModule droneOsdModule = drone != null ? drone.getDroneOsdModule() : null;
        final AtomicLong osdStateCount = new AtomicLong();       // 所有 OSD/State 推送总数(末尾统计)
        final AtomicLong payloadFrame = new AtomicLong();        // 负载 OSD 独立帧号
        final AtomicLong onlineStatusFrame = new AtomicLong();   // 在线状态独立帧号(避免与总数混用致 #n 跳变)

        // 无人机设备状态模块(DeviceStateModule)。
        DeviceStateModule droneDeviceStateModule = drone != null ? drone.getDeviceStateModule() : null;

        // (一) 无人机设备状态模块(drone DeviceStateModule)。
        if (droneDeviceStateModule == null) {
            DemoLog.warn(name(), "droneDeviceStateModule is null, 跳过 DeviceStateModule 订阅。");
        } else {
            // 1) 无人机在线状态。
            safeSubscribe(
                "DroneOnlineStatus",
                droneDeviceStateModule.listenDroneOnlineStatus(new FlightHubCallback.DroneOnlineStatusCallback() {
                    @Override
                    public void onResult(DroneOnlineStatus status) {
                        osdStateCount.incrementAndGet();
                        long n = onlineStatusFrame.incrementAndGet();       // 本流独立帧号
                        if (status == null) {
                            DemoLog.info(name(), "listenDroneOnlineStatus() #DroneOnlineStatus, 无人机在线状态 DroneOnlineStatus: #" + n + " null(本帧无数据)");
                            return;
                        }
                        DemoLog.info(name(), "listenDroneOnlineStatus() #DroneOnlineStatus, 无人机在线状态: #" + n + " " + status);
                    }
                }),
                droneDeviceStateModule::cancelListenDroneOnlineStatus
            );
        }

        // (二) 无人机 OSD 模块(drone DroneOsdModule)。
        if (droneOsdModule == null) {
            DemoLog.warn(name(), "droneOsdModule is null, 跳过 OSD 订阅。");
        } else {
            // 1) 负载 OSD 信息推送。
            //    注意: 据 SDK 文档, 当前版本后端暂不支持 topic_drone_payloads_osd_info 推送, 本回调通常收不到;
            //    如需相关数据请改用: 云台姿态 listenAircraftPayloadsGimbalAttitude(见 10)、
            //    激光测距 CameraControlModule.listenCameraMeasureTarget、变焦/相机参数 listenAircraftPayloadsCamerasList(见 31)。
            safeSubscribe(
                "AircraftPayloadsOsd",
                droneOsdModule.listenAircraftPayloadsOsdInfo(new FlightHubCallback.AircraftPayloadsOsdInfoListCallback() {
                    @Override
                    public void onResult(AircraftPayloadsOsdInfoList list) {
                        osdStateCount.incrementAndGet();
                        long n = payloadFrame.incrementAndGet();     // 负载 OSD 帧号
                        // 说明: 该 topic 当前后端暂不支持(见上), 正常收不到本回调; 下方为「若后端支持」时的演示写法。
                        // 设计意图: 负载 OSD 属高频推送, demo 逐帧打印完整明细仅为演示(不节流);
                        // 生产提醒: 实际业务代码不应逐帧打印此类高频 log, 应按需节流/聚合, 避免刷屏与日志膨胀。
                        DemoLog.info(name(), "listenAircraftPayloadsOsdInfo() #AircraftPayloadsOsdInfoList, 帧 #" + n);
                        printPayloadsOsd(n, list);
                    }
                }
                ),
                droneOsdModule::cancelListenAircraftPayloadsOsdInfo
            );

            // 2) 飞机电池信息(整机电量/降落电量/剩余飞行时间/返航电量 + 各电池明细)。
            safeSubscribe(
                "AircraftBatteryInfo",
                droneOsdModule.listenAircraftBatteryInfo(new FlightHubCallback.AircraftBatteryInfoCallback() {
                    @Override
                    public void onResult(AircraftBatteryInfo info) {
                        osdStateCount.incrementAndGet();
                        DemoLog.info(name(), "listenAircraftBatteryInfo() #AircraftBatteryInfo");
                        printBatteryInfo(info);
                    }
                }),
                droneOsdModule::cancelListenAircraftBatteryInfo
            );

            // 3) 飞机姿态(机头朝向/俯仰/横滚, 单位: 度)。
            safeSubscribe(
                "AircraftAttitude",
                droneOsdModule.listenAircraftAttitude(new FlightHubCallback.AircraftAttitudeCallback() {
                    @Override
                    public void onResult(AircraftAttitude a) {
                        osdStateCount.incrementAndGet();
                        if (a == null) {
                            DemoLog.info(name(), "listenAircraftAttitude() #AircraftAttitude, 飞机姿态 AircraftAttitude: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; 角度单位为「度」。
                        printBlock("listenAircraftAttitude() #AircraftAttitude, 飞机姿态",
                            "head=" + a.getAttitudeHead() + " (航向角, 度)",
                            "pitch=" + a.getAttitudePitch() + " (俯仰角, 度)",
                            "roll=" + a.getAttitudeRoll() + " (横滚角, 度)");
                    }
                }),
                droneOsdModule::cancelListenAircraftAttitude
            );

            // 4) 飞机位置(经纬度 + 椭球高度/相对起飞点高度, 单位: m)。
            safeSubscribe(
                "AircraftLocation",
                droneOsdModule.listenAircraftLocation(new FlightHubCallback.AircraftLocationCallback() {
                    @Override
                    public void onResult(AircraftLocation loc) {
                        osdStateCount.incrementAndGet();
                        if (loc == null) {
                            DemoLog.info(name(), "listenAircraftLocation() #AircraftLocation, 飞机位置 AircraftLocation: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; height/elevation 语义以 bean 为准:
                        // getHeight()=椭球高度(WGS84), getElevation()=相对起飞点高度。
                        printBlock("listenAircraftLocation() #AircraftLocation, 飞机位置",
                            "latitude=" + loc.getLatitude() + " (纬度, [-90,90]度)",
                            "longitude=" + loc.getLongitude() + " (经度, [-180,180]度)",
                            "height=" + loc.getHeight() + " (椭球高度 WGS84, m)",
                            "elevation=" + loc.getElevation() + " (相对起飞点高度, m)");
                    }
                }),
                droneOsdModule::cancelListenAircraftLocation
            );

            // 5) 飞机模式码(当前飞行模式, 见 DroneModeCode)。
            safeSubscribe(
                "AircraftModeCode",
                droneOsdModule.listenAircraftModeCode(new FlightHubCallback.AircraftModeCodeCallback() {
                    @Override
                    public void onResult(AircraftModeCode mc) {
                        osdStateCount.incrementAndGet();
                        if (mc == null) {
                            DemoLog.info(name(), "listenAircraftModeCode() #AircraftModeCode, 飞机模式码 AircraftModeCode: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenAircraftModeCode() #AircraftModeCode, 飞机模式码: mode_code=" + mc.getModeCode()
                            + " (当前飞行模式)"
                        );
                    }
                }),
                droneOsdModule::cancelListenAircraftModeCode
            );

            // 6) 风速/风向(风速为姿态推测的估计值, 仅供参考; 风向为档位枚举, 非角度)。
            safeSubscribe(
                "AircraftWindSpeed",
                droneOsdModule.listenAircraftWindSpeed(new FlightHubCallback.AircraftWindSpeedCallback() {
                    @Override
                    public void onResult(AircraftWindSpeed ws) {
                        osdStateCount.incrementAndGet();
                        if (ws == null) {
                            DemoLog.info(name(), "listenAircraftWindSpeed() #AircraftWindSpeed, 风速 AircraftWindSpeed: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; wind_direction 是档位枚举(非角度), 语义以 bean 为准。
                        printBlock("listenAircraftWindSpeed() #AircraftWindSpeed, 风速/风向",
                            "wind_speed=" + ws.getWindSpeed() + " (风速估计值, 由飞机姿态推测仅供参考)",
                            "wind_direction=" + ws.getWindDirection() + " (风向档位)");
                    }
                }),
                droneOsdModule::cancelListenAircraftWindSpeed
            );

            // 7) 飞机控制码(当前控制模式/控制源)。
            safeSubscribe(
                "AircraftControlCode",
                droneOsdModule.listenAircraftControlCode(new FlightHubCallback.AircraftControlCodeCallback() {
                    @Override
                    public void onResult(AircraftControlCode cc) {
                        osdStateCount.incrementAndGet();
                        if (cc == null) {
                            DemoLog.info(name(), "listenAircraftControlCode() #AircraftControlCode, 飞机控制码 AircraftControlCode: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; control_mode 为枚举, 语义以 bean 为准。
                        DemoLog.info(
                            name(),
                            "listenAircraftControlCode() #AircraftControlCode, 飞机控制码: control_mode=" + cc.getControlMode()
                            + " (控制模式)"
                        );
                    }
                }),
                droneOsdModule::cancelListenAircraftControlCode
            );

            // 8) 飞机速度(水平/垂直, 单位: m/s)。
            safeSubscribe(
                "AircraftSpeed",
                droneOsdModule.listenAircraftSpeed(new FlightHubCallback.AircraftSpeedCallback() {
                    @Override
                    public void onResult(AircraftSpeed sp) {
                        osdStateCount.incrementAndGet();
                        if (sp == null) {
                            DemoLog.info(name(), "listenAircraftSpeed() #AircraftSpeed, 飞机速度 AircraftSpeed: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftSpeed() #AircraftSpeed, 飞机速度",
                            "horizontal=" + sp.getHorizontalSpeed() + " (水平速度, m/s)",
                            "vertical=" + sp.getVerticalSpeed() + " (垂直速度, m/s)");
                    }
                }),
                droneOsdModule::cancelListenAircraftSpeed
            );

            // 9) 各负载相机实时画面对应的世界地理区域(画面四边对应的经纬边界)。
            safeSubscribe(
                "AircraftPayloadsCameraLiveviewWorldRegion",
                droneOsdModule.listenAircraftPayloadsCameraLiveviewWorldRegion(new FlightHubCallback.AircraftPayloadsCameraLiveviewWorldRegionCallback() {
                    @Override
                    public void onResult(AircraftPayloadsCameraLiveviewWorldRegion region) {
                        osdStateCount.incrementAndGet();
                        DemoLog.info(name(), "listenAircraftPayloadsCameraLiveviewWorldRegion() #AircraftPayloadsCameraLiveviewWorldRegion");
                        printLiveviewWorldRegion(region);
                    }
                }),
                droneOsdModule::cancelListenAircraftPayloadsCameraLiveviewWorldRegion
            );

            // 10) 各负载云台姿态(payload_index + pitch/roll/yaw)。
            safeSubscribe(
                "AircraftPayloadsGimbalAttitude",
                droneOsdModule.listenAircraftPayloadsGimbalAttitude(new FlightHubCallback.AircraftPayloadsGimbalAttitudeCallback() {
                    @Override
                    public void onResult(AircraftPayloadsGimbalAttitude attitude) {
                        osdStateCount.incrementAndGet();
                        DemoLog.info(name(), "listenAircraftPayloadsGimbalAttitude() #AircraftPayloadsGimbalAttitude");
                        printPayloadsGimbalAttitude(attitude);
                    }
                }),
                droneOsdModule::cancelListenAircraftPayloadsGimbalAttitude
            );

            // 11) 负载列表(当前挂载的负载 payload_index 集合)。
            safeSubscribe(
                "AircraftPayloadsList",
                droneOsdModule.listenAircraftPayloadsList(new FlightHubCallback.AircraftPayloadsListCallback() {
                    @Override
                    public void onResult(AircraftPayloadsList payloadsList) {
                        osdStateCount.incrementAndGet();
                        if (payloadsList == null) {
                            DemoLog.info(name(), "listenAircraftPayloadsList() #AircraftPayloadsList, 负载列表 AircraftPayloadsList: null(本帧无数据)");
                            return;
                        }
                        List<String> list = payloadsList.getPayloadsList();
                        // 打印数据;
                        printBlock("listenAircraftPayloadsList() #AircraftPayloadsList, 负载列表",
                            "size=" + (list == null ? 0 : list.size()),
                            "payloads=" + (list == null ? "null" : list.toString()));
                    }
                }),
                droneOsdModule::cancelListenAircraftPayloadsList
            );

            // 12) 飞机控制源。
            safeSubscribe(
                "AircraftControlSource",
                droneOsdModule.listenAircraftControlSource(new FlightHubCallback.AircraftControlSourceCallback() {
                    @Override
                    public void onResult(AircraftControlSource v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftControlSource() #AircraftControlSource, 飞机控制源 AircraftControlSource: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenAircraftControlSource() #AircraftControlSource, 飞机控制源: controlSource=" + v.getControlSource() + " (当前控制源)");
                    }
                }),
                droneOsdModule::cancelListenAircraftControlSource
            );

            // 13) 飞机定位状态。
            safeSubscribe(
                "AircraftPositionState",
                droneOsdModule.listenAircraftPositionState(new FlightHubCallback.AircraftPositionStateCallback() {
                    @Override
                    public void onResult(AircraftPositionState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftPositionState() #AircraftPositionState, 飞机定位状态 AircraftPositionState: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftPositionState() #AircraftPositionState, 飞机定位状态",
                            "isFixed=" + v.getIsFixed() + " (是否收敛等状态)",
                            "quality=" + v.getQuality() + " (搜星档位等)",
                            "gpsNumber=" + v.getGpsNumber() + " (GPS 搜星数量)",
                            "rtkNumber=" + v.getRtkNumber() + " (RTK 搜星数量)");
                    }
                }),
                droneOsdModule::cancelListenAircraftPositionState
            );

            // 14) 飞机档位(飞行档位/模式)。
            safeSubscribe(
                "AircraftGear",
                droneOsdModule.listenAircraftGear(new FlightHubCallback.AircraftGearCallback() {
                    @Override
                    public void onResult(AircraftGear v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftGear() #AircraftGear, 飞机档位 AircraftGear: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenAircraftGear() #AircraftGear, 飞机档位: gear=" + v.getGear()
                            + " (飞行档位)"
                        );
                    }
                }),
                droneOsdModule::cancelListenAircraftGear
            );

            // 15) 距返航点距离。
            safeSubscribe(
                "AircraftHomeDistance",
                droneOsdModule.listenAircraftHomeDistance(new FlightHubCallback.AircraftHomeDistanceCallback() {
                    @Override
                    public void onResult(AircraftHomeDistance v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftHomeDistance() #AircraftHomeDistance, 距返航点距离 AircraftHomeDistance: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenAircraftHomeDistance() #AircraftHomeDistance, 距返航点距离: homeDistance=" + v.getHomeDistance()
                            + " (Home点距离, 单位见协议, 通常为米)"
                        );
                    }
                }),
                droneOsdModule::cancelListenAircraftHomeDistance
            );

            // 16) 返航点地理信息。
            safeSubscribe(
                "AircraftHomeGeographicalInfo",
                droneOsdModule.listenAircraftHomeGeographicalInfo(new FlightHubCallback.AircraftHomeGeographicalInfoCallback() {
                    @Override
                    public void onResult(AircraftHomeGeographicalInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftHomeGeographicalInfo() #AircraftHomeGeographicalInfo, 返航点地理信息 AircraftHomeGeographicalInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftHomeGeographicalInfo() #AircraftHomeGeographicalInfo, 返航点地理信息",
                            "homeLongitude=" + v.getHomeLongitude() + " (Home点经度)",
                            "homeLatitude=" + v.getHomeLatitude() + " (Home点纬度)");
                    }
                }),
                droneOsdModule::cancelListenAircraftHomeGeographicalInfo
            );

            // 17) 返航信息。
            safeSubscribe(
                "AircraftRthInfo",
                droneOsdModule.listenAircraftRthInfo(new FlightHubCallback.AircraftRthInfoCallback() {
                    @Override
                    public void onResult(AircraftRthInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftRthInfo() #AircraftRthInfo, 返航信息 AircraftRthInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftRthInfo() #AircraftRthInfo, 返航信息",
                            "rthMode=" + v.getRthMode() + " (返航模式)",
                            "rthAltitude=" + v.getRthAltitude() + " (返航高度)");
                    }
                }),
                droneOsdModule::cancelListenAircraftRthInfo
            );

            // 18) 当前返航模式。
            safeSubscribe(
                "AircraftCurrentRthMode",
                droneOsdModule.listenAircraftCurrentRthMode(new FlightHubCallback.AircraftCurrentRthModeCallback() {
                    @Override
                    public void onResult(AircraftCurrentRthMode v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftCurrentRthMode() #AircraftCurrentRthMode, 当前返航模式 AircraftCurrentRthMode: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenAircraftCurrentRthMode() #AircraftCurrentRthMode, 当前返航模式: currentRthMode=" + v.getCurrentRthMode()
                            + " (当前返航高度模式)"
                        );
                    }
                }),
                droneOsdModule::cancelListenAircraftCurrentRthMode
            );

            // 19) 失控动作。
            safeSubscribe(
                "AircraftRcLostAction",
                droneOsdModule.listenAircraftRcLostAction(new FlightHubCallback.AircraftRcLostActionCallback() {
                    @Override
                    public void onResult(AircraftRcLostAction v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftRcLostAction() #AircraftRcLostAction, 失控动作 AircraftRcLostAction: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenAircraftRcLostAction() #AircraftRcLostAction, 失控动作: rcLostAction=" + v.getRcLostAction()
                            + " (遥控器失联动作)"
                        );
                    }
                }),
                droneOsdModule::cancelListenAircraftRcLostAction
            );

            // 20) 飞行限制信息。
            safeSubscribe(
                "AircraftLimitsInfo",
                droneOsdModule.listenAircraftLimitsInfo(new FlightHubCallback.AircraftLimitsInfoCallback() {
                    @Override
                    public void onResult(AircraftLimitsInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftLimitsInfo() #AircraftLimitsInfo, 飞行限制信息 AircraftLimitsInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftLimitsInfo() #AircraftLimitsInfo, 飞行限制信息",
                            "heightLimit=" + v.getHeightLimit() + " (限制高度, 单位 m)",
                            "isNearHeightLimit=" + v.getIsNearHeightLimit() + " (是否接近限制高度)",
                            "isNearAreaLimit=" + v.getIsNearAreaLimit() + " (是否接近限飞区)",
                            "distanceLimitState=" + v.getDistanceLimitState() + " (限远状态)",
                            "distanceLimit=" + v.getDistanceLimit() + " (限远距离)",
                            "isNearDistanceLimit=" + v.getIsNearDistanceLimit() + " (是否接近限远距离)");
                    }
                }),
                droneOsdModule::cancelListenAircraftLimitsInfo
            );

            // 21) 避障状态。
            safeSubscribe(
                "AircraftObstacleAvoidanceState",
                droneOsdModule.listenAircraftObstacleAvoidanceState(new FlightHubCallback.AircraftObstacleAvoidanceStateCallback() {
                    @Override
                    public void onResult(AircraftObstacleAvoidanceState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftObstacleAvoidanceState() #AircraftObstacleAvoidanceState, 避障状态 AircraftObstacleAvoidanceState: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftObstacleAvoidanceState() #AircraftObstacleAvoidanceState, 避障状态",
                            "horizon=" + v.getHorizon() + " (水平避障状态)",
                            "upside=" + v.getUpside() + " (上视避障状态)",
                            "downside=" + v.getDownside() + " (下视避障状态)");
                    }
                }),
                droneOsdModule::cancelListenAircraftObstacleAvoidanceState
            );

            // 22) 夜航灯状态。
            safeSubscribe(
                "AircraftNightLightsState",
                droneOsdModule.listenAircraftNightLightsState(new FlightHubCallback.AircraftNightLightsStateCallback() {
                    @Override
                    public void onResult(AircraftNightLightsState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftNightLightsState() #AircraftNightLightsState, 夜航灯状态 AircraftNightLightsState: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenAircraftNightLightsState() #AircraftNightLightsState, 夜航灯状态: nightLightsState=" + v.getNightLightsState() + " (夜航灯状态)");
                    }
                }),
                droneOsdModule::cancelListenAircraftNightLightsState
            );

            // 23) 电池告警阈值。
            safeSubscribe(
                "AircraftBatteryWarningThresholds",
                droneOsdModule.listenAircraftBatteryWarningThresholds(new FlightHubCallback.AircraftBatteryWarningThresholdsCallback() {
                    @Override
                    public void onResult(AircraftBatteryWarningThresholds v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftBatteryWarningThresholds() #AircraftBatteryWarningThresholds, 电池告警阈值 AircraftBatteryWarningThresholds: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftBatteryWarningThresholds() #AircraftBatteryWarningThresholds, 电池告警阈值",
                            "lowBatteryWarningThreshold=" + v.getLowBatteryWarningThreshold() + " (低电量告警阈值百分比)",
                            "seriousLowBatteryWarningThreshold=" + v.getSeriousLowBatteryWarningThreshold() + " (严重低电量告警阈值百分比)");
                    }
                }),
                droneOsdModule::cancelListenAircraftBatteryWarningThresholds
            );

            // 24) 固件状态。
            safeSubscribe(
                "AircraftFirmwareStatus",
                droneOsdModule.listenAircraftFirmwareStatus(new FlightHubCallback.AircraftFirmwareStatusCallback() {
                    @Override
                    public void onResult(AircraftFirmwareStatus v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftFirmwareStatus() #AircraftFirmwareStatus, 固件状态 AircraftFirmwareStatus: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftFirmwareStatus() #AircraftFirmwareStatus, 固件状态",
                            "firmwareVersion=" + v.getFirmwareVersion() + " (固件版本)",
                            "compatibleStatus=" + v.getCompatibleStatus() + " (固件一致性)",
                            "firmwareUpgradeStatus=" + v.getFirmwareUpgradeStatus() + " (固件升级状态)");
                    }
                }),
                droneOsdModule::cancelListenAircraftFirmwareStatus
            );

            // 25) 存储容量信息。
            safeSubscribe(
                "AircraftStorageInfo",
                droneOsdModule.listenAircraftStorageInfo(new FlightHubCallback.AircraftStorageInfoCallback() {
                    @Override
                    public void onResult(AircraftStorageInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftStorageInfo() #AircraftStorageInfo, 存储容量信息 AircraftStorageInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftStorageInfo() #AircraftStorageInfo, 存储容量信息",
                            "total=" + v.getTotal() + " (总容量,KB)",
                            "used=" + v.getUsed() + " (已使用容量,KB)");
                    }
                }),
                droneOsdModule::cancelListenAircraftStorageInfo
            );

            // 26) 累计飞行信息。
            safeSubscribe(
                "AircraftTotalFlightInfo",
                droneOsdModule.listenAircraftTotalFlightInfo(new FlightHubCallback.AircraftTotalFlightInfoCallback() {
                    @Override
                    public void onResult(AircraftTotalFlightInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftTotalFlightInfo() #AircraftTotalFlightInfo, 累计飞行信息 AircraftTotalFlightInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftTotalFlightInfo() #AircraftTotalFlightInfo, 累计飞行信息",
                            "totalFlightTime=" + v.getTotalFlightTime() + " (总飞行时间, 秒)",
                            "totalFlightDistance=" + v.getTotalFlightDistance() + " (总飞行距离, m)",
                            "totalFlightSorties=" + v.getTotalFlightSorties() + " (总飞行架次)");
                    }
                }),
                droneOsdModule::cancelListenAircraftTotalFlightInfo
            );

            // 27) 激活时间。
            safeSubscribe(
                "AircraftActivationTime",
                droneOsdModule.listenAircraftActivationTime(new FlightHubCallback.AircraftActivationTimeCallback() {
                    @Override
                    public void onResult(AircraftActivationTime v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftActivationTime() #AircraftActivationTime, 激活时间 AircraftActivationTime: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenAircraftActivationTime() #AircraftActivationTime, 激活时间: activationTime=" + v.getActivationTime() + " (飞行器激活时间, unix时间戳)");
                    }
                }),
                droneOsdModule::cancelListenAircraftActivationTime
            );

            // 28) 维护保养状态。
            safeSubscribe(
                "AircraftMaintainStatusInfo",
                droneOsdModule.listenAircraftMaintainStatusInfo(new FlightHubCallback.AircraftMaintainStatusInfoCallback() {
                    @Override
                    public void onResult(AircraftMaintainStatusInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftMaintainStatusInfo() #AircraftMaintainStatusInfo, 维护保养状态 AircraftMaintainStatusInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; 列表逐项(null-safe)。
                        List<AircraftMaintainStatus> items = v.getMaintainStatuses();
                        if (items == null || items.isEmpty()) {
                            DemoLog.info(name(), "listenAircraftMaintainStatusInfo() #AircraftMaintainStatusInfo, 维护保养状态: 空或无数据");
                            return;
                        }
                        DemoLog.info(name(), "listenAircraftMaintainStatusInfo() #AircraftMaintainStatusInfo, 维护保养状态: size=" + items.size());
                        for (int i = 0; i < items.size(); i++) {
                            AircraftMaintainStatus it = items.get(i);
                            if (it == null) { DemoLog.info(name(), "  [" + i + "] null, 跳过"); continue; }
                            printItem(i,
                                "state=" + it.getState() + " (保养状态)",
                                "lastMaintainType=" + it.getLastMaintainType() + " (上次保养类型)",
                                "lastMaintainTime=" + it.getLastMaintainTime() + " (上次保养时间,UTC秒时间戳)",
                                "lastMaintainFlightTime=" + it.getLastMaintainFlightTime() + " (上次保养时飞行航时,h)",
                                "lastMaintainFlightSorties=" + it.getLastMaintainFlightSorties() + " (上次保养时飞行架次)");
                        }
                    }
                }),
                droneOsdModule::cancelListenAircraftMaintainStatusInfo
            );

            // 29) 合规信息。
            safeSubscribe(
                "AircraftComplianceInfo",
                droneOsdModule.listenAircraftComplianceInfo(new FlightHubCallback.AircraftComplianceInfoCallback() {
                    @Override
                    public void onResult(AircraftComplianceInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftComplianceInfo() #AircraftComplianceInfo, 合规信息 AircraftComplianceInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenAircraftComplianceInfo() #AircraftComplianceInfo, 合规信息",
                            "ridState=" + v.getRidState() + " (RID工作状态)",
                            "country=" + v.getCountry() + " (国家区域码)");
                    }
                }),
                droneOsdModule::cancelListenAircraftComplianceInfo
            );

            // 30) 轨迹 ID。
            safeSubscribe(
                "AircraftTrackId",
                droneOsdModule.listenAircraftTrackId(new FlightHubCallback.AircraftTrackIdCallback() {
                    @Override
                    public void onResult(AircraftTrackId v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftTrackId() #AircraftTrackId, 轨迹 ID AircraftTrackId: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenAircraftTrackId() #AircraftTrackId, 轨迹 ID: trackId=" + v.getTrackId() + " (航迹 ID)");
                    }
                }),
                droneOsdModule::cancelListenAircraftTrackId
            );

            // 31) 负载相机列表。
            safeSubscribe(
                "AircraftPayloadsCamerasList",
                droneOsdModule.listenAircraftPayloadsCamerasList(new FlightHubCallback.AircraftPayloadsCamerasListCallback() {
                    @Override
                    public void onResult(AircraftPayloadsCamerasList v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenAircraftPayloadsCamerasList() #AircraftPayloadsCamerasList, 负载相机列表 AircraftPayloadsCamerasList: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; 列表逐项(null-safe)。
                        List<AircraftPayloadCameraInfo> items = v.getCamerasList();
                        if (items == null || items.isEmpty()) {
                            DemoLog.info(name(), "listenAircraftPayloadsCamerasList() #AircraftPayloadsCamerasList, 负载相机列表: 空或无数据");
                            return;
                        }
                        DemoLog.info(name(), "listenAircraftPayloadsCamerasList() #AircraftPayloadsCamerasList, 负载相机列表: size=" + items.size());
                        for (int i = 0; i < items.size(); i++) {
                            AircraftPayloadCameraInfo it = items.get(i);
                            if (it == null) { DemoLog.info(name(), "  [" + i + "] null, 跳过"); continue; }
                            printItem(i,
                                "payloadIndex=" + it.getPayloadIndex() + " (负载索引)",
                                "cameraMode=" + it.getCameraMode() + " (相机模式)",
                                "zoomFactor=" + it.getZoomFactor() + " (变焦倍数)",
                                "irZoomFactor=" + it.getIrZoomFactor() + " (红外变焦倍数)",
                                "recordingState=" + it.getRecordingState() + " (录制状态)",
                                "recordTime=" + it.getRecordTime() + " (录制时长, 秒)",
                                "remainPhotoNum=" + it.getRemainPhotoNum() + " (剩余拍照数量)",
                                "remainRecordDuration=" + it.getRemainRecordDuration() + " (剩余录制时长)");
                        }
                    }
                }),
                droneOsdModule::cancelListenAircraftPayloadsCamerasList
            );

            // 32) 云端控制权状态。
            safeSubscribe(
                "ControlRightsCloudControlStatus",
                droneOsdModule.listenControlRightsCloudControlStatus(new FlightHubCallback.ControlRightsCloudControlStatusCallback() {
                    @Override
                    public void onResult(ControlRightsCloudControlStatus v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenControlRightsCloudControlStatus() #ControlRightsCloudControlStatus, 云端控制权状态 ControlRightsCloudControlStatus: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; 列表逐项(null-safe)。
                        List<ControlRightsCloudControlStatusItem> items = v.getControls();
                        if (items == null || items.isEmpty()) {
                            DemoLog.info(
                                name(),
                                "listenControlRightsCloudControlStatus() #ControlRightsCloudControlStatus, 云端控制权状态: 空或无数据 (sn=" + v.getSn() + " (设备序列号SN), reason=" + v.getReason() + " (原因))"
                            );
                            return;
                        }
                        printBlock("listenControlRightsCloudControlStatus() #ControlRightsCloudControlStatus, 云端控制权状态",
                            "sn=" + v.getSn() + " (设备序列号SN)",
                            "reason=" + v.getReason() + " (原因)",
                            "size=" + items.size());
                        for (int i = 0; i < items.size(); i++) {
                            ControlRightsCloudControlStatusItem it = items.get(i);
                            if (it == null) { DemoLog.info(name(), "  [" + i + "] null, 跳过"); continue; }
                            printItem(i,
                                "type=" + it.getType() + " (类型)",
                                "key=" + it.getKey() + " (控制权 key)",
                                "userId=" + it.getUserId() + " (用户 ID)",
                                "locked=" + it.isLocked() + " (是否锁定)",
                                "version=" + it.getVersion() + " (版本号)");
                        }
                    }
                }),
                droneOsdModule::cancelListenControlRightsCloudControlStatus
            );

            // 33) 设备端控制权状态。
            safeSubscribe(
                "ControlRightsDeviceControlStatus",
                droneOsdModule.listenControlRightsDeviceControlStatus(new FlightHubCallback.ControlRightsDeviceControlStatusCallback() {
                    @Override
                    public void onResult(ControlRightsDeviceControlStatus v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenControlRightsDeviceControlStatus() #ControlRightsDeviceControlStatus, 设备端控制权状态 ControlRightsDeviceControlStatus: null(本帧无数据)");
                            return;
                        }
                        // 打印数据; 顶层标量。
                        printBlock("listenControlRightsDeviceControlStatus() #ControlRightsDeviceControlStatus, 设备端控制权状态",
                            "sn=" + v.getSn() + " (设备序列号)",
                            "reason=" + v.getReason() + " (原因)");

                        // 打印数据; 列表逐项(null-safe)。
                        List<ControlRightsDeviceControlStatusItem> items = v.getControls();
                        if (items == null || items.isEmpty()) {
                            DemoLog.info(name(), "listenControlRightsDeviceControlStatus() #ControlRightsDeviceControlStatus, 设备端控制权状态: 空或无数据");
                            return;
                        }

                        DemoLog.info(name(), "listenControlRightsDeviceControlStatus() #ControlRightsDeviceControlStatus, 设备端控制权状态: size=" + items.size());
                        for (int i = 0; i < items.size(); i++) {
                            ControlRightsDeviceControlStatusItem it = items.get(i);
                            if (it == null) { DemoLog.info(name(), "  [" + i + "] null, 跳过"); continue; }
                            printItem(i,
                                "type=" + it.getType() + " (类型)",
                                "key=" + it.getKey() + " (键)",
                                "owner=" + it.getOwner() + " (owner)",
                                "version=" + it.getVersion() + " (版本)");
                        }
                    }
                }),
                droneOsdModule::cancelListenControlRightsDeviceControlStatus
            );

            // 34) 任务媒体文件可显示。
            safeSubscribe(
                "FlightTasksMediaFileDisplayAvailable",
                droneOsdModule.listenFlightTasksMediaFileDisplayAvailable(new FlightHubCallback.FlightTasksMediaFileDisplayAvailableCallback() {
                    @Override
                    public void onResult(FlightTasksMediaFileDisplayAvailable v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenFlightTasksMediaFileDisplayAvailable() #FlightTasksMediaFileDisplayAvailable, 任务媒体文件可显示 FlightTasksMediaFileDisplayAvailable: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenFlightTasksMediaFileDisplayAvailable() #FlightTasksMediaFileDisplayAvailable, 任务媒体文件可显示",
                            "uuid=" + v.getUuid() + " (UUID)",
                            "idxParentId=" + v.getIdxParentId() + " (父级索引 ID)",
                            "fileType=" + v.getFileType() + " (文件类型)",
                            "subFileType=" + v.getSubFileType() + " (子文件类型)",
                            "mediaFileId=" + v.getMediaFileId() + " (媒体文件 ID)",
                            "flightId=" + v.getFlightId() + " (飞行 ID)",
                            "sn=" + v.getSn() + " (设备序列号)",
                            "mediaSource=" + v.getMediaSource() + " (媒体来源)",
                            "url=" + v.getUrl() + " (URL 地址)");
                    }
                }),
                droneOsdModule::cancelListenFlightTasksMediaFileDisplayAvailable
            );
        }

        // (三) 无人机设备 HMS 模块(drone DeviceHMSModule)。
        DeviceHMSModule droneHmsModule = drone != null ? drone.getDeviceHMSModule() : null;
        if (droneHmsModule == null) {
            DemoLog.warn(name(), "droneHmsModule is null, 跳过 HMS 订阅。");
        } else {
            // 1) 设备 HMS(健康管理/故障告警)数据推送。
            safeSubscribe(
                "DeviceHMS",
                droneHmsModule.listenDeviceHMS(new FlightHubCallback.DeviceHmsCallback() {
                    @Override
                    public void onResult(HmsData hmsData) {
                        osdStateCount.incrementAndGet();
                        printHmsData("listenDeviceHMS() #HmsData, 无人机", hmsData);
                    }
                }),
                droneHmsModule::cancelListenDeviceHMS
            );
        }

        // ---- 机场(dock)相关订阅(机场为可选设备: 无人机可无机场)----
        // 安全解析机场及其各模块: 无机场时 session.dock() 会抛异常, 故先判 hasDock 并用 try/catch 兜底;
        // 任一环节失败都只记录并跳过机场订阅, 不影响上面无人机侧订阅与本轮观察。
        Dock dock = null;
        DeviceStateModule dockStateModule = null;
        DockControlModule dockControlModule = null;
        DockOsdModule dockOsdModule = null;
        DeviceHMSModule dockHmsModule = null;
        if (!session.hasDock()) {
            DemoLog.info(name(), "当前设备无机场(dock), 跳过所有机场订阅。");
        } else {
            try {
                dock = session.dock();
                if (dock == null) {
                    DemoLog.warn(name(), "解析机场(dock)实例失败, dock is null !");
                } else {
                    dockStateModule = dock.getDeviceStateModule();
                    dockControlModule = dock.getDockControlModule();
                    dockOsdModule = dock.getDockOsdModule();
                    dockHmsModule = dock.getDeviceHMSModule();
                }
            } catch (Exception e) {
                DemoLog.warn(name(), "解析机场(dock)或其模块失败, 跳过机场订阅: " + Utils.describe(e));
            }
        }

        // (四) 机场设备状态模块(dock DeviceStateModule)。
        if (dockStateModule == null) {
            if (dock != null) {
                DemoLog.warn(name(), "dockStateModule is null, 跳过机场设备状态订阅。");
            }
        } else {
            // 1) 机场在线状态。
            safeSubscribe(
                "DockOnlineStatus",
                dockStateModule.listenDockOnlineStatus(new FlightHubCallback.DockOnlineStatusCallback() {
                    @Override
                    public void onResult(DockOnlineStatus v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockOnlineStatus() #DockOnlineStatus, 机场在线状态 DockOnlineStatus: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenDockOnlineStatus() #DockOnlineStatus, 机场在线状态: online=" + v.isOnline() + " (是否在线)");
                    }
                }),
                dockStateModule::cancelListenDockOnlineStatus
            );
        }

        // (五) 机场控制模块(dock DockControlModule)。
        if (dockControlModule == null) {
            if (dock != null) {
                DemoLog.warn(name(), "dockControlModule is null, 跳过机场控制订阅。");
            }
        } else {
            // 1) 机场空调状态。
            safeSubscribe(
                "DockAirConditionerStatusInfo",
                dockControlModule.listenDockAirConditionerStatusInfo(new FlightHubCallback.DockAirConditionerStatusInfoCallback() {
                    @Override
                    public void onResult(DockAirConditionerStatusInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockAirConditionerStatusInfo() #DockAirConditionerStatusInfo, 机场空调状态 DockAirConditionerStatusInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenDockAirConditionerStatusInfo() #DockAirConditionerStatusInfo, 机场空调状态",
                            "airConditionerState=" + v.getAirConditionerState() + " (空调当前工作模式)",
                            "switchTime=" + v.getSwitchTime() + " (切换时间)");
                    }
                }),
                dockControlModule::cancelListenDockAirConditionerStatusInfo
            );

            // 2) 机场补光灯状态。
            safeSubscribe(
                "DockSupplementLightState",
                dockControlModule.listenDockSupplementLightState(new FlightHubCallback.DockSupplementLightStateCallback() {
                    @Override
                    public void onResult(DockSupplementLightState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockSupplementLightState() #DockSupplementLightState, 机场补光灯状态 DockSupplementLightState: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenDockSupplementLightState() #DockSupplementLightState, 机场补光灯状态: supplementLightState=" + v.getSupplementLightState() + " (补光灯状态)");
                    }
                }),
                dockControlModule::cancelListenDockSupplementLightState
            );
        }

        // (六) 机场 OSD 模块(dock DockOsdModule)。
        if (dockOsdModule == null) {
            if (dock != null) {
                DemoLog.warn(name(), "dockOsdModule is null, 跳过机场 OSD 订阅。");
            }
        } else {
            // 1) 机场累计运行时长。
            safeSubscribe(
                "DockAccumulatedRunningTime",
                dockOsdModule.listenDockAccumulatedRunningTime(new FlightHubCallback.DockAccTimeCallback() {
                    @Override
                    public void onResult(DockAccTime v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockAccumulatedRunningTime() #DockAccTime, 机场累计运行时长 DockAccTime: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenDockAccumulatedRunningTime() #DockAccTime, 机场累计运行时长: accTime=" + v.getAccTime() + " (累计运行时长)");
                    }
                }),
                dockOsdModule::cancelListenDockAccumulatedRunningTime
            );

            // 2) 机场激活时间。
            safeSubscribe(
                "DockActivationTime",
                dockOsdModule.listenDockActivationTime(new FlightHubCallback.DockActivationTimeCallback() {
                    @Override
                    public void onResult(DockActivationTime v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockActivationTime() #DockActivationTime, 机场激活时间 DockActivationTime: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockActivationTime() #DockActivationTime, 机场激活时间: activationTime=" + v.getActivationTime() + " (激活时间; SDK 文档标注为 Unix 时间戳, 秒)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockActivationTime
            );

            // 3) 机场声光报警状态。
            safeSubscribe(
                "DockAlarmState",
                dockOsdModule.listenDockAlarmState(new FlightHubCallback.DockAlarmStateCallback() {
                    @Override
                    public void onResult(DockAlarmState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockAlarmState() #DockAlarmState, 机场声光报警状态 DockAlarmState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(name(), "listenDockAlarmState() #DockAlarmState, 机场声光报警状态: alarmState=" + v.getAlarmState() + " (告警状态)");
                    }
                }),
                dockOsdModule::cancelListenDockAlarmState
            );

            // 4) 机场备降点。
            safeSubscribe(
                "DockAlternateLandPoint",
                dockOsdModule.listenDockAlternateLandPoint(new FlightHubCallback.DockAlternateLandPointCallback() {
                    @Override
                    public void onResult(DockAlternateLandPoint v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockAlternateLandPoint() #DockAlternateLandPoint, 机场备降点 DockAlternateLandPoint: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenDockAlternateLandPoint() #DockAlternateLandPoint, 机场备降点",
                            "isConfigured=" + v.getIsConfigured() + " (备降点是否已设置)",
                            "longitude=" + v.getLongitude() + " (经度)",
                            "latitude=" + v.getLatitude() + " (纬度)",
                            "safeLandHeight=" + v.getSafeLandHeight() + " (安全高度/备降转移高度)",
                            "height=" + v.getHeight() + " (高度)");
                    }
                }),
                dockOsdModule::cancelListenDockAlternateLandPoint
            );

            // 5) 机场备用电池信息。
            safeSubscribe(
                "DockBackupBatteryInfo",
                dockOsdModule.listenDockBackupBatteryInfo(new FlightHubCallback.DockBackupBatteryInfoCallback() {
                    @Override
                    public void onResult(DockBackupBatteryInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockBackupBatteryInfo() #DockBackupBatteryInfo, 机场备用电池信息 DockBackupBatteryInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenDockBackupBatteryInfo() #DockBackupBatteryInfo, 机场备用电池信息",
                            "switchState=" + v.getSwitchState() + " (切换状态)",
                            "voltage=" + v.getVoltage() + " (电压)",
                            "temperature=" + v.getTemperature() + " (温度)");
                    }
                }),
                dockOsdModule::cancelListenDockBackupBatteryInfo
            );

            // 6) 机场电池存储模式。
            safeSubscribe(
                "DockBatteryStoreMode",
                dockOsdModule.listenDockBatteryStoreMode(new FlightHubCallback.DockBatteryStoreModeCallback() {
                    @Override
                    public void onResult(DockBatteryStoreMode v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockBatteryStoreMode() #DockBatteryStoreMode, 机场电池存储模式 DockBatteryStoreMode: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockBatteryStoreMode() #DockBatteryStoreMode, 机场电池存储模式: batteryStoreMode=" + v.getBatteryStoreMode() + " (电池存储模式, 整型原始值)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockBatteryStoreMode
            );

            // 7) 机场舱盖状态。
            safeSubscribe(
                "DockCoverState",
                dockOsdModule.listenDockCoverState(new FlightHubCallback.DockCoverStateCallback() {
                    @Override
                    public void onResult(DockCoverState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockCoverState() #DockCoverState, 机场舱盖状态 DockCoverState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockCoverState() #DockCoverState, 机场舱盖状态: coverState=" + v.getCoverState()
                            + " (舱盖状态)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockCoverState
            );

            // 8) 机场 DRC 链路状态。
            safeSubscribe(
                "DockDrcState",
                dockOsdModule.listenDockDrcState(new FlightHubCallback.DockDrcStateCallback() {
                    @Override
                    public void onResult(DockDrcState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockDrcState() #DockDrcState, 机场 DRC 链路状态 DockDrcState: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenDockDrcState() #DockDrcState, 机场 DRC 链路状态: drcState=" + v.getDrcState() + " (远程调试状态)");
                    }
                }),
                dockOsdModule::cancelListenDockDrcState
            );

            // 9) 机场飞行器电池保养信息(保养状态/剩余保养时间/加热保温状态 + 各电池基础信息)。
            safeSubscribe(
                "DockDroneBatteryMaintenanceInfo",
                dockOsdModule.listenDockDroneBatteryMaintenanceInfo(new FlightHubCallback.DockDroneBatteryMaintenanceInfoCallback() {
                    @Override
                    public void onResult(DockDroneBatteryMaintenanceInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockDroneBatteryMaintenanceInfo() #DockDroneBatteryMaintenanceInfo, 机场飞行器电池保养信息 DockDroneBatteryMaintenanceInfo: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        printBlock("listenDockDroneBatteryMaintenanceInfo() #DockDroneBatteryMaintenanceInfo, 机场飞行器电池保养信息",
                            "maintenanceState=" + v.getMaintenanceState() + " (保养状态)",
                            "maintenanceTimeLeft=" + v.getMaintenanceTimeLeft() + " (电池保养剩余时间, 单位:小时)",
                            "heatState=" + v.getHeatState() + " (电池加热保温状态)");

                        // 打印数据; 列表逐项(null-safe)。
                        List<BatteryBasicInfo> items = v.getBatteries();
                        if (items == null || items.isEmpty()) {
                            DemoLog.info(name(), "listenDockDroneBatteryMaintenanceInfo() #DockDroneBatteryMaintenanceInfo, 机场飞行器电池保养信息: 电池列表空或无数据");
                            return;
                        }

                        DemoLog.info(name(), "listenDockDroneBatteryMaintenanceInfo() #DockDroneBatteryMaintenanceInfo, 机场飞行器电池保养信息: 电池数量 size=" + items.size());
                        for (int i = 0; i < items.size(); i++) {
                            BatteryBasicInfo it = items.get(i);
                            if (it == null) { DemoLog.info(name(), "  [" + i + "] null, 跳过"); continue; }
                            printItem(i,
                                "index=" + it.getIndex() + " (电池序号)",
                                "capacityPercent=" + it.getCapacityPercent() + " (电池剩余电量百分比 [0,100])",
                                "voltage=" + it.getVoltage() + " (电压 mV)",
                                "temperature=" + it.getTemperature() + " (温度, 单位:摄氏度)");
                        }
                    }
                }),
                dockOsdModule::cancelListenDockDroneBatteryMaintenanceInfo
            );

            // 10) 机场飞机充电状态。
            safeSubscribe(
                "DockDroneChargeState",
                dockOsdModule.listenDockDroneChargeState(new FlightHubCallback.DockDroneChargeStateCallback() {
                    @Override
                    public void onResult(DockDroneChargeState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockDroneChargeState() #DockDroneChargeState, 机场飞机充电状态 DockDroneChargeState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockDroneChargeState() #DockDroneChargeState, 机场飞机充电状态",
                            "state=" + v.getState() + " (状态; bean 未定义枚举图例)",
                            "capacityPercent=" + v.getCapacityPercent() + " (电量百分比, %)");
                    }
                }),
                dockOsdModule::cancelListenDockDroneChargeState
            );

            // 11) 机场紧急停止按钮状态。
            safeSubscribe(
                "DockEmergencyStopState",
                dockOsdModule.listenDockEmergencyStopState(new FlightHubCallback.DockEmergencyStopStateCallback() {
                    @Override
                    public void onResult(DockEmergencyStopState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockEmergencyStopState() #DockEmergencyStopState, 机场紧急停止按钮状态 DockEmergencyStopState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockEmergencyStopState() #DockEmergencyStopState, 机场紧急停止按钮状态: emergencyStopState=" + v.getEmergencyStopState() + " (急停状态)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockEmergencyStopState
            );

            // 12) 机场固件信息。
            safeSubscribe(
                "DockFirmwareInfo",
                dockOsdModule.listenDockFirmwareInfo(new FlightHubCallback.DockFirmwareInfoCallback() {
                    @Override
                    public void onResult(DockFirmwareInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockFirmwareInfo() #DockFirmwareInfo, 机场固件信息 DockFirmwareInfo: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockFirmwareInfo() #DockFirmwareInfo, 机场固件信息",
                            "firmwareVersion=" + v.getFirmwareVersion() + " (固件版本)",
                            "compatibleStatus=" + v.getCompatibleStatus() + " (兼容状态)",
                            "firmwareUpgradeStatus=" + v.getFirmwareUpgradeStatus() + " (固件升级状态)");
                    }
                }),
                dockOsdModule::cancelListenDockFirmwareInfo
            );

            // 13) 机场首次上电时间。
            safeSubscribe(
                "DockFirstPowerOnTime",
                dockOsdModule.listenDockFirstPowerOnTime(new FlightHubCallback.DockFirstPowerOnTimeCallback() {
                    @Override
                    public void onResult(DockFirstPowerOnTime v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockFirstPowerOnTime() #DockFirstPowerOnTime, 机场首次上电时间 DockFirstPowerOnTime: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockFirstPowerOnTime() #DockFirstPowerOnTime, 机场首次上电时间: firstPowerOn=" + v.getFirstPowerOn() + " (首次开机时间)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockFirstPowerOnTime
            );

            // 14) 机场作业次数。
            safeSubscribe(
                "DockJobNumber",
                dockOsdModule.listenDockJobNumber(new FlightHubCallback.DockJobNumberCallback() {
                    @Override
                    public void onResult(DockJobNumber v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockJobNumber() #DockJobNumber, 机场作业次数 DockJobNumber: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(name(), "listenDockJobNumber() #DockJobNumber, 机场作业次数: jobNumber=" + v.getJobNumber() + " (作业次数)");
                    }
                }),
                dockOsdModule::cancelListenDockJobNumber
            );

            // 15) 机场位置。
            safeSubscribe(
                "DockLocation",
                dockOsdModule.listenDockLocation(new FlightHubCallback.DockLocationCallback() {
                    @Override
                    public void onResult(DockLocation v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockLocation() #DockLocation, 机场位置 DockLocation: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockLocation() #DockLocation, 机场位置",
                            "heading=" + v.getHeading() + " (航向角)",
                            "height=" + v.getHeight() + " (高度)",
                            "latitude=" + v.getLatitude() + " (纬度)",
                            "longitude=" + v.getLongitude() + " (经度)");
                    }
                }),
                dockOsdModule::cancelListenDockLocation
            );

            // 16) 机场保养维护信息。
            safeSubscribe(
                "DockMaintainStatusInfo",
                dockOsdModule.listenDockMaintainStatusInfo(new FlightHubCallback.DockMaintainStatusInfoCallback() {
                    @Override
                    public void onResult(DockMaintainStatusInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockMaintainStatusInfo() #DockMaintainStatusInfo, 机场保养维护信息 DockMaintainStatusInfo: null(本帧无数据)");
                            return;
                        }

                        // 打印数据; 列表逐项(null-safe)。
                        List<DockMaintainStatus> items = v.getMaintainStatusInfos();
                        if (items == null || items.isEmpty()) { DemoLog.info(name(), "listenDockMaintainStatusInfo() #DockMaintainStatusInfo, 机场保养维护信息: 空或无数据"); return; }
                        DemoLog.info(name(), "listenDockMaintainStatusInfo() #DockMaintainStatusInfo, 机场保养维护信息: size=" + items.size());
                        for (int i = 0; i < items.size(); i++) {
                            DockMaintainStatus it = items.get(i);
                            if (it == null) {
                                DemoLog.info(name(), "  [" + i + "] null, 跳过");
                                continue;
                            }

                            // 打印数据;
                            printItem(i,
                                "state=" + it.getState() + " (状态)",
                                "lastMaintainType=" + it.getLastMaintainType() + " (上次保养类型)",
                                "lastMaintainTime=" + it.getLastMaintainTime() + " (上次保养时间)",
                                "lastMaintainWorkSorties=" + it.getLastMaintainWorkSorties() + " (上次保养后工作架次)");
                        }
                    }
                }),
                dockOsdModule::cancelListenDockMaintainStatusInfo
            );

            // 17) 机场媒体文件详情(机场媒体文件数量): 剩余待上传的媒体文件数量。
            safeSubscribe(
                "DockMediaFileDetail",
                dockOsdModule.listenDockMediaFileDetail(new FlightHubCallback.DockMediaFileDetailCallback() {
                    @Override
                    public void onResult(DockMediaFileDetail v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockMediaFileDetail() #DockMediaFileDetail, 机场媒体文件详情 DockMediaFileDetail: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockMediaFileDetail() #DockMediaFileDetail, 机场媒体文件详情: remainUpload=" + v.getRemainUpload()
                            + " (剩余上传数, 单位: 个)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockMediaFileDetail
            );

            // 18) 机场气象信息。
            safeSubscribe(
                "DockMeteorologicalInfo",
                dockOsdModule.listenDockMeteorologicalInfo(new FlightHubCallback.DockMeteorologicalInfoCallback() {
                    @Override
                    public void onResult(DockMeteorologicalInfo v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockMeteorologicalInfo() #DockMeteorologicalInfo, 机场气象信息 DockMeteorologicalInfo: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockMeteorologicalInfo() #DockMeteorologicalInfo, 机场气象信息",
                            "temperature=" + v.getTemperature() + " (舱内温度, 摄氏度)",
                            "environmentTemperature=" + v.getEnvironmentTemperature() + " (环境温度, 摄氏度)",
                            "humidity=" + v.getHumidity() + " (湿度)",
                            "windSpeed=" + v.getWindSpeed() + " (风速)",
                            "rainfall=" + v.getRainfall() + " (降雨量)");
                    }
                }),
                dockOsdModule::cancelListenDockMeteorologicalInfo
            );

            // 19) 机场模式码(ModeCode)。
            safeSubscribe(
                "DockModeCode",
                dockOsdModule.listenDockModeCode(new FlightHubCallback.DockModeCodeCallback() {
                    @Override
                    public void onResult(DockModeCode v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockModeCode() #DockModeCode, 机场模式码 DockModeCode: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(name(), "listenDockModeCode() #DockModeCode, 机场模式码: modeCode=" + v.getModeCode() + " (模式码)");
                    }
                }),
                dockOsdModule::cancelListenDockModeCode
            );

            // 19.1) 飞行任务步骤码(FlightTaskStepCode)。
            safeSubscribe(
                "FlightTaskStepCode",
                dockOsdModule.listenFlightTaskStepCode(new FlightHubCallback.FlightTaskStepCodeCallback() {
                    @Override
                    public void onResult(FlightTaskStepCode v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenFlightTaskStepCode() #FlightTaskStepCode, 飞行任务步骤码: null(本帧无数据)");
                            return;
                        }

                        DemoLog.info(name(), "listenFlightTaskStepCode() #FlightTaskStepCode, flighttaskStepCode="
                            + v.getFlighttaskStepCode() + " (0准备/1飞行/2恢复/3自定义飞行区/4地形障碍物/5空闲/256未知)");
                    }
                }),
                dockOsdModule::cancelListenFlightTaskStepCode
            );

            // 20) 机场网络状态。
            safeSubscribe(
                "DockNetworkState",
                dockOsdModule.listenDockNetworkState(new FlightHubCallback.DockNetworkStateCallback() {
                    @Override
                    public void onResult(DockNetworkState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockNetworkState() #DockNetworkState, 机场网络状态 DockNetworkState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockNetworkState() #DockNetworkState, 机场网络状态",
                            "type=" + v.getType() + " (类型)",
                            "quality=" + v.getQuality() + " (质量)",
                            "rate=" + v.getRate() + " (速率)");
                    }
                }),
                dockOsdModule::cancelListenDockNetworkState
            );

            // 21) 机场搜星状态。
            safeSubscribe(
                "DockPositionState",
                dockOsdModule.listenDockPositionState(new FlightHubCallback.DockPositionStateCallback() {
                    @Override
                    public void onResult(DockPositionState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockPositionState() #DockPositionState, 机场搜星状态 DockPositionState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockPositionState() #DockPositionState, 机场搜星状态",
                            "isCalibration=" + v.getIsCalibration() + " (是否标定)",
                            "isFixed=" + v.getIsFixed() + " (是否收敛)",
                            "quality=" + v.getQuality() + " (搜星档位)",
                            "gpsNumber=" + v.getGpsNumber() + " (GPS 搜星数量)",
                            "rtkNumber=" + v.getRtkNumber() + " (RTK 搜星数量)");
                    }
                }),
                dockOsdModule::cancelListenDockPositionState
            );

            // 22) 机场电源状态。
            safeSubscribe(
                "DockPowerState",
                dockOsdModule.listenDockPowerState(new FlightHubCallback.DockPowerStateCallback() {
                    @Override
                    public void onResult(DockPowerState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockPowerState() #DockPowerState, 机场电源状态 DockPowerState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockPowerState() #DockPowerState, 机场电源状态",
                            "electricSupplyVoltage=" + v.getElectricSupplyVoltage() + " (供电电压)",
                            "workingVoltage=" + v.getWorkingVoltage() + " (工作电压)",
                            "workingCurrent=" + v.getWorkingCurrent() + " (工作电流)");
                    }
                }),
                dockOsdModule::cancelListenDockPowerState
            );

            // 23) 机场推杆状态。
            safeSubscribe(
                "DockPutterState",
                dockOsdModule.listenDockPutterState(new FlightHubCallback.DockPutterStateCallback() {
                    @Override
                    public void onResult(DockPutterState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockPutterState() #DockPutterState, 机场推杆状态 DockPutterState: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenDockPutterState() #DockPutterState, 机场推杆状态: putterState=" + v.getPutterState() + " (推杆状态)");
                    }
                }),
                dockOsdModule::cancelListenDockPutterState
            );

            // 24) 机场相对备降点(相对备降点的坐标与状态)。
            safeSubscribe(
                "DockRelativeAlternateLandPoint",
                dockOsdModule.listenDockRelativeAlternateLandPoint(new FlightHubCallback.DockRelativeAlternateLandPointCallback() {
                    @Override
                    public void onResult(DockRelativeAlternateLandPoint v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockRelativeAlternateLandPoint() #DockRelativeAlternateLandPoint, 机场相对备降点 DockRelativeAlternateLandPoint: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockRelativeAlternateLandPoint() #DockRelativeAlternateLandPoint, 机场相对备降点",
                            "longitude=" + v.getLongitude() + " (经度)",
                            "latitude=" + v.getLatitude() + " (纬度)",
                            "safeLandHeight=" + v.getSafeLandHeight() + " (安全降落高度)",
                            "status=" + v.getStatus() + " (状态)");
                    }
                }),
                dockOsdModule::cancelListenDockRelativeAlternateLandPoint
            );

            // 25) 机场自收敛坐标(self_converge_coordinate)。
            safeSubscribe(
                "DockSelfConvergeCoordinate",
                dockOsdModule.listenDockSelfConvergeCoordinate(new FlightHubCallback.DockSelfConvergeCoordinateCallback() {
                    @Override
                    public void onResult(DockSelfConvergeCoordinate v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockSelfConvergeCoordinate() #DockSelfConvergeCoordinate, 机场自收敛坐标 DockSelfConvergeCoordinate: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockSelfConvergeCoordinate() #DockSelfConvergeCoordinate, 机场自收敛坐标",
                            "height=" + v.getHeight() + " (高度)",
                            "longitude=" + v.getLongitude() + " (经度)",
                            "latitude=" + v.getLatitude() + " (纬度)");
                    }
                }),
                dockOsdModule::cancelListenDockSelfConvergeCoordinate
            );

            // 26) 机场静音模式。
            safeSubscribe(
                "DockSilentMode",
                dockOsdModule.listenDockSilentMode(new FlightHubCallback.DockSilentModeCallback() {
                    @Override
                    public void onResult(DockSilentMode v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockSilentMode() #DockSilentMode, 机场静音模式 DockSilentMode: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockSilentMode() #DockSilentMode, 机场静音模式: silentMode=" + v.getSilentMode()
                            + " (静音模式原始 int 值)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockSilentMode
            );

            // 27) 机场存储容量状态。
            safeSubscribe(
                "DockStorageState",
                dockOsdModule.listenDockStorageState(new FlightHubCallback.DockStorageStateCallback() {
                    @Override
                    public void onResult(DockStorageState v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockStorageState() #DockStorageState, 机场存储容量状态 DockStorageState: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockStorageState() #DockStorageState, 机场存储容量状态",
                            "total=" + v.getTotal() + " (总数)",
                            "used=" + v.getUsed() + " (used)");
                    }
                }),
                dockOsdModule::cancelListenDockStorageState
            );

            // 28) 机场子设备信息列表。
            safeSubscribe(
                "DockSubDevice",
                dockOsdModule.listenDockSubDevice(new FlightHubCallback.DockSubDeviceInfoListCallback() {
                    @Override
                    public void onResult(DockSubDeviceInfoList v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockSubDevice() #DockSubDeviceInfoList, 机场子设备信息列表 DockSubDeviceInfoList: null(本帧无数据)");
                            return;
                        }

                        // 打印数据; 列表逐项(null-safe)。
                        List<DockSubDeviceInfo> items = v.getSubDeviceInfos();
                        if (items == null || items.isEmpty()) {
                            DemoLog.info(name(), "listenDockSubDevice() #DockSubDeviceInfoList, 机场子设备信息列表: 空或无数据");
                            return;
                        }

                        DemoLog.info(name(), "listenDockSubDevice() #DockSubDeviceInfoList, 机场子设备信息列表: size=" + items.size());
                        for (int i = 0; i < items.size(); i++) {
                            DockSubDeviceInfo it = items.get(i);
                            if (it == null) {
                                DemoLog.info(name(), "  [" + i + "] null, 跳过");
                                continue;
                            }

                            // 打印数据;
                            printItem(i,
                                "deviceSn=" + it.getDeviceSn() + " (设备序列号)",
                                "deviceModelKey=" + it.getDeviceModelKey() + " (设备型号标识)",
                                "deviceOnlineStatus=" + it.getDeviceOnlineStatus() + " (设备在线状态)",
                                "devicePaired=" + it.getDevicePaired() + " (设备配对状态)");
                        }
                    }
                }),
                dockOsdModule::cancelListenDockSubDevice
            );

            // 29) 机场用户体验改善计划状态。
            safeSubscribe(
                "DockUserExperienceImprovement",
                dockOsdModule.listenDockUserExperienceImprovement(new FlightHubCallback.DockUserExperienceImprovementCallback() {
                    @Override
                    public void onResult(DockUserExperienceImprovement v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockUserExperienceImprovement() #DockUserExperienceImprovement, 用户体验改善计划 DockUserExperienceImprovement: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        DemoLog.info(
                            name(),
                            "listenDockUserExperienceImprovement() #DockUserExperienceImprovement, 用户体验改善计划: userExperienceImprovement=" + v.getUserExperienceImprovement()
                            + " (用户体验改善计划, 原始整型值)"
                        );
                    }
                }),
                dockOsdModule::cancelListenDockUserExperienceImprovement
            );

            // 30) 机场图传链路质量。
            safeSubscribe(
                "DockWirelessLink",
                dockOsdModule.listenDockWirelessLink(new FlightHubCallback.DockWirelessLinkCallback() {
                    @Override
                    public void onResult(DockWirelessLink v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDockWirelessLink() #DockWirelessLink, 机场图传链路质量 DockWirelessLink: null(本帧无数据)");
                            return;
                        }

                        // 打印数据;
                        printBlock("listenDockWirelessLink() #DockWirelessLink, 机场图传链路质量",
                            "dongleNum=" + v.getDongleNum() + " (飞机上 dongle 数量)",
                            "linkState4g=" + v.getLinkState4g() + " (4G 链路连接状态)",
                            "sdrLinkState=" + v.getSdrLinkState() + " (SDR 链路状态)",
                            "linkWorkmode=" + v.getLinkWorkmode() + " (链路工作模式)",
                            "sdrQuality=" + v.getSdrQuality() + " (SDR 信号质量)",
                            "quality4g=" + v.getQuality4g() + " (总体 4G 信号质量)",
                            "uavQuality4g=" + v.getUavQuality4g() + " (飞机端 4G 信号质量)",
                            "gndQuality4g=" + v.getGndQuality4g() + " (地面端 4G 信号质量)",
                            "sdrFreqBand=" + v.getSdrFreqBand() + " (SDR 频段)",
                            "freqBand4g=" + v.getFreqBand4g() + " (4G 频段)");
                    }
                }),
                dockOsdModule::cancelListenDockWirelessLink
            );

            // 31) 机场中的无人机状态。
            safeSubscribe(
                "DroneInDock",
                dockOsdModule.listenDroneInDock(new FlightHubCallback.DroneInDockCallback() {
                    @Override
                    public void onResult(DroneInDock v) {
                        osdStateCount.incrementAndGet();
                        if (v == null) {
                            DemoLog.info(name(), "listenDroneInDock() #DroneInDock, 机场中的无人机状态 DroneInDock: null(本帧无数据)");
                            return;
                        }
                        // 打印数据;
                        DemoLog.info(name(), "listenDroneInDock() #DroneInDock, 机场中的无人机状态: droneInDock=" + v.getDroneInDock() + " (机场中的无人机)");
                    }
                }),
                dockOsdModule::cancelListenDroneInDock
            );
        }

        // (七) 机场 HMS 模块(dock DeviceHMSModule)。
        if (dockHmsModule == null) {
            if (dock != null) {
                DemoLog.warn(name(), "dockHmsModule is null, 跳过机场 HMS 订阅。");
            }
        } else {
            // 1) 机场设备 HMS(健康管理/故障告警)数据推送(结构同无人机 HMS, 复用 printHmsData)。
            safeSubscribe(
                "DockDeviceHMS",
                dockHmsModule.listenDeviceHMS(new FlightHubCallback.DeviceHmsCallback() {
                    @Override
                    public void onResult(HmsData hmsData) {
                        osdStateCount.incrementAndGet();
                        printHmsData("listenDeviceHMS() #HmsData, 机场", hmsData);
                    }
                }),
                dockHmsModule::cancelListenDeviceHMS
            );
        }

        // (八) 相机状态监听(CameraControlModule drone.getCameraControlModule())。
        // 注意: 这 6 路走 DRC Northbound 链路推送, DRC 链路未建立时订阅本身成功但不会收到回调;
        // 需先通过 PayloadControlDemo [4] 建立 DRC 链路, 再运行本 demo 方可收到数据。
        final CameraControlModule camModule = drone != null ? drone.getCameraControlModule() : null;
        if (camModule == null) {
            DemoLog.warn(name(), "camModule(CameraControlModule) is null, 跳过相机状态订阅。");
        } else {
            final String camPayloadIndex = payloadIndex();
            DemoLog.info(name(), "相机状态订阅 payloadIndex=" + camPayloadIndex);

            // 1) DRC 相机状态(相机模式/录像/拍照/剩余时间等)。
            safeSubscribe(
                "DrcCameraState",
                camModule.listenDrcCameraState(camPayloadIndex, new FlightHubCallback.CameraStateCallback() {
                    @Override
                    public void onResult(CameraState s) {
                        osdStateCount.incrementAndGet();
                        if (s == null) {
                            DemoLog.info(name(), "listenDrcCameraState() #CameraState, DRC 相机状态 CameraState: null(本帧无数据)");
                            return;
                        }
                        printBlock("listenDrcCameraState() #CameraState, DRC 相机状态",
                            "payload=" + s.getPayloadIndex(),
                            "cameraMode(swig)=" + s.getCameraMode() + " (相机模式)",
                            "recordingState=" + s.isRecordingState() + " (是否录像中)",
                            "recordTime=" + s.getRecordTime() + "s (当前录像时长)",
                            "remainRecordDuration=" + s.getRemainRecordDuration() + "s (剩余可录时长)",
                            "photoState=" + s.isPhotoState() + " (是否拍照中)",
                            "remainPhotoNum=" + s.getRemainPhotoNum() + " (剩余可拍张数)",
                            "intervalPhotoInterval=" + s.getIntervalPhotoInterval() + " (定时拍照间隔)");
                    }
                }),
                id -> camModule.cancelListenDrcCameraState(camPayloadIndex, id)
            );

            // 2) DRC 直播范围(画面四角对应的地理经纬边界)。
            safeSubscribe(
                "DrcLiveviewWorldRegion",
                camModule.listenDrcLiveviewWorldRegion(camPayloadIndex, new FlightHubCallback.LiveviewWorldRegionCallback() {
                    @Override
                    public void onResult(LiveviewWorldRegion r) {
                        osdStateCount.incrementAndGet();
                        if (r == null || r.getWorldRegion() == null) {
                            DemoLog.info(name(), "listenDrcLiveviewWorldRegion() #LiveviewWorldRegion, DRC 直播范围 LiveviewWorldRegion: null(本帧无数据)");
                            return;
                        }
                        CameraLiveviewWorldRegion reg = r.getWorldRegion();
                        printBlock("listenDrcLiveviewWorldRegion() #LiveviewWorldRegion, DRC 直播范围",
                            "payload=" + r.getPayloadIndex(),
                            "left=" + reg.getLeft(),
                            "right=" + reg.getRight(),
                            "top=" + reg.getTop(),
                            "bottom=" + reg.getBottom());
                    }
                }),
                id -> camModule.cancelListenDrcLiveviewWorldRegion(camPayloadIndex, id)
            );

            // 3) DRC 广角镜头状态。
            safeSubscribe(
                "DrcCameraWideLensState",
                camModule.listenDrcCameraWideLensState(camPayloadIndex, new FlightHubCallback.CameraWideLensStateCallback() {
                    @Override
                    public void onResult(CameraWideLensState s) {
                        osdStateCount.incrementAndGet();
                        if (s == null) {
                            DemoLog.info(name(), "listenDrcCameraWideLensState() #CameraWideLensState, DRC 广角镜头状态 CameraWideLensState: null(本帧无数据)");
                            return;
                        }
                        printBlock("listenDrcCameraWideLensState() #CameraWideLensState, DRC 广角镜头状态",
                            "payload=" + s.getPayloadIndex(),
                            "wideFocusState(swig)=" + s.getWideFocusState());
                    }
                }),
                id -> camModule.cancelListenDrcCameraWideLensState(camPayloadIndex, id)
            );

            // 4) DRC 变焦镜头状态。
            safeSubscribe(
                "DrcCameraZoomLensState",
                camModule.listenDrcCameraZoomLensState(camPayloadIndex, new FlightHubCallback.CameraZoomLensStateCallback() {
                    @Override
                    public void onResult(CameraZoomLensState s) {
                        osdStateCount.incrementAndGet();
                        if (s == null) {
                            DemoLog.info(name(), "listenDrcCameraZoomLensState() #CameraZoomLensState, DRC 变焦镜头状态 CameraZoomLensState: null(本帧无数据)");
                            return;
                        }
                        printBlock("listenDrcCameraZoomLensState() #CameraZoomLensState, DRC 变焦镜头状态",
                            "payload=" + s.getPayloadIndex(),
                            "zoomFocusState(swig)=" + s.getZoomFocusState());
                    }
                }),
                id -> camModule.cancelListenDrcCameraZoomLensState(camPayloadIndex, id)
            );

            // 5) DRC 测量目标(激光测距目标点信息)。
            safeSubscribe(
                "DrcCameraMeasureTarget",
                camModule.listenDrcCameraMeasureTarget(camPayloadIndex, new FlightHubCallback.CameraMeasureTargetCallback() {
                    @Override
                    public void onResult(CameraMeasureTarget t) {
                        osdStateCount.incrementAndGet();
                        if (t == null) {
                            DemoLog.info(name(), "listenDrcCameraMeasureTarget() #CameraMeasureTarget, DRC 测量目标 CameraMeasureTarget: null(本帧无数据)");
                            return;
                        }
                        printBlock("listenDrcCameraMeasureTarget() #CameraMeasureTarget, DRC 测量目标",
                            "payload=" + t.getPayloadIndex(),
                            "latitude=" + t.getLatitude() + " (纬度)",
                            "longitude=" + t.getLongitude() + " (经度)",
                            "altitude=" + t.getAltitude() + " (高度, m)",
                            "distance=" + t.getDistance() + " (激光测距距离, m)",
                            "laserState(swig)=" + t.getLaserMeasureState());
                    }
                }),
                id -> camModule.cancelListenDrcCameraMeasureTarget(camPayloadIndex, id)
            );

            // 6) DRC 云台状态(yaw/pitch/roll)。
            safeSubscribe(
                "DrcGimbalState",
                camModule.listenDrcGimbalState(camPayloadIndex, new FlightHubCallback.GimbalStateCallback() {
                    @Override
                    public void onResult(GimbalState s) {
                        osdStateCount.incrementAndGet();
                        if (s == null) {
                            DemoLog.info(name(), "listenDrcGimbalState() #GimbalState, DRC 云台状态 GimbalState: null(本帧无数据)");
                            return;
                        }
                        printBlock("listenDrcGimbalState() #GimbalState, DRC 云台状态",
                            "payload=" + s.getPayloadIndex(),
                            "yaw=" + s.getYaw() + " (偏航角, 度)",
                            "pitch=" + s.getPitch() + " (俯仰角, 度)",
                            "roll=" + s.getRoll() + " (横滚角, 度)");
                    }
                }),
                id -> camModule.cancelListenDrcGimbalState(camPayloadIndex, id)
            );
        }

        // (九) 工作空间设备变更 & 拓扑变更监听(DeviceManageModule)。
        // 包含工作空间设备变更、设备拓扑变更(Drone/Dock)。
        // 注意: 设备拓扑变更(Drone)与(Dock)底层是同一 API(listenDeviceTopoChanged), 合并为一路订阅不重复。
        final DeviceManageModule deviceMgr = session.workspace().getDeviceManageModule();
        if (deviceMgr == null) {
            DemoLog.warn(name(), "DeviceManageModule is null, 跳过工作空间设备变更与拓扑变更监听");
        } else {
            safeSubscribe("workspaceDeviceChanged",
                deviceMgr.listenWorkspaceDeviceChanged(new FlightHubCallback.WorkspaceDeviceChangeCallback() {
                    @Override
                    public void onResult(WorkspaceDeviceChange change) {
                        osdStateCount.incrementAndGet();
                        if (change == null) {
                            return;
                        }
                        List<DeviceChange> deviceChanges = change.getDeviceChanges();
                        int cnt = deviceChanges != null ? deviceChanges.size() : 0;
                        DemoLog.info(name(), "listenWorkspaceDeviceChanged() #WorkspaceDeviceChange, [推送] 工作空间设备变更: count=" + cnt);
                        if (deviceChanges == null) {
                            return;
                        }
                        for (int i = 0; i < deviceChanges.size(); i++) {
                            DeviceChange item = deviceChanges.get(i);
                            if (item == null) {
                                continue;
                            }
                            List<String> devs = item.getChangeDevices();
                            DemoLog.info(name(), "  [" + i + "] change_type=" + item.getChangeType()
                                    + " devices=" + (devs != null ? devs.size() : 0));
                            if (devs != null) {
                                for (int j = 0; j < devs.size(); j++) {
                                    DemoLog.info(name(), "    [" + j + "] " + devs.get(j));
                                }
                            }
                        }
                    }
                }),
                deviceMgr::cancelListenWorkspaceDeviceChanged);

            safeSubscribe("deviceTopoChanged",
                deviceMgr.listenDeviceTopoChanged(new FlightHubCallback.DeviceTopoChangeCallback() {
                    @Override
                    public void onResult(DeviceTopoChange change) {
                        osdStateCount.incrementAndGet();
                        if (change == null) {
                            return;
                        }
                        List<String> devices = change.getDevices();
                        DemoLog.info(name(), "listenDeviceTopoChanged() #DeviceTopoChange, [推送] 设备拓扑变更: gateway_sn=" + change.getGatewaySn()
                                + " devices=" + (devices != null ? devices.size() : 0));
                        if (devices != null) {
                            for (int i = 0; i < devices.size(); i++) {
                                DemoLog.info(name(), "  [" + i + "] " + devices.get(i));
                            }
                        }
                    }
                }),
                deviceMgr::cancelListenDeviceTopoChanged);
        }

        DemoLog.info(name(), "DeviceOsd/State 订阅(监听)已建立, 观察 " + observeMs / 1000 + "s (" + observeMs / 60000 + " 分钟) ...");
        // sleep 等待 OSD 推送;
        sleep(observeMs);
        DemoLog.info(name(), "共收到 OSD/State 推送 " + osdStateCount.get() + " 次。");
    }

    /**
     * 交互询问本次 OSD/State 监听的观察时长(分钟), 返回对应毫秒数。
     *
     * <p>健壮兜底, 任何输入问题都不中断 demo：
     * <ul>
     *   <li>无交互输入({@code input==null}) 或直接回车 → 用默认 {@link #DEFAULT_OBSERVE_MS};</li>
     *   <li>非数字 → 告警并回退默认;</li>
     *   <li>越界 → 夹到 [{@link #MIN_OBSERVE_MINUTES}, {@link #MAX_OBSERVE_MINUTES}] 分钟。</li>
     * </ul>
     * 复用入口注入的<b>共享</b> {@code input} reader(经 {@link Utils#readLine}), 不另建 reader 以免吞字节。
     */
    private long askObserveMinutesMs() {
        long defaultMinutes = DEFAULT_OBSERVE_MS / 60000;
        if (input == null) {
            DemoLog.info(name(), "无交互输入, 观察时长用默认 " + defaultMinutes + " 分钟");
            return DEFAULT_OBSERVE_MS;
        }

        String line = Utils.readLine(
            input,
            "\n请输入本次 OSD/State 监听的观察时长(分钟, 直接回车默认 " + defaultMinutes
            + " 分钟, 范围 " + MIN_OBSERVE_MINUTES + "-" + MAX_OBSERVE_MINUTES + "): ");
        if (line == null || line.isEmpty()) {
            DemoLog.info(name(), "使用默认观察时长 " + defaultMinutes + " 分钟");
            return DEFAULT_OBSERVE_MS;
        }
        try {
            long minutes = Long.parseLong(line);
            long clamped = minutes;
            if (clamped < MIN_OBSERVE_MINUTES) {
                clamped = MIN_OBSERVE_MINUTES;
            } else if (clamped > MAX_OBSERVE_MINUTES) {
                clamped = MAX_OBSERVE_MINUTES;
            }
            if (clamped != minutes) {
                DemoLog.warn(name(), "观察时长 " + minutes + " 分钟超出范围, 已夹到 " + clamped + " 分钟");
            }
            DemoLog.info(name(), "观察时长设为 " + clamped + " 分钟");
            return clamped * 60000L;
        } catch (NumberFormatException e) {
            DemoLog.warn(name(), "无法解析 '" + line + "' 为分钟数, 回退默认 " + defaultMinutes + " 分钟");
            return DEFAULT_OBSERVE_MS;
        }
    }

    /**
     * 打印一帧「负载 OSD 信息」的完整明细。
     * 顶层对象、payload 列表、每个 payload 的 smart_track 子列表都做空判断,
     * 任一环节缺失只打印占位说明, 不抛异常(回调线程内出错会中断推送)。
     *
     * @param frameNo 负载 OSD 帧序号(payloadFrame 计数)
     * @param list    SDK 回调下发的负载 OSD 列表(可能为 null)
     */
    private void printPayloadsOsd(long frameNo, AircraftPayloadsOsdInfoList list) {
        String tag = name();
        DemoLog.info(tag, "===== 负载 OSD 明细 (帧 #" + frameNo + ") START =====");
        if (list == null) {
            DemoLog.info(tag, "  AircraftPayloadsOsdInfoList: null(本帧无数据)");
            DemoLog.info(tag, "===== 负载 OSD 明细 (帧 #" + frameNo + ") END =====");
            return;
        }

        List<AircraftPayloadsOsdInfo> payloads = list.getPayloadsInfoList();
        if (payloads == null || payloads.isEmpty()) {
            DemoLog.info(tag, "  payloadsInfoList: 空或无数据");
            DemoLog.info(tag, "===== 负载 OSD 明细 (帧 #" + frameNo + ") END =====");
            return;
        }

        DemoLog.info(tag, "  payloadsInfoList size = " + payloads.size());
        for (int i = 0; i < payloads.size(); i++) {
            AircraftPayloadsOsdInfo p = payloads.get(i);
            if (p == null) {
                DemoLog.info(tag, "  [" + i + "] payload: null, 跳过");
                continue;
            }
            // 单个 payload 的全部标量字段汇成一个 { } 块(云台姿态 / 激光测距 / 变焦热成像 / 控制权固件SN)。
            List<Integer> palettes = p.getThermalSupportedPaletteStyles();
            printItem(i,
                "payload_index=" + p.getPayloadIndex(),
                // 云台姿态。
                "gimbal_pitch=" + p.getGimbalPitch() + " (云台俯仰轴角度, [-180,180]度)",
                "gimbal_roll=" + p.getGimbalRoll() + " (云台横滚轴角度, [-180,180]度)",
                "gimbal_yaw=" + p.getGimbalYaw() + " (云台偏航轴/朝向角度, [-180,180]度)",
                // 激光测距。
                "measure_target_altitude=" + p.getMeasureTargetAltitude() + " (激光测距目标海拔, m)",
                "measure_target_longitude=" + p.getMeasureTargetLongitude() + " (目标经度, [-180,180]度)",
                "measure_target_latitude=" + p.getMeasureTargetLatitude() + " (目标纬度, [-90,90]度)",
                "measure_target_distance=" + p.getMeasureTargetDistance() + " (测距距离, m)",
                "measure_target_error_state=" + p.getMeasureTargetErrorState() + " (测距状态)",
                // 变焦 / 热成像。
                "zoom_factor=" + p.getZoomFactor(),
                "thermal_current_palette_style=" + p.getThermalCurrentPaletteStyle() + " (调色盘样式)",
                "thermal_gain_mode=" + p.getThermalGainMode() + " (增益模式)",
                "thermal_global_temperature_min=" + p.getThermalGlobalTemperatureMin() + " (全局最低温, 摄氏度)",
                "thermal_global_temperature_max=" + p.getThermalGlobalTemperatureMax() + " (全局最高温, 摄氏度)",
                "thermal_isotherm_state=" + p.getThermalIsothermState() + " (等温线状态)",
                "thermal_isotherm_lower_limit=" + p.getThermalIsothermLowerLimit() + " (测温区间下限, 摄氏度, 仅等温线开启有效)",
                "thermal_isotherm_upper_limit=" + p.getThermalIsothermUpperLimit() + " (测温区间上限, 摄氏度, 仅等温线开启有效)",
                "thermal_supported_palette_styles=" + (palettes == null ? "null" : palettes.toString()) + " (设备支持的调色盘样式集合)",
                // 控制权 / 固件 / SN。
                "control_source=" + p.getControlSource() + " (负载控制权)",
                "firmware_version=" + p.getFirmwareVersion() + " (负载固件版本)",
                "sn=" + p.getSn() + " (负载 SN)");

            // 智能跟踪点(子列表, 可能为空): 逐点一个 { } 块, 缩进比 payload 更深。
            List<SmartTrackPointInfo> tracks = p.getSmartTrackPoints();
            if (tracks == null || tracks.isEmpty()) {
                DemoLog.info(tag, "      smart_track_points: 空或无数据");
            } else {
                DemoLog.info(tag, "      smart_track_points size = " + tracks.size());
                for (int j = 0; j < tracks.size(); j++) {
                    SmartTrackPointInfo t = tracks.get(j);
                    if (t == null) {
                        DemoLog.info(tag, "        <" + j + "> track: null, 跳过");
                        continue;
                    }
                    printSubItem(j,
                        "track_altitude=" + t.getTrackAltitude() + " (跟踪目标海拔, m)",
                        "track_latitude=" + t.getTrackLatitude() + " (纬度, [-90,90]度)",
                        "track_longitude=" + t.getTrackLongitude() + " (经度, [-180,180]度)",
                        "track_target_mode=" + t.getTrackTargetMode() + " (跟踪目标模式)");
                }
            }
        }
        DemoLog.info(tag, "===== 负载 OSD 明细 (帧 #" + frameNo + ") END =====");
    }

    /**
     * 打印飞机电池信息: 整机汇总(容量/降落电量/剩余飞行时间/返航电量) + 各电池明细。
     * 顶层对象、电池子列表、每个电池元素都判空, 缺失只打印占位说明。
     *
     * @param info SDK 回调下发的电池信息(可能为 null)
     */
    private void printBatteryInfo(AircraftBatteryInfo info) {
        String tag = name();
        if (info == null) {
            DemoLog.info(tag, "飞机电池信息 AircraftBatteryInfo: null(本帧无数据)");
            return;
        }

        printBlock("飞机电池信息",
            "capacity=" + info.getCapacityPercent() + "% (整机剩余电量)",
            "landing_power=" + info.getLandingPower() + "% (降落所需电量)",
            "remain_flight_time=" + info.getRemainFlightTime() + "s (预计剩余飞行时间)",
            "return_home_power=" + info.getReturnHomePower() + "% (返航所需电量)");

        List<BatteryInfo> batteries = info.getBatteries();
        if (batteries == null || batteries.isEmpty()) {
            DemoLog.info(tag, "  batteries: 空或无数据");
            return;
        }

        DemoLog.info(tag, "  batteries size = " + batteries.size());
        for (int i = 0; i < batteries.size(); i++) {
            BatteryInfo b = batteries.get(i);
            if (b == null) {
                DemoLog.info(tag, "  [" + i + "] battery: null, 跳过");
                continue;
            }

            printItem(i,
                "index=" + b.getIndex(),
                "sn=" + b.getSn(),
                "capacity=" + b.getCapacityPercent() + "%",
                "voltage=" + b.getVoltage() + "mV",
                "temperature=" + b.getTemperature() + "℃",
                "loop_times=" + b.getLoopTimes(),
                "high_voltage_storage_days=" + b.getHighVoltageStorageDays(),
                "type=" + b.getType(),
                "sub_type=" + b.getSubType(),
                "firmware=" + b.getFirmwareVersion());
        }
    }

    /**
     * 打印各负载云台姿态(payload_index + pitch/roll/yaw, 单位: 度)。
     * 顶层对象、列表、列表元素都判空。
     *
     * @param attitude SDK 回调下发的负载云台姿态(可能为 null)
     */
    private void printPayloadsGimbalAttitude(AircraftPayloadsGimbalAttitude attitude) {
        String tag = name();
        if (attitude == null) {
            DemoLog.info(tag, "负载云台姿态 AircraftPayloadsGimbalAttitude: null(本帧无数据)");
            return;
        }

        List<AircraftPayloadGimbalAttitude> list = attitude.getPayloadsGimbalAttitude();
        if (list == null || list.isEmpty()) {
            DemoLog.info(tag, "负载云台姿态: 空或无数据");
            return;
        }

        DemoLog.info(tag, "负载云台姿态: size=" + list.size());
        for (int i = 0; i < list.size(); i++) {
            AircraftPayloadGimbalAttitude it = list.get(i);
            if (it == null) {
                DemoLog.info(tag, "  [" + i + "] item: null, 跳过");
                continue;
            }

            printItem(i,
                "payload_index=" + it.getPayloadIndex(),
                "pitch=" + it.getGimbalPitch() + " (云台俯仰角, 度)",
                "roll=" + it.getGimbalRoll() + " (云台横滚角, 度)",
                "yaw=" + it.getGimbalYaw() + " (云台偏航角, 度)");
        }
    }

    /**
     * 打印各负载相机实时画面对应的世界地理区域(画面四边 top/bottom/left/right 对应的经纬边界)。
     * 顶层对象、列表、列表元素、每个元素的 region 子对象都判空。
     *
     * @param region SDK 回调下发的负载相机实时画面世界区域(可能为 null)
     */
    private void printLiveviewWorldRegion(AircraftPayloadsCameraLiveviewWorldRegion region) {
        String tag = name();
        if (region == null) {
            DemoLog.info(tag, "负载相机实时画面世界区域 AircraftPayloadsCameraLiveviewWorldRegion: null(本帧无数据)");
            return;
        }

        List<AircraftPayloadLiveviewWorldRegionEntry> list = region.getPayloadsList();
        if (list == null || list.isEmpty()) {
            DemoLog.info(tag, "负载相机实时画面世界区域: 空或无数据");
            return;
        }

        DemoLog.info(tag, "负载相机实时画面世界区域: size=" + list.size());
        for (int i = 0; i < list.size(); i++) {
            AircraftPayloadLiveviewWorldRegionEntry e = list.get(i);
            if (e == null) {
                DemoLog.info(tag, "  [" + i + "] entry: null, 跳过");
                continue;
            }

            CameraLiveviewWorldRegion r = e.getRegion();
            if (r == null) {
                DemoLog.info(tag, "  [" + i + "] payload_index=" + e.getPayloadIndex() + " region: null(本条无区域数据)");
                continue;
            }

            printItem(i,
                "payload_index=" + e.getPayloadIndex(),
                "region_top=" + r.getTop(),
                "region_bottom=" + r.getBottom(),
                "region_left=" + r.getLeft(),
                "region_right=" + r.getRight() + " (画面四边对应的经纬边界)");
        }
    }

    /**
     * 打印一帧「设备 HMS(健康管理/故障告警)」数据的完整明细。
     * 顶层 HmsData、hmsDataItems 列表、每个 item、主 basics、subHmsData 子列表、
     * 每条子 basics、以及 basics 内的 args 参数块都逐层判空, 任一环节缺失只打印
     * 占位说明, 绝不裸抛 NPE(回调在 native 线程触发, 抛异常会中断后续推送)。
     *
     * <p>字段中文说明严格取自 bean 的 Javadoc(level=告警级别、module_id=模块 ID、
     * imminent=是否紧急、ignore=是否忽略 等);bean 未文档化具体枚举取值含义,
     * 故此处不臆造 {0:...} 之类的枚举图例, 实际语义以协议/bean 为准。
     *
     * @param source  数据来源标识(如「无人机」/「机场」), 用于日志区分同一份 HMS 结构来自哪个设备
     * @param hmsData SDK 回调下发的设备 HMS 数据(可能为 null)
     */
    private void printHmsData(String source, HmsData hmsData) {
        String tag = name();
        DemoLog.info(tag, "===== " + source + "设备 HMS 明细 START =====");
        if (hmsData == null) {
            DemoLog.info(tag, "  HmsData: null(本帧无数据)");
            DemoLog.info(tag, "===== " + source + "设备 HMS 明细 END =====");
            return;
        }

        List<HmsDataItem> items = hmsData.getHmsDataItems();
        if (items == null || items.isEmpty()) {
            DemoLog.info(tag, "  hmsDataItems: 空或无数据");
            DemoLog.info(tag, "===== " + source + "设备 HMS 明细 END =====");
            return;
        }

        DemoLog.info(tag, "  hmsDataItems size = " + items.size());
        for (int i = 0; i < items.size(); i++) {
            HmsDataItem item = items.get(i);
            if (item == null) {
                DemoLog.info(tag, "  [" + i + "] item: null, 跳过");
                continue;
            }

            // 主 basics(本条 HMS 的基础字段)。
            HmsDataBasics basics = item.getBasics();
            if (basics == null) {
                DemoLog.info(tag, "  [" + i + "] basics: null(本条无基础数据)");
            } else {
                // 基础字段 + args.* 汇成一个 { } 块。
                List<String> fs = new ArrayList<String>();
                fs.add("level=" + basics.getLevel() + " (告警级别)");
                fs.add("module_id=" + basics.getModuleId() + " (模块 ID)");
                fs.add("code=" + basics.getCode() + " (告警码)");
                fs.add("device_type=" + basics.getDeviceType() + " (设备类型)");
                fs.add("domain_type=" + basics.getDomainType() + " (域类型)");
                fs.add("in_the_sky=" + basics.getInTheSky() + " (是否在飞行中)");
                fs.add("imminent=" + basics.getImminent() + " (是否紧急)");
                fs.add("status_key=" + basics.getStatusKey() + " (状态键)");
                fs.add("joint_key=" + basics.getJointKey() + " (联合键)");
                fs.add("id=" + basics.getId() + " (ID)");
                fs.add("parent_id=" + basics.getParentId() + " (父级 ID)");

                // args 参数块(可能为 null)。
                HmsArgs args = basics.getArgs();
                if (args == null) {
                    fs.add("args=null(本条无参数)");
                } else {
                    fs.add("args.component_index=" + args.getComponentIndex() + " (组件索引)");
                    fs.add("args.sensor_index=" + args.getSensorIndex() + " (传感器索引)");
                    fs.add("args.device_id=" + args.getDeviceId() + " (设备 ID)");
                    fs.add("args.sender_host_id=" + args.getSenderHostId() + " (发送者主机 ID)");
                    fs.add("args.ignore=" + args.getIgnore() + " (是否忽略)");
                    fs.add("args.alarm_info=" + args.getAlarmInfo() + " (告警信息)");
                    fs.add("args.create_timestamp=" + args.getCreateTimestamp() + " (创建时间戳)");
                    fs.add("args.display_scene=" + args.getDisplayScene() + " (显示场景)");
                }
                printItem(i, fs.toArray(new String[0]));
            }

            // 子 HMS 列表(subHmsData, 可能为空)。
            List<HmsDataBasics> subs = item.getSubHmsData();
            if (subs == null || subs.isEmpty()) {
                DemoLog.info(tag, "      subHmsData: 空或无数据");
                continue;
            }

            DemoLog.info(tag, "      subHmsData size = " + subs.size());
            for (int j = 0; j < subs.size(); j++) {
                HmsDataBasics sub = subs.get(j);
                if (sub == null) {
                    DemoLog.info(tag, "        <" + j + "> sub: null, 跳过");
                    continue;
                }
                List<String> fs = new ArrayList<String>();
                fs.add("level=" + sub.getLevel() + " (告警级别)");
                fs.add("module_id=" + sub.getModuleId() + " (模块 ID)");
                fs.add("code=" + sub.getCode() + " (告警码)");
                fs.add("device_type=" + sub.getDeviceType() + " (设备类型)");
                fs.add("domain_type=" + sub.getDomainType() + " (域类型)");
                fs.add("in_the_sky=" + sub.getInTheSky() + " (是否在飞行中)");
                fs.add("imminent=" + sub.getImminent() + " (是否紧急)");
                fs.add("status_key=" + sub.getStatusKey() + " (状态键)");
                fs.add("joint_key=" + sub.getJointKey() + " (联合键)");
                fs.add("id=" + sub.getId() + " (ID)");
                fs.add("parent_id=" + sub.getParentId() + " (父级 ID)");

                HmsArgs sargs = sub.getArgs();
                if (sargs == null) {
                    fs.add("args=null(本条无参数)");
                } else {
                    fs.add("args.component_index=" + sargs.getComponentIndex() + " (组件索引)");
                    fs.add("args.sensor_index=" + sargs.getSensorIndex() + " (传感器索引)");
                    fs.add("args.device_id=" + sargs.getDeviceId() + " (设备 ID)");
                    fs.add("args.sender_host_id=" + sargs.getSenderHostId() + " (发送者主机 ID)");
                    fs.add("args.ignore=" + sargs.getIgnore() + " (是否忽略)");
                    fs.add("args.alarm_info=" + sargs.getAlarmInfo() + " (告警信息)");
                    fs.add("args.create_timestamp=" + sargs.getCreateTimestamp() + " (创建时间戳)");
                    fs.add("args.display_scene=" + sargs.getDisplayScene() + " (显示场景)");
                }
                printSubItem(j, fs.toArray(new String[0]));
            }
        }
        DemoLog.info(tag, "===== " + source + "设备 HMS 明细 END =====");
    }

    /**
     * 把「标题 + 若干字段」格式化为多行 { } 块并打印(单条日志内嵌换行, 提升可读性)。
     * 字段数 <= 1 时退化为一行「标题: 字段」, 不为单字段占用多行。
     *
     * @param header 段标题(不带冒号), 如「机场网络状态」
     * @param fields 已拼好的「字段名=值 (中文说明)」串, 如 "type=" + v.getType() + " (类型)"
     */
    private void printBlock(String header, String... fields) {
        DemoLog.info(name(), formatBlock(header, "", fields));
    }

    /**
     * 打印列表元素块: 「  [i]」标号行 + 缩进的 { } 字段块(单条日志), 与 {@link #printBlock} 风格一致。
     *
     * @param index  列表下标
     * @param fields 已拼好的「字段名=值 (中文说明)」串
     */
    private void printItem(int index, String... fields) {
        if (fields == null || fields.length == 0) {
            DemoLog.info(name(), "  [" + index + "]");
            return;
        }
        StringBuilder sb = new StringBuilder("  [").append(index).append("]\n  {\n");
        for (int i = 0; i < fields.length; i++) {
            sb.append("    ").append(fields[i]).append(i < fields.length - 1 ? ",\n" : "\n");
        }
        sb.append("  }");
        DemoLog.info(name(), sb.toString());
    }

    /**
     * 打印子列表元素块: 「        <j>」标号行 + 更深缩进的 { } 字段块(单条日志),
     * 供二级列表(如智能跟踪点、子 HMS)逐项打印, 与 {@link #printItem} 风格一致但缩进更深。
     */
    private void printSubItem(int index, String... fields) {
        if (fields == null || fields.length == 0) {
            DemoLog.info(name(), "        <" + index + ">");
            return;
        }
        StringBuilder sb = new StringBuilder("        <").append(index).append(">\n        {\n");
        for (int i = 0; i < fields.length; i++) {
            sb.append("          ").append(fields[i]).append(i < fields.length - 1 ? ",\n" : "\n");
        }
        sb.append("        }");
        DemoLog.info(name(), sb.toString());
    }

    /**
     * 组装多行 { } 块字符串: 「[indent]header:换行[indent]{换行[indent]  f1,换行 ... 换行[indent]}」。
     * 字段数 0 时只返回「[indent]header:」; 字段数 1 时返回「[indent]header: f1」(不成块)。
     */
    private static String formatBlock(String header, String indent, String... fields) {
        if (fields == null || fields.length == 0) {
            return indent + header + ":";
        }
        if (fields.length == 1) {
            return indent + header + ": " + fields[0];
        }
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(header).append(":\n").append(indent).append("{\n");
        for (int i = 0; i < fields.length; i++) {
            sb.append(indent).append("  ").append(fields[i]).append(i < fields.length - 1 ? ",\n" : "\n");
        }
        sb.append(indent).append("}");
        return sb.toString();
    }

    /**
     * 安全订阅: 在基类 {@link #subscribe} 外包一层 try/catch, 让「单路订阅建立失败」
     * (例如 SDK 返回 listenId=-1 时 {@link com.dji.flighthubsdk.demo.runtime.Subscription#of} 抛出异常)
     * 只记录并跳过, 不中断后续订阅与本轮观察——本 demo 一次建立数十路订阅,
     * 借此保证任一路失败都不会连累其余订阅与末尾统计。集成开发者可借鉴此「逐路隔离」的健壮写法。
     */
    private void safeSubscribe(String label, int listenId, Subscription.Canceller canceller) {
        try {
            subscribe(label, listenId, canceller);
        } catch (Exception e) {
            DemoLog.warn(name(), label + " 订阅失败(忽略, 不影响其它订阅): " + Utils.describe(e));
        }
    }
}
