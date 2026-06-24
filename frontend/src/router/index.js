import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/learn/first-sight',
    name: 'FirstSight',
    component: () => import('../views/FirstSight.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/learn/spelling',
    name: 'SpellingMode',
    component: () => import('../views/SpellingMode.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/word/:id',
    name: 'WordDetail',
    component: () => import('../views/WordDetail.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/book/:bookId',
    name: 'BookWords',
    component: () => import('../views/BookWords.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/library',
    name: 'BookLibrary',
    component: () => import('../views/BookLibrary.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('../views/Settings.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/summary',
    name: 'StudySummary',
    component: () => import('../views/StudySummary.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin',
    name: 'AdminPanel',
    component: () => import('../views/AdminPanel.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局路由守卫
router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')

  // 需要登录的页面 → 无 token 跳登录
  if (to.meta.requiresAuth && !token) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin) {
    const role = localStorage.getItem('userRole')
    if (role !== 'ADMIN') {
      return { name: 'Home' }
    }
  }

  // 已登录访问 guest 页面（登录/注册） → 跳首页
  if (to.meta.guest && token) {
    return { name: 'Home' }
  }
})

export default router
