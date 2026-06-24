<script setup>
import { ref } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()
const checkinMsg = ref('')

defineProps({
  show: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])

async function handleCheckin() {
  try {
    const result = await store.doCheckin()
    checkinMsg.value = `已连续打卡 ${result.streak} 天!`
  } catch (e) {
    checkinMsg.value = e.response?.data?.message || '打卡失败'
  }
}
</script>

<template>
  <div v-if="show" class="completion-banner">
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
      <button class="text-btn" @click="$emit('close')">继续学习</button>
    </div>
  </div>
</template>

<style scoped>
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
  cursor: pointer;
  transition: background 0.15s;
}
.text-btn:hover { background: var(--color-warning-light); }
</style>
