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

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * 组合信任管理器 —— 依次尝试多个 {@link X509TrustManager}，任一校验通过即视为可信。
 *
 * <p>用于在不替换 JDK 默认信任链的前提下叠加信任某个私有化部署的自签名证书：默认信任管理器
 * 校验失败时继续尝试自定义信任管理器，而不是直接拒绝；两者都失败才真正拒绝。不是 trust-all。
 *
 * @author DJI DevKit Team
 */
final class CompositeX509TrustManager implements X509TrustManager {

    private final X509TrustManager[] delegates;

    /**
     * 构造组合信任管理器。
     *
     * @param delegates 按顺序尝试的信任管理器
     */
    CompositeX509TrustManager(final X509TrustManager... delegates) {
        this.delegates = delegates;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        checkTrusted(tm -> tm.checkClientTrusted(chain, authType));
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        checkTrusted(tm -> tm.checkServerTrusted(chain, authType));
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> all = new ArrayList<>();
        for (X509TrustManager delegate : delegates) {
            all.addAll(List.of(delegate.getAcceptedIssuers()));
        }
        return all.toArray(new X509Certificate[0]);
    }

    /**
     * 依次尝试每个委托信任管理器，任一通过即视为可信；全部失败则抛出最后一次失败原因。
     *
     * @param check 具体校验动作（client 或 server）
     */
    private void checkTrusted(final TrustCheck check) throws CertificateException {
        CertificateException lastFailure = null;
        for (X509TrustManager delegate : delegates) {
            try {
                check.check(delegate);
                return;
            } catch (CertificateException e) {
                lastFailure = e;
            }
        }
        throw lastFailure == null
                ? new CertificateException("没有配置任何信任管理器")
                : lastFailure;
    }

    /** 校验动作函数接口，统一 client/server 两种校验的重试逻辑。 */
    private interface TrustCheck {
        void check(X509TrustManager trustManager) throws CertificateException;
    }
}
