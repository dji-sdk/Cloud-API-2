// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.base;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.CloseDebugModeResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.DeviceDebugModule;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoConfig;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.DemoParams;
import com.dji.flighthubsdk.demo.runtime.DeviceSession;
import com.dji.flighthubsdk.demo.runtime.Errors;
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
 * <p>相较参考文档的 DemoBase, 本基类把设备会话、订阅登记都做成随会话自动清理:
 * 由入口 ({@code DemoEntry})统一初始化 SDK 与选设备, 再经
 * {@link #runInSession(SdkRuntime, DemoParams, DeviceSession, BufferedReader)} 注入运行 {@link #run()},
 * 结束时逆序退订并关闭本轮会话 (SDK 生命周期由入口统一 shutdown)。
 *
 * <p>子类只需实现 {@link #run()} 与 {@link #name()}, 并可用以下便捷能力:
 * <ul>
 *   <li>{@link #session} / {@link #params} / {@link #config} —— 设备句柄与参数; </li>
 *   <li>{@link #registry()} —— 注册订阅 (会话结束自动退订); </li>
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
     * 交互输入 reader: 由入口 ({@code DemoEntry})注入的<b>共享</b>标准输入。
     *
     * <p>菜单式 demo(如 {@code DrcDemo})用它读用户选择; <b>务必复用</b>此实例、
     * 不要在 demo 内另建 {@code new BufferedReader(System.in)}——两个 reader 各自缓冲会互相"吞字节"。
     * 无交互场景 (理论上)可为 {@code null}, 读输入前需判空。
     */
    protected BufferedReader input;

    /** demo 名 (日志 tag 与菜单显示)。 */
    public abstract String name();

    /** 子类的实际演示逻辑。 */
    protected abstract void run() throws Exception;

    /** 是否需要设备会话 (个别无需设备的 demo, 如编解码测试, 可重写为 false)。 */
    public boolean requiresDevice() {
        return true;
    }

    /**
     * 注入式运行: 用外部已初始化的 runtime / params / session 跑一遍 demo。
     *
     * <p>交互菜单与非交互单跑共用——SDK 由入口统一初始化、全程复用; 本方法<b>只关闭本次 session</b>
     * (退订本轮订阅), <b>不 shutdown SDK</b>, 以便入口继续运行 (回菜单或退出时统一 shutdown)。
     *
     * @param runtime 共享 SDK 运行时 (不在此关闭)
     * @param params  参数
     * @param session 本轮设备会话 (可为 null, 表示无需设备的 demo); 本方法结束时关闭
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
            // 不关闭 runtime: SDK 生命周期归入口统一管理。
        }
    }

    /** 运行主体: 打印起止分段 + 调用子类 {@link #run()}, 异常/错误记录日志并在控制台回显一行摘要, 不外抛。 */
    private void runBody() {
        DemoLog.info(name(), "===== 开始: " + name() + " =====");
        try {
            run();
            DemoLog.info(name(), "===== 完成: " + name() + " =====");
        } catch (UnsupportedOperationException e) {
            // 脚手架 demo 抛此异常表示「尚未实现」——这是预期行为, 不是错误。
            // 降级到 WARN(不打堆栈), 避免在日志中产生误导性的「demo 执行失败」ERROR 条目,
            // 让维护者一眼区分「功能未实现」和「真实的运行期错误」。
            // 现状: 全工程已无 throw new UnsupportedOperationException, 本分支只会接住 SDK/JDK 侧
            // 真实抛出的 UOE(如不可变集合被改写); 仍双写控制台, 否则那种情况既被误标成「脚手架」
            // 又在终端上不留一字。
            console("   demo 尚未实现(脚手架): " + Utils.describe(e));
            DemoLog.warn(name(), "demo 尚未实现(脚手架): " + Utils.describe(e));
        } catch (Throwable e) {
            // 用 Throwable 而非 Exception: SDK 是 JNI, 原生方法可能抛 Error(UnsatisfiedLinkError/
            // LinkageError 等), 若只 catch(Exception) 会漏出去把整个进程/交互会话拖垮。
            // 单个 demo 的失败 (含 Error)在此隔离——记录后返回, 不影响入口菜单继续。
            //
            // DemoLog 只落滚动日志文件: 只写它等于对终端用户静默——用户看到「开始执行 ...」后直接
            // 跳回菜单, 既不知道失败, 也不知道要去翻日志 (设备选错/句柄取不到/前置 fail-fast 全都
            // 走这条路)。终端只留一行摘要, 堆栈仍只进日志文件。
            // console 在前、DemoLog 在后, 与 runOutcome 中各分支的 console→DemoLog 既有顺序一致。
            console("   demo 执行失败: " + Utils.describe(e));
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
     * 负载索引: 优先用会话按设备 modelKey 匹配到的值 (device_common_configs),
     * 无会话时回退默认值 {@link DemoParams#DEFAULT_PAYLOAD_INDEX}。供负载/相机操控使用。
     */
    protected String payloadIndex() {
        if (session != null) {
            return session.targetPayloadIndex();
        }
        return DemoParams.DEFAULT_PAYLOAD_INDEX;
    }

    /** 读取超时 (毫秒)。 */
    protected long timeout(String key) {
        return params.timeout(key, params.defaultTimeout());
    }

    protected long defaultTimeout() {
        return params.defaultTimeout();
    }

    /**
     * 把一个「以 ResultCallbackWithError 结尾的动作」同步化: 下发后等待回调并校验错误码。
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

    /**
     * 校验错误码, 失败即抛出 (含服务端码与消息)。
     *
     * <p>子类习惯调用的名字保留在基类, 实现委托 {@link Errors#require}——错误字段的拼装口径全工程只有
     * {@link Errors} 一份 (见其类注释)。
     */
    protected void requireSuccess(String label, FlightHubError err) {
        Errors.require(label, err);
    }

    /**
     * {@link FlightHubError} 的单行摘要 (null 安全), 供<b>回调处</b>「不论成败都原样落一行」使用。
     *
     * <p>{@link #requireSuccess} 只在<b>失败</b>时才把错误码拼进异常消息, 成功路径不留痕; 本方法则额外
     * 带上 {@code success=}、用逗号分隔、{@code msg} 加引号, 以便一眼看出服务端消息的边界 (消息本身常含
     * 空格)。两种口径的字段名一致, 实现同在 {@link Errors} 内, 不会各自漂移。
     */
    protected static String describeError(FlightHubError e) {
        return Errors.detail(e);
    }

    /** 建立订阅并登记到会话 registry(会话结束自动退订)。 */
    protected Subscription subscribe(String label, int listenId, Subscription.Canceller canceller) {
        Subscription sub = Subscription.of(label, listenId, canceller);
        return registry().add(sub);
    }

    /** 让主线程休眠一段时间 (用于观察推送), 可被中断。 */
    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==================== 控制台输出与交互辅助 (所有 demo 共用) ====================

    /**
     * 回显一行到控制台 (stdout)。
     *
     * <p>{@link DemoLog} 只落滚动日志文件, 交互终端看不到; 菜单文案与每一步的
     * 「开始 / 成功 / 超时 / 失败」结果回显都要经由本方法, 用户才能直接看清进展。
     *
     * <p><b>本方法收敛的范围</b>: 原先 19 个 demo(含 {@code Scenario})各写一份完全相同的私有实现,
     * 现统一提升到基类——<b>各 demo 的业务回显</b>从此只有这一个改动点。
     *
     * <p><b>但它不是全工程唯一的 stdout 出口</b>(别照着"改一处即可全局重定向"去改):
     * {@code Utils.printTitle/printHeader/printKeyValue/readIntInRange}、{@code Menu.run}(菜单正文与
     * prompt)、{@code DemoEntry}(主菜单/横幅/错误回显)、{@code Devices.logDeviceList}(设备列表)
     * 仍直写 {@code System.out}——它们要么在 {@code utils}/{@code runtime} 层拿不到 demo 实例,
     * 要么根本不在某个 demo 的上下文里。真要做整体重定向 (换 {@code PrintStream}、加时间戳、写双份),
     * 须连同这几处一并调整; 只改本方法会得到一半带前缀、一半不带的错乱输出。
     */
    protected void console(String line) {
        System.out.println(line);
    }

    /**
     * 回显一行到控制台 (stdout), <b>仅在 DEBUG 级别启用时输出</b>。
     *
     * <p>用于含敏感信息 (设备 SN / IP / 邮箱 / 手机号等)的控制台回显: 生产环境 (log_level=info)
     * 下自动抑制, 避免敏感数据在终端可见; 开发调试 (log_level=debug) 时正常输出以便排查。
     */
    protected void consoleDebug(String line) {
        if (DemoLog.isDebugEnabled()) {
            System.out.println(line);
        }
    }

    /**
     * 提示信息<b>双写</b>: 既落日志文件 (带 demo 名 tag)又回显控制台 (缩进 3 空格)。
     *
     * <p>用于前置条件不满足、用户取消等<b>需要留痕</b>的提示——只 {@link #console} 会丢失事后排查
     * 线索, 只 {@link DemoLog} 用户当场看不到。
     */
    protected void hint(String msg) {
        DemoLog.info(name(), msg);
        console("   " + msg);
    }

    /**
     * 提示信息<b>双写</b> (debug 级): 既落日志文件 (DEBUG 级), 又在 DEBUG 启用时回显控制台。
     *
     * <p>用于含敏感信息的提示——与 {@link #hint} 相同的双写语义, 但级别降为 DEBUG,
     * 生产环境 (log_level=info) 下日志文件和控制台均不输出。
     */
    protected void hintDebug(String msg) {
        DemoLog.debug(name(), msg);
        if (DemoLog.isDebugEnabled()) {
            console("   " + msg);
        }
    }

    /**
     * 监听推送专用: <b>只落日志文件、不刷控制台</b>。
     *
     * <p>监听是持续的后台观察, 高频路 (OSD/电池/相机定频类)逐条回显会把菜单 prompt 滚出屏幕。
     * 回调在 SDK 线程触发, 本方法只做记录、不阻塞。
     */
    protected void pushLog(String line) {
        DemoLog.info(name(), line);
    }

    /**
     * 监听推送专用 (debug 级): <b>只落日志文件、不刷控制台</b>, 级别为 DEBUG。
     *
     * <p>用于含敏感信息 (设备 SN 等)的推送日志: 生产环境 (log_level=info) 下不落盘,
     * 开发调试时正常记录。语义与 {@link #pushLog} 一致, 仅级别不同。
     */
    protected void pushLogDebug(String line) {
        DemoLog.debug(name(), line);
    }

    /**
     * 危险动作二次确认: 须输入 {@code yes}(不区分大小写)才执行, 其他输入 (含空行/EOF)一律取消。
     *
     * <p>用于会让飞机真实起飞/移动、或产生不可逆后果 (如删除文件)的动作。确认与取消
     * <b>都写一条日志</b>——事后必须能从日志复原「这个不可逆动作是用户批准的」及其具体对象。
     *
     * @param action 动作描述 (会同时出现在确认提示、确认日志与取消提示中)
     * @return {@code true} 表示用户已确认, 调用方可继续执行
     */
    protected boolean confirmDangerous(String action) {
        String line = Utils.readLine(input,
                "\n   警告: 即将执行「" + action + "」\n   输入 yes 确认执行, 其他任意输入取消: ");
        boolean ok = line != null && line.equalsIgnoreCase("yes");
        if (ok) {
            DemoLog.info(name(), "用户确认执行: " + action);
        } else {
            hint("已取消: " + action);
        }
        return ok;
    }

    /**
     * 收尾单步的 Throwable 兜底 (<b>回显版</b>): 吞掉异常并记录, 不中断后续收尾、不掩盖主异常。
     *
     * <p>与 {@link #quiet} 的区别: 本方法<b>回显控制台并记录堆栈</b>(ERROR 级), 用于菜单式 demo
     * 退出时的还原动作——用户就在终端前, 收尾失败必须当场可见。
     *
     * <p>用 {@code Throwable} 而非 {@code Exception}: SDK 经 JNI 调用, 原生层可能抛
     * {@code Error}(如 {@code UnsatisfiedLinkError}), 只 catch {@code Exception} 会让 Error 穿透、
     * 中断 {@code finally} 里的后续收尾步骤。
     */
    protected void quietStep(String label, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            console("   [收尾] " + label + " 异常: " + Utils.describe(t));
            DemoLog.error(name(), "[收尾] " + label + " 异常: " + Utils.describe(t), t);
        }
    }

    /**
     * 收尾单步的 Throwable 兜底 (<b>静默版</b>): 只记一条 WARN, 不回显控制台、不记堆栈。
     *
     * <p>与 {@link #quietStep} 是同一族的两种策略, 差异见后者说明。本方法用于场景
     * ({@code Scenario})的线性收尾——场景的控制台输出由 {@code step(...)} 统一编号,
     * 收尾噪音混进去会打乱步骤序列。
     *
     * <p>原为 {@code Scenario} 的独有方法, 因与 {@link #quietStep} 同族而一并上提到基类,
     * 便于调用方在两种口径之间对照选择, 而不是在两个类里各找一半。
     */
    protected void quiet(String label, Runnable step) {
        try {
            step.run();
        } catch (Throwable t) {
            DemoLog.warn(name(), "[收尾] " + label + " 失败(不阻断后续收尾): " + Utils.describe(t));
        }
    }

    /** 「接收 ResultCallbackWithError 的动作」函数式接口。 */
    @FunctionalInterface
    public interface ActionInvoker {
        void invoke(FlightHubCallback.ResultCallbackWithError callback);
    }

    // ==================== 三态结果框架 (菜单式 demo 共用) ====================

    /**
     * 单个动作的三态结果。超时与明确失败<b>必须区分</b>:
     * <ul>
     *   <li>{@link #SUCCESS}  — 服务端明确返回成功; </li>
     *   <li>{@link #TIMEOUT}  — 等待超时/中断, 应答未到; 命令<b>可能</b>已在设备侧生效
     *       (迟到应答被 {@link Await} 静默丢弃), 调用方应按「可能已生效」置位状态标志,
     *       让退出收尾照常还原; </li>
     *   <li>{@link #FAILED}   — 命令下发本身失败 (SDK 同步拒绝), 或服务端返回明确错误码;
     *       确认未生效, 收尾无需还原。</li>
     * </ul>
     *
     * <p>此枚举统一定义在基类, 供所有菜单式 demo({@code DrcDemo}、{@code FlightControlDemo}、
     * {@code PayloadControlDemo})直接引用, 消除原先三份私有定义的重复。
     */
    public enum Outcome { SUCCESS, TIMEOUT, FAILED }

    /**
     * 执行「{@link FlightHubCallback.ResultCallbackWithError} 动作」并回显三态结果。
     *
     * <p>不复用 {@link #call}——它把超时与错误码失败压平成同一种异常, 而交互式菜单必须区分
     * (见 {@link Outcome})。
     *
     * @param label     操作名称 (用于日志和控制台回显)
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
     * 而非 {@link FlightHubCallback.ResultCallbackWithError}(两者 {@code onResult} 签名均为
     * {@code (FlightHubError error)}, 仅类型不同, 由具体 SDK 方法的形参类型决定), 故单独提供此重载。
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
     * 执行 {@link #callOutcome} 并仅在 {@link Outcome#SUCCESS} 时返回 true。
     *
     * <p>适用于释放/断开类动作: 超时按失败处理 (返回 false) 是安全方向——收尾逻辑会重试;
     * 获取类动作若需区分超时与失败, 请直接使用 {@link #callOutcome}。
     */
    protected boolean callItem(String label, ActionInvoker action, long timeoutMs) {
        return callOutcome(label, action, timeoutMs) == Outcome.SUCCESS;
    }

    /**
     * 执行 {@link #syncOutcome} 并仅在 {@link Outcome#SUCCESS} 时返回 true。
     *
     * <p>适用场景同 {@link #callItem}。
     */
    protected boolean syncItem(String label, Consumer<FlightHubCallback.ResultCallback> action, long timeoutMs) {
        return syncOutcome(label, action, timeoutMs) == Outcome.SUCCESS;
    }

    /**
     * 「下发 → 等待 → 校验」的三态骨架 (内部实现, 由 {@link #callOutcome} / {@link #syncOutcome} 调用)。
     *
     * <p>下发失败 → FAILED(命令确认未发出); 等待超时/中断 → TIMEOUT(命令可能已生效);
     * 错误码失败 → FAILED(服务端明确拒绝); 成功 → SUCCESS。
     * 每步结果均实时回显控制台并写日志文件。
     *
     * <p>{@code protected} 可见性: 允许子类在特殊场景 (如 {@code TakeoffCallback} 双参数回调)
     * 手动构造 {@link Await} 后复用本骨架, 同时保持 dispatch 逻辑在调用方灵活组合。
     */
    protected Outcome runOutcome(String label, long timeoutMs, Await<FlightHubError> await,
            Runnable dispatch) {
        console("\n>> [" + label + "] 执行中 ...");
        try {
            dispatch.run();
        } catch (Exception e) {
            // 下发本身失败 (SDK 同步拒绝/未初始化等): 命令未发出, 必须判 FAILED——
            // 若与下方超时分支混为一谈, 调用方会把「从未生效」当「可能已生效」置位,
            // 退出收尾就会去还原一个不存在的状态 (释放未获取的控制权/断开未建立的链路)。
            console("   结果 -> [失败] " + Utils.describe(e));
            DemoLog.error(name(), label + " 下发失败: " + Utils.describe(e), e);
            return Outcome.FAILED;
        }
        FlightHubError err;
        try {
            err = await.await(timeoutMs);
        } catch (FlightHubException e) {
            // 仅等待超时/中断走到这里: 应答未到不代表命令未生效, 按 TIMEOUT 交调用方决策
            // (获取类动作按「可能已生效」置位并由收尾还原; 释放/断开类保持置位由收尾重试)。
            console("   结果 -> [超时] " + Utils.describe(e)
                    + " (应答未到, 命令可能已在设备侧生效)");
            DemoLog.warn(name(), label + " 等待超时: " + Utils.describe(e));
            return Outcome.TIMEOUT;
        }
        try {
            requireSuccess(label, err);
        } catch (Exception e) {
            console("   结果 -> [失败] " + Utils.describe(e));
            DemoLog.error(name(), label + " 失败: " + Utils.describe(e), e);
            return Outcome.FAILED;
        }
        DemoLog.info(name(), label + " 成功");
        console("   结果 -> [成功]");
        return Outcome.SUCCESS;
    }

    // ==================== 远程调试模式收尾 (DeviceDebugDemo / FlySafeUnlockDemo 共用) ====================

    /**
     * 本 demo 是否曾自动开启机场远程调试模式。由子类在 {@code ensureDebugMode} 成功/超时后置位,
     * {@link #restoreDebugModeIfOpened} 收尾时据此判断是否需要还原。
     */
    protected boolean debugModeOpenedByDemo = false;

    /**
     * run() 收尾: 若本 demo 曾自动开启机场远程调试模式, 调用 closeDebugMode 还原, 避免设备状态残留。
     *
     * <p>失败只告警不外抛, 且兜 {@code Throwable} (对齐全工程 JNI Error 隔离策略)——收尾跑在 finally 里,
     * 抛出会掩盖主流程的真实异常。仅 {@code SUCCESS} 才清位: {@code TIMEOUT} 没确认关成, 保留置位以便
     * 日志上仍能看出「机场可能仍在调试模式」。
     *
     * @param module    机场的 DeviceDebugModule 实例 (由调用方解析, 避免收尾时再解析可能已失效的句柄)
     * @param deviceSn  机场 SN
     * @param timeout   超时毫秒
     */
    protected void restoreDebugModeIfOpened(DeviceDebugModule module, String deviceSn, long timeout) {
        if (!debugModeOpenedByDemo) {
            return;
        }
        try {
            hint("[收尾] 本 demo 曾自动开启远程调试模式, 关闭还原...");
            final Await<FlightHubError> closeAwait = new Await<>(name() + ".closeDebugMode(收尾)");
            Outcome o = runOutcome("关闭远程调试模式(收尾)", timeout, closeAwait, () ->
                module.closeDebugMode(deviceSn, new FlightHubCallback.CloseDebugModeCallback() {
                    @Override
                    public void onResult(FlightHubError error, CloseDebugModeResult result) {
                        closeAwait.set(error);
                    }
                })
            );
            if (o == Outcome.SUCCESS) {
                debugModeOpenedByDemo = false;
                hint("[收尾] 远程调试模式已关闭");
            } else {
                DemoLog.warn(name(), "[收尾] 关闭远程调试模式未确认成功(" + o + "), 机场可能仍处于调试模式");
                hint("[收尾] 关闭远程调试模式未确认成功(" + o + "), 请到机场侧确认是否已退出调试模式");
            }
        } catch (Throwable t) {
            DemoLog.warn(name(), "[收尾] 关闭远程调试模式异常: " + Utils.describe(t));
            console("   [收尾] 关闭远程调试模式异常: " + Utils.describe(t) + "(机场可能仍处于调试模式)");
        }
    }
}
