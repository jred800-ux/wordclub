<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div id="app-container">
    <header class="app-header">
      <router-link to="/" class="logo">WordClub</router-link>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/words">单词列表</router-link>
      </nav>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #1a73e8;
  text-decoration: none;
}

nav {
  display: flex;
  gap: 20px;
}

nav a {
  color: #555;
  text-decoration: none;
  font-size: 15px;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
}

nav a:hover,
nav a.router-link-exact-active {
  color: #1a73e8;
  border-bottom-color: #1a73e8;
}

main {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
}
</style>
