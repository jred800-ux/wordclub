<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ username: '', password: '' })
const showPassword = ref(false)
const errorMsg = ref('')
const loading = ref(false)

async function handleLogin() {
  errorMsg.value = ''
  if (!form.value.username || !form.value.password) {
    errorMsg.value = '请填写用户名和密码'
    return
  }
  loading.value = true
  try {
    await authStore.login(form.value.username, form.value.password)
    router.push('/')
  } catch (e) {
    const data = e.response?.data
    errorMsg.value = data?.message || '登录失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- Brand -->
      <div class="brand">
        <div class="brand-icon">W</div>
        <h1>WordClub</h1>
      </div>
      <p class="subtitle">欢迎回来</p>

      <!-- Error -->
      <div v-if="errorMsg" class="error-banner">
        <span class="material-icons">error_outline</span>
        {{ errorMsg }}
      </div>

      <!-- Form -->
      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="input-group">
          <span class="material-icons input-icon">person</span>
          <input
            v-model="form.username"
            type="text"
            placeholder="用户名"
            autocomplete="username"
          />
        </div>
        <div class="input-group">
          <span class="material-icons input-icon">lock</span>
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码"
            autocomplete="current-password"
          />
          <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
            <span class="material-icons">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
          </button>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>登录</span>
        </button>
      </form>

      <!-- Footer -->
      <p class="auth-footer">
        还没有账户？
        <router-link to="/register">立即注册 →</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  background: var(--color-bg);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: 40px 32px;
}

.brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 4px;
}
.brand-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-md);
  font-size: 18px;
  font-weight: 800;
}
.brand h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.subtitle {
  text-align: center;
  font-size: 15px;
  color: var(--color-text-secondary);
  margin: 8px 0 24px;
}

/* Error */
.error-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--color-danger-light);
  color: var(--color-danger);
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
}
.error-banner .material-icons { font-size: 18px; }

/* Form */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}
.input-group input {
  width: 100%;
  height: 46px;
  padding: 0 14px 0 42px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--color-text-primary);
  background: var(--color-bg);
  outline: none;
  transition: border-color 0.2s;
}
.input-group input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.input-icon {
  position: absolute;
  left: 12px;
  font-size: 20px;
  color: var(--color-text-muted);
  pointer-events: none;
}
.toggle-pw {
  position: absolute;
  right: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  background: none;
  color: var(--color-text-muted);
  border-radius: var(--radius-sm);
}
.toggle-pw:hover { color: var(--color-text-secondary); }
.toggle-pw .material-icons { font-size: 20px; }

.submit-btn {
  height: 46px;
  border: none;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.submit-btn:hover:not(:disabled) { background: var(--color-primary-dark); }
.submit-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Footer */
.auth-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
.auth-footer a {
  color: var(--color-primary);
  font-weight: 500;
}

@media (max-width: 480px) {
  .auth-card { padding: 28px 20px; }
}
</style>
