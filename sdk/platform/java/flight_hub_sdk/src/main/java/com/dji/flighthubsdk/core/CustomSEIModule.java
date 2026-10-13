// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.generated.ICustomSEIModule;
import com.dji.flighthubsdk.generated.IntVector;
import com.dji.flighthubsdk.generated.SeiItemVector;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * 自定义 SEI（Supplemental Enhancement Information）处理模块，
 */
public class CustomSEIModule {

    private ICustomSEIModule customSeiModule;

    /**
     * 构造 CustomSEIModule
     * @param customSeiModule 底层 SEI 模块接口
     */
    public CustomSEIModule(ICustomSEIModule customSeiModule) {
        this.customSeiModule = customSeiModule;
    }

    /**
     * 将自定义 SEI 数据编码到 H.264 帧中
     * @param frameData 原始 H.264 帧数据
     * @param customSeiData 待编码的 SEI 数据项列表
     * @return 编码后的帧数据（含 SEI）；失败时返回空列表
     * @implNote 链路: SDK 内部 | 在视频帧 NAL 单元前插入 SEI NAL
     */
    public java.util.List<Integer> customSEIEncode(java.util.List<Integer> frameData, java.util.List<com.dji.flighthubsdk.bean.SeiItem> customSeiData) {
        if (customSeiModule == null) {
            Logger.warn("CustomSEIModule", "CustomSEI module is null");
            return new java.util.ArrayList<>();
        }

        if (frameData == null || customSeiData == null) {
            Logger.warn("CustomSEIModule", "Input data is null");
            return new java.util.ArrayList<>();
        }

        SeiItemVector swigSeiData = null;
        IntVector swigFrameData = null;
        IntVector outputFrameData = null;
        try {
            swigFrameData = new IntVector();
            for (Integer value : frameData) {
                swigFrameData.add(value);
            }

            swigSeiData = FlightHubSdkConvert.toNativeSeiItemVector(customSeiData);

            outputFrameData = new IntVector();

            boolean success = customSeiModule.CustomSEIEncodeWrapper(swigFrameData, swigSeiData, outputFrameData);

            if (!success) {
                Logger.warn("CustomSEIModule", "CustomSEIEncode failed");
                return new java.util.ArrayList<>();
            }

            java.util.List<Integer> result = new java.util.ArrayList<>();
            for (int i = 0; i < outputFrameData.size(); i++) {
                result.add(outputFrameData.get(i));
            }

            return result;
        } catch (Exception e) {
            Logger.error("CustomSEIModule", "CustomSEIEncode error: " + e.getMessage(), e);
            return new java.util.ArrayList<>();
        } finally {
            if (swigSeiData != null) {
                swigSeiData.delete();
            }
            if (swigFrameData != null) {
                swigFrameData.delete();
            }
            if (outputFrameData != null) {
                outputFrameData.delete();
            }
        }
    }

    /**
     * 从 H.264 帧中解码出自定义 SEI 数据
     * @param frameData H.264 帧数据（含 SEI）
     * @return 解码结果（含 SEI 头部与数据项列表）；失败时返回 null
     * @implNote 链路: SDK 内部 | 解析帧中的 SEI NAL 单元
     */
    public CustomSEIDecodeResult customSEIDecode(java.util.List<Integer> frameData) {
        if (customSeiModule == null) {
            Logger.warn("CustomSEIModule", "CustomSEI module is null");
            return null;
        }

        if (frameData == null) {
            Logger.warn("CustomSEIModule", "Input frame data is null");
            return null;
        }

        try {
            IntVector swigFrameData = new IntVector();
            for (Integer value : frameData) {
                swigFrameData.add(value);
            }

            com.dji.flighthubsdk.generated.SeiHeader nativeHeader = new com.dji.flighthubsdk.generated.SeiHeader();
            SeiItemVector nativeSeiData = new SeiItemVector();

            boolean success = customSeiModule.CustomSEIDecodeWrapper(swigFrameData, nativeHeader, nativeSeiData);

            if (!success) {
                Logger.warn("CustomSEIModule", "CustomSEIDecode failed");
                return null;
            }

            com.dji.flighthubsdk.bean.SeiHeader beanHeader = FlightHubSdkConvert.toSeiHeader(nativeHeader);
            java.util.List<com.dji.flighthubsdk.bean.SeiItem> beanSeiData = FlightHubSdkConvert.toSeiItemList(nativeSeiData);

            return new CustomSEIDecodeResult(beanHeader, beanSeiData);
        } catch (Exception e) {
            Logger.error("CustomSEIModule", "CustomSEIDecode error: " + e.getMessage(), e);
            return null;
        }
    }

    /** SEI 解码结果，包含头部信息和数据项列表 */
    public static class CustomSEIDecodeResult {
        private final com.dji.flighthubsdk.bean.SeiHeader header;
        private final java.util.List<com.dji.flighthubsdk.bean.SeiItem> seiData;

        /**
         * 构造 SEI 解码结果
         * @param header SEI 头部信息
         * @param seiData SEI 数据项列表
         */
        public CustomSEIDecodeResult(com.dji.flighthubsdk.bean.SeiHeader header, java.util.List<com.dji.flighthubsdk.bean.SeiItem> seiData) {
            this.header = header;
            this.seiData = seiData;
        }

        /**
         * 获取 SEI 头部信息
         * @return SEI 头部信息
         */
        public com.dji.flighthubsdk.bean.SeiHeader getHeader() {
            return header;
        }

        /**
         * 获取 SEI 数据项列表
         * @return SEI 数据项列表
         */
        public java.util.List<com.dji.flighthubsdk.bean.SeiItem> getSeiData() {
            return seiData;
        }
    }
}
