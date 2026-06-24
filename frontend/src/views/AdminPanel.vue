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
const selectedUser = ref(null)
const userDetail = ref(null)
const detailLoading = ref(false)

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

async function viewDetail(user) {
  selectedUser.value = user
  detailLoading.value = true
  userDetail.value = null
  try {
    const res = await api.get(`/admin/users/${user.id}`)
    userDetail.value = res.data || res
  } catch (e) {
    console.error('viewDetail:', e)
    userDetail.value = null
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  selectedUser.value = null
  userDetail.value = null
}
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
              <button class="detail-btn" @click="viewDetail(u)">详情</button>
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

    <!-- User Detail Modal -->
    <div v-if="selectedUser" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content">
        <div class="modal-header">
          <h2>用户详情</h2>
          <button class="modal-close" @click="closeDetail">&times;</button>
        </div>
        <div v-if="detailLoading" class="loading-state">加载中...</div>
        <div v-else-if="!userDetail" class="error-state">加载失败</div>
        <div v-else class="detail-body">
          <section class="detail-section">
            <h3>基本信息</h3>
            <dl>
              <dt>ID</dt><dd>{{ userDetail.id }}</dd>
              <dt>用户名</dt><dd>{{ userDetail.username }}</dd>
              <dt>邮箱</dt><dd>{{ userDetail.email }}</dd>
              <dt>昵称</dt><dd>{{ userDetail.nickname || '-' }}</dd>
              <dt>角色</dt><dd>{{ userDetail.role === 'ADMIN' ? '管理员' : '用户' }}</dd>
              <dt>状态</dt><dd>{{ userDetail.enabled ? '启用' : '禁用' }}</dd>
              <dt>注册时间</dt><dd>{{ userDetail.createdAt ? new Date(userDetail.createdAt).toLocaleString() : '-' }}</dd>
            </dl>
          </section>

          <section class="detail-section" v-if="userDetail.settings">
            <h3>学习设置</h3>
            <dl>
              <dt>每日新词数</dt><dd>{{ userDetail.settings.newWordCount }}</dd>
              <dt>复习比例</dt><dd>1:{{ userDetail.settings.reviewRatio }}</dd>
              <dt>卡片顺序</dt><dd>{{ userDetail.settings.cardOrder === 'random' ? '随机' : '字母序' }}</dd>
              <dt>大字体</dt><dd>{{ userDetail.settings.largeFont ? '开' : '关' }}</dd>
              <dt>深色模式</dt><dd>{{ userDetail.settings.darkMode ? '开' : '关' }}</dd>
              <dt>学习模式</dt><dd>{{ userDetail.settings.learningMode === 'spelling' ? '拼写' : '认读' }}</dd>
              <dt>考试日期</dt><dd>{{ userDetail.settings.examDate || '未设置' }}</dd>
              <dt>选中词书ID</dt><dd>{{ userDetail.settings.selectedBookId || '无' }}</dd>
            </dl>
          </section>
          <section class="detail-section" v-else>
            <h3>学习设置</h3>
            <p class="no-data">未配置</p>
          </section>

          <section class="detail-section">
            <h3>学习统计</h3>
            <dl>
              <dt>已掌握</dt><dd>{{ userDetail.learningStats?.mastered || 0 }}</dd>
              <dt>复习中</dt><dd>{{ userDetail.learningStats?.reviewing || 0 }}</dd>
              <dt>学习中</dt><dd>{{ userDetail.learningStats?.learning || 0 }}</dd>
              <dt>新词</dt><dd>{{ userDetail.learningStats?.newCount || 0 }}</dd>
              <dt>收藏数</dt><dd>{{ userDetail.learningStats?.favoritesCount || 0 }}</dd>
              <dt>垃圾桶</dt><dd>{{ userDetail.learningStats?.blacklistCount || 0 }}</dd>
            </dl>
          </section>

          <section class="detail-section">
            <h3>打卡记录</h3>
            <dl>
              <dt>累计打卡</dt><dd>{{ userDetail.checkinStats?.totalCheckins || 0 }} 次</dd>
              <dt>最后打卡</dt><dd>{{ userDetail.checkinStats?.lastCheckinDate || '从未打卡' }}</dd>
            </dl>
          </section>
        </div>
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
.detail-btn { padding: 4px 12px; background: var(--color-primary); color: #fff; border: none; border-radius: var(--radius-sm); font-size: 12px; cursor: pointer; margin-right: 6px; }
.detail-btn:hover { background: var(--color-primary-dark); }

.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; padding: 16px; font-size: 13px; color: var(--color-text-secondary); }
.pagination button { padding: 6px 16px; border: 1px solid var(--color-border); border-radius: var(--radius-md); background: var(--color-surface); color: var(--color-text-primary); cursor: pointer; font-size: 13px; }
.pagination button:disabled { opacity: 0.4; cursor: default; }

.loading-state, .error-state { text-align: center; padding: 40px; color: var(--color-text-muted); }

/* Detail Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; padding: 20px;
}
.modal-content {
  background: var(--color-surface); border-radius: var(--radius-xl);
  max-width: 640px; width: 100%; max-height: 85vh; overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 24px; border-bottom: 1px solid var(--color-border);
}
.modal-header h2 { font-size: 18px; font-weight: 700; color: var(--color-text-primary); }
.modal-close {
  background: none; border: none; font-size: 24px; color: var(--color-text-muted); cursor: pointer;
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  border-radius: var(--radius-sm);
}
.modal-close:hover { background: var(--color-divider); }
.detail-body { padding: 20px 24px; }
.detail-section { margin-bottom: 20px; }
.detail-section h3 {
  font-size: 14px; font-weight: 600; color: var(--color-text-secondary);
  margin-bottom: 10px; padding-bottom: 6px; border-bottom: 1px solid var(--color-divider);
}
.detail-section dl { display: grid; grid-template-columns: 1fr 2fr; gap: 4px 12px; font-size: 13px; }
.detail-section dt { color: var(--color-text-muted); }
.detail-section dd { color: var(--color-text-primary); font-weight: 500; }
.no-data { font-size: 13px; color: var(--color-text-muted); }
</style>
