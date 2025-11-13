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
        <a v-if="isLoggedIn" @click="logout" class="nav-link">退出</a>
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
</style>
