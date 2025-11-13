<template>
  <div class="search-page">
    <div class="search-header">
      <h2>搜索结果："{{ searchQuery }}"</h2>
      <div class="search-filters">
        <select v-model="sortBy" class="filter-select">
          <option value="relevance">相关度</option>
          <option value="date">日期</option>
          <option value="name">名称</option>
        </select>
      </div>
    </div>
    
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>搜索中...</p>
    </div>
    
    <div v-else-if="searchResults.length === 0" class="empty-state">
      <div class="empty-icon">🔍</div>
      <h3>未找到结果</h3>
      <p>请尝试不同的关键词或更改搜索条件</p>
    </div>
    
    <div v-else class="search-results">
      <div v-for="(result, index) in searchResults" :key="index" class="result-card">
        <h3>{{ result.title }}</h3>
        <div class="result-description">
          <template v-if="result.attachments && result.attachments.length">
            <div class="attachment" v-for="a in result.attachments" :key="a.id">
              <span class="name">{{ a.name }}</span>
              <span class="size">{{ (a.size/1024/1024).toFixed(2) }} MB</span>
              <button class="dl" @click="download(result.id, a)">下载</button>
            </div>
          </template>
          <template v-else>
            <em>无附件</em>
          </template>
        </div>
      </div>
      
      <div class="pagination">
        <button 
          :disabled="currentPage === 1" 
          @click="changePage(currentPage - 1)" 
          class="pagination-button"
        >
          上一页
        </button>
        <span class="page-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
        <button 
          :disabled="currentPage === totalPages || totalPages === 0" 
          @click="changePage(currentPage + 1)" 
          class="pagination-button"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const router = useRouter();

const searchQuery = ref('');
const loading = ref(true);
const searchResults = ref([]);
const sortBy = ref('relevance');
const currentPage = ref(1);
const totalPages = ref(0);
const itemsPerPage = 10;

const fetchSearch = async (query, page = 1) => {
  loading.value = true;
  try {
    const res = await axios.get('/api/resources/search', {
      params: { q: query, page, size: itemsPerPage, sort: sortBy.value, mode: 'and' }
    });
    const data = res.data || { content: [], totalPages: 0 };
    searchResults.value = data.content;
    totalPages.value = data.totalPages || 0;
  } catch (e) {
    console.error('搜索失败:', e);
    searchResults.value = [];
    totalPages.value = 0;
  } finally {
    loading.value = false;
  }
};

const updateSearch = async () => {
  searchQuery.value = route.query.q || '';
  if (!searchQuery.value) {
    router.push('/');
    return;
  }
  currentPage.value = parseInt(route.query.page) || 1;
  sortBy.value = route.query.sort || 'relevance';
  await fetchSearch(searchQuery.value, currentPage.value);
};

watch(sortBy, (newSortBy) => {
  router.push({ path: '/search', query: { ...route.query, sort: newSortBy } });
});

const changePage = (page) => {
  currentPage.value = page;
  router.push({ path: '/search', query: { ...route.query, page } });
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const download = async (resourceId, att) => {
  try {
    const res = await axios.get(`/api/resources/${resourceId}/download/${att.id}`, { responseType: 'blob' })
    let filename = att.name || 'download'
    const cd = res.headers?.['content-disposition'] || res.headers?.get?.('content-disposition')
    if (cd) {
      const match = /filename\*=UTF-8''([^;]+)/i.exec(cd) || /filename="?([^";]+)"?/i.exec(cd)
      if (match && match[1]) filename = decodeURIComponent(match[1])
    }
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
  } catch (e) {
    alert('下载失败')
  }
}

onMounted(updateSearch);
watch(() => route.query, updateSearch, { deep: true });
</script>

<style scoped>
.search-page {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.search-header h2 {
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.search-filters {
  display: flex;
  gap: 1rem;
}

.filter-select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  min-width: 120px;
  color: #333;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}

.filter-select:hover {
  border-color: #3498db;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.result-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  padding: 1.5rem;
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid #3498db;
}

.result-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.result-card h3 {
  margin: 0 0 0.75rem 0;
}

.result-card h3 a {
  color: #3498db;
  text-decoration: none;
}

.result-card h3 a:hover {
  text-decoration: underline;
}

.result-description {
  color: #555;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.attachment { display: flex; gap: .75rem; align-items: center; padding: .25rem 0; }
.attachment .name { flex: 1; }
.attachment .size { color: #888; font-size: .9rem; }
.dl { background: #2ecc71; color: #fff; border: 0; border-radius: 4px; padding: .3rem .6rem; cursor: pointer; }
.dl:hover { background: #27ae60; }

.result-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: #888;
}

.result-url {
  color: #27ae60;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-left-color: #3498db;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 4rem 0;
  color: #666;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.pagination-button {
  padding: 0.5rem 1rem;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  color: #333;
}

.pagination-button:hover:not(:disabled) {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
}

@media (max-width: 768px) {
  .search-page {
    padding: 1rem;
  }
  
  .search-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .result-card {
    padding: 1rem;
  }
}
</style>
