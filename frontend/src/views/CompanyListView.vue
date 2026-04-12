<template>
  <div class="page">
    <h1 class="page-title">企業検索</h1>

    <!-- 検索フォーム -->
    <form class="search-form" @submit.prevent="onSearch">
      <div class="form-row">
        <label class="form-label">
          法人名
          <input v-model="form.name" class="form-input" type="text" placeholder="例: テスト株式会社" maxlength="200" />
        </label>
        <label class="form-label">
          所在地
          <input v-model="form.location" class="form-input" type="text" placeholder="例: 東京都" maxlength="200" />
        </label>
      </div>
      <div class="form-row">
        <label class="form-label">
          資本金（円以上）
          <input v-model.number="form.minCapital" class="form-input" type="number" placeholder="例: 10000000" min="0" />
        </label>
        <label class="form-label">
          従業員数（名以上）
          <input v-model.number="form.minEmployees" class="form-input" type="number" placeholder="例: 100" min="0" />
        </label>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '検索中...' : '検索' }}
        </button>
        <button type="button" class="btn-secondary" @click="onReset">リセット</button>
      </div>
    </form>

    <!-- エラー -->
    <div v-if="error" class="alert-error">{{ error }}</div>

    <!-- 検索結果 -->
    <template v-if="result">
      <div class="result-summary">
        <span>{{ result.totalElements.toLocaleString() }} 件中
          {{ currentRangeStart }}〜{{ currentRangeEnd }} 件を表示</span>
      </div>

      <div v-if="result.content.length === 0" class="empty-state">
        該当する企業が見つかりませんでした。
      </div>

      <table v-else class="result-table">
        <thead>
          <tr>
            <th>法人番号</th>
            <th>法人名</th>
            <th>所在地</th>
            <th class="num-col">資本金</th>
            <th class="num-col">従業員数</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="c in result.content"
            :key="c.corporateNumber"
            class="clickable-row"
            @click="goToDetail(c.corporateNumber)"
          >
            <td class="corp-num">{{ c.corporateNumber }}</td>
            <td class="company-name">{{ c.name }}</td>
            <td>{{ c.location || '—' }}</td>
            <td class="num-col">{{ formatCapital(c.capitalStock) }}</td>
            <td class="num-col">{{ c.employeeNumber != null ? c.employeeNumber.toLocaleString() + ' 名' : '—' }}</td>
          </tr>
        </tbody>
      </table>

      <!-- ページネーション -->
      <div v-if="result.totalPages > 1" class="pagination">
        <button :disabled="currentPage === 0" @click="changePage(currentPage - 1)">‹ 前</button>
        <span class="page-info">{{ currentPage + 1 }} / {{ result.totalPages }}</span>
        <button :disabled="currentPage >= result.totalPages - 1" @click="changePage(currentPage + 1)">次 ›</button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { searchCompanies } from '@/api/companies'

const router = useRouter()

const form = reactive({ name: '', location: '', minCapital: null, minEmployees: null })
const result = ref(null)
const loading = ref(false)
const error = ref(null)
const currentPage = ref(0)
const PAGE_SIZE = 20

const currentRangeStart = computed(() => currentPage.value * PAGE_SIZE + 1)
const currentRangeEnd = computed(() =>
  Math.min((currentPage.value + 1) * PAGE_SIZE, result.value?.totalElements ?? 0)
)

async function fetchPage(page) {
  loading.value = true
  error.value = null
  try {
    const params = {
      page,
      size: PAGE_SIZE,
      ...(form.name && { name: form.name }),
      ...(form.location && { location: form.location }),
      ...(form.minCapital != null && form.minCapital !== '' && { minCapital: form.minCapital }),
      ...(form.minEmployees != null && form.minEmployees !== '' && { minEmployees: form.minEmployees }),
    }
    result.value = await searchCompanies(params)
    currentPage.value = page
  } catch (e) {
    error.value = e.response?.data?.error ?? 'エラーが発生しました。しばらく時間をおいてから再試行してください。'
  } finally {
    loading.value = false
  }
}

function onSearch() {
  fetchPage(0)
}

function onReset() {
  form.name = ''
  form.location = ''
  form.minCapital = null
  form.minEmployees = null
  result.value = null
  error.value = null
  currentPage.value = 0
}

function changePage(page) {
  fetchPage(page)
}

function goToDetail(corporateNumber) {
  router.push({ name: 'CompanyDetail', params: { corporateNumber } })
}

function formatCapital(value) {
  if (value == null) return '—'
  if (value >= 100_000_000) {
    const oku = Math.floor(value / 100_000_000)
    const man = Math.floor((value % 100_000_000) / 10_000)
    return man > 0 ? `${oku}億${man.toLocaleString()}万円` : `${oku}億円`
  }
  if (value >= 10_000) {
    return `${Math.floor(value / 10_000).toLocaleString()}万円`
  }
  return `${value.toLocaleString()}円`
}
</script>

<style scoped>
.page { max-width: 1100px; margin: 0 auto; padding: 2rem 1rem; }
.page-title { font-size: 1.6rem; font-weight: 700; margin-bottom: 1.5rem; color: #1a202c; }

/* フォーム */
.search-form { background: #f7fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.5rem; margin-bottom: 1.5rem; }
.form-row { display: flex; gap: 1rem; margin-bottom: 1rem; flex-wrap: wrap; }
.form-label { display: flex; flex-direction: column; gap: 0.3rem; flex: 1; min-width: 200px; font-size: 0.875rem; font-weight: 600; color: #4a5568; }
.form-input { padding: 0.5rem 0.75rem; border: 1px solid #cbd5e0; border-radius: 6px; font-size: 0.95rem; outline: none; transition: border-color 0.2s; }
.form-input:focus { border-color: #3b82f6; }
.form-actions { display: flex; gap: 0.75rem; margin-top: 0.5rem; }
.btn-primary { padding: 0.55rem 1.5rem; background: #3b82f6; color: #fff; border: none; border-radius: 6px; font-size: 0.95rem; font-weight: 600; cursor: pointer; transition: background 0.2s; }
.btn-primary:hover:not(:disabled) { background: #2563eb; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary { padding: 0.55rem 1.25rem; background: #fff; color: #4a5568; border: 1px solid #cbd5e0; border-radius: 6px; font-size: 0.95rem; cursor: pointer; transition: background 0.2s; }
.btn-secondary:hover { background: #f7fafc; }

/* エラー */
.alert-error { background: #fff5f5; border: 1px solid #feb2b2; color: #c53030; border-radius: 6px; padding: 0.75rem 1rem; margin-bottom: 1rem; font-size: 0.9rem; }

/* 結果 */
.result-summary { font-size: 0.875rem; color: #718096; margin-bottom: 0.75rem; }
.empty-state { text-align: center; color: #718096; padding: 3rem; }

.result-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.result-table th { background: #edf2f7; text-align: left; padding: 0.6rem 0.75rem; font-weight: 600; color: #4a5568; border-bottom: 2px solid #cbd5e0; white-space: nowrap; }
.result-table td { padding: 0.6rem 0.75rem; border-bottom: 1px solid #e2e8f0; vertical-align: middle; }
.clickable-row { cursor: pointer; transition: background 0.1s; }
.clickable-row:hover { background: #ebf8ff; }
.corp-num { font-family: monospace; font-size: 0.82rem; color: #718096; }
.company-name { font-weight: 600; color: #2b6cb0; }
.num-col { text-align: right; white-space: nowrap; }

/* ページネーション */
.pagination { display: flex; align-items: center; justify-content: center; gap: 1rem; margin-top: 1.5rem; }
.pagination button { padding: 0.4rem 1rem; border: 1px solid #cbd5e0; border-radius: 6px; background: #fff; cursor: pointer; font-size: 0.9rem; transition: background 0.2s; }
.pagination button:hover:not(:disabled) { background: #ebf8ff; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: 0.875rem; color: #4a5568; }
</style>
