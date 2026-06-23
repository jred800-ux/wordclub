<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useWordStore } from '../stores/word'

const route = useRoute()
const store = useWordStore()

onMounted(() => {
  if (!store.words.length) store.fetchWords()
})

const word = computed(() => {
  const id = Number(route.params.id)
  return store.words.find(w => w.id === id) || store.currentWord
})

const morphemes = computed(() => {
  if (!word.value) return []
  const w = word.value
  return [
    { type: '前缀', typeEn: 'Prefix', text: w.prefix || '—', color: '#f59e0b', desc: '单词前缀' },
    { type: '词根', typeEn: 'Root', text: w.root || w.spelling?.slice(0, 4) || '—', color: '#10b981', desc: '核心词根，承载主要含义' },
    { type: '后缀', typeEn: 'Suffix', text: w.suffix || '—', color: '#3b82f6', desc: '决定词性' },
  ]
})

const sentences = computed(() => {
  if (!word.value) return []
  const w = word.value.spelling || ''
  return [
    { en: `She was ${w} in her efforts to learn English.`, zh: '她在学习英语方面坚持不懈。' },
    { en: `The ${w} symptoms required further examination.`, zh: '持续的症状需要进一步检查。' },
  ]
})

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
  <div class="word-detail" v-if="word">
    <!-- Hero -->
    <section class="hero-section">
      <h1 class="hero-word">{{ word.spelling }}</h1>
      <div class="hero-phonetic">
        {{ word.phonetic || `/${word.spelling}/` }}
        <button class="audio-btn" @click="playWord" title="发音">
          <span class="material-icons">volume_up</span>
        </button>
      </div>
      <span class="pos-badge">{{ word.partOfSpeech || 'noun' }}</span>
    </section>

    <!-- Morpheme Analysis -->
    <section class="section-card">
      <h3 class="section-title">
        <span class="material-icons">schema</span> 词素分析
      </h3>
      <div class="morpheme-grid">
        <div v-for="m in morphemes" :key="m.type" class="morpheme-item">
          <div class="morpheme-chip" :style="{ background: m.color }">
            {{ m.text }}
          </div>
          <div class="morpheme-type">{{ m.type }}</div>
          <div class="morpheme-type-en">{{ m.typeEn }}</div>
          <div class="morpheme-desc">{{ m.desc }}</div>
        </div>
      </div>
    </section>

    <!-- Definitions -->
    <section class="section-card">
      <h3 class="section-title">
        <span class="material-icons">translate</span> 释义
      </h3>
      <div class="def-block">
        <div class="def-label">English</div>
        <p class="def-text">Continuing firmly or obstinately in a course of action in spite of difficulty or opposition.</p>
      </div>
      <div class="def-block">
        <div class="def-label">中文</div>
        <p class="def-text">{{ word.meaning }}</p>
      </div>
    </section>

    <!-- Example Sentences -->
    <section class="section-card">
      <h3 class="section-title">
        <span class="material-icons">format_quote</span> 例句
      </h3>
      <div class="sentence-list">
        <div v-for="(s, idx) in sentences" :key="idx" class="sentence-item">
          <button class="play-btn" @click="playSentence(s.en)">
            <span class="material-icons">play_circle</span>
          </button>
          <div class="sentence-content">
            <p class="sentence-en">{{ s.en }}</p>
            <p class="sentence-zh">{{ s.zh }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Pagination -->
    <div class="pagination-row">
      <router-link to="/learn/first-sight" class="btn-primary">
        下一个 <span class="material-icons">arrow_forward</span>
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

/* Hero */
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

/* Section Cards */
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

/* Morphemes */
.morpheme-grid {
  display: flex;
  gap: 16px;
}
.morpheme-item {
  flex: 1;
  text-align: center;
  padding: 16px 8px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
}
.morpheme-chip {
  display: inline-flex;
  padding: 6px 20px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  border-radius: var(--radius-md);
  margin-bottom: 8px;
}
.morpheme-type {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.morpheme-type-en {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-bottom: 4px;
}
.morpheme-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.4;
}

/* Definitions */
.def-block {
  margin-bottom: 16px;
}
.def-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}
.def-text {
  font-size: 15px;
  color: var(--color-text-primary);
  line-height: 1.7;
}

/* Sentences */
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

/* Pagination */
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

/* Empty */
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
  .morpheme-grid { flex-direction: column; gap: 8px; }
}
</style>
