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

package com.dji.fh.kernel.spi.event;

/**
 * Kernel 就绪事件。SDK initialize 完成后触发，feature module 在此后可放心使用 SDK 能力。
 *
 * @author DJI DevKit Team
 */
public final class KernelReadyEvent extends AbstractKernelEvent {

    /**
     * Kernel 运行模式。
     */
    public enum Mode {

        /**
         * 真 SDK 模式（依赖 Linux JNI）
         */
        SDK,

        /**
         * Mock 模式（开发态 / 测试态）
         */
        MOCK
    }

    private final Mode mode;
    private final String version;

    /**
     * @param source  触发来源
     * @param mode    运行模式
     * @param version Kernel 版本号
     */
    public KernelReadyEvent(String source, Mode mode, String version) {
        super(source);
        this.mode = mode;
        this.version = version;
    }

    /**
     * @return 运行模式（SDK / MOCK）
     */
    public Mode mode() {
        return mode;
    }

    /**
     * @return Kernel 版本号
     */
    public String version() {
        return version;
    }
}
