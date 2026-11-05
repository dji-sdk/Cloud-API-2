// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 模型删除结果（含被权限锁定的文件） */
public final class TerraDeleteModelFilesResult {
    /** 是否存在被锁定文件 */
    private boolean hasLockFiles;
    /** 被锁定文件的原始 JSON（每个元素一个文件对象） */
    private List<String> lockFiles = new ArrayList<>();

    public TerraDeleteModelFilesResult() {
    }

    public boolean isHasLockFiles() { return hasLockFiles; }

    public void setHasLockFiles(boolean hasLockFiles) { this.hasLockFiles = hasLockFiles; }

    public List<String> getLockFiles() { return lockFiles; }

    public void setLockFiles(List<String> lockFiles) {
        this.lockFiles = lockFiles != null ? lockFiles : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerraDeleteModelFilesResult that = (TerraDeleteModelFilesResult) o;
        return hasLockFiles == that.hasLockFiles && Objects.equals(lockFiles, that.lockFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasLockFiles, lockFiles);
    }

    @Override
    public String toString() {
        return "TerraDeleteModelFilesResult{hasLockFiles=" + hasLockFiles
                + ", lockFiles=" + lockFiles + '}';
    }
}
