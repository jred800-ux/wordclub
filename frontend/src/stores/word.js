import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useWordStore = defineStore('word', () => {
  const words = ref([])
  const currentIndex = ref(0)
  const loading = ref(false)
  const error = ref(null)

  // Learning progress
  const masteredIds = ref(new Set())
  const fuzzyIds = ref(new Set())
  const unknownIds = ref(new Set())

  // Settings
  const cardOrder = ref('random')  // 'alphabetical' | 'random'
  const largeFont = ref(false)
  const darkMode = ref(false)
  const dailyGoal = ref(20)
  const examDate = ref('')

  // Stats
  const todayLearned = ref(50)
  const todayMinutes = ref(45)
  const streakDays = ref(15)

  // ----- Computed -----
  const currentWord = computed(() => words.value[currentIndex.value] || null)
  const totalWords = computed(() => words.value.length)
  const progress = computed(() => {
    if (!totalWords.value) return 0
    return masteredIds.value.size + fuzzyIds.value.size + unknownIds.value.size
  })
  const progressPercent = computed(() => {
    if (!totalWords.value) return 0
    return Math.round((progress.value / totalWords.value) * 100)
  })

  // ----- Actions -----
  async function fetchWords() {
    loading.value = true
    error.value = null
    try {
      const data = await api.get('/words')
      words.value = Array.isArray(data) ? data : (data.content || data.data || [])
    } catch (e) {
      error.value = e.message
      // Fallback demo data
      words.value = [
        { id: 1, spelling: 'persistent', meaning: '坚持不懈的；执着的', phonetic: "/pərˈsɪstənt/", partOfSpeech: 'adjective', root: 'sist', prefix: 'per-', suffix: '-ent' },
        { id: 2, spelling: 'elaborate', meaning: '精心制作的；详细阐述', phonetic: "/ɪˈlæbərət/", partOfSpeech: 'adjective, verb', root: 'labor', prefix: 'e-', suffix: '-ate' },
        { id: 3, spelling: 'substantial', meaning: '大量的；实质的', phonetic: "/səbˈstænʃəl/", partOfSpeech: 'adjective', root: 'stant', prefix: 'sub-', suffix: '-ial' },
        { id: 4, spelling: 'consequence', meaning: '结果；重要性', phonetic: "/ˈkɑːnsɪkwens/", partOfSpeech: 'noun', root: 'sequ', prefix: 'con-', suffix: '-ence' },
        { id: 5, spelling: 'predominant', meaning: '主要的；占优势的', phonetic: "/prɪˈdɑːmɪnənt/", partOfSpeech: 'adjective', root: 'domin', prefix: 'pre-', suffix: '-ant' },
      ]
    } finally {
      loading.value = false
    }
  }

  function markMastered(word) {
    masteredIds.value.add(word.id)
    fuzzyIds.value.delete(word.id)
    unknownIds.value.delete(word.id)
    nextWord()
  }

  function markFuzzy(word) {
    fuzzyIds.value.add(word.id)
    masteredIds.value.delete(word.id)
    unknownIds.value.delete(word.id)
    nextWord()
  }

  function markUnknown(word) {
    unknownIds.value.add(word.id)
    masteredIds.value.delete(word.id)
    fuzzyIds.value.delete(word.id)
    nextWord()
  }

  function nextWord() {
    if (currentIndex.value < words.value.length - 1) {
      currentIndex.value++
    } else {
      currentIndex.value = 0
    }
  }

  function skipWord() {
    nextWord()
  }

  function setOrder(order) {
    cardOrder.value = order
  }

  function toggleLargeFont() {
    largeFont.value = !largeFont.value
  }

  function toggleDarkMode() {
    darkMode.value = !darkMode.value
  }

  return {
    words, currentIndex, loading, error,
    masteredIds, fuzzyIds, unknownIds,
    cardOrder, largeFont, darkMode, dailyGoal, examDate,
    todayLearned, todayMinutes, streakDays,
    currentWord, totalWords, progress, progressPercent,
    fetchWords, markMastered, markFuzzy, markUnknown,
    skipWord, nextWord, setOrder,
    toggleLargeFont, toggleDarkMode,
  }
})
