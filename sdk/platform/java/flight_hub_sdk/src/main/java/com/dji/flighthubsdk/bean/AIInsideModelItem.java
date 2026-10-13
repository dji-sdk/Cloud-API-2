// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** AI Inside 算法模型条目 */
public class AIInsideModelItem {
    /** 算法签名 ID，全局唯一标识 */
    private String signedId = "";
    /** 算法签名名称（英文标识符，如 "fire_detection"） */
    private String signedName = "";
    /** 算法显示名称（可读名称，如 "Fire Detection"） */
    private String modelName = "";
    /** 算法版本号，同 signed_id 下版本递增 */
    private int signedVersion;
    /** 算法签名时间戳（Unix 秒） */
    private long signedUtc;
    /** 算法过期时间戳（Unix 秒） */
    private long expiredUtc;
    /** 模型推理参数（置信度、NMS、IoU） */
    private AIInsideModelParam modelParam;
    /** 算法可识别的目标标签列表 */
    private List<AIInsideLabel> labels = new ArrayList<>();
    /** 绑定的飞机 SN 列表（同 license.sn_list） */
    private List<String> snList = new ArrayList<>();
    /** 支持的图像来源（0=可见光, 1=红外） */
    private List<Integer> imageSource = new ArrayList<>();
    /** 支持的数字效果列表 */
    private List<Integer> digitalEffect = new ArrayList<>();
    /** 支持的可见光分辨率（0=1k, 1=2k） */
    private List<Integer> vlResolution = new ArrayList<>();

    public AIInsideModelItem() {}

    /** 算法签名 ID，全局唯一标识 */
    public String getSignedId() { return signedId; }

    /** @param v 算法签名 ID，全局唯一标识 */
    public void setSignedId(String v) { this.signedId = v != null ? v : ""; }

    /** 算法签名名称（英文标识符，如 "fire_detection"） */
    public String getSignedName() { return signedName; }

    /** @param v 算法签名名称（英文标识符，如 "fire_detection"） */
    public void setSignedName(String v) { this.signedName = v != null ? v : ""; }

    /** 算法显示名称（可读名称，如 "Fire Detection"） */
    public String getModelName() { return modelName; }

    /** @param v 算法显示名称（可读名称，如 "Fire Detection"） */
    public void setModelName(String v) { this.modelName = v != null ? v : ""; }

    /** 算法版本号，同 signed_id 下版本递增 */
    public int getSignedVersion() { return signedVersion; }

    /** @param signedVersion 算法版本号，同 signed_id 下版本递增 */
    public void setSignedVersion(int v) { this.signedVersion = v; }

    /** 算法签名时间戳（Unix 秒） */
    public long getSignedUtc() { return signedUtc; }

    /** @param v 算法签名时间戳（Unix 秒） */
    public void setSignedUtc(long v) { this.signedUtc = v; }

    /** 算法过期时间戳（Unix 秒） */
    public long getExpiredUtc() { return expiredUtc; }

    /** @param v 算法过期时间戳（Unix 秒） */
    public void setExpiredUtc(long v) { this.expiredUtc = v; }

    /** 模型推理参数（置信度、NMS、IoU） */
    public AIInsideModelParam getModelParam() { return modelParam; }

    /** @param v 模型推理参数（置信度、NMS、IoU） */
    public void setModelParam(AIInsideModelParam v) { this.modelParam = v; }

    /** 算法可识别的目标标签列表 */
    public List<AIInsideLabel> getLabels() { return labels; }

    /** @param v 算法可识别的目标标签列表 */
    public void setLabels(List<AIInsideLabel> v) { this.labels = v != null ? v : new ArrayList<>(); }

    /** 绑定的飞机 SN 列表（同 license.sn_list） */
    public List<String> getSnList() { return snList; }

    /** @param snList 绑定的飞机 SN 列表（同 license.sn_list） */
    public void setSnList(List<String> v) { this.snList = v != null ? v : new ArrayList<>(); }

    /** 支持的图像来源（0=可见光, 1=红外） */
    public List<Integer> getImageSource() { return imageSource; }

    /** @param v 支持的图像来源（0=可见光, 1=红外） */
    public void setImageSource(List<Integer> v) { this.imageSource = v != null ? v : new ArrayList<>(); }

    /** 支持的数字效果列表 */
    public List<Integer> getDigitalEffect() { return digitalEffect; }

    /** @param v 支持的数字效果列表 */
    public void setDigitalEffect(List<Integer> v) { this.digitalEffect = v != null ? v : new ArrayList<>(); }

    /** 支持的可见光分辨率（0=1k, 1=2k） */
    public List<Integer> getVlResolution() { return vlResolution; }

    /** @param v 支持的可见光分辨率（0=1k, 1=2k） */
    public void setVlResolution(List<Integer> v) { this.vlResolution = v != null ? v : new ArrayList<>(); }
}
