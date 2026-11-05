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

package com.dji.flighthub.mcp.tools.osd;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 将 SDK 回调负载（bean）反射转换为 JSON 友好的 Map / List / 基础类型。
 *
 * @author DJI FlightHub SDK Team
 */
final class OsdBeanMapper {

    static final int MAX_DEPTH = 6;
    private static final String SDK_PACKAGE_PREFIX = "com.dji.flighthubsdk.";
    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("([a-z0-9])([A-Z])");

    private OsdBeanMapper() {
    }

    /**
     * 转为可写入订阅缓冲区的事件 Map。非对象负载（数字、列表等）包装为 {"value": ...}。
     */
    static Map<String, Object> toEvent(Object payload) {
        Object mapped = map(payload, 0);
        if (mapped instanceof Map<?, ?> map) {
            Map<String, Object> event = new LinkedHashMap<>();
            map.forEach((k, v) -> event.put(String.valueOf(k), v));
            return event;
        }
        Map<String, Object> wrapped = new LinkedHashMap<>();
        wrapped.put("value", mapped);
        return wrapped;
    }

    static Object map(Object value, int depth) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Character || value instanceof CharSequence) {
            return value.toString();
        }
        if (value instanceof Enum<?> e) {
            return e.name();
        }
        if (depth >= MAX_DEPTH) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(String.valueOf(k), map(v, depth + 1)));
            return result;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            iterable.forEach(item -> result.add(map(item, depth + 1)));
            return result;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                result.add(map(Array.get(value, i), depth + 1));
            }
            return result;
        }
        if (value.getClass().getName().startsWith(SDK_PACKAGE_PREFIX)) {
            return mapBean(value, depth);
        }
        return value.toString();
    }

    private static Map<String, Object> mapBean(Object bean, int depth) {
        Map<String, Object> result = new LinkedHashMap<>();
        Method[] methods = bean.getClass().getMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        for (Method method : methods) {
            String key = propertyName(method);
            if (key == null) {
                continue;
            }
            try {
                result.put(key, map(method.invoke(bean), depth + 1));
            } catch (ReflectiveOperationException | RuntimeException e) {
                result.put(key, null);
            }
        }
        return result;
    }

    private static String propertyName(Method method) {
        if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0
                || method.getReturnType() == void.class || method.getDeclaringClass() == Object.class) {
            return null;
        }
        String name = method.getName();
        String property;
        if (name.startsWith("get") && name.length() > 3) {
            property = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            property = name.substring(2);
        } else {
            return null;
        }
        return CAMEL_BOUNDARY.matcher(property).replaceAll("$1_$2").toLowerCase();
    }
}
