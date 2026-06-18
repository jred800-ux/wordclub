import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

export const useWordStore = defineStore('word', () => {
  const words = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchWords() {
    loading.value = true
    error.value = null
    try {
      const data = await api.get('/words')
      words.value = data
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function addWord(word) {
    const created = await api.post('/words', word)
    words.value.push(created)
    return created
  }

  return { words, loading, error, fetchWords, addWord }
})
