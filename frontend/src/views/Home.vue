<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWordStore } from '../stores/word'
import { watch } from 'vue'
import api from '../api'

const router = useRouter()
const store = useWordStore()

const bookProgress = ref(null)
const loadingProgress = ref(false)

const quotes = [
  { en: "The limits of my language mean the limits of my world.", zh: "语言的边界，即是世界的边界。", author: "Ludwig Wittgenstein" },
  { en: "To have another language is to possess a second soul.", zh: "掌握另一门语言，就是拥有第二个灵魂。", author: "Charlemagne" },
  { en: "Language is the road map of a culture.", zh: "语言是文化的路线图。", author: "Rita Mae Brown" },
  { en: "A different language is a different vision of life.", zh: "不同的语言，是不同的人生视角。", author: "Federico Fellini" },
  { en: "Words are, of course, the most powerful drug used by mankind.", zh: "语言是人类最强大的药物。", author: "Rudyard Kipling" },
  { en: "Learning is a treasure that will follow its owner everywhere.", zh: "学习是跟随主人到任何地方的财富。", author: "Chinese Proverb" },
  { en: "The beautiful thing about learning is nobody can take it away from you.", zh: "学习的美好之处在于，没有人能把它从你身上夺走。", author: "B.B. King" },
  { en: "One language sets you in a corridor for life. Two languages open every door along the way.", zh: "一种语言让你在人生走廊中前行，两种语言则打开沿途的每一扇门。", author: "Frank Smith" },
]

function randomQuote() {
  const saved = localStorage.getItem('homeQuote')
  if (saved) {
    try {
      const q = JSON.parse(saved)
      if (q.date === new Date().toDateString()) return q
    } catch (e) {}
  }
  const q = quotes[Math.floor(Math.random() * quotes.length)]
  localStorage.setItem('homeQuote', JSON.stringify({ ...q, date: new Date().toDateString() }))
  return q
}

const quote = ref(randomQuote())

const learnPath = computed(() =>
  store.learningMode === 'spelling' ? '/learn/spelling' : '/learn/first-sight'
)

const selectedBook = computed(() =>
  store.books.find(b => b.id === store.selectedBookId)
)

const completionPercent = computed(() => {
  if (!bookProgress.value) return 0
  return bookProgress.value.completionPercent || 0
})

const estimatedDate = computed(() => {
  if (!bookProgress.value?.estimatedCompletionDate) return null
  return bookProgress.value.estimatedCompletionDate
})

onMounted(async () => {
  await store.fetchBooks()
  store.fetchStats()
  if (!store.selectedBookId && store.books.length) store.selectBook(store.books[0].id)
  await fetchBookProgress()
})

async function fetchBookProgress() {
  if (!store.selectedBookId) return
  loadingProgress.value = true
  try {
    const data = await api.get(`/learning/progress/book/${store.selectedBookId}`, {
      params: { dailyNewWordCount: store.newWordCount }
    })
    bookProgress.value = data.data || data
  } catch (e) {
    console.error('[Home] fetchBookProgress:', e.message)
  } finally {
    loadingProgress.value = false
  }
}

watch(() => store.selectedBookId, () => fetchBookProgress())

function startLearning() {
  store.skipCheckin = false
  router.push(learnPath.value)
}

const toolChips = [
  { icon: 'bookmark',        label: '生词本',   color: '#f59e0b', to: '/favorites', disabled: false },
  { icon: 'autorenew',       label: '再来一组', color: '#3b82f6',
    action: () => { store.skipCheckin = true; router.push(learnPath.value) },
    disabled: !store.checkedInToday },
  { icon: 'quiz',            label: '单词测试', color: '#10b981', disabled: true },
  { icon: 'menu_book',       label: '词文串学', color: '#8b5cf6', disabled: true },
]

function chipClick(chip) {
  if (chip.disabled) return
  if (chip.to) router.push(chip.to)
  else if (chip.action) chip.action()
}
</script>

<template>
  <div class="home-dashboard">
    <!-- ===== 1. 学习天数 ===== -->
    <p class="streak-line">
      在 WordClub 已经学习了
      <span class="streak-num">{{ store.totalCheckins }}</span>
      天
      <span class="material-icons streak-fire" :class="{ lit: store.checkedInToday }">local_fire_department</span>
    </p>

    <!-- ===== 2. 词书进度卡片 (左中右结构) ===== -->
    <div class="book-progress-card" v-if="selectedBook">
      <div class="card-layout">
        <!-- 书封 -->
        <div class="card-cover">
          <span class="material-icons cover-icon">menu_book</span>
        </div>
        <!-- 中间: 书籍信息 + 进度条 -->
        <div class="card-left">
          <div class="book-info">
            <h2 class="book-name">{{ selectedBook.bookname }}</h2>
            <span class="book-type">乱序版</span>
          </div>
          <div class="card-meta" v-if="estimatedDate">
            <span class="material-icons meta-icon">event</span>
            预计 <strong>{{ estimatedDate }}</strong> 完成
          </div>
          <div class="progress-section">
            <div class="progress-bar-track">
              <div class="progress-bar-fill" :style="{ width: completionPercent + '%' }"></div>
            </div>
            <div class="progress-stats">
              <span>{{ bookProgress?.studiedCount || 0 }} / {{ bookProgress?.totalWords || 0 }}</span>
              <span class="progress-pct">{{ completionPercent }}%</span>
            </div>
          </div>
        </div>

        <!-- 右侧: 状态 + 词表 + 四个工具 -->
        <div class="card-right">
          <router-link :to="`/book/${store.selectedBookId}`" class="wordlist-btn">
            <span class="material-icons">list_alt</span>
            词表
            <span class="material-icons wordlist-arrow">chevron_right</span>
          </router-link>
          <span class="checkin-status" :class="{ done: store.checkedInToday }">
            <span class="material-icons checkin-dot">{{ store.checkedInToday ? 'check_circle' : 'radio_button_unchecked' }}</span>
            {{ store.checkedInToday ? '今日已打卡' : '今日未打卡' }}
          </span>
          <div class="card-tools">
            <div
              v-for="chip in toolChips"
              :key="chip.label"
              class="card-tool-chip"
              :class="{ disabled: chip.disabled }"
              @click="chipClick(chip)"
            >
              <span class="material-icons" :style="{ color: chip.disabled ? 'var(--color-text-muted)' : chip.color }">{{ chip.icon }}</span>
              <span class="card-tool-label">{{ chip.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- No book -->
    <div class="book-progress-card muted" v-else-if="!loadingProgress">
      <p>暂未选择词书，请前往 <router-link to="/library">词库</router-link> 选择</p>
    </div>

    <!-- ===== 3. CTA ===== -->
    <div class="cta-section">
      <button v-if="!store.checkedInToday" class="start-learning-btn" @click="startLearning">
        <span class="material-icons">play_arrow</span>
        开始学习
      </button>
      <button v-else class="achievement-btn" @click="router.push('/summary')">
        <span class="material-icons">emoji_events</span>
        今日成就
      </button>
    </div>

    <!-- ===== 4. 励志引语 ===== -->
    <div class="quote-card">
      <blockquote>
        <p class="quote-en">"{{ quote.en }}"</p>
        <p class="quote-zh">{{ quote.zh }}</p>
        <footer>— {{ quote.author }}</footer>
      </blockquote>
    </div>
  </div>
</template>

<style scoped>
.home-dashboard {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px 48px;
}

/* ===== 1. 学习天数 ===== */
.streak-line {
  text-align: center;
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0 0 28px 0;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
}
.streak-num {
  font-size: 40px;
  font-weight: 800;
  color: var(--color-text-primary);
  line-height: 1;
  margin: 0 2px;
}
.streak-fire {
  font-size: 28px;
  color: var(--color-border);
  transition: color 0.3s, text-shadow 0.3s;
  vertical-align: middle;
}
.streak-fire.lit {
  color: #f97316;
  text-shadow: 0 0 8px rgba(249, 115, 22, 0.4);
}

/* ===== 2. 词书进度卡片 ===== */
.book-progress-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  padding: 72px 44px;
  margin-bottom: 24px;
}
.book-progress-card.muted {
  text-align: center;
  color: var(--color-text-muted);
  padding: 48px 24px;
}
.book-progress-card.muted a {
  color: var(--color-primary);
  font-weight: 500;
}

/* ---- 左中右布局 ---- */
.card-layout {
  display: flex;
  gap: 36px;
  align-items: stretch;
}
/* 书封 */
.card-cover {
  flex-shrink: 0;
  width: 90px;
  height: 130px;
  background: linear-gradient(135deg, #e8ecf4 0%, #d5dae6 100%);
  border-radius: 6px 10px 10px 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 8px rgba(0,0,0,0.08), inset 0 1px 0 rgba(255,255,255,0.6);
  position: relative;
}
.card-cover::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: linear-gradient(to right, rgba(0,0,0,0.08), transparent);
  border-radius: 3px 0 0 3px;
}
.cover-icon {
  font-size: 36px;
  color: var(--color-text-muted);
}
.card-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.card-right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
  justify-content: center;
}

/* 打卡状态 — 水平居中 */
.checkin-status {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--color-text-muted);
  white-space: nowrap;
}
.checkin-status.done {
  color: #16a34a;
}
.checkin-dot {
  font-size: 18px;
}

/* 中间: 书名 + 类型 */
.book-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.book-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}
.book-type {
  padding: 3px 14px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
}

/* 预计完成 */
.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 24px;
}
.meta-icon { font-size: 18px; color: var(--color-primary); }

/* 进度条 */
.progress-section {
  /* fills remaining space naturally */
}
.progress-bar-track {
  height: 12px;
  background: var(--color-divider);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: 8px;
}
.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6 0%, #6366f1 100%);
  border-radius: var(--radius-full);
  transition: width 0.5s ease;
}
.progress-stats {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--color-text-secondary);
}
.progress-pct { font-weight: 700; color: var(--color-primary); }

/* 词表按钮 — 左半圆右直角，贴合卡片右边缘 */
.wordlist-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 9px 14px 9px 18px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  border-radius: 20px 0 0 20px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.15s;
  white-space: nowrap;
  align-self: flex-end;
  margin-right: -44px;
}
.wordlist-btn:hover { background: #dde4ff; }
.wordlist-btn .material-icons { font-size: 16px; }
.wordlist-arrow {
  font-size: 14px !important;
  opacity: 0.5;
}

/* 四个工具一排 */
.card-tools {
  display: flex;
  gap: 8px;
}
.card-tool-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  background: var(--color-bg);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}
.card-tool-chip:hover:not(.disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}
.card-tool-chip.disabled { opacity: 0.4; cursor: default; }
.card-tool-chip .material-icons { font-size: 22px; }
.card-tool-label { font-size: 12px; font-weight: 500; color: var(--color-text-secondary); white-space: nowrap; }

/* ===== 3. CTA ===== */
.cta-section { display: flex; justify-content: center; margin-bottom: 28px; }

.start-learning-btn, .achievement-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 40px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
}
.start-learning-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}
.start-learning-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}
.achievement-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}
.achievement-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
}
.start-learning-btn .material-icons,
.achievement-btn .material-icons { font-size: 20px; }

/* ===== 4. 励志引语 ===== */
.quote-card {
  text-align: center;
  padding: 40px 20px 0;
}
.quote-card blockquote { margin: 0; padding: 0; border: none; }
.quote-en {
  font-size: 15px;
  font-style: italic;
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
}
.quote-zh {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}
.quote-card footer {
  font-size: 12px;
  color: var(--color-primary);
  font-weight: 500;
  font-style: normal;
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .home-dashboard { padding: 16px 12px 32px; }
  .streak-num { font-size: 30px; }
  .book-progress-card { padding: 24px 20px; }
  .card-layout { flex-direction: column; gap: 20px; }
  .card-cover { width: 60px; height: 85px; align-self: center; }
  .card-right { flex-direction: row; flex-wrap: wrap; gap: 8px; }
  .wordlist-btn { margin-right: -20px; padding: 8px 12px 8px 16px; }
  .card-tools { flex-wrap: wrap; gap: 6px; }
  .card-tool-chip { flex: 1 1 calc(50% - 3px); min-width: 0; padding: 10px 6px; }
}

/* ===== Dark mode ===== */
:root.dark .card-cover {
  background: linear-gradient(135deg, #2a2d3a 0%, #1e2030 100%);
  box-shadow: 2px 2px 8px rgba(0,0,0,0.3), inset 0 1px 0 rgba(255,255,255,0.05);
}
:root.dark .card-cover::before {
  background: linear-gradient(to right, rgba(255,255,255,0.06), transparent);
}
</style>
