// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.base;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoConfig;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.DemoParams;
import com.dji.flighthubsdk.demo.runtime.DeviceSession;
import com.dji.flighthubsdk.demo.runtime.FlightHubException;
import com.dji.flighthubsdk.demo.runtime.SdkRuntime;
import com.dji.flighthubsdk.demo.runtime.Subscription;
import com.dji.flighthubsdk.demo.runtime.SubscriptionRegistry;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.io.BufferedReader;
import java.util.function.Consumer;

/**
 * 所有模块 demo / 场景 demo 的基类。
 *
 * <p>相较参考文档的 DemoBase, 本基类把设备会话、订阅登记都做成随会话自动清理：
 * 由入口({@code DemoEntry})统一初始化 SDK 与选设备, 再经
 * {@link #runInSession(SdkRuntime, DemoParams, DeviceSession, BufferedReader)} 注入运行 {@link #run()},
 * 结束时逆序退订并关闭本轮会话(SDK 生命周期由入口统一 shutdown)。
 *
 * <p>子类只需实现 {@link #run()} 与 {@link #name()}, 并可用以下便捷能力：
 * <ul>
 *   <li>{@link #session} / {@link #params} / {@link #config} —— 设备句柄与参数; </li>
 *   <li>{@link #registry()} —— 注册订阅(会话结束自动退订); </li>
 *   <li>{@link #call(String, ActionInvoker, long)} —— 把「带回调的动作」同步化并校验错误码; </li>
 *   <li>{@link #subscribe(String, int, Subscription.Canceller)} —— 建立并登记订阅。</li>
 * </ul>
 */
public abstract class ModuleDemo {

    protected DemoConfig config;
    protected DemoParams params;
    protected SdkRuntime runtime;
    protected DeviceSession session;
    /**
     * 交互输入 reader：由入口({@code DemoEntry})注入的<b>共享</b>标准输入。
     *
     * <p>菜单式 demo(如 {@code DrcDemo})用它读用户选择; <b>务必复用</b>此实例、
     * 不要在 demo 内另建 {@code new BufferedReader(System.in)}——两个 reader 各自缓冲会互相"吞字节"。
     * 无交互场景(理论上)可为 {@code null}, 读输入前需判空。
     */
    protected BufferedReader input;

    /** demo 名(日志 tag 与菜单显示)。 */
    public abstract String name();

    /** 子类的实际演示逻辑。 */
    protected abstract void run() throws Exception;

    /** 是否需要设备会话(个别无需设备的 demo, 如编解码测试, 可重写为 false)。 */
    public boolean requiresDevice() {
        return true;
    }

    /**
     * 注入式运行：用外部已初始化的 runtime / params / session 跑一遍 demo。
     *
     * <p>交互菜单与非交互单跑共用——SDK 由入口统一初始化、全程复用; 本方法<b>只关闭本次 session</b>
     * (退订本轮订阅), <b>不 shutdown SDK</b>, 以便入口继续运行(回菜单或退出时统一 shutdown)。
     *
     * @param runtime 共享 SDK 运行时(不在此关闭)
     * @param params  参数
     * @param session 本轮设备会话(可为 null, 表示无需设备的 demo); 本方法结束时关闭
     * @param input   入口注入的共享标准输入 reader(供菜单式 demo 读取选择; 生命周期归入口, 不在此关闭)
     */
    public final void runInSession(SdkRuntime runtime, DemoParams params, DeviceSession session,
            BufferedReader input) {
        this.runtime = runtime;
        this.params = params;
        this.config = runtime.config();
        this.session = session;
        this.input = input;
        try {
            runBody();
        } finally {
            if (session != null) {
                session.close();
            }
            // 不关闭 runtime：SDK 生命周期归入口统一管理。
        }
    }

    /** 运行主体：打印起止分段 + 调用子类 {@link #run()}, 异常/错误只记录不外抛。 */
    private void runBody() {
        DemoLog.info(name(), "===== 开始: " + name() + " =====");
        try {
            run();
            DemoLog.info(name(), "===== 完成: " + name() + " =====");
        } catch (UnsupportedOperationException e) {
            // 脚手架 demo 抛此异常表示「尚未实现」——这是预期行为, 不是错误。
            // 降级到 WARN(不打堆栈), 避免在日志中产生误导性的「demo 执行失败」ERROR 条目,
            // 让维护者一眼区分「功能未实现」和「真实的运行期错误」。
            DemoLog.warn(name(), "demo 尚未实现(脚手架): " + Utils.describe(e));
        } catch (Throwable e) {
            // 用 Throwable 而非 Exception：SDK 是 JNI, 原生方法可能抛 Error(UnsatisfiedLinkError/
            // LinkageError 等), 若只 catch(Exception) 会漏出去把整个进程/交互会话拖垮。
            // 单个 demo 的失败(含 Error)在此隔离——记录后返回, 不影响入口菜单继续。
            DemoLog.error(name(), "demo 执行失败: " + Utils.describe(e), e);
        }
    }

    // ---- 便捷能力 ----

    /** 当前会话的订阅登记处。 */
    protected SubscriptionRegistry registry() {
        if (session == null) {
            throw new FlightHubException("当前 demo 无设备会话");
        }
        return session.registry();
    }

    /**
     * 负载索引：优先用会话按设备 modelKey 匹配到的值(device_common_configs),
     * 无会话时回退默认值 {@link DemoParams#DEFAULT_PAYLOAD_INDEX}。供负载/相机操控使用。
     */
    protected String payloadIndex() {
        if (session != null) {
            return session.targetPayloadIndex();
        }
        return DemoParams.DEFAULT_PAYLOAD_INDEX;
    }

    /** 读取超时(毫秒)。 */
    protected long timeout(String key) {
        return params.timeout(key, params.defaultTimeout());
    }

    protected long defaultTimeout() {
        return params.defaultTimeout();
    }

    /**
     * 把一个「以 ResultCallbackWithError 结尾的动作」同步化：下发后等待回调并校验错误码。
     * 失败抛 {@link FlightHubException}(携带 serverCode/serverMessage, 避免错误码信息丢失)。
     *
     * @param action 形如 {@code cb -> module.returnHome(cb)}
     */
    protected void call(String label, ActionInvoker action, long timeoutMs) {
        final Await<FlightHubError> await = new Await<FlightHubError>(label);
        action.invoke(new FlightHubCallback.ResultCallbackWithError() {
            @Override
            public void onResult(FlightHubError error) {
                await.set(error);
            }
        });
        FlightHubError err = await.await(timeoutMs);
        requireSuccess(label, err);
        DemoLog.info(name(), label + " 成功");
    }

    /** 同上, 使用默认超时。 */
    protected void call(String label, ActionInvoker action) {
        call(label, action, defaultTimeout());
    }

    /** 校验错误码, 失败即抛出(含服务端码与消息)。 */
    protected void requireSuccess(String label, FlightHubError err) {
        if (err == null) {
            throw new FlightHubException(label + " 回调返回 null error");
        }
        if (!err.isSuccess()) {
            throw new FlightHubException(label + " 失败: code=" + err.getErrorCode()
                    + " serverCode=" + err.getServerCode()
                    + " deviceCode=" + err.getDeviceCode()
                    + " msg=" + err.getServerMessage());
        }
    }

    /** 建立订阅并登记到会话 registry(会话结束自动退订)。 */
    protected Subscription subscribe(String label, int listenId, Subscription.Canceller canceller) {
        Subscription sub = Subscription.of(label, listenId, canceller);
        return registry().add(sub);
    }

    /** 让主线程休眠一段时间(用于观察推送), 可被中断。 */
    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 「接收 ResultCallbackWithError 的动作」函数式接口。 */
    @FunctionalInterface
    public interface ActionInvoker {
        void invoke(FlightHubCallback.ResultCallbackWithError callback);
    }

    // ==================== 三态结果框架(菜单式 demo 共用) ====================

    /**
     * 单个动作的三态结果。超时与明确失败<b>必须区分</b>：
     * <ul>
     *   <li>{@link #SUCCESS}  — 服务端明确返回成功;</li>
     *   <li>{@link #TIMEOUT}  — 等待超时/中断, 应答未到; 命令<b>可能</b>已在设备侧生效
     *       (迟到应答被 {@link Await} 静默丢弃), 调用方应按「可能已生效」置位状态标志,
     *       让退出收尾照常还原;</li>
     *   <li>{@link #FAILED}   — 命令下发本身失败(SDK 同步拒绝), 或服务端返回明确错误码;
     *       确认未生效, 收尾无需还原。</li>
     * </ul>
     *
     * <p>此枚举统一定义在基类，供所有菜单式 demo（{@code DrcDemo}、{@code FlightControlDemo}、
     * {@code PayloadControlDemo}）直接引用，消除原先三份私有定义的重复。
     */
    public enum Outcome { SUCCESS, FAILED, TIMEOUT }

    /**
     * 执行「{@link FlightHubCallback.ResultCallbackWithError} 动作」并回显三态结果。
     *
     * <p>不复用 {@link #call}——它把超时与错误码失败压平成同一种异常，而交互式菜单必须区分
     * （见 {@link Outcome}）。
     *
     * @param label     操作名称（用于日志和控制台回显）
     * @param action    形如 {@code cb -> module.acquireFlightControlAuthority(cb)}
     * @param timeoutMs 等待超时毫秒
     * @return 三态结果
     */
    protected Outcome callOutcome(String label, ActionInvoker action, long timeoutMs) {
        final Await<FlightHubError> await = new Await<FlightHubError>(label);
        return runOutcome(label, timeoutMs, await, () ->
                action.invoke(new FlightHubCallback.ResultCallbackWithError() {
                    @Override
                    public void onResult(FlightHubError error) {
                        await.set(error);
                    }
                }));
    }

    /**
     * 执行「{@link FlightHubCallback.ResultCallback} 动作」并回显三态结果。
     *
     * <p>DRC 连接/断开等 SDK 接口要求的回调类型是 {@link FlightHubCallback.ResultCallback}
     * 而非 {@link FlightHubCallback.ResultCallbackWithError}（两者 {@code onResult} 签名均为
     * {@code (FlightHubError error)}，仅类型不同，由具体 SDK 方法的形参类型决定），故单独提供此重载。
     *
     * @param label     操作名称
     * @param action    形如 {@code drc::connectDrcLink}
     * @param timeoutMs 等待超时毫秒
     * @return 三态结果
     */
    protected Outcome syncOutcome(String label, Consumer<FlightHubCallback.ResultCallback> action,
            long timeoutMs) {
        final Await<FlightHubError> await = new Await<FlightHubError>(label);
        return runOutcome(label, timeoutMs, await, () ->
                action.accept(new FlightHubCallback.ResultCallback() {
                    @Override
                    public void onResult(FlightHubError error) {
                        await.set(error);
                    }
                }));
    }

    /**
     * 「下发 → 等待 → 校验」的三态骨架（内部实现，由 {@link #callOutcome} / {@link #syncOutcome} 调用）。
     *
     * <p>下发失败 → FAILED（命令确认未发出）；等待超时/中断 → TIMEOUT（命令可能已生效）；
     * 错误码失败 → FAILED（服务端明确拒绝）；成功 → SUCCESS。
     * 每步结果均实时回显控制台并写日志文件。
     *
     * <p>{@code protected} 可见性：允许子类在特殊场景（如 {@code TakeoffCallback} 双参数回调）
     * 手动构造 {@link Await} 后复用本骨架，同时保持 dispatch 逻辑在调用方灵活组合。
     */
    protected Outcome runOutcome(String label, long timeoutMs, Await<FlightHubError> await,
            Runnable dispatch) {
        System.out.println("\n>> [" + label + "] 执行中 ...");
        try {
            dispatch.run();
        } catch (Exception e) {
            // 下发本身失败(SDK 同步拒绝/未初始化等): 命令未发出, 必须判 FAILED——
            // 若与下方超时分支混为一谈, 调用方会把「从未生效」当「可能已生效」置位,
            // 退出收尾就会去还原一个不存在的状态(释放未获取的控制权/断开未建立的链路)。
            System.out.println("   结果 -> [失败] " + Utils.describe(e));
            DemoLog.error(name(), label + " 下发失败: " + Utils.describe(e), e);
            return Outcome.FAILED;
        }
        FlightHubError err;
        try {
            err = await.await(timeoutMs);
        } catch (FlightHubException e) {
            // 仅等待超时/中断走到这里: 应答未到不代表命令未生效, 按 TIMEOUT 交调用方决策
            // (获取类动作按「可能已生效」置位并由收尾还原; 释放/断开类保持置位由收尾重试)。
            System.out.println("   结果 -> [超时] " + Utils.describe(e)
                    + " (应答未到, 命令可能已在设备侧生效)");
            DemoLog.warn(name(), label + " 等待超时: " + Utils.describe(e));
            return Outcome.TIMEOUT;
        }
        try {
            requireSuccess(label, err);
        } catch (Exception e) {
            System.out.println("   结果 -> [失败] " + Utils.describe(e));
            DemoLog.error(name(), label + " 失败: " + Utils.describe(e), e);
            return Outcome.FAILED;
        }
        DemoLog.info(name(), label + " 成功");
        System.out.println("   结果 -> [成功]");
        return Outcome.SUCCESS;
    }
}
