// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * demo_params.json 的类型化访问入口。
 *
 * <p>加载顺序：优先当前工作目录下的 demo_params.json(用户可就地改参数),
 * 找不到则回退 classpath 资源 {@code /demo_params.json}(打包默认值)。
 *
 * <p>各模块/场景通过 {@code params.node("fly_to").dbl("latitude", ...)} 读取分组参数,
 * 避免手动交互输入慢、易超时，也避免硬编码不灵活。
 */
public final class DemoParams {

    /**
     * 机型未匹配到 target_payload_index 时的回退默认值。
     *
     * <p>空字符串表示「未配置」，而非某个具体负载索引——旧版本使用 {@code "99-0-0"}(H30T 的负载索引)
     * 作为兜底，对于未知机型会把错误的 payload index 发往服务端，造成静默失败且难以排查。
     * 现统一返回空字符串，由 SDK/服务端在收到空 payloadIndex 时给出明确错误，引导用户正确配置。
     */
    public static final String DEFAULT_PAYLOAD_INDEX = "";

    private final Node root;
    /** device_model_key → target_payload_index 对照表(来自 device_common_configs_list)。 */
    private final Map<String, String> payloadIndexByModel;

    private DemoParams(Node root) {
        this.root = root;
        // 先按 logging 配置初始化日志(目录/单文件大小), 后续日志即落到配置的目录。
        Node lg = root.child("logging");
        DemoLog.configure(
            lg.str("demo_logs_dir", "demo_logs"),
            lg.intVal("max_file_size_mb", 30)
        );

        this.payloadIndexByModel = parseDeviceCommonConfigs(root);
    }

    /**
     * 解析 device_common_configs.device_common_configs_list 为 modelKey → target_payload_index 映射。
     * 空 target_payload_index 也会入表(表示该机型无预设负载索引)。
     */
    private static Map<String, String> parseDeviceCommonConfigs(Node root) {
        Map<String, String> m = new HashMap<String, String>();
        List<Object> arr = root.child("device_common_configs").list("device_common_configs_list");
        for (Object o : arr) {
            Node n = new Node(o);
            String modelKey = n.str("device_model_key", "");
            if (!modelKey.isEmpty()) {
                m.put(modelKey, n.str("target_payload_index", ""));
            }
        }

        DemoLog.info("DemoParams", "已加载 device_common_configs 机型配置 " + m.size() + " 条");
        // 逐条打印 modelKey → target_payload_index 映射, 便于核对配置是否生效。
        for (Map.Entry<String, String> e : m.entrySet()) {
            DemoLog.info(
                "DemoParams", "  " + e.getKey() + " -> target_payload_index="
                + (e.getValue().isEmpty() ? "(空)" : e.getValue())
            );
        }

        return m;
    }

    /** 用默认文件名 {@code demo_params.json} 加载参数(详见 {@link #load(String)})。 */
    public static DemoParams load() {
        return load("demo_params.json");
    }

    /**
     * 从指定路径加载参数：优先工作目录下的文件(用户可就地改), 找不到再回退 classpath 内置资源。
     * 两处都缺、或读取/解析失败时抛 {@link FlightHubException}。
     */
    public static DemoParams load(String path) {
        // 1) 工作目录文件优先
        Path p = Paths.get(path);
        if (Files.exists(p)) {
            try {
                String text = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                DemoLog.info("DemoParams", "参数取自文件: " + p.toAbsolutePath());
                return new DemoParams(new Node(Json.parse(text)));
            } catch (IOException e) {
                throw new FlightHubException("读取参数文件失败: " + p, e);
            }
        }
        // 2) 回退 classpath 资源(打包进 jar 的默认值)
        try (InputStream in = DemoParams.class.getResourceAsStream("/demo_params.json")) {
            if (in == null) {
                throw new FlightHubException("未找到 demo_params.json(工作目录与 classpath 均无)");
            }
            byte[] buf = readAll(in);
            DemoLog.info("DemoParams", "参数取自内置资源 /demo_params.json");
            return new DemoParams(new Node(Json.parse(new String(buf, StandardCharsets.UTF_8))));
        } catch (IOException e) {
            throw new FlightHubException("读取内置参数资源失败", e);
        }
    }

    /** 取一个参数分组(如 "fly_to" / "takeoff" / "poi" / "timeouts")。 */
    public Node node(String group) {
        return root.child(group);
    }

    // ---- 常用快捷方法 ----

    /**
     * 按设备 modelKey 查预设的 target_payload_index(来自 device_common_configs_list)。
     *
     * <p>匹配逻辑：
     * <ol>
     *   <li>机型在列表中且 target_payload_index 非空 → 直接返回;</li>
     *   <li>机型在列表中但 target_payload_index 为空字符串 → WARN 提示用户补充配置, 返回空字符串;</li>
     *   <li>机型不在列表中 → ERROR 提示用户在 device_common_configs_list 中添加该机型, 返回空字符串。</li>
     * </ol>
     *
     * <p>返回空字符串时, SDK/服务端的后续调用会给出明确错误码, 比返回错误的 payload index
     * (如旧版本的 "99-0-0") 更容易定位根因。
     */
    public String payloadIndexForModel(String modelKey) {
        if (modelKey != null) {
            String v = payloadIndexByModel.get(modelKey);
            if (v != null && !v.isEmpty()) {
                // 命中机型配置且非空：打印 modelKey → target_payload_index。
                DemoLog.info("DemoParams", "机型 " + modelKey + " 命中 target_payload_index=" + v);
                return v;
            }
            if (v != null) {
                // 机型在列表中但 target_payload_index 配置为空字符串：WARN 提示用户补充。
                DemoLog.warn(
                    "DemoParams",
                    "机型(" + modelKey + ") 的 target_payload_index 配置为空, "
                    + "负载操控将使用空 payloadIndex — 请在 device_common_configs_list 中"
                    + "按机型实际负载填写 target_payload_index !"
                );
                return DEFAULT_PAYLOAD_INDEX;
            }
        }

        // 机型完全不在列表中：ERROR 级别，引导用户添加配置。
        DemoLog.error(
            "DemoParams",
            "机型(" + modelKey + ") 未在 device_common_configs_list 中找到, "
            + "回退空 payloadIndex — 请检查 demo_params.json 是否包含此机型的配置 !"
        );
        return DEFAULT_PAYLOAD_INDEX;
    }

    public long timeout(String key, long def) {
        return node("timeouts").longVal(key, def);
    }

    /** 默认回调超时(ms)：来自 demo_params.json 的 timeouts.default_callback_timeout_ms, 缺省回退 15000。 */
    public long defaultTimeout() {
        return timeout("default_callback_timeout_ms", 15000L);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] tmp = new byte[8192];
        int n;
        while ((n = in.read(tmp)) != -1) {
            out.write(tmp, 0, n);
        }
        return out.toByteArray();
    }
}
