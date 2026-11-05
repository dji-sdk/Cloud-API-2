// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用工具类: 汇聚跨模块复用的控制台辅助能力, 避免各 demo 各自实现。
 *
 * <p>包括:
 * <ul>
 *   <li>控制台输出格式化 — {@link #printTitle}(标题栏)/{@link #printHeader}(分节标题)/
 *       {@link #dashLine}(分隔线)/{@link #printKeyValue}(键值对); </li>
 *   <li>交互输入读取 — {@link #readLine}/{@link #readLineEcho}(带回显)/
 *       {@link #readIntInRange}(范围校验 + {@link #QUIT} 退出哨兵); </li>
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
     * 打印标题栏 (标题单独一行, 上下各一条 70 宽的 '=' 分隔线)。
     * 样式:
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
     * 打印分节标题 (标题一行 + 其下一条 70 宽的 '-' 分隔线)。
     * 样式:
     *    header-title string.
     *    ---------------------- ... ----------------------   (70 个 '-')
     */
    public static void printHeader(String header) {
        System.out.println();
        System.out.println(header);
        System.out.println("----------------------------------------------------------------------");
    }

    /** 生成 n 个 '-' 拼成的分隔线 (n&lt;=0 返回空串)。 */
    public static String dashLine(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    /** 打印一行「键: 值」, 键按<b>显示宽度</b>补齐 (中文算 2 宽), 用于展示所选设备信息。 */
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

    /** 字符串显示宽度: CJK 等宽字符算 2, 其余算 1。 */
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

    /** 是否为双宽字符 (CJK 统一表意/全角/假名/谚文等常见区间)。 */
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
     * 打印提示并读取一行 (已 trim)。到达输入流末尾 (EOF)返回 {@code null}。
     *
     * @param in     共享的标准输入 reader(调用方负责生命周期)
     * @param prompt 提示语 (不换行)
     */
    public static String readLine(BufferedReader in, String prompt) {
        System.out.print(prompt);
        try {
            String line = in.readLine();
            return line == null ? null : line.trim();
        } catch (IOException e) {
            // 与 EOF(readLine 返回 null)一样返回 null: 调用方 (readIntInRange / 各 demo)统一按
            // 「输入流不可用, 收工」处理, 语义一致, 不必各写一套 IOException 分支。
            // 但两者的排查方向完全不同——EOF 是管道正常结束, IOException 是控制台真的坏了
            // (终端被回收、被信号打断、stdin 被关闭), 静默吞掉会让用户只看到一句
            // 「输入流已结束」而完全无从下手, 故必须留痕。
            //
            // 这里走 stderr 而非 DemoLog: 本类是零依赖的最底层工具, 而 DemoLog 反过来 import 了
            // 它 (见 DemoLog 的 import 段), 加反向 import 会形成 utils ↔ runtime 的包级循环。
            // stderr 与 stdout 分离, 也不会插进菜单文案中间打乱排版。
            System.err.println("[ERROR] 读取标准输入失败, 按 EOF 处理: " + describe(e));
            return null;
        }
    }

    /**
     * 与 {@link #readLine} 相同, 但读到非空值后立刻在下一行回显 "   &gt; 用户输入"。
     *
     * <p>用于 {@code gradle run} 场景: gradle 的 progress bar 会覆盖用户敲的输入,
     * 事后用户看不到自己敲进去的输入。此方法留下一条固化的回显记录, 便于事后核对。
     */
    public static String readLineEcho(BufferedReader in, String prompt) {
        String v = readLine(in, prompt);
        if (v != null && !v.isEmpty()) {
            System.out.println("   > " + v);
        }
        return v;
    }

    /**
     * 循环读取一个 [min, max] 区间内的整数; 用户输入 q(或 EOF)返回 {@link #QUIT}。
     * 重试文案里 {@code q} 的动词固定为「退出」, 需要其它措辞时用
     * {@link #readIntInRange(BufferedReader, String, int, int, String)}。
     */
    public static int readIntInRange(BufferedReader in, String prompt, int min, int max) {
        return readIntInRange(in, prompt, min, max, "退出");
    }

    /**
     * 循环读取一个 [min, max] 区间内的整数; 用户输入 q(或 EOF)返回 {@link #QUIT}。
     *
     * <p>非法输入 (非数字 / 越界)会提示后重试, 空行直接重新提示。
     *
     * <p><b>约束</b>: {@link #QUIT} 为 {@code -1}, 故 {@code [min,max]} 不应包含 {@code -1}
     * (菜单编号从 1 起, 天然满足), 否则合法的 {@code -1} 会被误判为退出。
     *
     * <p><b>{@code quitWord} 存在的理由</b>: 重试文案原先硬编码「(q 退出)」, 而调用方的 {@code prompt}
     * 里写的可能是「q 返回」(子菜单)或「q 取消」(单值输入)——同一个 {@code q} 在一屏之内被叫成两个名字,
     * 用户按了 {@code q} 才知道到底会发生什么。本参数让两处措辞出自同一个真相。
     *
     * @param quitWord {@code q} 的动词, 如 {@code "退出"}/{@code "返回"}/{@code "取消"};
     *                 null 或空串回退 {@code "退出"}
     */
    public static int readIntInRange(BufferedReader in, String prompt, int min, int max, String quitWord) {
        String q = (quitWord == null || quitWord.isEmpty()) ? "退出" : quitWord;
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
                System.out.println("请输入 " + min + " 到 " + max + " 之间的数字(q " + q + ")");
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字(q " + q + ")");
            }
        }
    }

    /**
     * 解析逗号分隔输入为去空白、去空项的列表; null 输入返回空列表。
     *
     * <p>供多个模块 demo 的交互输入场景复用(如 flightTaskIds / uuids / SN 列表输入)。
     */
    public static List<String> parseCsv(String s) {
        List<String> out = new ArrayList<>();
        if (s == null) {
            return out;
        }
        for (String part : s.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    /**
     * 机场 mode_code 枚举的中文描述(0-9, 见 SDK bean {@code DockModeCode} 类注释)。
     *
     * <p>供 {@code DeviceDebugDemo} 与 {@code FlySafeUnlockDemo} 共用, 消除原先两份重复定义。
     */
    public static String describeModeCode(int code) {
        switch (code) {
            case 0: return "空闲中";
            case 1: return "现场调试中";
            case 2: return "远程调试中";
            case 3: return "设备升级中";
            case 4: return "设备作业中";
            case 5: return "机场设备未标定";
            case 6: return "节能状态中";
            case 7: return "现场急停中";
            case 8: return "换电中";
            case 9: return "日志拉取中";
            default: return "未知(" + code + ")";
        }
    }

    /**
     * 异常的简短可读描述: 优先 {@code getMessage()}; 无消息时 (NullPointerException / 原生 Error 常见,
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

    // ==================== PII 脱敏 (合规: 手机号/邮箱/姓名/IMEI) ====================

    /**
     * 手机号脱敏: 前 2 + 7 个 * + 后 4 (如 13*******8000)。
     * 固定 7 个 * 不随原长变化, 防止通过掩码长度反推号码位数。
     * null/空返回空串; 长度不足 6 时全部掩码。
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        int len = phone.length();
        if (len < 6) {
            return stars(len);
        }
        return phone.substring(0, 2) + "*******" + phone.substring(len - 4);
    }

    /**
     * 邮箱脱敏: 前 2 + *** + 后 2 @ 前 2 + *** + 后 2 (如 sa***ng@dj***om)。
     * 本地名与域名各保留首尾各 2, 中间固定 3 个 *。null/空返回空串。
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return maskEmailSegment(email);
        }
        return maskEmailSegment(email.substring(0, at))
            + "@" + maskEmailSegment(email.substring(at + 1));
    }

    /** 邮箱分段脱敏: 保留首 2 + 尾 2, 中间 3 个 *; 长度不足 5 时全部掩码。 */
    private static String maskEmailSegment(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        int len = s.length();
        if (len <= 4) {
            return stars(len);
        }
        return s.substring(0, 2) + "***" + s.substring(len - 2);
    }

    /**
     * 中文姓名脱敏: 仅保留第 1 字 + 2 个 * (如 韦**)。
     * 固定 2 个 * 不随原长变化, 防止通过掩码长度反推姓名字数。
     * null/空返回空串。
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return name.charAt(0) + "**";
    }

    /**
     * IMEI 脱敏: 前 4 + * + 后 2 (如 1234*********45)。
     * 中间 * 数量随原长变化 (保留长度以便核对位数); null/空返回空串; 长度不足 7 时全部掩码。
     */
    public static String maskImei(String imei) {
        if (imei == null || imei.isEmpty()) {
            return "";
        }
        int len = imei.length();
        if (len < 7) {
            return stars(len);
        }
        return imei.substring(0, 4) + stars(len - 6) + imei.substring(len - 2);
    }

    /**
     * 用户 ID 脱敏: 前 4 + * + 后 2 (如 1547*********xx)。
     * 中间 * 数量随原长变化; null/空返回空串; 长度不足 7 时全部掩码。
     */
    public static String maskUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return "";
        }
        int len = userId.length();
        if (len < 7) {
            return stars(len);
        }
        return userId.substring(0, 4) + stars(len - 6) + userId.substring(len - 2);
    }

    /** 生成 n 个 '*' 拼成的字符串 (n&lt;=0 返回空串)。 */
    private static String stars(int n) {
        if (n <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}
