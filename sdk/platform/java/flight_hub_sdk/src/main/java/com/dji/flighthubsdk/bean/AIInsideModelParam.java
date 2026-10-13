// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

/** AI Inside 模型推理参数 */
public class AIInsideModelParam {
    /** 置信度阈值，低于此值的检测结果将被过滤 */
    private double confThreshold;
    /** NMS 前最大候选框数量 */
    private int nmsPre;
    /** IoU 阈值，用于非极大值抑制（NMS）去重 */
    private double iouThreshold;

    public AIInsideModelParam() {}

    /** 置信度阈值，低于此值的检测结果将被过滤 */
    public double getConfThreshold() { return confThreshold; }

    /** @param v 置信度阈值，低于此值的检测结果将被过滤 */
    public void setConfThreshold(double v) { this.confThreshold = v; }

    /** NMS 前最大候选框数量 */
    public int getNmsPre() { return nmsPre; }

    /** @param v NMS 前最大候选框数量 */
    public void setNmsPre(int v) { this.nmsPre = v; }

    /** IoU 阈值，用于非极大值抑制（NMS）去重 */
    public double getIouThreshold() { return iouThreshold; }

    /** @param v IoU 阈值，用于非极大值抑制（NMS）去重 */
    public void setIouThreshold(double v) { this.iouThreshold = v; }
}
