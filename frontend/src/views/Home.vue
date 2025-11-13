<template>
  <div class="home-container">
    <section class="hero">
      <div class="title-container">
        <h1 class="title">浙江大学课程资源共享计划</h1>
        <router-link to="/introduction" class="intro-link">介绍</router-link>
      </div>
      <p>汇集优质教学资源，助力求是学子的学术探索</p>
      
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
          <button @click="handleSearch" class="hero-search-button">
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
    
    <section class="features">
      <div class="feature-card">
        <div class="feature-icon">📚</div>
        <h3>课程检索</h3>
        <p>快速查找各院系专业课程资源，覆盖本科生到研究生全部课程体系。</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">📝</div>
        <h3>资源丰富</h3>
        <p>包含课件、习题、实验报告、笔记和历年考题等多种类型的学习资料。</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">👥</div>
        <h3>共享协作</h3>
        <p>加入我们的学习社区，共同分享知识，互相交流学习心得与方法。</p>
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
/* 关键修改：设置全屏背景样式 */
html, body, #app {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
}

body {
  background-image: url('/images/background.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  background-repeat: no-repeat;
  position: relative;
}

body::before {
  content: "";
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('/images/background.jpg');
  background-size: cover;
  background-position: center;
  z-index: -1;
}

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
.hero, .feature-card {
  backdrop-filter: blur(4px); /* 背景模糊效果 */
  background-color: rgba(255, 255, 255, 0.7); /* 半透明白色背景 */
}

.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 3rem 1rem;
  background: linear-gradient(135deg, #3498db, #2c3e50);
  color: white;
  border-radius: 12px;
  margin-bottom: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

/* 标题容器和介绍链接样式 */
.title-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 1.2rem;
  position: relative;
}

/* 改进标题样式 */
.hero h1 {
  font-size: 2.8rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
  font-family: "Microsoft YaHei", "Hiragino Sans GB", "STHeiti", "SimHei", sans-serif;
  margin: 0;
}

.intro-link {
  display: inline-block;
  padding: 0.3rem 1rem;
  background-color: rgba(255, 255, 255, 0.3);
  color: white;
  text-decoration: none;
  border-radius: 20px;
  font-size: 1rem;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.intro-link:hover {
  background-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 为标题添加下划线装饰 */
.title-container:after {
  content: "";
  position: absolute;
  bottom: -0.3em;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #ffffff, transparent);
  border-radius: 3px;
}

.hero p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  max-width: 600px;
  letter-spacing: 0.03em;
  line-height: 1.6;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
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
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  border-radius: 50px;
  background: white;
  transition: all 0.3s ease;
}

.hero-search:hover, .hero-search:focus-within {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.2);
}

.search-icon {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  z-index: 2;
}

.hero-search-input {
  flex: 1;
  padding: 1rem 1rem 1rem 50px;
  font-size: 1.1rem;
  border: none;
  border-radius: 50px 0 0 50px;
  outline: none;
  color: #333;
  background: transparent;
  box-shadow: none;
  z-index: 1;
}

.hero-search-input::placeholder {
  color: #aaa;
}

.hero-search-button {
  padding: 0.8rem 2rem;
  font-size: 1.1rem;
  background: linear-gradient(45deg, #e74c3c, #c0392b);
  color: white;
  border: none;
  border-radius: 0 50px 50px 0;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
  letter-spacing: 0.03em;
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.1);
}

.hero-search-button:hover {
  background: linear-gradient(45deg, #c0392b, #e74c3c);
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.2);
  transform: translateY(-1px);
}

.hero-search-button:active {
  transform: translateY(1px);
  box-shadow: inset 0 3px 3px rgba(0, 0, 0, 0.1);
}

/* 热门搜索标签 */
.search-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.7rem;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.9);
}

.search-tags span {
  opacity: 0.8;
}

.search-tags a {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  background: rgba(255, 255, 255, 0.2);
  padding: 0.2rem 0.8rem;
  border-radius: 20px;
  transition: all 0.2s;
}

.search-tags a:hover {
  background: rgba(255, 255, 255, 0.3);
  color: white;
  transform: translateY(-1px);
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  padding: 1rem;
}

.feature-card {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
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