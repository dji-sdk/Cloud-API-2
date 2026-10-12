// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "value.h"
#include <functional>
#include <optional>
#include <string>
#include <vector>

namespace flight_hub_sdk {

// ============================================================
// 公共子类型
// ============================================================

/// 要素渲染属性（GeoJSON Feature Properties，对齐 proto ElementProperties）
struct ElementProperties {
    std::string color;          ///< 渲染颜色，HEX 格式（如 "#2D8CF0"）
    bool clamp_to_ground = false; ///< 是否贴地渲染
    bool is3d = false;          ///< 是否 3D 要素
    bool is_transparent = false; ///< 要素是否透明渲染
    int font_size = 0;          ///< 字体大小
    int icon = 0;               ///< 图标 ID
};

/// 地理坐标点（对应 {lng, lat, alt}）
struct ElementGeoPoint {
    double lng = 0.0;  ///< 经度
    double lat = 0.0;  ///< 纬度
    double alt = 0.0;  ///< 高度（米）
};

/// 要素几何数据（GeoJSON Geometry，对齐 proto ElementGeometry）
struct ElementGeometry {
    std::string type;            ///< GeoJSON 几何类型："Point"/"LineString"/"Polygon"
    double radius = 0.0;         ///< 圆半径（米），仅圆要素（type=7）使用
    /// 坐标数组 raw JSON，格式由 type 决定：
    ///   Point:      [lng, lat, alt]              例: [114.06, 22.55, 12.6]
    ///   LineString: [[lng,lat,alt],...]          例: [[114.03,22.53,0],[114.04,22.53,0]]
    ///   Polygon:    [[[lng,lat,alt],...]]        例: [[[114.22,30.55,0],[114.22,30.55,0]]]
    ///   圆(type=7): 同 Point 格式，配合 radius 使用  例: [114.06,22.53,0], radius=825.88
    std::string coordinates;
    /// 面包屑轨迹点 raw JSON，格式同 coordinates
    std::string bread_crumbs;
};

/// 媒体图片（对齐 proto ElementMediaImage）
struct ElementMediaImage {
    std::string object_key;    ///< OSS 对象 key
    int media_file_id = 0;     ///< 媒体库文件 ID
    std::string media_file_uuid; ///< 媒体库文件 UUID
};

/// 要素绑定的媒体资源（对齐 proto ElementMediaResources）
struct ElementMediaResources {
    std::vector<ElementMediaImage> images; ///< 图片列表
};

/// 要素几何内容，GeoJSON Feature 结构（对齐 proto ElementResourceContent）
/// 用于 CreateElement.resource.content 和 UpdateElement.content
struct ElementResourceContent {
    std::string type;                        ///< GeoJSON 类型，固定传 "Feature"
    ElementProperties properties;            ///< 渲染属性
    ElementGeometry geometry;                ///< 几何数据
    ElementMediaResources media_resources;   ///< 绑定的媒体资源；无绑定图片时不传
};

/// 要素资源（对齐 proto ElementResourceItem）
struct ElementResourceItem {
    ElementResourceContent content; ///< 几何内容（GeoJSON Feature）
    int type = 0;                   ///< 几何类型：0-点 1-线 2-面 7-圆
    std::string remark;             ///< 备注
};

/// 要素排序项（移动要素时使用）
struct ElementResourceOrderItem {
    std::string id;  ///< 要素 UUID
    int order = 0;   ///< 排序序号
};

// ============================================================
// HTTP 请求/响应结构体
// ============================================================

/// CreateElement 请求参数（POST /pf-map/api/v1/elements/create）
struct CreateElementParams {
    std::string id;                    ///< 可选：自定义要素 UUID，空时服务端自动生成
    int source = 0;                    ///< 要素来源类型（埋点用）
    ElementResourceItem resource;      ///< 要素资源（几何类型 + GeoJSON Feature），必填
    std::string name;                  ///< 要素名称；空时服务端使用创建者昵称 + 自增序号
    int element_source = 0;            ///< 埋点来源标记
    std::string desc;                  ///< 描述
};

/// CreateElement 响应
struct CreateElementResult {
    std::string id;  ///< 新建要素 UUID
};

/// UpdateElement 请求参数（PUT /pf-map/api/v1/elements/update/{id}）
/// 所有字段可选，仅传入需要修改的字段；传入 target_layer_id 时触发移动操作
struct UpdateElementParams {
    std::string id;                                         ///< 要素 UUID，必填
    std::optional<std::string> name;                        ///< 要素名称
    std::optional<ElementResourceContent> content;          ///< 几何数据（GeoJSON Feature），整体覆盖
    std::optional<int> display;                             ///< 显示状态：1-显示 0-隐藏
    std::optional<std::string> remark;                      ///< 备注
    std::optional<int> status;                              ///< 元素状态
    std::optional<int> elevation_load_status;               ///< 高程加载状态（仅 Web）
    std::optional<int> user_status;                         ///< 用户个人显影状态
    std::string target_layer_id;                            ///< 目标图层 UUID；非空时触发移动
    std::vector<ElementResourceOrderItem> orders;           ///< 移动后目标图层内的排序列表
};

/// UpdateElement 响应
struct UpdateElementResult {
    std::string id;  ///< 被更新的要素 UUID
};

/// ListElementHierarchy 请求参数（GET /pf-map/api/v1/elements/lists）
struct ListElementHierarchyParams {
    std::string group_id;                    ///< 指定图层 UUID 过滤（空=查 workspace 下全部）
    std::string pid;                         ///< 父目录 UUID（空=查根目录）
    std::optional<bool> is_distributed;      ///< true=仅返回已分发的图层
    std::optional<int> type;                 ///< 图层类型筛选
    std::optional<std::string> element_uuid; ///< 定位特定元素
    bool exclude_copy = false;               ///< true=排除复制中的图层
};

/// 要素资源体（对齐 proto ElementResourceData / resp.HandwritingListItem）
/// 基础几何（点/线/面/圆及视锥）为完整结构；3D 模型等非基础类型的 resource 形状不同
struct ElementResourceData {
    ElementResourceContent content; ///< 几何内容（GeoJSON Feature）
    int type = 0;                   ///< 几何类型：0-点 1-线 2-面 7-圆
    std::string user_name;          ///< 创建者昵称
    std::string user_id;            ///< 创建者 UID
    std::string remark;             ///< 备注
};

/// ListElementHierarchy 响应（树形结构 JSON 透传）
/// 要素节点（对应服务端 ElementItem）
struct ElementItemInfo {
    std::string id;
    std::string name;
    std::string desc;
    int source = 0;                ///< 0=历史/app上传 1=地图创建 2=模型对比创建 3=KML导入
    int order = 0;
    int status = 0;                ///< 管理员可见状态：0=隐藏 1=显示
    int display = 0;               ///< 前端渲染开关：0=不渲染 1=渲染
    ElementResourceData resource;  ///< 要素资源体
    long long update_time = 0;     ///< 最后更新时间（毫秒时间戳）
    long long create_time = 0;     ///< 创建时间（毫秒时间戳）
    int elevation_load_status = 0; ///< 高程数据加载状态：0=未加载 1=已加载
    bool notify = false;           ///< 是否需要实时推送（C2C 图层为 true）
};

/// 图层节点（对应服务端 ElementGroupOutput，支持递归子图层）
struct ElementGroupInfo {
    std::string id;
    std::string name;
    int order = 0;
    bool is_distributed = false;   ///< 是否已分发到子工作区
    int type = 0;                  ///< 图层类型：0=普通 1=默认 2=共享 3=C2C 4=视锥 5=DataHub 6=自动派遣 7=大疆行业APP
    bool is_lock = false;          ///< 图层是否被锁定（锁定后不可编辑/删除）
    std::string tri_state;         ///< 显隐三态："all"=全显 "none"=全隐 "half"=部分显
    long long create_time = 0;     ///< 创建时间（毫秒时间戳）
    std::string pid;               ///< 父图层 UUID（根图层为空）
    std::vector<ElementItemInfo> elements;   ///< 直属要素列表
    std::vector<ElementGroupInfo> children;  ///< 子图层列表（递归）
};

/// ListElementHierarchy 响应
struct ListElementHierarchyResult {
    std::vector<ElementGroupInfo> list;  ///< 图层树形列表
};

/// SetElementVisibility 请求参数（PUT /pf-map/api/v1/elements/{id}）
struct SetElementVisibilityParams {
    std::string id;   ///< 要素 UUID，必填
    int display = 0;  ///< 显示状态：1-显示 0-隐藏，必填
};

/// SetElementVisibility 响应
struct SetElementVisibilityResult {
    std::string id;  ///< 被操作的要素 UUID
};

/// DeleteElement 请求参数（DELETE /pf-map/api/v1/elements/delete/{id}）
struct DeleteElementParams {
    std::string id;              ///< 要素 UUID，必填
    bool from_batch_delete = false; ///< 是否来自批量删除
};

/// DeleteElement 响应
struct DeleteElementResult {
    std::string id;  ///< 被删除的要素 UUID
};

// ============================================================
// WS 推送消息结构体
// ============================================================

/// 要素创建推送消息（topic_element_create）
struct ElementCreateMsg {
    std::string id;        ///< 新建要素 UUID
    std::string group_id;  ///< 所属图层 UUID
    std::string name;      ///< 要素名称
    int display = 0;       ///< 显示状态：1-显示 0-隐藏
};

/// 要素更新推送消息（topic_element_update）
struct ElementUpdateMsg {
    std::string id;        ///< 被更新的要素 UUID
    std::string group_id;  ///< 所属图层 UUID
    std::string name;      ///< 更新后的名称
    int display = 0;       ///< 更新后的显示状态：1-显示 0-隐藏
};

/// 要素删除推送消息（topic_element_delete）
struct ElementDeleteMsg {
    std::string id;        ///< 被删除的要素 UUID
    std::string group_id;  ///< 所属图层 UUID
};

// ============================================================
// IElementModule — 元素管理模块接口
// ============================================================

class IElementModule {
public:
    virtual ~IElementModule() = default;

    // ── HTTP 接口 ──────────────────────────────────────────────────────────

    /// @brief 创建要素
    /// @param params 创建参数（resource 必填）
    /// @param callback 结果回调
    /// @internal 链路: HTTP | POST /pf-map/api/v1/elements/create @endinternal
    virtual void createElement(
        const CreateElementParams& params,
        std::function<void(FlightHubError err, const CreateElementResult& result)> callback) = 0;

    /// @brief 更新要素属性或移动要素到目标图层
    /// @param params 更新参数（id 必填，其余字段可选）
    /// @param callback 结果回调
    /// @internal 链路: HTTP | PUT /pf-map/api/v1/elements/update/{id} @endinternal
    virtual void updateElement(
        const UpdateElementParams& params,
        std::function<void(FlightHubError err, const UpdateElementResult& result)> callback) = 0;

    /// @brief 查询元素层级列表（目录树 JSON 透传）
    /// @param params 查询参数
    /// @param callback 结果回调（data 为 []ElementGroupOutput 树形结构 JSON）
    /// @internal 链路: HTTP | GET /pf-map/api/v1/elements/lists @endinternal
    virtual void listElementHierarchy(
        const ListElementHierarchyParams& params,
        std::function<void(FlightHubError err, const ListElementHierarchyResult& result)> callback) = 0;

    /// @brief 设置要素显示/隐藏状态（PaaS 专用简化接口）
    /// @param params 参数（id + display 必填）
    /// @param callback 结果回调
    /// @internal 链路: HTTP | PUT /pf-map/api/v1/elements/{id} @endinternal
    virtual void setElementVisibility(
        const SetElementVisibilityParams& params,
        std::function<void(FlightHubError err, const SetElementVisibilityResult& result)> callback) = 0;

    /// @brief 删除要素（仅限基础几何类型：点/线/面/圆）
    /// @param params 参数（id 必填）
    /// @param callback 结果回调
    /// @internal 链路: HTTP | DELETE /pf-map/api/v1/elements/delete/{id} @endinternal
    virtual void deleteElement(
        const DeleteElementParams& params,
        std::function<void(FlightHubError err, const DeleteElementResult& result)> callback) = 0;

    // ── WS 订阅接口 ────────────────────────────────────────────────────────

    /// @brief 订阅要素创建事件
    /// @return 监听 ID
    /// @internal 链路: WebSocket | topic: topic_element_create @endinternal
    virtual int listenElementCreate(
        const std::function<void(const ElementCreateMsg& msg)>& callback) = 0;

    virtual void cancelListenElementCreate(int listen_id) = 0;

    /// @brief 订阅要素更新事件
    /// @return 监听 ID
    /// @internal 链路: WebSocket | topic: topic_element_update @endinternal
    virtual int listenElementUpdate(
        const std::function<void(const ElementUpdateMsg& msg)>& callback) = 0;

    virtual void cancelListenElementUpdate(int listen_id) = 0;

    /// @brief 订阅要素删除事件
    /// @return 监听 ID
    /// @internal 链路: WebSocket | topic: topic_element_delete @endinternal
    virtual int listenElementDelete(
        const std::function<void(const ElementDeleteMsg& msg)>& callback) = 0;

    virtual void cancelListenElementDelete(int listen_id) = 0;
};

}  // namespace flight_hub_sdk
