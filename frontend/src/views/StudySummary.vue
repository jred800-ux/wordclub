<script setup>
import { ref, onMounted } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()
const checkinMsg = ref('')
const statsError = ref('')

onMounted(async () => {
  try {
    await store.fetchStats()
  } catch (e) {
    statsError.value = '加载统计失败'
  }
})

async function handleCheckin() {
  if (store.checkedInToday) return
  try {
    const result = await store.doCheckin()
    checkinMsg.value = `打卡成功! 连续 ${result.streak} 天`
    setTimeout(() => (checkinMsg.value = ''), 3000)
  } catch (e) {
    checkinMsg.value = e.response?.data?.message || '打卡失败'
  }
}
</script>

<template>
  <div class="study-summary">
    <div v-if="statsError" class="error-banner">{{ statsError }}</div>
    <div class="celebration-card">
      <span class="material-icons celebration-icon">celebration</span>
      <h2>恭喜!</h2>
      <p>今日目标已达成。</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <span class="material-icons stat-icon" style="color: #3b82f6;">menu_book</span>
        <div class="stat-number">{{ store.todayLearned }}</div>
        <div class="stat-label">今日学习单词</div>
      </div>
      <div class="stat-card">
        <span class="material-icons stat-icon" style="color: #f59e0b;">how_to_reg</span>
        <div class="stat-number">{{ store.totalCheckins }}</div>
        <div class="stat-label">累计打卡次数</div>
      </div>
      <div class="stat-card">
        <span class="material-icons stat-icon" style="color: #ef4444;">local_fire_department</span>
        <div class="stat-number">{{ store.streakDays }}</div>
        <div class="stat-label">连续打卡天数</div>
      </div>
    </div>

    <button
      class="share-btn"
      @click="handleCheckin"
      :disabled="store.checkedInToday"
      :class="{ shared: checkinMsg }"
    >
      <span class="material-icons">{{ store.checkedInToday ? 'check_circle' : 'how_to_reg' }}</span>
      {{ store.checkedInToday ? '今日已打卡' : (checkinMsg || '打卡记录') }}
    </button>

    <div class="quote-block">
      <blockquote>
        "语言的边界，即是世界的边界。"
        <footer>— Ludwig Wittgenstein</footer>
      </blockquote>
    </div>
  </div>
</template>

<style scoped>
.study-summary {
  max-width: 680px;
  margin: 0 auto;
  padding: 32px 20px;
}
.error-banner {
  text-align: center;
  padding: 10px 16px;
  background: var(--color-danger-light);
  color: #991b1b;
  border-radius: var(--radius-md);
  margin-bottom: 16px;
  font-size: 14px;
}

.celebration-card {
  text-align: center;
  padding: 32px 20px;
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-radius: var(--radius-xl);
  margin-bottom: 24px;
}
.celebration-icon {
  font-size: 48px;
  color: #f59e0b;
  margin-bottom: 8px;
}
.celebration-card h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.celebration-card p {
  font-size: 15px;
  color: var(--color-text-secondary);
}

.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}
.stat-card {
  flex: 1;
  text-align: center;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 20px 12px;
}
.stat-icon {
  font-size: 28px;
  margin-bottom: 6px;
  display: block;
}
.stat-number {
  font-size: 26px;
  font-weight: 800;
  color: var(--color-text-primary);
  margin-bottom: 2px;
}
.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.share-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px 24px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: var(--radius-full);
  transition: opacity 0.2s, transform 0.15s;
  margin-bottom: 32px;
}
.share-btn:hover {
  transform: scale(1.02);
}
.share-btn.shared {
  background: var(--color-success);
}
.share-btn:disabled {
  opacity: 0.6;
  cursor: default;
  background: var(--color-success);
}

.quote-block {
  text-align: center;
  padding: 20px 0;
}
blockquote {
  font-size: 14px;
  font-style: italic;
  color: var(--color-text-secondary);
  border: none;
  margin: 0;
  padding: 0;
}
blockquote footer {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 8px;
  font-style: normal;
}

@media (max-width: 640px) {
  .study-summary { padding: 16px 12px; }
  .stats-row { flex-direction: column; }
  .heatmap-cell { min-width: 8px; }
}
</style>
