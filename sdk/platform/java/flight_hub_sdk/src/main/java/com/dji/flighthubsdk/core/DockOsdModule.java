// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.generated.IDockOsdModule;
import com.dji.flighthubsdk.util.Logger;

/**
 * 机场 OSD 数据订阅模块
 */
public class DockOsdModule {

    private final IDockOsdModule dockOsdModule;
    private final String sn;

    /**
     * 监听机场位置
     * @param callback 回调函数
     * @return 监听ID
     * @implNote 链路: WebSocket | topic: topic_dock_location
     */
    public int listenDockLocation(FlightHubCallback.DockLocationCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockLocationCallback swigCallback = new com.dji.flighthubsdk.generated.DockLocationCallback() {
                @Override
                public void onDockLocation(com.dji.flighthubsdk.generated.DockLocation location) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockLocation(location));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockLocationWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDockLocation error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockLocation(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅机场位置信息
     */
    public void cancelListenDockLocation(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockLocationWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDockLocation error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅机场中的无人机状态
     * @param callback 无人机在机场状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_has_drone
     */
    public int listenDroneInDock(FlightHubCallback.DroneInDockCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DroneInDockCallback swigCallback = new com.dji.flighthubsdk.generated.DroneInDockCallback() {
                @Override
                public void onDroneInDock(com.dji.flighthubsdk.generated.DroneInDock drone_in_dock) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDroneInDock(drone_in_dock));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDroneInDockWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDroneInDock error: " + e.getMessage(), e);
            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDroneInDock(null));
            }
        }
        return -1;
    }

    /**
     * 取消订阅机场中的无人机状态
     */
    public void cancelListenDroneInDock(int listenId) {
        try {
            this.dockOsdModule.cancelListenDroneInDockWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDroneInDock error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场舱盖状态
     * @param callback 舱盖状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_cover_state
     */
    public int listenDockCoverState(FlightHubCallback.DockCoverStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockCoverStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockCoverStateCallback() {
                @Override
                public void onDockCoverState(com.dji.flighthubsdk.generated.DockCoverState dock_cover_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockCoverState(dock_cover_state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockCoverStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockCoverState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockCoverState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场舱盖状态
     */
    public void cancelListenDockCoverState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockCoverStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockCoverState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场媒体文件详情
     * @param callback 媒体文件详情回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_media_file_detail
     */
    public int listenDockMediaFileDetail(FlightHubCallback.DockMediaFileDetailCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockMediaFileDetailCallback swigCallback = new com.dji.flighthubsdk.generated.DockMediaFileDetailCallback() {
                @Override
                public void onDockMediaFileDetail(com.dji.flighthubsdk.generated.DockMediaFileDetail dock_media_file_detail) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockMediaFileDetail(dock_media_file_detail));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockMediaFileDetailWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockMediaFileDetail() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockMediaFileDetail(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场媒体文件详情
     */
    public void cancelListenDockMediaFileDetail(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockMediaFileDetailWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockMediaFileDetail() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场图传链路质量
     * @param callback 图传质量回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_wireless_link
     */
    public int listenDockWirelessLink(FlightHubCallback.DockWirelessLinkCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockWirelessLinkCallback swigCallback = new com.dji.flighthubsdk.generated.DockWirelessLinkCallback() {
                @Override
                public void onDockWirelessLink(com.dji.flighthubsdk.generated.DockWirelessLink dock_wireless_link) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockWirelessLink(dock_wireless_link));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockWirelessLinkWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockWirelessLink() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockWirelessLink(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场图传链路质量
     */
    public void cancelListenDockWirelessLink(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockWirelessLinkWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockWirelessLink() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场作业次数
     * @param callback 作业次数回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_job_number
     */
    public int listenDockJobNumber(FlightHubCallback.DockJobNumberCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockJobNumberCallback swigCallback = new com.dji.flighthubsdk.generated.DockJobNumberCallback() {
                @Override
                public void onDockJobNumber(com.dji.flighthubsdk.generated.DockJobNumber dock_job_number) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockJobNumber(dock_job_number));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockJobNumberWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockJobNumber() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockJobNumber(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场作业次数
     */
    public void cancelListenDockJobNumber(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockJobNumberWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockJobNumber() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场电源状态
     * @param callback 电源状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_power_state
     */
    public int listenDockPowerState(FlightHubCallback.DockPowerStateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockPowerStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockPowerStateCallback() {
                @Override
                public void onDockPowerState(com.dji.flighthubsdk.generated.DockPowerState dock_power_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockPowerState(dock_power_state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockPowerStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockPowerState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockPowerState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场电源状态
     */
    public void cancelListenDockPowerState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockPowerStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockPowerState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场存储容量状态
     * @param callback 存储状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_storage_state
     */
    public int listenDockStorageState(FlightHubCallback.DockStorageStateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockStorageStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockStorageStateCallback() {
                @Override
                public void onDockStorageState(com.dji.flighthubsdk.generated.DockStorageState dock_storage_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockStorageState(dock_storage_state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockStorageStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockStorageState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockStorageState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场存储容量状态
     */
    public void cancelListenDockStorageState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockStorageStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockStorageState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场首次上电时间
     * @param callback 首次上电时间回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_first_power_on_time
     */
    public int listenDockFirstPowerOnTime(FlightHubCallback.DockFirstPowerOnTimeCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockFirstPowerOnTimeCallback swigCallback = new com.dji.flighthubsdk.generated.DockFirstPowerOnTimeCallback() {
                @Override
                public void onDockFirstPowerOnTime(com.dji.flighthubsdk.generated.DockFirstPowerOnTime dock_first_power_on_time) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockFirstPowerOnTime(dock_first_power_on_time));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockFirstPowerOnTimeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockFirstPowerOnTime() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockFirstPowerOnTime(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场首次上电时间
     */
    public void cancelListenDockFirstPowerOnTime(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockFirstPowerOnTimeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockFirstPowerOnTime() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场累计运行时长
     * @param callback 累计运行时长回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_accumulated_running_time
     */
    public int listenDockAccumulatedRunningTime(FlightHubCallback.DockAccTimeCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockAccTimeCallback swigCallback = new com.dji.flighthubsdk.generated.DockAccTimeCallback() {
                @Override
                public void onDockAccTime(com.dji.flighthubsdk.generated.DockAccTime dock_acc_time) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockAccTime(dock_acc_time));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockAccumulatedRunningTimeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockAccumulatedRunningTime() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockAccTime(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场累计运行时长
     */
    public void cancelListenDockAccumulatedRunningTime(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockAccumulatedRunningTimeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockAccumulatedRunningTime() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场备降点
     * @param callback 备降点回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_alternate_land_point
     */
    public int listenDockAlternateLandPoint(FlightHubCallback.DockAlternateLandPointCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockAlternateLandPointCallback swigCallback = new com.dji.flighthubsdk.generated.DockAlternateLandPointCallback() {
                @Override
                public void onDockAlternateLandPoint(com.dji.flighthubsdk.generated.DockAlternateLandPoint land_point) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockAlternateLandPoint(land_point));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockAlternateLandPointWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockAlternateLandPoint() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockAlternateLandPoint(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场备降点
     */
    public void cancelListenDockAlternateLandPoint(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockAlternateLandPointWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockAlternateLandPoint() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场搜星状态
     * @param callback 搜星状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_position_state
     */
    public int listenDockPositionState(FlightHubCallback.DockPositionStateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockPositionStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockPositionStateCallback() {
                @Override
                public void onDockPositionState(com.dji.flighthubsdk.generated.DockPositionState dock_position_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockPositionState(dock_position_state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockPositionStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockPositionState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockPositionState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场搜星状态
     */
    public void cancelListenDockPositionState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockPositionStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockPositionState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场激活时间
     * @param callback 激活时间回调（Unix 时间戳，秒）
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_activation_time
     */
    public int listenDockActivationTime(FlightHubCallback.DockActivationTimeCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockActivationTimeCallback swigCallback = new com.dji.flighthubsdk.generated.DockActivationTimeCallback() {
                @Override
                public void onDockActivationTime(com.dji.flighthubsdk.generated.DockActivationTime dock_activation_time) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockActivationTime(dock_activation_time));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockActivationTimeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockActivationTime() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockActivationTime(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场激活时间
     */
    public void cancelListenDockActivationTime(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockActivationTimeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockActivationTime() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场电池存储模式
     * @param callback 电池存储模式回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_battery_store_mode
     */
    public int listenDockBatteryStoreMode(FlightHubCallback.DockBatteryStoreModeCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockBatteryStoreModeCallback swigCallback = new com.dji.flighthubsdk.generated.DockBatteryStoreModeCallback() {
                @Override
                public void onDockBatteryStoreMode(com.dji.flighthubsdk.generated.DockBatteryStoreMode dock_battery_store_mode) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockBatteryStoreMode(dock_battery_store_mode));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockBatteryStoreModeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockBatteryStoreMode() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockBatteryStoreMode(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场电池存储模式
     */
    public void cancelListenDockBatteryStoreMode(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockBatteryStoreModeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockBatteryStoreMode() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场声光报警状态
     * @param callback 报警状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_alarm_state
     */
    public int listenDockAlarmState(FlightHubCallback.DockAlarmStateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockAlarmStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockAlarmStateCallback() {
                @Override
                public void onDockAlarmState(com.dji.flighthubsdk.generated.DockAlarmState dock_alarm_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockAlarmState(dock_alarm_state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockAlarmStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockAlarmState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockAlarmState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场声光报警状态
     */
    public void cancelListenDockAlarmState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockAlarmStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockAlarmState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场飞行器电池保养信息
     * @param callback 电池保养信息回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_drone_battery_maintenance_info
     */
    public int listenDockDroneBatteryMaintenanceInfo(FlightHubCallback.DockDroneBatteryMaintenanceInfoCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockDroneBatteryMaintenanceInfoCallback swigCallback = new com.dji.flighthubsdk.generated.DockDroneBatteryMaintenanceInfoCallback() {
                @Override
                public void onDockDroneBatteryMaintenanceInfo(com.dji.flighthubsdk.generated.DockDroneBatteryMaintenanceInfo info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockDroneBatteryMaintenanceInfo(info));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockDroneBatteryMaintenanceInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockDroneBatteryMaintenanceInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockDroneBatteryMaintenanceInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场飞行器电池保养信息
     */
    public void cancelListenDockDroneBatteryMaintenanceInfo(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockDroneBatteryMaintenanceInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockDroneBatteryMaintenanceInfo() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场紧急停止按钮状态
     * @param callback 紧急停止状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_emergency_stop_state
     */
    public int listenDockEmergencyStopState(FlightHubCallback.DockEmergencyStopStateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockEmergencyStopStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockEmergencyStopStateCallback() {
                @Override
                public void onDockEmergencyStopState(com.dji.flighthubsdk.generated.DockEmergencyStopState state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockEmergencyStopState(state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockEmergencyStopStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockEmergencyStopState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockEmergencyStopState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场紧急停止按钮状态
     */
    public void cancelListenDockEmergencyStopState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockEmergencyStopStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockEmergencyStopState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场静音模式
     * @param callback 静音模式回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_silent_mode
     */
    public int listenDockSilentMode(FlightHubCallback.DockSilentModeCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockSilentModeCallback swigCallback = new com.dji.flighthubsdk.generated.DockSilentModeCallback() {
                @Override
                public void onDockSilentMode(com.dji.flighthubsdk.generated.DockSilentMode state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockSilentMode(state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockSilentModeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockSilentMode() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockSilentMode(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场静音模式
     */
    public void cancelListenDockSilentMode(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockSilentModeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockSilentMode() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场用户体验改善计划状态
     * @param callback 用户体验改善计划回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_user_experience_improvement
     */
    public int listenDockUserExperienceImprovement(FlightHubCallback.DockUserExperienceImprovementCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockUserExperienceImprovementCallback swigCallback = new com.dji.flighthubsdk.generated.DockUserExperienceImprovementCallback() {
                @Override
                public void onDockUserExperienceImprovement(com.dji.flighthubsdk.generated.DockUserExperienceImprovement user_experience_improvement) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockUserExperienceImprovement(user_experience_improvement));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockUserExperienceImprovementWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockUserExperienceImprovement() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockUserExperienceImprovement(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场用户体验改善计划状态
     */
    public void cancelListenDockUserExperienceImprovement(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockUserExperienceImprovementWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockUserExperienceImprovement() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场 DRC 链路状态
     * @param callback DRC 链路状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_drc_state
     */
    public int listenDockDrcState(FlightHubCallback.DockDrcStateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockDrcStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockDrcStateCallback() {
                @Override
                public void onDockDrcState(com.dji.flighthubsdk.generated.DockDrcState state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockDrcState(state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockDrcStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockDrcState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockDrcState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场 DRC 链路状态
     */
    public void cancelListenDockDrcState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockDrcStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockDrcState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场推杆状态
     * @param callback 推杆状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_putter_state
     */
    public int listenDockPutterState(FlightHubCallback.DockPutterStateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockPutterStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockPutterStateCallback() {
                @Override
                public void onDockPutterState(com.dji.flighthubsdk.generated.DockPutterState state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockPutterState(state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockPutterStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockPutterState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockPutterState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场推杆状态
     */
    public void cancelListenDockPutterState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockPutterStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockPutterState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场相对备降点
     * @param callback 相对备降点回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_relative_alternate_land_point
     */
    public int listenDockRelativeAlternateLandPoint(FlightHubCallback.DockRelativeAlternateLandPointCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockRelativeAlternateLandPointCallback swigCallback = new com.dji.flighthubsdk.generated.DockRelativeAlternateLandPointCallback() {
                @Override
                public void onDockRelativeAlternateLandPoint(com.dji.flighthubsdk.generated.DockRelativeAlternateLandPoint state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockRelativeAlternateLandPoint(state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockRelativeAlternateLandPointWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockRelativeAlternateLandPoint() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockRelativeAlternateLandPoint(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场相对备降点
     */
    public void cancelListenDockRelativeAlternateLandPoint(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockRelativeAlternateLandPointWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockRelativeAlternateLandPoint() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场自收敛坐标
     * @param callback 自收敛坐标回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_self_converge_coordinate
     */
    public int listenDockSelfConvergeCoordinate(FlightHubCallback.DockSelfConvergeCoordinateCallback callback) {
        try {
            // 创建SWIG回调对象;
            com.dji.flighthubsdk.generated.DockSelfConvergeCoordinateCallback swigCallback = new com.dji.flighthubsdk.generated.DockSelfConvergeCoordinateCallback() {
                @Override
                public void onDockSelfConvergeCoordinate(com.dji.flighthubsdk.generated.DockSelfConvergeCoordinate coordinate) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockSelfConvergeCoordinate(coordinate));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockSelfConvergeCoordinateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockSelfConvergeCoordinate() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockSelfConvergeCoordinate(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场自收敛坐标
     */
    public void cancelListenDockSelfConvergeCoordinate(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockSelfConvergeCoordinateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockSelfConvergeCoordinate() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场固件信息
     * @param callback 固件信息回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_firmware_info
     */
    public int listenDockFirmwareInfo(FlightHubCallback.DockFirmwareInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockFirmwareInfoCallback swigCallback = new com.dji.flighthubsdk.generated.DockFirmwareInfoCallback() {
                @Override
                public void onDockFirmwareInfo(com.dji.flighthubsdk.generated.DockFirmwareInfo firmware_info) {
                    try {
                        if (callback != null) {
                            callback.onResult(FlightHubSdkConvert.toDockFirmwareInfo(firmware_info));
                        }
                    } catch (Exception e) {
                        Logger.error("DockOsdModule", "[" + sn + "] listenDockFirmwareInfo() callback error: " + e.getMessage(), e);
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockFirmwareInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockFirmwareInfo() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockFirmwareInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场固件信息
     */
    public void cancelListenDockFirmwareInfo(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockFirmwareInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockFirmwareInfo() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场 ModeCode
     * @param callback ModeCode 回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_mode_code
     */
    public int listenDockModeCode(FlightHubCallback.DockModeCodeCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockModeCodeCallback swigCallback = new com.dji.flighthubsdk.generated.DockModeCodeCallback() {
                @Override
                public void onDockModeCode(com.dji.flighthubsdk.generated.DockModeCode dock_mode_code) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockModeCode(dock_mode_code));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockModeCodeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDockModeCode error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockModeCode(null));
            }
        }

        return -1;
    }

    /**
     * 取消订阅机场 ModeCode
     */
    public void cancelListenDockModeCode(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockModeCodeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDockModeCode error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅飞行任务步骤码
     * @param callback 步骤码回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_task_flight_task_step_code
     */
    public int listenFlightTaskStepCode(FlightHubCallback.FlightTaskStepCodeCallback callback) {
        try {
            com.dji.flighthubsdk.generated.FlightTaskStepCodeCallback swigCallback = new com.dji.flighthubsdk.generated.FlightTaskStepCodeCallback() {
                @Override
                public void onFlightTaskStepCode(com.dji.flighthubsdk.generated.FlightTaskStepCode flight_task_step_code) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toFlightTaskStepCode(flight_task_step_code));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenFlightTaskStepCodeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenFlightTaskStepCode error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toFlightTaskStepCode(null));
            }
        }

        return -1;
    }

    /**
     * 取消订阅飞行任务步骤码
     */
    public void cancelListenFlightTaskStepCode(int listenId) {
        try {
            this.dockOsdModule.cancelListenFlightTaskStepCodeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenFlightTaskStepCode error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅机场子设备信息列表
     * @param callback 子设备列表回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_sub_device
     */
    public int listenDockSubDevice(FlightHubCallback.DockSubDeviceInfoListCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockSubDeviceInfoListCallback swigCallback = new com.dji.flighthubsdk.generated.DockSubDeviceInfoListCallback() {
                @Override
                public void onDockSubDeviceInfoList(com.dji.flighthubsdk.generated.DockSubDeviceInfoList dock_sub_device_info_list) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockSubDeviceInfoList(dock_sub_device_info_list));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockSubDeviceWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockSubDevice() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockSubDeviceInfoList(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场子设备信息列表
     */
    public void cancelListenDockSubDevice(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockSubDeviceWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockSubDevice() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场网络状态
     * @param callback 网络状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_network_state
     */
    public int listenDockNetworkState(FlightHubCallback.DockNetworkStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockNetworkStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockNetworkStateCallback() {
                @Override
                public void onDockNetworkState(com.dji.flighthubsdk.generated.DockNetworkState dock_network_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockNetworkState(dock_network_state));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockNetworkStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockNetworkState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockNetworkState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场网络状态
     */
    public void cancelListenDockNetworkState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockNetworkStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockNetworkState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场飞机充电状态
     * @param callback 充电状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_drone_charge_state
     */
    public int listenDockDroneChargeState(FlightHubCallback.DockDroneChargeStateCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockDroneChargeStateCallback swigCallback = new com.dji.flighthubsdk.generated.DockDroneChargeStateCallback() {
                @Override
                public void onDockDroneChargeState(com.dji.flighthubsdk.generated.DockDroneChargeState dock_drone_charge_state) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockDroneChargeState(dock_drone_charge_state));
                    }
                }
            };
            
            int listenId = this.dockOsdModule.listenDockDroneChargeStateWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] listenDockDroneChargeState() error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockDroneChargeState(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场飞机充电状态
     */
    public void cancelListenDockDroneChargeState(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockDroneChargeStateWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "[" + sn + "] cancelListenDockDroneChargeState() error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场气象信息
     * @param callback 气象信息回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_meteorological_info
     */
    public int listenDockMeteorologicalInfo(FlightHubCallback.DockMeteorologicalInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockMeteorologicalInfoCallback swigCallback = new com.dji.flighthubsdk.generated.DockMeteorologicalInfoCallback() {
                @Override
                public void onDockMeteorologicalInfo(com.dji.flighthubsdk.generated.DockMeteorologicalInfo dock_meteorological_info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockMeteorologicalInfo(dock_meteorological_info));
                    }
                }
            };
            
            int listenId = this.dockOsdModule.listenDockMeteorologicalInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDockMeteorologicalInfo error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockMeteorologicalInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场气象信息
     */
    public void cancelListenDockMeteorologicalInfo(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockMeteorologicalInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDockMeteorologicalInfo error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场备用电池信息
     * @param callback 备用电池信息回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_backup_battery_info
     */
    public int listenDockBackupBatteryInfo(FlightHubCallback.DockBackupBatteryInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockBackupBatteryInfoCallback swigCallback = new com.dji.flighthubsdk.generated.DockBackupBatteryInfoCallback() {
                @Override
                public void onDockBackupBatteryInfo(com.dji.flighthubsdk.generated.DockBackupBatteryInfo backup_battery_info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockBackupBatteryInfo(backup_battery_info));
                    }
                }
            };
            
            int listenId = this.dockOsdModule.listenDockBackupBatteryInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDockBackupBatteryInfo error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockBackupBatteryInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场备用电池信息
     */
    public void cancelListenDockBackupBatteryInfo(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockBackupBatteryInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDockBackupBatteryInfo error: " + e.getMessage(), e);
        }
    }


    /**
     * 订阅机场保养状态信息
     * @param callback 保养状态回调
     * @return 监听 ID
     * @implNote 链路: WebSocket | topic: topic_dock_maintain_status_info
     */
    public int listenDockMaintainStatusInfo(FlightHubCallback.DockMaintainStatusInfoCallback callback) {
        try {
            // 创建SWIG回调对象
            com.dji.flighthubsdk.generated.DockMaintainStatusInfoCallback swigCallback = new com.dji.flighthubsdk.generated.DockMaintainStatusInfoCallback() {
                @Override
                public void onDockMaintainStatusInfo(com.dji.flighthubsdk.generated.DockMaintainStatusInfo info) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockMaintainStatusInfo(info));
                    }
                }
            };
            
            int listenId = this.dockOsdModule.listenDockMaintainStatusInfoWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDockMaintainStatusInfo error: " + e.getMessage(), e);

            if (callback != null) {
                callback.onResult(FlightHubSdkConvert.toDockMaintainStatusInfo(null));
            }
        }

        return -1;
    }


    /**
     * 取消订阅机场保养状态信息
     */
    public void cancelListenDockMaintainStatusInfo(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockMaintainStatusInfoWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDockMaintainStatusInfo error: " + e.getMessage(), e);
        }
    }

    // ===================== EA340 消息推送补全（Phase D） =====================

    /**
     * 订阅机场4G Dongle信息(dongle_infos)
     * @param callback 回调
     * @return 监听ID
     * @implNote 链路: WebSocket | topic: topic_dock_dongle_infos
     */
    public int listenDockDongleInfos(FlightHubCallback.DockDongleInfosCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockDongleInfosCallback swigCallback = new com.dji.flighthubsdk.generated.DockDongleInfosCallback() {
                @Override
                public void onDockDongleInfos(com.dji.flighthubsdk.generated.DockDongleInfos msg) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockDongleInfos(msg));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockDongleInfosWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDockDongleInfos error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅机场4G Dongle信息 */
    public void cancelListenDockDongleInfos(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockDongleInfosWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDockDongleInfos error: " + e.getMessage(), e);
        }
    }

    /**
     * 订阅机场空调工作模式(air_conditioner_mode)
     * @param callback 回调
     * @return 监听ID
     * @implNote 链路: WebSocket | topic: topic_dock_air_conditioner_mode
     */
    public int listenDockAirConditionerMode(FlightHubCallback.DockAirConditionerModeCallback callback) {
        try {
            com.dji.flighthubsdk.generated.DockAirConditionerModeCallback swigCallback = new com.dji.flighthubsdk.generated.DockAirConditionerModeCallback() {
                @Override
                public void onDockAirConditionerMode(com.dji.flighthubsdk.generated.DockAirConditionerMode msg) {
                    if (callback != null) {
                        callback.onResult(FlightHubSdkConvert.toDockAirConditionerMode(msg));
                    }
                }
            };

            int listenId = this.dockOsdModule.listenDockAirConditionerModeWrapper(swigCallback);
            if (listenId >= 0) {
                swigCallback.swigReleaseOwnership();
            }
            return listenId;
        } catch (Exception e) {
            Logger.error("DockOsdModule", "listenDockAirConditionerMode error: " + e.getMessage(), e);
        }
        return -1;
    }

    /** 取消订阅机场空调工作模式 */
    public void cancelListenDockAirConditionerMode(int listenId) {
        try {
            this.dockOsdModule.cancelListenDockAirConditionerModeWrapper(listenId);
        } catch (Exception e) {
            Logger.error("DockOsdModule", "cancelListenDockAirConditionerMode error: " + e.getMessage(), e);
        }
    }

    public DockOsdModule(IDockOsdModule dockOsdModule, String sn) {
        this.dockOsdModule = dockOsdModule;
        this.sn = sn;
    }
}
