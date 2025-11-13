<template>
  <div class="home-container">
    <section class="hero">
      <div class="title-container">
        <h1 class="title">ZJU ShareHub</h1>
      </div>
      <p class="subtitle">简洁 · 聚合 · 共享 —— 在一个页面里找到所有需要的浙大课程资料</p>
      
      <!-- 美化搜索框区域 -->
      <div class="hero-search-container">
        <div class="hero-search">
          <div class="search-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" viewBox="0 0 16 16">
              <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1.007 1.007 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0z"/>
            </svg>
          </div>
          <input 
            v-model="searchQuery" 
            @keyup.enter="handleSearch"
            placeholder="搜索课程、资料或专业..." 
            class="hero-search-input"
          />
          <button @click="handleSearch" class="search-button">
            <span>搜索</span>
          </button>
        </div>
        <div class="search-tags">
          <span>热门搜索:</span>
          <a href="#" @click.prevent="quickSearch('高等数学')">高等数学</a>
          <a href="#" @click.prevent="quickSearch('数据结构')">数据结构</a>
          <a href="#" @click.prevent="quickSearch('计算机网络')">计算机网络</a>
        </div>
      </div>
    </section>
    
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const searchQuery = ref('');

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ 
      path: '/search', 
      query: { q: searchQuery.value } 
    });
  }
};

const quickSearch = (term) => {
  searchQuery.value = term;
  handleSearch();
};
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}

body { background: #ffffff; position: relative; }

/* 容器样式修改 */
.home-container {
  display: flex;
  flex-direction: column;
  gap: 3rem;
  min-height: 100vh;
  width: 100%;
  padding: 2rem;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

/* 添加半透明覆盖层，提高文本可读性 */
.hero { background-color: #fff; }

.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 2.5rem 1.25rem;
  background: #fff;
  color: var(--text);
  border-radius: 16px;
  margin-bottom: 2rem;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
}

/* 标题容器样式 */
.title-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.2rem;
  position: relative;
}

/* 改进标题样式 */
.hero h1 {
  font-size: clamp(2.5rem, 4vw, 3.6rem);
  font-weight: 700;
  letter-spacing: 0.02em;
  font-family: "Microsoft YaHei", "Hiragino Sans GB", "STHeiti", "SimHei", sans-serif;
  margin: 0;
  color: #111827;
}

.subtitle {
  font-size: 1.1rem;
  margin-bottom: 1.6rem;
  max-width: 720px;
  letter-spacing: 0.01em;
  line-height: 1.7;
  color: #6b7280;
}

/* 美化搜索区域 */
.hero-search-container {
  width: 100%;
  max-width: 650px;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.hero-search {
  display: flex;
  width: 100%;
  position: relative;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  transition: box-shadow .2s ease, transform .2s ease, border-color .2s ease;
}

.hero-search:hover, .hero-search:focus-within { transform: translateY(-1px); box-shadow: var(--shadow-md); border-color: rgba(52,152,219,.35); }

.search-icon {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #9aa4b2;
  z-index: 2;
}

.hero-search-input {
  flex: 1;
  padding: 0.9rem 1rem 0.9rem 52px;
  font-size: 1.05rem;
  border: none;
  border-radius: 14px 0 0 14px;
  outline: none;
  color: var(--text);
  background: transparent;
  box-shadow: none;
  z-index: 1;
}

.hero-search-input::placeholder {
  color: #aaa;
}

/* Button now uses global .search-button styles */

/* 热门搜索标签 */
.search-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.7rem;
  font-size: 0.95rem;
  color: var(--muted);
}

.search-tags span {
  opacity: 0.9;
}

.search-tags a {
  color: var(--primary);
  text-decoration: none;
  background: rgba(52,152,219,0.08);
  padding: 0.25rem 0.8rem;
  border-radius: 999px;
  transition: all 0.15s ease;
  border: 1px solid rgba(52,152,219,0.25);
}

.search-tags a:hover {
  background: rgba(52,152,219,0.14);
  color: var(--primary);
  transform: translateY(-1px);
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  padding: 1rem;
}

.feature-card {
  background-color: #fff;
  padding: 2rem;
  border-radius: 14px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: #333;
}

.feature-card p {
  color: #666;
}

/* 添加响应式字体大小 */
@media (max-width: 768px) {
  .hero h1 {
    font-size: 2.2rem;
  }
  
  .title-container {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .intro-link {
    margin-top: 0.5rem;
  }
  
  .hero-search {
    flex-direction: column;
    border-radius: 10px;
  }
  
  .hero-search-input {
    padding: 1rem 1rem 1rem 50px;
    border-radius: 10px;
    width: 100%;
    box-sizing: border-box;
  }
  
  .hero-search-button {
    width: 100%;
    border-radius: 0 0 10px 10px;
  }
  
  .search-icon {
    top: 25%;
  }
}

@media (max-width: 480px) {
  .hero h1 {
    font-size: 1.8rem;
  }
  
  .hero-search-container {
    width: 90%;
  }
}
</style>
