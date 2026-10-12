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

package com.dji.fh.kernel.impl.manifest;

import com.dji.fh.kernel.spi.manifest.ModuleManifest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 扫描 classpath 上的 module.yaml 并解析为 {@link ModuleManifest} 列表。
 *
 * <p>默认扫描路径 {@code classpath*:META-INF/fh-modules/*.yaml}；可在构造时覆盖。
 * 行为 fail-fast：任意一份失败 → 立即抛 {@link ManifestException}，
 * Spring context 启动中断（D2-4）。
 *
 * <p>校验项：
 * <ul>
 *   <li>YAML 语法可解析</li>
 *   <li>必填字段非空（schemaVersion / id / version / name / description / metadata / requires / toggle）</li>
 *   <li>{@code requires.kernel} 版本范围与当前 Kernel 版本兼容</li>
 *   <li>{@code id} 全局唯一</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public class ModuleManifestLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleManifestLoader.class);

    /**
     * 默认 classpath 扫描 pattern。
     */
    public static final String DEFAULT_PATTERN = "classpath*:META-INF/fh-modules/*.yaml";

    /**
     * 当前 Kernel 版本。校验 requires.kernel 时用。
     * v0.1 硬编码；后续可改为从 Implementation-Version manifest 读。
     */
    public static final String CURRENT_KERNEL_VERSION = "0.1.0";

    private final ResourcePatternResolver resolver;
    private final String pattern;
    private final ObjectMapper yamlMapper;

    /**
     * @param resolver Spring 资源解析器（Spring Boot 一般提供 ApplicationContext 本身）
     */
    public ModuleManifestLoader(ResourcePatternResolver resolver) {
        this(resolver, DEFAULT_PATTERN);
    }

    /**
     * @param resolver Spring 资源解析器
     * @param pattern  自定义扫描 pattern（测试 / 特殊路径用）
     */
    public ModuleManifestLoader(ResourcePatternResolver resolver, String pattern) {
        this.resolver = Objects.requireNonNull(resolver, "resolver 不能为空");
        this.pattern = Objects.requireNonNull(pattern, "pattern 不能为空");
        this.yamlMapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 便利构造器：手动 new 一个 PathMatchingResourcePatternResolver。
     */
    public static ModuleManifestLoader withDefaults() {
        return new ModuleManifestLoader(new PathMatchingResourcePatternResolver());
    }

    /**
     * 扫描 + 解析 + 校验全部 module.yaml。
     *
     * @return 加载成功的 manifest 列表（按解析顺序；可能为空列表）
     * @throws ManifestException 任意一份失败立刻抛
     */
    public List<ModuleManifest> loadAll() {
        Resource[] resources;
        try {
            resources = resolver.getResources(pattern);
        } catch (IOException e) {
            throw new ManifestException("扫描 classpath pattern " + pattern + " 失败", e);
        }
        LOG.info("发现 {} 份 module.yaml（pattern={}）", resources.length, pattern);

        List<ModuleManifest> manifests = new ArrayList<>(resources.length);
        Map<String, String> idToLocation = new HashMap<>();
        for (Resource r : resources) {
            String location = describe(r);
            ModuleManifest m = parseOne(r, location);
            validate(m, location);
            String existing = idToLocation.get(m.id());
            if (existing != null) {
                throw new ManifestException(
                        "module id 冲突: '" + m.id() + "' 同时声明于 " + existing + " 与 " + location);
            }
            idToLocation.put(m.id(), location);
            manifests.add(m);
        }
        return manifests;
    }

    private ModuleManifest parseOne(Resource r, String location) {
        try (InputStream in = r.getInputStream()) {
            return yamlMapper.readValue(in, ModuleManifest.class);
        } catch (IOException e) {
            throw new ManifestException("解析 " + location + " 失败: " + e.getMessage(), e);
        }
    }

    private void validate(ModuleManifest m, String location) {
        requireNonBlank(m.schemaVersion(), "schemaVersion", location);
        requireNonBlank(m.id(), "id", location);
        requireNonBlank(m.version(), "version", location);
        requireNonBlank(m.name(), "name", location);
        requireNonBlank(m.description(), "description", location);
        if (m.metadata() == null) {
            throw new ManifestException(location + " 缺字段: metadata");
        }
        if (m.requires() == null) {
            throw new ManifestException(location + " 缺字段: requires");
        }
        if (m.toggle() == null) {
            throw new ManifestException(location + " 缺字段: toggle");
        }
        checkKernelCompatibility(m, location);
    }

    private static void requireNonBlank(String value, String field, String location) {
        if (value == null || value.isBlank()) {
            throw new ManifestException(location + " 缺字段: " + field);
        }
    }

    private static void checkKernelCompatibility(ModuleManifest m, String location) {
        String range = m.requires().kernel();
        if (range == null || range.isBlank()) {
            return;
        }
        if (!SemverRange.parse(range).contains(CURRENT_KERNEL_VERSION)) {
            throw new ManifestException(
                    location + " 不兼容当前 kernel: 需要 " + range
                            + "，当前 " + CURRENT_KERNEL_VERSION);
        }
    }

    private static String describe(Resource r) {
        try {
            return r.getURI().toString();
        } catch (IOException e) {
            return r.getDescription();
        }
    }

    /**
     * 极简语义版本范围解析（仅支持 v0.1 需要的格式 {@code ">=X.Y.Z,<A.B.C"}）。
     * 复杂语义在后续 plan 引专门库（如 maven-artifact / semver4j）。
     */
    static final class SemverRange {

        private final int[] minInclusive;
        private final int[] maxExclusive;

        private SemverRange(int[] minInclusive, int[] maxExclusive) {
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
        }

        static SemverRange parse(String range) {
            String[] parts = range.split(",");
            if (parts.length != 2 || !parts[0].trim().startsWith(">=") || !parts[1].trim().startsWith("<")) {
                throw new ManifestException(
                        "kernel 版本范围格式不支持: " + range + "（仅支持 '>=X.Y.Z,<A.B.C' 形式，"
                                + "如 '>=0.1.0,<1.0.0'；kernel 进 SemVer，主版本变化才会破坏 API，"
                                + "推荐 '>=当前版本,<下个主版本'）");
            }
            int[] min = parseVersion(parts[0].trim().substring(2));
            int[] max = parseVersion(parts[1].trim().substring(1));
            return new SemverRange(min, max);
        }

        boolean contains(String version) {
            int[] v = parseVersion(version);
            return compare(v, minInclusive) >= 0 && compare(v, maxExclusive) < 0;
        }

        private static int[] parseVersion(String v) {
            String[] segs = v.split("\\.");
            if (segs.length != 3) {
                throw new ManifestException("版本号格式必须 X.Y.Z: " + v);
            }
            int[] r = new int[3];
            for (int i = 0; i < 3; i++) {
                r[i] = Integer.parseInt(segs[i].trim());
            }
            return r;
        }

        private static int compare(int[] a, int[] b) {
            for (int i = 0; i < 3; i++) {
                if (a[i] != b[i]) {
                    return Integer.compare(a[i], b[i]);
                }
            }
            return 0;
        }
    }
}
