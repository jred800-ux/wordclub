import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
  },
  {
    path: '/words',
    name: 'WordList',
    component: () => import('../views/WordList.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
