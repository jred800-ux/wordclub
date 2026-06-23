<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['close'])

const router = useRouter()
const query = ref('')
const results = ref([])
const loading = ref(false)
const searched = ref(false)
let debounceTimer = null

watch(() => props.visible, (val) => {
  if (val) {
    query.value = ''
    results.value = []
    searched.value = false
  }
})

function doSearch() {
  const kw = query.value.trim()
  if (!kw) {
    results.value = []
    searched.value = false
    return
  }
  loading.value = true
  searched.value = true
  api.get('/vocabulary/search', { params: { q: kw, page: 0, size: 10 } })
    .then(data => {
      const pageData = data.data || data
      results.value = pageData.content || []
    })
    .catch(() => {
      results.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function onInput() {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(doSearch, 300)
}

function goWord(word) {
  emit('close')
  router.push(`/word/${word.id}`)
}

function onKeydown(e) {
  if (e.key === 'Escape') {
    emit('close')
  }
}

function onBackdrop(e) {
  if (e.target === e.currentTarget) {
    emit('close')
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="search-backdrop" @click="onBackdrop" @keydown="onKeydown">
      <div class="search-panel">
        <div class="search-input-wrap">
          <span class="material-icons search-icon">search</span>
          <input
            ref="inputRef"
            v-model="query"
            type="text"
            class="search-input"
            placeholder="搜索英文单词或中文释义..."
            autofocus
            @input="onInput"
            @keydown="onKeydown"
          />
          <button v-if="query" class="clear-btn" @click="query = ''; results = []; searched = false">
            <span class="material-icons">close</span>
          </button>
        </div>

        <div class="search-results">
          <!-- Loading -->
          <div v-if="loading" class="search-state">
            <span class="material-icons spin">sync</span>
            <p>搜索中...</p>
          </div>

          <!-- Results -->
          <template v-else-if="results.length">
            <button
              v-for="word in results"
              :key="word.id"
              class="search-result-item"
              @click="goWord(word)"
            >
              <div class="result-main">
                <span class="result-spelling">{{ word.spelling }}</span>
                <span class="result-phonetic">{{ word.ukPhonetic || '' }}</span>
              </div>
              <span class="result-meaning">{{ (word.paraphrase || '').slice(0, 60) }}</span>
              <span class="material-icons result-arrow">chevron_right</span>
            </button>
          </template>

          <!-- No results -->
          <div v-else-if="searched" class="search-state">
            <span class="material-icons">search_off</span>
            <p>未找到匹配的单词</p>
          </div>

          <!-- Empty prompt -->
          <div v-else class="search-state">
            <span class="material-icons">text_fields</span>
            <p>输入关键词开始搜索</p>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.search-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  justify-content: center;
  padding-top: 12vh;
}

.search-panel {
  width: 560px;
  max-width: 90vw;
  max-height: 70vh;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  align-self: flex-start;
}

/* Input */
.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
}
.search-icon {
  font-size: 22px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  color: var(--color-text-primary);
  background: transparent;
  min-width: 0;
}
.search-input::placeholder {
  color: var(--color-text-muted);
}
.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: var(--color-border);
  color: var(--color-text-secondary);
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;
  padding: 0;
}
.clear-btn .material-icons {
  font-size: 16px;
}
.clear-btn:hover {
  background: var(--color-text-muted);
  color: #fff;
}

/* Results */
.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 20px;
  border: none;
  background: none;
  text-align: left;
  cursor: pointer;
  transition: background 0.1s;
}
.search-result-item:hover {
  background: var(--color-bg);
}

.result-main {
  display: flex;
  align-items: baseline;
  gap: 8px;
  flex-shrink: 0;
  min-width: 180px;
}
.result-spelling {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.result-phonetic {
  font-size: 12px;
  color: var(--color-text-muted);
  font-family: 'Segoe UI', monospace;
}
.result-meaning {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.result-arrow {
  font-size: 18px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

/* States */
.search-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 20px;
  color: var(--color-text-muted);
  gap: 8px;
}
.search-state .material-icons {
  font-size: 36px;
}
.search-state p {
  font-size: 14px;
}

.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
