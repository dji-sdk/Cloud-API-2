// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "device_capability.h"

#include <string>
#include <string_view>
#include <set>
#include <unordered_map>
#include <shared_mutex>


namespace flight_hub_sdk {
    // 通过 domain、type、sub_type 可以唯一地确定一款设备(可以为飞行器、负载、遥控器等);
    // todo: add more here;  
    // C++17 安全、零开销、无 SSO 依赖;

    /* 机场系列(class: airport); */
    /// 机场 DOCK 1~3;
    inline constexpr std::string_view kModelKey_3_1_0 = "3-1-0";       // DJI Dock 1 (class: "airport");
    inline constexpr std::string_view kModelKey_3_2_0 = "3-2-0";       // DJI Dock 2 (class: "airport");
    inline constexpr std::string_view kModelKey_3_3_0 = "3-3-0";       // DJI Dock 3 (class: "airport");

    /* 飞机系列(class: drone); */
    /// Matrice 300 RTK, 经纬 300 RTK 飞机;
    inline constexpr std::string_view kModelKey_0_60_0 = "0-60-0";     // Matrice 300 RTK (class: "drone");

    /// Matrice 30 / Matrice 30T;
    // 经纬 M30 系列飞机, 配合 DJI RC Plus + DJI Pilot 2 手飞, 或者大疆机场(DOCK 1);
    inline constexpr std::string_view kModelKey_0_67_0 = "0-67-0";     // Matrice 30(M30) (class: "drone");
    inline constexpr std::string_view kModelKey_0_67_1 = "0-67-1";     // Matrice 30T(M30T) (class: "drone");

    /// wm265 飞机, Mavic 3 行业飞机;
    //  DJI Mavic 3 行业系列: M3E/M3T/M3TA
    //  DJI Mavic 3 行业系列目前包括 DJI Mavic 3E、DJI Mavic 3T 和 DJI Mavic 3T Advanced 三个版本。
    //  DJI Mavic 3E 配备 4/3 CMOS 2000 万像素的广角相机和长焦相机，具备强悍的影像实力。
    //  DJI Mavic 3T 配备 1/2″ CMOS 4800 万像素的广角相机、长焦相机和热成像相机，具备热成像感知能力。
    //  DJI Mavic 3T Advanced 在 DJI Mavic 3T 的基础上优化了红外模组，可以提供更好的红外成像效果。
    inline constexpr std::string_view kModelKey_0_77_0 = "0-77-0";     // Mavic 3 行业系列(M3E 相机) (class: "drone");   DevelopCode: "wm265";
    inline constexpr std::string_view kModelKey_0_77_1 = "0-77-1";     // Mavic 3 行业系列(M3T 相机) (class: "drone");   DevelopCode: "wm265";
    inline constexpr std::string_view kModelKey_0_77_2 = "0-77-2";     // Mavic 3 系列(M3M) (class: "drone");           DevelopCode: "wm265";
    inline constexpr std::string_view kModelKey_0_77_3 = "0-77-3";     // Mavic 3 行业系列(M3TA 相机) (class: "drone");  DevelopCode: "wm265";

    /// pm431;
    // Matrice 350 RTK 飞机, 配合 DJI RC Plus+ DJI Pilot 2 使用;
    inline constexpr std::string_view kModelKey_0_89_0 = "0-89-0";     // Matrice 350 RTK (class: "drone");  DevelopCode: "pm431";

    /// ea220e, ea220t;
    // DJI Matrice 3D/3TD 无人机专为 大疆机场 2 打造。
    // Matrice 3D 搭载长焦相机和机械快门广角相机，可满足 1:500 高精度测绘作业要求。
    // Matrice 3TD 搭载广角、长焦和红外相机，能直观呈现可见光和热成像画面，适用于安防、巡检作业。
    inline constexpr std::string_view kModelKey_0_91_0 = "0-91-0";     // Matrice 3D(M3D) (class: "drone");
    inline constexpr std::string_view kModelKey_0_91_1 = "0-91-1";     // Matrice 3TD(M3TD) (class: "drone");

    /// wa345e, wa345t;
    inline constexpr std::string_view kModelKey_0_99_0 = "0-99-0";     // DJI Matrice 4 系列（M4E 相机）(class: "drone");  DevelopCode: "wa345";
    inline constexpr std::string_view kModelKey_0_99_1 = "0-99-1";     // DJI Matrice 4 系列（M4T 相机）(class: "drone");  DevelopCode: "wa345t";

    /// ea230e, ea230t;
    // DJI Matrice 4D/4TD 无人机专为大疆机场 3 打造，搭配 DJI RC Plus 2 行业版遥控器还可单独使用。
    // 两款无人机均配备广角相机、中长焦相机、长焦相机和激光测距模块。
    // Matrice 4D 更能胜任高精度专业测绘、精细化表面巡检等作业需求。
    // Matrice 4TD 额外搭载热成像相机，并新增近红外补光灯，可覆盖能源巡检、应急抢险、公共安全等众多应用场景。
    inline constexpr std::string_view kModelKey_0_100_0 = "0-100-0";   // Matrice 4D (class: "drone");
    inline constexpr std::string_view kModelKey_0_100_1 = "0-100-1";   // Matrice 4TD (class: "drone");

    /// pm440;
    // Matrice 400; 搭配 DJI RC Plus 2+ DJI Pilot 2 使用;
    inline constexpr std::string_view kModelKey_0_103_0 = "0-103-0";   // Matrice 400 (class: "drone");  DevelopCode: "pm440";

    /* 中继 (class: "relay") */
    inline constexpr std::string_view kModelKey_6_600_0 = "6-600-0";   // Relay(比如 D-RTK 3) (class: "relay");

    /* 基站和关联的设备; */
    inline constexpr std::string_view kModelKey_8_135_0 = "8-135-0";   // Base Station(比如 O4 Ground Station) (class: "base_station");
    inline constexpr std::string_view kModelKey_0_130_0 = "0-130-0";   // ea340t drone (class: "drone");
    inline constexpr std::string_view kModelKey_9_131_0 = "9-131-0";   // ea340d dock (class: "dock");

    /* RC 设备; */
    /// DevelopCode: "rm500";
    inline constexpr std::string_view kModelKey_2_56_0 = "2-56-0";     // RC("DJI 带屏遥控器行业版", 搭配 Matrice 300 RTK) (class: "rc");
    /// DevelopCode: "rm700";
    inline constexpr std::string_view kModelKey_2_119_0 = "2-119-0";   // RC("DJI RC Plus", 搭配 Matrice 350 RTK, Matrice 300 RTK, Matrice 30/30T) (class: "rc");
    /// DevelopCode: "rm510";
    inline constexpr std::string_view kModelKey_2_144_0 = "2-144-0";   // RC("DJI RC Pro 行业版", Mavic 3 行业系列) (class: "rc");
    /// DevelopCode: "rc701";
    inline constexpr std::string_view kModelKey_2_174_0 = "2-174-0";   // RC("DJI RC Plus 2", 搭配 DJI Matrice 4 系列) (class: "rc");




    /**
     * 设备能力管理模块;
     */
    class DeviceCapabilityManager {
    public:
        static DeviceCapabilityManager& getInstance();

        /**
         * @brief
         *    更新 device_sn 对应的 model_info, 调用者保证参数有效;
         *    允许多线程调用;
         * @param
         *    device_sn 设备 SN;
         *    concise_model_info model_info;
         * @return
         *    void 无返回值;
         */
        void updateModelInfo(const std::string &device_sn, ConciseModelInfo concise_model_info);

        /**
         * @brief
         *    更新设备 model_info;
         *    允许多线程调用;
         * @param
         *    device_model_info, 设备 SN 和 model_info 对应的映射;
         * @return
         *    void 无返回值;
         */
        void updateModelInfo(std::unordered_map<std::string, ConciseModelInfo> &&device_model_info);


        // 清理指定设备SN的 model_info;
        void removeModelInfo(const std::string &device_sn);

        // 清理所有的 model_info;
        void clearAllModelInfo();

        /**
         * @brief
         *    获取设备 model_info;
         *    允许多线程调用;
         * @param
         *    device_sn, 设备 SN;
         * @return
         *   bool 表示返回值是否有效;
         *   ConciseModelInfo 表示设备 model_info;
         */
        std::pair<bool, ConciseModelInfo> getModelInfo(const std::string &device_sn) const;


        /**
         * @brief
         *    检查 model_key 对应的 feature 的(不)支持状态;
         *    [ReadOnly] 允许多线程调用;
         *    该方法基于黑名单(不支持列表)设计，在黑名单语境下，"未配置(返回值 NotConfigured)" 实际意味着 "未被标记为不支持"。
         * @param
         *    model_key, 设备 model_key;
         *    feature_id, 注册的 feature id;
         * @return
         *   FeatureStatus 返回值指示 model_key 对应的 feature 的(不)支持状态;
         */
        static flight_hub_sdk::FeatureStatus checkUnsupported(const std::string &model_key, flight_hub_sdk::FeatureId feature_id);


        /**
         * @brief
         *    按 device_sn 检查 feature 的(不)支持状态;
         *    先经 model_info 注册表把 device_sn 解析为 model_key, 再复用 checkUnsupported(model_key, feature_id);
         *    [ReadOnly] 允许多线程调用;
         *    注意: 不能与上面的 checkUnsupported 重载(两者形参类型均为 (const std::string&, FeatureId),
         *          C++ 无法按形参名重载), 故采用独立方法名;
         * @param
         *    device_sn, 设备 SN;
         *    feature_id, 注册的 feature id;
         * @return
         *   FeatureStatus; 若 device_sn 尚未在注册表中(model_info 未拉取到), 返回 NotFound;
         *   返回状态说明, "NotFound 涵盖设备未注册/model_info 未拉取两种情况, 调用方可按'不拦截'处理", 如调用报错, 有相关错误信息给到调用者。
         */
        static flight_hub_sdk::FeatureStatus checkUnsupportedByDeviceSn(const std::string &device_sn, flight_hub_sdk::FeatureId feature_id);



    private:
        DeviceCapabilityManager() = default;
        ~DeviceCapabilityManager() = default;

        // 禁止拷贝构造、赋值、移动构造、移动赋值;
        DeviceCapabilityManager(const DeviceCapabilityManager&) = delete;
        DeviceCapabilityManager& operator=(const DeviceCapabilityManager&) = delete;
        DeviceCapabilityManager(DeviceCapabilityManager&&) = delete;
        DeviceCapabilityManager& operator=(DeviceCapabilityManager&&) = delete;


       /*
        * 动态注册表：model_info_lookup_（device_sn → ConciseModelInfo）,
        * 运行时由 DeviceListManager 拉取设备列表后填充;
        * 数据: DeviceSN --> ConciseModelInfo;
        */
        mutable std::shared_mutex                         model_info_rw_mutex_; 
        std::unordered_map<std::string, ConciseModelInfo> model_info_lookup_;


       /*
        * 静态能力黑名单;
        * 设备 model_key 和 对应的注册能力(黑名单: 不支持的 Feature ID);
        * 静态配置, 随 SDK 版本发布更新，只读;
        *
        * 采用 Construct-On-First-Use(函数局部静态)而非静态数据成员:
        *   - 避免跨 TU 的静态初始化顺序问题(SIOF): checkUnsupported 为 static, 可能在其它 TU
        *     的静态初始化阶段被调用, 若此表尚未初始化则为 UB;
        *   - 函数局部 static 保证首次调用时才构造, 且构造过程线程安全(C++11 magic static);
        *   - 返回 const 引用, 只读, 无额外拷贝开销;
        */
       static const std::unordered_map<std::string, std::set<FeatureId>>& unsupportedFeaturesTable();
    };
}  // namespace flight_hub_sdk