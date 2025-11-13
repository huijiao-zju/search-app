<template>
  <div class="app-container">
    <header>
      <div class="logo"></div>
      <div class="search-container">
        <input 
          v-model="searchQuery" 
          @keyup.enter="handleSearch"
          placeholder="搜索..."
          class="search-input" 
        />
        <button @click="handleSearch" class="search-button">搜索</button>
      </div>
      <nav>
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link v-if="!isLoggedIn" to="/login" class="nav-link">登录</router-link>
        <router-link v-if="!isLoggedIn" to="/register" class="nav-link">注册</router-link>
        <router-link v-if="isLoggedIn" to="/resources" class="nav-link">资源</router-link>
        <router-link v-if="isLoggedIn" to="/upload" class="nav-link">上传</router-link>
        <div v-if="isLoggedIn" class="user-area">
          <button class="user-badge" @click="menuOpen = !menuOpen">
            {{ currentUser?.username || currentUser?.email }}
            <svg class="caret" width="12" height="12" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7 10l5 5 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <div v-if="menuOpen" class="user-menu">
            <button class="menu-item danger" @click="logoutAndClose">退出登录</button>
          </div>
        </div>
      </nav>
    </header>
    <main>
      <router-view />
    </main>
    <footer>
      <p>© 2024 Zhejiang University</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from './stores/user';

const router = useRouter();
const userStore = useUserStore();
const searchQuery = ref('');

const isLoggedIn = computed(() => userStore.isLoggedIn);
const currentUser = computed(() => userStore.currentUser);
const menuOpen = ref(false);

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ 
      path: '/search', 
      query: { q: searchQuery.value } 
    });
  }
};

const logout = () => {
  userStore.logout();
  router.push('/');
};
const logoutAndClose = () => { menuOpen.value = false; logout(); };
</script>

<style>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.85rem 2rem;
  background: rgba(52, 152, 219, 0.9);
  color: #fff;
  backdrop-filter: saturate(120%) blur(6px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.search-container {
  display: flex;
  max-width: 500px;
  width: 100%;
}

.search-input {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px 0 0 4px;
  width: 100%;
  font-size: 1rem;
}

.search-button {
  padding: 0.5rem 1rem;
  background-color: #2980b9;
  border: none;
  border-radius: 0 4px 4px 0;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-button:hover {
  background-color: #1c6ea4;
}

nav {
  display: flex;
  gap: 1rem;
}

.nav-link {
  color: #fff;
  text-decoration: none;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.22);
}

.nav-user {
  color: #fff;
  opacity: 0.95;
  padding: 0.5rem 0.6rem;
}

main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

footer {
  text-align: center;
  padding: 1rem;
  background-color: #333;
  color: #fff;
}

/* User area (right side) */
.user-area { position: relative; margin-left: .25rem; }
.user-badge {
  display: inline-flex;
  align-items: center;
  gap: .4rem;
  padding: .45rem .7rem;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.35);
  background: rgba(255,255,255,0.15);
  color: #fff;
  cursor: pointer;
}
.user-badge .caret { opacity: .9; }

.user-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  background: #fff;
  color: #333;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(0,0,0,.10);
  min-width: 140px;
  padding: .35rem;
  z-index: 200;
}
.menu-item {
  width: 100%;
  text-align: left;
  background: transparent;
  border: 0;
  padding: .55rem .65rem;
  border-radius: 8px;
  cursor: pointer;
}
.menu-item:hover { background: rgba(0,0,0,.04); }
.menu-item.danger { color: #e74c3c; }
</style>
