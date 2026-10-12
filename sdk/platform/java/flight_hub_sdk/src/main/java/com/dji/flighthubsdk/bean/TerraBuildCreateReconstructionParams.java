// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** Terra Build 创建重建任务的请求参数 */
public final class TerraBuildCreateReconstructionParams {
    private List<Integer> reconstructionType;
    private String name;
    private List<String> folderUuids;
    private List<Integer> folderIds;
    private double simplifiedFactor;
    private boolean simplifiedFactorSet;
    private String wkt;
    private int qualityLevel;
    private boolean qualityLevelSet;
    private int qualityLevel3dgs;
    private boolean qualityLevel3dgsSet;
    private boolean isShownOnMap;
    private boolean isShownOnMapSet;
    private boolean optimizeSfM;
    private boolean optimizeSfMSet;
    private int customStorageParentId;
    private boolean customStorageParentIdSet;
    private List<Integer> generateModelFormats;
    private String qualityReportLanguage;
    private String qualityReportMeasureUnitSystem;
    private TerraBuildPredefineArea predefineArea;
    private TerraBuildParameter2D parameter2d;
    private TerraBuildParameter3D parameter3d;
    private TerraBuildParameter3DGS parameter3dgs;
    private TerraBuildParameterLidar parameterLidar;
    public TerraBuildCreateReconstructionParams() {
    }

/**
     * 构造 TerraBuildCreateReconstructionParams。
     *
     * @param reconstructionType 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达
     * @param name 任务名称
     * @param folderUuids 媒体文件夹 UUID 列表（与 folderIds 二选一）
     * @param folderIds 媒体文件夹 ID 列表（与 folderUuids 二选一）
     * @param simplifiedFactor 模型简化系数
     * @param simplifiedFactorSet SimplifiedFactor设置
     * @param wkt 输出坐标系 WKT 字符串
     * @param qualityLevel 重建精细程度，1=高, 2=中, 3=低
     * @param qualityLevelSet QualityLevel设置
     * @param qualityLevel3dgs 3DGS 质量等级，1=极高, 2=高, 4=预览
     * @param qualityLevel3dgsSet QualityLevel3dgs设置
     * @param isShownOnMap IsShownOn地图
     * @param isShownOnMapSet IsShownOn地图设置
     * @param optimizeSfM 是否优化空三
     * @param optimizeSfMSet OptimizeSfM设置
     * @param generateModelFormats 生成模型格式列表
     * @param qualityReportLanguage 质量报告语言。枚举：zh/en/es/fr/de/ja/ko
     * @param qualityReportMeasureUnitSystem 度量单位制。枚举："metric"=公制, "imperial"=英制
     */
    public TerraBuildCreateReconstructionParams(
            List<Integer> reconstructionType,
            String name,
            List<String> folderUuids,
            List<Integer> folderIds,
            double simplifiedFactor,
            boolean simplifiedFactorSet,
            String wkt,
            int qualityLevel,
            boolean qualityLevelSet,
            int qualityLevel3dgs,
            boolean qualityLevel3dgsSet,
            boolean isShownOnMap,
            boolean isShownOnMapSet,
            boolean optimizeSfM,
            boolean optimizeSfMSet,
            List<Integer> generateModelFormats,
            String qualityReportLanguage,
            String qualityReportMeasureUnitSystem) {
        this.reconstructionType = reconstructionType != null ? reconstructionType : Collections.emptyList();
        this.name = name != null ? name : "";
        this.folderUuids = folderUuids != null ? folderUuids : Collections.emptyList();
        this.folderIds = folderIds != null ? folderIds : Collections.emptyList();
        this.simplifiedFactor = simplifiedFactor;
        this.simplifiedFactorSet = simplifiedFactorSet;
        this.wkt = wkt != null ? wkt : "";
        this.qualityLevel = qualityLevel;
        this.qualityLevelSet = qualityLevelSet;
        this.qualityLevel3dgs = qualityLevel3dgs;
        this.qualityLevel3dgsSet = qualityLevel3dgsSet;
        this.isShownOnMap = isShownOnMap;
        this.isShownOnMapSet = isShownOnMapSet;
        this.optimizeSfM = optimizeSfM;
        this.optimizeSfMSet = optimizeSfMSet;
        this.generateModelFormats = generateModelFormats != null ? generateModelFormats : Collections.emptyList();
        this.qualityReportLanguage = qualityReportLanguage != null ? qualityReportLanguage : "";
        this.qualityReportMeasureUnitSystem = qualityReportMeasureUnitSystem != null ? qualityReportMeasureUnitSystem : "";
    }

    /** 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达 */
    public List<Integer> getReconstructionType() { return reconstructionType; }
    /** 任务名称 */
    public String getName() { return name; }
    /** 媒体文件夹 UUID 列表（与 folderIds 二选一） */
    public List<String> getFolderUuids() { return folderUuids; }
    /** 媒体文件夹 ID 列表（与 folderUuids 二选一） */
    public List<Integer> getFolderIds() { return folderIds; }
    /** 模型简化系数 */
    public double getSimplifiedFactor() { return simplifiedFactor; }
    /** simplifiedFactor 是否已设置 */
    public boolean isSimplifiedFactorSet() { return simplifiedFactorSet; }
    /** 输出坐标系 WKT 字符串 */
    public String getWkt() { return wkt; }
    /** 重建精细程度 */
    public int getQualityLevel() { return qualityLevel; }
    /** qualityLevel 是否已设置 */
    public boolean isQualityLevelSet() { return qualityLevelSet; }
    /** 3DGS 质量等级 */
    public int getQualityLevel3dgs() { return qualityLevel3dgs; }
    /** qualityLevel3dgs 是否已设置 */
    public boolean isQualityLevel3dgsSet() { return qualityLevel3dgsSet; }
    /** 是否在地图上展示 */
    public boolean isShownOnMap() { return isShownOnMap; }
    /** isShownOnMap 是否已设置 */
    public boolean isShownOnMapSet() { return isShownOnMapSet; }
    /** 是否优化空三 */
    public boolean isOptimizeSfM() { return optimizeSfM; }
    /** optimizeSfM 是否已设置 */
    public boolean isOptimizeSfMSet() { return optimizeSfMSet; }
    /** 生成模型格式列表 */
    public List<Integer> getGenerateModelFormats() { return generateModelFormats; }
    /** 质量报告语言。枚举：zh/en/es/fr/de/ja/ko */
    public String getQualityReportLanguage() { return qualityReportLanguage; }
    /** 度量单位制。枚举："metric"=公制, "imperial"=英制 */
    public String getQualityReportMeasureUnitSystem() { return qualityReportMeasureUnitSystem; }

    /** 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达 */
    public void setReconstructionType(List<Integer> reconstructionType) {
        this.reconstructionType = reconstructionType;
    }

    /** 任务名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 媒体文件夹 UUID 列表（与 folderIds 二选一） */
    public void setFolderUuids(List<String> folderUuids) {
        this.folderUuids = folderUuids;
    }

    /** 媒体文件夹 ID 列表（与 folderUuids 二选一） */
    public void setFolderIds(List<Integer> folderIds) {
        this.folderIds = folderIds;
    }

    /** 模型简化系数 */
    public void setSimplifiedFactor(double simplifiedFactor) {
        this.simplifiedFactor = simplifiedFactor;
    }

    /** simplifiedFactor 是否已设置 */
    public void setSimplifiedFactorSet(boolean simplifiedFactorSet) {
        this.simplifiedFactorSet = simplifiedFactorSet;
    }

    /** 输出坐标系 WKT 字符串 */
    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    /** 重建精细程度 */
    public void setQualityLevel(int qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    /** qualityLevel 是否已设置 */
    public void setQualityLevelSet(boolean qualityLevelSet) {
        this.qualityLevelSet = qualityLevelSet;
    }

    /** 3DGS 质量等级 */
    public void setQualityLevel3dgs(int qualityLevel3dgs) {
        this.qualityLevel3dgs = qualityLevel3dgs;
    }

    /** qualityLevel3dgs 是否已设置 */
    public void setQualityLevel3dgsSet(boolean qualityLevel3dgsSet) {
        this.qualityLevel3dgsSet = qualityLevel3dgsSet;
    }

    /** 是否在地图上展示 */
    public void setShownOnMap(boolean shownOnMap) {
        this.isShownOnMap = shownOnMap;
    }

    /** isShownOnMap 是否已设置 */
    public void setShownOnMapSet(boolean shownOnMapSet) {
        this.isShownOnMapSet = shownOnMapSet;
    }

    /** 是否优化空三 */
    public void setOptimizeSfM(boolean optimizeSfM) {
        this.optimizeSfM = optimizeSfM;
    }

    /** optimizeSfM 是否已设置 */
    public void setOptimizeSfMSet(boolean optimizeSfMSet) {
        this.optimizeSfMSet = optimizeSfMSet;
    }

    /** 自定义存储父目录 ID */
    public int getCustomStorageParentId() { return customStorageParentId; }
    public void setCustomStorageParentId(int customStorageParentId) { this.customStorageParentId = customStorageParentId; }
    public boolean isCustomStorageParentIdSet() { return customStorageParentIdSet; }
    public void setCustomStorageParentIdSet(boolean customStorageParentIdSet) { this.customStorageParentIdSet = customStorageParentIdSet; }

    /** 生成模型格式列表 */
    public void setGenerateModelFormats(List<Integer> generateModelFormats) {
        this.generateModelFormats = generateModelFormats;
    }

    /** 质量报告语言。枚举：zh/en/es/fr/de/ja/ko */
    public void setQualityReportLanguage(String qualityReportLanguage) {
        this.qualityReportLanguage = qualityReportLanguage;
    }

    /** 度量单位制。枚举："metric"=公制, "imperial"=英制 */
    public void setQualityReportMeasureUnitSystem(String qualityReportMeasureUnitSystem) {
        this.qualityReportMeasureUnitSystem = qualityReportMeasureUnitSystem;
    }

    /** 兴趣区域（含 polygonPoints） */
    public TerraBuildPredefineArea getPredefineArea() { return predefineArea; }
    /** 兴趣区域（含 polygonPoints） */
    public void setPredefineArea(TerraBuildPredefineArea predefineArea) { this.predefineArea = predefineArea; }

    /** 2D 可见光重建子参数（设置后自动启用） */
    public TerraBuildParameter2D getParameter2d() { return parameter2d; }
    /** 2D 可见光重建子参数（设置后自动启用） */
    public void setParameter2d(TerraBuildParameter2D parameter2d) { this.parameter2d = parameter2d; }

    /** 3D 重建子参数（设置后自动启用） */
    public TerraBuildParameter3D getParameter3d() { return parameter3d; }
    /** 3D 重建子参数（设置后自动启用） */
    public void setParameter3d(TerraBuildParameter3D parameter3d) { this.parameter3d = parameter3d; }

    /** 3DGS 重建子参数（设置后自动启用） */
    public TerraBuildParameter3DGS getParameter3dgs() { return parameter3dgs; }
    /** 3DGS 重建子参数（设置后自动启用） */
    public void setParameter3dgs(TerraBuildParameter3DGS parameter3dgs) { this.parameter3dgs = parameter3dgs; }

    /** 激光雷达重建子参数（设置后自动启用） */
    public TerraBuildParameterLidar getParameterLidar() { return parameterLidar; }
    /** 激光雷达重建子参数（设置后自动启用） */
    public void setParameterLidar(TerraBuildParameterLidar parameterLidar) { this.parameterLidar = parameterLidar; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraBuildCreateReconstructionParams that = (TerraBuildCreateReconstructionParams) o;
        return Objects.equals(reconstructionType, that.reconstructionType)
                && Objects.equals(name, that.name)
                && Objects.equals(folderUuids, that.folderUuids)
                && Objects.equals(folderIds, that.folderIds)
                && Double.compare(that.simplifiedFactor, simplifiedFactor) == 0
                && simplifiedFactorSet == that.simplifiedFactorSet
                && Objects.equals(wkt, that.wkt)
                && qualityLevel == that.qualityLevel
                && qualityLevelSet == that.qualityLevelSet
                && qualityLevel3dgs == that.qualityLevel3dgs
                && qualityLevel3dgsSet == that.qualityLevel3dgsSet
                && isShownOnMap == that.isShownOnMap
                && isShownOnMapSet == that.isShownOnMapSet
                && optimizeSfM == that.optimizeSfM
                && optimizeSfMSet == that.optimizeSfMSet
                && customStorageParentId == that.customStorageParentId
                && customStorageParentIdSet == that.customStorageParentIdSet
                && Objects.equals(generateModelFormats, that.generateModelFormats)
                && Objects.equals(qualityReportLanguage, that.qualityReportLanguage)
                && Objects.equals(qualityReportMeasureUnitSystem, that.qualityReportMeasureUnitSystem)
                && Objects.equals(predefineArea, that.predefineArea)
                && Objects.equals(parameter2d, that.parameter2d)
                && Objects.equals(parameter3d, that.parameter3d)
                && Objects.equals(parameter3dgs, that.parameter3dgs)
                && Objects.equals(parameterLidar, that.parameterLidar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                reconstructionType,
                name,
                folderUuids,
                folderIds,
                simplifiedFactor,
                simplifiedFactorSet,
                wkt,
                qualityLevel,
                qualityLevelSet,
                qualityLevel3dgs,
                qualityLevel3dgsSet,
                isShownOnMap,
                isShownOnMapSet,
                optimizeSfM,
                optimizeSfMSet,
                customStorageParentId,
                customStorageParentIdSet,
                generateModelFormats,
                qualityReportLanguage,
                qualityReportMeasureUnitSystem,
                predefineArea,
                parameter2d,
                parameter3d,
                parameter3dgs,
                parameterLidar);
    }

    @Override
    public String toString() {
        return "TerraBuildCreateReconstructionParams{"
                + "reconstructionType=" + reconstructionType
                + ", name='" + name + '\''
                + ", folderUuids=" + folderUuids
                + ", folderIds=" + folderIds
                + ", simplifiedFactor=" + simplifiedFactor
                + ", simplifiedFactorSet=" + simplifiedFactorSet
                + ", wkt='" + wkt + '\''
                + ", qualityLevel=" + qualityLevel
                + ", qualityLevelSet=" + qualityLevelSet
                + ", qualityLevel3dgs=" + qualityLevel3dgs
                + ", qualityLevel3dgsSet=" + qualityLevel3dgsSet
                + ", isShownOnMap=" + isShownOnMap
                + ", isShownOnMapSet=" + isShownOnMapSet
                + ", optimizeSfM=" + optimizeSfM
                + ", optimizeSfMSet=" + optimizeSfMSet
                + ", customStorageParentId=" + customStorageParentId
                + ", customStorageParentIdSet=" + customStorageParentIdSet
                + ", generateModelFormats=" + generateModelFormats
                + ", qualityReportLanguage='" + qualityReportLanguage + '\''
                + ", qualityReportMeasureUnitSystem='" + qualityReportMeasureUnitSystem + '\''
                + ", predefineArea=" + predefineArea
                + ", parameter2d=" + parameter2d
                + ", parameter3d=" + parameter3d
                + ", parameter3dgs=" + parameter3dgs
                + ", parameterLidar=" + parameterLidar
                + '}';
    }
}
