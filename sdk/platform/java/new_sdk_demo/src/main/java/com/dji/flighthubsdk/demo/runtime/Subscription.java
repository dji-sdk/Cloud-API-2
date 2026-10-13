// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一个可自动取消的订阅句柄。
 *
 * <p><b>核心抽象</b>：{@code int id = listenXxx(cb)} 后若忘记调用
 * {@code cancelListenXxx(id)}, 就会造成监听器泄漏。
 * 本类把「listenId + 对应的 cancel 动作」打包成一个 {@link AutoCloseable},
 * 交给 {@link SubscriptionRegistry} 统一管理, 随会话/模块生命周期自动退订。
 *
 * <p>典型用法(在模块 demo 中)：
 * <pre>{@code
 * Subscription sub = Subscription.of("droneOsd",
 *         osd.listenAircraftPayloadsOsdInfo(cb),
 *         osd::cancelListenAircraftPayloadsOsdInfo);
 * registry.add(sub);          // 注册后由 registry 负责关闭
 * // ... 或 try (Subscription s = ...) { ... } 自动关闭
 * }</pre>
 */
public final class Subscription implements AutoCloseable {

    /** 取消动作：接收 listenId 并调用底层 cancelListenXxx。 */
    @FunctionalInterface
    public interface Canceller {
        void cancel(int listenId);
    }

    private final String name;
    private final int listenId;
    private final Canceller canceller;
    /** 关闭标志: CAS 抢占关闭权, 保证并发 close 时底层 cancel 只执行一次(见 {@link #close})。 */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private Subscription(String name, int listenId, Canceller canceller) {
        this.name = name;
        this.listenId = listenId;
        this.canceller = canceller;
    }

    /**
     * 创建订阅句柄。
     *
     * @param name      订阅名(日志用)
     * @param listenId  listenXxx 返回的 id; -1 视为订阅失败
     * @param canceller 对应的 cancelListenXxx 方法引用
     * @throws FlightHubException 当 listenId == -1
     */
    public static Subscription of(String name, int listenId, Canceller canceller) {
        if (listenId == -1) {
            throw new FlightHubException("订阅失败: " + name + " 返回 listenId=-1");
        }
        DemoLog.debug("Subscription", "订阅建立: " + name + " (id=" + listenId + ")");
        return new Subscription(name, listenId, canceller);
    }

    public int listenId() {
        return listenId;
    }

    public boolean isClosed() {
        return closed.get();
    }

    /** 幂等关闭：调用底层 cancel。异常只记录不抛出, 保证清理链不中断。 */
    @Override
    public void close() {
        // CAS 抢占关闭权: 「先检查后置位」的 volatile 写法在并发 close 下两个线程都能通过检查,
        // 对同一 listenId 双发 cancel 会打进原生层(行为未定义); compareAndSet 保证只有一个线程执行 cancel。
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        try {
            canceller.cancel(listenId);
            DemoLog.debug("Subscription", "订阅取消: " + name + " (id=" + listenId + ")");
        } catch (Throwable t) {
            DemoLog.warn("Subscription", "取消订阅异常: " + name + " -> " + Utils.describe(t));
        }
    }
}
