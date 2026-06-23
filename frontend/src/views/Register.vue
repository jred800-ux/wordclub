<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ username: '', email: '', password: '' })
const showPassword = ref(false)
const errorMsg = ref('')
const loading = ref(false)

async function handleRegister() {
  errorMsg.value = ''
  if (!form.value.username || !form.value.email || !form.value.password) {
    errorMsg.value = '请填写所有字段'
    return
  }
  if (form.value.password.length < 6) {
    errorMsg.value = '密码最少 6 位'
    return
  }
  loading.value = true
  try {
    await authStore.register(form.value.username, form.value.email, form.value.password)
    router.push('/login?registered=1')
  } catch (e) {
    const data = e.response?.data
    errorMsg.value = data?.message || '注册失败，请检查网络连接'
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
      <p class="subtitle">创建你的学习账户</p>

      <!-- Error -->
      <div v-if="errorMsg" class="error-banner">
        <span class="material-icons">error_outline</span>
        {{ errorMsg }}
      </div>

      <!-- Registered success hint -->
      <div v-if="$route.query.registered" class="success-banner">
        <span class="material-icons">check_circle</span>
        注册成功，请登录
      </div>

      <!-- Form -->
      <form @submit.prevent="handleRegister" class="auth-form">
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
          <span class="material-icons input-icon">email</span>
          <input
            v-model="form.email"
            type="email"
            placeholder="邮箱"
            autocomplete="email"
          />
        </div>
        <div class="input-group">
          <span class="material-icons input-icon">lock</span>
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码（至少6位）"
            autocomplete="new-password"
          />
          <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
            <span class="material-icons">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
          </button>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>注册</span>
        </button>
      </form>

      <!-- Footer -->
      <p class="auth-footer">
        已有账户？
        <router-link to="/login">立即登录 →</router-link>
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

/* Messages */
.error-banner, .success-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
}
.error-banner {
  background: var(--color-danger-light);
  color: var(--color-danger);
}
.success-banner {
  background: var(--color-success-light);
  color: #065f46;
}
.error-banner .material-icons, .success-banner .material-icons { font-size: 18px; }

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
