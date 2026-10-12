// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 设备发现工具：从 Workspace 拉取设备列表、按角色解析 drone/机场(dock) 的 {@link Device} 句柄。
 *
 * <p>把设备查找逻辑收敛为可复用的静态方法：
 * {@link #drone(DeviceTopo)}(子设备=飞机)/ {@link #dock(DeviceTopo)}(主设备即机场)/
 * {@link #isDockLike(Device)}。SN 由调用方从返回的 Device 取({@link Device#getDeviceSn()})。
 */
public final class Devices {

    private static final String TAG = "Devices";

    /**
     * 设备 class 常量(与 SDK deviceClass 字符串一致)。
     *
     * <p>注意：标准大疆机场(Dock/Dock2/Dock3)的<b>主设备</b> class 是 {@code "airport"} 而非
     * {@code "dock"}; 字面 {@code "dock"} 只在 base_station 下的联网机场子设备(EA340)里出现。
     * 因此"是不是机场"要用 {@link #isDockLike(Device)}(airport ∪ dock)判断, 别只比 {@code "dock"}。
     * 其余 class(rc / base_station / relay)当前无需区分, 未定义常量。
     */
    public static final String CLASS_DRONE = "drone";
    public static final String CLASS_AIRPORT = "airport";
    public static final String CLASS_DOCK = "dock";

    private Devices() {}

    /** 同步获取设备拓扑列表。 */
    public static List<DeviceTopo> fetchDeviceList(Workspace workspace, long timeoutMs) {
        final Await<Result> await = new Await<Result>("getDeviceList");
        workspace.getDeviceManageModule().getDeviceList(new FlightHubCallback.DeviceListCallback() {
            @Override
            public void onResult(List<DeviceTopo> devices, FlightHubError error) {
                // 打印设备 topo 列表(主设备 + 各子设备详细信息), 便于核对设备在线与型号。
                logDeviceList(devices, error);
                await.set(new Result(devices, error));
            }
        });

        Result r = await.await(timeoutMs);
        if (r.error != null && !r.error.isSuccess()) {
            throw new FlightHubException("获取设备列表失败: " + describe(r.error));
        }
        if (r.devices == null || r.devices.isEmpty()) {
            throw new FlightHubException("设备列表为空");
        }

        return r.devices;
    }

    /** 打印设备拓扑列表详情：每个拓扑打印主设备一行 + 各子设备一行(含 SN/class/name/nickname/model/online)。 */
    private static void logDeviceList(List<DeviceTopo> devices, FlightHubError error) {
        if (error != null && !error.isSuccess()) {
            DemoLog.error(TAG, "getDeviceList 回调错误: " + describe(error));
            return;
        }

        int n = devices != null ? devices.size() : 0;
        DemoLog.info(TAG, "设备列表：共 " + n + " 个 DeviceTopo.");
        if (devices == null) {
            return;
        }

        DemoLog.info(TAG, Utils.dashLine(140));
        for (int i = 0; i < devices.size(); i++) {
            // 日志版附带在线标记(withOnline=true), 便于排查"设备离线"类问题。
            for (String ln : formatTopoLines(i + 1, devices.get(i), true)) {
                DemoLog.info(TAG, ln);
            }
        }

        DemoLog.info(TAG, Utils.dashLine(140));
    }

    /**
     * 把一个拓扑格式化成「编号主设备 + 缩进子设备树」多行(选择列表与日志共用), 样式：
     * <pre>
     * 1. rc RM510(SN: YOUR_DEVICE_SN)
     *     └─> drone M3T(SN: YOUR_DEVICE_SN)
     * </pre>
     * 主设备无子设备时给一行 {@code     └─> (无子设备)}。
     *
     * @param withOnline true 时每行尾部附紧凑在线标记 {@code  [✓ online]}/{@code  [✗ offline]}
     *                   (日志诊断与交互选择列表都用——离线子设备如「在线机场下的离线飞机」一眼可辨)
     */
    private static List<String> formatTopoLines(int no, DeviceTopo topo, boolean withOnline) {
        List<String> lines = new ArrayList<String>();
        lines.add(no + ". " + labelWithSn(topo.getDevice(), withOnline));
        List<Device> subs = topo.getSubDevices();
        if (subs == null || subs.isEmpty()) {
            lines.add("    └─> (无子设备)");
        } else {
            for (Device d : subs) {
                if (d != null) {
                    lines.add("    └─> " + labelWithSn(d, withOnline));
                }
            }
        }

        return lines;
    }

    /**
     * 设备一行标签：{@code <class> <name>(SN: <sn>)}, 名称取 nickname → deviceName → sn(见 {@link #label(Device)})。
     * {@code withOnline} 为 true 时在末尾附紧凑在线标记 {@code  [✓online]}/{@code  [✗offline]}。
     */
    private static String labelWithSn(Device d, boolean withOnline) {
        if (d == null) {
            return "unknown(SN: )";
        }
        String s = label(d) + "(SN: " + d.getDeviceSn() + ")";
        if (withOnline) {
            s += d.isOnline() ? "  [✓ online]" : "  [✗ offline]";
        }
        return s;
    }

    /** 只保留在线主设备的拓扑(主设备非空且 isOnline)。 */
    public static List<DeviceTopo> fetchOnlineDeviceList(Workspace workspace, long timeoutMs) {
        List<DeviceTopo> all = fetchDeviceList(workspace, timeoutMs);
        List<DeviceTopo> onlineDeviceTopoList = new ArrayList<DeviceTopo>();
        for (DeviceTopo t : all) {
            Device main = t.getDevice();
            if (main != null && main.isOnline()) {
                onlineDeviceTopoList.add(t);
            }
        }

        if (onlineDeviceTopoList.isEmpty()) {
            throw new FlightHubException("无在线设备(共 " + all.size() + " 个设备, 均离线)");
        }

        return onlineDeviceTopoList;
    }

    /**
     * 在一个拓扑里找指定 class 的设备(主设备或子设备)。
     * 注意: 基于当前的 1个主设备(一般是网关设备) + 子设备列表的 topo 架构;
     * 若以后的产品迭代 topo 架构变化, 则需要修改代码适配。
     */
    public static Device deviceOf(DeviceTopo topo, String deviceClass) {
        if (topo == null) {
            return null;
        }

        Device main = topo.getDevice();
        if (main != null && deviceClass.equals(main.getDeviceClass())) {
            return main;
        }
        List<Device> subs = topo.getSubDevices();
        if (subs != null) {
            for (Device d : subs) {
                if (d != null && deviceClass.equals(d.getDeviceClass())) {
                    return d;
                }
            }
        }

        return null;
    }

    /** 拓扑里的飞机：class=drone 的设备(始终为子设备); 无则 null。 */
    public static Device drone(DeviceTopo topo) {
        return deviceOf(topo, CLASS_DRONE);
    }

    /**
     * 拓扑里的机场(dock)：主设备即机场(官方 {@code getDockInstance(topo.device.sn)});
     * 主设备非机场(如 base_station 主设备)时回退扫子设备。无则 null。
     */
    public static Device dock(DeviceTopo topo) {
        if (topo == null) {
            return null;
        }
        if (isDockLike(topo.getDevice())) {
            return topo.getDevice();
        }
        List<Device> subs = topo.getSubDevices();
        if (subs != null) {
            for (Device d : subs) {
                if (isDockLike(d)) {
                    return d;
                }
            }
        }

        return null;
    }

    /** 设备 class 是否为机场：{@code airport}(标准机场主设备)或 {@code dock}(联网机场子设备)。 */
    public static boolean isDockLike(Device d) {
        if (d == null) {
            return false;
        }
        String c = d.getDeviceClass();
        return CLASS_AIRPORT.equals(c) || CLASS_DOCK.equals(c);
    }

    /**
     * 交互式选择设备：打印在线设备编号列表, 读用户选择。
     *
     * <p>只有一个设备时自动选中; 用户输入 q / EOF 返回 {@code null}。
     */
    public static DeviceTopo selectInteractive(List<DeviceTopo> list, BufferedReader in) {
        if (list.size() == 1) {
            DemoLog.info(TAG, "仅一个在线设备, 自动选中");
            return list.get(0);
        }
        Utils.printHeader("[在线(主)设备列表]");
        for (int i = 0; i < list.size(); i++) {
            printTopoLine(i + 1, list.get(i));
        }
        int idx = Utils.readIntInRange(in, "\n请选择设备编号(q 退出): ", 1, list.size());
        if (idx == Utils.QUIT) {
            return null;
        }
        return list.get(idx - 1);
    }

    /**
     * 打印一行设备(主设备 + 缩进子设备), 样式：
     * <pre>
     *     1. rc RM510(SN: YOUR_DEVICE_SN)  [✓ online]
     *         └─> drone M3T(SN: YOUR_DEVICE_SN)  [✗ offline]
     * </pre>
     */
    private static void printTopoLine(int no, DeviceTopo topo) {
        // 交互选择列表也附在线标记(withOnline=true)：列表只按「主设备在线」过滤,
        // 子设备可能离线(如在线机场下的离线飞机), 选择前一眼可辨, 免得选完拉流才发现无源。
        for (String ln : formatTopoLines(no, topo, true)) {
            System.out.println(ln);
        }
    }

    /** 打印所选拓扑的详细信息(主设备 + 各子设备)。 */
    public static void printInfo(DeviceTopo topo) {
        Utils.printHeader("[已选择设备]");
        Device main = topo.getDevice();
        if (main != null) {
            Utils.printKeyValue("主设备", label(main));
            Utils.printKeyValue("SN", main.getDeviceSn());
            Utils.printKeyValue("class", main.getDeviceClass());
            Utils.printKeyValue("model", main.getModelKey());
            Utils.printKeyValue("online", String.valueOf(main.isOnline()));
        }
        List<Device> subs = topo.getSubDevices();
        if (subs != null) {
            for (Device d : subs) {
                if (d != null) {
                    Utils.printKeyValue("子设备", label(d) + " (SN: " + d.getDeviceSn() + ")");
                }
            }
        }
    }

    /** 拓扑标签：以主设备为准。 */
    public static String label(DeviceTopo topo) {
        return label(topo.getDevice());
    }

    /** 设备标签：deviceClass + 名称(nickname → deviceName → sn 回退)。 */
    public static String label(Device device) {
        if (device == null) {
            return "unknown";
        }
        String cls = device.getDeviceClass() != null ? device.getDeviceClass() : "";
        String name = notEmpty(device.getNickname())
                ? device.getNickname()
                : (notEmpty(device.getDeviceName()) ? device.getDeviceName() : device.getDeviceSn());
        return (cls + " " + name).trim();
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static String describe(FlightHubError e) {
        return "code=" + e.getErrorCode() + " serverCode=" + e.getServerCode()
                + " deviceCode=" + e.getDeviceCode() + " msg=" + e.getServerMessage();
    }

    private static final class Result {
        final List<DeviceTopo> devices;
        final FlightHubError error;
        Result(List<DeviceTopo> devices, FlightHubError error) {
            this.devices = devices;
            this.error = error;
        }
    }
}
