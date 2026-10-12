// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.utils;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 通用工具类：汇聚跨模块复用的控制台辅助能力, 避免各 demo 各自实现。
 *
 * <p>包括：
 * <ul>
 *   <li>控制台输出格式化 — {@link #printTitle}(标题栏)/{@link #printHeader}(分节标题)/
 *       {@link #dashLine}(分隔线)/{@link #printKeyValue}(键值对);</li>
 *   <li>交互输入读取 — {@link #readLine}/{@link #readLineEcho}(带回显)/
 *       {@link #readIntInRange}(范围校验 + {@link #QUIT} 退出哨兵);</li>
 *   <li>异常摘要 — {@link #describe(Throwable)}, 统一异常展示格式, 供各 demo 打印结果时复用。</li>
 * </ul>
 *
 * <p>全部方法均为无状态静态方法, 线程安全; 本类不可实例化。
 */
public final class Utils {

    /** {@link #readIntInRange} 在用户输入 q / EOF 时返回的退出哨兵。 */
    public static final int QUIT = -1;

    private Utils() {
    }

    /**
     * 打印标题栏(标题单独一行, 上下各一条 70 宽的 '=' 分隔线)。
     * 样式：
     *    ====================== ... ======================   (70 个 '=')
     *    Title
     *    ====================== ... ======================
     */
    public static void printTitle(String title) {
        System.out.println();
        System.out.println("======================================================================");
        System.out.println(title);
        System.out.println("======================================================================");
    }


    /**
     * 打印分节标题(标题一行 + 其下一条 70 宽的 '-' 分隔线)。
     * 样式：
     *    header-title string.
     *    ---------------------- ... ----------------------   (70 个 '-')
     */
    public static void printHeader(String header) {
        System.out.println();
        System.out.println(header);
        System.out.println("----------------------------------------------------------------------");
    }

    /** 生成 n 个 '-' 拼成的分隔线(n&lt;=0 返回空串)。 */
    public static String dashLine(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    /** 打印一行「键: 值」, 键按<b>显示宽度</b>补齐(中文算 2 宽), 用于展示所选设备信息。 */
    public static void printKeyValue(String key, String value) {
        int width = 12;
        int pad = width - displayWidth(key);
        StringBuilder sb = new StringBuilder("  ").append(key);
        for (int i = 0; i < pad; i++) {
            sb.append(' ');
        }
        sb.append(": ").append(value != null ? value : "");
        System.out.println(sb);
    }

    /** 字符串显示宽度：CJK 等宽字符算 2, 其余算 1。 */
    private static int displayWidth(String s) {
        if (s == null) {
            return 0;
        }
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            w += isWide(s.charAt(i)) ? 2 : 1;
        }

        return w;
    }

    /** 是否为双宽字符(CJK 统一表意/全角/假名/谚文等常见区间)。 */
    private static boolean isWide(char c) {
        return (c >= 0x1100 && c <= 0x115F)   // Hangul Jamo
            || (c >= 0x2E80 && c <= 0xA4CF)   // CJK 部首 … 彝文
            || (c >= 0xAC00 && c <= 0xD7A3)   // Hangul 音节
            || (c >= 0xF900 && c <= 0xFAFF)   // CJK 兼容表意
            || (c >= 0xFE30 && c <= 0xFE4F)   // CJK 兼容形式
            || (c >= 0xFF00 && c <= 0xFF60)   // 全角 ASCII
            || (c >= 0xFFE0 && c <= 0xFFE6);  // 全角符号
    }

    /**
     * 打印提示并读取一行(已 trim)。到达输入流末尾(EOF)返回 {@code null}。
     *
     * @param in     共享的标准输入 reader(调用方负责生命周期)
     * @param prompt 提示语(不换行)
     */
    public static String readLine(BufferedReader in, String prompt) {
        System.out.print(prompt);
        try {
            String line = in.readLine();
            return line == null ? null : line.trim();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 与 {@link #readLine} 相同, 但读到非空值后立刻在下一行回显 "   &gt; 用户输入"。
     *
     * <p>用于 {@code gradle run} 场景: gradle 的 progress bar 会覆盖用户敲的输入,
     * 事后用户看不到自己敲进去的输入。此方法留下一条固化的回显记录,
     */
    public static String readLineEcho(BufferedReader in, String prompt) {
        String v = readLine(in, prompt);
        if (v != null && !v.isEmpty()) {
            System.out.println("   > " + v);
        }
        return v;
    }

    /**
     * 循环读取一个 [min, max] 区间内的整数;用户输入 q(或 EOF)返回 {@link #QUIT}。
     *
     * <p>非法输入(非数字 / 越界)会提示后重试, 空行直接重新提示。
     *
     * <p><b>约束</b>：{@link #QUIT} 为 {@code -1}, 故 {@code [min,max]} 不应包含 {@code -1}
     * (菜单编号从 1 起, 天然满足), 否则合法的 {@code -1} 会被误判为退出。
     */
    public static int readIntInRange(BufferedReader in, String prompt, int min, int max) {
        while (true) {
            String line = readLine(in, prompt);
            if (line == null || line.equalsIgnoreCase("q")) {
                return QUIT;
            }
            if (line.isEmpty()) {
                continue;
            }
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) {
                    return v;
                }
                System.out.println("请输入 " + min + " 到 " + max + " 之间的数字(q 退出)");
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字(q 退出)");
            }
        }
    }

    /**
     * 异常的简短可读描述：优先 {@code getMessage()}; 无消息时(NullPointerException / 原生 Error 常见,
     * {@code getMessage()} 返回 null)回退异常类名, 避免日志/控制台出现无意义的 {@code "xxx: null"}。
     * 完整堆栈由 {@code DemoLog.error(tag, msg, t)} 另行落盘, 本方法只负责单行摘要。
     */
    public static String describe(Throwable t) {
        if (t == null) {
            return "null";
        }
        String msg = t.getMessage();
        return (msg == null || msg.isEmpty()) ? t.getClass().getSimpleName() : msg;
    }
}
