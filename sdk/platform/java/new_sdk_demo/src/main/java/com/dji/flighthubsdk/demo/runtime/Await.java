// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 回调等待器：把「异步 onResult 回调」桥接为「同步等待一个结果」。
 *
 * <p>用 {@link CountDownLatch} 实现无忙等、可超时的等待, 避免 {@code AtomicBoolean done +
 * while(!done) Thread.sleep(...)} 这类忙等反模式。
 *
 * <p><b>线程约定</b>：SDK 回调在 native/RTC 线程触发, 务必不要在回调里做阻塞操作;
 * 这里只做「保存结果 + countDown」, 主线程通过 {@link #await(long)} 取回结果。
 *
 * <pre>{@code
 * Await<FlightHubError> a = new Await<>();
 * drone.getFlightControlModule().returnHome(err -> a.set(err));
 * FlightHubError err = a.await(10_000); // 超时抛 FlightHubException
 * }</pre>
 *
 * @param <T> 结果类型
 */
public final class Await<T> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<T> result = new AtomicReference<T>();
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final String name;

    public Await() {
        this("await");
    }

    public Await(String name) {
        this.name = name;
    }

    /**
     * 在回调里调用：保存结果并唤醒等待方(幂等, 只有第一个结果生效, 含 {@code null} 结果)。
     *
     * @return true = 本次调用是首个生效的结果;false = 已有结果(本次被忽略)。
     *         调用方可据此决定「结果已被采纳」类日志是否打印, 避免迟到回调留下误导性记录。
     */
    public boolean set(T value) {
        // 用独立 done 标志做 CAS, 实现真正的「首个结果生效」——不依赖结果是否为 null,
        // 也避免竞态双投递时被后到的回调覆盖。
        if (done.compareAndSet(false, true)) {
            result.set(value);
            latch.countDown();
            return true;
        }
        return false;
    }

    /**
     * 同步等待结果。
     *
     * @param timeoutMs 超时毫秒；<b>必须 &gt; 0</b>——传入 ≤0 立即抛出 {@link FlightHubException}
     *                  并附带诊断信息，防止配置写 0 后产生「所有操作即刻超时」的难以定位现象。
     * @return 回调传入的结果
     * @throws FlightHubException 超时、被中断或 {@code timeoutMs ≤ 0}
     */
    public T await(long timeoutMs) {
        if (timeoutMs <= 0) {
            throw new FlightHubException("[" + name + "] 超时配置无效(timeoutMs=" + timeoutMs
                    + "), 必须 > 0 — 请检查 demo_params.json 中对应的超时字段");
        }
        try {
            if (!latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                throw new FlightHubException("[" + name + "] 等待回调超时(" + timeoutMs + "ms)");
            }
            return result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FlightHubException("[" + name + "] 等待被中断", e);
        }
    }

    /** 是否已经收到结果(非阻塞)。 */
    public boolean isDone() {
        return latch.getCount() == 0;
    }
}
