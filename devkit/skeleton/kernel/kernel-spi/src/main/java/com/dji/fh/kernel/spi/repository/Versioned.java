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

package com.dji.fh.kernel.spi.repository;

/**
 * 实体可选实现该接口以启用乐观锁。
 * Repository 实现层识别该接口并以 version 字段防并发冲突。
 *
 * @author DJI DevKit Team
 */
public interface Versioned {

    /**
     * @return 当前版本号；新实体返回 0
     */
    long version();
}
