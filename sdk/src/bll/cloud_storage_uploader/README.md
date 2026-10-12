# 云存储上传器模块

本模块提供了云存储文件上传的抽象接口和具体实现，支持多种云存储提供商。

## 架构设计

### 抽象层
- `ICloudStorageUploader`: 云存储上传器抽象接口
- `CloudStorageUploadResult`: 上传结果封装
- `CloudStorageProgressCallback`: 进度回调函数类型

### 实现层
- `AwsS3Uploader`: AWS S3 存储实现
- `CloudStorageFactory`: 云存储上传器工厂类

## 支持的云存储提供商

- AWS S3
- MinIO (S3兼容)
- 未来可扩展：阿里云OSS、腾讯云COS等

## 使用方法

```cpp
// 1. 获取STS凭证
STSInfo sts_info;
sts_info.bucket = "my-bucket";
sts_info.endpoint = "https://s3.amazonaws.com";
sts_info.region = "us-east-1";
sts_info.provider = "aws";
sts_info.credentials.access_key_id = "your-access-key";
sts_info.credentials.access_key_secret = "your-secret-key";
sts_info.credentials.security_token = "your-session-token";

// 2. 创建上传器
auto uploader = CloudStorageFactory::createUploader("aws");

// 3. 上传文件
uploader->uploadFileAsync(
    "/path/to/local/file.mp3",
    "audio/file.mp3",
    sts_info,
    [](const CloudStorageUploadResult& result) {
        if (result.success) {
            std::cout << "上传成功: " << result.object_url << std::endl;
        } else {
            std::cout << "上传失败: " << result.error_message << std::endl;
        }
    }
);
```

## 编译配置

在 CMake 中启用 AWS SDK 支持：

```cmake
set(USE_AWS_SDK ON)
```

## 错误码

- `FILE_NOT_FOUND`: 文件不存在
- `FILE_TOO_LARGE`: 文件过大（超过10MB限制）
- `UNSUPPORTED_PROVIDER`: 不支持的云存储提供商
- `UPLOAD_FAILED`: 上传失败

## 特性

- 异步上传支持
- 进度回调
- 错误处理
- 多提供商支持
- 条件编译（可选择性启用AWS SDK）
