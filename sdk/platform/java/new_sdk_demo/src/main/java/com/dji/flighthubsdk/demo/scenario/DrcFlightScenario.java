// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.scenario;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AircraftHomeDistance;
import com.dji.flighthubsdk.bean.AircraftHomeGeographicalInfo;
import com.dji.flighthubsdk.bean.AircraftLocation;
import com.dji.flighthubsdk.bean.AircraftModeCode;
import com.dji.flighthubsdk.bean.AircraftPayloadGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftPayloadsGimbalAttitude;
import com.dji.flighthubsdk.bean.AircraftSpeed;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.demo.base.Precondition;
import com.dji.flighthubsdk.demo.base.Scenario;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 场景: DRC 虚拟摇杆飞行操控。
 *
 * <p>流程:
 * <ol>
 *   <li>订阅飞机 OSD 状态推送(6 路: Home 点距离/坐标、位置、模式码、速度、云台姿态);</li>
 *   <li>获取飞行控制权、负载控制权;</li>
 *   <li>建立 DRC 链路(虚拟摇杆的硬前置);</li>
 *   <li>启动 20 Hz 后台摇杆发送线程——无按键时持续发中性帧(1024), 保证松杆不漂移;</li>
 *   <li>交互操控菜单: G=起飞, W/S/A/D/Q/E/I/K/Z/X=摇杆, R=返航, QUIT=退出;</li>
 *   <li>收尾: 停止摇杆 → 补中性尾帧 → 补返航(如已起飞未返航)→ 断 DRC → 释放控制权 → 取消监听。</li>
 * </ol>
 *
 * <p><b>摇杆键位说明</b>(大小写不敏感, 每次输入 + Enter 后保持 {@code stick_hold_ms} ms, 随后自动归中性):
 * <pre>
 *   W / S  →  俯仰(前进 / 后退)     pitch       1024+step / 1024-step
 *   A / D  →  横滚(左移 / 右移)     roll        1024-step / 1024+step
 *   Q / E  →  偏航(左转 / 右转)     yaw         1024-step / 1024+step
 *   I / K  →  油门(上升 / 下降)     throttle    1024+step / 1024-step
 *   Z / X  →  云台俯仰(上仰 / 下俯) gimbalPitch 1024+step / 1024-step
 * </pre>
 *
 * <p>参数取自 {@code demo_params.json} 的 {@code drc_flight_scenario} 分组:
 * {@code stick_step}(默认 450)、{@code stick_hold_ms}(默认 1000)。
 * 起飞参数复用 {@code takeoff} 分组。
 *
 * <p><b>收尾保证</b>(finally, 每步各自兜异常互不干扰):
 * <ol>
 *   <li>停止调度器 + 当前线程补发 {@value #STICK_NEUTRAL_TAIL_FRAMES} 帧中性尾帧;</li>
 *   <li>若飞机已起飞且未执行返航 → 补发返航指令(在断 DRC 之前);
 *       <b>与 PoiScenario 的策略对比</b>：PoiScenario 收尾时选择「不自动返航, 仅告警」——
 *       本场景选择相反策略(自动补发返航), 是因为 DRC 全程处于「持续手动操控」语境, 用户对飞机
 *       状态持续可见/可控, 断链后自动补一次返航是让飞机脱离无人操控状态的合理默认动作; 而 POI
 *       等自动化任务场景里用户可能未持续盯盘, 服务端对空域状况的判断更可靠, 故选保守告警。
 *       两者均为有意设计而非缺陷, 使用者接入生产环境时应按自身场景重新评估该取舍。</li>
 *   <li>断开 DRC 链路;</li>
 *   <li>释放负载控制权、飞行控制权;</li>
 *   <li>显式取消全部监听。</li>
 * </ol>
 */
public class DrcFlightScenario extends Scenario {

    private static final int STICK_NEUTRAL = 1024;
    /** 20 Hz: 每帧间隔 50ms。 */
    private static final long STICK_INTERVAL_MS = 50L;
    /** 停止摇杆后补发的中性尾帧数, 确保飞控侧收到松杆信号。 */
    private static final int STICK_NEUTRAL_TAIL_FRAMES = 10;

    @Override
    public String name() {
        return "DrcFlightScenario";
    }

    @Override
    protected void steps() throws Exception {
        if (!session.hasDrone()) {
            DemoLog.warn(name(), "该设备没有无人机, 无法运行 DRC 飞行场景");
            System.out.println("   该设备没有无人机, 无法运行 DRC 飞行场景");
            return;
        }

        final Drone drone;
        try {
            drone = session.drone();
        } catch (Exception e) {
            DemoLog.warn(name(), "获取无人机实例失败, 无法运行 DRC 飞行场景: " + Utils.describe(e));
            return;
        }
        if (drone == null) {
            DemoLog.warn(name(), "获取无人机实例失败(session.drone() 返回 null), 无法运行 DRC 飞行场景");
            return;
        }

        final FlightControlModule fc = drone.getFlightControlModule();
        final ControlAuthModule auth = drone.getControlAuthModule();
        final DrcModule drc = drone.getDrcModule();
        final DroneOsdModule osd = drone.getDroneOsdModule();

        if (fc == null || auth == null || drc == null || osd == null) {
            throw new FlightHubException(
                "获取模块失败: fc=" + (fc != null)
                + " auth=" + (auth != null) + " drc=" + (drc != null)
                + " osd=" + (osd != null)
            );
        }

        final String payloadIndex = payloadIndex();
        final long defaultTimeout = defaultTimeout();
        final Node cfg = params.node("drc_flight_scenario");

        // stick_step 限制在 [1, 660]: 660 = STICK_NEUTRAL - STICK_MIN = 最大偏移；0 无意义。
        final int step = Math.max(1, Math.min(660, cfg.intVal("stick_step", 450)));
        // stick_hold_ms 至少 200ms, 否则飞控几乎无法感知。
        final long holdMs = Math.max(200L, cfg.longVal("stick_hold_ms", 1000L));

        // 收尾判断标志: 「下发即可能生效」语义, 回调超时不代表未生效。
        boolean flightAuthAttempted = false;
        boolean payloadAuthAttempted = false;
        boolean drcConnectAttempted = false;
        boolean tookOff = false;
        boolean returnHomeAttempted = false;

        final List<Subscription> subs = new ArrayList<Subscription>();
        ScheduledExecutorService stickScheduler = null;

        // 当前目标杆量(默认中性)；摇杆线程读此值发送；主线程在按键时写入。两者通过 AtomicReference 同步。
        final AtomicReference<StickControlData> currentStick =
            new AtomicReference<StickControlData>(neutral());
        // 杆量有效截止时间戳: 超过后摇杆线程自动切回中性, 实现「松杆」语义。
        final AtomicLong stickExpiry = new AtomicLong(0L);

        try {
            // ---- Step 1: OSD 监听 ----
            step("订阅 OSD: Home 点距离 / Home 地理坐标 / 飞机位置 / 模式码 / 速度 / 云台姿态");

            subs.add(subscribe("homeDistance",
                    osd.listenAircraftHomeDistance(new FlightHubCallback.AircraftHomeDistanceCallback() {
                        @Override
                        public void onResult(AircraftHomeDistance v) {
                            if (v == null) {
                                return;
                            }
                            DemoLog.info(
                                name(), "[OSD] listenAircraftHomeDistance() #AircraftHomeDistance,home_distance="
                                + String.format("%.1f", v.getHomeDistance()) + "m"
                            );
                        }
                    }),
                    osd::cancelListenAircraftHomeDistance));

            subs.add(subscribe("homeGeoInfo",
                    osd.listenAircraftHomeGeographicalInfo(
                        new FlightHubCallback.AircraftHomeGeographicalInfoCallback() {
                            @Override
                            public void onResult(AircraftHomeGeographicalInfo v) {
                                if (v == null) {
                                    return;
                                }
                                DemoLog.info(
                                    name(), "[OSD] listenAircraftHomeGeographicalInfo() #AircraftHomeGeographicalInfo,home_lat=" + v.getHomeLatitude()
                                    + " home_lon=" + v.getHomeLongitude()
                                );
                            }
                        }),
                    osd::cancelListenAircraftHomeGeographicalInfo));

            subs.add(subscribe("location",
                    osd.listenAircraftLocation(new FlightHubCallback.AircraftLocationCallback() {
                        @Override
                        public void onResult(AircraftLocation v) {
                            if (v == null) {
                                return;
                            }
                            DemoLog.info(
                                name(), "[OSD] listenAircraftLocation() #AircraftLocation,lat=" + v.getLatitude()
                                + " lon=" + v.getLongitude()
                                + " height=" + String.format("%.1f", v.getHeight()) + "m"
                                + " elev=" + String.format("%.1f", v.getElevation()) + "m"
                            );
                        }
                    }),
                    osd::cancelListenAircraftLocation));

            subs.add(subscribe("modeCode",
                    osd.listenAircraftModeCode(new FlightHubCallback.AircraftModeCodeCallback() {
                        @Override
                        public void onResult(AircraftModeCode v) {
                            if (v == null) {
                                return;
                            }
                            DemoLog.info(name(), "[OSD] listenAircraftModeCode() #AircraftModeCode,mode_code=" + v.getModeCode());
                        }
                    }),
                    osd::cancelListenAircraftModeCode));

            subs.add(subscribe("speed",
                    osd.listenAircraftSpeed(new FlightHubCallback.AircraftSpeedCallback() {
                        @Override
                        public void onResult(AircraftSpeed v) {
                            if (v == null) {
                                return;
                            }
                            DemoLog.info(
                                name(), "[OSD] listenAircraftSpeed() #AircraftSpeed,h_speed="
                                + String.format("%.1f", v.getHorizontalSpeed()) + "m/s"
                                + " v_speed=" + String.format("%.2f", v.getVerticalSpeed()) + "m/s"
                            );
                        }
                    }),
                    osd::cancelListenAircraftSpeed));

            subs.add(subscribe("gimbalAttitude",
                    osd.listenAircraftPayloadsGimbalAttitude(
                        new FlightHubCallback.AircraftPayloadsGimbalAttitudeCallback() {
                            @Override
                            public void onResult(AircraftPayloadsGimbalAttitude v) {
                                if (v == null || v.getPayloadsGimbalAttitude() == null) {
                                    return;
                                }
                                for (AircraftPayloadGimbalAttitude item : v.getPayloadsGimbalAttitude()) {
                                    DemoLog.info(
                                        name(), "[OSD] listenAircraftPayloadsGimbalAttitude() #AircraftPayloadsGimbalAttitude,gimbal idx=" + item.getPayloadIndex()
                                        + " pitch=" + String.format("%.1f", item.getGimbalPitch())
                                        + " roll=" + String.format("%.1f", item.getGimbalRoll())
                                        + " yaw=" + String.format("%.1f", item.getGimbalYaw())
                                    );
                                }
                            }
                        }),
                    osd::cancelListenAircraftPayloadsGimbalAttitude));

            // ---- Step 2: 获取飞行控制权 ----
            step("获取飞行控制权");
            flightAuthAttempted = true;
            Precondition.acquireFlightControl(drone, defaultTimeout);

            // ---- Step 3: 获取负载控制权 ----
            step("获取负载控制权(payloadIndex=" + payloadIndex + ")");
            payloadAuthAttempted = true;
            call("获取负载控制权", cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb), defaultTimeout);

            // ---- Step 4: 建立 DRC 链路 ----
            step("建立 DRC 链路(虚拟摇杆硬前置)");
            drcConnectAttempted = true;
            syncResult("建立 DRC 链路", drc::connectDrcLink, defaultTimeout);
            long linkSettleMs = params.node("drc").longVal("link_settle_ms", 3000L);
            DemoLog.info(name(), "DRC 建链成功, 等待链路稳定 " + linkSettleMs + "ms ...");
            sleep(linkSettleMs);

            // ---- Step 5: 启动 20Hz 摇杆发送后台线程 ----
            step("启动 20Hz 摇杆发送线程(无按键时持续发中性帧, 保证松杆不漂移)");
            stickScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "DrcStickSender");
                t.setDaemon(true);
                return t;
            });

            stickScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    // stickExpiry 到期自动回中性, 实现按键抬起后的「松杆」。
                    StickControlData data = System.currentTimeMillis() < stickExpiry.get()
                        ? currentStick.get() : neutral();
                    try {
                        fc.drcSendStickControl(data);
                    } catch (Throwable t) {
                        DemoLog.warn(name(), "[摇杆线程] 发送异常: " + Utils.describe(t));
                    }
                }
            }, 0, STICK_INTERVAL_MS, TimeUnit.MILLISECONDS);

            // ---- Step 6: 交互操控菜单 ----
            step("进入交互操控菜单");
            printControlMenu(step, holdMs);

            while (true) {
                String line = Utils.readLine(input, "\n> ");
                if (line == null) {
                    DemoLog.warn(name(), "输入流已结束(EOF/stdin 关闭), 退出操控菜单");
                    break;
                }
                String cmd = line.trim().toLowerCase();

                if (cmd.isEmpty()) {
                    printControlMenu(step, holdMs);
                    continue;
                }

                // 退出
                if (cmd.equals("quit")) {
                    DemoLog.info(name(), "用户退出操控菜单");
                    break;
                }

                // 一键起飞
                if (cmd.equals("g")) {
                    // SDK-P1-10: 起飞是不可逆的危险动作, 下发前须二次确认(对齐 FlightControlDemo.confirmDangerous) 。
                    String confirm = Utils.readLine(input,
                        "\n   警告: 即将执行「DRC 一键起飞(飞机将真实起飞!)」\n   输入 yes 确认执行, 其他任意输入取消: ");
                    if (confirm == null || !confirm.equalsIgnoreCase("yes")) {
                        System.out.println("   已取消起飞");
                        DemoLog.info(name(), "用户取消一键起飞");
                        continue;
                    }
                    DemoLog.info(name(), "用户确认执行一键起飞");
                    System.out.println(">> 下发一键起飞指令 ...");
                    // 指令失败不终止场景: 兜 Throwable(对齐全工程 JNI Error 隔离策略),
                    // 打印告警后回菜单, 由用户决定重试或 QUIT 退出——飞行中因一次指令失败
                    // 踢出操控菜单会让操作者失去对空中飞机的手动操控手段。
                    try {
                        tookOff = true;   // 下发前置位: 回调超时不代表未生效, 收尾据此兜底补返航。
                        FlightHubError err =
                            Precondition.takeoffResult(drone, params, timeout("takeoff_timeout_ms"));
                        // 范围修正(对齐 FlyToScenario/PoiScenario): 服务端以明确业务码拒绝
                        // (serverCode != 0)时飞机确定未起飞, 复位 tookOff, 避免收尾对未起飞
                        // 的飞机补发返航。传输层歧义失败(serverCode==0)保持保守置位。
                        if (err != null && !err.isSuccess() && err.getServerCode() != 0) {
                            tookOff = false;
                        }
                        if (err != null && err.isSuccess()) {
                            System.out.println("   起飞指令已下发(关注 demo_logs/ OSD 推送观察飞行状态)");
                        } else {
                            String detail = err == null ? "null error"
                                : ("code=" + err.getErrorCode() + " serverCode=" + err.getServerCode()
                                + " msg=" + err.getServerMessage());
                            System.out.println("   起飞指令失败: " + detail + " (可重试或输入 QUIT 退出)");
                            DemoLog.warn(name(), "一键起飞失败: " + detail);
                        }
                    } catch (Throwable t) {
                        System.out.println("   起飞指令异常: " + Utils.describe(t) + " (可重试或输入 QUIT 退出)");
                        DemoLog.warn(name(), "一键起飞异常(不终止场景): " + Utils.describe(t));
                    }
                    continue;
                }

                // 返航
                if (cmd.equals("r")) {
                    System.out.println(">> 下发返航(ReturnHome)指令 ...");
                    DemoLog.info(name(), "用户触发返航");
                    // 下发返航前先置中杆量并使 hold 失效: DRC 摇杆输入语义上优先于自动飞行,
                    // 若 hold 窗口内的非中性帧继续发送, 存在打断返航的风险。调度器继续以 20Hz
                    // 发送中性帧(不影响返航), 用户如需重新操控可再次按摇杆键显式恢复。
                    currentStick.set(neutral());
                    stickExpiry.set(0L);
                    returnHomeAttempted = true;   // 下发前置位。
                    // 指令失败不终止场景(理由同 G 分支): 网络抖动导致的一次超时不应踢出操控菜单。
                    try {
                        call("返航", cb -> fc.returnHome(cb), defaultTimeout);
                        System.out.println("   返航指令已下发(摇杆已置中; 如需继续操控请再次按摇杆键)");
                    } catch (Throwable t) {
                        System.out.println("   返航指令失败: " + Utils.describe(t) + " (可重试或输入 QUIT 退出)");
                        DemoLog.warn(name(), "返航失败(不终止场景, returnHomeAttempted 保持置位, 收尾不再重复补发): "
                            + Utils.describe(t));
                    }
                    continue;
                }

                // 摇杆按键
                StickControlData stick = buildStick(cmd, step);
                if (stick != null) {
                    String summary = stickSummary(stick);
                    currentStick.set(stick);
                    stickExpiry.set(System.currentTimeMillis() + holdMs);
                    System.out.println("   摇杆: " + summary + "  (持续 " + holdMs + "ms)");
                    DemoLog.info(name(), "摇杆: cmd=" + cmd + " " + summary
                        + " hold=" + holdMs + "ms");
                } else {
                    System.out.println("   未知命令 '" + cmd + "', 输入空行查看菜单");
                }
            }

        } finally {
            // ---- 收尾(每步各自兜异常, 一步失败不阻断后续) ----
            step("收尾：停止摇杆 → 补中性尾帧 → 补返航(如需) → 断 DRC → 释放控制权 → 取消监听");

            // 1) 立即停止调度器(不再发任何杆量)。
            if (stickScheduler != null) {
                final ScheduledExecutorService schedulerToStop = stickScheduler;
                quiet("停止摇杆调度器", () -> schedulerToStop.shutdownNow());
            }

            // 2) 当前线程补发中性尾帧: 确保飞控侧收到松杆信号, 防止惯性漂移。
            //    DRC 链路此时仍有效(断链在后续步骤), 发送可成功。
            if (drcConnectAttempted) {
                quiet("发送中性松杆尾帧", () -> {
                    StickControlData n = neutral();
                    for (int i = 0; i < STICK_NEUTRAL_TAIL_FRAMES; i++) {
                        try {
                            fc.drcSendStickControl(n);
                            Thread.sleep(STICK_INTERVAL_MS);
                        } catch (Throwable ignored) {
                            break;
                        }
                    }
                    DemoLog.info(name(), "中性松杆尾帧已发送(" + STICK_NEUTRAL_TAIL_FRAMES + " 帧)");
                });
            }

            // 3) 已起飞且未执行返航 → 补发返航(须在断 DRC 之前: 返航走 HTTP, 不依赖 DRC, 但
            //    保持与 FlyToScenario「补停在断 DRC 前」相同的顺序策略, 语义更清晰)。
            if (tookOff && !returnHomeAttempted) {
                quiet("补发返航指令", () -> {
                    DemoLog.info(name(), "[收尾] 已起飞且未执行返航, 补发返航指令");
                    call("收尾返航", cb -> fc.returnHome(cb), defaultTimeout);
                    DemoLog.info(name(), "[收尾] 返航指令已下发");
                });
            }

            // 4) 断开 DRC 链路。
            if (drcConnectAttempted) {
                quiet("断开 DRC 链路",
                    () -> syncResult("断开 DRC 链路", drc::disconnectDrcLink, defaultTimeout));
            }

            // 5) 释放负载控制权、飞行控制权。
            if (payloadAuthAttempted) {
                quiet("释放负载控制权",
                    () -> call("释放负载控制权",
                        cb -> auth.releasePayloadControlAuthority(payloadIndex, cb),
                        defaultTimeout)
                );
            }
            if (flightAuthAttempted) {
                Precondition.releaseFlightControl(drone, defaultTimeout);   // 内部吞异常仅告警
            }

            // 6) 显式取消全部监听(subscribe 已同步登记会话 registry, 双保险；close 幂等)。
            for (Subscription sub : subs) {
                sub.close();
            }
            DemoLog.info(name(), "已取消监听 " + subs.size() + " 路");
        }
    }

    // ==================== 摇杆工具方法 ====================

    /** 全轴中性杆量(均为 {@value #STICK_NEUTRAL})。 */
    private static StickControlData neutral() {
        return new StickControlData();
    }

    /**
     * 根据单字母命令生成摇杆数据；未知命令返回 null。
     *
     * <p>正方向定义(与 SDK StickControlData 注释一致):
     * pitch&gt;1024=前进, roll&gt;1024=右移, yaw&gt;1024=右转, throttle&gt;1024=上升,
     * gimbalPitch&gt;1024=上仰。
     */
    private static StickControlData buildStick(String cmd, int step) {
        StickControlData s = neutral();
        switch (cmd) {
            case "w": s.setPitch(STICK_NEUTRAL + step);         break;
            case "s": s.setPitch(STICK_NEUTRAL - step);         break;
            case "a": s.setRoll(STICK_NEUTRAL - step);          break;
            case "d": s.setRoll(STICK_NEUTRAL + step);          break;
            case "q": s.setYaw(STICK_NEUTRAL - step);           break;
            case "e": s.setYaw(STICK_NEUTRAL + step);           break;
            case "i": s.setThrottle(STICK_NEUTRAL + step);      break;
            case "k": s.setThrottle(STICK_NEUTRAL - step);      break;
            case "z": s.setGimbalPitch(STICK_NEUTRAL + step);   break;
            case "x": s.setGimbalPitch(STICK_NEUTRAL - step);   break;
            default:  return null;
        }
        return s;
    }

    /** 将摇杆各轴当前值格式化为可读字符串, 用于日志和控制台输出。 */
    private static String stickSummary(StickControlData s) {
        return
            "roll=" + s.getRoll() + " pitch=" + s.getPitch()
            + " throttle=" + s.getThrottle() + " yaw=" + s.getYaw()
            + " gimbal_pitch=" + s.getGimbalPitch();
    }

    /** 向控制台打印操控键位菜单; 进入循环前及用户输入空行时调用。 */
    private static void printControlMenu(int step, long holdMs) {
        System.out.println("\n========= DRC 虚拟摇杆操控  (step=" + step + ", hold=" + holdMs + "ms) =========");
        System.out.println("  G         一键起飞");
        System.out.println("  W / S     俯仰: 前进 / 后退    (pitch  +" + step + " / -" + step + ")");
        System.out.println("  A / D     横滚: 左移 / 右移    (roll   -" + step + " / +" + step + ")");
        System.out.println("  Q / E     偏航: 左转 / 右转    (yaw    -" + step + " / +" + step + ")");
        System.out.println("  I / K     油门: 上升 / 下降    (throttle +" + step + " / -" + step + ")");
        System.out.println("  Z / X     云台: 上仰 / 下俯    (gimbal_pitch +" + step + " / -" + step + ")");
        System.out.println("  R         返航(Return Home)");
        System.out.println("  QUIT      退出场景(输入字符串\"QUIT\", 自动清理资源)");
        System.out.println("  (空行)    重新显示此菜单");
        System.out.println("  \n提示: OSD 推送仅落日志文件, 请关注 demo_logs/ 观察飞行状态");
        System.out.println("================================================================");
    }
}
