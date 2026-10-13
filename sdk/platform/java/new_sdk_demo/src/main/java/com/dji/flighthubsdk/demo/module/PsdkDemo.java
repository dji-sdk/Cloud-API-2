// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CreatePsdkSpeakerResourceParams;
import com.dji.flighthubsdk.bean.EditPsdkSpeakerResourceParams;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.GetPsdkWidgetResourceParams;
import com.dji.flighthubsdk.bean.PsdkDeviceInfo;
import com.dji.flighthubsdk.bean.PsdkDeviceList;
import com.dji.flighthubsdk.bean.PsdkSpeakerAudioPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerResource;
import com.dji.flighthubsdk.bean.PsdkSpeakerResourceList;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSconfig;
import com.dji.flighthubsdk.bean.PsdkStateSpeakerInfo;
import com.dji.flighthubsdk.bean.PsdkStateSummaryInfo;
import com.dji.flighthubsdk.bean.PsdkWidgetResource;
import com.dji.flighthubsdk.bean.PsdkWidgetResourceList;
import com.dji.flighthubsdk.bean.STSInfo;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.DrcModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.PsdkControlModule;
import com.dji.flighthubsdk.core.PsdkResourceModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.base.Precondition;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.value.MegaphonePlayMode;
import com.dji.flighthubsdk.value.MegaphoneTTSLanguageType;
import com.dji.flighthubsdk.value.MegaphoneTTSVoiceType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PSDK 负载综合 Demo(菜单驱动)：状态监听、喊话器控制、控件、喊话器资源管理与探照灯控制。
 *
 * <p>覆盖 {@code PsdkControlModule} 全部 16 个控制接口与 8 路状态监听中的 3 路(非全量,
 * 探照灯状态/播放进度/浮窗文本等 5 路未演示), 以及 {@code PsdkResourceModule} 全部 7 个资源接口：
 *
 * <pre>
 * 主菜单(平铺 24 项)
 * ├── [1]        状态监听开/关   listenDrcPsdkDeviceList / listenDrcPsdkStateSummaryInfo
 * │                              / listenDrcPsdkStateSpeakerInfo(toggle, 推送只落日志)
 * ├── [2]-[8]    喊话器控制      speakerReplay / speakerPlayStop / speakerPlayVolumeSet
 * │                              / speakerTTSSet / speakerPlayModeSet / speakerTTSPlayStart
 * │                              / speakerAudioPlayStart
 * ├── [9]-[10]   控件            widgetInputBoxTextSet / widgetValueSet
 * ├── [11]-[17]  喊话器资源      createPsdkSpeakerResource / editPsdkSpeakerResource
 * │                              / deletePsdkSpeakerResource / getPsdkSpeakerResourceList
 * │                              / getSTS / getPsdkWidgetResources / uploadSpeakerAudioFile
 * └── [18]-[24]  探照灯          searchlightModeSet / searchlightBrightnessSet
 *                                / searchlightCalibration / searchlightFineTuningSet
 *                                / searchlightWideFieldModeSet / searchlightLightGimbalControlSet
 *                                / searchlightTypeSet
 * </pre>
 *
 * <p><b>接口串联设计：</b>
 * <ul>
 *   <li>启动时一次性建立 DRC 前置(飞行控制权 → 负载控制权 → DRC 链路)——drone 级控制
 *       ([2]-[10]/[18]-[24])均经 DRC 链路下发(C++ psdk_control_module.h 标注「链路: DRC」);
 *       前置失败不阻断, workspace 级资源操作([11]-[17])仍可用;</li>
 *   <li>[1] 订阅设备列表后自动记住首个设备的 psdkIndex, 后续控制项默认使用之
 *       (未订阅/无推送时回退配置 {@code psdk.psdk_index}, 见 {@link #readPsdkIndex});</li>
 *   <li>[15] getSTS → [17] uploadSpeakerAudioFile → [11] createPsdkSpeakerResource(object_key)
 *       构成「取凭证 → 上传 → 登记资源」链路: 上传后的最终 objectKey = STS objectKeyPrefix/本地文件名
 *       (SDK 内部拼接, 见 [17] 方法注释)。</li>
 * </ul>
 *
 * <p><b>SDK 入口：</b>{@code session.drone().getPsdkControlModule()}(挂在 Drone 上, 需 DRC 前置)与
 * {@code session.workspace().getPsdkResourceModule()}(挂在 Workspace 上, 纯 HTTP, 无需 DRC)。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>DRC 前置非阻塞：任一步失败仅提示「喊话器/探照灯/控件不可用」, 不影响 workspace 级资源操作;
 *       无 drone 的会话跳过前置, drone 级菜单项逐项拦截({@link #requireDrone});</li>
 *   <li>获取/建链类前置步骤按三态结果维护标志——超时(应答未到)时命令可能已生效, 按「可能已生效」置位,
 *       退出收尾照常还原; 仅明确失败(FAILED)时不置位/回置(获取控制权两步非 FAILED 才置位,
 *       建链一步先置位再下发、FAILED 回置);</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分;</li>
 *   <li>监听推送只落日志文件不刷控制台; 订阅经基类 subscribe() 登记(会话结束兜底退订),
 *       toggle 关闭与退出 finally 主动取消;</li>
 *   <li>结果解析只在 SUCCESS 门控内(convert 层对 null 输入会造成非 null 空 bean, FAILED 时判空守卫失效)。</li>
 * </ul>
 */
public class PsdkDemo extends ModuleDemo {

    /** [1] 状态监听句柄(设备列表路); 非 null 表示监听开启中。经基类 subscribe() 登记, 另存供 toggle 关闭与 finally 兜底。 */
    private Subscription deviceListSub;
    /** [1] 状态摘要监听句柄(订阅失败时为 null)。 */
    private Subscription summarySub;
    /** [1] 喊话器状态监听句柄(订阅失败时为 null)。 */
    private Subscription speakerInfoSub;
    /** [1] 设备列表推送记住的首个 psdkIndex(每次推送刷新); -1 表示尚无推送(见 {@link #readPsdkIndex} 的回退)。 */
    private final AtomicInteger currentPsdkIndex = new AtomicInteger(-1);

    @Override
    public String name() {
        return "PsdkDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式 PSDK demo");
            return;
        }
        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        // PsdkControlModule 挂在 Drone、PsdkResourceModule 挂在 Workspace：无 drone 时不整体中止
        // (session.drone() 对无 drone 会话直接抛异常, 须先 hasDrone 判定), 跳过 DRC 前置,
        // 仅 workspace 级资源菜单可用, drone 级菜单项由 requireDrone 逐项拦截。
        final Drone drone = session.hasDrone() ? session.drone() : null;
        final ControlAuthModule auth = drone != null ? drone.getControlAuthModule() : null;
        final DrcModule drc = drone != null ? drone.getDrcModule() : null;
        final String payloadIndex = payloadIndex();

        // 前置标志：超时(应答未到)时命令可能已生效, 按「可能已生效」置位并由收尾还原; 仅明确失败(FAILED)不置位/回置(见类注释)。
        boolean drcConnected = false; // 建链标志; 先置位、FAILED 回置; 收尾断开 DRC 链路
        boolean payloadAuth = false;  // 负载控制权标志; 非 FAILED 置位; 收尾释放负载权
        boolean flightAuth = false;   // 飞行控制权标志; 非 FAILED 置位; 收尾释放飞行权

        try {
            // ---- DRC 前置(非阻塞：失败不影响 workspace 级资源操作) ----
            if (auth != null && drc != null) {
                try {
                    console("\n>>> DRC 前置：建立链路(喊话器/探照灯/控件需要)...");
                    console("   [1/3] 获取飞行控制权...");
                    // 三态判定(对齐 FlightControlDemo [2]/DrcDemo [1]): 超时(TIMEOUT)应答未到, 命令可能已生效,
                    // 按「可能已生效」置位并由收尾释放; 仅明确失败(FAILED)不置位——从未获取的控制权收尾无需释放。
                    // FAILED 不中断前置: 后续步骤照常尝试(前置非阻塞, 见类注释), 各步结果由 runOutcome 三态回显。
                    flightAuth = callOutcome("获取飞行控制权", auth::acquireFlightControlAuthority, actionTimeout) != Outcome.FAILED;
                    if (!payloadIndex.isEmpty()) {
                        console("   [2/3] 获取负载控制权(payloadIndex=" + payloadIndex + ")...");
                        payloadAuth = callOutcome("获取负载控制权",
                            cb -> auth.acquirePayloadControlAuthority(payloadIndex, cb), actionTimeout) != Outcome.FAILED;
                    }
                    console("   [3/3] 建立 DRC 链路...");
                    drcConnected = true;
                    Outcome drcOutcome = syncOutcome("建立 DRC 链路", drc::connectDrcLink, actionTimeout);
                    if (drcOutcome == Outcome.FAILED) {
                        // 明确失败(下发被拒/错误码)：链路确认未建立, 收尾无需断开; TIMEOUT 保持置位由收尾断开。
                        drcConnected = false;
                        console(">>> DRC 链路建立失败(喊话器/探照灯/控件不可用, workspace 级资源操作不受影响)\n");
                    } else if (drcOutcome == Outcome.TIMEOUT) {
                        console(">>> DRC 链路状态未确认(应答超时, 可能已建立): drone 级操作可尝试, 失败请重试; 退出时照常断开\n");
                    } else {
                        console(">>> DRC 链路就绪\n");
                    }
                } catch (Exception e) {
                    console(">>> DRC 前置失败: " + Utils.describe(e) + "(喊话器/探照灯/控件不可用, workspace 级资源操作不受影响)\n");
                }
            }

            while (true) {
                Utils.printTitle("执行 " + name());
                if (deviceListSub != null) {
                    console("(状态监听中; 选 [1] 关闭)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-24, q 退出): ", 1, 24);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出 PSDK 菜单");
                    break;
                }
                try {
                    switch (choice) {
                        case 1:  toggleStateListen();                     break;
                        case 2:  runOnDrone((m, cb) -> m.speakerReplay(readPsdkIndex(), cb), "喊话器重放", actionTimeout);   break;
                        case 3:  runOnDrone((m, cb) -> m.speakerPlayStop(readPsdkIndex(), cb), "喊话器停止", actionTimeout); break;
                        case 4:  setSpeakerVolume(actionTimeout);         break;
                        case 5:  setSpeakerTts(actionTimeout);            break;
                        case 6:  setSpeakerPlayMode(actionTimeout);       break;
                        case 7:  ttsPlayStart(actionTimeout);             break;
                        case 8:  audioPlayStart(actionTimeout);           break;
                        case 9:  widgetInputBoxTextSet(actionTimeout);    break;
                        case 10: widgetValueSet(actionTimeout);           break;
                        case 11: createSpeakerResource(actionTimeout);    break;
                        case 12: editSpeakerResource(actionTimeout);      break;
                        case 13: deleteSpeakerResource(actionTimeout);    break;
                        case 14: getSpeakerResourceList(actionTimeout);   break;
                        case 15: getSts(actionTimeout);                   break;
                        case 16: getWidgetResources(actionTimeout);       break;
                        case 17: uploadSpeakerAudioFile(actionTimeout);   break;
                        case 18: setSearchlightMode(actionTimeout);       break;
                        case 19: setSearchlightBrightness(actionTimeout); break;
                        case 20: runOnDrone((m, cb) -> m.searchlightCalibration(readPsdkIndex(), cb), "探照灯校准", actionTimeout); break;
                        case 21: setSearchlightFineTuning(actionTimeout); break;
                        case 22: setSearchlightWideField(actionTimeout);  break;
                        case 23: setSearchlightGimbalCtrl(actionTimeout); break;
                        case 24: setSearchlightType(actionTimeout);       break;
                        default: break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            // 逆序收尾：断链 → 释放负载权 → 释放飞行权 → 取消订阅。
            // 前两步经 quiet 兜 Throwable(SDK 为 JNI, 原生 Error 不得中断后续收尾、也不得掩盖主异常);
            // releaseFlightControl 自身失败仅告警不抛; 订阅另有 registry 兜底。
            if (drcConnected) {
                quiet("断开 DRC 链路", () -> syncOutcome("断开 DRC 链路(收尾)", drc::disconnectDrcLink, actionTimeout));
            }
            if (payloadAuth) {
                quiet("释放负载控制权", () -> callOutcome("释放负载控制权(收尾)",
                    cb -> auth.releasePayloadControlAuthority(payloadIndex, cb), actionTimeout));
            }
            if (flightAuth) {
                Precondition.releaseFlightControl(drone, actionTimeout);
            }
            closeSubs();
        }
    }

    // ==================== [1] 状态监听 (listenDrcPsdkDeviceList / listenDrcPsdkStateSummaryInfo / listenDrcPsdkStateSpeakerInfo) ====================

    /**
     * 状态监听 toggle：开启时建立设备列表 + 状态摘要 + 喊话器状态三路订阅, 再次选择则全部取消。
     * 摘要/喊话器单路订阅失败(listen 返回 -1)仅 hint 留痕并跳过该路, 成功文案回报实际建立路数。
     *
     * <p>设备列表推送把首个设备的 psdkIndex 记入 {@link #currentPsdkIndex} 供后续控制项默认使用;
     * 摘要/喊话器两路按订阅时刻的 psdkIndex 订阅——若此刻设备列表推送尚未到达, 用的是配置回退值。
     * 推送只落日志文件不刷控制台。
     */
    private void toggleStateListen() {
        if (deviceListSub != null) {
            closeSubs();
            console("   已取消状态监听");
            return;
        }
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        final PsdkControlModule m = drone.getPsdkControlModule();
        if (m == null) {
            hint("getPsdkControlModule() 返回 null");
            return;
        }
        int devicesId = m.listenDrcPsdkDeviceList(new FlightHubCallback.PsdkDeviceListCallback() {
            @Override
            public void onResult(PsdkDeviceList list) {
                if (list == null || list.getDeviceInfos() == null) {
                    return;
                }
                for (int i = 0; i < list.getDeviceInfos().size(); i++) {
                    PsdkDeviceInfo it = list.getDeviceInfos().get(i);
                    pushLog("listenDrcPsdkDeviceList() #PsdkDeviceList, [PSDK devices] [" + i + "] psdkIndex="
                        + it.getPsdkIndex() + " objectKey=" + it.getObjectKey());
                    if (i == 0) {
                        currentPsdkIndex.set(it.getPsdkIndex());
                    }
                }
            }
        });
        if (devicesId == -1) {
            hint("订阅设备列表失败(listenDrcPsdkDeviceList 返回 -1)");
            return;
        }
        final int idx = readPsdkIndex();
        int summaryId = m.listenDrcPsdkStateSummaryInfo(idx, new FlightHubCallback.PsdkStateSummaryInfoCallback() {
            @Override
            public void onResult(PsdkStateSummaryInfo info) {
                if (info == null) {
                    return;
                }
                pushLog("listenDrcPsdkStateSummaryInfo() #PsdkStateSummaryInfo, [PSDK summary] idx="
                    + info.getPsdkIndex() + " type=" + info.getPsdkType() + " name=" + info.getPsdkName());
            }
        });
        int speakerId = m.listenDrcPsdkStateSpeakerInfo(idx, new FlightHubCallback.PsdkStateSpeakerInfoCallback() {
            @Override
            public void onResult(PsdkStateSpeakerInfo info) {
                if (info == null) {
                    return;
                }
                pushLog("listenDrcPsdkStateSpeakerInfo() #PsdkStateSpeakerInfo, [PSDK speaker] workMode="
                    + info.getWorkMode() + " playMode=" + info.getPlayMode() + " vol=" + info.getPlayVolume());
            }
        });
        // subscribe() 内部已 registry().add(会话结束兜底退订), 不得重复登记; 另存句柄供 toggle 关闭。
        // canceller 用对应的 cancelListen* 真退订(摘要/喊话器两路的 cancel 额外要求 psdkIndex 参数)。
        deviceListSub = subscribe("psdkDeviceList", devicesId, m::cancelListenDrcPsdkDeviceList);
        int routes = 1;
        if (summaryId != -1) {
            summarySub = subscribe("psdkSummary", summaryId, id -> m.cancelListenDrcPsdkStateSummaryInfo(idx, id));
            routes++;
        } else {
            hint("状态摘要订阅失败(listenDrcPsdkStateSummaryInfo 返回 -1), 该路推送不可用");
        }
        if (speakerId != -1) {
            speakerInfoSub = subscribe("psdkSpeaker", speakerId, id -> m.cancelListenDrcPsdkStateSpeakerInfo(idx, id));
            routes++;
        } else {
            hint("喊话器状态订阅失败(listenDrcPsdkStateSpeakerInfo 返回 -1), 该路推送不可用");
        }
        console("   订阅已建立(" + routes + "/3 路, psdkIndex=" + idx + "), 推送落日志文件");
    }

    // ==================== [2]-[8] 喊话器控制 (speaker*) ====================
    // [2] speakerReplay 与 [3] speakerPlayStop 无参数, 直接在 run() 的 switch 内联 runOnDrone。

    /** [4] 音量设置：volume 取自配置 {@code psdk.speaker_volume}(SDK bean 与 C++ 层均未标注取值范围, 以设备实际为准)。 */
    private void setSpeakerVolume(long timeout) {
        int v = params.node("psdk").intVal("speaker_volume", 50);
        console("   > volume=" + v + "(取自配置 psdk.speaker_volume)");
        runOnDrone((m, cb) -> m.speakerPlayVolumeSet(readPsdkIndex(), v, cb), "设置音量", timeout);
    }

    /**
     * [5] TTS 设置：参数取自配置 {@code psdk.tts_*}。0/1 映射出处 SDK {@code value/MegaphoneTTSVoiceType}
     * (0=男声 1=女声)与 {@code value/MegaphoneTTSLanguageType}(0=中文 1=英文), 与 C++ value.h 一致;
     * volume/speed 的取值范围两侧均未标注, 以设备实际为准。
     */
    private void setSpeakerTts(long timeout) {
        Node c = params.node("psdk");
        PsdkSpeakerTTSconfig cfg = new PsdkSpeakerTTSconfig();
        cfg.setVolume(c.intVal("tts_volume", 50));
        cfg.setSpeed(c.intVal("tts_speed", 5));
        cfg.setType(c.intVal("tts_type", 0) == 1 ? MegaphoneTTSVoiceType.WOMAN : MegaphoneTTSVoiceType.MAN);
        cfg.setLanguage(c.intVal("tts_language", 0) == 1 ? MegaphoneTTSLanguageType.ENGLISH : MegaphoneTTSLanguageType.CHINESE);
        console("   > vol=" + cfg.getVolume() + " speed=" + cfg.getSpeed()
            + " type=" + cfg.getType() + " language=" + cfg.getLanguage());
        runOnDrone((m, cb) -> m.speakerTTSSet(readPsdkIndex(), cfg, cb), "TTS 设置", timeout);
    }

    /**
     * [6] 播放模式：取自配置 {@code psdk.speaker_play_mode}。映射出处 SDK {@code value/MegaphonePlayMode}
     * (0=单次播放 1=循环播放(单曲)), demo 把非 0 值一律按循环播放处理。
     */
    private void setSpeakerPlayMode(long timeout) {
        int mode = params.node("psdk").intVal("speaker_play_mode", 1);
        MegaphonePlayMode pm = mode == 0 ? MegaphonePlayMode.SINGLE_PLAY : MegaphonePlayMode.LOOP_PLAYBACK;
        console("   > playMode=" + pm + "(0=单次播放 1=循环播放, 取自配置 psdk.speaker_play_mode)");
        runOnDrone((m, cb) -> m.speakerPlayModeSet(readPsdkIndex(), pm, cb), "设置播放模式", timeout);
    }

    /** [7] TTS 播放：name/text/md5 取自配置 {@code psdk.tts_play}。 */
    private void ttsPlayStart(long timeout) {
        Node c = params.node("psdk").child("tts_play");
        PsdkSpeakerTTSPlayInfo p = new PsdkSpeakerTTSPlayInfo();
        p.setName(c.str("name", "demo-tts"));
        p.setText(c.str("text", "你好"));
        p.setMd5(c.str("md5", ""));
        console("   > name=" + p.getName() + " text=" + p.getText());
        runOnDrone((m, cb) -> m.speakerTTSPlayStart(readPsdkIndex(), p, cb), "TTS 播放", timeout);
    }

    /** [8] 文件播放：name/url/md5/format 取自配置 {@code psdk.audio_play}(format 的 bean 默认值即 "pcm")。 */
    private void audioPlayStart(long timeout) {
        Node c = params.node("psdk").child("audio_play");
        PsdkSpeakerAudioPlayInfo p = new PsdkSpeakerAudioPlayInfo();
        p.setName(c.str("name", "demo-audio"));
        p.setUrl(c.str("url", ""));
        p.setMd5(c.str("md5", ""));
        p.setFormat(c.str("format", "pcm"));
        console("   > name=" + p.getName() + " url=" + p.getUrl());
        runOnDrone((m, cb) -> m.speakerAudioPlayStart(readPsdkIndex(), p, cb), "文件播放", timeout);
    }

    // ==================== [9]-[10] 控件 (widget*) ====================

    /** [9] 文本框内容：交互输入优先, 留空/EOF 回退配置 {@code psdk.widget_input_text}。 */
    private void widgetInputBoxTextSet(long timeout) {
        String def = params.node("psdk").str("widget_input_text", "hello");
        String user = Utils.readLine(input, "   请输入文本(留空使用配置默认): ");
        final String t = (user != null && !user.trim().isEmpty()) ? user.trim() : def;
        console("   > text=" + t);
        runOnDrone((m, cb) -> m.widgetInputBoxTextSet(readPsdkIndex(), t, cb), "设置文本框", timeout);
    }

    /** [10] widget 值：widgetIndex/value 取自配置 {@code psdk.widget_index}/{@code psdk.widget_value}。 */
    private void widgetValueSet(long timeout) {
        Node c = params.node("psdk");
        int idx = c.intVal("widget_index", 0);
        int val = c.intVal("widget_value", 0);
        console("   > widgetIndex=" + idx + " value=" + val);
        runOnDrone((m, cb) -> m.widgetValueSet(readPsdkIndex(), idx, val, cb), "设置 widget", timeout);
    }

    // ==================== [11]-[17] 喊话器资源 (PsdkResourceModule, workspace 级) ====================

    /**
     * [11] 创建资源：参数取自配置 {@code psdk.resource_create}。字段语义(出处 C++ request_message.h
     * {@code CreateSpeakerResourceRequest}, 与 bean javadoc 一致)：
     * <ul>
     *   <li>file_type 取值 "audio"/"tts";</li>
     *   <li>object_key/size/visible 在 file_type=audio 时必填(object_key 为对象存储 key,
     *       可由 [15]/[17] 上传链得到, 非本地路径);</li>
     *   <li>content 在 file_type=tts 时必填; duration 在 audio+record 场景必填;</li>
     *   <li>create_scene 取值 "import"/"record"; need_opus 表示是否转 opus 格式。</li>
     * </ul>
     */
    private void createSpeakerResource(long timeout) {
        PsdkResourceModule m = requirePsdkResourceModule();
        if (m == null) {
            return;
        }
        Node c = params.node("psdk").child("resource_create");
        CreatePsdkSpeakerResourceParams p = new CreatePsdkSpeakerResourceParams();
        p.setName(c.str("name", "demo-resource"));
        // [迁移备注] 配置默认值由旧版 "voice"(契约外非法值, 服务端必拒)修正为契约值 "audio"(出处: bean javadoc 与
        // C++ request_message.h, 合法值 "audio"/"tts")——未显式配置 file_type 时请求负载随之改变, 属正确性修复。
        p.setFileType(c.str("file_type", "audio"));
        p.setObjectKey(c.str("object_key", ""));
        p.setDuration(c.longVal("duration", 0L));
        p.setContent(c.str("content", ""));
        p.setCreateScene(c.str("create_scene", ""));
        p.setNeedOpus(c.bool("need_opus", false));
        p.setSize(c.longVal("size", 0L));
        p.setVisible(c.bool("visible", true));
        console("   > name=" + p.getName() + " fileType=" + p.getFileType());
        final Await<FlightHubError> await = new Await<>(name() + ".createPsdkSpeakerResource");
        final AtomicReference<PsdkSpeakerResource> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建资源", timeout, await, () ->
            m.createPsdkSpeakerResource(p, new FlightHubCallback.PsdkSpeakerResourceCallback() {
                @Override
                public void onResult(FlightHubError error, PsdkSpeakerResource resource) {
                    ref.set(resource);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            console("   已创建: id=" + ref.get().getId() + " name=" + ref.get().getName());
        }
    }

    /**
     * [12] 修改资源：resourceId 交互输入(可先用 [14] 查询), 其余字段取自配置 {@code psdk.resource_edit}。
     * 字段语义同 [11](出处 C++ request_message.h {@code EditSpeakerResourceRequest}):
     * file_type 取值 "audio"/"tts"; id 服务端期望 string 类型, bean 侧即为 String。
     */
    private void editSpeakerResource(long timeout) {
        PsdkResourceModule m = requirePsdkResourceModule();
        if (m == null) {
            return;
        }
        Integer id = readResourceId();
        if (id == null) {
            return;
        }
        Node c = params.node("psdk").child("resource_edit");
        EditPsdkSpeakerResourceParams p = new EditPsdkSpeakerResourceParams();
        p.setId(String.valueOf(id));
        p.setName(c.str("name", "demo-resource-edit"));
        // [迁移备注] 默认值 "voice"→"audio" 的修正说明见 [11] createSpeakerResource 同款备注。
        p.setFileType(c.str("file_type", "audio"));
        p.setContent(c.str("content", ""));
        p.setVisible(c.bool("visible", true));
        final Await<FlightHubError> await = new Await<>(name() + ".editPsdkSpeakerResource");
        final AtomicReference<PsdkSpeakerResource> ref = new AtomicReference<>();
        Outcome o = runOutcome("修改资源", timeout, await, () ->
            m.editPsdkSpeakerResource(p, new FlightHubCallback.PsdkSpeakerResourceCallback() {
                @Override
                public void onResult(FlightHubError error, PsdkSpeakerResource resource) {
                    ref.set(resource);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            console("   已修改: id=" + ref.get().getId() + " state=" + ref.get().getState());
        }
    }

    /** [13] 删除资源：resourceId 交互输入(可先用 [14] 查询)。 */
    private void deleteSpeakerResource(long timeout) {
        PsdkResourceModule m = requirePsdkResourceModule();
        if (m == null) {
            return;
        }
        Integer id = readResourceId();
        if (id == null) {
            return;
        }
        final int resourceId = id;
        callOutcome("删除资源", cb -> m.deletePsdkSpeakerResource(resourceId, cb), timeout);
    }

    /** [14] 查询资源列表：回显 id/name/state, 供 [12]/[13] 选取 resourceId。 */
    private void getSpeakerResourceList(long timeout) {
        PsdkResourceModule m = requirePsdkResourceModule();
        if (m == null) {
            return;
        }
        final Await<FlightHubError> await = new Await<>(name() + ".getPsdkSpeakerResourceList");
        final AtomicReference<PsdkSpeakerResourceList> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询资源列表", timeout, await, () ->
            m.getPsdkSpeakerResourceList(new FlightHubCallback.PsdkSpeakerResourceListCallback() {
                @Override
                public void onResult(FlightHubError error, PsdkSpeakerResourceList resourceList) {
                    ref.set(resourceList);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            List<PsdkSpeakerResource> list = ref.get().getResources();
            console("   共 " + (list != null ? list.size() : 0) + " 条");
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    PsdkSpeakerResource r = list.get(i);
                    console("   [" + (i + 1) + "] id=" + r.getId() + " name=" + r.getName() + " state=" + r.getState());
                }
            }
        }
    }

    /** [15] 获取 STS：回显 bucket/endpoint/region/objectKeyPrefix; 临时凭证(AK/SK/token)只落日志文件不回显控制台。 */
    private void getSts(long timeout) {
        PsdkResourceModule m = requirePsdkResourceModule();
        if (m == null) {
            return;
        }
        final Await<FlightHubError> await = new Await<>(name() + ".getSTS");
        final AtomicReference<STSInfo> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取 STS", timeout, await, () ->
            m.getSTS(new FlightHubCallback.STSInfoCallback() {
                @Override
                public void onResult(FlightHubError error, STSInfo stsInfo) {
                    ref.set(stsInfo);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            STSInfo s = ref.get();
            console("   bucket=" + s.getBucket() + " endpoint=" + s.getEndpoint() + " region=" + s.getRegion());
            console("   objectKeyPrefix=" + s.getObjectKeyPrefix());
            if (s.getCredentials() != null) {
                DemoLog.info(name(), "STS credentials=" + s.getCredentials());
            }
        }
    }

    /**
     * [16] 控件资源包：object_keys 取自配置 {@code psdk.widget_resources_object_keys}
     * (来源为 [1] 设备列表推送里各 PSDK 设备的 objectKey)。
     * object_keys 为必填字段(出处 C++ request_message.h {@code PsdkWidgetResourceRequest}),
     * 未配置时仍发送空列表, 预期由服务端返回错误。
     */
    private void getWidgetResources(long timeout) {
        PsdkResourceModule m = requirePsdkResourceModule();
        if (m == null) {
            return;
        }
        List<String> keys = new ArrayList<>();
        for (Object o : params.node("psdk").list("widget_resources_object_keys")) {
            if (o != null) {
                keys.add(o.toString());
            }
        }
        if (keys.isEmpty()) {
            hint("psdk.widget_resources_object_keys 未配置, 将发送空列表(object_keys 为必填字段, 预期由服务端报错)");
        }
        GetPsdkWidgetResourceParams p = new GetPsdkWidgetResourceParams();
        p.setObjectKeys(keys);
        final Await<FlightHubError> await = new Await<>(name() + ".getPsdkWidgetResources");
        final AtomicReference<PsdkWidgetResourceList> ref = new AtomicReference<>();
        Outcome o = runOutcome("获取控件资源包", timeout, await, () ->
            m.getPsdkWidgetResources(p, new FlightHubCallback.PsdkWidgetResourceListCallback() {
                @Override
                public void onResult(FlightHubError error, PsdkWidgetResourceList resourceList) {
                    ref.set(resourceList);
                    await.set(error);
                }
            })
        );
        if (o == Outcome.SUCCESS && ref.get() != null) {
            List<PsdkWidgetResource> urls = ref.get().getUiResourceUrls();
            console("   共 " + (urls != null ? urls.size() : 0) + " 条 URL");
        }
    }

    /**
     * [17] 上传音频：先 getSTS 取临时凭证, 再把本地文件({@code psdk.upload_file_path})上传到对象存储。
     *
     * <p>SDK 本地校验(出处 C++ psdk_resource_module_impl.cpp, 非服务端返回)：文件必须存在且 ≤10MB,
     * 否则直接回调 FILE_NOT_FOUND/FILE_TOO_LARGE。上传成功后的最终 objectKey =
     * STS objectKeyPrefix/本地文件名(由 SDK 内部拼接, 上传接口本身不回传该值),
     * 供 [11] 创建资源的 object_key 引用——注意 upload_file_path 是本地路径, 不是对象存储 key。
     */
    private void uploadSpeakerAudioFile(long timeout) {
        PsdkResourceModule m = requirePsdkResourceModule();
        if (m == null) {
            return;
        }
        String filePath = params.node("psdk").str("upload_file_path", "");
        if (filePath.isEmpty()) {
            hint("psdk.upload_file_path 未配置");
            return;
        }
        final Await<FlightHubError> stsAwait = new Await<>(name() + ".getSTS");
        final AtomicReference<STSInfo> stsRef = new AtomicReference<>();
        Outcome so = runOutcome("获取 STS", timeout, stsAwait, () ->
            m.getSTS(new FlightHubCallback.STSInfoCallback() {
                @Override
                public void onResult(FlightHubError error, STSInfo stsInfo) {
                    stsRef.set(stsInfo);
                    stsAwait.set(error);
                }
            })
        );
        if (so != Outcome.SUCCESS || stsRef.get() == null) {
            return;
        }
        callOutcome("上传音频", cb -> m.uploadSpeakerAudioFile(filePath, stsRef.get(), cb), timeout);
    }

    // ==================== [18]-[24] 探照灯 (searchlight*) ====================
    // [20] searchlightCalibration 无参数, 直接在 run() 的 switch 内联 runOnDrone。

    /** [18] 工作模式：mode 取自配置 {@code psdk.searchlight_mode}, 取值语义见 {@link #describeSearchlightWorkMode}。 */
    private void setSearchlightMode(long timeout) {
        int mode = params.node("psdk").intVal("searchlight_mode", 1);
        console("   > mode=" + mode + "(" + describeSearchlightWorkMode(mode) + ")");
        runOnDrone((m, cb) -> m.searchlightModeSet(readPsdkIndex(), mode, cb), "探照灯工作模式", timeout);
    }

    /** [19] 亮度设置：brightness 取自配置 {@code psdk.searchlight_brightness}(SDK bean 与 C++ 层均未标注取值范围, 以设备实际为准)。 */
    private void setSearchlightBrightness(long timeout) {
        int b = params.node("psdk").intVal("searchlight_brightness", 50);
        console("   > brightness=" + b + "(取自配置 psdk.searchlight_brightness)");
        runOnDrone((m, cb) -> m.searchlightBrightnessSet(readPsdkIndex(), b, cb), "探照灯亮度", timeout);
    }

    /**
     * [21] 云台微调：position/value/saved 取自配置 {@code psdk.searchlight_fine_*}。
     * 取值语义(position 0=左 1=右, value -100~100)出自旧参考 demo 文案(FlightHubSDKDemo),
     * SDK bean 与 C++ 层均只标注「微调位置/微调值」未给枚举与范围, 以实际设备为准。
     */
    private void setSearchlightFineTuning(long timeout) {
        Node c = params.node("psdk");
        int pos = c.intVal("searchlight_fine_position", 0);
        double val = c.dbl("searchlight_fine_value", 0.0);
        boolean saved = c.bool("searchlight_fine_saved", false);
        console("   > position=" + pos + " value=" + val + " saved=" + saved);
        runOnDrone((m, cb) -> m.searchlightFineTuningSet(readPsdkIndex(), pos, val, saved, cb), "探照灯微调", timeout);
    }

    /**
     * [22] 广视野：取自配置 {@code psdk.searchlight_wide_field}。开关语义(0=关 1=开)出自旧参考 demo 文案,
     * SDK bean 与 C++ 层均无枚举定义, 以实际设备为准。
     */
    private void setSearchlightWideField(long timeout) {
        int mode = params.node("psdk").intVal("searchlight_wide_field", 0);
        console("   > wideField=" + mode + "(0=关 1=开)");
        runOnDrone((mod, cb) -> mod.searchlightWideFieldModeSet(readPsdkIndex(), mode, cb), "探照灯广视野", timeout);
    }

    /**
     * [23] 云台联动：取自配置 {@code psdk.searchlight_gimbal_ctrl}。开关语义(0=关 1=开)出自旧参考 demo 文案,
     * SDK bean 与 C++ 层均无枚举定义, 以实际设备为准。
     */
    private void setSearchlightGimbalCtrl(long timeout) {
        int ctrl = params.node("psdk").intVal("searchlight_gimbal_ctrl", 0);
        console("   > gimbalCtrl=" + ctrl + "(0=关 1=开)");
        runOnDrone((mod, cb) -> mod.searchlightLightGimbalControlSet(readPsdkIndex(), ctrl, cb), "探照灯云台联动", timeout);
    }

    /**
     * [24] 灯光类型：取自配置 {@code psdk.searchlight_type}。取值语义(0=近光 1=远光 2=全开)出自旧参考
     * demo 文案(FlightHubSDKDemo), SDK bean 与 C++ 层均无枚举定义, 以实际设备为准。
     */
    private void setSearchlightType(long timeout) {
        int t = params.node("psdk").intVal("searchlight_type", 2);
        console("   > type=" + t + "(0=近光 1=远光 2=全开)");
        runOnDrone((mod, cb) -> mod.searchlightTypeSet(readPsdkIndex(), t, cb), "探照灯灯光类型", timeout);
    }

    // ==================== 收尾 ====================

    /** 取消 [1] 建立的全部监听(close 幂等且内部吞 Throwable)。toggle 关闭与退出 finally 共用。 */
    private void closeSubs() {
        if (deviceListSub != null) {
            deviceListSub.close();
            deviceListSub = null;
        }
        if (summarySub != null) {
            summarySub.close();
            summarySub = null;
        }
        if (speakerInfoSub != null) {
            speakerInfoSub.close();
            speakerInfoSub = null;
        }
    }

    /** 收尾单步的 Throwable 兜底：记录后继续, 不中断后续收尾、不掩盖主异常。 */
    private void quiet(String label, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            DemoLog.warn(name(), "[收尾] " + label + " 失败(不阻断): " + Utils.describe(t));
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * drone 级 PSDK 控制动作骨架：设备/模块判空守卫后, 经基类 {@code callOutcome} 执行并回显三态结果
     * (SUCCESS/TIMEOUT/FAILED)。全部 drone 级控制走 DRC 链路, 前置未就绪时由错误码/超时反馈。
     */
    private void runOnDrone(DroneAction action, String label, long timeout) {
        Drone drone = requireDrone();
        if (drone == null) {
            return;
        }
        PsdkControlModule m = drone.getPsdkControlModule();
        if (m == null) {
            hint("getPsdkControlModule() 返回 null");
            return;
        }
        callOutcome(label, cb -> action.invoke(m, cb), timeout);
    }

    /** drone 级动作的设备守卫：无 drone / drone 离线(选设备时刻的拓扑快照)时提示并返回 null。 */
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

    /** workspace 级资源模块守卫：获取失败时提示并返回 null。 */
    private PsdkResourceModule requirePsdkResourceModule() {
        PsdkResourceModule m = session.workspace().getPsdkResourceModule();
        if (m == null) {
            hint("getPsdkResourceModule() 返回 null");
            return null;
        }
        return m;
    }

    /**
     * 当前操作的 psdkIndex：优先用 [1] 设备列表推送记住的首个 psdkIndex(每次推送刷新),
     * 尚无推送时回退配置 {@code psdk.psdk_index}。psdkIndex 为整型 PSDK 序号,
     * 区别于相机负载索引 payloadIndex(字符串 "type-subtype-gimbalindex", 见 PsdkControlModule javadoc)。
     */
    private int readPsdkIndex() {
        int cur = currentPsdkIndex.get();
        return cur >= 0 ? cur : params.node("psdk").intVal("psdk_index", 0);
    }

    /**
     * 交互读取 resourceId(非负整数, 供 [12]/[13] 使用)：EOF(null)/q 判定先于判空与解析,
     * 非数字/负数循环重试。
     *
     * @return resourceId; null 表示用户取消(q/EOF)
     */
    private Integer readResourceId() {
        while (true) {
            String line = Utils.readLineEcho(input, "   请输入 resourceId(可先用 [14] 查询列表, q 取消): ");
            if (line == null || line.equalsIgnoreCase("q")) {
                hint("已取消");
                return null;
            }
            if (line.isEmpty()) {
                console("   resourceId 不能为空, 请输入数字 ID 或 q 取消");
                continue;
            }
            try {
                int v = Integer.parseInt(line);
                if (v < 0) {
                    console("   resourceId 不能为负数, 请输入非负整数或 q 取消");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                console("   输入无效, 请输入数字 ID(如 123)或 q 取消");
            }
        }
    }

    /** 探照灯工作模式描述(出处 SDK {@code value/SearchLightWorkMode}, 与 C++ value.h 一致)。 */
    private static String describeSearchlightWorkMode(int mode) {
        switch (mode) {
            case 0: return "关闭";
            case 1: return "常亮";
            case 2: return "爆闪";
            case 3: return "快速爆闪";
            case 4: return "交替爆闪";
            default: return "未知(" + mode + ")";
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

    /** 监听推送专用：只落日志文件不刷控制台, 避免推送打断菜单交互。 */
    private void pushLog(String line) {
        DemoLog.info(name(), line);
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：24 个菜单项与 {@code PsdkControlModule}/{@code PsdkResourceModule} 同名接口
     * 一一对应(见类 Javadoc 菜单树)。增删菜单项时须同步 {@link #run()} 中的 switch 分支与
     * {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === PSDK 负载综合 (PSDK Control & Resource) ===\n"
            + "  [状态]\n"
            + "   1.  状态监听开/关  (listenDrcPsdkDeviceList / listenDrcPsdkStateSummaryInfo / listenDrcPsdkStateSpeakerInfo)\n"
            + "  [喊话器控制 (drone, 走 DRC)]\n"
            + "   2.  重新播放       (speakerReplay)\n"
            + "   3.  停止播放       (speakerPlayStop)\n"
            + "   4.  音量设置       (speakerPlayVolumeSet)\n"
            + "   5.  TTS 设置       (speakerTTSSet)\n"
            + "   6.  播放模式       (speakerPlayModeSet)\n"
            + "   7.  TTS 播放       (speakerTTSPlayStart)\n"
            + "   8.  文件播放       (speakerAudioPlayStart)\n"
            + "  [控件 (drone, 走 DRC)]\n"
            + "   9.  文本框内容     (widgetInputBoxTextSet)\n"
            + "  10.  widget 值      (widgetValueSet)\n"
            + "  [喊话器资源 (workspace, HTTP)]\n"
            + "  11.  创建资源       (createPsdkSpeakerResource)\n"
            + "  12.  修改资源       (editPsdkSpeakerResource)\n"
            + "  13.  删除资源       (deletePsdkSpeakerResource)\n"
            + "  14.  查询列表       (getPsdkSpeakerResourceList)\n"
            + "  15.  获取 STS       (getSTS)\n"
            + "  16.  控件资源包     (getPsdkWidgetResources)\n"
            + "  17.  上传音频       (uploadSpeakerAudioFile)\n"
            + "  [探照灯 (drone, 走 DRC)]\n"
            + "  18.  工作模式       (searchlightModeSet)\n"
            + "  19.  亮度设置       (searchlightBrightnessSet)\n"
            + "  20.  云台校准       (searchlightCalibration)\n"
            + "  21.  云台微调       (searchlightFineTuningSet)\n"
            + "  22.  广视野         (searchlightWideFieldModeSet)\n"
            + "  23.  云台联动       (searchlightLightGimbalControlSet)\n"
            + "  24.  灯光类型       (searchlightTypeSet)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }

    // ==================== 内部类型 ====================

    /** 「接收 PsdkControlModule + ResultCallbackWithError 的 drone 级动作」函数式接口(供 {@link #runOnDrone} 组合)。 */
    @FunctionalInterface
    private interface DroneAction {
        void invoke(PsdkControlModule m, FlightHubCallback.ResultCallbackWithError cb);
    }
}
