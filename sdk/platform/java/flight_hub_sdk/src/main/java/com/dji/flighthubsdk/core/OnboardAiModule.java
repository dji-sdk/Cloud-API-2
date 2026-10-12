// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.generated.AiIdentifyFilterSetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreModeSetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreResetParam;
import com.dji.flighthubsdk.generated.AiIdentifyScoreSetParam;
import com.dji.flighthubsdk.generated.AiIdentifySetParam;
import com.dji.flighthubsdk.generated.AiModelSelectParam;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.generated.IOnboardAiModule;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 机载 AI 模块接口，提供 AI 识别开关/过滤/阈值配置、模型选择及 AI 数据订阅。
 */
public class OnboardAiModule {

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

    private final IOnboardAiModule onboardAiModule;
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> aiDataInfoCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> actionCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private int id_counter = 0;

    private synchronized int generateActionCallbackId() {
        return id_counter++;
    }

    public OnboardAiModule(IOnboardAiModule onboardAiModule) {
        this.onboardAiModule = onboardAiModule;
    }

    /**
     * 订阅机载 AI 数据
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: DRC Northbound | method: drc_ai_info_push
     */
    public int listenAIDataInfo(FlightHubCallback.AiDataInfoCallback callback) {
        try {
            com.dji.flighthubsdk.generated.AiDataInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AiDataInfoCallback() {
                @Override
                public void onAiDataInfo(com.dji.flighthubsdk.generated.AiDataInfo info) {
                    try {
                        if (callback != null) {
                            callback.onResult(info);
                        }
                    } catch (Exception e) {
                        Logger.error("OnboardAiModule", "listenAIDataInfo callback error: " + e.getMessage(), e);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (aiDataInfoCallbackRef) {
                aiDataInfoCallbackRef.put(-1, container);
                int listenId = this.onboardAiModule.listenAIDataInfoWrapper(swigCallback);
                aiDataInfoCallbackRef.remove(-1);
                if (listenId >= 0) {
                    aiDataInfoCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "listenAIDataInfo error: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 取消订阅机载 AI 数据
     * @param listenId 监听 ID
     */
    public void cancelListenAIDataInfo(int listenId) {
        try {
            this.onboardAiModule.cancelListenAIDataInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "cancelListenAIDataInfo error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = aiDataInfoCallbackRef.remove(listenId);
        if (removed != null) {
            removed.release();
        }
    }

    /**
     * 设置 AI 识别开关
     * @implNote 链路: DRC | method: drc_ai_identify_set
     */
    public void AIIdentifySet(AiIdentifySetParam param, FlightHubCallback.ResultCallback callback) {
        invokeAction((id, swigCallback) -> this.onboardAiModule.AIIdentifySetWrapper(param, swigCallback), callback);
    }

    /**
     * 设置 AI 识别过滤
     * @implNote 链路: DRC | method: drc_ai_identify_filter_set
     */
    public void AIIdentifyFilterSet(AiIdentifyFilterSetParam param, FlightHubCallback.ResultCallback callback) {
        invokeAction((id, swigCallback) -> this.onboardAiModule.AIIdentifyFilterSetWrapper(param, swigCallback), callback);
    }

    /**
     * 设置 AI 识别分数阈值
     * @implNote 链路: DRC | method: drc_ai_identify_score_set
     */
    public void AIIdentifyScoreSet(AiIdentifyScoreSetParam param, FlightHubCallback.ResultCallback callback) {
        invokeAction((id, swigCallback) -> this.onboardAiModule.AIIdentifyScoreSetWrapper(param, swigCallback), callback);
    }

    /**
     * 设置 AI 识别分数模式
     * @implNote 链路: DRC | method: drc_ai_identify_score_mode_set
     */
    public void AIIdentifyScoreModeSet(AiIdentifyScoreModeSetParam param, FlightHubCallback.ResultCallback callback) {
        invokeAction((id, swigCallback) -> this.onboardAiModule.AIIdentifyScoreModeSetWrapper(param, swigCallback), callback);
    }

    /**
     * 重置 AI 识别分数；2.0 须填 index + light_type（官方 index=0；light_type: 1广角/2变焦/3红外）
     * @implNote 链路: DRC | method: drc_ai_identify_score_reset
     */
    public void AIIdentifyScoreReset(AiIdentifyScoreResetParam param, FlightHubCallback.ResultCallback callback) {
        invokeAction((id, swigCallback) -> this.onboardAiModule.AIIdentifyScoreResetWrapper(param, swigCallback), callback);
    }

    /**
     * 选择 AI 模型
     * @implNote 链路: DRC | method: drc_ai_model_select
     */
    public void AIModelSelect(AiModelSelectParam param, FlightHubCallback.ResultCallback callback) {
        invokeAction((id, swigCallback) -> this.onboardAiModule.AIModelSelectWrapper(param, swigCallback), callback);
    }

    private interface ActionInvoker {
        void invoke(int id, com.dji.flighthubsdk.generated.ResultCallback swigCallback);
    }

    private void invokeAction(ActionInvoker invoker, FlightHubCallback.ResultCallback callback) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallback swigCallback = new com.dji.flighthubsdk.generated.ResultCallback() {
                @Override
                public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightHubError(err));
                        }
                    } catch (Exception e) {
                        Logger.error("OnboardAiModule", "action callback error: " + e.getMessage(), e);
                    } finally {
                        actionCallbackRef.remove(id);
                    }
                }
            };
            final CallbackRefContainer container = new CallbackRefContainer(swigCallback, callback);
            synchronized (actionCallbackRef) {
                actionCallbackRef.put(id, container);
                invoker.invoke(id, swigCallback);
            }
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "action error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ==================== 新增 DRC AI 高级方法 ====================

    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> aiAdvancedCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<Integer, CallbackRefContainer> aiAlarmStateCallbackRef = new java.util.concurrent.ConcurrentHashMap<>();

    private void invokeDrcAiAction(FlightHubCallback.ResultCallbackWithError callback,
                                    java.util.function.Consumer<com.dji.flighthubsdk.generated.ResultCallbackWithError> nativeCall) {
        try {
            int id = generateActionCallbackId();
            com.dji.flighthubsdk.generated.ResultCallbackWithError swigCb =
                new com.dji.flighthubsdk.generated.ResultCallbackWithError() {
                    @Override
                    public void onResult(com.dji.flighthubsdk.generated.FlightHubError err) {
                        try {
                            if (callback != null) {
                                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                                    err.getError_code(), (int) err.getHttp_code(),
                                    (int) err.getServer_code(), (int) err.getDevice_code(),
                                    err.getServer_message()));
                            }
                        } catch (Exception e) {
                            Logger.error("OnboardAiModule", "drc action callback error: " + e.getMessage(), e);
                        } finally {
                            aiAdvancedCallbackRef.remove(id);
                        }
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (aiAdvancedCallbackRef) {
                aiAdvancedCallbackRef.put(id, container);
                nativeCall.accept(swigCb);
            }
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "drc action error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        }
    }

    // ── AI Spotlight Zoom (Tracking) ──

    /**
     * AI跟踪开关
     * @implNote 链路: DRC | method: drc_ai_spotlight_zoom_set
     */
    public void DrcAISpotlightZoomSet(int on, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAISpotlightZoomSetWrapper(on, cb));
    }

    /**
     * AI框选目标跟随
     * @implNote 链路: DRC | method: drc_ai_spotlight_zoom_select
     */
    public void DrcAISpotlightZoomSelect(com.dji.flighthubsdk.bean.AiSpotlightZoomSelectParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.AiSpotlightZoomSelectParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeAiSpotlightZoomSelectParams(params);
            final com.dji.flighthubsdk.generated.AiSpotlightZoomSelectParams np = nativeParams;
            invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAISpotlightZoomSelectWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "DrcAISpotlightZoomSelect error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * AI跟随确认
     * @implNote 链路: DRC | method: drc_ai_spotlight_zoom_confirm
     */
    public void DrcAISpotlightZoomConfirm(FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAISpotlightZoomConfirmWrapper(cb));
    }

    /**
     * 停止AI目标跟随
     * @implNote 链路: DRC | method: drc_ai_spotlight_zoom_stop
     */
    public void DrcAISpotlightZoomStop(FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAISpotlightZoomStopWrapper(cb));
    }

    /**
     * AI识别目标跟随
     * @implNote 链路: DRC | method: drc_ai_spotlight_zoom_track
     */
    public void DrcAISpotlightZoomTrack(int targetIndex, FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAISpotlightZoomTrackWrapper(targetIndex, cb));
    }

    // ── AI Alarm ──

    /**
     * 开启AI主动告警
     * @param params AI 告警开启参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_ai_alarm_start
     */
    public void DrcAIAlarmStart(com.dji.flighthubsdk.bean.AiAlarmStartParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.AiAlarmStartParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeAiAlarmStartParams(params);
            final com.dji.flighthubsdk.generated.AiAlarmStartParams np = nativeParams;
            invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAIAlarmStartWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "DrcAIAlarmStart error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    /**
     * 关闭AI主动告警
     * @implNote 链路: DRC | method: drc_ai_alarm_stop
     */
    public void DrcAIAlarmStop(FlightHubCallback.ResultCallbackWithError callback) {
        invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAIAlarmStopWrapper(cb));
    }

    /**
     * AI模型配置
     * @param params AI 模型配置参数
     * @param callback 结果回调
     * @implNote 链路: DRC | method: drc_ai_models_config
     */
    public void DrcAIModelsConfig(com.dji.flighthubsdk.bean.AiModelsConfigParams params, FlightHubCallback.ResultCallbackWithError callback) {
        com.dji.flighthubsdk.generated.AiModelsConfigParams nativeParams = null;
        try {
            nativeParams = FlightHubSdkConvert.toNativeAiModelsConfigParams(params);
            final com.dji.flighthubsdk.generated.AiModelsConfigParams np = nativeParams;
            invokeDrcAiAction(callback, cb -> this.onboardAiModule.DrcAIModelsConfigWrapper(np, cb));
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "DrcAIModelsConfig error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(new com.dji.flighthubsdk.bean.FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            }
        } finally {
            if (nativeParams != null) nativeParams.delete();
        }
    }

    // ── Observe: AI Alarm State ──

    /**
     * 订阅AI告警状态推送
     * @implNote 链路: DRC Northbound | method: drc_ai_alarm_state_push
     */
    public int listenDrcAIAlarmState(FlightHubCallback.AiAlarmStateCallback callback) {
        try {
            com.dji.flighthubsdk.generated.AiAlarmStateCallback swigCb =
                new com.dji.flighthubsdk.generated.AiAlarmStateCallback() {
                    @Override
                    public void onAiAlarmState(com.dji.flighthubsdk.generated.AiAlarmStateInfo info) {
                        if (callback != null) callback.onResult(info);
                    }
                };
            CallbackRefContainer container = new CallbackRefContainer(swigCb, callback);
            synchronized (aiAlarmStateCallbackRef) {
                aiAlarmStateCallbackRef.put(-1, container);
                int listenId = this.onboardAiModule.listenDrcAIAlarmStateWrapper(swigCb);
                aiAlarmStateCallbackRef.remove(-1);
                if (listenId >= 0) {
                    aiAlarmStateCallbackRef.put(listenId, container);
                } else {
                    container.release();
                }
                return listenId;
            }
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "listenDrcAIAlarmState error: " + e.getMessage(), e);
            return -1;
        }
    }

    /** 取消订阅AI告警状态推送 */
    public void cancelListenDrcAIAlarmState(int listenId) {
        try {
            this.onboardAiModule.cancelListenDrcAIAlarmStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("OnboardAiModule", "cancelListenDrcAIAlarmState error: " + e.getMessage(), e);
        }
        CallbackRefContainer removed = aiAlarmStateCallbackRef.remove(listenId);
        if (removed != null) removed.release();
    }
}
