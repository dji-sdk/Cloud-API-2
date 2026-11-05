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

package com.dji.fh.feature.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * UserProperties 配置校验单元测试。
 *
 * @author DJI DevKit Team
 */
class UserPropertiesTest {

    /** 合法的 32 字节测试密钥。 */
    private static final String VALID_SECRET = "0123456789abcdef0123456789abcdef";

    private static UserProperties.Jwt jwt(String secret) {
        return new UserProperties.Jwt(secret, null);
    }

    @Nested
    class JwtSecret {

        @Test
        void jwt_shouldFail_whenSecretIsNull() {
            assertThatThrownBy(() -> jwt(null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("未配置");
        }

        @Test
        void jwt_shouldFail_whenSecretIsBlank() {
            assertThatThrownBy(() -> jwt("   "))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("未配置");
        }

        /**
         * 回归测试：compose 未透传 FH_JWT_SECRET 时，Spring 会把
         * application.yml 中的 "${FH_JWT_SECRET}" 当作字面量绑定，错误信息必须
         * 直接指向"环境变量没进容器"，而不是只报长度不足。
         */
        @Test
        void jwt_shouldExplainUnresolvedPlaceholder_whenLiteralPlaceholderBound() {
            assertThatThrownBy(() -> jwt("${FH_JWT_SECRET}"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("未解析的占位符")
                    .hasMessageContaining("environment")
                    .hasMessageContaining("${FH_JWT_SECRET}");
        }

        @Test
        void jwt_shouldFail_whenSecretTooShort() {
            assertThatThrownBy(() -> jwt("too-short-secret"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("长度不足")
                    .hasMessageContaining("16 字节");
        }

        @Test
        void jwt_shouldFail_whenSecretIsKnownWeakPlaceholder() {
            assertThatThrownBy(() -> jwt("change-me-in-production-must-be-32-chars!!"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("公知弱占位值");
        }

        @Test
        void jwt_shouldFail_whenSecretIsTemplatePlaceholder() {
            assertThatThrownBy(() -> jwt("CHANGE_ME_JWT_SECRET_0123456789abcdef"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("部署模板中的占位值");
        }

        /** deploy.env.example 的尖括号占位值长度超过 32 字节，不能只靠长度校验拦截。 */
        @Test
        void jwt_shouldFail_whenSecretIsAngleBracketPlaceholder() {
            assertThatThrownBy(() -> jwt("<replace-with-strong-random-key>"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("部署模板中的占位值");
        }

        @Test
        void jwt_shouldAccept_whenSecretIsValidLength() {
            assertThatCode(() -> jwt(VALID_SECRET)).doesNotThrowAnyException();
        }

        @Test
        void jwt_shouldDefaultExpiryTo24Hours_whenNull() {
            assertThat(jwt(VALID_SECRET).expiry()).isEqualTo(Duration.ofHours(24));
        }

        @Test
        void jwt_shouldKeepExpiry_whenProvided() {
            UserProperties.Jwt jwt = new UserProperties.Jwt(VALID_SECRET, Duration.ofHours(2));

            assertThat(jwt.expiry()).isEqualTo(Duration.ofHours(2));
        }
    }

    @Test
    void constructor_shouldFail_whenJwtMissing() {
        assertThatThrownBy(() -> new UserProperties(null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fh.user.jwt 未配置");
    }

    @Test
    void constructor_shouldDefaultRegistrationDisabled_whenMissing() {
        UserProperties props = new UserProperties(jwt(VALID_SECRET), null);

        assertThat(props.registration().enabled()).isFalse();
        assertThat(props.registration().defaultRole()).isEqualTo("user");
    }
}
