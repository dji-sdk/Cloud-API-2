// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.SetRelayOccupiedStatusResult;
import com.dji.flighthubsdk.core.FlightHubSdkManager;
import com.dji.flighthubsdk.core.Relay;
import com.dji.flighthubsdk.core.RelayControlModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 中继控制 Demo：调用 {@code relay.getRelayControlModule().setRelayOccupiedStatus(...)}。
 *
 * <p>用于验证 #21（relay-occupied-status）：SDK 发参为 {@code int occupied_status}(0=禁止抢占,1=允许抢占)，
 * 但线上字段应为后端的 {@code allow_occupied}(bool)。本 demo 打印结果，并提示核对请求体键名/类型。
 *
 * <p>链路：{@code HTTP | POST /device/api/v1/service/relay-occupied-status}。
 */
public class RelayControlDemo extends ModuleDemo {

    @Override
    public String name() {
        return "RelayControlDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式中继控制 demo");
            return;
        }
        final String relaySn = askRelaySn();
        if (relaySn.isEmpty()) {
            hint("中继 SN 为空, 取消");
            return;
        }
        final Relay relay = FlightHubSdkManager.getRelayInstance(relaySn);
        if (relay == null) {
            hint("getRelayInstance 返回 null, sn=" + relaySn);
            return;
        }
        final RelayControlModule module = relay.getRelayControlModule();
        if (module == null) {
            hint("getRelayControlModule() 返回 null");
            return;
        }
        final long actionTimeout = timeout("default_callback_timeout_ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(menu(relaySn));
            int choice = Utils.readIntInRange(input, "请选择(1-2, q 退出): ", 1, 2);
            if (choice == Utils.QUIT) {
                break;
            }
            // 1 -> occupied_status=1(允许抢占); 2 -> occupied_status=0(禁止抢占)
            int occupiedStatus = (choice == 1) ? 1 : 0;
            call(module, actionTimeout, occupiedStatus);
        }
    }

    private String menu(String relaySn) {
        return "\n"
                + "  中继 SN = " + relaySn + "\n"
                + "  1. 允许抢占 (occupied_status=1 → 期望 body allow_occupied:true)\n"
                + "  2. 禁止抢占 (occupied_status=0 → 期望 body allow_occupied:false)\n"
                + "  q. 退出\n";
    }

    private void call(RelayControlModule module, long timeout, final int occupiedStatus) {
        String label = "setRelayOccupiedStatus(" + occupiedStatus + ")";
        final Await<FlightHubError> await = new Await<FlightHubError>(name() + "." + label);
        final AtomicReference<SetRelayOccupiedStatusResult> refResult =
                new AtomicReference<SetRelayOccupiedStatusResult>();
        Outcome o = runOutcome(label, timeout, await, () ->
                module.setRelayOccupiedStatus(occupiedStatus,
                        new FlightHubCallback.SetRelayOccupiedStatusCallback() {
                            @Override
                            public void onResult(FlightHubError error, SetRelayOccupiedStatusResult result) {
                                refResult.set(result);
                                await.set(error);
                            }
                        })
        );
        if (o == Outcome.SUCCESS) {
            SetRelayOccupiedStatusResult r = refResult.get();
            both("   result: bid=" + (r == null ? "null" : r.getBid())
                    + ", status=" + (r == null ? "null" : r.getStatus()));
            both("   (请核对 SDK/mock 请求体: 键应为 allow_occupied, 值应为 "
                    + (occupiedStatus != 0) + ")");
        }
    }

    private String askRelaySn() {
        String def = session.dockSn() != null && !session.dockSn().isEmpty()
                ? session.dockSn() : rootSn();
        String line = Utils.readLine(input, "   中继 SN (回车用 \"" + def + "\"): ");
        if (line == null) {
            return def;
        }
        line = line.trim();
        return line.isEmpty() ? def : line;
    }

    private String rootSn() {
        if (session.topo() == null || session.topo().getDevice() == null) {
            return "";
        }
        String sn = session.topo().getDevice().getDeviceSn();
        return sn != null ? sn : "";
    }

    /** 结果行双写：既落日志文件(带 demo 名 tag)又回显控制台。 */
    private void both(String line) {
        DemoLog.info(name(), line);
        console(line);
    }
}
