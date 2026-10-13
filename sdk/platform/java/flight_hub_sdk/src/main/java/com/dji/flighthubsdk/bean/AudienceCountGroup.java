// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;


import java.util.Objects;
/** 获取在线观众数量 - 可选流分组 */
public class AudienceCountGroup {
    private String level1Group = "";
    private String level2Group = "";

    public AudienceCountGroup() {
    }


    /** 一级分组 */
    public String getLevel1Group() {
        return level1Group;
    }

    /** @param level1Group 一级分组 */
    public void setLevel1Group(String level1Group) {
        this.level1Group = level1Group != null ? level1Group : "";
    }

    /** 二级分组 */
    public String getLevel2Group() {
        return level2Group;
    }

    /** @param level2Group 二级分组 */
    public void setLevel2Group(String level2Group) {
        this.level2Group = level2Group != null ? level2Group : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudienceCountGroup that = (AudienceCountGroup) o;
        return Objects.equals(level1Group, that.level1Group)
                && Objects.equals(level2Group, that.level2Group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level1Group, level2Group);
    }

    @Override
    public String toString() {
        return "AudienceCountGroup{level1Group='" + level1Group + '\'' + ", level2Group='" + level2Group + '\'' + '}';
    }
}
