// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AirsenseWarning;
import com.dji.flighthubsdk.bean.AirsenseWarningItem;
import com.dji.flighthubsdk.bean.EsdkCustomDataTransmission;
import com.dji.flighthubsdk.bean.HighestPriorityUploadFlighttaskMedia;
import com.dji.flighthubsdk.bean.PsdkCustomDataTransmission;
import com.dji.flighthubsdk.core.AirsenseWarningModule;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.EsdkControlModule;
import com.dji.flighthubsdk.core.FlightTaskMediaUploadModule;
import com.dji.flighthubsdk.core.PsdkControlModule;
import com.dji.flighthubsdk.demo.base.Menu;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.runtime.SubscriptionRegistry;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.List;

/**
 * 上云 API 2 能力补齐 Demo(菜单驱动): ESDK/PSDK 自定义数据透传、飞行任务媒体优先上传、Airsense 告警监听。
 *
 * <p>覆盖本期补齐的 4 个 drone 级模块 (均挂在 {@code Drone} 上, 经 {@code session.drone().getXxx()} 获取):
 * <pre>
 * 主菜单
 * ├── [1] ESDK 发送自定义数据       (EsdkControlModule#sendEsdkCustomDataTransmissionToEsdk)
 * ├── [2] ESDK 监听自定义数据 开/关  (EsdkControlModule#listenEsdkCustomDataTransmissionFromEsdk)
 * ├── [3] PSDK 发送自定义数据       (PsdkControlModule#sendPsdkCustomDataTransmissionToPsdk)
 * ├── [4] PSDK 监听自定义数据 开/关  (PsdkControlModule#listenPsdkCustomDataTransmissionFromPsdk)
 * ├── [5] 飞行任务媒体优先上传       (FlightTaskMediaUploadModule#uploadFlighttaskMediaPrioritize)
 * ├── [6] 最高优先级媒体 监听 开/关   (FlightTaskMediaUploadModule#listenHighestPriorityUploadFlighttaskMedia)
 * └── [7] Airsense 告警 监听 开/关   (AirsenseWarningModule#listenAirsenseWarning)
 * </pre>
 *
 * <p><b>设计要点</b>(对齐 {@link PsdkDemo}):
 * <ul>
 *   <li>发送类经基类 {@link #callOutcome} 三态回显 (SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分;</li>
 *   <li>监听 toggle: 开启后经基类 {@link #subscribe} 登记 (会话结束兜底退订), 再次选择主动退订,
 *       退出 finally 统一 {@link #closeSubs} 收尾; 推送回调运行在 SDK 线程, 只落日志文件不刷控制台;</li>
 *   <li>回调参数均为 {@code bean.*}(SDK 已在 core 层做 generated→bean 转换), demo 不接触 generated 类型;</li>
 *   <li>ESDK/PSDK 自定义数据内容长度受 SDK 约束 (&lt;256): demo 原样透传, 超长由 SDK 返回
 *       ERROR_INVALID_PARAM(不在 demo 侧截断), 便于演示真实错误码路径。</li>
 * </ul>
 *
 * <p>参数取自配置节点 {@code cloud_extra}(见 demo_params.json), 交互输入优先、留空回退配置默认。
 */
public class CloudExtraDemo extends ModuleDemo {

    /** [2] ESDK 自定义数据监听句柄; 非 null 表示监听开启中。 */
    private Subscription esdkSub;
    /** [4] PSDK 自定义数据监听句柄。 */
    private Subscription psdkSub;
    /** [6] 最高优先级媒体上传监听句柄。 */
    private Subscription mediaSub;
    /** [7] Airsense 告警监听句柄。 */
    private Subscription airsenseSub;

    @Override
    public String name() {
        return "CloudExtraDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式 demo");
            return;
        }
        final long timeout = defaultTimeout();
        DemoLog.info(name(), "actionTimeout=" + timeout + "ms");

        try {
            Menu.of(this, "执行 " + name())
                    .banner("=== 上云 API 2 能力补齐 (ESDK/PSDK 数据透传 / 媒体优先上传 / Airsense 告警) ===")
                    .header(() -> {
                        StringBuilder on = new StringBuilder();
                        if (esdkSub != null) {
                            on.append(" ESDK");
                        }
                        if (psdkSub != null) {
                            on.append(" PSDK");
                        }
                        if (mediaSub != null) {
                            on.append(" 媒体");
                        }
                        if (airsenseSub != null) {
                            on.append(" Airsense");
                        }
                        if (on.length() > 0) {
                            console("(监听中:" + on + "; 再次选择对应项可关闭)");
                        }
                    })
                    .section("[ESDK 自定义数据透传]")
                    .item("发送数据       (sendEsdkCustomDataTransmissionToEsdk)", () -> sendEsdk(timeout))
                    .item("监听数据 开/关  (listenEsdkCustomDataTransmissionFromEsdk)", () -> toggleEsdkListen())
                    .section("[PSDK 自定义数据透传]")
                    .item("发送数据       (sendPsdkCustomDataTransmissionToPsdk)", () -> sendPsdk(timeout))
                    .item("监听数据 开/关  (listenPsdkCustomDataTransmissionFromPsdk)", () -> togglePsdkListen())
                    .section("[飞行任务媒体优先上传]")
                    .item("优先上传       (uploadFlighttaskMediaPrioritize)", () -> uploadPrioritize(timeout))
                    .item("最高优先级 监听 开/关 (listenHighestPriorityUploadFlighttaskMedia)", () -> toggleMediaListen())
                    .section("[Airsense 告警]")
                    .item("告警监听 开/关  (listenAirsenseWarning)", () -> toggleAirsenseListen())
                    .exitLog("退出 上云 API 2 能力补齐 菜单")
                    .run();
        } finally {
            closeSubs();
        }
    }

    // ==================== DRC 自动建链/收尾 (发送类接口公共前置) ====================

    /**
     * 发送类接口(esdk/psdk/优先上传)的公共前置: 占负载控制权 + 建 DRC 链 + 等链路稳定。
     *
     * <p>这三个命令走 DRC 通道(provider=mqtt_broker 或 drtc), 通道仅在 DRC 链路存在期间可用,
     * 单独发送会报 ERROR_DRC_LINK。链路信息由 {@link LinkGuard} 持有, 发送方在 finally 中
     * 调用 {@link LinkGuard#close} 断链并释放控制权还原现场。
     *
     * <p>建链/占权失败只提示不中断(发送本身的三态结果仍如实回显, 便于演示真实错误码路径);
     * connectDrcLink 回调 SUCCESS 时底层连接仍可能异步进行中(MQTT paho Connect 刚发起未完成),
     * 立即发送会报 ERROR_DRC_LINK(-401), 故按 {@code drc.link_settle_ms}(默认 3000) 等待。
     *
     * @return 链路句柄; 未建链时为 null(此时发送预期报 ERROR_DRC_LINK)
     */
    private LinkGuard requireDrcLink(long timeout) {
        Drone drone = requireDrone();
        if (drone == null) {
            return null;
        }
        ControlAuthModule auth = drone.getControlAuthModule();
        DrcModule drc = drone.getDrcModule();
        if (auth == null || drc == null) {
            hint("获取 ControlAuthModule/DrcModule 失败: auth=" + (auth != null) + " drc=" + (drc != null)
                + ", 将直接发送(预期报 ERROR_DRC_LINK)");
            return null;
        }
        boolean gotAuth = false;
        if (callOutcome("获取负载控制权(发送前置)",
                cb -> auth.acquirePayloadControlAuthority(payloadIndex(), cb), timeout) != Outcome.FAILED) {
            gotAuth = true;
        }
        if (syncOutcome("DRC 链路建立(发送前置)", drc::connectDrcLink, timeout) == Outcome.FAILED) {
            if (gotAuth) {
                quietStep("释放负载控制权(收尾)", () ->
                    callItem("释放负载控制权(收尾)",
                        cb -> auth.releasePayloadControlAuthority(payloadIndex(), cb), timeout));
            }
            return null;
        }
        long settleMs = params.node("drc").longVal("link_settle_ms", 3000);
        hint("等待 " + settleMs + "ms 让 DRC 链路稳定...");
        sleep(settleMs);
        return new LinkGuard(auth, drc, gotAuth, timeout);
    }

    /** DRC 链路收尾句柄: close() 断链 + 释放控制权, 一步失败不影响另一步。 */
    private final class LinkGuard implements AutoCloseable {
        private final ControlAuthModule auth;
        private final DrcModule drc;
        private final boolean gotAuth;
        private final long timeout;

        LinkGuard(ControlAuthModule auth, DrcModule drc, boolean gotAuth, long timeout) {
            this.auth = auth;
            this.drc = drc;
            this.gotAuth = gotAuth;
            this.timeout = timeout;
        }

        @Override
        public void close() {
            quietStep("DRC 链路断开(收尾)", () ->
                syncItem("DRC 链路断开(收尾)", drc::disconnectDrcLink, timeout));
            if (gotAuth) {
                quietStep("释放负载控制权(收尾)", () ->
                    callItem("释放负载控制权(收尾)",
                        cb -> auth.releasePayloadControlAuthority(payloadIndex(), cb), timeout));
            }
        }
    }

    /**
     * 发送 + 失败自动重试一次: 首次 connectDrcLink 回调 SUCCESS 后底层 MQTT/DRTC 可能仍在异步握手,
     * 即使已按 {@code link_settle_ms} 等待, 冷启动首连仍可能报 ERROR_DRC_LINK。此时链路已建
     * (link != null), 在同一条链路上等 {@code drc.send_retry_ms}(默认 3000) 后原样重发一次。
     *
     * <p>未建链(link == null)不重试, 首发结果直接如实回显; 重发结果(成功/失败/超时)同样如实回显。
     * callOutcome 只暴露三态不含错误码, 故按「已建链 + 非成功」触发重试(重发一次幂等, 代价可忽略)。
     */
    private void sendWithRetry(String label, LinkGuard link, ActionInvoker action, long timeout) {
        Outcome first = callOutcome(label, action, timeout);
        if (first == Outcome.SUCCESS || link == null) {
            return;
        }
        long retryMs = params.node("drc").longVal("send_retry_ms", 3000);
        hint("首发未成功(" + first + "), 等待 " + retryMs + "ms 后在同一 DRC 链路上重试一次(冷启动首连可能未就绪)...");
        sleep(retryMs);
        callOutcome(label + "(重试)", action, timeout);
    }

    // ==================== [1] ESDK 发送 ====================

    /** [1] 发送 ESDK 自定义数据: 交互输入优先, 留空回退配置 {@code cloud_extra.esdk_value}; 长度受 SDK 约束 (<256)。 */
    private void sendEsdk(long timeout) {
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        EsdkControlModule m = drone.getEsdkControlModule();
        if (m == null) {
            hint("getEsdkControlModule() 返回 null");
            return;
        }
        final String value = readValue("cloud_extra", "esdk_value", "hello-esdk");
        console("   > value(len=" + value.length() + ")=" + value);
        try (LinkGuard link = requireDrcLink(timeout)) {
            sendWithRetry("ESDK 发送自定义数据", link, cb -> m.sendEsdkCustomDataTransmissionToEsdk(value, cb), timeout);
        }
    }

    // ==================== [2] ESDK 监听 ====================

    /** [2] ESDK 自定义数据监听 toggle。推送只落日志文件。 */
    private void toggleEsdkListen() {
        if (esdkSub != null) {
            esdkSub.close();
            registry().remove(esdkSub);
            esdkSub = null;
            console("   已取消 ESDK 自定义数据监听");
            return;
        }
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        EsdkControlModule m = drone.getEsdkControlModule();
        if (m == null) {
            hint("getEsdkControlModule() 返回 null");
            return;
        }
        int id = m.listenEsdkCustomDataTransmissionFromEsdk(new FlightHubCallback.EsdkCustomDataTransmissionFromEsdkCallback() {
            @Override
            public void onResult(EsdkCustomDataTransmission msg) {
                if (msg == null) {
                    return;
                }
                pushLog("listenEsdkCustomDataTransmissionFromEsdk() #EsdkCustomDataTransmission, valueLen="
                        + msg.getValue().length());
            }
        });
        if (id == -1) {
            hint("订阅失败(listenEsdkCustomDataTransmissionFromEsdk 返回 -1)");
            return;
        }
        esdkSub = subscribe("esdkCustomData", id, m::cancelListenEsdkCustomDataTransmissionFromEsdk);
        console("   ESDK 自定义数据监听已建立(listenId=" + id + "), 推送落日志文件");
    }

    // ==================== [3] PSDK 发送 ====================

    /** [3] 发送 PSDK 自定义数据: 交互输入优先, 留空回退配置 {@code cloud_extra.psdk_value}; 长度受 SDK 约束 (<256)。 */
    private void sendPsdk(long timeout) {
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        PsdkControlModule m = drone.getPsdkControlModule();
        if (m == null) {
            hint("getPsdkControlModule() 返回 null");
            return;
        }
        final String value = readValue("cloud_extra", "psdk_value", "hello-psdk");
        console("   > value(len=" + value.length() + ")=" + value);
        try (LinkGuard link = requireDrcLink(timeout)) {
            sendWithRetry("PSDK 发送自定义数据", link, cb -> m.sendPsdkCustomDataTransmissionToPsdk(value, cb), timeout);
        }
    }

    // ==================== [4] PSDK 监听 ====================

    /** [4] PSDK 自定义数据监听 toggle。推送只落日志文件。 */
    private void togglePsdkListen() {
        if (psdkSub != null) {
            psdkSub.close();
            registry().remove(psdkSub);
            psdkSub = null;
            console("   已取消 PSDK 自定义数据监听");
            return;
        }
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        PsdkControlModule m = drone.getPsdkControlModule();
        if (m == null) {
            hint("getPsdkControlModule() 返回 null");
            return;
        }
        int id = m.listenPsdkCustomDataTransmissionFromPsdk(new FlightHubCallback.PsdkCustomDataTransmissionFromPsdkCallback() {
            @Override
            public void onResult(PsdkCustomDataTransmission msg) {
                if (msg == null) {
                    return;
                }
                pushLog("listenPsdkCustomDataTransmissionFromPsdk() #PsdkCustomDataTransmission, valueLen="
                        + msg.getValue().length());
            }
        });
        if (id == -1) {
            hint("订阅失败(listenPsdkCustomDataTransmissionFromPsdk 返回 -1)");
            return;
        }
        psdkSub = subscribe("psdkCustomData", id, m::cancelListenPsdkCustomDataTransmissionFromPsdk);
        console("   PSDK 自定义数据监听已建立(listenId=" + id + "), 推送落日志文件");
    }

    // ==================== [5] 飞行任务媒体优先上传 ====================

    /** [5] 优先上传指定 flightId 的飞行任务媒体: 交互输入优先, 留空回退配置 {@code cloud_extra.flight_id}。 */
    private void uploadPrioritize(long timeout) {
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        FlightTaskMediaUploadModule m = drone.getFlightTaskMediaUploadModule();
        if (m == null) {
            hint("getFlightTaskMediaUploadModule() 返回 null");
            return;
        }
        final String flightId = readValue("cloud_extra", "flight_id", "");
        if (flightId.isEmpty()) {
            hint("flightId 为空(可交互输入或配置 cloud_extra.flight_id), 已取消");
            return;
        }
        console("   > flightId=" + flightId);
        try (LinkGuard link = requireDrcLink(timeout)) {
            sendWithRetry("飞行任务媒体优先上传", link, cb -> m.uploadFlighttaskMediaPrioritize(flightId, cb), timeout);
        }
    }

    // ==================== [6] 最高优先级媒体监听 ====================

    /** [6] 最高优先级媒体上传监听 toggle。推送只落日志文件。 */
    private void toggleMediaListen() {
        if (mediaSub != null) {
            mediaSub.close();
            registry().remove(mediaSub);
            mediaSub = null;
            console("   已取消 最高优先级媒体上传监听");
            return;
        }
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        FlightTaskMediaUploadModule m = drone.getFlightTaskMediaUploadModule();
        if (m == null) {
            hint("getFlightTaskMediaUploadModule() 返回 null");
            return;
        }
        int id = m.listenHighestPriorityUploadFlighttaskMedia(new FlightHubCallback.HighestPriorityUploadFlighttaskMediaCallback() {
            @Override
            public void onResult(HighestPriorityUploadFlighttaskMedia msg) {
                if (msg == null) {
                    return;
                }
                pushLog("listenHighestPriorityUploadFlighttaskMedia() #HighestPriorityUploadFlighttaskMedia, flightId="
                        + msg.getFlightId());
            }
        });
        if (id == -1) {
            hint("订阅失败(listenHighestPriorityUploadFlighttaskMedia 返回 -1)");
            return;
        }
        mediaSub = subscribe("highestPriorityMedia", id, m::cancelListenHighestPriorityUploadFlighttaskMedia);
        console("   最高优先级媒体上传监听已建立(listenId=" + id + "), 推送落日志文件");
    }

    // ==================== [7] Airsense 告警监听 ====================

    /** [7] Airsense 告警监听 toggle。推送只落日志文件, 逐条记录告警条目关键字段。 */
    private void toggleAirsenseListen() {
        if (airsenseSub != null) {
            airsenseSub.close();
            registry().remove(airsenseSub);
            airsenseSub = null;
            console("   已取消 Airsense 告警监听");
            return;
        }
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        AirsenseWarningModule m = drone.getAirsenseWarningModule();
        if (m == null) {
            hint("getAirsenseWarningModule() 返回 null");
            return;
        }
        int id = m.listenAirsenseWarning(new FlightHubCallback.AirsenseWarningCallback() {
            @Override
            public void onResult(AirsenseWarning airsense_warning) {
                if (airsense_warning == null) {
                    return;
                }
                List<AirsenseWarningItem> items = airsense_warning.getData();
                int n = items != null ? items.size() : 0;
                pushLog("listenAirsenseWarning() #AirsenseWarning, items=" + n);
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        AirsenseWarningItem it = items.get(i);
                        pushLog("  [" + i + "] icao=" + it.getIcao() + " level=" + it.getWarningLevel()
                                + " distance=" + it.getDistance() + " relativeAltitude=" + it.getRelativeAltitude());
                    }
                }
            }
        });
        if (id == -1) {
            hint("订阅失败(listenAirsenseWarning 返回 -1)");
            return;
        }
        airsenseSub = subscribe("airsenseWarning", id, m::cancelListenAirsenseWarning);
        console("   Airsense 告警监听已建立(listenId=" + id + "), 推送落日志文件");
    }

    // ==================== 收尾与辅助 ====================

    /** 取消本 demo 建立的全部监听 (close 幂等且内部吞 Throwable)。toggle 关闭与退出 finally 共用。 */
    private void closeSubs() {
        SubscriptionRegistry reg = registry();
        if (esdkSub != null) {
            esdkSub.close();
            reg.remove(esdkSub);
            esdkSub = null;
        }
        if (psdkSub != null) {
            psdkSub.close();
            reg.remove(psdkSub);
            psdkSub = null;
        }
        if (mediaSub != null) {
            mediaSub.close();
            reg.remove(mediaSub);
            mediaSub = null;
        }
        if (airsenseSub != null) {
            airsenseSub.close();
            reg.remove(airsenseSub);
            airsenseSub = null;
        }
    }

    /** drone 级动作的设备守卫: 无 drone / drone 离线时提示并返回 null。 */
    private Drone requireDrone() {
        if (!session.hasDrone()) {
            hint("当前会话无无人机");
            return null;
        }
        if (!session.isDroneOnline()) {
            hint("无人机离线");
            return null;
        }
        return session.drone();
    }

    /**
     * 读取字符串参数: 交互输入优先, 留空/EOF 回退配置 {@code <node>.<key>}(再回退 def)。
     */
    private String readValue(String nodeName, String key, String def) {
        Node c = params.node(nodeName);
        String fallback = c.str(key, def);
        String user = Utils.readLine(input, "   请输入内容(留空使用配置默认 \"" + fallback + "\"): ");
        return (user != null && !user.trim().isEmpty()) ? user.trim() : fallback;
    }
}
