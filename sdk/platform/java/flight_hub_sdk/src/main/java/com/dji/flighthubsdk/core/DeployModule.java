// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IDeployModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * 部署/踏勘模块接口，提供离场点监听、视觉踏勘、星历下载、进离场航线侦察等能力。
 */
public class DeployModule {

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

    private final IDeployModule deployModule;

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> actionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int actionCallbackIdCounter = 0;

    public DeployModule(IDeployModule deployModule) {
        this.deployModule = deployModule;
    }

    private synchronized int generateActionCallbackId() {
        return actionCallbackIdCounter++;
    }

    /**
     * 订阅机场离场点信息
     * @param callback 离场点回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_dock_departure_point
     */
    public int listenDockDeparturePoint(FlightHubCallback.DockDeparturePointCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockDeparturePointCallback swigCallback = new com.dji.flighthubsdk.generated.DockDeparturePointCallback() {
                @Override
                public void onDockDeparturePoint(com.dji.flighthubsdk.generated.DockDeparturePoint msg) {
                    if (callback != null) callback.onResult(msg);
                }
            };

            int listenId = this.deployModule.listenDockDeparturePointWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeployModule", "listenDockDeparturePoint error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅机场离场点信息
     * @param listenId 监听 ID
     */
    public void cancelListenDockDeparturePoint(int listenId) {
        try {
            this.deployModule.cancelListenDockDeparturePointWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeployModule", "cancelListenDockDeparturePoint error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅进离场航线侦察任务状态
     * @param callback 侦察任务状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_task_visual_scout_status
     */
    public int listenAircraftVisualScoutStatus(FlightHubCallback.AircraftVisualScoutStatusCallback callback) {
        try {
            com.dji.flighthubsdk.generated.AircraftVisualScoutStatusCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftVisualScoutStatusCallback() {
                @Override
                public void onAircraftVisualScoutStatus(com.dji.flighthubsdk.generated.AircraftVisualScoutStatus msg) {
                    if (callback != null) callback.onResult(msg);
                }
            };

            int listenId = this.deployModule.listenAircraftVisualScoutStatusWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeployModule", "listenAircraftVisualScoutStatus error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅进离场航线侦察任务状态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftVisualScoutStatus(int listenId) {
        try {
            this.deployModule.cancelListenAircraftVisualScoutStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeployModule", "cancelListenAircraftVisualScoutStatus error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅踏勘任务异常信息
     * @param callback 异常信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_task_visual_scout_abnormal_info
     */
    public int listenAircraftVisualScoutAbnormalInfo(FlightHubCallback.AircraftVisualScoutAbnormalInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.AircraftVisualScoutAbnormalInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftVisualScoutAbnormalInfoCallback() {
                @Override
                public void onAircraftVisualScoutAbnormalInfo(com.dji.flighthubsdk.generated.AircraftVisualScoutAbnormalInfo msg) {
                    if (callback != null) callback.onResult(msg);
                }
            };

            int listenId = this.deployModule.listenAircraftVisualScoutAbnormalInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeployModule", "listenAircraftVisualScoutAbnormalInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅踏勘任务异常信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftVisualScoutAbnormalInfo(int listenId) {
        try {
            this.deployModule.cancelListenAircraftVisualScoutAbnormalInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeployModule", "cancelListenAircraftVisualScoutAbnormalInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅障碍物过近告警
     * @param callback 障碍物信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_task_visual_scout_obstacle_info
     */
    public int listenAircraftVisualScoutObstacleInfo(FlightHubCallback.AircraftVisualScoutObstacleInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.AircraftVisualScoutObstacleInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftVisualScoutObstacleInfoCallback() {
                @Override
                public void onAircraftVisualScoutObstacleInfo(com.dji.flighthubsdk.generated.AircraftVisualScoutObstacleInfo msg) {
                    if (callback != null) callback.onResult(msg);
                }
            };

            int listenId = this.deployModule.listenAircraftVisualScoutObstacleInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeployModule", "listenAircraftVisualScoutObstacleInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅障碍物过近告警
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftVisualScoutObstacleInfo(int listenId) {
        try {
            this.deployModule.cancelListenAircraftVisualScoutObstacleInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeployModule", "cancelListenAircraftVisualScoutObstacleInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅星历下载状态
     * @param callback 下载状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_task_ephemeris_download_state
     */
    public int listenAircraftEphemerisDownloadState(FlightHubCallback.EphemerisDownloadStateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.EphemerisDownloadStateCallback swigCallback = new com.dji.flighthubsdk.generated.EphemerisDownloadStateCallback() {
                @Override
                public void onEphemerisDownloadState(com.dji.flighthubsdk.generated.EphemerisDownloadState msg) {
                    if (callback != null) callback.onResult(msg);
                }
            };

            int listenId = this.deployModule.listenAircraftEphemerisDownloadStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeployModule", "listenAircraftEphemerisDownloadState error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅星历下载状态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftEphemerisDownloadState(int listenId) {
        try {
            this.deployModule.cancelListenAircraftEphemerisDownloadStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeployModule", "cancelListenAircraftEphemerisDownloadState error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅星历信息
     * @param callback 星历信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_ephemeris_info
     */
    public int listenAircraftEphemerisInfo(FlightHubCallback.EphemerisInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.EphemerisInfoCallback swigCallback = new com.dji.flighthubsdk.generated.EphemerisInfoCallback() {
                @Override
                public void onEphemerisInfo(com.dji.flighthubsdk.generated.EphemerisInfo msg) {
                    if (callback != null) callback.onResult(msg);
                }
            };

            int listenId = this.deployModule.listenAircraftEphemerisInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DeployModule", "listenAircraftEphemerisInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅星历信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftEphemerisInfo(int listenId) {
        try {
            this.deployModule.cancelListenAircraftEphemerisInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DeployModule", "cancelListenAircraftEphemerisInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 下载星历数据
     * @param params 下载参数（星历 URL、文件名）
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /control/api/v1/manual/scout/ephemeris/url
     */
    public void downloadEphemeris(com.dji.flighthubsdk.generated.DownloadEphemerisParams params, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        actionCallbackRef.remove(id);
                    }
                }
            };
            synchronized (actionCallbackRef) {
                actionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deployModule.downloadEphemerisWrapper(params, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeployModule", "downloadEphemeris error: " + e.getMessage(), e);
            if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
        }
    }

    /**
     * 执行进离场航线侦察任务（开始/暂停）
     * @param params 侦察任务参数
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /control/api/v1/manual/scout/task
     */
    public void executeInOutWaylineDetect(com.dji.flighthubsdk.generated.InOutWaylineDetectParams params, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        actionCallbackRef.remove(id);
                    }
                }
            };
            synchronized (actionCallbackRef) {
                actionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deployModule.executeInOutWaylineDetectWrapper(params, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeployModule", "executeInOutWaylineDetect error: " + e.getMessage(), e);
            if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
        }
    }

    /**
     * 设置进离场航线飞行参数
     * @param params 飞行设置参数（返航高度、失联动作、最大高度/距离限制）
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /control/api/v1/manual/scout/flight/setting
     */
    public void setInOutWaylineFlight(com.dji.flighthubsdk.generated.InOutWaylineFlightSettingParams params, FlightHubCallback.ResultCallbackWithError callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCallback = new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        actionCallbackRef.remove(id);
                    }
                }
            };
            synchronized (actionCallbackRef) {
                actionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deployModule.setInOutWaylineFlightWrapper(params, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeployModule", "setInOutWaylineFlight error: " + e.getMessage(), e);
            if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
        }
    }

    /**
     * 获取进离场航线飞行设置
     * @param callback 结果回调，成功时返回当前飞行设置参数
     * @implNote 链路: HTTP | GET /control/api/v1/manual/scout/flight/setting
     */
    public void getInOutWaylineFlightSetting(FlightHubCallback.InOutWaylineFlightSettingCallback callback) {
        try {
            final int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.InOutWaylineFlightSettingResultCallback swigCallback = new com.dji.flighthubsdk.generated.InOutWaylineFlightSettingResultCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.InOutWaylineFlightSettingParams params, com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(params, new com.dji.flighthubsdk.bean.FlightHubError(
                                err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        actionCallbackRef.remove(id);
                    }
                }
            };
            synchronized (actionCallbackRef) {
                actionCallbackRef.put(id, new CallbackRefContainer(swigCallback, callback));
                this.deployModule.getInOutWaylineFlightSettingWrapper(swigCallback);
            }
        } catch (Exception e) {
            Logger.error("DeployModule", "getInOutWaylineFlightSetting error: " + e.getMessage(), e);
            if (callback != null) callback.onResult(null, new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
        }
    }
}
