// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.bean;

import java.util.ArrayList;
import java.util.List;

/** AI Inside 算法完整详情（上传入库后返回） */
public class AIInsideAlgorithmDetail {
    /** DB 自增主键 */
    private int id;
    /** 创建时间（RFC3339 格式） */
    private String createdAt = "";
    /** 最后更新时间（RFC3339 格式） */
    private String updatedAt = "";
    /** 算法签名 ID，全局唯一标识 */
    private String signedId = "";
    /** 算法签名名称（英文标识符） */
    private String signedName = "";
    /** 算法版本号，同 signed_id 下版本递增 */
    private int signedVersion;
    /** 算法签名时间戳（Unix 秒） */
    private long signedUtc;
    /** 模型 UUID */
    private String modelUuid = "";
    /** 模型显示名称 */
    private String modelName = "";
    /** 模型类型（如 "detection"） */
    private String modelType = "";
    /** 算法可识别的目标标签列表 */
    private List<AIInsideLabel> labels = new ArrayList<>();
    /** 模型推理参数（置信度、NMS、IoU） */
    private AIInsideModelParam modelParam;
    /** 授权信息（含授权 SN 列表和有效期） */
    private AIInsideLicense license;
    /** 适用产品 ID 列表 */
    private List<Integer> applicableProduct = new ArrayList<>();
    /** MinIO 对象存储路径 */
    private String fileKey = "";
    /** 支持的图像来源（0=可见光, 1=红外） */
    private List<Integer> imageSource = new ArrayList<>();
    /** 支持的数字效果列表 */
    private List<Integer> digitalEffect = new ArrayList<>();
    /** 支持的可见光分辨率（0=1k, 1=2k） */
    private List<Integer> vlResolution = new ArrayList<>();

    public AIInsideAlgorithmDetail() {}

    /** DB 自增主键 */
    public int getId() { return id; }

    /** @param v DB 自增主键 */
    public void setId(int v) { this.id = v; }

    /** 创建时间（RFC3339 格式） */
    public String getCreatedAt() { return createdAt; }

    /** @param v 创建时间（RFC3339 格式） */
    public void setCreatedAt(String v) { this.createdAt = v != null ? v : ""; }

    /** 最后更新时间（RFC3339 格式） */
    public String getUpdatedAt() { return updatedAt; }

    /** @param v 最后更新时间（RFC3339 格式） */
    public void setUpdatedAt(String v) { this.updatedAt = v != null ? v : ""; }

    /** 算法签名 ID，全局唯一标识 */
    public String getSignedId() { return signedId; }

    /** @param v 算法签名 ID，全局唯一标识 */
    public void setSignedId(String v) { this.signedId = v != null ? v : ""; }

    /** 算法签名名称（英文标识符） */
    public String getSignedName() { return signedName; }

    /** @param v 算法签名名称（英文标识符） */
    public void setSignedName(String v) { this.signedName = v != null ? v : ""; }

    /** 算法版本号，同 signed_id 下版本递增 */
    public int getSignedVersion() { return signedVersion; }

    /** @param signedVersion 算法版本号，同 signed_id 下版本递增 */
    public void setSignedVersion(int v) { this.signedVersion = v; }

    /** 算法签名时间戳（Unix 秒） */
    public long getSignedUtc() { return signedUtc; }

    /** @param v 算法签名时间戳（Unix 秒） */
    public void setSignedUtc(long v) { this.signedUtc = v; }

    /** 模型 UUID */
    public String getModelUuid() { return modelUuid; }

    /** @param v 模型 UUID */
    public void setModelUuid(String v) { this.modelUuid = v != null ? v : ""; }

    /** 模型显示名称 */
    public String getModelName() { return modelName; }

    /** @param v 模型显示名称 */
    public void setModelName(String v) { this.modelName = v != null ? v : ""; }

    /** 模型类型（如 "detection"） */
    public String getModelType() { return modelType; }

    /** @param v 模型类型（如 "detection"） */
    public void setModelType(String v) { this.modelType = v != null ? v : ""; }

    /** 算法可识别的目标标签列表 */
    public List<AIInsideLabel> getLabels() { return labels; }

    /** @param v 算法可识别的目标标签列表 */
    public void setLabels(List<AIInsideLabel> v) { this.labels = v != null ? v : new ArrayList<>(); }

    /** 模型推理参数（置信度、NMS、IoU） */
    public AIInsideModelParam getModelParam() { return modelParam; }

    /** @param v 模型推理参数（置信度、NMS、IoU） */
    public void setModelParam(AIInsideModelParam v) { this.modelParam = v; }

    /** 授权信息（含授权 SN 列表和有效期） */
    public AIInsideLicense getLicense() { return license; }

    /** @param v 授权信息（含授权 SN 列表和有效期） */
    public void setLicense(AIInsideLicense v) { this.license = v; }

    /** 适用产品 ID 列表 */
    public List<Integer> getApplicableProduct() { return applicableProduct; }

    /** @param v 适用产品 ID 列表 */
    public void setApplicableProduct(List<Integer> v) { this.applicableProduct = v != null ? v : new ArrayList<>(); }

    /** MinIO 对象存储路径 */
    public String getFileKey() { return fileKey; }

    /** @param v MinIO 对象存储路径 */
    public void setFileKey(String v) { this.fileKey = v != null ? v : ""; }

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
