<!--
  Copyright 2025-2026 DJI. All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { UserInfo } from '@/types/auth'
import {
  fetchUsers,
  approveUser,
  rejectUser,
  setUserRole,
  resetUserPassword,
  changeMyPassword,
  deleteUser,
} from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { RefreshCw } from 'lucide-vue-next'

const authStore = useAuthStore()
const router = useRouter()
const users = ref<UserInfo[]>([])
const loading = ref(false)
const error = ref('')
const actionLoading = ref<number | null>(null)

/** 自助改密时的 actionLoading 哨兵（避开真实 userId 0 与 null）。 */
const SELF_ACTION_LOADING_ID = -1

/**
 * 重置密码弹窗。
 * - targetUserId === null：修改自己的密码（需旧密码）
 * - targetUserId !== null：管理员重置他人密码（仅新密码）
 */
const resetDialog = ref<{ targetUserId: number | null; username: string } | null>(null)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

/** 是否当前登录用户（admin 改自己密码走自助通道，需校验旧密码） */
function isSelf(userId: number): boolean {
  return authStore.user?.id === userId
}

async function loadUsers(): Promise<void> {
  loading.value = true
  error.value = ''
  try {
    users.value = await fetchUsers()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function handleAction(userId: number, action: () => Promise<void>): Promise<void> {
  actionLoading.value = userId
  try {
    await action()
    await loadUsers()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '操作失败'
  } finally {
    actionLoading.value = null
  }
}

function handleApprove(id: number): void {
  handleAction(id, () => approveUser(id))
}

function handleReject(id: number): void {
  handleAction(id, () => rejectUser(id))
}

function handleToggleAdmin(user: UserInfo): void {
  const newRole = user.role === 'admin' ? 'user' : 'admin'
  handleAction(user.id, () => setUserRole(user.id, newRole))
}

function handleDelete(id: number): void {
  if (!confirm('确定要删除该用户吗？此操作不可恢复。')) {
    return
  }
  handleAction(id, () => deleteUser(id))
}

function openResetDialog(user: UserInfo): void {
  // admin 改自己密码：走自助通道（需旧密码），targetUserId 置空
  // admin 改他人密码：走管理员重置通道（仅新密码）
  const targetUserId = isSelf(user.id) ? null : user.id
  resetDialog.value = { targetUserId, username: user.username }
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
}

async function handleResetPassword(): Promise<void> {
  if (!resetDialog.value) {
    return
  }
  // 清空上一次操作的错误，按本次结果决定是否关闭弹窗
  error.value = ''
  if (newPassword.value.length < 8) {
    error.value = '新密码至少 8 位'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = '两次输入的新密码不一致'
    return
  }
  const isSelfReset = resetDialog.value.targetUserId === null
  if (isSelfReset && oldPassword.value.length < 8) {
    error.value = '旧密码至少 8 位'
    return
  }
  if (isSelfReset) {
    // 自助改密：成功后强制登出并跳转登录页（旧 JWT 不失效，需以新密码重新登录）
    actionLoading.value = SELF_ACTION_LOADING_ID
    try {
      await changeMyPassword(oldPassword.value, newPassword.value)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '操作失败'
      return
    } finally {
      actionLoading.value = null
    }
    authStore.logout()
    resetDialog.value = null
    await router.push('/welcome')
    return
  }
  // 管理员重置他人密码：成功后刷新列表
  const targetUserId = resetDialog.value.targetUserId
  if (targetUserId === null) {
    // 类型收窄保护，理论上不会到达
    return
  }
  await handleAction(targetUserId, () => resetUserPassword(targetUserId, newPassword.value))
  if (!error.value) {
    resetDialog.value = null
  }
}

function statusClass(status: string): string {
  switch (status) {
    case 'approved': return 'badge--success'
    case 'pending': return 'badge--warning'
    case 'rejected': return 'badge--danger'
    default: return ''
  }
}

function statusLabel(status: string): string {
  switch (status) {
    case 'approved': return 'Approved'
    case 'pending': return 'Pending'
    case 'rejected': return 'Rejected'
    default: return status
  }
}

function roleLabel(role: string): string {
  return role === 'admin' ? '管理员' : '用户'
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) {
    return '-'
  }
  try {
    return new Date(dateStr).toLocaleString('zh-CN')
  } catch {
    return dateStr
  }
}

onMounted(loadUsers)
</script>

<template>
  <div class="user-mgmt">
    <div class="user-mgmt__header">
      <h2>用户管理</h2>
      <button class="btn btn--ghost" :disabled="loading" @click="loadUsers">
        <RefreshCw :size="14" :class="{ spinning: loading }" />
        <span>刷新</span>
      </button>
    </div>

    <p v-if="error" class="user-mgmt__error">{{ error }}</p>

    <div class="user-mgmt__table-wrap">
      <table class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>角色</th>
            <th>状态</th>
            <th>邮箱</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading && users.length === 0">
            <td colspan="7" class="user-table__empty">加载中...</td>
          </tr>
          <tr v-for="u in users" :key="u.id">
            <td>{{ u.id }}</td>
            <td>
              <span class="username">{{ u.username }}</span>
              <span v-if="isSelf(u.id)" class="badge badge--info">我</span>
            </td>
            <td>
              <span class="badge" :class="u.role === 'admin' ? 'badge--primary' : ''">
                {{ roleLabel(u.role) }}
              </span>
            </td>
            <td>
              <span class="badge" :class="statusClass(u.status)">
                {{ statusLabel(u.status) }}
              </span>
            </td>
            <td>{{ u.email ?? '-' }}</td>
            <td>{{ formatDate(u.createdAt) }}</td>
            <td class="user-table__actions">
              <!-- 待审核用户：审核通过 -->
              <button
                v-if="u.status === 'pending'"
                class="btn btn--approve btn--sm"
                :disabled="actionLoading === u.id"
                @click="handleApprove(u.id)"
              >
                Approve
              </button>
              <!-- 重置密码（admin 可重置他人；管理员重置自己密码走自助通道，需旧密码） -->
              <button
                class="btn btn--outline btn--sm"
                :disabled="actionLoading === u.id"
                @click="openResetDialog(u)"
              >
                {{ isSelf(u.id) ? '修改密码' : 'Reset Pwd' }}
              </button>
              <!-- 角色切换（非自己） -->
              <button
                v-if="!isSelf(u.id)"
                class="btn btn--sm"
                :class="u.role === 'admin' ? 'btn--revoke-admin' : 'btn--grant-admin'"
                :disabled="actionLoading === u.id"
                @click="handleToggleAdmin(u)"
              >
                {{ u.role === 'admin' ? '取消 Admin' : '设为 Admin' }}
              </button>
              <!-- 待审核用户：拒绝/删除 -->
              <button
                v-if="u.status === 'pending' && !isSelf(u.id)"
                class="btn btn--delete btn--sm"
                :disabled="actionLoading === u.id"
                @click="handleReject(u.id)"
              >
                Delete
              </button>
              <!-- 非 pending 用户：删除（不能删自己） -->
              <button
                v-if="u.status !== 'pending' && !isSelf(u.id)"
                class="btn btn--delete btn--sm"
                :disabled="actionLoading === u.id"
                @click="handleDelete(u.id)"
              >
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 重置密码弹窗 -->
    <Teleport to="body">
      <div v-if="resetDialog" class="modal-overlay" @click.self="resetDialog = null">
        <div class="modal-card">
          <h3>{{ resetDialog.targetUserId === null ? '修改我的密码' : '重置密码' }}</h3>
          <p class="modal-desc">
            <template v-if="resetDialog.targetUserId === null">
              修改当前账号 <strong>{{ resetDialog.username }}</strong> 的密码，需输入旧密码
            </template>
            <template v-else>
              为用户 <strong>{{ resetDialog.username }}</strong> 设置新密码
            </template>
          </p>
          <input
            v-if="resetDialog.targetUserId === null"
            v-model="oldPassword"
            type="password"
            placeholder="旧密码（至少 8 位）"
            class="modal-input"
            minlength="8"
          />
          <input
            v-model="newPassword"
            type="password"
            placeholder="新密码（至少 8 位）"
            class="modal-input"
            minlength="8"
          />
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="确认新密码"
            class="modal-input"
            minlength="8"
            @keyup.enter="handleResetPassword"
          />
          <p
            v-if="confirmPassword.length > 0 && newPassword !== confirmPassword"
            class="modal-mismatch"
          >
            两次输入的密码不一致
          </p>
          <div class="modal-actions">
            <button class="btn btn--ghost" @click="resetDialog = null">取消</button>
            <button
              class="btn btn--primary"
              :disabled="newPassword.length < 8
                || newPassword !== confirmPassword
                || (resetDialog.targetUserId === null && oldPassword.length < 8)"
              @click="handleResetPassword"
            >
              {{ resetDialog.targetUserId === null ? '确认修改' : '确认重置' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.user-mgmt {
  padding: 24px;
  width: 100%;
}

.user-mgmt__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.user-mgmt__header h2 {
  color: var(--app-text-primary, #e6edf3);
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.user-mgmt__error {
  color: #f87171;
  font-size: 13px;
  padding: 8px 12px;
  background: rgba(248, 113, 113, 0.08);
  border-radius: var(--radius-sm);
  border-left: 3px solid #f87171;
  margin-bottom: 16px;
}

.user-mgmt__table-wrap {
  overflow-x: auto;
  border: 1px solid var(--app-border, rgba(255,255,255,0.08));
  border-radius: var(--radius-md);
  background: var(--app-bg-panel, rgba(255,255,255,0.04));
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.user-table th,
.user-table td {
  padding: 10px 14px;
  text-align: left;
  white-space: nowrap;
  color: var(--app-text-primary, #e6edf3);
}

.user-table th {
  font-weight: 500;
  color: var(--app-text-secondary, #8b949e);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--app-border, rgba(255,255,255,0.08));
}

.user-table tbody tr {
  border-bottom: 1px solid var(--app-border, rgba(255,255,255,0.04));
  transition: background var(--transition-base);
}

.user-table tbody tr:hover {
  background: rgba(255, 255, 255, 0.03);
}

.user-table__empty {
  text-align: center;
  color: var(--app-text-secondary, #8b949e);
  padding: 32px !important;
}

.user-table__actions {
  display: flex;
  gap: 4px;
  align-items: center;
}

.username {
  font-weight: 500;
  margin-right: 6px;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.06);
  color: var(--app-text-secondary, #8b949e);
}

.badge--primary {
  background: rgba(99, 102, 241, 0.15);
  color: #818cf8;
}

.badge--success {
  background: rgba(34, 197, 94, 0.12);
  color: #22c55e;
}

.badge--warning {
  background: rgba(250, 204, 21, 0.12);
  color: #facc15;
}

.badge--danger {
  background: rgba(248, 113, 113, 0.12);
  color: #f87171;
}

.badge--info {
  background: rgba(56, 189, 248, 0.12);
  color: #38bdf8;
  margin-left: 4px;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.btn--sm {
  padding: 4px 8px;
}

.btn--ghost {
  background: rgba(255, 255, 255, 0.06);
  color: var(--app-text-secondary, #8b949e);
}

.btn--ghost:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  color: var(--app-text-primary, #e6edf3);
}

.btn--primary {
  background: var(--app-primary, #06b6d4);
  color: #fff;
}

.btn--primary:hover:not(:disabled) {
  filter: brightness(1.1);
}

.btn--success {
  background: rgba(34, 197, 94, 0.15);
  color: #22c55e;
}

.btn--success:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.25);
}

.btn--danger {
  background: rgba(248, 113, 113, 0.12);
  color: #f87171;
}

.btn--danger:hover:not(:disabled) {
  background: rgba(248, 113, 113, 0.2);
}

.btn--approve {
  background: #22c55e;
  color: #fff;
  font-weight: 600;
}

.btn--approve:hover:not(:disabled) {
  background: #16a34a;
}

.btn--outline {
  background: rgba(255, 255, 255, 0.08);
  color: var(--app-text-primary, #e6edf3);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.btn--outline:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.14);
}

.btn--grant-admin {
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
}

.btn--grant-admin:hover:not(:disabled) {
  background: rgba(56, 189, 248, 0.25);
}

.btn--revoke-admin {
  background: rgba(250, 204, 21, 0.15);
  color: #facc15;
}

.btn--revoke-admin:hover:not(:disabled) {
  background: rgba(250, 204, 21, 0.25);
}

.btn--delete {
  background: #ef4444;
  color: #fff;
  font-weight: 600;
}

.btn--delete:hover:not(:disabled) {
  background: #dc2626;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 刷新图标旋转动画 */
.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}

.modal-card {
  background: var(--app-bg-panel, #1a1f2e);
  border: 1px solid var(--app-border, rgba(255,255,255,0.1));
  border-radius: var(--radius-lg);
  padding: 24px;
  width: 380px;
  max-width: 90vw;
}

.modal-card h3 {
  margin: 0 0 8px;
  color: var(--app-text-primary, #e6edf3);
  font-size: 16px;
}

.modal-desc {
  color: var(--app-text-secondary, #8b949e);
  font-size: 13px;
  margin: 0 0 16px;
}

.modal-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--app-border, rgba(255,255,255,0.12));
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-text-primary, #e6edf3);
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.modal-input:focus {
  border-color: var(--app-primary, #06b6d4);
}

.modal-input + .modal-input {
  margin-top: 10px;
}

.modal-mismatch {
  color: #f87171;
  font-size: 12px;
  margin: 6px 0 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>
