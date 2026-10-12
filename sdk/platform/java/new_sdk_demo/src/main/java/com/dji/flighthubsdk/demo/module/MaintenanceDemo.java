// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CreateMaintenanceOrderParams;
import com.dji.flighthubsdk.bean.CreateMaintenanceOrderResults;
import com.dji.flighthubsdk.bean.DeviceRepairConfig;
import com.dji.flighthubsdk.bean.DeviceRepairConfigResults;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GeneratedOrderInfo;
import com.dji.flighthubsdk.bean.GetDeviceRepairConfigParams;
import com.dji.flighthubsdk.bean.GetMaintenanceOrdersListParams;
import com.dji.flighthubsdk.bean.MaintenanceOrderContactAddressParams;
import com.dji.flighthubsdk.bean.MaintenanceOrderDetailResults;
import com.dji.flighthubsdk.bean.MaintenanceOrderDeviceItemParams;
import com.dji.flighthubsdk.bean.MaintenanceOrdersListItem;
import com.dji.flighthubsdk.bean.MaintenanceOrdersListResults;
import com.dji.flighthubsdk.core.MaintenanceManagementModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 保修管理模块 Demo(菜单驱动)：报修配置查询 → 创建报修订单 → 查询订单详情 → 订单列表。
 *
 * <p>本类提供以下菜单功能：
 * <ul>
 *   <li>[1] 查询报修配置；</li>
 *   <li>[2] 创建报修订单；</li>
 *   <li>[3] 查询订单详情；</li>
 *   <li>[4] 分页查询订单列表。</li>
 * </ul>
 *
 * <p><b>接口串联设计(业务流程)：</b>
 * <pre>
 *   [1] 查报修配置        ← 确认 supportRepair=true + 允许的 sourceType
 *          ↓(supportRepair=true 才创建)
 *   [2] 创建报修订单      ← 参数取自配置节, 成功后保存 orderNumber
 *          ↓(自动沿用 orderNumber)
 *   [3] 查询订单详情      ← 确认订单字段和状态
 *          ↓
 *   [4] 分页查询订单列表  ← 验证新订单出现在列表中
 * </pre>
 * 菜单不强制串联顺序：每项均独立可运行。[3] 在当次会话有已创建的 orderNumber 时优先沿用,
 * 否则取配置节中的 {@code query_order_number}, 支持查询历史订单。
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getMaintenanceManagementModule()}(挂在 Workspace 上)。
 *
 * <p><b>操作建议：</b>[1] 查配置 → [2] 创建 → [3] 查详情 → [4] 查列表。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>参数配置驱动</b>：所有参数取自 {@code maintenance}
 *       配置节, 无需手动输入, 也不会因等待输入超时导致请求失败；</li>
 *   <li><b>报修配置前置校验</b>：[2] 创建订单前, 若本次会话已执行 [1] 且目标设备不支持在线报修
 *       ({@code supportRepair=false}), 则提前拦截并给出提示, 避免创建必然失败的订单；</li>
 *   <li><b>orderNumber 会话内沿用</b>：[2] 成功后将 {@code orderNumber} 保存在 {@link #lastCreatedOrderNumber},
 *       [3] 优先沿用, 实现「创建 → 查详情」业务串联, 无需用户手动记录；</li>
 *   <li><b>三态结果回显</b>：每步成功/超时/失败均实时回显控制台(见基类 {@link #runOutcome}),
 *       超时仍属 TIMEOUT 而非 FAILED, 调用方可据此区分「命令可能已生效」与「命令确认失败」；</li>
 *   <li><b>结果关键字段格式化输出</b>：成功时打印订单编号、状态、设备 SN 等关键字段到控制台,
 *       方便操作者快速确认, 而不仅写日志文件；</li>
 *   <li>本模块无 WebSocket 订阅, 无需 teardown 退订逻辑, 是最简洁的菜单式模块范式。</li>
 * </ul>
 */
public class MaintenanceDemo extends ModuleDemo {

    /**
     * 本次会话中由 [2] 创建成功的最新订单编号, 供 [3] 查询详情时自动沿用。
     * null 表示本次会话尚未成功创建过订单。
     */
    private String lastCreatedOrderNumber = null;

    /**
     * 本次会话 [1] 查询配置时, 目标设备 SN 是否支持在线报修。
     * null 表示尚未查询过(不加限制)；false 表示已确认不支持([2] 时拦截)。
     * 与 {@link #lastRepairConfigSn} 成对使用: [2] 仅当缓存对应的 SN 与本次目标 SN 一致时才用于拦截,
     * 避免不同设备的能力被错配(如 [1] 查的是 device_sns[0], [2] 目标是 session 设备)。
     */
    private Boolean lastRepairConfigSupportRepair = null;
    /** {@link #lastRepairConfigSupportRepair} 缓存对应的设备 SN。 */
    private String lastRepairConfigSn = null;

    @Override
    public String name() {
        return "MaintenanceDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式保修管理 demo");
            return;
        }

        // workspace 是本模块的唯一依赖, 不需要 drone/dock
        final MaintenanceManagementModule module = session.workspace().getMaintenanceManagementModule();
        if (module == null) {
            DemoLog.error(name(), "getMaintenanceManagementModule() 返回 null, 无法运行保修管理 demo");
            console("   获取 MaintenanceManagementModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            if (lastCreatedOrderNumber != null) {
                console("(本次会话已创建订单: " + lastCreatedOrderNumber
                    + ", [3] 查详情将自动沿用此编号)");
            }
            System.out.print(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-4, q 退出): ", 1, 4);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出保修管理菜单");
                break;
            }

            // 单个菜单动作失败不退出菜单：记录后回到菜单继续
            try {
                switch (choice) {
                    case 1: getRepairConfigs(module, actionTimeout);          break;
                    case 2: createMaintenanceOrder(module, actionTimeout);    break;
                    case 3: getMaintenanceOrderDetail(module, actionTimeout); break;
                    case 4: getMaintenanceOrdersList(module, actionTimeout);  break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    // ==================== [1] 查询报修配置 ====================

    /**
     * 查询报修配置。
     *
     * <p>设备 SN 列表来源(优先级从高到低)：
     * <ol>
     *   <li>{@code maintenance.device_sns} 配置项(非空时使用, 支持多设备批量查询)；</li>
     *   <li>当前 session 已选设备的 SN(机场 SN 和/或飞机 SN)——配置为空时自动使用,
     *       覆盖「选了设备但没填配置」的常见场景；</li>
     *   <li>两者均为空时给出明确提示并中止——SDK C++ 层会本地校验空列表并直接回调
     *       ERROR_INVALID_PARAM, 请求不会发往服务端。</li>
     * </ol>
     *
     * <p>查询结果中的 {@code supportRepair} 决定设备是否可提交在线报修；若目标设备
     * {@code supportRepair=false}, [2] 创建订单时会提前拦截。
     */
    private void getRepairConfigs(MaintenanceManagementModule module, long actionTimeout) {
        Node cfg = params.node("maintenance");
        // Node.list() 返回 List<Object>, 手动转为 List<String>
        List<Object> rawSns = cfg.list("device_sns");
        List<String> deviceSns = new ArrayList<>(rawSns.size());
        for (Object o : rawSns) {
            if (o != null && !o.toString().isEmpty()) {
                deviceSns.add(o.toString());
            }
        }

        // 配置为空时, 自动使用 session 已选设备的 SN(机场 + 飞机均补入)
        if (deviceSns.isEmpty()) {
            if (session.hasDock() && session.dockSn() != null && !session.dockSn().isEmpty()) {
                deviceSns.add(session.dockSn());
            }
            if (session.hasDrone() && session.droneSn() != null && !session.droneSn().isEmpty()) {
                deviceSns.add(session.droneSn());
            }
            if (!deviceSns.isEmpty()) {
                hint("maintenance.device_sns 未配置, 自动使用当前已选设备 SN: " + deviceSns);
            }
        }

        // 两者均为空：SDK 会本地校验并直接回调 ERROR_INVALID_PARAM(请求不会发出), 这里提前拦截给出明确提示
        if (deviceSns.isEmpty()) {
            hint("无法确定查询目标：maintenance.device_sns 未配置, 且当前 session 无已选设备 SN。"
                + "请在 demo_params.json 的 maintenance.device_sns 中填写目标设备 SN。");
            return;
        }

        GetDeviceRepairConfigParams reqParams = new GetDeviceRepairConfigParams();
        reqParams.setDeviceSns(deviceSns);

        console("\n   查询报修配置: deviceSns=" + deviceSns);
        DemoLog.info(name(), "getRepairConfigs: deviceSns=" + deviceSns);

        // 双参回调(error, result)：Await<FlightHubError> 走 runOutcome 三态骨架；
        // 业务结果用 AtomicReference 在回调中保存, 成功后在 Outcome.SUCCESS 分支读取。
        final AtomicReference<DeviceRepairConfigResults> resultRef = new AtomicReference<>();
        final Await<FlightHubError> await = new Await<>(name() + ".getRepairConfigs");
        Outcome outcome = runOutcome("查询报修配置", actionTimeout, await, () ->
            module.getRepairConfigs(reqParams, new FlightHubCallback.GetRepairConfigsCallback() {
                @Override
                public void onResult(FlightHubError error, DeviceRepairConfigResults result) {
                    resultRef.set(result);
                    await.set(error);
                }
            })
        );

        if (outcome == Outcome.SUCCESS) {
            DeviceRepairConfigResults result = resultRef.get();
            List<DeviceRepairConfig> configs =
                    result != null && result.getData() != null ? result.getData() : Collections.emptyList();
            console("   报修配置列表(共 " + configs.size() + " 条, sourceType: 0=定损 2=日志反馈 3=直接报修):");
            // target_device_sn 优先取配置项, 否则取本次实际查询的主设备 SN(第一个)
            String targetSn = cfg.str("target_device_sn", "");
            if (targetSn.isEmpty() && !deviceSns.isEmpty()) {
                targetSn = deviceSns.get(0);
            }
            for (DeviceRepairConfig c : configs) {
                console("     SN=" + c.getDeviceSn()
                    + " supportRepair=" + c.isSupportRepair()
                    + " allowedSourceTypes=" + c.getAllowedSourceTypes()
                    + (c.getRepairGuidance() != null
                                ? " guidance=" + c.getRepairGuidance() : ""));
                DemoLog.info(name(), "repairConfig: " + c);
                // 若配置的目标设备 SN 与当前结果匹配, 记录 supportRepair(连同对应 SN)供 [2] 前置校验
                if (!targetSn.isEmpty() && targetSn.equals(c.getDeviceSn())) {
                    lastRepairConfigSupportRepair = c.isSupportRepair();
                    lastRepairConfigSn = targetSn;
                    if (!c.isSupportRepair()) {
                        hint("目标设备 " + targetSn + " 不支持在线报修(supportRepair=false), "
                            + "[2] 创建订单将被拦截");
                    } else {
                        hint("目标设备 " + targetSn + " 支持在线报修, allowedSourceTypes="
                            + c.getAllowedSourceTypes());
                    }
                }
            }
            if (configs.isEmpty()) {
                hint("未返回配置数据, 请检查设备 SN 是否正确, 或设备是否在当前工作空间下");
            }
        }
    }

    // ==================== [2] 创建报修订单 ====================

    /**
     * 创建报修订单。
     *
     * <p>参数全部取自 {@code maintenance} 配置节, 无需手动输入, 避免了「手动输参 → 等待超时
     * → 订单创建失败」的问题。
     *
     * <p><b>前置校验</b>：若本次会话已执行 [1] 且目标设备不支持在线报修, 提前拦截并给出提示,
     * 避免服务端返回「设备不支持在线报修」错误码后才告知用户。
     *
     * <p><b>结果沿用</b>：创建成功后, 返回的 {@code orderNumber} 保存到 {@link #lastCreatedOrderNumber},
     * [3] 查询详情时自动使用, 无需用户手动记录。
     *
     * <p><b>参数说明</b>(取自 {@code maintenance} 配置节)：
     * <ul>
     *   <li>{@code target_device_sn}：报修设备 SN, 未配置时自动回退当前已选设备 SN(机场优先, 其次飞机)；</li>
     *   <li>{@code target_device_model_key}：必填, 设备 modelKey(demo 未做自动回退, 需手动配置;
     *       会话所选设备的 modelKey 可由 {@code session.topo()} 取得)；</li>
     *   <li>{@code source_type}：报修来源类型(0=定损 2=日志反馈 3=直接报修), 默认 3；</li>
     *   <li>{@code fault_description}：故障描述文字；</li>
     *   <li>{@code fault_occurrence_time}：故障发生时间——SDK 内创建请求字段本身未注明格式
     *       (HTTP 层创建示例为 "2026-04-01 10:00:00", 查详情响应字段则注为 RFC3339),
     *       以服务端实际校验为准, 留空允许；</li>
     *   <li>{@code contact_person/phone/email}：联系人信息；</li>
     *   <li>{@code contact_country/province/city/district/address_detail}：联系地址。</li>
     * </ul>
     */
    private void createMaintenanceOrder(MaintenanceManagementModule module, long actionTimeout) {
        Node cfg = params.node("maintenance");

        String deviceSn = cfg.str("target_device_sn", "");
        // target_device_sn 未配置时, 自动使用 session 已选设备的 SN(机场优先, 其次飞机)
        if (deviceSn.isEmpty()) {
            if (session.hasDock() && session.dockSn() != null && !session.dockSn().isEmpty()) {
                deviceSn = session.dockSn();
                hint("maintenance.target_device_sn 未配置, 自动使用当前已选机场 SN: " + deviceSn);
            } else if (session.hasDrone() && session.droneSn() != null
                    && !session.droneSn().isEmpty()) {
                deviceSn = session.droneSn();
                hint("maintenance.target_device_sn 未配置, 自动使用当前已选飞机 SN: " + deviceSn);
            }
        }

        // 前置校验：已知目标设备不支持在线报修时提前拦截。缓存必须与本次目标 SN 一致才生效——
        // [1] 缓存主体可能是 device_sns[0](与 session 设备不同), 不比对会把 A 设备的能力错用于 B 设备。
        if (Boolean.FALSE.equals(lastRepairConfigSupportRepair)
                && deviceSn.equals(lastRepairConfigSn)) {
            hint("目标设备 " + deviceSn + " 不支持在线报修(本次会话 [1] 查询结果 supportRepair=false), "
                + "创建订单将被服务端拒绝。请确认设备 SN 正确, 或先执行 [1] 重新查询。");
            return;
        }

        String deviceModelKey = cfg.str("target_device_model_key", "");
        if (deviceSn.isEmpty() || deviceModelKey.isEmpty()) {
            // deviceModelKey 本 demo 未做自动回退——可经 Devices.dock/drone(session.topo()).getModelKey()
            // 取到, 此处保持显式配置以避免 SN 与 modelKey 错配, 缺失时提示手动填写
            hint("创建报修订单缺少必填参数："
                + (deviceSn.isEmpty() ? " target_device_sn" : "")
                + (deviceModelKey.isEmpty() ? " target_device_model_key" : "")
                + "。\n   请在 demo_params.json 的 maintenance 节填写："
                + "\n     \"target_device_sn\": \"<设备序列号>\""
                + "\n     \"target_device_model_key\": \"<设备型号Key, 如 3-2-0>\"");
            return;
        }

        int sourceType = cfg.intVal("source_type", 3); // 0=定损 2=日志反馈 3=直接报修(默认)

        // 构建设备信息参数
        MaintenanceOrderDeviceItemParams deviceItem = new MaintenanceOrderDeviceItemParams();
        deviceItem.setDeviceSn(deviceSn);
        deviceItem.setDeviceModelKey(deviceModelKey);
        deviceItem.setSourceType(sourceType);
        deviceItem.setModule(cfg.str("device_module", ""));
        deviceItem.setNameplateSn(cfg.str("nameplate_sn", ""));

        // 构建联系地址(各字段允许为空)
        MaintenanceOrderContactAddressParams address = new MaintenanceOrderContactAddressParams();
        address.setCountry(cfg.str("contact_country", ""));
        address.setProvince(cfg.str("contact_province", ""));
        address.setCity(cfg.str("contact_city", ""));
        address.setDistrict(cfg.str("contact_district", ""));
        address.setDetail(cfg.str("contact_address_detail", ""));

        // 组装完整参数
        CreateMaintenanceOrderParams reqParams = new CreateMaintenanceOrderParams();
        reqParams.setDeviceList(Collections.singletonList(deviceItem));
        reqParams.setFaultDescription(cfg.str("fault_description", "SDK Demo 测试报修"));
        reqParams.setFaultOccurrenceTime(cfg.str("fault_occurrence_time", ""));
        reqParams.setContactPerson(cfg.str("contact_person", ""));
        reqParams.setContactPhone(cfg.str("contact_phone", ""));
        reqParams.setContactEmail(cfg.str("contact_email", ""));
        reqParams.setContactAddress(address);

        console("\n   创建报修订单参数:");
        console("     deviceSn=" + deviceSn + " modelKey=" + deviceModelKey
            + " sourceType=" + sourceType + "(" + describeSourceType(sourceType) + ")");
        console("     故障描述: " + reqParams.getFaultDescription());
        console("     联系人: " + reqParams.getContactPerson()
            + " 电话: " + reqParams.getContactPhone()
            + " 邮箱: " + reqParams.getContactEmail());
        DemoLog.info(name(), "createMaintenanceOrder: " + reqParams);

        final AtomicReference<CreateMaintenanceOrderResults> resultRef = new AtomicReference<>();
        final Await<FlightHubError> await = new Await<>(name() + ".createMaintenanceOrder");
        Outcome outcome = runOutcome("创建报修订单", actionTimeout, await, () ->
            module.createMaintenanceOrder(reqParams, new FlightHubCallback.CreateMaintenanceOrderCallback() {
                @Override
                public void onResult(FlightHubError error, CreateMaintenanceOrderResults result) {
                    resultRef.set(result);
                    await.set(error);
                }
            })
        );

        if (outcome == Outcome.SUCCESS) {
            CreateMaintenanceOrderResults result = resultRef.get();
            if (result != null) {
                console("   订单创建结果: success=" + result.isSuccess());
                List<GeneratedOrderInfo> orders = result.getGeneratedOrders();
                if (orders != null && !orders.isEmpty()) {
                    for (GeneratedOrderInfo order : orders) {
                        console("     订单编号=" + order.getOrderNumber()
                            + " 状态=" + order.getStatus()
                            + " 瑞云订单号=" + order.getRuiyunOrderNumber());
                        DemoLog.info(name(), "generatedOrder: " + order);
                    }
                    // 保存第一个订单编号, [3] 查询详情自动沿用
                    lastCreatedOrderNumber = orders.get(0).getOrderNumber();
                    hint("订单编号已保存: " + lastCreatedOrderNumber
                        + ", [3] 查询详情和 [4] 查列表可验证此订单");
                } else {
                    hint("创建成功但未返回订单列表, 请通过 [4] 查询订单列表确认");
                }
            }
        }
    }

    // ==================== [3] 查询订单详情 ====================

    /**
     * 查询单个报修订单详情。
     *
     * <p><b>orderNumber 获取策略</b>(优先级从高到低)：
     * <ol>
     *   <li>本次会话由 [2] 创建成功保存的 {@link #lastCreatedOrderNumber}(自动串联)；</li>
     *   <li>配置节 {@code maintenance.query_order_number}(查询历史订单, 不依赖当前会话)。</li>
     * </ol>
     * 两者均为空时给出配置提示并中止, 避免将空字符串发给服务端。
     */
    private void getMaintenanceOrderDetail(MaintenanceManagementModule module, long actionTimeout) {
        // orderNumber 优先沿用本次会话创建的
        String orderNumber = lastCreatedOrderNumber;
        if (orderNumber == null || orderNumber.isEmpty()) {
            orderNumber = params.node("maintenance").str("query_order_number", "");
        }
        if (orderNumber.isEmpty()) {
            hint("未找到可查询的订单编号：本次会话未创建订单, 且 maintenance.query_order_number 未配置。"
                + "请先执行 [2] 创建订单, 或在配置中填写 query_order_number。");
            return;
        }

        console("\n   查询订单详情: orderNumber=" + orderNumber);
        DemoLog.info(name(), "getMaintenanceOrderDetail: orderNumber=" + orderNumber);

        final String finalOrderNumber = orderNumber;
        final AtomicReference<MaintenanceOrderDetailResults> resultRef = new AtomicReference<>();
        final Await<FlightHubError> await = new Await<>(name() + ".getMaintenanceOrderDetail");
        Outcome outcome = runOutcome("查询订单详情", actionTimeout, await, () ->
            module.getMaintenanceOrderDetail(finalOrderNumber, new FlightHubCallback.GetMaintenanceOrderDetailCallback() {
                @Override
                public void onResult(FlightHubError error, MaintenanceOrderDetailResults result) {
                    resultRef.set(result);
                    await.set(error);
                }
            })
        );

        if (outcome == Outcome.SUCCESS) {
            MaintenanceOrderDetailResults detail = resultRef.get();
            if (detail != null) {
                console("   订单详情:");
                console("     订单编号:   " + detail.getOrderNumber());
                console("     状态:       " + detail.getStatus()
                    + "(" + detail.getStatusText() + ")");
                console("     来源:       " + detail.getSourceType()
                    + "(" + detail.getSourceTypeText() + ")");
                console("     设备 SN:    " + detail.getDeviceSn()
                    + "  modelKey: " + detail.getDeviceModelKey());
                console("     故障描述:   " + detail.getFaultDescription());
                console("     联系人:     " + detail.getContactPerson()
                    + "  电话: " + detail.getContactPhone());
                console("     创建时间:   " + detail.getCreatedAt());
                console("     更新时间:   " + detail.getUpdatedAt());
                if (detail.getRuiyunOrderNumber() != null
                        && !detail.getRuiyunOrderNumber().isEmpty()) {
                    console("     瑞云订单号: " + detail.getRuiyunOrderNumber()
                        + "  同步状态: " + detail.getRuiyunSyncStatus());
                }
                DemoLog.info(name(), "orderDetail: " + detail);
            }
        }
    }

    // ==================== [4] 分页查询订单列表 ====================

    /**
     * 分页查询报修订单列表。
     *
     * <p>参数取自 {@code maintenance} 配置节：{@code list_page}/{@code list_page_size} 控制分页,
     * {@code list_status_filter} 可按订单状态筛选(空字符串=不筛选),
     * {@code list_device_sn_filter} 可精确匹配设备 SN。
     *
     * <p>成功后打印每条订单的编号、状态、设备 SN、创建时间, 以及分页总量。
     * 若本次会话创建了订单, 自动检查该订单是否出现在当前页并给出提示。
     */
    private void getMaintenanceOrdersList(MaintenanceManagementModule module, long actionTimeout) {
        Node cfg = params.node("maintenance");
        int page = cfg.intVal("list_page", 1);
        int pageSize = cfg.intVal("list_page_size", 10);
        String statusFilter = cfg.str("list_status_filter", "");
        String deviceSnFilter = cfg.str("list_device_sn_filter", "");

        GetMaintenanceOrdersListParams reqParams = new GetMaintenanceOrdersListParams();
        reqParams.setPage(page);
        reqParams.setPageSize(pageSize);
        if (!statusFilter.isEmpty()) {
            reqParams.setStatus(statusFilter);
        }
        if (!deviceSnFilter.isEmpty()) {
            reqParams.setDeviceSn(deviceSnFilter);
        }
        // 按创建时间倒序：最新创建的排在前面, 便于验证 [2] 的结果
        reqParams.setSortColumn("created_at");
        reqParams.setSortType("desc");

        console("\n   查询订单列表: page=" + page + " pageSize=" + pageSize
            + (statusFilter.isEmpty() ? "" : " status=" + statusFilter)
            + (deviceSnFilter.isEmpty() ? "" : " deviceSn=" + deviceSnFilter));
        DemoLog.info(name(), "getMaintenanceOrdersList: " + reqParams);

        final AtomicReference<MaintenanceOrdersListResults> resultRef = new AtomicReference<>();
        final Await<FlightHubError> await = new Await<>(name() + ".getMaintenanceOrdersList");
        Outcome outcome = runOutcome("查询订单列表", actionTimeout, await, () ->
            module.getMaintenanceOrdersList(reqParams, new FlightHubCallback.GetMaintenanceOrdersListCallback() {
                @Override
                public void onResult(FlightHubError error, MaintenanceOrdersListResults result) {
                    resultRef.set(result);
                    await.set(error);
                }
            })
        );

        if (outcome == Outcome.SUCCESS) {
            MaintenanceOrdersListResults result = resultRef.get();
            if (result != null) {
                List<MaintenanceOrdersListItem> list =
                        result.getList() != null ? result.getList() : Collections.emptyList();
                long total = result.getPagination() != null ? result.getPagination().getTotal() : -1;
                console("   订单列表(本页 " + list.size() + " 条 / 总计 "
                    + (total >= 0 ? total : "?") + " 条):");
                for (MaintenanceOrdersListItem item : list) {
                    console("     " + item.getOrderNumber()
                        + "  状态=" + item.getStatus() + "(" + item.getStatusText() + ")"
                        + "  设备=" + item.getDeviceSn()
                        + "  创建=" + item.getCreatedAt());
                    DemoLog.info(name(), "listItem: " + item.getOrderNumber()
                        + " status=" + item.getStatus() + " sn=" + item.getDeviceSn());
                }
                if (list.isEmpty()) {
                    hint("当前分页无数据, 可调整 list_status_filter 或 list_device_sn_filter 过滤条件");
                }
                // 若本次会话创建了订单, 自动检查是否在当前列表中
                if (lastCreatedOrderNumber != null) {
                    boolean found = false;
                    for (MaintenanceOrdersListItem item : list) {
                        if (lastCreatedOrderNumber.equals(item.getOrderNumber())) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        hint("已确认：本次创建的订单 " + lastCreatedOrderNumber
                            + " 出现在当前列表中");
                    } else {
                        hint("注意：本次创建的订单 " + lastCreatedOrderNumber
                            + " 未在当前页找到, 可能在其他页或状态不匹配过滤条件");
                    }
                }
            }
        }
    }

    // ==================== 辅助方法 ====================

    /** 报修来源类型描述(0=定损 2=日志反馈 3=直接报修, 见 MaintenanceOrderDeviceItemParams bean 注释)。 */
    private static String describeSourceType(int sourceType) {
        switch (sourceType) {
            case 0:  return "定损";
            case 2:  return "日志反馈";
            case 3:  return "直接报修";
            default: return "未知";
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
     * 主菜单文案：4 个菜单项与 {@code MaintenanceManagementModule} 同名接口一一对应(见类 Javadoc 业务流程图)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 保修管理 (Maintenance) ===\n"
            + "  1.  查询报修配置       (getRepairConfigs: 确认设备是否支持在线报修 + 允许的报修来源类型)\n"
            + "  2.  创建报修订单       (createMaintenanceOrder: 参数取自 maintenance 配置节; 结果 orderNumber 自动保存)\n"
            + "  3.  查询订单详情       (getMaintenanceOrderDetail: 自动沿用 [2] 的 orderNumber, 也可配置 query_order_number)\n"
            + "  4.  分页查询订单列表   (getMaintenanceOrdersList: 验证新建订单出现在列表中)\n"
            + "\n"
            + "  操作建议: [1] 查配置 → [2] 创建 → [3] 查详情 → [4] 查列表\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
