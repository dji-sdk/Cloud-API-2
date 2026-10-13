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

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MediaProxySslContextFactory} 单元测试。
 *
 * <p>用两份本地生成的自签名测试证书（非真实部署的证书）验证：未配置时不介入默认信任链、
 * 配置后能信任指定证书、但不会变成信任任意证书（trust-all）。
 *
 * @author DJI DevKit Team
 */
class MediaProxySslContextFactoryTest {

    private static final String TRUSTED_CERT_RESOURCE = "/certs/trusted-self-signed.pem";
    private static final String UNTRUSTED_CERT_RESOURCE = "/certs/untrusted-self-signed.pem";
    private static final String AUTH_TYPE = "RSA";

    @Test
    void createSslContext_shouldReturnNull_whenPathIsNull() {
        assertThat(MediaProxySslContextFactory.createSslContext(null)).isNull();
    }

    @Test
    void createSslContext_shouldReturnNull_whenPathIsBlank() {
        assertThat(MediaProxySslContextFactory.createSslContext("  ")).isNull();
    }

    @Test
    void createSslContext_shouldReturnWorkingContext_whenPathConfigured() throws Exception {
        SSLContext context = MediaProxySslContextFactory.createSslContext(resourcePath(TRUSTED_CERT_RESOURCE));

        assertThat(context).isNotNull();
        assertThat(context.getProtocol()).isEqualTo("TLS");
    }

    @Test
    void createTrustManager_shouldReturnNull_whenPathBlank() {
        assertThat(MediaProxySslContextFactory.createTrustManager("")).isNull();
    }

    @Test
    void createTrustManager_shouldTrustConfiguredCertificate() throws Exception {
        X509TrustManager trustManager = MediaProxySslContextFactory.createTrustManager(
                resourcePath(TRUSTED_CERT_RESOURCE));

        trustManager.checkServerTrusted(loadChain(TRUSTED_CERT_RESOURCE), AUTH_TYPE);
    }

    @Test
    void createTrustManager_shouldReject_whenCertificateNotConfiguredOrPublic() throws Exception {
        X509TrustManager trustManager = MediaProxySslContextFactory.createTrustManager(
                resourcePath(TRUSTED_CERT_RESOURCE));

        assertThatThrownBy(() -> trustManager.checkServerTrusted(loadChain(UNTRUSTED_CERT_RESOURCE), AUTH_TYPE))
                .isInstanceOf(CertificateException.class);
    }

    @Test
    void createSslContext_shouldThrow_whenCertFileMissing() {
        assertThatThrownBy(() -> MediaProxySslContextFactory.createSslContext("/no/such/file.pem"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createSslContext_shouldThrow_whenCertFileHasNoCertificates() throws Exception {
        Path empty = Files.createTempFile("fh-media-proxy-empty-cert", ".pem");
        Files.writeString(empty, "not a certificate");

        assertThatThrownBy(() -> MediaProxySslContextFactory.createSslContext(empty.toString()))
                .isInstanceOf(IllegalStateException.class);
    }

    private static String resourcePath(final String resource) throws URISyntaxException {
        return Path.of(MediaProxySslContextFactoryTest.class.getResource(resource).toURI()).toString();
    }

    private static X509Certificate[] loadChain(final String resource) throws IOException, CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (InputStream in = MediaProxySslContextFactoryTest.class.getResourceAsStream(resource)) {
            Certificate cert = factory.generateCertificate(in);
            return new X509Certificate[]{(X509Certificate) cert};
        }
    }
}
