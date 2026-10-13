// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 3D 重建参数 */
public final class TerraBuildParameter3D {
    private String sfmMethod;
    private int sfmFeatureQuantityLevel;
    private boolean sfmFeatureQuantityLevelSet;
    private boolean useImagePosition;
    private boolean useImagePositionSet;
    private boolean generateBlocksExchangeXml;
    private boolean generateBlocksExchangeXmlSet;
    private TerraBuildGeoDesc blocksExchangeXmlGeoDesc;
    private TerraBuildGeoDesc outputGeoDesc;
    private boolean outputPointcloud;
    private boolean outputPointcloudSet;
    private int densityPercentLevel;
    private boolean densityPercentLevelSet;
    private boolean generatePnts;
    private boolean generatePntsSet;
    private boolean generateLas;
    private boolean generateLasSet;
    private boolean generateLaz;
    private boolean generateLazSet;
    private boolean generatePcd;
    private boolean generatePcdSet;
    private boolean generatePointS3mb;
    private boolean generatePointS3mbSet;
    private boolean generatePointPly;
    private boolean generatePointPlySet;
    private boolean mergePoint;
    private boolean mergePointSet;
    private boolean outputPlyWithNormals;
    private boolean outputPlyWithNormalsSet;
    private int plyWithNormalsLevel;
    private boolean plyWithNormalsLevelSet;
    private boolean groundExtract;
    private boolean groundExtractSet;
    private int sceneType;
    private boolean sceneTypeSet;
    private double maxSizeBuilding;
    private boolean maxSizeBuildingSet;
    private double iterDist;
    private boolean iterDistSet;
    private double iterAngle;
    private boolean iterAngleSet;
    private double usableMemoryGb;
    private boolean usableMemoryGbSet;
    private boolean stereoMode;
    private boolean stereoModeSet;
    private int globalMinDepth;
    private boolean globalMinDepthSet;
    private boolean outputMesh;
    private boolean outputMeshSet;
    private boolean generatePly;
    private boolean generatePlySet;
    private boolean generateObj;
    private boolean generateObjSet;
    private boolean generateFbx;
    private boolean generateFbxSet;
    private boolean generateB3dm;
    private boolean generateB3dmSet;
    private boolean generateOsgb;
    private boolean generateOsgbSet;
    private boolean generateS3mb;
    private boolean generateS3mbSet;
    private boolean generateI3s;
    private boolean generateI3sSet;
    private boolean waterRefine;
    private boolean waterRefineSet;
    private boolean waterRemove;
    private boolean waterRemoveSet;
    private double modelSimplify;
    private boolean modelSimplifySet;
    private boolean generateDem;
    private boolean generateDemSet;
    private double demResolution;
    private boolean demResolutionSet;
    private boolean generateContour;
    private boolean generateContourSet;
    private double contourInterval;
    private boolean contourIntervalSet;
    private double contourDatum;
    private boolean contourDatumSet;
    private double contourElevationAnnotationRadius;
    private boolean contourElevationAnnotationRadiusSet;
    private boolean useContourFilter;
    private boolean useContourFilterSet;
    private double contourFilterLength;
    private boolean contourFilterLengthSet;
    private boolean generateGridAndTin;
    private boolean generateGridAndTinSet;
    private double gridInterval;
    private boolean gridIntervalSet;
    private boolean cutAoi;
    private boolean cutAoiSet;
    private int qualityLevel;
    private boolean qualityLevelSet;

    public TerraBuildParameter3D() {
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

    /** 是否输出点云 */
    public boolean isOutputPointcloud() { return outputPointcloud; }
    public void setOutputPointcloud(boolean outputPointcloud) { this.outputPointcloud = outputPointcloud; }
    public boolean isOutputPointcloudSet() { return outputPointcloudSet; }
    public void setOutputPointcloudSet(boolean outputPointcloudSet) { this.outputPointcloudSet = outputPointcloudSet; }

    /** 密度百分比等级 */
    public int getDensityPercentLevel() { return densityPercentLevel; }
    public void setDensityPercentLevel(int densityPercentLevel) { this.densityPercentLevel = densityPercentLevel; }
    public boolean isDensityPercentLevelSet() { return densityPercentLevelSet; }
    public void setDensityPercentLevelSet(boolean densityPercentLevelSet) { this.densityPercentLevelSet = densityPercentLevelSet; }

    /** 是否生成 PNTS */
    public boolean isGeneratePnts() { return generatePnts; }
    public void setGeneratePnts(boolean generatePnts) { this.generatePnts = generatePnts; }
    public boolean isGeneratePntsSet() { return generatePntsSet; }
    public void setGeneratePntsSet(boolean generatePntsSet) { this.generatePntsSet = generatePntsSet; }

    /** 是否生成 LAS */
    public boolean isGenerateLas() { return generateLas; }
    public void setGenerateLas(boolean generateLas) { this.generateLas = generateLas; }
    public boolean isGenerateLasSet() { return generateLasSet; }
    public void setGenerateLasSet(boolean generateLasSet) { this.generateLasSet = generateLasSet; }

    /** 是否生成 LAZ */
    public boolean isGenerateLaz() { return generateLaz; }
    public void setGenerateLaz(boolean generateLaz) { this.generateLaz = generateLaz; }
    public boolean isGenerateLazSet() { return generateLazSet; }
    public void setGenerateLazSet(boolean generateLazSet) { this.generateLazSet = generateLazSet; }

    /** 是否生成 PCD */
    public boolean isGeneratePcd() { return generatePcd; }
    public void setGeneratePcd(boolean generatePcd) { this.generatePcd = generatePcd; }
    public boolean isGeneratePcdSet() { return generatePcdSet; }
    public void setGeneratePcdSet(boolean generatePcdSet) { this.generatePcdSet = generatePcdSet; }

    /** 是否生成 s3mb 格式点云模型 */
    public boolean isGeneratePointS3mb() { return generatePointS3mb; }
    public void setGeneratePointS3mb(boolean generatePointS3mb) { this.generatePointS3mb = generatePointS3mb; }
    public boolean isGeneratePointS3mbSet() { return generatePointS3mbSet; }
    public void setGeneratePointS3mbSet(boolean generatePointS3mbSet) { this.generatePointS3mbSet = generatePointS3mbSet; }

    /** 是否生成点云 PLY */
    public boolean isGeneratePointPly() { return generatePointPly; }
    public void setGeneratePointPly(boolean generatePointPly) { this.generatePointPly = generatePointPly; }
    public boolean isGeneratePointPlySet() { return generatePointPlySet; }
    public void setGeneratePointPlySet(boolean generatePointPlySet) { this.generatePointPlySet = generatePointPlySet; }

    /** 是否合并点云 */
    public boolean isMergePoint() { return mergePoint; }
    public void setMergePoint(boolean mergePoint) { this.mergePoint = mergePoint; }
    public boolean isMergePointSet() { return mergePointSet; }
    public void setMergePointSet(boolean mergePointSet) { this.mergePointSet = mergePointSet; }

    /** 是否输出带法线的 PLY */
    public boolean isOutputPlyWithNormals() { return outputPlyWithNormals; }
    public void setOutputPlyWithNormals(boolean outputPlyWithNormals) { this.outputPlyWithNormals = outputPlyWithNormals; }
    public boolean isOutputPlyWithNormalsSet() { return outputPlyWithNormalsSet; }
    public void setOutputPlyWithNormalsSet(boolean outputPlyWithNormalsSet) { this.outputPlyWithNormalsSet = outputPlyWithNormalsSet; }

    /** 带法线 PLY 等级 */
    public int getPlyWithNormalsLevel() { return plyWithNormalsLevel; }
    public void setPlyWithNormalsLevel(int plyWithNormalsLevel) { this.plyWithNormalsLevel = plyWithNormalsLevel; }
    public boolean isPlyWithNormalsLevelSet() { return plyWithNormalsLevelSet; }
    public void setPlyWithNormalsLevelSet(boolean plyWithNormalsLevelSet) { this.plyWithNormalsLevelSet = plyWithNormalsLevelSet; }

    /** 是否提取地面 */
    public boolean isGroundExtract() { return groundExtract; }
    public void setGroundExtract(boolean groundExtract) { this.groundExtract = groundExtract; }
    public boolean isGroundExtractSet() { return groundExtractSet; }
    public void setGroundExtractSet(boolean groundExtractSet) { this.groundExtractSet = groundExtractSet; }

    /** 地面场景类型 0=平地 1=缓坡 2=陡坡 */
    public int getSceneType() { return sceneType; }
    public void setSceneType(int sceneType) { this.sceneType = sceneType; }
    public boolean isSceneTypeSet() { return sceneTypeSet; }
    public void setSceneTypeSet(boolean sceneTypeSet) { this.sceneTypeSet = sceneTypeSet; }

    /** 最大建筑物对角线长度 */
    public double getMaxSizeBuilding() { return maxSizeBuilding; }
    public void setMaxSizeBuilding(double maxSizeBuilding) { this.maxSizeBuilding = maxSizeBuilding; }
    public boolean isMaxSizeBuildingSet() { return maxSizeBuildingSet; }
    public void setMaxSizeBuildingSet(boolean maxSizeBuildingSet) { this.maxSizeBuildingSet = maxSizeBuildingSet; }

    /** 迭代距离 */
    public double getIterDist() { return iterDist; }
    public void setIterDist(double iterDist) { this.iterDist = iterDist; }
    public boolean isIterDistSet() { return iterDistSet; }
    public void setIterDistSet(boolean iterDistSet) { this.iterDistSet = iterDistSet; }

    /** 迭代角度 */
    public double getIterAngle() { return iterAngle; }
    public void setIterAngle(double iterAngle) { this.iterAngle = iterAngle; }
    public boolean isIterAngleSet() { return iterAngleSet; }
    public void setIterAngleSet(boolean iterAngleSet) { this.iterAngleSet = iterAngleSet; }

    /** 限制内存用量 */
    public double getUsableMemoryGb() { return usableMemoryGb; }
    public void setUsableMemoryGb(double usableMemoryGb) { this.usableMemoryGb = usableMemoryGb; }
    public boolean isUsableMemoryGbSet() { return usableMemoryGbSet; }
    public void setUsableMemoryGbSet(boolean usableMemoryGbSet) { this.usableMemoryGbSet = usableMemoryGbSet; }

    /** 是否立体模式 */
    public boolean isStereoMode() { return stereoMode; }
    public void setStereoMode(boolean stereoMode) { this.stereoMode = stereoMode; }
    public boolean isStereoModeSet() { return stereoModeSet; }
    public void setStereoModeSet(boolean stereoModeSet) { this.stereoModeSet = stereoModeSet; }

    /** 全局最低高度 */
    public int getGlobalMinDepth() { return globalMinDepth; }
    public void setGlobalMinDepth(int globalMinDepth) { this.globalMinDepth = globalMinDepth; }
    public boolean isGlobalMinDepthSet() { return globalMinDepthSet; }
    public void setGlobalMinDepthSet(boolean globalMinDepthSet) { this.globalMinDepthSet = globalMinDepthSet; }

    /** 是否输出网格 */
    public boolean isOutputMesh() { return outputMesh; }
    public void setOutputMesh(boolean outputMesh) { this.outputMesh = outputMesh; }
    public boolean isOutputMeshSet() { return outputMeshSet; }
    public void setOutputMeshSet(boolean outputMeshSet) { this.outputMeshSet = outputMeshSet; }

    /** 是否生成 PLY */
    public boolean isGeneratePly() { return generatePly; }
    public void setGeneratePly(boolean generatePly) { this.generatePly = generatePly; }
    public boolean isGeneratePlySet() { return generatePlySet; }
    public void setGeneratePlySet(boolean generatePlySet) { this.generatePlySet = generatePlySet; }

    /** 是否生成 OBJ */
    public boolean isGenerateObj() { return generateObj; }
    public void setGenerateObj(boolean generateObj) { this.generateObj = generateObj; }
    public boolean isGenerateObjSet() { return generateObjSet; }
    public void setGenerateObjSet(boolean generateObjSet) { this.generateObjSet = generateObjSet; }

    /** 是否生成 FBX */
    public boolean isGenerateFbx() { return generateFbx; }
    public void setGenerateFbx(boolean generateFbx) { this.generateFbx = generateFbx; }
    public boolean isGenerateFbxSet() { return generateFbxSet; }
    public void setGenerateFbxSet(boolean generateFbxSet) { this.generateFbxSet = generateFbxSet; }

    /** 是否生成 B3DM */
    public boolean isGenerateB3dm() { return generateB3dm; }
    public void setGenerateB3dm(boolean generateB3dm) { this.generateB3dm = generateB3dm; }
    public boolean isGenerateB3dmSet() { return generateB3dmSet; }
    public void setGenerateB3dmSet(boolean generateB3dmSet) { this.generateB3dmSet = generateB3dmSet; }

    /** 是否生成 OSGB */
    public boolean isGenerateOsgb() { return generateOsgb; }
    public void setGenerateOsgb(boolean generateOsgb) { this.generateOsgb = generateOsgb; }
    public boolean isGenerateOsgbSet() { return generateOsgbSet; }
    public void setGenerateOsgbSet(boolean generateOsgbSet) { this.generateOsgbSet = generateOsgbSet; }

    /** 是否生成 S3MB */
    public boolean isGenerateS3mb() { return generateS3mb; }
    public void setGenerateS3mb(boolean generateS3mb) { this.generateS3mb = generateS3mb; }
    public boolean isGenerateS3mbSet() { return generateS3mbSet; }
    public void setGenerateS3mbSet(boolean generateS3mbSet) { this.generateS3mbSet = generateS3mbSet; }

    /** 是否生成 I3S */
    public boolean isGenerateI3s() { return generateI3s; }
    public void setGenerateI3s(boolean generateI3s) { this.generateI3s = generateI3s; }
    public boolean isGenerateI3sSet() { return generateI3sSet; }
    public void setGenerateI3sSet(boolean generateI3sSet) { this.generateI3sSet = generateI3sSet; }

    /** 是否水面优化 */
    public boolean isWaterRefine() { return waterRefine; }
    public void setWaterRefine(boolean waterRefine) { this.waterRefine = waterRefine; }
    public boolean isWaterRefineSet() { return waterRefineSet; }
    public void setWaterRefineSet(boolean waterRefineSet) { this.waterRefineSet = waterRefineSet; }

    /** 是否移除水面 */
    public boolean isWaterRemove() { return waterRemove; }
    public void setWaterRemove(boolean waterRemove) { this.waterRemove = waterRemove; }
    public boolean isWaterRemoveSet() { return waterRemoveSet; }
    public void setWaterRemoveSet(boolean waterRemoveSet) { this.waterRemoveSet = waterRemoveSet; }

    /** 模型简化系数 */
    public double getModelSimplify() { return modelSimplify; }
    public void setModelSimplify(double modelSimplify) { this.modelSimplify = modelSimplify; }
    public boolean isModelSimplifySet() { return modelSimplifySet; }
    public void setModelSimplifySet(boolean modelSimplifySet) { this.modelSimplifySet = modelSimplifySet; }

    /** 是否生成 DEM */
    public boolean isGenerateDem() { return generateDem; }
    public void setGenerateDem(boolean generateDem) { this.generateDem = generateDem; }
    public boolean isGenerateDemSet() { return generateDemSet; }
    public void setGenerateDemSet(boolean generateDemSet) { this.generateDemSet = generateDemSet; }

    /** DEM 分辨率 */
    public double getDemResolution() { return demResolution; }
    public void setDemResolution(double demResolution) { this.demResolution = demResolution; }
    public boolean isDemResolutionSet() { return demResolutionSet; }
    public void setDemResolutionSet(boolean demResolutionSet) { this.demResolutionSet = demResolutionSet; }

    /** 是否生成等高线 */
    public boolean isGenerateContour() { return generateContour; }
    public void setGenerateContour(boolean generateContour) { this.generateContour = generateContour; }
    public boolean isGenerateContourSet() { return generateContourSet; }
    public void setGenerateContourSet(boolean generateContourSet) { this.generateContourSet = generateContourSet; }

    /** 等高线间隔 */
    public double getContourInterval() { return contourInterval; }
    public void setContourInterval(double contourInterval) { this.contourInterval = contourInterval; }
    public boolean isContourIntervalSet() { return contourIntervalSet; }
    public void setContourIntervalSet(boolean contourIntervalSet) { this.contourIntervalSet = contourIntervalSet; }

    /** 等高线基准面 */
    public double getContourDatum() { return contourDatum; }
    public void setContourDatum(double contourDatum) { this.contourDatum = contourDatum; }
    public boolean isContourDatumSet() { return contourDatumSet; }
    public void setContourDatumSet(boolean contourDatumSet) { this.contourDatumSet = contourDatumSet; }

    /** 等高线高程注记半径 */
    public double getContourElevationAnnotationRadius() { return contourElevationAnnotationRadius; }
    public void setContourElevationAnnotationRadius(double contourElevationAnnotationRadius) { this.contourElevationAnnotationRadius = contourElevationAnnotationRadius; }
    public boolean isContourElevationAnnotationRadiusSet() { return contourElevationAnnotationRadiusSet; }
    public void setContourElevationAnnotationRadiusSet(boolean contourElevationAnnotationRadiusSet) { this.contourElevationAnnotationRadiusSet = contourElevationAnnotationRadiusSet; }

    /** 是否使用等高线过滤 */
    public boolean isUseContourFilter() { return useContourFilter; }
    public void setUseContourFilter(boolean useContourFilter) { this.useContourFilter = useContourFilter; }
    public boolean isUseContourFilterSet() { return useContourFilterSet; }
    public void setUseContourFilterSet(boolean useContourFilterSet) { this.useContourFilterSet = useContourFilterSet; }

    /** 等高线过滤长度 */
    public double getContourFilterLength() { return contourFilterLength; }
    public void setContourFilterLength(double contourFilterLength) { this.contourFilterLength = contourFilterLength; }
    public boolean isContourFilterLengthSet() { return contourFilterLengthSet; }
    public void setContourFilterLengthSet(boolean contourFilterLengthSet) { this.contourFilterLengthSet = contourFilterLengthSet; }

    /** 是否生成格网和 TIN */
    public boolean isGenerateGridAndTin() { return generateGridAndTin; }
    public void setGenerateGridAndTin(boolean generateGridAndTin) { this.generateGridAndTin = generateGridAndTin; }
    public boolean isGenerateGridAndTinSet() { return generateGridAndTinSet; }
    public void setGenerateGridAndTinSet(boolean generateGridAndTinSet) { this.generateGridAndTinSet = generateGridAndTinSet; }

    /** 格网间隔 */
    public double getGridInterval() { return gridInterval; }
    public void setGridInterval(double gridInterval) { this.gridInterval = gridInterval; }
    public boolean isGridIntervalSet() { return gridIntervalSet; }
    public void setGridIntervalSet(boolean gridIntervalSet) { this.gridIntervalSet = gridIntervalSet; }

    /** 是否裁切 AOI */
    public boolean isCutAoi() { return cutAoi; }
    public void setCutAoi(boolean cutAoi) { this.cutAoi = cutAoi; }
    public boolean isCutAoiSet() { return cutAoiSet; }
    public void setCutAoiSet(boolean cutAoiSet) { this.cutAoiSet = cutAoiSet; }

    /** 重建精细程度 */
    public int getQualityLevel() { return qualityLevel; }
    public void setQualityLevel(int qualityLevel) { this.qualityLevel = qualityLevel; }
    public boolean isQualityLevelSet() { return qualityLevelSet; }
    public void setQualityLevelSet(boolean qualityLevelSet) { this.qualityLevelSet = qualityLevelSet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildParameter3D that = (TerraBuildParameter3D) o;
        return sfmFeatureQuantityLevel == that.sfmFeatureQuantityLevel
                && sfmFeatureQuantityLevelSet == that.sfmFeatureQuantityLevelSet
                && useImagePosition == that.useImagePosition
                && useImagePositionSet == that.useImagePositionSet
                && generateBlocksExchangeXml == that.generateBlocksExchangeXml
                && generateBlocksExchangeXmlSet == that.generateBlocksExchangeXmlSet
                && outputPointcloud == that.outputPointcloud
                && outputPointcloudSet == that.outputPointcloudSet
                && densityPercentLevel == that.densityPercentLevel
                && densityPercentLevelSet == that.densityPercentLevelSet
                && generatePnts == that.generatePnts && generatePntsSet == that.generatePntsSet
                && generateLas == that.generateLas && generateLasSet == that.generateLasSet
                && generateLaz == that.generateLaz && generateLazSet == that.generateLazSet
                && generatePcd == that.generatePcd && generatePcdSet == that.generatePcdSet
                && generatePointS3mb == that.generatePointS3mb && generatePointS3mbSet == that.generatePointS3mbSet
                && generatePointPly == that.generatePointPly && generatePointPlySet == that.generatePointPlySet
                && mergePoint == that.mergePoint && mergePointSet == that.mergePointSet
                && outputPlyWithNormals == that.outputPlyWithNormals && outputPlyWithNormalsSet == that.outputPlyWithNormalsSet
                && plyWithNormalsLevel == that.plyWithNormalsLevel && plyWithNormalsLevelSet == that.plyWithNormalsLevelSet
                && groundExtract == that.groundExtract && groundExtractSet == that.groundExtractSet
                && sceneType == that.sceneType && sceneTypeSet == that.sceneTypeSet
                && Double.compare(that.maxSizeBuilding, maxSizeBuilding) == 0 && maxSizeBuildingSet == that.maxSizeBuildingSet
                && Double.compare(that.iterDist, iterDist) == 0 && iterDistSet == that.iterDistSet
                && Double.compare(that.iterAngle, iterAngle) == 0 && iterAngleSet == that.iterAngleSet
                && Double.compare(that.usableMemoryGb, usableMemoryGb) == 0 && usableMemoryGbSet == that.usableMemoryGbSet
                && stereoMode == that.stereoMode && stereoModeSet == that.stereoModeSet
                && globalMinDepth == that.globalMinDepth && globalMinDepthSet == that.globalMinDepthSet
                && outputMesh == that.outputMesh && outputMeshSet == that.outputMeshSet
                && generatePly == that.generatePly && generatePlySet == that.generatePlySet
                && generateObj == that.generateObj && generateObjSet == that.generateObjSet
                && generateFbx == that.generateFbx && generateFbxSet == that.generateFbxSet
                && generateB3dm == that.generateB3dm && generateB3dmSet == that.generateB3dmSet
                && generateOsgb == that.generateOsgb && generateOsgbSet == that.generateOsgbSet
                && generateS3mb == that.generateS3mb && generateS3mbSet == that.generateS3mbSet
                && generateI3s == that.generateI3s && generateI3sSet == that.generateI3sSet
                && waterRefine == that.waterRefine && waterRefineSet == that.waterRefineSet
                && waterRemove == that.waterRemove && waterRemoveSet == that.waterRemoveSet
                && Double.compare(that.modelSimplify, modelSimplify) == 0 && modelSimplifySet == that.modelSimplifySet
                && generateDem == that.generateDem && generateDemSet == that.generateDemSet
                && Double.compare(that.demResolution, demResolution) == 0 && demResolutionSet == that.demResolutionSet
                && generateContour == that.generateContour && generateContourSet == that.generateContourSet
                && Double.compare(that.contourInterval, contourInterval) == 0 && contourIntervalSet == that.contourIntervalSet
                && Double.compare(that.contourDatum, contourDatum) == 0 && contourDatumSet == that.contourDatumSet
                && Double.compare(that.contourElevationAnnotationRadius, contourElevationAnnotationRadius) == 0
                && contourElevationAnnotationRadiusSet == that.contourElevationAnnotationRadiusSet
                && useContourFilter == that.useContourFilter && useContourFilterSet == that.useContourFilterSet
                && Double.compare(that.contourFilterLength, contourFilterLength) == 0 && contourFilterLengthSet == that.contourFilterLengthSet
                && generateGridAndTin == that.generateGridAndTin && generateGridAndTinSet == that.generateGridAndTinSet
                && Double.compare(that.gridInterval, gridInterval) == 0 && gridIntervalSet == that.gridIntervalSet
                && cutAoi == that.cutAoi && cutAoiSet == that.cutAoiSet
                && qualityLevel == that.qualityLevel && qualityLevelSet == that.qualityLevelSet
                && Objects.equals(sfmMethod, that.sfmMethod)
                && Objects.equals(blocksExchangeXmlGeoDesc, that.blocksExchangeXmlGeoDesc)
                && Objects.equals(outputGeoDesc, that.outputGeoDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sfmMethod, sfmFeatureQuantityLevel, useImagePosition,
                generateBlocksExchangeXml, blocksExchangeXmlGeoDesc, outputGeoDesc,
                outputPointcloud, densityPercentLevel, generatePnts, generateLas,
                generateLaz, generatePcd, generatePointS3mb, generatePointPly, mergePoint,
                outputPlyWithNormals, plyWithNormalsLevel, groundExtract,
                sceneType, maxSizeBuilding, iterDist, iterAngle, usableMemoryGb,
                stereoMode, globalMinDepth, outputMesh,
                generatePly, generateObj, generateFbx, generateB3dm, generateOsgb,
                generateS3mb, generateI3s, waterRefine, waterRemove, modelSimplify,
                generateDem, demResolution, generateContour, contourInterval, contourDatum,
                contourElevationAnnotationRadius, useContourFilter, contourFilterLength,
                generateGridAndTin, gridInterval, cutAoi, qualityLevel);
    }

    @Override
    public String toString() {
        return "TerraBuildParameter3D{"
                + "sfmMethod='" + sfmMethod + '\''
                + ", qualityLevel=" + qualityLevel
                + ", outputMesh=" + outputMesh
                + ", generateB3dm=" + generateB3dm
                + ", generateOsgb=" + generateOsgb
                + ", generatePointS3mb=" + generatePointS3mb
                + ", sceneType=" + sceneType
                + ", maxSizeBuilding=" + maxSizeBuilding
                + ", iterDist=" + iterDist
                + ", iterAngle=" + iterAngle
                + ", usableMemoryGb=" + usableMemoryGb
                + ", stereoMode=" + stereoMode
                + ", globalMinDepth=" + globalMinDepth
                + ", outputGeoDesc=" + outputGeoDesc
                + '}';
    }
}
