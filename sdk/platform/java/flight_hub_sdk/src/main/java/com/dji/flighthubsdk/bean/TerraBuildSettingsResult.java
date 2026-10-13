// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
/** Terra Build 获取重建设置的返回结果 */
public final class TerraBuildSettingsResult {
    private int state;
    private TerraBuildSettingsDetail reconstructionSettings;
    private String mediaInputObjectKey;
    private String sfmOutputObjectPrefix;
    private List<TerraBuildFolderInfo> folderList;
    public TerraBuildSettingsResult() {
    }

/**
     * 构造 TerraBuildSettingsResult。
     *
     * @param state 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点}
     * @param reconstructionSettings 重建设置
     * @param mediaInputObjectKey 媒体输入对象存储 Key
     * @param sfmOutputObjectPrefix SFM 输出对象存储前缀
     * @param folderList 文件夹列表
     */
    public TerraBuildSettingsResult(
            int state,
            TerraBuildSettingsDetail reconstructionSettings,
            String mediaInputObjectKey,
            String sfmOutputObjectPrefix,
            List<TerraBuildFolderInfo> folderList) {
        this.state = state;
        this.reconstructionSettings = reconstructionSettings;
        this.mediaInputObjectKey = mediaInputObjectKey != null ? mediaInputObjectKey : "";
        this.sfmOutputObjectPrefix = sfmOutputObjectPrefix != null ? sfmOutputObjectPrefix : "";
        this.folderList = folderList != null ? folderList : Collections.emptyList();
    }

    /** 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点} */
    public int getState() { return state; }
    /** 重建设置 */
    public TerraBuildSettingsDetail getReconstructionSettings() { return reconstructionSettings; }
    /** 媒体输入对象存储 Key */
    public String getMediaInputObjectKey() { return mediaInputObjectKey; }
    /** SFM 输出对象存储前缀 */
    public String getSfmOutputObjectPrefix() { return sfmOutputObjectPrefix; }
    /** 文件夹列表 */
    public List<TerraBuildFolderInfo> getFolderList() { return folderList; }

    /** 建图文件状态。{0:未知, 1:模型文件解压中, 2:模型文件解压成功, 3:模型文件解压失败, 4:选择坐标系, 5:格式转换中, 6:格式转换成功, 7:格式转换失败, 8:建图失败, 9:模型导入成功, 10:格式校验中, 11:格式校验失败, 12:等待Terra Cloud资源中, 13:等待资源失败, 14:重建中, 15:重建成功, 16:重建取消, 17:等待图片上传, 119:空三运行失败, 120:空三运行中, 121:空三等待资源中, 122:空三等待资源失败, 123:空三被终止或取消, 124:空三完成DMV解析中, 125:DMV解析失败, 126:DMV解析成功等待刺点} */
    public void setState(int state) {
        this.state = state;
    }

    /** 重建设置 */
    public void setReconstructionSettings(TerraBuildSettingsDetail reconstructionSettings) {
        this.reconstructionSettings = reconstructionSettings;
    }

    /** 媒体输入对象存储 Key */
    public void setMediaInputObjectKey(String mediaInputObjectKey) {
        this.mediaInputObjectKey = mediaInputObjectKey;
    }

    /** SFM 输出对象存储前缀 */
    public void setSfmOutputObjectPrefix(String sfmOutputObjectPrefix) {
        this.sfmOutputObjectPrefix = sfmOutputObjectPrefix;
    }

    /** 文件夹列表 */
    public void setFolderList(List<TerraBuildFolderInfo> folderList) {
        this.folderList = folderList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerraBuildSettingsResult that = (TerraBuildSettingsResult) o;
        return state == that.state
                && Objects.equals(reconstructionSettings, that.reconstructionSettings)
                && Objects.equals(mediaInputObjectKey, that.mediaInputObjectKey)
                && Objects.equals(sfmOutputObjectPrefix, that.sfmOutputObjectPrefix)
                && Objects.equals(folderList, that.folderList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                state,
                reconstructionSettings,
                mediaInputObjectKey,
                sfmOutputObjectPrefix,
                folderList);
    }

    @Override
    public String toString() {
        return "TerraBuildSettingsResult{"
                + "state=" + state
                + ", reconstructionSettings=" + reconstructionSettings
                + ", mediaInputObjectKey='" + mediaInputObjectKey + '\''
                + ", sfmOutputObjectPrefix='" + sfmOutputObjectPrefix + '\''
                + ", folderList=" + folderList
                + '}';
    }
}
