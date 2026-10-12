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

package com.dji.flighthub.mcp.simulator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * pf-device-simulator-service HTTP API 客户端。
 *
 * <p>懒登录 + session cookie 缓存 + 401 自动重登重试一次。兼容模拟器
 * {@code auth.enabled=false} 场景：登录失败时不阻断，后续业务请求无 cookie 直接尝试
 * （模拟器关闭鉴权时中间件本身不校验 cookie）。不修改模拟器代码。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class SimulatorClient {

    private static final Logger log = LoggerFactory.getLogger(SimulatorClient.class);

    private static final String LOGIN_PATH = "/simulator/api/v1/auth/login";
    private static final String COOKIE_HEADER = "Cookie";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final int STATUS_UNREACHABLE = 0;
    private static final int STATUS_UNAUTHORIZED = 401;
    private static final int HTTP_SUCCESS_MIN = 200;
    private static final int HTTP_SUCCESS_MAX_EXCLUSIVE = 300;

    private final SimulatorProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AtomicBoolean loginAttempted = new AtomicBoolean(false);
    private final AtomicReference<String> sessionCookie = new AtomicReference<>();
    private final Object loginLock = new Object();

    public SimulatorClient(SimulatorProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    /**
     * 发起 GET 请求。
     *
     * @param path 请求路径（含 leading slash）
     * @return 解析后的 JSON 响应体
     * @throws SimulatorApiException 请求失败（连接失败/4xx/5xx）
     */
    public Map<String, Object> get(String path) throws SimulatorApiException {
        return request("GET", path, null);
    }

    /**
     * 发起 POST 请求。
     *
     * @param path 请求路径（含 leading slash）
     * @param body 请求体，序列化为 JSON；可为 null（空 body）
     * @return 解析后的 JSON 响应体
     * @throws SimulatorApiException 请求失败（连接失败/4xx/5xx）
     */
    public Map<String, Object> post(String path, Object body) throws SimulatorApiException {
        return request("POST", path, body);
    }

    /**
     * 发起 PATCH 请求。
     *
     * @param path 请求路径（含 leading slash）
     * @param body 请求体，序列化为 JSON
     * @return 解析后的 JSON 响应体
     * @throws SimulatorApiException 请求失败（连接失败/4xx/5xx）
     */
    public Map<String, Object> patch(String path, Object body) throws SimulatorApiException {
        return request("PATCH", path, body);
    }

    /**
     * 发起 DELETE 请求。
     *
     * @param path 请求路径（含 leading slash）
     * @return 解析后的 JSON 响应体
     * @throws SimulatorApiException 请求失败（连接失败/4xx/5xx）
     */
    public Map<String, Object> delete(String path) throws SimulatorApiException {
        return request("DELETE", path, null);
    }

    private Map<String, Object> request(String method, String path, Object body) throws SimulatorApiException {
        ensureLoggedIn();
        SimpleResponse response = exchange(method, path, body);
        if (response.status() == STATUS_UNAUTHORIZED) {
            synchronized (loginLock) {
                attemptLogin();
            }
            response = exchange(method, path, body);
        }
        return parseResponse(response, path);
    }

    private void ensureLoggedIn() {
        // 双重检查锁定：外层无锁快速路径，登录完成后后续请求可直接跳过同步块。
        // 关键点：loginAttempted 必须在 attemptLogin() 完整执行结束后才置位，
        // 否则并发线程会在登录网络往返尚未完成时就读到"已尝试登录"，从而绕过锁直接发起业务请求，
        // 导致拿着空 cookie 触发一次多余的 401 重试 + 重复登录。
        if (!loginAttempted.get()) {
            synchronized (loginLock) {
                if (!loginAttempted.get()) {
                    attemptLogin();
                    loginAttempted.set(true);
                }
            }
        }
    }

    private void attemptLogin() {
        try {
            Map<String, Object> loginBody = Map.of(
                    "username", properties.getUsername(),
                    "password", properties.getPassword());
            SimpleResponse response = exchange("POST", LOGIN_PATH, loginBody);
            if (isSuccess(response.status())) {
                String cookie = extractSessionCookie(response.setCookieHeaders());
                sessionCookie.set(cookie);
            } else {
                log.warn("Simulator login failed with status {}, proceeding without session cookie "
                        + "(assuming auth.enabled=false)", response.status());
                sessionCookie.set(null);
            }
        } catch (SimulatorApiException e) {
            log.warn("Simulator login attempt raised an exception, proceeding without session cookie: {}",
                    e.getMessage());
            sessionCookie.set(null);
        }
    }

    private String extractSessionCookie(List<String> setCookieHeaders) {
        if (setCookieHeaders == null || setCookieHeaders.isEmpty()) {
            return null;
        }
        return setCookieHeaders.get(0).split(";", 2)[0];
    }

    /**
     * 判断 HTTP 状态码是否落在成功区间 [200, 300)。
     *
     * @param status HTTP 状态码
     * @return true 表示成功
     */
    private boolean isSuccess(int status) {
        return status >= HTTP_SUCCESS_MIN && status < HTTP_SUCCESS_MAX_EXCLUSIVE;
    }

    private SimpleResponse exchange(String method, String path, Object body) throws SimulatorApiException {
        try {
            RestClient.RequestBodySpec spec = restClient
                    .method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(path);
            String cookie = sessionCookie.get();
            if (cookie != null) {
                spec.header(COOKIE_HEADER, cookie);
            }
            if (body != null) {
                spec.contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(body);
            }
            return spec.exchange((request, response) -> {
                String responseBody = new String(response.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                List<String> setCookie = response.getHeaders().get(SET_COOKIE_HEADER);
                return new SimpleResponse(response.getStatusCode().value(), responseBody, setCookie);
            });
        } catch (Exception e) {
            log.warn("Simulator request failed - path={}, reason={}", path, e.getMessage());
            throw new SimulatorApiException(STATUS_UNREACHABLE, path + ": connection failed - " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parseResponse(SimpleResponse response, String path) throws SimulatorApiException {
        if (!isSuccess(response.status())) {
            throw new SimulatorApiException(response.status(), path + ": " + extractErrorMessage(response.body()));
        }
        if (response.body() == null || response.body().isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new SimulatorApiException(response.status(), path + ": failed to parse response - " + e.getMessage());
        }
    }

    private String extractErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "(empty response body)";
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {
            });
            Object error = parsed.get("error");
            if (error != null && !error.toString().isBlank()) {
                return error.toString();
            }
        } catch (IOException ignored) {
            // 响应体不是 JSON，直接回退到原始文本
        }
        return body;
    }

    private record SimpleResponse(int status, String body, List<String> setCookieHeaders) {
    }
}
