import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
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
  const newWordCount = ref(50)
  const reviewRatio = ref(1) // 1=1:1, 2=1:2, ..., 5=1:5
  const examDate = ref('')
  const learningMode = ref('first-sight') // 'first-sight' | 'spelling'

  // Stats (from API)
  const todayLearned = ref(0)
  const streakDays = ref(0)
  const masteredCount = ref(0)
  const todayNewCount = ref(0)
  const todayReviewCount = ref(0)
  const pendingReviewCount = ref(0)
  const checkedInToday = ref(false)
  const totalCheckins = ref(0)

  // Blacklist (trash)
  const blacklistedIds = ref(new Set())

  // --- Computed ---

  // Effective daily goal: new words + available review words (capped by user's review ratio setting)
  const effectiveReviewTarget = computed(() => Math.min(newWordCount.value * reviewRatio.value, pendingReviewCount.value))
  const dailyGoal = computed(() => newWordCount.value + effectiveReviewTarget.value)
  const reviewWordCount = computed(() => newWordCount.value * reviewRatio.value)
  const dailyGoalPercent = computed(() => {
    if (!dailyGoal.value) return 0
    // New words count toward the new-word portion, review words count toward the review portion
    const newProgress = Math.min(todayNewCount.value, newWordCount.value)
    const reviewProgress = Math.min(todayReviewCount.value, effectiveReviewTarget.value)
    return Math.min(100, Math.round(((newProgress + reviewProgress) / dailyGoal.value) * 100))
  })
  const dailyGoalReached = computed(() => {
    if (!dailyGoal.value) return false
    return todayNewCount.value >= newWordCount.value && todayReviewCount.value >= effectiveReviewTarget.value
  })

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

  const bookProgress = ref(null) // { totalWords, studiedCount, masteredCount, completionPercent, resumePage, resumeIndex, lastWordId }

  // Settings persistence
  const _settingsLoaded = ref(false)
  let _settingsResolve = null
  const _settingsPromise = new Promise(resolve => { _settingsResolve = resolve })
  function settingsReady() { return _settingsPromise }
  let _saveTimer = null

  async function fetchSettings() {
    try {
      const data = await api.get('/learning/settings')
      const s = data.data || data
      if (s) {
        newWordCount.value = s.newWordCount ?? 50
        reviewRatio.value = s.reviewRatio ?? 1
        cardOrder.value = s.cardOrder ?? 'random'
        largeFont.value = s.largeFont ?? false
        darkMode.value = s.darkMode ?? false
        learningMode.value = s.learningMode ?? 'first-sight'
        examDate.value = s.examDate ?? ''
        if (s.selectedBookId) {
          selectedBookId.value = s.selectedBookId
        }
      }
    } catch (e) {
      console.error('[WordStore] fetchSettings:', e.message)
    } finally {
      _settingsLoaded.value = true
      if (_settingsResolve) { _settingsResolve(); _settingsResolve = null }
      // Flush any changes that happened during loading (e.g. selectBook)
      saveSettings()
    }
  }

  async function saveSettings() {
    if (!_settingsLoaded.value) return
    if (_saveTimer) clearTimeout(_saveTimer)
    _saveTimer = setTimeout(async () => {
      try {
        await api.put('/learning/settings', {
          newWordCount: newWordCount.value,
          reviewRatio: reviewRatio.value,
          cardOrder: cardOrder.value,
          largeFont: largeFont.value,
          darkMode: darkMode.value,
          learningMode: learningMode.value,
          examDate: examDate.value,
          selectedBookId: selectedBookId.value,
        })
      } catch (e) {
        console.error('[WordStore] saveSettings:', e.message)
      }
    }, 500)
  }

  // Auto-save when any setting changes
  watch(
    [newWordCount, reviewRatio, cardOrder, largeFont, darkMode, learningMode, examDate, selectedBookId],
    () => saveSettings()
  )

  async function selectBook(bookId, resume = true) {
    selectedBookId.value = bookId
    currentIndex.value = 0
    currentPage.value = 0
    words.value = []
    bookProgress.value = null
    fetchBlacklistedIds()

    if (resume) {
      try {
        const data = await api.get(`/learning/progress/book/${bookId}`)
        const prog = data.data || data
        if (prog) {
          bookProgress.value = prog
          if (prog.resumePage != null) {
            await fetchWords(prog.resumePage)
            const idx = (prog.resumeIndex || 0) + 1
            currentIndex.value = idx < words.value.length ? idx : 0
            return
          }
        }
      } catch (e) {
        console.error('[WordStore] fetchBookProgress failed, starting fresh:', e)
      }
    }

    await fetchWords(0)
  }

  // Seeded PRNG (mulberry32) — deterministic shuffle across refreshes
  function _seededRandom(seed) {
    let s = Math.abs(seed) | 0 || 1
    return function () {
      s = (s * 1664525 + 1013904223) | 0
      return (s >>> 0) / 4294967296
    }
  }

  // Fisher-Yates shuffle with custom RNG (in-place)
  function _shuffle(arr, rng) {
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor((rng || Math.random)() * (i + 1))
      ;[arr[i], arr[j]] = [arr[j], arr[i]]
    }
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

      // Apply card order on client side — seeded shuffle for stability
      if (cardOrder.value === 'random') {
        _shuffle(words.value, _seededRandom(selectedBookId.value || 1))
      }
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
      todayNewCount.value = stats.todayNewCount || 0
      todayReviewCount.value = stats.todayReviewCount || 0
      pendingReviewCount.value = stats.pendingReviewCount || 0
      masteredCount.value = stats.mastered || 0
      streakDays.value = stats.streakDays || 0
      checkedInToday.value = !!stats.checkedInToday
      totalCheckins.value = stats.totalCheckins || 0
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

  // Helper: immutable Set operations that Vue 3 reactivity can track
  // (Vue 3 ref() does NOT track Set.add()/delete() — must replace the entire ref)
  function _setAdd(setRef, value) {
    setRef.value = new Set([...setRef.value, value])
  }
  function _setDelete(setRef, value) {
    const next = new Set(setRef.value)
    next.delete(value)
    setRef.value = next
  }

  function markMastered(word) {
    _setAdd(masteredIds, word.id)
    _setDelete(fuzzyIds, word.id)
    _setDelete(unknownIds, word.id)
    recordReview(word.id, 5)
    fetchStats()
    nextWord()
  }

  function markFuzzy(word) {
    _setAdd(fuzzyIds, word.id)
    _setDelete(masteredIds, word.id)
    _setDelete(unknownIds, word.id)
    recordReview(word.id, 2)
    fetchStats()
    nextWord()
  }

  function markUnknown(word) {
    _setAdd(unknownIds, word.id)
    _setDelete(masteredIds, word.id)
    _setDelete(fuzzyIds, word.id)
    recordReview(word.id, 0)
    fetchStats()
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

  // --- Check-in ---

  async function doCheckin() {
    const data = await api.post('/learning/checkin')
    const result = data.data || data
    streakDays.value = result.streak || 0
    checkedInToday.value = true
    totalCheckins.value = result.totalCheckins || 0
    return result
  }

  // --- Blacklist (trash) ---

  async function fetchBlacklistedIds() {
    try {
      const data = await api.get('/learning/blacklist/ids')
      const ids = data.data || data || []
      blacklistedIds.value = new Set(ids)
    } catch (e) {
      console.error('[WordStore] fetchBlacklistedIds:', e.message)
    }
  }

  async function addToBlacklist(wordId) {
    await api.post(`/learning/blacklist/${wordId}`)
    _setAdd(blacklistedIds, wordId)
  }

  async function removeFromBlacklist(wordId) {
    await api.delete(`/learning/blacklist/${wordId}`)
    _setDelete(blacklistedIds, wordId)
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

  function setLearningMode(mode) {
    learningMode.value = mode
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
    cardOrder, largeFont, darkMode, learningMode, dailyGoal, reviewWordCount,
    newWordCount, reviewRatio, examDate, bookProgress,
    todayLearned, streakDays, masteredCount,
    todayNewCount, todayReviewCount, pendingReviewCount,
    dailyGoalPercent, dailyGoalReached, effectiveReviewTarget,
    checkedInToday, totalCheckins, blacklistedIds,
    currentWord, totalWords, progress, progressPercent,
    fetchBooks, selectBook, fetchWords, fetchWordDetail, fetchStats,
    recordReview, toggleFavorite,
    markMastered, markFuzzy, markUnknown,
    skipWord, nextWord, setOrder,
    toggleLargeFont, toggleDarkMode, setLearningMode,
    fetchSettings, saveSettings, settingsReady,
    doCheckin, fetchBlacklistedIds, addToBlacklist, removeFromBlacklist,
  }
})
