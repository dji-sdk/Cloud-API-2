// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.ContentsGeometryData;
import com.dji.flighthubsdk.bean.ContentsPropertyData;
import com.dji.flighthubsdk.bean.CreateCustomizedFlightAreasParams;
import com.dji.flighthubsdk.bean.CreateFlightAreasResults;
import com.dji.flighthubsdk.bean.DeviceSignalItem;
import com.dji.flighthubsdk.bean.DeviceSyncResults;
import com.dji.flighthubsdk.bean.DeviceSyncStatusResults;
import com.dji.flighthubsdk.bean.FlightAreaInfoItem;
import com.dji.flighthubsdk.bean.FlightAreasContentData;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetAreasSyncStatusResults;
import com.dji.flighthubsdk.bean.GetFlightAreasResults;
import com.dji.flighthubsdk.bean.GetOfflineMapsModelInfoResults;
import com.dji.flighthubsdk.bean.GetOfflineMapsSyncStatusResults;
import com.dji.flighthubsdk.bean.ModelItemInfoData;
import com.dji.flighthubsdk.bean.OfflineMapsModelData;
import com.dji.flighthubsdk.bean.OfflineMapsSyncResults;
import com.dji.flighthubsdk.bean.SetOfflineMapsParams;
import com.dji.flighthubsdk.bean.SignalMapsResults;
import com.dji.flighthubsdk.bean.SyncFlightAreasResults;
import com.dji.flighthubsdk.bean.UpdateCustomizedFlightAreasParams;
import com.dji.flighthubsdk.bean.UpdateFlightAreasResults;
import com.dji.flighthubsdk.bean.UpdateOfflineMapsParams;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 安全服务 Demo(菜单驱动)：信号地图查询、自定义飞行区域管理、地形障碍物离线地图管理。
 *
 * <p>对应 {@code SafetyServiceModule} 的 13 个安全服务接口(非全量: 模块共 19 个接口,
 * 其余 6 个解禁证书接口由 {@code FlySafeUnlockDemo} 覆盖, 见下方职责区分;
 * getCustomizedFlightAreas 另有一个参数对象重载, 本 demo 用平铺参数重载, 不单独计)：
 *
 * <pre>
 * 主菜单
 * ├── [信号地图]
 * │   └── [1]  获取信号地图            searchSignalMaps
 * ├── [自定义飞行区域]
 * │   ├── [2]  创建                    createCustomizedFlightAreas
 * │   ├── [3]  查询列表                getCustomizedFlightAreas
 * │   ├── [4]  更新                    updateCustomizedFlightAreas(需输入 id)
 * │   ├── [5]  删除                    deleteCustomizedFlightAreas(需输入 id)
 * │   ├── [6]  同步至设备              syncCustomizedFlightAreas
 * │   └── [7]  查同步状态              getFlightAreasSyncStatus
 * └── [地形障碍物-离线地图]
 *     ├── [8]  查询模型信息            getOfflineMapsModelInfo
 *     ├── [9]  创建/更新模型           updateOfflineMapsModelInfo
 *     ├── [10] 取消生成                cancelOfflineMapsModelInfo
 *     ├── [11] 修改开关                setOfflineMaps
 *     ├── [12] 同步至设备              syncOfflineMaps
 *     └── [13] 查同步状态              getOfflineMapsSyncStatus
 * </pre>
 *
 * <p><b>与 FlySafeUnlockDemo 的职责区分：</b>两个 demo 共用同一个
 * {@code SafetyServiceModule}, 但覆盖的接口互不重叠：
 * <ul>
 *   <li>SafetyServiceDemo(本类) → 安全服务 → 信号地图 / 自定义飞行区域 / 离线地图, 共 13 个接口；</li>
 *   <li>FlySafeUnlockDemo → 限飞解禁 → 同模块的 6 个解禁证书接口(getUnlockLicenseList 等)。</li>
 * </ul>
 *
 * <p><b>接口串联设计：</b>[2] 创建成功回显的区域 id 供 [4] 更新 / [5] 删除手动输入(不自动缓存);
 * [6]/[12] 同步下发后用 [7]/[13] 查看各设备同步状态。
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getSafetyServiceModule()}(挂在 Workspace 上)。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>复杂参数(几何、模型条目、SN 列表等)全部从 {@code demo_params.json} 的
 *       {@code safety_service} 配置节读取, 交互仅收敛在 flightAreaId 与开关类输入；</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 结果对象仅在 SUCCESS 时解析回显；</li>
 *   <li>同步类接口([6][7][12][13])的 SN 须为网关类型设备(机场 1、2、3 以及地面站, 见 message.h
 *       DeviceSyncStatusResults 注释); SN 列表为空时 SDK 本地即同步回调 ERROR_INVALID_PARAM
 *       (safety_service_module_impl.cpp), demo 提前拦截并提示补配置；</li>
 *   <li>均为即时 HTTP 动作, 无订阅、无退出收尾。</li>
 * </ul>
 */
public class SafetyServiceDemo extends ModuleDemo {

    @Override
    public String name() {
        return "SafetyServiceDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式安全服务 demo");
            return;
        }

        final SafetyServiceModule module = session.workspace().getSafetyServiceModule();
        if (module == null) {
            DemoLog.error(name(), "getSafetyServiceModule() 返回 null");
            System.out.println("   获取 SafetyServiceModule 失败(getSafetyServiceModule() 返回 null), 返回上级菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-13, q 退出): ", 1, 13);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出安全服务菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1:  searchSignalMaps(module, actionTimeout);            break;
                    case 2:  createCustomizedFlightAreas(module, actionTimeout); break;
                    case 3:  getCustomizedFlightAreas(module, actionTimeout);    break;
                    case 4:  updateCustomizedFlightAreas(module, actionTimeout); break;
                    case 5:  deleteCustomizedFlightAreas(module, actionTimeout); break;
                    case 6:  syncCustomizedFlightAreas(module, actionTimeout);   break;
                    case 7:  getFlightAreasSyncStatus(module, actionTimeout);    break;
                    case 8:  getOfflineMapsModelInfo(module, actionTimeout);     break;
                    case 9:  updateOfflineMapsModelInfo(module, actionTimeout);  break;
                    case 10: cancelOfflineMapsModelInfo(module, actionTimeout);  break;
                    case 11: setOfflineMaps(module, actionTimeout);              break;
                    case 12: syncOfflineMaps(module, actionTimeout);             break;
                    case 13: getOfflineMapsSyncStatus(module, actionTimeout);    break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 获取信号地图 (searchSignalMaps) ====================

    private void searchSignalMaps(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service");
        final List<String> sns = readStringList(cfg, "signal_map_device_sns");
        final List<String> dataTypes = readStringList(cfg, "signal_map_data_types");
        if (sns.isEmpty()) {
            hint("safety_service.signal_map_device_sns 未配置");
            return;
        }
        if (dataTypes.isEmpty()) {
            // SDK 对 dataTypes 只给了示例 ["4g", "sdr"](api/safety_service_module.h 与 Java 模块注释同),
            // 完整取值枚举仓库内未记载; 未配置时回退该示例组合。
            dataTypes.add("4g");
            dataTypes.add("sdr");
        }
        console("   > deviceSns=" + sns + " dataTypes=" + dataTypes);

        final Await<FlightHubError> await = new Await<>(name() + ".searchSignalMaps");
        final AtomicReference<SignalMapsResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取信号地图", timeout, await, () ->
            module.searchSignalMaps(sns, dataTypes, new FlightHubCallback.SearchSignalMapsCallback() {
                @Override
                public void onResult(FlightHubError error, SignalMapsResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            SignalMapsResults r = ref.get();
            if (r != null) {
                List<DeviceSignalItem> list = r.getLists();
                int size = list != null ? list.size() : 0;
                console("   共 " + size + " 台设备的信号地图:");
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        DeviceSignalItem d = list.get(i);
                        int grids = d.getGrids() != null ? d.getGrids().size() : 0;
                        console("   [" + (i + 1) + "] sn=" + d.getDeviceSn() + " grids=" + grids);
                    }
                }
            }
        }
    }

    // ==================== [2] 创建自定义飞行区域 (createCustomizedFlightAreas) ====================

    private void createCustomizedFlightAreas(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service").child("flight_area_create");
        final CreateCustomizedFlightAreasParams p = new CreateCustomizedFlightAreasParams();
        p.setName(cfg.str("name", "demo-flight-area"));
        // type 枚举: "nfz"=限飞区(最多 100 个), "dfence"=作业区, "noland"=禁降区(最多 50 个)
        // (CreateCustomizedFlightAreasParams bean 与 message.h 同注)。
        p.setType(cfg.str("type", "nfz"));
        p.setContent(buildContent(cfg));
        p.setCreatedBy(cfg.str("created_by", "demo_user"));

        console("   > name=" + p.getName() + " type=" + p.getType()
            + " geometry=" + p.getContent().getGeometry().getType() + " createdBy=" + p.getCreatedBy());

        final Await<FlightHubError> await = new Await<>(name() + ".createCustomizedFlightAreas");
        final AtomicReference<CreateFlightAreasResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建飞行区域", timeout, await, () ->
            module.createCustomizedFlightAreas(p, new FlightHubCallback.CreateCustomizedFlightAreasCallback() {
                @Override
                public void onResult(FlightHubError error, CreateFlightAreasResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            CreateFlightAreasResults r = ref.get();
            if (r != null) {
                console("   已创建: id=" + r.getId() + " name=" + r.getName() + " (id 供 [4] 更新/[5] 删除输入)");
            }
        }
    }

    // ==================== [3] 获取自定义飞行区域列表 (getCustomizedFlightAreas) ====================

    private void getCustomizedFlightAreas(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service").child("flight_area_get");
        // type 枚举 "nfz"/"dfence"/"noland", status 枚举 "enable"=已启用, "disable"=已禁用;
        // pageIndex 从 1 开始 (GetCustomizedFlightAreasParams bean 与 message.h 同注)。
        // 过滤参数均可传空串表示不过滤 (http_interface.cpp get_customized_flight_areas 注:「空值表示不使用该项过滤条件」, 实现对空串跳过拼接 query)。
        final String qName = cfg.str("name", "");
        final String qType = cfg.str("type", "");
        final String qStatus = cfg.str("status", "");
        final List<String> ids = readStringList(cfg, "ids");
        final int pageIndex = cfg.intVal("page_index", 1);
        final int pageSize = cfg.intVal("page_size", 20);

        console("   > name=" + qName + " type=" + qType + " status=" + qStatus
            + " ids=" + ids + " pageIndex=" + pageIndex + " pageSize=" + pageSize);

        final Await<FlightHubError> await = new Await<>(name() + ".getCustomizedFlightAreas");
        final AtomicReference<GetFlightAreasResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询飞行区域", timeout, await, () ->
            module.getCustomizedFlightAreas(qName, qType, qStatus, ids, pageIndex, pageSize, new FlightHubCallback.GetCustomizedFlightAreasCallback() {
                @Override
                public void onResult(FlightHubError error, GetFlightAreasResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            GetFlightAreasResults r = ref.get();
            if (r != null) {
                List<FlightAreaInfoItem> list = r.getList();
                int size = list != null ? list.size() : 0;
                int total = r.getPagination() != null ? r.getPagination().getTotal() : 0;
                console("   本页 " + size + " 条(总计 " + total + " 条):");
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        FlightAreaInfoItem it = list.get(i);
                        console("   [" + (i + 1) + "] id=" + it.getId() + " name=" + it.getName()
                            + " type=" + it.getType() + " status=" + it.getStatus());
                    }
                }
            }
        }
    }

    // ==================== [4] 更新自定义飞行区域 (updateCustomizedFlightAreas) ====================

    private void updateCustomizedFlightAreas(SafetyServiceModule module, long timeout) {
        // EOF(null)/q 判定先于判空; id 为空时 SDK 本地也会同步回调 ERROR_INVALID_PARAM
        // (safety_service_module_impl.cpp), 此处提前拦截并留痕。
        String id = Utils.readLineEcho(input, "   请输入 flightAreaId(q 取消): ");
        if (id == null || id.equalsIgnoreCase("q")) {
            hint("已取消更新");
            return;
        }
        if (id.isEmpty()) {
            hint("flightAreaId 不能为空");
            return;
        }

        // 复用 flight_area_create 配置节的属性/几何, 名称加 "-updated" 后缀以便肉眼区分。
        Node cfg = params.node("safety_service").child("flight_area_create");
        final UpdateCustomizedFlightAreasParams p = new UpdateCustomizedFlightAreasParams();
        p.setFlightAreaId(id);
        p.setName(cfg.str("name", "demo-flight-area") + "-updated");
        p.setContent(buildContent(cfg));
        p.setUpdateBy(cfg.str("created_by", "demo_user"));

        console("   > id=" + p.getFlightAreaId() + " name=" + p.getName());

        final Await<FlightHubError> await = new Await<>(name() + ".updateCustomizedFlightAreas");
        final AtomicReference<UpdateFlightAreasResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("更新飞行区域", timeout, await, () ->
            module.updateCustomizedFlightAreas(p, new FlightHubCallback.UpdateCustomizedFlightAreasCallback() {
                @Override
                public void onResult(FlightHubError error, UpdateFlightAreasResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            UpdateFlightAreasResults r = ref.get();
            if (r != null) {
                console("   已更新: id=" + r.getId());
            }
        }
    }

    // ==================== [5] 删除自定义飞行区域 (deleteCustomizedFlightAreas) ====================

    private void deleteCustomizedFlightAreas(SafetyServiceModule module, long timeout) {
        // EOF(null)/q 判定先于判空; id 为空时 SDK 本地也会同步回调 ERROR_INVALID_PARAM
        // (safety_service_module_impl.cpp), 此处提前拦截并留痕。
        final String id = Utils.readLineEcho(input, "   请输入 flightAreaId(q 取消): ");
        if (id == null || id.equalsIgnoreCase("q")) {
            hint("已取消删除");
            return;
        }
        if (id.isEmpty()) {
            hint("flightAreaId 不能为空");
            return;
        }
        console("   > flightAreaId=" + id);
        callOutcome("删除飞行区域", cb -> module.deleteCustomizedFlightAreas(id, cb), timeout);
    }

    // ==================== [6] 同步自定义飞行区域至设备 (syncCustomizedFlightAreas) ====================

    private void syncCustomizedFlightAreas(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service");
        final List<String> sns = readStringList(cfg, "flight_area_sync_sns");
        if (sns.isEmpty()) {
            hint("safety_service.flight_area_sync_sns 未配置(需网关类型设备 SN: 机场 1/2/3 或地面站)");
            return;
        }
        console("   > deviceSns=" + sns);

        final Await<FlightHubError> await = new Await<>(name() + ".syncCustomizedFlightAreas");
        final AtomicReference<SyncFlightAreasResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("同步飞行区域", timeout, await, () ->
            module.syncCustomizedFlightAreas(sns, new FlightHubCallback.SyncCustomizedFlightAreasCallback() {
                @Override
                public void onResult(FlightHubError error, SyncFlightAreasResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            SyncFlightAreasResults r = ref.get();
            if (r != null) {
                printSyncDispatchResults(r.getResults());
            }
        }
    }

    // ==================== [7] 获取自定义飞行区域同步状态 (getFlightAreasSyncStatus) ====================

    private void getFlightAreasSyncStatus(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service");
        final List<String> sns = readStringList(cfg, "flight_area_sync_sns");
        if (sns.isEmpty()) {
            hint("safety_service.flight_area_sync_sns 未配置(需网关类型设备 SN: 机场 1/2/3 或地面站)");
            return;
        }
        console("   > deviceSns=" + sns);

        final Await<FlightHubError> await = new Await<>(name() + ".getFlightAreasSyncStatus");
        final AtomicReference<GetAreasSyncStatusResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询飞行区域同步状态", timeout, await, () ->
            module.getFlightAreasSyncStatus(sns, new FlightHubCallback.GetFlightAreasSyncStatusCallback() {
                @Override
                public void onResult(FlightHubError error, GetAreasSyncStatusResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            GetAreasSyncStatusResults r = ref.get();
            if (r != null) {
                printSyncStatusList(r.getList());
            }
        }
    }

    // ==================== [8] 查询离线地图模型信息 (getOfflineMapsModelInfo) ====================

    private void getOfflineMapsModelInfo(SafetyServiceModule module, long timeout) {
        final Await<FlightHubError> await = new Await<>(name() + ".getOfflineMapsModelInfo");
        final AtomicReference<GetOfflineMapsModelInfoResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询离线地图", timeout, await, () ->
            module.getOfflineMapsModelInfo(new FlightHubCallback.GetOfflineMapsModelInfoCallback() {
                @Override
                public void onResult(FlightHubError error, GetOfflineMapsModelInfoResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            GetOfflineMapsModelInfoResults r = ref.get();
            if (r != null) {
                console("   disable=" + r.isDisable());
                OfflineMapsModelData map = r.getOfflineMap();
                if (map == null) {
                    console("   offlineMap: null");
                    return;
                }
                // percent 为同步进度百分比; status 枚举两源(bean 与 message.h)均只记载 "finish"=完成,
                // 其余取值仓库内未见出处; result 为结果码(取值枚举未记载)。
                console("   id=" + map.getId() + " status=" + map.getStatus()
                    + " percent=" + map.getPercent() + "% result=" + map.getResult()
                    + " updatedTime=" + map.getUpdatedTime());
                int cnt = map.getModels() != null ? map.getModels().size() : 0;
                console("   models=" + cnt);
            }
        }
    }

    // ==================== [9] 创建/更新离线地图模型 (updateOfflineMapsModelInfo) ====================

    private void updateOfflineMapsModelInfo(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service");
        final List<ModelItemInfoData> m3d = readModelItems(cfg, "offline_map_model3d_items");
        final List<ModelItemInfoData> m2d = readModelItems(cfg, "offline_map_model2d_items");
        if (m3d.isEmpty() && m2d.isEmpty()) {
            hint("safety_service.offline_map_model3d_items 与 offline_map_model2d_items 均未配置");
            return;
        }
        final UpdateOfflineMapsParams p = new UpdateOfflineMapsParams();
        p.setModel3dItems(m3d);
        p.setModel2dItems(m2d);
        console("   > model3d=" + m3d.size() + " model2d=" + m2d.size());
        callOutcome("更新离线地图", cb -> module.updateOfflineMapsModelInfo(p, cb), timeout);
    }

    // ==================== [10] 取消离线地图生成 (cancelOfflineMapsModelInfo) ====================

    private void cancelOfflineMapsModelInfo(SafetyServiceModule module, long timeout) {
        callOutcome("取消离线地图生成", cb -> module.cancelOfflineMapsModelInfo(cb), timeout);
    }

    // ==================== [11] 修改离线地图开关 (setOfflineMaps) ====================

    private void setOfflineMaps(SafetyServiceModule module, long timeout) {
        int v = Utils.readIntInRange(input, "   请输入离线地图开关(0=禁用 1=启用, q 取消): ", 0, 1);
        if (v == Utils.QUIT) {
            hint("已取消修改离线地图开关");
            return;
        }
        final boolean enable = v != 0;
        final SetOfflineMapsParams p = new SetOfflineMapsParams();
        p.setOfflineMapEnable(enable);
        console("   > offlineMapEnable=" + enable);
        callOutcome("修改离线地图开关", cb -> module.setOfflineMaps(p, cb), timeout);
    }

    // ==================== [12] 同步离线地图至设备 (syncOfflineMaps) ====================

    private void syncOfflineMaps(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service");
        final List<String> sns = readStringList(cfg, "offline_map_sync_sns");
        if (sns.isEmpty()) {
            hint("safety_service.offline_map_sync_sns 未配置(需网关类型设备 SN: 机场 1/2/3 或地面站)");
            return;
        }
        console("   > deviceSns=" + sns);

        final Await<FlightHubError> await = new Await<>(name() + ".syncOfflineMaps");
        final AtomicReference<OfflineMapsSyncResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("同步离线地图", timeout, await, () ->
            module.syncOfflineMaps(sns, new FlightHubCallback.SyncOfflineMapsCallback() {
                @Override
                public void onResult(FlightHubError error, OfflineMapsSyncResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            OfflineMapsSyncResults r = ref.get();
            if (r != null) {
                printSyncDispatchResults(r.getResults());
            }
        }
    }

    // ==================== [13] 获取设备离线地图同步状态 (getOfflineMapsSyncStatus) ====================

    private void getOfflineMapsSyncStatus(SafetyServiceModule module, long timeout) {
        Node cfg = params.node("safety_service");
        final List<String> sns = readStringList(cfg, "offline_map_sync_sns");
        if (sns.isEmpty()) {
            hint("safety_service.offline_map_sync_sns 未配置");
            return;
        }
        console("   > deviceSns=" + sns);

        final Await<FlightHubError> await = new Await<>(name() + ".getOfflineMapsSyncStatus");
        final AtomicReference<GetOfflineMapsSyncStatusResults> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询离线地图同步状态", timeout, await, () ->
            module.getOfflineMapsSyncStatus(sns, new FlightHubCallback.GetOfflineMapsSyncStatusCallback() {
                @Override
                public void onResult(FlightHubError error, GetOfflineMapsSyncStatusResults result) {
                    ref.set(result);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            GetOfflineMapsSyncStatusResults r = ref.get();
            if (r != null) {
                printSyncStatusList(r.getList());
            }
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 从 {@code flight_area_create} 配置节构建飞行区内容(content.type 固定 "Feature",
     * 见 message.h FlightAreasContentData 注释)。[2] 创建与 [4] 更新共用同一份配置。
     */
    private FlightAreasContentData buildContent(Node cfg) {
        // width 为缓冲宽度(米), 仅折线(Polyline)类型飞行区域有效, 文档取值范围 [10, 100] 默认 10
        // (ContentsPropertyData bean 与 message.h 同注); 默认几何为 Circle 时该值不生效。
        ContentsPropertyData prop = new ContentsPropertyData(
            cfg.str("color", "#FF0000"), cfg.intVal("width", 10), cfg.bool("clamp_to_ground", false));
        ContentsGeometryData geom = buildGeometry(cfg, cfg.str("geometry_type", "Circle"));
        return new FlightAreasContentData("Feature", prop, geom);
    }

    /**
     * 从配置读几何数据: Circle 用 center + radius, 其余类型(Polygon/Polyline)用 coordinates 三层嵌套。
     *
     * <p>几何类型枚举(ContentsGeometryData bean 与 message.h 同注): "Circle"=圆形(限飞区+作业区),
     * "Polygon"=多边形(限飞区+作业区), "Polyline"=折线(仅禁降区)。坐标格式 [经度, 纬度]
     * (GeoJSON 标准, 经度在前); 多边形约束: 至少 3 个顶点、每个环顶点数 &lt; 255、首尾坐标必须
     * 相同(闭合); 圆形 radius 单位米、需 &gt; 0。center 缺失或不足 2 个坐标分量时回退 (0.0, 0.0) 占位。
     */
    private ContentsGeometryData buildGeometry(Node cfg, String geomType) {
        if ("Circle".equalsIgnoreCase(geomType)) {
            List<Double> center = new ArrayList<>();
            for (Object o : cfg.list("center")) {
                if (o instanceof Number) {
                    center.add(((Number) o).doubleValue());
                }
            }
            if (center.size() < 2) {
                center.clear();
                // 占位坐标；真实飞行须在配置中填写有效 center。
                center.add(0.0);
                center.add(0.0);
            }
            return new ContentsGeometryData("Circle", new ArrayList<>(), center, cfg.dbl("radius", 500.0));
        }
        // 非圆形(Polygon/Polyline): coordinates 三层嵌套 [[[lng,lat], [lng,lat], ...]]
        List<List<List<Double>>> coords = new ArrayList<>();
        for (Object ring : cfg.list("coordinates")) {
            if (!(ring instanceof List)) {
                continue;
            }
            List<List<Double>> ringOut = new ArrayList<>();
            for (Object pt : (List<?>) ring) {
                if (!(pt instanceof List)) {
                    continue;
                }
                List<Double> ptOut = new ArrayList<>();
                for (Object v : (List<?>) pt) {
                    if (v instanceof Number) {
                        ptOut.add(((Number) v).doubleValue());
                    }
                }
                ringOut.add(ptOut);
            }
            coords.add(ringOut);
        }
        return new ContentsGeometryData(geomType, coords, new ArrayList<>(), 0.0);
    }

    /**
     * 从配置读模型条目数组: [{uuid, object_key, file_type}, ...]。
     * file_type 枚举: "tif"、"b3dm"、"pnts"(ModelItemInfoData bean 与 message.h 同注)。
     */
    @SuppressWarnings("unchecked")
    private List<ModelItemInfoData> readModelItems(Node cfg, String key) {
        List<ModelItemInfoData> out = new ArrayList<>();
        for (Object o : cfg.list(key)) {
            if (!(o instanceof Map)) {
                continue;
            }
            Map<String, Object> m = (Map<String, Object>) o;
            ModelItemInfoData it = new ModelItemInfoData();
            it.setUuid(strOrEmpty(m.get("uuid")));
            it.setObjectKey(strOrEmpty(m.get("object_key")));
            it.setFileType(strOrEmpty(m.get("file_type")));
            out.add(it);
        }
        return out;
    }

    /** 从配置读字符串列表(非字符串元素按 toString 转换, null 元素跳过)。 */
    private List<String> readStringList(Node cfg, String key) {
        List<String> out = new ArrayList<>();
        for (Object o : cfg.list(key)) {
            if (o != null) {
                out.add(o.toString());
            }
        }
        return out;
    }

    /** null 安全的 toString(null 转空串), 供配置 Map 取值使用。 */
    private String strOrEmpty(Object v) {
        return v == null ? "" : v.toString();
    }

    /**
     * 回显同步下发结果列表([6]/[12] 共用): 逐设备 sn + code(带 {@link #describeSyncCode} 中文释义)。
     */
    private void printSyncDispatchResults(List<DeviceSyncResults> results) {
        int size = results != null ? results.size() : 0;
        console("   同步请求已下发, 共 " + size + " 台设备:");
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                DeviceSyncResults d = results.get(i);
                console("   [" + (i + 1) + "] sn=" + d.getDeviceSn() + " code=" + d.getCode()
                    + "(" + describeSyncCode(d.getCode()) + ")");
            }
        }
    }

    /**
     * 回显设备同步状态列表([7]/[13] 共用): 逐设备 sn + status + reason(带
     * {@link #describeSyncReason} 中文释义)。status 为字符串枚举: "waiting"=等待同步,
     * "synchronizing"=同步中, "synchronized"=已同步, "fail"=同步失败
     * (DeviceSyncStatusResults bean 与 message.h 同注)。
     */
    private void printSyncStatusList(List<DeviceSyncStatusResults> list) {
        int size = list != null ? list.size() : 0;
        console("   共 " + size + " 台设备:");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                DeviceSyncStatusResults d = list.get(i);
                console("   [" + (i + 1) + "] sn=" + d.getSn() + " status=" + d.getStatus()
                    + " reason=" + d.getReason() + "(" + describeSyncReason(d.getReason()) + ")");
            }
        }
    }

    /**
     * 同步下发结果码 → 中文释义。出处: DeviceSyncResults bean 与 message.h(两源一致):
     * 0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时。
     */
    private static String describeSyncCode(int code) {
        switch (code) {
            case 0:      return "成功";
            case 230012: return "不支持自定义飞行区";
            case 230013: return "设备离线";
            case 230014: return "设备繁忙";
            case 230015: return "超时";
            default:     return "未知结果码";
        }
    }

    /**
     * 设备同步失败原因码 → 中文释义。出处: DeviceSyncStatusResults bean 与 message.h(两源一致):
     * 0=无错误(成功), 1=解析云端返回的文件信息失败, 2=获取飞行器端文件信息失败, 3=从云端下载文件失败,
     * 4=链路翻转失败, 5=传输文件失败, 6=disable 失败, 7=自定义飞行区删除失败,
     * 8=飞行器端加载作业区域数据失败, 9=enable 失败, 10=机场增强图传无法关闭, 作业区域数据同步失败,
     * 11=飞行器开机失败, 无法同步作业区域数据, 12=checksum 校验失败, 13=同步异常超时,
     * 255=未知错误(机场侧快速起飞, 等不及司空返回结果, 主动上报 255)。
     */
    private static String describeSyncReason(int reason) {
        switch (reason) {
            case 0:   return "无错误(成功)";
            case 1:   return "解析云端返回的文件信息失败";
            case 2:   return "获取飞行器端文件信息失败";
            case 3:   return "从云端下载文件失败";
            case 4:   return "链路翻转失败";
            case 5:   return "传输文件失败";
            case 6:   return "disable 失败";
            case 7:   return "自定义飞行区删除失败";
            case 8:   return "飞行器端加载作业区域数据失败";
            case 9:   return "enable 失败";
            case 10:  return "机场增强图传无法关闭, 作业区域数据同步失败";
            case 11:  return "飞行器开机失败, 无法同步作业区域数据";
            case 12:  return "checksum 校验失败";
            case 13:  return "同步异常超时";
            case 255: return "未知错误(机场侧快速起飞, 等不及司空返回结果, 主动上报 255)";
            default:  return "未知原因码";
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
     * 主菜单文案：13 个菜单项与 {@code SafetyServiceModule} 同名接口一一对应(见类 Javadoc 菜单树)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 安全服务 (Safety Service) ===\n"
            + "  [信号地图]\n"
            + "   1.  获取信号地图              (searchSignalMaps)\n"
            + "  [自定义飞行区域]\n"
            + "   2.  创建自定义飞行区域        (createCustomizedFlightAreas)\n"
            + "   3.  获取自定义飞行区域列表    (getCustomizedFlightAreas)\n"
            + "   4.  更新自定义飞行区域        (updateCustomizedFlightAreas, 需输入 id)\n"
            + "   5.  删除自定义飞行区域        (deleteCustomizedFlightAreas, 需输入 id)\n"
            + "   6.  同步至设备                (syncCustomizedFlightAreas)\n"
            + "   7.  获取同步状态              (getFlightAreasSyncStatus)\n"
            + "  [地形障碍物-离线地图]\n"
            + "   8.  查询模型信息              (getOfflineMapsModelInfo)\n"
            + "   9.  创建/更新离线地图模型     (updateOfflineMapsModelInfo)\n"
            + "  10.  取消离线地图生成          (cancelOfflineMapsModelInfo)\n"
            + "  11.  修改离线地图开关          (setOfflineMaps)\n"
            + "  12.  同步至设备                (syncOfflineMaps)\n"
            + "  13.  获取设备同步状态          (getOfflineMapsSyncStatus)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
