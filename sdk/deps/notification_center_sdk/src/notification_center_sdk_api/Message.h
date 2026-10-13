// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <vector>
#include <unordered_map>
#include <cstdint>
#include <cmath>

namespace notification_center {

// floatEqual 的 EPS 选择
// 核心依据是 IEEE 754 机器精度 (machine epsilon)

// 类型	      位宽	   有效十进制位	  机器精度	         推荐比较 EPS
// float	32-bit	    ~7 位	  ~1.19e-7 (2^-23)	1e-5 ~ 1e-6
// double	64-bit	    ~15 位	  ~2.22e-16 (2^-52)	1e-9 ~ 1e-12

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


// 飞机姿态信息
class AircraftAttitude {
public:
    double attitude_head = 0.0;  // 机头朝向角度
    double attitude_pitch = 0.0; // 俯仰轴角度
    double attitude_roll = 0.0;  // 横滚轴角度

    bool operator==(const AircraftAttitude &other) const {
        return
            floatEqual(attitude_head, other.attitude_head) &&
            floatEqual(attitude_pitch, other.attitude_pitch) &&
            floatEqual(attitude_roll, other.attitude_roll);
    }

    bool operator!=(const AircraftAttitude &other) const {
        return !(*this == other);
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
    int type = 0;
    int voltage = 0;

    bool operator==(const BatteryInfo &other) const {
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

    bool operator!=(const BatteryInfo &other) const {
        return !(*this == other);
    }
};

// 飞机电池组信息
class AircraftBatteryInfo {
public:
    std::vector<BatteryInfo> batteries;
    int capacity_percent = 0;
    int landing_power = 0;
    int remain_flight_time = 0;
    int return_home_power = 0;

    bool operator==(const AircraftBatteryInfo &other) const {
        return batteries == other.batteries &&
            capacity_percent == other.capacity_percent &&
            landing_power == other.landing_power &&
            remain_flight_time == other.remain_flight_time &&
            return_home_power == other.return_home_power;
    }

    bool operator!=(const AircraftBatteryInfo &other) const {
        return !(*this == other);
    }
};

// 飞机控制码信息
class AircraftControlCode {
public:
    int control_mode = 0; // "0": "航线控制-执行态", "1": "指令控制-手动飞行", "2": "指令控制-自动飞行", "3": "航线控制-中断态";

    bool operator==(const AircraftControlCode &other) const {
        return control_mode == other.control_mode;
    }

    bool operator!=(const AircraftControlCode &other) const {
        return !(*this == other);
    }
};

// 飞机位置信息
class AircraftLocation {
public:
    double height = 0.0;    // 椭球高度
    double elevation = 0.0; // 相对起飞点高度
    double longitude = 0.0;
    double latitude = 0.0;

    bool operator==(const AircraftLocation &other) const {
        return
            floatEqual(height, other.height) &&
            floatEqual(elevation, other.elevation) &&
            floatEqual(longitude, other.longitude) &&
            floatEqual(latitude, other.latitude);
    }

    bool operator!=(const AircraftLocation &other) const {
        return !(*this == other);
    }
};

// 飞机模式码信息
class AircraftModeCode {
public:
    int mode_code = 0;

    bool operator==(const AircraftModeCode &other) const {
        return mode_code == other.mode_code;
    }

    bool operator!=(const AircraftModeCode &other) const {
        return !(*this == other);
    }
};

// 相机直播视图世界区域信息
class CameraLiveviewWorldRegion {
public:
    double bottom = 0.0;
    double left = 0.0;
    double right = 0.0;
    double top = 0.0;

    bool operator==(const CameraLiveviewWorldRegion &other) const {
        return
            floatEqual(bottom, other.bottom) &&
            floatEqual(left, other.left) &&
            floatEqual(right, other.right) &&
            floatEqual(top, other.top);
    }

    bool operator!=(const CameraLiveviewWorldRegion &other) const {
        return !(*this == other);
    }
};

// 飞机负载相机直播视图世界区域信息
class AircraftPayloadsCameraLiveviewWorldRegion {
public:
    std::unordered_map<std::string, CameraLiveviewWorldRegion> payloads_list;

    bool operator==(const AircraftPayloadsCameraLiveviewWorldRegion &other) const {
        return payloads_list == other.payloads_list;
    }

    bool operator!=(const AircraftPayloadsCameraLiveviewWorldRegion &other) const {
        return !(*this == other);
    }
};

// 飞机负载云台姿态信息
class AircraftPayloadGimbalAttitude {
public:
    double gimbal_pitch = 0.0;
    double gimbal_roll = 0.0;
    double gimbal_yaw = 0.0;

    bool operator==(const AircraftPayloadGimbalAttitude &other) const {
        return
            floatEqual(gimbal_pitch, other.gimbal_pitch) &&
            floatEqual(gimbal_roll, other.gimbal_roll) &&
            floatEqual(gimbal_yaw, other.gimbal_yaw);
    }

    bool operator!=(const AircraftPayloadGimbalAttitude &other) const {
        return !(*this == other);
    }
};

// 飞机负载云台姿态组信息
class AircraftPayloadsGimbalAttitude {
public:
    std::unordered_map<std::string, AircraftPayloadGimbalAttitude>
    payloads_gimbal_attitude;

    bool operator==(const AircraftPayloadsGimbalAttitude &other) const {
        return payloads_gimbal_attitude == other.payloads_gimbal_attitude;
    }

    bool operator!=(const AircraftPayloadsGimbalAttitude &other) const {
        return !(*this == other);
    }
};

// 飞机负载列表信息
class AircraftPayloadsList {
public:
    std::vector<std::string> payloads_list;

    bool operator==(const AircraftPayloadsList &other) const {
        return payloads_list == other.payloads_list;
    }

    bool operator!=(const AircraftPayloadsList &other) const {
        return !(*this == other);
    }
};

// 飞机速度信息
class AircraftSpeed {
public:
    double horizontal_speed = 0.0; // 水平速度 m/s
    double vertical_speed = 0.0;   // 垂直速度 m/s

    bool operator==(const AircraftSpeed &other) const {
        return
            floatEqual(horizontal_speed, other.horizontal_speed) &&
            floatEqual(vertical_speed, other.vertical_speed);
    }

    bool operator!=(const AircraftSpeed &other) const {
        return !(*this == other);
    }
};

// 飞机风速信息
class AircraftWindSpeed {
public:
    int wind_direction = 0; // 当前风向 "enum_desc": "1:正北  2：东北  3:东 4:东南  5:南  6:西南  7:西   8:西北"
    double wind_speed = 0.0; // 风速估计，该风速是通过飞机姿态推测出的，有一定的误差，仅供参考，不能作为气象数据使用

    bool operator==(const AircraftWindSpeed &other) const {
        return
            wind_direction == other.wind_direction &&
            floatEqual(wind_speed, other.wind_speed);
    }

    bool operator!=(const AircraftWindSpeed &other) const {
        return !(*this == other);
    }
};

// 设备变更信息
class DeviceChange {
public:
    std::string change_type; // "add" or "reduce"
    std::vector<std::string> change_devices;

    bool operator==(const DeviceChange &other) const {
        return change_type == other.change_type &&
            change_devices == other.change_devices;
    }

    bool operator!=(const DeviceChange &other) const {
        return !(*this == other);
    }
};

// 设备拓扑变更信息
class DeviceTopoChange {
public:
    std::string gateway_sn;
    std::vector<std::string> devices; // 设备列表

    bool operator==(const DeviceTopoChange &other) const {
        return gateway_sn == other.gateway_sn && devices == other.devices;
    }

    bool operator!=(const DeviceTopoChange &other) const {
        return !(*this == other);
    }
};

// 工作空间设备变更信息
class WorkspaceDeviceChange {
public:
    std::vector<DeviceChange> device_changes;

    bool operator==(const WorkspaceDeviceChange &other) const {
        return device_changes == other.device_changes;
    }

    bool operator!=(const WorkspaceDeviceChange &other) const {
        return !(*this == other);
    }
};

// 控制权限云控制状态项信息
class ControlRightsCloudControlStatusItem {
public:
    std::string type; // flight/payload, 飞机还是负载
    std::string key;  // 如果 type 是 flight, 则 key 固定为flight, 如果是 payload,
                      // 则是 payload_81-0-0 这样的格式(带上了payload_index)
    std::string user_id; // 用户 id
    bool is_locked = false;
    double version = 0.0;

    bool operator==(const ControlRightsCloudControlStatusItem &other) const {
        return
            type == other.type &&
            key == other.key &&
            user_id == other.user_id &&
            is_locked == other.is_locked &&
            floatEqual(version, other.version);
    }

    bool operator!=(const ControlRightsCloudControlStatusItem &other) const {
        return !(*this == other);
    }
};

// 控制权限云控制状态信息
class ControlRightsCloudControlStatus {
public:
    std::string sn;
    std::string reason;
    std::vector<ControlRightsCloudControlStatusItem> controls;

    bool operator==(const ControlRightsCloudControlStatus &other) const {
        return sn == other.sn && reason == other.reason &&
            controls == other.controls;
    }

    bool operator!=(const ControlRightsCloudControlStatus &other) const {
        return !(*this == other);
    }
};

// 控制权限设备控制状态项信息
class ControlRightsDeviceControlStatusItem {
public:
    std::string type; // flight/payload, 飞机还是负载
    std::string key;  // 如果 type 是 flight, 则 key 固定为flight, 如果是 payload,
                      // 则是 payload_81-0-0 这样的格式(带上了payload_index)
    std::string owner; // RC/MC/CCC
    double version = 0.0;

    bool operator==(const ControlRightsDeviceControlStatusItem &other) const {
        return
            type == other.type &&
            key == other.key &&
            owner == other.owner &&
            floatEqual(version, other.version);
    }

    bool operator!=(const ControlRightsDeviceControlStatusItem &other) const {
        return !(*this == other);
    }
};

// 控制权限设备控制状态信息
class ControlRightsDeviceControlStatus {
public:
    std::string sn;
    std::string reason;
    std::vector<ControlRightsDeviceControlStatusItem> controls;

    bool operator==(const ControlRightsDeviceControlStatus &other) const {
        return sn == other.sn && reason == other.reason &&
            controls == other.controls;
    }

    bool operator!=(const ControlRightsDeviceControlStatus &other) const {
        return !(*this == other);
    }
};

// 设备在线状态信息
class DeviceOnlineStatus {
public:
    bool is_online = false;

    bool operator==(const DeviceOnlineStatus &other) const {
        return is_online == other.is_online;
    }

    bool operator!=(const DeviceOnlineStatus &other) const {
        return !(*this == other);
    }
};

// 设备重启信息
class DeviceReboot {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DeviceReboot &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DeviceReboot &other) const {
        return !(*this == other);
    }
};

// 机场位置信息
class DockLocation {
public:
    double heading = 0.0;
    double height = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;

    bool operator==(const DockLocation &other) const {
        return
            floatEqual(heading, other.heading) &&
            floatEqual(height, other.height) &&
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude);
    }

    bool operator!=(const DockLocation &other) const {
        return !(*this == other);
    }
};

// 机场离场点信息
class DockDeparturePoint {
public:
    double longitude = 0.0;  // 经度
    double latitude = 0.0;   // 纬度
    double height = 0.0;     // 椭球高度

    bool operator==(const DockDeparturePoint &other) const {
        return
            floatEqual(longitude, other.longitude) &&
            floatEqual(latitude, other.latitude) &&
            floatEqual(height, other.height);
    }

    bool operator!=(const DockDeparturePoint &other) const {
        return !(*this == other);
    }
};

// 机场内飞机信息
class DroneInDock {
public:
    int drone_in_dock = 0;

    bool operator==(const DroneInDock &other) const {
        return drone_in_dock == other.drone_in_dock;
    }

    bool operator!=(const DroneInDock &other) const {
        return !(*this == other);
    }
};

// 机场舱盖状态信息;
class DockCoverState {
public:
    int cover_state = 0; // 0: 关闭, 1: 打开, 2: 半开, 3: 舱盖状态异常;

    bool operator==(const DockCoverState &rhs) const {
        return cover_state == rhs.cover_state;
    }

    bool operator!=(const DockCoverState &rhs) const {
        return !(*this == rhs);
    }
};

// 机场舱盖开启结果
class DockCoverOpen {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DockCoverOpen &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DockCoverOpen &other) const {
        return !(*this == other);
    }
};

// 机场舱盖关闭结果
class DockCoverClose {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DockCoverClose &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DockCoverClose &other) const {
        return !(*this == other);
    }
};

// 设备格式化结果
class DeviceFormat {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DeviceFormat &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DeviceFormat &other) const {
        return !(*this == other);
    }
};

// 设备 RTK 校准结果
class DeviceRtkCalibration {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DeviceRtkCalibration &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DeviceRtkCalibration &other) const {
        return !(*this == other);
    }
};

// 设备 SIM 卡槽切换结果
class DeviceSimSlotSwitch {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DeviceSimSlotSwitch &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DeviceSimSlotSwitch &other) const {
        return !(*this == other);
    }
};

// 设备 eSIM 运营商切换结果
class DeviceEsimOperatorSwitch {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DeviceEsimOperatorSwitch &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DeviceEsimOperatorSwitch &other) const {
        return !(*this == other);
    }
};

// 设备云环境切换结果
class DeviceCloudEnvironmentSwitch {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;
    int progress = 0;

    bool operator==(const DeviceCloudEnvironmentSwitch &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status && progress == other.progress;
    }

    bool operator!=(const DeviceCloudEnvironmentSwitch &other) const {
        return !(*this == other);
    }
};

// 网络测速结果
class DeviceNetworkSpeedTestSpeedResult {
public:
    double upload_speed = 0.0;
    double download_speed = 0.0;

    bool operator==(const DeviceNetworkSpeedTestSpeedResult &other) const {
        return
            floatEqual(upload_speed, other.upload_speed) &&
            floatEqual(download_speed, other.download_speed);
    }

    bool operator!=(const DeviceNetworkSpeedTestSpeedResult &other) const {
        return !(*this == other);
    }
};

// 设备网络测速进度
class DeviceNetworkSpeedTestProgress {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;
    DeviceNetworkSpeedTestSpeedResult speed_result;

    bool operator==(const DeviceNetworkSpeedTestProgress &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status &&
               speed_result == other.speed_result;
    }

    bool operator!=(const DeviceNetworkSpeedTestProgress &other) const {
        return !(*this == other);
    }
};

// 无人机开机结果
class DroneOpen {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DroneOpen &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DroneOpen &other) const {
        return !(*this == other);
    }
};

// 无人机关机结果
class DroneClose {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DroneClose &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DroneClose &other) const {
        return !(*this == other);
    }
};

// 无人机充电开启结果
class DroneChargeOpen {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DroneChargeOpen &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DroneChargeOpen &other) const {
        return !(*this == other);
    }
};

// 无人机充电关闭结果
class DroneChargeClose {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DroneChargeClose &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DroneChargeClose &other) const {
        return !(*this == other);
    }
};

// 无人机格式化结果
class DroneFormat {
public:
    std::string bid;
    long long timestamp = 0;
    int result = 0;
    std::string status;

    bool operator==(const DroneFormat &other) const {
        return bid == other.bid && timestamp == other.timestamp &&
               result == other.result && status == other.status;
    }

    bool operator!=(const DroneFormat &other) const {
        return !(*this == other);
    }
};

// 机场飞机充电状态;
class DockDroneChargeState {
public:
    int state = 0; // 0: 空闲, 1: 正在充电;
    int capacity_percent = 0;

    bool operator==(const DockDroneChargeState &rhs) const {
        return (state == rhs.state) && (capacity_percent == rhs.capacity_percent);
    }

    bool operator!=(const DockDroneChargeState &rhs) const {
        return !(*this == rhs);
    }
};

// 机场气象信息;
class DockMeteorologicalInfo {
public:
    double temperature = 0.0;             // 舱内温度;
    double environment_temperature = 0.0; // 环境温度;
    int humidity = 0;                     // 舱内湿度;
    double wind_speed = 0.0;              // 风速;
    int rainfall = 0;                     // 雨量;

    bool operator==(const DockMeteorologicalInfo &rhs) const {
        return
            floatEqual(temperature, rhs.temperature) &&
            floatEqual(environment_temperature, rhs.environment_temperature) &&
            (humidity == rhs.humidity) &&
            floatEqual(wind_speed, rhs.wind_speed) &&
            (rainfall == rhs.rainfall);
    }

    bool operator!=(const DockMeteorologicalInfo &rhs) const {
        return !(*this == rhs);
    }
};

// 机场备用电池信息;
class DockBackupBatteryInfo {
public:
    int switch_state = 0;     // 备用电池开关(0: 备用电池关闭, 1: 备用电池开启);
    int voltage = 0;          // 备用电池电压(单位: 毫伏, mV);
    double temperature = 0.0; // 备用电池温度(单位: 摄氏度);

    bool operator==(const DockBackupBatteryInfo &rhs) const {
        return
            (switch_state == rhs.switch_state) &&
            (voltage == rhs.voltage) &&
            floatEqual(temperature, rhs.temperature);
    }

    bool operator!=(const DockBackupBatteryInfo &rhs) const {
        return !(*this == rhs);
    }
};

// 机场保养信息;
class DockMaintainStatus {
public:
    int state = 0;                      // 保养状态(0: 未保养, 1: 保养中);
    int last_maintain_type = 0;         // 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养);
    long last_maintain_time = 0;        // 上一次保养时间, utc 秒时间戳(单位: 秒); 类型与历史 JSON 对齐
    int last_maintain_work_sorties = 0; // 上一次保养时飞行架次, [0, 2147483647];

    bool operator==(const DockMaintainStatus &rhs) const {
        return (state == rhs.state) &&
            (last_maintain_type == rhs.last_maintain_type) &&
            (last_maintain_time == rhs.last_maintain_time) &&
            (last_maintain_work_sorties == rhs.last_maintain_work_sorties);
    }

    bool operator!=(const DockMaintainStatus &rhs) const {
        return !(*this == rhs);
    }
};

// 机场保养信息列表;
class DockMaintainStatusInfo {
public:
    std::vector<DockMaintainStatus> maintain_status_info;

    bool operator==(const DockMaintainStatusInfo &rhs) const {
        return maintain_status_info == rhs.maintain_status_info;
    }

    bool operator!=(const DockMaintainStatusInfo &rhs) const {
        return !(*this == rhs);
    }
};

// 机场 mode code;
class DockModeCode {
public:
    int mode_code = 0;  // {0: 空闲中, 1: 现场调试, 2: 远程调试, 3: 固件升级中, 4: 作业中, 5: 待标定;};

    bool operator==(const DockModeCode &rhs) const {
        return mode_code == rhs.mode_code;
    }

    bool operator!=(const DockModeCode &rhs) const {
        return !(*this == rhs);
    }
};

// 飞行任务 step code;
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

    bool operator!=(const FlightTaskStepCode &rhs) const {
        return !(*this == rhs);
    }
};


// 飞向飞机自规划路径点信息
class FlyToDroneSelfPlanPathPoint {
public:
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    bool operator==(const FlyToDroneSelfPlanPathPoint &other) const {
        return
            floatEqual(latitude, other.latitude) &&
            floatEqual(longitude, other.longitude) &&
            floatEqual(height, other.height);
    }

    bool operator!=(const FlyToDroneSelfPlanPathPoint &other) const {
        return !(*this == other);
    }
};

// 飞向飞机自规划路径信息
class FlyToDroneSelfPlanPath {
public:
    std::string fly_to_id;
    std::string sn;
    long timestamp = 0;
    std::vector<FlyToDroneSelfPlanPathPoint> planned_path_points;
    double remaining_distance = 0.0;
    double remaining_time = 0.0;

    bool operator==(const FlyToDroneSelfPlanPath &other) const {
        return
            fly_to_id == other.fly_to_id &&
            sn == other.sn &&
            timestamp == other.timestamp &&
            planned_path_points == other.planned_path_points &&
            floatEqual(remaining_distance, other.remaining_distance) &&
            floatEqual(remaining_time, other.remaining_time);
    }

    bool operator!=(const FlyToDroneSelfPlanPath &other) const {
        return !(*this == other);
    }
};

// 飞向点进度信息
class FlyToPointProgress {
public:
    std::string bid;
    std::string tid;
    long timestamp = 0;
    std::string fly_to_id;
    std::string flight_id;
    int task_seq = 0;
    std::string sn;
    std::string status;
    int result = 0;
    int waypoint_index = 0;

    bool operator==(const FlyToPointProgress &other) const {
        return bid == other.bid && tid == other.tid &&
            timestamp == other.timestamp && fly_to_id == other.fly_to_id &&
            flight_id == other.flight_id && task_seq == other.task_seq &&
            sn == other.sn && status == other.status && result == other.result &&
            waypoint_index == other.waypoint_index;
    }

    bool operator!=(const FlyToPointProgress &other) const {
        return !(*this == other);
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

    bool operator==(const FlightTasksMediaFileDisplayAvailable &other) const {
        return uuid == other.uuid && idx_parent_id == other.idx_parent_id &&
            file_type == other.file_type &&
            sub_file_type == other.sub_file_type &&
            media_file_id == other.media_file_id &&
            flight_id == other.flight_id && sn == other.sn &&
            media_source == other.media_source && url == other.url;
    }

    bool operator!=(const FlightTasksMediaFileDisplayAvailable &other) const {
        return !(*this == other);
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
    std::string status;
    int result = 0;
    int waypoint_index = 0;
    std::string takeoff_mode; // Takeoff/TakeoffWithFlyTo
    long timestamp = 0;

    bool operator==(const AircraftTakeoffProgress &other) const {
        return bid == other.bid && tid == other.tid &&
            flight_id == other.flight_id && track_id == other.track_id &&
            sn == other.sn && status == other.status && result == other.result &&
            waypoint_index == other.waypoint_index &&
            takeoff_mode == other.takeoff_mode && timestamp == other.timestamp;
    }

    bool operator!=(const AircraftTakeoffProgress &other) const {
        return !(*this == other);
    }
};

// 异常详情信息
class FlightTaskExceptionDetail {
public:
    int code = 0;
    std::string message;
    int task_id = 0;
    std::string happen_at; // 时间字符串，如 "2025-09-17T04:01:00.972477409Z"
    std::string sn;

    bool operator==(const FlightTaskExceptionDetail &other) const {
        return code == other.code && message == other.message &&
            task_id == other.task_id && happen_at == other.happen_at &&
            sn == other.sn;
    }

    bool operator!=(const FlightTaskExceptionDetail &other) const {
        return !(*this == other);
    }
};

// 任务异常信息
class FlightTaskException {
public:
    std::string uuid;
    std::string name;
    std::string sn;
    int task_type = 0;
    int status = 0;
    int progress = 0;
    std::vector<FlightTaskExceptionDetail> exceptions;
    long timestamp = 0;

    bool operator==(const FlightTaskException &other) const {
        return uuid == other.uuid && name == other.name && sn == other.sn &&
            task_type == other.task_type && status == other.status &&
            progress == other.progress && exceptions == other.exceptions &&
            timestamp == other.timestamp;
    }

    bool operator!=(const FlightTaskException &other) const {
        return !(*this == other);
    }
};


// 飞机: 激光打点信息;
class AircraftPayloadsCameraMeasureTarget {
public:
    std::string payload_index;
    double measure_target_altitude = 0.0;    // 海拔高度;
    double measure_target_longitude = 0.0;   // 经度;
    double measure_target_latitude = 0.0;    // 纬度;
    double measure_target_distance = 0.0;
    int measure_target_error_state = 0;

    bool operator==(const AircraftPayloadsCameraMeasureTarget &rhs) const {
        return
            (payload_index == rhs.payload_index) &&
            floatEqual(measure_target_altitude, rhs.measure_target_altitude) &&
            floatEqual(measure_target_longitude, rhs.measure_target_longitude) &&
            floatEqual(measure_target_latitude, rhs.measure_target_latitude) &&
            floatEqual(measure_target_distance, rhs.measure_target_distance) &&
            (measure_target_error_state == rhs.measure_target_error_state);
    }

    bool operator!=(const AircraftPayloadsCameraMeasureTarget &rhs) const {
        return !(*this == rhs);
    }
};

// 飞机: 激光打点信息列表;
class AircraftPayloadsCameraMeasureTargets {
public:
    std::vector<AircraftPayloadsCameraMeasureTarget> measure_targets;

    bool operator==(const AircraftPayloadsCameraMeasureTargets &rhs) const {
        return measure_targets == rhs.measure_targets;
    }

    bool operator!=(const AircraftPayloadsCameraMeasureTargets &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockSubDeviceInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 机场: 子设备信息列表;
class DockSubDeviceInfoList {
public:
    std::vector<DockSubDeviceInfo> sub_device_info_vec;

    bool operator==(const DockSubDeviceInfoList &rhs) const {
        return sub_device_info_vec == rhs.sub_device_info_vec;
    }

    bool operator!=(const DockSubDeviceInfoList &rhs) const {
        return !(*this == rhs);
    }
};

// 机场空调工作模式信息
class DockAirConditionerStatusInfo {
public:
    int air_conditioner_state = 0;
    int switch_time = 0;
    bool operator==(const DockAirConditionerStatusInfo &other) const {
        return air_conditioner_state == other.air_conditioner_state &&
            switch_time == other.switch_time;
    }

    bool operator!=(const DockAirConditionerStatusInfo &other) const {
        return !(*this == other);
    }
};

// 机场补光灯状态信息
class DockSupplementLightState {
public:
    int supplement_light_state = 0;
    bool operator==(const DockSupplementLightState &other) const {
        return supplement_light_state == other.supplement_light_state;
    }

    bool operator!=(const DockSupplementLightState &other) const {
        return !(*this == other);
    }
};

// 直播录制状态推送（topic_live_record_info）
class LiveRecordInfo {
public:
    std::string bid;
    std::string sn;
    std::string camera;
    std::string video;
    int state = 0;           // RecordState: 0=Success, 1=Recording, 2=Initializing, 3=Failed
    std::string created_by;
    int duration = 0;
    int64_t create_time = 0;
    bool operator==(const LiveRecordInfo& other) const {
        return bid == other.bid && sn == other.sn && camera == other.camera && video == other.video
            && state == other.state && created_by == other.created_by && duration == other.duration && create_time == other.create_time;
    }

    bool operator!=(const LiveRecordInfo &other) const {
        return !(*this == other);
    }
};

// 机场网络状态(network_state);
class DockNetworkState {
public:
    int type = -1;     // int, "网络类型", { 1: 4G, 2: 以太网 };  默认无效值: -1;
    int quality = -1;  // int, "网络质量(当网络类型为4G时才具有网络质量信息)", { 0: 差, 1: 中, 2: 好 }; 默认无效值: -1;
    int rate = 0;      // int, "网络速率(单位KB/秒)"; 默认值: 0;

    bool operator==(const DockNetworkState &rhs) const {
        return (type == rhs.type) && (quality == rhs.quality) && (rate == rhs.rate);
    }

    bool operator!=(const DockNetworkState &rhs) const {
        return !(*this == rhs);
    }
};


// 媒体文件数量(media_file_detail);
class DockMediaFileDetail {
public:
    int remain_upload = 0;  // int, "待上传数量";

    bool operator==(const DockMediaFileDetail &rhs) const {
        return remain_upload == rhs.remain_upload;
    }

    bool operator!=(const DockMediaFileDetail &rhs) const {
        return !(*this == rhs);
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
    float sdr_freq_band = 0;      // float 类型即可(无需 double): sdr 频段;
    float freq_band_4g = 0;       // float 类型即可(无需 double): 4g 频段;

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

    bool operator!=(const DockWirelessLink &rhs) const {
        return !(*this == rhs);
    }
};


// 机场作业次数(job_number);
class DockJobNumber {
public:
    int job_number = 0;  // int, 作业次数;

    bool operator==(const DockJobNumber &rhs) const {
        return job_number == rhs.job_number;
    }

    bool operator!=(const DockJobNumber &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockPowerState &rhs) const {
        return !(*this == rhs);
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
        return
            (total == rhs.total) && (used == rhs.used);
    }

    bool operator!=(const DockStorageState &rhs) const {
        return !(*this == rhs);
    }
};


// 机场首次上电时间(first_power_on);
class DockFirstPowerOnTime {
public:
    // 首次上电时间，毫秒时间戳用 string，与历史 JSON 对齐。
    std::string first_power_on = "0";  // 首次上电时间(ms);

    bool operator==(const DockFirstPowerOnTime &rhs) const {
        return first_power_on == rhs.first_power_on;
    }

    bool operator!=(const DockFirstPowerOnTime &rhs) const {
        return !(*this == rhs);
    }
};


// 机场累计运行时长(acc_time);
class DockAccTime {
public:
    int acc_time = 0;    // int, 机场累计运行时长(秒);

    bool operator==(const DockAccTime &rhs) const {
        return acc_time == rhs.acc_time;
    }

    bool operator!=(const DockAccTime &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockAlternateLandPoint &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockPositionState &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockActivationTime &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockBatteryStoreMode &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockAlarmState &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const BatteryBasicInfo &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockDroneBatteryMaintenanceInfo &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockEmergencyStopState &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockSilentMode &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockUserExperienceImprovement &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockDrcState &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockPutterState &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const DockRelativeAlternateLandPoint &rhs) const {
        return !(*this == rhs);
    }
};


// 自收敛坐标(self_converge_coordinate);
// DockSelfConvergeCoordinate;
class DockSelfConvergeCoordinate {
public:
    double height = 0.0;
    double longitude = 0.0;
    double latitude = 0.0;

    bool operator==(const DockSelfConvergeCoordinate &rhs) const {
        return
            floatEqual(height, rhs.height) &&
            floatEqual(longitude, rhs.longitude) &&
            floatEqual(latitude, rhs.latitude);
    }

    bool operator!=(const DockSelfConvergeCoordinate &rhs) const {
        return !(*this == rhs);
    }
};


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

    bool operator!=(const SmartTrackPointInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 飞机负载信息;
// AircraftPayloadsOsdInfo
class AircraftPayloadsOsdInfo {

public:
    std::string payload_index;                             // 负载索引/负载编号,{type-subtype-gimbalindex}, such as "81-0-0";

    double gimbal_pitch = 0.0;                             // 云台俯仰轴角度, [-180, 180]度;
    double gimbal_roll = 0.0;                              // 云台横滚轴角度,[-180, 180]度;
    double gimbal_yaw = 0.0;                               // 云台朝向角度/云台偏航轴角度,[-180, 180]度;

    double measure_target_altitude = 0.0;                  // 激光测距目标海拔(单位: m);
    double measure_target_longitude = 0.0;                 // 激光测距目标经度,[-180,180]度;
    double measure_target_latitude = 0.0;                  // 激光测距目标纬度,[-90, 90]度;
    double measure_target_distance = 0.0;                  // 测距距离(单位: m);
    int measure_target_error_state = 0;                    // 测距状态, 0: NORMAL, 1: TOO_CLOSE, 2: TOO_FAR, 3: NO_SIGNAL;

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

    std::string control_source;                             // 负载控制权;
    std::string firmware_version;                            // 负载固件版本;
    std::string sn;                                          // 负载sn;


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
            measure_target_error_state == rhs.measure_target_error_state &&
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

    bool operator!=(const AircraftPayloadsOsdInfo &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftPayloadsOsdInfoList &rhs) const {
        return !(*this == rhs);
    }
};


// 飞机: 档位信息(gear);
// AircraftGear;
class AircraftGear {
public:
    // int, 0:"A", 1:"P", 2:"NAV", 3:"FPV", 4:"FARM", 5:"S", 6:"F", 7:"M", 8:"G", 9:"T";
    int gear = 0;

    bool operator==(const AircraftGear &rhs) const {
        return gear == rhs.gear;
    }

    bool operator!=(const AircraftGear &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftHomeDistance &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftPositionState &rhs) const {
        return !(*this == rhs);
    }
};


// 飞机: 当前控制源(control_source);
// AircraftControlSource;
class AircraftControlSource {
public:
    std::string control_source;    // 当前控制源;

    bool operator==(const AircraftControlSource &rhs) const {
        return control_source == rhs.control_source;
    }

    bool operator!=(const AircraftControlSource &rhs) const {
        return !(*this == rhs);
    }
};


// 飞机: 限制类信息状态;
// AircraftLimitsInfo;
class AircraftLimitsInfo {
public:
    int height_limit = 0;             // 相对起飞点的飞行限制高度,单位(m);
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

    bool operator!=(const AircraftLimitsInfo &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftBatteryWarningThresholds &rhs) const {
        return !(*this == rhs);
    }
};


// 固件状态信息;
// AircraftFirmwareStatus;
class AircraftFirmwareStatus {
public:
    std::string firmware_version;     // 固件版本, such as "00.01.0439";
    int compatible_status = 0;             // 固件一致性, {"0: 一致, 1: 不一致"};
    int firmware_upgrade_status = 0;       // 固件升级状态, {"0: 未升级, 1: 升级中"};

    bool operator==(const AircraftFirmwareStatus &rhs) const {
        return
            firmware_version == rhs.firmware_version &&
            compatible_status == rhs.compatible_status &&
            firmware_upgrade_status == rhs.firmware_upgrade_status;
    }

    bool operator!=(const AircraftFirmwareStatus &rhs) const {
        return !(*this == rhs);
    }
};


// 累计飞行信息(total_flight_info);
// AircraftTotalFlightInfo;
class AircraftTotalFlightInfo {
public:
    double total_flight_time = 0.0;       // double, 总飞行时长(飞行器累计飞行航时);
    double total_flight_distance = 0.0;   // double, 总飞行距离;
    int total_flight_sorties = 0;         // int, 总飞行架次;

    bool operator==(const AircraftTotalFlightInfo &rhs) const {
        return
            floatEqual(total_flight_time, rhs.total_flight_time) &&
            floatEqual(total_flight_distance, rhs.total_flight_distance) &&
            total_flight_sorties == rhs.total_flight_sorties;
    }

    bool operator!=(const AircraftTotalFlightInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 飞行器激活时间(unix时间戳)(activation_time);
// AircraftActivationTime;
class AircraftActivationTime {
public:
    // 跨平台不宜用 long；此处保持与历史 JSON 一致。
    long activation_time = 0;       // 飞行器激活时间(unix时间戳);

    bool operator==(const AircraftActivationTime &rhs) const {
        return activation_time == rhs.activation_time;
    }

    bool operator!=(const AircraftActivationTime &rhs) const {
        return !(*this == rhs);
    }
};


// 飞机：保养信息(maintain_status);
// 飞机保养信息;
class AircraftMaintainStatus {
public:
    int state = 0;                           // 保养状态(0: 未保养, 1: 保养中);
    int last_maintain_type = 0;              // 上一次保养类型(0: 无保养, 1: 飞机基础保养, 2: 飞机常规保养, 3: 飞机深度保养, 17: 机场保养);
    long last_maintain_time = 0;             // 上一次保养时间, utc 秒时间戳(单位: 秒); 类型与历史 JSON 对齐
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

    bool operator!=(const AircraftMaintainStatus &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftMaintainStatusInfo &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftNightLightsState &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftObstacleAvoidanceState &rhs) const {
        return !(*this == rhs);
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
        return
            (total == rhs.total) && (used == rhs.used);
    }

    bool operator!=(const AircraftStorageInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 航迹id(track_id);
// AircraftTrackId;
class AircraftTrackId {
public:
    std::string track_id;    // string, 航迹id, such as "da247702-9636-4404-b98c-7e667efb4c4b";

    bool operator==(const AircraftTrackId &rhs) const {
        return track_id == rhs.track_id;
    }

    bool operator!=(const AircraftTrackId &rhs) const {
        return !(*this == rhs);
    }
};


// 合规相关信息(compliance information);
// AircraftComplianceInfo;
class AircraftComplianceInfo {
public:
    int rid_state = 0;           // int, RID(Remote Identification)工作状态", {1: 正常, 0: 异常};
    std::string country;    // string, 国家区域码;

    bool operator==(const AircraftComplianceInfo &rhs) const {
        return
            (rid_state == rhs.rid_state) &&
            (country == rhs.country);
    }

    bool operator!=(const AircraftComplianceInfo &rhs) const {
        return !(*this == rhs);
    }
};


/*
 * 飞机相机相关的数据;
 */

// 红外测温点相关信息;
// IrMeteringPoint;
class IrMeteringPoint {
public:
    double temperature = 0.0;    // double, 温度;
    double x = 0.0;              // double, x坐标(0.0~1.0);
    double y = 0.0;              // double, y坐标(0.0~1.0);

    bool operator==(const IrMeteringPoint &rhs) const {
        return
            floatEqual(temperature, rhs.temperature) &&
            floatEqual(x, rhs.x) &&
            floatEqual(y, rhs.y);
    }

    bool operator!=(const IrMeteringPoint &rhs) const {
        return !(*this == rhs);
    }
};

// Liveview 世界区域信息;
// LiveviewWorldRegion;
class LiveviewWorldRegion {
public:
    double bottom = 0.0;
    double left = 0.0;
    double right = 0.0;
    double top = 0.0;

    bool operator==(const LiveviewWorldRegion &rhs) const {
        return
            floatEqual(bottom, rhs.bottom) &&
            floatEqual(left, rhs.left) &&
            floatEqual(right, rhs.right) &&
            floatEqual(top, rhs.top);
    }

    bool operator!=(const LiveviewWorldRegion &rhs) const {
        return !(*this == rhs);
    }
};


// 飞机相机负载信息;
// AircraftPayloadCameraInfo;
class AircraftPayloadCameraInfo {
public:
    std::string payload_index;    // "payload_index": "81-0-0";
    int camera_mode = 0;               // 相机模式, {0: 拍照, 1: 录像, 2: 低光智能, 3: 全景拍照, -1: 不支持的模式};
    int ir_metering_mode = 0;
    IrMeteringPoint ir_metering_point;
    double ir_zoom_factor = 0.0;
    LiveviewWorldRegion liveview_world_region;
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
    double zoom_factor = 0.0;
    int zoom_focus_mode = 0;
    int zoom_focus_state = 0;
    int zoom_focus_value = 0;
    int zoom_iso = 0;
    int zoom_max_focus_value = 0;
    int zoom_min_focus_value = 0;
    int zoom_shutter_speed = 0;


    bool operator==(const AircraftPayloadCameraInfo &rhs) const {
        return
            (payload_index == rhs.payload_index) &&
            (camera_mode == rhs.camera_mode) &&
            (ir_metering_mode == rhs.ir_metering_mode) &&
            (ir_metering_point == rhs.ir_metering_point) &&
            floatEqual(ir_zoom_factor, rhs.ir_zoom_factor) &&
            (liveview_world_region == rhs.liveview_world_region) &&
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

    bool operator!=(const AircraftPayloadCameraInfo &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftPayloadsCamerasList &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftRcLostAction &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftRthInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
// AircraftHomeGeographicalInfo;
class AircraftHomeGeographicalInfo {
public:
    double home_longitude = 0.0;       // double, Home点经度, [-180, 180] 度;
    double home_latitude = 0.0;        // double, Home点纬度, [-90, 90] 度;


    bool operator==(const AircraftHomeGeographicalInfo &rhs) const {
        return
            floatEqual(home_longitude, rhs.home_longitude) &&
            floatEqual(home_latitude, rhs.home_latitude);
    }

    bool operator!=(const AircraftHomeGeographicalInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 机场固件信息;
// DockFirmwareInfo
class DockFirmwareInfo {
public:
    std::string firmware_version;     // 固件版本, such as "00.01.0439";
    int compatible_status = 0;             // 固件一致性, { 0: 一致, 1: 不一致 };
    int firmware_upgrade_status = 0;       // 固件升级状态, { 0: 未升级, 1: 升级中 };

    bool operator==(const DockFirmwareInfo &rhs) const {
        return
            firmware_version == rhs.firmware_version &&
            compatible_status == rhs.compatible_status &&
            firmware_upgrade_status == rhs.firmware_upgrade_status;
    }

    bool operator!=(const DockFirmwareInfo &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const AircraftCurrentRthMode &rhs) const {
        return !(*this == rhs);
    }
};


// HMS: 参数信息;
class HmsArgs {
public:
    int component_index = -1;        // int, 文案变量, 默认无效值(-1);
    int sensor_index = -1;           // int, 文案变量, 默认无效值(-1);
    std::string device_id;           // string, 告警产生的设备ID, 无效值: 空值;
    std::string sender_host_id;      // string, 告警产生的模块ID, 无效值: 空值;
    int ignore = 0;                  // int, 告警是否忽略, {0:不忽略, 1: 忽略};
    int alarm_info = 0;              // int, 模块自定义的告警信息;
    long create_timestamp = 0;       // 故障产生的 UTC 时间(单位秒); 类型与历史 JSON 对齐
    int display_scene = 0;           // int, 0: 正式错误码, 1: 调试错误码, 只用于排障;

    bool operator==(const HmsArgs &rhs) const {
        return
            component_index == rhs.component_index &&
            sensor_index == rhs.sensor_index && device_id == rhs.device_id &&
            sender_host_id == rhs.sender_host_id && ignore == rhs.ignore &&
            alarm_info == rhs.alarm_info &&
            create_timestamp == rhs.create_timestamp &&
            display_scene == rhs.display_scene;
    }

    bool operator!=(const HmsArgs &rhs) const {
        return !(*this == rhs);
    }
};

// HMS: 基础告警信息;
class HmsDataBasics {
public:
    int level = 0;           // int, 告警等级, {"0":"通知","1":"提醒","2":"警告"};
    int module_id = -1;      // int, 事件模块, {"0":"飞行任务","1":"设备管理","2":"媒体","3":"hms"};
    std::string code;        // string, 告警码, {无效值: 空值};
    std::string device_type; // string, 设备类型, 上云API格式:{domain-type-subtype},可以根据产品支持查询, {无效值: 空值};
    std::string domain_type; // string, such as "drone_nest";
    int in_the_sky = 0;      // int, 是否飞行, {"0":"在地上", "1":"在天上"};
    int imminent = 0;        // int, 是否为及时性的, {"0":"否", "1":"是"};
    HmsArgs args;            // HmsArgs, 告警参数信息;
    std::string status_key;  // string;
    std::string joint_key;   // string;
    int id = 0;              // int, 每个设备上报事件里hms的唯一id;
    int parent_id = 0;       // int, 每个设备上报事件里hms的唯一父id;

    bool operator==(const HmsDataBasics &rhs) const {
        return
            level == rhs.level && module_id == rhs.module_id &&
            code == rhs.code && device_type == rhs.device_type &&
            domain_type == rhs.domain_type && in_the_sky == rhs.in_the_sky &&
            imminent == rhs.imminent && args == rhs.args &&
            status_key == rhs.status_key && joint_key == rhs.joint_key &&
            id == rhs.id && parent_id == rhs.parent_id;
    }

    bool operator!=(const HmsDataBasics &rhs) const {
        return !(*this == rhs);
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

    bool operator!=(const HmsDataItem &rhs) const {
        return !(*this == rhs);
    }
};

// HMS 告警信息列表;
class HmsData {
public:
    std::vector<HmsDataItem> hms_data_items;

    bool operator==(const HmsData &rhs) const {
        return hms_data_items == rhs.hms_data_items;
    }

    bool operator!=(const HmsData &rhs) const {
        return !(*this == rhs);
    }
};


// 进离场任务状态;
// topic_task_visual_scout_status;
class AircraftVisualScoutStatus {
public:
    std::string task_id;
    int visual_scout_stage = -1;                   // 默认无效值 -1;
    long scout_wayline_generate_remain_time = -1;  // 默认无效值 -1;

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
    int err_code = -1;  // 当前协议值(>= 0), 默认无效值 -1;

    bool operator==(const AircraftVisualScoutAbnormalInfo &rhs) const {
        return
            task_id == rhs.task_id &&
            err_code == rhs.err_code;
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
    bool obstacle_tag = false;   // 数据成员必须带上默认值, 这里默认设为 false;

    bool operator==(const AircraftVisualScoutObstacleInfo &rhs) const {
        return
            task_id == rhs.task_id &&
            obstacle_tag == rhs.obstacle_tag;
    }

    bool operator!=(const AircraftVisualScoutObstacleInfo &rhs) const {
        return !(*this == rhs);
    }
};


// 星历下载状态;
// topic_task_ephemeris_download_state;
class EphemerisDownloadState {
public:
    int state = -1;  // 当前协议值(>= 0), 默认无效值 -1;

    bool operator==(const EphemerisDownloadState &rhs) const {
        return state == rhs.state;
    }
    
    bool operator!=(const EphemerisDownloadState &rhs) const {
        return !(rhs == *this);
    }
};


// 位置信息;
class PositionData {
public:
    double lat = 0.0;         // 纬度;
    double lng = 0.0;         // 经度;
    double abs_alt = 0.0;     // 高度;
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


// 星历信息;
// topic_drone_ephemeris_info;
class EphemerisInfo {
public:
    std::string ephemeris;

    bool operator==(const EphemerisInfo &rhs) const {
        return ephemeris == rhs.ephemeris;
    }

    bool operator!=(const EphemerisInfo &rhs) const {
        return !(rhs == *this);
    }
};


// "topic_media_file_device_upload";
// 设备媒体回传通知;
class MediaFileDeviceUpload {
public:
    int64_t file_id = -1;                // 文件 id;
    std::string flight_id;               // 飞行任务 id;
    std::string device_sn;               // 设备 SN;
    std::string object_key;              // 文件对象 key;
    int64_t object_size = 0;             // 文件大小(字节);
    int file_type = -1;                  // 文件类型，2：图片、3：视频;
    
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
    double length_in_35mm_film = -1;  // 35mm胶片等效焦距，示例值：50;

    std::string image_type;           // 图片类型; {广角:"WideCamera", 变焦:"ZoomCamera", 红外:"InfraredCamera", 全景:"Panorama"}
    std::string payload_type;
    std::string action_uuid;
    PositionData photo_position;
    PositionData shoot_position;

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


class MediaFileVideoMetaData {
public:
    double duration = -1;           // 视频时长(单位：秒)，示例值：26.927;
    std::string encoder;            // 视频编码器，示例值：Lavf58.12.100;
    std::string format_long_name;   // 视频格式名称，示例值：QuickTime / MOV;
    std::string format_name;        // 示例值："mov,mp4,m4a,3gp,3g2,mj2";
    int height = -1;
    int width = -1;
    
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
    int64_t file_id = -1;               // 文件 id;
    std::string flight_id;              // 飞行任务 id;
    std::string device_sn;              // 设备 SN;
    std::string object_key;             // 文件对象 key(媒体文件在对象存储中的唯一标识);
    int64_t object_size = 0;            // 文件大小(字节);
    int file_type = -1;                 // 文件类型，2：图片、3：视频;
    std::string preview_object_key;     // 预览文件对象 key(媒体文件在对象存储中的唯一标识);

    MediaFileImageMetaData image_metadata;
    MediaFileVideoMetaData video_metadata;

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



// ImportWaylineEvent
class PointsItem {
public:
    double lng = 0.0;
    double lat = 0.0;
    double alt = 0.0;

    bool operator==(const PointsItem &other) const {
        return
            floatEqual(lat, other.lat) &&
            floatEqual(lng, other.lng) &&
            floatEqual(alt, other.alt);
    }
};

class SurveyAreaScope {
public:
    std::vector<PointsItem> points;

    bool operator==(const SurveyAreaScope &other) const {
        return points == other.points;
    }
};

class KmzData {
public:
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
    SurveyAreaScope survey_area_scope;

    bool operator==(const KmzData &other) const {
        return wayline_type == other.wayline_type &&
               wayline_point_count == other.wayline_point_count &&
               exit_on_rc_lost_type == other.exit_on_rc_lost_type &&
               exit_on_rc_lost_behavior == other.exit_on_rc_lost_behavior &&
               start_record_action_count == other.start_record_action_count &&
               stop_record_action_count == other.stop_record_action_count &&
               ortho_camera_overlap_h == other.ortho_camera_overlap_h &&
               ortho_camera_overlap_w == other.ortho_camera_overlap_w &&
               inclined_camera_overlap_h == other.inclined_camera_overlap_h &&
               inclined_camera_overlap_w == other.inclined_camera_overlap_w &&
               supplement_prism_model_count == other.supplement_prism_model_count &&
               estimated_photo_count == other.estimated_photo_count &&
               floatEqual(duration, other.duration) &&
               floatEqual(distance, other.distance) &&
               floatEqual(survey_area, other.survey_area) &&
               floatEqual(start_latitude, other.start_latitude) &&
               floatEqual(start_longitude, other.start_longitude) &&
               floatEqual(start_altitude, other.start_altitude) &&
               floatEqual(margin_distance, other.margin_distance) &&
               floatEqual(ortho_gsd, other.ortho_gsd) &&
               floatEqual(incline_gsd, other.incline_gsd) &&
               is_wayline_validity_check == other.is_wayline_validity_check &&
               has_oriented_action == other.has_oriented_action &&
               has_accurate_action == other.has_accurate_action &&
               is_surface_fly_on == other.is_surface_fly_on &&
               is_elevation_optimization_on == other.is_elevation_optimization_on &&
               is_smart_oblique_on == other.is_smart_oblique_on &&
               is_mapping_strip_use_template_altitude == other.is_mapping_strip_use_template_altitude &&
               is_with_megaphone == other.is_with_megaphone &&
               is_with_search_light == other.is_with_search_light &&
               is_ai_wayline == other.is_ai_wayline &&
               drone_type == other.drone_type &&
               target_detect_area_file_path == other.target_detect_area_file_path &&
               wayline_validity_check_codes == other.wayline_validity_check_codes &&
               elevation_file_names == other.elevation_file_names &&
               payload_types == other.payload_types &&
               payload_types_with_index == other.payload_types_with_index &&
               accurate_action_uuids == other.accurate_action_uuids &&
               action_uuids == other.action_uuids &&
               res_file_md5s == other.res_file_md5s &&
               audio_file_md5s == other.audio_file_md5s &&
               target_detect_action_uuids == other.target_detect_action_uuids &&
               ai_audio_file_md5s == other.ai_audio_file_md5s &&
               action_uuid_to_md5_map == other.action_uuid_to_md5_map &&
               audio_action_uuid_to_file_path_map == other.audio_action_uuid_to_file_path_map &&
               ai_audio_action_uuid_to_file_path_map == other.ai_audio_action_uuid_to_file_path_map &&
               survey_area_scope == other.survey_area_scope;
    }
};

class ExtData {
public:
    std::string kmz_without_res_object_key;
    std::string kmz_without_res_md5;
    std::string target_area_file_object_key;
    std::string business_file_object_key;
    std::vector<std::string> model_file_object_keys;

    bool operator==(const ExtData &other) const {
        return kmz_without_res_object_key == other.kmz_without_res_object_key &&
               kmz_without_res_md5 == other.kmz_without_res_md5 &&
               target_area_file_object_key == other.target_area_file_object_key &&
               business_file_object_key == other.business_file_object_key &&
               model_file_object_keys == other.model_file_object_keys;
    }
};

class ImportWaylineEvent {
public:
    std::string async_request_id;
    long long timestamp = 0;
    std::string kmz_object_key;
    std::string wayline_uuid;
    int code = 0;
    std::string message;
    KmzData kmz_data;
    ExtData ext_data;

    bool operator==(const ImportWaylineEvent &other) const {
        return async_request_id == other.async_request_id &&
               timestamp == other.timestamp &&
               kmz_object_key == other.kmz_object_key &&
               wayline_uuid == other.wayline_uuid &&
               code == other.code &&
               message == other.message &&
               kmz_data == other.kmz_data &&
               ext_data == other.ext_data;
    }
};

// ─────────────────────────────────────────────────────────
// "topic_reconstruction_status_changed_event"
// 重建任务状态变更推送
// ─────────────────────────────────────────────────────────

/// 单个重建任务信息
class ReconJobInfo {
public:
    int type = 0;           // 任务类型（14=2D, 15=3D, 24=3DGS, 25=Lidar, 12=SfM, 23=ZIP 等）
    std::string uuid;       // 异步平台实例 ID
    int job_status = 0;     // 1=申请中, 3=执行中, 6=成功, 7=失败, 8=停止中
    int percent = 0;        // 进度百分比（0-100）
    int code = 0;           // 错误码（omitempty）

    bool operator==(const ReconJobInfo &o) const {
        return type == o.type && uuid == o.uuid && job_status == o.job_status
            && percent == o.percent && code == o.code;
    }
    bool operator!=(const ReconJobInfo &o) const { return !(*this == o); }
};

/// 重建任务状态变更消息
class ReconstructionStatusChanged {
public:
    std::string workspace_id;                   // 项目 UUID
    uint64_t id = 0;                            // 文件 ID
    std::string name;                           // 文件名
    int state = 0;                              // 模型状态码
    std::string uuid;                           // 文件 UUID
    int file_type = 0;                          // 文件类型
    int sub_file_type = 0;                      // 子文件类型
    int code = 0;                               // 错误码，仅失败时填充
    int64_t size = 0;                           // 文件大小（bytes）
    ReconJobInfo job_info;                      // 当前触发的任务信息
    std::vector<ReconJobInfo> job_infos;        // 所有关联任务列表
    uint32_t fast_reconstruction_id = 0;        // 快速重建关联 ID

    bool operator==(const ReconstructionStatusChanged &o) const {
        return workspace_id == o.workspace_id
            && id == o.id
            && name == o.name
            && state == o.state
            && uuid == o.uuid
            && file_type == o.file_type
            && sub_file_type == o.sub_file_type
            && code == o.code
            && size == o.size
            && job_info == o.job_info
            && job_infos == o.job_infos
            && fast_reconstruction_id == o.fast_reconstruction_id;
    }
    bool operator!=(const ReconstructionStatusChanged &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "topic_atc_command_task_event"
// 指令任务事件
// ─────────────────────────────────────────────────────────

class CommandTaskData {
public:
    int id = 0;
    std::string created_at;
    std::string uuid;
    std::string prj_uuid;
    std::string sn;
    std::string drone_sn;
    std::string track_id;
    std::string user_id;
    int status = 0;
    int err_code = 0;
    std::string takeoff_mode;
    double security_takeoff_height = 0.0;
    int max_speed = 0;
    int out_of_control_action = 0;
    int rth_altitude = 0;
    int commander_flight_mode = 0;
    double commander_flight_height = 0.0;
    int rth_mode = 0;
    std::string cloud_to_cloud_id;

    bool operator==(const CommandTaskData &o) const {
        return id == o.id && uuid == o.uuid && sn == o.sn && drone_sn == o.drone_sn
            && status == o.status && err_code == o.err_code;
    }
    bool operator!=(const CommandTaskData &o) const { return !(*this == o); }
};

class CommandTaskEvent {
public:
    std::string event_id;
    std::string action;
    std::string uuid;
    int64_t timestamp = 0;
    CommandTaskData task_data;

    bool operator==(const CommandTaskEvent &o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
    bool operator!=(const CommandTaskEvent &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "topic_atc_flyto_task_event"
// 指点飞行任务事件
// ─────────────────────────────────────────────────────────

class FlyToWayPoint {
public:
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    bool operator==(const FlyToWayPoint &o) const {
        return floatEqual(latitude, o.latitude) && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height);
    }
    bool operator!=(const FlyToWayPoint &o) const { return !(*this == o); }
};

class FlyToTaskData {
public:
    int id = 0;
    std::string uuid;
    std::string prj_uuid;
    std::string sn;
    std::string drone_sn;
    std::string user_id;
    int seq_no = 0;
    std::string flight_task_uuid;
    int flight_task_type = 0;
    int status = 0;
    int err_code = 0;
    int way_point_index = 0;
    std::vector<FlyToWayPoint> way_points;
    std::vector<FlyToWayPoint> planned_path_points;
    double remaining_distance = 0.0;
    double remaining_time = 0.0;

    bool operator==(const FlyToTaskData &o) const {
        return id == o.id && uuid == o.uuid && sn == o.sn && drone_sn == o.drone_sn
            && status == o.status && err_code == o.err_code;
    }
    bool operator!=(const FlyToTaskData &o) const { return !(*this == o); }
};

class FlyToTaskEvent {
public:
    std::string event_id;
    std::string action;
    std::string uuid;
    int64_t timestamp = 0;
    FlyToTaskData task_data;

    bool operator==(const FlyToTaskEvent &o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
    bool operator!=(const FlyToTaskEvent &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "topic_atc_poi_task_event"
// POI 任务事件
// ─────────────────────────────────────────────────────────

class PoiCenterPoint {
public:
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    bool operator==(const PoiCenterPoint &o) const {
        return floatEqual(latitude, o.latitude) && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height);
    }
    bool operator!=(const PoiCenterPoint &o) const { return !(*this == o); }
};

class PoiTaskData {
public:
    std::string uuid;
    std::string sn;
    std::string device_sn;
    PoiCenterPoint poi_center_point;
    double speed = 0.0;
    double circle_radius = 0.0;
    std::string payload_index;
    int status = 0;
    int err_code = 0;

    bool operator==(const PoiTaskData &o) const {
        return uuid == o.uuid && sn == o.sn && device_sn == o.device_sn
            && status == o.status && err_code == o.err_code;
    }
    bool operator!=(const PoiTaskData &o) const { return !(*this == o); }
};

class PoiTaskEvent {
public:
    std::string event_id;
    std::string action;
    std::string uuid;
    int64_t timestamp = 0;
    PoiTaskData task_data;

    bool operator==(const PoiTaskEvent &o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
    bool operator!=(const PoiTaskEvent &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "topic_atc_wayline_task_event"
// 航线任务事件
// ─────────────────────────────────────────────────────────

class WaylineBreakPoint {
public:
    int index = 0;
    int state = 0;
    double progress = 0.0;
    int wayline_id = 0;
    int break_reason = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    double attitude_head = 0.0;

    bool operator==(const WaylineBreakPoint &o) const {
        return index == o.index && state == o.state && wayline_id == o.wayline_id
            && break_reason == o.break_reason;
    }
    bool operator!=(const WaylineBreakPoint &o) const { return !(*this == o); }
};

class WaylineObstacle {
public:
    int id = 0;
    int type = 0;
    int64_t timestamp = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    int wayline_id = 0;
    int waypoint_index = 0;

    bool operator==(const WaylineObstacle &o) const {
        return id == o.id && type == o.type && wayline_id == o.wayline_id;
    }
    bool operator!=(const WaylineObstacle &o) const { return !(*this == o); }
};

class WaylineTaskData {
public:
    std::string name;
    std::string uuid;
    int task_type = 0;
    int status = 0;
    std::string sn;
    std::string landing_dock_sn;
    std::string wayline_uuid;
    int wayline_precision_type = 0;
    double wayline_duration = 0.0;
    std::vector<int> wayline_segment_indexes;
    int total_waypoints = 0;
    int current_waypoint_index = 0;
    std::string bid;
    std::string begin_at;
    std::string end_at;
    std::string run_at;
    std::string completed_at;
    int out_of_control_action_in_flight = 0;
    int rth_altitude = 0;
    int rth_mode = 0;
    int min_battery_capacity = 0;
    std::vector<std::string> tags;
    std::string cloud_to_cloud_id;
    WaylineBreakPoint break_point;
    bool is_break_point_resume = false;
    int media_upload_status = 0;
    bool fast_reconstruction = false;
    int device_type = 0;
    std::vector<WaylineObstacle> obstacles;
    std::string created_at;
    std::string updated_at;
    std::string deleted_at;

    bool operator==(const WaylineTaskData &o) const {
        return uuid == o.uuid && sn == o.sn && status == o.status && task_type == o.task_type;
    }
    bool operator!=(const WaylineTaskData &o) const { return !(*this == o); }
};

class WaylineTaskEvent {
public:
    std::string event_id;
    std::string action;
    std::string uuid;
    int64_t timestamp = 0;
    WaylineTaskData task_data;

    bool operator==(const WaylineTaskEvent &o) const {
        return event_id == o.event_id && action == o.action && uuid == o.uuid
            && timestamp == o.timestamp && task_data == o.task_data;
    }
    bool operator!=(const WaylineTaskEvent &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "in_flight_wayline_task_notify"
// 空中航线任务进度推送
// ─────────────────────────────────────────────────────────

class InFlightWaylineTaskProgressPercent {
public:
    int percent = 0;

    bool operator==(const InFlightWaylineTaskProgressPercent &o) const {
        return percent == o.percent;
    }
    bool operator!=(const InFlightWaylineTaskProgressPercent &o) const { return !(*this == o); }
};

class InFlightWaylineTaskNotify {
public:
    std::string in_flight_wayline_id;
    InFlightWaylineTaskProgressPercent progress;
    int status = 0;
    int result = 0;
    std::string gateway_sn;
    int way_point_index = 0;
    double wayline_duration = 0.0;
    double wayline_length = 0.0;
    std::string task_name;
    int64_t timestamp = 0;

    bool operator==(const InFlightWaylineTaskNotify &o) const {
        return in_flight_wayline_id == o.in_flight_wayline_id
            && progress == o.progress
            && status == o.status
            && result == o.result
            && gateway_sn == o.gateway_sn
            && way_point_index == o.way_point_index
            && wayline_duration == o.wayline_duration
            && wayline_length == o.wayline_length
            && task_name == o.task_name
            && timestamp == o.timestamp;
    }
    bool operator!=(const InFlightWaylineTaskNotify &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────
// 固件升级进度 (topic_device_ota_progress)
// ─────────────────────────────────────────────────────────

class DeviceOtaProgressInfo {
public:
    int percent = 0;
    int current_step = 0;
    std::string ext;

    bool operator==(const DeviceOtaProgressInfo &o) const {
        return percent == o.percent && current_step == o.current_step && ext == o.ext;
    }
    bool operator!=(const DeviceOtaProgressInfo &o) const { return !(*this == o); }
};

class DeviceOtaProgress {
public:
    std::string bid;
    int64_t timestamp = 0;
    std::string status;
    int result = 0;
    DeviceOtaProgressInfo progress;

    bool operator==(const DeviceOtaProgress &o) const {
        return bid == o.bid && timestamp == o.timestamp && status == o.status
            && result == o.result && progress == o.progress;
    }
    bool operator!=(const DeviceOtaProgress &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 设备异常报告上传进度 (topic_device_fileupload_progress)
// ─────────────────────────────────────────────────────────

class DeviceFileUploadFileProgress {
public:
    int percent = 0;
    int current_step = 0;
    std::string status;
    int total_step = 0;
    int64_t upload_rate = 0;

    bool operator==(const DeviceFileUploadFileProgress &o) const {
        return percent == o.percent && current_step == o.current_step
            && status == o.status && total_step == o.total_step
            && upload_rate == o.upload_rate;
    }
    bool operator!=(const DeviceFileUploadFileProgress &o) const { return !(*this == o); }
};

class DeviceFileUploadFileInfo {
public:
    std::string device_sn;
    std::string key;
    std::string module;
    DeviceFileUploadFileProgress progress;
    int64_t size = 0;

    bool operator==(const DeviceFileUploadFileInfo &o) const {
        return device_sn == o.device_sn && key == o.key && module == o.module
            && progress == o.progress && size == o.size;
    }
    bool operator!=(const DeviceFileUploadFileInfo &o) const { return !(*this == o); }
};

class DeviceFileUploadProgress {
public:
    std::string bid;
    int64_t timestamp = 0;
    std::string status;
    int result = 0;
    std::vector<DeviceFileUploadFileInfo> files;

    bool operator==(const DeviceFileUploadProgress &o) const {
        return bid == o.bid && timestamp == o.timestamp && status == o.status
            && result == o.result && files == o.files;
    }
    bool operator!=(const DeviceFileUploadProgress &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// PSDK喊话器音频播放进度 (topic_psdk_speaker_audio_play_start_progress)
// PSDK喊话器TTS播放进度 (topic_psdk_speaker_tts_play_start_progress)
// ─────────────────────────────────────────────────────────

class PsdkSpeakerProgressInfo {
public:
    int percent = 0;
    std::string step_key;

    bool operator==(const PsdkSpeakerProgressInfo &o) const {
        return percent == o.percent && step_key == o.step_key;
    }
    bool operator!=(const PsdkSpeakerProgressInfo &o) const { return !(*this == o); }
};

class PsdkSpeakerOutputInfo {
public:
    std::string md5;
    PsdkSpeakerProgressInfo progress;
    int psdk_index = 0;
    std::string status;

    bool operator==(const PsdkSpeakerOutputInfo &o) const {
        return md5 == o.md5 && progress == o.progress
            && psdk_index == o.psdk_index && status == o.status;
    }
    bool operator!=(const PsdkSpeakerOutputInfo &o) const { return !(*this == o); }
};

class PsdkSpeakerPlayProgress {
public:
    std::string gateway;
    PsdkSpeakerOutputInfo output;
    int result = 0;
    std::string sn;

    bool operator==(const PsdkSpeakerPlayProgress &o) const {
        return gateway == o.gateway && output == o.output
            && result == o.result && sn == o.sn;
    }
    bool operator!=(const PsdkSpeakerPlayProgress &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// POI飞行进度通知 (topic_task_flight_poi_progress)
// ─────────────────────────────────────────────────────────

class FlightPoiProgressCenterPoint {
public:
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    bool operator==(const FlightPoiProgressCenterPoint &o) const {
        return floatEqual(latitude, o.latitude)
            && floatEqual(longitude, o.longitude)
            && floatEqual(height, o.height);
    }
    bool operator!=(const FlightPoiProgressCenterPoint &o) const { return !(*this == o); }
};

class FlightPoiProgress {
public:
    std::string uuid;
    std::string sn;
    std::string device_sn;
    FlightPoiProgressCenterPoint poi_center_point;
    double speed = 0.0;
    double circle_radius = 0.0;
    std::string payload_index;
    int status = 0;
    int err_code = 0;

    bool operator==(const FlightPoiProgress &o) const {
        return uuid == o.uuid && sn == o.sn && device_sn == o.device_sn
            && poi_center_point == o.poi_center_point
            && floatEqual(speed, o.speed)
            && floatEqual(circle_radius, o.circle_radius)
            && payload_index == o.payload_index
            && status == o.status && err_code == o.err_code;
    }
    bool operator!=(const FlightPoiProgress &o) const { return !(*this == o); }
};
// ─────────────────────────────────────────────────────────
// "topic_element_create" / "topic_element_update" / "topic_element_delete"
// ─────────────────────────────────────────────────────────
class ElementCreate {
public:
    std::string id;
    std::string group_id;
    std::string name;
    int display = 0;

    bool operator==(const ElementCreate &o) const {
        return id == o.id && group_id == o.group_id && name == o.name && display == o.display;
    }
    bool operator!=(const ElementCreate &o) const { return !(*this == o); }
};

class ElementUpdate {
public:
    std::string id;
    std::string group_id;
    std::string name;
    int display = 0;

    bool operator==(const ElementUpdate &o) const {
        return id == o.id && group_id == o.group_id && name == o.name && display == o.display;
    }
    bool operator!=(const ElementUpdate &o) const { return !(*this == o); }
};

class ElementDelete {
public:
    std::string id;
    std::string group_id;

    bool operator==(const ElementDelete &o) const {
        return id == o.id && group_id == o.group_id;
    }
    bool operator!=(const ElementDelete &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "topic_task_topo_changed"
// 漫游任务 topo 变更推送
// ─────────────────────────────────────────────────────────

class TaskTopoChanged {
public:
    std::string drone_sn;
    std::string flight_id;
    std::string takeoff_sn;

    bool operator==(const TaskTopoChanged &o) const {
        return drone_sn == o.drone_sn && flight_id == o.flight_id && takeoff_sn == o.takeoff_sn;
    }
    bool operator!=(const TaskTopoChanged &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "reconstruction.unzip_progress"
// 模型文件解压进度推送
// ─────────────────────────────────────────────────────────

/// 模型文件解压进度消息
class TerraUnzipProgress {
public:
    long long file_id = 0;  // 关联的 File 记录 ID
    std::string uuid;       // 关联的 File 记录 UUID
    std::string name;       // 文件名
    double percent = 0.0;   // 解压进度百分比 [0, 100]

    bool operator==(const TerraUnzipProgress &o) const {
        return file_id == o.file_id && uuid == o.uuid
            && name == o.name && floatEqual(percent, o.percent);
    }
    bool operator!=(const TerraUnzipProgress &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "model.status_changed"
// 模型状态变更推送
// ─────────────────────────────────────────────────────────

/// 模型状态变更消息
class TerraModelStatusChanged {
public:
    long long file_id = 0;  // 关联的 File 记录 ID
    std::string uuid;       // 关联的 File 记录 UUID
    std::string name;       // 文件名
    int state = 0;          // 模型新状态（1-解压中 2-成功 3-失败 5-转换中 6-转换成功 7-转换失败）
    int file_type = 0;      // 文件类型
    int sub_file_type = 0;  // 子文件类型
    int error_code = 0;     // 错误码（状态失败时携带，0 表示无错误）

    bool operator==(const TerraModelStatusChanged &o) const {
        return file_id == o.file_id && uuid == o.uuid && name == o.name
            && state == o.state && file_type == o.file_type
            && sub_file_type == o.sub_file_type && error_code == o.error_code;
    }
    bool operator!=(const TerraModelStatusChanged &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// "media.file_show_on_map"
// 文件展示地图状态变更推送
// ─────────────────────────────────────────────────────────

/// 展示地图文件项
class TerraFileShowOnMapItem {
public:
    long long id = 0;       // File 记录 ID
    std::string name;       // 文件名
    int file_type = 0;      // 文件类型
    int sub_file_type = 0;  // 子文件类型
    std::string preview_url; // 预览图签名 URL

    bool operator==(const TerraFileShowOnMapItem &o) const {
        return id == o.id && name == o.name && file_type == o.file_type
            && sub_file_type == o.sub_file_type && preview_url == o.preview_url;
    }
    bool operator!=(const TerraFileShowOnMapItem &o) const { return !(*this == o); }
};

/// 文件展示地图状态消息
class TerraFileShowOnMap {
public:
    std::vector<TerraFileShowOnMapItem> list;   // 展示到地图的文件列表

    bool operator==(const TerraFileShowOnMap &o) const { return list == o.list; }
    bool operator!=(const TerraFileShowOnMap &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────────────────
// "topic_media_file_hide_on_map"  模型隐藏地图推送（来自 file-storage）
// ─────────────────────────────────────────────────────────────────────

/// 模型隐藏地图消息（Delete/BatchDelete 触发）
class TerraMediaFileHideOnMap {
public:
    std::vector<int> ids;   ///< 被隐藏的模型文件 ID 列表（uint32）

    bool operator==(const TerraMediaFileHideOnMap &o) const { return ids == o.ids; }
    bool operator!=(const TerraMediaFileHideOnMap &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────────────────
// "topic_media_file_show_on_map"  模型显示地图推送（来自 file-storage）
// ─────────────────────────────────────────────────────────────────────

/// 单个上图模型文件摘要
class TerraMediaFileShowOnMapItem {
public:
    std::string name;          ///< 文件名
    int id = 0;                ///< 文件 ID（uint32）
    double shoot_lat = 0.0;
    double shoot_lng = 0.0;
    double shoot_abs_alt = 0.0;
    double shoot_rel_alt = 0.0;
    double photo_lat = 0.0;
    double photo_lng = 0.0;
    double photo_abs_alt = 0.0;
    double photo_rel_alt = 0.0;
    std::string preview_url;
    int file_type = 0;
    int sub_file_type = 0;

    bool operator==(const TerraMediaFileShowOnMapItem &o) const {
        return id == o.id && name == o.name && preview_url == o.preview_url
            && file_type == o.file_type && sub_file_type == o.sub_file_type
            && std::abs(shoot_lat    - o.shoot_lat)    < 1e-9
            && std::abs(shoot_lng    - o.shoot_lng)    < 1e-9
            && std::abs(shoot_abs_alt - o.shoot_abs_alt) < 1e-9
            && std::abs(shoot_rel_alt - o.shoot_rel_alt) < 1e-9
            && std::abs(photo_lat    - o.photo_lat)    < 1e-9
            && std::abs(photo_lng    - o.photo_lng)    < 1e-9
            && std::abs(photo_abs_alt - o.photo_abs_alt) < 1e-9
            && std::abs(photo_rel_alt - o.photo_rel_alt) < 1e-9;
    }
    bool operator!=(const TerraMediaFileShowOnMapItem &o) const { return !(*this == o); }
};

/// 模型显示地图消息（BatchShowOnMap 触发）
class TerraMediaFileShowOnMap {
public:
    std::vector<TerraMediaFileShowOnMapItem> list;

    bool operator==(const TerraMediaFileShowOnMap &o) const { return list == o.list; }
    bool operator!=(const TerraMediaFileShowOnMap &o) const { return !(*this == o); }
};

// ═══════════════════════════════════════════════════════════════════════
// EA340 消息推送补全 · Phase A (koc3_EA340支持/00_消息推送补全)
// ═══════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────
// 机场直播状态推送 (topic_dock_live_status)
// ─────────────────────────────────────────────────────────

class LiveStatusItem {
public:
    std::string video_id;
    std::string video_type;
    int64_t video_quality = 0;
    int64_t status = 0;
    int64_t error_status = 0;

    bool operator==(const LiveStatusItem &o) const {
        return video_id == o.video_id && video_type == o.video_type
            && video_quality == o.video_quality && status == o.status
            && error_status == o.error_status;
    }
    bool operator!=(const LiveStatusItem &o) const { return !(*this == o); }
};

class DockLiveStatus {
public:
    std::vector<LiveStatusItem> live_status;

    bool operator==(const DockLiveStatus &o) const { return live_status == o.live_status; }
    bool operator!=(const DockLiveStatus &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 机场直播能力推送 (topic_dock_live_capacity)
// ─────────────────────────────────────────────────────────

class LiveCapacityVideo {
public:
    std::string video_index;
    std::string video_type;
    std::vector<std::string> switchable_video_types;

    bool operator==(const LiveCapacityVideo &o) const {
        return video_index == o.video_index && video_type == o.video_type
            && switchable_video_types == o.switchable_video_types;
    }
    bool operator!=(const LiveCapacityVideo &o) const { return !(*this == o); }
};

class LiveCapacityCamera {
public:
    std::string camera_index;
    int64_t available_video_number = 0;
    int64_t coexist_video_number_max = 0;
    std::vector<LiveCapacityVideo> video_list;

    bool operator==(const LiveCapacityCamera &o) const {
        return camera_index == o.camera_index
            && available_video_number == o.available_video_number
            && coexist_video_number_max == o.coexist_video_number_max
            && video_list == o.video_list;
    }
    bool operator!=(const LiveCapacityCamera &o) const { return !(*this == o); }
};

class LiveCapacityDevice {
public:
    std::string sn;
    int64_t available_video_number = 0;
    int64_t coexist_video_number_max = 0;
    std::vector<LiveCapacityCamera> camera_list;

    bool operator==(const LiveCapacityDevice &o) const {
        return sn == o.sn
            && available_video_number == o.available_video_number
            && coexist_video_number_max == o.coexist_video_number_max
            && camera_list == o.camera_list;
    }
    bool operator!=(const LiveCapacityDevice &o) const { return !(*this == o); }
};

class DockLiveCapacity {
public:
    int64_t available_video_number = 0;
    int64_t coexist_video_number_max = 0;
    std::vector<LiveCapacityDevice> device_list;

    bool operator==(const DockLiveCapacity &o) const {
        return available_video_number == o.available_video_number
            && coexist_video_number_max == o.coexist_video_number_max
            && device_list == o.device_list;
    }
    bool operator!=(const DockLiveCapacity &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 4G Dongle 信息推送 (topic_dock_dongle_infos / topic_drone_dongle_infos)
// ─────────────────────────────────────────────────────────

class EsimInfoItem {
public:
    bool enabled = false;
    std::string iccid;
    int64_t telecom_operator = 0;

    bool operator==(const EsimInfoItem &o) const {
        return enabled == o.enabled && iccid == o.iccid && telecom_operator == o.telecom_operator;
    }
    bool operator!=(const EsimInfoItem &o) const { return !(*this == o); }
};

class SimInfoItem {
public:
    std::string iccid;
    int64_t telecom_operator = 0;

    bool operator==(const SimInfoItem &o) const {
        return iccid == o.iccid && telecom_operator == o.telecom_operator;
    }
    bool operator!=(const SimInfoItem &o) const { return !(*this == o); }
};

class DongleInfoItem {
public:
    int64_t dongle_type = 0;
    std::string eid;
    int64_t esim_activate_state = 0;
    std::vector<EsimInfoItem> esim_infos;
    std::string imei;
    int64_t sim_card_state = 0;
    int64_t sim_slot = 0;
    SimInfoItem sim_info;
    int64_t telecom_operator = 0;

    bool operator==(const DongleInfoItem &o) const {
        return dongle_type == o.dongle_type && eid == o.eid
            && esim_activate_state == o.esim_activate_state
            && esim_infos == o.esim_infos && imei == o.imei
            && sim_card_state == o.sim_card_state && sim_slot == o.sim_slot
            && sim_info == o.sim_info && telecom_operator == o.telecom_operator;
    }
    bool operator!=(const DongleInfoItem &o) const { return !(*this == o); }
};

class DockDongleInfos {
public:
    std::vector<DongleInfoItem> dongle_infos;

    bool operator==(const DockDongleInfos &o) const { return dongle_infos == o.dongle_infos; }
    bool operator!=(const DockDongleInfos &o) const { return !(*this == o); }
};

class DroneDongleInfos {
public:
    std::vector<DongleInfoItem> dongle_infos;

    bool operator==(const DroneDongleInfos &o) const { return dongle_infos == o.dongle_infos; }
    bool operator!=(const DroneDongleInfos &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 机场空调工作模式推送 (topic_dock_air_conditioner_mode)
// ─────────────────────────────────────────────────────────

class DockAirConditionerMode {
public:
    int64_t air_conditioner_mode = 0;

    bool operator==(const DockAirConditionerMode &o) const { return air_conditioner_mode == o.air_conditioner_mode; }
    bool operator!=(const DockAirConditionerMode &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 限飞数据库版本推送 (topic_drone_flysafe_database_version)
// ─────────────────────────────────────────────────────────

class DroneFlysafeDatabaseVersion {
public:
    std::string flysafe_database_version;

    bool operator==(const DroneFlysafeDatabaseVersion &o) const { return flysafe_database_version == o.flysafe_database_version; }
    bool operator!=(const DroneFlysafeDatabaseVersion &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// PSDK UI 资源推送 (topic_drone_psdk_ui_resource)
// ─────────────────────────────────────────────────────────

class PsdkUiResourceItem {
public:
    int64_t psdk_index = 0;
    int64_t psdk_ready = 0;
    std::string object_key;

    bool operator==(const PsdkUiResourceItem &o) const {
        return psdk_index == o.psdk_index && psdk_ready == o.psdk_ready && object_key == o.object_key;
    }
    bool operator!=(const PsdkUiResourceItem &o) const { return !(*this == o); }
};

class DronePsdkUiResource {
public:
    std::vector<PsdkUiResourceItem> psdk_ui_resource;

    bool operator==(const DronePsdkUiResource &o) const { return psdk_ui_resource == o.psdk_ui_resource; }
    bool operator!=(const DronePsdkUiResource &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 飞行器进入当前状态原因推送 (topic_drone_mode_code_reason)
// ─────────────────────────────────────────────────────────

class DroneModeCodeReason {
public:
    int64_t mode_code_reason = 0;

    bool operator==(const DroneModeCodeReason &o) const { return mode_code_reason == o.mode_code_reason; }
    bool operator!=(const DroneModeCodeReason &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 指点飞行模式相关推送
// ─────────────────────────────────────────────────────────

class DroneCommanderFlightMode {
public:
    int64_t commander_flight_mode = 0;

    bool operator==(const DroneCommanderFlightMode &o) const { return commander_flight_mode == o.commander_flight_mode; }
    bool operator!=(const DroneCommanderFlightMode &o) const { return !(*this == o); }
};

class DroneCurrentCommanderFlightMode {
public:
    int64_t current_commander_flight_mode = 0;

    bool operator==(const DroneCurrentCommanderFlightMode &o) const { return current_commander_flight_mode == o.current_commander_flight_mode; }
    bool operator!=(const DroneCurrentCommanderFlightMode &o) const { return !(*this == o); }
};

class DroneCommanderModeLostAction {
public:
    int64_t commander_mode_lost_action = 0;

    bool operator==(const DroneCommanderModeLostAction &o) const { return commander_mode_lost_action == o.commander_mode_lost_action; }
    bool operator!=(const DroneCommanderModeLostAction &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 航线解析库版本推送 (topic_drone_wpmz_version)
// ─────────────────────────────────────────────────────────

class DroneWpmzVersion {
public:
    std::string wpmz_version;

    bool operator==(const DroneWpmzVersion &o) const { return wpmz_version == o.wpmz_version; }
    bool operator!=(const DroneWpmzVersion &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 离线地图开关推送 (topic_drone_offline_map_enable)
// ─────────────────────────────────────────────────────────

class DroneOfflineMapEnable {
public:
    bool offline_map_enable = false;

    bool operator==(const DroneOfflineMapEnable &o) const { return offline_map_enable == o.offline_map_enable; }
    bool operator!=(const DroneOfflineMapEnable &o) const { return !(*this == o); }
};

// ─────────────────────────────────────────────────────────
// 飞机相机水印设置推送 (topic_drone_camera_watermark_settings)
// 注意: 与 DRC CameraWatermarkSettings 不同 (无 payload_index / manufacturer_enable), 不要复用
// ─────────────────────────────────────────────────────────

class DroneCameraWatermarkSettingsPush {
public:
    int64_t global_enable = 0;
    int64_t drone_type_enable = 0;
    int64_t drone_sn_enable = 0;
    int64_t datetime_enable = 0;
    int64_t gps_enable = 0;
    int64_t user_custom_string_enable = 0;
    std::string user_custom_string;
    int64_t layout = 0;

    bool operator==(const DroneCameraWatermarkSettingsPush &o) const {
        return global_enable == o.global_enable && drone_type_enable == o.drone_type_enable
            && drone_sn_enable == o.drone_sn_enable && datetime_enable == o.datetime_enable
            && gps_enable == o.gps_enable && user_custom_string_enable == o.user_custom_string_enable
            && user_custom_string == o.user_custom_string && layout == o.layout;
    }
    bool operator!=(const DroneCameraWatermarkSettingsPush &o) const { return !(*this == o); }
};

} // namespace notification_center
