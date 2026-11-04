// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.demo.module;

import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.ElementCreateParams;
import com.dji.flighthubsdk.bean.ElementCreateResult;
import com.dji.flighthubsdk.bean.ElementDeleteParams;
import com.dji.flighthubsdk.bean.ElementDeleteResult;
import com.dji.flighthubsdk.bean.ElementGeometry;
import com.dji.flighthubsdk.bean.ElementGroupOutput;
import com.dji.flighthubsdk.bean.ElementItem;
import com.dji.flighthubsdk.bean.ElementMediaImage;
import com.dji.flighthubsdk.bean.ElementResourceData;
import com.dji.flighthubsdk.bean.ElementListHierarchyParams;
import com.dji.flighthubsdk.bean.ElementListHierarchyResult;
import com.dji.flighthubsdk.bean.ElementProperties;
import com.dji.flighthubsdk.bean.ElementResourceContent;
import com.dji.flighthubsdk.bean.ElementUpdateParams;
import com.dji.flighthubsdk.bean.ElementUpdateResult;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.ElementModule;
import com.dji.flighthubsdk.demo.base.ModuleDemo;
import com.dji.flighthubsdk.demo.runtime.Await;
import com.dji.flighthubsdk.demo.runtime.DemoLog;
import com.dji.flighthubsdk.demo.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 元素管理 Demo：调用 {@code session.workspace().getElementModule()} 的要素 HTTP 接口。
 *
 * <p>用于验证 #56：SDK 元素渲染属性键 {@code clamp_to_ground} 与后端驼峰
 * {@code clampToGround} 不一致，导致贴地渲染设置/读取失效。本 demo 创建/更新要素时
 * 显式把 {@code clampToGround} 置 true，并提示核对请求体键名应为 {@code clampToGround}。
 *
 * <p>链路（网关别名 /pf-map → 后端 map）：
 * <ul>
 *   <li>创建要素：{@code HTTP | POST /pf-map/api/v1/elements/create}</li>
 *   <li>更新要素：{@code HTTP | PUT /pf-map/api/v1/elements/update/{id}}</li>
 *   <li>查询层级：{@code HTTP | GET /pf-map/api/v1/elements/lists}</li>
 * </ul>
 *
 * <p><b>SDK 入口：</b>{@code session.workspace().getElementModule()}(挂在 Workspace 上)。
 */
public class ElementDemo extends ModuleDemo {

    /** remark 回归测试状态，子菜单各步骤间共享。 */
    private String remarkTestElementId;
    private String remarkTestGroupId;
    private String remarkTestTargetRemark;
    private String remarkTestSdkRemark;
    private String remarkTestGeometryType;
    private String remarkTestCoordinates;
    private boolean remarkTestQueried;
    private boolean remarkTestCreatedByDemo;

    @Override
    public String name() {
        return "ElementDemo";
    }

    @Override
    protected void run() throws Exception {
        if (input == null) {
            DemoLog.warn(name(), "无交互输入(input=null), 无法运行菜单式元素管理 demo");
            return;
        }

        final ElementModule module = session.workspace().getElementModule();
        if (module == null) {
            DemoLog.error(name(), "getElementModule() 返回 null");
            console("   获取 ElementModule 失败(getElementModule() 返回 null), 返回上级菜单");
            return;
        }

        final long actionTimeout = timeout("default_callback_timeout_ms");
        DemoLog.info(name(), "actionTimeout=" + actionTimeout + "ms");

        while (true) {
            Utils.printTitle("执行 " + name());
            console(menu());
            int choice = Utils.readIntInRange(input, "请选择(1-4, q 退出): ", 1, 4);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出元素管理菜单");
                break;
            }
            try {
                switch (choice) {
                    case 1: createElement(module, actionTimeout); break;
                    case 2: updateElement(module, actionTimeout); break;
                    case 3: listElementHierarchy(module, actionTimeout); break;
                    case 4: remarkTestMenu(module, actionTimeout); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "菜单动作执行异常: " + Utils.describe(e), e);
            }
        }
    }

    private String menu() {
        return "\n"
                + "  1. 创建要素       createElement (clampToGround=true)\n"
                + "  2. 更新要素       updateElement (需输入 id, clampToGround=true)\n"
                + "  3. 查询元素层级   listElementHierarchy\n"
                + "  4. remark 回归测试\n"
                + "  q. 退出\n";
    }

    // ==================== [1] 创建要素 ====================

    private void createElement(ElementModule module, long timeout) {
        String id = createPointElement(module, timeout, "demo-element");
        if (id != null) {
            console("   已创建: id=" + id + " (id 供 [2] 更新输入)");
            console("   请核对日志 element_create body: 键应为 clampToGround");
        }
    }

    /** 创建一个点要素并返回 id；失败返回 null。 */
    private String createPointElement(ElementModule module, long timeout, String elementName) {
        // 构造 GeoJSON Feature：点(0)，坐标 raw JSON；渲染属性只关注贴地 clampToGround。
        ElementResourceContent content = new ElementResourceContent();
        content.setType("Feature");
        ElementProperties props = content.getProperties();
        props.setColor("#FF0000");
        props.setClampToGround(true);   // #56 关键：应为 true，请求体键须为 clampToGround
        ElementGeometry geometry = content.getGeometry();
        geometry.setType("Point");
        geometry.setCoordinates("[114.06,22.55,12.6]");

        final ElementCreateParams p = new ElementCreateParams(content, 0, elementName);
        console("   > name=" + p.getName() + " resourceType=" + p.getResourceType()
                + " geometry=" + geometry.getType() + " clampToGround=true");

        final Await<FlightHubError> await = new Await<>(name() + ".createElement");
        final AtomicReference<ElementCreateResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("创建要素", timeout, await, () ->
                module.createElement(p, new FlightHubCallback.ElementCreateElementCallback() {
                    @Override
                    public void onResult(FlightHubError error, ElementCreateResult result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
        );
        if (o != Outcome.SUCCESS) {
            return null;
        }
        ElementCreateResult r = ref.get();
        if (r == null || r.getId() == null || r.getId().isEmpty()) {
            console("   创建成功但未返回 id");
            return null;
        }
        return r.getId();
    }

    // ==================== [4] remark 回归测试 ====================

    /**
     * remark 回归测试子菜单：2=updateElement 设置 remark；
     * 3=listElementHierarchy 查询；4=检查 resource.remark；5=清理测试要素。
     */
    private void remarkTestMenu(ElementModule module, long timeout) {
        while (true) {
            Utils.printTitle("remark 回归测试");
            printRemarkTestState();
            console("\n"
                    + "  1. 选择测试要素（已有 elementId / 回车新建）\n"
                    + "  2. updateElement 设置 remark\n"
                    + "  3. listElementHierarchy 查询同一要素\n"
                    + "  4. 检查返回的 resource.remark\n"
                    + "  5. 清理本次新建的测试要素\n"
                    + "  q. 返回\n");
            int choice = Utils.readIntInRange(input, "请选择(1-5, q 返回): ", 1, 5);
            if (choice == Utils.QUIT) {
                DemoLog.info(name(), "退出 remark 回归测试菜单");
                return;
            }
            try {
                switch (choice) {
                    case 1: remarkStepSelect(module, timeout); break;
                    case 2: remarkStepUpdate(module, timeout); break;
                    case 3: remarkStepList(module, timeout); break;
                    case 4: remarkStepCheck(module); break;
                    case 5: remarkStepCleanup(module, timeout); break;
                    default: break;
                }
            } catch (Exception e) {
                console("   结果 -> [异常] " + Utils.describe(e));
                DemoLog.error(name(), "remark 回归步骤异常: " + Utils.describe(e), e);
            }
        }
    }

    private void printRemarkTestState() {
        if (remarkTestElementId == null) {
            console("  当前未选择测试要素");
            return;
        }
        console("  测试要素: id=" + remarkTestElementId
                + (remarkTestGroupId == null ? "" : ", groupId=" + remarkTestGroupId)
                + (remarkTestCreatedByDemo ? " (本 demo 新建)" : " (已有要素)"));
        console("  写入 remark=" + showRemark(remarkTestTargetRemark)
                + " | SDK 返回=" + showRemark(remarkTestSdkRemark));
    }

    private static String showRemark(String value) {
        return value == null ? "<未设置>" : "[" + value + "]";
    }

    /** 第 1 步：选择已有要素，或创建一个新的测试要素。 */
    private void remarkStepSelect(ElementModule module, long timeout) {
        console("\n   [准备] 选择测试要素 ...");
        console("   输入已有 elementId = 复用该要素；直接回车 = 新建测试要素");
        String inputId = Utils.readLineEcho(input, "   elementId(回车=新建, q 取消): ");
        if (inputId == null || inputId.equalsIgnoreCase("q")) {
            hint("已取消选择测试要素");
            return;
        }
        resetRemarkTestState();
        inputId = inputId.trim();
        if (!inputId.isEmpty()) {
            remarkTestElementId = inputId;
            console("   [准备] 使用已有要素 elementId=" + remarkTestElementId);
        } else {
            String newId = createPointElement(module, timeout, "remark-regress");
            if (newId == null) {
                console("   [准备] 创建失败");
                return;
            }
            remarkTestElementId = newId;
            remarkTestCreatedByDemo = true;
            console("   [准备] 已创建 elementId=" + newId);
        }
        console("   [准备] 请继续执行 [2] updateElement 设置 remark");
    }

    private void resetRemarkTestState() {
        remarkTestElementId = null;
        remarkTestGroupId = null;
        remarkTestTargetRemark = null;
        remarkTestSdkRemark = null;
        remarkTestGeometryType = null;
        remarkTestCoordinates = null;
        remarkTestQueried = false;
        remarkTestCreatedByDemo = false;
    }

    /** 第 2 步：updateElement 设置 remark。 */
    private void remarkStepUpdate(ElementModule module, long timeout) {
        if (!requireRemarkElement()) {
            return;
        }
        String inputRemark = Utils.readLineEcho(input, "   请输入要写入的 remark(q 取消): ");
        if (inputRemark == null || inputRemark.equalsIgnoreCase("q")) {
            hint("已取消写入 remark");
            return;
        }
        inputRemark = inputRemark.trim();
        if (inputRemark.isEmpty()) {
            hint("remark 不能为空");
            return;
        }
        remarkTestTargetRemark = inputRemark;
        remarkTestSdkRemark = null;
        remarkTestQueried = false;

        console("\n   [2/5] updateElement 设置 remark=" + remarkTestTargetRemark);
        final ElementUpdateParams update = new ElementUpdateParams(remarkTestElementId);
        update.setRemark(remarkTestTargetRemark);
        final Await<FlightHubError> await = new Await<>(name() + ".remark.update");
        Outcome o = runOutcome("updateElement 设置 remark", timeout, await, () ->
                module.updateElement(update, new FlightHubCallback.ElementUpdateElementCallback() {
                    @Override
                    public void onResult(FlightHubError error, ElementUpdateResult result) {
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            console("   [2/5] 结果: updateElement 成功, 无错误码");
            console("   [2/5] 请继续执行 [3] listElementHierarchy 查询同一要素");
        }
    }

    /** 第 3 步：按 elementUuid 或 groupId 查询同一要素。 */
    private void remarkStepList(ElementModule module, long timeout) {
        if (!requireRemarkElement()) {
            return;
        }
        console("\n   [3/5] listElementHierarchy 查询同一要素 ...");
        String mode = Utils.readLineEcho(input,
                "   定位方式: 回车=elementUuid, 输入 2=groupId (q 取消): ");
        if (mode == null || mode.equalsIgnoreCase("q")) {
            hint("已取消查询");
            return;
        }
        mode = mode.trim();

        final ElementListHierarchyParams params = new ElementListHierarchyParams();
        String queryDesc;
        if ("2".equals(mode)) {
            String inputGroupId = Utils.readLineEcho(input, "   请输入 groupId(q 取消): ");
            if (inputGroupId == null || inputGroupId.equalsIgnoreCase("q")) {
                hint("已取消查询");
                return;
            }
            inputGroupId = inputGroupId.trim();
            if (inputGroupId.isEmpty()) {
                hint("groupId 不能为空");
                return;
            }
            params.setGroupId(inputGroupId);
            queryDesc = "group_id=" + inputGroupId;
        } else {
            params.setElementUuid(remarkTestElementId);
            queryDesc = "element_uuid=" + remarkTestElementId;
        }

        console("   请求: GET /pf-map/api/v1/elements/lists?" + queryDesc);
        final Await<FlightHubError> await = new Await<>(name() + ".remark.list");
        final AtomicReference<ElementListHierarchyResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("listElementHierarchy 查询同一要素", timeout, await, () ->
                module.listElementHierarchy(params,
                        new FlightHubCallback.ElementListHierarchyCallback() {
                            @Override
                            public void onResult(FlightHubError error, ElementListHierarchyResult result) {
                                ref.set(result);
                                await.set(error);
                            }
                        })
        );
        if (o != Outcome.SUCCESS) {
            console("   [3/5] listElementHierarchy 未成功");
            return;
        }

        ElementListHierarchyResult listResult = ref.get();
        FoundElement found = findElementById(
                listResult == null ? null : listResult.getList(), remarkTestElementId);
        if (found == null) {
            console("   [3/5] 未在返回结果中找到要素 elementId=" + remarkTestElementId);
            DemoLog.error(name(), "remark 回归: 未找到要素 elementId=" + remarkTestElementId);
            return;
        }
        remarkTestGroupId = found.groupId;
        remarkTestSdkRemark = found.remark;
        remarkTestGeometryType = found.geometryType;
        remarkTestCoordinates = found.coordinates;
        remarkTestQueried = true;

        console("   [3/5] 查询成功");
        console("   [3/5] ElementGroupOutput.id(groupId)=" + found.groupId);
        console("   [3/5] ElementItem.id=" + remarkTestElementId);
        console("   [3/5] resource.remark=" + showRemark(found.remark));
        console("   [3/5] 请继续执行 [4] 检查 resource.remark");
    }

    /** 第 4 步：检查 SDK 返回的 remark 是否等于 updateElement 写入值。 */
    private void remarkStepCheck(ElementModule module) {
        if (!requireRemarkElement()) {
            return;
        }
        if (!remarkTestQueried) {
            hint("请先执行 [3] listElementHierarchy 查询同一要素");
            return;
        }
        if (remarkTestTargetRemark == null) {
            hint("请先执行 [2] updateElement 设置 remark");
            return;
        }

        boolean isNull = remarkTestSdkRemark == null || remarkTestSdkRemark.isEmpty();
        boolean match = remarkTestTargetRemark.equals(remarkTestSdkRemark);
        console("\n   [4/5] 检查返回的 resource.remark");
        console("   updateElement 写入值: " + showRemark(remarkTestTargetRemark));
        console("   listElementHierarchy 返回值: " + showRemark(remarkTestSdkRemark));
        console("   remark 是否为空: " + (isNull ? "是 (复现问题)" : "否"));
        console("   几何数据: type=" + remarkTestGeometryType
                + " coordinates=" + remarkTestCoordinates);
        if (match) {
            console("   结果: PASS (SDK 返回了 updateElement 写入的 remark)");
            DemoLog.info(name(), "remark 回归 PASS: elementId=" + remarkTestElementId
                    + ", target=" + remarkTestTargetRemark
                    + ", sdk=" + remarkTestSdkRemark);
        } else {
            console("   结果: FAIL (SDK 返回 remark 与写入值不一致或为空)");
            DemoLog.error(name(), "remark 回归 FAIL: elementId=" + remarkTestElementId
                    + ", target=" + remarkTestTargetRemark
                    + ", sdk=" + remarkTestSdkRemark);
        }
    }

    /** 第 5 步：清理本 demo 新建的测试要素。 */
    private void remarkStepCleanup(ElementModule module, long timeout) {
        if (remarkTestElementId == null) {
            hint("当前没有测试要素");
            return;
        }
        if (!remarkTestCreatedByDemo) {
            hint("当前要素不是本 demo 新建的，不自动删除: " + remarkTestElementId);
            return;
        }
        if (!confirmDangerous("删除本 demo 新建的测试要素 " + remarkTestElementId)) {
            return;
        }
        final ElementDeleteParams params = new ElementDeleteParams(remarkTestElementId);
        final Await<FlightHubError> await = new Await<>(name() + ".remark.delete");
        Outcome o = runOutcome("清理: deleteElement", timeout, await, () ->
                module.deleteElement(params, new FlightHubCallback.ElementDeleteElementCallback() {
                    @Override
                    public void onResult(FlightHubError error, ElementDeleteResult result) {
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            console("   已删除测试要素 elementId=" + remarkTestElementId);
            DemoLog.info(name(), "remark 回归清理完成: elementId=" + remarkTestElementId);
            resetRemarkTestState();
        }
    }

    private boolean requireRemarkElement() {
        if (remarkTestElementId == null) {
            hint("请先执行 [1] 选择测试要素");
            return false;
        }
        return true;
    }

    private FoundElement findElementById(List<ElementGroupOutput> groups, String elementId) {
        if (groups == null) {
            return null;
        }
        for (ElementGroupOutput group : groups) {
            FoundElement found = findElementInGroup(group, elementId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private FoundElement findElementInGroup(ElementGroupOutput group, String elementId) {
        List<ElementItem> elements = group.getElements();
        if (elements != null) {
            for (ElementItem item : elements) {
                if (elementId.equals(item.getId())) {
                    ElementResourceData resource = item.getResource();
                    String geometryType = "";
                    String coordinates = "";
                    if (resource != null && resource.getContent() != null
                            && resource.getContent().getGeometry() != null) {
                        geometryType = resource.getContent().getGeometry().getType();
                        coordinates = resource.getContent().getGeometry().getCoordinates();
                    }
                    return new FoundElement(group.getId(), resource == null ? null : resource.getRemark(),
                            geometryType, coordinates);
                }
            }
        }
        if (group.getChildren() != null) {
            for (ElementGroupOutput child : group.getChildren()) {
                FoundElement found = findElementInGroup(child, elementId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static final class FoundElement {
        final String groupId;
        final String remark;
        final String geometryType;
        final String coordinates;

        FoundElement(String groupId, String remark, String geometryType, String coordinates) {
            this.groupId = groupId;
            this.remark = remark;
            this.geometryType = geometryType;
            this.coordinates = coordinates;
        }
    }

    // ==================== [2] 更新要素 ====================

    private void updateElement(ElementModule module, long timeout) {
        String id = Utils.readLineEcho(input, "   请输入 elementId(q 取消): ");
        if (id == null || id.equalsIgnoreCase("q")) {
            hint("已取消更新");
            return;
        }
        if (id.isEmpty()) {
            hint("elementId 不能为空");
            return;
        }

        ElementResourceContent content = new ElementResourceContent();
        content.setType("Feature");
        ElementProperties props = content.getProperties();
        props.setColor("#00FF00");
        props.setClampToGround(true);   // #56 关键
        ElementGeometry geometry = content.getGeometry();
        geometry.setType("Point");
        geometry.setCoordinates("[114.06,22.55,12.6]");

        final ElementUpdateParams p = new ElementUpdateParams(id);
        p.setName("demo-element-updated");
        p.setContent(content);
        console("   > id=" + p.getId() + " name=demo-element-updated clampToGround=true");

        final Await<FlightHubError> await = new Await<>(name() + ".updateElement");
        final AtomicReference<ElementUpdateResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("更新要素", timeout, await, () ->
                module.updateElement(p, new FlightHubCallback.ElementUpdateElementCallback() {
                    @Override
                    public void onResult(FlightHubError error, ElementUpdateResult result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            ElementUpdateResult r = ref.get();
            console("   已更新: id=" + (r == null ? "null" : r.getId()));
            console("   请核对日志 element_update body: 键应为 clampToGround");
        }
    }

    // ==================== [3] 查询元素层级列表 ====================

    private void listElementHierarchy(ElementModule module, long timeout) {
        final ElementListHierarchyParams p = new ElementListHierarchyParams();
        final Await<FlightHubError> await = new Await<>(name() + ".listElementHierarchy");
        final AtomicReference<ElementListHierarchyResult> ref = new AtomicReference<>();
        Outcome o = runOutcome("查询元素层级", timeout, await, () ->
                module.listElementHierarchy(p, new FlightHubCallback.ElementListHierarchyCallback() {
                    @Override
                    public void onResult(FlightHubError error, ElementListHierarchyResult result) {
                        ref.set(result);
                        await.set(error);
                    }
                })
        );
        if (o == Outcome.SUCCESS) {
            ElementListHierarchyResult r = ref.get();
            List<ElementGroupOutput> list = r == null ? null : r.getList();
            int size = list == null ? 0 : list.size();
            console("   图层数=" + size);
            if (list != null) {
                console("   [树结构]");
                for (int i = 0; i < list.size(); i++) {
                    printTree(list.get(i), 0, i + 1);
                }
                for (int i = 0; i < list.size(); i++) {
                    ElementGroupOutput g = list.get(i);
                    console("   [" + (i + 1) + "] group=" + g.getId() + " name=" + g.getName());
                    List<ElementItem> elements = g.getElements();
                    if (elements != null) {
                        for (int j = 0; j < elements.size(); j++) {
                            ElementItem e = elements.get(j);
                            ElementResourceData rd = e.getResource();
                            if (rd == null || rd.getContent() == null) {
                                continue;
                            }
                            if (e.getName() != null
                                    && (e.getName().contains("breadcrumbs")
                                        || e.getName().startsWith("demo-element"))) {
                                String breadCrumbs = rd.getContent().getGeometry() != null
                                        ? rd.getContent().getGeometry().getBreadCrumbs() : "";
                                console("       element=" + e.getId()
                                    + " name=" + e.getName()
                                    + " breadCrumbs=" + breadCrumbs);
                            }
                            ElementProperties props = rd.getContent().getProperties();
                            if (props != null) {
                                console("       element=" + e.getId()
                                    + " icon=" + props.getIcon()
                                    + " iconUrl=" + props.getIconUrl()
                                    + " width=" + props.getWidth()
                                    + " startCap=" + props.getStartCap()
                                    + " endCap=" + props.getEndCap());
                            }
                            if (rd.getContent().getMediaResources() != null
                                    && rd.getContent().getMediaResources().getImages() != null) {
                                for (ElementMediaImage img : rd.getContent().getMediaResources().getImages()) {
                                    console("       image objectKey=" + img.getObjectKey()
                                        + " url=" + img.getUrl()
                                        + " previewUrl=" + img.getPreviewUrl()
                                        + " webpUrl=" + img.getWebpUrl()
                                        + " fileType=" + img.getFileType()
                                        + " subFileType=" + img.getSubFileType());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void printTree(ElementGroupOutput g, int depth, int index) {
        StringBuilder pad = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            pad.append("  ");
        }
        int childCount = g.getChildren() == null ? 0 : g.getChildren().size();
        int elementCount = g.getElements() == null ? 0 : g.getElements().size();
        console(pad + "[" + index + "] group=" + g.getId()
                + " name=" + g.getName()
                + " pid=" + g.getParentId()
                + " children=" + childCount
                + " elements=" + elementCount);
        if (g.getChildren() != null) {
            for (int i = 0; i < g.getChildren().size(); i++) {
                printTree(g.getChildren().get(i), depth + 1, i + 1);
            }
        }
    }
}
