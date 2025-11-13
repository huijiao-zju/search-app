<template>
  <div class="resources">
    <div class="header">
      <h2>课程资源列表</h2>
      <router-link to="/upload" class="btn">上传新资源</router-link>
    </div>
    <p v-if="canDelete" class="delete-hint">管理员可点击卡片后按 Delete 快捷键删除资源。</p>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else>
      <div v-if="!resources.length" class="empty">暂无资源，去上传一个吧。</div>

      <div
        class="resource-card"
        v-for="r in resources"
        :key="r.id"
        :class="{ selected: selectedResourceId === r.id }"
        tabindex="0"
        @click="selectResource(r.id)"
      >
        <div class="title-row">
          <h3>{{ r.title }}</h3>
          <span v-if="r.college" class="college">{{ r.college }}</span>
          <span class="date">{{ formatDate(r.createdAt) }}</span>
          <span v-if="r.uploaderName" class="uploader">由 {{ r.uploaderName }} 上传</span>
          <button
            v-if="canDelete"
            class="delete-btn"
            :disabled="deletingId === r.id"
            @click.stop="deleteResource(r.id)"
          >
            {{ deletingId === r.id ? '删除中...' : '删除' }}
          </button>
        </div>
        <ul class="attachments">
          <li v-for="a in r.attachments" :key="a.id" class="attachment">
            <span class="name">{{ a.name }}</span>
            <span class="cat" :class="a.category">{{ a.category === 'EXAM' ? '回忆卷' : '学习笔记' }}</span>
            <span class="size">{{ formatSize(a.size) }}</span>
            <button class="dl" @click.stop="download(r.id, a)">下载</button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import axios from 'axios'
import { useUserStore } from '../stores/user'

const resources = ref([])
const loading = ref(false)
const error = ref('')
const selectedResourceId = ref(null)
const deletingId = ref(null)

const userStore = useUserStore()
const canDelete = computed(() => (userStore.user?.username || '').toLowerCase() === 'testqwq')

const fetchList = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await axios.get('/api/resources')
    resources.value = res.data || []
    if (!resources.value.some(r => r.id === selectedResourceId.value)) {
      selectedResourceId.value = null
    }
  } catch (e) {
    error.value = e.response?.data?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const formatSize = (n) => {
  if (n > 1024 * 1024) return (n / (1024 * 1024)).toFixed(2) + ' MB'
  if (n > 1024) return (n / 1024).toFixed(2) + ' KB'
  return n + ' B'
}

const formatDate = (iso) => {
  try { return new Date(iso).toLocaleString() } catch { return '' }
}

const download = async (resourceId, att) => {
  try {
    const res = await axios.get(`/api/resources/${resourceId}/download/${att.id}`, { responseType: 'blob' })
    // Try to get filename from header; fallback to attachment name
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

const selectResource = (id) => {
  if (!canDelete.value) return
  selectedResourceId.value = id
}

const deleteResource = async (resourceId) => {
  if (!canDelete.value || deletingId.value) return
  if (!window.confirm('确定要删除该资源及其所有附件吗？')) return
  deletingId.value = resourceId
  try {
    await axios.delete(`/api/resources/${resourceId}`)
    selectedResourceId.value = null
    await fetchList()
  } catch (e) {
    error.value = e.response?.data || '删除失败'
  } finally {
    deletingId.value = null
  }
}

const onKey = (event) => {
  if (!canDelete.value) return
  if (event.key === 'Delete' && selectedResourceId.value) {
    event.preventDefault()
    deleteResource(selectedResourceId.value)
  }
}

onMounted(() => {
  fetchList()
  document.addEventListener('keydown', onKey)
})
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKey)
})
</script>

<style scoped>
.resources { max-width: 900px; margin: 0 auto; background: #fff; padding: 1.5rem; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.delete-hint { color: #c0392b; font-size: .9rem; margin-top: -.5rem; margin-bottom: 1rem; }
.btn { background: #3498db; color: #fff; text-decoration: none; padding: .5rem .8rem; border-radius: 4px; }
.loading { color: #666; }
.error { color: #e74c3c; }
.empty { color: #666; padding: .5rem 0; }
.resource-card { border: 1px solid #eee; border-radius: 6px; padding: 1rem; margin-bottom: 1rem; outline: none; }
.resource-card.selected { border-color: #c0392b; box-shadow: 0 0 0 2px rgba(192,57,43,.2); }
.title-row { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: .5rem; gap: .5rem; }
.title-row h3 { margin: 0; }
.date { color: #888; font-size: .9rem; }
.uploader { color: #6b7280; font-size: .85rem; }
.college { color: #3498db; font-size: .9rem; margin-left: .5rem; }
.attachments { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: .5rem; }
.attachment { display: flex; gap: .75rem; align-items: center; }
.name { flex: 1; }
.size { color: #888; }
.dl { background: #2ecc71; color: #fff; border: 0; border-radius: 4px; padding: .3rem .6rem; cursor: pointer; }
.dl:hover { background: #27ae60; }
.delete-btn { background: #e74c3c; color: #fff; border: 0; border-radius: 4px; padding: .25rem .6rem; cursor: pointer; }
.delete-btn[disabled] { opacity: .6; cursor: not-allowed; }
.cat { font-size: .8rem; padding: .15rem .4rem; border-radius: 999px; border: 1px solid #e5e7eb; background: #f8fafc; color: #374151; }
.cat.EXAM { border-color: rgba(231, 76, 60, .3); background: rgba(231, 76, 60, .08); color: #c0392b; }
.cat.NOTE { border-color: rgba(52, 152, 219, .3); background: rgba(52,152,219,.08); color: #2980b9; }
</style>
