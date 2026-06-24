<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useWordStore } from './stores/word'
import SearchModal from './components/SearchModal.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const wordStore = useWordStore()
const showSearch = ref(false)
const showUserMenu = ref(false)

const isGuestPage = computed(() => {
  return ['Login', 'Register'].includes(route.name)
})

function handleLogout() {
  showUserMenu.value = false
  authStore.logout()
  router.push('/login')
}

// Load user settings when authenticated
onMounted(() => {
  if (authStore.isLoggedIn) {
    wordStore.fetchSettings()
  }
})

watch(() => authStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    wordStore.fetchSettings()
  }
})

// Dark mode — toggle class on <html> so :root.dark CSS applies
function applyDarkMode(enabled) {
  if (enabled) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}
if (wordStore.darkMode) applyDarkMode(true)
watch(() => wordStore.darkMode, applyDarkMode)
</script>

<template>
  <!-- Guest pages (login/register) render without shell -->
  <div v-if="isGuestPage" class="guest-layout">
    <RouterView />
  </div>

  <!-- Authenticated layout with header -->
  <div v-else id="app-shell">
    <!-- Header -->
    <header class="app-header">
      <div class="header-left">
        <router-link to="/" class="brand">
          <span class="brand-icon">W</span>
          <span class="brand-text">WordClub</span>
        </router-link>
      </div>

      <nav class="header-nav">
        <router-link to="/">控制台</router-link>
        <router-link to="/library">词库</router-link>
        <router-link to="/summary">学习统计</router-link>
        <router-link to="/settings">设置</router-link>
      </nav>

      <div class="header-actions">
        <button class="icon-btn" title="搜索" @click="showSearch = true">
          <span class="material-icons">search</span>
        </button>
        <button class="icon-btn has-badge" title="通知">
          <span class="material-icons">notifications</span>
          <span class="badge-dot"></span>
        </button>

        <!-- Logged in: user dropdown -->
        <div v-if="authStore.isLoggedIn" class="user-menu-wrapper">
          <div class="user-info" @click="showUserMenu = !showUserMenu">
            <span class="material-icons">account_circle</span>
            <span class="user-name">{{ authStore.user?.nickname || '用户' }}</span>
            <span class="material-icons arrow" :class="{ open: showUserMenu }">arrow_drop_down</span>
          </div>
          <div v-if="showUserMenu" class="user-dropdown" @click.self="showUserMenu = false">
            <div class="dropdown-item">
              <span class="material-icons">help_outline</span>
              <span>帮助中心</span>
            </div>
            <div class="dropdown-item">
              <span class="material-icons">person</span>
              <span>个人信息</span>
            </div>
            <div class="dropdown-item" @click="handleLogout">
              <span class="material-icons">logout</span>
              <span>退出登录</span>
            </div>
          </div>
        </div>

        <!-- Not logged in: login button -->
        <router-link v-else to="/login" class="login-btn">登录</router-link>
      </div>
    </header>

    <div class="app-body">
      <main class="main-content">
        <RouterView />
      </main>
    </div>

    <!-- Search Modal -->
    <SearchModal :visible="showSearch" @close="showSearch = false" />
  </div>
</template>

<style scoped>
/* ===== Guest Layout ===== */
.guest-layout {
  min-height: 100vh;
}

/* ===== Shell ===== */
#app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* ===== Header ===== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  height: var(--header-height);
  padding: 0 20px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-primary);
  font-weight: 700;
  font-size: 18px;
}
.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 800;
}

.header-nav {
  display: flex;
  gap: 4px;
  flex: 1;
}
.header-nav a {
  padding: 6px 14px;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: color 0.15s, background 0.15s;
}
.header-nav a:hover {
  color: var(--color-primary);
  background: var(--color-primary-light);
}
.header-nav a.router-link-exact-active {
  color: var(--color-primary);
  background: var(--color-primary-light);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.icon-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: none;
  color: var(--color-text-secondary);
  border-radius: var(--radius-full);
  transition: background 0.15s;
}
.icon-btn:hover {
  background: var(--color-divider);
}

.badge-dot {
  position: absolute;
  top: 6px;
  right: 8px;
  width: 7px;
  height: 7px;
  background: var(--color-danger);
  border-radius: 50%;
  border: 1.5px solid var(--color-surface);
}

/* Login button */
.login-btn {
  padding: 6px 18px;
  background: var(--color-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  border-radius: var(--radius-md);
  transition: background 0.15s;
}
.login-btn:hover {
  background: var(--color-primary-dark);
}

/* User menu */
.user-menu-wrapper {
  position: relative;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  cursor: pointer;
}
.user-info:hover {
  background: var(--color-divider);
}
.user-info .material-icons {
  font-size: 28px;
  color: var(--color-text-muted);
}
.user-info .arrow {
  font-size: 20px;
  transition: transform 0.2s;
}
.user-info .arrow.open {
  transform: rotate(180deg);
}
.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 6px;
  min-width: 160px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  padding: 6px 0;
  z-index: 200;
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: background 0.1s;
}
.dropdown-item:hover {
  background: var(--color-divider);
  color: var(--color-text-primary);
}
.dropdown-item .material-icons {
  font-size: 18px;
}

/* ===== Body Layout ===== */
.app-body {
  display: flex;
  flex: 1;
}

.main-content {
  flex: 1;
  overflow-y: auto;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .header-nav {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  .header-nav a {
    white-space: nowrap;
    font-size: 13px;
    padding: 4px 10px;
  }
  .user-name {
    display: none;
  }
}
</style>
