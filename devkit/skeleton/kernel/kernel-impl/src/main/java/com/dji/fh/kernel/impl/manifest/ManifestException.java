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

package com.dji.fh.kernel.impl.manifest;

/**
 * module.yaml 加载 / 校验失败异常。
 *
 * <p>{@link ModuleManifestLoader} 在解析失败、必填字段缺失、kernel 版本不兼容、
 * id 冲突等场景抛出。被外层包装为 {@link IllegalStateException} 中断 Spring 启动。
 *
 * @author DJI DevKit Team
 */
public class ManifestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ManifestException(String message) {
        super(message);
    }

    public ManifestException(String message, Throwable cause) {
        super(message, cause);
    }
}
