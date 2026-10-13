/**
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

/**
 * Skeleton BFF 对外接口统一前缀。
 *
 * 后端通过 server.servlet.context-path=/pf-devkit/ai-plugin/skeleton 前置所有 HTTP 接口
 * （controller + actuator + mock）。前端所有请求路径需以此为前缀，避免散落硬编码。
 */
export const API_BASE = '/pf-devkit/ai-plugin/skeleton'
