// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 模型文件详情结果
 * 对应接口: GET /pf-terra/api/v1/models/files/{file_id}
 *
 * <p>边界字段（boundary 相关）、强度字段及 maxReturnNumber 在服务端该字段不存在时
 * 返回默认值 0.0，无法与真实值 0.0 区分，调用方不应依赖 0.0 来判断字段不存在。</p>
 */
public final class TerraModelDetailResult {

    // ── 基础字段 ──────────────────────────────────────────────────────────

    /** File 记录 ID */
    private long id;
    /** File 记录 UUID */
    private String uuid = "";
    /** 文件名 */
    private String name = "";
    /** 文件类型（目录/模型/媒体等） */
    private int fileType;
    /** 子文件类型（2D/3D/3DGS/Lidar/全景等具体格式） */
    private int subFileType;
    /** 文件大小（字节） */
    private long size;
    /** 文件后缀（小写），如 ".zip" */
    private String suffix = "";
    /** 文件权限位（bitmask） */
    private int mod;
    /** 是否已加载到地图 */
    private boolean showOnMap;
    /** 模型状态（1-解压中 2-成功 3-失败 等） */
    private int state;
    /** 用户选择的坐标系 WKT 描述 */
    private String selectedWkt = "";
    /** 父目录 ID */
    private long parentId;
    /** 更新时间（秒级时间戳） */
    private long updateAt;
    /** 创建时间（秒级时间戳） */
    private long createAt;
    /** 原始模型文件签名 URL */
    private String url = "";
    /** WebP 缩略图签名 URL */
    private String webpUrl = "";
    /** 预览图签名 URL */
    private String previewUrl = "";
    /** 缩略图签名 URL */
    private String thumbnailUrl = "";
    /** 原始模型 OSS 对象 key */
    private String objectKey = "";
    /** 索引文件 OSS key（tileset.json 等） */
    private String indexFileKey = "";
    /** 模型文件 OSS 前缀 */
    private String filePrefix = "";
    /** WebP 文件 OSS 前缀 */
    private String webpFilePrefix = "";

    // ── 3D 边界信息 ───────────────────────────────────────────────────────

    /** 最大纬度（度） */
    private double maxLat;
    /** 最小纬度（度） */
    private double minLat;
    /** 最大经度（度） */
    private double maxLng;
    /** 最小经度（度） */
    private double minLng;
    /** 最大高程（米） */
    private double maxAlt;
    /** 最小高程（米） */
    private double minAlt;
    /** 模型中心纬度（度） */
    private double latitude;
    /** 模型中心经度（度） */
    private double longitude;
    /** 模型中心高程（米） */
    private double altitude;
    /** 空间盒半长 X 方向（米） */
    private double halfX;
    /** 空间盒半长 Y 方向（米） */
    private double halfY;
    /** 空间盒半长 Z 方向（米） */
    private double halfZ;
    /** 4x4 变换矩阵，行主序展平为 16 元素 */
    private List<Double> transform = Collections.emptyList();

    // ── 2D 瓦片边界 ───────────────────────────────────────────────────────

    /** 瓦片最大层级 */
    private int maxLevel;
    /** 瓦片最小层级 */
    private int minLevel;
    /** 起始纬度（度） */
    private double startLat;
    /** 结束纬度（度） */
    private double endLat;
    /** 起始经度（度） */
    private double startLng;
    /** 结束经度（度） */
    private double endLng;

    // ── 点云强度信息（Lidar 专用）─────────────────────────────────────────

    /** 最大强度值 */
    private double maxIntensity;
    /** 最小强度值 */
    private double minIntensity;

    // ── Lidar 最大回波数 ───────────────────────────────────────────────────

    /** Lidar 最大回波数 */
    private double maxReturnNumber;

    // ── 地理边界多边形 ────────────────────────────────────────────────────

    /** 地理边界多边形顶点列表（WGS84） */
    private List<TerraGeo2DPoint> boundingPoints = Collections.emptyList();

    // ── 媒体扩展信息（设备/云台/航线等，仅媒体类文件填充）─────────────────

    /** 拍摄设备 SN 或型号 */
    private String device = "";
    /** 视频时长（秒） */
    private double duration;
    /** 拍摄者标识 */
    private String photographer = "";
    /** 分辨率描述，如 "3840x2160" */
    private String resolution = "";
    /** 图片高度（像素） */
    private int extHeight;
    /** 图片宽度（像素） */
    private int extWidth;
    /** 变焦倍数 */
    private double zoomFactor;
    /** 关联任务名称 */
    private String taskName = "";
    /** 拍摄位置经度（设备 GPS，度） */
    private double shootLng;
    /** 拍摄位置纬度（设备 GPS，度） */
    private double shootLat;
    /** 拍摄位置绝对海拔（米，相对海平面） */
    private double shootAbsAlt;
    /** 拍摄位置相对海拔（米，相对起飞点） */
    private double shootRelAlt;
    /** 照片 GPS 打点经度（度） */
    private double photoLng;
    /** 照片 GPS 打点纬度（度） */
    private double photoLat;
    /** 照片 GPS 打点绝对海拔（米） */
    private double photoAbsAlt;
    /** 照片 GPS 打点相对海拔（米） */
    private double photoRelAlt;
    /** 关联航线名称 */
    private String lineName = "";
    /** 是否原图 */
    private boolean isOriginal;
    /** 云台俯仰角（度） */
    private double gimbalPitch;
    /** 云台偏航角（弧度） */
    private double gimbalYaw;
    /** 云台偏航角（度） */
    private double gimbalYawDegree;
    /** 云台横滚角（度） */
    private double gimbalRoll;
    /** 焦距（mm） */
    private double focalLength;
    /** 负载型号 key */
    private String payloadModelKey = "";
    /** 飞行器型号 key */
    private String droneModelKey = "";
    /** 照片创建时间（ISO 字符串） */
    private String createTime = "";
    /** 航线动作 UUID（触发该照片的具体动作） */
    private String waylineActionUuid = "";
    /** 航线 UUID */
    private String waylineUuid = "";
    /** 原始航线 UUID（通过临时航线飞行时保留） */
    private String originWaylineUuid = "";
    /** 数字变焦比 */
    private double digitalZoomRatio;
    /** 35mm 等效焦距（mm） */
    private double lengthIn35mmFilm;

    // ── 标签与用户 ────────────────────────────────────────────────────────

    /** 文件标签列表 */
    private List<String> fileTags = Collections.emptyList();
    /** 创建人用户名（私有化不支持，返回空） */
    private String userName = "";

    public TerraModelDetailResult() {}

    // ── Getters / Setters ─────────────────────────────────────────────────

    public long getId() { return id; } public void setId(long v) { id = v; }
    public String getUuid() { return uuid; } public void setUuid(String v) { uuid = v != null ? v : ""; }
    public String getName() { return name; } public void setName(String v) { name = v != null ? v : ""; }
    public int getFileType() { return fileType; } public void setFileType(int v) { fileType = v; }
    public int getSubFileType() { return subFileType; } public void setSubFileType(int v) { subFileType = v; }
    public long getSize() { return size; } public void setSize(long v) { size = v; }
    public String getSuffix() { return suffix; } public void setSuffix(String v) { suffix = v != null ? v : ""; }
    public int getMod() { return mod; } public void setMod(int v) { mod = v; }
    public boolean isShowOnMap() { return showOnMap; } public void setShowOnMap(boolean v) { showOnMap = v; }
    public int getState() { return state; } public void setState(int v) { state = v; }
    public String getSelectedWkt() { return selectedWkt; } public void setSelectedWkt(String v) { selectedWkt = v != null ? v : ""; }
    public long getParentId() { return parentId; } public void setParentId(long v) { parentId = v; }
    public long getUpdateAt() { return updateAt; } public void setUpdateAt(long v) { updateAt = v; }
    public long getCreateAt() { return createAt; } public void setCreateAt(long v) { createAt = v; }
    public String getUrl() { return url; } public void setUrl(String v) { url = v != null ? v : ""; }
    public String getWebpUrl() { return webpUrl; } public void setWebpUrl(String v) { webpUrl = v != null ? v : ""; }
    public String getPreviewUrl() { return previewUrl; } public void setPreviewUrl(String v) { previewUrl = v != null ? v : ""; }
    public String getThumbnailUrl() { return thumbnailUrl; } public void setThumbnailUrl(String v) { thumbnailUrl = v != null ? v : ""; }
    public String getObjectKey() { return objectKey; } public void setObjectKey(String v) { objectKey = v != null ? v : ""; }
    public String getIndexFileKey() { return indexFileKey; } public void setIndexFileKey(String v) { indexFileKey = v != null ? v : ""; }
    public String getFilePrefix() { return filePrefix; } public void setFilePrefix(String v) { filePrefix = v != null ? v : ""; }
    public String getWebpFilePrefix() { return webpFilePrefix; } public void setWebpFilePrefix(String v) { webpFilePrefix = v != null ? v : ""; }
    public double getMaxLat() { return maxLat; } public void setMaxLat(double v) { maxLat = v; }
    public double getMinLat() { return minLat; } public void setMinLat(double v) { minLat = v; }
    public double getMaxLng() { return maxLng; } public void setMaxLng(double v) { maxLng = v; }
    public double getMinLng() { return minLng; } public void setMinLng(double v) { minLng = v; }
    public double getMaxAlt() { return maxAlt; } public void setMaxAlt(double v) { maxAlt = v; }
    public double getMinAlt() { return minAlt; } public void setMinAlt(double v) { minAlt = v; }
    public double getLatitude() { return latitude; } public void setLatitude(double v) { latitude = v; }
    public double getLongitude() { return longitude; } public void setLongitude(double v) { longitude = v; }
    public double getAltitude() { return altitude; } public void setAltitude(double v) { altitude = v; }
    public double getHalfX() { return halfX; } public void setHalfX(double v) { halfX = v; }
    public double getHalfY() { return halfY; } public void setHalfY(double v) { halfY = v; }
    public double getHalfZ() { return halfZ; } public void setHalfZ(double v) { halfZ = v; }
    public List<Double> getTransform() { return transform; }
    public void setTransform(List<Double> v) { transform = v != null ? v : Collections.emptyList(); }
    public int getMaxLevel() { return maxLevel; } public void setMaxLevel(int v) { maxLevel = v; }
    public int getMinLevel() { return minLevel; } public void setMinLevel(int v) { minLevel = v; }
    public double getStartLat() { return startLat; } public void setStartLat(double v) { startLat = v; }
    public double getEndLat() { return endLat; } public void setEndLat(double v) { endLat = v; }
    public double getStartLng() { return startLng; } public void setStartLng(double v) { startLng = v; }
    public double getEndLng() { return endLng; } public void setEndLng(double v) { endLng = v; }
    public double getMaxIntensity() { return maxIntensity; } public void setMaxIntensity(double v) { maxIntensity = v; }
    public double getMinIntensity() { return minIntensity; } public void setMinIntensity(double v) { minIntensity = v; }
    public double getMaxReturnNumber() { return maxReturnNumber; } public void setMaxReturnNumber(double v) { maxReturnNumber = v; }
    public List<TerraGeo2DPoint> getBoundingPoints() { return boundingPoints; }
    public void setBoundingPoints(List<TerraGeo2DPoint> v) { boundingPoints = v != null ? v : Collections.emptyList(); }
    public String getDevice() { return device; } public void setDevice(String v) { device = v != null ? v : ""; }
    public double getDuration() { return duration; } public void setDuration(double v) { duration = v; }
    public String getPhotographer() { return photographer; } public void setPhotographer(String v) { photographer = v != null ? v : ""; }
    public String getResolution() { return resolution; } public void setResolution(String v) { resolution = v != null ? v : ""; }
    public int getExtHeight() { return extHeight; } public void setExtHeight(int v) { extHeight = v; }
    public int getExtWidth() { return extWidth; } public void setExtWidth(int v) { extWidth = v; }
    public double getZoomFactor() { return zoomFactor; } public void setZoomFactor(double v) { zoomFactor = v; }
    public String getTaskName() { return taskName; } public void setTaskName(String v) { taskName = v != null ? v : ""; }
    public double getShootLng() { return shootLng; } public void setShootLng(double v) { shootLng = v; }
    public double getShootLat() { return shootLat; } public void setShootLat(double v) { shootLat = v; }
    public double getShootAbsAlt() { return shootAbsAlt; } public void setShootAbsAlt(double v) { shootAbsAlt = v; }
    public double getShootRelAlt() { return shootRelAlt; } public void setShootRelAlt(double v) { shootRelAlt = v; }
    public double getPhotoLng() { return photoLng; } public void setPhotoLng(double v) { photoLng = v; }
    public double getPhotoLat() { return photoLat; } public void setPhotoLat(double v) { photoLat = v; }
    public double getPhotoAbsAlt() { return photoAbsAlt; } public void setPhotoAbsAlt(double v) { photoAbsAlt = v; }
    public double getPhotoRelAlt() { return photoRelAlt; } public void setPhotoRelAlt(double v) { photoRelAlt = v; }
    public String getLineName() { return lineName; } public void setLineName(String v) { lineName = v != null ? v : ""; }
    public boolean isOriginal() { return isOriginal; } public void setOriginal(boolean v) { isOriginal = v; }
    public double getGimbalPitch() { return gimbalPitch; } public void setGimbalPitch(double v) { gimbalPitch = v; }
    public double getGimbalYaw() { return gimbalYaw; } public void setGimbalYaw(double v) { gimbalYaw = v; }
    public double getGimbalYawDegree() { return gimbalYawDegree; } public void setGimbalYawDegree(double v) { gimbalYawDegree = v; }
    public double getGimbalRoll() { return gimbalRoll; } public void setGimbalRoll(double v) { gimbalRoll = v; }
    public double getFocalLength() { return focalLength; } public void setFocalLength(double v) { focalLength = v; }
    public String getPayloadModelKey() { return payloadModelKey; } public void setPayloadModelKey(String v) { payloadModelKey = v != null ? v : ""; }
    public String getDroneModelKey() { return droneModelKey; } public void setDroneModelKey(String v) { droneModelKey = v != null ? v : ""; }
    public String getCreateTime() { return createTime; } public void setCreateTime(String v) { createTime = v != null ? v : ""; }
    public String getWaylineActionUuid() { return waylineActionUuid; } public void setWaylineActionUuid(String v) { waylineActionUuid = v != null ? v : ""; }
    public String getWaylineUuid() { return waylineUuid; } public void setWaylineUuid(String v) { waylineUuid = v != null ? v : ""; }
    public String getOriginWaylineUuid() { return originWaylineUuid; } public void setOriginWaylineUuid(String v) { originWaylineUuid = v != null ? v : ""; }
    public double getDigitalZoomRatio() { return digitalZoomRatio; } public void setDigitalZoomRatio(double v) { digitalZoomRatio = v; }
    public double getLengthIn35mmFilm() { return lengthIn35mmFilm; } public void setLengthIn35mmFilm(double v) { lengthIn35mmFilm = v; }
    public List<String> getFileTags() { return fileTags; }
    public void setFileTags(List<String> v) { fileTags = v != null ? v : Collections.emptyList(); }
    public String getUserName() { return userName; } public void setUserName(String v) { userName = v != null ? v : ""; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraModelDetailResult that = (TerraModelDetailResult) o;
        return id == that.id && Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() { return Objects.hash(id, uuid); }

    @Override
    public String toString() {
        return "TerraModelDetailResult{id=" + id + ", uuid='" + uuid + "', name='" + name
                + "', fileType=" + fileType + ", subFileType=" + subFileType
                + ", state=" + state + ", showOnMap=" + showOnMap + ", size=" + size + "}";
    }
}
