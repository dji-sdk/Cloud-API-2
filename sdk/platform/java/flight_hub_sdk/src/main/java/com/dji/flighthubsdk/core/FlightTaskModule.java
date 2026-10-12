// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;

/**
 * 航线任务管理模块接口，提供任务的增删改查、执行、状态更新、设备推荐、TCAS 等功能。
 */
public class FlightTaskModule {
    private class CallbackRefContainer {
        private Object callbackJniRef;
        private Object callbackJavaRef;

        CallbackRefContainer(Object callbackJniRef, Object callbackJavaRef) {
            this.callbackJniRef = callbackJniRef;
            this.callbackJavaRef = callbackJavaRef;
        }

        public void release() {
            if (callbackJniRef != null) {
                try {
                    callbackJniRef.getClass().getMethod("delete").invoke(callbackJniRef);
                } catch (Exception ignored) {
                }
                callbackJniRef = null;
            }
        }
    }

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getCommandTaskListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getFlightTaskTrackCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getFlightTaskListCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> createFlightTaskCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getFlightTaskDetailCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> editFlightTaskCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> deleteFlightTaskCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateFlightTaskStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> batchUpdateFlightTaskStatusCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getDeviceRecommendationCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getDeviceTcasCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> updateDeviceTcasCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> startRoamingCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> setRoamingStationSnCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> getInFlightTaskToposCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int idCounter = 0;
    private synchronized int generateId() {
        return idCounter++;
    }

    private final IFlightTaskModule module;

    /**
     * 构造飞行任务模块
     * @param module 底层飞行任务模块接口
     */
    public FlightTaskModule(IFlightTaskModule module) {
        this.module = module;
    }

    /**
     * 获取航线任务列表
     * @param params 查询参数（分页、SN 过滤、时间范围、状态过滤等）
     * @param callback 结果回调，成功时返回任务列表和分页信息
     * @implNote 链路: HTTP | GET /atc/api/v1/flight-tasks
     */
    public void getFlightTaskList(com.dji.flighthubsdk.bean.FlightTaskListParams params, FlightHubCallback.GetFlightTaskListCallback callback) {
        int id = generateId();
        GetFlightTaskListCallback swigCb = new GetFlightTaskListCallback() {
            @Override
            public void onGetFlightTaskList(com.dji.flighthubsdk.generated.FlightHubError err, FlightTaskListResult result) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toFlightTaskListResult(result));
                    }
                } finally {
                    getFlightTaskListCallbackRef.remove(id);
                }
            }
        };
        synchronized (getFlightTaskListCallbackRef) {
            getFlightTaskListCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.FlightTaskListParams nativeFlightTaskListParams = null;
            try {
                nativeFlightTaskListParams = FlightHubSdkConvert.toNativeFlightTaskListParams(params);
                module.getFlightTaskListWrapper(nativeFlightTaskListParams, swigCb);
            } finally {
                if (nativeFlightTaskListParams != null) {
                    nativeFlightTaskListParams.delete();
                }
            }
        }

    }

    /**
     * 获取一键起飞任务列表
     * @param params 查询参数（分页、SN 过滤、时间范围、状态过滤等）
     * @param callback 结果回调，成功时返回任务列表和分页信息
     * @implNote 链路: HTTP | GET /atc/api/v1/command-tasks
     */
    public void getCommandTaskList(com.dji.flighthubsdk.bean.GetCommandTaskListParams params, FlightHubCallback.GetCommandTaskListCallback callback) {
        int id = generateId();
        GetCommandTaskListCallback swigCb = new GetCommandTaskListCallback() {
            @Override
            public void onGetCommandTaskList(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.GetCommandTaskListResults results) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetCommandTaskListResults(results));
                    }
                } finally {
                    getCommandTaskListCallbackRef.remove(id);
                }
            }
        };
        synchronized (getCommandTaskListCallbackRef) {
            getCommandTaskListCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.GetCommandTaskListParams nativeParams = null;
            try {
                nativeParams = FlightHubSdkConvert.toNativeGetCommandTaskListParams(params);
                module.getCommandTaskListWrapper(nativeParams, swigCb);
            } finally {
                if (nativeParams != null) {
                    nativeParams.delete();
                }
            }
        }

    }

    /**
     * 查询飞行任务轨迹
     * @param params 查询参数（任务UUID）
     * @param callback 结果回调，成功时返回任务轨迹信息
     * @implNote 链路: HTTP | GET /atc/api/v1/flight-tasks/track
     */
    public void getFlightTaskTrack(com.dji.flighthubsdk.bean.GetFlightTaskTrackParams params, FlightHubCallback.GetFlightTaskTrackCallback callback) {
        int id = generateId();
        GetFlightTaskTrackCallback swigCb = new GetFlightTaskTrackCallback() {
            @Override
            public void onGetFlightTaskTrack(com.dji.flighthubsdk.generated.FlightHubError err, com.dji.flighthubsdk.generated.GetFlightTaskTrackResults results) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetFlightTaskTrackResults(results));
                    }
                } finally {
                    getFlightTaskTrackCallbackRef.remove(id);
                }
            }
        };
        synchronized (getFlightTaskTrackCallbackRef) {
            getFlightTaskTrackCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.GetFlightTaskTrackParams nativeParams = null;
            try {
                nativeParams = FlightHubSdkConvert.toNativeGetFlightTaskTrackParams(params);
                module.getFlightTaskTrackWrapper(nativeParams, swigCb);
            } finally {
                if (nativeParams != null) {
                    nativeParams.delete();
                }
            }
        }

    }

    /**
     * 创建航线任务
     * @param req 创建任务请求（名称、SN、航线、起止时间等）
     * @param callback 结果回调，成功时返回任务 UUID
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks
     */
    public void createFlightTask(com.dji.flighthubsdk.bean.CreateTaskReq req, FlightHubCallback.CreateFlightTaskCallback callback) {
        int id = generateId();
        CreateFlightTaskCallback swigCb = new CreateFlightTaskCallback() {
            @Override
            public void onCreateFlightTask(com.dji.flighthubsdk.generated.FlightHubError err, CreateTaskResp result) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCreateTaskResp(result));
                    }
                } finally {
                    createFlightTaskCallbackRef.remove(id);
                }
            }
        };
        synchronized (createFlightTaskCallbackRef) {
            createFlightTaskCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.CreateTaskReq nativeCreateTaskReq = null;
            try {
                nativeCreateTaskReq = FlightHubSdkConvert.toNativeCreateTaskReq(req);
                module.createFlightTaskWrapper(nativeCreateTaskReq, swigCb);
            } finally {
                if (nativeCreateTaskReq != null) {
                    nativeCreateTaskReq.delete();
                }
            }
        }

    }

    /**
     * 获取航线任务详情
     * @param taskUuid 任务 UUID
     * @param callback 结果回调，成功时返回任务详情
     * @implNote 链路: HTTP | GET /atc/api/v1/flight-tasks/{task_uuid}
     */
    public void getFlightTaskDetail(String taskUuid, FlightHubCallback.GetFlightTaskDetailCallback callback) {
        int id = generateId();
        GetFlightTaskDetailCallback swigCb = new GetFlightTaskDetailCallback() {
            @Override
            public void onGetFlightTaskDetail(com.dji.flighthubsdk.generated.FlightHubError err, TaskDetail result) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toTaskDetail(result));
                    }
                } finally {
                    getFlightTaskDetailCallbackRef.remove(id);
                }
            }
        };
        synchronized (getFlightTaskDetailCallbackRef) {
            getFlightTaskDetailCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            module.getFlightTaskDetailWrapper(taskUuid, swigCb);
        }
    }

    /**
     * 编辑航线任务（全量替换）。
     * <p>服务端为全量替换设计，所有必填字段（name, taskType, timeZone, sn,
     * waylineUuid, outOfControlActionInFlight, rthAltitude）每次都必须传入。
     * 建议先调用 {@link #getFlightTaskDetail} 获取完整数据，修改目标字段后再传入。</p>
     * @param taskUuid 任务 UUID
     * @param req 编辑请求（全量替换，必须包含所有必填字段）
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /atc/api/v1/flight-tasks/{task_uuid}
     */
    public void editFlightTask(String taskUuid, com.dji.flighthubsdk.bean.EditTaskReq req, FlightHubCallback.CreateFlightTaskCallback callback) {
        int id = generateId();
        CreateFlightTaskCallback swigCb = new CreateFlightTaskCallback() {
            @Override
            public void onCreateFlightTask(com.dji.flighthubsdk.generated.FlightHubError err, CreateTaskResp result) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toCreateTaskResp(result));
                    }
                } finally {
                    editFlightTaskCallbackRef.remove(id);
                }
            }
        };
        synchronized (editFlightTaskCallbackRef) {
            editFlightTaskCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.EditTaskReq nativeEditTaskReq = FlightHubSdkConvert.toNativeEditTaskReq(req);
            try {
                module.editFlightTaskWrapper(taskUuid, nativeEditTaskReq, swigCb);
            } finally {
                if (nativeEditTaskReq != null) {
                    nativeEditTaskReq.delete();
                }
            }
        }
    }

    /**
     * 删除航线任务
     * @param taskUuid 任务 UUID
     * @param callback 结果回调
     * @implNote 链路: HTTP | DELETE /atc/api/v1/flight-tasks/{task_uuid}
     */
    public void deleteFlightTask(String taskUuid, FlightHubCallback.ResultCallbackWithError callback) {
        int id = generateId();
        ResultCallbackWithError swigCb = new ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                try {
                    if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                } finally {
                    deleteFlightTaskCallbackRef.remove(id);
                }
            }
        };
        synchronized (deleteFlightTaskCallbackRef) {
            deleteFlightTaskCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            module.deleteFlightTaskWrapper(taskUuid, swigCb);
        }
    }

    /**
     * 更新航线任务状态
     * @param taskUuid 任务 UUID
     * @param status 目标状态值
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /atc/api/v1/flight-tasks/{task_uuid}/status
     */
    public void updateFlightTaskStatus(String taskUuid, int status, FlightHubCallback.ResultCallbackWithError callback) {
        int id = generateId();
        ResultCallbackWithError swigCb = new ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                try {
                    if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                } finally {
                    updateFlightTaskStatusCallbackRef.remove(id);
                }
            }
        };
        synchronized (updateFlightTaskStatusCallbackRef) {
            updateFlightTaskStatusCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            module.updateFlightTaskStatusWrapper(taskUuid, status, swigCb);
        }
    }

    /**
     * 批量更新航线任务状态
     * @param req 批量更新请求（任务 UUID 列表 + 目标状态）
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /atc/api/v1/flight-tasks/batch/status
     */
    public void batchUpdateFlightTaskStatus(com.dji.flighthubsdk.bean.BatchUpdateStatusReq req, FlightHubCallback.ResultCallbackWithError callback) {
        int id = generateId();
        ResultCallbackWithError swigCb = new ResultCallbackWithError() {
            @Override
            public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                try {
                    if (callback != null) callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                } finally {
                    batchUpdateFlightTaskStatusCallbackRef.remove(id);
                }
            }
        };
        synchronized (batchUpdateFlightTaskStatusCallbackRef) {
            batchUpdateFlightTaskStatusCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.BatchUpdateStatusReq nativeBatchUpdateStatusReq = null;
            try {
                nativeBatchUpdateStatusReq = FlightHubSdkConvert.toNativeBatchUpdateStatusReq(req);
                module.batchUpdateFlightTaskStatusWrapper(nativeBatchUpdateStatusReq, swigCb);
            } finally {
                if (nativeBatchUpdateStatusReq != null) {
                    nativeBatchUpdateStatusReq.delete();
                }
            }
        }

    }

    /**
     * 获取设备推荐（根据航线和策略推荐最优设备）
     * @param req 推荐请求（SN 列表、航线坐标、策略 ID 等）
     * @param callback 结果回调，成功时返回推荐设备列表
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks/device-recommendation
     */
    public void getDeviceRecommendation(com.dji.flighthubsdk.bean.DeviceRecommendationReq req, FlightHubCallback.GetDeviceRecommendationCallback callback) {
        int id = generateId();
        GetDeviceRecommendationCallback swigCb = new GetDeviceRecommendationCallback() {
            @Override
            public void onGetDeviceRecommendation(com.dji.flighthubsdk.generated.FlightHubError err, DeviceRecommendationResp result) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toDeviceRecommendationResp(result));
                    }
                } finally {
                    getDeviceRecommendationCallbackRef.remove(id);
                }
            }
        };
        synchronized (getDeviceRecommendationCallbackRef) {
            getDeviceRecommendationCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.DeviceRecommendationReq nativeDeviceRecommendationReq = null;
            try {
                nativeDeviceRecommendationReq = FlightHubSdkConvert.toNativeDeviceRecommendationReq(req);
                module.getDeviceRecommendationWrapper(nativeDeviceRecommendationReq, swigCb);
            } finally {
                if (nativeDeviceRecommendationReq != null) {
                    nativeDeviceRecommendationReq.delete();
                }
            }
        }

    }

    /**
     * 获取设备 TCAS（防碰撞）信息
     * @param centerSn 中心设备 SN 列表
     * @param callback 结果回调，成功时返回 TCAS 信息列表
     * @implNote 链路: HTTP | GET /atc/api/v1/flight-tasks/tcas
     */
    public void getDeviceTcas(java.util.List<String> centerSn, FlightHubCallback.GetDeviceTcasCallback callback) {
        StringVector nativeCenterSn = null;
        try {
            nativeCenterSn = FlightHubSdkConvert.toNativeStringVector(centerSn);
            int id = generateId();
            GetDeviceTcasCallback swigCb = new GetDeviceTcasCallback() {
                @Override
                public void onGetDeviceTcas(com.dji.flighthubsdk.generated.FlightHubError err, GetTCAInfosResp result) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err), FlightHubSdkConvert.toGetTCAInfosResp(result));
                        }
                    } finally {
                        getDeviceTcasCallbackRef.remove(id);
                    }
                }
            };
            synchronized (getDeviceTcasCallbackRef) {
                getDeviceTcasCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                module.getDeviceTcasWrapper(nativeCenterSn, swigCb);
            }
        } finally {
            if (nativeCenterSn != null) {
                nativeCenterSn.delete();
            }
        }
    }

    /**
     * 更新设备 TCAS（防碰撞）信息
     * @param centerSn 中心设备 SN 列表
     * @param callback 结果回调
     * @implNote 链路: HTTP | PUT /atc/api/v1/flight-tasks/tcas
     */
    public void updateDeviceTcas(java.util.List<String> centerSn, FlightHubCallback.ResultCallbackWithError callback) {
        StringVector nativeCenterSn = null;
        try {
            nativeCenterSn = FlightHubSdkConvert.toNativeStringVector(centerSn);
            int id = generateId();
            ResultCallbackWithError swigCb = new ResultCallbackWithError() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(err.getError_code(), (int) err.getHttp_code(), (int) err.getServer_code(), (int) err.getDevice_code(), err.getServer_message()));
                        }
                    } finally {
                        updateDeviceTcasCallbackRef.remove(id);
                    }
                }
            };
            synchronized (updateDeviceTcasCallbackRef) {
                updateDeviceTcasCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                module.updateDeviceTcasWrapper(nativeCenterSn, swigCb);
            }
        } finally {
            if (nativeCenterSn != null) {
                nativeCenterSn.delete();
            }
        }
    }

    /**
     * 开始漫游调度。
     * @param params 无人机 SN、架次 UUID 和来源任务类型（1=航线，2=一键起飞）
     * @param callback 结果回调
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks/start-roaming
     */
    public void startRoaming(com.dji.flighthubsdk.bean.StartRoamingParams params,
                             FlightHubCallback.StartRoamingCallback callback) {
        int id = generateId();
        StartRoamingCallback swigCb = new StartRoamingCallback() {
            @Override
            public void onStartRoaming(com.dji.flighthubsdk.generated.FlightHubError err) {
                try {
                    if (callback != null) callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                } finally {
                    startRoamingCallbackRef.remove(id);
                }
            }
        };
        synchronized (startRoamingCallbackRef) {
            startRoamingCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.StartRoamingParams nativeParams = null;
            try {
                nativeParams = FlightHubSdkConvert.toNativeStartRoamingParams(params);
                module.startRoamingWrapper(nativeParams, swigCb);
            } finally {
                if (nativeParams != null) nativeParams.delete();
            }
        }
    }

    /**
     * 设置飞机级漫游基站白名单。
     * @implNote 链路: HTTP | POST /atc/api/v1/flight-tasks/roaming/base-stations
     */
    public void setRoamingStationSn(com.dji.flighthubsdk.bean.SetRoamingStationSnParams params,
                                    FlightHubCallback.SetRoamingStationSnCallback callback) {
        int id = generateId();
        SetRoamingStationSnCallback swigCb = new SetRoamingStationSnCallback() {
            @Override
            public void onSetRoamingStationSn(com.dji.flighthubsdk.generated.FlightHubError err,
                                              SetRoamingStationSnResult result) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                FlightHubSdkConvert.toSetRoamingStationSnResult(result));
                    }
                } finally {
                    setRoamingStationSnCallbackRef.remove(id);
                }
            }
        };
        synchronized (setRoamingStationSnCallbackRef) {
            setRoamingStationSnCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
            com.dji.flighthubsdk.generated.SetRoamingStationSnParams nativeParams = null;
            try {
                nativeParams = FlightHubSdkConvert.toNativeSetRoamingStationSnParams(params);
                module.setRoamingStationSnWrapper(nativeParams, swigCb);
            } finally {
                if (nativeParams != null) nativeParams.delete();
            }
        }
    }

    /**
     * 查询飞行中任务拓扑。
     * @param droneSns 无人机 SN 列表，数量范围为 1–10
     * @implNote 链路: HTTP | GET /atc/api/v1/flight-tasks/in-flight-task-topos
     */
    public void getInFlightTaskTopos(java.util.List<String> droneSns,
                                     FlightHubCallback.GetInFlightTaskToposCallback callback) {
        int id = generateId();
        GetInFlightTaskToposCallback swigCb = new GetInFlightTaskToposCallback() {
            @Override
            public void onGetInFlightTaskTopos(com.dji.flighthubsdk.generated.FlightHubError err,
                                               GetInFlightTaskToposResult result) {
                try {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightHubError(err),
                                FlightHubSdkConvert.toGetInFlightTaskToposResult(result));
                    }
                } finally {
                    getInFlightTaskToposCallbackRef.remove(id);
                }
            }
        };
        StringVector nativeDroneSns = null;
        try {
            nativeDroneSns = FlightHubSdkConvert.toNativeStringVector(droneSns);
            synchronized (getInFlightTaskToposCallbackRef) {
                getInFlightTaskToposCallbackRef.put(id, new CallbackRefContainer(swigCb, callback));
                module.getInFlightTaskToposWrapper(nativeDroneSns, swigCb);
            }
        } finally {
            if (nativeDroneSns != null) nativeDroneSns.delete();
        }
    }

    /**
     * 订阅漫游任务拓扑变更推送。
     * @return 监听 ID，失败时返回 -1
     * @implNote 链路: WebSocket | topic_task_topo_changed
     */
    public int listenTaskTopoChanged(FlightHubCallback.TaskTopoChangedCallback callback) {
        TaskTopoChangedCallback swigCb = new TaskTopoChangedCallback() {
            @Override
            public void onTaskTopoChanged(TaskTopoChangedEvent event) {
                if (callback != null) {
                    callback.onResult(FlightHubSdkConvert.toTaskTopoChangedEvent(event));
                }
            }
        };
        int listenId = module.listenTaskTopoChangedWrapper(swigCb);
        if (listenId >= 0) {
            swigCb.swigReleaseOwnership();
        }
        return listenId;
    }

    /** 取消漫游任务拓扑变更订阅。 */
    public void cancelListenTaskTopoChanged(int listenId) {
        module.cancelListenTaskTopoChangedWrapper(listenId);
    }
}
