// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <vector>
#include <cstdint>
#include <climits>
#include <cmath>
#include <unordered_map>
#include "value.h"

namespace flight_hub_sdk {

using ListenId = int;

// 像 int64_t 这样的 type_t 类型, SWIG 支持不友好，但是 AIGC_JSON 可以支持;
// AIGC_JSON 可以使用 type_t 类型, 但转到 FH SDK MESSAGE 的时候, 需要修改到对应的映射类型;

// int64_t(AIGC_JSON)  <---> long long(SWIG) <----> long (Java)


// floatEqual 的 EPS 选择
// 核心依据是 IEEE 754 机器精度 (machine epsilon)

// 类型	       位宽	    有效十进制位	  机器精度	            推荐比较 EPS
// float	 32-bit	    ~7 位	    ~1.19e-7 (2^-23)	  1e-5 ~ 1e-6
// double	 64-bit	    ~15 位	    ~2.22e-16 (2^-52)	  1e-9 ~ 1e-12

// 选择逻辑：比较 EPS 应比机器精度大若干数量级，为累积运算误差留余量。
// 浮点型对象: 差值阈值;
constexpr double EPS_FLT = 1e-6;
constexpr double EPS_DBL = 1e-12;

inline bool floatEqual(float a, float b, double eps = EPS_FLT) {
    return std::fabs(a - b) < eps;
}


inline bool floatEqual(double a, double b, double eps = EPS_DBL) {
    return std::fabs(a - b) < eps;
}


// Common 分页信息;
struct CommonPaginationData {
    int page = 0;          // 当前页码;
    int page_size = 0;     // 分页大小;
    int total = 0;         // 总数量;

    bool operator==(const CommonPaginationData &rhs) const {
        return
            page == rhs.page &&
            page_size == rhs.page_size &&
            total == rhs.total;
    }

    bool operator!=(const CommonPaginationData &rhs) const {
        return !(*this == rhs);
    }
};


/// drone osd
class DroneOnlineStatus {
public:
    bool is_online = false;

    bool operator==(const DroneOnlineStatus& other) const {
        return is_online == other.is_online;
    }
};

class DockOnlineStatus {
public:
    bool is_online = false;

    bool operator==(const DockOnlineStatus& other) const {
        return is_online == other.is_online;
    }
};


class LinkStatus {
public:
    int ctrl_link_status = static_cast<int>(DroneLinkState::DroneLinkState_UNKNOWN);   ///< 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`）
    int data_link_status = static_cast<int>(DroneLinkState::DroneLinkState_UNKNOWN);  ///< 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`）
    int live_link_status = static_cast<int>(DroneLinkState::DroneLinkState_UNKNOWN);  ///< 取值语义同 `flight_hub_sdk::DroneLinkState`（见 `value.h`）

    bool operator==(const LinkStatus& other) const {
        return
            ctrl_link_status == other.ctrl_link_status &&
            data_link_status == other.data_link_status &&
            live_link_status == other.live_link_status;
    }
};

// 电池信息
class BatteryInfo {
public:
    int capacity_percent = 0;
    std::string firmware_version;
    int high_voltage_storage_days = 0;
    int index = 0;
    int loop_times = 0;
    std::string sn;
    int sub_type = 0;
    double temperature = 0.0;
    int type = 0; // TODO: 枚举
    int voltage = 0;

    bool operator==(const BatteryInfo& other) const {
        return
            capacity_percent == other.capacity_percent &&
            firmware_version == other.firmware_version &&
            high_voltage_storage_days == other.high_voltage_storage_days &&
            index == other.index &&
            loop_times == other.loop_times &&
            sn == other.sn &&
            sub_type == other.sub_type &&
            floatEqual(temperature, other.temperature) &&
            type == other.type &&
            voltage == other.voltage;
    }
};

// 电池组信息
class AircraftBatteryInfo {
public:
    std::vector<BatteryInfo> batteries;
    int capacity_percent = 0;
    int landing_power = 0;
    int remain_flight_time = 0;
    int return_home_power = 0;

    bool operator==(const AircraftBatteryInfo& other) const {
        return
            batteries == other.batteries &&
            capacity_percent == other.capacity_percent &&
            landing_power == other.landing_power &&
            remain_flight_time == other.remain_flight_time &&
            return_home_power == other.return_home_power;
    }
};

class AircraftAttitude {
public:
    double attitude_head = 0.0; // 机头朝向角度
    double attitude_pitch = 0.0; // 俯仰轴角度
    double attitude_roll = 0.0; // 横滚轴角度
    bool operator==(const AircraftAttitude& other) const {
        return
            floatEqual(attitude_head, other.attitude_head) &&
            floatEqual(attitude_pitch, other.attitude_pitch) &&
            floatEqual(attitude_roll, other.attitude_roll);
    }
};


class AircraftLocation {
public:
    double height = 0.0;    // 椭球高度
    double elevation = 0.0; // 相对起飞点高度
    double longitude = 0.0;
    double latitude = 0.0;

    bool operator==(const AircraftLocation& other) const {
        return
            floatEqual(height, other.height) &&
            floatEqual(elevation, other.elevation) &&
            floatEqual(longitude, other.longitude) &&
            floatEqual(latitude, other.latitude);
    }
};

class AircraftModeCode {
public:
    int mode_code = static_cast<int>(DroneModeCode::DroneModeCode_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::DroneModeCode`（见 `value.h`）

    bool operator==(const AircraftModeCode& other) const {
        return mode_code == other.mode_code;
    }
};

class AircraftControlCode {
public:
    int control_mode = 0; // "0": "航线控制-执行态", "1": "指令控制-手动飞行", "2": "指令控制-自动飞行", "3": "航线控制-中断态"

    bool operator==(const AircraftControlCode& other) const {
        return control_mode == other.control_mode;
    }
};

class AircraftSpeed {
public:
    double horizontal_speed = 0.0; // 水平速度 m/s
    double vertical_speed = 0.0; // 垂直速度 m/s

    bool operator==(const AircraftSpeed& other) const {
        return
            floatEqual(horizontal_speed, other.horizontal_speed) &&
            floatEqual(vertical_speed, other.vertical_speed);
    }
};

// 载荷相机实时视图世界区域信息（前向声明，在 LiveviewWorldRegion 定义后实现）
class AircraftPayloadsCameraLiveviewWorldRegion;

// 载荷云台姿态信息
class AircraftPayloadGimbalAttitude {
public:
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double gimbal_pitch = 0.0;
    double gimbal_roll = 0.0;
    double gimbal_yaw = 0.0;

    bool operator==(const AircraftPayloadGimbalAttitude& other) const {
        return
            payload_index == other.payload_index &&
            floatEqual(gimbal_pitch, other.gimbal_pitch) &&
            floatEqual(gimbal_roll, other.gimbal_roll) &&
            floatEqual(gimbal_yaw, other.gimbal_yaw);
    }
};

// 载荷云台姿态组信息
class AircraftPayloadsGimbalAttitude {
public:
    std::vector<AircraftPayloadGimbalAttitude> payloads_gimbal_attitude;

    bool operator==(const AircraftPayloadsGimbalAttitude& other) const {
        return payloads_gimbal_attitude == other.payloads_gimbal_attitude;
    }
};

// 载荷列表信息
class AircraftPayloadsList {
public:
    std::vector<std::string> payloads_list;

    bool operator==(const AircraftPayloadsList& other) const {
        return payloads_list == other.payloads_list;
    }
};

// 控制权限云控制状态项信息
class ControlRightsCloudControlStatusItem {
public:
    std::string type; // flight/payload, 飞机还是负载
    std::string key; // 如果 type 是 flight, 则 key 固定为flight, 如果是 payload, 则是 payload_81-0-0 这样的格式(带上了payload_index)
    std::string user_id; // 用户 id
    bool is_locked = false;
    double version = 0;

    bool operator==(const ControlRightsCloudControlStatusItem& other) const {
        return
            type == other.type &&
            key == other.key &&
            user_id == other.user_id &&
            is_locked == other.is_locked &&
            floatEqual(version, other.version);
    }
};

// 控制权限云控制状态信息
class ControlRightsCloudControlStatus {
public:
    std::string sn;
    std::string reason;
    std::vector<ControlRightsCloudControlStatusItem> controls;

    bool operator==(const ControlRightsCloudControlStatus& other) const {
        return
            sn == other.sn &&
            reason == other.reason &&
            controls == other.controls;
    }
};

// 控制权限设备控制状态项信息
class ControlRightsDeviceControlStatusItem {
public:
    std::string type;   // flight/payload, 飞机还是负载
    std::string key;    // 如果 type 是 flight, 则 key 固定为flight, 如果是 payload, 则是 payload_81-0-0 这样的格式(带上了payload_index)
    std::string owner;  // RC/MC/CCC
    double version = 0;

    bool operator==(const ControlRightsDeviceControlStatusItem& other) const {
        return
            type == other.type &&
            key == other.key &&
            owner == other.owner &&
            floatEqual(version, other.version);
    }
};

// 控制权限设备控制状态信息
class ControlRightsDeviceControlStatus {
public:
    std::string sn;
    std::string reason;
    std::vector<ControlRightsDeviceControlStatusItem> controls;

    bool operator==(const ControlRightsDeviceControlStatus& other) const {
        return
            sn == other.sn &&
            reason == other.reason &&
            controls == other.controls;
    }
};

// 飞行任务断点恢复事件信息
class FlightTaskEventBreakPointResume {
public:
    std::string task_uuid;
    std::string parent_uuid;
    std::string group_uuid;
    std::string sn;
    std::string landing_dock_sn;

    bool operator==(const FlightTaskEventBreakPointResume& other) const {
        return
            task_uuid == other.task_uuid &&
            parent_uuid == other.parent_uuid &&
            group_uuid == other.group_uuid &&
            sn == other.sn &&
            landing_dock_sn == other.landing_dock_sn;
    }
};

// 飞行任务连续子任务事件信息
class FlightTaskEventContinuousSubTask {
public:
    std::string task_uuid;
    std::string parent_uuid;
    std::string group_uuid;
    std::string sn;
    std::string landing_dock_sn;

    bool operator==(const FlightTaskEventContinuousSubTask& other) const {
        return
            task_uuid == other.task_uuid &&
            parent_uuid == other.parent_uuid &&
            group_uuid == other.group_uuid &&
            sn == other.sn &&
            landing_dock_sn == other.landing_dock_sn;
    }
};

// 飞行任务 flighttask_step_code 信息;
class FlightTaskStepCode {
public:
    // 说明;
    // "0": "作业准备中",
    // "1": "飞行作业中",
    // "2": "作业后状态恢复",
    // "3": "自定义飞行区更新中",
    // "4": "地形障碍物更新中",
    // "5": "任务空闲",
    // "256": "未知状态"
    int flighttask_step_code = 0;

    bool operator==(const FlightTaskStepCode &rhs) const {
        return flighttask_step_code == rhs.flighttask_step_code;
    }
};


// 机场: 子设备信息;
class DockSubDeviceInfo {
public:
    std::string device_sn;           // string, "子设备序列号(SN)";
    std::string device_model_key;    // string,"子设备枚举值", {domain-type-subtype};
    int device_online_status = 0;    // int,"机场停机坪上的飞行器开机状态", { 0: 关机, 1: 开机 };
    int device_paired = 0;           // int,"机场停机坪上的飞行器是否与机场对频", { 0: 未对频, 1: 已对频 };

    bool operator==(const DockSubDeviceInfo &rhs) const {
        return
            device_sn == rhs.device_sn &&
            device_model_key == rhs.device_model_key &&
            device_online_status == rhs.device_online_status &&
            device_paired == rhs.device_paired;
    }
};


// 机场: 子设备信息列表;
class DockSubDeviceInfoList {
public:
    std::vector<DockSubDeviceInfo> sub_device_info_vec;

    bool operator==(const DockSubDeviceInfoList &rhs) const {
        return sub_device_info_vec == rhs.sub_device_info_vec;
    }
};


// 机场网络状态(network_state);
class DockNetworkState {
public:
    int type = -1;     // int, "网络类型", { 1: 4G, 2: 以太网 };  默认无效值: -1;
    int quality = -1;  // int, "网络质量(当网络类型为4G时才具有网络质量信息)", { 0: 差, 1: 中, 2: 好 }; 默认无效值: -1;
    int rate = 0;      // int, "网络速率(单位KB/秒)"; 默认值: 0;

    bool operator==(const DockNetworkState &rhs) const {
        return
            (type == rhs.type) &&
            (quality == rhs.quality) &&
            (rate == rhs.rate);
    }
};

// 媒体文件数量(media_file_detail);
class DockMediaFileDetail {
public:
    int remain_upload = 0;  // int, "待上传数量";

    bool operator==(const DockMediaFileDetail &rhs) const {
        return remain_upload == rhs.remain_upload;
    }
};


// 机场图传质量(wireless_link);
class DockWirelessLink {
public:
    int dongle_num = 0;           // int: 0,1,2 飞机上 dongle 数量;
    int link_state_4g = 0;        // int: 4g 链路连接状态, 0-断开, 1-连接;
    int sdr_link_state = 0;       // int, "sdr链路状态", { 0: 断开, 1: 连接 };
    int link_workmode = 0;        // int, "链路工作模式", { 0: sdr only, 1: 4G融合 };
    int sdr_quality = 0;          // int: 标识sdr信号强弱, 0~5 一共六档;
    int quality_4g = 0;           // int: 标识总体4G信号强弱, 0~5 一共六档，310UI上使用此值, 对应v1协议的 lk_quality;
    int uav_quality_4g = 0;       // int: 标识飞机端4G信号强弱,0~5 一共六档;
    int gnd_quality_4g = 0;       // int: 标识地面端4G信号强弱,0~5 一共六档;
    float sdr_freq_band = 0;      // float: sdr 频段;
    float freq_band_4g = 0;       // float: 4g 频段;

   /*
    * fixed: 使用 floatEqual 进行浮点型比较;
    */
    bool operator==(const DockWirelessLink &rhs) const {
        return
            dongle_num == rhs.dongle_num &&
            link_state_4g == rhs.link_state_4g &&
            sdr_link_state == rhs.sdr_link_state &&
            link_workmode == rhs.link_workmode &&
            sdr_quality == rhs.sdr_quality &&
            quality_4g == rhs.quality_4g &&
            uav_quality_4g == rhs.uav_quality_4g &&
            gnd_quality_4g == rhs.gnd_quality_4g &&
            floatEqual(sdr_freq_band, rhs.sdr_freq_band) &&
            floatEqual(freq_band_4g, rhs.freq_band_4g);
    }
};


// 机场作业次数(job_number);
class DockJobNumber {
public:
    int job_number = 0;    // int, 作业次数;

    bool operator==(const DockJobNumber &rhs) const {
        return job_number == rhs.job_number;
    }
};


// 机场：DockPowerState；
// 市电电压(electric_supply_voltage), V;
// 工作电压(working_voltage), mV;
// 工作电流(working_current), mA;
class DockPowerState {
public:
    int electric_supply_voltage = 0;  // int, 市电电压, 单位：V;
    int working_voltage = 0;          // int, 工作电压(mV);
    int working_current = 0;          // int, 工作电流(mA);

    bool operator==(const DockPowerState &rhs) const {
        return
            (electric_supply_voltage == rhs.electric_supply_voltage) &&
            (working_voltage == rhs.working_voltage) &&
            (working_current == rhs.working_current);
    }
};


// 机场存储容量(storage);
// DockStorageState;
// !todo: 从物模型的协议设计来看使用了 int 类型, 容量最多 2.x TB, 暂时使用 int 类型;
class DockStorageState {
public:
    int total = 0;  //  int, 总容量(单位KB);
    int used = 0;    // int, 已使用(单位KB);

    bool operator==(const DockStorageState &rhs) const {
        return (total == rhs.total) && (used == rhs.used);
    }
};


// 机场首次上电时间(first_power_on);
class DockFirstPowerOnTime {
public:
    // 首次上电时间, 毫秒时间戳用 string 类型, SDK C++ AIGCJson 对(long long)类型支持不友好;
    std::string first_power_on = "0";  // string, 首次上电时间(ms);

    bool operator==(const DockFirstPowerOnTime &rhs) const {
        return first_power_on == rhs.first_power_on;
    }
};


// 机场累计运行时长(acc_time);
class DockAccTime {
public:
    int acc_time = 0;    // int, 机场累计运行时长(秒);

    bool operator==(const DockAccTime &rhs) const {
        return acc_time == rhs.acc_time;
    }
};


// 备降点(alternate_land_point);
class DockAlternateLandPoint {
public:
    int is_configured = 0;          // 备降点是否设置, {0:未设置, 1:已设置};
    double longitude = 0.0;         // 经度;
    double latitude = 0.0;          // 纬度;
    double safe_land_height = 0.0;  // 安全高度(备降转移高);
    double height = 0.0;            // 高度;


    bool operator==(const DockAlternateLandPoint &rhs) const {
        return
            is_configured == rhs.is_configured &&
            floatEqual(longitude, rhs.longitude) &&
            floatEqual(latitude, rhs.latitude) &&
            floatEqual(safe_land_height, rhs.safe_land_height) &&
            floatEqual(height, rhs.height);
    }
};



// 搜星状态(position_state);
class DockPositionState {
public:
    int is_calibration = 0;     // int, 是否标定, 0:未标定,1:已标定; 只有基站才有是否标定;
    int is_fixed = 0;           // int, 是否收敛, 0:未开始, 1:收敛中, 2:收敛成功, 3: 收敛失败;
    int quality = 0;            // int, 搜星档位, 1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档;
    int gps_number = 0;         // int, GPS 搜星数量;
    int rtk_number = 0;         // int, RTK 搜星数量;
    
    
    bool operator==(const DockPositionState &rhs) const {
        return
            is_calibration == rhs.is_calibration &&
            is_fixed == rhs.is_fixed &&
            quality == rhs.quality &&
            gps_number == rhs.gps_number &&
            rtk_number == rhs.rtk_number;
    }
};


// 机场激活时间(activation_time, unix时间戳 s);
// DockActivationTime;
class DockActivationTime {
public:
    int activation_time = 0;  // int, 机场激活时间(unix时间戳 s);

    bool operator==(const DockActivationTime &rhs) const {
        return activation_time == rhs.activation_time;
    }
};


// 电池(存储)模式(battery_store_mode);
// DockBatteryStoreMode;
class DockBatteryStoreMode {
public:
    int battery_store_mode = 0;  // int, 电池(存储)模式, {1: 电池计划存储策略, 2: 电池应急存储策略};

    bool operator==(const DockBatteryStoreMode &rhs) const {
        return battery_store_mode == rhs.battery_store_mode;
    }
};


// 机场声光报警状态(alarm_state);
// DockAlarmState;
class DockAlarmState {
public:
    int alarm_state = 0;  // int, 声光报警状态, {0: 声光报警关闭, 1: 声光报警开启};

    bool operator==(const DockAlarmState &rhs) const {
        return alarm_state == rhs.alarm_state;
    }
};


// 飞行器电池保养信息(drone_battery_maintenance_info);
// 机场飞行器 电池基础信息;
class BatteryBasicInfo {
public:
    int index = 0;                   // int, 电池序号, 0:左电池; 1:右电池；
    int capacity_percent = 0;        // int, 电池剩余电量,[0,100];
    int voltage = 0;                 // int, 电压(mV), 参考[0~28000mV];
    double temperature = 0.0;        // double, 温度(摄氏度);
    
    bool operator==(const BatteryBasicInfo &rhs) const {
        return
            index == rhs.index &&
            capacity_percent == rhs.capacity_percent &&
            voltage == rhs.voltage &&
            floatEqual(temperature, rhs.temperature);
    }
};


// 飞行器电池保养信息(drone_battery_maintenance_info);
// [含] 机场飞行器 电池基础信息;
class DockDroneBatteryMaintenanceInfo {
public:
    int maintenance_state = 0;                  // int, 保养状态, 0: 无需保养, 1: 待保养, 2: 正在保养;
    int maintenance_time_left = 0;              // int, 电池保养剩余时间(小时);
    int heat_state = 0;                         // int, 电池加热保温状态, 0: 电池未开启加热或保温, 1: 电池在加热中, 2: 电池在保温中;

    std::vector<BatteryBasicInfo> batteries;    // std::vector<BatteryBasicInfo>, 电池基础信息;


    bool operator==(const DockDroneBatteryMaintenanceInfo &rhs) const {
        return
            maintenance_state == rhs.maintenance_state &&
            maintenance_time_left == rhs.maintenance_time_left &&
            heat_state == rhs.heat_state &&
            batteries == rhs.batteries;
    }
};


// 紧急停止按钮状态(emergency_stop_state);
// DockEmergencyStopState;
class DockEmergencyStopState {
public:
    int emergency_stop_state = 0;    // int, 紧急停止按钮状态, 0: 关闭, 1: 开启;

    bool operator==(const DockEmergencyStopState &rhs) const {
        return emergency_stop_state == rhs.emergency_stop_state;
    }
};


// 机场静音模式(silent_mode);
// DockSilentMode;
class DockSilentMode {
public:
    int silent_mode = 0;    // int, 机场静音模式, 0: 静音模式关闭, 1: 静音模式开启;

    bool operator==(const DockSilentMode &rhs) const {
        return silent_mode == rhs.silent_mode;
    }
};


// 用户体验改善计划(user_experience_improvement);
// DockUserExperienceImprovement;
class DockUserExperienceImprovement {
public:
    // 用户体验改善计划, { 0:初始状态, 1:拒绝加入用户体验改善计划, 2:同意加入用户体验改善计划; }
    int user_experience_improvement = 0;

    bool operator==(const DockUserExperienceImprovement &rhs) const {
        return user_experience_improvement == rhs.user_experience_improvement;
    }
};


// DRC 链路的状态(drc_state);
// DockDrcState;
class DockDrcState {
public:
    int drc_state = 0;  // int, DRC 链路状态, { 0: 未连接, 1: 连接中, 2: 已连接; }

    bool operator==(const DockDrcState &rhs) const {
        return drc_state == rhs.drc_state;
    }
};


// 推杆状态(putter_state);
// DockPutterState;
class DockPutterState {
public:
    int putter_state = 0;  // int, 推杆状态, 参考 EA210 {0: 关闭, 1: 打开, 2: 半开, 3: 推杆状态异常};

    bool operator==(const DockPutterState &rhs) const {
        return putter_state == rhs.putter_state;
    }
};


// 相对备降点(relative_alternate_land_point);
// DockRelativeAlternateLandPoint;
class DockRelativeAlternateLandPoint {
public:
    double longitude = 0.0;          // double;
    double latitude = 0.0;           // double;
    double safe_land_height = 0.0;   // double,安全高度(备降转移高);
    int status = 0;                  // int, { 0: 正常, 1: 机场未设置相对备降点, 2: 机场未进入基站模式, 3: 机场朝向未收敛 };

    bool operator==(const DockRelativeAlternateLandPoint &rhs) const {
        return
            floatEqual(longitude, rhs.longitude) &&
            floatEqual(latitude, rhs.latitude) &&
            floatEqual(safe_land_height, rhs.safe_land_height) &&
            status == rhs.status;
    }
};


// 自收敛坐标(self_converge_coordinate);
// DockSelfConvergeCoordinate;
class DockSelfConvergeCoordinate {
public:
    double height = 0.0;
    double longitude = 0.0;
    double latitude = 0.0;

    // !todo: 应该使用 浮点型相等比较;
    // !todo: use nearly_equal(x, y, eps = 1e-6);
    // fixed: 使用 floatEqual 进行浮点型比较;
    bool operator==(const DockSelfConvergeCoordinate &rhs) const {
        return
            floatEqual(height, rhs.height) &&
            floatEqual(longitude, rhs.longitude) &&
            floatEqual(latitude, rhs.latitude);
    }
};


// 任务媒体状态信息
class TasksMediaStatus {
public:
    std::string task_uuid;
    int media_status = 0; // 0-待上传，1-上传中，2-上传结束

    bool operator==(const TasksMediaStatus& other) const {
        return task_uuid == other.task_uuid && media_status == other.media_status;
    }
};

// 飞行任务媒体状态变更信息
class FlightTasksMediaStatusChanged {
public:
    std::vector<TasksMediaStatus> task_media;

    bool operator==(const FlightTasksMediaStatusChanged& other) const {
        return task_media == other.task_media;
    }
};

// 飞行任务信息
class FlightTask {
public:
    std::string uuid;
    std::string name;
    int task_type = 0;
    int status = 0;
    int progress = 0;
    std::string sn;
    std::string folder_info;
    long run_at = 0; // 任务实际运行时间
    long complete_at = 0; // 任务结束时间
    int total_waypoints = 0; // 总航线数
    int current_waypoint_index = 0; // 已飞完的航点数
    long progress_version = 0; // 消息的时间戳，用来处理乱序问题
    std::string resumable_status; /*ResumableStatus*/
    std::string obstacle_avoidance_notify; /*ObstacleNotifyType*/
    std::string wayline_uuid;

    bool operator==(const FlightTask& other) const {
        return
            uuid == other.uuid &&
            name == other.name &&
            task_type == other.task_type &&
            status == other.status &&
            progress == other.progress &&
            sn == other.sn &&
            folder_info == other.folder_info &&
            run_at == other.run_at &&
            complete_at == other.complete_at &&
            total_waypoints == other.total_waypoints &&
            current_waypoint_index == other.current_waypoint_index &&
            progress_version == other.progress_version &&
            resumable_status == other.resumable_status &&
            obstacle_avoidance_notify == other.obstacle_avoidance_notify &&
            wayline_uuid == other.wayline_uuid;
    }
};

// 飞行任务组信息
class FlightTasks {
public:
    std::vector<FlightTask> flight_tasks;

    bool operator==(const FlightTasks& other) const {
        return flight_tasks == other.flight_tasks;
    }
};

// 航线任务上传的媒体在媒体库可加载通知
class FlightTasksMediaFileDisplayAvailable {
public:
    std::string uuid;
    int idx_parent_id = -1;
    int file_type = -1;
    int sub_file_type = -1;
    int media_file_id = -1;
    std::string flight_id;
    std::string sn;
    int media_source = -1;
    std::string url;

    bool operator==(const FlightTasksMediaFileDisplayAvailable& other) const {
        return
            uuid == other.uuid &&
            idx_parent_id == other.idx_parent_id &&
            file_type == other.file_type &&
            sub_file_type == other.sub_file_type &&
            media_file_id == other.media_file_id &&
            flight_id == other.flight_id &&
            sn == other.sn &&
            media_source == other.media_source &&
            url == other.url;
    }
};

// 飞行器 HSI 信息（障碍物感知/避障传感器数据）;
class HsiInfo {
public:
    std::vector<int> around_distances;  // 周围障碍物距离数组
    bool back_enable = false;           // 后方传感器启用
    bool back_work = false;             // 后方传感器工作中
    int down_distance = 0;              // 下方距离
    bool down_enable = false;           // 下方传感器启用
    bool down_work = false;             // 下方传感器工作中
    bool front_enable = false;          // 前方传感器启用
    bool front_work = false;            // 前方传感器工作中
    bool horizontal_enable = false;     // 水平避障启用
    bool horizontal_work = false;       // 水平避障工作中
    bool left_enable = false;           // 左方传感器启用
    bool left_work = false;             // 左方传感器工作中
    bool right_enable = false;          // 右方传感器启用
    bool right_work = false;            // 右方传感器工作中
    int up_distance = 0;               // 上方距离
    bool up_enable = false;             // 上方传感器启用
    bool up_work = false;              // 上方传感器工作中
    bool vertical_enable = false;       // 垂直避障启用
    bool vertical_work = false;         // 垂直避障工作中

    bool operator==(const HsiInfo& rhs) const {
        return around_distances == rhs.around_distances &&
            back_enable == rhs.back_enable && back_work == rhs.back_work &&
            down_distance == rhs.down_distance && down_enable == rhs.down_enable && down_work == rhs.down_work &&
            front_enable == rhs.front_enable && front_work == rhs.front_work &&
            horizontal_enable == rhs.horizontal_enable && horizontal_work == rhs.horizontal_work &&
            left_enable == rhs.left_enable && left_work == rhs.left_work &&
            right_enable == rhs.right_enable && right_work == rhs.right_work &&
            up_distance == rhs.up_distance && up_enable == rhs.up_enable && up_work == rhs.up_work &&
            vertical_enable == rhs.vertical_enable && vertical_work == rhs.vertical_work;
    }
};

class AircraftWindSpeed {
public:
    int wind_direction = 0; // 当前风向 "enum_desc": "1:正北  2：东北  3:东  4:东南  5:南  6:西南  7:西   8:西北"
    double wind_speed = 0.0; // 风速估计，该风速是通过飞机姿态推测出的，有一定的误差，仅供参考，不能作为气象数据使用

    bool operator==(const AircraftWindSpeed& other) const {
        return
            wind_direction == other.wind_direction &&
            floatEqual(wind_speed, other.wind_speed);
    }
};

// 飞机起飞进度事件信息
class AircraftTakeoffProgress {
public:
    std::string bid;
    std::string tid;
    std::string flight_id;
    std::string track_id;
    std::string sn;
    // SDK 仅会收到以下 status（pf-atc-service WebSocket 过滤后仅推送）：
    // - "task_ready"  : 已准备起飞（飞机就绪，即将起飞）
    // - "task_finish" : 一键起飞流程结束（成功或失败，需配合 result 判断；失败时 status 仍为 task_finish）
    // 上游设备还可能上报 takeoff_prepare、wayline_progress、wayline_cancel、wayline_failed、wayline_ok，但不会推送给 SDK。
    std::string status;
    // 结果码：0 表示成功，>0 表示失败（具体错误码由设备/DTCP 返回）。仅在 status 为 "task_finish" 时有意义。
    int result = 0;
    int waypoint_index = 0;
    std::string takeoff_mode; // Takeoff/TakeoffWithFlyTo
    long timestamp = 0;

    bool operator==(const AircraftTakeoffProgress& other) const {
        return
            bid == other.bid &&
            tid == other.tid &&
            flight_id == other.flight_id &&
            track_id == other.track_id &&
            sn == other.sn &&
            status == other.status &&
            result == other.result &&
            waypoint_index == other.waypoint_index &&
            takeoff_mode == other.takeoff_mode &&
            timestamp == other.timestamp;
    }
};

// [removed] WorkspaceSessionInfo — 去 SaaS 后无 session 概念

/// drone osd end

/// SDK 配置信息
struct FlightHubSdkConfig {
    std::string log_level = "info";          ///< 日志级别，可选值：debug / info / warn / error
    bool log_to_console = true;              ///< 是否将 SDK 日志输出到标准输出/标准错误，便于在控制台或容器日志中查看
    bool show_version_info = true;           ///< 是否在初始化时打印 SDK 版本与构建信息
    std::string log_file_path = "flight_hub_sdk.log";  ///< SDK 日志文件路径，默认 flight_hub_sdk.log；可为文件名或目录，SDK 滚动日志时可能追加时间戳
    size_t log_max_file_size = 4 * 1024 * 1024;        ///< 单个日志文件最大字节数，默认 4MB；为 0 时由 SDK 使用内置默认值
    int log_max_files = 1000;                ///< 保留的日志文件个数上限，默认 1000；为 0 时由 SDK 使用内置默认值
    std::string address;                     ///< PaaS 服务地址（含协议），例如 https://your-paas-host
    std::string port;                        ///< FlightHub 服务端口
    std::string notification_server_address;  ///< 消息通知 WebSocket 服务地址（完整 URL，含鉴权参数时需自行拼接）
    std::string drtc_config_path;            ///< DRTC Native SDK 配置文件路径（drtc_config_node.json，文件路径非目录）；仅 Linux BUILD_WITH_DRTC_USER 场景需要，为空时飞机 DRTC 拉流 Init 会失败
    bool verify_ssl = true;                  ///< WSS/HTTPS 证书验证开关（安全默认开启）；校验服务器证书链与主机名/IP；私有化自签名部署须配 ssl_ca_path 指向 CA 文件；显式置 false 会产生 WARN 日志，仅限受控测试环境
    std::string ssl_ca_path;                 ///< WSS 私有 CA 证书文件路径（PEM 格式）；为空时使用系统 CA 信任库，私有化自签名部署应指向自有 CA 文件
    bool operator==(const FlightHubSdkConfig& other) const {
        return
            log_level == other.log_level &&
            log_to_console == other.log_to_console &&
            show_version_info == other.show_version_info &&
            log_file_path == other.log_file_path &&
            log_max_file_size == other.log_max_file_size &&
            log_max_files == other.log_max_files &&
            address == other.address &&
            port == other.port &&
            notification_server_address == other.notification_server_address &&
            drtc_config_path == other.drtc_config_path &&
            verify_ssl == other.verify_ssl &&
            ssl_ca_path == other.ssl_ca_path;
    }
};

struct Device {
    std::string device_sn;
    std::string device_class;
    std::string device_name;
    std::string nickname;   // 设备昵称(来自 devices: device_inside_callsign 字段)
    std::string model_key;  // 设备型号 key，用于机型判断（如网联飞机 EA340 系列）
    bool is_online = false;
    std::vector<std::string> parent_sns;  ///< 父设备 SN 列表（relay 等子设备场景）

    bool operator==(const Device& other) const {
        return
            device_sn == other.device_sn &&
            device_class == other.device_class &&
            device_name == other.device_name &&
            nickname == other.nickname &&
            model_key == other.model_key &&
            is_online == other.is_online &&
            parent_sns == other.parent_sns;
    }
};

struct DeviceTopo {
    Device device;
    std::vector<Device> sub_devices;

    bool operator==(const DeviceTopo& other) const {
        return device == other.device && sub_devices == other.sub_devices;
    }
};

/// 直播拉流地址信息。由后端 POST /pf-live/api/v2/stream/start 返回，供用户自行拉流播放。
struct LiveStreamStartResult {
    std::string expire_ts;      ///< 观众端拉流凭证过期时间戳（Unix 秒）。agora 为签发时间+60s（短期窗口，JWT 自身 exp 另有 365 天），volc 为签发时间+30min，drtc 恒为 0（过期时间编码在 url 的 query 参数中）
    std::string url_type;       ///< 直播流提供方类型。取值："agora"（WebRTC/WHEP）、"drtc"、"volc"
    std::string url;            ///< 拉流/加入频道的连接地址。agora 为 WHEP 地址（HTTP URL，含 token），drtc/volc 为 URL-encoded query string（含 app_id/room_id/token 等）
    std::string publish_uid;    ///< 推流端（设备端）在 RTC 频道中的用户标识。agora 为数字（如 "50000"），drtc 为设备 SN，volc 固定为 "device"
    std::string provider_url;   ///< DRTC 仲裁地址，仅 url_type="drtc" 时返回；其他 provider 为空

    bool operator==(const LiveStreamStartResult& other) const {
        return
            expire_ts == other.expire_ts &&
            url_type == other.url_type &&
            url == other.url &&
            publish_uid == other.publish_uid &&
            provider_url == other.provider_url;
    }
};

/// 单路可用直播源（LiveInfo）；由 live_capacity 解析，用于 getLiveStream/getLiveRecord，不表示已开流
struct LiveInfo {
    std::string device_sn;                  ///< 设备序列号
    std::string payload_index;              ///< 负载/摄像头索引
    std::string video_id;                   ///< 视频流索引
    std::vector<int> switchable_lens_types; ///< 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`）

    bool operator==(const LiveInfo& other) const {
        return
            device_sn == other.device_sn &&
            payload_index == other.payload_index &&
            video_id == other.video_id &&
            switchable_lens_types == other.switchable_lens_types;
    }
};

/// 单条直播录制记录。对应 GetLiveRecordsBySNResponse.RecordTask。
struct LiveRecordItem {
    std::string bid;         ///< 业务标识(bid)（录制任务 ID）
    std::string sn;          ///< 设备 SN
    int state = 0;           ///< 录制状态，取值语义同 `RecordState`（见 `value.h`）：0 成功/已结束，1 录制中，2 初始化中，3 失败
    std::string camera;      ///< 摄像头标识
    std::string created_by;  ///< 创建者
    long long duration = 0;  ///< 录制时长（秒）

    bool operator==(const LiveRecordItem& other) const {
        return
            bid == other.bid &&
            sn == other.sn &&
            state == other.state &&
            camera == other.camera &&
            created_by == other.created_by &&
            duration == other.duration;
    }
};

/// 直播录制状态推送信息
struct LiveRecordStateInfo {
    std::string bid;         ///< 业务标识(bid)
    std::string sn;          ///< 设备 SN
    std::string camera;      ///< 摄像头标识
    std::string video;       ///< 视频流标识
    int state = static_cast<int>(RecordState::RecordSuccess); ///< 录制状态，取值语义同 `flight_hub_sdk::RecordState`（见 `value.h`）
    std::string created_by;  ///< 创建者
    int duration = 0;        ///< 录制时长，单位：秒
    long long create_time = 0; ///< 创建时间戳

    bool operator==(const LiveRecordStateInfo& other) const {
        return
            bid == other.bid &&
            sn == other.sn &&
            camera == other.camera &&
            video == other.video &&
            state == other.state &&
            created_by == other.created_by &&
            duration == other.duration &&
            create_time == other.create_time;
    }
};

/// 获取在线观众数量 - 请求项（设备 + 摄像头列表）
struct DeviceAudienceCountRequestItem {
    std::string sn;
    std::vector<std::string> camera_list;

    bool operator==(const DeviceAudienceCountRequestItem& other) const {
        return sn == other.sn && camera_list == other.camera_list;
    }
};

/// 获取在线观众数量 - 可选流分组
struct AudienceCountGroup {
    std::string level_1_group;
    std::string level_2_group;

    bool operator==(const AudienceCountGroup& other) const {
        return level_1_group == other.level_1_group && level_2_group == other.level_2_group;
    }
};

/// 获取在线观众数量 - 响应项（设备 + 观众数）
struct DeviceAudienceCountItem {
    std::string sn;
    int count = 0;

    bool operator==(const DeviceAudienceCountItem& other) const {
        return sn == other.sn && count == other.count;
    }
};

// ---------- 航线任务（FlightTask Module，Workspace 下）----------
/// 分页信息
struct PaginationInfo {
    int page = 1;
    int page_size = 10;
    int total = 0;
    bool operator==(const PaginationInfo& o) const {
        return
            page == o.page &&
            page_size == o.page_size &&
            total == o.total;
    }
};

/// 航线任务列表查询参数
struct FlightTaskListParams {
    int page = 1;                    ///< 页码，从 1 开始
    int page_size = 10;              ///< 每页数量
    std::vector<std::string> sn;     ///< 设备 SN 过滤列表
    std::string begin_at;            ///< 起始时间过滤（秒级时间戳），需与 end_at 配合使用
    std::string end_at;              ///< 结束时间过滤（秒级时间戳）
    std::string name;                ///< 任务名称过滤（模糊搜索）
    std::vector<int> status;         ///< 任务状态过滤列表。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中
    int task_type = 0;               ///< 任务类型过滤：0=不筛选 1=立即任务 2=条件任务
    int media_upload_type = 0;       ///< 媒体上传类型过滤。枚举：0=全部, 1=上传至云端, 2=直传云云对接

    bool operator==(const FlightTaskListParams& o) const {
        return
            page == o.page &&
            page_size == o.page_size &&
            sn == o.sn &&
            begin_at == o.begin_at &&
            end_at == o.end_at &&
            name == o.name &&
            status == o.status &&
            task_type == o.task_type &&
            media_upload_type == o.media_upload_type;
    }
};


/// 查询一键起飞任务列表请求参数
struct GetCommandTaskListParams {
    int page = 1;                        ///< 页码，从 1 开始（必填）
    int page_size = 10;                  ///< 每页数量，1~100（必填）
    std::string sn;                      ///< 设备 SN 过滤（网关 SN），与 drone_sn 至少传一个
    std::string drone_sn;                ///< 飞机 SN 过滤，与 sn 至少传一个
    long long begin_at = -1;             ///< 开始时间（秒级时间戳），-1 表示不过滤，需与 end_at 配对
    long long end_at = -1;               ///< 结束时间（秒级时间戳），-1 表示不过滤，需与 begin_at 配对
    std::vector<int> status;             ///< 任务状态过滤列表。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时

    bool operator==(const GetCommandTaskListParams& o) const {
        return page == o.page && page_size == o.page_size
            && sn == o.sn && drone_sn == o.drone_sn
            && begin_at == o.begin_at && end_at == o.end_at
            && status == o.status;
    }
};

/// 一键起飞任务列表项
struct CommandTaskListItemData {
    std::string uuid;                            ///< 任务 UUID
    std::string sn;                              ///< 设备 SN（网关）
    std::string drone_sn;                        ///< 飞机 SN
    int status = 0;                              ///< 任务状态。枚举：0=待执行, 1=执行中, 2=已完成, 3=失败, 4=超时
    int err_code = 0;                            ///< 错误码（0 表示成功）
    std::string takeoff_mode;                    ///< 起飞模式（"Takeoff" / "TakeoffWithFlyTo"）
    double security_takeoff_height = 0.0;        ///< 安全起飞高度（米）
    int max_speed = 0;                           ///< 最大速度
    int rth_altitude = 0;                        ///< 返航高度（米）
    std::string created_at;                      ///< 创建时间（ISO 8601）
    std::string user_id;                         ///< 用户 ID

    bool operator==(const CommandTaskListItemData& o) const {
        return uuid == o.uuid && sn == o.sn && drone_sn == o.drone_sn
            && status == o.status && err_code == o.err_code
            && takeoff_mode == o.takeoff_mode
            && floatEqual(security_takeoff_height, o.security_takeoff_height)
            && max_speed == o.max_speed && rth_altitude == o.rth_altitude
            && created_at == o.created_at && user_id == o.user_id;
    }

    bool operator!=(const CommandTaskListItemData& o) const {
        return !(*this == o);
    }
};

/// 查询一键起飞任务列表响应
struct GetCommandTaskListResults {
    std::vector<CommandTaskListItemData> list;   ///< 任务列表
    PaginationInfo pagination;                  ///< 分页信息

    bool operator==(const GetCommandTaskListResults& o) const {
        return list == o.list && pagination == o.pagination;
    }

    bool operator!=(const GetCommandTaskListResults& o) const {
        return !(*this == o);
    }
};


/// 查询飞行任务轨迹请求参数
struct GetFlightTaskTrackParams {
    std::string task_uuid;               ///< 任务 UUID（必填）

    bool operator==(const GetFlightTaskTrackParams& o) const {
        return task_uuid == o.task_uuid;
    }
};

/// 轨迹点
struct FlightTrackPoint {
    long long timestamp = 0;             ///< 时间戳（秒级）
    double latitude = 0.0;               ///< 纬度
    double longitude = 0.0;              ///< 经度
    double height = 0.0;                 ///< 高度（米）

    bool operator==(const FlightTrackPoint& o) const {
        return timestamp == o.timestamp
            && floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height);
    }
};

/// 飞行轨迹信息
struct FlightTrackInfo {
    std::string track_id;                ///< 轨迹 ID
    std::string drone_sn;                ///< 无人机 SN
    int flight_distance = 0;             ///< 飞行距离（米）
    int flight_duration = 0;             ///< 飞行时长（秒）
    std::vector<FlightTrackPoint> points; ///< 轨迹点列表

    bool operator==(const FlightTrackInfo& o) const {
        return track_id == o.track_id && drone_sn == o.drone_sn
            && flight_distance == o.flight_distance
            && flight_duration == o.flight_duration
            && points == o.points;
    }
};

/// 查询飞行任务轨迹响应
struct GetFlightTaskTrackResults {
    std::string uuid;                    ///< 任务 UUID
    std::string run_at;                  ///< 任务执行时间（ISO 8601）
    std::string completed_at;            ///< 任务完成时间（ISO 8601）
    std::string name;                    ///< 任务名称
    int status = 0;                      ///< 任务状态（0~10，同航线任务状态枚举）
    std::string wayline_uuid;            ///< 航线 UUID
    FlightTrackInfo track;               ///< 飞行轨迹信息

    bool operator==(const GetFlightTaskTrackResults& o) const {
        return uuid == o.uuid && run_at == o.run_at
            && completed_at == o.completed_at && name == o.name
            && status == o.status && wayline_uuid == o.wayline_uuid
            && track == o.track;
    }

    bool operator!=(const GetFlightTaskTrackResults& o) const {
        return !(*this == o);
    }
};


/// 断点续飞信息
struct WaylineBreakPoint {
    int index = 0;                           ///< 断点索引
    int state = 0;                           ///< 断点状态
    double progress = 0.0;                   ///< 进度
    int wayline_id = 0;                      ///< 航线 ID
    int break_reason = 0;                    ///< 断点 Reason
    double latitude = 0.0;                   ///< 断点纬度
    double longitude = 0.0;                  ///< 断点经度
    double height = 0.0;                     ///< 断点高度（米）
    double attitude_head = 0.0;              ///< 机头朝向角度

    bool operator==(const WaylineBreakPoint& o) const {
        return index == o.index
            && state == o.state
            && floatEqual(progress, o.progress)
            && wayline_id == o.wayline_id
            && break_reason == o.break_reason
            && floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height)
            && floatEqual(attitude_head, o.attitude_head);
    }
};

/// 避障信息
struct WaylineObstacle {
    int id = 0;                              ///< 避障信息 ID
    int type = 0;                            ///< 避障点类型。枚举：0=绕行开始(Start), 1=绕行结束(Return), 2=避障刹停(Stop)
    long long timestamp = 0;                 ///< 时间戳，单位：毫秒
    double latitude = 0.0;                   ///< 纬度，单位 1/2^32*180度
    double longitude = 0.0;                  ///< 经度，单位 1/2^32*360度
    double height = 0.0;                     ///< 避障点高度（米）
    int wayline_id = 0;                      ///< 航线 ID
    int waypoint_index = 0;                  ///< 当前航点索引

    bool operator==(const WaylineObstacle& o) const {
        return id == o.id
            && type == o.type
            && timestamp == o.timestamp
            && floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height)
            && wayline_id == o.wayline_id
            && waypoint_index == o.waypoint_index;
    }
};

/// 航线任务详情
struct TaskDetail {
    std::string uuid;                ///< 任务 UUID
    std::string name;                ///< 任务名称
    int task_type = 0;               ///< 任务类型：1=立即任务 2=条件任务
    int status = 0;                  ///< 任务状态。枚举：0=待开始, 1=启动失败, 2=执行中, 3=已暂停, 4=已终止, 5=成功, 6=挂起, 7=超时, 8=部分执行, 9=准备中, 10=起飞排队中
    std::string sn;                  ///< 设备 SN
    std::string landing_dock_sn;     ///< 降落机场 SN
    std::string wayline_uuid;        ///< 航线 UUID
    std::string begin_at;            ///< 任务开始时间（ISO 8601）
    std::string end_at;              ///< 任务结束时间（ISO 8601）
    int out_of_control_action_in_flight = 0; ///< 飞行中失控动作。枚举：1=返航, 2=继续执行
    double rth_altitude = 0;         ///< 返航高度，单位：米
    int rth_mode = 0;                ///< 返航高度模式。枚举：0=智能高度, 1=设定高度
    int min_battery_capacity = 0;    ///< 最低电量要求，单位：%
    int wayline_precision_type = 0;  ///< 航线精度类型。枚举：0=GPS, 1=RTK
    std::vector<int> wayline_segment_indexes; ///< 航线分段索引列表
    int current_waypoint_index = 0;  ///< 当前航点索引
    int total_waypoints = 0;         ///< 总航点数
    std::string time_zone;           ///< 时区
    int device_type = 0;             ///< 设备类型。枚举：0=机场, 1=遥控器
    std::string bid;                 ///< 业务标识(bid)
    WaylineBreakPoint break_point;   ///< 断点续飞信息
    std::string completed_at;        ///< 完成时间（ISO 8601）
    std::string created_at;          ///< 创建时间（ISO 8601）
    std::string deleted_at;          ///< 删除时间（ISO 8601）
    bool is_break_point_resume = false; ///< 是否支持断点续飞
    int media_upload_status = 0;     ///< 媒体上传状态
    std::vector<WaylineObstacle> obstacles; ///< 避障信息列表
    std::string run_at;              ///< 实际执行时间（ISO 8601）
    std::vector<std::string> tags;   ///< 任务标签列表
    std::string updated_at;          ///< 更新时间（ISO 8601）
    double wayline_duration = 0;     ///< 航线预估时长，单位：秒

    bool operator==(const TaskDetail& o) const {
        return
            uuid == o.uuid &&
            name == o.name &&
            task_type == o.task_type &&
            status == o.status &&
            sn == o.sn;
    }
};

/// 航线任务列表查询结果
struct FlightTaskListResult {
    std::vector<TaskDetail> list;   ///< 任务详情列表
    PaginationInfo pagination;      ///< 分页信息
};

/// 创建航线任务请求
struct CreateTaskReq {
    std::string name;                ///< 任务名称（必填）
    std::string sn;                  ///< 起飞机场序列号（必填）
    int task_type = 1;               ///< 任务类型：1=立即任务 2=条件任务（须设置 begin_at/end_at 执行窗口）（必填）
    std::string time_zone;           ///< 时区（必填），如 Asia/Shanghai, UTC
    std::string wayline_uuid;        ///< 航线 UUID（必填）
    int out_of_control_action_in_flight = 1; ///< 飞行中失控动作（必填）。枚举：1=返航, 2=继续执行
    double rth_altitude = 0;         ///< 返航高度，单位：米（必填）
    int rth_mode = -1;               ///< 返航高度模式。枚举：0=智能高度, 1=设定高度。-1 表示未设置（可选，不传则使用后端默认值）
    long long begin_at = -1;         ///< 条件任务计划开始时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填。-1 未设置。创建条件任务时 begin_at 应不早于当前时间 2 分钟（最少提前 2 分钟预约）。服务端按分钟粒度每分钟扫描；超过 begin_at 后 timed_task_delay_threshold（默认 50 秒）仍未完成下发时任务为启动失败
    long long end_at = -1;           ///< 条件任务执行窗口结束时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填，须 end_at > begin_at。-1 未设置
    int device_type = -1;            ///< 设备类型。枚举：0=机场, 1=遥控器。-1 表示未设置（可选，不传则使用后端默认值）
    std::string landing_dock_sn;     ///< 降落机场 SN。为空时代表和起飞机场一致
    int min_battery_capacity = -1;   ///< 条件任务最低电量百分比。范围：0-100。-1 表示未设置（可选，不传则使用后端默认值）
    int wayline_precision_type = -1; ///< 航线精度类型。枚举：0=GPS, 1=RTK。-1 表示未设置（可选，不传则使用后端默认值）
    std::vector<int> wayline_segment_indexes; ///< 航线分段索引列表
    double wayline_duration = -1;    ///< 航线预估时长，单位：秒。-1 表示未设置
    std::string uuid;                ///< 任务 UUID（可选，用于指定 UUID）
    std::vector<std::string> tags;   ///< 任务标签列表。最多 10 个，每个长度 1-45 字符
    bool is_break_point_resume = false; ///< 是否为断点续飞任务。true 时须同时填写 break_point
    WaylineBreakPoint break_point;   ///< 断点信息（与 is_break_point_resume 一并写入 HTTP）
    int signal_type = 0;             ///< 信号类型：0=未开启信号增强，1=开启信号增强（漫游）。仅当为 1 时写入 HTTP body
    std::string resumable_status;    ///< 续飞策略（可选）。枚举：auto=自动续飞, manual=手动续飞。为空时不写入 HTTP body
};

/// 创建航线任务响应
struct CreateTaskResp {
    std::string uuid;                ///< 任务 UUID
    std::vector<int> wayline_validity_check_codes; ///< 航线有效性校验码列表
};

/// 编辑航线任务请求（全量替换，服务端不支持部分字段更新）。
/// 建议先调用 getFlightTaskDetail 获取当前任务完整数据，修改目标字段后全量传入。
/// 必填字段：name, task_type, time_zone, sn, wayline_uuid, out_of_control_action_in_flight, rth_altitude
struct EditTaskReq {
    std::string uuid;                ///< 任务 UUID
    std::string name;                ///< 任务名称
    int task_type = -1;              ///< 任务类型：1=立即任务 2=条件任务（须设置 begin_at/end_at）。-1 未设置
    std::string time_zone;           ///< 时区，如 Asia/Shanghai, UTC
    long long begin_at = -1;         ///< 条件任务计划开始时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填。-1 未设置。创建条件任务时 begin_at 应不早于当前时间 2 分钟（最少提前 2 分钟预约）。服务端按分钟粒度每分钟扫描；超过 begin_at 后 timed_task_delay_threshold（默认 50 秒）仍未完成下发时任务为启动失败
    long long end_at = -1;           ///< 条件任务执行窗口结束时间（秒级 Unix 时间戳，与 time_zone 对应）。task_type=2 必填，须 end_at > begin_at。-1 未设置
    std::string sn;                  ///< 起飞机场序列号
    std::string landing_dock_sn;     ///< 降落机场 SN。为空时代表和起飞机场一致
    std::string wayline_uuid;        ///< 航线 UUID
    std::vector<int> wayline_segment_indexes; ///< 航线分段索引列表
    int wayline_precision_type = -1; ///< 航线精度类型。枚举：0=GPS, 1=RTK。-1 表示未设置
    int out_of_control_action_in_flight = -1; ///< 飞行中失控动作。枚举：1=返航, 2=继续执行。-1 表示未设置
    double rth_altitude = -1;        ///< 返航高度，单位：米。-1 表示未设置
    int rth_mode = -1;               ///< 返航高度模式。枚举：0=智能高度, 1=设定高度。-1 表示未设置
    int min_battery_capacity = -1;   ///< 条件任务最低电量百分比。范围：0-100。-1 表示未设置
    bool is_break_point_resume = false; ///< 是否为断点续飞任务。true 时须同时填写 break_point
    std::vector<std::string> tags;   ///< 任务标签列表。最多 10 个，每个长度 1-45 字符
    WaylineBreakPoint break_point;   ///< 断点信息（与 is_break_point_resume 一并写入 HTTP）

};

/// 更新航线任务状态请求
struct UpdateStatusReq {
    std::string task_uuid;           ///< 任务 UUID
    int status = 0;                  ///< 目标状态。枚举：0=待开始, 6=已挂起（仅允许这两个值）
};

/// 批量更新航线任务状态请求
struct BatchUpdateStatusReq {
    std::vector<std::string> task_uuids; ///< 任务 UUID 列表，批量 1-50 个
    int status = 0;                  ///< 目标状态。枚举：0=待开始, 6=已挂起（仅允许这两个值）
};

/// 设备推荐位置坐标
struct DeviceRecommendationPosition {
    double latitude = 0;             ///< 纬度，单位：度
    double longitude = 0;            ///< 经度，单位：度
    double height = 0;               ///< 高度，单位：米
};

/// 设备推荐请求
struct DeviceRecommendationReq {
    std::vector<std::string> sn;                          ///< 设备 SN 列表
    std::vector<std::string> device_model_keys;           ///< 设备型号 key 列表
    int strategy_id = 1;                                  ///< 推荐策略 ID
    std::string prj_uuid;                                 ///< 项目 UUID
    std::string wayline_uuid;                             ///< 航线 UUID
    int wayline_height_type = 0;                          ///< 航线高度类型
    DeviceRecommendationPosition fly_to_position;         ///< 飞向目标位置。匹配 strategy_id=1 时传入
    DeviceRecommendationPosition wayline_start_position;  ///< 航线起点位置。匹配 strategy_id=2,5 时传入
    DeviceRecommendationPosition wayline_end_position;    ///< 航线终点位置。匹配 strategy_id=3 时传入
};

/// 推荐设备信息
struct RecommendedDevice {
    std::string drone_sn;            ///< 飞行器 SN
    std::string gateway_sn;          ///< 网关设备 SN
    double distance = 0;             ///< 距离，单位：米
    bool matched = false;            ///< 是否匹配
};

/// 设备推荐响应
struct DeviceRecommendationResp {
    std::vector<RecommendedDevice> devices; ///< 推荐设备列表
};

/// TCAS（防碰撞）信息
struct TCAInfo {
    std::string center_sn;           ///< 中心设备 SN
    std::string data_json;           ///< TCAS 数据 JSON
};

/// 获取 TCAS 信息列表响应
struct GetTCAInfosResp {
    std::vector<TCAInfo> tcas;       ///< TCAS 信息列表
};

/// 开始漫游请求（ATC StartRoaming）
struct StartRoamingParams {
    std::string drone_sn;            ///< 无人机 SN（必填）
    std::string flight_id;           ///< 架次 UUID（必填）
    int source_task_type = 1;        ///< 来源任务类型：1=航线 tasks，2=command_tasks（必填）
};

/// 设置漫游基站白名单请求
struct SetRoamingStationSnParams {
    std::string drone_sn;                        ///< 无人机 SN
    std::vector<std::string> station_sns;        ///< 基站 SN 列表（0–50）
};

/// 漫游基站校验失败原因（与 proto RoamingStationFailReason 对齐）
enum class RoamingStationFailReason {
    Unspecified = 0,          ///< 成功 / 未指定
    Not610 = 1,               ///< 设备型号非 EA610
    NotRoamingMode = 2,       ///< 子模式非漫游基站
    DeviceUnreachable = 3,    ///< 设备属性/型号查询失败或离线
};

/// 单个基站白名单校验/写入结果
struct RoamingStationSnResult {
    std::string station_sn;
    bool success = false;
    RoamingStationFailReason fail_reason = RoamingStationFailReason::Unspecified;
};

/// 设置漫游基站白名单响应
struct SetRoamingStationSnResult {
    std::vector<RoamingStationSnResult> results;
};

/// 飞行中任务 topo 项
struct InFlightTaskTopoItem {
    std::string drone_sn;
    std::string flight_id;
    std::string takeoff_sn;          ///< 起飞基站 SN
};

/// 查询飞行中任务 topo 响应
struct GetInFlightTaskToposResult {
    std::vector<InFlightTaskTopoItem> list;
};

/// 任务 topo 变更推送（WebSocket topic_task_topo_changed）
struct TaskTopoChangedEvent {
    std::string drone_sn;
    std::string flight_id;
    std::string takeoff_sn;
};

// 负载相机控制: 变焦参数;
struct CameraFocalLengthParams {
    std::string payload_index;  ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_type = static_cast<int>(CameraLensType::CameraLensType_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`）
    int zoom_factor = 0;  // 变焦倍数，可见光是2-200;

    bool operator==(const CameraFocalLengthParams& rhs) const {
        return
            (payload_index == rhs.payload_index) &&
            (camera_type == rhs.camera_type) &&
            (zoom_factor == rhs.zoom_factor);
    }
};


// 负载相机控制: 联动变焦参数;
struct LinkageZoomParams {
    std::string payload_index;  ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int state = 0;  // 联动变焦状态, 0=关闭, 1=开启;

    bool operator==(const LinkageZoomParams& rhs) const {
        return
            (payload_index == rhs.payload_index) &&
            (state == rhs.state);
    }
};


// 负载控制: 重置云台;
// Method: gimbal_reset;
struct GimbalResetParams {
    std::string payload_index;  ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int reset_mode = 0;         // 重置模式, 参考上云 API, { 0: "回中", 1: "向下", 2: "偏航回中", 3: "俯仰向下" };

    bool operator==(const GimbalResetParams& rhs) const {
        return (payload_index == rhs.payload_index) && (reset_mode == rhs.reset_mode);
    }
};



struct SwitchLiveLensParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int lens_type = static_cast<int>(CameraLensType::CameraLensType_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`）

    bool operator==(const SwitchLiveLensParams& other) const {
        return payload_index == other.payload_index && lens_type == other.lens_type;
    }
};

struct SetPhotoStorageSettingsParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::vector<std::string> photo_storage_settings;

    bool operator==(const SetPhotoStorageSettingsParams& other) const {
        return payload_index == other.payload_index && photo_storage_settings == other.photo_storage_settings;
    }
};

struct SetVideoStorageSettingsParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::vector<std::string> video_storage_settings;

    bool operator==(const SetVideoStorageSettingsParams& other) const {
        return payload_index == other.payload_index && video_storage_settings == other.video_storage_settings;
    }
};

struct SetNightSceneModeParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int mode = static_cast<int>(NightSceneMode::AUTO); ///< 取值语义同 `flight_hub_sdk::NightSceneMode`（见 `value.h`）

    bool operator==(const SetNightSceneModeParams& other) const {
        return payload_index == other.payload_index && mode == other.mode;
    }
};

/// 直播视频帧信息
struct FrameInfo {
    bool is_key_frame = false;  ///< 是否为关键帧（I 帧）

    bool operator==(const FrameInfo& other) const {
        return is_key_frame == other.is_key_frame;
    }
};

/// AI 检测目标信息
struct AiDetectionTarget {
    int target_id;          ///< 目标 ID
    int target_type;        ///< 目标类型
    int target_state;       ///< 目标状态
    int target_cx;          ///< 目标中心 x 坐标
    int target_cy;          ///< 目标中心 y 坐标
    int target_w;           ///< 目标宽度
    int target_h;           ///< 目标高度
    long target_distance;   ///< 目标距离

    bool operator==(const AiDetectionTarget& other) const {
        return
            target_id == other.target_id &&
            target_type == other.target_type &&
            target_state == other.target_state &&
            target_cx == other.target_cx &&
            target_cy == other.target_cy &&
            target_w == other.target_w &&
            target_h == other.target_h &&
            target_distance == other.target_distance;
    }
};

/// AI 检测 SEI 信息（包含检测目标列表）
struct AiSeiInfo {
    std::vector<AiDetectionTarget> targets;  ///< AI 检测目标列表

    bool operator==(const AiSeiInfo& other) const {
        return targets == other.targets;
    }
};

/// AR SEI 信息（相机姿态与位置）
struct ArSeiInfo {
    double camera_yaw = 0;    ///< 相机在 NED 坐标系下的偏航角，单位：度
    double camera_pitch = 0;  ///< 相机在 NED 坐标系下的俯仰角，单位：度
    double camera_roll = 0;   ///< 相机在 NED 坐标系下的横滚角，单位：度
    int camera_pos_x = 0;     ///< 相机在 GND 坐标系下的 X 位置，单位：mm
    int camera_pos_y = 0;     ///< 相机在 GND 坐标系下的 Y 位置，单位：mm
    int camera_pos_z = 0;     ///< 相机在 GND 坐标系下的 Z 位置，单位：mm
    int latitude = 0;         ///< GND 坐标系原点纬度，单位：1/2^32*180°
    int longitude = 0;        ///< GND 坐标系原点经度，单位：1/2^32*360°
    int gnd_origin_z = 0;     ///< GND 坐标系原点 Z 轴高度，单位：mm，向下为正
    int altitude = 0;         ///< 相对起飞点高度，单位：mm，向上为正

    bool operator==(const ArSeiInfo& other) const {
        return
            floatEqual(camera_yaw, other.camera_yaw) &&
            floatEqual(camera_pitch, other.camera_pitch) &&
            floatEqual(camera_roll, other.camera_roll) &&
            camera_pos_x == other.camera_pos_x &&
            camera_pos_y == other.camera_pos_y &&
            camera_pos_z == other.camera_pos_z &&
            latitude == other.latitude &&
            longitude == other.longitude &&
            gnd_origin_z == other.gnd_origin_z &&
            altitude == other.altitude;
    }
};

/// FOV SEI 信息（相机 cam_fov 协议物理量，非角度值）
/// @note 字段名 fov_horizontal/fov_vertical 为历史命名，对应协议 image_width/image_height。
///       水平/垂直视场角（度）需自行计算：2×atan(fov_horizontal/(2×cur_actual_focal_length)) 等。
struct FovSeiInfo {
    long fov_horizontal = 0;         ///< 有效成像宽度，单位 μm（微米），对应 image_width
    long fov_vertical = 0;           ///< 有效成像高度，单位 μm（微米），对应 image_height
    long cur_actual_focal_length = 0; ///< 当前镜头实际焦距，整型值 ÷1000 为 mm，对应 lens_focal_length

    bool operator==(const FovSeiInfo& other) const {
        return
            fov_horizontal == other.fov_horizontal &&
            fov_vertical == other.fov_vertical &&
            cur_actual_focal_length == other.cur_actual_focal_length;
    }
};

/// 智能辅助 SEI 信息
struct AssistantSeiInfo {
    bool is_auto_direction = false;  ///< 是否自动朝向
    int direction = 0;               ///< 朝向值

    bool operator==(const AssistantSeiInfo& other) const {
        return is_auto_direction == other.is_auto_direction && direction == other.direction;
    }
};

/// EIS（电子防抖）裁剪坐标点
struct CamCropPos {
    int x = 0;  ///< x 坐标
    int y = 0;  ///< y 坐标

    bool operator==(const CamCropPos& other) const {
        return x == other.x && y == other.y;
    }
};

/// EIS（电子防抖）SEI 信息（裁剪区域四角坐标）
struct EisSeiInfo {
    int version = 0;              ///< 版本号
    CamCropPos left_top;          ///< 裁剪区域左上角
    CamCropPos right_top;         ///< 裁剪区域右上角
    CamCropPos right_bottom;      ///< 裁剪区域右下角
    CamCropPos left_bottom;       ///< 裁剪区域左下角

    bool operator==(const EisSeiInfo& other) const {
        return
            version == other.version &&
            left_top == other.left_top &&
            right_top == other.right_top &&
            right_bottom == other.right_bottom &&
            left_bottom == other.left_bottom;
    }
};

/// 指点飞行参数
struct FlyToPointParams {
    double latitude;    ///< 目标点纬度，单位：度
    double longitude;   ///< 目标点经度，单位：度
    double height;      ///< 目标点高度，单位：米
    int max_speed;      ///< 最大飞行速度，单位：m/s

    // TODO(apifox): need_verify_flight_control (是否需要验证飞行控制权，默认 true)

    bool operator==(const FlyToPointParams& other) const {
        return
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude) &&
            floatEqual(height, other.height) &&
            max_speed == other.max_speed;
    }
};

/// 指点飞行目标点坐标
struct FlyToTargetPoint {
    double latitude  = 0.0;   ///< 纬度，单位：度
    double longitude = 0.0;   ///< 经度，单位：度
    double height    = 0.0;   ///< 高度，单位：米
};

/// 更新指点飞行目标点参数
struct FlyToUpdateTargetPointsParams {
    std::string                    fly_to_id;     ///< 当前执行中的指点飞行任务 ID
    FlyToTargetPoint               start_point;   ///< 起始点坐标（当前飞机位置）
    std::vector<FlyToTargetPoint>  points;        ///< 目标点坐标数组（至少 1 个）
    int                            max_speed = 0; ///< 最大飞行速度，单位：m/s

    // TODO(apifox): need_verify_flight_control (是否需要验证飞行控制权，默认 true)
};

/// 飞行器自主规划路径点
class FlyToDroneSelfPlanPathPoint {
public:
    double latitude = 0.0;    ///< 纬度，单位：度
    double longitude = 0.0;   ///< 经度，单位：度
    double height = 0.0;      ///< 高度，单位：米

    bool operator==(const FlyToDroneSelfPlanPathPoint& other) const {
        return
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude) &&
            floatEqual(height, other.height);
    }
};

struct CameraState {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_mode = static_cast<int>(CameraMode::CameraMode_UNKNOWN); ///< 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`）
    int record_time = 0;
    bool recording_state = false;
    int remain_record_duration = 0;
    bool photo_state = false;
    int remain_photo_num = 0;
    double interval_photo_interval = 0;
    int video_resolution = 0;                     ///< 通用视频分辨率
    int linkage_zoom_state = 0;                   ///< 联动变焦状态
    int photo_size = 0;                           ///< 照片尺寸
    int video_resolution_wide = 0;                ///< 广角视频分辨率
    int video_resolution_zoom = 0;                ///< 变焦视频分辨率
    int photo_size_wide = 0;                      ///< 广角照片尺寸
    int photo_size_zoom = 0;                      ///< 变焦照片尺寸
    int photo_format = 0;                         ///< 照片格式
    bool thermal_temp_exceed_range_valid = false; ///< 温度超出范围有效性
    bool thermal_super_resolution_enable = false; ///< 红外超分使能
    int isotherm_enable = 0;                      ///< 等温线使能
    int isotherm_roi_based_rendering_enable = 0;  ///< 等温线 ROI 渲染使能
    int isotherm_color_rendering_type = 0;        ///< 等温线颜色渲染类型
    int isotherm_lower_threshold = 0;             ///< 等温线下阈值
    int isotherm_upper_threshold = 0;             ///< 等温线上阈值

    bool operator==(const CameraState& other) const {
        return
            payload_index == other.payload_index &&
            camera_mode == other.camera_mode &&
            record_time == other.record_time &&
            recording_state == other.recording_state &&
            remain_record_duration == other.remain_record_duration &&
            photo_state == other.photo_state &&
            remain_photo_num == other.remain_photo_num &&
            floatEqual(interval_photo_interval, other.interval_photo_interval) &&
            video_resolution == other.video_resolution &&
            linkage_zoom_state == other.linkage_zoom_state &&
            photo_size == other.photo_size &&
            video_resolution_wide == other.video_resolution_wide &&
            video_resolution_zoom == other.video_resolution_zoom &&
            photo_size_wide == other.photo_size_wide &&
            photo_size_zoom == other.photo_size_zoom &&
            photo_format == other.photo_format &&
            thermal_temp_exceed_range_valid == other.thermal_temp_exceed_range_valid &&
            thermal_super_resolution_enable == other.thermal_super_resolution_enable &&
            isotherm_enable == other.isotherm_enable &&
            isotherm_roi_based_rendering_enable == other.isotherm_roi_based_rendering_enable &&
            isotherm_color_rendering_type == other.isotherm_color_rendering_type &&
            isotherm_lower_threshold == other.isotherm_lower_threshold &&
            isotherm_upper_threshold == other.isotherm_upper_threshold;
    }
};

struct LiveviewWorldRegion {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double bottom = 0;
    double left = 0;
    double right = 0;
    double top = 0;

    bool operator==(const LiveviewWorldRegion& other) const {
        return
            payload_index == other.payload_index &&
            floatEqual(bottom, other.bottom) &&
            floatEqual(left, other.left) &&
            floatEqual(right, other.right) &&
            floatEqual(top, other.top);
    }
};

// 载荷相机实时视图世界区域信息
class AircraftPayloadsCameraLiveviewWorldRegion {
public:
    std::unordered_map<std::string, LiveviewWorldRegion> payloads_list;

    bool operator==(const AircraftPayloadsCameraLiveviewWorldRegion& other) const {
        return payloads_list == other.payloads_list;
    }
};

struct CameraLensFovInfo {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int width = 0;
    int height = 0;
    int image_ratio = 0;
    int focal_length = 0;

    bool operator==(const CameraLensFovInfo& other) const {
        return
            payload_index == other.payload_index &&
            width == other.width &&
            height == other.height &&
            image_ratio == other.image_ratio &&
            focal_length == other.focal_length;
    }
};

struct CameraWideLensState {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int wide_calibrate_farthest_focus_value = 0;
    int wide_calibrate_nearest_focus_value = 0;
    int wide_exposure_mode = static_cast<int>(CameraExposureMode::CameraExposureMode_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`）
    int wide_exposure_value = static_cast<int>(CameraExposureCompensation::CameraExposureCompensation_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`）
    int wide_focus_mode = static_cast<int>(CameraFocusMode::CameraFocusMode_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`）
    int wide_focus_state = static_cast<int>(CameraFocusState::CameraFocusState_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`）
    int wide_focus_value = 0;
    int wide_iso = static_cast<int>(CameraISO::CameraISO_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`）
    int wide_max_focus_value = 0;
    int wide_min_focus_value = 0;
    int wide_shutter_speed = static_cast<int>(CameraShutterSpeed::CameraShutterSpeed_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`）
    bool wide_dewarping_state = false;
    bool wide_mechanical_shutter_state = false;
    int wide_aperture_value = static_cast<int>(CameraAperture::CameraAperture_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`）
    CameraLensFovInfo wide_fov_info; ///< 广角镜头 FOV 信息（画幅/焦距）

    bool operator==(const CameraWideLensState& other) const {
        return
            payload_index == other.payload_index &&
            wide_calibrate_farthest_focus_value == other.wide_calibrate_farthest_focus_value &&
            wide_calibrate_nearest_focus_value == other.wide_calibrate_nearest_focus_value &&
            wide_exposure_mode == other.wide_exposure_mode &&
            wide_exposure_value == other.wide_exposure_value &&
            wide_focus_mode == other.wide_focus_mode &&
            wide_focus_state == other.wide_focus_state &&
            wide_focus_value == other.wide_focus_value &&
            wide_iso == other.wide_iso &&
            wide_max_focus_value == other.wide_max_focus_value &&
            wide_min_focus_value == other.wide_min_focus_value &&
            wide_shutter_speed == other.wide_shutter_speed &&
            wide_dewarping_state == other.wide_dewarping_state &&
            wide_mechanical_shutter_state == other.wide_mechanical_shutter_state &&
            wide_aperture_value == other.wide_aperture_value &&
            wide_fov_info == other.wide_fov_info;
    }
};

struct CameraZoomLensState {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int zoom_calibrate_farthest_focus_value = 0;
    int zoom_calibrate_nearest_focus_value = 0;
    int zoom_exposure_mode = static_cast<int>(CameraExposureMode::CameraExposureMode_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraExposureMode`（见 `value.h`）
    int zoom_exposure_value = static_cast<int>(CameraExposureCompensation::CameraExposureCompensation_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraExposureCompensation`（见 `value.h`）
    double zoom_factor = 0;
    int zoom_focus_mode = static_cast<int>(CameraFocusMode::CameraFocusMode_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraFocusMode`（见 `value.h`）
    int zoom_focus_state = static_cast<int>(CameraFocusState::CameraFocusState_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraFocusState`（见 `value.h`）
    int zoom_focus_value = 0;
    int zoom_iso = static_cast<int>(CameraISO::CameraISO_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraISO`（见 `value.h`）
    int zoom_max_focus_value = 0;
    int zoom_min_focus_value = 0;
    int zoom_shutter_speed = static_cast<int>(CameraShutterSpeed::CameraShutterSpeed_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraShutterSpeed`（见 `value.h`）
    int zoom_aperture_value = static_cast<int>(CameraAperture::CameraAperture_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraAperture`（见 `value.h`）
    CameraLensFovInfo zoom_fov_info; ///< 变焦镜头 FOV 信息（画幅/焦距）

    bool operator==(const CameraZoomLensState& other) const {
        return
            payload_index == other.payload_index &&
            zoom_calibrate_farthest_focus_value == other.zoom_calibrate_farthest_focus_value &&
            zoom_calibrate_nearest_focus_value == other.zoom_calibrate_nearest_focus_value &&
            zoom_exposure_mode == other.zoom_exposure_mode &&
            zoom_exposure_value == other.zoom_exposure_value &&
            floatEqual(zoom_factor, other.zoom_factor) &&
            zoom_focus_mode == other.zoom_focus_mode &&
            zoom_focus_state == other.zoom_focus_state &&
            zoom_focus_value == other.zoom_focus_value &&
            zoom_iso == other.zoom_iso &&
            zoom_max_focus_value == other.zoom_max_focus_value &&
            zoom_min_focus_value == other.zoom_min_focus_value &&
            zoom_shutter_speed == other.zoom_shutter_speed &&
            zoom_aperture_value == other.zoom_aperture_value &&
            zoom_fov_info == other.zoom_fov_info;
    }
};

struct GimbalState {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double yaw = 0;
    double pitch = 0;
    double roll = 0;

    bool operator==(const GimbalState& other) const {
        return
            payload_index == other.payload_index &&
            floatEqual(yaw, other.yaw) &&
            floatEqual(pitch, other.pitch) &&
            floatEqual(roll, other.roll);
    }
};

// 摄像头osd 推送: measure_target;
struct CameraMeasureTarget {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double altitude = 0;
    double distance = 0;
    double latitude = 0;
    double longitude = 0;
    int laser_measure_state = static_cast<int>(CameraLaserMeasureState::CameraLaserMeasureState_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraLaserMeasureState`（见 `value.h`）

    bool operator==(const CameraMeasureTarget& other) const {
        return
            payload_index == other.payload_index &&
            floatEqual(altitude, other.altitude) &&
            floatEqual(distance, other.distance) &&
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude) &&
            laser_measure_state == other.laser_measure_state;
    }
};

struct NightModeSettings {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int night_mode = static_cast<int>(NightSceneMode::AUTO); ///< 取值语义同 `flight_hub_sdk::NightSceneMode`（见 `value.h`）
    bool is_working = false;
    int denoise_level = 0;                    ///< 降噪等级
    bool night_vision_enable = false;         ///< 夜视使能
    bool infrared_fill_light_enable = false;  ///< 红外补光使能
    int night_scene_mode_suggestion = 0;      ///< 夜景模式建议

    bool operator==(const NightModeSettings& other) const {
        return payload_index == other.payload_index &&
               night_mode == other.night_mode &&
               is_working == other.is_working &&
               denoise_level == other.denoise_level &&
               night_vision_enable == other.night_vision_enable &&
               infrared_fill_light_enable == other.infrared_fill_light_enable &&
               night_scene_mode_suggestion == other.night_scene_mode_suggestion;
    }
};

/// 相机水印设置（DRC `drc_camera_state_push.camera_state.camera_watermark_settings`）。
struct CameraWatermarkSettings {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int global_enable = 0;
    int drone_type_enable = 0;
    int manufacturer_enable = 0;
    int drone_sn_enable = 0;
    int datetime_enable = 0;
    int gps_enable = 0;
    int user_custom_string_enable = 0;
    std::string user_custom_string;
    int layout = 0;

    bool operator==(const CameraWatermarkSettings& other) const {
        return payload_index == other.payload_index &&
               global_enable == other.global_enable &&
               drone_type_enable == other.drone_type_enable &&
               manufacturer_enable == other.manufacturer_enable &&
               drone_sn_enable == other.drone_sn_enable &&
               datetime_enable == other.datetime_enable &&
               gps_enable == other.gps_enable &&
               user_custom_string_enable == other.user_custom_string_enable &&
               user_custom_string == other.user_custom_string &&
               layout == other.layout;
    }
};

/// 相机存储信息（DRC `drc_camera_state_push.camera_state.storage`）。
struct CameraStorageInfo {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int total = 0;   ///< 总容量
    int used = 0;    ///< 已用容量

    bool operator==(const CameraStorageInfo& other) const {
        return payload_index == other.payload_index &&
               total == other.total &&
               used == other.used;
    }
};

/// 红外测温区域（像素坐标；DRC `ir_pip_state.ir_meter_region`）。
struct CameraIrMeterRegion {
    int top_left_x = 0;
    int top_left_y = 0;
    int bottom_right_x = 0;
    int bottom_right_y = 0;

    bool operator==(const CameraIrMeterRegion& other) const {
        return top_left_x == other.top_left_x &&
               top_left_y == other.top_left_y &&
               bottom_right_x == other.bottom_right_x &&
               bottom_right_y == other.bottom_right_y;
    }
};

/// 红外画中画状态（DRC `drc_camera_state_push.camera_state.ir_pip_state`）。
struct CameraIrPipState {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int display_mode = 0;
    int enhance_mode = 0;
    int ir_mix_rate = 0;
    CameraIrMeterRegion ir_meter_region;

    bool operator==(const CameraIrPipState& other) const {
        return payload_index == other.payload_index &&
               display_mode == other.display_mode &&
               enhance_mode == other.enhance_mode &&
               ir_mix_rate == other.ir_mix_rate &&
               ir_meter_region == other.ir_meter_region;
    }
};

/// 等温线渲染多边形顶点（DRC 上行像素坐标，与已有下行 set 的 `IsothermPolygonPoint`（double 归一化*10000）不同）。
struct CameraIsothermPolygonPoint {
    int x = 0;
    int y = 0;

    bool operator==(const CameraIsothermPolygonPoint& other) const {
        return x == other.x && y == other.y;
    }
};

/// 媒体存储列表（DRC `drc_camera_state_push.media_storage`）。
struct CameraMediaStorage {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::vector<std::string> photo_storage_settings;
    std::vector<std::string> video_storage_settings;

    bool operator==(const CameraMediaStorage& other) const {
        return payload_index == other.payload_index &&
               photo_storage_settings == other.photo_storage_settings &&
               video_storage_settings == other.video_storage_settings;
    }
};

/// 去雾算法信息（DRC `drc_camera_state_push.algorithm_dehaze_info`）。
struct CameraAlgorithmDehazeInfo {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int mode = 0;
    int level = 0;
    bool is_working = false;

    bool operator==(const CameraAlgorithmDehazeInfo& other) const {
        return payload_index == other.payload_index &&
               mode == other.mode &&
               level == other.level &&
               is_working == other.is_working;
    }
};

/// 预录制信息（DRC `drc_camera_state_push.pre_recorded_info`）。
struct CameraPreRecordedInfo {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int status = 0;
    int pre_recorded_time = 0;

    bool operator==(const CameraPreRecordedInfo& other) const {
        return payload_index == other.payload_index &&
               status == other.status &&
               pre_recorded_time == other.pre_recorded_time;
    }
};

/// 基础照片存储格式能力。
struct CameraPhotoStorageFormat {
    int photo_storage_format_num = 0;
    std::vector<std::string> photo_storage_formats;

    bool operator==(const CameraPhotoStorageFormat& other) const {
        return photo_storage_format_num == other.photo_storage_format_num &&
               photo_storage_formats == other.photo_storage_formats;
    }
};

/// 相机能力集（DRC `drc_camera_state_push.camera_capability`）。
struct CameraCapability {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::vector<int> zoom_factor_range;   ///< 变焦倍数范围
    std::vector<int> ir_factor_range;     ///< 红外倍数范围
    CameraPhotoStorageFormat base_photo_storage_format;

    bool operator==(const CameraCapability& other) const {
        return payload_index == other.payload_index &&
               zoom_factor_range == other.zoom_factor_range &&
               ir_factor_range == other.ir_factor_range &&
               base_photo_storage_format == other.base_photo_storage_format;
    }
};

/// 可设置能力事件项。
struct CameraSettableEvent {
    int event_id = 0;
    int settable = 0;

    bool operator==(const CameraSettableEvent& other) const {
        return event_id == other.event_id && settable == other.settable;
    }
};

/// 相机可设置能力集（DRC `drc_camera_state_push.camera_settable_capability`）。
struct CameraSettableCapability {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::vector<CameraSettableEvent> events;

    bool operator==(const CameraSettableCapability& other) const {
        return payload_index == other.payload_index &&
               events == other.events;
    }
};

/// 可见能力事件项。
struct CameraVisibleCapabilityItem {
    int event_id = 0;
    int visible = 0;

    bool operator==(const CameraVisibleCapabilityItem& other) const {
        return event_id == other.event_id && visible == other.visible;
    }
};

/// 相机可见能力集（DRC `drc_camera_state_push.camera_visible_capability`）。
struct CameraVisibleCapability {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_mode = 0;
    std::vector<CameraVisibleCapabilityItem> events;

    bool operator==(const CameraVisibleCapability& other) const {
        return payload_index == other.payload_index &&
               camera_mode == other.camera_mode &&
               events == other.events;
    }
};

/// 相机等温线渲染多边形（DRC `drc_camera_state_push.camera_state.isotherm_rendering_polygon`）。
/// 拆成独立 listen 因为它是可变长数组，与主 CameraState 生命周期分离便于按变更回调。
struct CameraIsothermPolygon {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::vector<CameraIsothermPolygonPoint> points;

    bool operator==(const CameraIsothermPolygon& other) const {
        return payload_index == other.payload_index &&
               points == other.points;
    }
};

/// 相机拍照上行信息（DRC `drc_camera_photo_info_push` 全量透传）。
/// 与已有切片式 `listenPanoramaStatus` / `listenIntervalTakePhotoStatus` 并存；
/// 补齐 `result` 与非全景/定时模式下的其余字段。
///
/// `payload_index`：ME 上行**无此字段**。仅当本载荷已有 `drc_camera_state_push` 且
/// `camera_mode` 与本推送一致时填本载荷 index；否则为空字符串，调用方勿当作可靠路由键。
struct CameraPhotoInfo {
    std::string payload_index;                    ///< 见上：可能为空，非 ME 原字段
    int camera_mode = 0;                          ///< 相机模式
    int current_step = 0;                         ///< 当前步骤（多步拍摄）
    int percent = 0;                              ///< 进度百分比
    int countdown_time = 0;                       ///< 倒计时（秒）
    int result = 0;                               ///< 拍照结果（0 成功 / 非 0 各种失败码）
    std::string status;                           ///< 状态字符串（原始，未做枚举转换以避免丢信息）

    bool operator==(const CameraPhotoInfo& other) const {
        return payload_index == other.payload_index &&
               camera_mode == other.camera_mode &&
               current_step == other.current_step &&
               percent == other.percent &&
               countdown_time == other.countdown_time &&
               result == other.result &&
               status == other.status;
    }
};

/// 飞行器自主规划路径信息
class FlyToDroneSelfPlanPath {
public:
    std::string fly_to_id;    ///< 指点飞行任务 ID
    std::string sn;           ///< 飞行器 SN
    long timestamp = 0;       ///< 时间戳
    std::vector<FlyToDroneSelfPlanPathPoint> planned_path_points; ///< 规划路径点列表
    double remaining_distance = 0; ///< 剩余飞行距离，单位：米
    double remaining_time = 0;     ///< 剩余飞行时间，单位：秒

    bool operator==(const FlyToDroneSelfPlanPath& other) const {
        return
            fly_to_id == other.fly_to_id &&
            sn == other.sn &&
            timestamp == other.timestamp &&
            planned_path_points == other.planned_path_points &&
            floatEqual(remaining_distance, other.remaining_distance) &&
            floatEqual(remaining_time, other.remaining_time);
    }
};

/// 指点飞行进度信息
class FlyToPointProgress {
public:
    std::string bid;          ///< 业务标识(bid)
    std::string tid;          ///< 事务 ID
    long timestamp = 0;       ///< 时间戳
    std::string fly_to_id;    ///< 指点飞行任务 ID
    std::string flight_id;    ///< 飞行 ID
    int task_seq = 0;         ///< 任务序号
    std::string sn;           ///< 飞行器 SN
    std::string status;       ///< 执行状态
    int result = 0;           ///< 执行结果
    int waypoint_index = 0;   ///< 当前航点索引

    bool operator==(const FlyToPointProgress& other) const {
        return
            bid == other.bid &&
            tid == other.tid &&
            timestamp == other.timestamp &&
            fly_to_id == other.fly_to_id &&
            flight_id == other.flight_id &&
            task_seq == other.task_seq &&
            sn == other.sn &&
            status == other.status &&
            result == other.result &&
            waypoint_index == other.waypoint_index;
    }
};

/// DRC 虚拟摇杆杆量。中性值 1024（悬停/无输入）；建议操控范围 364–1684（1024±660）。
/// 松杆建议连续发送若干帧全 1024。建议发送频率约 10–20Hz。SDK 透传不裁剪，客户端宜自行 clamp 到建议范围。
class StickControlData {
public:
    int roll = 1024;            ///< 横滚杆量，右为正；中性 1024，建议范围 364–1684
    int pitch = 1024;           ///< 俯仰杆量，前为正；中性 1024，建议范围 364–1684
    int throttle = 1024;        ///< 油门杆量，上为正；中性 1024，建议范围 364–1684
    int yaw = 1024;             ///< 偏航杆量，右为正；中性 1024，建议范围 364–1684
    int gimbal_pitch = 1024;    ///< 云台俯仰杆量；中性 1024，建议范围 364–1684

    bool operator==(const StickControlData& other) const {
        return
            roll == other.roll &&
            pitch == other.pitch &&
            throttle == other.throttle &&
            yaw == other.yaw &&
            gimbal_pitch == other.gimbal_pitch;
    }
};

/// PSDK
// PSDK 设备连接状态
struct PsdkDeviceInfo {
    int psdk_index = 0;
    std::string object_key;

    bool operator==(const PsdkDeviceInfo& other) const {
        return psdk_index == other.psdk_index && object_key == other.object_key;
    }
};

// PSDK 设备列表
struct PsdkDeviceList {
    std::vector<PsdkDeviceInfo> psdk_device_list;

    bool operator==(const PsdkDeviceList& other) const {
        return psdk_device_list == other.psdk_device_list;
    }
};

struct PsdkStateSummaryInfo {
    int psdk_index = 0;
    int psdk_type = static_cast<int>(PsdkType::UNKNOWN); ///< 取值语义同 `flight_hub_sdk::PsdkType`（见 `value.h`）
    int psdk_sub_type = static_cast<int>(PsdkSubType::UNKNOWN); ///< 取值语义同 `flight_hub_sdk::PsdkSubType`（见 `value.h`）
    std::string psdk_name;
    std::string psdk_sn;
    std::string psdk_version;
    std::string psdk_lib_version;
    int psdk_restrict_state = -1; //默认无效状态; 0:限制功能 1:不限制 2:部分限制

    bool operator==(const PsdkStateSummaryInfo& other) const {
        return
            psdk_index == other.psdk_index &&
            psdk_type == other.psdk_type &&
            psdk_sub_type == other.psdk_sub_type &&
            psdk_name == other.psdk_name &&
            psdk_sn == other.psdk_sn &&
            psdk_version == other.psdk_version &&
            psdk_lib_version == other.psdk_lib_version &&
            psdk_restrict_state == other.psdk_restrict_state;
    }
};

struct PsdkStateSpeakerInfo {
    int work_mode = static_cast<int>(MegaphoneWorkMode::UNKNOWN); ///< `MegaphoneWorkMode`（`value.h`）
    int play_mode = static_cast<int>(MegaphonePlayMode::UNKNOWN); ///< `MegaphonePlayMode`
    int system_state = static_cast<int>(PSDKMegaphoneSystemState::UNKNOWN); ///< `PSDKMegaphoneSystemState`
    int play_volume = 0;
    std::string play_file_name;
    std::string play_file_md5;
    int tts_volume = 0;
    int tts_type = 0; //0：男生 1：女生
    int tts_language = 0;//0：中文 1：英文
    int tts_speed = 50;

    bool operator==(const PsdkStateSpeakerInfo& other) const {
        return
            work_mode == other.work_mode &&
            play_mode == other.play_mode &&
            system_state == other.system_state &&
            play_volume == other.play_volume &&
            play_file_name == other.play_file_name &&
            play_file_md5 == other.play_file_md5 &&
            tts_volume == other.tts_volume &&
            tts_type == other.tts_type &&
            tts_language == other.tts_language &&
            tts_speed == other.tts_speed;
    }
};

struct PsdkStateSearchlightInfo {
    int work_mode = static_cast<int>(SearchLightWorkMode::UNKNOWN); ///< `SearchLightWorkMode`（`value.h`）
    int brightness = 0;
    int calibration_status = 0;
    int calibration_progress = 0;
    double left_value = 0.0;
    double right_value = 0.0;
    int wide_field_mode = 0;
    int light_gimbal_control = 0;
    int lighting_type = 0;

    bool operator==(const PsdkStateSearchlightInfo& other) const {
        return
            work_mode == other.work_mode &&
            brightness == other.brightness &&
            calibration_status == other.calibration_status &&
            calibration_progress == other.calibration_progress &&
            floatEqual(left_value, other.left_value) &&
            floatEqual(right_value, other.right_value) &&
            wide_field_mode == other.wide_field_mode &&
            light_gimbal_control == other.light_gimbal_control &&
            lighting_type == other.lighting_type;
    }
};

struct PsdkSpeakerPlayProgressInfo {
    int psdk_index = 0;
    int result = 0;
    std::string status;
    std::string step_key;
    int percent = 0;
    std::string md5;

    bool operator==(const PsdkSpeakerPlayProgressInfo& other) const {
        return
            psdk_index == other.psdk_index &&
            result == other.result &&
            status == other.status &&
            step_key == other.step_key &&
            percent == other.percent &&
            md5 == other.md5;
    }
};

struct PsdkSpeakerTTSconfig {
    int volume = 0;
    int type = static_cast<int>(MegaphoneTTSVoiceType::MAN); ///< `MegaphoneTTSVoiceType`（`value.h`）
    int language = static_cast<int>(MegaphoneTTSLanguageType::CHINESE); ///< `MegaphoneTTSLanguageType`
    int speed = 0;

    bool operator==(const PsdkSpeakerTTSconfig& other) const {
        return
            volume == other.volume &&
            type == other.type &&
            language == other.language &&
            speed == other.speed;
    }
};

struct PsdkSpeakerAudioPlayInfo {
    std::string name;
    std::string url;
    std::string md5;
    std::string format = "pcm";

    bool operator==(const PsdkSpeakerAudioPlayInfo& other) const {
        return
            name == other.name &&
            url == other.url &&
            md5 == other.md5 &&
            format == other.format;
    }
};

struct PsdkSpeakerTTSPlayInfo {
    std::string name;
    std::string text;
    std::string md5;

    bool operator==(const PsdkSpeakerTTSPlayInfo& other) const {
        return
            name == other.name &&
            text == other.text &&
            md5 == other.md5;
    }
};

struct PsdkFloatingWindowTextInfo {
    int psdk_index = 0;
    std::string floating_window_text;

    bool operator==(const PsdkFloatingWindowTextInfo& other) const {
        return
            psdk_index == other.psdk_index &&
            floating_window_text == other.floating_window_text;
    }
};

struct PsdkSpeakerResource {
    int id = 0;
    std::string name;
    std::string file_type; // "audio" 或 "tts"
    std::string object_key;
    std::string object_url;
    std::string format;
    long size = 0;
    long duration = 0;
    std::string state;
    std::string state_message;
    std::string pcm_object_key;
    std::string pcm_object_url;
    long pcm_size = 0;
    std::string pcm_md5_sum;
    std::string content;
    std::string md5_sum;
    long created_at = 0;
    std::string opus_object_key;   ///< Opus 格式对象 key
    long opus_size = 0;            ///< Opus 文件大小
    std::string opus_md5_sum;      ///< Opus 文件 MD5

    bool operator==(const PsdkSpeakerResource& other) const {
        return
            id == other.id &&
            name == other.name &&
            file_type == other.file_type &&
            object_key == other.object_key &&
            object_url == other.object_url &&
            format == other.format &&
            size == other.size &&
            duration == other.duration &&
            state == other.state &&
            state_message == other.state_message &&
            pcm_object_key == other.pcm_object_key &&
            pcm_object_url == other.pcm_object_url &&
            pcm_size == other.pcm_size &&
            pcm_md5_sum == other.pcm_md5_sum &&
            content == other.content &&
            md5_sum == other.md5_sum &&
            created_at == other.created_at;
    }
};

struct PsdkSpeakerResourceList {
    std::vector<PsdkSpeakerResource> resources;

    bool operator==(const PsdkSpeakerResourceList& other) const {
        return resources == other.resources;
    }
};

/// 媒体文件列表 API 参数（getMediaFileList，project_id 由接口单独传入）
struct GetMediaFileListParams {
    int parent_id = 0;       // 0 或不传表示根目录
    int page = 1;
    int size = 30;
    bool get_sub_file = false;
    std::string sort_by;     // 如 "-create_at"

    bool operator==(const GetMediaFileListParams& other) const {
        return
            parent_id == other.parent_id &&
            page == other.page &&
            size == other.size &&
            get_sub_file == other.get_sub_file &&
            sort_by == other.sort_by;
    }
};

/// 媒体文件列表项（使用 int 与 HTTP 响应结构一致，避免 AIGCJson 解析问题）
struct MediaFileListItem {
    int id = 0;
    std::string uuid;
    int file_type = 0;
    int sub_file_type = 0;
    std::string name;
    std::string suffix;
    int size = 0;
    bool has_child = false;
    bool has_position = false;
    std::string create_at;
    std::string update_at;
    bool show_on_map = false;
    std::string preview_url;
    std::string original_url;
};

/// 媒体文件列表分页
struct MediaFileListPagination {
    int page = 1;
    int page_size = 30;
    int total = 0;
};

/// 媒体文件列表祖先节点（面包屑）
struct MediaFileListAncestor {
    std::string name;
    int id = 0;
    std::string uuid;
    int file_type = 0;
    int sub_file_type = 0;
};

/// 媒体文件列表结果
struct MediaFileListResult {
    std::vector<MediaFileListItem> list;
    MediaFileListPagination pagination;
    std::vector<MediaFileListAncestor> ancestors;
};

struct CreatePsdkSpeakerResourceParams {
    std::string name;
    std::string file_type;  // "audio" or "tts"
    std::string object_key; // required if file_type is "audio"
    long duration = 0;   // required if file_type is "audio" and create_scene is "record"
    std::string content;    // required if file_type is "tts"
    std::string create_scene; // "import" or "record"
    bool need_opus = false; // whether to convert to opus format
    long size = 0;             ///< 文件大小
    bool visible = false;      ///< 是否可见

    bool operator==(const CreatePsdkSpeakerResourceParams& other) const {
        return
            name == other.name &&
            file_type == other.file_type &&
            object_key == other.object_key &&
            duration == other.duration &&
            content == other.content &&
            create_scene == other.create_scene &&
            need_opus == other.need_opus &&
            size == other.size &&
            visible == other.visible;
    }
};

struct EditPsdkSpeakerResourceParams {
    std::string id;     // 服务端期望 string 类型
    std::string name;       // required
    std::string file_type;  // "audio" or "tts"
    bool visible = false;   // required if file_type is "audio"
    std::string content;    // required if file_type is "tts"

    bool operator==(const EditPsdkSpeakerResourceParams& other) const {
        return
            id == other.id &&
            name == other.name &&
            file_type == other.file_type &&
            visible == other.visible &&
            content == other.content;
    }
};

struct STSCredential {
    std::string access_key_id;
    std::string access_key_secret;
    long long expire = 0;
    std::string security_token;
    int platform = 0;  // 直播专用，后续再废弃

    bool operator==(const STSCredential& other) const {
        return
            access_key_id == other.access_key_id &&
            access_key_secret == other.access_key_secret &&
            expire == other.expire &&
            security_token == other.security_token &&
            platform == other.platform;
    }
};

struct STSInfo {
    std::string object_key_prefix;
    std::string bucket;
    std::string endpoint;
    std::string provider;
    std::string region;
    STSCredential credentials;

    bool operator==(const STSInfo& other) const {
        return
            object_key_prefix == other.object_key_prefix &&
            bucket == other.bucket &&
            endpoint == other.endpoint &&
            provider == other.provider &&
            region == other.region &&
            credentials == other.credentials;
    }
};

struct PsdkWidgetResource {
    std::string object_key;
    std::string url;

    bool operator==(const PsdkWidgetResource& other) const {
        return object_key == other.object_key && url == other.url;
    }
};

struct PsdkWidgetResourceList {
    std::vector<PsdkWidgetResource> ui_resource_urls;

    bool operator==(const PsdkWidgetResourceList& other) const {
        return ui_resource_urls == other.ui_resource_urls;
    }
};

struct GetPsdkWidgetResourceParams {
    std::vector<std::string> object_keys;  // required, dive, required

    bool operator==(const GetPsdkWidgetResourceParams& other) const {
        return object_keys == other.object_keys;
    }
};

struct ZoomAtTargetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = static_cast<int>(CameraLensType::CameraLensType_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`）
    double x = 0;  // 目标框左上角坐标x，0～1
    double y = 0;  // 目标框左上角坐标y，0～1
    double width = 0;  // 目标框宽度，0～1
    double height = 0;  // 目标框高度，0～1

    bool operator==(const ZoomAtTargetParams& other) const {
        return
            payload_index == other.payload_index &&
            camera_lens_type == other.camera_lens_type &&
            floatEqual(x, other.x) &&
            floatEqual(y, other.y) &&
            floatEqual(width, other.width) &&
            floatEqual(height, other.height);
    }
};


// (相机)负载控制: 长按画面拖动 — 已删除，请使用 CameraScreenDragParams + drcSendCameraScreenDrag

struct LookAtTargetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double latitude = 0;
    double longitude = 0;
    double height = 0;
    // 220锁机头

    bool operator==(const LookAtTargetParams& other) const {
        return
            payload_index == other.payload_index &&
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude) &&
            floatEqual(height, other.height);
    }
};

/// 云台拖拽参数（drc_camera_screen_drag）：画面拖动控制云台转动速度
struct CameraScreenDragParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    bool locked = true;       /// 是否锁机头
    double pitch_speed = 0;   /// 俯仰速度，度/秒
    double yaw_speed = 0;    /// 偏航速度，度/秒

    bool operator==(const CameraScreenDragParams& other) const {
        return
            payload_index == other.payload_index &&
            locked == other.locked &&
            floatEqual(pitch_speed, other.pitch_speed) &&
            floatEqual(yaw_speed, other.yaw_speed);
    }
};

struct PanoramaParam {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"

    bool operator==(const PanoramaParam& other) const {
        return payload_index == other.payload_index;
    }
};

struct PanoramaStatus {
    int current_step = static_cast<int>(PanoramaRunningState::PanoramaRunningState_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::PanoramaRunningState`（见 `value.h`）
    int percent = 0;  // 全景拍照进度，0～100
    int status = static_cast<int>(CameraPhotoState::CameraPhotoState_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraPhotoState`（见 `value.h`）

    bool operator==(const PanoramaStatus& other) const {
        return
            current_step == other.current_step &&
            percent == other.percent &&
            status == other.status;
    }
};

struct SwitchCameraModeParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_mode = static_cast<int>(CameraMode::CameraMode_UNKNOWN); ///< 相机模式整型码，取值语义同 `flight_hub_sdk::CameraMode`（见 `value.h`）

    bool operator==(const SwitchCameraModeParams& other) const {
        return payload_index == other.payload_index && camera_mode == other.camera_mode;
    }
};

/// 指令飞行 / 一键起飞参数。
/// 对应 **CommandRequest**（与 **DroneTakeoffRequest** 字段一致）。
/// SDK 实际请求：`POST /atc/api/v1/flight-tasks/command`；`device_sn` 由 Module 注入网关 SN，调用方无需填写。
/// 可选数值字段默认 **-1**、字符串默认空：表示未设置，BLL 不写入 JSON。
struct TakeoffParams {
    // --- 必填 ---
    double security_takeoff_height = 0;  ///< 安全起飞高度（米），范围 8-1500（必填）
    int max_speed = 0;                   ///< 最大速度（米/秒）（必填）
    int out_of_control_action = static_cast<int>(OutOfControlAction::UNKNOWN); ///< 失控动作（必填）。Hover/ReturnHome/Continue。参见 `OutOfControlAction`（`value.h`）
    int rth_altitude = 0;                ///< 返航高度（米），范围 15-1500（必填）
    double target_height = 0;            ///< 目标高度（米），范围 2-10000（必填）
    int takeoff_mode = static_cast<int>(TakeoffMode::UNKNOWN); ///< 起飞模式（必填）。Takeoff/TakeoffWithFlyTo。参见 `TakeoffMode`（`value.h`）

    // --- 坐标；随请求体下发 ---
    double target_latitude = 0;          ///< 目标纬度
    double target_longitude = 0;         ///< 目标经度

    // --- 可选（未设置则不传）---
    int commander_flight_mode = -1;      ///< 指点飞行模式：0-平稳模式，1-运动模式。-1 表示未设置
    double commander_flight_height = -1; ///< 指点飞行高度（米），范围 20-1500。-1 表示未设置
    int rth_mode = -1;                   ///< 返航模式：0-智能高度，1-设定高度。-1 表示未设置
    std::string cloud_to_cloud_id;       ///< 云云对接存储桶 ID。空串表示未设置
    int need_verify_flight_control = -1; ///< 是否需要验证飞行控制权。-1 未设置；0=false；1=true

    bool operator==(const TakeoffParams& other) const {
        return
            floatEqual(security_takeoff_height, other.security_takeoff_height) &&
            max_speed == other.max_speed &&
            out_of_control_action == other.out_of_control_action &&
            rth_altitude == other.rth_altitude &&
            floatEqual(target_height, other.target_height) &&
            floatEqual(target_latitude, other.target_latitude) &&
            floatEqual(target_longitude, other.target_longitude) &&
            takeoff_mode == other.takeoff_mode &&
            commander_flight_mode == other.commander_flight_mode &&
            floatEqual(commander_flight_height, other.commander_flight_height) &&
            rth_mode == other.rth_mode &&
            cloud_to_cloud_id == other.cloud_to_cloud_id &&
            need_verify_flight_control == other.need_verify_flight_control;
    }
};

/// 指令飞行 / 一键起飞结果。对应 CommandResponse / DroneTakeoffResponse。
struct TakeoffResult {
    std::string flight_id;  ///< 飞行任务 ID
    std::string fly_to_id;  ///< 指点飞行任务 ID
};


// 飞机下载星历 参数;
// DownloadEphemerisParams;
struct DownloadEphemerisParams {
    std::string ephemeris_url;        // ephemeris 文件 url;
    std::string ephemeris_file_name;  // ephemeris 文件名;


    bool operator==(const DownloadEphemerisParams& rhs) const {
        return
            ephemeris_url == rhs.ephemeris_url &&
            ephemeris_file_name == rhs.ephemeris_file_name;
    }

    bool operator!=(const DownloadEphemerisParams& rhs) const {
        return !(*this == rhs);
    }
}; 


// 开始/暂停进离场任务 InOutWaylineDetectParams
struct InOutWaylineDetectParams {
    std::string task_id;            // 任务id;
    int task_cmd;                   // 0 开始勘探选址; 1 终止勘探选址; 2 停止离场点视觉勘探; 3 开始离场点视觉勘探;
    bool is_auto_visual_scout;      // 是否自动勘探;
    double scout_relative_height;   // 离场点相对起飞高度 自动勘探需填;
    double patrol_relative_height;  // 方兴航线执行相对起飞高度;


    bool operator==(const InOutWaylineDetectParams& rhs) const {
        return
            task_id == rhs.task_id &&
            task_cmd == rhs.task_cmd &&
            is_auto_visual_scout == rhs.is_auto_visual_scout &&
            floatEqual(scout_relative_height, rhs.scout_relative_height) &&
            floatEqual(patrol_relative_height, rhs.patrol_relative_height);
    }

    bool operator!=(const InOutWaylineDetectParams& rhs) const {
        return !(*this == rhs);
    }
};


// 进离场航线飞行设置;
struct InOutWaylineFlightSettingParams {
    std::string sn;                // 设备序列号;
    double min_return_home_height;  // 最低返航高度，单位：米;
    int lost_contact_action;       // 失联动作。枚举：0=悬停, 1=返航, 2=自动降落;
    double max_height_limit;       // 最大高度限制，单位：米;
    double max_distance_limit;     // 最大距离限制，单位：米;

    bool operator==(const InOutWaylineFlightSettingParams& rhs) const {
        return
            sn == rhs.sn &&
            lost_contact_action == rhs.lost_contact_action &&
            floatEqual(min_return_home_height, rhs.min_return_home_height) &&
            floatEqual(max_height_limit, rhs.max_height_limit) &&
            floatEqual(max_distance_limit, rhs.max_distance_limit);
    }
    
    bool operator!=(const InOutWaylineFlightSettingParams& rhs) const {
        return !(*this == rhs);
    }
};


/// 直播画质设置参数
struct LiveQualitySettingParams {
    std::string sn;          ///< 设备序列号
    std::string camera;      ///< 摄像头标识
    std::string video;       ///< 视频流标识
    int video_quality;       ///< 视频质量等级：0=自适应 1=标清 4=超清
    // TODO(apifox): gateway_sn (网关设备 SN)


    bool operator==(const LiveQualitySettingParams& rhs) const {
        return
            sn == rhs.sn &&
            camera == rhs.camera &&
            video == rhs.video &&
            video_quality == rhs.video_quality;
    }

    bool operator!=(const LiveQualitySettingParams& rhs) const {
        return !(*this == rhs);
    }
};


/// 试飞航线生成 - 点位 (lng/lat/alt)
struct TryFlightWaylinePoint {
    double lng = 0;
    double lat = 0;
    double alt = 0;
};

/// 试飞航线生成 - 请求参数
struct GenerateTryFlightWaylineParams {
    std::string name;
    std::string device_type;
    TryFlightWaylinePoint target_point;
    TryFlightWaylinePoint observer_point;
};

/// 试飞航线生成 - 响应
struct GenerateTryFlightWaylineResponse {
    std::string key;
    std::string name;
    std::string uuid;
    std::string request_id;
};

/// 试飞航线下发 - 请求参数
struct CreateTestFlightTaskParams {
    std::string name;
    std::string wayline_uuid;
    std::string sn;
    std::string landing_dock_sn;
    int task_type = 1;  // 立即任务
    int out_of_control_action_in_flight = 1;
    int rth_mode = 1;
    int repeat_type = 0;
    std::string time_zone;
    std::string default_name;
    int wayline_precision_type = 1;
    double wayline_duration = 0.0;  // 航线预估执行时间，单位秒
    int rth_altitude = 100;  // 返航高度，单位米
    std::string resumable_status;
    std::string media_folder_id_paths;
    int device_type = 0;
};

/// 试飞航线下发 - 响应
struct CreateTestFlightTaskResponse {
    std::string uuid;
    int begin_at = 0;
    int end_at = 0;
};

struct LiveVideoInfo {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::string video_index;
    int video_type = static_cast<int>(CameraLensType::CameraLensType_UNKNOWN); ///< 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`）
    std::vector<int> switchable_video_types; ///< 取值语义同 `flight_hub_sdk::CameraLensType`（见 `value.h`）

    bool operator==(const LiveVideoInfo& other) const {
        return payload_index == other.payload_index &&
               video_index == other.video_index &&
               video_type == other.video_type &&
               switchable_video_types == other.switchable_video_types;
    }
};

struct ThermalGainModeInfo {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int thermal_gain_mode = static_cast<int>(IrGainMode::Auto); ///< 取值语义同 `flight_hub_sdk::IrGainMode`（见 `value.h`）

    bool operator==(const ThermalGainModeInfo& other) const {
        return
            payload_index == other.payload_index &&
            thermal_gain_mode == other.thermal_gain_mode;
    }
};

/// 红外镜头完整状态（DRC `drc_camera_osd_info_push.ir_lense` 全量透传）。
/// - 兼容说明：包含 `thermal_gain_mode`，`ThermalGainModeInfo` 为其子集，两者可并存。
struct CameraIrLensState {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    bool screen_split_enable = false;                      ///< 分屏开关（画中画）
    int ir_zoom_factor = 0;                                ///< IR 变焦倍率
    int thermal_current_palette_style = 0;                 ///< 当前调色板样式
    int thermal_gain_mode = static_cast<int>(IrGainMode::Auto); ///< 增益模式（对应 IrGainMode 枚举整型）
    double thermal_global_temperature_max = 0.0;           ///< 全局最高温（°C）
    double thermal_global_temperature_min = 0.0;           ///< 全局最低温（°C）
    int thermal_isotherm_lower_limit = 0;                  ///< 等温线下限（%）
    int thermal_isotherm_state = 0;                        ///< 等温线状态（0/1）
    int thermal_isotherm_upper_limit = 0;                  ///< 等温线上限（%）
    CameraLensFovInfo ir_fov_info;                         ///< IR 镜头 FOV 信息

    bool operator==(const CameraIrLensState& other) const {
        return
            payload_index == other.payload_index &&
            screen_split_enable == other.screen_split_enable &&
            ir_zoom_factor == other.ir_zoom_factor &&
            thermal_current_palette_style == other.thermal_current_palette_style &&
            thermal_gain_mode == other.thermal_gain_mode &&
            floatEqual(thermal_global_temperature_max, other.thermal_global_temperature_max) &&
            floatEqual(thermal_global_temperature_min, other.thermal_global_temperature_min) &&
            thermal_isotherm_lower_limit == other.thermal_isotherm_lower_limit &&
            thermal_isotherm_state == other.thermal_isotherm_state &&
            thermal_isotherm_upper_limit == other.thermal_isotherm_upper_limit &&
            ir_fov_info == other.ir_fov_info;
    }
};

struct ThermalGainModeSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int thermal_gain_mode = static_cast<int>(IrGainMode::Auto); ///< 取值语义同 `flight_hub_sdk::IrGainMode`（见 `value.h`）

    bool operator==(const ThermalGainModeSetParams& other) const {
        return
            payload_index == other.payload_index &&
            thermal_gain_mode == other.thermal_gain_mode;
    }
};

// 飞行任务异常详情信息
struct FlightTaskExceptionDetail {
    int code = 0;
    std::string message;
    int task_id = 0;
    std::string happen_at; // 时间字符串，如 "2025-09-17T04:01:00.972477409Z"
    std::string sn;

    bool operator==(const FlightTaskExceptionDetail& other) const {
        return
            code == other.code &&
            message == other.message &&
            task_id == other.task_id &&
            happen_at == other.happen_at &&
            sn == other.sn;
    }
};

// 飞行任务异常信息
struct FlightTaskException {
    std::string uuid;
    std::string name;
    std::string sn;
    int task_type = 0;
    int status = 0;  // 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中
    int progress = 0;
    std::vector<FlightTaskExceptionDetail> exceptions;
    long timestamp = 0;

    bool operator==(const FlightTaskException& other) const {
        return
            uuid == other.uuid &&
            name == other.name &&
            sn == other.sn &&
            task_type == other.task_type &&
            status == other.status &&
            progress == other.progress &&
            exceptions == other.exceptions &&
            timestamp == other.timestamp;
    }
};

// 设备变更信息
struct DeviceChange {
    std::string change_type; // "add" or "reduce"
    std::vector<std::string> change_devices;

    bool operator==(const DeviceChange& other) const {
        return change_type == other.change_type && change_devices == other.change_devices;
    }
};

// 工作空间设备变更信息
struct WorkspaceDeviceChange {
    std::vector<DeviceChange> device_changes;

    bool operator==(const WorkspaceDeviceChange& other) const {
        return device_changes == other.device_changes;
    }
};

// 机场位置信息
class DockLocation {
public:
    double heading = 0.0; // 朝向
    double height = 0.0; // 高度
    double latitude = 0.0; // 纬度
    double longitude = 0.0; // 经度

    bool operator==(const DockLocation& other) const {
        return
            floatEqual(heading, other.heading) &&
            floatEqual(height, other.height) &&
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude);
    }
};

// 机场离场点信息
class DockDeparturePoint {
public:
    double longitude = 0.0;  // 经度
    double latitude = 0.0;   // 纬度
    double height = 0.0;     // 椭球高度

    bool operator==(const DockDeparturePoint& other) const {
        return
            floatEqual(longitude, other.longitude) &&
            floatEqual(latitude, other.latitude) &&
            floatEqual(height, other.height);
    }
};

// 机场内飞机信息
class DroneInDock {
public:
    int drone_in_dock = 0;

    bool operator==(const DroneInDock& other) const {
        return drone_in_dock == other.drone_in_dock;
    }
};

// 机场舱盖状态信息;
class DockCoverState {
public:
    int cover_state = 0;  // 0: 关闭, 1: 打开, 2: 半开, 3: 舱盖状态异常;

    bool operator==(const DockCoverState& rhs) const {
        return cover_state == rhs.cover_state;
    }
};

// 机场飞机充电状态;
class DockDroneChargeState {
public:
    int state = 0;              // 0: 空闲, 1: 正在充电;
    int capacity_percent = 0;

    bool operator==(const DockDroneChargeState& rhs) const {
        return (state == rhs.state) && (capacity_percent == rhs.capacity_percent);
    }
};

// 机场气象信息;
class DockMeteorologicalInfo {
public:
    double temperature = 0.0;                   // 舱内温度;
    double environment_temperature = 0.0;       // 环境温度;
    int humidity = 0;                           // 舱内湿度;
    double wind_speed = 0.0;                    // 风速;
    int rainfall = 0;                           // 雨量;

    bool operator==(const DockMeteorologicalInfo& rhs) const {
        return
            floatEqual(temperature, rhs.temperature) &&
            floatEqual(environment_temperature, rhs.environment_temperature) &&
            (humidity == rhs.humidity) &&
            floatEqual(wind_speed, rhs.wind_speed) &&
            (rainfall == rhs.rainfall);
    }
};

// 机场备用电池信息;
class DockBackupBatteryInfo {
public:
    int switch_state = 0;            // 备用电池开关(0: 备用电池关闭, 1: 备用电池开启);
    int voltage = 0;                 // 备用电池电压(单位: 毫伏, mV);
    double temperature = 0.0;        // 备用电池温度(单位: 摄氏度);

    bool operator==(const DockBackupBatteryInfo& rhs) const {
        return
            (switch_state == rhs.switch_state) &&
            (voltage == rhs.voltage) &&
            floatEqual(temperature, rhs.temperature);
    }
};

// 机场保养信息;
class DockMaintainStatus {
public:
    int state = 0;                         // 保养状态(0: 未保养, 1: 保养中);
    int last_maintain_type = 0;            // 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养);
    long last_maintain_time = 0;           // 上一次保养时间, utc 秒时间戳(单位: 秒); // TODO: long long 类型 AIGC 解析出错;
    int last_maintain_work_sorties = 0;    // 上一次保养时飞行架次, [0, 2147483647];

    bool operator==(const DockMaintainStatus& rhs) const {
        return
            (state == rhs.state) &&
            (last_maintain_type == rhs.last_maintain_type) &&
            (last_maintain_time == rhs.last_maintain_time) &&
            (last_maintain_work_sorties == rhs.last_maintain_work_sorties);
    }
};

// 机场保养信息列表;
class DockMaintainStatusInfo {
public:
    std::vector<DockMaintainStatus> maintain_status_info;

    bool operator==(const DockMaintainStatusInfo& rhs) const {
        return maintain_status_info == rhs.maintain_status_info;
    }
};


// 机场空调工作模式信息
class DockAirConditionerStatusInfo {
public:
    int air_conditioner_state = static_cast<int>(DockAirConditionerState::DockAirConditionerState_UNKNOWN); ///< `DockAirConditionerState`（`value.h`）
    int switch_time = 0;
    bool operator==(const DockAirConditionerStatusInfo& other) const {
        return air_conditioner_state == other.air_conditioner_state && switch_time == other.switch_time;
    }
};

// 机场补光灯状态信息
class DockSupplementLightState {
public:
    int supplement_light_state = 0;
    bool operator==(const DockSupplementLightState& other) const {
        return supplement_light_state == other.supplement_light_state;
    }
};

/// 机场 mode_code（listenDockModeCode）。
class DockModeCode {
public:
    /**
     * 机场模式码取值：
     *   0  空闲中
     *   1  现场调试中
     *   2  远程调试中
     *   3  设备升级中
     *   4  设备作业中（细分见 flighttask_step_code）
     *   5  机场设备未标定
     *   6  节能状态中
     *   7  现场急停中
     *   8  换电中
     *   9  日志拉取中
     */
    int mode_code = 0;

    bool operator==(const DockModeCode &rhs) const {
        return mode_code == rhs.mode_code;
    }
};

// 设备拓扑变更信息
struct DeviceTopoChange {
    std::string gateway_sn;
    std::vector<std::string> devices; // 设备列表

    bool operator==(const DeviceTopoChange& other) const {
        return gateway_sn == other.gateway_sn && devices == other.devices;
    }
};

// 简化的自定义 SEI 数据结构定义

/// SEI 自定义数据（透传字节流）
struct SeiData {
    std::vector<int> data;  ///< 自定义字节数据

    bool operator==(const SeiData& other) const {
        return data == other.data;
    }
};

/// SEI 目标检测框
struct SeiDetectionBox {
    int id = 0;             ///< 目标 ID
    int type = 0;           ///< 目标类型
    int state = 0;          ///< 目标状态
    int x = 0;              ///< 检测框左上角 x 坐标，单位：像素
    int y = 0;              ///< 检测框左上角 y 坐标，单位：像素
    int width = 0;          ///< 检测框宽度，单位：像素
    int height = 0;         ///< 检测框高度，单位：像素
    int confidence = 0;     ///< 置信度（0-100）

    bool operator==(const SeiDetectionBox& other) const {
        return
            id == other.id &&
            type == other.type &&
            state == other.state &&
            x == other.x &&
            y == other.y &&
            width == other.width &&
            height == other.height &&
            confidence == other.confidence;
    }
};

/// SEI 2D 坐标点
struct SeiPoint2D {
    int x = 0;  ///< x 坐标，单位：像素
    int y = 0;  ///< y 坐标，单位：像素

    bool operator==(const SeiPoint2D& other) const {
        return x == other.x && y == other.y;
    }
};

/// SEI 多边形检测区域
struct SeiDetectionPolygon {
    int id = 0;             ///< 目标 ID
    int type = 0;           ///< 目标类型
    int state = 0;          ///< 目标状态
    int confidence = 0;     ///< 置信度（0-100）
    std::vector<SeiPoint2D> points;  ///< 多边形顶点列表

    bool operator==(const SeiDetectionPolygon& other) const {
        return
            id == other.id &&
            type == other.type &&
            state == other.state &&
            confidence == other.confidence &&
            points == other.points;
    }
};

/// SEI 文本叠加
struct SeiTextOverlay {
    int id = 0;             ///< 叠加项 ID
    int x = 0;              ///< 文本位置 x，单位：像素
    int y = 0;              ///< 文本位置 y，单位：像素
    int font_size = 12;     ///< 字体大小
    int color_r = 255;      ///< 颜色红色分量（0-255）
    int color_g = 255;      ///< 颜色绿色分量（0-255）
    int color_b = 255;      ///< 颜色蓝色分量（0-255）
    int alpha = 255;        ///< 透明度（0-255，255 为完全不透明）
    std::string text;       ///< 文本内容

    bool operator==(const SeiTextOverlay& other) const {
        return
            id == other.id &&
            x == other.x &&
            y == other.y &&
            font_size == other.font_size &&
            color_r == other.color_r &&
            color_g == other.color_g &&
            color_b == other.color_b &&
            alpha == other.alpha &&
            text == other.text;
    }
};

/// SEI 数据项（联合体风格，通过 type 字段区分实际数据类型）
struct SeiItem {
    int type = static_cast<int>(SeiDataType::UNKNOWN); ///< 数据类型，取值语义同 `flight_hub_sdk::SeiDataType`（见 `value.h`）
    SeiData custom_data;                ///< 自定义数据（dataType=CUSTOM_DATA 时有效）
    SeiDetectionBox detection_box;      ///< 目标检测框（dataType=DETECTION_BOX 时有效）
    SeiDetectionPolygon detection_polygon; ///< 多边形检测区域（dataType=DETECTION_POLYGON 时有效）
    SeiTextOverlay text_overlay;        ///< 文本叠加（dataType=TEXT_OVERLAY 时有效）

    // 构造函数
    SeiItem(const SeiData& data)
        : type(static_cast<int>(SeiDataType::CUSTOM_DATA)), custom_data(data) {}

    SeiItem(const SeiDetectionBox& box)
        : type(static_cast<int>(SeiDataType::DETECTION_BOX)), detection_box(box) {}

    SeiItem(const SeiDetectionPolygon& polygon)
        : type(static_cast<int>(SeiDataType::DETECTION_POLYGON)), detection_polygon(polygon) {}

    SeiItem(const SeiTextOverlay& text)
        : type(static_cast<int>(SeiDataType::TEXT_OVERLAY)), text_overlay(text) {}

    bool operator==(const SeiItem& other) const {
        if (type != other.type) return false;

        switch (static_cast<SeiDataType>(type)) {
            case SeiDataType::CUSTOM_DATA:
                return custom_data == other.custom_data;
            case SeiDataType::DETECTION_BOX:
                return detection_box == other.detection_box;
            case SeiDataType::DETECTION_POLYGON:
                return detection_polygon == other.detection_polygon;
            case SeiDataType::TEXT_OVERLAY:
                return text_overlay == other.text_overlay;
            default:
                return false;
        }
    }
};

/// SEI 头部信息
struct SeiHeader {
    int version = 1;       ///< 协议版本号
    int reserved = 0;      ///< 保留字段
    int data_size = 0;     ///< 数据大小（字节），编码时自动计算
    long timestamp = 0;    ///< 时间戳，单位：毫秒

    bool operator==(const SeiHeader& other) const {
        return
            version == other.version &&
            reserved == other.reserved &&
            data_size == other.data_size &&
            timestamp == other.timestamp;
    }
};

/// 航点坐标信息
class Waypoint {
public:
    double latitude = 0.0;   ///< 纬度，单位：度
    double longitude = 0.0;  ///< 经度，单位：度
    double height = 0.0;     ///< 高度，单位：米

    bool operator==(const Waypoint& other) const {
        return
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude) &&
            floatEqual(height, other.height);
    }
};

/// 无人机 POI 环绕进度事件信息
class PoiStatusEvent {
public:
    std::string device_sn;       ///< 机场设备 SN
    std::string drone_sn;        ///< 飞行器 SN
    Waypoint center_point;       ///< POI 中心点坐标
    double circle_radius = 0.0;  ///< 环绕半径，单位：米
    double circle_speed = 0.0;   ///< 当前环绕速度，单位：m/s
    double max_circle_speed = 0.0; ///< 最大环绕速度，单位：m/s
    int reason = 0;              ///< 状态变更原因
    std::string status;          ///< 当前执行状态
    long timestamp = 0;          ///< 时间戳
    long execute_duration = 0;   ///< 已执行时长
    std::string payload_index;   ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"

    bool operator==(const PoiStatusEvent& other) const {
        return
            device_sn == other.device_sn &&
            drone_sn == other.drone_sn &&
            center_point == other.center_point &&
            floatEqual(circle_radius, other.circle_radius) &&
            floatEqual(circle_speed, other.circle_speed) &&
            floatEqual(max_circle_speed, other.max_circle_speed) &&
            reason == other.reason &&
            status == other.status &&
            timestamp == other.timestamp &&
            execute_duration == other.execute_duration &&
            payload_index == other.payload_index;
    }
};

/// POI 环绕模式进入参数
class POIEnterParams {
public:
    std::string payload_index;    ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    Waypoint poi_center_point;    ///< POI 中心点坐标
    double speed = 0.0;           ///< 环绕速度，单位：m/s
    double circle_radius = 0.0;   ///< 环绕半径，单位：米

    // TODO(apifox): need_verify_flight_control (是否需要验证飞行控制权，默认 true)

    bool operator==(const POIEnterParams& other) const {
        return
            payload_index == other.payload_index &&
            poi_center_point == other.poi_center_point &&
            floatEqual(speed, other.speed) &&
            floatEqual(circle_radius, other.circle_radius);
    }
};

/// POI 任务信息
class POITaskInfo {
public:
    std::string uuid;                 ///< POI 任务 UUID
    std::string device_sn;            ///< 设备 SN（机场/遥控器）
    std::string sn;                   ///< 飞行器 SN
    int status = 0;                   ///< POI任务状态：0-未开始，1-执行中，2-暂停，3-已完成，4-失败
    int err_code = 0;                 ///< 错误码（任务失败时的错误代码）
    Waypoint poi_center_point;        ///< POI 中心点坐标（经纬度和高度）
    double speed = 0.0;               ///< 环绕速度，单位：m/s
    double circle_radius = 0.0;       ///< 环绕半径，单位：米
    std::string payload_index;        ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    
    bool operator==(const POITaskInfo& other) const {
        return
            uuid == other.uuid &&
            device_sn == other.device_sn &&
            sn == other.sn &&
            status == other.status &&
            err_code == other.err_code &&
            poi_center_point == other.poi_center_point &&
            floatEqual(speed, other.speed) &&
            floatEqual(circle_radius, other.circle_radius) &&
            payload_index == other.payload_index;
    }
};

struct IntervalTakePhotoSetParam {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double interval = 0.0;
};

struct IntervalTakePhotoParam {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
};

struct IntervalTakePhotoStatus {
    int countdown_time = 0;

    bool operator==(const IntervalTakePhotoStatus& o) const {
        return countdown_time == o.countdown_time;
    }
};

struct PhotoSizeParam {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int photo_size = 0;
    std::string camera_type;
};

struct CameraAimParam {
    std::string camera_type;
    bool locked = false;
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double x = 0.0;
    double y = 0.0;
};

struct AiIdentifySetParam {
    int on = 0;
};

/// 设置 AI 识别过滤；2.0 须填 index + light_type，1.0 可只填 filters（index/light_type 默认 0）
struct AiIdentifyFilterSetParam {
    int index = 0;           ///< 模型索引；DJI 官方 0，三方 ≥128
    int light_type = 0;      ///< 光线类型：0 无效 / 1 广角 / 2 变焦 / 3 红外
    std::vector<int> filters;
};

/// 设置 AI 识别置信度；2.0 须填 index + light_type，1.0 可只填 score
struct AiIdentifyScoreSetParam {
    int index = 0;           ///< 模型索引；DJI 官方 0，三方 ≥128
    int light_type = 0;      ///< 光线类型：0 无效 / 1 广角 / 2 变焦 / 3 红外
    int score = 0;           ///< 置信度分数，范围 0 ～ 100
};

/// 设置 AI 识别置信度模式；2.0 须填 index + light_type
struct AiIdentifyScoreModeSetParam {
    int index = 0;           ///< 模型索引；DJI 官方 0，三方 ≥128
    int light_type = 0;      ///< 光线类型：0 无效 / 1 广角 / 2 变焦 / 3 红外
    int score_mode = 0;      ///< 0:无效值 1:计数模式 2:搜救模式 3:用户自定义模式
};

/// 重置 AI 识别置信度；2.0 须填 index + light_type
struct AiIdentifyScoreResetParam {
    int index = 0;           ///< 模型索引；DJI 官方 0，三方 ≥128
    int light_type = 0;      ///< 光线类型：0 无效 / 1 广角 / 2 变焦 / 3 红外
};


struct AiModelItem {
    int index = 0; // AI模型编号 (三方模型从 0x80 开始，当架次有效)
    std::string signed_name; // 开发者签发时自定义的模型名 (可读字符名称)
    bool operator==(const AiModelItem& other) const {
        return index == other.index && signed_name == other.signed_name;
    }
};

struct AiModelLabel {
    int index = 0;    //AI模型识别类型编号
    std::string name; //AI模型识别类型名称
    bool operator==(const AiModelLabel& other) const {
        return index == other.index && name == other.name;
    }
};

struct SelectedAiModel {
    int index = 0;  // 当前生效的AI模型， 0 表示DJI默认模型，其他表示用户自定义模型
    int score = 0; // 置信度分数，范围0 ～ 100
    int score_mode = 0; // AIIdentifyScoreMode  置信度模式
    std::vector<int> image_source; // 图像输入来源信息
    std::vector<int> digital_effect; // 红外调色盘信息
    std::vector<int> filters;
    std::vector<AiModelLabel> labels;
    bool operator==(const SelectedAiModel& other) const {
        return
            index == other.index &&
            score == other.score &&
            score_mode == other.score_mode &&
            image_source == other.image_source &&
            digital_effect == other.digital_effect &&
            filters == other.filters &&
            labels == other.labels;
    }
};

/// 2.0 已选模型运行状态（置信度权威来源；优先于 selected_ai_model）
struct AiModelsStatusItem {
    int index = 0;
    int light_type = 0;      ///< 0 无效 / 1 广角 / 2 变焦 / 3 红外
    int score = 0;
    int score_mode = 0;
    std::vector<int> filters;
    bool operator==(const AiModelsStatusItem& other) const {
        return index == other.index
            && light_type == other.light_type
            && score == other.score
            && score_mode == other.score_mode
            && filters == other.filters;
    }
};

/// 2.0 模型基础能力信息
struct AiModelsInfoItem {
    int index = 0;
    std::vector<int> image_source;
    std::vector<int> digital_effect;
    std::vector<AiModelLabel> labels;
    bool operator==(const AiModelsInfoItem& other) const {
        return index == other.index
            && image_source == other.image_source
            && digital_effect == other.digital_effect
            && labels == other.labels;
    }
};

/// 单路码流绑定的模型列表（EA340 AI 2.0）
struct AiModelsConfigSet {
    int stream_type = 0;                     ///< 码流类型, 0: 融合流, 1: 广角, 2: 变焦, 3: 红外
    std::vector<int> models;                 ///< 模型索引列表, DJI官方: 0, 三方: other
    bool operator==(const AiModelsConfigSet& other) const {
        return stream_type == other.stream_type && models == other.models;
    }
};

struct AiModelsConfigParams {
    std::vector<AiModelsConfigSet> ai_models_config; ///< AI 模型配置列表
};

struct AiDataInfo {
    int identify_on = 0;    // AIIdentifyMode
    std::vector<AiModelItem> ai_model_list;
    SelectedAiModel selected_ai_model;  // 保留；协议 2.0 废弃，兼容旧业务
    std::vector<AiModelsConfigSet> ai_models_config;             ///< 2.0 各码流模型配置
    std::vector<AiModelsStatusItem> selected_ai_models_status;   ///< 2.0 已选模型状态（置信度权威）
    std::vector<AiModelsInfoItem> ai_models_infos;               ///< 2.0 全部模型基础信息
    bool operator==(const AiDataInfo& other) const {
        return
            identify_on == other.identify_on &&
            ai_model_list == other.ai_model_list &&
            selected_ai_model == other.selected_ai_model &&
            ai_models_config == other.ai_models_config &&
            selected_ai_models_status == other.selected_ai_models_status &&
            ai_models_infos == other.ai_models_infos;
    }
};

struct AiModelSelectParam {
    int index = 0;
    bool operator==(const AiModelSelectParam& other) const {
        return index == other.index;
    }
};

struct SetDistanceLimitResult {
    std::string message;
    bool operator==(const SetDistanceLimitResult& other) const {
        return message == other.message;
    }
};

struct SetHeightLimitResult {
    std::string message;
    bool operator==(const SetHeightLimitResult& other) const {
        return message == other.message;
    }
};

struct SetObstacleAvoidanceResult {
    std::string message;
    bool operator==(const SetObstacleAvoidanceResult& other) const {
        return message == other.message;
    }
};

struct SetRemainingPowerForRthResult {
    std::string message;
    bool operator==(const SetRemainingPowerForRthResult& other) const {
        return message == other.message;
    }
};

struct SetNightLightsStateResult {
    std::string message;
    bool operator==(const SetNightLightsStateResult& other) const {
        return message == other.message;
    }
};

struct SwitchAlarmStateResult {
    std::string message;
    bool operator==(const SwitchAlarmStateResult& other) const {
        return message == other.message;
    }
};

struct EsimOperatorSwitchResult {
    std::string message;
    std::string bid;
    bool operator==(const EsimOperatorSwitchResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct SimSlotSwitchResult {
    std::string message;
    std::string bid;
    bool operator==(const SimSlotSwitchResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct SetBatteryStoreModeResult {
    std::string message;
    std::string bid;
    bool operator==(const SetBatteryStoreModeResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct ManualBatterySwapResult {
    std::string message;
    std::string bid;
    bool operator==(const ManualBatterySwapResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct ManualBatterySwapProgressResult {
    std::string message;
    std::string bid;
    std::string data_json;
    bool operator==(const ManualBatterySwapProgressResult& other) const {
        return message == other.message && bid == other.bid && data_json == other.data_json;
    }
};

struct DonglePowerSwitchResult {
    std::string message;
    std::string bid;
    bool operator==(const DonglePowerSwitchResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct LowPowerSwitchResult {
    std::string message;
    std::string bid;
    bool operator==(const LowPowerSwitchResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct EsimOperatorSwitchProgressResult {
    std::string message;
    std::string bid;
    std::string data_json;
    bool operator==(const EsimOperatorSwitchProgressResult& other) const {
        return message == other.message && bid == other.bid && data_json == other.data_json;
    }
};

struct OpenDebugModeResult {
    std::string message;
    std::string bid;
    bool operator==(const OpenDebugModeResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct CloseDebugModeResult {
    std::string message;
    std::string bid;
    bool operator==(const CloseDebugModeResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct OpenDockCoverResult {
    std::string message;
    std::string bid;
    bool operator==(const OpenDockCoverResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct OpenDockCoverProgressResult {
    std::string message;
    std::string bid;
    double create_time;
    double update_time;
    bool operator==(const OpenDockCoverProgressResult& other) const {
        return
            message == other.message &&
            bid == other.bid &&
            floatEqual(create_time, other.create_time) &&
            floatEqual(update_time, other.update_time);
    }
};

struct CloseDockCoverResult {
    std::string message;
    std::string bid;
    bool operator==(const CloseDockCoverResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct CloseDockCoverProgressResult {
    std::string message;
    std::string bid;
    double create_time;
    double update_time;
    bool operator==(const CloseDockCoverProgressResult& other) const {
        return
            message == other.message &&
            bid == other.bid &&
            floatEqual(create_time, other.create_time) &&
            floatEqual(update_time, other.update_time);
    }
};

struct DeviceRebootResult {
    std::string message;
    std::string bid;
    bool operator==(const DeviceRebootResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct DeviceRebootProgressResult {
    std::string message;
    std::string bid;
    double create_time;
    double update_time;
    bool operator==(const DeviceRebootProgressResult& other) const {
        return
            message == other.message &&
            bid == other.bid &&
            floatEqual(create_time, other.create_time) &&
            floatEqual(update_time, other.update_time);
    }
};

struct DeviceFormatResult {
    std::string message;
    std::string bid;

    bool operator==(const DeviceFormatResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct DeviceFormatProgressResult {
    std::string message;
    std::string bid;
    double create_time;
    double update_time;
    bool operator==(const DeviceFormatProgressResult& other) const {
        return
            message == other.message &&
            bid == other.bid &&
            floatEqual(create_time, other.create_time) &&
            floatEqual(update_time, other.update_time);
    }
};

struct DroneFormatResult {
    std::string message;
    std::string bid;

    bool operator==(const DroneFormatResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct RtkCalibrationResult {
    std::string message;
    std::string bid;

    bool operator==(const RtkCalibrationResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct ManulRtkCalibrationParams {
    double longitude = 0.0;  // 经度
    double latitude = 0.0;   // 纬度
    double height = 0.0;     // 高度，单位：米
    bool operator==(const ManulRtkCalibrationParams& other) const {
        return
            floatEqual(longitude, other.longitude) &&
            floatEqual(latitude, other.latitude) &&
            floatEqual(height, other.height);
    }
};

struct NetworkRtkCalibrationParams {
    std::string host;        // 主机地址
    int port;                // 端口
    std::string account;     // 账号。需 Base64 编码
    std::string password;    // 密码。需 Base64 编码
    std::string mount_point; // 挂载点
    int version;             // 版本
    bool operator==(const NetworkRtkCalibrationParams& other) const {
        return
            host == other.host &&
            port == other.port &&
            account == other.account &&
            password == other.password &&
            mount_point == other.mount_point &&
            version == other.version;
    }
};

struct OpenDroneResult {
    std::string message;
    std::string bid;
    bool operator==(const OpenDroneResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct CloseDroneResult {
    std::string message;
    std::string bid;
    bool operator==(const CloseDroneResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct OpenChargeResult {
    std::string message;
    std::string bid;
    bool operator==(const OpenChargeResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

struct CloseChargeResult {
    std::string message;
    std::string bid;
    bool operator==(const CloseChargeResult& other) const {
        return message == other.message && bid == other.bid;
    }
};

// 
struct NetworkTestProgressResult {
    std::string message;
    std::string bid;
    double create_time = 0.0;
    double update_time = 0.0;
    double download_speed = 0.0;
    double upload_speed = 0.0;
    bool operator==(const NetworkTestProgressResult& other) const {
        return
            message == other.message &&
            bid == other.bid &&
            floatEqual(create_time, other.create_time) &&
            floatEqual(update_time, other.update_time) &&
            floatEqual(download_speed, other.download_speed) &&
            floatEqual(upload_speed, other.upload_speed);
    }
};

// DroneStatus Start
struct DroneStatusBatteryItemResult {
    int capacity_percent = 0;
    std::string firmware_version;
    int high_voltage_storage_days = 0;
    int index = 0;
    int loop_times = 0;
    std::string sn;
    int sub_type = 0;
    double temperature = 0.0;
    int type = 0;
    int voltage = 0;
};

struct DroneStatusBatteryResult {
    std::vector<DroneStatusBatteryItemResult> batteries;
    int capacity_percent = 0;
    int landing_power = 0;
    int remain_flight_time = 0;
    int remain_job_time = 0;
    int return_home_power = 0;
};

struct DroneStatusDistanceLimitStatusResult {
    int distance_limit = 0;
    int is_near_distance_limit = 0;
    int state = 0;
};

struct DroneStatusObstacleAvoidanceResult {
    int downside = 0;
    int horizon = 0;
    int upside = 0;
};

struct DroneStatusStorageResult {
    long long total = 0;
    long long used = 0;
};

struct DroneStatusPositionStateResult {
    int gps_number = 0;
    int is_fixed = 0;
    int quality = 0;
    int rtk_number = 0;
};

struct DroneStatusPropertiesResult {
    DroneStatusBatteryResult battery;
    DroneStatusDistanceLimitStatusResult distance_limit_status;
    int height_limit = 0;
    double home_distance = 0.0;
    double home_latitude = 0.0;
    double home_longitude = 0.0;
    int horizontal_speed = 0;
    int low_battery_warning_threshold = 0;
    int mode_code = 0;
    int mode_code_reason = 0;
    int night_lights_state = 0;
    DroneStatusObstacleAvoidanceResult obstacle_avoidance;
    DroneStatusPositionStateResult position_state;
    int remaining_power_for_return_home = 0;
    int serious_low_battery_warning_threshold = 0;
    DroneStatusStorageResult storage;
    double total_flight_distance = 0.0;
    int total_flight_sorties = 0;
    double total_flight_time = 0.0;
    int vertical_speed = 0;
};

struct DroneStatusDeviceModelResult {
    std::string device_class;
    std::string key;
    std::string name;
};

struct DroneStatusItemResult {
    std::string sn;
    bool online_status = false;
    long long updated_time = 0;
    DroneStatusDeviceModelResult device_model;
    DroneStatusPropertiesResult properties;
};

struct DroneDeviceStatusResultList {
    std::vector<DroneStatusItemResult> list;
};
// DroneStatus End

// DockStatus Start
struct DockStatusAirConditionerResult {
    int air_conditioner_state = 0;
    int switch_time = 0;
};

struct DockStatusAlternateLandPointResult {
    double height = 0.0;
    int is_configured = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double safe_land_height = 0.0;
};

struct DockStatusBackupBatteryResult {
    int switch_state = 0;
    double temperature = 0.0;
    int voltage = 0;
};

struct DockStatusNetworkStateResult {
    int quality = 0;
    int rate = 0;
    int type = 0;
};

struct DockStatusPositionStateResult {
    int gps_number = 0;
    int is_calibration = 0;
    int is_fixed = 0;
    int quality = 0;
    int rtk_number = 0;
};

struct DockStatusTiltAngleResult {
    int valid = 0;
    double value = 0.0;
};

struct DockStatusPropertiesResult {
    int acc_time = 0;
    double acdc_power_input = 0.0;
    DockStatusAirConditionerResult air_conditioner;
    DockStatusAlternateLandPointResult alternate_land_point;
    DockStatusBackupBatteryResult backup_battery;
    int electric_supply_voltage = 0;
    double environment_temperature = 0.0;
    int humidity = 0;
    int job_number = 0;
    DockStatusNetworkStateResult network_state;
    int poe_link_status = 0;
    double poe_power_output = 0;
    DockStatusPositionStateResult position_state;
    int rainfall = 0;
    double temperature = 0.0;
    DockStatusTiltAngleResult tilt_angle;
    int working_voltage = 0;
};

struct DockStatusDeviceModelResult {
    std::string device_class;
    std::string key;
    std::string name;
};

struct DockStatusItemResult {
    std::string sn;
    bool online_status = false;
    long long updated_time = 0;
    DockStatusDeviceModelResult device_model;
    DockStatusPropertiesResult properties;
};

struct DockDeviceStatusResultList {
    std::vector<DockStatusItemResult> list;
};
// DockStatus End

// 智能跟踪点信息(smart track 点信息);
// SmartTrackPointInfo;
class SmartTrackPointInfo {
public:
    double track_altitude = 0.0;       // 跟踪目标海拔高度; 单位：米;
    double track_latitude = 0.0;       // 目标纬度, [-90, 90]度;
    double track_longitude = 0.0;      // 目标经度, [-180, 180]度;
    int track_target_mode = 0;         // track 模式, {1: 正常跟踪, 2: 正常跟踪，可信度低, 3: 目标是GPS点，或目标丢失但有预测};

    bool operator==(const SmartTrackPointInfo &rhs) const {
        return
            floatEqual(track_altitude, rhs.track_altitude) &&
            floatEqual(track_latitude, rhs.track_latitude) &&
            floatEqual(track_longitude, rhs.track_longitude) &&
            track_target_mode == rhs.track_target_mode;
    }
};


// 飞机负载信息;
// AircraftPayloadsOsdInfo
// 注意: 当前版本后端暂不支持 topic_drone_payloads_osd_info 推送，该接口无法收到回调。
// 替代方案（模块化路径）:
//   - 云台姿态(gimbal_pitch/roll/yaw) → IDroneOsdModule::listenAircraftPayloadsGimbalAttitude
//   - 激光测距(measure_target_*) → ICameraControlModule::listenCameraMeasureTarget
//   - 变焦倍率(zoom_factor) → IDroneOsdModule::listenAircraftPayloadsCamerasList
//   - 其他字段(smart_track/thermal/control_source/firmware_version/sn) 当前无替代接口
class AircraftPayloadsOsdInfo {

public:
    std::string payload_index = "";                        ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"

    double gimbal_pitch = 0.0;                             // 云台俯仰轴角度, [-180, 180]度;
    double gimbal_roll = 0.0;                              // 云台横滚轴角度,[-180, 180]度;
    double gimbal_yaw = 0.0;                               // 云台朝向角度/云台偏航轴角度,[-180, 180]度;

    double measure_target_altitude = 0.0;                  // 激光测距目标海拔(单位: m);
    double measure_target_longitude = 0.0;                 // 激光测距目标经度,[-180,180]度;
    double measure_target_latitude = 0.0;                  // 激光测距目标纬度,[-90, 90]度;
    double measure_target_distance = 0.0;                  // 测距距离(单位: m);
    // todo: measure_target_error_state 原链路协议设计成 double 类型，最好改为 int 类型 (枚举值)
    double measure_target_error_state = 0;                 // 测距状态, 0: NORMAL, 1: TOO_CLOSE, 2: TOO_FAR, 3: NO_SIGNAL;

    std::vector<SmartTrackPointInfo> smart_track_points;   // smart track 点信息;

    double zoom_factor = 0.0;                              // 当前变焦倍率;

    int thermal_current_palette_style = 0;                 // 调色盘样式;
    int thermal_gain_mode = 0;                             // 增益模式;
    double thermal_global_temperature_min = 0.0;           // 全局画面中测量的最低温度(摄氏度);
    double thermal_global_temperature_max = 0.0;           // 全局画面中测量的最高温度(摄氏度);
    int thermal_isotherm_state = 0;                        // 是否开启等温线, {0: 关闭, 1: 开启};
    int thermal_isotherm_lower_limit = 0;                  // 测温区间下限(摄氏度), 仅启用等温线功能后有效;
    int thermal_isotherm_upper_limit = 0;                  // 测温区间上限(摄氏度), 仅启用等温线功能后有效;

    std::vector<int> thermal_supported_palette_styles;     // 设备支持的调色盘样式集合, 不同设备所支持的样式能力有所不同",

    std::string control_source ="";                        // 负载控制权;
    std::string firmware_version = "";                     // 负载固件版本;
    std::string sn = "";                                   // 负载sn;


    bool operator==(const AircraftPayloadsOsdInfo &rhs) const {
        return
            payload_index == rhs.payload_index &&
            floatEqual(gimbal_pitch, rhs.gimbal_pitch) &&
            floatEqual(gimbal_roll, rhs.gimbal_roll) &&
            floatEqual(gimbal_yaw, rhs.gimbal_yaw) &&
            floatEqual(measure_target_altitude, rhs.measure_target_altitude) &&
            floatEqual(measure_target_longitude, rhs.measure_target_longitude) &&
            floatEqual(measure_target_latitude, rhs.measure_target_latitude) &&
            floatEqual(measure_target_distance, rhs.measure_target_distance) &&
            floatEqual(measure_target_error_state, rhs.measure_target_error_state) &&
            smart_track_points == rhs.smart_track_points &&
            floatEqual(zoom_factor, rhs.zoom_factor) &&
            thermal_current_palette_style == rhs.thermal_current_palette_style &&
            thermal_gain_mode == rhs.thermal_gain_mode &&
            floatEqual(thermal_global_temperature_min, rhs.thermal_global_temperature_min) &&
            floatEqual(thermal_global_temperature_max, rhs.thermal_global_temperature_max) &&
            thermal_isotherm_state == rhs.thermal_isotherm_state &&
            thermal_isotherm_lower_limit == rhs.thermal_isotherm_lower_limit &&
            thermal_isotherm_upper_limit == rhs.thermal_isotherm_upper_limit &&
            thermal_supported_palette_styles == rhs.thermal_supported_palette_styles &&
            control_source == rhs.control_source &&
            firmware_version == rhs.firmware_version &&
            sn == rhs.sn;
    }
}; 


// 飞机负载信息列表;
// AircraftPayloadsOsdInfoList
class AircraftPayloadsOsdInfoList {
public:
    std::vector<AircraftPayloadsOsdInfo> payloads_info_list;

    bool operator==(const AircraftPayloadsOsdInfoList &rhs) const {
        return payloads_info_list == rhs.payloads_info_list;
    }
};


// 飞机: 档位信息(gear);
// AircraftGear;
class AircraftGear {
public:
    int gear = 0;   // 档位,{0: A, 1: P, 2: NAV, 3: FPV, 4: FARM, 5: S, 6: F, 7: M, 8: G, 9: T};

    bool operator==(const AircraftGear &rhs) const {
        return gear == rhs.gear;
    }
};


// 飞机: Home点距离(home_distance);
// AircraftHomeDistance;
class AircraftHomeDistance {
public:
    double home_distance = 0.0;   // Home点距离;

    bool operator==(const AircraftHomeDistance &rhs) const {
        return floatEqual(home_distance, rhs.home_distance);
    }
};


// 飞机: 搜星状态(position_state);
// AircraftPositionState;
class AircraftPositionState {
public:
    int is_fixed = 0;                // 是否收敛,{0: 非开始, 1: 收敛中, 2: 收敛成功, 3: 收敛失败};
    int quality = 0;                 // 搜星档位,{1: 1档, 2: 2档, 3: 3档, 4: 4档, 5: 5档};
    int gps_number = 0;              // gps搜星数量;
    int rtk_number = 0;              // rtk搜星数量;

    bool operator==(const AircraftPositionState &rhs) const {
        return
            is_fixed == rhs.is_fixed &&
            quality == rhs.quality &&
            gps_number == rhs.gps_number &&
            rtk_number == rhs.rtk_number;
    }
};


// 飞机: 当前控制源(control_source);
// AircraftControlSource;
class AircraftControlSource {
public:
    std::string control_source = "";    // 当前控制源;

    bool operator==(const AircraftControlSource &rhs) const {
        return control_source == rhs.control_source;
    }
};


// 飞机: 限制类信息状态;
// AircraftLimitsInfo;
class AircraftLimitsInfo {
public:
    int height_limit = 0;             // 相对起飞点的飞行限制高度, 单位(m);
    int is_near_height_limit = 0;     // 是否接近设定的限制高度, {0: 未达到设定的限制高度, 1: 接近设定的限制高度};
    int is_near_area_limit = 0;       // 是否接近限飞区, {0: 未达到限飞区, 1: 接近限飞区};
    int distance_limit_state = 0;     // 限远状态, {0: 未设置, 1: 有设置};
    int distance_limit = 0;           // 限远距离;
    int is_near_distance_limit = 0;   // 是否接近限远距离;

    bool operator==(const AircraftLimitsInfo &rhs) const {
        return
            height_limit == rhs.height_limit &&
            is_near_height_limit == rhs.is_near_height_limit &&
            is_near_area_limit == rhs.is_near_area_limit &&
            distance_limit_state == rhs.distance_limit_state &&
            distance_limit == rhs.distance_limit &&
            is_near_distance_limit == rhs.is_near_distance_limit;
    }
};


// 电量告警信息(用户设置的阈值);
// AircraftBatteryWarningThresholds;
class AircraftBatteryWarningThresholds {
public:
    int low_battery_warning_threshold = 0;            // int, 用户设置的电池低电量告警百分比, [0, 100];
    int serious_low_battery_warning_threshold = 0;    // int, 用户设置的电池严重低电量告警百分比, [0,100];

    bool operator==(const AircraftBatteryWarningThresholds &rhs) const {
        return
            low_battery_warning_threshold == rhs.low_battery_warning_threshold &&
            serious_low_battery_warning_threshold == rhs.serious_low_battery_warning_threshold;
    }
};


// 固件状态信息;
// AircraftFirmwareStatus;
class AircraftFirmwareStatus {
public:
    std::string firmware_version = "";     // 固件版本, such as "00.01.0439";
    int compatible_status = 0;             // 固件一致性, {"0: 一致, 1: 不一致"};
    int firmware_upgrade_status = 0;       // 固件升级状态, {"0: 未升级, 1: 升级中"};

    bool operator==(const AircraftFirmwareStatus &rhs) const {
        return
            firmware_version == rhs.firmware_version &&
            compatible_status == rhs.compatible_status &&
            firmware_upgrade_status == rhs.firmware_upgrade_status;
    }
};


// 累计飞行信息(total_flight_info);
// AircraftTotalFlightInfo;
class AircraftTotalFlightInfo {
public:
    double total_flight_time = 0.0;       // double, 总飞行时长(飞行器累计飞行航时);
    double total_flight_distance = 0.0;   // double, 总飞行距离;
    int total_flight_sorties = 0;         // int, 总飞行架次;

    // fixed: 使用 floatEqual 进行浮点型比较;
    bool operator==(const AircraftTotalFlightInfo &rhs) const {
        return
            floatEqual(total_flight_time, rhs.total_flight_time) &&
            floatEqual(total_flight_distance, rhs.total_flight_distance) &&
            total_flight_sorties == rhs.total_flight_sorties;
    }
};


// 飞行器激活时间(unix时间戳)(activation_time);
// AircraftActivationTime;
class AircraftActivationTime {
public:
    // !TODO: c++ 跨平台代码，最好不要用 long 类型，这里保持与历史代码一致。
    // !TODO: use int or long long(After fixing AIGCJson);
    long activation_time = 0;       // 飞行器激活时间(unix时间戳);

    bool operator==(const AircraftActivationTime &rhs) const {
        return activation_time == rhs.activation_time;
    }
};


// 飞机：保养信息(maintain_status);
// 飞机保养信息;
class AircraftMaintainStatus {
public:
    int state = 0;                           // 保养状态(0: 未保养, 1: 保养中);
    int last_maintain_type = 0;              // 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养);
    long last_maintain_time = 0;             // 上一次保养时间, utc 秒时间戳(单位: 秒); // TODO: long long 类型 AIGC 解析出错;
    int last_maintain_flight_time = 0;       // 上一次保养时飞行航时(单位: h);
    int last_maintain_flight_sorties = 0;    // 上一次保养时飞行架次;


    bool operator==(const AircraftMaintainStatus &rhs) const {
        return
            (state == rhs.state) &&
            (last_maintain_type == rhs.last_maintain_type) &&
            (last_maintain_time == rhs.last_maintain_time) &&
            (last_maintain_flight_time == rhs.last_maintain_flight_time) &&
            (last_maintain_flight_sorties == rhs.last_maintain_flight_sorties);
    }
};


// 飞机保养信息列表;
// 飞机：保养信息(maintain_status);
// AircraftMaintainStatusInfo;
class AircraftMaintainStatusInfo {
public:
    std::vector<AircraftMaintainStatus> maintain_status_info;

    bool operator==(const AircraftMaintainStatusInfo &rhs) const {
        return maintain_status_info == rhs.maintain_status_info;
    }
};


// 飞行器夜航灯状态(night_lights_state);
// AircraftNightLightsState;
class AircraftNightLightsState {
public:
    int night_lights_state = 0;    // 飞行器夜航灯状态, {0:关闭, 1:打开};

    bool operator==(const AircraftNightLightsState &rhs) const {
        return night_lights_state == rhs.night_lights_state;
    }
};


// 飞行器避障状态(obstacle_avoidance);
// AircraftObstacleAvoidanceState;
class AircraftObstacleAvoidanceState {
public:
    int horizon = 0;  // int, 水平避障状态, {0:关闭, 1:开启};
    int upside = 0;   // int, 上视避障状态, {0:关闭, 1:开启};
    int downside = 0; // int, 下视避障状态, {0:关闭, 1:开启};

    bool operator==(const AircraftObstacleAvoidanceState &rhs) const {
        return
            horizon == rhs.horizon &&
            upside == rhs.upside &&
            downside == rhs.downside;
    }
};


// 飞机存储容量(storage);
// AircraftStorageInfo;
// !todo: 从物模型的协议设计来看使用了 int 类型, 容量最多 2.x TB, 暂时使用 int 类型;
class AircraftStorageInfo {
public:
    int total = 0;   // 总容量;
    int used = 0;    // 已使用;

    bool operator==(const AircraftStorageInfo &rhs) const {
        return (total == rhs.total) && (used == rhs.used);
    }
};


// 航迹id(track_id);
// AircraftTrackId;
class AircraftTrackId {
public:
    std::string track_id = "";    // string, 航迹id, such as "da247702-9636-4404-b98c-7e667efb4c4b";

    bool operator==(const AircraftTrackId &rhs) const {
        return track_id == rhs.track_id;
    }
};


// 合规相关信息(compliance information);
// AircraftComplianceInfo;
class AircraftComplianceInfo {
public:
    int rid_state = 0;           // int, RID(Remote Identification)工作状态", {1: 正常, 0: 异常};
    std::string country = "";    // string, 国家区域码;

    bool operator==(const AircraftComplianceInfo &rhs) const {
        return (rid_state == rhs.rid_state) && (country == rhs.country);
    }
};


/*
 * 飞机相机相关的数据;
 */

// 红外测温点相关信息;
// IrMeteringPoint;
class CameraIrMeteringPoint {
public:
    double temperature = 0.0;    // double, 温度;
    double x = 0.0;              // double, x坐标(0.0~1.0);
    double y = 0.0;              // double, y坐标(0.0~1.0);

    bool operator==(const CameraIrMeteringPoint &rhs) const {
        return
            floatEqual(temperature, rhs.temperature) &&
            floatEqual(x, rhs.x) &&
            floatEqual(y, rhs.y);
    }
};

/// 红外测温区域信息（DRC `ir_thermometry.ir_metering_area`）。
struct CameraIrMeteringArea {
    double aver_temperature = 0.0;   ///< 区域平均温度（°C）
    double height = 0.0;             ///< 区域高度（归一化 0.0~1.0）
    double width = 0.0;              ///< 区域宽度（归一化 0.0~1.0）
    double x = 0.0;                  ///< 区域左上角 x（归一化 0.0~1.0）
    double y = 0.0;                  ///< 区域左上角 y（归一化 0.0~1.0）
    CameraIrMeteringPoint max_temperature_point; ///< 区域内最高温点
    CameraIrMeteringPoint min_temperature_point; ///< 区域内最低温点

    bool operator==(const CameraIrMeteringArea& other) const {
        return
            floatEqual(aver_temperature, other.aver_temperature) &&
            floatEqual(height, other.height) &&
            floatEqual(width, other.width) &&
            floatEqual(x, other.x) &&
            floatEqual(y, other.y) &&
            max_temperature_point == other.max_temperature_point &&
            min_temperature_point == other.min_temperature_point;
    }
};

/// 红外测温上行推送（DRC `drc_camera_osd_info_push.ir_thermometry` 全量透传）。
/// 与已有 `setIrMeteringMode` / `setIrMeteringPoint` / `setIrMeteringArea` 成对。
struct CameraIrThermometryInfo {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int ir_metering_mode = 0;                     ///< 测温模式（点/区域/关）
    CameraIrMeteringPoint ir_metering_point;      ///< 点测温结果
    CameraIrMeteringArea ir_metering_area;        ///< 区域测温结果（含区域内最高/最低温点）

    bool operator==(const CameraIrThermometryInfo& other) const {
        return
            payload_index == other.payload_index &&
            ir_metering_mode == other.ir_metering_mode &&
            ir_metering_point == other.ir_metering_point &&
            ir_metering_area == other.ir_metering_area;
    }
};

// Liveview 世界区域信息;
// CameraLiveviewWorldRegion;
class CameraLiveviewWorldRegion {
public:
    double bottom = 0.0;
    double left = 0.0;
    double right = 0.0;
    double top = 0.0;

    bool operator==(const CameraLiveviewWorldRegion &rhs) const {
        return
            floatEqual(bottom, rhs.bottom) &&
            floatEqual(left, rhs.left) &&
            floatEqual(right, rhs.right) &&
            floatEqual(top, rhs.top);
    }
};


// 飞机相机负载信息;
// AircraftPayloadCameraInfo;
class AircraftPayloadCameraInfo {
public:
    std::string payload_index = "";    // "payload_index": "81-0-0";
    int camera_mode = 0;               // 相机模式, {0: 拍照, 1: 录像, 2: 低光智能, 3: 全景拍照, -1: 不支持的模式};
    int ir_metering_mode = 0;
    CameraIrMeteringPoint ir_metering_point;
    double ir_zoom_factor = 0;
    CameraLiveviewWorldRegion liveview_world_region;
    int photo_state = 0;
    std::vector<std::string> photo_storage_settings;
    int record_time = 0;         // 视频录制时长, 单位：秒(s);
    int recording_state = 0;
    int remain_photo_num = 0;    // 剩余拍照张数;
    int remain_record_duration = 0;
    bool screen_split_enable = false;

    int wide_calibrate_farthest_focus_value = 0;
    int wide_calibrate_nearest_focus_value = 0;
    int wide_exposure_mode = 0;
    int wide_exposure_value = 0;
    int wide_focus_mode = 0;
    int wide_focus_state = 0;
    int wide_focus_value = 0;
    int wide_iso = 0;
    int wide_max_focus_value = 0;
    int wide_min_focus_value = 0;
    int wide_shutter_speed = 0;
    
    int zoom_calibrate_farthest_focus_value = 0;
    int zoom_calibrate_nearest_focus_value = 0;
    int zoom_exposure_mode = 0;
    int zoom_exposure_value = 0;
    double zoom_factor = 0;
    int zoom_focus_mode = 0;
    int zoom_focus_state = 0;
    int zoom_focus_value = 0;
    int zoom_iso = 0;
    int zoom_max_focus_value = 0;
    int zoom_min_focus_value = 0;
    int zoom_shutter_speed = 0;


    bool operator==(const AircraftPayloadCameraInfo &rhs) const {
        return
            (camera_mode == rhs.camera_mode) &&
            (ir_metering_mode == rhs.ir_metering_mode) &&
            (ir_metering_point == rhs.ir_metering_point) &&
            floatEqual(ir_zoom_factor, rhs.ir_zoom_factor) &&
            (liveview_world_region == rhs.liveview_world_region) &&
            (payload_index == rhs.payload_index) &&
            (photo_state == rhs.photo_state) &&
            (photo_storage_settings == rhs.photo_storage_settings) &&
            (record_time == rhs.record_time) &&
            (recording_state == rhs.recording_state) &&
            (remain_photo_num == rhs.remain_photo_num) &&
            (remain_record_duration == rhs.remain_record_duration) &&
            (screen_split_enable == rhs.screen_split_enable) &&
            (wide_calibrate_farthest_focus_value == rhs.wide_calibrate_farthest_focus_value) &&
            (wide_calibrate_nearest_focus_value == rhs.wide_calibrate_nearest_focus_value) &&
            (wide_exposure_mode == rhs.wide_exposure_mode) &&
            (wide_exposure_value == rhs.wide_exposure_value) &&
            (wide_focus_mode == rhs.wide_focus_mode) &&
            (wide_focus_state == rhs.wide_focus_state) &&
            (wide_focus_value == rhs.wide_focus_value) &&
            (wide_iso == rhs.wide_iso) &&
            (wide_max_focus_value == rhs.wide_max_focus_value) &&
            (wide_min_focus_value == rhs.wide_min_focus_value) &&
            (wide_shutter_speed == rhs.wide_shutter_speed) &&
            (zoom_calibrate_farthest_focus_value == rhs.zoom_calibrate_farthest_focus_value) &&
            (zoom_calibrate_nearest_focus_value == rhs.zoom_calibrate_nearest_focus_value) &&
            (zoom_exposure_mode == rhs.zoom_exposure_mode) &&
            (zoom_exposure_value == rhs.zoom_exposure_value) &&
            floatEqual(zoom_factor, rhs.zoom_factor) &&
            (zoom_focus_mode == rhs.zoom_focus_mode) &&
            (zoom_focus_state == rhs.zoom_focus_state) &&
            (zoom_focus_value == rhs.zoom_focus_value) &&
            (zoom_iso == rhs.zoom_iso) &&
            (zoom_max_focus_value == rhs.zoom_max_focus_value) &&
            (zoom_min_focus_value == rhs.zoom_min_focus_value) &&
            (zoom_shutter_speed == rhs.zoom_shutter_speed);
    }
};


// 飞机相机负载信息列表;
// AircraftPayloadsCamerasList;
class AircraftPayloadsCamerasList {
public:
    std::vector<AircraftPayloadCameraInfo> cameras_list;

    bool operator==(const AircraftPayloadsCamerasList &rhs) const {
        return cameras_list == rhs.cameras_list;
    }
};


// 遥控器失控动作(rc_lost_action);
// AircraftRcLostAction;
class AircraftRcLostAction {
public:
    int rc_lost_action = 0;   // int, {0: 悬停, 1: 着陆(降落), 2: 返航};

    bool operator==(const AircraftRcLostAction &rhs) const {
        return rc_lost_action == rhs.rc_lost_action;
    }
};


// 返航信息(返航模式, 返航高度);
// AircraftRthInfo;
class AircraftRthInfo {
public:
    int rth_mode = 0;         // int, 返航高度模式设置值, {0: "智能高度", 1: "设定高度"};
    int rth_altitude = 0;     // int, 返航高度(单位: m), 参考值: [20, 1500];

    bool operator==(const AircraftRthInfo &rhs) const {
        return (rth_mode == rhs.rth_mode) && (rth_altitude == rhs.rth_altitude);
    }
};


// 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
// AircraftHomeGeographicalInfo;
class AircraftHomeGeographicalInfo {
public:
    double home_longitude = 0;       // double, Home点经度, [-180, 180] 度;
    double home_latitude = 0;        // double, Home点纬度, [-90, 90] 度;


    // fixed: 使用 floatEqual 进行浮点型比较;
    bool operator==(const AircraftHomeGeographicalInfo &rhs) const {
        return
            floatEqual(home_longitude, rhs.home_longitude) &&
            floatEqual(home_latitude, rhs.home_latitude);
    }
};



// 机场固件信息;
// DockFirmwareInfo
class DockFirmwareInfo {
public:
    std::string firmware_version = "";     // 固件版本, such as "00.01.0439";
    int compatible_status = 0;             // 固件一致性, { 0: 一致, 1: 不一致 };
    int firmware_upgrade_status = 0;       // 固件升级状态, { 0: 未升级, 1: 升级中 };

    bool operator==(const DockFirmwareInfo &rhs) const {
        return
            firmware_version == rhs.firmware_version &&
            compatible_status == rhs.compatible_status &&
            firmware_upgrade_status == rhs.firmware_upgrade_status;
    }
};


// 返航高度模式当前值(current_rth_mode);
// AircraftCurrentRthMode;
class AircraftCurrentRthMode {
public:
    int current_rth_mode = 0;    // int, 返航高度模式, { 0: 智能高度, 1: 设定高度 };

    bool operator==(const AircraftCurrentRthMode &rhs) const {
        return current_rth_mode == rhs.current_rth_mode;
    }
};


// HMS: args 参数信息;
class HmsArgs {
public:
    int component_index = -1;        // int, 文案变量, 默认无效值(-1);
    int sensor_index = -1;           // int, 文案变量, 默认无效值(-1);
    std::string device_id;           // string, 告警产生的设备ID, 无效值: 空值;
    std::string sender_host_id;      // string, 告警产生的模块ID, 无效值: 空值;
    int ignore = 0;                  // int, 告警是否忽略, {0:不忽略, 1: 忽略};
    int alarm_info = 0;              // int, 模块自定义的告警信息;
    long create_timestamp = 0;       // long, 故障产生的UTC时间(单位秒); // TODO: long long 类型 AIGC 解析出错;
    int display_scene = 0;           // int, 0: 正式错误码, 1: 调试错误码, 只用于排障;

    bool operator==(const HmsArgs &rhs) const {
        return
            component_index == rhs.component_index &&
            sensor_index == rhs.sensor_index &&
            device_id == rhs.device_id &&
            sender_host_id == rhs.sender_host_id &&
            ignore == rhs.ignore &&
            alarm_info == rhs.alarm_info &&
            create_timestamp == rhs.create_timestamp &&
            display_scene == rhs.display_scene;
    }
};


// HMS: 基础告警信息;
class HmsDataBasics {
public:
    int level = 0;            // int, 告警等级, {"0":"通知","1":"提醒","2":"警告"};
    int module_id = -1;       // int, 事件模块, {"0":"飞行任务","1":"设备管理","2":"媒体","3":"hms"};
    std::string code;         // string, 告警码, {无效值: 空值};
    std::string device_type;  // string, 设备类型, 上云API格式:{domain-type-subtype},可以根据产品支持查询, {无效值: 空值};
    std::string domain_type;  // string, such as "drone_nest";
    int in_the_sky = 0;       // int, 是否飞行, {"0":"在地上", "1":"在天上"};
    int imminent = 0;         // int, 是否为及时性的, {"0":"否", "1":"是"};
    HmsArgs args;             // HmsArgs, 告警参数信息;
    std::string status_key;   // string;
    std::string joint_key;    // string;
    int id = 0;               // int, 每个设备上报事件里hms的唯一id;
    int parent_id = 0;        // int, 每个设备上报事件里hms的唯一父id;

    bool operator==(const HmsDataBasics &rhs) const {
        return
            level == rhs.level &&
            module_id == rhs.module_id &&
            code == rhs.code &&
            device_type == rhs.device_type &&
            domain_type == rhs.domain_type &&
            in_the_sky == rhs.in_the_sky &&
            imminent == rhs.imminent &&
            args == rhs.args &&
            status_key == rhs.status_key &&
            joint_key == rhs.joint_key &&
            id == rhs.id &&
            parent_id == rhs.parent_id;
    }
};

// HMS: 告警信息 Item(单条信息);
class HmsDataItem {
public:
    HmsDataBasics basics;
    std::vector<HmsDataBasics> sub_hms_data;

    bool operator==(const HmsDataItem &rhs) const {
        return (basics == rhs.basics) && (sub_hms_data == rhs.sub_hms_data);
    }
};

// HMS 告警信息列表;
class HmsData {
public:
    std::vector<HmsDataItem> hms_data_items;

    bool operator==(const HmsData &rhs) const {
        return hms_data_items == rhs.hms_data_items;
    }
};

/// 设备日志项
struct DeviceLogListResultLogItem {
    int boot_index = 0;        ///< 启动索引
    long long start_time = 0;  ///< 日志开始时间
    long long end_time = 0;    ///< 日志结束时间
    long long size = 0;        ///< 日志大小，单位：字节
    std::string md5;           ///< 文件 MD5
};

/// 设备日志文件项
struct DeviceLogListResultFileItem {
    std::string device_sn;     ///< 设备 SN
    std::string module;        ///< 模块名称
    int result = 0;            ///< 查询结果码
    std::vector<DeviceLogListResultLogItem> list; ///< 日志项列表
    std::string ObjectKey;     ///< 对象存储 key
    int log_type = 0;          ///< 日志类型
    int responsibility_log_type = 0; ///< 责任日志类型
};

/// 设备日志文件列表查询结果
struct DeviceLogListResult {
    std::vector<DeviceLogListResultFileItem> files; ///< files
};

/// 日志报告 — 日志项参数
struct DeviceLogReportLogItemParam {
    int boot_index = 0;        ///< 启动索引
    long long start_time = 0;  ///< 日志开始时间
    long long end_time = 0;    ///< 日志结束时间
    long long size = 0;        ///< 日志大小，单位：字节
    std::string md5;           ///< 文件 MD5
};

/// 日志报告 — 日志列表项参数
struct DeviceLogReportLogListItemParam {
    std::string device_sn;     ///< 设备 SN
    std::vector<DeviceLogReportLogItemParam> list; ///< 日志项列表
    std::string module;        ///< 模块名称
};

/// 日志报告 — 领域项参数
struct DeviceLogReportDomainItemParam {
    long long key = 0;         ///< 领域 key
    std::string value;         ///< 领域值
};

/// 日志报告 — 联系方式参数
struct DeviceLogReportContactInformationParam {
    std::string email;         ///< 邮箱
    std::string phone;         ///< 电话
};

/// 日志报告 — 设备拓扑项参数
struct DeviceLogReportDeviceTopoItemParam {
    std::string device_model_key; ///< 设备型号 key
    std::string sn;            ///< 设备 SN
};

/// 日志报告 — 设备拓扑参数
struct DeviceLogReportDeviceTopoParam {
    std::vector<DeviceLogReportDeviceTopoItemParam> parents; ///< 父设备列表
    std::vector<DeviceLogReportDeviceTopoItemParam> hosts;   ///< 主机设备列表
};

/// 创建设备日志报告请求参数
struct CreateDeviceLogReportRequestParam {
    std::string report_information;  ///< 报告描述信息
    long long error_start_time = 0;  ///< 错误发生起始时间，Unix 时间戳（秒）
    DeviceLogReportDeviceTopoParam device_topo; ///< 设备拓扑信息
    DeviceLogReportContactInformationParam report_contact_information; ///< 上报联系信息
    std::vector<std::string> appendix_files; ///< 附件文件列表
    std::vector<DeviceLogReportLogListItemParam> log_list; ///< 日志列表
    std::vector<DeviceLogReportDomainItemParam> domains; ///< 领域列表
    bool is_crashed = false;         ///< 是否崩溃
};

/// 设备任务进度
struct CreateDeviceJobProgressResult {
    int percent = 0;           ///< 进度百分比
    int current_step = 0;      ///< 当前步骤
    std::string ext;           ///< 扩展信息
};

/// 设备任务数据
struct CreateDeviceJobDataResult {
    std::string bid;           ///< 业务标识(bid)
    std::string sn;            ///< 设备 SN
    std::string biz_code;      ///< 业务编码
    std::string status;        ///< 任务状态
    int result = 0;            ///< 结果码
    CreateDeviceJobProgressResult progress; ///< 进度信息
    std::string service_identifier; ///< 服务标识
};

/// 创建设备日志报告结果
struct CreateDeviceLogReportResult {
    std::string track_number;  ///< 追踪编号
    std::string report_uuid;   ///< 报告 UUID
    std::string error_log_list; ///< 错误日志列表
    CreateDeviceJobDataResult creat_device_job_data; ///< 设备任务数据
};

/// 日志报告列表分页信息
struct LogReportListResultPagination {
    int page = 0;              ///< 当前页码
    int page_size = 0;         ///< 每页数量
    int total = 0;             ///< 总记录数
};

/// 日志报告附件文件项
struct LogReportListResultAppendixFileItem {
    std::string file_name;     ///< 文件名
    std::string file_object_key; ///< 文件对象存储 Key
    long long file_size = 0;   ///< 文件大小，单位：字节
};

/// 日志报告附件文件列表
struct LogReportListResultAppendixFiles {
    std::vector<LogReportListResultAppendixFileItem> files; ///< files
};

/// 日志报告设备拓扑项
struct LogReportListResultDeviceTopoItem {
    std::string sn;            ///< 设备 SN
    std::string device_model_key; ///< 设备型号 key
    std::string device_organization_callsign; ///< 设备组织呼号
};

/// 日志报告设备拓扑
struct LogReportListResultDeviceTopo {
    std::vector<LogReportListResultDeviceTopoItem> hosts;   ///< 主机设备列表
    std::vector<LogReportListResultDeviceTopoItem> parents; ///< 父设备列表
};

/// 日志报告联系方式
struct LogReportListResultContactInformation {
    std::string email;         ///< 邮箱
    std::string phone;         ///< 电话
};

/// 日志报告设备日志分片
struct LogReportListResultDeviceLogItemList {
    int boot_index = 0;            ///< 启动序号
    long long start_time = 0;      ///< 分片开始时间（Unix 秒）
    long long end_time = 0;        ///< 分片结束时间（Unix 秒）
    long long size = 0;            ///< 分片大小（字节）
};

/// 日志报告设备日志文件
struct LogReportListResultDeviceLog {
    std::string device_sn;         ///< 设备序列号
    std::string module;            ///< 模块编号
    int result = 0;                ///< 结果
    std::vector<LogReportListResultDeviceLogItemList> list; ///< 日志分片列表
    std::string object_key;        ///< 对象存储 Key
};

/// 日志报告设备日志
struct LogReportListResultDeviceLogs {
    std::vector<LogReportListResultDeviceLog> files; ///< 设备日志文件列表
};

/// 日志报告设备上传文件进度
struct LogReportListResultDeviceUploadFilesItemProgress {
    int current_step = 0;          ///< 当前步骤
    int total_step = 0;            ///< 总步骤数
    int progress = 0;              ///< 上传进度（0-100）
    long long finish_time = 0;     ///< 完成时间（Unix 秒）
    int upload_rate = 0;           ///< 上传速率
    int result = 0;                ///< 结果
    std::string status;            ///< 状态
};

/// 日志报告设备上传文件项
struct LogReportListResultDeviceUploadFilesItem {
    std::string module;            ///< 模块编号
    long long size = 0;            ///< 文件大小（字节）
    std::string device_sn;         ///< 设备序列号
    std::string key;               ///< 文件 Key
    std::string fingerprint;       ///< 文件指纹
    LogReportListResultDeviceUploadFilesItemProgress progress; ///< 上传进度
    std::string download_url;      ///< 下载 URL
};

/// 日志报告设备上传文件
struct LogReportListResultDeviceUploadFiles {
    std::vector<LogReportListResultDeviceUploadFilesItem> files; ///< 上传文件列表
};

/// 日志报告记录项
struct LogReportListResultItem {
    std::string report_uuid;   ///< 报告 UUID
    long long report_time = 0; ///< 报告时间
    std::string report_user_id; ///< 报告用户 ID
    std::string report_user_organization_callsign; ///< 上报用户所属组织呼号
    LogReportListResultDeviceTopo device_topo; ///< 设备拓扑信息
    std::string report_information; ///< 报告描述信息
    std::string track_number;  ///< 追踪编号
    long long error_start_time = 0; ///< 错误发生起始时间
    LogReportListResultAppendixFiles appendix_files; ///< 附件文件信息
    LogReportListResultDeviceLogs device_logs; ///< 设备日志文件列表
    LogReportListResultDeviceUploadFiles device_upload_files; ///< 设备上传文件列表
    std::string status;        ///< 报告状态
    LogReportListResultContactInformation report_contact_information; ///< 上报联系信息
    std::string bid;           ///< 业务标识(bid)
    std::string device_report_feedback; ///< 设备报告反馈
    int read_status = 0;       ///< 阅读状态
    std::string url;           ///< 报告 URL
};

/// 日志报告列表查询结果
struct LogReportListResult {
    LogReportListResultPagination pagination; ///< 分页信息
    std::vector<LogReportListResultItem> list; ///< 日志报告列表
};

/// 历史 HMS 告警查询参数
struct HistoryHmsDataParam {
    std::vector<std::string> sn_list; ///< 设备 SN 列表
    long long create_time_begin = 0;  ///< 查询起始时间（毫秒级时间戳）
    long long create_time_end = 0;    ///< 查询结束时间（毫秒级时间戳）
    int page_size = 0;                ///< 每页数量
    int page_num = 0;                 ///< 页码
    // TODO(apifox): level (告警级别。枚举：0=通知, 1=提醒, 2=警告)
    // TODO(apifox): sort_column (排序字段)
    // TODO(apifox): sort_type (排序方向：asc/desc)
    // TODO(apifox): message (告警消息筛选)
    // TODO(apifox): language (语言)
    // TODO(apifox): sub_hms_list (子 HMS 列表)
};

/// 历史 HMS 查询结果分页信息
struct HistoryHmsDataResultPagination {
    int page_num = 0;     ///< 当前页码
    int page_size = 0;    ///< 每页数量
    int total = 0;        ///< 总记录数
};

/// HMS 告警参数详情
struct HistoryHmsDataResultArgs {
    int alarm_info = 0;          ///< 告警信息
    int component_index = 0;     ///< 组件索引
    long long create_timestamp = 0; ///< 创建时间戳
    std::string device_id;       ///< 设备 ID
    int display_scene = 0;       ///< 显示场景
    int ignore = 0;              ///< 是否忽略
    std::string sender_host_id;  ///< 发送方主机 ID
    int sensor_index = 0;        ///< 传感器索引
};

/// HMS 告警记录项
struct HistoryHmsDataResultItem {
    std::string _id;             ///< 记录 ID
    std::string bid;             ///< 业务标识(bid)
    std::string tid;             ///< 事务 ID
    int level = 0;               ///< 告警级别。枚举：0=通知, 1=提醒, 2=警告
    int module = 0;              ///< 模块标识
    std::string message;         ///< 告警消息
    int status = 0;              ///< 事件状态。枚举：1=live, 2=非 live
    std::string code;            ///< 告警码
    long long device_data_create_time = 0; ///< 设备数据创建时间
    long long device_data_update_time = 0; ///< 设备数据更新时间
    long long create_time = 0;   ///< 记录创建时间
    long long update_time = 0;   ///< 记录更新时间
    HistoryHmsDataResultArgs args; ///< 告警参数详情
    std::string device_type;     ///< 设备类型
    std::string domain_type;     ///< 领域类型
    int imminent = 0;            ///< 是否紧急
    int in_the_sky = 0;          ///< 是否在飞行中
    std::string joint_key;       ///< 联合键
    std::string status_key;      ///< 状态键
    std::string sn;              ///< 设备 SN
};

/// 历史 HMS 告警查询结果
struct HistoryHmsDataResult {
    HistoryHmsDataResultPagination pagination; ///< 分页信息
    std::vector<HistoryHmsDataResultItem> list; ///< HMS 数据项列表
};

struct ModelInfo {
    std::string key;
    std::string domain;
    std::string type;
    std::string sub_type;
    std::string name;
    std::string device_class;
    std::string develop_code;
};


// Concise‌ModelInfo 对应以下协议数据;
// "model_info": {
//     "model_key": "3-3-0",
//     "name": "DJI Dock 3",
//     "class": "airport"
// },

struct ConciseModelInfo {
    std::string model_key;
    std::string name;
    std::string device_class;

    ConciseModelInfo() = default;
    ConciseModelInfo(const std::string &the_model_key, const std::string &the_name, const std::string &the_device_class)
      : model_key(the_model_key), name(the_name), device_class(the_device_class) { }
};


struct SubDeviceInfo {
    std::string device_sn;
    ModelInfo model_info;
    bool device_online_status;
    std::string device_firmware_version;
    int device_firmware_status;
    int device_working_status;
    std::string flysafe_database_version;
    std::vector<std::string> parent_sns;
    std::string nickname;   // 设备昵称(后端 device_callsign 字段, 与 getDeviceList 的 nickname 同源)
};

struct DeviceDetailInfo {
    std::string device_sn;
    std::string nickname;   // 设备昵称(后端 device_callsign 字段, 与 getDeviceList 的 nickname 同源)
    ModelInfo model_info;
    int device_add_time;
    bool device_online_status;
    std::string device_firmware_version;
    int device_firmware_status;
    int device_working_status;
    std::string complete_machine_sn;
    std::vector<SubDeviceInfo> sub_devices;
};

struct ModifyDeviceNameItem {
    std::string device_sn;
    std::string device_callsign;
};

struct ModifyDeviceNameParam {
    std::vector<ModifyDeviceNameItem> device_list;
};

struct FirmwareReleaseNoteResult {
    std::string device_model_key;
    std::string lang;
    std::string product_version;
    std::string release_note;
    std::string released_time;
};

struct FirmwareReleaseNoteListResult {
    std::vector<FirmwareReleaseNoteResult> list;
};

struct FirmwareUpgradeDeviceItem {
    std::string sn;
    std::string device_model_key;
    int firmware_upgrade_type;     // 固件升级类型
    std::string product_version;
};

struct FirmwareUpgradeParam {
    std::string org_uuid;
    std::vector<FirmwareUpgradeDeviceItem> devices;
};

struct FirmwareUpgradeResponseDeviceItemResult {
    int firmware_upgrade_type = 0;
    std::string product_version;
    std::string sn;
};

struct FirmwareUpgradeResult {
    int device_code = 0;
    std::vector<FirmwareUpgradeResponseDeviceItemResult> devices;
    std::string status;
};

struct FirmwareUpgradeProgressStep {
    int current_step;
    int percent;
};

struct FirmwareUpgradeProgressOTA {
    FirmwareUpgradeProgressStep progress;
    std::string status;
};

struct FirmwareUpgradeAssociateDrone {
    int firmware_status;
    std::string sn;
    int working_status;
};

struct FirmwareUpgradeAssociateRelay {
    int firmware_status;
    std::string sn;
};

struct FirmwareUpgradeProgressItem {
    FirmwareUpgradeAssociateDrone associate_drone;
    FirmwareUpgradeAssociateRelay associate_relay;
    int device_code;
    int firmware_status;
    FirmwareUpgradeProgressOTA ota;
    std::string sn;
    int working_status;
};

struct FirmwareUpgradeProgressResult {
    std::vector<FirmwareUpgradeProgressItem> list;
};

struct FirmwareItemResult {
    int id;
    std::string created_at;
    std::string device_model_key;
    std::string status;
    std::string version;
    std::string desc;
};

struct FirmwareListResult {
    std::vector<FirmwareItemResult> list;
};


struct FirmwareCompatibilityRulesResult {
    std::string min_version;
    std::string max_version;
};

struct FirmwareCompatibilityConfigItemResult {
    std::string device_model_key;
    std::vector<std::string> compatible_versions;
    FirmwareCompatibilityRulesResult rules;
};

struct FirmwareCompatibilityConfigResult {
    std::vector<FirmwareCompatibilityConfigItemResult> data;
};


struct FirmwareServiceProgress {
    int percent;
    int current_step;
};

struct FirmwareServiceDevice {
    std::string sn;
    int firmware_upgrade_type;
    std::string product_version;
};

struct FirmwareServiceData {
    std::vector<FirmwareServiceDevice> devices;
};

struct FirmwareService {
    std::string bid;
    std::string status;
    long long create_time;
    long long update_time;
    int device_code;
    FirmwareServiceProgress progress;
    FirmwareServiceData data;
};

struct FirmwareStatusItem {
    std::string sn;                 // 设备序列号
    std::string firmware_version;   // 固件版本
    int firmware_status;            // 固件状态, {1: 无需升级, 2: 待升级, 3: 一致性升级, 4: 固件升级中};
    std::string device_model_key;   // 设备型号 Key
    FirmwareService services;       // 关联的升级服务
};

struct FirmwareStatusListResult {
    std::vector<FirmwareStatusItem> list;
};

struct FirmwareInfoParam {
    std::string device_model_key;
    std::string version;
    std::string desc;
    std::string object_key;
    std::string md5;
};

// ==================== 解锁证书相关 ====================

struct UnlockLicenseStsCredentialsResult {
    std::string access_key_id;
    std::string access_key_secret;
    std::string security_token;
    long long expire = 0;
};

struct ImportUnlockLicenseStsResult {
    std::string provider;
    std::string bucket;
    std::string region;
    std::string endpoint;
    std::string file_store_dir;
    UnlockLicenseStsCredentialsResult credentials;
};

struct CheckUnlockLicenseResultItem {
    std::string file_name;
    std::string name;
    std::string type;
    long long begin_time = 0;
    long long end_time = 0;
    std::string url;
};

struct CheckUnlockLicenseResult {
    std::vector<std::string> err_file_name;
    std::vector<std::string> err_cert_mismatch;
    std::vector<CheckUnlockLicenseResultItem> list;
};

struct QueryDeviceUnlockLicenseListParam {
    std::string device_sn;
    int device_model_domain = 0;  // 设备类型域。枚举：0=飞机, 3=机场
    std::string license_type;     ///< 证书类型过滤
    std::string license_id;       ///< 证书 ID 过滤
    bool detail = false;          ///< 是否返回详情
};

struct DeviceUnlockLicenseCommonFields {
    int id = 0;
    std::string name;
    int type = 0;
    int group_id = 0;
    std::string user_id;
    std::string device_sn;
    long long begin_time = 0;
    long long end_time = 0;
    bool user_only = false;
    bool enabled = false;
};

struct DeviceUnlockAreaUnlock {
    std::vector<int> area_ids;
};

struct DeviceUnlockCircleUnlock {
    double radius = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
};

struct DeviceUnlockCountryUnlock {
    int country_number = 0;
    double height = 0.0;
};

struct DeviceUnlockHeightUnlock {
    double height = 0.0;
};

struct DeviceUnlockPolygonPoint {
    double latitude = 0.0;
    double longitude = 0.0;
};

struct DeviceUnlockPolygonUnlock {
    std::vector<DeviceUnlockPolygonPoint> points;
};

struct DeviceUnlockPowerUnlock {
    int level = 0;
};

struct DeviceUnlockRidUnlock {
    int level = 0;
};

struct DeviceUnlockLicenseItem {
    DeviceUnlockLicenseCommonFields common_fields;
    DeviceUnlockAreaUnlock area_unlock;
    DeviceUnlockCircleUnlock circle_unlock;
    DeviceUnlockCountryUnlock country_unlock;
    DeviceUnlockHeightUnlock height_unlock;
    DeviceUnlockPolygonUnlock polygon_unlock;
    DeviceUnlockPowerUnlock power_unlock;
    DeviceUnlockRidUnlock rid_unlock;
};

struct QueryDeviceUnlockLicenseListResult {
    int result = 0;
    int device_model_domain = 0;
    bool consistence = false;
    std::vector<DeviceUnlockLicenseItem> licenses;
};

struct SwitchUnlockLicenseParam {
    std::string device_sn;
    int license_id = 0;
    bool enable = false;
};

struct CheckUnlockLicenseParam {
    std::string file_key;
    std::string device_sn;
};

struct ImportUnlockLicenseParam {
    std::string device_sn;
    std::vector<std::string> cert_url;
};

/**
 * @brief 上传解锁证书文件成功后的返回结果，供后续 checkUnlockLicense / submitUnlockLicense 使用
 * 流程：上传证书 -> 用 file_store_dir + 文件名拼成 file_key 调 checkUnlockLicense -> 用 check 返回的 url 调 submitUnlockLicense
 */
struct UploadUnlockLicenseFileResult {
    std::string file_store_dir;  ///< 存储目录，如 "unlock_license"，用于拼接 file_key 调用 checkUnlockLicense
};

// WaylineList Begin
struct GetWaylineListParam {
    int page = 0;                                    // 页码（必填）
    int page_size = 0;                               // 每页数量（必填）
    long long start_timestamp = 0;                   // 开始时间（秒级时间戳）。需与 end_timestamp 同时传入
    long long end_timestamp = 0;                     // 结束时间（秒级时间戳）。需与 start_timestamp 同时传入
    std::vector<std::string> drone_types;             // 飞行器类型过滤
    std::vector<std::string> wayline_uuids;           // 航线 UUID 过滤
    std::vector<std::string> payload_types;            // 载荷类型过滤
    std::vector<std::string> wayline_types;            // 航线类型过滤
};
struct WaylineListPaginationResult {
    int page = 0;
    int page_size = 0;
    int total = 0;
};

struct WaylineListItemResult {
    std::string wayline_uuid;
    std::vector<int> wayline_types;
    std::string wayline_name;
    int size = 0;
    std::vector<std::string> payload_types;
    std::vector<std::string> payload_types_with_index;
    std::string drone_type;
    long long updated_at = 0;
    bool with_megaphone = false;
    bool with_search_light = false;
    bool is_ai_wayline = false;
};

struct GetWaylineListResult {
    WaylineListPaginationResult pagination;
    std::vector<WaylineListItemResult> list;
};
// WaylineList End

// WaylineDetail Begin
struct WaylineDetailSurveyAreaScopePoint {
    double lng = 0;
    double lat = 0;
    double alt = 0;
};

struct WaylineDetailSurveyAreaScope {
    std::vector<WaylineDetailSurveyAreaScopePoint> points;
};

struct WaylineDetailKmzData {
    /// 航线类型。枚举：0=航点航线(WAYPOINT), 1=面状航线/正射(MAPPING2D), 2=面状航线/倾斜(MAPPING3D),
    /// 3=带状航线(MAPPINGSTRIP), 4=斜面航线(MAPPING2D_Facade), 5=标准几何体航线(SOLID),
    /// 7=贴近摄影航线(MAPPING), 8=AI巡逻航线(AI_PATROL)
    int wayline_type = 0;
    int wayline_point_count = 0;
    int exit_on_rc_lost_type = 0;
    int exit_on_rc_lost_behavior = 0;
    int start_record_action_count = 0;
    int stop_record_action_count = 0;
    int ortho_camera_overlap_h = 0;
    int ortho_camera_overlap_w = 0;
    int inclined_camera_overlap_h = 0;
    int inclined_camera_overlap_w = 0;
    int supplement_prism_model_count = 0;
    int estimated_photo_count = 0;
    double duration = 0;
    double distance = 0;
    double survey_area = 0;
    double start_latitude = 0;
    double start_longitude = 0;
    double start_altitude = 0;
    double margin_distance = 0;
    double ortho_gsd = 0;
    double incline_gsd = 0;
    bool is_wayline_validity_check = false;
    bool has_oriented_action = false;
    bool has_accurate_action = false;
    bool is_surface_fly_on = false;
    bool is_elevation_optimization_on = false;
    bool is_smart_oblique_on = false;
    bool is_mapping_strip_use_template_altitude = false;
    bool is_with_megaphone = false;
    bool is_with_search_light = false;
    bool is_ai_wayline = false;
    std::string drone_type;
    std::string target_detect_area_file_path;
    std::vector<int> wayline_validity_check_codes;
    std::vector<std::string> elevation_file_names;
    std::vector<std::string> payload_types;
    std::vector<std::string> payload_types_with_index;
    std::vector<std::string> accurate_action_uuids;
    std::vector<std::string> action_uuids;
    std::vector<std::string> res_file_md5s;
    std::vector<std::string> audio_file_md5s;
    std::vector<std::string> target_detect_action_uuids;
    std::vector<std::string> ai_audio_file_md5s;
    std::unordered_map<std::string, std::string> action_uuid_to_md5_map;
    std::unordered_map<std::string, std::string> audio_action_uuid_to_file_path_map;
    std::unordered_map<std::string, std::string> ai_audio_action_uuid_to_file_path_map;
    WaylineDetailSurveyAreaScope survey_area_scope;
};

struct WaylineDetailExtData {
    std::string kmz_without_res_object_key;
    std::string kmz_without_res_md5;
    std::string target_area_file_object_key;
    std::string business_file_object_key;
    std::vector<std::string> model_file_object_keys;
};

struct GetWaylineDetailResult {
    std::string wayline_uuid;
    std::string md5;
    std::string wayline_name;
    std::string kmz_object_key;
    std::string url;
    std::string suffix;
    long long deleted_at = 0;
    long long updated_at = 0;
    std::string kmz_without_res_url;
    std::string target_area_file_url;
    std::string business_file_url;
    WaylineDetailKmzData kmz_data;
    WaylineDetailExtData ext_data;
    std::unordered_map<std::string, std::string> editor_warnings;  ///< 编辑态告警
    std::unordered_map<std::string, std::string> execute_warnings; ///< 执行态告警
};
// WaylineDetail End

struct SubmitWaylineInfoParam {
    std::string kmz_object_key;
    std::string wayline_name;
    std::string async_request_id;
    bool return_paas_data = false;
    long long timestamp = 0;
    std::unordered_map<std::string, std::string> transparent; ///< 透传数据
};

struct BatchDownloadWaylinesParam {
    std::vector<std::string> wayline_uuids;
};

struct BatchDownloadWaylinesItemResult {
    std::string wayline_uuid;
    std::string wayline_name;
    std::string kmz_object_key;
    std::string url;
    int size = 0;
};

struct BatchDownloadWaylinesResult {
    std::vector<BatchDownloadWaylinesItemResult> list;
};

struct DuplicateWaylineParam {
    std::string original_wayline_uuid;
    std::string target_wayline_name;
    std::string target_object_key_prefix;
};

struct DuplicateWaylineResult {
    std::string wayline_uuid;
    std::string wayline_name;
};

// ============================================================
// CopyWayline（航线复制）
// ============================================================
struct CopyWaylineParam {
    std::string original_wayline_uuid;   ///< 原始航线 UUID（必填）
    std::string target_wayline_name;     ///< 目标航线名称（必填）
};

struct CopyWaylineResult {
    std::string wayline_uuid;            ///< 新复制的航线 UUID
    std::string wayline_name;            ///< 新复制的航线名称
};

// ============================================================
// ConcatWayline（航线拼接）
// ============================================================
struct ConcatWaylineParam {
    std::string target_wayline_name;                                  ///< 目标航线名称（必填）
    std::string kmz_object_key;                                       ///< KMZ 文件对象存储路径（必填）
    std::vector<std::string> sub_wayline_uuids;                       ///< 子航线 UUID 列表，按拼接顺序（必填）
    std::unordered_map<std::string, std::string> editor_warnings;     ///< 编辑器警告（可选）
};

/// 模型边界信息（中心点 + 半长）
struct ConcatWaylineExtDataModelBoundary {
    double latitude = 0;              ///< 中心纬度
    double longitude = 0;             ///< 中心经度
    double altitude = 0;              ///< 中心海拔高度
    double half_x = 0;                ///< 经度距离半长（米）
    double half_y = 0;                ///< 纬度距离半长（米）
    double half_z = 0;                ///< 海拔高度半长（米）
    std::vector<double> transform;    ///< 变换矩阵
};

/// 二维地理坐标点
struct ConcatWaylineExtDataModelBoundingPoint {
    double lng = 0;                   ///< 经度
    double lat = 0;                   ///< 纬度
};

/// 二维地理多边形
struct ConcatWaylineExtDataModelBounding {
    std::vector<ConcatWaylineExtDataModelBoundingPoint> points;  ///< 多边形顶点列表
};

/// 贴近摄影模型文件信息
struct ConcatWaylineExtDataModel {
    std::string uuid;                                            ///< 模型 UUID
    std::string file_key;                                        ///< 文件存储 Key
    ConcatWaylineExtDataModelBoundary boundary;                  ///< 模型边界信息
    ConcatWaylineExtDataModelBounding bounding;                  ///< 模型包围多边形
};

/// 航线拼接扩展数据
struct ConcatWaylineExtData {
    std::string kmz_without_res_object_key;                      ///< 航线 KMZ 文件存储路径（不含资源文件）
    std::string kmz_without_res_md5;                             ///< 航线 KMZ 文件 MD5（不含资源文件）
    std::string target_area_file_object_key;                     ///< 目标识别区域文件存储路径
    std::string business_file_object_key;                        ///< 业务文件存储桶路径
    std::vector<std::string> model_file_object_keys;             ///< 贴近摄影航线关联模型索引文件路径列表
    std::vector<std::string> elevation_file_object_keys;         ///< 高程文件存储桶路径列表
    long long content_length = 0;                                ///< 航线文件大小（字节）
    std::vector<ConcatWaylineExtDataModel> models;               ///< 贴近摄影模型信息列表
};

struct ConcatWaylineResult {
    std::string wayline_uuid;            ///< 拼接后的新航线 UUID
    WaylineDetailKmzData kmz_data;       ///< KMZ 航线解析数据
    ConcatWaylineExtData ext_data;       ///< 航线扩展数据
};

// ============================================================
// ImportKmz（上传 KMZ 并导入航线）
// ============================================================
struct ImportKmzItem {
    std::string kmz_object_key;          ///< KMZ 文件在对象存储中的路径，格式：wayline/import/<filename>.kmz
    std::string wayline_name;            ///< 航线名称（默认为文件名去掉 .kmz 扩展名）
    std::string async_request_id;        ///< 异步请求 ID（UUID 格式），可用于后续查询导入状态
};

struct ImportKmzResult {
    std::vector<ImportKmzItem> list;     ///< 导入成功的航线列表
    int total = 0;                       ///< 导入成功的数量
};

/// 批量删除航线结果（含 FH2 通用响应信息，后续可在此结构体扩展字段）
struct BatchDeleteWaylinesResult {
    int server_code = 0;
    std::string server_message;
};

struct UpdateWaylineResult {
    std::string status;
};

// ImportWaylineEvent 航线导入事件
struct ImportWaylineEventPoint {
    double lng = 0.0;
    double lat = 0.0;
    double alt = 0.0;
};

struct ImportWaylineEventSurveyAreaScope {
    std::vector<ImportWaylineEventPoint> points;
};

struct ImportWaylineEventKmzData {
    /// 航线类型。枚举：0=航点航线(WAYPOINT), 1=面状航线/正射(MAPPING2D), 2=面状航线/倾斜(MAPPING3D),
    /// 3=带状航线(MAPPINGSTRIP), 4=斜面航线(MAPPING2D_Facade), 5=标准几何体航线(SOLID),
    /// 7=贴近摄影航线(MAPPING), 8=AI巡逻航线(AI_PATROL)
    int wayline_type = 0;
    int wayline_point_count = 0;
    int exit_on_rc_lost_type = 0;
    int exit_on_rc_lost_behavior = 0;
    int start_record_action_count = 0;
    int stop_record_action_count = 0;
    int ortho_camera_overlap_h = 0;
    int ortho_camera_overlap_w = 0;
    int inclined_camera_overlap_h = 0;
    int inclined_camera_overlap_w = 0;
    int supplement_prism_model_count = 0;
    int estimated_photo_count = 0;
    double duration = 0.0;
    double distance = 0.0;
    double survey_area = 0;
    double start_latitude = 0.0;
    double start_longitude = 0.0;
    double start_altitude = 0.0;
    double margin_distance = 0;
    double ortho_gsd = 0;
    double incline_gsd = 0;
    bool is_wayline_validity_check = false;
    bool has_oriented_action = false;
    bool has_accurate_action = false;
    bool is_surface_fly_on = false;
    bool is_elevation_optimization_on = false;
    bool is_smart_oblique_on = false;
    bool is_mapping_strip_use_template_altitude = false;
    bool is_with_megaphone = false;
    bool is_with_search_light = false;
    bool is_ai_wayline = false;
    std::string drone_type;
    std::string target_detect_area_file_path;
    std::vector<int> wayline_validity_check_codes;
    std::vector<std::string> elevation_file_names;
    std::vector<std::string> payload_types;
    std::vector<std::string> payload_types_with_index;
    std::vector<std::string> accurate_action_uuids;
    std::vector<std::string> action_uuids;
    std::vector<std::string> res_file_md5s;
    std::vector<std::string> audio_file_md5s;
    std::vector<std::string> target_detect_action_uuids;
    std::vector<std::string> ai_audio_file_md5s;
    std::unordered_map<std::string, std::string> action_uuid_to_md5_map;
    std::unordered_map<std::string, std::string> audio_action_uuid_to_file_path_map;
    std::unordered_map<std::string, std::string> ai_audio_action_uuid_to_file_path_map;
    ImportWaylineEventSurveyAreaScope survey_area_scope;
};

struct ImportWaylineEventExtData {
    std::string kmz_without_res_object_key;
    std::string kmz_without_res_md5;
    std::string target_area_file_object_key;
    std::string business_file_object_key;
    std::vector<std::string> model_file_object_keys;
};

struct ImportWaylineEventResult {
    std::string async_request_id;
    long long timestamp = 0;
    std::string kmz_object_key;
    std::string wayline_uuid;
    int code = 0;
    std::string message;
    ImportWaylineEventKmzData kmz_data;
    ImportWaylineEventExtData ext_data;
};
// ImportWaylineEvent End

// =====================================================================
// AI Inside 机载算法管理对外结构体（SWIG 可见，时间戳用 long long）
// =====================================================================

struct AIInsideLabel {
    int type = 0;         ///< 标签类型枚举值（如 1=目标类型）
    std::string name;     ///< 标签名称（如 "fire"、"smoke"）
};

struct AIInsideModelParam {
    double conf_threshold = 0;  ///< 置信度阈值，低于此值的检测结果将被过滤
    int nms_pre = 0;            ///< NMS 前最大候选框数量
    double iou_threshold = 0;   ///< IoU 阈值，用于非极大值抑制（NMS）去重
};

struct AIInsideLicense {
    std::vector<std::string> sn_list;  ///< 授权飞机 SN 列表，仅列表内设备可装载此算法
    long long started_utc = 0;         ///< 授权开始时间（Unix 秒）
    long long expired_utc = 0;         ///< 授权到期时间（Unix 秒），超期后算法状态变为 expired
};

// GetAIInsideModels Begin
struct AIInsideModelItem {
    std::string signed_id;       ///< 算法签名 ID，全局唯一标识
    std::string signed_name;     ///< 算法签名名称（英文标识符，如 "fire_detection"）
    std::string model_name;      ///< 算法显示名称（可读名称，如 "Fire Detection"）
    int signed_version = 0;      ///< 算法版本号，同 signed_id 下版本递增
    long long signed_utc = 0;    ///< 算法签名时间戳（Unix 秒）
    long long expired_utc = 0;   ///< 算法过期时间戳（Unix 秒）
    AIInsideModelParam model_param;           ///< 模型推理参数（置信度、NMS、IoU）
    std::vector<AIInsideLabel> labels;        ///< 算法可识别的目标标签列表
    std::vector<std::string> sn_list;         ///< 绑定的飞机 SN 列表（同 license.sn_list）
    std::vector<int> image_source;            ///< 支持的图像来源（0=可见光, 1=红外）
    std::vector<int> digital_effect;          ///< 支持的数字效果列表
    std::vector<int> vl_resolution;           ///< 支持的可见光分辨率（0=1k, 1=2k）
};

struct GetAIInsideModelsResult {
    std::vector<AIInsideModelItem> models;  ///< 组织下算法列表，按 signed_utc 降序排列
};
// GetAIInsideModels End

// GetAIInsideModelDevices Begin
struct GetAIInsideModelDevicesParam {
    std::string signed_id;                 ///< 算法签名 ID（必填）
    int page = 1;                          ///< 页码，从 1 开始（必填）
    int page_size = 20;                    ///< 每页条数（必填）
    std::vector<std::string> drone_sn;     ///< 指定查询的飞机 SN 列表（可选，为空则查询全部绑定设备）
};

struct AIInsideDeviceModel {
    std::string device_model_key;  ///< 设备型号枚举 Key（如 "EA230"）
    std::string domain;            ///< 设备域（"dock" 表示机场，"drone" 表示飞机）
    std::string name;              ///< 设备型号显示名称
};

struct AIInsideOperateProgressDetail {
    int percent = 0;       ///< 进度百分比 0~100
    int current_step = 0;  ///< 当前步骤
};

struct AIInsideOperateProgress {
    std::string device_cmd_method;           ///< 当前操作指令类型（"load_ai_model" / "unload_ai_model"）
    std::string status;                      ///< 操作状态字符串（如 "ok" / "failed"）
    AIInsideOperateProgressDetail progress;  ///< 嵌套进度
    int device_code = 0;                     ///< 设备侧错误码（0=无错）
    long long create_time = 0;               ///< 操作创建时间（Unix ms）
};

struct AIInsideModelInfo {
    std::string signed_id;        ///< 飞机当前装载的算法签名 ID
    std::string model_name;       ///< 飞机当前装载的算法显示名称
    long long signed_utc = 0;     ///< 飞机当前装载的算法签名时间戳（Unix 秒）
    long long expired_utc = 0;    ///< 飞机当前装载的算法过期时间戳（Unix 秒）
    std::string status;           ///< 算法装载状态：latest/updatable/not_loaded/expired/not_supported
};

struct AIInsideAssociateDrone {
    std::string device_sn;               ///< 飞机 SN
    AIInsideDeviceModel device_model;    ///< 飞机型号信息
    bool device_online_status = false;   ///< 飞机是否在线
    int device_working_status = 0;       ///< 飞机工作状态码
    AIInsideModelInfo ai_model_info;     ///< 飞机当前算法装载状态
};

struct AIInsideDeviceItem {
    std::string device_sn;                          ///< 机场 SN
    AIInsideDeviceModel device_model;               ///< 机场型号信息
    std::string complete_machine_sn;                ///< 整机铭牌 SN
    bool device_online_status = false;              ///< 机场是否在线
    int device_working_status = 0;                  ///< 机场工作状态码
    AIInsideOperateProgress operate_ai_model;       ///< 机场当前算法操作进度
    AIInsideAssociateDrone associate_drone_device_info; ///< 机场关联的飞机信息
};

struct GetAIInsideModelDevicesResult {
    std::vector<AIInsideDeviceItem> list;  ///< 当前页设备列表
    int total = 0;                         ///< 总设备数
    int page = 0;                          ///< 当前页码
    int page_size = 0;                     ///< 每页条数
};
// GetAIInsideModelDevices End

// GetAIInsideModelDevicesSummary Begin
struct GetAIInsideModelDevicesSummaryParam {
    std::string signed_id;        ///< 算法签名 ID（必填）
    std::string operation_type;   ///< 操作类型：load（装载）或 update（更新）（必填）
};

struct GetAIInsideModelDevicesSummaryResult {
    int total_devices = 0;     ///< 该算法绑定的总设备数
    int operable_devices = 0;  ///< 满足当前操作类型条件的可操作设备数
};
// GetAIInsideModelDevicesSummary End

// OperateAIInsideModel Begin
struct OperateAIInsideModelParam {
    std::string signed_id;                        ///< 算法签名 ID（必填）
    std::string operation_type;                   ///< 操作类型：load（装载）/ update（更新）/ unload（卸载）（必填）
    std::vector<std::string> drone_sn_list;       ///< 指定操作的设备 SN 列表（可选，为空则对全部可操作设备下发）
};

struct AIInsideOperateDeviceResult {
    std::string drone_sn;  ///< 飞机 SN
    int code = 0;          ///< 操作结果码（0=成功，非 0 见错误码表，如 238002=机场状态不满足）
};

struct OperateAIInsideModelResult {
    std::vector<AIInsideOperateDeviceResult> results;  ///< 各设备操作结果列表
};
// OperateAIInsideModel End

// CreateAIInsideAlgorithm Begin
struct CreateAIInsideAlgorithmParam {
    std::string name;   ///< 上传的文件名（如 "fire_detection_v2.zip"）（必填）
};

struct AIInsideCredentials {
    std::string access_key_id;      ///< MinIO AccessKey ID
    std::string access_key_secret;  ///< MinIO SecretKey
    long long expire = 0;           ///< 凭证过期时间（Unix 秒）
    std::string security_token;     ///< STS SessionToken（当前实现为空字符串）
};

struct CreateAIInsideAlgorithmResult {
    AIInsideCredentials credentials;  ///< MinIO 临时上传凭证
    std::string provider;             ///< 存储提供商标识（如 "minio"）
    std::string bucket;               ///< 存储 Bucket 名称
    std::string region;               ///< 存储区域
    std::string endpoint;             ///< 存储访问端点（含协议头，如 "https://minio.example.com"）
    std::string file_store_dir;       ///< 文件存储目录前缀，上传时使用此路径拼接文件名
};
// CreateAIInsideAlgorithm End

// FileUploadAIInsideAlgorithm Begin
struct FileUploadAIInsideAlgorithmParam {
    std::string file_key;   ///< 上传完成后的 MinIO 对象 Key（由 createAIInsideAlgorithm 接口返回的路径拼接文件名）（必填）
};

struct AIInsideAlgorithmDetail {
    int id = 0;                     ///< DB 自增主键
    std::string created_at;         ///< 创建时间（RFC3339 格式）
    std::string updated_at;         ///< 最后更新时间（RFC3339 格式）
    std::string signed_id;          ///< 算法签名 ID，全局唯一标识
    std::string signed_name;        ///< 算法签名名称（英文标识符）
    int signed_version = 0;         ///< 算法版本号，同 signed_id 下版本递增
    long long signed_utc = 0;       ///< 算法签名时间戳（Unix 秒）
    std::string model_uuid;         ///< 模型 UUID
    std::string model_name;         ///< 模型显示名称
    std::string model_type;         ///< 模型类型（如 "detection"）
    std::vector<AIInsideLabel> labels;        ///< 算法可识别的目标标签列表
    AIInsideModelParam model_param;           ///< 模型推理参数（置信度、NMS、IoU）
    AIInsideLicense license;                  ///< 授权信息（含授权 SN 列表和有效期）
    std::vector<int> applicable_product;      ///< 适用产品 ID 列表
    std::string file_key;                     ///< MinIO 对象存储路径
    std::vector<int> image_source;            ///< 支持的图像来源（0=可见光, 1=红外）
    std::vector<int> digital_effect;          ///< 支持的数字效果列表
    std::vector<int> vl_resolution;           ///< 支持的可见光分辨率（0=1k, 1=2k）
};

struct FileUploadAIInsideAlgorithmResult {
    AIInsideAlgorithmDetail detail;  ///< 算法入库后的完整详情
};
// FileUploadAIInsideAlgorithm End

// DeleteAIInsideAlgorithm Begin
struct DeleteAIInsideAlgorithmParam {
    std::string signed_id;   ///< 要删除的算法签名 ID（必填）
};
// DeleteAIInsideAlgorithm End

// UploadAIInsideAlgorithmFile Begin
/// 一站式算法文件上传参数：内部自动完成「获取 STS 凭证 → 上传文件 → 通知后端处理」三步
struct UploadAIInsideAlgorithmFileParam {
    std::string file_path;  ///< 本地算法 ZIP 文件的绝对路径（必填）
};

/// 一站式算法文件上传结果：上传成功后可直接得到算法入库的完整详情
struct UploadAIInsideAlgorithmFileResult {
    std::string object_key;              ///< MinIO 对象存储路径（上传后生成）
    std::string object_url;              ///< MinIO 对象访问 URL
    std::string etag;                    ///< 文件 ETag（上传后生成）
    AIInsideAlgorithmDetail detail;      ///< 算法入库后的完整详情（由 file-upload 接口返回）
};
// UploadAIInsideAlgorithmFile End

// [信号地图查询]: 信号网格数据;
class GridSignalItem {
public:
    std::string h3_index;        // Uber H3网格下标，等级为 11;
    int sdr_avg = 0;             // 平均sdr信号强度;
    int sdr_max = 0;             // 最大sdr信号强度;
    int sdr_min = 0;             // 最小sdr信号强度;

    int signal_4g_avg = 0;       // 平均4g信号强度, 如果没有插入4G模块, 则无此字段;
    int signal_4g_max = 0;       // 最大4g信号强度, 如果没有插入4G模块, 则无此字段;
    int signal_4g_min = 0;       // 最小4g信号强度, 如果没有插入4G模块, 则无此字段;
    
    
    bool operator==(const GridSignalItem &rhs) const {
        return
            h3_index == rhs.h3_index &&
            sdr_avg == rhs.sdr_avg &&
            sdr_max == rhs.sdr_max &&
            sdr_min == rhs.sdr_min &&
            signal_4g_avg == rhs.signal_4g_avg &&
            signal_4g_max == rhs.signal_4g_max &&
            signal_4g_min == rhs.signal_4g_min;
    }
};


class DeviceSignalItem {
public:
    std::string device_sn;
    std::vector<GridSignalItem> grids;

    bool operator==(const DeviceSignalItem &rhs) const {
        return device_sn == rhs.device_sn && grids == rhs.grids;
    }

    bool operator!=(const DeviceSignalItem &rhs) const {
        return !(*this == rhs);
    }
};

class SignalMapsResults {
public:
    std::vector<DeviceSignalItem> lists;

    bool operator==(const SignalMapsResults &rhs) const {
        return lists == rhs.lists;
    }
    
    bool operator!=(const SignalMapsResults &rhs) const {
        return !(*this == rhs);
    }
};


// 自定义区域: 创建自定义区域请求参数;

// 自定区域: 属性;
class ContentsPropertyData {
public:
    std::string color;             // 颜色, 颜色值用于方便在地图上面区分不同区域, 有效值比如 "#FF0000"
    // 缓冲宽度（米），仅折线(Polyline)类型飞行区域有效，取值范围 [10, 100]，默认 10
    // 含义：折线两侧各扩展 width/2 米形成面状区域
    int width = 0;
    bool clampToGround = false;    // 服务端(旧协议设计上字段是 "clampToGround");

    bool operator==(const ContentsPropertyData &rhs) const {
        return
            color == rhs.color &&
            width == rhs.width &&
            clampToGround == rhs.clampToGround;
    }
    
    bool operator!=(const ContentsPropertyData &rhs) const {
        return !(*this == rhs);
    }
};


// 自定义飞行区域:Geometry 参数;
// fixed: 使用 floatEqual 进行浮点型比较;
class ContentsGeometryData {
public:
    // type: "Circle", 圆形: 限飞区和作业区支持;
    // type: "Polygon", 多边形: 限飞区和作业区支持;
    // type: "Polyline", 自定义(折线): 折线仅禁降区支持;
    std::string type;
    // [折线/多边形] 顶点坐标，非圆形时必填。
    // 坐标格式：[经度, 纬度]（GeoJSON 标准，经度在前）
    // 多边形约束：
    //   - 至少 3 个顶点
    //   - 每个环的顶点数 < 255
    //   - 首尾坐标必须相同（闭合多边形）
    std::vector<std::vector<std::vector<double>>> coordinates;

    // 圆心坐标，创建圆形区域时传入。格式：[经度, 纬度]
    std::vector<double> center;
    // 半径（米），创建圆形区域时传入，需 > 0
    double radius = 0;

    bool operator==(const ContentsGeometryData &rhs) const {
        return
            type == rhs.type &&
            coordinates == rhs.coordinates &&
            center == rhs.center &&
            floatEqual(radius, rhs.radius);
    }

    bool operator!=(const ContentsGeometryData &rhs) const {
        return !(*this == rhs);
    }
};


// 自定义飞行区域: 飞行区内容 Contents;
class FlightAreasContentData {
public:
    std::string            type = "Feature";    // 固定值 "Feature";
    ContentsPropertyData   properties;
    ContentsGeometryData   geometry;

    bool operator==(const FlightAreasContentData &rhs) const {
        return
            type == rhs.type &&
            properties == rhs.properties &&
            geometry == rhs.geometry;
    }
    
    bool operator!=(const FlightAreasContentData &rhs) const {
        return !(*this == rhs);
    }
};


// 创建[自定义飞行区] 请求参数;
class CreateCustomizedFlightAreasParams {
public:
    std::string            name;            // name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区";
    // type, 飞行区类型, {"nfz" 限飞区; "dfence" 作业区; "noland" 禁降区}"
    // 数量限制：noland 最多 50 个, nfz 最多 100 个
    std::string            type;
    FlightAreasContentData content;         // content, 飞行区内容;
    std::string            created_by;      // created_by, 创建用户 ID;

    bool operator==(const CreateCustomizedFlightAreasParams &rhs) const {
        return
            name == rhs.name &&
            type == rhs.type &&
            content == rhs.content &&
            created_by == rhs.created_by;
    }
};

// 创建飞行区域返回值, 协议 response data 部分的数据;
class CreateFlightAreasResults {
public:
    std::string id;
    std::string name;

    bool operator==(const CreateFlightAreasResults &rhs) const {
        return id == rhs.id && name == rhs.name;
    }

    bool operator!=(const CreateFlightAreasResults &rhs) const {
        return !(*this == rhs);
    }
};


// list item;
class FlightAreaInfoItem {
public:
    std::string id;                  // 飞行区id;
    std::string name;                // 飞行区名称;
    std::string status;              // 飞行区状态;
    std::string type;                // 飞行区类型;
    FlightAreasContentData content;  // 飞行区内容;

    long long created_time = 0;      // 创建时间;
    std::string created_by;          // 创建用户 id;
    long long updated_time = 0;      // 更新时间;
    std::string updated_by;          // 更新用户 id;

    bool operator==(const FlightAreaInfoItem& rhs) const {
        return
            (id == rhs.id) &&
            (name == rhs.name) &&
            (status == rhs.status) &&
            (type == rhs.type) &&
            (content == rhs.content) &&
            (created_time == rhs.created_time) &&
            (created_by == rhs.created_by) &&
            (updated_time == rhs.updated_time) &&
            (updated_by == rhs.updated_by);
    }

    bool operator!=(const FlightAreaInfoItem& rhs) const {
        return !(*this == rhs);
    }
};


// FlightAreaPagination 分页信息;
class FlightAreaPagination {
public:
    int page = 0;          // 当前页码;
    int page_size = 0;     // 分页大小;
    int total = 0;         // 总数量;

    bool operator ==(const FlightAreaPagination& rhs) const {
        return page == rhs.page && page_size == rhs.page_size && total == rhs.total;
    }
    
    bool operator !=(const FlightAreaPagination& rhs) const {
        return !(*this == rhs);
    }
};


class GetFlightAreasResults {
public:
    std::vector<FlightAreaInfoItem> list;
    FlightAreaPagination pagination;

    bool operator ==(const GetFlightAreasResults& rhs) const {
        return list == rhs.list && pagination == rhs.pagination;
    }
    
    bool operator !=(const GetFlightAreasResults& rhs) const {
        return !(*this == rhs);
    }
};


class GetCustomizedFlightAreasParams {
public:
    std::string name;              // 飞行区名称过滤
    std::string type;              // 飞行区类型过滤。枚举："dfence"=作业区, "nfz"=限飞区, "noland"=禁降区
    std::string status;            // 状态过滤。枚举："disable"=已禁用, "enable"=已启用
    std::vector<std::string> ids;  // 飞行区 ID 过滤
    int page_index = 0;            // 页码，从 1 开始，不传查全部
    int page_size = 0;             // 每页数量
    
    
    bool operator==(const GetCustomizedFlightAreasParams& rhs) const {
        return
            (name == rhs.name) &&
            (type == rhs.type) &&
            (status == rhs.status) &&
            (ids == rhs.ids) &&
            (page_index == rhs.page_index) &&
            (page_size == rhs.page_size);
    }
};


class UpdatedAreaData {
public:
    std::string id;
    std::string name;
    std::string status;
    std::string type;

    FlightAreasContentData content;  // 飞行区内容;

    long long created_time = 0;
    std::string created_by;
    long long updated_time = 0;
    std::string updated_by;


    bool operator==(const UpdatedAreaData& rhs) const {
        return
            (id == rhs.id) &&
            (name == rhs.name) &&
            (status == rhs.status) &&
            (type == rhs.type) &&
            (content == rhs.content) &&
            (created_time == rhs.created_time) &&
            (created_by == rhs.created_by) &&
            (updated_time == rhs.updated_time) &&
            (updated_by == rhs.updated_by);
    }
    
    bool operator!=(const UpdatedAreaData& rhs) const {
        return !(*this == rhs);
    }
};


class UpdateFlightAreasResults {
public:
    std::string id;
    UpdatedAreaData updated_area;
    
    bool operator==(const UpdateFlightAreasResults& rhs) const {
        return (id == rhs.id) && (updated_area == rhs.updated_area);
    }
    
    bool operator!=(const UpdateFlightAreasResults& rhs) const {
        return !(*this == rhs);
    }
};


class DeviceSyncResults {
public:
    std::string device_sn;    // 待同步设备的SN,;
    int code = 0;                 // 同步结果码。枚举：0=成功, 230012=不支持自定义飞行区, 230013=设备离线, 230014=设备繁忙, 230015=超时;

    bool operator==(const DeviceSyncResults& rhs) const {
        return (device_sn == rhs.device_sn) && (code == rhs.code);
    }
    
    bool operator!=(const DeviceSyncResults& rhs) const {
        return !(*this == rhs);
    }
};


// 同步自定义飞行区;
class SyncFlightAreasResults {
public:
    std::vector<DeviceSyncResults> results;

    bool operator==(const SyncFlightAreasResults& rhs) const {
        return (results == rhs.results);
    }
};


class DeviceSyncStatusResults {
public:
    // 待同步设备的SN, 支持网关类型设备(机场1、2、3 以及 地面站);
    std::string sn;
    // 同步状态。枚举：{ "waiting"=等待同步, "synchronizing"=同步中, "synchronized"=已同步, "fail"=同步失败 };
    std::string status;
    // 失败原因码。
    /*
    {
        "0": "无错误(成功)",
        "1": "解析云端返回的文件信息失败",
        "2": "获取飞行器端文件信息失败",
        "3": "从云端下载文件失败",
        "4": "链路翻转失败",
        "5": "传输文件失败",
        "6": "disable失败",
        "7": "自定义飞行区删除失败",
        "8": "飞行器端加载作业区域数据失败",
        "9": "enable失败",
        "10": "机场增强图传无法关闭，作业区域数据同步失败",
        "11": "飞行器开机失败，无法同步作业区域数据",
        "12": "checksum校验失败",
        "13": "同步异常超时",
        "255": "未知错误(机场侧快速起飞，等不急服务端返回结果，主动上报255)"
    }
    */
    int reason = 0;

    bool operator==(const DeviceSyncStatusResults& rhs) const {
        return
            (sn == rhs.sn) &&
            (status == rhs.status) &&
            (reason == rhs.reason);
    }

    bool operator!=(const DeviceSyncStatusResults& rhs) const {
        return !(*this == rhs);
    }
};


class GetAreasSyncStatusResults {
public:
    std::vector<DeviceSyncStatusResults> list;

    bool operator==(const GetAreasSyncStatusResults& rhs) const {
        return (list == rhs.list);
    }

    bool operator!=(const GetAreasSyncStatusResults& rhs) const {
        return !(*this == rhs);
    }
};


class UpdateCustomizedFlightAreasParams {
public:
    std::string flight_area_id;

    std::string name;  // name, 飞行区名称, 比如 "圆形限飞区", "圆形作业区";
    FlightAreasContentData content;         // content, 飞行区内容;
    std::string update_by;  // update_by, 更新用户 ID;

    bool operator==(const UpdateCustomizedFlightAreasParams &rhs) const {
        return
            flight_area_id == rhs.flight_area_id &&
            name == rhs.name &&
            content == rhs.content &&
            update_by == rhs.update_by;
    }
    
    bool operator!=(const UpdateCustomizedFlightAreasParams &rhs) const {
        return !(*this == rhs);
    }
};


struct OfflineMapsModelInfoData {
    std::string uuid;

    bool operator==(const OfflineMapsModelInfoData &rhs) const {
        return uuid == rhs.uuid;
    }

    bool operator!=(const OfflineMapsModelInfoData &rhs) const {
        return !(*this == rhs);
    }
};

struct OfflineMapsModelData {
    int id = -1;               // 离线地图模型ID;
    long long updated_time = 0;     // 更新时间;
    std::vector<OfflineMapsModelInfoData> models;  // 模型信息;
    int percent = 0;           // 同步进度百分比;
    std::string status;        // 状态。枚举："finish"=完成;
    int result = 0;            // 结果码;

    bool operator==(const OfflineMapsModelData &rhs) const {
        return
            id == rhs.id &&
            updated_time == rhs.updated_time &&
            models == rhs.models &&
            percent == rhs.percent &&
            status == rhs.status &&
            result == rhs.result;
    }
    
    bool operator!=(const OfflineMapsModelData &rhs) const {
        return !(*this == rhs);
    }
};


// 查询离线地图模型信息;
class GetOfflineMapsModelInfoResults {
public:
    bool disable = false;               //  是否被禁用;
    OfflineMapsModelData offline_map;

    bool operator==(const GetOfflineMapsModelInfoResults &rhs) const {
        return disable == rhs.disable && offline_map == rhs.offline_map;
    }
    
    bool operator!=(const GetOfflineMapsModelInfoResults &rhs) const {
        return !(*this == rhs);
    }
};


class ModelItemInfoData {
public:
    std::string uuid;
    std::string object_key;    // 对象存储 key;
    std::string file_type;     // 文件类型。枚举："tif", "b3dm", "pnts";

    bool operator==(const ModelItemInfoData &rhs) const {
        return
            uuid == rhs.uuid &&
            object_key == rhs.object_key &&
            file_type == rhs.file_type;
    }
    
    bool operator!=(const ModelItemInfoData &rhs) const {
        return !(*this == rhs);
    }
};


// 更新离线地图模型;
class UpdateOfflineMapsParams {
public:
    std::vector<ModelItemInfoData> model_3d_items;
    std::vector<ModelItemInfoData> model_2d_items;

    bool operator==(const UpdateOfflineMapsParams &rhs) const {
        return model_3d_items == rhs.model_3d_items && model_2d_items == rhs.model_2d_items;
    }
    
    bool operator!=(const UpdateOfflineMapsParams &rhs) const {
        return !(*this == rhs);
    }
};



class OfflineMapsSyncResults {
public:
    std::vector<DeviceSyncResults> results;
    
    bool operator==(const OfflineMapsSyncResults &rhs) const {
        return results == rhs.results;
    }
    
    bool operator!=(const OfflineMapsSyncResults &rhs) const {
        return !(*this == rhs);
    }
};


class GetOfflineMapsSyncStatusResults {
public:
    std::vector<DeviceSyncStatusResults> list;
    
    
    bool operator==(const GetOfflineMapsSyncStatusResults &rhs) const {
        return list == rhs.list;
    }

    bool operator!=(const GetOfflineMapsSyncStatusResults &rhs) const {
        return !(*this == rhs);
    }
};


class SetOfflineMapsParams {
public:
    bool offline_map_enable = false;  // 离线地图开关;

    bool operator==(const SetOfflineMapsParams &rhs) const {
        return offline_map_enable == rhs.offline_map_enable;
    }
    
    bool operator!=(const SetOfflineMapsParams &rhs) const {
        return !(*this == rhs);
    }
};


// [媒体服务] 获取媒体文件列表请求参数;
class GetMediaFilesListParams {
public:
    std::string flight_id;                // [可选参数] 飞行航线 ID, 传空字符串("")表示不指定具体的航线 ID;
    int start_index = 0;                  // [必须参数] 下一页请求的起始 index(第一次查询传入 0, 请求的响应结果返回会返回下一页起始索引);
    int page_size = 100;                  // [必须参数] 每页大小(比如可以传 100, 默认可传 100, 最大500);
    std::vector<std::string> file_types;  // [可选参数] file_types, 文件枚举类型("2": 图片, "3": 视频), 比如: ["2", "3"];
    std::string device_sn;                // [可选参数] 设备 sn(机场 Dock SN，非飞机 Drone SN), 传空字符串表示不指定具体的 设备; 媒体文件由机场上传至云端, 服务端 media_files.sn 存的是机场 SN 并做等值匹配, 传飞机 SN 查不到数据;
    long long start_create_time = -1;     // [可选参数] 开始时间, 时间戳: 秒, 传 -1 表示不指定该参数;
    long long end_create_time = -1;       // [可选参数] 结束时间, 时间戳: 秒, 传 -1 表示不指定该参数;

    bool operator==(const GetMediaFilesListParams &rhs) const {
        return
            flight_id == rhs.flight_id &&
            start_index == rhs.start_index &&
            page_size == rhs.page_size &&
            file_types == rhs.file_types &&
            device_sn == rhs.device_sn &&
            start_create_time == rhs.start_create_time &&
            end_create_time == rhs.end_create_time;
    }

    bool operator!=(const GetMediaFilesListParams &rhs) const {
        return !(*this == rhs);
    }
};


// 位置信息;
class PositionData {
public:
    double lat = 0.0;         // 纬度;
    double lng = 0.0;         // 经度;
    double abs_alt = 0.0;     // 绝对高度;
    double rel_alt = 0.0;     // 相对高度;

    bool operator==(const PositionData &rhs) const {
        return
            floatEqual(lat, rhs.lat) &&
            floatEqual(lng, rhs.lng) &&
            floatEqual(abs_alt, rhs.abs_alt) &&
            floatEqual(rel_alt, rhs.rel_alt);
    }
    
    bool operator!=(const PositionData &rhs) const {
        return !(*this == rhs);
    }
};


// 媒体文件列表: 图片的扩展数据;
class MediaFilesListItemExtData {
public:
    std::string create_time;         // 创建时间;
    double flight_yaw = 0.0;         // 飞行偏航角;
    double focal_length = 0.0;       // 焦距;
    double gimbal_pitch = 0.0;       // 云台俯仰角;
    double gimbal_roll = 0.0;        // 云台横滚角;
    double gimbal_yaw = 0.0;         // 云台偏航角;
    int width = 0;                   // 图片宽度;
    int height = 0;                  // 图片高度;
    std::string image_type;          // 图片类型; { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera" }, 空字符串表示无效;
    PositionData photo_position;     // 图片位置信息;
    PositionData shoot_position;     // 飞机位置信息;
    double zoom_factor = INT_MIN;    // 焦距倍数, 服务端可能无返回值, 默认为 INT_MIN(比如 -2.147483648E9) 无效值;

    bool operator==(const MediaFilesListItemExtData &rhs) const {
        return
            create_time == rhs.create_time &&
            floatEqual(flight_yaw, rhs.flight_yaw) &&
            floatEqual(focal_length, rhs.focal_length) &&
            floatEqual(gimbal_pitch, rhs.gimbal_pitch) &&
            floatEqual(gimbal_roll, rhs.gimbal_roll) &&
            floatEqual(gimbal_yaw, rhs.gimbal_yaw) &&
            width == rhs.width &&
            height == rhs.height &&
            image_type == rhs.image_type &&
            photo_position == rhs.photo_position &&
            shoot_position == rhs.shoot_position &&
            floatEqual(zoom_factor, rhs.zoom_factor);
    }

    bool operator!=(const MediaFilesListItemExtData &rhs) const {
        return !(*this == rhs);
    }
};

// 媒体文件列表 item 数据;
class MediaFilesListItemInfoData {
public:
    long long file_id = -1;                    // 文件id;
    std::string flight_id;                     // 飞行记录id;
    std::string wayline_action_uuid;           // 航点动作id;
    long long size = 0;                        // 文件大小(字节);
    std::string object_key;                    // 文件存储对象key;
    std::string file_name;                     // 文件名;
    std::string suffix;                        // 文件后缀;
    MediaFilesListItemExtData ext;             // 扩展字段; 扩展数据(图片才会返回);
    std::string created_at;                    // 创建时间;
    std::string file_path;                     // 文件路径;
    std::string device_sn;                     ///< 设备 SN
    std::string payload_type;                  ///< 负载类型
    int type = 0;                              ///< 文件类型; 取值参见 MediaType 枚举; 媒体列表接口实际只会出现 0(未定义)/1(文件夹)/2(图片)/3(视频)

    bool operator==(const MediaFilesListItemInfoData &rhs) const {
        return
            file_id == rhs.file_id &&
            flight_id == rhs.flight_id &&
            wayline_action_uuid == rhs.wayline_action_uuid &&
            size == rhs.size &&
            object_key == rhs.object_key &&
            file_name == rhs.file_name &&
            suffix == rhs.suffix &&
            ext == rhs.ext &&
            created_at == rhs.created_at &&
            file_path == rhs.file_path &&
            device_sn == rhs.device_sn &&
            payload_type == rhs.payload_type &&
            type == rhs.type;
    }

    bool operator!=(const MediaFilesListItemInfoData &rhs) const {
        return !(*this == rhs);
    }
};


// 获取媒体文件列表数据;
class GetMediaFilesListResults {
public:
    std::vector<MediaFilesListItemInfoData> list;      // 数据列表;
    int page_size = 0;                                 // 页数据大小;
    int next_index = 0;                                // 下一页的起始索引, 如果是最后一页, 则返回-1(表示获取完毕);
    int total = 0;                                     ///< 总记录数

    bool operator==(const GetMediaFilesListResults &rhs) const {
        return
            list == rhs.list &&
            page_size == rhs.page_size &&
            next_index == rhs.next_index &&
            total == rhs.total;
    }
    
    bool operator!=(const GetMediaFilesListResults &rhs) const {
        return !(*this == rhs);
    }
};


class GetMediaFilesDownloadUrlResults {
public:
    std::string url;  // 下载地址;

    bool operator==(const GetMediaFilesDownloadUrlResults &rhs) const {
        return url == rhs.url;
    }

    bool operator!=(const GetMediaFilesDownloadUrlResults &rhs) const {
        return !(*this == rhs);
    }
};


class MediaFileDownloadInfo {
public:
    std::string object_key;
    std::string url;


    MediaFileDownloadInfo(const std::string &obj_key = "", const std::string &download_url = "")
      : object_key(obj_key), url(download_url) { }


    bool operator==(const MediaFileDownloadInfo &rhs) const {
        return (object_key == rhs.object_key) && (url == rhs.url);
    }

    bool operator!=(const MediaFileDownloadInfo &rhs) const {
        return !(*this == rhs);
    }
};

class GetMediaFilesDownloadUrlListResults {
public:
    std::vector<MediaFileDownloadInfo> list;

    bool operator==(const GetMediaFilesDownloadUrlListResults &rhs) const {
        return list == rhs.list;
    }

    bool operator!=(const GetMediaFilesDownloadUrlListResults &rhs) const {
        return !(*this == rhs);
    }
};


// "topic_media_file_device_upload";
// 设备媒体回传通知;
class MediaFileDeviceUpload {
public:
    long long file_id = -1;              // 文件 id;
    std::string flight_id;               // 飞行任务 id;
    std::string device_sn;               // 设备 SN;
    std::string object_key;              // 文件对象 key;
    long long  object_size = 0;          // 文件大小(字节);
    // 文件类型枚举值：
    // - 2  : 图片(photo)
    // - 3  : 视频(video)
    // - -1 : 未知/默认
    int file_type = -1;
    
    bool operator==(const MediaFileDeviceUpload &rhs) const {
        return
            file_id == rhs.file_id &&
            flight_id == rhs.flight_id &&
            device_sn == rhs.device_sn &&
            object_key == rhs.object_key &&
            object_size == rhs.object_size &&
            file_type == rhs.file_type;
    }

    bool operator!=(const MediaFileDeviceUpload &rhs) const {
        return !(*this == rhs);
    }
};


// 图片元数据信息;
class MediaFileImageMetaData {
public:
    std::string photo_create_time;
    double flight_yaw = -1;
    double focal_length = -1;
    double gimbal_pitch = -1;
    double gimbal_roll = -1;
    double gimbal_yaw = -1;
    int height = -1;
    int width = -1;
    double zoom_factor = -1;
    double digital_zoom_ratio = -1;
    double length_in_35mm_film = -1;  // 35mm 胶片等效焦距，示例值：50;

    std::string image_type;    // 图片类型, { 广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"PanoramaCamera"}, 空字符串表示无效;
    std::string payload_type;  // 载荷类型, 示例值: "1-98-0";
    std::string action_uuid;   // 动作唯一标识符(UUID);
    PositionData photo_position;  // 照片拍摄位置信息;
    PositionData shoot_position;  // 飞机位置信息;

    bool operator==(const MediaFileImageMetaData &rhs) const {
        return
            photo_create_time == rhs.photo_create_time &&
            floatEqual(flight_yaw, rhs.flight_yaw) &&
            floatEqual(focal_length, rhs.focal_length) &&
            floatEqual(gimbal_pitch, rhs.gimbal_pitch) &&
            floatEqual(gimbal_roll, rhs.gimbal_roll) &&
            floatEqual(gimbal_yaw, rhs.gimbal_yaw) &&
            height == rhs.height &&
            width == rhs.width &&
            floatEqual(zoom_factor, rhs.zoom_factor) &&
            floatEqual(digital_zoom_ratio, rhs.digital_zoom_ratio) &&
            floatEqual(length_in_35mm_film, rhs.length_in_35mm_film) &&
            image_type == rhs.image_type &&
            payload_type == rhs.payload_type &&
            action_uuid == rhs.action_uuid &&
            photo_position == rhs.photo_position &&
            shoot_position == rhs.shoot_position;
    }

    bool operator!=(const MediaFileImageMetaData &rhs) const {
        return !(*this == rhs);
    }
};


// 视频元数据信息;
class MediaFileVideoMetaData {
public:
    double duration = -1;           // 视频时长(单位: 秒), 示例值: 26.927, 负数表示无效值;
    std::string encoder;            // 视频编码器, 示例值：Lavf58.12.100, 空字符串表示无效值;
    std::string format_long_name;   // 视频格式名称, 示例值：QuickTime / MOV, 空字符串表示无效值;
    std::string format_name;        // 示例值: "mov,mp4,m4a,3gp,3g2,mj2", 空字符串表示无效值;
    int height = -1;                // 视频高度(单位: 像素), 示例值：1080, 负数表示无效值;
    int width = -1;                 // 视频宽度(单位: 像素), 示例值：1920, 负数表示无效值;
    
    bool operator==(const MediaFileVideoMetaData &rhs) const {
        return
            floatEqual(duration, rhs.duration) &&
            encoder == rhs.encoder &&
            format_long_name == rhs.format_long_name &&
            format_name == rhs.format_name &&
            height == rhs.height &&
            width == rhs.width;
    }

    bool operator!=(const MediaFileVideoMetaData &rhs) const {
        return !(*this == rhs);
    }
};


// "topic_media_file_post_process";
// 设备媒体后处理完成通知;
class MediaFilePostProcess {
public:
    long long file_id = -1;            // 文件 id;
    std::string flight_id;             // 飞行任务 id;
    std::string device_sn;             // 设备 SN;
    std::string object_key;            // 文件对象 key(媒体文件在对象存储中的唯一标识);
    long long object_size = 0;         // 文件大小(字节);
    int file_type = -1;                // 文件类型, { 2: 图片; 3: 视频 }; 可根据 file_type 判断文件类型, 解析和使用后续的 image_metadata 或 video_metadata;
    std::string preview_object_key;    // 预览文件对象 key(媒体文件在对象存储中的唯一标识);

    MediaFileImageMetaData image_metadata;    // 图片媒体元数据, 包含拍摄相关详细信息;
    MediaFileVideoMetaData video_metadata;    // 视频媒体元数据, 包含视频相关详细信息;

    bool operator==(const MediaFilePostProcess &rhs) const {
        return
            file_id == rhs.file_id &&
            flight_id == rhs.flight_id &&
            device_sn == rhs.device_sn &&
            object_key == rhs.object_key &&
            object_size == rhs.object_size &&
            file_type == rhs.file_type &&
            preview_object_key == rhs.preview_object_key &&
            image_metadata == rhs.image_metadata &&
            video_metadata == rhs.video_metadata;
    }

    bool operator!=(const MediaFilePostProcess &rhs) const {
        return !(*this == rhs);
    }
};


// [ATC 空管服务]
// 查询导出进度;
class GetExportFlightRecordsProgressParams {
public:
    std::string content_type;    // 导出类型(详细信息:"details", 概要信息: "summary", 空字符串空则查询全部);
    int status = -1;             // 导出状态, {正在导出: 0; 导出完成: 1; 导出失败: 2; 已取消: 3; 负数比如(-1)查询所有状态 }
    std::string sort;            // 默认(空字符串)按照时间降序，"ASC": 按照时间升序;
    int page = 1;                // page 页;
    int page_size = 50;          // page 大小;
    std::string export_id;       // 指定查询某一次导出记录, 空字符串表示不指定;


    bool operator==(const GetExportFlightRecordsProgressParams &rhs) const {
        return
            content_type == rhs.content_type &&
            status == rhs.status &&
            sort == rhs.sort &&
            page == rhs.page &&
            page_size == rhs.page_size &&
            export_id == rhs.export_id;
    }
    
    bool operator!=(const GetExportFlightRecordsProgressParams &rhs) const {
        return !(*this == rhs);
    }
};


class ExportFlightRecordsResults {
public:
    std::string export_id;

    bool operator==(const ExportFlightRecordsResults &rhs) const {
        return export_id == rhs.export_id;
    }

    bool operator!=(const ExportFlightRecordsResults &rhs) const {
        return !(*this == rhs);
    }
};



struct FlightRecordExportProgressItemData {
    long long id = -1;                   // 文件id;
    std::string export_id;               // 任务 uuid;
    std::string created_at;              // 创建时间;
    std::string content_type;            // 导出类型, 比如 "details";
    int status = -1;                     // 导出状态;
    int progress = -1;                   // 导出进度;
    int failed_reason_code = -1;         // 导出失败原因码;
    std::string object_key;              // 文件存储对象 key;
    std::string file_name;               // 文件名;
    std::string user_id;                 // 用户 id;
    std::string prj_uuid;                // 项目 uuid;
    std::vector<std::string> file_type;  // 文件类型, 比如 ["Excel"];
    std::string download_url;            // 下载地址;

    bool operator==(const FlightRecordExportProgressItemData &rhs) const {
        return
            id == rhs.id &&
            export_id == rhs.export_id &&
            created_at == rhs.created_at &&
            content_type == rhs.content_type &&
            status == rhs.status &&
            progress == rhs.progress &&
            failed_reason_code == rhs.failed_reason_code &&
            object_key == rhs.object_key &&
            file_name == rhs.file_name &&
            user_id == rhs.user_id &&
            prj_uuid == rhs.prj_uuid &&
            file_type == rhs.file_type &&
            download_url == rhs.download_url;
    }

    bool operator!=(const FlightRecordExportProgressItemData &rhs) const {
        return !(*this == rhs);
    }
};


struct GetExportFlightRecordsProgressResults {
    CommonPaginationData pagination;
    std::vector<FlightRecordExportProgressItemData> list;

    bool operator==(const GetExportFlightRecordsProgressResults &rhs) const {
        return pagination == rhs.pagination && list == rhs.list;
    }
    
    bool operator!=(const GetExportFlightRecordsProgressResults &rhs) const {
        return !(*this == rhs);
    }
};


// 导出飞行记录;
/// 导出飞行记录请求参数。
/// 必填字段：content_type, file_type, file_name, flight_task_ids
class ExportFlightRecordsRequestParams {
public:
    std::string content_type;                    ///< [必填] 导出类型。枚举："summary"=摘要, "details"=详情
    std::vector<std::string> file_type;          ///< [必填] 文件类型, 比如 ["Excel"]
    std::string file_name;                       ///< [必填] 文件名
    std::vector<std::string> flight_task_ids;    ///< [必填] 飞行任务 id 列表
    std::vector<std::string> metrics;            ///< 指标列表
    std::string time_zone;                       ///< 时区, 比如 "Asia/Shanghai"
    std::string locale;                          ///< 语言, 比如 "zh"

    bool operator==(const ExportFlightRecordsRequestParams &rhs) const {
        return
            content_type == rhs.content_type &&
            file_type == rhs.file_type &&
            file_name == rhs.file_name &&
            flight_task_ids == rhs.flight_task_ids &&
            metrics == rhs.metrics &&
            time_zone == rhs.time_zone &&
            locale == rhs.locale;
    }

    bool operator!=(const ExportFlightRecordsRequestParams &rhs) const {
        return !(*this == rhs);
    }
};



// 进离场任务状态;
// topic_task_visual_scout_status;
class AircraftVisualScoutStatus {
public:
    std::string task_id;
    int visual_scout_stage;
    long scout_wayline_generate_remain_time;

    bool operator==(const AircraftVisualScoutStatus &rhs) const {
        return
            task_id == rhs.task_id &&
            visual_scout_stage == rhs.visual_scout_stage &&
            scout_wayline_generate_remain_time == rhs.scout_wayline_generate_remain_time;
    }

    bool operator!=(const AircraftVisualScoutStatus &rhs) const {
        return !(*this == rhs);
    }
};


// 踏勘任务异常消息;
// topic_task_visual_scout_abnormal_info;
class AircraftVisualScoutAbnormalInfo {
public:
    std::string task_id;
    int err_code;

    bool operator==(const AircraftVisualScoutAbnormalInfo &rhs) const {
        return task_id == rhs.task_id && err_code == rhs.err_code;
    }

    bool operator!=(const AircraftVisualScoutAbnormalInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 障碍物过近消息;
// topic_task_visual_scout_obstacle_info
class AircraftVisualScoutObstacleInfo {
public:
    std::string task_id;
    bool obstacle_tag;

    bool operator==(const AircraftVisualScoutObstacleInfo &rhs) const {
        return task_id == rhs.task_id && obstacle_tag == rhs.obstacle_tag;
    }

    bool operator!=(const AircraftVisualScoutObstacleInfo &rhs) const {
        return !(*this == rhs);
    }
};


// Geo Caging 围栏信息（SDK 公开结构）
struct GeoCagingInfoParam {
    double fg_longitude = 0.0;   // 飞行地理围栏经度
    double fg_latitude = 0.0;    // 飞行地理围栏纬度
    int fg_radius = 0;           // 飞行地理围栏半径（米）
    int fg_height = 0;           // 飞行地理围栏高度（米）
    double cv_longitude = 0.0;   // 自定义电子围栏经度
    double cv_latitude = 0.0;    // 自定义电子围栏纬度
    int cv_radius = 0;           // 自定义电子围栏半径（米）
    int cv_height = 0;           // 自定义电子围栏高度（米）
};

// 星历下载状态;
// topic_task_ephemeris_download_state;
class EphemerisDownloadState {
public:
    int state;

    bool operator==(const EphemerisDownloadState &rhs) const {
        return state == rhs.state;
    }
    
    bool operator!=(const EphemerisDownloadState &rhs) const {
        return !(*this == rhs);
    }
};


// 星历信息;
// topic_drone_ephemeris_info;
class EphemerisInfo {
public:
    std::string ephemeris;

    bool operator==(const EphemerisInfo &rhs) const {
        return ephemeris == rhs.ephemeris;
    }

    bool operator!=(const EphemerisInfo &rhs) const {
        return !(*this == rhs);
    }
};

/// 设备影子同步单字段状态
struct FlightSafeSyncField {
    std::string field;     // 字段名，如 "rth_altitude", "out_of_control_action"
    bool synced = false;   // 该字段是否已同步到设备
};


/// 设备影子同步结果
struct FlightSafeSyncResult {
    std::string status;                          // 整体同步状态: "all_synced" / "partial_synced" / "failed"
    std::vector<FlightSafeSyncField> results;    // 各字段同步详情
};

struct FlightSmartPlanSettings {
    int smart_planning_mode;                         // 智能规划模式: 0：开启 1：关闭
};

struct FlightSmartPlanModeResult {
    FlightSmartPlanSettings settings;       // 服务端返回的实际生效设置
    FlightSafeSyncResult device_shadow_sync;  // 设备影子同步结果
};

// 失控动作回调结果
struct FlightOutOfControlSettings {
    std::string out_of_control_action;   // 失控动作: "Hover" / "ReturnHome" / "Continue"
};

struct FlightOutOfControlActionResult {
    FlightOutOfControlSettings settings;
    FlightSafeSyncResult device_shadow_sync;
};

// 飞行作业高度回调结果
struct FlightCommanderHeightSetting {
    int commander_flight_height = 0;   // 飞行作业高度（米）
};

struct FlightCommanderHeightResult {
    FlightCommanderHeightSetting settings;
    FlightSafeSyncResult device_shadow_sync;
};

// 返航高度回调结果
struct FlightRthAltitudeSetting {
    int rth_altitude = 0;   // 返航高度（米）
};

struct FlightRthAltitudeResult {
    FlightRthAltitudeSetting settings;
    FlightSafeSyncResult device_shadow_sync;
};

// 空中回传设置：接口已不再返回 data 字段，回调仅传 FlightHubError

/// HTTP/WebSocket 鉴权头键值对
struct HeaderKeyValue {
    std::string key;    ///< 头部字段名
    std::string value;  ///< 头部字段值
};

// ============================================================
// 中继站调试接口 Result 结构
// ============================================================

// 注意：设置中继广播隐藏、SSID、密码、图传搜索模式 这 4 个接口无 data 响应，
// callback 只回调 FlightHubError，不需要 Result 结构体

/// 设置中继占用状态结果（有 bid+status）
struct SetRelayOccupiedStatusResult {
    std::string status;
    std::string bid;
    bool operator==(const SetRelayOccupiedStatusResult& other) const {
        return status == other.status && bid == other.bid;
    }
};

/// 中继重启结果（有 bid+status）
struct RelayRebootResult {
    std::string status;
    std::string bid;
    bool operator==(const RelayRebootResult& other) const {
        return status == other.status && bid == other.bid;
    }
};

/// 中继断开连接结果（有 bid+status）
struct RelayDisconnectResult {
    std::string status;
    std::string bid;
    bool operator==(const RelayDisconnectResult& other) const {
        return status == other.status && bid == other.bid;
    }
};


// 不支持报修时返回的维修指导, support_repair=true 时为默认值(协议中 null object);
struct RepairGuidance {
    std::string redirect_url;  // 维修指导跳转链接，型号不支持时指向官网维修页;
    std::string description;   // 描述码：devices_repair_guidance_prefix(型号不支持), devices_repair_detail_repair_order_unfinished(有未完成订单);
};


// 获取设备报修配置;
struct DeviceRepairConfig {
    std::string device_sn;                  // 设备 SN;
    bool support_repair = true;             // 是否支持在线报修;
    std::vector<int> allowed_source_types;  // 允许的报修来源类型：0=定损 2=日志反馈 3=直接报修;
    RepairGuidance repair_guidance;         // 不支持报修时返回的维修指导, support_repair=true 时为默认值(协议中 null object);
};


struct DeviceRepairConfigResults {
    std::vector<DeviceRepairConfig> data;
};


// 获取设备报修配置 请求参数;
class GetDeviceRepairConfigParams {
public:
    std::vector<std::string> device_sns;  // 设备 SN;

    bool operator==(const GetDeviceRepairConfigParams& other) const {
        return device_sns == other.device_sns;
    }

    bool operator!=(const GetDeviceRepairConfigParams& other) const {
        return !(*this == other);
    }
};


// 创建报修订单 - 联系地址;
struct MaintenanceOrderContactAddressParams {
    std::string country;
    std::string province;
    std::string city;
    std::string district;
    std::string detail;
};

// 创建报修订单 - 设备信息;
struct MaintenanceOrderDeviceItemParams {
    std::string device_model_key;    // 设备型号 key;
    std::string device_sn;
    std::string module;              // 设备机型领域;
    std::string nameplate_sn;        // 铭牌 SN，无则传空字符串;
    int source_type = 0;             // 报修来源类型：0=定损 2=日志反馈 3=直接报修;
    std::string source_ref_id;       // 来源关联 ID, 来源关联 ID，无则传空字符串;
};

// attachments;
struct MaintenanceOrderAttachmentsParams {
    std::string file_name;      // 文件名;
    std::string file_key;       // OSS 存储路径(object key);
    long long file_size = 0;    // 文件大小(字节);
};


// 创建报修订单 - 设备拓扑节点;
struct MaintenanceOrderTopoNodeParams {
    std::string sn;
    std::string device_model_key;
    bool online_status = false;
};

// 创建报修订单 - 设备拓扑;
struct MaintenanceOrderDeviceTopoParams {
    std::vector<MaintenanceOrderTopoNodeParams> hosts;
    std::vector<MaintenanceOrderTopoNodeParams> parents;
    std::vector<MaintenanceOrderTopoNodeParams> nodes;
};


// 创建报修订单 - 日志信息;
struct MaintenanceOrderLogInfoParams {
    std::string device_sn;   // 设备 SN;
    std::string module;      // 设备模块;
    std::string object_key;  // 日志文件 OSS 路径;
};

// 创建报修订单 请求参数;
struct CreateMaintenanceOrderParams {
    std::string fault_occurrence_time;                            // 故障发生时间 fault_occurrence_time;
    std::vector<MaintenanceOrderDeviceItemParams> device_list;    // 设备列表;
    std::string fault_description;                                // 故障描述;
    std::vector<MaintenanceOrderAttachmentsParams> attachments;   // 附件列表;
    std::string contact_person;                                   // 联系人;
    std::string contact_phone;                                    // 联系电话;
    std::string contact_email;                                    // 联系邮箱;
    MaintenanceOrderContactAddressParams contact_address;         // 联系地址;
    std::string expected_service_time;                            // 期望服务时间;
    std::string report_uuid;                                      // 报告 UUID(关联的日志报告 UUID(日志反馈报修时传入));
    MaintenanceOrderDeviceTopoParams device_topo;                 // 设备拓扑;
    std::vector<MaintenanceOrderLogInfoParams> log_list;          // 日志列表;
};

// 创建报修订单 - 生成的订单信息;
struct GeneratedOrderInfoResult {
    std::string order_number;           // 订单编号;
    int status = 0;                     // 订单状态;
    std::string ruiyun_order_number;    // 瑞云订单编号(瑞云 Ruiyun 三方工单号);
};

// 创建报修订单 返回结果;
struct CreateMaintenanceOrderResults {
    bool success = false;                                      // 是否全部创建成功;
    std::vector<GeneratedOrderInfoResult> generated_orders;
};


// 查询单个报修订单详情 - 联系地址;
struct MaintenanceOrderFullContactAddress {
    std::string country;
    std::string province;
    std::string city;
    std::string district;
    std::string detail;
};

// 查询单个报修订单详情 - 状态变更历史;
struct MaintenanceOrderStatusHistory {
    int from_status = 0;
    int to_status = 0;
    std::string change_time;
    std::string operator_id;
    std::string operator_name;
    std::string remark;
};

// 查询单个报修订单详情 -- attachments;
struct MaintenanceOrderDetailAttachments {
    long long id = 0;
    std::string file_name;
    std::string object_key;  // OSS 存储路径;
    long long file_size = 0; // 文件大小(字节);
    std::string upload_time;
    std::string created_at;
};


struct MaintenanceOrderRepairInfo {
    std::string assignee;
    std::string assignee_phone;
    std::string repair_start_time;
};

struct MaintenanceOrderLogReport {
    std::string report_uuid;
    std::string track_number;
    std::string status;
};

// 查询单个报修订单详情 返回结果;
struct MaintenanceOrderDetailResults {
    std::string order_number;                // 报修订单号，格式：MO_{8位随机ID}{型号key}{毫秒时间戳};
    std::string organization_uuid;           // 所属组织 UUID;
    std::string creator_id;                  // 创建人用户 ID（UUID）;
    std::string creator_name;                // 创建人昵称;
    int source_type = 0;                     // 报修来源类型码：0=定损 2=日志反馈 3=直接报修;
    std::string source_type_text;            // 报修来源类型文本（中文）;
    int status = 0;                          // 订单状态码：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消;
    std::string status_text;                 // 订单状态文本（中文）;
    std::string fault_description;           // 故障现象描述;
    std::string fault_occurrence_time;       // 故障发生时间（RFC3339）;
    std::string expected_service_time;       // 期望上门服务时间;
    std::string contact_person;              // 联系人姓名;
    std::string contact_phone;               // 联系人电话;
    std::string contact_email;               // 联系人邮箱;
    MaintenanceOrderFullContactAddress contact_address_full;  // 联系地址;
    std::string created_at;                  // 订单创建时间;
    std::string updated_at;                  // 订单最后更新时间;
    std::string submission_time;             // 提交时间;
    std::string accepted_time;               // 受理时间;
    std::string completed_time;              // 完成时间;
    std::string ruiyun_order_number;         // 瑞云工单号;
    std::string ruiyun_sync_status;          // 瑞云同步状态：pending（待同步）/ synced（已同步）/ failed（同步失败）;
    std::string device_sn;                   // 设备激活 SN;
    std::string device_model_key;            // 设备型号 key;
    std::string module;                      // 设备机型领域（如 dock、drone）;
    std::string nameplate_sn;                // 设备铭牌 SN（整机序列号）;
    std::string diagnostic_id;               // 关联定损报告 UUID，无则为空字符串;
    std::string device_report_id;            // 关联日志报告 UUID，无则为空字符串;
    std::vector<MaintenanceOrderDetailAttachments> attachments;  // 附件信息;
    std::vector<MaintenanceOrderStatusHistory> status_history;   // 订单状态变更历史，无历史时为空数组;
    MaintenanceOrderRepairInfo repair_info;  // 维修人员信息，已指派维修人员时返回;
    MaintenanceOrderLogReport log_report;    // 关联日志报告信息，日志反馈渠道且存在 device_report_id 时返回;
};


//  (设备)报修管理模块;
// 分页查询报修订单列表请求参数;
struct GetMaintenanceOrdersListParams {
    int page = 1;  // 页码(默认 1);
    int page_size = 100;  // [1, 100];
    std::string order_number;
    std::string status; // 订单状态：0=草稿 1=提交中 2=受理中 3=已受理 4=维修处理中 5=待评价 6=已完成 7=已取消;
    std::string initiated_from;  // 来源类型：0=定损 2=日志反馈 3=直接报修;
    std::string device_sn;       // 设备 SN（精确匹配);
    std::vector<std::string> creator_ids;
    std::string key_word;        // 关键词搜索(匹配设备 SN 或组织内昵称);
    std::string created_at_start;
    std::string created_at_end;
    std::string sort_column;  // 排序字段: ["order_number", "created_at", "submission_time", "completed_time"];
    std::string sort_type;    // 排序方向, ["asc", "desc"] 默认 "desc";
};



struct MaintenanceOrdersListItem {
    std::string order_number;
    std::string ruiyun_order_number;
    int status = 0;
    std::string status_text;
    int source_type = 0;
    std::string source_type_text;
    std::string creator_id;
    std::string creator_name;
    std::string contact_person;
    std::string contact_phone;
    std::string contact_email;
    MaintenanceOrderFullContactAddress contact_address_full;
    std::string fault_description;
    std::string fault_occurrence_time;
    std::string expected_service_time;
    std::string created_at;
    std::string updated_at;
    std::string submission_time;
    std::string accepted_time;
    std::string completed_time;
    std::string device_sn;
    std::string device_model_key;
    std::string device_model_name;
    std::string nameplate_sn;
    std::string module;
    std::string device_organization_callsign;
    std::vector<MaintenanceOrderDetailAttachments> attachments;
    std::vector<MaintenanceOrderStatusHistory> status_history;
    MaintenanceOrderRepairInfo repair_info;
    MaintenanceOrderLogReport log_report;
};



struct MaintenanceOrdersListResults {
    CommonPaginationData pagination;
    std::vector<MaintenanceOrdersListItem> list;
};



// 设备定损: 拓扑节点;
struct SelfDiagnosisTopoNodeParams {
    std::string sn;
    std::string type;
    int domain = 0;
    std::string device_model;
};

// 设备定损: 设备拓扑;
struct SelfDiagnosisDeviceTopoParams {
    std::string index;
    std::vector<SelfDiagnosisTopoNodeParams> hosts;
    std::vector<SelfDiagnosisTopoNodeParams> parents;
    std::vector<SelfDiagnosisTopoNodeParams> nodes;
    std::vector<SelfDiagnosisTopoNodeParams> others;
    std::vector<SelfDiagnosisTopoNodeParams> docks;
};

// 设备定损: 下发定损指令 请求参数;
// NOTE(apifox): Apifox 仅需 device_sn, 其余字段(type/part/index/motor_action/device_topo)为 SDK 独有扩展参数
struct CreateSelfDiagnosisParams {
    std::string device_sn;       // 设备 SN;
    int type = 0;                // 定损类型;
    int part = 0;                // 定损部位;
    int index = 0;               // 索引;
    int motor_action = 0;        // 电机动作;
    SelfDiagnosisDeviceTopoParams device_topo;  // 设备拓扑;
};

// 定损操作(取消/删除) 请求参数;
struct OperateSelfDiagnosisParams {
    std::string report_uuid;         // 定损报告 UUID;
    std::string report_cmd_method;   // 操作指令: "cancel" / "delete";
};


/// 设备定损: 分页查询查询定损报告列表;
struct GetSelfDiagnosisReportListParams {
    int page = 1;                      // 页码(默认 1);
    int page_size = 20;                // 默认 20;
    std::vector<std::string> status;
    std::string device_sn;             // 设备 SN（精确匹配);
    std::string report_time_begin;     // 上报时间范围起始(Unix 秒，闭区间);
    std::string report_time_end;       // 上报时间范围结束(Unix 秒，闭区间);
    int part = 0;                      // 部件类型过滤（0=整机，10=单机业务定损，其他值由设备协议定义）;
    int origin = 0;                    // 定损来源过滤（0=FH2，1=NeptuneApp）;
    std::string query;
    std::vector<std::string> report_user;
    std::string sort_by;  //
    std::string sort_type;             // 排序方向：asc=升序，desc=降序，默认 desc;
};


// 定损报告: 错误码信息;
struct DiagnosisErrorCodeItemResult {
    std::string error_code;
    std::string error_code_describe;
    std::string error_code_describe_en;
    int level = 0;
};

// 定损报告: 错误码分级列表;
struct DiagnosisErrorCodeListResult {
    std::vector<DiagnosisErrorCodeItemResult> notice;
    std::vector<DiagnosisErrorCodeItemResult> warning;
    std::vector<DiagnosisErrorCodeItemResult> alert;
};

// 定损报告: 物料诊断结果;
struct DiagnosisMaterialItemResult {
    DiagnosisErrorCodeListResult error_code_list;
    std::string material_num;
    std::string material_name;
    std::string material_probability;
    int hms_total = 0;
};

// 定损报告: 自检诊断结果;
// 定损结果详情，按 HMS 告警等级分为 high / mid / low 三组;
struct SelfDiagnosticResultData {
    std::vector<DiagnosisMaterialItemResult> high;
    std::vector<DiagnosisMaterialItemResult> mid;
    std::vector<DiagnosisMaterialItemResult> low;
};

// 定损报告: 设备上报进度 - 子设备;
struct DiagnosisDeviceProgressItemResult {
    std::string sn;
    std::string status;
    int percent = 0;
};

// 定损报告: 设备上报进度;
struct DiagnosisDeviceReportProgressResult {
    int result = 0;
    std::string sn;
    std::string status;
    int percent = 0;
    std::vector<DiagnosisDeviceProgressItemResult> all_device;
};

// 定损报告: 查询单条定损报告详情 结果;
struct GetSelfDiagnosisReportResults {
    std::string report_uuid;                         // 定损报告唯一标识（UUID）;
    std::string report_user_id;                      // 发起定损操作的用户 ID;
    std::string report_user_organization_callsign;   // 发起人在本组织中的呼号（昵称），由 organization_member 表查询得出;
    long long create_time = 0;                       // 报告创建时间（Unix 秒时间戳）;
    std::string complete_machine_sn;                 // 整机 SN（铭牌 SN / 主控 SN）;
    std::string device_sn;
    std::string device_model_key;                    // 设备型号标识;
    std::string device_organization_callsign;        // 设备在本组织中的昵称 / 呼号，由 organization_device 表查询得出;
    std::string status;                              // 定损报告当前状态, {"pre_commit", "in_progress", "ok", "canceled", "timeout", "failed"};
    int result_code = 0;                             // 定损结果码(由设备上报)。0 表示成功, 210098 表示超时，其它值为设备定义的错误码;
    std::string bid;                                 // 批次 ID，用于关联同一批次的多条报告，也用于向 DTCP 查询实时进度;
    int part = 0;                                    // 定损部件类型。0=整机，1=电池，10=单机业务定损;
    int origin = 0;                                  // 定损来源。0=FH2，1=NeptuneApp;
    SelfDiagnosticResultData self_diagnostic_result;
    DiagnosisDeviceReportProgressResult device_report_progress;
    std::string qr_code_content;
    std::string qr_guide_text;
};

// 定损报告: 查询定损报告列表 结果;
struct GetSelfDiagnosisReportListResults {
    CommonPaginationData pagination;
    std::vector<GetSelfDiagnosisReportResults> list;
};

// 获取EA700 AI Box的设备信息，包括固件版本、存储状态等;
struct AIBoxInfoResults {
    int index = 0;                 // 设备索引;
    int type = 0;                  // 设备类型;
    std::string sn;                // 设备 SN;
    std::string firmware_version;  // 固件版本;
    double storage_total = 0.0;    // 总存储空间(GB);
    double storage_used = 0.0;     // 已使用存储空间(GB);
};

struct AIBoxInfoListResults {
    std::vector<AIBoxInfoResults> ai_boxes;
};

struct FetchFileDownloadUrlParams {
    std::string device_sn;
    std::string box_sn;
    std::vector<std::string> object_keys;
};

// 下载URL列表;
struct FileDownloadUrlListResults {
    std::vector<std::string> list;
};

struct FetchAppListParam {
    std::string device_sn;
    std::string box_sn;
};

struct FetchAppListResults {
    std::string bid;
    std::string status;
};

// 获取应用列表进度 请求参数;
struct FetchAppListProgressParam {
    std::string device_sn;  // 机场设备SN;
    std::string box_sn;     // AI Box设备SN;
    std::string bid;        // 任务 ID;
};


struct AIBoxAppInfoResults {
    std::string id;           // 应用 ID;
    std::string name;         // 应用 name;
    std::string version;      // 应用版本;
    std::string description;  // 应用描述;
    std::string publisher;    // 应用发布者;
    double app_size = 0.0;    // 应用大小(MB);
    double data_size = 0.0;   // 应用数据大小(MB);
    int type = 0;             // 应用类型：0=一方应用，1=三方应用;
};


struct FetchAppListProgressExtResults {
    std::string sn;                                    // EA700 AI Box设备SN;
    std::vector<AIBoxAppInfoResults> ai_box_app_info;  // 应用信息;
    std::string error_reason;                          // 错误原因;
};


// 获取应用列表进度结果;
struct FetchAppListProgressResults {
    std::string bid;
    std::string status;
    int percent = 0;
    long long create_time = 0;
    long long update_time = 0;
    int device_code = 0;
    FetchAppListProgressExtResults ext;
};


// 文件导出：请求参数;
struct ExportFileParam {
    std::string device_sn;    // 机场设备SN;
    std::string box_sn;       // AI-Box SN;
    std::string app_id;       // 应用ID;
    int export_type = 0;      // 导出类型：1=导出日志，2=导出应用数据文件;
};


// 导出文件: 导出操作结果数据;
struct ExportFileResults {
    std::string bid;
    std::string status;
};


// 导出文件: 导出进度请求参数;
struct ExportFileProgressParam {
    std::string device_sn;
    std::string box_sn;
    std::string bid;
};

// 导出文件: 导出进度结果数据;
struct ExportFileProgressExtResults {
    std::string sn;         // EA700 AI Box设备SN;
    int export_type = 0;    // 导出类型：1=文件，2=日志;
    std::string app_id;     // 应用ID;
    std::string object_key; // 导出完成时返回: 导出文件的object key;
};


// 导出文件: 导出进度结果数据;
struct ExportFileProgressResults {
    std::string bid;               // 任务ID;
    std::string status;            // 任务状态;
    int percent = 0;               // 任务进度(百分比, 0-100);
    long long create_time = 0;     // 任务创建时间(Unix时间戳);
    long long update_time = 0;     // 任务更新时间(Unix时间戳);
    int device_code = 0;           // 设备代码;
    ExportFileProgressExtResults ext;
};


// 上传文件参数;
struct UploadFileParam {
    std::string file_path;    // 文件路径;
    std::string device_sn;    // 设备序列号;
    std::string box_sn;       // AI Box序列号;
};

// 向EA700 AI Box下发应用安装指令;
// 安装应用参数;
struct InstallAppParam {
    std::string device_sn;    // 机场设备SN;
    std::string box_sn;       // AI Box 设备SN;
    std::string object_key;   // 应用文件的OSS对象Key;
};

// 安装应用结果数据;
struct InstallAppResults {
    std::string bid;        // 任务ID, 用于查询任务进度;
    std::string status;     // 任务状态;
};


// 查询EA700 AI Box应用安装任务进度 请求参数;
struct InstallAppProgressParam {
    std::string device_sn;  // 机场设备SN;
    std::string box_sn;     // AI Box设备SN;
    std::string bid;        // 任务ID;
};


struct InstallAppProgressExtResults {
    std::string sn;         // EA700 AI Box设备SN;
};

// 查询EA700 AI Box应用安装任务进度 结果数据;
struct InstallAppProgressResults {
    std::string bid;                    // 任务 ID;
    std::string status;                 // 任务状态;
    int percent = 0;                    // 任务进度(百分比，0-100);
    long long create_time = 0;          // 任务创建时间(Unix时间戳);
    long long update_time = 0;          // 任务更新时间(Unix时间戳);
    int device_code = 0;                // 设备代码;
    InstallAppProgressExtResults ext;
};

// 对EA700 AI Box上的应用执行卸载、启动、停止或清除操作;
// 操作应用请求参数;
struct OperateAppParam {
    std::string device_sn;  // 机场设备SN;
    std::string box_sn;     // AI Box设备SN;
    std::string app_id;     // 应用ID;
    std::string action;     // 操作类型, {"uninstall", "start", "stop", "clear"}
};


// 操作应用 结果数据;
struct OperateAppResults {
    std::string bid;      // 任务ID，用于查询任务进度;
    std::string status;   // 任务状态;
};


// 上传文件结果数据;
struct UploadFileResults {
    std::string object_url;    // 上传成功后的对象 URL;
    std::string etag;          // 上传成功后的 ETag;
    std::string object_key;    // 上传成功后的 object_key;
};


// ─────────────────────────────────────────────────────────
// 指令任务事件 (topic_atc_command_task_event)
// 指令任务的创建和更新事件。指令任务通常用于手动控制飞行操作。
// ─────────────────────────────────────────────────────────

struct CommandTaskData {
    int id = 0;                              // 数据库自增 ID
    std::string created_at;                  // 创建时间（ISO 8601 格式）
    std::string uuid;                        // 任务 UUID
    std::string prj_uuid;                    // 项目 UUID
    std::string sn;                          // 机场序列号
    std::string drone_sn;                    // 无人机序列号
    std::string track_id;                    // 轨迹 ID
    std::string user_id;                     // 用户 ID
    int status = 0;                          // 任务状态：0-待执行，1-执行中，2-已完成，3-失败，4-已取消
    int err_code = 0;                        // 错误码
    std::string takeoff_mode;                // 起飞模式："Takeoff"-直接起飞，"TakeoffWithFlyTo"-起飞后飞向目标点
    double security_takeoff_height = 0.0;    // 安全起飞高度（米）
    int max_speed = 0;                       // 最大速度（米/秒）
    int out_of_control_action = 0;           // 失控动作：1-返航，2-继续执行，3-悬停
    int rth_altitude = 0;                    // 返航高度（米）
    int commander_flight_mode = 0;           // 指点飞行模式
    double commander_flight_height = 0.0;    // 指点飞行高度（米）
    int rth_mode = 0;                        // 返航模式
    std::string cloud_to_cloud_id;           // 云云对接 ID

    bool operator==(const CommandTaskData& o) const {
        return id == o.id
            && created_at == o.created_at
            && uuid == o.uuid
            && prj_uuid == o.prj_uuid
            && sn == o.sn
            && drone_sn == o.drone_sn
            && track_id == o.track_id
            && user_id == o.user_id
            && status == o.status
            && err_code == o.err_code
            && takeoff_mode == o.takeoff_mode
            && floatEqual(security_takeoff_height, o.security_takeoff_height)
            && max_speed == o.max_speed
            && out_of_control_action == o.out_of_control_action
            && rth_altitude == o.rth_altitude
            && commander_flight_mode == o.commander_flight_mode
            && floatEqual(commander_flight_height, o.commander_flight_height)
            && rth_mode == o.rth_mode
            && cloud_to_cloud_id == o.cloud_to_cloud_id;
    }
};

struct CommandTaskEvent {
    std::string event_id;                    // 事件唯一标识 (UUID)
    std::string action;                      // 操作类型（Routing Key）："command_task_created"-指令任务创建，"command_task_updated"-指令任务更新
    std::string uuid;                        // 任务 UUID
    long long timestamp = 0;                 // 时间戳（毫秒）
    CommandTaskData task_data;               // 任务详情数据

    bool operator==(const CommandTaskEvent& o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
};

// ─────────────────────────────────────────────────────────
// 指点飞行任务事件 (topic_atc_flyto_task_event)
// 指点飞行任务的创建和更新事件。指点飞行任务用于控制无人机飞向指定的坐标点。
// ─────────────────────────────────────────────────────────

struct FlyToWayPoint {
    double latitude = 0.0;                   // 纬度
    double longitude = 0.0;                  // 经度
    double height = 0.0;                     // 高度（米）

    bool operator==(const FlyToWayPoint& o) const {
        return floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height);
    }
};

struct FlyToTaskData {
    int id = 0;                              // 数据库自增 ID
    std::string uuid;                        // 任务 UUID
    std::string prj_uuid;                    // 项目 UUID
    std::string sn;                          // 机场序列号
    std::string drone_sn;                    // 无人机序列号
    std::string user_id;                     // 用户 ID
    int seq_no = 0;                          // 序列号
    std::string flight_task_uuid;            // 关联的飞行任务 UUID
    int flight_task_type = 0;                // 飞行任务类型：1-航线任务，2-指令任务
    int status = 0;                          // 任务状态：0-待执行，1-执行中，2-成功，3-失败，4-取消，5-超时
    int err_code = 0;                        // 错误码
    int way_point_index = 0;                 // 当前航点索引
    std::vector<FlyToWayPoint> way_points;   // 航点列表
    std::vector<FlyToWayPoint> planned_path_points; // 规划路径点
    double remaining_distance = 0.0;         // 剩余距离（米）
    double remaining_time = 0.0;             // 剩余时间（秒）

    bool operator==(const FlyToTaskData& o) const {
        return id == o.id
            && uuid == o.uuid
            && prj_uuid == o.prj_uuid
            && sn == o.sn
            && drone_sn == o.drone_sn
            && user_id == o.user_id
            && seq_no == o.seq_no
            && flight_task_uuid == o.flight_task_uuid
            && flight_task_type == o.flight_task_type
            && status == o.status
            && err_code == o.err_code
            && way_point_index == o.way_point_index
            && way_points == o.way_points
            && planned_path_points == o.planned_path_points
            && floatEqual(remaining_distance, o.remaining_distance)
            && floatEqual(remaining_time, o.remaining_time);
    }
};

struct FlyToTaskEvent {
    std::string event_id;                    // 事件唯一标识 (UUID)
    std::string action;                      // 操作类型，对应 Routing Key
    std::string uuid;                        // 任务 UUID
    long long timestamp = 0;                 // 时间戳（毫秒）
    FlyToTaskData task_data;                 // 任务详情数据

    bool operator==(const FlyToTaskEvent& o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
};

// ─────────────────────────────────────────────────────────
// POI 任务事件 (topic_atc_poi_task_event)
// POI（Point of Interest，兴趣点）任务的创建和更新事件。POI 任务通常用于环绕拍摄等场景。
// ─────────────────────────────────────────────────────────

struct PoiCenterPoint {
    double latitude = 0.0;                   // 纬度
    double longitude = 0.0;                  // 经度
    double height = 0.0;                     // 高度（椭球高，米）

    bool operator==(const PoiCenterPoint& o) const {
        return floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height);
    }
};

struct PoiTaskData {
    std::string uuid;                        // 任务 UUID
    std::string sn;                          // 机场序列号
    std::string device_sn;                   // 设备（无人机）序列号
    PoiCenterPoint poi_center_point;         // POI 中心点坐标
    double speed = 0.0;                      // 飞行速度（米/秒）
    double circle_radius = 0.0;              // 环绕半径（米）
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int status = 0;                          // 任务状态
    int err_code = 0;                        // 错误码

    bool operator==(const PoiTaskData& o) const {
        return uuid == o.uuid
            && sn == o.sn
            && device_sn == o.device_sn
            && poi_center_point == o.poi_center_point
            && floatEqual(speed, o.speed)
            && floatEqual(circle_radius, o.circle_radius)
            && payload_index == o.payload_index
            && status == o.status
            && err_code == o.err_code;
    }
};

struct PoiTaskEvent {
    std::string event_id;                    // 事件唯一标识 (UUID)
    std::string action;                      // 操作类型，对应 Routing Key
    std::string uuid;                        // 任务 UUID
    long long timestamp = 0;                 // 时间戳（毫秒）
    PoiTaskData task_data;                   // 任务详情数据

    bool operator==(const PoiTaskEvent& o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
};

// ─────────────────────────────────────────────────────────
// 航线任务事件 (topic_atc_wayline_task_event)
// 航线任务的各种事件，包括创建、状态变更、属性更新、删除和避障信息变更等。
// 航线任务是最常用的自动飞行任务类型。
// ─────────────────────────────────────────────────────────

struct WaylineTaskData {
    std::string name;                        // 任务名称
    std::string uuid;                        // 任务 UUID
    int task_type = 0;                       // 任务类型：1-立即任务，2-条件任务
    int status = 0;                          // 任务状态：0-待开始，1-启动失败，2-执行中，3-已暂停，4-已终止，5-成功，6-挂起，7-超时，8-部分执行，9-准备中，10-起飞排队中
    std::string sn;                          // 执行任务的设备序列号
    std::string landing_dock_sn;             // 降落机场序列号（为空时与起飞机场一致）
    std::string wayline_uuid;                // 航线 UUID
    int wayline_precision_type = 0;          // 航线精度类型：0-GPS，1-RTK
    double wayline_duration = 0.0;           // 任务预估时长（秒）
    std::vector<int> wayline_segment_indexes; // 执行航段索引数组
    int total_waypoints = 0;                 // 飞行任务总航点数
    int current_waypoint_index = 0;          // 已飞完的航点索引
    std::string bid;                         // DTCP 返回的 bid
    std::string begin_at;                    // 设定的开始时间（ISO 8601 格式）
    std::string end_at;                      // 设定的结束时间（ISO 8601 格式）
    std::string run_at;                      // 任务实际执行时间（ISO 8601 格式）
    std::string completed_at;                // 任务实际结束或完成时间（ISO 8601 格式）
    int out_of_control_action_in_flight = 0; // 失控动作：1-返航，2-继续执行
    int rth_altitude = 0;                    // 返航高度（米）
    int rth_mode = 0;                        // 返航高度模式：0-智能高度，1-设定高度
    int min_battery_capacity = 0;            // 任务执行最低电量（50-100）
    std::vector<std::string> tags;           // 云云对接标签
    std::string cloud_to_cloud_id;           // 云云对接存储桶 ID（为空表示非云云对接任务）
    WaylineBreakPoint break_point;           // 断点信息
    bool is_break_point_resume = false;      // 是否为断点续飞任务
    int media_upload_status = 0;             // 媒体上传状态：0-待上传，1-上传中，2-上传结束
    bool fast_reconstruction = false;        // 是否有快速建模任务
    int device_type = 0;                     // 设备类型：0-机场，1-遥控器
    std::vector<WaylineObstacle> obstacles;  // 避障信息列表
    std::string created_at;                  // 创建时间（ISO 8601 格式）
    std::string updated_at;                  // 更新时间（ISO 8601 格式）
    std::string deleted_at;                  // 删除时间（ISO 8601 格式）

    bool operator==(const WaylineTaskData& o) const {
        return name == o.name
            && uuid == o.uuid
            && task_type == o.task_type
            && status == o.status
            && sn == o.sn
            && landing_dock_sn == o.landing_dock_sn
            && wayline_uuid == o.wayline_uuid
            && wayline_precision_type == o.wayline_precision_type
            && floatEqual(wayline_duration, o.wayline_duration)
            && wayline_segment_indexes == o.wayline_segment_indexes
            && total_waypoints == o.total_waypoints
            && current_waypoint_index == o.current_waypoint_index
            && bid == o.bid
            && begin_at == o.begin_at
            && end_at == o.end_at
            && run_at == o.run_at
            && completed_at == o.completed_at
            && out_of_control_action_in_flight == o.out_of_control_action_in_flight
            && rth_altitude == o.rth_altitude
            && rth_mode == o.rth_mode
            && min_battery_capacity == o.min_battery_capacity
            && tags == o.tags
            && cloud_to_cloud_id == o.cloud_to_cloud_id
            && break_point == o.break_point
            && is_break_point_resume == o.is_break_point_resume
            && media_upload_status == o.media_upload_status
            && fast_reconstruction == o.fast_reconstruction
            && device_type == o.device_type
            && obstacles == o.obstacles
            && created_at == o.created_at
            && updated_at == o.updated_at
            && deleted_at == o.deleted_at;
    }
};

struct WaylineTaskEvent {
    std::string event_id;                    // 事件唯一标识 (UUID)
    // 操作类型枚举：
    // - "wayline_task_created"          : 任务创建
    // - "wayline_task_attr_updated"     : 任务属性更新
    // - "wayline_task_deleted"          : 任务删除
    // - "wayline_task_obstracle_changed": 障碍物变更
    // - "wayline_task_execute_output"   : 任务执行输出
    std::string action;
    std::string uuid;                        // 任务 UUID
    long long timestamp = 0;                 // 时间戳（毫秒）
    WaylineTaskData task_data;               // 任务详情数据

    bool operator==(const WaylineTaskEvent& o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
};

// ─────────────────────────────────────────────────────────
// 空中下发航线（HTTP 创建/控制/查询 + WS 进度推送）
// ─────────────────────────────────────────────────────────

/// 空中航线控制动作
enum class InFlightWaylineControlAction {
    Pause = 1,   ///< 暂停
    Resume = 2,  ///< 恢复
    Cancel = 3,  ///< 取消
};

/// 创建空中航线任务参数（不含 drone_sn / gateway_sn，由 Module 注入）
struct CreateInFlightWaylineTaskParams {
    std::string wayline_uuid;             ///< 航线 UUID（必填）
    int rth_altitude = 0;                 ///< 返航高度(米)（必填，飞控验证要求≥2）
    int wayline_precision_type = -1;      ///< 航线精度类型。-1 表示未设置
    int rth_mode = -1;                    ///< 返航高度模式。-1 表示未设置
    int out_of_control_action = -1;       ///< 飞控失控动作: 0-返航 1-悬停 2-降落。-1 表示未设置
    int exit_wayline_when_rc_lost = -1;   ///< 航线失控动作: 0-继续执行 1-执行飞控失控动作。-1 表示未设置
    double wayline_duration = -1.0;     ///< 任务预估时长。-1 表示未设置
    double wayline_length = -1.0;       ///< 航线长度。-1 表示未设置
    std::string task_name;                ///< 任务名称。空串表示未设置
    int fast_reconstruction = -1;         ///< 快速建模。-1 未设置；0=false；1=true
};

/// 创建空中航线任务结果
struct CreateInFlightWaylineTaskResult {
    std::string flight_task_id;            ///< 飞行主任务 ID
    std::string in_flight_wayline_task_id; ///< 空中航线子任务 ID
};

/// 空中航线子任务详情
struct InFlightWaylineTaskDetail {
    std::string uuid;              ///< 空中航线任务 UUID
    std::string user_id;           ///< 创建用户 ID
    int status = 0;                ///< 任务状态
    std::string wayline_id;        ///< 航线 ID
    int err_code = 0;              ///< 错误码
    int way_point_index = 0;       ///< 当前航点索引
    int progress_percent = 0;      ///< 执行进度百分比
    double wayline_duration = 0.0; ///< 任务预估时长
    double wayline_length = 0.0;   ///< 航线长度
    std::string task_name;         ///< 任务名称
};

/// 活跃空中航线任务查询结果
struct ActiveInFlightWaylineTaskInfo {
    std::string flight_id;                 ///< 飞行主任务 ID
    int flight_type = 0;                   ///< 飞行任务类型
    std::string sn;                        ///< 设备 SN
    InFlightWaylineTaskDetail in_flight_task; ///< 空中航线子任务详情
    std::string flight_task_data;          ///< 飞行任务扩展数据（JSON）
};

/// 空中航线任务进度推送（WS topic: in_flight_wayline_task_notify）
struct InFlightWaylineProgress {
    std::string in_flight_wayline_id; ///< 空中航线任务 ID
    int percent = 0;                  ///< 执行进度百分比(0-100)
    int status = 0;                   ///< 任务状态
    int result = 0;                   ///< 执行结果
    std::string gateway_sn;           ///< 网关 SN
    int way_point_index = 0;          ///< 当前航点索引
    double wayline_duration = 0.0;    ///< 任务预估时长
    double wayline_length = 0.0;      ///< 航线长度
    std::string task_name;            ///< 任务名称
    long timestamp = 0;               ///< 时间戳(秒)。勿用 int64_t（SWIG 无法映射到 Java）

    bool operator==(const InFlightWaylineProgress& o) const {
        return in_flight_wayline_id == o.in_flight_wayline_id
            && percent == o.percent
            && status == o.status
            && result == o.result
            && gateway_sn == o.gateway_sn
            && way_point_index == o.way_point_index
            && floatEqual(wayline_duration, o.wayline_duration)
            && floatEqual(wayline_length, o.wayline_length)
            && task_name == o.task_name
            && timestamp == o.timestamp;
    }
};

// ─────────────────────────────────────────────────────────
// 固件升级进度 (topic_device_ota_progress)
// ─────────────────────────────────────────────────────────

struct DeviceOtaProgressInfo {
    int percent = 0;                         // 进度百分比
    int current_step = 0;                    // 当前步骤
    std::string ext;                         // 扩展信息

    bool operator==(const DeviceOtaProgressInfo &o) const {
        return percent == o.percent && current_step == o.current_step && ext == o.ext;
    }
};

struct DeviceOtaProgress {
    std::string bid;                         // 业务 ID
    long long timestamp = 0;                 // 时间戳
    std::string status;                      // 状态
    int result = 0;                          // 结果码
    DeviceOtaProgressInfo progress;          // 进度信息

    bool operator==(const DeviceOtaProgress &o) const {
        return bid == o.bid && timestamp == o.timestamp && status == o.status
            && result == o.result && progress == o.progress;
    }
};

// ─────────────────────────────────────────────────────────
// 设备异常报告上传进度 (topic_device_fileupload_progress)
// ─────────────────────────────────────────────────────────

struct DeviceFileUploadFileProgress {
    int percent = 0;                         // 进度百分比
    int current_step = 0;                    // 当前步骤
    std::string status;                      // 状态
    int total_step = 0;                      // 总步骤数
    long upload_rate = 0;                    // 上传速率（字节/秒）

    bool operator==(const DeviceFileUploadFileProgress &o) const {
        return percent == o.percent && current_step == o.current_step
            && status == o.status && total_step == o.total_step
            && upload_rate == o.upload_rate;
    }
};

struct DeviceFileUploadFileInfo {
    std::string device_sn;                   // 设备序列号
    std::string key;                         // 文件对象存储 key
    std::string module;                      // 模块标识
    DeviceFileUploadFileProgress progress;   // 文件上传进度
    long size = 0;                           // 文件大小（字节）

    bool operator==(const DeviceFileUploadFileInfo &o) const {
        return device_sn == o.device_sn && key == o.key && module == o.module
            && progress == o.progress && size == o.size;
    }
};

struct DeviceFileUploadProgress {
    std::string bid;                         // 业务 ID
    long long timestamp = 0;                 // 时间戳
    std::string status;                      // 状态
    int result = 0;                          // 结果码
    std::vector<DeviceFileUploadFileInfo> files; // 文件上传信息列表

    bool operator==(const DeviceFileUploadProgress &o) const {
        return bid == o.bid && timestamp == o.timestamp && status == o.status
            && result == o.result && files == o.files;
    }
};

// ─────────────────────────────────────────────────────────
// PSDK喊话器播放进度
// (topic_psdk_speaker_audio_play_start_progress)
// (topic_psdk_speaker_tts_play_start_progress)
// ─────────────────────────────────────────────────────────

struct PsdkSpeakerPlayProgressDetail {
    int percent = 0;                         // 进度百分比
    std::string step_key;                    // 当前阶段标识

    bool operator==(const PsdkSpeakerPlayProgressDetail &o) const {
        return percent == o.percent && step_key == o.step_key;
    }
};

struct PsdkSpeakerOutputDetail {
    std::string md5;                         // 输出内容 MD5
    PsdkSpeakerPlayProgressDetail progress;  // 进度信息
    int psdk_index = 0;                      // PSDK 索引
    std::string status;                      // 任务状态

    bool operator==(const PsdkSpeakerOutputDetail &o) const {
        return md5 == o.md5 && progress == o.progress
            && psdk_index == o.psdk_index && status == o.status;
    }
};

struct PsdkSpeakerPlayStartProgress {
    std::string gateway;                     // 网关设备标识
    PsdkSpeakerOutputDetail output;          // 输出详情
    int result = 0;                          // 结果码
    std::string sn;                          // 设备序列号

    bool operator==(const PsdkSpeakerPlayStartProgress &o) const {
        return gateway == o.gateway && output == o.output
            && result == o.result && sn == o.sn;
    }
};

// ─────────────────────────────────────────────────────────
// POI飞行进度通知 (topic_task_flight_poi_progress)
// ─────────────────────────────────────────────────────────

struct FlightPoiProgressCenterPoint {
    double latitude = 0.0;                   // 纬度
    double longitude = 0.0;                  // 经度
    double height = 0.0;                     // 高度

    bool operator==(const FlightPoiProgressCenterPoint &o) const {
        return floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height);
    }
};

struct FlightPoiProgress {
    std::string uuid;                        // 任务 UUID
    std::string sn;                          // 机场序列号
    std::string device_sn;                   // 设备（无人机）序列号
    FlightPoiProgressCenterPoint poi_center_point; // POI 中心点
    double speed = 0.0;                      // 飞行速度（m/s）
    double circle_radius = 0.0;              // 环绕半径（m）
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int status = 0;                          // 任务状态
    int err_code = 0;                        // 错误码

    bool operator==(const FlightPoiProgress &o) const {
        return uuid == o.uuid && sn == o.sn && device_sn == o.device_sn
            && poi_center_point == o.poi_center_point
            && floatEqual(speed, o.speed)
            && floatEqual(circle_radius, o.circle_radius)
            && payload_index == o.payload_index
            && status == o.status && err_code == o.err_code;
    }
};

// ─────────────────────────────────────────────────────────
// AI Alarm 相关结构体
// ─────────────────────────────────────────────────────────

struct AiAlarmTarget {
    int target_type = 0;                     // 告警目标类型: 0 人, 1 车, 2 船, 3 绝对温度点, 4 移动目标, 128~137 自定义
    double target_value = 0.0;               // 告警目标数值
    bool use_min_threshold = false;          // 是否使用 target_min_threshold
    bool use_max_threshold = false;          // 是否使用 target_max_threshold
    double target_min_threshold = 0.0;       // 告警目标最小阈值
    double target_max_threshold = 0.0;       // 告警目标最大阈值
    bool operator==(const AiAlarmTarget& o) const {
        return target_type == o.target_type
            && floatEqual(target_value, o.target_value)
            && use_min_threshold == o.use_min_threshold
            && use_max_threshold == o.use_max_threshold
            && floatEqual(target_min_threshold, o.target_min_threshold)
            && floatEqual(target_max_threshold, o.target_max_threshold);
    }
};

struct AiAlarmStreamInputConf {
    int stream_source = 0;                   // 码流编号
    int model_idx = 0;                       // 模型编号
    int algorithm_type = 0;                  // 告警算法类型: 1 DJI可见光人车船, 2 DJI红外人车船, 3 DJI红外温度识别, 5 三方红外AI, 255 三方算法
    int alarm_interval = 0;                  // 告警间隔(秒)
    std::vector<AiAlarmTarget> alarm_targets; // 告警目标数组
    bool operator==(const AiAlarmStreamInputConf& o) const {
        return stream_source == o.stream_source
            && model_idx == o.model_idx
            && algorithm_type == o.algorithm_type
            && alarm_interval == o.alarm_interval
            && alarm_targets == o.alarm_targets;
    }
};

struct AiAlarmStartParams {
    std::vector<AiAlarmStreamInputConf> stream_input_confs; // 手飞多码流告警配置数组
};

struct AiAlarmStateInfo {
    bool alarm_on = false;                   // 告警是否开启
    std::vector<AiAlarmStreamInputConf> stream_input_confs; // 手飞多码流告警当前生效配置数组
    bool operator==(const AiAlarmStateInfo& o) const {
        return alarm_on == o.alarm_on
            && stream_input_confs == o.stream_input_confs;
    }
};

// ─────────────────────────────────────────────────────────
// Flight Control Auxiliary 相关结构体
// ─────────────────────────────────────────────────────────

struct FlashLedPatternInfo {
    int color = 0;                           // 颜色 0:关灯 1:红 2:绿 3:蓝 4:黄 5:白
    int period = 0;                          // 周期(最小控灯周期50ms，输入1为50ms，2为100ms)
    int brightness = 0;                      // 亮度(0-100百分比)
    bool operator==(const FlashLedPatternInfo& o) const {
        return color == o.color && period == o.period && brightness == o.brightness;
    }
};

struct FlashLedParams {
    int on = 0;                              // 开关状态 0:关闭 1:开启
    std::string pattern_uuid;                // pattern的uuid
    std::string pattern_name;                // pattern的名字
    std::vector<FlashLedPatternInfo> patterns; // 爆闪pattern信息数组
};

/// DRC 北向 flash_led 状态（与 FlashLedParams 字段对齐，供 listenDrcFlashLed）
struct FlashLedStateInfo {
    int on = 0;
    std::string pattern_uuid;
    std::string pattern_name;
    std::vector<FlashLedPatternInfo> patterns;
    bool operator==(const FlashLedStateInfo& o) const {
        return on == o.on
            && pattern_uuid == o.pattern_uuid
            && pattern_name == o.pattern_name
            && patterns == o.patterns;
    }
};

/// HTTP flash-configs 模板片段（on/off/interval 单位 ms）
struct FlashLedSegment {
    int color = 0;       // 0关 1红 2绿 3蓝 4黄 5白
    int count = 1;       // 闪烁次数 1..99
    int on = 0;          // 亮灯时长 ms
    int off = 0;         // 灭灯时长 ms
    int interval = 0;    // 组间停顿 ms
};

/// HTTP flash-configs 创建/更新参数
struct FlashLedConfigParams {
    std::string name;
    std::vector<FlashLedSegment> flash_segments;
};

/// HTTP flash-configs 配置项
struct FlashLedConfig {
    std::string id;
    std::string name;
    std::vector<FlashLedSegment> flash_segments;
};

struct FlashLedConfigList {
    std::vector<FlashLedConfig> configs;
};

struct CameraLookAtParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    bool locked = false;                     // 是否锁定 (false关闭 true开启)
    double height = 0.0;                     // 高度值
    double latitude = 0.0;                   // 纬度
    double longitude = 0.0;                  // 经度
};

struct DrcOsdInfo {
    double attitude_head = 0.0;              // 姿态航向角
    double height = 0.0;                     // 高度
    double latitude = 0.0;                   // 纬度
    double longitude = 0.0;                  // 经度
    double speed_x = 0.0;                    // X方向速度
    double speed_y = 0.0;                    // Y方向速度
    double speed_z = 0.0;                    // Z方向速度
    int ultrasonic_height = 0;               // 超声波高度
    double horizontal_speed = 0.0;           // 水平速度
    double vertical_speed = 0.0;             // 垂直速度
    double home_distance = 0.0;              // 返航距离
    int wind_direction = 0;                  // 风向
    int wind_speed = 0;                      // 风速
    double elevation = 0.0;                  // 飞行器相对起飞点高度
    double gimbal_pitch = 0.0;               // 云台俯仰角
    double gimbal_yaw = 0.0;                 // 云台偏航角
    double gimbal_roll = 0.0;                // 云台横滚角
    bool operator==(const DrcOsdInfo& o) const {
        return floatEqual(attitude_head, o.attitude_head)
            && floatEqual(height, o.height)
            && floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(speed_x, o.speed_x)
            && floatEqual(speed_y, o.speed_y)
            && floatEqual(speed_z, o.speed_z)
            && ultrasonic_height == o.ultrasonic_height
            && floatEqual(horizontal_speed, o.horizontal_speed)
            && floatEqual(vertical_speed, o.vertical_speed)
            && floatEqual(home_distance, o.home_distance)
            && wind_direction == o.wind_direction
            && wind_speed == o.wind_speed
            && floatEqual(elevation, o.elevation)
            && floatEqual(gimbal_pitch, o.gimbal_pitch)
            && floatEqual(gimbal_yaw, o.gimbal_yaw)
            && floatEqual(gimbal_roll, o.gimbal_roll);
    }
};

struct DrcBatteryItem {
    int capacity_percent = 0;                // 容量百分比
    std::string firmware_version;            // 固件版本
    int high_voltage_storage_days = 0;       // 高压存储天数
    int index = 0;                           // 索引
    int loop_times = 0;                      // 循环次数
    std::string sn;                          // 序列号
    double temperature = 0.0;                // 温度
    int voltage = 0;                         // 电压
    bool operator==(const DrcBatteryItem& o) const {
        return capacity_percent == o.capacity_percent
            && firmware_version == o.firmware_version
            && high_voltage_storage_days == o.high_voltage_storage_days
            && index == o.index
            && loop_times == o.loop_times
            && sn == o.sn
            && floatEqual(temperature, o.temperature)
            && voltage == o.voltage;
    }
};

struct DrcBatteriesInfo {
    std::vector<DrcBatteryItem> batteries;   // 电池列表
    int capacity_percent = 0;                // 总电池容量百分比
    int landing_power = 0;                   // 降落电量
    int remain_flight_time = 0;              // 剩余飞行时间(秒)
    int return_home_power = 0;               // 返航电量
    int remain_job_time = 0;                 // 剩余工作时间(秒)
    int low_battery_warning_threshold = 0;   // 低电量告警阈值
    int serious_low_battery_warning_threshold = 0; // 严重低电量告警阈值
    bool operator==(const DrcBatteriesInfo& o) const {
        return batteries == o.batteries
            && capacity_percent == o.capacity_percent
            && landing_power == o.landing_power
            && remain_flight_time == o.remain_flight_time
            && return_home_power == o.return_home_power
            && remain_job_time == o.remain_job_time
            && low_battery_warning_threshold == o.low_battery_warning_threshold
            && serious_low_battery_warning_threshold == o.serious_low_battery_warning_threshold;
    }
};

struct DrcParachuteInfo {
    std::string parachute_firmware_version;   // 降落伞固件版本
    int parachute_is_fired = 0;              // 是否已弹出
    int parachute_on_fire_status = 0;        // 弹出状态
    int parachute_type = 0;                  // 降落伞类型
    std::string parachute_sn;                // 降落伞序列号
    int parachute_is_abnormal = 0;           // 是否异常
    bool operator==(const DrcParachuteInfo& o) const {
        return parachute_firmware_version == o.parachute_firmware_version
            && parachute_is_fired == o.parachute_is_fired
            && parachute_on_fire_status == o.parachute_on_fire_status
            && parachute_type == o.parachute_type
            && parachute_sn == o.parachute_sn
            && parachute_is_abnormal == o.parachute_is_abnormal;
    }
};

struct RtcQualityTestParams {
    int index = 0;                           // 数据编号
    int length = 0;                          // 消息数据长度
    std::string data;                        // 消息数据内容
    int diff_time = 0;                       // 每包数据时间间隔
    double timestamp = 0.0;                  // 毫秒时间戳
    bool is_need_ack = false;                // 是否需要ack
    bool is_test_end = false;                // 是否测试结束
};

struct DrcDelayInfo {
    int stick_delay = 0;                     // 摇杆延迟(ms)
    int sdr_cmd_delay = 0;                   // SDR命令延迟(ms)
    bool operator==(const DrcDelayInfo& o) const { return stick_delay == o.stick_delay && sdr_cmd_delay == o.sdr_cmd_delay; }
};


struct DrcCountDownInfo {
    int count_down_type = 0;                 // 倒计时类型
    int count_down_time = 0;                 // 倒计时时间
    bool can_cancel = false;                 // 是否可取消
    bool operator==(const DrcCountDownInfo& o) const { return count_down_type == o.count_down_type && count_down_time == o.count_down_time && can_cancel == o.can_cancel; }
};

struct CameraWatermarkParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int global_enable = 0;                   // 全局启用
    int drone_type_enable = 0;               // 无人机类型启用
    int manufacturer_enable = 0;             // 制造商启用
    int drone_sn_enable = 0;                 // 无人机序列号启用
    int datetime_enable = 0;                 // 日期时间启用
    int gps_enable = 0;                      // GPS启用
    int user_custom_string_enable = 0;       // 用户自定义字符串启用
    std::string user_custom_string;          // 用户自定义字符串
    int layout = 0;                          // 布局
};

struct IsothermPolygonPoint {
    double x = 0.0;                          // 顶点横坐标（取值范围：0~10000，精度4）
    double y = 0.0;                          // 顶点纵坐标（取值范围：0~10000，精度4）
};

// ─────────────────────────────────────────────────────────────────────────────
// DRC 高级相机控制 Params 结构体
// ─────────────────────────────────────────────────────────────────────────────

struct CameraApertureValueSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int aperture_value = 0;                 ///< 光圈值（如 280 = F2.8）
};

struct CameraExposureModeSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int exposure_mode = 0;                  ///< 曝光模式（0=自动 1=快门优先 2=光圈优先 3=手动）
};

struct CameraExposureSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int exposure_value = 0;                 ///< 曝光补偿值
};

struct CameraFocusModeSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int focus_mode = 0;                     ///< 对焦模式（0=MF 1=AFC 2=AFS）
};

struct CameraFocusValueSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int focus_value = 0;                    ///< 对焦值
};

struct CameraIsoSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int iso_value = 0;                      ///< ISO 值（如 100 / 200 / 400）
};

struct CameraMechanicalShutterSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int state = 0;                          ///< 机械快门状态（0=关 1=开）
};

struct CameraPointFocusActionParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    double x = 0.0;                         ///< 对焦点 X 坐标（0.0~1.0）
    double y = 0.0;                         ///< 对焦点 Y 坐标（0.0~1.0）
};

struct CameraShutterSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int shutter_value = 0;                  ///< 快门值（如 500 = 1/500s）
};

struct CameraDewarpingSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int dewarping_state = 0;               ///< 畸变矫正状态（0=关 1=开）
};

struct CameraScreenSplitParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    bool enable = false;                    ///< 分屏开关（false=关 true=开）
};

struct CleanFogParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
};

// ─────────────────────────────────────────────────────────────────────────────
// DRC 红外/热成像 Params 结构体
// ─────────────────────────────────────────────────────────────────────────────

struct ThermalCurrentPaletteSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int palette_style = 0;                  ///< 调色板样式。枚举：0=白热(WHITE_HOT), 1=黑热(BLACK_HOT), 2=红热(RED_HOT), 3=绿热(GREEN_HOT), 4=融合(FUSION), 5=彩虹(RAINBOW), 6=热铁1(IRONBOW1), 7=热铁2(IRONBOW2), 8=冰火(ICE_FIRE), 9=复古(SEPIA), 10=辉光(GLOWBOW), 11=色彩1(COLOR1), 12=色彩2(COLOR2), 13=雨(RAIN), 14=热点(HOT_SPOT), 15=彩虹2(RAINBOW2), 16=灰度(GRAY), 17=金属(METAL), 18=冷点(COLD_SPOT)
};

struct ThermalSuperResolutionEnableParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int enable = 0;                         ///< 超分辨率状态（0=关 1=开）
};

struct IrMeteringModeSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int mode = 0;                           ///< 测温模式（0=关闭 1=点测温 2=区域测温）
};

struct IrMeteringPointSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double x = 0.0;                         ///< 测温点X坐标（归一化0~1）
    double y = 0.0;                         ///< 测温点Y坐标（归一化0~1）
};

struct IrMeteringAreaSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    double x = 0.0;                         ///< 测温区域左上角X坐标（归一化0~1）
    double y = 0.0;                         ///< 测温区域左上角Y坐标（归一化0~1）
    double width = 0.0;                     ///< 测温区域宽度（归一化0~1）
    double height = 0.0;                    ///< 测温区域高度（归一化0~1）
};

struct SetIrDisplayModeParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int display_mode = 0;                   ///< 显示模式（1=纯红外 2=PIP 3=融合）
};

struct SetIrEnhanceModeParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int enhance_mode = 0;                   ///< 增强模式（0=关闭 1=边缘增强 2=热源回帖）
};

struct SetIrMixRateParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int ir_mix_rate = 0;                    ///< 红外可见光融合比例（0~100）
};

struct SetIsothermEnableParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int enable = 0;                         ///< 等温线开关（0=关 1=开）
};

struct SetIsothermColorRenderingTypeParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int type = 0;                           ///< 着色类型（0=保留 1=聚焦显色 2=边界警示 3=全域自适应）
};

struct SetIsothermLowerUpperRangeParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int lower = 0;                          ///< 等温线下限温度
    int upper = 0;                          ///< 等温线上限温度
};

struct SetIsothermRenderingPolygonParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    std::vector<IsothermPolygonPoint> polygon; ///< 多边形区域顶点坐标列表
};

struct SetIsothermRoiBasedRenderingEnableParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int enable = 0;                         ///< ROI 渲染使能（0=关 1=开）
};

struct InfraredFillLightEnableParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    bool enable = false;                    ///< 近红外补光使能（false=关 true=开）
};

struct CameraNightVisionEnableParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    bool enable = false;                    ///< 黑白夜视使能（false=关 true=开）
};

struct CameraSettingsResetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
};

// ─────────────────────────────────────────────────────────────────────────────
// DRC 其他相机 Params 结构体
// ─────────────────────────────────────────────────────────────────────────────

struct CameraDenoiseLevelSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int level = 0;                          ///< 降噪等级（0=关 1=标准 2=增强 3=超强）
};

struct VideoResolutionSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int camera_lens_type = 0;               ///< 相机类型（镜头类型）
    int video_resolution = 0;               ///< 视频分辨率
};

struct CameraPhotoFormatSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int photo_format = 0;                   ///< 照片格式（1=JPEG 16=DLT664）
};

struct PreRecordedStatusSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int status = 0;                         ///< 预录制状态（0=关 1=开）
};

struct PreRecordedTimeSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int pre_recorded_time = 0;              ///< 预录制时长（秒）
};

struct AlgorithmDehazeModeSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int mode = 0;                           ///< 去雾模式（0=关闭 1=使能 2=自动 255=未知）
};

struct AlgorithmDehazeLevelSetParams {
    std::string payload_index;               ///< 负载索引，格式为 {type-subtype-gimbalindex}，例如 "81-0-0"
    int level = 0;                          ///< 去雾等级（1~4 级，255=未知）
};

// ─────────────────────────────────────────────────────────────────────────────
// DRC AI 跟踪 Params 结构体
// ─────────────────────────────────────────────────────────────────────────────

struct AiSpotlightZoomSelectParams {
    double center_x = 0.0;                  ///< 目标框中心 X 坐标（0.0~1.0）
    double center_y = 0.0;                  ///< 目标框中心 Y 坐标（0.0~1.0）
    double width = 0.0;                     ///< 目标框宽度（0.0~1.0）
    double height = 0.0;                    ///< 目标框高度（0.0~1.0）
};

// ============================================================
// 航线 Editor STS（POST /gateway/pf-wayline/editor/v1/sts）
// ============================================================

/// 航线 Editor OSS STS 凭证
struct WaylineEditorStsCredentials {
    std::string access_key_id;     ///< 临时 AccessKey ID
    std::string access_key_secret; ///< 临时 AccessKey Secret
    int expire = 0;                ///< 凭证有效期（秒）
    std::string security_token;    ///< STS SecurityToken
    int platform = 0;              ///< 存储平台标识
};

/// 获取航线 Editor STS 请求参数
struct GetWaylineEditorStsParams {
    std::string object_key_prefix; ///< 对象存储路径前缀，必填
    int expire = 3600;             ///< 凭证有效期（秒），默认 3600（proto int64，实际值不超过 int 范围）
};

/// 获取航线 Editor STS 结果
struct GetWaylineEditorStsResult {
    WaylineEditorStsCredentials credentials; ///< STS 临时凭证
    std::string object_key_prefix; ///< 对象存储路径前缀（对应服务端 object_key_prefix）
    std::string bucket;       ///< 存储桶名称
    std::string endpoint;     ///< 存储服务端点
    std::string provider;     ///< 存储提供商（minio/aws/ali 等）
    std::string region;       ///< 存储区域
};

// ============================================================
// 图片备注-上传回调（POST /pf-media/api/v1/geo-media-file）
// ============================================================

/// 上传地理标注媒体资源（图片备注）请求参数
struct UploadGeoMediaFileParams {
    std::string name;        ///< 文件名称，必填
    std::string object_key;  ///< 媒体资源在 OSS 中的 key（通过 STS 直传后获得），必填
    std::string fingerprint; ///< 媒体资源指纹（md5），可选
    int media_source = 0;    ///< 媒体来源，可选
};

/// 上传地理标注媒体资源结果
struct UploadGeoMediaFileResult {
    int file_id = 0;        ///< 文件 ID（files 表主键），可用于轮询 URL
    std::string file_uuid;  ///< 文件 UUID
    int media_file_id = 0;  ///< 媒体文件 ID（media_files 表主键），可用于批量删除
};

// ============================================================
// 图片备注-轮询文件 URL（GET /pf-media/api/v1/media-files/{file_id}）
// ============================================================

/// 媒体文件地理位置
struct MediaFileGeoPosition {
    double lat = 0.0;     ///< 纬度
    double lng = 0.0;     ///< 经度
    double abs_alt = 0.0; ///< 绝对高度（米）
};

/// 媒体文件扩展信息
struct MediaFileDetailExt {
    int width = 0;                        ///< 图片宽度（像素）
    int height = 0;                       ///< 图片高度（像素）
    MediaFileGeoPosition photo_position;  ///< 照片 GPS 位置
};

/// 根据文件 ID 获取媒体文件详情结果
struct GetMediaFileDetailByIdResult {
    std::string url;             ///< 原始文件下载 URL
    std::string thumbnail_url;   ///< 缩略图 URL
    std::string alert_image_url; ///< 报警图片 URL
    std::string name;            ///< 文件名
    std::string suffix;          ///< 文件后缀
    long long size = 0;          ///< 文件大小（字节）
    std::string create_at;       ///< 创建时间（ISO 字符串）
    MediaFileDetailExt ext;      ///< 扩展信息
};

// ─────────────────────────────────────────────────────────────────────────────
// PaaS ai-alert 告警配置与查询结构体（810068331）
// ─────────────────────────────────────────────────────────────────────────────

struct AiAlertAlarmInfo {
    std::vector<int> alarm_method;  ///< 告警方式：1=声音 / 2=图像 / 3=其他
    int alarm_interval = 0;         ///< 告警间隔，单位秒
};

struct AiAlertAlarmTarget {
    int target_type = 0;            ///< 目标类型：0=人 / 1=车 / 2=船
    int target_value = 0;           ///< 检测置信度阈值（整数，如 80 表示 80%）
    bool use_min_threshold = false; ///< 是否启用阈值下限
    bool use_max_threshold = false; ///< 是否启用阈值上限
    int target_max_threshold = 0;   ///< 阈值上限（整数）
    int target_min_threshold = 0;   ///< 阈值下限（整数）
};

struct AiAlertLocation {
    double latitude = 0;   ///< 纬度
    double longitude = 0;  ///< 经度
    double altitude = 0;   ///< 海拔高度，单位米
};

struct AiAlertTargetAlertInfo {
    int target_type = 0;   ///< 目标类型：0=人 / 1=车 / 2=船
    int target_value = 0;  ///< 识别置信度（整数）
    std::string label;     ///< 目标标签名称
};

struct AiAlertFlightItem {
    std::string flight_id;          ///< 架次 ID
    int count = 0;                  ///< 该架次告警数量
    std::string flight_task_name;   ///< 飞行任务名称
    int status = 0;                 ///< 架次状态
    long long start_time = 0;       ///< 架次开始时间（毫秒时间戳）
};

struct AiAlertRecordItem {
    std::string alert_uuid;                                       ///< 告警唯一 ID
    std::string flight_id;                                        ///< 所属架次 ID
    std::string drone_sn;                                         ///< 无人机序列号
    std::string gateway_sn;                                       ///< 遥控器序列号
    int status = 0;                                               ///< 0=待处理 1=处理中 2=已完成 3=云云对接 4=失败 5=超时
    int algorithm_source = 0;                                     ///< 算法来源
    AiAlertLocation location;                                     ///< 告警发生时的位置信息
    std::vector<AiAlertTargetAlertInfo> target_alert_infos;       ///< 各目标类型的识别结果列表
    std::string thumbnail_url;                                    ///< status=1/2/3 时有值，24h CDN 鉴权 URL
    std::vector<std::string> labels;                              ///< 目标标签列表
    long long timestamp = 0;                                      ///< 毫秒时间戳
    std::string task_name;                                        ///< 飞行任务名称
    int media_index = 0;                                          ///< 媒体文件索引
};

// ---- 1 POST /device-setting ----
struct PostAiAlertDeviceSettingParam {
    std::string drone_sn;                  ///< 无人机序列号（必填）
    bool enable_target_alert = false;      ///< 是否开启目标检测告警
    AiAlertAlarmInfo alarm_info;           ///< 告警方式与间隔配置
};
struct PostAiAlertDeviceSettingResult {};

// ---- 2 GET /device-setting ----
struct GetAiAlertDeviceSettingParam {
    std::string drone_sn;  ///< 无人机序列号（必填）
};
struct GetAiAlertDeviceSettingResult {
    std::string drone_sn;                  ///< 无人机序列号
    bool enable_target_alert = false;      ///< 是否开启目标检测告警
    AiAlertAlarmInfo alarm_info;           ///< 告警方式与间隔配置
};

// ---- 3 POST /algorithm-setting ----
struct PostAiAlertAlgorithmSettingParam {
    std::string drone_sn;                          ///< 无人机序列号（必填）
    int algorithm_type = 0;                        ///< 算法类型编号（必填，唯一键之一）
    std::string algorithm_name;                    ///< 算法名称
    std::vector<AiAlertAlarmTarget> alarm_targets; ///< 目标告警阈值列表
};
struct PostAiAlertAlgorithmSettingResult {};

// ---- 4 GET /algorithm-setting ----
struct GetAiAlertAlgorithmSettingParam {
    std::string drone_sn;   ///< 无人机序列号（必填）
    int algorithm_type = 0; ///< 算法类型编号
};
struct GetAiAlertAlgorithmSettingResult {
    std::string drone_sn;                          ///< 无人机序列号
    int algorithm_type = 0;                        ///< 算法类型编号
    std::string algorithm_name;                    ///< 算法名称
    std::vector<AiAlertAlarmTarget> alarm_targets; ///< 目标告警阈值列表
};

// ---- 5 GET /flights ----
struct GetAiAlertFlightsParam {
    std::string drone_sn;                  ///< 无人机序列号过滤
    long long begin_at = 0;                ///< 开始时间（毫秒时间戳，0 表示不传）
    long long end_at = 0;                  ///< 结束时间（毫秒时间戳，0 表示不传）
    std::vector<int> algorithm_sources;    ///< 算法来源过滤
    int page = 1;                          ///< 页码，默认 1
    int page_size = 50;                    ///< 每页数量，默认 50
};
struct GetAiAlertFlightsResult {
    std::vector<AiAlertFlightItem> data;   ///< 架次统计列表
    int total = 0;                         ///< 总条数
    int page = 1;                          ///< 当前页码
    int page_size = 50;                    ///< 每页数量
    int page_count = 0;                    ///< 总页数
};

// ---- 6 GET /records ----
struct GetAiAlertRecordsParam {
    std::vector<std::string> flight_id;    ///< 架次 ID 列表（来自 getAiAlertFlights，必填）
    std::vector<std::string> drone_sn;     ///< 无人机 SN 过滤
    std::vector<int> target_type;          ///< 目标类型过滤：0=人 1=车 2=船
    std::vector<int> algorithm_sources;    ///< 算法来源过滤
    long long begin_at = 0;               ///< 开始时间（毫秒时间戳，0 表示不传）
    long long end_at = 0;                 ///< 结束时间（毫秒时间戳，0 表示不传）
    int page = 1;                          ///< 页码，默认 1
    int page_size = 50;                    ///< 每页数量，默认 50
};
struct GetAiAlertRecordsResult {
    std::unordered_map<std::string, std::vector<AiAlertRecordItem>> data;  ///< key = flight_id
    int total = 0;                         ///< 总条数
    int page = 1;                          ///< 当前页码
    int page_size = 50;                    ///< 每页数量
    int page_count = 0;                    ///< 总页数
};

// ===================== Live Sync (paas_live_sync) Begin =====================

struct LiveSyncSchemaOption {
    // GB28181
    std::string server_id;
    std::string server_ip;
    std::string server_port;
    std::string device_password;
    std::string local_port;
    std::string device_id;
    std::string local_channel;
    // RTMP
    std::string url;
    // RTSP
    std::string provider;
    std::string volc_user_id;
    std::string rtsp_url;
    std::string username;
    std::string password;
    bool enable_ts = false;
};

struct LiveSyncVideoCapacity {
    std::string video_type;
    std::string video_index;
};

struct LiveSyncCameraCapacity {
    std::string camera_index;
    std::vector<LiveSyncVideoCapacity> videos_list;
};

struct LiveSyncItem {
    std::string uuid;
    int status = 0;                  ///< 1 空闲 2 推流中 3 失败
    std::string org_uuid;            ///< PaaS 固定为默认 org，永不为空，透传即可
    std::string sn;
    std::string camera_index;
    std::string name;
    std::string schema;              ///< rtmp / gb28181 / rtsp
    LiveSyncSchemaOption schema_option;
    std::string device_name;
    bool device_online = false;
    std::string device_model_key;
    bool is_airport = false;
    std::string video_type;
    int video_quality = 0;
    std::string region;
    bool enable = false;
    std::string err_msg;
    std::string device_type;
};

struct LiveSyncCreateParams {
    std::string converter_name;
    std::string sn;                  ///< 直播源设备 SN，惯例填飞机（drone）SN；传网关 SN 后端不拦截但下游行为不保证
    std::string camera_index;
    std::string video_type;
    int video_quality = 0;
    std::string schema;
    LiveSyncSchemaOption schema_option;
};

struct LiveSyncCreateResult {
    LiveSyncItem item;
    int limit = 0;                   ///< 成功时 null→0，has_limit 始终 false；PaaS 超限返回 HTTP 400 + server_code=ConverterMaxNum，通过 FlightHubError.server_code 判断
    bool has_limit = false;
};

struct LiveSyncUpdateParams {
    std::string id;
    bool set_enable = false;
    bool enable = false;
    bool set_schema = false;
    std::string schema;
    bool set_schema_option = false;
    LiveSyncSchemaOption schema_option;
    bool set_converter_name = false;
    std::string converter_name;
    bool set_sn = false;
    std::string sn;                  ///< 直播源设备 SN，惯例填飞机（drone）SN；传网关 SN 后端不拦截但下游行为不保证
    bool set_camera_index = false;
    std::string camera_index;
    bool set_video_type = false;
    std::string video_type;
    bool set_video_quality = false;
    int video_quality = 0;
};

struct LiveSyncListParams {
    int page = 1;
    int size = 20;
    bool set_sn = false;
    std::string sn;
    bool set_schema = false;
    std::string schema;
    bool set_enable = false;
    bool enable = false;
};

struct LiveSyncListResult {
    int page = 1;
    int size = 20;
    long long total = 0;
    std::vector<LiveSyncItem> list;
};

struct LiveSyncDeviceV2 {
    std::string sn;
    std::string name;
    std::string device_model_key;
    std::string device_type;         ///< 设备类型字符串："airport" / "drone" / "dock" / "base_station" / "relay" / "rc" / "payload" / "dy" / ""（未知）
    std::vector<LiveSyncCameraCapacity> camera_capacity;
};

struct LiveSyncDeviceItemV2 {
    LiveSyncDeviceV2 gateway;
    std::vector<LiveSyncDeviceV2> hosts;  ///< 子设备列表；V2 含无人机和 dock，V1 仅无人机
};

struct LiveSyncDevicesV2Result {
    long long total = 0;
    std::vector<LiveSyncDeviceItemV2> list;
};

struct LiveSyncSummaryResult {
    long long total = 0;
    long long enable_cnt = 0;
};

// ===================== Live Sync End =====================

// ===================== 直播同步相关推送 Start =====================
// 字段与 notification_center 命名空间下同名 Pod 对齐（snake_case，与 notify.proto 一致）;

// 机场直播状态(live_status);
// 注意: 字段用 long long 而非 int64_t, 与本文件其余部分保持一致, 避免 SWIG 把 int64_t
// 生成为不可用的 SWIGTYPE_p_int64_t 不透明类型（Java 侧拿不到数值）;
struct LiveStatusItem {
    std::string video_id;
    std::string video_type;
    long long video_quality = 0;
    long long status = 0;
    long long error_status = 0;

    bool operator==(const LiveStatusItem& rhs) const {
        return video_id == rhs.video_id && video_type == rhs.video_type &&
            video_quality == rhs.video_quality && status == rhs.status &&
            error_status == rhs.error_status;
    }
};

struct DockLiveStatus {
    std::vector<LiveStatusItem> live_status;

    bool operator==(const DockLiveStatus& rhs) const { return live_status == rhs.live_status; }
};

// 机场直播能力(live_capacity);
struct LiveCapacityVideo {
    std::string video_index;
    std::string video_type;
    std::vector<std::string> switchable_video_types;

    bool operator==(const LiveCapacityVideo& rhs) const {
        return video_index == rhs.video_index && video_type == rhs.video_type &&
            switchable_video_types == rhs.switchable_video_types;
    }
};

struct LiveCapacityCamera {
    std::string camera_index;
    long long available_video_number = 0;
    long long coexist_video_number_max = 0;
    std::vector<LiveCapacityVideo> video_list;

    bool operator==(const LiveCapacityCamera& rhs) const {
        return camera_index == rhs.camera_index &&
            available_video_number == rhs.available_video_number &&
            coexist_video_number_max == rhs.coexist_video_number_max &&
            video_list == rhs.video_list;
    }
};

struct LiveCapacityDevice {
    std::string sn;
    long long available_video_number = 0;
    long long coexist_video_number_max = 0;
    std::vector<LiveCapacityCamera> camera_list;

    bool operator==(const LiveCapacityDevice& rhs) const {
        return sn == rhs.sn &&
            available_video_number == rhs.available_video_number &&
            coexist_video_number_max == rhs.coexist_video_number_max &&
            camera_list == rhs.camera_list;
    }
};

struct DockLiveCapacity {
    long long available_video_number = 0;
    long long coexist_video_number_max = 0;
    std::vector<LiveCapacityDevice> device_list;

    bool operator==(const DockLiveCapacity& rhs) const {
        return available_video_number == rhs.available_video_number &&
            coexist_video_number_max == rhs.coexist_video_number_max &&
            device_list == rhs.device_list;
    }
};

// Dongle 信息(机场/飞机共用 Item);
struct EsimInfoItem {
    bool enabled = false;
    std::string iccid;
    long long telecom_operator = 0;

    bool operator==(const EsimInfoItem& rhs) const {
        return enabled == rhs.enabled && iccid == rhs.iccid && telecom_operator == rhs.telecom_operator;
    }
};

struct SimInfoItem {
    std::string iccid;
    long long telecom_operator = 0;

    bool operator==(const SimInfoItem& rhs) const {
        return iccid == rhs.iccid && telecom_operator == rhs.telecom_operator;
    }
};

struct DongleInfoItem {
    long long dongle_type = 0;
    std::string eid;
    long long esim_activate_state = 0;
    std::vector<EsimInfoItem> esim_infos;
    std::string imei;
    long long sim_card_state = 0;
    long long sim_slot = 0;
    SimInfoItem sim_info;
    long long telecom_operator = 0;

    bool operator==(const DongleInfoItem& rhs) const {
        return dongle_type == rhs.dongle_type && eid == rhs.eid &&
            esim_activate_state == rhs.esim_activate_state &&
            esim_infos == rhs.esim_infos && imei == rhs.imei &&
            sim_card_state == rhs.sim_card_state && sim_slot == rhs.sim_slot &&
            sim_info == rhs.sim_info && telecom_operator == rhs.telecom_operator;
    }
};

struct DockDongleInfos {
    std::vector<DongleInfoItem> dongle_infos;

    bool operator==(const DockDongleInfos& rhs) const { return dongle_infos == rhs.dongle_infos; }
};

struct DroneDongleInfos {
    std::vector<DongleInfoItem> dongle_infos;

    bool operator==(const DroneDongleInfos& rhs) const { return dongle_infos == rhs.dongle_infos; }
};

// 机场空调工作模式(air_conditioner_mode);
struct DockAirConditionerMode {
    long long air_conditioner_mode = 0;

    bool operator==(const DockAirConditionerMode& rhs) const { return air_conditioner_mode == rhs.air_conditioner_mode; }
};

// 限飞数据库版本(flysafe_database_version);
struct DroneFlysafeDatabaseVersion {
    std::string flysafe_database_version;

    bool operator==(const DroneFlysafeDatabaseVersion& rhs) const { return flysafe_database_version == rhs.flysafe_database_version; }
};

// PSDK UI 资源(psdk_ui_resource);
struct PsdkUiResourceItem {
    long long psdk_index = 0;
    long long psdk_ready = 0;
    std::string object_key;

    bool operator==(const PsdkUiResourceItem& rhs) const {
        return psdk_index == rhs.psdk_index && psdk_ready == rhs.psdk_ready && object_key == rhs.object_key;
    }
};

struct DronePsdkUiResource {
    std::vector<PsdkUiResourceItem> psdk_ui_resource;

    bool operator==(const DronePsdkUiResource& rhs) const { return psdk_ui_resource == rhs.psdk_ui_resource; }
};

// 飞行器进入当前状态原因(mode_code_reason);
struct DroneModeCodeReason {
    long long mode_code_reason = 0;

    bool operator==(const DroneModeCodeReason& rhs) const { return mode_code_reason == rhs.mode_code_reason; }
};

// 指点飞行模式设置值(commander_flight_mode);
struct DroneCommanderFlightMode {
    long long commander_flight_mode = 0;

    bool operator==(const DroneCommanderFlightMode& rhs) const { return commander_flight_mode == rhs.commander_flight_mode; }
};

// 指点飞行模式当前值(current_commander_flight_mode);
struct DroneCurrentCommanderFlightMode {
    long long current_commander_flight_mode = 0;

    bool operator==(const DroneCurrentCommanderFlightMode& rhs) const { return current_commander_flight_mode == rhs.current_commander_flight_mode; }
};

// 指点飞行失控动作(commander_mode_lost_action);
struct DroneCommanderModeLostAction {
    long long commander_mode_lost_action = 0;

    bool operator==(const DroneCommanderModeLostAction& rhs) const { return commander_mode_lost_action == rhs.commander_mode_lost_action; }
};

// 航线解析库版本(wpmz_version);
struct DroneWpmzVersion {
    std::string wpmz_version;

    bool operator==(const DroneWpmzVersion& rhs) const { return wpmz_version == rhs.wpmz_version; }
};

// 离线地图开关(offline_map_enable);
struct DroneOfflineMapEnable {
    bool offline_map_enable = false;

    bool operator==(const DroneOfflineMapEnable& rhs) const { return offline_map_enable == rhs.offline_map_enable; }
};

// 飞机相机水印设置推送(WebSocket topic_drone_camera_watermark_settings);
// 注意: 与 DRC 现有 CameraWatermarkSettings 字段不同(缺 payload_index / manufacturer_enable), 不要混用;
struct DroneCameraWatermarkSettingsPush {
    long long global_enable = 0;
    long long drone_type_enable = 0;
    long long drone_sn_enable = 0;
    long long datetime_enable = 0;
    long long gps_enable = 0;
    long long user_custom_string_enable = 0;
    std::string user_custom_string;
    long long layout = 0;

    bool operator==(const DroneCameraWatermarkSettingsPush& rhs) const {
        return global_enable == rhs.global_enable && drone_type_enable == rhs.drone_type_enable &&
            drone_sn_enable == rhs.drone_sn_enable && datetime_enable == rhs.datetime_enable &&
            gps_enable == rhs.gps_enable && user_custom_string_enable == rhs.user_custom_string_enable &&
            user_custom_string == rhs.user_custom_string && layout == rhs.layout;
    }
};

// ===================== 直播同步相关推送 End =====================

// ===================== 云环境切换 Begin =====================
// 设备信息（呼号/型号/SN）
struct CloudEnvDeviceInfo {
    std::string callsign;
    std::string device_model_key;
    std::string device_sn;

    bool operator==(const CloudEnvDeviceInfo& other) const {
        return callsign == other.callsign && device_model_key == other.device_model_key && device_sn == other.device_sn;
    }
};

// 一组拓扑设备：host（飞行器）/ nodes（子节点，如电池、云台）/ parents（父级，如遥控器）
struct CloudEnvDeviceList {
    std::vector<CloudEnvDeviceInfo> host;
    std::vector<CloudEnvDeviceInfo> nodes;
    std::vector<CloudEnvDeviceInfo> parents;
};

// 司空2 公有云配置（target_config.type == 0 时使用）
struct CloudEnvPublicCloudConfig {
    std::string bind_code;
    std::string org_id;
    std::string zone_id;
};

// 司空2 私有云配置（target_config.type == 2 时使用）
struct CloudEnvPrivateCloudConfig {
    std::string bind_code;
    std::string org_id;
    std::string url;
};

// 三方云(API1/API2) 配置（target_config.type == 1 或 3 时使用）
struct CloudEnvThirdCloudConfig {
    std::string bind_code;
    std::string org_id;
    std::string mqtt_addr;
    std::string mqtt_password;
    std::string mqtt_username;
};

// 目标云环境配置
// type: 云环境类型，0=司空2 公有云, 1=三方云(API1), 2=司空2 私有云, 3=三方云(API2)
struct CloudEnvTargetConfig {
    int type = 0;
    CloudEnvPublicCloudConfig public_cloud_config;
    CloudEnvPrivateCloudConfig private_cloud_config;
    CloudEnvThirdCloudConfig third_cloud_config;
};

// POST /device/api/v1/service/cloud-environment-switch 请求参数
// 设备完成云环境切换后调用此接口通知服务端，携带切换结果及设备一机一密鉴权信息。
struct CloudEnvironmentSwitchParam {
    std::vector<CloudEnvDeviceList> devices_list;
    CloudEnvTargetConfig target_config;
};

// 设备一机一密鉴权信息
struct DeviceAuthVerifyParam {
    std::string sn;
    std::string secret;
    std::string nonce;
};

// POST /device/api/v1/service/cloud_environment_switch/callback 请求参数
// 向设备组下发云环境切换指令，支持司空2 公有云、司空2 私有云、三方云(API1)、三方云(API2)之间互切。
struct CloudEnvironmentSwitchCallbackParam {
    int code = 0;             // 0=success, 非0=failed
    std::string status;       // ok/failed
    DeviceAuthVerifyParam device_auth_verify;
    std::vector<CloudEnvDeviceList> devices_list;
    CloudEnvTargetConfig target_config;
};

// topic_cloud_environment_switch 推送结果 / HTTP 触发回调结果
struct CloudEnvironmentSwitchResult {
    std::string message;
    std::string bid;
    int progress = 0;

    bool operator==(const CloudEnvironmentSwitchResult& other) const {
        return message == other.message && bid == other.bid && progress == other.progress;
    }
};
// ===================== 云环境切换 End =====================
} // namespace flight_hub_sdk
