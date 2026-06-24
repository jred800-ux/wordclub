<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWordStore } from '../stores/word'
import api from '../api'

const router = useRouter()
const store = useWordStore()
const trashedWords = ref([])
const trashedCount = computed(() => trashedWords.value.length)

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
]
const icons = ['school', 'menu_book', 'auto_stories', 'flight_takeoff', 'translate', 'psychology']

const newWordOptions = [50, 75, 100, 125, 150, 175, 200]
const ratioOptions = [1, 2, 3, 4, 5]

const examDates = [
  { label: 'CET-4 (6月)', value: '2026-06-15' },
  { label: 'CET-6 (12月)', value: '2026-12-20' },
  { label: '考研 (12月)', value: '2026-12-26' },
]

onMounted(() => {
  store.fetchBooks()
  fetchTrash()
})

async function fetchTrash() {
  try {
    const res = await api.get('/learning/blacklist')
    trashedWords.value = (res.data || res) || []
  } catch (e) {
    console.error('fetchTrash:', e)
  }
}

async function restoreWord(wordId) {
  try {
    await api.delete(`/learning/blacklist/${wordId}`)
    trashedWords.value = trashedWords.value.filter(w => w.wordId !== wordId)
    store.removeFromBlacklist(wordId)
  } catch (e) {
    console.error('restoreWord:', e)
  }
}

function selectBook(book) {
  store.selectBook(book.id)
}

function startLearning() {
  const path = store.learningMode === 'spelling' ? '/learn/spelling' : '/learn/first-sight'
  router.push(path)
}

function resetProgress() {
  if (store.selectedBookId) {
    store.selectBook(store.selectedBookId, false)
  }
}
</script>

<template>
  <div class="settings-page">
    <div class="page-header">
      <h1>学习设置</h1>
      <p>配置你的词书、每日目标和学习偏好</p>
    </div>

    <!-- 1. 词书选择 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="material-icons">menu_book</span>
        <div>
          <strong>词书选择</strong>
          <p>选择你要学习的单词书，可随时切换</p>
        </div>
      </div>

      <div class="book-grid">
        <button
          v-for="(book, idx) in store.books"
          :key="book.id"
          class="book-card"
          :class="{ active: store.selectedBookId === book.id }"
          @click="selectBook(book)"
        >
          <div class="book-image" :style="{ background: gradients[idx % gradients.length] }">
            <span class="material-icons">{{ icons[idx % icons.length] }}</span>
            <span class="book-check" v-if="store.selectedBookId === book.id">
              <span class="material-icons">check_circle</span>
            </span>
          </div>
          <div class="book-info">
            <strong>{{ book.bookname }}</strong>
            <span>{{ (book.vocCount || 0).toLocaleString() }} 词</span>
          </div>
        </button>
      </div>
    </section>

    <!-- 2. 进度提示 -->
    <section v-if="store.bookProgress && store.bookProgress.studiedCount > 0" class="resume-card">
      <div class="resume-info">
        <span class="material-icons">history</span>
        <div>
          <strong>上次学习进度: {{ store.bookProgress.completionPercent }}% 已完成</strong>
          <p>已学 {{ store.bookProgress.studiedCount }} / {{ store.bookProgress.totalWords }} 词，掌握 {{ store.bookProgress.masteredCount }} 词</p>
        </div>
      </div>
      <div class="resume-actions">
        <button class="btn-resume" @click="startLearning">继续学习</button>
        <button class="btn-reset" @click="resetProgress">重新开始</button>
      </div>
    </section>

    <!-- 3. 每日目标 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="material-icons">flag</span>
        <div>
          <strong>每日目标</strong>
          <p>由新词和复习词组成，选择适合你的强度</p>
        </div>
      </div>

      <div class="goal-config">
        <!-- 新词数 -->
        <div class="goal-group">
          <label class="goal-label">新词数</label>
          <div class="pill-row">
            <button
              v-for="n in newWordOptions"
              :key="n"
              class="pill"
              :class="{ active: store.newWordCount === n }"
              @click="store.newWordCount = n"
            >{{ n }}</button>
          </div>
        </div>

        <!-- 复习比例 -->
        <div class="goal-group">
          <label class="goal-label">新词 : 复习</label>
          <div class="pill-row">
            <button
              v-for="r in ratioOptions"
              :key="r"
              class="pill"
              :class="{ active: store.reviewRatio === r }"
              @click="store.reviewRatio = r"
            >1 : {{ r }}</button>
          </div>
        </div>

        <!-- 计算结果 -->
        <div class="goal-summary">
          <div class="goal-stat">
            <span>新词数</span>
            <strong>{{ store.newWordCount }}</strong>
          </div>
          <span class="goal-plus">+</span>
          <div class="goal-stat">
            <span>复习词数</span>
            <strong>{{ store.reviewWordCount }}</strong>
          </div>
          <span class="goal-equals">=</span>
          <div class="goal-stat total">
            <span>每日总量</span>
            <strong>{{ store.dailyGoal }}</strong>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. 学习偏好 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="material-icons">tune</span>
        <div>
          <strong>学习偏好</strong>
          <p>自定义学习过程中的显示和交互方式</p>
        </div>
      </div>

      <div class="prefs-grid">
        <!-- Card Order -->
        <div class="pref-item">
          <div class="pref-info">
            <span class="material-icons">sort</span>
            <div>
              <strong>卡片顺序</strong>
              <p>按字母序或随机序展示单词卡片</p>
            </div>
          </div>
          <div class="toggle-btn-group">
            <button
              :class="['toggle-btn', { active: store.cardOrder === 'alphabetical' }]"
              @click="store.setOrder('alphabetical')"
            >字母序</button>
            <button
              :class="['toggle-btn', { active: store.cardOrder === 'random' }]"
              @click="store.setOrder('random')"
            >随机序</button>
          </div>
        </div>

        <!-- Large Font -->
        <div class="pref-item">
          <div class="pref-info">
            <span class="material-icons">format_size</span>
            <div>
              <strong>大字体</strong>
              <p>放大单词卡片中的文字</p>
            </div>
          </div>
          <label class="switch">
            <input type="checkbox" :checked="store.largeFont" @change="store.toggleLargeFont()" />
            <span class="slider"></span>
          </label>
        </div>

        <!-- Dark Mode -->
        <div class="pref-item">
          <div class="pref-info">
            <span class="material-icons">dark_mode</span>
            <div>
              <strong>深色模式</strong>
              <p>切换到深色主题配色</p>
            </div>
          </div>
          <label class="switch">
            <input type="checkbox" :checked="store.darkMode" @change="store.toggleDarkMode()" />
            <span class="slider"></span>
          </label>
        </div>
      </div>
    </section>

    <!-- 5. 考试目标 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="material-icons">event_available</span>
        <div>
          <strong>考试目标</strong>
          <p>设置考试日期，计算每日所需学习量</p>
        </div>
      </div>

      <div class="exam-select">
        <select v-model="store.examDate">
          <option value="">不设置考试目标</option>
          <option v-for="d in examDates" :key="d.value" :value="d.value">{{ d.label }}</option>
        </select>
      </div>
    </section>

    <!-- 6. 垃圾桶 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="material-icons">delete_outline</span>
        <div>
          <strong>垃圾桶</strong>
          <p>被你标记为"太简单"的单词（共 {{ trashedCount }} 个）</p>
        </div>
      </div>

      <div v-if="trashedWords.length === 0" class="empty-hint">
        垃圾桶是空的。在学习时将太简单的词扔进垃圾桶，它们就不会再出现在每日背诵中。
      </div>

      <div v-else class="trash-list">
        <div v-for="item in trashedWords" :key="item.wordId" class="trash-item">
          <div class="trash-word-info">
            <strong>{{ item.spelling }}</strong>
            <span>{{ item.paraphrase }}</span>
          </div>
          <button class="btn-restore" @click="restoreWord(item.wordId)">
            <span class="material-icons">restore</span> 恢复
          </button>
        </div>
      </div>
    </section>

    <!-- 7. 学习模式 -->
    <section class="settings-section">
      <div class="section-title">
        <span class="material-icons">psychology</span>
        <div>
          <strong>学习模式</strong>
          <p>选择默认的学习方式，控制台点击"开始学习"将直接进入此模式</p>
        </div>
      </div>

      <div class="mode-select">
        <button
          class="mode-option"
          :class="{ active: store.learningMode === 'first-sight' }"
          @click="store.setLearningMode('first-sight')"
        >
          <span class="material-icons mode-opt-icon">visibility</span>
          <div class="mode-opt-text">
            <strong>认读模式</strong>
            <p>看到单词，判断是否认识</p>
          </div>
          <span v-if="store.learningMode === 'first-sight'" class="material-icons check-icon">check_circle</span>
        </button>
        <button
          class="mode-option"
          :class="{ active: store.learningMode === 'spelling' }"
          @click="store.setLearningMode('spelling')"
        >
          <span class="material-icons mode-opt-icon">edit</span>
          <div class="mode-opt-text">
            <strong>拼写模式</strong>
            <p>根据释义拼写出单词</p>
          </div>
          <span v-if="store.learningMode === 'spelling'" class="material-icons check-icon">check_circle</span>
        </button>
      </div>
    </section>

    <!-- CTA -->
    <div class="settings-cta">
      <button class="cta-btn" @click="startLearning">
        保存并开始学习
        <span class="material-icons">arrow_forward</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 20px 48px;
}

.page-header {
  margin-bottom: 32px;
}
.page-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.page-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* Section */
.settings-section {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 24px;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  gap: 14px;
  margin-bottom: 20px;
}
.section-title .material-icons {
  font-size: 28px;
  color: var(--color-primary);
  margin-top: 2px;
}
.section-title strong {
  display: block;
  font-size: 16px;
  color: var(--color-text-primary);
  margin-bottom: 2px;
}
.section-title p {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 0;
}

/* Book Grid */
.book-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.book-card {
  position: relative;
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-bg);
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
  padding: 0;
  text-align: left;
}
.book-card:hover {
  box-shadow: var(--shadow-md);
}
.book-card.active {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary);
}

.book-image {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.book-image .material-icons {
  font-size: 32px;
  color: rgba(255,255,255,0.5);
}
.book-check {
  position: absolute;
  top: 6px;
  right: 6px;
  color: #fff;
}
.book-check .material-icons {
  font-size: 20px;
  color: #fff;
}

.book-info {
  padding: 10px 14px;
}
.book-info strong {
  display: block;
  font-size: 13px;
  color: var(--color-text-primary);
}
.book-info span {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* Resume Card */
.resume-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border: 1px solid #93c5fd;
  border-radius: var(--radius-lg);
  padding: 16px 24px;
  margin-bottom: 20px;
}
.resume-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}
.resume-info .material-icons {
  font-size: 28px;
  color: var(--color-primary);
}
.resume-info strong {
  display: block;
  font-size: 14px;
  color: var(--color-text-primary);
}
.resume-info p {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin: 2px 0 0;
}
.resume-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
.btn-resume {
  padding: 8px 18px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}
.btn-resume:hover { opacity: 0.9; }
.btn-reset {
  padding: 8px 18px;
  background: #fff;
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: 13px;
  cursor: pointer;
}
.btn-reset:hover { background: var(--color-bg); }

/* Goal Config */
.goal-config {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.goal-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.goal-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-secondary);
}
.pill-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.pill {
  padding: 8px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.pill:hover { border-color: var(--color-primary); color: var(--color-primary); }
.pill.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

.goal-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: var(--color-primary-light);
  border-radius: var(--radius-md);
  margin-top: 4px;
}
.goal-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.goal-stat span {
  font-size: 11px;
  color: var(--color-text-muted);
  text-transform: uppercase;
}
.goal-stat strong {
  font-size: 22px;
  color: var(--color-text-primary);
}
.goal-stat.total strong {
  color: var(--color-primary);
}
.goal-plus, .goal-equals {
  font-size: 18px;
  color: var(--color-text-muted);
  font-weight: 300;
}

/* Preferences */
.prefs-grid {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.pref-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-divider);
}
.pref-item:last-child { border-bottom: none; }
.pref-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pref-info .material-icons {
  font-size: 22px;
  color: var(--color-text-muted);
}
.pref-info strong {
  display: block;
  font-size: 14px;
  color: var(--color-text-primary);
}
.pref-info p {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

.toggle-btn-group {
  display: flex;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
}
.toggle-btn {
  padding: 6px 16px;
  border: none;
  background: var(--color-surface);
  color: var(--color-text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}
.toggle-btn.active {
  background: var(--color-primary);
  color: #fff;
}
.toggle-btn:not(.active):hover {
  background: var(--color-divider);
}

/* Switch */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 22px;
  flex-shrink: 0;
}
.switch input { display: none; }
.slider {
  position: absolute;
  inset: 0;
  background: var(--color-border);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: 0.2s;
}
.slider::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  background: #fff;
  border-radius: 50%;
  transition: 0.2s;
}
.switch input:checked + .slider {
  background: var(--color-primary);
}
.switch input:checked + .slider::before {
  transform: translateX(18px);
}

/* Exam */
.exam-select select {
  width: 100%;
  max-width: 320px;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  font-size: 14px;
  color: var(--color-text-primary);
  cursor: pointer;
}

/* Learning Mode Selector */
.mode-select {
  display: flex;
  gap: 12px;
}
.mode-option {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
  text-align: left;
}
.mode-option:hover {
  border-color: var(--color-primary);
}
.mode-option.active {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary);
}
.mode-opt-icon {
  font-size: 32px;
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 10px;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}
.mode-opt-text {
  flex: 1;
}
.mode-opt-text strong {
  display: block;
  font-size: 15px;
  color: var(--color-text-primary);
  margin-bottom: 2px;
}
.mode-opt-text p {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}
.check-icon {
  font-size: 24px;
  color: var(--color-primary);
  flex-shrink: 0;
}

/* CTA */
.settings-cta {
  margin-top: 28px;
  text-align: center;
}
.cta-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 40px;
  background: var(--color-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: background 0.15s;
  box-shadow: var(--shadow-md);
}
.cta-btn:hover {
  background: var(--color-primary-dark);
}

/* Trash List */
.trash-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.trash-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  gap: 12px;
}
.trash-word-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.trash-word-info strong {
  font-size: 14px;
  color: var(--color-text-primary);
}
.trash-word-info span {
  font-size: 12px;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.btn-restore {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  background: var(--color-surface);
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-restore:hover { background: var(--color-primary-light); }
.empty-hint {
  font-size: 13px;
  color: var(--color-text-muted);
  text-align: center;
  padding: 20px 12px;
}

@media (max-width: 640px) {
  .settings-page { padding: 20px 12px; }
  .settings-section { padding: 18px; }
  .book-grid { grid-template-columns: repeat(2, 1fr); }
  .resume-card { flex-direction: column; align-items: stretch; }
  .resume-actions { justify-content: flex-end; }
  .goal-summary { flex-wrap: wrap; justify-content: center; }
}
</style>
