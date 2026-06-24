<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()
const letterSlots = ref([])
const activeIndex = ref(0)
const showHint = ref(false)
const submitted = ref(false)
const isCorrect = ref(false)
const shakeKey = ref(0)

onMounted(() => {
  if (!store.words.length) {
    if (store.selectedBookId) {
      store.selectBook(store.selectedBookId)
    } else {
      store.fetchWords()
    }
  }
  setupSlots()
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})

const word = computed(() => store.currentWord)

function setupSlots() {
  if (!word.value) return
  const len = word.value.spelling.length
  letterSlots.value = Array.from({ length: len }, (_, i) => ({
    letter: i === 0 || i === len - 1 ? word.value.spelling[i] : '',
    prefill: i === 0 || i === len - 1,
  }))
  activeIndex.value = letterSlots.value.findIndex(s => !s.prefill)
  showHint.value = false
  submitted.value = false
  isCorrect.value = false
}

function onKeydown(e) {
  if (submitted.value) return
  if (e.key === 'Enter') { submitWord(); return }
  if (e.ctrlKey && e.key === 'h') { revealHint(); e.preventDefault(); return }

  if (/^[a-zA-Z]$/.test(e.key)) {
    const idx = findNextEmpty(activeIndex.value)
    if (idx === -1) return
    letterSlots.value[idx].letter = e.key.toLowerCase()
    activeIndex.value = findNextEmpty(idx + 1)
  }
  if (e.key === 'Backspace') {
    if (activeIndex.value > 0) {
      const prev = findPrevEditable(activeIndex.value - 1)
      if (prev !== -1) {
        letterSlots.value[prev].letter = ''
        activeIndex.value = prev
      }
    }
  }
}

function findNextEmpty(start) {
  for (let i = start; i < letterSlots.value.length; i++) {
    if (!letterSlots.value[i].prefill && !letterSlots.value[i].letter) return i
  }
  return -1
}

function findPrevEditable(start) {
  for (let i = start; i >= 0; i--) {
    if (!letterSlots.value[i].prefill) return i
  }
  return -1
}

function setActive(idx) {
  if (submitted.value || letterSlots.value[idx].prefill) return
  activeIndex.value = idx
}

function revealHint() {
  const idx = findNextEmpty(0)
  if (idx === -1) return
  letterSlots.value[idx].letter = word.value.spelling[idx]
  activeIndex.value = findNextEmpty(idx + 1)
  showHint.value = true
  setTimeout(() => (showHint.value = false), 1500)
}

function submitWord() {
  const answer = letterSlots.value.map(s => s.letter).join('')
  isCorrect.value = answer === word.value.spelling
  submitted.value = true
  if (!isCorrect.value) {
    shakeKey.value++
  }
}

function nextWord() {
  store.nextWord()
  nextTick(setupSlots)
}

function skipWord() {
  store.skipWord()
  nextTick(setupSlots)
}

function playAudio() {
  if (!word.value || !window.speechSynthesis) return
  const u = new SpeechSynthesisUtterance(word.value.spelling)
  u.lang = 'en-US'
  u.rate = 0.8
  speechSynthesis.speak(u)
}

// Auto-play pronunciation when word changes
watch(word, (newWord) => {
  if (newWord) {
    setTimeout(() => playAudio(), 300)
  }
})
</script>

<template>
  <div class="spelling-mode" v-if="word">
    <!-- Progress -->
    <div class="progress-section">
      <div class="progress-label">
        学习进度 <strong>{{ store.progress }} / {{ store.totalWords }}</strong>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: store.progressPercent + '%' }"></div>
      </div>
    </div>

    <!-- Word Info -->
    <div class="spelling-card">
      <!-- Part of Speech -->
      <span class="pos-chip">{{ word.partOfSpeech || 'noun' }}</span>

      <!-- Definition -->
      <p class="definition">{{ word.meaning }}</p>

      <!-- Pronunciation -->
      <div class="phonetic-row">
        <span class="phonetic-text">{{ word.phonetic || `/${word.spelling}/` }}</span>
        <button class="audio-btn" @click="playAudio" title="发音">
          <span class="material-icons">volume_up</span>
        </button>
      </div>

      <!-- Letter Slots -->
      <div class="slots-row" :key="shakeKey">
        <button
          v-for="(slot, idx) in letterSlots"
          :key="idx"
          :class="[
            'letter-slot',
            {
              active: idx === activeIndex && !submitted,
              prefill: slot.prefill,
              filled: slot.letter && !slot.prefill,
              correct: submitted && isCorrect,
              wrong: submitted && !isCorrect && !slot.prefill && slot.letter !== word.spelling[idx]
            }
          ]"
          @click="setActive(idx)"
          :disabled="slot.prefill || submitted"
        >
          {{ slot.letter || '_' }}
        </button>
      </div>

      <!-- Result -->
      <div v-if="submitted" class="result-row">
        <div v-if="isCorrect" class="result-msg success">
          <span class="material-icons">check_circle</span> 正确！
        </div>
        <div v-else class="result-msg error">
          <span class="material-icons">cancel</span>
          正确答案：<strong>{{ word.spelling }}</strong>
        </div>
      </div>

      <!-- Hint toggle -->
      <div class="hint-row">
        <button class="text-btn" @click="revealHint" :disabled="submitted">
          <span class="material-icons">lightbulb</span> 提示 (Ctrl+H)
        </button>
      </div>

      <!-- Actions -->
      <div class="bottom-actions">
        <button
          v-if="!submitted"
          class="btn-primary"
          @click="submitWord"
          :disabled="letterSlots.every(s => s.prefill || s.letter)"
        >
          确认
          <span class="shortcut">ENTER 提交</span>
        </button>
        <button v-else class="btn-primary" @click="nextWord">
          下一个 <span class="material-icons">arrow_forward</span>
        </button>
        <button class="btn-ghost" @click="skipWord" :disabled="submitted">
          <span class="material-icons">skip_next</span> 跳过此词
        </button>
      </div>
    </div>
  </div>

  <div v-else class="empty-state">
    <div v-if="store.loading" class="loading-spinner"></div>
    <p v-else>暂无单词数据</p>
  </div>
</template>

<style scoped>
.spelling-mode {
  max-width: 600px;
  margin: 0 auto;
  padding: 32px 20px;
}

/* Progress */
.progress-section { margin-bottom: 32px; }
.progress-label { font-size: 14px; color: var(--color-text-secondary); margin-bottom: 8px; }
.progress-label strong { color: var(--color-text-primary); }
.progress-bar { height: 6px; background: var(--color-border); border-radius: var(--radius-full); overflow: hidden; }
.progress-fill { height: 100%; background: var(--color-primary); border-radius: var(--radius-full); transition: width 0.4s; }

/* Card */
.spelling-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: 40px;
  text-align: center;
}
.pos-chip {
  display: inline-block;
  padding: 4px 16px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 16px;
}
.definition {
  font-size: 18px;
  color: var(--color-text-primary);
  line-height: 1.7;
  margin-bottom: 12px;
}
.phonetic-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 32px;
}
.phonetic-text {
  font-size: 14px;
  color: var(--color-text-muted);
  font-family: 'Segoe UI', monospace;
}
.audio-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-full);
}

/* Letter Slots */
.slots-row {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.letter-slot {
  width: 42px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-bottom: 2px solid var(--color-border);
  background: transparent;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
}
.letter-slot:focus { outline: none; }
.letter-slot.active { border-bottom-color: var(--color-primary); }
.letter-slot.prefill { color: var(--color-text-muted); cursor: default; }
.letter-slot.filled { color: var(--color-text-primary); border-bottom-color: var(--color-primary); }
.letter-slot.correct { border-bottom-color: var(--color-success); color: var(--color-success); }
.letter-slot.wrong { border-bottom-color: var(--color-danger); color: var(--color-danger); animation: shake 0.4s; }

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

/* Result */
.result-row { margin-bottom: 16px; }
.result-msg {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 20px;
  border-radius: var(--radius-full);
}
.result-msg.success { background: var(--color-success-light); color: #065f46; }
.result-msg.error { background: var(--color-danger-light); color: #991b1b; }

/* Hint */
.hint-row { margin-bottom: 24px; }
.text-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: none;
  background: none;
  color: var(--color-warning);
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
}
.text-btn:hover:not(:disabled) { background: var(--color-warning-light); }
.text-btn:disabled { opacity: 0.4; cursor: default; }

/* Actions */
.bottom-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 32px;
  border: none;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);
  transition: background 0.15s, opacity 0.15s;
}
.btn-primary:hover:not(:disabled) { background: var(--color-primary-dark); }
.btn-primary:disabled { opacity: 0.6; cursor: default; }
.shortcut {
  font-size: 11px;
  opacity: 0.7;
  font-weight: 400;
}
.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border: none;
  background: none;
  color: var(--color-text-muted);
  font-size: 13px;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
}
.btn-ghost:hover:not(:disabled) { background: var(--color-divider); }

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
  .spelling-mode { padding: 20px 12px; }
  .spelling-card { padding: 24px 16px; }
  .letter-slot { width: 36px; height: 44px; font-size: 18px; }
}
</style>
