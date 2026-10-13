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

package com.dji.fh.kernel.impl.auth;

import com.dji.fh.kernel.spi.auth.PermissionEvaluator;
import com.dji.fh.kernel.spi.auth.PublicEndpoint;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import com.dji.fh.kernel.spi.auth.RequirePermission;
import com.dji.fh.kernel.spi.auth.RequireRole;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * 实施 {@link RequireRole} / {@link RequirePermission} / {@link RequireAuthenticated} 三个注解的切面。
 *
 * <p>支持方法级与类级注解。类级注解作用于该类所有 public 方法；方法级覆盖类级。
 * 方法级 {@link PublicEndpoint} 可覆盖类级 {@link RequireAuthenticated}，标记登录/注册等公开入口。
 *
 * <p>校验失败抛 {@link AccessDeniedException}（unchecked）。ISV 可在 {@code @ControllerAdvice}
 * 中捕获映射为 HTTP 403。
 *
 * <p>当前 {@link SecurityContext} 通过 {@link SecurityContextHolder#current()} 获取
 * （永不为 null；未认证时为 {@link com.dji.fh.kernel.spi.auth.NoOpSecurityContext}）。
 *
 * @author DJI DevKit Team
 */
@Aspect
public class AuthorizationAspect {

    private final PermissionEvaluator permissionEvaluator;

    /**
     * @param permissionEvaluator 用于 {@link RequirePermission} 校验
     */
    public AuthorizationAspect(PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = Objects.requireNonNull(
                permissionEvaluator, "permissionEvaluator 不能为空");
    }

    /**
     * 单一切点拦截带有 3 个注解之一（方法级或类级）的执行点。
     *
     * <p>不在 pointcut expression 里直接绑定注解参数 —— 那样会因为 4 种 @within/@annotation 组合
     * 产生 null binding 问题；改在方法体内通过反射读注解，更稳。
     *
     * @param pjp ProceedingJoinPoint
     * @return 原方法返回值
     * @throws Throwable 鉴权失败或原方法异常
     */
    @Around("@within(com.dji.fh.kernel.spi.auth.RequireRole) "
            + "|| @annotation(com.dji.fh.kernel.spi.auth.RequireRole) "
            + "|| @within(com.dji.fh.kernel.spi.auth.RequirePermission) "
            + "|| @annotation(com.dji.fh.kernel.spi.auth.RequirePermission) "
            + "|| @within(com.dji.fh.kernel.spi.auth.RequireAuthenticated) "
            + "|| @annotation(com.dji.fh.kernel.spi.auth.RequireAuthenticated)")
    public Object check(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Class<?> targetClass = pjp.getTarget().getClass();

        SecurityContext ctx = SecurityContextHolder.current();

        // @PublicEndpoint（方法级优先，回落类级）：标记公开端点，跳过 RequireAuthenticated
        // 用于类级 @RequireAuthenticated 下放行登录/注册等入口
        boolean isPublic = findAnnotation(method, targetClass, PublicEndpoint.class) != null;

        // @RequireAuthenticated（方法级优先，回落类级）；被 @PublicEndpoint 覆盖时跳过
        if (!isPublic && findAnnotation(method, targetClass, RequireAuthenticated.class) != null) {
            requireAuthenticated(ctx);
        }

        // @RequireRole（公开端点一般不叠加角色校验，故不联动 isPublic）
        RequireRole roleAnno = findAnnotation(method, targetClass, RequireRole.class);
        if (roleAnno != null) {
            requireRole(ctx, roleAnno);
        }

        // @RequirePermission
        RequirePermission permAnno = findAnnotation(method, targetClass, RequirePermission.class);
        if (permAnno != null) {
            requirePermission(ctx, permAnno);
        }

        return pjp.proceed();
    }

    private static <A extends java.lang.annotation.Annotation> A findAnnotation(
            Method method, Class<?> targetClass, Class<A> annotationType) {
        A m = method.getAnnotation(annotationType);
        if (m != null) {
            return m;
        }
        return targetClass.getAnnotation(annotationType);
    }

    private static void requireAuthenticated(SecurityContext ctx) {
        if (!ctx.isAuthenticated()) {
            throw new AccessDeniedException("需要已认证用户，当前匿名");
        }
    }

    private static void requireRole(SecurityContext ctx, RequireRole anno) {
        String[] required = anno.value();
        if (required.length == 0) {
            return;
        }
        boolean ok;
        if (anno.requireAll()) {
            ok = Arrays.stream(required).allMatch(ctx::hasRole);
        } else {
            ok = Arrays.stream(required).anyMatch(ctx::hasRole);
        }
        if (!ok) {
            throw new AccessDeniedException(
                    "缺少所需角色: " + Arrays.toString(required)
                            + "（requireAll=" + anno.requireAll() + "）");
        }
    }

    private void requirePermission(SecurityContext ctx, RequirePermission anno) {
        String[] required = anno.value();
        if (required.length == 0) {
            return;
        }
        for (String permission : required) {
            if (!permissionEvaluator.hasPermission(ctx, permission)) {
                throw new AccessDeniedException("缺少所需权限: " + permission);
            }
        }
    }
}
