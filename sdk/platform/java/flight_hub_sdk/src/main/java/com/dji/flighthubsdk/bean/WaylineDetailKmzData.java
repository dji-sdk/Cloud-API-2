// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;

import java.util.Objects;
/** 航线详情中的 KMZ 解析数据。 */
public final class WaylineDetailKmzData {

    private int waylineType;
    private int waylinePointCount;
    private int exitOnRcLostType;
    private int exitOnRcLostBehavior;
    private int startRecordActionCount;
    private int stopRecordActionCount;
    private double orthoCameraOverlapH;
    private double orthoCameraOverlapW;
    private double inclinedCameraOverlapH;
    private double inclinedCameraOverlapW;
    private int supplementPrismModelCount;
    private int estimatedPhotoCount;
    private double duration;
    private double distance;
    private double surveyArea;
    private double startLatitude;
    private double startLongitude;
    private double startAltitude;
    private double marginDistance;
    private double orthoGsd;
    private double inclineGsd;
    private boolean waylineValidityCheck;
    private boolean hasOrientedAction;
    private boolean hasAccurateAction;
    private boolean surfaceFlyOn;
    private boolean elevationOptimizationOn;
    private boolean smartObliqueOn;
    private boolean mappingStripUseTemplateAltitude;
    private boolean withMegaphone;
    private boolean withSearchLight;
    private boolean aiWayline;
    private String droneType;
    private String targetDetectAreaFilePath;
    private List<Integer> waylineValidityCheckCodes;
    private List<String> elevationFileNames;
    private List<String> payloadTypes;
    private List<String> payloadTypesWithIndex;
    private List<String> accurateActionUuids;
    private List<String> actionUuids;
    private WaylineDetailSurveyAreaScope surveyAreaScope;
    public WaylineDetailKmzData() {
    }

/**
     * 构造 WaylineDetailKmzData。
     */
    public WaylineDetailKmzData(
            int waylineType,
            int waylinePointCount,
            int exitOnRcLostType,
            int exitOnRcLostBehavior,
            int startRecordActionCount,
            int stopRecordActionCount,
            double orthoCameraOverlapH,
            double orthoCameraOverlapW,
            double inclinedCameraOverlapH,
            double inclinedCameraOverlapW,
            int supplementPrismModelCount,
            int estimatedPhotoCount,
            double duration,
            double distance,
            double surveyArea,
            double startLatitude,
            double startLongitude,
            double startAltitude,
            double marginDistance,
            double orthoGsd,
            double inclineGsd,
            boolean waylineValidityCheck,
            boolean hasOrientedAction,
            boolean hasAccurateAction,
            boolean surfaceFlyOn,
            boolean elevationOptimizationOn,
            boolean smartObliqueOn,
            boolean mappingStripUseTemplateAltitude,
            boolean withMegaphone,
            boolean withSearchLight,
            boolean aiWayline,
            String droneType,
            String targetDetectAreaFilePath,
            List<Integer> waylineValidityCheckCodes,
            List<String> elevationFileNames,
            List<String> payloadTypes,
            List<String> payloadTypesWithIndex,
            List<String> accurateActionUuids,
            List<String> actionUuids,
            WaylineDetailSurveyAreaScope surveyAreaScope) {
        this.waylineType = waylineType;
        this.waylinePointCount = waylinePointCount;
        this.exitOnRcLostType = exitOnRcLostType;
        this.exitOnRcLostBehavior = exitOnRcLostBehavior;
        this.startRecordActionCount = startRecordActionCount;
        this.stopRecordActionCount = stopRecordActionCount;
        this.orthoCameraOverlapH = orthoCameraOverlapH;
        this.orthoCameraOverlapW = orthoCameraOverlapW;
        this.inclinedCameraOverlapH = inclinedCameraOverlapH;
        this.inclinedCameraOverlapW = inclinedCameraOverlapW;
        this.supplementPrismModelCount = supplementPrismModelCount;
        this.estimatedPhotoCount = estimatedPhotoCount;
        this.duration = duration;
        this.distance = distance;
        this.surveyArea = surveyArea;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.startAltitude = startAltitude;
        this.marginDistance = marginDistance;
        this.orthoGsd = orthoGsd;
        this.inclineGsd = inclineGsd;
        this.waylineValidityCheck = waylineValidityCheck;
        this.hasOrientedAction = hasOrientedAction;
        this.hasAccurateAction = hasAccurateAction;
        this.surfaceFlyOn = surfaceFlyOn;
        this.elevationOptimizationOn = elevationOptimizationOn;
        this.smartObliqueOn = smartObliqueOn;
        this.mappingStripUseTemplateAltitude = mappingStripUseTemplateAltitude;
        this.withMegaphone = withMegaphone;
        this.withSearchLight = withSearchLight;
        this.aiWayline = aiWayline;
        this.droneType = droneType != null ? droneType : "";
        this.targetDetectAreaFilePath = targetDetectAreaFilePath != null ? targetDetectAreaFilePath : "";
        this.waylineValidityCheckCodes =
                waylineValidityCheckCodes != null ? waylineValidityCheckCodes : Collections.emptyList();
        this.elevationFileNames = elevationFileNames != null ? elevationFileNames : Collections.emptyList();
        this.payloadTypes = payloadTypes != null ? payloadTypes : Collections.emptyList();
        this.payloadTypesWithIndex = payloadTypesWithIndex != null ? payloadTypesWithIndex : Collections.emptyList();
        this.accurateActionUuids = accurateActionUuids != null ? accurateActionUuids : Collections.emptyList();
        this.actionUuids = actionUuids != null ? actionUuids : Collections.emptyList();
        this.surveyAreaScope = surveyAreaScope;
    }

    /**
     * 航线类型。枚举：0=航点航线(WAYPOINT), 1=面状航线/正射(MAPPING2D), 2=面状航线/倾斜(MAPPING3D),
     * 3=带状航线(MAPPINGSTRIP), 4=斜面航线(MAPPING2D_Facade), 5=标准几何体航线(SOLID),
     * 7=贴近摄影航线(MAPPING), 8=AI巡逻航线(AI_PATROL)
     */
    public int getWaylineType() { return waylineType; }
    /** 航线点数量 */
    public int getWaylinePointCount() { return waylinePointCount; }
    /** 遥控器失联退出类型 */
    public int getExitOnRcLostType() { return exitOnRcLostType; }
    /** 遥控器失联退出行为 */
    public int getExitOnRcLostBehavior() { return exitOnRcLostBehavior; }
    /** 开始录制动作数量 */
    public int getStartRecordActionCount() { return startRecordActionCount; }
    /** 停止录制动作数量 */
    public int getStopRecordActionCount() { return stopRecordActionCount; }
    /** 正射相机水平重叠率 */
    public double getOrthoCameraOverlapH() { return orthoCameraOverlapH; }
    /** 正射相机宽度重叠率 */
    public double getOrthoCameraOverlapW() { return orthoCameraOverlapW; }
    /** 倾斜相机水平重叠率 */
    public double getInclinedCameraOverlapH() { return inclinedCameraOverlapH; }
    /** 倾斜相机宽度重叠率 */
    public double getInclinedCameraOverlapW() { return inclinedCameraOverlapW; }
    /** 补拍棱镜模型数量 */
    public int getSupplementPrismModelCount() { return supplementPrismModelCount; }
    /** 预计拍照数量 */
    public int getEstimatedPhotoCount() { return estimatedPhotoCount; }
    /** 时长 */
    public double getDuration() { return duration; }
    /** 距离 */
    public double getDistance() { return distance; }
    /** 测区面积 */
    public double getSurveyArea() { return surveyArea; }
    /** 起始纬度 */
    public double getStartLatitude() { return startLatitude; }
    /** 起始经度 */
    public double getStartLongitude() { return startLongitude; }
    /** 起始高度 */
    public double getStartAltitude() { return startAltitude; }
    /** 边距距离 */
    public double getMarginDistance() { return marginDistance; }
    /** 正射GSD */
    public double getOrthoGsd() { return orthoGsd; }
    /** 倾斜GSD */
    public double getInclineGsd() { return inclineGsd; }
    /** 是否航线有效性检查 */
    public boolean isWaylineValidityCheck() { return waylineValidityCheck; }
    /** 是否有定向动作 */
    public boolean isHasOrientedAction() { return hasOrientedAction; }
    /** 是否有精确动作 */
    public boolean isHasAccurateAction() { return hasAccurateAction; }
    /** 是否仿地飞行 */
    public boolean isSurfaceFlyOn() { return surfaceFlyOn; }
    /** 是否高程优化 */
    public boolean isElevationOptimizationOn() { return elevationOptimizationOn; }
    /** 是否智能倾斜 */
    public boolean isSmartObliqueOn() { return smartObliqueOn; }
    /** 是否测绘带使用模板高度 */
    public boolean isMappingStripUseTemplateAltitude() { return mappingStripUseTemplateAltitude; }
    /** 是否携带喊话器 */
    public boolean isWithMegaphone() { return withMegaphone; }
    /** 是否携带探照灯 */
    public boolean isWithSearchLight() { return withSearchLight; }
    /** 是否AI航线 */
    public boolean isAiWayline() { return aiWayline; }
    /** 飞行器类型 */
    public String getDroneType() { return droneType; }
    /** 目标检测区域文件路径 */
    public String getTargetDetectAreaFilePath() { return targetDetectAreaFilePath; }
    /** 航线有效性检查码列表 */
    public List<Integer> getWaylineValidityCheckCodes() { return waylineValidityCheckCodes; }
    /** 高程文件名列表 */
    public List<String> getElevationFileNames() { return elevationFileNames; }
    /** 负载类型列表 */
    public List<String> getPayloadTypes() { return payloadTypes; }
    /** 带索引的负载类型列表 */
    public List<String> getPayloadTypesWithIndex() { return payloadTypesWithIndex; }
    /** 精确动作UUID列表 */
    public List<String> getAccurateActionUuids() { return accurateActionUuids; }
    /** 动作UUID列表 */
    public List<String> getActionUuids() { return actionUuids; }
    /** 测区范围 */
    public WaylineDetailSurveyAreaScope getSurveyAreaScope() { return surveyAreaScope; }

    /**
     * 航线类型。枚举：0=航点航线(WAYPOINT), 1=面状航线/正射(MAPPING2D), 2=面状航线/倾斜(MAPPING3D),
     * 3=带状航线(MAPPINGSTRIP), 4=斜面航线(MAPPING2D_Facade), 5=标准几何体航线(SOLID),
     * 7=贴近摄影航线(MAPPING), 8=AI巡逻航线(AI_PATROL)
     */
    public void setWaylineType(int waylineType) {
        this.waylineType = waylineType;
    }

    /** 航线点数量 */
    public void setWaylinePointCount(int waylinePointCount) {
        this.waylinePointCount = waylinePointCount;
    }

    /** 遥控器失联退出类型 */
    public void setExitOnRcLostType(int exitOnRcLostType) {
        this.exitOnRcLostType = exitOnRcLostType;
    }

    /** 遥控器失联退出行为 */
    public void setExitOnRcLostBehavior(int exitOnRcLostBehavior) {
        this.exitOnRcLostBehavior = exitOnRcLostBehavior;
    }

    /** 开始录制动作数量 */
    public void setStartRecordActionCount(int startRecordActionCount) {
        this.startRecordActionCount = startRecordActionCount;
    }

    /** 停止录制动作数量 */
    public void setStopRecordActionCount(int stopRecordActionCount) {
        this.stopRecordActionCount = stopRecordActionCount;
    }

    /** 正射相机水平重叠率 */
    public void setOrthoCameraOverlapH(double orthoCameraOverlapH) {
        this.orthoCameraOverlapH = orthoCameraOverlapH;
    }

    /** 正射相机宽度重叠率 */
    public void setOrthoCameraOverlapW(double orthoCameraOverlapW) {
        this.orthoCameraOverlapW = orthoCameraOverlapW;
    }

    /** 倾斜相机水平重叠率 */
    public void setInclinedCameraOverlapH(double inclinedCameraOverlapH) {
        this.inclinedCameraOverlapH = inclinedCameraOverlapH;
    }

    /** 倾斜相机宽度重叠率 */
    public void setInclinedCameraOverlapW(double inclinedCameraOverlapW) {
        this.inclinedCameraOverlapW = inclinedCameraOverlapW;
    }

    /** 补拍棱镜模型数量 */
    public void setSupplementPrismModelCount(int supplementPrismModelCount) {
        this.supplementPrismModelCount = supplementPrismModelCount;
    }

    /** 预计拍照数量 */
    public void setEstimatedPhotoCount(int estimatedPhotoCount) {
        this.estimatedPhotoCount = estimatedPhotoCount;
    }

    /** 时长 */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /** 距离 */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /** 测区面积 */
    public void setSurveyArea(double surveyArea) {
        this.surveyArea = surveyArea;
    }

    /** 起始纬度 */
    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    /** 起始经度 */
    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    /** 起始高度 */
    public void setStartAltitude(double startAltitude) {
        this.startAltitude = startAltitude;
    }

    /** 边距距离 */
    public void setMarginDistance(double marginDistance) {
        this.marginDistance = marginDistance;
    }

    /** 正射GSD */
    public void setOrthoGsd(double orthoGsd) {
        this.orthoGsd = orthoGsd;
    }

    /** 倾斜GSD */
    public void setInclineGsd(double inclineGsd) {
        this.inclineGsd = inclineGsd;
    }

    /** 是否航线有效性检查 */
    public void setWaylineValidityCheck(boolean waylineValidityCheck) {
        this.waylineValidityCheck = waylineValidityCheck;
    }

    /** 是否有定向动作 */
    public void setHasOrientedAction(boolean hasOrientedAction) {
        this.hasOrientedAction = hasOrientedAction;
    }

    /** 是否有精确动作 */
    public void setHasAccurateAction(boolean hasAccurateAction) {
        this.hasAccurateAction = hasAccurateAction;
    }

    /** 是否仿地飞行 */
    public void setSurfaceFlyOn(boolean surfaceFlyOn) {
        this.surfaceFlyOn = surfaceFlyOn;
    }

    /** 是否高程优化 */
    public void setElevationOptimizationOn(boolean elevationOptimizationOn) {
        this.elevationOptimizationOn = elevationOptimizationOn;
    }

    /** 是否智能倾斜 */
    public void setSmartObliqueOn(boolean smartObliqueOn) {
        this.smartObliqueOn = smartObliqueOn;
    }

    /** 是否测绘带使用模板高度 */
    public void setMappingStripUseTemplateAltitude(boolean mappingStripUseTemplateAltitude) {
        this.mappingStripUseTemplateAltitude = mappingStripUseTemplateAltitude;
    }

    /** 是否携带喊话器 */
    public void setWithMegaphone(boolean withMegaphone) {
        this.withMegaphone = withMegaphone;
    }

    /** 是否携带探照灯 */
    public void setWithSearchLight(boolean withSearchLight) {
        this.withSearchLight = withSearchLight;
    }

    /** 是否AI航线 */
    public void setAiWayline(boolean aiWayline) {
        this.aiWayline = aiWayline;
    }

    /** 飞行器类型 */
    public void setDroneType(String droneType) {
        this.droneType = droneType;
    }

    /** 目标检测区域文件路径 */
    public void setTargetDetectAreaFilePath(String targetDetectAreaFilePath) {
        this.targetDetectAreaFilePath = targetDetectAreaFilePath;
    }

    /** 航线有效性检查码列表 */
    public void setWaylineValidityCheckCodes(List<Integer> waylineValidityCheckCodes) {
        this.waylineValidityCheckCodes = waylineValidityCheckCodes;
    }

    /** 高程文件名列表 */
    public void setElevationFileNames(List<String> elevationFileNames) {
        this.elevationFileNames = elevationFileNames;
    }

    /** 负载类型列表 */
    public void setPayloadTypes(List<String> payloadTypes) {
        this.payloadTypes = payloadTypes;
    }

    /** 带索引的负载类型列表 */
    public void setPayloadTypesWithIndex(List<String> payloadTypesWithIndex) {
        this.payloadTypesWithIndex = payloadTypesWithIndex;
    }

    /** 精确动作UUID列表 */
    public void setAccurateActionUuids(List<String> accurateActionUuids) {
        this.accurateActionUuids = accurateActionUuids;
    }

    /** 动作UUID列表 */
    public void setActionUuids(List<String> actionUuids) {
        this.actionUuids = actionUuids;
    }

    /** 测区范围 */
    public void setSurveyAreaScope(WaylineDetailSurveyAreaScope surveyAreaScope) {
        this.surveyAreaScope = surveyAreaScope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WaylineDetailKmzData that = (WaylineDetailKmzData) o;
        return waylineType == that.waylineType
                && waylinePointCount == that.waylinePointCount
                && exitOnRcLostType == that.exitOnRcLostType
                && exitOnRcLostBehavior == that.exitOnRcLostBehavior
                && startRecordActionCount == that.startRecordActionCount
                && stopRecordActionCount == that.stopRecordActionCount
                && Double.compare(that.orthoCameraOverlapH, orthoCameraOverlapH) == 0
                && Double.compare(that.orthoCameraOverlapW, orthoCameraOverlapW) == 0
                && Double.compare(that.inclinedCameraOverlapH, inclinedCameraOverlapH) == 0
                && Double.compare(that.inclinedCameraOverlapW, inclinedCameraOverlapW) == 0
                && supplementPrismModelCount == that.supplementPrismModelCount
                && estimatedPhotoCount == that.estimatedPhotoCount
                && Double.compare(that.duration, duration) == 0
                && Double.compare(that.distance, distance) == 0
                && Double.compare(that.surveyArea, surveyArea) == 0
                && Double.compare(that.startLatitude, startLatitude) == 0
                && Double.compare(that.startLongitude, startLongitude) == 0
                && Double.compare(that.startAltitude, startAltitude) == 0
                && Double.compare(that.marginDistance, marginDistance) == 0
                && Double.compare(that.orthoGsd, orthoGsd) == 0
                && Double.compare(that.inclineGsd, inclineGsd) == 0
                && waylineValidityCheck == that.waylineValidityCheck
                && hasOrientedAction == that.hasOrientedAction
                && hasAccurateAction == that.hasAccurateAction
                && surfaceFlyOn == that.surfaceFlyOn
                && elevationOptimizationOn == that.elevationOptimizationOn
                && smartObliqueOn == that.smartObliqueOn
                && mappingStripUseTemplateAltitude == that.mappingStripUseTemplateAltitude
                && withMegaphone == that.withMegaphone
                && withSearchLight == that.withSearchLight
                && aiWayline == that.aiWayline
                && Objects.equals(droneType, that.droneType)
                && Objects.equals(targetDetectAreaFilePath, that.targetDetectAreaFilePath)
                && Objects.equals(waylineValidityCheckCodes, that.waylineValidityCheckCodes)
                && Objects.equals(elevationFileNames, that.elevationFileNames)
                && Objects.equals(payloadTypes, that.payloadTypes)
                && Objects.equals(payloadTypesWithIndex, that.payloadTypesWithIndex)
                && Objects.equals(accurateActionUuids, that.accurateActionUuids)
                && Objects.equals(actionUuids, that.actionUuids)
                && Objects.equals(surveyAreaScope, that.surveyAreaScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                waylineType,
                waylinePointCount,
                exitOnRcLostType,
                exitOnRcLostBehavior,
                startRecordActionCount,
                stopRecordActionCount,
                orthoCameraOverlapH,
                orthoCameraOverlapW,
                inclinedCameraOverlapH,
                inclinedCameraOverlapW,
                supplementPrismModelCount,
                estimatedPhotoCount,
                duration,
                distance,
                surveyArea,
                startLatitude,
                startLongitude,
                startAltitude,
                marginDistance,
                orthoGsd,
                inclineGsd,
                waylineValidityCheck,
                hasOrientedAction,
                hasAccurateAction,
                surfaceFlyOn,
                elevationOptimizationOn,
                smartObliqueOn,
                mappingStripUseTemplateAltitude,
                withMegaphone,
                withSearchLight,
                aiWayline,
                droneType,
                targetDetectAreaFilePath,
                waylineValidityCheckCodes,
                elevationFileNames,
                payloadTypes,
                payloadTypesWithIndex,
                accurateActionUuids,
                actionUuids,
                surveyAreaScope);
    }

    @Override
    public String toString() {
        return "WaylineDetailKmzData{"
                + "waylineType=" + waylineType
                + ", waylinePointCount=" + waylinePointCount
                + ", exitOnRcLostType=" + exitOnRcLostType
                + ", exitOnRcLostBehavior=" + exitOnRcLostBehavior
                + ", startRecordActionCount=" + startRecordActionCount
                + ", stopRecordActionCount=" + stopRecordActionCount
                + ", orthoCameraOverlapH=" + orthoCameraOverlapH
                + ", orthoCameraOverlapW=" + orthoCameraOverlapW
                + ", inclinedCameraOverlapH=" + inclinedCameraOverlapH
                + ", inclinedCameraOverlapW=" + inclinedCameraOverlapW
                + ", supplementPrismModelCount=" + supplementPrismModelCount
                + ", estimatedPhotoCount=" + estimatedPhotoCount
                + ", duration=" + duration
                + ", distance=" + distance
                + ", surveyArea=" + surveyArea
                + ", startLatitude=" + startLatitude
                + ", startLongitude=" + startLongitude
                + ", startAltitude=" + startAltitude
                + ", marginDistance=" + marginDistance
                + ", orthoGsd=" + orthoGsd
                + ", inclineGsd=" + inclineGsd
                + ", waylineValidityCheck=" + waylineValidityCheck
                + ", hasOrientedAction=" + hasOrientedAction
                + ", hasAccurateAction=" + hasAccurateAction
                + ", surfaceFlyOn=" + surfaceFlyOn
                + ", elevationOptimizationOn=" + elevationOptimizationOn
                + ", smartObliqueOn=" + smartObliqueOn
                + ", mappingStripUseTemplateAltitude=" + mappingStripUseTemplateAltitude
                + ", withMegaphone=" + withMegaphone
                + ", withSearchLight=" + withSearchLight
                + ", aiWayline=" + aiWayline
                + ", droneType='" + droneType + '\''
                + ", targetDetectAreaFilePath='" + targetDetectAreaFilePath + '\''
                + ", waylineValidityCheckCodes=" + waylineValidityCheckCodes
                + ", elevationFileNames=" + elevationFileNames
                + ", payloadTypes=" + payloadTypes
                + ", payloadTypesWithIndex=" + payloadTypesWithIndex
                + ", accurateActionUuids=" + accurateActionUuids
                + ", actionUuids=" + actionUuids
                + ", surveyAreaScope=" + surveyAreaScope
                + '}';
    }
}
