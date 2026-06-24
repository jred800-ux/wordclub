<script setup>
import { computed } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()

const todayTotal = computed(() => store.todayNewCount + store.todayReviewCount)

const newFillWidth = computed(() => {
  return (Math.min(store.todayNewCount, store.newWordCount) / Math.max(store.dailyGoal, 1) * 100) + '%'
})

const reviewFillWidth = computed(() => {
  return (store.todayReviewCount / Math.max(store.dailyGoal, 1) * 100) + '%'
})
</script>

<template>
  <div class="daily-goal-bar">
    <div class="daily-goal-header">
      <span>今日目标</span>
      <strong>{{ todayTotal }} / {{ store.dailyGoal }}</strong>
    </div>
    <div class="daily-goal-track">
      <div class="dg-fill-new" :style="{ width: newFillWidth }"></div>
      <div class="dg-fill-review" :style="{ width: reviewFillWidth }"></div>
    </div>
    <div class="daily-goal-legend">
      <span class="legend-new"><span class="dot"></span>新词 {{ store.todayNewCount }} / {{ store.newWordCount }}</span>
      <span class="legend-review"><span class="dot"></span>复习 {{ store.todayReviewCount }} / {{ store.effectiveReviewTarget }}</span>
    </div>
  </div>
</template>

<style scoped>
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
.daily-goal-header strong { color: var(--color-text-primary); }
.daily-goal-track {
  display: flex;
  height: 6px;
  background: var(--color-border);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: 8px;
}
.dg-fill-new { height: 100%; background: var(--color-primary); transition: width 0.4s ease; }
.dg-fill-review { height: 100%; background: var(--color-warning); transition: width 0.4s ease; }
.daily-goal-legend { display: flex; gap: 16px; font-size: 12px; color: var(--color-text-muted); }
.legend-new .dot, .legend-review .dot {
  display: inline-block; width: 8px; height: 8px;
  border-radius: 50%; margin-right: 4px; vertical-align: middle;
}
.legend-new .dot { background: var(--color-primary); }
.legend-review .dot { background: var(--color-warning); }
</style>
