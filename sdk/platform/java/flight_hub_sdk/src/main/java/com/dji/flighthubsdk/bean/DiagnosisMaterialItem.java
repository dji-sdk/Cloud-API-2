// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 定损报告 - 物料诊断结果 */
public final class DiagnosisMaterialItem {
    private DiagnosisErrorCodeList errorCodeList;
    private String materialNum;
    private String materialName;
    private String materialProbability;
    private int hmsTotal;
    public DiagnosisMaterialItem() {
    }

/**
     * 构造 DiagnosisMaterialItem。
     *
     * @param errorCodeList 错误码列表
     * @param materialNum 物料编号
     * @param materialName 物料名称
     * @param materialProbability 物料概率
     * @param hmsTotal HMS 事件总数
     */
    public DiagnosisMaterialItem(DiagnosisErrorCodeList errorCodeList, String materialNum, String materialName, String materialProbability, int hmsTotal) {
        this.errorCodeList = errorCodeList;
        this.materialNum = materialNum;
        this.materialName = materialName;
        this.materialProbability = materialProbability;
        this.hmsTotal = hmsTotal;
    }

    /** 错误码列表 */
    public DiagnosisErrorCodeList getErrorCodeList() { return errorCodeList; }
    /** 物料编号 */
    public String getMaterialNum() { return materialNum; }
    /** 物料名称 */
    public String getMaterialName() { return materialName; }
    /** 物料概率 */
    public String getMaterialProbability() { return materialProbability; }
    /** HMS 事件总数 */
    public int getHmsTotal() { return hmsTotal; }

    /** 错误码列表 */
    public void setErrorCodeList(DiagnosisErrorCodeList errorCodeList) {
        this.errorCodeList = errorCodeList;
    }

    /** 物料编号 */
    public void setMaterialNum(String materialNum) {
        this.materialNum = materialNum;
    }

    /** 物料名称 */
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    /** 物料概率 */
    public void setMaterialProbability(String materialProbability) {
        this.materialProbability = materialProbability;
    }

    /** HMS 事件总数 */
    public void setHmsTotal(int hmsTotal) {
        this.hmsTotal = hmsTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiagnosisMaterialItem that = (DiagnosisMaterialItem) o;
        return Objects.equals(errorCodeList, that.errorCodeList)
                && Objects.equals(materialNum, that.materialNum)
                && Objects.equals(materialName, that.materialName)
                && Objects.equals(materialProbability, that.materialProbability)
                && hmsTotal == that.hmsTotal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                errorCodeList,
                materialNum,
                materialName,
                materialProbability,
                hmsTotal);
    }

    @Override
    public String toString() {
        return "DiagnosisMaterialItem{"
                + "errorCodeList=" + errorCodeList
                + ", materialNum='" + materialNum + '\''
                + ", materialName='" + materialName + '\''
                + ", materialProbability='" + materialProbability + '\''
                + ", hmsTotal=" + hmsTotal
                + '}';
    }
}
