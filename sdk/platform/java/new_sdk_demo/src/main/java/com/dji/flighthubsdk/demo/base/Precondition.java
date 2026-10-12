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
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.value.OutOfControlAction;
import com.dji.flighthubsdk.value.TakeoffMode;

/**
 * 前置条件工具集：场景 case 通过组合这些前置动作来编排执行顺序。
 *
 * <p>每个方法「同步执行 + 失败即抛 {@link FlightHubException}」, 从而在场景里可以写成：
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

    private Precondition() {}

    /** 前置：获取飞行控制权。 */
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

    /** 前置：释放飞行控制权(清理用, 失败仅告警不抛)。 */
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
            if (err != null && err.isSuccess()) {
                DemoLog.info(TAG, "释放飞行控制权: 成功");
            } else {
                DemoLog.warn(TAG, "释放飞行控制权: 失败");
            }
        } catch (Throwable e) {
            // 清理路径须兜 Throwable(对齐 demo 全局的 JNI Error 隔离策略, 如 ModuleDemo.runBody 与
            // 各模块收尾的 quietStep): SDK 经 JNI 调用, 原生层可能抛 Error(如 UnsatisfiedLinkError),
            // 只 catch Exception 会让 Error 穿透, 中断调用方 finally 里的后续收尾。
            DemoLog.warn(TAG, "释放飞行控制权异常: " + Utils.describe(e));
        }
    }

    /**
     * 前置：一键起飞。
     *
     * <p>按参考文档的关键设计点, <b>仅判断指令下发是否成功</b>, 不阻塞等待起飞完成;
     * 起飞过程由场景通过订阅进度推送来观察。
     */
    public static void takeoff(Drone drone, DemoParams params, long timeoutMs) {
        requireSuccess("一键起飞指令下发", takeoffResult(drone, params, timeoutMs));
    }

    /**
     * 一键起飞(返回错误对象版本): 与 {@link #takeoff} 相同的参数组装与下发逻辑, 但把回调结果
     * {@link FlightHubError} 原样返回给调用方, 由调用方自行判定成功/明确拒绝(serverCode != 0)/
     * 传输层歧义, 以便实现「服务端明确拒绝时复位 tookOff 标志」之类的精细策略。
     *
     * <p>仍可能抛 {@link FlightHubException}: 模块获取失败或等待回调超时(超时语义 = 指令可能已生效,
     * 调用方应按保守策略处理)。
     */
    public static FlightHubError takeoffResult(Drone drone, DemoParams params, long timeoutMs) {
        Node cfg = params.node("takeoff");
        TakeoffParams tp = new TakeoffParams();
        // 字段读取与 FlightControlDemo [6] 完全对齐(takeoff 配置节两条路径共用, 漏读会造成静默漂移)。
        tp.setTargetLatitude(cfg.dbl("target_latitude", 0));
        tp.setTargetLongitude(cfg.dbl("target_longitude", 0));
        tp.setTargetHeight(cfg.dbl("target_height", 30));
        tp.setSecurityTakeoffHeight(cfg.dbl("security_takeoff_height", 20));
        tp.setMaxSpeed(cfg.intVal("max_speed", 10));
        tp.setRthAltitude(cfg.intVal("rth_altitude", 50));
        tp.setCommanderFlightHeight(cfg.dbl("commander_flight_height", 100));
        tp.setCommanderFlightMode(cfg.intVal("commander_flight_mode", 0));
        tp.setRthMode(cfg.intVal("rth_mode", 0));
        tp.setCloudToCloudId(cfg.str("cloud_to_cloud_id", ""));
        tp.setNeedVerifyFlightControl(cfg.intVal("need_verify_flight_control", -1));
        // 枚举兜底：与 FlightControlDemo [6] 完全对齐——fromValue 对非法值返回 UNKNOWN(65535),
        // 若不拦截会把 65535 原样发往服务端(服务端通常以业务错误码拒绝, 但日志难以定位根因)。
        OutOfControlAction ooc = OutOfControlAction.fromValue(cfg.intVal("out_of_control_action", 1));
        if (ooc == OutOfControlAction.UNKNOWN) {
            DemoLog.warn(TAG, "[修正] out_of_control_action 配置值非法(合法: 0=悬停 1=返航 2=继续任务), 回退默认 1=返航");
            ooc = OutOfControlAction.RETURN_HOME;
        }
        tp.setOutOfControlAction(ooc);
        TakeoffMode tm = TakeoffMode.fromValue(cfg.intVal("takeoff_mode", 0));
        if (tm == TakeoffMode.UNKNOWN) {
            DemoLog.warn(TAG, "[修正] takeoff_mode 配置值非法(合法: 0=起飞 1=起飞并飞向目标点), 回退默认 0=起飞");
            tm = TakeoffMode.TAKEOFF;
        }
        tp.setTakeoffMode(tm);

        DemoLog.info(TAG, "下发一键起飞指令...");
        TakeoffModule takeoffModule = drone.getTakeoffModule();
        if (takeoffModule == null) {
            throw new FlightHubException("获取 TakeoffModule 失败(返回 null), 无法下发起飞指令");
        }
        final Await<FlightHubError> await = new Await<FlightHubError>("takeoff");
        takeoffModule.takeoff(tp, new FlightHubCallback.TakeoffCallback() {
            @Override
            public void onResult(FlightHubError error, TakeoffResult result) {
                await.set(error);
            }
        });
        return await.await(timeoutMs);
    }

    private static void requireSuccess(String label, FlightHubError err) {
        if (err == null || !err.isSuccess()) {
            // 错误详情字段与 ModuleDemo.requireSuccess 对齐(含 deviceCode), 便于诊断固件侧拒绝。
            String detail = err == null ? "null error"
                    : ("code=" + err.getErrorCode() + " serverCode=" + err.getServerCode()
                    + " deviceCode=" + err.getDeviceCode() + " msg=" + err.getServerMessage());
            throw new FlightHubException("前置条件失败[" + label + "]: " + detail);
        }
        DemoLog.info(TAG, label + " 成功");
    }
}
