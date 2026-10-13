// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "value.h"
#include <functional>
#include <optional>
#include <string>
#include <vector>

namespace flight_hub_sdk {

// === 公共子结构体 ===

/// 兴趣区域多边形坐标点（供 CreateReconstructionParams 和 TerraBuildSettingsDetail 共用）
struct ReconstructionPolygonPoint {
    double lat = 0.0;
    double lng = 0.0;
};

/// 兴趣区域（多边形），JSON 结构为 { "polygon_points": [{lat,lng}, ...] }
struct ReconPredefineArea {
    std::vector<ReconstructionPolygonPoint> polygon_points;
};

// === Terra Build 公开结果结构体 ===

// ─────────────────────────────────────────────────────────
// WebSocket 推送消息结构体
// "topic_reconstruction_status_changed_event"
// ─────────────────────────────────────────────────────────

/// 单个重建任务信息（推送中的 job_info / job_infos）
struct TerraBuildJobInfo {
    int type = 0;           // 任务类型：14=2D, 15=3D, 24=3DGS, 25=Lidar, 12=SfM, 23=ZIP 等
    std::string uuid;       // 异步平台实例 ID
    int job_status = 0;     // 1=申请中, 3=执行中, 6=成功, 7=失败, 8=停止中
    int percent = 0;        // 进度百分比（0-100）
    int code = 0;           // 错误码（omitempty）
};

/// 重建任务状态变更推送消息
struct TerraBuildReconstructionStatusChanged {
    std::string workspace_id;                   // 项目 UUID
    long long id = 0;                            // 文件 ID
    std::string name;                           // 文件名
    int state = 0;                              // 建图文件状态, {0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点}
    std::string uuid;                           // 文件 UUID
    int file_type = 0;                          // 文件类型
    int sub_file_type = 0;                      // 子文件类型（格式标识）
    int code = 0;                               // 错误码，仅失败时填充
    long long size = 0;                           // 文件大小（bytes）
    TerraBuildJobInfo job_info;                 // 当前触发的任务信息
    std::vector<TerraBuildJobInfo> job_infos;   // 所有关联任务列表（完整状态回调时填充）
    int fast_reconstruction_id = 0;        // 快速重建关联 ID
};

/// 重建参数详情
struct TerraBuildSettingsDetail {
    std::string name;                                       //任务名称
    std::vector<int> reconstruction_type;                   //重建类型
    double simplified_factor = 0.0;                         //模型简化系数
    int reconstruction_mode = 0;                            //重建模式
    ReconPredefineArea predefine_area;                       // 兴趣区域（含 polygon_points）
    std::vector<int> generate_model_formats;                //输出格式列表
    std::string wkt;                                        //输出坐标系WKT字符串
    int quality_level = 0;                                  //建模质量等级
    int quality_level_3dgs = 0;                             //3DGS质量等级
    // TODO(apifox): folder_uuids (文件夹 UUID 列表)
    // TODO(apifox): gcp_settings (控制点设置)
    // TODO(apifox): is_use_gcp (是否使用控制点)
    // TODO(apifox): utm_zone_index (UTM 区域索引)
    // TODO(apifox): version (版本号)
    // TODO(apifox): ref_GPS (GPS 参考信息)
};

/// 文件夹信息
struct TerraBuildFolderInfo {
    int folder_id = 0;          //图像所在媒体文件夹ID
    std::string folder_uuid;    //图像所在媒体文件夹UUID
    std::string folder_name;    //图像所在媒体文件夹名称
};

/// 获取重建设置结果
struct TerraBuildSettingsResult {
    int state = 0;                                      // 建图文件状态, {0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点}
    TerraBuildSettingsDetail reconstruction_settings;   //重建参数JSON对象
    std::string media_input_object_key;                 //输入图像清单的OSS Key
    std::string sfm_output_object_prefix;               //空三输出文件的OSS目录前缀
    std::vector<TerraBuildFolderInfo> folder_list;      //输入图像所在媒体文件夹列表
};

/// 质量报告结果
struct TerraBuildQualityReportResult {
    std::string quality_report_url;                     //质量报告 PDF 签名 URL
};

/// 模型文件项
struct TerraBuildModelFileItem {
    int id = 0;
    std::string uuid;
    std::string name;
    int file_type = 0;                      // 文件大类型
    int sub_file_type = 0;                  // 文件子类型
    int state = 0;                          // 建图文件状态, {0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点}
    std::vector<int> reconstruction_type;   // 重建类型（数组）
    std::string thumbnail_url;
    std::string zip_url;                    // 打包下载 URL
    std::string created_at;
    std::string updated_at;
};

/// 模型文件列表结果
struct TerraBuildModelFileListResult {
    std::vector<TerraBuildModelFileItem> list;
    long long total = 0;  ///< 总记录数（对应 proto PaginationInfo.total，int64）
    int page = 0;
    int page_size = 0;
};

/// 模型文件签名 URL 结果
struct TerraBuildModelFileUrlResult {
    std::string url;
};

/// 创建重建任务结果
struct TerraBuildCreateReconstructionResult {
    int id = 0;             // 任务 ID
    std::string uuid;       // 任务 UUID（即 file_uuid）
    std::string job_uuid;   // 实例 UUID
};

/// 空三结果
struct TerraBuildSfmResult {
    std::string sfm_json_url;                              // 空三计算结果 JSON 文件签名 URL
    std::string sfm_report_json_url;                       // 空三质量报告 JSON（暂不支持）
    std::string gcp_out_json_url;                          // 控制点输出 JSON（暂不支持）
    std::vector<std::string> re_projection_error_pic_url_list; // 重投影误差图片签名 URL 列表
};

/// 创建全景拼接结果
struct TerraBuildCreatePanoramaResult {
    int id = 0;             // 任务 ID
    std::string uuid;       // 任务 UUID
    std::string job_uuid;   // 实例 UUID
};

/// 重建图像项
struct TerraBuildReconstructionImageItem {
    std::string image_id;            // 图像 ID（文件名去掉扩展名）
    std::string file_name;           // 原始文件名
    std::string thumbnail_image_url; // 缩略图签名 URL
    std::string origin_image_url;    // 原图签名 URL
};

/// 重建图像列表结果
struct TerraBuildReconstructionImagesResult {
    std::vector<TerraBuildReconstructionImageItem> list;
    int total = 0;
};

/// 重建图像列表结果
struct TerraBuildReportSetting {
    std::string language = "zh";                    // 报告语言: zh/en
    std::string measure_unit_system = "metric";     //度量单位: metric/imperial
};

// === ITerraBuildModule 接口 ===

// --- 公开参数子结构体（前缀 Recon 与内部序列化层隔离，避免重复定义）---

/// ReconGeoDesc — 地理坐标系描述（公开层）
struct ReconGeoDesc {
    std::string cs_type;                // "GEO_CS" / "LOCAL_CS" / "LOCAL_ENU_CS"
    std::string geo_cs;                 // EPSG 代号，如 "EPSG:32649"
    std::string geo_cs_wkt;             // WKT 描述
    std::string geoid_file_name;        // 大地水准面文件名
    std::vector<double> offset;         // 坐标偏移量
    std::string override_vertical_cs;   // 重载高程坐标系
};

/// ReconQualityReportSetting — 质量报告设置（公开层）
struct ReconQualityReportSetting {
    std::string language = "zh";                // zh/en/es/fr/de/ja/ko
    std::string measure_unit_system = "metric"; // metric/imperial
};

/// ReconParameter2D — 二维重建参数（公开层）
struct ReconParameter2D {
    std::string SfM_method;                             //空三算法
    std::optional<int> sfm_feature_quantity_level;      //特征点数量上限
    std::optional<bool> use_image_position;             //是否使用影像记录的位置信息
    std::optional<bool> generate_blocks_exchangge_xml;  //生成CC格式空三结果 // TODO(apifox-typo): 应为 generate_blocks_exchange_xml
    ReconGeoDesc blocks_exchange_xml_geo_desc;          //CC格式空三成果的输出坐标系
    std::optional<double> usable_memory_GB;             //限制内存用量
    std::optional<int> map_mode;                        //拼图模式
    std::optional<int> quality_level;                   //重建精细程度，1=高, 2=中, 3=低
    std::optional<int> resolution_type_2d;              //GSD分辨率
    std::optional<int> dom_tile_size;                   //分块成果单块DOM像素尺寸，需为2的整数倍
    ReconGeoDesc output_geo_desc;                       //输出坐标系描述
    std::optional<double> resolution_gsd_2d;            //GSD分辨率（m/px），范围0.0001 - 10
    std::optional<bool> use_Wallis;                     //是否开启图像内部匀色
    std::optional<bool> use_Dehaze;                     //是否开启去雾
    std::optional<bool> use_gcj02;                      //是否使用国测局部坐标系
    std::optional<bool> generate_maps_2d_tiff;          //是否生成DOM（TIFF格式）
    std::optional<bool> generate_maps_2d_tiff_cog;      //是否生成DOM(COG云优化 GEOTIFF格式)
    std::optional<bool> use_smart_fill_hole;            //是否启用智能填洞
};

/// ReconParameter3D — 三维重建参数（公开层）
struct ReconParameter3D {
    std::string SfM_method;                             //空三算法
    std::optional<int> sfm_feature_quantity_level;      //特征点数量上限
    std::optional<bool> use_image_position;             //是否使用影像记录的位置信息
    std::optional<bool> generate_blocks_exchangge_xml;  //生成CC格式空三结果 // TODO(apifox-typo): 应为 generate_blocks_exchange_xml
    ReconGeoDesc blocks_exchange_xml_geo_desc;          //CC格式空三成果的输出坐标系
    std::optional<bool> output_pointcloud;              //是否输出点云成果
    std::optional<int> density_percent_level;           //点云密度，1=高(100%) 2=中(25%) 3=低(6.25%)
    std::optional<bool> generate_pnts;                  //是否生成pnts格式点云LOD模型
    std::optional<bool> generate_las;                   //是否生成LAS格式点云模型
    std::optional<bool> generate_laz;                   //是否生成LAZ格式点云模型
    std::optional<bool> generate_pcd;                   //是否生成PCD格式点云模型
    std::optional<bool> generate_point_s3mb;            //是否生成s3mb格式点云模型
    std::optional<bool> generate_point_ply;             //是否生成ply格式点云模型
    std::optional<bool> merge_point;                    //是否将点云合并成一个文件
    std::optional<bool> output_ply_with_normals;        //是否开启带法向量的点云ply
    std::optional<int> ply_with_normals_level;          //图源等级 原图:4 缩略图:2
    std::optional<bool> groud_extract;                  //是否提取地面点 // TODO(apifox-typo): 应为 ground_extract
    std::optional<int> scene_type;                      //地面场景类型，0=平地, 1=缓坡, 2=陡坡
    std::optional<double> max_size_building;            //最大建筑物对角线长度，范围 [1, 1000]
    std::optional<double> iter_dist;                    //迭代距离，范围 [0.01, 10]
    std::optional<double> iter_angle;                   //迭代角度，范围 [0.01, 60]
    std::optional<double> usable_memory_GB;             //限制内存用量
    std::optional<bool> stereo_mode;                    //是否立体模式
    std::optional<int> global_min_depth;                //全局最低高度
    std::optional<bool> output_mesh;                    //纹理模型成果参数
    std::optional<bool> generate_ply;                   //是否生成 ply 格式纹理模型
    std::optional<bool> generate_obj;                   //是否生成 obj 格式纹理模型
    std::optional<bool> generate_fbx;                   //是否生成 fbx 格式纹理模型
    std::optional<bool> generate_b3dm;                  //是否生成 b3dm 格式网格 LOD 模型
    std::optional<bool> generate_osgb;                  //是否生成 osgb 格式网格 LOD 模型
    std::optional<bool> generate_s3mb;                  //是否生成 s3mb 格式网格 LOD 模型
    std::optional<bool> generate_i3s;                   //是否生成 i3s 格式网格 LOD 模型
    std::optional<bool> water_refine;                   //是否开启水面平整
    std::optional<bool> water_remove;                   //是否移除水面
    std::optional<double> model_simplify;               //简化模型面片比例，范围 [0.05-1]
    std::optional<bool> generate_dem;                   //DEM 成果参数
    std::optional<double> dem_resolution;               //DEM 分辨率（米），范围 [0.05, 50]
    std::optional<bool> generate_contour;               //等高线参数
    std::optional<double> contour_datum;                //等高线基准面高程
    std::optional<double> contour_interval;             //等高线间距
    std::optional<double> contour_elevation_annotation_radius; //等高线高程标注半径
    std::optional<bool> use_contour_filter;             //是否启用等高线过滤
    std::optional<double> contour_filter_length;        //等高线过滤最小长度
    std::optional<bool> generate_grid_and_tin;          //格网与 TIN 参数
    std::optional<double> grid_interval;                //格网间距
    std::optional<bool> cut_AOI;                        //是否删去拍摄位置之外的部分
    std::optional<int> quality_level;                   //重建精细程度，1=高, 2=中, 3=低
    ReconGeoDesc output_geo_desc;                       //输出坐标系描述
};

/// ReconParameter3DGS — 3DGS 重建参数（公开层）
struct ReconParameter3DGS {
    std::string SfM_method;                             // 空三算法，可选值 "GLOBAL"、"INCREMENTAL"
    std::optional<int> sfm_feature_quantity_level;      // 特征点数量上限，可选值 1、2、3，分别代表 4w、2w、9k
    std::optional<bool> use_image_position;             // 是否使用影像记录的位置信息
    std::optional<bool> generate_blocks_exchangge_xml;  // 是否生成 CC 格式空三成果 // TODO(apifox-typo): 应为 generate_blocks_exchange_xml
    ReconGeoDesc blocks_exchange_xml_geo_desc;          // CC 格式空三成果的输出坐标系
    std::string global_data_type;                       // 数据类型，如 "rgb" / "lidar"
    std::optional<int> quality_level_3dgs;              // 3DGS 质量等级：1=极高, 2=高, 4=预览
    std::optional<bool> generate_terrags;               // 是否生成 terrags 格式成果
    std::optional<bool> generate_3dtiles;               // 是否生成 3D Tiles 格式成果
    std::optional<bool> generate_3dgs_dom;              // 是否基于 3DGS 模型生成 2D 地图
    ReconGeoDesc output_geo_desc;                       // 输出坐标系描述
};

/// ReconParameterLidar — 激光雷达重建参数（公开层）
struct ReconParameterLidar {
    std::optional<bool> output_pointcloud;              // 是否输出点云成果
    std::optional<int> density_percent_level;           // 点云密度，1=高(100%) 2=中(25%) 3=低(6.25%)
    std::optional<bool> generate_pnts;                  // 是否生成 pnts 格式点云 LOD 模型
    std::optional<bool> generate_las;                   // 是否生成 LAS 格式点云模型
    std::optional<bool> generate_laz;                   // 是否生成 LAZ 压缩点云格式
    std::optional<bool> generate_pcd;                   // 是否生成 PCD 点云格式
    std::optional<bool> generate_point_s3mb;            // 是否生成 s3mb 格式点云模型
    std::optional<bool> generate_point_ply;             // 是否生成 ply 格式点云模型
    std::optional<bool> merge_point;                    // 是否将点云合并成一个文件
    std::optional<bool> output_ply_with_normals;        // 是否开启带法向量的点云 ply
    std::optional<int> ply_with_normals_level;          // 图源等级，原图:4 缩略图:2
    std::optional<bool> groud_extract;                  // 是否提取地面点 // TODO(apifox-typo): 应为 ground_extract
    std::optional<int> scene_type;                      // 地面场景类型，0=平地, 1=缓坡, 2=陡坡
    std::optional<double> max_size_building;            // 最大建筑物对角线长度，范围 [1, 1000]
    std::optional<double> iter_dist;                    // 迭代距离，范围 [0.01, 10]
    std::optional<double> iter_angle;                   // 迭代角度，范围 [0.01, 60]
    std::optional<double> usable_memory_GB;             // 通用处理参数，限制内存用量
    std::optional<bool> colorize_point;                 // 是否对点云进行着色
    std::optional<bool> lpp_optimise;                   // 是否进行 ICP 优化
    std::optional<int> refine_calibrate;                // 是否进行自标定，0=否, 1=是
    std::optional<bool> smooth_point;                   // 是否平滑点云
    std::optional<double> sample_distance;              // 体素降采样距离，0=不降采样
    std::optional<double> output_height_offset;         // 高度偏移值
    std::optional<int> quality_level;                   // 时序降采样率，1=100%, 2=25%, 3=6.25%
    std::optional<bool> can_process_L1;                 // 是否可以处理禅思 L1 雷达数据
    ReconGeoDesc output_geo_desc;                       // 输出坐标系描述
    std::optional<bool> output_mesh;                    // 三维网格参数，是否输出网格成果
    std::optional<int> quality_level_mesh;              // 网格重建质量等级
    std::optional<double> model_simplify;               // 模型简化比例，取值范围 0~1
    std::optional<bool> generate_obj;                   // 是否生成 OBJ 格式
    std::optional<bool> generate_fbx;                   // 是否生成 FBX 格式
    std::optional<bool> generate_ply;                   // 是否生成 PLY 格式
    std::optional<bool> generate_osgb;                  // 是否生成 OSGB 格式
    std::optional<bool> generate_b3dm;                  // 是否生成 B3DM 格式
    std::optional<bool> generate_i3s;                   // 是否生成 I3S 格式
    std::optional<bool> generate_maps_2d;               // 二维成果参数，是否生成二维成果
    std::optional<bool> generate_maps_2d_tiff;          // 是否生成 DOM（TIFF 格式）
    std::optional<bool> generate_maps_2d_tiff_cog;      // 是否生成 DOM（COG 云优化 GeoTIFF 格式）
    std::optional<int> resolution_type_2d;              // 分辨率模式，0=高中低, 1=GSD
    std::optional<int> resolution_level_2d;             // 二维成果分辨率，1=高, 2=中, 3=低
    std::optional<double> resolution_gsd_2d;            // GSD 分辨率（m/px），范围 0.0001-10
    std::optional<int> dom_tile_size;                   // DOM 分块大小（像素），0=不分块
    std::optional<bool> use_gcj02;                      // 是否使用国测局坐标系
    std::optional<bool> generate_dem;                   // DEM 成果参数，是否生成 DEM
    std::optional<double> dem_resolution;               // DEM 分辨率（米），范围 [0.05, 50]
    std::optional<bool> generate_contour;               // 等高线参数，是否生成等高线
    std::optional<double> contour_datum;                // 等高线基准面高程
    std::optional<double> contour_interval;             // 等高线间距
    std::optional<double> contour_elevation_annotation_radius; // 等高线高程标注半径
    std::optional<bool> use_contour_filter;             // 是否启用等高线过滤
    std::optional<double> contour_filter_length;        // 等高线过滤最小长度
    std::optional<bool> generate_3dgs;                  // 3DGS 成果参数，是否生成 3DGS
    std::optional<int> quality_level_3dgs;              // 3DGS 质量等级，1/2/4
    std::optional<bool> generate_3dtiles;               // 是否生成 3D Tiles
};

/// 创建全景拼接任务参数
struct CreatePanoramaParams {
    std::string name;                           // 任务名称，必填
    std::vector<std::string> folder_uuids;      // 文件夹 UUID 列表，必填
    std::string wkt;                            // 输出坐标系 WKT 字符串，可选
    std::optional<bool> is_shown_on_map;        // 是否在地图上展示，可选
};

/// 创建重建任务参数
struct CreateReconstructionParams {
    // 必填
    std::vector<int> reconstruction_type;       // 2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达
    std::string name;                           // 任务名称

    // 二选一（输入图像来源）
    std::vector<std::string> folder_uuids;      //媒体(图片)文件夹UUID列表
    std::vector<int> folder_ids;                //媒体(图片)文件夹ID列表(与folder_uuids二选一)

    // 可选基础字段（不设置则不发送）
    std::optional<double> simplified_factor;    //模型简化系数
    std::optional<ReconPredefineArea> predefine_area;        // 兴趣区域（含 polygon_points）
    std::vector<int> generate_model_formats;                 // 生成模型格式列表
    std::string wkt;                                         // 输出坐标系 WKT 字符串
    std::optional<int> quality_level;                        // 重建精细程度，1=高, 2=中, 3=低
    std::optional<int> quality_level_3dgs;                   // 3DGS 质量等级，1=极高, 2=高, 4=预览
    std::optional<bool> is_shown_on_map;                     // 是否在地图上展示
    std::optional<bool> optimize_SfM;                        // 是否优化空三
    std::optional<int> custom_storage_parent_id;             // 自定义存储父目录 ID
    std::optional<ReconQualityReportSetting> quality_report_setting;   // 质量报告设置

    // 可选 Parameter 对象（不设置则不发送）
    std::optional<ReconParameter2D> parameter_2d;             // 二维重建参数
    std::optional<ReconParameter3D> parameter_3d;             // 三维重建参数
    std::optional<ReconParameter3DGS> parameter_3dgs;         // 3DGS 重建参数
    std::optional<ReconParameterLidar> parameter_lidar;       // 激光雷达重建参数
};

/// 上传回调结果
struct TerraUploadCallbackResult {
    long long id = 0;   // 新创建的 File 记录 ID
    std::string uuid;   // 新创建的 File 记录 UUID
    std::string name;   // 文件名（原样返回）
};

/// 批量加载/取消加载结果
struct TerraShowOnMapResult {
    std::vector<int> ids;  // 实际被操作的文件 ID 列表
};

// ─────────────────────────────────────────────────────────
// WebSocket 推送消息结构体 — 新增 terra 相关
// ─────────────────────────────────────────────────────────

/// 模型解压进度推送（reconstruction.unzip_progress）
struct TerraUnzipProgressMsg {
    long long file_id = 0;  // 关联的 File 记录 ID
    std::string uuid;       // 关联的 File 记录 UUID
    std::string name;       // 文件名
    double percent = 0.0;   // 解压进度百分比 [0, 100]
};

/// 模型状态变更推送（model.status_changed）
/// state: 1-解压中 2-成功 3-失败 5-转换中 6-转换成功 7-转换失败
struct TerraModelStatusChangedMsg {
    long long file_id = 0;  // 关联的 File 记录 ID
    std::string uuid;       // 关联的 File 记录 UUID
    std::string name;       // 文件名
    int state = 0;          // 模型新状态
    int file_type = 0;      // 文件类型
    int sub_file_type = 0;  // 子文件类型
    int error_code = 0;     // 错误码（0 表示无错误）
};

/// 展示地图文件项
struct TerraFileShowOnMapItem {
    long long id = 0;       // File 记录 ID
    std::string name;       // 文件名
    int file_type = 0;      // 文件类型
    int sub_file_type = 0;  // 子文件类型
    std::string preview_url; // 预览图签名 URL
};

/// 文件展示地图状态推送（media.file_show_on_map）
struct TerraFileShowOnMapMsg {
    std::vector<TerraFileShowOnMapItem> list;
};

// ─────────────────────────────────────────────────────────────────────
// topic_media_file_hide_on_map — 模型隐藏地图推送（来自 file-storage）
// ─────────────────────────────────────────────────────────────────────

/// 模型隐藏地图消息（Delete/BatchDelete 触发）
struct TerraMediaFileHideOnMapMsg {
    std::vector<int> ids;  ///< 被隐藏的模型文件 ID 列表（uint32）
};

// ─────────────────────────────────────────────────────────────────────
// topic_media_file_show_on_map — 模型显示地图推送（来自 file-storage）
// ─────────────────────────────────────────────────────────────────────

/// 单个上图模型文件摘要
struct TerraMediaFileShowOnMapItem {
    std::string name;          ///< 文件名
    int id = 0;                ///< 文件 ID（uint32）
    double shoot_lat = 0.0;    ///< 拍摄位置纬度
    double shoot_lng = 0.0;    ///< 拍摄位置经度
    double shoot_abs_alt = 0.0;///< 拍摄位置绝对高度（米）
    double shoot_rel_alt = 0.0;///< 拍摄位置相对高度（米）
    double photo_lat = 0.0;    ///< 照片位置纬度
    double photo_lng = 0.0;    ///< 照片位置经度
    double photo_abs_alt = 0.0;///< 照片位置绝对高度（米）
    double photo_rel_alt = 0.0;///< 照片位置相对高度（米）
    std::string preview_url;   ///< 预览图 URL
    int file_type = 0;         ///< 文件大类型（int32）
    int sub_file_type = 0;     ///< 文件子类型（uint32）
};

/// 模型显示地图消息（BatchShowOnMap 触发）
struct TerraMediaFileShowOnMapMsg {
    std::vector<TerraMediaFileShowOnMapItem> list;
};

// === 上传回调请求参数 ===

/// TerraUploadCallbackParams — 模型上传回调请求参数
struct TerraUploadCallbackParams {
    std::string object_key;                 // OSS 对象 key，必填
    std::string name;                       // 文件名（含后缀），必填
    std::optional<long long> parent_id;     // 父目录 ID（0 表示根目录）
    std::optional<std::string> drive_uuid;  // 存储盘 UUID
    std::optional<bool> show_on_map;        // 解压完成后是否自动加载到地图
};

// === 模型重命名请求参数 ===

/// TerraRenameModelParams — 模型重命名请求参数
struct TerraRenameModelParams {
    std::string name;                           // 新名称（不允许含 /），必填
    std::optional<bool> in_uniform_height;      // 等高模式
    std::optional<int> model_display_mode;      // 显示模式：1-普通 2-环绕 3-线框
    std::optional<long long> collected_at;      // 采集时间（毫秒时间戳）
};

// === 加载/取消加载请求参数 ===

/// TerraSelectFilterParams — 全选反选场景的筛选条件
struct TerraSelectFilterParams {
    std::optional<int> selected_parent_id;  // 选中范围的父目录 ID（uint32）
    std::vector<int> except_ids;            // 排除的文件 ID 列表
    std::string key;                        // 关键字模糊搜索
};

/// TerraShowOnMapParams — 批量加载/取消加载模型到地图请求参数
struct TerraShowOnMapParams {
    std::vector<int> ids;                           // 指定文件 ID 列表（与 select_filter 二选一）
    std::optional<bool> show_on_map;                // true=加载，false=取消加载
    std::optional<bool> is_distributed;             // 是否已分发
    std::optional<TerraSelectFilterParams> select_filter; // 全选过滤条件
};

// ─────────────────────────────────────────────────────────────────────
// 1.5 GetModelDetail 相关结构体
// ─────────────────────────────────────────────────────────────────────

/// 模型边界信息（3D 空间盒 + 2D 瓦片层级范围）
/// 注：字段缺失时服务端返回默认值 0.0 / 0，无法与真实值为 0 区分
struct TerraModelBoundaryInfo {
    // ---- 3D 模型边界 ----
    double max_lat = 0.0;    ///< 最大纬度（度）
    double min_lat = 0.0;    ///< 最小纬度（度）
    double max_lng = 0.0;    ///< 最大经度（度）
    double min_lng = 0.0;    ///< 最小经度（度）
    double max_alt = 0.0;    ///< 最大高程（米）
    double min_alt = 0.0;    ///< 最小高程（米）
    double latitude = 0.0;   ///< 模型中心纬度（度）
    double longitude = 0.0;  ///< 模型中心经度（度）
    double altitude = 0.0;   ///< 模型中心高程（米）
    double half_x = 0.0;     ///< 空间盒半长 X 方向（米）
    double half_y = 0.0;     ///< 空间盒半长 Y 方向（米）
    double half_z = 0.0;     ///< 空间盒半长 Z 方向（米）
    std::vector<double> transform;  ///< 4x4 变换矩阵，行主序展平为 16 元素
    // ---- 2D 瓦片边界 ----
    int max_level = 0;       ///< 瓦片最大层级
    int min_level = 0;       ///< 瓦片最小层级
    double start_lat = 0.0;  ///< 起始纬度（度）
    double end_lat = 0.0;    ///< 结束纬度（度）
    double start_lng = 0.0;  ///< 起始经度（度）
    double end_lng = 0.0;    ///< 结束经度（度）
};

/// 点云强度值范围（Lidar 专用）
struct TerraModelIntensityInfo {
    double max_intensity = 0.0;  ///< 最大强度值
    double min_intensity = 0.0;  ///< 最小强度值
};

/// WGS84 二维坐标点（不含高程），用于地理边界多边形
struct TerraModelGeo2DPoint {
    double lng = 0.0;  ///< 经度（度）
    double lat = 0.0;  ///< 纬度（度）
};

/// 位置坐标（含绝对/相对海拔）
struct TerraModelPosition {
    double lng = 0.0;      ///< 经度（度）
    double lat = 0.0;      ///< 纬度（度）
    double abs_alt = 0.0;  ///< 绝对海拔（米，相对海平面）
    double rel_alt = 0.0;  ///< 相对海拔（米，相对起飞点）
};

/// 媒体文件扩展信息（设备/云台/航线等，仅媒体类文件填充）
struct TerraModelExtInfo {
    std::string device;               ///< 拍摄设备 SN 或型号
    double duration = 0.0;            ///< 视频时长（秒）
    std::string photographer;         ///< 拍摄者标识
    std::string resolution;           ///< 分辨率描述，如 "3840x2160"
    int height = 0;                   ///< 图片高度（像素）
    int width = 0;                    ///< 图片宽度（像素）
    double zoom_factor = 0.0;         ///< 变焦倍数
    std::string task_name;            ///< 关联任务名称
    TerraModelPosition shoot_position; ///< 拍摄位置（设备 GPS）
    TerraModelPosition photo_position; ///< 照片位置（GPS 打点）
    std::string line_name;            ///< 关联航线名称
    bool is_original = false;         ///< 是否原图
    double gimbal_pitch = 0.0;        ///< 云台俯仰角（度）
    double gimbal_yaw = 0.0;          ///< 云台偏航角（弧度）
    double gimbal_yaw_degree = 0.0;   ///< 云台偏航角（度）
    double gimbal_roll = 0.0;         ///< 云台横滚角（度）
    double focal_length = 0.0;        ///< 焦距（mm）
    std::string payload_model_key;    ///< 负载型号 key
    std::string drone_model_key;      ///< 飞行器型号 key
    std::string create_time;          ///< 照片创建时间（ISO 字符串）
    std::string wayline_action_uuid;  ///< 航线动作 UUID（触发该照片的具体动作）
    std::string wayline_uuid;         ///< 航线 UUID
    std::string origin_wayline_uuid;  ///< 原始航线 UUID（通过临时航线飞行时保留）
    double digital_zoom_ratio = 0.0;  ///< 数字变焦比
    double length_in35mm_film = 0.0;  ///< 35mm 等效焦距（mm）
};

/// 模型文件详情结果（对应 GET /pf-terra/api/v1/models/files/{file_id}）
struct TerraModelDetailResult {
    long long id = 0;               ///< File 记录 ID
    std::string uuid;               ///< File 记录 UUID
    std::string name;               ///< 文件名
    int file_type = 0;              ///< 文件类型（目录/模型/媒体等）
    int sub_file_type = 0;          ///< 子文件类型（2D/3D/3DGS/Lidar/全景等具体格式）
    long long size = 0;             ///< 文件大小（字节）
    std::string suffix;             ///< 文件后缀（小写），如 ".zip"
    int mod = 0;                    ///< 文件权限位（bitmask）
    bool show_on_map = false;       ///< 是否已加载到地图
    int state = 0;                  ///< 模型状态（1-解压中 2-成功 3-失败 等）
    std::string selected_wkt;       ///< 用户选择的坐标系 WKT 描述
    long long parent_id = 0;        ///< 父目录 ID
    long long update_at = 0;        ///< 更新时间（秒级时间戳）
    long long create_at = 0;        ///< 创建时间（秒级时间戳）
    std::string url;                ///< 原始模型文件签名 URL
    std::string webp_url;           ///< WebP 缩略图签名 URL
    std::string preview_url;        ///< 预览图签名 URL
    std::string thumbnail_url;      ///< 缩略图签名 URL
    std::string object_key;         ///< 原始模型 OSS 对象 key
    std::string index_file_key;     ///< 索引文件 OSS key（tileset.json 等）
    std::string file_prefix;        ///< 模型文件 OSS 前缀
    std::string webp_file_prefix;   ///< WebP 文件 OSS 前缀
    TerraModelBoundaryInfo boundary;         ///< 模型边界信息
    TerraModelIntensityInfo intensity_info;  ///< 点云强度值范围（Lidar 专用）
    double max_return_number = 0.0;          ///< Lidar 最大回波数
    std::vector<TerraModelGeo2DPoint> bounding_points;  ///< 地理边界多边形顶点列表（WGS84）
    TerraModelExtInfo ext;                   ///< 媒体扩展信息（设备/云台/航线等）
    std::vector<std::string> file_tags;      ///< 文件标签列表
    std::string user_name;                   ///< 创建人用户名（私有化不支持，返回空）
};

// ─────────────────────────────────────────────────────────────────────
// 1.6 SetLasWkt 相关结构体
// ─────────────────────────────────────────────────────────────────────

/// LAS 模型坐标系设置参数（对应 POST /pf-terra/api/v1/models/converters）
/// 仅适用于 SubFileType=FormatLas(7) 的模型文件
struct TerraSetLasWktParams {
    std::string file_uuid;  ///< LAS 模型文件 UUID，必填
    std::string wkt;        ///< 坐标系描述，如 "EPSG:4326" 或完整 WKT 字符串，必填
};

/// Terra 重建模块接口，提供三维/二维/全景重建任务管理、模型文件管理、质量报告等功能。
class ITerraBuildModule {
public:
    virtual ~ITerraBuildModule() = default;

    /// @brief 获取重建设置
    /// @param file_uuid 文件 UUID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-terra/api/v1/models/reconstruction/settings/{file_uuid}
    /// @endinternal
    virtual void getReconstructionSettings(
        const std::string& file_uuid,
        std::function<void(FlightHubError err, const TerraBuildSettingsResult& result)> callback) = 0;

    /// @brief 获取质量报告
    /// @param file_uuid 文件 UUID
    /// @param language 语言（如 zh / en）
    /// @param measure_unit_system 度量单位制
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-terra/api/v1/models/reconstruction/{file_uuid}/quality-report
    /// @endinternal
    virtual void getQualityReport(
        const std::string& file_uuid,
        const std::string& language,
        const std::string& measure_unit_system,
        std::function<void(FlightHubError err, const TerraBuildQualityReportResult& result)> callback) = 0;

    /// @brief 获取模型文件列表（分页）
    /// @param page 页码
    /// @param page_size 每页数量
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-terra/api/v1/models/files
    /// @endinternal
    virtual void getModelFileList(
        int page,
        int page_size,
        std::function<void(FlightHubError err, const TerraBuildModelFileListResult& result)> callback) = 0;

    /// @brief 获取模型文件签名 URL
    /// @param file_id 文件 ID
    /// @param type 文件类型。枚举：1=原始文件(默认), 2=转换后 ZIP
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-terra/api/v1/models/files/{file_id}/url
    /// @endinternal
    virtual void getModelFileUrl(
        int file_id,
        int type,
        std::function<void(FlightHubError err, const TerraBuildModelFileUrlResult& result)> callback) = 0;

    /// @brief 创建重建任务
    /// @param params 重建参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-terra/api/v1/models/reconstruction
    /// @endinternal
    virtual void createReconstruction(
        const CreateReconstructionParams& params,
        std::function<void(FlightHubError err, const TerraBuildCreateReconstructionResult& result)> callback) = 0;

    /// @brief 重试重建任务
    /// @param file_uuid 文件 UUID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-terra/api/v1/models/reconstruction/retry
    /// @endinternal
    virtual void retryReconstruction(
        const std::string& file_uuid,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 停止重建任务
    /// @param file_uuid 文件 UUID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-terra/api/v1/models/reconstruction/stop
    /// @endinternal
    virtual void stopReconstruction(
        const std::string& file_uuid,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 获取空三结果
    /// @param file_uuid 文件 UUID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-terra/api/v1/models/reconstruction/{file_uuid}/sfm-result
    /// @endinternal
    virtual void getSfmResult(
        const std::string& file_uuid,
        std::function<void(FlightHubError err, const TerraBuildSfmResult& result)> callback) = 0;

    /// @brief 删除单个模型文件
    /// @param file_id 文件 ID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | DELETE /pf-terra/api/v1/models/reconstruction/files/{file_id}
    /// @endinternal
    virtual void deleteModelFile(
        int file_id,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 批量删除模型文件
    /// @param file_ids 文件 ID 列表
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | DELETE /model/api/v1/files?ids=1,2,3
    /// @endinternal
    virtual void batchDeleteModelFiles(
        const std::vector<int>& file_ids,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 创建全景拼接任务
    /// @param params 全景参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-terra/api/v1/models/panorama
    /// @endinternal
    virtual void createPanorama(
        const CreatePanoramaParams& params,
        std::function<void(FlightHubError err, const TerraBuildCreatePanoramaResult& result)> callback) = 0;

    /// @brief 获取重建图像列表
    /// @param file_uuid 文件 UUID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-terra/api/v1/images/reconstruction/{file_uuid}
    /// @endinternal
    virtual void getReconstructionImages(
        const std::string& file_uuid,
        std::function<void(FlightHubError err, const TerraBuildReconstructionImagesResult& result)> callback) = 0;

    /// @brief 订阅重建任务状态变更推送
    /// @param callback 状态变更回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_reconstruction_status_changed_event
    /// @endinternal
    virtual int listenReconstructionStatusChanged(
        const std::function<void(const TerraBuildReconstructionStatusChanged& msg)>& callback) = 0;

    /// @brief 取消订阅重建任务状态变更推送
    /// @param listen_id 监听 ID
    virtual void cancelListenReconstructionStatusChanged(int listen_id) = 0;

    /// @brief 模型文件上传回调
    /// @param params 上传回调参数
    /// @param callback 结果回调（返回 file id/uuid/name）
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-terra/api/v1/models/files/upload-callback
    /// @endinternal
    virtual void uploadModelCallback(
        const TerraUploadCallbackParams& params,
        std::function<void(FlightHubError err, const TerraUploadCallbackResult& result)> callback) = 0;

    /// @brief 模型重命名
    /// @param file_id 文件 ID
    /// @param params 重命名参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /model/api/v1/files/{id}
    /// @endinternal
    virtual void renameModel(
        int file_id,
        const TerraRenameModelParams& params,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 批量加载/取消加载模型到地图
    /// @param params 加载参数（ids 或 select_filter + show_on_map）
    /// @param callback 结果回调（返回实际被操作的 file id 列表）
    ///
    /// @internal
    /// 链路: HTTP | PUT /model/api/v1/files
    /// @endinternal
    virtual void showModelOnMap(
        const TerraShowOnMapParams& params,
        std::function<void(FlightHubError err, const TerraShowOnMapResult& result)> callback) = 0;

    /// @brief 订阅模型解压进度推送
    /// @param callback 解压进度回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_reconstruction_unzip_progress
    /// @endinternal
    virtual int listenTerraUnzipProgress(
        const std::function<void(const TerraUnzipProgressMsg& msg)>& callback) = 0;

    /// @brief 取消订阅模型解压进度推送
    /// @param listen_id 监听 ID
    virtual void cancelListenTerraUnzipProgress(int listen_id) = 0;

    /// @brief 订阅模型状态变更推送
    /// @param callback 状态变更回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_model_status_changed
    /// @endinternal
    virtual int listenTerraModelStatusChanged(
        const std::function<void(const TerraModelStatusChangedMsg& msg)>& callback) = 0;

    /// @brief 取消订阅模型状态变更推送
    /// @param listen_id 监听 ID
    virtual void cancelListenTerraModelStatusChanged(int listen_id) = 0;

    /// @brief 订阅文件展示地图状态推送
    /// @param callback 展示状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_model_file_show_on_map
    /// @endinternal
    virtual int listenTerraFileShowOnMap(
        const std::function<void(const TerraFileShowOnMapMsg& msg)>& callback) = 0;

    /// @brief 取消订阅文件展示地图状态推送
    /// @param listen_id 监听 ID
    virtual void cancelListenTerraFileShowOnMap(int listen_id) = 0;

    /// @brief 订阅模型隐藏地图推送
    /// @return 监听 ID
    /// @internal 链路: WebSocket | topic: topic_media_file_hide_on_map @endinternal
    virtual int listenTerraMediaFileHideOnMap(
        const std::function<void(const TerraMediaFileHideOnMapMsg& msg)>& callback) = 0;

    virtual void cancelListenTerraMediaFileHideOnMap(int listen_id) = 0;

    /// @brief 订阅模型显示地图推送（来自 file-storage BatchShowOnMap）
    /// @return 监听 ID
    /// @internal 链路: WebSocket | topic: topic_media_file_show_on_map @endinternal
    virtual int listenTerraMediaFileShowOnMap(
        const std::function<void(const TerraMediaFileShowOnMapMsg& msg)>& callback) = 0;

    virtual void cancelListenTerraMediaFileShowOnMap(int listen_id) = 0;

    // ─────────────────────────────────────────────────────────────────────
    // 1.5 获取模型文件详情
    // ─────────────────────────────────────────────────────────────────────

    /// @brief 获取模型文件详情
    /// @param file_id 文件 ID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-terra/api/v1/models/files/{file_id}
    /// @endinternal
    virtual void getModelDetail(
        int file_id,
        std::function<void(FlightHubError err, const TerraModelDetailResult& result)> callback) = 0;

    // ─────────────────────────────────────────────────────────────────────
    // 1.6 设置 LAS 模型坐标系并触发格式转换
    // ─────────────────────────────────────────────────────────────────────

    /// @brief 设置 LAS 模型坐标系并触发格式转换
    /// @param params 参数（file_uuid 必填，wkt 必填）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-terra/api/v1/models/converters
    /// @endinternal
    virtual void setLasWkt(
        const TerraSetLasWktParams& params,
        std::function<void(FlightHubError err)> callback) = 0;
};

}  // namespace flight_hub_sdk
