<script setup>
</script>

<template>
  <div class="home">
    <h1>欢迎来到 WordClub</h1>
    <p>一个简洁的单词学习工具。</p>
    <router-link to="/words" class="cta-button">查看单词列表</router-link>
  </div>
</template>

<style scoped>
.home {
  text-align: center;
  padding: 80px 0;
}

h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 12px;
}

p {
  font-size: 16px;
  color: #666;
  margin-bottom: 32px;
}

.cta-button {
  display: inline-block;
  padding: 10px 28px;
  background: #1a73e8;
  color: #fff;
  border-radius: 6px;
  text-decoration: none;
  font-size: 15px;
  transition: background 0.2s;
}

.cta-button:hover {
  background: #1557b0;
}
</style>
