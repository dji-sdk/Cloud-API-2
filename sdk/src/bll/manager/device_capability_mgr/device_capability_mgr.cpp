// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "device_capability_mgr.h"
#include "base/common/logger.h"

#include <mutex>

namespace flight_hub_sdk {

// 设备能力管理模块实例;
DeviceCapabilityManager& DeviceCapabilityManager::getInstance() {
    static DeviceCapabilityManager instance;
    return instance;
}


// 更新 device_sn 对应的 model_info, 调用者保证参数有效;
void DeviceCapabilityManager::updateModelInfo(const std::string &device_sn, ConciseModelInfo concise_model_info) {
    std::unique_lock<std::shared_mutex> lock_gd(model_info_rw_mutex_);
    model_info_lookup_.insert_or_assign(device_sn, std::move(concise_model_info));
}


// 批量更新 model_info, 调用者保证参数有效;
void DeviceCapabilityManager::updateModelInfo(std::unordered_map<std::string, ConciseModelInfo> &&device_model_info) {
    std::unique_lock<std::shared_mutex> lock_gd(model_info_rw_mutex_);
    for (auto &[sn, info] : device_model_info) {
        model_info_lookup_.insert_or_assign(sn, std::move(info));
    }
}


// 清理 device model info;
void DeviceCapabilityManager::removeModelInfo(const std::string &device_sn) {
    std::unique_lock<std::shared_mutex> lock_gd(model_info_rw_mutex_);
    model_info_lookup_.erase(device_sn);
}


// 清空 device model info;
void DeviceCapabilityManager::clearAllModelInfo() {
    std::unique_lock<std::shared_mutex> lock_gd(model_info_rw_mutex_);
    model_info_lookup_.clear();
}


// 获取设备的 简要 model info;
std::pair<bool, ConciseModelInfo> DeviceCapabilityManager::getModelInfo(const std::string &device_sn) const {
    std::shared_lock<std::shared_mutex> lock_gd(model_info_rw_mutex_);
    auto it = model_info_lookup_.find(device_sn);
    if (it != model_info_lookup_.end()) {
        return {true, it->second};
    }

    // 打印日志;
    LOG_DEBUG("[DeviceCapabilityManager] getModelInfo(), device(\"%s\") NOT found proper concise-model-info",
        device_sn.c_str()
    );

    return {false, ConciseModelInfo{}};
}


// 检查 model_key 对应的 feature 的(不)支持状态;
flight_hub_sdk::FeatureStatus DeviceCapabilityManager::checkUnsupported(const std::string &model_key, flight_hub_sdk::FeatureId feature_id) {
    const auto &table = unsupportedFeaturesTable();
    auto model_it = table.find(model_key);
    if (model_it == table.end()) {
        // 打印日志;
        LOG_DEBUG("[DeviceCapabilityManager] checkUnsupported(), model_key(\"%s\") NOT found in unsupported features table",
            model_key.c_str()
        );

        return FeatureStatus::NotFound;
    }

    // 明确找到了 unsupported config;
    if ((model_it->second).find(feature_id) != (model_it->second).end()) {
        return FeatureStatus::Unsupported;
    }

    // 打印日志;
    LOG_DEBUG("[DeviceCapabilityManager] checkUnsupported(), model_key(\"%s\") NOT Configured in unsupported features table",
        model_key.c_str()
    );

    return FeatureStatus::NotConfigured;
}


//  按 device_sn 检查 feature 的(不)支持状态;
flight_hub_sdk::FeatureStatus DeviceCapabilityManager::checkUnsupportedByDeviceSn(const std::string &device_sn, flight_hub_sdk::FeatureId feature_id) {
    // 经注册表把 device_sn 解析为 model_key;
    auto [found, model_info] = getInstance().getModelInfo(device_sn);
    if (!found) {
        // 尚未拉取到该设备的 model_info, 无法判定; 与 model_key 未命中语义一致, 返回 NotFound;
        // 打印日志;
        LOG_DEBUG("[DeviceCapabilityManager] checkUnsupportedByDeviceSn(), device_sn(\"%s\") NOT found its MODEL_INFO in model info lookup table",
            device_sn.c_str()
        );

        return FeatureStatus::NotFound;
    }

    // 打印日志;
    LOG_DEBUG("[DeviceCapabilityManager] checkUnsupportedByDeviceSn(), device_sn(\"%s\")  found its MODEL_INFO { model_key: %s, name: %s, device_class: %s }",
        device_sn.c_str(),
        model_info.model_key.c_str(),
        model_info.name.c_str(),
        model_info.device_class.c_str()
    );

    return checkUnsupported(model_info.model_key, feature_id);
}






/*
 * 设备 model_key 和 对应的注册能力(黑名单: 不支持的 Feature ID);
 * 静态配置, 随 SDK 版本发布更新，只读;
 *
 * Construct-On-First-Use: 函数局部静态, 首次调用时才构造, 线程安全(C++11 magic static),
 * 消除跨 TU 的静态初始化顺序问题(SIOF);
 */
// todo: 在框架上不断补充配置;
const std::unordered_map<std::string, std::set<flight_hub_sdk::FeatureId>>& DeviceCapabilityManager::unsupportedFeaturesTable() {
    static const std::unordered_map<std::string, std::set<flight_hub_sdk::FeatureId>> table = {
    /* 大疆机场(Dock) 能力黑名单; */
    // DJI Dock 1:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_3_1_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
            FeatureId::FlyToPoint,
            FeatureId::FlightTaskControl,
            FeatureId::POI,
            FeatureId::FC_EmergencyStop,
            FeatureId::FC_EmergencyLanding
        }
    },

    /* 大疆机场2 (Dock 2) 能力黑名单; */
    // DJI Dock 2:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_3_2_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
            FeatureId::FlyToPoint,
            FeatureId::FlightTaskControl,
            FeatureId::POI,
            FeatureId::FC_EmergencyStop,
            FeatureId::FC_EmergencyLanding
        }
    },

    /* 大疆机场3 (Dock 3) 能力黑名单; */
    // DJI Dock 3:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_3_3_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
            FeatureId::FlyToPoint,
            FeatureId::FlightTaskControl,
            FeatureId::POI,
            FeatureId::FC_EmergencyStop,
            FeatureId::FC_EmergencyLanding
        }
    },

    /* Matrice 300 RTK, 经纬 300 RTK 飞机 能力黑名单; */
    // Matrice 300 RTK:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_60_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /* Matrice 30 飞机 能力黑名单; */
    // DJI Matrice 30(M30):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_67_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /* Matrice 30T(M30T) 飞机 能力黑名单; */
    // DJI Matrice 30T(M30T):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_67_1),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /*
     * wm265 飞机, Mavic 3 行业系列: M3E / M3T / M3M / M3TA;
     * DJI Mavic 3E   配备 4/3 CMOS 2000 万像素广角相机和长焦相机;
     * DJI Mavic 3T   配备 1/2″ CMOS 4800 万像素广角相机、长焦相机和热成像相机;
     * DJI Mavic 3M   多光谱机型;
     * DJI Mavic 3TA  在 M3T 基础上优化红外模组;
     */
    // Mavic 3E (M3E 相机):  model_key --> UN-supported_features set;
    {
        std::string(kModelKey_0_77_0),
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // Mavic 3T (M3T 相机):  model_key --> UN-supported_features set;
    {
        std::string(kModelKey_0_77_1),
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // Mavic 3M (M3M):  model_key --> UN-supported_features set;
    {
        std::string(kModelKey_0_77_2),
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // Mavic 3TA (M3TA 相机):  model_key --> UN-supported_features set;
    {
        std::string(kModelKey_0_77_3),
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /* pm431 飞机, Matrice 350 RTK, 配合 DJI RC Plus + DJI Pilot 2 使用; */
    // Matrice 350 RTK:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_89_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    // DJI Matrice 3D(M3D):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_91_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // DJI Matrice 3TD(M3TD):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_91_1),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /* wa345e / wa345t 飞机, DJI Matrice 4 系列(M4E / M4T 相机); */
    // Matrice 4E (M4E 相机):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_99_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // Matrice 4T (M4T 相机):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_99_1),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // DJI Matrice 4D:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_100_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // DJI Matrice 4TD:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_100_1),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    // DJI Matrice 400:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_103_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /* 中继 (class: "relay") */
    // Relay(比如 D-RTK 3) (class: "relay");
    {
        // model_key;
        std::string(kModelKey_6_600_0),
        // UN-supported_features set;
        // todo: 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /* 基站和关联的设备; */
    // Base Station(比如 O4 Ground Station):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_8_135_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // ea340t drone:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_0_130_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // ea340d dock:  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_9_131_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },

    /* RC 遥控器设备; */
    // RC "DJI 带屏遥控器行业版"(rm500, 搭配 Matrice 300 RTK):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_2_56_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // RC "DJI RC Plus"(rm700, 搭配 M350/M300 RTK, M30/M30T):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_2_119_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // RC "DJI RC Pro 行业版"(rm510, 搭配 Mavic 3 行业系列):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_2_144_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    },
    // RC "DJI RC Plus 2"(rc701, 搭配 DJI Matrice 4 系列):  model_key --> UN-supported_features set;
    {
        // model_key;
        std::string(kModelKey_2_174_0),
        // UN-supported_features set;
        // todo: 无法一次列全, 需持续完善;
        std::set<flight_hub_sdk::FeatureId>{
        }
    }
    };

    // 返回 能力黑名单;
    return table;
}
}  // namespace flight_hub_sdk
