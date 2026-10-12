// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 3DGS 重建参数 */
public final class TerraBuildParameter3DGS {
    private String sfmMethod;
    private int sfmFeatureQuantityLevel;
    private boolean sfmFeatureQuantityLevelSet;
    private boolean useImagePosition;
    private boolean useImagePositionSet;
    private boolean generateBlocksExchangeXml;
    private boolean generateBlocksExchangeXmlSet;
    private TerraBuildGeoDesc blocksExchangeXmlGeoDesc;
    private TerraBuildGeoDesc outputGeoDesc;
    private String globalDataType;
    private int qualityLevel3dgs;
    private boolean qualityLevel3dgsSet;
    private boolean generateTerrags;
    private boolean generateTerragsSet;
    private boolean generate3dtiles;
    private boolean generate3dtilesSet;
    private boolean generate3dgsDom;
    private boolean generate3dgsDomSet;

    public TerraBuildParameter3DGS() {
    }

    /** 空三方法，如 "GLOBAL" */
    public String getSfmMethod() { return sfmMethod; }
    public void setSfmMethod(String sfmMethod) { this.sfmMethod = sfmMethod; }

    /** 空三特征提取等级 */
    public int getSfmFeatureQuantityLevel() { return sfmFeatureQuantityLevel; }
    public void setSfmFeatureQuantityLevel(int sfmFeatureQuantityLevel) { this.sfmFeatureQuantityLevel = sfmFeatureQuantityLevel; }
    public boolean isSfmFeatureQuantityLevelSet() { return sfmFeatureQuantityLevelSet; }
    public void setSfmFeatureQuantityLevelSet(boolean sfmFeatureQuantityLevelSet) { this.sfmFeatureQuantityLevelSet = sfmFeatureQuantityLevelSet; }

    /** 是否使用影像位置 */
    public boolean isUseImagePosition() { return useImagePosition; }
    public void setUseImagePosition(boolean useImagePosition) { this.useImagePosition = useImagePosition; }
    public boolean isUseImagePositionSet() { return useImagePositionSet; }
    public void setUseImagePositionSet(boolean useImagePositionSet) { this.useImagePositionSet = useImagePositionSet; }

    /** 是否生成 blocks exchange XML */
    public boolean isGenerateBlocksExchangeXml() { return generateBlocksExchangeXml; }
    public void setGenerateBlocksExchangeXml(boolean generateBlocksExchangeXml) { this.generateBlocksExchangeXml = generateBlocksExchangeXml; }
    public boolean isGenerateBlocksExchangeXmlSet() { return generateBlocksExchangeXmlSet; }
    public void setGenerateBlocksExchangeXmlSet(boolean generateBlocksExchangeXmlSet) { this.generateBlocksExchangeXmlSet = generateBlocksExchangeXmlSet; }

    /** Blocks Exchange XML 坐标描述 */
    public TerraBuildGeoDesc getBlocksExchangeXmlGeoDesc() { return blocksExchangeXmlGeoDesc; }
    public void setBlocksExchangeXmlGeoDesc(TerraBuildGeoDesc blocksExchangeXmlGeoDesc) { this.blocksExchangeXmlGeoDesc = blocksExchangeXmlGeoDesc; }

    /** 输出坐标描述 */
    public TerraBuildGeoDesc getOutputGeoDesc() { return outputGeoDesc; }
    public void setOutputGeoDesc(TerraBuildGeoDesc outputGeoDesc) { this.outputGeoDesc = outputGeoDesc; }

    /** 全局数据类型 */
    public String getGlobalDataType() { return globalDataType; }
    public void setGlobalDataType(String globalDataType) { this.globalDataType = globalDataType; }

    /** 3DGS 质量等级 */
    public int getQualityLevel3dgs() { return qualityLevel3dgs; }
    public void setQualityLevel3dgs(int qualityLevel3dgs) { this.qualityLevel3dgs = qualityLevel3dgs; }
    public boolean isQualityLevel3dgsSet() { return qualityLevel3dgsSet; }
    public void setQualityLevel3dgsSet(boolean qualityLevel3dgsSet) { this.qualityLevel3dgsSet = qualityLevel3dgsSet; }

    /** 是否生成 TerraGS */
    public boolean isGenerateTerrags() { return generateTerrags; }
    public void setGenerateTerrags(boolean generateTerrags) { this.generateTerrags = generateTerrags; }
    public boolean isGenerateTerragsSet() { return generateTerragsSet; }
    public void setGenerateTerragsSet(boolean generateTerragsSet) { this.generateTerragsSet = generateTerragsSet; }

    /** 是否生成 3DTiles */
    public boolean isGenerate3dtiles() { return generate3dtiles; }
    public void setGenerate3dtiles(boolean generate3dtiles) { this.generate3dtiles = generate3dtiles; }
    public boolean isGenerate3dtilesSet() { return generate3dtilesSet; }
    public void setGenerate3dtilesSet(boolean generate3dtilesSet) { this.generate3dtilesSet = generate3dtilesSet; }

    /** 是否生成 3DGS DOM */
    public boolean isGenerate3dgsDom() { return generate3dgsDom; }
    public void setGenerate3dgsDom(boolean generate3dgsDom) { this.generate3dgsDom = generate3dgsDom; }
    public boolean isGenerate3dgsDomSet() { return generate3dgsDomSet; }
    public void setGenerate3dgsDomSet(boolean generate3dgsDomSet) { this.generate3dgsDomSet = generate3dgsDomSet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildParameter3DGS that = (TerraBuildParameter3DGS) o;
        return sfmFeatureQuantityLevel == that.sfmFeatureQuantityLevel
                && sfmFeatureQuantityLevelSet == that.sfmFeatureQuantityLevelSet
                && useImagePosition == that.useImagePosition
                && useImagePositionSet == that.useImagePositionSet
                && generateBlocksExchangeXml == that.generateBlocksExchangeXml
                && generateBlocksExchangeXmlSet == that.generateBlocksExchangeXmlSet
                && qualityLevel3dgs == that.qualityLevel3dgs
                && qualityLevel3dgsSet == that.qualityLevel3dgsSet
                && generateTerrags == that.generateTerrags
                && generateTerragsSet == that.generateTerragsSet
                && generate3dtiles == that.generate3dtiles
                && generate3dtilesSet == that.generate3dtilesSet
                && generate3dgsDom == that.generate3dgsDom
                && generate3dgsDomSet == that.generate3dgsDomSet
                && Objects.equals(sfmMethod, that.sfmMethod)
                && Objects.equals(blocksExchangeXmlGeoDesc, that.blocksExchangeXmlGeoDesc)
                && Objects.equals(outputGeoDesc, that.outputGeoDesc)
                && Objects.equals(globalDataType, that.globalDataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sfmMethod, sfmFeatureQuantityLevel, sfmFeatureQuantityLevelSet,
                useImagePosition, useImagePositionSet, generateBlocksExchangeXml,
                generateBlocksExchangeXmlSet, blocksExchangeXmlGeoDesc, outputGeoDesc,
                globalDataType, qualityLevel3dgs, qualityLevel3dgsSet,
                generateTerrags, generateTerragsSet, generate3dtiles, generate3dtilesSet,
                generate3dgsDom, generate3dgsDomSet);
    }

    @Override
    public String toString() {
        return "TerraBuildParameter3DGS{"
                + "sfmMethod='" + sfmMethod + '\''
                + ", qualityLevel3dgs=" + qualityLevel3dgs
                + ", globalDataType='" + globalDataType + '\''
                + ", generateTerrags=" + generateTerrags
                + ", generate3dtiles=" + generate3dtiles
                + ", generate3dgsDom=" + generate3dgsDom
                + ", outputGeoDesc=" + outputGeoDesc
                + '}';
    }
}
