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
import { useRouter } from 'vue-router'
import {
  LayoutDashboard,
  MonitorPlay,
  Cpu,
  Joystick,
  Route,
  ClipboardList,
  Library,
  Map,
  Boxes,
  Bot,
  LogIn,
  BookOpen,
} from 'lucide-vue-next'

const router = useRouter()

/** 平台核心功能模块，用于首屏能力展示卡片 */
const features = [
  { icon: LayoutDashboard, title: '数据看板', desc: '运营数据总览，资产统计与飞行数据可视化' },
  { icon: MonitorPlay, title: '直播大屏', desc: '多路实时视频流监控，多布局灵活切换' },
  { icon: Cpu, title: '设备管理', desc: '机场/飞行器列表、在线状态与固件管理' },
  { icon: Joystick, title: '驾驶舱', desc: '实时飞行监控、遥控操作与视频画面' },
  { icon: Route, title: '航线编辑器', desc: '航线库管理，可视化航线创建与编辑' },
  { icon: ClipboardList, title: '任务管理', desc: '飞行任务创建、执行监控与轨迹回放' },
  { icon: Library, title: '媒体管理', desc: '无人机拍摄照片与视频的统一浏览下载' },
  { icon: Map, title: '空域管理', desc: '电子围栏、禁飞区与作业区可视化管理' },
  { icon: Boxes, title: '模型库', desc: '三维重建任务管理与模型成果浏览' },
  { icon: Bot, title: 'AI 助手', desc: '自然语言交互，智能运维与任务下发' },
]

/** 进入登录页 */
function goLogin(): void {
  router.push('/login')
}
</script>

<template>
  <div class="welcome-page">
    <div class="welcome-bg-orb welcome-bg-orb--1" />
    <div class="welcome-bg-orb welcome-bg-orb--2" />
    <div class="welcome-bg-orb welcome-bg-orb--3" />

    <!-- 顶部导航：品牌标识 -->
    <header class="welcome-topbar">
      <div class="welcome-brand">
        <img src="@/assets/logo.svg" alt="DJI Cloud API 2" class="welcome-logo" />
        <span class="welcome-brand-name">无人机管理平台</span>
      </div>
    </header>

    <!-- 主体 -->
    <main class="welcome-main">
      <!-- Hero -->
      <section class="welcome-hero">
        <div class="welcome-hero-grid" aria-hidden="true" />
        <div class="welcome-hero-glow" aria-hidden="true" />
        <span class="welcome-badge">基于DJI 上云API2 SDK</span>
        <h1 class="welcome-title">大疆上云API2无人机管理系统</h1>
        <p class="welcome-subtitle">
          基于大疆上云 API 2 构建的全功能无人机运营管理系统，
          覆盖设备管理、直播监控、航线规划、任务调度、媒体管理与三维重建等完整能力。
        </p>
        <div class="welcome-hero-actions">
          <button class="welcome-cta-btn welcome-cta-btn--primary" @click="goLogin">
            <LogIn :size="16" :stroke-width="2" />
            登录进入系统
          </button>
          <a
            href="/docs/user-guide/index.html"
            target="_blank"
            rel="noopener noreferrer"
            class="welcome-cta-btn welcome-cta-btn--ghost"
          >
            <BookOpen :size="16" :stroke-width="2" />
            查看使用说明
          </a>
        </div>
        <div class="welcome-stats">
          <div class="welcome-stat">
            <span class="welcome-stat-num">10</span>
            <span class="welcome-stat-label">大模块</span>
          </div>
          <div class="welcome-stat">
            <span class="welcome-stat-num">AI</span>
            <span class="welcome-stat-label">辅助</span>
          </div>
          <div class="welcome-stat">
            <span class="welcome-stat-num">开箱</span>
            <span class="welcome-stat-label">即用</span>
          </div>
        </div>
      </section>

      <!-- 功能卡片 -->
      <section class="welcome-cards">
        <div v-for="(f, i) in features" :key="f.title" class="welcome-card" :style="{ animationDelay: `${i * 60}ms` }">
          <div class="welcome-card-icon-wrap">
            <component :is="f.icon" :size="22" :stroke-width="1.75" class="welcome-card-icon" />
          </div>
          <h3 class="welcome-card-title">{{ f.title }}</h3>
          <p class="welcome-card-desc">{{ f.desc }}</p>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.welcome-page {
  position: relative;
  min-height: 100vh;
  background: #080c14;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

/* 背景光效 */
.welcome-bg-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(90px);
  pointer-events: none;
}

.welcome-bg-orb--1 {
  width: 480px;
  height: 480px;
  background: var(--app-primary, #06b6d4);
  top: -160px;
  right: -120px;
  opacity: 0.14;
}

.welcome-bg-orb--2 {
  width: 360px;
  height: 360px;
  background: #7c3aed;
  bottom: -120px;
  left: -80px;
  opacity: 0.10;
}

.welcome-bg-orb--3 {
  width: 240px;
  height: 240px;
  background: var(--app-primary, #06b6d4);
  top: 45%;
  left: 55%;
  opacity: 0.06;
}

/* 顶部导航 */
.welcome-topbar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 40px;
}

.welcome-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome-logo {
  height: 32px;
  width: auto;
}

.welcome-brand-name {
  font-size: 16px;
  font-weight: 600;
  color: #e6edf3;
  letter-spacing: 0.5px;
}

.welcome-cta-btn:focus-visible {
  outline: 2px solid var(--app-primary);
  outline-offset: 2px;
}

/* 主体 */
.welcome-main {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 40px 80px;
}

.welcome-hero {
  position: relative;
  text-align: center;
  max-width: 760px;
  margin-bottom: 40px;
  padding-top: 40px;
}

/* 等距网格光效：为 hero 增加空间纵深与科技感 */
.welcome-hero-grid {
  position: absolute;
  inset: 0 -40% -40px;
  background-image:
    linear-gradient(rgba(6, 182, 212, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(6, 182, 212, 0.06) 1px, transparent 1px);
  background-size: 44px 44px;
  -webkit-mask-image: radial-gradient(ellipse 60% 60% at 50% 30%, #000 30%, transparent 75%);
  mask-image: radial-gradient(ellipse 60% 60% at 50% 30%, #000 30%, transparent 75%);
  pointer-events: none;
  z-index: 0;
}

/* 顶部辉光，聚焦 hero 视觉重心 */
.welcome-hero-glow {
  position: absolute;
  top: -60px;
  left: 50%;
  transform: translateX(-50%);
  width: 560px;
  height: 160px;
  background: radial-gradient(ellipse at center, rgba(6, 182, 212, 0.16), transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.welcome-badge {
  position: relative;
  z-index: 1;
  display: inline-block;
  padding: 5px 14px;
  margin-bottom: 20px;
  border: 1px solid rgba(6, 182, 212, 0.3);
  border-radius: 999px;
  background: rgba(6, 182, 212, 0.08);
  color: var(--app-primary);
  font-size: 12px;
  letter-spacing: 0.5px;
  animation: welcome-fade-up 0.6s ease both;
}

.welcome-title {
  position: relative;
  z-index: 1;
  font-size: 36px;
  font-weight: 700;
  color: #e6edf3;
  margin: 0 0 16px;
  line-height: 1.3;
  animation: welcome-fade-up 0.6s ease 0.08s both;
}

.welcome-subtitle {
  position: relative;
  z-index: 1;
  font-size: 15px;
  line-height: 1.8;
  color: #8b949e;
  margin: 0 auto 28px;
  max-width: 640px;
  animation: welcome-fade-up 0.6s ease 0.16s both;
}

/* Hero 区主 CTA：首屏即出现，双按钮双入口 */
.welcome-hero-actions {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
  animation: welcome-fade-up 0.6s ease 0.24s both;
}

/* 信任统计条：用数据支撑平台能力 */
.welcome-stats {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  gap: 48px;
  margin-top: 40px;
  animation: welcome-fade-up 0.6s ease 0.32s both;
}

.welcome-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.welcome-stat-num {
  font-size: 22px;
  font-weight: 700;
  color: #e6edf3;
  line-height: 1.2;
}

.welcome-stat-label {
  font-size: 12px;
  color: #8b949e;
}

@keyframes welcome-fade-up {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 功能卡片 */
.welcome-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  width: 100%;
  max-width: 1080px;
  margin-bottom: 48px;
}

.welcome-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.07);
  transition: all var(--transition-base);
  animation: welcome-fade-up 0.5s ease both;
}

.welcome-card:hover {
  transform: translateY(-3px);
  border-color: var(--app-primary);
  background: var(--app-primary-bg);
}

/* 图标容器：hover 时改变底色，反馈更明确 */
.welcome-card-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-bottom: 14px;
  border-radius: 10px;
  background: rgba(6, 182, 212, 0.08);
  border: 1px solid rgba(6, 182, 212, 0.14);
  transition: all var(--transition-base);
}

.welcome-card:hover .welcome-card-icon-wrap {
  background: rgba(6, 182, 212, 0.16);
  border-color: var(--app-primary);
}

.welcome-card-icon {
  color: var(--app-primary);
}

.welcome-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #e6edf3;
  margin: 0 0 6px;
}

.welcome-card-desc {
  font-size: 12px;
  line-height: 1.6;
  color: #8b949e;
  margin: 0;
}

/* 底部 CTA 已并入 Hero 区，此处保留共享的按钮样式供 Hero 双按钮使用 */
.welcome-cta-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 200px;
  padding: 12px 28px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  text-decoration: none;
}

.welcome-cta-btn--ghost {
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: transparent;
  color: #e6edf3;
}

.welcome-cta-btn--ghost:hover {
  border-color: var(--app-primary);
  color: var(--app-primary);
  background: var(--app-primary-bg);
}

.welcome-cta-btn--primary {
  border: none;
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-active));
  color: #fff;
}

.welcome-cta-btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: var(--app-primary-glow);
}

/* 响应式 */
@media (max-width: 1024px) {
  .welcome-stats {
    gap: 32px;
  }
}

@media (max-width: 640px) {
  .welcome-topbar {
    padding: 14px 20px;
  }

  .welcome-brand-name {
    display: none;
  }

  .welcome-main {
    padding: 24px 20px 60px;
  }

  .welcome-title {
    font-size: 26px;
  }

  .welcome-hero-grid {
    inset: 0 -100% -20px;
    background-size: 32px 32px;
  }

  .welcome-stats {
    gap: 24px;
  }

  .welcome-stat-num {
    font-size: 18px;
  }

  .welcome-cards {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
}
</style>