<template>
  <n-card class="section-card">
    <template #header>
      <div class="sec-hd">
        <n-icon :component="BookOutline" size="16" color="#4f46e5" />
        <span>特許情報</span>
        <n-text v-if="!loading && !error" depth="3" style="font-size:0.75rem;">{{ items.length }} 件</n-text>
      </div>
    </template>

    <n-skeleton v-if="loading" :repeat="3" text style="margin-bottom:0.5rem;" />

    <n-alert v-else-if="error" type="error" :closable="false">データの取得に失敗しました。</n-alert>

    <n-empty v-else-if="!items.length" size="small" description="データなし" />

    <n-data-table
      v-else
      :columns="columns"
      :data="items"
      :bordered="true"
      size="small"
      striped
      :max-height="360"
      virtual-scroll
    />
  </n-card>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { NCard, NDataTable, NIcon, NText, NSkeleton, NAlert, NEmpty } from 'naive-ui'
import { BookOutline } from '@vicons/ionicons5'
import { getPatents } from '@/api/companies'
import { formatJpDate } from '@/utils/format'

const props = defineProps({
  corporateNumber: { type: String, required: true },
})

const items = ref([])
const loading = ref(true)
const error = ref(false)

const columns = [
  { title: '特許種別', key: 'patentType', width: 90 },
  { title: 'タイトル', key: 'title', ellipsis: { tooltip: true }, minWidth: 200 },
  { title: '特許番号', key: 'patentNumber', width: 150 },
  { title: '取得日', key: 'dateOfPatent', width: 110, render: r => formatJpDate(r.dateOfPatent) },
  {
    title: '詳細',
    key: 'url',
    width: 90,
    render: r => r.url
      ? h('a', { href: r.url, target: '_blank', rel: 'noopener noreferrer', style: { color: '#4f46e5', fontSize: '0.85rem' } }, '詳細リンク')
      : h('span', '—'),
  },
]

onMounted(async () => {
  try {
    const data = await getPatents(props.corporateNumber)
    items.value = Array.isArray(data) ? data : []
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.section-card {
  border-radius: 10px;
}

.sec-hd {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sec-hd span {
  font-size: 0.9rem;
  font-weight: 600;
  color: #0f172a;
}
</style>
