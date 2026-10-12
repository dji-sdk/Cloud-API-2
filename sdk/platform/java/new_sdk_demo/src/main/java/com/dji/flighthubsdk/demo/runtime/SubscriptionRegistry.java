// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 订阅/资源登记处：持有一批 {@link AutoCloseable}, 在 {@link #close()} 时按 <b>后进先出</b> 逆序释放。
 *
 * <p>用于避免监听器泄漏：每个模块 demo / 场景持有一个 registry,
 * 所有 {@link Subscription}(以及其它 AutoCloseable, 如文件流)注册进来,
 * demo 结束时一次性 {@code close()} 全部退订, 杜绝「订阅了但忘记 cancel」。
 *
 * <p>线程安全：内部同步, 允许回调线程与主线程并发注册。
 */
public final class SubscriptionRegistry implements AutoCloseable {

    private final Deque<AutoCloseable> resources = new ArrayDeque<AutoCloseable>();
    private boolean closed;

    /** 注册一个可关闭资源, 返回它本身以便链式使用。 */
    public synchronized <T extends AutoCloseable> T add(T resource) {
        if (closed) {
            // 已关闭仍注册：立即关闭, 避免泄漏。
            closeQuietly(resource);
            return resource;
        }
        resources.push(resource);
        return resource;
    }

    /**
     * 从登记处移除一个资源(不关闭它, 由调用方负责关闭)。
     *
     * <p>用途：当调用方已主动关闭某资源(如 {@code stopMonitors} 主动退订监听), 可调用本方法
     * 将其从 registry 移出, 避免 registry 长期持有已关闭资源的引用(内存冗余)。
     * 如果资源未在 registry 中则静默忽略(幂等)。
     *
     * <p>{@link ArrayDeque#remove(Object)} 依赖 {@code equals} 比较, 对象引用相同时正确匹配。
     * 时间复杂度 O(n), 适合每次 stopMonitors 批量移除 ~10 个条目的场景。
     */
    public synchronized void remove(AutoCloseable resource) {
        resources.remove(resource);
    }

    /** 当前登记的资源数量。 */
    public synchronized int size() {
        return resources.size();
    }

    /** 逆序释放全部资源。幂等。 */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        int n = resources.size();
        while (!resources.isEmpty()) {
            closeQuietly(resources.pop());
        }
        if (n > 0) {
            DemoLog.info("SubscriptionRegistry", "已释放 " + n + " 个订阅/资源");
        }
    }

    private static void closeQuietly(AutoCloseable c) {
        try {
            c.close();
        } catch (Throwable t) {
            DemoLog.warn("SubscriptionRegistry", "释放资源异常: " + Utils.describe(t));
        }
    }
}
