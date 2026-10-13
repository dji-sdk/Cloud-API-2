// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.DockControlModule;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.value.DockCameraPosition;
import com.dji.flighthubsdk.value.DockCoverMode;

import java.util.List;

/**
 * 机场控制模块 Demo(菜单驱动)：舱内/舱外摄像头切换、空调模式、舱盖、补光灯、充电。
 *
 * <p>按 {@code PayloadControlDemo} 的子菜单范式实现, 提供以下功能：
 * <ul>
 *   <li>[1] 切换舱内/舱外摄像头；</li>
 *   <li>[2] 空调模式；</li>
 *   <li>[3] 舱盖(开启/关闭/强制关闭)；</li>
 *   <li>[4] 补光灯(开启/关闭)；</li>
 *   <li>[5] 充电(开启/关闭)。</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>[1] 切换摄像头所需的 {@code LiveInfo} 经机场直播源(range)发现获得, 选源按
 *       <b>本机场 SN 过滤</b>(避免同拓扑他机源被错选；超时时长可配
 *       {@code dock_control.range_timeout_ms})；range 监听用完即取消；</li>
 *   <li>9 项(舱盖开/关/强制关闭、补光灯开/关、充电开/关共 7 项各占一项, 另加空调模式、摄像头位置这 2 项
 *       本就各为单项)合并为 5 项菜单, 开/关/模式经子选择完成(q 可取消)；</li>
 *   <li>动作结果同步等待并校验错误码。</li>
 * </ul>
 *
 * <p>机场控制均为一次性设置指令(HTTP 链路, 无需控制权/DRC 前置), 不产生需退出收尾的状态；
 * 舱盖/充电等物理动作由机场自身安全逻辑把关；<b>强制关闭舱盖</b>绕过常规关闭流程,
 * 执行前须输入 {@code yes} 二次确认(并确认舱内无异物、飞机已停妥)。
 *
 * <p><b>结果回显：</b>{@code DemoLog} 只写日志文件, 故每个操作项的「开始/[成功]/[超时]/[失败](错误码)」
 * 经 {@link #callItem}(基类 {@link ModuleDemo#callOutcome}/{@link ModuleDemo.Outcome} 三态骨架)
 * 实时回显控制台——应答超时不代表命令未生效(见 {@link ModuleDemo.Outcome#TIMEOUT}), 对<b>强制关闭舱盖</b>
 * 这类危险动作尤其不能把「未确认」误报成「确认失败」, 故不采用两态回显折叠二者。
 */
public class DockControlDemo extends ModuleDemo {

    /** [1] 切换摄像头前, 等待机场上报可用直播源(range)的默认超时(ms)。range 来自 SDK 每 5s 一轮的
     * live_capacity 轮询, 10s 可覆盖 ~2 轮；相比 {@code LiveStreamDemo} 的 20s(需额外容忍首轮
     * topo 拉取延迟), 本 demo 场景下机场通常已在线, 无需更长等待。 */
    private static final long DEFAULT_RANGE_TIMEOUT_MS = 10000L;

    @Override
    public String name() {
        return "DockControlDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            // 菜单式 demo 必须有交互输入；正常经 DemoEntry 运行时 input 恒非空, 这里只是防御。
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式机场控制 demo");
            return;
        }
        if (!session.hasDock()) {
            hint("该设备没有机场, 无法运行机场控制 demo");
            return;
        }

        final Dock dock = session.dock();

        // 模块 getter 在设备不支持该模块时返回 null(Dock.getDockControlModule/getLiveDeviceModule 内部只对
        // native 指针做判空, 无 catch 分支；全类仅 getDockOsdModule 一个例外有 try/catch), 显式判空给出诊断,
        // 避免后续调用抛裸 NullPointerException。
        final DockControlModule dc = dock.getDockControlModule();
        if (dc == null) {
            DemoLog.error(name(), "获取 DockControlModule 失败, 无法运行机场控制 demo");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "dockSn=" + session.dockSn() + " actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-5, q 退出): ", 1, 5);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出机场控制菜单");
                break;
            }

            // 单个菜单动作失败(含超时、错误码)不退出菜单：记录后回到菜单继续。
            // 原生 Error(如 UnsatisfiedLinkError)不在此拦截, 交由基类 runBody 兜底隔离。
            try {
                dispatch(choice, dock, dc, actionTimeout);
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    /** 菜单分发(编号与类 javadoc 的功能列表一一对应)。 */
    private void dispatch(int choice, Dock dock, DockControlModule dc, long actionTimeout) {
        switch (choice) {
            case 1:
                changeCameraPosition(dock, dc, actionTimeout);
                break;

            case 2: {
                // 模式值直通 SDK(枚举表 DockAirConditionerState 0-9)。
                console("   空调模式: 0=空闲 1=制冷 2=制热 3=除湿 4=制冷退出 5=制热退出"
                    + " 6=除湿退出 7=制冷准备 8=制热准备 9=除湿准备");
                final int mode = Utils.readIntInRange(input, "   请选择空调模式(0-9, q 取消): ", 0, 9);
                if (mode == Utils.QUIT) {
                    hint("已取消设置空调模式");
                    break;
                }
                callItem(
                    "设置空调模式(" + mode + ")",
                    cb -> dc.setDockAirConditionerMode(mode, cb), actionTimeout
                );
                break;
            }

            case 3: {
                // DockCoverMode OPEN(0)/CLOSE(1)/FORCE_CLOSE(2)。
                console("   舱盖动作: 0=开启 1=关闭 2=强制关闭(需二次确认)");
                int v = Utils.readIntInRange(input, "   请选择舱盖动作(0-2, q 取消): ", 0, 2);
                if (v == Utils.QUIT) {
                    hint("已取消舱盖操作");
                    break;
                }
                final DockCoverMode mode = v == 0 ? DockCoverMode.OPEN
                        : (v == 1 ? DockCoverMode.CLOSE : DockCoverMode.FORCE_CLOSE);
                // 强制关闭绕过常规关闭流程, 须输入 yes 确认(普通开启/关闭由机场自身安全逻辑把关, 不设门)。
                if (mode == DockCoverMode.FORCE_CLOSE
                    && !confirmDangerous("强制关闭舱盖(请确认舱内无异物、飞机已停妥！)")) {
                    break;
                }
                callItem("舱盖-" + coverModeCn(mode), cb -> dc.setDockCoverState(mode, cb), actionTimeout);
                break;
            }

            case 4: {
                final Boolean on = readOnOff("补光灯");
                if (on == null) {
                    break;
                }
                callItem(
                    "补光灯-" + (on ? "开启" : "关闭"),
                    cb -> dc.setDockSupplementLightState(on, cb), actionTimeout
                );
                break;
            }

            case 5: {
                final Boolean on = readOnOff("充电");
                if (on == null) {
                    break;
                }
                callItem(
                    "充电-" + (on ? "开启" : "关闭"),
                    cb -> dc.setDockChargeState(on, cb), actionTimeout
                );
                break;
            }

            default:
                break;
        }
    }

    // ==================== [1] 切换舱内/舱外摄像头 ====================

    /**
     * 切换舱内/舱外摄像头：{@code changeDockCameraPosition} 需要一路机场
     * {@code LiveInfo}, 经 {@code listenLiveAvailableRange} 一次性发现(按<b>本机场 SN</b> 过滤),
     * 用完即取消 range 监听。无可用直播源(机场离线/不支持该摄像头位置切换/live_capacity 尚未上报)时跳过并提示。
     */
    private void changeCameraPosition(Dock dock, DockControlModule dc, long actionTimeout) {
        console("   目标位置: 0=舱内 1=舱外");
        int v = Utils.readIntInRange(input, "   请选择目标位置(0-1, q 取消): ", 0, 1);
        if (v == Utils.QUIT) {
            hint("已取消切换摄像头");
            return;
        }
        final DockCameraPosition position = v == 0 ? DockCameraPosition.INSIDE : DockCameraPosition.OUTSIDE;

        final LiveDeviceModule ldm = dock.getLiveDeviceModule();
        if (ldm == null) {
            hint("获取机场 LiveDeviceModule 失败, 无法发现直播源");
            return;
        }
        final long rangeTimeout = params.node("dock_control").longVal("range_timeout_ms", DEFAULT_RANGE_TIMEOUT_MS);
        console("   等待机场直播源(最长 " + rangeTimeout + "ms) ...");

        final String dockSn = session.dockSn();
        final Await<LiveInfo> await = new Await<LiveInfo>("dockLiveRangeForCamera");
        int rangeId = ldm.listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
            @Override
            public void onResult(List<LiveInfo> infos) {
                LiveInfo picked = pickDockLiveInfo(infos, dockSn);
                if (picked != null) {
                    await.set(picked);
                }
            }
        });
        if (rangeId == -1) {
            hint("机场直播源监听建立失败(listenId=-1), 无法切换摄像头");
            return;
        }

        LiveInfo picked;
        try {
            picked = await.await(rangeTimeout);
        } catch (FlightHubException e) {
            hint("等待机场直播源超时(" + rangeTimeout + "ms, 请确认机场在线且支持舱内/舱外摄像头), 已取消切换");
            return;
        } finally {
            // range 监听仅用于发现源, 用完即取消。
            ldm.cancelListenLiveAvailableRange(rangeId);
        }

        hint("使用直播源: sn=" + picked.getDeviceSn() + " camera=" + picked.getPayloadIndex()
            + " video=" + picked.getVideoId());

        // gatewaySn 传空串(由 SDK/服务端按 LiveInfo 归属解析)。
        final LiveInfo info = picked;
        callItem(
            "切换摄像头-" + (position == DockCameraPosition.INSIDE ? "舱内" : "舱外"),
            cb -> dc.changeDockCameraPosition(info, position, "", cb), actionTimeout
        );
    }

    /**
     * 从机场 range 列表挑一路<b>属于本机场</b>的源：本机场 SN 匹配优先, 其次 sn 为空的兜底路；
     * 全部明确属于其它设备则返回 null(继续等待, 宁缺勿错——错选他机源会把切换指令发给错误的流)。
     */
    private LiveInfo pickDockLiveInfo(List<LiveInfo> infos, String dockSn) {
        if (infos == null || infos.isEmpty()) {
            return null;
        }
        LiveInfo unknown = null;
        for (LiveInfo li : infos) {
            if (li == null) {
                continue;
            }
            String sn = li.getDeviceSn();
            if (dockSn != null && dockSn.equals(sn)) {
                return li;
            }
            if ((sn == null || sn.isEmpty()) && unknown == null) {
                unknown = li;
            }
        }
        if (unknown != null) {
            DemoLog.warn(name(), "选中源未携带 deviceSn(无法验证归属), 兜底采用: payload="
                + unknown.getPayloadIndex() + " video_id=" + unknown.getVideoId());
        }
        return unknown;
    }

    // ==================== 交互辅助 ====================

    /**
     * 危险动作二次确认：须输入 {@code yes}(不区分大小写)才执行, 其他输入(含空行/EOF)一律取消。
     * 本模块仅「强制关闭舱盖」设门(绕过常规关闭流程)；范式同 FlightControlDemo。
     */
    private boolean confirmDangerous(String action) {
        String line = Utils.readLine(input,
            "\n   警告: 即将执行「" + action + "」\n   输入 yes 确认执行, 其他任意输入取消: ");
        boolean ok = line != null && line.equalsIgnoreCase("yes");
        if (!ok) {
            hint("已取消: " + action);
        } else {
            DemoLog.info(name(), "用户确认执行: " + action);
        }

        return ok;
    }

    /** 读取 开启/关闭 子选择；q/EOF 返回 null 表示放弃本次操作。 */
    private Boolean readOnOff(String what) {
        console("   " + what + "动作: 0=开启 1=关闭");
        int v = Utils.readIntInRange(input, "   请选择" + what + "动作(0-1, q 取消): ", 0, 1);
        if (v == Utils.QUIT) {
            hint("已取消" + what + "操作");
            return null;
        }

        return v == 0;
    }

    /** 舱盖模式中文名(用于动作标签)。 */
    private static String coverModeCn(DockCoverMode mode) {
        switch (mode) {
            case OPEN:
                return "开启";
            case CLOSE:
                return "关闭";
            default:
                return "强制关闭";
        }
    }

    // ---- 控制台结果回显(范式同 DrcDemo/PayloadControlDemo: 三态结果)----
    // DemoLog 只落滚动日志文件, 交互终端看不到调用结果；以下把每个测试项的
    // 「开始 / 成功 / 超时 / 失败(附错误码与消息)」实时回显到 stdout, 让用户直接看清每一步结果。
    // Outcome 枚举 / callOutcome / runOutcome 由基类 ModuleDemo 统一提供。

    /** 回显一行到控制台(stdout)。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 提示/说明信息：写日志文件的同时回显控制台。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    /** 执行一个「ResultCallbackWithError 动作」并回显结果；仅明确成功返回 true(超时视为失败)。 */
    private boolean callItem(String label, ActionInvoker action, long timeoutMs) {
        return callOutcome(label, action, timeoutMs) == Outcome.SUCCESS;
    }

    /** 机场控制菜单文案(每轮循环打印；编号与类 javadoc 的功能列表一致)。 */
    private String menu() {
        return "\n"
            + "  1.  切换舱内/舱外摄像头 (changeDockCameraPosition, 需机场有可用直播源)\n"
            + "  2.  空调模式            (setDockAirConditionerMode, 0-9)\n"
            + "  3.  舱盖                (setDockCoverState: 开启/关闭/强制关闭, 强制关闭需 yes 确认)\n"
            + "  4.  补光灯              (setDockSupplementLightState: 开启/关闭)\n"
            + "  5.  充电                (setDockChargeState: 开启/关闭)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
