import { ref } from 'vue'

/**
 * Shared speech synthesis composable.
 * Handles the browser audio-unlock gate and provides a uniform play() function.
 */
export function useSpeech() {
  const unlocked = ref(false)

  function unlock() {
    unlocked.value = true
  }

  function play(text, options = {}) {
    if (!text || !window.speechSynthesis) return
    const u = new SpeechSynthesisUtterance(text)
    u.lang = options.lang || 'en-US'
    u.rate = options.rate ?? 0.8
    u.pitch = options.pitch ?? 1
    speechSynthesis.speak(u)
  }

  /** Play a word's spelling (use after user interaction to satisfy browser autoplay policy) */
  function playWord(word) {
    if (!word || !unlocked.value) return
    play(word.spelling || word)
  }

  return { unlocked, unlock, play, playWord }
}
