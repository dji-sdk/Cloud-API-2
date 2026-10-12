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

package com.dji.fh.feature.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 媒体管理 feature 配置。
 *
 * @param defaultPageSize      默认每页数量（默认 50）
 * @param maxPageSize          最大每页数量（默认 500）
 * @param enabled              是否启用本 feature（默认 true）
 * @param proxyTrustedCertPath 媒体代理回源时追加信任的证书文件路径（PEM/DER，可为空）。
 *                             私有化部署的对象存储常用自签名证书，JVM 默认信任库不认得，
 *                             配置此项后 {@code MediaProxyController} 会在 JDK 默认信任链之外
 *                             <b>追加</b>信任该证书，而不是整体关闭证书校验
 * @author DJI DevKit Team
 */
@ConfigurationProperties("fh.skeleton.feature.media")
public record MediaProperties(
        int defaultPageSize,
        int maxPageSize,
        boolean enabled,
        String proxyTrustedCertPath
) {
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 500;

    public MediaProperties {
        if (defaultPageSize <= 0) {
            defaultPageSize = DEFAULT_PAGE_SIZE;
        }
        if (maxPageSize <= 0) {
            maxPageSize = MAX_PAGE_SIZE;
        }
        if (proxyTrustedCertPath == null) {
            proxyTrustedCertPath = "";
        }
    }
}
