// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 算法操作嵌套进度（对齐后端 percent / current_step） */
public class AIInsideOperateProgressDetail {
    /** 进度百分比 0~100 */
    private int percent;
    /** 当前步骤 */
    private int currentStep;

    public AIInsideOperateProgressDetail() {}

    /** 进度百分比 0~100 */
    public int getPercent() { return percent; }
    /** @param percent 进度百分比 0~100 */
    public void setPercent(int v) { this.percent = v; }

    /** 当前步骤 */
    public int getCurrentStep() { return currentStep; }
    /** @param currentStep 当前步骤 */
    public void setCurrentStep(int v) { this.currentStep = v; }
}
