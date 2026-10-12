// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.DeviceDetailInfo;
import com.dji.flighthubsdk.bean.DeviceStatusModel;
import com.dji.flighthubsdk.bean.DockStatusItemResult;
import com.dji.flighthubsdk.bean.DockStatusNetworkStateResult;
import com.dji.flighthubsdk.bean.DockStatusPositionStateResult;
import com.dji.flighthubsdk.bean.DockStatusPropertiesResult;
import com.dji.flighthubsdk.bean.DroneStatusBatteryResult;
import com.dji.flighthubsdk.bean.DroneStatusItemResult;
import com.dji.flighthubsdk.bean.DroneStatusPositionStateResult;
import com.dji.flighthubsdk.bean.DroneStatusPropertiesResult;
import com.dji.flighthubsdk.bean.DroneStatusStorageResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.ModelInfo;
import com.dji.flighthubsdk.bean.SubDeviceInfo;
import com.dji.flighthubsdk.core.DeviceManageModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 设备管理模块 Demo(菜单驱动)：设备详情 + 机场/飞机设备状态查询。
 *
 * <p>覆盖 {@code DeviceManageModule} 的 3 个<b>只读查询</b>接口：
 *
 * <pre>
 * 主菜单
 * ├── [1] 查询设备详情      getDeviceDetailInfo   GET  /device/detail
 * ├── [2] 查询机场设备状态  getDockDeviceStatus   POST /device-properties
 * └── [3] 查询飞机设备状态  getDroneDeviceStatus  POST /device-properties
 * </pre>
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getDeviceManageModule()}(挂在 Workspace 上)。
 *
 * <p><b>为何不含写接口：</b>模块另有 {@code modifyDeviceName}(改名)与 {@code unbindDevice}(解绑)
 * 两个写接口, 本 demo 有意不纳入——解绑会把设备从工作空间移除, 误触后需要重新绑定设备才能恢复,
 * 代价远超 demo 的演示价值。模块余下的 {@code getDeviceList} 与两个监听接口已分别由
 * {@code Devices}(开机选设备)与 {@code DeviceOsdDemo}(第九节推送订阅)覆盖, 此处不重复。
 *
 * <p><b>SN 缺省行为：</b>三个菜单项都允许直接回车, 此时回退到当前会话的 SN——
 * {@code [1]} 用<b>拓扑主设备(网关)</b>的 SN(见 {@link #rootSn()}, 只有它才有 {@code sub_devices}),
 * {@code [2]}/{@code [3]} 分别用 dockSn / droneSn, 省去手敲长串 SN。
 */
public class DeviceManageDemo extends ModuleDemo {

    @Override
    public String name() {
        return "DeviceManageDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式设备管理 demo");
            return;
        }

        final DeviceManageModule module = session.workspace().getDeviceManageModule();
        if (module == null) {
            DemoLog.error(name(), "getDeviceManageModule() 返回 null");
            console("   获取 DeviceManageModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms"
            + ", 会话设备: rootSn=" + rootSn() + " dockSn=" + session.dockSn()
            + " droneSn=" + session.droneSn());

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-3, q 退出): ", 1, 3);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出设备管理菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: getDeviceDetailInfo(module, actionTimeout);  break;
                    case 2: getDockDeviceStatus(module, actionTimeout);  break;
                    case 3: getDroneDeviceStatus(module, actionTimeout); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 查询设备详情 (getDeviceDetailInfo) ====================

    private void getDeviceDetailInfo(DeviceManageModule module, long timeout) {
        final String sn = askSn("设备 SN", rootSn());
        if (sn.isEmpty()) {
            hint("设备 SN 为空, 且当前会话无可回退的主设备 SN, 取消查询");
            return;
        }
        console("\n   查询设备详情: sn=" + sn);

        final Await<FlightHubError> await = new Await<>(name() + ".getDeviceDetailInfo");
        final AtomicReference<DeviceDetailInfo> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询设备详情", timeout, await, () ->
            module.getDeviceDetailInfo(sn, new FlightHubCallback.GetDeviceDetailInfoCallback() {
                @Override
                public void onResult(FlightHubError error, DeviceDetailInfo deviceDetail) {
                    ref.set(deviceDetail);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            printDeviceDetail(ref.get());
        }
    }

    /** 打印设备详情(主设备字段 + 子设备列表), 每行同时落日志文件与控制台。 */
    private void printDeviceDetail(DeviceDetailInfo detail) {
        if (detail == null) {
            hint("设备详情为空(回调返回 null)");
            return;
        }

        both("   device_sn            = " + detail.getDeviceSn());
        both("   nickname             = " + detail.getNickname());
        both("   complete_machine_sn  = " + detail.getCompleteMachineSn());
        both("   online               = " + detail.isOnline());
        both("   device_add_time      = " + detail.getDeviceAddTime());
        both("   firmware_version     = " + detail.getDeviceFirmwareVersion());
        both("   firmware_status      = " + detail.getDeviceFirmwareStatus());
        both("   working_status       = " + detail.getDeviceWorkingStatus());
        both("   model_info           = " + formatModelInfo(detail.getModelInfo()));

        List<SubDeviceInfo> subs = detail.getSubDevices();
        if (subs == null || subs.isEmpty()) {
            both("   sub_devices          = (无)");
            hint("若预期有子设备: /device/detail 只对拓扑主设备(网关)返回 sub_devices, "
                + "查子设备 SN 时该字段不返回。当前会话主设备 SN=" + rootSn());
            return;
        }
        both("   sub_devices(" + subs.size() + "):");
        for (int i = 0; i < subs.size(); i++) {
            SubDeviceInfo s = subs.get(i);
            if (s == null) {
                continue;
            }
            both("     [" + (i + 1) + "] device_sn=" + s.getDeviceSn()
                + "  nickname=" + s.getNickname()
                + "  online=" + s.isOnline()
                + "  firmware_version=" + s.getDeviceFirmwareVersion()
                + "  firmware_status=" + s.getDeviceFirmwareStatus()
                + "  working_status=" + s.getDeviceWorkingStatus());
            both("         flysafe_database_version=" + s.getFlysafeDatabaseVersion()
                + "  parent_sns=" + s.getParentSns());
            both("         model_info=" + formatModelInfo(s.getModelInfo()));
        }
    }

    // ==================== [2] 查询机场设备状态 (getDockDeviceStatus) ====================

    private void getDockDeviceStatus(DeviceManageModule module, long timeout) {
        final List<String> sns = askSnList("机场 SN", session.dockSn());
        if (sns.isEmpty()) {
            hint("机场 SN 列表为空, 且当前会话无可回退的机场 SN, 取消查询");
            return;
        }
        console("\n   查询机场设备状态: sns=" + sns);

        final Await<FlightHubError> await = new Await<>(name() + ".getDockDeviceStatus");
        final AtomicReference<List<DockStatusItemResult>> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询机场设备状态", timeout, await, () ->
            module.getDockDeviceStatus(sns, new FlightHubCallback.DockDeviceStatusListCallback() {
                @Override
                public void onResult(FlightHubError error, List<DockStatusItemResult> items) {
                    ref.set(items);
                    await.set(error);
                }
            })
        );

        if (o != Outcome.SUCCESS) {
            return;
        }
        List<DockStatusItemResult> items = ref.get();
        if (items == null || items.isEmpty()) {
            hint("机场设备状态列表为空(所查 SN 可能不是机场, 或无状态数据)");
            return;
        }
        both("   共 " + items.size() + " 条机场状态:");
        for (int i = 0; i < items.size(); i++) {
            DockStatusItemResult item = items.get(i);
            if (item == null) {
                continue;
            }
            both("   [" + (i + 1) + "] sn=" + item.getSn()
                + "  online=" + item.isOnline()
                + "  updated_time=" + item.getUpdatedTime()
                + "  model=" + formatStatusModel(item.getDeviceModel()));
            printDockProperties(item.getProperties());
        }
    }

    /** 打印机场状态属性(仅挑选排障常用的环境/供电/网络/定位字段, 非全量)。 */
    private void printDockProperties(DockStatusPropertiesResult p) {
        if (p == null) {
            both("       properties = (无)");
            return;
        }
        both("       环境: 温度=" + p.getTemperature() + "℃"
            + "  舱外温度=" + p.getEnvironmentTemperature() + "℃"
            + "  湿度=" + p.getHumidity() + "%"
            + "  雨量=" + p.getRainfall());
        both("       供电: 工作电压=" + p.getWorkingVoltage() + "mV"
            + "  供电电压=" + p.getElectricSupplyVoltage() + "mV"
            + "  AC/DC 输入=" + p.getAcdcPowerInput()
            + "  PoE 输出=" + p.getPoePowerOutput()
            + "  PoE 链路=" + p.getPoeLinkStatus());
        both("       运行: 累计工作时长=" + p.getAccTime() + "s"
            + "  作业次数=" + p.getJobNumber());

        DockStatusNetworkStateResult net = p.getNetworkState();
        if (net != null) {
            both("       网络: type=" + net.getType()
                + "  quality=" + net.getQuality()
                + "  rate=" + net.getRate());
        }
        DockStatusPositionStateResult pos = p.getPositionState();
        if (pos != null) {
            both("       定位: gps=" + pos.getGpsNumber()
                + "  rtk=" + pos.getRtkNumber()
                + "  quality=" + pos.getQuality()
                + "  fixed=" + pos.getFixed()
                + "  calibration=" + pos.getCalibration());
        }
    }

    // ==================== [3] 查询飞机设备状态 (getDroneDeviceStatus) ====================

    private void getDroneDeviceStatus(DeviceManageModule module, long timeout) {
        final List<String> sns = askSnList("飞机 SN", session.droneSn());
        if (sns.isEmpty()) {
            hint("飞机 SN 列表为空, 且当前会话无可回退的飞机 SN, 取消查询");
            return;
        }
        console("\n   查询飞机设备状态: sns=" + sns);

        final Await<FlightHubError> await = new Await<>(name() + ".getDroneDeviceStatus");
        final AtomicReference<List<DroneStatusItemResult>> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询飞机设备状态", timeout, await, () ->
            module.getDroneDeviceStatus(sns, new FlightHubCallback.DroneDeviceStatusListCallback() {
                @Override
                public void onResult(FlightHubError error, List<DroneStatusItemResult> items) {
                    ref.set(items);
                    await.set(error);
                }
            })
        );

        if (o != Outcome.SUCCESS) {
            return;
        }
        List<DroneStatusItemResult> items = ref.get();
        if (items == null || items.isEmpty()) {
            hint("飞机设备状态列表为空(所查 SN 可能不是飞机, 或无状态数据)");
            return;
        }
        both("   共 " + items.size() + " 条飞机状态:");
        for (int i = 0; i < items.size(); i++) {
            DroneStatusItemResult item = items.get(i);
            if (item == null) {
                continue;
            }
            both("   [" + (i + 1) + "] sn=" + item.getSn()
                + "  online=" + item.isOnline()
                + "  updated_time=" + item.getUpdatedTime()
                + "  model=" + formatStatusModel(item.getDeviceModel()));
            printDroneProperties(item.getProperties());
        }
    }

    /** 打印飞机状态属性(仅挑选排障常用的飞行/电池/定位/存储字段, 非全量)。 */
    private void printDroneProperties(DroneStatusPropertiesResult p) {
        if (p == null) {
            both("       properties = (无)");
            return;
        }
        both("       飞行: mode_code=" + p.getModeCode()
            + "(reason=" + p.getModeCodeReason() + ")"
            + "  水平速度=" + p.getHorizontalSpeed()
            + "  垂直速度=" + p.getVerticalSpeed()
            + "  限高=" + p.getHeightLimit());
        both("       返航点: 距离=" + p.getHomeDistance() + "m"
            + "  lat=" + p.getHomeLatitude()
            + "  lng=" + p.getHomeLongitude());
        both("       累计: 里程=" + p.getTotalFlightDistance() + "m"
            + "  架次=" + p.getTotalFlightSorties()
            + "  时长=" + p.getTotalFlightTime() + "s");
        both("       告警阈值: 低电=" + p.getLowBatteryWarningThreshold() + "%"
            + "  严重低电=" + p.getSeriousLowBatteryWarningThreshold() + "%"
            + "  返航剩余电量=" + p.getRemainingPowerForReturnHome() + "%");

        DroneStatusBatteryResult bat = p.getBattery();
        if (bat != null) {
            both("       电池: 剩余=" + bat.getCapacityPercent() + "%"
                + "  返航电量=" + bat.getReturnHomePower() + "%"
                + "  降落电量=" + bat.getLandingPower() + "%"
                + "  剩余飞行时间=" + bat.getRemainFlightTime() + "s"
                + "  电池数=" + (bat.getBatteries() != null ? bat.getBatteries().size() : 0));
        }
        DroneStatusPositionStateResult pos = p.getPositionState();
        if (pos != null) {
            both("       定位: gps=" + pos.getGpsNumber()
                + "  rtk=" + pos.getRtkNumber()
                + "  quality=" + pos.getQuality()
                + "  fixed=" + pos.getFixed());
        }
        DroneStatusStorageResult st = p.getStorage();
        if (st != null) {
            both("       存储: used=" + st.getUsed() + " / total=" + st.getTotal());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 拓扑根(主设备/网关)的 SN —— {@code [1] 查询设备详情} 的缺省值。
     *
     * <p><b>不能用 {@link com.dji.flighthubsdk.demo.runtime.DeviceSession#dockSn()}</b>：
     * {@code dockSn()} 在「主设备非机场」的拓扑里(如 base_station 挂 EA340 子机场)会解析到<b>子设备</b>,
     * 而 {@code /device/detail} 只对拓扑根返回 {@code sub_devices} 数组——拿子设备 SN 去查,
     * 后端连 {@code sub_devices} 这个 key 都不会返回, 子设备列表必然显示"(无)"。
     * 机场/飞机状态查询({@code [2]}/{@code [3]})查的是具体设备的状态, 仍用 dockSn/droneSn。
     */
    private String rootSn() {
        if (session.topo() == null || session.topo().getDevice() == null) {
            return "";
        }
        String sn = session.topo().getDevice().getDeviceSn();
        return sn != null ? sn : "";
    }

    /**
     * 读单个 SN：直接回车回退到 {@code fallback}(会话所选设备的 SN)。
     * 返回已 trim 的字符串, 无输入且无回退值时返回空串。
     */
    private String askSn(String label, String fallback) {
        String def = fallback != null ? fallback : "";
        String line = Utils.readLineEcho(input,
            "   请输入" + label + "(直接回车用当前设备 " + (def.isEmpty() ? "(无)" : def) + "): ");
        if (line == null || line.trim().isEmpty()) {
            return def;
        }
        return line.trim();
    }

    /**
     * 读 SN 列表(逗号分隔)：直接回车回退到 {@code fallback} 单元素列表。
     * 无输入且无回退值时返回空列表。
     */
    private List<String> askSnList(String label, String fallback) {
        String line = Utils.readLineEcho(input,
            "   请输入" + label + "(逗号分隔多个, 直接回车用当前设备 "
                + (fallback == null || fallback.isEmpty() ? "(无)" : fallback) + "): ");
        List<String> out = new ArrayList<>();
        if (line != null) {
            for (String part : line.split(",")) {
                String t = part.trim();
                if (!t.isEmpty()) {
                    out.add(t);
                }
            }
        }
        if (out.isEmpty() && fallback != null && !fallback.isEmpty()) {
            out.add(fallback);
        }
        return out;
    }

    private static String formatModelInfo(ModelInfo m) {
        if (m == null) {
            return "(null)";
        }
        return "{key=" + m.getKey()
            + ", domain=" + m.getDomain()
            + ", type=" + m.getType()
            + ", sub_type=" + m.getSubType()
            + ", name=" + m.getName()
            + ", device_class=" + m.getDeviceClass()
            + ", develop_code=" + m.getDevelopCode() + "}";
    }

    private static String formatStatusModel(DeviceStatusModel m) {
        if (m == null) {
            return "(null)";
        }
        return "{key=" + m.getKey()
            + ", name=" + m.getName()
            + ", device_class=" + m.getDeviceClass() + "}";
    }

    /** 纯控制台输出(不落日志文件), 用于菜单文案与过程提示。 */
    private void console(String line) {
        System.out.println(line);
    }

    /** 查询结果行双写：既落日志文件(带 demo 名 tag)又回显控制台, 便于事后核对接口返回值。 */
    private void both(String line) {
        DemoLog.info(name(), line);
        console(line);
    }

    /** 提示信息双写：用于前置条件不满足、结果为空等需留痕的提示。 */
    private void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：3 个菜单项与 {@code DeviceManageModule} 同名只读接口一一对应(见类 Javadoc 菜单树)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 设备管理 (Device Manage) ===\n"
            + "  1.  查询设备详情       (getDeviceDetailInfo, 单个 SN, 含子设备列表)\n"
            + "  2.  查询机场设备状态   (getDockDeviceStatus, SN 列表)\n"
            + "  3.  查询飞机设备状态   (getDroneDeviceStatus, SN 列表)\n"
            + "\n"
            + "  提示: 三项均可直接回车 —— [1] 用拓扑主设备(网关)SN, [2]/[3] 用机场/飞机 SN\n"
            + "  注意: [1] 只有查拓扑主设备才会返回 sub_devices; 拿子设备 SN 查, 子设备列表必为空\n"
            + "  说明: 本 demo 只含只读查询; 改名/解绑等写接口有意不纳入(误触代价高)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
