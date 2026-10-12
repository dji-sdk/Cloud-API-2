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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

/**
 * 媒体代理上游 TLS 信任构造器。
 *
 * <p>私有化部署的对象存储常用自签名证书（非公共 CA 签发），JVM 默认信任库不认得它，
 * {@code MediaProxyController} 回源时会在证书校验阶段失败
 * （{@code SSLHandshakeException: PKIX path building failed}）。ISV 可通过
 * {@code fh.skeleton.feature.media.proxy-trusted-cert-path} 配置该证书文件路径，
 * 本类据此构造一个在 JDK 默认信任链之外<b>追加</b>信任该证书的 {@link SSLContext}
 * —— 不是 trust-all，默认信任链对公共 CA 证书的校验不受影响。
 *
 * @author DJI DevKit Team
 */
final class MediaProxySslContextFactory {

    private static final String X509_CERT_TYPE = "X.509";
    private static final String TLS_PROTOCOL = "TLS";
    private static final String TRUST_ALIAS_PREFIX = "fh-media-proxy-trusted-";

    private MediaProxySslContextFactory() {
    }

    /**
     * 构造信任 JDK 默认 CA + 配置证书的 SSLContext。
     *
     * @param trustedCertPath 受信证书文件路径（PEM/DER），为空时返回 {@code null}
     * @return 自定义 SSLContext；未配置证书路径时返回 {@code null}，调用方应回退到默认 SSLContext
     * @throws IllegalStateException 证书文件不存在、不可读或内容无法解析出证书时
     */
    static SSLContext createSslContext(final String trustedCertPath) {
        X509TrustManager trustManager = createTrustManager(trustedCertPath);
        if (trustManager == null) {
            return null;
        }
        try {
            SSLContext context = SSLContext.getInstance(TLS_PROTOCOL);
            context.init(null, new TrustManager[]{trustManager}, null);
            return context;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("媒体代理 SSLContext 初始化失败：path=" + trustedCertPath, e);
        }
    }

    /**
     * 构造叠加信任（JDK 默认 CA + 配置证书）的组合 {@link X509TrustManager}。
     *
     * @param trustedCertPath 受信证书文件路径（PEM/DER），为空时返回 {@code null}
     * @return 组合信任管理器；未配置证书路径时返回 {@code null}
     */
    static X509TrustManager createTrustManager(final String trustedCertPath) {
        if (trustedCertPath == null || trustedCertPath.isBlank()) {
            return null;
        }
        try {
            X509TrustManager defaultTm = loadDefaultTrustManager();
            X509TrustManager customTm = loadCustomTrustManager(trustedCertPath);
            return new CompositeX509TrustManager(defaultTm, customTm);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("媒体代理受信证书加载失败：path=" + trustedCertPath, e);
        }
    }

    /**
     * 加载 JDK 默认信任管理器（即未额外配置时 HttpClient 本就会用到的那条信任链）。
     *
     * @return 默认 X509TrustManager
     */
    private static X509TrustManager loadDefaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        return findX509TrustManager(tmf);
    }

    /**
     * 从证书文件加载自定义信任管理器 —— 仅信任该文件中声明的证书。
     *
     * @param trustedCertPath 证书文件路径
     * @return 自定义 X509TrustManager
     */
    private static X509TrustManager loadCustomTrustManager(final String trustedCertPath)
            throws GeneralSecurityException, IOException {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);

        CertificateFactory certificateFactory = CertificateFactory.getInstance(X509_CERT_TYPE);
        try (InputStream in = Files.newInputStream(Path.of(trustedCertPath))) {
            Collection<? extends Certificate> certs = certificateFactory.generateCertificates(in);
            if (certs.isEmpty()) {
                throw new IllegalStateException("受信证书文件未解析出任何证书：path=" + trustedCertPath);
            }
            int index = 0;
            for (Certificate cert : certs) {
                trustStore.setCertificateEntry(TRUST_ALIAS_PREFIX + index, cert);
                index++;
            }
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return findX509TrustManager(tmf);
    }

    /**
     * 从 TrustManagerFactory 中取出 X509TrustManager。
     *
     * @param tmf 信任管理器工厂
     * @return X509TrustManager
     */
    private static X509TrustManager findX509TrustManager(final TrustManagerFactory tmf) {
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager x509TrustManager) {
                return x509TrustManager;
            }
        }
        throw new IllegalStateException("未找到 X509TrustManager");
    }
}
