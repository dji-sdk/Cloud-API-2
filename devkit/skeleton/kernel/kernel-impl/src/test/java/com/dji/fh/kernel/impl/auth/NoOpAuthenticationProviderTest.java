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

package com.dji.fh.kernel.impl.auth;

import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NoOpAuthenticationProviderTest {

    @Test
    void authenticate_shouldReturnNoOpContext_whenAnyRequest() {
        NoOpAuthenticationProvider provider = new NoOpAuthenticationProvider();
        HttpServletRequest request = mock(HttpServletRequest.class);

        Optional<SecurityContext> result = provider.authenticate(request);

        assertThat(result).contains(NoOpSecurityContext.INSTANCE);
    }

    @Test
    void authenticate_shouldReturnNoOpContext_whenRequestIsNull() {
        NoOpAuthenticationProvider provider = new NoOpAuthenticationProvider();

        Optional<SecurityContext> result = provider.authenticate(null);

        assertThat(result).contains(NoOpSecurityContext.INSTANCE);
    }

    @Test
    void order_shouldBeMaxValue_whenDefault() {
        assertThat(new NoOpAuthenticationProvider().order())
                .isEqualTo(Integer.MAX_VALUE);
    }
}
