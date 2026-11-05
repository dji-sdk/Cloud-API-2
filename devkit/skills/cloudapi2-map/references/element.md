# ElementModule — 地图元素（点/线/面/圆）

## 模块位置与心智模型

`ElementModule` 挂在 **Workspace** 上：

```java
ElementModule element = workspace.getElementModule();
```

**11 个顶层方法**：5 个 HTTP（增删改查 + 可见性）+ 3 对 WS 订阅/取消（create/update/delete）。

元素是 DJI Cloud API 2 地图上的自定义标注（点/线/面/圆），按**分组（group）树形层级**组织，支持从 App 上传、地图手绘、模型对比、KML 导入等多种来源。

## 几何模型（和 SafetyService 的不一样，别混用）

```
ElementResourceContent
├── type:       固定 "Feature"
├── properties: ElementProperties { color, clampToGround, is3d, isTransparent, fontSize, icon }
├── geometry:   ElementGeometry { type, coordinates, radius, breadCrumbs }
└── mediaResources: ElementMediaResources { images: List<ElementMediaImage> }
```

`resourceType`（int）与 `ElementGeometry.type`（GeoJSON 字符串）的对应关系——**注意编号不连续，源码里实际只定义了 0/1/2/7，3~6 未出现**（不要据此猜测它们是预留给某种未来类型，源码没有相关注释佐证，只是实际没用到）：

| resourceType | geometry.type | 说明 |
|---:|---|---|
| 0 | `"Point"` | 点 |
| 1 | `"LineString"` | 线 |
| 2 | `"Polygon"` | 面 |
| 7 | `"Point"` + `radius` | 圆（复用 Point 坐标 + 半径字段，**不是独立的 "Circle" 类型**） |

`ElementGeometry.coordinates` 是**原始 JSON 字符串**（不是强类型对象），按 `type` 解析：

| type | coordinates 内容 | 示例 |
|---|---|---|
| `Point` | `[lng, lat, alt]` | `"[114.06, 22.55, 50]"` |
| `LineString` | `[[lng,lat,alt], ...]` | `"[[114.06,22.55,50],[114.07,22.56,50]]"` |
| `Polygon` | `[[[lng,lat,alt], ...]]` | 外层数组是环（ring），当前只用第一个环 |
| 圆（resourceType=7） | 同 Point，另有 `radius`（米） | — |

> ⚠️ `coordinates` 字段类型是 `String`，必须自己 `JSON.toJSONString(...)` 序列化后再塞进去，SDK 不提供强类型的坐标构建器。
> ⚠️ 圆复用 `"Point"` 类型字符串，**不要**写成 `"Circle"` —— `ElementModule` 没有这个枚举值（这是和 `SafetyServiceModule` 飞行区模型最容易混的地方）。

`ElementProperties`：
- `color`：HEX 字符串，如 `"#2D8CF0"`
- `clampToGround` / `is3d` / `isTransparent`：布尔，渲染相关
- `fontSize`、`icon`：标注文字/图标，均可选

## 1. 创建元素

```java
ElementGeometry geometry = new ElementGeometry();
geometry.setType("Point");
geometry.setCoordinates(JSON.toJSONString(List.of(114.06, 22.55, 50)));
// radius 是原始类型 double（默认 0），不能传 null；普通点不调用即可，圆形时传米数（> 0）

ElementProperties props = new ElementProperties();
props.setColor("#2D8CF0");
props.setClampToGround(true);

ElementResourceContent content = new ElementResourceContent();
content.setType("Feature");
content.setProperties(props);
content.setGeometry(geometry);

// 便捷构造：(resourceContent, resourceType, name)
ElementCreateParams params = new ElementCreateParams(content, 0, "起飞点");

element.createElement(params, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
    // result: ElementCreateResult { id }
});
```

`ElementCreateParams` 全字段：`id`（新建时不传，服务端生成）、`source`（创建来源，见下表）、`resourceContent`、`resourceType`（0/1/2/7）、`resourceRemark`、`name`、`elementSource`、`desc`。

`source` 枚举（`ElementItem.source` 同一套，查询时用于区分来源）：

| source | 含义 |
|---:|---|
| 0 | 历史数据 / App 上传 |
| 1 | 地图创建 |
| 2 | 模型对比创建 |
| 3 | KML 导入 |

## 2. 更新元素

```java
ElementUpdateParams params = new ElementUpdateParams();
params.setId(elementId);          // 必填
params.setName("新名称");
params.setContent(newContent);    // 需要改几何/样式时传
params.setDisplay(1);             // Integer，null = 不更新该字段；1=显示 0=隐藏
params.setTargetLayerId(groupId); // 非空 = 把元素移动到该分组
params.setOrders(List.of(orderItem)); // 批量调整同组元素排序

element.updateElement(params, (error, result) -> {
    // result: ElementUpdateResult { id }
});
```

> ⚠️ `display` 是包装类型 `Integer` 不是 `int`：传 `null` 表示"不更新可见性"，传 `0`/`1` 才会真正改写。如果只想改名字，`display` 保持 `null`，不要图省事传 `0`。
> ⚠️ `targetLayerId` 非空会触发"移动元素到目标分组"，只改名字/样式时不要带这个字段。

## 3. 查询层级结构

```java
ElementListHierarchyParams params = new ElementListHierarchyParams();
params.setIsDistributed(false);   // 是否只看共享/分发的分组
params.setType(null);             // 按分组类型过滤，见下表；不传 = 全部

element.listElementHierarchy(params, (error, result) -> {
    for (ElementGroupOutput group : result.getList()) {
        // group.getTriState(): "all" 全显 / "none" 全隐 / "half" 部分显（是显隐状态，不是选中状态）
        // group.getElements(): 本组元素
        // group.getChildren(): 子分组（递归结构）
    }
});
```

`ElementGroupOutput.type` 枚举：

| type | 含义 |
|---:|---|
| 0 | 普通分组 |
| 1 | 默认分组 |
| 2 | 共享分组 |
| 3 | C2C 分组 |
| 4 | 视锥分组 |
| 5 | DataHub 分组 |
| 6 | 自动派遣分组 |
| 7 | 大疆行业 APP 分组 |

> ⚠️ `ElementGroupOutput` 是**递归结构**（`children: List<ElementGroupOutput>`），遍历要用递归或显式栈，不能只读第一层。
> ⚠️ `ElementItem.notify` 为 `true` 代表这是 **C2C 图层**的元素，前端渲染/订阅逻辑可能需要特殊处理（如跳过本地已知变更，避免和 C2C 回源冲突）。

## 4. 设置可见性

```java
ElementSetVisibilityParams params = new ElementSetVisibilityParams();
params.setId(elementId);
params.setDisplay(1);   // 必填：1=显示 0=隐藏

element.setElementVisibility(params, (error, result) -> {
    // result: ElementSetVisibilityResult { id }
});
```

> 和 `updateElement` 的区别：这是单独的轻量接口，只改可见性，不会连带其它字段。批量只改可见性时优先用这个，不要为了省一次调用去拼 `updateElement`。

## 5. 删除元素

```java
ElementDeleteParams params = new ElementDeleteParams();
params.setId(elementId);
params.setFromBatchDelete(false);   // true = 批量删除场景，跳过单条分发通知

element.deleteElement(params, (error, result) -> {
    // result: ElementDeleteResult（具体字段回调里可空，删除成功以 error 为准）
});
```

> ⚠️ javadoc 明确：`deleteElement` **仅限基础几何类型（点/线/面/圆，即 resourceType 0/1/2/7）**。分组、特殊来源（C2C/视锥/DataHub 等）元素不保证能通过本接口删除，遇到报错先确认元素的 `resourceType` 和所属分组 `type`。
> ⚠️ `fromBatchDelete=true` 时服务端会跳过单条的 WS 分发通知（`topic_element_delete` 不会为这条单独触发）——批量删除后如果前端依赖 WS 事件刷新列表，记得改成删除完成后主动拉取 `listElementHierarchy`。

## 6. WebSocket 订阅（创建/更新/删除）

三组订阅完全对称，**回调都是单参数，没有 FlightHubError**：

```java
int createListenId = element.listenElementCreate(msg -> {
    // ElementCreateMsg { id, groupId, name, display }
});
int updateListenId = element.listenElementUpdate(msg -> {
    // ElementUpdateMsg { id, groupId, name, display }
});
int deleteListenId = element.listenElementDelete(msg -> {
    // ElementDeleteMsg { id, groupId }
});

// 关闭时
element.cancelListenElementCreate(createListenId);
element.cancelListenElementUpdate(updateListenId);
element.cancelListenElementDelete(deleteListenId);
```

topic：`topic_element_create` / `topic_element_update` / `topic_element_delete`。

## 必须遵守的规则

- ✅ 规则 2：HTTP 回调检查 `error.getErrorCode() == SUCCESS`
- ✅ 规则 3：三个 WS listenId 必须分别保存，`@PreDestroy` 中全部 cancel
- ✅ 规则 5：`getWorkspaceInstance()` / `getElementModule()` 判空
- ⚠️ 元素操作是 **workspace 级地图数据管理**，不需要 `acquireFlightControlAuthority`
- ⚠️ WS 回调（`ElementCreateCallback`/`UpdateCallback`/`DeleteCallback`）**没有 error 参数**，不要写成 `(error, msg) ->`

## 常见错误

- ❌ 把 `listenElementCreate/Update/Delete` 的回调写成 `(error, msg)` → 编译失败，WS 推送是单参数
- ❌ 圆形元素写 `geometry.setType("Circle")` → 不存在该枚举，圆复用 `"Point"` + `radius`
- ❌ `resourceType` 按连续枚举猜测 3/4/5/6 存在 → 实际只有 0/1/2/7，中间是空缺
- ❌ `coordinates` 当强类型对象赋值 → 字段是 `String`，必须先序列化成 JSON 字符串
- ❌ `updateElement` 想只改名字却把 `display` 设成 `0` → 会连带把元素隐藏；只改名字时 `display` 留 `null`
- ❌ 对共享/C2C/视锥等特殊分组下的元素调用 `deleteElement` 并假设一定成功 → javadoc 限定仅基础几何类型
- ❌ 批量删除后指望 `topic_element_delete` 逐条推送 → `fromBatchDelete=true` 时跳过单条通知，需主动刷新列表
- ❌ 遍历 `listElementHierarchy` 结果只读第一层 `elements` → `children` 是递归分组，忽略会漏掉子分组元素
