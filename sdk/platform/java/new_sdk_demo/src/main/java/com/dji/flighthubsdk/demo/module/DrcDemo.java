// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.LinkStatus;
import com.dji.flighthubsdk.core.CameraControlModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.function.Consumer;

/**
 * DRC 远程调试模块 Demo(菜单驱动)。
 *
 * <p>DRC(Direct Remote Control)是「云端直连无人机」的低时延操控链路：先取得负载/飞行控制权、
 * 建立 DRC 链路, 链路可用后即可下发实时操控指令(本 demo 以「云台重置」作为最小可用性验证)。
 * 本类按菜单选择逐项演示以下四步, 覆盖一条完整的 DRC 生命周期：
 * <ol>
 *   <li><b>获取负载控制权</b>——{@code ControlAuthModule.acquirePayloadControlAuthority}, 云台/相机类操控的前置; </li>
 *   <li><b>DRC 链路建立</b>——{@code DrcModule.connectDrcLink}; </li>
 *   <li><b>云台重置</b>——{@code CameraControlModule.drcResetGimbal}, 一条经 DRC 下发的操控指令, 用于验证链路可用性; </li>
 *   <li><b>DRC 链路断开</b>——{@code DrcModule.disconnectDrcLink}。</li>
 * </ol>
 *
 * <p>建议按 1→2→3→4 的顺序操作; 菜单不强制顺序, 但会在缺少前置时给出提示。菜单循环执行,
 * 输入 {@code q}(或到达输入流末尾)返回上级菜单: 退出前会<b>尽力而为地收尾</b>——已建链则断开、
 * 已占权则释放负载控制权(见 {@link #teardown}), 避免把飞机留在「DRC 受控 / 占权」状态影响后续 demo;
 * 随后由基类 {@link #runInSession} 统一退订(链路状态监听)并关闭本轮会话。
 *
 * <p><b>超时语义</b>(对齐 {@code FlightControlDemo}/{@code PayloadControlDemo})：获取类动作
 * (获取控制权/建链)超时说明<b>应答未到而命令可能已生效</b>(迟到应答被忽略), 按「可能已生效」置位
 * 状态标志, 退出收尾照常还原; 释放/断开类仅明确成功才清位(安全方向)。
 *
 * <p><b>结果回显</b>：{@code DemoLog} 只写滚动日志文件, 交互终端看不到调用结果; 故本 demo 经
 * {@link #runOutcome}(由 {@link #callItem}/{@link #syncItem}/{@link #callOutcome}/{@link #syncOutcome}
 * 调用)把每个测试项的「开始 / [成功] / [超时] / [失败](附错误码与消息)」实时回显到控制台(stdout),
 * 并同步落日志文件, 链路状态推送亦以 {@code [推送]} 前缀回显, 让用户直接看清每一步的调用结果。
 *
 * <p><b>回调类型差异</b>：{@code connectDrcLink}/{@code disconnectDrcLink} 用
 * {@link FlightHubCallback.ResultCallback}, 与 {@link #callOutcome} 依赖的
 * {@link FlightHubCallback.ResultCallbackWithError} 不同, 故按同样范式单独提供 {@link #syncOutcome};
 * 二者最终都汇入 {@link #runOutcome} 的「下发 → 等待 → 校验」三态骨架。
 */
public class DrcDemo extends ModuleDemo {

    @Override
    public String name() {
        return "DrcDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            // 菜单式 demo 必须有交互输入; 正常经 DemoEntry 运行时 input 恒非空, 这里只是防御。
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式 DRC demo");
            return;
        }
        if (!session.hasDrone()) {
            // 先于 session.drone() 判定(对齐 FlightControlDemo): 无 drone 时 drone() 会直接抛异常。
            hint("该设备没有无人机, 无法运行 DRC demo");
            return;
        }

        final Drone drone = session.drone();

        // 模块 getter 在 SDK 内部失败时会返回 null(见 Drone.getDrcModule/getControlAuthModule 的
        // catch 分支; getCameraControlModule 目前无此 guard, 构造失败会抛出而非返回 null), 故仍
        // 统一显式判空并给出清晰诊断, 避免后续 drc.listenLinkStatus(...) / auth.*/cam.* 抛裸 NullPointerException。
        final DrcModule drc = drone.getDrcModule();
        final ControlAuthModule auth = drone.getControlAuthModule();
        final CameraControlModule cam = drone.getCameraControlModule();
        if (drc == null || auth == null || cam == null) {
            DemoLog.error(
                name(), "获取模块失败, 无法运行 DRC demo: drc=" + (drc != null)
                + " auth=" + (auth != null) + " cam=" + (cam != null));
            return;
        }

        final String payloadIndex = payloadIndex();

        final Node cfg = params.node("drc");
        // 配置里的 reset_mode 是不受信来源: 越界值在此夹取回默认 0, 保证下发 SDK 的模式恒在 [0,3]。
        final int defaultResetMode = clampResetMode(cfg.intVal("gimbal_reset_mode", 0));
        final long settleMs = cfg.longVal("link_settle_ms", 3000);
        final long actionTimeout = timeout("default_callback_timeout_ms");

        DemoLog.info(name(), "payloadIndex=" + payloadIndex + ", 默认 reset_mode=" + defaultResetMode);

        // 订阅 DRC 链路状态(会话结束由 registry 自动退订), 便于观察 建链/断链 的状态迁移。
        subscribe(
            "DrcLinkStatus",
            drc.listenLinkStatus(new FlightHubCallback.LinkStatusCallback() {
                @Override
                public void onResult(LinkStatus s) {
                    if (s == null) {
                        return;
                    }
                    // 链路状态推送直接反映 DRC 链路可用性: 既落日志文件, 也回显控制台。
                    String line = "DRC 链路状态: "
                        + "控制链路状态 ctrl=" + s.getCtrlLinkStatus()
                        + ", 数据链路状态 data=" + s.getDataLinkStatus()
                        + ", 直播链路状态 live=" + s.getLiveLinkStatus();
                    DemoLog.info(name(), "listenLinkStatus() #LinkStatus, " + line);
                    console("   [推送] " + line);
                }
            }),
            id -> drc.cancelListenLinkStatus(id)
        );

        // 仅用于给出顺序提示与收尾判断(是否已占权/已建链), 不强制流程顺序。
        boolean gotAuth = false;
        boolean linked = false;

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-4, q 退出): ", 1, 4);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出 DRC 菜单");
                    break;
                }

                // 单个菜单动作失败(含超时、错误码)不退出菜单：记录后回到菜单继续。
                // 原生 Error(如 UnsatisfiedLinkError)不在此拦截, 交由基类 runBody 兜底隔离。
                try {
                    switch (choice) {
                        case 1:
                            // 超时≠失败: 命令可能已生效(应答迟到被忽略), 按「可能已占权」置位, 退出收尾照常释放。
                            if (callOutcome("获取负载控制权",
                                    cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb), actionTimeout)
                                    != Outcome.FAILED) {
                                gotAuth = true;
                            }
                            break;

                        case 2:
                            if (!gotAuth) {
                                hint("尚未获取负载控制权, 建议先执行菜单 [1]");
                            }
                            // 超时按「链路可能已建」置位, 退出收尾照常断开——按失败处理会让收尾漏断,
                            // 把实际已建立(应答迟到)的链路永久留在受控态。
                            if (syncOutcome("DRC 链路建立", drc::connectDrcLink, actionTimeout) != Outcome.FAILED) {
                                linked = true;
                                hint("等待 " + settleMs + "ms 观察链路稳定(看上方 [推送] 链路状态)...");
                                sleep(settleMs);
                            }
                            break;

                        case 3:
                            if (!linked) {
                                hint("DRC 链路尚未建立, 建议先执行菜单 [2]; 云台重置需链路可用");
                            }
                            int mode = readResetMode(defaultResetMode);
                            if (callItem("云台重置(reset_mode=" + mode + ")",
                                    cb -> cam.drcResetGimbal(payloadIndex, mode, cb), actionTimeout)) {
                                hint("请观察云台是否已复位——复位成功即表明 DRC 链路可用");
                            }
                            break;

                        case 4:
                            if (syncItem("DRC 链路断开", drc::disconnectDrcLink, actionTimeout)) {
                                linked = false;
                            }
                            break;

                        default:
                            break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            // 退出前尽力而为收尾：把飞机从「DRC 受控 / 占权」状态还原, 避免影响后续 demo。
            teardown(drc, auth, payloadIndex, linked, gotAuth, actionTimeout);
        }
    }

    /**
     * 退出菜单前的尽力而为收尾：已建链则断链, 已占权则释放负载控制权。
     *
     * <p>本方法在 {@link #run} 的 {@code finally} 中调用, 已处于退出路径：callItem/syncItem 内部
     * 已兜 {@code Exception} 并回显成败; 每步再经 {@link #quietStep} 兜 {@code Throwable}
     * (SDK 为 JNI, 原生 Error 不得中断后续收尾), 一步失败不影响另一步, 异常只记录不外抛
     * (与基类会话关闭协同, 不破坏 {@code runBody} 的收尾)。
     */
    private void teardown(DrcModule drc, ControlAuthModule auth, String payloadIndex,
            boolean linked, boolean gotAuth, long actionTimeout) {
        if (!linked && !gotAuth) {
            return;
        }
        console("\n>> [退出收尾] 还原 DRC 受控 / 占权状态 ...");
        if (linked) {
            quietStep("DRC 链路断开(收尾)",
                    () -> syncItem("DRC 链路断开(收尾)", drc::disconnectDrcLink, actionTimeout));
        }
        if (gotAuth) {
            quietStep(
                "释放负载控制权(收尾)",
                () -> callItem("释放负载控制权(收尾)", cb -> auth.releasePayloadControlAuthority(payloadIndex, cb), actionTimeout)
            );
        }
    }

    /** 收尾单步的 Throwable 兜底：记录后继续, 不中断后续收尾、不掩盖主异常。 */
    private void quietStep(String label, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            console("   [收尾] " + label + " 异常: " + Utils.describe(t));
            DemoLog.error(name(), "[收尾] " + label + " 异常: " + Utils.describe(t), t);
        }
    }

    // ---- 控制台结果回显(范式同 FlightControlDemo/PayloadControlDemo: 三态结果)----
    // DemoLog 只落滚动日志文件, 交互终端看不到调用结果; 以下把每个测试项的
    // 「开始 / 成功 / 超时 / 失败(附错误码与消息)」实时回显到 stdout, 让用户直接看清每一步结果。
    // Outcome 枚举 / callOutcome / syncOutcome / runOutcome 由基类 ModuleDemo 统一提供。

    /** 回显一行到控制台(stdout)。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 提示/说明信息：写日志文件的同时回显控制台。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    /** 执行一个「ResultCallbackWithError 动作」并回显结果; 仅明确成功返回 true(超时视为失败)。 */
    private boolean callItem(String label, ActionInvoker action, long timeoutMs) {
        return callOutcome(label, action, timeoutMs) == Outcome.SUCCESS;
    }

    /** 执行一个「ResultCallback 动作」(DRC 连接/断开)并回显结果; 仅明确成功返回 true。 */
    private boolean syncItem(String label, Consumer<FlightHubCallback.ResultCallback> action, long timeoutMs) {
        return syncOutcome(label, action, timeoutMs) == Outcome.SUCCESS;
    }

    /**
     * 读取云台重置模式(0=回中,1=向下,2=偏航回中,3=俯仰向下); 空行/非法/无交互回退默认值。
     */
    private int readResetMode(int def) {
        String line = Utils.readLine(input, "\n请选择 reset_mode: 0=回中 1=向下 2=偏航回中 3=俯仰向下 (回车默认 " + def + "): ");
        if (line == null || line.isEmpty()) {
            return def;
        }
        try {
            int v = Integer.parseInt(line);
            if (v >= 0 && v <= 3) {
                return v;
            }
        } catch (NumberFormatException ignored) {
            // 落到下方警告 + 默认值
        }

        DemoLog.warn(name(), "无效 reset_mode='" + line + "', 使用默认 " + def);
        return def;
    }

    /** 夹取云台重置模式到合法区间 [0,3]; 越界记录告警并回退默认 0。 */
    private int clampResetMode(int mode) {
        if (mode < 0 || mode > 3) {
            DemoLog.warn(name(), "reset_mode=" + mode + " 超出范围[0,3], 回退默认 0");
            return 0;
        }
        return mode;
    }

    /** DRC 菜单文案(每轮循环打印一次; 具体输入提示由 readIntInRange 单独打印)。 */
    private String menu() {
        return "\n"
            + "  1. 获取负载控制权    (acquirePayloadControlAuthority)\n"
            + "  2. DRC 链路建立    (connectDrcLink)\n"
            + "  3. 云台重置    (drcResetGimbal) —— 测试 DRC 链路可用性\n"
            + "  4. DRC 链路断开    (disconnectDrcLink)\n"
            + "  q. 返回上级菜单\n";
    }
}
