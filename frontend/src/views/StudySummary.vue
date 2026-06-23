<script setup>
import { ref } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()
const shared = ref(false)

// Heatmap data: 7 rows (days of week) x 15 weeks
const weeks = 15
const heatmapData = ref(generateHeatmap())

function generateHeatmap() {
  const rows = []
  for (let d = 0; d < 7; d++) {
    const week = []
    for (let w = 0; w < weeks; w++) {
      const level = Math.random() < 0.4 ? 0 : Math.floor(Math.random() * 4) + 1
      week.push(level)
    }
    rows.push(week)
  }
  return rows
}

function getHeatColor(level) {
  const colors = ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127']
  return colors[level] || colors[0]
}

function handleShare() {
  shared.value = true
  setTimeout(() => (shared.value = false), 2500)
}
</script>

<template>
  <div class="study-summary">
    <!-- Celebration Card -->
    <div class="celebration-card">
      <span class="material-icons celebration-icon">celebration</span>
      <h2>恭喜！</h2>
      <p>今日目标已达成。</p>
    </div>

    <!-- Stats Row -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="material-icons stat-icon" style="color: #3b82f6;">menu_book</span>
        <div class="stat-number">{{ store.todayLearned }}</div>
        <div class="stat-label">今日学习单词</div>
      </div>
      <div class="stat-card">
        <span class="material-icons stat-icon" style="color: #f59e0b;">timer</span>
        <div class="stat-number">{{ store.todayMinutes }} 分钟</div>
        <div class="stat-label">今日学习时长</div>
      </div>
      <div class="stat-card">
        <span class="material-icons stat-icon" style="color: #ef4444;">local_fire_department</span>
        <div class="stat-number">{{ store.streakDays }}</div>
        <div class="stat-label">连续打卡天数</div>
      </div>
    </div>

    <!-- Heatmap -->
    <div class="heatmap-card">
      <h3 class="card-title">学习热力图</h3>
      <div class="heatmap-container">
        <div class="heatmap-grid">
          <template v-for="(row, dayIdx) in heatmapData" :key="dayIdx">
            <div
              v-for="(level, weekIdx) in row"
              :key="`${dayIdx}-${weekIdx}`"
              class="heatmap-cell"
              :style="{ background: getHeatColor(level) }"
              :title="`${level} 个单词`"
            ></div>
          </template>
        </div>
        <div class="heatmap-legend">
          <span>少</span>
          <div class="legend-swatch" style="background: #ebedf0;"></div>
          <div class="legend-swatch" style="background: #c6e48b;"></div>
          <div class="legend-swatch" style="background: #7bc96f;"></div>
          <div class="legend-swatch" style="background: #239a3b;"></div>
          <div class="legend-swatch" style="background: #196127;"></div>
          <span>多</span>
        </div>
      </div>
    </div>

    <!-- Share -->
    <button class="share-btn" @click="handleShare" :class="{ shared }">
      <span class="material-icons">share</span>
      {{ shared ? '已分享！' : '打卡并分享' }}
    </button>

    <!-- Quote -->
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

/* Celebration Card */
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

/* Stats Row */
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

/* Heatmap */
.heatmap-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 24px;
  margin-bottom: 24px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}
.heatmap-container {
  overflow-x: auto;
}
.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(15, 1fr);
  gap: 2px;
  margin-bottom: 10px;
}
.heatmap-cell {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 2px;
  min-width: 10px;
}
.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 3px;
  font-size: 11px;
  color: var(--color-text-muted);
}
.legend-swatch {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

/* Share Button */
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

/* Quote */
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
