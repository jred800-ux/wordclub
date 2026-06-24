import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// Global error handler — catches unhandled Vue errors in production
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue Error]', err, 'in', info, 'component:', instance?.$?.type?.name || instance?.$options?.name)
}

// Global warning handler (suppress in production if needed)
app.config.warnHandler = (msg, instance, trace) => {
  console.warn('[Vue Warn]', msg, trace, 'component:', instance?.$?.type?.name || instance?.$options?.name)
}

// Initialize auth store before mounting
import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
authStore.init().finally(() => {
  app.mount('#app')
})
