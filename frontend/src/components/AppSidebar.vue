<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

defineProps({
  collapsed: { type: Boolean, default: false }
})

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

const menuItems = [
  { icon: 'dashboard', label: '控制台', to: '/' },
  { icon: 'menu_book', label: '词库', to: '/library' },
  { icon: 'bar_chart', label: '学习统计', to: '/summary' },
]

const modeItems = [
  { icon: 'visibility', label: '认读模式', to: '/learn/first-sight' },
  { icon: 'edit', label: '拼写模式', to: '/learn/spelling' },
]
</script>

<template>
  <aside class="sidebar" :class="{ collapsed }">
    <!-- Main Menu -->
    <div class="menu-section">
      <div class="section-label">导航</div>
      <router-link
        v-for="item in menuItems"
        :key="item.to"
        :to="item.to"
        class="menu-item"
        :class="{ active: route.path === item.to }"
      >
        <span class="material-icons">{{ item.icon }}</span>
        <span class="item-label">{{ item.label }}</span>
      </router-link>
    </div>

    <div class="sidebar-divider"></div>

    <!-- Learning Modes -->
    <div class="menu-section">
      <div class="section-label">学习模式</div>
      <router-link
        v-for="item in modeItems"
        :key="item.to"
        :to="item.to"
        class="menu-item mode-card"
        :class="{ active: route.path === item.to }"
      >
        <span class="material-icons">{{ item.icon }}</span>
        <span class="item-label">{{ item.label }}</span>
        <span v-if="route.path === item.to" class="material-icons check-badge">check_circle</span>
      </router-link>
    </div>

    <div class="sidebar-spacer"></div>

    <!-- Bottom actions -->
    <div class="sidebar-bottom">
      <a href="#" class="menu-item">
        <span class="material-icons">help_outline</span>
        <span class="item-label">帮助中心</span>
      </a>
      <a href="#" class="menu-item" @click.prevent="handleLogout">
        <span class="material-icons">logout</span>
        <span class="item-label">退出登录</span>
      </a>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  min-width: var(--sidebar-width);
  background: var(--color-surface);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  transition: width 0.2s, min-width 0.2s;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 0;
  min-width: 0;
  padding: 12px 0;
}

.menu-section {
  padding: 4px 8px;
}

.section-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 8px 12px 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.15s;
  text-decoration: none;
  position: relative;
}
.menu-item:hover {
  background: var(--color-divider);
  color: var(--color-text-primary);
}
.menu-item.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.menu-item .material-icons {
  font-size: 20px;
  flex-shrink: 0;
}

.item-label {
  white-space: nowrap;
  overflow: hidden;
}

.check-badge {
  position: absolute;
  right: 12px;
  font-size: 18px !important;
  color: var(--color-success) !important;
}

.sidebar-divider {
  height: 1px;
  background: var(--color-border);
  margin: 8px 16px;
}

.sidebar-spacer {
  flex: 1;
}

.sidebar-bottom {
  padding: 4px 8px;
  border-top: 1px solid var(--color-border);
  padding-top: 8px;
}
</style>
