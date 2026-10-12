// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.ILiveSyncModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;
import java.util.concurrent.ConcurrentHashMap;

/** 直播转发模块（旁路转码器 CRUD）。通过 {@code Workspace.getLiveSyncModule()} 获取实例。 */
public class LiveSyncModule {

    private static final String TAG = "LiveSyncModule";

    private class CallbackRefContainer {
        private final Object callback_jni_ref;
        private final Object callback_java_ref;

        public CallbackRefContainer(Object jni, Object java) {
            this.callback_jni_ref = jni;
            this.callback_java_ref = java;
        }
    }

    private final ConcurrentHashMap<Integer, CallbackRefContainer> createCallbackRef  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> deleteCallbackRef  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> updateCallbackRef  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> listCallbackRef    = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> devicesCallbackRef = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> summaryCallbackRef = new ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() { return id_counter++; }

    private void removeCallbackRef(ConcurrentHashMap<Integer, CallbackRefContainer> map, int id) {
        map.remove(id);
    }

    private final ILiveSyncModule module;

    public LiveSyncModule(ILiveSyncModule module) {
        this.module = module;
    }

    /**
     * 创建转码器
     * @implNote POST /gateway/manage/api/v1/paas/c2c/live-sync
     */
    public void createLiveSync(
            com.dji.flighthubsdk.bean.LiveSyncCreateParams params,
            FlightHubCallback.CreateLiveSyncCallback callback) {
        com.dji.flighthubsdk.generated.LiveSyncCreateParams np = null;
        try {
            np = FlightHubSdkConvert.toNativeLiveSyncCreateParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CreateLiveSyncCallback swigCb =
                new com.dji.flighthubsdk.generated.CreateLiveSyncCallback() {
                    @Override
                    public void onCreateLiveSync(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.LiveSyncCreateResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toLiveSyncCreateResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[createLiveSync] cb error: " + e.getMessage(), e);
                        } finally {
                            removeCallbackRef(createCallbackRef, id);
                        }
                    }
                };
            synchronized (createCallbackRef) {
                createCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.createLiveSyncWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createLiveSync] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toLiveSyncCreateResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 删除转码器
     * @implNote DELETE /gateway/manage/api/v1/paas/c2c/live-sync/{id}
     */
    public void deleteLiveSync(
            String id,
            FlightHubCallback.DeleteLiveSyncCallback callback) {
        try {
            int cbId = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DeleteLiveSyncCallback swigCb =
                new com.dji.flighthubsdk.generated.DeleteLiveSyncCallback() {
                    @Override
                    public void onDeleteLiveSync(com.dji.flighthubsdk.generated.FlightHubError err) {
                        try {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[deleteLiveSync] cb error: " + e.getMessage(), e);
                        } finally {
                            removeCallbackRef(deleteCallbackRef, cbId);
                        }
                    }
                };
            synchronized (deleteCallbackRef) {
                deleteCallbackRef.put(cbId, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.deleteLiveSyncWrapper(id, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteLiveSync] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    /**
     * 更新转码器（仅 enable 或全量改配置）
     * @implNote PUT /gateway/manage/api/v1/paas/c2c/live-sync/{id}
     */
    public void updateLiveSync(
            com.dji.flighthubsdk.bean.LiveSyncUpdateParams params,
            FlightHubCallback.UpdateLiveSyncCallback callback) {
        com.dji.flighthubsdk.generated.LiveSyncUpdateParams np = null;
        try {
            np = FlightHubSdkConvert.toNativeLiveSyncUpdateParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UpdateLiveSyncCallback swigCb =
                new com.dji.flighthubsdk.generated.UpdateLiveSyncCallback() {
                    @Override
                    public void onUpdateLiveSync(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.LiveSyncItem result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toLiveSyncItem(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[updateLiveSync] cb error: " + e.getMessage(), e);
                        } finally {
                            removeCallbackRef(updateCallbackRef, id);
                        }
                    }
                };
            synchronized (updateCallbackRef) {
                updateCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.updateLiveSyncWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[updateLiveSync] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toLiveSyncItem(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 分页查询转码器列表
     * @implNote GET /gateway/manage/api/v1/paas/c2c/live-sync/list
     */
    public void getLiveSyncList(
            com.dji.flighthubsdk.bean.LiveSyncListParams params,
            FlightHubCallback.GetLiveSyncListCallback callback) {
        com.dji.flighthubsdk.generated.LiveSyncListParams np = null;
        try {
            np = FlightHubSdkConvert.toNativeLiveSyncListParams(params);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetLiveSyncListCallback swigCb =
                new com.dji.flighthubsdk.generated.GetLiveSyncListCallback() {
                    @Override
                    public void onGetLiveSyncList(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.LiveSyncListResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toLiveSyncListResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getLiveSyncList] cb error: " + e.getMessage(), e);
                        } finally {
                            removeCallbackRef(listCallbackRef, id);
                        }
                    }
                };
            synchronized (listCallbackRef) {
                listCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getLiveSyncListWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getLiveSyncList] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toLiveSyncListResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 获取可用设备树（V2）
     * @implNote GET /gateway/manage/api/v1/paas/c2c/live-sync/devices-v2
     */
    public void getLiveSyncDevicesV2(FlightHubCallback.GetLiveSyncDevicesV2Callback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetLiveSyncDevicesV2Callback swigCb =
                new com.dji.flighthubsdk.generated.GetLiveSyncDevicesV2Callback() {
                    @Override
                    public void onGetLiveSyncDevicesV2(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.LiveSyncDevicesV2Result result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toLiveSyncDevicesV2Result(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getLiveSyncDevicesV2] cb error: " + e.getMessage(), e);
                        } finally {
                            removeCallbackRef(devicesCallbackRef, id);
                        }
                    }
                };
            synchronized (devicesCallbackRef) {
                devicesCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getLiveSyncDevicesV2Wrapper(swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getLiveSyncDevicesV2] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toLiveSyncDevicesV2Result(null));
            }
        }
    }

    /**
     * 获取转码器汇总（总数与启用数）
     * @implNote GET /gateway/manage/api/v1/paas/c2c/live-sync/summary
     */
    public void getLiveSyncSummary(FlightHubCallback.GetLiveSyncSummaryCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetLiveSyncSummaryCallback swigCb =
                new com.dji.flighthubsdk.generated.GetLiveSyncSummaryCallback() {
                    @Override
                    public void onGetLiveSyncSummary(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.LiveSyncSummaryResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toLiveSyncSummaryResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getLiveSyncSummary] cb error: " + e.getMessage(), e);
                        } finally {
                            removeCallbackRef(summaryCallbackRef, id);
                        }
                    }
                };
            synchronized (summaryCallbackRef) {
                summaryCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getLiveSyncSummaryWrapper(swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getLiveSyncSummary] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toLiveSyncSummaryResult(null));
            }
        }
    }
}
