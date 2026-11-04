// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.core.SessionInfo;
import com.dji.flighthubsdk.core.SessionManager;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.runtime.Node;
import com.dji.flighthubsdk.demo.utils.Utils;
import com.dji.flighthubsdk.generated.SessionConfig;

import java.util.List;

/**
 * Session 多实例 Demo（int session_id；一期无 unregister）。
 *
 * <p>入口：{@link FlightHubSDK#getSessionManager()} → register / getAllSessionInfo；
 * 设备走 {@link FlightHubSDK#getWorkspaceInstance(int)} 等门面。
 */
public class SessionDemo extends ModuleDemo {

    @Override
    public boolean requiresDevice() {
        return false;
    }

    @Override
    public String name() {
        return "SessionDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式 demo");
            return;
        }

        while (true) {
            Utils.printTitle("执行 " + name());
            System.out.print(mainMenu());
            int choice = Utils.readIntInRange(input, "请选择(1-5, q 退出): ", 1, 5);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出 Session 菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: runList(); break;
                    case 2: runRegister(); break;
                    case 3: runLookupInfo(); break;
                    case 4: runGetDeviceViaFacade(); break;
                    case 5: runShowSession0Workspace(); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    private String mainMenu() {
        return "\n  ---- 查询 ----\n"
                + "  1. 列出所有 SessionInfo           (getAllSessionInfo)\n"
                + "  2. 注册子节点 Session             (register → int id)\n"
                + "  3. 按 session_id 查看 SessionInfo (list 过滤)\n"
                + "  ---- 设备 ----\n"
                + "  4. 门面 getXxx(sessionId) 取设备\n"
                + "  ---- 默认 ----\n"
                + "  5. 查看 session 0 Workspace\n"
                + "  q. 退出\n";
    }

    private void runList() {
        SessionManager mgr = FlightHubSDK.getSessionManager();
        if (mgr == null) {
            console("   结果 -> [失败] SessionManager 为 null（SDK 未初始化？）");
            return;
        }
        List<SessionInfo> sessions = mgr.getAllSessionInfo();
        if (sessions == null || sessions.isEmpty()) {
            console("   结果 -> [空] 没有任何已注册的 session");
            return;
        }
        console("   结果 -> [成功] 共 " + sessions.size() + " 个 session:");
        for (SessionInfo s : sessions) {
            console("     " + s);
        }
    }

    private void runRegister() {
        Node cfg = params.node("session");
        String nodeId = cfg.str("node_id", "");
        if (nodeId == null || nodeId.isEmpty()) {
            hint("[跳过] session.node_id 未配置");
            return;
        }
        String displayName = cfg.str("display_name", "");

        SessionConfig config = new SessionConfig(nodeId);
        if (displayName != null && !displayName.isEmpty()) {
            config.setDisplay_name(displayName);
        }

        console("   register: node_id='" + nodeId + "' display_name='" + displayName + "'");
        SessionManager mgr = FlightHubSDK.getSessionManager();
        if (mgr == null) {
            console("   结果 -> [失败] SessionManager 为 null");
            return;
        }
        int sid = mgr.register(config);
        if (sid < 0) {
            console("   结果 -> [失败] register 返回 " + sid);
            return;
        }
        console("   结果 -> [成功] session_id=" + sid);
        SessionInfo info = findInfo(mgr, sid);
        if (info != null) {
            console("   " + info);
        }
        Workspace ws = FlightHubSDK.getWorkspaceInstance(sid);
        console("   getWorkspaceInstance(" + sid + ") -> " + (ws != null ? "ok" : "null"));
    }

    private void runLookupInfo() {
        console("   请输入 int session_id (直接回车=0):");
        String line = Utils.readLine(input, "   > ");
        int sessionId = SessionManager.kDefaultSessionId;
        if (line != null && !line.isEmpty()) {
            try {
                sessionId = Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                console("   结果 -> [失败] 非法 session_id");
                return;
            }
        }

        SessionManager mgr = FlightHubSDK.getSessionManager();
        if (mgr == null) {
            console("   结果 -> [失败] SessionManager 为 null");
            return;
        }
        SessionInfo info = findInfo(mgr, sessionId);
        if (info == null) {
            console("   结果 -> [未找到] session_id=" + sessionId);
            return;
        }
        console("   结果 -> [找到] " + info);
    }

    private void runGetDeviceViaFacade() {
        console("   请输入 int session_id (直接回车=上次注册需自行记住):");
        String line = Utils.readLine(input, "   > ");
        int sessionId;
        try {
            sessionId = (line == null || line.isEmpty()) ? -1 : Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            console("   结果 -> [失败] 非法 session_id");
            return;
        }
        if (sessionId < 0) {
            console("   结果 -> [失败] 请输入有效 session_id");
            return;
        }

        SessionManager mgr = FlightHubSDK.getSessionManager();
        if (mgr == null || findInfo(mgr, sessionId) == null) {
            console("   结果 -> [失败] session 不存在，请先 register");
            return;
        }

        Node cfg = params.node("session");
        String dockSn = cfg.str("dock_sn", "");
        String droneSn = cfg.str("drone_sn", "");
        if (dockSn != null && !dockSn.isEmpty()) {
            console("   getDockInstance(" + sessionId + ", '" + dockSn + "') -> "
                    + (FlightHubSDK.getDockInstance(sessionId, dockSn) != null ? "ok" : "null"));
        }
        if (droneSn != null && !droneSn.isEmpty()) {
            console("   getDroneInstance(" + sessionId + ", '" + droneSn + "') -> "
                    + (FlightHubSDK.getDroneInstance(sessionId, droneSn) != null ? "ok" : "null"));
        }
        console("   getWorkspaceInstance(" + sessionId + ") -> "
                + (FlightHubSDK.getWorkspaceInstance(sessionId) != null ? "ok" : "null"));
    }

    private void runShowSession0Workspace() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        console("   getWorkspaceInstance() [session 0] -> " + (ws != null ? "ok" : "null"));
        Workspace ws0 = FlightHubSDK.getWorkspaceInstance(SessionManager.kDefaultSessionId);
        console("   getWorkspaceInstance(" + SessionManager.kDefaultSessionId + ") -> "
                + (ws0 != null ? "ok" : "null"));
        SessionManager mgr = FlightHubSDK.getSessionManager();
        if (mgr != null) {
            SessionInfo s0 = findInfo(mgr, SessionManager.kDefaultSessionId);
            console("   session 0 info -> " + (s0 != null ? s0.toString() : "null"));
        }
    }

    private static SessionInfo findInfo(SessionManager mgr, int sessionId) {
        if (mgr == null) {
            return null;
        }
        for (SessionInfo info : mgr.getAllSessionInfo()) {
            if (info != null && info.getSessionId() == sessionId) {
                return info;
            }
        }
        return null;
    }

    protected void console(String line) {
        System.out.println(line);
    }

    protected void hint(String msg) {
        System.out.println("   >> " + msg);
    }
}
