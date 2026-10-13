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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SimulatorClient} 单元测试：用 JDK 自带 HttpServer 起一个假的模拟器端点。
 *
 * @author DJI FlightHub SDK Team
 */
class SimulatorClientTest {

    private static final String LOGIN_PATH = "/simulator/api/v1/auth/login";
    private static final String BUSINESS_PATH = "/simulator/api/v1/devices";
    private static final String SESSION_COOKIE = "sim_session=abc123; Path=/; HttpOnly";

    private HttpServer server;
    private int port;
    private AtomicInteger loginCallCount;
    private AtomicInteger businessCallCount;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        loginCallCount = new AtomicInteger(0);
        businessCallCount = new AtomicInteger(0);
        server.setExecutor(null);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private SimulatorClient buildClient() {
        SimulatorProperties properties = new SimulatorProperties();
        properties.setBaseUrl("http://localhost:" + port);
        properties.setUsername("admin");
        properties.setPassword("a-strong-test-secret");
        return new SimulatorClient(properties, new ObjectMapper());
    }

    private void writeJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(bytes);
        }
    }

    private HttpHandler successfulLoginHandler() {
        return exchange -> {
            loginCallCount.incrementAndGet();
            exchange.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE);
            writeJson(exchange, 200, "{\"ok\":true,\"username\":\"admin\",\"expires_at\":0}");
        };
    }

    private HttpHandler failingLoginHandler() {
        return exchange -> {
            loginCallCount.incrementAndGet();
            writeJson(exchange, 401, "{\"ok\":false,\"error\":\"auth disabled\"}");
        };
    }

    private HttpHandler businessOkHandler() {
        return exchange -> {
            businessCallCount.incrementAndGet();
            writeJson(exchange, 200, "{\"items\":[]}");
        };
    }

    @Test
    void get_shouldLoginFirst_thenCallBusinessEndpoint() throws Exception {
        server.createContext(LOGIN_PATH, successfulLoginHandler());
        server.createContext(BUSINESS_PATH, exchange -> {
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            assertTrue(cookie != null && cookie.contains("sim_session=abc123"));
            businessOkHandler().handle(exchange);
        });
        server.start();
        SimulatorClient client = buildClient();

        Map<String, Object> result = client.get(BUSINESS_PATH);

        assertEquals(1, loginCallCount.get());
        assertEquals(1, businessCallCount.get());
        assertTrue(result.containsKey("items"));
    }

    @Test
    void get_shouldReuseCookie_onSecondCall() throws Exception {
        server.createContext(LOGIN_PATH, successfulLoginHandler());
        server.createContext(BUSINESS_PATH, businessOkHandler());
        server.start();
        SimulatorClient client = buildClient();

        client.get(BUSINESS_PATH);
        client.get(BUSINESS_PATH);

        assertEquals(1, loginCallCount.get());
        assertEquals(2, businessCallCount.get());
    }

    @Test
    void get_shouldRelogin_andRetryOnce_when401() throws Exception {
        server.createContext(LOGIN_PATH, successfulLoginHandler());
        server.createContext(BUSINESS_PATH, exchange -> {
            int call = businessCallCount.incrementAndGet();
            if (call == 1) {
                writeJson(exchange, 401, "{\"error\":\"session expired\"}");
            } else {
                writeJson(exchange, 200, "{\"items\":[]}");
            }
        });
        server.start();
        SimulatorClient client = buildClient();

        Map<String, Object> result = client.get(BUSINESS_PATH);

        assertEquals(2, loginCallCount.get());
        assertEquals(2, businessCallCount.get());
        assertTrue(result.containsKey("items"));
    }

    @Test
    void get_shouldToleratesAuthDisabled_whenLoginFails() throws Exception {
        server.createContext(LOGIN_PATH, failingLoginHandler());
        server.createContext(BUSINESS_PATH, exchange -> {
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            assertTrue(cookie == null);
            businessOkHandler().handle(exchange);
        });
        server.start();
        SimulatorClient client = buildClient();

        Map<String, Object> result = client.get(BUSINESS_PATH);

        assertEquals(1, loginCallCount.get());
        assertEquals(1, businessCallCount.get());
        assertTrue(result.containsKey("items"));
    }

    @Test
    void get_shouldThrowSimulatorApiException_on4xx() throws Exception {
        server.createContext(LOGIN_PATH, successfulLoginHandler());
        server.createContext(BUSINESS_PATH, exchange -> writeJson(exchange, 404, "{\"error\":\"device not found\"}"));
        server.start();
        SimulatorClient client = buildClient();

        SimulatorApiException exception = assertThrows(SimulatorApiException.class, () -> client.get(BUSINESS_PATH));

        assertEquals(404, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains(BUSINESS_PATH));
        assertTrue(exception.getMessage().contains("device not found"));
    }

    @Test
    void get_shouldThrowSimulatorApiException_on5xx() throws Exception {
        server.createContext(LOGIN_PATH, successfulLoginHandler());
        server.createContext(BUSINESS_PATH, exchange -> writeJson(exchange, 500, "{\"error\":\"internal error\"}"));
        server.start();
        SimulatorClient client = buildClient();

        SimulatorApiException exception = assertThrows(SimulatorApiException.class, () -> client.get(BUSINESS_PATH));

        assertEquals(500, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains(BUSINESS_PATH));
        assertTrue(exception.getMessage().contains("internal error"));
    }

    @Test
    void get_shouldThrowSimulatorApiException_whenServerUnreachable() throws Exception {
        server.createContext(LOGIN_PATH, successfulLoginHandler());
        server.start();
        int deadPort = server.getAddress().getPort();
        server.stop(0);

        SimulatorProperties properties = new SimulatorProperties();
        properties.setBaseUrl("http://localhost:" + deadPort);
        SimulatorClient client = new SimulatorClient(properties, new ObjectMapper());

        SimulatorApiException exception = assertThrows(SimulatorApiException.class, () -> client.get(BUSINESS_PATH));

        assertEquals(0, exception.getHttpStatus());
    }

    @Test
    void get_shouldLoginExactlyOnce_whenTwoRequestsRaceDuringLogin() throws Exception {
        CountDownLatch releaseLogin = new CountDownLatch(1);
        server.createContext(LOGIN_PATH, exchange -> {
            loginCallCount.incrementAndGet();
            try {
                // 模拟登录网络往返的耗时：在测试主线程主动放行前一直阻塞，
                // 从而拉宽并发窗口，确保第二个线程的业务请求真正与登录过程重叠。
                releaseLogin.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE);
            writeJson(exchange, 200, "{\"ok\":true,\"username\":\"admin\",\"expires_at\":0}");
        });
        server.createContext(BUSINESS_PATH, exchange -> {
            businessCallCount.incrementAndGet();
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            if (cookie != null && cookie.contains("sim_session=abc123")) {
                writeJson(exchange, 200, "{\"items\":[]}");
            } else {
                writeJson(exchange, 401, "{\"error\":\"no session\"}");
            }
        });
        server.start();
        SimulatorClient client = buildClient();

        CyclicBarrier startBarrier = new CyclicBarrier(2);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
        Runnable callBusinessEndpoint = () -> {
            try {
                startBarrier.await(5, TimeUnit.SECONDS);
                client.get(BUSINESS_PATH);
            } catch (Exception e) {
                errors.add(e);
            }
        };
        Thread threadA = new Thread(callBusinessEndpoint, "sim-client-race-a");
        Thread threadB = new Thread(callBusinessEndpoint, "sim-client-race-b");
        threadA.start();
        threadB.start();

        // 给两个线程留出时间越过 barrier 并真正进入 ensureLoggedIn()/登录请求，
        // 确认二者都已在竞争登录锁之后，再放行登录响应。
        Thread.sleep(200);
        releaseLogin.countDown();

        threadA.join(5000);
        threadB.join(5000);

        assertTrue(errors.isEmpty(), "并发请求不应抛出异常: " + errors);
        assertEquals(1, loginCallCount.get(), "无论多少线程并发触发登录，登录请求都只应发生一次");
        assertEquals(2, businessCallCount.get());
    }
}
