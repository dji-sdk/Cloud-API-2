// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.LiveSyncCreateParams;
import com.dji.flighthubsdk.bean.LiveSyncCreateResult;
import com.dji.flighthubsdk.bean.LiveSyncDeviceItemV2;
import com.dji.flighthubsdk.bean.LiveSyncDeviceV2;
import com.dji.flighthubsdk.bean.LiveSyncDevicesV2Result;
import com.dji.flighthubsdk.bean.LiveSyncItem;
import com.dji.flighthubsdk.bean.LiveSyncListParams;
import com.dji.flighthubsdk.bean.LiveSyncListResult;
import com.dji.flighthubsdk.bean.LiveSyncSchemaOption;
import com.dji.flighthubsdk.bean.LiveSyncSummaryResult;
import com.dji.flighthubsdk.bean.LiveSyncUpdateParams;
import com.dji.flighthubsdk.core.LiveSyncModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 旁路推流 Demo(菜单驱动)：把设备摄像头直播流转推至 RTMP / RTSP / GB28181 第三方平台。
 *
 * <p>对应 {@code LiveSyncModule} 全部 6 个接口(即 PaaS 旁路推流 /manage/api/v1/paas/c2c/live-sync 系列):
 *
 * <pre>
 * 主菜单
 * ├── [1] 可用设备列表   getLiveSyncDevicesV2 —— 列出旁路推流可用的机场/无人机(含摄像头能力)
 * ├── [2] 创建转码器     createLiveSync       —— 按 schema 创建(rtmp/rtsp/gb28181, 参数来自 demo_params.json)
 * ├── [3] 转码器列表     getLiveSyncList      —— 分页查询当前已创建的转码器
 * ├── [4] 开关转码器     updateLiveSync       —— 仅 enable 开关(启动/停止推流)
 * ├── [5] 更新转码器配置 updateLiveSync       —— 变更 schema/schema_option/SN/镜头/清晰度
 * ├── [6] 删除转码器     deleteLiveSync
 * └── [7] 汇总统计       getLiveSyncSummary   —— 总数/启用数
 * </pre>
 *
 * <p><b>SDK 入口：</b>{@code workspace.getLiveSyncModule()}——与 {@code AutoRecordModule} 同为
 * <b>项目级(workspace)配置模块</b>(PAAS 路径不带 project_id, 去组织/去鉴权), 自身不依赖设备会话。
 * 本 demo 仍走标准设备会话入口(入口自动拉设备/选设备), 但仅借用会话拿到的 workspace 引用;
 * 创建转码器时的目标 SN 默认取当前 session 设备, 也可用 [1] 列出的任意设备或手动输入。
 *
 * <p><b>参数(demo_params.json 的 "live_sync" 节)：</b>见 {@link #createFromParams(LiveSyncModule, long)}。
 * schema_option 按 schema 类型分开配置: rtmp 用 url; rtsp 用 username/password; gb28181 用
 * server_* / device_* / local_* / device_password 这些前缀字段。注意 DRTC 链路(EA340/0-130-0
 * + camera 122-0-0) 走标准 GB28181 SIP: local_port 必须非 0, 且对端 server_ip:server_port
 * 需为真正能应答 REGISTER 并收 RTP 的国标平台, 否则任务只会"注册超时"而推不出流。
 */
public class LiveSyncDemo extends ModuleDemo {

    @Override
    public String name() {
        return "LiveSyncDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式旁路推流 demo");
            return;
        }
        final LiveSyncModule module = session.workspace().getLiveSyncModule();
        if (module == null) {
            hint("获取 LiveSyncModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }
        final long actionTimeout = timeout("default_callback_timeout_ms");
        // 创建转码器后端耗时较长(含 DRTC 建链/直播调度, 实测可到 ~30s), 单独用更长的回调超时,
        // 避免"接口实际已成功但 demo 显示超时"的误导。
        final long createTimeout = params.timeout("live_sync_create_timeout_ms", 40_000L);
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms, createTimeout=" + createTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-7, q 退出): ", 1, 7);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出旁路推流菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: listDevices(module, actionTimeout); break;
                    case 2: createFromParams(module, createTimeout); break;
                    case 3: listConverters(module, actionTimeout); break;
                    case 4: switchEnable(module, actionTimeout); break;
                    case 5: updateConverter(module, actionTimeout); break;
                    case 6: deleteConverter(module, actionTimeout); break;
                    case 7: summary(module, actionTimeout); break;
                    default: break;
                }
            } catch (Exception e) {
                System.out.println("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 可用设备列表 (getLiveSyncDevicesV2) ====================

    private void listDevices(LiveSyncModule module, long timeout) {
        final Await<FlightHubError> await = new Await<>(name() + ".getLiveSyncDevicesV2");
        final AtomicReference<LiveSyncDevicesV2Result> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取旁路推流可用设备", timeout, await, () ->
            module.getLiveSyncDevicesV2(new FlightHubCallback.GetLiveSyncDevicesV2Callback() {
                @Override
                public void onResult(FlightHubError error, LiveSyncDevicesV2Result result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) {
            return;
        }
        LiveSyncDevicesV2Result result = ref.get();
        if (result == null) {
            System.out.println("   设备列表为 null");
            return;
        }
        System.out.println("   设备总数=" + result.getTotal());
        List<LiveSyncDeviceItemV2> list = result.getList();
        if (list == null || list.isEmpty()) {
            System.out.println("   无可用设备");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            LiveSyncDeviceItemV2 it = list.get(i);
            System.out.println("   [" + (i + 1) + "] gateway=" + describeDevice(it.getGateway()));
            List<LiveSyncDeviceV2> hosts = it.getHosts();
            if (hosts != null) {
                for (LiveSyncDeviceV2 h : hosts) {
                    System.out.println("        host=" + describeDevice(h));
                }
            }
        }
    }

    private String describeDevice(LiveSyncDeviceV2 d) {
        if (d == null) {
            return "null";
        }
        int camCnt = (d.getCameraCapacity() != null) ? d.getCameraCapacity().size() : 0;
        return "sn=" + d.getSn() + " name=" + d.getName() + " model=" + d.getDeviceModelKey()
            + " type=" + d.getDeviceType() + " cameras=" + camCnt;
    }

    // ==================== [2] 创建转码器 (createLiveSync) ====================

    private void createFromParams(LiveSyncModule module, long timeout) {
        Node cfg = params.node("live_sync");
        LiveSyncCreateParams p = new LiveSyncCreateParams();
        String nameCfg = cfg.str("converter_name", "");
        p.setConverterName(nameCfg.isEmpty() ? defaultName() : nameCfg);
        p.setSn(deviceSnOr(cfg.str("sn", "")));
        String cameraIndex = cfg.str("camera_index", "");
        if (cameraIndex.isEmpty() && input != null) {
            // camera_index 与机型强相关(形如 "99-0-0"), 不写死默认值: 交互模式留空时询问, 非交互则交由服务端校验。
            String v = Utils.readLineEcho(input, "   请输入负载相机索引 camera_index(形如 99-0-0): ");
            cameraIndex = v == null ? "" : v.trim();
        }
        p.setCameraIndex(cameraIndex);
        p.setVideoType(cfg.str("video_type", ""));
        p.setVideoQuality(cfg.intVal("video_quality", 0));
        String schema = cfg.str("schema", "rtmp");
        p.setSchema(schema);
        p.setSchemaOption(buildSchemaOption(cfg, schema, false));

        final Await<FlightHubError> await = new Await<>(name() + ".createLiveSync");
        final AtomicReference<LiveSyncCreateResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建旁路推流转码器(" + schema + " sn=" + p.getSn() + " " + p.getCameraIndex() + ")",
            timeout, await, () ->
            module.createLiveSync(p, new FlightHubCallback.CreateLiveSyncCallback() {
                @Override
                public void onResult(FlightHubError error, LiveSyncCreateResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) {
            return;
        }
        LiveSyncCreateResult result = ref.get();
        if (result == null) {
            System.out.println("   创建结果为 null");
            return;
        }
        LiveSyncItem item = result.getItem();
        System.out.println("   uuid=" + (item != null ? item.getUuid() : "null")
            + "  limit=" + result.getLimit());
        if (item != null) {
            printItem(item);
        }
    }

    // ==================== [3] 转码器列表 (getLiveSyncList) ====================

    private void listConverters(LiveSyncModule module, long timeout) {
        Node cfg = params.node("live_sync");
        LiveSyncListParams p = new LiveSyncListParams();
        p.setPage(cfg.intVal("list_page", 1));
        p.setSize(cfg.intVal("list_size", 20));
        String sn = cfg.str("list_sn", "");
        if (!sn.isEmpty()) {
            p.setSetSn(true);
            p.setSn(sn);
        }
        String schema = cfg.str("list_schema", "");
        if (!schema.isEmpty()) {
            p.setSetSchema(true);
            p.setSchema(schema);
        }

        final Await<FlightHubError> await = new Await<>(name() + ".getLiveSyncList");
        final AtomicReference<LiveSyncListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询旁路推流转码器列表(page=" + p.getPage() + " size=" + p.getSize() + ")",
            timeout, await, () ->
            module.getLiveSyncList(p, new FlightHubCallback.GetLiveSyncListCallback() {
                @Override
                public void onResult(FlightHubError error, LiveSyncListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) {
            return;
        }
        LiveSyncListResult result = ref.get();
        if (result == null) {
            System.out.println("   列表为 null");
            return;
        }
        System.out.println("   page=" + result.getPage() + " size=" + result.getSize()
            + " total=" + result.getTotal() + " 条");
        List<LiveSyncItem> list = result.getList();
        if (list == null || list.isEmpty()) {
            System.out.println("   无转码器");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println("   [" + (i + 1) + "] " + oneLineItem(list.get(i)));
        }
    }

    // ==================== [4] 开关转码器 (updateLiveSync: enable) ====================

    private void switchEnable(LiveSyncModule module, long timeout) {
        String id = readConverterId(module, timeout);
        if (id == null || id.isEmpty()) {
            hint("未输入转码器 id, 取消开关操作");
            return;
        }
        String mode = Utils.readLine(input, "   开启(1) / 关闭(0): ");
        boolean enable = "1".equals(mode == null ? "" : mode.trim());

        LiveSyncUpdateParams p = new LiveSyncUpdateParams();
        p.setId(id);
        p.setSetEnable(true);
        p.setEnable(enable);

        final Await<FlightHubError> await = new Await<>(name() + ".updateLiveSync.enable");
        final AtomicReference<LiveSyncItem> ref = new AtomicReference<>();
        Outcome o = runOutcome("开关转码器(" + id + " -> enable=" + enable + ")", timeout, await, () ->
            module.updateLiveSync(p, new FlightHubCallback.UpdateLiveSyncCallback() {
                @Override
                public void onResult(FlightHubError error, LiveSyncItem result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) {
            return;
        }
        LiveSyncItem item = ref.get();
        if (item != null) {
            printItem(item);
        }
    }

    // ==================== [5] 更新转码器配置 (updateLiveSync: schema/option) ====================

    private void updateConverter(LiveSyncModule module, long timeout) {
        String id = readConverterId(module, timeout);
        if (id == null || id.isEmpty()) {
            hint("未输入转码器 id, 取消更新操作");
            return;
        }
        Node cfg = params.node("live_sync");
        LiveSyncUpdateParams p = new LiveSyncUpdateParams();
        p.setId(id);

        String schema = cfg.str("update_schema", "");
        if (!schema.isEmpty()) {
            p.setSetSchema(true);
            p.setSchema(schema);
        }
        p.setSetSchemaOption(true);
        p.setSchemaOption(buildSchemaOption(cfg, schema.isEmpty() ? "rtmp" : schema, true));

        String sn = cfg.str("update_sn", "");
        if (!sn.isEmpty()) {
            p.setSetSn(true);
            p.setSn(sn);
        }
        String cam = cfg.str("update_camera_index", "");
        if (!cam.isEmpty()) {
            p.setSetCameraIndex(true);
            p.setCameraIndex(cam);
        }
        String name = cfg.str("update_converter_name", "");
        if (!name.isEmpty()) {
            p.setSetConverterName(true);
            p.setConverterName(name);
        }

        final Await<FlightHubError> await = new Await<>(name() + ".updateLiveSync.config");
        final AtomicReference<LiveSyncItem> ref = new AtomicReference<>();
        Outcome o = runOutcome("更新转码器配置(" + id + " schema=" + schema + ")", timeout, await, () ->
            module.updateLiveSync(p, new FlightHubCallback.UpdateLiveSyncCallback() {
                @Override
                public void onResult(FlightHubError error, LiveSyncItem result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) {
            return;
        }
        LiveSyncItem item = ref.get();
        if (item != null) {
            printItem(item);
        }
    }

    // ==================== [6] 删除转码器 (deleteLiveSync) ====================

    private void deleteConverter(LiveSyncModule module, long timeout) {
        String id = readConverterId(module, timeout);
        if (id == null || id.isEmpty()) {
            hint("未输入转码器 id, 取消删除操作");
            return;
        }
        final Await<FlightHubError> await = new Await<>(name() + ".deleteLiveSync");
        Outcome o = runOutcome("删除转码器(" + id + ")", timeout, await, () ->
            module.deleteLiveSync(id, new FlightHubCallback.DeleteLiveSyncCallback() {
                @Override
                public void onResult(FlightHubError error) {
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS) {
            System.out.println("   删除成功");
        }
    }

    // ==================== [7] 汇总统计 (getLiveSyncSummary) ====================

    private void summary(LiveSyncModule module, long timeout) {
        final Await<FlightHubError> await = new Await<>(name() + ".getLiveSyncSummary");
        final AtomicReference<LiveSyncSummaryResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询旁路推流汇总统计", timeout, await, () ->
            module.getLiveSyncSummary(new FlightHubCallback.GetLiveSyncSummaryCallback() {
                @Override
                public void onResult(FlightHubError error, LiveSyncSummaryResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o != Outcome.SUCCESS) {
            return;
        }
        LiveSyncSummaryResult result = ref.get();
        if (result == null) {
            System.out.println("   汇总为 null");
            return;
        }
        System.out.println("   总数=" + result.getTotal() + "  启用数=" + result.getEnableCnt());
    }

    // ==================== 辅助 ====================

    /** 交互输入转码器 id(可回车复用 session 设备相关的最近项, 这里直接读输入)。 */
    private String readConverterId(LiveSyncModule module, long timeout) {
        // 先列出已建转码器, 便于用户复制 uuid
        System.out.println("   (先拉取当前转码器列表, 供参考:)");
        LiveSyncListParams p = new LiveSyncListParams();
        p.setPage(1);
        p.setSize(10);
        final Await<FlightHubError> await = new Await<>(name() + ".readConverterId.list");
        final AtomicReference<LiveSyncListResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("拉取转码器列表参考", timeout, await, () ->
            module.getLiveSyncList(p, new FlightHubCallback.GetLiveSyncListCallback() {
                @Override
                public void onResult(FlightHubError error, LiveSyncListResult result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            List<LiveSyncItem> list = ref.get().getList();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    System.out.println("      [" + (i + 1) + "] " + oneLineItem(list.get(i)));
                }
            }
        }
        return Utils.readLine(input, "   请输入转码器 uuid(如 f176844e-... 或留空取消): ");
    }

    /**
     * 读取 schema_option 敏感字段: 优先取 demo_params.json 配置, 交互模式填空时询问输入。
     * 避免把默认密码/用户名写死在 demo 代码里(本 demo 面向开源)。
     *
     * @param cfg    live_sync 参数节点
     * @param key    配置键
     * @param prompt 交互询问文案(留空时展示)
     * @param hidden 是否为密码等密文输入(true 回显为星号, 与 plain 一致避免敏感信息落控制台)
     */
    private String readParam(Node cfg, String key, String prompt, boolean hidden) {
        String v = cfg.str(key, "");
        if (v.isEmpty() && input != null) {
            System.out.print("   " + prompt + (hidden ? "(输入不显示): " : ": "));
            String line = Utils.readLine(input, "");
            v = line == null ? "" : line.trim();
        }
        return v;
    }

    /** 从配置构建 schema_option：按 schema 类型填对应字段。 */
    private LiveSyncSchemaOption buildSchemaOption(Node cfg, String schema, boolean forUpdate) {
        LiveSyncSchemaOption o = new LiveSyncSchemaOption();
        if ("gb28181".equals(schema)) {
            o.setServerId(cfg.str("gb28181_server_id", ""));
            o.setServerIp(cfg.str("gb28181_server_ip", ""));
            o.setServerPort(cfg.str("gb28181_server_port", "5060"));
            o.setDeviceId(cfg.str("gb28181_device_id", ""));
            o.setLocalChannel(cfg.str("gb28181_local_channel", ""));
            o.setDevicePassword(readParam(cfg, "gb28181_device_password", "请输入 GB28181 设备密码", true));
            // DRTC 链路要求 local_port 非 0(0 会让 MRTC mrtc_agent_init 报参数错误)
            o.setLocalPort(cfg.str("gb28181_local_port", "7060"));
        } else if ("rtsp".equals(schema)) {
            o.setUsername(readParam(cfg, "rtsp_username", "请输入 RTSP 用户名", false));
            o.setPassword(readParam(cfg, "rtsp_password", "请输入 RTSP 密码", true));
            o.setEnableTs(cfg.bool("rtsp_enable_ts", false));
        } else { // rtmp
            String url = cfg.str("rtmp_url", "");
            if (url.isEmpty()) {
                // url 全局唯一, 未配置时基于 config.json 的 server.address 推导默认推流地址,
                // 避免把具体环境 IP 写死在 demo 代码里(本 demo 面向开源)。
                url = defaultRtmpUrl();
            }
            o.setUrl(url);
        }
        return o;
    }

    /** 转码器名称默认值: 便于识别本次创建来自本 demo。 */
    private String defaultName() {
        return "demo-live-sync-" + java.lang.System.currentTimeMillis();
    }

    /**
     * RTMP 默认推流地址: 基于 config.json 的 {@code server.address} 推导, 不写死环境 IP。
     * 形如 {@code rtmp://<host>:1935/live/demo_live_sync_<ts>}; 端口可用 demo_params.json
     * 的 {@code live_sync.rtmp_port} 覆盖(缺省 1935, 标准 RTMP)。
     */
    private String defaultRtmpUrl() {
        String port = params.node("live_sync").str("rtmp_port", "1935");
        return "rtmp://" + serverHost() + ":" + port + "/live/demo_live_sync_"
            + java.lang.System.currentTimeMillis();
    }

    /** 从 config.json 的 server.address 提取主机名(去掉协议与路径, 兼容 https://host、host、host:port 等写法)。 */
    private String serverHost() {
        String address = config != null ? config.address() : "";
        if (address == null || address.isEmpty()) {
            return "localhost";
        }
        String host = address;
        // 去掉协议前缀
        int schemeIdx = host.indexOf("://");
        if (schemeIdx >= 0) {
            host = host.substring(schemeIdx + 3);
        }
        // 去掉路径(如有)
        int slash = host.indexOf('/');
        if (slash >= 0) {
            host = host.substring(0, slash);
        }
        // 去掉端口(如有)
        int colon = host.lastIndexOf(':');
        if (colon >= 0) {
            host = host.substring(0, colon);
        }
        return host;
    }

    /** 目标 SN: 配置优先, 否则回退 session 设备; 都没有时返回空串(由服务端判定)。 */
    private String deviceSnOr(String cfgSn) {
        if (cfgSn != null && !cfgSn.isEmpty()) {
            return cfgSn;
        }
        if (session != null) {
            String drone = session.droneSn();
            if (drone != null && !drone.isEmpty()) {
                return drone;
            }
            return session.dockSn();
        }
        return "";
    }

    private void printItem(LiveSyncItem item) {
        System.out.println("   uuid=" + item.getUuid()
            + "  sn=" + item.getSn() + "  camera=" + item.getCameraIndex()
            + "  schema=" + item.getSchema() + "  status=" + item.getStatus()
            + "  enable=" + item.isEnable());
        LiveSyncSchemaOption so = item.getSchemaOption();
        if (so != null) {
            System.out.println("   schemaOption: url=" + so.getUrl()
                + "  rtspUrl=" + so.getRtspUrl()
                + "  serverIp=" + so.getServerIp() + ":" + so.getServerPort()
                + "  deviceId=" + so.getDeviceId() + "  localPort=" + so.getLocalPort()
                + "  username=" + so.getUsername());
        }
    }

    private String oneLineItem(LiveSyncItem item) {
        LiveSyncSchemaOption so = item.getSchemaOption();
        String opt = "";
        if (so != null) {
            if (!so.getUrl().isEmpty()) {
                opt = so.getUrl();
            } else if (!so.getRtspUrl().isEmpty()) {
                opt = so.getRtspUrl();
            } else {
                opt = so.getServerIp() + ":" + so.getServerPort() + " dev=" + so.getDeviceId();
            }
        }
        return "id=" + item.getUuid() + " " + item.getSn() + "/" + item.getCameraIndex()
            + " " + item.getSchema() + " status=" + item.getStatus()
            + " enable=" + item.isEnable() + " -> " + opt;
    }

    // ==================== 菜单文案 ====================

    private String menu() {
        return "\n"
            + "  === 旁路推流 (Live Sync) ===\n"
            + "  1.  可用设备列表   (getLiveSyncDevicesV2)\n"
            + "  2.  创建转码器     (createLiveSync, 参数来自 demo_params.json 的 live_sync 节)\n"
            + "  3.  转码器列表     (getLiveSyncList)\n"
            + "  4.  开关转码器     (updateLiveSync: enable)\n"
            + "  5.  更新转码器配置 (updateLiveSync: schema/schema_option)\n"
            + "  6.  删除转码器     (deleteLiveSync)\n"
            + "  7.  汇总统计       (getLiveSyncSummary)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}