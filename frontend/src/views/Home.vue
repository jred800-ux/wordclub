<script setup>
import { computed, onMounted } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()

const learnPath = computed(() => {
  return store.learningMode === 'spelling' ? '/learn/spelling' : '/learn/first-sight'
})

onMounted(async () => {
  await store.fetchBooks()
  store.fetchStats()
  if (!store.words.length && !store.selectedBookId && store.books.length) {
    store.selectBook(store.books[0].id)
  } else if (!store.words.length) {
    store.fetchWords()
  }
})
</script>

<template>
  <div class="home-dashboard">
    <!-- Hero -->
    <div class="hero-banner">
      <h1>欢迎回来</h1>
      <p>继续你的词汇精进之旅。</p>
      <router-link :to="learnPath" class="hero-cta">
        开始学习
        <span class="material-icons">arrow_forward</span>
      </router-link>
    </div>

    <!-- Quick Stats -->
    <div class="quick-stats">
      <div class="q-stat">
        <div class="q-number">{{ store.todayLearned }}</div>
        <div class="q-label">今日已学</div>
      </div>
      <div class="q-stat">
        <div class="q-number">{{ store.todayMinutes }}m</div>
        <div class="q-label">学习时长</div>
      </div>
      <div class="q-stat">
        <div class="q-number">{{ store.streakDays }}</div>
        <div class="q-label">连续天数</div>
      </div>
    </div>

    <!-- Mode Cards -->
    <div class="mode-section">
      <h2>选择学习模式</h2>
      <div class="mode-cards">
        <router-link to="/learn/first-sight" class="mode-card" :class="{ active: store.learningMode === 'first-sight' }">
          <span class="material-icons mode-icon">visibility</span>
          <h3>认读模式</h3>
          <p>看到单词，选择是否认识</p>
          <span v-if="store.learningMode === 'first-sight'" class="default-badge">默认</span>
        </router-link>
        <router-link to="/learn/spelling" class="mode-card" :class="{ active: store.learningMode === 'spelling' }">
          <span class="material-icons mode-icon">edit</span>
          <h3>拼写模式</h3>
          <p>根据释义拼写出单词</p>
          <span v-if="store.learningMode === 'spelling'" class="default-badge">默认</span>
        </router-link>
      </div>
    </div>

    <!-- Recent Words -->
    <div class="recent-section" v-if="store.words.length">
      <h2>词库概览</h2>
      <div class="word-chips">
        <span
          v-for="w in store.words.slice(0, 8)"
          :key="w.id"
          class="word-chip"
        >
          {{ w.spelling }}
        </span>
        <router-link to="/library" class="view-all">查看全部</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-dashboard {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px;
}

.hero-banner {
  text-align: center;
  padding: 48px 20px;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, #e0e7ff 100%);
  border-radius: var(--radius-xl);
  margin-bottom: 28px;
}
.hero-banner h1 {
  font-size: 30px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.hero-banner p {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin-bottom: 24px;
}
.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 12px 32px;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-full);
  text-decoration: none;
  transition: background 0.15s;
}
.hero-cta:hover { background: var(--color-primary-dark); }

.quick-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}
.q-stat {
  flex: 1;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  padding: 20px;
  text-align: center;
}
.q-number {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-primary);
}
.q-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}

.mode-section h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 14px;
}
.mode-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
}
.mode-card {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 28px 20px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  text-decoration: none;
  transition: box-shadow 0.2s, transform 0.2s;
  text-align: center;
}
.mode-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.mode-card.active {
  border: 2px solid var(--color-primary);
  box-shadow: 0 0 0 1px var(--color-primary);
}
.default-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 10px;
  background: var(--color-primary);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--radius-full);
}
.mode-icon {
  font-size: 36px;
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 12px;
  border-radius: var(--radius-xl);
}
.mode-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.mode-card p {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.recent-section h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 14px;
}
.word-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.word-chip {
  padding: 6px 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}
.view-all {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary);
}

@media (max-width: 640px) {
  .home-dashboard { padding: 20px 12px; }
  .quick-stats { flex-direction: column; gap: 8px; }
  .mode-cards { flex-direction: column; }
}
</style>
