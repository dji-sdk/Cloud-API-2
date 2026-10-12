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

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link CompositeX509TrustManager} 单元测试。
 *
 * @author DJI DevKit Team
 */
class CompositeX509TrustManagerTest {

    private static final X509Certificate[] EMPTY_CHAIN = new X509Certificate[0];
    private static final String AUTH_TYPE = "RSA";

    @Test
    void checkServerTrusted_shouldPass_whenFirstDelegateTrusts() throws Exception {
        X509TrustManager first = mock(X509TrustManager.class);
        X509TrustManager second = mock(X509TrustManager.class);
        CompositeX509TrustManager composite = new CompositeX509TrustManager(first, second);

        composite.checkServerTrusted(EMPTY_CHAIN, AUTH_TYPE);

        verifyNoInteractions(second);
    }

    @Test
    void checkServerTrusted_shouldFallBackToSecondDelegate_whenFirstRejects() throws Exception {
        X509TrustManager first = mock(X509TrustManager.class);
        X509TrustManager second = mock(X509TrustManager.class);
        doThrow(new CertificateException("not trusted by first"))
                .when(first).checkServerTrusted(any(), any());

        CompositeX509TrustManager composite = new CompositeX509TrustManager(first, second);

        composite.checkServerTrusted(EMPTY_CHAIN, AUTH_TYPE);
    }

    @Test
    void checkServerTrusted_shouldThrow_whenAllDelegatesReject() throws Exception {
        X509TrustManager first = mock(X509TrustManager.class);
        X509TrustManager second = mock(X509TrustManager.class);
        doThrow(new CertificateException("not trusted by first"))
                .when(first).checkServerTrusted(any(), any());
        doThrow(new CertificateException("not trusted by second"))
                .when(second).checkServerTrusted(any(), any());

        CompositeX509TrustManager composite = new CompositeX509TrustManager(first, second);

        assertThatThrownBy(() -> composite.checkServerTrusted(EMPTY_CHAIN, AUTH_TYPE))
                .isInstanceOf(CertificateException.class)
                .hasMessage("not trusted by second");
    }

    @Test
    void getAcceptedIssuers_shouldMergeAllDelegates() {
        X509Certificate issuerA = mock(X509Certificate.class);
        X509Certificate issuerB = mock(X509Certificate.class);
        X509TrustManager first = mock(X509TrustManager.class);
        X509TrustManager second = mock(X509TrustManager.class);
        when(first.getAcceptedIssuers()).thenReturn(new X509Certificate[]{issuerA});
        when(second.getAcceptedIssuers()).thenReturn(new X509Certificate[]{issuerB});

        CompositeX509TrustManager composite = new CompositeX509TrustManager(first, second);

        assertThat(composite.getAcceptedIssuers()).containsExactly(issuerA, issuerB);
    }
}
