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

package com.dji.fh.kernel.impl.repository;

import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;

/**
 * H2 嵌入式 DataSource 的工厂方法集。
 *
 * <p>v0.1 默认走文件模式，存放路径 {@code ./data/fh-skeleton}。
 * Mock / 集成测试场景可用 {@link #embeddedInMemory(String)} 拿到 in-memory 实例。
 *
 * <p><b>注意</b>：容器化部署需 volume mount 持久化 {@code ./data} 目录（Plan A4 处理）。
 *
 * @author DJI DevKit Team
 */
public final class H2DataSourceFactory {

    private static final String FILE_URL_TEMPLATE = "jdbc:h2:file:%s;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
    private static final String MEMORY_URL_TEMPLATE = "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1";
    private static final String H2_DRIVER = "org.h2.Driver";

    private H2DataSourceFactory() {
    }

    /**
     * 创建文件模式 H2 DataSource。多进程共享通过 AUTO_SERVER=TRUE 自动开 TCP server。
     *
     * @param filePath 数据文件路径（相对或绝对，不含扩展名）
     * @return 配置好的 DataSource
     */
    public static DataSource embeddedFileBased(String filePath) {
        return DataSourceBuilder.create()
                .driverClassName(H2_DRIVER)
                .url(String.format(FILE_URL_TEMPLATE, filePath))
                .username("sa")
                .password("")
                .build();
    }

    /**
     * 创建内存模式 H2 DataSource。进程内独占；DB_CLOSE_DELAY=-1 防止最后一个连接关闭即清库。
     *
     * @param dbName 内存库名（同名共享，不同名隔离）
     * @return 配置好的 DataSource
     */
    public static DataSource embeddedInMemory(String dbName) {
        return DataSourceBuilder.create()
                .driverClassName(H2_DRIVER)
                .url(String.format(MEMORY_URL_TEMPLATE, dbName))
                .username("sa")
                .password("")
                .build();
    }
}
