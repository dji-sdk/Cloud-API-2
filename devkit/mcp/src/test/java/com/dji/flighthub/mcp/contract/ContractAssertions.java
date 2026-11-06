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

package com.dji.flighthub.mcp.contract;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 契约断言工具：JSON-RPC 信封不变量与 MCP 工具结果信封断言，A 层与 C 层共用。
 *
 * @author DJI DevKit Team
 */
public final class ContractAssertions {

    /** JSON-RPC 协议版本字面量 */
    private static final String PROTOCOL_VERSION = "2.0";

    private static final String FIELD_JSONRPC = "jsonrpc";

    private static final String FIELD_ID = "id";

    private static final String FIELD_RESULT = "result";

    private static final String FIELD_ERROR = "error";

    private static final String FIELD_CODE = "code";

    private static final String FIELD_MESSAGE = "message";

    private static final String FIELD_CONTENT = "content";

    private static final String FIELD_IS_ERROR = "isError";

    private static final String FIELD_TEXT = "text";

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ContractAssertions() {
    }

    /**
     * 断言 JSON-RPC 信封不变量：jsonrpc 版本、id 回显、result/error 互斥、error 形状。
     *
     * @param response   响应
     * @param expectedId 期望的 id（解析错误时为 null）
     */
    public static void assertEnvelope(Map<String, Object> response, Object expectedId) {
        assertNotNull(response, "响应不能为空");
        assertEquals(PROTOCOL_VERSION, response.get(FIELD_JSONRPC),
                "jsonrpc 必须为 2.0，实际响应: " + response);
        assertEquals(expectedId, response.get(FIELD_ID),
                "响应 id 必须与请求一致，实际响应: " + response);
        boolean hasResult = response.containsKey(FIELD_RESULT);
        boolean hasError = response.containsKey(FIELD_ERROR);
        assertTrue(hasResult ^ hasError, "result 与 error 必须互斥且恰有其一，实际为: " + response);
        if (!hasError) {
            return;
        }
        Object error = response.get(FIELD_ERROR);
        assertTrue(error instanceof Map, "error 必须是对象，实际响应: " + response);
        Map<?, ?> errorMap = (Map<?, ?>) error;
        assertTrue(errorMap.get(FIELD_CODE) instanceof Integer,
                "error.code 必须是整数，实际响应: " + response);
        Object message = errorMap.get(FIELD_MESSAGE);
        assertTrue(message instanceof String text && !text.isBlank(),
                "error.message 必须是非空字符串，实际响应: " + response);
    }

    /**
     * 断言响应是错误响应且错误码匹配。
     *
     * @param response     响应
     * @param expectedCode 期望错误码
     */
    public static void assertErrorCode(Map<String, Object> response, int expectedCode) {
        assertEquals(expectedCode, errorCode(response),
                "错误码不匹配，实际响应: " + response);
    }

    /**
     * 取响应中的错误码。
     *
     * @param response 响应
     * @return 错误码
     */
    public static int errorCode(Map<String, Object> response) {
        Object error = response.get(FIELD_ERROR);
        assertTrue(error instanceof Map, "响应中没有 error 对象: " + response);
        Object code = ((Map<?, ?>) error).get(FIELD_CODE);
        assertTrue(code instanceof Integer, "error.code 必须是整数: " + response);
        return (Integer) code;
    }

    /**
     * 取成功响应的 result 对象。
     *
     * @param response 响应
     * @return result
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> resultOf(Map<String, Object> response) {
        Object result = response.get(FIELD_RESULT);
        assertTrue(result instanceof Map, "result 必须是对象: " + response);
        return (Map<String, Object>) result;
    }

    /**
     * 断言工具结果信封形状：{@code content} 是非空数组且首项为 text 块，{@code isError} 是布尔值。
     *
     * @param response 响应
     */
    public static void assertToolEnvelope(Map<String, Object> response) {
        Map<String, Object> result = resultOf(response);
        firstTextBlock(result);
        assertTrue(result.get(FIELD_IS_ERROR) instanceof Boolean, "result.isError 必须是布尔值");
    }

    /**
     * 取工具结果信封里的 isError。
     *
     * @param response 响应
     * @return isError
     */
    public static boolean isToolError(Map<String, Object> response) {
        Object isError = resultOf(response).get(FIELD_IS_ERROR);
        assertTrue(isError instanceof Boolean, "result.isError 必须是布尔值: " + response);
        return (Boolean) isError;
    }

    /**
     * 解析 content[0].text 里的业务负载 JSON。
     *
     * @param response 响应
     * @return 业务负载
     */
    public static Map<String, Object> toolPayload(Map<String, Object> response) {
        String text = firstTextBlock(resultOf(response));
        try {
            return OBJECT_MAPPER.readValue(text, MAP_TYPE);
        } catch (Exception e) {
            throw new AssertionError("content[0].text 不是合法 JSON: " + text, e);
        }
    }

    /**
     * 校验工具结果信封的 content 结构并取出 content[0].text。
     *
     * @param result JSON-RPC result 对象
     * @return content[0].text 文本
     */
    private static String firstTextBlock(Map<String, Object> result) {
        Object content = result.get(FIELD_CONTENT);
        assertTrue(content instanceof List<?> list && !list.isEmpty(), "result.content 必须是非空数组");
        Object first = ((List<?>) content).get(0);
        assertTrue(first instanceof Map, "content[0] 必须是对象");
        Object text = ((Map<?, ?>) first).get(FIELD_TEXT);
        assertTrue(text instanceof String, "content[0].text 必须是字符串");
        return (String) text;
    }
}
