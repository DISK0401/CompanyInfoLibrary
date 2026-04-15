<template>
  <div class="table-container">
    <n-data-table
      :columns="computedColumns"
      :data="companies"
      :loading="loading"
      :remote="true"
      :pagination="paginationConfig"
      :max-height="'calc(100vh - 320px)'"
      :scroll-x="scrollX"
      :row-props="rowProps"
      :row-key="row => row.corporateNumber"
      :bordered="true"
      striped
      size="small"
      @update:sorter="handleSorter"
    />
  </div>
</template>

<script setup>
import { computed, h } from 'vue'
import { NDataTable } from 'naive-ui'
import { COMPANY_FIELDS } from '@/constants/companyFields'
import { formatCapital } from '@/utils/format'

const props = defineProps({
  companies: { type: Array, default: () => [] },
  totalRecords: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  page: { type: Number, default: 0 },
  pageSize: { type: Number, default: 20 },
  sortField: { type: String, default: null },
  sortOrder: { type: Number, default: null },
  visibleColumns: { type: Array, default: () => [] },
})

const emit = defineEmits(['page-change', 'sort-change', 'row-click'])

const computedColumns = computed(() => {
  return props.visibleColumns.map(colKey => {
    const field = COMPANY_FIELDS.find(f => f.key === colKey)
    const col = {
      title: field?.label ?? colKey,
      key: colKey,
      sorter: true,
      minWidth: field?.minWidth ?? 100,
      ellipsis: { tooltip: true },
    }
    if (colKey === 'capitalStock') {
      col.render = row => h('span', { style: { fontFamily: '"Fira Code", monospace', fontSize: '0.8rem', display: 'block', textAlign: 'right' } }, formatCapital(row.capitalStock))
      col.align = 'right'
    } else if (colKey === 'employeeNumber') {
      col.render = row => row.employeeNumber != null
        ? h('span', { style: { fontFamily: '"Fira Code", monospace', fontSize: '0.8rem', display: 'block', textAlign: 'right' } }, row.employeeNumber.toLocaleString() + ' 名')
        : h('span', { style: { color: '#cbd5e1' } }, '—')
      col.align = 'right'
    } else if (colKey === 'name') {
      col.render = row => h('span', { style: { fontWeight: 500, color: '#3730a3' } }, row.name)
    } else if (colKey === 'corporateNumber') {
      col.render = row => h('span', { style: { fontFamily: '"Fira Code", monospace', fontSize: '0.75rem', color: '#94a3b8', letterSpacing: '0.03em' } }, row.corporateNumber)
    } else if (colKey === 'companyUrl') {
      col.render = row => row.companyUrl
        ? h('a', { href: row.companyUrl, target: '_blank', rel: 'noopener', style: { color: '#4f46e5', fontSize: '0.8rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', display: 'block', maxWidth: '180px' }, onClick: e => e.stopPropagation() }, row.companyUrl)
        : h('span', { style: { color: '#cbd5e1' } }, '—')
    } else if (colKey === 'dateOfEstablishment') {
      col.render = row => h('span', { style: { fontFamily: '"Fira Code", monospace', fontSize: '0.8rem' } }, row.dateOfEstablishment?.replace(/-/g, '/') ?? '—')
    } else if (colKey === 'foundingYear') {
      col.render = row => h('span', { style: { fontFamily: '"Fira Code", monospace', fontSize: '0.8rem', display: 'block', textAlign: 'right' } }, row.foundingYear ?? '—')
      col.align = 'right'
    } else {
      col.render = row => h('span', { style: { color: row[colKey] == null ? '#cbd5e1' : 'inherit' } }, row[colKey] ?? '—')
    }
    return col
  })
})

const paginationConfig = computed(() => ({
  page: props.page + 1,
  pageSize: props.pageSize,
  itemCount: props.totalRecords,
  showSizePicker: true,
  pageSizes: [20, 50, 100],
  prefix: ({ itemCount }) => `全 ${itemCount.toLocaleString()} 件`,
  onChange: (page) => emit('page-change', { page: page - 1, rows: props.pageSize }),
  onUpdatePageSize: (size) => emit('page-change', { page: 0, rows: size }),
}))

const scrollX = computed(() => {
  return props.visibleColumns.reduce((acc, key) => {
    const f = COMPANY_FIELDS.find(f => f.key === key)
    return acc + (f?.minWidth ?? 100)
  }, 0)
})

function rowProps(row) {
  return {
    style: 'cursor: pointer;',
    onClick: () => emit('row-click', row.corporateNumber),
  }
}

function handleSorter(sorter) {
  if (!sorter || sorter.order === false) {
    emit('sort-change', { sortField: null, sortOrder: null })
  } else {
    emit('sort-change', {
      sortField: sorter.columnKey,
      sortOrder: sorter.order === 'ascend' ? 1 : -1,
    })
  }
}
</script>

<style scoped>
.table-container {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
  border: 1px solid #e2e8f0;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(8px);
}

.table-container :deep(.n-data-table-th) {
  border-right: 1px solid #4b5563 !important;
  border-bottom: none !important;
}

.table-container :deep(.n-data-table-th:last-child) {
  border-right: none !important;
}

.table-container :deep(.n-data-table-td) {
  border-bottom: 1px solid #f1f5f9 !important;
  font-size: 0.875rem;
}

.table-container :deep(.n-pagination) {
  justify-content: flex-end;
  padding: 0.6rem 1rem;
  border-top: 1px solid #e8ecf0;
  background: #f8fafc;
}
</style>
