// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.*;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Inside 机载算法管理模块（PAAS 纯血场景）。
 * <p>负责机载算法全生命周期管理：列表查询、设备绑定查询、设备统计、
 * 批量装载/卸载指令下发，以及私有化场景的算法上传凭证创建、文件处理、算法删除。</p>
 */
public class AIInsideManageModule {

    private static final String TAG = "AIInsideManageModule";

    /** 持有 SWIG Director 回调对象强引用，防止异步回调触发前被 GC 回收。Director native 内存由 C++ wrapper delete callback 释放。 */
    private class CallbackRefContainer {
        private final Object callback_jni_ref;
        private final Object callback_java_ref;

        public CallbackRefContainer(Object jni, Object java) {
            this.callback_jni_ref = jni;
            this.callback_java_ref = java;
        }
    }

    private final ConcurrentHashMap<Integer, CallbackRefContainer> getModelsRef      = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getDevicesRef     = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> getSummaryRef     = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> operateRef        = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> createRef         = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> fileUploadRef     = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> deleteRef         = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, CallbackRefContainer> uploadFileRef     = new ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() { return id_counter++; }

    private void removeActionCallbackRef(ConcurrentHashMap<Integer, CallbackRefContainer> map, int id) {
        map.remove(id);
    }

    private final IAIInsideManageModule module;

    public AIInsideManageModule(IAIInsideManageModule module) {
        this.module = module;
    }

    /**
     * 销毁模块，清除未完成 action 回调的强引用。
     * 应在 Workspace 销毁时调用；Director native 内存由 C++ 侧 delete callback 负责。
     */
    @SuppressWarnings("unchecked")
    public void destroy() {
        List<ConcurrentHashMap<Integer, CallbackRefContainer>> allMaps = Arrays.asList(
                getModelsRef, getDevicesRef, getSummaryRef,
                operateRef, createRef, fileUploadRef, deleteRef, uploadFileRef);
        for (ConcurrentHashMap<Integer, CallbackRefContainer> map : allMaps) {
            map.clear();
        }
    }

    /**
     * 获取机载算法列表。
     *
     * @implNote GET /api/v1/ai-inside/models
     * @param callback 结果回调，在主线程触发
     */
    public void getAIInsideModels(FlightHubCallback.GetAIInsideModelsCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetAIInsideModelsCallback swigCb =
                new com.dji.flighthubsdk.generated.GetAIInsideModelsCallback() {
                    @Override
                    public void onGetAIInsideModels(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.GetAIInsideModelsResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetAIInsideModelsResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getAIInsideModels] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(getModelsRef, id);
                        }
                    }
                };
            synchronized (getModelsRef) {
                getModelsRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getAIInsideModelsWrapper(swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getAIInsideModels] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toGetAIInsideModelsResult(null));
            }
        }
    }

    /**
     * 获取指定算法绑定的设备详情（分页）。
     *
     * @implNote GET /api/v1/ai-inside/models/devices
     * @param param    请求参数（signed_id、page、page_size、可选 drone_sn 列表）
     * @param callback 结果回调，在主线程触发
     */
    public void getAIInsideModelDevices(
            com.dji.flighthubsdk.bean.GetAIInsideModelDevicesParam param,
            FlightHubCallback.GetAIInsideModelDevicesCallback callback) {
        com.dji.flighthubsdk.generated.GetAIInsideModelDevicesParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeGetAIInsideModelDevicesParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetAIInsideModelDevicesCallback swigCb =
                new com.dji.flighthubsdk.generated.GetAIInsideModelDevicesCallback() {
                    @Override
                    public void onGetAIInsideModelDevices(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.GetAIInsideModelDevicesResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetAIInsideModelDevicesResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getAIInsideModelDevices] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(getDevicesRef, id);
                        }
                    }
                };
            synchronized (getDevicesRef) {
                getDevicesRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getAIInsideModelDevicesWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getAIInsideModelDevices] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toGetAIInsideModelDevicesResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 获取指定算法在指定操作类型下的可操作设备数量统计。
     *
     * @implNote GET /api/v1/ai-inside/models/devices/summary
     * @param param    请求参数（signed_id、operation_type: load/update）
     * @param callback 结果回调，在主线程触发
     */
    public void getAIInsideModelDevicesSummary(
            com.dji.flighthubsdk.bean.GetAIInsideModelDevicesSummaryParam param,
            FlightHubCallback.GetAIInsideModelDevicesSummaryCallback callback) {
        com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeGetAIInsideModelDevicesSummaryParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryCallback swigCb =
                new com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryCallback() {
                    @Override
                    public void onGetAIInsideModelDevicesSummary(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.GetAIInsideModelDevicesSummaryResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toGetAIInsideModelDevicesSummaryResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[getAIInsideModelDevicesSummary] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(getSummaryRef, id);
                        }
                    }
                };
            synchronized (getSummaryRef) {
                getSummaryRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.getAIInsideModelDevicesSummaryWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[getAIInsideModelDevicesSummary] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toGetAIInsideModelDevicesSummaryResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 批量下发 Load / Update / Unload 操作指令。
     *
     * @implNote POST /api/v1/ai-inside/models/operate
     * @param param    请求参数（signed_id、operation_type: load/update/unload、可选 drone_sn_list）
     * @param callback 结果回调，含各设备操作结果码
     */
    public void operateAIInsideModel(
            com.dji.flighthubsdk.bean.OperateAIInsideModelParam param,
            FlightHubCallback.OperateAIInsideModelCallback callback) {
        com.dji.flighthubsdk.generated.OperateAIInsideModelParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeOperateAIInsideModelParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.OperateAIInsideModelCallback swigCb =
                new com.dji.flighthubsdk.generated.OperateAIInsideModelCallback() {
                    @Override
                    public void onOperateAIInsideModel(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.OperateAIInsideModelResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toOperateAIInsideModelResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[operateAIInsideModel] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(operateRef, id);
                        }
                    }
                };
            synchronized (operateRef) {
                operateRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.operateAIInsideModelWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[operateAIInsideModel] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toOperateAIInsideModelResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 创建算法包上传凭证（私有化场景）。
     * 返回 MinIO 预签名 STS 凭证，供调用方直传 ZIP 算法包到存储。
     *
     * @implNote POST /api/v1/ai-inside/algorithms/create
     * @param param    请求参数（name: 上传文件名）
     * @param callback 结果回调，含 MinIO 凭证和存储路径信息
     */
    public void createAIInsideAlgorithm(
            com.dji.flighthubsdk.bean.CreateAIInsideAlgorithmParam param,
            FlightHubCallback.CreateAIInsideAlgorithmCallback callback) {
        com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeCreateAIInsideAlgorithmParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmCallback swigCb =
                new com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmCallback() {
                    @Override
                    public void onCreateAIInsideAlgorithm(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.CreateAIInsideAlgorithmResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toCreateAIInsideAlgorithmResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[createAIInsideAlgorithm] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(createRef, id);
                        }
                    }
                };
            synchronized (createRef) {
                createRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.createAIInsideAlgorithmWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[createAIInsideAlgorithm] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toCreateAIInsideAlgorithmResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 处理已上传的算法文件（私有化场景）。
     * 前端上传 ZIP 到 MinIO 后调用此接口，后端将解压解析并写入 DB。
     *
     * @implNote POST /api/v1/ai-inside/algorithms/file-upload
     * @param param    请求参数（file_key: MinIO 对象 Key）
     * @param callback 结果回调，含完整算法详情
     */
    public void fileUploadAIInsideAlgorithm(
            com.dji.flighthubsdk.bean.FileUploadAIInsideAlgorithmParam param,
            FlightHubCallback.FileUploadAIInsideAlgorithmCallback callback) {
        com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeFileUploadAIInsideAlgorithmParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmCallback swigCb =
                new com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmCallback() {
                    @Override
                    public void onFileUploadAIInsideAlgorithm(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.FileUploadAIInsideAlgorithmResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toFileUploadAIInsideAlgorithmResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[fileUploadAIInsideAlgorithm] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(fileUploadRef, id);
                        }
                    }
                };
            synchronized (fileUploadRef) {
                fileUploadRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.fileUploadAIInsideAlgorithmWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[fileUploadAIInsideAlgorithm] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toFileUploadAIInsideAlgorithmResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 删除指定算法（私有化场景）。
     * 同步清理 MinIO 文件和 DB 记录。
     *
     * @implNote DELETE /api/v1/ai-inside/algorithms
     * @param param    请求参数（signed_id: 算法签名 ID）
     * @param callback 结果回调，code=0 表示删除成功
     */
    public void deleteAIInsideAlgorithm(
            com.dji.flighthubsdk.bean.DeleteAIInsideAlgorithmParam param,
            FlightHubCallback.DeleteAIInsideAlgorithmCallback callback) {
        com.dji.flighthubsdk.generated.DeleteAIInsideAlgorithmParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeDeleteAIInsideAlgorithmParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.DeleteAIInsideAlgorithmCallback swigCb =
                new com.dji.flighthubsdk.generated.DeleteAIInsideAlgorithmCallback() {
                    @Override
                    public void onDeleteAIInsideAlgorithm(
                            com.dji.flighthubsdk.generated.FlightHubError err) {
                        try {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[deleteAIInsideAlgorithm] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(deleteRef, id);
                        }
                    }
                };
            synchronized (deleteRef) {
                deleteRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.deleteAIInsideAlgorithmWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[deleteAIInsideAlgorithm] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (np != null) np.delete();
        }
    }

    /**
     * 一站式算法文件上传（私有化）。
     * 内部自动串联：获取 STS 上传凭证 → 上传 ZIP 到 MinIO → 通知后端解压入库。
     *
     * @implNote 内部串联 POST .../algorithms/create → MinIO PUT → POST .../algorithms/file-upload
     * @param param    请求参数（filePath: 本地 ZIP 文件绝对路径）
     * @param callback 结果回调，含 object_key 和完整算法入库详情
     */
    public void uploadAlgorithmFile(
            com.dji.flighthubsdk.bean.UploadAIInsideAlgorithmFileParam param,
            FlightHubCallback.UploadAlgorithmFileCallback callback) {
        com.dji.flighthubsdk.generated.UploadAIInsideAlgorithmFileParam np = null;
        try {
            np = FlightHubSdkConvert.toNativeUploadAlgorithmFileParam(param);
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.UploadAlgorithmFileCallback swigCb =
                new com.dji.flighthubsdk.generated.UploadAlgorithmFileCallback() {
                    @Override
                    public void onUploadAlgorithmFile(
                            com.dji.flighthubsdk.generated.FlightHubError err,
                            com.dji.flighthubsdk.generated.UploadAIInsideAlgorithmFileResult result) {
                        try {
                            if (callback != null) {
                                callback.onResult(
                                    FlightHubSdkConvert.toFlightHubError(err),
                                    FlightHubSdkConvert.toUploadAlgorithmFileResult(result));
                            }
                        } catch (Exception e) {
                            Logger.error(TAG, "[uploadAlgorithmFile] cb error: " + e.getMessage(), e);
                        } finally {
                            removeActionCallbackRef(uploadFileRef, id);
                        }
                    }
                };
            synchronized (uploadFileRef) {
                uploadFileRef.put(id, new CallbackRefContainer(swigCb, callback));
                swigCb.swigReleaseOwnership();
                module.uploadAlgorithmFileWrapper(np, swigCb);
            }
        } catch (Exception e) {
            Logger.error(TAG, "[uploadAlgorithmFile] error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(
                    new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED),
                    FlightHubSdkConvert.toUploadAlgorithmFileResult(null));
            }
        } finally {
            if (np != null) np.delete();
        }
    }
}
