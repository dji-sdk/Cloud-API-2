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

import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.DroneOsdModule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * OSD 可订阅字段目录，通过反射扫描 OSD 模块中成对的 listenXxx / cancelListenXxx 生成。
 *
 * <p>字段名 = 方法名去掉 listen 前缀与设备前缀后转 snake_case。
 * 无配对 cancel 或同表重名时直接抛出异常，避免订阅泄漏或字段被静默覆盖。
 *
 * @author DJI FlightHub SDK Team
 */
final class OsdFieldCatalog {

    private static final String LISTEN_PREFIX = "listen";
    private static final String CANCEL_PREFIX = "cancelListen";
    private static final Pattern CAMEL_BOUNDARY = Pattern.compile("([a-z0-9])([A-Z])");

    static final OsdFieldCatalog DRONE = build(DroneOsdModule.class, "Aircraft");
    static final OsdFieldCatalog DOCK = build(DockOsdModule.class, "Dock");

    /**
     * 一个可订阅的 OSD 字段。
     *
     * @param name        对外字段名（snake_case）
     * @param listen      SDK listen 方法
     * @param cancel      SDK cancelListen 方法
     * @param callbackType 回调接口类型
     * @param payloadType 回调负载类型（onResult 第一个参数）
     */
    record Field(String name, Method listen, Method cancel, Class<?> callbackType, Class<?> payloadType) {

        /**
         * 订阅该字段，返回 SDK listenId（负数表示 SDK 拒绝）。
         */
        int listen(Object module, Consumer<Object> onPayload) {
            Object callback = Proxy.newProxyInstance(callbackType.getClassLoader(),
                    new Class<?>[]{callbackType}, (proxy, method, args) -> {
                        if ("onResult".equals(method.getName()) && args != null && args.length > 0) {
                            onPayload.accept(args[0]);
                            return null;
                        }
                        return switch (method.getName()) {
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            case "toString" -> "OsdCallback[" + name + "]";
                            default -> null;
                        };
                    });
            return (int) invoke(listen, module, callback);
        }

        void cancel(Object module, int listenId) {
            invoke(cancel, module, listenId);
        }

        /**
         * 用负载类型的无参构造生成默认负载（mock 用）；无法构造时返回 null。
         */
        Object newDefaultPayload() {
            try {
                return payloadType.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException | RuntimeException e) {
                return null;
            }
        }

        private static Object invoke(Method method, Object target, Object arg) {
            try {
                return method.invoke(target, arg);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new IllegalStateException(cause);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private final Map<String, Field> fields;

    private OsdFieldCatalog(Map<String, Field> fields) {
        this.fields = Collections.unmodifiableMap(fields);
    }

    Field get(String name) {
        return fields.get(name);
    }

    List<String> names() {
        return new ArrayList<>(fields.keySet());
    }

    Map<String, Field> all() {
        return fields;
    }

    static OsdFieldCatalog forDeviceType(String deviceType) {
        return "dock".equals(deviceType) ? DOCK : DRONE;
    }

    static OsdFieldCatalog build(Class<?> moduleClass, String devicePrefix) {
        Map<String, Field> result = new LinkedHashMap<>();
        for (Method listen : moduleClass.getMethods()) {
            if (!isListenMethod(listen)) {
                continue;
            }
            String suffix = listen.getName().substring(LISTEN_PREFIX.length());
            Method cancel = findCancel(moduleClass, CANCEL_PREFIX + suffix);
            if (cancel == null) {
                throw new IllegalStateException("No paired " + CANCEL_PREFIX + suffix
                        + " found in " + moduleClass.getSimpleName());
            }
            Class<?> callbackType = listen.getParameterTypes()[0];
            Field field = new Field(toFieldName(suffix, devicePrefix), listen, cancel,
                    callbackType, resolvePayloadType(callbackType));
            if (result.putIfAbsent(field.name(), field) != null) {
                throw new IllegalStateException("Duplicate OSD field name '" + field.name()
                        + "' in " + moduleClass.getSimpleName());
            }
        }
        return new OsdFieldCatalog(sorted(result));
    }

    static String toFieldName(String listenSuffix, String devicePrefix) {
        String base = listenSuffix;
        if (base.startsWith(devicePrefix) && base.length() > devicePrefix.length()) {
            base = base.substring(devicePrefix.length());
        }
        return CAMEL_BOUNDARY.matcher(base).replaceAll("$1_$2").toLowerCase();
    }

    private static boolean isListenMethod(Method method) {
        return method.getName().startsWith(LISTEN_PREFIX)
                && !Modifier.isStatic(method.getModifiers())
                && method.getReturnType() == int.class
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0].isInterface();
    }

    private static Method findCancel(Class<?> moduleClass, String name) {
        try {
            return moduleClass.getMethod(name, int.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static Class<?> resolvePayloadType(Class<?> callbackType) {
        for (Method method : callbackType.getMethods()) {
            if ("onResult".equals(method.getName()) && method.getParameterCount() > 0
                    && !method.isBridge() && !method.isSynthetic()) {
                return method.getParameterTypes()[0];
            }
        }
        return Object.class;
    }

    private static Map<String, Field> sorted(Map<String, Field> unsorted) {
        Map<String, Field> sorted = new LinkedHashMap<>();
        unsorted.keySet().stream().sorted().forEach(name -> sorted.put(name, unsorted.get(name)));
        return sorted;
    }
}
