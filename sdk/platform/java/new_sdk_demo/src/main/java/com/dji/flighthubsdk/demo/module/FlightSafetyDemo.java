// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightCommanderHeightResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.FlightOutOfControlActionResult;
import com.dji.flighthubsdk.bean.FlightRthAltitudeResult;
import com.dji.flighthubsdk.bean.FlightSafeSyncField;
import com.dji.flighthubsdk.bean.FlightSafeSyncResult;
import com.dji.flighthubsdk.bean.FlightSmartPlanModeResult;
import com.dji.flighthubsdk.core.FlightSettingsModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 飞行安全配置 Demo(菜单驱动)：智能规划模式、失控动作、飞行任务高度、返航高度、空中回传开关。
 *
 * <p>对应 {@code FlightSettingsModule} 全部 5 个设置接口：
 *
 * <pre>
 * 主菜单
 * ├── [1] 智能规划模式    setFlightSmartPlanMode
 * ├── [2] 失控动作        setOutOfControlAction
 * ├── [3] 飞行任务高度    setFlightTaskHeight
 * ├── [4] 返航高度        setRthAltitude
 * └── [5] 空中回传开关    setAirTransferEnable
 * </pre>
 *
 * <p><b>SDK 入口：</b>{@code session.drone().getFlightSettingsModule()}(挂在 Drone 上, 需要飞行器设备)。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>均为即时 HTTP 设置动作, 无进度订阅、无退出收尾, 参数由用户交互输入；</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分；</li>
 *   <li>成功后回显服务端 echo 值与设备影子同步状态({@link FlightSafeSyncResult},
 *       经各结果 bean 的 {@code getDeviceShadowSync()} 获取), 服务端未返回同步信息的接口
 *       静默跳过(见 {@link #printSyncResult}); [5] setAirTransferEnable 回调无结果对象
 *       (仅 error), 成功时只回显本地开关状态。</li>
 * </ul>
 */
public class FlightSafetyDemo extends ModuleDemo {

    @Override
    public String name() {
        return "FlightSafetyDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式飞行安全配置 demo");
            return;
        }
        if (!session.hasDrone()) {
            hint("当前设备无飞行器(Drone), 飞行安全配置模块需要飞行器设备, 无法运行");
            return;
        }

        final FlightSettingsModule module = session.drone().getFlightSettingsModule();
        if (module == null) {
            DemoLog.error(name(), "getFlightSettingsModule() 返回 null");
            console("   获取 FlightSettingsModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-5, q 退出): ", 1, 5);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出飞行安全配置菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: setFlightSmartPlanMode(module, actionTimeout); break;
                    case 2: setOutOfControlAction(module, actionTimeout);  break;
                    case 3: setFlightTaskHeight(module, actionTimeout);    break;
                    case 4: setRthAltitude(module, actionTimeout);         break;
                    case 5: setAirTransferEnable(module, actionTimeout);   break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 智能规划模式 ====================

    private void setFlightSmartPlanMode(FlightSettingsModule module, long timeout) {
        // 注意取值语义与直觉相反: 0=开启, 1=关闭(以 SDK bean FlightSmartPlanModeResult 注释为准)
        final int mode = Utils.readIntInRange(input, "   请输入规划模式(0=开启 1=关闭, q 取消): ", 0, 1);
        if (mode == Utils.QUIT) return;
        console("   设置智能规划模式: mode=" + mode + "(0=开启 1=关闭)");

        final Await<FlightHubError> await = new Await<>(name() + ".setFlightSmartPlanMode");
        final AtomicReference<FlightSmartPlanModeResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("设置智能规划模式", timeout, await, () ->
            module.setFlightSmartPlanMode(mode, new FlightHubCallback.SetFlightSmartPlanModeCallback() {
                @Override
                public void onResult(FlightHubError error, FlightSmartPlanModeResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            FlightSmartPlanModeResult r = ref.get();
            if (r != null) {
                console("   服务器回显: smartPlanningMode=" + r.getSmartPlanningMode());
                printSyncResult(r.getDeviceShadowSync());
            }
        }
    }

    // ==================== [2] 失控动作 ====================

    private void setOutOfControlAction(FlightSettingsModule module, long timeout) {
        // 合法值以 SDK 文档为准: Hover(悬停) / ReturnHome(返航) / Continue(继续任务), 自由输入透传由服务端校验
        String line = Utils.readLine(input, "   请输入失控动作(Hover/ReturnHome/Continue, q 取消): ");
        if (line == null || line.trim().equalsIgnoreCase("q")) return;
        if (line.trim().isEmpty()) {
            hint("失控动作不能为空");
            return;
        }
        final String action = line.trim();
        console("   设置失控动作: action=" + action);

        final Await<FlightHubError> await = new Await<>(name() + ".setOutOfControlAction");
        final AtomicReference<FlightOutOfControlActionResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("设置失控动作", timeout, await, () ->
            module.setOutOfControlAction(action, new FlightHubCallback.SetOutOfControlActionCallback() {
                @Override
                public void onResult(FlightHubError error, FlightOutOfControlActionResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            FlightOutOfControlActionResult r = ref.get();
            if (r != null) {
                console("   服务器回显: outOfControlAction=" + r.getOutOfControlAction());
                printSyncResult(r.getDeviceShadowSync());
            }
        }
    }

    // ==================== [3] 飞行任务高度 ====================

    private void setFlightTaskHeight(FlightSettingsModule module, long timeout) {
        final int height = askInt("   请输入飞行任务高度(单位: 米, q 取消): ");
        if (height == Utils.QUIT) return;
        console("   设置飞行任务高度: height=" + height + "m");

        final Await<FlightHubError> await = new Await<>(name() + ".setFlightTaskHeight");
        final AtomicReference<FlightCommanderHeightResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("设置飞行任务高度", timeout, await, () ->
            module.setFlightTaskHeight(height, new FlightHubCallback.SetFlightTaskHeightCallback() {
                @Override
                public void onResult(FlightHubError error, FlightCommanderHeightResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            FlightCommanderHeightResult r = ref.get();
            if (r != null) {
                if (r.getCommanderFlightHeight() > 0) {
                    console("   服务器回显: commanderFlightHeight=" + r.getCommanderFlightHeight() + "m");
                } else {
                    console("   已下发 height=" + height + "m (服务端未 echo back)");
                }
                printSyncResult(r.getDeviceShadowSync());
            }
        }
    }

    // ==================== [4] 返航高度 ====================

    private void setRthAltitude(FlightSettingsModule module, long timeout) {
        final int altitude = askInt("   请输入返航高度(单位: 米, q 取消): ");
        if (altitude == Utils.QUIT) return;
        console("   设置返航高度: altitude=" + altitude + "m");

        final Await<FlightHubError> await = new Await<>(name() + ".setRthAltitude");
        final AtomicReference<FlightRthAltitudeResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("设置返航高度", timeout, await, () ->
            module.setRthAltitude(altitude, new FlightHubCallback.SetRthAltitudeCallback() {
                @Override
                public void onResult(FlightHubError error, FlightRthAltitudeResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            FlightRthAltitudeResult r = ref.get();
            if (r != null) {
                console("   服务器回显: rthAltitude=" + r.getRthAltitude() + "m");
                printSyncResult(r.getDeviceShadowSync());
            }
        }
    }

    // ==================== [5] 空中回传开关 ====================

    private void setAirTransferEnable(FlightSettingsModule module, long timeout) {
        int state = Utils.readIntInRange(input, "   请输入空中回传开关(0=禁用 1=启用, q 取消): ", 0, 1);
        if (state == Utils.QUIT) return;
        final boolean enabled = state != 0;
        console("   设置空中回传开关: enabled=" + enabled);

        // setAirTransferEnable 的回调仅回传 error(无结果对象), 但回调类型是专属的
        // SetAirTransferEnableCallback 而非 ResultCallbackWithError, 不能走基类 callOutcome
        final Await<FlightHubError> await = new Await<>(name() + ".setAirTransferEnable");
        Outcome o = runOutcome("设置空中回传开关", timeout, await, () ->
            module.setAirTransferEnable(enabled, new FlightHubCallback.SetAirTransferEnableCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            console("   空中回传已" + (enabled ? "启用" : "禁用"));
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 交互读取一个非负整数参数(高度类), 输入 q/EOF 返回 {@link Utils#QUIT} 表示取消, 非数字/负数循环重试。
     *
     * <p>不设上界(各机型限高/返航高度范围不同, 由服务端校验并返回错误码);
     * 拒绝负数除物理语义外, 也避免用户输入 {@code -1} 与 QUIT 哨兵冲突被误判为取消。
     */
    private int askInt(String prompt) {
        while (true) {
            String line = Utils.readLine(input, prompt);
            if (line == null || line.trim().equalsIgnoreCase("q")) {
                return Utils.QUIT;
            }

            try {
                int v = Integer.parseInt(line.trim());
                if (v < 0) {
                    console("   高度参数不能为负数, 请输入非负整数(如 100)或 q 取消");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                console("   输入无效, 请输入非负整数(如 100)或 q 取消");
            }
        }
    }

    /**
     * 打印设置结果中的设备影子同步状态({@link FlightSafeSyncResult}, 经结果 bean 的
     * {@code getDeviceShadowSync()} 获取)：整体 status + 逐字段 synced 标志。
     * 部分接口不 echo back 同步信息, 此时静默跳过, 不打印以免误导。
     */
    private void printSyncResult(FlightSafeSyncResult sync) {
        if (sync == null) {
            return;
        }

        String status = sync.getStatus();
        List<FlightSafeSyncField> fields = sync.getResults();
        boolean hasStatus = status != null && !status.isEmpty();
        boolean hasFields = fields != null && !fields.isEmpty();
        if (!hasStatus && !hasFields) {
            return;
        }
        console("   设备影子同步: status=" + (hasStatus ? status : "(空)"));
        if (hasFields) {
            for (FlightSafeSyncField f : fields) {
                console("     field=" + f.getField() + " synced=" + f.isSynced());
            }
        }
    }

    /** 纯控制台输出(不落日志文件), 用于菜单文案与结果回显。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 提示信息双写：既落日志文件(带 demo 名 tag)又回显控制台, 用于前置条件不满足、取消等需留痕的提示。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：5 个设置项与 {@code FlightSettingsModule} 同名接口一一对应(见类 Javadoc 菜单树)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 飞行安全配置 (Flight Settings) ===\n"
            + "  1.  设置智能规划模式   (setFlightSmartPlanMode)\n"
            + "  2.  设置失控动作       (setOutOfControlAction)\n"
            + "  3.  设置飞行任务高度   (setFlightTaskHeight)\n"
            + "  4.  设置返航高度       (setRthAltitude)\n"
            + "  5.  设置空中回传开关   (setAirTransferEnable)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
