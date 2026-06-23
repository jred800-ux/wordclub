<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useWordStore } from '../stores/word'

const route = useRoute()
const router = useRouter()
const store = useWordStore()

const pageSize = 20

const bookId = computed(() => Number(route.params.bookId))

onMounted(async () => {
  await store.fetchBooks()
  store.selectBook(bookId.value)
})

function goPage(p) {
  if (p >= 0 && p < store.totalPages) {
    store.fetchWords(p, pageSize)
  }
}

function goWord(word) {
  router.push(`/word/${word.id}`)
}

function goBack() {
  router.push('/library')
}

const pageNumbers = computed(() => {
  const total = store.totalPages
  const current = store.currentPage
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i)
  }
  const pages = []
  pages.push(0)
  if (current > 2) pages.push(-1)
  const start = Math.max(1, current - 1)
  const end = Math.min(total - 2, current + 1)
  for (let i = start; i <= end; i++) pages.push(i)
  if (current < total - 3) pages.push(-1)
  pages.push(total - 1)
  return pages
})

function getMeaning(paraphrase) {
  if (!paraphrase) return ''
  const parts = paraphrase.split('\n')
  return parts[0].length > 40 ? parts[0].slice(0, 40) + '...' : parts[0]
}
</script>

<template>
  <div class="book-words">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <span class="material-icons">arrow_back</span>
        返回词库
      </button>
      <div class="header-info">
        <h1>{{ store.selectedBook?.bookname || '加载中...' }}</h1>
        <span class="word-count">{{ (store.totalElements || 0).toLocaleString() }} 词</span>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="store.loading" class="state-wrap">
      <span class="material-icons spin">sync</span>
      <p>加载中...</p>
    </div>

    <!-- Error -->
    <div v-else-if="store.error" class="state-wrap error">
      <span class="material-icons">error_outline</span>
      <p>{{ store.error }}</p>
      <button class="retry-btn" @click="store.fetchWords(store.currentPage, pageSize)">重试</button>
    </div>

    <!-- Empty -->
    <div v-else-if="!store.words.length" class="state-wrap">
      <span class="material-icons">menu_book</span>
      <p>该词书暂无单词</p>
    </div>

    <!-- Word list -->
    <template v-else>
      <div class="word-table">
        <div class="table-header">
          <span class="col-index">#</span>
          <span class="col-spell">单词</span>
          <span class="col-phonetic">音标</span>
          <span class="col-meaning">释义</span>
        </div>
        <button
          v-for="(word, idx) in store.words"
          :key="word.id"
          class="word-row"
          @click="goWord(word)"
        >
          <span class="col-index">{{ store.currentPage * pageSize + idx + 1 }}</span>
          <span class="col-spell">{{ word.spelling }}</span>
          <span class="col-phonetic">{{ word.ukPhonetic || '' }}</span>
          <span class="col-meaning">{{ getMeaning(word.paraphrase) }}</span>
          <span class="material-icons chevron">chevron_right</span>
        </button>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="store.totalPages > 1">
        <button
          class="page-btn"
          :disabled="store.currentPage <= 0"
          @click="goPage(store.currentPage - 1)"
        >
          <span class="material-icons">chevron_left</span>
        </button>
        <template v-for="p in pageNumbers" :key="p">
          <span v-if="p === -1" class="page-ellipsis">...</span>
          <button
            v-else
            class="page-btn"
            :class="{ active: p === store.currentPage }"
            @click="goPage(p)"
          >
            {{ p + 1 }}
          </button>
        </template>
        <button
          class="page-btn"
          :disabled="store.currentPage >= store.totalPages - 1"
          @click="goPage(store.currentPage + 1)"
        >
          <span class="material-icons">chevron_right</span>
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.book-words {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

/* Header */
.page-header {
  margin-bottom: 24px;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 12px;
}
.back-btn:hover {
  background: var(--color-bg);
}
.back-btn .material-icons {
  font-size: 18px;
}
.header-info {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.header-info h1 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
}
.word-count {
  font-size: 14px;
  color: var(--color-text-muted);
}

/* States */
.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  color: var(--color-text-muted);
  gap: 12px;
}
.state-wrap .material-icons {
  font-size: 40px;
}
.state-wrap p {
  font-size: 15px;
}
.state-wrap.error { color: var(--color-error); }
.retry-btn {
  padding: 8px 20px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: 14px;
  cursor: pointer;
  margin-top: 8px;
}
.retry-btn:hover { opacity: 0.9; }

.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Table */
.word-table {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}
.table-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.word-row {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: background 0.12s;
  border-bottom: 1px solid var(--color-border);
}
.word-row:last-child { border-bottom: none; }
.word-row:hover { background: var(--color-bg); }

.col-index {
  width: 48px;
  font-size: 12px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}
.col-spell {
  flex: 0 0 160px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.col-phonetic {
  flex: 0 0 140px;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.col-meaning {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chevron {
  font-size: 18px;
  color: var(--color-text-muted);
  flex-shrink: 0;
  margin-left: 8px;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 24px;
}
.page-btn {
  min-width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}
.page-btn:hover:not(:disabled) { background: var(--color-bg); }
.page-btn.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.page-btn:disabled {
  opacity: 0.35;
  cursor: default;
}
.page-btn .material-icons {
  font-size: 20px;
}
.page-ellipsis {
  width: 36px;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 14px;
}

@media (max-width: 640px) {
  .book-words { padding: 16px 12px; }
  .col-phonetic { display: none; }
  .col-spell { flex: 0 0 120px; font-size: 14px; }
  .back-btn { font-size: 12px; }
}
</style>
