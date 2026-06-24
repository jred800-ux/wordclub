<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWordStore } from '../stores/word'

const router = useRouter()
const store = useWordStore()
const loading = ref(true)
const errorMsg = ref('')

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
]
const icons = ['school', 'menu_book', 'auto_stories', 'flight_takeoff', 'translate', 'psychology']

onMounted(async () => {
  try {
    await store.fetchBooks()
    errorMsg.value = store.books.length ? '' : '暂无可用词书，请联系管理员'
  } catch (e) {
    errorMsg.value = '加载词库失败，请刷新重试'
  } finally {
    loading.value = false
  }
})

function selectBook(book) {
  router.push(`/book/${book.id}`)
}
</script>

<template>
  <div class="book-library">
    <div class="library-grid">
      <div class="library-main">
        <div class="section-header">
          <h1>词库选择</h1>
          <p>选择一套课程，开启你的语言沉浸式学习之旅。</p>
        </div>

        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载中...</p>
        </div>
        <div v-else-if="errorMsg" class="error-state">{{ errorMsg }}</div>
        <div v-else-if="!store.books.length" class="empty-state">暂无可用词书</div>
        <div v-else class="course-grid">
          <button
            v-for="(book, idx) in store.books"
            :key="book.id"
            class="course-card"
            :class="{ active: store.selectedBookId === book.id }"
            @click="selectBook(book)"
          >
            <div class="course-image" :style="{ background: gradients[idx % gradients.length] }">
              <span class="material-icons">{{ icons[idx % icons.length] }}</span>
              <span class="course-status">{{ store.selectedBookId === book.id ? '学习中' : '可选' }}</span>
            </div>
            <div class="course-info">
              <span class="material-icons">menu_book</span>
              <div>
                <strong>{{ book.bookname }}</strong>
                <span class="vocab-count">{{ (book.vocCount || 0).toLocaleString() }} 词</span>
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.book-library {
  padding: 32px 24px;
}

.library-grid {
  max-width: 900px;
  margin: 0 auto;
}

.section-header {
  margin-bottom: 24px;
}
.section-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.section-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.course-card {
  border: none;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  text-align: left;
  padding: 0;
}
.course-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.course-card.active {
  box-shadow: 0 0 0 2px var(--color-primary), var(--shadow-md);
}

.course-image {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.course-image .material-icons {
  font-size: 48px;
  color: rgba(255,255,255,0.5);
}
.course-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  background: rgba(0,0,0,0.45);
  color: #fff;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
}

.course-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
}
.course-info strong {
  display: block;
  font-size: 15px;
  color: var(--color-text-primary);
}
.vocab-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--color-text-muted);
  font-size: 14px;
}
.loading-spinner {
  width: 36px; height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 12px;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 640px) {
  .book-library { padding: 20px 12px; }
  .course-grid { grid-template-columns: 1fr; }
}
</style>
