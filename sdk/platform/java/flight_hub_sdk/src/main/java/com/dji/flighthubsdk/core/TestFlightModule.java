// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.CreateTestFlightTaskParams;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.GenerateTryFlightWaylineParams;
import com.dji.flighthubsdk.generated.ITestFlightModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 试飞模块接口，提供试飞航线生成与试飞任务创建。
 */
public class TestFlightModule {

    private class CallbackRefContainer {
        private Object callback_jni_ref;
        private Object callback_java_ref;

        public CallbackRefContainer(Object callback_jni_ref, Object callback_java_ref) {
            this.callback_jni_ref = callback_jni_ref;
            this.callback_java_ref = callback_java_ref;
        }

        public void release() {
            if (callback_jni_ref != null) {
                try {
                    callback_jni_ref.getClass().getMethod("delete").invoke(callback_jni_ref);
                } catch (Exception ignored) {
                }
                callback_jni_ref = null;
            }
        }
    }

    private final ITestFlightModule testFlightModule;
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> generateTryFlightWaylineCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createTestFlightTaskCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public TestFlightModule(ITestFlightModule testFlightModule) {
        this.testFlightModule = testFlightModule;
    }

    /**
     * 生成试飞航线
     * @param projectId 项目 ID
     * @param params 生成参数
     * @param callback 回调
     */
    public void generateTryFlightWayline(String projectId, GenerateTryFlightWaylineParams params, FlightHubCallback.GenerateTryFlightWaylineCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GenerateTryFlightWaylineCallback swigCallback = new com.dji.flighthubsdk.generated.GenerateTryFlightWaylineCallback() {
                @Override
                public void onGenerateTryFlightWayline(com.dji.flighthubsdk.generated.GenerateTryFlightWaylineResponse response, com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(response, FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("TestFlightModule", "generateTryFlightWayline callback error: " + e.getMessage(), e);
                    } finally {
                        generateTryFlightWaylineCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (generateTryFlightWaylineCallbackRef) {
                generateTryFlightWaylineCallbackRef.put(id, container);
                this.testFlightModule.generateTryFlightWaylineWrapper(projectId, params, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("TestFlightModule", "generateTryFlightWayline error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(null, new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 创建试飞任务
     * @param projectId 项目 ID
     * @param params 创建参数
     * @param callback 回调
     */
    public void createTestFlightTask(String projectId, CreateTestFlightTaskParams params, FlightHubCallback.CreateTestFlightTaskCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CreateTestFlightTaskCallback swigCallback = new com.dji.flighthubsdk.generated.CreateTestFlightTaskCallback() {
                @Override
                public void onCreateTestFlightTask(com.dji.flighthubsdk.generated.CreateTestFlightTaskResponse response, com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(response, FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("TestFlightModule", "createTestFlightTask callback error: " + e.getMessage(), e);
                    } finally {
                        createTestFlightTaskCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (createTestFlightTaskCallbackRef) {
                createTestFlightTaskCallbackRef.put(id, container);
                this.testFlightModule.createTestFlightTaskWrapper(projectId, params, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("TestFlightModule", "createTestFlightTask error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(null, new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }
}
