import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useWordStore = defineStore('word', () => {
  const words = ref([])
  const currentIndex = ref(0)
  const loading = ref(false)
  const error = ref(null)

  // Books
  const books = ref([])
  const selectedBookId = ref(null)

  // Pagination
  const currentPage = ref(0)
  const totalPages = ref(0)
  const totalElements = ref(0)

  // Learning progress (server-side)
  const masteredIds = ref(new Set())
  const fuzzyIds = ref(new Set())
  const unknownIds = ref(new Set())

  // Settings
  const cardOrder = ref('random')
  const largeFont = ref(false)
  const darkMode = ref(false)
  const dailyGoal = ref(20)
  const examDate = ref('')

  // Stats (from API)
  const todayLearned = ref(0)
  const todayMinutes = ref(0)
  const streakDays = ref(0)
  const masteredCount = ref(0)

  // --- Computed ---

  const currentWord = computed(() => {
    const w = words.value[currentIndex.value]
    if (!w) return null
    // Normalize fields for backward compatibility
    return {
      ...w,
      meaning: w.paraphrase || w.meaning,
      phonetic: w.ukPhonetic || w.phonetic || '',
      usPhonetic: w.usPhonetic || '',
      partOfSpeech: w.partOfSpeech || extractPos(w.paraphrase),
    }
  })

  const totalWords = computed(() => words.value.length)
  const progress = computed(() => masteredIds.value.size + fuzzyIds.value.size + unknownIds.value.size)
  const progressPercent = computed(() => {
    if (!totalWords.value) return 0
    return Math.round((progress.value / totalWords.value) * 100)
  })

  const selectedBook = computed(() => books.value.find(b => b.id === selectedBookId.value))

  // --- Actions ---

  async function fetchBooks() {
    try {
      const data = await api.get('/books')
      books.value = data.data || data || []
    } catch (e) {
      console.error('[WordStore] fetchBooks:', e.message)
    }
  }

  function selectBook(bookId) {
    selectedBookId.value = bookId
    currentIndex.value = 0
    currentPage.value = 0
    words.value = []
    fetchWords()
  }

  async function fetchWords(page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      let url = `/vocabulary?page=${page}&size=${size}`
      if (selectedBookId.value) url += `&bookId=${selectedBookId.value}`
      const data = await api.get(url)
      const pageData = data.data || data
      words.value = pageData.content || []
      currentPage.value = pageData.number ?? page
      totalPages.value = pageData.totalPages ?? 0
      totalElements.value = pageData.totalElements ?? words.value.length
    } catch (e) {
      error.value = e.message
      console.error('[WordStore] fetchWords:', e.message)
    } finally {
      loading.value = false
    }
  }

  async function fetchWordDetail(id) {
    try {
      const data = await api.get(`/vocabulary/${id}`)
      return data.data || data
    } catch (e) {
      console.error('[WordStore] fetchWordDetail:', e.message)
      return null
    }
  }

  async function fetchStats() {
    try {
      const data = await api.get('/learning/stats')
      const stats = data.data || {}
      todayLearned.value = stats.todayLearned || 0
      masteredCount.value = stats.mastered || 0
    } catch (e) {
      console.error('[WordStore] fetchStats:', e.message)
    }
  }

  async function recordReview(wordId, quality) {
    try {
      await api.post('/learning/review', {
        wordId,
        bookId: selectedBookId.value,
        quality,
      })
    } catch (e) {
      console.error('[WordStore] recordReview:', e.message)
    }
  }

  // --- Favorites ---

  async function toggleFavorite(wordId) {
    try {
      const check = await api.get(`/learning/favorites/${wordId}/check`)
      const isFav = check.data || check
      if (isFav) {
        await api.delete(`/learning/favorites/${wordId}`)
      } else {
        await api.post(`/learning/favorites/${wordId}`)
      }
    } catch (e) {
      console.error('[WordStore] toggleFavorite:', e.message)
    }
  }

  // --- Local Learning Actions ---

  function markMastered(word) {
    masteredIds.value.add(word.id)
    fuzzyIds.value.delete(word.id)
    unknownIds.value.delete(word.id)
    recordReview(word.id, 5)
    nextWord()
  }

  function markFuzzy(word) {
    fuzzyIds.value.add(word.id)
    masteredIds.value.delete(word.id)
    unknownIds.value.delete(word.id)
    recordReview(word.id, 2)
    nextWord()
  }

  function markUnknown(word) {
    unknownIds.value.add(word.id)
    masteredIds.value.delete(word.id)
    fuzzyIds.value.delete(word.id)
    recordReview(word.id, 0)
    nextWord()
  }

  function nextWord() {
    if (currentIndex.value < words.value.length - 1) {
      currentIndex.value++
    } else {
      if (currentPage.value + 1 < totalPages.value) {
        fetchWords(currentPage.value + 1).then(() => {
          currentIndex.value = 0
        })
      } else {
        currentIndex.value = 0
      }
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

  // --- Helpers ---

  function extractPos(paraphrase) {
    if (!paraphrase) return ''
    const match = paraphrase.match(/^([a-z]+)\.?\s/i)
    return match ? match[1] : ''
  }

  return {
    words, currentIndex, loading, error,
    books, selectedBookId, selectedBook,
    currentPage, totalPages, totalElements,
    masteredIds, fuzzyIds, unknownIds,
    cardOrder, largeFont, darkMode, dailyGoal, examDate,
    todayLearned, todayMinutes, streakDays, masteredCount,
    currentWord, totalWords, progress, progressPercent,
    fetchBooks, selectBook, fetchWords, fetchWordDetail, fetchStats,
    recordReview, toggleFavorite,
    markMastered, markFuzzy, markUnknown,
    skipWord, nextWord, setOrder,
    toggleLargeFont, toggleDarkMode,
  }
})
