<template>
  <div class="page">
    <div class="page-hd">
      <h1 class="page-title">企業検索</h1>
      <p class="page-sub">条件を設定して絞り込み検索。条件なしで検索すると全件が対象になります。</p>
    </div>

    <QueryBuilder @search="onSearch" @reset="onReset" />

    <n-alert v-if="store.error" type="error" :show-icon="true" style="margin-top: 0.75rem;">
      {{ store.error }}
    </n-alert>

    <template v-if="store.result">
      <div class="result-toolbar">
        <div class="result-info">
          <span class="result-num">{{ store.result.totalElements.toLocaleString() }}</span>
          <span class="result-label">件の企業が見つかりました</span>
          <n-text depth="3" style="font-size: 0.75rem;">（{{ rangeStart }}〜{{ rangeEnd }} 件表示中）</n-text>
        </div>
        <ColumnSettingsPanel
          :model-value="visibleColumns"
          @update:model-value="onColumnsUpdate"
        />
      </div>

      <CompanyDataTable
        :companies="store.result.content"
        :total-records="store.result.totalElements"
        :loading="store.loading"
        :page="store.currentPage"
        :page-size="pageSize"
        :sort-field="sortField"
        :sort-order="sortOrder"
        :visible-columns="visibleColumns"
        @page-change="onPageChange"
        @sort-change="onSortChange"
        @row-click="goToDetail"
      />
    </template>

    <div v-else-if="!store.loading && !store.error" class="empty-start">
      <div class="empty-icon-wrap">
        <n-icon :component="BusinessOutline" size="36" color="#6366f1" />
      </div>
      <p class="empty-title">企業情報を検索</p>
      <p class="empty-sub">上の検索フォームに条件を入力して「検索」ボタンを押してください</p>
    </div>

    <div v-if="store.loading && !store.result" class="initial-loading">
      <n-spin size="medium" />
      <n-text depth="3">検索中...</n-text>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NAlert, NSpin, NIcon, NText } from 'naive-ui'
import { BusinessOutline } from '@vicons/ionicons5'
import QueryBuilder from '@/components/search/QueryBuilder.vue'
import CompanyDataTable from '@/components/table/CompanyDataTable.vue'
import ColumnSettingsPanel from '@/components/table/ColumnSettingsPanel.vue'
import { useCompanySearchStore } from '@/stores/companySearch'
import { useColumnSettings } from '@/composables/useColumnSettings'

const router = useRouter()
const store = useCompanySearchStore()
const { visibleColumns, loadSettings, saveSettings } = useColumnSettings()

const sortField = ref('capitalStock')
const sortOrder = ref(-1)
const pageSize = ref(20)

const rangeStart = computed(() => store.currentPage * pageSize.value + 1)
const rangeEnd = computed(() => Math.min((store.currentPage + 1) * pageSize.value, store.result?.totalElements ?? 0))

onMounted(() => loadSettings())

function onSearch(body) {
  store.currentPage = 0
  store.search({ ...body, sort: sortField.value ? [{ field: sortField.value, direction: sortOrder.value === 1 ? 'ASC' : 'DESC' }] : [], page: 0, size: pageSize.value })
}
function onReset() { store.reset() }
function onColumnsUpdate(cols) { visibleColumns.value = cols; saveSettings() }
function onPageChange({ page, rows }) { if (rows && rows !== pageSize.value) pageSize.value = rows; store.changePage(page, rows) }
function onSortChange({ sortField: sf, sortOrder: so }) {
  sortField.value = sf; sortOrder.value = so
  if (store.lastSearchBody) store.search({ ...store.lastSearchBody, sort: sf ? [{ field: sf, direction: so === 1 ? 'ASC' : 'DESC' }] : [], page: 0 })
}
function goToDetail(corporateNumber) { router.push({ name: 'CompanyDetail', params: { corporateNumber } }) }
</script>

<style scoped>
.page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* ページヘッダー */
.page-hd {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(79,70,229,0.12);
  position: relative;
}

/* 左アクセントライン */
.page-hd::before {
  content: '';
  position: absolute;
  left: -1.5rem;
  top: 0;
  bottom: 1rem;
  width: 3px;
  background: linear-gradient(to bottom, #4f46e5, #a5b4fc);
  border-radius: 0 2px 2px 0;
}

.page-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.page-sub {
  margin: 0;
  font-size: 0.8rem;
  color: #94a3b8;
  letter-spacing: 0.01em;
}

/* 結果ツールバー */
.result-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.6rem 0.875rem;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(8px);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.result-info { display: flex; align-items: baseline; gap: 0.3rem; flex-wrap: wrap; }

.result-num {
  font-size: 1.25rem;
  font-weight: 700;
  color: #4f46e5;
  font-family: 'Fira Code', monospace;
  letter-spacing: -0.03em;
}

.result-label { font-size: 0.82rem; font-weight: 500; color: #475569; }

/* 初期状態 */
.empty-start {
  text-align: center;
  padding: 5rem 2rem 4rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.empty-icon-wrap {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(99,102,241,0.2);
}

.empty-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 600;
  color: #334155;
  letter-spacing: -0.01em;
}

.empty-sub {
  margin: 0;
  font-size: 0.82rem;
  color: #94a3b8;
  line-height: 1.7;
}

.initial-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 3rem;
  color: #94a3b8;
  font-size: 0.875rem;
}
</style>
