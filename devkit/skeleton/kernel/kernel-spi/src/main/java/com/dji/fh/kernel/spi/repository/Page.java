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

import java.util.List;

/**
 * 轻量分页结果包装，避免耦合 Spring Data Pageable。
 *
 * @param <T>           元素类型
 * @param content       本页内容（构造时做防御性不可变拷贝；调用方后续修改原始集合不影响 Page）
 * @param page          页号（0-based）
 * @param size          每页大小
 * @param totalElements 全部元素总数
 * @author DJI DevKit Team
 */
public record Page<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
    public Page {
        content = content == null ? List.of() : List.copyOf(content);
    }
}
