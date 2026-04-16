<template>
  <n-card class="section-card">
    <template #header>
      <div class="sec-hd">
        <n-icon :component="CartOutline" size="16" color="#4f46e5" />
        <span>調達情報</span>
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
import { CartOutline } from '@vicons/ionicons5'
import { getProcurements } from '@/api/companies'
import { formatAmount, formatJpDate } from '@/utils/format'

const props = defineProps({
  corporateNumber: { type: String, required: true },
})

const items = ref([])
const loading = ref(true)
const error = ref(false)

const numStyle = { fontFamily: 'monospace', fontSize: '0.82rem' }

const columns = [
  { title: '件名', key: 'title', ellipsis: { tooltip: true }, minWidth: 200 },
  {
    title: '金額',
    key: 'amount',
    width: 130,
    align: 'right',
    render: r => h('span', { style: numStyle }, formatAmount(r.amount)),
  },
  { title: '受注日', key: 'dateOfOrder', width: 110, render: r => formatJpDate(r.dateOfOrder) },
  { title: '担当府省', key: 'governmentDepartments', ellipsis: { tooltip: true } },
]

onMounted(async () => {
  try {
    const data = await getProcurements(props.corporateNumber)
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
