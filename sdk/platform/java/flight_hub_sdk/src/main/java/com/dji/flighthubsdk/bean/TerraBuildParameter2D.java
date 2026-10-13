// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 2D 可见光重建参数 */
public final class TerraBuildParameter2D {
    private String sfmMethod;
    private int sfmFeatureQuantityLevel;
    private boolean sfmFeatureQuantityLevelSet;
    private boolean useImagePosition;
    private boolean useImagePositionSet;
    private boolean generateBlocksExchangeXml;
    private boolean generateBlocksExchangeXmlSet;
    private TerraBuildGeoDesc blocksExchangeXmlGeoDesc;
    private TerraBuildGeoDesc outputGeoDesc;
    private double usableMemoryGb;
    private boolean usableMemoryGbSet;
    private int mapMode;
    private boolean mapModeSet;
    private int qualityLevel;
    private boolean qualityLevelSet;
    private int resolutionType2d;
    private boolean resolutionType2dSet;
    private int domTileSize;
    private boolean domTileSizeSet;
    private double resolutionGsd2d;
    private boolean resolutionGsd2dSet;
    private boolean useWallis;
    private boolean useWallisSet;
    private boolean useDehaze;
    private boolean useDehazeSet;
    private boolean useGcj02;
    private boolean useGcj02Set;
    private boolean generateMaps2dTiff;
    private boolean generateMaps2dTiffSet;
    private boolean generateMaps2dTiffCog;
    private boolean generateMaps2dTiffCogSet;
    private boolean useSmartFillHole;
    private boolean useSmartFillHoleSet;

    public TerraBuildParameter2D() {
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

    /** 限制内存用量 */
    public double getUsableMemoryGb() { return usableMemoryGb; }
    public void setUsableMemoryGb(double usableMemoryGb) { this.usableMemoryGb = usableMemoryGb; }
    public boolean isUsableMemoryGbSet() { return usableMemoryGbSet; }
    public void setUsableMemoryGbSet(boolean usableMemoryGbSet) { this.usableMemoryGbSet = usableMemoryGbSet; }

    /** 建图模式 */
    public int getMapMode() { return mapMode; }
    public void setMapMode(int mapMode) { this.mapMode = mapMode; }
    public boolean isMapModeSet() { return mapModeSet; }
    public void setMapModeSet(boolean mapModeSet) { this.mapModeSet = mapModeSet; }

    /** 重建精细程度，1=高, 2=中, 3=低 */
    public int getQualityLevel() { return qualityLevel; }
    public void setQualityLevel(int qualityLevel) { this.qualityLevel = qualityLevel; }
    public boolean isQualityLevelSet() { return qualityLevelSet; }
    public void setQualityLevelSet(boolean qualityLevelSet) { this.qualityLevelSet = qualityLevelSet; }

    /** 2D 分辨率类型 */
    public int getResolutionType2d() { return resolutionType2d; }
    public void setResolutionType2d(int resolutionType2d) { this.resolutionType2d = resolutionType2d; }
    public boolean isResolutionType2dSet() { return resolutionType2dSet; }
    public void setResolutionType2dSet(boolean resolutionType2dSet) { this.resolutionType2dSet = resolutionType2dSet; }

    /** DOM 瓦片大小 */
    public int getDomTileSize() { return domTileSize; }
    public void setDomTileSize(int domTileSize) { this.domTileSize = domTileSize; }
    public boolean isDomTileSizeSet() { return domTileSizeSet; }
    public void setDomTileSizeSet(boolean domTileSizeSet) { this.domTileSizeSet = domTileSizeSet; }

    /** 2D 分辨率 GSD */
    public double getResolutionGsd2d() { return resolutionGsd2d; }
    public void setResolutionGsd2d(double resolutionGsd2d) { this.resolutionGsd2d = resolutionGsd2d; }
    public boolean isResolutionGsd2dSet() { return resolutionGsd2dSet; }
    public void setResolutionGsd2dSet(boolean resolutionGsd2dSet) { this.resolutionGsd2dSet = resolutionGsd2dSet; }

    /** 是否使用 Wallis 滤波 */
    public boolean isUseWallis() { return useWallis; }
    public void setUseWallis(boolean useWallis) { this.useWallis = useWallis; }
    public boolean isUseWallisSet() { return useWallisSet; }
    public void setUseWallisSet(boolean useWallisSet) { this.useWallisSet = useWallisSet; }

    /** 是否使用去雾 */
    public boolean isUseDehaze() { return useDehaze; }
    public void setUseDehaze(boolean useDehaze) { this.useDehaze = useDehaze; }
    public boolean isUseDehazeSet() { return useDehazeSet; }
    public void setUseDehazeSet(boolean useDehazeSet) { this.useDehazeSet = useDehazeSet; }

    /** 是否使用 GCJ02 坐标系 */
    public boolean isUseGcj02() { return useGcj02; }
    public void setUseGcj02(boolean useGcj02) { this.useGcj02 = useGcj02; }
    public boolean isUseGcj02Set() { return useGcj02Set; }
    public void setUseGcj02Set(boolean useGcj02Set) { this.useGcj02Set = useGcj02Set; }

    /** 是否生成 2D TIFF */
    public boolean isGenerateMaps2dTiff() { return generateMaps2dTiff; }
    public void setGenerateMaps2dTiff(boolean generateMaps2dTiff) { this.generateMaps2dTiff = generateMaps2dTiff; }
    public boolean isGenerateMaps2dTiffSet() { return generateMaps2dTiffSet; }
    public void setGenerateMaps2dTiffSet(boolean generateMaps2dTiffSet) { this.generateMaps2dTiffSet = generateMaps2dTiffSet; }

    /** 是否生成 2D TIFF COG */
    public boolean isGenerateMaps2dTiffCog() { return generateMaps2dTiffCog; }
    public void setGenerateMaps2dTiffCog(boolean generateMaps2dTiffCog) { this.generateMaps2dTiffCog = generateMaps2dTiffCog; }
    public boolean isGenerateMaps2dTiffCogSet() { return generateMaps2dTiffCogSet; }
    public void setGenerateMaps2dTiffCogSet(boolean generateMaps2dTiffCogSet) { this.generateMaps2dTiffCogSet = generateMaps2dTiffCogSet; }

    /** 是否使用智能填充孔洞 */
    public boolean isUseSmartFillHole() { return useSmartFillHole; }
    public void setUseSmartFillHole(boolean useSmartFillHole) { this.useSmartFillHole = useSmartFillHole; }
    public boolean isUseSmartFillHoleSet() { return useSmartFillHoleSet; }
    public void setUseSmartFillHoleSet(boolean useSmartFillHoleSet) { this.useSmartFillHoleSet = useSmartFillHoleSet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildParameter2D that = (TerraBuildParameter2D) o;
        return sfmFeatureQuantityLevel == that.sfmFeatureQuantityLevel
                && sfmFeatureQuantityLevelSet == that.sfmFeatureQuantityLevelSet
                && useImagePosition == that.useImagePosition
                && useImagePositionSet == that.useImagePositionSet
                && generateBlocksExchangeXml == that.generateBlocksExchangeXml
                && generateBlocksExchangeXmlSet == that.generateBlocksExchangeXmlSet
                && Double.compare(that.usableMemoryGb, usableMemoryGb) == 0
                && usableMemoryGbSet == that.usableMemoryGbSet
                && mapMode == that.mapMode
                && mapModeSet == that.mapModeSet
                && qualityLevel == that.qualityLevel
                && qualityLevelSet == that.qualityLevelSet
                && resolutionType2d == that.resolutionType2d
                && resolutionType2dSet == that.resolutionType2dSet
                && domTileSize == that.domTileSize
                && domTileSizeSet == that.domTileSizeSet
                && Double.compare(that.resolutionGsd2d, resolutionGsd2d) == 0
                && resolutionGsd2dSet == that.resolutionGsd2dSet
                && useWallis == that.useWallis
                && useWallisSet == that.useWallisSet
                && useDehaze == that.useDehaze
                && useDehazeSet == that.useDehazeSet
                && useGcj02 == that.useGcj02
                && useGcj02Set == that.useGcj02Set
                && generateMaps2dTiff == that.generateMaps2dTiff
                && generateMaps2dTiffSet == that.generateMaps2dTiffSet
                && generateMaps2dTiffCog == that.generateMaps2dTiffCog
                && generateMaps2dTiffCogSet == that.generateMaps2dTiffCogSet
                && useSmartFillHole == that.useSmartFillHole
                && useSmartFillHoleSet == that.useSmartFillHoleSet
                && Objects.equals(sfmMethod, that.sfmMethod)
                && Objects.equals(blocksExchangeXmlGeoDesc, that.blocksExchangeXmlGeoDesc)
                && Objects.equals(outputGeoDesc, that.outputGeoDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sfmMethod, sfmFeatureQuantityLevel, sfmFeatureQuantityLevelSet,
                useImagePosition, useImagePositionSet, generateBlocksExchangeXml,
                generateBlocksExchangeXmlSet, blocksExchangeXmlGeoDesc, outputGeoDesc,
                usableMemoryGb, usableMemoryGbSet,
                mapMode, mapModeSet, qualityLevel, qualityLevelSet, resolutionType2d,
                resolutionType2dSet, domTileSize, domTileSizeSet, resolutionGsd2d,
                resolutionGsd2dSet, useWallis, useWallisSet, useDehaze, useDehazeSet,
                useGcj02, useGcj02Set, generateMaps2dTiff, generateMaps2dTiffSet,
                generateMaps2dTiffCog, generateMaps2dTiffCogSet,
                useSmartFillHole, useSmartFillHoleSet);
    }

    @Override
    public String toString() {
        return "TerraBuildParameter2D{"
                + "sfmMethod='" + sfmMethod + '\''
                + ", sfmFeatureQuantityLevel=" + sfmFeatureQuantityLevel
                + ", sfmFeatureQuantityLevelSet=" + sfmFeatureQuantityLevelSet
                + ", useImagePosition=" + useImagePosition
                + ", useImagePositionSet=" + useImagePositionSet
                + ", generateBlocksExchangeXml=" + generateBlocksExchangeXml
                + ", generateBlocksExchangeXmlSet=" + generateBlocksExchangeXmlSet
                + ", blocksExchangeXmlGeoDesc=" + blocksExchangeXmlGeoDesc
                + ", outputGeoDesc=" + outputGeoDesc
                + ", usableMemoryGb=" + usableMemoryGb
                + ", usableMemoryGbSet=" + usableMemoryGbSet
                + ", mapMode=" + mapMode
                + ", mapModeSet=" + mapModeSet
                + ", qualityLevel=" + qualityLevel
                + ", qualityLevelSet=" + qualityLevelSet
                + ", resolutionType2d=" + resolutionType2d
                + ", resolutionType2dSet=" + resolutionType2dSet
                + ", domTileSize=" + domTileSize
                + ", domTileSizeSet=" + domTileSizeSet
                + ", resolutionGsd2d=" + resolutionGsd2d
                + ", resolutionGsd2dSet=" + resolutionGsd2dSet
                + ", useWallis=" + useWallis
                + ", useWallisSet=" + useWallisSet
                + ", useDehaze=" + useDehaze
                + ", useDehazeSet=" + useDehazeSet
                + ", useGcj02=" + useGcj02
                + ", useGcj02Set=" + useGcj02Set
                + ", generateMaps2dTiff=" + generateMaps2dTiff
                + ", generateMaps2dTiffSet=" + generateMaps2dTiffSet
                + ", generateMaps2dTiffCog=" + generateMaps2dTiffCog
                + ", generateMaps2dTiffCogSet=" + generateMaps2dTiffCogSet
                + ", useSmartFillHole=" + useSmartFillHole
                + ", useSmartFillHoleSet=" + useSmartFillHoleSet
                + '}';
    }
}
