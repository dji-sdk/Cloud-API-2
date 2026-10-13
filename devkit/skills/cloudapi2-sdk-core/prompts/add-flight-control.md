# 添加飞行控制功能

## 目标
实现手动飞行控制：起飞、返航、取消返航、急停、指点飞行。
> ⚠️ 协议中不存在独立的 `landing()` 接口；降落通过 `returnHome()` 实现（无人机返回机场后自动降落）。

## 涉及 SDK API
- `FlightHubSDK.getDroneInstance(sn)` → Drone
- `drone.getControlAuthModule().acquireFlightControlAuthority(callback)` — 获取控制权
- `drone.getControlAuthModule().releaseFlightControlAuthority(callback)` — 释放控制权
- `drone.getTakeoffModule().takeoff(params, callback)` — 起飞
- `drone.getFlightControlModule().returnHome(callback)` — 返航（无人机返回机场并自动降落）
- `drone.getFlightControlModule().returnHomeCancel(callback)` — 取消返航
- `drone.getFlightControlModule().drcEmergencyStop(callback)` — 急停悬停
- `drone.getFlightControlModule().drcEmergencyLanding(callback)` — 紧急降落
- `drone.getFlightControlModule().drcForceLanding(callback)` — 强制降落（不可逆）

## 必须遵守的规则
- ✅ 规则 4：**必须先 acquire，用完 release**（即使指令失败也要释放）
- ✅ 规则 5：getDroneInstance null 检查
- ✅ 规则 2：所有回调检查 errorCode

## 实现步骤

1. **创建 FlightControlService**
   - 封装 `safeFlightCommand` 通用模板（acquire → command → release）
   - 实现 takeoff / returnHome / returnHomeCancel / drcEmergencyStop / drcEmergencyLanding / drcForceLanding
   - 每个方法都使用通用模板，确保释放

2. **创建 Controller**
   - POST `/api/drone/{sn}/takeoff` — 起飞
   - POST `/api/drone/{sn}/return-home` — 返航
   - POST `/api/drone/{sn}/return-home/cancel` — 取消返航
   - POST `/api/drone/{sn}/emergency-stop` — 急停悬停
   - POST `/api/drone/{sn}/emergency-landing` — 紧急降落
   - POST `/api/drone/{sn}/force-landing` — 强制降落（不可逆）

3. **起飞参数（TakeoffParams）**
   - securityTakeoffHeight: 8~1500m
   - maxSpeed: >0 m/s
   - rthAltitude: 15~1500m（返航高度）
   - targetHeight: 2~10000m
   - targetLatitude / targetLongitude
   - outOfControlAction: HOVER / RETURN_HOME / CONTINUE

## ⚠️ 安全警告（输出前置条件——规则 6）

**在输出任何飞控代码之前**，必须先向用户输出以下警告块：

```
⚠️ **危险操作警告**
- 操作类型：[takeoff / returnHome / drcEmergencyStop / drcEmergencyLanding / drcForceLanding / flyToPoint]
- 不可逆性：飞控指令发出后立即执行，无法通过代码回滚
- 物理影响：此代码将控制真实无人机执行物理动作，操作失误可能导致坠机或财产损失
- 生产建议：务必实现前端二次确认弹窗（confirm dialog）；建议支持 dry_run 参数做预校验
```

如果用户已在本轮对话中确认知晓风险，可省略。**禁止跳过此步骤直接输出代码。**

## 常见错误
- ❌ 不调 acquireFlightControlAuthority 直接发指令
- ❌ 不通过 module getter 访问 API（如 `drone.returnHome()` 而非 `drone.getFlightControlModule().returnHome()`）
- ❌ release 放在 acquire 回调外面（异步执行顺序错误）
- ❌ 指令失败后不释放控制权（阻塞其他操作方）
- ❌ 没有前端确认步骤就直接执行飞控
- ❌ 实现 `landing()` 方法（该方法不存在于协议中）
- ❌ 直接输出飞控代码而不附带危险操作警告（违反规则 6）
- ❌ 使用旧版 `emergencyStop()` 方法（v3.3.x 已拆分为 drcEmergencyStop/drcEmergencyLanding/drcForceLanding）
