<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useWordStore } from '../stores/word'

const router = useRouter()
const store = useWordStore()
const statusMsg = ref('')
const audioUnlocked = ref(false)
const showCompletion = ref(false)
const checkinMsg = ref('')

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
  audioUnlocked.value = true
  store.markMastered(store.currentWord)
  statusMsg.value = '已存入掌握列表'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function handleFuzzy() {
  if (!store.currentWord) return
  audioUnlocked.value = true
  store.markFuzzy(store.currentWord)
  statusMsg.value = '已标记为模糊'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function handleUnknown() {
  if (!store.currentWord) return
  audioUnlocked.value = true
  store.markUnknown(store.currentWord)
  statusMsg.value = '已标记为不认识'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function playAudio() {
  audioUnlocked.value = true
  const word = store.currentWord?.spelling
  if (word && window.speechSynthesis) {
    const u = new SpeechSynthesisUtterance(word)
    u.lang = 'en-US'
    u.rate = 0.8
    speechSynthesis.speak(u)
  }
}

// Auto-play only after first user interaction (avoids Chrome speechSynthesis blocking)
watch(() => store.currentWord, (newWord) => {
  if (newWord && audioUnlocked.value) playAudio()
})

// Show completion banner when daily goal reached and auto-redirect
watch(() => store.dailyGoalReached, (reached) => {
  if (reached) {
    showCompletion.value = true
    if (!store.checkedInToday) {
      // Auto-redirect to study summary after a short delay so user sees the completion
      setTimeout(() => router.push('/summary'), 1200)
    } else {
      setTimeout(() => router.push('/summary'), 1200)
    }
  }
})

async function handleCheckin() {
  try {
    const result = await store.doCheckin()
    checkinMsg.value = `已连续打卡 ${result.streak} 天!`
  } catch (e) {
    checkinMsg.value = e.response?.data?.message || '打卡失败'
  }
}

async function handleTrash() {
  if (!store.currentWord) return
  audioUnlocked.value = true
  await store.addToBlacklist(store.currentWord.id)
  statusMsg.value = '已扔进垃圾桶'
  setTimeout(() => {
    statusMsg.value = ''
    store.nextWord()
  }, 800)
}
</script>

<template>
  <div class="first-sight" :class="{ 'large-font': store.largeFont }" v-if="store.currentWord">
    <!-- Daily Goal -->
    <div class="daily-goal-bar">
      <div class="daily-goal-header">
        <span>今日目标</span>
        <strong>{{ store.todayNewCount + store.todayReviewCount }} / {{ store.dailyGoal }}</strong>
      </div>
      <div class="daily-goal-track">
        <div class="dg-fill-new" :style="{ width: (Math.min(store.todayNewCount, store.newWordCount) / Math.max(store.dailyGoal, 1) * 100) + '%' }"></div>
        <div class="dg-fill-review" :style="{ width: (store.todayReviewCount / Math.max(store.dailyGoal, 1) * 100) + '%' }"></div>
      </div>
      <div class="daily-goal-legend">
        <span class="legend-new"><span class="dot"></span>新词 {{ store.todayNewCount }} / {{ store.newWordCount }}</span>
        <span class="legend-review"><span class="dot"></span>复习 {{ store.todayReviewCount }} / {{ store.effectiveReviewTarget }}</span>
      </div>
    </div>

    <!-- Completion Banner -->
    <div v-if="showCompletion" class="completion-banner">
      <div class="completion-content">
        <span class="material-icons celebration-icon">emoji_events</span>
        <h3>今日目标达成!</h3>
        <p>你已经学完了今日计划的 {{ store.dailyGoal }} 个单词</p>
        <button
          v-if="!store.checkedInToday"
          class="checkin-btn"
          @click="handleCheckin"
          :disabled="!!checkinMsg"
        >
          <span class="material-icons">how_to_reg</span>
          {{ checkinMsg || '打卡记录' }}
        </button>
        <p v-else class="already-checkedin">
          <span class="material-icons">check_circle</span> 今日已打卡 · 连续 {{ store.streakDays }} 天
        </p>
        <button class="text-btn" @click="showCompletion = false">继续学习</button>
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
      <button class="action-btn trash" @click="handleTrash">
        <span class="material-icons">delete_outline</span>
        <span class="action-label">太简单</span>
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
.action-btn.trash:hover {
  border-color: #9ca3af;
  background: #f3f4f6;
  color: #6b7280;
}

/* Completion Banner */
.completion-banner {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border: 1px solid #fcd34d;
  border-radius: var(--radius-lg);
  padding: 24px;
  text-align: center;
  margin-bottom: 20px;
}
.completion-content h3 {
  font-size: 20px;
  font-weight: 700;
  color: #92400e;
  margin: 8px 0 4px;
}
.completion-content p {
  font-size: 13px;
  color: #a16207;
  margin-bottom: 16px;
}
.celebration-icon {
  font-size: 40px;
  color: #f59e0b;
}
.checkin-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 28px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  margin-bottom: 8px;
}
.checkin-btn:disabled { opacity: 0.7; cursor: default; }
.already-checkedin {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #059669;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
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
