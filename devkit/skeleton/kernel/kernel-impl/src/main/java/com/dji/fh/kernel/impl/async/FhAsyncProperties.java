/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.fh.kernel.impl.async;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Kernel 事件异步派发线程池的可调配置。
 *
 * <p>对应配置前缀 {@code fh.skeleton.async.*}。ISV 可在 application.yml / application.properties
 * 通过该前缀覆盖任意字段。
 *
 * <p>默认值面向中小规模 SDK + 中等吞吐事件场景；高频/低频场景请按需调优。
 *
 * @author DJI DevKit Team
 */
@ConfigurationProperties(prefix = "fh.skeleton.async")
public class FhAsyncProperties {

    /**
     * 核心线程数。默认 4。
     */
    private int coreSize = 4;

    /**
     * 最大线程数。默认 16。
     */
    private int maxSize = 16;

    /**
     * 任务队列容量。默认 200。
     */
    private int queueCapacity = 200;

    /**
     * 空闲线程 keepAlive 秒数。默认 60。
     */
    private int keepAliveSeconds = 60;

    /**
     * 拒绝策略。默认 caller-runs（提交线程兜底执行，不丢任务）。
     */
    private Rejection rejection = Rejection.CALLER_RUNS;

    /**
     * 拒绝策略枚举。
     */
    public enum Rejection {

        /**
         * 提交线程直接执行被拒任务，不丢。
         */
        CALLER_RUNS {
            @Override
            public RejectedExecutionHandler toPolicy() {
                return new ThreadPoolExecutor.CallerRunsPolicy();
            }
        },

        /**
         * 抛 RejectedExecutionException。
         */
        ABORT {
            @Override
            public RejectedExecutionHandler toPolicy() {
                return new ThreadPoolExecutor.AbortPolicy();
            }
        },

        /**
         * 静默丢弃被拒任务。
         */
        DISCARD {
            @Override
            public RejectedExecutionHandler toPolicy() {
                return new ThreadPoolExecutor.DiscardPolicy();
            }
        },

        /**
         * 丢弃队列最老任务后重试入队。
         */
        DISCARD_OLDEST {
            @Override
            public RejectedExecutionHandler toPolicy() {
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            }
        };

        /**
         * @return 对应的 JDK RejectedExecutionHandler 实例
         */
        public abstract RejectedExecutionHandler toPolicy();
    }

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public Rejection getRejection() {
        return rejection;
    }

    public void setRejection(Rejection rejection) {
        this.rejection = rejection;
    }
}
