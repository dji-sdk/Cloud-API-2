/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.fh.kernel.realsdk;

import com.dji.fh.kernel.spi.query.DeviceQueryService;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 真 SDK 实现的设备列表查询服务。
 *
 * <p>每次调用 {@link #listDevices()} 都通过 {@link SdkInvoker#getDeviceList} 实时查询 SDK，
 * 用 {@link CountDownLatch} 桥接异步回调为同步返回。超时返回空列表，不抛异常。
 *
 * @author DJI DevKit Team
 */
public class RealDeviceQueryService implements DeviceQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(RealDeviceQueryService.class);

    private final SdkInvoker sdkInvoker;
    private final RealSdkProperties props;

    public RealDeviceQueryService(SdkInvoker sdkInvoker, RealSdkProperties props) {
        this.sdkInvoker = sdkInvoker;
        this.props = props;
    }

    @Override
    public List<DeviceSnapshot> listDevices() {
        if (!sdkInvoker.isInitialized()) {
            LOG.warn("SDK 未初始化，返回空设备列表");
            return Collections.emptyList();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<DeviceSnapshot>> ref = new AtomicReference<>(Collections.emptyList());

        try {
            sdkInvoker.getDeviceList((deviceList, error) -> {
                try {
                    if (error != null && !error.isSuccess()) {
                        LOG.warn("getDeviceList SDK 错误：errorCode={}", error.getErrorCode());
                        return;
                    }
                    if (deviceList == null) {
                        return;
                    }
                    ref.set(convertToSnapshots(deviceList));
                } finally {
                    latch.countDown();
                }
            });
        } catch (Exception ex) {
            LOG.warn("getDeviceList 调用异常：{}", ex.getMessage());
            return Collections.emptyList();
        }

        try {
            boolean ok = latch.await(props.deviceListTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!ok) {
                LOG.warn("getDeviceList 超时（{}ms）", props.deviceListTimeout().toMillis());
                return Collections.emptyList();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.warn("getDeviceList 等待被中断");
            return Collections.emptyList();
        }

        return ref.get();
    }

    /**
     * SDK DeviceTopo 列表转为 SPI DeviceSnapshot 列表（扁平 + 保留拓扑）。
     *
     * <p>TODO: 此逻辑与 {@link RealSdkStarter#pullInitialDeviceList()} 中的 DeviceTopo 遍历模式重复，
     * 后续考虑统一为共享转换方法。当前两处输出类型不同（DeviceSnapshot vs DeviceInfo），暂不合并。
     *
     * @param topoList SDK 返回的设备拓扑列表
     * @return 扁平化的设备快照列表（主设备 + 子设备各自独立出现）
     */
    private List<DeviceSnapshot> convertToSnapshots(List<DeviceTopo> topoList) {
        List<DeviceSnapshot> result = new ArrayList<>();
        for (DeviceTopo topo : topoList) {
            Device d = topo.getDevice();
            if (d == null) {
                continue;
            }

            // 先转子设备
            List<DeviceSnapshot> subSnapshots = new ArrayList<>();
            List<Device> subDevices = topo.getSubDevices();
            if (subDevices != null) {
                for (Device sub : subDevices) {
                    if (sub == null) {
                        continue;
                    }
                    DeviceSnapshot subSnapshot = new DeviceSnapshot(
                            sub.getDeviceSn(),
                            sub.getDeviceClass(),
                            sub.getModelKey(),
                            sub.getDeviceName(),
                            sub.getNickname(),
                            sub.isOnline());
                    subSnapshots.add(subSnapshot);
                }
            }

            // 主设备（带子设备引用）
            DeviceSnapshot mainSnapshot = new DeviceSnapshot(
                    d.getDeviceSn(),
                    d.getDeviceClass(),
                    d.getModelKey(),
                    d.getDeviceName(),
                    d.getNickname(),
                    d.isOnline(),
                    List.copyOf(subSnapshots));
            result.add(mainSnapshot);

            // 子设备也扁平加入结果
            result.addAll(subSnapshots);
        }
        return result;
    }
}
