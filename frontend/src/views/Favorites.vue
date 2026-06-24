<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWordStore } from '../stores/word'
import api from '../api'

const router = useRouter()
const store = useWordStore()
const favWords = ref([])
const loading = ref(true)

onMounted(async () => {
  await loadFavorites()
})

async function loadFavorites() {
  loading.value = true
  try {
    const data = await api.get('/learning/favorites/words')
    favWords.value = data.data || data || []
  } catch (e) {
    console.error('[Favorites] load failed:', e.message)
  } finally {
    loading.value = false
  }
}

function goToWord(wordId) {
  router.push(`/word/${wordId}`)
}

async function removeFavorite(wordId) {
  await store.toggleFavorite(wordId)
  favWords.value = favWords.value.filter(w => w.id !== wordId)
}

function playWord(word) {
  if (!word || !window.speechSynthesis) return
  const u = new SpeechSynthesisUtterance(word.spelling)
  u.lang = 'en-US'; u.rate = 0.8
  speechSynthesis.speak(u)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2>
        <span class="material-icons">bookmark</span>
        生词本
      </h2>
      <span class="fav-count" v-if="favWords.length">{{ favWords.length }} 个单词</span>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="empty-state">
      <div class="loading-spinner"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="!favWords.length" class="empty-state">
      <span class="material-icons empty-icon">bookmark_border</span>
      <p>生词本还是空的</p>
      <p class="empty-hint">学习时点击「不认识」，单词会自动加入生词本</p>
      <button class="go-learn-btn" @click="router.push('/')">去学习</button>
    </div>

    <!-- Word list -->
    <div v-else class="word-list">
      <div
        v-for="word in favWords"
        :key="word.id"
        class="word-item"
        @click="goToWord(word.id)"
      >
        <div class="word-left">
          <button class="mini-audio-btn" @click.stop="playWord(word)" title="发音">
            <span class="material-icons">volume_up</span>
          </button>
          <div class="word-info">
            <div class="word-spell">{{ word.spelling }}</div>
            <div class="word-para">{{ word.paraphrase || '暂无释义' }}</div>
          </div>
        </div>
        <div class="word-right">
          <span class="fav-date">{{ formatDate(word.favoritedAt) }}</span>
          <button class="unfav-btn" @click.stop="removeFavorite(word.id)" title="移出生词本">
            <span class="material-icons">close</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.favorites-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 32px 20px 40px;
}

.page-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}
.page-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}
.page-header .material-icons {
  color: #f59e0b;
}
.fav-count {
  font-size: 13px;
  color: var(--color-text-muted);
}

/* Empty */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--color-text-muted);
}
.empty-icon {
  font-size: 56px;
  color: var(--color-border);
  margin-bottom: 12px;
  display: block;
}
.empty-state p {
  font-size: 15px;
  margin-bottom: 6px;
}
.empty-hint {
  font-size: 13px;
  color: var(--color-text-muted);
}
.go-learn-btn {
  margin-top: 16px;
  padding: 10px 28px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.loading-spinner {
  width: 36px; height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Word list */
.word-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.word-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.15s;
}
.word-item:hover {
  background: var(--color-divider);
}
.word-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}
.mini-audio-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px; height: 32px;
  border: none;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  cursor: pointer;
  flex-shrink: 0;
}
.mini-audio-btn .material-icons {
  font-size: 18px;
}
.word-info {
  min-width: 0;
}
.word-spell {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.word-para {
  font-size: 13px;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.word-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.fav-date {
  font-size: 11px;
  color: var(--color-text-muted);
}
.unfav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border: none;
  background: none;
  color: var(--color-text-muted);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: color 0.15s, background 0.15s;
}
.unfav-btn:hover {
  color: var(--color-danger);
  background: var(--color-danger-light);
}
.unfav-btn .material-icons {
  font-size: 16px;
}

@media (max-width: 640px) {
  .favorites-page {
    padding: 20px 12px 32px;
  }
}
</style>
