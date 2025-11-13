import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from './stores/user'
import axios from 'axios'
import './style.css'
import App from './App.vue'

// Import components for router
import Home from './views/Home.vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import Search from './views/Search.vue'
import NotFound from './views/NotFound.vue'
import Introduction from './views/Introduction.vue' // 新增导入介绍页面组件
import Upload from './views/Upload.vue'
import Resources from './views/Resources.vue'

// Create pinia store
const pinia = createPinia()

// Configure axios
axios.defaults.baseURL = 'http://localhost:8080'
const token = localStorage.getItem('token')
if (token) {
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

// Create router
const routes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/search', component: Search },
  { path: '/introduction', component: Introduction }, // 新增介绍页面路由
  { path: '/resources', component: Resources, meta: { requiresAuth: true } },
  { path: '/upload', component: Upload, meta: { requiresAuth: true } },
  { path: '/:pathMatch(.*)*', component: NotFound }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard: protect routes with requiresAuth
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem('token')
  if (to.meta?.requiresAuth && !isLoggedIn) {
    next({ path: '/login' })
  } else {
    next()
  }
})

// Create and mount the app
const app = createApp(App)
app.use(pinia)
app.use(router)

// If a token exists (e.g., after刷新), fetch profile to show username
try {
  const store = useUserStore()
  if (token) {
    store.fetchUserProfile()
  }
} catch (e) {
  // ignore; pinia not ready or other transient issues
}
app.mount('#app')
