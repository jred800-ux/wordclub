<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useWordStore } from '../stores/word'

const route = useRoute()
const store = useWordStore()

const word = ref(null)
const examples = ref([])
const isFav = ref(false)
const loading = ref(true)

onMounted(async () => {
  const id = Number(route.params.id)
  const data = await store.fetchWordDetail(id)
  if (data) {
    word.value = data.word || data
    examples.value = data.examples || []
  }
  try {
    const api = (await import('../api')).default
    const res = await api.get(`/learning/favorites/${id}/check`)
    isFav.value = res?.data ?? res ?? false
  } catch (e) {
    // Favorite check is non-critical — ignore and leave isFav as false
    console.warn('[WordDetail] Favorite check failed:', e.message)
  }
  loading.value = false
})

const wordPos = () => {
  if (!word.value) return 'word'
  const p = word.value.paraphrase || word.value.meaning || ''
  const match = p.match(/^([a-z]+)\.?\s/i)
  return match ? match[1] : 'word'
}

async function toggleFav() {
  await store.toggleFavorite(word.value.id)
  isFav.value = !isFav.value
}

function playSentence(text) {
  if (window.speechSynthesis) {
    const u = new SpeechSynthesisUtterance(text)
    u.lang = 'en-US'
    u.rate = 0.85
    speechSynthesis.speak(u)
  }
}

function playWord() {
  if (!word.value || !window.speechSynthesis) return
  const u = new SpeechSynthesisUtterance(word.value.spelling)
  u.lang = 'en-US'
  u.rate = 0.8
  speechSynthesis.speak(u)
}
</script>

<template>
  <div class="word-detail" v-if="word && !loading">
    <section class="hero-section">
      <h1 class="hero-word">{{ word.spelling }}</h1>
      <div class="hero-phonetic">
        <span v-if="word.ukPhonetic">UK {{ word.ukPhonetic }}</span>
        <span v-if="word.usPhonetic">US {{ word.usPhonetic }}</span>
        <span v-if="!word.ukPhonetic && !word.usPhonetic">/{{ word.spelling }}/</span>
        <button class="audio-btn" @click="playWord" title="发音">
          <span class="material-icons">volume_up</span>
        </button>
      </div>
      <span class="pos-badge">{{ wordPos() }}</span>
      <button class="fav-btn" :class="{ active: isFav }" @click="toggleFav" :title="isFav ? '取消收藏' : '收藏'">
        <span class="material-icons">{{ isFav ? 'favorite' : 'favorite_border' }}</span>
      </button>
    </section>

    <section class="section-card">
      <h3 class="section-title">
        <span class="material-icons">translate</span> 释义
      </h3>
      <div class="def-block">
        <p class="def-text">{{ word.paraphrase || word.meaning || '暂无释义' }}</p>
      </div>
      <div v-if="word.frequency != null" class="freq-badge">
        词频: {{ (word.frequency * 100).toFixed(1) }}%
      </div>
    </section>

    <section class="section-card" v-if="examples.length">
      <h3 class="section-title">
        <span class="material-icons">format_quote</span> 例句 ({{ examples.length }})
      </h3>
      <div class="sentence-list">
        <div v-for="ex in examples.slice(0, 5)" :key="ex.id" class="sentence-item">
          <button class="play-btn" @click="playSentence(ex.enSentence)">
            <span class="material-icons">play_circle</span>
          </button>
          <div class="sentence-content">
            <p class="sentence-en">{{ ex.enSentence }}</p>
            <p class="sentence-zh">{{ ex.cnSentence }}</p>
          </div>
        </div>
      </div>
    </section>

    <div class="pagination-row">
      <router-link to="/learn/first-sight" class="btn-primary">
        继续学习 <span class="material-icons">arrow_forward</span>
      </router-link>
    </div>
  </div>

  <div v-else class="empty-state">
    <div v-if="store.loading" class="loading-spinner"></div>
    <p v-else>暂无单词数据</p>
  </div>
</template>

<style scoped>
.word-detail {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 20px;
}

.hero-section {
  text-align: center;
  padding: 40px 0 32px;
}
.hero-word {
  font-size: 42px;
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -1px;
  margin-bottom: 8px;
}
.hero-phonetic {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  color: var(--color-text-secondary);
  font-family: 'Segoe UI', monospace;
  margin-bottom: 12px;
}
.audio-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-full);
}
.audio-btn:hover { background: #dde4ff; }
.pos-badge {
  display: inline-block;
  padding: 4px 18px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 500;
}
.fav-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px; height: 32px;
  border: none; background: none;
  color: var(--color-text-muted);
  border-radius: var(--radius-full);
  vertical-align: middle;
  margin-left: 8px;
}
.fav-btn.active { color: var(--color-danger); }
.fav-btn:hover { background: var(--color-divider); }
.freq-badge {
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.section-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 24px;
  margin-bottom: 20px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 20px;
}
.section-title .material-icons {
  color: var(--color-primary);
}

.def-block {
  margin-bottom: 16px;
}
.def-text {
  font-size: 15px;
  color: var(--color-text-primary);
  line-height: 1.7;
}

.sentence-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sentence-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
}
.play-btn {
  display: flex;
  align-items: flex-start;
  padding: 2px 0;
  border: none;
  background: none;
  color: var(--color-primary);
  cursor: pointer;
  flex-shrink: 0;
}
.play-btn .material-icons { font-size: 24px; }
.sentence-en {
  font-size: 15px;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.sentence-zh {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.pagination-row {
  text-align: center;
  margin-top: 24px;
}
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px;
  border: none;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: background 0.15s;
}
.btn-primary:hover { background: var(--color-primary-dark); }

.empty-state { text-align: center; padding: 80px 20px; color: var(--color-text-muted); }
.loading-spinner {
  width: 36px; height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 640px) {
  .word-detail { padding: 20px 12px; }
  .hero-word { font-size: 32px; }
}
</style>
