// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>
#include <string>
#include <vector>

namespace flight_hub_sdk {

// === 媒体上传相关公开结构体 ===

/// 媒体上传 STS 临时凭证
struct MediaUploadStsCredentials {
    std::string access_key_id;      ///< STS 临时 AccessKeyId
    std::string access_key_secret;  ///< STS 临时 AccessKeySecret
    int expire = 0;                 ///< 凭证有效期（秒）
    std::string security_token;     ///< STS SecurityToken
};

/// 获取媒体上传 STS 凭证结果
struct GetMediaUploadStsResult {
    std::string bucket;                     ///< OSS 存储桶名称
    MediaUploadStsCredentials credentials;  ///< STS 临时凭证
    std::string object_key_prefix;          ///< 上传路径前缀
    std::string endpoint;                   ///< OSS 访问端点
    std::string provider;                   ///< 存储提供商（ali/aws/minio 等）
    std::string region;                     ///< 存储区域
};

/// 媒体上传完成回调结果
struct MediaUploadCallbackResult {
    std::string object_key;     ///< 文件 OSS Key
    std::string filename;       ///< 文件名
    std::string flight_id;      ///< 航线任务 ID
    std::string uuid;           ///< 文件 UUID（files 表）
    int id = 0;                 ///< 文件 ID（files 表主键）
    int media_id = 0;           ///< 媒体文件 ID（media_files 表主键）
};

/// 获取上传凭证参数
struct GetMediaUploadStsParams {
    std::string file_path;      ///< 上传目标路径前缀（OSS Object Key 前缀），必填
    int expire = 3600;          ///< 凭证有效期（秒），最大 3600
    std::string sn;             ///< 设备序列号（可选）
    int module = 0;             ///< 业务模块标识（可选）。枚举：2=飞行记录STS, 2001=SN加密, 其他(含0)=默认设备STS
};

/// 上传回调文件扩展信息
struct MediaUploadCallbackFileExt {
    std::string prj_uuid;                   ///< 关联项目 UUID
    std::string workspace_id;               ///< 工作空间 ID
    std::string flight_id;                  ///< 航线任务 ID
    std::string drone_model_key;            ///< 飞行器型号 Key
    std::string payload_model_key;          ///< 负载型号 Key
    bool is_original = true;                ///< 是否原片
    std::string user_id;                    ///< 上传用户 ID
    std::string wayline_id;                 ///< 航线 ID
    std::string tinny_fingerprint;          ///< 短指纹
    int fast_reconstruction_media_type = 0; ///< 快速重建媒体类型
    long long media_index = 0;               ///< 媒体索引
};

/// 上传回调拍摄位置
struct MediaUploadCallbackShootPosition {
    double lat = 0.0;   ///< 纬度
    double lng = 0.0;   ///< 经度
};

/// 上传回调文件元数据（图像拍摄信息）
struct MediaUploadCallbackMetadata {
    double absolute_altitude = 0.0;     ///< 绝对高度（米）
    double relative_altitude = 0.0;     ///< 相对高度（米）
    double gimbal_yaw_degree = 0.0;     ///< 云台偏航角
    std::string created_time;           ///< 拍摄时间
    MediaUploadCallbackShootPosition shoot_position; ///< 拍摄位置
};

/// 上传回调文件信息
struct MediaUploadCallbackFile {
    std::string object_key;                     ///< 文件在 OSS 上的完整 Key，必填
    std::string fingerprint;                    ///< 文件指纹（MD5）
    std::string name;                           ///< 文件名，必填
    int type = 0;                               ///< 文件类型; 取值参见 MediaType 枚举
    std::string cloud_to_cloud_id;              ///< 云端传输标识
    MediaUploadCallbackFileExt ext;             ///< 扩展信息
    MediaUploadCallbackMetadata metadata;       ///< 图像元数据
    std::string created_time;                  ///< 文件创建时间
    std::string source;                        ///< 上传来源（web/device）
};

/// 上传完成回调参数
struct MediaUploadCallbackParams {
    std::string sn;                 ///< 设备序列号（可选）
    int result = 0;                 ///< 上传结果（0=成功）
    int progress = 0;               ///< 上传进度
    MediaUploadCallbackFile file;   ///< 文件信息（必填）
    int expected_file_count = 0;    ///< 预期文件总数
    int uploaded_file_count = 0;    ///< 已上传文件数
    int flight_type = 0;            ///< 飞行类型
    std::string biz_code;           ///< 业务码（如 "file_upload_callback"）
    std::string version;            ///< 版本号
};

/// 媒体服务模块接口，提供媒体文件查询、删除、下载、上传凭证获取、上传回调等功能。
class IMediaServiceModule {
public:
    virtual ~IMediaServiceModule() = default;

    /// @brief 获取媒体文件列表
    /// @param request_params 查询参数（设备 SN、分页、文件类型等）
    /// @param callback 结果回调，成功时返回媒体文件列表
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-media/api/v1/media-files/list
    /// @endinternal
    virtual void getMediaFilesList(
        const GetMediaFilesListParams &request_params,
        const std::function<void(FlightHubError err, const GetMediaFilesListResults& results)>& callback) = 0;

    /// @brief 批量删除媒体文件
    /// @param file_ids 文件 id 列表
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-media/api/v1/media-files/batch-delete
    /// @endinternal
    virtual void deleteMediaFiles(
        const std::vector<long long> &file_ids,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 获取媒体文件下载地址
    /// @param file_id 文件 id
    /// @param callback 结果回调，成功时返回下载地址
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-media/api/v1/media-files/url
    /// @endinternal
    virtual void getMediaFilesDownloadURL(
        long long file_id,
        const std::function<void(FlightHubError err, const GetMediaFilesDownloadUrlResults& results)>& callback) = 0;


    /// @brief 获取媒体文件下载地址
    /// @param object_keys 存储对象key列表
    /// @param callback 结果回调, 成功时返回下载地址 url
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-media/api/v1/media-files/url
    /// @endinternal
    virtual void getMediaFilesDownloadURL(
        const std::vector<std::string> & object_keys,
        const std::function<void(FlightHubError err, const GetMediaFilesDownloadUrlListResults& results)>& callback) = 0;



    /// @brief 监听设备媒体后处理完成通知
    /// @param device_sn 设备序列号
    /// @param callback 消息回调
    /// @return 监听 ID，用于取消监听
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_media_file_post_process
    /// @endinternal
    virtual int listenMediaFilePostProcessMessage(
        const std::string &device_sn,
        const std::function<void(const MediaFilePostProcess& msg)>& callback) = 0;

    /// @brief 取消监听设备媒体后处理完成通知
    /// @param listen_id 监听 ID
    virtual void cancelListenMediaFilePostProcessMessage(int listen_id) = 0;

    /// @brief 监听设备媒体回传通知
    /// @param device_sn 设备序列号
    /// @param callback 消息回调
    /// @return 监听 ID，用于取消监听
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_media_file_device_upload
    /// @endinternal
    virtual int listenMediaFileDeviceUploadMessage(
        const std::string &device_sn,
        const std::function<void(const MediaFileDeviceUpload& msg)>& callback) = 0;

    /// @brief 取消监听设备媒体回传通知
    /// @param listen_id 监听 ID
    virtual void cancelListenMediaFileDeviceUploadMessage(int listen_id) = 0;

    /// @brief 获取 OSS STS 临时上传凭证
    /// @param params 上传凭证请求参数
    /// @param callback 结果回调，成功时返回 STS 凭证信息
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-media/inner/v1/media-files/sts
    /// @endinternal
    virtual void getMediaUploadSts(
        const GetMediaUploadStsParams& params,
        const std::function<void(FlightHubError err, const GetMediaUploadStsResult& result)>& callback) = 0;

    /// @brief 媒体文件上传完成后通知服务端
    /// @param params 上传回调参数
    /// @param callback 结果回调，成功时返回文件信息
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-media/api/v1/upload-callback
    /// @endinternal
    virtual void mediaUploadCallback(
        const MediaUploadCallbackParams& params,
        const std::function<void(FlightHubError err, const MediaUploadCallbackResult& result)>& callback) = 0;

    /// @brief 上传地理标注媒体资源（图片备注上传回调）
    /// @param params 上传参数（name、object_key 必填）
    /// @param callback 结果回调，成功时返回 file_id/file_uuid/media_file_id
    /// @internal
    /// 链路: HTTP | POST /pf-media/api/v1/geo-media-file
    /// @endinternal
    virtual void uploadGeoMediaFile(
        const UploadGeoMediaFileParams& params,
        const std::function<void(FlightHubError err, const UploadGeoMediaFileResult& result)>& callback) = 0;

    /// @brief 根据文件 ID 获取媒体文件详情（轮询文件 URL）
    /// @param file_id 文件 ID（files 表主键），必填
    /// @param callback 结果回调，成功时返回文件 URL、缩略图 URL 等信息
    /// @internal
    /// 链路: HTTP | GET /pf-media/api/v1/media-files/{file_id}
    /// @endinternal
    virtual void getMediaFileDetailById(
        int file_id,
        const std::function<void(FlightHubError err, const GetMediaFileDetailByIdResult& result)>& callback) = 0;

};  // End of IMediaServiceModule;


}  // namespace flight_hub_sdk;
