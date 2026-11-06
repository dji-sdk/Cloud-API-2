// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.base;

import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 交互菜单运行器: 把「菜单文案 + 选择上界 + 分支派发」三件事收敛成<b>一份</b>菜单项列表。
 *
 * <p><b>解决的问题</b>: 迁移前每个菜单 demo 都手写一段结构完全相同的 {@code while (true)} 循环,
 * 且同一个菜单的信息被拆成三处必须同步修改的副本:
 * <pre>{@code
 * System.out.print(menu());                                       // (1) 文案里的序号
 * int c = Utils.readIntInRange(input, "请选择 (1-6, q 退出): ", 1, 6);  // (2) 上界与提示里的 6
 * switch (c) { case 1: ...; case 6: ...; }                        // (3) 分支序号
 * }</pre>
 * 插入一个菜单项要同时改三处, 漏改的失败方式还很隐蔽: 漏改 (2) 的上界＝新项<b>永远选不中</b>
 * ({@code readIntInRange} 直接判为越界重读), 漏改 (3) ＝落到 {@code default} <b>静默什么都不做</b>——
 * 两种都不报错, 只是功能"不见了"。用本类后序号、上界、提示文案、派发全部由 {@link #item} 的<b>调用顺序</b>
 * 推导, 三者不可能再对不上。
 *
 * <p><b>用法</b>(菜单项文案里的对齐空格与接口名注释按原样保留, 仅去掉手写序号):
 * <pre>{@code
 * Menu.of(this, "执行 " + name())
 *     .banner("=== AI Box 管理 ===")
 *     .header(new Runnable() {              // 可选: 每轮重新计算的动态提示 (缓存态/订阅态)
 *         public void run() {
 *             if (lastBoxSn != null) console("(已缓存 boxSn=" + lastBoxSn + ")");
 *         }
 *     })
 *     .item("查询 AI Box 信息    (fetchAIBoxInfo, 缓存 boxSn)", () -> fetchBoxInfo(module, sn))
 *     .section("--- 拍摄 ---")               // 分组小标题, 不占序号
 *     .item("拍照                (drcTakePhoto)", () -> takePhoto())
 *     .exitLog("退出 AI Box 菜单")     // 可选: 不写也会写一行 "退出菜单: <title>"
 *     .run();
 * }</pre>
 *
 * <p><b>不做危险动作确认</b>: {@code confirmDangerous} 保持在各动作方法<b>内部</b>、参数读取<b>之后</b>调用。
 * 提到菜单层会让确认文案退化成菜单标题 ("删除模型文件" 而不是 "删除模型文件 fileId=123"),
 * 而这类确认的价值恰恰在于让用户看清<b>作用对象</b>。
 *
 * <p><b>只捕获 {@code Exception} 而非 {@code Throwable}</b>: 这是主流程而非收尾路径。单个菜单动作失败
 * (含超时、错误码)记录后回菜单继续; 但原生 {@code Error}(如 {@code UnsatisfiedLinkError})说明 JNI 层
 * 已不可用, 必须让它穿透到基类 {@code runBody} 兜底并走完清理, 而不是让用户对着一个已经坏掉的 SDK
 * 继续点菜单。与全工程「收尾兜 Throwable、主流程兜 Exception」的分工一致。
 */
public final class Menu {

    /**
     * 序号列宽 ({@code "1.  "} / {@code "10. "}), 左对齐。
     *
     * <p>公开是为了让 {@code DemoEntry} 的主菜单用同一个值排版: 主菜单不经本类渲染
     * (它列的是 demo 而不是菜单项), 但用户看到的是同一串界面, 两处宽度必须同源,
     * 否则一进子菜单文案左边界就整体挪一格。
     */
    public static final int NUMBER_COLUMN_WIDTH = 4;

    /** 一个菜单动作。允许抛任意 {@code RuntimeException}, 由运行器统一捕获回显。 */
    public interface Action {
        void run();
    }

    private final ModuleDemo owner;
    private final String title;
    private final List<Object> rows = new ArrayList<Object>();   // String=分组小标题, Item=菜单项
    private final List<Item> items = new ArrayList<Item>();      // 仅菜单项, 下标+1 即序号

    private String banner;
    private Runnable header;
    private String quitLabel = "返回上级菜单";
    private String quitWord = "退出";
    /** {@code null} = 用默认文案 (见 {@link #exitLogText}); 显式关闭走 {@link #noExitLog}。 */
    private String exitLog;
    /** 是否显式要求「退出时不写日志」。与 {@code exitLog == null} 区分开, 见 {@link #exitLog}。 */
    private boolean exitLogDisabled;

    private Menu(ModuleDemo owner, String title) {
        this.owner = owner;
        this.title = title;
    }

    /**
     * 新建一个菜单。
     *
     * @param owner 宿主 demo, 用于 {@code console/name()} 与共享的 {@code input}
     *              (不要另建 {@code BufferedReader}, 会与宿主互相吞字节)
     * @param title 每轮重绘的标题, 交给 {@link Utils#printTitle}
     */
    public static Menu of(ModuleDemo owner, String title) {
        return new Menu(owner, title);
    }

    /** 菜单首行横幅, 如 {@code "=== AI Box 管理 ==="}。 */
    public Menu banner(String text) {
        this.banner = text;
        return this;
    }

    /**
     * 每轮重新执行的动态提示 (缓存命中、订阅路数等)。
     *
     * <p>必须是回调而不是一段字符串: 这些提示读的是<b>会随菜单操作变化</b>的状态,
     * 求值一次会永远停在进入菜单那一刻的旧值。
     */
    public Menu header(Runnable header) {
        this.header = header;
        return this;
    }

    /** 分组小标题 (如 {@code "--- 拍摄 ---"}), 只影响排版、不占序号。 */
    public Menu section(String text) {
        rows.add(text);
        return this;
    }

    /**
     * 紧跟上一菜单项的续行说明, 缩进对齐到文案列 (不占序号、不加空行)。
     *
     * <p>用于一行放不下的补充 (如某项参数取自哪个配置节)。与 {@link #section} 的区别:
     * section 是分组, note 属于上一个 item。
     */
    public Menu note(String text) {
        rows.add(new Note(text));
        return this;
    }

    /**
     * 追加一个菜单项。序号 = 本方法的调用次序, 无需也无法手写。
     *
     * @param label  菜单文案 (不含序号), 可保留原有对齐空格与接口名注释
     * @param action 选中后执行的动作
     */
    public Menu item(String label, Action action) {
        Item it = new Item(label, action);
        rows.add(it);
        items.add(it);
        return this;
    }

    /** 覆盖 {@code q} 行文案, 默认 {@code "返回上级菜单"}; 子菜单常用 {@code "返回"}。 */
    public Menu quit(String label) {
        this.quitLabel = label;
        return this;
    }

    /** 覆盖输入提示里 {@code q} 的动词, 默认 {@code "退出"}; 子菜单常用 {@code "返回"}。 */
    public Menu quitWord(String word) {
        this.quitWord = word;
        return this;
    }

    /**
     * 覆盖用户选 {@code q} 时写的那行日志。<b>不调用也会写</b>——默认文案见 {@link #exitLogText}。
     *
     * <p><b>为什么是「不写=有默认」而不是「不写=不写」</b>: 本方法原先是纯可选 setter, 漏调即静默无日志。
     * 迁移到本类时 23 个菜单里有 21 个漏调了它, 于是日志文件里成片地丢失「用户在此退出了 X 菜单」这条
     * 时间边界——而这正是事后按时间轴复原「用户当时在哪一层、之后的操作属于哪个菜单」的唯一锚点。
     * 漏调不报错、不影响功能, 只是排查时凭空少一段, 恰恰是本类想根除的那类失败模式 (见类注释)。
     * 现在默认有日志, 真不想要的菜单显式调 {@link #noExitLog()} 声明意图。
     */
    public Menu exitLog(String msg) {
        this.exitLog = msg;
        this.exitLogDisabled = false;
        return this;
    }

    /** 显式声明「本菜单退出时不写日志」(与漏调 {@link #exitLog} 区分开)。 */
    public Menu noExitLog() {
        this.exitLogDisabled = true;
        return this;
    }

    /**
     * 退出日志的实际文案: 显式设过就用设的, 否则用 {@code "退出菜单: " + title}。
     *
     * <p>默认取 {@code title} 而非 {@code owner.name()}: 一个 demo 可能有多层子菜单
     * (如 {@code TerraBuildDemo} 的 4 个), 用 demo 名会让这几行日志完全一样, 分不出退的是哪一层。
     */
    private String exitLogText() {
        if (exitLogDisabled) {
            return null;
        }
        return exitLog != null ? exitLog : "退出菜单: " + title;
    }

    /** 进入菜单循环, 直到用户输入 {@code q} 或输入流结束 (EOF)。 */
    public void run() {
        if (items.isEmpty()) {
            throw new IllegalStateException("菜单没有任何可选项: " + title);
        }
        final String text = render();
        final String prompt = "请选择(1-" + items.size() + ", q " + quitWord + "): ";
        final String exitMsg = exitLogText();

        while (true) {
            Utils.printTitle(title);
            if (header != null) {
                // header 与菜单动作一样要兜异常, 且理由更硬: 它读的恰恰是「会随菜单操作变化」的
                // 缓存态/订阅态 (这正是它必须是回调的原因, 见 header 方法注释), 而这些字段在某个
                // 动作失败后完全可能处在半更新状态。一个 NPE 从这里抛出去会掀翻整个菜单循环,
                // 用户丢掉的是全部后续操作机会——代价远大于"这一轮少一行动态提示"。
                // 与动作一样只兜 Exception 不兜 Throwable: 原生 Error 须穿透到 runBody(见类注释)。
                try {
                    header.run();
                } catch (Exception e) {
                    owner.console("   [提示渲染失败, 不影响菜单] " + Utils.describe(e));
                    DemoLog.warn(owner.name(), "菜单动态提示渲染异常: " + Utils.describe(e));
                }
            }
            System.out.print(text);

            // readIntInRange 对 EOF 同样返回 QUIT: 控制台已无法交互时不能空转刷屏。
            // 透传 quitWord, 使输入非法时的重试文案与上面 prompt 里的说法一致
            // (子菜单 prompt 写"q 返回", 重试却说"q 退出"会让用户不敢按)。
            int choice = Utils.readIntInRange(owner.input, prompt, 1, items.size(), quitWord);
            if (choice == Utils.QUIT) {
                if (exitMsg != null) {
                    DemoLog.info(owner.name(), exitMsg);
                }
                return;
            }

            // 单个菜单动作失败 (含超时、错误码)不退出菜单: 记录后回到菜单继续。
            // 原生 Error(如 UnsatisfiedLinkError)不在此拦截, 交由基类 runBody 兜底隔离 (见类注释)。
            try {
                items.get(choice - 1).action.run();
            } catch (Exception e) {
                owner.console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(owner.name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    /**
     * 渲染菜单文案。序号列宽固定 {@link #NUMBER_COLUMN_WIDTH}({@code "1.  "} / {@code "10. "}),
     * 使一位数与两位数菜单项的文案左边界对齐。
     */
    private String render() {
        StringBuilder sb = new StringBuilder("\n");
        if (banner != null) {
            sb.append("  ").append(banner).append('\n');
        }
        int no = 0;
        for (Object row : rows) {
            if (row instanceof String) {
                // 分组之间空一行分隔; 但菜单以分组开头时不要顶一行空行。
                if (no > 0 || banner != null) {
                    sb.append('\n');
                }
                sb.append("  ").append((String) row).append('\n');
                continue;
            }
            if (row instanceof Note) {
                // 缩进 = 左边距 2 + 序号列宽, 对齐到上一项的文案列。
                sb.append("  ");
                for (int i = 0; i < NUMBER_COLUMN_WIDTH; i++) {
                    sb.append(' ');
                }
                sb.append(((Note) row).text).append('\n');
                continue;
            }
            no++;
            String tag = no + ".";
            sb.append("  ").append(tag);
            for (int i = tag.length(); i < NUMBER_COLUMN_WIDTH; i++) {
                sb.append(' ');
            }
            sb.append(((Item) row).label).append('\n');
        }
        sb.append("\n  q.");
        for (int i = "q.".length(); i < NUMBER_COLUMN_WIDTH; i++) {
            sb.append(' ');
        }
        sb.append(quitLabel).append('\n');
        return sb.toString();
    }

    private static final class Note {
        final String text;

        Note(String text) {
            this.text = text;
        }
    }

    private static final class Item {
        final String label;
        final Action action;

        Item(String label, Action action) {
            this.label = label;
            this.action = action;
        }
    }
}
