// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.bean.HistoryHmsDataParam;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.util.Logger;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;

/**
 * HMS（Health Management System）模块接口，提供历史告警数据查询功能。
 */
public class HMSModule {

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

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> queryHistoryHMSDataCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;
    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    private IHMSModule hmsModule;

    /**
     * 构造 HMS 健康管理模块
     * @param hmsModule 底层 HMS 模块接口
     */
    public HMSModule(IHMSModule hmsModule) {
        this.hmsModule = hmsModule;
    }

    /**
     * 查询历史 HMS 告警数据
     * @param params 查询参数（设备 SN 列表、时间范围、分页）
     * @param callback 结果回调，成功时返回告警数据列表和分页信息
     * @implNote 链路: HTTP | POST /device/api/v1/manager/hms
     */
    public void queryHistoryHMSData(HistoryHmsDataParam params, FlightHubCallback.QueryHistoryHMSDataCallback callback) {
        try {
            int id = generateActionCallbackId();
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.QueryHistoryHMSDataCallback swigCallback = new com.dji.flighthubsdk.generated.QueryHistoryHMSDataCallback() {
                @Override
                public void onQueryHistoryHMSData(com.dji.flighthubsdk.generated.FlightHubError err, HistoryHmsDataResult result) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toHistoryHmsDataResult(result));
                        }

                    } catch (Exception e) {
                        Logger.error("HMSModule", "[queryHistoryHMSData] query history HMS data callback error: " + e.getMessage(), e);
                    } finally {
                        queryHistoryHMSDataCallbackRef.remove(id);
                    }
                }
            };
            // 调用SWIG生成的JNI方法
            synchronized (queryHistoryHMSDataCallbackRef) {
                queryHistoryHMSDataCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.hmsModule.QueryHistoryHMSDataWrapper(
                        FlightHubSdkConvert.toNativeHistoryHmsDataParam(params),
                        swigCallback);
            }
        } catch (Exception e) {
            Logger.error("HMSModule", "[queryHistoryHMSData] query history HMS data error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED), null);
            }
        }
    }
}
