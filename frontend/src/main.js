import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// Initialize auth store before mounting
import { useAuthStore } from './stores/auth'
const authStore = useAuthStore()
authStore.init().finally(() => {
  app.mount('#app')
})
