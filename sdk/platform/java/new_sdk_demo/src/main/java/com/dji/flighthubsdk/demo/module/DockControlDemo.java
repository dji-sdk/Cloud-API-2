// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.DockControlModule;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.demo.base.Menu;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.value.DockCameraPosition;
import com.dji.flighthubsdk.value.DockCoverMode;

import java.util.List;

/**
 * 机场控制模块 Demo(菜单驱动): 舱内/舱外摄像头切换、空调模式、舱盖、补光灯、充电。
 *
 * <p>按 {@code PayloadControlDemo} 的子菜单范式实现, 提供以下功能:
 * <ul>
 *   <li>[1] 切换舱内/舱外摄像头; </li>
 *   <li>[2] 空调模式; </li>
 *   <li>[3] 舱盖 (开启/关闭/强制关闭); </li>
 *   <li>[4] 补光灯 (开启/关闭); </li>
 *   <li>[5] 充电 (开启/关闭)。</li>
 * </ul>
 *
 * <p><b>设计要点: </b>
 * <ul>
 *   <li>[1] 切换摄像头所需的 {@code LiveInfo} 经机场直播源 (range)发现获得, 选源按
 *       <b>本机场 SN 过滤</b>(避免同拓扑他机源被错选; 超时时长可配
 *       {@code dock_control.range_timeout_ms}); range 监听用完即取消; </li>
 *   <li>舱盖 (开/关/强制关闭 3 选 1)、补光灯 (开/关 2 选 1)、充电 (开/关 2 选 1)、空调模式、摄像头位置
 *       共 5 个菜单项 (9 个原子动作经子选择完成, q 可取消); </li>
 *   <li>动作结果同步等待并校验错误码。</li>
 * </ul>
 *
 * <p>机场控制均为一次性设置指令 (HTTP 链路, 无需控制权/DRC 前置), 不产生需退出收尾的状态;
 * 舱盖/充电等物理动作由机场自身安全逻辑把关; <b>强制关闭舱盖</b>绕过常规关闭流程,
 * 执行前须输入 {@code yes} 二次确认 (并确认舱内无异物、飞机已停妥)。
 *
 * <p><b>结果回显: </b>{@code DemoLog} 只写日志文件, 故每个操作项的「开始/[成功]/[超时]/[失败](错误码)」
 * 经 {@link #callItem}(基类 {@link ModuleDemo#callOutcome}/{@link ModuleDemo.Outcome} 三态骨架)
 * 实时回显控制台——应答超时不代表命令未生效 (见 {@link ModuleDemo.Outcome#TIMEOUT}), 对<b>强制关闭舱盖</b>
 * 这类危险动作尤其不能把「未确认」误报成「确认失败」, 故不采用两态回显折叠二者。
 */
public class DockControlDemo extends ModuleDemo {

    /** [1] 切换摄像头前, 等待机场上报可用直播源 (range)的默认超时 (ms)。range 来自 SDK 每 5s 一轮的
     * live_capacity 轮询, 10s 可覆盖 ~2 轮; 相比 {@code LiveStreamDemo} 的 20s(需额外容忍首轮
     * topo 拉取延迟), 本 demo 场景下机场通常已在线, 无需更长等待。 */
    private static final long DEFAULT_RANGE_TIMEOUT_MS = 10000L;

    @Override
    public String name() {
        return "DockControlDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            // 菜单式 demo 必须有交互输入; 正常经 DemoEntry 运行时 input 恒非空, 这里只是防御。
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式机场控制 demo");
            return;
        }
        if (!session.hasDock()) {
            hint("该设备没有机场, 无法运行机场控制 demo");
            return;
        }

        Dock dockOrNull = null;
        try {
            dockOrNull = session.dock();
        } catch (Exception e) {
            DemoLog.warn(name(), "获取机场实例失败, 无法运行机场控制 demo: " + Utils.describe(e));
            return;
        }
        final Dock dock = dockOrNull;

        // 模块 getter 在设备不支持该模块时返回 null(Dock.getDockControlModule/getLiveDeviceModule 内部只对
        // native 指针做判空, 无 catch 分支; 全类仅 getDockOsdModule 一个例外有 try/catch), 显式判空给出诊断,
        // 避免后续调用抛裸 NullPointerException。
        final DockControlModule dc = dock.getDockControlModule();
        if (dc == null) {
            DemoLog.error(name(), "获取 DockControlModule 失败, 无法运行机场控制 demo");
            return;
        }

        final long actionTimeout = defaultTimeout();
        DemoLog.info(name(), "dockSn=" + session.dockSn() + " actionTimeout=" + actionTimeout + "ms");

        Menu.of(this, "执行 " + name())
                .item("切换舱内/舱外摄像头 (changeDockCameraPosition, 需机场有可用直播源)",
                        () -> changeCameraPosition(dock, dc, actionTimeout))
                .item("空调模式            (setDockAirConditionerMode, 0-3)", () -> {
                    // 只放行 0-3: DockAirConditionerState 枚举有 0-9 十个值, 但 4-9(制冷/制热/除湿的
                    // 「退出中」「准备中」)是机场上报的过渡态, 不是可下发的目标模式——
                    // setDockAirConditionerMode 的 SDK javadoc 明确「0=空闲 1=制冷 2=制热 3=除湿」。
                    // 下发 4-9 会被服务端以业务错误码拒绝, 白白让用户以为「demo 支持但机场不支持」。
                    // 口径与 DeviceDebugDemo 的空调子菜单一致 (那处本就只给 4 个选项)。
                    console("   空调模式: 0=空闲 1=制冷 2=制热 3=除湿");
                    console("   (4-9 为机场上报的过渡态: 制冷/制热/除湿的退出中与准备中, 不可下发)");
                    final int mode = Utils.readIntInRange(input, "   请选择空调模式(0-3, q 取消): ", 0, 3, "取消");
                    if (mode == Utils.QUIT) {
                        hint("已取消设置空调模式");
                        return;
                    }
                    callItem(
                        "设置空调模式(" + mode + ")",
                        cb -> dc.setDockAirConditionerMode(mode, cb), actionTimeout
                    );
                })
                .item("舱盖                (setDockCoverState: 开启/关闭/强制关闭, 强制关闭需 yes 确认)", () -> {
                    // DockCoverMode OPEN(0)/CLOSE(1)/FORCE_CLOSE(2)。
                    console("   舱盖动作: 0=开启 1=关闭 2=强制关闭(需二次确认)");
                    int v = Utils.readIntInRange(input, "   请选择舱盖动作(0-2, q 取消): ", 0, 2, "取消");
                    if (v == Utils.QUIT) {
                        hint("已取消舱盖操作");
                        return;
                    }
                    final DockCoverMode mode = v == 0 ? DockCoverMode.OPEN
                            : (v == 1 ? DockCoverMode.CLOSE : DockCoverMode.FORCE_CLOSE);
                    // 强制关闭绕过常规关闭流程, 须输入 yes 确认 (普通开启/关闭由机场自身安全逻辑把关, 不设门)。
                    if (mode == DockCoverMode.FORCE_CLOSE
                        && !confirmDangerous("强制关闭舱盖(请确认舱内无异物、飞机已停妥！)")) {
                        return;
                    }
                    callItem("舱盖-" + coverModeCn(mode), cb -> dc.setDockCoverState(mode, cb), actionTimeout);
                })
                .item("补光灯              (setDockSupplementLightState: 开启/关闭)", () -> {
                    final Boolean on = readOnOff("补光灯");
                    if (on == null) {
                        return;
                    }
                    callItem(
                        "补光灯-" + (on ? "开启" : "关闭"),
                        cb -> dc.setDockSupplementLightState(on, cb), actionTimeout
                    );
                })
                .item("充电                (setDockChargeState: 开启/关闭)", () -> {
                    final Boolean on = readOnOff("充电");
                    if (on == null) {
                        return;
                    }
                    callItem(
                        "充电-" + (on ? "开启" : "关闭"),
                        cb -> dc.setDockChargeState(on, cb), actionTimeout
                    );
                })
                .exitLog("退出机场控制菜单")
                .run();
    }

    // ==================== [1] 切换舱内/舱外摄像头 ====================

    /**
     * 切换舱内/舱外摄像头: {@code changeDockCameraPosition} 需要一路机场
     * {@code LiveInfo}, 经 {@code listenLiveAvailableRange} 一次性发现 (按<b>本机场 SN</b> 过滤),
     * 用完即取消 range 监听。无可用直播源 (机场离线/不支持该摄像头位置切换/live_capacity 尚未上报)时跳过并提示。
     */
    private void changeCameraPosition(Dock dock, DockControlModule dc, long actionTimeout) {
        console("   目标位置: 0=舱内 1=舱外");
        int v = Utils.readIntInRange(input, "   请选择目标位置(0-1, q 取消): ", 0, 1, "取消");
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

        // 登记 registry 双保险: Subscription.close() 内部 try/catch(Throwable) 吞掉 cancel 异常
        // (裸 cancelListenLiveAvailableRange 抛 Throwable 时会传播且无兜底); 即使本方法 finally
        // 因 Error 中断未执行, 会话 close 时 registry 仍会兜底退订。与 LiveStreamDemo/FlySafeUnlockDemo
        // 的 range 监听范式一致。
        final Subscription rangeSub = subscribe("dockLiveRangeForCamera",
            rangeId, ldm::cancelListenLiveAvailableRange);

        LiveInfo picked;
        try {
            picked = await.await(rangeTimeout);
        } catch (FlightHubException e) {
            hint("等待机场直播源超时(" + rangeTimeout + "ms, 请确认机场在线且支持舱内/舱外摄像头), 已取消切换");
            return;
        } finally {
            // range 监听仅用于发现源, 用完即取消; 同步移出 registry 避免已关闭引用长期积累。
            rangeSub.close();
            registry().remove(rangeSub);
        }

        hintDebug("使用直播源: sn=" + picked.getDeviceSn() + " camera=" + picked.getPayloadIndex()
            + " video=" + picked.getVideoId());

        // gatewaySn 传空串 (由 SDK/服务端按 LiveInfo 归属解析)。
        final LiveInfo info = picked;
        callItem(
            "切换摄像头-" + (position == DockCameraPosition.INSIDE ? "舱内" : "舱外"),
            cb -> dc.changeDockCameraPosition(info, position, "", cb), actionTimeout
        );
    }

    /**
     * 从机场 range 列表挑一路<b>属于本机场</b>的源: 本机场 SN 匹配优先, 其次 sn 为空的兜底路;
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

    /** 读取 开启/关闭 子选择; q/EOF 返回 null 表示放弃本次操作。 */
    private Boolean readOnOff(String what) {
        console("   " + what + "动作: 0=开启 1=关闭");
        int v = Utils.readIntInRange(input, "   请选择" + what + "动作(0-1, q 取消): ", 0, 1, "取消");
        if (v == Utils.QUIT) {
            hint("已取消" + what + "操作");
            return null;
        }

        return v == 0;
    }

    /** 舱盖模式中文名 (用于动作标签)。 */
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

    // ---- 控制台结果回显 (范式同 DrcDemo/PayloadControlDemo: 三态结果)----
    // DemoLog 只落滚动日志文件, 交互终端看不到调用结果; 以下把每个测试项的
    // 「开始 / 成功 / 超时 / 失败 (附错误码与消息)」实时回显到 stdout, 让用户直接看清每一步结果。
    // Outcome 枚举 / callOutcome / runOutcome / console / hint /
    // confirmDangerous 均由基类 ModuleDemo 统一提供。

    // callItem 已提升到基类 ModuleDemo, 此处不再重复定义。
}
