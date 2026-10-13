// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.base;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.List;
import java.util.function.Consumer;

/**
 * 场景 demo 基类：在 {@link ModuleDemo} 之上, 强调「跨模块编排 + 前置条件链」。
 *
 * <p>场景与模块 demo 的区别仅在语义与组织：场景通常先用 {@link Precondition} 满足依赖
 * (如获取控制权、起飞、连 DRC), 再执行目标流程, 最后统一清理。技术能力完全复用基类。
 *
 * <p>子类实现 {@link #steps()} 表达线性步骤流程; 每一步用 {@code step(...)} 打印分段日志。
 */
public abstract class Scenario extends ModuleDemo {

    private int stepNo = 0;

    @Override
    protected final void run() throws Exception {
        steps();
    }

    /** 子类实现：按步骤编排场景。 */
    protected abstract void steps() throws Exception;

    /** 打印一个步骤分段标题。 */
    protected void step(String title) {
        stepNo++;
        String line = "[Step " + stepNo + "] " + title;
        DemoLog.info(name(), line);
        console(line);
    }

    /** 回显一行到控制台(stdout)。 */
    private void console(String line) {
        System.out.println(line);
    }

    /**
     * 把「以 {@link FlightHubCallback.ResultCallback} 结尾的动作」同步化。
     *
     * <p>基类 {@link #call} 仅接受 {@link FlightHubCallback.ResultCallbackWithError}；
     * DRC 连接/断开、POI 退出/调速等 SDK 接口要求的回调类型是 {@link FlightHubCallback.ResultCallback}
     * (两者的 {@code onResult} 签名其实同为 {@code (FlightHubError error)}, 只是类型不同, 由具体 SDK
     * 方法的形参类型决定用哪个), 需要本方法桥接。失败或超时均抛
     * {@link com.dji.flighthubsdk.demo.runtime.FlightHubException}。
     *
     * <p>原先在 {@code FlyToScenario} 和 {@code PoiScenario} 中各自私有实现，现统一提升到基类，
     * 消除重复并保证未来新场景直接复用。
     *
     * @param label     操作名称（用于日志）
     * @param action    形如 {@code drc::disconnectDrcLink} 或 {@code poi::exit}
     * @param timeoutMs 等待超时毫秒
     */
    protected void syncResult(String label, Consumer<FlightHubCallback.ResultCallback> action,
            long timeoutMs) {
        final Await<FlightHubError> await = new Await<FlightHubError>(label);
        action.accept(new FlightHubCallback.ResultCallback() {
            @Override
            public void onResult(FlightHubError error) {
                await.set(error);
            }
        });
        requireSuccess(label, await.await(timeoutMs));
        DemoLog.info(name(), label + " 成功");
    }

    /**
     * 收尾单步的 Throwable 兜底：吞掉异常并记录，不中断后续收尾、不掩盖主异常。
     *
     * <p>场景的 {@code finally} 收尾块中，每一步都应经由本方法包裹——SDK 为 JNI 实现，
     * 原生层可能抛 {@link Error}（如 {@code UnsatisfiedLinkError}），只 catch {@code Exception}
     * 会导致 Error 穿透并中断后续收尾步骤。
     *
     * <p>原先在 {@code FlyToScenario} 和 {@code PoiScenario} 中各自私有实现，现统一提升到基类。
     *
     * @param label 步骤名称（用于警告日志）
     * @param step  收尾动作
     */
    protected void quiet(String label, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            DemoLog.warn(name(), "[收尾] " + label + " 失败(不阻断后续收尾): " + Utils.describe(t));
        }
    }

    /**
     * 建立订阅并登记到 {@code subs}；建立失败(如 {@code listenId == -1})时告警并跳过, 不阻断场景。
     *
     * <p>label 只传一次, 供 {@link #subscribe} 与失败告警共用, 避免两处各自硬编码同一字符串导致漂移。
     * 原先在 {@code FlyToScenario} 里为每路订阅各自重复一份 try/catch, 现提升到基类统一收敛。
     *
     * @param subs      调用方持有的订阅列表(场景收尾时统一 {@code close()})
     * @param label     订阅名(日志/告警用)
     * @param listenId  listenXxx 返回的 id
     * @param canceller 对应的 cancelListenXxx 方法引用
     */
    protected void trySubscribe(List<Subscription> subs, String label, int listenId,
            Subscription.Canceller canceller) {
        try {
            subs.add(subscribe(label, listenId, canceller));
        } catch (FlightHubException e) {
            DemoLog.warn(name(), "订阅失败(跳过, 不阻断场景): " + label + ": " + Utils.describe(e));
        }
    }
}
