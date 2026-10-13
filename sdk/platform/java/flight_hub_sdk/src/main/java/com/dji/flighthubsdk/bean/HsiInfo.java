// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** 飞行器 HSI 信息（障碍物感知/避障传感器数据）。 */
public final class HsiInfo {

    /** 周围障碍物距离数组 */
    private List<Integer> aroundDistances;
    /** 后方传感器启用 */
    private boolean backEnable;
    /** 后方传感器工作中 */
    private boolean backWork;
    /** 下方距离 */
    private int downDistance;
    /** 下方传感器启用 */
    private boolean downEnable;
    /** 下方传感器工作中 */
    private boolean downWork;
    /** 前方传感器启用 */
    private boolean frontEnable;
    /** 前方传感器工作中 */
    private boolean frontWork;
    /** 水平避障启用 */
    private boolean horizontalEnable;
    /** 水平避障工作中 */
    private boolean horizontalWork;
    /** 左方传感器启用 */
    private boolean leftEnable;
    /** 左方传感器工作中 */
    private boolean leftWork;
    /** 右方传感器启用 */
    private boolean rightEnable;
    /** 右方传感器工作中 */
    private boolean rightWork;
    /** 上方距离 */
    private int upDistance;
    /** 上方传感器启用 */
    private boolean upEnable;
    /** 上方传感器工作中 */
    private boolean upWork;
    /** 垂直避障启用 */
    private boolean verticalEnable;
    /** 垂直避障工作中 */
    private boolean verticalWork;
    public HsiInfo() {
    }

/**
     * 构造 HsiInfo。
     *
     * @param aroundDistances 周围障碍物距离数组
     * @param backEnable 后方传感器启用
     * @param backWork 后方传感器工作中
     * @param downDistance 下方距离
     * @param downEnable 下方传感器启用
     * @param downWork 下方传感器工作中
     * @param frontEnable 前方传感器启用
     * @param frontWork 前方传感器工作中
     * @param horizontalEnable 水平避障启用
     * @param horizontalWork 水平避障工作中
     * @param leftEnable 左方传感器启用
     * @param leftWork 左方传感器工作中
     * @param rightEnable 右方传感器启用
     * @param rightWork 右方传感器工作中
     * @param upDistance 上方距离
     * @param upEnable 上方传感器启用
     * @param upWork 上方传感器工作中
     * @param verticalEnable 垂直避障启用
     * @param verticalWork 垂直避障工作中
     */
    public HsiInfo(List<Integer> aroundDistances,
                   boolean backEnable, boolean backWork,
                   int downDistance, boolean downEnable, boolean downWork,
                   boolean frontEnable, boolean frontWork,
                   boolean horizontalEnable, boolean horizontalWork,
                   boolean leftEnable, boolean leftWork,
                   boolean rightEnable, boolean rightWork,
                   int upDistance, boolean upEnable, boolean upWork,
                   boolean verticalEnable, boolean verticalWork) {
        this.aroundDistances = aroundDistances != null ? aroundDistances : Collections.emptyList();
        this.backEnable = backEnable;
        this.backWork = backWork;
        this.downDistance = downDistance;
        this.downEnable = downEnable;
        this.downWork = downWork;
        this.frontEnable = frontEnable;
        this.frontWork = frontWork;
        this.horizontalEnable = horizontalEnable;
        this.horizontalWork = horizontalWork;
        this.leftEnable = leftEnable;
        this.leftWork = leftWork;
        this.rightEnable = rightEnable;
        this.rightWork = rightWork;
        this.upDistance = upDistance;
        this.upEnable = upEnable;
        this.upWork = upWork;
        this.verticalEnable = verticalEnable;
        this.verticalWork = verticalWork;
    }

    /** 周围障碍物距离数组 */
    public List<Integer> getAroundDistances() { return aroundDistances; }
    /** 后方传感器启用 */
    public boolean isBackEnable() { return backEnable; }
    /** 后方传感器工作中 */
    public boolean isBackWork() { return backWork; }
    /** 下方距离 */
    public int getDownDistance() { return downDistance; }
    /** 下方传感器启用 */
    public boolean isDownEnable() { return downEnable; }
    /** 下方传感器工作中 */
    public boolean isDownWork() { return downWork; }
    /** 前方传感器启用 */
    public boolean isFrontEnable() { return frontEnable; }
    /** 前方传感器工作中 */
    public boolean isFrontWork() { return frontWork; }
    /** 水平避障启用 */
    public boolean isHorizontalEnable() { return horizontalEnable; }
    /** 水平避障工作中 */
    public boolean isHorizontalWork() { return horizontalWork; }
    /** 左方传感器启用 */
    public boolean isLeftEnable() { return leftEnable; }
    /** 左方传感器工作中 */
    public boolean isLeftWork() { return leftWork; }
    /** 右方传感器启用 */
    public boolean isRightEnable() { return rightEnable; }
    /** 右方传感器工作中 */
    public boolean isRightWork() { return rightWork; }
    /** 上方距离 */
    public int getUpDistance() { return upDistance; }
    /** 上方传感器启用 */
    public boolean isUpEnable() { return upEnable; }
    /** 上方传感器工作中 */
    public boolean isUpWork() { return upWork; }
    /** 垂直避障启用 */
    public boolean isVerticalEnable() { return verticalEnable; }
    /** 垂直避障工作中 */
    public boolean isVerticalWork() { return verticalWork; }

    /** 周围障碍物距离数组 */
    public void setAroundDistances(List<Integer> aroundDistances) {
        this.aroundDistances = aroundDistances;
    }

    /** 后方传感器启用 */
    public void setBackEnable(boolean backEnable) {
        this.backEnable = backEnable;
    }

    /** 后方传感器工作中 */
    public void setBackWork(boolean backWork) {
        this.backWork = backWork;
    }

    /** 下方距离 */
    public void setDownDistance(int downDistance) {
        this.downDistance = downDistance;
    }

    /** 下方传感器启用 */
    public void setDownEnable(boolean downEnable) {
        this.downEnable = downEnable;
    }

    /** 下方传感器工作中 */
    public void setDownWork(boolean downWork) {
        this.downWork = downWork;
    }

    /** 前方传感器启用 */
    public void setFrontEnable(boolean frontEnable) {
        this.frontEnable = frontEnable;
    }

    /** 前方传感器工作中 */
    public void setFrontWork(boolean frontWork) {
        this.frontWork = frontWork;
    }

    /** 水平避障启用 */
    public void setHorizontalEnable(boolean horizontalEnable) {
        this.horizontalEnable = horizontalEnable;
    }

    /** 水平避障工作中 */
    public void setHorizontalWork(boolean horizontalWork) {
        this.horizontalWork = horizontalWork;
    }

    /** 左方传感器启用 */
    public void setLeftEnable(boolean leftEnable) {
        this.leftEnable = leftEnable;
    }

    /** 左方传感器工作中 */
    public void setLeftWork(boolean leftWork) {
        this.leftWork = leftWork;
    }

    /** 右方传感器启用 */
    public void setRightEnable(boolean rightEnable) {
        this.rightEnable = rightEnable;
    }

    /** 右方传感器工作中 */
    public void setRightWork(boolean rightWork) {
        this.rightWork = rightWork;
    }

    /** 上方距离 */
    public void setUpDistance(int upDistance) {
        this.upDistance = upDistance;
    }

    /** 上方传感器启用 */
    public void setUpEnable(boolean upEnable) {
        this.upEnable = upEnable;
    }

    /** 上方传感器工作中 */
    public void setUpWork(boolean upWork) {
        this.upWork = upWork;
    }

    /** 垂直避障启用 */
    public void setVerticalEnable(boolean verticalEnable) {
        this.verticalEnable = verticalEnable;
    }

    /** 垂直避障工作中 */
    public void setVerticalWork(boolean verticalWork) {
        this.verticalWork = verticalWork;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HsiInfo that = (HsiInfo) o;
        return backEnable == that.backEnable && backWork == that.backWork
                && downDistance == that.downDistance && downEnable == that.downEnable && downWork == that.downWork
                && frontEnable == that.frontEnable && frontWork == that.frontWork
                && horizontalEnable == that.horizontalEnable && horizontalWork == that.horizontalWork
                && leftEnable == that.leftEnable && leftWork == that.leftWork
                && rightEnable == that.rightEnable && rightWork == that.rightWork
                && upDistance == that.upDistance && upEnable == that.upEnable && upWork == that.upWork
                && verticalEnable == that.verticalEnable && verticalWork == that.verticalWork
                && Objects.equals(aroundDistances, that.aroundDistances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aroundDistances, backEnable, backWork,
                downDistance, downEnable, downWork,
                frontEnable, frontWork,
                horizontalEnable, horizontalWork,
                leftEnable, leftWork,
                rightEnable, rightWork,
                upDistance, upEnable, upWork,
                verticalEnable, verticalWork);
    }

    @Override
    public String toString() {
        return "HsiInfo{"
                + "aroundDistances=" + aroundDistances
                + ", backEnable=" + backEnable + ", backWork=" + backWork
                + ", downDistance=" + downDistance + ", downEnable=" + downEnable + ", downWork=" + downWork
                + ", frontEnable=" + frontEnable + ", frontWork=" + frontWork
                + ", horizontalEnable=" + horizontalEnable + ", horizontalWork=" + horizontalWork
                + ", leftEnable=" + leftEnable + ", leftWork=" + leftWork
                + ", rightEnable=" + rightEnable + ", rightWork=" + rightWork
                + ", upDistance=" + upDistance + ", upEnable=" + upEnable + ", upWork=" + upWork
                + ", verticalEnable=" + verticalEnable + ", verticalWork=" + verticalWork
                + '}';
    }
}
