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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * 测试替身：记录每次调用（method/path/body），返回预置响应或抛出预置异常。
 *
 * @author DJI FlightHub SDK Team
 */
public class FakeSimulatorClient extends SimulatorClient {

    private final Deque<RecordedCall> calls = new ArrayDeque<>();
    private final Deque<Map<String, Object>> responses = new ArrayDeque<>();
    private SimulatorApiException nextException;

    public FakeSimulatorClient() {
        super(new SimulatorProperties(), new ObjectMapper());
    }

    public void enqueueResponse(Map<String, Object> response) {
        responses.addLast(response);
    }

    public void throwOnNextCall(SimulatorApiException exception) {
        this.nextException = exception;
    }

    public RecordedCall lastCall() {
        return calls.peekLast();
    }

    @Override
    public Map<String, Object> get(String path) throws SimulatorApiException {
        return record("GET", path, null);
    }

    @Override
    public Map<String, Object> post(String path, Object body) throws SimulatorApiException {
        return record("POST", path, body);
    }

    @Override
    public Map<String, Object> patch(String path, Object body) throws SimulatorApiException {
        return record("PATCH", path, body);
    }

    @Override
    public Map<String, Object> delete(String path) throws SimulatorApiException {
        return record("DELETE", path, null);
    }

    private Map<String, Object> record(String method, String path, Object body) throws SimulatorApiException {
        calls.addLast(new RecordedCall(method, path, body));
        if (nextException != null) {
            SimulatorApiException toThrow = nextException;
            nextException = null;
            throw toThrow;
        }
        Map<String, Object> response = responses.pollFirst();
        return response != null ? response : Map.of();
    }

    /**
     * 一次记录的调用。
     *
     * @param method 请求方法
     * @param path   请求路径
     * @param body   请求体
     */
    public record RecordedCall(String method, String path, Object body) {
    }
}
