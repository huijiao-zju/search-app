<template>
  <div class="upload-container">
    <h2>上传课程资源</h2>
    <p class="hint">请输入课程名称并选择附件（单个文件最大 50MB，可多选）。</p>

    <form @submit.prevent="submit" class="form">
      <div class="form-group">
        <label for="title">课程名称</label>
        <input id="title" v-model.trim="title" type="text" required placeholder="例如：高等数学 A(1)" />
      </div>

      <div class="form-group">
        <label for="college">学院</label>
        <input id="college" v-model.trim="college" type="text" required placeholder="例如：数学科学学院" />
      </div>

      <div class="form-group">
        <label for="type">类型</label>
        <select id="type" v-model="selectedType">
          <option value="NOTE">学习笔记</option>
          <option value="EXAM">回忆卷</option>
        </select>
      </div>

      <div class="form-group">
        <label for="files">附件</label>
        <input id="files" type="file" multiple @change="onFiles" />
        <ul v-if="selectedFiles.length" class="file-list">
          <li v-for="(f, i) in selectedFiles" :key="i" class="file-row">
            <div class="file-main">
              <strong>{{ f.name }}</strong>
              <span class="muted">{{ formatSize(f.size) }}</span>
              <span v-if="f.size > MAX_BYTES" class="bad"> 超过50MB</span>
            </div>
            <div class="file-meta">
              <span class="tag">{{ selectedType === 'EXAM' ? '回忆卷' : '学习笔记' }}</span>
            </div>
          </li>
        </ul>
      </div>

      <div class="actions">
        <button type="submit" :disabled="submitting || invalid">{{ submitting ? '提交中...' : '提交' }}</button>
        <span v-if="error" class="error">{{ error }}</span>
        <span v-if="success" class="success">上传成功！资源ID：{{ success.id }}</span>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'

const MAX_BYTES = 50 * 1024 * 1024
const title = ref('')
const selectedFiles = ref([])
const selectedType = ref('NOTE')
const college = ref('')
const submitting = ref(false)
const error = ref('')
const success = ref(null)

const invalid = computed(() => {
  if (!title.value.trim()) return true
  if (!selectedFiles.value.length) return true
  return selectedFiles.value.some(f => f.size > MAX_BYTES)
})

const onFiles = (e) => {
  const files = Array.from(e.target.files || [])
  selectedFiles.value = files
}

const formatSize = (n) => {
  if (n > 1024 * 1024) return (n / (1024 * 1024)).toFixed(2) + ' MB'
  if (n > 1024) return (n / 1024).toFixed(2) + ' KB'
  return n + ' B'
}

const submit = async () => {
  error.value = ''
  success.value = null
  if (invalid.value) {
    error.value = '请填写标题并选择合法大小的附件'
    return
  }
  const form = new FormData()
  form.append('title', title.value.trim())
  form.append('college', college.value.trim())
  form.append('type', selectedType.value)
  selectedFiles.value.forEach((f) => {
    form.append('files', f)
  })
  submitting.value = true
  try {
    const res = await axios.post('/api/resources', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    success.value = res.data
    // 清空
    title.value = ''
    college.value = ''
    selectedType.value = 'NOTE'
    selectedFiles.value = []
  } catch (e) {
    error.value = e.response?.data || '上传失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.upload-container { max-width: 720px; margin: 0 auto; background: #fff; padding: 1.5rem; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.08); }
h2 { margin: 0 0 0.5rem; }
.hint { color: #666; margin-bottom: 1rem; }
.form { display: flex; flex-direction: column; gap: 1rem; }
.form-group { display: flex; flex-direction: column; gap: .5rem; }
input[type="text"] { padding: .6rem; border: 1px solid #ddd; border-radius: 4px; }
input[type="file"] { padding: .4rem 0; }
.file-list { margin: 0; padding-left: 0; color: #555; list-style: none; display: flex; flex-direction: column; gap: .6rem; }
.file-row { display: flex; justify-content: space-between; align-items: center; gap: 1rem; border: 1px solid var(--border); padding: .6rem .75rem; border-radius: 8px; }
.file-main .muted { color: #888; margin-left: .5rem; font-size: .9em; }
.file-meta { display: flex; align-items: center; gap: .4rem; }
.file-meta select { padding: .35rem .5rem; border: 1px solid #ddd; border-radius: 6px; }
.actions { display: flex; align-items: center; gap: 1rem; }
button { background: #3498db; color: #fff; border: 0; padding: .6rem 1rem; border-radius: 4px; cursor: pointer; }
button:disabled { background: #95a5a6; cursor: not-allowed; }
.error { color: #e74c3c; }
.success { color: #27ae60; }
.bad { color: #e74c3c; margin-left: .5rem; }
</style>
