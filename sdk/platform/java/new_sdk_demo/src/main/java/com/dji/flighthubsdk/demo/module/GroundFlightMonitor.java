// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AircraftModeCode;
import com.dji.flighthubsdk.bean.AircraftPositionState;
import com.dji.flighthubsdk.bean.DockCoverState;
import com.dji.flighthubsdk.bean.DockModeCode;
import com.dji.flighthubsdk.bean.DockPutterState;
import com.dji.flighthubsdk.bean.DroneInDock;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.FlightTaskException;
import com.dji.flighthubsdk.bean.FlightTaskExceptionDetail;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.core.DeviceStateModule;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.DeviceSession;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.runtime.SubscriptionRegistry;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.DrcLandingMissionState;
import com.dji.flighthubsdk.generated.DrcOsdInfo;
import com.dji.flighthubsdk.value.DroneModeCode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 舱外起降状态监听：把「舱外(地面)起飞 → 机场降落」全过程涉及的各路推送集中订阅到一处,
 * 供执行 {@code groundTakeoff} / {@code groundLanding} 时旁路观察整条状态链。
 *
 * <p><b>本类完全自包含</b>：拥有自己的订阅列表、自己的开/关方法、自己的状态渲染,
 * 不复用也不改动 {@code FlightControlDemo} 的 {@code [1]} 通用消息监听
 * ({@code monitorSubs}/{@code startMonitors}/{@code stopMonitors})。二者可同时开启、互不干扰
 * ——SDK 的 {@code listenXxx} 按 {@code listenId} 逐个登记回调, 同一 topic 多次订阅各自独立,
 * 取消其一不影响其二。
 *
 * <p><b>订阅清单(11 路)</b>：
 * <ul>
 *   <li><b>核心 6 路(起降过程主链)</b>
 *     <ul>
 *       <li>飞机模式码 {@code listenAircraftModeCode}——起飞/降落阶段的主信号
 *           (2 起飞准备完毕 → 4 自动起飞 → 3 手动飞行 … → 10 自动降落);</li>
 *       <li>机场舱盖 {@code listenDockCoverState}——降落至机场时「关→开→关」的开合过程;</li>
 *       <li>飞机在舱 {@code listenDroneInDock}——降落入舱的终态判据;</li>
 *       <li>机场模式码 {@code listenDockModeCode}——机场侧是否进入作业态;</li>
 *       <li>机场推杆 {@code listenDockPutterState}——入舱后夹紧/松开;</li>
 *       <li>DRC 降落任务状态 {@code listenDrcLandingMissionState}——降落细分状态 0–9 与舱盖开合百分比。</li>
 *     </ul>
 *   </li>
 *   <li><b>高度 / 位置</b>：DRC OSD {@code listenDrcOsdInfo}(高度、相对起飞点高度、经纬度、升降速度)、
 *       飞机位置状态 {@code listenAircraftPositionState}(定位固定解/星数);</li>
 *   <li><b>在线 / 链路 / 异常</b>：飞机在线 {@code listenDroneOnlineStatus}、
 *       DRC 链路 {@code listenLinkStatus}、飞行任务异常 {@code listenFlightTasksException}
 *       ——监听「没数据」时用来快速定位是掉线 / 没建链 / 任务报错。</li>
 * </ul>
 *
 * <p><b>数据落点</b>：所有推送<b>只写日志文件、不刷控制台</b>(与 {@code [1]} 现状一致)——
 * 交互期控制台留给菜单与动作结果。控制台只回显开/关与逐路建立结果。
 *
 * <p>事后从日志里捞整条状态链, 推荐按 {@code DemoLog} 行格式里的 tag 段过滤(纯 ASCII, 无正则元字符,
 * 且天然把本监听与 {@code [1]} 的推送分开)：
 * <pre>{@code grep 'SDKDemo::GroundFlightMonitor' sdk_demo_log_*.log}</pre>
 * 每条推送行另带 {@value #LOG_PREFIX} 前缀, 按前缀过滤时<b>必须用字面匹配</b>(加 {@code -F})——
 * 不加时方括号会被当成正则字符类(匹配「舱/外/起/降」任一字), 捞出大量无关行：
 * <pre>{@code grep -F '[舱外起降]' sdk_demo_log_*.log}</pre>
 *
 * <p><b>前置条件</b>：
 * <ul>
 *   <li>机场侧 4 路(舱盖/在舱/机场模式码/推杆)需要当前设备<b>含机场</b>; 无机场时自动跳过, 其余照常;</li>
 *   <li>DRC 两路(降落任务状态 / DRC OSD)属 DRC 北向推送, 须先建 DRC 链路才有数据
 *       (订阅本身照常建立成功, 只是不出数)。</li>
 * </ul>
 *
 * <p><b>生命周期</b>：{@link #toggle()} 开/关切换; {@link #close()} 由宿主 demo 在退出收尾时调用。
 * 每路订阅同时登记进会话 {@code SubscriptionRegistry} 兜底——即使收尾被原生 Error 中断,
 * 会话关闭时仍会退订。
 */
public final class GroundFlightMonitor implements AutoCloseable {

    /** 日志 tag。 */
    private static final String TAG = "GroundFlightMonitor";
    /** 推送日志行统一前缀, 便于事后从混合日志里 grep 出整条舱外起降状态链。 */
    private static final String LOG_PREFIX = "[舱外起降] ";

    private final DeviceSession session;
    private final SubscriptionRegistry registry;

    /** 本监听建立的订阅; 与 FlightControlDemo 的 monitorSubs 完全独立。 */
    private final List<Subscription> subs = new ArrayList<Subscription>();

    public GroundFlightMonitor(DeviceSession session, SubscriptionRegistry registry) {
        this.session = session;
        this.registry = registry;
    }

    /** 当前是否处于监听中。 */
    public boolean isRunning() {
        return !subs.isEmpty();
    }

    /** 当前已建立的监听路数。 */
    public int size() {
        return subs.size();
    }

    /** 开/关切换(菜单项语义): 未开启则建立全部监听, 已开启则全部取消。 */
    public void toggle() {
        if (isRunning()) {
            stop();
        } else {
            start();
        }
    }

    // ==================== 开启 ====================

    /**
     * 建立全部舱外起降相关监听。各路独立建立：单路失败只告警并继续其余路
     * (机场侧整体不可用时跳过机场 4 路, 不影响飞机侧)。
     */
    public void start() {
        if (isRunning()) {
            // 防御: 调用方已按开/关分流, 正常不可达。
            console("   舱外起降状态监听已开启(" + subs.size() + " 路), 再次选择该菜单项即关闭");
            return;
        }
        console("\n>> [舱外起降状态监听] 建立监听(推送只落日志文件, 不刷控制台) ...");

        startDroneSide();
        startDockSide();

        if (subs.isEmpty()) {
            console("   未能建立任何监听(请检查设备在线状态与日志)");
            DemoLog.warn(TAG, "舱外起降状态监听: 一路都未建立成功");
            return;
        }
        console("   舱外起降状态监听建立完成: 共 " + subs.size()
                + " 路(推送见日志文件, 前缀 " + LOG_PREFIX.trim() + "; 再次选择该菜单项关闭)");
        console("   提示: 降落任务状态与 DRC OSD 属 DRC 北向推送, 须先建 DRC 链路才有数据");
        DemoLog.info(TAG, "舱外起降状态监听建立完成: " + subs.size() + " 路");
    }

    /** 飞机侧 7 路：模式码、位置状态、任务异常、在线状态、DRC 链路、降落任务状态、DRC OSD。 */
    private void startDroneSide() {
        if (!session.hasDrone()) {
            console("   [跳过] 当前设备无无人机, 飞机侧监听全部跳过");
            DemoLog.warn(TAG, "当前设备无无人机(drone), 跳过飞机侧监听");
            return;
        }

        final Drone drone;
        try {
            drone = session.drone();
        } catch (Exception e) {
            console("   [跳过] 解析无人机实例失败, 飞机侧监听全部跳过: " + Utils.describe(e));
            DemoLog.warn(TAG, "解析无人机实例失败, 跳过飞机侧监听: " + Utils.describe(e));
            return;
        }
        if (drone == null) {
            console("   [跳过] 无人机实例为 null, 飞机侧监听全部跳过");
            DemoLog.warn(TAG, "无人机实例为 null, 跳过飞机侧监听");
            return;
        }

        // 各模块 getter 在 SDK 内部失败时返回 null(见 Drone 各 getter 的 catch 分支), 逐个判空跳过。
        final DroneOsdModule osd = drone.getDroneOsdModule();
        final FlightControlModule fc = drone.getFlightControlModule();
        final DeviceStateModule state = drone.getDeviceStateModule();
        final DrcModule drc = drone.getDrcModule();

        // ---- 飞机模式码: 判定起飞/降落阶段的核心信号 ----
        if (osd == null) {
            skip("飞机模式码/位置状态/任务异常", "获取 DroneOsdModule 失败");
        } else {
            addSub("aircraftModeCode", () -> Subscription.of("aircraftModeCode",
                    osd.listenAircraftModeCode(new FlightHubCallback.AircraftModeCodeCallback() {
                        @Override
                        public void onResult(AircraftModeCode mc) {
                            if (mc == null) {
                                return;
                            }
                            DroneModeCode code = mc.getModeCode();
                            pushLog("listenAircraftModeCode() #AircraftModeCode, 飞机模式码: mode_code="
                                    + (code != null ? code.getValue() : -1)
                                    + "(" + describeDroneModeCode(code) + ")");
                        }
                    }),
                    osd::cancelListenAircraftModeCode));

            addSub("aircraftPositionState", () -> Subscription.of("aircraftPositionState",
                    osd.listenAircraftPositionState(new FlightHubCallback.AircraftPositionStateCallback() {
                        @Override
                        public void onResult(AircraftPositionState s) {
                            if (s == null) {
                                return;
                            }
                            pushLog("listenAircraftPositionState() #AircraftPositionState, 飞机位置状态: isFixed="
                                    + s.getIsFixed() + " quality=" + s.getQuality()
                                    + " gpsNumber=" + s.getGpsNumber() + " rtkNumber=" + s.getRtkNumber());
                        }
                    }),
                    osd::cancelListenAircraftPositionState));

            // 注意命名不对称: listen 方法是 Tasks(复数), 回调接口是 FlightTaskExceptionCallback(单数)。
            addSub("flightTasksException", () -> Subscription.of("flightTasksException",
                    osd.listenFlightTasksException(new FlightHubCallback.FlightTaskExceptionCallback() {
                        @Override
                        public void onResult(FlightTaskException ex) {
                            if (ex == null) {
                                return;
                            }
                            pushLog("listenFlightTasksException() #FlightTaskException, 飞行任务异常: uuid="
                                    + ex.getUuid() + " name=" + ex.getName() + " sn=" + ex.getSn()
                                    + " taskType=" + ex.getTaskType()
                                    + " status=" + ex.getStatus() + "(" + describeTaskStatus(ex.getStatus()) + ")"
                                    + " progress=" + ex.getProgress() + "%"
                                    + " details=" + describeExceptionDetails(ex.getExceptions()));
                        }
                    }),
                    osd::cancelListenFlightTasksException));
        }

        // ---- 在线状态: 监听「没数据」时先看这一路 ----
        if (state == null) {
            skip("飞机在线状态", "获取 DeviceStateModule 失败");
        } else {
            addSub("droneOnlineStatus", () -> Subscription.of("droneOnlineStatus",
                    state.listenDroneOnlineStatus(new FlightHubCallback.DroneOnlineStatusCallback() {
                        @Override
                        public void onResult(DroneOnlineStatus s) {
                            pushLog("listenDroneOnlineStatus() #DroneOnlineStatus, 飞机在线状态: online="
                                    + (s != null && s.isOnline()));
                        }
                    }),
                    state::cancelListenDroneOnlineStatus));
        }

        // ---- DRC 链路状态: DRC 两路没数据时用来判断是不是没建链 ----
        if (drc == null) {
            skip("DRC 链路状态", "获取 DrcModule 失败");
        } else {
            addSub("drcLinkStatus", () -> Subscription.of("drcLinkStatus",
                    drc.listenLinkStatus(new FlightHubCallback.LinkStatusCallback() {
                        @Override
                        public void onResult(LinkStatus s) {
                            if (s == null) {
                                return;
                            }
                            pushLog("listenLinkStatus() #LinkStatus, DRC 链路状态: ctrl=" + s.getCtrlLinkStatus()
                                    + " data=" + s.getDataLinkStatus() + " live=" + s.getLiveLinkStatus());
                        }
                    }),
                    drc::cancelListenLinkStatus));
        }

        // ---- DRC 降落任务状态 + DRC OSD(均须先建 DRC 链路才有数据) ----
        if (fc == null) {
            skip("降落任务状态/DRC OSD", "获取 FlightControlModule 失败");
            return;
        }
        addSub("drcLandingMissionState", () -> Subscription.of("drcLandingMissionState",
                fc.listenDrcLandingMissionState(new FlightHubCallback.DrcLandingMissionStateCallback() {
                    @Override
                    public void onResult(DrcLandingMissionState info) {
                        if (info == null) {
                            return;
                        }
                        // 注: getCover_percent() 在协议侧拼作 cover_precent(协议设计如此), SDK 对外已更正。
                        pushLog("listenDrcLandingMissionState() #DrcLandingMissionState, 降落任务状态: state="
                                + info.getLanding_state() + "(" + describeLandingState(info.getLanding_state()) + ")"
                                + " pauseReason=" + info.getPause_reason()
                                + " coverPercent=" + info.getCover_percent() + "%"
                                + " coverErrCode=" + info.getCover_error_code());
                    }
                }),
                fc::cancelListenDrcLandingMissionState));

        // DRC OSD 是本清单里唯一的高频推送(约 10Hz): 每帧一行日志, 日志文件会明显变大;
        // 只落日志不刷控制台, 且带统一前缀, 事后按关键字过滤即可。
        addSub("drcOsdInfo", () -> Subscription.of("drcOsdInfo",
                fc.listenDrcOsdInfo(new FlightHubCallback.DrcOsdInfoCallback() {
                    @Override
                    public void onResult(DrcOsdInfo info) {
                        if (info == null) {
                            return;
                        }
                        pushLog("listenDrcOsdInfo() #DrcOsdInfo, DRC OSD: height=" + info.getHeight()
                                + " elevation=" + info.getElevation()
                                + " lat=" + info.getLatitude() + " lon=" + info.getLongitude()
                                + " vSpeed=" + info.getVertical_speed()
                                + " hSpeed=" + info.getHorizontal_speed()
                                + " homeDist=" + info.getHome_distance());
                    }
                }),
                fc::cancelListenDrcOsdInfo));
    }

    /** 机场侧 4 路：舱盖、飞机在舱、机场模式码、推杆。机场为可选设备, 无机场时整体跳过。 */
    private void startDockSide() {
        if (!session.hasDock()) {
            console("   [跳过] 当前设备无机场(dock), 机场侧 4 路监听跳过(舱盖/在舱/机场模式码/推杆)");
            DemoLog.info(TAG, "当前设备无机场(dock), 跳过机场侧监听");
            return;
        }

        // 无机场时 session.dock() 会抛异常; 已判 hasDock, 仍用 try/catch 兜实例解析失败。
        final DockOsdModule dockOsd;
        try {
            Dock dock = session.dock();
            if (dock == null) {
                console("   [跳过] 机场实例为 null, 机场侧监听跳过");
                DemoLog.warn(TAG, "解析机场(dock)实例失败, dock is null");
                return;
            }
            dockOsd = dock.getDockOsdModule();
        } catch (Exception e) {
            console("   [跳过] 解析机场或其模块失败, 机场侧监听跳过: " + Utils.describe(e));
            DemoLog.warn(TAG, "解析机场(dock)或其模块失败, 跳过机场侧监听: " + Utils.describe(e));
            return;
        }
        if (dockOsd == null) {
            skip("机场舱盖/在舱/模式码/推杆", "获取 DockOsdModule 失败");
            return;
        }

        // 舱盖状态: 降落至机场的「关→开→关」全过程都在这一路。
        addSub("dockCoverState", () -> Subscription.of("dockCoverState",
                dockOsd.listenDockCoverState(new FlightHubCallback.DockCoverStateCallback() {
                    @Override
                    public void onResult(DockCoverState v) {
                        if (v == null) {
                            return;
                        }
                        pushLog("listenDockCoverState() #DockCoverState, 机场舱盖状态: coverState="
                                + v.getCoverState() + "(" + describeCoverState(v.getCoverState()) + ")");
                    }
                }),
                dockOsd::cancelListenDockCoverState));

        // 飞机在舱: 降落入舱的终态判据。
        addSub("droneInDock", () -> Subscription.of("droneInDock",
                dockOsd.listenDroneInDock(new FlightHubCallback.DroneInDockCallback() {
                    @Override
                    public void onResult(DroneInDock v) {
                        if (v == null) {
                            return;
                        }
                        pushLog("listenDroneInDock() #DroneInDock, 飞机在舱状态: droneInDock="
                                + v.getDroneInDock() + "(" + describeDroneInDock(v.getDroneInDock()) + ")");
                    }
                }),
                dockOsd::cancelListenDroneInDock));

        addSub("dockModeCode", () -> Subscription.of("dockModeCode",
                dockOsd.listenDockModeCode(new FlightHubCallback.DockModeCodeCallback() {
                    @Override
                    public void onResult(DockModeCode v) {
                        if (v == null) {
                            return;
                        }
                        pushLog("listenDockModeCode() #DockModeCode, 机场模式码: modeCode="
                                + v.getModeCode() + "(" + describeDockModeCode(v.getModeCode()) + ")");
                    }
                }),
                dockOsd::cancelListenDockModeCode));

        addSub("dockPutterState", () -> Subscription.of("dockPutterState",
                dockOsd.listenDockPutterState(new FlightHubCallback.DockPutterStateCallback() {
                    @Override
                    public void onResult(DockPutterState v) {
                        if (v == null) {
                            return;
                        }
                        pushLog("listenDockPutterState() #DockPutterState, 机场推杆状态: putterState="
                                + v.getPutterState() + "(" + describePutterState(v.getPutterState()) + ")");
                    }
                }),
                dockOsd::cancelListenDockPutterState));
    }

    // ==================== 关闭 / 收尾 ====================

    /** 取消全部监听(开/关切换的「关」半边)。 */
    public void stop() {
        if (subs.isEmpty()) {
            console("   舱外起降状态监听当前未开启");
            return;
        }
        int n = subs.size();
        console("\n>> [舱外起降状态监听] 取消全部 " + n + " 路监听 ...");
        closeAll();
        DemoLog.info(TAG, "已取消舱外起降状态监听 " + n + " 路");
        console("   已全部取消(可再次选择该菜单项重新开启)");
    }

    /** 宿主 demo 退出收尾时调用; 幂等(未开启时静默返回)。 */
    @Override
    public void close() {
        if (subs.isEmpty()) {
            return;
        }
        int n = subs.size();
        closeAll();
        console("   已取消 " + n + " 路舱外起降状态监听");
        DemoLog.info(TAG, "退出前已取消舱外起降状态监听 " + n + " 路");
    }

    /**
     * 关闭并清空全部订阅。关闭的同时从会话 registry 移除, 避免 registry 长期持有已关闭 sub 引用
     * (长会话下反复开/关会累积冗余条目); {@link Subscription#close()} 幂等且内部吞异常。
     */
    private void closeAll() {
        for (Subscription sub : subs) {
            sub.close();
            registry.remove(sub);
        }
        subs.clear();
    }

    // ==================== 订阅辅助 ====================

    /** 建立单路监听并登记(subs + 会话 registry 双登记); 失败只告警不中断其余路。 */
    private void addSub(String label, Callable<Subscription> factory) {
        try {
            Subscription sub = factory.call();
            subs.add(sub);
            // 兜底: 同步登记进会话 registry——即使 close() 被原生 Error 中断, 会话关闭时仍会退订。
            registry.add(sub);
            console("   [监听] " + label + " 已建立");
        } catch (Exception e) {
            console("   [监听] " + label + " 建立失败: " + Utils.describe(e));
            DemoLog.warn(TAG, "监听建立失败: " + label + " -> " + Utils.describe(e));
        }
    }

    /** 某组监听因前置条件不满足而整体跳过时的统一回显。 */
    private void skip(String group, String reason) {
        console("   [跳过] " + group + ": " + reason);
        DemoLog.warn(TAG, "跳过监听 " + group + ": " + reason);
    }

    // ==================== 输出 ====================

    /** 回显一行到控制台(stdout)。 */
    protected void console(String line) {
        System.out.println(line);
    }

    /**
     * 推送记录：<b>只落日志文件、不刷控制台</b>——监听是持续后台观察, 逐条回显会把菜单 prompt
     * 滚出屏幕。回调在 SDK 线程触发, 只做记录不阻塞。统一前缀便于事后从日志里过滤整条状态链。
     */
    private void pushLog(String line) {
        DemoLog.info(TAG, LOG_PREFIX + line);
    }

    // ==================== 状态码释义(本类自持, 不与其它 demo 共享) ====================

    /** 飞机模式码释义(见 {@link DroneModeCode} 各常量注释)。 */
    private static String describeDroneModeCode(DroneModeCode code) {
        if (code == null) {
            return "无数据";
        }
        switch (code) {
            case STANDBY:                   return "待机";
            case TAKEOFF_PREPARATION:       return "起飞准备";
            case READY_TO_TAKE_OFF:         return "起飞准备完毕";
            case MANUAL_FLIGHT:             return "手动飞行";
            case AUTOMATIC_TAKEOFF:         return "自动起飞";
            case ROUTE_FLIGHT:              return "航线飞行";
            case PANORAMIC_PHOTO:           return "全景拍照";
            case SMART_FOLLOW:              return "智能跟随";
            case ADSB_AVOIDANCE:            return "ADS-B 躲避";
            case RETURN_HOME_AUTOMATICALLY: return "自动返航";
            case AUTOMATIC_LANDING:         return "自动降落";
            case FORCED_LANDING:            return "强制降落";
            case LANDING_WITH_THREE_BLADES: return "三桨叶降落";
            case UPGRADING:                 return "升级中";
            case NOT_CONNECTED:             return "未连接";
            case APAS:                      return "高级手动飞行(智能绕行避障)";
            case JOYSTICK:                  return "虚拟摇杆模式";
            case COMMAND_FLIGHT:            return "指点飞行";
            case RTK_CONVERGING:            return "RTK 收敛中";
            case EXPLORATION_AND_SITING:    return "机场选址中";
            case POI:                       return "POI 环绕";
            case DEPARTURE_POINT:           return "离场航线";
            case RELAY_SITING:              return "中继选址中";
            default:                        return "未知";
        }
    }

    /** 机场舱盖状态释义(见 {@code DockCoverState#getCoverState()} 注释)。 */
    private static String describeCoverState(int state) {
        switch (state) {
            case 0:  return "关闭";
            case 1:  return "打开";
            case 2:  return "半开";
            case 3:  return "舱盖状态异常";
            default: return "未知";
        }
    }

    /** 机场推杆状态释义(见 {@code message.h} DockPutterState: 参考 EA210)。 */
    private static String describePutterState(int state) {
        switch (state) {
            case 0:  return "关闭";
            case 1:  return "打开";
            case 2:  return "半开";
            case 3:  return "推杆状态异常";
            default: return "未知";
        }
    }

    /**
     * 飞机在舱状态释义。
     *
     * <p><b>注意</b>：SDK 未给 {@code drone_in_dock} 定义枚举({@code message.h} 里只是裸 int),
     * 此处按云 API 惯例解释为 {@code 0=不在舱内 / 1=在舱内}, 故日志同时打印原始值——
     * 以原始值为准, 中文仅供参考。
     */
    private static String describeDroneInDock(int v) {
        switch (v) {
            case 0:  return "不在舱内";
            case 1:  return "在舱内";
            default: return "未知";
        }
    }

    /** 机场模式码释义(见 {@code DockModeCode} 类注释)。 */
    private static String describeDockModeCode(int code) {
        switch (code) {
            case 0:  return "空闲中";
            case 1:  return "现场调试中";
            case 2:  return "远程调试中";
            case 3:  return "设备升级中";
            case 4:  return "设备作业中";
            case 5:  return "机场设备未标定";
            case 6:  return "节能状态中";
            case 7:  return "现场急停中";
            case 8:  return "换电中";
            case 9:  return "日志拉取中";
            default: return "未知";
        }
    }

    /** 降落任务状态释义(见 {@code DrcLandingMissionState::landing_state} 注释)。 */
    private static String describeLandingState(int state) {
        switch (state) {
            case 0:  return "初始空闲";
            case 1:  return "等待参数同步";
            case 2:  return "航向对齐";
            case 3:  return "精准降落(有水平源)";
            case 4:  return "无源降落(无水平源, 继续下降)";
            case 5:  return "悬停微调(有水平源)";
            case 6:  return "纯悬停(无水平源)";
            case 7:  return "备降(有源)";
            case 8:  return "备降(无源)";
            case 9:  return "备降(用户 RC 接管)";
            default: return "未知";
        }
    }

    /** 飞行任务状态释义(见 {@code FlightTaskException#getStatus()} 注释)。 */
    private static String describeTaskStatus(int status) {
        switch (status) {
            case 0:  return "待开始";
            case 1:  return "启动失败";
            case 2:  return "执行中";
            case 3:  return "已暂停";
            case 4:  return "已终止";
            case 5:  return "成功";
            case 6:  return "挂起";
            case 7:  return "超时";
            case 8:  return "部分执行";
            case 9:  return "准备中";
            case 10: return "起飞排队中";
            default: return "未知";
        }
    }

    /** 异常详情列表摘要(逐条 code/message; 空列表回 "[]")。 */
    private static String describeExceptionDetails(List<FlightTaskExceptionDetail> details) {
        if (details == null || details.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < details.size(); i++) {
            FlightTaskExceptionDetail d = details.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            if (d == null) {
                sb.append("null");
                continue;
            }
            sb.append("code=").append(d.getCode())
                    .append(" msg=").append(d.getMessage())
                    .append(" taskId=").append(d.getTaskId())
                    .append(" at=").append(d.getHappenAt());
        }
        return sb.append("]").toString();
    }
}
