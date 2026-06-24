<script setup>
import { onMounted, ref, watch } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()
const statusMsg = ref('')

onMounted(async () => {
  await store.settingsReady()
  store.fetchStats()
  if (!store.words.length) {
    if (store.selectedBookId) {
      store.selectBook(store.selectedBookId)
    } else {
      store.fetchWords()
    }
  }
})

function handleMastered() {
  if (!store.currentWord) return
  store.markMastered(store.currentWord)
  statusMsg.value = '已存入掌握列表'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function handleFuzzy() {
  if (!store.currentWord) return
  store.markFuzzy(store.currentWord)
  statusMsg.value = '已标记为模糊'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function handleUnknown() {
  if (!store.currentWord) return
  store.markUnknown(store.currentWord)
  statusMsg.value = '已标记为不认识'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function playAudio() {
  const word = store.currentWord?.spelling
  if (word && window.speechSynthesis) {
    const u = new SpeechSynthesisUtterance(word)
    u.lang = 'en-US'
    u.rate = 0.8
    speechSynthesis.speak(u)
  }
}

// Auto-play pronunciation when word changes
watch(() => store.currentWord, () => {
  if (store.currentWord) {
    setTimeout(() => playAudio(), 300)
  }
})
</script>

<template>
  <div class="first-sight" :class="{ 'large-font': store.largeFont }" v-if="store.currentWord">
    <!-- Progress -->
    <div class="progress-section">
      <div class="progress-label">
        学习进度
        <strong>{{ store.progress }} / {{ store.totalWords }}</strong>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: store.progressPercent + '%' }"></div>
      </div>
    </div>

    <!-- Daily Goal -->
    <div class="daily-goal-bar">
      <div class="daily-goal-header">
        <span>今日目标</span>
        <strong>{{ store.todayNewCount + store.todayReviewCount }} / {{ store.dailyGoal }}</strong>
      </div>
      <div class="daily-goal-track">
        <div class="dg-fill-new" :style="{ width: (store.todayNewCount / Math.max(store.dailyGoal, 1) * 100) + '%' }"></div>
        <div class="dg-fill-review" :style="{ width: (store.todayReviewCount / Math.max(store.dailyGoal, 1) * 100) + '%' }"></div>
      </div>
      <div class="daily-goal-legend">
        <span class="legend-new"><span class="dot"></span>新词 {{ store.todayNewCount }}</span>
        <span class="legend-review"><span class="dot"></span>复习 {{ store.todayReviewCount }}</span>
      </div>
    </div>

    <!-- Word Card -->
    <div class="word-card">
      <div class="word-main">
        <h1 class="word-spelling">{{ store.currentWord.spelling }}</h1>
        <div class="word-phonetic">
          {{ store.currentWord.phonetic || `/${store.currentWord.spelling}/` }}
          <button class="audio-btn" @click="playAudio" title="发音">
            <span class="material-icons">volume_up</span>
          </button>
        </div>
        <span class="pos-chip">{{ store.currentWord.partOfSpeech || 'noun' }}</span>
      </div>

      <router-link
        :to="`/word/${store.currentWord.id}`"
        class="detail-link"
      >
        <span class="material-icons">touch_app</span>
        点击查看详情
      </router-link>
    </div>

    <!-- Triple Action Buttons -->
    <div class="action-row">
      <button class="action-btn unknown" @click="handleUnknown">
        <span class="material-icons">close</span>
        <span class="action-label">不认识</span>
      </button>
      <button class="action-btn fuzzy" @click="handleFuzzy">
        <span class="material-icons">visibility_off</span>
        <span class="action-label">模糊</span>
      </button>
      <button class="action-btn mastered" @click="handleMastered">
        <span class="material-icons">check</span>
        <span class="action-label">认识</span>
      </button>
    </div>

    <!-- Status Banner -->
    <transition name="fade">
      <div v-if="statusMsg" class="status-banner">
        <span class="material-icons">check_circle</span>
        {{ statusMsg }}
      </div>
    </transition>

  </div>

  <!-- Loading / Empty -->
  <div v-else class="empty-state">
    <div v-if="store.loading" class="loading-spinner"></div>
    <p v-else>暂无单词数据</p>
  </div>
</template>

<style scoped>
.first-sight {
  max-width: 640px;
  margin: 0 auto;
  padding: 32px 20px;
}

/* Progress */
.progress-section {
  margin-bottom: 32px;
}
.progress-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}
.progress-label strong {
  color: var(--color-text-primary);
}
.progress-bar {
  height: 6px;
  background: var(--color-border);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  transition: width 0.4s ease;
}

/* Daily Goal */
.daily-goal-bar {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 14px 18px;
  margin-bottom: 20px;
}
.daily-goal-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}
.daily-goal-header strong {
  color: var(--color-text-primary);
}
.daily-goal-track {
  display: flex;
  height: 6px;
  background: var(--color-border);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: 8px;
}
.dg-fill-new {
  height: 100%;
  background: var(--color-primary);
  transition: width 0.4s ease;
}
.dg-fill-review {
  height: 100%;
  background: var(--color-warning);
  transition: width 0.4s ease;
}
.daily-goal-legend {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-muted);
}
.legend-new .dot, .legend-review .dot {
  display: inline-block;
  width: 8px; height: 8px;
  border-radius: 50%;
  margin-right: 4px;
  vertical-align: middle;
}
.legend-new .dot { background: var(--color-primary); }
.legend-review .dot { background: var(--color-warning); }

/* Word Card */
.word-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: 48px 40px;
  text-align: center;
  margin-bottom: 24px;
}
.word-spelling {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}
.word-phonetic {
  font-size: 16px;
  color: var(--color-text-secondary);
  font-family: 'Segoe UI', monospace;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
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
  transition: background 0.15s;
}
.audio-btn:hover { background: #dde4ff; }

.pos-chip {
  display: inline-block;
  padding: 4px 16px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
}
.detail-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 500;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  transition: background 0.15s;
}
.detail-link:hover {
  background: var(--color-primary-light);
}

/* Action Buttons */
.action-row {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 32px;
}
.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 28px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  transition: all 0.15s;
  min-width: 100px;
}
.action-btn .material-icons { font-size: 28px; }
.action-label { font-size: 13px; font-weight: 500; }

.action-btn.unknown:hover {
  border-color: var(--color-danger);
  background: var(--color-danger-light);
  color: var(--color-danger);
}
.action-btn.fuzzy:hover {
  border-color: var(--color-warning);
  background: var(--color-warning-light);
  color: var(--color-warning);
}
.action-btn.mastered:hover {
  border-color: var(--color-success);
  background: var(--color-success-light);
  color: var(--color-success);
}

/* Status Banner */
.status-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  background: var(--color-success-light);
  color: #065f46;
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 32px;
}
.status-banner .material-icons {
  color: var(--color-success);
}

/* Empty */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-muted);
}
.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 640px) {
  .first-sight { padding: 20px 12px; }
  .word-card { padding: 32px 20px; }
  .word-spelling { font-size: 28px; }
  .action-row { gap: 8px; }
  .action-btn { padding: 12px 16px; min-width: 80px; }
}
</style>
