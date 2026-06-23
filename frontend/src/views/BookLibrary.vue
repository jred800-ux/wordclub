<script setup>
import { ref, computed } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()

const courses = [
  { id: 'cet4', name: 'CET-4', vocab: 4500, status: '未开始', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', icon: 'school' },
  { id: 'cet6', name: 'CET-6', vocab: 6000, status: '学习中', gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)', icon: 'menu_book' },
  { id: 'postgrad', name: '考研英语', vocab: 5500, status: '未开始', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', icon: 'auto_stories' },
  { id: 'ielts', name: '雅思 IELTS', vocab: 8000, status: '未开始', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', icon: 'flight_takeoff' },
]

const examDates = [
  { label: 'CET-4 (6月)', value: '2026-06-15' },
  { label: 'CET-6 (12月)', value: '2026-12-20' },
  { label: '考研 (12月)', value: '2026-12-26' },
]

const estimatedDays = computed(() => {
  if (!store.dailyGoal) return '--'
  const selected = courses.find(c => c.status === '学习中')
  const total = selected ? selected.vocab : 4500
  return Math.ceil(total / store.dailyGoal)
})

const intensity = computed(() => {
  const days = Number(estimatedDays.value)
  if (isNaN(days)) return '无'
  if (days <= 30) return '高强度'
  if (days <= 60) return '适中'
  return '轻松'
})

function selectCourse(course) {
  courses.forEach(c => { c.status = c.id === course.id ? '学习中' : (c.status === '学习中' ? '未开始' : c.status) })
}
</script>

<template>
  <div class="book-library">
    <div class="library-grid">
      <!-- Main content -->
      <div class="library-main">
        <div class="section-header">
          <h1>词库选择</h1>
          <p>选择一套课程，开启你的语言沉浸式学习之旅。</p>
        </div>

        <div class="course-grid">
          <button
            v-for="course in courses"
            :key="course.id"
            class="course-card"
            :class="{ active: course.status === '学习中' }"
            @click="selectCourse(course)"
          >
            <div class="course-image" :style="{ background: course.gradient }">
              <span class="material-icons">{{ course.icon }}</span>
              <span class="course-status">{{ course.status }}</span>
            </div>
            <div class="course-info">
              <span class="material-icons">menu_book</span>
              <div>
                <strong>{{ course.name }}</strong>
                <span class="vocab-count">{{ course.vocab.toLocaleString() }} 词</span>
              </div>
            </div>
          </button>
        </div>
      </div>

      <!-- Sidebar Plan -->
      <aside class="plan-sidebar">
        <div class="plan-card">
          <h3>个性化你的学习计划</h3>

          <div class="form-group">
            <label>每日目标词数</label>
            <select v-model.number="store.dailyGoal">
              <option :value="10">10 词/天</option>
              <option :value="20">20 词/天</option>
              <option :value="50">50 词/天</option>
              <option :value="100">100 词/天</option>
            </select>
          </div>

          <div class="form-group">
            <label>
              <span class="material-icons">event_available</span>
              考试日期
            </label>
            <select v-model="store.examDate">
              <option value="">选择考试</option>
              <option v-for="d in examDates" :key="d.value" :value="d.value">{{ d.label }}</option>
            </select>
          </div>

          <div class="plan-summary">
            <div class="summary-row">
              <span>预计完成天数</span>
              <strong>{{ estimatedDays }} 天</strong>
            </div>
          </div>

          <div class="plan-divider"></div>

          <div class="plan-overview">
            <div class="overview-title">计划概览</div>
            <div class="overview-item">
              <span class="material-icons">menu_book</span>
              <span>已选书籍</span>
              <strong class="ov-right">{{ courses.find(c => c.status === '学习中')?.name || '—' }}</strong>
            </div>
            <div class="overview-item">
              <span class="material-icons">format_list_numbered</span>
              <span>总词数</span>
              <strong class="ov-right">{{ courses.find(c => c.status === '学习中')?.vocab?.toLocaleString() || '0' }}</strong>
            </div>
            <div class="overview-item">
              <span class="material-icons">speed</span>
              <span>强度</span>
              <strong class="ov-right">{{ intensity }}</strong>
            </div>
          </div>

          <router-link to="/learn/first-sight" class="start-btn">
            开始学习
            <span class="material-icons">arrow_forward</span>
          </router-link>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.book-library {
  padding: 32px 24px;
}

.library-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Section header */
.section-header {
  margin-bottom: 24px;
}
.section-header h1 {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.section-header p {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* Course Grid */
.course-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.course-card {
  border: none;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  text-align: left;
  padding: 0;
}
.course-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.course-card.active {
  box-shadow: 0 0 0 2px var(--color-primary), var(--shadow-md);
}

.course-image {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.course-image .material-icons {
  font-size: 48px;
  color: rgba(255,255,255,0.5);
}
.course-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  background: rgba(0,0,0,0.45);
  color: #fff;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 500;
}

.course-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
}
.course-info strong {
  display: block;
  font-size: 15px;
  color: var(--color-text-primary);
}
.vocab-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* Plan Sidebar */
.plan-sidebar {
  position: sticky;
  top: 72px;
  align-self: start;
}
.plan-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 24px;
}
.plan-card h3 {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}
.form-group label .material-icons {
  font-size: 16px;
}
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  font-size: 14px;
  color: var(--color-text-primary);
  cursor: pointer;
}

.plan-summary {
  background: var(--color-primary-light);
  border-radius: var(--radius-md);
  padding: 14px;
  margin-bottom: 16px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.summary-row strong {
  font-size: 20px;
  color: var(--color-primary);
}

.plan-divider {
  height: 1px;
  background: var(--color-border);
  margin: 16px 0;
}

.overview-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}
.overview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}
.overview-item .material-icons {
  font-size: 17px;
  color: var(--color-text-muted);
}
.ov-right {
  margin-left: auto;
  color: var(--color-text-primary);
}

.start-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px 24px;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-full);
  text-decoration: none;
  transition: background 0.15s;
  margin-top: 20px;
}
.start-btn:hover { background: var(--color-primary-dark); }

@media (max-width: 900px) {
  .library-grid {
    grid-template-columns: 1fr;
  }
  .plan-sidebar {
    position: static;
  }
}
@media (max-width: 640px) {
  .book-library { padding: 20px 12px; }
  .course-grid { grid-template-columns: 1fr; }
}
</style>
