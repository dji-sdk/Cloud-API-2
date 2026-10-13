// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveRecordItem;
import com.dji.flighthubsdk.bean.LiveRecordStateInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.core.LiveRecordModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 直播录制 Demo(菜单驱动)：开始/停止录制、查询录制记录、订阅/取消订阅录制状态。
 *
 * <p>对应 {@code LiveRecordModule} 全部 5 个接口：
 *
 * <pre>
 * 主菜单
 * ├── [1] 开始录制            startLiveRecord
 * ├── [2] 停止录制            stopLiveRecord
 * ├── [3] 查询设备录制记录    getDeviceLiveRecords
 * ├── [4] 订阅录制状态        listenRecordState(推送只落日志)
 * └── [5] 取消订阅录制状态    cancelListenRecordState
 * </pre>
 *
 * <p><b>SDK 入口(本 demo 仅覆盖 drone 侧)：</b>先经
 * {@code drone.getLiveDeviceModule().listenLiveAvailableRange} 拿到 {@link LiveInfo},
 * 再 {@code drone.getLiveRecord(liveInfo)} 得到 {@link LiveRecordModule}。
 * demo 启动时一次性发现并缓存 module, 后续菜单操作复用。
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>直播源发现按<b>本机 SN</b> 过滤({@link #discoverLiveInfo}), 不盲选他机源；</li>
 *   <li>三态结果回显(SUCCESS/TIMEOUT/FAILED), 超时与失败明确区分；</li>
 *   <li>[4] 的订阅经基类 {@code subscribe} 登记到会话 registry(demo 结束兜底退订),
 *       正常路径由 [5] 或 run() 收尾显式关闭, 推送只落日志文件不刷控制台。</li>
 * </ul>
 */
public class LiveRecordDemo extends ModuleDemo {

    /**
     * 等待 drone 上报可用直播源(range)的超时(ms)。range 来自 SDK 每 5s 一轮的 live_capacity
     * 轮询, 10s 可覆盖 ~2 轮; 相比 {@code LiveStreamDemo} 的 20s(需额外容忍首轮 topo 拉取延迟),
     * 本 demo 场景下设备通常已在线, 无需更长等待。
     */
    private static final long RANGE_TIMEOUT_MS = 10000L;

    /** [4] 的订阅句柄(基类 subscribe 已登记到 registry, demo 结束自动收尾)。 */
    private Subscription stateSub;

    @Override
    public String name() {
        return "LiveRecordDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式直播录制 demo");
            return;
        }
        if (!session.hasDrone()) {
            hint("当前 session 未绑定无人机, LiveRecord 无法录制直播流, 跳过");
            return;
        }
        if (!session.isDroneOnline()) {
            hint("无人机不在线, LiveRecord 无法录制直播流, 跳过");
            return;
        }

        final Drone drone = session.drone();
        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        console("发现无人机直播源(最长等待 " + RANGE_TIMEOUT_MS + "ms) ...");
        final LiveInfo liveInfo = discoverLiveInfo(drone.getLiveDeviceModule(), session.droneSn(), RANGE_TIMEOUT_MS);
        if (liveInfo == null) {
            hint("未能获取无人机可用直播源(可能设备未上报直播能力 live_capacity, 或上报源均属其它设备), 无法初始化 LiveRecordModule");
            return;
        }
        final LiveRecordModule module = drone.getLiveRecord(liveInfo);
        if (module == null) {
            DemoLog.error(name(), "drone.getLiveRecord() 返回 null");
            console("   获取 LiveRecordModule 失败(返回 null), 无法进入本 demo 菜单");
            return;
        }
        DemoLog.info(name(), "LiveRecordModule 已就绪: sn=" + liveInfo.getDeviceSn()
            + " camera=" + liveInfo.getPayloadIndex() + " video=" + liveInfo.getVideoId());

        try {
            while (true) {
                Utils.printTitle("执行 " + name());
                if (stateSub != null) {
                    console("(录制状态订阅中: 推送只落日志; 选 [5] 取消)");
                }
                System.out.print(menu());
                int choice = Utils.readIntInRange(input, "请选择(1-5, q 退出): ", 1, 5);
                if (choice == Utils.QUIT) {
                    DemoLog.info(name(), "退出直播录制菜单");
                    break;
                }
                try {
                    switch (choice) {
                        case 1: startLiveRecord(module, actionTimeout);      break;
                        case 2: stopLiveRecord(module, actionTimeout);       break;
                        case 3: getDeviceLiveRecords(module, actionTimeout); break;
                        case 4: listenRecordState(module);                   break;
                        case 5: cancelListenRecordState();                   break;
                        default: break;
                    }
                } catch (Exception e) {
                    console("   结果 -> [异常] " + Utils.describe(e));
                    DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
                }
            }
        } finally {
            // registry() 会兜底关订阅, 这里显式关一次让本地字段清空, 并从 registry 移除死引用
            if (stateSub != null) {
                stateSub.close();
                registry().remove(stateSub);
                stateSub = null;
            }
        }
    }

    // ==================== [1] 开始录制 (startLiveRecord) ====================

    private void startLiveRecord(LiveRecordModule module, long timeout) {
        Node cfg = params.node("live_record");
        String timeZone = cfg.str("time_zone", "Asia/Shanghai");
        console("   开始录制: timeZone=" + timeZone);
        // @deprecated: 本接口暂未开放使用
        // callOutcome("开始录制", cb -> module.startLiveRecord(timeZone, cb), timeout);
    }

    // ==================== [2] 停止录制 (stopLiveRecord) ====================

    private void stopLiveRecord(LiveRecordModule module, long timeout) {
        // @deprecated: 本接口暂未开放使用
        // callOutcome("停止录制", cb -> module.stopLiveRecord(cb), timeout);
    }

    // ==================== [3] 查询设备录制记录 (getDeviceLiveRecords) ====================

    private void getDeviceLiveRecords(LiveRecordModule module, long timeout) {
        final String sn = session.droneSn();
        console("   查询录制记录: sn=" + sn);

        /* @deprecated: 本接口暂未开放使用
        final Await<FlightHubError> await = new Await<>(name() + ".getDeviceLiveRecords");
        final AtomicReference<List<LiveRecordItem>> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询录制记录", timeout, await, () ->
            module.getDeviceLiveRecords(sn, new FlightHubCallback.GetDeviceLiveRecordsCallback() {
                @Override
                public void onResult(FlightHubError error, List<LiveRecordItem> records) {
                    ref.set(records);
                    await.set(error);
                }
            })
        );

        if (o == Outcome.SUCCESS) {
            List<LiveRecordItem> records = ref.get();
            int count = records != null ? records.size() : 0;
            console("   共 " + count + " 条录制记录");
            if (records != null) {
                for (int i = 0; i < records.size(); i++) {
                    LiveRecordItem r = records.get(i);
                    console("   [" + (i + 1) + "] bid=" + r.getBid()
                        + "  sn=" + r.getSn() + "  camera=" + r.getCamera()
                        + "  state=" + r.getState() + "(" + describeRecordState(r.getState()) + ")"
                        + "  duration=" + r.getDuration() + "s");
                }
            }
        }
        */
    }

    // ==================== [4] 订阅录制状态 (listenRecordState) ====================

    private void listenRecordState(LiveRecordModule module) {
        if (stateSub != null) {
            hint("已在订阅中, 请先选 [5] 取消订阅");
            return;
        }
        /* @deprecated: 本接口暂未开放使用
        int listenId = module.listenRecordState(new FlightHubCallback.LiveRecordStateCallback() {
            @Override
            public void onRecordState(LiveRecordStateInfo info) {
                if (info == null) {
                    return;
                }
                pushLog("listenRecordState() #LiveRecordStateInfo, 录制状态推送: sn=" + info.getSn()
                    + " camera=" + info.getCamera() + " video=" + info.getVideo()
                    + " state=" + info.getState() + " duration=" + info.getDuration() + "s"
                    + " bid=" + info.getBid() + " createdBy=" + info.getCreatedBy());
            }
        });
        if (listenId == -1) {
            hint("订阅失败(listenId=-1)");
            return;
        }
        stateSub = subscribe("listenRecordState", listenId,
            id -> module.cancelListenRecordState(id));
        console("   订阅已建立(listenId=" + listenId + "), 推送只落日志文件");
        */
    }

    // ==================== [5] 取消订阅录制状态 (cancelListenRecordState) ====================

    private void cancelListenRecordState() {
        if (stateSub == null) {
            hint("当前无订阅, 请先选 [4] 订阅");
            return;
        }
        // @deprecated: 本接口暂未开放使用
        // stateSub.close();
        // registry().remove(stateSub);
        // stateSub = null;
        // console("   已取消订阅");
    }

    // ==================== 辅助方法 ====================

    /**
     * 一次性从 drone 的 range 回调里按<b>本机 SN</b>({@code droneSn})过滤取 LiveInfo:
     * 全部属于其它设备时不盲选, 继续等待下一次 range 回调, 直到超时返回 null——对齐
     * LiveStreamDemo / PayloadControlDemo 已修过的"跨设备错选直播源"问题, 避免误绑他机流。
     */
    private LiveInfo discoverLiveInfo(LiveDeviceModule ldm, String droneSn, long timeoutMs) {
        if (ldm == null) {
            return null;
        }
        final Await<LiveInfo> await = new Await<>(name() + ".listenLiveAvailableRange");
        int id = ldm.listenLiveAvailableRange(new FlightHubCallback.LiveAvailableRangeCallback() {
            @Override
            public void onResult(List<LiveInfo> liveInfos) {
                if (await.isDone() || liveInfos == null || liveInfos.isEmpty()) {
                    return;
                }
                LiveInfo picked = pickOwnLiveInfo(liveInfos, droneSn);
                if (picked == null) {
                    DemoLog.warn(name(), "listenLiveAvailableRange() #List<LiveInfo>, 上报的 " + liveInfos.size()
                        + " 路直播源均不属于本设备(sn=" + droneSn + "), 继续等待本机源");
                    return;
                }
                await.set(picked);
            }
        });
        if (id == -1) {
            DemoLog.warn(name(), "listenLiveAvailableRange 返回 -1");
            return null;
        }
        try {
            return await.await(timeoutMs);
        } catch (FlightHubException e) {
            DemoLog.info(name(), "等待可用直播源超时(" + timeoutMs + "ms)");
            return null;
        } finally {
            ldm.cancelListenLiveAvailableRange(id);
        }
    }

    /**
     * 从可用源列表挑一路<b>属于本设备</b>({@code ownSn})的源: sn 匹配优先; sn 为空(上报未带归属,
     * 无法验证)时兜底采用并告警; 全部明确属于其它设备则返回 {@code null}(调用方继续等待, 不盲选)。
     */
    private LiveInfo pickOwnLiveInfo(List<LiveInfo> infos, String ownSn) {
        if (infos == null || infos.isEmpty()) {
            return null;
        }
        LiveInfo unknown = null;
        for (LiveInfo li : infos) {
            if (li == null) {
                continue;
            }
            String sn = li.getDeviceSn();
            if (ownSn != null && ownSn.equals(sn)) {
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

    /** 录制状态枚举描述(见 LiveRecordItem bean 注释, 同 RecordState: 0=成功/已结束 1=录制中 2=初始化中 3=失败)。 */
    private static String describeRecordState(int state) {
        switch (state) {
            case 0:  return "成功/已结束";
            case 1:  return "录制中";
            case 2:  return "初始化中";
            case 3:  return "失败";
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

    /** 监听推送专用：只落日志文件不刷控制台, 避免推送打断菜单交互。 */
    private void pushLog(String line) {
        DemoLog.info(name(), line);
    }

    // ==================== 菜单文案 ====================

    /**
     * 主菜单文案：5 个菜单项与 {@code LiveRecordModule} 同名接口一一对应(见类 Javadoc 菜单树)。
     * 增删菜单项时须同步 {@link #run()} 中的 switch 分支与 {@code readIntInRange} 的取值上界。
     */
    private String menu() {
        return "\n"
            + "  === 直播录制 (Live Record) ===\n"
            + "  1.  开始录制           (startLiveRecord)\n"
            + "  2.  停止录制           (stopLiveRecord)\n"
            + "  3.  查询设备录制记录   (getDeviceLiveRecords)\n"
            + "  4.  订阅录制状态       (listenRecordState, 推送只落日志)\n"
            + "  5.  取消订阅录制状态   (cancelListenRecordState)\n"
            + "\n"
            + "  q.  返回上级菜单\n";
    }
}
