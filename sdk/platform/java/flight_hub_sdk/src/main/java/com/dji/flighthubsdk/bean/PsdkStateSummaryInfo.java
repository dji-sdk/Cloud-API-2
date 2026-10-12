// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import com.dji.flighthubsdk.value.PsdkSubType;
import com.dji.flighthubsdk.value.PsdkType;

import java.util.Objects;
/** PSDK 状态摘要。 */
public final class PsdkStateSummaryInfo {

    private int psdkIndex;
    /** 取值语义同 `flight_hub_sdk::PsdkType`（见 `value.h`） */
    private PsdkType psdkType;
    /** 取值语义同 `flight_hub_sdk::PsdkSubType`（见 `value.h`） */
    private PsdkSubType psdkSubType;
    private String psdkName;
    private String psdkSn;
    private String psdkVersion;
    private String psdkLibVersion;
    /** 默认无效状态; 0:限制功能 1:不限制 2:部分限制 */
    private int psdkRestrictState;
    public PsdkStateSummaryInfo() {
    }

/**
     * 构造 PsdkStateSummaryInfo。
     *
     * @param psdkIndex PSDK索引
     * @param psdkType PSDK类型
     * @param psdkSubType PSDK子类型
     * @param psdkName PSDK名称
     * @param psdkSn PSDK序列号
     * @param psdkVersion PSDK版本
     * @param psdkLibVersion PSDK库版本
     * @param psdkRestrictState PSDK限制状态，0:限制 1:不限制 2:部分限制
     */
    public PsdkStateSummaryInfo(
            int psdkIndex,
            PsdkType psdkType,
            PsdkSubType psdkSubType,
            String psdkName,
            String psdkSn,
            String psdkVersion,
            String psdkLibVersion,
            int psdkRestrictState) {
        this.psdkIndex = psdkIndex;
        this.psdkType = psdkType != null ? psdkType : PsdkType.UNKNOWN;
        this.psdkSubType = psdkSubType != null ? psdkSubType : PsdkSubType.UNKNOWN;
        this.psdkName = psdkName != null ? psdkName : "";
        this.psdkSn = psdkSn != null ? psdkSn : "";
        this.psdkVersion = psdkVersion != null ? psdkVersion : "";
        this.psdkLibVersion = psdkLibVersion != null ? psdkLibVersion : "";
        this.psdkRestrictState = psdkRestrictState;
    }

    /** PSDK索引 */
    public int getPsdkIndex() { return psdkIndex; }
    /** 取值语义同 `flight_hub_sdk::PsdkType`（见 `value.h`） */
    public PsdkType getPsdkType() { return psdkType; }
    /** 取值语义同 `flight_hub_sdk::PsdkSubType`（见 `value.h`） */
    public PsdkSubType getPsdkSubType() { return psdkSubType; }
    /** PSDK名称 */
    public String getPsdkName() { return psdkName; }
    /** PSDK序列号 */
    public String getPsdkSn() { return psdkSn; }
    /** PSDK版本 */
    public String getPsdkVersion() { return psdkVersion; }
    /** PSDK库版本 */
    public String getPsdkLibVersion() { return psdkLibVersion; }
    /** 默认无效状态; 0:限制功能 1:不限制 2:部分限制 */
    public int getPsdkRestrictState() { return psdkRestrictState; }

    /** PSDK索引 */
    public void setPsdkIndex(int psdkIndex) {
        this.psdkIndex = psdkIndex;
    }

    /** @param psdkType 取值语义同 `flight_hub_sdk::PsdkType`（见 `value.h`） */
    public void setPsdkType(PsdkType psdkType) {
        this.psdkType = psdkType;
    }

    /** @param psdkSubType 取值语义同 `flight_hub_sdk::PsdkSubType`（见 `value.h`） */
    public void setPsdkSubType(PsdkSubType psdkSubType) {
        this.psdkSubType = psdkSubType;
    }

    /** PSDK名称 */
    public void setPsdkName(String psdkName) {
        this.psdkName = psdkName;
    }

    /** PSDK序列号 */
    public void setPsdkSn(String psdkSn) {
        this.psdkSn = psdkSn;
    }

    /** PSDK版本 */
    public void setPsdkVersion(String psdkVersion) {
        this.psdkVersion = psdkVersion;
    }

    /** PSDK库版本 */
    public void setPsdkLibVersion(String psdkLibVersion) {
        this.psdkLibVersion = psdkLibVersion;
    }

    /** @param psdkRestrictState 默认无效状态; 0:限制功能 1:不限制 2:部分限制 */
    public void setPsdkRestrictState(int psdkRestrictState) {
        this.psdkRestrictState = psdkRestrictState;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PsdkStateSummaryInfo that = (PsdkStateSummaryInfo) o;
        return psdkIndex == that.psdkIndex
                && psdkRestrictState == that.psdkRestrictState
                && psdkType == that.psdkType
                && psdkSubType == that.psdkSubType
                && Objects.equals(psdkName, that.psdkName)
                && Objects.equals(psdkSn, that.psdkSn)
                && Objects.equals(psdkVersion, that.psdkVersion)
                && Objects.equals(psdkLibVersion, that.psdkLibVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                psdkIndex, psdkType, psdkSubType, psdkName, psdkSn, psdkVersion, psdkLibVersion, psdkRestrictState);
    }

    @Override
    public String toString() {
        return "PsdkStateSummaryInfo{"
                + "psdkIndex=" + psdkIndex
                + ", psdkType=" + psdkType
                + ", psdkSubType=" + psdkSubType
                + ", psdkName='" + psdkName + '\''
                + ", psdkSn='" + psdkSn + '\''
                + ", psdkVersion='" + psdkVersion + '\''
                + ", psdkLibVersion='" + psdkLibVersion + '\''
                + ", psdkRestrictState=" + psdkRestrictState
                + '}';
    }
}
