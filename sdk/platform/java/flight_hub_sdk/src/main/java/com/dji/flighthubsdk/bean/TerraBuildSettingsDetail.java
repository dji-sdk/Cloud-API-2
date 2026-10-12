// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** Terra Build 重建设置详情 */
public final class TerraBuildSettingsDetail {
    private String name;
    private List<Integer> reconstructionType;
    private double simplifiedFactor;
    private int reconstructionMode;
    private TerraBuildPredefineArea predefineArea;
    private List<Integer> generateModelFormats;
    private String wkt;
    private int qualityLevel;
    private int qualityLevel3dgs;
    public TerraBuildSettingsDetail() {
    }

/**
     * 构造 TerraBuildSettingsDetail。
     *
     * @param name 任务名称
     * @param reconstructionType 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达
     * @param simplifiedFactor 模型简化系数
     * @param reconstructionMode 重建模式
     * @param predefineArea 兴趣区域多边形点列表
     * @param generateModelFormats 输出格式列表
     * @param wkt 输出坐标系 WKT 字符串
     * @param qualityLevel 建模质量等级
     * @param qualityLevel3dgs 3DGS 质量等级
     */
    public TerraBuildSettingsDetail(
            String name,
            List<Integer> reconstructionType,
            double simplifiedFactor,
            int reconstructionMode,
            TerraBuildPredefineArea predefineArea,
            List<Integer> generateModelFormats,
            String wkt,
            int qualityLevel,
            int qualityLevel3dgs) {
        this.name = name != null ? name : "";
        this.reconstructionType = reconstructionType != null ? reconstructionType : Collections.emptyList();
        this.simplifiedFactor = simplifiedFactor;
        this.reconstructionMode = reconstructionMode;
        this.predefineArea = predefineArea;
        this.generateModelFormats = generateModelFormats != null ? generateModelFormats : Collections.emptyList();
        this.wkt = wkt != null ? wkt : "";
        this.qualityLevel = qualityLevel;
        this.qualityLevel3dgs = qualityLevel3dgs;
    }

    /** 任务名称 */
    public String getName() { return name; }
    /** 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达 */
    public List<Integer> getReconstructionType() { return reconstructionType; }
    /** 模型简化系数 */
    public double getSimplifiedFactor() { return simplifiedFactor; }
    /** 重建模式 */
    public int getReconstructionMode() { return reconstructionMode; }
    /** 兴趣区域（含 polygonPoints） */
    public TerraBuildPredefineArea getPredefineArea() { return predefineArea; }
    /** 输出格式列表 */
    public List<Integer> getGenerateModelFormats() { return generateModelFormats; }
    /** 输出坐标系 WKT 字符串 */
    public String getWkt() { return wkt; }
    /** 建模质量等级 */
    public int getQualityLevel() { return qualityLevel; }
    /** 3DGS 质量等级 */
    public int getQualityLevel3dgs() { return qualityLevel3dgs; }

    /** 任务名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 重建类型。枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达 */
    public void setReconstructionType(List<Integer> reconstructionType) {
        this.reconstructionType = reconstructionType;
    }

    /** 模型简化系数 */
    public void setSimplifiedFactor(double simplifiedFactor) {
        this.simplifiedFactor = simplifiedFactor;
    }

    /** 重建模式 */
    public void setReconstructionMode(int reconstructionMode) {
        this.reconstructionMode = reconstructionMode;
    }

    /** 兴趣区域（含 polygonPoints） */
    public void setPredefineArea(TerraBuildPredefineArea predefineArea) {
        this.predefineArea = predefineArea;
    }

    /** 输出格式列表 */
    public void setGenerateModelFormats(List<Integer> generateModelFormats) {
        this.generateModelFormats = generateModelFormats;
    }

    /** 输出坐标系 WKT 字符串 */
    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    /** 建模质量等级 */
    public void setQualityLevel(int qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    /** 3DGS 质量等级 */
    public void setQualityLevel3dgs(int qualityLevel3dgs) {
        this.qualityLevel3dgs = qualityLevel3dgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraBuildSettingsDetail that = (TerraBuildSettingsDetail) o;
        return Objects.equals(name, that.name)
                && Objects.equals(reconstructionType, that.reconstructionType)
                && Double.compare(that.simplifiedFactor, simplifiedFactor) == 0
                && reconstructionMode == that.reconstructionMode
                && Objects.equals(predefineArea, that.predefineArea)
                && Objects.equals(generateModelFormats, that.generateModelFormats)
                && Objects.equals(wkt, that.wkt)
                && qualityLevel == that.qualityLevel
                && qualityLevel3dgs == that.qualityLevel3dgs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                reconstructionType,
                simplifiedFactor,
                reconstructionMode,
                predefineArea,
                generateModelFormats,
                wkt,
                qualityLevel,
                qualityLevel3dgs);
    }

    @Override
    public String toString() {
        return "TerraBuildSettingsDetail{"
                + "name='" + name + '\''
                + ", reconstructionType=" + reconstructionType
                + ", simplifiedFactor=" + simplifiedFactor
                + ", reconstructionMode=" + reconstructionMode
                + ", predefineArea=" + predefineArea
                + ", generateModelFormats=" + generateModelFormats
                + ", wkt='" + wkt + '\''
                + ", qualityLevel=" + qualityLevel
                + ", qualityLevel3dgs=" + qualityLevel3dgs
                + '}';
    }
}
