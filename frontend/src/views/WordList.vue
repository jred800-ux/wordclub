<script setup>
import { onMounted } from 'vue'
import { useWordStore } from '../stores/word'

const store = useWordStore()

onMounted(() => {
  store.fetchWords()
})
</script>

<template>
  <div class="word-list">
    <h2>单词列表</h2>

    <div v-if="store.loading" class="state">加载中...</div>
    <div v-else-if="store.error" class="state error">出错了：{{ store.error }}</div>
    <div v-else-if="!store.words.length" class="state">暂无单词，快去添加吧。</div>

    <ul v-else>
      <li v-for="word in store.words" :key="word.id">
        <span class="spelling">{{ word.spelling }}</span>
        <span class="meaning">{{ word.meaning }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.word-list {
  padding: 20px 0;
}

h2 {
  margin-bottom: 20px;
  font-size: 22px;
}

.state {
  padding: 40px 0;
  text-align: center;
  color: #999;
}

.state.error {
  color: #d93025;
}

ul {
  list-style: none;
  padding: 0;
}

li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

li:hover {
  background: #f8f9fa;
}

.spelling {
  font-weight: 600;
  font-size: 16px;
}

.meaning {
  color: #666;
}
</style>
