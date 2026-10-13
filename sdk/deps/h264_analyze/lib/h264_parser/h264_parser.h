// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#ifndef H264_PARSER_H
#define H264_PARSER_H

#include <vector>
#include <string>
#include <cstdint>

struct SEISubPayload {
    uint16_t payload_type;      // 子载荷类型 (2字节)
    uint16_t payload_length;    // 子载荷长度 (2字节)
    std::vector<uint8_t> payload_data; // 子载荷数据
};

struct SEIFrame {
    uint32_t offset;        // SEI帧在文件中的偏移位置
    uint32_t size;          // SEI帧的大小（字节）
    uint8_t sei_payload_type;   // H.264标准SEI载荷类型 (0xf5 for DJI)
    std::vector<SEISubPayload> sub_payloads; // 子载荷数组
};

struct NALU {
    uint32_t offset;        // NALU在文件中的偏移位置
    uint32_t size;          // NALU的大小（字节）
    uint8_t nalu_type;      // NALU类型
    std::vector<uint8_t> data; // NALU数据（包含起始码）
};

struct VideoFrame {
    uint32_t offset;        // 帧在文件中的偏移位置
    uint32_t size;          // 帧的大小（字节）
    uint8_t frame_type;     // 帧类型：'I'=I帧, 'P'=P帧, 'B'=B帧
    bool is_key_frame;      // 是否为关键帧
    uint32_t nalu_count;    // 该帧包含的NALU数量
};

class H264Parser {
public:
    H264Parser();
    ~H264Parser();
    
    // 解析H264文件并提取SEI帧
    bool parseH264Stream(const std::string& filename);
    
    // 获取所有找到的SEI帧
    const std::vector<SEIFrame>& getSEIFrames() const;
    
    // 获取所有找到的视频帧
    const std::vector<VideoFrame>& getVideoFrames() const;
    
    // 打印SEI帧信息
    void printSEIFrames() const;
    
    // 打印视频帧信息
    void printVideoFrames() const;
    
    // 去除H264码流中的SEI帧并保存到文件
    bool removeSEIAndSave(const std::string& input_filename, const std::string& output_filename);
    
    // 拆分H264码流为所有NALU（包含SEI等）
    std::vector<NALU> splitNALUs(const std::vector<uint8_t>& h264_data);
    
    // 拆分H264码流为所有NALU（从文件）
    std::vector<NALU> splitNALUsFromFile(const std::string& filename);

    // 辅助函数
    static bool is_key_frame(const uint8_t *data, const uint32_t& length);
    static char getFrameType(uint8_t nalu_type, bool is_key_frame);

private:
    std::vector<SEIFrame> sei_frames_;
    std::vector<VideoFrame> video_frames_;
    
    // 解析视频帧大小
    void parseVideoFrames(const std::vector<uint8_t>& file_data, size_t file_size);
};

#endif // H264_PARSER_H
