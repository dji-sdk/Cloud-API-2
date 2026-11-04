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
 *
 * 构建分两步:
 *   1) 以 device_capability_mgr.h 中的 kAllModelKeys 打底, 每个 SDK 已知机型都落一个空黑名单条目;
 *      —— 保证"已声明的 model_key 必定已注册", checkUnsupported() 对已知机型至少返回
 *         NotConfigured, 只有未知/非法 model_key 才返回 NotFound;
 *   2) 逐机型登记黑名单; 每款机型保留一个登记位, 便于逐款排查、逐步补齐;
 *
 * 新增机型时:
 *   - 必做: 在 .h 的 kAllModelKeys 中登记(否则 checkUnsupported 会误判为 NotFound);
 *   - 建议: 在下面第 2 步对应分组内补一行登记位, 便于后续跟踪该机型的能力确认进度;
 *     即便漏写登记位也不会出问题 —— Step 1 已保证该机型必定在表中;
 */
// todo: 在框架上不断补充配置;
const std::unordered_map<std::string, std::set<flight_hub_sdk::FeatureId>>& DeviceCapabilityManager::unsupportedFeaturesTable() {
    static const std::unordered_map<std::string, std::set<flight_hub_sdk::FeatureId>> table = [] {
        std::unordered_map<std::string, std::set<flight_hub_sdk::FeatureId>> unsupported_features_table;
        unsupported_features_table.reserve(kAllModelKeys.size());

        /* Step 1: 用 SDK 已知机型全量清单打底, 默认"无已知不支持项"; */
        for (const auto &model_key : kAllModelKeys) {
            unsupported_features_table.emplace(std::string(model_key), std::set<flight_hub_sdk::FeatureId>{});
        }

        /*
         * Step 2: 逐机型登记"不支持的 Feature";
         * 登记位约定:
         *   - 空集合 {} 表示"当前尚无已知不支持项", 带 todo 标记, 待确认后填入;
         *   - 已确认有不支持项的机型, 直接把 FeatureId 列进去;
         */
        auto mark = [&unsupported_features_table](std::string_view model_key,
                                                  std::set<flight_hub_sdk::FeatureId> unsupported_features) {
            unsupported_features_table[std::string(model_key)] = std::move(unsupported_features);
        };

        /* ========== 机场系列 (class: "airport") ========== */
        // 大疆机场 DOCK 1 / DOCK 2 / DOCK 3 能力黑名单;
        // 机场机型走航线任务下发, 不支持指点飞行/航线任务控制/POI 环绕/急停/迫降;
        // todo: 无法一次列全, 需持续完善;
        for (const auto &dock_model_key : {kModelKey_3_1_0, kModelKey_3_2_0, kModelKey_3_3_0}) {
            mark(dock_model_key, {
                FeatureId::FlyToPoint,
                FeatureId::FlightTaskControl,
                FeatureId::POI,
                FeatureId::FC_EmergencyStop,
                FeatureId::FC_EmergencyLanding
            });
        }
        mark(kModelKey_3_4_0,   {});   // D-RTK 3 (Inside Edition), ei200;                    todo: 待确认不支持项;

        /* ========== 飞机系列 (class: "drone") ========== */
        mark(kModelKey_0_60_0,  {});   // Matrice 300 RTK, pm430;                             todo: 待确认不支持项;
        mark(kModelKey_0_67_0,  {});   // Matrice 30(M30), pm320;                             todo: 待确认不支持项;
        mark(kModelKey_0_67_1,  {});   // Matrice 30T(M30T), pm320;                           todo: 待确认不支持项;
        mark(kModelKey_0_77_0,  {});   // Mavic 3E(M3E 相机), wm265;                          todo: 待确认不支持项;
        mark(kModelKey_0_77_1,  {});   // Mavic 3T(M3T 相机), wm265;                          todo: 待确认不支持项;
        mark(kModelKey_0_77_2,  {});   // Mavic 3M(M3M), wm265;                               todo: 待确认不支持项;
        mark(kModelKey_0_77_3,  {});   // Mavic 3TA(M3TA 相机), wm265;                        todo: 待确认不支持项;
        mark(kModelKey_0_89_0,  {});   // Matrice 350 RTK, pm431;                             todo: 待确认不支持项;
        mark(kModelKey_0_91_0,  {});   // Matrice 3D(M3D), ea220e;                            todo: 待确认不支持项;
        mark(kModelKey_0_91_1,  {});   // Matrice 3TD(M3TD), ea220t;                          todo: 待确认不支持项;
        mark(kModelKey_0_99_0,  {});   // Matrice 4E(M4E 相机), wa345e;                       todo: 待确认不支持项;
        mark(kModelKey_0_99_1,  {});   // Matrice 4T(M4T 相机), wa345t;                       todo: 待确认不支持项;
        mark(kModelKey_0_100_0, {});   // Matrice 4D, ea230e;                                 todo: 待确认不支持项;
        mark(kModelKey_0_100_1, {});   // Matrice 4TD, ea230t;                                todo: 待确认不支持项;
        mark(kModelKey_0_103_0, {});   // Matrice 400, pm440;                                 todo: 待确认不支持项;
        mark(kModelKey_0_130_0, {});   // M5TD 网联飞机, ea340t;                              todo: 待确认不支持项;
        mark(kModelKey_0_139_0, {});   // pm355(搭配 rc710 遥控器);                           todo: 待确认不支持项;

        /* ========== 中继 (class: "relay") ========== */
        mark(kModelKey_6_600_0, {});   // Relay(比如 D-RTK 3), ea600;                         todo: 待确认不支持项;
        mark(kModelKey_6_610_0, {});   // EA610 中继(上报名 "O4 Ground Station"), ea610relay; todo: 待确认不支持项;

        /* ========== 基站/机库 (class: "base_station" / "dock") ========== */
        mark(kModelKey_8_135_0, {});   // Base Station(O4 Ground Station), ea610;             todo: 待确认不支持项;
        mark(kModelKey_9_116_0, {});   // ea231d 机库;                                        todo: 待确认不支持项;
        mark(kModelKey_9_131_0, {});   // ea340d 机库;                                        todo: 待确认不支持项;

        /* ========== 遥控器 (class: "rc") ========== */
        mark(kModelKey_2_56_0,  {});   // "DJI 带屏遥控器行业版" rm500(搭配 M300 RTK);        todo: 待确认不支持项;
        mark(kModelKey_2_119_0, {});   // "DJI RC Plus" rm700(搭配 M350/M300 RTK, M30/M30T);  todo: 待确认不支持项;
        mark(kModelKey_2_144_0, {});   // "DJI RC Pro 行业版" rm510(搭配 Mavic 3 行业系列);   todo: 待确认不支持项;
        mark(kModelKey_2_174_0, {});   // "DJI RC Plus 2" rc701(搭配 Matrice 4 系列);         todo: 待确认不支持项;
        mark(kModelKey_2_197_0, {});   // rc710(搭配 pm355);                                  todo: 待确认不支持项;

        return unsupported_features_table;
    }();

    // 返回 能力黑名单;
    return table;
}
}  // namespace flight_hub_sdk
