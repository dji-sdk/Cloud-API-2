// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;

/** 激光雷达重建参数 */
public final class TerraBuildParameterLidar {
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
    private boolean groudExtract;
    private boolean groudExtractSet;
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
    private int qualityLevel;
    private boolean qualityLevelSet;
    private double sampleDistance;
    private boolean sampleDistanceSet;
    private double outputHeightOffset;
    private boolean outputHeightOffsetSet;
    private int refineCalibrate;
    private boolean refineCalibrateSet;
    private boolean lppOptimise;
    private boolean lppOptimiseSet;
    private boolean smoothPoint;
    private boolean smoothPointSet;
    private boolean colorizePoint;
    private boolean colorizePointSet;
    private boolean canProcessL1;
    private boolean canProcessL1Set;
    private TerraBuildGeoDesc outputGeoDesc;
    private boolean outputMesh;
    private boolean outputMeshSet;
    private int qualityLevelMesh;
    private boolean qualityLevelMeshSet;
    private double modelSimplify;
    private boolean modelSimplifySet;
    private boolean generateObj;
    private boolean generateObjSet;
    private boolean generateFbx;
    private boolean generateFbxSet;
    private boolean generatePly;
    private boolean generatePlySet;
    private boolean generateOsgb;
    private boolean generateOsgbSet;
    private boolean generateB3dm;
    private boolean generateB3dmSet;
    private boolean generateI3s;
    private boolean generateI3sSet;
    private boolean generateMaps2d;
    private boolean generateMaps2dSet;
    private boolean generateMaps2dTiff;
    private boolean generateMaps2dTiffSet;
    private boolean generateMaps2dTiffCog;
    private boolean generateMaps2dTiffCogSet;
    private int resolutionType2d;
    private boolean resolutionType2dSet;
    private int resolutionLevel2d;
    private boolean resolutionLevel2dSet;
    private double resolutionGsd2d;
    private boolean resolutionGsd2dSet;
    private int domTileSize;
    private boolean domTileSizeSet;
    private boolean useGcj02;
    private boolean useGcj02Set;
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
    private boolean generate3dgs;
    private boolean generate3dgsSet;
    private int qualityLevel3dgs;
    private boolean qualityLevel3dgsSet;
    private boolean generate3dtiles;
    private boolean generate3dtilesSet;

    public TerraBuildParameterLidar() {
    }

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
    public boolean isGroudExtract() { return groudExtract; }
    public void setGroudExtract(boolean groudExtract) { this.groudExtract = groudExtract; }
    public boolean isGroudExtractSet() { return groudExtractSet; }
    public void setGroudExtractSet(boolean groudExtractSet) { this.groudExtractSet = groudExtractSet; }

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

    /** 重建精细程度 */
    public int getQualityLevel() { return qualityLevel; }
    public void setQualityLevel(int qualityLevel) { this.qualityLevel = qualityLevel; }
    public boolean isQualityLevelSet() { return qualityLevelSet; }
    public void setQualityLevelSet(boolean qualityLevelSet) { this.qualityLevelSet = qualityLevelSet; }

    /** 采样距离 */
    public double getSampleDistance() { return sampleDistance; }
    public void setSampleDistance(double sampleDistance) { this.sampleDistance = sampleDistance; }
    public boolean isSampleDistanceSet() { return sampleDistanceSet; }
    public void setSampleDistanceSet(boolean sampleDistanceSet) { this.sampleDistanceSet = sampleDistanceSet; }

    /** 输出高度偏移 */
    public double getOutputHeightOffset() { return outputHeightOffset; }
    public void setOutputHeightOffset(double outputHeightOffset) { this.outputHeightOffset = outputHeightOffset; }
    public boolean isOutputHeightOffsetSet() { return outputHeightOffsetSet; }
    public void setOutputHeightOffsetSet(boolean outputHeightOffsetSet) { this.outputHeightOffsetSet = outputHeightOffsetSet; }

    /** 精化标定 */
    public int getRefineCalibrate() { return refineCalibrate; }
    public void setRefineCalibrate(int refineCalibrate) { this.refineCalibrate = refineCalibrate; }
    public boolean isRefineCalibrateSet() { return refineCalibrateSet; }
    public void setRefineCalibrateSet(boolean refineCalibrateSet) { this.refineCalibrateSet = refineCalibrateSet; }

    /** 是否进行 LPP 优化 */
    public boolean isLppOptimise() { return lppOptimise; }
    public void setLppOptimise(boolean lppOptimise) { this.lppOptimise = lppOptimise; }
    public boolean isLppOptimiseSet() { return lppOptimiseSet; }
    public void setLppOptimiseSet(boolean lppOptimiseSet) { this.lppOptimiseSet = lppOptimiseSet; }

    /** 是否平滑点云 */
    public boolean isSmoothPoint() { return smoothPoint; }
    public void setSmoothPoint(boolean smoothPoint) { this.smoothPoint = smoothPoint; }
    public boolean isSmoothPointSet() { return smoothPointSet; }
    public void setSmoothPointSet(boolean smoothPointSet) { this.smoothPointSet = smoothPointSet; }

    /** 是否着色点云 */
    public boolean isColorizePoint() { return colorizePoint; }
    public void setColorizePoint(boolean colorizePoint) { this.colorizePoint = colorizePoint; }
    public boolean isColorizePointSet() { return colorizePointSet; }
    public void setColorizePointSet(boolean colorizePointSet) { this.colorizePointSet = colorizePointSet; }

    /** 是否支持 L1 处理 */
    public boolean isCanProcessL1() { return canProcessL1; }
    public void setCanProcessL1(boolean canProcessL1) { this.canProcessL1 = canProcessL1; }
    public boolean isCanProcessL1Set() { return canProcessL1Set; }
    public void setCanProcessL1Set(boolean canProcessL1Set) { this.canProcessL1Set = canProcessL1Set; }

    /** 输出坐标描述 */
    public TerraBuildGeoDesc getOutputGeoDesc() { return outputGeoDesc; }
    public void setOutputGeoDesc(TerraBuildGeoDesc outputGeoDesc) { this.outputGeoDesc = outputGeoDesc; }

    /** 是否输出网格 */
    public boolean isOutputMesh() { return outputMesh; }
    public void setOutputMesh(boolean outputMesh) { this.outputMesh = outputMesh; }
    public boolean isOutputMeshSet() { return outputMeshSet; }
    public void setOutputMeshSet(boolean outputMeshSet) { this.outputMeshSet = outputMeshSet; }

    /** 网格质量等级 */
    public int getQualityLevelMesh() { return qualityLevelMesh; }
    public void setQualityLevelMesh(int qualityLevelMesh) { this.qualityLevelMesh = qualityLevelMesh; }
    public boolean isQualityLevelMeshSet() { return qualityLevelMeshSet; }
    public void setQualityLevelMeshSet(boolean qualityLevelMeshSet) { this.qualityLevelMeshSet = qualityLevelMeshSet; }

    /** 模型简化系数 */
    public double getModelSimplify() { return modelSimplify; }
    public void setModelSimplify(double modelSimplify) { this.modelSimplify = modelSimplify; }
    public boolean isModelSimplifySet() { return modelSimplifySet; }
    public void setModelSimplifySet(boolean modelSimplifySet) { this.modelSimplifySet = modelSimplifySet; }

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

    /** 是否生成 PLY */
    public boolean isGeneratePly() { return generatePly; }
    public void setGeneratePly(boolean generatePly) { this.generatePly = generatePly; }
    public boolean isGeneratePlySet() { return generatePlySet; }
    public void setGeneratePlySet(boolean generatePlySet) { this.generatePlySet = generatePlySet; }

    /** 是否生成 OSGB */
    public boolean isGenerateOsgb() { return generateOsgb; }
    public void setGenerateOsgb(boolean generateOsgb) { this.generateOsgb = generateOsgb; }
    public boolean isGenerateOsgbSet() { return generateOsgbSet; }
    public void setGenerateOsgbSet(boolean generateOsgbSet) { this.generateOsgbSet = generateOsgbSet; }

    /** 是否生成 B3DM */
    public boolean isGenerateB3dm() { return generateB3dm; }
    public void setGenerateB3dm(boolean generateB3dm) { this.generateB3dm = generateB3dm; }
    public boolean isGenerateB3dmSet() { return generateB3dmSet; }
    public void setGenerateB3dmSet(boolean generateB3dmSet) { this.generateB3dmSet = generateB3dmSet; }

    /** 是否生成 I3S */
    public boolean isGenerateI3s() { return generateI3s; }
    public void setGenerateI3s(boolean generateI3s) { this.generateI3s = generateI3s; }
    public boolean isGenerateI3sSet() { return generateI3sSet; }
    public void setGenerateI3sSet(boolean generateI3sSet) { this.generateI3sSet = generateI3sSet; }

    /** 是否生成 2D 地图 */
    public boolean isGenerateMaps2d() { return generateMaps2d; }
    public void setGenerateMaps2d(boolean generateMaps2d) { this.generateMaps2d = generateMaps2d; }
    public boolean isGenerateMaps2dSet() { return generateMaps2dSet; }
    public void setGenerateMaps2dSet(boolean generateMaps2dSet) { this.generateMaps2dSet = generateMaps2dSet; }

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

    /** 2D 分辨率类型 */
    public int getResolutionType2d() { return resolutionType2d; }
    public void setResolutionType2d(int resolutionType2d) { this.resolutionType2d = resolutionType2d; }
    public boolean isResolutionType2dSet() { return resolutionType2dSet; }
    public void setResolutionType2dSet(boolean resolutionType2dSet) { this.resolutionType2dSet = resolutionType2dSet; }

    /** 2D 分辨率等级 */
    public int getResolutionLevel2d() { return resolutionLevel2d; }
    public void setResolutionLevel2d(int resolutionLevel2d) { this.resolutionLevel2d = resolutionLevel2d; }
    public boolean isResolutionLevel2dSet() { return resolutionLevel2dSet; }
    public void setResolutionLevel2dSet(boolean resolutionLevel2dSet) { this.resolutionLevel2dSet = resolutionLevel2dSet; }

    /** 2D 分辨率 GSD */
    public double getResolutionGsd2d() { return resolutionGsd2d; }
    public void setResolutionGsd2d(double resolutionGsd2d) { this.resolutionGsd2d = resolutionGsd2d; }
    public boolean isResolutionGsd2dSet() { return resolutionGsd2dSet; }
    public void setResolutionGsd2dSet(boolean resolutionGsd2dSet) { this.resolutionGsd2dSet = resolutionGsd2dSet; }

    /** DOM 瓦片大小 */
    public int getDomTileSize() { return domTileSize; }
    public void setDomTileSize(int domTileSize) { this.domTileSize = domTileSize; }
    public boolean isDomTileSizeSet() { return domTileSizeSet; }
    public void setDomTileSizeSet(boolean domTileSizeSet) { this.domTileSizeSet = domTileSizeSet; }

    /** 是否使用 GCJ02 坐标系 */
    public boolean isUseGcj02() { return useGcj02; }
    public void setUseGcj02(boolean useGcj02) { this.useGcj02 = useGcj02; }
    public boolean isUseGcj02Set() { return useGcj02Set; }
    public void setUseGcj02Set(boolean useGcj02Set) { this.useGcj02Set = useGcj02Set; }

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

    /** 是否生成 3DGS */
    public boolean isGenerate3dgs() { return generate3dgs; }
    public void setGenerate3dgs(boolean generate3dgs) { this.generate3dgs = generate3dgs; }
    public boolean isGenerate3dgsSet() { return generate3dgsSet; }
    public void setGenerate3dgsSet(boolean generate3dgsSet) { this.generate3dgsSet = generate3dgsSet; }

    /** 3DGS 质量等级 */
    public int getQualityLevel3dgs() { return qualityLevel3dgs; }
    public void setQualityLevel3dgs(int qualityLevel3dgs) { this.qualityLevel3dgs = qualityLevel3dgs; }
    public boolean isQualityLevel3dgsSet() { return qualityLevel3dgsSet; }
    public void setQualityLevel3dgsSet(boolean qualityLevel3dgsSet) { this.qualityLevel3dgsSet = qualityLevel3dgsSet; }

    /** 是否生成 3DTiles */
    public boolean isGenerate3dtiles() { return generate3dtiles; }
    public void setGenerate3dtiles(boolean generate3dtiles) { this.generate3dtiles = generate3dtiles; }
    public boolean isGenerate3dtilesSet() { return generate3dtilesSet; }
    public void setGenerate3dtilesSet(boolean generate3dtilesSet) { this.generate3dtilesSet = generate3dtilesSet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraBuildParameterLidar that = (TerraBuildParameterLidar) o;
        return outputPointcloud == that.outputPointcloud
                && densityPercentLevel == that.densityPercentLevel
                && generatePnts == that.generatePnts
                && generateLas == that.generateLas
                && generateLaz == that.generateLaz
                && generatePcd == that.generatePcd
                && generatePointS3mb == that.generatePointS3mb
                && generatePointPly == that.generatePointPly
                && mergePoint == that.mergePoint
                && outputPlyWithNormals == that.outputPlyWithNormals
                && plyWithNormalsLevel == that.plyWithNormalsLevel
                && groudExtract == that.groudExtract
                && sceneType == that.sceneType
                && Double.compare(that.maxSizeBuilding, maxSizeBuilding) == 0
                && Double.compare(that.iterDist, iterDist) == 0
                && Double.compare(that.iterAngle, iterAngle) == 0
                && Double.compare(that.usableMemoryGb, usableMemoryGb) == 0
                && qualityLevel == that.qualityLevel
                && Double.compare(that.sampleDistance, sampleDistance) == 0
                && Double.compare(that.outputHeightOffset, outputHeightOffset) == 0
                && refineCalibrate == that.refineCalibrate
                && lppOptimise == that.lppOptimise
                && smoothPoint == that.smoothPoint
                && colorizePoint == that.colorizePoint
                && canProcessL1 == that.canProcessL1
                && outputMesh == that.outputMesh
                && qualityLevelMesh == that.qualityLevelMesh
                && Double.compare(that.modelSimplify, modelSimplify) == 0
                && generateObj == that.generateObj
                && generateFbx == that.generateFbx
                && generatePly == that.generatePly
                && generateOsgb == that.generateOsgb
                && generateB3dm == that.generateB3dm
                && generateI3s == that.generateI3s
                && generateMaps2d == that.generateMaps2d
                && generateMaps2dTiff == that.generateMaps2dTiff
                && generateMaps2dTiffCog == that.generateMaps2dTiffCog
                && resolutionType2d == that.resolutionType2d
                && resolutionLevel2d == that.resolutionLevel2d
                && Double.compare(that.resolutionGsd2d, resolutionGsd2d) == 0
                && domTileSize == that.domTileSize
                && useGcj02 == that.useGcj02
                && generateDem == that.generateDem
                && Double.compare(that.demResolution, demResolution) == 0
                && generateContour == that.generateContour
                && Double.compare(that.contourInterval, contourInterval) == 0
                && Double.compare(that.contourDatum, contourDatum) == 0
                && Double.compare(that.contourElevationAnnotationRadius, contourElevationAnnotationRadius) == 0
                && useContourFilter == that.useContourFilter
                && Double.compare(that.contourFilterLength, contourFilterLength) == 0
                && generate3dgs == that.generate3dgs
                && qualityLevel3dgs == that.qualityLevel3dgs
                && generate3dtiles == that.generate3dtiles
                && Objects.equals(outputGeoDesc, that.outputGeoDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputPointcloud, densityPercentLevel, generatePnts,
                generateLas, generateLaz, generatePcd, generatePointS3mb, generatePointPly,
                mergePoint, outputPlyWithNormals, plyWithNormalsLevel, groudExtract,
                sceneType, maxSizeBuilding, iterDist, iterAngle, usableMemoryGb,
                qualityLevel, sampleDistance, outputHeightOffset, refineCalibrate,
                lppOptimise, smoothPoint, colorizePoint, canProcessL1, outputGeoDesc,
                outputMesh, qualityLevelMesh, modelSimplify, generateObj, generateFbx,
                generatePly, generateOsgb, generateB3dm, generateI3s,
                generateMaps2d, generateMaps2dTiff, generateMaps2dTiffCog,
                resolutionType2d, resolutionLevel2d, resolutionGsd2d, domTileSize,
                useGcj02, generateDem,
                demResolution, generateContour, contourInterval, contourDatum,
                contourElevationAnnotationRadius, useContourFilter, contourFilterLength,
                generate3dgs, qualityLevel3dgs, generate3dtiles);
    }

    @Override
    public String toString() {
        return "TerraBuildParameterLidar{"
                + "qualityLevel=" + qualityLevel
                + ", outputPointcloud=" + outputPointcloud
                + ", generatePointS3mb=" + generatePointS3mb
                + ", outputPlyWithNormals=" + outputPlyWithNormals
                + ", plyWithNormalsLevel=" + plyWithNormalsLevel
                + ", groudExtract=" + groudExtract
                + ", sceneType=" + sceneType
                + ", maxSizeBuilding=" + maxSizeBuilding
                + ", iterDist=" + iterDist
                + ", iterAngle=" + iterAngle
                + ", usableMemoryGb=" + usableMemoryGb
                + ", resolutionLevel2d=" + resolutionLevel2d
                + ", useGcj02=" + useGcj02
                + ", outputMesh=" + outputMesh
                + ", outputGeoDesc=" + outputGeoDesc
                + '}';
    }
}
