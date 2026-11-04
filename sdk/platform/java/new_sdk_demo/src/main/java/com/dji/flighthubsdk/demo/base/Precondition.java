// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.base;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.TakeoffParams;
import com.dji.flighthubsdk.bean.TakeoffResult;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.TakeoffModule;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.DemoParams;
import com.dji.flighthubsdk.demo.runtime.Errors;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.value.OutOfControlAction;
import com.dji.flighthubsdk.value.SpeedMode;
import com.dji.flighthubsdk.value.TakeoffMode;

import java.util.function.Consumer;

/**
 * 前置条件工具集: 场景 case 通过组合这些前置动作来编排执行顺序。
 *
 * <p>每个方法「同步执行 + 失败即抛 {@link FlightHubException}」, 从而在场景里可以写成:
 * <pre>{@code
 * Precondition.acquireFlightControl(drone, timeout);
 * Precondition.takeoff(drone, params, takeoffTimeout);
 * // 前置满足后再执行目标流程; 任一前置失败会中止场景并清理。
 * }</pre>
 *
 * <p>参数一律来自 {@link DemoParams}, 不做手动交互输入。
 */
public final class Precondition {

    private static final String TAG = "Precondition";

    /**
     * {@code takeoff} 配置节三个高度类字段的<b>兜底</b>默认值(米), 三者统一为 100。
     *
     * <p><b>仅在 demo_params.json 缺该键(或值无法解析成数值)时才生效</b>——配置值恒优先,
     * 见 {@link #buildTakeoffParams}。定为常量是为了让「兜底值」有唯一出处, 避免两条起飞路径
     * 各写一份字面量后再次漂移。
     */
    private static final double DEFAULT_TARGET_HEIGHT = 100;
    /** @see #DEFAULT_TARGET_HEIGHT */
    private static final double DEFAULT_SECURITY_TAKEOFF_HEIGHT = 100;
    /** @see #DEFAULT_TARGET_HEIGHT */
    private static final int DEFAULT_RTH_ALTITUDE = 100;

    private Precondition() {}

    /** 前置: 获取飞行控制权。 */
    public static void acquireFlightControl(Drone drone, long timeoutMs) {
        DemoLog.info(TAG, "获取飞行控制权...");
        ControlAuthModule auth = drone.getControlAuthModule();
        if (auth == null) {
            throw new FlightHubException("获取 ControlAuthModule 失败(返回 null), 无法获取飞行控制权");
        }
        final Await<FlightHubError> await = new Await<FlightHubError>("acquireFlightControl");
        auth.acquireFlightControlAuthority(
                new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(FlightHubError error) {
                        await.set(error);
                    }
                });
        requireSuccess("获取飞行控制权", await.await(timeoutMs));
    }

    /**
     * 前置: 释放飞行控制权 (清理用, 失败仅告警不抛)。
     *
     * <p>失败必须<b>带原因</b>落日志: 本方法只在收尾路径被调用, 一句无字段的「失败」等于让排查者
     * 面对「控制权到底有没有还回去、为什么没还」两个都答不上来的问题——而这直接决定下一次作业能不能
     * 拿到控制权。三种结果分开记: 成功 / 回调契约异常 (err 为 null) / 服务端明确失败 (带 {@link Errors} 字段)。
     */
    public static void releaseFlightControl(Drone drone, long timeoutMs) {
        try {
            ControlAuthModule auth = drone.getControlAuthModule();
            if (auth == null) {
                DemoLog.warn(TAG, "释放飞行控制权跳过: ControlAuthModule 返回 null");
                return;
            }
            final Await<FlightHubError> await = new Await<FlightHubError>("releaseFlightControl");
            auth.releaseFlightControlAuthority(
                    new FlightHubCallback.ResultCallbackWithError() {
                        @Override
                        public void onResult(FlightHubError error) {
                            await.set(error);
                        }
                    });
            FlightHubError err = await.await(timeoutMs);
            if (Errors.isSuccess(err)) {
                DemoLog.info(TAG, "释放飞行控制权: 成功");
            } else if (err == null) {
                // 原判据 `err != null && err.isSuccess()` 把这种情况和业务失败压成同一条「失败」。
                // 回调带着 null error 返回属 SDK 回调契约异常, 与服务端拒绝是两回事: 前者要查 SDK/JNI,
                // 后者要查权限与设备状态, 混在一起等于把排查方向抹掉。
                DemoLog.warn(TAG, "释放飞行控制权: 回调返回 null error(SDK 回调契约异常), 释放结果未知");
            } else {
                DemoLog.warn(TAG, "释放飞行控制权: 失败(未确认释放, 若本次并未实际持有则属预期) "
                        + Errors.describe(err));
            }
        } catch (Throwable e) {
            // 清理路径须兜 Throwable(对齐 demo 全局的 JNI Error 隔离策略, 如 ModuleDemo.runBody 与
            // 各模块收尾的 quietStep): SDK 经 JNI 调用, 原生层可能抛 Error(如 UnsatisfiedLinkError),
            // 只 catch Exception 会让 Error 穿透, 中断调用方 finally 里的后续收尾。
            DemoLog.warn(TAG, "释放飞行控制权异常(下发失败/等待超时/原生错误), 未确认释放、控制权状态未知: "
                    + Utils.describe(e));
        }
    }

    /**
     * 前置: 一键起飞。
     *
     * <p>按参考文档的关键设计点, <b>仅判断指令下发是否成功</b>, 不阻塞等待起飞完成;
     * 起飞过程由场景通过订阅进度推送来观察。
     */
    public static void takeoff(Drone drone, DemoParams params, long timeoutMs) {
        requireSuccess("一键起飞指令下发", takeoffResult(drone, params, timeoutMs));
    }

    /**
     * 由 {@code takeoff} 配置节组装一键起飞参数——<b>[6] 一键起飞与场景前置的唯一组装点</b>。
     *
     * <p>此前 {@code FlightControlDemo.takeoff()} 与 {@link #takeoffResult} 各抄一份读取逻辑,
     * 三个高度类字段的兜底默认值已实际漂移(120/100/100 vs 30/20/50), 同一份 demo_params.json
     * 缺键时两条路径会飞出不同高度。故抽成本方法, 两侧共用, 结构上杜绝再次漂移。
     *
     * <p><b>取值优先级</b>：{@code demo_params.json} 的 {@code takeoff} 节恒优先——
     * {@link Node#dbl}/{@link Node#intVal}/{@link Node#str} 只在 key 缺失或无法解析成目标类型时
     * 才回退到本方法里的字面默认值(并记一条 debug 日志)。也就是说下面这些字面量<b>只是兜底</b>,
     * 正常配置齐全时一个都用不上。
     *
     * <p>枚举字段(失控动作/起飞模式)另做 UNKNOWN 拦截：{@code fromValue} 对非法值返回
     * UNKNOWN(65535), 不拦截会把 65535 原样发往服务端(服务端通常以业务错误码拒绝, 但日志难以定位根因)。
     * 两者都回退到安全方向(返航 / 不飞向目标点的普通起飞)。
     *
     * @param params demo 参数(读其 {@code takeoff} 节)
     * @param warn   非法枚举值的告警出口——菜单式 demo 传 {@code this::hint}(控制台+日志),
     *               场景前置传 {@code msg -> DemoLog.warn(TAG, msg)}(仅日志)
     * @return 组装好的起飞参数
     */
    public static TakeoffParams buildTakeoffParams(DemoParams params, Consumer<String> warn) {
        Node cfg = params.node("takeoff");
        TakeoffParams tp = new TakeoffParams();
        tp.setTargetLatitude(cfg.dbl("target_latitude", 0));
        tp.setTargetLongitude(cfg.dbl("target_longitude", 0));
        tp.setTargetHeight(cfg.dbl("target_height", DEFAULT_TARGET_HEIGHT));
        tp.setSecurityTakeoffHeight(cfg.dbl("security_takeoff_height", DEFAULT_SECURITY_TAKEOFF_HEIGHT));
        tp.setMaxSpeed(cfg.intVal("max_speed", 10));
        tp.setRthAltitude(cfg.intVal("rth_altitude", DEFAULT_RTH_ALTITUDE));
        tp.setCommanderFlightHeight(cfg.dbl("commander_flight_height", 100));
        tp.setCommanderFlightMode(cfg.intVal("commander_flight_mode", 0));
        tp.setRthMode(cfg.intVal("rth_mode", 0));
        tp.setCloudToCloudId(cfg.str("cloud_to_cloud_id", ""));
        tp.setNeedVerifyFlightControl(cfg.intVal("need_verify_flight_control", -1));
        // 新增可选字段: -1 表示不下发, 设备保持当前设置。
        // 放在共用的 buildTakeoffParams 里而非各调用点, 否则 FlightControlDemo [6] 与场景前置
        // 会再次各抄一份并漂移(本方法存在的理由正是消除这种漂移)。
        tp.setSpeedMode(SpeedMode.fromValue(cfg.intVal("speed_mode", -1)));
        tp.setRealHeightCapEnabled(cfg.intVal("real_height_cap_enabled", -1));
        tp.setRealHeightCapLimit(cfg.intVal("real_height_cap_limit", -1));

        // 枚举兜底统一委托 safeXxx, 本方法不再自写一份: 同一份逻辑曾在三处各写一份
        // (本方法 / takeoffFull / FlightControlDemo [6]) 并已漂移成相反行为, 见 safeTakeoffMode 注释。
        // warn 口径由调用方决定: 菜单式 demo 传 hint(控制台+日志), 场景前置传 DemoLog.warn(仅日志)。
        tp.setOutOfControlAction(safeOutOfControlAction(cfg, warn));
        tp.setTakeoffMode(safeTakeoffMode(cfg, warn));
        return tp;
    }

    /**
     * 一键起飞(返回错误对象版本): 与 {@link #takeoff} 相同的参数组装与下发逻辑, 但把回调结果
     * {@link FlightHubError} 原样返回给调用方, 由调用方自行判定成功/明确拒绝(serverCode != 0)/
     * 传输层歧义, 以便实现「服务端明确拒绝时复位 tookOff 标志」之类的精细策略。
     *
     * <p>仍可能抛 {@link FlightHubException}: 模块获取失败或等待回调超时 (超时语义 = 指令可能已生效,
     * 调用方应按保守策略处理)。
     */
    public static FlightHubError takeoffResult(Drone drone, DemoParams params, long timeoutMs) {
        // 参数组装与下发全部委托 takeoffFull(其内部走 buildTakeoffParams), 本方法只做结果降级:
        // 丢掉 TakeoffResult、只把 error 交给调用方。此处曾自己再调一次 buildTakeoffParams 却把
        // 结果丢弃不用, 纯属合并残留——重复读一遍配置还会让非法枚举的告警重复打印两次。
        return takeoffFull(drone, params, timeoutMs).error;
    }

    /**
     * 一键起飞 (完整结果版本): 与 {@link #takeoffResult} 相同的参数组装与下发逻辑, 但把回调返回的
     * {@link TakeoffResult}(含 flightId/flyToId)一并返回给调用方, 供关联后续任务事件
     * (如 {@code listenCommandTaskEvent} 推送的 {@code CommandTaskData.uuid})以判断起飞任务是否
     * 真正<b>完成</b>, 而不仅仅是指令下发成功。
     *
     * <p>仍可能抛 {@link FlightHubException}: 模块获取失败或等待回调超时 (超时语义 = 指令可能已生效,
     * 调用方应按保守策略处理)。
     */
    public static TakeoffOutcome takeoffFull(Drone drone, DemoParams params, long timeoutMs) {
        // 参数组装唯一出处: 与 FlightControlDemo [6] 共用 buildTakeoffParams。
        // 本方法曾自写一份字段读取, 三个高度类字段的兜底值已漂移(30/20/50 vs 100/100/100),
        // 同一份 demo_params.json 缺键时两条起飞路径会飞出不同高度; 改为共用后结构上杜绝再次漂移。
        // 场景前置只落日志、不打乱步骤编号, 故 warn 口径传 DemoLog.warn。
        TakeoffParams tp = buildTakeoffParams(params, msg -> DemoLog.warn(TAG, msg));

        DemoLog.info(TAG, "下发一键起飞指令...");
        TakeoffModule takeoffModule = drone.getTakeoffModule();
        if (takeoffModule == null) {
            throw new FlightHubException("获取 TakeoffModule 失败(返回 null), 无法下发起飞指令");
        }
        final Await<TakeoffOutcome> await = new Await<TakeoffOutcome>("takeoff");
        takeoffModule.takeoff(tp, new FlightHubCallback.TakeoffCallback() {
            @Override
            public void onResult(FlightHubError error, TakeoffResult result) {
                await.set(new TakeoffOutcome(error, result));
            }
        });
        return await.await(timeoutMs);
    }

    /**
     * 起飞模式枚举兜底: {@code TakeoffMode.fromValue} 对非法配置值返回 {@code UNKNOWN(65535)},
     * 不拦截会把 65535 原样发往服务端。非法时回退到<b>配置默认值 {@code 0=TAKEOFF}(原地起飞)</b>——
     * 两个合法取值里更保守的一个: {@code 1=TAKEOFF_WITH_FLY_TO} 会让飞机起飞后<b>自动飞向</b>
     * {@code takeoff.target_latitude/longitude}, 而这两个坐标恰恰是配置残缺时最不可信的字段。
     *
     * <p><b>为什么抽成共享方法</b>: 本类的 {@link #takeoffFull} 与 {@code FlightControlDemo} 的
     * 一键起飞曾各写一份同样逻辑, 且已漂移成<b>相反行为</b>——一处回退 {@code TAKEOFF}, 另一处提示
     * 「回退默认 0=起飞」却赋 {@code TAKEOFF_WITH_FLY_TO}(=1)。两条路径共用同一份实现即杜绝再次漂移。
     *
     * @param cfg  {@code takeoff} 配置节
     * @param warn 修正提示的输出口径: 交互式 demo 传 {@code this::hint}(日志 + 控制台, 用户当场可见),
     *             场景传 {@code m -> DemoLog.warn(TAG, m)}(只落日志, 不打乱步骤编号)
     * @return 合法的起飞模式枚举, 绝不为 {@code UNKNOWN}
     */
    public static TakeoffMode safeTakeoffMode(Node cfg, Consumer<String> warn) {
        TakeoffMode tm = TakeoffMode.fromValue(cfg.intVal("takeoff_mode", 0));
        if (tm == TakeoffMode.UNKNOWN) {
            warn.accept("[修正] takeoff_mode 配置值非法(合法: 0=起飞 1=起飞并飞向目标点), 回退默认 0=起飞");
            tm = TakeoffMode.TAKEOFF;
        }
        return tm;
    }

    /**
     * 失控动作枚举兜底: 非法值回退到配置默认值 {@code 1=RETURN_HOME}(返航)。
     * 共享理由与输出口径同 {@link #safeTakeoffMode}。
     *
     * @param cfg  {@code takeoff} 配置节
     * @param warn 修正提示的输出口径
     * @return 合法的失控动作枚举, 绝不为 {@code UNKNOWN}
     */
    public static OutOfControlAction safeOutOfControlAction(Node cfg, Consumer<String> warn) {
        OutOfControlAction ooc = OutOfControlAction.fromValue(cfg.intVal("out_of_control_action", 1));
        if (ooc == OutOfControlAction.UNKNOWN) {
            warn.accept("[修正] out_of_control_action 配置值非法(合法: 0=悬停 1=返航 2=继续任务), 回退默认 1=返航");
            ooc = OutOfControlAction.RETURN_HOME;
        }
        return ooc;
    }

    /** 一键起飞完整结果: 错误对象 + SDK 返回的 {@link TakeoffResult}(含 flightId/flyToId, 供事件关联)。 */
    public static final class TakeoffOutcome {
        public final FlightHubError error;
        public final TakeoffResult result;

        public TakeoffOutcome(FlightHubError error, TakeoffResult result) {
            this.error = error;
            this.result = result;
        }
    }

    /**
     * 前置校验: 失败即抛, 成功落一行日志。
     *
     * <p>与 {@code ModuleDemo.requireSuccess} 的差别只在<b>消息前缀</b>({@code 前置条件失败[..]}——
     * 场景日志里要一眼看出「是前置没满足, 不是主流程失败」)和成功日志; 错误字段拼装委托
     * {@link Errors#describe}, 不再各写一份「靠注释声称对齐」的副本。
     */
    private static void requireSuccess(String label, FlightHubError err) {
        if (!Errors.isSuccess(err)) {
            throw new FlightHubException("前置条件失败[" + label + "]: " + Errors.describe(err));
        }
        DemoLog.info(TAG, label + " 成功");
    }
}
