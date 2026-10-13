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

/** 注册请求参数 */
export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
}

/** 登录请求参数 */
export interface LoginRequest {
  username: string
  password: string
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  nickname: string | null
  role: string
  email: string | null
  emailVerified: boolean
  status: string
  createdAt: string | null
}

/** 认证响应（注册时 token 可能为 null，表示待审核） */
export interface AuthResponse {
  token: string | null
  user: UserInfo
  message: string | null
}
