<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useWordStore } from '../stores/word'
import { useSpeech } from '../composables/useSpeech'
import CompletionBanner from '../components/CompletionBanner.vue'
import DailyGoalBar from '../components/DailyGoalBar.vue'

const router = useRouter()
const store = useWordStore()
const { unlocked: audioUnlocked, unlock, playWord } = useSpeech()
const statusMsg = ref('')
const showCompletion = ref(false)

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
  unlock()
  store.markMastered(store.currentWord)
  statusMsg.value = '已存入掌握列表'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function handleFuzzy() {
  if (!store.currentWord) return
  unlock()
  store.markFuzzy(store.currentWord)
  statusMsg.value = '已标记为模糊'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function handleUnknown() {
  if (!store.currentWord) return
  unlock()
  store.markUnknown(store.currentWord)
  statusMsg.value = '已标记为不认识'
  setTimeout(() => (statusMsg.value = ''), 2000)
}

function handlePlayAudio() {
  unlock()
  playWord(store.currentWord)
}

// Auto-play only after first user interaction (avoids Chrome speechSynthesis blocking)
watch(() => store.currentWord, (newWord) => {
  if (newWord && audioUnlocked.value) playWord(newWord)
})

// Show completion banner when daily goal reached and auto-redirect
watch(() => store.dailyGoalReached, (reached) => {
  if (reached) {
    showCompletion.value = true
    setTimeout(() => router.push('/summary'), 1200)
  }
})

async function handleTrash() {
  if (!store.currentWord) return
  unlock()
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
    <DailyGoalBar />

    <!-- Completion Banner -->
    <CompletionBanner :show="showCompletion" @close="showCompletion = false" />

    <!-- Word Card -->
    <div class="word-card">
      <div class="word-main">
        <h1 class="word-spelling">{{ store.currentWord.spelling }}</h1>
        <div class="word-phonetic">
          {{ store.currentWord.phonetic || `/${store.currentWord.spelling}/` }}
          <button class="audio-btn" @click="handlePlayAudio" title="发音" aria-label="播放发音">
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
  cursor: pointer;
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
  text-decoration: none;
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
  cursor: pointer;
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
