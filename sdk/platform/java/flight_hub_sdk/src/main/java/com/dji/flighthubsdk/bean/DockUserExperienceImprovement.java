// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Objects;
/** 用户体验改善计划。 */
public final class DockUserExperienceImprovement {

    private int userExperienceImprovement;
    public DockUserExperienceImprovement() {
    }

/**
     * 构造 DockUserExperienceImprovement。
     *
     * @param userExperienceImprovement 用户体验改进计划
     */
    public DockUserExperienceImprovement(int userExperienceImprovement) {
        this.userExperienceImprovement = userExperienceImprovement;
    }

    /** 用户体验改进计划 */
    public int getUserExperienceImprovement() {
        return userExperienceImprovement;
    }

    /** 用户体验改进计划 */
    public void setUserExperienceImprovement(int userExperienceImprovement) {
        this.userExperienceImprovement = userExperienceImprovement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DockUserExperienceImprovement that = (DockUserExperienceImprovement) o;
        return userExperienceImprovement == that.userExperienceImprovement;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userExperienceImprovement);
    }

    @Override
    public String toString() {
        return "DockUserExperienceImprovement{userExperienceImprovement=" + userExperienceImprovement + '}';
    }
}
