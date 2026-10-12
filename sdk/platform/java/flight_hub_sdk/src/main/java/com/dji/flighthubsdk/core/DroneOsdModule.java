// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.IDroneOsdModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * 飞行器 OSD 数据订阅模块（WebSocket NotificationCenter + HSI）
 */
public class DroneOsdModule {

    private final IDroneOsdModule droneOsdModule;
    private final String sn;

    public DroneOsdModule(IDroneOsdModule droneOsdModule, String sn) {
        this.droneOsdModule = droneOsdModule;
        this.sn = sn;
    }

    // 飞机负载 OSD 信息列表({type-subtype-gimbalindex} + payloads);
    // AircraftPayloadsOsdInfoList;
    /**
     * 订阅飞机负载 OSD 信息列表
     * <p><b>注意：当前版本后端暂不支持 topic_drone_payloads_osd_info 推送，调用后无法收到回调。</b></p>
     * <p>替代方案：</p>
     * <ul>
     *   <li>云台姿态 → {@link #listenAircraftPayloadsGimbalAttitude(FlightHubCallback.AircraftPayloadsGimbalAttitudeCallback)}</li>
     *   <li>激光测距 → {@code CameraControlModule.listenDrcCameraMeasureTarget()}</li>
     *   <li>变焦倍率/相机参数 → {@link #listenAircraftPayloadsCamerasList(FlightHubCallback.AircraftPayloadsCamerasListCallback)}</li>
     * </ul>
     * @param callback 飞机负载 OSD 信息列表回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_payloads_osd_info
     */
    public int listenAircraftPayloadsOsdInfo(FlightHubCallback.AircraftPayloadsOsdInfoListCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftPayloadsOsdInfoListCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftPayloadsOsdInfoListCallback() {
                @Override
                public void onAircraftPayloadsOsdInfoList(com.dji.flighthubsdk.generated.AircraftPayloadsOsdInfoList data) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftPayloadsOsdInfoList(data));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] AircraftPayloadsOsdInfoList callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftPayloadsOsdInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPayloadsOsdInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftPayloadsOsdInfoList(null));
            }
        }

        return -1;
    }

    /**
     * 取消订阅飞机负载 OSD 信息列表
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftPayloadsOsdInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftPayloadsOsdInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftPayloadsOsdInfo() error: " + e.getMessage(), e);
        }
    }


    // 监听: 飞机档位信息(gear);
    // AircraftGear;
    /**
     * 订阅飞机档位信息
     * @param callback 飞机档位信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_gear
     */
    public int listenAircraftGear(FlightHubCallback.AircraftGearCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftGearCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftGearCallback() {
                @Override
                public void onAircraftGear(com.dji.flighthubsdk.generated.AircraftGear data) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftGear(data));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] AircraftPayloadsOsdInfoList error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftGearWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftGear() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftGear(null));
            }
        }

        return -1;
    }


    // 取消监听: 飞机档位信息(gear);
    /**
     * 取消订阅飞机档位信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftGear(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftGearWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftGear() error: " + e.getMessage(), e);
        }
    }


    // 监听: 飞机 Home点距离(home_distance);
    // AircraftHomeDistance;
    /**
     * 订阅飞机 Home 点距离
     * @param callback Home 点距离回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_home_distance
     */
    public int listenAircraftHomeDistance(FlightHubCallback.AircraftHomeDistanceCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftHomeDistanceCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftHomeDistanceCallback() {
                @Override
                public void onAircraftHomeDistance(com.dji.flighthubsdk.generated.AircraftHomeDistance homeDistance) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftHomeDistance(homeDistance));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftHomeDistance() error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftHomeDistanceWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftHomeDistance() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftHomeDistance(null));
            }
        }

        return -1;
    }

    // 取消监听: 飞机 Home点距离(home_distance);
    /**
     * 取消订阅飞机 Home 点距离
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftHomeDistance(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftHomeDistanceWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftHomeDistance() error: " + e.getMessage(), e);
        }
    }


    // 飞机: 搜星状态(position_state);
    // AircraftPositionState;
    /**
     * 订阅飞机搜星状态
     * @param callback 搜星状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_position_state
     */
    public int listenAircraftPositionState(FlightHubCallback.AircraftPositionStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftPositionStateCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftPositionStateCallback() {
                @Override
                public void onAircraftPositionState(com.dji.flighthubsdk.generated.AircraftPositionState state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftPositionState(state));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPositionState() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftPositionStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPositionState() error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftPositionState(null));
            }
        }

        return -1;
    }


    // 取消监听: 飞机: 搜星状态(position_state);
    /**
     * 取消订阅飞机搜星状态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftPositionState(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftPositionStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftPositionState() error: " + e.getMessage(), e);
        }
    }


    // 飞机: 当前控制源(control_source);
    // AircraftControlSource;
    /**
     * 订阅飞机当前控制源
     * @param callback 当前控制源回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_control_source
     */
    public int listenAircraftControlSource(FlightHubCallback.AircraftControlSourceCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftControlSourceCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftControlSourceCallback() {
                @Override
                public void onAircraftControlSource(com.dji.flighthubsdk.generated.AircraftControlSource state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftControlSource(state));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftControlSource() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftControlSourceWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftControlSource()  error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftControlSource(null));
            }
        }

        return -1;
    }


    // 取消监听: 飞机当前控制源(control_source);
    // AircraftControlSource;
    /**
     * 取消订阅飞机当前控制源
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftControlSource(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftControlSourceWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftControlSource()  error: " + e.getMessage(), e);
        }
    }


    // 监听: 飞机限制类信息状态;
    // AircraftLimitsInfo;
    /**
     * 订阅飞机限制类信息状态
     * @param callback 限制类信息状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_limits_info
     */
    public int listenAircraftLimitsInfo(FlightHubCallback.AircraftLimitsInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftLimitsInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftLimitsInfoCallback() {
                @Override
                public void onAircraftLimitsInfo(com.dji.flighthubsdk.generated.AircraftLimitsInfo info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftLimitsInfo(info));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftLimitsInfo() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftLimitsInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftLimitsInfo()  error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftLimitsInfo(null));
            }
        }

        return -1;
    }


    // 取消监听: 飞机限制类信息状态;
    /**
     * 取消订阅飞机限制类信息状态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftLimitsInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftLimitsInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftLimitsInfo() error: " + e.getMessage(), e);
        }
    }


    // 飞机: 限制类信息状态;
    // AircraftLimitsInfo;
    /**
     * 订阅电量告警信息（用户设置的阈值）
     * @param callback 电量告警信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_battery_warning_thresholds
     */
    public int listenAircraftBatteryWarningThresholds(FlightHubCallback.AircraftBatteryWarningThresholdsCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftBatteryWarningThresholdsCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftBatteryWarningThresholdsCallback() {
                @Override
                public void onAircraftBatteryWarningThresholds(com.dji.flighthubsdk.generated.AircraftBatteryWarningThresholds thresholds) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftBatteryWarningThresholds(thresholds));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftBatteryWarningThresholds() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftBatteryWarningThresholdsWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftBatteryWarningThresholds() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftBatteryWarningThresholds(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅电量告警信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftBatteryWarningThresholds(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftBatteryWarningThresholdsWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftBatteryWarningThresholds() error: " + e.getMessage(), e);
        }
    }


    // 监听: 固件状态信息;
    // AircraftFirmwareStatus;
    /**
     * 订阅固件状态信息
     * @param callback 固件状态信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_firmware_status
     */
    public int listenAircraftFirmwareStatus(FlightHubCallback.AircraftFirmwareStatusCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftFirmwareStatusCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftFirmwareStatusCallback() {
                @Override
                public void onAircraftFirmwareStatus(com.dji.flighthubsdk.generated.AircraftFirmwareStatus firmwareStatus) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftFirmwareStatus(firmwareStatus));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftFirmwareStatus() error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftFirmwareStatusWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftFirmwareStatus() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftFirmwareStatus(null));
            }
        }

        return -1;
    }


    // 取消监听: 固件状态信息;
    /**
     * 取消订阅固件状态信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftFirmwareStatus(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftFirmwareStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftFirmwareStatus() error: " + e.getMessage(), e);
        }
    }


    // 返航高度模式当前值(current_rth_mode);
    // AircraftCurrentRthMode;
    /**
     * 订阅返航高度模式当前值
     * @param callback 返航高度模式当前值回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_current_rth_mode
     */
    public int listenAircraftCurrentRthMode(FlightHubCallback.AircraftCurrentRthModeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftCurrentRthModeCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftCurrentRthModeCallback() {
                @Override
                public void onAircraftCurrentRthMode(com.dji.flighthubsdk.generated.AircraftCurrentRthMode rthMode) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftCurrentRthMode(rthMode));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftCurrentRthMode() error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftCurrentRthModeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftCurrentRthMode() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftCurrentRthMode(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅返航高度模式当前值
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftCurrentRthMode(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftCurrentRthModeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftCurrentRthMode() error: " + e.getMessage(), e);
        }
    }


    // 监听: 累计飞行信息(total_flight_info);
    // AircraftTotalFlightInfo;
    /**
     * 订阅累计飞行信息
     * @param callback 累计飞行信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_total_flight_info
     */
    public int listenAircraftTotalFlightInfo(FlightHubCallback.AircraftTotalFlightInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftTotalFlightInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftTotalFlightInfoCallback() {
                @Override
                public void onAircraftTotalFlightInfo(com.dji.flighthubsdk.generated.AircraftTotalFlightInfo firmwareStatus) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftTotalFlightInfo(firmwareStatus));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftTotalFlightInfo() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftTotalFlightInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftTotalFlightInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftTotalFlightInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅累计飞行信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftTotalFlightInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftTotalFlightInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftTotalFlightInfo() error: " + e.getMessage(), e);
        }
    }


    // 监听: 飞行器激活时间(unix时间戳)(activation_time);
    // AircraftActivationTime;
    /**
     * 订阅飞行器激活时间
     * @param callback 飞行器激活时间回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_activation_time
     */
    public int listenAircraftActivationTime(FlightHubCallback.AircraftActivationTimeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftActivationTimeCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftActivationTimeCallback() {
                @Override
                public void onAircraftActivationTime(com.dji.flighthubsdk.generated.AircraftActivationTime firmwareStatus) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftActivationTime(firmwareStatus));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftActivationTime() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftActivationTimeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftActivationTime() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftActivationTime(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅飞行器激活时间
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftActivationTime(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftActivationTimeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftActivationTime() error: " + e.getMessage(), e);
        }
    }


    // 飞机：保养信息(maintain_status);
    // AircraftMaintainStatusInfo;
    /**
     * 订阅飞机保养信息
     * @param callback 保养信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_maintain_status_info
     */
    public int listenAircraftMaintainStatusInfo(FlightHubCallback.AircraftMaintainStatusInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftMaintainStatusInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftMaintainStatusInfoCallback() {
                @Override
                public void onAircraftMaintainStatusInfo(com.dji.flighthubsdk.generated.AircraftMaintainStatusInfo maintainStatusInfo) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftMaintainStatusInfo(maintainStatusInfo));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftMaintainStatusInfo() error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftMaintainStatusInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftMaintainStatusInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftMaintainStatusInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅飞机保养信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftMaintainStatusInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftMaintainStatusInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftMaintainStatusInfo() error: " + e.getMessage(), e);
        }
    }


    // 飞行器夜航灯状态(night_lights_state);
    // AircraftNightLightsState;
    /**
     * 订阅飞行器夜航灯状态
     * @param callback 夜航灯状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_night_lights_state
     */
    public int listenAircraftNightLightsState(FlightHubCallback.AircraftNightLightsStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftNightLightsStateCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftNightLightsStateCallback() {
                @Override
                public void onAircraftNightLightsState(com.dji.flighthubsdk.generated.AircraftNightLightsState state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftNightLightsState(state));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftNightLightsState() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftNightLightsStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftNightLightsState() callback error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftNightLightsState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅飞行器夜航灯状态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftNightLightsState(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftNightLightsStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftNightLightsState() error: " + e.getMessage(), e);
        }
    }


    // 飞行器避障状态(obstacle_avoidance);
    // AircraftObstacleAvoidanceState;
    /**
     * 订阅飞行器避障状态
     * @param callback 避障状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_obstacle_avoidance_state
     */
    public int listenAircraftObstacleAvoidanceState(FlightHubCallback.AircraftObstacleAvoidanceStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftObstacleAvoidanceStateCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftObstacleAvoidanceStateCallback() {
                @Override
                public void onAircraftObstacleAvoidanceState(com.dji.flighthubsdk.generated.AircraftObstacleAvoidanceState state) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftObstacleAvoidanceState(state));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftObstacleAvoidanceState() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftObstacleAvoidanceStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftObstacleAvoidanceState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftObstacleAvoidanceState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅飞行器避障状态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftObstacleAvoidanceState(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftObstacleAvoidanceStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftObstacleAvoidanceState() error: " + e.getMessage(), e);
        }
    }


    // 存储容量(storage);
    // AircraftStorageInfo;
    /**
     * 订阅存储容量信息
     * @param callback 存储容量信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_storage_info
     */
    public int listenAircraftStorageInfo(FlightHubCallback.AircraftStorageInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftStorageInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftStorageInfoCallback() {
                @Override
                public void onAircraftStorageInfo(com.dji.flighthubsdk.generated.AircraftStorageInfo info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftStorageInfo(info));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftStorageInfo() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftStorageInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftStorageInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftStorageInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅存储容量信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftStorageInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftStorageInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftStorageInfo() error: " + e.getMessage(), e);
        }
    }


    // 航迹id(track_id);
    // AircraftTrackId;
    /**
     * 订阅航迹 ID
     * @param callback 航迹 ID 回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_track_id
     */
    public int listenAircraftTrackId(FlightHubCallback.AircraftTrackIdCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftTrackIdCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftTrackIdCallback() {
                @Override
                public void onAircraftTrackId(com.dji.flighthubsdk.generated.AircraftTrackId info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftTrackId(info));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftTrackId() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftTrackIdWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftTrackId() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftTrackId(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅航迹 ID
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftTrackId(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftTrackIdWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftTrackId() error: " + e.getMessage(), e);
        }
    }


    // 合规相关信息(compliance information);
    // AircraftComplianceInfo;
    /**
     * 订阅合规相关信息
     * @param callback 合规相关信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_compliance_info
     */
    public int listenAircraftComplianceInfo(FlightHubCallback.AircraftComplianceInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftComplianceInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftComplianceInfoCallback() {
                @Override
                public void onAircraftComplianceInfo(com.dji.flighthubsdk.generated.AircraftComplianceInfo info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftComplianceInfo(info));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftComplianceInfo() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftComplianceInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftComplianceInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftComplianceInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅合规相关信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftComplianceInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftComplianceInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftComplianceInfo() error: " + e.getMessage(), e);
        }
    }


    // 监听: 飞行器相机信息(cameras);
    // AircraftPayloadsCamerasList;
    /**
     * 订阅飞行器相机信息
     * @param callback 飞行器相机信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_payloads_cameras_list
     */
    public int listenAircraftPayloadsCamerasList(FlightHubCallback.AircraftPayloadsCamerasListCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftPayloadsCamerasListCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftPayloadsCamerasListCallback() {
                @Override
                public void onAircraftPayloadsCamerasList(com.dji.flighthubsdk.generated.AircraftPayloadsCamerasList info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftPayloadsCamerasList(info));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPayloadsCamerasList() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftPayloadsCamerasListWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPayloadsCamerasList() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftPayloadsCamerasList(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅飞行器相机信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftPayloadsCamerasList(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftPayloadsCamerasListWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftPayloadsCamerasList() error: " + e.getMessage(), e);
        }
    }


    // 遥控器失控动作(rc_lost_action);
    // AircraftRcLostAction;
    /**
     * 订阅遥控器失控动作
     * @param callback 遥控器失控动作回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_rc_lost_action
     */
    public int listenAircraftRcLostAction(FlightHubCallback.AircraftRcLostActionCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftRcLostActionCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftRcLostActionCallback() {
                @Override
                public void onAircraftRcLostAction(com.dji.flighthubsdk.generated.AircraftRcLostAction action) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftRcLostAction(action));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftRcLostAction() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftRcLostActionWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftRcLostAction() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftRcLostAction(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅遥控器失控动作
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftRcLostAction(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftRcLostActionWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftRcLostAction() error: " + e.getMessage(), e);
        }
    }


    // 返航信息(返航模式, 返航高度);
    // AircraftRthInfo;
    /**
     * 订阅返航信息（返航模式、返航高度）
     * @param callback 返航信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_rth_info
     */
    public int listenAircraftRthInfo(FlightHubCallback.AircraftRthInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftRthInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftRthInfoCallback() {
                @Override
                public void onAircraftRthInfo(com.dji.flighthubsdk.generated.AircraftRthInfo action) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftRthInfo(action));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftRthInfo() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftRthInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftRthInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftRthInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅返航信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftRthInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftRthInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftRthInfo() error: " + e.getMessage(), e);
        }
    }


    // 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
    // AircraftHomeGeographicalInfo;
    /**
     * 订阅飞机 Home 点地理坐标信息
     * @param callback Home 点地理坐标信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_home_geographical_info
     */
    public int listenAircraftHomeGeographicalInfo(FlightHubCallback.AircraftHomeGeographicalInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftHomeGeographicalInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftHomeGeographicalInfoCallback() {
                @Override
                public void onAircraftHomeGeographicalInfo(com.dji.flighthubsdk.generated.AircraftHomeGeographicalInfo action) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftHomeGeographicalInfo(action));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftHomeGeographicalInfo() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftHomeGeographicalInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftHomeGeographicalInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftHomeGeographicalInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅飞机 Home 点地理坐标信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftHomeGeographicalInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftHomeGeographicalInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftHomeGeographicalInfo() error: " + e.getMessage(), e);
        }
    }
    /**
     * 订阅飞机电池信息
     * @param callback 电池信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_battery_info
     */
    public int listenAircraftBatteryInfo(FlightHubCallback.AircraftBatteryInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftBatteryInfoCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftBatteryInfoCallback() {
                @Override
                public void onAircraftBatteryInfo(com.dji.flighthubsdk.generated.AircraftBatteryInfo aircraft_battery_info) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftBatteryInfo(aircraft_battery_info));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] Aircraft battery info callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            int listenId = this.droneOsdModule.listenAircraftBatteryInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftBatteryInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftBatteryInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机电池信息
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftBatteryInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftBatteryInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftBatteryInfo error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机姿态
     * @param callback 飞机姿态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_attitude
     */
    public int listenAircraftAttitude(FlightHubCallback.AircraftAttitudeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftAttitudeCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftAttitudeCallback() {
                @Override
                public void onAircraftAttitude(com.dji.flighthubsdk.generated.AircraftAttitude aircraft_attitude) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftAttitude(aircraft_attitude));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] Aircraft attitude callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            int listenId = this.droneOsdModule.listenAircraftAttitudeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftAttitude error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftAttitude(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机姿态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftAttitude(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftAttitudeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftAttitude error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机位置
     * @param callback 飞机位置回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_location
     */
    public int listenAircraftLocation(FlightHubCallback.AircraftLocationCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftLocationCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftLocationCallback() {
                @Override
                public void onAircraftLocation(com.dji.flighthubsdk.generated.AircraftLocation aircraft_location) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftLocation(aircraft_location));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] Aircraft location callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            int listenId = this.droneOsdModule.listenAircraftLocationWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftLocation error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftLocation(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机位置
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftLocation(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftLocationWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftLocation error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机模式码
     * @param callback 飞机模式码回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_mode_code
     */
    public int listenAircraftModeCode(FlightHubCallback.AircraftModeCodeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftModeCodeCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftModeCodeCallback() {
                @Override
                public void onAircraftModeCode(com.dji.flighthubsdk.generated.AircraftModeCode aircraft_mode_code) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftModeCode(aircraft_mode_code));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] Aircraft mode code callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            int listenId = this.droneOsdModule.listenAircraftModeCodeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftModeCode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftModeCode(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机模式码
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftModeCode(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftModeCodeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftModeCode error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机风速
     * @param callback 飞机风速回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_wind_speed
     */
    public int listenAircraftWindSpeed(FlightHubCallback.AircraftWindSpeedCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftWindSpeedCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftWindSpeedCallback() {
                @Override
                public void onAircraftWindSpeed(com.dji.flighthubsdk.generated.AircraftWindSpeed aircraft_wind_speed) {
                    try {
                        // 调用Java回调
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftWindSpeed(aircraft_wind_speed));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] Aircraft wind speed callback error: " + e.getMessage(), e);
                    }
                }
            };
            
            int listenId = this.droneOsdModule.listenAircraftWindSpeedWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftWindSpeed error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftWindSpeed(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机风速
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftWindSpeed(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftWindSpeedWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftWindSpeed error: " + e.getMessage(), e);
        }
    }
    // ==================== 飞行任务异常和设备变更相关接口 ====================

    /**
     * 订阅飞行任务异常
     * @param callback 飞行任务异常回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_task_flight_exception
     */
    public int listenFlightTasksException(FlightHubCallback.FlightTaskExceptionCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.FlightTaskExceptionCallback swigCallback = new com.dji.flighthubsdk.generated.FlightTaskExceptionCallback() {
                @Override
                public void onFlightTaskException(com.dji.flighthubsdk.generated.FlightTaskException exception) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toFlightTaskException(exception));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] Flight task exception callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenFlightTasksExceptionWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenFlightTasksException error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toFlightTaskException(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞行任务异常
     * @param listenId 监听 ID
     */
    public void cancelListenFlightTasksException(int listenId) {
        try {
            this.droneOsdModule.cancelListenFlightTasksExceptionWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenFlightTasksException error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机控制码
     * @param callback 飞机控制码回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_control_code
     */
    public int listenAircraftControlCode(FlightHubCallback.AircraftControlCodeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftControlCodeCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftControlCodeCallback() {
                @Override
                public void onAircraftControlCode(com.dji.flighthubsdk.generated.AircraftControlCode control_code) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toAircraftControlCode(control_code));
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftControlCodeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftControlCode error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftControlCode(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机控制码
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftControlCode(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftControlCodeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftControlCode error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机速度
     * @param callback 飞机速度回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_speed
     */
    public int listenAircraftSpeed(FlightHubCallback.AircraftSpeedCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftSpeedCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftSpeedCallback() {
                @Override
                public void onAircraftSpeed(com.dji.flighthubsdk.generated.AircraftSpeed speed) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toAircraftSpeed(speed));
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftSpeedWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftSpeed error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftSpeed(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞机速度
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftSpeed(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftSpeedWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftSpeed error: " + e.getMessage(), e);
        }
    }

        /**
     * 订阅载荷相机实时视图世界区域
     * @param callback 载荷相机实时视图世界区域回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_payloads_liveview_world_region
     */
    public int listenAircraftPayloadsCameraLiveviewWorldRegion(FlightHubCallback.AircraftPayloadsCameraLiveviewWorldRegionCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftPayloadsCameraLiveviewWorldRegionCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftPayloadsCameraLiveviewWorldRegionCallback() {
                @Override
                public void onAircraftPayloadsCameraLiveviewWorldRegion(com.dji.flighthubsdk.generated.AircraftPayloadsCameraLiveviewWorldRegion region) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftPayloadsCameraLiveviewWorldRegion(region));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] AircraftPayloadsCameraLiveviewWorldRegion callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftPayloadsCameraLiveviewWorldRegionWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPayloadsCameraLiveviewWorldRegion error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftPayloadsCameraLiveviewWorldRegion(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅载荷相机实时视图世界区域
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftPayloadsCameraLiveviewWorldRegion(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftPayloadsCameraLiveviewWorldRegionWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftPayloadsCameraLiveviewWorldRegion error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅载荷云台姿态
     * @param callback 载荷云台姿态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_payloads_gimbal_attitude
     */
    public int listenAircraftPayloadsGimbalAttitude(FlightHubCallback.AircraftPayloadsGimbalAttitudeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftPayloadsGimbalAttitudeCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftPayloadsGimbalAttitudeCallback() {
                @Override
                public void onAircraftPayloadsGimbalAttitude(com.dji.flighthubsdk.generated.AircraftPayloadsGimbalAttitude attitude) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftPayloadsGimbalAttitude(attitude));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] AircraftPayloadsGimbalAttitude callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftPayloadsGimbalAttitudeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPayloadsGimbalAttitude error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftPayloadsGimbalAttitude(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅载荷云台姿态
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftPayloadsGimbalAttitude(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftPayloadsGimbalAttitudeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftPayloadsGimbalAttitude error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅载荷列表
     * @param callback 载荷列表回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_payloads_list
     */
    public int listenAircraftPayloadsList(FlightHubCallback.AircraftPayloadsListCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.AircraftPayloadsListCallback swigCallback = new com.dji.flighthubsdk.generated.AircraftPayloadsListCallback() {
                @Override
                public void onAircraftPayloadsList(com.dji.flighthubsdk.generated.AircraftPayloadsList payloads_list) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toAircraftPayloadsList(payloads_list));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] AircraftPayloadsList callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenAircraftPayloadsListWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenAircraftPayloadsList error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toAircraftPayloadsList(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅载荷列表
     * @param listenId 监听 ID
     */
    public void cancelListenAircraftPayloadsList(int listenId) {
        try {
            this.droneOsdModule.cancelListenAircraftPayloadsListWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenAircraftPayloadsList error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅控制权限云控制状态
     * @param callback 控制权限云控制状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_cloud_control_status
     */
    public int listenControlRightsCloudControlStatus(FlightHubCallback.ControlRightsCloudControlStatusCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ControlRightsCloudControlStatusCallback swigCallback = new com.dji.flighthubsdk.generated.ControlRightsCloudControlStatusCallback() {
                @Override
                public void onControlRightsCloudControlStatus(com.dji.flighthubsdk.generated.ControlRightsCloudControlStatus status) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toControlRightsCloudControlStatus(status));
                    }
                }
            };

            int listenId = this.droneOsdModule.listenControlRightsCloudControlStatusWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenControlRightsCloudControlStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toControlRightsCloudControlStatus(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅控制权限云控制状态
     * @param listenId 监听 ID
     */
    public void cancelListenControlRightsCloudControlStatus(int listenId) {
        try {
            this.droneOsdModule.cancelListenControlRightsCloudControlStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenControlRightsCloudControlStatus error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅控制权限设备控制状态
     * @param callback 控制权限设备控制状态回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_drone_cloud_control_status
     */
    public int listenControlRightsDeviceControlStatus(FlightHubCallback.ControlRightsDeviceControlStatusCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.ControlRightsDeviceControlStatusCallback swigCallback = new com.dji.flighthubsdk.generated.ControlRightsDeviceControlStatusCallback() {
                @Override
                public void onControlRightsDeviceControlStatus(com.dji.flighthubsdk.generated.ControlRightsDeviceControlStatus status) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toControlRightsDeviceControlStatus(status));
                    }
                }
            };

            int listenId = this.droneOsdModule.listenControlRightsDeviceControlStatusWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenControlRightsDeviceControlStatus error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toControlRightsDeviceControlStatus(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅控制权限设备控制状态
     * @param listenId 监听 ID
     */
    public void cancelListenControlRightsDeviceControlStatus(int listenId) {
        try {
            this.droneOsdModule.cancelListenControlRightsDeviceControlStatusWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenControlRightsDeviceControlStatus error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞行任务媒体文件显示可用
     * @param callback 飞行任务媒体文件显示可用回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: WebSocket | topic: topic_task_media_file_display_available
     */
    public int listenFlightTasksMediaFileDisplayAvailable(FlightHubCallback.FlightTasksMediaFileDisplayAvailableCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.FlightTasksMediaFileDisplayAvailableCallback swigCallback = new com.dji.flighthubsdk.generated.FlightTasksMediaFileDisplayAvailableCallback() {
                @Override
                public void onFlightTasksMediaFileDisplayAvailable(com.dji.flighthubsdk.generated.FlightTasksMediaFileDisplayAvailable available) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightTasksMediaFileDisplayAvailable(available));
                    }
                }
            };

            int listenId = this.droneOsdModule.listenFlightTasksMediaFileDisplayAvailableWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenFlightTasksMediaFileDisplayAvailable error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toFlightTasksMediaFileDisplayAvailable(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞行任务媒体文件显示可用
     * @param listenId 监听 ID
     */
    public void cancelListenFlightTasksMediaFileDisplayAvailable(int listenId) {
        try {
            this.droneOsdModule.cancelListenFlightTasksMediaFileDisplayAvailableWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenFlightTasksMediaFileDisplayAvailable error: " + e.getMessage(), e);
        }
    }


    // ==================== 飞行器 HSI 信息（障碍物感知/避障传感器数据） ====================

    /**
     * 订阅飞行器 HSI 信息（障碍物感知/避障传感器数据）
     * @param callback HSI 信息回调
     * @return 监听 ID，用于取消订阅
     * @implNote 链路: SDK 内部 | HSI 障碍物感知数据
     */
    public int listenHsiInfo(FlightHubCallback.HsiInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.HsiInfoCallback swigCallback = new com.dji.flighthubsdk.generated.HsiInfoCallback() {
                @Override
                public void onHsiInfo(com.dji.flighthubsdk.generated.HsiInfo info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toHsiInfo(info));
                        }
                    } catch (Exception e) {
                        Logger.error("DroneOsdModule", "[" + sn + "] HsiInfo callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.droneOsdModule.listenHsiInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenHsiInfo error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toHsiInfo(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅飞行器 HSI 信息
     * @param listenId 监听 ID
     */
    public void cancelListenHsiInfo(int listenId) {
        try {
            this.droneOsdModule.cancelListenHsiInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenHsiInfo error: " + e.getMessage(), e);
        }
    }

    // ===================== EA340 消息推送补全（Phase D） =====================

    /**
     * 订阅飞行器进入当前状态原因(mode_code_reason)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_mode_code_reason
     */
    public int listenDroneModeCodeReason(FlightHubCallback.DroneModeCodeReasonCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneModeCodeReasonCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneModeCodeReasonCallback() {
                        @Override
                        public void onDroneModeCodeReason(com.dji.flighthubsdk.generated.DroneModeCodeReason msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneModeCodeReason(msg));
                            }
                        }
                    };
            int listenId = this.droneOsdModule.listenDroneModeCodeReasonWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenDroneModeCodeReason error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅飞行器进入当前状态原因 */
    public void cancelListenDroneModeCodeReason(int listenId) {
        try {
            this.droneOsdModule.cancelListenDroneModeCodeReasonWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenDroneModeCodeReason error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞机4G Dongle信息(dongle_infos)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_drone_dongle_infos
     */
    public int listenDroneDongleInfos(FlightHubCallback.DroneDongleInfosCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneDongleInfosCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneDongleInfosCallback() {
                        @Override
                        public void onDroneDongleInfos(com.dji.flighthubsdk.generated.DroneDongleInfos msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneDongleInfos(msg));
                            }
                        }
                    };
            int listenId = this.droneOsdModule.listenDroneDongleInfosWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenDroneDongleInfos error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅飞机4G Dongle信息 */
    public void cancelListenDroneDongleInfos(int listenId) {
        try {
            this.droneOsdModule.cancelListenDroneDongleInfosWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenDroneDongleInfos error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅限飞数据库版本(flysafe_database_version)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_flysafe_database_version
     */
    public int listenDroneFlysafeDatabaseVersion(FlightHubCallback.DroneFlysafeDatabaseVersionCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneFlysafeDatabaseVersionCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneFlysafeDatabaseVersionCallback() {
                        @Override
                        public void onDroneFlysafeDatabaseVersion(com.dji.flighthubsdk.generated.DroneFlysafeDatabaseVersion msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneFlysafeDatabaseVersion(msg));
                            }
                        }
                    };
            int listenId = this.droneOsdModule.listenDroneFlysafeDatabaseVersionWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenDroneFlysafeDatabaseVersion error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅限飞数据库版本 */
    public void cancelListenDroneFlysafeDatabaseVersion(int listenId) {
        try {
            this.droneOsdModule.cancelListenDroneFlysafeDatabaseVersionWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenDroneFlysafeDatabaseVersion error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅航线解析库版本(wpmz_version)
     * @param callback 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_wpmz_version
     */
    public int listenDroneWpmzVersion(FlightHubCallback.DroneWpmzVersionCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DroneWpmzVersionCallback swigCallback =
                    new com.dji.flighthubsdk.generated.DroneWpmzVersionCallback() {
                        @Override
                        public void onDroneWpmzVersion(com.dji.flighthubsdk.generated.DroneWpmzVersion msg) {
                            if (callback != null) {
                                callback.onResult(FlightHubSdkConvert.toDroneWpmzVersion(msg));
                            }
                        }
                    };
            int listenId = this.droneOsdModule.listenDroneWpmzVersionWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] listenDroneWpmzVersion error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅航线解析库版本 */
    public void cancelListenDroneWpmzVersion(int listenId) {
        try {
            this.droneOsdModule.cancelListenDroneWpmzVersionWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DroneOsdModule", "[" + sn + "] cancelListenDroneWpmzVersion error: " + e.getMessage(), e);
        }
    }
}
