<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../api'

const router = useRouter()
const authStore = useAuthStore()
const users = ref([])
const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const searchQuery = ref('')
const loading = ref(false)
const errorMsg = ref('')

onMounted(() => {
  if (!authStore.isAdmin) {
    router.replace('/')
    return
  }
  fetchUsers()
})

async function fetchUsers() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await api.get('/admin/users', { params: { page: page.value, size: 20, search: searchQuery.value || undefined } })
    const data = res.data || res
    users.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (e) {
    errorMsg.value = '加载用户列表失败'
    console.error('fetchUsers:', e)
  } finally {
    loading.value = false
  }
}

async function toggleStatus(user) {
  try {
    const res = await api.put(`/admin/users/${user.id}/status`)
    user.enabled = res.data.enabled
  } catch (e) {
    alert(e.response?.data?.message || '操作失败')
  }
}

async function deleteUser(user) {
  if (!confirm(`确认删除用户 "${user.username}" (${user.email})？此操作不可撤销。`)) return
  try {
    await api.delete(`/admin/users/${user.id}`)
    await fetchUsers()
  } catch (e) {
    alert(e.response?.data?.message || '删除失败')
  }
}

function onSearch() {
  page.value = 0
  fetchUsers()
}

function prevPage() { if (page.value > 0) { page.value--; fetchUsers() } }
function nextPage() { if (page.value < totalPages.value - 1) { page.value++; fetchUsers() } }
</script>

<template>
  <div class="admin-panel">
    <div class="page-header">
      <h1>管理面板</h1>
      <p>用户管理 · 共 {{ totalElements }} 个用户</p>
    </div>

    <div class="search-bar">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="搜索用户名或邮箱..."
        class="search-input"
        @keyup.enter="onSearch"
      />
      <button class="search-btn" @click="onSearch">搜索</button>
    </div>

    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else-if="errorMsg" class="error-state">{{ errorMsg }}</div>
    <div v-else class="user-table-wrap">
      <table class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>昵称</th>
            <th>角色</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in users" :key="u.id" :class="{ disabled: !u.enabled }">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.nickname || '-' }}</td>
            <td>
              <span class="role-badge" :class="{ admin: u.role === 'ADMIN' }">
                {{ u.role === 'ADMIN' ? '管理员' : '用户' }}
              </span>
            </td>
            <td>
              <label class="switch">
                <input type="checkbox" :checked="u.enabled" @change="toggleStatus(u)" :disabled="u.role === 'ADMIN'" />
                <span class="slider"></span>
              </label>
            </td>
            <td>{{ u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-' }}</td>
            <td>
              <button
                class="del-btn"
                @click="deleteUser(u)"
                :disabled="u.role === 'ADMIN'"
                :title="u.role === 'ADMIN' ? '不能删除管理员' : '删除用户'"
              >删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page === 0" @click="prevPage">上一页</button>
        <span>{{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="nextPage">下一页</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-panel {
  max-width: 960px;
  margin: 0 auto;
  padding: 32px 20px;
}
.page-header { margin-bottom: 20px; }
.page-header h1 { font-size: 24px; font-weight: 700; color: var(--color-text-primary); }
.page-header p { font-size: 13px; color: var(--color-text-secondary); margin-top: 4px; }

.search-bar { display: flex; gap: 8px; margin-bottom: 20px; }
.search-input {
  flex: 1; padding: 8px 14px; border: 1px solid var(--color-border);
  border-radius: var(--radius-md); font-size: 14px; color: var(--color-text-primary);
  background: var(--color-surface);
}
.search-input:focus { outline: none; border-color: var(--color-primary); }
.search-btn {
  padding: 8px 18px; background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-md); font-size: 13px; font-weight: 600; cursor: pointer;
}

.user-table-wrap { overflow-x: auto; background: var(--color-surface); border-radius: var(--radius-lg); box-shadow: var(--shadow-card); }
.user-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.user-table th { text-align: left; padding: 12px 14px; color: var(--color-text-secondary); font-weight: 600; border-bottom: 1px solid var(--color-border); white-space: nowrap; }
.user-table td { padding: 10px 14px; border-bottom: 1px solid var(--color-divider); color: var(--color-text-primary); white-space: nowrap; }
.user-table tr.disabled td { opacity: 0.5; }

.role-badge { padding: 2px 10px; border-radius: var(--radius-full); font-size: 11px; font-weight: 600; background: var(--color-primary-light); color: var(--color-primary); }
.role-badge.admin { background: #fef3c7; color: #92400e; }

.switch { position: relative; display: inline-block; width: 36px; height: 20px; }
.switch input { display: none; }
.slider { position: absolute; inset: 0; background: var(--color-border); border-radius: var(--radius-full); cursor: pointer; transition: 0.2s; }
.slider::before { content: ''; position: absolute; top: 2px; left: 2px; width: 16px; height: 16px; background: #fff; border-radius: 50%; transition: 0.2s; }
.switch input:checked + .slider { background: var(--color-success); }
.switch input:checked + .slider::before { transform: translateX(16px); }
.switch input:disabled + .slider { opacity: 0.4; cursor: default; }

.del-btn { padding: 4px 12px; background: var(--color-danger); color: #fff; border: none; border-radius: var(--radius-sm); font-size: 12px; cursor: pointer; }
.del-btn:disabled { opacity: 0.3; cursor: default; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 16px; font-size: 13px; color: var(--color-text-secondary); }
.pagination button { padding: 6px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-md); background: var(--color-surface); color: var(--color-text-primary); cursor: pointer; font-size: 13px; }
.pagination button:disabled { opacity: 0.4; cursor: default; }

.loading-state, .error-state { text-align: center; padding: 40px; color: var(--color-text-muted); }
</style>
